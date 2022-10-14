// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.failover.tools;

public class SearchFilter
{
    private boolean fineIfFoundOnlyMaster;
    private boolean fineIfFoundOnlySlave;
    private boolean initialConnection;
    private boolean failoverLoop;
    
    public SearchFilter() {
    }
    
    public SearchFilter(final boolean fineIfFoundOnlyMaster, final boolean fineIfFoundOnlySlave) {
        this.fineIfFoundOnlyMaster = fineIfFoundOnlyMaster;
        this.fineIfFoundOnlySlave = fineIfFoundOnlySlave;
    }
    
    public SearchFilter(final boolean initialConnection) {
        this.initialConnection = initialConnection;
    }
    
    public boolean isInitialConnection() {
        return this.initialConnection;
    }
    
    public boolean isFineIfFoundOnlyMaster() {
        return this.fineIfFoundOnlyMaster;
    }
    
    public boolean isFineIfFoundOnlySlave() {
        return this.fineIfFoundOnlySlave;
    }
    
    public boolean isFailoverLoop() {
        return this.failoverLoop;
    }
    
    public void setFailoverLoop(final boolean failoverLoop) {
        this.failoverLoop = failoverLoop;
    }
    
    @Override
    public String toString() {
        return "SearchFilter{, fineIfFoundOnlyMaster=" + this.fineIfFoundOnlyMaster + ", fineIfFoundOnlySlave=" + this.fineIfFoundOnlySlave + ", initialConnection=" + this.initialConnection + ", failoverLoop=" + this.failoverLoop + "}";
    }
}
