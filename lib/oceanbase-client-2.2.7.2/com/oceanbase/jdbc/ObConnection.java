// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.SQLException;

public interface ObConnection extends MysqlConnection
{
    void setSessionTimeZone(final String p0) throws SQLException;
    
    String getSessionTimeZone();
    
    void changeUser(final String p0, final String p1) throws SQLException;
}
