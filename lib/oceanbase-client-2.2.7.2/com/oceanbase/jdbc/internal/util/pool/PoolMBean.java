// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.pool;

public interface PoolMBean
{
    long getActiveConnections();
    
    long getTotalConnections();
    
    long getIdleConnections();
    
    long getConnectionRequests();
    
    void resetStaticGlobal();
}
