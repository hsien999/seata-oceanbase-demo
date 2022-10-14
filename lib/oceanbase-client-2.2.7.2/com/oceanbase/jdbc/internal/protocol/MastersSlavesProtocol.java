// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol;

import com.oceanbase.jdbc.internal.failover.FailoverProxy;
import java.util.Collections;
import java.util.ArrayList;
import java.sql.SQLException;
import java.util.Deque;
import java.util.Collection;
import java.util.ArrayDeque;
import com.oceanbase.jdbc.internal.failover.tools.SearchFilter;
import com.oceanbase.jdbc.HostAddress;
import java.util.List;
import com.oceanbase.jdbc.internal.failover.impl.MastersSlavesListener;
import com.oceanbase.jdbc.internal.io.LruTraceCache;
import java.util.concurrent.locks.ReentrantLock;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import com.oceanbase.jdbc.UrlParser;

public class MastersSlavesProtocol extends MasterProtocol
{
    protected boolean masterConnection;
    private boolean mustBeMasterConnection;
    
    public MastersSlavesProtocol(final UrlParser url, final GlobalStateInfo globalInfo, final ReentrantLock lock, final LruTraceCache traceCache) {
        super(url, globalInfo, lock, traceCache);
        this.masterConnection = false;
        this.mustBeMasterConnection = false;
    }
    
    public static void loop(final MastersSlavesListener listener, final GlobalStateInfo globalInfo, final List<HostAddress> addresses, final SearchFilter searchFilter) throws SQLException {
        final ArrayDeque<HostAddress> loopAddresses = new ArrayDeque<HostAddress>(addresses);
        if (loopAddresses.isEmpty()) {
            resetHostList(listener, loopAddresses);
        }
        int maxConnectionTry = listener.getRetriesAllDown();
        SQLException lastQueryException = null;
        boolean firstLoop = true;
        while (!loopAddresses.isEmpty() || (!searchFilter.isFailoverLoop() && maxConnectionTry > 0)) {
            final MastersSlavesProtocol protocol = getNewProtocol(listener.getProxy(), globalInfo, listener.getUrlParser());
            if (listener.isExplicitClosed() || (!listener.isSecondaryHostFailReconnect() && !listener.isMasterHostFailReconnect())) {
                return;
            }
            --maxConnectionTry;
            try {
                HostAddress host = loopAddresses.pollFirst();
                if (host == null) {
                    loopAddresses.addAll(listener.getUrlParser().getHostAddresses());
                    host = loopAddresses.pollFirst();
                }
                protocol.setHostAddress(host);
                protocol.connect();
                if (listener.isExplicitClosed()) {
                    protocol.close();
                    return;
                }
                listener.removeFromBlacklist(protocol.getHostAddress());
                if (listener.isMasterHostFailReconnect() && protocol.isMasterConnection()) {
                    if (foundMaster(listener, protocol, searchFilter)) {
                        return;
                    }
                }
                else if (listener.isSecondaryHostFailReconnect() && !protocol.isMasterConnection()) {
                    if (foundSecondary(listener, protocol, searchFilter)) {
                        return;
                    }
                }
                else {
                    protocol.close();
                }
            }
            catch (SQLException e) {
                lastQueryException = e;
                listener.addToBlacklist(protocol.getHostAddress());
            }
            if (!listener.isMasterHostFailReconnect() && !listener.isSecondaryHostFailReconnect()) {
                return;
            }
            if (loopAddresses.isEmpty() && listener.isMasterHostFailReconnect() && listener.urlParser.getOptions().allowMasterDownConnection && !listener.isSecondaryHostFailReconnect()) {
                return;
            }
            if (loopAddresses.isEmpty() && searchFilter.isInitialConnection() && !listener.isMasterHostFailReconnect()) {
                return;
            }
            if (!loopAddresses.isEmpty() || searchFilter.isFailoverLoop() || maxConnectionTry <= 0) {
                continue;
            }
            resetHostList(listener, loopAddresses);
            if (firstLoop) {
                firstLoop = false;
            }
            else {
                try {
                    Thread.sleep(250L);
                }
                catch (InterruptedException ex) {}
            }
        }
        if (!listener.isMasterHostFailReconnect() && !listener.isSecondaryHostFailReconnect()) {
            return;
        }
        String error = "No active connection found for replica";
        if (listener.isMasterHostFailReconnect()) {
            error = "No active connection found for master";
        }
        if (lastQueryException != null) {
            throw new SQLException(error + " : " + lastQueryException.getMessage(), lastQueryException.getSQLState(), lastQueryException.getErrorCode(), lastQueryException);
        }
        throw new SQLException(error);
    }
    
    private static void resetHostList(final MastersSlavesListener listener, final Deque<HostAddress> loopAddresses) {
        final List<HostAddress> servers = new ArrayList<HostAddress>();
        servers.addAll(listener.getUrlParser().getHostAddresses());
        Collections.shuffle(servers);
        servers.removeAll(listener.connectedHosts());
        loopAddresses.clear();
        loopAddresses.addAll(servers);
    }
    
    protected static boolean foundMaster(final MastersSlavesListener listener, final MastersSlavesProtocol protocol, final SearchFilter searchFilter) {
        protocol.setMustBeMasterConnection(true);
        if (listener.isMasterHostFailReconnect()) {
            listener.foundActiveMaster(protocol);
        }
        else {
            protocol.close();
        }
        return !listener.isSecondaryHostFailReconnect() || listener.isExplicitClosed() || searchFilter.isFineIfFoundOnlyMaster() || !listener.isSecondaryHostFailReconnect();
    }
    
    protected static boolean foundSecondary(final MastersSlavesListener listener, final MastersSlavesProtocol protocol, final SearchFilter searchFilter) throws SQLException {
        protocol.setMustBeMasterConnection(false);
        if (listener.isSecondaryHostFailReconnect()) {
            listener.foundActiveSecondary(protocol);
        }
        else {
            protocol.close();
        }
        return !listener.isMasterHostFailReconnect() || listener.isExplicitClosed() || searchFilter.isFineIfFoundOnlySlave() || !listener.isMasterHostFailReconnect();
    }
    
    private static MastersSlavesProtocol getNewProtocol(final FailoverProxy proxy, final GlobalStateInfo globalInfo, final UrlParser urlParser) {
        final MastersSlavesProtocol newProtocol = new MastersSlavesProtocol(urlParser, globalInfo, proxy.lock, proxy.traceCache);
        newProtocol.setProxy(proxy);
        return newProtocol;
    }
    
    @Override
    public boolean mustBeMasterConnection() {
        return this.mustBeMasterConnection;
    }
    
    public void setMustBeMasterConnection(final boolean mustBeMasterConnection) {
        this.mustBeMasterConnection = mustBeMasterConnection;
    }
}
