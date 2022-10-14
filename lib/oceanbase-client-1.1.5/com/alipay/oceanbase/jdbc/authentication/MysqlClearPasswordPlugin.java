// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.authentication;

import java.io.UnsupportedEncodingException;
import com.alipay.oceanbase.jdbc.ExceptionInterceptor;
import com.alipay.oceanbase.jdbc.SQLError;
import com.alipay.oceanbase.jdbc.Messages;
import com.alipay.oceanbase.jdbc.StringUtils;
import java.util.List;
import com.alipay.oceanbase.jdbc.Buffer;
import java.sql.SQLException;
import java.util.Properties;
import com.alipay.oceanbase.jdbc.Connection;
import com.alipay.oceanbase.jdbc.AuthenticationPlugin;

public class MysqlClearPasswordPlugin implements AuthenticationPlugin
{
    private Connection connection;
    private String password;
    
    public MysqlClearPasswordPlugin() {
        this.password = null;
    }
    
    @Override
    public void init(final Connection conn, final Properties props) throws SQLException {
        this.connection = conn;
    }
    
    @Override
    public void destroy() {
        this.password = null;
    }
    
    @Override
    public String getProtocolPluginName() {
        return "mysql_clear_password";
    }
    
    @Override
    public boolean requiresConfidentiality() {
        return true;
    }
    
    @Override
    public boolean isReusable() {
        return true;
    }
    
    @Override
    public void setAuthenticationParameters(final String user, final String password) {
        this.password = password;
    }
    
    @Override
    public boolean nextAuthenticationStep(final Buffer fromServer, final List<Buffer> toServer) throws SQLException {
        toServer.clear();
        Buffer bresp;
        try {
            final String encoding = this.connection.versionMeetsMinimum(5, 7, 6) ? this.connection.getPasswordCharacterEncoding() : "UTF-8";
            bresp = new Buffer(StringUtils.getBytes((this.password != null) ? this.password : "", encoding));
        }
        catch (UnsupportedEncodingException e) {
            throw SQLError.createSQLException(Messages.getString("MysqlClearPasswordPlugin.1", new Object[] { this.connection.getPasswordCharacterEncoding() }), "S1000", null);
        }
        bresp.setPosition(bresp.getBufLength());
        final int oldBufLength = bresp.getBufLength();
        bresp.writeByte((byte)0);
        bresp.setBufLength(oldBufLength + 1);
        bresp.setPosition(0);
        toServer.add(bresp);
        return true;
    }
}
