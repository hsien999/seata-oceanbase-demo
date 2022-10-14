// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.util.LinkedHashMap;
import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import java.sql.SQLFeatureNotSupportedException;
import java.net.SocketException;
import java.security.Permission;
import java.sql.SQLPermission;
import java.util.concurrent.Executor;
import com.oceanbase.jdbc.extend.datatype.StructImpl;
import java.sql.Struct;
import com.oceanbase.jdbc.extend.datatype.ArrayImpl;
import com.oceanbase.jdbc.extend.datatype.ComplexDataType;
import java.sql.Array;
import java.util.Properties;
import java.sql.SQLClientInfoException;
import java.sql.ClientInfoStatus;
import java.util.HashMap;
import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.Blob;
import java.sql.Clob;
import java.util.UUID;
import java.util.Set;
import java.sql.ResultSet;
import java.sql.SQLWarning;
import java.sql.DatabaseMetaData;
import java.sql.Savepoint;
import java.util.regex.Matcher;
import com.oceanbase.jdbc.internal.util.dao.CloneableCallableStatement;
import com.oceanbase.jdbc.internal.util.dao.CallableStatementCacheKey;
import java.sql.SQLSyntaxErrorException;
import java.util.Locale;
import java.sql.CallableStatement;
import java.sql.SQLNonTransientConnectionException;
import com.oceanbase.jdbc.internal.util.StringCacheUtil;
import java.sql.PreparedStatement;
import java.sql.Statement;
import com.oceanbase.jdbc.internal.util.pool.Pools;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.Utils;
import java.util.Map;
import java.sql.Connection;
import java.util.TimeZone;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import com.oceanbase.jdbc.internal.util.LRUCache;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.internal.util.CallableStatementCache;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;
import com.oceanbase.jdbc.internal.logging.Logger;

public class OceanBaseConnection implements ConnectionImpl
{
    private static final Logger logger;
    private static final Pattern CALLABLE_STATEMENT_PATTERN;
    private static final Pattern PREPARABLE_STATEMENT_PATTERN;
    public final ReentrantLock lock;
    private final Protocol protocol;
    private final Options options;
    public OceanBasePooledConnection pooledConnection;
    protected boolean nullCatalogMeansCurrent;
    private CallableStatementCache callableStatementCache;
    private volatile int lowercaseTableNames;
    private boolean canUseServerTimeout;
    private boolean sessionStateAware;
    private int stateFlag;
    private int defaultTransactionIsolation;
    private ExceptionFactory exceptionFactory;
    private boolean warningsCleared;
    private LRUCache serverSideStatementCheckCache;
    private LRUCache serverSideStatementCache;
    private LRUCache complexDataCache;
    private UrlParser urlParser;
    private GlobalStateInfo globalStateInfo;
    private TimeZone sessionTimeZone;
    private static final int DEPTH_INDEX = 1;
    private static final int PARENT_TYPE_INDEX = 3;
    private static final int CHILD_TYPE_INDEX = 4;
    private static final int ATTR_NO_INDEX = 5;
    private static final int CHILD_OWNER_INDEX = 6;
    private static final int ATTR_TYPE_INDEX = 7;
    private String origHostToConnectTo;
    private boolean isInGlobalTx;
    private boolean enableNetworkStatistics;
    private int origPortToConnectTo;
    private String origDatabaseToConnectTo;
    private Connection complexConnection;
    private boolean autoCommit;
    private Map<String, Class<?>> typeMap;
    private static final String complexTypeSql = "SELECT\n  0 DEPTH,\n  NULL PARENT_OWNER,\n  NULL PARENT_TYPE,\n  to_char(TYPE_NAME) CHILD_TYPE,\n  0 ATTR_NO,\n  SYS_CONTEXT('USERENV', 'CURRENT_USER') CHILD_TYPE_OWNER,\n  A.TYPECODE ATTR_TYPE_CODE,\n  NULL LENGTH,\n  NULL NUMBER_PRECISION,\n  NULL SCALE,\n  NULL CHARACTER_SET_NAME\nFROM\n  USER_TYPES A WHERE TYPE_NAME = ?\nUNION\n(\nWITH \nCTE_RESULT(PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME) \nAS (\n    SELECT\n      SYS_CONTEXT('USERENV','CURRENT_USER') PARENT_OWNER,\n      B.TYPE_NAME PARENT_TYPE,\n      B.ELEM_TYPE_NAME CHILD_TYPE,\n      0 ATTR_NO,\n      B.ELEM_TYPE_OWNER CHILD_TYPE_OWNER,\n      NVL(A.TYPECODE, B.ELEM_TYPE_NAME) AS ATTR_TYPE_CODE,\n      B.LENGTH LENGTH,\n      B.NUMBER_PRECISION NUMBER_PRECISION,\n      B.SCALE SCALE,\n      B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n    FROM\n      USER_COLL_TYPES B LEFT JOIN USER_TYPES A ON A.TYPE_NAME = B.ELEM_TYPE_NAME\n    UNION\n    SELECT\n      SYS_CONTEXT('USERENV','CURRENT_USER') PARENT_OWNER,\n      B.TYPE_NAME PARENT_TYPE,\n      B.ATTR_TYPE_NAME CHILD_TYPE,\n      B.ATTR_NO ATTR_NO,\n      B.ATTR_TYPE_OWNER CHILD_TYPE_OWNER,\n      NVL(A.TYPECODE, B.ATTR_TYPE_NAME) AS ATTR_TYPE_CODE,\n      B.LENGTH LENGTH,\n      B.NUMBER_PRECISION NUMBER_PRECISION,\n      B.SCALE SCALE,\n      B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n    FROM USER_TYPE_ATTRS B LEFT JOIN USER_TYPES A ON B.ATTR_TYPE_NAME = A.TYPE_NAME ORDER BY ATTR_NO\n) ,\nCTE(DEPTH, PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME)\nAS (\n  SELECT\n    1 DEPTH,\n    PARENT_OWNER,\n    PARENT_TYPE,\n    CHILD_TYPE,\n    ATTR_NO,\n    CHILD_TYPE_OWNER,\n    ATTR_TYPE_CODE,\n    LENGTH,\n    NUMBER_PRECISION,\n    SCALE, CHARACTER_SET_NAME\n  FROM CTE_RESULT WHERE PARENT_TYPE = ?\n  UNION ALL\n  SELECT\n    DEPTH + 1 DEPTH,\n    CTE_RESULT.PARENT_OWNER,\n    CTE_RESULT.PARENT_TYPE,\n    CTE_RESULT.CHILD_TYPE,\n    CTE_RESULT.ATTR_NO,\n    CTE_RESULT.CHILD_TYPE_OWNER,\n    CTE_RESULT.ATTR_TYPE_CODE,\n    CTE_RESULT.LENGTH,\n    CTE_RESULT.NUMBER_PRECISION,\n    CTE_RESULT.SCALE,\n    CTE_RESULT.CHARACTER_SET_NAME\n  FROM CTE_RESULT INNER JOIN CTE ON CTE_RESULT.PARENT_TYPE = CTE.CHILD_TYPE\n)\nSELECT * FROM CTE\n);";
    private Map<String, Integer> indexMap;
    private int isolationLevel;
    
    public OceanBaseConnection(final Protocol protocol) {
        this.lowercaseTableNames = -1;
        this.stateFlag = 0;
        this.defaultTransactionIsolation = 0;
        this.sessionTimeZone = TimeZone.getDefault();
        this.isInGlobalTx = false;
        this.enableNetworkStatistics = false;
        this.complexConnection = null;
        this.autoCommit = true;
        this.isolationLevel = 2;
        this.protocol = protocol;
        this.options = protocol.getOptions();
        this.canUseServerTimeout = protocol.versionGreaterOrEqual(10, 1, 2);
        this.sessionStateAware = protocol.sessionStateAware();
        this.nullCatalogMeansCurrent = this.options.nullCatalogMeansCurrent;
        if (this.options.cacheCallableStmts) {
            this.callableStatementCache = CallableStatementCache.newInstance(this.options.callableStmtCacheSize);
        }
        this.lock = protocol.getLock();
        this.exceptionFactory = ExceptionFactory.of(this.getServerThreadId(), this.options);
        this.complexDataCache = new LRUCache(50);
        this.urlParser = protocol.getUrlParser();
    }
    
    public OceanBaseConnection(final UrlParser urlParser, final GlobalStateInfo globalInfo, final boolean flag) throws SQLException {
        this.lowercaseTableNames = -1;
        this.stateFlag = 0;
        this.defaultTransactionIsolation = 0;
        this.sessionTimeZone = TimeZone.getDefault();
        this.isInGlobalTx = false;
        this.enableNetworkStatistics = false;
        this.complexConnection = null;
        this.autoCommit = true;
        this.isolationLevel = 2;
        final Protocol protocol = Utils.retrieveProxy(urlParser, globalInfo);
        this.urlParser = urlParser;
        this.globalStateInfo = globalInfo;
        this.protocol = protocol;
        this.options = protocol.getOptions();
        this.canUseServerTimeout = protocol.versionGreaterOrEqual(10, 1, 2);
        this.sessionStateAware = protocol.sessionStateAware();
        this.nullCatalogMeansCurrent = this.options.nullCatalogMeansCurrent;
        if (this.options.cacheCallableStmts) {
            this.callableStatementCache = CallableStatementCache.newInstance(this.options.callableStmtCacheSize);
        }
        this.lock = protocol.getLock();
        this.exceptionFactory = ExceptionFactory.of(this.getServerThreadId(), this.options);
        this.complexDataCache = new LRUCache(50);
    }
    
    public static OceanBaseConnection newConnection(final UrlParser urlParser, final GlobalStateInfo globalInfo) throws SQLException {
        if (urlParser.getOptions().pool) {
            return Pools.retrievePool(urlParser).getConnection();
        }
        final Protocol protocol = Utils.retrieveProxy(urlParser, globalInfo);
        final OceanBaseConnection conn = new OceanBaseConnection(protocol);
        return conn;
    }
    
    public static String quoteIdentifier(final String string) {
        return "`" + string.replaceAll("`", "``") + "`";
    }
    
    @Deprecated
    public static String unquoteIdentifier(final String string) {
        if (string != null && string.startsWith("`") && string.endsWith("`") && string.length() >= 2) {
            return string.substring(1, string.length() - 1).replace("``", "`");
        }
        return string;
    }
    
    protected Protocol getProtocol() {
        return this.protocol;
    }
    
    @Override
    public Statement createStatement() throws SQLException {
        this.checkConnection();
        return new OceanBaseStatement(this, 1003, 1007, this.exceptionFactory);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) {
        return new OceanBaseStatement(this, resultSetType, resultSetConcurrency, this.exceptionFactory);
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        return new OceanBaseStatement(this, resultSetType, resultSetConcurrency, this.exceptionFactory);
    }
    
    private void checkConnection() throws SQLException {
        if (this.protocol.isExplicitClosed()) {
            throw this.exceptionFactory.create("createStatement() is called on closed connection", "08000");
        }
        if (this.protocol.isClosed() && this.protocol.getProxy() != null) {
            this.lock.lock();
            try {
                this.protocol.getProxy().reconnect();
            }
            finally {
                this.lock.unlock();
            }
        }
    }
    
    public ClientSidePreparedStatement clientPrepareStatement(final String sql) throws SQLException {
        return new ClientSidePreparedStatement(this, sql, 1003, 1007, 1, this.exceptionFactory);
    }
    
    public ServerSidePreparedStatement serverPrepareStatement(final String sql) throws SQLException {
        return new ServerSidePreparedStatement(false, this, sql, 1003, 1007, 1, this.exceptionFactory);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        return this.internalPrepareStatement(sql, 1003, 1007, 2);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        return this.internalPrepareStatement(sql, resultSetType, resultSetConcurrency, 2);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return this.internalPrepareStatement(sql, resultSetType, resultSetConcurrency, 2);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int autoGeneratedKeys) throws SQLException {
        return this.internalPrepareStatement(sql, 1003, 1007, autoGeneratedKeys);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int[] columnIndexes) throws SQLException {
        return this.prepareStatement(sql, 1);
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final String[] columnNames) throws SQLException {
        return this.prepareStatement(sql, 1);
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
    
    private PreparedStatement internalPrepareStatement(final String sql, final int resultSetScrollType, final int resultSetConcurrency, final int autoGeneratedKeys) throws SQLException {
        if (sql != null) {
            String sqlQuery = Utils.nativeSql(sql, this.protocol);
            if (this.options.useSqlStringCache) {
                sqlQuery = this.getCachedSql(sqlQuery);
            }
            if ((this.options.useServerPrepStmts && OceanBaseConnection.PREPARABLE_STATEMENT_PATTERN.matcher(sqlQuery).find()) || (this.options.useServerPrepStmts && this.protocol.isOracleMode())) {
                this.checkConnection();
                try {
                    if (this.protocol.isOracleMode() && this.options.supportNameBinding) {
                        sqlQuery = Utils.trimSQLString(sqlQuery, this.protocol.noBackslashEscapes(), this.protocol.isOracleMode(), true);
                    }
                    final ServerSidePreparedStatement ret = new ServerSidePreparedStatement(false, this, sqlQuery, resultSetScrollType, resultSetConcurrency, autoGeneratedKeys, this.exceptionFactory);
                    return ret;
                }
                catch (SQLNonTransientConnectionException e) {
                    throw e;
                }
                catch (SQLException ex) {}
            }
            if (this.protocol.isOracleMode() && this.options.supportNameBinding) {
                sqlQuery = Utils.trimSQLString(sqlQuery, this.protocol.noBackslashEscapes(), this.protocol.isOracleMode(), true);
            }
            final ClientSidePreparedStatement ret2 = new ClientSidePreparedStatement(this, sqlQuery, resultSetScrollType, resultSetConcurrency, autoGeneratedKeys, this.exceptionFactory);
            return ret2;
        }
        throw new SQLException("SQL value can not be NULL");
    }
    
    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        return this.prepareCall(sql, 1003, 1007);
    }
    
    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this.checkConnection();
        String querySetToServer = sql;
        final String uppserSQL = sql.replaceAll("--(.*?)\n", "").replaceAll("\\/\\*(.*?)\\*\\/", "").toUpperCase(Locale.ROOT).trim();
        final Matcher matcher = OceanBaseConnection.CALLABLE_STATEMENT_PATTERN.matcher(querySetToServer);
        if (this.protocol.isOracleMode() && !matcher.matches()) {
            return new OceanBaseProcedureStatement(false, sql, this, "", null, null, resultSetType, resultSetConcurrency, this.exceptionFactory, true);
        }
        if (!matcher.matches()) {
            throw new SQLSyntaxErrorException("invalid callable syntax. must be like {[?=]call <procedure/function name>[(?,?, ...)]}\n but was : " + sql);
        }
        querySetToServer = matcher.group(2);
        final boolean isFunction = matcher.group(3) != null;
        final String databaseAndProcedure = (matcher.group(8) == null) ? null : matcher.group(8).trim();
        String database = (matcher.group(10) == null) ? null : matcher.group(10).trim();
        final String procedureName = (matcher.group(13) == null) ? null : matcher.group(13).trim();
        String arguments = (matcher.group(16) == null) ? null : matcher.group(16).trim();
        if (database == null && this.sessionStateAware) {
            database = this.protocol.getDatabase();
        }
        if (database != null && this.options.cacheCallableStmts) {
            if (this.callableStatementCache.containsKey(new CallableStatementCacheKey(database, querySetToServer))) {
                try {
                    final CallableStatement callableStatement = ((LinkedHashMap<K, CallableStatement>)this.callableStatementCache).get(new CallableStatementCacheKey(database, querySetToServer));
                    if (callableStatement != null) {
                        return ((CloneableCallableStatement)callableStatement).clone(this);
                    }
                }
                catch (CloneNotSupportedException cloneNotSupportedException) {
                    cloneNotSupportedException.printStackTrace();
                }
            }
            if (this.protocol.isOracleMode() && this.options.supportNameBinding && isFunction && arguments != null) {
                arguments = Utils.trimSQLString(arguments, this.protocol.noBackslashEscapes(), this.protocol.isOracleMode(), false);
            }
            final CallableStatement callableStatement = this.createNewCallableStatement(querySetToServer, procedureName, isFunction, databaseAndProcedure, database, arguments, resultSetType, resultSetConcurrency, this.exceptionFactory);
            this.callableStatementCache.put(new CallableStatementCacheKey(database, querySetToServer), callableStatement);
            return callableStatement;
        }
        return this.createNewCallableStatement(querySetToServer, procedureName, isFunction, databaseAndProcedure, database, arguments, resultSetType, resultSetConcurrency, this.exceptionFactory);
    }
    
    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        return this.prepareCall(sql);
    }
    
    private CallableStatement createNewCallableStatement(final String query, final String procedureName, final boolean isFunction, final String databaseAndProcedure, final String database, final String arguments, final int resultSetType, final int resultSetConcurrency, final ExceptionFactory exceptionFactory) throws SQLException {
        if (isFunction) {
            if (!this.getProtocol().isOracleMode()) {
                return new OceanBaseFunctionStatement(this, database, databaseAndProcedure, (arguments == null) ? "()" : arguments, resultSetType, resultSetConcurrency, exceptionFactory);
            }
            return new OceanBaseProcedureStatement(true, "BEGIN ?:=" + databaseAndProcedure + ((arguments == null) ? "()" : arguments) + ";END;", this, procedureName, database, arguments, resultSetType, resultSetConcurrency, exceptionFactory);
        }
        else {
            if (databaseAndProcedure != null && arguments == null) {
                final String callableSql = "call " + databaseAndProcedure + "()";
                return new OceanBaseProcedureStatement(false, callableSql, this, procedureName, database, arguments, resultSetType, resultSetConcurrency, exceptionFactory);
            }
            return new OceanBaseProcedureStatement(false, query, this, procedureName, database, arguments, resultSetType, resultSetConcurrency, exceptionFactory);
        }
    }
    
    @Override
    public String nativeSQL(final String sql) throws SQLException {
        return Utils.nativeSql(sql, this.protocol);
    }
    
    @Override
    public boolean getAutoCommit() throws SQLException {
        return this.protocol.getAutocommit();
    }
    
    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        this.protocol.setAutoCommit(autoCommit);
    }
    
    @Override
    public void commit() throws SQLException {
        this.lock.lock();
        try {
            if (this.protocol.inTransaction()) {
                try (final Statement st = this.createStatement()) {
                    st.execute("COMMIT");
                }
            }
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        if (this.getAutoCommit()) {
            throw new SQLException("Can't call rollback when autocommit enable");
        }
        this.lock.lock();
        try {
            if (this.protocol.inTransaction()) {
                try (final Statement st = this.createStatement()) {
                    st.execute("ROLLBACK");
                }
            }
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public void rollback(final Savepoint savepoint) throws SQLException {
        if (this.getAutoCommit()) {
            throw new SQLException("Can't call rollback when autocommit enable");
        }
        try (final Statement st = this.createStatement()) {
            if (!this.protocol.isOracleMode()) {
                st.execute("ROLLBACK TO SAVEPOINT `" + savepoint.getSavepointName() + "`");
            }
            else {
                st.execute("ROLLBACK TO " + savepoint.getSavepointName());
            }
        }
    }
    
    @Override
    public void close() throws SQLException {
        if (this.pooledConnection != null) {
            if (!this.getAutoCommit()) {
                this.rollback();
            }
            this.pooledConnection.fireConnectionClosed();
            return;
        }
        this.protocol.closeExplicit();
    }
    
    @Override
    public boolean isClosed() {
        return this.protocol.isClosed();
    }
    
    @Override
    public DatabaseMetaData getMetaData() {
        return new OceanBaseDatabaseMetaData(this, this.protocol.getUrlParser());
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        return this.protocol.getReadonly();
    }
    
    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        try {
            OceanBaseConnection.logger.debug("conn={}({}) - set read-only to value {} {}", this.protocol.getServerThreadId(), this.protocol.isMasterConnection() ? "M" : "S", readOnly);
            this.stateFlag |= 0x4;
            this.protocol.setReadonly(readOnly);
        }
        catch (SQLException e) {
            throw this.exceptionFactory.create(e);
        }
    }
    
    @Override
    public String getCatalog() throws SQLException {
        if (this.getProtocol().isOracleMode()) {
            return null;
        }
        return this.protocol.getCatalog();
    }
    
    @Override
    public void setCatalog(final String catalog) throws SQLException {
        if (this.getProtocol().isOracleMode()) {
            return;
        }
        if (catalog == null) {
            throw new SQLException("The catalog name may not be null", "XAE05");
        }
        if (catalog.equals(this.protocol.getCatalog())) {
            return;
        }
        try {
            this.stateFlag |= 0x2;
            this.protocol.setCatalog(catalog);
        }
        catch (SQLException e) {
            throw this.exceptionFactory.create(e);
        }
    }
    
    public boolean isServerMariaDb() throws SQLException {
        return this.protocol.isServerMariaDb();
    }
    
    public boolean versionGreaterOrEqual(final int major, final int minor, final int patch) {
        return this.protocol.versionGreaterOrEqual(major, minor, patch);
    }
    
    @Override
    public int getTransactionIsolation() throws SQLException {
        return this.protocol.getTransactionIsolationLevel();
    }
    
    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        if (this.protocol.isOracleMode() && level != 2 && level != 8) {
            throw this.exceptionFactory.create("Unsupported transaction isolation level by OracleModel");
        }
        try {
            this.stateFlag |= 0x10;
            this.protocol.setTransactionIsolation(level);
        }
        catch (SQLException e) {
            throw this.exceptionFactory.create(e);
        }
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        if (this.warningsCleared || this.isClosed() || !this.protocol.hasWarnings()) {
            return null;
        }
        if (this.protocol.isOracleMode()) {
            return new SQLWarning("The execution is complete, but with warnings");
        }
        SQLWarning last = null;
        SQLWarning first = null;
        try (final Statement st = this.createStatement();
             final ResultSet rs = st.executeQuery("show warnings")) {
            while (rs.next()) {
                final int code = rs.getInt(2);
                final String message = rs.getString(3);
                final SQLWarning warning = new SQLWarning(message, null, code);
                if (first == null) {
                    first = warning;
                    last = warning;
                }
                else {
                    last.setNextWarning(warning);
                    last = warning;
                }
            }
        }
        return first;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        if (this.isClosed()) {
            throw this.exceptionFactory.create("Connection.clearWarnings cannot be called on a closed connection");
        }
        this.warningsCleared = true;
    }
    
    public void reenableWarnings() {
        this.warningsCleared = false;
    }
    
    @Override
    public Map<String, Class<?>> getTypeMap() {
        return this.typeMap;
    }
    
    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        if (this.typeMap != null && this.typeMap.size() != 0) {
            final Set<String> set = this.typeMap.keySet();
            final Class<?> clazz;
            set.forEach(s -> clazz = map.put(s, this.typeMap.get(s)));
        }
        this.typeMap = map;
    }
    
    @Override
    public int getHoldability() {
        return 1;
    }
    
    @Override
    public void setHoldability(final int holdability) {
    }
    
    @Override
    public Savepoint setSavepoint() throws SQLException {
        String randomName = "";
        if (this.protocol.isOracleMode()) {
            if (this.getAutoCommit()) {
                throw new SQLException("Unable to set a savepoint with auto-commit enabled");
            }
            for (int i = 0; i < 10; ++i) {
                final char c = (char)(Math.random() * 26.0 + 97.0);
                randomName += c;
            }
        }
        else {
            randomName = UUID.randomUUID().toString();
        }
        return this.setSavepoint(randomName);
    }
    
    @Override
    public Savepoint setSavepoint(final String name) throws SQLException {
        final Savepoint savepoint = new OceanBaseSavepoint(name);
        try (final Statement st = this.createStatement()) {
            if (this.protocol.isOracleMode()) {
                st.execute("SAVEPOINT " + savepoint.getSavepointName());
            }
            else {
                st.execute("SAVEPOINT `" + savepoint.getSavepointName() + "`");
            }
        }
        return savepoint;
    }
    
    @Override
    public void releaseSavepoint(final Savepoint savepoint) throws SQLException {
        try (final Statement st = this.createStatement()) {
            st.execute("RELEASE SAVEPOINT `" + savepoint.getSavepointName() + "`");
        }
    }
    
    @Override
    public Clob createClob() {
        return new com.oceanbase.jdbc.Clob();
    }
    
    @Override
    public Blob createBlob() {
        return new com.oceanbase.jdbc.Blob();
    }
    
    @Override
    public NClob createNClob() {
        return new JDBC4NClob("", null);
    }
    
    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw this.exceptionFactory.notSupported("SQLXML type is not supported");
    }
    
    @Override
    public boolean isValid(final int timeout) throws SQLException {
        if (timeout < 0) {
            throw new SQLException("the value supplied for timeout is negative");
        }
        if (this.isClosed()) {
            return false;
        }
        try {
            return this.protocol.isValid(timeout * 1000);
        }
        catch (SQLException e) {
            return false;
        }
    }
    
    private void checkClientClose(final String name) throws SQLClientInfoException {
        if (this.protocol.isExplicitClosed()) {
            final Map<String, ClientInfoStatus> failures = new HashMap<String, ClientInfoStatus>();
            failures.put(name, ClientInfoStatus.REASON_UNKNOWN);
            throw new SQLClientInfoException("setClientInfo() is called on closed connection", failures);
        }
    }
    
    private void checkClientReconnect(final String name) throws SQLClientInfoException {
        if (this.protocol.isClosed() && this.protocol.getProxy() != null) {
            this.lock.lock();
            try {
                this.protocol.getProxy().reconnect();
            }
            catch (SQLException sqle) {
                final Map<String, ClientInfoStatus> failures = new HashMap<String, ClientInfoStatus>();
                failures.put(name, ClientInfoStatus.REASON_UNKNOWN);
                throw new SQLClientInfoException("Connection closed", failures, sqle);
            }
            finally {
                this.lock.unlock();
            }
        }
    }
    
    private void checkClientValidProperty(final String name) throws SQLClientInfoException {
        if (name == null || (!"ApplicationName".equals(name) && !"ClientUser".equals(name) && !"ClientHostname".equals(name))) {
            final Map<String, ClientInfoStatus> failures = new HashMap<String, ClientInfoStatus>();
            failures.put(name, ClientInfoStatus.REASON_UNKNOWN_PROPERTY);
            throw new SQLClientInfoException("setClientInfo() parameters can only be \"ApplicationName\",\"ClientUser\" or \"ClientHostname\", but was : " + name, failures);
        }
    }
    
    private String buildClientQuery(final String name, final String value) {
        final StringBuilder escapeQuery = new StringBuilder("SET @").append(name).append("=");
        if (value == null) {
            escapeQuery.append("null");
        }
        else {
            escapeQuery.append("'");
            int charsOffset = 0;
            final int charsLength = value.length();
            if (this.protocol.noBackslashEscapes()) {
                while (charsOffset < charsLength) {
                    final char charValue = value.charAt(charsOffset);
                    if (charValue == '\'') {
                        escapeQuery.append('\'');
                    }
                    escapeQuery.append(charValue);
                    ++charsOffset;
                }
            }
            else {
                while (charsOffset < charsLength) {
                    final char charValue = value.charAt(charsOffset);
                    if (charValue == '\'' || charValue == '\\' || charValue == '\"' || charValue == '\0') {
                        escapeQuery.append('\\');
                    }
                    escapeQuery.append(charValue);
                    ++charsOffset;
                }
            }
            escapeQuery.append("'");
        }
        return escapeQuery.toString();
    }
    
    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
        this.checkClientClose(name);
        this.checkClientReconnect(name);
        this.checkClientValidProperty(name);
        try {
            final Statement statement = this.createStatement();
            statement.execute(this.buildClientQuery(name, value));
        }
        catch (SQLException sqle) {
            final Map<String, ClientInfoStatus> failures = new HashMap<String, ClientInfoStatus>();
            failures.put(name, ClientInfoStatus.REASON_UNKNOWN);
            throw new SQLClientInfoException("unexpected error during setClientInfo", failures, sqle);
        }
    }
    
    @Override
    public Properties getClientInfo() throws SQLException {
        this.checkConnection();
        if (this.protocol.isOracleMode()) {
            final String sql = "SELECT @ApplicationName, @ClientUser, @ClientHostname from dual";
        }
        else {
            final String sql = "SELECT @ApplicationName, @ClientUser, @ClientHostname";
        }
        final Properties properties = new Properties();
        String sql;
        try (final Statement statement = this.createStatement();
             final ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                if (rs.getString(1) != null) {
                    properties.setProperty("ApplicationName", rs.getString(1));
                }
                if (rs.getString(2) != null) {
                    properties.setProperty("ClientUser", rs.getString(2));
                }
                if (rs.getString(3) != null) {
                    properties.setProperty("ClientHostname", rs.getString(3));
                }
                return properties;
            }
        }
        properties.setProperty("ApplicationName", null);
        properties.setProperty("ClientUser", null);
        properties.setProperty("ClientHostname", null);
        return properties;
    }
    
    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException {
        final Map<String, ClientInfoStatus> propertiesExceptions = new HashMap<String, ClientInfoStatus>();
        for (final String name : new String[] { "ApplicationName", "ClientUser", "ClientHostname" }) {
            try {
                this.setClientInfo(name, properties.getProperty(name));
            }
            catch (SQLClientInfoException e) {
                propertiesExceptions.putAll(e.getFailedProperties());
            }
        }
        if (!propertiesExceptions.isEmpty()) {
            final String errorMsg = "setClientInfo errors : the following properties where not set : " + propertiesExceptions.keySet();
            throw new SQLClientInfoException(errorMsg, propertiesExceptions);
        }
    }
    
    @Override
    public String getClientInfo(final String name) throws SQLException {
        this.checkConnection();
        if (!"ApplicationName".equals(name) && !"ClientUser".equals(name) && !"ClientHostname".equals(name)) {
            throw new SQLException("name must be \"ApplicationName\", \"ClientUser\" or \"ClientHostname\", but was \"" + name + "\"");
        }
        if (this.protocol.isOracleMode()) {
            final String sql = "SELECT @" + name + " from dual";
        }
        else {
            final String sql = "SELECT @" + name;
        }
        String sql;
        try (final Statement statement = this.createStatement();
             final ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString(1);
            }
        }
        return null;
    }
    
    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        if (!this.getProtocol().isOracleMode()) {
            throw this.exceptionFactory.notSupported("Array type is not supported");
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
                    attrType = new ComplexDataType(typeName, this.getSchema(), ComplexDataType.getObComplexType(typeName));
                }
            }
        }
        if (fetchTypeFromRemote) {
            attrType = this.getComplexDataTypeFromRemote(typeName);
        }
        if (attrType.getType() == 4) {
            throw this.exceptionFactory.notSupported("array element is still array is not supported");
        }
        final ComplexDataType parentType = new ComplexDataType("", this.getSchema(), 4);
        parentType.setAttrCount(1);
        parentType.setAttrType(0, attrType);
        final ObArray array = new ArrayImpl(parentType);
        array.setAttrData(elements);
        return array;
    }
    
    public boolean getCacheComplexData() {
        return true;
    }
    
    public void recacheComplexDataType(final ComplexDataType type) {
        synchronized (this.protocol) {
            synchronized (this.complexDataCache) {
                ((HashMap<String, ComplexDataType>)this.complexDataCache).put(type.getTypeName().toUpperCase(), type);
            }
        }
    }
    
    public ComplexDataType getComplexDataType(final String typeName) throws SQLException {
        ComplexDataType type = null;
        type = this.getComplexDataTypeFromCache(typeName);
        if (null != type && type.isValid()) {
            return type;
        }
        type = this.getComplexDataTypeFromRemote(typeName);
        return type;
    }
    
    public ComplexDataType getComplexDataTypeFromCache(final String typeName) {
        synchronized (this.complexDataCache) {
            return ((LinkedHashMap<K, ComplexDataType>)this.complexDataCache).get(typeName.toUpperCase());
        }
    }
    
    private Connection getComplexConnection() throws SQLException {
        if (null == this.complexConnection || this.complexConnection.isClosed()) {
            this.complexConnection = new OceanBaseConnection(this.protocol.getUrlParser(), this.globalStateInfo, true);
        }
        return this.complexConnection;
    }
    
    public ComplexDataType getComplexDataTypeFromRemote(final String typeName) throws SQLException {
        try {
            final Connection conn = this.getComplexConnection();
            PreparedStatement ps = conn.prepareStatement("SELECT\n  0 DEPTH,\n  NULL PARENT_OWNER,\n  NULL PARENT_TYPE,\n  to_char(TYPE_NAME) CHILD_TYPE,\n  0 ATTR_NO,\n  SYS_CONTEXT('USERENV', 'CURRENT_USER') CHILD_TYPE_OWNER,\n  A.TYPECODE ATTR_TYPE_CODE,\n  NULL LENGTH,\n  NULL NUMBER_PRECISION,\n  NULL SCALE,\n  NULL CHARACTER_SET_NAME\nFROM\n  USER_TYPES A WHERE TYPE_NAME = ?\nUNION\n(\nWITH \nCTE_RESULT(PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME) \nAS (\n    SELECT\n      SYS_CONTEXT('USERENV','CURRENT_USER') PARENT_OWNER,\n      B.TYPE_NAME PARENT_TYPE,\n      B.ELEM_TYPE_NAME CHILD_TYPE,\n      0 ATTR_NO,\n      B.ELEM_TYPE_OWNER CHILD_TYPE_OWNER,\n      NVL(A.TYPECODE, B.ELEM_TYPE_NAME) AS ATTR_TYPE_CODE,\n      B.LENGTH LENGTH,\n      B.NUMBER_PRECISION NUMBER_PRECISION,\n      B.SCALE SCALE,\n      B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n    FROM\n      USER_COLL_TYPES B LEFT JOIN USER_TYPES A ON A.TYPE_NAME = B.ELEM_TYPE_NAME\n    UNION\n    SELECT\n      SYS_CONTEXT('USERENV','CURRENT_USER') PARENT_OWNER,\n      B.TYPE_NAME PARENT_TYPE,\n      B.ATTR_TYPE_NAME CHILD_TYPE,\n      B.ATTR_NO ATTR_NO,\n      B.ATTR_TYPE_OWNER CHILD_TYPE_OWNER,\n      NVL(A.TYPECODE, B.ATTR_TYPE_NAME) AS ATTR_TYPE_CODE,\n      B.LENGTH LENGTH,\n      B.NUMBER_PRECISION NUMBER_PRECISION,\n      B.SCALE SCALE,\n      B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n    FROM USER_TYPE_ATTRS B LEFT JOIN USER_TYPES A ON B.ATTR_TYPE_NAME = A.TYPE_NAME ORDER BY ATTR_NO\n) ,\nCTE(DEPTH, PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME)\nAS (\n  SELECT\n    1 DEPTH,\n    PARENT_OWNER,\n    PARENT_TYPE,\n    CHILD_TYPE,\n    ATTR_NO,\n    CHILD_TYPE_OWNER,\n    ATTR_TYPE_CODE,\n    LENGTH,\n    NUMBER_PRECISION,\n    SCALE, CHARACTER_SET_NAME\n  FROM CTE_RESULT WHERE PARENT_TYPE = ?\n  UNION ALL\n  SELECT\n    DEPTH + 1 DEPTH,\n    CTE_RESULT.PARENT_OWNER,\n    CTE_RESULT.PARENT_TYPE,\n    CTE_RESULT.CHILD_TYPE,\n    CTE_RESULT.ATTR_NO,\n    CTE_RESULT.CHILD_TYPE_OWNER,\n    CTE_RESULT.ATTR_TYPE_CODE,\n    CTE_RESULT.LENGTH,\n    CTE_RESULT.NUMBER_PRECISION,\n    CTE_RESULT.SCALE,\n    CTE_RESULT.CHARACTER_SET_NAME\n  FROM CTE_RESULT INNER JOIN CTE ON CTE_RESULT.PARENT_TYPE = CTE.CHILD_TYPE\n)\nSELECT * FROM CTE\n);", 1004, 1007);
            ps.setString(1, typeName.toUpperCase());
            ps.setString(2, typeName.toUpperCase());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                rs.beforeFirst();
            }
            else {
                rs.close();
                ps.close();
                String tmpString = null;
                if (typeName.startsWith("DBMS_XA")) {
                    tmpString = "'SYS'";
                }
                else {
                    tmpString = "SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA')";
                }
                final String complexAllTypeSql = "SELECT\n    0 DEPTH,\n    NULL PARENT_OWNER,\n    NULL PARENT_TYPE,\n    to_char(TYPE_NAME) CHILD_TYPE,\n    0 ATTR_NO,\n    OWNER CHILD_TYPE_OWNER,\n    A.TYPECODE ATTR_TYPE_CODE,\n    NULL LENGTH,\n    NULL NUMBER_PRECISION,\n    NULL SCALE,\n    NULL CHARACTER_SET_NAME\n  FROM\n    ALL_TYPES A WHERE TYPE_NAME = ? AND OWNER = " + tmpString + "\n" + "  UNION\n" + "  (\n" + "  WITH\n" + "  CTE_RESULT(PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME)\n" + "  AS (\n" + "      SELECT\n" + "        B.OWNER PARENT_OWNER,\n" + "        B.TYPE_NAME PARENT_TYPE,\n" + "        B.ELEM_TYPE_NAME CHILD_TYPE,\n" + "        0 ATTR_NO,\n" + "        B.ELEM_TYPE_OWNER CHILD_TYPE_OWNER,\n" + "        NVL(A.TYPECODE, B.ELEM_TYPE_NAME) AS ATTR_TYPE_CODE,\n" + "        B.LENGTH LENGTH,\n" + "        B.NUMBER_PRECISION NUMBER_PRECISION,\n" + "        B.SCALE SCALE,\n" + "        B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n" + "      FROM\n" + "        ALL_COLL_TYPES B LEFT JOIN ALL_TYPES A ON A.TYPE_NAME = B.ELEM_TYPE_NAME AND A.OWNER = B.ELEM_TYPE_OWNER\n" + "      UNION\n" + "      SELECT\n" + "        B.OWNER PARENT_OWNER,\n" + "        B.TYPE_NAME PARENT_TYPE,\n" + "        B.ATTR_TYPE_NAME CHILD_TYPE,\n" + "        B.ATTR_NO ATTR_NO,\n" + "        B.ATTR_TYPE_OWNER CHILD_TYPE_OWNER,\n" + "        NVL(A.TYPECODE, B.ATTR_TYPE_NAME) AS ATTR_TYPE_CODE,\n" + "        B.LENGTH LENGTH,\n" + "        B.NUMBER_PRECISION NUMBER_PRECISION,\n" + "        B.SCALE SCALE,\n" + "        B.CHARACTER_SET_NAME CHARACTER_SET_NAME\n" + "      FROM ALL_TYPE_ATTRS B LEFT JOIN ALL_TYPES A ON A.TYPE_NAME = B.ATTR_TYPE_NAME AND A.OWNER = B.ATTR_TYPE_OWNER ORDER BY ATTR_NO\n" + "  ) ,\n" + "  CTE(DEPTH, PARENT_OWNER, PARENT_TYPE, CHILD_TYPE, ATTR_NO, CHILD_TYPE_OWNER, ATTR_TYPE_CODE, LENGTH, NUMBER_PRECISION, SCALE, CHARACTER_SET_NAME)\n" + "  AS (\n" + "    SELECT\n" + "      1 DEPTH,\n" + "      PARENT_OWNER,\n" + "      PARENT_TYPE,\n" + "      CHILD_TYPE,\n" + "      ATTR_NO,\n" + "      CHILD_TYPE_OWNER,\n" + "      ATTR_TYPE_CODE,\n" + "      LENGTH,\n" + "      NUMBER_PRECISION,\n" + "      SCALE, CHARACTER_SET_NAME\n" + "    FROM CTE_RESULT WHERE PARENT_TYPE = ? AND PARENT_OWNER = " + tmpString + "\n" + "    UNION ALL\n" + "    SELECT\n" + "      DEPTH + 1 DEPTH,\n" + "      CTE_RESULT.PARENT_OWNER,\n" + "      CTE_RESULT.PARENT_TYPE,\n" + "      CTE_RESULT.CHILD_TYPE,\n" + "      CTE_RESULT.ATTR_NO,\n" + "      CTE_RESULT.CHILD_TYPE_OWNER,\n" + "      CTE_RESULT.ATTR_TYPE_CODE,\n" + "      CTE_RESULT.LENGTH,\n" + "      CTE_RESULT.NUMBER_PRECISION,\n" + "      CTE_RESULT.SCALE,\n" + "      CTE_RESULT.CHARACTER_SET_NAME\n" + "    FROM CTE_RESULT INNER JOIN CTE ON CTE_RESULT.PARENT_TYPE = CTE.CHILD_TYPE AND CTE_RESULT.PARENT_OWNER = CTE.CHILD_TYPE_OWNER\n" + "  )\n" + "  SELECT * FROM CTE\n" + "  );";
                ps = conn.prepareStatement(complexAllTypeSql, 1004, 1007);
                ps.setString(1, typeName.toUpperCase());
                ps.setString(2, typeName.toUpperCase());
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
            conn.close();
        }
        catch (SQLException e) {
            throw e;
        }
        return this.getComplexDataTypeFromCache(typeName);
    }
    
    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
        if (!this.getProtocol().isOracleMode()) {
            throw this.exceptionFactory.notSupported("Struct type is not supported");
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
        final ObStruct struct = new StructImpl(type);
        struct.setAttrData(attributes);
        return struct;
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        try {
            if (this.isWrapperFor(iface)) {
                return iface.cast(this);
            }
            throw new SQLException("The receiver is not a wrapper for " + iface.getName());
        }
        catch (Exception e) {
            throw new SQLException("The receiver is not a wrapper and does not implement the interface");
        }
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return iface.isInstance(this);
    }
    
    @Deprecated
    public String getUsername() {
        return this.protocol.getUsername();
    }
    
    @Deprecated
    public String getHostname() {
        return this.protocol.getHost();
    }
    
    @Deprecated
    public int getPort() {
        return this.protocol.getPort();
    }
    
    protected boolean getPinGlobalTxToPhysicalConnection() {
        return this.protocol.getPinGlobalTxToPhysicalConnection();
    }
    
    public void setHostFailed() {
        if (this.protocol.getProxy() == null) {
            this.protocol.setHostFailedWithoutProxy();
        }
    }
    
    public int getLowercaseTableNames() throws SQLException {
        if (this.lowercaseTableNames == -1) {
            try (final Statement st = this.createStatement();
                 final ResultSet rs = st.executeQuery("select @@lower_case_table_names")) {
                rs.next();
                this.lowercaseTableNames = rs.getInt(1);
            }
        }
        return this.lowercaseTableNames;
    }
    
    @Override
    public void abort(final Executor executor) throws SQLException {
        if (this.isClosed()) {
            return;
        }
        final SQLPermission sqlPermission = new SQLPermission("callAbort");
        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(sqlPermission);
        }
        if (executor == null) {
            throw this.exceptionFactory.create("Cannot abort the connection: null executor passed");
        }
        executor.execute(this.protocol::abort);
    }
    
    @Override
    public int getNetworkTimeout() throws SQLException {
        return this.protocol.getTimeout();
    }
    
    @Override
    public String getSchema() throws SQLException {
        if (this.protocol.isOracleMode()) {
            return this.getDatabase().toUpperCase();
        }
        return this.getCatalog();
    }
    
    public String getDatabase() {
        return this.urlParser.getDatabase();
    }
    
    @Override
    public void setSchema(final String arg0) {
    }
    
    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        if (this.isClosed()) {
            throw this.exceptionFactory.create("Connection.setNetworkTimeout cannot be called on a closed connection");
        }
        if (milliseconds < 0) {
            throw this.exceptionFactory.create("Connection.setNetworkTimeout cannot be called with a negative timeout");
        }
        final SQLPermission sqlPermission = new SQLPermission("setNetworkTimeout");
        final SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(sqlPermission);
        }
        try {
            this.stateFlag |= 0x1;
            this.protocol.setTimeout(milliseconds);
        }
        catch (SocketException se) {
            throw this.exceptionFactory.create("Cannot set the network timeout", se);
        }
    }
    
    public long getServerThreadId() {
        return this.protocol.getServerThreadId();
    }
    
    public boolean canUseServerTimeout() {
        return this.canUseServerTimeout;
    }
    
    public void setDefaultTransactionIsolation(final int defaultTransactionIsolation) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
    }
    
    public void reset() throws SQLException {
        final boolean useComReset = this.options.useResetConnection && this.protocol.isServerMariaDb() && (this.protocol.versionGreaterOrEqual(10, 3, 13) || (this.protocol.getMajorServerVersion() == 10 && this.protocol.getMinorServerVersion() == 2 && this.protocol.versionGreaterOrEqual(10, 2, 22)));
        if (useComReset) {
            this.protocol.reset();
        }
        if (this.stateFlag != 0) {
            try {
                if ((this.stateFlag & 0x1) != 0x0) {
                    this.setNetworkTimeout(null, this.options.socketTimeout);
                }
                if ((this.stateFlag & 0x8) != 0x0) {
                    this.setAutoCommit(this.options.autocommit);
                }
                if ((this.stateFlag & 0x2) != 0x0) {
                    this.protocol.resetDatabase();
                }
                if ((this.stateFlag & 0x4) != 0x0) {
                    this.setReadOnly(false);
                }
                if (!useComReset && (this.stateFlag & 0x10) != 0x0) {
                    this.setTransactionIsolation(this.defaultTransactionIsolation);
                }
                this.stateFlag = 0;
            }
            catch (SQLException sqle) {
                throw this.exceptionFactory.create("error resetting connection");
            }
        }
        this.warningsCleared = true;
    }
    
    public boolean includeDeadLockInfo() {
        return this.options.includeInnodbStatusInDeadlockExceptions;
    }
    
    public boolean includeThreadsTraces() {
        return this.options.includeThreadDumpInDeadlockExceptions;
    }
    
    @Override
    public String getSessionTimeZone() {
        return this.sessionTimeZone.getID();
    }
    
    public Statement getMetadataSafeStatement() throws SQLException {
        final Statement stmt = this.createStatement();
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
    public void setSessionTimeZone(final String zoneID) throws SQLException {
        this.checkConnection();
        if (this.protocol.isOracleMode()) {
            boolean needSetSessionTimeZone = true;
            final TimeZone targetTimeZone = TimeZone.getTimeZone(zoneID);
            if (!this.protocol.isTZTablesImported()) {
                if (null != this.sessionTimeZone && targetTimeZone.getRawOffset() == this.sessionTimeZone.getRawOffset()) {
                    needSetSessionTimeZone = false;
                }
            }
            else if (null != this.sessionTimeZone && this.sessionTimeZone.getID().equals(zoneID)) {
                needSetSessionTimeZone = false;
            }
            if (needSetSessionTimeZone) {
                final Statement stmt = this.getMetadataSafeStatement();
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
            return;
        }
        throw new SQLFeatureNotSupportedException();
    }
    
    public boolean isInGlobalTx() {
        return this.isInGlobalTx;
    }
    
    public void setInGlobalTx(final boolean flag) {
        this.isInGlobalTx = flag;
    }
    
    public long getLastPacketCostTime() throws SQLException {
        return this.protocol.getLastPacketCostTime();
    }
    
    public void networkStatistics(final boolean flag) {
        this.protocol.setNetworkStatisticsFlag(flag);
    }
    
    public void clearNetworkStatistics() {
        this.protocol.clearNetworkStatistics();
    }
    
    public long getLastPacketResponseTimestamp() {
        return this.protocol.getLastPacketResponseTimestamp();
    }
    
    public long getLastPacketSendTimestamp() {
        return this.protocol.getLastPacketSendTimestamp();
    }
    
    @Override
    public void changeUser(String userName, String newPassword) throws SQLException {
        if (userName == null || userName.equals("")) {
            userName = "";
        }
        if (newPassword == null) {
            newPassword = "";
        }
        this.protocol.changeUser(userName, newPassword);
    }
    
    static {
        logger = LoggerFactory.getLogger(OceanBaseConnection.class);
        CALLABLE_STATEMENT_PATTERN = Pattern.compile("^(\\s*\\{)?\\s*((\\?\\s*=)?(\\s*\\/\\*([^\\*]|\\*[^\\/])*\\*\\/)*\\s*call(\\s*\\/\\*([^\\*]|\\*[^\\/])*\\*\\/)*\\s*((((`[^`]+`)|([^`\\}]+))\\.)?((`[^`]+`)|([^`\\}\\(]+)))\\s*(\\(.*\\))?(\\s*\\/\\*([^\\*]|\\*[^\\/])*\\*\\/)*\\s*(#.*)?)\\s*(\\}\\s*)?$", 34);
        PREPARABLE_STATEMENT_PATTERN = Pattern.compile("^(\\s*\\/\\*([^\\*]|\\*[^\\/])*\\*\\/)*\\s*(SELECT|UPDATE|INSERT|DELETE|REPLACE|DO|CALL|DECLARE)", 2);
    }
}
