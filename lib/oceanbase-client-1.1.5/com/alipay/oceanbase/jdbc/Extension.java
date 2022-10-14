// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public interface Extension
{
    void init(final Connection p0, final Properties p1) throws SQLException;
    
    void destroy();
}
