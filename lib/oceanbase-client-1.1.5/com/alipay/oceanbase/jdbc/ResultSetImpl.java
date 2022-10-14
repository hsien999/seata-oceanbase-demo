// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.RowId;
import java.sql.SQLXML;
import java.sql.NClob;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEvent;
import com.alipay.oceanbase.jdbc.util.MysqlCommonUtils;
import com.alipay.oceanbase.jdbc.jdk8.SqlTimeStampReflection;
import com.alipay.oceanbase.jdbc.jdk8.SqlTimeReflection;
import java.sql.Connection;
import java.io.UnsupportedEncodingException;
import com.alipay.oceanbase.jdbc.extend.datatype.NUMBER;
import com.alipay.oceanbase.jdbc.extend.datatype.INTERVALYM;
import com.alipay.oceanbase.jdbc.extend.datatype.INTERVALDS;
import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMPLTZ;
import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMPTZ;
import com.alipay.oceanbase.jdbc.jdk8.LocalDateTimeReflection;
import com.alipay.oceanbase.jdbc.jdk8.LocalTimeReflection;
import com.alipay.oceanbase.jdbc.jdk8.LocalDateReflection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Ref;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.sql.ResultSetMetaData;
import com.alipay.oceanbase.jdbc.extend.datatype.ComplexData;
import java.sql.ResultSet;
import java.util.StringTokenizer;
import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMP;
import com.alipay.oceanbase.jdbc.jdk8.SqlDateReflection;
import java.io.StringReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.io.InputStream;
import java.sql.Array;
import com.alipay.oceanbase.jdbc.extend.datatype.ComplexDataType;
import java.sql.Struct;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;
import com.alipay.oceanbase.jdbc.log.LogUtils;
import java.util.HashMap;
import java.sql.SQLException;
import java.math.BigInteger;
import java.util.TimeZone;
import java.sql.Statement;
import java.sql.SQLWarning;
import java.util.Calendar;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEventHandler;
import java.util.Map;
import java.lang.reflect.Constructor;

public class ResultSetImpl implements ResultSetInternalMethods, ObResultSet
{
    private static final Constructor<?> JDBC_4_RS_4_ARG_CTOR;
    private static final Constructor<?> JDBC_4_RS_5_ARG_CTOR;
    private static final Constructor<?> JDBC_4_UPD_RS_5_ARG_CTOR;
    protected static final double MIN_DIFF_PREC;
    protected static final double MAX_DIFF_PREC;
    static int resultCounter;
    protected String catalog;
    protected Map<String, Integer> columnLabelToIndex;
    protected Map<String, Integer> columnToIndexCache;
    protected boolean[] columnUsed;
    protected volatile MySQLConnection connection;
    protected long connectionId;
    protected int currentRow;
    protected boolean doingUpdates;
    protected ProfilerEventHandler eventSink;
    Calendar fastDefaultCal;
    Calendar fastClientCal;
    protected int fetchDirection;
    protected int fetchSize;
    protected Field[] fields;
    protected char firstCharOfQuery;
    protected Map<String, Integer> fullColumnNameToIndex;
    protected Map<String, Integer> columnNameToIndex;
    protected boolean hasBuiltIndexMapping;
    protected boolean isBinaryEncoded;
    protected boolean isClosed;
    protected ResultSetInternalMethods nextResultSet;
    protected boolean onInsertRow;
    protected StatementImpl owningStatement;
    protected String pointOfOrigin;
    protected boolean profileSql;
    protected boolean reallyResult;
    protected int resultId;
    protected int resultSetConcurrency;
    protected int resultSetType;
    protected RowData rowData;
    protected String serverInfo;
    PreparedStatement statementUsedForFetchingRows;
    protected ResultSetRow thisRow;
    protected long updateCount;
    protected long updateId;
    private boolean useStrictFloatingPoint;
    protected boolean useUsageAdvisor;
    protected SQLWarning warningChain;
    protected boolean wasNullFlag;
    protected Statement wrapperStatement;
    protected boolean retainOwningStatement;
    protected Calendar gmtCalendar;
    protected boolean useFastDateParsing;
    private boolean padCharsWithSpace;
    private boolean jdbcCompliantTruncationForReads;
    private boolean useFastIntParsing;
    private boolean useColumnNamesInFindColumn;
    private ExceptionInterceptor exceptionInterceptor;
    static final char[] EMPTY_SPACE;
    protected boolean onValidRow;
    private String invalidRowReason;
    protected boolean useLegacyDatetimeCode;
    private TimeZone serverTimeZoneTz;
    
    protected static BigInteger convertLongToUlong(final long longVal) {
        final byte[] asBytes = { (byte)(longVal >>> 56), (byte)(longVal >>> 48), (byte)(longVal >>> 40), (byte)(longVal >>> 32), (byte)(longVal >>> 24), (byte)(longVal >>> 16), (byte)(longVal >>> 8), (byte)(longVal & 0xFFL) };
        return new BigInteger(1, asBytes);
    }
    
    protected static ResultSetImpl getInstance(final long updateCount, final long updateID, final MySQLConnection conn, final StatementImpl creatorStmt) throws SQLException {
        if (!Util.isJdbc4()) {
            return new ResultSetImpl(updateCount, updateID, conn, creatorStmt);
        }
        return (ResultSetImpl)Util.handleNewInstance(ResultSetImpl.JDBC_4_RS_4_ARG_CTOR, new Object[] { updateCount, updateID, conn, creatorStmt }, conn.getExceptionInterceptor());
    }
    
    protected static ResultSetImpl getInstance(final String catalog, final Field[] fields, final RowData tuples, final MySQLConnection conn, final StatementImpl creatorStmt, final boolean isUpdatable) throws SQLException {
        if (!Util.isJdbc4()) {
            if (!isUpdatable) {
                return new ResultSetImpl(catalog, fields, tuples, conn, creatorStmt);
            }
            return new UpdatableResultSet(catalog, fields, tuples, conn, creatorStmt);
        }
        else {
            if (!isUpdatable) {
                return (ResultSetImpl)Util.handleNewInstance(ResultSetImpl.JDBC_4_RS_5_ARG_CTOR, new Object[] { catalog, fields, tuples, conn, creatorStmt }, conn.getExceptionInterceptor());
            }
            return (ResultSetImpl)Util.handleNewInstance(ResultSetImpl.JDBC_4_UPD_RS_5_ARG_CTOR, new Object[] { catalog, fields, tuples, conn, creatorStmt }, conn.getExceptionInterceptor());
        }
    }
    
    public ResultSetImpl(final long updateCount, final long updateID, final MySQLConnection conn, final StatementImpl creatorStmt) {
        this.catalog = null;
        this.columnLabelToIndex = null;
        this.columnToIndexCache = null;
        this.columnUsed = null;
        this.connectionId = 0L;
        this.currentRow = -1;
        this.doingUpdates = false;
        this.eventSink = null;
        this.fastDefaultCal = null;
        this.fastClientCal = null;
        this.fetchDirection = 1000;
        this.fetchSize = 0;
        this.fullColumnNameToIndex = null;
        this.columnNameToIndex = null;
        this.hasBuiltIndexMapping = false;
        this.isBinaryEncoded = false;
        this.isClosed = false;
        this.nextResultSet = null;
        this.onInsertRow = false;
        this.profileSql = false;
        this.reallyResult = false;
        this.resultSetConcurrency = 0;
        this.resultSetType = 0;
        this.serverInfo = null;
        this.thisRow = null;
        this.updateId = -1L;
        this.useStrictFloatingPoint = false;
        this.useUsageAdvisor = false;
        this.warningChain = null;
        this.wasNullFlag = false;
        this.gmtCalendar = null;
        this.useFastDateParsing = false;
        this.padCharsWithSpace = false;
        this.useFastIntParsing = true;
        this.onValidRow = false;
        this.invalidRowReason = null;
        this.updateCount = updateCount;
        this.updateId = updateID;
        this.reallyResult = false;
        this.fields = new Field[0];
        this.connection = conn;
        this.owningStatement = creatorStmt;
        this.retainOwningStatement = false;
        if (this.connection != null) {
            this.exceptionInterceptor = this.connection.getExceptionInterceptor();
            this.retainOwningStatement = this.connection.getRetainStatementAfterResultSetClose();
            this.connectionId = this.connection.getId();
            this.serverTimeZoneTz = this.connection.getServerTimezoneTZ();
            this.padCharsWithSpace = this.connection.getPadCharsWithSpace();
            this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
        }
    }
    
    public ResultSetImpl(final String catalog, final Field[] fields, final RowData tuples, final MySQLConnection conn, final StatementImpl creatorStmt) throws SQLException {
        this.catalog = null;
        this.columnLabelToIndex = null;
        this.columnToIndexCache = null;
        this.columnUsed = null;
        this.connectionId = 0L;
        this.currentRow = -1;
        this.doingUpdates = false;
        this.eventSink = null;
        this.fastDefaultCal = null;
        this.fastClientCal = null;
        this.fetchDirection = 1000;
        this.fetchSize = 0;
        this.fullColumnNameToIndex = null;
        this.columnNameToIndex = null;
        this.hasBuiltIndexMapping = false;
        this.isBinaryEncoded = false;
        this.isClosed = false;
        this.nextResultSet = null;
        this.onInsertRow = false;
        this.profileSql = false;
        this.reallyResult = false;
        this.resultSetConcurrency = 0;
        this.resultSetType = 0;
        this.serverInfo = null;
        this.thisRow = null;
        this.updateId = -1L;
        this.useStrictFloatingPoint = false;
        this.useUsageAdvisor = false;
        this.warningChain = null;
        this.wasNullFlag = false;
        this.gmtCalendar = null;
        this.useFastDateParsing = false;
        this.padCharsWithSpace = false;
        this.useFastIntParsing = true;
        this.onValidRow = false;
        this.invalidRowReason = null;
        this.connection = conn;
        this.retainOwningStatement = false;
        if (this.connection != null) {
            this.exceptionInterceptor = this.connection.getExceptionInterceptor();
            this.useStrictFloatingPoint = this.connection.getStrictFloatingPoint();
            this.connectionId = this.connection.getId();
            this.useFastDateParsing = this.connection.getUseFastDateParsing();
            this.profileSql = this.connection.getProfileSql();
            this.retainOwningStatement = this.connection.getRetainStatementAfterResultSetClose();
            this.jdbcCompliantTruncationForReads = this.connection.getJdbcCompliantTruncationForReads();
            this.useFastIntParsing = this.connection.getUseFastIntParsing();
            this.serverTimeZoneTz = this.connection.getServerTimezoneTZ();
            this.padCharsWithSpace = this.connection.getPadCharsWithSpace();
        }
        this.owningStatement = creatorStmt;
        this.catalog = catalog;
        this.fields = fields;
        this.rowData = tuples;
        if (this.rowData != null) {
            this.updateCount = this.rowData.size();
        }
        else {
            this.updateCount = 0L;
        }
        this.reallyResult = true;
        if (this.rowData != null && this.rowData.size() > 0) {
            if (this.updateCount == 1L && this.thisRow == null) {
                this.rowData.close();
                this.updateCount = -1L;
            }
        }
        else {
            this.thisRow = null;
        }
        if (this.rowData != null) {
            this.rowData.setOwner(this);
        }
        if (this.fields != null) {
            this.initializeWithMetadata();
        }
        this.useLegacyDatetimeCode = this.connection.getUseLegacyDatetimeCode();
        this.useColumnNamesInFindColumn = this.connection.getUseColumnNamesInFindColumn();
        this.setRowPositionValidity();
    }
    
    @Override
    public void initializeWithMetadata() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.rowData.setMetadata(this.fields);
            this.columnToIndexCache = new HashMap<String, Integer>();
            if (this.profileSql || this.connection.getUseUsageAdvisor()) {
                this.columnUsed = new boolean[this.fields.length];
                this.pointOfOrigin = LogUtils.findCallingClassAndMethod(new Throwable());
                this.resultId = ResultSetImpl.resultCounter++;
                this.useUsageAdvisor = this.connection.getUseUsageAdvisor();
                this.eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
            }
            if (this.connection.getGatherPerformanceMetrics()) {
                this.connection.incrementNumberOfResultSetsCreated();
                final Set<String> tableNamesSet = new HashSet<String>();
                for (int i = 0; i < this.fields.length; ++i) {
                    final Field f = this.fields[i];
                    String tableName = f.getOriginalTableName();
                    if (tableName == null) {
                        tableName = f.getTableName();
                    }
                    if (tableName != null) {
                        if (this.connection.lowerCaseTableNames()) {
                            tableName = tableName.toLowerCase();
                        }
                        tableNamesSet.add(tableName);
                    }
                }
                this.connection.reportNumberOfTablesAccessed(tableNamesSet.size());
            }
        }
    }
    
    private synchronized Calendar getFastDefaultCalendar() {
        if (this.fastDefaultCal == null) {
            (this.fastDefaultCal = new GregorianCalendar(Locale.US)).setTimeZone(this.getDefaultTimeZone());
        }
        return this.fastDefaultCal;
    }
    
    private synchronized Calendar getFastClientCalendar() {
        if (this.fastClientCal == null) {
            this.fastClientCal = new GregorianCalendar(Locale.US);
        }
        return this.fastClientCal;
    }
    
    @Override
    public boolean absolute(int row) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            boolean b;
            if (this.rowData.size() == 0) {
                b = false;
            }
            else {
                if (this.onInsertRow) {
                    this.onInsertRow = false;
                }
                if (this.doingUpdates) {
                    this.doingUpdates = false;
                }
                if (this.thisRow != null) {
                    this.thisRow.closeOpenStreams();
                }
                if (row == 0) {
                    this.beforeFirst();
                    b = false;
                }
                else if (row == 1) {
                    b = this.first();
                }
                else if (row == -1) {
                    b = this.last();
                }
                else if (row > this.rowData.size()) {
                    this.afterLast();
                    b = false;
                }
                else if (row < 0) {
                    final int newRowPosition = this.rowData.size() + row + 1;
                    if (newRowPosition <= 0) {
                        this.beforeFirst();
                        b = false;
                    }
                    else {
                        b = this.absolute(newRowPosition);
                    }
                }
                else {
                    --row;
                    this.rowData.setCurrentRow(row);
                    this.thisRow = this.rowData.getAt(row);
                    b = true;
                }
            }
            this.setRowPositionValidity();
            return b;
        }
    }
    
    @Override
    public void afterLast() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.onInsertRow = false;
            }
            if (this.doingUpdates) {
                this.doingUpdates = false;
            }
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            if (this.rowData.size() != 0) {
                this.rowData.afterLast();
                this.thisRow = null;
            }
            this.setRowPositionValidity();
        }
    }
    
    @Override
    public void beforeFirst() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.onInsertRow = false;
            }
            if (this.doingUpdates) {
                this.doingUpdates = false;
            }
            if (this.rowData.size() == 0) {
                return;
            }
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            this.rowData.beforeFirst();
            this.thisRow = null;
            this.setRowPositionValidity();
        }
    }
    
    @Override
    public void buildIndexMapping() throws SQLException {
        final int numFields = this.fields.length;
        this.columnLabelToIndex = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        this.fullColumnNameToIndex = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        this.columnNameToIndex = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
        for (int i = numFields - 1; i >= 0; --i) {
            final Integer index = i;
            final String columnName = this.fields[i].getOriginalName();
            final String columnLabel = this.fields[i].getName();
            final String fullColumnName = this.fields[i].getFullName();
            if (columnLabel != null) {
                this.columnLabelToIndex.put(columnLabel, index);
            }
            if (fullColumnName != null) {
                this.fullColumnNameToIndex.put(fullColumnName, index);
            }
            if (columnName != null) {
                this.columnNameToIndex.put(columnName, index);
            }
        }
        this.hasBuiltIndexMapping = true;
    }
    
    @Override
    public void cancelRowUpdates() throws SQLException {
        throw new NotUpdatable();
    }
    
    protected final MySQLConnection checkClosed() throws SQLException {
        final MySQLConnection c = this.connection;
        if (c == null) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Operation_not_allowed_after_ResultSet_closed_144"), "S1000", this.getExceptionInterceptor());
        }
        return c;
    }
    
    protected final void checkColumnBounds(final int columnIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (columnIndex < 1) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Column_Index_out_of_range_low", new Object[] { columnIndex, this.fields.length }), "S1009", this.getExceptionInterceptor());
            }
            if (columnIndex > this.fields.length) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Column_Index_out_of_range_high", new Object[] { columnIndex, this.fields.length }), "S1009", this.getExceptionInterceptor());
            }
            if (this.profileSql || this.useUsageAdvisor) {
                this.columnUsed[columnIndex - 1] = true;
            }
        }
    }
    
    protected void checkRowPos() throws SQLException {
        this.checkClosed();
        if (!this.onValidRow) {
            throw SQLError.createSQLException(this.invalidRowReason, "S1000", this.getExceptionInterceptor());
        }
    }
    
    private void setRowPositionValidity() throws SQLException {
        if (this.rowData == null) {
            return;
        }
        if (!this.rowData.isDynamic() && this.rowData.size() == 0) {
            this.invalidRowReason = Messages.getString("ResultSet.Illegal_operation_on_empty_result_set");
            this.onValidRow = false;
        }
        else if (this.rowData.isBeforeFirst()) {
            this.invalidRowReason = Messages.getString("ResultSet.Before_start_of_result_set_146");
            this.onValidRow = false;
        }
        else if (this.rowData.isAfterLast()) {
            this.invalidRowReason = Messages.getString("ResultSet.After_end_of_result_set_148");
            this.onValidRow = false;
        }
        else {
            this.onValidRow = true;
            this.invalidRowReason = null;
        }
    }
    
    protected Blob getLobBlobObject(final byte[] data, final String encoding, final MySQLConnection conn, final ExceptionInterceptor exceptionInterceptor) {
        if (null == data) {
            return null;
        }
        Blob blob = new Blob(true, data, encoding, conn, exceptionInterceptor);
        if (null == blob.getLocator()) {
            blob = null;
        }
        return blob;
    }
    
    protected Clob getLobClobObject(final byte[] data, final String encoding, final MySQLConnection conn, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        if (null == data) {
            return null;
        }
        Clob clob = new Clob(true, data, encoding, conn, exceptionInterceptor);
        if (null == clob.getLocator()) {
            clob = null;
        }
        return clob;
    }
    
    @Override
    public synchronized void clearNextResult() {
        this.nextResultSet = null;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.warningChain = null;
        }
    }
    
    @Override
    public void close() throws SQLException {
        this.realClose(true);
    }
    
    private int convertToZeroWithEmptyCheck() throws SQLException {
        if (this.connection.getEmptyStringsConvertToZero()) {
            return 0;
        }
        throw SQLError.createSQLException("Can't convert empty string ('') to numeric", "22018", this.getExceptionInterceptor());
    }
    
    private String convertToZeroLiteralStringWithEmptyCheck() throws SQLException {
        if (this.connection.getEmptyStringsConvertToZero()) {
            return "0";
        }
        throw SQLError.createSQLException("Can't convert empty string ('') to numeric", "22018", this.getExceptionInterceptor());
    }
    
    @Override
    public ResultSetInternalMethods copy() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final ResultSetInternalMethods rs = getInstance(this.catalog, this.fields, this.rowData, this.connection, this.owningStatement, false);
            return rs;
        }
    }
    
    @Override
    public void redefineFieldsForDBMD(final Field[] f) {
        this.fields = f;
        for (int i = 0; i < this.fields.length; ++i) {
            this.fields[i].setUseOldNameMetadata(true);
            this.fields[i].setConnection(this.connection);
        }
    }
    
    @Override
    public void populateCachedMetaData(final CachedResultSetMetaData cachedMetaData) throws SQLException {
        cachedMetaData.fields = this.fields;
        cachedMetaData.columnNameToIndex = this.columnLabelToIndex;
        cachedMetaData.fullColumnNameToIndex = this.fullColumnNameToIndex;
        cachedMetaData.metadata = this.getMetaData();
    }
    
    @Override
    public void initializeFromCachedMetaData(final CachedResultSetMetaData cachedMetaData) {
        this.fields = cachedMetaData.fields;
        this.columnLabelToIndex = cachedMetaData.columnNameToIndex;
        this.fullColumnNameToIndex = cachedMetaData.fullColumnNameToIndex;
        this.hasBuiltIndexMapping = true;
    }
    
    @Override
    public void deleteRow() throws SQLException {
        throw new NotUpdatable();
    }
    
    public Field[] getFields() {
        return this.fields;
    }
    
    private String extractStringFromNativeColumn(final int columnIndex, final int mysqlType) throws SQLException {
        final int columnIndexMinusOne = columnIndex - 1;
        this.wasNullFlag = false;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        final String encoding = this.fields[columnIndexMinusOne].getEncoding();
        return this.thisRow.getString(columnIndex - 1, encoding, this.connection);
    }
    
    protected Date fastDateCreate(final Calendar cal, final int year, final int month, final int day) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            Calendar targetCalendar = cal;
            if (cal == null) {
                if (this.connection.getNoTimezoneConversionForDateType()) {
                    targetCalendar = this.getFastClientCalendar();
                }
                else {
                    targetCalendar = this.getFastDefaultCalendar();
                }
            }
            if (!this.useLegacyDatetimeCode) {
                return TimeUtil.fastDateCreate(year, month, day, targetCalendar);
            }
            final boolean useGmtMillis = cal == null && !this.connection.getNoTimezoneConversionForDateType() && this.connection.getUseGmtMillisForDatetimes();
            return TimeUtil.fastDateCreate(useGmtMillis, useGmtMillis ? this.getGmtCalendar() : targetCalendar, targetCalendar, year, month, day);
        }
    }
    
    protected Time fastTimeCreate(Calendar cal, final int hour, final int minute, final int second) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (!this.useLegacyDatetimeCode) {
                return TimeUtil.fastTimeCreate(hour, minute, second, cal, this.getExceptionInterceptor());
            }
            if (cal == null) {
                cal = this.getFastDefaultCalendar();
            }
            return TimeUtil.fastTimeCreate(cal, hour, minute, second, this.getExceptionInterceptor());
        }
    }
    
    protected Timestamp fastTimestampCreate(Calendar cal, final int year, final int month, final int day, final int hour, final int minute, final int seconds, final int secondsPart) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (!this.useLegacyDatetimeCode) {
                return TimeUtil.fastTimestampCreate(cal.getTimeZone(), year, month, day, hour, minute, seconds, secondsPart);
            }
            if (cal == null) {
                cal = this.getFastDefaultCalendar();
            }
            final boolean useGmtMillis = this.connection.getUseGmtMillisForDatetimes();
            return TimeUtil.fastTimestampCreate(useGmtMillis, useGmtMillis ? this.getGmtCalendar() : null, cal, year, month, day, hour, minute, seconds, secondsPart);
        }
    }
    
    @Override
    public int findColumn(final String columnName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (!this.hasBuiltIndexMapping) {
                this.buildIndexMapping();
            }
            Integer index = this.columnToIndexCache.get(columnName);
            if (index != null) {
                return index + 1;
            }
            index = this.columnLabelToIndex.get(columnName);
            if (index == null && this.useColumnNamesInFindColumn) {
                index = this.columnNameToIndex.get(columnName);
            }
            if (index == null) {
                index = this.fullColumnNameToIndex.get(columnName);
            }
            if (index != null) {
                this.columnToIndexCache.put(columnName, index);
                return index + 1;
            }
            for (int i = 0; i < this.fields.length; ++i) {
                if (this.fields[i].getName().equalsIgnoreCase(columnName)) {
                    return i + 1;
                }
                if (this.fields[i].getFullName().equalsIgnoreCase(columnName)) {
                    return i + 1;
                }
            }
            throw SQLError.createSQLException(Messages.getString("ResultSet.Column____112") + columnName + Messages.getString("ResultSet.___not_found._113"), "S0022", this.getExceptionInterceptor());
        }
    }
    
    @Override
    public boolean first() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            boolean b = true;
            if (this.rowData.isEmpty()) {
                b = false;
            }
            else {
                if (this.onInsertRow) {
                    this.onInsertRow = false;
                }
                if (this.doingUpdates) {
                    this.doingUpdates = false;
                }
                this.rowData.beforeFirst();
                this.thisRow = this.rowData.next();
            }
            this.setRowPositionValidity();
            return b;
        }
    }
    
    public Struct getStruct(final int columnIndex) throws SQLException {
        this.checkColumnBounds(columnIndex);
        if (!this.connection.getIO().isOracleMode()) {
            throw SQLError.createSQLFeatureNotSupportedException();
        }
        final int internalColumnIndex = columnIndex - 1;
        if (this.fields[internalColumnIndex].getMysqlType() != 160) {
            throw new SQLException("the field type is not FIELD_TYPE_COMPLEX");
        }
        final String typeName = this.fields[internalColumnIndex].getComplexTypeName();
        final ComplexDataType type = ((ConnectionImpl)this.connection).getComplexDataType(typeName);
        if (type.getType() != 3) {
            throw new SQLException("the field complex type is not TYPE_OBJECT");
        }
        final StructImpl struct = (StructImpl)this.thisRow.getComplexRowData(internalColumnIndex);
        return struct;
    }
    
    public Struct getStruct(final String colName) throws SQLException {
        return this.getStruct(this.findColumn(colName));
    }
    
    @Override
    public Array getArray(final int columnIndex) throws SQLException {
        this.checkColumnBounds(columnIndex);
        if (!this.connection.getIO().isOracleMode()) {
            throw SQLError.createSQLFeatureNotSupportedException();
        }
        final int internalColumnIndex = columnIndex - 1;
        ArrayImpl array = null;
        if (this.fields[internalColumnIndex].getMysqlType() != 160) {
            throw new SQLException("the field type is not FIELD_TYPE_COMPLEX");
        }
        final String typeName = this.fields[internalColumnIndex].getComplexTypeName();
        final ComplexDataType type = ((ConnectionImpl)this.connection).getComplexDataType(typeName);
        if (type.getType() != 4) {
            throw new SQLException("the field complex type is not TYPE_COLLECTION");
        }
        array = (ArrayImpl)this.thisRow.getComplexRowData(internalColumnIndex);
        return array;
    }
    
    @Override
    public Array getArray(final String colName) throws SQLException {
        return this.getArray(this.findColumn(colName));
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        this.checkRowPos();
        if (!this.isBinaryEncoded) {
            return this.getBinaryStream(columnIndex);
        }
        return this.getNativeBinaryStream(columnIndex);
    }
    
    @Override
    public InputStream getAsciiStream(final String columnName) throws SQLException {
        return this.getAsciiStream(this.findColumn(columnName));
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        if (!this.isBinaryEncoded) {
            final String stringVal = this.getString(columnIndex);
            if (stringVal != null) {
                if (stringVal.length() == 0) {
                    final BigDecimal val = new BigDecimal(this.convertToZeroLiteralStringWithEmptyCheck());
                    return val;
                }
                try {
                    final BigDecimal val = new BigDecimal(stringVal);
                    return val;
                }
                catch (NumberFormatException ex) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, columnIndex }), "S1009", this.getExceptionInterceptor());
                }
            }
            return null;
        }
        return this.getNativeBigDecimal(columnIndex);
    }
    
    @Deprecated
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        if (!this.isBinaryEncoded) {
            final String stringVal = this.getString(columnIndex);
            if (stringVal != null) {
                if (stringVal.length() == 0) {
                    final BigDecimal val = new BigDecimal(this.convertToZeroLiteralStringWithEmptyCheck());
                    try {
                        return val.setScale(scale);
                    }
                    catch (ArithmeticException ex) {
                        try {
                            return val.setScale(scale, 4);
                        }
                        catch (ArithmeticException arEx) {
                            throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, columnIndex }), "S1009", this.getExceptionInterceptor());
                        }
                    }
                }
                BigDecimal val;
                try {
                    val = new BigDecimal(stringVal);
                }
                catch (NumberFormatException ex2) {
                    if (this.fields[columnIndex - 1].getMysqlType() != 16) {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { columnIndex, stringVal }), "S1009", this.getExceptionInterceptor());
                    }
                    final long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
                    val = new BigDecimal(valueAsLong);
                }
                try {
                    return val.setScale(scale);
                }
                catch (ArithmeticException ex) {
                    try {
                        return val.setScale(scale, 4);
                    }
                    catch (ArithmeticException arithEx) {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { columnIndex, stringVal }), "S1009", this.getExceptionInterceptor());
                    }
                }
            }
            return null;
        }
        return this.getNativeBigDecimal(columnIndex, scale);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnName) throws SQLException {
        return this.getBigDecimal(this.findColumn(columnName));
    }
    
    @Deprecated
    @Override
    public BigDecimal getBigDecimal(final String columnName, final int scale) throws SQLException {
        return this.getBigDecimal(this.findColumn(columnName), scale);
    }
    
    private final BigDecimal getBigDecimalFromString(final String stringVal, final int columnIndex, final int scale) throws SQLException {
        if (stringVal != null) {
            Label_0084: {
                if (stringVal.length() != 0) {
                    break Label_0084;
                }
                BigDecimal bdVal = new BigDecimal(this.convertToZeroLiteralStringWithEmptyCheck());
                try {
                    if (scale > 127) {
                        return bdVal;
                    }
                    return bdVal.setScale(scale);
                }
                catch (ArithmeticException ex) {
                    try {
                        return bdVal.setScale(scale, 4);
                    }
                    catch (ArithmeticException arEx) {
                        throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, columnIndex }), "S1009");
                    }
                }
                try {
                    try {
                        if (scale > 127) {
                            bdVal = new BigDecimal(stringVal);
                        }
                        else {
                            bdVal = new BigDecimal(stringVal).setScale(scale);
                        }
                        return bdVal;
                    }
                    catch (ArithmeticException ex) {
                        try {
                            return new BigDecimal(stringVal).setScale(scale, 4);
                        }
                        catch (ArithmeticException arEx) {
                            throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, columnIndex }), "S1009");
                        }
                    }
                }
                catch (NumberFormatException ex2) {
                    if (this.fields[columnIndex - 1].getMysqlType() == 16) {
                        final long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
                        try {
                            return new BigDecimal(valueAsLong).setScale(scale);
                        }
                        catch (ArithmeticException arEx2) {
                            try {
                                return new BigDecimal(valueAsLong).setScale(scale, 4);
                            }
                            catch (ArithmeticException arEx3) {
                                throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, columnIndex }), "S1009");
                            }
                        }
                    }
                    if (this.fields[columnIndex - 1].getMysqlType() == 1 && this.connection.getTinyInt1isBit() && this.fields[columnIndex - 1].getLength() == 1L) {
                        return new BigDecimal(stringVal.equalsIgnoreCase("true") ? 1 : 0).setScale(scale);
                    }
                    throw new SQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, columnIndex }), "S1009");
                }
            }
        }
        return null;
    }
    
    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        final int columnIndexMinusOne = columnIndex - 1;
        final Field field = this.fields[columnIndexMinusOne];
        if (field != null) {
            switch (field.getMysqlType()) {
                case 210: {
                    final Blob blob = this.getLobBlobObject(this.thisRow.getColumnValue(columnIndexMinusOne), this.thisRow.metadata[columnIndexMinusOne].getEncoding(), this.connection, this.getExceptionInterceptor());
                    return (blob == null) ? null : blob.getBinaryStream();
                }
                case 211: {
                    final Clob clob = this.getLobClobObject(this.thisRow.getColumnValue(columnIndexMinusOne), this.thisRow.metadata[columnIndexMinusOne].getEncoding(), this.connection, this.getExceptionInterceptor());
                    return (clob == null) ? null : clob.getAsciiStream();
                }
            }
        }
        if (this.isBinaryEncoded) {
            return this.getNativeBinaryStream(columnIndex);
        }
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return this.thisRow.getBinaryInputStream(columnIndexMinusOne);
    }
    
    @Override
    public InputStream getBinaryStream(final String columnName) throws SQLException {
        return this.getBinaryStream(this.findColumn(columnName));
    }
    
    @Override
    public java.sql.Blob getBlob(final int columnIndex) throws SQLException {
        final int columnIndexMinusOne = columnIndex - 1;
        final Field field = this.fields[columnIndexMinusOne];
        if (field != null && field.getMysqlType() == 210) {
            return this.getLobBlobObject(this.thisRow.getColumnValue(columnIndexMinusOne), this.thisRow.metadata[columnIndexMinusOne].getEncoding(), this.connection, this.getExceptionInterceptor());
        }
        if (this.isBinaryEncoded) {
            return this.getNativeBlob(columnIndex);
        }
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
        }
        else {
            this.wasNullFlag = false;
        }
        if (this.wasNullFlag) {
            return null;
        }
        if (!this.connection.getEmulateLocators()) {
            return new Blob(this.thisRow.getColumnValue(columnIndexMinusOne), this.getExceptionInterceptor());
        }
        return new BlobFromLocator(this, columnIndex, this.getExceptionInterceptor());
    }
    
    @Override
    public java.sql.Blob getBlob(final String colName) throws SQLException {
        return this.getBlob(this.findColumn(colName));
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        this.checkColumnBounds(columnIndex);
        final int columnIndexMinusOne = columnIndex - 1;
        final Field field = this.fields[columnIndexMinusOne];
        if (field.getMysqlType() == 16) {
            return this.byteArrayToBoolean(columnIndexMinusOne);
        }
        this.wasNullFlag = false;
        final int sqlType = field.getSQLType();
        switch (sqlType) {
            case 16: {
                if (field.getMysqlType() == -1) {
                    final String stringVal = this.getString(columnIndex);
                    return this.getBooleanFromString(stringVal);
                }
                final long boolVal = this.getLong(columnIndex, false);
                return boolVal == -1L || boolVal > 0L;
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
                final long boolVal = this.getLong(columnIndex, false);
                return boolVal == -1L || boolVal > 0L;
            }
            default: {
                if (this.connection.getPedantic()) {
                    switch (sqlType) {
                        case -4:
                        case -3:
                        case -2:
                        case 70:
                        case 91:
                        case 92:
                        case 93:
                        case 2000:
                        case 2002:
                        case 2003:
                        case 2004:
                        case 2005:
                        case 2006: {
                            throw SQLError.createSQLException("Required type conversion not allowed", "22018", this.getExceptionInterceptor());
                        }
                    }
                }
                if (sqlType == -2 || sqlType == -3 || sqlType == -4 || sqlType == 2004) {
                    return this.byteArrayToBoolean(columnIndexMinusOne);
                }
                if (this.useUsageAdvisor) {
                    this.issueConversionViaParsingWarning("getBoolean()", columnIndex, this.thisRow.getColumnValue(columnIndexMinusOne), this.fields[columnIndex], new int[] { 16, 5, 1, 2, 3, 8, 4 });
                }
                final String stringVal2 = this.getString(columnIndex);
                return this.getBooleanFromString(stringVal2);
            }
        }
    }
    
    private boolean byteArrayToBoolean(final int columnIndexMinusOne) throws SQLException {
        final Object value = this.thisRow.getColumnValue(columnIndexMinusOne);
        if (value == null) {
            this.wasNullFlag = true;
            return false;
        }
        this.wasNullFlag = false;
        if (((byte[])value).length == 0) {
            return false;
        }
        final byte boolVal = ((byte[])value)[0];
        return boolVal == 49 || (boolVal != 48 && (boolVal == -1 || boolVal > 0));
    }
    
    @Override
    public boolean getBoolean(final String columnName) throws SQLException {
        return this.getBoolean(this.findColumn(columnName));
    }
    
    private final boolean getBooleanFromString(final String stringVal) throws SQLException {
        if (stringVal != null && stringVal.length() > 0) {
            final int c = Character.toLowerCase(stringVal.charAt(0));
            return c == 116 || c == 121 || c == 49 || stringVal.equals("-1");
        }
        return false;
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        if (this.isBinaryEncoded) {
            return this.getNativeByte(columnIndex);
        }
        final String stringVal = this.getString(columnIndex);
        if (this.wasNullFlag || stringVal == null) {
            return 0;
        }
        return this.getByteFromString(stringVal, columnIndex);
    }
    
    @Override
    public byte getByte(final String columnName) throws SQLException {
        return this.getByte(this.findColumn(columnName));
    }
    
    private final byte getByteFromString(String stringVal, final int columnIndex) throws SQLException {
        if (stringVal != null && stringVal.length() == 0) {
            return (byte)this.convertToZeroWithEmptyCheck();
        }
        if (stringVal == null) {
            return 0;
        }
        stringVal = stringVal.trim();
        try {
            final int decimalIndex = stringVal.indexOf(".");
            if (decimalIndex != -1) {
                final double valueAsDouble = Double.parseDouble(stringVal);
                if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -128.0 || valueAsDouble > 127.0)) {
                    this.throwRangeException(stringVal, columnIndex, -6);
                }
                return (byte)valueAsDouble;
            }
            final long valueAsLong = Long.parseLong(stringVal);
            if (this.jdbcCompliantTruncationForReads && (valueAsLong < -128L || valueAsLong > 127L)) {
                this.throwRangeException(String.valueOf(valueAsLong), columnIndex, -6);
            }
            return (byte)valueAsLong;
        }
        catch (NumberFormatException NFE) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Value____173") + stringVal + Messages.getString("ResultSet.___is_out_of_range_[-127,127]_174"), "S1009", this.getExceptionInterceptor());
        }
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return this.getBytes(columnIndex, false);
    }
    
    protected byte[] getBytes(final int columnIndex, final boolean noConversion) throws SQLException {
        this.checkColumnBounds(columnIndex);
        final int columnIndexMinusOne = columnIndex - 1;
        final Field field = this.fields[columnIndexMinusOne];
        if (field != null) {
            switch (field.getMysqlType()) {
                case 210: {
                    final Blob blob = this.getLobBlobObject(this.thisRow.getColumnValue(columnIndexMinusOne), this.thisRow.metadata[columnIndexMinusOne].getEncoding(), this.connection, this.getExceptionInterceptor());
                    return (byte[])((blob == null) ? null : blob.getBytes(1L, (int)blob.length()));
                }
                case 211: {
                    throw SQLError.createSQLFeatureNotSupportedException();
                }
            }
        }
        if (this.isBinaryEncoded) {
            return this.getNativeBytes(columnIndex, noConversion);
        }
        this.checkRowPos();
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
        }
        else {
            this.wasNullFlag = false;
        }
        if (this.wasNullFlag) {
            return null;
        }
        return this.thisRow.getColumnValue(columnIndexMinusOne);
    }
    
    @Override
    public byte[] getBytes(final String columnName) throws SQLException {
        return this.getBytes(this.findColumn(columnName));
    }
    
    private final byte[] getBytesFromString(final String stringVal) throws SQLException {
        if (stringVal != null) {
            return StringUtils.getBytes(stringVal, this.connection.getEncoding(), this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.connection, this.getExceptionInterceptor());
        }
        return null;
    }
    
    @Override
    public int getBytesSize() throws SQLException {
        final RowData localRowData = this.rowData;
        this.checkClosed();
        if (localRowData instanceof RowDataStatic) {
            int bytesSize = 0;
            for (int numRows = localRowData.size(), i = 0; i < numRows; ++i) {
                bytesSize += localRowData.getAt(i).getBytesSize();
            }
            return bytesSize;
        }
        return -1;
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
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        this.checkColumnBounds(columnIndex);
        final int columnIndexMinusOne = columnIndex - 1;
        final Field field = this.fields[columnIndexMinusOne];
        if (field != null) {
            switch (field.getMysqlType()) {
                case 210: {
                    final Blob blob = this.getLobBlobObject(this.thisRow.getColumnValue(columnIndexMinusOne), this.thisRow.metadata[columnIndexMinusOne].getEncoding(), this.connection, this.getExceptionInterceptor());
                    return (blob == null) ? null : blob.getCharacterStream();
                }
                case 211: {
                    final Clob clob = this.getLobClobObject(this.thisRow.getColumnValue(columnIndexMinusOne), this.thisRow.metadata[columnIndexMinusOne].getEncoding(), this.connection, this.getExceptionInterceptor());
                    return (clob == null) ? null : clob.getCharacterStream();
                }
            }
        }
        if (this.isBinaryEncoded) {
            return this.getNativeCharacterStream(columnIndex);
        }
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return this.thisRow.getReader(columnIndexMinusOne);
    }
    
    @Override
    public Reader getCharacterStream(final String columnName) throws SQLException {
        return this.getCharacterStream(this.findColumn(columnName));
    }
    
    private final Reader getCharacterStreamFromString(final String stringVal) throws SQLException {
        if (stringVal != null) {
            return new StringReader(stringVal);
        }
        return null;
    }
    
    @Override
    public java.sql.Clob getClob(final int i) throws SQLException {
        final int columnIndexMinusOne = i - 1;
        final Field field = this.fields[columnIndexMinusOne];
        if (field != null && field.getMysqlType() == 211) {
            return this.getLobClobObject(this.thisRow.getColumnValue(columnIndexMinusOne), this.thisRow.metadata[columnIndexMinusOne].getEncoding(), this.connection, this.getExceptionInterceptor());
        }
        if (this.isBinaryEncoded) {
            return this.getNativeClob(i);
        }
        final String asString = this.getStringForClob(i);
        if (asString == null) {
            return null;
        }
        return new Clob(asString, this.getExceptionInterceptor());
    }
    
    @Override
    public java.sql.Clob getClob(final String colName) throws SQLException {
        return this.getClob(this.findColumn(colName));
    }
    
    private final java.sql.Clob getClobFromString(final String stringVal) throws SQLException {
        return new Clob(stringVal, this.getExceptionInterceptor());
    }
    
    @Override
    public int getConcurrency() throws SQLException {
        return 1007;
    }
    
    @Override
    public String getCursorName() throws SQLException {
        throw SQLError.createSQLException(Messages.getString("ResultSet.Positioned_Update_not_supported"), "S1C00", this.getExceptionInterceptor());
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return this.getDate(columnIndex, null);
    }
    
    public Object getLocalDate(final int columnIndex) throws SQLException {
        return SqlDateReflection.toLocalDate((this.getDate(columnIndex, null) == null) ? null : this.getDate(columnIndex, null));
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        final int fieldType = this.fields[columnIndex - 1].getMysqlType();
        if (((ConnectionImpl)this.connection).isOracleMode()) {
            if (fieldType == 202 || fieldType == 201 || fieldType == 200) {
                final TIMESTAMP oracleTimestamp = this.getTIMESTAMP(columnIndex, fieldType);
                if (oracleTimestamp == null) {
                    this.wasNullFlag = true;
                    return null;
                }
                this.wasNullFlag = false;
                final Timestamp timestamp = oracleTimestamp.timestampValue(cal);
                return new Date(timestamp.getTime());
            }
            else if (fieldType == 12) {
                final Timestamp timestamp2 = this.getTimestamp(columnIndex);
                if (timestamp2 == null) {
                    this.wasNullFlag = true;
                    return null;
                }
                this.wasNullFlag = false;
                return new Date(timestamp2.getTime());
            }
        }
        if (this.isBinaryEncoded) {
            return this.getNativeDate(columnIndex, cal);
        }
        if (!this.useFastDateParsing) {
            final String stringVal = this.getStringInternal(columnIndex, false);
            if (stringVal == null) {
                return null;
            }
            return this.getDateFromString(stringVal, columnIndex, cal);
        }
        else {
            this.checkColumnBounds(columnIndex);
            final int columnIndexMinusOne = columnIndex - 1;
            final Date tmpDate = this.thisRow.getDateFast(columnIndexMinusOne, this.connection, this, cal);
            if (this.thisRow.isNull(columnIndexMinusOne) || tmpDate == null) {
                this.wasNullFlag = true;
                return null;
            }
            this.wasNullFlag = false;
            return tmpDate;
        }
    }
    
    public Object getLocalDate(final int columnIndex, final Calendar cal) throws SQLException {
        return SqlDateReflection.toLocalDate((this.getDate(columnIndex, cal) == null) ? null : this.getDate(columnIndex, cal));
    }
    
    @Override
    public Date getDate(final String columnName) throws SQLException {
        return this.getDate(this.findColumn(columnName));
    }
    
    public Object getLocalDate(final String columnName) throws SQLException {
        return SqlDateReflection.toLocalDate((this.getDate(this.findColumn(columnName)) == null) ? null : this.getDate(this.findColumn(columnName)));
    }
    
    @Override
    public Date getDate(final String columnName, final Calendar cal) throws SQLException {
        return this.getDate(this.findColumn(columnName), cal);
    }
    
    public Object getLocalDate(final String columnName, final Calendar cal) throws SQLException {
        return SqlDateReflection.toLocalDate((this.getDate(this.findColumn(columnName), cal) == null) ? null : this.getDate(this.findColumn(columnName), cal));
    }
    
    private final Date getDateFromString(String stringVal, final int columnIndex, final Calendar targetCalendar) throws SQLException {
        int year = 0;
        int month = 0;
        int day = 0;
        try {
            this.wasNullFlag = false;
            if (stringVal == null) {
                this.wasNullFlag = true;
                return null;
            }
            stringVal = stringVal.trim();
            final int dec = stringVal.indexOf(".");
            if (dec > -1) {
                stringVal = stringVal.substring(0, dec);
            }
            if (stringVal.equals("0") || stringVal.equals("0000-00-00") || stringVal.equals("0000-00-00 00:00:00") || stringVal.equals("00000000000000") || stringVal.equals("0")) {
                if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                    this.wasNullFlag = true;
                    return null;
                }
                if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                    throw SQLError.createSQLException("Value '" + stringVal + "' can not be represented as java.sql.Date", "S1009", this.getExceptionInterceptor());
                }
                return this.fastDateCreate(targetCalendar, 1, 1, 1);
            }
            else if (this.fields[columnIndex - 1].getMysqlType() == 7) {
                switch (stringVal.length()) {
                    case 19:
                    case 21: {
                        year = Integer.parseInt(stringVal.substring(0, 4));
                        month = Integer.parseInt(stringVal.substring(5, 7));
                        day = Integer.parseInt(stringVal.substring(8, 10));
                        return this.fastDateCreate(targetCalendar, year, month, day);
                    }
                    case 8:
                    case 14: {
                        year = Integer.parseInt(stringVal.substring(0, 4));
                        month = Integer.parseInt(stringVal.substring(4, 6));
                        day = Integer.parseInt(stringVal.substring(6, 8));
                        return this.fastDateCreate(targetCalendar, year, month, day);
                    }
                    case 6:
                    case 10:
                    case 12: {
                        year = Integer.parseInt(stringVal.substring(0, 2));
                        if (year <= 69) {
                            year += 100;
                        }
                        month = Integer.parseInt(stringVal.substring(2, 4));
                        day = Integer.parseInt(stringVal.substring(4, 6));
                        return this.fastDateCreate(targetCalendar, year + 1900, month, day);
                    }
                    case 4: {
                        year = Integer.parseInt(stringVal.substring(0, 4));
                        if (year <= 69) {
                            year += 100;
                        }
                        month = Integer.parseInt(stringVal.substring(2, 4));
                        return this.fastDateCreate(targetCalendar, year + 1900, month, 1);
                    }
                    case 2: {
                        year = Integer.parseInt(stringVal.substring(0, 2));
                        if (year <= 69) {
                            year += 100;
                        }
                        return this.fastDateCreate(targetCalendar, year + 1900, 1, 1);
                    }
                    default: {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[] { stringVal, columnIndex }), "S1009", this.getExceptionInterceptor());
                    }
                }
            }
            else {
                if (this.fields[columnIndex - 1].getMysqlType() == 13) {
                    if (stringVal.length() == 2 || stringVal.length() == 1) {
                        year = Integer.parseInt(stringVal);
                        if (year <= 69) {
                            year += 100;
                        }
                        year += 1900;
                    }
                    else {
                        year = Integer.parseInt(stringVal.substring(0, 4));
                    }
                    return this.fastDateCreate(targetCalendar, year, 1, 1);
                }
                if (this.fields[columnIndex - 1].getMysqlType() == 11) {
                    return this.fastDateCreate(targetCalendar, 1970, 1, 1);
                }
                if (stringVal.length() >= 10) {
                    if (stringVal.length() != 18) {
                        year = Integer.parseInt(stringVal.substring(0, 4));
                        month = Integer.parseInt(stringVal.substring(5, 7));
                        day = Integer.parseInt(stringVal.substring(8, 10));
                    }
                    else {
                        final StringTokenizer st = new StringTokenizer(stringVal, "- ");
                        year = Integer.parseInt(st.nextToken());
                        month = Integer.parseInt(st.nextToken());
                        day = Integer.parseInt(st.nextToken());
                    }
                    return this.fastDateCreate(targetCalendar, year, month, day);
                }
                if (stringVal.length() == 8) {
                    return this.fastDateCreate(targetCalendar, 1970, 1, 1);
                }
                throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[] { stringVal, columnIndex }), "S1009", this.getExceptionInterceptor());
            }
        }
        catch (SQLException sqlEx) {
            throw sqlEx;
        }
        catch (Exception e) {
            final SQLException sqlEx2 = SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Date", new Object[] { stringVal, columnIndex }), "S1009", this.getExceptionInterceptor());
            sqlEx2.initCause(e);
            throw sqlEx2;
        }
    }
    
    private TimeZone getDefaultTimeZone() {
        return this.useLegacyDatetimeCode ? this.connection.getDefaultTimeZone() : this.serverTimeZoneTz;
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        if (this.getMetaData().getColumnType(columnIndex) == 100 && this.connection.getIO().isOracleMode()) {
            return this.getFloat(columnIndex);
        }
        if (!this.isBinaryEncoded) {
            return this.getDoubleInternal(columnIndex);
        }
        return this.getNativeDouble(columnIndex);
    }
    
    @Override
    public double getDouble(final String columnName) throws SQLException {
        return this.getDouble(this.findColumn(columnName));
    }
    
    private final double getDoubleFromString(final String stringVal, final int columnIndex) throws SQLException {
        return this.getDoubleInternal(stringVal, columnIndex);
    }
    
    protected double getDoubleInternal(final int colIndex) throws SQLException {
        return this.getDoubleInternal(this.getString(colIndex), colIndex);
    }
    
    protected double getDoubleInternal(final String stringVal, final int colIndex) throws SQLException {
        try {
            if (stringVal == null) {
                return 0.0;
            }
            if (stringVal.length() == 0) {
                return this.convertToZeroWithEmptyCheck();
            }
            double d = Double.parseDouble(stringVal);
            if (this.useStrictFloatingPoint) {
                if (d == 2.147483648E9) {
                    d = 2.147483647E9;
                }
                else if (d == 1.0000000036275E-15) {
                    d = 1.0E-15;
                }
                else if (d == 9.999999869911E14) {
                    d = 9.99999999999999E14;
                }
                else if (d == 1.4012984643248E-45) {
                    d = 1.4E-45;
                }
                else if (d == 1.4013E-45) {
                    d = 1.4E-45;
                }
                else if (d == 3.4028234663853E37) {
                    d = 3.4028235E37;
                }
                else if (d == -2.14748E9) {
                    d = -2.147483648E9;
                }
                else if (d == 3.40282E37) {
                    d = 3.4028235E37;
                }
            }
            return d;
        }
        catch (NumberFormatException e) {
            if (this.fields[colIndex - 1].getMysqlType() == 16) {
                final long valueAsLong = this.getNumericRepresentationOfSQLBitType(colIndex);
                return (double)valueAsLong;
            }
            throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_number", new Object[] { stringVal, colIndex }), "S1009", this.getExceptionInterceptor());
        }
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.fetchDirection;
        }
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.fetchSize;
        }
    }
    
    @Override
    public char getFirstCharOfQuery() {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                return this.firstCharOfQuery;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        if (!this.isBinaryEncoded) {
            String val = null;
            val = this.getString(columnIndex);
            return this.getFloatFromString(val, columnIndex);
        }
        return this.getNativeFloat(columnIndex);
    }
    
    public ResultSet getCursor(final int columnIndex) throws SQLException {
        final int internalColumnIndex = columnIndex - 1;
        if (this.fields[internalColumnIndex].getMysqlType() != 163) {
            throw new SQLException("the field complex type is not TYPE_CURSOR");
        }
        if (this.thisRow == null) {
            throw new SQLException("invalid row in columnIndex " + columnIndex);
        }
        final ComplexData complexData = this.thisRow.getComplexRowData(internalColumnIndex);
        if (complexData == null) {
            throw new SQLException("invalid data in complexIndex " + columnIndex);
        }
        final ComplexDataType type = complexData.getComplexType();
        if (type == null) {
            throw new SQLException("null type in complexIndex " + columnIndex);
        }
        if (type.getType() != 163) {
            throw new SQLException("invalid type in complexIndex " + columnIndex + ", type is " + type.getType());
        }
        final Object[] objects = complexData.getAttrData();
        if (objects.length != 1) {
            throw new SQLException("invalid data in complex " + columnIndex);
        }
        final RowObCursorData obCursorData = (RowObCursorData)objects[0];
        if (!obCursorData.isOpen()) {
            throw new SQLException("cursor is not open");
        }
        final RowDataObCursor obCursor = new RowDataObCursor(this.connection, obCursorData.getCursorId(), this.owningStatement);
        obCursor.setOwner(this);
        if (this.isBinaryEncoded) {
            obCursor.setBinaryEncoded();
        }
        obCursor.setRowObCursorData(obCursorData);
        obCursor.hasNext();
        return obCursor;
    }
    
    @Override
    public float getFloat(final String columnName) throws SQLException {
        return this.getFloat(this.findColumn(columnName));
    }
    
    private final float getFloatFromString(final String val, final int columnIndex) throws SQLException {
        try {
            if (val == null) {
                return 0.0f;
            }
            if (val.length() == 0) {
                return (float)this.convertToZeroWithEmptyCheck();
            }
            final float f = Float.parseFloat(val);
            if (this.jdbcCompliantTruncationForReads && (f == Float.MIN_VALUE || f == Float.MAX_VALUE)) {
                final double valAsDouble = Double.parseDouble(val);
                if (valAsDouble < 1.401298464324817E-45 - ResultSetImpl.MIN_DIFF_PREC || valAsDouble > 3.4028234663852886E38 - ResultSetImpl.MAX_DIFF_PREC) {
                    this.throwRangeException(String.valueOf(valAsDouble), columnIndex, 6);
                }
            }
            return f;
        }
        catch (NumberFormatException nfe) {
            try {
                final Double valueAsDouble = new Double(val);
                final float valueAsFloat = valueAsDouble.floatValue();
                if (this.jdbcCompliantTruncationForReads && ((this.jdbcCompliantTruncationForReads && valueAsFloat == Float.NEGATIVE_INFINITY) || valueAsFloat == Float.POSITIVE_INFINITY)) {
                    this.throwRangeException(valueAsDouble.toString(), columnIndex, 6);
                }
                return valueAsFloat;
            }
            catch (NumberFormatException ex) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getFloat()_-____200") + val + Messages.getString("ResultSet.___in_column__201") + columnIndex, "S1009", this.getExceptionInterceptor());
            }
        }
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (!this.isBinaryEncoded) {
            final int columnIndexMinusOne = columnIndex - 1;
            if (this.fields[columnIndexMinusOne].getMysqlType() == 16) {
                final long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
                if (this.jdbcCompliantTruncationForReads && (valueAsLong < -2147483648L || valueAsLong > 2147483647L)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex, 4);
                }
                return (int)valueAsLong;
            }
            if (this.useFastIntParsing) {
                if (this.thisRow.isNull(columnIndexMinusOne)) {
                    this.wasNullFlag = true;
                }
                else {
                    this.wasNullFlag = false;
                }
                if (this.wasNullFlag) {
                    return 0;
                }
                if (this.thisRow.length(columnIndexMinusOne) == 0L) {
                    return this.convertToZeroWithEmptyCheck();
                }
                final boolean needsFullParse = this.thisRow.isFloatingPointNumber(columnIndexMinusOne);
                if (!needsFullParse) {
                    try {
                        return this.getIntWithOverflowCheck(columnIndexMinusOne);
                    }
                    catch (NumberFormatException nfe) {
                        try {
                            return this.parseIntAsDouble(columnIndex, this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getEncoding(), this.connection));
                        }
                        catch (NumberFormatException ex) {
                            throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____74") + this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getEncoding(), this.connection) + "'", "S1009", this.getExceptionInterceptor());
                        }
                    }
                }
            }
            String val = null;
            try {
                val = this.getString(columnIndex);
                if (val == null) {
                    return 0;
                }
                if (val.length() == 0) {
                    return this.convertToZeroWithEmptyCheck();
                }
                if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                    final int intVal = Integer.parseInt(val);
                    this.checkForIntegerTruncation(columnIndexMinusOne, null, intVal);
                    return intVal;
                }
                final int intVal = this.parseIntAsDouble(columnIndex, val);
                this.checkForIntegerTruncation(columnIndex, null, intVal);
                return intVal;
            }
            catch (NumberFormatException nfe) {
                try {
                    return this.parseIntAsDouble(columnIndex, val);
                }
                catch (NumberFormatException ex2) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____74") + val + "'", "S1009", this.getExceptionInterceptor());
                }
            }
        }
        return this.getNativeInt(columnIndex);
    }
    
    @Override
    public int getInt(final String columnName) throws SQLException {
        return this.getInt(this.findColumn(columnName));
    }
    
    private final int getIntFromString(String val, final int columnIndex) throws SQLException {
        try {
            if (val == null) {
                return 0;
            }
            if (val.length() == 0) {
                return this.convertToZeroWithEmptyCheck();
            }
            if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                val = val.trim();
                final int valueAsInt = Integer.parseInt(val);
                if (this.jdbcCompliantTruncationForReads && (valueAsInt == Integer.MIN_VALUE || valueAsInt == Integer.MAX_VALUE)) {
                    final long valueAsLong = Long.parseLong(val);
                    if (valueAsLong < -2147483648L || valueAsLong > 2147483647L) {
                        this.throwRangeException(String.valueOf(valueAsLong), columnIndex, 4);
                    }
                }
                return valueAsInt;
            }
            final double valueAsDouble = Double.parseDouble(val);
            if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -2.147483648E9 || valueAsDouble > 2.147483647E9)) {
                this.throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
            }
            return (int)valueAsDouble;
        }
        catch (NumberFormatException nfe) {
            try {
                final double valueAsDouble2 = Double.parseDouble(val);
                if (this.jdbcCompliantTruncationForReads && (valueAsDouble2 < -2.147483648E9 || valueAsDouble2 > 2.147483647E9)) {
                    this.throwRangeException(String.valueOf(valueAsDouble2), columnIndex, 4);
                }
                return (int)valueAsDouble2;
            }
            catch (NumberFormatException ex) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getInt()_-____206") + val + Messages.getString("ResultSet.___in_column__207") + columnIndex, "S1009", this.getExceptionInterceptor());
            }
        }
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return this.getLong(columnIndex, true);
    }
    
    private long getLong(final int columnIndex, final boolean overflowCheck) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (!this.isBinaryEncoded) {
            final int columnIndexMinusOne = columnIndex - 1;
            if (this.fields[columnIndexMinusOne].getMysqlType() == 16) {
                return this.getNumericRepresentationOfSQLBitType(columnIndex);
            }
            if (this.useFastIntParsing) {
                if (this.thisRow.isNull(columnIndexMinusOne)) {
                    this.wasNullFlag = true;
                }
                else {
                    this.wasNullFlag = false;
                }
                if (this.wasNullFlag) {
                    return 0L;
                }
                if (this.thisRow.length(columnIndexMinusOne) == 0L) {
                    return this.convertToZeroWithEmptyCheck();
                }
                final boolean needsFullParse = this.thisRow.isFloatingPointNumber(columnIndexMinusOne);
                if (!needsFullParse) {
                    try {
                        return this.getLongWithOverflowCheck(columnIndexMinusOne, overflowCheck);
                    }
                    catch (NumberFormatException nfe) {
                        try {
                            return this.parseLongAsDouble(columnIndexMinusOne, this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getEncoding(), this.connection));
                        }
                        catch (NumberFormatException ex) {
                            throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____79") + this.thisRow.getString(columnIndexMinusOne, this.fields[columnIndexMinusOne].getEncoding(), this.connection) + "'", "S1009", this.getExceptionInterceptor());
                        }
                    }
                }
            }
            String val = null;
            try {
                val = this.getString(columnIndex);
                if (val == null) {
                    return 0L;
                }
                if (val.length() == 0) {
                    return this.convertToZeroWithEmptyCheck();
                }
                if (val.indexOf("e") == -1 && val.indexOf("E") == -1) {
                    return this.parseLongWithOverflowCheck(columnIndexMinusOne, null, val, overflowCheck);
                }
                return this.parseLongAsDouble(columnIndexMinusOne, val);
            }
            catch (NumberFormatException nfe) {
                try {
                    return this.parseLongAsDouble(columnIndexMinusOne, val);
                }
                catch (NumberFormatException ex2) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____79") + val + "'", "S1009", this.getExceptionInterceptor());
                }
            }
        }
        return this.getNativeLong(columnIndex, overflowCheck, true);
    }
    
    @Override
    public long getLong(final String columnName) throws SQLException {
        return this.getLong(this.findColumn(columnName));
    }
    
    private final long getLongFromString(final String val, final int columnIndexZeroBased) throws SQLException {
        try {
            if (val == null) {
                return 0L;
            }
            if (val.length() == 0) {
                return this.convertToZeroWithEmptyCheck();
            }
            if (val.indexOf("e") == -1 && val.indexOf("E") == -1) {
                return this.parseLongWithOverflowCheck(columnIndexZeroBased, null, val, true);
            }
            return this.parseLongAsDouble(columnIndexZeroBased, val);
        }
        catch (NumberFormatException nfe) {
            try {
                return this.parseLongAsDouble(columnIndexZeroBased, val);
            }
            catch (NumberFormatException ex) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getLong()_-____211") + val + Messages.getString("ResultSet.___in_column__212") + (columnIndexZeroBased + 1), "S1009", this.getExceptionInterceptor());
            }
        }
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        this.checkClosed();
        return new com.alipay.oceanbase.jdbc.ResultSetMetaData(this.fields, this.connection.getUseOldAliasMetadataBehavior(), this.connection.getYearIsDateType(), this.getExceptionInterceptor());
    }
    
    protected Array getNativeArray(final int i) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    protected InputStream getNativeAsciiStream(final int columnIndex) throws SQLException {
        this.checkRowPos();
        return this.getNativeBinaryStream(columnIndex);
    }
    
    protected BigDecimal getNativeBigDecimal(final int columnIndex) throws SQLException {
        this.checkColumnBounds(columnIndex);
        final int scale = this.fields[columnIndex - 1].getDecimals();
        return this.getNativeBigDecimal(columnIndex, scale);
    }
    
    protected BigDecimal getNativeBigDecimal(final int columnIndex, final int scale) throws SQLException {
        this.checkColumnBounds(columnIndex);
        String stringVal = null;
        final Field f = this.fields[columnIndex - 1];
        final Object value = this.thisRow.getColumnValue(columnIndex - 1);
        if (value == null) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        switch (f.getSQLType()) {
            case 2:
            case 3: {
                stringVal = StringUtils.toAsciiString((byte[])value);
                break;
            }
            default: {
                stringVal = this.getNativeString(columnIndex);
                break;
            }
        }
        return this.getBigDecimalFromString(stringVal, columnIndex, scale);
    }
    
    protected InputStream getNativeBinaryStream(final int columnIndex) throws SQLException {
        this.checkRowPos();
        final int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        switch (this.fields[columnIndexMinusOne].getMysqlType()) {
            case 210: {
                final Blob blob = this.getLobBlobObject(this.thisRow.getColumnValue(columnIndexMinusOne), this.thisRow.metadata[columnIndexMinusOne].getEncoding(), this.connection, this.getExceptionInterceptor());
                return (blob == null) ? null : blob.getBinaryStream();
            }
            case 211: {
                final Clob clob = this.getLobClobObject(this.thisRow.getColumnValue(columnIndexMinusOne), this.thisRow.metadata[columnIndexMinusOne].getEncoding(), this.connection, this.getExceptionInterceptor());
                return (clob == null) ? null : clob.getAsciiStream();
            }
            default: {
                switch (this.fields[columnIndexMinusOne].getSQLType()) {
                    case -7:
                    case -4:
                    case -3:
                    case -2:
                    case 2004: {
                        return this.thisRow.getBinaryInputStream(columnIndexMinusOne);
                    }
                    default: {
                        final byte[] b = this.getNativeBytes(columnIndex, false);
                        if (b != null) {
                            return new ByteArrayInputStream(b);
                        }
                        return null;
                    }
                }
                break;
            }
        }
    }
    
    protected java.sql.Blob getNativeBlob(final int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        final Object value = this.thisRow.getColumnValue(columnIndex - 1);
        if (value == null) {
            this.wasNullFlag = true;
        }
        else {
            this.wasNullFlag = false;
        }
        if (this.wasNullFlag) {
            return null;
        }
        final int mysqlType = this.fields[columnIndex - 1].getMysqlType();
        byte[] dataAsBytes = null;
        switch (mysqlType) {
            case 203:
            case 249:
            case 250:
            case 251:
            case 252: {
                dataAsBytes = (byte[])value;
                break;
            }
            default: {
                dataAsBytes = this.getNativeBytes(columnIndex, false);
                break;
            }
        }
        if (!this.connection.getEmulateLocators()) {
            return new Blob(dataAsBytes, this.getExceptionInterceptor());
        }
        return new BlobFromLocator(this, columnIndex, this.getExceptionInterceptor());
    }
    
    public static boolean arraysEqual(final byte[] left, final byte[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }
        if (left.length != right.length) {
            return false;
        }
        for (int i = 0; i < left.length; ++i) {
            if (left[i] != right[i]) {
                return false;
            }
        }
        return true;
    }
    
    protected byte getNativeByte(final int columnIndex) throws SQLException {
        return this.getNativeByte(columnIndex, true);
    }
    
    protected byte getNativeByte(int columnIndex, final boolean overflowCheck) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        final Object value = this.thisRow.getColumnValue(columnIndex - 1);
        if (value == null) {
            this.wasNullFlag = true;
            return 0;
        }
        this.wasNullFlag = false;
        --columnIndex;
        final Field field = this.fields[columnIndex];
        switch (field.getMysqlType()) {
            case 16: {
                final long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -128L || valueAsLong > 127L)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, -6);
                }
                return (byte)valueAsLong;
            }
            case 1: {
                final byte valueAsByte = ((byte[])value)[0];
                if (!field.isUnsigned()) {
                    return valueAsByte;
                }
                final short valueAsShort = (valueAsByte >= 0) ? valueAsByte : ((short)(valueAsByte + 256));
                if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsShort > 127) {
                    this.throwRangeException(String.valueOf(valueAsShort), columnIndex + 1, -6);
                }
                return (byte)valueAsShort;
            }
            case 2:
            case 13: {
                final short valueAsShort = this.getNativeShort(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsShort < -128 || valueAsShort > 127)) {
                    this.throwRangeException(String.valueOf(valueAsShort), columnIndex + 1, -6);
                }
                return (byte)valueAsShort;
            }
            case 3:
            case 9: {
                final int valueAsInt = this.getNativeInt(columnIndex + 1, false);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsInt < -128 || valueAsInt > 127)) {
                    this.throwRangeException(String.valueOf(valueAsInt), columnIndex + 1, -6);
                }
                return (byte)valueAsInt;
            }
            case 4: {
                final float valueAsFloat = this.getNativeFloat(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsFloat < -128.0f || valueAsFloat > 127.0f)) {
                    this.throwRangeException(String.valueOf(valueAsFloat), columnIndex + 1, -6);
                }
                return (byte)valueAsFloat;
            }
            case 5: {
                final double valueAsDouble = this.getNativeDouble(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -128.0 || valueAsDouble > 127.0)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -6);
                }
                return (byte)valueAsDouble;
            }
            case 8: {
                final long valueAsLong = this.getNativeLong(columnIndex + 1, false, true);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -128L || valueAsLong > 127L)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, -6);
                }
                return (byte)valueAsLong;
            }
            default: {
                if (this.useUsageAdvisor) {
                    this.issueConversionViaParsingWarning("getByte()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex], new int[] { 5, 1, 2, 3, 8, 4 });
                }
                return this.getByteFromString(this.getNativeString(columnIndex + 1), columnIndex + 1);
            }
        }
    }
    
    protected byte[] getNativeBytes(final int columnIndex, final boolean noConversion) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        final Object value = this.thisRow.getColumnValue(columnIndex - 1);
        if (value == null) {
            this.wasNullFlag = true;
        }
        else {
            this.wasNullFlag = false;
        }
        if (this.wasNullFlag) {
            return null;
        }
        final Field field = this.fields[columnIndex - 1];
        int mysqlType = field.getMysqlType();
        if (noConversion) {
            mysqlType = 252;
        }
        switch (mysqlType) {
            case 16:
            case 203:
            case 249:
            case 250:
            case 251:
            case 252: {
                return (byte[])value;
            }
            case 15:
            case 209:
            case 253:
            case 254: {
                if (value instanceof byte[]) {
                    return (byte[])value;
                }
                break;
            }
        }
        final int sqlType = field.getSQLType();
        if (sqlType == -3 || sqlType == -2) {
            return (byte[])value;
        }
        return this.getBytesFromString(this.getNativeString(columnIndex));
    }
    
    protected Reader getNativeCharacterStream(final int columnIndex) throws SQLException {
        final int columnIndexMinusOne = columnIndex - 1;
        switch (this.fields[columnIndexMinusOne].getSQLType()) {
            case -1:
            case 1:
            case 12:
            case 2005: {
                if (this.thisRow.isNull(columnIndexMinusOne)) {
                    this.wasNullFlag = true;
                    return null;
                }
                this.wasNullFlag = false;
                return this.thisRow.getReader(columnIndexMinusOne);
            }
            default: {
                final String asString = this.getStringForClob(columnIndex);
                if (asString == null) {
                    return null;
                }
                return this.getCharacterStreamFromString(asString);
            }
        }
    }
    
    protected java.sql.Clob getNativeClob(final int columnIndex) throws SQLException {
        final String stringVal = this.getStringForClob(columnIndex);
        if (stringVal == null) {
            return null;
        }
        return this.getClobFromString(stringVal);
    }
    
    private String getNativeConvertToString(final int columnIndex, final Field field) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final int sqlType = field.getSQLType();
            final int mysqlType = field.getMysqlType();
            switch (sqlType) {
                case -7: {
                    return String.valueOf(this.getNumericRepresentationOfSQLBitType(columnIndex));
                }
                case 16: {
                    final boolean booleanVal = this.getBoolean(columnIndex);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    return String.valueOf(booleanVal);
                }
                case -6: {
                    final byte tinyintVal = this.getNativeByte(columnIndex, false);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    if (!field.isUnsigned() || tinyintVal >= 0) {
                        return String.valueOf(tinyintVal);
                    }
                    final short unsignedTinyVal = (short)(tinyintVal & 0xFF);
                    return String.valueOf(unsignedTinyVal);
                }
                case 5: {
                    int intVal = this.getNativeInt(columnIndex, false);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    if (!field.isUnsigned() || intVal >= 0) {
                        return String.valueOf(intVal);
                    }
                    intVal &= 0xFFFF;
                    return String.valueOf(intVal);
                }
                case 4: {
                    final int intVal = this.getNativeInt(columnIndex, false);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    if (!field.isUnsigned() || intVal >= 0 || field.getMysqlType() == 9) {
                        return String.valueOf(intVal);
                    }
                    final long longVal = (long)intVal & 0xFFFFFFFFL;
                    return String.valueOf(longVal);
                }
                case -5: {
                    if (!field.isUnsigned()) {
                        final long longVal = this.getNativeLong(columnIndex, false, true);
                        if (this.wasNullFlag) {
                            return null;
                        }
                        return String.valueOf(longVal);
                    }
                    else {
                        final long longVal = this.getNativeLong(columnIndex, false, false);
                        if (this.wasNullFlag) {
                            return null;
                        }
                        return String.valueOf(convertLongToUlong(longVal));
                    }
                    break;
                }
                case 7: {
                    final float floatVal = this.getNativeFloat(columnIndex);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    return String.valueOf(floatVal);
                }
                case 6:
                case 8: {
                    final double doubleVal = this.getNativeDouble(columnIndex);
                    if (this.wasNullFlag) {
                        return null;
                    }
                    return String.valueOf(doubleVal);
                }
                case 2:
                case 3: {
                    final String stringVal = StringUtils.toAsciiString(this.thisRow.getColumnValue(columnIndex - 1));
                    if (stringVal == null) {
                        this.wasNullFlag = true;
                        return null;
                    }
                    this.wasNullFlag = false;
                    if (stringVal.length() == 0) {
                        final BigDecimal val = new BigDecimal(0);
                        return val.toString();
                    }
                    BigDecimal val;
                    try {
                        val = new BigDecimal(stringVal);
                    }
                    catch (NumberFormatException ex) {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, columnIndex }), "S1009", this.getExceptionInterceptor());
                    }
                    return val.toString();
                }
                case -1:
                case 1:
                case 12:
                case 2005: {
                    return this.extractStringFromNativeColumn(columnIndex, mysqlType);
                }
                case -4:
                case -3:
                case -2: {
                    if (!field.isBlob()) {
                        return this.extractStringFromNativeColumn(columnIndex, mysqlType);
                    }
                    if (!field.isBinary()) {
                        return this.extractStringFromNativeColumn(columnIndex, mysqlType);
                    }
                    Object obj;
                    final byte[] data = (byte[])(obj = this.getBytes(columnIndex));
                    if (data != null && data.length >= 2) {
                        if (data[0] == -84 && data[1] == -19) {
                            try {
                                final ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
                                final ObjectInputStream objIn = new ObjectInputStream(bytesIn);
                                obj = objIn.readObject();
                                objIn.close();
                                bytesIn.close();
                            }
                            catch (ClassNotFoundException cnfe) {
                                throw SQLError.createSQLException(Messages.getString("ResultSet.Class_not_found___91") + cnfe.toString() + Messages.getString("ResultSet._while_reading_serialized_object_92"), this.getExceptionInterceptor());
                            }
                            catch (IOException ex2) {
                                obj = data;
                            }
                        }
                        return obj.toString();
                    }
                    return this.extractStringFromNativeColumn(columnIndex, mysqlType);
                }
                case 91: {
                    if (mysqlType == 13) {
                        short shortVal = this.getNativeShort(columnIndex);
                        if (this.connection.getYearIsDateType()) {
                            if (field.getLength() == 2L) {
                                if (shortVal <= 69) {
                                    shortVal += 100;
                                }
                                shortVal += 1900;
                            }
                            return this.fastDateCreate(null, shortVal, 1, 1).toString();
                        }
                        if (this.wasNullFlag) {
                            return null;
                        }
                        return String.valueOf(shortVal);
                    }
                    else {
                        if (this.connection.getNoDatetimeStringSync()) {
                            final byte[] asBytes = this.getNativeBytes(columnIndex, true);
                            if (asBytes == null) {
                                return null;
                            }
                            if (asBytes.length == 0) {
                                return "0000-00-00";
                            }
                            final int year = (asBytes[0] & 0xFF) | (asBytes[1] & 0xFF) << 8;
                            final int month = asBytes[2];
                            final int day = asBytes[3];
                            if (year == 0 && month == 0 && day == 0) {
                                return "0000-00-00";
                            }
                        }
                        final Date dt = this.getNativeDate(columnIndex);
                        if (dt == null) {
                            return null;
                        }
                        return String.valueOf(dt);
                    }
                    break;
                }
                case 92: {
                    final Time tm = this.getNativeTime(columnIndex, null, this.connection.getDefaultTimeZone(), false);
                    if (tm == null) {
                        return null;
                    }
                    return String.valueOf(tm);
                }
                case 93: {
                    if (this.connection.getNoDatetimeStringSync()) {
                        final byte[] asBytes2 = this.getNativeBytes(columnIndex, true);
                        if (asBytes2 == null) {
                            return null;
                        }
                        if (asBytes2.length == 0) {
                            return "0000-00-00 00:00:00";
                        }
                        final int year2 = (asBytes2[0] & 0xFF) | (asBytes2[1] & 0xFF) << 8;
                        final int month2 = asBytes2[2];
                        final int day2 = asBytes2[3];
                        if (year2 == 0 && month2 == 0 && day2 == 0) {
                            return "0000-00-00 00:00:00";
                        }
                    }
                    final Timestamp tstamp = this.getNativeTimestamp(columnIndex, null, this.connection.getDefaultTimeZone(), false);
                    if (tstamp == null) {
                        return null;
                    }
                    final String result = String.valueOf(tstamp);
                    if (!this.connection.getNoDatetimeStringSync()) {
                        return result;
                    }
                    if (result.endsWith(".0")) {
                        return result.substring(0, result.length() - 2);
                    }
                    return this.extractStringFromNativeColumn(columnIndex, mysqlType);
                }
                default: {
                    return this.extractStringFromNativeColumn(columnIndex, mysqlType);
                }
            }
        }
    }
    
    protected Date getNativeDate(final int columnIndex) throws SQLException {
        return this.getNativeDate(columnIndex, null);
    }
    
    protected Date getNativeDate(final int columnIndex, final Calendar cal) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        final int columnIndexMinusOne = columnIndex - 1;
        final int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
        Date dateToReturn = null;
        if (mysqlType == 10) {
            dateToReturn = this.thisRow.getNativeDate(columnIndexMinusOne, this.connection, this, cal);
        }
        else {
            final TimeZone tz = (cal != null) ? cal.getTimeZone() : this.getDefaultTimeZone();
            final boolean rollForward = tz != null && !tz.equals(this.getDefaultTimeZone());
            dateToReturn = (Date)this.thisRow.getNativeDateTimeValue(columnIndexMinusOne, null, 91, mysqlType, tz, rollForward, this.connection, this);
        }
        if (dateToReturn == null) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return dateToReturn;
    }
    
    Date getNativeDateViaParseConversion(final int columnIndex) throws SQLException {
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getDate()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex - 1], new int[] { 10 });
        }
        final String stringVal = this.getNativeString(columnIndex);
        return this.getDateFromString(stringVal, columnIndex, null);
    }
    
    protected double getNativeDouble(int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        --columnIndex;
        if (this.thisRow.isNull(columnIndex)) {
            this.wasNullFlag = true;
            return 0.0;
        }
        this.wasNullFlag = false;
        final Field f = this.fields[columnIndex];
        switch (f.getMysqlType()) {
            case 5: {
                return this.thisRow.getNativeDouble(columnIndex);
            }
            case 1: {
                if (!f.isUnsigned()) {
                    return this.getNativeByte(columnIndex + 1);
                }
                return this.getNativeShort(columnIndex + 1);
            }
            case 2:
            case 13: {
                if (!f.isUnsigned()) {
                    return this.getNativeShort(columnIndex + 1);
                }
                return this.getNativeInt(columnIndex + 1);
            }
            case 3:
            case 9: {
                if (!f.isUnsigned()) {
                    return this.getNativeInt(columnIndex + 1);
                }
                return (double)this.getNativeLong(columnIndex + 1);
            }
            case 8: {
                final long valueAsLong = this.getNativeLong(columnIndex + 1);
                if (!f.isUnsigned()) {
                    return (double)valueAsLong;
                }
                final BigInteger asBigInt = convertLongToUlong(valueAsLong);
                return asBigInt.doubleValue();
            }
            case 4: {
                return this.getNativeFloat(columnIndex + 1);
            }
            case 16: {
                return (double)this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
            }
            default: {
                final String stringVal = this.getNativeString(columnIndex + 1);
                if (this.useUsageAdvisor) {
                    this.issueConversionViaParsingWarning("getDouble()", columnIndex, stringVal, this.fields[columnIndex], new int[] { 5, 1, 2, 3, 8, 4 });
                }
                return this.getDoubleFromString(stringVal, columnIndex + 1);
            }
        }
    }
    
    protected float getNativeFloat(int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        --columnIndex;
        if (this.thisRow.isNull(columnIndex)) {
            this.wasNullFlag = true;
            return 0.0f;
        }
        this.wasNullFlag = false;
        final Field f = this.fields[columnIndex];
        switch (f.getMysqlType()) {
            case 16: {
                final long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
                return (float)valueAsLong;
            }
            case 5: {
                final Double valueAsDouble = new Double(this.getNativeDouble(columnIndex + 1));
                final float valueAsFloat = valueAsDouble.floatValue();
                if ((this.jdbcCompliantTruncationForReads && valueAsFloat == Float.NEGATIVE_INFINITY) || valueAsFloat == Float.POSITIVE_INFINITY) {
                    this.throwRangeException(valueAsDouble.toString(), columnIndex + 1, 6);
                }
                return (float)this.getNativeDouble(columnIndex + 1);
            }
            case 1: {
                if (!f.isUnsigned()) {
                    return this.getNativeByte(columnIndex + 1);
                }
                return this.getNativeShort(columnIndex + 1);
            }
            case 2:
            case 13: {
                if (!f.isUnsigned()) {
                    return this.getNativeShort(columnIndex + 1);
                }
                return (float)this.getNativeInt(columnIndex + 1);
            }
            case 3:
            case 9: {
                if (!f.isUnsigned()) {
                    return (float)this.getNativeInt(columnIndex + 1);
                }
                return (float)this.getNativeLong(columnIndex + 1);
            }
            case 8: {
                final long valueAsLong = this.getNativeLong(columnIndex + 1);
                if (!f.isUnsigned()) {
                    return (float)valueAsLong;
                }
                final BigInteger asBigInt = convertLongToUlong(valueAsLong);
                return asBigInt.floatValue();
            }
            case 4: {
                return this.thisRow.getNativeFloat(columnIndex);
            }
            default: {
                final String stringVal = this.getNativeString(columnIndex + 1);
                if (this.useUsageAdvisor) {
                    this.issueConversionViaParsingWarning("getFloat()", columnIndex, stringVal, this.fields[columnIndex], new int[] { 5, 1, 2, 3, 8, 4 });
                }
                return this.getFloatFromString(stringVal, columnIndex + 1);
            }
        }
    }
    
    protected int getNativeInt(final int columnIndex) throws SQLException {
        return this.getNativeInt(columnIndex, true);
    }
    
    protected int getNativeInt(int columnIndex, final boolean overflowCheck) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        --columnIndex;
        if (this.thisRow.isNull(columnIndex)) {
            this.wasNullFlag = true;
            return 0;
        }
        this.wasNullFlag = false;
        final Field f = this.fields[columnIndex];
        switch (f.getMysqlType()) {
            case 16: {
                final long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -2147483648L || valueAsLong > 2147483647L)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 4);
                }
                return (int)valueAsLong;
            }
            case 1: {
                final byte tinyintVal = this.getNativeByte(columnIndex + 1, false);
                if (!f.isUnsigned() || tinyintVal >= 0) {
                    return tinyintVal;
                }
                return tinyintVal + 256;
            }
            case 2:
            case 13: {
                final short asShort = this.getNativeShort(columnIndex + 1, false);
                if (!f.isUnsigned() || asShort >= 0) {
                    return asShort;
                }
                return asShort + 65536;
            }
            case 3:
            case 9: {
                final int valueAsInt = this.thisRow.getNativeInt(columnIndex);
                if (!f.isUnsigned()) {
                    return valueAsInt;
                }
                final long valueAsLong = (valueAsInt >= 0) ? valueAsInt : (valueAsInt + 4294967296L);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsLong > 2147483647L) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 4);
                }
                return (int)valueAsLong;
            }
            case 8: {
                final long valueAsLong = this.getNativeLong(columnIndex + 1, false, true);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -2147483648L || valueAsLong > 2147483647L)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 4);
                }
                return (int)valueAsLong;
            }
            case 5: {
                final double valueAsDouble = this.getNativeDouble(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -2.147483648E9 || valueAsDouble > 2.147483647E9)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, 4);
                }
                return (int)valueAsDouble;
            }
            case 4: {
                final double valueAsDouble = this.getNativeFloat(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -2.147483648E9 || valueAsDouble > 2.147483647E9)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, 4);
                }
                return (int)valueAsDouble;
            }
            default: {
                final String stringVal = this.getNativeString(columnIndex + 1);
                if (this.useUsageAdvisor) {
                    this.issueConversionViaParsingWarning("getInt()", columnIndex, stringVal, this.fields[columnIndex], new int[] { 5, 1, 2, 3, 8, 4 });
                }
                return this.getIntFromString(stringVal, columnIndex + 1);
            }
        }
    }
    
    protected long getNativeLong(final int columnIndex) throws SQLException {
        return this.getNativeLong(columnIndex, true, true);
    }
    
    protected long getNativeLong(int columnIndex, final boolean overflowCheck, final boolean expandUnsignedLong) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        --columnIndex;
        if (this.thisRow.isNull(columnIndex)) {
            this.wasNullFlag = true;
            return 0L;
        }
        this.wasNullFlag = false;
        final Field f = this.fields[columnIndex];
        switch (f.getMysqlType()) {
            case 16: {
                return this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
            }
            case 1: {
                if (!f.isUnsigned()) {
                    return this.getNativeByte(columnIndex + 1);
                }
                return this.getNativeInt(columnIndex + 1);
            }
            case 2: {
                if (!f.isUnsigned()) {
                    return this.getNativeShort(columnIndex + 1);
                }
                return this.getNativeInt(columnIndex + 1, false);
            }
            case 13: {
                return this.getNativeShort(columnIndex + 1);
            }
            case 3:
            case 9: {
                final int asInt = this.getNativeInt(columnIndex + 1, false);
                if (!f.isUnsigned() || asInt >= 0) {
                    return asInt;
                }
                return asInt + 4294967296L;
            }
            case 8: {
                final long valueAsLong = this.thisRow.getNativeLong(columnIndex);
                if (!f.isUnsigned() || !expandUnsignedLong) {
                    return valueAsLong;
                }
                final BigInteger asBigInt = convertLongToUlong(valueAsLong);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (asBigInt.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) > 0 || asBigInt.compareTo(new BigInteger(String.valueOf(Long.MIN_VALUE))) < 0)) {
                    this.throwRangeException(asBigInt.toString(), columnIndex + 1, -5);
                }
                return this.getLongFromString(asBigInt.toString(), columnIndex);
            }
            case 5: {
                final double valueAsDouble = this.getNativeDouble(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -9.223372036854776E18 || valueAsDouble > 9.223372036854776E18)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -5);
                }
                return (long)valueAsDouble;
            }
            case 4: {
                final double valueAsDouble = this.getNativeFloat(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -9.223372036854776E18 || valueAsDouble > 9.223372036854776E18)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, -5);
                }
                return (long)valueAsDouble;
            }
            default: {
                final String stringVal = this.getNativeString(columnIndex + 1);
                if (this.useUsageAdvisor) {
                    this.issueConversionViaParsingWarning("getLong()", columnIndex, stringVal, this.fields[columnIndex], new int[] { 5, 1, 2, 3, 8, 4 });
                }
                return this.getLongFromString(stringVal, columnIndex + 1);
            }
        }
    }
    
    protected Ref getNativeRef(final int i) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    protected short getNativeShort(final int columnIndex) throws SQLException {
        return this.getNativeShort(columnIndex, true);
    }
    
    protected short getNativeShort(int columnIndex, final boolean overflowCheck) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        --columnIndex;
        if (this.thisRow.isNull(columnIndex)) {
            this.wasNullFlag = true;
            return 0;
        }
        this.wasNullFlag = false;
        final Field f = this.fields[columnIndex];
        switch (f.getMysqlType()) {
            case 16: {
                final long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -32768L || valueAsLong > 32767L)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 5);
                }
                return (short)valueAsLong;
            }
            case 1: {
                final byte tinyintVal = this.getNativeByte(columnIndex + 1, false);
                if (!f.isUnsigned() || tinyintVal >= 0) {
                    return tinyintVal;
                }
                return (short)(tinyintVal + 256);
            }
            case 2:
            case 13: {
                final short asShort = this.thisRow.getNativeShort(columnIndex);
                if (!f.isUnsigned()) {
                    return asShort;
                }
                final int valueAsInt = asShort & 0xFFFF;
                if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsInt > 32767) {
                    this.throwRangeException(String.valueOf(valueAsInt), columnIndex + 1, 5);
                }
                return (short)valueAsInt;
            }
            case 3:
            case 9: {
                if (!f.isUnsigned()) {
                    final int valueAsInt = this.getNativeInt(columnIndex + 1, false);
                    if ((overflowCheck && this.jdbcCompliantTruncationForReads && valueAsInt > 32767) || valueAsInt < -32768) {
                        this.throwRangeException(String.valueOf(valueAsInt), columnIndex + 1, 5);
                    }
                    return (short)valueAsInt;
                }
                final long valueAsLong = this.getNativeLong(columnIndex + 1, false, true);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && valueAsLong > 32767L) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 5);
                }
                return (short)valueAsLong;
            }
            case 8: {
                final long valueAsLong = this.getNativeLong(columnIndex + 1, false, false);
                if (!f.isUnsigned()) {
                    if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsLong < -32768L || valueAsLong > 32767L)) {
                        this.throwRangeException(String.valueOf(valueAsLong), columnIndex + 1, 5);
                    }
                    return (short)valueAsLong;
                }
                final BigInteger asBigInt = convertLongToUlong(valueAsLong);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (asBigInt.compareTo(new BigInteger(String.valueOf(32767))) > 0 || asBigInt.compareTo(new BigInteger(String.valueOf(-32768))) < 0)) {
                    this.throwRangeException(asBigInt.toString(), columnIndex + 1, 5);
                }
                return (short)this.getIntFromString(asBigInt.toString(), columnIndex + 1);
            }
            case 5: {
                final double valueAsDouble = this.getNativeDouble(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsDouble < -32768.0 || valueAsDouble > 32767.0)) {
                    this.throwRangeException(String.valueOf(valueAsDouble), columnIndex + 1, 5);
                }
                return (short)valueAsDouble;
            }
            case 4: {
                final float valueAsFloat = this.getNativeFloat(columnIndex + 1);
                if (overflowCheck && this.jdbcCompliantTruncationForReads && (valueAsFloat < -32768.0f || valueAsFloat > 32767.0f)) {
                    this.throwRangeException(String.valueOf(valueAsFloat), columnIndex + 1, 5);
                }
                return (short)valueAsFloat;
            }
            default: {
                final String stringVal = this.getNativeString(columnIndex + 1);
                if (this.useUsageAdvisor) {
                    this.issueConversionViaParsingWarning("getShort()", columnIndex, stringVal, this.fields[columnIndex], new int[] { 5, 1, 2, 3, 8, 4 });
                }
                return this.getShortFromString(stringVal, columnIndex + 1);
            }
        }
    }
    
    protected String getNativeString(final int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (this.fields == null) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Query_generated_no_fields_for_ResultSet_133"), "S1002", this.getExceptionInterceptor());
        }
        if (this.thisRow.isNull(columnIndex - 1)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        String stringVal = null;
        final Field field = this.fields[columnIndex - 1];
        stringVal = this.getNativeConvertToString(columnIndex, field);
        final int mysqlType = field.getMysqlType();
        if (mysqlType != 7 && mysqlType != 10 && field.isZeroFill() && stringVal != null) {
            final int origLength = stringVal.length();
            final StringBuilder zeroFillBuf = new StringBuilder(origLength);
            for (long numZeros = field.getLength() - origLength, i = 0L; i < numZeros; ++i) {
                zeroFillBuf.append('0');
            }
            zeroFillBuf.append(stringVal);
            stringVal = zeroFillBuf.toString();
        }
        return stringVal;
    }
    
    private Time getNativeTime(final int columnIndex, final Calendar targetCalendar, final TimeZone tz, final boolean rollForward) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        final int columnIndexMinusOne = columnIndex - 1;
        final int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
        Time timeVal = null;
        if (mysqlType == 11) {
            timeVal = this.thisRow.getNativeTime(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this);
        }
        else {
            timeVal = (Time)this.thisRow.getNativeDateTimeValue(columnIndexMinusOne, null, 92, mysqlType, tz, rollForward, this.connection, this);
        }
        if (timeVal == null) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return timeVal;
    }
    
    Time getNativeTimeViaParseConversion(final int columnIndex, final Calendar targetCalendar, final TimeZone tz, final boolean rollForward) throws SQLException {
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getTime()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex - 1], new int[] { 11 });
        }
        final String strTime = this.getNativeString(columnIndex);
        return this.getTimeFromString(strTime, targetCalendar, columnIndex, tz, rollForward);
    }
    
    private Timestamp getNativeTimestamp(final int columnIndex, final Calendar targetCalendar, final TimeZone tz, final boolean rollForward) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        final int columnIndexMinusOne = columnIndex - 1;
        Timestamp tsVal = null;
        final int mysqlType = this.fields[columnIndexMinusOne].getMysqlType();
        switch (mysqlType) {
            case 7:
            case 12: {
                tsVal = this.thisRow.getNativeTimestamp(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this);
                break;
            }
            default: {
                tsVal = (Timestamp)this.thisRow.getNativeDateTimeValue(columnIndexMinusOne, null, 93, mysqlType, tz, rollForward, this.connection, this);
                break;
            }
        }
        if (tsVal == null) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        return tsVal;
    }
    
    Timestamp getNativeTimestampViaParseConversion(final int columnIndex, final Calendar targetCalendar, final TimeZone tz, final boolean rollForward) throws SQLException {
        if (this.useUsageAdvisor) {
            this.issueConversionViaParsingWarning("getTimestamp()", columnIndex, this.thisRow.getColumnValue(columnIndex - 1), this.fields[columnIndex - 1], new int[] { 7, 12 });
        }
        final String strTimestamp = this.getNativeString(columnIndex);
        return this.getTimestampFromString(columnIndex, targetCalendar, strTimestamp, tz, rollForward);
    }
    
    protected InputStream getNativeUnicodeStream(final int columnIndex) throws SQLException {
        this.checkRowPos();
        return this.getBinaryStream(columnIndex);
    }
    
    protected URL getNativeURL(final int colIndex) throws SQLException {
        final String val = this.getString(colIndex);
        if (val == null) {
            return null;
        }
        try {
            return new URL(val);
        }
        catch (MalformedURLException mfe) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Malformed_URL____141") + val + "'", "S1009", this.getExceptionInterceptor());
        }
    }
    
    @Override
    public synchronized ResultSetInternalMethods getNextResultSet() {
        return this.nextResultSet;
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        final int columnIndexMinusOne = columnIndex - 1;
        if (this.thisRow.isNull(columnIndexMinusOne)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        final Field field = this.fields[columnIndexMinusOne];
        switch (field.getSQLType()) {
            case -7: {
                if (field.getMysqlType() == 16 && !field.isSingleBit()) {
                    return this.getObjectDeserializingIfNeeded(columnIndex);
                }
                return this.getBoolean(columnIndex);
            }
            case 16: {
                return this.getBoolean(columnIndex);
            }
            case -6: {
                if (!field.isUnsigned()) {
                    return this.getByte(columnIndex);
                }
                return this.getInt(columnIndex);
            }
            case 5: {
                return this.getInt(columnIndex);
            }
            case 4: {
                if (!field.isUnsigned() || field.getMysqlType() == 9) {
                    return this.getInt(columnIndex);
                }
                return this.getLong(columnIndex);
            }
            case -5: {
                if (!field.isUnsigned()) {
                    return this.getLong(columnIndex);
                }
                final String stringVal = this.getString(columnIndex);
                if (stringVal == null) {
                    return null;
                }
                try {
                    return new BigInteger(stringVal);
                }
                catch (NumberFormatException nfe) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigInteger", new Object[] { columnIndex, stringVal }), "S1009", this.getExceptionInterceptor());
                }
            }
            case 2:
            case 3: {
                final String stringVal = this.getString(columnIndex);
                if (stringVal == null) {
                    return null;
                }
                if (stringVal.length() == 0) {
                    final BigDecimal val = new BigDecimal(0);
                    return val;
                }
                BigDecimal val;
                try {
                    val = new BigDecimal(stringVal);
                }
                catch (NumberFormatException ex) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, columnIndex }), "S1009", this.getExceptionInterceptor());
                }
                return val;
            }
            case 7: {
                return new Float(this.getFloat(columnIndex));
            }
            case 6:
            case 8: {
                return new Double(this.getDouble(columnIndex));
            }
            case 1:
            case 12: {
                if (field.getMysqlType() == 163) {
                    return this.getCursor(columnIndex);
                }
                if (field.getMysqlType() == 200) {
                    return this.getTIMESTAMPTZ(columnIndex);
                }
                if (field.getMysqlType() == 201) {
                    return this.getTIMESTAMPLTZ(columnIndex);
                }
                if (field.getMysqlType() == 202) {
                    return this.getTimestamp(columnIndex);
                }
                if (field.getMysqlType() == 160) {
                    return this.getArray(columnIndex);
                }
                if (field.getMysqlType() == 205) {
                    return this.getINTERVALDS(columnIndex);
                }
                if (field.getMysqlType() == 210) {
                    return this.getBlob(columnIndex);
                }
                if (field.getMysqlType() == 211) {
                    return this.getClob(columnIndex);
                }
                if (field.getMysqlType() == 204) {
                    return this.getINTERVALYM(columnIndex);
                }
                if (this.isNUMBERTYPE(field.getMysqlType())) {
                    return this.getNUMBER(columnIndex);
                }
                if (!field.isOpaqueBinary()) {
                    return this.getString(columnIndex);
                }
                return this.getBytes(columnIndex);
            }
            case -1: {
                if (!field.isOpaqueBinary()) {
                    return this.getStringForClob(columnIndex);
                }
                return this.getBytes(columnIndex);
            }
            case 2005: {
                return this.getStringForClob(columnIndex);
            }
            case -4:
            case -3:
            case -2: {
                if (field.getMysqlType() == 255) {
                    return this.getBytes(columnIndex);
                }
                return this.getObjectDeserializingIfNeeded(columnIndex);
            }
            case 91: {
                if (field.getMysqlType() == 13 && !this.connection.getYearIsDateType()) {
                    return this.getShort(columnIndex);
                }
                return this.getDate(columnIndex);
            }
            case 92: {
                return this.getTime(columnIndex);
            }
            case 93: {
                return this.getTimestamp(columnIndex);
            }
            case -101: {
                return this.getTIMESTAMPTZ(columnIndex);
            }
            case -102: {
                return this.getTIMESTAMPLTZ(columnIndex);
            }
            case 100: {
                return new Float(this.getFloat(columnIndex));
            }
            default: {
                return this.getString(columnIndex);
            }
        }
    }
    
    private Object getObjectDeserializingIfNeeded(final int columnIndex) throws SQLException {
        final Field field = this.fields[columnIndex - 1];
        if (!field.isBinary() && !field.isBlob()) {
            return this.getBytes(columnIndex);
        }
        final byte[] data = this.getBytes(columnIndex);
        if (this.connection.getAutoDeserialize()) {
            Object obj = data;
            if (data != null && data.length >= 2) {
                if (data[0] == -84 && data[1] == -19) {
                    try {
                        final ByteArrayInputStream bytesIn = new ByteArrayInputStream(data);
                        final ObjectInputStream objIn = new ObjectInputStream(bytesIn);
                        obj = objIn.readObject();
                        objIn.close();
                        bytesIn.close();
                        return obj;
                    }
                    catch (ClassNotFoundException cnfe) {
                        throw SQLError.createSQLException(Messages.getString("ResultSet.Class_not_found___91") + cnfe.toString() + Messages.getString("ResultSet._while_reading_serialized_object_92"), this.getExceptionInterceptor());
                    }
                    catch (IOException ex) {
                        obj = data;
                        return obj;
                    }
                }
                return this.getString(columnIndex);
            }
            return obj;
        }
        return data;
    }
    
    @Override
    public <T> T getObject(final int columnIndex, final Class<T> type) throws SQLException {
        if (type == null) {
            throw SQLError.createSQLException("Type parameter can not be null", "S1009", this.getExceptionInterceptor());
        }
        if (type.equals(String.class)) {
            return (T)this.getString(columnIndex);
        }
        if (type.equals(BigDecimal.class)) {
            return (T)this.getBigDecimal(columnIndex);
        }
        if (type.equals(Boolean.class) || type.equals(Boolean.TYPE)) {
            return (T)Boolean.valueOf(this.getBoolean(columnIndex));
        }
        if (type.equals(Integer.class) || type.equals(Integer.TYPE)) {
            return (T)Integer.valueOf(this.getInt(columnIndex));
        }
        if (type.equals(Long.class) || type.equals(Long.TYPE)) {
            return (T)Long.valueOf(this.getLong(columnIndex));
        }
        if (type.equals(Float.class) || type.equals(Float.TYPE)) {
            return (T)Float.valueOf(this.getFloat(columnIndex));
        }
        if (type.equals(Double.class) || type.equals(Double.TYPE)) {
            return (T)Double.valueOf(this.getDouble(columnIndex));
        }
        if (type.equals(byte[].class)) {
            return (T)(Object)this.getBytes(columnIndex);
        }
        if (type.equals(Date.class)) {
            return (T)this.getDate(columnIndex);
        }
        if (type.equals(LocalDateReflection.localDate)) {
            return (T)this.getLocalDate(columnIndex);
        }
        if (type.equals(Time.class)) {
            return (T)this.getTime(columnIndex);
        }
        if (type.equals(LocalTimeReflection.localTime)) {
            return (T)this.getLocalTime(columnIndex);
        }
        if (type.equals(LocalDateTimeReflection.localDateTime)) {
            return (T)this.getLocalDateTime(columnIndex);
        }
        if (type.equals(Timestamp.class)) {
            return (T)this.getTimestamp(columnIndex);
        }
        if (type.equals(Clob.class)) {
            return (T)this.getClob(columnIndex);
        }
        if (type.equals(Blob.class)) {
            return (T)this.getBlob(columnIndex);
        }
        if (type.equals(Array.class)) {
            return (T)this.getArray(columnIndex);
        }
        if (type.equals(Ref.class)) {
            return (T)this.getRef(columnIndex);
        }
        if (type.equals(URL.class)) {
            return (T)this.getURL(columnIndex);
        }
        if (type.equals(TIMESTAMPTZ.class)) {
            return (T)this.getTIMESTAMPTZ(columnIndex);
        }
        if (type.equals(TIMESTAMPLTZ.class)) {
            return (T)this.getTIMESTAMPLTZ(columnIndex);
        }
        if (type.equals(INTERVALDS.class)) {
            return (T)this.getINTERVALDS(columnIndex);
        }
        if (type.equals(INTERVALYM.class)) {
            return (T)this.getINTERVALYM(columnIndex);
        }
        if (type.equals(NUMBER.class)) {
            return (T)this.getNUMBER(columnIndex);
        }
        if (this.connection.getAutoDeserialize()) {
            try {
                return type.cast(this.getObject(columnIndex));
            }
            catch (ClassCastException cce) {
                final SQLException sqlEx = SQLError.createSQLException("Conversion not supported for type " + type.getName(), "S1009", this.getExceptionInterceptor());
                sqlEx.initCause(cce);
                throw sqlEx;
            }
        }
        throw SQLError.createSQLException("Conversion not supported for type " + type.getName(), "S1009", this.getExceptionInterceptor());
    }
    
    @Override
    public <T> T getObject(final String columnLabel, final Class<T> type) throws SQLException {
        return this.getObject(this.findColumn(columnLabel), type);
    }
    
    @Override
    public Object getObject(final int i, final Map<String, Class<?>> map) throws SQLException {
        return this.getObject(i);
    }
    
    @Override
    public Object getObject(final String columnName) throws SQLException {
        return this.getObject(this.findColumn(columnName));
    }
    
    @Override
    public Object getObject(final String colName, final Map<String, Class<?>> map) throws SQLException {
        return this.getObject(this.findColumn(colName), map);
    }
    
    @Override
    public Object getObjectStoredProc(final int columnIndex, final int desiredSqlType) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        final Field field = this.fields[columnIndex - 1];
        Object value = null;
        if (field.getMysqlType() == 163) {
            value = this.thisRow.getComplexRowData(columnIndex - 1);
        }
        else {
            value = this.thisRow.getColumnValue(columnIndex - 1);
        }
        if (value == null) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        switch (field.getMysqlType()) {
            case -7:
            case 16: {
                return this.getBoolean(columnIndex);
            }
            case -6: {
                return this.getInt(columnIndex);
            }
            case 5: {
                return this.getInt(columnIndex);
            }
            case 4: {
                if (!field.isUnsigned() || field.getMysqlType() == 9) {
                    return this.getInt(columnIndex);
                }
                return this.getLong(columnIndex);
            }
            case -5: {
                if (field.isUnsigned()) {
                    return this.getBigDecimal(columnIndex);
                }
                return this.getLong(columnIndex);
            }
            case 2:
            case 3: {
                final String stringVal = this.getString(columnIndex);
                if (stringVal == null) {
                    return null;
                }
                if (stringVal.length() == 0) {
                    final BigDecimal val = new BigDecimal(0);
                    return val;
                }
                BigDecimal val;
                try {
                    val = new BigDecimal(stringVal);
                }
                catch (NumberFormatException ex) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_BigDecimal", new Object[] { stringVal, columnIndex }), "S1009", this.getExceptionInterceptor());
                }
                return val;
            }
            case 7: {
                return new Float(this.getFloat(columnIndex));
            }
            case 6: {
                if (!this.connection.getRunningCTS13()) {
                    return new Double(this.getFloat(columnIndex));
                }
                return new Float(this.getFloat(columnIndex));
            }
            case 8: {
                return new Double(this.getDouble(columnIndex));
            }
            case 1:
            case 12: {
                if (field.getMysqlType() == 211) {
                    return this.getClob(columnIndex);
                }
                if (field.getMysqlType() == 211) {
                    return this.getBlob(columnIndex);
                }
                return this.getString(columnIndex);
            }
            case -1: {
                return this.getStringForClob(columnIndex);
            }
            case -4:
            case -3:
            case -2: {
                return this.getBytes(columnIndex);
            }
            case 91: {
                if (field.getMysqlType() == 13 && !this.connection.getYearIsDateType()) {
                    return this.getShort(columnIndex);
                }
                return this.getDate(columnIndex);
            }
            case 92: {
                return this.getTime(columnIndex);
            }
            case 93: {
                return this.getTimestamp(columnIndex);
            }
            case 163: {
                return this.getCursor(columnIndex);
            }
            case 210: {
                return this.getBlob(columnIndex);
            }
            case 211: {
                return this.getClob(columnIndex);
            }
            default: {
                return this.getString(columnIndex);
            }
        }
    }
    
    @Override
    public Object getObjectStoredProc(final int i, final Map<Object, Object> map, final int desiredSqlType) throws SQLException {
        return this.getObjectStoredProc(i, desiredSqlType);
    }
    
    @Override
    public Object getObjectStoredProc(final String columnName, final int desiredSqlType) throws SQLException {
        return this.getObjectStoredProc(this.findColumn(columnName), desiredSqlType);
    }
    
    @Override
    public Object getObjectStoredProc(final String colName, final Map<Object, Object> map, final int desiredSqlType) throws SQLException {
        return this.getObjectStoredProc(this.findColumn(colName), map, desiredSqlType);
    }
    
    @Override
    public Ref getRef(final int i) throws SQLException {
        this.checkColumnBounds(i);
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Ref getRef(final String colName) throws SQLException {
        return this.getRef(this.findColumn(colName));
    }
    
    @Override
    public int getRow() throws SQLException {
        this.checkClosed();
        final int currentRowNumber = this.rowData.getCurrentRowNumber();
        int row = 0;
        if (!this.rowData.isDynamic()) {
            if (currentRowNumber < 0 || this.rowData.isAfterLast() || this.rowData.isEmpty()) {
                row = 0;
            }
            else {
                row = currentRowNumber + 1;
            }
        }
        else {
            row = currentRowNumber + 1;
        }
        return row;
    }
    
    @Override
    public String getServerInfo() {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                return this.serverInfo;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    private long getNumericRepresentationOfSQLBitType(final int columnIndex) throws SQLException {
        final Object value = this.thisRow.getColumnValue(columnIndex - 1);
        if (this.fields[columnIndex - 1].isSingleBit() || ((byte[])value).length == 1) {
            return ((byte[])value)[0];
        }
        final byte[] asBytes = (byte[])value;
        int shift = 0;
        final long[] steps = new long[asBytes.length];
        for (int i = asBytes.length - 1; i >= 0; --i) {
            steps[i] = (long)(asBytes[i] & 0xFF) << shift;
            shift += 8;
        }
        long valueAsLong = 0L;
        for (int j = 0; j < asBytes.length; ++j) {
            valueAsLong |= steps[j];
        }
        return valueAsLong;
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (!this.isBinaryEncoded) {
            if (this.fields[columnIndex - 1].getMysqlType() == 16) {
                final long valueAsLong = this.getNumericRepresentationOfSQLBitType(columnIndex);
                if (this.jdbcCompliantTruncationForReads && (valueAsLong < -32768L || valueAsLong > 32767L)) {
                    this.throwRangeException(String.valueOf(valueAsLong), columnIndex, 5);
                }
                return (short)valueAsLong;
            }
            if (this.useFastIntParsing) {
                final Object value = this.thisRow.getColumnValue(columnIndex - 1);
                if (value == null) {
                    this.wasNullFlag = true;
                }
                else {
                    this.wasNullFlag = false;
                }
                if (this.wasNullFlag) {
                    return 0;
                }
                final byte[] shortAsBytes = (byte[])value;
                if (shortAsBytes.length == 0) {
                    return (short)this.convertToZeroWithEmptyCheck();
                }
                boolean needsFullParse = false;
                for (int i = 0; i < shortAsBytes.length; ++i) {
                    if ((char)shortAsBytes[i] == 'e' || (char)shortAsBytes[i] == 'E') {
                        needsFullParse = true;
                        break;
                    }
                }
                if (!needsFullParse) {
                    try {
                        return this.parseShortWithOverflowCheck(columnIndex, shortAsBytes, null);
                    }
                    catch (NumberFormatException nfe) {
                        try {
                            return this.parseShortAsDouble(columnIndex, StringUtils.toString(shortAsBytes));
                        }
                        catch (NumberFormatException ex) {
                            throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____96") + StringUtils.toString(shortAsBytes) + "'", "S1009", this.getExceptionInterceptor());
                        }
                    }
                }
            }
            String val = null;
            try {
                val = this.getString(columnIndex);
                if (val == null) {
                    return 0;
                }
                if (val.length() == 0) {
                    return (short)this.convertToZeroWithEmptyCheck();
                }
                if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                    return this.parseShortWithOverflowCheck(columnIndex, null, val);
                }
                return this.parseShortAsDouble(columnIndex, val);
            }
            catch (NumberFormatException nfe2) {
                try {
                    return this.parseShortAsDouble(columnIndex, val);
                }
                catch (NumberFormatException ex2) {
                    throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____96") + val + "'", "S1009", this.getExceptionInterceptor());
                }
            }
        }
        return this.getNativeShort(columnIndex);
    }
    
    @Override
    public short getShort(final String columnName) throws SQLException {
        return this.getShort(this.findColumn(columnName));
    }
    
    private final short getShortFromString(final String val, final int columnIndex) throws SQLException {
        try {
            if (val == null) {
                return 0;
            }
            if (val.length() == 0) {
                return (short)this.convertToZeroWithEmptyCheck();
            }
            if (val.indexOf("e") == -1 && val.indexOf("E") == -1 && val.indexOf(".") == -1) {
                return this.parseShortWithOverflowCheck(columnIndex, null, val);
            }
            return this.parseShortAsDouble(columnIndex, val);
        }
        catch (NumberFormatException nfe) {
            try {
                return this.parseShortAsDouble(columnIndex, val);
            }
            catch (NumberFormatException ex) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Invalid_value_for_getShort()_-____217") + val + Messages.getString("ResultSet.___in_column__218") + columnIndex, "S1009", this.getExceptionInterceptor());
            }
        }
    }
    
    @Override
    public Statement getStatement() throws SQLException {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                if (this.wrapperStatement != null) {
                    return this.wrapperStatement;
                }
                return this.owningStatement;
            }
        }
        catch (SQLException sqlEx) {
            if (!this.retainOwningStatement) {
                throw SQLError.createSQLException("Operation not allowed on closed ResultSet. Statements can be retained over result set closure by setting the connection property \"retainStatementAfterResultSetClose\" to \"true\".", "S1000", this.getExceptionInterceptor());
            }
            if (this.wrapperStatement != null) {
                return this.wrapperStatement;
            }
            return this.owningStatement;
        }
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        this.checkColumnBounds(columnIndex);
        final int columnIndexMinusOne = columnIndex - 1;
        final Field field = this.fields[columnIndexMinusOne];
        if (field != null) {
            switch (field.getMysqlType()) {
                case 210: {
                    throw SQLError.createSQLFeatureNotSupportedException();
                }
                case 211: {
                    final Clob clob = this.getLobClobObject(this.thisRow.getColumnValue(columnIndexMinusOne), this.thisRow.metadata[columnIndexMinusOne].getEncoding(), this.connection, this.getExceptionInterceptor());
                    return (clob == null) ? null : clob.getSubString(1L, (int)clob.length());
                }
                case 4: {
                    if (field.getConnect().getIO().isOracleMode()) {
                        final float floatFromString = this.getFloatFromString(this.getStringInternal(columnIndex, true), columnIndex);
                        return Float.toString(floatFromString);
                    }
                }
                case 5: {
                    if (field.getConnect().getIO().isOracleMode()) {
                        final double doubleFromString = this.getDoubleFromString(this.getStringInternal(columnIndex, true), columnIndex);
                        return Double.toString(doubleFromString);
                    }
                    break;
                }
            }
        }
        String stringVal = this.getStringInternal(columnIndex, true);
        if (this.padCharsWithSpace && stringVal != null && field.getMysqlType() == 254) {
            final int fieldLength = (int)field.getLength() / field.getMaxBytesPerCharacter();
            final int currentLength = stringVal.length();
            if (currentLength < fieldLength) {
                final StringBuilder paddedBuf = new StringBuilder(fieldLength);
                paddedBuf.append(stringVal);
                final int difference = fieldLength - currentLength;
                paddedBuf.append(ResultSetImpl.EMPTY_SPACE, 0, difference);
                stringVal = paddedBuf.toString();
            }
        }
        return stringVal;
    }
    
    @Override
    public String getString(final String columnName) throws SQLException {
        return this.getString(this.findColumn(columnName));
    }
    
    private String getStringForClob(final int columnIndex) throws SQLException {
        String asString = null;
        final String forcedEncoding = this.connection.getClobCharacterEncoding();
        if (forcedEncoding == null) {
            if (!this.isBinaryEncoded) {
                asString = this.getString(columnIndex);
            }
            else {
                asString = this.getNativeString(columnIndex);
            }
        }
        else {
            try {
                byte[] asBytes = null;
                if (!this.isBinaryEncoded) {
                    asBytes = this.getBytes(columnIndex);
                }
                else {
                    asBytes = this.getNativeBytes(columnIndex, true);
                }
                if (asBytes != null) {
                    asString = StringUtils.toString(asBytes, forcedEncoding);
                }
            }
            catch (UnsupportedEncodingException uee) {
                throw SQLError.createSQLException("Unsupported character encoding " + forcedEncoding, "S1009", this.getExceptionInterceptor());
            }
        }
        return asString;
    }
    
    protected String getStringInternal(final int columnIndex, final boolean checkDateTypes) throws SQLException {
        final int fieldType = this.fields[columnIndex - 1].getMysqlType();
        if (this.connection.getIO().isOracleMode()) {
            if (fieldType == 202) {
                final TIMESTAMP timestamp = this.getTIMESTAMP(columnIndex);
                if (timestamp != null) {
                    this.wasNullFlag = false;
                    return timestamp.stringValue(this.connection);
                }
                this.wasNullFlag = true;
                return null;
            }
            else if (fieldType == 200) {
                final TIMESTAMPTZ timestamptz = this.getTIMESTAMPTZ(columnIndex);
                if (timestamptz != null) {
                    this.wasNullFlag = false;
                    return timestamptz.toResultSetString(this.connection);
                }
                this.wasNullFlag = true;
                return null;
            }
            else if (fieldType == 201) {
                final TIMESTAMPLTZ timestampltz = this.getTIMESTAMPLTZ(columnIndex);
                if (timestampltz != null) {
                    this.wasNullFlag = false;
                    return timestampltz.toResultSetString(this.connection);
                }
                this.wasNullFlag = true;
                return null;
            }
            else if (fieldType == 203) {
                final byte[] returnBytes = this.thisRow.getColumnValue(columnIndex - 1);
                if (returnBytes != null) {
                    this.wasNullFlag = false;
                    final StringBuilder sb = new StringBuilder();
                    for (final byte b : returnBytes) {
                        sb.append(String.format("%02x", b));
                    }
                    return sb.toString();
                }
                this.wasNullFlag = true;
                return null;
            }
            else if (fieldType == 205) {
                final byte[] returnBytes = this.thisRow.getColumnValue(columnIndex - 1);
                if (returnBytes != null) {
                    this.wasNullFlag = false;
                    return INTERVALDS.toString(returnBytes);
                }
                this.wasNullFlag = true;
                return null;
            }
            else if (fieldType == 204) {
                final byte[] returnBytes = this.thisRow.getColumnValue(columnIndex - 1);
                if (returnBytes != null) {
                    this.wasNullFlag = false;
                    return INTERVALYM.toString(returnBytes);
                }
                this.wasNullFlag = true;
                return null;
            }
        }
        if (this.isBinaryEncoded) {
            return this.getNativeString(columnIndex);
        }
        this.checkRowPos();
        this.checkColumnBounds(columnIndex);
        if (this.fields == null) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Query_generated_no_fields_for_ResultSet_99"), "S1002", this.getExceptionInterceptor());
        }
        final int internalColumnIndex = columnIndex - 1;
        if (this.thisRow.isNull(internalColumnIndex)) {
            this.wasNullFlag = true;
            return null;
        }
        this.wasNullFlag = false;
        final Field metadata = this.fields[internalColumnIndex];
        String stringVal = null;
        if (metadata.getMysqlType() == 16) {
            if (!metadata.isSingleBit()) {
                return String.valueOf(this.getNumericRepresentationOfSQLBitType(columnIndex));
            }
            final byte[] value = this.thisRow.getColumnValue(internalColumnIndex);
            if (value.length == 0) {
                return String.valueOf(this.convertToZeroWithEmptyCheck());
            }
            return String.valueOf(value[0]);
        }
        else {
            final String encoding = metadata.getEncoding();
            stringVal = this.thisRow.getString(internalColumnIndex, encoding, this.connection);
            if (metadata.getMysqlType() != 13) {
                if (checkDateTypes && !this.connection.getNoDatetimeStringSync()) {
                    switch (metadata.getSQLType()) {
                        case 92: {
                            final Time tm = this.getTimeFromString(stringVal, null, columnIndex, this.getDefaultTimeZone(), false);
                            if (tm == null) {
                                this.wasNullFlag = true;
                                return null;
                            }
                            this.wasNullFlag = false;
                            return tm.toString();
                        }
                        case 91: {
                            final Date dt = this.getDateFromString(stringVal, columnIndex, null);
                            if (dt == null) {
                                this.wasNullFlag = true;
                                return null;
                            }
                            this.wasNullFlag = false;
                            return dt.toString();
                        }
                        case 93: {
                            final Timestamp ts = this.getTimestampFromString(columnIndex, null, stringVal, this.getDefaultTimeZone(), false);
                            if (ts == null) {
                                this.wasNullFlag = true;
                                return null;
                            }
                            this.wasNullFlag = false;
                            return ts.toString();
                        }
                    }
                }
                return stringVal;
            }
            if (!this.connection.getYearIsDateType()) {
                return stringVal;
            }
            final Date dt2 = this.getDateFromString(stringVal, columnIndex, null);
            if (dt2 == null) {
                this.wasNullFlag = true;
                return null;
            }
            this.wasNullFlag = false;
            return dt2.toString();
        }
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return this.getTimeInternal(columnIndex, null, this.getDefaultTimeZone(), false);
    }
    
    public Object getLocalTime(final int columnIndex) throws SQLException {
        return (this.getTimeInternal(columnIndex, null, this.getDefaultTimeZone(), false) == null) ? null : SqlTimeReflection.toLocalTime(this.getTimeInternal(columnIndex, null, this.getDefaultTimeZone(), false));
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return this.getTimeInternal(columnIndex, cal, (cal == null) ? Calendar.getInstance().getTimeZone() : cal.getTimeZone(), true);
    }
    
    public Object getLocalTime(final int columnIndex, final Calendar cal) throws SQLException {
        return (this.getTimeInternal(columnIndex, cal, this.getCurrentTimeZone(cal), true) == null) ? null : SqlTimeReflection.toLocalTime(this.getTimeInternal(columnIndex, cal, this.getCurrentTimeZone(cal), true));
    }
    
    @Override
    public Time getTime(final String columnName) throws SQLException {
        return this.getTime(this.findColumn(columnName));
    }
    
    public Object getLocalTime(final String columnName) throws SQLException {
        return SqlTimeReflection.toLocalTime((this.getTime(this.findColumn(columnName)) == null) ? null : this.getTime(this.findColumn(columnName)));
    }
    
    @Override
    public Time getTime(final String columnName, final Calendar cal) throws SQLException {
        return this.getTime(this.findColumn(columnName), cal);
    }
    
    public Object getLocalTime(final String columnName, final Calendar cal) throws SQLException {
        return SqlTimeReflection.toLocalTime((this.getTime(this.findColumn(columnName), cal) == null) ? null : this.getTime(this.findColumn(columnName), cal));
    }
    
    private Time getTimeFromString(String timeAsString, final Calendar targetCalendar, final int columnIndex, final TimeZone tz, final boolean rollForward) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            int hr = 0;
            int min = 0;
            int sec = 0;
            try {
                if (timeAsString == null) {
                    this.wasNullFlag = true;
                    return null;
                }
                timeAsString = timeAsString.trim();
                final int dec = timeAsString.indexOf(".");
                if (dec > -1) {
                    timeAsString = timeAsString.substring(0, dec);
                }
                if (!timeAsString.equals("0") && !timeAsString.equals("0000-00-00") && !timeAsString.equals("0000-00-00 00:00:00") && !timeAsString.equals("00000000000000")) {
                    this.wasNullFlag = false;
                    final Field timeColField = this.fields[columnIndex - 1];
                    if (timeColField.getMysqlType() == 7) {
                        final int length = timeAsString.length();
                        switch (length) {
                            case 19: {
                                hr = Integer.parseInt(timeAsString.substring(length - 8, length - 6));
                                min = Integer.parseInt(timeAsString.substring(length - 5, length - 3));
                                sec = Integer.parseInt(timeAsString.substring(length - 2, length));
                                break;
                            }
                            case 12:
                            case 14: {
                                hr = Integer.parseInt(timeAsString.substring(length - 6, length - 4));
                                min = Integer.parseInt(timeAsString.substring(length - 4, length - 2));
                                sec = Integer.parseInt(timeAsString.substring(length - 2, length));
                                break;
                            }
                            case 10: {
                                hr = Integer.parseInt(timeAsString.substring(6, 8));
                                min = Integer.parseInt(timeAsString.substring(8, 10));
                                sec = 0;
                                break;
                            }
                            default: {
                                throw SQLError.createSQLException(Messages.getString("ResultSet.Timestamp_too_small_to_convert_to_Time_value_in_column__257") + columnIndex + "(" + this.fields[columnIndex - 1] + ").", "S1009", this.getExceptionInterceptor());
                            }
                        }
                        final SQLWarning precisionLost = new SQLWarning(Messages.getString("ResultSet.Precision_lost_converting_TIMESTAMP_to_Time_with_getTime()_on_column__261") + columnIndex + "(" + this.fields[columnIndex - 1] + ").");
                        if (this.warningChain == null) {
                            this.warningChain = precisionLost;
                        }
                        else {
                            this.warningChain.setNextWarning(precisionLost);
                        }
                    }
                    else if (timeColField.getMysqlType() == 12) {
                        hr = Integer.parseInt(timeAsString.substring(11, 13));
                        min = Integer.parseInt(timeAsString.substring(14, 16));
                        sec = Integer.parseInt(timeAsString.substring(17, 19));
                        final SQLWarning precisionLost2 = new SQLWarning(Messages.getString("ResultSet.Precision_lost_converting_DATETIME_to_Time_with_getTime()_on_column__264") + columnIndex + "(" + this.fields[columnIndex - 1] + ").");
                        if (this.warningChain == null) {
                            this.warningChain = precisionLost2;
                        }
                        else {
                            this.warningChain.setNextWarning(precisionLost2);
                        }
                    }
                    else {
                        if (timeColField.getMysqlType() == 10) {
                            return this.fastTimeCreate(targetCalendar, 0, 0, 0);
                        }
                        if (timeAsString.length() != 5 && timeAsString.length() != 8) {
                            throw SQLError.createSQLException(Messages.getString("ResultSet.Bad_format_for_Time____267") + timeAsString + Messages.getString("ResultSet.___in_column__268") + columnIndex, "S1009", this.getExceptionInterceptor());
                        }
                        hr = Integer.parseInt(timeAsString.substring(0, 2));
                        min = Integer.parseInt(timeAsString.substring(3, 5));
                        sec = ((timeAsString.length() == 5) ? 0 : Integer.parseInt(timeAsString.substring(6)));
                    }
                    final Calendar sessionCalendar = this.getCalendarInstanceForSessionOrNew();
                    return TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, this.fastTimeCreate(sessionCalendar, hr, min, sec), this.connection.getServerTimezoneTZ(), tz, rollForward);
                }
                if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                    this.wasNullFlag = true;
                    return null;
                }
                if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                    throw SQLError.createSQLException("Value '" + timeAsString + "' can not be represented as java.sql.Time", "S1009", this.getExceptionInterceptor());
                }
                return this.fastTimeCreate(targetCalendar, 0, 0, 0);
            }
            catch (RuntimeException ex) {
                final SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", this.getExceptionInterceptor());
                sqlEx.initCause(ex);
                throw sqlEx;
            }
        }
    }
    
    private Time getTimeInternal(final int columnIndex, final Calendar targetCalendar, final TimeZone tz, final boolean rollForward) throws SQLException {
        this.checkRowPos();
        final int fieldType = this.fields[columnIndex - 1].getMysqlType();
        if (((ConnectionImpl)this.connection).isOracleMode() && (fieldType == 202 || fieldType == 201 || fieldType == 200)) {
            final TIMESTAMP oracleTimestamp = this.getTIMESTAMP(columnIndex, fieldType);
            if (oracleTimestamp == null) {
                this.wasNullFlag = true;
                return null;
            }
            this.wasNullFlag = false;
            return new Time(oracleTimestamp.timestampValue(targetCalendar).getTime());
        }
        else {
            if (this.isBinaryEncoded) {
                return this.getNativeTime(columnIndex, targetCalendar, tz, rollForward);
            }
            if (!this.useFastDateParsing) {
                final String timeAsString = this.getStringInternal(columnIndex, false);
                return this.getTimeFromString(timeAsString, targetCalendar, columnIndex, tz, rollForward);
            }
            this.checkColumnBounds(columnIndex);
            final int columnIndexMinusOne = columnIndex - 1;
            if (this.thisRow.isNull(columnIndexMinusOne)) {
                this.wasNullFlag = true;
                return null;
            }
            this.wasNullFlag = false;
            return this.thisRow.getTimeFast(columnIndexMinusOne, targetCalendar, tz, rollForward, this.connection, this);
        }
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return this.getTimestampInternal(columnIndex, null, this.getDefaultTimeZone(), false);
    }
    
    public Object getLocalDateTime(final int columnIndex) throws SQLException {
        return (this.getTimestampInternal(columnIndex, null, this.getDefaultTimeZone(), false) == null) ? null : SqlTimeStampReflection.toLocalTime(this.getTimestampInternal(columnIndex, null, this.getDefaultTimeZone(), false));
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return this.getTimestampInternal(columnIndex, cal, (cal == null) ? Calendar.getInstance().getTimeZone() : cal.getTimeZone(), true);
    }
    
    public Object getLocalDateTime(final int columnIndex, final Calendar cal) throws SQLException {
        return (this.getTimestampInternal(columnIndex, cal, this.getCurrentTimeZone(cal), true) == null) ? null : SqlTimeStampReflection.toLocalTime(this.getTimestampInternal(columnIndex, cal, this.getCurrentTimeZone(cal), true));
    }
    
    @Override
    public Timestamp getTimestamp(final String columnName) throws SQLException {
        return this.getTimestamp(this.findColumn(columnName));
    }
    
    public Object getLocalDateTime(final String columnName) throws SQLException {
        return SqlTimeStampReflection.toLocalTime((this.getTimestamp(this.findColumn(columnName)) == null) ? null : this.getTimestamp(this.findColumn(columnName)));
    }
    
    @Override
    public Timestamp getTimestamp(final String columnName, final Calendar cal) throws SQLException {
        return this.getTimestamp(this.findColumn(columnName), cal);
    }
    
    public Object getLocalDateTime(final String columnName, final Calendar cal) throws SQLException {
        return SqlTimeStampReflection.toLocalTime((this.getTimestamp(this.findColumn(columnName), cal) == null) ? null : this.getTimestamp(this.findColumn(columnName), cal));
    }
    
    private TIMESTAMP getTIMESTAMP(final int columnIndex, final int fieldsType) throws SQLException {
        if (fieldsType == 202) {
            return this.getTIMESTAMP(columnIndex);
        }
        if (fieldsType == 200) {
            final TIMESTAMPTZ oracleTimestampZ = this.getTIMESTAMPTZ(columnIndex);
            if (oracleTimestampZ == null) {
                return null;
            }
            return TIMESTAMPTZ.toTIMESTAMP(this.connection, oracleTimestampZ.toBytes());
        }
        else {
            if (fieldsType != 201) {
                return null;
            }
            final TIMESTAMPLTZ oracleTimestampLTZ = this.getTIMESTAMPLTZ(columnIndex);
            if (oracleTimestampLTZ == null) {
                return null;
            }
            return TIMESTAMPLTZ.toTIMESTAMP(this.connection, oracleTimestampLTZ.toBytes());
        }
    }
    
    public TIMESTAMP getTIMESTAMP(final int columnIndex) throws SQLException {
        final int internalColumnIndex = columnIndex - 1;
        String timestampStr = null;
        if (this.fields[internalColumnIndex].getMysqlType() != 202) {
            throw new SQLException("the field type is not FIELD_TYPE_TIMESTAMP");
        }
        final byte[] returnBytes = this.thisRow.getColumnValue(internalColumnIndex);
        if (null == returnBytes) {
            return null;
        }
        if (returnBytes.length < 12) {
            throw new SQLException(String.format("timestamp field data length is invalid, expected 12 at least , actual length is %d", returnBytes.length));
        }
        timestampStr = this.buildTimestampStr(returnBytes);
        final TIMESTAMP timestamp = new TIMESTAMP(Timestamp.valueOf(timestampStr));
        timestamp.setByte(11, returnBytes[11]);
        return timestamp;
    }
    
    public TIMESTAMP getTIMESTAMP(final String columnName) throws SQLException {
        return this.getTIMESTAMP(this.findColumn(columnName));
    }
    
    @Override
    public TIMESTAMPTZ getTIMESTAMPTZ(final int columnIndex) throws SQLException {
        final int internalColumnIndex = columnIndex - 1;
        if (this.fields[internalColumnIndex].getMysqlType() != 200) {
            throw new SQLException("the field type is not FIELD_TYPE_TIMESTAMPTZ");
        }
        final byte[] returnBytes = this.thisRow.getColumnValue(internalColumnIndex);
        if (null == returnBytes) {
            return null;
        }
        if (returnBytes.length < 16) {
            throw new SQLException(String.format("timestamp field data length is invalid, expected >= 16, actual length is %d", returnBytes.length));
        }
        final TIMESTAMPTZ timestamptz = new TIMESTAMPTZ(returnBytes);
        timestamptz.setByte(11, returnBytes[11]);
        return timestamptz;
    }
    
    @Override
    public TIMESTAMPTZ getTIMESTAMPTZ(final String columnName) throws SQLException {
        return this.getTIMESTAMPTZ(this.findColumn(columnName));
    }
    
    @Override
    public TIMESTAMPLTZ getTIMESTAMPLTZ(final int columnIndex) throws SQLException {
        final int internalColumnIndex = columnIndex - 1;
        if (this.fields[internalColumnIndex].getMysqlType() != 201) {
            throw new SQLException("the field type is not FIELD_TYPE_TIMESTAMPLTZ");
        }
        final byte[] returnBytes = this.thisRow.getColumnValue(internalColumnIndex);
        if (null == returnBytes) {
            return null;
        }
        if (returnBytes.length < 12) {
            throw new SQLException(String.format("timestamp field data length is invalid, expected 12 at least, actual length is %d", returnBytes.length));
        }
        final TIMESTAMPLTZ timestampltz = new TIMESTAMPLTZ(returnBytes);
        return timestampltz;
    }
    
    @Override
    public TIMESTAMPLTZ getTIMESTAMPLTZ(final String columnName) throws SQLException {
        return this.getTIMESTAMPLTZ(this.findColumn(columnName));
    }
    
    private boolean isNUMBERTYPE(final int mysqlType) {
        return mysqlType == 246 || mysqlType == 5 || mysqlType == 4 || mysqlType == 3 || mysqlType == 8 || mysqlType == 2 || mysqlType == 206 || mysqlType == 9 || mysqlType == 0;
    }
    
    @Override
    public NUMBER getNUMBER(final int columnIndex) throws SQLException {
        final int internalColumnIndex = columnIndex - 1;
        if (!this.isNUMBERTYPE(this.fields[internalColumnIndex].getMysqlType())) {
            throw new SQLException("the field type is not FIELD_TYPE_NUMBER : " + this.fields[internalColumnIndex].getMysqlType());
        }
        final byte[] returnBytes = this.thisRow.getColumnValue(internalColumnIndex);
        if (null == returnBytes) {
            return null;
        }
        final NUMBER number = new NUMBER(returnBytes);
        return number;
    }
    
    @Override
    public NUMBER getNUMBER(final String columnName) throws SQLException {
        return this.getNUMBER(this.findColumn(columnName));
    }
    
    @Override
    public INTERVALDS getINTERVALDS(final int columnIndex) throws SQLException {
        final int internalColumnIndex = columnIndex - 1;
        if (this.fields[internalColumnIndex].getMysqlType() != 205) {
            throw new SQLException("the field type is not FIELD_TYPE_INTERVALDS : " + this.fields[internalColumnIndex].getMysqlType());
        }
        final byte[] returnBytes = this.thisRow.getColumnValue(internalColumnIndex);
        if (null == returnBytes) {
            return null;
        }
        if (returnBytes.length < 14) {
            throw new SQLException(String.format("timestamp field data length is invalid, expected 14 at least, actual length is %d", returnBytes.length));
        }
        final INTERVALDS intervalds = new INTERVALDS(returnBytes);
        return intervalds;
    }
    
    @Override
    public INTERVALDS getINTERVALDS(final String columnName) throws SQLException {
        return this.getINTERVALDS(this.findColumn(columnName));
    }
    
    @Override
    public INTERVALYM getINTERVALYM(final String columnName) throws SQLException {
        return this.getINTERVALYM(this.findColumn(columnName));
    }
    
    @Override
    public INTERVALYM getINTERVALYM(final int columnIndex) throws SQLException {
        final int internalColumnIndex = columnIndex - 1;
        if (this.fields[internalColumnIndex].getMysqlType() != 204) {
            throw new SQLException("the field type is not FIELD_TYPE_INTERVALYM : " + this.fields[internalColumnIndex].getMysqlType());
        }
        final byte[] returnBytes = this.thisRow.getColumnValue(internalColumnIndex);
        if (null == returnBytes) {
            return null;
        }
        if (returnBytes.length < 7) {
            throw new SQLException(String.format("timestamp field data length is invalid, expected 7 at least, actual length is %d", returnBytes.length));
        }
        final INTERVALYM intervalym = new INTERVALYM(returnBytes);
        return intervalym;
    }
    
    private String buildTimestamp(final byte b) throws SQLException {
        if (b < 10) {
            return "0" + b;
        }
        return "" + b;
    }
    
    private String buildTimestampStr(final byte[] bytes) throws SQLException {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.buildTimestamp(bytes[0]));
        sb.append(this.buildTimestamp(bytes[1]));
        sb.append("-");
        sb.append(this.buildTimestamp(bytes[2]));
        sb.append("-");
        sb.append(this.buildTimestamp(bytes[3]));
        sb.append(" ");
        sb.append(this.buildTimestamp(bytes[4]));
        sb.append(":");
        sb.append(this.buildTimestamp(bytes[5]));
        sb.append(":");
        sb.append(this.buildTimestamp(bytes[6]));
        sb.append(".");
        final byte[] nanosBytes = new byte[4];
        System.arraycopy(bytes, 7, nanosBytes, 0, 4);
        final int nanos = MysqlCommonUtils.bytes2Int(nanosBytes);
        String temp = String.format("%09d", nanos);
        char[] chars;
        int index;
        for (chars = temp.toCharArray(), index = chars.length; index > 1 && chars[index - 1] == '0'; --index) {}
        temp = temp.substring(0, index);
        final int scale = bytes[11];
        if (scale > temp.length()) {
            final int x = scale - temp.length();
            final StringBuilder strBuf = new StringBuilder();
            for (int i = 0; i < x; ++i) {
                strBuf.append("0");
            }
            temp += strBuf.toString();
        }
        sb.append(temp);
        return sb.toString();
    }
    
    private Timestamp getTimestampFromString(final int columnIndex, final Calendar targetCalendar, String timestampValue, final TimeZone tz, final boolean rollForward) throws SQLException {
        try {
            this.wasNullFlag = false;
            if (timestampValue == null) {
                this.wasNullFlag = true;
                return null;
            }
            timestampValue = timestampValue.trim();
            int length = timestampValue.length();
            final Calendar sessionCalendar = this.connection.getUseJDBCCompliantTimezoneShift() ? this.connection.getUtcCalendar() : this.getCalendarInstanceForSessionOrNew();
            if (length > 0 && timestampValue.charAt(0) == '0' && (timestampValue.equals("0000-00-00") || timestampValue.equals("0000-00-00 00:00:00") || timestampValue.equals("00000000000000") || timestampValue.equals("0"))) {
                if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                    this.wasNullFlag = true;
                    return null;
                }
                if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                    throw SQLError.createSQLException("Value '" + timestampValue + "' can not be represented as java.sql.Timestamp", "S1009", this.getExceptionInterceptor());
                }
                return this.fastTimestampCreate(null, 1, 1, 1, 0, 0, 0, 0);
            }
            else if (this.fields[columnIndex - 1].getMysqlType() == 13) {
                if (!this.useLegacyDatetimeCode) {
                    return TimeUtil.fastTimestampCreate(tz, Integer.parseInt(timestampValue.substring(0, 4)), 1, 1, 0, 0, 0, 0);
                }
                return TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, this.fastTimestampCreate(sessionCalendar, Integer.parseInt(timestampValue.substring(0, 4)), 1, 1, 0, 0, 0, 0), this.connection.getServerTimezoneTZ(), tz, rollForward);
            }
            else {
                int year = 0;
                int month = 0;
                int day = 0;
                int hour = 0;
                int minutes = 0;
                int seconds = 0;
                int nanos = 0;
                final int decimalIndex = timestampValue.indexOf(".");
                if (decimalIndex == length - 1) {
                    --length;
                }
                else if (decimalIndex != -1) {
                    if (decimalIndex + 2 > length) {
                        throw new IllegalArgumentException();
                    }
                    nanos = Integer.parseInt(timestampValue.substring(decimalIndex + 1));
                    final int numDigits = length - (decimalIndex + 1);
                    if (numDigits < 9) {
                        final int factor = (int)Math.pow(10.0, 9 - numDigits);
                        nanos *= factor;
                    }
                    length = decimalIndex;
                }
                switch (length) {
                    case 19:
                    case 20:
                    case 21:
                    case 22:
                    case 23:
                    case 24:
                    case 25:
                    case 26: {
                        year = Integer.parseInt(timestampValue.substring(0, 4));
                        month = Integer.parseInt(timestampValue.substring(5, 7));
                        day = Integer.parseInt(timestampValue.substring(8, 10));
                        hour = Integer.parseInt(timestampValue.substring(11, 13));
                        minutes = Integer.parseInt(timestampValue.substring(14, 16));
                        seconds = Integer.parseInt(timestampValue.substring(17, 19));
                        break;
                    }
                    case 14: {
                        year = Integer.parseInt(timestampValue.substring(0, 4));
                        month = Integer.parseInt(timestampValue.substring(4, 6));
                        day = Integer.parseInt(timestampValue.substring(6, 8));
                        hour = Integer.parseInt(timestampValue.substring(8, 10));
                        minutes = Integer.parseInt(timestampValue.substring(10, 12));
                        seconds = Integer.parseInt(timestampValue.substring(12, 14));
                        break;
                    }
                    case 12: {
                        year = Integer.parseInt(timestampValue.substring(0, 2));
                        if (year <= 69) {
                            year += 100;
                        }
                        year += 1900;
                        month = Integer.parseInt(timestampValue.substring(2, 4));
                        day = Integer.parseInt(timestampValue.substring(4, 6));
                        hour = Integer.parseInt(timestampValue.substring(6, 8));
                        minutes = Integer.parseInt(timestampValue.substring(8, 10));
                        seconds = Integer.parseInt(timestampValue.substring(10, 12));
                        break;
                    }
                    case 10: {
                        if (this.fields[columnIndex - 1].getMysqlType() == 10 || timestampValue.indexOf("-") != -1) {
                            year = Integer.parseInt(timestampValue.substring(0, 4));
                            month = Integer.parseInt(timestampValue.substring(5, 7));
                            day = Integer.parseInt(timestampValue.substring(8, 10));
                            hour = 0;
                            minutes = 0;
                            break;
                        }
                        year = Integer.parseInt(timestampValue.substring(0, 2));
                        if (year <= 69) {
                            year += 100;
                        }
                        month = Integer.parseInt(timestampValue.substring(2, 4));
                        day = Integer.parseInt(timestampValue.substring(4, 6));
                        hour = Integer.parseInt(timestampValue.substring(6, 8));
                        minutes = Integer.parseInt(timestampValue.substring(8, 10));
                        year += 1900;
                        break;
                    }
                    case 8: {
                        if (timestampValue.indexOf(":") != -1) {
                            hour = Integer.parseInt(timestampValue.substring(0, 2));
                            minutes = Integer.parseInt(timestampValue.substring(3, 5));
                            seconds = Integer.parseInt(timestampValue.substring(6, 8));
                            year = 1970;
                            month = 1;
                            day = 1;
                            break;
                        }
                        year = Integer.parseInt(timestampValue.substring(0, 4));
                        month = Integer.parseInt(timestampValue.substring(4, 6));
                        day = Integer.parseInt(timestampValue.substring(6, 8));
                        year -= 1900;
                        --month;
                        break;
                    }
                    case 6: {
                        year = Integer.parseInt(timestampValue.substring(0, 2));
                        if (year <= 69) {
                            year += 100;
                        }
                        year += 1900;
                        month = Integer.parseInt(timestampValue.substring(2, 4));
                        day = Integer.parseInt(timestampValue.substring(4, 6));
                        break;
                    }
                    case 4: {
                        year = Integer.parseInt(timestampValue.substring(0, 2));
                        if (year <= 69) {
                            year += 100;
                        }
                        year += 1900;
                        month = Integer.parseInt(timestampValue.substring(2, 4));
                        day = 1;
                        break;
                    }
                    case 2: {
                        year = Integer.parseInt(timestampValue.substring(0, 2));
                        if (year <= 69) {
                            year += 100;
                        }
                        year += 1900;
                        month = 1;
                        day = 1;
                        break;
                    }
                    default: {
                        throw new SQLException("Bad format for Timestamp '" + timestampValue + "' in column " + columnIndex + ".", "S1009");
                    }
                }
                if (!this.useLegacyDatetimeCode) {
                    return TimeUtil.fastTimestampCreate(tz, year, month, day, hour, minutes, seconds, nanos);
                }
                return TimeUtil.changeTimezone(this.connection, sessionCalendar, targetCalendar, this.fastTimestampCreate(sessionCalendar, year, month, day, hour, minutes, seconds, nanos), this.connection.getServerTimezoneTZ(), tz, rollForward);
            }
        }
        catch (RuntimeException e) {
            final SQLException sqlEx = SQLError.createSQLException("Cannot convert value '" + timestampValue + "' from column " + columnIndex + " to TIMESTAMP.", "S1009", this.getExceptionInterceptor());
            sqlEx.initCause(e);
            throw sqlEx;
        }
    }
    
    private Timestamp getTimestampInternal(final int columnIndex, final Calendar targetCalendar, final TimeZone tz, final boolean rollForward) throws SQLException {
        final int fieldType = this.fields[columnIndex - 1].getMysqlType();
        if (((ConnectionImpl)this.connection).isOracleMode() && (fieldType == 202 || fieldType == 201 || fieldType == 200)) {
            final TIMESTAMP oracleTimestamp = this.getTIMESTAMP(columnIndex, fieldType);
            if (oracleTimestamp == null) {
                this.wasNullFlag = true;
                return null;
            }
            this.wasNullFlag = false;
            return oracleTimestamp.timestampValue(targetCalendar);
        }
        else {
            if (this.isBinaryEncoded) {
                return this.getNativeTimestamp(columnIndex, targetCalendar, tz, rollForward);
            }
            Timestamp tsVal = null;
            if (!this.useFastDateParsing) {
                final String timestampValue = this.getStringInternal(columnIndex, false);
                tsVal = this.getTimestampFromString(columnIndex, targetCalendar, timestampValue, tz, rollForward);
            }
            else {
                this.checkClosed();
                this.checkRowPos();
                this.checkColumnBounds(columnIndex);
                tsVal = this.thisRow.getTimestampFast(columnIndex - 1, targetCalendar, tz, rollForward, this.connection, this);
            }
            if (tsVal == null) {
                this.wasNullFlag = true;
            }
            else {
                this.wasNullFlag = false;
            }
            return tsVal;
        }
    }
    
    @Override
    public int getType() throws SQLException {
        return this.resultSetType;
    }
    
    @Deprecated
    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        if (!this.isBinaryEncoded) {
            this.checkRowPos();
            return this.getBinaryStream(columnIndex);
        }
        return this.getNativeBinaryStream(columnIndex);
    }
    
    @Deprecated
    @Override
    public InputStream getUnicodeStream(final String columnName) throws SQLException {
        return this.getUnicodeStream(this.findColumn(columnName));
    }
    
    @Override
    public long getUpdateCount() {
        return this.updateCount;
    }
    
    @Override
    public long getUpdateID() {
        return this.updateId;
    }
    
    @Override
    public URL getURL(final int colIndex) throws SQLException {
        final String val = this.getString(colIndex);
        if (val == null) {
            return null;
        }
        try {
            return new URL(val);
        }
        catch (MalformedURLException mfe) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Malformed_URL____104") + val + "'", "S1009", this.getExceptionInterceptor());
        }
    }
    
    @Override
    public URL getURL(final String colName) throws SQLException {
        final String val = this.getString(colName);
        if (val == null) {
            return null;
        }
        try {
            return new URL(val);
        }
        catch (MalformedURLException mfe) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Malformed_URL____107") + val + "'", "S1009", this.getExceptionInterceptor());
        }
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.warningChain;
        }
    }
    
    @Override
    public void insertRow() throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public boolean isAfterLast() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final boolean b = this.rowData.isAfterLast();
            return b;
        }
    }
    
    @Override
    public boolean isBeforeFirst() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.rowData.isBeforeFirst();
        }
    }
    
    @Override
    public boolean isFirst() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.rowData.isFirst();
        }
    }
    
    @Override
    public boolean isLast() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            return this.rowData.isLast();
        }
    }
    
    private void issueConversionViaParsingWarning(final String methodName, final int columnIndex, final Object value, final Field fieldInfo, final int[] typesWithNoParseConversion) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            final StringBuilder originalQueryBuf = new StringBuilder();
            if (this.owningStatement != null && this.owningStatement instanceof PreparedStatement) {
                originalQueryBuf.append(Messages.getString("ResultSet.CostlyConversionCreatedFromQuery"));
                originalQueryBuf.append(((PreparedStatement)this.owningStatement).originalSql);
                originalQueryBuf.append("\n\n");
            }
            else {
                originalQueryBuf.append(".");
            }
            final StringBuilder convertibleTypesBuf = new StringBuilder();
            for (int i = 0; i < typesWithNoParseConversion.length; ++i) {
                convertibleTypesBuf.append(MysqlDefs.typeToName(typesWithNoParseConversion[i]));
                convertibleTypesBuf.append("\n");
            }
            final String message = Messages.getString("ResultSet.CostlyConversion", new Object[] { methodName, columnIndex + 1, fieldInfo.getOriginalName(), fieldInfo.getOriginalTableName(), originalQueryBuf.toString(), (value != null) ? value.getClass().getName() : com.alipay.oceanbase.jdbc.ResultSetMetaData.getClassNameForJavaType(fieldInfo.getSQLType(), fieldInfo.isUnsigned(), fieldInfo.getMysqlType(), fieldInfo.isBinary() || fieldInfo.isBlob(), fieldInfo.isOpaqueBinary(), this.connection.getYearIsDateType()), MysqlDefs.typeToName(fieldInfo.getMysqlType()), convertibleTypesBuf.toString() });
            this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", (this.owningStatement == null) ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, (this.owningStatement == null) ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
        }
    }
    
    @Override
    public boolean last() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            boolean b = true;
            if (this.rowData.size() == 0) {
                b = false;
            }
            else {
                if (this.onInsertRow) {
                    this.onInsertRow = false;
                }
                if (this.doingUpdates) {
                    this.doingUpdates = false;
                }
                if (this.thisRow != null) {
                    this.thisRow.closeOpenStreams();
                }
                this.rowData.beforeLast();
                this.thisRow = this.rowData.next();
            }
            this.setRowPositionValidity();
            return b;
        }
    }
    
    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void moveToInsertRow() throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public boolean next() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.onInsertRow = false;
            }
            if (this.doingUpdates) {
                this.doingUpdates = false;
            }
            if (!this.reallyResult()) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.ResultSet_is_from_UPDATE._No_Data_115"), "S1000", this.getExceptionInterceptor());
            }
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            boolean b;
            if (this.rowData.size() == 0) {
                b = false;
            }
            else {
                this.thisRow = this.rowData.next();
                if (this.thisRow == null) {
                    b = false;
                }
                else {
                    this.clearWarnings();
                    b = true;
                }
            }
            this.setRowPositionValidity();
            return b;
        }
    }
    
    private int parseIntAsDouble(final int columnIndex, final String val) throws NumberFormatException, SQLException {
        if (val == null) {
            return 0;
        }
        final double valueAsDouble = Double.parseDouble(val);
        if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -2.147483648E9 || valueAsDouble > 2.147483647E9)) {
            this.throwRangeException(String.valueOf(valueAsDouble), columnIndex, 4);
        }
        return (int)valueAsDouble;
    }
    
    private int getIntWithOverflowCheck(final int columnIndex) throws SQLException {
        final int intValue = this.thisRow.getInt(columnIndex);
        this.checkForIntegerTruncation(columnIndex, null, intValue);
        return intValue;
    }
    
    private void checkForIntegerTruncation(final int columnIndex, final byte[] valueAsBytes, final int intValue) throws SQLException {
        if (this.jdbcCompliantTruncationForReads && (intValue == Integer.MIN_VALUE || intValue == Integer.MAX_VALUE)) {
            String valueAsString = null;
            if (valueAsBytes == null) {
                valueAsString = this.thisRow.getString(columnIndex, this.fields[columnIndex].getEncoding(), this.connection);
            }
            final long valueAsLong = Long.parseLong((valueAsString == null) ? StringUtils.toString(valueAsBytes) : valueAsString);
            if (valueAsLong < -2147483648L || valueAsLong > 2147483647L) {
                this.throwRangeException((valueAsString == null) ? StringUtils.toString(valueAsBytes) : valueAsString, columnIndex + 1, 4);
            }
        }
    }
    
    private long parseLongAsDouble(final int columnIndexZeroBased, final String val) throws NumberFormatException, SQLException {
        if (val == null) {
            return 0L;
        }
        final double valueAsDouble = Double.parseDouble(val);
        if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -9.223372036854776E18 || valueAsDouble > 9.223372036854776E18)) {
            this.throwRangeException(val, columnIndexZeroBased + 1, -5);
        }
        return (long)valueAsDouble;
    }
    
    private long getLongWithOverflowCheck(final int columnIndexZeroBased, final boolean doOverflowCheck) throws SQLException {
        final long longValue = this.thisRow.getLong(columnIndexZeroBased);
        if (doOverflowCheck) {
            this.checkForLongTruncation(columnIndexZeroBased, null, longValue);
        }
        return longValue;
    }
    
    private long parseLongWithOverflowCheck(final int columnIndexZeroBased, final byte[] valueAsBytes, String valueAsString, final boolean doCheck) throws NumberFormatException, SQLException {
        long longValue = 0L;
        if (valueAsBytes == null && valueAsString == null) {
            return 0L;
        }
        if (valueAsBytes != null) {
            longValue = StringUtils.getLong(valueAsBytes);
        }
        else {
            valueAsString = valueAsString.trim();
            longValue = Long.parseLong(valueAsString);
        }
        if (doCheck && this.jdbcCompliantTruncationForReads) {
            this.checkForLongTruncation(columnIndexZeroBased, valueAsBytes, longValue);
        }
        return longValue;
    }
    
    private void checkForLongTruncation(final int columnIndexZeroBased, final byte[] valueAsBytes, final long longValue) throws SQLException {
        if (longValue == Long.MIN_VALUE || longValue == Long.MAX_VALUE) {
            String valueAsString = null;
            if (valueAsBytes == null) {
                valueAsString = this.thisRow.getString(columnIndexZeroBased, this.fields[columnIndexZeroBased].getEncoding(), this.connection);
            }
            final double valueAsDouble = Double.parseDouble((valueAsString == null) ? StringUtils.toString(valueAsBytes) : valueAsString);
            if (valueAsDouble < -9.223372036854776E18 || valueAsDouble > 9.223372036854776E18) {
                this.throwRangeException((valueAsString == null) ? StringUtils.toString(valueAsBytes) : valueAsString, columnIndexZeroBased + 1, -5);
            }
        }
    }
    
    private short parseShortAsDouble(final int columnIndex, final String val) throws NumberFormatException, SQLException {
        if (val == null) {
            return 0;
        }
        final double valueAsDouble = Double.parseDouble(val);
        if (this.jdbcCompliantTruncationForReads && (valueAsDouble < -32768.0 || valueAsDouble > 32767.0)) {
            this.throwRangeException(String.valueOf(valueAsDouble), columnIndex, 5);
        }
        return (short)valueAsDouble;
    }
    
    private short parseShortWithOverflowCheck(final int columnIndex, final byte[] valueAsBytes, String valueAsString) throws NumberFormatException, SQLException {
        short shortValue = 0;
        if (valueAsBytes == null && valueAsString == null) {
            return 0;
        }
        if (valueAsBytes != null) {
            shortValue = StringUtils.getShort(valueAsBytes);
        }
        else {
            valueAsString = valueAsString.trim();
            shortValue = Short.parseShort(valueAsString);
        }
        if (this.jdbcCompliantTruncationForReads && (shortValue == -32768 || shortValue == 32767)) {
            final long valueAsLong = Long.parseLong((valueAsString == null) ? StringUtils.toString(valueAsBytes) : valueAsString);
            if (valueAsLong < -32768L || valueAsLong > 32767L) {
                this.throwRangeException((valueAsString == null) ? StringUtils.toString(valueAsBytes) : valueAsString, columnIndex, 5);
            }
        }
        return shortValue;
    }
    
    public boolean prev() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            int rowIndex = this.rowData.getCurrentRowNumber();
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            boolean b = true;
            if (rowIndex - 1 >= 0) {
                --rowIndex;
                this.rowData.setCurrentRow(rowIndex);
                this.thisRow = this.rowData.getAt(rowIndex);
                b = true;
            }
            else if (rowIndex - 1 == -1) {
                --rowIndex;
                this.rowData.setCurrentRow(rowIndex);
                this.thisRow = null;
                b = false;
            }
            else {
                b = false;
            }
            this.setRowPositionValidity();
            return b;
        }
    }
    
    @Override
    public boolean previous() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.onInsertRow) {
                this.onInsertRow = false;
            }
            if (this.doingUpdates) {
                this.doingUpdates = false;
            }
            return this.prev();
        }
    }
    
    @Override
    public void realClose(final boolean calledExplicitly) throws SQLException {
        final MySQLConnection locallyScopedConn = this.connection;
        if (locallyScopedConn == null) {
            return;
        }
        synchronized (locallyScopedConn.getConnectionMutex()) {
            if (this.isClosed) {
                return;
            }
            try {
                if (this.useUsageAdvisor) {
                    if (!calledExplicitly) {
                        this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", (this.owningStatement == null) ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, (this.owningStatement == null) ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, Messages.getString("ResultSet.ResultSet_implicitly_closed_by_driver")));
                    }
                    if (this.rowData instanceof RowDataStatic) {
                        if (this.rowData.size() > this.connection.getResultSetSizeThreshold()) {
                            this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", (this.owningStatement == null) ? Messages.getString("ResultSet.N/A_159") : this.owningStatement.currentCatalog, this.connectionId, (this.owningStatement == null) ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, Messages.getString("ResultSet.Too_Large_Result_Set", new Object[] { this.rowData.size(), this.connection.getResultSetSizeThreshold() })));
                        }
                        if (!this.isLast() && !this.isAfterLast() && this.rowData.size() != 0) {
                            this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", (this.owningStatement == null) ? Messages.getString("ResultSet.N/A_159") : this.owningStatement.currentCatalog, this.connectionId, (this.owningStatement == null) ? -1 : this.owningStatement.getId(), this.resultId, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, Messages.getString("ResultSet.Possible_incomplete_traversal_of_result_set", new Object[] { this.getRow(), this.rowData.size() })));
                        }
                    }
                    if (this.columnUsed.length > 0 && !this.rowData.wasEmpty()) {
                        final StringBuilder buf = new StringBuilder(Messages.getString("ResultSet.The_following_columns_were_never_referenced"));
                        boolean issueWarn = false;
                        for (int i = 0; i < this.columnUsed.length; ++i) {
                            if (!this.columnUsed[i]) {
                                if (!issueWarn) {
                                    issueWarn = true;
                                }
                                else {
                                    buf.append(", ");
                                }
                                buf.append(this.fields[i].getFullName());
                            }
                        }
                        if (issueWarn) {
                            this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", (this.owningStatement == null) ? "N/A" : this.owningStatement.currentCatalog, this.connectionId, (this.owningStatement == null) ? -1 : this.owningStatement.getId(), 0, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, buf.toString()));
                        }
                    }
                }
            }
            finally {
                if (this.owningStatement != null && calledExplicitly) {
                    this.owningStatement.removeOpenResultSet(this);
                }
                SQLException exceptionDuringClose = null;
                if (this.rowData != null) {
                    try {
                        this.rowData.close();
                    }
                    catch (SQLException sqlEx) {
                        exceptionDuringClose = sqlEx;
                    }
                }
                if (this.statementUsedForFetchingRows != null) {
                    try {
                        this.statementUsedForFetchingRows.realClose(true, false);
                    }
                    catch (SQLException sqlEx) {
                        if (exceptionDuringClose != null) {
                            exceptionDuringClose.setNextException(sqlEx);
                        }
                        else {
                            exceptionDuringClose = sqlEx;
                        }
                    }
                }
                this.rowData = null;
                this.fields = null;
                this.columnLabelToIndex = null;
                this.fullColumnNameToIndex = null;
                this.columnToIndexCache = null;
                this.eventSink = null;
                this.warningChain = null;
                if (!this.retainOwningStatement) {
                    this.owningStatement = null;
                }
                this.catalog = null;
                this.serverInfo = null;
                this.thisRow = null;
                this.fastDefaultCal = null;
                this.fastClientCal = null;
                this.connection = null;
                this.isClosed = true;
                if (exceptionDuringClose != null) {
                    throw exceptionDuringClose;
                }
            }
        }
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return this.isClosed;
    }
    
    @Override
    public void updateNString(final int columnIndex, final String nString) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNString(final String columnLabel, final String nString) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNClob(final int columnIndex, final NClob nClob) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNClob(final String columnLabel, final NClob nClob) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public NClob getNClob(final int columnIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public NClob getNClob(final String columnLabel) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateSQLXML(final String columnLabel, final SQLXML xmlObject) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public String getNString(final int columnIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public String getNString(final String columnLabel) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Reader getNCharacterStream(final int columnIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Reader getNCharacterStream(final String columnLabel) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateClob(final int columnIndex, final Reader reader) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateClob(final String columnLabel, final Reader reader) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNClob(final int columnIndex, final Reader reader) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNClob(final String columnLabel, final Reader reader) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public boolean reallyResult() {
        return this.rowData != null || this.reallyResult;
    }
    
    @Override
    public void refreshRow() throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public boolean relative(final int rows) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.rowData.size() == 0) {
                this.setRowPositionValidity();
                return false;
            }
            if (this.thisRow != null) {
                this.thisRow.closeOpenStreams();
            }
            this.rowData.moveRowRelative(rows);
            this.thisRow = this.rowData.getAt(this.rowData.getCurrentRowNumber());
            this.setRowPositionValidity();
            return !this.rowData.isAfterLast() && !this.rowData.isBeforeFirst();
        }
    }
    
    @Override
    public boolean rowDeleted() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public boolean rowInserted() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public boolean rowUpdated() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    protected void setBinaryEncoded() {
        this.isBinaryEncoded = true;
    }
    
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (direction != 1000 && direction != 1001 && direction != 1002) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Illegal_value_for_fetch_direction_64"), "S1009", this.getExceptionInterceptor());
            }
            this.fetchDirection = direction;
        }
    }
    
    @Override
    public void setFetchSize(final int rows) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (rows < 0) {
                throw SQLError.createSQLException(Messages.getString("ResultSet.Value_must_be_between_0_and_getMaxRows()_66"), "S1009", this.getExceptionInterceptor());
            }
            this.fetchSize = rows;
        }
    }
    
    @Override
    public void setFirstCharOfQuery(final char c) {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                this.firstCharOfQuery = c;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected synchronized void setNextResultSet(final ResultSetInternalMethods nextResultSet) {
        this.nextResultSet = nextResultSet;
    }
    
    @Override
    public void setOwningStatement(final StatementImpl owningStatement) {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                this.owningStatement = owningStatement;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected synchronized void setResultSetConcurrency(final int concurrencyFlag) {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                this.resultSetConcurrency = concurrencyFlag;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected synchronized void setResultSetType(final int typeFlag) {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                this.resultSetType = typeFlag;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void setServerInfo(final String info) {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                this.serverInfo = info;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public synchronized void setStatementUsedForFetchingRows(final PreparedStatement stmt) {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                this.statementUsedForFetchingRows = stmt;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public synchronized void setWrapperStatement(final Statement wrapperStatement) {
        try {
            synchronized (this.checkClosed().getConnectionMutex()) {
                this.wrapperStatement = wrapperStatement;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void throwRangeException(final String valueAsString, final int columnIndex, final int jdbcType) throws SQLException {
        String datatype = null;
        switch (jdbcType) {
            case -6: {
                datatype = "TINYINT";
                break;
            }
            case 5: {
                datatype = "SMALLINT";
                break;
            }
            case 4: {
                datatype = "INTEGER";
                break;
            }
            case -5: {
                datatype = "BIGINT";
                break;
            }
            case 7: {
                datatype = "REAL";
                break;
            }
            case 6: {
                datatype = "FLOAT";
                break;
            }
            case 8: {
                datatype = "DOUBLE";
                break;
            }
            case 3: {
                datatype = "DECIMAL";
                break;
            }
            default: {
                datatype = " (JDBC type '" + jdbcType + "')";
                break;
            }
        }
        throw SQLError.createSQLException("'" + valueAsString + "' in column '" + columnIndex + "' is outside valid range for the datatype " + datatype + ".", "22003", this.getExceptionInterceptor());
    }
    
    @Override
    public String toString() {
        if (this.reallyResult) {
            return super.toString();
        }
        return "Result set representing update count of " + this.updateCount;
    }
    
    @Override
    public void updateArray(final int arg0, final Array arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateArray(final String arg0, final Array arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public RowId getRowId(final int columnIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public RowId getRowId(final String columnLabel) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateRowId(final int columnIndex, final RowId x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateRowId(final String columnLabel, final RowId x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateAsciiStream(final String columnName, final InputStream x, final int length) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnName), x, length);
    }
    
    @Override
    public void updateBigDecimal(final int columnIndex, final BigDecimal x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateBigDecimal(final String columnName, final BigDecimal x) throws SQLException {
        this.updateBigDecimal(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateBinaryStream(final String columnName, final InputStream x, final int length) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnName), x, length);
    }
    
    @Override
    public void updateBlob(final int arg0, final java.sql.Blob arg1) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateBlob(final String arg0, final java.sql.Blob arg1) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateBoolean(final int columnIndex, final boolean x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateBoolean(final String columnName, final boolean x) throws SQLException {
        this.updateBoolean(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateByte(final int columnIndex, final byte x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateByte(final String columnName, final byte x) throws SQLException {
        this.updateByte(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateBytes(final int columnIndex, final byte[] x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateBytes(final String columnName, final byte[] x) throws SQLException {
        this.updateBytes(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateCharacterStream(final String columnName, final Reader reader, final int length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnName), reader, length);
    }
    
    @Override
    public void updateClob(final int arg0, final java.sql.Clob arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateClob(final String columnName, final java.sql.Clob clob) throws SQLException {
        this.updateClob(this.findColumn(columnName), clob);
    }
    
    @Override
    public void updateDate(final int columnIndex, final Date x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateDate(final String columnName, final Date x) throws SQLException {
        this.updateDate(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateDouble(final int columnIndex, final double x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateDouble(final String columnName, final double x) throws SQLException {
        this.updateDouble(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateFloat(final int columnIndex, final float x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateFloat(final String columnName, final float x) throws SQLException {
        this.updateFloat(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateInt(final int columnIndex, final int x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateInt(final String columnName, final int x) throws SQLException {
        this.updateInt(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateLong(final int columnIndex, final long x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateLong(final String columnName, final long x) throws SQLException {
        this.updateLong(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateNull(final int columnIndex) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateNull(final String columnName) throws SQLException {
        this.updateNull(this.findColumn(columnName));
    }
    
    @Override
    public void updateObject(final int columnIndex, final Object x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateObject(final int columnIndex, final Object x, final int scale) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateObject(final String columnName, final Object x) throws SQLException {
        this.updateObject(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateObject(final String columnName, final Object x, final int scale) throws SQLException {
        this.updateObject(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateRef(final int arg0, final Ref arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateRef(final String arg0, final Ref arg1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateRow() throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateShort(final int columnIndex, final short x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateShort(final String columnName, final short x) throws SQLException {
        this.updateShort(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateString(final int columnIndex, final String x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateString(final String columnName, final String x) throws SQLException {
        this.updateString(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateTime(final int columnIndex, final Time x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateTime(final String columnName, final Time x) throws SQLException {
        this.updateTime(this.findColumn(columnName), x);
    }
    
    @Override
    public void updateTimestamp(final int columnIndex, final Timestamp x) throws SQLException {
        throw new NotUpdatable();
    }
    
    @Override
    public void updateTimestamp(final String columnName, final Timestamp x) throws SQLException {
        this.updateTimestamp(this.findColumn(columnName), x);
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return this.wasNullFlag;
    }
    
    protected Calendar getGmtCalendar() {
        if (this.gmtCalendar == null) {
            this.gmtCalendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        }
        return this.gmtCalendar;
    }
    
    protected ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }
    
    private TimeZone getCurrentTimeZone(final Calendar cal) {
        return (cal == null) ? Calendar.getInstance().getTimeZone() : cal.getTimeZone();
    }
    
    static {
        Label_0206: {
            if (Util.isJdbc4()) {
                try {
                    String jdbc4ClassName = Util.isJdbc42() ? "com.alipay.oceanbase.jdbc.JDBC42ResultSet" : "com.alipay.oceanbase.jdbc.JDBC4ResultSet";
                    JDBC_4_RS_4_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(Long.TYPE, Long.TYPE, MySQLConnection.class, StatementImpl.class);
                    JDBC_4_RS_5_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(String.class, Field[].class, RowData.class, MySQLConnection.class, StatementImpl.class);
                    jdbc4ClassName = (Util.isJdbc42() ? "com.alipay.oceanbase.jdbc.JDBC42UpdatableResultSet" : "com.alipay.oceanbase.jdbc.JDBC4UpdatableResultSet");
                    JDBC_4_UPD_RS_5_ARG_CTOR = Class.forName(jdbc4ClassName).getConstructor(String.class, Field[].class, RowData.class, MySQLConnection.class, StatementImpl.class);
                    break Label_0206;
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
            JDBC_4_RS_4_ARG_CTOR = null;
            JDBC_4_RS_5_ARG_CTOR = null;
            JDBC_4_UPD_RS_5_ARG_CTOR = null;
        }
        MIN_DIFF_PREC = Float.parseFloat(Float.toString(Float.MIN_VALUE)) - Double.parseDouble(Float.toString(Float.MIN_VALUE));
        MAX_DIFF_PREC = Float.parseFloat(Float.toString(Float.MAX_VALUE)) - Double.parseDouble(Float.toString(Float.MAX_VALUE));
        ResultSetImpl.resultCounter = 1;
        EMPTY_SPACE = new char[255];
        for (int i = 0; i < ResultSetImpl.EMPTY_SPACE.length; ++i) {
            ResultSetImpl.EMPTY_SPACE[i] = ' ';
        }
    }
}
