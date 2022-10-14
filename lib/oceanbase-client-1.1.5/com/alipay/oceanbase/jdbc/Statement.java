// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.io.InputStream;
import java.sql.SQLException;

public interface Statement extends java.sql.Statement, Wrapper
{
    void enableStreamingResults() throws SQLException;
    
    void disableStreamingResults() throws SQLException;
    
    void setLocalInfileInputStream(final InputStream p0);
    
    InputStream getLocalInfileInputStream();
    
    void setPingTarget(final PingTarget p0);
    
    ExceptionInterceptor getExceptionInterceptor();
    
    void removeOpenResultSet(final ResultSetInternalMethods p0);
    
    int getOpenResultSetCount();
    
    void setHoldResultsOpenOverClose(final boolean p0);
}
