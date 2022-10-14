// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.pool;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;
import java.text.NumberFormat;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import java.util.concurrent.Executor;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.OceanBaseConnection;
import com.oceanbase.jdbc.internal.util.Utils;
import java.util.Iterator;
import java.sql.SQLException;
import java.util.concurrent.ThreadFactory;
import com.oceanbase.jdbc.internal.util.scheduler.OceanBaseThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import com.oceanbase.jdbc.OceanBasePooledConnection;
import java.util.concurrent.LinkedBlockingDeque;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.UrlParser;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.logging.Logger;

public class Pool implements AutoCloseable, PoolMBean
{
    private static final Logger logger;
    private static final int POOL_STATE_OK = 0;
    private static final int POOL_STATE_CLOSING = 1;
    private final AtomicInteger poolState;
    private final UrlParser urlParser;
    private final Options options;
    private final AtomicInteger pendingRequestNumber;
    private final AtomicInteger totalConnection;
    private final LinkedBlockingDeque<OceanBasePooledConnection> idleConnections;
    private final ThreadPoolExecutor connectionAppender;
    private final BlockingQueue<Runnable> connectionAppenderQueue;
    private final String poolTag;
    private final ScheduledThreadPoolExecutor poolExecutor;
    private final ScheduledFuture scheduledFuture;
    private GlobalStateInfo globalInfo;
    private int maxIdleTime;
    private long timeToConnectNanos;
    private long connectionTime;
    
    public Pool(final UrlParser urlParser, final int poolIndex, final ScheduledThreadPoolExecutor poolExecutor) {
        this.poolState = new AtomicInteger();
        this.pendingRequestNumber = new AtomicInteger();
        this.totalConnection = new AtomicInteger();
        this.connectionTime = 0L;
        this.urlParser = urlParser;
        this.options = urlParser.getOptions();
        this.maxIdleTime = this.options.maxIdleTime;
        this.poolTag = this.generatePoolTag(poolIndex);
        this.connectionAppenderQueue = new ArrayBlockingQueue<Runnable>(this.options.maxPoolSize);
        (this.connectionAppender = new ThreadPoolExecutor(1, 1, 10L, TimeUnit.SECONDS, this.connectionAppenderQueue, new OceanBaseThreadFactory(this.poolTag + "-appender"))).allowCoreThreadTimeOut(true);
        this.connectionAppender.prestartCoreThread();
        this.idleConnections = new LinkedBlockingDeque<OceanBasePooledConnection>();
        final int scheduleDelay = Math.min(30, this.maxIdleTime / 2);
        this.poolExecutor = poolExecutor;
        this.scheduledFuture = poolExecutor.scheduleAtFixedRate(this::removeIdleTimeoutConnection, scheduleDelay, scheduleDelay, TimeUnit.SECONDS);
        if (this.options.registerJmxPool) {
            try {
                this.registerJmx();
            }
            catch (Exception ex) {
                Pool.logger.error("pool " + this.poolTag + " not registered due to exception : " + ex.getMessage());
            }
        }
        try {
            for (int i = 0; i < this.options.minPoolSize; ++i) {
                this.addConnection();
            }
        }
        catch (SQLException sqle) {
            Pool.logger.error("error initializing pool connection", sqle);
        }
    }
    
    private void addConnectionRequest() {
        if (this.totalConnection.get() < this.options.maxPoolSize && this.poolState.get() == 0) {
            this.connectionAppender.prestartCoreThread();
            this.connectionAppenderQueue.offer(() -> {
                if ((this.totalConnection.get() < this.options.minPoolSize || this.pendingRequestNumber.get() > 0) && this.totalConnection.get() < this.options.maxPoolSize) {
                    try {
                        this.addConnection();
                    }
                    catch (SQLException ex) {}
                }
            });
        }
    }
    
    private void removeIdleTimeoutConnection() {
        final Iterator<OceanBasePooledConnection> iterator = this.idleConnections.descendingIterator();
        while (iterator.hasNext()) {
            final OceanBasePooledConnection item = iterator.next();
            final long idleTime = System.nanoTime() - item.getLastUsed().get();
            final boolean timedOut = idleTime > TimeUnit.SECONDS.toNanos(this.maxIdleTime);
            boolean shouldBeReleased = false;
            if (this.globalInfo != null) {
                if (idleTime > TimeUnit.SECONDS.toNanos(this.globalInfo.getWaitTimeout() - 45)) {
                    shouldBeReleased = true;
                }
                if (timedOut && this.totalConnection.get() > this.options.minPoolSize) {
                    shouldBeReleased = true;
                }
            }
            else if (timedOut) {
                shouldBeReleased = true;
            }
            if (shouldBeReleased && this.idleConnections.remove(item)) {
                this.totalConnection.decrementAndGet();
                this.silentCloseConnection(item);
                this.addConnectionRequest();
                if (!Pool.logger.isDebugEnabled()) {
                    continue;
                }
                Pool.logger.debug("pool {} connection removed due to inactivity (total:{}, active:{}, pending:{})", this.poolTag, this.totalConnection.get(), this.getActiveConnections(), this.pendingRequestNumber.get());
            }
        }
    }
    
    private void addConnection() throws SQLException {
        final Protocol protocol = Utils.retrieveProxy(this.urlParser, this.globalInfo);
        final OceanBaseConnection connection = new OceanBaseConnection(protocol);
        final OceanBasePooledConnection pooledConnection = this.createPoolConnection(connection);
        if (this.options.staticGlobal) {
            if (this.globalInfo == null) {
                this.initializePoolGlobalState(connection);
            }
            connection.setDefaultTransactionIsolation(this.globalInfo.getDefaultTransactionIsolation());
        }
        else {
            connection.setDefaultTransactionIsolation(connection.getTransactionIsolation());
        }
        if (this.poolState.get() == 0 && this.totalConnection.incrementAndGet() <= this.options.maxPoolSize) {
            this.idleConnections.addFirst(pooledConnection);
            if (Pool.logger.isDebugEnabled()) {
                Pool.logger.debug("pool {} new physical connection created (total:{}, active:{}, pending:{})", this.poolTag, this.totalConnection.get(), this.getActiveConnections(), this.pendingRequestNumber.get());
            }
            return;
        }
        this.silentCloseConnection(pooledConnection);
    }
    
    private OceanBasePooledConnection getIdleConnection() throws InterruptedException {
        return this.getIdleConnection(0L, TimeUnit.NANOSECONDS);
    }
    
    private OceanBasePooledConnection getIdleConnection(final long timeout, final TimeUnit timeUnit) throws InterruptedException {
        while (true) {
            final OceanBasePooledConnection item = (timeout == 0L) ? this.idleConnections.pollFirst() : this.idleConnections.pollFirst(timeout, timeUnit);
            if (item == null) {
                break;
            }
            final OceanBaseConnection connection = item.getConnection();
            try {
                if (TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - item.getLastUsed().get()) <= this.options.poolValidMinDelay) {
                    item.lastUsedToNow();
                    return item;
                }
                if (connection.isValid(10)) {
                    item.lastUsedToNow();
                    return item;
                }
            }
            catch (SQLException ex) {}
            this.totalConnection.decrementAndGet();
            this.silentAbortConnection(item);
            this.addConnectionRequest();
            if (!Pool.logger.isDebugEnabled()) {
                continue;
            }
            Pool.logger.debug("pool {} connection removed from pool due to failed validation (total:{}, active:{}, pending:{})", this.poolTag, this.totalConnection.get(), this.getActiveConnections(), this.pendingRequestNumber.get());
        }
        return null;
    }
    
    private void silentCloseConnection(final OceanBasePooledConnection item) {
        try {
            item.close();
        }
        catch (SQLException ex) {}
    }
    
    private void silentAbortConnection(final OceanBasePooledConnection item) {
        try {
            item.abort(this.poolExecutor);
        }
        catch (SQLException ex) {}
    }
    
    private OceanBasePooledConnection createPoolConnection(final OceanBaseConnection connection) {
        final OceanBasePooledConnection pooledConnection = new OceanBasePooledConnection(connection);
        pooledConnection.addConnectionEventListener(new ConnectionEventListener() {
            @Override
            public void connectionClosed(final ConnectionEvent event) {
                final OceanBasePooledConnection item = (OceanBasePooledConnection)event.getSource();
                if (Pool.this.poolState.get() == 0) {
                    try {
                        if (!Pool.this.idleConnections.contains(item)) {
                            item.getConnection().reset();
                            Pool.this.idleConnections.addFirst(item);
                        }
                    }
                    catch (SQLException sqle) {
                        Pool.this.totalConnection.decrementAndGet();
                        Pool.this.silentCloseConnection(item);
                        Pool.logger.debug("connection removed from pool {} due to error during reset", Pool.this.poolTag);
                    }
                }
                else {
                    try {
                        item.close();
                    }
                    catch (SQLException ex) {}
                    Pool.this.totalConnection.decrementAndGet();
                }
            }
            
            @Override
            public void connectionErrorOccurred(final ConnectionEvent event) {
                final OceanBasePooledConnection item = (OceanBasePooledConnection)event.getSource();
                if (Pool.this.idleConnections.remove(item)) {
                    Pool.this.totalConnection.decrementAndGet();
                }
                Pool.this.silentCloseConnection(item);
                Pool.this.addConnectionRequest();
                Pool.logger.debug("connection {} removed from pool {} due to having throw a Connection exception (total:{}, active:{}, pending:{})", item.getConnection().getServerThreadId(), Pool.this.poolTag, Pool.this.totalConnection.get(), Pool.this.getActiveConnections(), Pool.this.pendingRequestNumber.get());
            }
        });
        return pooledConnection;
    }
    
    public OceanBaseConnection getConnection() throws SQLException {
        this.pendingRequestNumber.incrementAndGet();
        try {
            OceanBasePooledConnection pooledConnection;
            if ((pooledConnection = this.getIdleConnection((this.totalConnection.get() > 4) ? 0L : 50L, TimeUnit.MICROSECONDS)) != null) {
                return pooledConnection.getConnection();
            }
            this.addConnectionRequest();
            if ((pooledConnection = this.getIdleConnection(TimeUnit.MILLISECONDS.toNanos(this.options.connectTimeout), TimeUnit.NANOSECONDS)) != null) {
                return pooledConnection.getConnection();
            }
            throw ExceptionFactory.INSTANCE.create(String.format("No connection available within the specified time (option 'connectTimeout': %s ms)", NumberFormat.getInstance().format(this.options.connectTimeout)));
        }
        catch (InterruptedException interrupted) {
            throw ExceptionFactory.INSTANCE.create("Thread was interrupted", "70100", interrupted);
        }
        finally {
            this.pendingRequestNumber.decrementAndGet();
        }
    }
    
    public OceanBaseConnection getConnection(final String username, final String password) throws SQLException {
        try {
            Label_0067: {
                if (this.urlParser.getUsername() != null) {
                    if (!this.urlParser.getUsername().equals(username)) {
                        break Label_0067;
                    }
                }
                else if (username != null) {
                    break Label_0067;
                }
                if (this.urlParser.getPassword() != null) {
                    if (!this.urlParser.getPassword().equals(password)) {
                        break Label_0067;
                    }
                }
                else if (password != null) {
                    break Label_0067;
                }
                return this.getConnection();
            }
            final UrlParser tmpUrlParser = (UrlParser)this.urlParser.clone();
            tmpUrlParser.setUsername(username);
            tmpUrlParser.setPassword(password);
            final Protocol protocol = Utils.retrieveProxy(tmpUrlParser, this.globalInfo);
            return new OceanBaseConnection(protocol);
        }
        catch (CloneNotSupportedException cloneException) {
            throw new SQLException("Error getting connection, parameters cannot be cloned", cloneException);
        }
    }
    
    private String generatePoolTag(final int poolIndex) {
        if (this.options.poolName == null) {
            this.options.poolName = "MariaDB-pool";
        }
        return this.options.poolName + "-" + poolIndex;
    }
    
    public UrlParser getUrlParser() {
        return this.urlParser;
    }
    
    @Override
    public void close() throws InterruptedException {
        synchronized (this) {
            Pools.remove(this);
            this.poolState.set(1);
            this.pendingRequestNumber.set(0);
            this.scheduledFuture.cancel(false);
            this.connectionAppender.shutdown();
            try {
                this.connectionAppender.awaitTermination(10L, TimeUnit.SECONDS);
            }
            catch (InterruptedException ex) {}
            if (Pool.logger.isInfoEnabled()) {
                Pool.logger.info("closing pool {} (total:{}, active:{}, pending:{})", this.poolTag, this.totalConnection.get(), this.getActiveConnections(), this.pendingRequestNumber.get());
            }
            final ExecutorService connectionRemover = new ThreadPoolExecutor(this.totalConnection.get(), this.options.maxPoolSize, 10L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(this.options.maxPoolSize), new OceanBaseThreadFactory(this.poolTag + "-destroyer"));
            final long start = System.nanoTime();
            do {
                this.closeAll(connectionRemover, this.idleConnections);
                if (this.totalConnection.get() > 0) {
                    Thread.sleep(0L, 1000);
                }
            } while (this.totalConnection.get() > 0 && TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start) < 10L);
            if (this.totalConnection.get() > 0 || this.idleConnections.isEmpty()) {
                this.closeAll(connectionRemover, this.idleConnections);
            }
            connectionRemover.shutdown();
            try {
                this.unRegisterJmx();
            }
            catch (Exception ex2) {}
            connectionRemover.awaitTermination(10L, TimeUnit.SECONDS);
        }
    }
    
    private void closeAll(final ExecutorService connectionRemover, final Collection<OceanBasePooledConnection> collection) {
        synchronized (collection) {
            for (final OceanBasePooledConnection item : collection) {
                collection.remove(item);
                this.totalConnection.decrementAndGet();
                try {
                    item.abort(connectionRemover);
                }
                catch (SQLException ex) {}
            }
        }
    }
    
    private void initializePoolGlobalState(final OceanBaseConnection connection) throws SQLException {
        try (final Statement stmt = connection.createStatement()) {
            String sql = "SELECT @@max_allowed_packet,@@wait_timeout,@@autocommit,@@auto_increment_increment,@@time_zone,@@system_time_zone,@@tx_isolation";
            if (!connection.isServerMariaDb()) {
                final int major = connection.getMetaData().getDatabaseMajorVersion();
                if ((major >= 8 && connection.versionGreaterOrEqual(8, 0, 3)) || (major < 8 && connection.versionGreaterOrEqual(5, 7, 20))) {
                    sql = "SELECT @@max_allowed_packet,@@wait_timeout,@@autocommit,@@auto_increment_increment,@@time_zone,@@system_time_zone,@@transaction_isolation";
                }
            }
            try (final ResultSet rs = stmt.executeQuery(sql)) {
                rs.next();
                final int transactionIsolation = Utils.transactionFromString(rs.getString(7));
                this.globalInfo = new GlobalStateInfo(rs.getLong(1), rs.getInt(2), rs.getBoolean(3), rs.getInt(4), rs.getString(5), rs.getString(6), transactionIsolation);
                this.maxIdleTime = Math.min(this.options.maxIdleTime, this.globalInfo.getWaitTimeout() - 45);
            }
        }
    }
    
    public String getPoolTag() {
        return this.poolTag;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final Pool pool = (Pool)obj;
        return this.poolTag.equals(pool.poolTag);
    }
    
    @Override
    public int hashCode() {
        return this.poolTag.hashCode();
    }
    
    public GlobalStateInfo getGlobalInfo() {
        return this.globalInfo;
    }
    
    @Override
    public long getActiveConnections() {
        return this.totalConnection.get() - this.idleConnections.size();
    }
    
    @Override
    public long getTotalConnections() {
        return this.totalConnection.get();
    }
    
    @Override
    public long getIdleConnections() {
        return this.idleConnections.size();
    }
    
    @Override
    public long getConnectionRequests() {
        return this.pendingRequestNumber.get();
    }
    
    private void registerJmx() throws Exception {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final String jmxName = this.poolTag.replace(":", "_");
        final ObjectName name = new ObjectName("com.oceanbase.jdbc.pool:type=" + jmxName);
        if (!mbs.isRegistered(name)) {
            mbs.registerMBean(this, name);
        }
    }
    
    private void unRegisterJmx() throws Exception {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        final String jmxName = this.poolTag.replace(":", "_");
        final ObjectName name = new ObjectName("com.oceanbase.jdbc.pool:type=" + jmxName);
        if (mbs.isRegistered(name)) {
            mbs.unregisterMBean(name);
        }
    }
    
    public List<Long> testGetConnectionIdleThreadIds() {
        final List<Long> threadIds = new ArrayList<Long>();
        for (final OceanBasePooledConnection pooledConnection : this.idleConnections) {
            threadIds.add(pooledConnection.getConnection().getServerThreadId());
        }
        return threadIds;
    }
    
    @Override
    public void resetStaticGlobal() {
        this.globalInfo = null;
    }
    
    static {
        logger = LoggerFactory.getLogger(Pool.class);
    }
}
