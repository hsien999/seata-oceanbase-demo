// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.failover.impl;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import com.oceanbase.jdbc.internal.util.dao.ReconnectDuringTransactionException;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.protocol.MasterProtocol;
import java.util.List;
import java.util.Collections;
import java.util.Collection;
import com.oceanbase.jdbc.HostAddress;
import java.util.LinkedList;
import com.oceanbase.jdbc.internal.failover.Listener;
import com.oceanbase.jdbc.internal.failover.thread.FailoverLoop;
import com.oceanbase.jdbc.internal.failover.HandleErrorResult;
import java.lang.reflect.Method;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.failover.tools.SearchFilter;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import com.oceanbase.jdbc.UrlParser;
import com.oceanbase.jdbc.internal.util.constant.HaMode;
import com.oceanbase.jdbc.internal.logging.Logger;
import com.oceanbase.jdbc.internal.failover.AbstractMastersListener;

public class MastersFailoverListener extends AbstractMastersListener
{
    private static final Logger logger;
    private final HaMode mode;
    
    public MastersFailoverListener(final UrlParser urlParser, final GlobalStateInfo globalInfo) {
        super(urlParser, globalInfo);
        this.mode = urlParser.getHaMode();
        this.setMasterHostFail();
    }
    
    @Override
    public void initializeConnection() throws SQLException {
        super.initializeConnection();
        this.currentProtocol = null;
        this.reconnectFailedConnection(new SearchFilter(true, false));
        this.resetMasterFailoverData();
    }
    
    @Override
    public void preExecute() throws SQLException {
        this.lastQueryNanos = System.nanoTime();
        if (this.currentProtocol != null && this.currentProtocol.isClosed()) {
            this.preAutoReconnect();
        }
    }
    
    @Override
    public void preClose() {
        if (this.explicitClosed.compareAndSet(false, true)) {
            this.proxy.lock.lock();
            try {
                this.removeListenerFromSchedulers();
                this.closeConnection(this.currentProtocol);
            }
            finally {
                this.proxy.lock.unlock();
            }
        }
    }
    
    @Override
    public long getServerThreadId() {
        return this.currentProtocol.getServerThreadId();
    }
    
    @Override
    public void preAbort() {
        if (this.explicitClosed.compareAndSet(false, true)) {
            this.proxy.lock.lock();
            try {
                this.removeListenerFromSchedulers();
                this.abortConnection(this.currentProtocol);
            }
            finally {
                this.proxy.lock.unlock();
            }
        }
    }
    
    @Override
    public HandleErrorResult primaryFail(final Method method, final Object[] args, final boolean killCmd, final boolean alreadyClosed) {
        final boolean inTransaction = this.currentProtocol != null && this.currentProtocol.inTransaction();
        if (this.currentProtocol.isConnected()) {
            this.currentProtocol.close();
        }
        try {
            this.reconnectFailedConnection(new SearchFilter(true, false));
            this.handleFailLoop();
            if (killCmd) {
                return new HandleErrorResult(true, false);
            }
            if (alreadyClosed || (!inTransaction && this.isQueryRelaunchable(method, args))) {
                MastersFailoverListener.logger.info("Connection to master lost, new master {} found, query type permit to be re-execute on new server without throwing exception", this.currentProtocol.getHostAddress());
                return this.relaunchOperation(method, args);
            }
            return new HandleErrorResult(true);
        }
        catch (Exception e) {
            if (e.getCause() != null && this.proxy.hasToHandleFailover((SQLException)e.getCause()) && this.currentProtocol.isConnected()) {
                this.currentProtocol.close();
            }
            FailoverLoop.removeListener(this);
            return new HandleErrorResult();
        }
    }
    
    @Override
    public void reconnectFailedConnection(final SearchFilter searchFilter) throws SQLException {
        this.proxy.lock.lock();
        try {
            if (!searchFilter.isInitialConnection() && (this.isExplicitClosed() || !this.isMasterHostFail())) {
                return;
            }
            this.currentConnectionAttempts.incrementAndGet();
            this.resetOldsBlackListHosts();
            final List<HostAddress> loopAddress = new LinkedList<HostAddress>(this.urlParser.getHostAddresses());
            if (HaMode.LOADBALANCE.equals(this.mode)) {
                loopAddress.removeAll(this.getBlacklistKeys());
                Collections.shuffle(loopAddress);
                final List<HostAddress> blacklistShuffle = new LinkedList<HostAddress>(this.getBlacklistKeys());
                blacklistShuffle.retainAll(this.urlParser.getHostAddresses());
                Collections.shuffle(blacklistShuffle);
                loopAddress.addAll(blacklistShuffle);
            }
            else {
                loopAddress.removeAll(this.getBlacklistKeys());
                loopAddress.addAll(this.getBlacklistKeys());
                loopAddress.retainAll(this.urlParser.getHostAddresses());
            }
            if (this.currentProtocol != null && !this.isMasterHostFail()) {
                loopAddress.remove(this.currentProtocol.getHostAddress());
            }
            MasterProtocol.loop(this, this.globalInfo, loopAddress, searchFilter);
            if (!this.isMasterHostFail()) {
                FailoverLoop.removeListener(this);
            }
            this.resetMasterFailoverData();
        }
        finally {
            this.proxy.lock.unlock();
        }
    }
    
    @Override
    public void switchReadOnlyConnection(final Boolean mustBeReadOnly) throws SQLException {
        if (this.urlParser.getOptions().assureReadOnly && this.currentReadOnlyAsked != mustBeReadOnly) {
            this.proxy.lock.lock();
            try {
                if (this.currentReadOnlyAsked != mustBeReadOnly) {
                    this.currentReadOnlyAsked = mustBeReadOnly;
                    this.setSessionReadOnly(mustBeReadOnly, this.currentProtocol);
                }
            }
            finally {
                this.proxy.lock.unlock();
            }
        }
    }
    
    @Override
    public void foundActiveMaster(final Protocol protocol) throws SQLException {
        if (this.isExplicitClosed()) {
            this.proxy.lock.lock();
            try {
                protocol.close();
            }
            finally {
                this.proxy.lock.unlock();
            }
            return;
        }
        this.syncConnection(this.currentProtocol, protocol);
        this.proxy.lock.lock();
        try {
            if (this.currentProtocol != null && !this.currentProtocol.isClosed()) {
                this.currentProtocol.close();
            }
            this.currentProtocol = protocol;
        }
        finally {
            this.proxy.lock.unlock();
        }
        this.resetMasterFailoverData();
        FailoverLoop.removeListener(this);
    }
    
    @Override
    public void reconnect() throws SQLException {
        final boolean inTransaction = this.currentProtocol != null && this.currentProtocol.inTransaction();
        this.reconnectFailedConnection(new SearchFilter(true, false));
        this.handleFailLoop();
        if (inTransaction) {
            throw new ReconnectDuringTransactionException("Connection reconnect automatically during an active transaction", 1401, "25S03");
        }
    }
    
    @Override
    public void handleFailLoop() {
        if (this.isMasterHostFail()) {
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
        return this.currentProtocol != null && this.currentProtocol.isConnected();
    }
    
    @Override
    public boolean checkMasterStatus(final SearchFilter searchFilter) {
        if (this.currentProtocol != null) {
            this.pingMasterProtocol(this.currentProtocol);
        }
        return false;
    }
    
    @Override
    public void rePrepareOnSlave(final ServerPrepareResult oldServerPrepareResult, final boolean mustExecuteOnSlave) {
    }
    
    @Override
    public void reset() throws SQLException {
        if (!this.isMasterHostFail()) {
            this.currentProtocol.reset();
        }
    }
    
    static {
        logger = LoggerFactory.getLogger(MastersFailoverListener.class);
    }
}
