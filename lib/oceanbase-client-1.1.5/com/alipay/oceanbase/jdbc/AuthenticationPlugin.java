// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.util.List;

public interface AuthenticationPlugin extends Extension
{
    String getProtocolPluginName();
    
    boolean requiresConfidentiality();
    
    boolean isReusable();
    
    void setAuthenticationParameters(final String p0, final String p1);
    
    boolean nextAuthenticationStep(final Buffer p0, final List<Buffer> p1) throws SQLException;
}
