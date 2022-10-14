// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.jmx.LoadBalanceConnectionGroupManager;
import java.util.HashMap;

public class ConnectionGroupManager
{
    private static HashMap<String, ConnectionGroup> GROUP_MAP;
    private static LoadBalanceConnectionGroupManager mbean;
    private static boolean hasRegisteredJmx;
    
    public static synchronized ConnectionGroup getConnectionGroupInstance(final String groupName) {
        if (ConnectionGroupManager.GROUP_MAP.containsKey(groupName)) {
            return ConnectionGroupManager.GROUP_MAP.get(groupName);
        }
        final ConnectionGroup group = new ConnectionGroup(groupName);
        ConnectionGroupManager.GROUP_MAP.put(groupName, group);
        return group;
    }
    
    public static void registerJmx() throws SQLException {
        if (ConnectionGroupManager.hasRegisteredJmx) {
            return;
        }
        ConnectionGroupManager.mbean.registerJmx();
        ConnectionGroupManager.hasRegisteredJmx = true;
    }
    
    public static ConnectionGroup getConnectionGroup(final String groupName) {
        return ConnectionGroupManager.GROUP_MAP.get(groupName);
    }
    
    private static Collection<ConnectionGroup> getGroupsMatching(final String group) {
        if (group == null || group.equals("")) {
            final Set<ConnectionGroup> s = new HashSet<ConnectionGroup>();
            s.addAll(ConnectionGroupManager.GROUP_MAP.values());
            return s;
        }
        final Set<ConnectionGroup> s = new HashSet<ConnectionGroup>();
        final ConnectionGroup o = ConnectionGroupManager.GROUP_MAP.get(group);
        if (o != null) {
            s.add(o);
        }
        return s;
    }
    
    public static void addHost(final String group, final String hostPortPair, final boolean forExisting) {
        final Collection<ConnectionGroup> s = getGroupsMatching(group);
        for (final ConnectionGroup cg : s) {
            cg.addHost(hostPortPair, forExisting);
        }
    }
    
    public static int getActiveHostCount(final String group) {
        final Set<String> active = new HashSet<String>();
        final Collection<ConnectionGroup> s = getGroupsMatching(group);
        for (final ConnectionGroup cg : s) {
            active.addAll(cg.getInitialHosts());
        }
        return active.size();
    }
    
    public static long getActiveLogicalConnectionCount(final String group) {
        int count = 0;
        final Collection<ConnectionGroup> s = getGroupsMatching(group);
        for (final ConnectionGroup cg : s) {
            count += (int)cg.getActiveLogicalConnectionCount();
        }
        return count;
    }
    
    public static long getActivePhysicalConnectionCount(final String group) {
        int count = 0;
        final Collection<ConnectionGroup> s = getGroupsMatching(group);
        for (final ConnectionGroup cg : s) {
            count += (int)cg.getActivePhysicalConnectionCount();
        }
        return count;
    }
    
    public static int getTotalHostCount(final String group) {
        final Collection<ConnectionGroup> s = getGroupsMatching(group);
        final Set<String> hosts = new HashSet<String>();
        for (final ConnectionGroup cg : s) {
            hosts.addAll(cg.getInitialHosts());
            hosts.addAll(cg.getClosedHosts());
        }
        return hosts.size();
    }
    
    public static long getTotalLogicalConnectionCount(final String group) {
        long count = 0L;
        final Collection<ConnectionGroup> s = getGroupsMatching(group);
        for (final ConnectionGroup cg : s) {
            count += cg.getTotalLogicalConnectionCount();
        }
        return count;
    }
    
    public static long getTotalPhysicalConnectionCount(final String group) {
        long count = 0L;
        final Collection<ConnectionGroup> s = getGroupsMatching(group);
        for (final ConnectionGroup cg : s) {
            count += cg.getTotalPhysicalConnectionCount();
        }
        return count;
    }
    
    public static long getTotalTransactionCount(final String group) {
        long count = 0L;
        final Collection<ConnectionGroup> s = getGroupsMatching(group);
        for (final ConnectionGroup cg : s) {
            count += cg.getTotalTransactionCount();
        }
        return count;
    }
    
    public static void removeHost(final String group, final String hostPortPair) throws SQLException {
        removeHost(group, hostPortPair, false);
    }
    
    public static void removeHost(final String group, final String host, final boolean removeExisting) throws SQLException {
        final Collection<ConnectionGroup> s = getGroupsMatching(group);
        for (final ConnectionGroup cg : s) {
            cg.removeHost(host, removeExisting);
        }
    }
    
    public static String getActiveHostLists(final String group) {
        final Collection<ConnectionGroup> s = getGroupsMatching(group);
        final Map<String, Integer> hosts = new HashMap<String, Integer>();
        for (final ConnectionGroup cg : s) {
            final Collection<String> l = cg.getInitialHosts();
            for (final String host : l) {
                Integer o = hosts.get(host);
                if (o == null) {
                    o = 1;
                }
                else {
                    ++o;
                }
                hosts.put(host, o);
            }
        }
        final StringBuilder sb = new StringBuilder();
        String sep = "";
        for (final String host2 : hosts.keySet()) {
            sb.append(sep);
            sb.append(host2);
            sb.append('(');
            sb.append(hosts.get(host2));
            sb.append(')');
            sep = ",";
        }
        return sb.toString();
    }
    
    public static String getRegisteredConnectionGroups() {
        final Collection<ConnectionGroup> s = getGroupsMatching(null);
        final StringBuilder sb = new StringBuilder();
        String sep = "";
        for (final ConnectionGroup cg : s) {
            final String group = cg.getGroupName();
            sb.append(sep);
            sb.append(group);
            sep = ",";
        }
        return sb.toString();
    }
    
    static {
        ConnectionGroupManager.GROUP_MAP = new HashMap<String, ConnectionGroup>();
        ConnectionGroupManager.mbean = new LoadBalanceConnectionGroupManager();
        ConnectionGroupManager.hasRegisteredJmx = false;
    }
}
