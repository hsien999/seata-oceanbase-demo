// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.util.List;
import com.oceanbase.jdbc.internal.util.pool.Pools;
import com.oceanbase.jdbc.util.Options;
import java.util.Collections;
import com.oceanbase.jdbc.util.DefaultOptions;
import com.oceanbase.jdbc.internal.util.constant.HaMode;
import java.util.Properties;
import java.util.logging.Logger;
import javax.sql.XAConnection;
import java.io.PrintWriter;
import javax.sql.PooledConnection;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import java.sql.Connection;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.pool.Pool;
import java.io.Closeable;
import javax.sql.XADataSource;
import javax.sql.DataSource;
import javax.sql.ConnectionPoolDataSource;

public class OceanBasePoolDataSource implements ConnectionPoolDataSource, DataSource, XADataSource, Closeable, AutoCloseable
{
    private UrlParser urlParser;
    private Pool pool;
    private String hostname;
    private Integer port;
    private Integer connectTimeout;
    private String database;
    private String url;
    private String user;
    private String password;
    private String poolName;
    private Integer maxPoolSize;
    private Integer minPoolSize;
    private Integer maxIdleTime;
    private Boolean staticGlobal;
    private Integer poolValidMinDelay;
    
    public OceanBasePoolDataSource(final String hostname, final int port, final String database) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
    }
    
    public OceanBasePoolDataSource(final String url) {
        this.url = url;
    }
    
    public OceanBasePoolDataSource() {
    }
    
    public String getDatabaseName() {
        if (this.database != null) {
            return this.database;
        }
        return (this.urlParser != null && this.urlParser.getDatabase() != null) ? this.urlParser.getDatabase() : "";
    }
    
    public void setDatabaseName(final String database) throws SQLException {
        this.checkNotInitialized();
        this.database = database;
    }
    
    private void checkNotInitialized() throws SQLException {
        if (this.pool != null) {
            throw new SQLException("can not perform a configuration change once initialized");
        }
    }
    
    public String getUser() {
        if (this.user != null) {
            return this.user;
        }
        return (this.urlParser != null) ? this.urlParser.getUsername() : null;
    }
    
    public void setUser(final String user) throws SQLException {
        this.checkNotInitialized();
        this.user = user;
    }
    
    public void setPassword(final String password) throws SQLException {
        this.checkNotInitialized();
        this.password = password;
    }
    
    public int getPort() {
        if (this.port != null && this.port != 0) {
            return this.port;
        }
        return (this.urlParser != null) ? this.urlParser.getHostAddresses().get(0).port : 3306;
    }
    
    public void setPort(final int port) throws SQLException {
        this.checkNotInitialized();
        this.port = port;
    }
    
    public int getPortNumber() {
        return this.getPort();
    }
    
    public void setPortNumber(final int port) throws SQLException {
        this.checkNotInitialized();
        if (port > 0) {
            this.setPort(port);
        }
    }
    
    public void setUrl(final String url) throws SQLException {
        this.checkNotInitialized();
        this.url = url;
    }
    
    public String getServerName() {
        if (this.hostname != null) {
            return this.hostname;
        }
        final boolean hasHost = this.urlParser != null && this.urlParser.getHostAddresses().get(0).host != null;
        return hasHost ? this.urlParser.getHostAddresses().get(0).host : "localhost";
    }
    
    public void setServerName(final String serverName) throws SQLException {
        this.checkNotInitialized();
        this.hostname = serverName;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        try {
            if (this.pool == null) {
                this.initialize();
            }
            return this.pool.getConnection();
        }
        catch (SQLException e) {
            throw ExceptionFactory.INSTANCE.create(e);
        }
    }
    
    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        try {
            if (this.pool == null) {
                this.user = username;
                this.password = password;
                this.initialize();
                return this.pool.getConnection();
            }
            Label_0106: {
                if (this.urlParser.getUsername() != null) {
                    if (!this.urlParser.getUsername().equals(username)) {
                        break Label_0106;
                    }
                }
                else if (username != null) {
                    break Label_0106;
                }
                if (this.urlParser.getPassword() != null) {
                    if (!this.urlParser.getPassword().equals(password)) {
                        break Label_0106;
                    }
                }
                else if (password != null && !password.isEmpty()) {
                    break Label_0106;
                }
                return this.pool.getConnection();
            }
            final UrlParser urlParser = (UrlParser)this.urlParser.clone();
            urlParser.setUsername(username);
            urlParser.setPassword(password);
            return OceanBaseConnection.newConnection(urlParser, this.pool.getGlobalInfo());
        }
        catch (SQLException e) {
            throw ExceptionFactory.INSTANCE.create(e);
        }
        catch (CloneNotSupportedException cloneException) {
            throw new SQLException("Error in configuration");
        }
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
    public PrintWriter getLogWriter() {
        return null;
    }
    
    @Override
    public void setLogWriter(final PrintWriter out) {
    }
    
    @Override
    public int getLoginTimeout() {
        if (this.connectTimeout != null) {
            return this.connectTimeout / 1000;
        }
        return (this.urlParser != null) ? (this.urlParser.getOptions().connectTimeout / 1000) : 0;
    }
    
    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        this.checkNotInitialized();
        this.connectTimeout = seconds * 1000;
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
    
    public String getPoolName() {
        return (this.pool != null) ? this.pool.getPoolTag() : this.poolName;
    }
    
    public void setPoolName(final String poolName) throws SQLException {
        this.checkNotInitialized();
        this.poolName = poolName;
    }
    
    public int getMaxPoolSize() {
        if (this.maxPoolSize == null) {
            return 8;
        }
        return this.maxPoolSize;
    }
    
    public void setMaxPoolSize(final int maxPoolSize) throws SQLException {
        this.checkNotInitialized();
        this.maxPoolSize = maxPoolSize;
    }
    
    public int getMinPoolSize() {
        if (this.minPoolSize == null) {
            return this.getMaxPoolSize();
        }
        return this.minPoolSize;
    }
    
    public void setMinPoolSize(final int minPoolSize) throws SQLException {
        this.checkNotInitialized();
        this.minPoolSize = minPoolSize;
    }
    
    public int getMaxIdleTime() {
        if (this.maxIdleTime == null) {
            return 600;
        }
        return this.maxIdleTime;
    }
    
    public void setMaxIdleTime(final int maxIdleTime) throws SQLException {
        this.checkNotInitialized();
        this.maxIdleTime = maxIdleTime;
    }
    
    public Boolean getStaticGlobal() {
        return this.staticGlobal;
    }
    
    public void setStaticGlobal(final Boolean staticGlobal) {
        this.staticGlobal = staticGlobal;
    }
    
    public Integer getPoolValidMinDelay() {
        if (this.poolValidMinDelay == null) {
            return 1000;
        }
        return this.poolValidMinDelay;
    }
    
    public void setPoolValidMinDelay(final Integer poolValidMinDelay) {
        this.poolValidMinDelay = poolValidMinDelay;
    }
    
    private synchronized void initializeUrlParser() throws SQLException {
        if (this.url != null && !this.url.isEmpty()) {
            final Properties props = new Properties();
            props.setProperty("pool", "true");
            if (this.user != null) {
                props.setProperty("user", this.user);
            }
            if (this.password != null) {
                props.setProperty("password", this.password);
            }
            if (this.poolName != null) {
                props.setProperty("poolName", this.poolName);
            }
            if (this.database != null) {
                props.setProperty("database", this.database);
            }
            if (this.maxPoolSize != null) {
                props.setProperty("maxPoolSize", String.valueOf(this.maxPoolSize));
            }
            if (this.minPoolSize != null) {
                props.setProperty("minPoolSize", String.valueOf(this.minPoolSize));
            }
            if (this.maxIdleTime != null) {
                props.setProperty("maxIdleTime", String.valueOf(this.maxIdleTime));
            }
            if (this.connectTimeout != null) {
                props.setProperty("connectTimeout", String.valueOf(this.connectTimeout));
            }
            if (this.staticGlobal != null) {
                props.setProperty("staticGlobal", String.valueOf(this.staticGlobal));
            }
            if (this.poolValidMinDelay != null) {
                props.setProperty("poolValidMinDelay", String.valueOf(this.poolValidMinDelay));
            }
            this.urlParser = UrlParser.parse(this.url, props);
        }
        else {
            final Options options = DefaultOptions.defaultValues(HaMode.NONE);
            options.pool = true;
            options.user = this.user;
            options.password = this.password;
            options.poolName = this.poolName;
            if (this.maxPoolSize != null) {
                options.maxPoolSize = this.maxPoolSize;
            }
            if (this.minPoolSize != null) {
                options.minPoolSize = this.minPoolSize;
            }
            if (this.maxIdleTime != null) {
                options.maxIdleTime = this.maxIdleTime;
            }
            if (this.staticGlobal != null) {
                options.staticGlobal = this.staticGlobal;
            }
            if (this.connectTimeout != null) {
                options.connectTimeout = this.connectTimeout;
            }
            if (this.poolValidMinDelay != null) {
                options.poolValidMinDelay = this.poolValidMinDelay;
            }
            this.urlParser = new UrlParser(this.database, Collections.singletonList(new HostAddress((this.hostname == null || this.hostname.isEmpty()) ? "localhost" : this.hostname, (this.port == null) ? 3306 : this.port)), options, HaMode.NONE);
        }
    }
    
    @Override
    public void close() {
        try {
            if (this.pool != null) {
                this.pool.close();
            }
        }
        catch (InterruptedException ex) {}
    }
    
    public synchronized void initialize() throws SQLException {
        if (this.pool == null) {
            this.initializeUrlParser();
            this.pool = Pools.retrievePool(this.urlParser);
        }
    }
    
    public List<Long> testGetConnectionIdleThreadIds() {
        return this.pool.testGetConnectionIdleThreadIds();
    }
    
    public void testForceMaxIdleTime(final int maxIdleTime) throws SQLException {
        this.initializeUrlParser();
        this.urlParser.getOptions().maxIdleTime = maxIdleTime;
        this.pool = Pools.retrievePool(this.urlParser);
    }
    
    public Pool testGetPool() {
        return this.pool;
    }
}
