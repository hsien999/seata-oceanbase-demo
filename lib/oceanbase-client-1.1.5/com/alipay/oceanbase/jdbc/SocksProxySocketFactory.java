// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.util.Properties;

public class SocksProxySocketFactory extends StandardSocketFactory
{
    public static int SOCKS_DEFAULT_PORT;
    
    @Override
    protected Socket createSocket(final Properties props) {
        final String socksProxyHost = props.getProperty("socksProxyHost");
        final String socksProxyPortString = props.getProperty("socksProxyPort", String.valueOf(SocksProxySocketFactory.SOCKS_DEFAULT_PORT));
        int socksProxyPort = SocksProxySocketFactory.SOCKS_DEFAULT_PORT;
        try {
            socksProxyPort = Integer.valueOf(socksProxyPortString);
        }
        catch (NumberFormatException ex) {}
        return new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksProxyHost, socksProxyPort)));
    }
    
    static {
        SocksProxySocketFactory.SOCKS_DEFAULT_PORT = 1080;
    }
}
