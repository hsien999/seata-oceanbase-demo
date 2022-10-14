// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;
import java.util.Collection;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;

public class ConnectionGroup
{
    private String groupName;
    private long connections;
    private long activeConnections;
    private HashMap<Long, LoadBalancedConnectionProxy> connectionProxies;
    private Set<String> hostList;
    private boolean isInitialized;
    private long closedProxyTotalPhysicalConnections;
    private long closedProxyTotalTransactions;
    private int activeHosts;
    private Set<String> closedHosts;
    
    ConnectionGroup(final String groupName) {
        this.connections = 0L;
        this.activeConnections = 0L;
        this.connectionProxies = new HashMap<Long, LoadBalancedConnectionProxy>();
        this.hostList = new HashSet<String>();
        this.isInitialized = false;
        this.closedProxyTotalPhysicalConnections = 0L;
        this.closedProxyTotalTransactions = 0L;
        this.activeHosts = 0;
        this.closedHosts = new HashSet<String>();
        this.groupName = groupName;
    }
    
    public long registerConnectionProxy(final LoadBalancedConnectionProxy proxy, final List<String> localHostList) {
        final long currentConnectionId;
        synchronized (this) {
            if (!this.isInitialized) {
                this.hostList.addAll(localHostList);
                this.isInitialized = true;
                this.activeHosts = localHostList.size();
            }
            final long connections = this.connections + 1L;
            this.connections = connections;
            currentConnectionId = connections;
            this.connectionProxies.put(currentConnectionId, proxy);
        }
        ++this.activeConnections;
        return currentConnectionId;
    }
    
    public String getGroupName() {
        return this.groupName;
    }
    
    public Collection<String> getInitialHosts() {
        return this.hostList;
    }
    
    public int getActiveHostCount() {
        return this.activeHosts;
    }
    
    public Collection<String> getClosedHosts() {
        return this.closedHosts;
    }
    
    public long getTotalLogicalConnectionCount() {
        return this.connections;
    }
    
    public long getActiveLogicalConnectionCount() {
        return this.activeConnections;
    }
    
    public long getActivePhysicalConnectionCount() {
        long result = 0L;
        final Map<Long, LoadBalancedConnectionProxy> proxyMap = new HashMap<Long, LoadBalancedConnectionProxy>();
        synchronized (this.connectionProxies) {
            proxyMap.putAll(this.connectionProxies);
        }
        for (final LoadBalancedConnectionProxy proxy : proxyMap.values()) {
            result += proxy.getActivePhysicalConnectionCount();
        }
        return result;
    }
    
    public long getTotalPhysicalConnectionCount() {
        long allConnections = this.closedProxyTotalPhysicalConnections;
        final Map<Long, LoadBalancedConnectionProxy> proxyMap = new HashMap<Long, LoadBalancedConnectionProxy>();
        synchronized (this.connectionProxies) {
            proxyMap.putAll(this.connectionProxies);
        }
        for (final LoadBalancedConnectionProxy proxy : proxyMap.values()) {
            allConnections += proxy.getTotalPhysicalConnectionCount();
        }
        return allConnections;
    }
    
    public long getTotalTransactionCount() {
        long transactions = this.closedProxyTotalTransactions;
        final Map<Long, LoadBalancedConnectionProxy> proxyMap = new HashMap<Long, LoadBalancedConnectionProxy>();
        synchronized (this.connectionProxies) {
            proxyMap.putAll(this.connectionProxies);
        }
        for (final LoadBalancedConnectionProxy proxy : proxyMap.values()) {
            transactions += proxy.getTransactionCount();
        }
        return transactions;
    }
    
    public void closeConnectionProxy(final LoadBalancedConnectionProxy proxy) {
        --this.activeConnections;
        this.connectionProxies.remove(proxy.getConnectionGroupProxyID());
        this.closedProxyTotalPhysicalConnections += proxy.getTotalPhysicalConnectionCount();
        this.closedProxyTotalTransactions += proxy.getTransactionCount();
    }
    
    public void removeHost(final String hostPortPair) throws SQLException {
        this.removeHost(hostPortPair, false);
    }
    
    public void removeHost(final String hostPortPair, final boolean removeExisting) throws SQLException {
        this.removeHost(hostPortPair, removeExisting, true);
    }
    
    public synchronized void removeHost(final String hostPortPair, final boolean removeExisting, final boolean waitForGracefulFailover) throws SQLException {
        if (this.activeHosts == 1) {
            throw SQLError.createSQLException("Cannot remove host, only one configured host active.", null);
        }
        if (this.hostList.remove(hostPortPair)) {
            --this.activeHosts;
            if (removeExisting) {
                final Map<Long, LoadBalancedConnectionProxy> proxyMap = new HashMap<Long, LoadBalancedConnectionProxy>();
                synchronized (this.connectionProxies) {
                    proxyMap.putAll(this.connectionProxies);
                }
                for (final LoadBalancedConnectionProxy proxy : proxyMap.values()) {
                    if (waitForGracefulFailover) {
                        proxy.removeHostWhenNotInUse(hostPortPair);
                    }
                    else {
                        proxy.removeHost(hostPortPair);
                    }
                }
            }
            this.closedHosts.add(hostPortPair);
            return;
        }
        throw SQLError.createSQLException("Host is not configured: " + hostPortPair, null);
    }
    
    public void addHost(final String hostPortPair) {
        this.addHost(hostPortPair, false);
    }
    
    public void addHost(final String hostPortPair, final boolean forExisting) {
        synchronized (this) {
            if (this.hostList.add(hostPortPair)) {
                ++this.activeHosts;
            }
        }
        if (!forExisting) {
            return;
        }
        final Map<Long, LoadBalancedConnectionProxy> proxyMap = new HashMap<Long, LoadBalancedConnectionProxy>();
        synchronized (this.connectionProxies) {
            proxyMap.putAll(this.connectionProxies);
        }
        for (final LoadBalancedConnectionProxy proxy : proxyMap.values()) {
            proxy.addHost(hostPortPair);
        }
    }
}
