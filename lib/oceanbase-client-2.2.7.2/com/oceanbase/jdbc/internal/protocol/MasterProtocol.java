// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol;

import java.util.Collections;
import java.util.ArrayList;
import java.sql.SQLException;
import java.util.Deque;
import java.util.Collection;
import java.util.ArrayDeque;
import com.oceanbase.jdbc.internal.failover.tools.SearchFilter;
import com.oceanbase.jdbc.HostAddress;
import java.util.List;
import com.oceanbase.jdbc.internal.failover.Listener;
import com.oceanbase.jdbc.internal.failover.FailoverProxy;
import com.oceanbase.jdbc.internal.io.LruTraceCache;
import java.util.concurrent.locks.ReentrantLock;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import com.oceanbase.jdbc.UrlParser;
import java.io.Closeable;

public class MasterProtocol extends AbstractQueryProtocol implements Closeable
{
    public MasterProtocol(final UrlParser urlParser, final GlobalStateInfo globalInfo, final ReentrantLock lock, final LruTraceCache traceCache) {
        super(urlParser, globalInfo, lock, traceCache);
    }
    
    private static MasterProtocol getNewProtocol(final FailoverProxy proxy, final GlobalStateInfo globalInfo, final UrlParser urlParser) {
        final MasterProtocol newProtocol = new MasterProtocol(urlParser, globalInfo, proxy.lock, proxy.traceCache);
        newProtocol.setProxy(proxy);
        return newProtocol;
    }
    
    public static void loop(final Listener listener, final GlobalStateInfo globalInfo, final List<HostAddress> addresses, final SearchFilter searchFilter) throws SQLException {
        final ArrayDeque<HostAddress> loopAddresses = new ArrayDeque<HostAddress>(addresses);
        if (loopAddresses.isEmpty()) {
            resetHostList(listener, loopAddresses);
        }
        int maxConnectionTry = listener.getRetriesAllDown();
        boolean firstLoop = true;
        SQLException lastQueryException = null;
        while (!loopAddresses.isEmpty() || (!searchFilter.isFailoverLoop() && maxConnectionTry > 0)) {
            final MasterProtocol protocol = getNewProtocol(listener.getProxy(), globalInfo, listener.getUrlParser());
            if (listener.isExplicitClosed()) {
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
                listener.foundActiveMaster(protocol);
                return;
            }
            catch (SQLException e) {
                listener.addToBlacklist(protocol.getHostAddress());
                lastQueryException = e;
                if (!loopAddresses.isEmpty() || searchFilter.isFailoverLoop() || maxConnectionTry <= 0) {
                    continue;
                }
                resetHostList(listener, loopAddresses);
                if (firstLoop) {
                    firstLoop = false;
                    continue;
                }
                try {
                    Thread.sleep(250L);
                }
                catch (InterruptedException ex) {}
                continue;
            }
            break;
        }
        if (lastQueryException != null) {
            throw new SQLException("No active connection found for master : " + lastQueryException.getMessage(), lastQueryException.getSQLState(), lastQueryException.getErrorCode(), lastQueryException);
        }
        throw new SQLException("No active connection found for master");
    }
    
    private static void resetHostList(final Listener listener, final Deque<HostAddress> loopAddresses) {
        final List<HostAddress> servers = new ArrayList<HostAddress>();
        servers.addAll(listener.getUrlParser().getHostAddresses());
        Collections.shuffle(servers);
        loopAddresses.clear();
        loopAddresses.addAll(servers);
    }
}
