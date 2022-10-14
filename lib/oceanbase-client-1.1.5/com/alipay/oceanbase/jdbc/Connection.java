// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.concurrent.Executor;
import java.util.Properties;
import java.util.TimeZone;
import com.alipay.oceanbase.jdbc.log.Log;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface Connection extends java.sql.Connection, ConnectionProperties
{
    void changeUser(final String p0, final String p1) throws SQLException;
    
    @Deprecated
    void clearHasTriedMaster();
    
    PreparedStatement clientPrepareStatement(final String p0) throws SQLException;
    
    PreparedStatement clientPrepareStatement(final String p0, final int p1) throws SQLException;
    
    PreparedStatement clientPrepareStatement(final String p0, final int p1, final int p2) throws SQLException;
    
    PreparedStatement clientPrepareStatement(final String p0, final int[] p1) throws SQLException;
    
    PreparedStatement clientPrepareStatement(final String p0, final int p1, final int p2, final int p3) throws SQLException;
    
    PreparedStatement clientPrepareStatement(final String p0, final String[] p1) throws SQLException;
    
    int getActiveStatementCount();
    
    long getIdleFor();
    
    Log getLog() throws SQLException;
    
    @Deprecated
    String getServerCharacterEncoding();
    
    String getServerCharset();
    
    TimeZone getServerTimezoneTZ();
    
    String getStatementComment();
    
    @Deprecated
    boolean hasTriedMaster();
    
    boolean isInGlobalTx();
    
    void setInGlobalTx(final boolean p0);
    
    boolean isMasterConnection();
    
    boolean isNoBackslashEscapesSet();
    
    boolean isSameResource(final Connection p0);
    
    boolean lowerCaseTableNames();
    
    boolean parserKnowsUnicode();
    
    void ping() throws SQLException;
    
    void resetServerState() throws SQLException;
    
    PreparedStatement serverPrepareStatement(final String p0) throws SQLException;
    
    PreparedStatement serverPrepareStatement(final String p0, final int p1) throws SQLException;
    
    PreparedStatement serverPrepareStatement(final String p0, final int p1, final int p2) throws SQLException;
    
    PreparedStatement serverPrepareStatement(final String p0, final int p1, final int p2, final int p3) throws SQLException;
    
    PreparedStatement serverPrepareStatement(final String p0, final int[] p1) throws SQLException;
    
    PreparedStatement serverPrepareStatement(final String p0, final String[] p1) throws SQLException;
    
    void setFailedOver(final boolean p0);
    
    @Deprecated
    void setPreferSlaveDuringFailover(final boolean p0);
    
    void setStatementComment(final String p0);
    
    void shutdownServer() throws SQLException;
    
    boolean supportsIsolationLevel();
    
    boolean supportsQuotedIdentifiers();
    
    boolean supportsTransactions();
    
    boolean versionMeetsMinimum(final int p0, final int p1, final int p2) throws SQLException;
    
    void reportQueryTime(final long p0);
    
    boolean isAbonormallyLongQuery(final long p0);
    
    void initializeExtension(final Extension p0) throws SQLException;
    
    int getAutoIncrementIncrement();
    
    boolean hasSameProperties(final Connection p0);
    
    Properties getProperties();
    
    String getHost();
    
    void setProxy(final MySQLConnection p0);
    
    boolean isServerLocal() throws SQLException;
    
    int getSessionMaxRows();
    
    void setSessionMaxRows(final int p0) throws SQLException;
    
    void setSchema(final String p0) throws SQLException;
    
    String getSchema() throws SQLException;
    
    void abort(final Executor p0) throws SQLException;
    
    void setNetworkTimeout(final Executor p0, final int p1) throws SQLException;
    
    int getNetworkTimeout() throws SQLException;
    
    void abortInternal() throws SQLException;
    
    void checkClosed() throws SQLException;
    
    Object getConnectionMutex();
}
