// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.io.IOException;
import java.util.GregorianCalendar;
import com.alipay.oceanbase.jdbc.extend.datatype.ComplexDataType;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import com.alipay.oceanbase.jdbc.extend.datatype.INTERVALYM;
import com.alipay.oceanbase.jdbc.extend.datatype.INTERVALDS;
import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMPTZ;
import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMPLTZ;
import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMP;
import java.sql.Timestamp;
import java.util.TimeZone;
import java.sql.Ref;
import java.sql.Clob;
import java.math.BigDecimal;
import com.alipay.oceanbase.jdbc.util.ObCrc32C;
import java.io.Reader;
import java.sql.Blob;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEvent;
import com.alipay.oceanbase.jdbc.log.LogUtils;
import java.sql.Struct;
import java.sql.Array;
import java.sql.ParameterMetaData;
import java.sql.ResultSetMetaData;
import java.io.InputStream;
import com.alipay.oceanbase.jdbc.exceptions.MySQLStatementCancelledException;
import com.alipay.oceanbase.jdbc.exceptions.MySQLTimeoutException;
import java.util.TimerTask;
import java.util.ArrayList;
import com.alipay.oceanbase.jdbc.log.LogFactory;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.sql.Time;
import java.util.Calendar;
import java.sql.SQLException;
import java.lang.reflect.Constructor;
import com.alipay.oceanbase.jdbc.log.Log;

public class ServerPreparedStatement extends PreparedStatement implements ObPrepareStatement
{
    private Log logger;
    private static final Constructor<?> JDBC_4_SPS_CTOR;
    private long checksum;
    protected static final int BLOB_STREAM_READ_BUF_SIZE = 8192;
    private static final int ORACLE_TIME_SCALE = 9;
    private boolean hasOnDuplicateKeyUpdate;
    private boolean detectedLongParameterSwitch;
    private int fieldCount;
    private boolean invalid;
    private SQLException invalidationException;
    private Buffer outByteBuffer;
    private BindValue[] parameterBindings;
    private Field[] parameterFields;
    private Field[] resultFields;
    private boolean sendTypesToServer;
    private long serverStatementId;
    private int stringTypeCode;
    private boolean serverNeedsResetBeforeEachExecution;
    private long currentStatementMetaVersion;
    private String serverPreparedSql;
    protected boolean isCached;
    private boolean useAutoSlowLog;
    private Calendar serverTzCalendar;
    private Calendar defaultTzCalendar;
    private boolean hasCheckedRewrite;
    private boolean canRewrite;
    private int locationOfOnDuplicateKeyUpdate;
    
    private void storeTime(final Buffer intoBuf, final Time tm) throws SQLException {
        intoBuf.ensureCapacity(9);
        intoBuf.writeByte((byte)8);
        intoBuf.writeByte((byte)0);
        intoBuf.writeLong(0L);
        final Calendar sessionCalendar = this.getCalendarInstanceForSessionOrNew();
        synchronized (sessionCalendar) {
            final Date oldTime = sessionCalendar.getTime();
            try {
                sessionCalendar.setTime(tm);
                intoBuf.writeByte((byte)sessionCalendar.get(11));
                intoBuf.writeByte((byte)sessionCalendar.get(12));
                intoBuf.writeByte((byte)sessionCalendar.get(13));
            }
            finally {
                sessionCalendar.setTime(oldTime);
            }
        }
    }
    
    protected static ServerPreparedStatement getInstance(final MySQLConnection conn, final String sql, final String catalog, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        if (!Util.isJdbc4()) {
            return new ServerPreparedStatement(conn, sql, catalog, resultSetType, resultSetConcurrency);
        }
        try {
            return (ServerPreparedStatement)ServerPreparedStatement.JDBC_4_SPS_CTOR.newInstance(conn, sql, catalog, resultSetType, resultSetConcurrency);
        }
        catch (IllegalArgumentException e) {
            throw new SQLException(e.toString(), "S1000");
        }
        catch (InstantiationException e2) {
            throw new SQLException(e2.toString(), "S1000");
        }
        catch (IllegalAccessException e3) {
            throw new SQLException(e3.toString(), "S1000");
        }
        catch (InvocationTargetException e4) {
            final Throwable target = e4.getTargetException();
            if (target instanceof SQLException) {
                throw (SQLException)target;
            }
            throw new SQLException(target.toString(), "S1000");
        }
    }
    
    protected ServerPreparedStatement(final MySQLConnection conn, final String sql, final String catalog, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        super(conn, catalog);
        this.logger = LogFactory.getLogger();
        this.checksum = 1L;
        this.hasOnDuplicateKeyUpdate = false;
        this.detectedLongParameterSwitch = false;
        this.invalid = false;
        this.sendTypesToServer = false;
        this.stringTypeCode = 254;
        this.currentStatementMetaVersion = -1L;
        this.serverPreparedSql = "";
        this.isCached = false;
        this.hasCheckedRewrite = false;
        this.canRewrite = false;
        this.locationOfOnDuplicateKeyUpdate = -2;
        this.psStats.setServerPsType();
        this.checkNullOrEmptyQuery(sql);
        final int startOfStatement = findStartOfStatement(sql);
        this.firstCharOfStmt = StringUtils.firstAlphaCharUc(sql, startOfStatement);
        this.hasOnDuplicateKeyUpdate = (this.firstCharOfStmt == 'I' && this.containsOnDuplicateKeyInString(sql));
        if (this.connection.versionMeetsMinimum(5, 0, 0)) {
            this.serverNeedsResetBeforeEachExecution = !this.connection.versionMeetsMinimum(5, 0, 3);
        }
        else {
            this.serverNeedsResetBeforeEachExecution = !this.connection.versionMeetsMinimum(4, 1, 10);
        }
        this.useAutoSlowLog = this.connection.getAutoSlowLog();
        this.useTrueBoolean = this.connection.versionMeetsMinimum(3, 21, 23);
        final String statementComment = this.connection.getStatementComment();
        this.originalSql = sql;
        if (this.connection.versionMeetsMinimum(4, 1, 2)) {
            this.stringTypeCode = 253;
        }
        else {
            this.stringTypeCode = 254;
        }
        try {
            this.serverPrepare(sql);
        }
        catch (SQLException sqlEx) {
            this.realClose(false, true);
            throw sqlEx;
        }
        catch (Exception ex) {
            this.realClose(false, true);
            final SQLException sqlEx2 = SQLError.createSQLException(ex.toString(), "S1000", this.getExceptionInterceptor());
            sqlEx2.initCause(ex);
            throw sqlEx2;
        }
        this.setResultSetType(resultSetType);
        this.setResultSetConcurrency(resultSetConcurrency);
        this.parameterTypes = new int[this.parameterCount];
    }
    
    @Override
    public void addBatch() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.batchedArgs == null) {
                this.batchedArgs = new ArrayList<Object>();
            }
            this.batchedArgs.add(new BatchedBindValues(this.parameterBindings));
        }
    }
    
    @Override
    public String asSql(final boolean quoteStreamsAndUnknowns) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            PreparedStatement pStmtForSub = null;
            try {
                pStmtForSub = PreparedStatement.getInstance(this.connection, this.originalSql, this.currentCatalog);
                for (int numParameters = pStmtForSub.parameterCount, ourNumParameters = this.parameterCount, i = 0; i < numParameters && i < ourNumParameters; ++i) {
                    if (this.parameterBindings[i] != null) {
                        if (this.parameterBindings[i].isNull) {
                            pStmtForSub.setNull(i + 1, 0);
                        }
                        else {
                            final BindValue bindValue = this.parameterBindings[i];
                            switch (bindValue.bufferType) {
                                case 1: {
                                    pStmtForSub.setByte(i + 1, (byte)bindValue.longBinding);
                                    break;
                                }
                                case 2: {
                                    pStmtForSub.setShort(i + 1, (short)bindValue.longBinding);
                                    break;
                                }
                                case 3: {
                                    pStmtForSub.setInt(i + 1, (int)bindValue.longBinding);
                                    break;
                                }
                                case 8: {
                                    pStmtForSub.setLong(i + 1, bindValue.longBinding);
                                    break;
                                }
                                case 4: {
                                    pStmtForSub.setFloat(i + 1, bindValue.floatBinding);
                                    break;
                                }
                                case 5: {
                                    pStmtForSub.setDouble(i + 1, bindValue.doubleBinding);
                                    break;
                                }
                                default: {
                                    pStmtForSub.setObject(i + 1, this.parameterBindings[i].value);
                                    break;
                                }
                            }
                        }
                    }
                }
                return pStmtForSub.asSql(quoteStreamsAndUnknowns);
            }
            finally {
                if (pStmtForSub != null) {
                    try {
                        pStmtForSub.close();
                    }
                    catch (SQLException ex) {}
                }
            }
        }
    }
    
    @Override
    protected MySQLConnection checkClosed() throws SQLException {
        if (this.invalid) {
            throw this.invalidationException;
        }
        return super.checkClosed();
    }
    
    @Override
    public void clearParameters() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.clearParametersInternal(true);
        }
    }
    
    private void clearParametersInternal(final boolean clearServerParameters) throws SQLException {
        boolean hadLongData = false;
        if (this.parameterBindings != null) {
            for (int i = 0; i < this.parameterCount; ++i) {
                if (this.parameterBindings[i] != null && this.parameterBindings[i].isLongData) {
                    hadLongData = true;
                }
                this.parameterBindings[i].reset();
            }
        }
        if (clearServerParameters && hadLongData) {
            this.serverResetStatement();
            this.detectedLongParameterSwitch = false;
        }
    }
    
    protected void setClosed(final boolean flag) {
        this.isClosed = flag;
    }
    
    @Override
    public void close() throws SQLException {
        final MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null) {
            return;
        }
        synchronized (locallyScopedConn.getConnectionMutex()) {
            if (this.isCached && this.isPoolable() && !this.isClosed) {
                this.clearParameters();
                this.isClosed = true;
                this.connection.recachePreparedStatement(this);
                return;
            }
            this.realClose(true, true);
        }
    }
    
    private void dumpCloseForTestcase() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final StringBuilder buf = new StringBuilder();
            this.connection.generateConnectionCommentBlock(buf);
            buf.append("DEALLOCATE PREPARE debug_stmt_");
            buf.append(this.statementId);
            buf.append(";\n");
            this.connection.dumpTestcaseQuery(buf.toString());
        }
    }
    
    private void dumpExecuteForTestcase() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final StringBuilder buf = new StringBuilder();
            for (int i = 0; i < this.parameterCount; ++i) {
                this.connection.generateConnectionCommentBlock(buf);
                buf.append("SET @debug_stmt_param");
                buf.append(this.statementId);
                buf.append("_");
                buf.append(i);
                buf.append("=");
                if (this.parameterBindings[i].isNull) {
                    buf.append("NULL");
                }
                else {
                    buf.append(this.parameterBindings[i].toString(true));
                }
                buf.append(";\n");
            }
            this.connection.generateConnectionCommentBlock(buf);
            buf.append("EXECUTE debug_stmt_");
            buf.append(this.statementId);
            if (this.parameterCount > 0) {
                buf.append(" USING ");
                for (int i = 0; i < this.parameterCount; ++i) {
                    if (i > 0) {
                        buf.append(", ");
                    }
                    buf.append("@debug_stmt_param");
                    buf.append(this.statementId);
                    buf.append("_");
                    buf.append(i);
                }
            }
            buf.append(";\n");
            this.connection.dumpTestcaseQuery(buf.toString());
        }
    }
    
    private void dumpPrepareForTestcase() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final StringBuilder buf = new StringBuilder(this.originalSql.length() + 64);
            this.connection.generateConnectionCommentBlock(buf);
            buf.append("PREPARE debug_stmt_");
            buf.append(this.statementId);
            buf.append(" FROM \"");
            buf.append(this.originalSql);
            buf.append("\";\n");
            this.connection.dumpTestcaseQuery(buf.toString());
        }
    }
    
    @Override
    protected long[] executeBatchSerially(final int batchTimeout) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final MySQLConnection locallyScopedConn = this.connection;
            if (locallyScopedConn.isReadOnly()) {
                throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.2") + Messages.getString("ServerPreparedStatement.3"), "S1009", this.getExceptionInterceptor());
            }
            this.clearWarnings();
            final BindValue[] oldBindValues = this.parameterBindings;
            try {
                long[] updateCounts = null;
                if (this.batchedArgs != null) {
                    final int nbrCommands = this.batchedArgs.size();
                    updateCounts = new long[nbrCommands];
                    if (this.retrieveGeneratedKeys) {
                        this.batchedGeneratedKeys = new ArrayList<ResultSetRow>(nbrCommands);
                    }
                    for (int i = 0; i < nbrCommands; ++i) {
                        updateCounts[i] = -3L;
                    }
                    SQLException sqlEx = null;
                    int commandIndex = 0;
                    BindValue[] previousBindValuesForBatch = null;
                    CancelTask timeoutTask = null;
                    try {
                        if (locallyScopedConn.getEnableQueryTimeouts() && batchTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                            timeoutTask = new CancelTask(this);
                            locallyScopedConn.getCancelTimer().schedule(timeoutTask, batchTimeout);
                        }
                        for (commandIndex = 0; commandIndex < nbrCommands; ++commandIndex) {
                            final Object arg = this.batchedArgs.get(commandIndex);
                            try {
                                if (arg instanceof String) {
                                    updateCounts[commandIndex] = this.executeUpdateInternal((String)arg, true, this.retrieveGeneratedKeys);
                                    this.getBatchedGeneratedKeys((this.results.getFirstCharOfQuery() == 'I' && this.containsOnDuplicateKeyInString((String)arg)) ? 1 : 0);
                                }
                                else {
                                    this.parameterBindings = ((BatchedBindValues)arg).batchedParameterValues;
                                    if (previousBindValuesForBatch != null) {
                                        for (int j = 0; j < this.parameterBindings.length; ++j) {
                                            if (this.parameterBindings[j].bufferType != previousBindValuesForBatch[j].bufferType) {
                                                this.sendTypesToServer = true;
                                                break;
                                            }
                                        }
                                    }
                                    try {
                                        updateCounts[commandIndex] = this.executeUpdateInternal(false, true);
                                    }
                                    finally {
                                        previousBindValuesForBatch = this.parameterBindings;
                                    }
                                    this.getBatchedGeneratedKeys(this.containsOnDuplicateKeyUpdateInSQL() ? 1 : 0);
                                }
                            }
                            catch (SQLException ex) {
                                updateCounts[commandIndex] = -3L;
                                if (!this.continueBatchOnError || ex instanceof MySQLTimeoutException || ex instanceof MySQLStatementCancelledException || this.hasDeadlockOrTimeoutRolledBackTx(ex)) {
                                    final long[] newUpdateCounts = new long[commandIndex];
                                    System.arraycopy(updateCounts, 0, newUpdateCounts, 0, commandIndex);
                                    throw SQLError.createBatchUpdateException(ex, newUpdateCounts, this.getExceptionInterceptor());
                                }
                                sqlEx = ex;
                            }
                        }
                    }
                    finally {
                        if (timeoutTask != null) {
                            timeoutTask.cancel();
                            locallyScopedConn.getCancelTimer().purge();
                        }
                        this.resetCancelledState();
                    }
                    if (sqlEx != null) {
                        throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
                    }
                }
                return (updateCounts != null) ? updateCounts : new long[0];
            }
            finally {
                this.parameterBindings = oldBindValues;
                this.sendTypesToServer = true;
                this.clearBatch();
            }
        }
    }
    
    @Override
    protected ResultSetInternalMethods executeInternal(final int maxRowsToRetrieve, final Buffer sendPacket, final boolean createStreamingResultSet, final boolean queryIsSelectOnly, final Field[] metadataFromCache, final boolean isBatch) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final long serverExectueStartNs = System.nanoTime();
            ++this.numberOfExecutions;
            try {
                return this.serverExecute(maxRowsToRetrieve, createStreamingResultSet, metadataFromCache);
            }
            catch (SQLException sqlEx) {
                if (this.connection.getEnablePacketDebug()) {
                    this.connection.getIO().dumpPacketRingBuffer();
                }
                if (this.connection.getDumpQueriesOnException()) {
                    final String extractedSql = this.toString();
                    final StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
                    messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
                    messageBuf.append(extractedSql);
                    messageBuf.append("\n\n");
                    sqlEx = ConnectionImpl.appendMessageToException(sqlEx, messageBuf.toString(), this.getExceptionInterceptor());
                }
                throw sqlEx;
            }
            catch (Exception ex) {
                if (this.connection.getEnablePacketDebug()) {
                    this.connection.getIO().dumpPacketRingBuffer();
                }
                SQLException sqlEx2 = SQLError.createSQLException(ex.toString(), "S1000", this.getExceptionInterceptor());
                if (this.connection.getDumpQueriesOnException()) {
                    final String extractedSql2 = this.toString();
                    final StringBuilder messageBuf2 = new StringBuilder(extractedSql2.length() + 32);
                    messageBuf2.append("\n\nQuery being executed when exception was thrown:\n");
                    messageBuf2.append(extractedSql2);
                    messageBuf2.append("\n\n");
                    sqlEx2 = ConnectionImpl.appendMessageToException(sqlEx2, messageBuf2.toString(), this.getExceptionInterceptor());
                }
                sqlEx2.initCause(ex);
                throw sqlEx2;
            }
            finally {
                this.psStats.addServerPSExecuteTotalCostNs(System.nanoTime() - serverExectueStartNs);
            }
        }
    }
    
    @Override
    protected Buffer fillSendPacket() throws SQLException {
        return null;
    }
    
    @Override
    protected Buffer fillSendPacket(final byte[][] batchedParameterStrings, final InputStream[] batchedParameterStreams, final boolean[] batchedIsStream, final int[] batchedStreamLengths) throws SQLException {
        return null;
    }
    
    protected BindValue getBinding(int parameterIndex, final boolean forLongData) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.parameterBindings.length == 0) {
                throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.8"), "S1009", this.getExceptionInterceptor());
            }
            if (--parameterIndex < 0 || parameterIndex >= this.parameterBindings.length) {
                throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.9") + (parameterIndex + 1) + Messages.getString("ServerPreparedStatement.10") + this.parameterBindings.length, "S1009", this.getExceptionInterceptor());
            }
            if (this.parameterBindings[parameterIndex] == null) {
                this.parameterBindings[parameterIndex] = new BindValue();
            }
            else if (this.parameterBindings[parameterIndex].isLongData && !forLongData) {
                this.detectedLongParameterSwitch = true;
            }
            return this.parameterBindings[parameterIndex];
        }
    }
    
    public BindValue[] getParameterBindValues() {
        return this.parameterBindings;
    }
    
    byte[] getBytes(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final BindValue bindValue = this.getBinding(parameterIndex, false);
            if (bindValue.isNull) {
                return null;
            }
            if (bindValue.isLongData) {
                throw SQLError.createSQLFeatureNotSupportedException();
            }
            if (this.outByteBuffer == null) {
                this.outByteBuffer = new Buffer(this.connection.getNetBufferLength());
            }
            this.outByteBuffer.clear();
            final int originalPosition = this.outByteBuffer.getPosition();
            this.storeBinding(this.outByteBuffer, bindValue, this.connection.getIO());
            final int newPosition = this.outByteBuffer.getPosition();
            final int length = newPosition - originalPosition;
            final byte[] valueAsBytes = new byte[length];
            System.arraycopy(this.outByteBuffer.getByteBuffer(), originalPosition, valueAsBytes, 0, length);
            return valueAsBytes;
        }
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.resultFields == null) {
                return null;
            }
            return new com.alipay.oceanbase.jdbc.ResultSetMetaData(this.resultFields, this.connection.getUseOldAliasMetadataBehavior(), this.connection.getYearIsDateType(), this.getExceptionInterceptor());
        }
    }
    
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.parameterMetaData == null) {
                this.parameterMetaData = new MysqlParameterMetadata(this.parameterFields, this.parameterCount, this.getExceptionInterceptor());
            }
            return this.parameterMetaData;
        }
    }
    
    @Override
    boolean isNull(final int paramIndex) {
        throw new IllegalArgumentException(Messages.getString("ServerPreparedStatement.7"));
    }
    
    @Override
    protected void realClose(final boolean calledExplicitly, final boolean closeOpenResults) throws SQLException {
        final MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null) {
            return;
        }
        synchronized (locallyScopedConn.getConnectionMutex()) {
            final long closeStartNs = System.nanoTime();
            if (this.connection != null) {
                if (this.connection.getAutoGenerateTestcaseScript()) {
                    this.dumpCloseForTestcase();
                }
                SQLException exceptionDuringClose = null;
                if (calledExplicitly && !this.connection.isClosed()) {
                    synchronized (this.connection.getConnectionMutex()) {
                        try {
                            final MysqlIO mysql = this.connection.getIO();
                            final Buffer packet = mysql.getSharedSendPacket();
                            packet.writeByte((byte)25);
                            packet.writeLong(this.serverStatementId);
                            mysql.sendCommand(25, null, packet, true, null, 0);
                        }
                        catch (SQLException sqlEx) {
                            exceptionDuringClose = sqlEx;
                        }
                    }
                }
                if (this.isCached) {
                    this.connection.decachePreparedStatement(this);
                }
                super.realClose(calledExplicitly, closeOpenResults);
                this.clearParametersInternal(false);
                this.parameterBindings = null;
                this.parameterFields = null;
                this.resultFields = null;
                this.psStats.addServerPsRealCloseCostNs(System.nanoTime() - closeStartNs);
                if (exceptionDuringClose != null) {
                    throw exceptionDuringClose;
                }
            }
        }
    }
    
    protected void rePrepare() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.invalidationException = null;
            try {
                this.serverPrepare(this.originalSql);
            }
            catch (SQLException sqlEx) {
                this.invalidationException = sqlEx;
            }
            catch (Exception ex) {
                (this.invalidationException = SQLError.createSQLException(ex.toString(), "S1000", this.getExceptionInterceptor())).initCause(ex);
            }
            if (this.invalidationException != null) {
                this.invalid = true;
                this.parameterBindings = null;
                this.parameterFields = null;
                this.resultFields = null;
                if (this.results != null) {
                    try {
                        this.results.close();
                    }
                    catch (Exception ex2) {}
                }
                if (this.generatedKeysResults != null) {
                    try {
                        this.generatedKeysResults.close();
                    }
                    catch (Exception ex3) {}
                }
                try {
                    this.closeAllOpenResults();
                }
                catch (Exception ex4) {}
                if (this.connection != null && !this.connection.getDontTrackOpenResources()) {
                    this.connection.unregisterStatement(this);
                }
            }
        }
    }
    
    @Override
    boolean isCursorRequired() throws SQLException {
        return this.resultFields != null && this.connection.isCursorFetchEnabled() && this.getResultSetType() == 1003 && this.getResultSetConcurrency() == 1007 && this.getFetchSize() > 0;
    }
    
    protected void updateParamInfoBaseOnResultSet(final Field[] fields) {
    }
    
    private ResultSetInternalMethods serverExecute(final int maxRowsToRetrieve, final boolean createStreamingResultSet, final Field[] metadataFromCache) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final long serverExecuteStartNs = System.nanoTime();
            final MysqlIO mysql = this.connection.getIO();
            if (mysql.shouldIntercept()) {
                final ResultSetInternalMethods interceptedResults = mysql.invokeStatementInterceptorsPre(this.originalSql, this, true);
                if (interceptedResults != null) {
                    return interceptedResults;
                }
            }
            if (this.detectedLongParameterSwitch) {
                boolean firstFound = false;
                long boundTimeToCheck = 0L;
                for (int i = 0; i < this.parameterCount - 1; ++i) {
                    if (this.parameterBindings[i].isLongData) {
                        if (firstFound && boundTimeToCheck != this.parameterBindings[i].boundBeforeExecutionNum) {
                            throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.11") + Messages.getString("ServerPreparedStatement.12"), "S1C00", this.getExceptionInterceptor());
                        }
                        firstFound = true;
                        boundTimeToCheck = this.parameterBindings[i].boundBeforeExecutionNum;
                    }
                }
                this.serverResetStatement();
            }
            for (int j = 0; j < this.parameterCount; ++j) {
                if (!this.parameterBindings[j].isSet) {
                    if (this instanceof ServerCallableStatement) {
                        final ServerCallableStatement callableStatement = (ServerCallableStatement)this;
                        final CallableStatement.CallableStatementParam param = callableStatement.checkIsOutputParam(j + 1);
                        if (param.isOut) {
                            this.setNull(j + 1, 6);
                            continue;
                        }
                    }
                    throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.13") + (j + 1) + Messages.getString("ServerPreparedStatement.14"), "S1009", this.getExceptionInterceptor());
                }
            }
            for (int j = 0; j < this.parameterCount; ++j) {
                if (this.parameterBindings[j].isLongData) {
                    this.serverLongData(j, this.parameterBindings[j]);
                }
            }
            if (this.connection.getAutoGenerateTestcaseScript()) {
                this.dumpExecuteForTestcase();
            }
            final Buffer packet = mysql.getSharedSendPacket();
            packet.clear();
            packet.writeByte((byte)23);
            packet.writeLong(this.serverStatementId);
            if (this.connection.versionMeetsMinimum(4, 1, 2)) {
                if (this.isCursorRequired()) {
                    packet.writeByte((byte)1);
                }
                else {
                    packet.writeByte((byte)0);
                }
                if (this.connection.getIO().isOracleMode() && this.connection.getUseServerPsStmtChecksum()) {
                    packet.writeLong(this.checksum);
                }
                else {
                    packet.writeLong(1L);
                }
            }
            final int nullCount = (this.parameterCount + 7) / 8;
            final int nullBitsPosition = packet.getPosition();
            for (int i = 0; i < nullCount; ++i) {
                packet.writeByte((byte)0);
            }
            final byte[] nullBitsBuffer = new byte[nullCount];
            if (this.connection.getAllowAlwaysSendParamTypes()) {
                this.sendTypesToServer = true;
            }
            packet.writeByte((byte)(this.sendTypesToServer ? 1 : 0));
            if (this.sendTypesToServer) {
                for (int k = 0; k < this.parameterCount; ++k) {
                    packet.writeInt(this.parameterBindings[k].bufferType);
                    if (this.parameterBindings[k].bufferType == 160) {
                        final Object obj = this.parameterBindings[k].value;
                        if (obj instanceof Array) {
                            this.storeArrayTypeInfo(packet, obj);
                        }
                        else {
                            if (!(obj instanceof Struct)) {
                                throw new SQLException("complex param type is not supported\uff0c only array is supported");
                            }
                            this.storeStructTypeInfo(packet, obj);
                        }
                    }
                }
            }
            for (int k = 0; k < this.parameterCount; ++k) {
                if (!this.parameterBindings[k].isLongData) {
                    if (!this.parameterBindings[k].isNull) {
                        this.storeBinding(packet, this.parameterBindings[k], mysql);
                    }
                    else {
                        final byte[] array = nullBitsBuffer;
                        final int n = k / 8;
                        array[n] |= (byte)(1 << (k & 0x7));
                    }
                }
            }
            final int endPosition = packet.getPosition();
            packet.setPosition(nullBitsPosition);
            packet.writeBytesNoNull(nullBitsBuffer);
            packet.setPosition(endPosition);
            long begin = 0L;
            final boolean logSlowQueries = this.connection.getLogSlowQueries();
            final boolean gatherPerformanceMetrics = this.connection.getGatherPerformanceMetrics();
            if (this.profileSQL || logSlowQueries || gatherPerformanceMetrics) {
                begin = mysql.getCurrentTimeNanosOrMillis();
            }
            this.resetCancelledState();
            CancelTask timeoutTask = null;
            try {
                String queryAsString = "";
                if (this.profileSQL || logSlowQueries || gatherPerformanceMetrics) {
                    queryAsString = this.asSql(true);
                }
                if (this.connection.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && this.connection.versionMeetsMinimum(5, 0, 0)) {
                    timeoutTask = new CancelTask(this);
                    this.connection.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
                }
                this.statementBegins();
                final long sendServerPrepareStartNs = System.nanoTime();
                this.psStats.addServerPSPreExecuteCostNs(sendServerPrepareStartNs - serverExecuteStartNs);
                final Buffer resultPacket = mysql.sendCommand(23, null, packet, false, null, 0);
                this.psStats.addServerPSExecuteCostNs(System.nanoTime() - sendServerPrepareStartNs);
                long queryEndTime = 0L;
                if (logSlowQueries || gatherPerformanceMetrics || this.profileSQL) {
                    queryEndTime = mysql.getCurrentTimeNanosOrMillis();
                }
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    this.connection.getCancelTimer().purge();
                    if (timeoutTask.caughtWhileCancelling != null) {
                        throw timeoutTask.caughtWhileCancelling;
                    }
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
                boolean queryWasSlow = false;
                if (logSlowQueries || gatherPerformanceMetrics) {
                    final long elapsedTime = queryEndTime - begin;
                    if (logSlowQueries) {
                        if (this.useAutoSlowLog) {
                            queryWasSlow = (elapsedTime > this.connection.getSlowQueryThresholdMillis());
                        }
                        else {
                            queryWasSlow = this.connection.isAbonormallyLongQuery(elapsedTime);
                            this.connection.reportQueryTime(elapsedTime);
                        }
                    }
                    if (queryWasSlow) {
                        final StringBuilder mesgBuf = new StringBuilder(48 + this.originalSql.length());
                        mesgBuf.append(Messages.getString("ServerPreparedStatement.15"));
                        mesgBuf.append(mysql.getSlowQueryThreshold());
                        mesgBuf.append(Messages.getString("ServerPreparedStatement.15a"));
                        mesgBuf.append(elapsedTime);
                        mesgBuf.append(Messages.getString("ServerPreparedStatement.16"));
                        mesgBuf.append("as prepared: ");
                        mesgBuf.append(this.originalSql);
                        mesgBuf.append("\n\n with parameters bound:\n\n");
                        mesgBuf.append(queryAsString);
                        this.eventSink.consumeEvent(new ProfilerEvent((byte)6, "", this.currentCatalog, this.connection.getId(), this.getId(), 0, System.currentTimeMillis(), elapsedTime, mysql.getQueryTimingUnits(), null, LogUtils.findCallingClassAndMethod(new Throwable()), mesgBuf.toString()));
                    }
                    if (gatherPerformanceMetrics) {
                        this.connection.registerQueryExecutionTime(elapsedTime);
                    }
                }
                this.connection.incrementNumberOfPreparedExecutes();
                if (this.profileSQL) {
                    (this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection)).consumeEvent(new ProfilerEvent((byte)4, "", this.currentCatalog, this.connectionId, this.statementId, -1, System.currentTimeMillis(), mysql.getCurrentTimeNanosOrMillis() - begin, mysql.getQueryTimingUnits(), null, LogUtils.findCallingClassAndMethod(new Throwable()), this.truncateQueryToLog(queryAsString)));
                }
                ResultSetInternalMethods rs = mysql.readAllResults(this, maxRowsToRetrieve, this.resultSetType, this.resultSetConcurrency, createStreamingResultSet, this.currentCatalog, resultPacket, true, this.fieldCount, metadataFromCache);
                if (mysql.shouldIntercept()) {
                    final ResultSetInternalMethods interceptedResults2 = mysql.invokeStatementInterceptorsPost(this.originalSql, this, rs, true, null);
                    if (interceptedResults2 != null) {
                        rs = interceptedResults2;
                    }
                }
                if (this.profileSQL) {
                    final long fetchEndTime = mysql.getCurrentTimeNanosOrMillis();
                    this.eventSink.consumeEvent(new ProfilerEvent((byte)5, "", this.currentCatalog, this.connection.getId(), this.getId(), 0, System.currentTimeMillis(), fetchEndTime - queryEndTime, mysql.getQueryTimingUnits(), null, LogUtils.findCallingClassAndMethod(new Throwable()), null));
                }
                if (queryWasSlow && this.connection.getExplainSlowQueries()) {
                    mysql.explainSlowQuery(StringUtils.getBytes(queryAsString), queryAsString);
                }
                if (!createStreamingResultSet && this.serverNeedsResetBeforeEachExecution) {
                    this.serverResetStatement();
                }
                this.sendTypesToServer = false;
                this.results = rs;
                if (mysql.hadWarnings()) {
                    mysql.scanForAndThrowDataTruncation();
                }
                final ResultSetImpl rsi = (ResultSetImpl)rs;
                this.updateParamInfoBaseOnResultSet((metadataFromCache == null) ? rsi.getFields() : metadataFromCache);
                return rs;
            }
            catch (SQLException sqlEx) {
                if (mysql.shouldIntercept()) {
                    mysql.invokeStatementInterceptorsPost(this.originalSql, this, null, true, sqlEx);
                }
                throw sqlEx;
            }
            finally {
                this.statementExecuting.set(false);
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    this.connection.getCancelTimer().purge();
                }
            }
        }
    }
    
    private int getMysqlType(final int complexType) throws SQLException {
        switch (complexType) {
            case 2: {
                return 12;
            }
            case 4: {
                return 160;
            }
            case 0: {
                return 246;
            }
            case 1: {
                return 15;
            }
            case 3: {
                return 160;
            }
            case 6: {
                return 203;
            }
            default: {
                throw new SQLException("unsupported complex type");
            }
        }
    }
    
    private void storeArrayTypeInfo(final Buffer packet, final Object value) throws SQLException {
        final ObArray array = (ObArray)value;
        packet.writeLenBytes(new byte[0]);
        packet.writeLenBytes(new byte[0]);
        packet.writeFieldLength(array.getComplexType().getVersion());
        final int elementType = this.getMysqlType(array.getBaseType());
        packet.writeByte((byte)elementType);
        if (elementType >= 160 && elementType <= 162) {
            packet.writeLenBytes(StringUtils.getBytes(array.getComplexType().getAttrType(0).getSchemaName()));
            packet.writeLenBytes(StringUtils.getBytes(array.getComplexType().getAttrType(0).getTypeName()));
            packet.writeFieldLength(array.getComplexType().getAttrType(0).getVersion());
        }
    }
    
    private void storeStructTypeInfo(final Buffer packet, final Object value) throws SQLException {
        final StructImpl struct = (StructImpl)value;
        packet.writeLenBytes(StringUtils.getBytes(struct.getComplexType().getSchemaName()));
        packet.writeLenBytes(StringUtils.getBytes(struct.getComplexType().getTypeName()));
        packet.writeFieldLength(struct.getComplexType().getVersion());
    }
    
    private void serverLongData(final int parameterIndex, final BindValue longData) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final long startNs = System.nanoTime();
            final MysqlIO mysql = this.connection.getIO();
            final Buffer packet = mysql.getSharedSendPacket();
            final Object value = longData.value;
            if (value instanceof byte[]) {
                packet.clear();
                packet.writeByte((byte)24);
                packet.writeLong(this.serverStatementId);
                packet.writeInt(parameterIndex);
                packet.writeBytesNoNull((byte[])longData.value);
                mysql.sendCommand(24, null, packet, true, null, 0);
            }
            else if (value instanceof InputStream) {
                this.storeStream(mysql, parameterIndex, packet, (InputStream)value);
            }
            else if (value instanceof Blob) {
                this.storeStream(mysql, parameterIndex, packet, ((Blob)value).getBinaryStream());
            }
            else {
                if (!(value instanceof Reader)) {
                    throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.18") + value.getClass().getName() + "'", "S1009", this.getExceptionInterceptor());
                }
                this.storeReader(mysql, parameterIndex, packet, (Reader)value);
            }
            this.psStats.addServerPsSendLongDataCostNs(System.nanoTime() - startNs);
        }
    }
    
    private void serverPrepare(final String sql) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final long startNs = System.nanoTime();
            final MysqlIO mysql = this.connection.getIO();
            if (this.connection.getAutoGenerateTestcaseScript()) {
                this.dumpPrepareForTestcase();
            }
            try {
                long begin = 0L;
                if (StringUtils.startsWithIgnoreCaseAndWs(sql, "LOAD DATA")) {
                    this.isLoadDataQuery = true;
                }
                else {
                    this.isLoadDataQuery = false;
                }
                if (this.connection.getProfileSql()) {
                    begin = System.currentTimeMillis();
                }
                String characterEncoding = null;
                final String connectionEncoding = this.connection.getEncoding();
                if (!this.isLoadDataQuery && this.connection.getUseUnicode() && connectionEncoding != null) {
                    characterEncoding = connectionEncoding;
                }
                final byte[] b = StringUtils.getBytes(sql, characterEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.connection, this.connection.getExceptionInterceptor());
                final ObCrc32C crc32C = new ObCrc32C();
                crc32C.reset();
                crc32C.update(b, 0, b.length);
                this.checksum = crc32C.getValue();
                final Buffer prepareResultPacket = mysql.sendCommand(22, sql, null, false, characterEncoding, 0);
                if (this.connection.versionMeetsMinimum(4, 1, 1)) {
                    prepareResultPacket.setPosition(1);
                }
                else {
                    prepareResultPacket.setPosition(0);
                }
                this.serverStatementId = prepareResultPacket.readLong();
                this.fieldCount = prepareResultPacket.readInt();
                this.parameterCount = prepareResultPacket.readInt();
                this.parameterBindings = new BindValue[this.parameterCount];
                for (int i = 0; i < this.parameterCount; ++i) {
                    this.parameterBindings[i] = new BindValue();
                }
                this.connection.incrementNumberOfPrepares();
                if (this.profileSQL) {
                    this.eventSink.consumeEvent(new ProfilerEvent((byte)2, "", this.currentCatalog, this.connectionId, this.statementId, -1, System.currentTimeMillis(), mysql.getCurrentTimeNanosOrMillis() - begin, mysql.getQueryTimingUnits(), null, LogUtils.findCallingClassAndMethod(new Throwable()), this.truncateQueryToLog(sql)));
                }
                final boolean checkEOF = !mysql.isEOFDeprecated();
                if (this.parameterCount > 0 && this.connection.versionMeetsMinimum(4, 1, 2) && !mysql.isVersion(5, 0, 0)) {
                    this.parameterFields = new Field[this.parameterCount];
                    for (int j = 0; j < this.parameterCount; ++j) {
                        final Buffer metaDataPacket = mysql.readPacket();
                        this.parameterFields[j] = mysql.unpackField(metaDataPacket, false);
                    }
                    if (checkEOF) {
                        mysql.readPacket();
                    }
                }
                if (this.fieldCount > 0) {
                    this.resultFields = new Field[this.fieldCount];
                    for (int j = 0; j < this.fieldCount; ++j) {
                        final Buffer fieldPacket = mysql.readPacket();
                        this.resultFields[j] = mysql.unpackField(fieldPacket, false);
                    }
                    if (checkEOF) {
                        mysql.readPacket();
                    }
                }
                mysql.readAndCheckExtraOKPacket();
            }
            catch (SQLException sqlEx) {
                if (this.connection.getDumpQueriesOnException()) {
                    final StringBuilder messageBuf = new StringBuilder(this.originalSql.length() + 32);
                    messageBuf.append("\n\nQuery being prepared when exception was thrown:\n\n");
                    messageBuf.append(this.originalSql);
                    sqlEx = ConnectionImpl.appendMessageToException(sqlEx, messageBuf.toString(), this.getExceptionInterceptor());
                }
                throw sqlEx;
            }
            finally {
                this.connection.getIO().clearInputStream();
                this.psStats.addServerPSDoPrepareCostNs(System.nanoTime() - startNs);
            }
        }
    }
    
    private String truncateQueryToLog(final String sql) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            String query = null;
            if (sql.length() > this.connection.getMaxQuerySizeToLog()) {
                final StringBuilder queryBuf = new StringBuilder(this.connection.getMaxQuerySizeToLog() + 12);
                queryBuf.append(sql.substring(0, this.connection.getMaxQuerySizeToLog()));
                queryBuf.append(Messages.getString("MysqlIO.25"));
                query = queryBuf.toString();
            }
            else {
                query = sql;
            }
            return query;
        }
    }
    
    private void serverResetStatement() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final long startNs = System.nanoTime();
            final MysqlIO mysql = this.connection.getIO();
            final Buffer packet = mysql.getSharedSendPacket();
            packet.clear();
            packet.writeByte((byte)26);
            packet.writeLong(this.serverStatementId);
            try {
                mysql.sendCommand(26, null, packet, !this.connection.versionMeetsMinimum(4, 1, 2), null, 0);
            }
            catch (SQLException sqlEx) {
                throw sqlEx;
            }
            catch (Exception ex) {
                final SQLException sqlEx2 = SQLError.createSQLException(ex.toString(), "S1000", this.getExceptionInterceptor());
                sqlEx2.initCause(ex);
                throw sqlEx2;
            }
            finally {
                mysql.clearInputStream();
                this.psStats.addResetPSCostNs(System.nanoTime() - startNs);
            }
        }
    }
    
    @Override
    public void setArray(final int parameterIndex, final Array x) throws SQLException {
        this.checkClosed();
        if (!this.connection.getIO().isOracleMode()) {
            throw SQLError.createSQLFeatureNotSupportedException();
        }
        if (x == null) {
            this.setNull(parameterIndex, 2003);
        }
        else {
            final BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, 160);
            binding.value = x;
        }
    }
    
    @Override
    public void setStruct(final int parameterIndex, final Struct x) throws SQLException {
        this.checkClosed();
        if (!this.connection.getIO().isOracleMode()) {
            throw SQLError.createSQLFeatureNotSupportedException();
        }
        if (x == null) {
            this.setNull(parameterIndex, 2002);
        }
        else {
            final BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, 160);
            binding.value = x;
        }
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, -2);
            }
            else {
                final BindValue binding = this.getBinding(parameterIndex, true);
                this.resetToType(binding, 252);
                binding.value = x;
                binding.isLongData = true;
                if (this.connection.getUseStreamLengthsInPrepStmts()) {
                    binding.bindLength = length;
                }
                else {
                    binding.bindLength = -1L;
                }
            }
        }
    }
    
    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, 3);
            }
            else {
                final BindValue binding = this.getBinding(parameterIndex, false);
                if (this.connection.versionMeetsMinimum(5, 0, 3)) {
                    this.resetToType(binding, 246);
                }
                else {
                    this.resetToType(binding, this.stringTypeCode);
                }
                binding.value = StringUtils.fixDecimalExponent(StringUtils.consistentToString(x));
            }
        }
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, -2);
            }
            else {
                final BindValue binding = this.getBinding(parameterIndex, true);
                this.resetToType(binding, 252);
                binding.value = x;
                binding.isLongData = true;
                if (this.connection.getUseStreamLengthsInPrepStmts()) {
                    binding.bindLength = length;
                }
                else {
                    binding.bindLength = -1L;
                }
            }
        }
    }
    
    @Override
    public void setBlob(final int parameterIndex, final Blob x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, -2);
            }
            else if (!this.connection.getIO().isUseOracleLocator() || ((ObBlob)x).getLocator() == null || !this.connection.getUseServerPreparedStmts()) {
                final BindValue binding = this.getBinding(parameterIndex, true);
                this.resetToType(binding, 252);
                binding.value = x;
                binding.isLongData = true;
                if (this.connection.getUseStreamLengthsInPrepStmts()) {
                    binding.bindLength = x.length();
                }
                else {
                    binding.bindLength = -1L;
                }
            }
            else {
                final BindValue binding = this.getBinding(parameterIndex, true);
                this.resetToType(binding, 210);
                binding.value = ((ObBlob)x).getLocator().binaryData;
            }
        }
    }
    
    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        this.setByte(parameterIndex, (byte)(x ? 1 : 0));
    }
    
    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        this.checkClosed();
        final BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 1);
        binding.longBinding = x;
    }
    
    @Override
    public void setBytes(final int parameterIndex, byte[] x) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(parameterIndex, -2);
        }
        else {
            final BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, 253);
            if (this.connection.getIO().isOracleMode()) {
                final StringBuilder sb = new StringBuilder();
                for (final byte b : x) {
                    sb.append(String.format("%02x", b));
                }
                x = sb.toString().getBytes();
            }
            binding.value = x;
        }
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (reader == null) {
                this.setNull(parameterIndex, -2);
            }
            else {
                final BindValue binding = this.getBinding(parameterIndex, true);
                this.resetToType(binding, 252);
                binding.value = reader;
                binding.isLongData = true;
                if (this.connection.getUseStreamLengthsInPrepStmts()) {
                    binding.bindLength = length;
                }
                else {
                    binding.bindLength = -1L;
                }
            }
        }
    }
    
    @Override
    public void setClob(final int parameterIndex, final Clob x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, -2);
            }
            else if (!this.connection.getIO().isUseOracleLocator() || ((ObClob)x).getLocator() == null || !this.connection.getUseServerPreparedStmts()) {
                final BindValue binding = this.getBinding(parameterIndex, true);
                this.resetToType(binding, 252);
                binding.value = x.getCharacterStream();
                binding.isLongData = true;
                if (this.connection.getUseStreamLengthsInPrepStmts()) {
                    binding.bindLength = x.length();
                }
                else {
                    binding.bindLength = -1L;
                }
            }
            else {
                final BindValue binding = this.getBinding(parameterIndex, true);
                this.resetToType(binding, 211);
                binding.value = ((ObClob)x).getLocator().binaryData;
            }
        }
    }
    
    @Override
    public void setDate(final int parameterIndex, final java.sql.Date x) throws SQLException {
        this.setDate(parameterIndex, x, null);
    }
    
    @Override
    public void setDate(final int parameterIndex, final java.sql.Date x, final Calendar cal) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 91);
        }
        else {
            final BindValue binding = this.getBinding(parameterIndex, false);
            if (this.connection.getIO().isOracleMode()) {
                this.resetToType(binding, 12);
            }
            else {
                this.resetToType(binding, 10);
            }
            binding.value = x;
        }
    }
    
    @Override
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (!this.connection.getAllowNanAndInf() && (x == Double.POSITIVE_INFINITY || x == Double.NEGATIVE_INFINITY || Double.isNaN(x))) {
                throw SQLError.createSQLException("'" + x + "' is not a valid numeric or approximate numeric value", "S1009", this.getExceptionInterceptor());
            }
            final BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, 5);
            binding.doubleBinding = x;
        }
    }
    
    @Override
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        this.checkClosed();
        final BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 4);
        binding.floatBinding = x;
    }
    
    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        this.checkClosed();
        final BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 3);
        binding.longBinding = x;
    }
    
    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        this.checkClosed();
        final BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 8);
        binding.longBinding = x;
    }
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        this.checkClosed();
        final BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 6);
        binding.isNull = true;
    }
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
        this.checkClosed();
        final BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 6);
        binding.isNull = true;
    }
    
    @Override
    public void setRef(final int i, final Ref x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        this.checkClosed();
        final BindValue binding = this.getBinding(parameterIndex, false);
        this.resetToType(binding, 2);
        binding.longBinding = x;
    }
    
    @Override
    public void setString(final int parameterIndex, final String x) throws SQLException {
        this.checkClosed();
        if (x == null) {
            this.setNull(parameterIndex, 1);
        }
        else {
            final BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, this.stringTypeCode);
            binding.value = x;
        }
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.setTimeInternal(parameterIndex, x, null, this.connection.getDefaultTimeZone(), false);
        }
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.setTimeInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
        }
    }
    
    private void setTimeInternal(final int parameterIndex, final Time x, final Calendar targetCalendar, final TimeZone tz, final boolean rollForward) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 92);
        }
        else {
            final BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, 11);
            if (!this.useLegacyDatetimeCode) {
                binding.value = x;
            }
            else {
                final Calendar sessionCalendar = this.getCalendarInstanceForSessionOrNew();
                binding.value = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
            }
        }
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.setTimestampInternal(parameterIndex, x, null, this.connection.getDefaultTimeZone(), false);
        }
    }
    
    @Override
    public void setTIMESTAMP(final int parameterIndex, final Timestamp x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, 93);
            }
            else {
                this.checkClosed();
                final BindValue binding = this.getBinding(parameterIndex, false);
                this.resetToType(binding, 202);
                binding.value = new TIMESTAMP(x);
            }
        }
    }
    
    @Override
    public void setTIMESTAMPLTZ(final int parameterIndex, final TIMESTAMPLTZ x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, 93);
            }
            else {
                this.checkClosed();
                final BindValue binding = this.getBinding(parameterIndex, false);
                this.resetToType(binding, 201);
                binding.value = x;
            }
        }
    }
    
    @Override
    public void setTIMESTAMPTZ(final int parameterIndex, final TIMESTAMPTZ x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, 93);
            }
            else {
                this.checkClosed();
                final BindValue binding = this.getBinding(parameterIndex, false);
                this.resetToType(binding, 200);
                binding.value = x;
            }
        }
    }
    
    @Override
    public void setINTERVALDS(final int parameterIndex, final INTERVALDS x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, 93);
            }
            else {
                this.checkClosed();
                final BindValue binding = this.getBinding(parameterIndex, false);
                this.resetToType(binding, 205);
                binding.value = x;
            }
        }
    }
    
    @Override
    public void setINTERVALYM(final int parameterIndex, final INTERVALYM x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, 93);
            }
            else {
                this.checkClosed();
                final BindValue binding = this.getBinding(parameterIndex, false);
                this.resetToType(binding, 204);
                binding.value = x;
            }
        }
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.setTimestampInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
        }
    }
    
    private void setTimestampInternal(final int parameterIndex, Timestamp x, final Calendar targetCalendar, final TimeZone tz, final boolean rollForward) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 93);
        }
        else {
            if (this.connection.getIO().isOracleMode()) {
                this.setTIMESTAMP(parameterIndex, x);
                return;
            }
            final BindValue binding = this.getBinding(parameterIndex, false);
            this.resetToType(binding, 12);
            if (!this.sendFractionalSeconds) {
                x = TimeUtil.truncateFractionalSeconds(x);
            }
            if (!this.useLegacyDatetimeCode) {
                binding.value = x;
            }
            else {
                final Calendar sessionCalendar = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew();
                binding.value = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
            }
        }
    }
    
    protected void resetToType(final BindValue oldValue, final int bufferType) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            oldValue.reset();
            if (bufferType != 6 || oldValue.bufferType == 0) {
                if (oldValue.bufferType != bufferType || bufferType == 160) {
                    this.sendTypesToServer = true;
                    oldValue.bufferType = bufferType;
                }
            }
            oldValue.isSet = true;
            oldValue.boundBeforeExecutionNum = this.numberOfExecutions;
        }
    }
    
    @Deprecated
    @Override
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        this.checkClosed();
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setURL(final int parameterIndex, final URL x) throws SQLException {
        this.checkClosed();
        this.setString(parameterIndex, x.toString());
    }
    
    private void storeBinding(final Buffer packet, final BindValue bindValue, final MysqlIO mysql) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            try {
                final Object value = bindValue.value;
                switch (bindValue.bufferType) {
                    case 1: {
                        packet.writeByte((byte)bindValue.longBinding);
                    }
                    case 2: {
                        packet.ensureCapacity(2);
                        packet.writeInt((int)bindValue.longBinding);
                    }
                    case 3: {
                        packet.ensureCapacity(4);
                        packet.writeLong((int)bindValue.longBinding);
                    }
                    case 8: {
                        packet.ensureCapacity(8);
                        packet.writeLongLong(bindValue.longBinding);
                    }
                    case 4: {
                        packet.ensureCapacity(4);
                        packet.writeFloat(bindValue.floatBinding);
                    }
                    case 5: {
                        packet.ensureCapacity(8);
                        packet.writeDouble(bindValue.doubleBinding);
                    }
                    case 11: {
                        this.storeTime(packet, (Time)value);
                    }
                    case 7:
                    case 10:
                    case 12: {
                        this.storeDateTime(packet, (Date)value, mysql, bindValue.bufferType);
                    }
                    case 200:
                    case 201:
                    case 202: {
                        this.storeOracleTIMESTAMP(packet, value, mysql, bindValue.bufferType);
                    }
                    case 204:
                    case 205: {
                        this.storeOracleINTERVALXX(packet, value, mysql, bindValue.bufferType);
                        break;
                    }
                    case 0:
                    case 15:
                    case 209:
                    case 210:
                    case 211:
                    case 246:
                    case 253:
                    case 254: {
                        if (value instanceof byte[]) {
                            packet.writeLenBytes((byte[])value);
                        }
                        else if (!this.isLoadDataQuery) {
                            packet.writeLenString((String)value, this.charEncoding, this.connection.getServerCharset(), this.charConverter, this.connection.parserKnowsUnicode(), this.connection);
                        }
                        else {
                            packet.writeLenBytes(StringUtils.getBytes((String)value));
                        }
                    }
                    case 160: {
                        this.storeComplexData(packet, value, mysql);
                    }
                }
            }
            catch (UnsupportedEncodingException uEE) {
                throw SQLError.createSQLException(Messages.getString("ServerPreparedStatement.22") + this.connection.getEncoding() + "'", "S1000", this.getExceptionInterceptor());
            }
        }
    }
    
    private void storeDateTime412AndOlder(final Buffer intoBuf, final Date dt, final int bufferType) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            Calendar sessionCalendar = null;
            if (!this.useLegacyDatetimeCode) {
                if (bufferType == 10) {
                    sessionCalendar = this.getDefaultTzCalendar();
                }
                else {
                    sessionCalendar = this.getServerTzCalendar();
                }
            }
            else {
                sessionCalendar = ((dt instanceof Timestamp && this.connection.getUseJDBCCompliantTimezoneShift()) ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew());
            }
            final Date oldTime = sessionCalendar.getTime();
            try {
                intoBuf.ensureCapacity(8);
                intoBuf.writeByte((byte)7);
                sessionCalendar.setTime(dt);
                final int year = sessionCalendar.get(1);
                final int month = sessionCalendar.get(2) + 1;
                final int date = sessionCalendar.get(5);
                intoBuf.writeInt(year);
                intoBuf.writeByte((byte)month);
                intoBuf.writeByte((byte)date);
                if (dt instanceof java.sql.Date) {
                    intoBuf.writeByte((byte)0);
                    intoBuf.writeByte((byte)0);
                    intoBuf.writeByte((byte)0);
                }
                else {
                    intoBuf.writeByte((byte)sessionCalendar.get(11));
                    intoBuf.writeByte((byte)sessionCalendar.get(12));
                    intoBuf.writeByte((byte)sessionCalendar.get(13));
                }
            }
            finally {
                sessionCalendar.setTime(oldTime);
            }
        }
    }
    
    private void storeDateTime(final Buffer intoBuf, final Date dt, final MysqlIO mysql, final int bufferType) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.connection.versionMeetsMinimum(4, 1, 3)) {
                this.storeDateTime413AndNewer(intoBuf, dt, bufferType);
            }
            else {
                this.storeDateTime412AndOlder(intoBuf, dt, bufferType);
            }
        }
    }
    
    private void storeOracleINTERVALXX(final Buffer intoBuf, final Object value, final MysqlIO mysql, final int bufferType) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            int length = 0;
            byte[] data;
            if (value instanceof INTERVALDS) {
                data = ((INTERVALDS)value).getBytes();
            }
            else {
                if (!(value instanceof INTERVALYM)) {
                    throw new SQLException("Unknown type");
                }
                data = ((INTERVALYM)value).getBytes();
            }
            length += data.length;
            intoBuf.ensureCapacity(length);
            intoBuf.writeByte((byte)length);
            intoBuf.writeBytesNoNull(data, 0, length);
        }
    }
    
    private void storeOracleTIMESTAMP(final Buffer intoBuf, final Object value, final MysqlIO mysql, final int bufferType) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            int length = 0;
            byte[] data;
            if (value instanceof TIMESTAMP) {
                data = ((TIMESTAMP)value).getBytes();
            }
            else if (value instanceof TIMESTAMPLTZ) {
                data = ((TIMESTAMPLTZ)value).getBytes();
            }
            else {
                if (!(value instanceof TIMESTAMPTZ)) {
                    throw new SQLException("Unknown TIMESTAMP");
                }
                data = ((TIMESTAMPTZ)value).getBytes();
                length += 2;
            }
            length += data.length;
            intoBuf.ensureCapacity(length);
            intoBuf.writeByte((byte)length);
            intoBuf.writeBytesNoNull(data, 0, 11);
            intoBuf.writeByte((byte)9);
            if (bufferType == 200) {
                intoBuf.writeBytesNoNull(data, 12, length - 14);
                intoBuf.writeByte((byte)0);
                intoBuf.writeByte((byte)0);
            }
        }
    }
    
    private void storeComplexData(final Buffer intoBuf, final Object value, final MysqlIO mysql) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            ObComplexData data = null;
            if (value instanceof ObArray) {
                data = (ObComplexData)value;
                this.storeComplexArray(intoBuf, data, mysql);
            }
            else {
                if (!(value instanceof ObStruct)) {
                    throw new SQLException("unknown complex data, which is not array or struct");
                }
                data = (ObComplexData)value;
                this.storeComplexStruct(intoBuf, data, mysql);
            }
        }
    }
    
    private void storeComplexArray(final Buffer intoBuf, final ObComplexData data, final MysqlIO mysql) throws SQLException {
        intoBuf.writeFieldLength(data.getAttrCount());
        final int nullCount = (data.getAttrCount() + 7) / 8;
        final int nullBitsPosition = intoBuf.getPosition();
        for (int i = 0; i < nullCount; ++i) {
            intoBuf.writeByte((byte)0);
        }
        final byte[] nullBitsBuffer = new byte[nullCount];
        for (int j = 0; j < data.getAttrCount(); ++j) {
            if (null != data.getAttrData(j)) {
                this.storeComplexAttrData(intoBuf, data.getComplexType().getAttrType(0), data.getAttrData(j), mysql);
            }
            else {
                final byte[] array = nullBitsBuffer;
                final int n = j / 8;
                array[n] |= (byte)(1 << j % 8);
            }
        }
        final int endPosition = intoBuf.getPosition();
        intoBuf.setPosition(nullBitsPosition);
        intoBuf.writeBytesNoNull(nullBitsBuffer);
        intoBuf.setPosition(endPosition);
    }
    
    private void storeComplexStruct(final Buffer intoBuf, final ObComplexData data, final MysqlIO mysql) throws SQLException {
        final int nullCount = (data.getAttrCount() + 7) / 8;
        final int nullBitsPosition = intoBuf.getPosition();
        for (int i = 0; i < nullCount; ++i) {
            intoBuf.writeByte((byte)0);
        }
        final byte[] nullBitsBuffer = new byte[nullCount];
        for (int j = 0; j < data.getAttrCount(); ++j) {
            if (null != data.getAttrData(j)) {
                this.storeComplexAttrData(intoBuf, data.getComplexType().getAttrType(j), data.getAttrData(j), mysql);
            }
            else {
                final byte[] array = nullBitsBuffer;
                final int n = j / 8;
                array[n] |= (byte)(1 << j % 8);
            }
        }
        final int endPosition = intoBuf.getPosition();
        intoBuf.setPosition(nullBitsPosition);
        intoBuf.writeBytesNoNull(nullBitsBuffer);
        intoBuf.setPosition(endPosition);
    }
    
    private void storeComplexAttrData(final Buffer intoBuf, final ComplexDataType type, final Object value, final MysqlIO mysql) throws SQLException {
        switch (type.getType()) {
            case 4: {
                this.storeComplexArray(intoBuf, (ObComplexData)value, mysql);
            }
            case 3: {
                this.storeComplexStruct(intoBuf, (ObComplexData)value, mysql);
            }
            case 0: {
                final String valueStr = String.valueOf(value);
                intoBuf.writeLenBytes(StringUtils.getBytes(valueStr));
            }
            case 2: {
                this.storeDateTime(intoBuf, (Date)value, mysql, 12);
            }
            case 1:
            case 6: {
                if (value instanceof byte[]) {
                    intoBuf.writeLenBytes((byte[])value);
                }
                else {
                    intoBuf.writeLenBytes(StringUtils.getBytes((String)value));
                }
            }
            default: {
                throw new SQLException("unsupported complex data type");
            }
        }
    }
    
    private void storeDateTime413AndNewer(final Buffer intoBuf, final Date dt, final int bufferType) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            Calendar sessionCalendar = null;
            if (!this.useLegacyDatetimeCode) {
                if (bufferType == 10) {
                    sessionCalendar = this.getDefaultTzCalendar();
                }
                else {
                    sessionCalendar = this.getServerTzCalendar();
                }
            }
            else {
                sessionCalendar = ((dt instanceof Timestamp && this.connection.getUseJDBCCompliantTimezoneShift()) ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew());
            }
            final Date oldTime = sessionCalendar.getTime();
            try {
                sessionCalendar.setTime(dt);
                if (dt instanceof java.sql.Date) {
                    sessionCalendar.set(11, 0);
                    sessionCalendar.set(12, 0);
                    sessionCalendar.set(13, 0);
                }
                byte length = 7;
                if (dt instanceof Timestamp) {
                    length = 11;
                }
                intoBuf.ensureCapacity(length);
                intoBuf.writeByte(length);
                final int year = sessionCalendar.get(1);
                final int month = sessionCalendar.get(2) + 1;
                final int date = sessionCalendar.get(5);
                intoBuf.writeInt(year);
                intoBuf.writeByte((byte)month);
                intoBuf.writeByte((byte)date);
                if (dt instanceof java.sql.Date) {
                    intoBuf.writeByte((byte)0);
                    intoBuf.writeByte((byte)0);
                    intoBuf.writeByte((byte)0);
                }
                else {
                    intoBuf.writeByte((byte)sessionCalendar.get(11));
                    intoBuf.writeByte((byte)sessionCalendar.get(12));
                    intoBuf.writeByte((byte)sessionCalendar.get(13));
                }
                if (length == 11) {
                    intoBuf.writeLong(((Timestamp)dt).getNanos() / 1000);
                }
            }
            finally {
                sessionCalendar.setTime(oldTime);
            }
        }
    }
    
    private Calendar getServerTzCalendar() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.serverTzCalendar == null) {
                this.serverTzCalendar = new GregorianCalendar(this.connection.getServerTimezoneTZ());
            }
            return this.serverTzCalendar;
        }
    }
    
    private Calendar getDefaultTzCalendar() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.defaultTzCalendar == null) {
                this.defaultTzCalendar = new GregorianCalendar(TimeZone.getDefault());
            }
            return this.defaultTzCalendar;
        }
    }
    
    private void storeReader(final MysqlIO mysql, final int parameterIndex, final Buffer packet, final Reader inStream) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final String forcedEncoding = this.connection.getClobCharacterEncoding();
            final String clobEncoding = (forcedEncoding == null) ? this.connection.getEncoding() : forcedEncoding;
            int maxBytesChar = 2;
            if (clobEncoding != null) {
                if (!clobEncoding.equals("UTF-16")) {
                    maxBytesChar = this.connection.getMaxBytesPerChar(clobEncoding);
                    if (maxBytesChar == 1) {
                        maxBytesChar = 2;
                    }
                }
                else {
                    maxBytesChar = 4;
                }
            }
            final char[] buf = new char[8192 / maxBytesChar];
            int numRead = 0;
            int bytesInPacket = 0;
            int totalBytesRead = 0;
            int bytesReadAtLastSend = 0;
            final int packetIsFullAt = this.connection.getBlobSendChunkSize();
            try {
                packet.clear();
                packet.writeByte((byte)24);
                packet.writeLong(this.serverStatementId);
                packet.writeInt(parameterIndex);
                boolean readAny = false;
                while ((numRead = inStream.read(buf)) != -1) {
                    readAny = true;
                    final byte[] valueAsBytes = StringUtils.getBytes(buf, null, clobEncoding, this.connection.getServerCharset(), 0, numRead, this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                    packet.writeBytesNoNull(valueAsBytes, 0, valueAsBytes.length);
                    bytesInPacket += valueAsBytes.length;
                    totalBytesRead += valueAsBytes.length;
                    if (bytesInPacket >= packetIsFullAt) {
                        bytesReadAtLastSend = totalBytesRead;
                        mysql.sendCommand(24, null, packet, true, null, 0);
                        bytesInPacket = 0;
                        packet.clear();
                        packet.writeByte((byte)24);
                        packet.writeLong(this.serverStatementId);
                        packet.writeInt(parameterIndex);
                    }
                }
                if (totalBytesRead != bytesReadAtLastSend) {
                    mysql.sendCommand(24, null, packet, true, null, 0);
                }
                if (!readAny) {
                    mysql.sendCommand(24, null, packet, true, null, 0);
                }
            }
            catch (IOException ioEx) {
                final SQLException sqlEx = SQLError.createSQLException(Messages.getString("ServerPreparedStatement.24") + ioEx.toString(), "S1000", this.getExceptionInterceptor());
                sqlEx.initCause(ioEx);
                throw sqlEx;
            }
            finally {
                if (this.connection.getAutoClosePStmtStreams() && inStream != null) {
                    try {
                        inStream.close();
                    }
                    catch (IOException ex) {}
                }
            }
        }
    }
    
    private void storeStream(final MysqlIO mysql, final int parameterIndex, final Buffer packet, final InputStream inStream) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final byte[] buf = new byte[8192];
            int numRead = 0;
            try {
                int bytesInPacket = 0;
                int totalBytesRead = 0;
                int bytesReadAtLastSend = 0;
                final int packetIsFullAt = this.connection.getBlobSendChunkSize();
                packet.clear();
                packet.writeByte((byte)24);
                packet.writeLong(this.serverStatementId);
                packet.writeInt(parameterIndex);
                boolean readAny = false;
                while ((numRead = inStream.read(buf)) != -1) {
                    readAny = true;
                    packet.writeBytesNoNull(buf, 0, numRead);
                    bytesInPacket += numRead;
                    totalBytesRead += numRead;
                    if (bytesInPacket >= packetIsFullAt) {
                        bytesReadAtLastSend = totalBytesRead;
                        mysql.sendCommand(24, null, packet, true, null, 0);
                        bytesInPacket = 0;
                        packet.clear();
                        packet.writeByte((byte)24);
                        packet.writeLong(this.serverStatementId);
                        packet.writeInt(parameterIndex);
                    }
                }
                if (totalBytesRead != bytesReadAtLastSend) {
                    mysql.sendCommand(24, null, packet, true, null, 0);
                }
                if (!readAny) {
                    mysql.sendCommand(24, null, packet, true, null, 0);
                }
            }
            catch (IOException ioEx) {
                final SQLException sqlEx = SQLError.createSQLException(Messages.getString("ServerPreparedStatement.25") + ioEx.toString(), "S1000", this.getExceptionInterceptor());
                sqlEx.initCause(ioEx);
                throw sqlEx;
            }
            finally {
                if (this.connection.getAutoClosePStmtStreams() && inStream != null) {
                    try {
                        inStream.close();
                    }
                    catch (IOException ex) {}
                }
            }
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder toStringBuf = new StringBuilder();
        toStringBuf.append("com.alipay.oceanbase.jdbc.ServerPreparedStatement[");
        toStringBuf.append(this.serverStatementId);
        toStringBuf.append("] - ");
        try {
            toStringBuf.append(this.asSql());
        }
        catch (SQLException sqlEx) {
            toStringBuf.append(Messages.getString("ServerPreparedStatement.6"));
            toStringBuf.append(sqlEx);
        }
        return toStringBuf.toString();
    }
    
    public long getServerStatementId() {
        return this.serverStatementId;
    }
    
    @Override
    public boolean canRewriteAsMultiValueInsertAtSqlLevel() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (!this.hasCheckedRewrite) {
                this.hasCheckedRewrite = true;
                this.canRewrite = PreparedStatement.canRewrite(this.originalSql, this.isOnDuplicateKeyUpdate(), this.getLocationOfOnDuplicateKeyUpdate(), findStartOfStatement(this.originalSql));
                this.parseInfo = new ParseInfo(this.originalSql, this.connection, this.connection.getMetaData(), this.charEncoding, this.charConverter, this.psStats);
            }
            return this.canRewrite;
        }
    }
    
    protected static int findStartOfStatement(final String sql) {
        int statementStartPos;
        for (statementStartPos = 0; Character.isWhitespace(sql.charAt(statementStartPos)); ++statementStartPos) {}
        int originPos = statementStartPos;
        while (statementStartPos < sql.length()) {
            if (!StringUtils.startsWithIgnoreCaseAndWs(sql, "/*", statementStartPos) && !StringUtils.startsWithIgnoreCaseAndWs(sql, "--", statementStartPos) && !StringUtils.startsWithIgnoreCaseAndWs(sql, "#", statementStartPos)) {
                return statementStartPos;
            }
            originPos = statementStartPos;
            while (StringUtils.startsWithIgnoreCaseAndWs(sql, "/*", statementStartPos)) {
                statementStartPos = sql.indexOf("*/", statementStartPos);
                if (statementStartPos != -1) {
                    statementStartPos += 2;
                }
                else {
                    statementStartPos = originPos;
                }
                while (Character.isWhitespace(sql.charAt(statementStartPos))) {
                    ++statementStartPos;
                }
            }
            originPos = statementStartPos;
            while (StringUtils.startsWithIgnoreCaseAndWs(sql, "--", statementStartPos)) {
                statementStartPos = sql.indexOf(10, statementStartPos);
                if (statementStartPos == -1) {
                    statementStartPos = sql.indexOf(13, statementStartPos);
                    if (statementStartPos == -1) {
                        return originPos;
                    }
                }
                while (Character.isWhitespace(sql.charAt(statementStartPos))) {
                    ++statementStartPos;
                }
            }
            originPos = statementStartPos;
            while (StringUtils.startsWithIgnoreCaseAndWs(sql, "#", statementStartPos)) {
                statementStartPos = sql.indexOf(10, statementStartPos);
                if (statementStartPos == -1) {
                    statementStartPos = sql.indexOf(13, statementStartPos);
                    if (statementStartPos == -1) {
                        return originPos;
                    }
                }
                while (Character.isWhitespace(sql.charAt(statementStartPos))) {
                    ++statementStartPos;
                }
            }
        }
        return statementStartPos;
    }
    
    public boolean canRewriteAsMultivalueInsertStatement() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (!this.canRewriteAsMultiValueInsertAtSqlLevel()) {
                return false;
            }
            BindValue[] currentBindValues = null;
            final BindValue[] previousBindValues = null;
            for (int nbrCommands = this.batchedArgs.size(), commandIndex = 0; commandIndex < nbrCommands; ++commandIndex) {
                final Object arg = this.batchedArgs.get(commandIndex);
                if (!(arg instanceof String)) {
                    currentBindValues = ((BatchedBindValues)arg).batchedParameterValues;
                    if (previousBindValues != null) {
                        for (int j = 0; j < this.parameterBindings.length; ++j) {
                            if (currentBindValues[j].bufferType != previousBindValues[j].bufferType) {
                                return false;
                            }
                        }
                    }
                }
            }
            return true;
        }
    }
    
    @Override
    protected int getLocationOfOnDuplicateKeyUpdate() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.locationOfOnDuplicateKeyUpdate == -2) {
                this.locationOfOnDuplicateKeyUpdate = StatementImpl.getOnDuplicateKeyLocation(this.originalSql, this.connection.getDontCheckOnDuplicateKeyUpdateInSQL(), this.connection.getRewriteBatchedStatements(), this.connection.isNoBackslashEscapesSet());
            }
            return this.locationOfOnDuplicateKeyUpdate;
        }
    }
    
    protected boolean isOnDuplicateKeyUpdate() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.getLocationOfOnDuplicateKeyUpdate() != -1;
        }
    }
    
    @Override
    protected long[] computeMaxParameterSetSizeAndBatchSize(final int numBatchedArgs) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            long sizeOfEntireBatch = 10L;
            long maxSizeOfParameterSet = 0L;
            for (int i = 0; i < numBatchedArgs; ++i) {
                final BindValue[] paramArg = this.batchedArgs.get(i).batchedParameterValues;
                long sizeOfParameterSet = 0L;
                sizeOfParameterSet += (this.parameterCount + 7) / 8;
                sizeOfParameterSet += this.parameterCount * 2;
                for (int j = 0; j < this.parameterBindings.length; ++j) {
                    if (!paramArg[j].isNull) {
                        final long size = paramArg[j].getBoundLength();
                        if (paramArg[j].isLongData) {
                            if (size != -1L) {
                                sizeOfParameterSet += size;
                            }
                        }
                        else {
                            sizeOfParameterSet += size;
                        }
                    }
                }
                sizeOfEntireBatch += sizeOfParameterSet;
                if (sizeOfParameterSet > maxSizeOfParameterSet) {
                    maxSizeOfParameterSet = sizeOfParameterSet;
                }
            }
            return new long[] { maxSizeOfParameterSet, sizeOfEntireBatch };
        }
    }
    
    @Override
    protected int setOneBatchedParameterSet(final java.sql.PreparedStatement batchedStatement, int batchedParamIndex, final Object paramSet) throws SQLException {
        final BindValue[] paramArg = ((BatchedBindValues)paramSet).batchedParameterValues;
        for (int j = 0; j < paramArg.length; ++j) {
            if (paramArg[j].isNull) {
                batchedStatement.setNull(batchedParamIndex++, 0);
            }
            else if (paramArg[j].isLongData) {
                final Object value = paramArg[j].value;
                if (value instanceof InputStream) {
                    batchedStatement.setBinaryStream(batchedParamIndex++, (InputStream)value, (int)paramArg[j].bindLength);
                }
                else {
                    batchedStatement.setCharacterStream(batchedParamIndex++, (Reader)value, (int)paramArg[j].bindLength);
                }
            }
            else {
                switch (paramArg[j].bufferType) {
                    case 1: {
                        batchedStatement.setByte(batchedParamIndex++, (byte)paramArg[j].longBinding);
                        break;
                    }
                    case 2: {
                        batchedStatement.setShort(batchedParamIndex++, (short)paramArg[j].longBinding);
                        break;
                    }
                    case 3: {
                        batchedStatement.setInt(batchedParamIndex++, (int)paramArg[j].longBinding);
                        break;
                    }
                    case 8: {
                        batchedStatement.setLong(batchedParamIndex++, paramArg[j].longBinding);
                        break;
                    }
                    case 4: {
                        batchedStatement.setFloat(batchedParamIndex++, paramArg[j].floatBinding);
                        break;
                    }
                    case 5: {
                        batchedStatement.setDouble(batchedParamIndex++, paramArg[j].doubleBinding);
                        break;
                    }
                    case 11: {
                        batchedStatement.setTime(batchedParamIndex++, (Time)paramArg[j].value);
                        break;
                    }
                    case 10: {
                        batchedStatement.setDate(batchedParamIndex++, (java.sql.Date)paramArg[j].value);
                        break;
                    }
                    case 7:
                    case 12: {
                        batchedStatement.setTimestamp(batchedParamIndex++, (Timestamp)paramArg[j].value);
                        break;
                    }
                    case 202: {
                        ((ServerPreparedStatement)batchedStatement).setTIMESTAMP(batchedParamIndex++, ((TIMESTAMP)paramArg[j].value).timestampValue());
                        break;
                    }
                    case 201: {
                        ((ServerPreparedStatement)batchedStatement).setTIMESTAMPLTZ(batchedParamIndex++, (TIMESTAMPLTZ)paramArg[j].value);
                        break;
                    }
                    case 200: {
                        ((ServerPreparedStatement)batchedStatement).setTIMESTAMPTZ(batchedParamIndex++, (TIMESTAMPTZ)paramArg[j].value);
                        break;
                    }
                    case 205: {
                        ((ServerPreparedStatement)batchedStatement).setINTERVALDS(batchedParamIndex++, (INTERVALDS)paramArg[j].value);
                        break;
                    }
                    case 204: {
                        ((ServerPreparedStatement)batchedStatement).setINTERVALYM(batchedParamIndex++, (INTERVALYM)paramArg[j].value);
                        break;
                    }
                    case 0:
                    case 15:
                    case 246:
                    case 253:
                    case 254: {
                        final Object value = paramArg[j].value;
                        if (value instanceof byte[]) {
                            batchedStatement.setBytes(batchedParamIndex, (byte[])value);
                        }
                        else {
                            batchedStatement.setString(batchedParamIndex, (String)value);
                        }
                        if (batchedStatement instanceof ServerPreparedStatement) {
                            final BindValue asBound = ((ServerPreparedStatement)batchedStatement).getBinding(batchedParamIndex, false);
                            asBound.bufferType = paramArg[j].bufferType;
                        }
                        ++batchedParamIndex;
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unknown type when re-binding parameter into batched statement for parameter index " + batchedParamIndex);
                    }
                }
            }
        }
        return batchedParamIndex;
    }
    
    @Override
    protected boolean containsOnDuplicateKeyUpdateInSQL() {
        return this.hasOnDuplicateKeyUpdate;
    }
    
    @Override
    protected PreparedStatement prepareBatchedInsertSQL(final MySQLConnection localConn, final int numBatches) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            try {
                final PreparedStatement pstmt = ((Wrapper)localConn.prepareStatement(this.parseInfo.getSqlForBatch(numBatches), this.resultSetConcurrency, this.resultSetType)).unwrap(PreparedStatement.class);
                pstmt.setRetrieveGeneratedKeys(this.retrieveGeneratedKeys);
                return pstmt;
            }
            catch (UnsupportedEncodingException e) {
                final SQLException sqlEx = SQLError.createSQLException("Unable to prepare batch statement", "S1000", this.getExceptionInterceptor());
                sqlEx.initCause(e);
                throw sqlEx;
            }
        }
    }
    
    @Override
    public void setPoolable(final boolean poolable) throws SQLException {
        if (!poolable) {
            this.connection.decachePreparedStatement(this);
        }
        super.setPoolable(poolable);
    }
    
    static {
        if (Util.isJdbc4()) {
            try {
                final String jdbc4ClassName = Util.isJdbc42() ? "com.alipay.oceanbase.jdbc.JDBC42ServerPreparedStatement" : "com.alipay.oceanbase.jdbc.JDBC4ServerPreparedStatement";
                JDBC_4_SPS_CTOR = Class.forName(jdbc4ClassName).getConstructor(MySQLConnection.class, String.class, String.class, Integer.TYPE, Integer.TYPE);
                return;
            }
            catch (SecurityException e) {
                throw new RuntimeException(e);
            }
            catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            }
            catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_SPS_CTOR = null;
    }
    
    public static class BatchedBindValues
    {
        public BindValue[] batchedParameterValues;
        
        BatchedBindValues(final BindValue[] paramVals) {
            final int numParams = paramVals.length;
            this.batchedParameterValues = new BindValue[numParams];
            for (int i = 0; i < numParams; ++i) {
                this.batchedParameterValues[i] = new BindValue(paramVals[i]);
            }
        }
    }
    
    public static class BindValue
    {
        public long boundBeforeExecutionNum;
        public long bindLength;
        public int bufferType;
        public double doubleBinding;
        public float floatBinding;
        public boolean isLongData;
        public boolean isNull;
        public boolean isSet;
        public long longBinding;
        public Object value;
        
        BindValue() {
            this.boundBeforeExecutionNum = 0L;
            this.isSet = false;
        }
        
        BindValue(final BindValue copyMe) {
            this.boundBeforeExecutionNum = 0L;
            this.isSet = false;
            this.value = copyMe.value;
            this.isSet = copyMe.isSet;
            this.isLongData = copyMe.isLongData;
            this.isNull = copyMe.isNull;
            this.bufferType = copyMe.bufferType;
            this.bindLength = copyMe.bindLength;
            this.longBinding = copyMe.longBinding;
            this.floatBinding = copyMe.floatBinding;
            this.doubleBinding = copyMe.doubleBinding;
        }
        
        void reset() {
            this.isNull = false;
            this.isSet = false;
            this.value = null;
            this.isLongData = false;
            this.longBinding = 0L;
            this.floatBinding = 0.0f;
            this.doubleBinding = 0.0;
        }
        
        @Override
        public String toString() {
            return this.toString(false);
        }
        
        public String toString(final boolean quoteIfNeeded) {
            if (this.isLongData) {
                return "' STREAM DATA '";
            }
            if (this.isNull) {
                return "NULL";
            }
            switch (this.bufferType) {
                case 1:
                case 2:
                case 3:
                case 8: {
                    return String.valueOf(this.longBinding);
                }
                case 4: {
                    return String.valueOf(this.floatBinding);
                }
                case 5: {
                    return String.valueOf(this.doubleBinding);
                }
                case 7:
                case 10:
                case 11:
                case 12:
                case 15:
                case 209:
                case 253:
                case 254: {
                    if (quoteIfNeeded) {
                        return "'" + String.valueOf(this.value) + "'";
                    }
                    return String.valueOf(this.value);
                }
                default: {
                    if (this.value instanceof byte[]) {
                        return "byte data";
                    }
                    if (quoteIfNeeded) {
                        return "'" + String.valueOf(this.value) + "'";
                    }
                    return String.valueOf(this.value);
                }
            }
        }
        
        long getBoundLength() {
            if (this.isNull) {
                return 0L;
            }
            if (this.isLongData) {
                return this.bindLength;
            }
            switch (this.bufferType) {
                case 1: {
                    return 1L;
                }
                case 2: {
                    return 2L;
                }
                case 3: {
                    return 4L;
                }
                case 8: {
                    return 8L;
                }
                case 4: {
                    return 4L;
                }
                case 5: {
                    return 8L;
                }
                case 11: {
                    return 9L;
                }
                case 10: {
                    return 7L;
                }
                case 7:
                case 12: {
                    return 11L;
                }
                case 201:
                case 202: {
                    return 12L;
                }
                case 200: {
                    return 16L;
                }
                case 0:
                case 15:
                case 209:
                case 246:
                case 253:
                case 254: {
                    if (this.value instanceof byte[]) {
                        return ((byte[])this.value).length;
                    }
                    return ((String)this.value).length();
                }
                default: {
                    return 0L;
                }
            }
        }
    }
}
