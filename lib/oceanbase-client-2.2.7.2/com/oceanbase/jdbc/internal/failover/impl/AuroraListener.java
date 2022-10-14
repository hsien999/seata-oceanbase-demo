// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.failover.impl;

import java.sql.ResultSet;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import java.util.ArrayList;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.protocol.AuroraProtocol;
import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.LinkedList;
import com.oceanbase.jdbc.internal.util.dao.ReconnectDuringTransactionException;
import com.oceanbase.jdbc.internal.failover.tools.SearchFilter;
import java.util.regex.Matcher;
import java.util.Iterator;
import com.oceanbase.jdbc.internal.util.Utils;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import com.oceanbase.jdbc.UrlParser;
import com.oceanbase.jdbc.HostAddress;
import java.util.regex.Pattern;
import java.util.logging.Logger;

public class AuroraListener extends MastersSlavesListener
{
    private static final Logger logger;
    private final Pattern auroraDnsPattern;
    private final HostAddress clusterHostAddress;
    private String clusterDnsSuffix;
    
    public AuroraListener(final UrlParser urlParser, final GlobalStateInfo globalInfo) throws SQLException {
        super(urlParser, globalInfo);
        this.auroraDnsPattern = Pattern.compile("(.+)\\.(cluster-|cluster-ro-)?([a-zA-Z0-9]+\\.[a-zA-Z0-9\\-]+\\.rds\\.amazonaws\\.com)", 2);
        this.clusterDnsSuffix = null;
        this.clusterHostAddress = this.findClusterHostAddress();
    }
    
    private HostAddress findClusterHostAddress() throws SQLException {
        for (final HostAddress hostAddress : this.hostAddresses) {
            final Matcher matcher = this.auroraDnsPattern.matcher(hostAddress.host);
            if (matcher.find()) {
                if (this.clusterDnsSuffix != null) {
                    if (!this.clusterDnsSuffix.equalsIgnoreCase(matcher.group(3))) {
                        throw new SQLException("Connection string must contain only one aurora cluster. '" + hostAddress.host + "' doesn't correspond to DNS prefix '" + this.clusterDnsSuffix + "'");
                    }
                }
                else {
                    this.clusterDnsSuffix = matcher.group(3);
                }
                if (matcher.group(2) != null && !matcher.group(2).isEmpty()) {
                    return hostAddress;
                }
                continue;
            }
            else {
                if (this.clusterDnsSuffix != null || !hostAddress.host.contains(".") || Utils.isIPv4(hostAddress.host) || Utils.isIPv6(hostAddress.host)) {
                    continue;
                }
                this.clusterDnsSuffix = hostAddress.host.substring(hostAddress.host.indexOf(".") + 1);
            }
        }
        return null;
    }
    
    public String getClusterDnsSuffix() {
        return this.clusterDnsSuffix;
    }
    
    public HostAddress getClusterHostAddress() {
        return this.clusterHostAddress;
    }
    
    @Override
    public void reconnectFailedConnection(final SearchFilter initialSearchFilter) throws SQLException {
        SearchFilter searchFilter = initialSearchFilter;
        if (!searchFilter.isInitialConnection() && (this.isExplicitClosed() || (searchFilter.isFineIfFoundOnlyMaster() && !this.isMasterHostFail()) || (searchFilter.isFineIfFoundOnlySlave() && !this.isSecondaryHostFail()))) {
            return;
        }
        if (!searchFilter.isFailoverLoop()) {
            try {
                this.checkWaitingConnection();
                if ((searchFilter.isFineIfFoundOnlyMaster() && !this.isMasterHostFail()) || (searchFilter.isFineIfFoundOnlySlave() && !this.isSecondaryHostFail())) {
                    return;
                }
            }
            catch (ReconnectDuringTransactionException e) {
                return;
            }
        }
        this.currentConnectionAttempts.incrementAndGet();
        this.resetOldsBlackListHosts();
        final List<HostAddress> loopAddress = new LinkedList<HostAddress>(this.hostAddresses);
        loopAddress.removeAll(this.getBlacklistKeys());
        Collections.shuffle(loopAddress);
        final List<HostAddress> blacklistShuffle = new LinkedList<HostAddress>(this.getBlacklistKeys());
        blacklistShuffle.retainAll(this.hostAddresses);
        Collections.shuffle(blacklistShuffle);
        loopAddress.addAll(blacklistShuffle);
        if (this.masterProtocol != null && !this.isMasterHostFail()) {
            loopAddress.remove(this.masterProtocol.getHostAddress());
            loopAddress.add(this.masterProtocol.getHostAddress());
        }
        if (!this.isSecondaryHostFail() && this.secondaryProtocol != null) {
            loopAddress.remove(this.secondaryProtocol.getHostAddress());
            loopAddress.add(this.secondaryProtocol.getHostAddress());
        }
        if (this.hostAddresses.size() <= 1) {
            searchFilter = new SearchFilter(true, false);
        }
        if (this.isMasterHostFail() || this.isSecondaryHostFail() || searchFilter.isInitialConnection()) {
            do {
                AuroraProtocol.loop(this, this.globalInfo, loopAddress, searchFilter);
                if (!searchFilter.isFailoverLoop()) {
                    try {
                        this.checkWaitingConnection();
                    }
                    catch (ReconnectDuringTransactionException ex) {}
                }
            } while (searchFilter.isInitialConnection() && this.masterProtocol == null && (!this.urlParser.getOptions().allowMasterDownConnection || this.secondaryProtocol == null));
        }
        if (this.getCurrentProtocol() != null && !this.getCurrentProtocol().isClosed()) {
            this.retrieveAllEndpointsAndSet(this.getCurrentProtocol());
        }
        if (searchFilter.isInitialConnection() && this.masterProtocol == null && !this.currentReadOnlyAsked) {
            this.currentProtocol = this.secondaryProtocol;
            this.currentReadOnlyAsked = true;
        }
    }
    
    public void retrieveAllEndpointsAndSet(final Protocol protocol) throws SQLException {
        if (this.clusterDnsSuffix != null) {
            final List<String> endpoints = this.getCurrentEndpointIdentifiers(protocol);
            this.setUrlParserFromEndpoints(endpoints, protocol.getPort());
        }
    }
    
    private List<String> getCurrentEndpointIdentifiers(final Protocol protocol) throws SQLException {
        final List<String> endpoints = new ArrayList<String>();
        try {
            this.proxy.lock.lock();
            try {
                final Results results = new Results();
                protocol.executeQuery(false, results, "select server_id, session_id from information_schema.replica_host_status where last_update_timestamp > now() - INTERVAL 3 MINUTE");
                results.commandEnd();
                final ResultSet resultSet = results.getResultSet();
                while (resultSet.next()) {
                    endpoints.add(resultSet.getString(1) + "." + this.clusterDnsSuffix);
                }
                Collections.shuffle(endpoints);
            }
            finally {
                this.proxy.lock.unlock();
            }
        }
        catch (SQLException qe) {
            AuroraListener.logger.warning("SQL exception occurred: " + qe.getMessage());
            if (protocol.getProxy().hasToHandleFailover(qe)) {
                if (this.masterProtocol == null || this.masterProtocol.equals(protocol)) {
                    this.setMasterHostFail();
                }
                else if (this.secondaryProtocol.equals(protocol)) {
                    this.setSecondaryHostFail();
                }
                this.addToBlacklist(protocol.getHostAddress());
                this.reconnectFailedConnection(new SearchFilter(this.isMasterHostFail(), this.isSecondaryHostFail()));
            }
        }
        return endpoints;
    }
    
    private void setUrlParserFromEndpoints(final List<String> endpoints, final int port) {
        final List<HostAddress> addresses = new ArrayList<HostAddress>();
        for (final String endpoint : endpoints) {
            if (endpoint != null) {
                addresses.add(new HostAddress(endpoint, port, null));
            }
        }
        if (addresses.isEmpty()) {
            addresses.addAll(this.urlParser.getHostAddresses());
        }
        this.hostAddresses = addresses;
    }
    
    public HostAddress searchByStartName(final Protocol secondaryProtocol, final List<HostAddress> loopAddress) {
        if (!this.isSecondaryHostFail()) {
            int checkWriterAttempts = 3;
            HostAddress currentWriter = null;
            do {
                try {
                    currentWriter = this.searchForMasterHostAddress(secondaryProtocol, loopAddress);
                }
                catch (SQLException qe) {
                    if (this.proxy.hasToHandleFailover(qe) && this.setSecondaryHostFail()) {
                        this.addToBlacklist(secondaryProtocol.getHostAddress());
                        return null;
                    }
                }
                --checkWriterAttempts;
            } while (currentWriter == null && checkWriterAttempts > 0);
            if (currentWriter == null && this.getClusterHostAddress() != null) {
                final AuroraProtocol possibleMasterProtocol = AuroraProtocol.getNewProtocol(this.getProxy(), this.globalInfo, this.getUrlParser());
                possibleMasterProtocol.setHostAddress(this.getClusterHostAddress());
                try {
                    possibleMasterProtocol.connect();
                    if (possibleMasterProtocol.isMasterConnection()) {
                        possibleMasterProtocol.setMustBeMasterConnection(true);
                        this.foundActiveMaster(possibleMasterProtocol);
                    }
                    else {
                        possibleMasterProtocol.setMustBeMasterConnection(false);
                    }
                }
                catch (SQLException qe2) {
                    if (this.proxy.hasToHandleFailover(qe2)) {
                        this.addToBlacklist(possibleMasterProtocol.getHostAddress());
                    }
                }
            }
            return currentWriter;
        }
        return null;
    }
    
    private HostAddress searchForMasterHostAddress(final Protocol protocol, final List<HostAddress> loopAddress) throws SQLException {
        this.proxy.lock.lock();
        String masterHostName;
        try {
            final Results results = new Results();
            protocol.executeQuery(false, results, "select server_id from information_schema.replica_host_status where session_id = 'MASTER_SESSION_ID' and last_update_timestamp > now() - INTERVAL 3 MINUTE ORDER BY last_update_timestamp DESC LIMIT 1");
            results.commandEnd();
            final ResultSet queryResult = results.getResultSet();
            if (!queryResult.isBeforeFirst()) {
                return null;
            }
            queryResult.next();
            masterHostName = queryResult.getString(1);
        }
        finally {
            this.proxy.lock.unlock();
        }
        if (masterHostName == null) {
            return null;
        }
        for (final HostAddress hostAddress : loopAddress) {
            final Matcher matcher = this.auroraDnsPattern.matcher(hostAddress.host);
            if (hostAddress.host.startsWith(masterHostName) && !matcher.find()) {
                return hostAddress;
            }
        }
        if (this.clusterDnsSuffix == null && protocol.getHost().contains(".")) {
            this.clusterDnsSuffix = protocol.getHost().substring(protocol.getHost().indexOf(".") + 1);
            final HostAddress masterHostAddress = new HostAddress(masterHostName + "." + this.clusterDnsSuffix, protocol.getPort(), null);
            loopAddress.add(masterHostAddress);
            if (!this.hostAddresses.contains(masterHostAddress)) {
                this.hostAddresses.add(masterHostAddress);
            }
            return masterHostAddress;
        }
        return null;
    }
    
    @Override
    public boolean checkMasterStatus(final SearchFilter searchFilter) {
        if (!this.isMasterHostFail()) {
            try {
                if (this.masterProtocol != null && !this.masterProtocol.checkIfMaster()) {
                    this.setMasterHostFail();
                    if (this.isSecondaryHostFail()) {
                        this.foundActiveSecondary(this.masterProtocol);
                    }
                    return true;
                }
            }
            catch (SQLException e) {
                try {
                    this.masterProtocol.ping();
                }
                catch (SQLException ee) {
                    this.proxy.lock.lock();
                    try {
                        this.masterProtocol.close();
                    }
                    finally {
                        this.proxy.lock.unlock();
                    }
                    if (this.setMasterHostFail()) {
                        this.addToBlacklist(this.masterProtocol.getHostAddress());
                    }
                }
                return true;
            }
        }
        if (!this.isSecondaryHostFail()) {
            try {
                if (this.secondaryProtocol != null && this.secondaryProtocol.checkIfMaster()) {
                    this.setSecondaryHostFail();
                    if (this.isMasterHostFail()) {
                        this.foundActiveMaster(this.secondaryProtocol);
                    }
                    return true;
                }
            }
            catch (SQLException e) {
                try {
                    this.secondaryProtocol.ping();
                }
                catch (Exception ee2) {
                    this.proxy.lock.lock();
                    try {
                        this.secondaryProtocol.close();
                    }
                    finally {
                        this.proxy.lock.unlock();
                    }
                    if (this.setSecondaryHostFail()) {
                        this.addToBlacklist(this.secondaryProtocol.getHostAddress());
                    }
                    return true;
                }
            }
        }
        return false;
    }
    
    static {
        logger = Logger.getLogger(AuroraListener.class.getName());
    }
}
