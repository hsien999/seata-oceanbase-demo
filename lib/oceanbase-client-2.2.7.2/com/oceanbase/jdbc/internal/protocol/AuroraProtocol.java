// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol;

import java.net.SocketException;
import com.oceanbase.jdbc.internal.util.SqlStates;
import java.sql.ResultSet;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.internal.failover.FailoverProxy;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Deque;
import com.oceanbase.jdbc.internal.failover.impl.MastersSlavesListener;
import java.util.Collection;
import java.util.ArrayDeque;
import com.oceanbase.jdbc.internal.failover.tools.SearchFilter;
import java.util.List;
import java.sql.SQLException;
import com.oceanbase.jdbc.HostAddress;
import com.oceanbase.jdbc.internal.failover.impl.AuroraListener;
import com.oceanbase.jdbc.internal.io.LruTraceCache;
import java.util.concurrent.locks.ReentrantLock;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import com.oceanbase.jdbc.UrlParser;

public class AuroraProtocol extends MastersSlavesProtocol
{
    public AuroraProtocol(final UrlParser url, final GlobalStateInfo globalInfo, final ReentrantLock lock, final LruTraceCache traceCache) {
        super(url, globalInfo, lock, traceCache);
    }
    
    private static void searchProbableMaster(final AuroraListener listener, final GlobalStateInfo globalInfo, final HostAddress probableMaster) {
        AuroraProtocol protocol = getNewProtocol(listener.getProxy(), globalInfo, listener.getUrlParser());
        try {
            protocol.setHostAddress(probableMaster);
            protocol.connect();
            listener.removeFromBlacklist(protocol.getHostAddress());
            if (listener.isMasterHostFailReconnect() && protocol.isMasterConnection()) {
                protocol.setMustBeMasterConnection(true);
                listener.foundActiveMaster(protocol);
            }
            else if (listener.isSecondaryHostFailReconnect() && !protocol.isMasterConnection()) {
                protocol.setMustBeMasterConnection(false);
                listener.foundActiveSecondary(protocol);
            }
            else {
                protocol.close();
                protocol = getNewProtocol(listener.getProxy(), globalInfo, listener.getUrlParser());
            }
        }
        catch (SQLException e) {
            listener.addToBlacklist(protocol.getHostAddress());
        }
    }
    
    public static void loop(final AuroraListener listener, final GlobalStateInfo globalInfo, final List<HostAddress> addresses, final SearchFilter initialSearchFilter) throws SQLException {
        SearchFilter searchFilter = initialSearchFilter;
        final Deque<HostAddress> loopAddresses = new ArrayDeque<HostAddress>(addresses);
        if (loopAddresses.isEmpty()) {
            resetHostList(listener, loopAddresses);
        }
        int maxConnectionTry = listener.getRetriesAllDown();
        SQLException lastQueryException = null;
        HostAddress probableMasterHost = null;
        boolean firstLoop = true;
        while (!loopAddresses.isEmpty() || (!searchFilter.isFailoverLoop() && maxConnectionTry > 0)) {
            final AuroraProtocol protocol = getNewProtocol(listener.getProxy(), globalInfo, listener.getUrlParser());
            if (listener.isExplicitClosed() || (!listener.isSecondaryHostFailReconnect() && !listener.isMasterHostFailReconnect())) {
                return;
            }
            --maxConnectionTry;
            try {
                HostAddress host = loopAddresses.pollFirst();
                if (host == null) {
                    for (final HostAddress hostAddress : listener.getUrlParser().getHostAddresses()) {
                        if (!hostAddress.equals(listener.getClusterHostAddress())) {
                            loopAddresses.add(hostAddress);
                        }
                    }
                    if (listener.getClusterHostAddress() != null && (listener.getUrlParser().getHostAddresses().size() < 2 || loopAddresses.isEmpty())) {
                        loopAddresses.add(listener.getClusterHostAddress());
                    }
                    host = loopAddresses.pollFirst();
                }
                protocol.setHostAddress(host);
                protocol.connect();
                if (listener.isExplicitClosed()) {
                    protocol.close();
                    return;
                }
                listener.removeFromBlacklist(protocol.getHostAddress());
                if (listener.isMasterHostFailReconnect() && protocol.isMasterConnection()) {
                    if (searchFilter.isFineIfFoundOnlyMaster() && listener.getUrlParser().getHostAddresses().size() <= 1 && protocol.getHostAddress().equals(listener.getClusterHostAddress())) {
                        listener.retrieveAllEndpointsAndSet(protocol);
                        if (listener.getUrlParser().getHostAddresses().size() > 1) {
                            loopAddresses.addAll(listener.getUrlParser().getHostAddresses());
                            searchFilter = new SearchFilter(false);
                        }
                    }
                    if (MastersSlavesProtocol.foundMaster(listener, protocol, searchFilter)) {
                        return;
                    }
                }
                else if (!protocol.isMasterConnection()) {
                    if (listener.isSecondaryHostFailReconnect()) {
                        if (listener.getUrlParser().getHostAddresses().size() <= 1 && protocol.getHostAddress().equals(listener.getClusterHostAddress())) {
                            listener.retrieveAllEndpointsAndSet(protocol);
                            if (listener.getUrlParser().getHostAddresses().size() > 1) {
                                loopAddresses.addAll(listener.getUrlParser().getHostAddresses());
                                searchFilter = new SearchFilter(false);
                            }
                        }
                        else if (MastersSlavesProtocol.foundSecondary(listener, protocol, searchFilter)) {
                            return;
                        }
                    }
                    else {
                        try {
                            if (listener.isSecondaryHostFailReconnect() || (listener.isMasterHostFailReconnect() && probableMasterHost == null)) {
                                probableMasterHost = listener.searchByStartName(protocol, listener.getUrlParser().getHostAddresses());
                                if (probableMasterHost != null) {
                                    loopAddresses.remove(probableMasterHost);
                                    searchProbableMaster(listener, globalInfo, probableMasterHost);
                                    if (listener.isMasterHostFailReconnect() && searchFilter.isFineIfFoundOnlySlave()) {
                                        return;
                                    }
                                }
                            }
                        }
                        finally {
                            protocol.close();
                        }
                    }
                }
                else {
                    protocol.close();
                }
            }
            catch (SQLException e) {
                lastQueryException = e;
                listener.addToBlacklist(protocol.getHostAddress());
            }
            if (!listener.isMasterHostFailReconnect() && !listener.isSecondaryHostFailReconnect()) {
                return;
            }
            if (loopAddresses.isEmpty() && listener.isMasterHostFailReconnect() && listener.urlParser.getOptions().allowMasterDownConnection && !listener.isSecondaryHostFailReconnect()) {
                return;
            }
            if (loopAddresses.isEmpty() && searchFilter.isInitialConnection() && !listener.isMasterHostFailReconnect()) {
                return;
            }
            if (loopAddresses.isEmpty() && !searchFilter.isFailoverLoop() && maxConnectionTry > 0) {
                resetHostList(listener, loopAddresses);
                if (firstLoop) {
                    firstLoop = false;
                }
                else {
                    try {
                        Thread.sleep(250L);
                    }
                    catch (InterruptedException ex) {}
                }
            }
            if (maxConnectionTry != 0 || loopAddresses.contains(listener.getClusterHostAddress()) || listener.getClusterHostAddress() == null) {
                continue;
            }
            loopAddresses.add(listener.getClusterHostAddress());
        }
        if (!listener.isMasterHostFailReconnect() && !listener.isSecondaryHostFailReconnect()) {
            return;
        }
        String error = "No active connection found for replica";
        if (listener.isMasterHostFailReconnect()) {
            error = "No active connection found for master";
        }
        if (lastQueryException != null) {
            throw new SQLException(error, lastQueryException.getSQLState(), lastQueryException.getErrorCode(), lastQueryException);
        }
        throw new SQLException(error);
    }
    
    private static void resetHostList(final AuroraListener listener, final Deque<HostAddress> loopAddresses) {
        final List<HostAddress> servers = new ArrayList<HostAddress>();
        servers.addAll(listener.getUrlParser().getHostAddresses());
        Collections.shuffle(servers);
        if (listener.getClusterHostAddress() != null && listener.getUrlParser().getHostAddresses().size() < 2) {
            servers.add(listener.getClusterHostAddress());
        }
        servers.removeAll(listener.connectedHosts());
        loopAddresses.clear();
        loopAddresses.addAll(servers);
    }
    
    public static AuroraProtocol getNewProtocol(final FailoverProxy proxy, final GlobalStateInfo globalInfo, final UrlParser urlParser) {
        final AuroraProtocol newProtocol = new AuroraProtocol(urlParser, globalInfo, proxy.lock, proxy.traceCache);
        newProtocol.setProxy(proxy);
        return newProtocol;
    }
    
    @Override
    public boolean isMasterConnection() {
        return this.masterConnection;
    }
    
    @Override
    public void readPipelineCheckMaster() throws SQLException {
        final Results results = new Results();
        this.getResult(results);
        results.commandEnd();
        final ResultSet resultSet = results.getResultSet();
        this.masterConnection = (!resultSet.next() || (this.masterConnection = (0 == resultSet.getInt(1))));
        this.reader.setServerThreadId(this.serverThreadId, this.masterConnection);
        this.writer.setServerThreadId(this.serverThreadId, this.masterConnection);
        this.readOnly = !this.masterConnection;
    }
    
    @Override
    public boolean isValid(final int timeout) throws SQLException {
        int initialTimeout = -1;
        try {
            initialTimeout = this.socket.getSoTimeout();
            this.socket.setSoTimeout(timeout);
            if (this.isMasterConnection()) {
                return this.checkIfMaster();
            }
            return this.ping();
        }
        catch (SocketException socketException) {
            throw new SQLException("Could not valid connection : " + socketException.getMessage(), SqlStates.CONNECTION_EXCEPTION.getSqlState(), socketException);
        }
        finally {
            try {
                if (initialTimeout != -1) {
                    this.socket.setSoTimeout(initialTimeout);
                }
            }
            catch (SocketException ex) {}
        }
    }
    
    @Override
    public boolean checkIfMaster() throws SQLException {
        this.proxy.lock.lock();
        try {
            final Results results = new Results();
            this.executeQuery(this.isMasterConnection(), results, "select @@innodb_read_only");
            results.commandEnd();
            final ResultSet queryResult = results.getResultSet();
            if (queryResult != null && queryResult.next()) {
                this.masterConnection = (0 == queryResult.getInt(1));
                this.reader.setServerThreadId(this.serverThreadId, this.masterConnection);
                this.writer.setServerThreadId(this.serverThreadId, this.masterConnection);
            }
            else {
                this.masterConnection = true;
            }
            this.readOnly = !this.masterConnection;
            return this.masterConnection;
        }
        catch (SQLException sqle) {
            throw new SQLException("could not check the 'innodb_read_only' variable status on " + this.getHostAddress() + " : " + sqle.getMessage(), SqlStates.CONNECTION_EXCEPTION.getSqlState(), sqle);
        }
        finally {
            this.proxy.lock.unlock();
        }
    }
}
