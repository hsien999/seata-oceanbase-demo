// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import com.oceanbase.jdbc.util.Options;
import java.util.Collections;
import com.oceanbase.jdbc.util.DefaultOptions;
import com.oceanbase.jdbc.internal.util.constant.HaMode;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.XAConnection;
import javax.sql.PooledConnection;
import java.io.PrintWriter;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.XADataSource;
import javax.sql.ConnectionPoolDataSource;
import javax.sql.DataSource;

public class OceanBaseDataSource implements DataSource, ConnectionPoolDataSource, XADataSource
{
    private UrlParser urlParser;
    private String hostname;
    private Integer port;
    private Integer connectTimeoutInMs;
    private String database;
    private String url;
    private String user;
    private String password;
    private String properties;
    
    public OceanBaseDataSource(final String hostname, final int port, final String database) {
        this.port = null;
        this.hostname = hostname;
        this.port = port;
        this.database = database;
    }
    
    public OceanBaseDataSource(final String url) {
        this.port = null;
        this.url = url;
    }
    
    public OceanBaseDataSource() {
        this.port = null;
    }
    
    public String getDatabaseName() {
        if (this.database != null) {
            return this.database;
        }
        return (this.urlParser != null && this.urlParser.getDatabase() != null) ? this.urlParser.getDatabase() : "";
    }
    
    public void setDatabaseName(final String database) throws SQLException {
        this.database = database;
        this.reInitializeIfNeeded();
    }
    
    public String getUser() {
        if (this.user != null) {
            return this.user;
        }
        return (this.urlParser != null) ? this.urlParser.getUsername() : null;
    }
    
    public void setUser(final String user) throws SQLException {
        this.user = user;
        this.reInitializeIfNeeded();
    }
    
    public String getUserName() {
        return this.getUser();
    }
    
    public void setUserName(final String userName) throws SQLException {
        this.setUser(userName);
    }
    
    public void setPassword(final String password) throws SQLException {
        this.password = password;
        this.reInitializeIfNeeded();
    }
    
    public int getPort() {
        if (this.port != 0) {
            return this.port;
        }
        return (this.urlParser != null) ? this.urlParser.getHostAddresses().get(0).port : 3306;
    }
    
    public void setPort(final int port) throws SQLException {
        this.port = port;
        this.reInitializeIfNeeded();
    }
    
    public int getPortNumber() {
        return this.getPort();
    }
    
    public void setPortNumber(final int port) throws SQLException {
        if (port > 0) {
            this.setPort(port);
        }
    }
    
    @Deprecated
    public void setProperties(final String properties) throws SQLException {
        this.properties = properties;
        this.reInitializeIfNeeded();
    }
    
    public void setUrl(final String url) throws SQLException {
        this.url = url;
        this.reInitializeIfNeeded();
    }
    
    public String getServerName() {
        if (this.hostname != null) {
            return this.hostname;
        }
        final boolean hasHost = this.urlParser != null && this.urlParser.getHostAddresses().get(0).host != null;
        return hasHost ? this.urlParser.getHostAddresses().get(0).host : "localhost";
    }
    
    public void setServerName(final String serverName) throws SQLException {
        this.hostname = serverName;
        this.reInitializeIfNeeded();
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        try {
            if (this.urlParser == null) {
                this.initialize();
            }
            return OceanBaseConnection.newConnection(this.urlParser, null);
        }
        catch (SQLException e) {
            throw ExceptionFactory.INSTANCE.create(e);
        }
    }
    
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        try {
            if (this.urlParser == null) {
                this.user = username;
                this.password = password;
                this.initialize();
            }
            final UrlParser urlParser = (UrlParser)this.urlParser.clone();
            urlParser.setUsername(username);
            urlParser.setPassword(password);
            return OceanBaseConnection.newConnection(urlParser, null);
        }
        catch (SQLException e) {
            throw ExceptionFactory.INSTANCE.create(e);
        }
        catch (CloneNotSupportedException cloneException) {
            throw ExceptionFactory.INSTANCE.create("Error in configuration");
        }
    }
    
    @Override
    public PrintWriter getLogWriter() {
        return null;
    }
    
    @Override
    public void setLogWriter(final PrintWriter out) {
    }
    
    @Override
    public int getLoginTimeout() {
        if (this.connectTimeoutInMs != null) {
            return this.connectTimeoutInMs / 1000;
        }
        return (this.urlParser != null) ? (this.urlParser.getOptions().connectTimeout / 1000) : 30;
    }
    
    @Override
    public void setLoginTimeout(final int seconds) {
        this.connectTimeoutInMs = seconds * 1000;
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        try {
            if (this.isWrapperFor(iface)) {
                return iface.cast(this);
            }
            throw new SQLException("The receiver is not a wrapper and does not implement the interface");
        }
        catch (Exception e) {
            throw new SQLException("The receiver is not a wrapper and does not implement the interface");
        }
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> interfaceOrWrapper) throws SQLException {
        return interfaceOrWrapper.isInstance(this);
    }
    
    @Override
    public PooledConnection getPooledConnection() throws SQLException {
        return new OceanBasePooledConnection((OceanBaseConnection)this.getConnection());
    }
    
    @Override
    public PooledConnection getPooledConnection(final String user, final String password) throws SQLException {
        return new OceanBasePooledConnection((OceanBaseConnection)this.getConnection(user, password));
    }
    
    @Override
    public XAConnection getXAConnection() throws SQLException {
        return new OceanBaseXaConnection((OceanBaseConnection)this.getConnection());
    }
    
    @Override
    public XAConnection getXAConnection(final String user, final String password) throws SQLException {
        return new OceanBaseXaConnection((OceanBaseConnection)this.getConnection(user, password));
    }
    
    @Override
    public Logger getParentLogger() {
        return null;
    }
    
    protected UrlParser getUrlParser() {
        return this.urlParser;
    }
    
    private void reInitializeIfNeeded() throws SQLException {
        if (this.urlParser != null) {
            this.initialize();
        }
    }
    
    protected synchronized void initialize() throws SQLException {
        if (this.url != null && !this.url.isEmpty()) {
            final Properties props = new Properties();
            if (this.user != null) {
                props.setProperty("user", this.user);
            }
            if (this.password != null) {
                props.setProperty("password", this.password);
            }
            if (this.database != null) {
                props.setProperty("database", this.database);
            }
            if (this.connectTimeoutInMs != null) {
                props.setProperty("connectTimeout", String.valueOf(this.connectTimeoutInMs));
            }
            if (this.port != null) {
                props.setProperty("port", String.valueOf(this.port));
            }
            this.urlParser = UrlParser.parse(this.url, props);
        }
        else {
            final Options options = DefaultOptions.defaultValues(HaMode.NONE);
            options.user = this.user;
            options.password = this.password;
            this.urlParser = new UrlParser(this.database, Collections.singletonList(new HostAddress((this.hostname == null || this.hostname.isEmpty()) ? "localhost" : this.hostname, (this.port == null) ? 3306 : this.port)), options, HaMode.NONE);
            if (this.properties != null) {
                this.urlParser.setProperties(this.properties);
            }
            if (this.connectTimeoutInMs != null) {
                this.urlParser.getOptions().connectTimeout = this.connectTimeoutInMs;
            }
        }
    }
    
    public void setURL(final String url) throws SQLException {
        this.setUrl(url);
    }
    
    public String getURL() {
        return this.url;
    }
}
