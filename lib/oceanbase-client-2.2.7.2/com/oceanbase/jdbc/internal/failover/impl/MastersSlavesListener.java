// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.failover.impl;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import com.oceanbase.jdbc.internal.failover.HandleErrorResult;
import com.oceanbase.jdbc.internal.protocol.MastersSlavesProtocol;
import java.util.Collections;
import java.util.Collection;
import java.util.LinkedList;
import com.oceanbase.jdbc.internal.util.dao.ReconnectDuringTransactionException;
import com.oceanbase.jdbc.OceanBaseStatement;
import com.oceanbase.jdbc.OceanBaseConnection;
import java.net.SocketException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLNonTransientConnectionException;
import com.oceanbase.jdbc.internal.util.SqlStates;
import java.lang.reflect.Method;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.failover.tools.SearchFilter;
import com.oceanbase.jdbc.internal.failover.Listener;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import com.oceanbase.jdbc.internal.failover.thread.FailoverLoop;
import java.util.ArrayDeque;
import com.oceanbase.jdbc.internal.util.scheduler.SchedulerServiceProviderHolder;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import com.oceanbase.jdbc.UrlParser;
import com.oceanbase.jdbc.HostAddress;
import java.util.List;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.util.scheduler.DynamicSizedSchedulerInterface;
import com.oceanbase.jdbc.internal.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.failover.AbstractMastersSlavesListener;

public class MastersSlavesListener extends AbstractMastersSlavesListener
{
    private static final AtomicInteger listenerCount;
    private static final Logger logger;
    private static DynamicSizedSchedulerInterface dynamicSizedScheduler;
    protected Protocol masterProtocol;
    protected Protocol secondaryProtocol;
    protected List<HostAddress> hostAddresses;
    
    public MastersSlavesListener(final UrlParser urlParser, final GlobalStateInfo globalInfo) {
        super(urlParser, globalInfo);
        if (MastersSlavesListener.dynamicSizedScheduler.isTerminated()) {
            loadScheduler();
        }
        MastersSlavesListener.listenerCount.incrementAndGet();
        this.masterProtocol = null;
        this.secondaryProtocol = null;
        this.hostAddresses = urlParser.getHostAddresses();
        this.setMasterHostFail();
        this.setSecondaryHostFail();
    }
    
    private static void loadScheduler() {
        (MastersSlavesListener.dynamicSizedScheduler = SchedulerServiceProviderHolder.getScheduler(1, "MariaDb-failover", 8)).scheduleWithFixedDelay(new Runnable() {
            private final ArrayDeque<FailoverLoop> failoverLoops = new ArrayDeque<FailoverLoop>(8);
            
            @Override
            public void run() {
                final int desiredFailCount = Math.min(8, MastersSlavesListener.listenerCount.get() / 5 + 1);
                int countChange = desiredFailCount - this.failoverLoops.size();
                if (countChange != 0) {
                    MastersSlavesListener.dynamicSizedScheduler.setPoolSize(desiredFailCount);
                    if (countChange > 0) {
                        while (countChange > 0) {
                            this.failoverLoops.add(new FailoverLoop(MastersSlavesListener.dynamicSizedScheduler));
                            --countChange;
                        }
                    }
                    else {
                        final List<FailoverLoop> removedLoops = new ArrayList<FailoverLoop>(-countChange);
                        while (countChange < 0) {
                            final FailoverLoop failoverLoop = this.failoverLoops.remove();
                            failoverLoop.unscheduleTask();
                            removedLoops.add(failoverLoop);
                            ++countChange;
                        }
                        for (final FailoverLoop failoverLoop2 : removedLoops) {
                            failoverLoop2.blockTillTerminated();
                        }
                    }
                }
            }
        }, 1L, 2L, TimeUnit.MINUTES);
    }
    
    @Override
    protected void removeListenerFromSchedulers() {
        super.removeListenerFromSchedulers();
        FailoverLoop.removeListener(this);
        MastersSlavesListener.listenerCount.addAndGet(-1);
    }
    
    @Override
    public void initializeConnection() throws SQLException {
        super.initializeConnection();
        try {
            this.reconnectFailedConnection(new SearchFilter(true));
        }
        catch (SQLException e) {
            this.checkInitialConnection(e);
        }
    }
    
    @Override
    public boolean isClosed() {
        if (this.currentProtocol != null) {
            return this.currentProtocol.isClosed();
        }
        return this.urlParser.getOptions().allowMasterDownConnection && this.secondaryProtocol.isClosed();
    }
    
    @Override
    public Object invoke(final Method method, final Object[] args) throws Throwable {
        if (this.currentProtocol == null) {
            try {
                this.reconnectFailedConnection(new SearchFilter(true, false));
                this.handleFailLoop();
            }
            catch (SQLException e) {
                FailoverLoop.removeListener(this);
                throw new InvocationTargetException(new SQLNonTransientConnectionException("No master connection available (only read-only)\n(Possible because option allowMasterDownConnection is set)", SqlStates.CONNECTION_EXCEPTION.getSqlState()));
            }
            if (!this.isMasterHostFail()) {
                try {
                    this.syncConnection(this.secondaryProtocol, this.masterProtocol);
                    this.currentProtocol = this.masterProtocol;
                    return method.invoke(this.currentProtocol, args);
                }
                catch (SQLException e) {
                    if (this.setMasterHostFail()) {
                        this.addToBlacklist(this.masterProtocol.getHostAddress());
                    }
                }
            }
            throw new InvocationTargetException(new SQLNonTransientConnectionException("No master connection available (only read-only)\n(Possible because option allowMasterDownConnection is set)", SqlStates.CONNECTION_EXCEPTION.getSqlState()));
        }
        return method.invoke(this.currentProtocol, args);
    }
    
    @Override
    public boolean versionGreaterOrEqual(final int major, final int minor, final int patch) {
        final Protocol protocol = (this.currentProtocol != null) ? this.currentProtocol : this.secondaryProtocol;
        return protocol != null && protocol.versionGreaterOrEqual(major, minor, patch);
    }
    
    @Override
    public boolean isServerMariaDb() {
        final Protocol protocol = (this.currentProtocol != null) ? this.currentProtocol : this.secondaryProtocol;
        return protocol != null && protocol.isServerMariaDb();
    }
    
    @Override
    public boolean sessionStateAware() {
        final Protocol protocol = (this.currentProtocol != null) ? this.currentProtocol : this.secondaryProtocol;
        return protocol != null && protocol.sessionStateAware();
    }
    
    @Override
    public String getCatalog() throws SQLException {
        final Protocol protocol = (this.currentProtocol != null) ? this.currentProtocol : this.secondaryProtocol;
        if (protocol == null) {
            return this.urlParser.getDatabase();
        }
        return protocol.getCatalog();
    }
    
    @Override
    public int getMajorServerVersion() {
        final Protocol protocol = (this.currentProtocol != null) ? this.currentProtocol : this.secondaryProtocol;
        if (protocol == null) {
            return 5;
        }
        return protocol.getMajorServerVersion();
    }
    
    @Override
    public boolean isMasterConnection() {
        return this.currentProtocol == null || this.currentProtocol.isMasterConnection();
    }
    
    @Override
    public int getTimeout() throws SocketException {
        if (this.currentProtocol != null) {
            return this.currentProtocol.getTimeout();
        }
        return (this.urlParser.getOptions().socketTimeout == null) ? 0 : this.urlParser.getOptions().socketTimeout;
    }
    
    @Override
    public void prolog(final long maxRows, final OceanBaseConnection connection, final OceanBaseStatement statement) throws SQLException {
        if (this.currentProtocol != null) {
            this.currentProtocol.prolog(maxRows, true, connection, statement);
        }
    }
    
    @Override
    public boolean noBackslashEscapes() {
        final Protocol protocol = (this.currentProtocol != null) ? this.currentProtocol : this.secondaryProtocol;
        return protocol.noBackslashEscapes();
    }
    
    @Override
    public long getServerThreadId() {
        if (this.currentProtocol == null) {
            return -1L;
        }
        return this.currentProtocol.getServerThreadId();
    }
    
    protected void checkInitialConnection(final SQLException queryException) throws SQLException {
        Protocol waitingProtocol;
        if (this.isSecondaryHostFail() && (waitingProtocol = this.waitNewSecondaryProtocol.getAndSet(null)) != null) {
            this.secondaryProtocol = waitingProtocol;
            if (this.urlParser.getOptions().assureReadOnly) {
                this.setSessionReadOnly(true, this.secondaryProtocol);
            }
            if (this.currentReadOnlyAsked) {
                this.currentProtocol = waitingProtocol;
            }
            this.resetSecondaryFailoverData();
        }
        if (this.isMasterHostFail() && (waitingProtocol = this.waitNewMasterProtocol.getAndSet(null)) != null) {
            this.masterProtocol = waitingProtocol;
            if (!this.currentReadOnlyAsked || this.isSecondaryHostFail()) {
                this.currentProtocol = waitingProtocol;
            }
            this.resetMasterFailoverData();
        }
        if (this.masterProtocol == null || !this.masterProtocol.isConnected()) {
            this.setMasterHostFail();
            if (!this.urlParser.getOptions().allowMasterDownConnection || this.secondaryProtocol == null) {
                this.throwFailoverMessage((this.masterProtocol != null) ? this.masterProtocol.getHostAddress() : null, true, queryException, false);
            }
        }
        else {
            this.resetMasterFailoverData();
            if (this.isSecondaryHostFail()) {
                this.handleFailLoop();
            }
        }
    }
    
    @Override
    public void preClose() {
        if (this.explicitClosed.compareAndSet(false, true)) {
            this.proxy.lock.lock();
            try {
                this.removeListenerFromSchedulers();
                this.closeConnection(this.waitNewSecondaryProtocol.getAndSet(null));
                this.closeConnection(this.waitNewMasterProtocol.getAndSet(null));
                this.closeConnection(this.masterProtocol);
                this.closeConnection(this.secondaryProtocol);
            }
            finally {
                this.proxy.lock.unlock();
            }
        }
    }
    
    @Override
    public void preAbort() {
        if (this.explicitClosed.compareAndSet(false, true)) {
            this.proxy.lock.lock();
            try {
                this.removeListenerFromSchedulers();
                this.abortConnection(this.waitNewSecondaryProtocol.getAndSet(null));
                this.abortConnection(this.waitNewMasterProtocol.getAndSet(null));
                this.abortConnection(this.masterProtocol);
                this.abortConnection(this.secondaryProtocol);
            }
            finally {
                this.proxy.lock.unlock();
            }
        }
    }
    
    @Override
    public void preExecute() throws SQLException {
        this.lastQueryNanos = System.nanoTime();
        this.checkWaitingConnection();
        if (this.currentProtocol != null && (this.currentProtocol.isClosed() || (!this.currentReadOnlyAsked && !this.currentProtocol.isMasterConnection()))) {
            this.preAutoReconnect();
        }
    }
    
    @Override
    public boolean isValid(final int timeout) throws SQLException {
        if (this.currentProtocol == null) {
            return false;
        }
        if (this.currentProtocol.isMasterConnection()) {
            final boolean valid = this.currentProtocol.isValid(timeout);
            if (this.secondaryProtocol != null) {
                try {
                    final boolean secondValid = this.secondaryProtocol.isValid(timeout);
                    if (!valid && this.urlParser.getOptions().allowMasterDownConnection && secondValid) {
                        this.setMasterHostFail();
                        return true;
                    }
                }
                catch (SQLException ex) {}
            }
            return valid;
        }
        final boolean valid = this.currentProtocol.isValid(timeout);
        if (this.masterProtocol != null) {
            try {
                this.masterProtocol.isValid(timeout);
            }
            catch (SQLException ex2) {}
        }
        return valid;
    }
    
    public void checkWaitingConnection() throws SQLException {
        if (this.isSecondaryHostFail()) {
            this.proxy.lock.lock();
            try {
                final Protocol waitingProtocol = this.waitNewSecondaryProtocol.getAndSet(null);
                if (waitingProtocol != null && this.pingSecondaryProtocol(waitingProtocol)) {
                    this.lockAndSwitchSecondary(waitingProtocol);
                }
            }
            finally {
                this.proxy.lock.unlock();
            }
        }
        if (this.isMasterHostFail()) {
            this.proxy.lock.lock();
            try {
                final Protocol waitingProtocol = this.waitNewMasterProtocol.getAndSet(null);
                if (waitingProtocol != null && this.pingMasterProtocol(waitingProtocol)) {
                    this.lockAndSwitchMaster(waitingProtocol);
                }
            }
            finally {
                this.proxy.lock.unlock();
            }
        }
    }
    
    @Override
    public void reconnectFailedConnection(final SearchFilter searchFilter) throws SQLException {
        if (!searchFilter.isInitialConnection() && (this.isExplicitClosed() || (searchFilter.isFineIfFoundOnlyMaster() && !this.isMasterHostFail()) || (searchFilter.isFineIfFoundOnlySlave() && !this.isSecondaryHostFail()))) {
            return;
        }
        if (!searchFilter.isFailoverLoop()) {
            try {
                this.checkWaitingConnection();
                if ((searchFilter.isFineIfFoundOnlyMaster() && !this.isMasterHostFail()) || (searchFilter.isFineIfFoundOnlySlave() && !this.isSecondaryHostFail())) {
                    return;
                }
            }
            catch (ReconnectDuringTransactionException e) {
                return;
            }
        }
        this.currentConnectionAttempts.incrementAndGet();
        this.resetOldsBlackListHosts();
        final List<HostAddress> loopAddress = new LinkedList<HostAddress>(this.hostAddresses);
        loopAddress.removeAll(this.getBlacklistKeys());
        Collections.shuffle(loopAddress);
        final List<HostAddress> blacklistShuffle = new LinkedList<HostAddress>(this.getBlacklistKeys());
        blacklistShuffle.retainAll(this.hostAddresses);
        Collections.shuffle(blacklistShuffle);
        loopAddress.addAll(blacklistShuffle);
        if (this.masterProtocol != null && !this.isMasterHostFail()) {
            loopAddress.remove(this.masterProtocol.getHostAddress());
            loopAddress.add(this.masterProtocol.getHostAddress());
        }
        if (this.secondaryProtocol != null && !this.isSecondaryHostFail()) {
            loopAddress.remove(this.secondaryProtocol.getHostAddress());
            loopAddress.add(this.secondaryProtocol.getHostAddress());
        }
        if (this.isMasterHostFail() || this.isSecondaryHostFail() || searchFilter.isInitialConnection()) {
            do {
                MastersSlavesProtocol.loop(this, this.globalInfo, loopAddress, searchFilter);
                if (!searchFilter.isFailoverLoop()) {
                    try {
                        this.checkWaitingConnection();
                    }
                    catch (ReconnectDuringTransactionException ex) {}
                }
            } while (searchFilter.isInitialConnection() && this.masterProtocol == null && (!this.urlParser.getOptions().allowMasterDownConnection || this.secondaryProtocol == null));
            if (searchFilter.isInitialConnection() && this.masterProtocol == null && this.currentReadOnlyAsked) {
                this.currentProtocol = this.secondaryProtocol;
                this.currentReadOnlyAsked = true;
            }
        }
    }
    
    @Override
    public void foundActiveMaster(final Protocol newMasterProtocol) {
        if (this.isMasterHostFail()) {
            if (this.isExplicitClosed()) {
                newMasterProtocol.close();
                return;
            }
            if (!this.waitNewMasterProtocol.compareAndSet(null, newMasterProtocol)) {
                newMasterProtocol.close();
            }
        }
        else {
            newMasterProtocol.close();
        }
    }
    
    public void lockAndSwitchMaster(final Protocol newMasterProtocol) throws ReconnectDuringTransactionException {
        if (this.masterProtocol != null && !this.masterProtocol.isClosed()) {
            this.masterProtocol.close();
        }
        if (!this.currentReadOnlyAsked || this.isSecondaryHostFail()) {
            if (this.currentProtocol != null) {
                try {
                    this.syncConnection(this.currentProtocol, newMasterProtocol);
                }
                catch (Exception ex) {}
            }
            this.currentProtocol = newMasterProtocol;
        }
        final boolean inTransaction = this.masterProtocol != null && this.masterProtocol.inTransaction();
        this.masterProtocol = newMasterProtocol;
        this.resetMasterFailoverData();
        if (inTransaction) {
            throw new ReconnectDuringTransactionException("Connection reconnect automatically during an active transaction", 1401, "25S03");
        }
    }
    
    @Override
    public void foundActiveSecondary(final Protocol newSecondaryProtocol) throws SQLException {
        if (this.isSecondaryHostFail()) {
            if (this.isExplicitClosed()) {
                newSecondaryProtocol.close();
                return;
            }
            if (this.proxy.lock.tryLock()) {
                try {
                    this.lockAndSwitchSecondary(newSecondaryProtocol);
                }
                finally {
                    this.proxy.lock.unlock();
                }
            }
            else if (!this.waitNewSecondaryProtocol.compareAndSet(null, newSecondaryProtocol)) {
                newSecondaryProtocol.close();
            }
        }
        else {
            newSecondaryProtocol.close();
        }
    }
    
    public void lockAndSwitchSecondary(final Protocol newSecondaryProtocol) throws SQLException {
        if (this.secondaryProtocol != null && !this.secondaryProtocol.isClosed()) {
            this.secondaryProtocol.close();
        }
        if (this.currentReadOnlyAsked || (this.urlParser.getOptions().failOnReadOnly && !this.currentReadOnlyAsked && this.isMasterHostFail())) {
            if (this.currentProtocol != null) {
                try {
                    this.syncConnection(this.currentProtocol, newSecondaryProtocol);
                }
                catch (Exception ex) {}
            }
            this.currentProtocol = newSecondaryProtocol;
        }
        this.secondaryProtocol = newSecondaryProtocol;
        if (this.urlParser.getOptions().assureReadOnly) {
            this.setSessionReadOnly(true, this.secondaryProtocol);
        }
        this.resetSecondaryFailoverData();
    }
    
    @Override
    public void switchReadOnlyConnection(final Boolean mustBeReadOnly) throws SQLException {
        this.checkWaitingConnection();
        if (this.currentReadOnlyAsked != mustBeReadOnly) {
            this.proxy.lock.lock();
            try {
                if (this.currentReadOnlyAsked == mustBeReadOnly) {
                    return;
                }
                this.currentReadOnlyAsked = mustBeReadOnly;
                if (this.currentReadOnlyAsked) {
                    if (this.currentProtocol == null) {
                        this.currentProtocol = this.secondaryProtocol;
                    }
                    else if (this.currentProtocol.isMasterConnection()) {
                        if (!this.isSecondaryHostFail()) {
                            try {
                                this.syncConnection(this.masterProtocol, this.secondaryProtocol);
                                this.currentProtocol = this.secondaryProtocol;
                                return;
                            }
                            catch (SQLException e) {
                                if (this.setSecondaryHostFail()) {
                                    this.addToBlacklist(this.secondaryProtocol.getHostAddress());
                                }
                            }
                        }
                        FailoverLoop.addListener(this);
                    }
                }
                else if (this.currentProtocol == null) {
                    this.currentProtocol = this.masterProtocol;
                }
                else if (!this.currentProtocol.isMasterConnection()) {
                    Label_0279: {
                        if (!this.isMasterHostFail()) {
                            try {
                                this.syncConnection(this.secondaryProtocol, this.masterProtocol);
                                this.currentProtocol = this.masterProtocol;
                                return;
                            }
                            catch (SQLException e) {
                                if (this.setMasterHostFail()) {
                                    this.addToBlacklist(this.masterProtocol.getHostAddress());
                                }
                                break Label_0279;
                            }
                        }
                        if (this.urlParser.getOptions().allowMasterDownConnection) {
                            this.currentProtocol = null;
                            return;
                        }
                        try {
                            this.reconnectFailedConnection(new SearchFilter(true, false));
                            this.handleFailLoop();
                        }
                        catch (SQLException e) {
                            FailoverLoop.removeListener(this);
                            final HostAddress failHost = (this.masterProtocol != null) ? this.masterProtocol.getHostAddress() : null;
                            this.throwFailoverMessage(failHost, true, new SQLException("master connection failed"), false);
                        }
                    }
                    if (!this.isMasterHostFail()) {
                        try {
                            this.syncConnection(this.secondaryProtocol, this.masterProtocol);
                            this.currentProtocol = this.masterProtocol;
                        }
                        catch (SQLException e) {
                            if (this.setMasterHostFail()) {
                                this.addToBlacklist(this.masterProtocol.getHostAddress());
                            }
                        }
                    }
                    else {
                        this.currentReadOnlyAsked = !mustBeReadOnly;
                        final HostAddress failHost2 = (this.masterProtocol != null) ? this.masterProtocol.getHostAddress() : null;
                        this.throwFailoverMessage(failHost2, true, new SQLException("master connection failed"), false);
                    }
                }
            }
            finally {
                this.proxy.lock.unlock();
            }
        }
    }
    
    @Override
    public HandleErrorResult primaryFail(final Method method, final Object[] args, final boolean killCmd, final boolean isClosed) {
        final boolean alreadyClosed = this.masterProtocol == null || isClosed;
        final boolean inTransaction = this.masterProtocol != null && this.masterProtocol.inTransaction();
        if (this.masterProtocol != null && this.masterProtocol.isConnected()) {
            this.masterProtocol.close();
        }
        if (this.urlParser.getOptions().failOnReadOnly && !this.isSecondaryHostFail()) {
            try {
                if (this.secondaryProtocol != null && this.secondaryProtocol.ping()) {
                    this.proxy.lock.lock();
                    try {
                        if (this.masterProtocol != null) {
                            this.syncConnection(this.masterProtocol, this.secondaryProtocol);
                        }
                        this.currentProtocol = this.secondaryProtocol;
                    }
                    finally {
                        this.proxy.lock.unlock();
                    }
                    FailoverLoop.addListener(this);
                    try {
                        return this.relaunchOperation(method, args);
                    }
                    catch (Exception ex) {
                        return new HandleErrorResult();
                    }
                }
            }
            catch (Exception e) {
                if (this.setSecondaryHostFail()) {
                    this.blackListAndCloseConnection(this.secondaryProtocol);
                }
            }
        }
        try {
            this.reconnectFailedConnection(new SearchFilter(true, this.urlParser.getOptions().failOnReadOnly));
            this.handleFailLoop();
            if (this.currentProtocol == null) {
                this.setMasterHostFail();
                FailoverLoop.removeListener(this);
                return new HandleErrorResult();
            }
            if (killCmd) {
                return new HandleErrorResult(true, false);
            }
            if (this.currentReadOnlyAsked || alreadyClosed || (!inTransaction && this.isQueryRelaunchable(method, args))) {
                MastersSlavesListener.logger.info("Connection to master lost, new master {}, conn={} found, query type permit to be re-execute on new server without throwing exception", this.currentProtocol.getHostAddress(), this.currentProtocol.getServerThreadId());
                return this.relaunchOperation(method, args);
            }
            return new HandleErrorResult(true);
        }
        catch (Exception e) {
            if (e.getCause() != null && this.proxy.hasToHandleFailover((SQLException)e.getCause()) && this.currentProtocol != null && this.currentProtocol.isConnected()) {
                this.currentProtocol.close();
            }
            this.setMasterHostFail();
            FailoverLoop.removeListener(this);
            return new HandleErrorResult();
        }
    }
    
    private void blackListAndCloseConnection(final Protocol protocol) {
        this.addToBlacklist(protocol.getHostAddress());
        if (protocol.isConnected()) {
            this.proxy.lock.lock();
            try {
                protocol.close();
            }
            finally {
                this.proxy.lock.unlock();
            }
        }
    }
    
    @Override
    public void reconnect() throws SQLException {
        boolean inTransaction = false;
        SearchFilter filter;
        if (this.currentReadOnlyAsked) {
            filter = new SearchFilter(true, true);
        }
        else {
            inTransaction = (this.masterProtocol != null && this.masterProtocol.inTransaction());
            filter = new SearchFilter(true, this.urlParser.getOptions().failOnReadOnly);
        }
        this.reconnectFailedConnection(filter);
        this.handleFailLoop();
        if (inTransaction) {
            throw new ReconnectDuringTransactionException("Connection reconnect automatically during an active transaction", 1401, "25S03");
        }
    }
    
    private boolean pingSecondaryProtocol(final Protocol protocol) {
        try {
            if (protocol != null && protocol.isConnected() && protocol.ping()) {
                return true;
            }
        }
        catch (Exception e) {
            protocol.close();
            if (this.setSecondaryHostFail()) {
                this.addToBlacklist(protocol.getHostAddress());
            }
        }
        return false;
    }
    
    @Override
    public HandleErrorResult secondaryFail(final Method method, final Object[] args, final boolean killCmd) throws SQLException {
        this.proxy.lock.lock();
        try {
            if (this.pingSecondaryProtocol(this.secondaryProtocol)) {
                return this.relaunchOperation(method, args);
            }
        }
        finally {
            this.proxy.lock.unlock();
        }
        if (!this.isMasterHostFail()) {
            try {
                if (this.masterProtocol != null && this.masterProtocol.isValid(1000)) {
                    this.syncConnection(this.secondaryProtocol, this.masterProtocol);
                    this.proxy.lock.lock();
                    try {
                        this.currentProtocol = this.masterProtocol;
                    }
                    finally {
                        this.proxy.lock.unlock();
                    }
                    FailoverLoop.addListener(this);
                    MastersSlavesListener.logger.info("Connection to slave lost, using master connection, query is re-execute on master server without throwing exception");
                    return this.relaunchOperation(method, args);
                }
            }
            catch (Exception e) {
                if (this.setMasterHostFail()) {
                    this.blackListAndCloseConnection(this.masterProtocol);
                }
            }
        }
        try {
            this.reconnectFailedConnection(new SearchFilter(true, true));
            this.handleFailLoop();
            if (this.isSecondaryHostFail()) {
                this.syncConnection(this.secondaryProtocol, this.masterProtocol);
                this.proxy.lock.lock();
                try {
                    this.currentProtocol = this.masterProtocol;
                }
                finally {
                    this.proxy.lock.unlock();
                }
            }
            if (killCmd) {
                return new HandleErrorResult(true, false);
            }
            MastersSlavesListener.logger.info("Connection to slave lost, new slave {}, conn={} found, query is re-execute on new server without throwing exception", this.currentProtocol.getHostAddress(), this.currentProtocol.getServerThreadId());
            return this.relaunchOperation(method, args);
        }
        catch (Exception ee) {
            FailoverLoop.removeListener(this);
            return new HandleErrorResult();
        }
    }
    
    @Override
    public void handleFailLoop() {
        if (this.isMasterHostFail() || this.isSecondaryHostFail()) {
            if (!this.isExplicitClosed()) {
                FailoverLoop.addListener(this);
            }
        }
        else {
            FailoverLoop.removeListener(this);
        }
    }
    
    @Override
    public boolean isMasterConnected() {
        return this.masterProtocol != null && this.masterProtocol.isConnected();
    }
    
    @Override
    public boolean inTransaction() {
        return this.currentProtocol == null || this.currentProtocol.inTransaction();
    }
    
    @Override
    public boolean checkMasterStatus(final SearchFilter searchFilter) {
        if (this.masterProtocol != null) {
            this.pingMasterProtocol(this.masterProtocol);
        }
        return false;
    }
    
    @Override
    public void rePrepareOnSlave(final ServerPrepareResult oldServerPrepareResult, final boolean mustBeOnMaster) throws SQLException {
        if (this.isSecondaryHostFail()) {
            final Protocol waitingProtocol = this.waitNewSecondaryProtocol.getAndSet(null);
            if (waitingProtocol != null) {
                this.proxy.lock.lock();
                try {
                    if (this.pingSecondaryProtocol(waitingProtocol)) {
                        this.lockAndSwitchSecondary(waitingProtocol);
                    }
                }
                finally {
                    this.proxy.lock.unlock();
                }
            }
        }
        if (this.secondaryProtocol != null && !this.isSecondaryHostFail()) {
            final ServerPrepareResult serverPrepareResult = this.secondaryProtocol.prepare(oldServerPrepareResult.getSql(), mustBeOnMaster);
            try {
                serverPrepareResult.getUnProxiedProtocol().releasePrepareStatement(serverPrepareResult);
            }
            catch (SQLException ex) {}
            oldServerPrepareResult.failover(serverPrepareResult.getStatementId(), this.secondaryProtocol);
        }
    }
    
    public List<HostAddress> connectedHosts() {
        final List<HostAddress> usedHost = new ArrayList<HostAddress>();
        if (this.isMasterHostFail()) {
            final Protocol masterProtocol = this.waitNewMasterProtocol.get();
            if (masterProtocol != null) {
                usedHost.add(masterProtocol.getHostAddress());
            }
        }
        else {
            usedHost.add(this.masterProtocol.getHostAddress());
        }
        if (this.isSecondaryHostFail()) {
            final Protocol secondProtocol = this.waitNewSecondaryProtocol.get();
            if (secondProtocol != null) {
                usedHost.add(secondProtocol.getHostAddress());
            }
        }
        else {
            usedHost.add(this.secondaryProtocol.getHostAddress());
        }
        return usedHost;
    }
    
    @Override
    public void reset() throws SQLException {
        if (!this.isMasterHostFail()) {
            this.masterProtocol.reset();
        }
        if (!this.isSecondaryHostFail()) {
            this.secondaryProtocol.reset();
        }
    }
    
    static {
        listenerCount = new AtomicInteger();
        logger = LoggerFactory.getLogger(MastersSlavesListener.class);
        loadScheduler();
    }
}
