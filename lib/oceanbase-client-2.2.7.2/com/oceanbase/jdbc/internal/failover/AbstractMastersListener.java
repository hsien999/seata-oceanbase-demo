// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.failover;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import java.util.concurrent.ConcurrentHashMap;
import java.net.SocketException;
import com.oceanbase.jdbc.OceanBaseStatement;
import com.oceanbase.jdbc.OceanBaseConnection;
import java.sql.SQLNonTransientConnectionException;
import com.oceanbase.jdbc.internal.util.dao.ClientPrepareResult;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Iterator;
import java.util.Map;
import java.lang.reflect.Method;
import java.util.Set;
import com.oceanbase.jdbc.internal.util.SqlStates;
import com.oceanbase.jdbc.internal.failover.tools.SearchFilter;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.UrlParser;
import com.oceanbase.jdbc.internal.logging.Logger;
import com.oceanbase.jdbc.internal.failover.thread.ConnectionValidator;
import com.oceanbase.jdbc.HostAddress;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractMastersListener implements Listener
{
    private static final ConcurrentMap<HostAddress, Long> blacklist;
    private static final ConnectionValidator connectionValidationLoop;
    private static final Logger logger;
    public final UrlParser urlParser;
    protected final AtomicInteger currentConnectionAttempts;
    protected final AtomicBoolean explicitClosed;
    protected final GlobalStateInfo globalInfo;
    private final AtomicBoolean masterHostFail;
    protected volatile boolean currentReadOnlyAsked;
    protected Protocol currentProtocol;
    protected FailoverProxy proxy;
    protected long lastRetry;
    protected long lastQueryNanos;
    private volatile long masterHostFailNanos;
    
    protected AbstractMastersListener(final UrlParser urlParser, final GlobalStateInfo globalInfo) {
        this.currentConnectionAttempts = new AtomicInteger();
        this.explicitClosed = new AtomicBoolean(false);
        this.masterHostFail = new AtomicBoolean();
        this.currentReadOnlyAsked = false;
        this.currentProtocol = null;
        this.lastRetry = 0L;
        this.lastQueryNanos = 0L;
        this.masterHostFailNanos = 0L;
        this.urlParser = urlParser;
        this.globalInfo = globalInfo;
        this.masterHostFail.set(true);
        this.lastQueryNanos = System.nanoTime();
    }
    
    public static void clearBlacklist() {
        AbstractMastersListener.blacklist.clear();
    }
    
    @Override
    public void initializeConnection() throws SQLException {
        final long connectionTimeoutMillis = TimeUnit.SECONDS.toMillis(this.urlParser.getOptions().validConnectionTimeout);
        this.lastQueryNanos = System.nanoTime();
        if (connectionTimeoutMillis > 0L) {
            AbstractMastersListener.connectionValidationLoop.addListener(this, connectionTimeoutMillis);
        }
    }
    
    protected void removeListenerFromSchedulers() {
        AbstractMastersListener.connectionValidationLoop.removeListener(this);
    }
    
    protected void preAutoReconnect() throws SQLException {
        if (!this.isExplicitClosed()) {
            try {
                final boolean currentReadOnlyAsked = this.currentReadOnlyAsked;
                this.reconnectFailedConnection(new SearchFilter(!currentReadOnlyAsked, currentReadOnlyAsked));
            }
            catch (SQLException ex) {}
            this.handleFailLoop();
            return;
        }
        throw new SQLException("Connection is closed", SqlStates.CONNECTION_EXCEPTION.getSqlState());
    }
    
    @Override
    public FailoverProxy getProxy() {
        return this.proxy;
    }
    
    @Override
    public void setProxy(final FailoverProxy proxy) {
        this.proxy = proxy;
    }
    
    @Override
    public Set<HostAddress> getBlacklistKeys() {
        return AbstractMastersListener.blacklist.keySet();
    }
    
    @Override
    public HandleErrorResult handleFailover(final SQLException qe, final Method method, final Object[] args, final Protocol protocol, final boolean isClosed) throws SQLException {
        if (this.isExplicitClosed()) {
            throw new SQLException("Connection has been closed !");
        }
        if (this.setMasterHostFail()) {
            AbstractMastersListener.logger.warn("SQL Primary node [{}, conn={}, local_port={}, timeout={}] connection fail. Reason : {}", this.currentProtocol.getHostAddress().toString(), this.currentProtocol.getServerThreadId(), this.currentProtocol.getSocket().getLocalPort(), this.currentProtocol.getTimeout(), qe.getMessage());
            this.addToBlacklist(this.currentProtocol.getHostAddress());
        }
        final boolean killCmd = qe != null && qe.getSQLState() != null && qe.getSQLState().equals("70100") && 1927 == qe.getErrorCode();
        return this.primaryFail(method, args, killCmd, isClosed);
    }
    
    @Override
    public void addToBlacklist(final HostAddress hostAddress) {
        if (hostAddress != null && !this.isExplicitClosed()) {
            AbstractMastersListener.blacklist.putIfAbsent(hostAddress, System.nanoTime());
        }
    }
    
    @Override
    public void removeFromBlacklist(final HostAddress hostAddress) {
        if (hostAddress != null) {
            AbstractMastersListener.blacklist.remove(hostAddress);
        }
    }
    
    public void resetOldsBlackListHosts() {
        final long currentTimeNanos = System.nanoTime();
        final Set<Map.Entry<HostAddress, Long>> entries = AbstractMastersListener.blacklist.entrySet();
        for (final Map.Entry<HostAddress, Long> blEntry : entries) {
            final long entryNanos = blEntry.getValue();
            final long durationSeconds = TimeUnit.NANOSECONDS.toSeconds(currentTimeNanos - entryNanos);
            if (durationSeconds >= this.urlParser.getOptions().loadBalanceBlacklistTimeout) {
                AbstractMastersListener.blacklist.remove(blEntry.getKey(), entryNanos);
            }
        }
    }
    
    protected void resetMasterFailoverData() {
        if (this.masterHostFail.compareAndSet(true, false)) {
            this.masterHostFailNanos = 0L;
        }
    }
    
    protected void setSessionReadOnly(final boolean readOnly, final Protocol protocol) throws SQLException {
        if (protocol.versionGreaterOrEqual(5, 6, 5)) {
            AbstractMastersListener.logger.info("SQL node [{}, conn={}] is now in {} mode.", protocol.getHostAddress().toString(), protocol.getServerThreadId(), readOnly ? "read-only" : "write");
            protocol.executeQuery("SET SESSION TRANSACTION " + (readOnly ? "READ ONLY" : "READ WRITE"));
        }
    }
    
    public abstract void handleFailLoop();
    
    @Override
    public Protocol getCurrentProtocol() {
        return this.currentProtocol;
    }
    
    public long getMasterHostFailNanos() {
        return this.masterHostFailNanos;
    }
    
    @Override
    public boolean setMasterHostFail() {
        if (this.masterHostFail.compareAndSet(false, true)) {
            this.masterHostFailNanos = System.nanoTime();
            this.currentConnectionAttempts.set(0);
            return true;
        }
        return false;
    }
    
    @Override
    public boolean isMasterHostFail() {
        return this.masterHostFail.get();
    }
    
    @Override
    public boolean hasHostFail() {
        return this.masterHostFail.get();
    }
    
    @Override
    public SearchFilter getFilterForFailedHost() {
        return new SearchFilter(this.isMasterHostFail(), false);
    }
    
    public HandleErrorResult relaunchOperation(final Method method, final Object[] args) throws SQLException {
        final HandleErrorResult handleErrorResult = new HandleErrorResult(true);
        if (method != null) {
            final String name = method.getName();
            switch (name) {
                case "executeQuery": {
                    if (args[2] instanceof String) {
                        final String query = ((String)args[2]).toUpperCase(Locale.ROOT);
                        if (!"ALTER SYSTEM CRASH".equals(query) && !query.startsWith("KILL")) {
                            AbstractMastersListener.logger.debug("relaunch query to new connection {}", (this.currentProtocol != null) ? ("(conn=" + this.currentProtocol.getServerThreadId() + ")") : "");
                            try {
                                handleErrorResult.resultObject = method.invoke(this.currentProtocol, args);
                                handleErrorResult.mustThrowError = false;
                            }
                            catch (IllegalAccessException | InvocationTargetException ex3) {
                                final ReflectiveOperationException ex;
                                final ReflectiveOperationException e = ex;
                                throw new SQLException(e.getCause());
                            }
                        }
                        break;
                    }
                    break;
                }
                case "executePreparedQuery": {
                    try {
                        final boolean mustBeOnMaster = (boolean)args[0];
                        final ServerPrepareResult oldServerPrepareResult = (ServerPrepareResult)args[1];
                        final ServerPrepareResult serverPrepareResult = this.currentProtocol.prepare(oldServerPrepareResult.getSql(), mustBeOnMaster);
                        oldServerPrepareResult.failover(serverPrepareResult.getStatementId(), this.currentProtocol);
                        AbstractMastersListener.logger.debug("relaunch query to new connection " + ((this.currentProtocol != null) ? ("server thread id " + this.currentProtocol.getServerThreadId()) : ""));
                        handleErrorResult.resultObject = method.invoke(this.currentProtocol, args);
                        handleErrorResult.mustThrowError = false;
                    }
                    catch (Exception ex4) {}
                    break;
                }
                default: {
                    try {
                        handleErrorResult.resultObject = method.invoke(this.currentProtocol, args);
                        handleErrorResult.mustThrowError = false;
                    }
                    catch (IllegalAccessException | InvocationTargetException ex5) {
                        final ReflectiveOperationException ex2;
                        final ReflectiveOperationException e2 = ex2;
                        throw new SQLException(e2);
                    }
                    break;
                }
            }
        }
        return handleErrorResult;
    }
    
    public boolean isQueryRelaunchable(final Method method, final Object[] args) {
        if (method != null) {
            final String name = method.getName();
            switch (name) {
                case "executeQuery": {
                    if (!(boolean)args[0]) {
                        return true;
                    }
                    if (args[2] instanceof String) {
                        return ((String)args[2]).toUpperCase(Locale.ROOT).startsWith("SELECT");
                    }
                    if (args[2] instanceof ClientPrepareResult) {
                        final String query = new String(((ClientPrepareResult)args[2]).getQueryParts().get(0)).toUpperCase(Locale.ROOT);
                        return query.startsWith("SELECT");
                    }
                    break;
                }
                case "executePreparedQuery": {
                    if (!(boolean)args[0]) {
                        return true;
                    }
                    final ServerPrepareResult serverPrepareResult = (ServerPrepareResult)args[1];
                    return serverPrepareResult.getSql().toUpperCase(Locale.ROOT).startsWith("SELECT");
                }
                case "executeBatchStmt":
                case "executeBatchClient":
                case "executeBatchServer": {
                    return !(boolean)args[0];
                }
                default: {
                    return false;
                }
            }
        }
        return false;
    }
    
    @Override
    public Object invoke(final Method method, final Object[] args, final Protocol specificProtocol) throws Throwable {
        return method.invoke(specificProtocol, args);
    }
    
    @Override
    public Object invoke(final Method method, final Object[] args) throws Throwable {
        return method.invoke(this.currentProtocol, args);
    }
    
    @Override
    public void syncConnection(final Protocol from, final Protocol to) throws SQLException {
        if (from != null) {
            this.proxy.lock.lock();
            try {
                to.resetStateAfterFailover(from.getMaxRows(), from.getTransactionIsolationLevel(), from.getDatabase(), from.getAutocommit());
            }
            finally {
                this.proxy.lock.unlock();
            }
        }
    }
    
    @Override
    public boolean versionGreaterOrEqual(final int major, final int minor, final int patch) {
        return this.currentProtocol.versionGreaterOrEqual(major, minor, patch);
    }
    
    @Override
    public boolean isServerMariaDb() {
        return this.currentProtocol.isServerMariaDb();
    }
    
    @Override
    public boolean sessionStateAware() {
        return this.currentProtocol.sessionStateAware();
    }
    
    @Override
    public boolean noBackslashEscapes() {
        return this.currentProtocol.noBackslashEscapes();
    }
    
    @Override
    public int getMajorServerVersion() {
        return this.currentProtocol.getMajorServerVersion();
    }
    
    @Override
    public boolean isClosed() {
        return this.currentProtocol.isClosed();
    }
    
    @Override
    public boolean isValid(final int timeout) throws SQLException {
        return this.currentProtocol.isValid(timeout);
    }
    
    @Override
    public boolean isReadOnly() {
        return this.currentReadOnlyAsked;
    }
    
    @Override
    public boolean inTransaction() {
        return this.currentProtocol.inTransaction();
    }
    
    @Override
    public boolean isMasterConnection() {
        return true;
    }
    
    @Override
    public boolean isExplicitClosed() {
        return this.explicitClosed.get();
    }
    
    @Override
    public int getRetriesAllDown() {
        return this.urlParser.getOptions().retriesAllDown;
    }
    
    @Override
    public boolean isAutoReconnect() {
        return this.urlParser.getOptions().autoReconnect;
    }
    
    @Override
    public UrlParser getUrlParser() {
        return this.urlParser;
    }
    
    @Override
    public abstract void preExecute() throws SQLException;
    
    @Override
    public abstract void preClose();
    
    @Override
    public abstract void reconnectFailedConnection(final SearchFilter p0) throws SQLException;
    
    @Override
    public abstract void switchReadOnlyConnection(final Boolean p0) throws SQLException;
    
    @Override
    public abstract HandleErrorResult primaryFail(final Method p0, final Object[] p1, final boolean p2, final boolean p3) throws SQLException;
    
    @Override
    public void throwFailoverMessage(final HostAddress failHostAddress, final boolean wasMaster, final SQLException queryException, final boolean reconnected) throws SQLException {
        final String firstPart = "Communications link failure with " + (wasMaster ? "primary" : "secondary") + ((failHostAddress != null) ? (" host " + failHostAddress.host + ":" + failHostAddress.port) : "") + ". ";
        String error = "";
        if (reconnected) {
            error += " Driver has reconnect connection";
        }
        else if (this.currentConnectionAttempts.get() > this.urlParser.getOptions().retriesAllDown) {
            error = error + " Driver will not try to reconnect (too much failure > " + this.urlParser.getOptions().retriesAllDown + ")";
        }
        int vendorCode = 0;
        Throwable cause = null;
        String message;
        String sqlState;
        if (queryException == null) {
            message = firstPart + error;
            sqlState = SqlStates.CONNECTION_EXCEPTION.getSqlState();
        }
        else {
            message = firstPart + queryException.getMessage() + ". " + error;
            sqlState = queryException.getSQLState();
            vendorCode = queryException.getErrorCode();
            cause = queryException.getCause();
        }
        if (sqlState != null && sqlState.startsWith("08")) {
            if (!reconnected) {
                throw new SQLNonTransientConnectionException(message, sqlState, vendorCode, cause);
            }
            sqlState = "25S03";
        }
        throw new SQLException(message, sqlState, vendorCode, cause);
    }
    
    @Override
    public boolean canRetryFailLoop() {
        return this.currentConnectionAttempts.get() < this.urlParser.getOptions().failoverLoopRetries;
    }
    
    @Override
    public void prolog(final long maxRows, final OceanBaseConnection connection, final OceanBaseStatement statement) throws SQLException {
        this.currentProtocol.prolog(maxRows, true, connection, statement);
    }
    
    @Override
    public String getCatalog() throws SQLException {
        return this.currentProtocol.getCatalog();
    }
    
    @Override
    public int getTimeout() throws SocketException {
        return this.currentProtocol.getTimeout();
    }
    
    @Override
    public abstract void reconnect() throws SQLException;
    
    @Override
    public abstract boolean checkMasterStatus(final SearchFilter p0);
    
    @Override
    public long getLastQueryNanos() {
        return this.lastQueryNanos;
    }
    
    protected boolean pingMasterProtocol(final Protocol protocol) {
        try {
            if (protocol.isValid(1000)) {
                return true;
            }
        }
        catch (SQLException ex) {}
        this.proxy.lock.lock();
        try {
            protocol.close();
            if (this.setMasterHostFail()) {
                this.addToBlacklist(protocol.getHostAddress());
            }
        }
        finally {
            this.proxy.lock.unlock();
        }
        return false;
    }
    
    public void closeConnection(final Protocol protocol) {
        if (protocol != null && protocol.isConnected()) {
            protocol.close();
        }
    }
    
    public void abortConnection(final Protocol protocol) {
        if (protocol != null && protocol.isConnected()) {
            protocol.abort();
        }
    }
    
    static {
        blacklist = new ConcurrentHashMap<HostAddress, Long>();
        connectionValidationLoop = new ConnectionValidator();
        logger = LoggerFactory.getLogger(AbstractMastersListener.class);
    }
}
