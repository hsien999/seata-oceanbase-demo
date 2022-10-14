// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import com.alipay.oceanbase.jdbc.extend.datatype.ComplexDataType;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEventHandler;
import java.util.concurrent.Executor;
import java.sql.Savepoint;
import java.sql.Struct;
import java.sql.Array;
import java.sql.SQLClientInfoException;
import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.CallableStatement;
import java.sql.SQLWarning;
import java.util.Map;
import java.util.List;
import java.util.Properties;
import java.sql.DatabaseMetaData;
import com.alipay.oceanbase.jdbc.log.Log;
import java.util.TimeZone;
import java.util.Timer;
import java.util.Calendar;
import java.sql.Statement;
import java.sql.PreparedStatement;
import com.alipay.oceanbase.jdbc.stats.ConnectionStats;
import java.sql.SQLException;

public class MultiHostMySQLConnection implements ObConnection
{
    protected MultiHostConnectionProxy thisAsProxy;
    
    public MultiHostMySQLConnection(final MultiHostConnectionProxy proxy) {
        this.thisAsProxy = proxy;
    }
    
    protected MultiHostConnectionProxy getThisAsProxy() {
        return this.thisAsProxy;
    }
    
    protected MySQLConnection getActiveMySQLConnection() {
        synchronized (this.thisAsProxy) {
            return this.thisAsProxy.currentConnection;
        }
    }
    
    @Override
    public void abortInternal() throws SQLException {
        this.getActiveMySQLConnection().abortInternal();
    }
    
    @Override
    public void changeUser(final String userName, final String newPassword) throws SQLException {
        this.getActiveMySQLConnection().changeUser(userName, newPassword);
    }
    
    @Override
    public void checkClosed() throws SQLException {
        this.getActiveMySQLConnection().checkClosed();
    }
    
    @Override
    public ConnectionStats getConnectionStats() {
        return new ConnectionStats();
    }
    
    @Deprecated
    @Override
    public void clearHasTriedMaster() {
        this.getActiveMySQLConnection().clearHasTriedMaster();
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        this.getActiveMySQLConnection().clearWarnings();
    }
    
    @Override
    public PreparedStatement clientPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return this.getActiveMySQLConnection().clientPrepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement clientPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return this.getActiveMySQLConnection().clientPrepareStatement(sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public PreparedStatement clientPrepareStatement(final String sql, final int autoGenKeyIndex) throws SQLException {
        return this.getActiveMySQLConnection().clientPrepareStatement(sql, autoGenKeyIndex);
    }
    
    @Override
    public PreparedStatement clientPrepareStatement(final String sql, final int[] autoGenKeyIndexes) throws SQLException {
        return this.getActiveMySQLConnection().clientPrepareStatement(sql, autoGenKeyIndexes);
    }
    
    @Override
    public PreparedStatement clientPrepareStatement(final String sql, final String[] autoGenKeyColNames) throws SQLException {
        return this.getActiveMySQLConnection().clientPrepareStatement(sql, autoGenKeyColNames);
    }
    
    @Override
    public PreparedStatement clientPrepareStatement(final String sql) throws SQLException {
        return this.getActiveMySQLConnection().clientPrepareStatement(sql);
    }
    
    @Override
    public void close() throws SQLException {
        this.getActiveMySQLConnection().close();
    }
    
    @Override
    public void commit() throws SQLException {
        this.getActiveMySQLConnection().commit();
    }
    
    @Override
    public void createNewIO(final boolean isForReconnect) throws SQLException {
        this.getActiveMySQLConnection().createNewIO(isForReconnect);
    }
    
    @Override
    public Statement createStatement() throws SQLException {
        return this.getActiveMySQLConnection().createStatement();
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return this.getActiveMySQLConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return this.getActiveMySQLConnection().createStatement(resultSetType, resultSetConcurrency);
    }
    
    @Override
    public void dumpTestcaseQuery(final String query) {
        this.getActiveMySQLConnection().dumpTestcaseQuery(query);
    }
    
    @Override
    public Connection duplicate() throws SQLException {
        return this.getActiveMySQLConnection().duplicate();
    }
    
    @Override
    public ResultSetInternalMethods execSQL(final StatementImpl callingStatement, final String sql, final int maxRows, final Buffer packet, final int resultSetType, final int resultSetConcurrency, final boolean streamResults, final String catalog, final Field[] cachedMetadata, final boolean isBatch) throws SQLException {
        return this.getActiveMySQLConnection().execSQL(callingStatement, sql, maxRows, packet, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata, isBatch);
    }
    
    @Override
    public ResultSetInternalMethods execSQL(final StatementImpl callingStatement, final String sql, final int maxRows, final Buffer packet, final int resultSetType, final int resultSetConcurrency, final boolean streamResults, final String catalog, final Field[] cachedMetadata) throws SQLException {
        return this.getActiveMySQLConnection().execSQL(callingStatement, sql, maxRows, packet, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata);
    }
    
    @Override
    public String extractSqlFromPacket(final String possibleSqlQuery, final Buffer queryPacket, final int endOfQueryPacketPosition) throws SQLException {
        return this.getActiveMySQLConnection().extractSqlFromPacket(possibleSqlQuery, queryPacket, endOfQueryPacketPosition);
    }
    
    @Override
    public String exposeAsXml() throws SQLException {
        return this.getActiveMySQLConnection().exposeAsXml();
    }
    
    @Override
    public boolean getAllowLoadLocalInfile() {
        return this.getActiveMySQLConnection().getAllowLoadLocalInfile();
    }
    
    @Override
    public boolean getAllowMultiQueries() {
        return this.getActiveMySQLConnection().getAllowMultiQueries();
    }
    
    @Override
    public boolean getAllowNanAndInf() {
        return this.getActiveMySQLConnection().getAllowNanAndInf();
    }
    
    @Override
    public boolean getAllowUrlInLocalInfile() {
        return this.getActiveMySQLConnection().getAllowUrlInLocalInfile();
    }
    
    @Override
    public boolean getAlwaysSendSetIsolation() {
        return this.getActiveMySQLConnection().getAlwaysSendSetIsolation();
    }
    
    @Override
    public boolean getAutoClosePStmtStreams() {
        return this.getActiveMySQLConnection().getAutoClosePStmtStreams();
    }
    
    @Override
    public boolean getAutoDeserialize() {
        return this.getActiveMySQLConnection().getAutoDeserialize();
    }
    
    @Override
    public boolean getAutoGenerateTestcaseScript() {
        return this.getActiveMySQLConnection().getAutoGenerateTestcaseScript();
    }
    
    @Override
    public boolean getAutoReconnectForPools() {
        return this.getActiveMySQLConnection().getAutoReconnectForPools();
    }
    
    @Override
    public boolean getAutoSlowLog() {
        return this.getActiveMySQLConnection().getAutoSlowLog();
    }
    
    @Override
    public int getBlobSendChunkSize() {
        return this.getActiveMySQLConnection().getBlobSendChunkSize();
    }
    
    @Override
    public boolean getBlobsAreStrings() {
        return this.getActiveMySQLConnection().getBlobsAreStrings();
    }
    
    @Override
    public boolean getCacheCallableStatements() {
        return this.getActiveMySQLConnection().getCacheCallableStatements();
    }
    
    @Override
    public boolean getCacheCallableStmts() {
        return this.getActiveMySQLConnection().getCacheCallableStmts();
    }
    
    @Override
    public boolean getCachePrepStmts() {
        return this.getActiveMySQLConnection().getCachePrepStmts();
    }
    
    @Override
    public boolean getCachePreparedStatements() {
        return this.getActiveMySQLConnection().getCachePreparedStatements();
    }
    
    @Override
    public boolean getCacheResultSetMetadata() {
        return this.getActiveMySQLConnection().getCacheResultSetMetadata();
    }
    
    @Override
    public boolean getCacheServerConfiguration() {
        return this.getActiveMySQLConnection().getCacheServerConfiguration();
    }
    
    @Override
    public int getCallableStatementCacheSize() {
        return this.getActiveMySQLConnection().getCallableStatementCacheSize();
    }
    
    @Override
    public int getCallableStmtCacheSize() {
        return this.getActiveMySQLConnection().getCallableStmtCacheSize();
    }
    
    @Override
    public boolean getCapitalizeTypeNames() {
        return this.getActiveMySQLConnection().getCapitalizeTypeNames();
    }
    
    @Override
    public String getCharacterSetResults() {
        return this.getActiveMySQLConnection().getCharacterSetResults();
    }
    
    @Override
    public String getClientCertificateKeyStorePassword() {
        return this.getActiveMySQLConnection().getClientCertificateKeyStorePassword();
    }
    
    @Override
    public String getClientCertificateKeyStoreType() {
        return this.getActiveMySQLConnection().getClientCertificateKeyStoreType();
    }
    
    @Override
    public String getClientCertificateKeyStoreUrl() {
        return this.getActiveMySQLConnection().getClientCertificateKeyStoreUrl();
    }
    
    @Override
    public String getClientInfoProvider() {
        return this.getActiveMySQLConnection().getClientInfoProvider();
    }
    
    @Override
    public String getClobCharacterEncoding() {
        return this.getActiveMySQLConnection().getClobCharacterEncoding();
    }
    
    @Override
    public boolean getClobberStreamingResults() {
        return this.getActiveMySQLConnection().getClobberStreamingResults();
    }
    
    @Override
    public boolean getCompensateOnDuplicateKeyUpdateCounts() {
        return this.getActiveMySQLConnection().getCompensateOnDuplicateKeyUpdateCounts();
    }
    
    @Override
    public int getConnectTimeout() {
        return this.getActiveMySQLConnection().getConnectTimeout();
    }
    
    @Override
    public String getConnectionCollation() {
        return this.getActiveMySQLConnection().getConnectionCollation();
    }
    
    @Override
    public String getConnectionLifecycleInterceptors() {
        return this.getActiveMySQLConnection().getConnectionLifecycleInterceptors();
    }
    
    @Override
    public boolean getContinueBatchOnError() {
        return this.getActiveMySQLConnection().getContinueBatchOnError();
    }
    
    @Override
    public boolean getCreateDatabaseIfNotExist() {
        return this.getActiveMySQLConnection().getCreateDatabaseIfNotExist();
    }
    
    @Override
    public int getDefaultFetchSize() {
        return this.getActiveMySQLConnection().getDefaultFetchSize();
    }
    
    @Override
    public boolean getDontTrackOpenResources() {
        return this.getActiveMySQLConnection().getDontTrackOpenResources();
    }
    
    @Override
    public boolean getDumpMetadataOnColumnNotFound() {
        return this.getActiveMySQLConnection().getDumpMetadataOnColumnNotFound();
    }
    
    @Override
    public boolean getDumpQueriesOnException() {
        return this.getActiveMySQLConnection().getDumpQueriesOnException();
    }
    
    @Override
    public boolean getDynamicCalendars() {
        return this.getActiveMySQLConnection().getDynamicCalendars();
    }
    
    @Override
    public boolean getElideSetAutoCommits() {
        return this.getActiveMySQLConnection().getElideSetAutoCommits();
    }
    
    @Override
    public boolean getEmptyStringsConvertToZero() {
        return this.getActiveMySQLConnection().getEmptyStringsConvertToZero();
    }
    
    @Override
    public boolean getEmulateLocators() {
        return this.getActiveMySQLConnection().getEmulateLocators();
    }
    
    @Override
    public boolean getEmulateUnsupportedPstmts() {
        return this.getActiveMySQLConnection().getEmulateUnsupportedPstmts();
    }
    
    @Override
    public boolean getEnablePacketDebug() {
        return this.getActiveMySQLConnection().getEnablePacketDebug();
    }
    
    @Override
    public boolean getEnableQueryTimeouts() {
        return this.getActiveMySQLConnection().getEnableQueryTimeouts();
    }
    
    @Override
    public String getEncoding() {
        return this.getActiveMySQLConnection().getEncoding();
    }
    
    @Override
    public String getExceptionInterceptors() {
        return this.getActiveMySQLConnection().getExceptionInterceptors();
    }
    
    @Override
    public boolean getExplainSlowQueries() {
        return this.getActiveMySQLConnection().getExplainSlowQueries();
    }
    
    @Override
    public boolean getFailOverReadOnly() {
        return this.getActiveMySQLConnection().getFailOverReadOnly();
    }
    
    @Override
    public boolean getFunctionsNeverReturnBlobs() {
        return this.getActiveMySQLConnection().getFunctionsNeverReturnBlobs();
    }
    
    @Override
    public boolean getGatherPerfMetrics() {
        return this.getActiveMySQLConnection().getGatherPerfMetrics();
    }
    
    @Override
    public boolean getGatherPerformanceMetrics() {
        return this.getActiveMySQLConnection().getGatherPerformanceMetrics();
    }
    
    @Override
    public boolean getGenerateSimpleParameterMetadata() {
        return this.getActiveMySQLConnection().getGenerateSimpleParameterMetadata();
    }
    
    @Override
    public boolean getIgnoreNonTxTables() {
        return this.getActiveMySQLConnection().getIgnoreNonTxTables();
    }
    
    @Override
    public boolean getIncludeInnodbStatusInDeadlockExceptions() {
        return this.getActiveMySQLConnection().getIncludeInnodbStatusInDeadlockExceptions();
    }
    
    @Override
    public int getInitialTimeout() {
        return this.getActiveMySQLConnection().getInitialTimeout();
    }
    
    @Override
    public boolean getInteractiveClient() {
        return this.getActiveMySQLConnection().getInteractiveClient();
    }
    
    @Override
    public boolean getIsInteractiveClient() {
        return this.getActiveMySQLConnection().getIsInteractiveClient();
    }
    
    @Override
    public boolean getJdbcCompliantTruncation() {
        return this.getActiveMySQLConnection().getJdbcCompliantTruncation();
    }
    
    @Override
    public boolean getJdbcCompliantTruncationForReads() {
        return this.getActiveMySQLConnection().getJdbcCompliantTruncationForReads();
    }
    
    @Override
    public String getLargeRowSizeThreshold() {
        return this.getActiveMySQLConnection().getLargeRowSizeThreshold();
    }
    
    @Override
    public int getLoadBalanceBlacklistTimeout() {
        return this.getActiveMySQLConnection().getLoadBalanceBlacklistTimeout();
    }
    
    @Override
    public int getLoadBalancePingTimeout() {
        return this.getActiveMySQLConnection().getLoadBalancePingTimeout();
    }
    
    @Override
    public String getLoadBalanceStrategy() {
        return this.getActiveMySQLConnection().getLoadBalanceStrategy();
    }
    
    @Override
    public boolean getLoadBalanceValidateConnectionOnSwapServer() {
        return this.getActiveMySQLConnection().getLoadBalanceValidateConnectionOnSwapServer();
    }
    
    @Override
    public String getLocalSocketAddress() {
        return this.getActiveMySQLConnection().getLocalSocketAddress();
    }
    
    @Override
    public int getLocatorFetchBufferSize() {
        return this.getActiveMySQLConnection().getLocatorFetchBufferSize();
    }
    
    @Override
    public boolean getLogSlowQueries() {
        return this.getActiveMySQLConnection().getLogSlowQueries();
    }
    
    @Override
    public boolean getLogXaCommands() {
        return this.getActiveMySQLConnection().getLogXaCommands();
    }
    
    @Override
    public String getLogger() {
        return this.getActiveMySQLConnection().getLogger();
    }
    
    @Override
    public String getLoggerClassName() {
        return this.getActiveMySQLConnection().getLoggerClassName();
    }
    
    @Override
    public boolean getMaintainTimeStats() {
        return this.getActiveMySQLConnection().getMaintainTimeStats();
    }
    
    @Override
    public int getMaxAllowedPacket() {
        return this.getActiveMySQLConnection().getMaxAllowedPacket();
    }
    
    @Override
    public int getMaxQuerySizeToLog() {
        return this.getActiveMySQLConnection().getMaxQuerySizeToLog();
    }
    
    @Override
    public int getMaxReconnects() {
        return this.getActiveMySQLConnection().getMaxReconnects();
    }
    
    @Override
    public int getMaxRows() {
        return this.getActiveMySQLConnection().getMaxRows();
    }
    
    @Override
    public int getMetadataCacheSize() {
        return this.getActiveMySQLConnection().getMetadataCacheSize();
    }
    
    @Override
    public int getNetTimeoutForStreamingResults() {
        return this.getActiveMySQLConnection().getNetTimeoutForStreamingResults();
    }
    
    @Override
    public boolean getNoAccessToProcedureBodies() {
        return this.getActiveMySQLConnection().getNoAccessToProcedureBodies();
    }
    
    @Override
    public boolean getNoDatetimeStringSync() {
        return this.getActiveMySQLConnection().getNoDatetimeStringSync();
    }
    
    @Override
    public boolean getNoTimezoneConversionForTimeType() {
        return this.getActiveMySQLConnection().getNoTimezoneConversionForTimeType();
    }
    
    @Override
    public boolean getNoTimezoneConversionForDateType() {
        return this.getActiveMySQLConnection().getNoTimezoneConversionForDateType();
    }
    
    @Override
    public boolean getCacheDefaultTimezone() {
        return this.getActiveMySQLConnection().getCacheDefaultTimezone();
    }
    
    @Override
    public boolean getNullCatalogMeansCurrent() {
        return this.getActiveMySQLConnection().getNullCatalogMeansCurrent();
    }
    
    @Override
    public boolean getNullNamePatternMatchesAll() {
        return this.getActiveMySQLConnection().getNullNamePatternMatchesAll();
    }
    
    @Override
    public boolean getOverrideSupportsIntegrityEnhancementFacility() {
        return this.getActiveMySQLConnection().getOverrideSupportsIntegrityEnhancementFacility();
    }
    
    @Override
    public int getPacketDebugBufferSize() {
        return this.getActiveMySQLConnection().getPacketDebugBufferSize();
    }
    
    @Override
    public boolean getPadCharsWithSpace() {
        return this.getActiveMySQLConnection().getPadCharsWithSpace();
    }
    
    @Override
    public boolean getParanoid() {
        return this.getActiveMySQLConnection().getParanoid();
    }
    
    @Override
    public String getPasswordCharacterEncoding() {
        return this.getActiveMySQLConnection().getPasswordCharacterEncoding();
    }
    
    @Override
    public boolean getPedantic() {
        return this.getActiveMySQLConnection().getPedantic();
    }
    
    @Override
    public boolean getPinGlobalTxToPhysicalConnection() {
        return this.getActiveMySQLConnection().getPinGlobalTxToPhysicalConnection();
    }
    
    @Override
    public boolean getPopulateInsertRowWithDefaultValues() {
        return this.getActiveMySQLConnection().getPopulateInsertRowWithDefaultValues();
    }
    
    @Override
    public int getPrepStmtCacheSize() {
        return this.getActiveMySQLConnection().getPrepStmtCacheSize();
    }
    
    @Override
    public int getPrepStmtCacheSqlLimit() {
        return this.getActiveMySQLConnection().getPrepStmtCacheSqlLimit();
    }
    
    @Override
    public int getPreparedStatementCacheSize() {
        return this.getActiveMySQLConnection().getPreparedStatementCacheSize();
    }
    
    @Override
    public int getPreparedStatementCacheSqlLimit() {
        return this.getActiveMySQLConnection().getPreparedStatementCacheSqlLimit();
    }
    
    @Override
    public boolean getProcessEscapeCodesForPrepStmts() {
        return this.getActiveMySQLConnection().getProcessEscapeCodesForPrepStmts();
    }
    
    @Override
    public boolean getProfileSQL() {
        return this.getActiveMySQLConnection().getProfileSQL();
    }
    
    @Override
    public boolean getProfileSql() {
        return this.getActiveMySQLConnection().getProfileSql();
    }
    
    @Override
    public String getProfilerEventHandler() {
        return this.getActiveMySQLConnection().getProfilerEventHandler();
    }
    
    @Override
    public String getPropertiesTransform() {
        return this.getActiveMySQLConnection().getPropertiesTransform();
    }
    
    @Override
    public int getQueriesBeforeRetryMaster() {
        return this.getActiveMySQLConnection().getQueriesBeforeRetryMaster();
    }
    
    @Override
    public boolean getQueryTimeoutKillsConnection() {
        return this.getActiveMySQLConnection().getQueryTimeoutKillsConnection();
    }
    
    @Override
    public boolean getReconnectAtTxEnd() {
        return this.getActiveMySQLConnection().getReconnectAtTxEnd();
    }
    
    @Override
    public boolean getRelaxAutoCommit() {
        return this.getActiveMySQLConnection().getRelaxAutoCommit();
    }
    
    @Override
    public int getReportMetricsIntervalMillis() {
        return this.getActiveMySQLConnection().getReportMetricsIntervalMillis();
    }
    
    @Override
    public boolean getRequireSSL() {
        return this.getActiveMySQLConnection().getRequireSSL();
    }
    
    @Override
    public String getResourceId() {
        return this.getActiveMySQLConnection().getResourceId();
    }
    
    @Override
    public int getResultSetSizeThreshold() {
        return this.getActiveMySQLConnection().getResultSetSizeThreshold();
    }
    
    @Override
    public boolean getRetainStatementAfterResultSetClose() {
        return this.getActiveMySQLConnection().getRetainStatementAfterResultSetClose();
    }
    
    @Override
    public int getRetriesAllDown() {
        return this.getActiveMySQLConnection().getRetriesAllDown();
    }
    
    @Override
    public boolean getRewriteBatchedStatements() {
        return this.getActiveMySQLConnection().getRewriteBatchedStatements();
    }
    
    @Override
    public boolean getRollbackOnPooledClose() {
        return this.getActiveMySQLConnection().getRollbackOnPooledClose();
    }
    
    @Override
    public boolean getRoundRobinLoadBalance() {
        return this.getActiveMySQLConnection().getRoundRobinLoadBalance();
    }
    
    @Override
    public boolean getRunningCTS13() {
        return this.getActiveMySQLConnection().getRunningCTS13();
    }
    
    @Override
    public int getSecondsBeforeRetryMaster() {
        return this.getActiveMySQLConnection().getSecondsBeforeRetryMaster();
    }
    
    @Override
    public int getSelfDestructOnPingMaxOperations() {
        return this.getActiveMySQLConnection().getSelfDestructOnPingMaxOperations();
    }
    
    @Override
    public int getSelfDestructOnPingSecondsLifetime() {
        return this.getActiveMySQLConnection().getSelfDestructOnPingSecondsLifetime();
    }
    
    @Override
    public String getServerTimezone() {
        return this.getActiveMySQLConnection().getServerTimezone();
    }
    
    @Override
    public String getSessionVariables() {
        return this.getActiveMySQLConnection().getSessionVariables();
    }
    
    @Override
    public int getSlowQueryThresholdMillis() {
        return this.getActiveMySQLConnection().getSlowQueryThresholdMillis();
    }
    
    @Override
    public long getSlowQueryThresholdNanos() {
        return this.getActiveMySQLConnection().getSlowQueryThresholdNanos();
    }
    
    @Override
    public String getSocketFactory() {
        return this.getActiveMySQLConnection().getSocketFactory();
    }
    
    @Override
    public String getSocketFactoryClassName() {
        return this.getActiveMySQLConnection().getSocketFactoryClassName();
    }
    
    @Override
    public int getSocketTimeout() {
        return this.getActiveMySQLConnection().getSocketTimeout();
    }
    
    @Override
    public String getStatementInterceptors() {
        return this.getActiveMySQLConnection().getStatementInterceptors();
    }
    
    @Override
    public boolean getStrictFloatingPoint() {
        return this.getActiveMySQLConnection().getStrictFloatingPoint();
    }
    
    @Override
    public boolean getStrictUpdates() {
        return this.getActiveMySQLConnection().getStrictUpdates();
    }
    
    @Override
    public boolean getSupportLobLocator() {
        return this.getActiveMySQLConnection().getSupportLobLocator();
    }
    
    @Override
    public boolean getTcpKeepAlive() {
        return this.getActiveMySQLConnection().getTcpKeepAlive();
    }
    
    @Override
    public boolean getTcpNoDelay() {
        return this.getActiveMySQLConnection().getTcpNoDelay();
    }
    
    @Override
    public int getTcpRcvBuf() {
        return this.getActiveMySQLConnection().getTcpRcvBuf();
    }
    
    @Override
    public int getTcpSndBuf() {
        return this.getActiveMySQLConnection().getTcpSndBuf();
    }
    
    @Override
    public int getTcpTrafficClass() {
        return this.getActiveMySQLConnection().getTcpTrafficClass();
    }
    
    @Override
    public boolean getTinyInt1isBit() {
        return this.getActiveMySQLConnection().getTinyInt1isBit();
    }
    
    @Override
    public boolean getTraceProtocol() {
        return this.getActiveMySQLConnection().getTraceProtocol();
    }
    
    @Override
    public boolean getTransformedBitIsBoolean() {
        return this.getActiveMySQLConnection().getTransformedBitIsBoolean();
    }
    
    @Override
    public boolean getTreatUtilDateAsTimestamp() {
        return this.getActiveMySQLConnection().getTreatUtilDateAsTimestamp();
    }
    
    @Override
    public String getTrustCertificateKeyStorePassword() {
        return this.getActiveMySQLConnection().getTrustCertificateKeyStorePassword();
    }
    
    @Override
    public String getTrustCertificateKeyStoreType() {
        return this.getActiveMySQLConnection().getTrustCertificateKeyStoreType();
    }
    
    @Override
    public String getTrustCertificateKeyStoreUrl() {
        return this.getActiveMySQLConnection().getTrustCertificateKeyStoreUrl();
    }
    
    @Override
    public boolean getUltraDevHack() {
        return this.getActiveMySQLConnection().getUltraDevHack();
    }
    
    @Override
    public boolean getUseAffectedRows() {
        return this.getActiveMySQLConnection().getUseAffectedRows();
    }
    
    @Override
    public boolean getUseBlobToStoreUTF8OutsideBMP() {
        return this.getActiveMySQLConnection().getUseBlobToStoreUTF8OutsideBMP();
    }
    
    @Override
    public boolean getUseColumnNamesInFindColumn() {
        return this.getActiveMySQLConnection().getUseColumnNamesInFindColumn();
    }
    
    @Override
    public boolean getUseCompression() {
        return this.getActiveMySQLConnection().getUseCompression();
    }
    
    @Override
    public boolean getUseObChecksum() {
        return this.getActiveMySQLConnection().getUseObChecksum();
    }
    
    @Override
    public boolean getUseOceanBaseProtocolV20() {
        return this.getActiveMySQLConnection().getUseOceanBaseProtocolV20();
    }
    
    @Override
    public String getUseConfigs() {
        return this.getActiveMySQLConnection().getUseConfigs();
    }
    
    @Override
    public boolean getUseCursorFetch() {
        return this.getActiveMySQLConnection().getUseCursorFetch();
    }
    
    @Override
    public boolean getUseDirectRowUnpack() {
        return this.getActiveMySQLConnection().getUseDirectRowUnpack();
    }
    
    @Override
    public boolean getUseDynamicCharsetInfo() {
        return this.getActiveMySQLConnection().getUseDynamicCharsetInfo();
    }
    
    @Override
    public boolean getUseFastDateParsing() {
        return this.getActiveMySQLConnection().getUseFastDateParsing();
    }
    
    @Override
    public boolean getUseFastIntParsing() {
        return this.getActiveMySQLConnection().getUseFastIntParsing();
    }
    
    @Override
    public boolean getUseGmtMillisForDatetimes() {
        return this.getActiveMySQLConnection().getUseGmtMillisForDatetimes();
    }
    
    @Override
    public boolean getUseHostsInPrivileges() {
        return this.getActiveMySQLConnection().getUseHostsInPrivileges();
    }
    
    @Override
    public boolean getUseInformationSchema() {
        return this.getActiveMySQLConnection().getUseInformationSchema();
    }
    
    @Override
    public boolean getUseJDBCCompliantTimezoneShift() {
        return this.getActiveMySQLConnection().getUseJDBCCompliantTimezoneShift();
    }
    
    @Override
    public boolean getUseJvmCharsetConverters() {
        return this.getActiveMySQLConnection().getUseJvmCharsetConverters();
    }
    
    @Override
    public boolean getUseLegacyDatetimeCode() {
        return this.getActiveMySQLConnection().getUseLegacyDatetimeCode();
    }
    
    @Override
    public boolean getSendFractionalSeconds() {
        return this.getActiveMySQLConnection().getSendFractionalSeconds();
    }
    
    @Override
    public boolean getUseLocalSessionState() {
        return this.getActiveMySQLConnection().getUseLocalSessionState();
    }
    
    @Override
    public boolean getUseLocalTransactionState() {
        return this.getActiveMySQLConnection().getUseLocalTransactionState();
    }
    
    @Override
    public boolean getUseNanosForElapsedTime() {
        return this.getActiveMySQLConnection().getUseNanosForElapsedTime();
    }
    
    @Override
    public boolean getUseOldAliasMetadataBehavior() {
        return this.getActiveMySQLConnection().getUseOldAliasMetadataBehavior();
    }
    
    @Override
    public boolean getUseOldUTF8Behavior() {
        return this.getActiveMySQLConnection().getUseOldUTF8Behavior();
    }
    
    @Override
    public boolean getUseOnlyServerErrorMessages() {
        return this.getActiveMySQLConnection().getUseOnlyServerErrorMessages();
    }
    
    @Override
    public boolean getUseReadAheadInput() {
        return this.getActiveMySQLConnection().getUseReadAheadInput();
    }
    
    @Override
    public boolean getUseSSL() {
        return this.getActiveMySQLConnection().getUseSSL();
    }
    
    @Override
    public boolean getUseSSPSCompatibleTimezoneShift() {
        return this.getActiveMySQLConnection().getUseSSPSCompatibleTimezoneShift();
    }
    
    @Override
    public boolean getUseServerPrepStmts() {
        return this.getActiveMySQLConnection().getUseServerPrepStmts();
    }
    
    @Override
    public boolean getUseServerPreparedStmts() {
        return this.getActiveMySQLConnection().getUseServerPreparedStmts();
    }
    
    @Override
    public boolean getUseSqlStateCodes() {
        return this.getActiveMySQLConnection().getUseSqlStateCodes();
    }
    
    @Override
    public boolean getUseStreamLengthsInPrepStmts() {
        return this.getActiveMySQLConnection().getUseStreamLengthsInPrepStmts();
    }
    
    @Override
    public boolean getUseTimezone() {
        return this.getActiveMySQLConnection().getUseTimezone();
    }
    
    @Override
    public boolean getUseUltraDevWorkAround() {
        return this.getActiveMySQLConnection().getUseUltraDevWorkAround();
    }
    
    @Override
    public boolean getUseUnbufferedInput() {
        return this.getActiveMySQLConnection().getUseUnbufferedInput();
    }
    
    @Override
    public boolean getUseUnicode() {
        return this.getActiveMySQLConnection().getUseUnicode();
    }
    
    @Override
    public boolean getUseUsageAdvisor() {
        return this.getActiveMySQLConnection().getUseUsageAdvisor();
    }
    
    @Override
    public String getUtf8OutsideBmpExcludedColumnNamePattern() {
        return this.getActiveMySQLConnection().getUtf8OutsideBmpExcludedColumnNamePattern();
    }
    
    @Override
    public String getUtf8OutsideBmpIncludedColumnNamePattern() {
        return this.getActiveMySQLConnection().getUtf8OutsideBmpIncludedColumnNamePattern();
    }
    
    @Override
    public boolean getVerifyServerCertificate() {
        return this.getActiveMySQLConnection().getVerifyServerCertificate();
    }
    
    @Override
    public boolean getYearIsDateType() {
        return this.getActiveMySQLConnection().getYearIsDateType();
    }
    
    @Override
    public String getZeroDateTimeBehavior() {
        return this.getActiveMySQLConnection().getZeroDateTimeBehavior();
    }
    
    @Override
    public void setAllowLoadLocalInfile(final boolean property) {
        this.getActiveMySQLConnection().setAllowLoadLocalInfile(property);
    }
    
    @Override
    public void setAllowMultiQueries(final boolean property) {
        this.getActiveMySQLConnection().setAllowMultiQueries(property);
    }
    
    @Override
    public void setAllowNanAndInf(final boolean flag) {
        this.getActiveMySQLConnection().setAllowNanAndInf(flag);
    }
    
    @Override
    public void setAllowUrlInLocalInfile(final boolean flag) {
        this.getActiveMySQLConnection().setAllowUrlInLocalInfile(flag);
    }
    
    @Override
    public void setAlwaysSendSetIsolation(final boolean flag) {
        this.getActiveMySQLConnection().setAlwaysSendSetIsolation(flag);
    }
    
    @Override
    public void setAutoClosePStmtStreams(final boolean flag) {
        this.getActiveMySQLConnection().setAutoClosePStmtStreams(flag);
    }
    
    @Override
    public void setAutoDeserialize(final boolean flag) {
        this.getActiveMySQLConnection().setAutoDeserialize(flag);
    }
    
    @Override
    public void setAutoGenerateTestcaseScript(final boolean flag) {
        this.getActiveMySQLConnection().setAutoGenerateTestcaseScript(flag);
    }
    
    @Override
    public void setAutoReconnect(final boolean flag) {
        this.getActiveMySQLConnection().setAutoReconnect(flag);
    }
    
    @Override
    public void setAutoReconnectForConnectionPools(final boolean property) {
        this.getActiveMySQLConnection().setAutoReconnectForConnectionPools(property);
    }
    
    @Override
    public void setAutoReconnectForPools(final boolean flag) {
        this.getActiveMySQLConnection().setAutoReconnectForPools(flag);
    }
    
    @Override
    public void setAutoSlowLog(final boolean flag) {
        this.getActiveMySQLConnection().setAutoSlowLog(flag);
    }
    
    @Override
    public void setBlobSendChunkSize(final String value) throws SQLException {
        this.getActiveMySQLConnection().setBlobSendChunkSize(value);
    }
    
    @Override
    public void setBlobsAreStrings(final boolean flag) {
        this.getActiveMySQLConnection().setBlobsAreStrings(flag);
    }
    
    @Override
    public void setCacheCallableStatements(final boolean flag) {
        this.getActiveMySQLConnection().setCacheCallableStatements(flag);
    }
    
    @Override
    public void setCacheCallableStmts(final boolean flag) {
        this.getActiveMySQLConnection().setCacheCallableStmts(flag);
    }
    
    @Override
    public void setCachePrepStmts(final boolean flag) {
        this.getActiveMySQLConnection().setCachePrepStmts(flag);
    }
    
    @Override
    public void setCachePreparedStatements(final boolean flag) {
        this.getActiveMySQLConnection().setCachePreparedStatements(flag);
    }
    
    @Override
    public void setCacheResultSetMetadata(final boolean property) {
        this.getActiveMySQLConnection().setCacheResultSetMetadata(property);
    }
    
    @Override
    public void setCacheServerConfiguration(final boolean flag) {
        this.getActiveMySQLConnection().setCacheServerConfiguration(flag);
    }
    
    @Override
    public void setCallableStatementCacheSize(final int size) throws SQLException {
        this.getActiveMySQLConnection().setCallableStatementCacheSize(size);
    }
    
    @Override
    public void setCallableStmtCacheSize(final int cacheSize) throws SQLException {
        this.getActiveMySQLConnection().setCallableStmtCacheSize(cacheSize);
    }
    
    @Override
    public void setCapitalizeDBMDTypes(final boolean property) {
        this.getActiveMySQLConnection().setCapitalizeDBMDTypes(property);
    }
    
    @Override
    public void setCapitalizeTypeNames(final boolean flag) {
        this.getActiveMySQLConnection().setCapitalizeTypeNames(flag);
    }
    
    @Override
    public void setCharacterEncoding(final String encoding) {
        this.getActiveMySQLConnection().setCharacterEncoding(encoding);
    }
    
    @Override
    public void setCharacterSetResults(final String characterSet) {
        this.getActiveMySQLConnection().setCharacterSetResults(characterSet);
    }
    
    @Override
    public void setClientCertificateKeyStorePassword(final String value) {
        this.getActiveMySQLConnection().setClientCertificateKeyStorePassword(value);
    }
    
    @Override
    public void setClientCertificateKeyStoreType(final String value) {
        this.getActiveMySQLConnection().setClientCertificateKeyStoreType(value);
    }
    
    @Override
    public void setClientCertificateKeyStoreUrl(final String value) {
        this.getActiveMySQLConnection().setClientCertificateKeyStoreUrl(value);
    }
    
    @Override
    public void setClientInfoProvider(final String classname) {
        this.getActiveMySQLConnection().setClientInfoProvider(classname);
    }
    
    @Override
    public void setClobCharacterEncoding(final String encoding) {
        this.getActiveMySQLConnection().setClobCharacterEncoding(encoding);
    }
    
    @Override
    public void setClobberStreamingResults(final boolean flag) {
        this.getActiveMySQLConnection().setClobberStreamingResults(flag);
    }
    
    @Override
    public void setCompensateOnDuplicateKeyUpdateCounts(final boolean flag) {
        this.getActiveMySQLConnection().setCompensateOnDuplicateKeyUpdateCounts(flag);
    }
    
    @Override
    public void setConnectTimeout(final int timeoutMs) throws SQLException {
        this.getActiveMySQLConnection().setConnectTimeout(timeoutMs);
    }
    
    @Override
    public void setConnectionCollation(final String collation) {
        this.getActiveMySQLConnection().setConnectionCollation(collation);
    }
    
    @Override
    public void setConnectionLifecycleInterceptors(final String interceptors) {
        this.getActiveMySQLConnection().setConnectionLifecycleInterceptors(interceptors);
    }
    
    @Override
    public void setContinueBatchOnError(final boolean property) {
        this.getActiveMySQLConnection().setContinueBatchOnError(property);
    }
    
    @Override
    public void setCreateDatabaseIfNotExist(final boolean flag) {
        this.getActiveMySQLConnection().setCreateDatabaseIfNotExist(flag);
    }
    
    @Override
    public void setDefaultFetchSize(final int n) throws SQLException {
        this.getActiveMySQLConnection().setDefaultFetchSize(n);
    }
    
    @Override
    public void setDetectServerPreparedStmts(final boolean property) {
        this.getActiveMySQLConnection().setDetectServerPreparedStmts(property);
    }
    
    @Override
    public void setDontTrackOpenResources(final boolean flag) {
        this.getActiveMySQLConnection().setDontTrackOpenResources(flag);
    }
    
    @Override
    public void setDumpMetadataOnColumnNotFound(final boolean flag) {
        this.getActiveMySQLConnection().setDumpMetadataOnColumnNotFound(flag);
    }
    
    @Override
    public void setDumpQueriesOnException(final boolean flag) {
        this.getActiveMySQLConnection().setDumpQueriesOnException(flag);
    }
    
    @Override
    public void setDynamicCalendars(final boolean flag) {
        this.getActiveMySQLConnection().setDynamicCalendars(flag);
    }
    
    @Override
    public void setElideSetAutoCommits(final boolean flag) {
        this.getActiveMySQLConnection().setElideSetAutoCommits(flag);
    }
    
    @Override
    public void setEmptyStringsConvertToZero(final boolean flag) {
        this.getActiveMySQLConnection().setEmptyStringsConvertToZero(flag);
    }
    
    @Override
    public void setEmulateLocators(final boolean property) {
        this.getActiveMySQLConnection().setEmulateLocators(property);
    }
    
    @Override
    public void setEmulateUnsupportedPstmts(final boolean flag) {
        this.getActiveMySQLConnection().setEmulateUnsupportedPstmts(flag);
    }
    
    @Override
    public void setEnablePacketDebug(final boolean flag) {
        this.getActiveMySQLConnection().setEnablePacketDebug(flag);
    }
    
    @Override
    public void setEnableQueryTimeouts(final boolean flag) {
        this.getActiveMySQLConnection().setEnableQueryTimeouts(flag);
    }
    
    @Override
    public void setEncoding(final String property) {
        this.getActiveMySQLConnection().setEncoding(property);
    }
    
    @Override
    public void setExceptionInterceptors(final String exceptionInterceptors) {
        this.getActiveMySQLConnection().setExceptionInterceptors(exceptionInterceptors);
    }
    
    @Override
    public void setExplainSlowQueries(final boolean flag) {
        this.getActiveMySQLConnection().setExplainSlowQueries(flag);
    }
    
    @Override
    public void setFailOverReadOnly(final boolean flag) {
        this.getActiveMySQLConnection().setFailOverReadOnly(flag);
    }
    
    @Override
    public void setFunctionsNeverReturnBlobs(final boolean flag) {
        this.getActiveMySQLConnection().setFunctionsNeverReturnBlobs(flag);
    }
    
    @Override
    public void setGatherPerfMetrics(final boolean flag) {
        this.getActiveMySQLConnection().setGatherPerfMetrics(flag);
    }
    
    @Override
    public void setGatherPerformanceMetrics(final boolean flag) {
        this.getActiveMySQLConnection().setGatherPerformanceMetrics(flag);
    }
    
    @Override
    public void setGenerateSimpleParameterMetadata(final boolean flag) {
        this.getActiveMySQLConnection().setGenerateSimpleParameterMetadata(flag);
    }
    
    @Override
    public void setHoldResultsOpenOverStatementClose(final boolean flag) {
        this.getActiveMySQLConnection().setHoldResultsOpenOverStatementClose(flag);
    }
    
    @Override
    public void setIgnoreNonTxTables(final boolean property) {
        this.getActiveMySQLConnection().setIgnoreNonTxTables(property);
    }
    
    @Override
    public void setIncludeInnodbStatusInDeadlockExceptions(final boolean flag) {
        this.getActiveMySQLConnection().setIncludeInnodbStatusInDeadlockExceptions(flag);
    }
    
    @Override
    public void setInitialTimeout(final int property) throws SQLException {
        this.getActiveMySQLConnection().setInitialTimeout(property);
    }
    
    @Override
    public void setInteractiveClient(final boolean property) {
        this.getActiveMySQLConnection().setInteractiveClient(property);
    }
    
    @Override
    public void setIsInteractiveClient(final boolean property) {
        this.getActiveMySQLConnection().setIsInteractiveClient(property);
    }
    
    @Override
    public void setJdbcCompliantTruncation(final boolean flag) {
        this.getActiveMySQLConnection().setJdbcCompliantTruncation(flag);
    }
    
    @Override
    public void setJdbcCompliantTruncationForReads(final boolean jdbcCompliantTruncationForReads) {
        this.getActiveMySQLConnection().setJdbcCompliantTruncationForReads(jdbcCompliantTruncationForReads);
    }
    
    @Override
    public void setLargeRowSizeThreshold(final String value) throws SQLException {
        this.getActiveMySQLConnection().setLargeRowSizeThreshold(value);
    }
    
    @Override
    public void setLoadBalanceBlacklistTimeout(final int loadBalanceBlacklistTimeout) throws SQLException {
        this.getActiveMySQLConnection().setLoadBalanceBlacklistTimeout(loadBalanceBlacklistTimeout);
    }
    
    @Override
    public void setLoadBalancePingTimeout(final int loadBalancePingTimeout) throws SQLException {
        this.getActiveMySQLConnection().setLoadBalancePingTimeout(loadBalancePingTimeout);
    }
    
    @Override
    public void setLoadBalanceStrategy(final String strategy) {
        this.getActiveMySQLConnection().setLoadBalanceStrategy(strategy);
    }
    
    @Override
    public void setLoadBalanceValidateConnectionOnSwapServer(final boolean loadBalanceValidateConnectionOnSwapServer) {
        this.getActiveMySQLConnection().setLoadBalanceValidateConnectionOnSwapServer(loadBalanceValidateConnectionOnSwapServer);
    }
    
    @Override
    public void setLocalSocketAddress(final String address) {
        this.getActiveMySQLConnection().setLocalSocketAddress(address);
    }
    
    @Override
    public void setLocatorFetchBufferSize(final String value) throws SQLException {
        this.getActiveMySQLConnection().setLocatorFetchBufferSize(value);
    }
    
    @Override
    public void setLogSlowQueries(final boolean flag) {
        this.getActiveMySQLConnection().setLogSlowQueries(flag);
    }
    
    @Override
    public void setLogXaCommands(final boolean flag) {
        this.getActiveMySQLConnection().setLogXaCommands(flag);
    }
    
    @Override
    public void setLogger(final String property) {
        this.getActiveMySQLConnection().setLogger(property);
    }
    
    @Override
    public void setLoggerClassName(final String className) {
        this.getActiveMySQLConnection().setLoggerClassName(className);
    }
    
    @Override
    public void setMaintainTimeStats(final boolean flag) {
        this.getActiveMySQLConnection().setMaintainTimeStats(flag);
    }
    
    @Override
    public void setMaxQuerySizeToLog(final int sizeInBytes) throws SQLException {
        this.getActiveMySQLConnection().setMaxQuerySizeToLog(sizeInBytes);
    }
    
    @Override
    public void setMaxReconnects(final int property) throws SQLException {
        this.getActiveMySQLConnection().setMaxReconnects(property);
    }
    
    @Override
    public void setMaxRows(final int property) throws SQLException {
        this.getActiveMySQLConnection().setMaxRows(property);
    }
    
    @Override
    public void setMetadataCacheSize(final int value) throws SQLException {
        this.getActiveMySQLConnection().setMetadataCacheSize(value);
    }
    
    @Override
    public void setNetTimeoutForStreamingResults(final int value) throws SQLException {
        this.getActiveMySQLConnection().setNetTimeoutForStreamingResults(value);
    }
    
    @Override
    public void setNoAccessToProcedureBodies(final boolean flag) {
        this.getActiveMySQLConnection().setNoAccessToProcedureBodies(flag);
    }
    
    @Override
    public void setNoDatetimeStringSync(final boolean flag) {
        this.getActiveMySQLConnection().setNoDatetimeStringSync(flag);
    }
    
    @Override
    public void setNoTimezoneConversionForTimeType(final boolean flag) {
        this.getActiveMySQLConnection().setNoTimezoneConversionForTimeType(flag);
    }
    
    @Override
    public void setNoTimezoneConversionForDateType(final boolean flag) {
        this.getActiveMySQLConnection().setNoTimezoneConversionForDateType(flag);
    }
    
    @Override
    public void setCacheDefaultTimezone(final boolean flag) {
        this.getActiveMySQLConnection().setCacheDefaultTimezone(flag);
    }
    
    @Override
    public void setNullCatalogMeansCurrent(final boolean value) {
        this.getActiveMySQLConnection().setNullCatalogMeansCurrent(value);
    }
    
    @Override
    public void setNullNamePatternMatchesAll(final boolean value) {
        this.getActiveMySQLConnection().setNullNamePatternMatchesAll(value);
    }
    
    @Override
    public void setOverrideSupportsIntegrityEnhancementFacility(final boolean flag) {
        this.getActiveMySQLConnection().setOverrideSupportsIntegrityEnhancementFacility(flag);
    }
    
    @Override
    public void setPacketDebugBufferSize(final int size) throws SQLException {
        this.getActiveMySQLConnection().setPacketDebugBufferSize(size);
    }
    
    @Override
    public void setPadCharsWithSpace(final boolean flag) {
        this.getActiveMySQLConnection().setPadCharsWithSpace(flag);
    }
    
    @Override
    public void setParanoid(final boolean property) {
        this.getActiveMySQLConnection().setParanoid(property);
    }
    
    @Override
    public void setPasswordCharacterEncoding(final String characterSet) {
        this.getActiveMySQLConnection().setPasswordCharacterEncoding(characterSet);
    }
    
    @Override
    public void setPedantic(final boolean property) {
        this.getActiveMySQLConnection().setPedantic(property);
    }
    
    @Override
    public void setPinGlobalTxToPhysicalConnection(final boolean flag) {
        this.getActiveMySQLConnection().setPinGlobalTxToPhysicalConnection(flag);
    }
    
    @Override
    public void setPopulateInsertRowWithDefaultValues(final boolean flag) {
        this.getActiveMySQLConnection().setPopulateInsertRowWithDefaultValues(flag);
    }
    
    @Override
    public void setPrepStmtCacheSize(final int cacheSize) throws SQLException {
        this.getActiveMySQLConnection().setPrepStmtCacheSize(cacheSize);
    }
    
    @Override
    public void setPrepStmtCacheSqlLimit(final int sqlLimit) throws SQLException {
        this.getActiveMySQLConnection().setPrepStmtCacheSqlLimit(sqlLimit);
    }
    
    @Override
    public void setPreparedStatementCacheSize(final int cacheSize) throws SQLException {
        this.getActiveMySQLConnection().setPreparedStatementCacheSize(cacheSize);
    }
    
    @Override
    public void setPreparedStatementCacheSqlLimit(final int cacheSqlLimit) throws SQLException {
        this.getActiveMySQLConnection().setPreparedStatementCacheSqlLimit(cacheSqlLimit);
    }
    
    @Override
    public void setProcessEscapeCodesForPrepStmts(final boolean flag) {
        this.getActiveMySQLConnection().setProcessEscapeCodesForPrepStmts(flag);
    }
    
    @Override
    public void setProfileSQL(final boolean flag) {
        this.getActiveMySQLConnection().setProfileSQL(flag);
    }
    
    @Override
    public void setProfileSql(final boolean property) {
        this.getActiveMySQLConnection().setProfileSql(property);
    }
    
    @Override
    public void setProfilerEventHandler(final String handler) {
        this.getActiveMySQLConnection().setProfilerEventHandler(handler);
    }
    
    @Override
    public void setPropertiesTransform(final String value) {
        this.getActiveMySQLConnection().setPropertiesTransform(value);
    }
    
    @Override
    public void setQueriesBeforeRetryMaster(final int property) throws SQLException {
        this.getActiveMySQLConnection().setQueriesBeforeRetryMaster(property);
    }
    
    @Override
    public void setQueryTimeoutKillsConnection(final boolean queryTimeoutKillsConnection) {
        this.getActiveMySQLConnection().setQueryTimeoutKillsConnection(queryTimeoutKillsConnection);
    }
    
    @Override
    public void setReconnectAtTxEnd(final boolean property) {
        this.getActiveMySQLConnection().setReconnectAtTxEnd(property);
    }
    
    @Override
    public void setRelaxAutoCommit(final boolean property) {
        this.getActiveMySQLConnection().setRelaxAutoCommit(property);
    }
    
    @Override
    public void setReportMetricsIntervalMillis(final int millis) throws SQLException {
        this.getActiveMySQLConnection().setReportMetricsIntervalMillis(millis);
    }
    
    @Override
    public void setRequireSSL(final boolean property) {
        this.getActiveMySQLConnection().setRequireSSL(property);
    }
    
    @Override
    public void setResourceId(final String resourceId) {
        this.getActiveMySQLConnection().setResourceId(resourceId);
    }
    
    @Override
    public void setResultSetSizeThreshold(final int threshold) throws SQLException {
        this.getActiveMySQLConnection().setResultSetSizeThreshold(threshold);
    }
    
    @Override
    public void setRetainStatementAfterResultSetClose(final boolean flag) {
        this.getActiveMySQLConnection().setRetainStatementAfterResultSetClose(flag);
    }
    
    @Override
    public void setRetriesAllDown(final int retriesAllDown) throws SQLException {
        this.getActiveMySQLConnection().setRetriesAllDown(retriesAllDown);
    }
    
    @Override
    public void setRewriteBatchedStatements(final boolean flag) {
        this.getActiveMySQLConnection().setRewriteBatchedStatements(flag);
    }
    
    @Override
    public void setRollbackOnPooledClose(final boolean flag) {
        this.getActiveMySQLConnection().setRollbackOnPooledClose(flag);
    }
    
    @Override
    public void setRoundRobinLoadBalance(final boolean flag) {
        this.getActiveMySQLConnection().setRoundRobinLoadBalance(flag);
    }
    
    @Override
    public void setRunningCTS13(final boolean flag) {
        this.getActiveMySQLConnection().setRunningCTS13(flag);
    }
    
    @Override
    public void setSecondsBeforeRetryMaster(final int property) throws SQLException {
        this.getActiveMySQLConnection().setSecondsBeforeRetryMaster(property);
    }
    
    @Override
    public void setSelfDestructOnPingMaxOperations(final int maxOperations) throws SQLException {
        this.getActiveMySQLConnection().setSelfDestructOnPingMaxOperations(maxOperations);
    }
    
    @Override
    public void setSelfDestructOnPingSecondsLifetime(final int seconds) throws SQLException {
        this.getActiveMySQLConnection().setSelfDestructOnPingSecondsLifetime(seconds);
    }
    
    @Override
    public void setServerTimezone(final String property) {
        this.getActiveMySQLConnection().setServerTimezone(property);
    }
    
    @Override
    public void setSessionVariables(final String variables) {
        this.getActiveMySQLConnection().setSessionVariables(variables);
    }
    
    @Override
    public void setSlowQueryThresholdMillis(final int millis) throws SQLException {
        this.getActiveMySQLConnection().setSlowQueryThresholdMillis(millis);
    }
    
    @Override
    public void setSlowQueryThresholdNanos(final long nanos) throws SQLException {
        this.getActiveMySQLConnection().setSlowQueryThresholdNanos(nanos);
    }
    
    @Override
    public void setSocketFactory(final String name) {
        this.getActiveMySQLConnection().setSocketFactory(name);
    }
    
    @Override
    public void setSocketFactoryClassName(final String property) {
        this.getActiveMySQLConnection().setSocketFactoryClassName(property);
    }
    
    @Override
    public void setSocketTimeout(final int property) throws SQLException {
        this.getActiveMySQLConnection().setSocketTimeout(property);
    }
    
    @Override
    public void setStatementInterceptors(final String value) {
        this.getActiveMySQLConnection().setStatementInterceptors(value);
    }
    
    @Override
    public void setStrictFloatingPoint(final boolean property) {
        this.getActiveMySQLConnection().setStrictFloatingPoint(property);
    }
    
    @Override
    public void setStrictUpdates(final boolean property) {
        this.getActiveMySQLConnection().setStrictUpdates(property);
    }
    
    @Override
    public void setSupportLobLocator(final boolean property) {
        this.getActiveMySQLConnection().setSupportLobLocator(property);
    }
    
    @Override
    public void setTcpKeepAlive(final boolean flag) {
        this.getActiveMySQLConnection().setTcpKeepAlive(flag);
    }
    
    @Override
    public void setTcpNoDelay(final boolean flag) {
        this.getActiveMySQLConnection().setTcpNoDelay(flag);
    }
    
    @Override
    public void setTcpRcvBuf(final int bufSize) throws SQLException {
        this.getActiveMySQLConnection().setTcpRcvBuf(bufSize);
    }
    
    @Override
    public void setTcpSndBuf(final int bufSize) throws SQLException {
        this.getActiveMySQLConnection().setTcpSndBuf(bufSize);
    }
    
    @Override
    public void setTcpTrafficClass(final int classFlags) throws SQLException {
        this.getActiveMySQLConnection().setTcpTrafficClass(classFlags);
    }
    
    @Override
    public void setTinyInt1isBit(final boolean flag) {
        this.getActiveMySQLConnection().setTinyInt1isBit(flag);
    }
    
    @Override
    public void setTraceProtocol(final boolean flag) {
        this.getActiveMySQLConnection().setTraceProtocol(flag);
    }
    
    @Override
    public void setTransformedBitIsBoolean(final boolean flag) {
        this.getActiveMySQLConnection().setTransformedBitIsBoolean(flag);
    }
    
    @Override
    public void setTreatUtilDateAsTimestamp(final boolean flag) {
        this.getActiveMySQLConnection().setTreatUtilDateAsTimestamp(flag);
    }
    
    @Override
    public void setTrustCertificateKeyStorePassword(final String value) {
        this.getActiveMySQLConnection().setTrustCertificateKeyStorePassword(value);
    }
    
    @Override
    public void setTrustCertificateKeyStoreType(final String value) {
        this.getActiveMySQLConnection().setTrustCertificateKeyStoreType(value);
    }
    
    @Override
    public void setTrustCertificateKeyStoreUrl(final String value) {
        this.getActiveMySQLConnection().setTrustCertificateKeyStoreUrl(value);
    }
    
    @Override
    public void setUltraDevHack(final boolean flag) {
        this.getActiveMySQLConnection().setUltraDevHack(flag);
    }
    
    @Override
    public void setUseAffectedRows(final boolean flag) {
        this.getActiveMySQLConnection().setUseAffectedRows(flag);
    }
    
    @Override
    public void setUseBlobToStoreUTF8OutsideBMP(final boolean flag) {
        this.getActiveMySQLConnection().setUseBlobToStoreUTF8OutsideBMP(flag);
    }
    
    @Override
    public void setUseColumnNamesInFindColumn(final boolean flag) {
        this.getActiveMySQLConnection().setUseColumnNamesInFindColumn(flag);
    }
    
    @Override
    public void setUseCompression(final boolean property) {
        this.getActiveMySQLConnection().setUseCompression(property);
    }
    
    @Override
    public void setUseObChecksum(final boolean property) {
        this.getActiveMySQLConnection().setUseObChecksum(property);
    }
    
    @Override
    public void setUseOceanBaseProtocolV20(final boolean property) {
        this.getActiveMySQLConnection().setUseOceanBaseProtocolV20(property);
    }
    
    @Override
    public void setUseConfigs(final String configs) {
        this.getActiveMySQLConnection().setUseConfigs(configs);
    }
    
    @Override
    public void setUseCursorFetch(final boolean flag) {
        this.getActiveMySQLConnection().setUseCursorFetch(flag);
    }
    
    @Override
    public void setUseDirectRowUnpack(final boolean flag) {
        this.getActiveMySQLConnection().setUseDirectRowUnpack(flag);
    }
    
    @Override
    public void setUseDynamicCharsetInfo(final boolean flag) {
        this.getActiveMySQLConnection().setUseDynamicCharsetInfo(flag);
    }
    
    @Override
    public void setUseFastDateParsing(final boolean flag) {
        this.getActiveMySQLConnection().setUseFastDateParsing(flag);
    }
    
    @Override
    public void setUseFastIntParsing(final boolean flag) {
        this.getActiveMySQLConnection().setUseFastIntParsing(flag);
    }
    
    @Override
    public void setUseGmtMillisForDatetimes(final boolean flag) {
        this.getActiveMySQLConnection().setUseGmtMillisForDatetimes(flag);
    }
    
    @Override
    public void setUseHostsInPrivileges(final boolean property) {
        this.getActiveMySQLConnection().setUseHostsInPrivileges(property);
    }
    
    @Override
    public void setUseInformationSchema(final boolean flag) {
        this.getActiveMySQLConnection().setUseInformationSchema(flag);
    }
    
    @Override
    public void setUseJDBCCompliantTimezoneShift(final boolean flag) {
        this.getActiveMySQLConnection().setUseJDBCCompliantTimezoneShift(flag);
    }
    
    @Override
    public void setUseJvmCharsetConverters(final boolean flag) {
        this.getActiveMySQLConnection().setUseJvmCharsetConverters(flag);
    }
    
    @Override
    public void setUseLegacyDatetimeCode(final boolean flag) {
        this.getActiveMySQLConnection().setUseLegacyDatetimeCode(flag);
    }
    
    @Override
    public void setSendFractionalSeconds(final boolean flag) {
        this.getActiveMySQLConnection().setSendFractionalSeconds(flag);
    }
    
    @Override
    public void setUseLocalSessionState(final boolean flag) {
        this.getActiveMySQLConnection().setUseLocalSessionState(flag);
    }
    
    @Override
    public void setUseLocalTransactionState(final boolean flag) {
        this.getActiveMySQLConnection().setUseLocalTransactionState(flag);
    }
    
    @Override
    public void setUseNanosForElapsedTime(final boolean flag) {
        this.getActiveMySQLConnection().setUseNanosForElapsedTime(flag);
    }
    
    @Override
    public void setUseOldAliasMetadataBehavior(final boolean flag) {
        this.getActiveMySQLConnection().setUseOldAliasMetadataBehavior(flag);
    }
    
    @Override
    public void setUseOldUTF8Behavior(final boolean flag) {
        this.getActiveMySQLConnection().setUseOldUTF8Behavior(flag);
    }
    
    @Override
    public void setUseOnlyServerErrorMessages(final boolean flag) {
        this.getActiveMySQLConnection().setUseOnlyServerErrorMessages(flag);
    }
    
    @Override
    public void setUseReadAheadInput(final boolean flag) {
        this.getActiveMySQLConnection().setUseReadAheadInput(flag);
    }
    
    @Override
    public void setUseSSL(final boolean property) {
        this.getActiveMySQLConnection().setUseSSL(property);
    }
    
    @Override
    public void setUseSSPSCompatibleTimezoneShift(final boolean flag) {
        this.getActiveMySQLConnection().setUseSSPSCompatibleTimezoneShift(flag);
    }
    
    @Override
    public void setUseServerPrepStmts(final boolean flag) {
        this.getActiveMySQLConnection().setUseServerPrepStmts(flag);
    }
    
    @Override
    public void setUseServerPreparedStmts(final boolean flag) {
        this.getActiveMySQLConnection().setUseServerPreparedStmts(flag);
    }
    
    @Override
    public void setUseSqlStateCodes(final boolean flag) {
        this.getActiveMySQLConnection().setUseSqlStateCodes(flag);
    }
    
    @Override
    public void setUseStreamLengthsInPrepStmts(final boolean property) {
        this.getActiveMySQLConnection().setUseStreamLengthsInPrepStmts(property);
    }
    
    @Override
    public void setUseTimezone(final boolean property) {
        this.getActiveMySQLConnection().setUseTimezone(property);
    }
    
    @Override
    public void setUseUltraDevWorkAround(final boolean property) {
        this.getActiveMySQLConnection().setUseUltraDevWorkAround(property);
    }
    
    @Override
    public void setUseUnbufferedInput(final boolean flag) {
        this.getActiveMySQLConnection().setUseUnbufferedInput(flag);
    }
    
    @Override
    public void setUseUnicode(final boolean flag) {
        this.getActiveMySQLConnection().setUseUnicode(flag);
    }
    
    @Override
    public void setUseUsageAdvisor(final boolean useUsageAdvisorFlag) {
        this.getActiveMySQLConnection().setUseUsageAdvisor(useUsageAdvisorFlag);
    }
    
    @Override
    public void setUtf8OutsideBmpExcludedColumnNamePattern(final String regexPattern) {
        this.getActiveMySQLConnection().setUtf8OutsideBmpExcludedColumnNamePattern(regexPattern);
    }
    
    @Override
    public void setUtf8OutsideBmpIncludedColumnNamePattern(final String regexPattern) {
        this.getActiveMySQLConnection().setUtf8OutsideBmpIncludedColumnNamePattern(regexPattern);
    }
    
    @Override
    public void setVerifyServerCertificate(final boolean flag) {
        this.getActiveMySQLConnection().setVerifyServerCertificate(flag);
    }
    
    @Override
    public void setYearIsDateType(final boolean flag) {
        this.getActiveMySQLConnection().setYearIsDateType(flag);
    }
    
    @Override
    public void setZeroDateTimeBehavior(final String behavior) {
        this.getActiveMySQLConnection().setZeroDateTimeBehavior(behavior);
    }
    
    @Override
    public boolean useUnbufferedInput() {
        return this.getActiveMySQLConnection().useUnbufferedInput();
    }
    
    @Override
    public StringBuilder generateConnectionCommentBlock(final StringBuilder buf) {
        return this.getActiveMySQLConnection().generateConnectionCommentBlock(buf);
    }
    
    @Override
    public int getActiveStatementCount() {
        return this.getActiveMySQLConnection().getActiveStatementCount();
    }
    
    @Override
    public boolean getAutoCommit() throws SQLException {
        return this.getActiveMySQLConnection().getAutoCommit();
    }
    
    @Override
    public int getAutoIncrementIncrement() {
        return this.getActiveMySQLConnection().getAutoIncrementIncrement();
    }
    
    @Override
    public CachedResultSetMetaData getCachedMetaData(final String sql) {
        return this.getActiveMySQLConnection().getCachedMetaData(sql);
    }
    
    @Override
    public Calendar getCalendarInstanceForSessionOrNew() {
        return this.getActiveMySQLConnection().getCalendarInstanceForSessionOrNew();
    }
    
    @Override
    public Timer getCancelTimer() {
        return this.getActiveMySQLConnection().getCancelTimer();
    }
    
    @Override
    public String getCatalog() throws SQLException {
        return this.getActiveMySQLConnection().getCatalog();
    }
    
    @Override
    public String getCharacterSetMetadata() {
        return this.getActiveMySQLConnection().getCharacterSetMetadata();
    }
    
    @Override
    public SingleByteCharsetConverter getCharsetConverter(final String javaEncodingName) throws SQLException {
        return this.getActiveMySQLConnection().getCharsetConverter(javaEncodingName);
    }
    
    @Deprecated
    @Override
    public String getCharsetNameForIndex(final int charsetIndex) throws SQLException {
        return this.getEncodingForIndex(charsetIndex);
    }
    
    @Override
    public String getEncodingForIndex(final int collationIndex) throws SQLException {
        return this.getActiveMySQLConnection().getEncodingForIndex(collationIndex);
    }
    
    @Override
    public TimeZone getDefaultTimeZone() {
        return this.getActiveMySQLConnection().getDefaultTimeZone();
    }
    
    @Override
    public String getErrorMessageEncoding() {
        return this.getActiveMySQLConnection().getErrorMessageEncoding();
    }
    
    @Override
    public ExceptionInterceptor getExceptionInterceptor() {
        return this.getActiveMySQLConnection().getExceptionInterceptor();
    }
    
    @Override
    public int getHoldability() throws SQLException {
        return this.getActiveMySQLConnection().getHoldability();
    }
    
    @Override
    public String getHost() {
        return this.getActiveMySQLConnection().getHost();
    }
    
    @Override
    public String getHostPortPair() {
        return this.getActiveMySQLConnection().getHostPortPair();
    }
    
    @Override
    public long getId() {
        return this.getActiveMySQLConnection().getId();
    }
    
    @Override
    public long getIdleFor() {
        return this.getActiveMySQLConnection().getIdleFor();
    }
    
    @Override
    public MysqlIO getIO() throws SQLException {
        return this.getActiveMySQLConnection().getIO();
    }
    
    @Override
    public boolean isAlive() {
        if (this.thisAsProxy == null) {
            return false;
        }
        final MySQLConnection conn = this.getActiveMySQLConnection();
        return conn != null && conn.isAlive();
    }
    
    @Deprecated
    @Override
    public MySQLConnection getLoadBalanceSafeProxy() {
        return this.getMultiHostSafeProxy();
    }
    
    @Override
    public MySQLConnection getMultiHostSafeProxy() {
        return this.getThisAsProxy().getProxy();
    }
    
    @Override
    public Log getLog() throws SQLException {
        return this.getActiveMySQLConnection().getLog();
    }
    
    @Override
    public int getMaxBytesPerChar(final String javaCharsetName) throws SQLException {
        return this.getActiveMySQLConnection().getMaxBytesPerChar(javaCharsetName);
    }
    
    @Override
    public int getMaxBytesPerChar(final Integer charsetIndex, final String javaCharsetName) throws SQLException {
        return this.getActiveMySQLConnection().getMaxBytesPerChar(charsetIndex, javaCharsetName);
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return this.getActiveMySQLConnection().getMetaData();
    }
    
    @Override
    public Statement getMetadataSafeStatement() throws SQLException {
        return this.getActiveMySQLConnection().getMetadataSafeStatement();
    }
    
    @Override
    public int getNetBufferLength() {
        return this.getActiveMySQLConnection().getNetBufferLength();
    }
    
    @Override
    public Properties getProperties() {
        return this.getActiveMySQLConnection().getProperties();
    }
    
    @Override
    public boolean getRequiresEscapingEncoder() {
        return this.getActiveMySQLConnection().getRequiresEscapingEncoder();
    }
    
    @Deprecated
    @Override
    public String getServerCharacterEncoding() {
        return this.getServerCharset();
    }
    
    @Override
    public String getServerCharset() {
        return this.getActiveMySQLConnection().getServerCharset();
    }
    
    @Override
    public int getServerMajorVersion() {
        return this.getActiveMySQLConnection().getServerMajorVersion();
    }
    
    @Override
    public int getServerMinorVersion() {
        return this.getActiveMySQLConnection().getServerMinorVersion();
    }
    
    @Override
    public int getServerSubMinorVersion() {
        return this.getActiveMySQLConnection().getServerSubMinorVersion();
    }
    
    @Override
    public TimeZone getServerTimezoneTZ() {
        return this.getActiveMySQLConnection().getServerTimezoneTZ();
    }
    
    @Override
    public String getServerVariable(final String variableName) {
        return this.getActiveMySQLConnection().getServerVariable(variableName);
    }
    
    @Override
    public String getServerVersion() {
        return this.getActiveMySQLConnection().getServerVersion();
    }
    
    @Override
    public Calendar getSessionLockedCalendar() {
        return this.getActiveMySQLConnection().getSessionLockedCalendar();
    }
    
    @Override
    public String getStatementComment() {
        return this.getActiveMySQLConnection().getStatementComment();
    }
    
    @Override
    public List<StatementInterceptorV2> getStatementInterceptorsInstances() {
        return this.getActiveMySQLConnection().getStatementInterceptorsInstances();
    }
    
    @Override
    public int getTransactionIsolation() throws SQLException {
        return this.getActiveMySQLConnection().getTransactionIsolation();
    }
    
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return this.getActiveMySQLConnection().getTypeMap();
    }
    
    @Override
    public String getURL() {
        return this.getActiveMySQLConnection().getURL();
    }
    
    @Override
    public String getUser() {
        return this.getActiveMySQLConnection().getUser();
    }
    
    @Override
    public Calendar getUtcCalendar() {
        return this.getActiveMySQLConnection().getUtcCalendar();
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.getActiveMySQLConnection().getWarnings();
    }
    
    @Override
    public boolean hasSameProperties(final Connection c) {
        return this.getActiveMySQLConnection().hasSameProperties(c);
    }
    
    @Deprecated
    @Override
    public boolean hasTriedMaster() {
        return this.getActiveMySQLConnection().hasTriedMaster();
    }
    
    @Override
    public void incrementNumberOfPreparedExecutes() {
        this.getActiveMySQLConnection().incrementNumberOfPreparedExecutes();
    }
    
    @Override
    public void incrementNumberOfPrepares() {
        this.getActiveMySQLConnection().incrementNumberOfPrepares();
    }
    
    @Override
    public void incrementNumberOfResultSetsCreated() {
        this.getActiveMySQLConnection().incrementNumberOfResultSetsCreated();
    }
    
    @Override
    public void initializeExtension(final Extension ex) throws SQLException {
        this.getActiveMySQLConnection().initializeExtension(ex);
    }
    
    @Override
    public void initializeResultsMetadataFromCache(final String sql, final CachedResultSetMetaData cachedMetaData, final ResultSetInternalMethods resultSet) throws SQLException {
        this.getActiveMySQLConnection().initializeResultsMetadataFromCache(sql, cachedMetaData, resultSet);
    }
    
    @Override
    public void initializeSafeStatementInterceptors() throws SQLException {
        this.getActiveMySQLConnection().initializeSafeStatementInterceptors();
    }
    
    @Override
    public boolean isAbonormallyLongQuery(final long millisOrNanos) {
        return this.getActiveMySQLConnection().isAbonormallyLongQuery(millisOrNanos);
    }
    
    @Override
    public boolean isClientTzUTC() {
        return this.getActiveMySQLConnection().isClientTzUTC();
    }
    
    @Override
    public boolean isCursorFetchEnabled() throws SQLException {
        return this.getActiveMySQLConnection().isCursorFetchEnabled();
    }
    
    @Override
    public boolean isInGlobalTx() {
        return this.getActiveMySQLConnection().isInGlobalTx();
    }
    
    @Override
    public boolean isMasterConnection() {
        return this.getThisAsProxy().isMasterConnection();
    }
    
    @Override
    public boolean isNoBackslashEscapesSet() {
        return this.getActiveMySQLConnection().isNoBackslashEscapesSet();
    }
    
    @Override
    public boolean isReadInfoMsgEnabled() {
        return this.getActiveMySQLConnection().isReadInfoMsgEnabled();
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        return this.getActiveMySQLConnection().isReadOnly();
    }
    
    @Override
    public boolean isReadOnly(final boolean useSessionStatus) throws SQLException {
        return this.getActiveMySQLConnection().isReadOnly(useSessionStatus);
    }
    
    @Override
    public boolean isRunningOnJDK13() {
        return this.getActiveMySQLConnection().isRunningOnJDK13();
    }
    
    @Override
    public boolean isSameResource(final Connection otherConnection) {
        return this.getActiveMySQLConnection().isSameResource(otherConnection);
    }
    
    @Override
    public boolean isServerTzUTC() {
        return this.getActiveMySQLConnection().isServerTzUTC();
    }
    
    @Override
    public boolean lowerCaseTableNames() {
        return this.getActiveMySQLConnection().lowerCaseTableNames();
    }
    
    @Override
    public String nativeSQL(final String sql) throws SQLException {
        return this.getActiveMySQLConnection().nativeSQL(sql);
    }
    
    @Override
    public boolean parserKnowsUnicode() {
        return this.getActiveMySQLConnection().parserKnowsUnicode();
    }
    
    @Override
    public void ping() throws SQLException {
        this.getActiveMySQLConnection().ping();
    }
    
    @Override
    public void pingInternal(final boolean checkForClosedConnection, final int timeoutMillis) throws SQLException {
        this.getActiveMySQLConnection().pingInternal(checkForClosedConnection, timeoutMillis);
    }
    
    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return this.getActiveMySQLConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return this.getActiveMySQLConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        return this.getActiveMySQLConnection().prepareCall(sql);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return this.getActiveMySQLConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return this.getActiveMySQLConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGenKeyIndex) throws SQLException {
        return this.getActiveMySQLConnection().prepareStatement(sql, autoGenKeyIndex);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] autoGenKeyIndexes) throws SQLException {
        return this.getActiveMySQLConnection().prepareStatement(sql, autoGenKeyIndexes);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] autoGenKeyColNames) throws SQLException {
        return this.getActiveMySQLConnection().prepareStatement(sql, autoGenKeyColNames);
    }
    
    @Override
    public Clob createClob() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Blob createBlob() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public NClob createNClob() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public boolean isValid(final int timeout) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
        throw new SQLClientInfoException();
    }
    
    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException {
        throw new SQLClientInfoException();
    }
    
    @Override
    public String getClientInfo(final String name) throws SQLException {
        throw SQLError.createSQLException("not support", null);
    }
    
    @Override
    public Properties getClientInfo() throws SQLException {
        throw SQLError.createSQLException("not support", null);
    }
    
    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return this.getActiveMySQLConnection().prepareStatement(sql);
    }
    
    @Override
    public void realClose(final boolean calledExplicitly, final boolean issueRollback, final boolean skipLocalTeardown, final Throwable reason) throws SQLException {
        this.getActiveMySQLConnection().realClose(calledExplicitly, issueRollback, skipLocalTeardown, reason);
    }
    
    @Override
    public void recachePreparedStatement(final ServerPreparedStatement pstmt) throws SQLException {
        this.getActiveMySQLConnection().recachePreparedStatement(pstmt);
    }
    
    @Override
    public void decachePreparedStatement(final ServerPreparedStatement pstmt) throws SQLException {
        this.getActiveMySQLConnection().decachePreparedStatement(pstmt);
    }
    
    @Override
    public void registerQueryExecutionTime(final long queryTimeMs) {
        this.getActiveMySQLConnection().registerQueryExecutionTime(queryTimeMs);
    }
    
    @Override
    public void registerStatement(final com.alipay.oceanbase.jdbc.Statement stmt) {
        this.getActiveMySQLConnection().registerStatement(stmt);
    }
    
    @Override
    public void releaseSavepoint(final Savepoint arg0) throws SQLException {
        this.getActiveMySQLConnection().releaseSavepoint(arg0);
    }
    
    @Override
    public void reportNumberOfTablesAccessed(final int numTablesAccessed) {
        this.getActiveMySQLConnection().reportNumberOfTablesAccessed(numTablesAccessed);
    }
    
    @Override
    public void reportQueryTime(final long millisOrNanos) {
        this.getActiveMySQLConnection().reportQueryTime(millisOrNanos);
    }
    
    @Override
    public void resetServerState() throws SQLException {
        this.getActiveMySQLConnection().resetServerState();
    }
    
    @Override
    public void rollback() throws SQLException {
        this.getActiveMySQLConnection().rollback();
    }
    
    @Override
    public void rollback(final Savepoint savepoint) throws SQLException {
        this.getActiveMySQLConnection().rollback(savepoint);
    }
    
    @Override
    public PreparedStatement serverPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return this.getActiveMySQLConnection().serverPrepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    @Override
    public PreparedStatement serverPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return this.getActiveMySQLConnection().serverPrepareStatement(sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public PreparedStatement serverPrepareStatement(final String sql, final int autoGenKeyIndex) throws SQLException {
        return this.getActiveMySQLConnection().serverPrepareStatement(sql, autoGenKeyIndex);
    }
    
    @Override
    public PreparedStatement serverPrepareStatement(final String sql, final int[] autoGenKeyIndexes) throws SQLException {
        return this.getActiveMySQLConnection().serverPrepareStatement(sql, autoGenKeyIndexes);
    }
    
    @Override
    public PreparedStatement serverPrepareStatement(final String sql, final String[] autoGenKeyColNames) throws SQLException {
        return this.getActiveMySQLConnection().serverPrepareStatement(sql, autoGenKeyColNames);
    }
    
    @Override
    public PreparedStatement serverPrepareStatement(final String sql) throws SQLException {
        return this.getActiveMySQLConnection().serverPrepareStatement(sql);
    }
    
    @Override
    public boolean serverSupportsConvertFn() throws SQLException {
        return this.getActiveMySQLConnection().serverSupportsConvertFn();
    }
    
    @Override
    public void setAutoCommit(final boolean autoCommitFlag) throws SQLException {
        this.getActiveMySQLConnection().setAutoCommit(autoCommitFlag);
    }
    
    @Override
    public void setCatalog(final String catalog) throws SQLException {
        this.getActiveMySQLConnection().setCatalog(catalog);
    }
    
    @Override
    public void setFailedOver(final boolean flag) {
        this.getActiveMySQLConnection().setFailedOver(flag);
    }
    
    @Override
    public void setHoldability(final int arg0) throws SQLException {
        this.getActiveMySQLConnection().setHoldability(arg0);
    }
    
    @Override
    public void setInGlobalTx(final boolean flag) {
        this.getActiveMySQLConnection().setInGlobalTx(flag);
    }
    
    @Deprecated
    @Override
    public void setPreferSlaveDuringFailover(final boolean flag) {
        this.getActiveMySQLConnection().setPreferSlaveDuringFailover(flag);
    }
    
    @Override
    public void setProxy(final MySQLConnection proxy) {
        this.getThisAsProxy().setProxy(proxy);
    }
    
    @Override
    public void setReadInfoMsgEnabled(final boolean flag) {
        this.getActiveMySQLConnection().setReadInfoMsgEnabled(flag);
    }
    
    @Override
    public void setReadOnly(final boolean readOnlyFlag) throws SQLException {
        this.getActiveMySQLConnection().setReadOnly(readOnlyFlag);
    }
    
    @Override
    public void setReadOnlyInternal(final boolean readOnlyFlag) throws SQLException {
        this.getActiveMySQLConnection().setReadOnlyInternal(readOnlyFlag);
    }
    
    @Override
    public Savepoint setSavepoint() throws SQLException {
        return this.getActiveMySQLConnection().setSavepoint();
    }
    
    @Override
    public Savepoint setSavepoint(final String name) throws SQLException {
        return this.getActiveMySQLConnection().setSavepoint(name);
    }
    
    @Override
    public void setStatementComment(final String comment) {
        this.getActiveMySQLConnection().setStatementComment(comment);
    }
    
    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        this.getActiveMySQLConnection().setTransactionIsolation(level);
    }
    
    @Override
    public void shutdownServer() throws SQLException {
        this.getActiveMySQLConnection().shutdownServer();
    }
    
    @Override
    public boolean storesLowerCaseTableName() {
        return this.getActiveMySQLConnection().storesLowerCaseTableName();
    }
    
    @Override
    public boolean supportsIsolationLevel() {
        return this.getActiveMySQLConnection().supportsIsolationLevel();
    }
    
    @Override
    public boolean supportsQuotedIdentifiers() {
        return this.getActiveMySQLConnection().supportsQuotedIdentifiers();
    }
    
    @Override
    public boolean supportsTransactions() {
        return this.getActiveMySQLConnection().supportsTransactions();
    }
    
    @Override
    public void throwConnectionClosedException() throws SQLException {
        this.getActiveMySQLConnection().throwConnectionClosedException();
    }
    
    @Override
    public void transactionBegun() throws SQLException {
        this.getActiveMySQLConnection().transactionBegun();
    }
    
    @Override
    public void transactionCompleted() throws SQLException {
        this.getActiveMySQLConnection().transactionCompleted();
    }
    
    @Override
    public void unregisterStatement(final com.alipay.oceanbase.jdbc.Statement stmt) {
        this.getActiveMySQLConnection().unregisterStatement(stmt);
    }
    
    @Override
    public void unSafeStatementInterceptors() throws SQLException {
        this.getActiveMySQLConnection().unSafeStatementInterceptors();
    }
    
    @Override
    public boolean useAnsiQuotedIdentifiers() {
        return this.getActiveMySQLConnection().useAnsiQuotedIdentifiers();
    }
    
    @Override
    public boolean versionMeetsMinimum(final int major, final int minor, final int subminor) throws SQLException {
        return this.getActiveMySQLConnection().versionMeetsMinimum(major, minor, subminor);
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return this.getThisAsProxy().isClosed;
    }
    
    @Override
    public boolean getHoldResultsOpenOverStatementClose() {
        return this.getActiveMySQLConnection().getHoldResultsOpenOverStatementClose();
    }
    
    @Override
    public String getLoadBalanceConnectionGroup() {
        return this.getActiveMySQLConnection().getLoadBalanceConnectionGroup();
    }
    
    @Override
    public boolean getLoadBalanceEnableJMX() {
        return this.getActiveMySQLConnection().getLoadBalanceEnableJMX();
    }
    
    @Override
    public String getLoadBalanceExceptionChecker() {
        return this.getActiveMySQLConnection().getLoadBalanceExceptionChecker();
    }
    
    @Override
    public String getLoadBalanceSQLExceptionSubclassFailover() {
        return this.getActiveMySQLConnection().getLoadBalanceSQLExceptionSubclassFailover();
    }
    
    @Override
    public String getLoadBalanceSQLStateFailover() {
        return this.getActiveMySQLConnection().getLoadBalanceSQLStateFailover();
    }
    
    @Override
    public void setLoadBalanceConnectionGroup(final String loadBalanceConnectionGroup) {
        this.getActiveMySQLConnection().setLoadBalanceConnectionGroup(loadBalanceConnectionGroup);
    }
    
    @Override
    public void setLoadBalanceEnableJMX(final boolean loadBalanceEnableJMX) {
        this.getActiveMySQLConnection().setLoadBalanceEnableJMX(loadBalanceEnableJMX);
    }
    
    @Override
    public void setLoadBalanceExceptionChecker(final String loadBalanceExceptionChecker) {
        this.getActiveMySQLConnection().setLoadBalanceExceptionChecker(loadBalanceExceptionChecker);
    }
    
    @Override
    public void setLoadBalanceSQLExceptionSubclassFailover(final String loadBalanceSQLExceptionSubclassFailover) {
        this.getActiveMySQLConnection().setLoadBalanceSQLExceptionSubclassFailover(loadBalanceSQLExceptionSubclassFailover);
    }
    
    @Override
    public void setLoadBalanceSQLStateFailover(final String loadBalanceSQLStateFailover) {
        this.getActiveMySQLConnection().setLoadBalanceSQLStateFailover(loadBalanceSQLStateFailover);
    }
    
    @Override
    public void setLoadBalanceHostRemovalGracePeriod(final int loadBalanceHostRemovalGracePeriod) throws SQLException {
        this.getActiveMySQLConnection().setLoadBalanceHostRemovalGracePeriod(loadBalanceHostRemovalGracePeriod);
    }
    
    @Override
    public int getLoadBalanceHostRemovalGracePeriod() {
        return this.getActiveMySQLConnection().getLoadBalanceHostRemovalGracePeriod();
    }
    
    @Override
    public boolean isProxySet() {
        return this.getActiveMySQLConnection().isProxySet();
    }
    
    @Override
    public String getLoadBalanceAutoCommitStatementRegex() {
        return this.getActiveMySQLConnection().getLoadBalanceAutoCommitStatementRegex();
    }
    
    @Override
    public int getLoadBalanceAutoCommitStatementThreshold() {
        return this.getActiveMySQLConnection().getLoadBalanceAutoCommitStatementThreshold();
    }
    
    @Override
    public void setLoadBalanceAutoCommitStatementRegex(final String loadBalanceAutoCommitStatementRegex) {
        this.getActiveMySQLConnection().setLoadBalanceAutoCommitStatementRegex(loadBalanceAutoCommitStatementRegex);
    }
    
    @Override
    public void setLoadBalanceAutoCommitStatementThreshold(final int loadBalanceAutoCommitStatementThreshold) throws SQLException {
        this.getActiveMySQLConnection().setLoadBalanceAutoCommitStatementThreshold(loadBalanceAutoCommitStatementThreshold);
    }
    
    @Override
    public boolean getIncludeThreadDumpInDeadlockExceptions() {
        return this.getActiveMySQLConnection().getIncludeThreadDumpInDeadlockExceptions();
    }
    
    @Override
    public void setIncludeThreadDumpInDeadlockExceptions(final boolean flag) {
        this.getActiveMySQLConnection().setIncludeThreadDumpInDeadlockExceptions(flag);
    }
    
    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        this.getActiveMySQLConnection().setTypeMap(map);
    }
    
    @Override
    public boolean getIncludeThreadNamesAsStatementComment() {
        return this.getActiveMySQLConnection().getIncludeThreadNamesAsStatementComment();
    }
    
    @Override
    public void setIncludeThreadNamesAsStatementComment(final boolean flag) {
        this.getActiveMySQLConnection().setIncludeThreadNamesAsStatementComment(flag);
    }
    
    @Override
    public boolean isServerLocal() throws SQLException {
        return this.getActiveMySQLConnection().isServerLocal();
    }
    
    @Override
    public void setAuthenticationPlugins(final String authenticationPlugins) {
        this.getActiveMySQLConnection().setAuthenticationPlugins(authenticationPlugins);
    }
    
    @Override
    public String getAuthenticationPlugins() {
        return this.getActiveMySQLConnection().getAuthenticationPlugins();
    }
    
    @Override
    public void setDisabledAuthenticationPlugins(final String disabledAuthenticationPlugins) {
        this.getActiveMySQLConnection().setDisabledAuthenticationPlugins(disabledAuthenticationPlugins);
    }
    
    @Override
    public String getDisabledAuthenticationPlugins() {
        return this.getActiveMySQLConnection().getDisabledAuthenticationPlugins();
    }
    
    @Override
    public void setDefaultAuthenticationPlugin(final String defaultAuthenticationPlugin) {
        this.getActiveMySQLConnection().setDefaultAuthenticationPlugin(defaultAuthenticationPlugin);
    }
    
    @Override
    public String getDefaultAuthenticationPlugin() {
        return this.getActiveMySQLConnection().getDefaultAuthenticationPlugin();
    }
    
    @Override
    public void setParseInfoCacheFactory(final String factoryClassname) {
        this.getActiveMySQLConnection().setParseInfoCacheFactory(factoryClassname);
    }
    
    @Override
    public String getParseInfoCacheFactory() {
        return this.getActiveMySQLConnection().getParseInfoCacheFactory();
    }
    
    @Override
    public void setSchema(final String schema) throws SQLException {
        this.getActiveMySQLConnection().setSchema(schema);
    }
    
    @Override
    public String getSchema() throws SQLException {
        return this.getActiveMySQLConnection().getSchema();
    }
    
    @Override
    public void abort(final Executor executor) throws SQLException {
        this.getActiveMySQLConnection().abort(executor);
    }
    
    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        this.getActiveMySQLConnection().setNetworkTimeout(executor, milliseconds);
    }
    
    @Override
    public int getNetworkTimeout() throws SQLException {
        return this.getActiveMySQLConnection().getNetworkTimeout();
    }
    
    @Override
    public void setServerConfigCacheFactory(final String factoryClassname) {
        this.getActiveMySQLConnection().setServerConfigCacheFactory(factoryClassname);
    }
    
    @Override
    public String getServerConfigCacheFactory() {
        return this.getActiveMySQLConnection().getServerConfigCacheFactory();
    }
    
    @Override
    public void setDisconnectOnExpiredPasswords(final boolean disconnectOnExpiredPasswords) {
        this.getActiveMySQLConnection().setDisconnectOnExpiredPasswords(disconnectOnExpiredPasswords);
    }
    
    @Override
    public boolean getDisconnectOnExpiredPasswords() {
        return this.getActiveMySQLConnection().getDisconnectOnExpiredPasswords();
    }
    
    @Override
    public void setGetProceduresReturnsFunctions(final boolean getProcedureReturnsFunctions) {
        this.getActiveMySQLConnection().setGetProceduresReturnsFunctions(getProcedureReturnsFunctions);
    }
    
    @Override
    public boolean getGetProceduresReturnsFunctions() {
        return this.getActiveMySQLConnection().getGetProceduresReturnsFunctions();
    }
    
    @Override
    public Object getConnectionMutex() {
        return this.getActiveMySQLConnection().getConnectionMutex();
    }
    
    @Override
    public String getConnectionAttributes() throws SQLException {
        return this.getActiveMySQLConnection().getConnectionAttributes();
    }
    
    @Override
    public boolean getAllowMasterDownConnections() {
        return this.getActiveMySQLConnection().getAllowMasterDownConnections();
    }
    
    @Override
    public void setAllowMasterDownConnections(final boolean connectIfMasterDown) {
        this.getActiveMySQLConnection().setAllowMasterDownConnections(connectIfMasterDown);
    }
    
    @Override
    public boolean getAllowSlaveDownConnections() {
        return this.getActiveMySQLConnection().getAllowSlaveDownConnections();
    }
    
    @Override
    public void setAllowSlaveDownConnections(final boolean connectIfSlaveDown) {
        this.getActiveMySQLConnection().setAllowSlaveDownConnections(connectIfSlaveDown);
    }
    
    @Override
    public boolean getReadFromMasterWhenNoSlaves() {
        return this.getActiveMySQLConnection().getReadFromMasterWhenNoSlaves();
    }
    
    @Override
    public void setReadFromMasterWhenNoSlaves(final boolean useMasterIfSlavesDown) {
        this.getActiveMySQLConnection().setReadFromMasterWhenNoSlaves(useMasterIfSlavesDown);
    }
    
    @Override
    public boolean getReplicationEnableJMX() {
        return this.getActiveMySQLConnection().getReplicationEnableJMX();
    }
    
    @Override
    public void setReplicationEnableJMX(final boolean replicationEnableJMX) {
        this.getActiveMySQLConnection().setReplicationEnableJMX(replicationEnableJMX);
    }
    
    @Override
    public void setDetectCustomCollations(final boolean detectCustomCollations) {
        this.getActiveMySQLConnection().setDetectCustomCollations(detectCustomCollations);
    }
    
    @Override
    public boolean getDetectCustomCollations() {
        return this.getActiveMySQLConnection().getDetectCustomCollations();
    }
    
    @Override
    public int getSessionMaxRows() {
        return this.getActiveMySQLConnection().getSessionMaxRows();
    }
    
    @Override
    public void setSessionMaxRows(final int max) throws SQLException {
        this.getActiveMySQLConnection().setSessionMaxRows(max);
    }
    
    @Override
    public ProfilerEventHandler getProfilerEventHandlerInstance() {
        return this.getActiveMySQLConnection().getProfilerEventHandlerInstance();
    }
    
    @Override
    public void setProfilerEventHandlerInstance(final ProfilerEventHandler h) {
        this.getActiveMySQLConnection().setProfilerEventHandlerInstance(h);
    }
    
    @Override
    public String getServerRSAPublicKeyFile() {
        return this.getActiveMySQLConnection().getServerRSAPublicKeyFile();
    }
    
    @Override
    public void setServerRSAPublicKeyFile(final String serverRSAPublicKeyFile) throws SQLException {
        this.getActiveMySQLConnection().setServerRSAPublicKeyFile(serverRSAPublicKeyFile);
    }
    
    @Override
    public boolean getAllowPublicKeyRetrieval() {
        return this.getActiveMySQLConnection().getAllowPublicKeyRetrieval();
    }
    
    @Override
    public void setAllowPublicKeyRetrieval(final boolean allowPublicKeyRetrieval) throws SQLException {
        this.getActiveMySQLConnection().setAllowPublicKeyRetrieval(allowPublicKeyRetrieval);
    }
    
    @Override
    public void setDontCheckOnDuplicateKeyUpdateInSQL(final boolean dontCheckOnDuplicateKeyUpdateInSQL) {
        this.getActiveMySQLConnection().setDontCheckOnDuplicateKeyUpdateInSQL(dontCheckOnDuplicateKeyUpdateInSQL);
    }
    
    @Override
    public boolean getDontCheckOnDuplicateKeyUpdateInSQL() {
        return this.getActiveMySQLConnection().getDontCheckOnDuplicateKeyUpdateInSQL();
    }
    
    @Override
    public void setSocksProxyHost(final String socksProxyHost) {
        this.getActiveMySQLConnection().setSocksProxyHost(socksProxyHost);
    }
    
    @Override
    public String getSocksProxyHost() {
        return this.getActiveMySQLConnection().getSocksProxyHost();
    }
    
    @Override
    public void setSocksProxyPort(final int socksProxyPort) throws SQLException {
        this.getActiveMySQLConnection().setSocksProxyPort(socksProxyPort);
    }
    
    @Override
    public int getSocksProxyPort() {
        return this.getActiveMySQLConnection().getSocksProxyPort();
    }
    
    @Override
    public boolean getReadOnlyPropagatesToServer() {
        return this.getActiveMySQLConnection().getReadOnlyPropagatesToServer();
    }
    
    @Override
    public void setReadOnlyPropagatesToServer(final boolean flag) {
        this.getActiveMySQLConnection().setReadOnlyPropagatesToServer(flag);
    }
    
    @Override
    public String getEnabledSSLCipherSuites() {
        return this.getActiveMySQLConnection().getEnabledSSLCipherSuites();
    }
    
    @Override
    public void setEnabledSSLCipherSuites(final String cipherSuites) {
        this.getActiveMySQLConnection().setEnabledSSLCipherSuites(cipherSuites);
    }
    
    @Override
    public boolean getEnableEscapeProcessing() {
        return this.getActiveMySQLConnection().getEnableEscapeProcessing();
    }
    
    @Override
    public boolean getAllowAlwaysSendParamTypes() {
        return this.getActiveMySQLConnection().getAllowAlwaysSendParamTypes();
    }
    
    @Override
    public void setAllowAlwaysSendParamTypes(final boolean flag) {
        this.getActiveMySQLConnection().setAllowAlwaysSendParamTypes(flag);
    }
    
    @Override
    public boolean getUseFormatExceptionMessage() {
        return this.getActiveMySQLConnection().getUseFormatExceptionMessage();
    }
    
    @Override
    public void setUseFormatExceptionMessage(final boolean flag) {
        this.getActiveMySQLConnection().setUseFormatExceptionMessage(flag);
    }
    
    @Override
    public boolean getUseServerPsStmtChecksum() {
        return this.getActiveMySQLConnection().getUseServerPsStmtChecksum();
    }
    
    @Override
    public void setUseServerPsStmtChecksum(final boolean flag) {
        this.getActiveMySQLConnection().setUseServerPsStmtChecksum(flag);
    }
    
    @Override
    public void setUseSqlStringCache(final boolean flag) {
        this.getActiveMySQLConnection().setUseSqlStringCache(flag);
    }
    
    @Override
    public boolean getUseSqlStringCache() {
        return this.getActiveMySQLConnection().getUseSqlStringCache();
    }
    
    @Override
    public int getComplexDataCacheSize() {
        return this.getActiveMySQLConnection().getComplexDataCacheSize();
    }
    
    @Override
    public void setComplexDataCacheSize(final int value) throws SQLException {
        this.getActiveMySQLConnection().setComplexDataCacheSize(value);
    }
    
    @Override
    public boolean getCacheComplexData() {
        return this.getActiveMySQLConnection().getCacheComplexData();
    }
    
    @Override
    public void setCacheComplexData(final boolean flag) {
        this.getActiveMySQLConnection().setCacheComplexData(flag);
    }
    
    @Override
    public void setEnableEscapeProcessing(final boolean flag) {
        this.getActiveMySQLConnection().setEnableEscapeProcessing(flag);
    }
    
    @Override
    public boolean isUseSSLExplicit() {
        return this.getActiveMySQLConnection().isUseSSLExplicit();
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }
    
    @Override
    public String getSessionTimeZone() {
        return this.getServerTimezone();
    }
    
    @Override
    public boolean isOracleMode() throws SQLException {
        return ((ObConnection)this.getActiveMySQLConnection()).isOracleMode();
    }
    
    @Override
    public ComplexDataType getComplexDataType(final String typeName) throws SQLException {
        return ((ObConnection)this.getActiveMySQLConnection()).getComplexDataType(typeName);
    }
    
    @Override
    public ComplexDataType getComplexDataTypeFromCache(final String typeName) {
        return ((ObConnection)this.getActiveMySQLConnection()).getComplexDataTypeFromCache(typeName);
    }
    
    @Override
    public ComplexDataType getComplexDataTypeFromRemote(final String typeName) throws SQLException {
        return ((ObConnection)this.getActiveMySQLConnection()).getComplexDataTypeFromRemote(typeName);
    }
    
    @Override
    public void recacheComplexDataType(final ComplexDataType type) {
        ((ObConnection)this.getActiveMySQLConnection()).recacheComplexDataType(type);
    }
    
    @Override
    public long getObGroupDatasourceId() {
        return ((ObConnection)this.getActiveMySQLConnection()).getObGroupDatasourceId();
    }
    
    @Override
    public void setObGroupDatasourceId(final long obGroupDatasourceId) {
        ((ObConnection)this.getActiveMySQLConnection()).setObGroupDatasourceId(obGroupDatasourceId);
    }
    
    @Override
    public long getLocalConnectionId() {
        return ((ObConnection)this.getActiveMySQLConnection()).getLocalConnectionId();
    }
}
