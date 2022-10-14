// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.List;
import java.util.Iterator;
import java.util.LinkedList;
import java.sql.SQLXML;
import java.sql.NClob;
import java.net.URL;
import java.sql.Connection;
import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMPLTZ;
import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMP;
import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMPTZ;
import com.alipay.oceanbase.jdbc.extend.datatype.INTERVALYM;
import com.alipay.oceanbase.jdbc.extend.datatype.INTERVALDS;
import java.util.TimeZone;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.sql.Ref;
import java.text.DateFormat;
import java.text.ParsePosition;
import com.alipay.oceanbase.jdbc.jdk8.LocalTimeReflection;
import com.alipay.oceanbase.jdbc.jdk8.LocalDateTimeReflection;
import com.alipay.oceanbase.jdbc.jdk8.LocalDateReflection;
import java.sql.Time;
import java.math.BigInteger;
import java.util.Locale;
import java.sql.Timestamp;
import java.util.Calendar;
import java.sql.Clob;
import java.sql.Blob;
import java.math.BigDecimal;
import java.sql.Struct;
import java.sql.Array;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEvent;
import java.sql.RowId;
import java.sql.ParameterMetaData;
import java.io.StringReader;
import java.sql.ResultSet;
import java.util.Date;
import com.alipay.oceanbase.jdbc.exceptions.MySQLStatementCancelledException;
import com.alipay.oceanbase.jdbc.exceptions.MySQLTimeoutException;
import java.sql.Statement;
import java.util.TimerTask;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.io.IOException;
import java.io.Reader;
import com.alipay.oceanbase.jdbc.stats.PrepareStatementStats;
import java.nio.charset.CharsetEncoder;
import java.text.SimpleDateFormat;
import java.sql.ResultSetMetaData;
import java.io.InputStream;
import java.sql.DatabaseMetaData;
import java.lang.reflect.Constructor;

public class PreparedStatement extends StatementImpl implements java.sql.PreparedStatement
{
    private static final Constructor<?> JDBC_4_PSTMT_2_ARG_CTOR;
    private static final Constructor<?> JDBC_4_PSTMT_3_ARG_CTOR;
    private static final Constructor<?> JDBC_4_PSTMT_4_ARG_CTOR;
    private static final byte[] HEX_DIGITS;
    protected boolean batchHasPlainStatements;
    private DatabaseMetaData dbmd;
    protected char firstCharOfStmt;
    protected boolean isLoadDataQuery;
    protected boolean[] isNull;
    private boolean[] isStream;
    protected int numberOfExecutions;
    protected String originalSql;
    protected int parameterCount;
    protected MysqlParameterMetadata parameterMetaData;
    private InputStream[] parameterStreams;
    private byte[][] parameterValues;
    protected int[] parameterTypes;
    protected ParseInfo parseInfo;
    private ResultSetMetaData pstmtResultMetaData;
    private byte[][] staticSqlStrings;
    private byte[] streamConvertBuf;
    private int[] streamLengths;
    private SimpleDateFormat tsdf;
    private SimpleDateFormat ddf;
    private SimpleDateFormat tdf;
    protected boolean useTrueBoolean;
    protected boolean usingAnsiMode;
    protected String batchedValuesClause;
    private boolean doPingInstead;
    private boolean compensateForOnDuplicateKeyUpdate;
    private CharsetEncoder charsetEncoder;
    protected int batchCommandIndex;
    protected boolean serverSupportsFracSecs;
    protected PrepareStatementStats psStats;
    protected int rewrittenBatchSize;
    
    protected static int readFully(final Reader reader, final char[] buf, final int length) throws IOException {
        int numCharsRead;
        int count;
        for (numCharsRead = 0; numCharsRead < length; numCharsRead += count) {
            count = reader.read(buf, numCharsRead, length - numCharsRead);
            if (count < 0) {
                break;
            }
        }
        return numCharsRead;
    }
    
    protected static PreparedStatement getInstance(final MySQLConnection conn, final String catalog) throws SQLException {
        if (!Util.isJdbc4()) {
            return new PreparedStatement(conn, catalog);
        }
        return (PreparedStatement)Util.handleNewInstance(PreparedStatement.JDBC_4_PSTMT_2_ARG_CTOR, new Object[] { conn, catalog }, conn.getExceptionInterceptor());
    }
    
    protected static PreparedStatement getInstance(final MySQLConnection conn, final String sql, final String catalog) throws SQLException {
        if (!Util.isJdbc4()) {
            return new PreparedStatement(conn, sql, catalog);
        }
        return (PreparedStatement)Util.handleNewInstance(PreparedStatement.JDBC_4_PSTMT_3_ARG_CTOR, new Object[] { conn, sql, catalog }, conn.getExceptionInterceptor());
    }
    
    protected static PreparedStatement getInstance(final MySQLConnection conn, final String sql, final String catalog, final ParseInfo cachedParseInfo) throws SQLException {
        if (!Util.isJdbc4()) {
            return new PreparedStatement(conn, sql, catalog, cachedParseInfo);
        }
        return (PreparedStatement)Util.handleNewInstance(PreparedStatement.JDBC_4_PSTMT_4_ARG_CTOR, new Object[] { conn, sql, catalog, cachedParseInfo }, conn.getExceptionInterceptor());
    }
    
    public PreparedStatement(final MySQLConnection conn, final String catalog) throws SQLException {
        super(conn, catalog);
        this.batchHasPlainStatements = false;
        this.dbmd = null;
        this.firstCharOfStmt = '\0';
        this.isLoadDataQuery = false;
        this.isNull = null;
        this.isStream = null;
        this.numberOfExecutions = 0;
        this.originalSql = null;
        this.parameterStreams = null;
        this.parameterValues = null;
        this.parameterTypes = null;
        this.staticSqlStrings = null;
        this.streamConvertBuf = null;
        this.streamLengths = null;
        this.tsdf = null;
        this.useTrueBoolean = false;
        this.compensateForOnDuplicateKeyUpdate = false;
        this.batchCommandIndex = -1;
        this.psStats = new PrepareStatementStats();
        this.rewrittenBatchSize = 0;
        this.psStats.setClientPsType();
        this.detectFractionalSecondsSupport();
        this.compensateForOnDuplicateKeyUpdate = this.connection.getCompensateOnDuplicateKeyUpdateCounts();
    }
    
    protected void detectFractionalSecondsSupport() throws SQLException {
        this.serverSupportsFracSecs = (this.connection != null && this.connection.versionMeetsMinimum(5, 6, 4));
    }
    
    public PreparedStatement(final MySQLConnection conn, final String sql, final String catalog) throws SQLException {
        super(conn, catalog);
        this.batchHasPlainStatements = false;
        this.dbmd = null;
        this.firstCharOfStmt = '\0';
        this.isLoadDataQuery = false;
        this.isNull = null;
        this.isStream = null;
        this.numberOfExecutions = 0;
        this.originalSql = null;
        this.parameterStreams = null;
        this.parameterValues = null;
        this.parameterTypes = null;
        this.staticSqlStrings = null;
        this.streamConvertBuf = null;
        this.streamLengths = null;
        this.tsdf = null;
        this.useTrueBoolean = false;
        this.compensateForOnDuplicateKeyUpdate = false;
        this.batchCommandIndex = -1;
        this.psStats = new PrepareStatementStats();
        this.rewrittenBatchSize = 0;
        this.psStats.setClientPsType();
        if (sql == null) {
            throw SQLError.createSQLException(Messages.getString("PreparedStatement.0"), "S1009", this.getExceptionInterceptor());
        }
        this.detectFractionalSecondsSupport();
        this.originalSql = sql;
        if (this.originalSql.startsWith("/* ping */")) {
            this.doPingInstead = true;
        }
        else {
            this.doPingInstead = false;
        }
        this.dbmd = this.connection.getMetaData();
        this.useTrueBoolean = this.connection.versionMeetsMinimum(3, 21, 23);
        this.parseInfo = new ParseInfo(sql, this.connection, this.dbmd, this.charEncoding, this.charConverter, this.psStats);
        this.initializeFromParseInfo();
        this.compensateForOnDuplicateKeyUpdate = this.connection.getCompensateOnDuplicateKeyUpdateCounts();
        if (conn.getRequiresEscapingEncoder()) {
            this.charsetEncoder = Charset.forName(conn.getEncoding()).newEncoder();
        }
    }
    
    public PreparedStatement(final MySQLConnection conn, final String sql, final String catalog, final ParseInfo cachedParseInfo) throws SQLException {
        super(conn, catalog);
        this.batchHasPlainStatements = false;
        this.dbmd = null;
        this.firstCharOfStmt = '\0';
        this.isLoadDataQuery = false;
        this.isNull = null;
        this.isStream = null;
        this.numberOfExecutions = 0;
        this.originalSql = null;
        this.parameterStreams = null;
        this.parameterValues = null;
        this.parameterTypes = null;
        this.staticSqlStrings = null;
        this.streamConvertBuf = null;
        this.streamLengths = null;
        this.tsdf = null;
        this.useTrueBoolean = false;
        this.compensateForOnDuplicateKeyUpdate = false;
        this.batchCommandIndex = -1;
        this.psStats = new PrepareStatementStats();
        this.rewrittenBatchSize = 0;
        this.psStats.setClientPsType();
        if (sql == null) {
            throw SQLError.createSQLException(Messages.getString("PreparedStatement.1"), "S1009", this.getExceptionInterceptor());
        }
        this.detectFractionalSecondsSupport();
        this.originalSql = sql;
        this.dbmd = this.connection.getMetaData();
        this.useTrueBoolean = this.connection.versionMeetsMinimum(3, 21, 23);
        this.parseInfo = cachedParseInfo;
        this.usingAnsiMode = !this.connection.useAnsiQuotedIdentifiers();
        this.initializeFromParseInfo();
        this.compensateForOnDuplicateKeyUpdate = this.connection.getCompensateOnDuplicateKeyUpdateCounts();
        if (conn.getRequiresEscapingEncoder()) {
            this.charsetEncoder = Charset.forName(conn.getEncoding()).newEncoder();
        }
    }
    
    @Override
    public void addBatch() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.batchedArgs == null) {
                this.batchedArgs = new ArrayList<Object>();
            }
            for (int i = 0; i < this.parameterValues.length; ++i) {
                this.checkAllParametersSet(this.parameterValues[i], this.parameterStreams[i], i);
            }
            this.batchedArgs.add(new BatchParams(this.parameterValues, this.parameterStreams, this.isStream, this.streamLengths, this.isNull));
        }
    }
    
    @Override
    public void addBatch(final String sql) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.batchHasPlainStatements = true;
            super.addBatch(sql);
        }
    }
    
    public String asSql() throws SQLException {
        return this.asSql(false);
    }
    
    public String asSql(final boolean quoteStreamsAndUnknowns) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final StringBuilder buf = new StringBuilder();
            try {
                final int realParameterCount = this.parameterCount + this.getParameterIndexOffset();
                Object batchArg = null;
                if (this.batchCommandIndex != -1) {
                    batchArg = this.batchedArgs.get(this.batchCommandIndex);
                }
                for (int i = 0; i < realParameterCount; ++i) {
                    if (this.charEncoding != null) {
                        buf.append(StringUtils.toString(this.staticSqlStrings[i], this.charEncoding));
                    }
                    else {
                        buf.append(StringUtils.toString(this.staticSqlStrings[i]));
                    }
                    byte[] val = null;
                    if (batchArg != null && batchArg instanceof String) {
                        buf.append((String)batchArg);
                    }
                    else {
                        if (this.batchCommandIndex == -1) {
                            val = this.parameterValues[i];
                        }
                        else {
                            val = ((BatchParams)batchArg).parameterStrings[i];
                        }
                        boolean isStreamParam = false;
                        if (this.batchCommandIndex == -1) {
                            isStreamParam = this.isStream[i];
                        }
                        else {
                            isStreamParam = ((BatchParams)batchArg).isStream[i];
                        }
                        if (val == null && !isStreamParam) {
                            if (quoteStreamsAndUnknowns) {
                                buf.append("'");
                            }
                            buf.append("** NOT SPECIFIED **");
                            if (quoteStreamsAndUnknowns) {
                                buf.append("'");
                            }
                        }
                        else if (isStreamParam) {
                            if (quoteStreamsAndUnknowns) {
                                buf.append("'");
                            }
                            buf.append("** STREAM DATA **");
                            if (quoteStreamsAndUnknowns) {
                                buf.append("'");
                            }
                        }
                        else if (this.charConverter != null) {
                            buf.append(this.charConverter.toString(val));
                        }
                        else if (this.charEncoding != null) {
                            buf.append(new String(val, this.charEncoding));
                        }
                        else {
                            buf.append(StringUtils.toAsciiString(val));
                        }
                    }
                }
                if (this.charEncoding != null) {
                    buf.append(StringUtils.toString(this.staticSqlStrings[this.parameterCount + this.getParameterIndexOffset()], this.charEncoding));
                }
                else {
                    buf.append(StringUtils.toAsciiString(this.staticSqlStrings[this.parameterCount + this.getParameterIndexOffset()]));
                }
            }
            catch (UnsupportedEncodingException uue) {
                throw new RuntimeException(Messages.getString("PreparedStatement.32") + this.charEncoding + Messages.getString("PreparedStatement.33"));
            }
            return buf.toString();
        }
    }
    
    @Override
    public void clearBatch() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.batchHasPlainStatements = false;
            super.clearBatch();
        }
    }
    
    @Override
    public void clearParameters() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            for (int i = 0; i < this.parameterValues.length; ++i) {
                this.parameterValues[i] = null;
                this.parameterStreams[i] = null;
                this.isStream[i] = false;
                this.isNull[i] = false;
                this.parameterTypes[i] = 0;
            }
        }
    }
    
    private final void escapeblockFast(final byte[] buf, final Buffer packet, final int size) throws SQLException {
        int lastwritten = 0;
        for (int i = 0; i < size; ++i) {
            final byte b = buf[i];
            if (b == 0) {
                if (i > lastwritten) {
                    packet.writeBytesNoNull(buf, lastwritten, i - lastwritten);
                }
                packet.writeByte((byte)92);
                packet.writeByte((byte)48);
                lastwritten = i + 1;
            }
            else if (b == 92 || b == 39 || (!this.usingAnsiMode && b == 34)) {
                if (i > lastwritten) {
                    packet.writeBytesNoNull(buf, lastwritten, i - lastwritten);
                }
                packet.writeByte((byte)92);
                lastwritten = i;
            }
        }
        if (lastwritten < size) {
            packet.writeBytesNoNull(buf, lastwritten, size - lastwritten);
        }
    }
    
    private final void escapeblockFast(final byte[] buf, final ByteArrayOutputStream bytesOut, final int size) {
        int lastwritten = 0;
        for (int i = 0; i < size; ++i) {
            final byte b = buf[i];
            if (b == 0) {
                if (i > lastwritten) {
                    bytesOut.write(buf, lastwritten, i - lastwritten);
                }
                bytesOut.write(92);
                bytesOut.write(48);
                lastwritten = i + 1;
            }
            else if (b == 92 || b == 39 || (!this.usingAnsiMode && b == 34)) {
                if (i > lastwritten) {
                    bytesOut.write(buf, lastwritten, i - lastwritten);
                }
                bytesOut.write(92);
                lastwritten = i;
            }
        }
        if (lastwritten < size) {
            bytesOut.write(buf, lastwritten, size - lastwritten);
        }
    }
    
    protected boolean checkReadOnlySafeStatement() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.firstCharOfStmt == 'S' || !this.connection.isReadOnly();
        }
    }
    
    @Override
    public boolean execute() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final MySQLConnection locallyScopedConn = this.connection;
            if (!this.checkReadOnlySafeStatement()) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.20") + Messages.getString("PreparedStatement.21"), "S1009", this.getExceptionInterceptor());
            }
            ResultSetInternalMethods rs = null;
            CachedResultSetMetaData cachedMetadata = null;
            this.lastQueryIsOnDupKeyUpdate = false;
            if (this.retrieveGeneratedKeys) {
                this.lastQueryIsOnDupKeyUpdate = this.containsOnDuplicateKeyUpdateInSQL();
            }
            this.clearWarnings();
            this.setupStreamingTimeout(locallyScopedConn);
            this.batchedGeneratedKeys = null;
            final Buffer sendPacket = this.fillSendPacket();
            String oldCatalog = null;
            if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                oldCatalog = locallyScopedConn.getCatalog();
                locallyScopedConn.setCatalog(this.currentCatalog);
            }
            if (locallyScopedConn.getCacheResultSetMetadata()) {
                cachedMetadata = locallyScopedConn.getCachedMetaData(this.originalSql);
            }
            Field[] metadataFromCache = null;
            if (cachedMetadata != null) {
                metadataFromCache = cachedMetadata.fields;
            }
            boolean oldInfoMsgState = false;
            if (this.retrieveGeneratedKeys) {
                oldInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
                locallyScopedConn.setReadInfoMsgEnabled(true);
            }
            locallyScopedConn.setSessionMaxRows((this.firstCharOfStmt == 'S') ? this.maxRows : -1);
            rs = this.executeInternal(this.maxRows, sendPacket, this.createStreamingResultSet(), this.firstCharOfStmt == 'S', metadataFromCache, false);
            if (cachedMetadata != null) {
                locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, cachedMetadata, rs);
            }
            else if (rs.reallyResult() && locallyScopedConn.getCacheResultSetMetadata()) {
                locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, null, rs);
            }
            if (this.retrieveGeneratedKeys) {
                locallyScopedConn.setReadInfoMsgEnabled(oldInfoMsgState);
                rs.setFirstCharOfQuery(this.firstCharOfStmt);
            }
            if (oldCatalog != null) {
                locallyScopedConn.setCatalog(oldCatalog);
            }
            if (rs != null) {
                this.lastInsertId = rs.getUpdateID();
                this.results = rs;
            }
            return rs != null && rs.reallyResult();
        }
    }
    
    @Override
    protected long[] executeBatchInternal() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.connection.isReadOnly()) {
                throw new SQLException(Messages.getString("PreparedStatement.25") + Messages.getString("PreparedStatement.26"), "S1009");
            }
            if (this.batchedArgs == null || this.batchedArgs.size() == 0) {
                return new long[0];
            }
            final int batchTimeout = this.timeoutInMillis;
            this.timeoutInMillis = 0;
            this.resetCancelledState();
            try {
                this.statementBegins();
                this.clearWarnings();
                if (!this.batchHasPlainStatements && this.connection.getRewriteBatchedStatements()) {
                    if (this.canRewriteAsMultiValueInsertAtSqlLevel()) {
                        return this.executeBatchedInserts(batchTimeout);
                    }
                    if (this.connection.versionMeetsMinimum(4, 1, 0) && !this.batchHasPlainStatements && this.batchedArgs != null && this.batchedArgs.size() > 3 && !this.connection.getIO().isOracleMode()) {
                        return this.executePreparedBatchAsMultiStatement(batchTimeout);
                    }
                }
                return this.executeBatchSerially(batchTimeout);
            }
            finally {
                this.statementExecuting.set(false);
                this.clearBatch();
            }
        }
    }
    
    public boolean canRewriteAsMultiValueInsertAtSqlLevel() throws SQLException {
        return this.parseInfo.canRewriteAsMultiValueInsert;
    }
    
    protected int getLocationOfOnDuplicateKeyUpdate() throws SQLException {
        return this.parseInfo.locationOfOnDuplicateKeyUpdate;
    }
    
    protected long[] executePreparedBatchAsMultiStatement(final int batchTimeout) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.batchedValuesClause == null) {
                this.batchedValuesClause = this.originalSql + ";";
            }
            final MySQLConnection locallyScopedConn = this.connection;
            final boolean multiQueriesEnabled = locallyScopedConn.getAllowMultiQueries();
            CancelTask timeoutTask = null;
            try {
                this.clearWarnings();
                final int numBatchedArgs = this.batchedArgs.size();
                if (this.retrieveGeneratedKeys) {
                    this.batchedGeneratedKeys = new ArrayList<ResultSetRow>(numBatchedArgs);
                }
                int numValuesPerBatch = this.computeBatchSize(numBatchedArgs);
                if (numBatchedArgs < numValuesPerBatch) {
                    numValuesPerBatch = numBatchedArgs;
                }
                java.sql.PreparedStatement batchedStatement = null;
                int batchedParamIndex = 1;
                int numberToExecuteAsMultiValue = 0;
                int batchCounter = 0;
                int updateCountCounter = 0;
                final long[] updateCounts = new long[numBatchedArgs];
                SQLException sqlEx = null;
                try {
                    if (!multiQueriesEnabled) {
                        locallyScopedConn.getIO().enableMultiQueries();
                    }
                    if (this.retrieveGeneratedKeys) {
                        batchedStatement = ((Wrapper)locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch), 1)).unwrap(java.sql.PreparedStatement.class);
                    }
                    else {
                        batchedStatement = ((Wrapper)locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch))).unwrap(java.sql.PreparedStatement.class);
                    }
                    if (locallyScopedConn.getEnableQueryTimeouts() && batchTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                        timeoutTask = new CancelTask((StatementImpl)batchedStatement);
                        locallyScopedConn.getCancelTimer().schedule(timeoutTask, batchTimeout);
                    }
                    if (numBatchedArgs < numValuesPerBatch) {
                        numberToExecuteAsMultiValue = numBatchedArgs;
                    }
                    else {
                        numberToExecuteAsMultiValue = numBatchedArgs / numValuesPerBatch;
                    }
                    for (int numberArgsToExecute = numberToExecuteAsMultiValue * numValuesPerBatch, i = 0; i < numberArgsToExecute; ++i) {
                        if (i != 0 && i % numValuesPerBatch == 0) {
                            try {
                                batchedStatement.execute();
                            }
                            catch (SQLException ex) {
                                sqlEx = this.handleExceptionForBatch(batchCounter, numValuesPerBatch, updateCounts, ex);
                            }
                            updateCountCounter = this.processMultiCountsAndKeys((StatementImpl)batchedStatement, updateCountCounter, updateCounts);
                            batchedStatement.clearParameters();
                            batchedParamIndex = 1;
                        }
                        batchedParamIndex = this.setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.batchedArgs.get(batchCounter++));
                    }
                    try {
                        batchedStatement.execute();
                    }
                    catch (SQLException ex2) {
                        sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex2);
                    }
                    updateCountCounter = this.processMultiCountsAndKeys((StatementImpl)batchedStatement, updateCountCounter, updateCounts);
                    batchedStatement.clearParameters();
                    numValuesPerBatch = numBatchedArgs - batchCounter;
                }
                finally {
                    if (batchedStatement != null) {
                        batchedStatement.close();
                        batchedStatement = null;
                    }
                }
                try {
                    if (numValuesPerBatch > 0) {
                        if (this.retrieveGeneratedKeys) {
                            batchedStatement = locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch), 1);
                        }
                        else {
                            batchedStatement = locallyScopedConn.prepareStatement(this.generateMultiStatementForBatch(numValuesPerBatch));
                        }
                        if (timeoutTask != null) {
                            timeoutTask.toCancel = (StatementImpl)batchedStatement;
                        }
                        for (batchedParamIndex = 1; batchCounter < numBatchedArgs; batchedParamIndex = this.setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.batchedArgs.get(batchCounter++))) {}
                        try {
                            batchedStatement.execute();
                        }
                        catch (SQLException ex3) {
                            sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex3);
                        }
                        updateCountCounter = this.processMultiCountsAndKeys((StatementImpl)batchedStatement, updateCountCounter, updateCounts);
                        batchedStatement.clearParameters();
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
                    return updateCounts;
                }
                finally {
                    if (batchedStatement != null) {
                        batchedStatement.close();
                    }
                }
            }
            finally {
                if (timeoutTask != null) {
                    timeoutTask.cancel();
                    locallyScopedConn.getCancelTimer().purge();
                }
                this.resetCancelledState();
                if (!multiQueriesEnabled) {
                    locallyScopedConn.getIO().disableMultiQueries();
                }
                this.clearBatch();
            }
        }
    }
    
    private String generateMultiStatementForBatch(final int numBatches) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final StringBuilder newStatementSql = new StringBuilder((this.originalSql.length() + 1) * numBatches);
            newStatementSql.append(this.originalSql);
            for (int i = 0; i < numBatches - 1; ++i) {
                newStatementSql.append(';');
                newStatementSql.append(this.originalSql);
            }
            return newStatementSql.toString();
        }
    }
    
    protected long[] executeBatchedInserts(final int batchTimeout) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final String valuesClause = this.getValuesClause();
            final MySQLConnection locallyScopedConn = this.connection;
            if (valuesClause == null) {
                return this.executeBatchSerially(batchTimeout);
            }
            final int numBatchedArgs = this.batchedArgs.size();
            if (this.retrieveGeneratedKeys) {
                this.batchedGeneratedKeys = new ArrayList<ResultSetRow>(numBatchedArgs);
            }
            int numValuesPerBatch = this.computeBatchSize(numBatchedArgs);
            if (numBatchedArgs < numValuesPerBatch) {
                numValuesPerBatch = numBatchedArgs;
            }
            PreparedStatement batchedStatement = null;
            int batchedParamIndex = 1;
            long updateCountRunningTotal = 0L;
            int numberToExecuteAsMultiValue = 0;
            int batchCounter = 0;
            CancelTask timeoutTask = null;
            SQLException sqlEx = null;
            final long[] updateCounts = new long[numBatchedArgs];
            try {
                try {
                    batchedStatement = this.prepareBatchedInsertSQL(locallyScopedConn, numValuesPerBatch);
                    if (locallyScopedConn.getEnableQueryTimeouts() && batchTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                        timeoutTask = new CancelTask(batchedStatement);
                        locallyScopedConn.getCancelTimer().schedule(timeoutTask, batchTimeout);
                    }
                    if (numBatchedArgs < numValuesPerBatch) {
                        numberToExecuteAsMultiValue = numBatchedArgs;
                    }
                    else {
                        numberToExecuteAsMultiValue = numBatchedArgs / numValuesPerBatch;
                    }
                    for (int numberArgsToExecute = numberToExecuteAsMultiValue * numValuesPerBatch, i = 0; i < numberArgsToExecute; ++i) {
                        if (i != 0 && i % numValuesPerBatch == 0) {
                            try {
                                updateCountRunningTotal += batchedStatement.executeLargeUpdate();
                            }
                            catch (SQLException ex) {
                                sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex);
                            }
                            this.getBatchedGeneratedKeys(batchedStatement);
                            batchedStatement.clearParameters();
                            batchedParamIndex = 1;
                        }
                        batchedParamIndex = this.setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.batchedArgs.get(batchCounter++));
                    }
                    try {
                        updateCountRunningTotal += batchedStatement.executeLargeUpdate();
                    }
                    catch (SQLException ex2) {
                        sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex2);
                    }
                    this.getBatchedGeneratedKeys(batchedStatement);
                    numValuesPerBatch = numBatchedArgs - batchCounter;
                }
                finally {
                    if (batchedStatement != null) {
                        batchedStatement.close();
                        batchedStatement = null;
                    }
                }
                try {
                    if (numValuesPerBatch > 0) {
                        batchedStatement = this.prepareBatchedInsertSQL(locallyScopedConn, numValuesPerBatch);
                        if (timeoutTask != null) {
                            timeoutTask.toCancel = batchedStatement;
                        }
                        for (batchedParamIndex = 1; batchCounter < numBatchedArgs; batchedParamIndex = this.setOneBatchedParameterSet(batchedStatement, batchedParamIndex, this.batchedArgs.get(batchCounter++))) {}
                        try {
                            updateCountRunningTotal += batchedStatement.executeLargeUpdate();
                        }
                        catch (SQLException ex3) {
                            sqlEx = this.handleExceptionForBatch(batchCounter - 1, numValuesPerBatch, updateCounts, ex3);
                        }
                        this.getBatchedGeneratedKeys(batchedStatement);
                    }
                    if (sqlEx != null) {
                        throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
                    }
                    if (numBatchedArgs > 1) {
                        final long updCount = (updateCountRunningTotal > 0L) ? -2L : 0L;
                        for (int j = 0; j < numBatchedArgs; ++j) {
                            updateCounts[j] = updCount;
                        }
                    }
                    else {
                        updateCounts[0] = updateCountRunningTotal;
                    }
                    return updateCounts;
                }
                finally {
                    if (batchedStatement != null) {
                        batchedStatement.close();
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
        }
    }
    
    protected String getValuesClause() throws SQLException {
        return this.parseInfo.valuesClause;
    }
    
    protected int computeBatchSize(final int numBatchedArgs) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final long[] combinedValues = this.computeMaxParameterSetSizeAndBatchSize(numBatchedArgs);
            final long maxSizeOfParameterSet = combinedValues[0];
            final long sizeOfEntireBatch = combinedValues[1];
            final int maxAllowedPacket = this.connection.getMaxAllowedPacket();
            if (sizeOfEntireBatch < maxAllowedPacket - this.originalSql.length()) {
                return numBatchedArgs;
            }
            return (int)Math.max(1L, (maxAllowedPacket - this.originalSql.length()) / maxSizeOfParameterSet);
        }
    }
    
    protected long[] computeMaxParameterSetSizeAndBatchSize(final int numBatchedArgs) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            long sizeOfEntireBatch = 0L;
            long maxSizeOfParameterSet = 0L;
            for (int i = 0; i < numBatchedArgs; ++i) {
                final BatchParams paramArg = this.batchedArgs.get(i);
                final boolean[] isNullBatch = paramArg.isNull;
                final boolean[] isStreamBatch = paramArg.isStream;
                long sizeOfParameterSet = 0L;
                for (int j = 0; j < isNullBatch.length; ++j) {
                    if (!isNullBatch[j]) {
                        if (isStreamBatch[j]) {
                            final int streamLength = paramArg.streamLengths[j];
                            if (streamLength != -1) {
                                sizeOfParameterSet += streamLength * 2;
                            }
                            else {
                                final int paramLength = paramArg.parameterStrings[j].length;
                                sizeOfParameterSet += paramLength;
                            }
                        }
                        else {
                            sizeOfParameterSet += paramArg.parameterStrings[j].length;
                        }
                    }
                    else {
                        sizeOfParameterSet += 4L;
                    }
                }
                if (this.getValuesClause() != null) {
                    sizeOfParameterSet += this.getValuesClause().length() + 1;
                }
                else {
                    sizeOfParameterSet += this.originalSql.length() + 1;
                }
                sizeOfEntireBatch += sizeOfParameterSet;
                if (sizeOfParameterSet > maxSizeOfParameterSet) {
                    maxSizeOfParameterSet = sizeOfParameterSet;
                }
            }
            return new long[] { maxSizeOfParameterSet, sizeOfEntireBatch };
        }
    }
    
    protected long[] executeBatchSerially(final int batchTimeout) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final MySQLConnection locallyScopedConn = this.connection;
            if (locallyScopedConn == null) {
                this.checkClosed();
            }
            long[] updateCounts = null;
            if (this.batchedArgs != null) {
                final int nbrCommands = this.batchedArgs.size();
                updateCounts = new long[nbrCommands];
                for (int i = 0; i < nbrCommands; ++i) {
                    updateCounts[i] = -3L;
                }
                SQLException sqlEx = null;
                CancelTask timeoutTask = null;
                try {
                    if (locallyScopedConn.getEnableQueryTimeouts() && batchTimeout != 0 && locallyScopedConn.versionMeetsMinimum(5, 0, 0)) {
                        timeoutTask = new CancelTask(this);
                        locallyScopedConn.getCancelTimer().schedule(timeoutTask, batchTimeout);
                    }
                    if (this.retrieveGeneratedKeys) {
                        this.batchedGeneratedKeys = new ArrayList<ResultSetRow>(nbrCommands);
                    }
                    this.batchCommandIndex = 0;
                    while (this.batchCommandIndex < nbrCommands) {
                        final Object arg = this.batchedArgs.get(this.batchCommandIndex);
                        try {
                            if (arg instanceof String) {
                                updateCounts[this.batchCommandIndex] = this.executeUpdateInternal((String)arg, true, this.retrieveGeneratedKeys);
                                this.getBatchedGeneratedKeys((this.results.getFirstCharOfQuery() == 'I' && this.containsOnDuplicateKeyInString((String)arg)) ? 1 : 0);
                            }
                            else {
                                final BatchParams paramArg = (BatchParams)arg;
                                updateCounts[this.batchCommandIndex] = this.executeUpdateInternal(paramArg.parameterStrings, paramArg.parameterStreams, paramArg.isStream, paramArg.streamLengths, paramArg.isNull, true);
                                this.getBatchedGeneratedKeys(this.containsOnDuplicateKeyUpdateInSQL() ? 1 : 0);
                            }
                        }
                        catch (SQLException ex) {
                            updateCounts[this.batchCommandIndex] = -3L;
                            if (!this.continueBatchOnError || ex instanceof MySQLTimeoutException || ex instanceof MySQLStatementCancelledException || this.hasDeadlockOrTimeoutRolledBackTx(ex)) {
                                final long[] newUpdateCounts = new long[this.batchCommandIndex];
                                System.arraycopy(updateCounts, 0, newUpdateCounts, 0, this.batchCommandIndex);
                                throw SQLError.createBatchUpdateException(ex, newUpdateCounts, this.getExceptionInterceptor());
                            }
                            sqlEx = ex;
                        }
                        ++this.batchCommandIndex;
                    }
                    if (sqlEx != null) {
                        throw SQLError.createBatchUpdateException(sqlEx, updateCounts, this.getExceptionInterceptor());
                    }
                }
                catch (NullPointerException npe) {
                    try {
                        this.checkClosed();
                    }
                    catch (SQLException connectionClosedEx) {
                        updateCounts[this.batchCommandIndex] = -3L;
                        final long[] newUpdateCounts = new long[this.batchCommandIndex];
                        System.arraycopy(updateCounts, 0, newUpdateCounts, 0, this.batchCommandIndex);
                        throw SQLError.createBatchUpdateException(connectionClosedEx, newUpdateCounts, this.getExceptionInterceptor());
                    }
                    throw npe;
                }
                finally {
                    this.batchCommandIndex = -1;
                    if (timeoutTask != null) {
                        timeoutTask.cancel();
                        locallyScopedConn.getCancelTimer().purge();
                    }
                    this.resetCancelledState();
                }
            }
            return (updateCounts != null) ? updateCounts : new long[0];
        }
    }
    
    public String getDateTime(final String pattern) {
        final SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(new Date());
    }
    
    protected ResultSetInternalMethods executeInternal(final int maxRowsToRetrieve, final Buffer sendPacket, final boolean createStreamingResultSet, final boolean queryIsSelectOnly, final Field[] metadataFromCache, final boolean isBatch) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            try {
                final long exectueInternalStartNs = System.nanoTime();
                this.resetCancelledState();
                final MySQLConnection locallyScopedConnection = this.connection;
                ++this.numberOfExecutions;
                if (this.doPingInstead) {
                    this.doPingInstead();
                    return this.results;
                }
                CancelTask timeoutTask = null;
                ResultSetInternalMethods rs;
                try {
                    if (locallyScopedConnection.getEnableQueryTimeouts() && this.timeoutInMillis != 0 && locallyScopedConnection.versionMeetsMinimum(5, 0, 0)) {
                        timeoutTask = new CancelTask(this);
                        locallyScopedConnection.getCancelTimer().schedule(timeoutTask, this.timeoutInMillis);
                    }
                    if (!isBatch) {
                        this.statementBegins();
                    }
                    rs = locallyScopedConnection.execSQL(this, null, maxRowsToRetrieve, sendPacket, this.resultSetType, this.resultSetConcurrency, createStreamingResultSet, this.currentCatalog, metadataFromCache, isBatch);
                    if (timeoutTask != null) {
                        timeoutTask.cancel();
                        locallyScopedConnection.getCancelTimer().purge();
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
                }
                finally {
                    if (!isBatch) {
                        this.statementExecuting.set(false);
                    }
                    if (timeoutTask != null) {
                        timeoutTask.cancel();
                        locallyScopedConnection.getCancelTimer().purge();
                    }
                    this.psStats.addClientPSExecuteTotalCostNs(System.nanoTime() - exectueInternalStartNs);
                }
                return rs;
            }
            catch (NullPointerException npe) {
                this.checkClosed();
                throw npe;
            }
        }
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final MySQLConnection locallyScopedConn = this.connection;
            this.checkForDml(this.originalSql, this.firstCharOfStmt);
            CachedResultSetMetaData cachedMetadata = null;
            this.clearWarnings();
            this.batchedGeneratedKeys = null;
            this.setupStreamingTimeout(locallyScopedConn);
            final Buffer sendPacket = this.fillSendPacket();
            this.implicitlyCloseAllOpenResults();
            String oldCatalog = null;
            if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                oldCatalog = locallyScopedConn.getCatalog();
                locallyScopedConn.setCatalog(this.currentCatalog);
            }
            if (locallyScopedConn.getCacheResultSetMetadata()) {
                cachedMetadata = locallyScopedConn.getCachedMetaData(this.originalSql);
            }
            Field[] metadataFromCache = null;
            if (cachedMetadata != null) {
                metadataFromCache = cachedMetadata.fields;
            }
            locallyScopedConn.setSessionMaxRows(this.maxRows);
            this.results = this.executeInternal(this.maxRows, sendPacket, this.createStreamingResultSet(), true, metadataFromCache, false);
            if (oldCatalog != null) {
                locallyScopedConn.setCatalog(oldCatalog);
            }
            if (cachedMetadata != null) {
                locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, cachedMetadata, this.results);
            }
            else if (locallyScopedConn.getCacheResultSetMetadata()) {
                locallyScopedConn.initializeResultsMetadataFromCache(this.originalSql, null, this.results);
            }
            this.lastInsertId = this.results.getUpdateID();
            return this.results;
        }
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        return Util.truncateAndConvertToInt(this.executeLargeUpdate());
    }
    
    protected long executeUpdateInternal(final boolean clearBatchedGeneratedKeysAndWarnings, final boolean isBatch) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (clearBatchedGeneratedKeysAndWarnings) {
                this.clearWarnings();
                this.batchedGeneratedKeys = null;
            }
            return this.executeUpdateInternal(this.parameterValues, this.parameterStreams, this.isStream, this.streamLengths, this.isNull, isBatch);
        }
    }
    
    protected long executeUpdateInternal(final byte[][] batchedParameterStrings, final InputStream[] batchedParameterStreams, final boolean[] batchedIsStream, final int[] batchedStreamLengths, final boolean[] batchedIsNull, final boolean isReallyBatch) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final MySQLConnection locallyScopedConn = this.connection;
            if (locallyScopedConn.isReadOnly(false)) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.34") + Messages.getString("PreparedStatement.35"), "S1009", this.getExceptionInterceptor());
            }
            if (this.firstCharOfStmt == 'S' && this.isSelectQuery()) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.37"), "01S03", this.getExceptionInterceptor());
            }
            this.implicitlyCloseAllOpenResults();
            ResultSetInternalMethods rs = null;
            final Buffer sendPacket = this.fillSendPacket(batchedParameterStrings, batchedParameterStreams, batchedIsStream, batchedStreamLengths);
            String oldCatalog = null;
            if (!locallyScopedConn.getCatalog().equals(this.currentCatalog)) {
                oldCatalog = locallyScopedConn.getCatalog();
                locallyScopedConn.setCatalog(this.currentCatalog);
            }
            locallyScopedConn.setSessionMaxRows(-1);
            boolean oldInfoMsgState = false;
            if (this.retrieveGeneratedKeys) {
                oldInfoMsgState = locallyScopedConn.isReadInfoMsgEnabled();
                locallyScopedConn.setReadInfoMsgEnabled(true);
            }
            rs = this.executeInternal(-1, sendPacket, false, false, null, isReallyBatch);
            if (this.retrieveGeneratedKeys) {
                locallyScopedConn.setReadInfoMsgEnabled(oldInfoMsgState);
                rs.setFirstCharOfQuery(this.firstCharOfStmt);
            }
            if (oldCatalog != null) {
                locallyScopedConn.setCatalog(oldCatalog);
            }
            this.results = rs;
            this.updateCount = rs.getUpdateCount();
            if (this.containsOnDuplicateKeyUpdateInSQL() && this.compensateForOnDuplicateKeyUpdate && (this.updateCount == 2L || this.updateCount == 0L)) {
                this.updateCount = 1L;
            }
            this.lastInsertId = rs.getUpdateID();
            return this.updateCount;
        }
    }
    
    protected boolean containsOnDuplicateKeyUpdateInSQL() {
        return this.parseInfo.isOnDuplicateKeyUpdate;
    }
    
    protected Buffer fillSendPacket() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.fillSendPacket(this.parameterValues, this.parameterStreams, this.isStream, this.streamLengths);
        }
    }
    
    protected Buffer fillSendPacket(final byte[][] batchedParameterStrings, final InputStream[] batchedParameterStreams, final boolean[] batchedIsStream, final int[] batchedStreamLengths) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final long fillStartNs = System.nanoTime();
            final Buffer sendPacket = this.connection.getIO().getSharedSendPacket();
            sendPacket.clear();
            sendPacket.writeByte((byte)3);
            final boolean useStreamLengths = this.connection.getUseStreamLengthsInPrepStmts();
            int ensurePacketSize = 0;
            final String statementComment = this.connection.getStatementComment();
            byte[] commentAsBytes = null;
            if (statementComment != null) {
                if (this.charConverter != null) {
                    commentAsBytes = this.charConverter.toBytes(statementComment);
                }
                else {
                    commentAsBytes = StringUtils.getBytes(statementComment, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                }
                ensurePacketSize += commentAsBytes.length;
                ensurePacketSize += 6;
            }
            for (int i = 0; i < batchedParameterStrings.length; ++i) {
                if (batchedIsStream[i] && useStreamLengths) {
                    ensurePacketSize += batchedStreamLengths[i];
                }
            }
            if (ensurePacketSize != 0) {
                sendPacket.ensureCapacity(ensurePacketSize);
            }
            if (commentAsBytes != null) {
                sendPacket.writeBytesNoNull(Constants.SLASH_STAR_SPACE_AS_BYTES);
                sendPacket.writeBytesNoNull(commentAsBytes);
                sendPacket.writeBytesNoNull(Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
            }
            for (int i = 0; i < batchedParameterStrings.length; ++i) {
                this.checkAllParametersSet(batchedParameterStrings[i], batchedParameterStreams[i], i);
                sendPacket.writeBytesNoNull(this.staticSqlStrings[i]);
                if (batchedIsStream[i]) {
                    this.streamToBytes(sendPacket, batchedParameterStreams[i], true, batchedStreamLengths[i], useStreamLengths);
                }
                else {
                    sendPacket.writeBytesNoNull(batchedParameterStrings[i]);
                }
            }
            sendPacket.writeBytesNoNull(this.staticSqlStrings[batchedParameterStrings.length]);
            this.psStats.addFillSendPacketCostNs(System.nanoTime() - fillStartNs);
            return sendPacket;
        }
    }
    
    private void checkAllParametersSet(final byte[] parameterString, final InputStream parameterStream, final int columnIndex) throws SQLException {
        if (parameterString == null && parameterStream == null) {
            throw SQLError.createSQLException(Messages.getString("PreparedStatement.40") + (columnIndex + 1), "07001", this.getExceptionInterceptor());
        }
    }
    
    protected PreparedStatement prepareBatchedInsertSQL(final MySQLConnection localConn, final int numBatches) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final PreparedStatement pstmt = new PreparedStatement(localConn, "Rewritten batch of: " + this.originalSql, this.currentCatalog, this.parseInfo.getParseInfoForBatch(numBatches));
            pstmt.setRetrieveGeneratedKeys(this.retrieveGeneratedKeys);
            pstmt.rewrittenBatchSize = numBatches;
            return pstmt;
        }
    }
    
    protected void setRetrieveGeneratedKeys(final boolean flag) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.retrieveGeneratedKeys = flag;
        }
    }
    
    public int getRewrittenBatchSize() {
        return this.rewrittenBatchSize;
    }
    
    public String getNonRewrittenSql() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final int indexOfBatch = this.originalSql.indexOf(" of: ");
            if (indexOfBatch != -1) {
                return this.originalSql.substring(indexOfBatch + 5);
            }
            return this.originalSql;
        }
    }
    
    public byte[] getBytesRepresentation(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.isStream[parameterIndex]) {
                return this.streamToBytes(this.parameterStreams[parameterIndex], false, this.streamLengths[parameterIndex], this.connection.getUseStreamLengthsInPrepStmts());
            }
            final byte[] parameterVal = this.parameterValues[parameterIndex];
            if (parameterVal == null) {
                return null;
            }
            if (parameterVal[0] == 39 && parameterVal[parameterVal.length - 1] == 39) {
                final byte[] valNoQuotes = new byte[parameterVal.length - 2];
                System.arraycopy(parameterVal, 1, valNoQuotes, 0, parameterVal.length - 2);
                return valNoQuotes;
            }
            return parameterVal;
        }
    }
    
    protected byte[] getBytesRepresentationForBatch(final int parameterIndex, final int commandIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final Object batchedArg = this.batchedArgs.get(commandIndex);
            if (batchedArg instanceof String) {
                try {
                    return StringUtils.getBytes((String)batchedArg, this.charEncoding);
                }
                catch (UnsupportedEncodingException uue) {
                    throw new RuntimeException(Messages.getString("PreparedStatement.32") + this.charEncoding + Messages.getString("PreparedStatement.33"));
                }
            }
            final BatchParams params = (BatchParams)batchedArg;
            if (params.isStream[parameterIndex]) {
                return this.streamToBytes(params.parameterStreams[parameterIndex], false, params.streamLengths[parameterIndex], this.connection.getUseStreamLengthsInPrepStmts());
            }
            final byte[] parameterVal = params.parameterStrings[parameterIndex];
            if (parameterVal == null) {
                return null;
            }
            if (parameterVal[0] == 39 && parameterVal[parameterVal.length - 1] == 39) {
                final byte[] valNoQuotes = new byte[parameterVal.length - 2];
                System.arraycopy(parameterVal, 1, valNoQuotes, 0, parameterVal.length - 2);
                return valNoQuotes;
            }
            return parameterVal;
        }
    }
    
    private final String getDateTimePattern(final String dt, final boolean toTime) throws Exception {
        final int dtLength = (dt != null) ? dt.length() : 0;
        if (dtLength >= 8 && dtLength <= 10) {
            int dashCount = 0;
            boolean isDateOnly = true;
            for (int i = 0; i < dtLength; ++i) {
                final char c = dt.charAt(i);
                if (!Character.isDigit(c) && c != '-') {
                    isDateOnly = false;
                    break;
                }
                if (c == '-') {
                    ++dashCount;
                }
            }
            if (isDateOnly && dashCount == 2) {
                return "yyyy-MM-dd";
            }
        }
        boolean colonsOnly = true;
        for (int j = 0; j < dtLength; ++j) {
            final char c2 = dt.charAt(j);
            if (!Character.isDigit(c2) && c2 != ':') {
                colonsOnly = false;
                break;
            }
        }
        if (colonsOnly) {
            return "HH:mm:ss";
        }
        final StringReader reader = new StringReader(dt + " ");
        final ArrayList<Object[]> vec = new ArrayList<Object[]>();
        final ArrayList<Object[]> vecRemovelist = new ArrayList<Object[]>();
        Object[] nv = { 'y', new StringBuilder(), 0 };
        vec.add(nv);
        if (toTime) {
            nv = new Object[] { 'h', new StringBuilder(), 0 };
            vec.add(nv);
        }
        int z;
        while ((z = reader.read()) != -1) {
            final char separator = (char)z;
            for (int maxvecs = vec.size(), count = 0; count < maxvecs; ++count) {
                final Object[] v = vec.get(count);
                final int n = (int)v[2];
                char c3 = this.getSuccessor((char)v[0], n);
                if (!Character.isLetterOrDigit(separator)) {
                    if (c3 == (char)v[0] && c3 != 'S') {
                        vecRemovelist.add(v);
                    }
                    else {
                        ((StringBuilder)v[1]).append(separator);
                        if (c3 == 'X' || c3 == 'Y') {
                            v[2] = 4;
                        }
                    }
                }
                else {
                    if (c3 == 'X') {
                        c3 = 'y';
                        nv = new Object[] { 'M', new StringBuilder(((StringBuilder)v[1]).toString()).append('M'), 1 };
                        vec.add(nv);
                    }
                    else if (c3 == 'Y') {
                        c3 = 'M';
                        nv = new Object[] { 'd', new StringBuilder(((StringBuilder)v[1]).toString()).append('d'), 1 };
                        vec.add(nv);
                    }
                    ((StringBuilder)v[1]).append(c3);
                    if (c3 == (char)v[0]) {
                        v[2] = n + 1;
                    }
                    else {
                        v[0] = c3;
                        v[2] = 1;
                    }
                }
            }
            for (int size = vecRemovelist.size(), k = 0; k < size; ++k) {
                final Object[] v = vecRemovelist.get(k);
                vec.remove(v);
            }
            vecRemovelist.clear();
        }
        for (int size = vec.size(), k = 0; k < size; ++k) {
            final Object[] v = vec.get(k);
            final char c3 = (char)v[0];
            final int n = (int)v[2];
            final boolean bk = this.getSuccessor(c3, n) != c3;
            final boolean atEnd = (c3 == 's' || c3 == 'm' || (c3 == 'h' && toTime)) && bk;
            final boolean finishesAtDate = bk && c3 == 'd' && !toTime;
            final boolean containsEnd = ((StringBuilder)v[1]).toString().indexOf(87) != -1;
            if ((!atEnd && !finishesAtDate) || containsEnd) {
                vecRemovelist.add(v);
            }
        }
        for (int size = vecRemovelist.size(), k = 0; k < size; ++k) {
            vec.remove(vecRemovelist.get(k));
        }
        vecRemovelist.clear();
        final Object[] v = vec.get(0);
        final StringBuilder format = (StringBuilder)v[1];
        format.setLength(format.length() - 1);
        return format.toString();
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final long startNs = System.nanoTime();
            if (!this.isSelectQuery()) {
                return null;
            }
            PreparedStatement mdStmt = null;
            ResultSet mdRs = null;
            if (this.pstmtResultMetaData == null) {
                try {
                    mdStmt = new PreparedStatement(this.connection, this.originalSql, this.currentCatalog, this.parseInfo);
                    mdStmt.setMaxRows(1);
                    for (int paramCount = this.parameterValues.length, i = 1; i <= paramCount; ++i) {
                        mdStmt.setString(i, "");
                    }
                    final boolean hadResults = mdStmt.execute();
                    if (hadResults) {
                        mdRs = mdStmt.getResultSet();
                        this.pstmtResultMetaData = mdRs.getMetaData();
                    }
                    else {
                        this.pstmtResultMetaData = new com.alipay.oceanbase.jdbc.ResultSetMetaData(new Field[0], this.connection.getUseOldAliasMetadataBehavior(), this.connection.getYearIsDateType(), this.getExceptionInterceptor());
                    }
                }
                finally {
                    SQLException sqlExRethrow = null;
                    if (mdRs != null) {
                        try {
                            mdRs.close();
                        }
                        catch (SQLException sqlEx) {
                            sqlExRethrow = sqlEx;
                        }
                        mdRs = null;
                    }
                    if (mdStmt != null) {
                        try {
                            mdStmt.close();
                        }
                        catch (SQLException sqlEx) {
                            sqlExRethrow = sqlEx;
                        }
                        mdStmt = null;
                    }
                    if (sqlExRethrow != null) {
                        throw sqlExRethrow;
                    }
                }
                this.psStats.addGetMetaDataCostNs(System.nanoTime() - startNs);
            }
            return this.pstmtResultMetaData;
        }
    }
    
    protected boolean isSelectQuery() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return StringUtils.startsWithIgnoreCaseAndWs(StringUtils.stripComments(this.originalSql, "'\"", "'\"", true, false, true, true), "SELECT");
        }
    }
    
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.parameterMetaData == null) {
                if (this.connection.getGenerateSimpleParameterMetadata()) {
                    this.parameterMetaData = new MysqlParameterMetadata(this.parameterCount);
                }
                else {
                    this.parameterMetaData = new MysqlParameterMetadata(null, this.parameterCount, this.getExceptionInterceptor());
                }
            }
            return this.parameterMetaData;
        }
    }
    
    @Override
    public void setRowId(final int parameterIndex, final RowId x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    ParseInfo getParseInfo() {
        return this.parseInfo;
    }
    
    private final char getSuccessor(final char c, final int n) {
        return (c == 'y' && n == 2) ? 'X' : ((c == 'y' && n < 4) ? 'y' : ((c == 'y') ? 'M' : ((c == 'M' && n == 2) ? 'Y' : ((c == 'M' && n < 3) ? 'M' : ((c == 'M') ? 'd' : ((c == 'd' && n < 2) ? 'd' : ((c == 'd') ? 'H' : ((c == 'H' && n < 2) ? 'H' : ((c == 'H') ? 'm' : ((c == 'm' && n < 2) ? 'm' : ((c == 'm') ? 's' : ((c == 's' && n < 2) ? 's' : 'W'))))))))))));
    }
    
    private final void hexEscapeBlock(final byte[] buf, final Buffer packet, final int size) throws SQLException {
        for (final byte b : buf) {
            final int lowBits = (b & 0xFF) / 16;
            final int highBits = (b & 0xFF) % 16;
            packet.writeByte(PreparedStatement.HEX_DIGITS[lowBits]);
            packet.writeByte(PreparedStatement.HEX_DIGITS[highBits]);
        }
    }
    
    private void initializeFromParseInfo() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.staticSqlStrings = this.parseInfo.staticSql;
            this.isLoadDataQuery = this.parseInfo.foundLoadData;
            this.firstCharOfStmt = this.parseInfo.firstStmtChar;
            this.parameterCount = this.staticSqlStrings.length - 1;
            this.parameterValues = new byte[this.parameterCount][];
            this.parameterStreams = new InputStream[this.parameterCount];
            this.isStream = new boolean[this.parameterCount];
            this.streamLengths = new int[this.parameterCount];
            this.isNull = new boolean[this.parameterCount];
            this.parameterTypes = new int[this.parameterCount];
            this.clearParameters();
            for (int j = 0; j < this.parameterCount; ++j) {
                this.isStream[j] = false;
            }
        }
    }
    
    boolean isNull(final int paramIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.isNull[paramIndex];
        }
    }
    
    private final int readblock(final InputStream i, final byte[] b) throws SQLException {
        try {
            return i.read(b);
        }
        catch (Throwable ex) {
            final SQLException sqlEx = SQLError.createSQLException(Messages.getString("PreparedStatement.56") + ex.getClass().getName(), "S1000", this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }
    
    private final int readblock(final InputStream i, final byte[] b, final int length) throws SQLException {
        try {
            int lengthToRead = length;
            if (lengthToRead > b.length) {
                lengthToRead = b.length;
            }
            return i.read(b, 0, lengthToRead);
        }
        catch (Throwable ex) {
            final SQLException sqlEx = SQLError.createSQLException(Messages.getString("PreparedStatement.56") + ex.getClass().getName(), "S1000", this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }
    
    @Override
    protected void realClose(final boolean calledExplicitly, final boolean closeOpenResults) throws SQLException {
        final MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null) {
            return;
        }
        synchronized (locallyScopedConn.getConnectionMutex()) {
            if (this.isClosed) {
                return;
            }
            if (this.useUsageAdvisor && this.numberOfExecutions <= 1) {
                final String message = Messages.getString("PreparedStatement.43");
                this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.currentCatalog, this.connectionId, this.getId(), -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
            }
            super.realClose(calledExplicitly, closeOpenResults);
            this.dbmd = null;
            this.originalSql = null;
            this.staticSqlStrings = null;
            this.parameterValues = null;
            this.parameterStreams = null;
            this.isStream = null;
            this.streamLengths = null;
            this.isNull = null;
            this.streamConvertBuf = null;
            this.parameterTypes = null;
        }
    }
    
    @Override
    public void setArray(final int i, final Array x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    public void setStruct(final int i, final Struct x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 12);
        }
        else if (this.connection.getIO().isOracleMode()) {
            final int parameterIndexOffset = this.getParameterIndexOffset();
            final ByteArrayOutputStream bo = new ByteArrayOutputStream();
            bo.write(39);
            try {
                if (length >= 0) {
                    for (int index = length; index > 0; --index) {
                        final int b = x.read();
                        if (b == -1) {
                            throw new IndexOutOfBoundsException();
                        }
                        bo.write(b);
                    }
                }
                else {
                    int b;
                    while ((b = x.read()) != -1) {
                        bo.write(b);
                    }
                }
            }
            catch (Exception e) {
                throw new SQLException(" read bytes from inputStream error", e);
            }
            bo.write(39);
            this.parameterStreams[parameterIndex - 1 + parameterIndexOffset] = null;
            this.isStream[parameterIndex - 1 + parameterIndexOffset] = false;
            this.parameterValues[parameterIndex - 1 + parameterIndexOffset] = bo.toByteArray();
            this.isNull[parameterIndex - 1 + parameterIndexOffset] = false;
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2005;
        }
        else {
            this.setBinaryStream(parameterIndex, x, length);
        }
    }
    
    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 3);
        }
        else {
            this.setInternal(parameterIndex, StringUtils.fixDecimalExponent(StringUtils.consistentToString(x)));
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 3;
        }
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, -2);
            }
            else {
                final int parameterIndexOffset = this.getParameterIndexOffset();
                if (parameterIndex < 1 || parameterIndex > this.staticSqlStrings.length) {
                    throw SQLError.createSQLException(Messages.getString("PreparedStatement.2") + parameterIndex + Messages.getString("PreparedStatement.3") + this.staticSqlStrings.length + Messages.getString("PreparedStatement.4"), "S1009", this.getExceptionInterceptor());
                }
                if (parameterIndexOffset == -1 && parameterIndex == 1) {
                    throw SQLError.createSQLException("Can't set IN parameter for return value of stored function call.", "S1009", this.getExceptionInterceptor());
                }
                if (this.connection.getIO().isOracleMode()) {
                    final StringBuilder sb = new StringBuilder();
                    try {
                        if (length >= 0) {
                            for (int index = length; index > 0; --index) {
                                final int b = x.read();
                                if (b == -1) {
                                    throw new IndexOutOfBoundsException();
                                }
                                sb.append(String.format("%02x", b));
                            }
                        }
                        else {
                            int b;
                            while ((b = x.read()) != -1) {
                                sb.append(String.format("%02x", b));
                            }
                        }
                    }
                    catch (Exception e) {
                        throw new SQLException(" read bytes from inputStream error", e);
                    }
                    final byte[] hexBytes = StringUtils.getBytesWrapped(sb.toString(), '\'', '\'', this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                    this.parameterStreams[parameterIndex - 1 + parameterIndexOffset] = null;
                    this.isStream[parameterIndex - 1 + parameterIndexOffset] = false;
                    this.parameterValues[parameterIndex - 1 + parameterIndexOffset] = hexBytes;
                    this.isNull[parameterIndex - 1 + parameterIndexOffset] = false;
                    this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2004;
                }
                else {
                    this.parameterStreams[parameterIndex - 1 + parameterIndexOffset] = x;
                    this.isStream[parameterIndex - 1 + parameterIndexOffset] = true;
                    this.streamLengths[parameterIndex - 1 + parameterIndexOffset] = length;
                    this.isNull[parameterIndex - 1 + parameterIndexOffset] = false;
                    this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2004;
                }
            }
        }
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream, final long length) throws SQLException {
        this.setBinaryStream(parameterIndex, inputStream, (int)length);
    }
    
    @Override
    public void setBlob(final int i, final Blob x) throws SQLException {
        if (x == null) {
            this.setNull(i, 2004);
        }
        else {
            final int parameterIndexOffset = this.getParameterIndexOffset();
            if (this.connection.getIO().isOracleMode()) {
                if (this.connection.getIO().isUseOracleLocator() && ((com.alipay.oceanbase.jdbc.Blob)x).getLocator() != null && this.connection.getUseServerPreparedStmts()) {
                    final StringBuilder sb = new StringBuilder();
                    final com.alipay.oceanbase.jdbc.Blob blob = (com.alipay.oceanbase.jdbc.Blob)x;
                    for (final byte b : blob.getLocator().binaryData) {
                        sb.append(String.format("%02x", b & 0xFF));
                    }
                    final byte[] hexBytes = StringUtils.getBytesWrapped(sb.toString(), '\'', '\'', this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                    this.parameterStreams[i - 1 + parameterIndexOffset] = null;
                    this.isStream[i - 1 + parameterIndexOffset] = false;
                    this.parameterValues[i - 1 + parameterIndexOffset] = hexBytes;
                    this.isNull[i - 1 + parameterIndexOffset] = false;
                    this.parameterTypes[i - 1 + this.getParameterIndexOffset()] = 2004;
                }
                else {
                    final StringBuilder sb = new StringBuilder();
                    for (final byte b2 : x.getBytes(1L, (int)x.length())) {
                        sb.append(String.format("%02x", b2 & 0xFF));
                    }
                    final byte[] hexBytes2 = StringUtils.getBytesWrapped(sb.toString(), '\'', '\'', this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                    this.parameterStreams[i - 1 + parameterIndexOffset] = null;
                    this.isStream[i - 1 + parameterIndexOffset] = false;
                    this.parameterValues[i - 1 + parameterIndexOffset] = hexBytes2;
                    this.isNull[i - 1 + parameterIndexOffset] = false;
                    this.parameterTypes[i - 1 + this.getParameterIndexOffset()] = 2004;
                }
            }
            else {
                final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                bytesOut.write(39);
                this.escapeblockFast(x.getBytes(1L, (int)x.length()), bytesOut, (int)x.length());
                bytesOut.write(39);
                this.setInternal(i, bytesOut.toByteArray());
                this.parameterTypes[i - 1 + this.getParameterIndexOffset()] = 2004;
            }
        }
    }
    
    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        if (this.useTrueBoolean) {
            this.setInternal(parameterIndex, x ? "1" : "0");
        }
        else {
            this.setInternal(parameterIndex, x ? "'t'" : "'f'");
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 16;
        }
    }
    
    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        this.setInternal(parameterIndex, String.valueOf(x));
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -6;
    }
    
    @Override
    public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        this.setBytes(parameterIndex, x, true, true);
        if (x != null) {
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -2;
        }
    }
    
    protected void setBytes(final int parameterIndex, final byte[] x, final boolean checkForIntroducer, final boolean escapeForMBChars) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, -2);
            }
            else {
                byte[] sendBytes;
                if (this.connection.getIO().isOracleMode()) {
                    final StringBuilder sb = new StringBuilder();
                    for (final byte b : x) {
                        sb.append(String.format("%02x", b));
                    }
                    sendBytes = StringUtils.getBytesWrapped(sb.toString(), '\'', '\'', this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                }
                else {
                    final String connectionEncoding = this.connection.getEncoding();
                    try {
                        if (this.connection.isNoBackslashEscapesSet() || (escapeForMBChars && this.connection.getUseUnicode() && connectionEncoding != null && CharsetMapping.isMultibyteCharset(connectionEncoding))) {
                            final ByteArrayOutputStream bOut = new ByteArrayOutputStream(x.length * 2 + 3);
                            bOut.write(120);
                            bOut.write(39);
                            for (int i = 0; i < x.length; ++i) {
                                final int lowBits = (x[i] & 0xFF) / 16;
                                final int highBits = (x[i] & 0xFF) % 16;
                                bOut.write(PreparedStatement.HEX_DIGITS[lowBits]);
                                bOut.write(PreparedStatement.HEX_DIGITS[highBits]);
                            }
                            bOut.write(39);
                            this.setInternal(parameterIndex, bOut.toByteArray());
                            return;
                        }
                    }
                    catch (SQLException ex) {
                        throw ex;
                    }
                    catch (RuntimeException ex2) {
                        final SQLException sqlEx = SQLError.createSQLException(ex2.toString(), "S1009", null);
                        sqlEx.initCause(ex2);
                        throw sqlEx;
                    }
                    final int numBytes = x.length;
                    int pad = 2;
                    final boolean needsIntroducer = checkForIntroducer && this.connection.versionMeetsMinimum(4, 1, 0);
                    if (needsIntroducer) {
                        pad += 7;
                    }
                    final ByteArrayOutputStream bOut2 = new ByteArrayOutputStream(numBytes + pad);
                    if (needsIntroducer) {
                        bOut2.write(95);
                        bOut2.write(98);
                        bOut2.write(105);
                        bOut2.write(110);
                        bOut2.write(97);
                        bOut2.write(114);
                        bOut2.write(121);
                    }
                    bOut2.write(39);
                    for (final byte b2 : x) {
                        switch (b2) {
                            case 0: {
                                bOut2.write(92);
                                bOut2.write(48);
                                break;
                            }
                            case 10: {
                                bOut2.write(92);
                                bOut2.write(110);
                                break;
                            }
                            case 13: {
                                bOut2.write(92);
                                bOut2.write(114);
                                break;
                            }
                            case 92: {
                                bOut2.write(92);
                                bOut2.write(92);
                                break;
                            }
                            case 39: {
                                bOut2.write(92);
                                bOut2.write(39);
                                break;
                            }
                            case 34: {
                                bOut2.write(92);
                                bOut2.write(34);
                                break;
                            }
                            case 26: {
                                bOut2.write(92);
                                bOut2.write(90);
                                break;
                            }
                            default: {
                                bOut2.write(b2);
                                break;
                            }
                        }
                    }
                    bOut2.write(39);
                    sendBytes = bOut2.toByteArray();
                }
                this.setInternal(parameterIndex, sendBytes);
            }
        }
    }
    
    protected void setBytesNoEscape(final int parameterIndex, final byte[] parameterAsBytes) throws SQLException {
        final byte[] parameterWithQuotes = new byte[parameterAsBytes.length + 2];
        parameterWithQuotes[0] = 39;
        System.arraycopy(parameterAsBytes, 0, parameterWithQuotes, 1, parameterAsBytes.length);
        parameterWithQuotes[parameterAsBytes.length + 1] = 39;
        this.setInternal(parameterIndex, parameterWithQuotes);
    }
    
    protected void setBytesNoEscapeNoQuotes(final int parameterIndex, final byte[] parameterAsBytes) throws SQLException {
        this.setInternal(parameterIndex, parameterAsBytes);
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            try {
                if (reader == null) {
                    this.setNull(parameterIndex, -1);
                }
                else {
                    char[] c = null;
                    int len = 0;
                    final boolean useLength = this.connection.getUseStreamLengthsInPrepStmts();
                    final String forcedEncoding = this.connection.getClobCharacterEncoding();
                    if (useLength && length != -1) {
                        c = new char[length];
                        final int numCharsRead = readFully(reader, c, length);
                        if (forcedEncoding == null) {
                            this.setString(parameterIndex, new String(c, 0, numCharsRead));
                        }
                        else {
                            try {
                                this.setBytes(parameterIndex, StringUtils.getBytes(new String(c, 0, numCharsRead), forcedEncoding));
                            }
                            catch (UnsupportedEncodingException uee) {
                                throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009", this.getExceptionInterceptor());
                            }
                        }
                    }
                    else {
                        c = new char[4096];
                        final StringBuilder buf = new StringBuilder();
                        while ((len = reader.read(c)) != -1) {
                            buf.append(c, 0, len);
                        }
                        if (forcedEncoding == null) {
                            this.setString(parameterIndex, buf.toString());
                        }
                        else {
                            try {
                                this.setBytes(parameterIndex, StringUtils.getBytes(buf.toString(), forcedEncoding));
                            }
                            catch (UnsupportedEncodingException uee) {
                                throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009", this.getExceptionInterceptor());
                            }
                        }
                    }
                    this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2005;
                }
            }
            catch (IOException ioEx) {
                throw SQLError.createSQLException(ioEx.toString(), "S1000", this.getExceptionInterceptor());
            }
        }
    }
    
    @Override
    public void setClob(final int i, final Clob x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(i, 2005);
            }
            else {
                final String forcedEncoding = this.connection.getClobCharacterEncoding();
                if (forcedEncoding == null) {
                    this.setString(i, x.getSubString(1L, (int)x.length()));
                }
                else {
                    try {
                        this.setBytes(i, StringUtils.getBytes(x.getSubString(1L, (int)x.length()), forcedEncoding));
                    }
                    catch (UnsupportedEncodingException uee) {
                        throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009", this.getExceptionInterceptor());
                    }
                }
                this.parameterTypes[i - 1 + this.getParameterIndexOffset()] = 2005;
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
        else if (!this.useLegacyDatetimeCode) {
            this.newSetDateInternal(parameterIndex, x, cal);
        }
        else {
            synchronized (this.checkClosed().getConnectionMutex()) {
                if (((ConnectionImpl)this.connection).isOracleMode()) {
                    this.setTimestamp(parameterIndex, new Timestamp(x.getTime()));
                }
                else {
                    if (this.ddf == null) {
                        this.ddf = new SimpleDateFormat("''yyyy-MM-dd''", Locale.US);
                    }
                    if (cal != null) {
                        this.ddf.setTimeZone(cal.getTimeZone());
                    }
                    this.setInternal(parameterIndex, this.ddf.format(x));
                    this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 91;
                }
            }
        }
    }
    
    @Override
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (!this.connection.getAllowNanAndInf() && (x == Double.POSITIVE_INFINITY || x == Double.NEGATIVE_INFINITY || Double.isNaN(x))) {
                throw SQLError.createSQLException("'" + x + "' is not a valid numeric or approximate numeric value", "S1009", this.getExceptionInterceptor());
            }
            this.setInternal(parameterIndex, StringUtils.fixDecimalExponent(String.valueOf(x)));
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 8;
        }
    }
    
    @Override
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        this.setInternal(parameterIndex, StringUtils.fixDecimalExponent(String.valueOf(x)));
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 6;
    }
    
    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        this.setInternal(parameterIndex, String.valueOf(x));
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 4;
    }
    
    protected final void setInternal(final int paramIndex, final byte[] val) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final int parameterIndexOffset = this.getParameterIndexOffset();
            this.checkBounds(paramIndex, parameterIndexOffset);
            this.isStream[paramIndex - 1 + parameterIndexOffset] = false;
            this.isNull[paramIndex - 1 + parameterIndexOffset] = false;
            this.parameterStreams[paramIndex - 1 + parameterIndexOffset] = null;
            this.parameterValues[paramIndex - 1 + parameterIndexOffset] = val;
        }
    }
    
    protected void checkBounds(final int paramIndex, final int parameterIndexOffset) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (paramIndex < 1) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.49") + paramIndex + Messages.getString("PreparedStatement.50"), "S1009", this.getExceptionInterceptor());
            }
            if (paramIndex > this.parameterCount) {
                throw SQLError.createSQLException(Messages.getString("PreparedStatement.51") + paramIndex + Messages.getString("PreparedStatement.52") + this.parameterValues.length + Messages.getString("PreparedStatement.53"), "S1009", this.getExceptionInterceptor());
            }
            if (parameterIndexOffset == -1 && paramIndex == 1) {
                throw SQLError.createSQLException("Can't set IN parameter for return value of stored function call.", "S1009", this.getExceptionInterceptor());
            }
        }
    }
    
    protected final void setInternal(final int paramIndex, final String val) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            byte[] parameterAsBytes = null;
            if (this.charConverter != null) {
                parameterAsBytes = this.charConverter.toBytes(val);
            }
            else {
                parameterAsBytes = StringUtils.getBytes(val, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
            }
            this.setInternal(paramIndex, parameterAsBytes);
        }
    }
    
    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        this.setInternal(parameterIndex, String.valueOf(x));
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -5;
    }
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.setInternal(parameterIndex, "null");
            this.isNull[parameterIndex - 1 + this.getParameterIndexOffset()] = true;
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 0;
        }
    }
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType, final String arg) throws SQLException {
        this.setNull(parameterIndex, sqlType);
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 0;
    }
    
    private void setNumericObject(final int parameterIndex, final Object parameterObj, final int targetSqlType, final int scale) throws SQLException {
        Number parameterAsNum;
        if (parameterObj instanceof Boolean) {
            parameterAsNum = (parameterObj ? 1 : 0);
        }
        else if (parameterObj instanceof String) {
            switch (targetSqlType) {
                case -7: {
                    if ("1".equals(parameterObj) || "0".equals(parameterObj)) {
                        parameterAsNum = Integer.valueOf((String)parameterObj);
                        break;
                    }
                    final boolean parameterAsBoolean = "true".equalsIgnoreCase((String)parameterObj);
                    parameterAsNum = (parameterAsBoolean ? 1 : 0);
                    break;
                }
                case -6:
                case 4:
                case 5: {
                    parameterAsNum = Integer.valueOf((String)parameterObj);
                    break;
                }
                case -5: {
                    parameterAsNum = Long.valueOf((String)parameterObj);
                    break;
                }
                case 7: {
                    parameterAsNum = Float.valueOf((String)parameterObj);
                    break;
                }
                case 6:
                case 8: {
                    parameterAsNum = Double.valueOf((String)parameterObj);
                    break;
                }
                default: {
                    parameterAsNum = new BigDecimal((String)parameterObj);
                    break;
                }
            }
        }
        else {
            parameterAsNum = (Number)parameterObj;
        }
        switch (targetSqlType) {
            case -7:
            case -6:
            case 4:
            case 5: {
                this.setInt(parameterIndex, parameterAsNum.intValue());
                break;
            }
            case -5: {
                this.setLong(parameterIndex, parameterAsNum.longValue());
                break;
            }
            case 7: {
                this.setFloat(parameterIndex, parameterAsNum.floatValue());
                break;
            }
            case 6:
            case 8: {
                this.setDouble(parameterIndex, parameterAsNum.doubleValue());
                break;
            }
            case 2:
            case 3: {
                if (parameterAsNum instanceof BigDecimal) {
                    BigDecimal scaledBigDecimal = null;
                    try {
                        scaledBigDecimal = ((BigDecimal)parameterAsNum).setScale(scale);
                    }
                    catch (ArithmeticException ex) {
                        try {
                            scaledBigDecimal = ((BigDecimal)parameterAsNum).setScale(scale, 4);
                        }
                        catch (ArithmeticException arEx) {
                            throw SQLError.createSQLException("Can't set scale of '" + scale + "' for DECIMAL argument '" + parameterAsNum + "'", "S1009", this.getExceptionInterceptor());
                        }
                    }
                    this.setBigDecimal(parameterIndex, scaledBigDecimal);
                    break;
                }
                if (parameterAsNum instanceof BigInteger) {
                    this.setBigDecimal(parameterIndex, new BigDecimal((BigInteger)parameterAsNum, scale));
                    break;
                }
                this.setBigDecimal(parameterIndex, new BigDecimal(parameterAsNum.doubleValue()));
                break;
            }
        }
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object parameterObj) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (parameterObj == null) {
                this.setNull(parameterIndex, 1111);
            }
            else if (parameterObj instanceof Byte) {
                this.setInt(parameterIndex, (int)parameterObj);
            }
            else if (parameterObj instanceof String) {
                this.setString(parameterIndex, (String)parameterObj);
            }
            else if (parameterObj instanceof BigDecimal) {
                this.setBigDecimal(parameterIndex, (BigDecimal)parameterObj);
            }
            else if (parameterObj instanceof Short) {
                this.setShort(parameterIndex, (short)parameterObj);
            }
            else if (parameterObj instanceof Integer) {
                this.setInt(parameterIndex, (int)parameterObj);
            }
            else if (parameterObj instanceof Long) {
                this.setLong(parameterIndex, (long)parameterObj);
            }
            else if (parameterObj instanceof Float) {
                this.setFloat(parameterIndex, (float)parameterObj);
            }
            else if (parameterObj instanceof Double) {
                this.setDouble(parameterIndex, (double)parameterObj);
            }
            else if (parameterObj instanceof byte[]) {
                this.setBytes(parameterIndex, (byte[])parameterObj);
            }
            else if (parameterObj instanceof java.sql.Date) {
                this.setDate(parameterIndex, (java.sql.Date)parameterObj);
            }
            else if (parameterObj instanceof Time) {
                this.setTime(parameterIndex, (Time)parameterObj);
            }
            else if (parameterObj instanceof Timestamp) {
                this.setTimestamp(parameterIndex, (Timestamp)parameterObj);
            }
            else if ("java.time.LocalDate".equals(parameterObj.getClass().getName())) {
                java.sql.Date date;
                try {
                    date = LocalDateReflection.getDate(parameterObj);
                }
                catch (Exception e) {
                    throw new RuntimeException(parameterObj.getClass().getName() + " LocalDate Type conversion exception", e);
                }
                this.setDate(parameterIndex, date);
            }
            else if ("java.time.LocalDateTime".equals(parameterObj.getClass().getName())) {
                Timestamp timeStamp;
                try {
                    timeStamp = LocalDateTimeReflection.getTimeStamp(parameterObj);
                }
                catch (Exception e) {
                    throw new RuntimeException(parameterObj.getClass().getName() + " LocalDateTime Type conversion exception", e);
                }
                this.setTimestamp(parameterIndex, timeStamp);
            }
            else if ("java.time.LocalTime".equals(parameterObj.getClass().getName())) {
                Timestamp time;
                try {
                    time = LocalTimeReflection.getTime(parameterObj);
                }
                catch (Exception e) {
                    throw new RuntimeException(parameterObj.getClass().getName() + " LocalTime Type conversion exception", e);
                }
                this.setTimestamp(parameterIndex, time);
            }
            else if (parameterObj instanceof Boolean) {
                this.setBoolean(parameterIndex, (boolean)parameterObj);
            }
            else if (parameterObj instanceof Array) {
                this.setArray(parameterIndex, (Array)parameterObj);
            }
            else if (parameterObj instanceof Struct) {
                this.setStruct(parameterIndex, (Struct)parameterObj);
            }
            else if (parameterObj instanceof InputStream) {
                if (this.connection.getIO().isOracleMode()) {
                    throw new SQLException("oracle mode InputStream may cause confuse please use setBinaryStream or setAsciiStream instead.");
                }
                this.setBinaryStream(parameterIndex, (InputStream)parameterObj, -1);
            }
            else if (parameterObj instanceof Blob) {
                this.setBlob(parameterIndex, (Blob)parameterObj);
            }
            else if (parameterObj instanceof Clob) {
                this.setClob(parameterIndex, (Clob)parameterObj);
            }
            else if (this.connection.getTreatUtilDateAsTimestamp() && parameterObj instanceof Date) {
                this.setTimestamp(parameterIndex, new Timestamp(((Date)parameterObj).getTime()));
            }
            else if (parameterObj instanceof BigInteger) {
                this.setString(parameterIndex, parameterObj.toString());
            }
            else {
                this.setSerializableObject(parameterIndex, parameterObj);
            }
        }
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object parameterObj, final int targetSqlType) throws SQLException {
        if (!(parameterObj instanceof BigDecimal)) {
            this.setObject(parameterIndex, parameterObj, targetSqlType, 0);
        }
        else {
            this.setObject(parameterIndex, parameterObj, targetSqlType, ((BigDecimal)parameterObj).scale());
        }
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object parameterObj, final int targetSqlType, final int scale) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (parameterObj == null) {
                this.setNull(parameterIndex, 1111);
            }
            else {
                try {
                    switch (targetSqlType) {
                        case 16: {
                            if (parameterObj instanceof Boolean) {
                                this.setBoolean(parameterIndex, (boolean)parameterObj);
                                break;
                            }
                            if (parameterObj instanceof String) {
                                this.setBoolean(parameterIndex, "true".equalsIgnoreCase((String)parameterObj) || !"0".equalsIgnoreCase((String)parameterObj));
                                break;
                            }
                            if (parameterObj instanceof Number) {
                                final int intValue = ((Number)parameterObj).intValue();
                                this.setBoolean(parameterIndex, intValue != 0);
                                break;
                            }
                            throw SQLError.createSQLException("No conversion from " + parameterObj.getClass().getName() + " to Types.BOOLEAN possible.", "S1009", this.getExceptionInterceptor());
                        }
                        case -7:
                        case -6:
                        case -5:
                        case 2:
                        case 3:
                        case 4:
                        case 5:
                        case 6:
                        case 7:
                        case 8: {
                            this.setNumericObject(parameterIndex, parameterObj, targetSqlType, scale);
                            break;
                        }
                        case -1:
                        case 1:
                        case 12: {
                            if (parameterObj instanceof BigDecimal) {
                                this.setString(parameterIndex, StringUtils.fixDecimalExponent(StringUtils.consistentToString((BigDecimal)parameterObj)));
                                break;
                            }
                            this.setString(parameterIndex, parameterObj.toString());
                            break;
                        }
                        case 2005: {
                            if (this.connection.getIO().isOracleMode()) {
                                if (parameterObj instanceof Clob) {
                                    this.setClob(parameterIndex, (Clob)parameterObj);
                                    break;
                                }
                                throw new SQLException(parameterObj.getClass().getName() + " can not be converted to blob.");
                            }
                            else {
                                if (parameterObj instanceof Clob) {
                                    this.setClob(parameterIndex, (Clob)parameterObj);
                                    break;
                                }
                                this.setString(parameterIndex, parameterObj.toString());
                                break;
                            }
                            break;
                        }
                        case -4:
                        case -3:
                        case -2:
                        case 2004: {
                            if (this.connection.getIO().isOracleMode()) {
                                if (parameterObj instanceof Blob) {
                                    this.setBlob(parameterIndex, (Blob)parameterObj);
                                    break;
                                }
                                throw new SQLException(parameterObj.getClass().getName() + " can not be converted to blob.");
                            }
                            else {
                                if (parameterObj instanceof byte[]) {
                                    this.setBytes(parameterIndex, (byte[])parameterObj);
                                    break;
                                }
                                if (parameterObj instanceof Blob) {
                                    this.setBlob(parameterIndex, (Blob)parameterObj);
                                    break;
                                }
                                this.setBytes(parameterIndex, StringUtils.getBytes(parameterObj.toString(), this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor()));
                                break;
                            }
                            break;
                        }
                        case 91:
                        case 93: {
                            Date parameterAsDate;
                            if (parameterObj instanceof String) {
                                final ParsePosition pp = new ParsePosition(0);
                                final DateFormat sdf = new SimpleDateFormat(this.getDateTimePattern((String)parameterObj, false), Locale.US);
                                parameterAsDate = sdf.parse((String)parameterObj, pp);
                            }
                            else {
                                parameterAsDate = (Date)parameterObj;
                            }
                            switch (targetSqlType) {
                                case 91: {
                                    if (parameterAsDate instanceof java.sql.Date) {
                                        this.setDate(parameterIndex, (java.sql.Date)parameterAsDate);
                                        break;
                                    }
                                    this.setDate(parameterIndex, new java.sql.Date(parameterAsDate.getTime()));
                                    break;
                                }
                                case 93: {
                                    if (parameterAsDate instanceof Timestamp) {
                                        this.setTimestamp(parameterIndex, (Timestamp)parameterAsDate);
                                        break;
                                    }
                                    this.setTimestamp(parameterIndex, new Timestamp(parameterAsDate.getTime()));
                                    break;
                                }
                            }
                            break;
                        }
                        case 92: {
                            if (parameterObj instanceof String) {
                                final DateFormat sdf2 = new SimpleDateFormat(this.getDateTimePattern((String)parameterObj, true), Locale.US);
                                this.setTime(parameterIndex, new Time(sdf2.parse((String)parameterObj).getTime()));
                                break;
                            }
                            if (parameterObj instanceof Timestamp) {
                                final Timestamp xT = (Timestamp)parameterObj;
                                this.setTime(parameterIndex, new Time(xT.getTime()));
                                break;
                            }
                            this.setTime(parameterIndex, (Time)parameterObj);
                            break;
                        }
                        case 1111: {
                            this.setSerializableObject(parameterIndex, parameterObj);
                            break;
                        }
                        default: {
                            throw SQLError.createSQLException(Messages.getString("PreparedStatement.16"), "S1000", this.getExceptionInterceptor());
                        }
                    }
                }
                catch (Exception ex) {
                    if (ex instanceof SQLException) {
                        throw (SQLException)ex;
                    }
                    final SQLException sqlEx = SQLError.createSQLException(Messages.getString("PreparedStatement.17") + parameterObj.getClass().toString() + Messages.getString("PreparedStatement.18") + ex.getClass().getName() + Messages.getString("PreparedStatement.19") + ex.getMessage(), "S1000", this.getExceptionInterceptor());
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
            }
        }
    }
    
    protected int setOneBatchedParameterSet(final java.sql.PreparedStatement batchedStatement, int batchedParamIndex, final Object paramSet) throws SQLException {
        final BatchParams paramArg = (BatchParams)paramSet;
        final boolean[] isNullBatch = paramArg.isNull;
        final boolean[] isStreamBatch = paramArg.isStream;
        for (int j = 0; j < isNullBatch.length; ++j) {
            if (isNullBatch[j]) {
                batchedStatement.setNull(batchedParamIndex++, 0);
            }
            else if (isStreamBatch[j]) {
                batchedStatement.setBinaryStream(batchedParamIndex++, paramArg.parameterStreams[j], paramArg.streamLengths[j]);
            }
            else {
                ((PreparedStatement)batchedStatement).setBytesNoEscapeNoQuotes(batchedParamIndex++, paramArg.parameterStrings[j]);
            }
        }
        return batchedParamIndex;
    }
    
    @Override
    public void setRef(final int i, final Ref x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    private final void setSerializableObject(final int parameterIndex, final Object parameterObj) throws SQLException {
        try {
            final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
            final ObjectOutputStream objectOut = new ObjectOutputStream(bytesOut);
            objectOut.writeObject(parameterObj);
            objectOut.flush();
            objectOut.close();
            bytesOut.flush();
            bytesOut.close();
            final byte[] buf = bytesOut.toByteArray();
            final ByteArrayInputStream bytesIn = new ByteArrayInputStream(buf);
            this.setBinaryStream(parameterIndex, bytesIn, buf.length);
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -2;
        }
        catch (Exception ex) {
            final SQLException sqlEx = SQLError.createSQLException(Messages.getString("PreparedStatement.54") + ex.getClass().getName(), "S1009", this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }
    
    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        this.setInternal(parameterIndex, String.valueOf(x));
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 5;
    }
    
    @Override
    public void setString(final int parameterIndex, final String x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (x == null) {
                this.setNull(parameterIndex, 1);
            }
            else {
                this.checkClosed();
                final int stringLength = x.length();
                if (this.connection.isNoBackslashEscapesSet()) {
                    final boolean needsHexEscape = this.isEscapeNeededForString(x, stringLength);
                    if (!needsHexEscape) {
                        byte[] parameterAsBytes = null;
                        final StringBuilder quotedString = new StringBuilder(x.length() + 2);
                        quotedString.append('\'');
                        quotedString.append(x);
                        quotedString.append('\'');
                        if (!this.isLoadDataQuery) {
                            parameterAsBytes = StringUtils.getBytes(quotedString.toString(), this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                        }
                        else {
                            parameterAsBytes = StringUtils.getBytes(quotedString.toString());
                        }
                        this.setInternal(parameterIndex, parameterAsBytes);
                    }
                    else {
                        byte[] parameterAsBytes = null;
                        if (!this.isLoadDataQuery) {
                            parameterAsBytes = StringUtils.getBytes(x, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                        }
                        else {
                            parameterAsBytes = StringUtils.getBytes(x);
                        }
                        this.setBytes(parameterIndex, parameterAsBytes);
                    }
                    return;
                }
                String parameterAsString = x;
                boolean needsQuoted = true;
                if (this.isLoadDataQuery || this.isEscapeNeededForString(x, stringLength)) {
                    needsQuoted = false;
                    final StringBuilder buf = new StringBuilder((int)(x.length() * 1.1));
                    buf.append('\'');
                    for (int i = 0; i < stringLength; ++i) {
                        final char c = x.charAt(i);
                        if (!this.connection.getIO().isOracleMode()) {
                            switch (c) {
                                case '\0': {
                                    buf.append('\\');
                                    buf.append('0');
                                    break;
                                }
                                case '\n': {
                                    buf.append('\\');
                                    buf.append('n');
                                    break;
                                }
                                case '\r': {
                                    buf.append('\\');
                                    buf.append('r');
                                    break;
                                }
                                case '\\': {
                                    buf.append('\\');
                                    buf.append('\\');
                                    break;
                                }
                                case '\'': {
                                    buf.append('\\');
                                    buf.append('\'');
                                    break;
                                }
                                case '\"': {
                                    if (this.usingAnsiMode) {
                                        buf.append('\\');
                                    }
                                    buf.append('\"');
                                    break;
                                }
                                case '\u001a': {
                                    buf.append('\\');
                                    buf.append('Z');
                                    break;
                                }
                                case '¥':
                                case '\u20a9': {
                                    if (this.charsetEncoder != null) {
                                        final CharBuffer cbuf = CharBuffer.allocate(1);
                                        final ByteBuffer bbuf = ByteBuffer.allocate(1);
                                        cbuf.put(c);
                                        cbuf.position(0);
                                        this.charsetEncoder.encode(cbuf, bbuf, true);
                                        if (bbuf.get(0) == 92) {
                                            buf.append('\\');
                                        }
                                    }
                                    buf.append(c);
                                    break;
                                }
                                default: {
                                    buf.append(c);
                                    break;
                                }
                            }
                        }
                        else {
                            switch (c) {
                                case '\'': {
                                    buf.append("'");
                                    buf.append("'");
                                    break;
                                }
                                default: {
                                    buf.append(c);
                                    break;
                                }
                            }
                        }
                    }
                    buf.append('\'');
                    parameterAsString = buf.toString();
                }
                byte[] parameterAsBytes2 = null;
                if (!this.isLoadDataQuery) {
                    if (needsQuoted) {
                        parameterAsBytes2 = StringUtils.getBytesWrapped(parameterAsString, '\'', '\'', this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                    }
                    else {
                        parameterAsBytes2 = StringUtils.getBytes(parameterAsString, this.charConverter, this.charEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                    }
                }
                else {
                    parameterAsBytes2 = StringUtils.getBytes(parameterAsString);
                }
                this.setInternal(parameterIndex, parameterAsBytes2);
                this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 12;
            }
        }
    }
    
    private boolean isEscapeNeededForString(final String x, final int stringLength) throws SQLException {
        boolean needsHexEscape = false;
        for (int i = 0; i < stringLength; ++i) {
            final char c = x.charAt(i);
            if (!this.connection.getIO().isOracleMode()) {
                switch (c) {
                    case '\0': {
                        needsHexEscape = true;
                        break;
                    }
                    case '\n': {
                        needsHexEscape = true;
                        break;
                    }
                    case '\r': {
                        needsHexEscape = true;
                        break;
                    }
                    case '\\': {
                        needsHexEscape = true;
                        break;
                    }
                    case '\'': {
                        needsHexEscape = true;
                        break;
                    }
                    case '\"': {
                        needsHexEscape = true;
                        break;
                    }
                    case '\u001a': {
                        needsHexEscape = true;
                        break;
                    }
                }
            }
            else {
                switch (c) {
                    case '\'': {
                        needsHexEscape = true;
                        break;
                    }
                }
            }
            if (needsHexEscape) {
                break;
            }
        }
        return needsHexEscape;
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.setTimeInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
        }
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.setTimeInternal(parameterIndex, x, null, this.connection.getDefaultTimeZone(), false);
        }
    }
    
    private void setTimeInternal(final int parameterIndex, Time x, final Calendar targetCalendar, final TimeZone tz, final boolean rollForward) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 92);
        }
        else {
            this.checkClosed();
            if (!this.useLegacyDatetimeCode) {
                this.newSetTimeInternal(parameterIndex, x, targetCalendar);
            }
            else {
                final Calendar sessionCalendar = this.getCalendarInstanceForSessionOrNew();
                x = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
                this.setInternal(parameterIndex, "'" + x.toString() + "'");
            }
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 92;
        }
    }
    
    public void setINTERVALDS(final int parameterIndex, final INTERVALDS intervalds) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (intervalds == null) {
                this.setNull(parameterIndex, 93);
            }
            else {
                this.checkClosed();
                synchronized (this) {
                    final StringBuffer buf = new StringBuffer();
                    buf.append("interval '");
                    buf.append(intervalds.toString());
                    buf.append("' day(9) to second(9)");
                    this.setInternal(parameterIndex, buf.toString());
                }
                this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 205;
            }
        }
    }
    
    public void setINTERVALYM(final int parameterIndex, final INTERVALYM intervalym) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (intervalym == null) {
                this.setNull(parameterIndex, 93);
            }
            else {
                this.checkClosed();
                synchronized (this) {
                    final StringBuffer buf = new StringBuffer();
                    buf.append("interval '");
                    buf.append(intervalym.toString());
                    buf.append("' year(9) to month");
                    this.setInternal(parameterIndex, buf.toString());
                }
                this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 204;
            }
        }
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.setTimestampInternal(parameterIndex, x, cal, cal.getTimeZone(), true);
        }
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.setTimestampInternal(parameterIndex, x, null, this.connection.getDefaultTimeZone(), false);
        }
    }
    
    public void setTIMESTAMPTZ(final int parameterIndex, final TIMESTAMPTZ timestamptz) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (timestamptz == null) {
                this.setNull(parameterIndex, 93);
            }
            else {
                this.checkClosed();
                synchronized (this) {
                    final byte[] bytes = timestamptz.getBytes();
                    final Timestamp timestamp = new Timestamp(TIMESTAMPTZ.getOriginTime(bytes, TimeZone.getDefault()));
                    final Calendar calendar = Calendar.getInstance();
                    calendar.setTime(timestamp);
                    timestamp.setTime(calendar.getTime().getTime());
                    timestamp.setNanos(TIMESTAMP.getNanos(bytes, 7));
                    final String tzStr = TIMESTAMPTZ.toTimezoneStr(bytes[12], bytes[13], "");
                    final StringBuffer buf = new StringBuffer();
                    buf.append("timestamp '");
                    buf.append(timestamp.toString());
                    buf.append(" ").append(tzStr);
                    buf.append('\'');
                    this.setInternal(parameterIndex, buf.toString());
                }
                this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 93;
            }
        }
    }
    
    public void setTIMESTAMP(final int parameterIndex, final Timestamp timestamp) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (timestamp == null) {
                this.setNull(parameterIndex, 93);
            }
            else {
                this.checkClosed();
                synchronized (this) {
                    final StringBuffer buf = new StringBuffer();
                    buf.append("timestamp '");
                    buf.append(timestamp.toString());
                    buf.append('\'');
                    this.setInternal(parameterIndex, buf.toString());
                }
                this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 93;
            }
        }
    }
    
    public void setTIMESTAMPLTZ(final int parameterIndex, final TIMESTAMPLTZ timestampltz) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (timestampltz == null) {
                this.setNull(parameterIndex, 93);
            }
            else {
                this.checkClosed();
                synchronized (this) {
                    final Timestamp timestamp = TIMESTAMPLTZ.toTimestamp(this.connection, timestampltz.getBytes());
                    final StringBuffer buf = new StringBuffer();
                    buf.append("timestamp '");
                    buf.append(timestamp.toString());
                    buf.append('\'');
                    this.setInternal(parameterIndex, buf.toString());
                }
                this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 93;
            }
        }
    }
    
    private void setTimestampInternal(final int parameterIndex, Timestamp x, final Calendar targetCalendar, final TimeZone tz, final boolean rollForward) throws SQLException {
        if (((ObConnection)this.connection).isOracleMode()) {
            this.setTIMESTAMP(parameterIndex, x);
            return;
        }
        if (x == null) {
            this.setNull(parameterIndex, 93);
        }
        else {
            this.checkClosed();
            if (!this.sendFractionalSeconds) {
                x = TimeUtil.truncateFractionalSeconds(x);
            }
            if (!this.useLegacyDatetimeCode) {
                this.newSetTimestampInternal(parameterIndex, x, targetCalendar);
            }
            else {
                final Calendar sessionCalendar = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew();
                x = TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, x, tz, this.connection.getServerTimezoneTZ(), rollForward);
                if (this.connection.getUseSSPSCompatibleTimezoneShift()) {
                    this.doSSPSCompatibleTimezoneShift(parameterIndex, x);
                }
                else {
                    synchronized (this) {
                        if (this.tsdf == null) {
                            this.tsdf = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss", Locale.US);
                        }
                        final StringBuffer buf = new StringBuffer();
                        buf.append(this.tsdf.format(x));
                        if (this.serverSupportsFracSecs) {
                            final int nanos = x.getNanos();
                            if (nanos != 0) {
                                buf.append('.');
                                buf.append(TimeUtil.formatNanos(nanos, this.serverSupportsFracSecs, true));
                            }
                        }
                        buf.append('\'');
                        this.setInternal(parameterIndex, buf.toString());
                    }
                }
            }
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 93;
        }
    }
    
    private void newSetTimestampInternal(final int parameterIndex, final Timestamp x, final Calendar targetCalendar) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.tsdf == null) {
                this.tsdf = new SimpleDateFormat("''yyyy-MM-dd HH:mm:ss", Locale.US);
            }
            if (targetCalendar != null) {
                this.tsdf.setTimeZone(targetCalendar.getTimeZone());
            }
            else {
                this.tsdf.setTimeZone(this.connection.getServerTimezoneTZ());
            }
            final StringBuffer buf = new StringBuffer();
            buf.append(this.tsdf.format(x));
            buf.append('.');
            buf.append(TimeUtil.formatNanos(x.getNanos(), this.serverSupportsFracSecs, true));
            buf.append('\'');
            this.setInternal(parameterIndex, buf.toString());
        }
    }
    
    private void newSetTimeInternal(final int parameterIndex, final Time x, final Calendar targetCalendar) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.tdf == null) {
                this.tdf = new SimpleDateFormat("''HH:mm:ss''", Locale.US);
            }
            if (targetCalendar != null) {
                this.tdf.setTimeZone(targetCalendar.getTimeZone());
            }
            else {
                this.tdf.setTimeZone(this.connection.getServerTimezoneTZ());
            }
            this.setInternal(parameterIndex, this.tdf.format(x));
        }
    }
    
    private void newSetDateInternal(final int parameterIndex, final java.sql.Date x, final Calendar targetCalendar) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.ddf == null) {
                this.ddf = new SimpleDateFormat("''yyyy-MM-dd''", Locale.US);
            }
            if (targetCalendar != null) {
                this.ddf.setTimeZone(targetCalendar.getTimeZone());
            }
            else if (this.connection.getNoTimezoneConversionForDateType()) {
                this.ddf.setTimeZone(this.connection.getDefaultTimeZone());
            }
            else {
                this.ddf.setTimeZone(this.connection.getServerTimezoneTZ());
            }
            this.setInternal(parameterIndex, this.ddf.format(x));
        }
    }
    
    private void doSSPSCompatibleTimezoneShift(final int parameterIndex, final Timestamp x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final Calendar sessionCalendar2 = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew();
            synchronized (sessionCalendar2) {
                final Date oldTime = sessionCalendar2.getTime();
                try {
                    sessionCalendar2.setTime(x);
                    final int year = sessionCalendar2.get(1);
                    final int month = sessionCalendar2.get(2) + 1;
                    final int date = sessionCalendar2.get(5);
                    final int hour = sessionCalendar2.get(11);
                    final int minute = sessionCalendar2.get(12);
                    final int seconds = sessionCalendar2.get(13);
                    final StringBuilder tsBuf = new StringBuilder();
                    tsBuf.append('\'');
                    tsBuf.append(year);
                    tsBuf.append("-");
                    if (month < 10) {
                        tsBuf.append('0');
                    }
                    tsBuf.append(month);
                    tsBuf.append('-');
                    if (date < 10) {
                        tsBuf.append('0');
                    }
                    tsBuf.append(date);
                    tsBuf.append(' ');
                    if (hour < 10) {
                        tsBuf.append('0');
                    }
                    tsBuf.append(hour);
                    tsBuf.append(':');
                    if (minute < 10) {
                        tsBuf.append('0');
                    }
                    tsBuf.append(minute);
                    tsBuf.append(':');
                    if (seconds < 10) {
                        tsBuf.append('0');
                    }
                    tsBuf.append(seconds);
                    tsBuf.append('.');
                    tsBuf.append(TimeUtil.formatNanos(x.getNanos(), this.serverSupportsFracSecs, true));
                    tsBuf.append('\'');
                    this.setInternal(parameterIndex, tsBuf.toString());
                }
                finally {
                    sessionCalendar2.setTime(oldTime);
                }
            }
        }
    }
    
    @Deprecated
    @Override
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        if (x == null) {
            this.setNull(parameterIndex, 12);
        }
        else {
            this.setBinaryStream(parameterIndex, x, length);
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2005;
        }
    }
    
    @Override
    public void setURL(final int parameterIndex, final URL arg) throws SQLException {
        if (arg != null) {
            this.setString(parameterIndex, arg.toString());
            this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 70;
        }
        else {
            this.setNull(parameterIndex, 1);
        }
    }
    
    private final void streamToBytes(final Buffer packet, InputStream in, final boolean escape, final int streamLength, boolean useLength) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            try {
                if (this.streamConvertBuf == null) {
                    this.streamConvertBuf = new byte[4096];
                }
                final String connectionEncoding = this.connection.getEncoding();
                boolean hexEscape = false;
                try {
                    if (this.connection.isNoBackslashEscapesSet() || (this.connection.getUseUnicode() && connectionEncoding != null && CharsetMapping.isMultibyteCharset(connectionEncoding) && !this.connection.parserKnowsUnicode())) {
                        hexEscape = true;
                    }
                }
                catch (RuntimeException ex) {
                    final SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
                if (streamLength == -1) {
                    useLength = false;
                }
                int bc = -1;
                if (useLength) {
                    bc = this.readblock(in, this.streamConvertBuf, streamLength);
                }
                else {
                    bc = this.readblock(in, this.streamConvertBuf);
                }
                int lengthLeftToRead = streamLength - bc;
                if (hexEscape) {
                    packet.writeStringNoNull("x");
                }
                else if (this.connection.getIO().versionMeetsMinimum(4, 1, 0)) {
                    packet.writeStringNoNull("_binary");
                }
                if (escape) {
                    packet.writeByte((byte)39);
                }
                while (bc > 0) {
                    if (hexEscape) {
                        this.hexEscapeBlock(this.streamConvertBuf, packet, bc);
                    }
                    else if (escape) {
                        this.escapeblockFast(this.streamConvertBuf, packet, bc);
                    }
                    else {
                        packet.writeBytesNoNull(this.streamConvertBuf, 0, bc);
                    }
                    if (useLength) {
                        bc = this.readblock(in, this.streamConvertBuf, lengthLeftToRead);
                        if (bc <= 0) {
                            continue;
                        }
                        lengthLeftToRead -= bc;
                    }
                    else {
                        bc = this.readblock(in, this.streamConvertBuf);
                    }
                }
                if (escape) {
                    packet.writeByte((byte)39);
                }
            }
            finally {
                if (this.connection.getAutoClosePStmtStreams()) {
                    try {
                        in.close();
                    }
                    catch (IOException ex2) {}
                    in = null;
                }
            }
        }
    }
    
    private final byte[] streamToBytes(InputStream in, final boolean escape, final int streamLength, boolean useLength) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            in.mark(Integer.MAX_VALUE);
            try {
                if (this.streamConvertBuf == null) {
                    this.streamConvertBuf = new byte[4096];
                }
                if (streamLength == -1) {
                    useLength = false;
                }
                final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
                int bc = -1;
                if (useLength) {
                    bc = this.readblock(in, this.streamConvertBuf, streamLength);
                }
                else {
                    bc = this.readblock(in, this.streamConvertBuf);
                }
                int lengthLeftToRead = streamLength - bc;
                if (escape) {
                    if (this.connection.versionMeetsMinimum(4, 1, 0)) {
                        bytesOut.write(95);
                        bytesOut.write(98);
                        bytesOut.write(105);
                        bytesOut.write(110);
                        bytesOut.write(97);
                        bytesOut.write(114);
                        bytesOut.write(121);
                    }
                    bytesOut.write(39);
                }
                while (bc > 0) {
                    if (escape) {
                        this.escapeblockFast(this.streamConvertBuf, bytesOut, bc);
                    }
                    else {
                        bytesOut.write(this.streamConvertBuf, 0, bc);
                    }
                    if (useLength) {
                        bc = this.readblock(in, this.streamConvertBuf, lengthLeftToRead);
                        if (bc <= 0) {
                            continue;
                        }
                        lengthLeftToRead -= bc;
                    }
                    else {
                        bc = this.readblock(in, this.streamConvertBuf);
                    }
                }
                if (escape) {
                    bytesOut.write(39);
                }
                return bytesOut.toByteArray();
            }
            finally {
                try {
                    in.reset();
                }
                catch (IOException ex) {}
                if (this.connection.getAutoClosePStmtStreams()) {
                    try {
                        in.close();
                    }
                    catch (IOException ex2) {}
                    in = null;
                }
            }
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder();
        buf.append(super.toString());
        buf.append(": ");
        try {
            buf.append(this.asSql());
        }
        catch (SQLException sqlEx) {
            buf.append("EXCEPTION: " + sqlEx.toString());
        }
        return buf.toString();
    }
    
    protected int getParameterIndexOffset() {
        return 0;
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x) throws SQLException {
        this.setAsciiStream(parameterIndex, x, -1);
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        this.setAsciiStream(parameterIndex, x, (int)length);
        this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2005;
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x) throws SQLException {
        this.setBinaryStream(parameterIndex, x, -1);
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        this.setBinaryStream(parameterIndex, x, (int)length);
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream) throws SQLException {
        this.setBinaryStream(parameterIndex, inputStream);
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader) throws SQLException {
        this.setCharacterStream(parameterIndex, reader, -1);
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        this.setCharacterStream(parameterIndex, reader, (int)length);
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader) throws SQLException {
        this.setCharacterStream(parameterIndex, reader);
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        this.setCharacterStream(parameterIndex, reader, length);
    }
    
    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value) throws SQLException {
        this.setNCharacterStream(parameterIndex, value, -1L);
    }
    
    @Override
    public void setNString(final int parameterIndex, final String x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.charEncoding.equalsIgnoreCase("UTF-8") || this.charEncoding.equalsIgnoreCase("utf8")) {
                this.setString(parameterIndex, x);
                return;
            }
            if (x == null) {
                this.setNull(parameterIndex, 1);
            }
            else {
                final int stringLength = x.length();
                final StringBuilder buf = new StringBuilder((int)(x.length() * 1.1 + 4.0));
                buf.append("_utf8");
                buf.append('\'');
                for (int i = 0; i < stringLength; ++i) {
                    final char c = x.charAt(i);
                    if (!this.connection.getIO().isOracleMode()) {
                        switch (c) {
                            case '\0': {
                                buf.append('\\');
                                buf.append('0');
                                break;
                            }
                            case '\n': {
                                buf.append('\\');
                                buf.append('n');
                                break;
                            }
                            case '\r': {
                                buf.append('\\');
                                buf.append('r');
                                break;
                            }
                            case '\\': {
                                buf.append('\\');
                                buf.append('\\');
                                break;
                            }
                            case '\'': {
                                buf.append('\\');
                                buf.append('\'');
                                break;
                            }
                            case '\"': {
                                if (this.usingAnsiMode) {
                                    buf.append('\\');
                                }
                                buf.append('\"');
                                break;
                            }
                            case '\u001a': {
                                buf.append('\\');
                                buf.append('Z');
                                break;
                            }
                            default: {
                                buf.append(c);
                                break;
                            }
                        }
                    }
                    else {
                        switch (c) {
                            case '\'': {
                                buf.append('\\');
                                buf.append('\'');
                                break;
                            }
                            default: {
                                buf.append(c);
                                break;
                            }
                        }
                    }
                }
                buf.append('\'');
                final String parameterAsString = buf.toString();
                byte[] parameterAsBytes = null;
                if (!this.isLoadDataQuery) {
                    parameterAsBytes = StringUtils.getBytes(parameterAsString, this.connection.getCharsetConverter("UTF-8"), "UTF-8", this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                }
                else {
                    parameterAsBytes = StringUtils.getBytes(parameterAsString);
                }
                this.setInternal(parameterIndex, parameterAsBytes);
                this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = -9;
            }
        }
    }
    
    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            try {
                if (reader == null) {
                    this.setNull(parameterIndex, -1);
                }
                else {
                    char[] c = null;
                    int len = 0;
                    final boolean useLength = this.connection.getUseStreamLengthsInPrepStmts();
                    if (useLength && length != -1L) {
                        c = new char[(int)length];
                        final int numCharsRead = readFully(reader, c, (int)length);
                        this.setNString(parameterIndex, new String(c, 0, numCharsRead));
                    }
                    else {
                        c = new char[4096];
                        final StringBuilder buf = new StringBuilder();
                        while ((len = reader.read(c)) != -1) {
                            buf.append(c, 0, len);
                        }
                        this.setNString(parameterIndex, buf.toString());
                    }
                    this.parameterTypes[parameterIndex - 1 + this.getParameterIndexOffset()] = 2011;
                }
            }
            catch (IOException ioEx) {
                throw SQLError.createSQLException(ioEx.toString(), "S1000", this.getExceptionInterceptor());
            }
        }
    }
    
    @Override
    public void setNClob(final int parameterIndex, final NClob value) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNClob(final int parameterIndex, final Reader reader) throws SQLException {
        this.setNCharacterStream(parameterIndex, reader);
    }
    
    @Override
    public void setNClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        if (reader == null) {
            this.setNull(parameterIndex, -1);
        }
        else {
            this.setNCharacterStream(parameterIndex, reader, length);
        }
    }
    
    @Override
    public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    public ParameterBindings getParameterBindings() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return new EmulatedPreparedStatementBindings();
        }
    }
    
    public String getPreparedSql() {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                if (this.rewrittenBatchSize == 0) {
                    return this.originalSql;
                }
                try {
                    return this.parseInfo.getSqlForBatch(this.parseInfo);
                }
                catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        catch (SQLException e2) {
            throw new RuntimeException(e2);
        }
    }
    
    @Override
    public int getUpdateCount() throws SQLException {
        int count = super.getUpdateCount();
        if (this.containsOnDuplicateKeyUpdateInSQL() && this.compensateForOnDuplicateKeyUpdate && (count == 2 || count == 0)) {
            count = 1;
        }
        return count;
    }
    
    protected static boolean canRewrite(final String sql, final boolean isOnDuplicateKeyUpdate, final int locationOfOnDuplicateKeyUpdate, final int statementStartPos) {
        if (!StringUtils.startsWithIgnoreCaseAndWs(sql, "INSERT", statementStartPos)) {
            return StringUtils.startsWithIgnoreCaseAndWs(sql, "REPLACE", statementStartPos) && StringUtils.indexOfIgnoreCase(statementStartPos, sql, "SELECT", "\"'`", "\"'`", StringUtils.SEARCH_MODE__MRK_COM_WS) == -1;
        }
        if (StringUtils.indexOfIgnoreCase(statementStartPos, sql, "SELECT", "\"'`", "\"'`", StringUtils.SEARCH_MODE__MRK_COM_WS) != -1) {
            return false;
        }
        if (isOnDuplicateKeyUpdate) {
            final int updateClausePos = StringUtils.indexOfIgnoreCase(locationOfOnDuplicateKeyUpdate, sql, " UPDATE ");
            if (updateClausePos != -1) {
                return StringUtils.indexOfIgnoreCase(updateClausePos, sql, "LAST_INSERT_ID", "\"'`", "\"'`", StringUtils.SEARCH_MODE__MRK_COM_WS) == -1;
            }
        }
        return true;
    }
    
    @Override
    public long executeLargeUpdate() throws SQLException {
        return this.executeUpdateInternal(true, false);
    }
    
    public PrepareStatementStats getPsStats() {
        return this.psStats;
    }
    
    static {
        Label_0154: {
            if (Util.isJdbc4()) {
                try {
                    final String jdbc4ClassName = Util.isJdbc42() ? "com.alipay.oceanbase.jdbc.JDBC42PreparedStatement" : "com.alipay.oceanbase.jdbc.JDBC4PreparedStatement";
                    JDBC_4_PSTMT_2_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(MySQLConnection.class, String.class);
                    JDBC_4_PSTMT_3_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(MySQLConnection.class, String.class, String.class);
                    JDBC_4_PSTMT_4_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(MySQLConnection.class, String.class, String.class, ParseInfo.class);
                    break Label_0154;
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
            JDBC_4_PSTMT_2_ARG_CTOR = null;
            JDBC_4_PSTMT_3_ARG_CTOR = null;
            JDBC_4_PSTMT_4_ARG_CTOR = null;
        }
        HEX_DIGITS = new byte[] { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70 };
    }
    
    public class BatchParams
    {
        public boolean[] isNull;
        public boolean[] isStream;
        public InputStream[] parameterStreams;
        public byte[][] parameterStrings;
        public int[] streamLengths;
        
        BatchParams(final byte[][] strings, final InputStream[] streams, final boolean[] isStreamFlags, final int[] lengths, final boolean[] isNullFlags) {
            this.isNull = null;
            this.isStream = null;
            this.parameterStreams = null;
            this.parameterStrings = null;
            this.streamLengths = null;
            this.parameterStrings = new byte[strings.length][];
            this.parameterStreams = new InputStream[streams.length];
            this.isStream = new boolean[isStreamFlags.length];
            this.streamLengths = new int[lengths.length];
            this.isNull = new boolean[isNullFlags.length];
            System.arraycopy(strings, 0, this.parameterStrings, 0, strings.length);
            System.arraycopy(streams, 0, this.parameterStreams, 0, streams.length);
            System.arraycopy(isStreamFlags, 0, this.isStream, 0, isStreamFlags.length);
            System.arraycopy(lengths, 0, this.streamLengths, 0, lengths.length);
            System.arraycopy(isNullFlags, 0, this.isNull, 0, isNullFlags.length);
        }
    }
    
    class EndPoint
    {
        int begin;
        int end;
        
        EndPoint(final int b, final int e) {
            this.begin = b;
            this.end = e;
        }
    }
    
    public static final class ParseInfo
    {
        char firstStmtChar;
        boolean foundLoadData;
        long lastUsed;
        int statementLength;
        int statementStartPos;
        boolean canRewriteAsMultiValueInsert;
        byte[][] staticSql;
        boolean isOnDuplicateKeyUpdate;
        int locationOfOnDuplicateKeyUpdate;
        String valuesClause;
        boolean parametersInDuplicateKeyClause;
        String charEncoding;
        private ParseInfo batchHead;
        private ParseInfo batchValues;
        private ParseInfo batchODKUClause;
        
        ParseInfo(final String sql, final MySQLConnection conn, final DatabaseMetaData dbmd, final String encoding, final SingleByteCharsetConverter converter, final PrepareStatementStats psStats) throws SQLException {
            this(sql, conn, dbmd, encoding, converter, true, psStats);
        }
        
        public ParseInfo(final String sql, final MySQLConnection conn, final DatabaseMetaData dbmd, final String encoding, final SingleByteCharsetConverter converter, final boolean buildRewriteInfo, final PrepareStatementStats psStats) throws SQLException {
            this.firstStmtChar = '\0';
            this.foundLoadData = false;
            this.lastUsed = 0L;
            this.statementLength = 0;
            this.statementStartPos = 0;
            this.canRewriteAsMultiValueInsert = false;
            this.staticSql = null;
            this.isOnDuplicateKeyUpdate = false;
            this.locationOfOnDuplicateKeyUpdate = -1;
            this.parametersInDuplicateKeyClause = false;
            final long startNs = System.nanoTime();
            try {
                if (sql == null) {
                    throw SQLError.createSQLException(Messages.getString("PreparedStatement.61"), "S1009", conn.getExceptionInterceptor());
                }
                this.charEncoding = encoding;
                this.lastUsed = System.currentTimeMillis();
                final String quotedIdentifierString = dbmd.getIdentifierQuoteString();
                char quotedIdentifierChar = '\0';
                if (quotedIdentifierString != null && !quotedIdentifierString.equals(" ") && quotedIdentifierString.length() > 0) {
                    quotedIdentifierChar = quotedIdentifierString.charAt(0);
                }
                this.statementLength = sql.length();
                final ArrayList<int[]> endpointList = new ArrayList<int[]>();
                boolean inQuotes = false;
                char quoteChar = '\0';
                boolean inQuotedId = false;
                int lastParmEnd = 0;
                final boolean noBackslashEscapes = conn.isNoBackslashEscapesSet();
                this.statementStartPos = StatementImpl.findStartOfStatement(sql);
                for (int i = this.statementStartPos; i < this.statementLength; ++i) {
                    char c = sql.charAt(i);
                    if (this.firstStmtChar == '\0' && Character.isLetter(c)) {
                        this.firstStmtChar = Character.toUpperCase(c);
                        if (this.firstStmtChar == 'I') {
                            this.locationOfOnDuplicateKeyUpdate = StatementImpl.getOnDuplicateKeyLocation(sql, conn.getDontCheckOnDuplicateKeyUpdateInSQL(), conn.getRewriteBatchedStatements(), conn.isNoBackslashEscapesSet());
                            this.isOnDuplicateKeyUpdate = (this.locationOfOnDuplicateKeyUpdate != -1);
                        }
                    }
                    if (!noBackslashEscapes && c == '\\' && i < this.statementLength - 1) {
                        if (quoteChar != '\'') {
                            ++i;
                        }
                    }
                    else {
                        if (!inQuotes && quotedIdentifierChar != '\0' && c == quotedIdentifierChar) {
                            inQuotedId = !inQuotedId;
                        }
                        else if (!inQuotedId) {
                            if (inQuotes) {
                                if ((c == '\'' || c == '\"') && c == quoteChar) {
                                    if (i < this.statementLength - 1 && sql.charAt(i + 1) == quoteChar) {
                                        ++i;
                                        continue;
                                    }
                                    inQuotes = !inQuotes;
                                    quoteChar = '\0';
                                }
                                else if ((c == '\'' || c == '\"') && c == quoteChar) {
                                    inQuotes = !inQuotes;
                                    quoteChar = '\0';
                                }
                            }
                            else {
                                if ((c == '#' && !conn.getIO().isOracleMode()) || (c == '-' && i + 1 < this.statementLength && sql.charAt(i + 1) == '-')) {
                                    for (int endOfStmt = this.statementLength - 1; i < endOfStmt; ++i) {
                                        c = sql.charAt(i);
                                        if (c == '\r') {
                                            break;
                                        }
                                        if (c == '\n') {
                                            break;
                                        }
                                    }
                                    continue;
                                }
                                if (c == '/' && i + 1 < this.statementLength) {
                                    char cNext = sql.charAt(i + 1);
                                    if (cNext == '*') {
                                        i += 2;
                                        int j = i;
                                        while (j < this.statementLength) {
                                            ++i;
                                            cNext = sql.charAt(j);
                                            if (cNext == '*' && j + 1 < this.statementLength && sql.charAt(j + 1) == '/') {
                                                if (++i < this.statementLength) {
                                                    c = sql.charAt(i);
                                                    break;
                                                }
                                                break;
                                            }
                                            else {
                                                ++j;
                                            }
                                        }
                                    }
                                }
                                else if (c == '\'' || c == '\"') {
                                    inQuotes = true;
                                    quoteChar = c;
                                }
                            }
                        }
                        if (c == '?' && !inQuotes && !inQuotedId) {
                            endpointList.add(new int[] { lastParmEnd, i });
                            lastParmEnd = i + 1;
                            if (this.isOnDuplicateKeyUpdate && i > this.locationOfOnDuplicateKeyUpdate) {
                                this.parametersInDuplicateKeyClause = true;
                            }
                        }
                    }
                }
                if (this.firstStmtChar == 'L') {
                    if (StringUtils.startsWithIgnoreCaseAndWs(sql, "LOAD DATA")) {
                        this.foundLoadData = true;
                    }
                    else {
                        this.foundLoadData = false;
                    }
                }
                else {
                    this.foundLoadData = false;
                }
                endpointList.add(new int[] { lastParmEnd, this.statementLength });
                this.staticSql = new byte[endpointList.size()][];
                for (int i = 0; i < this.staticSql.length; ++i) {
                    final int[] ep = endpointList.get(i);
                    final int end = ep[1];
                    final int begin = ep[0];
                    final int len = end - begin;
                    if (this.foundLoadData) {
                        this.staticSql[i] = StringUtils.getBytes(sql, begin, len);
                    }
                    else if (encoding == null) {
                        final byte[] buf = new byte[len];
                        for (int k = 0; k < len; ++k) {
                            buf[k] = (byte)sql.charAt(begin + k);
                        }
                        this.staticSql[i] = buf;
                    }
                    else if (converter != null) {
                        this.staticSql[i] = StringUtils.getBytes(sql, converter, encoding, conn.getServerCharset(), begin, len, conn.parserKnowsUnicode(), conn.getExceptionInterceptor());
                    }
                    else {
                        this.staticSql[i] = StringUtils.getBytes(sql, encoding, conn.getServerCharset(), begin, len, conn.parserKnowsUnicode(), conn, conn.getExceptionInterceptor());
                    }
                }
            }
            catch (StringIndexOutOfBoundsException oobEx) {
                final SQLException sqlEx = new SQLException("Parse error for " + sql);
                sqlEx.initCause(oobEx);
                throw sqlEx;
            }
            if (buildRewriteInfo) {
                this.canRewriteAsMultiValueInsert = (PreparedStatement.canRewrite(sql, this.isOnDuplicateKeyUpdate, this.locationOfOnDuplicateKeyUpdate, this.statementStartPos) && !this.parametersInDuplicateKeyClause);
                if (this.canRewriteAsMultiValueInsert && conn.getRewriteBatchedStatements()) {
                    this.buildRewriteBatchedParams(sql, conn, dbmd, encoding, converter, psStats);
                }
            }
            psStats.addParseInfoCostNs(System.nanoTime() - startNs);
        }
        
        private void buildRewriteBatchedParams(final String sql, final MySQLConnection conn, final DatabaseMetaData metadata, final String encoding, final SingleByteCharsetConverter converter, final PrepareStatementStats psStats) throws SQLException {
            this.valuesClause = this.extractValuesClause(sql, conn.getMetaData().getIdentifierQuoteString());
            final String odkuClause = this.isOnDuplicateKeyUpdate ? sql.substring(this.locationOfOnDuplicateKeyUpdate) : null;
            String headSql = null;
            if (this.isOnDuplicateKeyUpdate) {
                headSql = sql.substring(0, this.locationOfOnDuplicateKeyUpdate);
            }
            else {
                headSql = sql;
            }
            this.batchHead = new ParseInfo(headSql, conn, metadata, encoding, converter, false, psStats);
            this.batchValues = new ParseInfo("," + this.valuesClause, conn, metadata, encoding, converter, false, psStats);
            this.batchODKUClause = null;
            if (odkuClause != null && odkuClause.length() > 0) {
                this.batchODKUClause = new ParseInfo("," + this.valuesClause + " " + odkuClause, conn, metadata, encoding, converter, false, psStats);
            }
        }
        
        private String extractValuesClause(final String sql, final String quoteCharStr) throws SQLException {
            int indexOfValues = -1;
            int valuesSearchStart = this.statementStartPos;
            while (indexOfValues == -1) {
                if (quoteCharStr.length() > 0) {
                    indexOfValues = StringUtils.indexOfIgnoreCase(valuesSearchStart, sql, "VALUES", quoteCharStr, quoteCharStr, StringUtils.SEARCH_MODE__MRK_COM_WS);
                }
                else {
                    indexOfValues = StringUtils.indexOfIgnoreCase(valuesSearchStart, sql, "VALUES");
                }
                if (indexOfValues <= 0) {
                    break;
                }
                char c = sql.charAt(indexOfValues - 1);
                if (!Character.isWhitespace(c) && c != ')' && c != '`') {
                    valuesSearchStart = indexOfValues + 6;
                    indexOfValues = -1;
                }
                else {
                    c = sql.charAt(indexOfValues + 6);
                    if (Character.isWhitespace(c) || c == '(') {
                        continue;
                    }
                    valuesSearchStart = indexOfValues + 6;
                    indexOfValues = -1;
                }
            }
            if (indexOfValues == -1) {
                return null;
            }
            final int indexOfFirstParen = sql.indexOf(40, indexOfValues + 6);
            if (indexOfFirstParen == -1) {
                return null;
            }
            int endOfValuesClause = sql.lastIndexOf(41);
            if (endOfValuesClause == -1) {
                return null;
            }
            if (this.isOnDuplicateKeyUpdate) {
                endOfValuesClause = this.locationOfOnDuplicateKeyUpdate - 1;
            }
            return sql.substring(indexOfFirstParen, endOfValuesClause + 1);
        }
        
        synchronized ParseInfo getParseInfoForBatch(final int numBatch) {
            final AppendingBatchVisitor apv = new AppendingBatchVisitor();
            this.buildInfoForBatch(numBatch, apv);
            final ParseInfo batchParseInfo = new ParseInfo(apv.getStaticSqlStrings(), this.firstStmtChar, this.foundLoadData, this.isOnDuplicateKeyUpdate, this.locationOfOnDuplicateKeyUpdate, this.statementLength, this.statementStartPos);
            return batchParseInfo;
        }
        
        String getSqlForBatch(final int numBatch) throws UnsupportedEncodingException {
            final ParseInfo batchInfo = this.getParseInfoForBatch(numBatch);
            return this.getSqlForBatch(batchInfo);
        }
        
        String getSqlForBatch(final ParseInfo batchInfo) throws UnsupportedEncodingException {
            int size = 0;
            final byte[][] sqlStrings = batchInfo.staticSql;
            final int sqlStringsLength = sqlStrings.length;
            for (int i = 0; i < sqlStringsLength; ++i) {
                size += sqlStrings[i].length;
                ++size;
            }
            final StringBuilder buf = new StringBuilder(size);
            for (int j = 0; j < sqlStringsLength - 1; ++j) {
                buf.append(StringUtils.toString(sqlStrings[j], this.charEncoding));
                buf.append("?");
            }
            buf.append(StringUtils.toString(sqlStrings[sqlStringsLength - 1]));
            return buf.toString();
        }
        
        private void buildInfoForBatch(final int numBatch, final BatchVisitor visitor) {
            final byte[][] headStaticSql = this.batchHead.staticSql;
            final int headStaticSqlLength = headStaticSql.length;
            if (headStaticSqlLength > 1) {
                for (int i = 0; i < headStaticSqlLength - 1; ++i) {
                    visitor.append(headStaticSql[i]).increment();
                }
            }
            final byte[] endOfHead = headStaticSql[headStaticSqlLength - 1];
            final byte[][] valuesStaticSql = this.batchValues.staticSql;
            final byte[] beginOfValues = valuesStaticSql[0];
            visitor.merge(endOfHead, beginOfValues).increment();
            int numValueRepeats = numBatch - 1;
            if (this.batchODKUClause != null) {
                --numValueRepeats;
            }
            final int valuesStaticSqlLength = valuesStaticSql.length;
            final byte[] endOfValues = valuesStaticSql[valuesStaticSqlLength - 1];
            for (int j = 0; j < numValueRepeats; ++j) {
                for (int k = 1; k < valuesStaticSqlLength - 1; ++k) {
                    visitor.append(valuesStaticSql[k]).increment();
                }
                visitor.merge(endOfValues, beginOfValues).increment();
            }
            if (this.batchODKUClause != null) {
                final byte[][] batchOdkuStaticSql = this.batchODKUClause.staticSql;
                final byte[] beginOfOdku = batchOdkuStaticSql[0];
                visitor.decrement().merge(endOfValues, beginOfOdku).increment();
                final int batchOdkuStaticSqlLength = batchOdkuStaticSql.length;
                if (numBatch > 1) {
                    for (int l = 1; l < batchOdkuStaticSqlLength; ++l) {
                        visitor.append(batchOdkuStaticSql[l]).increment();
                    }
                }
                else {
                    visitor.decrement().append(batchOdkuStaticSql[batchOdkuStaticSqlLength - 1]);
                }
            }
            else {
                visitor.decrement().append(this.staticSql[this.staticSql.length - 1]);
            }
        }
        
        private ParseInfo(final byte[][] staticSql, final char firstStmtChar, final boolean foundLoadData, final boolean isOnDuplicateKeyUpdate, final int locationOfOnDuplicateKeyUpdate, final int statementLength, final int statementStartPos) {
            this.firstStmtChar = '\0';
            this.foundLoadData = false;
            this.lastUsed = 0L;
            this.statementLength = 0;
            this.statementStartPos = 0;
            this.canRewriteAsMultiValueInsert = false;
            this.staticSql = null;
            this.isOnDuplicateKeyUpdate = false;
            this.locationOfOnDuplicateKeyUpdate = -1;
            this.parametersInDuplicateKeyClause = false;
            this.firstStmtChar = firstStmtChar;
            this.foundLoadData = foundLoadData;
            this.isOnDuplicateKeyUpdate = isOnDuplicateKeyUpdate;
            this.locationOfOnDuplicateKeyUpdate = locationOfOnDuplicateKeyUpdate;
            this.statementLength = statementLength;
            this.statementStartPos = statementStartPos;
            this.staticSql = staticSql;
        }
    }
    
    static class AppendingBatchVisitor implements BatchVisitor
    {
        LinkedList<byte[]> statementComponents;
        
        AppendingBatchVisitor() {
            this.statementComponents = new LinkedList<byte[]>();
        }
        
        @Override
        public BatchVisitor append(final byte[] values) {
            this.statementComponents.addLast(values);
            return this;
        }
        
        @Override
        public BatchVisitor increment() {
            return this;
        }
        
        @Override
        public BatchVisitor decrement() {
            this.statementComponents.removeLast();
            return this;
        }
        
        @Override
        public BatchVisitor merge(final byte[] front, final byte[] back) {
            final int mergedLength = front.length + back.length;
            final byte[] merged = new byte[mergedLength];
            System.arraycopy(front, 0, merged, 0, front.length);
            System.arraycopy(back, 0, merged, front.length, back.length);
            this.statementComponents.addLast(merged);
            return this;
        }
        
        public byte[][] getStaticSqlStrings() {
            final byte[][] asBytes = new byte[this.statementComponents.size()][];
            this.statementComponents.toArray(asBytes);
            return asBytes;
        }
        
        @Override
        public String toString() {
            final StringBuilder buf = new StringBuilder();
            final Iterator<byte[]> iter = this.statementComponents.iterator();
            while (iter.hasNext()) {
                buf.append(StringUtils.toString(iter.next()));
            }
            return buf.toString();
        }
    }
    
    class EmulatedPreparedStatementBindings implements ParameterBindings
    {
        private ResultSetImpl bindingsAsRs;
        private boolean[] parameterIsNull;
        
        EmulatedPreparedStatementBindings() throws SQLException {
            final List<ResultSetRow> rows = new ArrayList<ResultSetRow>();
            this.parameterIsNull = new boolean[PreparedStatement.this.parameterCount];
            System.arraycopy(PreparedStatement.this.isNull, 0, this.parameterIsNull, 0, PreparedStatement.this.parameterCount);
            final byte[][] rowData = new byte[PreparedStatement.this.parameterCount][];
            final Field[] typeMetadata = new Field[PreparedStatement.this.parameterCount];
            for (int i = 0; i < PreparedStatement.this.parameterCount; ++i) {
                if (PreparedStatement.this.batchCommandIndex == -1) {
                    rowData[i] = PreparedStatement.this.getBytesRepresentation(i);
                }
                else {
                    rowData[i] = PreparedStatement.this.getBytesRepresentationForBatch(i, PreparedStatement.this.batchCommandIndex);
                }
                int charsetIndex = 0;
                if (PreparedStatement.this.parameterTypes[i] == -2 || PreparedStatement.this.parameterTypes[i] == 2004) {
                    charsetIndex = 63;
                }
                else {
                    try {
                        charsetIndex = CharsetMapping.getCollationIndexForJavaEncoding(PreparedStatement.this.connection.getEncoding(), PreparedStatement.this.connection);
                    }
                    catch (SQLException ex) {
                        throw ex;
                    }
                    catch (RuntimeException ex2) {
                        final SQLException sqlEx = SQLError.createSQLException(ex2.toString(), "S1009", null);
                        sqlEx.initCause(ex2);
                        throw sqlEx;
                    }
                }
                final Field parameterMetadata = new Field(null, "parameter_" + (i + 1), charsetIndex, PreparedStatement.this.parameterTypes[i], rowData[i].length);
                parameterMetadata.setConnection(PreparedStatement.this.connection);
                typeMetadata[i] = parameterMetadata;
            }
            rows.add(new ByteArrayRow(rowData, PreparedStatement.this.getExceptionInterceptor()));
            (this.bindingsAsRs = new ResultSetImpl(PreparedStatement.this.connection.getCatalog(), typeMetadata, new RowDataStatic(rows), PreparedStatement.this.connection, null)).next();
        }
        
        @Override
        public Array getArray(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getArray(parameterIndex);
        }
        
        @Override
        public InputStream getAsciiStream(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getAsciiStream(parameterIndex);
        }
        
        @Override
        public BigDecimal getBigDecimal(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBigDecimal(parameterIndex);
        }
        
        @Override
        public InputStream getBinaryStream(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBinaryStream(parameterIndex);
        }
        
        @Override
        public Blob getBlob(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBlob(parameterIndex);
        }
        
        @Override
        public boolean getBoolean(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBoolean(parameterIndex);
        }
        
        @Override
        public byte getByte(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getByte(parameterIndex);
        }
        
        @Override
        public byte[] getBytes(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getBytes(parameterIndex);
        }
        
        @Override
        public Reader getCharacterStream(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getCharacterStream(parameterIndex);
        }
        
        @Override
        public Clob getClob(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getClob(parameterIndex);
        }
        
        @Override
        public java.sql.Date getDate(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getDate(parameterIndex);
        }
        
        @Override
        public double getDouble(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getDouble(parameterIndex);
        }
        
        @Override
        public float getFloat(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getFloat(parameterIndex);
        }
        
        @Override
        public int getInt(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getInt(parameterIndex);
        }
        
        @Override
        public long getLong(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getLong(parameterIndex);
        }
        
        @Override
        public Reader getNCharacterStream(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getCharacterStream(parameterIndex);
        }
        
        @Override
        public Reader getNClob(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getCharacterStream(parameterIndex);
        }
        
        @Override
        public Object getObject(final int parameterIndex) throws SQLException {
            PreparedStatement.this.checkBounds(parameterIndex, 0);
            if (this.parameterIsNull[parameterIndex - 1]) {
                return null;
            }
            switch (PreparedStatement.this.parameterTypes[parameterIndex - 1]) {
                case -6: {
                    return this.getByte(parameterIndex);
                }
                case 5: {
                    return this.getShort(parameterIndex);
                }
                case 4: {
                    return this.getInt(parameterIndex);
                }
                case -5: {
                    return this.getLong(parameterIndex);
                }
                case 6: {
                    return this.getFloat(parameterIndex);
                }
                case 8: {
                    return this.getDouble(parameterIndex);
                }
                default: {
                    return this.bindingsAsRs.getObject(parameterIndex);
                }
            }
        }
        
        @Override
        public Ref getRef(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getRef(parameterIndex);
        }
        
        @Override
        public short getShort(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getShort(parameterIndex);
        }
        
        @Override
        public String getString(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getString(parameterIndex);
        }
        
        @Override
        public Time getTime(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getTime(parameterIndex);
        }
        
        @Override
        public Timestamp getTimestamp(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getTimestamp(parameterIndex);
        }
        
        @Override
        public URL getURL(final int parameterIndex) throws SQLException {
            return this.bindingsAsRs.getURL(parameterIndex);
        }
        
        @Override
        public boolean isNull(final int parameterIndex) throws SQLException {
            PreparedStatement.this.checkBounds(parameterIndex, 0);
            return this.parameterIsNull[parameterIndex - 1];
        }
    }
    
    interface BatchVisitor
    {
        BatchVisitor increment();
        
        BatchVisitor decrement();
        
        BatchVisitor append(final byte[] p0);
        
        BatchVisitor merge(final byte[] p0, final byte[] p1);
    }
}
