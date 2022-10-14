// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;

public class RowDataObCursor extends ResultSetImpl
{
    private MySQLConnection connection;
    RowDataCursor dataCursor;
    boolean isOpen;
    private RowObCursorData rowObCursorData;
    private Field[] metadata;
    
    public void setRowObCursorData(final RowObCursorData rowObCursorData) {
        this.rowObCursorData = rowObCursorData;
    }
    
    public Field[] getMetadata() {
        return this.metadata;
    }
    
    private void notSupported() throws SQLException {
        throw new OperationNotSupportedException();
    }
    
    public RowDataObCursor(final MySQLConnection connection, final int id, final StatementImpl creatorStmt) throws SQLException {
        super(null, null, null, connection, creatorStmt);
        this.isOpen = false;
        this.dataCursor = new RowDataCursor(connection.getIO(), id);
        this.connection = connection;
        this.isOpen = true;
        this.rowData = null;
        this.thisRow = null;
    }
    
    public void setOwner(final ResultSetImpl rs) {
        this.dataCursor.setOwner(rs);
    }
    
    @Override
    public void setFetchSize(final int fetchSize) {
        this.dataCursor.setNumRowsToFetch(fetchSize);
    }
    
    @Override
    public void close() throws SQLException {
        if (this.isOpen && !this.connection.isClosed()) {
            synchronized (this.connection.getConnectionMutex()) {
                final MysqlIO mysql = this.connection.getIO();
                final Buffer packet = mysql.getSharedSendPacket();
                packet.writeByte((byte)25);
                packet.writeLong(this.dataCursor.getStatementIdOnServer());
                mysql.sendCommand(25, null, packet, true, null, 0);
                this.isOpen = false;
                this.rowObCursorData.setOpen(false);
            }
        }
    }
    
    @Override
    public boolean next() throws SQLException {
        if (!this.isOpen) {
            return false;
        }
        final ResultSetRow row = this.dataCursor.next();
        if (row == null) {
            return false;
        }
        this.fields = this.metadata;
        this.rowData = new RowDataStatic(this.dataCursor.getFetchedRows());
        this.thisRow = row;
        if (this.fields == null) {
            this.fields = row.metadata;
            this.initializeWithMetadata();
        }
        return this.onValidRow = true;
    }
    
    public boolean hasNext() throws SQLException {
        if (!this.isOpen) {
            return false;
        }
        final boolean ret = this.dataCursor.hasNext();
        if (this.dataCursor.getMetadata() != null && this.fields == null) {
            this.fields = this.dataCursor.getMetadata();
        }
        return ret;
    }
    
    @Override
    public boolean isFirst() throws SQLException {
        return this.dataCursor.isFirst();
    }
    
    @Override
    public boolean isLast() throws SQLException {
        return this.dataCursor.isLast();
    }
    
    @Override
    public void beforeFirst() throws SQLException {
        this.dataCursor.beforeFirst();
    }
    
    @Override
    public void afterLast() throws SQLException {
        this.dataCursor.afterLast();
    }
}
