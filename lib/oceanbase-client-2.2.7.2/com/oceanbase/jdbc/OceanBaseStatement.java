// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.SQLWarning;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.nio.charset.Charset;
import com.oceanbase.jdbc.internal.util.Utils;
import java.util.regex.Matcher;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import java.util.Arrays;
import java.sql.BatchUpdateException;
import java.util.concurrent.ExecutionException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import com.oceanbase.jdbc.internal.util.scheduler.SchedulerServiceProviderHolder;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.util.Options;
import java.util.concurrent.locks.ReentrantLock;
import com.oceanbase.jdbc.internal.logging.Logger;
import java.util.Map;
import java.util.regex.Pattern;
import java.sql.Statement;

public class OceanBaseStatement implements Statement, Cloneable
{
    public static final int STMT_UNKNOWN = 0;
    public static final int STMT_SELECT = 1;
    public static final int STMT_UPDATE = 2;
    public static final int STMT_DELETE = 3;
    public static final int STMT_INSERT = 4;
    public static final int STMT_CREATE = 5;
    public static final int STMT_DROP = 6;
    public static final int STMT_ALTER = 7;
    public static final int STMT_BEGIN = 8;
    public static final int STMT_DECLARE = 9;
    public static final int STMT_CALL = 10;
    private static final Pattern identifierPattern;
    private static final Pattern escapePattern;
    private static final Map<String, String> mapper;
    private static final Logger logger;
    protected final ReentrantLock lock;
    protected final int resultSetScrollType;
    protected final int resultSetConcurrency;
    protected final Options options;
    protected final boolean canUseServerTimeout;
    protected Protocol protocol;
    protected OceanBaseConnection connection;
    protected volatile boolean closed;
    protected int queryTimeout;
    protected long maxRows;
    protected Results results;
    protected int fetchSize;
    protected boolean isFetchSizeSet;
    protected volatile boolean executing;
    protected ExceptionFactory exceptionFactory;
    private ScheduledExecutorService timeoutScheduler;
    private boolean warningsCleared;
    private boolean mustCloseOnCompletion;
    private List<String> batchQueries;
    private Future<?> timerTaskFuture;
    private boolean isTimedout;
    private int maxFieldSize;
    private boolean escape;
    protected String originalSql;
    protected String parameterSql;
    protected String utickSql;
    protected String processedSql;
    protected String rowidSql;
    protected String actualSql;
    protected String simpleSql;
    protected int sqlType;
    protected boolean includeRowid;
    protected int parameterCount;
    
    public OceanBaseStatement(final OceanBaseConnection connection, final int resultSetScrollType, final int resultSetConcurrency, final ExceptionFactory exceptionFactory) {
        this.closed = false;
        this.isFetchSizeSet = false;
        this.mustCloseOnCompletion = false;
        this.escape = true;
        this.parameterCount = -1;
        this.protocol = connection.getProtocol();
        this.connection = connection;
        this.canUseServerTimeout = connection.canUseServerTimeout();
        this.resultSetScrollType = resultSetScrollType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.lock = this.connection.lock;
        this.options = this.protocol.getOptions();
        this.exceptionFactory = exceptionFactory;
        if (this.protocol.isOracleMode() && this.options.defaultFetchSize == 0) {
            this.fetchSize = 10;
        }
        else {
            this.fetchSize = this.options.defaultFetchSize;
        }
    }
    
    public OceanBaseStatement clone(final OceanBaseConnection connection) throws CloneNotSupportedException {
        final OceanBaseStatement clone = (OceanBaseStatement)super.clone();
        clone.connection = connection;
        clone.protocol = connection.getProtocol();
        clone.timerTaskFuture = null;
        clone.batchQueries = new ArrayList<String>();
        clone.closed = false;
        clone.warningsCleared = true;
        clone.maxRows = 0L;
        clone.fetchSize = this.options.defaultFetchSize;
        clone.exceptionFactory = ExceptionFactory.of(this.exceptionFactory.getThreadId(), this.exceptionFactory.getOptions());
        return clone;
    }
    
    protected void setTimerTask(final boolean isBatch) {
        assert this.timerTaskFuture == null;
        if (this.timeoutScheduler == null) {
            this.timeoutScheduler = SchedulerServiceProviderHolder.getTimeoutScheduler();
        }
        this.timerTaskFuture = this.timeoutScheduler.schedule(() -> {
            try {
                this.isTimedout = true;
                if (!isBatch) {
                    this.protocol.cancelCurrentQuery();
                }
                this.protocol.interrupt();
            }
            catch (Throwable t) {}
        }, this.queryTimeout, TimeUnit.SECONDS);
    }
    
    protected void executeQueryPrologue(final boolean isBatch) throws SQLException {
        this.executing = true;
        if (this.closed) {
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create("execute() is called on closed statement");
        }
        this.protocol.prolog(this.maxRows, this.protocol.getProxy() != null, this.connection, this);
        if (this.queryTimeout != 0 && (!this.canUseServerTimeout || isBatch)) {
            this.setTimerTask(isBatch);
        }
    }
    
    private void stopTimeoutTask() {
        if (this.timerTaskFuture != null) {
            if (!this.timerTaskFuture.cancel(true)) {
                try {
                    this.timerTaskFuture.get();
                }
                catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                catch (ExecutionException ex) {}
            }
            this.timerTaskFuture = null;
        }
    }
    
    protected SQLException executeExceptionEpilogue(final SQLException sqle) {
        if (sqle.getSQLState() != null && sqle.getSQLState().startsWith("08")) {
            try {
                this.close();
            }
            catch (SQLException ex) {}
        }
        if (sqle.getErrorCode() == 1148 && !this.options.allowLocalInfile) {
            return this.exceptionFactory.raiseStatementError(this.connection, this).create("Usage of LOCAL INFILE is disabled. To use it enable it via the connection property allowLocalInfile=true", "42000", 1148, sqle);
        }
        if (this.isTimedout) {
            return this.exceptionFactory.raiseStatementError(this.connection, this).create("Query timed out", "70100", 1317, sqle);
        }
        final SQLException sqlException = this.exceptionFactory.raiseStatementError(this.connection, this).create(sqle);
        OceanBaseStatement.logger.error("error executing query", sqlException);
        return sqlException;
    }
    
    protected void executeEpilogue() {
        this.stopTimeoutTask();
        this.isTimedout = false;
        this.executing = false;
    }
    
    protected void executeBatchEpilogue() {
        this.executing = false;
        this.stopTimeoutTask();
        this.isTimedout = false;
        this.clearBatch();
    }
    
    private SQLException handleFailoverAndTimeout(final SQLException sqle) {
        if (sqle.getSQLState() != null && sqle.getSQLState().startsWith("08")) {
            try {
                this.close();
            }
            catch (SQLException ex) {}
        }
        if (this.isTimedout) {
            return this.exceptionFactory.raiseStatementError(this.connection, this).create("Query timed out", "70100", 1317, sqle);
        }
        return sqle;
    }
    
    protected BatchUpdateException executeBatchExceptionEpilogue(final SQLException initialSqle, final int size) {
        SQLException sqle = this.handleFailoverAndTimeout(initialSqle);
        int[] ret;
        if (this.results == null || !this.results.commandEnd()) {
            ret = new int[size];
            Arrays.fill(ret, -3);
        }
        else {
            ret = this.results.getCmdInformation().getUpdateCounts();
        }
        sqle = this.exceptionFactory.raiseStatementError(this.connection, this).create(sqle);
        OceanBaseStatement.logger.error("error executing query", sqle);
        return new BatchUpdateException(sqle.getMessage(), sqle.getSQLState(), sqle.getErrorCode(), ret, sqle);
    }
    
    private boolean executeInternal(final String sql, final int fetchSize, final int autoGeneratedKeys) throws SQLException {
        this.lock.lock();
        try {
            this.executeQueryPrologue(false);
            this.results = new Results(this, fetchSize, false, 1, false, this.resultSetScrollType, this.resultSetConcurrency, autoGeneratedKeys, this.protocol.getAutoIncrementIncrement(), sql, null);
            this.protocol.executeQuery(this.protocol.isMasterConnection(), this.results, this.getTimeoutSql(this.nativeSql(sql, this.protocol)));
            this.results.commandEnd();
            return this.results.getResultSet() != null;
        }
        catch (SQLException exception) {
            throw this.executeExceptionEpilogue(exception);
        }
        finally {
            this.executeEpilogue();
            this.lock.unlock();
        }
    }
    
    @Override
    public String enquoteLiteral(final String val) throws SQLException {
        final Matcher matcher = OceanBaseStatement.escapePattern.matcher(val);
        final StringBuffer escapedVal = new StringBuffer("'");
        while (matcher.find()) {
            matcher.appendReplacement(escapedVal, OceanBaseStatement.mapper.get(matcher.group()));
        }
        matcher.appendTail(escapedVal);
        escapedVal.append("'");
        return escapedVal.toString();
    }
    
    @Override
    public String enquoteIdentifier(String identifier, final boolean alwaysQuote) throws SQLException {
        if (this.isSimpleIdentifier(identifier)) {
            return alwaysQuote ? ("`" + identifier + "`") : identifier;
        }
        if (identifier.contains("\u0000")) {
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create("Invalid name - containing u0000 character", "42000");
        }
        if (identifier.matches("^`.+`$")) {
            identifier = identifier.substring(1, identifier.length() - 1);
        }
        return "`" + identifier.replace("`", "``") + "`";
    }
    
    @Override
    public boolean isSimpleIdentifier(final String identifier) throws SQLException {
        return identifier != null && !identifier.isEmpty() && OceanBaseStatement.identifierPattern.matcher(identifier).matches();
    }
    
    @Override
    public String enquoteNCharLiteral(final String val) throws SQLException {
        return "N'" + val.replace("'", "''") + "'";
    }
    
    private String getTimeoutSql(final String sql) {
        if (this.queryTimeout != 0 && this.canUseServerTimeout) {
            return "SET STATEMENT max_statement_time=" + this.queryTimeout + " FOR " + sql;
        }
        return sql;
    }
    
    private String nativeSql(final String sql, final Protocol protocol) throws SQLException {
        return this.escape ? Utils.nativeSql(sql, protocol) : sql;
    }
    
    public boolean testExecute(final String sql, final Charset charset) throws SQLException {
        this.lock.lock();
        try {
            this.executeQueryPrologue(false);
            this.results = new Results(this, this.fetchSize, false, 1, false, this.resultSetScrollType, this.resultSetConcurrency, 2, this.protocol.getAutoIncrementIncrement(), sql, null);
            this.protocol.executeQuery(this.protocol.isMasterConnection(), this.results, this.getTimeoutSql(this.nativeSql(sql, this.protocol)), charset);
            this.results.commandEnd();
            return this.results.getResultSet() != null;
        }
        catch (SQLException exception) {
            throw this.executeExceptionEpilogue(exception);
        }
        finally {
            this.executeEpilogue();
            this.lock.unlock();
        }
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        return this.executeInternal(sql, this.fetchSize, 2);
    }
    
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        return this.executeInternal(sql, this.fetchSize, autoGeneratedKeys);
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        return this.executeInternal(sql, this.fetchSize, 1);
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        return this.executeInternal(sql, this.fetchSize, 1);
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        if (this.options.useServerPrepStmts && this.options.useCursorFetch && this.resultSetConcurrency == 1007 && this.resultSetScrollType == 1003 && this.getFetchSize() > 0) {
            final PreparedStatement pstmt = this.connection.prepareStatement(sql);
            pstmt.setFetchSize(this.fetchSize);
            pstmt.executeQuery();
            this.results = ((ServerSidePreparedStatement)pstmt).results;
            return this.results.getResultSet();
        }
        if (this.executeInternal(sql, this.fetchSize, 2)) {
            return this.results.getResultSet();
        }
        return JDBC4ResultSet.createEmptyResultSet();
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        if (this.executeInternal(sql, this.fetchSize, 2)) {
            return 0;
        }
        return this.getUpdateCount();
    }
    
    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (this.executeInternal(sql, this.fetchSize, autoGeneratedKeys)) {
            return 0;
        }
        return this.getUpdateCount();
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        return this.executeUpdate(sql, 1);
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        return this.executeUpdate(sql, 1);
    }
    
    @Override
    public long executeLargeUpdate(final String sql) throws SQLException {
        if (this.executeInternal(sql, this.fetchSize, 2)) {
            return 0L;
        }
        return this.getLargeUpdateCount();
    }
    
    @Override
    public long executeLargeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (this.executeInternal(sql, this.fetchSize, autoGeneratedKeys)) {
            return 0L;
        }
        return this.getLargeUpdateCount();
    }
    
    @Override
    public long executeLargeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        return this.executeLargeUpdate(sql, 1);
    }
    
    @Override
    public long executeLargeUpdate(final String sql, final String[] columnNames) throws SQLException {
        return this.executeLargeUpdate(sql, 1);
    }
    
    @Override
    public void close() throws SQLException {
        this.lock.lock();
        try {
            this.closed = true;
            if (this.results != null) {
                if (this.results.getFetchSize() != 0) {
                    this.skipMoreResults();
                }
                this.results.close();
            }
            if (this.connection == null || this.connection.pooledConnection == null || this.connection.pooledConnection.noStmtEventListeners()) {
                return;
            }
            this.connection.pooledConnection.fireStatementClosed(this);
        }
        finally {
            this.protocol = null;
            this.connection = null;
            this.lock.unlock();
        }
    }
    
    public void realClose() throws SQLException {
        this.lock.lock();
        try {
            this.closed = true;
            if (this.connection == null || this.connection.pooledConnection == null || this.connection.pooledConnection.noStmtEventListeners()) {
                return;
            }
            this.connection.pooledConnection.fireStatementClosed(this);
        }
        finally {
            this.protocol = null;
            this.connection = null;
            this.lock.unlock();
        }
    }
    
    @Override
    public int getMaxFieldSize() {
        return this.maxFieldSize;
    }
    
    @Override
    public void setMaxFieldSize(final int max) {
        this.maxFieldSize = max;
    }
    
    @Override
    public int getMaxRows() {
        return (int)this.maxRows;
    }
    
    @Override
    public void setMaxRows(final int max) throws SQLException {
        if (max < 0) {
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create("max rows cannot be negative : asked for " + max, "42000");
        }
        this.maxRows = max;
    }
    
    @Override
    public long getLargeMaxRows() {
        return this.maxRows;
    }
    
    @Override
    public void setLargeMaxRows(final long max) throws SQLException {
        if (max < 0L) {
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create("max rows cannot be negative : setLargeMaxRows value is " + max, "42000");
        }
        this.maxRows = max;
    }
    
    @Override
    public void setEscapeProcessing(final boolean enable) {
        this.escape = enable;
    }
    
    @Override
    public int getQueryTimeout() {
        return this.queryTimeout;
    }
    
    @Override
    public void setQueryTimeout(final int seconds) throws SQLException {
        if (seconds < 0) {
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create("Query timeout cannot be negative : asked for " + seconds, "42000");
        }
        this.queryTimeout = seconds;
    }
    
    public void setLocalInfileInputStream(final InputStream inputStream) throws SQLException {
        this.checkClose();
        this.protocol.setLocalInfileInputStream(inputStream);
    }
    
    @Override
    public void cancel() throws SQLException {
        this.checkClose();
        final boolean locked = this.lock.tryLock();
        try {
            if (this.executing) {
                this.protocol.cancelCurrentQuery();
            }
            else if (this.results != null && this.results.getFetchSize() != 0 && !this.results.isFullyLoaded(this.protocol)) {
                try {
                    this.protocol.cancelCurrentQuery();
                    this.skipMoreResults();
                }
                catch (SQLException ex) {}
                this.results.removeFetchSize();
            }
        }
        catch (SQLException e) {
            OceanBaseStatement.logger.error("error cancelling query", e);
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create(e);
        }
        finally {
            if (locked) {
                this.lock.unlock();
            }
        }
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        this.checkClose();
        if (!this.warningsCleared) {
            return this.connection.getWarnings();
        }
        return null;
    }
    
    @Override
    public void clearWarnings() {
        this.warningsCleared = true;
    }
    
    @Override
    public void setCursorName(final String name) throws SQLException {
        throw this.exceptionFactory.raiseStatementError(this.connection, this).notSupported("Cursors are not supported");
    }
    
    @Override
    public OceanBaseConnection getConnection() {
        return this.connection;
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        if (this.results != null) {
            return this.results.getGeneratedKeys(this.protocol);
        }
        return JDBC4ResultSet.createEmptyResultSet();
    }
    
    @Override
    public int getResultSetHoldability() {
        return 1;
    }
    
    @Override
    public boolean isClosed() {
        return this.closed;
    }
    
    @Override
    public boolean isPoolable() {
        return false;
    }
    
    @Override
    public void setPoolable(final boolean poolable) {
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        this.checkClose();
        return (this.results != null) ? this.results.getResultSet() : null;
    }
    
    @Override
    public int getUpdateCount() {
        if (this.results != null && this.results.getCmdInformation() != null && !this.results.isBatch()) {
            return this.results.getCmdInformation().getUpdateCount();
        }
        return -1;
    }
    
    @Override
    public long getLargeUpdateCount() {
        if (this.results != null && this.results.getCmdInformation() != null && !this.results.isBatch()) {
            return this.results.getCmdInformation().getLargeUpdateCount();
        }
        return -1L;
    }
    
    protected void skipMoreResults() throws SQLException {
        try {
            this.protocol.skip();
            this.warningsCleared = false;
            this.connection.reenableWarnings();
        }
        catch (SQLException e) {
            OceanBaseStatement.logger.debug("error skipMoreResults", e);
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create(e);
        }
    }
    
    @Override
    public boolean getMoreResults() throws SQLException {
        return this.getMoreResults(1);
    }
    
    @Override
    public boolean getMoreResults(final int current) throws SQLException {
        this.checkClose();
        return this.results != null && this.results.getMoreResults(current, this.protocol);
    }
    
    @Override
    public int getFetchDirection() {
        return 1000;
    }
    
    @Override
    public void setFetchDirection(final int direction) {
    }
    
    @Override
    public int getFetchSize() {
        return this.fetchSize;
    }
    
    @Override
    public void setFetchSize(final int rows) throws SQLException {
        if ((this.maxRows > 0L && rows > this.maxRows) || (this.protocol.isOracleMode() && rows < 0) || (!this.protocol.isOracleMode() && rows < 0 && rows != Integer.MIN_VALUE)) {
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create("invalid fetch size ");
        }
        if ((this.protocol.isOracleMode() && rows != 0) || !this.protocol.isOracleMode()) {
            this.fetchSize = rows;
        }
        this.isFetchSizeSet = true;
    }
    
    @Override
    public int getResultSetConcurrency() {
        return this.resultSetConcurrency;
    }
    
    @Override
    public int getResultSetType() {
        return this.resultSetScrollType;
    }
    
    @Override
    public void addBatch(final String sql) throws SQLException {
        if (this.batchQueries == null) {
            this.batchQueries = new ArrayList<String>();
        }
        if (sql == null) {
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create("null cannot be set to addBatch( String sql)");
        }
        this.batchQueries.add(sql);
    }
    
    @Override
    public void clearBatch() {
        if (this.batchQueries != null) {
            this.batchQueries.clear();
        }
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        this.checkClose();
        final int size;
        if (this.batchQueries == null || (size = this.batchQueries.size()) == 0) {
            return new int[0];
        }
        this.lock.lock();
        try {
            this.internalBatchExecution(size);
            return this.results.getCmdInformation().getUpdateCounts();
        }
        catch (SQLException initialSqlEx) {
            throw this.executeBatchExceptionEpilogue(initialSqlEx, size);
        }
        finally {
            this.executeBatchEpilogue();
            this.lock.unlock();
        }
    }
    
    @Override
    public long[] executeLargeBatch() throws SQLException {
        this.checkClose();
        final int size;
        if (this.batchQueries == null || (size = this.batchQueries.size()) == 0) {
            return new long[0];
        }
        this.lock.lock();
        try {
            this.internalBatchExecution(size);
            return this.results.getCmdInformation().getLargeUpdateCounts();
        }
        catch (SQLException initialSqlEx) {
            throw this.executeBatchExceptionEpilogue(initialSqlEx, size);
        }
        finally {
            this.executeBatchEpilogue();
            this.lock.unlock();
        }
    }
    
    private void internalBatchExecution(final int size) throws SQLException {
        this.executeQueryPrologue(true);
        this.results = new Results(this, 0, true, size, false, this.resultSetScrollType, this.resultSetConcurrency, 1, this.protocol.getAutoIncrementIncrement(), null, null);
        this.protocol.executeBatchStmt(this.protocol.isMasterConnection(), this.results, this.batchQueries);
        this.results.commandEnd();
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        try {
            if (this.isWrapperFor(iface)) {
                return (T)this;
            }
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create("The receiver is not a wrapper and does not implement the interface", "42000");
        }
        catch (Exception e) {
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create("The receiver is not a wrapper and does not implement the interface", "42000");
        }
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> interfaceOrWrapper) throws SQLException {
        return interfaceOrWrapper.isInstance(this);
    }
    
    @Override
    public void closeOnCompletion() {
        this.mustCloseOnCompletion = true;
    }
    
    @Override
    public boolean isCloseOnCompletion() {
        return this.mustCloseOnCompletion;
    }
    
    public void checkCloseOnCompletion(final ResultSet resultSet) throws SQLException {
        if (this.mustCloseOnCompletion && !this.closed && this.results != null && resultSet.equals(this.results.getResultSet())) {
            this.close();
        }
    }
    
    protected void checkClose() throws SQLException {
        if (this.closed) {
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create("Cannot do an operation on a closed statement");
        }
    }
    
    public Results getResults() {
        return this.results;
    }
    
    public String getOriginalSql() {
        return this.originalSql;
    }
    
    public int getSqlType() {
        return this.sqlType;
    }
    
    static {
        identifierPattern = Pattern.compile("[0-9a-zA-Z\\$_\\u0080-\\uFFFF]*", 192);
        escapePattern = Pattern.compile("[\u0000'\"\b\n\r\t\u001a\\\\]");
        mapper = new HashMap<String, String>();
        logger = LoggerFactory.getLogger(OceanBaseStatement.class);
        OceanBaseStatement.mapper.put("\u0000", "\\0");
        OceanBaseStatement.mapper.put("'", "\\\\'");
        OceanBaseStatement.mapper.put("\"", "\\\\\"");
        OceanBaseStatement.mapper.put("\b", "\\\\b");
        OceanBaseStatement.mapper.put("\n", "\\\\n");
        OceanBaseStatement.mapper.put("\r", "\\\\r");
        OceanBaseStatement.mapper.put("\t", "\\\\t");
        OceanBaseStatement.mapper.put("\u001a", "\\\\Z");
        OceanBaseStatement.mapper.put("\\", "\\\\");
    }
}
