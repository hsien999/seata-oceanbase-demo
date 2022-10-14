// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;

public interface ReplicationConnection extends ObConnection
{
    long getConnectionGroupId();
    
    Connection getCurrentConnection();
    
    Connection getMasterConnection();
    
    void promoteSlaveToMaster(final String p0) throws SQLException;
    
    void removeMasterHost(final String p0) throws SQLException;
    
    void removeMasterHost(final String p0, final boolean p1) throws SQLException;
    
    boolean isHostMaster(final String p0);
    
    Connection getSlavesConnection();
    
    void addSlaveHost(final String p0) throws SQLException;
    
    void removeSlave(final String p0) throws SQLException;
    
    void removeSlave(final String p0, final boolean p1) throws SQLException;
    
    boolean isHostSlave(final String p0);
}
