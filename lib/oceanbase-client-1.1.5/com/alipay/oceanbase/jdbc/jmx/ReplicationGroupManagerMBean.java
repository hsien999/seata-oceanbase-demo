// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jmx;

import java.sql.SQLException;

public interface ReplicationGroupManagerMBean
{
    void addSlaveHost(final String p0, final String p1) throws SQLException;
    
    void removeSlaveHost(final String p0, final String p1) throws SQLException;
    
    void promoteSlaveToMaster(final String p0, final String p1) throws SQLException;
    
    void removeMasterHost(final String p0, final String p1) throws SQLException;
    
    String getMasterHostsList(final String p0);
    
    String getSlaveHostsList(final String p0);
    
    String getRegisteredConnectionGroups();
    
    int getActiveMasterHostCount(final String p0);
    
    int getActiveSlaveHostCount(final String p0);
    
    int getSlavePromotionCount(final String p0);
    
    long getTotalLogicalConnectionCount(final String p0);
    
    long getActiveLogicalConnectionCount(final String p0);
}
