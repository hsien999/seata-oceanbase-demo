// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.LinkedHashMap;
import com.alipay.oceanbase.jdbc.log.NullLogger;
import java.security.Permission;
import java.util.concurrent.Executor;
import java.sql.Savepoint;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEvent;
import java.sql.Struct;
import com.alipay.oceanbase.jdbc.extend.datatype.ComplexDataType;
import java.sql.SQLClientInfoException;
import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.Clob;
import com.alipay.oceanbase.jdbc.util.StringCacheUtil;
import java.sql.CallableStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLWarning;
import java.sql.SQLFeatureNotSupportedException;
import java.io.IOException;
import java.util.Stack;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Locale;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.sql.ResultSet;
import java.util.Collections;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Enumeration;
import com.alipay.oceanbase.jdbc.log.LogUtils;
import com.alipay.oceanbase.jdbc.log.LogFactory;
import java.util.GregorianCalendar;
import java.sql.Blob;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.sql.SQLException;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Properties;
import com.alipay.oceanbase.jdbc.util.LRUCache;
import java.util.concurrent.CopyOnWriteArrayList;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEventHandler;
import java.util.TimeZone;
import java.sql.DatabaseMetaData;
import java.util.Random;
import com.alipay.oceanbase.jdbc.stats.ConnectionStats;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Timer;
import com.alipay.oceanbase.jdbc.log.Log;
import java.util.Map;
import java.lang.reflect.InvocationHandler;
import java.sql.SQLPermission;
import java.util.concurrent.atomic.AtomicLong;

public class ConnectionImpl extends ConnectionPropertiesImpl implements ObConnection
{
    private static final long serialVersionUID = 2877471301981509474L;
    private long localConnectionId;
    private static AtomicLong globalConnectionCounter;
    private static final SQLPermission SET_NETWORK_TIMEOUT_PERM;
    private static final SQLPermission ABORT_PERM;
    public static final String JDBC_LOCAL_CHARACTER_SET_RESULTS = "jdbc.local.character_set_results";
    private MySQLConnection proxy;
    private InvocationHandler realProxy;
    private static final Object CHARSET_CONVERTER_NOT_AVAILABLE_MARKER;
    public static Map<?, ?> charsetMap;
    protected static final String DEFAULT_LOGGER_CLASS = "com.alipay.oceanbase.jdbc.log.StandardLogger";
    private static final int HISTOGRAM_BUCKETS = 20;
    private static final String LOGGER_INSTANCE_NAME = "OceanBase";
    private static Map<String, Integer> mapTransIsolationNameToValue;
    private static final Log NULL_LOGGER;
    protected static Map<?, ?> roundRobinStatsMap;
    private static final Map<String, Map<Number, String>> dynamicIndexToCollationMapByUrl;
    private static final Map<String, Map<Integer, String>> dynamicIndexToCharsetMapByUrl;
    private static final Map<String, Map<Integer, String>> customIndexToCharsetMapByUrl;
    private static final Map<String, Map<String, Integer>> customCharsetToMblenMapByUrl;
    private CacheAdapter<String, Map<String, String>> serverConfigCache;
    private long queryTimeCount;
    private double queryTimeSum;
    private double queryTimeSumSquares;
    private double queryTimeMean;
    private transient Timer cancelTimer;
    private List<Extension> connectionLifecycleInterceptors;
    private static final Constructor<?> JDBC_4_CONNECTION_CTOR;
    private static final int DEFAULT_RESULT_SET_TYPE = 1003;
    private static final int DEFAULT_RESULT_SET_CONCURRENCY = 1007;
    private ConnectionStats connStats;
    private static final Random random;
    private boolean autoCommit;
    private CacheAdapter<String, PreparedStatement.ParseInfo> cachedPreparedStatementParams;
    private String characterSetMetadata;
    private String characterSetResultsOnServer;
    private Map<String, Object> charsetConverterMap;
    private long connectionCreationTimeMillis;
    private long connectionId;
    private String database;
    private DatabaseMetaData dbmd;
    private TimeZone defaultTimeZone;
    private TimeZone sessionTimeZone;
    private ProfilerEventHandler eventSink;
    private Throwable forceClosedReason;
    private boolean hasIsolationLevels;
    private boolean hasQuotedIdentifiers;
    private String host;
    public Map<Integer, String> indexToMysqlCharset;
    public Map<Integer, String> indexToCustomMysqlCharset;
    private Map<String, Integer> mysqlCharsetToCustomMblen;
    private transient MysqlIO io;
    private boolean isClientTzUTC;
    private boolean isClosed;
    private boolean isInGlobalTx;
    private boolean isRunningOnJDK13;
    private int isolationLevel;
    private boolean isServerTzUTC;
    private long lastQueryFinishedTime;
    private transient Log log;
    private long longestQueryTimeMs;
    private boolean lowerCaseTableNames;
    private long maximumNumberTablesAccessed;
    private int sessionMaxRows;
    private long metricsLastReportedMs;
    private long minimumNumberTablesAccessed;
    private String myURL;
    private boolean needsPing;
    private int netBufferLength;
    private boolean noBackslashEscapes;
    private long numberOfPreparedExecutes;
    private long numberOfPrepares;
    private long numberOfQueriesIssued;
    private long numberOfResultSetsCreated;
    private long[] numTablesMetricsHistBreakpoints;
    private int[] numTablesMetricsHistCounts;
    private long[] oldHistBreakpoints;
    private int[] oldHistCounts;
    private final CopyOnWriteArrayList<Statement> openStatements;
    private LRUCache parsedCallableStatementCache;
    private boolean parserKnowsUnicode;
    private String password;
    private long[] perfMetricsHistBreakpoints;
    private int[] perfMetricsHistCounts;
    private String pointOfOrigin;
    private int port;
    protected Properties props;
    private boolean readInfoMsg;
    private boolean readOnly;
    protected LRUCache resultSetMetadataCache;
    private TimeZone serverTimezoneTZ;
    private Map<String, String> serverVariables;
    private long shortestQueryTimeMs;
    private double totalQueryTimeMs;
    private boolean transactionsSupported;
    private Map<String, Class<?>> typeMap;
    private boolean useAnsiQuotes;
    private String user;
    private boolean useServerPreparedStmts;
    private LRUCache serverSideStatementCheckCache;
    private LRUCache serverSideStatementCache;
    private LRUCache complexDataCache;
    private Calendar sessionCalendar;
    private static final int DEPTH_INDEX = 1;
    private static final int PARENT_TYPE_INDEX = 3;
    private static final int CHILD_TYPE_INDEX = 4;
    private static final int ATTR_NO_INDEX = 5;
    private static final int CHILD_OWNER_INDEX = 6;
    private static final int ATTR_TYPE_INDEX = 7;
    private java.sql.Connection complexConnection;
    private static final String complexTypeSql = "SELECT\n  0 DEPTH,\n  NULL PARENT_OWNER,\n  NULL PARENT_TYPE,\n  to_char(TYPE_NAME) CHILD_TYPE,\n  0 ATTR_NO,\n  SYS_CONTEXT('USERENV', 'CURRENT_USER') CHILD_TYPE_OWNER,\n  A.TYPECODE ATTR_TYPE_CODE,\n  NULL LENGTH,\n  NULL NUMBER_PRECISION,\n  NULL SCALE,\n  NULL CHARACTER_SET_NAME\nFROM\n  USER_TYPES A WHERE TYPE_NAME = ?\nUNION\n(\nWITH \nCTE_RESULT(PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME) \nAS (\n    SELECT\n      SYS_CONTEXT('USERENV','CURRENT_USER') PARENT_OWNER,\n      B.TYPE_NAME PARENT_TYPE,\n      B.ELEM_TYPE_NAME CHILD_TYPE,\n      0 ATTR_NO,\n      B.ELEM_TYPE_OWNER CHILD_TYPE_OWNER,\n      NVL(A.TYPECODE, B.ELEM_TYPE_NAME) AS ATTR_TYPE_CODE,\n      B.LENGTH LENGTH,\n      B.NUMBER_PRECISION NUMBER_PRECISION,\n      B.SCALE SCALE,\n      B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n    FROM\n      USER_COLL_TYPES B LEFT JOIN USER_TYPES A ON A.TYPE_NAME = B.ELEM_TYPE_NAME\n    UNION\n    SELECT\n      SYS_CONTEXT('USERENV','CURRENT_USER') PARENT_OWNER,\n      B.TYPE_NAME PARENT_TYPE,\n      B.ATTR_TYPE_NAME CHILD_TYPE,\n      B.ATTR_NO ATTR_NO,\n      B.ATTR_TYPE_OWNER CHILD_TYPE_OWNER,\n      NVL(A.TYPECODE, B.ATTR_TYPE_NAME) AS ATTR_TYPE_CODE,\n      B.LENGTH LENGTH,\n      B.NUMBER_PRECISION NUMBER_PRECISION,\n      B.SCALE SCALE,\n      B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n    FROM USER_TYPE_ATTRS B LEFT JOIN USER_TYPES A ON B.ATTR_TYPE_NAME = A.TYPE_NAME ORDER BY ATTR_NO\n) ,\nCTE(DEPTH, PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME)\nAS (\n  SELECT\n    1 DEPTH,\n    PARENT_OWNER,\n    PARENT_TYPE,\n    CHILD_TYPE,\n    ATTR_NO,\n    CHILD_TYPE_OWNER,\n    ATTR_TYPE_CODE,\n    LENGTH,\n    NUMBER_PRECISION,\n    SCALE, CHARACTER_SET_NAME\n  FROM CTE_RESULT WHERE PARENT_TYPE = ?\n  UNION ALL\n  SELECT\n    DEPTH + 1 DEPTH,\n    CTE_RESULT.PARENT_OWNER,\n    CTE_RESULT.PARENT_TYPE,\n    CTE_RESULT.CHILD_TYPE,\n    CTE_RESULT.ATTR_NO,\n    CTE_RESULT.CHILD_TYPE_OWNER,\n    CTE_RESULT.ATTR_TYPE_CODE,\n    CTE_RESULT.LENGTH,\n    CTE_RESULT.NUMBER_PRECISION,\n    CTE_RESULT.SCALE,\n    CTE_RESULT.CHARACTER_SET_NAME\n  FROM CTE_RESULT INNER JOIN CTE ON CTE_RESULT.PARENT_TYPE = CTE.CHILD_TYPE\n)\nSELECT * FROM CTE\n);";
    private static final String complexAllTypeSql = "SELECT\n    0 DEPTH,\n    NULL PARENT_OWNER,\n    NULL PARENT_TYPE,\n    to_char(TYPE_NAME) CHILD_TYPE,\n    0 ATTR_NO,\n    OWNER CHILD_TYPE_OWNER,\n    A.TYPECODE ATTR_TYPE_CODE,\n    NULL LENGTH,\n    NULL NUMBER_PRECISION,\n    NULL SCALE,\n    NULL CHARACTER_SET_NAME\n  FROM\n    ALL_TYPES A WHERE TYPE_NAME = ? AND OWNER = ?\n  UNION\n  (\n  WITH\n  CTE_RESULT(PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME)\n  AS (\n      SELECT\n        B.OWNER PARENT_OWNER,\n        B.TYPE_NAME PARENT_TYPE,\n        B.ELEM_TYPE_NAME CHILD_TYPE,\n        0 ATTR_NO,\n        B.ELEM_TYPE_OWNER CHILD_TYPE_OWNER,\n        NVL(A.TYPECODE, B.ELEM_TYPE_NAME) AS ATTR_TYPE_CODE,\n        B.LENGTH LENGTH,\n        B.NUMBER_PRECISION NUMBER_PRECISION,\n        B.SCALE SCALE,\n        B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n      FROM\n        ALL_COLL_TYPES B LEFT JOIN ALL_TYPES A ON A.TYPE_NAME = B.ELEM_TYPE_NAME AND A.OWNER = B.ELEM_TYPE_OWNER\n      UNION\n      SELECT\n        B.OWNER PARENT_OWNER,\n        B.TYPE_NAME PARENT_TYPE,\n        B.ATTR_TYPE_NAME CHILD_TYPE,\n        B.ATTR_NO ATTR_NO,\n        B.ATTR_TYPE_OWNER CHILD_TYPE_OWNER,\n        NVL(A.TYPECODE, B.ATTR_TYPE_NAME) AS ATTR_TYPE_CODE,\n        B.LENGTH LENGTH,\n        B.NUMBER_PRECISION NUMBER_PRECISION,\n        B.SCALE SCALE,\n        B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n      FROM ALL_TYPE_ATTRS B LEFT JOIN ALL_TYPES A ON A.TYPE_NAME = B.ATTR_TYPE_NAME AND A.OWNER = B.ATTR_TYPE_OWNER ORDER BY ATTR_NO\n  ) ,\n  CTE(DEPTH, PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME)\n  AS (\n    SELECT\n      1 DEPTH,\n      PARENT_OWNER,\n      PARENT_TYPE,\n      CHILD_TYPE,\n      ATTR_NO,\n      CHILD_TYPE_OWNER,\n      ATTR_TYPE_CODE,\n      LENGTH,\n      NUMBER_PRECISION,\n      SCALE, CHARACTER_SET_NAME\n    FROM CTE_RESULT WHERE PARENT_TYPE = ? AND PARENT_OWNER = ?\n    UNION ALL\n    SELECT\n      DEPTH + 1 DEPTH,\n      CTE_RESULT.PARENT_OWNER,\n      CTE_RESULT.PARENT_TYPE,\n      CTE_RESULT.CHILD_TYPE,\n      CTE_RESULT.ATTR_NO,\n      CTE_RESULT.CHILD_TYPE_OWNER,\n      CTE_RESULT.ATTR_TYPE_CODE,\n      CTE_RESULT.LENGTH,\n      CTE_RESULT.NUMBER_PRECISION,\n      CTE_RESULT.SCALE,\n      CTE_RESULT.CHARACTER_SET_NAME\n    FROM CTE_RESULT INNER JOIN CTE ON CTE_RESULT.PARENT_TYPE = CTE.CHILD_TYPE AND CTE_RESULT.PARENT_OWNER = CTE.CHILD_TYPE_OWNER\n  )\n  SELECT * FROM CTE\n  );";
    private Calendar utcCalendar;
    private String origHostToConnectTo;
    private int origPortToConnectTo;
    private String origDatabaseToConnectTo;
    private String errorMessageEncoding;
    private boolean usePlatformCharsetConverters;
    private boolean hasTriedMasterFlag;
    private String statementComment;
    private boolean storesLowerCaseTableName;
    private List<StatementInterceptorV2> statementInterceptors;
    private boolean requiresEscapingEncoder;
    private String hostPortPair;
    protected long obGroupDatasourceId;
    private static final String SERVER_VERSION_STRING_VAR_NAME = "server_version_string";
    private int autoIncrementIncrement;
    private ExceptionInterceptor exceptionInterceptor;
    
    @Override
    public String getHost() {
        return this.host;
    }
    
    @Override
    public String getHostPortPair() {
        return (this.hostPortPair != null) ? this.hostPortPair : (this.host + ":" + this.port);
    }
    
    @Override
    public boolean isProxySet() {
        return this.proxy != null;
    }
    
    @Override
    public void setProxy(final MySQLConnection proxy) {
        this.proxy = proxy;
        this.realProxy = ((this.proxy instanceof MultiHostMySQLConnection) ? ((MultiHostMySQLConnection)proxy).getThisAsProxy() : null);
    }
    
    private MySQLConnection getProxy() {
        return (this.proxy != null) ? this.proxy : this;
    }
    
    @Deprecated
    @Override
    public MySQLConnection getLoadBalanceSafeProxy() {
        return this.getMultiHostSafeProxy();
    }
    
    @Override
    public MySQLConnection getMultiHostSafeProxy() {
        return this.getProxy();
    }
    
    @Override
    public Object getConnectionMutex() {
        return (this.realProxy != null) ? this.realProxy : this.getProxy();
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }
    
    protected static SQLException appendMessageToException(final SQLException sqlEx, final String messageToAppend, final ExceptionInterceptor interceptor) {
        final String origMessage = sqlEx.getMessage();
        final String sqlState = sqlEx.getSQLState();
        final int vendorErrorCode = sqlEx.getErrorCode();
        final StringBuilder messageBuf = new StringBuilder(origMessage.length() + messageToAppend.length());
        messageBuf.append(origMessage);
        messageBuf.append(messageToAppend);
        final SQLException sqlExceptionWithNewMessage = SQLError.createSQLException(messageBuf.toString(), sqlState, vendorErrorCode, interceptor);
        try {
            Method getStackTraceMethod = null;
            Method setStackTraceMethod = null;
            Object theStackTraceAsObject = null;
            final Class<?> stackTraceElementClass = Class.forName("java.lang.StackTraceElement");
            final Class<?> stackTraceElementArrayClass = Array.newInstance(stackTraceElementClass, new int[] { 0 }).getClass();
            getStackTraceMethod = Throwable.class.getMethod("getStackTrace", (Class<?>[])new Class[0]);
            setStackTraceMethod = Throwable.class.getMethod("setStackTrace", stackTraceElementArrayClass);
            if (getStackTraceMethod != null && setStackTraceMethod != null) {
                theStackTraceAsObject = getStackTraceMethod.invoke(sqlEx, new Object[0]);
                setStackTraceMethod.invoke(sqlExceptionWithNewMessage, theStackTraceAsObject);
            }
        }
        catch (NoClassDefFoundError noClassDefFoundError) {}
        catch (NoSuchMethodException ex) {}
        catch (Throwable t) {}
        return sqlExceptionWithNewMessage;
    }
    
    @Override
    public Timer getCancelTimer() {
        synchronized (this.getConnectionMutex()) {
            if (this.cancelTimer == null) {
                boolean createdNamedTimer = false;
                try {
                    final Constructor<Timer> ctr = Timer.class.getConstructor(String.class, Boolean.TYPE);
                    this.cancelTimer = ctr.newInstance("MySQL Statement Cancellation Timer", Boolean.TRUE);
                    createdNamedTimer = true;
                }
                catch (Throwable t) {
                    createdNamedTimer = false;
                }
                if (!createdNamedTimer) {
                    this.cancelTimer = new Timer(true);
                }
            }
            return this.cancelTimer;
        }
    }
    
    protected static Connection getInstance(final String hostToConnectTo, final int portToConnectTo, final Properties info, final String databaseToConnectTo, final String url) throws SQLException {
        if (!Util.isJdbc4()) {
            return new ConnectionImpl(hostToConnectTo, portToConnectTo, info, databaseToConnectTo, url);
        }
        return (Connection)Util.handleNewInstance(ConnectionImpl.JDBC_4_CONNECTION_CTOR, new Object[] { hostToConnectTo, portToConnectTo, info, databaseToConnectTo, url }, null);
    }
    
    protected static synchronized int getNextRoundRobinHostIndex(final String url, final List<?> hostList) {
        final int indexRange = hostList.size();
        final int index = ConnectionImpl.random.nextInt(indexRange);
        return index;
    }
    
    private static boolean nullSafeCompare(final String s1, final String s2) {
        return (s1 == null && s2 == null) || ((s1 != null || s2 == null) && s1 != null && s1.equals(s2));
    }
    
    protected ConnectionImpl() {
        this.localConnectionId = -1L;
        this.proxy = null;
        this.realProxy = null;
        this.connStats = new ConnectionStats();
        this.autoCommit = true;
        this.characterSetMetadata = null;
        this.characterSetResultsOnServer = null;
        this.charsetConverterMap = new HashMap<String, Object>(CharsetMapping.getNumberOfCharsetsConfigured());
        this.connectionCreationTimeMillis = 0L;
        this.database = null;
        this.dbmd = null;
        this.sessionTimeZone = null;
        this.hasIsolationLevels = false;
        this.hasQuotedIdentifiers = false;
        this.host = null;
        this.indexToMysqlCharset = new HashMap<Integer, String>();
        this.indexToCustomMysqlCharset = null;
        this.mysqlCharsetToCustomMblen = null;
        this.io = null;
        this.isClientTzUTC = false;
        this.isClosed = true;
        this.isInGlobalTx = false;
        this.isRunningOnJDK13 = false;
        this.isolationLevel = 2;
        this.isServerTzUTC = false;
        this.lastQueryFinishedTime = 0L;
        this.log = ConnectionImpl.NULL_LOGGER;
        this.longestQueryTimeMs = 0L;
        this.lowerCaseTableNames = false;
        this.maximumNumberTablesAccessed = 0L;
        this.sessionMaxRows = -1;
        this.minimumNumberTablesAccessed = Long.MAX_VALUE;
        this.myURL = null;
        this.needsPing = false;
        this.netBufferLength = 16384;
        this.noBackslashEscapes = false;
        this.numberOfPreparedExecutes = 0L;
        this.numberOfPrepares = 0L;
        this.numberOfQueriesIssued = 0L;
        this.numberOfResultSetsCreated = 0L;
        this.oldHistBreakpoints = null;
        this.oldHistCounts = null;
        this.openStatements = new CopyOnWriteArrayList<Statement>();
        this.parserKnowsUnicode = false;
        this.password = null;
        this.port = 3306;
        this.props = null;
        this.readInfoMsg = false;
        this.readOnly = false;
        this.serverTimezoneTZ = null;
        this.serverVariables = null;
        this.shortestQueryTimeMs = Long.MAX_VALUE;
        this.totalQueryTimeMs = 0.0;
        this.transactionsSupported = false;
        this.useAnsiQuotes = false;
        this.user = null;
        this.useServerPreparedStmts = false;
        this.complexConnection = null;
        this.errorMessageEncoding = "Cp1252";
        this.hasTriedMasterFlag = false;
        this.statementComment = null;
        this.obGroupDatasourceId = -1L;
        this.autoIncrementIncrement = 0;
    }
    
    public ConnectionImpl(final String hostToConnectTo, final int portToConnectTo, final Properties info, String databaseToConnectTo, final String url) throws SQLException {
        this.localConnectionId = -1L;
        this.proxy = null;
        this.realProxy = null;
        this.connStats = new ConnectionStats();
        this.autoCommit = true;
        this.characterSetMetadata = null;
        this.characterSetResultsOnServer = null;
        this.charsetConverterMap = new HashMap<String, Object>(CharsetMapping.getNumberOfCharsetsConfigured());
        this.connectionCreationTimeMillis = 0L;
        this.database = null;
        this.dbmd = null;
        this.sessionTimeZone = null;
        this.hasIsolationLevels = false;
        this.hasQuotedIdentifiers = false;
        this.host = null;
        this.indexToMysqlCharset = new HashMap<Integer, String>();
        this.indexToCustomMysqlCharset = null;
        this.mysqlCharsetToCustomMblen = null;
        this.io = null;
        this.isClientTzUTC = false;
        this.isClosed = true;
        this.isInGlobalTx = false;
        this.isRunningOnJDK13 = false;
        this.isolationLevel = 2;
        this.isServerTzUTC = false;
        this.lastQueryFinishedTime = 0L;
        this.log = ConnectionImpl.NULL_LOGGER;
        this.longestQueryTimeMs = 0L;
        this.lowerCaseTableNames = false;
        this.maximumNumberTablesAccessed = 0L;
        this.sessionMaxRows = -1;
        this.minimumNumberTablesAccessed = Long.MAX_VALUE;
        this.myURL = null;
        this.needsPing = false;
        this.netBufferLength = 16384;
        this.noBackslashEscapes = false;
        this.numberOfPreparedExecutes = 0L;
        this.numberOfPrepares = 0L;
        this.numberOfQueriesIssued = 0L;
        this.numberOfResultSetsCreated = 0L;
        this.oldHistBreakpoints = null;
        this.oldHistCounts = null;
        this.openStatements = new CopyOnWriteArrayList<Statement>();
        this.parserKnowsUnicode = false;
        this.password = null;
        this.port = 3306;
        this.props = null;
        this.readInfoMsg = false;
        this.readOnly = false;
        this.serverTimezoneTZ = null;
        this.serverVariables = null;
        this.shortestQueryTimeMs = Long.MAX_VALUE;
        this.totalQueryTimeMs = 0.0;
        this.transactionsSupported = false;
        this.useAnsiQuotes = false;
        this.user = null;
        this.useServerPreparedStmts = false;
        this.complexConnection = null;
        this.errorMessageEncoding = "Cp1252";
        this.hasTriedMasterFlag = false;
        this.statementComment = null;
        this.obGroupDatasourceId = -1L;
        this.autoIncrementIncrement = 0;
        this.connectionCreationTimeMillis = System.currentTimeMillis();
        this.localConnectionId = ConnectionImpl.globalConnectionCounter.getAndIncrement();
        if (databaseToConnectTo == null) {
            databaseToConnectTo = "";
        }
        this.origHostToConnectTo = hostToConnectTo;
        this.origPortToConnectTo = portToConnectTo;
        this.origDatabaseToConnectTo = databaseToConnectTo;
        try {
            Blob.class.getMethod("truncate", Long.TYPE);
            this.isRunningOnJDK13 = false;
        }
        catch (NoSuchMethodException nsme) {
            this.isRunningOnJDK13 = true;
        }
        this.sessionCalendar = new GregorianCalendar();
        (this.utcCalendar = new GregorianCalendar()).setTimeZone(TimeZone.getTimeZone("GMT"));
        this.log = LogFactory.getLogger(this.getLogger(), "OceanBase", this.getExceptionInterceptor());
        if (NonRegisteringDriver.isHostPropertiesList(hostToConnectTo)) {
            final Properties hostSpecificProps = NonRegisteringDriver.expandHostKeyValues(hostToConnectTo);
            final Enumeration<?> propertyNames = hostSpecificProps.propertyNames();
            while (propertyNames.hasMoreElements()) {
                final String propertyName = propertyNames.nextElement().toString();
                final String propertyValue = hostSpecificProps.getProperty(propertyName);
                info.setProperty(propertyName, propertyValue);
            }
        }
        else if (hostToConnectTo == null) {
            this.host = "localhost";
            this.hostPortPair = this.host + ":" + portToConnectTo;
        }
        else {
            this.host = hostToConnectTo;
            if (hostToConnectTo.indexOf(":") == -1) {
                this.hostPortPair = this.host + ":" + portToConnectTo;
            }
            else {
                this.hostPortPair = this.host;
            }
        }
        this.port = portToConnectTo;
        this.database = databaseToConnectTo;
        this.myURL = url;
        this.user = info.getProperty("user");
        this.password = info.getProperty("password");
        if (this.user == null || this.user.equals("")) {
            this.user = "";
        }
        if (this.password == null) {
            this.password = "";
        }
        this.initializeDriverProperties(this.props = info);
        this.defaultTimeZone = TimeUtil.getDefaultTimeZone(this.getCacheDefaultTimezone());
        this.isClientTzUTC = (!this.defaultTimeZone.useDaylightTime() && this.defaultTimeZone.getRawOffset() == 0);
        if (this.getUseUsageAdvisor()) {
            this.pointOfOrigin = LogUtils.findCallingClassAndMethod(new Throwable());
        }
        else {
            this.pointOfOrigin = "";
        }
        try {
            this.dbmd = this.getMetaData(false, false);
            this.initializeSafeStatementInterceptors();
            this.createNewIO(false);
            this.unSafeStatementInterceptors();
            if (url.startsWith("jdbc:oceanbase:oracle://") && !this.isOracleMode()) {
                throw new SQLException("Cannot connect to OceanBase server in MySQL mode by using jdbc:oceanbase:oracle:// prefix");
            }
        }
        catch (SQLException ex) {
            this.cleanup(ex);
            throw ex;
        }
        catch (Exception ex2) {
            this.cleanup(ex2);
            final StringBuilder mesg = new StringBuilder(128);
            if (!this.getParanoid()) {
                mesg.append("Cannot connect to OceanBase server on ");
                mesg.append(this.host);
                mesg.append(":");
                mesg.append(this.port);
                mesg.append(".\n\n");
                mesg.append("Make sure that there is a OceanBase server ");
                mesg.append("running on the machine/port you are trying ");
                mesg.append("to connect to and that the machine this software is running on ");
                mesg.append("is able to connect to this host/port (i.e. not firewalled). ");
                mesg.append("Also make sure that the server has not been started with the --skip-networking ");
                mesg.append("flag.\n\n");
            }
            else {
                mesg.append("Unable to connect to database.");
            }
            final SQLException sqlEx = SQLError.createSQLException(mesg.toString(), "08S01", this.getExceptionInterceptor());
            sqlEx.initCause(ex2);
            throw sqlEx;
        }
        NonRegisteringDriver.trackConnection(this);
        this.getLog().logInfo(String.format("success to create connection, ip=%s, port=%d, database=%s, url=%s", hostToConnectTo, portToConnectTo, databaseToConnectTo, url));
    }
    
    @Override
    public void unSafeStatementInterceptors() throws SQLException {
        final ArrayList<StatementInterceptorV2> unSafedStatementInterceptors = new ArrayList<StatementInterceptorV2>(this.statementInterceptors.size());
        for (int i = 0; i < this.statementInterceptors.size(); ++i) {
            final NoSubInterceptorWrapper wrappedInterceptor = this.statementInterceptors.get(i);
            unSafedStatementInterceptors.add(wrappedInterceptor.getUnderlyingInterceptor());
        }
        this.statementInterceptors = unSafedStatementInterceptors;
        if (this.io != null) {
            this.io.setStatementInterceptors(this.statementInterceptors);
        }
    }
    
    @Override
    public void initializeSafeStatementInterceptors() throws SQLException {
        this.isClosed = false;
        final List<Extension> unwrappedInterceptors = Util.loadExtensions(this, this.props, this.getStatementInterceptors(), "MysqlIo.BadStatementInterceptor", this.getExceptionInterceptor());
        this.statementInterceptors = new ArrayList<StatementInterceptorV2>(unwrappedInterceptors.size());
        for (int i = 0; i < unwrappedInterceptors.size(); ++i) {
            final Extension interceptor = unwrappedInterceptors.get(i);
            if (interceptor instanceof StatementInterceptor) {
                if (ReflectiveStatementInterceptorAdapter.getV2PostProcessMethod(interceptor.getClass()) != null) {
                    this.statementInterceptors.add(new NoSubInterceptorWrapper(new ReflectiveStatementInterceptorAdapter((StatementInterceptor)interceptor)));
                }
                else {
                    this.statementInterceptors.add(new NoSubInterceptorWrapper(new V1toV2StatementInterceptorAdapter((StatementInterceptor)interceptor)));
                }
            }
            else {
                this.statementInterceptors.add(new NoSubInterceptorWrapper((StatementInterceptorV2)interceptor));
            }
        }
    }
    
    @Override
    public List<StatementInterceptorV2> getStatementInterceptorsInstances() {
        return this.statementInterceptors;
    }
    
    private void addToHistogram(final int[] histogramCounts, final long[] histogramBreakpoints, final long value, final int numberOfTimes, final long currentLowerBound, final long currentUpperBound) {
        if (histogramCounts == null) {
            this.createInitialHistogram(histogramBreakpoints, currentLowerBound, currentUpperBound);
        }
        else {
            for (int i = 0; i < 20; ++i) {
                if (histogramBreakpoints[i] >= value) {
                    final int n = i;
                    histogramCounts[n] += numberOfTimes;
                    break;
                }
            }
        }
    }
    
    private void addToPerformanceHistogram(final long value, final int numberOfTimes) {
        this.checkAndCreatePerformanceHistogram();
        this.addToHistogram(this.perfMetricsHistCounts, this.perfMetricsHistBreakpoints, value, numberOfTimes, (this.shortestQueryTimeMs == Long.MAX_VALUE) ? 0L : this.shortestQueryTimeMs, this.longestQueryTimeMs);
    }
    
    private void addToTablesAccessedHistogram(final long value, final int numberOfTimes) {
        this.checkAndCreateTablesAccessedHistogram();
        this.addToHistogram(this.numTablesMetricsHistCounts, this.numTablesMetricsHistBreakpoints, value, numberOfTimes, (this.minimumNumberTablesAccessed == Long.MAX_VALUE) ? 0L : this.minimumNumberTablesAccessed, this.maximumNumberTablesAccessed);
    }
    
    private void buildCollationMapping() throws SQLException {
        Map<Integer, String> indexToCharset = null;
        Map<Number, String> sortedCollationMap = null;
        Map<Integer, String> customCharset = null;
        Map<String, Integer> customMblen = null;
        if (this.getCacheServerConfiguration()) {
            synchronized (ConnectionImpl.dynamicIndexToCharsetMapByUrl) {
                indexToCharset = ConnectionImpl.dynamicIndexToCharsetMapByUrl.get(this.getURL());
                sortedCollationMap = ConnectionImpl.dynamicIndexToCollationMapByUrl.get(this.getURL());
                customCharset = ConnectionImpl.customIndexToCharsetMapByUrl.get(this.getURL());
                customMblen = ConnectionImpl.customCharsetToMblenMapByUrl.get(this.getURL());
            }
        }
        if (indexToCharset == null) {
            indexToCharset = new HashMap<Integer, String>();
            if (this.versionMeetsMinimum(4, 1, 0) && this.getDetectCustomCollations()) {
                java.sql.Statement stmt = null;
                ResultSet results = null;
                try {
                    sortedCollationMap = new TreeMap<Number, String>();
                    customCharset = new HashMap<Integer, String>();
                    customMblen = new HashMap<String, Integer>();
                    stmt = this.getMetadataSafeStatement();
                    try {
                        results = stmt.executeQuery("SHOW COLLATION");
                        if (this.versionMeetsMinimum(5, 0, 0)) {
                            Util.resultSetToMap(sortedCollationMap, results, 3, 2);
                        }
                        else {
                            while (results.next()) {
                                sortedCollationMap.put(results.getLong(3), results.getString(2));
                            }
                        }
                    }
                    catch (SQLException ex) {
                        if (ex.getErrorCode() != 1820 || this.getDisconnectOnExpiredPasswords()) {
                            throw ex;
                        }
                    }
                    for (final Map.Entry<Number, String> indexEntry : sortedCollationMap.entrySet()) {
                        final int collationIndex = indexEntry.getKey().intValue();
                        final String charsetName = indexEntry.getValue();
                        indexToCharset.put(collationIndex, charsetName);
                        if (collationIndex >= 255 || !charsetName.equals(CharsetMapping.getMysqlCharsetNameForCollationIndex(collationIndex))) {
                            customCharset.put(collationIndex, charsetName);
                        }
                        if (!CharsetMapping.CHARSET_NAME_TO_CHARSET.containsKey(charsetName)) {
                            customMblen.put(charsetName, null);
                        }
                    }
                    if (customMblen.size() > 0) {
                        try {
                            results = stmt.executeQuery("SHOW CHARACTER SET");
                            while (results.next()) {
                                final String charsetName2 = results.getString("Charset");
                                if (customMblen.containsKey(charsetName2)) {
                                    customMblen.put(charsetName2, results.getInt("Maxlen"));
                                }
                            }
                        }
                        catch (SQLException ex) {
                            if (ex.getErrorCode() != 1820 || this.getDisconnectOnExpiredPasswords()) {
                                throw ex;
                            }
                        }
                    }
                    if (this.getCacheServerConfiguration()) {
                        synchronized (ConnectionImpl.dynamicIndexToCharsetMapByUrl) {
                            ConnectionImpl.dynamicIndexToCharsetMapByUrl.put(this.getURL(), indexToCharset);
                            ConnectionImpl.dynamicIndexToCollationMapByUrl.put(this.getURL(), sortedCollationMap);
                            ConnectionImpl.customIndexToCharsetMapByUrl.put(this.getURL(), customCharset);
                            ConnectionImpl.customCharsetToMblenMapByUrl.put(this.getURL(), customMblen);
                        }
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
                finally {
                    if (results != null) {
                        try {
                            results.close();
                        }
                        catch (SQLException ex3) {}
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                        }
                        catch (SQLException ex4) {}
                    }
                }
            }
            else {
                for (int i = 1; i < 255; ++i) {
                    indexToCharset.put(i, CharsetMapping.getMysqlCharsetNameForCollationIndex(i));
                }
                if (this.getCacheServerConfiguration()) {
                    synchronized (ConnectionImpl.dynamicIndexToCharsetMapByUrl) {
                        ConnectionImpl.dynamicIndexToCharsetMapByUrl.put(this.getURL(), indexToCharset);
                    }
                }
            }
        }
        this.indexToMysqlCharset = Collections.unmodifiableMap((Map<? extends Integer, ? extends String>)indexToCharset);
        if (customCharset != null) {
            this.indexToCustomMysqlCharset = Collections.unmodifiableMap((Map<? extends Integer, ? extends String>)customCharset);
        }
        if (customMblen != null) {
            this.mysqlCharsetToCustomMblen = Collections.unmodifiableMap((Map<? extends String, ? extends Integer>)customMblen);
        }
    }
    
    private boolean canHandleAsServerPreparedStatement(final String sql) throws SQLException {
        if (sql == null || sql.length() == 0) {
            return true;
        }
        if (!this.useServerPreparedStmts) {
            return false;
        }
        if (this.getCachePreparedStatements()) {
            synchronized (this.serverSideStatementCheckCache) {
                final Boolean flag = ((LinkedHashMap<K, Boolean>)this.serverSideStatementCheckCache).get(sql);
                if (flag != null) {
                    return flag;
                }
                final boolean canHandle = this.canHandleAsServerPreparedStatementNoCache(sql);
                if (sql.length() < this.getPreparedStatementCacheSqlLimit()) {
                    ((HashMap<String, Boolean>)this.serverSideStatementCheckCache).put(sql, canHandle ? Boolean.TRUE : Boolean.FALSE);
                }
                return canHandle;
            }
        }
        return this.canHandleAsServerPreparedStatementNoCache(sql);
    }
    
    private boolean canHandleAsServerPreparedStatementNoCache(final String sql) throws SQLException {
        if (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "CALL")) {
            return true;
        }
        boolean canHandleAsStatement = true;
        if (!this.versionMeetsMinimum(5, 0, 7) && (StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "SELECT") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "DELETE") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "INSERT") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "UPDATE") || StringUtils.startsWithIgnoreCaseAndNonAlphaNumeric(sql, "REPLACE"))) {
            int currentPos = 0;
            final int statementLength = sql.length();
            final int lastPosToLook = statementLength - 7;
            final boolean allowBackslashEscapes = !this.noBackslashEscapes;
            final String quoteChar = this.useAnsiQuotes ? "\"" : "'";
            boolean foundLimitWithPlaceholder = false;
            while (currentPos < lastPosToLook) {
                final int limitStart = StringUtils.indexOfIgnoreCase(currentPos, sql, "LIMIT ", quoteChar, quoteChar, allowBackslashEscapes ? StringUtils.SEARCH_MODE__ALL : StringUtils.SEARCH_MODE__MRK_COM_WS);
                if (limitStart == -1) {
                    break;
                }
                for (currentPos = limitStart + 7; currentPos < statementLength; ++currentPos) {
                    final char c = sql.charAt(currentPos);
                    if (!Character.isDigit(c) && !Character.isWhitespace(c) && c != ',' && c != '?') {
                        break;
                    }
                    if (c == '?') {
                        foundLimitWithPlaceholder = true;
                        break;
                    }
                }
            }
            canHandleAsStatement = !foundLimitWithPlaceholder;
        }
        else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "XA ")) {
            canHandleAsStatement = false;
        }
        else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "CREATE TABLE")) {
            canHandleAsStatement = false;
        }
        else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "DO")) {
            canHandleAsStatement = true;
        }
        else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "SET")) {
            canHandleAsStatement = false;
        }
        else if (StringUtils.startsWithIgnoreCaseAndWs(sql, "SHOW WARNINGS") && this.versionMeetsMinimum(5, 7, 2)) {
            canHandleAsStatement = false;
        }
        return canHandleAsStatement;
    }
    
    @Override
    public void changeUser(String userName, String newPassword) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            this.checkClosed();
            if (userName == null || userName.equals("")) {
                userName = "";
            }
            if (newPassword == null) {
                newPassword = "";
            }
            this.sessionMaxRows = -1;
            try {
                this.io.changeUser(userName, newPassword, this.database);
            }
            catch (SQLException ex) {
                if (this.versionMeetsMinimum(5, 6, 13) && "28000".equals(ex.getSQLState())) {
                    this.cleanup(ex);
                }
                throw ex;
            }
            this.user = userName;
            this.password = newPassword;
            if (this.versionMeetsMinimum(4, 1, 0)) {
                this.configureClientCharacterSet(true);
            }
            this.setSessionVariables();
            this.setupServerForTruncationChecks();
        }
    }
    
    private boolean characterSetNamesMatches(final String mysqlEncodingName) {
        return mysqlEncodingName != null && mysqlEncodingName.equalsIgnoreCase(this.serverVariables.get("character_set_client")) && mysqlEncodingName.equalsIgnoreCase(this.serverVariables.get("character_set_connection"));
    }
    
    private void checkAndCreatePerformanceHistogram() {
        if (this.perfMetricsHistCounts == null) {
            this.perfMetricsHistCounts = new int[20];
        }
        if (this.perfMetricsHistBreakpoints == null) {
            this.perfMetricsHistBreakpoints = new long[20];
        }
    }
    
    private void checkAndCreateTablesAccessedHistogram() {
        if (this.numTablesMetricsHistCounts == null) {
            this.numTablesMetricsHistCounts = new int[20];
        }
        if (this.numTablesMetricsHistBreakpoints == null) {
            this.numTablesMetricsHistBreakpoints = new long[20];
        }
    }
    
    @Override
    public void checkClosed() throws SQLException {
        if (this.isClosed) {
            this.throwConnectionClosedException();
        }
    }
    
    @Override
    public void throwConnectionClosedException() throws SQLException {
        final SQLException ex = SQLError.createSQLException("No operations allowed after connection closed.", "08003", this.getExceptionInterceptor());
        if (this.forceClosedReason != null) {
            ex.initCause(this.forceClosedReason);
        }
        throw ex;
    }
    
    private void checkServerEncoding() throws SQLException {
        if (this.getUseUnicode() && this.getEncoding() != null) {
            return;
        }
        String serverCharset = this.serverVariables.get("character_set");
        if (serverCharset == null) {
            serverCharset = this.serverVariables.get("character_set_server");
        }
        String mappedServerEncoding = null;
        if (serverCharset != null) {
            try {
                mappedServerEncoding = CharsetMapping.getJavaEncodingForMysqlCharset(serverCharset);
            }
            catch (RuntimeException ex) {
                final SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                sqlEx.initCause(ex);
                throw sqlEx;
            }
        }
        if (!this.getUseUnicode() && mappedServerEncoding != null) {
            final SingleByteCharsetConverter converter = this.getCharsetConverter(mappedServerEncoding);
            if (converter != null) {
                this.setUseUnicode(true);
                this.setEncoding(mappedServerEncoding);
                return;
            }
        }
        if (serverCharset != null) {
            if (mappedServerEncoding == null && Character.isLowerCase(serverCharset.charAt(0))) {
                final char[] ach = serverCharset.toCharArray();
                ach[0] = Character.toUpperCase(serverCharset.charAt(0));
                this.setEncoding(new String(ach));
            }
            if (mappedServerEncoding == null) {
                throw SQLError.createSQLException("Unknown character encoding on server '" + serverCharset + "', use 'characterEncoding=' property  to provide correct mapping", "01S00", this.getExceptionInterceptor());
            }
            try {
                StringUtils.getBytes("abc", mappedServerEncoding);
                this.setEncoding(mappedServerEncoding);
                this.setUseUnicode(true);
            }
            catch (UnsupportedEncodingException UE) {
                throw SQLError.createSQLException("The driver can not map the character encoding '" + this.getEncoding() + "' that your server is using to a character encoding your JVM understands. You can specify this mapping manually by adding \"useUnicode=true\" as well as \"characterEncoding=[an_encoding_your_jvm_understands]\" to your JDBC URL.", "0S100", this.getExceptionInterceptor());
            }
        }
    }
    
    private void checkTransactionIsolationLevel() throws SQLException {
        String txIsolationName = null;
        if (this.versionMeetsMinimum(4, 0, 3)) {
            txIsolationName = "tx_isolation";
        }
        else {
            txIsolationName = "transaction_isolation";
        }
        final String s = this.serverVariables.get(txIsolationName);
        if (s != null) {
            final Integer intTI = ConnectionImpl.mapTransIsolationNameToValue.get(s);
            if (intTI != null) {
                this.isolationLevel = intTI;
            }
        }
    }
    
    @Override
    public void abortInternal() throws SQLException {
        if (this.io != null) {
            try {
                this.io.forceClose();
                this.io.releaseResources();
            }
            catch (Throwable t) {}
            this.io = null;
        }
        this.log.logError(String.format("Connection[%s] will be physicallly closed by abortInternal()", this.getId()), new SQLException("Connection will be physicallly closed by abortInternal()"));
        this.isClosed = true;
    }
    
    private void cleanup(final Throwable whyCleanedUp) {
        try {
            if (this.io != null) {
                if (this.isClosed()) {
                    this.io.forceClose();
                }
                else {
                    this.log.logError(String.format("Connection[%s] will be physicallly closed by cleanup()", this.getId()), whyCleanedUp);
                    this.realClose(false, false, false, whyCleanedUp);
                }
            }
        }
        catch (SQLException ex) {}
        this.isClosed = true;
    }
    
    @Deprecated
    @Override
    public void clearHasTriedMaster() {
        this.hasTriedMasterFlag = false;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
    }
    
    @Override
    public java.sql.PreparedStatement clientPrepareStatement(final String sql) throws SQLException {
        return this.clientPrepareStatement(sql, 1003, 1007);
    }
    
    @Override
    public java.sql.PreparedStatement clientPrepareStatement(final String sql, final int autoGenKeyIndex) throws SQLException {
        final java.sql.PreparedStatement pStmt = this.clientPrepareStatement(sql);
        ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
        return pStmt;
    }
    
    @Override
    public java.sql.PreparedStatement clientPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return this.clientPrepareStatement(sql, resultSetType, resultSetConcurrency, true);
    }
    
    public java.sql.PreparedStatement clientPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final boolean processEscapeCodesIfNeeded) throws SQLException {
        this.checkClosed();
        final String nativeSql = (processEscapeCodesIfNeeded && this.getProcessEscapeCodesForPrepStmts()) ? this.nativeSQL(sql) : sql;
        PreparedStatement pStmt = null;
        if (this.getCachePreparedStatements()) {
            final PreparedStatement.ParseInfo pStmtInfo = this.cachedPreparedStatementParams.get(nativeSql);
            if (pStmtInfo == null) {
                pStmt = PreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database);
                this.cachedPreparedStatementParams.put(nativeSql, pStmt.getParseInfo());
            }
            else {
                pStmt = PreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database, pStmtInfo);
            }
        }
        else {
            pStmt = PreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database);
        }
        pStmt.setResultSetType(resultSetType);
        pStmt.setResultSetConcurrency(resultSetConcurrency);
        return pStmt;
    }
    
    @Override
    public java.sql.PreparedStatement clientPrepareStatement(final String sql, final int[] autoGenKeyIndexes) throws SQLException {
        final PreparedStatement pStmt = (PreparedStatement)this.clientPrepareStatement(sql);
        pStmt.setRetrieveGeneratedKeys(autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0);
        return pStmt;
    }
    
    @Override
    public java.sql.PreparedStatement clientPrepareStatement(final String sql, final String[] autoGenKeyColNames) throws SQLException {
        final PreparedStatement pStmt = (PreparedStatement)this.clientPrepareStatement(sql);
        pStmt.setRetrieveGeneratedKeys(autoGenKeyColNames != null && autoGenKeyColNames.length > 0);
        return pStmt;
    }
    
    @Override
    public java.sql.PreparedStatement clientPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return this.clientPrepareStatement(sql, resultSetType, resultSetConcurrency, true);
    }
    
    @Override
    public void close() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            if (this.connectionLifecycleInterceptors != null) {
                new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                    @Override
                    void forEach(final Extension each) throws SQLException {
                        ((ConnectionLifecycleInterceptor)each).close();
                    }
                }.doForAll();
            }
            if (null != this.complexConnection && !this.complexConnection.isClosed()) {
                this.complexConnection.close();
                this.complexConnection = null;
            }
            this.realClose(true, true, false, null);
        }
    }
    
    public void closeServerPrepStmt(final long serverStatementId) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            if (!this.isClosed()) {
                final MysqlIO mysql = this.getIO();
                final Buffer packet = mysql.getSharedSendPacket();
                packet.writeByte((byte)25);
                packet.writeLong(serverStatementId);
                mysql.sendCommand(25, null, packet, true, null, 0);
            }
        }
    }
    
    private void closeAllOpenStatements() throws SQLException {
        SQLException postponedException = null;
        for (final Statement stmt : this.openStatements) {
            try {
                ((StatementImpl)stmt).realClose(false, true);
            }
            catch (SQLException sqlEx) {
                postponedException = sqlEx;
            }
        }
        if (postponedException != null) {
            throw postponedException;
        }
    }
    
    private void closeStatement(java.sql.Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            }
            catch (SQLException ex) {}
            stmt = null;
        }
    }
    
    @Override
    public void commit() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            this.checkClosed();
            try {
                if (this.connectionLifecycleInterceptors != null) {
                    final IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                        @Override
                        void forEach(final Extension each) throws SQLException {
                            if (!((ConnectionLifecycleInterceptor)each).commit()) {
                                this.stopIterating = true;
                            }
                        }
                    };
                    iter.doForAll();
                    if (!iter.fullIteration()) {
                        return;
                    }
                }
                if (this.autoCommit && !this.getRelaxAutoCommit()) {
                    throw SQLError.createSQLException("Can't call commit when autocommit=true", this.getExceptionInterceptor());
                }
                if (this.transactionsSupported) {
                    if (this.getUseLocalTransactionState() && this.versionMeetsMinimum(5, 0, 0) && !this.io.inTransactionOnServer()) {
                        return;
                    }
                    this.execSQL(null, "commit", -1, null, 1003, 1007, false, this.database, null, false);
                }
            }
            catch (SQLException sqlException) {
                if ("08S01".equals(sqlException.getSQLState())) {
                    throw SQLError.createSQLException("Communications link failure during commit(). Transaction resolution unknown.", "08007", this.getExceptionInterceptor());
                }
                throw sqlException;
            }
            finally {
                this.needsPing = this.getReconnectAtTxEnd();
            }
        }
    }
    
    private void configureCharsetProperties() throws SQLException {
        if (this.getEncoding() != null) {
            try {
                final String testString = "abc";
                StringUtils.getBytes(testString, this.getEncoding());
            }
            catch (UnsupportedEncodingException UE) {
                final String oldEncoding = this.getEncoding();
                try {
                    this.setEncoding(CharsetMapping.getJavaEncodingForMysqlCharset(oldEncoding));
                }
                catch (RuntimeException ex) {
                    final SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
                if (this.getEncoding() == null) {
                    throw SQLError.createSQLException("Java does not support the MySQL character encoding '" + oldEncoding + "'.", "01S00", this.getExceptionInterceptor());
                }
                try {
                    final String testString2 = "abc";
                    StringUtils.getBytes(testString2, this.getEncoding());
                }
                catch (UnsupportedEncodingException encodingEx) {
                    throw SQLError.createSQLException("Unsupported character encoding '" + this.getEncoding() + "'.", "01S00", this.getExceptionInterceptor());
                }
            }
        }
    }
    
    private boolean configureClientCharacterSet(final boolean dontCheckServerMatch) throws SQLException {
        String realJavaEncoding = this.getEncoding();
        boolean characterSetAlreadyConfigured = false;
        try {
            if (this.versionMeetsMinimum(4, 1, 0)) {
                characterSetAlreadyConfigured = true;
                this.setUseUnicode(true);
                this.configureCharsetProperties();
                realJavaEncoding = this.getEncoding();
                try {
                    if (this.props != null && this.props.getProperty("com.alipay.oceanbase.jdbc.faultInjection.serverCharsetIndex") != null) {
                        this.io.serverCharsetIndex = Integer.parseInt(this.props.getProperty("com.alipay.oceanbase.jdbc.faultInjection.serverCharsetIndex"));
                    }
                    String serverEncodingToSet = CharsetMapping.getJavaEncodingForCollationIndex(this.io.serverCharsetIndex);
                    if (serverEncodingToSet == null || serverEncodingToSet.length() == 0) {
                        if (realJavaEncoding == null) {
                            throw SQLError.createSQLException("Unknown initial character set index '" + this.io.serverCharsetIndex + "' received from server. Initial client character set can be forced via the 'characterEncoding' property.", "S1000", this.getExceptionInterceptor());
                        }
                        this.setEncoding(realJavaEncoding);
                    }
                    if (this.versionMeetsMinimum(4, 1, 0) && "ISO8859_1".equalsIgnoreCase(serverEncodingToSet)) {
                        serverEncodingToSet = "Cp1252";
                    }
                    if ("UnicodeBig".equalsIgnoreCase(serverEncodingToSet) || "UTF-16".equalsIgnoreCase(serverEncodingToSet) || "UTF-16LE".equalsIgnoreCase(serverEncodingToSet) || "UTF-32".equalsIgnoreCase(serverEncodingToSet)) {
                        serverEncodingToSet = "UTF-8";
                    }
                    this.setEncoding(serverEncodingToSet);
                }
                catch (ArrayIndexOutOfBoundsException outOfBoundsEx) {
                    if (realJavaEncoding == null) {
                        throw SQLError.createSQLException("Unknown initial character set index '" + this.io.serverCharsetIndex + "' received from server. Initial client character set can be forced via the 'characterEncoding' property.", "S1000", this.getExceptionInterceptor());
                    }
                    this.setEncoding(realJavaEncoding);
                }
                catch (SQLException ex) {
                    throw ex;
                }
                catch (RuntimeException ex2) {
                    final SQLException sqlEx = SQLError.createSQLException(ex2.toString(), "S1009", null);
                    sqlEx.initCause(ex2);
                    throw sqlEx;
                }
                if (this.getEncoding() == null) {
                    this.setEncoding("ISO8859_1");
                }
                if (this.getUseUnicode()) {
                    if (realJavaEncoding != null) {
                        if (realJavaEncoding.equalsIgnoreCase("UTF-8") || realJavaEncoding.equalsIgnoreCase("UTF8")) {
                            final boolean utf8mb4Supported = this.versionMeetsMinimum(5, 5, 2);
                            final boolean useutf8mb4 = utf8mb4Supported && CharsetMapping.UTF8MB4_INDEXES.contains(this.io.serverCharsetIndex);
                            if (!this.getUseOldUTF8Behavior()) {
                                if (dontCheckServerMatch || !this.characterSetNamesMatches("utf8") || (utf8mb4Supported && !this.characterSetNamesMatches("utf8mb4"))) {
                                    this.execSQL(null, "SET NAMES " + (useutf8mb4 ? "utf8mb4" : "utf8"), -1, null, 1003, 1007, false, this.database, null, false);
                                    this.serverVariables.put("character_set_client", useutf8mb4 ? "utf8mb4" : "utf8");
                                    this.serverVariables.put("character_set_connection", useutf8mb4 ? "utf8mb4" : "utf8");
                                }
                            }
                            else {
                                this.execSQL(null, "SET NAMES latin1", -1, null, 1003, 1007, false, this.database, null, false);
                                this.serverVariables.put("character_set_client", "latin1");
                                this.serverVariables.put("character_set_connection", "latin1");
                            }
                            this.setEncoding(realJavaEncoding);
                        }
                        else {
                            final String mysqlCharsetName = CharsetMapping.getMysqlCharsetForJavaEncoding(realJavaEncoding.toUpperCase(Locale.ENGLISH), this);
                            if (mysqlCharsetName != null && (dontCheckServerMatch || !this.characterSetNamesMatches(mysqlCharsetName))) {
                                this.execSQL(null, "SET NAMES " + mysqlCharsetName, -1, null, 1003, 1007, false, this.database, null, false);
                                this.serverVariables.put("character_set_client", mysqlCharsetName);
                                this.serverVariables.put("character_set_connection", mysqlCharsetName);
                            }
                            this.setEncoding(realJavaEncoding);
                        }
                    }
                    else if (this.getEncoding() != null) {
                        String mysqlCharsetName = this.getServerCharset();
                        if (this.getUseOldUTF8Behavior()) {
                            mysqlCharsetName = "latin1";
                        }
                        boolean ucs2 = false;
                        if ("ucs2".equalsIgnoreCase(mysqlCharsetName) || "utf16".equalsIgnoreCase(mysqlCharsetName) || "utf16le".equalsIgnoreCase(mysqlCharsetName) || "utf32".equalsIgnoreCase(mysqlCharsetName)) {
                            mysqlCharsetName = "utf8";
                            ucs2 = true;
                            if (this.getCharacterSetResults() == null) {
                                this.setCharacterSetResults("UTF-8");
                            }
                        }
                        Label_0951: {
                            if (!dontCheckServerMatch && this.characterSetNamesMatches(mysqlCharsetName)) {
                                if (!ucs2) {
                                    break Label_0951;
                                }
                            }
                            try {
                                this.execSQL(null, "SET NAMES " + mysqlCharsetName, -1, null, 1003, 1007, false, this.database, null, false);
                                this.serverVariables.put("character_set_client", mysqlCharsetName);
                                this.serverVariables.put("character_set_connection", mysqlCharsetName);
                            }
                            catch (SQLException ex3) {
                                if (ex3.getErrorCode() != 1820 || this.getDisconnectOnExpiredPasswords()) {
                                    throw ex3;
                                }
                            }
                        }
                        realJavaEncoding = this.getEncoding();
                    }
                }
                String onServer = null;
                boolean isNullOnServer = false;
                if (this.serverVariables != null) {
                    onServer = this.serverVariables.get("character_set_results");
                    isNullOnServer = (onServer == null || "NULL".equalsIgnoreCase(onServer) || onServer.length() == 0);
                }
                if (this.getCharacterSetResults() == null) {
                    if (!isNullOnServer && !this.isOracleMode()) {
                        try {
                            this.execSQL(null, "SET character_set_results = NULL", -1, null, 1003, 1007, false, this.database, null, false);
                        }
                        catch (SQLException ex3) {
                            if (ex3.getErrorCode() != 1820 || this.getDisconnectOnExpiredPasswords()) {
                                throw ex3;
                            }
                        }
                        this.serverVariables.put("jdbc.local.character_set_results", null);
                    }
                    else {
                        this.serverVariables.put("jdbc.local.character_set_results", onServer);
                    }
                }
                else {
                    if (this.getUseOldUTF8Behavior() && !this.isOracleMode()) {
                        try {
                            this.execSQL(null, "SET NAMES latin1", -1, null, 1003, 1007, false, this.database, null, false);
                            this.serverVariables.put("character_set_client", "latin1");
                            this.serverVariables.put("character_set_connection", "latin1");
                        }
                        catch (SQLException ex3) {
                            if (ex3.getErrorCode() != 1820 || this.getDisconnectOnExpiredPasswords()) {
                                throw ex3;
                            }
                        }
                    }
                    final String charsetResults = this.getCharacterSetResults();
                    String mysqlEncodingName = null;
                    if ("UTF-8".equalsIgnoreCase(charsetResults) || "UTF8".equalsIgnoreCase(charsetResults)) {
                        mysqlEncodingName = "utf8";
                    }
                    else if ("null".equalsIgnoreCase(charsetResults)) {
                        mysqlEncodingName = "NULL";
                    }
                    else {
                        mysqlEncodingName = CharsetMapping.getMysqlCharsetForJavaEncoding(charsetResults.toUpperCase(Locale.ENGLISH), this);
                    }
                    if (mysqlEncodingName == null) {
                        throw SQLError.createSQLException("Can't map " + charsetResults + " given for characterSetResults to a supported MySQL encoding.", "S1009", this.getExceptionInterceptor());
                    }
                    if (!mysqlEncodingName.equalsIgnoreCase(this.serverVariables.get("character_set_results")) && !this.isOracleMode()) {
                        final StringBuilder setBuf = new StringBuilder("SET character_set_results = ".length() + mysqlEncodingName.length());
                        setBuf.append("SET character_set_results = ").append(mysqlEncodingName);
                        try {
                            this.execSQL(null, setBuf.toString(), -1, null, 1003, 1007, false, this.database, null, false);
                        }
                        catch (SQLException ex4) {
                            if (ex4.getErrorCode() != 1820 || this.getDisconnectOnExpiredPasswords()) {
                                throw ex4;
                            }
                        }
                        this.serverVariables.put("jdbc.local.character_set_results", mysqlEncodingName);
                        if (this.versionMeetsMinimum(5, 5, 0)) {
                            this.errorMessageEncoding = charsetResults;
                        }
                    }
                    else {
                        this.serverVariables.put("jdbc.local.character_set_results", onServer);
                    }
                }
                if (this.getConnectionCollation() != null && !this.isOracleMode()) {
                    final StringBuilder setBuf2 = new StringBuilder("SET collation_connection = ".length() + this.getConnectionCollation().length());
                    setBuf2.append("SET collation_connection = ").append(this.getConnectionCollation());
                    try {
                        this.execSQL(null, setBuf2.toString(), -1, null, 1003, 1007, false, this.database, null, false);
                    }
                    catch (SQLException ex5) {
                        if (ex5.getErrorCode() != 1820 || this.getDisconnectOnExpiredPasswords()) {
                            throw ex5;
                        }
                    }
                }
            }
            else {
                realJavaEncoding = this.getEncoding();
            }
        }
        finally {
            if (!this.isOracleMode()) {
                this.setEncoding(realJavaEncoding);
            }
        }
        try {
            final CharsetEncoder enc = Charset.forName(this.getEncoding()).newEncoder();
            final CharBuffer cbuf = CharBuffer.allocate(1);
            final ByteBuffer bbuf = ByteBuffer.allocate(1);
            cbuf.put("¥");
            cbuf.position(0);
            enc.encode(cbuf, bbuf, true);
            if (bbuf.get(0) == 92) {
                this.requiresEscapingEncoder = true;
            }
            else {
                cbuf.clear();
                bbuf.clear();
                cbuf.put("\u20a9");
                cbuf.position(0);
                enc.encode(cbuf, bbuf, true);
                if (bbuf.get(0) == 92) {
                    this.requiresEscapingEncoder = true;
                }
            }
        }
        catch (UnsupportedCharsetException ucex) {
            try {
                byte[] bbuf2 = StringUtils.getBytes("¥", this.getEncoding());
                if (bbuf2[0] == 92) {
                    this.requiresEscapingEncoder = true;
                }
                else {
                    bbuf2 = StringUtils.getBytes("\u20a9", this.getEncoding());
                    if (bbuf2[0] == 92) {
                        this.requiresEscapingEncoder = true;
                    }
                }
            }
            catch (UnsupportedEncodingException ueex) {
                throw SQLError.createSQLException("Unable to use encoding: " + this.getEncoding(), "S1000", ueex, this.getExceptionInterceptor());
            }
        }
        return characterSetAlreadyConfigured;
    }
    
    private void configureTimezone() throws SQLException {
        String configuredTimeZoneOnServer = this.serverVariables.get("timezone");
        if (configuredTimeZoneOnServer == null) {
            configuredTimeZoneOnServer = this.serverVariables.get("time_zone");
            if ("SYSTEM".equalsIgnoreCase(configuredTimeZoneOnServer)) {
                configuredTimeZoneOnServer = this.serverVariables.get("system_time_zone");
            }
        }
        if (this.isOracleMode()) {
            if (null != configuredTimeZoneOnServer) {
                final String serverTimeZone = TimeUtil.getCanonicalTimezone(configuredTimeZoneOnServer, this.getExceptionInterceptor());
                if (TimeZone.getDefault().getRawOffset() != TimeZone.getTimeZone(serverTimeZone).getRawOffset()) {
                    this.setSessionTimeZone(TimeZone.getDefault().getID());
                }
                else {
                    this.sessionTimeZone = TimeZone.getDefault();
                }
            }
            else {
                this.setSessionTimeZone(TimeZone.getDefault().getID());
            }
        }
        String canonicalTimezone = this.getServerTimezone();
        Label_0194: {
            if ((this.getUseTimezone() || !this.getUseLegacyDatetimeCode()) && configuredTimeZoneOnServer != null) {
                if (canonicalTimezone != null) {
                    if (!StringUtils.isEmptyOrWhitespaceOnly(canonicalTimezone)) {
                        break Label_0194;
                    }
                }
                try {
                    canonicalTimezone = TimeUtil.getCanonicalTimezone(configuredTimeZoneOnServer, this.getExceptionInterceptor());
                }
                catch (IllegalArgumentException iae) {
                    throw SQLError.createSQLException(iae.getMessage(), "S1000", this.getExceptionInterceptor());
                }
            }
        }
        if (canonicalTimezone != null && canonicalTimezone.length() > 0) {
            this.serverTimezoneTZ = TimeZone.getTimeZone(canonicalTimezone);
            if (!canonicalTimezone.equalsIgnoreCase("GMT") && this.serverTimezoneTZ.getID().equals("GMT")) {
                throw SQLError.createSQLException("No timezone mapping entry for '" + canonicalTimezone + "'", "S1009", this.getExceptionInterceptor());
            }
            this.isServerTzUTC = (!this.serverTimezoneTZ.useDaylightTime() && this.serverTimezoneTZ.getRawOffset() == 0);
        }
    }
    
    private void createInitialHistogram(final long[] breakpoints, long lowerBound, final long upperBound) {
        double bucketSize = (upperBound - (double)lowerBound) / 20.0 * 1.25;
        if (bucketSize < 1.0) {
            bucketSize = 1.0;
        }
        for (int i = 0; i < 20; ++i) {
            breakpoints[i] = lowerBound;
            lowerBound += (long)bucketSize;
        }
    }
    
    @Override
    public void createNewIO(final boolean isForReconnect) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            final Properties mergedProps = this.exposeAsProperties(this.props);
            if (!this.getHighAvailability()) {
                this.connectOneTryOnly(isForReconnect, mergedProps);
                return;
            }
            this.connectWithRetries(isForReconnect, mergedProps);
        }
    }
    
    private void connectWithRetries(final boolean isForReconnect, final Properties mergedProps) throws SQLException {
        final double timeout = this.getInitialTimeout();
        boolean connectionGood = false;
        Exception connectionException = null;
        int attemptCount = 0;
        while (attemptCount < this.getMaxReconnects() && !connectionGood) {
            try {
                if (this.io != null) {
                    this.io.forceClose();
                }
                this.coreConnect(mergedProps);
                this.pingInternal(false, 0);
                final boolean oldAutoCommit;
                final int oldIsolationLevel;
                final boolean oldReadOnly;
                final String oldCatalog;
                synchronized (this.getConnectionMutex()) {
                    this.connectionId = this.io.getThreadId();
                    this.isClosed = false;
                    oldAutoCommit = this.getAutoCommit();
                    oldIsolationLevel = this.isolationLevel;
                    oldReadOnly = this.isReadOnly(false);
                    oldCatalog = this.getCatalog();
                    this.io.setStatementInterceptors(this.statementInterceptors);
                }
                this.initializePropsFromServer();
                if (isForReconnect) {
                    this.setAutoCommit(oldAutoCommit);
                    if (this.hasIsolationLevels) {
                        this.setTransactionIsolation(oldIsolationLevel);
                    }
                    this.setCatalog(oldCatalog);
                    this.setReadOnly(oldReadOnly);
                }
                connectionGood = true;
            }
            catch (Exception EEE) {
                connectionException = EEE;
                connectionGood = false;
                if (!connectionGood) {
                    if (attemptCount > 0) {
                        try {
                            Thread.sleep((long)timeout * 1000L);
                        }
                        catch (InterruptedException ex) {}
                    }
                    ++attemptCount;
                    continue;
                }
            }
            break;
        }
        if (!connectionGood) {
            final SQLException chainedEx = SQLError.createSQLException(Messages.getString("Connection.UnableToConnectWithRetries", new Object[] { this.getMaxReconnects() }), "08001", this.getExceptionInterceptor());
            chainedEx.initCause(connectionException);
            throw chainedEx;
        }
        if (this.getParanoid() && !this.getHighAvailability()) {
            this.password = null;
            this.user = null;
        }
        if (isForReconnect) {
            final Iterator<Statement> statementIter = this.openStatements.iterator();
            Stack<Statement> serverPreparedStatements = null;
            while (statementIter.hasNext()) {
                final Statement statementObj = statementIter.next();
                if (statementObj instanceof ServerPreparedStatement) {
                    if (serverPreparedStatements == null) {
                        serverPreparedStatements = new Stack<Statement>();
                    }
                    serverPreparedStatements.add(statementObj);
                }
            }
            if (serverPreparedStatements != null) {
                while (!serverPreparedStatements.isEmpty()) {
                    serverPreparedStatements.pop().rePrepare();
                }
            }
        }
    }
    
    private void coreConnect(final Properties mergedProps) throws SQLException, IOException {
        int newPort = 3306;
        String newHost = "localhost";
        final String protocol = mergedProps.getProperty("PROTOCOL");
        if (protocol != null) {
            if ("tcp".equalsIgnoreCase(protocol)) {
                newHost = this.normalizeHost(mergedProps.getProperty("HOST"));
                newPort = this.parsePortNumber(mergedProps.getProperty("PORT", "3306"));
            }
            else if ("pipe".equalsIgnoreCase(protocol)) {
                this.setSocketFactoryClassName(NamedPipeSocketFactory.class.getName());
                final String path = mergedProps.getProperty("PATH");
                if (path != null) {
                    mergedProps.setProperty("namedPipePath", path);
                }
            }
            else {
                newHost = this.normalizeHost(mergedProps.getProperty("HOST"));
                newPort = this.parsePortNumber(mergedProps.getProperty("PORT", "3306"));
            }
        }
        else {
            final String[] parsedHostPortPair = NonRegisteringDriver.parseHostPortPair(this.hostPortPair);
            newHost = parsedHostPortPair[0];
            newHost = this.normalizeHost(newHost);
            if (parsedHostPortPair[1] != null) {
                newPort = this.parsePortNumber(parsedHostPortPair[1]);
            }
        }
        this.port = newPort;
        this.host = newHost;
        this.sessionMaxRows = -1;
        (this.io = new MysqlIO(newHost, newPort, mergedProps, this.getSocketFactoryClassName(), this.getProxy(), this.getSocketTimeout(), this.largeRowSizeThreshold.getValueAsInt())).doHandshake(this.user, this.password, this.database);
        if (this.versionMeetsMinimum(5, 5, 0)) {
            this.errorMessageEncoding = this.io.getEncodingForHandshake();
        }
    }
    
    private String normalizeHost(final String hostname) {
        if (hostname == null || StringUtils.isEmptyOrWhitespaceOnly(hostname)) {
            return "localhost";
        }
        return hostname;
    }
    
    private int parsePortNumber(final String portAsString) throws SQLException {
        int portNumber = 3306;
        try {
            portNumber = Integer.parseInt(portAsString);
        }
        catch (NumberFormatException nfe) {
            throw SQLError.createSQLException("Illegal connection port value '" + portAsString + "'", "01S00", this.getExceptionInterceptor());
        }
        return portNumber;
    }
    
    private void connectOneTryOnly(final boolean isForReconnect, final Properties mergedProps) throws SQLException {
        Exception connectionNotEstablishedBecause = null;
        try {
            this.coreConnect(mergedProps);
            this.connectionId = this.io.getThreadId();
            this.isClosed = false;
            final boolean oldAutoCommit = this.getAutoCommit();
            final int oldIsolationLevel = this.isolationLevel;
            final boolean oldReadOnly = this.isReadOnly(false);
            final String oldCatalog = this.getCatalog();
            this.io.setStatementInterceptors(this.statementInterceptors);
            this.initializePropsFromServer();
            if (isForReconnect) {
                this.setAutoCommit(oldAutoCommit);
                if (this.hasIsolationLevels) {
                    this.setTransactionIsolation(oldIsolationLevel);
                }
                this.setCatalog(oldCatalog);
                this.setReadOnly(oldReadOnly);
            }
        }
        catch (Exception EEE) {
            if (EEE instanceof SQLException && ((SQLException)EEE).getErrorCode() == 1820 && !this.getDisconnectOnExpiredPasswords()) {
                return;
            }
            if (this.io != null) {
                this.io.forceClose();
            }
            connectionNotEstablishedBecause = EEE;
            if (EEE instanceof SQLException) {
                throw (SQLException)EEE;
            }
            final SQLException chainedEx = SQLError.createSQLException(Messages.getString("Connection.UnableToConnect"), "08001", this.getExceptionInterceptor());
            chainedEx.initCause(connectionNotEstablishedBecause);
            throw chainedEx;
        }
    }
    
    private void createPreparedStatementCaches() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            final int cacheSize = this.getPreparedStatementCacheSize();
            try {
                final Class<?> factoryClass = Class.forName(this.getParseInfoCacheFactory());
                final CacheAdapterFactory<String, PreparedStatement.ParseInfo> cacheFactory = (CacheAdapterFactory<String, PreparedStatement.ParseInfo>)factoryClass.newInstance();
                this.cachedPreparedStatementParams = cacheFactory.getInstance(this, this.myURL, this.getPreparedStatementCacheSize(), this.getPreparedStatementCacheSqlLimit(), this.props);
            }
            catch (ClassNotFoundException e) {
                final SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantFindCacheFactory", new Object[] { this.getParseInfoCacheFactory(), "parseInfoCacheFactory" }), this.getExceptionInterceptor());
                sqlEx.initCause(e);
                throw sqlEx;
            }
            catch (InstantiationException e2) {
                final SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantLoadCacheFactory", new Object[] { this.getParseInfoCacheFactory(), "parseInfoCacheFactory" }), this.getExceptionInterceptor());
                sqlEx.initCause(e2);
                throw sqlEx;
            }
            catch (IllegalAccessException e3) {
                final SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantLoadCacheFactory", new Object[] { this.getParseInfoCacheFactory(), "parseInfoCacheFactory" }), this.getExceptionInterceptor());
                sqlEx.initCause(e3);
                throw sqlEx;
            }
            if (this.getUseServerPreparedStmts()) {
                this.serverSideStatementCheckCache = new LRUCache(cacheSize);
                this.serverSideStatementCache = new LRUCache(cacheSize) {
                    private static final long serialVersionUID = 7692318650375988114L;
                    
                    @Override
                    protected boolean removeEldestEntry(final Map.Entry<Object, Object> eldest) {
                        if (this.maxElements <= 1) {
                            return false;
                        }
                        final boolean removeIt = super.removeEldestEntry(eldest);
                        if (removeIt) {
                            final ServerPreparedStatement ps = eldest.getValue();
                            ps.setClosed(ps.isCached = false);
                            try {
                                ps.close();
                            }
                            catch (SQLException ex) {}
                        }
                        return removeIt;
                    }
                };
            }
        }
    }
    
    @Override
    public java.sql.Statement createStatement() throws SQLException {
        return this.createStatement(1003, 1007);
    }
    
    @Override
    public java.sql.Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this.checkClosed();
        final StatementImpl stmt = new StatementImpl(this.getMultiHostSafeProxy(), this.database);
        stmt.setResultSetType(resultSetType);
        stmt.setResultSetConcurrency(resultSetConcurrency);
        return stmt;
    }
    
    @Override
    public java.sql.Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        if (this.getPedantic() && resultSetHoldability != 1) {
            throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", this.getExceptionInterceptor());
        }
        return this.createStatement(resultSetType, resultSetConcurrency);
    }
    
    @Override
    public void dumpTestcaseQuery(final String query) {
        System.err.println(query);
    }
    
    @Override
    public Connection duplicate() throws SQLException {
        return new ConnectionImpl(this.origHostToConnectTo, this.origPortToConnectTo, this.props, this.origDatabaseToConnectTo, this.myURL);
    }
    
    @Override
    public ResultSetInternalMethods execSQL(final StatementImpl callingStatement, final String sql, final int maxRows, final Buffer packet, final int resultSetType, final int resultSetConcurrency, final boolean streamResults, final String catalog, final Field[] cachedMetadata) throws SQLException {
        return this.execSQL(callingStatement, sql, maxRows, packet, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata, false);
    }
    
    @Override
    public ResultSetInternalMethods execSQL(final StatementImpl callingStatement, final String sql, final int maxRows, final Buffer packet, final int resultSetType, final int resultSetConcurrency, final boolean streamResults, final String catalog, final Field[] cachedMetadata, final boolean isBatch) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            long queryStartTime = 0L;
            int endOfQueryPacketPosition = 0;
            if (packet != null) {
                endOfQueryPacketPosition = packet.getPosition();
            }
            if (this.getGatherPerformanceMetrics()) {
                queryStartTime = System.currentTimeMillis();
            }
            this.lastQueryFinishedTime = 0L;
            Label_0097: {
                if (!this.getHighAvailability() || (!this.autoCommit && !this.getAutoReconnectForPools()) || !this.needsPing || isBatch) {
                    break Label_0097;
                }
                try {
                    this.pingInternal(false, 0);
                    this.needsPing = false;
                }
                catch (Exception Ex) {
                    this.createNewIO(true);
                }
                try {
                    if (packet == null) {
                        String encoding = null;
                        if (this.getUseUnicode()) {
                            encoding = this.getEncoding();
                        }
                        return this.io.sqlQueryDirect(callingStatement, sql, encoding, null, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata);
                    }
                    return this.io.sqlQueryDirect(callingStatement, null, null, packet, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, cachedMetadata);
                }
                catch (SQLException sqlE) {
                    if (this.getDumpQueriesOnException()) {
                        final String extractedSql = this.extractSqlFromPacket(sql, packet, endOfQueryPacketPosition);
                        final StringBuilder messageBuf = new StringBuilder(extractedSql.length() + 32);
                        messageBuf.append("\n\nQuery being executed when exception was thrown:\n");
                        messageBuf.append(extractedSql);
                        messageBuf.append("\n\n");
                        sqlE = appendMessageToException(sqlE, messageBuf.toString(), this.getExceptionInterceptor());
                    }
                    if (this.getHighAvailability()) {
                        this.needsPing = true;
                    }
                    else {
                        final String sqlState = sqlE.getSQLState();
                        if (sqlState != null && sqlState.equals("08S01")) {
                            this.cleanup(sqlE);
                        }
                    }
                    throw sqlE;
                }
                catch (Exception ex) {
                    if (this.getHighAvailability()) {
                        this.needsPing = true;
                    }
                    else if (ex instanceof IOException) {
                        this.cleanup(ex);
                    }
                    final SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.UnexpectedException"), "S1000", this.getExceptionInterceptor());
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
                finally {
                    if (this.getMaintainTimeStats()) {
                        this.lastQueryFinishedTime = System.currentTimeMillis();
                    }
                    if (this.getGatherPerformanceMetrics()) {
                        final long queryTime = System.currentTimeMillis() - queryStartTime;
                        this.registerQueryExecutionTime(queryTime);
                    }
                }
            }
        }
    }
    
    @Override
    public String extractSqlFromPacket(final String possibleSqlQuery, final Buffer queryPacket, final int endOfQueryPacketPosition) throws SQLException {
        String extractedSql = null;
        if (possibleSqlQuery != null) {
            if (possibleSqlQuery.length() > this.getMaxQuerySizeToLog()) {
                final StringBuilder truncatedQueryBuf = new StringBuilder(possibleSqlQuery.substring(0, this.getMaxQuerySizeToLog()));
                truncatedQueryBuf.append(Messages.getString("MysqlIO.25"));
                extractedSql = truncatedQueryBuf.toString();
            }
            else {
                extractedSql = possibleSqlQuery;
            }
        }
        if (extractedSql == null) {
            int extractPosition = endOfQueryPacketPosition;
            boolean truncated = false;
            if (endOfQueryPacketPosition > this.getMaxQuerySizeToLog()) {
                extractPosition = this.getMaxQuerySizeToLog();
                truncated = true;
            }
            extractedSql = StringUtils.toString(queryPacket.getByteBuffer(), 5, extractPosition - 5);
            if (truncated) {
                extractedSql += Messages.getString("MysqlIO.25");
            }
        }
        return extractedSql;
    }
    
    @Override
    public StringBuilder generateConnectionCommentBlock(final StringBuilder buf) {
        buf.append("/* conn id ");
        buf.append(this.getId());
        buf.append(" clock: ");
        buf.append(System.currentTimeMillis());
        buf.append(" */ ");
        return buf;
    }
    
    @Override
    public int getActiveStatementCount() {
        return this.openStatements.size();
    }
    
    @Override
    public boolean getAutoCommit() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            return this.autoCommit;
        }
    }
    
    @Override
    public Calendar getCalendarInstanceForSessionOrNew() {
        if (this.getDynamicCalendars()) {
            return Calendar.getInstance();
        }
        return this.getSessionLockedCalendar();
    }
    
    @Override
    public String getCatalog() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            return this.database;
        }
    }
    
    @Override
    public String getCharacterSetMetadata() {
        synchronized (this.getConnectionMutex()) {
            return this.characterSetMetadata;
        }
    }
    
    @Override
    public SingleByteCharsetConverter getCharsetConverter(final String javaEncodingName) throws SQLException {
        if (javaEncodingName == null) {
            return null;
        }
        if (this.usePlatformCharsetConverters) {
            return null;
        }
        SingleByteCharsetConverter converter = null;
        synchronized (this.charsetConverterMap) {
            final Object asObject = this.charsetConverterMap.get(javaEncodingName);
            if (asObject == ConnectionImpl.CHARSET_CONVERTER_NOT_AVAILABLE_MARKER) {
                return null;
            }
            converter = (SingleByteCharsetConverter)asObject;
            if (converter == null) {
                try {
                    converter = SingleByteCharsetConverter.getInstance(javaEncodingName, this);
                    if (converter == null) {
                        this.charsetConverterMap.put(javaEncodingName, ConnectionImpl.CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
                    }
                    else {
                        this.charsetConverterMap.put(javaEncodingName, converter);
                    }
                }
                catch (UnsupportedEncodingException unsupEncEx) {
                    this.charsetConverterMap.put(javaEncodingName, ConnectionImpl.CHARSET_CONVERTER_NOT_AVAILABLE_MARKER);
                    converter = null;
                }
            }
        }
        return converter;
    }
    
    @Deprecated
    @Override
    public String getCharsetNameForIndex(final int charsetIndex) throws SQLException {
        return this.getEncodingForIndex(charsetIndex);
    }
    
    @Override
    public String getEncodingForIndex(final int charsetIndex) throws SQLException {
        String javaEncoding = null;
        if (this.getUseOldUTF8Behavior()) {
            return this.getEncoding();
        }
        if (charsetIndex != -1) {
            try {
                if (this.indexToMysqlCharset.size() > 0) {
                    javaEncoding = CharsetMapping.getJavaEncodingForMysqlCharset(this.indexToMysqlCharset.get(charsetIndex), this.getEncoding());
                }
                if (javaEncoding == null) {
                    javaEncoding = CharsetMapping.getJavaEncodingForCollationIndex(charsetIndex, this.getEncoding());
                }
            }
            catch (ArrayIndexOutOfBoundsException outOfBoundsEx) {
                throw SQLError.createSQLException("Unknown character set index for field '" + charsetIndex + "' received from server.", "S1000", this.getExceptionInterceptor());
            }
            catch (RuntimeException ex) {
                final SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                sqlEx.initCause(ex);
                throw sqlEx;
            }
            if (javaEncoding == null) {
                javaEncoding = this.getEncoding();
            }
        }
        else {
            javaEncoding = this.getEncoding();
        }
        return javaEncoding;
    }
    
    @Override
    public TimeZone getDefaultTimeZone() {
        return this.getCacheDefaultTimezone() ? this.defaultTimeZone : TimeUtil.getDefaultTimeZone(false);
    }
    
    @Override
    public String getSessionTimeZone() {
        return this.sessionTimeZone.getID();
    }
    
    public void setSessionTimeZone(final String zoneID) throws SQLException {
        if (!this.isOracleMode()) {
            throw new SQLFeatureNotSupportedException();
        }
        synchronized (this.getConnectionMutex()) {
            this.checkClosed();
            boolean needSetSessionTimeZone = true;
            final TimeZone targetTimeZone = TimeZone.getTimeZone(zoneID);
            if (null != this.sessionTimeZone && targetTimeZone.getRawOffset() == this.sessionTimeZone.getRawOffset()) {
                needSetSessionTimeZone = false;
            }
            if (needSetSessionTimeZone) {
                final java.sql.Statement stmt = this.getMetadataSafeStatement();
                try {
                    final String sql = String.format("alter session set time_zone = '%s'", zoneID);
                    stmt.execute(sql);
                    this.sessionTimeZone = targetTimeZone;
                }
                catch (SQLException e) {
                    throw e;
                }
                finally {
                    if (stmt != null) {
                        stmt.close();
                    }
                }
            }
        }
    }
    
    @Override
    public String getErrorMessageEncoding() {
        return this.errorMessageEncoding;
    }
    
    @Override
    public int getHoldability() throws SQLException {
        return 2;
    }
    
    @Override
    public long getId() {
        return this.connectionId;
    }
    
    @Override
    public long getIdleFor() {
        synchronized (this.getConnectionMutex()) {
            if (this.lastQueryFinishedTime == 0L) {
                return 0L;
            }
            final long now = System.currentTimeMillis();
            final long idleTime = now - this.lastQueryFinishedTime;
            return idleTime;
        }
    }
    
    @Override
    public MysqlIO getIO() throws SQLException {
        if (this.io == null || this.isClosed) {
            throw SQLError.createSQLException("Operation not allowed on closed connection", "08003", this.getExceptionInterceptor());
        }
        return this.io;
    }
    
    @Override
    public boolean isAlive() {
        return null != this.io && !this.isClosed;
    }
    
    @Override
    public Log getLog() throws SQLException {
        return this.log;
    }
    
    @Override
    public int getMaxBytesPerChar(final String javaCharsetName) throws SQLException {
        return this.getMaxBytesPerChar(null, javaCharsetName);
    }
    
    @Override
    public int getMaxBytesPerChar(final Integer charsetIndex, final String javaCharsetName) throws SQLException {
        String charset = null;
        int res = 1;
        try {
            if (this.indexToCustomMysqlCharset != null) {
                charset = this.indexToCustomMysqlCharset.get(charsetIndex);
            }
            if (charset == null) {
                charset = CharsetMapping.getMysqlCharsetNameForCollationIndex(charsetIndex);
            }
            if (charset == null) {
                charset = CharsetMapping.getMysqlCharsetForJavaEncoding(javaCharsetName, this);
            }
            Integer mblen = null;
            if (this.mysqlCharsetToCustomMblen != null) {
                mblen = this.mysqlCharsetToCustomMblen.get(charset);
            }
            if (mblen == null) {
                mblen = CharsetMapping.getMblen(charset);
            }
            if (mblen != null) {
                res = mblen;
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
        return res;
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return this.getMetaData(true, true);
    }
    
    private DatabaseMetaData getMetaData(final boolean checkClosed, final boolean checkForInfoSchema) throws SQLException {
        if (checkClosed) {
            this.checkClosed();
        }
        return com.alipay.oceanbase.jdbc.DatabaseMetaData.getInstance(this.getMultiHostSafeProxy(), this.database, checkForInfoSchema);
    }
    
    @Override
    public java.sql.Statement getMetadataSafeStatement() throws SQLException {
        final java.sql.Statement stmt = this.createStatement();
        if (stmt.getMaxRows() != 0) {
            stmt.setMaxRows(0);
        }
        stmt.setEscapeProcessing(false);
        if (stmt.getFetchSize() != 0) {
            stmt.setFetchSize(0);
        }
        return stmt;
    }
    
    @Override
    public int getNetBufferLength() {
        return this.netBufferLength;
    }
    
    @Deprecated
    @Override
    public String getServerCharacterEncoding() {
        return this.getServerCharset();
    }
    
    @Override
    public String getServerCharset() {
        if (this.io.versionMeetsMinimum(4, 1, 0)) {
            String charset = null;
            if (this.indexToCustomMysqlCharset != null) {
                charset = this.indexToCustomMysqlCharset.get(this.io.serverCharsetIndex);
            }
            if (charset == null) {
                charset = CharsetMapping.getMysqlCharsetNameForCollationIndex(this.io.serverCharsetIndex);
            }
            return (charset != null) ? charset : this.serverVariables.get("character_set_server");
        }
        return this.serverVariables.get("character_set");
    }
    
    @Override
    public int getServerMajorVersion() {
        return this.io.getServerMajorVersion();
    }
    
    @Override
    public int getServerMinorVersion() {
        return this.io.getServerMinorVersion();
    }
    
    @Override
    public int getServerSubMinorVersion() {
        return this.io.getServerSubMinorVersion();
    }
    
    @Override
    public TimeZone getServerTimezoneTZ() {
        return this.serverTimezoneTZ;
    }
    
    @Override
    public String getServerVariable(final String variableName) {
        if (this.serverVariables != null) {
            return this.serverVariables.get(variableName);
        }
        return null;
    }
    
    @Override
    public String getServerVersion() {
        return this.io.getServerVersion();
    }
    
    @Override
    public Calendar getSessionLockedCalendar() {
        return this.sessionCalendar;
    }
    
    @Override
    public int getTransactionIsolation() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            if (this.hasIsolationLevels && !this.getUseLocalSessionState()) {
                java.sql.Statement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = this.getMetadataSafeStatement();
                    String query = null;
                    int offset = 0;
                    if (!this.io.isOracleMode() && this.versionMeetsMinimum(4, 0, 3)) {
                        query = "SELECT @@session.tx_isolation";
                        offset = 1;
                    }
                    else {
                        query = "SHOW VARIABLES WHERE Variable_name = 'tx_isolation'";
                        offset = 2;
                    }
                    rs = stmt.executeQuery(query);
                    if (rs.next()) {
                        final String s = rs.getString(offset);
                        if (s != null) {
                            final Integer intTI = ConnectionImpl.mapTransIsolationNameToValue.get(s);
                            if (intTI != null) {
                                return intTI;
                            }
                        }
                        throw SQLError.createSQLException("Could not map transaction isolation '" + s + " to a valid JDBC level.", "S1000", this.getExceptionInterceptor());
                    }
                    throw SQLError.createSQLException("Could not retrieve transaction isolation level from server", "S1000", this.getExceptionInterceptor());
                }
                finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        }
                        catch (Exception ex) {}
                        rs = null;
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                        }
                        catch (Exception ex2) {}
                        stmt = null;
                    }
                }
            }
            return this.isolationLevel;
        }
    }
    
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            if (this.typeMap == null) {
                this.typeMap = new HashMap<String, Class<?>>();
            }
            return this.typeMap;
        }
    }
    
    @Override
    public String getURL() {
        return this.myURL;
    }
    
    @Override
    public String getUser() {
        return this.user;
    }
    
    @Override
    public Calendar getUtcCalendar() {
        return this.utcCalendar;
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }
    
    @Override
    public boolean hasSameProperties(final Connection c) {
        return this.props.equals(c.getProperties());
    }
    
    @Override
    public Properties getProperties() {
        return this.props;
    }
    
    @Deprecated
    @Override
    public boolean hasTriedMaster() {
        return this.hasTriedMasterFlag;
    }
    
    @Override
    public void incrementNumberOfPreparedExecutes() {
        if (this.getGatherPerformanceMetrics()) {
            ++this.numberOfPreparedExecutes;
            ++this.numberOfQueriesIssued;
        }
    }
    
    @Override
    public void incrementNumberOfPrepares() {
        if (this.getGatherPerformanceMetrics()) {
            ++this.numberOfPrepares;
        }
    }
    
    @Override
    public void incrementNumberOfResultSetsCreated() {
        if (this.getGatherPerformanceMetrics()) {
            ++this.numberOfResultSetsCreated;
        }
    }
    
    private void initializeDriverProperties(final Properties info) throws SQLException {
        this.initializeProperties(info);
        final String exceptionInterceptorClasses = this.getExceptionInterceptors();
        if (exceptionInterceptorClasses != null && !"".equals(exceptionInterceptorClasses)) {
            this.exceptionInterceptor = new ExceptionInterceptorChain(exceptionInterceptorClasses);
        }
        this.usePlatformCharsetConverters = this.getUseJvmCharsetConverters();
        this.log = LogFactory.getLogger(this.getLogger(), "OceanBase", this.getExceptionInterceptor());
        if (this.getProfileSql() || this.getUseUsageAdvisor()) {
            this.eventSink = ProfilerEventHandlerFactory.getInstance(this.getMultiHostSafeProxy());
        }
        if (this.getCachePreparedStatements()) {
            this.createPreparedStatementCaches();
        }
        if (this.getNoDatetimeStringSync() && this.getUseTimezone()) {
            throw SQLError.createSQLException("Can't enable noDatetimeStringSync and useTimezone configuration properties at the same time", "01S00", this.getExceptionInterceptor());
        }
        if (this.getCacheCallableStatements()) {
            this.parsedCallableStatementCache = new LRUCache(this.getCallableStatementCacheSize());
        }
        if (this.getAllowMultiQueries()) {
            this.setCacheResultSetMetadata(false);
        }
        if (this.getCacheResultSetMetadata()) {
            this.resultSetMetadataCache = new LRUCache(this.getMetadataCacheSize());
        }
        if (this.getSocksProxyHost() != null) {
            this.setSocketFactoryClassName("com.alipay.oceanbase.jdbc.SocksProxySocketFactory");
        }
        if (this.getCacheComplexData()) {
            this.complexDataCache = new LRUCache(this.getComplexDataCacheSize());
        }
    }
    
    private void initializePropsFromServer() throws SQLException {
        final String connectionInterceptorClasses = this.getConnectionLifecycleInterceptors();
        this.connectionLifecycleInterceptors = null;
        if (connectionInterceptorClasses != null) {
            this.connectionLifecycleInterceptors = Util.loadExtensions(this, this.props, connectionInterceptorClasses, "Connection.badLifecycleInterceptor", this.getExceptionInterceptor());
        }
        this.setSessionVariables();
        if (!this.versionMeetsMinimum(4, 1, 0)) {
            this.setTransformedBitIsBoolean(false);
        }
        this.parserKnowsUnicode = this.versionMeetsMinimum(4, 1, 0);
        if (this.getUseServerPreparedStmts() && this.versionMeetsMinimum(4, 1, 0)) {
            this.useServerPreparedStmts = true;
            if (this.versionMeetsMinimum(5, 0, 0) && !this.versionMeetsMinimum(5, 0, 3)) {
                this.useServerPreparedStmts = false;
            }
        }
        if (this.versionMeetsMinimum(3, 21, 22)) {
            this.loadServerVariables();
            if (this.versionMeetsMinimum(5, 0, 2)) {
                this.autoIncrementIncrement = this.getServerVariableAsInt("auto_increment_increment", 1);
            }
            else {
                this.autoIncrementIncrement = 1;
            }
            this.buildCollationMapping();
            LicenseConfiguration.checkLicenseType(this.serverVariables);
            final String lowerCaseTables = this.serverVariables.get("lower_case_table_names");
            this.lowerCaseTableNames = ("on".equalsIgnoreCase(lowerCaseTables) || "1".equalsIgnoreCase(lowerCaseTables) || "2".equalsIgnoreCase(lowerCaseTables));
            this.storesLowerCaseTableName = ("1".equalsIgnoreCase(lowerCaseTables) || "on".equalsIgnoreCase(lowerCaseTables));
            this.configureTimezone();
            if (this.serverVariables.containsKey("max_allowed_packet")) {
                final int serverMaxAllowedPacket = this.getServerVariableAsInt("max_allowed_packet", -1);
                if (serverMaxAllowedPacket != -1 && (serverMaxAllowedPacket < this.getMaxAllowedPacket() || this.getMaxAllowedPacket() <= 0)) {
                    this.setMaxAllowedPacket(serverMaxAllowedPacket);
                }
                else if (serverMaxAllowedPacket == -1 && this.getMaxAllowedPacket() == -1) {
                    this.setMaxAllowedPacket(65535);
                }
                if (this.getUseServerPrepStmts()) {
                    final int preferredBlobSendChunkSize = this.getBlobSendChunkSize();
                    final int packetHeaderSize = 8203;
                    final int allowedBlobSendChunkSize = Math.min(preferredBlobSendChunkSize, this.getMaxAllowedPacket()) - packetHeaderSize;
                    if (allowedBlobSendChunkSize <= 0) {
                        throw SQLError.createSQLException("Connection setting too low for 'maxAllowedPacket'. When 'useServerPrepStmts=true', 'maxAllowedPacket' must be higher than " + packetHeaderSize + ". Check also 'max_allowed_packet' in MySQL configuration files.", "01S00", this.getExceptionInterceptor());
                    }
                    this.setBlobSendChunkSize(String.valueOf(allowedBlobSendChunkSize));
                }
            }
            if (this.serverVariables.containsKey("net_buffer_length")) {
                this.netBufferLength = this.getServerVariableAsInt("net_buffer_length", 16384);
            }
            this.checkTransactionIsolationLevel();
            if (!this.versionMeetsMinimum(4, 1, 0)) {
                this.checkServerEncoding();
            }
            this.io.checkForCharsetMismatch();
            if (this.serverVariables.containsKey("sql_mode")) {
                final String sqlModeAsString = this.serverVariables.get("sql_mode");
                if (StringUtils.isStrictlyNumeric(sqlModeAsString)) {
                    this.useAnsiQuotes = ((Integer.parseInt(sqlModeAsString) & 0x4) > 0);
                }
                else if (sqlModeAsString != null) {
                    this.useAnsiQuotes = (sqlModeAsString.indexOf("ANSI_QUOTES") != -1);
                    this.noBackslashEscapes = (sqlModeAsString.indexOf("NO_BACKSLASH_ESCAPES") != -1);
                }
            }
        }
        this.configureClientCharacterSet(false);
        try {
            this.errorMessageEncoding = CharsetMapping.getCharacterEncodingForErrorMessages(this);
        }
        catch (SQLException ex) {
            throw ex;
        }
        catch (RuntimeException ex2) {
            final SQLException sqlEx = SQLError.createSQLException(ex2.toString(), "S1009", null);
            sqlEx.initCause(ex2);
            throw sqlEx;
        }
        if (this.versionMeetsMinimum(3, 23, 15)) {
            this.transactionsSupported = true;
            this.handleAutoCommitDefaults();
        }
        else {
            this.transactionsSupported = false;
        }
        if (this.versionMeetsMinimum(3, 23, 36)) {
            this.hasIsolationLevels = true;
        }
        else {
            this.hasIsolationLevels = false;
        }
        this.hasQuotedIdentifiers = this.versionMeetsMinimum(3, 23, 6);
        this.io.resetMaxBuf();
        if (this.io.versionMeetsMinimum(4, 1, 0)) {
            final String characterSetResultsOnServerMysql = this.serverVariables.get("jdbc.local.character_set_results");
            if (characterSetResultsOnServerMysql == null || StringUtils.startsWithIgnoreCaseAndWs(characterSetResultsOnServerMysql, "NULL") || characterSetResultsOnServerMysql.length() == 0) {
                final String defaultMetadataCharsetMysql = this.serverVariables.get("character_set_system");
                String defaultMetadataCharset = null;
                if (defaultMetadataCharsetMysql != null) {
                    defaultMetadataCharset = CharsetMapping.getJavaEncodingForMysqlCharset(defaultMetadataCharsetMysql);
                }
                else {
                    defaultMetadataCharset = "UTF-8";
                }
                this.characterSetMetadata = defaultMetadataCharset;
            }
            else {
                this.characterSetResultsOnServer = CharsetMapping.getJavaEncodingForMysqlCharset(characterSetResultsOnServerMysql);
                this.characterSetMetadata = this.characterSetResultsOnServer;
            }
        }
        else {
            this.characterSetMetadata = this.getEncoding();
        }
        if (this.versionMeetsMinimum(4, 1, 0) && !this.versionMeetsMinimum(4, 1, 10) && this.getAllowMultiQueries() && this.isQueryCacheEnabled()) {
            this.setAllowMultiQueries(false);
        }
        if (this.versionMeetsMinimum(5, 0, 0) && (this.getUseLocalTransactionState() || this.getElideSetAutoCommits()) && this.isQueryCacheEnabled() && !this.versionMeetsMinimum(5, 1, 32)) {
            this.setUseLocalTransactionState(false);
            this.setElideSetAutoCommits(false);
        }
        this.setupServerForTruncationChecks();
    }
    
    private boolean isQueryCacheEnabled() {
        return "ON".equalsIgnoreCase(this.serverVariables.get("query_cache_type")) && !"0".equalsIgnoreCase(this.serverVariables.get("query_cache_size"));
    }
    
    private int getServerVariableAsInt(final String variableName, final int fallbackValue) throws SQLException {
        try {
            return Integer.parseInt(this.serverVariables.get(variableName));
        }
        catch (NumberFormatException nfe) {
            this.getLog().logWarn(Messages.getString("Connection.BadValueInServerVariables", new Object[] { variableName, this.serverVariables.get(variableName), fallbackValue }));
            return fallbackValue;
        }
    }
    
    private void handleAutoCommitDefaults() throws SQLException {
        boolean resetAutoCommitDefault = false;
        if (!this.getElideSetAutoCommits()) {
            final String initConnectValue = this.serverVariables.get("init_connect");
            if ((this.versionMeetsMinimum(4, 1, 2) && initConnectValue != null && initConnectValue.length() > 0) || this.getIO().isOracleMode()) {
                ResultSet rs = null;
                java.sql.Statement stmt = null;
                try {
                    String querySql;
                    int offset;
                    if (this.io.isOracleMode()) {
                        querySql = "SHOW VARIABLES WHERE Variable_name = 'autocommit'";
                        offset = 2;
                    }
                    else {
                        querySql = "SELECT @@session.autocommit";
                        offset = 1;
                    }
                    stmt = this.getMetadataSafeStatement();
                    rs = stmt.executeQuery(querySql);
                    if (rs.next()) {
                        this.autoCommit = rs.getBoolean(offset);
                        resetAutoCommitDefault = !this.autoCommit;
                    }
                }
                finally {
                    if (rs != null) {
                        try {
                            rs.close();
                        }
                        catch (SQLException ex2) {}
                    }
                    if (stmt != null) {
                        try {
                            stmt.close();
                        }
                        catch (SQLException ex3) {}
                    }
                }
            }
            else {
                resetAutoCommitDefault = true;
            }
        }
        else if (this.getIO().isSetNeededForAutoCommitMode(true)) {
            this.autoCommit = false;
            resetAutoCommitDefault = true;
        }
        if (resetAutoCommitDefault) {
            try {
                this.setAutoCommit(true);
            }
            catch (SQLException ex) {
                if (ex.getErrorCode() != 1820 || this.getDisconnectOnExpiredPasswords()) {
                    throw ex;
                }
            }
        }
    }
    
    @Override
    public boolean isClientTzUTC() {
        return this.isClientTzUTC;
    }
    
    @Override
    public boolean isClosed() {
        return this.isClosed;
    }
    
    @Override
    public boolean isOracleMode() throws SQLException {
        this.checkClosed();
        if (null != this.io) {
            return this.io.isOracleMode();
        }
        throw new SQLException("connection has not been established");
    }
    
    @Override
    public boolean isCursorFetchEnabled() throws SQLException {
        return this.versionMeetsMinimum(5, 0, 2) && this.getUseCursorFetch();
    }
    
    @Override
    public boolean isInGlobalTx() {
        return this.isInGlobalTx;
    }
    
    @Override
    public boolean isMasterConnection() {
        return false;
    }
    
    @Override
    public boolean isNoBackslashEscapesSet() {
        return this.noBackslashEscapes;
    }
    
    @Override
    public boolean isReadInfoMsgEnabled() {
        return this.readInfoMsg;
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        return this.isReadOnly(true);
    }
    
    @Override
    public boolean isReadOnly(final boolean useSessionStatus) throws SQLException {
        if (useSessionStatus && !this.isClosed && this.versionMeetsMinimum(5, 6, 5) && !this.getUseLocalSessionState() && this.getReadOnlyPropagatesToServer()) {
            java.sql.Statement stmt = null;
            ResultSet rs = null;
            String querySql = null;
            int offset = 0;
            try {
                if (this.io.isOracleMode()) {
                    querySql = "SHOW VARIABLES WHERE Variable_name = 'tx_read_only'";
                    offset = 2;
                }
                else {
                    querySql = "SELECT @@session.tx_read_only";
                    offset = 1;
                }
                stmt = this.getMetadataSafeStatement();
                rs = stmt.executeQuery(querySql);
                if (rs.next()) {
                    final String value = rs.getString(offset);
                    return value.equalsIgnoreCase("1") || value.equalsIgnoreCase("on");
                }
            }
            catch (SQLException ex1) {
                if (ex1.getErrorCode() != 1820 || this.getDisconnectOnExpiredPasswords()) {
                    throw SQLError.createSQLException("Could not retrieve transation read-only status server", "S1000", ex1, this.getExceptionInterceptor());
                }
            }
            finally {
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (Exception ex2) {}
                    rs = null;
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    }
                    catch (Exception ex3) {}
                    stmt = null;
                }
            }
        }
        return this.readOnly;
    }
    
    @Override
    public boolean isRunningOnJDK13() {
        return this.isRunningOnJDK13;
    }
    
    @Override
    public boolean isSameResource(final Connection otherConnection) {
        synchronized (this.getConnectionMutex()) {
            if (otherConnection == null) {
                return false;
            }
            boolean directCompare = true;
            final String otherHost = ((ConnectionImpl)otherConnection).origHostToConnectTo;
            final String otherOrigDatabase = ((ConnectionImpl)otherConnection).origDatabaseToConnectTo;
            final String otherCurrentCatalog = ((ConnectionImpl)otherConnection).database;
            if (!nullSafeCompare(otherHost, this.origHostToConnectTo)) {
                directCompare = false;
            }
            else if (otherHost != null && otherHost.indexOf(44) == -1 && otherHost.indexOf(58) == -1) {
                directCompare = (((ConnectionImpl)otherConnection).origPortToConnectTo == this.origPortToConnectTo);
            }
            if (directCompare && (!nullSafeCompare(otherOrigDatabase, this.origDatabaseToConnectTo) || !nullSafeCompare(otherCurrentCatalog, this.database))) {
                directCompare = false;
            }
            if (directCompare) {
                return true;
            }
            final String otherResourceId = ((ConnectionImpl)otherConnection).getResourceId();
            final String myResourceId = this.getResourceId();
            if (otherResourceId != null || myResourceId != null) {
                directCompare = nullSafeCompare(otherResourceId, myResourceId);
                if (directCompare) {
                    return true;
                }
            }
            return false;
        }
    }
    
    @Override
    public boolean isServerTzUTC() {
        return this.isServerTzUTC;
    }
    
    private void createConfigCacheIfNeeded() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            if (this.serverConfigCache != null) {
                return;
            }
            try {
                final Class<?> factoryClass = Class.forName(this.getServerConfigCacheFactory());
                final CacheAdapterFactory<String, Map<String, String>> cacheFactory = (CacheAdapterFactory<String, Map<String, String>>)factoryClass.newInstance();
                this.serverConfigCache = cacheFactory.getInstance(this, this.myURL, Integer.MAX_VALUE, Integer.MAX_VALUE, this.props);
                final ExceptionInterceptor evictOnCommsError = new ExceptionInterceptor() {
                    @Override
                    public void init(final Connection conn, final Properties config) throws SQLException {
                    }
                    
                    @Override
                    public void destroy() {
                    }
                    
                    @Override
                    public SQLException interceptException(final SQLException sqlEx, final Connection conn) {
                        if (sqlEx.getSQLState() != null && sqlEx.getSQLState().startsWith("08")) {
                            ConnectionImpl.this.serverConfigCache.invalidate(ConnectionImpl.this.getURL());
                        }
                        return null;
                    }
                };
                if (this.exceptionInterceptor == null) {
                    this.exceptionInterceptor = evictOnCommsError;
                }
                else {
                    ((ExceptionInterceptorChain)this.exceptionInterceptor).addRingZero(evictOnCommsError);
                }
            }
            catch (ClassNotFoundException e) {
                final SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantFindCacheFactory", new Object[] { this.getParseInfoCacheFactory(), "parseInfoCacheFactory" }), this.getExceptionInterceptor());
                sqlEx.initCause(e);
                throw sqlEx;
            }
            catch (InstantiationException e2) {
                final SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantLoadCacheFactory", new Object[] { this.getParseInfoCacheFactory(), "parseInfoCacheFactory" }), this.getExceptionInterceptor());
                sqlEx.initCause(e2);
                throw sqlEx;
            }
            catch (IllegalAccessException e3) {
                final SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.CantLoadCacheFactory", new Object[] { this.getParseInfoCacheFactory(), "parseInfoCacheFactory" }), this.getExceptionInterceptor());
                sqlEx.initCause(e3);
                throw sqlEx;
            }
        }
    }
    
    private void loadServerVariables() throws SQLException {
        if (this.getCacheServerConfiguration()) {
            this.createConfigCacheIfNeeded();
            final Map<String, String> cachedVariableMap = this.serverConfigCache.get(this.getURL());
            if (cachedVariableMap != null) {
                final String cachedServerVersion = cachedVariableMap.get("server_version_string");
                if (cachedServerVersion != null && this.io.getServerVersion() != null && cachedServerVersion.equals(this.io.getServerVersion())) {
                    this.serverVariables = cachedVariableMap;
                    return;
                }
                this.serverConfigCache.invalidate(this.getURL());
            }
        }
        java.sql.Statement stmt = null;
        ResultSet results = null;
        try {
            stmt = this.getMetadataSafeStatement();
            String version = this.dbmd.getDriverVersion();
            if (version != null && version.indexOf(42) != -1) {
                final StringBuilder buf = new StringBuilder(version.length() + 10);
                for (int i = 0; i < version.length(); ++i) {
                    final char c = version.charAt(i);
                    if (c == '*') {
                        buf.append("[star]");
                    }
                    else {
                        buf.append(c);
                    }
                }
                version = buf.toString();
            }
            final String versionComment = (this.getParanoid() || version == null) ? "" : ("/* " + version + " */");
            this.serverVariables = new HashMap<String, String>();
            try {
                if (!this.io.isOracleMode() && this.versionMeetsMinimum(5, 1, 0)) {
                    final StringBuilder queryBuf = new StringBuilder(versionComment).append("SELECT");
                    queryBuf.append("  @@session.auto_increment_increment AS auto_increment_increment");
                    queryBuf.append(", @@character_set_client AS character_set_client");
                    queryBuf.append(", @@character_set_connection AS character_set_connection");
                    queryBuf.append(", @@character_set_results AS character_set_results");
                    queryBuf.append(", @@character_set_server AS character_set_server");
                    queryBuf.append(", @@init_connect AS init_connect");
                    queryBuf.append(", @@interactive_timeout AS interactive_timeout");
                    if (!this.versionMeetsMinimum(5, 5, 0)) {
                        queryBuf.append(", @@language AS language");
                    }
                    queryBuf.append(", @@license AS license");
                    queryBuf.append(", @@lower_case_table_names AS lower_case_table_names");
                    queryBuf.append(", @@max_allowed_packet AS max_allowed_packet");
                    queryBuf.append(", @@net_buffer_length AS net_buffer_length");
                    queryBuf.append(", @@net_write_timeout AS net_write_timeout");
                    queryBuf.append(", @@query_cache_size AS query_cache_size");
                    queryBuf.append(", @@query_cache_type AS query_cache_type");
                    queryBuf.append(", @@sql_mode AS sql_mode");
                    queryBuf.append(", @@system_time_zone AS system_time_zone");
                    queryBuf.append(", @@time_zone AS time_zone");
                    queryBuf.append(", @@tx_isolation AS tx_isolation");
                    queryBuf.append(", @@wait_timeout AS wait_timeout");
                    queryBuf.append(", @@version AS ob_server_version");
                    queryBuf.append(", @@time_zone AS time_zone");
                    results = stmt.executeQuery(queryBuf.toString());
                    if (results.next()) {
                        final ResultSetMetaData rsmd = results.getMetaData();
                        for (int j = 1; j <= rsmd.getColumnCount(); ++j) {
                            this.serverVariables.put(rsmd.getColumnLabel(j), results.getString(j));
                        }
                    }
                }
                else {
                    results = stmt.executeQuery(versionComment + "SHOW VARIABLES");
                    while (results.next()) {
                        this.serverVariables.put(results.getString(1), results.getString(2));
                    }
                }
                results.close();
                results = null;
            }
            catch (SQLException ex) {
                if (ex.getErrorCode() != 1820 || this.getDisconnectOnExpiredPasswords()) {
                    throw ex;
                }
            }
            if (this.getCacheServerConfiguration()) {
                this.serverVariables.put("server_version_string", this.io.getServerVersion());
                this.serverConfigCache.put(this.getURL(), this.serverVariables);
            }
        }
        catch (SQLException e) {
            throw e;
        }
        finally {
            if (results != null) {
                try {
                    results.close();
                }
                catch (SQLException ex2) {}
            }
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException ex3) {}
            }
        }
    }
    
    @Override
    public int getAutoIncrementIncrement() {
        return this.autoIncrementIncrement;
    }
    
    @Override
    public boolean lowerCaseTableNames() {
        return this.lowerCaseTableNames;
    }
    
    @Override
    public String nativeSQL(final String sql) throws SQLException {
        if (sql == null) {
            return null;
        }
        final Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, this.serverSupportsConvertFn(), this.getMultiHostSafeProxy());
        if (escapedSqlResult instanceof String) {
            return (String)escapedSqlResult;
        }
        return ((EscapeProcessorResult)escapedSqlResult).escapedSql;
    }
    
    private CallableStatement parseCallableStatement(final String sql) throws SQLException {
        final Object escapedSqlResult = EscapeProcessor.escapeSQL(sql, this.serverSupportsConvertFn(), this.getMultiHostSafeProxy());
        boolean isFunctionCall = false;
        String parsedSql = null;
        if (escapedSqlResult instanceof EscapeProcessorResult) {
            parsedSql = ((EscapeProcessorResult)escapedSqlResult).escapedSql;
            isFunctionCall = ((EscapeProcessorResult)escapedSqlResult).callingStoredFunction;
        }
        else {
            parsedSql = (String)escapedSqlResult;
            isFunctionCall = false;
        }
        if (this.isOracleMode()) {
            if (isFunctionCall) {
                parsedSql += " FROM dual";
            }
            return ServerCallableStatement.getInstance(this.getMultiHostSafeProxy(), parsedSql, this.database, isFunctionCall);
        }
        return com.alipay.oceanbase.jdbc.CallableStatement.getInstance(this.getMultiHostSafeProxy(), parsedSql, this.database, isFunctionCall);
    }
    
    @Override
    public boolean parserKnowsUnicode() {
        return this.parserKnowsUnicode;
    }
    
    @Override
    public void ping() throws SQLException {
        this.pingInternal(true, 0);
    }
    
    @Override
    public void pingInternal(final boolean checkForClosedConnection, final int timeoutMillis) throws SQLException {
        if (checkForClosedConnection) {
            this.checkClosed();
        }
        final long pingMillisLifetime = this.getSelfDestructOnPingSecondsLifetime();
        final int pingMaxOperations = this.getSelfDestructOnPingMaxOperations();
        if ((pingMillisLifetime > 0L && System.currentTimeMillis() - this.connectionCreationTimeMillis > pingMillisLifetime) || (pingMaxOperations > 0 && pingMaxOperations <= this.io.getCommandCount())) {
            this.close();
            throw SQLError.createSQLException(Messages.getString("Connection.exceededConnectionLifetime"), "08S01", this.getExceptionInterceptor());
        }
        this.io.sendCommand(14, null, null, false, null, timeoutMillis);
    }
    
    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        return this.prepareCall(sql, 1003, 1007);
    }
    
    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        if (this.versionMeetsMinimum(5, 0, 0)) {
            CallableStatement cStmt = null;
            if (!this.getCacheCallableStatements()) {
                cStmt = this.parseCallableStatement(sql);
            }
            else if (this.isOracleMode()) {
                synchronized (this.parsedCallableStatementCache) {
                    final CompoundCacheKey key = new CompoundCacheKey(this.getCatalog(), sql);
                    ServerCallableStatement.CallableStatementParamInfo cachedParamInfo = ((LinkedHashMap<K, ServerCallableStatement.CallableStatementParamInfo>)this.parsedCallableStatementCache).get(key);
                    if (cachedParamInfo != null) {
                        cStmt = ServerCallableStatement.getInstance(this.getMultiHostSafeProxy(), cachedParamInfo);
                    }
                    else {
                        cStmt = this.parseCallableStatement(sql);
                        synchronized (cStmt) {
                            cachedParamInfo = ((ServerCallableStatement)cStmt).paramInfo;
                        }
                        ((HashMap<CompoundCacheKey, ServerCallableStatement.CallableStatementParamInfo>)this.parsedCallableStatementCache).put(key, cachedParamInfo);
                    }
                }
            }
            else {
                synchronized (this.parsedCallableStatementCache) {
                    final CompoundCacheKey key = new CompoundCacheKey(this.getCatalog(), sql);
                    com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParamInfo cachedParamInfo2 = ((LinkedHashMap<K, com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParamInfo>)this.parsedCallableStatementCache).get(key);
                    if (cachedParamInfo2 != null) {
                        cStmt = com.alipay.oceanbase.jdbc.CallableStatement.getInstance(this.getMultiHostSafeProxy(), cachedParamInfo2);
                    }
                    else {
                        cStmt = this.parseCallableStatement(sql);
                        synchronized (cStmt) {
                            cachedParamInfo2 = ((com.alipay.oceanbase.jdbc.CallableStatement)cStmt).paramInfo;
                        }
                        ((HashMap<CompoundCacheKey, com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParamInfo>)this.parsedCallableStatementCache).put(key, cachedParamInfo2);
                    }
                }
            }
            ((PreparedStatement)cStmt).setResultSetType(resultSetType);
            ((PreparedStatement)cStmt).setResultSetConcurrency(resultSetConcurrency);
            return cStmt;
        }
        throw SQLError.createSQLException("Callable statements not supported.", "S1C00", this.getExceptionInterceptor());
    }
    
    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        if (this.getPedantic() && resultSetHoldability != 1) {
            throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", this.getExceptionInterceptor());
        }
        final com.alipay.oceanbase.jdbc.CallableStatement cStmt = (com.alipay.oceanbase.jdbc.CallableStatement)this.prepareCall(sql, resultSetType, resultSetConcurrency);
        return cStmt;
    }
    
    private String getCachedSql(final String originSql) throws SQLException {
        if (StringCacheUtil.sqlStringCache == null) {
            return originSql;
        }
        String sqlCache = StringCacheUtil.sqlStringCache.getIfPresent(originSql);
        if (sqlCache == null) {
            StringCacheUtil.sqlStringCache.put(originSql, originSql);
            sqlCache = originSql;
        }
        return sqlCache;
    }
    
    @Override
    public java.sql.PreparedStatement prepareStatement(final String sql) throws SQLException {
        return this.prepareStatement(sql, 1003, 1007);
    }
    
    @Override
    public java.sql.PreparedStatement prepareStatement(final String sql, final int autoGenKeyIndex) throws SQLException {
        final java.sql.PreparedStatement pStmt = this.prepareStatement(sql);
        ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
        return pStmt;
    }
    
    @Override
    public java.sql.PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            this.checkClosed();
            PreparedStatement pStmt = null;
            boolean canServerPrepare = true;
            String nativeSql = this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
            if (this.getUseSqlStringCache()) {
                nativeSql = this.getCachedSql(nativeSql);
            }
            if (this.useServerPreparedStmts && this.getEmulateUnsupportedPstmts()) {
                canServerPrepare = this.canHandleAsServerPreparedStatement(nativeSql);
            }
            if (this.useServerPreparedStmts && canServerPrepare) {
                if (this.getCachePreparedStatements()) {
                    synchronized (this.serverSideStatementCache) {
                        pStmt = ((HashMap<K, ServerPreparedStatement>)this.serverSideStatementCache).remove(sql);
                        if (pStmt != null) {
                            ((ServerPreparedStatement)pStmt).setClosed(false);
                            pStmt.clearParameters();
                        }
                        if (pStmt == null) {
                            try {
                                pStmt = ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database, resultSetType, resultSetConcurrency);
                                if (sql.length() < this.getPreparedStatementCacheSqlLimit()) {
                                    ((ServerPreparedStatement)pStmt).isCached = true;
                                }
                                pStmt.setResultSetType(resultSetType);
                                pStmt.setResultSetConcurrency(resultSetConcurrency);
                            }
                            catch (SQLException sqlEx) {
                                if (!this.getEmulateUnsupportedPstmts()) {
                                    throw sqlEx;
                                }
                                pStmt = (PreparedStatement)this.clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
                                if (sql.length() < this.getPreparedStatementCacheSqlLimit()) {
                                    ((HashMap<String, Boolean>)this.serverSideStatementCheckCache).put(sql, Boolean.FALSE);
                                }
                            }
                        }
                    }
                }
                else {
                    try {
                        pStmt = ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.database, resultSetType, resultSetConcurrency);
                        pStmt.setResultSetType(resultSetType);
                        pStmt.setResultSetConcurrency(resultSetConcurrency);
                    }
                    catch (SQLException sqlEx2) {
                        if (!this.getEmulateUnsupportedPstmts()) {
                            throw sqlEx2;
                        }
                        pStmt = (PreparedStatement)this.clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
                    }
                }
            }
            else {
                pStmt = (PreparedStatement)this.clientPrepareStatement(nativeSql, resultSetType, resultSetConcurrency, false);
            }
            return pStmt;
        }
    }
    
    @Override
    public java.sql.PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        if (this.getPedantic() && resultSetHoldability != 1) {
            throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", this.getExceptionInterceptor());
        }
        return this.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public java.sql.PreparedStatement prepareStatement(final String sql, final int[] autoGenKeyIndexes) throws SQLException {
        final java.sql.PreparedStatement pStmt = this.prepareStatement(sql);
        ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0);
        return pStmt;
    }
    
    @Override
    public java.sql.PreparedStatement prepareStatement(final String sql, final String[] autoGenKeyColNames) throws SQLException {
        final java.sql.PreparedStatement pStmt = this.prepareStatement(sql);
        ((PreparedStatement)pStmt).setRetrieveGeneratedKeys(autoGenKeyColNames != null && autoGenKeyColNames.length > 0);
        return pStmt;
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
        return false;
    }
    
    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
    }
    
    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException {
    }
    
    @Override
    public String getClientInfo(final String name) throws SQLException {
        return null;
    }
    
    @Override
    public Properties getClientInfo() throws SQLException {
        return null;
    }
    
    @Override
    public java.sql.Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        this.checkClosed();
        if (!this.isOracleMode()) {
            throw SQLError.createSQLFeatureNotSupportedException();
        }
        boolean fetchTypeFromRemote = true;
        ComplexDataType attrType = null;
        if (this.getCacheComplexData()) {
            synchronized (this.complexDataCache) {
                attrType = ((LinkedHashMap<K, ComplexDataType>)this.complexDataCache).get(typeName.toUpperCase());
                if (null != attrType && attrType.isValid()) {
                    fetchTypeFromRemote = false;
                }
                else if (null == attrType && ComplexDataType.isBaseDataType(ComplexDataType.getObComplexType(typeName))) {
                    fetchTypeFromRemote = false;
                    attrType = new ComplexDataType(typeName, this.database, ComplexDataType.getObComplexType(typeName));
                }
            }
        }
        if (fetchTypeFromRemote) {
            attrType = this.getComplexDataTypeFromRemote(typeName);
        }
        if (attrType.getType() == 4) {
            throw SQLError.createSQLFeatureNotSupportedException();
        }
        final ComplexDataType parentType = new ComplexDataType("", this.database, 4);
        parentType.setAttrCount(1);
        parentType.setAttrType(0, attrType);
        final ArrayImpl array = new ArrayImpl(parentType);
        array.setAttrData(elements);
        return array;
    }
    
    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
        this.checkClosed();
        if (!this.isOracleMode()) {
            throw SQLError.createSQLFeatureNotSupportedException();
        }
        boolean fetchTypeFromRemote = true;
        ComplexDataType type = null;
        if (this.getCacheComplexData()) {
            synchronized (this.complexDataCache) {
                type = ((LinkedHashMap<K, ComplexDataType>)this.complexDataCache).get(typeName.toUpperCase());
                if (null != type && type.isValid()) {
                    fetchTypeFromRemote = false;
                }
            }
        }
        if (fetchTypeFromRemote) {
            type = this.getComplexDataTypeFromRemote(typeName);
        }
        final StructImpl struct = new StructImpl(type);
        struct.setAttrData(attributes);
        return struct;
    }
    
    private java.sql.Connection getComplexConnection() throws SQLException {
        if (null == this.complexConnection || this.complexConnection.isClosed()) {
            this.complexConnection = new ConnectionImpl(this.origHostToConnectTo, this.origPortToConnectTo, this.props, this.origDatabaseToConnectTo, this.myURL);
        }
        return this.complexConnection;
    }
    
    @Override
    public ComplexDataType getComplexDataType(final String typeName) throws SQLException {
        ComplexDataType type = null;
        type = this.getComplexDataTypeFromCache(typeName);
        if (null != type && type.isValid()) {
            return type;
        }
        type = this.getComplexDataTypeFromRemote(typeName);
        return type;
    }
    
    @Override
    public ComplexDataType getComplexDataTypeFromCache(final String typeName) {
        synchronized (this.complexDataCache) {
            return ((LinkedHashMap<K, ComplexDataType>)this.complexDataCache).get(typeName.toUpperCase());
        }
    }
    
    @Override
    public ComplexDataType getComplexDataTypeFromRemote(final String typeName) throws SQLException {
        try {
            final java.sql.Connection conn = this.getComplexConnection();
            java.sql.PreparedStatement ps = conn.prepareStatement("SELECT\n  0 DEPTH,\n  NULL PARENT_OWNER,\n  NULL PARENT_TYPE,\n  to_char(TYPE_NAME) CHILD_TYPE,\n  0 ATTR_NO,\n  SYS_CONTEXT('USERENV', 'CURRENT_USER') CHILD_TYPE_OWNER,\n  A.TYPECODE ATTR_TYPE_CODE,\n  NULL LENGTH,\n  NULL NUMBER_PRECISION,\n  NULL SCALE,\n  NULL CHARACTER_SET_NAME\nFROM\n  USER_TYPES A WHERE TYPE_NAME = ?\nUNION\n(\nWITH \nCTE_RESULT(PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME) \nAS (\n    SELECT\n      SYS_CONTEXT('USERENV','CURRENT_USER') PARENT_OWNER,\n      B.TYPE_NAME PARENT_TYPE,\n      B.ELEM_TYPE_NAME CHILD_TYPE,\n      0 ATTR_NO,\n      B.ELEM_TYPE_OWNER CHILD_TYPE_OWNER,\n      NVL(A.TYPECODE, B.ELEM_TYPE_NAME) AS ATTR_TYPE_CODE,\n      B.LENGTH LENGTH,\n      B.NUMBER_PRECISION NUMBER_PRECISION,\n      B.SCALE SCALE,\n      B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n    FROM\n      USER_COLL_TYPES B LEFT JOIN USER_TYPES A ON A.TYPE_NAME = B.ELEM_TYPE_NAME\n    UNION\n    SELECT\n      SYS_CONTEXT('USERENV','CURRENT_USER') PARENT_OWNER,\n      B.TYPE_NAME PARENT_TYPE,\n      B.ATTR_TYPE_NAME CHILD_TYPE,\n      B.ATTR_NO ATTR_NO,\n      B.ATTR_TYPE_OWNER CHILD_TYPE_OWNER,\n      NVL(A.TYPECODE, B.ATTR_TYPE_NAME) AS ATTR_TYPE_CODE,\n      B.LENGTH LENGTH,\n      B.NUMBER_PRECISION NUMBER_PRECISION,\n      B.SCALE SCALE,\n      B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n    FROM USER_TYPE_ATTRS B LEFT JOIN USER_TYPES A ON B.ATTR_TYPE_NAME = A.TYPE_NAME ORDER BY ATTR_NO\n) ,\nCTE(DEPTH, PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME)\nAS (\n  SELECT\n    1 DEPTH,\n    PARENT_OWNER,\n    PARENT_TYPE,\n    CHILD_TYPE,\n    ATTR_NO,\n    CHILD_TYPE_OWNER,\n    ATTR_TYPE_CODE,\n    LENGTH,\n    NUMBER_PRECISION,\n    SCALE, CHARACTER_SET_NAME\n  FROM CTE_RESULT WHERE PARENT_TYPE = ?\n  UNION ALL\n  SELECT\n    DEPTH + 1 DEPTH,\n    CTE_RESULT.PARENT_OWNER,\n    CTE_RESULT.PARENT_TYPE,\n    CTE_RESULT.CHILD_TYPE,\n    CTE_RESULT.ATTR_NO,\n    CTE_RESULT.CHILD_TYPE_OWNER,\n    CTE_RESULT.ATTR_TYPE_CODE,\n    CTE_RESULT.LENGTH,\n    CTE_RESULT.NUMBER_PRECISION,\n    CTE_RESULT.SCALE,\n    CTE_RESULT.CHARACTER_SET_NAME\n  FROM CTE_RESULT INNER JOIN CTE ON CTE_RESULT.PARENT_TYPE = CTE.CHILD_TYPE\n)\nSELECT * FROM CTE\n);", 1004, 1007);
            ps.setString(1, typeName.toUpperCase());
            ps.setString(2, typeName.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rs.beforeFirst();
            }
            else {
                rs.close();
                ps.close();
                ps = conn.prepareStatement("SELECT\n    0 DEPTH,\n    NULL PARENT_OWNER,\n    NULL PARENT_TYPE,\n    to_char(TYPE_NAME) CHILD_TYPE,\n    0 ATTR_NO,\n    OWNER CHILD_TYPE_OWNER,\n    A.TYPECODE ATTR_TYPE_CODE,\n    NULL LENGTH,\n    NULL NUMBER_PRECISION,\n    NULL SCALE,\n    NULL CHARACTER_SET_NAME\n  FROM\n    ALL_TYPES A WHERE TYPE_NAME = ? AND OWNER = ?\n  UNION\n  (\n  WITH\n  CTE_RESULT(PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME)\n  AS (\n      SELECT\n        B.OWNER PARENT_OWNER,\n        B.TYPE_NAME PARENT_TYPE,\n        B.ELEM_TYPE_NAME CHILD_TYPE,\n        0 ATTR_NO,\n        B.ELEM_TYPE_OWNER CHILD_TYPE_OWNER,\n        NVL(A.TYPECODE, B.ELEM_TYPE_NAME) AS ATTR_TYPE_CODE,\n        B.LENGTH LENGTH,\n        B.NUMBER_PRECISION NUMBER_PRECISION,\n        B.SCALE SCALE,\n        B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n      FROM\n        ALL_COLL_TYPES B LEFT JOIN ALL_TYPES A ON A.TYPE_NAME = B.ELEM_TYPE_NAME AND A.OWNER = B.ELEM_TYPE_OWNER\n      UNION\n      SELECT\n        B.OWNER PARENT_OWNER,\n        B.TYPE_NAME PARENT_TYPE,\n        B.ATTR_TYPE_NAME CHILD_TYPE,\n        B.ATTR_NO ATTR_NO,\n        B.ATTR_TYPE_OWNER CHILD_TYPE_OWNER,\n        NVL(A.TYPECODE, B.ATTR_TYPE_NAME) AS ATTR_TYPE_CODE,\n        B.LENGTH LENGTH,\n        B.NUMBER_PRECISION NUMBER_PRECISION,\n        B.SCALE SCALE,\n        B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n      FROM ALL_TYPE_ATTRS B LEFT JOIN ALL_TYPES A ON A.TYPE_NAME = B.ATTR_TYPE_NAME AND A.OWNER = B.ATTR_TYPE_OWNER ORDER BY ATTR_NO\n  ) ,\n  CTE(DEPTH, PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME)\n  AS (\n    SELECT\n      1 DEPTH,\n      PARENT_OWNER,\n      PARENT_TYPE,\n      CHILD_TYPE,\n      ATTR_NO,\n      CHILD_TYPE_OWNER,\n      ATTR_TYPE_CODE,\n      LENGTH,\n      NUMBER_PRECISION,\n      SCALE, CHARACTER_SET_NAME\n    FROM CTE_RESULT WHERE PARENT_TYPE = ? AND PARENT_OWNER = ?\n    UNION ALL\n    SELECT\n      DEPTH + 1 DEPTH,\n      CTE_RESULT.PARENT_OWNER,\n      CTE_RESULT.PARENT_TYPE,\n      CTE_RESULT.CHILD_TYPE,\n      CTE_RESULT.ATTR_NO,\n      CTE_RESULT.CHILD_TYPE_OWNER,\n      CTE_RESULT.ATTR_TYPE_CODE,\n      CTE_RESULT.LENGTH,\n      CTE_RESULT.NUMBER_PRECISION,\n      CTE_RESULT.SCALE,\n      CTE_RESULT.CHARACTER_SET_NAME\n    FROM CTE_RESULT INNER JOIN CTE ON CTE_RESULT.PARENT_TYPE = CTE.CHILD_TYPE AND CTE_RESULT.PARENT_OWNER = CTE.CHILD_TYPE_OWNER\n  )\n  SELECT * FROM CTE\n  );", 1004, 1007);
                ps.setString(1, typeName.toUpperCase());
                ps.setString(2, "SYS");
                ps.setString(3, typeName.toUpperCase());
                ps.setString(4, "SYS");
                rs = ps.executeQuery();
            }
            while (rs.next()) {
                ComplexDataType complexType = null;
                final String childTypeName = rs.getString(4);
                final int type = ComplexDataType.getObComplexType(rs.getString(7));
                if (3 == type || 4 == type) {
                    complexType = this.getComplexDataTypeFromCache(childTypeName);
                    if (null == complexType) {
                        complexType = new ComplexDataType(childTypeName, rs.getString(6), type);
                        complexType.setValid(false);
                        this.recacheComplexDataType(complexType);
                    }
                    else {
                        complexType.setValid(false);
                    }
                }
                else {
                    complexType = this.getComplexDataTypeFromCache(childTypeName);
                    if (null == complexType) {
                        complexType = new ComplexDataType(childTypeName, "", type);
                        complexType.setValid(true);
                        this.recacheComplexDataType(complexType);
                    }
                }
                if (rs.getInt(1) > 0) {
                    final String parentTypeName = rs.getString(3);
                    final ComplexDataType parentType = this.getComplexDataTypeFromCache(parentTypeName);
                    final int attrIndex = rs.getInt(5);
                    if (3 != parentType.getType() || parentType.getAttrCount() >= attrIndex) {
                        continue;
                    }
                    parentType.setAttrCount(attrIndex);
                }
            }
            rs.first();
            do {
                if (rs.getInt(1) > 0) {
                    final String parentTypeName2 = rs.getString(3);
                    final ComplexDataType parentComplexType = this.getComplexDataTypeFromCache(parentTypeName2);
                    final String attrTypeName = rs.getString(4);
                    final ComplexDataType attrComplexType = this.getComplexDataTypeFromCache(attrTypeName);
                    if (parentComplexType.getType() == 3) {
                        parentComplexType.setAttrType(rs.getInt(5) - 1, attrComplexType);
                        parentComplexType.incInitAttrCount();
                        if (parentComplexType.getInitAttrCount() != parentComplexType.getAttrCount()) {
                            continue;
                        }
                        parentComplexType.setValid(true);
                    }
                    else {
                        if (parentComplexType.getType() != 4) {
                            continue;
                        }
                        parentComplexType.setAttrCount(1);
                        parentComplexType.setAttrType(0, attrComplexType);
                        parentComplexType.setValid(true);
                    }
                }
            } while (rs.next());
            rs.close();
            ps.close();
        }
        catch (SQLException e) {
            throw e;
        }
        return this.getComplexDataTypeFromCache(typeName);
    }
    
    @Override
    public void realClose(final boolean calledExplicitly, final boolean issueRollback, final boolean skipLocalTeardown, final Throwable reason) throws SQLException {
        SQLException sqlEx = null;
        if (this.isClosed()) {
            return;
        }
        this.forceClosedReason = reason;
        try {
            if (!skipLocalTeardown) {
                if (!this.getAutoCommit() && issueRollback) {
                    try {
                        this.rollback();
                    }
                    catch (SQLException ex) {
                        sqlEx = ex;
                    }
                }
                this.reportMetrics();
                if (this.getUseUsageAdvisor()) {
                    if (!calledExplicitly) {
                        final String message = "Connection implicitly closed by Driver. You should call Connection.close() from your code to free resources more efficiently and avoid resource leaks.";
                        this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.getCatalog(), this.getId(), -1, -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message));
                    }
                    final long connectionLifeTime = System.currentTimeMillis() - this.connectionCreationTimeMillis;
                    if (connectionLifeTime < 500L) {
                        final String message2 = "Connection lifetime of < .5 seconds. You might be un-necessarily creating short-lived connections and should investigate connection pooling to be more efficient.";
                        this.eventSink.consumeEvent(new ProfilerEvent((byte)0, "", this.getCatalog(), this.getId(), -1, -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, this.pointOfOrigin, message2));
                    }
                }
                try {
                    this.closeAllOpenStatements();
                }
                catch (SQLException ex) {
                    sqlEx = ex;
                }
                if (this.io != null) {
                    try {
                        this.io.quit();
                    }
                    catch (Exception ex2) {}
                }
            }
            else {
                this.io.forceClose();
            }
            if (this.statementInterceptors != null) {
                for (int i = 0; i < this.statementInterceptors.size(); ++i) {
                    this.statementInterceptors.get(i).destroy();
                }
            }
            if (this.exceptionInterceptor != null) {
                this.exceptionInterceptor.destroy();
            }
        }
        finally {
            this.openStatements.clear();
            if (this.io != null) {
                this.io.releaseResources();
                this.io = null;
            }
            this.statementInterceptors = null;
            this.exceptionInterceptor = null;
            ProfilerEventHandlerFactory.removeInstance(this);
            synchronized (this.getConnectionMutex()) {
                if (this.cancelTimer != null) {
                    this.cancelTimer.cancel();
                }
            }
            this.isClosed = true;
        }
        if (sqlEx != null) {
            throw sqlEx;
        }
    }
    
    @Override
    public void recacheComplexDataType(final ComplexDataType type) {
        synchronized (this.getConnectionMutex()) {
            synchronized (this.complexDataCache) {
                ((HashMap<String, ComplexDataType>)this.complexDataCache).put(type.getTypeName().toUpperCase(), type);
            }
        }
    }
    
    @Override
    public void recachePreparedStatement(final ServerPreparedStatement pstmt) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            if (this.getCachePreparedStatements() && pstmt.isPoolable()) {
                synchronized (this.serverSideStatementCache) {
                    ((HashMap<String, ServerPreparedStatement>)this.serverSideStatementCache).put(pstmt.originalSql, pstmt);
                }
            }
        }
    }
    
    @Override
    public void decachePreparedStatement(final ServerPreparedStatement pstmt) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            if (this.getCachePreparedStatements() && pstmt.isPoolable()) {
                synchronized (this.serverSideStatementCache) {
                    this.serverSideStatementCache.remove(pstmt.originalSql);
                }
            }
        }
    }
    
    @Override
    public void registerQueryExecutionTime(final long queryTimeMs) {
        if (queryTimeMs > this.longestQueryTimeMs) {
            this.longestQueryTimeMs = queryTimeMs;
            this.repartitionPerformanceHistogram();
        }
        this.addToPerformanceHistogram(queryTimeMs, 1);
        if (queryTimeMs < this.shortestQueryTimeMs) {
            this.shortestQueryTimeMs = ((queryTimeMs == 0L) ? 1L : queryTimeMs);
        }
        ++this.numberOfQueriesIssued;
        this.totalQueryTimeMs += queryTimeMs;
    }
    
    @Override
    public void registerStatement(final Statement stmt) {
        this.openStatements.addIfAbsent(stmt);
    }
    
    @Override
    public void releaseSavepoint(final Savepoint arg0) throws SQLException {
    }
    
    private void repartitionHistogram(final int[] histCounts, final long[] histBreakpoints, final long currentLowerBound, final long currentUpperBound) {
        if (this.oldHistCounts == null) {
            this.oldHistCounts = new int[histCounts.length];
            this.oldHistBreakpoints = new long[histBreakpoints.length];
        }
        System.arraycopy(histCounts, 0, this.oldHistCounts, 0, histCounts.length);
        System.arraycopy(histBreakpoints, 0, this.oldHistBreakpoints, 0, histBreakpoints.length);
        this.createInitialHistogram(histBreakpoints, currentLowerBound, currentUpperBound);
        for (int i = 0; i < 20; ++i) {
            this.addToHistogram(histCounts, histBreakpoints, this.oldHistBreakpoints[i], this.oldHistCounts[i], currentLowerBound, currentUpperBound);
        }
    }
    
    private void repartitionPerformanceHistogram() {
        this.checkAndCreatePerformanceHistogram();
        this.repartitionHistogram(this.perfMetricsHistCounts, this.perfMetricsHistBreakpoints, (this.shortestQueryTimeMs == Long.MAX_VALUE) ? 0L : this.shortestQueryTimeMs, this.longestQueryTimeMs);
    }
    
    private void repartitionTablesAccessedHistogram() {
        this.checkAndCreateTablesAccessedHistogram();
        this.repartitionHistogram(this.numTablesMetricsHistCounts, this.numTablesMetricsHistBreakpoints, (this.minimumNumberTablesAccessed == Long.MAX_VALUE) ? 0L : this.minimumNumberTablesAccessed, this.maximumNumberTablesAccessed);
    }
    
    private void reportMetrics() {
        if (this.getGatherPerformanceMetrics()) {
            final StringBuilder logMessage = new StringBuilder(256);
            logMessage.append("** Performance Metrics Report **\n");
            logMessage.append("\nLongest reported query: " + this.longestQueryTimeMs + " ms");
            logMessage.append("\nShortest reported query: " + this.shortestQueryTimeMs + " ms");
            logMessage.append("\nAverage query execution time: " + this.totalQueryTimeMs / this.numberOfQueriesIssued + " ms");
            logMessage.append("\nNumber of statements executed: " + this.numberOfQueriesIssued);
            logMessage.append("\nNumber of result sets created: " + this.numberOfResultSetsCreated);
            logMessage.append("\nNumber of statements prepared: " + this.numberOfPrepares);
            logMessage.append("\nNumber of prepared statement executions: " + this.numberOfPreparedExecutes);
            if (this.perfMetricsHistBreakpoints != null) {
                logMessage.append("\n\n\tTiming Histogram:\n");
                final int maxNumPoints = 20;
                int highestCount = Integer.MIN_VALUE;
                for (int i = 0; i < 20; ++i) {
                    if (this.perfMetricsHistCounts[i] > highestCount) {
                        highestCount = this.perfMetricsHistCounts[i];
                    }
                }
                if (highestCount == 0) {
                    highestCount = 1;
                }
                for (int i = 0; i < 19; ++i) {
                    if (i == 0) {
                        logMessage.append("\n\tless than " + this.perfMetricsHistBreakpoints[i + 1] + " ms: \t" + this.perfMetricsHistCounts[i]);
                    }
                    else {
                        logMessage.append("\n\tbetween " + this.perfMetricsHistBreakpoints[i] + " and " + this.perfMetricsHistBreakpoints[i + 1] + " ms: \t" + this.perfMetricsHistCounts[i]);
                    }
                    logMessage.append("\t");
                    for (int numPointsToGraph = (int)(maxNumPoints * (this.perfMetricsHistCounts[i] / (double)highestCount)), j = 0; j < numPointsToGraph; ++j) {
                        logMessage.append("*");
                    }
                    if (this.longestQueryTimeMs < this.perfMetricsHistCounts[i + 1]) {
                        break;
                    }
                }
                if (this.perfMetricsHistBreakpoints[18] < this.longestQueryTimeMs) {
                    logMessage.append("\n\tbetween ");
                    logMessage.append(this.perfMetricsHistBreakpoints[18]);
                    logMessage.append(" and ");
                    logMessage.append(this.perfMetricsHistBreakpoints[19]);
                    logMessage.append(" ms: \t");
                    logMessage.append(this.perfMetricsHistCounts[19]);
                }
            }
            if (this.numTablesMetricsHistBreakpoints != null) {
                logMessage.append("\n\n\tTable Join Histogram:\n");
                final int maxNumPoints = 20;
                int highestCount = Integer.MIN_VALUE;
                for (int i = 0; i < 20; ++i) {
                    if (this.numTablesMetricsHistCounts[i] > highestCount) {
                        highestCount = this.numTablesMetricsHistCounts[i];
                    }
                }
                if (highestCount == 0) {
                    highestCount = 1;
                }
                for (int i = 0; i < 19; ++i) {
                    if (i == 0) {
                        logMessage.append("\n\t" + this.numTablesMetricsHistBreakpoints[i + 1] + " tables or less: \t\t" + this.numTablesMetricsHistCounts[i]);
                    }
                    else {
                        logMessage.append("\n\tbetween " + this.numTablesMetricsHistBreakpoints[i] + " and " + this.numTablesMetricsHistBreakpoints[i + 1] + " tables: \t" + this.numTablesMetricsHistCounts[i]);
                    }
                    logMessage.append("\t");
                    for (int numPointsToGraph = (int)(maxNumPoints * (this.numTablesMetricsHistCounts[i] / (double)highestCount)), j = 0; j < numPointsToGraph; ++j) {
                        logMessage.append("*");
                    }
                    if (this.maximumNumberTablesAccessed < this.numTablesMetricsHistBreakpoints[i + 1]) {
                        break;
                    }
                }
                if (this.numTablesMetricsHistBreakpoints[18] < this.maximumNumberTablesAccessed) {
                    logMessage.append("\n\tbetween ");
                    logMessage.append(this.numTablesMetricsHistBreakpoints[18]);
                    logMessage.append(" and ");
                    logMessage.append(this.numTablesMetricsHistBreakpoints[19]);
                    logMessage.append(" tables: ");
                    logMessage.append(this.numTablesMetricsHistCounts[19]);
                }
            }
            this.log.logInfo(logMessage);
            this.metricsLastReportedMs = System.currentTimeMillis();
        }
    }
    
    protected void reportMetricsIfNeeded() {
        if (this.getGatherPerformanceMetrics() && System.currentTimeMillis() - this.metricsLastReportedMs > this.getReportMetricsIntervalMillis()) {
            this.reportMetrics();
        }
    }
    
    @Override
    public void reportNumberOfTablesAccessed(final int numTablesAccessed) {
        if (numTablesAccessed < this.minimumNumberTablesAccessed) {
            this.minimumNumberTablesAccessed = numTablesAccessed;
        }
        if (numTablesAccessed > this.maximumNumberTablesAccessed) {
            this.maximumNumberTablesAccessed = numTablesAccessed;
            this.repartitionTablesAccessedHistogram();
        }
        this.addToTablesAccessedHistogram(numTablesAccessed, 1);
    }
    
    @Override
    public void resetServerState() throws SQLException {
        if (!this.getParanoid() && this.io != null && this.versionMeetsMinimum(4, 0, 6)) {
            this.changeUser(this.user, this.password);
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            this.checkClosed();
            try {
                if (this.connectionLifecycleInterceptors != null) {
                    final IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                        @Override
                        void forEach(final Extension each) throws SQLException {
                            if (!((ConnectionLifecycleInterceptor)each).rollback()) {
                                this.stopIterating = true;
                            }
                        }
                    };
                    iter.doForAll();
                    if (!iter.fullIteration()) {
                        return;
                    }
                }
                if (this.autoCommit && !this.getRelaxAutoCommit()) {
                    throw SQLError.createSQLException("Can't call rollback when autocommit=true", "08003", this.getExceptionInterceptor());
                }
                if (this.transactionsSupported) {
                    try {
                        this.rollbackNoChecks();
                    }
                    catch (SQLException sqlEx) {
                        if (this.getIgnoreNonTxTables() && sqlEx.getErrorCode() == 1196) {
                            return;
                        }
                        throw sqlEx;
                    }
                }
            }
            catch (SQLException sqlException) {
                if ("08S01".equals(sqlException.getSQLState())) {
                    throw SQLError.createSQLException("Communications link failure during rollback(). Transaction resolution unknown.", "08007", this.getExceptionInterceptor());
                }
                throw sqlException;
            }
            finally {
                this.needsPing = this.getReconnectAtTxEnd();
            }
        }
    }
    
    @Override
    public void rollback(final Savepoint savepoint) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            if (!this.versionMeetsMinimum(4, 0, 14) && !this.versionMeetsMinimum(4, 1, 1)) {
                throw SQLError.createSQLFeatureNotSupportedException();
            }
            this.checkClosed();
            try {
                if (this.connectionLifecycleInterceptors != null) {
                    final IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                        @Override
                        void forEach(final Extension each) throws SQLException {
                            if (!((ConnectionLifecycleInterceptor)each).rollback(savepoint)) {
                                this.stopIterating = true;
                            }
                        }
                    };
                    iter.doForAll();
                    if (!iter.fullIteration()) {
                        return;
                    }
                }
                final StringBuilder rollbackQuery = new StringBuilder("ROLLBACK TO SAVEPOINT ");
                if (!this.isOracleMode()) {
                    rollbackQuery.append('`');
                    rollbackQuery.append(savepoint.getSavepointName());
                    rollbackQuery.append('`');
                }
                else {
                    rollbackQuery.append(savepoint.getSavepointName());
                }
                java.sql.Statement stmt = null;
                try {
                    stmt = this.getMetadataSafeStatement();
                    stmt.executeUpdate(rollbackQuery.toString());
                }
                catch (SQLException sqlEx) {
                    final int errno = sqlEx.getErrorCode();
                    if (errno == 1181) {
                        final String msg = sqlEx.getMessage();
                        if (msg != null) {
                            final int indexOfError153 = msg.indexOf("153");
                            if (indexOfError153 != -1) {
                                throw SQLError.createSQLException("Savepoint '" + savepoint.getSavepointName() + "' does not exist", "S1009", errno, this.getExceptionInterceptor());
                            }
                        }
                    }
                    if (this.getIgnoreNonTxTables() && sqlEx.getErrorCode() != 1196) {
                        throw sqlEx;
                    }
                    if ("08S01".equals(sqlEx.getSQLState())) {
                        throw SQLError.createSQLException("Communications link failure during rollback(). Transaction resolution unknown.", "08007", this.getExceptionInterceptor());
                    }
                    throw sqlEx;
                }
                finally {
                    this.closeStatement(stmt);
                }
            }
            finally {
                this.needsPing = this.getReconnectAtTxEnd();
            }
        }
    }
    
    private void rollbackNoChecks() throws SQLException {
        if (this.getUseLocalTransactionState() && this.versionMeetsMinimum(5, 0, 0) && !this.io.inTransactionOnServer()) {
            return;
        }
        this.execSQL(null, "rollback", -1, null, 1003, 1007, false, this.database, null, false);
    }
    
    @Override
    public java.sql.PreparedStatement serverPrepareStatement(final String sql) throws SQLException {
        final String nativeSql = this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
        return ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.getCatalog(), 1003, 1007);
    }
    
    @Override
    public java.sql.PreparedStatement serverPrepareStatement(final String sql, final int autoGenKeyIndex) throws SQLException {
        final String nativeSql = this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
        final PreparedStatement pStmt = ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.getCatalog(), 1003, 1007);
        pStmt.setRetrieveGeneratedKeys(autoGenKeyIndex == 1);
        return pStmt;
    }
    
    @Override
    public java.sql.PreparedStatement serverPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        final String nativeSql = this.getProcessEscapeCodesForPrepStmts() ? this.nativeSQL(sql) : sql;
        return ServerPreparedStatement.getInstance(this.getMultiHostSafeProxy(), nativeSql, this.getCatalog(), resultSetType, resultSetConcurrency);
    }
    
    @Override
    public java.sql.PreparedStatement serverPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        if (this.getPedantic() && resultSetHoldability != 1) {
            throw SQLError.createSQLException("HOLD_CUSRORS_OVER_COMMIT is only supported holdability level", "S1009", this.getExceptionInterceptor());
        }
        return this.serverPrepareStatement(sql, resultSetType, resultSetConcurrency);
    }
    
    @Override
    public java.sql.PreparedStatement serverPrepareStatement(final String sql, final int[] autoGenKeyIndexes) throws SQLException {
        final PreparedStatement pStmt = (PreparedStatement)this.serverPrepareStatement(sql);
        pStmt.setRetrieveGeneratedKeys(autoGenKeyIndexes != null && autoGenKeyIndexes.length > 0);
        return pStmt;
    }
    
    @Override
    public java.sql.PreparedStatement serverPrepareStatement(final String sql, final String[] autoGenKeyColNames) throws SQLException {
        final PreparedStatement pStmt = (PreparedStatement)this.serverPrepareStatement(sql);
        pStmt.setRetrieveGeneratedKeys(autoGenKeyColNames != null && autoGenKeyColNames.length > 0);
        return pStmt;
    }
    
    @Override
    public boolean serverSupportsConvertFn() throws SQLException {
        return this.versionMeetsMinimum(4, 0, 2);
    }
    
    @Override
    public void setAutoCommit(final boolean autoCommitFlag) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            this.checkClosed();
            if (this.connectionLifecycleInterceptors != null) {
                final IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                    @Override
                    void forEach(final Extension each) throws SQLException {
                        if (!((ConnectionLifecycleInterceptor)each).setAutoCommit(autoCommitFlag)) {
                            this.stopIterating = true;
                        }
                    }
                };
                iter.doForAll();
                if (!iter.fullIteration()) {
                    return;
                }
            }
            if (this.getAutoReconnectForPools()) {
                this.setHighAvailability(true);
            }
            try {
                if (this.transactionsSupported) {
                    boolean needsSetOnServer = true;
                    if (this.getUseLocalSessionState() && this.autoCommit == autoCommitFlag) {
                        needsSetOnServer = false;
                    }
                    else if (!this.getHighAvailability()) {
                        needsSetOnServer = this.getIO().isSetNeededForAutoCommitMode(autoCommitFlag);
                    }
                    this.autoCommit = autoCommitFlag;
                    if (needsSetOnServer) {
                        this.execSQL(null, autoCommitFlag ? "SET autocommit=1" : "SET autocommit=0", -1, null, 1003, 1007, false, this.database, null, false);
                    }
                }
                else {
                    if (!autoCommitFlag && !this.getRelaxAutoCommit()) {
                        throw SQLError.createSQLException("MySQL Versions Older than 3.23.15 do not support transactions", "08003", this.getExceptionInterceptor());
                    }
                    this.autoCommit = autoCommitFlag;
                }
            }
            finally {
                if (this.getAutoReconnectForPools()) {
                    this.setHighAvailability(false);
                }
            }
        }
    }
    
    @Override
    public void setCatalog(final String catalog) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            this.checkClosed();
            if (catalog == null) {
                throw SQLError.createSQLException("Catalog can not be null", "S1009", this.getExceptionInterceptor());
            }
            if (this.connectionLifecycleInterceptors != null) {
                final IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                    @Override
                    void forEach(final Extension each) throws SQLException {
                        if (!((ConnectionLifecycleInterceptor)each).setCatalog(catalog)) {
                            this.stopIterating = true;
                        }
                    }
                };
                iter.doForAll();
                if (!iter.fullIteration()) {
                    return;
                }
            }
            if (this.getUseLocalSessionState()) {
                if (this.lowerCaseTableNames) {
                    if (this.database.equalsIgnoreCase(catalog)) {
                        return;
                    }
                }
                else if (this.database.equals(catalog)) {
                    return;
                }
            }
            String quotedId = this.dbmd.getIdentifierQuoteString();
            if (quotedId == null || quotedId.equals(" ")) {
                quotedId = "";
            }
            StringBuilder query;
            if (!this.isOracleMode()) {
                query = new StringBuilder("USE ");
                query.append(StringUtils.quoteIdentifier(catalog, quotedId, this.getPedantic()));
            }
            else {
                query = new StringBuilder("alter session set current_schema= ");
                query.append(StringUtils.quoteIdentifier(catalog, "\"", this.getPedantic()));
            }
            this.execSQL(null, query.toString(), -1, null, 1003, 1007, false, this.database, null, false);
            this.database = catalog;
        }
    }
    
    @Override
    public void setFailedOver(final boolean flag) {
    }
    
    @Override
    public void setHoldability(final int arg0) throws SQLException {
    }
    
    @Override
    public void setInGlobalTx(final boolean flag) {
        this.isInGlobalTx = flag;
    }
    
    @Deprecated
    @Override
    public void setPreferSlaveDuringFailover(final boolean flag) {
    }
    
    @Override
    public void setReadInfoMsgEnabled(final boolean flag) {
        this.readInfoMsg = flag;
    }
    
    @Override
    public void setReadOnly(final boolean readOnlyFlag) throws SQLException {
        this.checkClosed();
        this.setReadOnlyInternal(readOnlyFlag);
    }
    
    @Override
    public void setReadOnlyInternal(final boolean readOnlyFlag) throws SQLException {
        if (this.getReadOnlyPropagatesToServer() && this.versionMeetsMinimum(5, 6, 5) && (!this.getUseLocalSessionState() || readOnlyFlag != this.readOnly)) {
            this.execSQL(null, "set session transaction " + (readOnlyFlag ? "read only" : "read write"), -1, null, 1003, 1007, false, this.database, null, false);
        }
        this.readOnly = readOnlyFlag;
    }
    
    @Override
    public Savepoint setSavepoint() throws SQLException {
        final MysqlSavepoint savepoint = new MysqlSavepoint(this.getExceptionInterceptor());
        this.setSavepoint(savepoint);
        return savepoint;
    }
    
    private void setSavepoint(final MysqlSavepoint savepoint) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            if (!this.versionMeetsMinimum(4, 0, 14) && !this.versionMeetsMinimum(4, 1, 1)) {
                throw SQLError.createSQLFeatureNotSupportedException();
            }
            this.checkClosed();
            final StringBuilder savePointQuery = new StringBuilder("SAVEPOINT ");
            if (!this.isOracleMode()) {
                savePointQuery.append('`');
                savePointQuery.append(savepoint.getSavepointName());
                savePointQuery.append('`');
            }
            else {
                savePointQuery.append(savepoint.getSavepointName());
            }
            java.sql.Statement stmt = null;
            try {
                stmt = this.getMetadataSafeStatement();
                stmt.executeUpdate(savePointQuery.toString());
            }
            finally {
                this.closeStatement(stmt);
            }
        }
    }
    
    @Override
    public Savepoint setSavepoint(final String name) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            final MysqlSavepoint savepoint = new MysqlSavepoint(name, this.getExceptionInterceptor());
            this.setSavepoint(savepoint);
            return savepoint;
        }
    }
    
    private void setSessionVariables() throws SQLException {
        if (this.versionMeetsMinimum(4, 0, 0) && this.getSessionVariables() != null) {
            final List<String> variablesToSet = StringUtils.split(this.getSessionVariables(), ",", "\"'", "\"'", false);
            final int numVariablesToSet = variablesToSet.size();
            java.sql.Statement stmt = null;
            try {
                stmt = this.getMetadataSafeStatement();
                for (int i = 0; i < numVariablesToSet; ++i) {
                    final String variableValuePair = variablesToSet.get(i);
                    if (variableValuePair.startsWith("@")) {
                        stmt.executeUpdate("SET " + variableValuePair);
                    }
                    else {
                        stmt.executeUpdate("SET SESSION " + variableValuePair);
                    }
                }
            }
            finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        }
    }
    
    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            this.checkClosed();
            if (!this.hasIsolationLevels) {
                throw SQLError.createSQLException("Transaction Isolation Levels are not supported on MySQL versions older than 3.23.36.", "S1C00", this.getExceptionInterceptor());
            }
            String sql = null;
            boolean shouldSendSet = false;
            if (this.getAlwaysSendSetIsolation()) {
                shouldSendSet = true;
            }
            else if (level != this.isolationLevel) {
                shouldSendSet = true;
            }
            if (this.getUseLocalSessionState()) {
                shouldSendSet = (this.isolationLevel != level);
            }
            if (shouldSendSet) {
                switch (level) {
                    case 0: {
                        throw SQLError.createSQLException("Transaction isolation level NONE not supported by MySQL", this.getExceptionInterceptor());
                    }
                    case 2: {
                        sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ COMMITTED";
                        break;
                    }
                    case 1: {
                        sql = "SET SESSION TRANSACTION ISOLATION LEVEL READ UNCOMMITTED";
                        break;
                    }
                    case 4: {
                        sql = "SET SESSION TRANSACTION ISOLATION LEVEL REPEATABLE READ";
                        break;
                    }
                    case 8: {
                        sql = "SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE";
                        break;
                    }
                    default: {
                        throw SQLError.createSQLException("Unsupported transaction isolation level '" + level + "'", "S1C00", this.getExceptionInterceptor());
                    }
                }
                this.execSQL(null, sql, -1, null, 1003, 1007, false, this.database, null, false);
                this.isolationLevel = level;
            }
        }
    }
    
    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            this.typeMap = map;
        }
    }
    
    private void setupServerForTruncationChecks() throws SQLException {
        if (this.getJdbcCompliantTruncation() && this.versionMeetsMinimum(5, 0, 2)) {
            final String currentSqlMode = this.serverVariables.get("sql_mode");
            final boolean strictTransTablesIsSet = StringUtils.indexOfIgnoreCase(currentSqlMode, "STRICT_TRANS_TABLES") != -1;
            if (currentSqlMode == null || currentSqlMode.length() == 0 || !strictTransTablesIsSet) {
                final StringBuilder commandBuf = new StringBuilder("SET sql_mode='");
                if (currentSqlMode != null && currentSqlMode.length() > 0) {
                    commandBuf.append(currentSqlMode);
                    commandBuf.append(",");
                }
                commandBuf.append("STRICT_TRANS_TABLES'");
                this.execSQL(null, commandBuf.toString(), -1, null, 1003, 1007, false, this.database, null, false);
                this.setJdbcCompliantTruncation(false);
            }
            else if (strictTransTablesIsSet) {
                this.setJdbcCompliantTruncation(false);
            }
        }
    }
    
    @Override
    public void shutdownServer() throws SQLException {
        try {
            this.io.sendCommand(8, null, null, false, null, 0);
        }
        catch (Exception ex) {
            final SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.UnhandledExceptionDuringShutdown"), "S1000", this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }
    
    @Override
    public boolean supportsIsolationLevel() {
        return this.hasIsolationLevels;
    }
    
    @Override
    public boolean supportsQuotedIdentifiers() {
        return this.hasQuotedIdentifiers;
    }
    
    @Override
    public boolean supportsTransactions() {
        return this.transactionsSupported;
    }
    
    @Override
    public void unregisterStatement(final Statement stmt) {
        this.openStatements.remove(stmt);
    }
    
    @Override
    public boolean useAnsiQuotedIdentifiers() {
        synchronized (this.getConnectionMutex()) {
            return this.useAnsiQuotes;
        }
    }
    
    @Override
    public boolean versionMeetsMinimum(final int major, final int minor, final int subminor) throws SQLException {
        this.checkClosed();
        return this.io.versionMeetsMinimum(major, minor, subminor);
    }
    
    @Override
    public CachedResultSetMetaData getCachedMetaData(final String sql) {
        if (this.resultSetMetadataCache != null) {
            synchronized (this.resultSetMetadataCache) {
                return ((LinkedHashMap<K, CachedResultSetMetaData>)this.resultSetMetadataCache).get(sql);
            }
        }
        return null;
    }
    
    @Override
    public void initializeResultsMetadataFromCache(final String sql, CachedResultSetMetaData cachedMetaData, final ResultSetInternalMethods resultSet) throws SQLException {
        if (cachedMetaData == null) {
            cachedMetaData = new CachedResultSetMetaData();
            resultSet.buildIndexMapping();
            resultSet.initializeWithMetadata();
            if (resultSet instanceof UpdatableResultSet) {
                ((UpdatableResultSet)resultSet).checkUpdatability();
            }
            resultSet.populateCachedMetaData(cachedMetaData);
            ((HashMap<String, CachedResultSetMetaData>)this.resultSetMetadataCache).put(sql, cachedMetaData);
        }
        else {
            resultSet.initializeFromCachedMetaData(cachedMetaData);
            resultSet.initializeWithMetadata();
            if (resultSet instanceof UpdatableResultSet) {
                ((UpdatableResultSet)resultSet).checkUpdatability();
            }
        }
    }
    
    @Override
    public String getStatementComment() {
        return this.statementComment;
    }
    
    @Override
    public void setStatementComment(final String comment) {
        this.statementComment = comment;
    }
    
    @Override
    public void reportQueryTime(final long millisOrNanos) {
        synchronized (this.getConnectionMutex()) {
            ++this.queryTimeCount;
            this.queryTimeSum += millisOrNanos;
            this.queryTimeSumSquares += millisOrNanos * millisOrNanos;
            this.queryTimeMean = (this.queryTimeMean * (this.queryTimeCount - 1L) + millisOrNanos) / this.queryTimeCount;
        }
    }
    
    @Override
    public boolean isAbonormallyLongQuery(final long millisOrNanos) {
        synchronized (this.getConnectionMutex()) {
            if (this.queryTimeCount < 15L) {
                return false;
            }
            final double stddev = Math.sqrt((this.queryTimeSumSquares - this.queryTimeSum * this.queryTimeSum / this.queryTimeCount) / (this.queryTimeCount - 1L));
            return millisOrNanos > this.queryTimeMean + 5.0 * stddev;
        }
    }
    
    @Override
    public void initializeExtension(final Extension ex) throws SQLException {
        ex.init(this, this.props);
    }
    
    @Override
    public void transactionBegun() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            if (this.connectionLifecycleInterceptors != null) {
                final IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                    @Override
                    void forEach(final Extension each) throws SQLException {
                        ((ConnectionLifecycleInterceptor)each).transactionBegun();
                    }
                };
                iter.doForAll();
            }
        }
    }
    
    @Override
    public void transactionCompleted() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            if (this.connectionLifecycleInterceptors != null) {
                final IterateBlock<Extension> iter = new IterateBlock<Extension>(this.connectionLifecycleInterceptors.iterator()) {
                    @Override
                    void forEach(final Extension each) throws SQLException {
                        ((ConnectionLifecycleInterceptor)each).transactionCompleted();
                    }
                };
                iter.doForAll();
            }
        }
    }
    
    @Override
    public boolean storesLowerCaseTableName() {
        return this.storesLowerCaseTableName;
    }
    
    @Override
    public ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }
    
    @Override
    public boolean getRequiresEscapingEncoder() {
        return this.requiresEscapingEncoder;
    }
    
    @Override
    public boolean isServerLocal() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            final SocketFactory factory = this.getIO().socketFactory;
            if (factory instanceof SocketMetadata) {
                return ((SocketMetadata)factory).isLocallyConnected(this);
            }
            this.getLog().logWarn(Messages.getString("Connection.NoMetadataOnSocketFactory"));
            return false;
        }
    }
    
    @Override
    public int getSessionMaxRows() {
        synchronized (this.getConnectionMutex()) {
            return this.sessionMaxRows;
        }
    }
    
    @Override
    public void setSessionMaxRows(final int max) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            if (this.sessionMaxRows != max) {
                this.sessionMaxRows = max;
                this.execSQL(null, "SET SQL_SELECT_LIMIT=" + ((this.sessionMaxRows == -1) ? "DEFAULT" : Integer.valueOf(this.sessionMaxRows)), -1, null, 1003, 1007, false, this.database, null, false);
            }
        }
    }
    
    @Override
    public void setSchema(final String schema) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            this.checkClosed();
        }
    }
    
    @Override
    public String getSchema() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            this.checkClosed();
            return null;
        }
    }
    
    @Override
    public void abort(final Executor executor) throws SQLException {
        final SecurityManager sec = System.getSecurityManager();
        if (sec != null) {
            sec.checkPermission(ConnectionImpl.ABORT_PERM);
        }
        if (executor == null) {
            throw SQLError.createSQLException("Executor can not be null", "S1009", this.getExceptionInterceptor());
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ConnectionImpl.this.abortInternal();
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    
    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        synchronized (this.getConnectionMutex()) {
            final SecurityManager sec = System.getSecurityManager();
            if (sec != null) {
                sec.checkPermission(ConnectionImpl.SET_NETWORK_TIMEOUT_PERM);
            }
            if (executor == null) {
                throw SQLError.createSQLException("Executor can not be null", "S1009", this.getExceptionInterceptor());
            }
            this.checkClosed();
            final MysqlIO mysqlIo = this.io;
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        ConnectionImpl.this.setSocketTimeout(milliseconds);
                        mysqlIo.setSocketTimeout(milliseconds);
                    }
                    catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
    
    @Override
    public int getNetworkTimeout() throws SQLException {
        synchronized (this.getConnectionMutex()) {
            this.checkClosed();
            return this.getSocketTimeout();
        }
    }
    
    @Override
    public ProfilerEventHandler getProfilerEventHandlerInstance() {
        return this.eventSink;
    }
    
    @Override
    public void setProfilerEventHandlerInstance(final ProfilerEventHandler h) {
        this.eventSink = h;
    }
    
    @Override
    public long getObGroupDatasourceId() {
        return this.obGroupDatasourceId;
    }
    
    @Override
    public void setObGroupDatasourceId(final long obGroupDatasourceId) {
        this.obGroupDatasourceId = obGroupDatasourceId;
    }
    
    @Override
    public long getLocalConnectionId() {
        return this.localConnectionId;
    }
    
    @Override
    public ConnectionStats getConnectionStats() {
        return this.connStats;
    }
    
    static {
        ConnectionImpl.globalConnectionCounter = new AtomicLong(0L);
        SET_NETWORK_TIMEOUT_PERM = new SQLPermission("setNetworkTimeout");
        ABORT_PERM = new SQLPermission("abort");
        CHARSET_CONVERTER_NOT_AVAILABLE_MARKER = new Object();
        ConnectionImpl.mapTransIsolationNameToValue = null;
        NULL_LOGGER = new NullLogger("OceanBase");
        dynamicIndexToCollationMapByUrl = new HashMap<String, Map<Number, String>>();
        dynamicIndexToCharsetMapByUrl = new HashMap<String, Map<Integer, String>>();
        customIndexToCharsetMapByUrl = new HashMap<String, Map<Integer, String>>();
        customCharsetToMblenMapByUrl = new HashMap<String, Map<String, Integer>>();
        (ConnectionImpl.mapTransIsolationNameToValue = new HashMap<String, Integer>(8)).put("READ-UNCOMMITED", 1);
        ConnectionImpl.mapTransIsolationNameToValue.put("READ-UNCOMMITTED", 1);
        ConnectionImpl.mapTransIsolationNameToValue.put("READ-COMMITTED", 2);
        ConnectionImpl.mapTransIsolationNameToValue.put("REPEATABLE-READ", 4);
        ConnectionImpl.mapTransIsolationNameToValue.put("SERIALIZABLE", 8);
        Label_0285: {
            if (Util.isJdbc4()) {
                try {
                    JDBC_4_CONNECTION_CTOR = Class.forName("com.alipay.oceanbase.jdbc.JDBC4Connection").getConstructor(String.class, Integer.TYPE, Properties.class, String.class, String.class);
                    break Label_0285;
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
            JDBC_4_CONNECTION_CTOR = null;
        }
        random = new Random();
    }
    
    public class ExceptionInterceptorChain implements ExceptionInterceptor
    {
        private List<Extension> interceptors;
        
        ExceptionInterceptorChain(final String interceptorClasses) throws SQLException {
            this.interceptors = Util.loadExtensions(ConnectionImpl.this, ConnectionImpl.this.props, interceptorClasses, "Connection.BadExceptionInterceptor", this);
        }
        
        void addRingZero(final ExceptionInterceptor interceptor) throws SQLException {
            this.interceptors.add(0, interceptor);
        }
        
        @Override
        public SQLException interceptException(SQLException sqlEx, final Connection conn) {
            if (this.interceptors != null) {
                final Iterator<Extension> iter = this.interceptors.iterator();
                while (iter.hasNext()) {
                    sqlEx = iter.next().interceptException(sqlEx, ConnectionImpl.this);
                }
            }
            return sqlEx;
        }
        
        @Override
        public void destroy() {
            if (this.interceptors != null) {
                final Iterator<Extension> iter = this.interceptors.iterator();
                while (iter.hasNext()) {
                    iter.next().destroy();
                }
            }
        }
        
        @Override
        public void init(final Connection conn, final Properties properties) throws SQLException {
            if (this.interceptors != null) {
                final Iterator<Extension> iter = this.interceptors.iterator();
                while (iter.hasNext()) {
                    iter.next().init(conn, properties);
                }
            }
        }
        
        public List<Extension> getInterceptors() {
            return this.interceptors;
        }
    }
    
    static class CompoundCacheKey
    {
        String componentOne;
        String componentTwo;
        int hashCode;
        
        CompoundCacheKey(final String partOne, final String partTwo) {
            this.componentOne = partOne;
            this.componentTwo = partTwo;
            this.hashCode = (((this.componentOne != null) ? this.componentOne : "") + this.componentTwo).hashCode();
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj instanceof CompoundCacheKey) {
                final CompoundCacheKey another = (CompoundCacheKey)obj;
                boolean firstPartEqual = false;
                if (this.componentOne == null) {
                    firstPartEqual = (another.componentOne == null);
                }
                else {
                    firstPartEqual = this.componentOne.equals(another.componentOne);
                }
                return firstPartEqual && this.componentTwo.equals(another.componentTwo);
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            return this.hashCode;
        }
    }
}
