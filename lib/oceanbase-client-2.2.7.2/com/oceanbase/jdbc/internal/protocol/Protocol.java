// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol;

import java.sql.SQLTimeoutException;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import java.util.concurrent.FutureTask;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import java.io.IOException;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.util.concurrent.locks.ReentrantLock;
import com.oceanbase.jdbc.OceanBaseStatement;
import com.oceanbase.jdbc.OceanBaseConnection;
import java.util.TimeZone;
import com.oceanbase.jdbc.internal.util.ServerPrepareStatementCache;
import java.net.Socket;
import java.net.SocketException;
import java.io.InputStream;
import java.util.List;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import com.oceanbase.jdbc.internal.util.dao.ClientPrepareResult;
import java.nio.charset.Charset;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.HostAddress;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.failover.FailoverProxy;
import com.oceanbase.jdbc.UrlParser;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;

public interface Protocol
{
    ServerPrepareResult prepare(final String p0, final boolean p1) throws SQLException;
    
    boolean getAutocommit() throws SQLException;
    
    void setAutoCommit(final boolean p0) throws SQLException;
    
    boolean noBackslashEscapes();
    
    void connect() throws SQLException;
    
    UrlParser getUrlParser();
    
    boolean inTransaction();
    
    boolean isOracleMode();
    
    boolean isTZTablesImported();
    
    FailoverProxy getProxy();
    
    void setProxy(final FailoverProxy p0);
    
    Options getOptions();
    
    boolean hasMoreResults();
    
    void close();
    
    void abort();
    
    void reset() throws SQLException;
    
    void closeExplicit();
    
    boolean isClosed();
    
    void resetDatabase() throws SQLException;
    
    String getCatalog() throws SQLException;
    
    void setCatalog(final String p0) throws SQLException;
    
    String getServerVersion();
    
    void setObServerVersion(final String p0);
    
    String getObServerVersion();
    
    boolean supportStmtPrepareExecute();
    
    boolean isConnected();
    
    boolean getReadonly() throws SQLException;
    
    void setReadonly(final boolean p0) throws SQLException;
    
    boolean isMasterConnection();
    
    boolean mustBeMasterConnection();
    
    HostAddress getHostAddress();
    
    void setHostAddress(final HostAddress p0);
    
    String getHost();
    
    int getPort();
    
    void rollback() throws SQLException;
    
    String getDatabase();
    
    String getUsername();
    
    void setUsername(final String p0);
    
    boolean ping() throws SQLException;
    
    boolean isValid(final int p0) throws SQLException;
    
    void executeQuery(final String p0) throws SQLException;
    
    void executeQuery(final boolean p0, final Results p1, final String p2) throws SQLException;
    
    void executeQuery(final boolean p0, final Results p1, final String p2, final Charset p3) throws SQLException;
    
    void executeQuery(final boolean p0, final Results p1, final ClientPrepareResult p2, final ParameterHolder[] p3) throws SQLException;
    
    void executeQuery(final boolean p0, final Results p1, final ClientPrepareResult p2, final ParameterHolder[] p3, final int p4) throws SQLException;
    
    void executePreparedQuery(final boolean p0, final ServerPrepareResult p1, final Results p2, final ParameterHolder[] p3) throws SQLException;
    
    ServerPrepareResult executePreparedQuery(final int p0, final ParameterHolder[] p1, final ServerPrepareResult p2, final Results p3) throws SQLException;
    
    ServerPrepareResult executeBatchServer(final ServerPrepareResult p0, final Results p1, final String p2, final List<ParameterHolder[]> p3, final boolean p4) throws SQLException;
    
    boolean executeBatchClient(final boolean p0, final Results p1, final ClientPrepareResult p2, final List<ParameterHolder[]> p3, final boolean p4) throws SQLException;
    
    void executeBatchStmt(final boolean p0, final Results p1, final List<String> p2) throws SQLException;
    
    void getResult(final Results p0) throws SQLException;
    
    void cancelCurrentQuery() throws SQLException;
    
    void interrupt();
    
    void skip() throws SQLException;
    
    boolean checkIfMaster() throws SQLException;
    
    boolean hasWarnings();
    
    long getMaxRows();
    
    void setMaxRows(final long p0) throws SQLException;
    
    int getMajorServerVersion();
    
    int getMinorServerVersion();
    
    void parseVersion(final String p0);
    
    boolean versionGreaterOrEqual(final int p0, final int p1, final int p2);
    
    void setLocalInfileInputStream(final InputStream p0);
    
    int getTimeout();
    
    void setTimeout(final int p0) throws SocketException;
    
    boolean getPinGlobalTxToPhysicalConnection();
    
    long getServerThreadId();
    
    Socket getSocket();
    
    void setTransactionIsolation(final int p0) throws SQLException;
    
    int getTransactionIsolationLevel();
    
    boolean isExplicitClosed();
    
    void connectWithoutProxy() throws SQLException;
    
    boolean shouldReconnectWithoutProxy();
    
    void setHostFailedWithoutProxy();
    
    void releasePrepareStatement(final ServerPrepareResult p0) throws SQLException;
    
    boolean forceReleasePrepareStatement(final int p0) throws SQLException;
    
    void forceReleaseWaitingPrepareStatement() throws SQLException;
    
    ServerPrepareStatementCache prepareStatementCache();
    
    TimeZone getTimeZone();
    
    void prolog(final long p0, final boolean p1, final OceanBaseConnection p2, final OceanBaseStatement p3) throws SQLException;
    
    void prologProxy(final ServerPrepareResult p0, final long p1, final boolean p2, final OceanBaseConnection p3, final OceanBaseStatement p4) throws SQLException;
    
    Results getActiveStreamingResult();
    
    void setActiveStreamingResult(final Results p0);
    
    ReentrantLock getLock();
    
    void setServerStatus(final short p0);
    
    void removeHasMoreResults();
    
    void setHasWarnings(final boolean p0);
    
    ServerPrepareResult addPrepareInCache(final String p0, final ServerPrepareResult p1);
    
    void readOkPacket(final Buffer p0, final Results p1);
    
    void readEofPacket() throws SQLException, IOException;
    
    void skipEofPacket() throws SQLException, IOException;
    
    SQLException readErrorPacket(final Buffer p0, final Results p1);
    
    void readResultSet(final ColumnDefinition[] p0, final Results p1) throws SQLException;
    
    void changeSocketTcpNoDelay(final boolean p0);
    
    void changeSocketSoTimeout(final int p0) throws SocketException;
    
    void removeActiveStreamingResult();
    
    void resetStateAfterFailover(final long p0, final int p1, final String p2, final boolean p3) throws SQLException;
    
    void setActiveFutureTask(final FutureTask p0);
    
    boolean isServerMariaDb();
    
    SQLException handleIoException(final Exception p0);
    
    PacketInputStream getReader();
    
    boolean isEofDeprecated();
    
    int getAutoIncrementIncrement() throws SQLException;
    
    boolean sessionStateAware();
    
    String getTraces();
    
    boolean isInterrupted();
    
    void stopIfInterrupted() throws SQLTimeoutException;
    
    void setChecksum(final long p0);
    
    void resetChecksum();
    
    long getChecksum();
    
    void setIterationCount(final int p0);
    
    int getIterationCount();
    
    void setExecuteMode(final int p0);
    
    int getExecuteMode();
    
    void setComStmtPrepareExecuteField(final int p0, final int p1, final long p2);
    
    ColumnDefinition[] sendFechRowViaCursor(final ServerPrepareResult p0, final int p1, final int p2, final Results p3) throws SQLException;
    
    long getLastPacketCostTime() throws SQLException;
    
    void setNetworkStatisticsFlag(final boolean p0);
    
    boolean getNetworkStatisticsFlag();
    
    long getLastPacketResponseTimestamp();
    
    long getLastPacketSendTimestamp();
    
    void clearNetworkStatistics();
    
    void changeUser(final String p0, final String p1) throws SQLException;
    
    String getEncoding();
}
