// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.util.Properties;
import java.io.IOException;
import java.net.SocketException;
import java.sql.DriverManager;
import java.net.Socket;

public class StandardSocketFactory implements SocketFactory, SocketMetadata
{
    public static final String TCP_NO_DELAY_PROPERTY_NAME = "tcpNoDelay";
    public static final String TCP_KEEP_ALIVE_DEFAULT_VALUE = "true";
    public static final String TCP_KEEP_ALIVE_PROPERTY_NAME = "tcpKeepAlive";
    public static final String TCP_RCV_BUF_PROPERTY_NAME = "tcpRcvBuf";
    public static final String TCP_SND_BUF_PROPERTY_NAME = "tcpSndBuf";
    public static final String TCP_TRAFFIC_CLASS_PROPERTY_NAME = "tcpTrafficClass";
    public static final String TCP_RCV_BUF_DEFAULT_VALUE = "0";
    public static final String TCP_SND_BUF_DEFAULT_VALUE = "0";
    public static final String TCP_TRAFFIC_CLASS_DEFAULT_VALUE = "0";
    public static final String TCP_NO_DELAY_DEFAULT_VALUE = "true";
    protected String host;
    protected int port;
    protected Socket rawSocket;
    protected int loginTimeoutCountdown;
    protected long loginTimeoutCheckTimestamp;
    protected int socketTimeoutBackup;
    
    public StandardSocketFactory() {
        this.host = null;
        this.port = 3306;
        this.rawSocket = null;
        this.loginTimeoutCountdown = DriverManager.getLoginTimeout() * 1000;
        this.loginTimeoutCheckTimestamp = System.currentTimeMillis();
        this.socketTimeoutBackup = 0;
    }
    
    @Override
    public Socket afterHandshake() throws SocketException, IOException {
        this.resetLoginTimeCountdown();
        this.rawSocket.setSoTimeout(this.socketTimeoutBackup);
        return this.rawSocket;
    }
    
    @Override
    public Socket beforeHandshake() throws SocketException, IOException {
        this.resetLoginTimeCountdown();
        this.socketTimeoutBackup = this.rawSocket.getSoTimeout();
        this.rawSocket.setSoTimeout(this.getRealTimeout(this.socketTimeoutBackup));
        return this.rawSocket;
    }
    
    protected Socket createSocket(final Properties props) {
        return new Socket();
    }
    
    private void configureSocket(final Socket sock, final Properties props) throws SocketException, IOException {
        sock.setTcpNoDelay(Boolean.valueOf(props.getProperty("tcpNoDelay", "true")));
        final String keepAlive = props.getProperty("tcpKeepAlive", "true");
        if (keepAlive != null && keepAlive.length() > 0) {
            sock.setKeepAlive(Boolean.valueOf(keepAlive));
        }
        final int receiveBufferSize = Integer.parseInt(props.getProperty("tcpRcvBuf", "0"));
        if (receiveBufferSize > 0) {
            sock.setReceiveBufferSize(receiveBufferSize);
        }
        final int sendBufferSize = Integer.parseInt(props.getProperty("tcpSndBuf", "0"));
        if (sendBufferSize > 0) {
            sock.setSendBufferSize(sendBufferSize);
        }
        final int trafficClass = Integer.parseInt(props.getProperty("tcpTrafficClass", "0"));
        if (trafficClass > 0) {
            sock.setTrafficClass(trafficClass);
        }
    }
    
    @Override
    public Socket connect(final String hostname, final int portNumber, final Properties props) throws SocketException, IOException {
        if (props != null) {
            this.host = hostname;
            this.port = portNumber;
            final String localSocketHostname = props.getProperty("localSocketAddress");
            InetSocketAddress localSockAddr = null;
            if (localSocketHostname != null && localSocketHostname.length() > 0) {
                localSockAddr = new InetSocketAddress(InetAddress.getByName(localSocketHostname), 0);
            }
            final String connectTimeoutStr = props.getProperty("connectTimeout");
            int connectTimeout = 0;
            if (connectTimeoutStr != null) {
                try {
                    connectTimeout = Integer.parseInt(connectTimeoutStr);
                }
                catch (NumberFormatException nfe) {
                    throw new SocketException("Illegal value '" + connectTimeoutStr + "' for connectTimeout");
                }
            }
            if (this.host != null) {
                final InetAddress[] possibleAddresses = InetAddress.getAllByName(this.host);
                if (possibleAddresses.length == 0) {
                    throw new SocketException("No addresses for host");
                }
                SocketException lastException = null;
                int i = 0;
                while (i < possibleAddresses.length) {
                    try {
                        this.configureSocket(this.rawSocket = this.createSocket(props), props);
                        final InetSocketAddress sockAddr = new InetSocketAddress(possibleAddresses[i], this.port);
                        if (localSockAddr != null) {
                            this.rawSocket.bind(localSockAddr);
                        }
                        this.rawSocket.connect(sockAddr, this.getRealTimeout(connectTimeout));
                    }
                    catch (SocketException ex) {
                        lastException = ex;
                        this.resetLoginTimeCountdown();
                        this.rawSocket = null;
                        ++i;
                        continue;
                    }
                    break;
                }
                if (this.rawSocket == null && lastException != null) {
                    throw lastException;
                }
                this.resetLoginTimeCountdown();
                return this.rawSocket;
            }
        }
        throw new SocketException("Unable to create socket");
    }
    
    @Override
    public boolean isLocallyConnected(final ConnectionImpl conn) throws SQLException {
        return Helper.isLocallyConnected(conn);
    }
    
    protected void resetLoginTimeCountdown() throws SocketException {
        if (this.loginTimeoutCountdown > 0) {
            final long now = System.currentTimeMillis();
            this.loginTimeoutCountdown -= (int)(now - this.loginTimeoutCheckTimestamp);
            if (this.loginTimeoutCountdown <= 0) {
                throw new SocketException(Messages.getString("Connection.LoginTimeout"));
            }
            this.loginTimeoutCheckTimestamp = now;
        }
    }
    
    protected int getRealTimeout(final int expectedTimeout) {
        if (this.loginTimeoutCountdown > 0 && (expectedTimeout == 0 || expectedTimeout > this.loginTimeoutCountdown)) {
            return this.loginTimeoutCountdown;
        }
        return expectedTimeout;
    }
}
