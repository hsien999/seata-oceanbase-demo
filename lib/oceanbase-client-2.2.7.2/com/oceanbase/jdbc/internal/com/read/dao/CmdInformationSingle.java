// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.dao;

import com.oceanbase.jdbc.JDBC4ResultSet;
import java.sql.ResultSet;
import com.oceanbase.jdbc.internal.protocol.Protocol;

public class CmdInformationSingle implements CmdInformation
{
    private final long insertId;
    private final int autoIncrement;
    private long updateCount;
    
    public CmdInformationSingle(final long insertId, final long updateCount, final int autoIncrement) {
        this.insertId = insertId;
        this.updateCount = updateCount;
        this.autoIncrement = autoIncrement;
    }
    
    @Override
    public int[] getUpdateCounts() {
        return new int[] { (int)this.updateCount };
    }
    
    @Override
    public int[] getServerUpdateCounts() {
        return new int[] { (int)this.updateCount };
    }
    
    @Override
    public long[] getLargeUpdateCounts() {
        return new long[] { this.updateCount };
    }
    
    @Override
    public int getUpdateCount() {
        return (int)this.updateCount;
    }
    
    @Override
    public long getLargeUpdateCount() {
        return this.updateCount;
    }
    
    @Override
    public void addErrorStat() {
    }
    
    @Override
    public void reset() {
    }
    
    @Override
    public void addResultSetStat() {
    }
    
    @Override
    public ResultSet getGeneratedKeys(final Protocol protocol, final String sql) {
        if (this.insertId == 0L) {
            final long[] insertIds = new long[0];
            return JDBC4ResultSet.createGeneratedData(insertIds, protocol, true);
        }
        if (this.updateCount > 1L && sql != null && !this.isDuplicateKeyUpdate(sql)) {
            final long[] insertIds = new long[(int)this.updateCount];
            for (int i = 0; i < this.updateCount; ++i) {
                insertIds[i] = this.insertId + i * this.autoIncrement;
            }
            return JDBC4ResultSet.createGeneratedData(insertIds, protocol, true);
        }
        return JDBC4ResultSet.createGeneratedData(new long[] { this.insertId }, protocol, true);
    }
    
    private boolean isDuplicateKeyUpdate(final String sql) {
        return sql.matches("(?i).*ON\\s+DUPLICATE\\s+KEY\\s+UPDATE.*");
    }
    
    @Override
    public ResultSet getBatchGeneratedKeys(final Protocol protocol) {
        return this.getGeneratedKeys(protocol, null);
    }
    
    @Override
    public int getCurrentStatNumber() {
        return 1;
    }
    
    @Override
    public boolean moreResults() {
        this.updateCount = -1L;
        return false;
    }
    
    @Override
    public boolean isCurrentUpdateCount() {
        return this.updateCount != -1L;
    }
    
    @Override
    public void addSuccessStat(final long updateCount, final long insertId) {
    }
    
    @Override
    public void setRewrite(final boolean rewritten) {
    }
}
