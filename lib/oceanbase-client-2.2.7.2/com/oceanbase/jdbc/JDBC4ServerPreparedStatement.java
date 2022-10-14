// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import java.sql.ResultSet;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.internal.util.dao.ClientPrepareResult;
import java.sql.ResultSetMetaData;
import java.sql.ParameterMetaData;
import java.io.UnsupportedEncodingException;
import java.sql.Statement;
import com.oceanbase.jdbc.util.ObCrc32C;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.Utils;
import java.util.Collections;
import java.util.TreeMap;
import java.util.ArrayList;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import java.util.List;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import java.util.Map;
import com.oceanbase.jdbc.internal.logging.Logger;

public class JDBC4ServerPreparedStatement extends BasePrepareStatement implements Cloneable
{
    private static final Logger logger;
    protected Map<Integer, ParameterHolder> currentParameterHolder;
    protected List<ParameterHolder[]> queryParameters;
    protected ServerPrepareResult serverPrepareResult;
    private boolean isObFunction;
    private boolean sendTypesToServer;
    private boolean mustExecuteOnMaster;
    private int iterationCount;
    private int executeMode;
    private long checksum;
    private OceanBaseResultSetMetaData metadata;
    private OceanBaseParameterMetaData parameterMetaData;
    
    public JDBC4ServerPreparedStatement(final boolean isObFunction, final OceanBaseConnection connection, final String sql, final int resultSetScrollType, final int resultSetConcurrency, final int autoGeneratedKeys, final ExceptionFactory exceptionFactory) throws SQLException {
        super(connection, resultSetScrollType, resultSetConcurrency, autoGeneratedKeys, exceptionFactory);
        this.queryParameters = new ArrayList<ParameterHolder[]>();
        this.serverPrepareResult = null;
        this.isObFunction = false;
        this.sendTypesToServer = false;
        this.executeMode = 0;
        this.checksum = 1L;
        this.isObFunction = isObFunction;
        this.currentParameterHolder = Collections.synchronizedMap(new TreeMap<Integer, ParameterHolder>());
        this.mustExecuteOnMaster = this.protocol.isMasterConnection();
        this.originalSql = sql;
        if (!this.protocol.isOracleMode()) {
            this.simpleSql = Utils.trimSQLString(this.originalSql, this.protocol.noBackslashEscapes(), false, true);
            if (!this.simpleSql.startsWith("CALL") && !this.simpleSql.startsWith("call")) {
                this.prepare(this.originalSql);
            }
        }
        else {
            final String[] tmp = Utils.trimSQLStringInternal(this.originalSql, this.protocol.noBackslashEscapes(), true, false);
            this.simpleSql = tmp[0];
            if (this.protocol.supportStmtPrepareExecute()) {
                this.parameterCount = Integer.parseInt(tmp[1]);
            }
            this.prepare(this.originalSql);
        }
        this.sqlType = Utils.getStatementType(this.simpleSql);
    }
    
    @Override
    public JDBC4ServerPreparedStatement clone(final OceanBaseConnection connection) throws CloneNotSupportedException {
        final JDBC4ServerPreparedStatement clone = (JDBC4ServerPreparedStatement)super.clone(connection);
        clone.metadata = this.metadata;
        clone.parameterMetaData = this.parameterMetaData;
        clone.queryParameters = new ArrayList<ParameterHolder[]>();
        clone.mustExecuteOnMaster = this.mustExecuteOnMaster;
        try {
            clone.originalSql = this.originalSql;
            if (!clone.protocol.isOracleMode()) {
                if (!this.simpleSql.startsWith("CALL") && !this.simpleSql.startsWith("call")) {
                    clone.prepare(this.originalSql);
                }
            }
            else {
                if (clone.protocol.supportStmtPrepareExecute()) {
                    clone.parameterCount = this.parameterCount;
                }
                clone.prepare(this.originalSql);
            }
            clone.sqlType = Utils.getStatementType(this.simpleSql);
        }
        catch (SQLException e) {
            throw new CloneNotSupportedException("PrepareStatement not ");
        }
        return clone;
    }
    
    private void prepare(final String sql) throws SQLException {
        try {
            final ObCrc32C crc32C = new ObCrc32C();
            crc32C.reset();
            byte[] b;
            try {
                b = sql.getBytes(this.options.characterEncoding);
            }
            catch (UnsupportedEncodingException e) {
                try {
                    this.close();
                }
                catch (Exception ex) {}
                final SQLException sqlException = new SQLException("sql string getBytes error" + e.getMessage());
                JDBC4ServerPreparedStatement.logger.error("error preparing query", sqlException);
                throw this.exceptionFactory.raiseStatementError(this.connection, this).create(sqlException);
            }
            crc32C.update(b, 0, b.length);
            this.checksum = crc32C.getValue();
            if (!this.protocol.supportStmtPrepareExecute()) {
                this.serverPrepareResult = this.protocol.prepare(sql, this.mustExecuteOnMaster);
                this.setMetaFromResult();
            }
        }
        catch (SQLException e2) {
            try {
                this.close();
            }
            catch (Exception ex2) {}
            JDBC4ServerPreparedStatement.logger.error("error preparing query", e2);
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create(e2);
        }
    }
    
    private void setMetaFromResult() {
        this.parameterCount = this.serverPrepareResult.getParameters().length;
        this.metadata = new OceanBaseResultSetMetaData(this.serverPrepareResult.getColumns(), this.protocol.getUrlParser().getOptions(), false, this.protocol.isOracleMode());
        this.parameterMetaData = new OceanBaseParameterMetaData(this.serverPrepareResult.getParameters());
    }
    
    @Override
    public void setParameter(final int parameterIndex, final ParameterHolder holder) throws SQLException {
        this.currentParameterHolder.put(parameterIndex - 1, holder);
    }
    
    protected int getParameterCount() {
        return this.parameterCount;
    }
    
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return this.parameterMetaData;
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return this.metadata;
    }
    
    @Override
    public void clearParameters() {
        this.currentParameterHolder.clear();
    }
    
    protected void validParameters() throws SQLException {
        if (!(this instanceof JDBC4ServerCallableStatement)) {
            for (int i = 0; i < this.parameterCount; ++i) {
                if (this.currentParameterHolder.get(i) == null) {
                    JDBC4ServerPreparedStatement.logger.error("Parameter at position {} is not set", (Object)(i + 1));
                    throw this.exceptionFactory.raiseStatementError(this.connection, this).create("Parameter at position " + (i + 1) + " is not set", "07004");
                }
            }
        }
    }
    
    @Override
    public void addBatch() throws SQLException {
        this.validParameters();
        this.queryParameters.add(this.currentParameterHolder.values().toArray(new ParameterHolder[0]));
    }
    
    @Override
    public void addBatch(final String sql) throws SQLException {
        throw this.exceptionFactory.raiseStatementError(this.connection, this).create("Cannot do addBatch(String) on preparedStatement");
    }
    
    @Override
    public void clearBatch() {
        this.queryParameters.clear();
        this.hasLongData = false;
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        this.checkClose();
        final int queryParameterSize = this.queryParameters.size();
        if (queryParameterSize == 0) {
            return new int[0];
        }
        this.executeBatchInternal(queryParameterSize);
        return this.results.getCmdInformation().getUpdateCounts();
    }
    
    @Override
    public long[] executeLargeBatch() throws SQLException {
        this.checkClose();
        final int queryParameterSize = this.queryParameters.size();
        if (queryParameterSize == 0) {
            return new long[0];
        }
        this.executeBatchInternal(queryParameterSize);
        return this.results.getCmdInformation().getLargeUpdateCounts();
    }
    
    boolean hasLongData(final ParameterHolder[] parameterHolders) {
        if (parameterHolders == null) {
            return false;
        }
        for (final ParameterHolder var : parameterHolders) {
            if (var.isLongData()) {
                return true;
            }
        }
        return false;
    }
    
    private void executeBatchInternal(int queryParameterSize) throws SQLException {
        this.lock.lock();
        this.executing = true;
        try {
            this.executeQueryPrologue(this.serverPrepareResult);
            if (this.queryTimeout != 0) {
                this.setTimerTask(true);
            }
            int remainParameterSize = queryParameterSize;
            int currentTurnParamSize = queryParameterSize;
            boolean continueRewrite = true;
            final int parameterCountReal = this.parameterCount;
            int preIndex = 0;
            ParameterHolder[] currentQueryParameters = null;
            final String curString = this.originalSql;
            boolean isInsert = false;
            while (continueRewrite) {
                if (this.options.rewriteBatchedStatements) {
                    if (remainParameterSize * parameterCountReal > this.options.maxBatchTotalParamsNum) {
                        currentTurnParamSize = this.options.maxBatchTotalParamsNum / parameterCountReal;
                        remainParameterSize -= currentTurnParamSize;
                    }
                    else {
                        continueRewrite = false;
                        currentTurnParamSize = remainParameterSize;
                        remainParameterSize = 0;
                    }
                    String sqlString = curString;
                    if (this.protocol.isOracleMode() && this.options.supportNameBinding) {
                        sqlString = Utils.trimSQLString(curString, this.protocol.noBackslashEscapes(), true, true);
                    }
                    final List<String> list = ClientPrepareResult.rewritablePartsInsertSql(sqlString, false, this.protocol.isOracleMode(), this.options.characterEncoding);
                    if (list != null) {
                        final StringBuilder sb = new StringBuilder();
                        sb.append(list.get(0));
                        sb.append(list.get(1));
                        for (int i = 0; i < parameterCountReal; ++i) {
                            sb.append('?');
                            sb.append(list.get(i + 2));
                        }
                        for (int remain = currentTurnParamSize - 1; remain != 0; --remain) {
                            sb.append(',');
                            sb.append(list.get(1));
                            for (int j = 0; j < parameterCountReal; ++j) {
                                sb.append('?');
                                sb.append(list.get(j + 2));
                            }
                        }
                        sb.append(list.get(list.size() - 1));
                        final int total = currentTurnParamSize * parameterCountReal;
                        final ParameterHolder[] allParams = new ParameterHolder[total];
                        int cur = 0;
                        for (int counter = preIndex; counter < currentTurnParamSize + preIndex; ++counter) {
                            final ParameterHolder[] parameterHolder = this.queryParameters.get(counter);
                            for (int k = 0; k < parameterHolder.length; ++k) {
                                allParams[cur++] = parameterHolder[k];
                            }
                        }
                        queryParameterSize = 1;
                        preIndex += currentTurnParamSize;
                        currentQueryParameters = allParams;
                        this.prepare(this.originalSql = sb.toString());
                        isInsert = true;
                    }
                    else {
                        isInsert = false;
                    }
                }
                else {
                    continueRewrite = false;
                }
                this.results = new Results(this, 0, true, currentTurnParamSize, true, this.resultSetScrollType, this.resultSetConcurrency, this.autoGeneratedKeys, this.protocol.getAutoIncrementIncrement(), null, null);
                if (this.protocol.supportStmtPrepareExecute()) {
                    this.iterationCount = 1;
                    this.executeMode |= 0x1;
                    if (this.parameterCount > 0) {
                        this.sendTypesToServer = true;
                    }
                    this.protocol.setComStmtPrepareExecuteField(this.iterationCount, this.executeMode, this.checksum);
                }
                else {
                    this.protocol.setChecksum(this.checksum);
                }
                if (this.options.useBatchMultiSend || this.options.useBulkStmts) {
                    this.serverPrepareResult = this.protocol.executeBatchServer(this.serverPrepareResult, this.results, this.originalSql, this.queryParameters, this.hasLongData);
                    if (this.results.getBatchSucceed()) {
                        if (this.metadata == null) {
                            this.setMetaFromResult();
                        }
                        this.protocol.resetChecksum();
                        this.results.commandEnd();
                        return;
                    }
                }
                SQLException exception = null;
                if (this.options.rewriteBatchedStatements && isInsert) {
                    final ParameterHolder[] parameterHolder2 = currentQueryParameters;
                    try {
                        if (this.queryTimeout > 0) {
                            this.protocol.stopIfInterrupted();
                        }
                        if (this.serverPrepareResult != null) {
                            this.serverPrepareResult.resetParameterTypeHeader();
                        }
                        if (this.protocol.supportStmtPrepareExecute()) {
                            this.serverPrepareResult = this.protocol.executePreparedQuery(parameterHolder2.length, parameterHolder2, this.serverPrepareResult, this.results);
                            if (!this.hasLongData(parameterHolder2)) {
                                this.serverPrepareResult = null;
                            }
                        }
                        else {
                            this.protocol.executePreparedQuery(this.mustExecuteOnMaster, this.serverPrepareResult, this.results, parameterHolder2);
                        }
                    }
                    catch (SQLException queryException) {
                        if (!this.options.continueBatchOnError) {
                            throw queryException;
                        }
                        if (exception == null) {
                            exception = queryException;
                        }
                    }
                }
                else {
                    for (int counter2 = 0; counter2 < queryParameterSize; ++counter2) {
                        final ParameterHolder[] parameterHolder3 = this.queryParameters.get(counter2);
                        try {
                            if (this.queryTimeout > 0) {
                                this.protocol.stopIfInterrupted();
                            }
                            if (this.serverPrepareResult != null) {
                                this.serverPrepareResult.resetParameterTypeHeader();
                            }
                            if (this.protocol.supportStmtPrepareExecute()) {
                                this.serverPrepareResult = this.protocol.executePreparedQuery(parameterCountReal, parameterHolder3, this.serverPrepareResult, this.results);
                            }
                            else {
                                this.protocol.executePreparedQuery(this.mustExecuteOnMaster, this.serverPrepareResult, this.results, parameterHolder3);
                            }
                        }
                        catch (SQLException queryException2) {
                            if (!this.options.continueBatchOnError) {
                                throw queryException2;
                            }
                            if (exception == null) {
                                exception = queryException2;
                            }
                        }
                    }
                }
                if (exception != null) {
                    throw exception;
                }
                this.protocol.resetChecksum();
                this.results.commandEnd();
            }
        }
        catch (SQLException initialSqlEx) {
            throw this.executeBatchExceptionEpilogue(initialSqlEx, queryParameterSize);
        }
        finally {
            this.executeBatchEpilogue();
            this.lock.unlock();
        }
    }
    
    private void executeQueryPrologue(final ServerPrepareResult serverPrepareResult) throws SQLException {
        this.executing = true;
        if (this.closed) {
            throw this.exceptionFactory.raiseStatementError(this.connection, this).create("execute() is called on closed statement");
        }
        this.protocol.prologProxy(serverPrepareResult, this.maxRows, this.protocol.getProxy() != null, this.connection, this);
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        if (!this.execute()) {
            return JDBC4ResultSet.createEmptyResultSet();
        }
        if (this.results == null) {
            return JDBC4ResultSet.createEmptyResultSet();
        }
        if (this.results.getCallableResultSet() != null) {
            return this.results.getCallableResultSet();
        }
        return this.results.getResultSet();
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        if (this.execute()) {
            return 0;
        }
        return this.getUpdateCount();
    }
    
    @Override
    public boolean execute() throws SQLException {
        if (this.protocol.supportStmtPrepareExecute()) {
            return this.prepareExecuteInternal(this.getFetchSize());
        }
        return this.executeInternal(this.getFetchSize());
    }
    
    @Override
    protected boolean executeInternal(final int fetchSize) throws SQLException {
        this.validParameters();
        this.lock.lock();
        try {
            this.executeQueryPrologue(this.serverPrepareResult);
            if (this.queryTimeout != 0) {
                this.setTimerTask(false);
            }
            final ParameterHolder[] parameterHolders = this.currentParameterHolder.values().toArray(new ParameterHolder[0]);
            this.results = new Results(this, fetchSize, false, 1, true, this.resultSetScrollType, this.resultSetConcurrency, this.autoGeneratedKeys, this.protocol.getAutoIncrementIncrement(), this.originalSql, parameterHolders);
            this.protocol.setChecksum(this.checksum);
            this.serverPrepareResult.resetParameterTypeHeader();
            this.protocol.executePreparedQuery(this.mustExecuteOnMaster, this.serverPrepareResult, this.results, parameterHolders);
            this.protocol.resetChecksum();
            this.results.commandEnd();
            return this.results.getCallableResultSet() != null || this.results.getResultSet() != null;
        }
        catch (SQLException exception) {
            throw this.executeExceptionEpilogue(exception);
        }
        finally {
            this.executeEpilogue();
            this.lock.unlock();
        }
    }
    
    protected boolean prepareExecuteInternal(final int fetchSize) throws SQLException {
        if (this.sqlType == 1) {
            this.iterationCount = (this.isFetchSizeSet ? fetchSize : 10);
            if (this.resultSetScrollType == 1004) {
                this.executeMode |= 0x8;
            }
        }
        else {
            this.iterationCount = 1;
        }
        this.protocol.setComStmtPrepareExecuteField(this.iterationCount, this.executeMode, this.checksum);
        if (this.parameterCount > 0) {
            this.sendTypesToServer = true;
        }
        this.validParameters();
        this.lock.lock();
        try {
            this.executeQueryPrologue(this.serverPrepareResult);
            if (this.queryTimeout != 0) {
                this.setTimerTask(false);
            }
            final ParameterHolder[] parameterHolders = this.currentParameterHolder.values().toArray(new ParameterHolder[0]);
            this.results = new Results(this, fetchSize, false, 1, true, this.resultSetScrollType, this.resultSetConcurrency, this.autoGeneratedKeys, this.protocol.getAutoIncrementIncrement(), this.originalSql, parameterHolders);
            if (this.serverPrepareResult != null) {
                this.serverPrepareResult.resetParameterTypeHeader();
                this.results.setStatementId(this.serverPrepareResult.getStatementId());
            }
            this.serverPrepareResult = this.protocol.executePreparedQuery(this.parameterCount, parameterHolders, this.serverPrepareResult, this.results);
            if (this.metadata == null) {
                this.setMetaFromResult();
            }
            this.protocol.resetChecksum();
            this.results.commandEnd();
            return this.results.getCallableResultSet() != null || this.results.getResultSet() != null;
        }
        catch (SQLException exception) {
            throw this.executeExceptionEpilogue(exception);
        }
        finally {
            this.executeEpilogue();
            this.lock.unlock();
        }
    }
    
    public ColumnDefinition[] cursorFetch(final int cursorId, final int fetchSize) throws SQLException {
        final ColumnDefinition[] ci = this.protocol.sendFechRowViaCursor(this.serverPrepareResult, cursorId, fetchSize, this.results);
        this.results.commandEnd();
        return ci;
    }
    
    @Override
    public void close() throws SQLException {
        if (this.protocol != null && this.serverPrepareResult != null) {
            try {
                this.serverPrepareResult.getUnProxiedProtocol().releasePrepareStatement(this.serverPrepareResult);
            }
            catch (SQLException ex) {}
        }
        this.lock.lock();
        try {
            this.closed = true;
            if (this.results != null) {
                if (this.results.getFetchSize() != 0) {
                    this.skipMoreResults();
                }
                this.results.close();
            }
            if (this.connection == null || this.connection.pooledConnection == null || this.connection.pooledConnection.noStmtEventListeners()) {
                return;
            }
            this.connection.pooledConnection.fireStatementClosed(this);
        }
        finally {
            this.protocol = null;
            this.connection = null;
            this.lock.unlock();
        }
    }
    
    @Override
    public void realClose() throws SQLException {
        if (this.protocol != null && this.serverPrepareResult != null) {
            try {
                this.serverPrepareResult.getUnProxiedProtocol().releasePrepareStatement(this.serverPrepareResult);
            }
            catch (SQLException ex) {}
        }
        this.lock.lock();
        try {
            this.closed = true;
            if (this.connection == null || this.connection.pooledConnection == null || this.connection.pooledConnection.noStmtEventListeners()) {
                return;
            }
            this.connection.pooledConnection.fireStatementClosed(this);
        }
        finally {
            this.protocol = null;
            this.connection = null;
            this.lock.unlock();
        }
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("sql : '" + this.originalSql + "'");
        if (this.parameterCount > 0) {
            sb.append(", parameters : [");
            for (int i = 0; i < this.parameterCount; ++i) {
                final ParameterHolder holder = this.currentParameterHolder.get(i);
                if (holder == null) {
                    sb.append("null");
                }
                else {
                    sb.append(holder.toString());
                }
                if (i != this.parameterCount - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }
    
    public long getServerThreadId() {
        return this.serverPrepareResult.getUnProxiedProtocol().getServerThreadId();
    }
    
    static {
        logger = LoggerFactory.getLogger(JDBC4ServerPreparedStatement.class);
    }
}