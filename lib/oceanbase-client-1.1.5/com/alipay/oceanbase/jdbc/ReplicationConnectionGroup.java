// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.Set;
import java.util.HashMap;

public class ReplicationConnectionGroup
{
    private String groupName;
    private long connections;
    private long slavesAdded;
    private long slavesRemoved;
    private long slavesPromoted;
    private long activeConnections;
    private HashMap<Long, ReplicationConnection> replicationConnections;
    private Set<String> slaveHostList;
    private boolean isInitialized;
    private Set<String> masterHostList;
    
    ReplicationConnectionGroup(final String groupName) {
        this.connections = 0L;
        this.slavesAdded = 0L;
        this.slavesRemoved = 0L;
        this.slavesPromoted = 0L;
        this.activeConnections = 0L;
        this.replicationConnections = new HashMap<Long, ReplicationConnection>();
        this.slaveHostList = new CopyOnWriteArraySet<String>();
        this.isInitialized = false;
        this.masterHostList = new CopyOnWriteArraySet<String>();
        this.groupName = groupName;
    }
    
    public long getConnectionCount() {
        return this.connections;
    }
    
    public long registerReplicationConnection(final ReplicationConnection conn, final List<String> localMasterList, final List<String> localSlaveList) {
        final long currentConnectionId;
        synchronized (this) {
            if (!this.isInitialized) {
                if (localMasterList != null) {
                    this.masterHostList.addAll(localMasterList);
                }
                if (localSlaveList != null) {
                    this.slaveHostList.addAll(localSlaveList);
                }
                this.isInitialized = true;
            }
            final long connections = this.connections + 1L;
            this.connections = connections;
            currentConnectionId = connections;
            this.replicationConnections.put(currentConnectionId, conn);
        }
        ++this.activeConnections;
        return currentConnectionId;
    }
    
    public String getGroupName() {
        return this.groupName;
    }
    
    public Collection<String> getMasterHosts() {
        return this.masterHostList;
    }
    
    public Collection<String> getSlaveHosts() {
        return this.slaveHostList;
    }
    
    public void addSlaveHost(final String hostPortPair) throws SQLException {
        if (this.slaveHostList.add(hostPortPair)) {
            ++this.slavesAdded;
            for (final ReplicationConnection c : this.replicationConnections.values()) {
                c.addSlaveHost(hostPortPair);
            }
        }
    }
    
    public void handleCloseConnection(final ReplicationConnection conn) {
        this.replicationConnections.remove(conn.getConnectionGroupId());
        --this.activeConnections;
    }
    
    public void removeSlaveHost(final String hostPortPair, final boolean closeGently) throws SQLException {
        if (this.slaveHostList.remove(hostPortPair)) {
            ++this.slavesRemoved;
            for (final ReplicationConnection c : this.replicationConnections.values()) {
                c.removeSlave(hostPortPair, closeGently);
            }
        }
    }
    
    public void promoteSlaveToMaster(final String hostPortPair) throws SQLException {
        if (this.slaveHostList.remove(hostPortPair) | this.masterHostList.add(hostPortPair)) {
            ++this.slavesPromoted;
            for (final ReplicationConnection c : this.replicationConnections.values()) {
                c.promoteSlaveToMaster(hostPortPair);
            }
        }
    }
    
    public void removeMasterHost(final String hostPortPair) throws SQLException {
        this.removeMasterHost(hostPortPair, true);
    }
    
    public void removeMasterHost(final String hostPortPair, final boolean closeGently) throws SQLException {
        if (this.masterHostList.remove(hostPortPair)) {
            for (final ReplicationConnection c : this.replicationConnections.values()) {
                c.removeMasterHost(hostPortPair, closeGently);
            }
        }
    }
    
    public int getConnectionCountWithHostAsSlave(final String hostPortPair) {
        int matched = 0;
        for (final ReplicationConnection c : this.replicationConnections.values()) {
            if (c.isHostSlave(hostPortPair)) {
                ++matched;
            }
        }
        return matched;
    }
    
    public int getConnectionCountWithHostAsMaster(final String hostPortPair) {
        int matched = 0;
        for (final ReplicationConnection c : this.replicationConnections.values()) {
            if (c.isHostMaster(hostPortPair)) {
                ++matched;
            }
        }
        return matched;
    }
    
    public long getNumberOfSlavesAdded() {
        return this.slavesAdded;
    }
    
    public long getNumberOfSlavesRemoved() {
        return this.slavesRemoved;
    }
    
    public long getNumberOfSlavePromotions() {
        return this.slavesPromoted;
    }
    
    public long getTotalConnectionCount() {
        return this.connections;
    }
    
    public long getActiveConnectionCount() {
        return this.activeConnections;
    }
    
    @Override
    public String toString() {
        return "ReplicationConnectionGroup[groupName=" + this.groupName + ",masterHostList=" + this.masterHostList + ",slaveHostList=" + this.slaveHostList + "]";
    }
}
