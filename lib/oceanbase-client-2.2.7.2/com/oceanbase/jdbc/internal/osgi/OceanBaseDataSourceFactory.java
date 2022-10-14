// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.osgi;

import com.oceanbase.jdbc.OceanBasePoolDataSource;
import com.oceanbase.jdbc.OceanBaseDataSource;
import com.oceanbase.jdbc.Driver;
import javax.sql.XADataSource;
import javax.sql.ConnectionPoolDataSource;
import java.sql.SQLException;
import javax.sql.DataSource;
import java.util.Properties;
import org.osgi.service.jdbc.DataSourceFactory;

public class OceanBaseDataSourceFactory implements DataSourceFactory
{
    public DataSource createDataSource(final Properties props) throws SQLException {
        if (props != null && (props.containsKey("minPoolSize") || props.containsKey("maxPoolSize") || props.containsKey("maxIdleTime"))) {
            return this.createPoolDataSource(props);
        }
        return this.createBasicDataSource(props);
    }
    
    public ConnectionPoolDataSource createConnectionPoolDataSource(final Properties props) throws SQLException {
        if (props != null && (props.containsKey("minPoolSize") || props.containsKey("maxPoolSize") || props.containsKey("maxIdleTime"))) {
            return this.createPoolDataSource(props);
        }
        return this.createBasicDataSource(props);
    }
    
    public XADataSource createXADataSource(final Properties props) throws SQLException {
        if (props != null && (props.containsKey("minPoolSize") || props.containsKey("maxPoolSize") || props.containsKey("maxIdleTime"))) {
            return this.createPoolDataSource(props);
        }
        return this.createBasicDataSource(props);
    }
    
    public Driver createDriver(final Properties props) throws SQLException {
        return new Driver();
    }
    
    private OceanBaseDataSource createBasicDataSource(final Properties props) throws SQLException {
        final OceanBaseDataSource dataSource = new OceanBaseDataSource();
        if (props.containsKey("url")) {
            dataSource.setUrl(props.getProperty("url"));
        }
        if (props.containsKey("serverName")) {
            dataSource.setServerName(props.getProperty("serverName"));
        }
        if (props.containsKey("portNumber")) {
            try {
                dataSource.setPortNumber(Integer.parseInt(props.getProperty("portNumber")));
            }
            catch (NumberFormatException nfe) {
                throw new SQLException("Port format must be integer, but value is '" + props.getProperty("portNumber") + "'");
            }
        }
        if (props.containsKey("user")) {
            dataSource.setUser(props.getProperty("user"));
        }
        if (props.containsKey("password")) {
            dataSource.setPassword(props.getProperty("password"));
        }
        if (props.containsKey("databaseName")) {
            dataSource.setDatabaseName(props.getProperty("databaseName"));
        }
        return dataSource;
    }
    
    private OceanBasePoolDataSource createPoolDataSource(final Properties props) throws SQLException {
        final OceanBasePoolDataSource dataSource = new OceanBasePoolDataSource();
        if (props.containsKey("url")) {
            dataSource.setUrl(props.getProperty("url"));
        }
        if (props.containsKey("serverName")) {
            dataSource.setServerName(props.getProperty("serverName"));
        }
        if (props.containsKey("portNumber")) {
            try {
                dataSource.setPortNumber(Integer.parseInt(props.getProperty("portNumber")));
            }
            catch (NumberFormatException nfe) {
                throw new SQLException("Port number format must be integer, but value is '" + props.getProperty("portNumber") + "'");
            }
        }
        if (props.containsKey("user")) {
            dataSource.setUser(props.getProperty("user"));
        }
        if (props.containsKey("password")) {
            dataSource.setPassword(props.getProperty("password"));
        }
        if (props.containsKey("databaseName")) {
            dataSource.setDatabaseName(props.getProperty("databaseName"));
        }
        if (props.containsKey("maxIdleTime")) {
            try {
                dataSource.setMaxIdleTime(Integer.parseInt(props.getProperty("maxIdleTime")));
            }
            catch (NumberFormatException nfe) {
                throw new SQLException("Max idle time format must be integer, but value is '" + props.getProperty("maxIdleTime") + "'");
            }
        }
        if (props.containsKey("maxPoolSize")) {
            try {
                dataSource.setMaxPoolSize(Integer.parseInt(props.getProperty("maxPoolSize")));
            }
            catch (NumberFormatException nfe) {
                throw new SQLException("Max pool size format must be integer, but value is '" + props.getProperty("maxPoolSize") + "'");
            }
        }
        if (props.containsKey("minPoolSize")) {
            try {
                dataSource.setMinPoolSize(Integer.parseInt(props.getProperty("minPoolSize")));
            }
            catch (NumberFormatException nfe) {
                throw new SQLException("Min pool size format must be integer, but value is '" + props.getProperty("minPoolSize") + "'");
            }
        }
        return dataSource;
    }
}
