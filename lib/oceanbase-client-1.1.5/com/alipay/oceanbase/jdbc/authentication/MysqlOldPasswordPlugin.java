// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.authentication;

import com.alipay.oceanbase.jdbc.StringUtils;
import com.alipay.oceanbase.jdbc.Util;
import java.util.List;
import com.alipay.oceanbase.jdbc.Buffer;
import java.sql.SQLException;
import java.util.Properties;
import com.alipay.oceanbase.jdbc.Connection;
import com.alipay.oceanbase.jdbc.AuthenticationPlugin;

public class MysqlOldPasswordPlugin implements AuthenticationPlugin
{
    private Connection connection;
    private String password;
    
    public MysqlOldPasswordPlugin() {
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
        return "mysql_old_password";
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
        toServer.clear();
        Buffer bresp = null;
        final String pwd = this.password;
        if (fromServer == null || pwd == null || pwd.length() == 0) {
            bresp = new Buffer(new byte[0]);
        }
        else {
            bresp = new Buffer(StringUtils.getBytes(Util.newCrypt(pwd, fromServer.readString().substring(0, 8), this.connection.getPasswordCharacterEncoding())));
            bresp.setPosition(bresp.getBufLength());
            final int oldBufLength = bresp.getBufLength();
            bresp.writeByte((byte)0);
            bresp.setBufLength(oldBufLength + 1);
            bresp.setPosition(0);
        }
        toServer.add(bresp);
        return true;
    }
}
