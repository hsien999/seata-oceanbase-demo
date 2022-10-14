// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdbc2.optional;

import java.sql.SQLException;
import java.sql.Connection;
import javax.sql.XAConnection;
import javax.sql.XADataSource;

public class MysqlXADataSource extends MysqlDataSource implements XADataSource
{
    static final long serialVersionUID = 7911390333152247455L;
    
    @Override
    public XAConnection getXAConnection() throws SQLException {
        final Connection conn = this.getConnection();
        return this.wrapConnection(conn);
    }
    
    @Override
    public XAConnection getXAConnection(final String u, final String p) throws SQLException {
        final Connection conn = this.getConnection(u, p);
        return this.wrapConnection(conn);
    }
    
    private XAConnection wrapConnection(final Connection conn) throws SQLException {
        if (this.getPinGlobalTxToPhysicalConnection() || ((com.alipay.oceanbase.jdbc.Connection)conn).getPinGlobalTxToPhysicalConnection()) {
            return SuspendableXAConnection.getInstance((com.alipay.oceanbase.jdbc.Connection)conn);
        }
        return MysqlXAConnection.getInstance((com.alipay.oceanbase.jdbc.Connection)conn, this.getLogXaCommands());
    }
}
