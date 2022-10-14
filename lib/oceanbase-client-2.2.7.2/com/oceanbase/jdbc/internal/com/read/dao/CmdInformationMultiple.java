// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.dao;

import com.oceanbase.jdbc.JDBC4ResultSet;
import java.sql.ResultSet;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;

public class CmdInformationMultiple implements CmdInformation
{
    private final ArrayList<Long> insertIds;
    private final ArrayList<Long> updateCounts;
    private final int expectedSize;
    private final int autoIncrement;
    private int insertIdNumber;
    private int moreResults;
    private boolean hasException;
    private boolean rewritten;
    
    public CmdInformationMultiple(final int expectedSize, final int autoIncrement) {
        this.insertIdNumber = 0;
        this.insertIds = new ArrayList<Long>(expectedSize);
        this.updateCounts = new ArrayList<Long>(expectedSize);
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
        this.moreResults = 0;
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
    public int[] getUpdateCounts() {
        if (this.rewritten) {
            final int[] ret = new int[this.expectedSize];
            Arrays.fill(ret, this.hasException ? -3 : -2);
            return ret;
        }
        final int[] ret = new int[Math.max(this.updateCounts.size(), this.expectedSize)];
        final Iterator<Long> iterator = this.updateCounts.iterator();
        int pos = 0;
        while (iterator.hasNext()) {
            ret[pos++] = iterator.next().intValue();
        }
        while (pos < ret.length) {
            ret[pos++] = -3;
        }
        return ret;
    }
    
    @Override
    public long[] getLargeUpdateCounts() {
        if (this.rewritten) {
            final long[] ret = new long[this.expectedSize];
            Arrays.fill(ret, this.hasException ? -3L : -2L);
            return ret;
        }
        final long[] ret = new long[Math.max(this.updateCounts.size(), this.expectedSize)];
        final Iterator<Long> iterator = this.updateCounts.iterator();
        int pos = 0;
        while (iterator.hasNext()) {
            ret[pos++] = iterator.next();
        }
        while (pos < ret.length) {
            ret[pos++] = -3L;
        }
        return ret;
    }
    
    @Override
    public int getUpdateCount() {
        if (this.moreResults >= this.updateCounts.size()) {
            return -1;
        }
        return this.updateCounts.get(this.moreResults).intValue();
    }
    
    @Override
    public long getLargeUpdateCount() {
        if (this.moreResults >= this.updateCounts.size()) {
            return -1L;
        }
        return this.updateCounts.get(this.moreResults);
    }
    
    @Override
    public ResultSet getBatchGeneratedKeys(final Protocol protocol) {
        final long[] ret = new long[this.insertIdNumber];
        int position = 0;
        final Iterator<Long> idIterator = this.insertIds.iterator();
        for (final Long updateCount : this.updateCounts) {
            final long insertId;
            if (updateCount != -3L && updateCount != -1L && (insertId = idIterator.next()) > 0L) {
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
        final Iterator<Long> updateIterator = this.updateCounts.iterator();
        for (int element = 0; element <= this.moreResults; ++element) {
            final long updateCount = updateIterator.next();
            final long insertId;
            if (updateCount != -3L && updateCount != -1L && (insertId = idIterator.next()) > 0L && element == this.moreResults) {
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
        return this.moreResults++ < this.updateCounts.size() - 1 && this.updateCounts.get(this.moreResults) == -1L;
    }
    
    @Override
    public boolean isCurrentUpdateCount() {
        return this.updateCounts.get(this.moreResults) != -1L;
    }
    
    @Override
    public void setRewrite(final boolean rewritten) {
        this.rewritten = rewritten;
    }
}
