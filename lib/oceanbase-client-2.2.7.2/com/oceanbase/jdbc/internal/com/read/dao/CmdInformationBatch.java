// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.dao;

import com.oceanbase.jdbc.JDBC4ResultSet;
import java.sql.ResultSet;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import java.util.Iterator;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Queue;

public class CmdInformationBatch implements CmdInformation
{
    private final Queue<Long> insertIds;
    private final Queue<Long> updateCounts;
    private final int expectedSize;
    private final int autoIncrement;
    private int insertIdNumber;
    private boolean hasException;
    private boolean rewritten;
    
    public CmdInformationBatch(final int expectedSize, final int autoIncrement) {
        this.insertIds = new ConcurrentLinkedQueue<Long>();
        this.updateCounts = new ConcurrentLinkedQueue<Long>();
        this.insertIdNumber = 0;
        this.expectedSize = expectedSize;
        this.autoIncrement = autoIncrement;
    }
    
    @Override
    public void addErrorStat() {
        this.hasException = true;
        this.updateCounts.add(-3L);
    }
    
    @Override
    public void reset() {
        this.insertIds.clear();
        this.updateCounts.clear();
        this.insertIdNumber = 0;
        this.hasException = false;
        this.rewritten = false;
    }
    
    @Override
    public void addResultSetStat() {
        this.updateCounts.add(-1L);
    }
    
    @Override
    public void addSuccessStat(final long updateCount, final long insertId) {
        this.insertIds.add(insertId);
        this.insertIdNumber += (int)updateCount;
        this.updateCounts.add(updateCount);
    }
    
    @Override
    public int[] getUpdateCounts() {
        if (this.rewritten) {
            final int[] ret = new int[this.expectedSize];
            int resultValue;
            if (this.hasException) {
                resultValue = -3;
            }
            else if (this.expectedSize == 1) {
                resultValue = this.updateCounts.element().intValue();
            }
            else {
                resultValue = 0;
                final Iterator<Long> iterator = this.updateCounts.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().intValue() != 0) {
                        resultValue = -2;
                    }
                }
            }
            Arrays.fill(ret, resultValue);
            return ret;
        }
        final int[] ret = new int[Math.max(this.updateCounts.size(), this.expectedSize)];
        final Iterator<Long> iterator2 = this.updateCounts.iterator();
        int pos = 0;
        while (iterator2.hasNext()) {
            ret[pos++] = iterator2.next().intValue();
        }
        while (pos < ret.length) {
            ret[pos++] = -3;
        }
        return ret;
    }
    
    @Override
    public int[] getServerUpdateCounts() {
        final int[] ret = new int[this.updateCounts.size()];
        final Iterator<Long> iterator = this.updateCounts.iterator();
        int pos = 0;
        while (iterator.hasNext()) {
            ret[pos++] = iterator.next().intValue();
        }
        return ret;
    }
    
    @Override
    public long[] getLargeUpdateCounts() {
        if (this.rewritten) {
            final long[] ret = new long[this.expectedSize];
            long resultValue;
            if (this.hasException) {
                resultValue = -3L;
            }
            else if (this.expectedSize == 1) {
                resultValue = this.updateCounts.element();
            }
            else {
                resultValue = 0L;
                final Iterator<Long> iterator = this.updateCounts.iterator();
                while (iterator.hasNext()) {
                    if (iterator.next().intValue() != 0) {
                        resultValue = -2L;
                    }
                }
            }
            Arrays.fill(ret, resultValue);
            return ret;
        }
        final long[] ret = new long[Math.max(this.updateCounts.size(), this.expectedSize)];
        final Iterator<Long> iterator2 = this.updateCounts.iterator();
        int pos = 0;
        while (iterator2.hasNext()) {
            ret[pos++] = iterator2.next();
        }
        while (pos < ret.length) {
            ret[pos++] = -3L;
        }
        return ret;
    }
    
    @Override
    public int getUpdateCount() {
        final Long updateCount = this.updateCounts.peek();
        return (updateCount == null) ? -1 : updateCount.intValue();
    }
    
    @Override
    public long getLargeUpdateCount() {
        final Long updateCount = this.updateCounts.peek();
        return (updateCount == null) ? -1L : updateCount;
    }
    
    @Override
    public ResultSet getBatchGeneratedKeys(final Protocol protocol) {
        final long[] ret = new long[this.insertIdNumber];
        int position = 0;
        final Iterator<Long> idIterator = this.insertIds.iterator();
        for (final Long updateCountLong : this.updateCounts) {
            final int updateCount = updateCountLong.intValue();
            final long insertId;
            if (updateCount != -3 && updateCount != -1 && (insertId = idIterator.next()) > 0L) {
                for (int i = 0; i < updateCount; ++i) {
                    ret[position++] = insertId + i * this.autoIncrement;
                }
            }
        }
        return JDBC4ResultSet.createGeneratedData(ret, protocol, true);
    }
    
    @Override
    public ResultSet getGeneratedKeys(final Protocol protocol, final String sql) {
        final long[] ret = new long[this.insertIdNumber];
        int position = 0;
        final Iterator<Long> idIterator = this.insertIds.iterator();
        for (final Long updateCountLong : this.updateCounts) {
            final int updateCount = updateCountLong.intValue();
            final long insertId;
            if (updateCount != -3 && updateCount != -1 && (insertId = idIterator.next()) > 0L) {
                for (int i = 0; i < updateCount; ++i) {
                    ret[position++] = insertId + i * this.autoIncrement;
                }
            }
        }
        return JDBC4ResultSet.createGeneratedData(ret, protocol, true);
    }
    
    @Override
    public int getCurrentStatNumber() {
        return this.updateCounts.size();
    }
    
    @Override
    public boolean moreResults() {
        return false;
    }
    
    @Override
    public boolean isCurrentUpdateCount() {
        return false;
    }
    
    @Override
    public void setRewrite(final boolean rewritten) {
        this.rewritten = rewritten;
    }
}
