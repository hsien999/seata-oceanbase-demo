// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.authentication;

import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import com.alipay.oceanbase.jdbc.ExportControlled;
import com.alipay.oceanbase.jdbc.Security;
import java.io.UnsupportedEncodingException;
import com.alipay.oceanbase.jdbc.ExceptionInterceptor;
import com.alipay.oceanbase.jdbc.SQLError;
import com.alipay.oceanbase.jdbc.Messages;
import com.alipay.oceanbase.jdbc.StringUtils;
import com.alipay.oceanbase.jdbc.MySQLConnection;
import java.util.List;
import com.alipay.oceanbase.jdbc.Buffer;
import java.sql.SQLException;
import java.util.Properties;
import com.alipay.oceanbase.jdbc.Connection;
import com.alipay.oceanbase.jdbc.AuthenticationPlugin;

public class Sha256PasswordPlugin implements AuthenticationPlugin
{
    public static String PLUGIN_NAME;
    private Connection connection;
    private String password;
    private String seed;
    private boolean publicKeyRequested;
    private String publicKeyString;
    
    public Sha256PasswordPlugin() {
        this.password = null;
        this.seed = null;
        this.publicKeyRequested = false;
        this.publicKeyString = null;
    }
    
    @Override
    public void init(final Connection conn, final Properties props) throws SQLException {
        this.connection = conn;
        final String pkURL = this.connection.getServerRSAPublicKeyFile();
        if (pkURL != null) {
            this.publicKeyString = readRSAKey(this.connection, pkURL);
        }
    }
    
    @Override
    public void destroy() {
        this.password = null;
        this.seed = null;
        this.publicKeyRequested = false;
    }
    
    @Override
    public String getProtocolPluginName() {
        return Sha256PasswordPlugin.PLUGIN_NAME;
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
        if (this.password == null || this.password.length() == 0 || fromServer == null) {
            final Buffer bresp = new Buffer(new byte[] { 0 });
            toServer.add(bresp);
        }
        else if (((MySQLConnection)this.connection).getIO().isSSLEstablished()) {
            Buffer bresp;
            try {
                bresp = new Buffer(StringUtils.getBytes(this.password, this.connection.getPasswordCharacterEncoding()));
            }
            catch (UnsupportedEncodingException e) {
                throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.3", new Object[] { this.connection.getPasswordCharacterEncoding() }), "S1000", null);
            }
            bresp.setPosition(bresp.getBufLength());
            final int oldBufLength = bresp.getBufLength();
            bresp.writeByte((byte)0);
            bresp.setBufLength(oldBufLength + 1);
            bresp.setPosition(0);
            toServer.add(bresp);
        }
        else if (this.connection.getServerRSAPublicKeyFile() != null) {
            this.seed = fromServer.readString();
            final Buffer bresp = new Buffer(encryptPassword(this.password, this.seed, this.connection, this.publicKeyString));
            toServer.add(bresp);
        }
        else {
            if (!this.connection.getAllowPublicKeyRetrieval()) {
                throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.2"), "08001", this.connection.getExceptionInterceptor());
            }
            if (this.publicKeyRequested && fromServer.getBufLength() > 20) {
                final Buffer bresp = new Buffer(encryptPassword(this.password, this.seed, this.connection, fromServer.readString()));
                toServer.add(bresp);
                this.publicKeyRequested = false;
            }
            else {
                this.seed = fromServer.readString();
                final Buffer bresp = new Buffer(new byte[] { 1 });
                toServer.add(bresp);
                this.publicKeyRequested = true;
            }
        }
        return true;
    }
    
    private static byte[] encryptPassword(final String password, final String seed, final Connection connection, final String key) throws SQLException {
        byte[] input = null;
        try {
            input = ((password != null) ? StringUtils.getBytesNullTerminated(password, connection.getPasswordCharacterEncoding()) : new byte[] { 0 });
        }
        catch (UnsupportedEncodingException e) {
            throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.3", new Object[] { connection.getPasswordCharacterEncoding() }), "S1000", null);
        }
        final byte[] mysqlScrambleBuff = new byte[input.length];
        Security.xorString(input, mysqlScrambleBuff, seed.getBytes(), input.length);
        return ExportControlled.encryptWithRSAPublicKey(mysqlScrambleBuff, ExportControlled.decodeRSAPublicKey(key, ((MySQLConnection)connection).getExceptionInterceptor()), ((MySQLConnection)connection).getExceptionInterceptor());
    }
    
    private static String readRSAKey(final Connection connection, final String pkPath) throws SQLException {
        String res = null;
        final byte[] fileBuf = new byte[2048];
        BufferedInputStream fileIn = null;
        try {
            final File f = new File(pkPath);
            final String canonicalPath = f.getCanonicalPath();
            fileIn = new BufferedInputStream(new FileInputStream(canonicalPath));
            int bytesRead = 0;
            final StringBuilder sb = new StringBuilder();
            while ((bytesRead = fileIn.read(fileBuf)) != -1) {
                sb.append(StringUtils.toAsciiString(fileBuf, 0, bytesRead));
            }
            res = sb.toString();
        }
        catch (IOException ioEx) {
            if (connection.getParanoid()) {
                throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.0", new Object[] { "" }), "S1009", connection.getExceptionInterceptor());
            }
            throw SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.0", new Object[] { "'" + pkPath + "'" }), "S1009", ioEx, connection.getExceptionInterceptor());
        }
        finally {
            if (fileIn != null) {
                try {
                    fileIn.close();
                }
                catch (Exception ex) {
                    final SQLException sqlEx = SQLError.createSQLException(Messages.getString("Sha256PasswordPlugin.1"), "S1000", ex, connection.getExceptionInterceptor());
                    throw sqlEx;
                }
            }
        }
        return res;
    }
    
    static {
        Sha256PasswordPlugin.PLUGIN_NAME = "sha256_password";
    }
}
