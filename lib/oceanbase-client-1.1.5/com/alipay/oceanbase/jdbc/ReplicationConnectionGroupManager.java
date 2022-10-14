// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.jmx.ReplicationGroupManager;
import java.util.HashMap;

public class ReplicationConnectionGroupManager
{
    private static HashMap<String, ReplicationConnectionGroup> GROUP_MAP;
    private static ReplicationGroupManager mbean;
    private static boolean hasRegisteredJmx;
    
    public static synchronized ReplicationConnectionGroup getConnectionGroupInstance(final String groupName) {
        if (ReplicationConnectionGroupManager.GROUP_MAP.containsKey(groupName)) {
            return ReplicationConnectionGroupManager.GROUP_MAP.get(groupName);
        }
        final ReplicationConnectionGroup group = new ReplicationConnectionGroup(groupName);
        ReplicationConnectionGroupManager.GROUP_MAP.put(groupName, group);
        return group;
    }
    
    public static void registerJmx() throws SQLException {
        if (ReplicationConnectionGroupManager.hasRegisteredJmx) {
            return;
        }
        ReplicationConnectionGroupManager.mbean.registerJmx();
        ReplicationConnectionGroupManager.hasRegisteredJmx = true;
    }
    
    public static ReplicationConnectionGroup getConnectionGroup(final String groupName) {
        return ReplicationConnectionGroupManager.GROUP_MAP.get(groupName);
    }
    
    public static Collection<ReplicationConnectionGroup> getGroupsMatching(final String group) {
        if (group == null || group.equals("")) {
            final Set<ReplicationConnectionGroup> s = new HashSet<ReplicationConnectionGroup>();
            s.addAll(ReplicationConnectionGroupManager.GROUP_MAP.values());
            return s;
        }
        final Set<ReplicationConnectionGroup> s = new HashSet<ReplicationConnectionGroup>();
        final ReplicationConnectionGroup o = ReplicationConnectionGroupManager.GROUP_MAP.get(group);
        if (o != null) {
            s.add(o);
        }
        return s;
    }
    
    public static void addSlaveHost(final String group, final String hostPortPair) throws SQLException {
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(group);
        for (final ReplicationConnectionGroup cg : s) {
            cg.addSlaveHost(hostPortPair);
        }
    }
    
    public static void removeSlaveHost(final String group, final String hostPortPair) throws SQLException {
        removeSlaveHost(group, hostPortPair, true);
    }
    
    public static void removeSlaveHost(final String group, final String hostPortPair, final boolean closeGently) throws SQLException {
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(group);
        for (final ReplicationConnectionGroup cg : s) {
            cg.removeSlaveHost(hostPortPair, closeGently);
        }
    }
    
    public static void promoteSlaveToMaster(final String group, final String hostPortPair) throws SQLException {
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(group);
        for (final ReplicationConnectionGroup cg : s) {
            cg.promoteSlaveToMaster(hostPortPair);
        }
    }
    
    public static long getSlavePromotionCount(final String group) throws SQLException {
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(group);
        long promoted = 0L;
        for (final ReplicationConnectionGroup cg : s) {
            final long tmp = cg.getNumberOfSlavePromotions();
            if (tmp > promoted) {
                promoted = tmp;
            }
        }
        return promoted;
    }
    
    public static void removeMasterHost(final String group, final String hostPortPair) throws SQLException {
        removeMasterHost(group, hostPortPair, true);
    }
    
    public static void removeMasterHost(final String group, final String hostPortPair, final boolean closeGently) throws SQLException {
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(group);
        for (final ReplicationConnectionGroup cg : s) {
            cg.removeMasterHost(hostPortPair, closeGently);
        }
    }
    
    public static String getRegisteredReplicationConnectionGroups() {
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(null);
        final StringBuilder sb = new StringBuilder();
        String sep = "";
        for (final ReplicationConnectionGroup cg : s) {
            final String group = cg.getGroupName();
            sb.append(sep);
            sb.append(group);
            sep = ",";
        }
        return sb.toString();
    }
    
    public static int getNumberOfMasterPromotion(final String groupFilter) {
        int total = 0;
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(groupFilter);
        for (final ReplicationConnectionGroup cg : s) {
            total += (int)cg.getNumberOfSlavePromotions();
        }
        return total;
    }
    
    public static int getConnectionCountWithHostAsSlave(final String groupFilter, final String hostPortPair) {
        int total = 0;
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(groupFilter);
        for (final ReplicationConnectionGroup cg : s) {
            total += cg.getConnectionCountWithHostAsSlave(hostPortPair);
        }
        return total;
    }
    
    public static int getConnectionCountWithHostAsMaster(final String groupFilter, final String hostPortPair) {
        int total = 0;
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(groupFilter);
        for (final ReplicationConnectionGroup cg : s) {
            total += cg.getConnectionCountWithHostAsMaster(hostPortPair);
        }
        return total;
    }
    
    public static Collection<String> getSlaveHosts(final String groupFilter) {
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(groupFilter);
        final Collection<String> hosts = new ArrayList<String>();
        for (final ReplicationConnectionGroup cg : s) {
            hosts.addAll(cg.getSlaveHosts());
        }
        return hosts;
    }
    
    public static Collection<String> getMasterHosts(final String groupFilter) {
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(groupFilter);
        final Collection<String> hosts = new ArrayList<String>();
        for (final ReplicationConnectionGroup cg : s) {
            hosts.addAll(cg.getMasterHosts());
        }
        return hosts;
    }
    
    public static long getTotalConnectionCount(final String group) {
        long connections = 0L;
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(group);
        for (final ReplicationConnectionGroup cg : s) {
            connections += cg.getTotalConnectionCount();
        }
        return connections;
    }
    
    public static long getActiveConnectionCount(final String group) {
        long connections = 0L;
        final Collection<ReplicationConnectionGroup> s = getGroupsMatching(group);
        for (final ReplicationConnectionGroup cg : s) {
            connections += cg.getActiveConnectionCount();
        }
        return connections;
    }
    
    static {
        ReplicationConnectionGroupManager.GROUP_MAP = new HashMap<String, ReplicationConnectionGroup>();
        ReplicationConnectionGroupManager.mbean = new ReplicationGroupManager();
        ReplicationConnectionGroupManager.hasRegisteredJmx = false;
    }
}
