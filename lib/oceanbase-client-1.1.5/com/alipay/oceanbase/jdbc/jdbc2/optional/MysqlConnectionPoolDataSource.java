// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdbc2.optional;

import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.Connection;
import javax.sql.PooledConnection;
import javax.sql.ConnectionPoolDataSource;

public class MysqlConnectionPoolDataSource extends MysqlDataSource implements ConnectionPoolDataSource
{
    static final long serialVersionUID = -7767325445592304961L;
    
    @Override
    public synchronized PooledConnection getPooledConnection() throws SQLException {
        final java.sql.Connection connection = this.getConnection();
        final MysqlPooledConnection mysqlPooledConnection = MysqlPooledConnection.getInstance((Connection)connection);
        return mysqlPooledConnection;
    }
    
    @Override
    public synchronized PooledConnection getPooledConnection(final String s, final String s1) throws SQLException {
        final java.sql.Connection connection = this.getConnection(s, s1);
        final MysqlPooledConnection mysqlPooledConnection = MysqlPooledConnection.getInstance((Connection)connection);
        return mysqlPooledConnection;
    }
}
