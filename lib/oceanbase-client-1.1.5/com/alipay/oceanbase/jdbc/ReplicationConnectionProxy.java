// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.Collection;
import java.util.ArrayList;
import java.sql.SQLException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.List;

public class ReplicationConnectionProxy extends MultiHostConnectionProxy implements PingTarget
{
    private ReplicationConnection thisAsReplicationConnection;
    private NonRegisteringDriver driver;
    protected boolean enableJMX;
    protected boolean allowMasterDownConnections;
    protected boolean allowSlaveDownConnections;
    protected boolean readFromMasterWhenNoSlaves;
    protected boolean readFromMasterWhenNoSlavesOriginal;
    protected boolean readOnly;
    ReplicationConnectionGroup connectionGroup;
    private long connectionGroupID;
    private List<String> masterHosts;
    private Properties masterProperties;
    protected LoadBalancedConnection masterConnection;
    private List<String> slaveHosts;
    private Properties slaveProperties;
    protected LoadBalancedConnection slavesConnection;
    private static Constructor<?> JDBC_4_REPL_CONNECTION_CTOR;
    private static Class<?>[] INTERFACES_TO_PROXY;
    
    public static ReplicationConnection createProxyInstance(final List<String> masterHostList, final Properties masterProperties, final List<String> slaveHostList, final Properties slaveProperties) throws SQLException {
        final ReplicationConnectionProxy connProxy = new ReplicationConnectionProxy(masterHostList, masterProperties, slaveHostList, slaveProperties);
        return (ReplicationConnection)Proxy.newProxyInstance(ReplicationConnection.class.getClassLoader(), ReplicationConnectionProxy.INTERFACES_TO_PROXY, connProxy);
    }
    
    private ReplicationConnectionProxy(final List<String> masterHostList, final Properties masterProperties, final List<String> slaveHostList, final Properties slaveProperties) throws SQLException {
        this.enableJMX = false;
        this.allowMasterDownConnections = false;
        this.allowSlaveDownConnections = false;
        this.readFromMasterWhenNoSlaves = false;
        this.readFromMasterWhenNoSlavesOriginal = false;
        this.readOnly = false;
        this.connectionGroupID = -1L;
        this.thisAsReplicationConnection = (ReplicationConnection)this.thisAsConnection;
        final String enableJMXAsString = masterProperties.getProperty("replicationEnableJMX", "false");
        try {
            this.enableJMX = Boolean.parseBoolean(enableJMXAsString);
        }
        catch (Exception e3) {
            throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.badValueForReplicationEnableJMX", new Object[] { enableJMXAsString }), "S1009", null);
        }
        final String allowMasterDownConnectionsAsString = masterProperties.getProperty("allowMasterDownConnections", "false");
        try {
            this.allowMasterDownConnections = Boolean.parseBoolean(allowMasterDownConnectionsAsString);
        }
        catch (Exception e4) {
            throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.badValueForAllowMasterDownConnections", new Object[] { allowMasterDownConnectionsAsString }), "S1009", null);
        }
        final String allowSlaveDownConnectionsAsString = masterProperties.getProperty("allowSlaveDownConnections", "false");
        try {
            this.allowSlaveDownConnections = Boolean.parseBoolean(allowSlaveDownConnectionsAsString);
        }
        catch (Exception e5) {
            throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.badValueForAllowSlaveDownConnections", new Object[] { allowSlaveDownConnectionsAsString }), "S1009", null);
        }
        final String readFromMasterWhenNoSlavesAsString = masterProperties.getProperty("readFromMasterWhenNoSlaves");
        try {
            this.readFromMasterWhenNoSlavesOriginal = Boolean.parseBoolean(readFromMasterWhenNoSlavesAsString);
        }
        catch (Exception e6) {
            throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.badValueForReadFromMasterWhenNoSlaves", new Object[] { readFromMasterWhenNoSlavesAsString }), "S1009", null);
        }
        final String group = masterProperties.getProperty("replicationConnectionGroup", null);
        if (group != null) {
            this.connectionGroup = ReplicationConnectionGroupManager.getConnectionGroupInstance(group);
            if (this.enableJMX) {
                ReplicationConnectionGroupManager.registerJmx();
            }
            this.connectionGroupID = this.connectionGroup.registerReplicationConnection(this.thisAsReplicationConnection, masterHostList, slaveHostList);
            this.slaveHosts = new ArrayList<String>(this.connectionGroup.getSlaveHosts());
            this.masterHosts = new ArrayList<String>(this.connectionGroup.getMasterHosts());
        }
        else {
            this.slaveHosts = new ArrayList<String>(slaveHostList);
            this.masterHosts = new ArrayList<String>(masterHostList);
        }
        this.driver = new NonRegisteringDriver();
        this.slaveProperties = slaveProperties;
        this.masterProperties = masterProperties;
        this.resetReadFromMasterWhenNoSlaves();
        try {
            this.initializeSlavesConnection();
        }
        catch (SQLException e) {
            if (!this.allowSlaveDownConnections) {
                if (this.connectionGroup != null) {
                    this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
                }
                throw e;
            }
        }
        SQLException exCaught = null;
        try {
            this.currentConnection = this.initializeMasterConnection();
        }
        catch (SQLException e2) {
            exCaught = e2;
        }
        if (this.currentConnection == null) {
            if (this.allowMasterDownConnections && this.slavesConnection != null) {
                this.readOnly = true;
                this.currentConnection = this.slavesConnection;
            }
            else {
                if (this.connectionGroup != null) {
                    this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
                }
                if (exCaught != null) {
                    throw exCaught;
                }
                throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.initializationWithEmptyHostsLists"), "S1009", null);
            }
        }
    }
    
    @Override
    MySQLConnection getNewWrapperForThisAsConnection() throws SQLException {
        if (Util.isJdbc4() || ReplicationConnectionProxy.JDBC_4_REPL_CONNECTION_CTOR != null) {
            return (MySQLConnection)Util.handleNewInstance(ReplicationConnectionProxy.JDBC_4_REPL_CONNECTION_CTOR, new Object[] { this }, null);
        }
        return new ReplicationMySQLConnection(this);
    }
    
    @Override
    protected void propagateProxyDown(final MySQLConnection proxyConn) {
        if (this.masterConnection != null) {
            this.masterConnection.setProxy(proxyConn);
        }
        if (this.slavesConnection != null) {
            this.slavesConnection.setProxy(proxyConn);
        }
    }
    
    @Override
    boolean shouldExceptionTriggerConnectionSwitch(final Throwable t) {
        return false;
    }
    
    public boolean isMasterConnection() {
        return this.currentConnection != null && this.currentConnection == this.masterConnection;
    }
    
    public boolean isSlavesConnection() {
        return this.currentConnection != null && this.currentConnection == this.slavesConnection;
    }
    
    @Override
    void pickNewConnection() throws SQLException {
    }
    
    @Override
    void doClose() throws SQLException {
        if (this.masterConnection != null) {
            this.masterConnection.close();
        }
        if (this.slavesConnection != null) {
            this.slavesConnection.close();
        }
        if (this.connectionGroup != null) {
            this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
        }
    }
    
    @Override
    void doAbortInternal() throws SQLException {
        this.masterConnection.abortInternal();
        this.slavesConnection.abortInternal();
        if (this.connectionGroup != null) {
            this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
        }
    }
    
    @Override
    void doAbort(final Executor executor) throws SQLException {
        this.masterConnection.abort(executor);
        this.slavesConnection.abort(executor);
        if (this.connectionGroup != null) {
            this.connectionGroup.handleCloseConnection(this.thisAsReplicationConnection);
        }
    }
    
    @Override
    Object invokeMore(final Object proxy, final Method method, final Object[] args) throws Throwable {
        this.checkConnectionCapabilityForMethod(method);
        boolean invokeAgain = false;
        while (true) {
            try {
                final Object result = method.invoke(this.thisAsConnection, args);
                if (result != null && result instanceof Statement) {
                    ((Statement)result).setPingTarget(this);
                }
                return result;
            }
            catch (InvocationTargetException e) {
                if (invokeAgain) {
                    invokeAgain = false;
                }
                else if (e.getCause() != null && e.getCause() instanceof SQLException && ((SQLException)e.getCause()).getSQLState() == "25000" && ((SQLException)e.getCause()).getErrorCode() == 1000001) {
                    try {
                        this.setReadOnly(this.readOnly);
                        invokeAgain = true;
                    }
                    catch (SQLException ex) {}
                }
                if (!invokeAgain) {
                    throw e;
                }
                continue;
            }
            break;
        }
    }
    
    private void checkConnectionCapabilityForMethod(final Method method) throws Throwable {
        if (this.masterHosts.isEmpty() && this.slaveHosts.isEmpty() && !ReplicationConnection.class.isAssignableFrom(method.getDeclaringClass())) {
            throw SQLError.createSQLException(Messages.getString("ReplicationConnectionProxy.noHostsInconsistentState"), "25000", 1000002, true, null);
        }
    }
    
    @Override
    public void doPing() throws SQLException {
        boolean isMasterConn = this.isMasterConnection();
        SQLException mastersPingException = null;
        SQLException slavesPingException = null;
        if (this.masterConnection != null) {
            try {
                this.masterConnection.ping();
            }
            catch (SQLException e) {
                mastersPingException = e;
            }
        }
        else {
            this.initializeMasterConnection();
        }
        if (this.slavesConnection != null) {
            try {
                this.slavesConnection.ping();
            }
            catch (SQLException e) {
                slavesPingException = e;
            }
        }
        else {
            try {
                this.initializeSlavesConnection();
                if (this.switchToSlavesConnectionIfNecessary()) {
                    isMasterConn = false;
                }
            }
            catch (SQLException e) {
                if (this.masterConnection == null || !this.readFromMasterWhenNoSlaves) {
                    throw e;
                }
            }
        }
        if (isMasterConn && mastersPingException != null) {
            if (this.slavesConnection != null && slavesPingException == null) {
                this.masterConnection = null;
                this.currentConnection = this.slavesConnection;
                this.readOnly = true;
            }
            throw mastersPingException;
        }
        if (!isMasterConn && (slavesPingException != null || this.slavesConnection == null)) {
            if (this.masterConnection != null && this.readFromMasterWhenNoSlaves && mastersPingException == null) {
                this.slavesConnection = null;
                this.currentConnection = this.masterConnection;
                this.readOnly = true;
                this.currentConnection.setReadOnly(true);
            }
            if (slavesPingException != null) {
                throw slavesPingException;
            }
        }
    }
    
    private MySQLConnection initializeMasterConnection() throws SQLException {
        this.masterConnection = null;
        if (this.masterHosts.size() == 0) {
            return null;
        }
        final LoadBalancedConnection newMasterConn = (LoadBalancedConnection)this.driver.connect(this.buildURL(this.masterHosts, this.masterProperties), this.masterProperties);
        newMasterConn.setProxy(this.getProxy());
        return this.masterConnection = newMasterConn;
    }
    
    private MySQLConnection initializeSlavesConnection() throws SQLException {
        this.slavesConnection = null;
        if (this.slaveHosts.size() == 0) {
            return null;
        }
        final LoadBalancedConnection newSlavesConn = (LoadBalancedConnection)this.driver.connect(this.buildURL(this.slaveHosts, this.slaveProperties), this.slaveProperties);
        newSlavesConn.setProxy(this.getProxy());
        newSlavesConn.setReadOnly(true);
        return this.slavesConnection = newSlavesConn;
    }
    
    private String buildURL(final List<String> hosts, final Properties props) {
        final StringBuilder url = new StringBuilder("jdbc:oceanbase:loadbalance://");
        boolean firstHost = true;
        for (final String host : hosts) {
            if (!firstHost) {
                url.append(',');
            }
            url.append(host);
            firstHost = false;
        }
        url.append("/");
        final String masterDb = props.getProperty("DBNAME");
        if (masterDb != null) {
            url.append(masterDb);
        }
        return url.toString();
    }
    
    private synchronized boolean switchToMasterConnection() throws SQLException {
        Label_0039: {
            if (this.masterConnection != null) {
                if (!this.masterConnection.isClosed()) {
                    break Label_0039;
                }
            }
            try {
                if (this.initializeMasterConnection() == null) {
                    return false;
                }
            }
            catch (SQLException e) {
                this.currentConnection = null;
                throw e;
            }
        }
        if (!this.isMasterConnection() && this.masterConnection != null) {
            MultiHostConnectionProxy.syncSessionState(this.currentConnection, this.masterConnection, false);
            this.currentConnection = this.masterConnection;
        }
        return true;
    }
    
    private synchronized boolean switchToSlavesConnection() throws SQLException {
        Label_0039: {
            if (this.slavesConnection != null) {
                if (!this.slavesConnection.isClosed()) {
                    break Label_0039;
                }
            }
            try {
                if (this.initializeSlavesConnection() == null) {
                    return false;
                }
            }
            catch (SQLException e) {
                this.currentConnection = null;
                throw e;
            }
        }
        if (!this.isSlavesConnection() && this.slavesConnection != null) {
            MultiHostConnectionProxy.syncSessionState(this.currentConnection, this.slavesConnection, true);
            this.currentConnection = this.slavesConnection;
        }
        return true;
    }
    
    private boolean switchToSlavesConnectionIfNecessary() throws SQLException {
        return (this.currentConnection == null || (this.isMasterConnection() && (this.readOnly || (this.masterHosts.isEmpty() && this.currentConnection.isClosed()))) || (!this.isMasterConnection() && this.currentConnection.isClosed())) && this.switchToSlavesConnection();
    }
    
    public synchronized Connection getCurrentConnection() {
        return (this.currentConnection == null) ? LoadBalancedConnectionProxy.getNullLoadBalancedConnectionInstance() : this.currentConnection;
    }
    
    public long getConnectionGroupId() {
        return this.connectionGroupID;
    }
    
    public synchronized Connection getMasterConnection() {
        return this.masterConnection;
    }
    
    public synchronized void promoteSlaveToMaster(final String hostPortPair) throws SQLException {
        this.masterHosts.add(hostPortPair);
        this.removeSlave(hostPortPair);
        if (this.masterConnection != null) {
            this.masterConnection.addHost(hostPortPair);
        }
        if (!this.readOnly && !this.isMasterConnection()) {
            this.switchToMasterConnection();
        }
    }
    
    public synchronized void removeMasterHost(final String hostPortPair) throws SQLException {
        this.removeMasterHost(hostPortPair, true);
    }
    
    public synchronized void removeMasterHost(final String hostPortPair, final boolean waitUntilNotInUse) throws SQLException {
        this.removeMasterHost(hostPortPair, waitUntilNotInUse, false);
    }
    
    public synchronized void removeMasterHost(final String hostPortPair, final boolean waitUntilNotInUse, final boolean isNowSlave) throws SQLException {
        if (isNowSlave) {
            this.slaveHosts.add(hostPortPair);
            this.resetReadFromMasterWhenNoSlaves();
        }
        this.masterHosts.remove(hostPortPair);
        if (this.masterConnection == null || this.masterConnection.isClosed()) {
            this.masterConnection = null;
            return;
        }
        if (waitUntilNotInUse) {
            this.masterConnection.removeHostWhenNotInUse(hostPortPair);
        }
        else {
            this.masterConnection.removeHost(hostPortPair);
        }
        if (this.masterHosts.isEmpty()) {
            this.masterConnection.close();
            this.masterConnection = null;
            this.switchToSlavesConnectionIfNecessary();
        }
    }
    
    public boolean isHostMaster(final String hostPortPair) {
        if (hostPortPair == null) {
            return false;
        }
        for (final String masterHost : this.masterHosts) {
            if (masterHost.equalsIgnoreCase(hostPortPair)) {
                return true;
            }
        }
        return false;
    }
    
    public synchronized Connection getSlavesConnection() {
        return this.slavesConnection;
    }
    
    public synchronized void addSlaveHost(final String hostPortPair) throws SQLException {
        if (this.isHostSlave(hostPortPair)) {
            return;
        }
        this.slaveHosts.add(hostPortPair);
        this.resetReadFromMasterWhenNoSlaves();
        if (this.slavesConnection == null) {
            this.initializeSlavesConnection();
            this.switchToSlavesConnectionIfNecessary();
        }
        else {
            this.slavesConnection.addHost(hostPortPair);
        }
    }
    
    public synchronized void removeSlave(final String hostPortPair) throws SQLException {
        this.removeSlave(hostPortPair, true);
    }
    
    public synchronized void removeSlave(final String hostPortPair, final boolean closeGently) throws SQLException {
        this.slaveHosts.remove(hostPortPair);
        this.resetReadFromMasterWhenNoSlaves();
        if (this.slavesConnection == null || this.slavesConnection.isClosed()) {
            this.slavesConnection = null;
            return;
        }
        if (closeGently) {
            this.slavesConnection.removeHostWhenNotInUse(hostPortPair);
        }
        else {
            this.slavesConnection.removeHost(hostPortPair);
        }
        if (this.slaveHosts.isEmpty()) {
            this.slavesConnection.close();
            this.slavesConnection = null;
            this.switchToMasterConnection();
            if (this.isMasterConnection()) {
                this.currentConnection.setReadOnly(this.readOnly);
            }
        }
    }
    
    public boolean isHostSlave(final String hostPortPair) {
        if (hostPortPair == null) {
            return false;
        }
        for (final String test : this.slaveHosts) {
            if (test.equalsIgnoreCase(hostPortPair)) {
                return true;
            }
        }
        return false;
    }
    
    public synchronized void setReadOnly(final boolean readOnly) throws SQLException {
        if (readOnly) {
            if (!this.isSlavesConnection() || this.currentConnection.isClosed()) {
                boolean switched = true;
                SQLException exceptionCaught = null;
                try {
                    switched = this.switchToSlavesConnection();
                }
                catch (SQLException e) {
                    switched = false;
                    exceptionCaught = e;
                }
                if (!switched && this.readFromMasterWhenNoSlaves && this.switchToMasterConnection()) {
                    exceptionCaught = null;
                }
                if (exceptionCaught != null) {
                    throw exceptionCaught;
                }
            }
        }
        else if (!this.isMasterConnection() || this.currentConnection.isClosed()) {
            boolean switched = true;
            SQLException exceptionCaught = null;
            try {
                switched = this.switchToMasterConnection();
            }
            catch (SQLException e) {
                switched = false;
                exceptionCaught = e;
            }
            if (!switched && this.switchToSlavesConnectionIfNecessary()) {
                exceptionCaught = null;
            }
            if (exceptionCaught != null) {
                throw exceptionCaught;
            }
        }
        this.readOnly = readOnly;
        if (this.readFromMasterWhenNoSlaves && this.isMasterConnection()) {
            this.currentConnection.setReadOnly(this.readOnly);
        }
    }
    
    public boolean isReadOnly() throws SQLException {
        return !this.isMasterConnection() || this.readOnly;
    }
    
    private void resetReadFromMasterWhenNoSlaves() {
        this.readFromMasterWhenNoSlaves = (this.slaveHosts.isEmpty() || this.readFromMasterWhenNoSlavesOriginal);
    }
    
    static {
        if (Util.isJdbc4()) {
            try {
                ReplicationConnectionProxy.JDBC_4_REPL_CONNECTION_CTOR = Class.forName("com.alipay.oceanbase.jdbc.JDBC4ReplicationMySQLConnection").getConstructor(ReplicationConnectionProxy.class);
                ReplicationConnectionProxy.INTERFACES_TO_PROXY = (Class<?>[])new Class[] { ReplicationConnection.class, Class.forName("com.alipay.oceanbase.jdbc.JDBC4MySQLConnection") };
                return;
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
        ReplicationConnectionProxy.INTERFACES_TO_PROXY = (Class<?>[])new Class[] { ReplicationConnection.class };
    }
}
