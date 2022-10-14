// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;

public interface Wrapper
{
     <T> T unwrap(final Class<T> p0) throws SQLException;
    
    boolean isWrapperFor(final Class<?> p0) throws SQLException;
}
