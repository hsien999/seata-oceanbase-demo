// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.tls;

import java.util.Iterator;
import java.sql.SQLException;
import java.util.ServiceLoader;
import com.oceanbase.jdbc.internal.protocol.tls.DefaultTlsSocketPlugin;

public class TlsSocketPluginLoader
{
    public static TlsSocketPlugin get(final String type) throws SQLException {
        if (type == null || type.isEmpty()) {
            return new DefaultTlsSocketPlugin();
        }
        final ServiceLoader<TlsSocketPlugin> loader = ServiceLoader.load(TlsSocketPlugin.class);
        for (final TlsSocketPlugin implClass : loader) {
            if (type.equals(implClass.type())) {
                return implClass;
            }
        }
        throw new SQLException("Client has not found any TLS factory plugin with name '" + type + "'.", "08004", 1251);
    }
}
