// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.concurrent.Executor;
import java.util.Map;
import java.util.Properties;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.stats.ConnectionStats;

public class ReplicationMySQLConnection extends MultiHostMySQLConnection implements ReplicationConnection
{
    public ReplicationMySQLConnection(final MultiHostConnectionProxy proxy) {
        super(proxy);
    }
    
    @Override
    protected ReplicationConnectionProxy getThisAsProxy() {
        return (ReplicationConnectionProxy)super.getThisAsProxy();
    }
    
    @Override
    public ConnectionStats getConnectionStats() {
        return new ConnectionStats();
    }
    
    @Override
    protected MySQLConnection getActiveMySQLConnection() {
        return (MySQLConnection)this.getCurrentConnection();
    }
    
    @Override
    public synchronized Connection getCurrentConnection() {
        return this.getThisAsProxy().getCurrentConnection();
    }
    
    @Override
    public long getConnectionGroupId() {
        return this.getThisAsProxy().getConnectionGroupId();
    }
    
    @Override
    public synchronized Connection getMasterConnection() {
        return this.getThisAsProxy().getMasterConnection();
    }
    
    private Connection getValidatedMasterConnection() {
        final Connection conn = this.getThisAsProxy().masterConnection;
        try {
            return (conn == null || conn.isClosed()) ? null : conn;
        }
        catch (SQLException e) {
            return null;
        }
    }
    
    @Override
    public void promoteSlaveToMaster(final String host) throws SQLException {
        this.getThisAsProxy().promoteSlaveToMaster(host);
    }
    
    @Override
    public void removeMasterHost(final String host) throws SQLException {
        this.getThisAsProxy().removeMasterHost(host);
    }
    
    @Override
    public void removeMasterHost(final String host, final boolean waitUntilNotInUse) throws SQLException {
        this.getThisAsProxy().removeMasterHost(host, waitUntilNotInUse);
    }
    
    @Override
    public boolean isHostMaster(final String host) {
        return this.getThisAsProxy().isHostMaster(host);
    }
    
    @Override
    public synchronized Connection getSlavesConnection() {
        return this.getThisAsProxy().getSlavesConnection();
    }
    
    private Connection getValidatedSlavesConnection() {
        final Connection conn = this.getThisAsProxy().slavesConnection;
        try {
            return (conn == null || conn.isClosed()) ? null : conn;
        }
        catch (SQLException e) {
            return null;
        }
    }
    
    @Override
    public void addSlaveHost(final String host) throws SQLException {
        this.getThisAsProxy().addSlaveHost(host);
    }
    
    @Override
    public void removeSlave(final String host) throws SQLException {
        this.getThisAsProxy().removeSlave(host);
    }
    
    @Override
    public void removeSlave(final String host, final boolean closeGently) throws SQLException {
        this.getThisAsProxy().removeSlave(host, closeGently);
    }
    
    @Override
    public boolean isHostSlave(final String host) {
        return this.getThisAsProxy().isHostSlave(host);
    }
    
    @Override
    public void setReadOnly(final boolean readOnlyFlag) throws SQLException {
        this.getThisAsProxy().setReadOnly(readOnlyFlag);
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        return this.getThisAsProxy().isReadOnly();
    }
    
    @Override
    public synchronized void ping() throws SQLException {
        try {
            final Connection conn;
            if ((conn = this.getValidatedMasterConnection()) != null) {
                conn.ping();
            }
        }
        catch (SQLException e) {
            if (this.isMasterConnection()) {
                throw e;
            }
        }
        try {
            final Connection conn;
            if ((conn = this.getValidatedSlavesConnection()) != null) {
                conn.ping();
            }
        }
        catch (SQLException e) {
            if (!this.isMasterConnection()) {
                throw e;
            }
        }
    }
    
    @Override
    public synchronized void changeUser(final String userName, final String newPassword) throws SQLException {
        Connection conn;
        if ((conn = this.getValidatedMasterConnection()) != null) {
            conn.changeUser(userName, newPassword);
        }
        if ((conn = this.getValidatedSlavesConnection()) != null) {
            conn.changeUser(userName, newPassword);
        }
    }
    
    @Override
    public synchronized void setStatementComment(final String comment) {
        Connection conn;
        if ((conn = this.getValidatedMasterConnection()) != null) {
            conn.setStatementComment(comment);
        }
        if ((conn = this.getValidatedSlavesConnection()) != null) {
            conn.setStatementComment(comment);
        }
    }
    
    @Override
    public boolean hasSameProperties(final Connection c) {
        final Connection connM = this.getValidatedMasterConnection();
        final Connection connS = this.getValidatedSlavesConnection();
        return (connM != null || connS != null) && (connM == null || connM.hasSameProperties(c)) && (connS == null || connS.hasSameProperties(c));
    }
    
    @Override
    public Properties getProperties() {
        final Properties props = new Properties();
        Connection conn;
        if ((conn = this.getValidatedMasterConnection()) != null) {
            props.putAll(conn.getProperties());
        }
        if ((conn = this.getValidatedSlavesConnection()) != null) {
            props.putAll(conn.getProperties());
        }
        return props;
    }
    
    @Override
    public void abort(final Executor executor) throws SQLException {
        this.getThisAsProxy().doAbort(executor);
    }
    
    @Override
    public void abortInternal() throws SQLException {
        this.getThisAsProxy().doAbortInternal();
    }
    
    @Override
    public boolean getAllowMasterDownConnections() {
        return this.getThisAsProxy().allowMasterDownConnections;
    }
    
    @Override
    public void setAllowMasterDownConnections(final boolean connectIfMasterDown) {
        this.getThisAsProxy().allowMasterDownConnections = connectIfMasterDown;
    }
    
    @Override
    public boolean getReplicationEnableJMX() {
        return this.getThisAsProxy().enableJMX;
    }
    
    @Override
    public void setReplicationEnableJMX(final boolean replicationEnableJMX) {
        this.getThisAsProxy().enableJMX = replicationEnableJMX;
    }
    
    @Override
    public void setProxy(final MySQLConnection proxy) {
        this.getThisAsProxy().setProxy(proxy);
    }
}
