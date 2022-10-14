// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.net.SocketAddress;
import java.sql.ResultSet;
import java.sql.Statement;
import java.net.UnknownHostException;
import java.net.InetSocketAddress;
import java.net.InetAddress;
import java.sql.SQLException;

public interface SocketMetadata
{
    boolean isLocallyConnected(final ConnectionImpl p0) throws SQLException;
    
    public static class Helper
    {
        public static final String IS_LOCAL_HOSTNAME_REPLACEMENT_PROPERTY_NAME = "com.alipay.oceanbase.jdbc.test.isLocalHostnameReplacement";
        
        public static boolean isLocallyConnected(final ConnectionImpl conn) throws SQLException {
            long threadId = conn.getId();
            final Statement processListStmt = conn.getMetadataSafeStatement();
            ResultSet rs = null;
            String processHost = null;
            if (System.getProperty("com.alipay.oceanbase.jdbc.test.isLocalHostnameReplacement") != null) {
                processHost = System.getProperty("com.alipay.oceanbase.jdbc.test.isLocalHostnameReplacement");
            }
            else if (conn.getProperties().getProperty("com.alipay.oceanbase.jdbc.test.isLocalHostnameReplacement") != null) {
                processHost = conn.getProperties().getProperty("com.alipay.oceanbase.jdbc.test.isLocalHostnameReplacement");
            }
            else {
                try {
                    processHost = findProcessHost(threadId, processListStmt);
                    if (processHost == null) {
                        conn.getLog().logWarn(String.format("Connection id %d not found in \"SHOW PROCESSLIST\", assuming 32-bit overflow, using SELECT CONNECTION_ID() instead", threadId));
                        rs = processListStmt.executeQuery("SELECT CONNECTION_ID()");
                        if (rs.next()) {
                            threadId = rs.getLong(1);
                            processHost = findProcessHost(threadId, processListStmt);
                        }
                        else {
                            conn.getLog().logError("No rows returned for statement \"SELECT CONNECTION_ID()\", local connection check will most likely be incorrect");
                        }
                    }
                }
                finally {
                    processListStmt.close();
                }
            }
            if (processHost != null) {
                conn.getLog().logDebug(String.format("Using 'host' value of '%s' to determine locality of connection", processHost));
                final int endIndex = processHost.lastIndexOf(":");
                if (endIndex != -1) {
                    processHost = processHost.substring(0, endIndex);
                    try {
                        boolean isLocal = false;
                        final InetAddress[] allHostAddr = InetAddress.getAllByName(processHost);
                        final SocketAddress remoteSocketAddr = conn.getIO().mysqlConnection.getRemoteSocketAddress();
                        if (remoteSocketAddr instanceof InetSocketAddress) {
                            final InetAddress whereIConnectedTo = ((InetSocketAddress)remoteSocketAddr).getAddress();
                            for (final InetAddress hostAddr : allHostAddr) {
                                if (hostAddr.equals(whereIConnectedTo)) {
                                    conn.getLog().logDebug(String.format("Locally connected - HostAddress(%s).equals(whereIconnectedTo({%s})", hostAddr, whereIConnectedTo));
                                    isLocal = true;
                                    break;
                                }
                                conn.getLog().logDebug(String.format("Attempted locally connected check failed - ! HostAddress(%s).equals(whereIconnectedTo(%s)", hostAddr, whereIConnectedTo));
                            }
                        }
                        else {
                            final String msg = String.format("Remote socket address %s is not an inet socket address", remoteSocketAddr);
                            conn.getLog().logDebug(msg);
                        }
                        return isLocal;
                    }
                    catch (UnknownHostException e) {
                        conn.getLog().logWarn(Messages.getString("Connection.CantDetectLocalConnect", new Object[] { processHost }), e);
                        return false;
                    }
                }
                conn.getLog().logWarn(String.format("No port number present in 'host' from SHOW PROCESSLIST '%s', unable to determine whether locally connected", processHost));
                return false;
            }
            conn.getLog().logWarn(String.format("Cannot find process listing for connection %d in SHOW PROCESSLIST output, unable to determine if locally connected", threadId));
            return false;
        }
        
        private static String findProcessHost(final long threadId, final Statement processListStmt) throws SQLException {
            String processHost = null;
            final ResultSet rs = processListStmt.executeQuery("SHOW PROCESSLIST");
            while (rs.next()) {
                final long id = rs.getLong(1);
                if (threadId == id) {
                    processHost = rs.getString(3);
                    break;
                }
            }
            return processHost;
        }
    }
}
