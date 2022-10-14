// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.credential;

import com.oceanbase.jdbc.Driver;
import java.util.Iterator;
import java.sql.SQLException;
import java.util.ServiceLoader;

public class CredentialPluginLoader
{
    private static ServiceLoader<CredentialPlugin> loader;
    
    public static CredentialPlugin get(final String type) throws SQLException {
        if (type == null || type.isEmpty()) {
            return null;
        }
        for (final CredentialPlugin implClass : CredentialPluginLoader.loader) {
            if (type.equals(implClass.type())) {
                return implClass;
            }
        }
        throw new SQLException("No identity plugin registered with the type \"" + type + "\".", "08004", 1251);
    }
    
    static {
        CredentialPluginLoader.loader = ServiceLoader.load(CredentialPlugin.class, Driver.class.getClassLoader());
    }
}
