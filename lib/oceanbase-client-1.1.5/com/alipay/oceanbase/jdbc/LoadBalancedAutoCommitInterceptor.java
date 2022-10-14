// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.util.Properties;

public class LoadBalancedAutoCommitInterceptor implements StatementInterceptorV2
{
    private int matchingAfterStatementCount;
    private int matchingAfterStatementThreshold;
    private String matchingAfterStatementRegex;
    private ConnectionImpl conn;
    private LoadBalancedConnectionProxy proxy;
    
    public LoadBalancedAutoCommitInterceptor() {
        this.matchingAfterStatementCount = 0;
        this.matchingAfterStatementThreshold = 0;
        this.proxy = null;
    }
    
    @Override
    public void destroy() {
    }
    
    @Override
    public boolean executeTopLevelOnly() {
        return false;
    }
    
    @Override
    public void init(final Connection connection, final Properties props) throws SQLException {
        this.conn = (ConnectionImpl)connection;
        final String autoCommitSwapThresholdAsString = props.getProperty("loadBalanceAutoCommitStatementThreshold", "0");
        try {
            this.matchingAfterStatementThreshold = Integer.parseInt(autoCommitSwapThresholdAsString);
        }
        catch (NumberFormatException ex) {}
        final String autoCommitSwapRegex = props.getProperty("loadBalanceAutoCommitStatementRegex", "");
        if ("".equals(autoCommitSwapRegex)) {
            return;
        }
        this.matchingAfterStatementRegex = autoCommitSwapRegex;
    }
    
    @Override
    public ResultSetInternalMethods postProcess(final String sql, final Statement interceptedStatement, final ResultSetInternalMethods originalResultSet, final Connection connection, final int warningCount, final boolean noIndexUsed, final boolean noGoodIndexUsed, final SQLException statementException) throws SQLException {
        if (!this.conn.getAutoCommit()) {
            this.matchingAfterStatementCount = 0;
        }
        else {
            if (this.proxy == null && this.conn.isProxySet()) {
                MySQLConnection lcl_proxy;
                for (lcl_proxy = this.conn.getMultiHostSafeProxy(); lcl_proxy != null && !(lcl_proxy instanceof LoadBalancedMySQLConnection); lcl_proxy = lcl_proxy.getMultiHostSafeProxy()) {}
                if (lcl_proxy != null) {
                    this.proxy = ((LoadBalancedMySQLConnection)lcl_proxy).getThisAsProxy();
                }
            }
            if (this.proxy != null && (this.matchingAfterStatementRegex == null || sql.matches(this.matchingAfterStatementRegex))) {
                ++this.matchingAfterStatementCount;
            }
            if (this.matchingAfterStatementCount >= this.matchingAfterStatementThreshold) {
                this.matchingAfterStatementCount = 0;
                try {
                    if (this.proxy != null) {
                        this.proxy.pickNewConnection();
                    }
                }
                catch (SQLException ex) {}
            }
        }
        return originalResultSet;
    }
    
    @Override
    public ResultSetInternalMethods preProcess(final String sql, final Statement interceptedStatement, final Connection connection) throws SQLException {
        return null;
    }
}
