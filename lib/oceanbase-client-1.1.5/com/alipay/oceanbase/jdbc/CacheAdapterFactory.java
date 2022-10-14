// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public interface CacheAdapterFactory<K, V>
{
    CacheAdapter<K, V> getInstance(final Connection p0, final String p1, final int p2, final int p3, final Properties p4) throws SQLException;
}
