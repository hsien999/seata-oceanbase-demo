// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.DriverAction;
import java.sql.DriverManager;
import com.oceanbase.jdbc.internal.util.DeRegister;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import com.oceanbase.jdbc.internal.util.constant.Version;
import java.lang.reflect.Field;
import java.util.List;
import java.util.ArrayList;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.util.DefaultOptions;
import com.oceanbase.jdbc.internal.util.constant.HaMode;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import java.sql.Connection;
import java.util.Properties;

public class Driver implements java.sql.Driver
{
    @Override
    public Connection connect(final String url, final Properties props) throws SQLException {
        final UrlParser urlParser = UrlParser.parse(url, props);
        if (urlParser == null || urlParser.getHostAddresses() == null) {
            return null;
        }
        return OceanBaseConnection.newConnection(urlParser, null);
    }
    
    @Override
    public boolean acceptsURL(final String url) {
        return UrlParser.acceptsUrl(url);
    }
    
    @Override
    public DriverPropertyInfo[] getPropertyInfo(final String url, final Properties info) throws SQLException {
        Options options;
        if (url != null && !url.isEmpty()) {
            final UrlParser urlParser = UrlParser.parse(url, info);
            if (urlParser == null || urlParser.getOptions() == null) {
                return new DriverPropertyInfo[0];
            }
            options = urlParser.getOptions();
        }
        else {
            options = DefaultOptions.parse(HaMode.NONE, "", info, null);
        }
        final List<DriverPropertyInfo> props = new ArrayList<DriverPropertyInfo>();
        for (final DefaultOptions o : DefaultOptions.values()) {
            try {
                final Field field = Options.class.getField(o.getOptionName());
                final Object value = field.get(options);
                final DriverPropertyInfo propertyInfo = new DriverPropertyInfo(field.getName(), (value == null) ? null : value.toString());
                propertyInfo.description = o.getDescription();
                propertyInfo.required = o.isRequired();
                props.add(propertyInfo);
            }
            catch (NoSuchFieldException ex) {}
            catch (IllegalAccessException ex2) {}
        }
        return props.toArray(new DriverPropertyInfo[props.size()]);
    }
    
    @Override
    public int getMajorVersion() {
        return Version.majorVersion;
    }
    
    @Override
    public int getMinorVersion() {
        return Version.minorVersion;
    }
    
    @Override
    public boolean jdbcCompliant() {
        return true;
    }
    
    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Use logging parameters for enabling logging.");
    }
    
    static {
        try {
            DriverManager.registerDriver(new Driver(), new DeRegister());
        }
        catch (SQLException e) {
            throw new RuntimeException("Could not register driver", e);
        }
    }
}
