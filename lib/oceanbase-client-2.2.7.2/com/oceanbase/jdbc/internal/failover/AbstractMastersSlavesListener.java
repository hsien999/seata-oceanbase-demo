// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.failover;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import com.oceanbase.jdbc.internal.failover.tools.SearchFilter;
import java.lang.reflect.Method;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import com.oceanbase.jdbc.UrlParser;
import java.util.concurrent.atomic.AtomicBoolean;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import java.util.concurrent.atomic.AtomicReference;
import com.oceanbase.jdbc.internal.logging.Logger;

public abstract class AbstractMastersSlavesListener extends AbstractMastersListener
{
    private static final Logger logger;
    protected final AtomicReference<Protocol> waitNewSecondaryProtocol;
    protected final AtomicReference<Protocol> waitNewMasterProtocol;
    private final AtomicBoolean secondaryHostFail;
    private volatile long secondaryHostFailNanos;
    
    protected AbstractMastersSlavesListener(final UrlParser urlParser, final GlobalStateInfo globalInfo) {
        super(urlParser, globalInfo);
        this.waitNewSecondaryProtocol = new AtomicReference<Protocol>();
        this.waitNewMasterProtocol = new AtomicReference<Protocol>();
        this.secondaryHostFail = new AtomicBoolean();
        this.secondaryHostFailNanos = 0L;
        this.secondaryHostFail.set(true);
    }
    
    @Override
    public HandleErrorResult handleFailover(final SQLException qe, final Method method, final Object[] args, final Protocol protocol, final boolean isClosed) throws SQLException {
        if (this.isExplicitClosed()) {
            throw new SQLException("Connection has been closed !");
        }
        final boolean killCmd = qe != null && qe.getSQLState() != null && qe.getSQLState().equals("70100") && 1927 == qe.getErrorCode();
        if (protocol == null) {
            return this.primaryFail(method, args, killCmd, isClosed);
        }
        if (protocol.mustBeMasterConnection()) {
            if (!protocol.isMasterConnection()) {
                AbstractMastersSlavesListener.logger.warn("SQL Primary node [{}, conn={}] is now in read-only mode. Exception : {}", this.currentProtocol.getHostAddress().toString(), this.currentProtocol.getServerThreadId(), qe.getMessage());
            }
            else if (this.setMasterHostFail()) {
                AbstractMastersSlavesListener.logger.warn("SQL Primary node [{}, conn={}] connection fail. Reason : {}", this.currentProtocol.getHostAddress().toString(), this.currentProtocol.getServerThreadId(), qe.getMessage());
                this.addToBlacklist(protocol.getHostAddress());
            }
            return this.primaryFail(method, args, killCmd, isClosed);
        }
        if (this.setSecondaryHostFail()) {
            AbstractMastersSlavesListener.logger.warn("SQL secondary node [{}, conn={}] connection fail. Reason : {}", this.currentProtocol.getHostAddress().toString(), this.currentProtocol.getServerThreadId(), qe.getMessage());
            this.addToBlacklist(protocol.getHostAddress());
        }
        return this.secondaryFail(method, args, killCmd);
    }
    
    @Override
    protected void resetMasterFailoverData() {
        super.resetMasterFailoverData();
        if (!this.secondaryHostFail.get()) {
            this.currentConnectionAttempts.set(0);
            this.lastRetry = 0L;
        }
    }
    
    protected void resetSecondaryFailoverData() {
        if (this.secondaryHostFail.compareAndSet(true, false)) {
            this.secondaryHostFailNanos = 0L;
        }
        if (!this.isMasterHostFail()) {
            this.currentConnectionAttempts.set(0);
            this.lastRetry = 0L;
        }
    }
    
    public long getSecondaryHostFailNanos() {
        return this.secondaryHostFailNanos;
    }
    
    public boolean setSecondaryHostFail() {
        if (this.secondaryHostFail.compareAndSet(false, true)) {
            this.secondaryHostFailNanos = System.nanoTime();
            this.currentConnectionAttempts.set(0);
            return true;
        }
        return false;
    }
    
    public boolean isSecondaryHostFail() {
        return this.secondaryHostFail.get();
    }
    
    public boolean isSecondaryHostFailReconnect() {
        return this.secondaryHostFail.get() && this.waitNewSecondaryProtocol.get() == null;
    }
    
    public boolean isMasterHostFailReconnect() {
        return this.isMasterHostFail() && this.waitNewMasterProtocol.get() == null;
    }
    
    @Override
    public boolean hasHostFail() {
        return this.isSecondaryHostFailReconnect() || this.isMasterHostFailReconnect();
    }
    
    @Override
    public SearchFilter getFilterForFailedHost() {
        return new SearchFilter(this.isMasterHostFail(), this.isSecondaryHostFail());
    }
    
    public abstract HandleErrorResult secondaryFail(final Method p0, final Object[] p1, final boolean p2) throws SQLException;
    
    public abstract void foundActiveSecondary(final Protocol p0) throws SQLException;
    
    static {
        logger = LoggerFactory.getLogger(AbstractMastersSlavesListener.class);
    }
}
