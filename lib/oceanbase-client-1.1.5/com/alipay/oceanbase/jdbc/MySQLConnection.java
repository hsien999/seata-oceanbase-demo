// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import com.alipay.oceanbase.jdbc.stats.ConnectionStats;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEventHandler;
import java.util.List;
import java.util.Properties;
import java.sql.Statement;
import com.alipay.oceanbase.jdbc.log.Log;
import java.util.TimeZone;
import java.util.Timer;
import java.util.Calendar;
import java.sql.SQLException;

public interface MySQLConnection extends Connection, ConnectionProperties
{
    boolean isProxySet();
    
    void createNewIO(final boolean p0) throws SQLException;
    
    void dumpTestcaseQuery(final String p0);
    
    Connection duplicate() throws SQLException;
    
    ResultSetInternalMethods execSQL(final StatementImpl p0, final String p1, final int p2, final Buffer p3, final int p4, final int p5, final boolean p6, final String p7, final Field[] p8) throws SQLException;
    
    ResultSetInternalMethods execSQL(final StatementImpl p0, final String p1, final int p2, final Buffer p3, final int p4, final int p5, final boolean p6, final String p7, final Field[] p8, final boolean p9) throws SQLException;
    
    String extractSqlFromPacket(final String p0, final Buffer p1, final int p2) throws SQLException;
    
    StringBuilder generateConnectionCommentBlock(final StringBuilder p0);
    
    int getActiveStatementCount();
    
    int getAutoIncrementIncrement();
    
    CachedResultSetMetaData getCachedMetaData(final String p0);
    
    Calendar getCalendarInstanceForSessionOrNew();
    
    Timer getCancelTimer();
    
    String getCharacterSetMetadata();
    
    SingleByteCharsetConverter getCharsetConverter(final String p0) throws SQLException;
    
    @Deprecated
    String getCharsetNameForIndex(final int p0) throws SQLException;
    
    String getEncodingForIndex(final int p0) throws SQLException;
    
    TimeZone getDefaultTimeZone();
    
    String getErrorMessageEncoding();
    
    ExceptionInterceptor getExceptionInterceptor();
    
    String getHost();
    
    String getHostPortPair();
    
    long getId();
    
    long getIdleFor();
    
    MysqlIO getIO() throws SQLException;
    
    boolean isAlive();
    
    Log getLog() throws SQLException;
    
    int getMaxBytesPerChar(final String p0) throws SQLException;
    
    int getMaxBytesPerChar(final Integer p0, final String p1) throws SQLException;
    
    Statement getMetadataSafeStatement() throws SQLException;
    
    int getNetBufferLength();
    
    Properties getProperties();
    
    boolean getRequiresEscapingEncoder();
    
    String getServerCharset();
    
    int getServerMajorVersion();
    
    int getServerMinorVersion();
    
    int getServerSubMinorVersion();
    
    TimeZone getServerTimezoneTZ();
    
    String getServerVariable(final String p0);
    
    String getServerVersion();
    
    Calendar getSessionLockedCalendar();
    
    String getStatementComment();
    
    List<StatementInterceptorV2> getStatementInterceptorsInstances();
    
    String getURL();
    
    String getUser();
    
    Calendar getUtcCalendar();
    
    void incrementNumberOfPreparedExecutes();
    
    void incrementNumberOfPrepares();
    
    void incrementNumberOfResultSetsCreated();
    
    void initializeResultsMetadataFromCache(final String p0, final CachedResultSetMetaData p1, final ResultSetInternalMethods p2) throws SQLException;
    
    void initializeSafeStatementInterceptors() throws SQLException;
    
    boolean isAbonormallyLongQuery(final long p0);
    
    boolean isClientTzUTC();
    
    boolean isCursorFetchEnabled() throws SQLException;
    
    boolean isReadInfoMsgEnabled();
    
    boolean isReadOnly() throws SQLException;
    
    boolean isReadOnly(final boolean p0) throws SQLException;
    
    boolean isRunningOnJDK13();
    
    boolean isServerTzUTC();
    
    boolean lowerCaseTableNames();
    
    void pingInternal(final boolean p0, final int p1) throws SQLException;
    
    void realClose(final boolean p0, final boolean p1, final boolean p2, final Throwable p3) throws SQLException;
    
    void recachePreparedStatement(final ServerPreparedStatement p0) throws SQLException;
    
    void decachePreparedStatement(final ServerPreparedStatement p0) throws SQLException;
    
    void registerQueryExecutionTime(final long p0);
    
    void registerStatement(final com.alipay.oceanbase.jdbc.Statement p0);
    
    void reportNumberOfTablesAccessed(final int p0);
    
    boolean serverSupportsConvertFn() throws SQLException;
    
    void setProxy(final MySQLConnection p0);
    
    void setReadInfoMsgEnabled(final boolean p0);
    
    void setReadOnlyInternal(final boolean p0) throws SQLException;
    
    void shutdownServer() throws SQLException;
    
    boolean storesLowerCaseTableName();
    
    void throwConnectionClosedException() throws SQLException;
    
    void transactionBegun() throws SQLException;
    
    void transactionCompleted() throws SQLException;
    
    void unregisterStatement(final com.alipay.oceanbase.jdbc.Statement p0);
    
    void unSafeStatementInterceptors() throws SQLException;
    
    boolean useAnsiQuotedIdentifiers();
    
    String getConnectionAttributes() throws SQLException;
    
    @Deprecated
    MySQLConnection getLoadBalanceSafeProxy();
    
    MySQLConnection getMultiHostSafeProxy();
    
    ProfilerEventHandler getProfilerEventHandlerInstance();
    
    void setProfilerEventHandlerInstance(final ProfilerEventHandler p0);
    
    ConnectionStats getConnectionStats();
}
