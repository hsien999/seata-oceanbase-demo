// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEventHandler;
import com.alipay.oceanbase.jdbc.log.Log;

public class ProfilerEventHandlerFactory
{
    private Connection ownerConnection;
    protected Log log;
    
    public static synchronized ProfilerEventHandler getInstance(final MySQLConnection conn) throws SQLException {
        ProfilerEventHandler handler = conn.getProfilerEventHandlerInstance();
        if (handler == null) {
            handler = (ProfilerEventHandler)Util.getInstance(conn.getProfilerEventHandler(), new Class[0], new Object[0], conn.getExceptionInterceptor());
            conn.initializeExtension(handler);
            conn.setProfilerEventHandlerInstance(handler);
        }
        return handler;
    }
    
    public static synchronized void removeInstance(final MySQLConnection conn) {
        final ProfilerEventHandler handler = conn.getProfilerEventHandlerInstance();
        if (handler != null) {
            handler.destroy();
        }
    }
    
    private ProfilerEventHandlerFactory(final Connection conn) {
        this.ownerConnection = null;
        this.log = null;
        this.ownerConnection = conn;
        try {
            this.log = this.ownerConnection.getLog();
        }
        catch (SQLException sqlEx) {
            throw new RuntimeException("Unable to get logger from connection");
        }
    }
}
