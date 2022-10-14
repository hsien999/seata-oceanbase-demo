// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdbc2.optional;

import java.util.concurrent.Executor;
import com.alipay.oceanbase.jdbc.MySQLConnection;
import com.alipay.oceanbase.jdbc.ExceptionInterceptor;
import com.alipay.oceanbase.jdbc.Extension;
import java.util.TimeZone;
import com.alipay.oceanbase.jdbc.log.Log;
import java.sql.Struct;
import java.sql.Array;
import java.util.Properties;
import java.sql.SQLClientInfoException;
import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.Statement;
import java.sql.SQLWarning;
import java.util.Map;
import java.sql.Savepoint;
import java.sql.DatabaseMetaData;
import com.alipay.oceanbase.jdbc.SQLError;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.Util;
import java.lang.reflect.Constructor;
import com.alipay.oceanbase.jdbc.Connection;

public class ConnectionWrapper extends WrapperBase implements Connection
{
    protected Connection mc;
    private String invalidHandleStr;
    private boolean closed;
    private boolean isForXa;
    private static final Constructor<?> JDBC_4_CONNECTION_WRAPPER_CTOR;
    
    protected static ConnectionWrapper getInstance(final MysqlPooledConnection mysqlPooledConnection, final Connection mysqlConnection, final boolean forXa) throws SQLException {
        if (!Util.isJdbc4()) {
            return new ConnectionWrapper(mysqlPooledConnection, mysqlConnection, forXa);
        }
        return (ConnectionWrapper)Util.handleNewInstance(ConnectionWrapper.JDBC_4_CONNECTION_WRAPPER_CTOR, new Object[] { mysqlPooledConnection, mysqlConnection, forXa }, mysqlPooledConnection.getExceptionInterceptor());
    }
    
    public ConnectionWrapper(final MysqlPooledConnection mysqlPooledConnection, final Connection mysqlConnection, final boolean forXa) throws SQLException {
        super(mysqlPooledConnection);
        this.mc = null;
        this.invalidHandleStr = "Logical handle no longer valid";
        this.mc = mysqlConnection;
        this.closed = false;
        this.isForXa = forXa;
        if (this.isForXa) {
            this.setInGlobalTx(false);
        }
    }
    
    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        this.checkClosed();
        if (autoCommit && this.isInGlobalTx()) {
            throw SQLError.createSQLException("Can't set autocommit to 'true' on an XAConnection", "2D000", 1401, this.exceptionInterceptor);
        }
        try {
            this.mc.setAutoCommit(autoCommit);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public boolean getAutoCommit() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.getAutoCommit();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return false;
        }
    }
    
    @Override
    public void setCatalog(final String catalog) throws SQLException {
        this.checkClosed();
        try {
            this.mc.setCatalog(catalog);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public String getCatalog() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.getCatalog();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return this.closed || this.mc.isClosed();
    }
    
    @Override
    public boolean isMasterConnection() {
        return this.mc.isMasterConnection();
    }
    
    @Override
    public void setHoldability(final int arg0) throws SQLException {
        this.checkClosed();
        try {
            this.mc.setHoldability(arg0);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public int getHoldability() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.getHoldability();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return 1;
        }
    }
    
    @Override
    public long getIdleFor() {
        return this.mc.getIdleFor();
    }
    
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.getMetaData();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public void setReadOnly(final boolean readOnly) throws SQLException {
        this.checkClosed();
        try {
            this.mc.setReadOnly(readOnly);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.isReadOnly();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return false;
        }
    }
    
    @Override
    public Savepoint setSavepoint() throws SQLException {
        this.checkClosed();
        if (this.isInGlobalTx()) {
            throw SQLError.createSQLException("Can't set autocommit to 'true' on an XAConnection", "2D000", 1401, this.exceptionInterceptor);
        }
        try {
            return this.mc.setSavepoint();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public Savepoint setSavepoint(final String arg0) throws SQLException {
        this.checkClosed();
        if (this.isInGlobalTx()) {
            throw SQLError.createSQLException("Can't set autocommit to 'true' on an XAConnection", "2D000", 1401, this.exceptionInterceptor);
        }
        try {
            return this.mc.setSavepoint(arg0);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        this.checkClosed();
        try {
            this.mc.setTransactionIsolation(level);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public int getTransactionIsolation() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.getTransactionIsolation();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return 4;
        }
    }
    
    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.getTypeMap();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.getWarnings();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public void clearWarnings() throws SQLException {
        this.checkClosed();
        try {
            this.mc.clearWarnings();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public void close() throws SQLException {
        this.close(true);
    }
    
    @Override
    public void commit() throws SQLException {
        this.checkClosed();
        if (this.isInGlobalTx()) {
            throw SQLError.createSQLException("Can't call commit() on an XAConnection associated with a global transaction", "2D000", 1401, this.exceptionInterceptor);
        }
        try {
            this.mc.commit();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public Statement createStatement() throws SQLException {
        this.checkClosed();
        try {
            return StatementWrapper.getInstance(this, this.pooledConnection, this.mc.createStatement());
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public Statement createStatement(final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this.checkClosed();
        try {
            return StatementWrapper.getInstance(this, this.pooledConnection, this.mc.createStatement(resultSetType, resultSetConcurrency));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public Statement createStatement(final int arg0, final int arg1, final int arg2) throws SQLException {
        this.checkClosed();
        try {
            return StatementWrapper.getInstance(this, this.pooledConnection, this.mc.createStatement(arg0, arg1, arg2));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public String nativeSQL(final String sql) throws SQLException {
        this.checkClosed();
        try {
            return this.mc.nativeSQL(sql);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public CallableStatement prepareCall(final String sql) throws SQLException {
        this.checkClosed();
        try {
            return CallableStatementWrapper.getInstance(this, this.pooledConnection, this.mc.prepareCall(sql));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public CallableStatement prepareCall(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this.checkClosed();
        try {
            return CallableStatementWrapper.getInstance(this, this.pooledConnection, this.mc.prepareCall(sql, resultSetType, resultSetConcurrency));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public CallableStatement prepareCall(final String arg0, final int arg1, final int arg2, final int arg3) throws SQLException {
        this.checkClosed();
        try {
            return CallableStatementWrapper.getInstance(this, this.pooledConnection, this.mc.prepareCall(arg0, arg1, arg2, arg3));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    public PreparedStatement clientPrepare(final String sql) throws SQLException {
        this.checkClosed();
        try {
            return new PreparedStatementWrapper(this, this.pooledConnection, this.mc.clientPrepareStatement(sql));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    public PreparedStatement clientPrepare(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this.checkClosed();
        try {
            return new PreparedStatementWrapper(this, this.pooledConnection, this.mc.clientPrepareStatement(sql, resultSetType, resultSetConcurrency));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        this.checkClosed();
        PreparedStatement res = null;
        try {
            res = PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.prepareStatement(sql));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
        return res;
    }
    
    @Override
    public PreparedStatement prepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this.checkClosed();
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.prepareStatement(sql, resultSetType, resultSetConcurrency));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement prepareStatement(final String arg0, final int arg1, final int arg2, final int arg3) throws SQLException {
        this.checkClosed();
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.prepareStatement(arg0, arg1, arg2, arg3));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement prepareStatement(final String arg0, final int arg1) throws SQLException {
        this.checkClosed();
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.prepareStatement(arg0, arg1));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement prepareStatement(final String arg0, final int[] arg1) throws SQLException {
        this.checkClosed();
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.prepareStatement(arg0, arg1));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement prepareStatement(final String arg0, final String[] arg1) throws SQLException {
        this.checkClosed();
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.prepareStatement(arg0, arg1));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public Clob createClob() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Blob createBlob() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public NClob createNClob() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public SQLXML createSQLXML() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public boolean isValid(final int timeout) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setClientInfo(final String name, final String value) throws SQLClientInfoException {
        throw new SQLClientInfoException();
    }
    
    @Override
    public void setClientInfo(final Properties properties) throws SQLClientInfoException {
        throw new SQLClientInfoException();
    }
    
    @Override
    public String getClientInfo(final String name) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Properties getClientInfo() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Array createArrayOf(final String typeName, final Object[] elements) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Struct createStruct(final String typeName, final Object[] attributes) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void releaseSavepoint(final Savepoint arg0) throws SQLException {
        this.checkClosed();
        try {
            this.mc.releaseSavepoint(arg0);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public void rollback() throws SQLException {
        this.checkClosed();
        if (this.isInGlobalTx()) {
            throw SQLError.createSQLException("Can't call rollback() on an XAConnection associated with a global transaction", "2D000", 1401, this.exceptionInterceptor);
        }
        try {
            this.mc.rollback();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public void rollback(final Savepoint arg0) throws SQLException {
        this.checkClosed();
        if (this.isInGlobalTx()) {
            throw SQLError.createSQLException("Can't call rollback() on an XAConnection associated with a global transaction", "2D000", 1401, this.exceptionInterceptor);
        }
        try {
            this.mc.rollback(arg0);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public boolean isSameResource(final Connection c) {
        if (c instanceof ConnectionWrapper) {
            return this.mc.isSameResource(((ConnectionWrapper)c).mc);
        }
        return this.mc.isSameResource(c);
    }
    
    protected void close(final boolean fireClosedEvent) throws SQLException {
        synchronized (this.pooledConnection) {
            if (this.closed) {
                return;
            }
            if (!this.isInGlobalTx() && this.mc.getRollbackOnPooledClose() && !this.getAutoCommit()) {
                this.rollback();
            }
            if (fireClosedEvent) {
                this.pooledConnection.callConnectionEventListeners(2, null);
            }
            this.closed = true;
        }
    }
    
    @Override
    public void checkClosed() throws SQLException {
        if (this.closed) {
            throw SQLError.createSQLException(this.invalidHandleStr, this.exceptionInterceptor);
        }
    }
    
    @Override
    public boolean isInGlobalTx() {
        return this.mc.isInGlobalTx();
    }
    
    @Override
    public void setInGlobalTx(final boolean flag) {
        this.mc.setInGlobalTx(flag);
    }
    
    @Override
    public void ping() throws SQLException {
        if (this.mc != null) {
            this.mc.ping();
        }
    }
    
    @Override
    public void changeUser(final String userName, final String newPassword) throws SQLException {
        this.checkClosed();
        try {
            this.mc.changeUser(userName, newPassword);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Deprecated
    @Override
    public void clearHasTriedMaster() {
        this.mc.clearHasTriedMaster();
    }
    
    @Override
    public PreparedStatement clientPrepareStatement(final String sql) throws SQLException {
        this.checkClosed();
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.clientPrepareStatement(sql));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement clientPrepareStatement(final String sql, final int autoGenKeyIndex) throws SQLException {
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.clientPrepareStatement(sql, autoGenKeyIndex));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement clientPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.clientPrepareStatement(sql, resultSetType, resultSetConcurrency));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement clientPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.clientPrepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement clientPrepareStatement(final String sql, final int[] autoGenKeyIndexes) throws SQLException {
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.clientPrepareStatement(sql, autoGenKeyIndexes));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement clientPrepareStatement(final String sql, final String[] autoGenKeyColNames) throws SQLException {
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.clientPrepareStatement(sql, autoGenKeyColNames));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public int getActiveStatementCount() {
        return this.mc.getActiveStatementCount();
    }
    
    @Override
    public Log getLog() throws SQLException {
        return this.mc.getLog();
    }
    
    @Deprecated
    @Override
    public String getServerCharacterEncoding() {
        return this.getServerCharset();
    }
    
    @Override
    public String getServerCharset() {
        return this.mc.getServerCharset();
    }
    
    @Override
    public TimeZone getServerTimezoneTZ() {
        return this.mc.getServerTimezoneTZ();
    }
    
    @Override
    public String getStatementComment() {
        return this.mc.getStatementComment();
    }
    
    @Deprecated
    @Override
    public boolean hasTriedMaster() {
        return this.mc.hasTriedMaster();
    }
    
    @Override
    public boolean isAbonormallyLongQuery(final long millisOrNanos) {
        return this.mc.isAbonormallyLongQuery(millisOrNanos);
    }
    
    @Override
    public boolean isNoBackslashEscapesSet() {
        return this.mc.isNoBackslashEscapesSet();
    }
    
    @Override
    public boolean lowerCaseTableNames() {
        return this.mc.lowerCaseTableNames();
    }
    
    @Override
    public boolean parserKnowsUnicode() {
        return this.mc.parserKnowsUnicode();
    }
    
    @Override
    public void reportQueryTime(final long millisOrNanos) {
        this.mc.reportQueryTime(millisOrNanos);
    }
    
    @Override
    public void resetServerState() throws SQLException {
        this.checkClosed();
        try {
            this.mc.resetServerState();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public PreparedStatement serverPrepareStatement(final String sql) throws SQLException {
        this.checkClosed();
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.serverPrepareStatement(sql));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement serverPrepareStatement(final String sql, final int autoGenKeyIndex) throws SQLException {
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.serverPrepareStatement(sql, autoGenKeyIndex));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement serverPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.serverPrepareStatement(sql, resultSetType, resultSetConcurrency));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement serverPrepareStatement(final String sql, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) throws SQLException {
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.serverPrepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement serverPrepareStatement(final String sql, final int[] autoGenKeyIndexes) throws SQLException {
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.serverPrepareStatement(sql, autoGenKeyIndexes));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public PreparedStatement serverPrepareStatement(final String sql, final String[] autoGenKeyColNames) throws SQLException {
        try {
            return PreparedStatementWrapper.getInstance(this, this.pooledConnection, this.mc.serverPrepareStatement(sql, autoGenKeyColNames));
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public void setFailedOver(final boolean flag) {
        this.mc.setFailedOver(flag);
    }
    
    @Deprecated
    @Override
    public void setPreferSlaveDuringFailover(final boolean flag) {
        this.mc.setPreferSlaveDuringFailover(flag);
    }
    
    @Override
    public void setStatementComment(final String comment) {
        this.mc.setStatementComment(comment);
    }
    
    @Override
    public void shutdownServer() throws SQLException {
        this.checkClosed();
        try {
            this.mc.shutdownServer();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public boolean supportsIsolationLevel() {
        return this.mc.supportsIsolationLevel();
    }
    
    @Override
    public boolean supportsQuotedIdentifiers() {
        return this.mc.supportsQuotedIdentifiers();
    }
    
    @Override
    public boolean supportsTransactions() {
        return this.mc.supportsTransactions();
    }
    
    @Override
    public boolean versionMeetsMinimum(final int major, final int minor, final int subminor) throws SQLException {
        this.checkClosed();
        try {
            return this.mc.versionMeetsMinimum(major, minor, subminor);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return false;
        }
    }
    
    @Override
    public String exposeAsXml() throws SQLException {
        this.checkClosed();
        try {
            return this.mc.exposeAsXml();
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
            return null;
        }
    }
    
    @Override
    public boolean getAllowLoadLocalInfile() {
        return this.mc.getAllowLoadLocalInfile();
    }
    
    @Override
    public boolean getAllowMultiQueries() {
        return this.mc.getAllowMultiQueries();
    }
    
    @Override
    public boolean getAllowNanAndInf() {
        return this.mc.getAllowNanAndInf();
    }
    
    @Override
    public boolean getAllowUrlInLocalInfile() {
        return this.mc.getAllowUrlInLocalInfile();
    }
    
    @Override
    public boolean getAlwaysSendSetIsolation() {
        return this.mc.getAlwaysSendSetIsolation();
    }
    
    @Override
    public boolean getAutoClosePStmtStreams() {
        return this.mc.getAutoClosePStmtStreams();
    }
    
    @Override
    public boolean getAutoDeserialize() {
        return this.mc.getAutoDeserialize();
    }
    
    @Override
    public boolean getAutoGenerateTestcaseScript() {
        return this.mc.getAutoGenerateTestcaseScript();
    }
    
    @Override
    public boolean getAutoReconnectForPools() {
        return this.mc.getAutoReconnectForPools();
    }
    
    @Override
    public boolean getAutoSlowLog() {
        return this.mc.getAutoSlowLog();
    }
    
    @Override
    public int getBlobSendChunkSize() {
        return this.mc.getBlobSendChunkSize();
    }
    
    @Override
    public boolean getBlobsAreStrings() {
        return this.mc.getBlobsAreStrings();
    }
    
    @Override
    public boolean getCacheCallableStatements() {
        return this.mc.getCacheCallableStatements();
    }
    
    @Override
    public boolean getCacheCallableStmts() {
        return this.mc.getCacheCallableStmts();
    }
    
    @Override
    public boolean getCachePrepStmts() {
        return this.mc.getCachePrepStmts();
    }
    
    @Override
    public boolean getCachePreparedStatements() {
        return this.mc.getCachePreparedStatements();
    }
    
    @Override
    public boolean getCacheResultSetMetadata() {
        return this.mc.getCacheResultSetMetadata();
    }
    
    @Override
    public boolean getCacheServerConfiguration() {
        return this.mc.getCacheServerConfiguration();
    }
    
    @Override
    public int getCallableStatementCacheSize() {
        return this.mc.getCallableStatementCacheSize();
    }
    
    @Override
    public int getCallableStmtCacheSize() {
        return this.mc.getCallableStmtCacheSize();
    }
    
    @Override
    public boolean getCapitalizeTypeNames() {
        return this.mc.getCapitalizeTypeNames();
    }
    
    @Override
    public String getCharacterSetResults() {
        return this.mc.getCharacterSetResults();
    }
    
    @Override
    public String getClientCertificateKeyStorePassword() {
        return this.mc.getClientCertificateKeyStorePassword();
    }
    
    @Override
    public String getClientCertificateKeyStoreType() {
        return this.mc.getClientCertificateKeyStoreType();
    }
    
    @Override
    public String getClientCertificateKeyStoreUrl() {
        return this.mc.getClientCertificateKeyStoreUrl();
    }
    
    @Override
    public String getClientInfoProvider() {
        return this.mc.getClientInfoProvider();
    }
    
    @Override
    public String getClobCharacterEncoding() {
        return this.mc.getClobCharacterEncoding();
    }
    
    @Override
    public boolean getClobberStreamingResults() {
        return this.mc.getClobberStreamingResults();
    }
    
    @Override
    public int getConnectTimeout() {
        return this.mc.getConnectTimeout();
    }
    
    @Override
    public String getConnectionCollation() {
        return this.mc.getConnectionCollation();
    }
    
    @Override
    public String getConnectionLifecycleInterceptors() {
        return this.mc.getConnectionLifecycleInterceptors();
    }
    
    @Override
    public boolean getContinueBatchOnError() {
        return this.mc.getContinueBatchOnError();
    }
    
    @Override
    public boolean getCreateDatabaseIfNotExist() {
        return this.mc.getCreateDatabaseIfNotExist();
    }
    
    @Override
    public int getDefaultFetchSize() {
        return this.mc.getDefaultFetchSize();
    }
    
    @Override
    public boolean getDontTrackOpenResources() {
        return this.mc.getDontTrackOpenResources();
    }
    
    @Override
    public boolean getDumpMetadataOnColumnNotFound() {
        return this.mc.getDumpMetadataOnColumnNotFound();
    }
    
    @Override
    public boolean getDumpQueriesOnException() {
        return this.mc.getDumpQueriesOnException();
    }
    
    @Override
    public boolean getDynamicCalendars() {
        return this.mc.getDynamicCalendars();
    }
    
    @Override
    public boolean getElideSetAutoCommits() {
        return this.mc.getElideSetAutoCommits();
    }
    
    @Override
    public boolean getEmptyStringsConvertToZero() {
        return this.mc.getEmptyStringsConvertToZero();
    }
    
    @Override
    public boolean getEmulateLocators() {
        return this.mc.getEmulateLocators();
    }
    
    @Override
    public boolean getEmulateUnsupportedPstmts() {
        return this.mc.getEmulateUnsupportedPstmts();
    }
    
    @Override
    public boolean getEnablePacketDebug() {
        return this.mc.getEnablePacketDebug();
    }
    
    @Override
    public boolean getEnableQueryTimeouts() {
        return this.mc.getEnableQueryTimeouts();
    }
    
    @Override
    public String getEncoding() {
        return this.mc.getEncoding();
    }
    
    @Override
    public boolean getExplainSlowQueries() {
        return this.mc.getExplainSlowQueries();
    }
    
    @Override
    public boolean getFailOverReadOnly() {
        return this.mc.getFailOverReadOnly();
    }
    
    @Override
    public boolean getFunctionsNeverReturnBlobs() {
        return this.mc.getFunctionsNeverReturnBlobs();
    }
    
    @Override
    public boolean getGatherPerfMetrics() {
        return this.mc.getGatherPerfMetrics();
    }
    
    @Override
    public boolean getGatherPerformanceMetrics() {
        return this.mc.getGatherPerformanceMetrics();
    }
    
    @Override
    public boolean getGenerateSimpleParameterMetadata() {
        return this.mc.getGenerateSimpleParameterMetadata();
    }
    
    @Override
    public boolean getHoldResultsOpenOverStatementClose() {
        return this.mc.getHoldResultsOpenOverStatementClose();
    }
    
    @Override
    public boolean getIgnoreNonTxTables() {
        return this.mc.getIgnoreNonTxTables();
    }
    
    @Override
    public boolean getIncludeInnodbStatusInDeadlockExceptions() {
        return this.mc.getIncludeInnodbStatusInDeadlockExceptions();
    }
    
    @Override
    public int getInitialTimeout() {
        return this.mc.getInitialTimeout();
    }
    
    @Override
    public boolean getInteractiveClient() {
        return this.mc.getInteractiveClient();
    }
    
    @Override
    public boolean getIsInteractiveClient() {
        return this.mc.getIsInteractiveClient();
    }
    
    @Override
    public boolean getJdbcCompliantTruncation() {
        return this.mc.getJdbcCompliantTruncation();
    }
    
    @Override
    public boolean getJdbcCompliantTruncationForReads() {
        return this.mc.getJdbcCompliantTruncationForReads();
    }
    
    @Override
    public String getLargeRowSizeThreshold() {
        return this.mc.getLargeRowSizeThreshold();
    }
    
    @Override
    public String getLoadBalanceStrategy() {
        return this.mc.getLoadBalanceStrategy();
    }
    
    @Override
    public String getLocalSocketAddress() {
        return this.mc.getLocalSocketAddress();
    }
    
    @Override
    public int getLocatorFetchBufferSize() {
        return this.mc.getLocatorFetchBufferSize();
    }
    
    @Override
    public boolean getLogSlowQueries() {
        return this.mc.getLogSlowQueries();
    }
    
    @Override
    public boolean getLogXaCommands() {
        return this.mc.getLogXaCommands();
    }
    
    @Override
    public String getLogger() {
        return this.mc.getLogger();
    }
    
    @Override
    public String getLoggerClassName() {
        return this.mc.getLoggerClassName();
    }
    
    @Override
    public boolean getMaintainTimeStats() {
        return this.mc.getMaintainTimeStats();
    }
    
    @Override
    public int getMaxQuerySizeToLog() {
        return this.mc.getMaxQuerySizeToLog();
    }
    
    @Override
    public int getMaxReconnects() {
        return this.mc.getMaxReconnects();
    }
    
    @Override
    public int getMaxRows() {
        return this.mc.getMaxRows();
    }
    
    @Override
    public int getMetadataCacheSize() {
        return this.mc.getMetadataCacheSize();
    }
    
    @Override
    public int getNetTimeoutForStreamingResults() {
        return this.mc.getNetTimeoutForStreamingResults();
    }
    
    @Override
    public boolean getNoAccessToProcedureBodies() {
        return this.mc.getNoAccessToProcedureBodies();
    }
    
    @Override
    public boolean getNoDatetimeStringSync() {
        return this.mc.getNoDatetimeStringSync();
    }
    
    @Override
    public boolean getNoTimezoneConversionForTimeType() {
        return this.mc.getNoTimezoneConversionForTimeType();
    }
    
    @Override
    public boolean getNoTimezoneConversionForDateType() {
        return this.mc.getNoTimezoneConversionForDateType();
    }
    
    @Override
    public boolean getCacheDefaultTimezone() {
        return this.mc.getCacheDefaultTimezone();
    }
    
    @Override
    public boolean getNullCatalogMeansCurrent() {
        return this.mc.getNullCatalogMeansCurrent();
    }
    
    @Override
    public boolean getNullNamePatternMatchesAll() {
        return this.mc.getNullNamePatternMatchesAll();
    }
    
    @Override
    public boolean getOverrideSupportsIntegrityEnhancementFacility() {
        return this.mc.getOverrideSupportsIntegrityEnhancementFacility();
    }
    
    @Override
    public int getPacketDebugBufferSize() {
        return this.mc.getPacketDebugBufferSize();
    }
    
    @Override
    public boolean getPadCharsWithSpace() {
        return this.mc.getPadCharsWithSpace();
    }
    
    @Override
    public boolean getParanoid() {
        return this.mc.getParanoid();
    }
    
    @Override
    public boolean getPedantic() {
        return this.mc.getPedantic();
    }
    
    @Override
    public boolean getPinGlobalTxToPhysicalConnection() {
        return this.mc.getPinGlobalTxToPhysicalConnection();
    }
    
    @Override
    public boolean getPopulateInsertRowWithDefaultValues() {
        return this.mc.getPopulateInsertRowWithDefaultValues();
    }
    
    @Override
    public int getPrepStmtCacheSize() {
        return this.mc.getPrepStmtCacheSize();
    }
    
    @Override
    public int getPrepStmtCacheSqlLimit() {
        return this.mc.getPrepStmtCacheSqlLimit();
    }
    
    @Override
    public int getPreparedStatementCacheSize() {
        return this.mc.getPreparedStatementCacheSize();
    }
    
    @Override
    public int getPreparedStatementCacheSqlLimit() {
        return this.mc.getPreparedStatementCacheSqlLimit();
    }
    
    @Override
    public boolean getProcessEscapeCodesForPrepStmts() {
        return this.mc.getProcessEscapeCodesForPrepStmts();
    }
    
    @Override
    public boolean getProfileSQL() {
        return this.mc.getProfileSQL();
    }
    
    @Override
    public boolean getProfileSql() {
        return this.mc.getProfileSql();
    }
    
    @Override
    public String getPropertiesTransform() {
        return this.mc.getPropertiesTransform();
    }
    
    @Override
    public int getQueriesBeforeRetryMaster() {
        return this.mc.getQueriesBeforeRetryMaster();
    }
    
    @Override
    public boolean getReconnectAtTxEnd() {
        return this.mc.getReconnectAtTxEnd();
    }
    
    @Override
    public boolean getRelaxAutoCommit() {
        return this.mc.getRelaxAutoCommit();
    }
    
    @Override
    public int getReportMetricsIntervalMillis() {
        return this.mc.getReportMetricsIntervalMillis();
    }
    
    @Override
    public boolean getRequireSSL() {
        return this.mc.getRequireSSL();
    }
    
    @Override
    public String getResourceId() {
        return this.mc.getResourceId();
    }
    
    @Override
    public int getResultSetSizeThreshold() {
        return this.mc.getResultSetSizeThreshold();
    }
    
    @Override
    public boolean getRewriteBatchedStatements() {
        return this.mc.getRewriteBatchedStatements();
    }
    
    @Override
    public boolean getRollbackOnPooledClose() {
        return this.mc.getRollbackOnPooledClose();
    }
    
    @Override
    public boolean getRoundRobinLoadBalance() {
        return this.mc.getRoundRobinLoadBalance();
    }
    
    @Override
    public boolean getRunningCTS13() {
        return this.mc.getRunningCTS13();
    }
    
    @Override
    public int getSecondsBeforeRetryMaster() {
        return this.mc.getSecondsBeforeRetryMaster();
    }
    
    @Override
    public String getServerTimezone() {
        return this.mc.getServerTimezone();
    }
    
    @Override
    public String getSessionVariables() {
        return this.mc.getSessionVariables();
    }
    
    @Override
    public int getSlowQueryThresholdMillis() {
        return this.mc.getSlowQueryThresholdMillis();
    }
    
    @Override
    public long getSlowQueryThresholdNanos() {
        return this.mc.getSlowQueryThresholdNanos();
    }
    
    @Override
    public String getSocketFactory() {
        return this.mc.getSocketFactory();
    }
    
    @Override
    public String getSocketFactoryClassName() {
        return this.mc.getSocketFactoryClassName();
    }
    
    @Override
    public int getSocketTimeout() {
        return this.mc.getSocketTimeout();
    }
    
    @Override
    public String getStatementInterceptors() {
        return this.mc.getStatementInterceptors();
    }
    
    @Override
    public boolean getStrictFloatingPoint() {
        return this.mc.getStrictFloatingPoint();
    }
    
    @Override
    public boolean getStrictUpdates() {
        return this.mc.getStrictUpdates();
    }
    
    @Override
    public boolean getSupportLobLocator() {
        return this.mc.getSupportLobLocator();
    }
    
    @Override
    public boolean getTcpKeepAlive() {
        return this.mc.getTcpKeepAlive();
    }
    
    @Override
    public boolean getTcpNoDelay() {
        return this.mc.getTcpNoDelay();
    }
    
    @Override
    public int getTcpRcvBuf() {
        return this.mc.getTcpRcvBuf();
    }
    
    @Override
    public int getTcpSndBuf() {
        return this.mc.getTcpSndBuf();
    }
    
    @Override
    public int getTcpTrafficClass() {
        return this.mc.getTcpTrafficClass();
    }
    
    @Override
    public boolean getTinyInt1isBit() {
        return this.mc.getTinyInt1isBit();
    }
    
    @Override
    public boolean getTraceProtocol() {
        return this.mc.getTraceProtocol();
    }
    
    @Override
    public boolean getTransformedBitIsBoolean() {
        return this.mc.getTransformedBitIsBoolean();
    }
    
    @Override
    public boolean getTreatUtilDateAsTimestamp() {
        return this.mc.getTreatUtilDateAsTimestamp();
    }
    
    @Override
    public String getTrustCertificateKeyStorePassword() {
        return this.mc.getTrustCertificateKeyStorePassword();
    }
    
    @Override
    public String getTrustCertificateKeyStoreType() {
        return this.mc.getTrustCertificateKeyStoreType();
    }
    
    @Override
    public String getTrustCertificateKeyStoreUrl() {
        return this.mc.getTrustCertificateKeyStoreUrl();
    }
    
    @Override
    public boolean getUltraDevHack() {
        return this.mc.getUltraDevHack();
    }
    
    @Override
    public boolean getUseBlobToStoreUTF8OutsideBMP() {
        return this.mc.getUseBlobToStoreUTF8OutsideBMP();
    }
    
    @Override
    public boolean getUseCompression() {
        return this.mc.getUseCompression();
    }
    
    @Override
    public boolean getUseObChecksum() {
        return this.mc.getUseObChecksum();
    }
    
    @Override
    public boolean getUseOceanBaseProtocolV20() {
        return this.mc.getUseOceanBaseProtocolV20();
    }
    
    @Override
    public String getUseConfigs() {
        return this.mc.getUseConfigs();
    }
    
    @Override
    public boolean getUseCursorFetch() {
        return this.mc.getUseCursorFetch();
    }
    
    @Override
    public boolean getUseDirectRowUnpack() {
        return this.mc.getUseDirectRowUnpack();
    }
    
    @Override
    public boolean getUseDynamicCharsetInfo() {
        return this.mc.getUseDynamicCharsetInfo();
    }
    
    @Override
    public boolean getUseFastDateParsing() {
        return this.mc.getUseFastDateParsing();
    }
    
    @Override
    public boolean getUseFastIntParsing() {
        return this.mc.getUseFastIntParsing();
    }
    
    @Override
    public boolean getUseGmtMillisForDatetimes() {
        return this.mc.getUseGmtMillisForDatetimes();
    }
    
    @Override
    public boolean getUseHostsInPrivileges() {
        return this.mc.getUseHostsInPrivileges();
    }
    
    @Override
    public boolean getUseInformationSchema() {
        return this.mc.getUseInformationSchema();
    }
    
    @Override
    public boolean getUseJDBCCompliantTimezoneShift() {
        return this.mc.getUseJDBCCompliantTimezoneShift();
    }
    
    @Override
    public boolean getUseJvmCharsetConverters() {
        return this.mc.getUseJvmCharsetConverters();
    }
    
    @Override
    public boolean getUseLocalSessionState() {
        return this.mc.getUseLocalSessionState();
    }
    
    @Override
    public boolean getUseNanosForElapsedTime() {
        return this.mc.getUseNanosForElapsedTime();
    }
    
    @Override
    public boolean getUseOldAliasMetadataBehavior() {
        return this.mc.getUseOldAliasMetadataBehavior();
    }
    
    @Override
    public boolean getUseOldUTF8Behavior() {
        return this.mc.getUseOldUTF8Behavior();
    }
    
    @Override
    public boolean getUseOnlyServerErrorMessages() {
        return this.mc.getUseOnlyServerErrorMessages();
    }
    
    @Override
    public boolean getUseReadAheadInput() {
        return this.mc.getUseReadAheadInput();
    }
    
    @Override
    public boolean getUseSSL() {
        return this.mc.getUseSSL();
    }
    
    @Override
    public boolean getUseSSPSCompatibleTimezoneShift() {
        return this.mc.getUseSSPSCompatibleTimezoneShift();
    }
    
    @Override
    public boolean getUseServerPrepStmts() {
        return this.mc.getUseServerPrepStmts();
    }
    
    @Override
    public boolean getUseServerPreparedStmts() {
        return this.mc.getUseServerPreparedStmts();
    }
    
    @Override
    public boolean getUseSqlStateCodes() {
        return this.mc.getUseSqlStateCodes();
    }
    
    @Override
    public boolean getUseStreamLengthsInPrepStmts() {
        return this.mc.getUseStreamLengthsInPrepStmts();
    }
    
    @Override
    public boolean getUseTimezone() {
        return this.mc.getUseTimezone();
    }
    
    @Override
    public boolean getUseUltraDevWorkAround() {
        return this.mc.getUseUltraDevWorkAround();
    }
    
    @Override
    public boolean getUseUnbufferedInput() {
        return this.mc.getUseUnbufferedInput();
    }
    
    @Override
    public boolean getUseUnicode() {
        return this.mc.getUseUnicode();
    }
    
    @Override
    public boolean getUseUsageAdvisor() {
        return this.mc.getUseUsageAdvisor();
    }
    
    @Override
    public String getUtf8OutsideBmpExcludedColumnNamePattern() {
        return this.mc.getUtf8OutsideBmpExcludedColumnNamePattern();
    }
    
    @Override
    public String getUtf8OutsideBmpIncludedColumnNamePattern() {
        return this.mc.getUtf8OutsideBmpIncludedColumnNamePattern();
    }
    
    @Override
    public boolean getYearIsDateType() {
        return this.mc.getYearIsDateType();
    }
    
    @Override
    public String getZeroDateTimeBehavior() {
        return this.mc.getZeroDateTimeBehavior();
    }
    
    @Override
    public void setAllowLoadLocalInfile(final boolean property) {
        this.mc.setAllowLoadLocalInfile(property);
    }
    
    @Override
    public void setAllowMultiQueries(final boolean property) {
        this.mc.setAllowMultiQueries(property);
    }
    
    @Override
    public void setAllowNanAndInf(final boolean flag) {
        this.mc.setAllowNanAndInf(flag);
    }
    
    @Override
    public void setAllowUrlInLocalInfile(final boolean flag) {
        this.mc.setAllowUrlInLocalInfile(flag);
    }
    
    @Override
    public void setAlwaysSendSetIsolation(final boolean flag) {
        this.mc.setAlwaysSendSetIsolation(flag);
    }
    
    @Override
    public void setAutoClosePStmtStreams(final boolean flag) {
        this.mc.setAutoClosePStmtStreams(flag);
    }
    
    @Override
    public void setAutoDeserialize(final boolean flag) {
        this.mc.setAutoDeserialize(flag);
    }
    
    @Override
    public void setAutoGenerateTestcaseScript(final boolean flag) {
        this.mc.setAutoGenerateTestcaseScript(flag);
    }
    
    @Override
    public void setAutoReconnect(final boolean flag) {
        this.mc.setAutoReconnect(flag);
    }
    
    @Override
    public void setAutoReconnectForConnectionPools(final boolean property) {
        this.mc.setAutoReconnectForConnectionPools(property);
    }
    
    @Override
    public void setAutoReconnectForPools(final boolean flag) {
        this.mc.setAutoReconnectForPools(flag);
    }
    
    @Override
    public void setAutoSlowLog(final boolean flag) {
        this.mc.setAutoSlowLog(flag);
    }
    
    @Override
    public void setBlobSendChunkSize(final String value) throws SQLException {
        this.mc.setBlobSendChunkSize(value);
    }
    
    @Override
    public void setBlobsAreStrings(final boolean flag) {
        this.mc.setBlobsAreStrings(flag);
    }
    
    @Override
    public void setCacheCallableStatements(final boolean flag) {
        this.mc.setCacheCallableStatements(flag);
    }
    
    @Override
    public void setCacheCallableStmts(final boolean flag) {
        this.mc.setCacheCallableStmts(flag);
    }
    
    @Override
    public void setCachePrepStmts(final boolean flag) {
        this.mc.setCachePrepStmts(flag);
    }
    
    @Override
    public void setCachePreparedStatements(final boolean flag) {
        this.mc.setCachePreparedStatements(flag);
    }
    
    @Override
    public void setCacheResultSetMetadata(final boolean property) {
        this.mc.setCacheResultSetMetadata(property);
    }
    
    @Override
    public void setCacheServerConfiguration(final boolean flag) {
        this.mc.setCacheServerConfiguration(flag);
    }
    
    @Override
    public void setCallableStatementCacheSize(final int size) throws SQLException {
        this.mc.setCallableStatementCacheSize(size);
    }
    
    @Override
    public void setCallableStmtCacheSize(final int cacheSize) throws SQLException {
        this.mc.setCallableStmtCacheSize(cacheSize);
    }
    
    @Override
    public void setCapitalizeDBMDTypes(final boolean property) {
        this.mc.setCapitalizeDBMDTypes(property);
    }
    
    @Override
    public void setCapitalizeTypeNames(final boolean flag) {
        this.mc.setCapitalizeTypeNames(flag);
    }
    
    @Override
    public void setCharacterEncoding(final String encoding) {
        this.mc.setCharacterEncoding(encoding);
    }
    
    @Override
    public void setCharacterSetResults(final String characterSet) {
        this.mc.setCharacterSetResults(characterSet);
    }
    
    @Override
    public void setClientCertificateKeyStorePassword(final String value) {
        this.mc.setClientCertificateKeyStorePassword(value);
    }
    
    @Override
    public void setClientCertificateKeyStoreType(final String value) {
        this.mc.setClientCertificateKeyStoreType(value);
    }
    
    @Override
    public void setClientCertificateKeyStoreUrl(final String value) {
        this.mc.setClientCertificateKeyStoreUrl(value);
    }
    
    @Override
    public void setClientInfoProvider(final String classname) {
        this.mc.setClientInfoProvider(classname);
    }
    
    @Override
    public void setClobCharacterEncoding(final String encoding) {
        this.mc.setClobCharacterEncoding(encoding);
    }
    
    @Override
    public void setClobberStreamingResults(final boolean flag) {
        this.mc.setClobberStreamingResults(flag);
    }
    
    @Override
    public void setConnectTimeout(final int timeoutMs) throws SQLException {
        this.mc.setConnectTimeout(timeoutMs);
    }
    
    @Override
    public void setConnectionCollation(final String collation) {
        this.mc.setConnectionCollation(collation);
    }
    
    @Override
    public void setConnectionLifecycleInterceptors(final String interceptors) {
        this.mc.setConnectionLifecycleInterceptors(interceptors);
    }
    
    @Override
    public void setContinueBatchOnError(final boolean property) {
        this.mc.setContinueBatchOnError(property);
    }
    
    @Override
    public void setCreateDatabaseIfNotExist(final boolean flag) {
        this.mc.setCreateDatabaseIfNotExist(flag);
    }
    
    @Override
    public void setDefaultFetchSize(final int n) throws SQLException {
        this.mc.setDefaultFetchSize(n);
    }
    
    @Override
    public void setDetectServerPreparedStmts(final boolean property) {
        this.mc.setDetectServerPreparedStmts(property);
    }
    
    @Override
    public void setDontTrackOpenResources(final boolean flag) {
        this.mc.setDontTrackOpenResources(flag);
    }
    
    @Override
    public void setDumpMetadataOnColumnNotFound(final boolean flag) {
        this.mc.setDumpMetadataOnColumnNotFound(flag);
    }
    
    @Override
    public void setDumpQueriesOnException(final boolean flag) {
        this.mc.setDumpQueriesOnException(flag);
    }
    
    @Override
    public void setDynamicCalendars(final boolean flag) {
        this.mc.setDynamicCalendars(flag);
    }
    
    @Override
    public void setElideSetAutoCommits(final boolean flag) {
        this.mc.setElideSetAutoCommits(flag);
    }
    
    @Override
    public void setEmptyStringsConvertToZero(final boolean flag) {
        this.mc.setEmptyStringsConvertToZero(flag);
    }
    
    @Override
    public void setEmulateLocators(final boolean property) {
        this.mc.setEmulateLocators(property);
    }
    
    @Override
    public void setEmulateUnsupportedPstmts(final boolean flag) {
        this.mc.setEmulateUnsupportedPstmts(flag);
    }
    
    @Override
    public void setEnablePacketDebug(final boolean flag) {
        this.mc.setEnablePacketDebug(flag);
    }
    
    @Override
    public void setEnableQueryTimeouts(final boolean flag) {
        this.mc.setEnableQueryTimeouts(flag);
    }
    
    @Override
    public void setEncoding(final String property) {
        this.mc.setEncoding(property);
    }
    
    @Override
    public void setExplainSlowQueries(final boolean flag) {
        this.mc.setExplainSlowQueries(flag);
    }
    
    @Override
    public void setFailOverReadOnly(final boolean flag) {
        this.mc.setFailOverReadOnly(flag);
    }
    
    @Override
    public void setFunctionsNeverReturnBlobs(final boolean flag) {
        this.mc.setFunctionsNeverReturnBlobs(flag);
    }
    
    @Override
    public void setGatherPerfMetrics(final boolean flag) {
        this.mc.setGatherPerfMetrics(flag);
    }
    
    @Override
    public void setGatherPerformanceMetrics(final boolean flag) {
        this.mc.setGatherPerformanceMetrics(flag);
    }
    
    @Override
    public void setGenerateSimpleParameterMetadata(final boolean flag) {
        this.mc.setGenerateSimpleParameterMetadata(flag);
    }
    
    @Override
    public void setHoldResultsOpenOverStatementClose(final boolean flag) {
        this.mc.setHoldResultsOpenOverStatementClose(flag);
    }
    
    @Override
    public void setIgnoreNonTxTables(final boolean property) {
        this.mc.setIgnoreNonTxTables(property);
    }
    
    @Override
    public void setIncludeInnodbStatusInDeadlockExceptions(final boolean flag) {
        this.mc.setIncludeInnodbStatusInDeadlockExceptions(flag);
    }
    
    @Override
    public void setInitialTimeout(final int property) throws SQLException {
        this.mc.setInitialTimeout(property);
    }
    
    @Override
    public void setInteractiveClient(final boolean property) {
        this.mc.setInteractiveClient(property);
    }
    
    @Override
    public void setIsInteractiveClient(final boolean property) {
        this.mc.setIsInteractiveClient(property);
    }
    
    @Override
    public void setJdbcCompliantTruncation(final boolean flag) {
        this.mc.setJdbcCompliantTruncation(flag);
    }
    
    @Override
    public void setJdbcCompliantTruncationForReads(final boolean jdbcCompliantTruncationForReads) {
        this.mc.setJdbcCompliantTruncationForReads(jdbcCompliantTruncationForReads);
    }
    
    @Override
    public void setLargeRowSizeThreshold(final String value) throws SQLException {
        this.mc.setLargeRowSizeThreshold(value);
    }
    
    @Override
    public void setLoadBalanceStrategy(final String strategy) {
        this.mc.setLoadBalanceStrategy(strategy);
    }
    
    @Override
    public void setLocalSocketAddress(final String address) {
        this.mc.setLocalSocketAddress(address);
    }
    
    @Override
    public void setLocatorFetchBufferSize(final String value) throws SQLException {
        this.mc.setLocatorFetchBufferSize(value);
    }
    
    @Override
    public void setLogSlowQueries(final boolean flag) {
        this.mc.setLogSlowQueries(flag);
    }
    
    @Override
    public void setLogXaCommands(final boolean flag) {
        this.mc.setLogXaCommands(flag);
    }
    
    @Override
    public void setLogger(final String property) {
        this.mc.setLogger(property);
    }
    
    @Override
    public void setLoggerClassName(final String className) {
        this.mc.setLoggerClassName(className);
    }
    
    @Override
    public void setMaintainTimeStats(final boolean flag) {
        this.mc.setMaintainTimeStats(flag);
    }
    
    @Override
    public void setMaxQuerySizeToLog(final int sizeInBytes) throws SQLException {
        this.mc.setMaxQuerySizeToLog(sizeInBytes);
    }
    
    @Override
    public void setMaxReconnects(final int property) throws SQLException {
        this.mc.setMaxReconnects(property);
    }
    
    @Override
    public void setMaxRows(final int property) throws SQLException {
        this.mc.setMaxRows(property);
    }
    
    @Override
    public void setMetadataCacheSize(final int value) throws SQLException {
        this.mc.setMetadataCacheSize(value);
    }
    
    @Override
    public void setNetTimeoutForStreamingResults(final int value) throws SQLException {
        this.mc.setNetTimeoutForStreamingResults(value);
    }
    
    @Override
    public void setNoAccessToProcedureBodies(final boolean flag) {
        this.mc.setNoAccessToProcedureBodies(flag);
    }
    
    @Override
    public void setNoDatetimeStringSync(final boolean flag) {
        this.mc.setNoDatetimeStringSync(flag);
    }
    
    @Override
    public void setNoTimezoneConversionForTimeType(final boolean flag) {
        this.mc.setNoTimezoneConversionForTimeType(flag);
    }
    
    @Override
    public void setNoTimezoneConversionForDateType(final boolean flag) {
        this.mc.setNoTimezoneConversionForDateType(flag);
    }
    
    @Override
    public void setCacheDefaultTimezone(final boolean flag) {
        this.mc.setCacheDefaultTimezone(flag);
    }
    
    @Override
    public void setNullCatalogMeansCurrent(final boolean value) {
        this.mc.setNullCatalogMeansCurrent(value);
    }
    
    @Override
    public void setNullNamePatternMatchesAll(final boolean value) {
        this.mc.setNullNamePatternMatchesAll(value);
    }
    
    @Override
    public void setOverrideSupportsIntegrityEnhancementFacility(final boolean flag) {
        this.mc.setOverrideSupportsIntegrityEnhancementFacility(flag);
    }
    
    @Override
    public void setPacketDebugBufferSize(final int size) throws SQLException {
        this.mc.setPacketDebugBufferSize(size);
    }
    
    @Override
    public void setPadCharsWithSpace(final boolean flag) {
        this.mc.setPadCharsWithSpace(flag);
    }
    
    @Override
    public void setParanoid(final boolean property) {
        this.mc.setParanoid(property);
    }
    
    @Override
    public void setPedantic(final boolean property) {
        this.mc.setPedantic(property);
    }
    
    @Override
    public void setPinGlobalTxToPhysicalConnection(final boolean flag) {
        this.mc.setPinGlobalTxToPhysicalConnection(flag);
    }
    
    @Override
    public void setPopulateInsertRowWithDefaultValues(final boolean flag) {
        this.mc.setPopulateInsertRowWithDefaultValues(flag);
    }
    
    @Override
    public void setPrepStmtCacheSize(final int cacheSize) throws SQLException {
        this.mc.setPrepStmtCacheSize(cacheSize);
    }
    
    @Override
    public void setPrepStmtCacheSqlLimit(final int sqlLimit) throws SQLException {
        this.mc.setPrepStmtCacheSqlLimit(sqlLimit);
    }
    
    @Override
    public void setPreparedStatementCacheSize(final int cacheSize) throws SQLException {
        this.mc.setPreparedStatementCacheSize(cacheSize);
    }
    
    @Override
    public void setPreparedStatementCacheSqlLimit(final int cacheSqlLimit) throws SQLException {
        this.mc.setPreparedStatementCacheSqlLimit(cacheSqlLimit);
    }
    
    @Override
    public void setProcessEscapeCodesForPrepStmts(final boolean flag) {
        this.mc.setProcessEscapeCodesForPrepStmts(flag);
    }
    
    @Override
    public void setProfileSQL(final boolean flag) {
        this.mc.setProfileSQL(flag);
    }
    
    @Override
    public void setProfileSql(final boolean property) {
        this.mc.setProfileSql(property);
    }
    
    @Override
    public void setPropertiesTransform(final String value) {
        this.mc.setPropertiesTransform(value);
    }
    
    @Override
    public void setQueriesBeforeRetryMaster(final int property) throws SQLException {
        this.mc.setQueriesBeforeRetryMaster(property);
    }
    
    @Override
    public void setReconnectAtTxEnd(final boolean property) {
        this.mc.setReconnectAtTxEnd(property);
    }
    
    @Override
    public void setRelaxAutoCommit(final boolean property) {
        this.mc.setRelaxAutoCommit(property);
    }
    
    @Override
    public void setReportMetricsIntervalMillis(final int millis) throws SQLException {
        this.mc.setReportMetricsIntervalMillis(millis);
    }
    
    @Override
    public void setRequireSSL(final boolean property) {
        this.mc.setRequireSSL(property);
    }
    
    @Override
    public void setResourceId(final String resourceId) {
        this.mc.setResourceId(resourceId);
    }
    
    @Override
    public void setResultSetSizeThreshold(final int threshold) throws SQLException {
        this.mc.setResultSetSizeThreshold(threshold);
    }
    
    @Override
    public void setRetainStatementAfterResultSetClose(final boolean flag) {
        this.mc.setRetainStatementAfterResultSetClose(flag);
    }
    
    @Override
    public void setRewriteBatchedStatements(final boolean flag) {
        this.mc.setRewriteBatchedStatements(flag);
    }
    
    @Override
    public void setRollbackOnPooledClose(final boolean flag) {
        this.mc.setRollbackOnPooledClose(flag);
    }
    
    @Override
    public void setRoundRobinLoadBalance(final boolean flag) {
        this.mc.setRoundRobinLoadBalance(flag);
    }
    
    @Override
    public void setRunningCTS13(final boolean flag) {
        this.mc.setRunningCTS13(flag);
    }
    
    @Override
    public void setSecondsBeforeRetryMaster(final int property) throws SQLException {
        this.mc.setSecondsBeforeRetryMaster(property);
    }
    
    @Override
    public void setServerTimezone(final String property) {
        this.mc.setServerTimezone(property);
    }
    
    @Override
    public void setSessionVariables(final String variables) {
        this.mc.setSessionVariables(variables);
    }
    
    @Override
    public void setSlowQueryThresholdMillis(final int millis) throws SQLException {
        this.mc.setSlowQueryThresholdMillis(millis);
    }
    
    @Override
    public void setSlowQueryThresholdNanos(final long nanos) throws SQLException {
        this.mc.setSlowQueryThresholdNanos(nanos);
    }
    
    @Override
    public void setSocketFactory(final String name) {
        this.mc.setSocketFactory(name);
    }
    
    @Override
    public void setSocketFactoryClassName(final String property) {
        this.mc.setSocketFactoryClassName(property);
    }
    
    @Override
    public void setSocketTimeout(final int property) throws SQLException {
        this.mc.setSocketTimeout(property);
    }
    
    @Override
    public void setStatementInterceptors(final String value) {
        this.mc.setStatementInterceptors(value);
    }
    
    @Override
    public void setStrictFloatingPoint(final boolean property) {
        this.mc.setStrictFloatingPoint(property);
    }
    
    @Override
    public void setStrictUpdates(final boolean property) {
        this.mc.setStrictUpdates(property);
    }
    
    @Override
    public void setSupportLobLocator(final boolean property) {
        this.mc.setSupportLobLocator(property);
    }
    
    @Override
    public void setTcpKeepAlive(final boolean flag) {
        this.mc.setTcpKeepAlive(flag);
    }
    
    @Override
    public void setTcpNoDelay(final boolean flag) {
        this.mc.setTcpNoDelay(flag);
    }
    
    @Override
    public void setTcpRcvBuf(final int bufSize) throws SQLException {
        this.mc.setTcpRcvBuf(bufSize);
    }
    
    @Override
    public void setTcpSndBuf(final int bufSize) throws SQLException {
        this.mc.setTcpSndBuf(bufSize);
    }
    
    @Override
    public void setTcpTrafficClass(final int classFlags) throws SQLException {
        this.mc.setTcpTrafficClass(classFlags);
    }
    
    @Override
    public void setTinyInt1isBit(final boolean flag) {
        this.mc.setTinyInt1isBit(flag);
    }
    
    @Override
    public void setTraceProtocol(final boolean flag) {
        this.mc.setTraceProtocol(flag);
    }
    
    @Override
    public void setTransformedBitIsBoolean(final boolean flag) {
        this.mc.setTransformedBitIsBoolean(flag);
    }
    
    @Override
    public void setTreatUtilDateAsTimestamp(final boolean flag) {
        this.mc.setTreatUtilDateAsTimestamp(flag);
    }
    
    @Override
    public void setTrustCertificateKeyStorePassword(final String value) {
        this.mc.setTrustCertificateKeyStorePassword(value);
    }
    
    @Override
    public void setTrustCertificateKeyStoreType(final String value) {
        this.mc.setTrustCertificateKeyStoreType(value);
    }
    
    @Override
    public void setTrustCertificateKeyStoreUrl(final String value) {
        this.mc.setTrustCertificateKeyStoreUrl(value);
    }
    
    @Override
    public void setUltraDevHack(final boolean flag) {
        this.mc.setUltraDevHack(flag);
    }
    
    @Override
    public void setUseBlobToStoreUTF8OutsideBMP(final boolean flag) {
        this.mc.setUseBlobToStoreUTF8OutsideBMP(flag);
    }
    
    @Override
    public void setUseCompression(final boolean property) {
        this.mc.setUseCompression(property);
    }
    
    @Override
    public void setUseObChecksum(final boolean property) {
        this.mc.setUseObChecksum(property);
    }
    
    @Override
    public void setUseOceanBaseProtocolV20(final boolean property) {
        this.mc.setUseOceanBaseProtocolV20(property);
    }
    
    @Override
    public void setUseConfigs(final String configs) {
        this.mc.setUseConfigs(configs);
    }
    
    @Override
    public void setUseCursorFetch(final boolean flag) {
        this.mc.setUseCursorFetch(flag);
    }
    
    @Override
    public void setUseDirectRowUnpack(final boolean flag) {
        this.mc.setUseDirectRowUnpack(flag);
    }
    
    @Override
    public void setUseDynamicCharsetInfo(final boolean flag) {
        this.mc.setUseDynamicCharsetInfo(flag);
    }
    
    @Override
    public void setUseFastDateParsing(final boolean flag) {
        this.mc.setUseFastDateParsing(flag);
    }
    
    @Override
    public void setUseFastIntParsing(final boolean flag) {
        this.mc.setUseFastIntParsing(flag);
    }
    
    @Override
    public void setUseGmtMillisForDatetimes(final boolean flag) {
        this.mc.setUseGmtMillisForDatetimes(flag);
    }
    
    @Override
    public void setUseHostsInPrivileges(final boolean property) {
        this.mc.setUseHostsInPrivileges(property);
    }
    
    @Override
    public void setUseInformationSchema(final boolean flag) {
        this.mc.setUseInformationSchema(flag);
    }
    
    @Override
    public void setUseJDBCCompliantTimezoneShift(final boolean flag) {
        this.mc.setUseJDBCCompliantTimezoneShift(flag);
    }
    
    @Override
    public void setUseJvmCharsetConverters(final boolean flag) {
        this.mc.setUseJvmCharsetConverters(flag);
    }
    
    @Override
    public void setUseLocalSessionState(final boolean flag) {
        this.mc.setUseLocalSessionState(flag);
    }
    
    @Override
    public void setUseNanosForElapsedTime(final boolean flag) {
        this.mc.setUseNanosForElapsedTime(flag);
    }
    
    @Override
    public void setUseOldAliasMetadataBehavior(final boolean flag) {
        this.mc.setUseOldAliasMetadataBehavior(flag);
    }
    
    @Override
    public void setUseOldUTF8Behavior(final boolean flag) {
        this.mc.setUseOldUTF8Behavior(flag);
    }
    
    @Override
    public void setUseOnlyServerErrorMessages(final boolean flag) {
        this.mc.setUseOnlyServerErrorMessages(flag);
    }
    
    @Override
    public void setUseReadAheadInput(final boolean flag) {
        this.mc.setUseReadAheadInput(flag);
    }
    
    @Override
    public void setUseSSL(final boolean property) {
        this.mc.setUseSSL(property);
    }
    
    @Override
    public void setUseSSPSCompatibleTimezoneShift(final boolean flag) {
        this.mc.setUseSSPSCompatibleTimezoneShift(flag);
    }
    
    @Override
    public void setUseServerPrepStmts(final boolean flag) {
        this.mc.setUseServerPrepStmts(flag);
    }
    
    @Override
    public void setUseServerPreparedStmts(final boolean flag) {
        this.mc.setUseServerPreparedStmts(flag);
    }
    
    @Override
    public void setUseSqlStateCodes(final boolean flag) {
        this.mc.setUseSqlStateCodes(flag);
    }
    
    @Override
    public void setUseStreamLengthsInPrepStmts(final boolean property) {
        this.mc.setUseStreamLengthsInPrepStmts(property);
    }
    
    @Override
    public void setUseTimezone(final boolean property) {
        this.mc.setUseTimezone(property);
    }
    
    @Override
    public void setUseUltraDevWorkAround(final boolean property) {
        this.mc.setUseUltraDevWorkAround(property);
    }
    
    @Override
    public void setUseUnbufferedInput(final boolean flag) {
        this.mc.setUseUnbufferedInput(flag);
    }
    
    @Override
    public void setUseUnicode(final boolean flag) {
        this.mc.setUseUnicode(flag);
    }
    
    @Override
    public void setUseUsageAdvisor(final boolean useUsageAdvisorFlag) {
        this.mc.setUseUsageAdvisor(useUsageAdvisorFlag);
    }
    
    @Override
    public void setUtf8OutsideBmpExcludedColumnNamePattern(final String regexPattern) {
        this.mc.setUtf8OutsideBmpExcludedColumnNamePattern(regexPattern);
    }
    
    @Override
    public void setUtf8OutsideBmpIncludedColumnNamePattern(final String regexPattern) {
        this.mc.setUtf8OutsideBmpIncludedColumnNamePattern(regexPattern);
    }
    
    @Override
    public void setYearIsDateType(final boolean flag) {
        this.mc.setYearIsDateType(flag);
    }
    
    @Override
    public void setZeroDateTimeBehavior(final String behavior) {
        this.mc.setZeroDateTimeBehavior(behavior);
    }
    
    @Override
    public boolean useUnbufferedInput() {
        return this.mc.useUnbufferedInput();
    }
    
    @Override
    public void initializeExtension(final Extension ex) throws SQLException {
        this.mc.initializeExtension(ex);
    }
    
    @Override
    public String getProfilerEventHandler() {
        return this.mc.getProfilerEventHandler();
    }
    
    @Override
    public void setProfilerEventHandler(final String handler) {
        this.mc.setProfilerEventHandler(handler);
    }
    
    @Override
    public boolean getVerifyServerCertificate() {
        return this.mc.getVerifyServerCertificate();
    }
    
    @Override
    public void setVerifyServerCertificate(final boolean flag) {
        this.mc.setVerifyServerCertificate(flag);
    }
    
    @Override
    public boolean getUseLegacyDatetimeCode() {
        return this.mc.getUseLegacyDatetimeCode();
    }
    
    @Override
    public void setUseLegacyDatetimeCode(final boolean flag) {
        this.mc.setUseLegacyDatetimeCode(flag);
    }
    
    @Override
    public boolean getSendFractionalSeconds() {
        return this.mc.getSendFractionalSeconds();
    }
    
    @Override
    public void setSendFractionalSeconds(final boolean flag) {
        this.mc.setSendFractionalSeconds(flag);
    }
    
    @Override
    public int getSelfDestructOnPingMaxOperations() {
        return this.mc.getSelfDestructOnPingMaxOperations();
    }
    
    @Override
    public int getSelfDestructOnPingSecondsLifetime() {
        return this.mc.getSelfDestructOnPingSecondsLifetime();
    }
    
    @Override
    public void setSelfDestructOnPingMaxOperations(final int maxOperations) throws SQLException {
        this.mc.setSelfDestructOnPingMaxOperations(maxOperations);
    }
    
    @Override
    public void setSelfDestructOnPingSecondsLifetime(final int seconds) throws SQLException {
        this.mc.setSelfDestructOnPingSecondsLifetime(seconds);
    }
    
    @Override
    public boolean getUseColumnNamesInFindColumn() {
        return this.mc.getUseColumnNamesInFindColumn();
    }
    
    @Override
    public void setUseColumnNamesInFindColumn(final boolean flag) {
        this.mc.setUseColumnNamesInFindColumn(flag);
    }
    
    @Override
    public boolean getUseLocalTransactionState() {
        return this.mc.getUseLocalTransactionState();
    }
    
    @Override
    public void setUseLocalTransactionState(final boolean flag) {
        this.mc.setUseLocalTransactionState(flag);
    }
    
    @Override
    public boolean getCompensateOnDuplicateKeyUpdateCounts() {
        return this.mc.getCompensateOnDuplicateKeyUpdateCounts();
    }
    
    @Override
    public void setCompensateOnDuplicateKeyUpdateCounts(final boolean flag) {
        this.mc.setCompensateOnDuplicateKeyUpdateCounts(flag);
    }
    
    @Override
    public boolean getUseAffectedRows() {
        return this.mc.getUseAffectedRows();
    }
    
    @Override
    public void setUseAffectedRows(final boolean flag) {
        this.mc.setUseAffectedRows(flag);
    }
    
    @Override
    public String getPasswordCharacterEncoding() {
        return this.mc.getPasswordCharacterEncoding();
    }
    
    @Override
    public void setPasswordCharacterEncoding(final String characterSet) {
        this.mc.setPasswordCharacterEncoding(characterSet);
    }
    
    @Override
    public int getAutoIncrementIncrement() {
        return this.mc.getAutoIncrementIncrement();
    }
    
    @Override
    public int getLoadBalanceBlacklistTimeout() {
        return this.mc.getLoadBalanceBlacklistTimeout();
    }
    
    @Override
    public void setLoadBalanceBlacklistTimeout(final int loadBalanceBlacklistTimeout) throws SQLException {
        this.mc.setLoadBalanceBlacklistTimeout(loadBalanceBlacklistTimeout);
    }
    
    @Override
    public int getLoadBalancePingTimeout() {
        return this.mc.getLoadBalancePingTimeout();
    }
    
    @Override
    public void setLoadBalancePingTimeout(final int loadBalancePingTimeout) throws SQLException {
        this.mc.setLoadBalancePingTimeout(loadBalancePingTimeout);
    }
    
    @Override
    public boolean getLoadBalanceValidateConnectionOnSwapServer() {
        return this.mc.getLoadBalanceValidateConnectionOnSwapServer();
    }
    
    @Override
    public void setLoadBalanceValidateConnectionOnSwapServer(final boolean loadBalanceValidateConnectionOnSwapServer) {
        this.mc.setLoadBalanceValidateConnectionOnSwapServer(loadBalanceValidateConnectionOnSwapServer);
    }
    
    @Override
    public void setRetriesAllDown(final int retriesAllDown) throws SQLException {
        this.mc.setRetriesAllDown(retriesAllDown);
    }
    
    @Override
    public int getRetriesAllDown() {
        return this.mc.getRetriesAllDown();
    }
    
    @Override
    public ExceptionInterceptor getExceptionInterceptor() {
        return this.pooledConnection.getExceptionInterceptor();
    }
    
    @Override
    public String getExceptionInterceptors() {
        return this.mc.getExceptionInterceptors();
    }
    
    @Override
    public void setExceptionInterceptors(final String exceptionInterceptors) {
        this.mc.setExceptionInterceptors(exceptionInterceptors);
    }
    
    @Override
    public boolean getQueryTimeoutKillsConnection() {
        return this.mc.getQueryTimeoutKillsConnection();
    }
    
    @Override
    public void setQueryTimeoutKillsConnection(final boolean queryTimeoutKillsConnection) {
        this.mc.setQueryTimeoutKillsConnection(queryTimeoutKillsConnection);
    }
    
    @Override
    public boolean hasSameProperties(final Connection c) {
        return this.mc.hasSameProperties(c);
    }
    
    @Override
    public Properties getProperties() {
        return this.mc.getProperties();
    }
    
    @Override
    public String getHost() {
        return this.mc.getHost();
    }
    
    @Override
    public void setProxy(final MySQLConnection conn) {
        this.mc.setProxy(conn);
    }
    
    @Override
    public boolean getRetainStatementAfterResultSetClose() {
        return this.mc.getRetainStatementAfterResultSetClose();
    }
    
    @Override
    public int getMaxAllowedPacket() {
        return this.mc.getMaxAllowedPacket();
    }
    
    @Override
    public String getLoadBalanceConnectionGroup() {
        return this.mc.getLoadBalanceConnectionGroup();
    }
    
    @Override
    public boolean getLoadBalanceEnableJMX() {
        return this.mc.getLoadBalanceEnableJMX();
    }
    
    @Override
    public String getLoadBalanceExceptionChecker() {
        return this.mc.getLoadBalanceExceptionChecker();
    }
    
    @Override
    public String getLoadBalanceSQLExceptionSubclassFailover() {
        return this.mc.getLoadBalanceSQLExceptionSubclassFailover();
    }
    
    @Override
    public String getLoadBalanceSQLStateFailover() {
        return this.mc.getLoadBalanceSQLStateFailover();
    }
    
    @Override
    public void setLoadBalanceConnectionGroup(final String loadBalanceConnectionGroup) {
        this.mc.setLoadBalanceConnectionGroup(loadBalanceConnectionGroup);
    }
    
    @Override
    public void setLoadBalanceEnableJMX(final boolean loadBalanceEnableJMX) {
        this.mc.setLoadBalanceEnableJMX(loadBalanceEnableJMX);
    }
    
    @Override
    public void setLoadBalanceExceptionChecker(final String loadBalanceExceptionChecker) {
        this.mc.setLoadBalanceExceptionChecker(loadBalanceExceptionChecker);
    }
    
    @Override
    public void setLoadBalanceSQLExceptionSubclassFailover(final String loadBalanceSQLExceptionSubclassFailover) {
        this.mc.setLoadBalanceSQLExceptionSubclassFailover(loadBalanceSQLExceptionSubclassFailover);
    }
    
    @Override
    public void setLoadBalanceSQLStateFailover(final String loadBalanceSQLStateFailover) {
        this.mc.setLoadBalanceSQLStateFailover(loadBalanceSQLStateFailover);
    }
    
    @Override
    public String getLoadBalanceAutoCommitStatementRegex() {
        return this.mc.getLoadBalanceAutoCommitStatementRegex();
    }
    
    @Override
    public int getLoadBalanceAutoCommitStatementThreshold() {
        return this.mc.getLoadBalanceAutoCommitStatementThreshold();
    }
    
    @Override
    public void setLoadBalanceAutoCommitStatementRegex(final String loadBalanceAutoCommitStatementRegex) {
        this.mc.setLoadBalanceAutoCommitStatementRegex(loadBalanceAutoCommitStatementRegex);
    }
    
    @Override
    public void setLoadBalanceAutoCommitStatementThreshold(final int loadBalanceAutoCommitStatementThreshold) throws SQLException {
        this.mc.setLoadBalanceAutoCommitStatementThreshold(loadBalanceAutoCommitStatementThreshold);
    }
    
    @Override
    public void setLoadBalanceHostRemovalGracePeriod(final int loadBalanceHostRemovalGracePeriod) throws SQLException {
        this.mc.setLoadBalanceHostRemovalGracePeriod(loadBalanceHostRemovalGracePeriod);
    }
    
    @Override
    public int getLoadBalanceHostRemovalGracePeriod() {
        return this.mc.getLoadBalanceHostRemovalGracePeriod();
    }
    
    @Override
    public void setTypeMap(final Map<String, Class<?>> map) throws SQLException {
        this.checkClosed();
        try {
            this.mc.setTypeMap(map);
        }
        catch (SQLException sqlException) {
            this.checkAndFireConnectionError(sqlException);
        }
    }
    
    @Override
    public boolean getIncludeThreadDumpInDeadlockExceptions() {
        return this.mc.getIncludeThreadDumpInDeadlockExceptions();
    }
    
    @Override
    public void setIncludeThreadDumpInDeadlockExceptions(final boolean flag) {
        this.mc.setIncludeThreadDumpInDeadlockExceptions(flag);
    }
    
    @Override
    public boolean getIncludeThreadNamesAsStatementComment() {
        return this.mc.getIncludeThreadNamesAsStatementComment();
    }
    
    @Override
    public void setIncludeThreadNamesAsStatementComment(final boolean flag) {
        this.mc.setIncludeThreadNamesAsStatementComment(flag);
    }
    
    @Override
    public boolean isServerLocal() throws SQLException {
        return this.mc.isServerLocal();
    }
    
    @Override
    public void setAuthenticationPlugins(final String authenticationPlugins) {
        this.mc.setAuthenticationPlugins(authenticationPlugins);
    }
    
    @Override
    public String getAuthenticationPlugins() {
        return this.mc.getAuthenticationPlugins();
    }
    
    @Override
    public void setDisabledAuthenticationPlugins(final String disabledAuthenticationPlugins) {
        this.mc.setDisabledAuthenticationPlugins(disabledAuthenticationPlugins);
    }
    
    @Override
    public String getDisabledAuthenticationPlugins() {
        return this.mc.getDisabledAuthenticationPlugins();
    }
    
    @Override
    public void setDefaultAuthenticationPlugin(final String defaultAuthenticationPlugin) {
        this.mc.setDefaultAuthenticationPlugin(defaultAuthenticationPlugin);
    }
    
    @Override
    public String getDefaultAuthenticationPlugin() {
        return this.mc.getDefaultAuthenticationPlugin();
    }
    
    @Override
    public void setParseInfoCacheFactory(final String factoryClassname) {
        this.mc.setParseInfoCacheFactory(factoryClassname);
    }
    
    @Override
    public String getParseInfoCacheFactory() {
        return this.mc.getParseInfoCacheFactory();
    }
    
    @Override
    public void setSchema(final String schema) throws SQLException {
        this.mc.setSchema(schema);
    }
    
    @Override
    public String getSchema() throws SQLException {
        return this.mc.getSchema();
    }
    
    @Override
    public void abort(final Executor executor) throws SQLException {
        this.mc.abort(executor);
    }
    
    @Override
    public void setNetworkTimeout(final Executor executor, final int milliseconds) throws SQLException {
        this.mc.setNetworkTimeout(executor, milliseconds);
    }
    
    @Override
    public int getNetworkTimeout() throws SQLException {
        return this.mc.getNetworkTimeout();
    }
    
    @Override
    public void setServerConfigCacheFactory(final String factoryClassname) {
        this.mc.setServerConfigCacheFactory(factoryClassname);
    }
    
    @Override
    public String getServerConfigCacheFactory() {
        return this.mc.getServerConfigCacheFactory();
    }
    
    @Override
    public void setDisconnectOnExpiredPasswords(final boolean disconnectOnExpiredPasswords) {
        this.mc.setDisconnectOnExpiredPasswords(disconnectOnExpiredPasswords);
    }
    
    @Override
    public boolean getDisconnectOnExpiredPasswords() {
        return this.mc.getDisconnectOnExpiredPasswords();
    }
    
    @Override
    public void setGetProceduresReturnsFunctions(final boolean getProcedureReturnsFunctions) {
        this.mc.setGetProceduresReturnsFunctions(getProcedureReturnsFunctions);
    }
    
    @Override
    public boolean getGetProceduresReturnsFunctions() {
        return this.mc.getGetProceduresReturnsFunctions();
    }
    
    @Override
    public void abortInternal() throws SQLException {
        this.mc.abortInternal();
    }
    
    @Override
    public Object getConnectionMutex() {
        return this.mc.getConnectionMutex();
    }
    
    @Override
    public boolean getAllowMasterDownConnections() {
        return this.mc.getAllowMasterDownConnections();
    }
    
    @Override
    public void setAllowMasterDownConnections(final boolean connectIfMasterDown) {
        this.mc.setAllowMasterDownConnections(connectIfMasterDown);
    }
    
    @Override
    public boolean getAllowSlaveDownConnections() {
        return this.mc.getAllowSlaveDownConnections();
    }
    
    @Override
    public void setAllowSlaveDownConnections(final boolean connectIfSlaveDown) {
        this.mc.setAllowSlaveDownConnections(connectIfSlaveDown);
    }
    
    @Override
    public boolean getReadFromMasterWhenNoSlaves() {
        return this.mc.getReadFromMasterWhenNoSlaves();
    }
    
    @Override
    public void setReadFromMasterWhenNoSlaves(final boolean useMasterIfSlavesDown) {
        this.mc.setReadFromMasterWhenNoSlaves(useMasterIfSlavesDown);
    }
    
    @Override
    public boolean getReplicationEnableJMX() {
        return this.mc.getReplicationEnableJMX();
    }
    
    @Override
    public void setReplicationEnableJMX(final boolean replicationEnableJMX) {
        this.mc.setReplicationEnableJMX(replicationEnableJMX);
    }
    
    @Override
    public String getConnectionAttributes() throws SQLException {
        return this.mc.getConnectionAttributes();
    }
    
    @Override
    public void setDetectCustomCollations(final boolean detectCustomCollations) {
        this.mc.setDetectCustomCollations(detectCustomCollations);
    }
    
    @Override
    public boolean getDetectCustomCollations() {
        return this.mc.getDetectCustomCollations();
    }
    
    @Override
    public int getSessionMaxRows() {
        return this.mc.getSessionMaxRows();
    }
    
    @Override
    public void setSessionMaxRows(final int max) throws SQLException {
        this.mc.setSessionMaxRows(max);
    }
    
    @Override
    public String getServerRSAPublicKeyFile() {
        return this.mc.getServerRSAPublicKeyFile();
    }
    
    @Override
    public void setServerRSAPublicKeyFile(final String serverRSAPublicKeyFile) throws SQLException {
        this.mc.setServerRSAPublicKeyFile(serverRSAPublicKeyFile);
    }
    
    @Override
    public boolean getAllowPublicKeyRetrieval() {
        return this.mc.getAllowPublicKeyRetrieval();
    }
    
    @Override
    public void setAllowPublicKeyRetrieval(final boolean allowPublicKeyRetrieval) throws SQLException {
        this.mc.setAllowPublicKeyRetrieval(allowPublicKeyRetrieval);
    }
    
    @Override
    public void setDontCheckOnDuplicateKeyUpdateInSQL(final boolean dontCheckOnDuplicateKeyUpdateInSQL) {
        this.mc.setDontCheckOnDuplicateKeyUpdateInSQL(dontCheckOnDuplicateKeyUpdateInSQL);
    }
    
    @Override
    public boolean getDontCheckOnDuplicateKeyUpdateInSQL() {
        return this.mc.getDontCheckOnDuplicateKeyUpdateInSQL();
    }
    
    @Override
    public void setSocksProxyHost(final String socksProxyHost) {
        this.mc.setSocksProxyHost(socksProxyHost);
    }
    
    @Override
    public String getSocksProxyHost() {
        return this.mc.getSocksProxyHost();
    }
    
    @Override
    public void setSocksProxyPort(final int socksProxyPort) throws SQLException {
        this.mc.setSocksProxyPort(socksProxyPort);
    }
    
    @Override
    public int getSocksProxyPort() {
        return this.mc.getSocksProxyPort();
    }
    
    @Override
    public boolean getReadOnlyPropagatesToServer() {
        return this.mc.getReadOnlyPropagatesToServer();
    }
    
    @Override
    public void setReadOnlyPropagatesToServer(final boolean flag) {
        this.mc.setReadOnlyPropagatesToServer(flag);
    }
    
    @Override
    public String getEnabledSSLCipherSuites() {
        return this.mc.getEnabledSSLCipherSuites();
    }
    
    @Override
    public void setEnabledSSLCipherSuites(final String cipherSuites) {
        this.mc.setEnabledSSLCipherSuites(cipherSuites);
    }
    
    @Override
    public boolean getEnableEscapeProcessing() {
        return this.mc.getEnableEscapeProcessing();
    }
    
    @Override
    public void setEnableEscapeProcessing(final boolean flag) {
        this.mc.setEnableEscapeProcessing(flag);
    }
    
    @Override
    public boolean getAllowAlwaysSendParamTypes() {
        return this.mc.getAllowAlwaysSendParamTypes();
    }
    
    @Override
    public boolean getUseFormatExceptionMessage() {
        return this.mc.getUseFormatExceptionMessage();
    }
    
    @Override
    public void setUseFormatExceptionMessage(final boolean flag) {
        this.mc.setUseFormatExceptionMessage(flag);
    }
    
    @Override
    public void setAllowAlwaysSendParamTypes(final boolean flag) {
        this.mc.setAllowAlwaysSendParamTypes(flag);
    }
    
    @Override
    public boolean getUseServerPsStmtChecksum() {
        return this.mc.getUseServerPsStmtChecksum();
    }
    
    @Override
    public void setUseServerPsStmtChecksum(final boolean flag) {
        this.mc.setUseServerPsStmtChecksum(flag);
    }
    
    @Override
    public void setUseSqlStringCache(final boolean flag) {
        this.mc.setUseSqlStringCache(flag);
    }
    
    @Override
    public boolean getUseSqlStringCache() {
        return this.mc.getUseSqlStringCache();
    }
    
    @Override
    public int getComplexDataCacheSize() {
        return this.mc.getComplexDataCacheSize();
    }
    
    @Override
    public void setComplexDataCacheSize(final int value) throws SQLException {
        this.mc.setComplexDataCacheSize(value);
    }
    
    @Override
    public boolean getCacheComplexData() {
        return this.mc.getCacheComplexData();
    }
    
    @Override
    public void setCacheComplexData(final boolean flag) {
        this.mc.setCacheComplexData(flag);
    }
    
    @Override
    public boolean isUseSSLExplicit() {
        return this.mc.isUseSSLExplicit();
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }
    
    static {
        if (Util.isJdbc4()) {
            try {
                JDBC_4_CONNECTION_WRAPPER_CTOR = Class.forName("com.alipay.oceanbase.jdbc.jdbc2.optional.JDBC4ConnectionWrapper").getConstructor(MysqlPooledConnection.class, Connection.class, Boolean.TYPE);
                return;
            }
            catch (SecurityException e) {
                throw new RuntimeException(e);
            }
            catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            }
            catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_CONNECTION_WRAPPER_CTOR = null;
    }
}
