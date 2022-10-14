// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.authentication;

import com.oceanbase.jdbc.Driver;
import java.util.Iterator;
import java.sql.SQLException;
import java.util.ServiceLoader;

public class AuthenticationPluginLoader
{
    private static ServiceLoader<AuthenticationPlugin> loader;
    
    public static AuthenticationPlugin get(final String type) throws SQLException {
        if (type == null || type.isEmpty()) {
            return null;
        }
        for (final AuthenticationPlugin implClass : AuthenticationPluginLoader.loader) {
            if (type.equals(implClass.type())) {
                return implClass;
            }
        }
        throw new SQLException("Client does not support authentication protocol requested by server. plugin type was = '" + type + "'", "08004", 1251);
    }
    
    static {
        AuthenticationPluginLoader.loader = ServiceLoader.load(AuthenticationPlugin.class, Driver.class.getClassLoader());
    }
}
