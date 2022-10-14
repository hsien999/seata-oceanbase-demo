// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.ArrayList;
import java.sql.SQLException;
import java.util.List;

public class RowDataCursor implements RowData
{
    private static final int BEFORE_START_OF_ROWS = -1;
    private List<ResultSetRow> fetchedRows;
    private int currentPositionInEntireResult;
    private int currentPositionInFetchedRows;
    private ResultSetImpl owner;
    private boolean lastRowFetched;
    private Field[] metadata;
    private MysqlIO mysql;
    private long statementIdOnServer;
    private ServerPreparedStatement prepStmt;
    private static final int SERVER_STATUS_LAST_ROW_SENT = 128;
    private boolean firstFetchCompleted;
    private boolean wasEmpty;
    private boolean useBufferRowExplicit;
    private boolean is_close;
    private int numRowsToFetch;
    
    public List<ResultSetRow> getFetchedRows() {
        return this.fetchedRows;
    }
    
    public long getStatementIdOnServer() {
        return this.statementIdOnServer;
    }
    
    public void setNumRowsToFetch(final int numRowsToFetch) {
        this.numRowsToFetch = numRowsToFetch;
    }
    
    public RowDataCursor(final MysqlIO ioChannel, final ServerPreparedStatement creatingStatement, final Field[] metadata) {
        this.currentPositionInEntireResult = -1;
        this.currentPositionInFetchedRows = -1;
        this.lastRowFetched = false;
        this.firstFetchCompleted = false;
        this.wasEmpty = false;
        this.useBufferRowExplicit = false;
        this.is_close = false;
        this.numRowsToFetch = 5;
        this.currentPositionInEntireResult = -1;
        this.metadata = metadata;
        this.mysql = ioChannel;
        this.statementIdOnServer = creatingStatement.getServerStatementId();
        this.prepStmt = creatingStatement;
        this.useBufferRowExplicit = MysqlIO.useBufferRowExplicit(this.metadata);
    }
    
    public RowDataCursor(final MysqlIO ioChannel, final long cursorId) {
        this.currentPositionInEntireResult = -1;
        this.currentPositionInFetchedRows = -1;
        this.lastRowFetched = false;
        this.firstFetchCompleted = false;
        this.wasEmpty = false;
        this.useBufferRowExplicit = false;
        this.is_close = false;
        this.numRowsToFetch = 5;
        this.currentPositionInEntireResult = -1;
        this.mysql = ioChannel;
        this.statementIdOnServer = cursorId;
        this.useBufferRowExplicit = MysqlIO.useBufferRowExplicit(this.metadata);
    }
    
    @Override
    public boolean isAfterLast() {
        return this.lastRowFetched && this.currentPositionInFetchedRows > this.fetchedRows.size();
    }
    
    @Override
    public ResultSetRow getAt(final int ind) throws SQLException {
        this.notSupported();
        return null;
    }
    
    @Override
    public boolean isBeforeFirst() throws SQLException {
        return this.currentPositionInEntireResult < 0;
    }
    
    @Override
    public void setCurrentRow(final int rowNumber) throws SQLException {
        this.notSupported();
    }
    
    @Override
    public int getCurrentRowNumber() throws SQLException {
        return this.currentPositionInEntireResult + 1;
    }
    
    @Override
    public boolean isDynamic() {
        return true;
    }
    
    @Override
    public boolean isEmpty() throws SQLException {
        return this.isBeforeFirst() && this.isAfterLast();
    }
    
    @Override
    public boolean isFirst() throws SQLException {
        return this.currentPositionInEntireResult == 0;
    }
    
    @Override
    public boolean isLast() throws SQLException {
        return this.lastRowFetched && this.currentPositionInFetchedRows == this.fetchedRows.size() - 1;
    }
    
    @Override
    public void addRow(final ResultSetRow row) throws SQLException {
        this.notSupported();
    }
    
    @Override
    public void afterLast() throws SQLException {
        this.notSupported();
    }
    
    @Override
    public void beforeFirst() throws SQLException {
        this.notSupported();
    }
    
    @Override
    public void beforeLast() throws SQLException {
        this.notSupported();
    }
    
    @Override
    public void close() throws SQLException {
        this.metadata = null;
        this.owner = null;
    }
    
    @Override
    public boolean hasNext() throws SQLException {
        if (this.fetchedRows != null && this.fetchedRows.size() == 0) {
            return false;
        }
        if (this.fetchedRows != null && this.currentPositionInFetchedRows == -1) {
            return this.fetchedRows.size() > 0;
        }
        if (this.owner != null && this.owner.owningStatement != null) {
            final int maxRows = this.owner.owningStatement.maxRows;
            if (maxRows != -1 && this.currentPositionInEntireResult + 1 > maxRows) {
                return false;
            }
        }
        if (this.currentPositionInEntireResult == -1) {
            this.fetchMoreRows();
            return this.fetchedRows.size() > 0;
        }
        if (this.currentPositionInFetchedRows < this.fetchedRows.size() - 1) {
            return true;
        }
        if (this.currentPositionInFetchedRows == this.fetchedRows.size() && this.lastRowFetched) {
            return false;
        }
        this.currentPositionInFetchedRows = -1;
        this.fetchMoreRows();
        return this.fetchedRows.size() > 0;
    }
    
    @Override
    public void moveRowRelative(final int rows) throws SQLException {
        this.notSupported();
    }
    
    @Override
    public ResultSetRow next() throws SQLException {
        if (this.fetchedRows == null && this.currentPositionInEntireResult != -1) {
            throw SQLError.createSQLException(Messages.getString("ResultSet.Operation_not_allowed_after_ResultSet_closed_144"), "S1000", this.mysql.getExceptionInterceptor());
        }
        if (!this.hasNext()) {
            return null;
        }
        ++this.currentPositionInEntireResult;
        ++this.currentPositionInFetchedRows;
        if (this.fetchedRows != null && this.fetchedRows.size() == 0) {
            return null;
        }
        if (this.fetchedRows == null || this.currentPositionInFetchedRows > this.fetchedRows.size() - 1) {
            this.fetchMoreRows();
            this.currentPositionInFetchedRows = 0;
        }
        final ResultSetRow row = this.fetchedRows.get(this.currentPositionInFetchedRows);
        row.setMetadata(this.metadata);
        return row;
    }
    
    private void fetchMoreRows() throws SQLException {
        if (this.lastRowFetched) {
            this.fetchedRows = new ArrayList<ResultSetRow>(0);
            return;
        }
        synchronized (this.owner.connection.getConnectionMutex()) {
            if (!this.firstFetchCompleted) {
                this.firstFetchCompleted = true;
            }
            int numRowsToFetch = this.owner.getFetchSize();
            if (numRowsToFetch == 0 && this.prepStmt != null) {
                numRowsToFetch = this.prepStmt.getFetchSize();
            }
            if (numRowsToFetch == Integer.MIN_VALUE) {
                numRowsToFetch = 1;
            }
            if (numRowsToFetch == 0) {
                numRowsToFetch = this.numRowsToFetch;
            }
            if (this.fetchedRows == null) {
                this.fetchedRows = new ArrayList<ResultSetRow>(numRowsToFetch);
            }
            else {
                this.fetchedRows.clear();
            }
            this.mysql.sendFechRowViaCursor(this.statementIdOnServer, numRowsToFetch);
            if (this.mysql.isOracleMode()) {
                final Buffer packet = this.mysql.checkErrorPacket();
                packet.setPosition(0);
                final long columnCount = packet.readFieldLength();
                if (this.metadata == null) {
                    final Field[] fields = new Field[(int)columnCount];
                    for (int i = 0; i < columnCount; ++i) {
                        final Buffer fieldPacket = this.mysql.readPacket();
                        fields[i] = this.mysql.unpackField(fieldPacket, false);
                    }
                    this.metadata = fields;
                }
                else {
                    for (int j = 0; j < columnCount; ++j) {
                        this.mysql.skipPacket();
                    }
                }
                this.mysql.readPacket();
            }
            ResultSetRow row;
            while ((row = this.mysql.nextRow(this.metadata, this.metadata.length, true, 1007, false, this.useBufferRowExplicit, false, null)) != null) {
                this.fetchedRows.add(row);
            }
        }
    }
    
    @Override
    public void removeRow(final int ind) throws SQLException {
        this.notSupported();
    }
    
    @Override
    public int size() {
        return -1;
    }
    
    protected void nextRecord() throws SQLException {
    }
    
    private void notSupported() throws SQLException {
        throw new OperationNotSupportedException();
    }
    
    @Override
    public void setOwner(final ResultSetImpl rs) {
        this.owner = rs;
    }
    
    @Override
    public ResultSetInternalMethods getOwner() {
        return this.owner;
    }
    
    @Override
    public boolean wasEmpty() {
        return this.wasEmpty;
    }
    
    @Override
    public void setMetadata(final Field[] metadata) {
        this.metadata = metadata;
    }
    
    public Field[] getMetadata() {
        return this.metadata;
    }
}
