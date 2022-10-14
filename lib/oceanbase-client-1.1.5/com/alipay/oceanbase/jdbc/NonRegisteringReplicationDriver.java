// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.Connection;
import java.util.Properties;
import java.sql.SQLException;

public class NonRegisteringReplicationDriver extends NonRegisteringDriver
{
    public NonRegisteringReplicationDriver() throws SQLException {
    }
    
    @Override
    public Connection connect(final String url, final Properties info) throws SQLException {
        return this.connectReplicationConnection(url, info);
    }
}
