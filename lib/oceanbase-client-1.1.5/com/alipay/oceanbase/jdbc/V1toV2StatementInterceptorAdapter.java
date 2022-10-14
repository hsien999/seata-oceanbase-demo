// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.Properties;
import java.sql.SQLException;

public class V1toV2StatementInterceptorAdapter implements StatementInterceptorV2
{
    private final StatementInterceptor toProxy;
    
    public V1toV2StatementInterceptorAdapter(final StatementInterceptor toProxy) {
        this.toProxy = toProxy;
    }
    
    @Override
    public ResultSetInternalMethods postProcess(final String sql, final Statement interceptedStatement, final ResultSetInternalMethods originalResultSet, final Connection connection, final int warningCount, final boolean noIndexUsed, final boolean noGoodIndexUsed, final SQLException statementException) throws SQLException {
        return this.toProxy.postProcess(sql, interceptedStatement, originalResultSet, connection);
    }
    
    @Override
    public void destroy() {
        this.toProxy.destroy();
    }
    
    @Override
    public boolean executeTopLevelOnly() {
        return this.toProxy.executeTopLevelOnly();
    }
    
    @Override
    public void init(final Connection conn, final Properties props) throws SQLException {
        this.toProxy.init(conn, props);
    }
    
    @Override
    public ResultSetInternalMethods preProcess(final String sql, final Statement interceptedStatement, final Connection connection) throws SQLException {
        return this.toProxy.preProcess(sql, interceptedStatement, connection);
    }
}
