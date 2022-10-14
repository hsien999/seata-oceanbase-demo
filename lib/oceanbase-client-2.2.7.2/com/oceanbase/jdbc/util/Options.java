// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.util;

import java.util.Objects;
import java.lang.reflect.Field;
import java.sql.DriverManager;
import java.util.Properties;

public class Options implements Cloneable
{
    public static final int MIN_VALUE__MAX_IDLE_TIME = 60;
    public String user;
    public String password;
    public boolean trustServerCertificate;
    public String serverSslCert;
    public String trustStore;
    public String trustStoreType;
    public String keyStoreType;
    public String trustStorePassword;
    public String keyStore;
    public String keyStorePassword;
    public String keyPassword;
    public String enabledSslProtocolSuites;
    public boolean useFractionalSeconds;
    public boolean pinGlobalTxToPhysicalConnection;
    public String socketFactory;
    public int connectTimeout;
    public String pipe;
    public String localSocket;
    public String sharedMemory;
    public boolean tcpNoDelay;
    public boolean tcpKeepAlive;
    public Integer tcpRcvBuf;
    public Integer tcpSndBuf;
    public boolean tcpAbortiveClose;
    public String localSocketAddress;
    public Integer socketTimeout;
    public boolean allowMultiQueries;
    public boolean trackSchema;
    public boolean rewriteBatchedStatements;
    public boolean useCompression;
    public boolean interactiveClient;
    public String passwordCharacterEncoding;
    public boolean blankTableNameMeta;
    public String credentialType;
    public Boolean useSsl;
    public String enabledSslCipherSuites;
    public String sessionVariables;
    public boolean tinyInt1isBit;
    public boolean yearIsDateType;
    public boolean createDatabaseIfNotExist;
    public String serverTimezone;
    public boolean nullCatalogMeansCurrent;
    public boolean dumpQueriesOnException;
    public boolean useOldAliasMetadataBehavior;
    public boolean useMysqlMetadata;
    public boolean allowLocalInfile;
    public boolean cachePrepStmts;
    public int prepStmtCacheSize;
    public int prepStmtCacheSqlLimit;
    public boolean useLegacyDatetimeCode;
    public boolean useAffectedRows;
    public boolean maximizeMysqlCompatibility;
    public boolean useServerPrepStmts;
    public boolean continueBatchOnError;
    public boolean jdbcCompliantTruncation;
    public boolean cacheCallableStmts;
    public int callableStmtCacheSize;
    public String connectionAttributes;
    public Boolean useBatchMultiSend;
    public int useBatchMultiSendNumber;
    public Boolean usePipelineAuth;
    public boolean enablePacketDebug;
    public boolean useBulkStmts;
    public boolean disableSslHostnameVerification;
    public boolean autocommit;
    public boolean includeInnodbStatusInDeadlockExceptions;
    public boolean includeThreadDumpInDeadlockExceptions;
    public String servicePrincipalName;
    public int defaultFetchSize;
    public Properties nonMappedOptions;
    public String tlsSocketType;
    public boolean log;
    public boolean profileSql;
    public int maxQuerySizeToLog;
    public Long slowQueryThresholdNanos;
    public boolean assureReadOnly;
    public boolean autoReconnect;
    public boolean failOnReadOnly;
    public int retriesAllDown;
    public int validConnectionTimeout;
    public int loadBalanceBlacklistTimeout;
    public int failoverLoopRetries;
    public boolean allowMasterDownConnection;
    public String galeraAllowedState;
    public boolean pool;
    public String poolName;
    public int maxPoolSize;
    public Integer minPoolSize;
    public int maxIdleTime;
    public boolean staticGlobal;
    public boolean registerJmxPool;
    public int poolValidMinDelay;
    public boolean useResetConnection;
    public boolean useReadAheadInput;
    public String serverRsaPublicKeyFile;
    public boolean allowPublicKeyRetrieval;
    public boolean supportLobLocator;
    public boolean useObChecksum;
    public boolean useOceanBaseProtocolV20;
    public boolean useFormatExceptionMessage;
    public boolean allowSendParamTypes;
    public int complexDataCacheSize;
    public boolean cacheComplexData;
    public boolean useSqlStringCache;
    public boolean useServerPsStmtChecksum;
    public String characterEncoding;
    public boolean useCursorFetch;
    public boolean supportNameBinding;
    public String socksProxyHost;
    public int socksProxyPort;
    public boolean connectProxy;
    public boolean usePieceData;
    public int pieceLength;
    public boolean useOraclePrepareExecute;
    public boolean autoDeserialize;
    public int maxBatchTotalParamsNum;
    
    public Options() {
        this.useFractionalSeconds = true;
        this.connectTimeout = ((DriverManager.getLoginTimeout() > 0) ? (DriverManager.getLoginTimeout() * 1000) : 30000);
        this.tcpNoDelay = true;
        this.tcpKeepAlive = true;
        this.trackSchema = true;
        this.useSsl = null;
        this.tinyInt1isBit = true;
        this.yearIsDateType = true;
        this.nullCatalogMeansCurrent = true;
        this.allowLocalInfile = false;
        this.cachePrepStmts = false;
        this.prepStmtCacheSize = 250;
        this.prepStmtCacheSqlLimit = 2048;
        this.useLegacyDatetimeCode = false;
        this.continueBatchOnError = true;
        this.jdbcCompliantTruncation = true;
        this.cacheCallableStmts = true;
        this.callableStmtCacheSize = 150;
        this.useBatchMultiSendNumber = 100;
        this.autocommit = true;
        this.nonMappedOptions = new Properties();
        this.maxQuerySizeToLog = 1024;
        this.assureReadOnly = true;
        this.retriesAllDown = 120;
        this.loadBalanceBlacklistTimeout = 50;
        this.failoverLoopRetries = 120;
        this.maxPoolSize = 8;
        this.maxIdleTime = 600;
        this.registerJmxPool = true;
        this.poolValidMinDelay = 1000;
        this.useReadAheadInput = true;
        this.supportLobLocator = true;
        this.useObChecksum = true;
        this.useOceanBaseProtocolV20 = true;
        this.useFormatExceptionMessage = false;
        this.allowSendParamTypes = false;
        this.complexDataCacheSize = 50;
        this.cacheComplexData = true;
        this.useSqlStringCache = false;
        this.useServerPsStmtChecksum = true;
        this.characterEncoding = "utf8";
        this.useCursorFetch = false;
        this.supportNameBinding = true;
        this.socksProxyPort = 1080;
        this.connectProxy = false;
        this.usePieceData = false;
        this.pieceLength = 1048576;
        this.useOraclePrepareExecute = false;
        this.autoDeserialize = false;
        this.maxBatchTotalParamsNum = 30000;
    }
    
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        final String newLine = System.getProperty("line.separator");
        result.append(this.getClass().getName());
        result.append(" Options {");
        result.append(newLine);
        final Field[] declaredFields;
        final Field[] fields = declaredFields = this.getClass().getDeclaredFields();
        for (final Field field : declaredFields) {
            result.append("  ");
            try {
                result.append(field.getName());
                result.append(": ");
                result.append(field.get(this));
            }
            catch (IllegalAccessException ex) {}
            result.append(newLine);
        }
        result.append("}");
        return result.toString();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final Options opt = (Options)obj;
        if (this.trustServerCertificate != opt.trustServerCertificate) {
            return false;
        }
        if (this.useFractionalSeconds != opt.useFractionalSeconds) {
            return false;
        }
        if (this.pinGlobalTxToPhysicalConnection != opt.pinGlobalTxToPhysicalConnection) {
            return false;
        }
        if (this.tcpNoDelay != opt.tcpNoDelay) {
            return false;
        }
        if (this.tcpKeepAlive != opt.tcpKeepAlive) {
            return false;
        }
        if (this.tcpAbortiveClose != opt.tcpAbortiveClose) {
            return false;
        }
        if (this.blankTableNameMeta != opt.blankTableNameMeta) {
            return false;
        }
        if (this.allowMultiQueries != opt.allowMultiQueries) {
            return false;
        }
        if (this.rewriteBatchedStatements != opt.rewriteBatchedStatements) {
            return false;
        }
        if (this.useCompression != opt.useCompression) {
            return false;
        }
        if (this.interactiveClient != opt.interactiveClient) {
            return false;
        }
        if (this.useSsl != opt.useSsl) {
            return false;
        }
        if (this.tinyInt1isBit != opt.tinyInt1isBit) {
            return false;
        }
        if (this.yearIsDateType != opt.yearIsDateType) {
            return false;
        }
        if (this.createDatabaseIfNotExist != opt.createDatabaseIfNotExist) {
            return false;
        }
        if (this.nullCatalogMeansCurrent != opt.nullCatalogMeansCurrent) {
            return false;
        }
        if (this.dumpQueriesOnException != opt.dumpQueriesOnException) {
            return false;
        }
        if (this.useOldAliasMetadataBehavior != opt.useOldAliasMetadataBehavior) {
            return false;
        }
        if (this.allowLocalInfile != opt.allowLocalInfile) {
            return false;
        }
        if (this.cachePrepStmts != opt.cachePrepStmts) {
            return false;
        }
        if (this.useLegacyDatetimeCode != opt.useLegacyDatetimeCode) {
            return false;
        }
        if (this.useAffectedRows != opt.useAffectedRows) {
            return false;
        }
        if (this.maximizeMysqlCompatibility != opt.maximizeMysqlCompatibility) {
            return false;
        }
        if (this.useServerPrepStmts != opt.useServerPrepStmts) {
            return false;
        }
        if (this.continueBatchOnError != opt.continueBatchOnError) {
            return false;
        }
        if (this.jdbcCompliantTruncation != opt.jdbcCompliantTruncation) {
            return false;
        }
        if (this.cacheCallableStmts != opt.cacheCallableStmts) {
            return false;
        }
        if (this.useBatchMultiSendNumber != opt.useBatchMultiSendNumber) {
            return false;
        }
        if (this.enablePacketDebug != opt.enablePacketDebug) {
            return false;
        }
        if (this.includeInnodbStatusInDeadlockExceptions != opt.includeInnodbStatusInDeadlockExceptions) {
            return false;
        }
        if (this.includeThreadDumpInDeadlockExceptions != opt.includeThreadDumpInDeadlockExceptions) {
            return false;
        }
        if (this.defaultFetchSize != opt.defaultFetchSize) {
            return false;
        }
        if (this.useBulkStmts != opt.useBulkStmts) {
            return false;
        }
        if (this.disableSslHostnameVerification != opt.disableSslHostnameVerification) {
            return false;
        }
        if (this.log != opt.log) {
            return false;
        }
        if (this.profileSql != opt.profileSql) {
            return false;
        }
        if (this.assureReadOnly != opt.assureReadOnly) {
            return false;
        }
        if (this.autoReconnect != opt.autoReconnect) {
            return false;
        }
        if (this.failOnReadOnly != opt.failOnReadOnly) {
            return false;
        }
        if (this.allowMasterDownConnection != opt.allowMasterDownConnection) {
            return false;
        }
        if (this.retriesAllDown != opt.retriesAllDown) {
            return false;
        }
        if (this.validConnectionTimeout != opt.validConnectionTimeout) {
            return false;
        }
        if (this.loadBalanceBlacklistTimeout != opt.loadBalanceBlacklistTimeout) {
            return false;
        }
        if (this.failoverLoopRetries != opt.failoverLoopRetries) {
            return false;
        }
        if (this.pool != opt.pool) {
            return false;
        }
        if (this.staticGlobal != opt.staticGlobal) {
            return false;
        }
        if (this.registerJmxPool != opt.registerJmxPool) {
            return false;
        }
        if (this.useResetConnection != opt.useResetConnection) {
            return false;
        }
        if (this.useReadAheadInput != opt.useReadAheadInput) {
            return false;
        }
        if (this.maxPoolSize != opt.maxPoolSize) {
            return false;
        }
        if (this.maxIdleTime != opt.maxIdleTime) {
            return false;
        }
        if (this.poolValidMinDelay != opt.poolValidMinDelay) {
            return false;
        }
        if (!Objects.equals(this.user, opt.user)) {
            return false;
        }
        if (!Objects.equals(this.password, opt.password)) {
            return false;
        }
        if (!Objects.equals(this.serverSslCert, opt.serverSslCert)) {
            return false;
        }
        if (!Objects.equals(this.trustStore, opt.trustStore)) {
            return false;
        }
        if (!Objects.equals(this.trustStorePassword, opt.trustStorePassword)) {
            return false;
        }
        if (!Objects.equals(this.keyStore, opt.keyStore)) {
            return false;
        }
        if (!Objects.equals(this.keyStorePassword, opt.keyStorePassword)) {
            return false;
        }
        if (!Objects.equals(this.keyPassword, opt.keyPassword)) {
            return false;
        }
        if (this.enabledSslProtocolSuites != null) {
            if (!this.enabledSslProtocolSuites.equals(opt.enabledSslProtocolSuites)) {
                return false;
            }
        }
        else if (opt.enabledSslProtocolSuites != null) {
            return false;
        }
        if (!Objects.equals(this.socketFactory, opt.socketFactory)) {
            return false;
        }
        if (this.connectTimeout != opt.connectTimeout) {
            return false;
        }
        if (!Objects.equals(this.pipe, opt.pipe)) {
            return false;
        }
        if (!Objects.equals(this.localSocket, opt.localSocket)) {
            return false;
        }
        if (!Objects.equals(this.sharedMemory, opt.sharedMemory)) {
            return false;
        }
        if (!Objects.equals(this.tcpRcvBuf, opt.tcpRcvBuf)) {
            return false;
        }
        if (!Objects.equals(this.tcpSndBuf, opt.tcpSndBuf)) {
            return false;
        }
        if (!Objects.equals(this.localSocketAddress, opt.localSocketAddress)) {
            return false;
        }
        if (!Objects.equals(this.socketTimeout, opt.socketTimeout)) {
            return false;
        }
        if (this.passwordCharacterEncoding != null) {
            if (!this.passwordCharacterEncoding.equals(opt.passwordCharacterEncoding)) {
                return false;
            }
        }
        else if (opt.passwordCharacterEncoding != null) {
            return false;
        }
        return Objects.equals(this.enabledSslCipherSuites, opt.enabledSslCipherSuites) && Objects.equals(this.sessionVariables, opt.sessionVariables) && Objects.equals(this.serverTimezone, opt.serverTimezone) && this.prepStmtCacheSize == opt.prepStmtCacheSize && this.prepStmtCacheSqlLimit == opt.prepStmtCacheSqlLimit && this.callableStmtCacheSize == opt.callableStmtCacheSize && Objects.equals(this.connectionAttributes, opt.connectionAttributes) && Objects.equals(this.useBatchMultiSend, opt.useBatchMultiSend) && Objects.equals(this.usePipelineAuth, opt.usePipelineAuth) && this.maxQuerySizeToLog == opt.maxQuerySizeToLog && Objects.equals(this.slowQueryThresholdNanos, opt.slowQueryThresholdNanos) && this.autocommit == opt.autocommit && Objects.equals(this.poolName, opt.poolName) && Objects.equals(this.galeraAllowedState, opt.galeraAllowedState) && Objects.equals(this.credentialType, opt.credentialType) && Objects.equals(this.nonMappedOptions, opt.nonMappedOptions) && Objects.equals(this.tlsSocketType, opt.tlsSocketType) && this.supportLobLocator == opt.supportLobLocator && this.useObChecksum == opt.useObChecksum && this.useOceanBaseProtocolV20 == opt.useOceanBaseProtocolV20 && this.useFormatExceptionMessage == opt.useFormatExceptionMessage && this.allowSendParamTypes == opt.allowSendParamTypes && this.complexDataCacheSize == opt.complexDataCacheSize && this.cacheComplexData == opt.cacheComplexData && this.useSqlStringCache == opt.useSqlStringCache && this.useServerPsStmtChecksum == opt.useServerPsStmtChecksum && this.characterEncoding == opt.characterEncoding && this.useCursorFetch == opt.useCursorFetch && this.supportNameBinding == opt.supportNameBinding && Objects.equals(this.socksProxyHost, opt.socksProxyHost) && this.socksProxyPort == opt.socksProxyPort && this.connectProxy == opt.connectProxy && this.usePieceData == opt.usePieceData && this.pieceLength == opt.pieceLength && this.useOraclePrepareExecute == opt.useOraclePrepareExecute && this.autoDeserialize == opt.autoDeserialize && this.maxBatchTotalParamsNum == opt.maxBatchTotalParamsNum && Objects.equals(this.minPoolSize, opt.minPoolSize);
    }
    
    @Override
    public int hashCode() {
        int result = (this.user != null) ? this.user.hashCode() : 0;
        result = 31 * result + ((this.password != null) ? this.password.hashCode() : 0);
        result = 31 * result + (this.trustServerCertificate ? 1 : 0);
        result = 31 * result + ((this.serverSslCert != null) ? this.serverSslCert.hashCode() : 0);
        result = 31 * result + ((this.trustStore != null) ? this.trustStore.hashCode() : 0);
        result = 31 * result + ((this.trustStorePassword != null) ? this.trustStorePassword.hashCode() : 0);
        result = 31 * result + ((this.keyStore != null) ? this.keyStore.hashCode() : 0);
        result = 31 * result + ((this.keyStorePassword != null) ? this.keyStorePassword.hashCode() : 0);
        result = 31 * result + ((this.keyPassword != null) ? this.keyPassword.hashCode() : 0);
        result = 31 * result + ((this.enabledSslProtocolSuites != null) ? this.enabledSslProtocolSuites.hashCode() : 0);
        result = 31 * result + (this.useFractionalSeconds ? 1 : 0);
        result = 31 * result + (this.pinGlobalTxToPhysicalConnection ? 1 : 0);
        result = 31 * result + ((this.socketFactory != null) ? this.socketFactory.hashCode() : 0);
        result = 31 * result + this.connectTimeout;
        result = 31 * result + ((this.pipe != null) ? this.pipe.hashCode() : 0);
        result = 31 * result + ((this.localSocket != null) ? this.localSocket.hashCode() : 0);
        result = 31 * result + ((this.sharedMemory != null) ? this.sharedMemory.hashCode() : 0);
        result = 31 * result + (this.tcpNoDelay ? 1 : 0);
        result = 31 * result + (this.tcpKeepAlive ? 1 : 0);
        result = 31 * result + ((this.tcpRcvBuf != null) ? this.tcpRcvBuf.hashCode() : 0);
        result = 31 * result + ((this.tcpSndBuf != null) ? this.tcpSndBuf.hashCode() : 0);
        result = 31 * result + (this.tcpAbortiveClose ? 1 : 0);
        result = 31 * result + ((this.localSocketAddress != null) ? this.localSocketAddress.hashCode() : 0);
        result = 31 * result + ((this.socketTimeout != null) ? this.socketTimeout.hashCode() : 0);
        result = 31 * result + (this.allowMultiQueries ? 1 : 0);
        result = 31 * result + (this.rewriteBatchedStatements ? 1 : 0);
        result = 31 * result + (this.useCompression ? 1 : 0);
        result = 31 * result + (this.interactiveClient ? 1 : 0);
        result = 31 * result + ((this.passwordCharacterEncoding != null) ? this.passwordCharacterEncoding.hashCode() : 0);
        result = 31 * result + (((boolean)this.useSsl) ? 1 : 0);
        result = 31 * result + ((this.enabledSslCipherSuites != null) ? this.enabledSslCipherSuites.hashCode() : 0);
        result = 31 * result + ((this.sessionVariables != null) ? this.sessionVariables.hashCode() : 0);
        result = 31 * result + (this.tinyInt1isBit ? 1 : 0);
        result = 31 * result + (this.yearIsDateType ? 1 : 0);
        result = 31 * result + (this.createDatabaseIfNotExist ? 1 : 0);
        result = 31 * result + ((this.serverTimezone != null) ? this.serverTimezone.hashCode() : 0);
        result = 31 * result + (this.nullCatalogMeansCurrent ? 1 : 0);
        result = 31 * result + (this.dumpQueriesOnException ? 1 : 0);
        result = 31 * result + (this.useOldAliasMetadataBehavior ? 1 : 0);
        result = 31 * result + (this.allowLocalInfile ? 1 : 0);
        result = 31 * result + (this.cachePrepStmts ? 1 : 0);
        result = 31 * result + this.prepStmtCacheSize;
        result = 31 * result + this.prepStmtCacheSqlLimit;
        result = 31 * result + (this.useLegacyDatetimeCode ? 1 : 0);
        result = 31 * result + (this.useAffectedRows ? 1 : 0);
        result = 31 * result + (this.maximizeMysqlCompatibility ? 1 : 0);
        result = 31 * result + (this.useServerPrepStmts ? 1 : 0);
        result = 31 * result + (this.continueBatchOnError ? 1 : 0);
        result = 31 * result + (this.jdbcCompliantTruncation ? 1 : 0);
        result = 31 * result + (this.cacheCallableStmts ? 1 : 0);
        result = 31 * result + this.callableStmtCacheSize;
        result = 31 * result + ((this.connectionAttributes != null) ? this.connectionAttributes.hashCode() : 0);
        result = 31 * result + ((this.useBatchMultiSend != null) ? this.useBatchMultiSend.hashCode() : 0);
        result = 31 * result + this.useBatchMultiSendNumber;
        result = 31 * result + ((this.usePipelineAuth != null) ? this.usePipelineAuth.hashCode() : 0);
        result = 31 * result + (this.enablePacketDebug ? 1 : 0);
        result = 31 * result + (this.includeInnodbStatusInDeadlockExceptions ? 1 : 0);
        result = 31 * result + (this.includeThreadDumpInDeadlockExceptions ? 1 : 0);
        result = 31 * result + (this.useBulkStmts ? 1 : 0);
        result = 31 * result + this.defaultFetchSize;
        result = 31 * result + (this.disableSslHostnameVerification ? 1 : 0);
        result = 31 * result + (this.log ? 1 : 0);
        result = 31 * result + (this.profileSql ? 1 : 0);
        result = 31 * result + this.maxQuerySizeToLog;
        result = 31 * result + ((this.slowQueryThresholdNanos != null) ? this.slowQueryThresholdNanos.hashCode() : 0);
        result = 31 * result + (this.assureReadOnly ? 1 : 0);
        result = 31 * result + (this.autoReconnect ? 1 : 0);
        result = 31 * result + (this.failOnReadOnly ? 1 : 0);
        result = 31 * result + (this.allowMasterDownConnection ? 1 : 0);
        result = 31 * result + this.retriesAllDown;
        result = 31 * result + this.validConnectionTimeout;
        result = 31 * result + this.loadBalanceBlacklistTimeout;
        result = 31 * result + this.failoverLoopRetries;
        result = 31 * result + (this.pool ? 1 : 0);
        result = 31 * result + (this.registerJmxPool ? 1 : 0);
        result = 31 * result + (this.useResetConnection ? 1 : 0);
        result = 31 * result + (this.useReadAheadInput ? 1 : 0);
        result = 31 * result + (this.staticGlobal ? 1 : 0);
        result = 31 * result + ((this.poolName != null) ? this.poolName.hashCode() : 0);
        result = 31 * result + ((this.galeraAllowedState != null) ? this.galeraAllowedState.hashCode() : 0);
        result = 31 * result + this.maxPoolSize;
        result = 31 * result + ((this.minPoolSize != null) ? this.minPoolSize.hashCode() : 0);
        result = 31 * result + this.maxIdleTime;
        result = 31 * result + this.poolValidMinDelay;
        result = 31 * result + (this.autocommit ? 1 : 0);
        result = 31 * result + ((this.credentialType != null) ? this.credentialType.hashCode() : 0);
        result = 31 * result + ((this.nonMappedOptions != null) ? this.nonMappedOptions.hashCode() : 0);
        result = 31 * result + ((this.tlsSocketType != null) ? this.tlsSocketType.hashCode() : 0);
        result = 31 * result + (this.supportLobLocator ? 1 : 0);
        result = 31 * result + (this.useObChecksum ? 1 : 0);
        result = 31 * result + (this.useOceanBaseProtocolV20 ? 1 : 0);
        result = 31 * result + (this.useFormatExceptionMessage ? 1 : 0);
        result = 31 * result + (this.allowSendParamTypes ? 1 : 0);
        result = 31 * result + this.complexDataCacheSize;
        result = 31 * result + (this.cacheComplexData ? 1 : 0);
        result = 31 * result + (this.useSqlStringCache ? 1 : 0);
        result = 31 * result + (this.useServerPsStmtChecksum ? 1 : 0);
        result = 31 * result + ((this.characterEncoding != null) ? this.characterEncoding.hashCode() : 0);
        result = 31 * result + (this.useCursorFetch ? 1 : 0);
        result = 31 * result + (this.supportNameBinding ? 1 : 0);
        result = 31 * result + ((this.socksProxyHost != null) ? this.socksProxyHost.hashCode() : 0);
        result = 31 * result + this.socksProxyPort;
        result = 31 * result + (this.connectProxy ? 1 : 0);
        result = 31 * result + (this.usePieceData ? 1 : 0);
        result = 31 * result + this.pieceLength;
        result = 31 * result + (this.useOraclePrepareExecute ? 1 : 0);
        result = 31 * result + (this.autoDeserialize ? 1 : 0);
        result = 31 * result + this.maxBatchTotalParamsNum;
        return result;
    }
    
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
