// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.failover;

import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import java.net.SocketException;
import com.oceanbase.jdbc.OceanBaseStatement;
import com.oceanbase.jdbc.OceanBaseConnection;
import com.oceanbase.jdbc.UrlParser;
import com.oceanbase.jdbc.HostAddress;
import java.util.Set;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import java.lang.reflect.Method;
import com.oceanbase.jdbc.internal.failover.tools.SearchFilter;
import java.sql.SQLException;

public interface Listener
{
    FailoverProxy getProxy();
    
    void setProxy(final FailoverProxy p0);
    
    void initializeConnection() throws SQLException;
    
    void preExecute() throws SQLException;
    
    void preClose();
    
    void preAbort();
    
    long getServerThreadId();
    
    void reconnectFailedConnection(final SearchFilter p0) throws SQLException;
    
    void switchReadOnlyConnection(final Boolean p0) throws SQLException;
    
    HandleErrorResult primaryFail(final Method p0, final Object[] p1, final boolean p2, final boolean p3) throws SQLException;
    
    Object invoke(final Method p0, final Object[] p1, final Protocol p2) throws Throwable;
    
    Object invoke(final Method p0, final Object[] p1) throws Throwable;
    
    HandleErrorResult handleFailover(final SQLException p0, final Method p1, final Object[] p2, final Protocol p3, final boolean p4) throws Throwable;
    
    void foundActiveMaster(final Protocol p0) throws SQLException;
    
    Set<HostAddress> getBlacklistKeys();
    
    void addToBlacklist(final HostAddress p0);
    
    void removeFromBlacklist(final HostAddress p0);
    
    void syncConnection(final Protocol p0, final Protocol p1) throws SQLException;
    
    UrlParser getUrlParser();
    
    void throwFailoverMessage(final HostAddress p0, final boolean p1, final SQLException p2, final boolean p3) throws SQLException;
    
    boolean isAutoReconnect();
    
    int getRetriesAllDown();
    
    boolean isExplicitClosed();
    
    void reconnect() throws SQLException;
    
    boolean isReadOnly();
    
    boolean inTransaction();
    
    int getMajorServerVersion();
    
    boolean isMasterConnection();
    
    boolean isClosed();
    
    boolean versionGreaterOrEqual(final int p0, final int p1, final int p2);
    
    boolean isServerMariaDb();
    
    boolean sessionStateAware();
    
    boolean noBackslashEscapes();
    
    boolean isValid(final int p0) throws SQLException;
    
    void prolog(final long p0, final OceanBaseConnection p1, final OceanBaseStatement p2) throws SQLException;
    
    String getCatalog() throws SQLException;
    
    int getTimeout() throws SocketException;
    
    Protocol getCurrentProtocol();
    
    boolean hasHostFail();
    
    boolean canRetryFailLoop();
    
    SearchFilter getFilterForFailedHost();
    
    boolean isMasterConnected();
    
    boolean setMasterHostFail();
    
    boolean isMasterHostFail();
    
    long getLastQueryNanos();
    
    boolean checkMasterStatus(final SearchFilter p0);
    
    void rePrepareOnSlave(final ServerPrepareResult p0, final boolean p1) throws SQLException;
    
    void reset() throws SQLException;
}
