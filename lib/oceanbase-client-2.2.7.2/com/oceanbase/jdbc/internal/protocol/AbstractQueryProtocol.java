// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol;

import java.util.LinkedHashMap;
import java.util.Collection;
import java.util.HashSet;
import java.util.Arrays;
import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import java.sql.SQLTransientConnectionException;
import com.oceanbase.jdbc.internal.util.exceptions.MaxAllowedPacketException;
import java.util.concurrent.ExecutionException;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.OceanBaseStatement;
import com.oceanbase.jdbc.OceanBaseConnection;
import com.oceanbase.jdbc.internal.com.read.resultset.SelectResultSet;
import com.oceanbase.jdbc.internal.com.read.resultset.CursorResultSet;
import com.oceanbase.jdbc.internal.com.read.resultset.UpdatableResultSet;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.net.URL;
import java.util.ServiceLoader;
import com.oceanbase.jdbc.LocalInfileInterceptor;
import java.nio.charset.StandardCharsets;
import com.oceanbase.jdbc.internal.util.Utils;
import com.oceanbase.jdbc.internal.com.read.ErrorPacket;
import com.oceanbase.jdbc.internal.com.send.SendChangeDbPacket;
import java.net.SocketException;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.sql.SQLNonTransientConnectionException;
import com.oceanbase.jdbc.internal.com.send.SendHandshakeResponsePacket;
import com.oceanbase.jdbc.credential.Credential;
import com.oceanbase.jdbc.internal.util.SqlStates;
import java.sql.SQLTimeoutException;
import com.oceanbase.jdbc.internal.util.BulkStatus;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.internal.util.scheduler.SchedulerServiceProviderHolder;
import java.util.Iterator;
import java.util.Locale;
import java.util.List;
import com.oceanbase.jdbc.internal.com.send.ComStmtPrepareExecute;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import com.oceanbase.jdbc.internal.com.send.ComStmtExecute;
import com.oceanbase.jdbc.internal.ColumnType;
import com.oceanbase.jdbc.internal.com.send.parameters.LongDataParameterHolder;
import com.oceanbase.jdbc.internal.com.send.ComQuery;
import com.oceanbase.jdbc.internal.util.dao.ClientPrepareResult;
import java.nio.charset.Charset;
import com.oceanbase.jdbc.internal.com.send.ComStmtPrepare;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import java.sql.ResultSet;
import java.net.SocketTimeoutException;
import com.oceanbase.jdbc.internal.util.LogQueryTool;
import com.oceanbase.jdbc.internal.util.exceptions.OceanBaseSqlException;
import com.oceanbase.jdbc.internal.util.dao.PrepareResult;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import java.io.IOException;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.internal.io.LruTraceCache;
import java.util.concurrent.locks.ReentrantLock;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import com.oceanbase.jdbc.UrlParser;
import java.util.concurrent.FutureTask;
import java.io.InputStream;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.Set;
import com.oceanbase.jdbc.internal.logging.Logger;

public class AbstractQueryProtocol extends AbstractConnectProtocol implements Protocol
{
    private static final Logger logger;
    private static final Set<Integer> LOCK_DEADLOCK_ERROR_CODES;
    private ThreadPoolExecutor readScheduler;
    private InputStream localInfileInputStream;
    private long maxRows;
    private volatile int statementIdToRelease;
    private FutureTask activeFutureTask;
    private boolean interrupted;
    private long checksum;
    private int iterationCount;
    private int executeMode;
    private boolean enableNetworkStatistics;
    
    AbstractQueryProtocol(final UrlParser urlParser, final GlobalStateInfo globalInfo, final ReentrantLock lock, final LruTraceCache traceCache) {
        super(urlParser, globalInfo, lock, traceCache);
        this.readScheduler = null;
        this.statementIdToRelease = -1;
        this.activeFutureTask = null;
        this.checksum = 1L;
        this.enableNetworkStatistics = false;
    }
    
    @Override
    public void reset() throws SQLException {
        this.cmdPrologue();
        try {
            this.writer.startPacket(0);
            this.writer.write(31);
            this.writer.flush();
            this.getResult(new Results());
            if (this.options.cachePrepStmts && this.options.useServerPrepStmts) {
                this.serverPrepareStatementCache.clear();
            }
        }
        catch (SQLException sqlException) {
            throw this.exceptionWithQuery("COM_RESET_CONNECTION failed.", sqlException, this.explicitClosed);
        }
        catch (IOException e) {
            throw this.exceptionWithQuery("COM_RESET_CONNECTION failed.", this.handleIoException(e), this.explicitClosed);
        }
    }
    
    private OceanBaseSqlException exceptionWithQuery(final ParameterHolder[] parameters, final PrepareResult serverPrepareResult, final SQLException sqlException, final boolean explicitClosed) {
        return this.exceptionWithQuery(LogQueryTool.queryWithParams(serverPrepareResult, parameters, this.options), sqlException, explicitClosed);
    }
    
    private OceanBaseSqlException exceptionWithQuery(final String sql, final SQLException sqlException, final boolean explicitClosed) {
        OceanBaseSqlException ex;
        if (explicitClosed) {
            ex = new OceanBaseSqlException("Connection has explicitly been closed/aborted.", sql, sqlException);
        }
        else if (sqlException.getCause() instanceof SocketTimeoutException) {
            ex = new OceanBaseSqlException("Connection timed out", sql, "08000", sqlException);
        }
        else {
            ex = OceanBaseSqlException.of(sqlException, sql);
        }
        if (this.options.includeThreadDumpInDeadlockExceptions || sqlException.getErrorCode() == 1064) {
            ex.withThreadName(Thread.currentThread().getName());
        }
        if (this.options.includeInnodbStatusInDeadlockExceptions && sqlException.getSQLState() != null && AbstractQueryProtocol.LOCK_DEADLOCK_ERROR_CODES.contains(sqlException.getErrorCode())) {
            try {
                this.lock.lock();
                this.cmdPrologue();
                final Results results = new Results();
                this.executeQuery(this.isMasterConnection(), results, "SHOW ENGINE INNODB STATUS");
                results.commandEnd();
                final ResultSet rs = results.getResultSet();
                if (rs.next()) {
                    return ex.withDeadLockInfo(rs.getString(3));
                }
            }
            catch (SQLException ex2) {}
            finally {
                this.lock.unlock();
            }
        }
        return ex;
    }
    
    @Override
    public ServerPrepareResult prepare(final String sql, final boolean executeOnMaster) throws SQLException {
        this.lock.lock();
        try {
            if (this.options.cachePrepStmts && this.options.useServerPrepStmts) {
                final ServerPrepareResult pr = ((LinkedHashMap<K, ServerPrepareResult>)this.serverPrepareStatementCache).get(this.database + "-" + sql);
                if (pr != null && pr.incrementShareCounter()) {
                    return pr;
                }
            }
            this.cmdPrologue();
            this.writer.startPacket(0);
            this.writer.write(22);
            this.writer.write(sql.getBytes(this.options.characterEncoding));
            this.writer.flush();
            return ComStmtPrepare.read(this.reader, this.eofDeprecated, this, sql);
        }
        catch (IOException e) {
            throw this.exceptionWithQuery(sql, this.handleIoException(e), this.explicitClosed);
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public void executeQuery(final String sql) throws SQLException {
        this.executeQuery(this.isMasterConnection(), new Results(), sql);
    }
    
    @Override
    public void executeQuery(final boolean mustExecuteOnMaster, final Results results, final String sql) throws SQLException {
        this.cmdPrologue();
        try {
            this.writer.startPacket(0);
            this.writer.write(3);
            this.writer.write(sql.getBytes(this.options.characterEncoding));
            this.writer.flush();
            this.getResult(results);
        }
        catch (SQLException sqlException) {
            if ("70100".equals(sqlException.getSQLState()) && 1927 == sqlException.getErrorCode()) {
                throw this.handleIoException(sqlException);
            }
            throw this.exceptionWithQuery(sql, sqlException, this.explicitClosed);
        }
        catch (IOException e) {
            throw this.exceptionWithQuery(sql, this.handleIoException(e), this.explicitClosed);
        }
    }
    
    @Override
    public void executeQuery(final boolean mustExecuteOnMaster, final Results results, final String sql, final Charset charset) throws SQLException {
        this.cmdPrologue();
        try {
            this.writer.startPacket(0);
            this.writer.write(3);
            this.writer.write(sql.getBytes(charset));
            this.writer.flush();
            this.getResult(results);
        }
        catch (SQLException sqlException) {
            throw this.exceptionWithQuery(sql, sqlException, this.explicitClosed);
        }
        catch (IOException e) {
            throw this.exceptionWithQuery(sql, this.handleIoException(e), this.explicitClosed);
        }
    }
    
    @Override
    public void executeQuery(final boolean mustExecuteOnMaster, final Results results, final ClientPrepareResult clientPrepareResult, final ParameterHolder[] parameters) throws SQLException {
        this.cmdPrologue();
        try {
            if (clientPrepareResult.getParamCount() == 0 && !clientPrepareResult.isQueryMultiValuesRewritable()) {
                if (clientPrepareResult.getQueryParts().size() == 1) {
                    ComQuery.sendDirect(this.writer, clientPrepareResult.getQueryParts().get(0));
                }
                else {
                    ComQuery.sendMultiDirect(this.writer, clientPrepareResult.getQueryParts());
                }
            }
            else {
                ComQuery.sendSubCmd(this.writer, clientPrepareResult, parameters, -1);
            }
            this.getResult(results);
        }
        catch (SQLException queryException) {
            throw this.exceptionWithQuery(parameters, clientPrepareResult, queryException, false);
        }
        catch (IOException e) {
            throw this.exceptionWithQuery(parameters, clientPrepareResult, this.handleIoException(e), false);
        }
    }
    
    @Override
    public void executeQuery(final boolean mustExecuteOnMaster, final Results results, final ClientPrepareResult clientPrepareResult, final ParameterHolder[] parameters, final int queryTimeout) throws SQLException {
        this.cmdPrologue();
        try {
            if (clientPrepareResult.getParamCount() == 0 && !clientPrepareResult.isQueryMultiValuesRewritable()) {
                if (clientPrepareResult.getQueryParts().size() == 1) {
                    ComQuery.sendDirect(this.writer, clientPrepareResult.getQueryParts().get(0), queryTimeout);
                }
                else {
                    ComQuery.sendMultiDirect(this.writer, clientPrepareResult.getQueryParts(), queryTimeout);
                }
            }
            else {
                ComQuery.sendSubCmd(this.writer, clientPrepareResult, parameters, queryTimeout);
            }
            this.getResult(results);
        }
        catch (SQLException queryException) {
            throw this.exceptionWithQuery(parameters, clientPrepareResult, queryException, false);
        }
        catch (IOException e) {
            throw this.exceptionWithQuery(parameters, clientPrepareResult, this.handleIoException(e), false);
        }
    }
    
    @Override
    public void executePreparedQuery(final boolean mustExecuteOnMaster, final ServerPrepareResult serverPrepareResult, final Results results, final ParameterHolder[] parameters) throws SQLException {
        this.cmdPrologue();
        try {
            final int parameterCount = serverPrepareResult.getParameters().length;
            if (this.isOracleMode()) {
                if (this.options.useServerPrepStmts && this.options.usePieceData) {
                    for (int i = 0; i < parameterCount; ++i) {
                        if (parameters[i].isLongData()) {
                            boolean continueWrite = true;
                            boolean first = true;
                            while (continueWrite) {
                                this.writer.startPacket(0);
                                this.writer.write(-94);
                                this.writer.writeInt(serverPrepareResult.getStatementId());
                                this.writer.writeShort((short)i);
                                continueWrite = ((LongDataParameterHolder)parameters[i]).writePieceData(this.writer, first, this.options);
                                first = false;
                                this.getResult(new Results());
                            }
                        }
                    }
                }
                else {
                    for (int i = 0; i < parameterCount; ++i) {
                        if (parameters[i].isLongData()) {
                            throw new SQLException("Not supported send long data on ob oracle");
                        }
                    }
                }
            }
            else {
                for (int i = 0; i < parameterCount; ++i) {
                    if (parameters[i].isLongData()) {
                        this.writer.startPacket(0);
                        this.writer.write(24);
                        this.writer.writeInt(serverPrepareResult.getStatementId());
                        this.writer.writeShort((short)i);
                        parameters[i].writeBinary(this.writer);
                        this.writer.flush();
                    }
                }
            }
            this.writer.setTimeZone(this.getServerTimeZone());
            boolean withRefCursor = false;
            final ColumnDefinition[] columns;
            final ColumnDefinition[] ci = columns = serverPrepareResult.getColumns();
            for (final ColumnDefinition columnDefinition : columns) {
                if (columnDefinition.getColumnType() == ColumnType.CURSOR) {
                    withRefCursor = true;
                    break;
                }
            }
            byte cursorFlag = 0;
            if (!withRefCursor && results.getStatement().getSqlType() == 1 && (this.isOracleMode() || (!this.isOracleMode() && this.options.useCursorFetch && results.getFetchSize() > 0 && results.getResultSetScrollType() == 1003 && results.getResultSetConcurrency() == 1007))) {
                cursorFlag = 1;
                results.setToCursorFetch(true);
                results.setStatementId(serverPrepareResult.getStatementId());
            }
            else {
                cursorFlag = 0;
            }
            ComStmtExecute.send(this.writer, serverPrepareResult.getStatementId(), parameters, parameterCount, serverPrepareResult.getParameterTypeHeader(), cursorFlag, this);
            this.getResult(results);
            results.setToCursorFetch(false);
        }
        catch (SQLException qex) {
            throw this.exceptionWithQuery(parameters, serverPrepareResult, qex, false);
        }
        catch (IOException e) {
            throw this.exceptionWithQuery(parameters, serverPrepareResult, this.handleIoException(e), false);
        }
    }
    
    @Override
    public ServerPrepareResult executePreparedQuery(final int parameterCount, final ParameterHolder[] parameters, ServerPrepareResult serverPrepareResult, final Results results) throws SQLException {
        this.cmdPrologue();
        try {
            if (this.isOracleMode()) {
                if (this.options.useServerPrepStmts && this.options.usePieceData) {
                    boolean first = true;
                    for (int i = 0; i < parameterCount; ++i) {
                        if (parameters[i].isLongData()) {
                            boolean continueWrite = true;
                            while (continueWrite) {
                                if (first) {
                                    serverPrepareResult = this.prepare(results.getStatement().getOriginalSql(), true);
                                    first = false;
                                    serverPrepareResult.resetParameterTypeHeader();
                                }
                                this.writer.startPacket(0);
                                this.writer.write(-94);
                                this.writer.writeInt(serverPrepareResult.getStatementId());
                                this.writer.writeShort((short)i);
                                continueWrite = ((LongDataParameterHolder)parameters[i]).writePieceData(this.writer, first, this.options);
                                first = false;
                                this.getResult(new Results());
                            }
                        }
                    }
                }
                else {
                    for (int j = 0; j < parameterCount; ++j) {
                        if (parameters[j].isLongData()) {
                            throw new SQLException("Not supported send long data on ob oracle");
                        }
                    }
                }
            }
            else {
                for (int j = 0; j < parameters.length; ++j) {
                    if (parameters[j].isLongData()) {
                        this.writer.startPacket(0);
                        this.writer.write(24);
                        this.writer.writeInt(results.getStatementId());
                        this.writer.writeShort((short)j);
                        parameters[j].writeBinary(this.writer);
                        this.writer.flush();
                    }
                }
            }
            this.writer.setTimeZone(this.getServerTimeZone());
            boolean withRefCursor = false;
            if (serverPrepareResult != null) {
                final ColumnDefinition[] columns;
                final ColumnDefinition[] ci = columns = serverPrepareResult.getColumns();
                for (final ColumnDefinition columnDefinition : columns) {
                    if (columnDefinition.getColumnType() == ColumnType.CURSOR) {
                        withRefCursor = true;
                        break;
                    }
                }
            }
            byte cursorFlag = 0;
            if (!withRefCursor && results.getStatement().getSqlType() == 1 && this.isOracleMode()) {
                cursorFlag = 1;
                results.setToCursorFetch(true);
            }
            else {
                cursorFlag = 0;
            }
            ComStmtPrepareExecute.send(this.writer, results, parameterCount, parameters, (ColumnType[])((serverPrepareResult != null) ? serverPrepareResult.getParameterTypeHeader() : null), cursorFlag, this, serverPrepareResult);
            serverPrepareResult = ComStmtPrepareExecute.read(this, this.reader, serverPrepareResult, results);
            results.setToCursorFetch(false);
            return serverPrepareResult;
        }
        catch (SQLException qex) {
            throw this.exceptionWithQuery(results.getParameters(), serverPrepareResult, qex, false);
        }
        catch (IOException e) {
            throw this.exceptionWithQuery(results.getParameters(), serverPrepareResult, this.handleIoException(e), false);
        }
    }
    
    @Override
    public boolean executeBatchClient(final boolean mustExecuteOnMaster, final Results results, final ClientPrepareResult prepareResult, final List<ParameterHolder[]> parametersList, final boolean hasLongData) throws SQLException {
        if (this.options.rewriteBatchedStatements) {
            if (prepareResult.isQueryMultiValuesRewritable() && results.getAutoGeneratedKeys() == 2) {
                this.executeBatchRewrite(results, prepareResult, parametersList, true);
                return true;
            }
            if (prepareResult.isQueryMultipleRewritable()) {
                if (this.options.useBulkStmts && !hasLongData && prepareResult.isQueryMultipleRewritable() && results.getAutoGeneratedKeys() == 2 && this.versionGreaterOrEqual(10, 2, 7) && this.executeBulkBatch(results, prepareResult.getSql(), null, parametersList)) {
                    return true;
                }
                this.executeBatchRewrite(results, prepareResult, parametersList, false);
                return true;
            }
        }
        if (this.options.useBulkStmts && !hasLongData && results.getAutoGeneratedKeys() == 2 && this.versionGreaterOrEqual(10, 2, 7) && this.executeBulkBatch(results, prepareResult.getSql(), null, parametersList)) {
            return true;
        }
        if (this.options.useBatchMultiSend) {
            this.executeBatchMulti(results, prepareResult, parametersList);
            return true;
        }
        return false;
    }
    
    private boolean executeBulkBatch(final Results results, final String sql, final ServerPrepareResult serverPrepareResult, final List<ParameterHolder[]> parametersList) throws SQLException {
        final ParameterHolder[] initParameters = parametersList.get(0);
        final int parameterCount = initParameters.length;
        final short[] types = new short[parameterCount];
        for (int i = 0; i < parameterCount; ++i) {
            types[i] = initParameters[i].getColumnType().getType();
        }
        for (final ParameterHolder[] parameters : parametersList) {
            for (int j = 0; j < parameterCount; ++j) {
                if (parameters[j].getColumnType().getType() != types[j]) {
                    return false;
                }
            }
        }
        if (sql.toLowerCase(Locale.ROOT).contains("select")) {
            return false;
        }
        this.cmdPrologue();
        ParameterHolder[] parameters2 = null;
        ServerPrepareResult tmpServerPrepareResult = serverPrepareResult;
        try {
            SQLException exception = null;
            if (serverPrepareResult == null) {
                tmpServerPrepareResult = this.prepare(sql, true);
            }
            final int statementId = (tmpServerPrepareResult != null) ? tmpServerPrepareResult.getStatementId() : -1;
            byte[] lastCmdData = null;
            int index = 0;
            final ParameterHolder[] firstParameters = parametersList.get(0);
            do {
                this.writer.startPacket(0);
                this.writer.write(-6);
                this.writer.writeInt(statementId);
                this.writer.writeShort((short)128);
                for (final ParameterHolder param : firstParameters) {
                    this.writer.writeShort(param.getColumnType().getType());
                }
                if (lastCmdData != null) {
                    this.writer.checkMaxAllowedLength(lastCmdData.length);
                    this.writer.write(lastCmdData);
                    this.writer.mark();
                    ++index;
                    lastCmdData = null;
                }
                while (index < parametersList.size()) {
                    parameters2 = parametersList.get(index);
                    for (final ParameterHolder holder : parameters2) {
                        if (holder.isNullData()) {
                            this.writer.write(1);
                        }
                        else {
                            this.writer.write(0);
                            holder.writeBinary(this.writer);
                        }
                    }
                    if (this.writer.exceedMaxLength() && this.writer.isMarked()) {
                        this.writer.flushBufferStopAtMark();
                    }
                    if (this.writer.bufferIsDataAfterMark()) {
                        break;
                    }
                    this.writer.checkMaxAllowedLength(0);
                    this.writer.mark();
                    ++index;
                }
                if (this.writer.bufferIsDataAfterMark()) {
                    lastCmdData = this.writer.resetMark();
                }
                else {
                    this.writer.flush();
                    this.writer.resetMark();
                }
                try {
                    this.getResult(results);
                }
                catch (SQLException sqle) {
                    if ("HY000".equals(sqle.getSQLState()) && sqle.getErrorCode() == 1295) {
                        results.getCmdInformation().reset();
                        return false;
                    }
                    if (exception != null) {
                        continue;
                    }
                    exception = this.exceptionWithQuery(sql, sqle, this.explicitClosed);
                    if (!this.options.continueBatchOnError) {
                        throw exception;
                    }
                    continue;
                }
            } while (index < parametersList.size() - 1);
            if (lastCmdData != null) {
                this.writer.startPacket(0);
                this.writer.write(-6);
                this.writer.writeInt(statementId);
                this.writer.writeShort((short)(-128));
                for (final ParameterHolder param : firstParameters) {
                    this.writer.writeShort(param.getColumnType().getType());
                }
                this.writer.write(lastCmdData);
                this.writer.flush();
                try {
                    this.getResult(results);
                }
                catch (SQLException sqle) {
                    if ("HY000".equals(sqle.getSQLState()) && sqle.getErrorCode() == 1295) {
                        return false;
                    }
                    if (exception == null) {
                        exception = this.exceptionWithQuery(sql, sqle, this.explicitClosed);
                        if (!this.options.continueBatchOnError) {
                            throw exception;
                        }
                    }
                }
            }
            if (exception != null) {
                throw exception;
            }
            results.setRewritten(true);
            return true;
        }
        catch (IOException e) {
            throw this.exceptionWithQuery(parameters2, tmpServerPrepareResult, this.handleIoException(e), this.explicitClosed);
        }
        finally {
            if (serverPrepareResult == null && tmpServerPrepareResult != null) {
                this.releasePrepareStatement(tmpServerPrepareResult);
            }
            this.writer.resetMark();
        }
    }
    
    private void initializeBatchReader() {
        if (this.options.useBatchMultiSend) {
            this.readScheduler = SchedulerServiceProviderHolder.getBulkScheduler();
        }
    }
    
    private void executeBatchMulti(final Results results, final ClientPrepareResult clientPrepareResult, final List<ParameterHolder[]> parametersList) throws SQLException {
        this.cmdPrologue();
        this.initializeBatchReader();
        new AbstractMultiSend(this, this.writer, results, clientPrepareResult, parametersList, this.readScheduler) {
            @Override
            public void sendCmd(final PacketOutputStream writer, final Results results, final List<ParameterHolder[]> parametersList, final List<String> queries, final int paramCount, final BulkStatus status, final PrepareResult prepareResult) throws IOException {
                final ParameterHolder[] parameters = parametersList.get(status.sendCmdCounter);
                ComQuery.sendSubCmd(writer, clientPrepareResult, parameters, -1);
            }
            
            @Override
            public SQLException handleResultException(final SQLException qex, final Results results, final List<ParameterHolder[]> parametersList, final List<String> queries, final int currentCounter, final int sendCmdCounter, final int paramCount, final PrepareResult prepareResult) {
                final int counter = results.getCurrentStatNumber() - 1;
                final ParameterHolder[] parameters = parametersList.get(counter);
                final List<byte[]> queryParts = clientPrepareResult.getQueryParts();
                final StringBuilder sql = new StringBuilder(new String(queryParts.get(0)));
                for (int i = 0; i < paramCount; ++i) {
                    sql.append(parameters[i].toString()).append(new String(queryParts.get(i + 1)));
                }
                return AbstractQueryProtocol.this.exceptionWithQuery(sql.toString(), qex, AbstractQueryProtocol.this.explicitClosed);
            }
            
            @Override
            public int getParamCount() {
                return clientPrepareResult.getQueryParts().size() - 1;
            }
            
            @Override
            public int getTotalExecutionNumber() {
                return parametersList.size();
            }
        }.executeBatch();
    }
    
    @Override
    public void executeBatchStmt(final boolean mustExecuteOnMaster, final Results results, final List<String> queries) throws SQLException {
        this.cmdPrologue();
        if (this.options.rewriteBatchedStatements) {
            boolean canAggregateSemiColumn = true;
            for (final String query : queries) {
                if (!ClientPrepareResult.canAggregateSemiColon(query, this.noBackslashEscapes(), this.isOracleMode())) {
                    canAggregateSemiColumn = false;
                    break;
                }
            }
            if (this.isInterrupted()) {
                throw new SQLTimeoutException("Timeout during batch execution");
            }
            if (canAggregateSemiColumn) {
                this.executeBatchAggregateSemiColon(results, queries);
            }
            else {
                this.executeBatch(results, queries);
            }
        }
        else {
            this.executeBatch(results, queries);
        }
    }
    
    private void executeBatch(final Results results, final List<String> queries) throws SQLException {
        if (this.options.useBatchMultiSend) {
            this.initializeBatchReader();
            new AbstractMultiSend(this, this.writer, results, queries, this.readScheduler) {
                @Override
                public void sendCmd(final PacketOutputStream pos, final Results results, final List<ParameterHolder[]> parametersList, final List<String> queries, final int paramCount, final BulkStatus status, final PrepareResult prepareResult) throws IOException {
                    final String sql = queries.get(status.sendCmdCounter);
                    pos.startPacket(0);
                    pos.write(3);
                    pos.write(sql);
                    pos.flush();
                }
                
                @Override
                public SQLException handleResultException(final SQLException qex, final Results results, final List<ParameterHolder[]> parametersList, final List<String> queries, final int currentCounter, final int sendCmdCounter, final int paramCount, final PrepareResult prepareResult) {
                    final String sql = queries.get(currentCounter + sendCmdCounter);
                    return AbstractQueryProtocol.this.exceptionWithQuery(sql, qex, AbstractQueryProtocol.this.explicitClosed);
                }
                
                @Override
                public int getParamCount() {
                    return -1;
                }
                
                @Override
                public int getTotalExecutionNumber() {
                    return queries.size();
                }
            }.executeBatch();
            return;
        }
        String sql = null;
        SQLException exception = null;
        for (int i = 0; i < queries.size() && !this.isInterrupted(); ++i) {
            try {
                sql = queries.get(i);
                this.writer.startPacket(0);
                this.writer.write(3);
                this.writer.write(sql);
                this.writer.flush();
                this.getResult(results);
            }
            catch (SQLException sqlException) {
                if (exception == null) {
                    exception = this.exceptionWithQuery(sql, sqlException, this.explicitClosed);
                    if (!this.options.continueBatchOnError) {
                        throw exception;
                    }
                }
            }
            catch (IOException e) {
                if (exception == null) {
                    exception = this.exceptionWithQuery(sql, this.handleIoException(e), this.explicitClosed);
                    if (!this.options.continueBatchOnError) {
                        throw exception;
                    }
                }
            }
        }
        this.stopIfInterrupted();
        if (exception != null) {
            throw exception;
        }
    }
    
    private void executeBatchAggregateSemiColon(final Results results, final List<String> queries) throws SQLException {
        String firstSql = null;
        int currentIndex = 0;
        final int totalQueries = queries.size();
        SQLException exception = null;
        do {
            try {
                firstSql = queries.get(currentIndex++);
                if (totalQueries == 1) {
                    this.writer.startPacket(0);
                    this.writer.write(3);
                    this.writer.write(firstSql);
                    this.writer.flush();
                }
                else {
                    currentIndex = ComQuery.sendBatchAggregateSemiColon(this.writer, firstSql, queries, currentIndex);
                }
                this.getResult(results);
            }
            catch (SQLException sqlException) {
                if (exception == null) {
                    exception = this.exceptionWithQuery(firstSql, sqlException, this.explicitClosed);
                    if (!this.options.continueBatchOnError) {
                        throw exception;
                    }
                }
            }
            catch (IOException e) {
                throw this.exceptionWithQuery(firstSql, this.handleIoException(e), this.explicitClosed);
            }
            this.stopIfInterrupted();
        } while (currentIndex < totalQueries);
        if (exception != null) {
            throw exception;
        }
    }
    
    private void executeBatchRewrite(final Results results, final ClientPrepareResult prepareResult, final List<ParameterHolder[]> parameterList, final boolean rewriteValues) throws SQLException {
        this.cmdPrologue();
        int currentIndex = 0;
        final int totalParameterList = parameterList.size();
        try {
            do {
                currentIndex = ComQuery.sendRewriteCmd(this.writer, prepareResult.getQueryParts(), currentIndex, prepareResult.getParamCount(), parameterList, rewriteValues);
                this.getResult(results);
                if (Thread.currentThread().isInterrupted()) {
                    throw new SQLException("Interrupted during batch", SqlStates.INTERRUPTED_EXCEPTION.getSqlState(), -1);
                }
            } while (currentIndex < totalParameterList);
        }
        catch (SQLException sqlEx) {
            throw OceanBaseSqlException.of(sqlEx, prepareResult.getSql());
        }
        catch (IOException e) {
            throw this.exceptionWithQuery(parameterList.get(currentIndex), prepareResult, this.handleIoException(e), this.explicitClosed);
        }
        finally {
            results.setRewritten(rewriteValues);
        }
    }
    
    @Override
    public ServerPrepareResult executeBatchServer(ServerPrepareResult serverPrepareResult, final Results results, final String sql, final List<ParameterHolder[]> parametersList, final boolean hasLongData) throws SQLException {
        this.cmdPrologue();
        if (this.options.useBulkStmts && !hasLongData && results.getAutoGeneratedKeys() == 2 && this.versionGreaterOrEqual(10, 2, 7) && this.executeBulkBatch(results, sql, serverPrepareResult, parametersList)) {
            results.setBatchSucceed(true);
            return serverPrepareResult;
        }
        if (!this.options.useBatchMultiSend) {
            results.setBatchSucceed(false);
            return serverPrepareResult;
        }
        this.initializeBatchReader();
        final ServerPrepareResult tempServerPrepareResult = null;
        serverPrepareResult = (ServerPrepareResult)new AbstractMultiSend(this, this.writer, results, tempServerPrepareResult, parametersList, true, sql, this.readScheduler) {
            @Override
            public void sendCmd(final PacketOutputStream writer, final Results results, final List<ParameterHolder[]> parametersList, final List<String> queries, final int paramCount, final BulkStatus status, final PrepareResult prepareResult) throws SQLException, IOException {
                final ParameterHolder[] parameters = parametersList.get(status.sendCmdCounter);
                if (parameters.length < paramCount) {
                    throw new SQLException("Parameter at position " + (paramCount - 1) + " is not set", "07004");
                }
                if (AbstractQueryProtocol.this.isOracleMode()) {
                    if (AbstractQueryProtocol.this.options.useServerPrepStmts && AbstractQueryProtocol.this.options.usePieceData) {
                        for (int i = 0; i < paramCount; ++i) {
                            if (parameters[i].isLongData()) {
                                writer.startPacket(0);
                                writer.write(-94);
                                writer.writeInt(this.statementId);
                                writer.writeShort((short)i);
                                writer.writeShort((short)3);
                                parameters[i].writeBinary(writer);
                                writer.flush();
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < paramCount; ++i) {
                            if (parameters[i].isLongData()) {
                                throw new SQLException("Not supported send long data on ob oracle");
                            }
                        }
                    }
                }
                else {
                    for (int i = 0; i < paramCount; ++i) {
                        if (parameters[i].isLongData()) {
                            writer.startPacket(0);
                            writer.write(24);
                            writer.writeInt(this.statementId);
                            writer.writeShort((short)i);
                            parameters[i].writeBinary(writer);
                            writer.flush();
                        }
                    }
                }
                if (AbstractQueryProtocol.this.supportStmtPrepareExecute()) {
                    results.setParameters(parameters);
                    ComStmtPrepareExecute.send(writer, results, paramCount, parameters, (ColumnType[])((tempServerPrepareResult != null) ? tempServerPrepareResult.getParameterTypeHeader() : null), (byte)0, this.getProtocol(), tempServerPrepareResult);
                }
                else {
                    ComStmtExecute.send(writer, this.statementId, parameters, paramCount, this.parameterTypeHeader, (byte)0, this.getProtocol());
                }
            }
            
            @Override
            public SQLException handleResultException(final SQLException qex, final Results results, final List<ParameterHolder[]> parametersList, final List<String> queries, final int currentCounter, final int sendCmdCounter, final int paramCount, final PrepareResult prepareResult) {
                return OceanBaseSqlException.of(qex, prepareResult.getSql());
            }
            
            @Override
            public int getParamCount() {
                return (this.getPrepareResult() == null) ? ((ParameterHolder[])parametersList.get(0)).length : ((ServerPrepareResult)this.getPrepareResult()).getParameters().length;
            }
            
            @Override
            public int getTotalExecutionNumber() {
                return parametersList.size();
            }
        }.executeBatch();
        results.setBatchSucceed(true);
        return serverPrepareResult;
    }
    
    @Override
    public ColumnDefinition[] sendFechRowViaCursor(final ServerPrepareResult serverPrepareResult, final int cursorId, final int fetchSize, final Results results) throws SQLException {
        this.cmdPrologue();
        try {
            this.writer.startPacket(0);
            this.writer.write(28);
            this.writer.writeInt(cursorId);
            this.writer.writeInt(fetchSize);
            this.writer.flush();
            if (this.isOracleMode()) {
                return this.getResultWithoutValue(results);
            }
            return null;
        }
        catch (IOException e) {
            throw this.exceptionWithQuery(" sendFechRowViaCursor failed.", this.handleIoException(e), this.explicitClosed);
        }
    }
    
    @Override
    public long getLastPacketCostTime() throws SQLException {
        if (!this.enableNetworkStatistics) {
            throw new SQLException("Cant get network cost info while setNetworkStatisticsFlag(true)");
        }
        return this.reader.getTimestampAfterRead() - this.writer.getTimestampBeforeFlush();
    }
    
    @Override
    public void setNetworkStatisticsFlag(final boolean flag) {
        this.enableNetworkStatistics = flag;
        this.writer.enableNetworkStatistics(flag);
        this.reader.enableNetworkStatistics(flag);
    }
    
    @Override
    public boolean getNetworkStatisticsFlag() {
        return this.enableNetworkStatistics;
    }
    
    @Override
    public long getLastPacketResponseTimestamp() {
        return this.reader.getTimestampAfterRead();
    }
    
    @Override
    public long getLastPacketSendTimestamp() {
        return this.writer.getTimestampBeforeFlush();
    }
    
    @Override
    public void clearNetworkStatistics() {
        this.writer.clearNetworkStatistics();
        this.reader.clearNetworkStatistics();
    }
    
    @Override
    public void changeUser(final String user, final String pwd) throws SQLException {
        this.cmdPrologue();
        try {
            final Credential credential = new Credential(user, pwd);
            final String clientIp = this.socket.getLocalAddress().getHostAddress();
            SendHandshakeResponsePacket.sendChangeUser(this.writer, credential, this.host, this.database, this.clientCapabilities, this.serverCapabilities, this.exchangeCharset, (byte)(Boolean.TRUE.equals(this.options.useSsl) ? 2 : 1), this.options, this.authenticationPluginType, this.seed, clientIp, this.isOracleMode());
            this.getResult(new Results());
        }
        catch (IOException e) {
            throw this.exceptionWithQuery(" change user failed.", this.handleIoException(e), this.explicitClosed);
        }
        this.setUsername(user);
        this.postConnectionQueries();
    }
    
    @Override
    public void rollback() throws SQLException {
        this.cmdPrologue();
        this.lock.lock();
        try {
            if (this.inTransaction()) {
                this.executeQuery("ROLLBACK");
            }
        }
        catch (Exception ex) {}
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public void setUsername(final String username) {
        this.username = username;
    }
    
    @Override
    public boolean forceReleasePrepareStatement(final int statementId) throws SQLException {
        if (this.lock.tryLock()) {
            try {
                this.checkClose();
                try {
                    this.writer.startPacket(0);
                    this.writer.write(25);
                    this.writer.writeInt(statementId);
                    this.writer.flush();
                    return true;
                }
                catch (IOException e) {
                    this.connected = false;
                    throw new SQLNonTransientConnectionException("Could not deallocate query: " + e.getMessage(), "08000", e);
                }
            }
            finally {
                this.lock.unlock();
            }
        }
        this.statementIdToRelease = statementId;
        return false;
    }
    
    @Override
    public void forceReleaseWaitingPrepareStatement() throws SQLException {
        if (this.statementIdToRelease != -1 && this.forceReleasePrepareStatement(this.statementIdToRelease)) {
            this.statementIdToRelease = -1;
        }
    }
    
    @Override
    public boolean ping() throws SQLException {
        this.cmdPrologue();
        this.lock.lock();
        try {
            this.writer.startPacket(0);
            this.writer.write(14);
            this.writer.flush();
            final Buffer buffer = this.reader.getPacket(true);
            return buffer.getByteAt(0) == 0;
        }
        catch (IOException e) {
            this.connected = false;
            throw new SQLNonTransientConnectionException("Could not ping: " + e.getMessage(), "08000", e);
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public boolean isValid(final int timeout) throws SQLException {
        int initialTimeout = -1;
        try {
            initialTimeout = this.socketTimeout;
            if (initialTimeout == 0) {
                this.changeSocketSoTimeout(timeout);
            }
            if (this.isMasterConnection() && !this.galeraAllowedStates.isEmpty()) {
                final Results results = new Results();
                this.executeQuery(true, results, "show status like 'wsrep_local_state'");
                results.commandEnd();
                final ResultSet rs = results.getResultSet();
                return rs != null && rs.next() && this.galeraAllowedStates.contains(rs.getString(2));
            }
            return this.ping();
        }
        catch (SocketException socketException) {
            AbstractQueryProtocol.logger.trace("Connection is not valid", socketException);
            this.connected = false;
            return false;
        }
        finally {
            try {
                if (initialTimeout != -1) {
                    this.changeSocketSoTimeout(initialTimeout);
                }
            }
            catch (SocketException socketException2) {
                AbstractQueryProtocol.logger.warn("Could not set socket timeout back to " + initialTimeout, socketException2);
                this.connected = false;
            }
        }
    }
    
    @Override
    public String getCatalog() throws SQLException {
        if ((this.serverCapabilities & 0x800000L) != 0x0L) {
            if (this.database != null && this.database.isEmpty()) {
                return null;
            }
            return this.database;
        }
        else {
            this.cmdPrologue();
            this.lock.lock();
            try {
                final Results results = new Results();
                this.executeQuery(this.isMasterConnection(), results, "select database()");
                results.commandEnd();
                final ResultSet rs = results.getResultSet();
                if (rs.next()) {
                    return this.database = rs.getString(1);
                }
                return null;
            }
            finally {
                this.lock.unlock();
            }
        }
    }
    
    @Override
    public void setCatalog(final String database) throws SQLException {
        this.cmdPrologue();
        this.lock.lock();
        try {
            SendChangeDbPacket.send(this.writer, database);
            final Buffer buffer = this.reader.getPacket(true);
            if (buffer.getByteAt(0) == -1) {
                final ErrorPacket ep = new ErrorPacket(buffer);
                throw new SQLException("Could not select database '" + database + "' : " + ep.getMessage(), ep.getSqlState(), ep.getErrorCode());
            }
            this.database = database;
        }
        catch (IOException e) {
            throw this.exceptionWithQuery("COM_INIT_DB", this.handleIoException(e), false);
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public void resetDatabase() throws SQLException {
        if (!this.database.equals(this.urlParser.getDatabase())) {
            this.setCatalog(this.urlParser.getDatabase());
        }
    }
    
    @Override
    public void cancelCurrentQuery() throws SQLException {
        try (final MasterProtocol copiedProtocol = new MasterProtocol(this.urlParser, new GlobalStateInfo(), new ReentrantLock(), this.traceCache)) {
            copiedProtocol.setHostAddress(this.getHostAddress());
            copiedProtocol.connect();
            copiedProtocol.executeQuery("KILL QUERY " + this.serverThreadId);
        }
        this.interrupted = true;
    }
    
    @Override
    public boolean getAutocommit() {
        return this.autoCommit;
    }
    
    @Override
    public void setAutoCommit(final boolean autoCommit) throws SQLException {
        if (this.autoCommit != autoCommit) {
            this.executeQuery("set autocommit = " + (autoCommit ? "1" : "0"));
            this.autoCommit = autoCommit;
        }
    }
    
    @Override
    public boolean inTransaction() {
        return (this.serverStatus & 0x1) != 0x0;
    }
    
    @Override
    public void closeExplicit() {
        this.explicitClosed = true;
        this.close();
    }
    
    @Override
    public void releasePrepareStatement(final ServerPrepareResult serverPrepareResult) throws SQLException {
        serverPrepareResult.decrementShareCounter();
        if (serverPrepareResult.canBeDeallocate()) {
            this.forceReleasePrepareStatement(serverPrepareResult.getStatementId());
        }
    }
    
    @Override
    public long getMaxRows() {
        return this.maxRows;
    }
    
    @Override
    public void setMaxRows(final long max) throws SQLException {
        if (this.maxRows != max) {
            if (max == 0L) {
                this.executeQuery("set @@SQL_SELECT_LIMIT=DEFAULT");
            }
            else {
                this.executeQuery("set @@SQL_SELECT_LIMIT=" + max);
            }
            this.maxRows = max;
        }
    }
    
    @Override
    public void setLocalInfileInputStream(final InputStream inputStream) {
        this.localInfileInputStream = inputStream;
    }
    
    @Override
    public int getTimeout() {
        return this.socketTimeout;
    }
    
    @Override
    public void setTimeout(final int timeout) throws SocketException {
        this.lock.lock();
        try {
            this.changeSocketSoTimeout(timeout);
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public void setReadonly(final boolean readOnly) throws SQLException {
        if (this.options.assureReadOnly && this.readOnly != readOnly && this.versionGreaterOrEqual(5, 6, 5)) {
            this.executeQuery("SET SESSION TRANSACTION " + (readOnly ? "READ ONLY" : "READ WRITE"));
        }
        this.readOnly = readOnly;
    }
    
    @Override
    public void setTransactionIsolation(final int level) throws SQLException {
        this.cmdPrologue();
        this.lock.lock();
        if (this.transactionIsolationLevel == level) {
            return;
        }
        try {
            String query = "SET SESSION TRANSACTION ISOLATION LEVEL";
            switch (level) {
                case 1: {
                    query += " READ UNCOMMITTED";
                    break;
                }
                case 2: {
                    query += " READ COMMITTED";
                    break;
                }
                case 4: {
                    query += " REPEATABLE READ";
                    break;
                }
                case 8: {
                    query += " SERIALIZABLE";
                    break;
                }
                default: {
                    throw new SQLException("Unsupported transaction isolation level");
                }
            }
            this.executeQuery(query);
            this.transactionIsolationLevel = level;
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public int getTransactionIsolationLevel() {
        return this.transactionIsolationLevel;
    }
    
    private void checkClose() throws SQLException {
        if (!this.connected) {
            throw new SQLException("Connection is close", "08000", 1220);
        }
    }
    
    @Override
    public void getResult(final Results results) throws SQLException {
        this.readPacket(results);
        while (this.hasMoreResults()) {
            this.readPacket(results);
        }
    }
    
    public ColumnDefinition[] getResultWithoutValue(final Results results) throws SQLException {
        ColumnDefinition[] ci = null;
        Buffer buffer;
        try {
            buffer = this.reader.getPacket(true);
        }
        catch (IOException e) {
            throw this.handleIoException(e);
        }
        switch (buffer.getByteAt(0)) {
            case 0: {
                this.readOkPacket(buffer, results);
                break;
            }
            case -1: {
                throw this.readErrorPacket(buffer, results);
            }
            case -5: {
                this.readLocalInfilePacket(buffer, results);
                break;
            }
            default: {
                ci = this.readResultSetColumnDef(buffer, results);
                break;
            }
        }
        return ci;
    }
    
    private ColumnDefinition[] readResultSetColumnDef(final Buffer buffer, final Results results) throws SQLException {
        final long fieldCount = buffer.getLengthEncodedNumeric();
        try {
            final ColumnDefinition[] ci = new ColumnDefinition[(int)fieldCount];
            for (int i = 0; i < fieldCount; ++i) {
                ci[i] = new ColumnDefinition(this.reader.getPacket(false), this.isOracleMode(), this.options.characterEncoding);
            }
            if (!this.eofDeprecated) {
                final Buffer bufferEof = this.reader.getPacket(true);
                if (bufferEof.readByte() != -2) {
                    throw new IOException("Packets out of order when reading field packets, expected was EOF stream." + (this.options.enablePacketDebug ? this.getTraces() : ("Packet contents (hex) = " + Utils.hexdump(this.options.maxQuerySizeToLog, 0, bufferEof.limit, new byte[][] { bufferEof.buf }))));
                }
                bufferEof.skipBytes(2);
                bufferEof.readShort();
            }
            return ci;
        }
        catch (IOException e) {
            throw this.handleIoException(e);
        }
    }
    
    private void readPacket(final Results results) throws SQLException {
        Buffer buffer;
        try {
            buffer = this.reader.getPacket(true);
        }
        catch (IOException e) {
            throw this.handleIoException(e);
        }
        switch (buffer.getByteAt(0)) {
            case 0: {
                this.readOkPacket(buffer, results);
                break;
            }
            case -1: {
                throw this.readErrorPacket(buffer, results);
            }
            case -5: {
                this.readLocalInfilePacket(buffer, results);
                break;
            }
            default: {
                final long fieldCount = buffer.getLengthEncodedNumeric();
                final ColumnDefinition[] columns = new ColumnDefinition[(int)fieldCount];
                this.readResultSet(columns, results);
                break;
            }
        }
    }
    
    private void handleStateChange(final Buffer buf, final Results results) {
        buf.skipLengthEncodedBytes();
        while (buf.remaining() > 0) {
            final Buffer stateInfo = buf.getLengthEncodedBuffer();
            if (stateInfo.remaining() > 0) {
                switch (stateInfo.readByte()) {
                    case 0: {
                        final Buffer sessionVariableBuf = stateInfo.getLengthEncodedBuffer();
                        final String variable = sessionVariableBuf.readStringLengthEncoded(StandardCharsets.UTF_8);
                        final String value = sessionVariableBuf.readStringLengthEncoded(StandardCharsets.UTF_8);
                        AbstractQueryProtocol.logger.debug("System variable change :  {} = {}", variable, value);
                        final String s = variable;
                        switch (s) {
                            case "auto_increment_increment": {
                                results.setAutoIncrement(this.autoIncrementIncrement = Integer.parseInt(value));
                                continue;
                            }
                        }
                        continue;
                    }
                    case 1: {
                        final Buffer sessionSchemaBuf = stateInfo.getLengthEncodedBuffer();
                        this.database = sessionSchemaBuf.readStringLengthEncoded(StandardCharsets.UTF_8);
                        AbstractQueryProtocol.logger.debug("Database change : now is '{}'", this.database);
                        continue;
                    }
                    default: {
                        stateInfo.skipLengthEncodedBytes();
                        continue;
                    }
                }
            }
        }
    }
    
    @Override
    public int getAutoIncrementIncrement() throws SQLException {
        if (!this.options.connectProxy && this.autoIncrementIncrement == 0) {
            this.lock.lock();
            try {
                final Results results = new Results();
                this.executeQuery(true, results, "select @@auto_increment_increment");
                results.commandEnd();
                final ResultSet rs = results.getResultSet();
                rs.next();
                this.autoIncrementIncrement = rs.getInt(1);
            }
            catch (SQLException e) {
                if (e.getSQLState().startsWith("08")) {
                    throw e;
                }
                this.autoIncrementIncrement = 1;
            }
            finally {
                this.lock.unlock();
            }
        }
        return this.autoIncrementIncrement;
    }
    
    @Override
    public void readOkPacket(final Buffer buffer, final Results results) {
        buffer.skipByte();
        final long updateCount = buffer.getLengthEncodedNumeric();
        final long insertId = buffer.getLengthEncodedNumeric();
        this.serverStatus = buffer.readShort();
        this.hasWarnings = (buffer.readShort() > 0);
        if ((this.serverStatus & 0x4000) != 0x0) {
            this.handleStateChange(buffer, results);
        }
        results.addStats(updateCount, insertId, this.hasMoreResults());
    }
    
    @Override
    public SQLException readErrorPacket(final Buffer buffer, final Results results) {
        this.removeHasMoreResults();
        this.hasWarnings = false;
        final ErrorPacket ep = new ErrorPacket(buffer);
        results.addStatsError(false);
        this.serverStatus |= 0x1;
        this.removeActiveStreamingResult();
        if (1054 == ep.getErrorCode()) {
            return new SQLException(ep.getMessage() + "\nIf column exists but type cannot be identified (example 'select ? `field1` from dual'). " + "Use CAST function to solve this problem (example 'select CAST(? as integer) `field1` from dual')", ep.getSqlState(), ep.getErrorCode());
        }
        return new SQLException(ep.getMessage(), ep.getSqlState(), ep.getErrorCode());
    }
    
    private void readLocalInfilePacket(final Buffer buffer, final Results results) throws SQLException {
        int seq = 2;
        buffer.getLengthEncodedNumeric();
        final String fileName = buffer.readStringNullEnd(StandardCharsets.UTF_8);
        try {
            this.writer.startPacket(seq);
            InputStream is;
            if (this.localInfileInputStream == null) {
                if (!this.getUrlParser().getOptions().allowLocalInfile) {
                    this.writer.writeEmptyPacket();
                    this.reader.getPacket(true);
                    throw new SQLException("Usage of LOCAL INFILE is disabled. To use it enable it via the connection property allowLocalInfile=true", SqlStates.FEATURE_NOT_SUPPORTED.getSqlState(), -1);
                }
                final ServiceLoader<LocalInfileInterceptor> loader = ServiceLoader.load(LocalInfileInterceptor.class);
                for (final LocalInfileInterceptor interceptor : loader) {
                    if (!interceptor.validate(fileName)) {
                        this.writer.writeEmptyPacket();
                        this.reader.getPacket(true);
                        throw new SQLException("LOAD DATA LOCAL INFILE request to send local file named \"" + fileName + "\" not validated by interceptor \"" + interceptor.getClass().getName() + "\"");
                    }
                }
                if (results.getSql() == null) {
                    this.writer.writeEmptyPacket();
                    this.reader.getPacket(true);
                    throw new SQLException("LOAD DATA LOCAL INFILE not permit in batch. file '" + fileName + "'", SqlStates.INVALID_AUTHORIZATION.getSqlState(), -1);
                }
                if (!Utils.validateFileName(results.getSql(), results.getParameters(), fileName)) {
                    this.writer.writeEmptyPacket();
                    this.reader.getPacket(true);
                    throw new SQLException("LOAD DATA LOCAL INFILE asked for file '" + fileName + "' that doesn't correspond to initial query " + results.getSql() + ". Possible malicious proxy changing server answer ! Command interrupted", SqlStates.INVALID_AUTHORIZATION.getSqlState(), -1);
                }
                try {
                    final URL url = new URL(fileName);
                    is = url.openStream();
                }
                catch (IOException ioe2) {
                    try {
                        is = new FileInputStream(fileName);
                    }
                    catch (FileNotFoundException f) {
                        this.writer.writeEmptyPacket();
                        this.reader.getPacket(true);
                        throw new SQLException("Could not send file : " + f.getMessage(), "22000", -1, f);
                    }
                }
            }
            else {
                is = this.localInfileInputStream;
                this.localInfileInputStream = null;
            }
            try {
                final byte[] buf = new byte[8192];
                int len;
                while ((len = is.read(buf)) > 0) {
                    this.writer.startPacket(seq++);
                    this.writer.write(buf, 0, len);
                    this.writer.flush();
                }
                this.writer.writeEmptyPacket();
            }
            catch (IOException ioe) {
                throw this.handleIoException(ioe);
            }
            finally {
                is.close();
            }
            this.getResult(results);
        }
        catch (IOException e) {
            throw this.handleIoException(e);
        }
    }
    
    @Override
    public void readResultSet(final ColumnDefinition[] ci, final Results results) throws SQLException {
        try {
            for (int i = 0; i < ci.length; ++i) {
                ci[i] = new ColumnDefinition(this.reader.getPacket(false), this.isOracleMode(), this.options.characterEncoding);
            }
            boolean callableResult = false;
            final boolean isPsOutParamter = false;
            if (!this.eofDeprecated) {
                final Buffer bufferEof = this.reader.getPacket(true);
                if (bufferEof.readByte() != -2) {
                    throw new IOException("Packets out of order when reading field packets, expected was EOF stream." + (this.options.enablePacketDebug ? this.getTraces() : ("Packet contents (hex) = " + Utils.hexdump(this.options.maxQuerySizeToLog, 0, bufferEof.limit, new byte[][] { bufferEof.buf }))));
                }
                bufferEof.skipBytes(2);
                final short currentStatus = bufferEof.readShort();
                callableResult = ((currentStatus & 0x1000) != 0x0);
            }
            SelectResultSet selectResultSet;
            if (results.getResultSetConcurrency() == 1008) {
                results.removeFetchSize();
                selectResultSet = new UpdatableResultSet(ci, results, this, this.reader, callableResult, this.eofDeprecated, isPsOutParamter);
            }
            else if (results.isToCursorFetch()) {
                selectResultSet = new CursorResultSet(ci, results, this, callableResult, this.eofDeprecated, isPsOutParamter);
            }
            else {
                selectResultSet = new SelectResultSet(ci, results, this, this.reader, callableResult, this.eofDeprecated, isPsOutParamter);
            }
            results.addResultSet(selectResultSet, this.hasMoreResults() || results.getFetchSize() > 0);
        }
        catch (IOException e) {
            throw this.handleIoException(e);
        }
    }
    
    @Override
    public void prologProxy(final ServerPrepareResult serverPrepareResult, final long maxRows, final boolean hasProxy, final OceanBaseConnection connection, final OceanBaseStatement statement) throws SQLException {
        this.prolog(maxRows, hasProxy, connection, statement);
    }
    
    @Override
    public void prolog(final long maxRows, final boolean hasProxy, final OceanBaseConnection connection, final OceanBaseStatement statement) throws SQLException {
        if (this.explicitClosed) {
            throw new SQLNonTransientConnectionException("execute() is called on closed connection", "08000");
        }
        if (!hasProxy && this.shouldReconnectWithoutProxy()) {
            try {
                this.connectWithoutProxy();
            }
            catch (SQLException qe) {
                throw ExceptionFactory.of((int)this.serverThreadId, this.options).create(qe);
            }
        }
        try {
            this.setMaxRows(maxRows);
        }
        catch (SQLException qe) {
            throw ExceptionFactory.of((int)this.serverThreadId, this.options).create(qe);
        }
        connection.reenableWarnings();
    }
    
    @Override
    public ServerPrepareResult addPrepareInCache(final String key, final ServerPrepareResult serverPrepareResult) {
        return this.serverPrepareStatementCache.put(key, serverPrepareResult);
    }
    
    private void cmdPrologue() throws SQLException {
        if (this.activeStreamingResult != null) {
            this.activeStreamingResult.loadFully(false, this);
            this.activeStreamingResult = null;
        }
        if (this.activeFutureTask != null) {
            try {
                this.activeFutureTask.get();
            }
            catch (ExecutionException ex) {}
            catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new SQLException("Interrupted reading remaining batch response ", SqlStates.INTERRUPTED_EXCEPTION.getSqlState(), -1, interruptedException);
            }
            finally {
                this.forceReleaseWaitingPrepareStatement();
            }
            this.activeFutureTask = null;
        }
        if (!this.connected) {
            throw this.exceptionFactory.create("Connection is closed", "08000", 1220);
        }
        this.interrupted = false;
    }
    
    @Override
    public void resetStateAfterFailover(final long maxRows, final int transactionIsolationLevel, final String database, final boolean autocommit) throws SQLException {
        this.setMaxRows(maxRows);
        if (transactionIsolationLevel != 0) {
            this.setTransactionIsolation(transactionIsolationLevel);
        }
        if (database != null && !"".equals(database) && !this.getDatabase().equals(database)) {
            this.setCatalog(database);
        }
        if (this.getAutocommit() != autocommit) {
            this.executeQuery("set autocommit=" + (autocommit ? "1" : "0"));
        }
    }
    
    @Override
    public SQLException handleIoException(final Exception initialException) {
        boolean mustReconnect = this.options.autoReconnect;
        boolean maxSizeError;
        if (initialException instanceof MaxAllowedPacketException) {
            maxSizeError = true;
            if (!((MaxAllowedPacketException)initialException).isMustReconnect()) {
                return new SQLNonTransientConnectionException(initialException.getMessage() + this.getTraces(), SqlStates.UNDEFINED_SQLSTATE.getSqlState(), initialException);
            }
            mustReconnect = true;
        }
        else {
            maxSizeError = this.writer.exceedMaxLength();
            if (maxSizeError) {
                mustReconnect = true;
            }
        }
        if (mustReconnect && !this.explicitClosed) {
            final String traces = this.getTraces();
            try {
                this.connect();
                try {
                    this.resetStateAfterFailover(this.getMaxRows(), this.getTransactionIsolationLevel(), this.getDatabase(), this.getAutocommit());
                    if (maxSizeError) {
                        return new SQLTransientConnectionException("Could not send query: query size is >= to max_allowed_packet (" + this.writer.getMaxAllowedPacket() + ")" + traces, "HY000", initialException);
                    }
                    this.cleanMemory();
                    return new SQLTransientConnectionException(initialException.getMessage() + traces, "HY000", initialException);
                }
                catch (SQLException queryException) {
                    return new SQLTransientConnectionException("reconnection succeed, but resetting previous state failed" + traces, "HY000", initialException);
                }
            }
            catch (SQLException queryException) {
                this.connected = false;
                return new SQLNonTransientConnectionException(initialException.getMessage() + "\nError during reconnection" + traces, "08000", queryException);
            }
        }
        this.connected = false;
        return new SQLNonTransientConnectionException(initialException.getMessage() + this.getTraces(), "08000", initialException);
    }
    
    @Override
    public void setActiveFutureTask(final FutureTask activeFutureTask) {
        this.activeFutureTask = activeFutureTask;
    }
    
    @Override
    public void interrupt() {
        this.interrupted = true;
    }
    
    @Override
    public boolean isInterrupted() {
        return this.interrupted;
    }
    
    @Override
    public void stopIfInterrupted() throws SQLTimeoutException {
        if (this.isInterrupted()) {
            throw new SQLTimeoutException("Timeout during batch execution");
        }
    }
    
    @Override
    public void setChecksum(final long checksum) {
        this.checksum = checksum;
    }
    
    @Override
    public long getChecksum() {
        return this.checksum;
    }
    
    @Override
    public void resetChecksum() {
        this.checksum = 1L;
    }
    
    @Override
    public void setIterationCount(final int iterationCount) {
        this.iterationCount = iterationCount;
    }
    
    @Override
    public int getIterationCount() {
        return this.iterationCount;
    }
    
    @Override
    public void setExecuteMode(final int executeMode) {
        this.executeMode = executeMode;
    }
    
    @Override
    public int getExecuteMode() {
        return this.executeMode;
    }
    
    @Override
    public void setComStmtPrepareExecuteField(final int iterationCount, final int executeMode, final long checksum) {
        this.iterationCount = iterationCount;
        this.executeMode = executeMode;
        this.checksum = checksum;
    }
    
    static {
        logger = LoggerFactory.getLogger(AbstractQueryProtocol.class);
        LOCK_DEADLOCK_ERROR_CODES = new HashSet<Integer>(Arrays.asList(1205, 1213, 1614));
    }
}
