// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.credential;

import java.sql.SQLException;
import com.oceanbase.jdbc.HostAddress;
import com.oceanbase.jdbc.util.Options;
import java.util.function.Supplier;

public interface CredentialPlugin extends Supplier<Credential>
{
    String name();
    
    String type();
    
    default boolean mustUseSsl() {
        return false;
    }
    
    default String defaultAuthenticationPluginType() {
        return null;
    }
    
    default CredentialPlugin initialize(final Options options, final String userName, final HostAddress hostAddress) throws SQLException {
        return this;
    }
}
