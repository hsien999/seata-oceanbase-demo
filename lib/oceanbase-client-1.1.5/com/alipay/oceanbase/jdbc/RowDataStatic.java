// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.util.List;

public class RowDataStatic implements RowData
{
    private Field[] metadata;
    private int index;
    ResultSetImpl owner;
    private List<ResultSetRow> rows;
    
    public RowDataStatic(final List<ResultSetRow> rows) {
        this.index = -1;
        this.rows = rows;
    }
    
    @Override
    public void addRow(final ResultSetRow row) {
        this.rows.add(row);
    }
    
    @Override
    public void afterLast() {
        if (this.rows.size() > 0) {
            this.index = this.rows.size();
        }
    }
    
    @Override
    public void beforeFirst() {
        if (this.rows.size() > 0) {
            this.index = -1;
        }
    }
    
    @Override
    public void beforeLast() {
        if (this.rows.size() > 0) {
            this.index = this.rows.size() - 2;
        }
    }
    
    @Override
    public void close() {
    }
    
    @Override
    public ResultSetRow getAt(final int atIndex) throws SQLException {
        if (atIndex < 0 || atIndex >= this.rows.size()) {
            return null;
        }
        return this.rows.get(atIndex).setMetadata(this.metadata);
    }
    
    @Override
    public int getCurrentRowNumber() {
        return this.index;
    }
    
    @Override
    public ResultSetInternalMethods getOwner() {
        return this.owner;
    }
    
    @Override
    public boolean hasNext() {
        final boolean hasMore = this.index + 1 < this.rows.size();
        return hasMore;
    }
    
    @Override
    public boolean isAfterLast() {
        return this.index >= this.rows.size() && this.rows.size() != 0;
    }
    
    @Override
    public boolean isBeforeFirst() {
        return this.index == -1 && this.rows.size() != 0;
    }
    
    @Override
    public boolean isDynamic() {
        return false;
    }
    
    @Override
    public boolean isEmpty() {
        return this.rows.size() == 0;
    }
    
    @Override
    public boolean isFirst() {
        return this.index == 0;
    }
    
    @Override
    public boolean isLast() {
        return this.rows.size() != 0 && this.index == this.rows.size() - 1;
    }
    
    @Override
    public void moveRowRelative(final int rowsToMove) {
        if (this.rows.size() > 0) {
            this.index += rowsToMove;
            if (this.index < -1) {
                this.beforeFirst();
            }
            else if (this.index > this.rows.size()) {
                this.afterLast();
            }
        }
    }
    
    @Override
    public ResultSetRow next() throws SQLException {
        ++this.index;
        if (this.index > this.rows.size()) {
            this.afterLast();
        }
        else if (this.index < this.rows.size()) {
            final ResultSetRow row = this.rows.get(this.index);
            return row.setMetadata(this.metadata);
        }
        return null;
    }
    
    @Override
    public void removeRow(final int atIndex) {
        this.rows.remove(atIndex);
    }
    
    @Override
    public void setCurrentRow(final int newIndex) {
        this.index = newIndex;
    }
    
    @Override
    public void setOwner(final ResultSetImpl rs) {
        this.owner = rs;
    }
    
    @Override
    public int size() {
        return this.rows.size();
    }
    
    @Override
    public boolean wasEmpty() {
        return this.rows != null && this.rows.size() == 0;
    }
    
    @Override
    public void setMetadata(final Field[] metadata) {
        this.metadata = metadata;
    }
}
