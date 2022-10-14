// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.profiler;

import java.sql.SQLException;
import java.util.Properties;
import com.alipay.oceanbase.jdbc.Connection;
import com.alipay.oceanbase.jdbc.log.Log;

public class LoggingProfilerEventHandler implements ProfilerEventHandler
{
    private Log log;
    
    @Override
    public void consumeEvent(final ProfilerEvent evt) {
        if (evt.eventType == 0) {
            this.log.logWarn(evt);
        }
        else {
            this.log.logInfo(evt);
        }
    }
    
    @Override
    public void destroy() {
        this.log = null;
    }
    
    @Override
    public void init(final Connection conn, final Properties props) throws SQLException {
        this.log = conn.getLog();
    }
}
