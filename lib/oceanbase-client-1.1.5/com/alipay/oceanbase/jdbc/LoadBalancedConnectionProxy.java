// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.Collections;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;
import java.util.HashSet;
import java.sql.SQLException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Properties;
import java.util.List;
import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.Map;

public class LoadBalancedConnectionProxy extends MultiHostConnectionProxy implements PingTarget
{
    private ConnectionGroup connectionGroup;
    private long connectionGroupProxyID;
    protected Map<String, ConnectionImpl> liveConnections;
    private Map<String, Integer> hostsToListIndexMap;
    private Map<ConnectionImpl, String> connectionsToHostsMap;
    private long totalPhysicalConnections;
    private long[] responseTimes;
    private int retriesAllDown;
    private BalanceStrategy balancer;
    private int autoCommitSwapThreshold;
    public static final String BLACKLIST_TIMEOUT_PROPERTY_KEY = "loadBalanceBlacklistTimeout";
    private int globalBlacklistTimeout;
    private static Map<String, Long> globalBlacklist;
    public static final String HOST_REMOVAL_GRACE_PERIOD_PROPERTY_KEY = "loadBalanceHostRemovalGracePeriod";
    private int hostRemovalGracePeriod;
    private Set<String> hostsToRemove;
    private boolean inTransaction;
    private long transactionStartTime;
    private long transactionCount;
    private LoadBalanceExceptionChecker exceptionChecker;
    private static Constructor<?> JDBC_4_LB_CONNECTION_CTOR;
    private static Class<?>[] INTERFACES_TO_PROXY;
    private static LoadBalancedConnection nullLBConnectionInstance;
    
    public static LoadBalancedConnection createProxyInstance(final List<String> hosts, final Properties props) throws SQLException {
        final LoadBalancedConnectionProxy connProxy = new LoadBalancedConnectionProxy(hosts, props);
        return (LoadBalancedConnection)Proxy.newProxyInstance(LoadBalancedConnection.class.getClassLoader(), LoadBalancedConnectionProxy.INTERFACES_TO_PROXY, connProxy);
    }
    
    private LoadBalancedConnectionProxy(List<String> hosts, final Properties props) throws SQLException {
        this.connectionGroup = null;
        this.connectionGroupProxyID = 0L;
        this.totalPhysicalConnections = 0L;
        this.autoCommitSwapThreshold = 0;
        this.globalBlacklistTimeout = 0;
        this.hostRemovalGracePeriod = 0;
        this.hostsToRemove = new HashSet<String>();
        this.inTransaction = false;
        this.transactionStartTime = 0L;
        this.transactionCount = 0L;
        final String group = props.getProperty("loadBalanceConnectionGroup", null);
        boolean enableJMX = false;
        final String enableJMXAsString = props.getProperty("loadBalanceEnableJMX", "false");
        try {
            enableJMX = Boolean.parseBoolean(enableJMXAsString);
        }
        catch (Exception e) {
            throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceEnableJMX", new Object[] { enableJMXAsString }), "S1009", null);
        }
        if (group != null) {
            this.connectionGroup = ConnectionGroupManager.getConnectionGroupInstance(group);
            if (enableJMX) {
                ConnectionGroupManager.registerJmx();
            }
            this.connectionGroupProxyID = this.connectionGroup.registerConnectionProxy(this, hosts);
            hosts = new ArrayList<String>(this.connectionGroup.getInitialHosts());
        }
        final int numHosts = this.initializeHostsSpecs(hosts, props);
        this.liveConnections = new HashMap<String, ConnectionImpl>(numHosts);
        this.hostsToListIndexMap = new HashMap<String, Integer>(numHosts);
        for (int i = 0; i < numHosts; ++i) {
            this.hostsToListIndexMap.put(this.hostList.get(i), i);
        }
        this.connectionsToHostsMap = new HashMap<ConnectionImpl, String>(numHosts);
        this.responseTimes = new long[numHosts];
        final String retriesAllDownAsString = this.localProps.getProperty("retriesAllDown", "120");
        try {
            this.retriesAllDown = Integer.parseInt(retriesAllDownAsString);
        }
        catch (NumberFormatException nfe) {
            throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForRetriesAllDown", new Object[] { retriesAllDownAsString }), "S1009", null);
        }
        final String blacklistTimeoutAsString = this.localProps.getProperty("loadBalanceBlacklistTimeout", "0");
        try {
            this.globalBlacklistTimeout = Integer.parseInt(blacklistTimeoutAsString);
        }
        catch (NumberFormatException nfe2) {
            throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceBlacklistTimeout", new Object[] { blacklistTimeoutAsString }), "S1009", null);
        }
        final String hostRemovalGracePeriodAsString = this.localProps.getProperty("loadBalanceHostRemovalGracePeriod", "15000");
        try {
            this.hostRemovalGracePeriod = Integer.parseInt(hostRemovalGracePeriodAsString);
        }
        catch (NumberFormatException nfe3) {
            throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceHostRemovalGracePeriod", new Object[] { hostRemovalGracePeriodAsString }), "S1009", null);
        }
        final String strategy = this.localProps.getProperty("loadBalanceStrategy", "random");
        if ("random".equals(strategy)) {
            this.balancer = Util.loadExtensions(null, props, "com.alipay.oceanbase.jdbc.RandomBalanceStrategy", "InvalidLoadBalanceStrategy", null).get(0);
        }
        else if ("bestResponseTime".equals(strategy)) {
            this.balancer = Util.loadExtensions(null, props, "com.alipay.oceanbase.jdbc.BestResponseTimeBalanceStrategy", "InvalidLoadBalanceStrategy", null).get(0);
        }
        else {
            this.balancer = Util.loadExtensions(null, props, strategy, "InvalidLoadBalanceStrategy", null).get(0);
        }
        final String autoCommitSwapThresholdAsString = props.getProperty("loadBalanceAutoCommitStatementThreshold", "0");
        try {
            this.autoCommitSwapThreshold = Integer.parseInt(autoCommitSwapThresholdAsString);
        }
        catch (NumberFormatException nfe4) {
            throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceAutoCommitStatementThreshold", new Object[] { autoCommitSwapThresholdAsString }), "S1009", null);
        }
        final String autoCommitSwapRegex = props.getProperty("loadBalanceAutoCommitStatementRegex", "");
        if (!"".equals(autoCommitSwapRegex)) {
            try {
                "".matches(autoCommitSwapRegex);
            }
            catch (Exception e2) {
                throw SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.badValueForLoadBalanceAutoCommitStatementRegex", new Object[] { autoCommitSwapRegex }), "S1009", null);
            }
        }
        if (this.autoCommitSwapThreshold > 0) {
            final String statementInterceptors = this.localProps.getProperty("statementInterceptors");
            if (statementInterceptors == null) {
                this.localProps.setProperty("statementInterceptors", "com.alipay.oceanbase.jdbc.LoadBalancedAutoCommitInterceptor");
            }
            else if (statementInterceptors.length() > 0) {
                this.localProps.setProperty("statementInterceptors", statementInterceptors + ",com.alipay.oceanbase.jdbc.LoadBalancedAutoCommitInterceptor");
            }
            props.setProperty("statementInterceptors", this.localProps.getProperty("statementInterceptors"));
        }
        this.balancer.init(null, props);
        final String lbExceptionChecker = this.localProps.getProperty("loadBalanceExceptionChecker", "com.alipay.oceanbase.jdbc.StandardLoadBalanceExceptionChecker");
        this.exceptionChecker = Util.loadExtensions(null, props, lbExceptionChecker, "InvalidLoadBalanceExceptionChecker", null).get(0);
        this.pickNewConnection();
    }
    
    @Override
    MySQLConnection getNewWrapperForThisAsConnection() throws SQLException {
        if (Util.isJdbc4() || LoadBalancedConnectionProxy.JDBC_4_LB_CONNECTION_CTOR != null) {
            return (MySQLConnection)Util.handleNewInstance(LoadBalancedConnectionProxy.JDBC_4_LB_CONNECTION_CTOR, new Object[] { this }, null);
        }
        return new LoadBalancedMySQLConnection(this);
    }
    
    @Override
    protected void propagateProxyDown(final MySQLConnection proxyConn) {
        for (final MySQLConnection c : this.liveConnections.values()) {
            c.setProxy(proxyConn);
        }
    }
    
    @Override
    boolean shouldExceptionTriggerConnectionSwitch(final Throwable t) {
        return t instanceof SQLException && this.exceptionChecker.shouldExceptionTriggerFailover((SQLException)t);
    }
    
    @Override
    boolean isMasterConnection() {
        return true;
    }
    
    @Override
    synchronized void invalidateConnection(final MySQLConnection conn) throws SQLException {
        super.invalidateConnection(conn);
        if (this.isGlobalBlacklistEnabled()) {
            this.addToGlobalBlacklist(this.connectionsToHostsMap.get(conn));
        }
        this.liveConnections.remove(this.connectionsToHostsMap.get(conn));
        final Object mappedHost = this.connectionsToHostsMap.remove(conn);
        if (mappedHost != null && this.hostsToListIndexMap.containsKey(mappedHost)) {
            final int hostIndex = this.hostsToListIndexMap.get(mappedHost);
            synchronized (this.responseTimes) {
                this.responseTimes[hostIndex] = 0L;
            }
        }
    }
    
    @Override
    synchronized void pickNewConnection() throws SQLException {
        if (this.isClosed && this.closedExplicitly) {
            return;
        }
        if (this.currentConnection == null) {
            this.currentConnection = this.balancer.pickConnection(this, Collections.unmodifiableList((List<? extends String>)this.hostList), Collections.unmodifiableMap((Map<? extends String, ? extends ConnectionImpl>)this.liveConnections), this.responseTimes.clone(), this.retriesAllDown);
            return;
        }
        if (this.currentConnection.isClosed()) {
            this.invalidateCurrentConnection();
        }
        final int pingTimeout = this.currentConnection.getLoadBalancePingTimeout();
        final boolean pingBeforeReturn = this.currentConnection.getLoadBalanceValidateConnectionOnSwapServer();
        int hostsTried = 0;
        final int hostsToTry = this.hostList.size();
        while (hostsTried < hostsToTry) {
            ConnectionImpl newConn = null;
            try {
                newConn = this.balancer.pickConnection(this, Collections.unmodifiableList((List<? extends String>)this.hostList), Collections.unmodifiableMap((Map<? extends String, ? extends ConnectionImpl>)this.liveConnections), this.responseTimes.clone(), this.retriesAllDown);
                if (this.currentConnection != null) {
                    if (pingBeforeReturn) {
                        if (pingTimeout == 0) {
                            newConn.ping();
                        }
                        else {
                            newConn.pingInternal(true, pingTimeout);
                        }
                    }
                    MultiHostConnectionProxy.syncSessionState(this.currentConnection, newConn);
                }
                this.currentConnection = newConn;
                return;
            }
            catch (SQLException e) {
                if (this.shouldExceptionTriggerConnectionSwitch(e) && newConn != null) {
                    this.invalidateConnection(newConn);
                }
                ++hostsTried;
                continue;
            }
            break;
        }
        this.isClosed = true;
        this.closedReason = "Connection closed after inability to pick valid new connection during load-balance.";
    }
    
    public synchronized ConnectionImpl createConnectionForHost(final String hostPortSpec) throws SQLException {
        final ConnectionImpl conn = super.createConnectionForHost(hostPortSpec);
        this.liveConnections.put(hostPortSpec, conn);
        this.connectionsToHostsMap.put(conn, hostPortSpec);
        ++this.totalPhysicalConnections;
        return conn;
    }
    
    private synchronized void closeAllConnections() {
        for (final MySQLConnection c : this.liveConnections.values()) {
            try {
                c.close();
            }
            catch (SQLException ex) {}
        }
        if (!this.isClosed) {
            this.balancer.destroy();
            if (this.connectionGroup != null) {
                this.connectionGroup.closeConnectionProxy(this);
            }
        }
        this.liveConnections.clear();
        this.connectionsToHostsMap.clear();
    }
    
    @Override
    synchronized void doClose() {
        this.closeAllConnections();
    }
    
    @Override
    synchronized void doAbortInternal() {
        for (final MySQLConnection c : this.liveConnections.values()) {
            try {
                c.abortInternal();
            }
            catch (SQLException ex) {}
        }
        if (!this.isClosed) {
            this.balancer.destroy();
            if (this.connectionGroup != null) {
                this.connectionGroup.closeConnectionProxy(this);
            }
        }
        this.liveConnections.clear();
        this.connectionsToHostsMap.clear();
    }
    
    @Override
    synchronized void doAbort(final Executor executor) {
        for (final MySQLConnection c : this.liveConnections.values()) {
            try {
                c.abort(executor);
            }
            catch (SQLException ex) {}
        }
        if (!this.isClosed) {
            this.balancer.destroy();
            if (this.connectionGroup != null) {
                this.connectionGroup.closeConnectionProxy(this);
            }
        }
        this.liveConnections.clear();
        this.connectionsToHostsMap.clear();
    }
    
    public synchronized Object invokeMore(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String methodName = method.getName();
        if (this.isClosed && !this.allowedOnClosedConnection(method) && method.getExceptionTypes().length > 0) {
            if (!this.autoReconnect || this.closedExplicitly) {
                String reason = "No operations allowed after connection closed.";
                if (this.closedReason != null) {
                    reason = reason + " " + this.closedReason;
                }
                throw SQLError.createSQLException(reason, "08003", null);
            }
            this.currentConnection = null;
            this.pickNewConnection();
            this.isClosed = false;
            this.closedReason = null;
        }
        if (!this.inTransaction) {
            this.inTransaction = true;
            this.transactionStartTime = System.nanoTime();
            ++this.transactionCount;
        }
        Object result = null;
        try {
            result = method.invoke(this.thisAsConnection, args);
            if (result != null) {
                if (result instanceof Statement) {
                    ((Statement)result).setPingTarget(this);
                }
                result = this.proxyIfReturnTypeIsJdbcInterface(method.getReturnType(), result);
            }
        }
        catch (InvocationTargetException e) {
            this.dealWithInvocationException(e);
        }
        finally {
            if ("commit".equals(methodName) || "rollback".equals(methodName)) {
                this.inTransaction = false;
                final String host = this.connectionsToHostsMap.get(this.currentConnection);
                if (host != null) {
                    synchronized (this.responseTimes) {
                        final Integer hostIndex = this.hostsToListIndexMap.get(host);
                        if (hostIndex != null && hostIndex < this.responseTimes.length) {
                            this.responseTimes[hostIndex] = System.nanoTime() - this.transactionStartTime;
                        }
                    }
                }
                this.pickNewConnection();
            }
        }
        return result;
    }
    
    @Override
    public synchronized void doPing() throws SQLException {
        SQLException se = null;
        boolean foundHost = false;
        final int pingTimeout = this.currentConnection.getLoadBalancePingTimeout();
        for (final String host : this.hostList) {
            final ConnectionImpl conn = this.liveConnections.get(host);
            if (conn == null) {
                continue;
            }
            try {
                if (pingTimeout == 0) {
                    conn.ping();
                }
                else {
                    conn.pingInternal(true, pingTimeout);
                }
                foundHost = true;
            }
            catch (SQLException e) {
                if (host.equals(this.connectionsToHostsMap.get(this.currentConnection))) {
                    this.closeAllConnections();
                    this.isClosed = true;
                    this.closedReason = "Connection closed because ping of current connection failed.";
                    throw e;
                }
                if (e.getMessage().equals(Messages.getString("Connection.exceededConnectionLifetime"))) {
                    if (se == null) {
                        se = e;
                    }
                }
                else {
                    se = e;
                    if (this.isGlobalBlacklistEnabled()) {
                        this.addToGlobalBlacklist(host);
                    }
                }
                this.liveConnections.remove(this.connectionsToHostsMap.get(conn));
            }
        }
        if (!foundHost) {
            this.closeAllConnections();
            this.isClosed = true;
            this.closedReason = "Connection closed due to inability to ping any active connections.";
            if (se != null) {
                throw se;
            }
            ((ConnectionImpl)this.currentConnection).throwConnectionClosedException();
        }
    }
    
    public void addToGlobalBlacklist(final String host, final long timeout) {
        if (this.isGlobalBlacklistEnabled()) {
            synchronized (LoadBalancedConnectionProxy.globalBlacklist) {
                LoadBalancedConnectionProxy.globalBlacklist.put(host, timeout);
            }
        }
    }
    
    public void addToGlobalBlacklist(final String host) {
        this.addToGlobalBlacklist(host, System.currentTimeMillis() + this.globalBlacklistTimeout);
    }
    
    public boolean isGlobalBlacklistEnabled() {
        return this.globalBlacklistTimeout > 0;
    }
    
    public synchronized Map<String, Long> getGlobalBlacklist() {
        if (!this.isGlobalBlacklistEnabled()) {
            if (this.hostsToRemove.isEmpty()) {
                return new HashMap<String, Long>(1);
            }
            final HashMap<String, Long> fakedBlacklist = new HashMap<String, Long>();
            for (final String h : this.hostsToRemove) {
                fakedBlacklist.put(h, System.currentTimeMillis() + 5000L);
            }
            return fakedBlacklist;
        }
        else {
            final Map<String, Long> blacklistClone = new HashMap<String, Long>(LoadBalancedConnectionProxy.globalBlacklist.size());
            synchronized (LoadBalancedConnectionProxy.globalBlacklist) {
                blacklistClone.putAll(LoadBalancedConnectionProxy.globalBlacklist);
            }
            final Set<String> keys = blacklistClone.keySet();
            keys.retainAll(this.hostList);
            final Iterator<String> i = keys.iterator();
            while (i.hasNext()) {
                final String host = i.next();
                final Long timeout = LoadBalancedConnectionProxy.globalBlacklist.get(host);
                if (timeout != null && timeout < System.currentTimeMillis()) {
                    synchronized (LoadBalancedConnectionProxy.globalBlacklist) {
                        LoadBalancedConnectionProxy.globalBlacklist.remove(host);
                    }
                    i.remove();
                }
            }
            if (keys.size() == this.hostList.size()) {
                return new HashMap<String, Long>(1);
            }
            return blacklistClone;
        }
    }
    
    public void removeHostWhenNotInUse(final String hostPortPair) throws SQLException {
        if (this.hostRemovalGracePeriod <= 0) {
            this.removeHost(hostPortPair);
            return;
        }
        final int timeBetweenChecks = (this.hostRemovalGracePeriod > 1000) ? 1000 : this.hostRemovalGracePeriod;
        synchronized (this) {
            this.addToGlobalBlacklist(hostPortPair, System.currentTimeMillis() + this.hostRemovalGracePeriod + timeBetweenChecks);
            final long cur = System.currentTimeMillis();
            while (System.currentTimeMillis() < cur + this.hostRemovalGracePeriod) {
                this.hostsToRemove.add(hostPortPair);
                if (!hostPortPair.equals(this.currentConnection.getHostPortPair())) {
                    this.removeHost(hostPortPair);
                    return;
                }
                try {
                    Thread.sleep(timeBetweenChecks);
                }
                catch (InterruptedException ex) {}
            }
        }
        this.removeHost(hostPortPair);
    }
    
    public synchronized void removeHost(final String hostPortPair) throws SQLException {
        if (this.connectionGroup != null && this.connectionGroup.getInitialHosts().size() == 1 && this.connectionGroup.getInitialHosts().contains(hostPortPair)) {
            throw SQLError.createSQLException("Cannot remove only configured host.", null);
        }
        this.hostsToRemove.add(hostPortPair);
        this.connectionsToHostsMap.remove(this.liveConnections.remove(hostPortPair));
        if (this.hostsToListIndexMap.remove(hostPortPair) != null) {
            final long[] newResponseTimes = new long[this.responseTimes.length - 1];
            int newIdx = 0;
            for (final String h : this.hostList) {
                if (!this.hostsToRemove.contains(h)) {
                    final Integer idx = this.hostsToListIndexMap.get(h);
                    if (idx != null && idx < this.responseTimes.length) {
                        newResponseTimes[newIdx] = this.responseTimes[idx];
                    }
                    this.hostsToListIndexMap.put(h, newIdx++);
                }
            }
            this.responseTimes = newResponseTimes;
        }
        if (hostPortPair.equals(this.currentConnection.getHostPortPair())) {
            this.invalidateConnection(this.currentConnection);
            this.pickNewConnection();
        }
    }
    
    public synchronized boolean addHost(final String hostPortPair) {
        if (this.hostsToListIndexMap.containsKey(hostPortPair)) {
            return false;
        }
        final long[] newResponseTimes = new long[this.responseTimes.length + 1];
        System.arraycopy(this.responseTimes, 0, newResponseTimes, 0, this.responseTimes.length);
        this.responseTimes = newResponseTimes;
        if (!this.hostList.contains(hostPortPair)) {
            this.hostList.add(hostPortPair);
        }
        this.hostsToListIndexMap.put(hostPortPair, this.responseTimes.length - 1);
        this.hostsToRemove.remove(hostPortPair);
        return true;
    }
    
    public synchronized boolean inTransaction() {
        return this.inTransaction;
    }
    
    public synchronized long getTransactionCount() {
        return this.transactionCount;
    }
    
    public synchronized long getActivePhysicalConnectionCount() {
        return this.liveConnections.size();
    }
    
    public synchronized long getTotalPhysicalConnectionCount() {
        return this.totalPhysicalConnections;
    }
    
    public synchronized long getConnectionGroupProxyID() {
        return this.connectionGroupProxyID;
    }
    
    public synchronized String getCurrentActiveHost() {
        final MySQLConnection c = this.currentConnection;
        if (c != null) {
            final Object o = this.connectionsToHostsMap.get(c);
            if (o != null) {
                return o.toString();
            }
        }
        return null;
    }
    
    public synchronized long getCurrentTransactionDuration() {
        if (this.inTransaction && this.transactionStartTime > 0L) {
            return System.nanoTime() - this.transactionStartTime;
        }
        return 0L;
    }
    
    static synchronized LoadBalancedConnection getNullLoadBalancedConnectionInstance() {
        if (LoadBalancedConnectionProxy.nullLBConnectionInstance == null) {
            LoadBalancedConnectionProxy.nullLBConnectionInstance = (LoadBalancedConnection)Proxy.newProxyInstance(LoadBalancedConnection.class.getClassLoader(), LoadBalancedConnectionProxy.INTERFACES_TO_PROXY, new NullLoadBalancedConnectionProxy());
        }
        return LoadBalancedConnectionProxy.nullLBConnectionInstance;
    }
    
    static {
        LoadBalancedConnectionProxy.globalBlacklist = new HashMap<String, Long>();
        Label_0103: {
            if (Util.isJdbc4()) {
                try {
                    LoadBalancedConnectionProxy.JDBC_4_LB_CONNECTION_CTOR = Class.forName("com.alipay.oceanbase.jdbc.JDBC4LoadBalancedMySQLConnection").getConstructor(LoadBalancedConnectionProxy.class);
                    LoadBalancedConnectionProxy.INTERFACES_TO_PROXY = (Class<?>[])new Class[] { LoadBalancedConnection.class, Class.forName("com.alipay.oceanbase.jdbc.JDBC4MySQLConnection") };
                    break Label_0103;
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
            LoadBalancedConnectionProxy.INTERFACES_TO_PROXY = (Class<?>[])new Class[] { LoadBalancedConnection.class };
        }
        LoadBalancedConnectionProxy.nullLBConnectionInstance = null;
    }
    
    private static class NullLoadBalancedConnectionProxy implements InvocationHandler
    {
        public NullLoadBalancedConnectionProxy() {
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            final SQLException exceptionToThrow = SQLError.createSQLException(Messages.getString("LoadBalancedConnectionProxy.unusableConnection"), "25000", 1000001, true, null);
            final Class<?>[] exceptionTypes;
            final Class<?>[] declaredException = exceptionTypes = method.getExceptionTypes();
            for (final Class<?> declEx : exceptionTypes) {
                if (declEx.isAssignableFrom(exceptionToThrow.getClass())) {
                    throw exceptionToThrow;
                }
            }
            throw new IllegalStateException(exceptionToThrow.getMessage(), exceptionToThrow);
        }
    }
}
