// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jmx;

import com.alipay.oceanbase.jdbc.ConnectionGroupManager;
import java.sql.SQLException;
import javax.management.MBeanServer;
import com.alipay.oceanbase.jdbc.ExceptionInterceptor;
import com.alipay.oceanbase.jdbc.SQLError;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class LoadBalanceConnectionGroupManager implements LoadBalanceConnectionGroupManagerMBean
{
    private boolean isJmxRegistered;
    
    public LoadBalanceConnectionGroupManager() {
        this.isJmxRegistered = false;
    }
    
    public synchronized void registerJmx() throws SQLException {
        if (this.isJmxRegistered) {
            return;
        }
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        try {
            final ObjectName name = new ObjectName("com.alipay.oceanbase.jdbc.jmx:type=LoadBalanceConnectionGroupManager");
            mbs.registerMBean(this, name);
            this.isJmxRegistered = true;
        }
        catch (Exception e) {
            throw SQLError.createSQLException("Unable to register load-balance management bean with JMX", null, e, null);
        }
    }
    
    @Override
    public void addHost(final String group, final String host, final boolean forExisting) {
        try {
            ConnectionGroupManager.addHost(group, host, forExisting);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public int getActiveHostCount(final String group) {
        return ConnectionGroupManager.getActiveHostCount(group);
    }
    
    @Override
    public long getActiveLogicalConnectionCount(final String group) {
        return ConnectionGroupManager.getActiveLogicalConnectionCount(group);
    }
    
    @Override
    public long getActivePhysicalConnectionCount(final String group) {
        return ConnectionGroupManager.getActivePhysicalConnectionCount(group);
    }
    
    @Override
    public int getTotalHostCount(final String group) {
        return ConnectionGroupManager.getTotalHostCount(group);
    }
    
    @Override
    public long getTotalLogicalConnectionCount(final String group) {
        return ConnectionGroupManager.getTotalLogicalConnectionCount(group);
    }
    
    @Override
    public long getTotalPhysicalConnectionCount(final String group) {
        return ConnectionGroupManager.getTotalPhysicalConnectionCount(group);
    }
    
    @Override
    public long getTotalTransactionCount(final String group) {
        return ConnectionGroupManager.getTotalTransactionCount(group);
    }
    
    @Override
    public void removeHost(final String group, final String host) throws SQLException {
        ConnectionGroupManager.removeHost(group, host);
    }
    
    @Override
    public String getActiveHostsList(final String group) {
        return ConnectionGroupManager.getActiveHostLists(group);
    }
    
    @Override
    public String getRegisteredConnectionGroups() {
        return ConnectionGroupManager.getRegisteredConnectionGroups();
    }
    
    @Override
    public void stopNewConnectionsToHost(final String group, final String host) throws SQLException {
        ConnectionGroupManager.removeHost(group, host);
    }
}
