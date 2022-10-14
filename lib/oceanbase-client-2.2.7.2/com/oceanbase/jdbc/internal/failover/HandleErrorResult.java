// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.failover;

public class HandleErrorResult
{
    public boolean mustThrowError;
    public boolean isReconnected;
    public Object resultObject;
    
    public HandleErrorResult() {
        this.mustThrowError = true;
        this.isReconnected = false;
        this.resultObject = null;
    }
    
    public HandleErrorResult(final boolean isReconnected) {
        this.mustThrowError = true;
        this.isReconnected = false;
        this.resultObject = null;
        this.isReconnected = isReconnected;
    }
    
    public HandleErrorResult(final boolean isReconnected, final boolean mustThrowError) {
        this.mustThrowError = true;
        this.isReconnected = false;
        this.resultObject = null;
        this.isReconnected = isReconnected;
        this.mustThrowError = mustThrowError;
    }
    
    @Override
    public String toString() {
        return "HandleErrorResult{mustThrowError=" + this.mustThrowError + ", isReconnected=" + this.isReconnected + ", resultObject=" + this.resultObject + "}";
    }
}
