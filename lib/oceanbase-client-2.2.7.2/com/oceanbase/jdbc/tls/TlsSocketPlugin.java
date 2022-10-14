// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.tls;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import javax.net.ssl.SSLSocket;
import java.net.Socket;
import java.sql.SQLException;
import javax.net.ssl.SSLSocketFactory;
import com.oceanbase.jdbc.util.Options;

public interface TlsSocketPlugin
{
    String name();
    
    String type();
    
    SSLSocketFactory getSocketFactory(final Options p0) throws SQLException;
    
    default SSLSocket createSocket(final Socket socket, final SSLSocketFactory sslSocketFactory) throws IOException {
        return (SSLSocket)sslSocketFactory.createSocket(socket, (socket.getInetAddress() == null) ? null : socket.getInetAddress().getHostAddress(), socket.getPort(), true);
    }
    
    void verify(final String p0, final SSLSession p1, final Options p2, final long p3) throws SSLException;
}
