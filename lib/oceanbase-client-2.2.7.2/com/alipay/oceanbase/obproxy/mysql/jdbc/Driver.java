// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.obproxy.mysql.jdbc;

import java.sql.DriverAction;
import java.sql.DriverManager;
import com.oceanbase.jdbc.internal.util.DeRegister;
import java.sql.SQLException;

public class Driver extends com.oceanbase.jdbc.Driver
{
    public Driver() throws SQLException {
    }
    
    static {
        try {
            DriverManager.registerDriver(new com.oceanbase.jdbc.Driver(), new DeRegister());
        }
        catch (SQLException e) {
            throw new RuntimeException("Could not register driver", e);
        }
    }
}
