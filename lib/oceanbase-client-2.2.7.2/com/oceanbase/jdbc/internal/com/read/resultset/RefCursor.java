// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.resultset;

import com.oceanbase.jdbc.internal.com.read.dao.ColumnLabelIndexer;
import com.oceanbase.jdbc.internal.com.read.resultset.rowprotocol.BinaryRowProtocol;
import com.oceanbase.jdbc.ServerSidePreparedStatement;
import java.sql.SQLException;
import java.io.IOException;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.extend.datatype.RowObCursorData;

public class RefCursor extends SelectResultSet
{
    private RowObCursorData rowObCursorData;
    private boolean isLastRow;
    
    public RefCursor(final ColumnDefinition[] columnsInformation, final Results results, final Protocol protocol, final boolean callableResult, final boolean eofDeprecated, final boolean isPsOutParamter, final RowObCursorData rowObCursorData) throws IOException, SQLException {
        super(columnsInformation, results, protocol, callableResult, eofDeprecated, isPsOutParamter);
        this.isLastRow = false;
        this.rowObCursorData = rowObCursorData;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (super.next()) {
            return true;
        }
        super.resetState();
        this.cursorFetch();
        return super.next();
    }
    
    private void cursorFetch() throws SQLException {
        if (this.isLastRow) {
            return;
        }
        final int numToFetch = this.getFetchSize();
        ColumnDefinition[] ci = new ColumnDefinition[0];
        try {
            ci = ((ServerSidePreparedStatement)this.getStatement()).cursorFetch(this.rowObCursorData.getCursorId(), numToFetch);
        }
        catch (SQLException e) {
            if ("ORA-01002: fetch out of sequence".equals(e.getMessage())) {
                this.isLastRow = true;
                return;
            }
            throw e;
        }
        if (ci != null) {
            this.columnsInformation = ci;
            this.columnInformationLength = ci.length;
            this.row = new BinaryRowProtocol(this.columnsInformation, this.columnInformationLength, this.getStatement().getMaxFieldSize(), this.options);
            this.columnLabelIndexer = new ColumnLabelIndexer(this.columnsInformation);
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
    public boolean last() throws SQLException {
        throw new SQLException("Operation not supported for streaming result set");
    }
    
    @Override
    public void beforeFirst() throws SQLException {
        throw new SQLException("Operation not supported for streaming result set");
    }
    
    @Override
    public void close() throws SQLException {
        this.rowObCursorData.setOpen(false);
    }
}
