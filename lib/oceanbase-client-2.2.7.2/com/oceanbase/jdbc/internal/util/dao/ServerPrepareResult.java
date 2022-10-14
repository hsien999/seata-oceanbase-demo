// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.dao;

import com.oceanbase.jdbc.internal.protocol.Protocol;
import java.util.concurrent.atomic.AtomicBoolean;
import com.oceanbase.jdbc.internal.ColumnType;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;

public class ServerPrepareResult implements PrepareResult
{
    private final String sql;
    private int statementId;
    private final ColumnDefinition[] columns;
    private final ColumnDefinition[] parameters;
    private ColumnType[] parameterTypeHeader;
    private final AtomicBoolean inCache;
    private Protocol unProxiedProtocol;
    private volatile int shareCounter;
    private volatile boolean isBeingDeallocate;
    
    public ServerPrepareResult(final String sql, final int statementId, final ColumnDefinition[] columns, final ColumnDefinition[] parameters, final Protocol unProxiedProtocol) {
        this.inCache = new AtomicBoolean();
        this.shareCounter = 1;
        this.sql = sql;
        this.statementId = statementId;
        this.columns = columns;
        this.parameters = parameters;
        this.parameterTypeHeader = new ColumnType[parameters.length];
        this.unProxiedProtocol = unProxiedProtocol;
    }
    
    public void resetParameterTypeHeader() {
        this.parameterTypeHeader = new ColumnType[this.parameters.length];
    }
    
    public void failover(final int statementId, final Protocol unProxiedProtocol) {
        this.statementId = statementId;
        this.unProxiedProtocol = unProxiedProtocol;
        this.parameterTypeHeader = new ColumnType[this.parameters.length];
        this.shareCounter = 1;
        this.isBeingDeallocate = false;
    }
    
    public void setAddToCache() {
        this.inCache.set(true);
    }
    
    public void setRemoveFromCache() {
        this.inCache.set(false);
    }
    
    public synchronized boolean incrementShareCounter() {
        if (this.isBeingDeallocate) {
            return false;
        }
        ++this.shareCounter;
        return true;
    }
    
    public synchronized void decrementShareCounter() {
        --this.shareCounter;
    }
    
    public synchronized boolean canBeDeallocate() {
        return this.shareCounter <= 0 && !this.isBeingDeallocate && !this.inCache.get() && (this.isBeingDeallocate = true);
    }
    
    @Override
    public int getParamCount() {
        return this.parameters.length;
    }
    
    public synchronized int getShareCounter() {
        return this.shareCounter;
    }
    
    public int getStatementId() {
        return this.statementId;
    }
    
    public ColumnDefinition[] getColumns() {
        return this.columns;
    }
    
    public ColumnDefinition[] getParameters() {
        return this.parameters;
    }
    
    public Protocol getUnProxiedProtocol() {
        return this.unProxiedProtocol;
    }
    
    @Override
    public String getSql() {
        return this.sql;
    }
    
    public ColumnType[] getParameterTypeHeader() {
        return this.parameterTypeHeader;
    }
}
