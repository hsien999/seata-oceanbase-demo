// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.util.Map;
import java.util.List;

public interface BalanceStrategy extends Extension
{
    ConnectionImpl pickConnection(final LoadBalancedConnectionProxy p0, final List<String> p1, final Map<String, ConnectionImpl> p2, final long[] p3, final int p4) throws SQLException;
}
