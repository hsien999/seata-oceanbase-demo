// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol;

import java.util.LinkedHashMap;
import java.sql.SQLTimeoutException;
import com.oceanbase.jdbc.internal.util.SqlStates;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import com.oceanbase.jdbc.internal.com.send.ComStmtPrepareExecute;
import com.oceanbase.jdbc.internal.com.send.ComStmtPrepare;
import java.io.IOException;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.BulkStatus;
import com.oceanbase.jdbc.internal.util.dao.ClientPrepareResult;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import java.util.concurrent.ThreadPoolExecutor;
import com.oceanbase.jdbc.internal.util.dao.PrepareResult;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import java.util.List;
import com.oceanbase.jdbc.internal.ColumnType;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public abstract class AbstractMultiSend
{
    private final Protocol protocol;
    private final PacketOutputStream writer;
    private final Results results;
    private final boolean binaryProtocol;
    private final boolean readPrepareStmtResult;
    protected int statementId;
    protected ColumnType[] parameterTypeHeader;
    private List<ParameterHolder[]> parametersList;
    private PrepareResult prepareResult;
    private List<String> queries;
    private String sql;
    private ThreadPoolExecutor readScheduler;
    
    public AbstractMultiSend(final Protocol protocol, final PacketOutputStream writer, final Results results, final ServerPrepareResult serverPrepareResult, final List<ParameterHolder[]> parametersList, final boolean readPrepareStmtResult, final String sql, final ThreadPoolExecutor readScheduler) {
        this.statementId = -1;
        this.protocol = protocol;
        this.writer = writer;
        this.results = results;
        this.prepareResult = serverPrepareResult;
        this.parametersList = parametersList;
        this.binaryProtocol = true;
        this.readPrepareStmtResult = readPrepareStmtResult;
        this.sql = sql;
        this.readScheduler = readScheduler;
    }
    
    public AbstractMultiSend(final Protocol protocol, final PacketOutputStream writer, final Results results, final ClientPrepareResult clientPrepareResult, final List<ParameterHolder[]> parametersList, final ThreadPoolExecutor readScheduler) {
        this.statementId = -1;
        this.protocol = protocol;
        this.writer = writer;
        this.results = results;
        this.prepareResult = clientPrepareResult;
        this.parametersList = parametersList;
        this.binaryProtocol = false;
        this.readPrepareStmtResult = false;
        this.readScheduler = readScheduler;
    }
    
    public AbstractMultiSend(final Protocol protocol, final PacketOutputStream writer, final Results results, final List<String> queries, final ThreadPoolExecutor readScheduler) {
        this.statementId = -1;
        this.protocol = protocol;
        this.writer = writer;
        this.results = results;
        this.queries = queries;
        this.binaryProtocol = false;
        this.readPrepareStmtResult = false;
        this.readScheduler = readScheduler;
    }
    
    public abstract void sendCmd(final PacketOutputStream p0, final Results p1, final List<ParameterHolder[]> p2, final List<String> p3, final int p4, final BulkStatus p5, final PrepareResult p6) throws SQLException, IOException;
    
    public abstract SQLException handleResultException(final SQLException p0, final Results p1, final List<ParameterHolder[]> p2, final List<String> p3, final int p4, final int p5, final int p6, final PrepareResult p7);
    
    public abstract int getParamCount();
    
    public abstract int getTotalExecutionNumber();
    
    public PrepareResult getPrepareResult() {
        return this.prepareResult;
    }
    
    public PrepareResult executeBatch() throws SQLException {
        final int paramCount = this.getParamCount();
        if (this.binaryProtocol) {
            if (this.readPrepareStmtResult) {
                this.parameterTypeHeader = new ColumnType[paramCount];
                if (this.prepareResult == null && this.protocol.getOptions().cachePrepStmts && this.protocol.getOptions().useServerPrepStmts) {
                    final String key = this.protocol.getDatabase() + "-" + this.sql;
                    this.prepareResult = ((LinkedHashMap<K, PrepareResult>)this.protocol.prepareStatementCache()).get(key);
                    if (this.prepareResult != null && !((ServerPrepareResult)this.prepareResult).incrementShareCounter()) {
                        this.prepareResult = null;
                    }
                }
                this.statementId = ((this.prepareResult == null) ? -1 : ((ServerPrepareResult)this.prepareResult).getStatementId());
            }
            else if (this.prepareResult != null) {
                this.statementId = ((ServerPrepareResult)this.prepareResult).getStatementId();
            }
        }
        return this.executeBatchStandard(paramCount);
    }
    
    private PrepareResult executeBatchStandard(final int estimatedParameterCount) throws SQLException {
        final int totalExecutionNumber = this.getTotalExecutionNumber();
        SQLException exception = null;
        final BulkStatus status = new BulkStatus();
        FutureTask<AsyncMultiReadResult> futureReadTask = null;
        int paramCount = estimatedParameterCount;
        try {
            do {
                status.sendEnded = false;
                status.sendSubCmdCounter = 0;
                final int requestNumberByBulk = Math.min(totalExecutionNumber - status.sendCmdCounter, this.protocol.getOptions().useBatchMultiSendNumber);
                this.protocol.changeSocketTcpNoDelay(false);
                if (!this.protocol.supportStmtPrepareExecute() && this.readPrepareStmtResult && this.prepareResult == null) {
                    ComStmtPrepare.send(this.writer, this.sql);
                    this.prepareResult = ComStmtPrepare.read(this.protocol.getReader(), this.protocol.isEofDeprecated(), this.protocol, this.sql);
                    this.statementId = ((ServerPrepareResult)this.prepareResult).getStatementId();
                    paramCount = this.getParamCount();
                }
                boolean useCurrentThread = false;
                while (status.sendSubCmdCounter < requestNumberByBulk) {
                    this.sendCmd(this.writer, this.results, this.parametersList, this.queries, paramCount, status, this.prepareResult);
                    final BulkStatus bulkStatus = status;
                    ++bulkStatus.sendSubCmdCounter;
                    final BulkStatus bulkStatus2 = status;
                    ++bulkStatus2.sendCmdCounter;
                    if (useCurrentThread) {
                        try {
                            if (this.protocol.supportStmtPrepareExecute()) {
                                this.prepareResult = ComStmtPrepareExecute.read(this.getProtocol(), this.protocol.getReader(), (ServerPrepareResult)this.prepareResult, this.results);
                            }
                            else {
                                this.protocol.getResult(this.results);
                            }
                        }
                        catch (SQLException qex) {
                            if ((this.readPrepareStmtResult && this.prepareResult == null) || !this.protocol.getOptions().continueBatchOnError) {
                                throw qex;
                            }
                            exception = qex;
                        }
                    }
                    else {
                        if (futureReadTask != null) {
                            continue;
                        }
                        try {
                            futureReadTask = new FutureTask<AsyncMultiReadResult>(new AsyncMultiRead(status, this.protocol, false, this, paramCount, this.results, this.parametersList, this.queries, this.sql, this.prepareResult));
                            this.readScheduler.execute(futureReadTask);
                        }
                        catch (RejectedExecutionException r) {
                            useCurrentThread = true;
                            try {
                                if (this.protocol.supportStmtPrepareExecute()) {
                                    this.prepareResult = ComStmtPrepareExecute.read(this.getProtocol(), this.protocol.getReader(), (ServerPrepareResult)this.prepareResult, this.results);
                                }
                                else {
                                    this.protocol.getResult(this.results);
                                }
                            }
                            catch (SQLException qex2) {
                                if ((this.readPrepareStmtResult && this.prepareResult == null) || !this.protocol.getOptions().continueBatchOnError) {
                                    throw qex2;
                                }
                                exception = qex2;
                            }
                        }
                    }
                }
                status.sendEnded = true;
                if (!useCurrentThread) {
                    this.protocol.changeSocketTcpNoDelay(this.protocol.getOptions().tcpNoDelay);
                    try {
                        final AsyncMultiReadResult asyncMultiReadResult = futureReadTask.get();
                        if (this.binaryProtocol && this.prepareResult == null && asyncMultiReadResult.getPrepareResult() != null) {
                            this.prepareResult = asyncMultiReadResult.getPrepareResult();
                            this.statementId = ((ServerPrepareResult)this.prepareResult).getStatementId();
                            paramCount = this.prepareResult.getParamCount();
                        }
                        if (asyncMultiReadResult.getException() != null) {
                            if ((this.readPrepareStmtResult && this.prepareResult == null) || !this.protocol.getOptions().continueBatchOnError) {
                                throw asyncMultiReadResult.getException();
                            }
                            exception = asyncMultiReadResult.getException();
                        }
                    }
                    catch (ExecutionException executionException) {
                        if (executionException.getCause() == null) {
                            throw new SQLException("Error reading results " + executionException.getMessage());
                        }
                        throw new SQLException("Error reading results " + executionException.getCause().getMessage());
                    }
                    catch (InterruptedException interruptedException) {
                        this.protocol.setActiveFutureTask(futureReadTask);
                        Thread.currentThread().interrupt();
                        throw new SQLException("Interrupted awaiting response ", SqlStates.INTERRUPTED_EXCEPTION.getSqlState(), interruptedException);
                    }
                    finally {
                        this.protocol.forceReleaseWaitingPrepareStatement();
                    }
                }
                if (this.protocol.isInterrupted()) {
                    futureReadTask.cancel(true);
                    throw new SQLTimeoutException("Timeout during batch execution");
                }
                futureReadTask = null;
            } while (status.sendCmdCounter < totalExecutionNumber);
            if (exception != null) {
                throw exception;
            }
            return this.prepareResult;
        }
        catch (IOException e) {
            status.sendEnded = true;
            status.sendCmdCounter = 0;
            throw this.protocol.handleIoException(e);
        }
    }
    
    public Protocol getProtocol() {
        return this.protocol;
    }
}
