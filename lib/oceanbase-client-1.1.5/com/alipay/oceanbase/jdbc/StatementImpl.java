// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.DriverManager;
import java.util.Enumeration;
import java.util.Properties;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEvent;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.Calendar;
import java.sql.ResultSet;
import java.sql.BatchUpdateException;
import com.alipay.oceanbase.jdbc.exceptions.MySQLStatementCancelledException;
import com.alipay.oceanbase.jdbc.exceptions.MySQLTimeoutException;
import java.util.TimerTask;
import java.util.Iterator;
import java.util.Collections;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.log.LogUtils;
import java.util.HashSet;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.sql.SQLWarning;
import java.util.Set;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEventHandler;
import java.util.List;

public class StatementImpl implements Statement
{
    protected static final String PING_MARKER = "/* ping */";
    protected static final String[] ON_DUPLICATE_KEY_UPDATE_CLAUSE;
    protected Object cancelTimeoutMutex;
    static int statementCounter;
    public static final byte USES_VARIABLES_FALSE = 0;
    public static final byte USES_VARIABLES_TRUE = 1;
    public static final byte USES_VARIABLES_UNKNOWN = -1;
    protected boolean wasCancelled;
    protected boolean wasCancelledByTimeout;
    protected List<Object> batchedArgs;
    protected SingleByteCharsetConverter charConverter;
    protected String charEncoding;
    protected volatile MySQLConnection connection;
    protected long connectionId;
    protected String currentCatalog;
    protected boolean doEscapeProcessing;
    protected ProfilerEventHandler eventSink;
    private int fetchSize;
    protected boolean isClosed;
    protected long lastInsertId;
    protected int maxFieldSize;
    protected int maxRows;
    protected Set<ResultSetInternalMethods> openResults;
    protected boolean pedantic;
    protected String pointOfOrigin;
    protected boolean profileSQL;
    protected ResultSetInternalMethods results;
    protected ResultSetInternalMethods generatedKeysResults;
    protected int resultSetConcurrency;
    protected int resultSetType;
    protected int statementId;
    protected int timeoutInMillis;
    protected long updateCount;
    protected boolean useUsageAdvisor;
    protected SQLWarning warningChain;
    protected boolean clearWarningsCalled;
    protected boolean holdResultsOpenOverClose;
    protected ArrayList<ResultSetRow> batchedGeneratedKeys;
    protected boolean retrieveGeneratedKeys;
    protected boolean continueBatchOnError;
    protected PingTarget pingTarget;
    protected boolean useLegacyDatetimeCode;
    protected boolean sendFractionalSeconds;
    private ExceptionInterceptor exceptionInterceptor;
    protected boolean lastQueryIsOnDupKeyUpdate;
    protected final AtomicBoolean statementExecuting;
    private boolean isImplicitlyClosingResults;
    private int originalResultSetType;
    private int originalFetchSize;
    private boolean isPoolable;
    private InputStream localInfileInputStream;
    protected final boolean version5013OrNewer;
    private boolean closeOnCompletion;
    
    public StatementImpl(final MySQLConnection c, final String catalog) throws SQLException {
        this.cancelTimeoutMutex = new Object();
        this.wasCancelled = false;
        this.wasCancelledByTimeout = false;
        this.charConverter = null;
        this.charEncoding = null;
        this.connection = null;
        this.connectionId = 0L;
        this.currentCatalog = null;
        this.doEscapeProcessing = true;
        this.eventSink = null;
        this.fetchSize = 0;
        this.isClosed = false;
        this.lastInsertId = -1L;
        this.maxFieldSize = MysqlIO.getMaxBuf();
        this.maxRows = -1;
        this.openResults = new HashSet<ResultSetInternalMethods>();
        this.pedantic = false;
        this.profileSQL = false;
        this.results = null;
        this.generatedKeysResults = null;
        this.resultSetConcurrency = 0;
        this.resultSetType = 0;
        this.timeoutInMillis = 0;
        this.updateCount = -1L;
        this.useUsageAdvisor = false;
        this.warningChain = null;
        this.clearWarningsCalled = false;
        this.holdResultsOpenOverClose = false;
        this.batchedGeneratedKeys = null;
        this.retrieveGeneratedKeys = false;
        this.continueBatchOnError = false;
        this.pingTarget = null;
        this.lastQueryIsOnDupKeyUpdate = false;
        this.statementExecuting = new AtomicBoolean(false);
        this.isImplicitlyClosingResults = false;
        this.originalResultSetType = 0;
        this.originalFetchSize = 0;
        this.isPoolable = true;
        this.closeOnCompletion = false;
        if (c == null || c.isClosed()) {
            throw SQLError.createSQLException(Messages.getString("Statement.0"), "08003", null);
        }
        this.connection = c;
        this.connectionId = this.connection.getId();
        this.exceptionInterceptor = this.connection.getExceptionInterceptor();
        this.currentCatalog = catalog;
        this.pedantic = this.connection.getPedantic();
        this.continueBatchOnError = this.connection.getContinueBatchOnError();
        this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
        this.sendFractionalSeconds = this.connection.getSendFractionalSeconds();
        this.doEscapeProcessing = this.connection.getEnableEscapeProcessing();
        if (!this.connection.getDontTrackOpenResources()) {
            this.connection.registerStatement(this);
        }
        this.maxFieldSize = this.connection.getMaxAllowedPacket();
        final int defaultFetchSize = this.connection.getDefaultFetchSize();
        if (defaultFetchSize != 0) {
            this.setFetchSize(defaultFetchSize);
        }
        if (this.connection.getUseUnicode()) {
            this.charEncoding = this.connection.getEncoding();
            this.charConverter = this.connection.getCharsetConverter(this.charEncoding);
        }
        final boolean profiling = this.connection.getProfileSql() || this.connection.getUseUsageAdvisor() || this.connection.getLogSlowQueries();
        if (this.connection.getAutoGenerateTestcaseScript() || profiling) {
            this.statementId = StatementImpl.statementCounter++;
        }
        if (profiling) {
            this.pointOfOrigin = LogUtils.findCallingClassAndMethod(new Throwable());
            this.profileSQL = this.connection.getProfileSql();
            this.useUsageAdvisor = this.connection.getUseUsageAdvisor();
            this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
        }
        final int maxRowsConn = this.connection.getMaxRows();
        if (maxRowsConn != -1) {
            this.setMaxRows(maxRowsConn);
        }
        this.holdResultsOpenOverClose = this.connection.getHoldResultsOpenOverStatementClose();
        this.version5013OrNewer = this.connection.versionMeetsMinimum(5, 0, 13);
    }
    
    @Override
    public void addBatch(final String sql) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.batchedArgs == null) {
                this.batchedArgs = new ArrayList<Object>();
            }
            if (sql != null) {
                this.batchedArgs.add(sql);
            }
        }
    }
    
    public List<Object> getBatchedArgs() {
        return (this.batchedArgs == null) ? null : Collections.unmodifiableList((List<?>)this.batchedArgs);
    }
    
    @Override
    public void cancel() throws SQLException {
        if (!this.statementExecuting.get()) {
            return;
        }
        if (!this.isClosed && this.connection != null && this.connection.versionMeetsMinimum(5, 0, 0)) {
            Connection cancelConn = null;
            java.sql.Statement cancelStmt = null;
            try {
                cancelConn = this.connection.duplicate();
                cancelStmt = cancelConn.createStatement();
                cancelStmt.execute("KILL QUERY " + this.connection.getIO().getThreadId());
                this.wasCancelled = true;
            }
            finally {
                if (cancelStmt != null) {
                    cancelStmt.close();
                }
                if (cancelConn != null) {
                    cancelConn.close();
                }
            }
        }
    }
    
    protected MySQLConnection checkClosed() throws SQLException {
        final MySQLConnection c = this.connection;
        if (c == null) {
            throw SQLError.createSQLException(Messages.getString("Statement.49"), "S1009", this.getExceptionInterceptor());
        }
        return c;
    }
    
    protected void checkForDml(final String sql, final char firstStatementChar) throws SQLException {
        if (firstStatementChar == 'I' || firstStatementChar == 'U' || firstStatementChar == 'D' || firstStatementChar == 'A' || firstStatementChar == 'C' || firstStatementChar == 'T' || firstStatementChar == 'R') {
            final String noCommentSql = StringUtils.stripComments(sql, "'\"", "'\"", true, false, true, true);
            if (StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "INSERT") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "UPDATE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "DELETE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "DROP") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "CREATE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "ALTER") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "TRUNCATE") || StringUtils.startsWithIgnoreCaseAndWs(noCommentSql, "RENAME")) {
                throw SQLError.createSQLException(Messages.getString("Statement.57"), "S1009", this.getExceptionInterceptor());
            }
        }
    }
    
    protected void checkNullOrEmptyQuery(final String sql) throws SQLException {
        if (sql == null) {
            throw SQLError.createSQLException(Messages.getString("Statement.59"), "S1009", this.getExceptionInterceptor());
        }
        if (sql.length() == 0) {
            throw SQLError.createSQLException(Messages.getString("Statement.61"), "S1009", this.getExceptionInterceptor());
        }
    }
    
    @Override
    public void clearBatch() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.batchedArgs != null) {
                this.batchedArgs.clear();
            }
        }
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.clearWarningsCalled = true;
            this.warningChain = null;
        }
    }
    
    @Override
    public void close() throws SQLException {
        this.realClose(true, true);
    }
    
    protected void closeAllOpenResults() throws SQLException {
        final MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null) {
            return;
        }
        synchronized (locallyScopedConn.getConnectionMutex()) {
            if (this.openResults != null) {
                for (final ResultSetInternalMethods element : this.openResults) {
                    try {
                        element.realClose(false);
                    }
                    catch (SQLException sqlEx) {
                        AssertionFailedException.shouldNotHappen(sqlEx);
                    }
                }
                this.openResults.clear();
            }
        }
    }
    
    protected void implicitlyCloseAllOpenResults() throws SQLException {
        this.isImplicitlyClosingResults = true;
        try {
            if (!this.connection.getHoldResultsOpenOverStatementClose() && !this.connection.getDontTrackOpenResources() && !this.holdResultsOpenOverClose) {
                if (this.results != null) {
                    this.results.realClose(false);
                }
                if (this.generatedKeysResults != null) {
                    this.generatedKeysResults.realClose(false);
                }
                this.closeAllOpenResults();
            }
        }
        finally {
            this.isImplicitlyClosingResults = false;
        }
    }
    
    @Override
    public void removeOpenResultSet(final ResultSetInternalMethods rs) {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                if (this.openResults != null) {
                    this.openResults.remove(rs);
                }
                final boolean hasMoreResults = rs.getNextResultSet() != null;
                if (this.results == rs && !hasMoreResults) {
                    this.results = null;
                }
                if (this.generatedKeysResults == rs) {
                    this.generatedKeysResults = null;
                }
                if (!this.isImplicitlyClosingResults && !hasMoreResults) {
                    this.checkAndPerformCloseOnCompletionAction();
                }
            }
        }
        catch (SQLException ex) {}
    }
    
    @Override
    public int getOpenResultSetCount() {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                if (this.openResults != null) {
                    return this.openResults.size();
                }
                return 0;
            }
        }
        catch (SQLException e) {
            return 0;
        }
    }
    
    private void checkAndPerformCloseOnCompletionAction() {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                if (this.isCloseOnCompletion() && !this.connection.getDontTrackOpenResources() && this.getOpenResultSetCount() == 0 && (this.results == null || !this.results.reallyResult() || this.results.isClosed()) && (this.generatedKeysResults == null || !this.generatedKeysResults.reallyResult() || this.generatedKeysResults.isClosed())) {
                    this.realClose(false, false);
                }
            }
        }
        catch (SQLException ex) {}
    }
    
    private ResultSetInternalMethods createResultSetUsingServerFetch(final String sql) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final java.sql.PreparedStatement pStmt = this.connection.prepareStatement(sql, this.resultSetType, this.resultSetConcurrency);
            pStmt.setFetchSize(this.fetchSize);
            if (this.maxRows > -1) {
                pStmt.setMaxRows(this.maxRows);
            }
            this.statementBegins();
            pStmt.execute();
            final ResultSetInternalMethods rs = ((StatementImpl)pStmt).getResultSetInternal();
            rs.setStatementUsedForFetchingRows((PreparedStatement)pStmt);
            return this.results = rs;
        }
    }
    
    protected boolean createStreamingResultSet() {
        return this.resultSetType == 1003 && this.resultSetConcurrency == 1007 && this.fetchSize == Integer.MIN_VALUE;
    }
    
    @Override
    public void enableStreamingResults() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.originalResultSetType = this.resultSetType;
            this.originalFetchSize = this.fetchSize;
            this.setFetchSize(Integer.MIN_VALUE);
            this.setResultSetType(1003);
        }
    }
    
    @Override
    public void disableStreamingResults() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.fetchSize == Integer.MIN_VALUE && this.resultSetType == 1003) {
                this.setFetchSize(this.originalFetchSize);
                this.setResultSetType(this.originalResultSetType);
            }
        }
    }
    
    protected void setupStreamingTimeout(final MySQLConnection con) throws SQLException {
        if (this.createStreamingResultSet() && con.getNetTimeoutForStreamingResults() > 0) {
            this.executeSimpleNonQuery(con, "SET net_write_timeout=" + con.getNetTimeoutForStreamingResults());
        }
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        return this.executeInternal(sql, false);
    }
    
    private boolean executeInternal(String sql, final boolean returnGeneratedKeys) throws SQLException {
        final MySQLConnection locallyScopedConn = this.checkClosed();
        synchronized (locallyScopedConn.getConnectionMutex()) {
            this.checkClosed();
            this.checkNullOrEmptyQuery(sql);
            this.resetCancelledState();
            final char firstNonWsChar = StringUtils.firstAlphaCharUc(sql, findStartOfStatement(sql));
            final boolean maybeSelect = firstNonWsChar == 'S';
            this.retrieveGeneratedKeys = returnGeneratedKeys;
            this.lastQueryIsOnDupKeyUpdate = (returnGeneratedKeys && firstNonWsChar == 'I' && this.containsOnDuplicateKeyInString(sql));
            if (!maybeSelect && locallyScopedConn.isReadOnly()) {
                throw SQLError.createSQLException(Messages.getString("Statement.27") + Messages.getString("Statement.28"), "S1009", this.getExceptionInterceptor());
            }
            final boolean readInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
            if (returnGeneratedKeys && firstNonWsChar == 'R') {
                locallyScopedConn.setReadInfoMsgEnabled(true);
            }
            try {
                this.setupStreamingTimeout(locallyScopedConn);
                if (this.doEscapeProcessing) {
                    final Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, locallyScopedConn.serverSupportsConvertFn(), locallyScopedConn);
                    if (escapedSqlResult instanceof String) {
                        sql = (String)escapedSqlResult;
                    }
                    else {
                        sql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
                    }
                }
                this.implicitlyCloseAllOpenResults();
                if (sql.charAt(0) == '/' && sql.startsWith("/* ping */")) {
                    this.doPingInstead();
                    return true;
                }
                CachedResultSetMetaData cachedMetaData = null;
                ResultSetInternalMethods rs = null;
                this.batchedGeneratedKeys = null;
                if (this.useServerFetch()) {
                    rs = this.createResultSetUsingServerFetch(sql);
                }
                else {
                    CancelTask timeoutTask = null;
                    String oldCatalog = null;
                    try {
                        if (locallyScopedConn.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                            timeoutTask = new CancelTask(this);
                            locallyScopedConn.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
                        }
                        if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                            oldCatalog = locallyScopedConn.getCatalog();
                            locallyScopedConn.setCatalog(this.currentCatalog);
                        }
                        Field[] cachedFields = null;
                        if (locallyScopedConn.getCacheResultSetMetadata()) {
                            cachedMetaData = locallyScopedConn.getCachedMetaData(sql);
                            if (cachedMetaData != null) {
                                cachedFields = cachedMetaData.fields;
                            }
                        }
                        locallyScopedConn.setSessionMaxRows(maybeSelect ? this.maxRows : -1);
                        this.statementBegins();
                        rs = locallyScopedConn.execSQL(this, sql, this.maxRows, null, this.resultSetType, this.resultSetConcurrency, this.createStreamingResultSet(), this.currentCatalog, cachedFields);
                        if (timeoutTask != null) {
                            if (timeoutTask.caughtWhileCancelling != null) {
                                throw timeoutTask.caughtWhileCancelling;
                            }
                            timeoutTask.cancel();
                            timeoutTask = null;
                        }
                        synchronized (this.cancelTimeoutMutex) {
                            if (this.wasCancelled) {
                                SQLException cause = null;
                                if (this.wasCancelledByTimeout) {
                                    cause = new MySQLTimeoutException();
                                }
                                else {
                                    cause = new MySQLStatementCancelledException();
                                }
                                this.resetCancelledState();
                                throw cause;
                            }
                        }
                    }
                    finally {
                        if (timeoutTask != null) {
                            timeoutTask.cancel();
                            locallyScopedConn.getCancelTimer().purge();
                        }
                        if (oldCatalog != null) {
                            locallyScopedConn.setCatalog(oldCatalog);
                        }
                    }
                }
                if (rs != null) {
                    this.lastInsertId = rs.getUpdateID();
                    (this.results = rs).setFirstCharOfQuery(firstNonWsChar);
                    if (rs.reallyResult()) {
                        if (cachedMetaData != null) {
                            locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
                        }
                        else if (this.connection.getCacheResultSetMetadata()) {
                            locallyScopedConn.initializeResultsMetadataFromCache(sql, null, this.results);
                        }
                    }
                }
                return rs != null && rs.reallyResult();
            }
            finally {
                locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
                this.statementExecuting.set(false);
            }
        }
    }
    
    protected void statementBegins() {
        this.clearWarningsCalled = false;
        this.statementExecuting.set(true);
    }
    
    protected void resetCancelledState() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.cancelTimeoutMutex == null) {
                return;
            }
            synchronized (this.cancelTimeoutMutex) {
                this.wasCancelled = false;
                this.wasCancelledByTimeout = false;
            }
        }
    }
    
    @Override
    public boolean execute(final String sql, final int returnGeneratedKeys) throws SQLException {
        return this.executeInternal(sql, returnGeneratedKeys == 1);
    }
    
    @Override
    public boolean execute(final String sql, final int[] generatedKeyIndices) throws SQLException {
        return this.executeInternal(sql, generatedKeyIndices != null && generatedKeyIndices.length > 0);
    }
    
    @Override
    public boolean execute(final String sql, final String[] generatedKeyNames) throws SQLException {
        return this.executeInternal(sql, generatedKeyNames != null && generatedKeyNames.length > 0);
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        return Util.truncateAndConvertToInt(this.executeBatchInternal());
    }
    
    protected long[] executeBatchInternal() throws SQLException {
        final MySQLConnection locallyScopedConn = this.checkClosed();
        synchronized (locallyScopedConn.getConnectionMutex()) {
            if (locallyScopedConn.isReadOnly()) {
                throw SQLError.createSQLException(Messages.getString("Statement.34") + Messages.getString("Statement.35"), "S1009", this.getExceptionInterceptor());
            }
            this.implicitlyCloseAllOpenResults();
            if (this.batchedArgs == null || this.batchedArgs.size() == 0) {
                return new long[0];
            }
            final int individualStatementTimeout = this.timeoutInMillis;
            this.timeoutInMillis = 0;
            CancelTask timeoutTask = null;
            try {
                this.resetCancelledState();
                this.statementBegins();
                try {
                    this.retrieveGeneratedKeys = true;
                    long[] updateCounts = null;
                    if (this.batchedArgs != null) {
                        final int nbrCommands = this.batchedArgs.size();
                        this.batchedGeneratedKeys = new ArrayList<ResultSetRow>(this.batchedArgs.size());
                        final boolean multiQueriesEnabled = locallyScopedConn.getAllowMultiQueries();
                        if (locallyScopedConn.versionMeetsMinimum(4, 1, 1) && (multiQueriesEnabled || (locallyScopedConn.getRewriteBatchedStatements() && nbrCommands > 4))) {
                            return this.executeBatchUsingMultiQueries(multiQueriesEnabled, nbrCommands, individualStatementTimeout);
                        }
                        if (locallyScopedConn.getEnableQueryTimeouts() && individualStatementTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                            timeoutTask = new CancelTask(this);
                            locallyScopedConn.getCancelTimer().schedule(timeoutTask, individualStatementTimeout);
                        }
                        updateCounts = new long[nbrCommands];
                        for (int i = 0; i < nbrCommands; ++i) {
                            updateCounts[i] = -3L;
                        }
                        SQLException sqlEx = null;
                        int commandIndex;
                        String sql;
                        long[] newUpdateCounts;
                        int j;
                        for (commandIndex = 0, commandIndex = 0; commandIndex < nbrCommands; ++commandIndex) {
                            try {
                                sql = this.batchedArgs.get(commandIndex);
                                updateCounts[commandIndex] = this.executeUpdateInternal(sql, true, true);
                                this.getBatchedGeneratedKeys((this.results.getFirstCharOfQuery() == 'I' && this.containsOnDuplicateKeyInString(sql)) ? 1 : 0);
                            }
                            catch (SQLException ex) {
                                updateCounts[commandIndex] = -3L;
                                if (!this.continueBatchOnError || ex instanceof MySQLTimeoutException || ex instanceof MySQLStatementCancelledException || this.hasDeadlockOrTimeoutRolledBackTx(ex)) {
                                    newUpdateCounts = new long[commandIndex];
                                    if (this.hasDeadlockOrTimeoutRolledBackTx(ex)) {
                                        for (j = 0; j < newUpdateCounts.length; ++j) {
                                            newUpdateCounts[j] = -3L;
                                        }
                                    }
                                    else {
                                        System.arraycopy(updateCounts, 0, newUpdateCounts, 0, commandIndex);
                                    }
                                    throw SQLError.createBatchUpdateException(ex, newUpdateCounts, this.getExceptionInterceptor());
                                }
                                sqlEx = ex;
                            }
                        }
                        if (sqlEx != null) {
                            throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
                        }
                    }
                    if (timeoutTask != null) {
                        if (timeoutTask.caughtWhileCancelling != null) {
                            throw timeoutTask.caughtWhileCancelling;
                        }
                        timeoutTask.cancel();
                        locallyScopedConn.getCancelTimer().purge();
                        timeoutTask = null;
                    }
                    return (updateCounts != null) ? updateCounts : new long[0];
                }
                finally {
                    this.statementExecuting.set(false);
                }
            }
            finally {
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                }
                this.resetCancelledState();
                this.timeoutInMillis = individualStatementTimeout;
                this.clearBatch();
            }
        }
    }
    
    protected final boolean hasDeadlockOrTimeoutRolledBackTx(final SQLException ex) {
        final int vendorCode = ex.getErrorCode();
        switch (vendorCode) {
            case 1206:
            case 1213: {
                return true;
            }
            case 1205: {
                return !this.version5013OrNewer;
            }
            default: {
                return false;
            }
        }
    }
    
    private long[] executeBatchUsingMultiQueries(final boolean multiQueriesEnabled, final int nbrCommands, final int individualStatementTimeout) throws SQLException {
        final MySQLConnection locallyScopedConn = this.checkClosed();
        synchronized (locallyScopedConn.getConnectionMutex()) {
            if (!multiQueriesEnabled) {
                locallyScopedConn.getIO().enableMultiQueries();
            }
            java.sql.Statement batchStmt = null;
            CancelTask timeoutTask = null;
            try {
                final long[] updateCounts = new long[nbrCommands];
                for (int i = 0; i < nbrCommands; ++i) {
                    updateCounts[i] = -3L;
                }
                int commandIndex = 0;
                StringBuilder queryBuf = new StringBuilder();
                batchStmt = locallyScopedConn.createStatement();
                if (locallyScopedConn.getEnableQueryTimeouts() && individualStatementTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                    timeoutTask = new CancelTask((StatementImpl)batchStmt);
                    locallyScopedConn.getCancelTimer().schedule(timeoutTask, individualStatementTimeout);
                }
                int counter = 0;
                int numberOfBytesPerChar = 1;
                final String connectionEncoding = locallyScopedConn.getEncoding();
                if (StringUtils.startsWithIgnoreCase(connectionEncoding, "utf")) {
                    numberOfBytesPerChar = 3;
                }
                else if (CharsetMapping.isMultibyteCharset(connectionEncoding)) {
                    numberOfBytesPerChar = 2;
                }
                int escapeAdjust = 1;
                batchStmt.setEscapeProcessing(this.doEscapeProcessing);
                if (this.doEscapeProcessing) {
                    escapeAdjust = 2;
                }
                SQLException sqlEx = null;
                int argumentSetsInBatchSoFar = 0;
                for (commandIndex = 0; commandIndex < nbrCommands; ++commandIndex) {
                    final String nextQuery = this.batchedArgs.get(commandIndex);
                    if (((queryBuf.length() + nextQuery.length()) * numberOfBytesPerChar + 1 + 4) * escapeAdjust + 32 > this.connection.getMaxAllowedPacket()) {
                        try {
                            batchStmt.execute(queryBuf.toString(), 1);
                        }
                        catch (SQLException ex) {
                            sqlEx = this.handleExceptionForBatch(commandIndex, argumentSetsInBatchSoFar, updateCounts, ex);
                        }
                        counter = this.processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
                        queryBuf = new StringBuilder();
                        argumentSetsInBatchSoFar = 0;
                    }
                    queryBuf.append(nextQuery);
                    queryBuf.append(";");
                    ++argumentSetsInBatchSoFar;
                }
                if (queryBuf.length() > 0) {
                    try {
                        batchStmt.execute(queryBuf.toString(), 1);
                    }
                    catch (SQLException ex2) {
                        sqlEx = this.handleExceptionForBatch(commandIndex - 1, argumentSetsInBatchSoFar, updateCounts, ex2);
                    }
                    counter = this.processMultiCountsAndKeys((StatementImpl)batchStmt, counter, updateCounts);
                }
                if (timeoutTask != null) {
                    if (timeoutTask.caughtWhileCancelling != null) {
                        throw timeoutTask.caughtWhileCancelling;
                    }
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                    timeoutTask = null;
                }
                if (sqlEx != null) {
                    throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
                }
                return (updateCounts != null) ? updateCounts : new long[0];
            }
            finally {
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                }
                this.resetCancelledState();
                try {
                    if (batchStmt != null) {
                        batchStmt.close();
                    }
                }
                finally {
                    if (!multiQueriesEnabled) {
                        locallyScopedConn.getIO().disableMultiQueries();
                    }
                }
            }
        }
    }
    
    protected int processMultiCountsAndKeys(final StatementImpl batchedStatement, int updateCountCounter, final long[] updateCounts) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            updateCounts[updateCountCounter++] = batchedStatement.getLargeUpdateCount();
            final boolean doGenKeys = this.batchedGeneratedKeys != null;
            byte[][] row = null;
            if (doGenKeys) {
                final long generatedKey = batchedStatement.getLastInsertID();
                row = new byte[][] { StringUtils.getBytes(Long.toString(generatedKey)) };
                this.batchedGeneratedKeys.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
            }
            while (batchedStatement.getMoreResults() || batchedStatement.getLargeUpdateCount() != -1L) {
                updateCounts[updateCountCounter++] = batchedStatement.getLargeUpdateCount();
                if (doGenKeys) {
                    final long generatedKey = batchedStatement.getLastInsertID();
                    row = new byte[][] { StringUtils.getBytes(Long.toString(generatedKey)) };
                    this.batchedGeneratedKeys.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
                }
            }
            return updateCountCounter;
        }
    }
    
    protected SQLException handleExceptionForBatch(final int endOfBatchIndex, final int numValuesPerBatch, final long[] updateCounts, final SQLException ex) throws BatchUpdateException, SQLException {
        for (int j = endOfBatchIndex; j > endOfBatchIndex - numValuesPerBatch; --j) {
            updateCounts[j] = -3L;
        }
        if (this.continueBatchOnError && !(ex instanceof MySQLTimeoutException) && !(ex instanceof MySQLStatementCancelledException) && !this.hasDeadlockOrTimeoutRolledBackTx(ex)) {
            return ex;
        }
        final long[] newUpdateCounts = new long[endOfBatchIndex];
        System.arraycopy(updateCounts, 0, newUpdateCounts, 0, endOfBatchIndex);
        throw SQLError.createBatchUpdateException(ex, newUpdateCounts, this.getExceptionInterceptor());
    }
    
    @Override
    public ResultSet executeQuery(String sql) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final MySQLConnection locallyScopedConn = this.connection;
            this.retrieveGeneratedKeys = false;
            this.resetCancelledState();
            this.checkNullOrEmptyQuery(sql);
            this.setupStreamingTimeout(locallyScopedConn);
            if (this.doEscapeProcessing) {
                final Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, locallyScopedConn.serverSupportsConvertFn(), this.connection);
                if (escapedSqlResult instanceof String) {
                    sql = (String)escapedSqlResult;
                }
                else {
                    sql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
                }
            }
            final char firstStatementChar = StringUtils.firstAlphaCharUc(sql, findStartOfStatement(sql));
            if (sql.charAt(0) == '/' && sql.startsWith("/* ping */")) {
                this.doPingInstead();
                return this.results;
            }
            this.checkForDml(sql, firstStatementChar);
            this.implicitlyCloseAllOpenResults();
            CachedResultSetMetaData cachedMetaData = null;
            if (this.useServerFetch()) {
                return this.results = this.createResultSetUsingServerFetch(sql);
            }
            CancelTask timeoutTask = null;
            String oldCatalog = null;
            try {
                if (locallyScopedConn.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                    timeoutTask = new CancelTask(this);
                    locallyScopedConn.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
                }
                if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                    oldCatalog = locallyScopedConn.getCatalog();
                    locallyScopedConn.setCatalog(this.currentCatalog);
                }
                Field[] cachedFields = null;
                if (locallyScopedConn.getCacheResultSetMetadata()) {
                    cachedMetaData = locallyScopedConn.getCachedMetaData(sql);
                    if (cachedMetaData != null) {
                        cachedFields = cachedMetaData.fields;
                    }
                }
                locallyScopedConn.setSessionMaxRows(this.maxRows);
                this.statementBegins();
                this.results = locallyScopedConn.execSQL(this, sql, this.maxRows, null, this.resultSetType, this.resultSetConcurrency, this.createStreamingResultSet(), this.currentCatalog, cachedFields);
                if (timeoutTask != null) {
                    if (timeoutTask.caughtWhileCancelling != null) {
                        throw timeoutTask.caughtWhileCancelling;
                    }
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                    timeoutTask = null;
                }
                synchronized (this.cancelTimeoutMutex) {
                    if (this.wasCancelled) {
                        SQLException cause = null;
                        if (this.wasCancelledByTimeout) {
                            cause = new MySQLTimeoutException();
                        }
                        else {
                            cause = new MySQLStatementCancelledException();
                        }
                        this.resetCancelledState();
                        throw cause;
                    }
                }
            }
            finally {
                this.statementExecuting.set(false);
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                }
                if (oldCatalog != null) {
                    locallyScopedConn.setCatalog(oldCatalog);
                }
            }
            this.lastInsertId = this.results.getUpdateID();
            if (cachedMetaData != null) {
                locallyScopedConn.initializeResultsMetadataFromCache(sql, cachedMetaData, this.results);
            }
            else if (this.connection.getCacheResultSetMetadata()) {
                locallyScopedConn.initializeResultsMetadataFromCache(sql, null, this.results);
            }
            return this.results;
        }
    }
    
    protected void doPingInstead() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.pingTarget != null) {
                this.pingTarget.doPing();
            }
            else {
                this.connection.ping();
            }
            final ResultSetInternalMethods fakeSelectOneResultSet = this.generatePingResultSet();
            this.results = fakeSelectOneResultSet;
        }
    }
    
    protected ResultSetInternalMethods generatePingResultSet() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final Field[] fields = { new Field(null, "1", -5, 1) };
            final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
            final byte[] colVal = { 49 };
            rows.add(new ByteArrayRow(new byte[][] { colVal }, this.getExceptionInterceptor()));
            return (ResultSetInternalMethods)DatabaseMetaData.buildResultSet(fields, rows, this.connection);
        }
    }
    
    protected void executeSimpleNonQuery(final MySQLConnection c, final String nonQuery) throws SQLException {
        c.execSQL(this, nonQuery, -1, null, 1003, 1007, false, this.currentCatalog, null, false).close();
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql));
    }
    
    protected long executeUpdateInternal(String sql, final boolean isBatch, final boolean returnGeneratedKeys) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final MySQLConnection locallyScopedConn = this.connection;
            this.checkNullOrEmptyQuery(sql);
            this.resetCancelledState();
            final char firstStatementChar = StringUtils.firstAlphaCharUc(sql, findStartOfStatement(sql));
            this.retrieveGeneratedKeys = returnGeneratedKeys;
            this.lastQueryIsOnDupKeyUpdate = (returnGeneratedKeys && firstStatementChar == 'I' && this.containsOnDuplicateKeyInString(sql));
            ResultSetInternalMethods rs = null;
            if (this.doEscapeProcessing) {
                final Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, this.connection.serverSupportsConvertFn(), this.connection);
                if (escapedSqlResult instanceof String) {
                    sql = (String)escapedSqlResult;
                }
                else {
                    sql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
                }
            }
            if (locallyScopedConn.isReadOnly(false)) {
                throw SQLError.createSQLException(Messages.getString("Statement.42") + Messages.getString("Statement.43"), "S1009", this.getExceptionInterceptor());
            }
            if (StringUtils.startsWithIgnoreCaseAndWs(sql, "select")) {
                throw SQLError.createSQLException(Messages.getString("Statement.46"), "01S03", this.getExceptionInterceptor());
            }
            this.implicitlyCloseAllOpenResults();
            CancelTask timeoutTask = null;
            String oldCatalog = null;
            final boolean readInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
            if (returnGeneratedKeys && firstStatementChar == 'R') {
                locallyScopedConn.setReadInfoMsgEnabled(true);
            }
            try {
                if (locallyScopedConn.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                    timeoutTask = new CancelTask(this);
                    locallyScopedConn.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
                }
                if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                    oldCatalog = locallyScopedConn.getCatalog();
                    locallyScopedConn.setCatalog(this.currentCatalog);
                }
                locallyScopedConn.setSessionMaxRows(-1);
                this.statementBegins();
                rs = locallyScopedConn.execSQL(this, sql, -1, null, 1003, 1007, false, this.currentCatalog, null, isBatch);
                if (timeoutTask != null) {
                    if (timeoutTask.caughtWhileCancelling != null) {
                        throw timeoutTask.caughtWhileCancelling;
                    }
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                    timeoutTask = null;
                }
                synchronized (this.cancelTimeoutMutex) {
                    if (this.wasCancelled) {
                        SQLException cause = null;
                        if (this.wasCancelledByTimeout) {
                            cause = new MySQLTimeoutException();
                        }
                        else {
                            cause = new MySQLStatementCancelledException();
                        }
                        this.resetCancelledState();
                        throw cause;
                    }
                }
            }
            finally {
                locallyScopedConn.setReadInfoMsgEnabled(readInfoMsgState);
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                }
                if (oldCatalog != null) {
                    locallyScopedConn.setCatalog(oldCatalog);
                }
                if (!isBatch) {
                    this.statementExecuting.set(false);
                }
            }
            (this.results = rs).setFirstCharOfQuery(firstStatementChar);
            this.updateCount = rs.getUpdateCount();
            this.lastInsertId = rs.getUpdateID();
            return this.updateCount;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, autoGeneratedKeys));
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, columnIndexes));
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        return Util.truncateAndConvertToInt(this.executeLargeUpdate(sql, columnNames));
    }
    
    protected Calendar getCalendarInstanceForSessionOrNew() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.connection != null) {
                return this.connection.getCalendarInstanceForSessionOrNew();
            }
            return new GregorianCalendar();
        }
    }
    
    @Override
    public java.sql.Connection getConnection() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.connection;
        }
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        return 1000;
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.fetchSize;
        }
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (!this.retrieveGeneratedKeys) {
                throw SQLError.createSQLException(Messages.getString("Statement.GeneratedKeysNotRequested"), "S1009", this.getExceptionInterceptor());
            }
            if (this.batchedGeneratedKeys != null) {
                final Field[] fields = { new Field("", "GENERATED_KEY", -5, 20) };
                fields[0].setConnection(this.connection);
                return this.generatedKeysResults = ResultSetImpl.getInstance(this.currentCatalog, fields, new RowDataStatic(this.batchedGeneratedKeys), this.connection, this, false);
            }
            if (this.lastQueryIsOnDupKeyUpdate) {
                return this.generatedKeysResults = this.getGeneratedKeysInternal(1L);
            }
            return this.generatedKeysResults = this.getGeneratedKeysInternal();
        }
    }
    
    protected ResultSetInternalMethods getGeneratedKeysInternal() throws SQLException {
        final long numKeys = this.getLargeUpdateCount();
        return this.getGeneratedKeysInternal(numKeys);
    }
    
    protected ResultSetInternalMethods getGeneratedKeysInternal(long numKeys) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final Field[] fields = { new Field("", "GENERATED_KEY", -5, 20) };
            fields[0].setConnection(this.connection);
            fields[0].setUseOldNameMetadata(true);
            final ArrayList<ResultSetRow> rowSet = new ArrayList<ResultSetRow>();
            long beginAt = this.getLastInsertID();
            if (beginAt < 0L) {
                fields[0].setUnsigned();
            }
            if (this.results != null) {
                final String serverInfo = this.results.getServerInfo();
                if (numKeys > 0L && this.results.getFirstCharOfQuery() == 'R' && serverInfo != null && serverInfo.length() > 0) {
                    numKeys = this.getRecordCountFromInfo(serverInfo);
                }
                if (beginAt != 0L && numKeys > 0L) {
                    for (int i = 0; i < numKeys; ++i) {
                        final byte[][] row = { null };
                        if (beginAt > 0L) {
                            row[0] = StringUtils.getBytes(Long.toString(beginAt));
                        }
                        else {
                            final byte[] asBytes = { (byte)(beginAt >>> 56), (byte)(beginAt >>> 48), (byte)(beginAt >>> 40), (byte)(beginAt >>> 32), (byte)(beginAt >>> 24), (byte)(beginAt >>> 16), (byte)(beginAt >>> 8), (byte)(beginAt & 0xFFL) };
                            final BigInteger val = new BigInteger(1, asBytes);
                            row[0] = val.toString().getBytes();
                        }
                        rowSet.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
                        beginAt += this.connection.getAutoIncrementIncrement();
                    }
                }
            }
            final ResultSetImpl gkRs = ResultSetImpl.getInstance(this.currentCatalog, fields, new RowDataStatic(rowSet), this.connection, this, false);
            return gkRs;
        }
    }
    
    protected int getId() {
        return this.statementId;
    }
    
    public long getLastInsertID() {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                return this.lastInsertId;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    public long getLongUpdateCount() {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                if (this.results == null) {
                    return -1L;
                }
                if (this.results.reallyResult()) {
                    return -1L;
                }
                return this.updateCount;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public int getMaxFieldSize() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.maxFieldSize;
        }
    }
    
    @Override
    public int getMaxRows() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.maxRows <= 0) {
                return 0;
            }
            return this.maxRows;
        }
    }
    
    @Override
    public boolean getMoreResults() throws SQLException {
        return this.getMoreResults(1);
    }
    
    @Override
    public boolean getMoreResults(final int current) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.results == null) {
                return false;
            }
            final boolean streamingMode = this.createStreamingResultSet();
            if (streamingMode && this.results.reallyResult()) {
                while (this.results.next()) {}
            }
            final ResultSetInternalMethods nextResultSet = this.results.getNextResultSet();
            switch (current) {
                case 1: {
                    if (this.results != null) {
                        if (!streamingMode && !this.connection.getDontTrackOpenResources()) {
                            this.results.realClose(false);
                        }
                        this.results.clearNextResult();
                        break;
                    }
                    break;
                }
                case 3: {
                    if (this.results != null) {
                        if (!streamingMode && !this.connection.getDontTrackOpenResources()) {
                            this.results.realClose(false);
                        }
                        this.results.clearNextResult();
                    }
                    this.closeAllOpenResults();
                    break;
                }
                case 2: {
                    if (!this.connection.getDontTrackOpenResources()) {
                        this.openResults.add(this.results);
                    }
                    this.results.clearNextResult();
                    break;
                }
                default: {
                    throw SQLError.createSQLException(Messages.getString("Statement.19"), "S1009", this.getExceptionInterceptor());
                }
            }
            this.results = nextResultSet;
            if (this.results == null) {
                this.updateCount = -1L;
                this.lastInsertId = -1L;
            }
            else if (this.results.reallyResult()) {
                this.updateCount = -1L;
                this.lastInsertId = -1L;
            }
            else {
                this.updateCount = this.results.getUpdateCount();
                this.lastInsertId = this.results.getUpdateID();
            }
            final boolean moreResults = this.results != null && this.results.reallyResult();
            if (!moreResults) {
                this.checkAndPerformCloseOnCompletionAction();
            }
            return moreResults;
        }
    }
    
    @Override
    public int getQueryTimeout() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.timeoutInMillis / 1000;
        }
    }
    
    private long getRecordCountFromInfo(final String serverInfo) {
        final StringBuilder recordsBuf = new StringBuilder();
        long recordsCount = 0L;
        long duplicatesCount = 0L;
        char c = '\0';
        int length;
        int i;
        for (length = serverInfo.length(), i = 0; i < length; ++i) {
            c = serverInfo.charAt(i);
            if (Character.isDigit(c)) {
                break;
            }
        }
        recordsBuf.append(c);
        ++i;
        while (i < length) {
            c = serverInfo.charAt(i);
            if (!Character.isDigit(c)) {
                break;
            }
            recordsBuf.append(c);
            ++i;
        }
        recordsCount = Long.parseLong(recordsBuf.toString());
        final StringBuilder duplicatesBuf = new StringBuilder();
        while (i < length) {
            c = serverInfo.charAt(i);
            if (Character.isDigit(c)) {
                break;
            }
            ++i;
        }
        duplicatesBuf.append(c);
        ++i;
        while (i < length) {
            c = serverInfo.charAt(i);
            if (!Character.isDigit(c)) {
                break;
            }
            duplicatesBuf.append(c);
            ++i;
        }
        duplicatesCount = Long.parseLong(duplicatesBuf.toString());
        return recordsCount - duplicatesCount;
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return (this.results != null && this.results.reallyResult()) ? this.results : null;
        }
    }
    
    @Override
    public int getResultSetConcurrency() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.resultSetConcurrency;
        }
    }
    
    @Override
    public int getResultSetHoldability() throws SQLException {
        return 1;
    }
    
    protected ResultSetInternalMethods getResultSetInternal() {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                return this.results;
            }
        }
        catch (SQLException e) {
            return this.results;
        }
    }
    
    @Override
    public int getResultSetType() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.resultSetType;
        }
    }
    
    @Override
    public int getUpdateCount() throws SQLException {
        return Util.truncateAndConvertToInt(this.getLargeUpdateCount());
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.clearWarningsCalled) {
                return null;
            }
            if (this.connection.getIO().isOracleMode()) {
                return null;
            }
            if (this.connection.versionMeetsMinimum(4, 1, 0)) {
                final SQLWarning pendingWarningsFromServer = SQLError.convertShowWarningsToSQLWarnings(this.connection);
                if (this.warningChain != null) {
                    this.warningChain.setNextWarning(pendingWarningsFromServer);
                }
                else {
                    this.warningChain = pendingWarningsFromServer;
                }
                return this.warningChain;
            }
            return this.warningChain;
        }
    }
    
    protected void realClose(final boolean calledExplicitly, boolean closeOpenResults) throws SQLException {
        final MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null || this.isClosed) {
            return;
        }
        if (!locallyScopedConn.getDontTrackOpenResources()) {
            locallyScopedConn.unregisterStatement(this);
        }
        if (this.useUsageAdvisor && !calledExplicitly) {
            final String message = Messages.getString("Statement.63") + Messages.getString("Statement.64");
            this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.currentCatalog, this.connectionId, this.getId(), -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
        }
        if (closeOpenResults) {
            closeOpenResults = (!this.holdResultsOpenOverClose && !this.connection.getDontTrackOpenResources());
        }
        if (closeOpenResults) {
            if (this.results != null) {
                try {
                    this.results.close();
                }
                catch (Exception ex) {}
            }
            if (this.generatedKeysResults != null) {
                try {
                    this.generatedKeysResults.close();
                }
                catch (Exception ex2) {}
            }
            this.closeAllOpenResults();
        }
        this.isClosed = true;
        this.results = null;
        this.generatedKeysResults = null;
        this.connection = null;
        this.warningChain = null;
        this.openResults = null;
        this.batchedGeneratedKeys = null;
        this.localInfileInputStream = null;
        this.pingTarget = null;
    }
    
    @Override
    public void setCursorName(final String name) throws SQLException {
    }
    
    @Override
    public void setEscapeProcessing(final boolean enable) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.doEscapeProcessing = enable;
        }
    }
    
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        switch (direction) {
            case 1000:
            case 1001:
            case 1002: {}
            default: {
                throw SQLError.createSQLException(Messages.getString("Statement.5"), "S1009", this.getExceptionInterceptor());
            }
        }
    }
    
    @Override
    public void setFetchSize(final int rows) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if ((rows < 0 && rows != Integer.MIN_VALUE) || (this.maxRows > 0 && rows > this.getMaxRows())) {
                throw SQLError.createSQLException(Messages.getString("Statement.7"), "S1009", this.getExceptionInterceptor());
            }
            this.fetchSize = rows;
        }
    }
    
    @Override
    public void setHoldResultsOpenOverClose(final boolean holdResultsOpenOverClose) {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                this.holdResultsOpenOverClose = holdResultsOpenOverClose;
            }
        }
        catch (SQLException ex) {}
    }
    
    @Override
    public void setMaxFieldSize(final int max) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (max < 0) {
                throw SQLError.createSQLException(Messages.getString("Statement.11"), "S1009", this.getExceptionInterceptor());
            }
            final int maxBuf = (this.connection != null) ? this.connection.getMaxAllowedPacket() : MysqlIO.getMaxBuf();
            if (max > maxBuf) {
                throw SQLError.createSQLException(Messages.getString("Statement.13", new Object[] { maxBuf }), "S1009", this.getExceptionInterceptor());
            }
            this.maxFieldSize = max;
        }
    }
    
    @Override
    public void setMaxRows(final int max) throws SQLException {
        this.setLargeMaxRows(max);
    }
    
    @Override
    public void setQueryTimeout(final int seconds) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (seconds < 0) {
                throw SQLError.createSQLException(Messages.getString("Statement.21"), "S1009", this.getExceptionInterceptor());
            }
            this.timeoutInMillis = seconds * 1000;
        }
    }
    
    void setResultSetConcurrency(final int concurrencyFlag) {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                this.resultSetConcurrency = concurrencyFlag;
            }
        }
        catch (SQLException ex) {}
    }
    
    void setResultSetType(final int typeFlag) {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                this.resultSetType = typeFlag;
            }
        }
        catch (SQLException ex) {}
    }
    
    protected void getBatchedGeneratedKeys(final java.sql.Statement batchedStatement) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.retrieveGeneratedKeys) {
                ResultSet rs = null;
                try {
                    rs = batchedStatement.getGeneratedKeys();
                    while (rs.next()) {
                        this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][] { rs.getBytes(1) }, this.getExceptionInterceptor()));
                    }
                }
                finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
            }
        }
    }
    
    protected void getBatchedGeneratedKeys(final int maxKeys) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.retrieveGeneratedKeys) {
                ResultSet rs = null;
                try {
                    if (maxKeys == 0) {
                        rs = this.getGeneratedKeysInternal();
                    }
                    else {
                        rs = this.getGeneratedKeysInternal(maxKeys);
                    }
                    while (rs.next()) {
                        this.batchedGeneratedKeys.add(new ByteArrayRow(new byte[][] { rs.getBytes(1) }, this.getExceptionInterceptor()));
                    }
                }
                finally {
                    this.isImplicitlyClosingResults = true;
                    try {
                        if (rs != null) {
                            rs.close();
                        }
                    }
                    finally {
                        this.isImplicitlyClosingResults = false;
                    }
                }
            }
        }
    }
    
    private boolean useServerFetch() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.connection.isCursorFetchEnabled() && this.fetchSize > 0 && this.resultSetConcurrency == 1007 && this.resultSetType == 1003;
        }
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        final MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null) {
            return true;
        }
        synchronized (locallyScopedConn.getConnectionMutex()) {
            return this.isClosed;
        }
    }
    
    @Override
    public boolean isPoolable() throws SQLException {
        return this.isPoolable;
    }
    
    @Override
    public void setPoolable(final boolean poolable) throws SQLException {
        this.isPoolable = poolable;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        this.checkClosed();
        return iface.isInstance(this);
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        }
        catch (ClassCastException cce) {
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.getExceptionInterceptor());
        }
    }
    
    protected static int findStartOfStatement(final String sql) {
        int statementStartPos = 0;
        if (StringUtils.startsWithIgnoreCaseAndWs(sql, "/*")) {
            statementStartPos = sql.indexOf("*/");
            if (statementStartPos == -1) {
                statementStartPos = 0;
            }
            else {
                statementStartPos += 2;
            }
        }
        else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "--") || StringUtils.startsWithIgnoreCaseAndWs(sql, "#")) {
            statementStartPos = sql.indexOf(10);
            if (statementStartPos == -1) {
                statementStartPos = sql.indexOf(13);
                if (statementStartPos == -1) {
                    statementStartPos = 0;
                }
            }
        }
        return statementStartPos;
    }
    
    @Override
    public InputStream getLocalInfileInputStream() {
        return this.localInfileInputStream;
    }
    
    @Override
    public void setLocalInfileInputStream(final InputStream stream) {
        this.localInfileInputStream = stream;
    }
    
    @Override
    public void setPingTarget(final PingTarget pingTarget) {
        this.pingTarget = pingTarget;
    }
    
    @Override
    public ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }
    
    protected boolean containsOnDuplicateKeyInString(final String sql) {
        return getOnDuplicateKeyLocation(sql, this.connection.getDontCheckOnDuplicateKeyUpdateInSQL(), this.connection.getRewriteBatchedStatements(), this.connection.isNoBackslashEscapesSet()) != -1;
    }
    
    protected static int getOnDuplicateKeyLocation(final String sql, final boolean dontCheckOnDuplicateKeyUpdateInSQL, final boolean rewriteBatchedStatements, final boolean noBackslashEscapes) {
        return (dontCheckOnDuplicateKeyUpdateInSQL && !rewriteBatchedStatements) ? -1 : StringUtils.indexOfIgnoreCase(0, sql, StatementImpl.ON_DUPLICATE_KEY_UPDATE_CLAUSE, "\"'`", "\"'`", noBackslashEscapes ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
    }
    
    @Override
    public void closeOnCompletion() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.closeOnCompletion = true;
        }
    }
    
    @Override
    public boolean isCloseOnCompletion() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.closeOnCompletion;
        }
    }
    
    @Override
    public long[] executeLargeBatch() throws SQLException {
        return this.executeBatchInternal();
    }
    
    @Override
    public long executeLargeUpdate(final String sql) throws SQLException {
        return this.executeUpdateInternal(sql, false, false);
    }
    
    @Override
    public long executeLargeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        return this.executeUpdateInternal(sql, false, autoGeneratedKeys == 1);
    }
    
    @Override
    public long executeLargeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        return this.executeUpdateInternal(sql, false, columnIndexes != null && columnIndexes.length > 0);
    }
    
    @Override
    public long executeLargeUpdate(final String sql, final String[] columnNames) throws SQLException {
        return this.executeUpdateInternal(sql, false, columnNames != null && columnNames.length > 0);
    }
    
    @Override
    public long getLargeMaxRows() throws SQLException {
        return this.getMaxRows();
    }
    
    @Override
    public long getLargeUpdateCount() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.results == null) {
                return -1L;
            }
            if (this.results.reallyResult()) {
                return -1L;
            }
            return this.results.getUpdateCount();
        }
    }
    
    @Override
    public void setLargeMaxRows(long max) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (max > 50000000L || max < 0L) {
                throw SQLError.createSQLException(Messages.getString("Statement.15") + max + " > " + 50000000 + ".", "S1009", this.getExceptionInterceptor());
            }
            if (max == 0L) {
                max = -1L;
            }
            this.maxRows = (int)max;
        }
    }
    
    boolean isCursorRequired() throws SQLException {
        return false;
    }
    
    static {
        ON_DUPLICATE_KEY_UPDATE_CLAUSE = new String[] { "ON", "DUPLICATE", "KEY", "UPDATE" };
        StatementImpl.statementCounter = 1;
    }
    
    class CancelTask extends TimerTask
    {
        long connectionId;
        SQLException caughtWhileCancelling;
        StatementImpl toCancel;
        Properties origConnProps;
        String origConnURL;
        
        CancelTask(final StatementImpl cancellee) throws SQLException {
            this.connectionId = 0L;
            this.caughtWhileCancelling = null;
            this.origConnProps = null;
            this.origConnURL = "";
            this.connectionId = cancellee.connectionId;
            this.toCancel = cancellee;
            this.origConnProps = new Properties();
            final Properties props = StatementImpl.this.connection.getProperties();
            final Enumeration<?> keys = props.propertyNames();
            while (keys.hasMoreElements()) {
                final String key = keys.nextElement().toString();
                this.origConnProps.setProperty(key, props.getProperty(key));
            }
            this.origConnURL = StatementImpl.this.connection.getURL();
        }
        
        @Override
        public void run() {
            final Thread cancelThread = new Thread() {
                @Override
                public void run() {
                    Connection cancelConn = null;
                    java.sql.Statement cancelStmt = null;
                    try {
                        if (StatementImpl.this.connection.getQueryTimeoutKillsConnection()) {
                            CancelTask.this.toCancel.wasCancelled = true;
                            CancelTask.this.toCancel.wasCancelledByTimeout = true;
                            StatementImpl.this.connection.realClose(false, false, true, new MySQLStatementCancelledException(Messages.getString("Statement.ConnectionKilledDueToTimeout")));
                        }
                        else {
                            synchronized (StatementImpl.this.cancelTimeoutMutex) {
                                if (CancelTask.this.origConnURL.equals(StatementImpl.this.connection.getURL())) {
                                    cancelConn = StatementImpl.this.connection.duplicate();
                                    cancelStmt = cancelConn.createStatement();
                                    cancelStmt.execute("KILL QUERY " + CancelTask.this.connectionId);
                                }
                                else {
                                    try {
                                        cancelConn = (Connection)DriverManager.getConnection(CancelTask.this.origConnURL, CancelTask.this.origConnProps);
                                        cancelStmt = cancelConn.createStatement();
                                        cancelStmt.execute("KILL QUERY " + CancelTask.this.connectionId);
                                    }
                                    catch (NullPointerException ex) {}
                                }
                                CancelTask.this.toCancel.wasCancelled = true;
                                CancelTask.this.toCancel.wasCancelledByTimeout = true;
                            }
                        }
                    }
                    catch (SQLException sqlEx) {
                        CancelTask.this.caughtWhileCancelling = sqlEx;
                    }
                    catch (NullPointerException ex2) {}
                    finally {
                        if (cancelStmt != null) {
                            try {
                                cancelStmt.close();
                            }
                            catch (SQLException sqlEx2) {
                                throw new RuntimeException(sqlEx2.toString());
                            }
                        }
                        if (cancelConn != null) {
                            try {
                                cancelConn.close();
                            }
                            catch (SQLException sqlEx2) {
                                throw new RuntimeException(sqlEx2.toString());
                            }
                        }
                        CancelTask.this.toCancel = null;
                        CancelTask.this.origConnProps = null;
                        CancelTask.this.origConnURL = null;
                    }
                }
            };
            cancelThread.start();
        }
    }
}
