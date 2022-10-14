// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import com.alipay.oceanbase.jdbc.profiler.ProfilerEventHandler;
import java.sql.Statement;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEvent;
import java.sql.SQLException;

public class RowDataDynamic implements RowData
{
    private int columnCount;
    private Field[] metadata;
    private int index;
    private MysqlIO io;
    private boolean isAfterEnd;
    private boolean noMoreRows;
    private boolean isBinaryEncoded;
    private ResultSetRow nextRow;
    private ResultSetImpl owner;
    private boolean streamerClosed;
    private boolean wasEmpty;
    private boolean useBufferRowExplicit;
    private boolean moreResultsExisted;
    private ExceptionInterceptor exceptionInterceptor;
    
    public RowDataDynamic(final MysqlIO io, final int colCount, final Field[] fields, final boolean isBinaryEncoded) throws SQLException {
        this.index = -1;
        this.isAfterEnd = false;
        this.noMoreRows = false;
        this.isBinaryEncoded = false;
        this.streamerClosed = false;
        this.wasEmpty = false;
        this.io = io;
        this.columnCount = colCount;
        this.isBinaryEncoded = isBinaryEncoded;
        this.metadata = fields;
        this.exceptionInterceptor = this.io.getExceptionInterceptor();
        this.useBufferRowExplicit = MysqlIO.useBufferRowExplicit(this.metadata);
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
        Object mutex = this;
        MySQLConnection conn = null;
        if (this.owner != null) {
            conn = this.owner.connection;
            if (conn != null) {
                mutex = conn.getConnectionMutex();
            }
        }
        boolean hadMore = false;
        int howMuchMore = 0;
        synchronized (mutex) {
            while (this.next() != null) {
                hadMore = true;
                if (++howMuchMore % 100 == 0) {
                    Thread.yield();
                }
            }
            if (conn != null) {
                if (!conn.getClobberStreamingResults() && conn.getNetTimeoutForStreamingResults() > 0) {
                    String oldValue = conn.getServerVariable("net_write_timeout");
                    if (oldValue == null || oldValue.length() == 0) {
                        oldValue = "60";
                    }
                    this.io.clearInputStream();
                    Statement stmt = null;
                    try {
                        stmt = conn.createStatement();
                        ((StatementImpl)stmt).executeSimpleNonQuery(conn, "SET net_write_timeout=" + oldValue);
                    }
                    finally {
                        if (stmt != null) {
                            stmt.close();
                        }
                    }
                }
                if (conn.getUseUsageAdvisor() && hadMore) {
                    final ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(conn);
                    eventSink.consumeEvent(new ProfilerEvent((byte)0, "", (this.owner.owningStatement == null) ? "N/A" : this.owner.owningStatement.currentCatalog, this.owner.connectionId, (this.owner.owningStatement == null) ? -1 : this.owner.owningStatement.getId(), -1, System.currentTimeMillis(), 0L, Constants.MILLIS_I18N, null, null, Messages.getString("RowDataDynamic.2") + howMuchMore + Messages.getString("RowDataDynamic.3") + Messages.getString("RowDataDynamic.4") + Messages.getString("RowDataDynamic.5") + Messages.getString("RowDataDynamic.6") + this.owner.pointOfOrigin));
                }
            }
        }
        this.metadata = null;
        this.owner = null;
    }
    
    @Override
    public ResultSetRow getAt(final int ind) throws SQLException {
        this.notSupported();
        return null;
    }
    
    @Override
    public int getCurrentRowNumber() throws SQLException {
        this.notSupported();
        return -1;
    }
    
    @Override
    public ResultSetInternalMethods getOwner() {
        return this.owner;
    }
    
    @Override
    public boolean hasNext() throws SQLException {
        final boolean hasNext = this.nextRow != null;
        if (!hasNext && !this.streamerClosed) {
            this.io.closeStreamer(this);
            this.streamerClosed = true;
        }
        return hasNext;
    }
    
    @Override
    public boolean isAfterLast() throws SQLException {
        return this.isAfterEnd;
    }
    
    @Override
    public boolean isBeforeFirst() throws SQLException {
        return this.index < 0;
    }
    
    @Override
    public boolean isDynamic() {
        return true;
    }
    
    @Override
    public boolean isEmpty() throws SQLException {
        this.notSupported();
        return false;
    }
    
    @Override
    public boolean isFirst() throws SQLException {
        this.notSupported();
        return false;
    }
    
    @Override
    public boolean isLast() throws SQLException {
        this.notSupported();
        return false;
    }
    
    @Override
    public void moveRowRelative(final int rows) throws SQLException {
        this.notSupported();
    }
    
    @Override
    public ResultSetRow next() throws SQLException {
        this.nextRecord();
        if (this.nextRow == null && !this.streamerClosed && !this.moreResultsExisted) {
            this.io.closeStreamer(this);
            this.streamerClosed = true;
        }
        if (this.nextRow != null && this.index != Integer.MAX_VALUE) {
            ++this.index;
        }
        return this.nextRow;
    }
    
    private void nextRecord() throws SQLException {
        try {
            if (!this.noMoreRows) {
                this.nextRow = this.io.nextRow(this.metadata, this.columnCount, this.isBinaryEncoded, 1007, true, this.useBufferRowExplicit, true, null);
                if (this.nextRow == null) {
                    this.noMoreRows = true;
                    this.isAfterEnd = true;
                    this.moreResultsExisted = this.io.tackOnMoreStreamingResults(this.owner);
                    if (this.index == -1) {
                        this.wasEmpty = true;
                    }
                }
            }
            else {
                this.nextRow = null;
                this.isAfterEnd = true;
            }
        }
        catch (SQLException sqlEx) {
            if (sqlEx instanceof StreamingNotifiable) {
                ((StreamingNotifiable)sqlEx).setWasStreamingResults();
            }
            this.noMoreRows = true;
            throw sqlEx;
        }
        catch (Exception ex) {
            final String exceptionType = ex.getClass().getName();
            String exceptionMessage = ex.getMessage();
            exceptionMessage += Messages.getString("RowDataDynamic.7");
            exceptionMessage += Util.stackTraceToString(ex);
            final SQLException sqlEx2 = SQLError.createSQLException(Messages.getString("RowDataDynamic.8") + exceptionType + Messages.getString("RowDataDynamic.9") + exceptionMessage, "S1000", this.exceptionInterceptor);
            sqlEx2.initCause(ex);
            throw sqlEx2;
        }
    }
    
    private void notSupported() throws SQLException {
        throw new OperationNotSupportedException();
    }
    
    @Override
    public void removeRow(final int ind) throws SQLException {
        this.notSupported();
    }
    
    @Override
    public void setCurrentRow(final int rowNumber) throws SQLException {
        this.notSupported();
    }
    
    @Override
    public void setOwner(final ResultSetImpl rs) {
        this.owner = rs;
    }
    
    @Override
    public int size() {
        return -1;
    }
    
    @Override
    public boolean wasEmpty() {
        return this.wasEmpty;
    }
    
    @Override
    public void setMetadata(final Field[] metadata) {
        this.metadata = metadata;
    }
}
