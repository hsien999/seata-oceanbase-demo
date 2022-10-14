// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol;

import com.oceanbase.jdbc.internal.util.SqlStates;
import java.sql.SQLTransientConnectionException;
import java.sql.SQLNonTransientConnectionException;
import com.oceanbase.jdbc.internal.com.send.ComStmtPrepareExecute;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.com.send.ComStmtPrepare;
import com.oceanbase.jdbc.internal.util.dao.PrepareResult;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import java.util.List;
import com.oceanbase.jdbc.internal.util.BulkStatus;
import java.util.concurrent.Callable;

public class AsyncMultiRead implements Callable<AsyncMultiReadResult>
{
    private final BulkStatus status;
    private final int sendCmdInitialCounter;
    private final Protocol protocol;
    private final boolean readPrepareStmtResult;
    private final AbstractMultiSend bulkSend;
    private final List<ParameterHolder[]> parametersList;
    private final List<String> queries;
    private final String sql;
    private final Results results;
    private final int paramCount;
    private final AsyncMultiReadResult asyncMultiReadResult;
    
    public AsyncMultiRead(final BulkStatus status, final Protocol protocol, final boolean readPrepareStmtResult, final AbstractMultiSend bulkSend, final int paramCount, final Results results, final List<ParameterHolder[]> parametersList, final List<String> queries, final String sql, final PrepareResult prepareResult) {
        this.status = status;
        this.sendCmdInitialCounter = status.sendCmdCounter - 1;
        this.protocol = protocol;
        this.readPrepareStmtResult = readPrepareStmtResult;
        this.bulkSend = bulkSend;
        this.paramCount = paramCount;
        this.results = results;
        this.parametersList = parametersList;
        this.queries = queries;
        this.sql = sql;
        this.asyncMultiReadResult = new AsyncMultiReadResult(prepareResult);
    }
    
    @Override
    public AsyncMultiReadResult call() throws Exception {
        final int initialTimeout = this.protocol.getTimeout();
        if (initialTimeout != 0) {
            this.protocol.changeSocketSoTimeout(0);
        }
        if (this.readPrepareStmtResult) {
            try {
                if (!this.protocol.supportStmtPrepareExecute()) {
                    this.asyncMultiReadResult.setPrepareResult(ComStmtPrepare.read(this.protocol.getReader(), this.protocol.isEofDeprecated(), this.protocol, this.sql));
                }
            }
            catch (SQLException queryException) {
                this.asyncMultiReadResult.setException(queryException);
            }
        }
        int counter = 0;
    Label_0310:
        while (!this.status.sendEnded || counter < this.status.sendSubCmdCounter) {
            while (counter < this.status.sendSubCmdCounter) {
                try {
                    if (this.protocol.supportStmtPrepareExecute()) {
                        this.asyncMultiReadResult.setPrepareResult(ComStmtPrepareExecute.read(this.protocol, this.protocol.getReader(), (ServerPrepareResult)this.asyncMultiReadResult.getPrepareResult(), this.results));
                    }
                    else {
                        this.protocol.getResult(this.results);
                    }
                }
                catch (SQLException qex) {
                    if (qex instanceof SQLNonTransientConnectionException || qex instanceof SQLTransientConnectionException) {
                        this.asyncMultiReadResult.setException(qex);
                        break Label_0310;
                    }
                    if (this.asyncMultiReadResult.getException() == null) {
                        this.asyncMultiReadResult.setException(this.bulkSend.handleResultException(qex, this.results, this.parametersList, this.queries, counter, this.sendCmdInitialCounter, this.paramCount, this.asyncMultiReadResult.getPrepareResult()));
                    }
                }
                ++counter;
                if (Thread.currentThread().isInterrupted()) {
                    this.asyncMultiReadResult.setException(new SQLException("Interrupted reading responses ", SqlStates.INTERRUPTED_EXCEPTION.getSqlState(), -1));
                    break;
                }
            }
        }
        if (initialTimeout != 0) {
            this.protocol.changeSocketSoTimeout(initialTimeout);
        }
        return this.asyncMultiReadResult;
    }
}
