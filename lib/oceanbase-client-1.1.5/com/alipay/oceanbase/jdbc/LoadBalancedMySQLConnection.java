// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import com.alipay.oceanbase.jdbc.stats.ConnectionStats;
import java.sql.SQLException;

public class LoadBalancedMySQLConnection extends MultiHostMySQLConnection implements LoadBalancedConnection
{
    public LoadBalancedMySQLConnection(final LoadBalancedConnectionProxy proxy) {
        super(proxy);
    }
    
    @Override
    protected LoadBalancedConnectionProxy getThisAsProxy() {
        return (LoadBalancedConnectionProxy)super.getThisAsProxy();
    }
    
    @Override
    public void close() throws SQLException {
        this.getThisAsProxy().doClose();
    }
    
    @Override
    public ConnectionStats getConnectionStats() {
        return new ConnectionStats();
    }
    
    @Override
    public void ping() throws SQLException {
        this.ping(true);
    }
    
    @Override
    public void ping(final boolean allConnections) throws SQLException {
        if (allConnections) {
            this.getThisAsProxy().doPing();
        }
        else {
            this.getActiveMySQLConnection().ping();
        }
    }
    
    @Override
    public boolean addHost(final String host) throws SQLException {
        return this.getThisAsProxy().addHost(host);
    }
    
    @Override
    public void removeHost(final String host) throws SQLException {
        this.getThisAsProxy().removeHost(host);
    }
    
    @Override
    public void removeHostWhenNotInUse(final String host) throws SQLException {
        this.getThisAsProxy().removeHostWhenNotInUse(host);
    }
}
