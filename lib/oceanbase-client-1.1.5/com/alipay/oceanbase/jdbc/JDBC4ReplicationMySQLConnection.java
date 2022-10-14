// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.NClob;
import java.sql.Clob;
import java.sql.Blob;
import java.sql.SQLClientInfoException;
import java.util.Properties;
import java.sql.Struct;
import java.sql.Array;
import java.sql.SQLXML;
import com.alipay.oceanbase.jdbc.stats.ConnectionStats;
import java.sql.SQLException;

public class JDBC4ReplicationMySQLConnection extends ReplicationMySQLConnection implements JDBC4MySQLConnection
{
    public JDBC4ReplicationMySQLConnection(final ReplicationConnectionProxy proxy) throws SQLException {
        super(proxy);
    }
    
    @Override
    public ConnectionStats getConnectionStats() {
        return new ConnectionStats();
    }
    
    private JDBC4MySQLConnection getJDBC4Connection() {
        return (JDBC4MySQLConnection)this.getActiveMySQLConnection();
    }
    
    @Override
    public SQLXML createSQLXML() throws SQLException {
        return this.getJDBC4Connection().createSQLXML();
    }
    
    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        return this.getJDBC4Connection().createArrayOf(typeName, elements);
    }
    
    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
        return this.getJDBC4Connection().createStruct(typeName, attributes);
    }
    
    @Override
    public Properties getClientInfo() throws SQLException {
        return this.getJDBC4Connection().getClientInfo();
    }
    
    @Override
    public String getClientInfo(final String name) throws SQLException {
        return this.getJDBC4Connection().getClientInfo(name);
    }
    
    @Override
    public boolean isValid(final int timeout) throws SQLException {
        return this.getJDBC4Connection().isValid(timeout);
    }
    
    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException {
        this.getJDBC4Connection().setClientInfo(properties);
    }
    
    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
        this.getJDBC4Connection().setClientInfo(name, value);
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        this.checkClosed();
        return iface.isInstance(this);
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        }
        catch (ClassCastException cce) {
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.getExceptionInterceptor());
        }
    }
    
    @Override
    public Blob createBlob() {
        return this.getJDBC4Connection().createBlob();
    }
    
    @Override
    public Clob createClob() {
        return this.getJDBC4Connection().createClob();
    }
    
    @Override
    public NClob createNClob() {
        return this.getJDBC4Connection().createNClob();
    }
    
    @Override
    public JDBC4ClientInfoProvider getClientInfoProviderImpl() throws SQLException {
        synchronized (this.getThisAsProxy()) {
            return this.getJDBC4Connection().getClientInfoProviderImpl();
        }
    }
}
