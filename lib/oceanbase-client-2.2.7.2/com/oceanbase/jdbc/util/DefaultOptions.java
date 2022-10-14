// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.util;

import java.lang.reflect.Field;
import java.util.Iterator;
import com.oceanbase.jdbc.internal.util.OptionUtils;
import com.oceanbase.jdbc.credential.CredentialPlugin;
import java.util.Properties;
import com.oceanbase.jdbc.internal.util.constant.HaMode;

public enum DefaultOptions
{
    USER("user", "1.0.0", "Database user name", false), 
    PASSWORD("password", "1.0.0", "Password of database user", false), 
    CONNECT_TIMEOUT("connectTimeout", Integer.valueOf(30000), Integer.valueOf(0), "1.1.8", "The connect timeout value, in milliseconds, or zero for no timeout.", false), 
    PIPE("pipe", "1.1.3", "On Windows, specify named pipe name to connect.", false), 
    LOCAL_SOCKET("localSocket", "1.1.4", "Permits connecting to the database via Unix domain socket, if the server allows it. \nThe value is the path of Unix domain socket (i.e \"socket\" database parameter : select @@socket).", false), 
    SHARED_MEMORY("sharedMemory", "1.1.4", "Permits connecting to the database via shared memory, if the server allows it. \nThe value is the base name of the shared memory.", false), 
    TCP_NO_DELAY("tcpNoDelay", Boolean.TRUE, "1.0.0", "Sets corresponding option on the connection socket.", false), 
    TCP_ABORTIVE_CLOSE("tcpAbortiveClose", Boolean.FALSE, "1.1.1", "Sets corresponding option on the connection socket.", false), 
    LOCAL_SOCKET_ADDRESS("localSocketAddress", "1.1.8", "Hostname or IP address to bind the connection socket to a local (UNIX domain) socket.", false), 
    SOCKET_TIMEOUT("socketTimeout", new Integer[] { 10000, null, null, null, null, null }, Integer.valueOf(0), "1.1.8", "Defined the network socket timeout (SO_TIMEOUT) in milliseconds. Value of 0 disables this timeout. \nIf the goal is to set a timeout for all queries, since MariaDB 10.1.1, the server has permitted a solution to limit the query time by setting a system variable, max_statement_time. The advantage is that the connection then is still usable.\nDefault: 0 (standard configuration) or 10000ms (using \"aurora\" failover configuration).", false), 
    INTERACTIVE_CLIENT("interactiveClient", Boolean.FALSE, "1.1.8", "Session timeout is defined by the wait_timeout server variable. Setting interactiveClient to true will tell the server to use the interactive_timeout server variable.", false), 
    DUMP_QUERY_ON_EXCEPTION("dumpQueriesOnException", Boolean.FALSE, "1.1.0", "If set to 'true', an exception is thrown during query execution containing a query string.", false), 
    USE_OLD_ALIAS_METADATA_BEHAVIOR("useOldAliasMetadataBehavior", Boolean.FALSE, "1.1.9", "Metadata ResultSetMetaData.getTableName() returns the physical table name. \"useOldAliasMetadataBehavior\" permits activating the legacy code that sends the table alias if set.", false), 
    SESSION_VARIABLES("sessionVariables", "1.1.0", "<var>=<value> pairs separated by comma, mysql session variables, set upon establishing successful connection.", false), 
    CREATE_DATABASE_IF_NOT_EXISTS("createDatabaseIfNotExist", Boolean.FALSE, "1.1.8", "the specified database in the url will be created if non-existent.", false), 
    SERVER_TIMEZONE("serverTimezone", "1.1.8", "Defines the server time zone.\nto use only if the jre server has a different time implementation of the server.\n(best to have the same server time zone when possible).", false), 
    NULL_CATALOG_MEANS_CURRENT("nullCatalogMeansCurrent", Boolean.TRUE, "1.1.8", "DatabaseMetaData use current catalog if null.", false), 
    TINY_INT_IS_BIT("tinyInt1isBit", Boolean.TRUE, "1.0.0", "Datatype mapping flag, handle Tiny as BIT(boolean).", false), 
    YEAR_IS_DATE_TYPE("yearIsDateType", Boolean.TRUE, "1.0.0", "Year is date type, rather than numerical.", false), 
    USE_SSL("useSsl", Boolean.FALSE, "1.1.0", "Force SSL on connection. (legacy alias \"useSSL\")", false), 
    USER_COMPRESSION("useCompression", Boolean.FALSE, "1.0.0", "Compresses the exchange with the database through gzip. This permits better performance when the database is not in the same location.", false), 
    ALLOW_MULTI_QUERIES("allowMultiQueries", Boolean.FALSE, "1.0.0", "permit multi-queries like insert into ab (i) values (1); insert into ab (i) values (2).", false), 
    REWRITE_BATCHED_STATEMENTS("rewriteBatchedStatements", Boolean.FALSE, "1.1.8", "For insert queries, rewrite batchedStatement to execute in a single executeQuery.\nexample:\n   insert into ab (i) values (?) with first batch values = 1, second = 2 will be rewritten\n   insert into ab (i) values (1), (2). \n\nIf query cannot be rewriten in \"multi-values\", rewrite will use multi-queries : INSERT INTO TABLE(col1) VALUES (?) ON DUPLICATE KEY UPDATE col2=? with values [1,2] and [2,3]\" will be rewritten\nINSERT INTO TABLE(col1) VALUES (1) ON DUPLICATE KEY UPDATE col2=2;INSERT INTO TABLE(col1) VALUES (3) ON DUPLICATE KEY UPDATE col2=4\n\nwhen active, the useServerPrepStmts option is set to false", false), 
    TCP_KEEP_ALIVE("tcpKeepAlive", Boolean.TRUE, "1.0.0", "Sets corresponding option on the connection socket.", false), 
    TCP_RCV_BUF("tcpRcvBuf", (Integer)null, Integer.valueOf(0), "1.0.0", "set buffer size for TCP buffer (SO_RCVBUF).", false), 
    TCP_SND_BUF("tcpSndBuf", (Integer)null, Integer.valueOf(0), "1.0.0", "set buffer size for TCP buffer (SO_SNDBUF).", false), 
    SOCKET_FACTORY("socketFactory", "1.0.0", "to use a custom socket factory, set it to the full name of the class that implements javax.net.SocketFactory.", false), 
    PIN_GLOBAL_TX_TO_PHYSICAL_CONNECTION("pinGlobalTxToPhysicalConnection", Boolean.FALSE, "1.1.8", "", false), 
    TRUST_SERVER_CERTIFICATE("trustServerCertificate", Boolean.FALSE, "1.1.1", "When using SSL, do not check server's certificate.", false), 
    SERVER_SSL_CERT("serverSslCert", "1.1.3", "Permits providing server's certificate in DER form, or server's CA certificate. The server will be added to trustStor. This permits a self-signed certificate to be trusted.\nCan be used in one of 3 forms : \n* serverSslCert=/path/to/cert.pem (full path to certificate)\n* serverSslCert=classpath:relative/cert.pem (relative to current classpath)\n* or as verbatim DER-encoded certificate string \"------BEGIN CERTIFICATE-----\" .", false), 
    USE_FRACTIONAL_SECONDS("useFractionalSeconds", Boolean.TRUE, "1.0.0", "Correctly handle subsecond precision in timestamps (feature available with MariaDB 5.3 and later).\nMay confuse 3rd party components (Hibernate).", false), 
    AUTO_RECONNECT("autoReconnect", Boolean.FALSE, "1.2.0", "Driver must recreateConnection after a failover.", false), 
    FAIL_ON_READ_ONLY("failOnReadOnly", Boolean.FALSE, "1.2.0", "After a master failover and no other master found, back on a read-only host ( throw exception if not).", false), 
    RETRY_ALL_DOWN("retriesAllDown", Integer.valueOf(120), Integer.valueOf(0), "1.2.0", "When using loadbalancing, the number of times the driver should cycle through available hosts, attempting to connect.\n     * Between cycles, the driver will pause for 250ms if no servers are available.", false), 
    FAILOVER_LOOP_RETRIES("failoverLoopRetries", Integer.valueOf(120), Integer.valueOf(0), "1.2.0", "When using failover, the number of times the driver should cycle silently through available hosts, attempting to connect.\n     * Between cycles, the driver will pause for 250ms if no servers are available.\n     * if set to 0, there will be no silent reconnection", false), 
    VALID_CONNECTION_TIMEOUT("validConnectionTimeout", Integer.valueOf(0), Integer.valueOf(0), "1.2.0", "When in multiple hosts, after this time in second without used, verification that the connections haven't been lost.\n     * When 0, no verification will be done. Defaults to 0 (120 before 1.5.8 version)", false), 
    LOAD_BALANCE_BLACKLIST_TIMEOUT("loadBalanceBlacklistTimeout", Integer.valueOf(50), Integer.valueOf(0), "1.2.0", "time in second a server is blacklisted after a connection failure.", false), 
    CACHE_PREP_STMTS("cachePrepStmts", Boolean.FALSE, "1.3.0", "enable/disable prepare Statement cache, default false.", false), 
    PREP_STMT_CACHE_SIZE("prepStmtCacheSize", Integer.valueOf(250), Integer.valueOf(0), "1.3.0", "This sets the number of prepared statements that the driver will cache per connection if \"cachePrepStmts\" is enabled.", false), 
    PREP_STMT_CACHE_SQL_LIMIT("prepStmtCacheSqlLimit", Integer.valueOf(2048), Integer.valueOf(0), "1.3.0", "This is the maximum length of a prepared SQL statement that the driver will cache  if \"cachePrepStmts\" is enabled.", false), 
    ASSURE_READONLY("assureReadOnly", Boolean.TRUE, "1.3.0", "Ensure that when Connection.setReadOnly(true) is called, host is in read-only mode by setting the session transaction to read-only.", false), 
    USE_LEGACY_DATETIME_CODE("useLegacyDatetimeCode", Boolean.TRUE, "1.3.0", "if true (default) store date/timestamps according to client time zone.\nif false, store all date/timestamps in DB according to server time zone, and time information (that is a time difference), doesn't take\ntimezone in account.", false), 
    MAXIMIZE_MYSQL_COMPATIBILITY("maximizeMysqlCompatibility", Boolean.FALSE, "1.3.0", "maximize MySQL compatibility.\nwhen using jdbc setDate(), will store date in client timezone, not in server timezone when useLegacyDatetimeCode = false.\ndefault to false.", false), 
    USE_SERVER_PREP_STMTS("useServerPrepStmts", Boolean.FALSE, "1.3.0", "useServerPrepStmts must prepared statements be prepared on server side, or just faked on client side.\n     * if rewriteBatchedStatements is set to true, this options will be set to false.", false), 
    TRUSTSTORE("trustStore", "1.3.0", "File path of the trustStore file (similar to java System property \"javax.net.ssl.trustStore\"). (legacy alias trustCertificateKeyStoreUrl)\nUse the specified file for trusted root certificates.\nWhen set, overrides serverSslCert.", false), 
    TRUST_CERTIFICATE_KEYSTORE_PASSWORD("trustStorePassword", "1.3.0", "Password for the trusted root certificate file (similar to java System property \"javax.net.ssl.trustStorePassword\").\n(legacy alias trustCertificateKeyStorePassword).", false), 
    KEYSTORE("keyStore", "1.3.0", "File path of the keyStore file that contain client private key store and associate certificates (similar to java System property \"javax.net.ssl.keyStore\", but ensure that only the private key's entries are used).(legacy alias clientCertificateKeyStoreUrl).", false), 
    KEYSTORE_PASSWORD("keyStorePassword", "1.3.0", "Password for the client certificate keyStore (similar to java System property \"javax.net.ssl.keyStorePassword\").(legacy alias clientCertificateKeyStorePassword)", false), 
    PRIVATE_KEYS_PASSWORD("keyPassword", "1.5.3", "Password for the private key in client certificate keyStore. (only needed if private key password differ from keyStore password).", false), 
    ENABLED_SSL_PROTOCOL_SUITES("enabledSslProtocolSuites", "1.5.0", "Force TLS/SSL protocol to a specific set of TLS versions (comma separated list). \nExample : \"TLSv1, TLSv1.1, TLSv1.2\"\n(Alias \"enabledSSLProtocolSuites\" works too)", false), 
    ENABLED_SSL_CIPHER_SUITES("enabledSslCipherSuites", "1.5.0", "Force TLS/SSL cipher (comma separated list).\nExample : \"TLS_DHE_RSA_WITH_AES_256_GCM_SHA384, TLS_DHE_DSS_WITH_AES_256_GCM_SHA384\"", false), 
    CONTINUE_BATCH_ON_ERROR("continueBatchOnError", Boolean.TRUE, "1.4.0", "When executing batch queries, must batch continue on error.", false), 
    JDBC_COMPLIANT_TRUNCATION("jdbcCompliantTruncation", Boolean.TRUE, "1.4.0", "Truncation error (\"Data truncated for column '%' at row %\", \"Out of range value for column '%' at row %\") will be thrown as error, and not as warning.", false), 
    CACHE_CALLABLE_STMTS("cacheCallableStmts", Boolean.TRUE, "1.4.0", "enable/disable callable Statement cache, default true.", false), 
    CALLABLE_STMT_CACHE_SIZE("callableStmtCacheSize", Integer.valueOf(150), Integer.valueOf(0), "1.4.0", "This sets the number of callable statements that the driver will cache per VM if \"cacheCallableStmts\" is enabled.", false), 
    CONNECTION_ATTRIBUTES("connectionAttributes", "1.4.0", "When performance_schema is active, permit to send server some client information in a key;value pair format (example: connectionAttributes=key1:value1,key2,value2).\nThose informations can be retrieved on server within tables performance_schema.session_connect_attrs and performance_schema.session_account_connect_attrs.\nThis can permit from server an identification of client/application", false), 
    USE_BATCH_MULTI_SEND("useBatchMultiSend", (Boolean)null, "1.5.0", "*Not compatible with aurora*\nDriver will can send queries by batch. \nIf set to false, queries are sent one by one, waiting for the result before sending the next one. \nIf set to true, queries will be sent by batch corresponding to the useBatchMultiSendNumber option value (default 100) or according to the max_allowed_packet server variable if the packet size does not permit sending as many queries. Results will be read later, avoiding a lot of network latency when the client and server aren't on the same host. \n\nThis option is mainly effective when the client is distant from the server.", false), 
    USE_BATCH_MULTI_SEND_NUMBER("useBatchMultiSendNumber", Integer.valueOf(100), Integer.valueOf(1), "1.5.0", "When option useBatchMultiSend is active, indicate the maximum query send in a row before reading results.", false), 
    LOGGING("log", Boolean.FALSE, "1.5.0", "Enable log information. \nrequire Slf4j version > 1.4 dependency.\nLog level correspond to Slf4j logging implementation", false), 
    PROFILE_SQL("profileSql", Boolean.FALSE, "1.5.0", "log query execution time.", false), 
    MAX_QUERY_LOG_SIZE("maxQuerySizeToLog", Integer.valueOf(1024), Integer.valueOf(0), "1.5.0", "Max query log size.", false), 
    SLOW_QUERY_TIME("slowQueryThresholdNanos", (Long)null, Long.valueOf(0L), "1.5.0", "Will log query with execution time superior to this value (if defined )", false), 
    PASSWORD_CHARACTER_ENCODING("passwordCharacterEncoding", "1.5.9", "Indicate password encoding charset. If not set, driver use platform's default charset.", false), 
    PIPELINE_AUTH("usePipelineAuth", (Boolean)null, "1.6.0", "*Not compatible with aurora*\nDuring connection, different queries are executed. When option is active those queries are send using pipeline (all queries are send, then only all results are reads), permitting faster connection creation", false), 
    ENABLE_PACKET_DEBUG("enablePacketDebug", Boolean.FALSE, "1.6.0", "Driver will save the last 16 MariaDB packet exchanges (limited to first 1000 bytes). Hexadecimal value of those packets will be added to stacktrace when an IOException occur.\nThis option has no impact on performance but driver will then take 16kb more memory.", false), 
    SSL_HOSTNAME_VERIFICATION("disableSslHostnameVerification", Boolean.FALSE, "2.1.0", "When using ssl, the driver checks the hostname against the server's identity as presented in the server's certificate (checking alternative names or the certificate CN) to prevent man-in-the-middle attacks. This option permits deactivating this validation. Hostname verification is disabled when the trustServerCertificate option is set", false), 
    USE_BULK_PROTOCOL("useBulkStmts", Boolean.FALSE, "2.1.0", "Use dedicated COM_STMT_BULK_EXECUTE protocol for batch insert when possible. (batch without Statement.RETURN_GENERATED_KEYS and streams) to have faster batch. (significant only if server MariaDB >= 10.2.7)", false), 
    AUTOCOMMIT("autocommit", Boolean.TRUE, "2.2.0", "Set default autocommit value on connection initialization", false), 
    POOL("pool", Boolean.FALSE, "2.2.0", "Use pool. This option is useful only if not using a DataSource object, but only a connection object.", false), 
    POOL_NAME("poolName", "2.2.0", "Pool name that permits identifying threads. default: auto-generated as MariaDb-pool-<pool-index>", false), 
    MAX_POOL_SIZE("maxPoolSize", Integer.valueOf(8), Integer.valueOf(1), "2.2.0", "The maximum number of physical connections that the pool should contain.", false), 
    MIN_POOL_SIZE("minPoolSize", (Integer)null, Integer.valueOf(0), "2.2.0", "When connections are removed due to not being used for longer than than \"maxIdleTime\", connections are closed and removed from the pool. \"minPoolSize\" indicates the number of physical connections the pool should keep available at all times. Should be less or equal to maxPoolSize.", false), 
    MAX_IDLE_TIME("maxIdleTime", Integer.valueOf(600), Integer.valueOf(60), "2.2.0", "The maximum amount of time in seconds that a connection can stay in the pool when not used. This value must always be below @wait_timeout value - 45s \nDefault: 600 in seconds (=10 minutes), minimum value is 60 seconds", false), 
    POOL_VALID_MIN_DELAY("poolValidMinDelay", Integer.valueOf(1000), Integer.valueOf(0), "2.2.0", "When asking a connection to pool, the pool will validate the connection state. \"poolValidMinDelay\" permits disabling this validation if the connection has been borrowed recently avoiding useless verifications in case of frequent reuse of connections. 0 means validation is done each time the connection is asked.", false), 
    STATIC_GLOBAL("staticGlobal", Boolean.FALSE, "2.2.0", "Indicates the values of the global variables max_allowed_packet, wait_timeout, autocommit, auto_increment_increment, time_zone, system_time_zone and tx_isolation) won't be changed, permitting the pool to create new connections faster.", false), 
    REGISTER_POOL_JMX("registerJmxPool", Boolean.TRUE, "2.2.0", "Register JMX monitoring pools.", false), 
    USE_RESET_CONNECTION("useResetConnection", Boolean.FALSE, "2.2.0", "When a connection is closed() (given back to pool), the pool resets the connection state. Setting this option, the prepare command will be deleted, session variables changed will be reset, and user variables will be destroyed when the server permits it (>= MariaDB 10.2.4, >= MySQL 5.7.3), permitting saving memory on the server if the application make extensive use of variables. Must not be used with the useServerPrepStmts option", false), 
    ALLOW_MASTER_DOWN("allowMasterDownConnection", Boolean.FALSE, "2.2.0", "When using master/slave configuration, permit to create connection when master is down. If no master is up, default connection is then a slave and Connection.isReadOnly() will then return true.", false), 
    GALERA_ALLOWED_STATE("galeraAllowedState", "2.2.5", "Usually, Connection.isValid just send an empty packet to server, and server send a small response to ensure connectivity. When this option is set, connector will ensure Galera server state \"wsrep_local_state\" correspond to allowed values (separated by comma). Example \"4,5\", recommended is \"4\". see galera state to know more.", false), 
    USE_AFFECTED_ROWS("useAffectedRows", Boolean.FALSE, "2.3.0", "If false (default), use \"found rows\" for the row count of statements. This corresponds to the JDBC standard.\nIf true, use \"affected rows\" for the row count.\nThis changes the behavior of, for example, UPDATE... ON DUPLICATE KEY statements.", false), 
    INCLUDE_STATUS("includeInnodbStatusInDeadlockExceptions", Boolean.FALSE, "2.3.0", "add \"SHOW ENGINE INNODB STATUS\" result to exception trace when having a deadlock exception", false), 
    INCLUDE_THREAD_DUMP("includeThreadDumpInDeadlockExceptions", Boolean.FALSE, "2.3.0", "add thread dump to exception trace when having a deadlock exception", false), 
    READ_AHEAD("useReadAheadInput", Boolean.TRUE, "2.4.0", "use a buffered inputSteam that read socket available data", false), 
    KEY_STORE_TYPE("keyStoreType", (String)null, "2.4.0", "indicate key store type (JKS/PKCS12). default is null, then using java default type", false), 
    TRUST_STORE_TYPE("trustStoreType", (String)null, "2.4.0", "indicate trust store type (JKS/PKCS12). default is null, then using java default type", false), 
    SERVICE_PRINCIPAL_NAME("servicePrincipalName", (String)null, "2.4.0", "when using GSSAPI authentication, SPN (Service Principal Name) use the server SPN information. When set, connector will use this value, ignoring server information", false), 
    DEFAULT_FETCH_SIZE("defaultFetchSize", Integer.valueOf(0), Integer.valueOf(0), "2.4.2", "The driver will call setFetchSize(n) with this value on all newly-created Statements", false), 
    USE_MYSQL_AS_DATABASE("useMysqlMetadata", Boolean.FALSE, "2.4.1", "force DatabaseMetadata.getDatabaseProductName() to return \"MySQL\" as database, not real database type", false), 
    BLANK_TABLE_NAME_META("blankTableNameMeta", Boolean.FALSE, "2.4.3", "Resultset metadata getTableName always return blank. This option is mainly for ORACLE db compatibility", false), 
    CREDENTIAL_TYPE("credentialType", (String)null, "2.5.0", "Indicate the credential plugin type to use. Plugin must be present in classpath", false), 
    SERVER_KEY_FILE("serverRsaPublicKeyFile", (String)null, "2.5.0", "Indicate path to MySQL server public key file", false), 
    ALLOW_SERVER_KEY_RETRIEVAL("allowPublicKeyRetrieval", Boolean.FALSE, "2.5.0", "Permit to get MySQL server key retrieval", false), 
    TLS_SOCKET_TYPE("tlsSocketType", (String)null, "2.5.0", "Indicate TLS socket type implementation", false), 
    TRACK_SCHEMA("trackSchema", Boolean.TRUE, "2.6.0", "manage session_track_schema setting when server has CLIENT_SESSION_TRACK capability", false), 
    SUPPORT_LOB_LOCATOR("supportLobLocator", Boolean.TRUE, "2.0.1", "Lob locator switch for BLOB and CLOB type data", false), 
    USE_OB_CHECKSUM("useObChecksum", Boolean.TRUE, "2.0.1", "A connection option for protocol v20", false), 
    USE_OCEANBASE_PROTOCOLV20("useOceanBaseProtocolV20", Boolean.TRUE, "2.0.1", "Use v20 protocol to transmit data", false), 
    ALLOW_ALWAYS_SEND_PARAM_TYPES("allowSendParamTypes", Boolean.FALSE, "2.0.1", "Store types of parameters in first package that is sent to the server", false), 
    USE_FORMAT_EXCEPTION_MESSAGE("useFormatExceptionMessage", Boolean.FALSE, "2.0.1", "Error message in ORACLE format used, such as 'ORA-'", false), 
    COMPLEX_DATA_CACHE_SIZE("complexDataCacheSize", Integer.valueOf(50), Integer.valueOf(0), "2.0.1", "Cached complex data size", false), 
    CACHE_COMPLEX_DATA("cacheComplexData", Boolean.TRUE, "2.0.1", "Whether to cache complex data", false), 
    USE_SQL_STRING_CACHE("useSqlStringCache", Boolean.FALSE, "2.0.1", "Cache sql sql strings into local jdbc memory", false), 
    USE_SERVER_PS_STMT_CHECKSUM("useServerPsStmtChecksum", Boolean.TRUE, "2.0.1", "Use prepare statement checksum to ensure the correctness of the mysql protocol", false), 
    CHARACTER_ENCODING("characterEncoding", "utf8", "2.0.1", "Support mysql url option characterEncoding", false), 
    USE_CURSOR_FETCH("useCursorFetch", Boolean.FALSE, "NA", "Indicate driver to fetch data from server by bunch of fetchSize rows. This permit to avoid having to fetch all results from server.", false), 
    SOCKS_PROXY_HOST("socksProxyHost", (String)null, "2.2.3", "Name or IP address of SOCKS host to connect through.", false), 
    SOCKS_PROXY_PORT("socksProxyPort", Integer.valueOf(1080), Integer.valueOf(0), "2.2.3", "Port of SOCKS server.", false), 
    CONNECT_PROXY("connectProxy", Boolean.FALSE, "2.2.3", "Indicate driver to connect to ob proxy ", false), 
    SUPPORT_NAME_BINDING("supportNameBinding", Boolean.TRUE, "2.2.2", "Oracle name binding switch the apis such as setIntAtName and registerOutParameterAtName", false), 
    PIECE_LENGTH("pieceLength", Integer.valueOf(1048576), Integer.valueOf(0), "2.2.6", "The size of data sent each time when COM_STMT_SEND_PIECE_DATA protocol is used in Oracle mode", false), 
    USE_PIECE_DATA("usePieceData", Boolean.FALSE, "2.2.6", "Use COM_STMT_SEND_PIECE_DATA protocol to set InputStream and Reader parameters in Oracle mode", false), 
    USE_ORACLE_PREPARE_EXECUTE("useOraclePrepareExecute", Boolean.FALSE, "2.2.6", "Oracle mode preparedStatement don't communicate with server until execute using COM_STMT_PREPARE_EXECUTE", false), 
    AUTO_DESERIALIZE("autoDeserialize", Boolean.FALSE, "2.2.7", "", false), 
    MAX_BATCH_TOTOAL_PARAMS_NUM("maxBatchTotalParamsNum", Integer.valueOf(30000), Integer.valueOf(0), "2.2.6", "When using executeBatch, the maximum number of spliced parameters", false);
    
    private final String optionName;
    private final String description;
    private final boolean required;
    private final Object objType;
    private final Object defaultValue;
    private final Object minValue;
    private final Object maxValue;
    
    private DefaultOptions(final String optionName, final String implementationVersion, final String description, final boolean required) {
        this.optionName = optionName;
        this.description = description;
        this.required = required;
        this.objType = String.class;
        this.defaultValue = null;
        this.minValue = null;
        this.maxValue = null;
    }
    
    private DefaultOptions(final String optionName, final String defaultValue, final String implementationVersion, final String description, final boolean required) {
        this.optionName = optionName;
        this.description = description;
        this.required = required;
        this.objType = String.class;
        this.defaultValue = defaultValue;
        this.minValue = null;
        this.maxValue = null;
    }
    
    private DefaultOptions(final String optionName, final int defaultValue, final String implementationVersion, final String description, final boolean required) {
        this.optionName = optionName;
        this.description = description;
        this.required = required;
        this.objType = String.class;
        this.defaultValue = defaultValue;
        this.minValue = null;
        this.maxValue = null;
    }
    
    private DefaultOptions(final String optionName, final Boolean defaultValue, final String implementationVersion, final String description, final boolean required) {
        this.optionName = optionName;
        this.objType = Boolean.class;
        this.defaultValue = defaultValue;
        this.description = description;
        this.required = required;
        this.minValue = null;
        this.maxValue = null;
    }
    
    private DefaultOptions(final String optionName, final Integer defaultValue, final Integer minValue, final String implementationVersion, final String description, final boolean required) {
        this.optionName = optionName;
        this.objType = Integer.class;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = Integer.MAX_VALUE;
        this.description = description;
        this.required = required;
    }
    
    private DefaultOptions(final String optionName, final Long defaultValue, final Long minValue, final String implementationVersion, final String description, final boolean required) {
        this.optionName = optionName;
        this.objType = Long.class;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = Long.MAX_VALUE;
        this.description = description;
        this.required = required;
    }
    
    private DefaultOptions(final String optionName, final Integer[] defaultValue, final Integer minValue, final String implementationVersion, final String description, final boolean required) {
        this.optionName = optionName;
        this.objType = Integer.class;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = Integer.MAX_VALUE;
        this.description = description;
        this.required = required;
    }
    
    public static Options defaultValues(final HaMode haMode) {
        return parse(haMode, "", new Properties());
    }
    
    public static Options defaultValues(final HaMode haMode, final boolean pool) {
        final Properties properties = new Properties();
        properties.setProperty("pool", String.valueOf(pool));
        final Options options = parse(haMode, "", properties);
        postOptionProcess(options, null);
        return options;
    }
    
    public static void parse(final HaMode haMode, final String urlParameters, final Options options) {
        final Properties prop = new Properties();
        parse(haMode, urlParameters, prop, options);
        postOptionProcess(options, null);
    }
    
    private static Options parse(final HaMode haMode, final String urlParameters, final Properties properties) {
        final Options options = parse(haMode, urlParameters, properties, null);
        postOptionProcess(options, null);
        return options;
    }
    
    public static Options parse(final HaMode haMode, final String urlParameters, final Properties properties, final Options options) {
        if (urlParameters != null && !urlParameters.isEmpty()) {
            final String[] split;
            final String[] parameters = split = urlParameters.split("&");
            for (final String parameter : split) {
                final int pos = parameter.indexOf(61);
                if (pos == -1) {
                    if (!properties.containsKey(parameter)) {
                        properties.setProperty(parameter, "");
                    }
                }
                else if (!properties.containsKey(parameter.substring(0, pos))) {
                    properties.setProperty(parameter.substring(0, pos), parameter.substring(pos + 1));
                }
            }
        }
        return parse(haMode, properties, options);
    }
    
    private static Options parse(final HaMode haMode, final Properties properties, final Options paramOptions) {
        final Options options = (paramOptions != null) ? paramOptions : new Options();
        try {
            for (final String key : properties.stringPropertyNames()) {
                final String propertyValue = properties.getProperty(key);
                final DefaultOptions o = OptionUtils.OPTIONS_MAP.get(key);
                if (o != null && propertyValue != null) {
                    final Field field = Options.class.getField(o.optionName);
                    if (o.objType.equals(String.class)) {
                        field.set(options, propertyValue);
                    }
                    else if (o.objType.equals(Boolean.class)) {
                        final String lowerCase = propertyValue.toLowerCase();
                        switch (lowerCase) {
                            case "":
                            case "1":
                            case "true": {
                                field.set(options, Boolean.TRUE);
                                continue;
                            }
                            case "0":
                            case "false": {
                                field.set(options, Boolean.FALSE);
                                continue;
                            }
                            default: {
                                throw new IllegalArgumentException("Optional parameter " + o.optionName + " must be boolean (true/false or 0/1) was \"" + propertyValue + "\"");
                            }
                        }
                    }
                    else {
                        if (o.objType.equals(Integer.class)) {
                            try {
                                final Integer value = Integer.parseInt(propertyValue);
                                assert o.minValue != null;
                                assert o.maxValue != null;
                                if (value.compareTo((Integer)o.minValue) < 0 || value.compareTo((Integer)o.maxValue) > 0) {
                                    throw new IllegalArgumentException("Optional parameter " + o.optionName + " must be greater or equal to " + o.minValue + (((int)o.maxValue != Integer.MAX_VALUE) ? (" and smaller than " + o.maxValue) : " ") + ", was \"" + propertyValue + "\"");
                                }
                                field.set(options, value);
                                continue;
                            }
                            catch (NumberFormatException n2) {
                                throw new IllegalArgumentException("Optional parameter " + o.optionName + " must be Integer, was \"" + propertyValue + "\"");
                            }
                        }
                        if (!o.objType.equals(Long.class)) {
                            continue;
                        }
                        try {
                            final Long value2 = Long.parseLong(propertyValue);
                            assert o.minValue != null;
                            assert o.maxValue != null;
                            if (value2.compareTo((Long)o.minValue) < 0 || value2.compareTo((Long)o.maxValue) > 0) {
                                throw new IllegalArgumentException("Optional parameter " + o.optionName + " must be greater or equal to " + o.minValue + (((long)o.maxValue != Long.MAX_VALUE) ? (" and smaller than " + o.maxValue) : " ") + ", was \"" + propertyValue + "\"");
                            }
                            field.set(options, value2);
                        }
                        catch (NumberFormatException n2) {
                            throw new IllegalArgumentException("Optional parameter " + o.optionName + " must be Long, was \"" + propertyValue + "\"");
                        }
                    }
                }
                else {
                    options.nonMappedOptions.setProperty(key, properties.getProperty(key));
                }
            }
            if (options.socketTimeout == null) {
                options.socketTimeout = ((Integer[])DefaultOptions.SOCKET_TIMEOUT.defaultValue)[haMode.ordinal()];
            }
            if (options.useCursorFetch) {
                options.useServerPrepStmts = true;
            }
        }
        catch (NoSuchFieldException | IllegalAccessException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException n = ex;
            n.printStackTrace();
        }
        catch (SecurityException s) {
            throw new IllegalArgumentException("Security too restrictive : " + s.getMessage());
        }
        return options;
    }
    
    public static void postOptionProcess(final Options options, final CredentialPlugin credentialPlugin) {
        if (options.usePieceData) {
            options.useOraclePrepareExecute = true;
            options.useCursorFetch = true;
        }
        if (options.pipe != null) {
            options.useBatchMultiSend = false;
            options.usePipelineAuth = false;
        }
        if (options.pool) {
            options.minPoolSize = ((options.minPoolSize == null) ? options.maxPoolSize : Math.min(options.minPoolSize, options.maxPoolSize));
        }
        if (options.defaultFetchSize < 0) {
            options.defaultFetchSize = 0;
        }
        if (credentialPlugin != null && credentialPlugin.mustUseSsl()) {
            options.useSsl = Boolean.TRUE;
        }
    }
    
    public static void propertyString(final Options options, final HaMode haMode, final StringBuilder sb) {
        try {
            boolean first = true;
            for (final DefaultOptions o : values()) {
                final Object value = Options.class.getField(o.optionName).get(options);
                if (value != null && !value.equals(o.defaultValue)) {
                    if (first) {
                        first = false;
                        sb.append('?');
                    }
                    else {
                        sb.append('&');
                    }
                    sb.append(o.optionName).append('=');
                    if (o.objType.equals(String.class)) {
                        sb.append((String)value);
                    }
                    else if (o.objType.equals(Boolean.class)) {
                        sb.append(((Boolean)value).toString());
                    }
                    else if (o.objType.equals(Integer.class) || o.objType.equals(Long.class)) {
                        sb.append(value);
                    }
                }
            }
        }
        catch (NoSuchFieldException | IllegalAccessException ex2) {
            final ReflectiveOperationException ex;
            final ReflectiveOperationException n = ex;
            n.printStackTrace();
        }
    }
    
    public String getOptionName() {
        return this.optionName;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public boolean isRequired() {
        return this.required;
    }
}
