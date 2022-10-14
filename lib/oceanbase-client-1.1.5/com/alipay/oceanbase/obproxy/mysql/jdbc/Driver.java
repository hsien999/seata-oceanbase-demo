// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.obproxy.mysql.jdbc;

import java.sql.DriverManager;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.NonRegisteringDriver;

@Deprecated
public class Driver extends NonRegisteringDriver implements java.sql.Driver
{
    public Driver() throws SQLException {
    }
    
    static {
        try {
            DriverManager.registerDriver(new Driver());
        }
        catch (SQLException E) {
            throw new RuntimeException("Can't register driver!");
        }
    }
}
