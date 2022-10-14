// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.resultset;

import com.oceanbase.jdbc.ServerSidePreparedStatement;
import java.sql.SQLException;
import java.io.IOException;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.com.read.dao.Results;

public class CursorResultSet extends SelectResultSet
{
    int statementId;
    
    public CursorResultSet(final ColumnDefinition[] columnsInformation, final Results results, final Protocol protocol, final boolean callableResult, final boolean eofDeprecated, final boolean isPsOutParamter) throws IOException, SQLException {
        super(columnsInformation, results, protocol, callableResult, eofDeprecated, isPsOutParamter);
        this.statementId = results.getStatementId();
    }
    
    private void cursorFetch() throws SQLException {
        if (this.isLastRow) {
            return;
        }
        final int numToFetch = this.getFetchSize();
        try {
            ((ServerSidePreparedStatement)this.getStatement()).cursorFetch(this.statementId, numToFetch);
        }
        catch (SQLException e) {
            if ("ORA-01002: fetch out of sequence".equals(e.getMessage())) {
                this.isLastRow = true;
                return;
            }
            throw e;
        }
        try {
            this.getCursorFetchData();
        }
        catch (IOException e2) {
            this.handleIoException(e2);
        }
    }
    
    private void getCursorFetchData() throws IOException, SQLException {
        for (int fetchSizeTmp = this.getFetchSize(); fetchSizeTmp >= 0 && super.readNextValue(); --fetchSizeTmp) {}
    }
    
    @Override
    public boolean isLast() throws SQLException {
        if (this.resultSetScrollType == 1003 && this.getProtocol().isOracleMode()) {
            throw new SQLException("Invalid operation on TYPE_FORWARD_ONLY CURSOR ResultSet for oracle mode: isLast");
        }
        if (this.rowPointer < this.dataSize - 1) {
            return false;
        }
        if (this.isEof) {
            return this.isLastRow && this.rowPointer == this.dataSize - 1 && this.dataSize > 0;
        }
        this.cursorFetch();
        return this.isEof && this.isLastRow && this.rowPointer == this.dataSize - 1 && this.dataSize > 0;
    }
    
    @Override
    public void afterLast() throws SQLException {
        if (this.resultSetScrollType == 1003) {
            throw new SQLException("Invalid operation on TYPE_FORWARD_ONLY CURSOR or STREAMING ResultSet: afterLast");
        }
        while (!this.isLastRow) {
            this.cursorFetch();
        }
        this.rowPointer = this.dataSize;
    }
    
    @Override
    public boolean last() throws SQLException {
        if (this.resultSetScrollType == 1003) {
            throw new SQLException("Invalid operation on TYPE_FORWARD_ONLY CURSOR or STREAMING ResultSet: last");
        }
        while (!this.isLastRow) {
            this.cursorFetch();
        }
        this.rowPointer = this.dataSize - 1;
        return this.dataSize > 0;
    }
    
    @Override
    public boolean absolute(final int row) throws SQLException {
        if (this.resultSetScrollType == 1003) {
            throw new SQLException("Invalid operation on TYPE_FORWARD_ONLY CURSOR or STREAMING ResultSet: absolute");
        }
        while (!this.isLastRow && (row > this.dataSize || row < -this.dataSize)) {
            this.cursorFetch();
        }
        if (row >= 0) {
            if (row <= this.dataSize) {
                this.rowPointer = row - 1;
                return true;
            }
            this.rowPointer = this.dataSize;
            return false;
        }
        else {
            if (row >= -this.dataSize) {
                this.rowPointer = this.dataSize + row;
                return true;
            }
            this.rowPointer = -1;
            return false;
        }
    }
    
    @Override
    public boolean relative(final int rows) throws SQLException {
        if (this.resultSetScrollType == 1003) {
            throw new SQLException("Invalid operation on TYPE_FORWARD_ONLY CURSOR or STREAMING ResultSet: relative");
        }
        final int newPos = this.rowPointer + rows;
        if (newPos < 0) {
            this.rowPointer = -1;
            return false;
        }
        return this.absolute(newPos + 1);
    }
    
    @Override
    public boolean next() throws SQLException {
        if (super.next()) {
            return true;
        }
        if (!this.isLastRow) {
            super.resetState();
            this.cursorFetch();
            return super.next();
        }
        return false;
    }
}
