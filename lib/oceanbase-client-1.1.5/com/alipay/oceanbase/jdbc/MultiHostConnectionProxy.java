// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.lang.reflect.Constructor;
import java.util.Properties;
import java.util.List;
import java.lang.reflect.InvocationHandler;

public abstract class MultiHostConnectionProxy implements InvocationHandler
{
    private static final String METHOD_GET_MULTI_HOST_SAFE_PROXY = "getMultiHostSafeProxy";
    private static final String METHOD_EQUALS = "equals";
    private static final String METHOD_HASH_CODE = "hashCode";
    private static final String METHOD_CLOSE = "close";
    private static final String METHOD_ABORT_INTERNAL = "abortInternal";
    private static final String METHOD_ABORT = "abort";
    private static final String METHOD_IS_CLOSED = "isClosed";
    private static final String METHOD_GET_AUTO_COMMIT = "getAutoCommit";
    private static final String METHOD_GET_CATALOG = "getCatalog";
    private static final String METHOD_GET_TRANSACTION_ISOLATION = "getTransactionIsolation";
    private static final String METHOD_GET_SESSION_MAX_ROWS = "getSessionMaxRows";
    List<String> hostList;
    Properties localProps;
    boolean autoReconnect;
    MySQLConnection thisAsConnection;
    MySQLConnection proxyConnection;
    MySQLConnection currentConnection;
    boolean isClosed;
    boolean closedExplicitly;
    String closedReason;
    protected Throwable lastExceptionDealtWith;
    private static Constructor<?> JDBC_4_MS_CONNECTION_CTOR;
    
    MultiHostConnectionProxy() throws SQLException {
        this.autoReconnect = false;
        this.thisAsConnection = null;
        this.proxyConnection = null;
        this.currentConnection = null;
        this.isClosed = false;
        this.closedExplicitly = false;
        this.closedReason = null;
        this.lastExceptionDealtWith = null;
        this.thisAsConnection = this.getNewWrapperForThisAsConnection();
    }
    
    MultiHostConnectionProxy(final List<String> hosts, final Properties props) throws SQLException {
        this();
        this.initializeHostsSpecs(hosts, props);
    }
    
    int initializeHostsSpecs(final List<String> hosts, final Properties props) {
        this.autoReconnect = ("true".equalsIgnoreCase(props.getProperty("autoReconnect")) || "true".equalsIgnoreCase(props.getProperty("autoReconnectForPools")));
        this.hostList = hosts;
        final int numHosts = this.hostList.size();
        (this.localProps = (Properties)props.clone()).remove("HOST");
        this.localProps.remove("PORT");
        for (int i = 0; i < numHosts; ++i) {
            this.localProps.remove("HOST." + (i + 1));
            this.localProps.remove("PORT." + (i + 1));
        }
        this.localProps.remove("NUM_HOSTS");
        this.localProps.setProperty("useLocalSessionState", "true");
        return numHosts;
    }
    
    MySQLConnection getNewWrapperForThisAsConnection() throws SQLException {
        if (Util.isJdbc4() || MultiHostConnectionProxy.JDBC_4_MS_CONNECTION_CTOR != null) {
            return (MySQLConnection)Util.handleNewInstance(MultiHostConnectionProxy.JDBC_4_MS_CONNECTION_CTOR, new Object[] { this }, null);
        }
        return new MultiHostMySQLConnection(this);
    }
    
    protected MySQLConnection getProxy() {
        return (this.proxyConnection != null) ? this.proxyConnection : this.thisAsConnection;
    }
    
    protected final void setProxy(final MySQLConnection proxyConn) {
        this.propagateProxyDown(this.proxyConnection = proxyConn);
    }
    
    protected void propagateProxyDown(final MySQLConnection proxyConn) {
        this.currentConnection.setProxy(proxyConn);
    }
    
    Object proxyIfReturnTypeIsJdbcInterface(final Class<?> returnType, final Object toProxy) {
        if (toProxy != null && Util.isJdbcInterface(returnType)) {
            final Class<?> toProxyClass = toProxy.getClass();
            return Proxy.newProxyInstance(toProxyClass.getClassLoader(), Util.getImplementedInterfaces(toProxyClass), this.getNewJdbcInterfaceProxy(toProxy));
        }
        return toProxy;
    }
    
    InvocationHandler getNewJdbcInterfaceProxy(final Object toProxy) {
        return new JdbcInterfaceProxy(toProxy);
    }
    
    void dealWithInvocationException(final InvocationTargetException e) throws SQLException, Throwable, InvocationTargetException {
        final Throwable t = e.getTargetException();
        if (t != null) {
            if (this.lastExceptionDealtWith != t && this.shouldExceptionTriggerConnectionSwitch(t)) {
                this.invalidateCurrentConnection();
                this.pickNewConnection();
                this.lastExceptionDealtWith = t;
            }
            throw t;
        }
        throw e;
    }
    
    abstract boolean shouldExceptionTriggerConnectionSwitch(final Throwable p0);
    
    abstract boolean isMasterConnection();
    
    synchronized void invalidateCurrentConnection() throws SQLException {
        this.invalidateConnection(this.currentConnection);
    }
    
    synchronized void invalidateConnection(final MySQLConnection conn) throws SQLException {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.realClose(true, !conn.getAutoCommit(), true, null);
            }
        }
        catch (SQLException ex) {}
    }
    
    abstract void pickNewConnection() throws SQLException;
    
    synchronized ConnectionImpl createConnectionForHost(final String hostPortSpec) throws SQLException {
        final Properties connProps = (Properties)this.localProps.clone();
        final String[] hostPortPair = NonRegisteringDriver.parseHostPortPair(hostPortSpec);
        final String hostName = hostPortPair[0];
        String portNumber = hostPortPair[1];
        final String dbName = connProps.getProperty("DBNAME");
        if (hostName == null) {
            throw new SQLException("Could not find a hostname to start a connection to");
        }
        if (portNumber == null) {
            portNumber = "3306";
        }
        connProps.setProperty("HOST", hostName);
        connProps.setProperty("PORT", portNumber);
        connProps.setProperty("HOST.1", hostName);
        connProps.setProperty("PORT.1", portNumber);
        connProps.setProperty("NUM_HOSTS", "1");
        connProps.setProperty("roundRobinLoadBalance", "false");
        final ConnectionImpl conn = (ConnectionImpl)ConnectionImpl.getInstance(hostName, Integer.parseInt(portNumber), connProps, dbName, "jdbc:oceanbase://" + hostName + ":" + portNumber + "/");
        conn.setProxy(this.getProxy());
        return conn;
    }
    
    static void syncSessionState(final Connection source, final Connection target) throws SQLException {
        if (source == null || target == null) {
            return;
        }
        syncSessionState(source, target, source.isReadOnly());
    }
    
    static void syncSessionState(final Connection source, final Connection target, final boolean readOnly) throws SQLException {
        if (target != null) {
            target.setReadOnly(readOnly);
        }
        if (source == null || target == null) {
            return;
        }
        target.setAutoCommit(source.getAutoCommit());
        target.setCatalog(source.getCatalog());
        target.setTransactionIsolation(source.getTransactionIsolation());
        target.setSessionMaxRows(source.getSessionMaxRows());
    }
    
    abstract void doClose() throws SQLException;
    
    abstract void doAbortInternal() throws SQLException;
    
    abstract void doAbort(final Executor p0) throws SQLException;
    
    @Override
    public synchronized Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String methodName = method.getName();
        if ("getMultiHostSafeProxy".equals(methodName)) {
            return this.thisAsConnection;
        }
        if ("equals".equals(methodName)) {
            return args[0].equals(this);
        }
        if ("hashCode".equals(methodName)) {
            return this.hashCode();
        }
        if ("close".equals(methodName)) {
            this.doClose();
            this.isClosed = true;
            this.closedReason = "Connection explicitly closed.";
            this.closedExplicitly = true;
            return null;
        }
        if ("abortInternal".equals(methodName)) {
            this.doAbortInternal();
            this.currentConnection.abortInternal();
            this.isClosed = true;
            this.closedReason = "Connection explicitly closed.";
            return null;
        }
        if ("abort".equals(methodName) && args.length == 1) {
            this.doAbort((Executor)args[0]);
            this.isClosed = true;
            this.closedReason = "Connection explicitly closed.";
            return null;
        }
        if ("isClosed".equals(methodName)) {
            return this.isClosed;
        }
        try {
            return this.invokeMore(proxy, method, args);
        }
        catch (InvocationTargetException e) {
            throw (e.getCause() != null) ? e.getCause() : e;
        }
        catch (Exception e2) {
            final Class<?>[] exceptionTypes;
            final Class<?>[] declaredException = exceptionTypes = method.getExceptionTypes();
            for (final Class<?> declEx : exceptionTypes) {
                if (declEx.isAssignableFrom(e2.getClass())) {
                    throw e2;
                }
            }
            throw new IllegalStateException(e2.getMessage(), e2);
        }
    }
    
    abstract Object invokeMore(final Object p0, final Method p1, final Object[] p2) throws Throwable;
    
    protected boolean allowedOnClosedConnection(final Method method) {
        final String methodName = method.getName();
        return methodName.equals("getAutoCommit") || methodName.equals("getCatalog") || methodName.equals("getTransactionIsolation") || methodName.equals("getSessionMaxRows");
    }
    
    static {
        if (Util.isJdbc4()) {
            try {
                MultiHostConnectionProxy.JDBC_4_MS_CONNECTION_CTOR = Class.forName("com.alipay.oceanbase.jdbc.JDBC4MultiHostMySQLConnection").getConstructor(MultiHostConnectionProxy.class);
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
    }
    
    class JdbcInterfaceProxy implements InvocationHandler
    {
        Object invokeOn;
        
        JdbcInterfaceProxy(final Object toInvokeOn) {
            this.invokeOn = null;
            this.invokeOn = toInvokeOn;
        }
        
        @Override
        public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
            synchronized (MultiHostConnectionProxy.this) {
                Object result = null;
                try {
                    result = method.invoke(this.invokeOn, args);
                    result = MultiHostConnectionProxy.this.proxyIfReturnTypeIsJdbcInterface(method.getReturnType(), result);
                }
                catch (InvocationTargetException e) {
                    MultiHostConnectionProxy.this.dealWithInvocationException(e);
                }
                return result;
            }
        }
    }
}
