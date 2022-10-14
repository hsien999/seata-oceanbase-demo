// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Driver;

public class ReplicationDriver extends NonRegisteringReplicationDriver implements Driver
{
    public ReplicationDriver() throws SQLException {
    }
    
    static {
        try {
            DriverManager.registerDriver(new NonRegisteringReplicationDriver());
        }
        catch (SQLException E) {
            throw new RuntimeException("Can't register driver!");
        }
    }
}
