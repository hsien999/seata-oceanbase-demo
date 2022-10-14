// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.Hashtable;
import java.sql.BatchUpdateException;
import java.net.BindException;
import com.alipay.oceanbase.jdbc.exceptions.MySQLQueryInterruptedException;
import com.alipay.oceanbase.jdbc.exceptions.MySQLTransactionRollbackException;
import com.alipay.oceanbase.jdbc.exceptions.MySQLSyntaxErrorException;
import com.alipay.oceanbase.jdbc.exceptions.MySQLIntegrityConstraintViolationException;
import com.alipay.oceanbase.jdbc.exceptions.MySQLDataException;
import com.alipay.oceanbase.jdbc.exceptions.MySQLNonTransientConnectionException;
import com.alipay.oceanbase.jdbc.exceptions.MySQLTransientConnectionException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.HashMap;
import java.util.TreeMap;
import java.sql.DataTruncation;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.lang.reflect.Constructor;
import java.util.Map;

public class SQLError
{
    static final int ER_WARNING_NOT_COMPLETE_ROLLBACK = 1196;
    private static Map<Integer, String> mysqlToSql99State;
    private static Map<Integer, String> mysqlToSqlState;
    public static final String SQL_STATE_WARNING = "01000";
    public static final String SQL_STATE_DISCONNECT_ERROR = "01002";
    public static final String SQL_STATE_DATE_TRUNCATED = "01004";
    public static final String SQL_STATE_PRIVILEGE_NOT_REVOKED = "01006";
    public static final String SQL_STATE_NO_DATA = "02000";
    public static final String SQL_STATE_WRONG_NO_OF_PARAMETERS = "07001";
    public static final String SQL_STATE_UNABLE_TO_CONNECT_TO_DATASOURCE = "08001";
    public static final String SQL_STATE_CONNECTION_IN_USE = "08002";
    public static final String SQL_STATE_CONNECTION_NOT_OPEN = "08003";
    public static final String SQL_STATE_CONNECTION_REJECTED = "08004";
    public static final String SQL_STATE_CONNECTION_FAILURE = "08006";
    public static final String SQL_STATE_TRANSACTION_RESOLUTION_UNKNOWN = "08007";
    public static final String SQL_STATE_COMMUNICATION_LINK_FAILURE = "08S01";
    public static final String SQL_STATE_FEATURE_NOT_SUPPORTED = "0A000";
    public static final String SQL_STATE_CARDINALITY_VIOLATION = "21000";
    public static final String SQL_STATE_INSERT_VALUE_LIST_NO_MATCH_COL_LIST = "21S01";
    public static final String SQL_STATE_STRING_DATA_RIGHT_TRUNCATION = "22001";
    public static final String SQL_STATE_NUMERIC_VALUE_OUT_OF_RANGE = "22003";
    public static final String SQL_STATE_INVALID_DATETIME_FORMAT = "22007";
    public static final String SQL_STATE_DATETIME_FIELD_OVERFLOW = "22008";
    public static final String SQL_STATE_DIVISION_BY_ZERO = "22012";
    public static final String SQL_STATE_INVALID_CHARACTER_VALUE_FOR_CAST = "22018";
    public static final String SQL_STATE_INTEGRITY_CONSTRAINT_VIOLATION = "23000";
    public static final String SQL_STATE_INVALID_CURSOR_STATE = "24000";
    public static final String SQL_STATE_INVALID_TRANSACTION_STATE = "25000";
    public static final String SQL_STATE_INVALID_AUTH_SPEC = "28000";
    public static final String SQL_STATE_INVALID_TRANSACTION_TERMINATION = "2D000";
    public static final String SQL_STATE_INVALID_CONDITION_NUMBER = "35000";
    public static final String SQL_STATE_INVALID_CATALOG_NAME = "3D000";
    public static final String SQL_STATE_ROLLBACK_SERIALIZATION_FAILURE = "40001";
    public static final String SQL_STATE_SYNTAX_ERROR = "42000";
    public static final String SQL_STATE_ER_TABLE_EXISTS_ERROR = "42S01";
    public static final String SQL_STATE_BASE_TABLE_OR_VIEW_NOT_FOUND = "42S02";
    public static final String SQL_STATE_ER_NO_SUCH_INDEX = "42S12";
    public static final String SQL_STATE_ER_DUP_FIELDNAME = "42S21";
    public static final String SQL_STATE_ER_BAD_FIELD_ERROR = "42S22";
    public static final String SQL_STATE_INVALID_CONNECTION_ATTRIBUTE = "01S00";
    public static final String SQL_STATE_ERROR_IN_ROW = "01S01";
    public static final String SQL_STATE_NO_ROWS_UPDATED_OR_DELETED = "01S03";
    public static final String SQL_STATE_MORE_THAN_ONE_ROW_UPDATED_OR_DELETED = "01S04";
    public static final String SQL_STATE_RESIGNAL_WHEN_HANDLER_NOT_ACTIVE = "0K000";
    public static final String SQL_STATE_STACKED_DIAGNOSTICS_ACCESSED_WITHOUT_ACTIVE_HANDLER = "0Z002";
    public static final String SQL_STATE_CASE_NOT_FOUND_FOR_CASE_STATEMENT = "20000";
    public static final String SQL_STATE_NULL_VALUE_NOT_ALLOWED = "22004";
    public static final String SQL_STATE_INVALID_LOGARITHM_ARGUMENT = "2201E";
    public static final String SQL_STATE_ACTIVE_SQL_TRANSACTION = "25001";
    public static final String SQL_STATE_READ_ONLY_SQL_TRANSACTION = "25006";
    public static final String SQL_STATE_SRE_PROHIBITED_SQL_STATEMENT_ATTEMPTED = "2F003";
    public static final String SQL_STATE_SRE_FUNCTION_EXECUTED_NO_RETURN_STATEMENT = "2F005";
    public static final String SQL_STATE_ER_QUERY_INTERRUPTED = "70100";
    public static final String SQL_STATE_BASE_TABLE_OR_VIEW_ALREADY_EXISTS = "S0001";
    public static final String SQL_STATE_BASE_TABLE_NOT_FOUND = "S0002";
    public static final String SQL_STATE_INDEX_ALREADY_EXISTS = "S0011";
    public static final String SQL_STATE_INDEX_NOT_FOUND = "S0012";
    public static final String SQL_STATE_COLUMN_ALREADY_EXISTS = "S0021";
    public static final String SQL_STATE_COLUMN_NOT_FOUND = "S0022";
    public static final String SQL_STATE_NO_DEFAULT_FOR_COLUMN = "S0023";
    public static final String SQL_STATE_GENERAL_ERROR = "S1000";
    public static final String SQL_STATE_MEMORY_ALLOCATION_FAILURE = "S1001";
    public static final String SQL_STATE_INVALID_COLUMN_NUMBER = "S1002";
    public static final String SQL_STATE_ILLEGAL_ARGUMENT = "S1009";
    public static final String SQL_STATE_DRIVER_NOT_CAPABLE = "S1C00";
    public static final String SQL_STATE_TIMEOUT_EXPIRED = "S1T00";
    public static final String SQL_STATE_CLI_SPECIFIC_CONDITION = "HY000";
    public static final String SQL_STATE_MEMORY_ALLOCATION_ERROR = "HY001";
    public static final String SQL_STATE_XA_RBROLLBACK = "XA100";
    public static final String SQL_STATE_XA_RBDEADLOCK = "XA102";
    public static final String SQL_STATE_XA_RBTIMEOUT = "XA106";
    public static final String SQL_STATE_XA_RMERR = "XAE03";
    public static final String SQL_STATE_XAER_NOTA = "XAE04";
    public static final String SQL_STATE_XAER_INVAL = "XAE05";
    public static final String SQL_STATE_XAER_RMFAIL = "XAE07";
    public static final String SQL_STATE_XAER_DUPID = "XAE08";
    public static final String SQL_STATE_XAER_OUTSIDE = "XAE09";
    private static Map<String, String> sqlStateMessages;
    private static final long DEFAULT_WAIT_TIMEOUT_SECONDS = 28800L;
    private static final int DUE_TO_TIMEOUT_FALSE = 0;
    private static final int DUE_TO_TIMEOUT_MAYBE = 2;
    private static final int DUE_TO_TIMEOUT_TRUE = 1;
    private static final Constructor<?> JDBC_4_COMMUNICATIONS_EXCEPTION_CTOR;
    
    static SQLWarning convertShowWarningsToSQLWarnings(final Connection connection) throws SQLException {
        return convertShowWarningsToSQLWarnings(connection, 0, false);
    }
    
    static SQLWarning convertShowWarningsToSQLWarnings(final Connection connection, final int warningCountIfKnown, final boolean forTruncationOnly) throws SQLException {
        Statement stmt = null;
        ResultSet warnRs = null;
        SQLWarning currentWarning = null;
        try {
            if (warningCountIfKnown < 100) {
                stmt = connection.createStatement();
                if (stmt.getMaxRows() != 0) {
                    stmt.setMaxRows(0);
                }
            }
            else {
                stmt = connection.createStatement(1003, 1007);
                stmt.setFetchSize(Integer.MIN_VALUE);
            }
            warnRs = stmt.executeQuery("SHOW WARNINGS");
            while (warnRs.next()) {
                final int code = warnRs.getInt("Code");
                if (forTruncationOnly) {
                    if (code != 1265 && code != 1264) {
                        continue;
                    }
                    final DataTruncation newTruncation = new MysqlDataTruncation(warnRs.getString("Message"), 0, false, false, 0, 0, code);
                    if (currentWarning == null) {
                        currentWarning = newTruncation;
                    }
                    else {
                        currentWarning.setNextWarning(newTruncation);
                    }
                }
                else {
                    final String message = warnRs.getString("Message");
                    final SQLWarning newWarning = new SQLWarning(message, mysqlToSqlState(code, connection.getUseSqlStateCodes()), code);
                    if (currentWarning == null) {
                        currentWarning = newWarning;
                    }
                    else {
                        currentWarning.setNextWarning(newWarning);
                    }
                }
            }
            if (forTruncationOnly && currentWarning != null) {
                throw currentWarning;
            }
            return currentWarning;
        }
        finally {
            SQLException reThrow = null;
            if (warnRs != null) {
                try {
                    warnRs.close();
                }
                catch (SQLException sqlEx) {
                    reThrow = sqlEx;
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException sqlEx) {
                    reThrow = sqlEx;
                }
            }
            if (reThrow != null) {
                throw reThrow;
            }
        }
    }
    
    public static void dumpSqlStatesMappingsAsXml() throws Exception {
        final TreeMap<Integer, Integer> allErrorNumbers = new TreeMap<Integer, Integer>();
        final Map<Object, String> mysqlErrorNumbersToNames = new HashMap<Object, String>();
        for (final Integer errorNumber : SQLError.mysqlToSql99State.keySet()) {
            allErrorNumbers.put(errorNumber, errorNumber);
        }
        for (final Integer errorNumber : SQLError.mysqlToSqlState.keySet()) {
            allErrorNumbers.put(errorNumber, errorNumber);
        }
        final Field[] possibleFields = MysqlErrorNumbers.class.getDeclaredFields();
        for (int i = 0; i < possibleFields.length; ++i) {
            final String fieldName = possibleFields[i].getName();
            if (fieldName.startsWith("ER_")) {
                mysqlErrorNumbersToNames.put(possibleFields[i].get(null), fieldName);
            }
        }
        System.out.println("<ErrorMappings>");
        for (final Integer errorNumber2 : allErrorNumbers.keySet()) {
            final String sql92State = mysqlToSql99(errorNumber2);
            final String oldSqlState = mysqlToXOpen(errorNumber2);
            System.out.println("   <ErrorMapping mysqlErrorNumber=\"" + errorNumber2 + "\" mysqlErrorName=\"" + mysqlErrorNumbersToNames.get(errorNumber2) + "\" legacySqlState=\"" + ((oldSqlState == null) ? "" : oldSqlState) + "\" sql92SqlState=\"" + ((sql92State == null) ? "" : sql92State) + "\"/>");
        }
        System.out.println("</ErrorMappings>");
    }
    
    static String get(final String stateCode) {
        return SQLError.sqlStateMessages.get(stateCode);
    }
    
    private static String mysqlToSql99(final int errno) {
        final Integer err = errno;
        if (SQLError.mysqlToSql99State.containsKey(err)) {
            return SQLError.mysqlToSql99State.get(err);
        }
        return "HY000";
    }
    
    static String mysqlToSqlState(final int errno, final boolean useSql92States) {
        if (useSql92States) {
            return mysqlToSql99(errno);
        }
        return mysqlToXOpen(errno);
    }
    
    private static String mysqlToXOpen(final int errno) {
        final Integer err = errno;
        if (SQLError.mysqlToSqlState.containsKey(err)) {
            return SQLError.mysqlToSqlState.get(err);
        }
        return "S1000";
    }
    
    public static SQLException createSQLException(final String message, final String sqlState, final ExceptionInterceptor interceptor) {
        return createSQLException(message, sqlState, 0, interceptor);
    }
    
    public static SQLException createSQLException(final String message, final ExceptionInterceptor interceptor) {
        return createSQLException(message, interceptor, null);
    }
    
    public static SQLException createSQLException(final String message, final ExceptionInterceptor interceptor, final Connection conn) {
        final SQLException sqlEx = new SQLException(message);
        return runThroughExceptionInterceptor(interceptor, sqlEx, conn);
    }
    
    public static SQLException createSQLException(final String message, final String sqlState, final Throwable cause, final ExceptionInterceptor interceptor) {
        return createSQLException(message, sqlState, cause, interceptor, null);
    }
    
    public static SQLException createSQLException(final String message, final String sqlState, final Throwable cause, final ExceptionInterceptor interceptor, final Connection conn) {
        final SQLException sqlEx = createSQLException(message, sqlState, null);
        if (sqlEx.getCause() == null) {
            sqlEx.initCause(cause);
        }
        return runThroughExceptionInterceptor(interceptor, sqlEx, conn);
    }
    
    public static SQLException createSQLException(final String message, final String sqlState, final int vendorErrorCode, final ExceptionInterceptor interceptor) {
        return createSQLException(message, sqlState, vendorErrorCode, false, interceptor);
    }
    
    public static SQLException createSQLException(final String message, final String sqlState, final int vendorErrorCode, final boolean isTransient, final ExceptionInterceptor interceptor) {
        return createSQLException(message, sqlState, vendorErrorCode, isTransient, interceptor, null);
    }
    
    public static SQLException createSQLException(String message, final String sqlState, final int vendorErrorCode, final boolean isTransient, final ExceptionInterceptor interceptor, final Connection conn) {
        try {
            SQLException sqlEx = null;
            if (conn != null && conn.getUseFormatExceptionMessage() && conn instanceof MySQLConnection && ((MySQLConnection)conn).getIO().isOracleMode()) {
                message = "ORA-" + vendorErrorCode + ": " + message;
            }
            if (sqlState != null) {
                if (sqlState.startsWith("08")) {
                    if (isTransient) {
                        if (!Util.isJdbc4()) {
                            sqlEx = new MySQLTransientConnectionException(message, sqlState, vendorErrorCode);
                        }
                        else {
                            sqlEx = (SQLException)Util.getInstance("com.alipay.oceanbase.jdbc.exceptions.jdbc4.MySQLTransientConnectionException", new Class[] { String.class, String.class, Integer.TYPE }, new Object[] { message, sqlState, vendorErrorCode }, interceptor);
                        }
                    }
                    else if (!Util.isJdbc4()) {
                        sqlEx = new MySQLNonTransientConnectionException(message, sqlState, vendorErrorCode);
                    }
                    else {
                        sqlEx = (SQLException)Util.getInstance("com.alipay.oceanbase.jdbc.exceptions.jdbc4.MySQLNonTransientConnectionException", new Class[] { String.class, String.class, Integer.TYPE }, new Object[] { message, sqlState, vendorErrorCode }, interceptor);
                    }
                }
                else if (sqlState.startsWith("22")) {
                    if (!Util.isJdbc4()) {
                        sqlEx = new MySQLDataException(message, sqlState, vendorErrorCode);
                    }
                    else {
                        sqlEx = (SQLException)Util.getInstance("com.alipay.oceanbase.jdbc.exceptions.jdbc4.MySQLDataException", new Class[] { String.class, String.class, Integer.TYPE }, new Object[] { message, sqlState, vendorErrorCode }, interceptor);
                    }
                }
                else if (sqlState.startsWith("23")) {
                    if (!Util.isJdbc4()) {
                        sqlEx = new MySQLIntegrityConstraintViolationException(message, sqlState, vendorErrorCode);
                    }
                    else {
                        sqlEx = (SQLException)Util.getInstance("com.alipay.oceanbase.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException", new Class[] { String.class, String.class, Integer.TYPE }, new Object[] { message, sqlState, vendorErrorCode }, interceptor);
                    }
                }
                else if (sqlState.startsWith("42")) {
                    if (!Util.isJdbc4()) {
                        sqlEx = new MySQLSyntaxErrorException(message, sqlState, vendorErrorCode);
                    }
                    else {
                        sqlEx = (SQLException)Util.getInstance("com.alipay.oceanbase.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException", new Class[] { String.class, String.class, Integer.TYPE }, new Object[] { message, sqlState, vendorErrorCode }, interceptor);
                    }
                }
                else if (sqlState.startsWith("40")) {
                    if (!Util.isJdbc4()) {
                        sqlEx = new MySQLTransactionRollbackException(message, sqlState, vendorErrorCode);
                    }
                    else {
                        sqlEx = (SQLException)Util.getInstance("com.alipay.oceanbase.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException", new Class[] { String.class, String.class, Integer.TYPE }, new Object[] { message, sqlState, vendorErrorCode }, interceptor);
                    }
                }
                else if (sqlState.startsWith("70100")) {
                    if (!Util.isJdbc4()) {
                        sqlEx = new MySQLQueryInterruptedException(message, sqlState, vendorErrorCode);
                    }
                    else {
                        sqlEx = (SQLException)Util.getInstance("com.alipay.oceanbase.jdbc.exceptions.jdbc4.MySQLQueryInterruptedException", new Class[] { String.class, String.class, Integer.TYPE }, new Object[] { message, sqlState, vendorErrorCode }, interceptor);
                    }
                }
                else {
                    sqlEx = new SQLException(message, sqlState, vendorErrorCode);
                }
            }
            else {
                sqlEx = new SQLException(message, sqlState, vendorErrorCode);
            }
            return runThroughExceptionInterceptor(interceptor, sqlEx, conn);
        }
        catch (SQLException sqlEx) {
            final SQLException unexpectedEx = new SQLException("Unable to create correct SQLException class instance, error class/codes may be incorrect. Reason: " + Util.stackTraceToString(sqlEx), "S1000");
            return runThroughExceptionInterceptor(interceptor, unexpectedEx, conn);
        }
    }
    
    public static SQLException createCommunicationsException(final MySQLConnection conn, final long lastPacketSentTimeMs, final long lastPacketReceivedTimeMs, final Exception underlyingException, final ExceptionInterceptor interceptor) {
        SQLException exToReturn = null;
        if (!Util.isJdbc4()) {
            exToReturn = new CommunicationsException(conn, lastPacketSentTimeMs, lastPacketReceivedTimeMs, underlyingException);
        }
        else {
            try {
                exToReturn = (SQLException)Util.handleNewInstance(SQLError.JDBC_4_COMMUNICATIONS_EXCEPTION_CTOR, new Object[] { conn, lastPacketSentTimeMs, lastPacketReceivedTimeMs, underlyingException }, interceptor);
            }
            catch (SQLException sqlEx) {
                return sqlEx;
            }
        }
        return runThroughExceptionInterceptor(interceptor, exToReturn, conn);
    }
    
    public static String createLinkFailureMessageBasedOnHeuristics(final MySQLConnection conn, long lastPacketSentTimeMs, final long lastPacketReceivedTimeMs, final Exception underlyingException) {
        long serverTimeoutSeconds = 0L;
        boolean isInteractiveClient = false;
        if (conn != null) {
            isInteractiveClient = conn.getInteractiveClient();
            String serverTimeoutSecondsStr = null;
            if (isInteractiveClient) {
                serverTimeoutSecondsStr = conn.getServerVariable("interactive_timeout");
            }
            else {
                serverTimeoutSecondsStr = conn.getServerVariable("wait_timeout");
            }
            if (serverTimeoutSecondsStr != null) {
                try {
                    serverTimeoutSeconds = Long.parseLong(serverTimeoutSecondsStr);
                }
                catch (NumberFormatException nfe) {
                    serverTimeoutSeconds = 0L;
                }
            }
        }
        final StringBuilder exceptionMessageBuf = new StringBuilder();
        final long nowMs = System.currentTimeMillis();
        if (lastPacketSentTimeMs == 0L) {
            lastPacketSentTimeMs = nowMs;
        }
        final long timeSinceLastPacketSentMs = nowMs - lastPacketSentTimeMs;
        final long timeSinceLastPacketSeconds = timeSinceLastPacketSentMs / 1000L;
        final long timeSinceLastPacketReceivedMs = nowMs - lastPacketReceivedTimeMs;
        int dueToTimeout = 0;
        StringBuilder timeoutMessageBuf = null;
        if (serverTimeoutSeconds != 0L) {
            if (timeSinceLastPacketSeconds > serverTimeoutSeconds) {
                dueToTimeout = 1;
                timeoutMessageBuf = new StringBuilder();
                timeoutMessageBuf.append(Messages.getString("CommunicationsException.2"));
                if (!isInteractiveClient) {
                    timeoutMessageBuf.append(Messages.getString("CommunicationsException.3"));
                }
                else {
                    timeoutMessageBuf.append(Messages.getString("CommunicationsException.4"));
                }
            }
        }
        else if (timeSinceLastPacketSeconds > 28800L) {
            dueToTimeout = 2;
            timeoutMessageBuf = new StringBuilder();
            timeoutMessageBuf.append(Messages.getString("CommunicationsException.5"));
            timeoutMessageBuf.append(Messages.getString("CommunicationsException.6"));
            timeoutMessageBuf.append(Messages.getString("CommunicationsException.7"));
            timeoutMessageBuf.append(Messages.getString("CommunicationsException.8"));
        }
        if (dueToTimeout == 1 || dueToTimeout == 2) {
            if (lastPacketReceivedTimeMs != 0L) {
                final Object[] timingInfo = { timeSinceLastPacketReceivedMs, timeSinceLastPacketSentMs };
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", timingInfo));
            }
            else {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[] { timeSinceLastPacketSentMs }));
            }
            if (timeoutMessageBuf != null) {
                exceptionMessageBuf.append((CharSequence)timeoutMessageBuf);
            }
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.11"));
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.12"));
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.13"));
        }
        else if (underlyingException instanceof BindException) {
            if (conn.getLocalSocketAddress() != null && !Util.interfaceExists(conn.getLocalSocketAddress())) {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.LocalSocketAddressNotAvailable"));
            }
            else {
                exceptionMessageBuf.append(Messages.getString("CommunicationsException.TooManyClientConnections"));
            }
        }
        if (exceptionMessageBuf.length() == 0) {
            exceptionMessageBuf.append(Messages.getString("CommunicationsException.20"));
            if (conn != null && conn.getMaintainTimeStats() && !conn.getParanoid()) {
                exceptionMessageBuf.append("\n\n");
                if (lastPacketReceivedTimeMs != 0L) {
                    final Object[] timingInfo = { timeSinceLastPacketReceivedMs, timeSinceLastPacketSentMs };
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfo", timingInfo));
                }
                else {
                    exceptionMessageBuf.append(Messages.getString("CommunicationsException.ServerPacketTimingInfoNoRecv", new Object[] { timeSinceLastPacketSentMs }));
                }
            }
        }
        return exceptionMessageBuf.toString();
    }
    
    private static SQLException runThroughExceptionInterceptor(final ExceptionInterceptor exInterceptor, final SQLException sqlEx, final Connection conn) {
        if (exInterceptor != null) {
            final SQLException interceptedEx = exInterceptor.interceptException(sqlEx, conn);
            if (interceptedEx != null) {
                return interceptedEx;
            }
        }
        return sqlEx;
    }
    
    public static SQLException createBatchUpdateException(final SQLException underlyingEx, final long[] updateCounts, final ExceptionInterceptor interceptor) throws SQLException {
        SQLException newEx;
        if (Util.isJdbc42()) {
            newEx = (SQLException)Util.getInstance("java.sql.BatchUpdateException", new Class[] { String.class, String.class, Integer.TYPE, long[].class, Throwable.class }, new Object[] { underlyingEx.getMessage(), underlyingEx.getSQLState(), underlyingEx.getErrorCode(), updateCounts, underlyingEx }, interceptor);
        }
        else {
            newEx = new BatchUpdateException(underlyingEx.getMessage(), underlyingEx.getSQLState(), underlyingEx.getErrorCode(), Util.truncateAndConvertToInt(updateCounts));
            newEx.initCause(underlyingEx);
        }
        return runThroughExceptionInterceptor(interceptor, newEx, null);
    }
    
    public static SQLException createSQLFeatureNotSupportedException() throws SQLException {
        SQLException newEx;
        if (Util.isJdbc4()) {
            newEx = (SQLException)Util.getInstance("java.sql.SQLFeatureNotSupportedException", null, null, null);
        }
        else {
            newEx = new NotImplemented();
        }
        return newEx;
    }
    
    public static SQLException createSQLFeatureNotSupportedException(final String message, final String sqlState, final ExceptionInterceptor interceptor) throws SQLException {
        SQLException newEx;
        if (Util.isJdbc4()) {
            newEx = (SQLException)Util.getInstance("java.sql.SQLFeatureNotSupportedException", new Class[] { String.class, String.class }, new Object[] { message, sqlState }, interceptor);
        }
        else {
            newEx = new NotImplemented();
        }
        return runThroughExceptionInterceptor(interceptor, newEx, null);
    }
    
    static {
        Label_0083: {
            if (Util.isJdbc4()) {
                try {
                    JDBC_4_COMMUNICATIONS_EXCEPTION_CTOR = Class.forName("com.alipay.oceanbase.jdbc.exceptions.jdbc4.CommunicationsException").getConstructor(MySQLConnection.class, Long.TYPE, Long.TYPE, Exception.class);
                    break Label_0083;
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
            JDBC_4_COMMUNICATIONS_EXCEPTION_CTOR = null;
        }
        (SQLError.sqlStateMessages = new HashMap<String, String>()).put("01002", Messages.getString("SQLError.35"));
        SQLError.sqlStateMessages.put("01004", Messages.getString("SQLError.36"));
        SQLError.sqlStateMessages.put("01006", Messages.getString("SQLError.37"));
        SQLError.sqlStateMessages.put("01S00", Messages.getString("SQLError.38"));
        SQLError.sqlStateMessages.put("01S01", Messages.getString("SQLError.39"));
        SQLError.sqlStateMessages.put("01S03", Messages.getString("SQLError.40"));
        SQLError.sqlStateMessages.put("01S04", Messages.getString("SQLError.41"));
        SQLError.sqlStateMessages.put("07001", Messages.getString("SQLError.42"));
        SQLError.sqlStateMessages.put("08001", Messages.getString("SQLError.43"));
        SQLError.sqlStateMessages.put("08002", Messages.getString("SQLError.44"));
        SQLError.sqlStateMessages.put("08003", Messages.getString("SQLError.45"));
        SQLError.sqlStateMessages.put("08004", Messages.getString("SQLError.46"));
        SQLError.sqlStateMessages.put("08007", Messages.getString("SQLError.47"));
        SQLError.sqlStateMessages.put("08S01", Messages.getString("SQLError.48"));
        SQLError.sqlStateMessages.put("21S01", Messages.getString("SQLError.49"));
        SQLError.sqlStateMessages.put("22003", Messages.getString("SQLError.50"));
        SQLError.sqlStateMessages.put("22008", Messages.getString("SQLError.51"));
        SQLError.sqlStateMessages.put("22012", Messages.getString("SQLError.52"));
        SQLError.sqlStateMessages.put("40001", Messages.getString("SQLError.53"));
        SQLError.sqlStateMessages.put("28000", Messages.getString("SQLError.54"));
        SQLError.sqlStateMessages.put("42000", Messages.getString("SQLError.55"));
        SQLError.sqlStateMessages.put("42S02", Messages.getString("SQLError.56"));
        SQLError.sqlStateMessages.put("S0001", Messages.getString("SQLError.57"));
        SQLError.sqlStateMessages.put("S0002", Messages.getString("SQLError.58"));
        SQLError.sqlStateMessages.put("S0011", Messages.getString("SQLError.59"));
        SQLError.sqlStateMessages.put("S0012", Messages.getString("SQLError.60"));
        SQLError.sqlStateMessages.put("S0021", Messages.getString("SQLError.61"));
        SQLError.sqlStateMessages.put("S0022", Messages.getString("SQLError.62"));
        SQLError.sqlStateMessages.put("S0023", Messages.getString("SQLError.63"));
        SQLError.sqlStateMessages.put("S1000", Messages.getString("SQLError.64"));
        SQLError.sqlStateMessages.put("S1001", Messages.getString("SQLError.65"));
        SQLError.sqlStateMessages.put("S1002", Messages.getString("SQLError.66"));
        SQLError.sqlStateMessages.put("S1009", Messages.getString("SQLError.67"));
        SQLError.sqlStateMessages.put("S1C00", Messages.getString("SQLError.68"));
        SQLError.sqlStateMessages.put("S1T00", Messages.getString("SQLError.69"));
        (SQLError.mysqlToSqlState = new Hashtable<Integer, String>()).put(1249, "01000");
        SQLError.mysqlToSqlState.put(1261, "01000");
        SQLError.mysqlToSqlState.put(1262, "01000");
        SQLError.mysqlToSqlState.put(1265, "01000");
        SQLError.mysqlToSqlState.put(1311, "01000");
        SQLError.mysqlToSqlState.put(1642, "01000");
        SQLError.mysqlToSqlState.put(1040, "08004");
        SQLError.mysqlToSqlState.put(1251, "08004");
        SQLError.mysqlToSqlState.put(1042, "08004");
        SQLError.mysqlToSqlState.put(1043, "08004");
        SQLError.mysqlToSqlState.put(1129, "08004");
        SQLError.mysqlToSqlState.put(1130, "08004");
        SQLError.mysqlToSqlState.put(1047, "08S01");
        SQLError.mysqlToSqlState.put(1053, "08S01");
        SQLError.mysqlToSqlState.put(1080, "08S01");
        SQLError.mysqlToSqlState.put(1081, "08S01");
        SQLError.mysqlToSqlState.put(1152, "08S01");
        SQLError.mysqlToSqlState.put(1153, "08S01");
        SQLError.mysqlToSqlState.put(1154, "08S01");
        SQLError.mysqlToSqlState.put(1155, "08S01");
        SQLError.mysqlToSqlState.put(1156, "08S01");
        SQLError.mysqlToSqlState.put(1157, "08S01");
        SQLError.mysqlToSqlState.put(1158, "08S01");
        SQLError.mysqlToSqlState.put(1159, "08S01");
        SQLError.mysqlToSqlState.put(1160, "08S01");
        SQLError.mysqlToSqlState.put(1161, "08S01");
        SQLError.mysqlToSqlState.put(1184, "08S01");
        SQLError.mysqlToSqlState.put(1189, "08S01");
        SQLError.mysqlToSqlState.put(1190, "08S01");
        SQLError.mysqlToSqlState.put(1218, "08S01");
        SQLError.mysqlToSqlState.put(1312, "0A000");
        SQLError.mysqlToSqlState.put(1314, "0A000");
        SQLError.mysqlToSqlState.put(1335, "0A000");
        SQLError.mysqlToSqlState.put(1336, "0A000");
        SQLError.mysqlToSqlState.put(1415, "0A000");
        SQLError.mysqlToSqlState.put(1845, "0A000");
        SQLError.mysqlToSqlState.put(1846, "0A000");
        SQLError.mysqlToSqlState.put(1044, "42000");
        SQLError.mysqlToSqlState.put(1049, "42000");
        SQLError.mysqlToSqlState.put(1055, "S1009");
        SQLError.mysqlToSqlState.put(1056, "S1009");
        SQLError.mysqlToSqlState.put(1057, "S1009");
        SQLError.mysqlToSqlState.put(1059, "S1009");
        SQLError.mysqlToSqlState.put(1060, "S1009");
        SQLError.mysqlToSqlState.put(1061, "S1009");
        SQLError.mysqlToSqlState.put(1062, "S1009");
        SQLError.mysqlToSqlState.put(1063, "S1009");
        SQLError.mysqlToSqlState.put(1064, "42000");
        SQLError.mysqlToSqlState.put(1065, "42000");
        SQLError.mysqlToSqlState.put(1066, "S1009");
        SQLError.mysqlToSqlState.put(1067, "S1009");
        SQLError.mysqlToSqlState.put(1068, "S1009");
        SQLError.mysqlToSqlState.put(1069, "S1009");
        SQLError.mysqlToSqlState.put(1070, "S1009");
        SQLError.mysqlToSqlState.put(1071, "S1009");
        SQLError.mysqlToSqlState.put(1072, "S1009");
        SQLError.mysqlToSqlState.put(1073, "S1009");
        SQLError.mysqlToSqlState.put(1074, "S1009");
        SQLError.mysqlToSqlState.put(1075, "S1009");
        SQLError.mysqlToSqlState.put(1082, "S1009");
        SQLError.mysqlToSqlState.put(1083, "S1009");
        SQLError.mysqlToSqlState.put(1084, "S1009");
        SQLError.mysqlToSqlState.put(1090, "42000");
        SQLError.mysqlToSqlState.put(1091, "42000");
        SQLError.mysqlToSqlState.put(1101, "42000");
        SQLError.mysqlToSqlState.put(1102, "42000");
        SQLError.mysqlToSqlState.put(1103, "42000");
        SQLError.mysqlToSqlState.put(1104, "42000");
        SQLError.mysqlToSqlState.put(1106, "42000");
        SQLError.mysqlToSqlState.put(1107, "42000");
        SQLError.mysqlToSqlState.put(1110, "42000");
        SQLError.mysqlToSqlState.put(1112, "42000");
        SQLError.mysqlToSqlState.put(1113, "42000");
        SQLError.mysqlToSqlState.put(1115, "42000");
        SQLError.mysqlToSqlState.put(1118, "42000");
        SQLError.mysqlToSqlState.put(1120, "42000");
        SQLError.mysqlToSqlState.put(1121, "42000");
        SQLError.mysqlToSqlState.put(1131, "42000");
        SQLError.mysqlToSqlState.put(1132, "42000");
        SQLError.mysqlToSqlState.put(1133, "42000");
        SQLError.mysqlToSqlState.put(1139, "42000");
        SQLError.mysqlToSqlState.put(1140, "42000");
        SQLError.mysqlToSqlState.put(1141, "42000");
        SQLError.mysqlToSqlState.put(1142, "42000");
        SQLError.mysqlToSqlState.put(1143, "42000");
        SQLError.mysqlToSqlState.put(1144, "42000");
        SQLError.mysqlToSqlState.put(1145, "42000");
        SQLError.mysqlToSqlState.put(1147, "42000");
        SQLError.mysqlToSqlState.put(1148, "42000");
        SQLError.mysqlToSqlState.put(1149, "42000");
        SQLError.mysqlToSqlState.put(1162, "42000");
        SQLError.mysqlToSqlState.put(1163, "42000");
        SQLError.mysqlToSqlState.put(1164, "42000");
        SQLError.mysqlToSqlState.put(1166, "42000");
        SQLError.mysqlToSqlState.put(1167, "42000");
        SQLError.mysqlToSqlState.put(1170, "42000");
        SQLError.mysqlToSqlState.put(1171, "42000");
        SQLError.mysqlToSqlState.put(1172, "42000");
        SQLError.mysqlToSqlState.put(1173, "42000");
        SQLError.mysqlToSqlState.put(1176, "42000");
        SQLError.mysqlToSqlState.put(1177, "42000");
        SQLError.mysqlToSqlState.put(1178, "42000");
        SQLError.mysqlToSqlState.put(1203, "42000");
        SQLError.mysqlToSqlState.put(1211, "42000");
        SQLError.mysqlToSqlState.put(1226, "42000");
        SQLError.mysqlToSqlState.put(1227, "42000");
        SQLError.mysqlToSqlState.put(1230, "42000");
        SQLError.mysqlToSqlState.put(1231, "42000");
        SQLError.mysqlToSqlState.put(1232, "42000");
        SQLError.mysqlToSqlState.put(1234, "42000");
        SQLError.mysqlToSqlState.put(1235, "42000");
        SQLError.mysqlToSqlState.put(1239, "42000");
        SQLError.mysqlToSqlState.put(1248, "42000");
        SQLError.mysqlToSqlState.put(1250, "42000");
        SQLError.mysqlToSqlState.put(1252, "42000");
        SQLError.mysqlToSqlState.put(1253, "42000");
        SQLError.mysqlToSqlState.put(1280, "42000");
        SQLError.mysqlToSqlState.put(1281, "42000");
        SQLError.mysqlToSqlState.put(1286, "42000");
        SQLError.mysqlToSqlState.put(1304, "42000");
        SQLError.mysqlToSqlState.put(1305, "42000");
        SQLError.mysqlToSqlState.put(1308, "42000");
        SQLError.mysqlToSqlState.put(1309, "42000");
        SQLError.mysqlToSqlState.put(1310, "42000");
        SQLError.mysqlToSqlState.put(1313, "42000");
        SQLError.mysqlToSqlState.put(1315, "42000");
        SQLError.mysqlToSqlState.put(1316, "42000");
        SQLError.mysqlToSqlState.put(1318, "42000");
        SQLError.mysqlToSqlState.put(1319, "42000");
        SQLError.mysqlToSqlState.put(1320, "42000");
        SQLError.mysqlToSqlState.put(1322, "42000");
        SQLError.mysqlToSqlState.put(1323, "42000");
        SQLError.mysqlToSqlState.put(1324, "42000");
        SQLError.mysqlToSqlState.put(1327, "42000");
        SQLError.mysqlToSqlState.put(1330, "42000");
        SQLError.mysqlToSqlState.put(1331, "42000");
        SQLError.mysqlToSqlState.put(1332, "42000");
        SQLError.mysqlToSqlState.put(1333, "42000");
        SQLError.mysqlToSqlState.put(1337, "42000");
        SQLError.mysqlToSqlState.put(1338, "42000");
        SQLError.mysqlToSqlState.put(1370, "42000");
        SQLError.mysqlToSqlState.put(1403, "42000");
        SQLError.mysqlToSqlState.put(1407, "42000");
        SQLError.mysqlToSqlState.put(1410, "42000");
        SQLError.mysqlToSqlState.put(1413, "42000");
        SQLError.mysqlToSqlState.put(1414, "42000");
        SQLError.mysqlToSqlState.put(1425, "42000");
        SQLError.mysqlToSqlState.put(1426, "42000");
        SQLError.mysqlToSqlState.put(1427, "42000");
        SQLError.mysqlToSqlState.put(1437, "42000");
        SQLError.mysqlToSqlState.put(1439, "42000");
        SQLError.mysqlToSqlState.put(1453, "42000");
        SQLError.mysqlToSqlState.put(1458, "42000");
        SQLError.mysqlToSqlState.put(1460, "42000");
        SQLError.mysqlToSqlState.put(1461, "42000");
        SQLError.mysqlToSqlState.put(1463, "42000");
        SQLError.mysqlToSqlState.put(1582, "42000");
        SQLError.mysqlToSqlState.put(1583, "42000");
        SQLError.mysqlToSqlState.put(1584, "42000");
        SQLError.mysqlToSqlState.put(1630, "42000");
        SQLError.mysqlToSqlState.put(1641, "42000");
        SQLError.mysqlToSqlState.put(1687, "42000");
        SQLError.mysqlToSqlState.put(1701, "42000");
        SQLError.mysqlToSqlState.put(1222, "21000");
        SQLError.mysqlToSqlState.put(1241, "21000");
        SQLError.mysqlToSqlState.put(1242, "21000");
        SQLError.mysqlToSqlState.put(1022, "23000");
        SQLError.mysqlToSqlState.put(1048, "23000");
        SQLError.mysqlToSqlState.put(1052, "23000");
        SQLError.mysqlToSqlState.put(1169, "23000");
        SQLError.mysqlToSqlState.put(1216, "23000");
        SQLError.mysqlToSqlState.put(1217, "23000");
        SQLError.mysqlToSqlState.put(1451, "23000");
        SQLError.mysqlToSqlState.put(1452, "23000");
        SQLError.mysqlToSqlState.put(1557, "23000");
        SQLError.mysqlToSqlState.put(1586, "23000");
        SQLError.mysqlToSqlState.put(1761, "23000");
        SQLError.mysqlToSqlState.put(1762, "23000");
        SQLError.mysqlToSqlState.put(1859, "23000");
        SQLError.mysqlToSqlState.put(1406, "22001");
        SQLError.mysqlToSqlState.put(1264, "01000");
        SQLError.mysqlToSqlState.put(1416, "22003");
        SQLError.mysqlToSqlState.put(1690, "22003");
        SQLError.mysqlToSqlState.put(1292, "22007");
        SQLError.mysqlToSqlState.put(1367, "22007");
        SQLError.mysqlToSqlState.put(1441, "22008");
        SQLError.mysqlToSqlState.put(1365, "22012");
        SQLError.mysqlToSqlState.put(1325, "24000");
        SQLError.mysqlToSqlState.put(1326, "24000");
        SQLError.mysqlToSqlState.put(1179, "25000");
        SQLError.mysqlToSqlState.put(1207, "25000");
        SQLError.mysqlToSqlState.put(1045, "28000");
        SQLError.mysqlToSqlState.put(1698, "28000");
        SQLError.mysqlToSqlState.put(1873, "28000");
        SQLError.mysqlToSqlState.put(1758, "35000");
        SQLError.mysqlToSqlState.put(1046, "3D000");
        SQLError.mysqlToSqlState.put(1058, "21S01");
        SQLError.mysqlToSqlState.put(1136, "21S01");
        SQLError.mysqlToSqlState.put(1050, "42S01");
        SQLError.mysqlToSqlState.put(1051, "42S02");
        SQLError.mysqlToSqlState.put(1109, "42S02");
        SQLError.mysqlToSqlState.put(1146, "42S02");
        SQLError.mysqlToSqlState.put(1054, "S0022");
        SQLError.mysqlToSqlState.put(1247, "42S22");
        SQLError.mysqlToSqlState.put(1037, "S1001");
        SQLError.mysqlToSqlState.put(1038, "S1001");
        SQLError.mysqlToSqlState.put(1205, "40001");
        SQLError.mysqlToSqlState.put(1213, "40001");
        (SQLError.mysqlToSql99State = new HashMap<Integer, String>()).put(1249, "01000");
        SQLError.mysqlToSql99State.put(1261, "01000");
        SQLError.mysqlToSql99State.put(1262, "01000");
        SQLError.mysqlToSql99State.put(1265, "01000");
        SQLError.mysqlToSql99State.put(1263, "01000");
        SQLError.mysqlToSql99State.put(1264, "01000");
        SQLError.mysqlToSql99State.put(1311, "01000");
        SQLError.mysqlToSql99State.put(1642, "01000");
        SQLError.mysqlToSql99State.put(1329, "02000");
        SQLError.mysqlToSql99State.put(1643, "02000");
        SQLError.mysqlToSql99State.put(1040, "08004");
        SQLError.mysqlToSql99State.put(1251, "08004");
        SQLError.mysqlToSql99State.put(1042, "08S01");
        SQLError.mysqlToSql99State.put(1043, "08S01");
        SQLError.mysqlToSql99State.put(1047, "08S01");
        SQLError.mysqlToSql99State.put(1053, "08S01");
        SQLError.mysqlToSql99State.put(1080, "08S01");
        SQLError.mysqlToSql99State.put(1081, "08S01");
        SQLError.mysqlToSql99State.put(1152, "08S01");
        SQLError.mysqlToSql99State.put(1153, "08S01");
        SQLError.mysqlToSql99State.put(1154, "08S01");
        SQLError.mysqlToSql99State.put(1155, "08S01");
        SQLError.mysqlToSql99State.put(1156, "08S01");
        SQLError.mysqlToSql99State.put(1157, "08S01");
        SQLError.mysqlToSql99State.put(1158, "08S01");
        SQLError.mysqlToSql99State.put(1159, "08S01");
        SQLError.mysqlToSql99State.put(1160, "08S01");
        SQLError.mysqlToSql99State.put(1161, "08S01");
        SQLError.mysqlToSql99State.put(1184, "08S01");
        SQLError.mysqlToSql99State.put(1189, "08S01");
        SQLError.mysqlToSql99State.put(1190, "08S01");
        SQLError.mysqlToSql99State.put(1218, "08S01");
        SQLError.mysqlToSql99State.put(1312, "0A000");
        SQLError.mysqlToSql99State.put(1314, "0A000");
        SQLError.mysqlToSql99State.put(1335, "0A000");
        SQLError.mysqlToSql99State.put(1336, "0A000");
        SQLError.mysqlToSql99State.put(1415, "0A000");
        SQLError.mysqlToSql99State.put(1845, "0A000");
        SQLError.mysqlToSql99State.put(1846, "0A000");
        SQLError.mysqlToSql99State.put(1044, "42000");
        SQLError.mysqlToSql99State.put(1049, "42000");
        SQLError.mysqlToSql99State.put(1055, "42000");
        SQLError.mysqlToSql99State.put(1056, "42000");
        SQLError.mysqlToSql99State.put(1057, "42000");
        SQLError.mysqlToSql99State.put(1059, "42000");
        SQLError.mysqlToSql99State.put(1061, "42000");
        SQLError.mysqlToSql99State.put(1063, "42000");
        SQLError.mysqlToSql99State.put(1064, "42000");
        SQLError.mysqlToSql99State.put(1065, "42000");
        SQLError.mysqlToSql99State.put(1066, "42000");
        SQLError.mysqlToSql99State.put(1067, "42000");
        SQLError.mysqlToSql99State.put(1068, "42000");
        SQLError.mysqlToSql99State.put(1069, "42000");
        SQLError.mysqlToSql99State.put(1070, "42000");
        SQLError.mysqlToSql99State.put(1071, "42000");
        SQLError.mysqlToSql99State.put(1072, "42000");
        SQLError.mysqlToSql99State.put(1073, "42000");
        SQLError.mysqlToSql99State.put(1074, "42000");
        SQLError.mysqlToSql99State.put(1075, "42000");
        SQLError.mysqlToSql99State.put(1083, "42000");
        SQLError.mysqlToSql99State.put(1084, "42000");
        SQLError.mysqlToSql99State.put(1090, "42000");
        SQLError.mysqlToSql99State.put(1091, "42000");
        SQLError.mysqlToSql99State.put(1101, "42000");
        SQLError.mysqlToSql99State.put(1102, "42000");
        SQLError.mysqlToSql99State.put(1103, "42000");
        SQLError.mysqlToSql99State.put(1104, "42000");
        SQLError.mysqlToSql99State.put(1106, "42000");
        SQLError.mysqlToSql99State.put(1107, "42000");
        SQLError.mysqlToSql99State.put(1110, "42000");
        SQLError.mysqlToSql99State.put(1112, "42000");
        SQLError.mysqlToSql99State.put(1113, "42000");
        SQLError.mysqlToSql99State.put(1115, "42000");
        SQLError.mysqlToSql99State.put(1118, "42000");
        SQLError.mysqlToSql99State.put(1120, "42000");
        SQLError.mysqlToSql99State.put(1121, "42000");
        SQLError.mysqlToSql99State.put(1131, "42000");
        SQLError.mysqlToSql99State.put(1132, "42000");
        SQLError.mysqlToSql99State.put(1133, "42000");
        SQLError.mysqlToSql99State.put(1139, "42000");
        SQLError.mysqlToSql99State.put(1140, "42000");
        SQLError.mysqlToSql99State.put(1141, "42000");
        SQLError.mysqlToSql99State.put(1142, "42000");
        SQLError.mysqlToSql99State.put(1143, "42000");
        SQLError.mysqlToSql99State.put(1144, "42000");
        SQLError.mysqlToSql99State.put(1145, "42000");
        SQLError.mysqlToSql99State.put(1147, "42000");
        SQLError.mysqlToSql99State.put(1148, "42000");
        SQLError.mysqlToSql99State.put(1149, "42000");
        SQLError.mysqlToSql99State.put(1162, "42000");
        SQLError.mysqlToSql99State.put(1163, "42000");
        SQLError.mysqlToSql99State.put(1164, "42000");
        SQLError.mysqlToSql99State.put(1166, "42000");
        SQLError.mysqlToSql99State.put(1167, "42000");
        SQLError.mysqlToSql99State.put(1170, "42000");
        SQLError.mysqlToSql99State.put(1171, "42000");
        SQLError.mysqlToSql99State.put(1172, "42000");
        SQLError.mysqlToSql99State.put(1173, "42000");
        SQLError.mysqlToSql99State.put(1176, "42000");
        SQLError.mysqlToSql99State.put(1177, "42000");
        SQLError.mysqlToSql99State.put(1178, "42000");
        SQLError.mysqlToSql99State.put(1203, "42000");
        SQLError.mysqlToSql99State.put(1211, "42000");
        SQLError.mysqlToSql99State.put(1226, "42000");
        SQLError.mysqlToSql99State.put(1227, "42000");
        SQLError.mysqlToSql99State.put(1230, "42000");
        SQLError.mysqlToSql99State.put(1231, "42000");
        SQLError.mysqlToSql99State.put(1232, "42000");
        SQLError.mysqlToSql99State.put(1234, "42000");
        SQLError.mysqlToSql99State.put(1235, "42000");
        SQLError.mysqlToSql99State.put(1239, "42000");
        SQLError.mysqlToSql99State.put(1248, "42000");
        SQLError.mysqlToSql99State.put(1250, "42000");
        SQLError.mysqlToSql99State.put(1252, "42000");
        SQLError.mysqlToSql99State.put(1253, "42000");
        SQLError.mysqlToSql99State.put(1280, "42000");
        SQLError.mysqlToSql99State.put(1281, "42000");
        SQLError.mysqlToSql99State.put(1286, "42000");
        SQLError.mysqlToSql99State.put(1304, "42000");
        SQLError.mysqlToSql99State.put(1305, "42000");
        SQLError.mysqlToSql99State.put(1308, "42000");
        SQLError.mysqlToSql99State.put(1309, "42000");
        SQLError.mysqlToSql99State.put(1310, "42000");
        SQLError.mysqlToSql99State.put(1313, "42000");
        SQLError.mysqlToSql99State.put(1315, "42000");
        SQLError.mysqlToSql99State.put(1316, "42000");
        SQLError.mysqlToSql99State.put(1318, "42000");
        SQLError.mysqlToSql99State.put(1319, "42000");
        SQLError.mysqlToSql99State.put(1320, "42000");
        SQLError.mysqlToSql99State.put(1322, "42000");
        SQLError.mysqlToSql99State.put(1323, "42000");
        SQLError.mysqlToSql99State.put(1324, "42000");
        SQLError.mysqlToSql99State.put(1327, "42000");
        SQLError.mysqlToSql99State.put(1330, "42000");
        SQLError.mysqlToSql99State.put(1331, "42000");
        SQLError.mysqlToSql99State.put(1332, "42000");
        SQLError.mysqlToSql99State.put(1333, "42000");
        SQLError.mysqlToSql99State.put(1337, "42000");
        SQLError.mysqlToSql99State.put(1338, "42000");
        SQLError.mysqlToSql99State.put(1370, "42000");
        SQLError.mysqlToSql99State.put(1403, "42000");
        SQLError.mysqlToSql99State.put(1407, "42000");
        SQLError.mysqlToSql99State.put(1410, "42000");
        SQLError.mysqlToSql99State.put(1413, "42000");
        SQLError.mysqlToSql99State.put(1414, "42000");
        SQLError.mysqlToSql99State.put(1425, "42000");
        SQLError.mysqlToSql99State.put(1426, "42000");
        SQLError.mysqlToSql99State.put(1427, "42000");
        SQLError.mysqlToSql99State.put(1437, "42000");
        SQLError.mysqlToSql99State.put(1439, "42000");
        SQLError.mysqlToSql99State.put(1453, "42000");
        SQLError.mysqlToSql99State.put(1458, "42000");
        SQLError.mysqlToSql99State.put(1460, "42000");
        SQLError.mysqlToSql99State.put(1461, "42000");
        SQLError.mysqlToSql99State.put(1463, "42000");
        SQLError.mysqlToSql99State.put(1582, "42000");
        SQLError.mysqlToSql99State.put(1583, "42000");
        SQLError.mysqlToSql99State.put(1584, "42000");
        SQLError.mysqlToSql99State.put(1630, "42000");
        SQLError.mysqlToSql99State.put(1641, "42000");
        SQLError.mysqlToSql99State.put(1687, "42000");
        SQLError.mysqlToSql99State.put(1701, "42000");
        SQLError.mysqlToSql99State.put(1222, "21000");
        SQLError.mysqlToSql99State.put(1241, "21000");
        SQLError.mysqlToSql99State.put(1242, "21000");
        SQLError.mysqlToSql99State.put(1022, "23000");
        SQLError.mysqlToSql99State.put(1048, "23000");
        SQLError.mysqlToSql99State.put(1052, "23000");
        SQLError.mysqlToSql99State.put(1062, "23000");
        SQLError.mysqlToSql99State.put(1169, "23000");
        SQLError.mysqlToSql99State.put(1216, "23000");
        SQLError.mysqlToSql99State.put(1217, "23000");
        SQLError.mysqlToSql99State.put(1451, "23000");
        SQLError.mysqlToSql99State.put(1452, "23000");
        SQLError.mysqlToSql99State.put(1557, "23000");
        SQLError.mysqlToSql99State.put(1586, "23000");
        SQLError.mysqlToSql99State.put(1761, "23000");
        SQLError.mysqlToSql99State.put(1762, "23000");
        SQLError.mysqlToSql99State.put(1859, "23000");
        SQLError.mysqlToSql99State.put(1406, "22001");
        SQLError.mysqlToSql99State.put(1416, "22003");
        SQLError.mysqlToSql99State.put(1690, "22003");
        SQLError.mysqlToSql99State.put(1292, "22007");
        SQLError.mysqlToSql99State.put(1367, "22007");
        SQLError.mysqlToSql99State.put(1441, "22008");
        SQLError.mysqlToSql99State.put(1365, "22012");
        SQLError.mysqlToSql99State.put(1325, "24000");
        SQLError.mysqlToSql99State.put(1326, "24000");
        SQLError.mysqlToSql99State.put(1179, "25000");
        SQLError.mysqlToSql99State.put(1207, "25000");
        SQLError.mysqlToSql99State.put(1045, "28000");
        SQLError.mysqlToSql99State.put(1698, "28000");
        SQLError.mysqlToSql99State.put(1873, "28000");
        SQLError.mysqlToSql99State.put(1758, "35000");
        SQLError.mysqlToSql99State.put(1046, "3D000");
        SQLError.mysqlToSql99State.put(1645, "0K000");
        SQLError.mysqlToSql99State.put(1887, "0Z002");
        SQLError.mysqlToSql99State.put(1339, "20000");
        SQLError.mysqlToSql99State.put(1058, "21S01");
        SQLError.mysqlToSql99State.put(1136, "21S01");
        SQLError.mysqlToSql99State.put(1138, "42000");
        SQLError.mysqlToSql99State.put(1903, "2201E");
        SQLError.mysqlToSql99State.put(1568, "25001");
        SQLError.mysqlToSql99State.put(1792, "25006");
        SQLError.mysqlToSql99State.put(1303, "2F003");
        SQLError.mysqlToSql99State.put(1321, "2F005");
        SQLError.mysqlToSql99State.put(1050, "42S01");
        SQLError.mysqlToSql99State.put(1051, "42S02");
        SQLError.mysqlToSql99State.put(1109, "42S02");
        SQLError.mysqlToSql99State.put(1146, "42S02");
        SQLError.mysqlToSql99State.put(1082, "42S12");
        SQLError.mysqlToSql99State.put(1060, "42S21");
        SQLError.mysqlToSql99State.put(1054, "42S22");
        SQLError.mysqlToSql99State.put(1247, "42S22");
        SQLError.mysqlToSql99State.put(1317, "70100");
        SQLError.mysqlToSql99State.put(1037, "HY001");
        SQLError.mysqlToSql99State.put(1038, "HY001");
        SQLError.mysqlToSql99State.put(1402, "XA100");
        SQLError.mysqlToSql99State.put(1614, "XA102");
        SQLError.mysqlToSql99State.put(1613, "XA106");
        SQLError.mysqlToSql99State.put(1401, "XAE03");
        SQLError.mysqlToSql99State.put(1397, "XAE04");
        SQLError.mysqlToSql99State.put(1398, "XAE05");
        SQLError.mysqlToSql99State.put(1399, "XAE07");
        SQLError.mysqlToSql99State.put(1440, "XAE08");
        SQLError.mysqlToSql99State.put(1400, "XAE09");
        SQLError.mysqlToSql99State.put(1205, "40001");
        SQLError.mysqlToSql99State.put(1213, "40001");
    }
}
