// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.authentication;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import com.alipay.oceanbase.jdbc.ExceptionInterceptor;
import com.alipay.oceanbase.jdbc.SQLError;
import com.alipay.oceanbase.jdbc.Messages;
import com.alipay.oceanbase.jdbc.Security;
import java.util.List;
import com.alipay.oceanbase.jdbc.Buffer;
import java.sql.SQLException;
import java.util.Properties;
import com.alipay.oceanbase.jdbc.Connection;
import com.alipay.oceanbase.jdbc.AuthenticationPlugin;

public class MysqlNativePasswordPlugin implements AuthenticationPlugin
{
    private Connection connection;
    private String password;
    
    public MysqlNativePasswordPlugin() {
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
        return "mysql_native_password";
    }
    
    @Override
    public boolean requiresConfidentiality() {
        return false;
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
        try {
            toServer.clear();
            Buffer bresp = null;
            final String pwd = this.password;
            if (fromServer == null || pwd == null || pwd.length() == 0) {
                bresp = new Buffer(new byte[0]);
            }
            else {
                bresp = new Buffer(Security.scramble411(pwd, fromServer.readString(), this.connection.getPasswordCharacterEncoding()));
            }
            toServer.add(bresp);
        }
        catch (NoSuchAlgorithmException nse) {
            throw SQLError.createSQLException(Messages.getString("MysqlIO.91") + Messages.getString("MysqlIO.92"), "S1000", null);
        }
        catch (UnsupportedEncodingException e) {
            throw SQLError.createSQLException(Messages.getString("MysqlNativePasswordPlugin.1", new Object[] { this.connection.getPasswordCharacterEncoding() }), "S1000", null);
        }
        return true;
    }
}
