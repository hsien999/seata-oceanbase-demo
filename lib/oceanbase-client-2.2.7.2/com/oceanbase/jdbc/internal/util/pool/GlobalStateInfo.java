// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.pool;

public class GlobalStateInfo
{
    private final long maxAllowedPacket;
    private final int waitTimeout;
    private final boolean autocommit;
    private final int autoIncrementIncrement;
    private final String timeZone;
    private final String systemTimeZone;
    private final int defaultTransactionIsolation;
    
    public GlobalStateInfo() {
        this.maxAllowedPacket = 1000000L;
        this.waitTimeout = 28800;
        this.autocommit = true;
        this.autoIncrementIncrement = 1;
        this.timeZone = "+00:00";
        this.systemTimeZone = "+00:00";
        this.defaultTransactionIsolation = 4;
    }
    
    public GlobalStateInfo(final long maxAllowedPacket, final int waitTimeout, final boolean autocommit, final int autoIncrementIncrement, final String timeZone, final String systemTimeZone, final int defaultTransactionIsolation) {
        this.maxAllowedPacket = maxAllowedPacket;
        this.waitTimeout = waitTimeout;
        this.autocommit = autocommit;
        this.autoIncrementIncrement = autoIncrementIncrement;
        this.timeZone = timeZone;
        this.systemTimeZone = systemTimeZone;
        this.defaultTransactionIsolation = defaultTransactionIsolation;
    }
    
    public long getMaxAllowedPacket() {
        return this.maxAllowedPacket;
    }
    
    public int getWaitTimeout() {
        return this.waitTimeout;
    }
    
    public boolean isAutocommit() {
        return this.autocommit;
    }
    
    public int getAutoIncrementIncrement() {
        return this.autoIncrementIncrement;
    }
    
    public String getTimeZone() {
        return this.timeZone;
    }
    
    public String getSystemTimeZone() {
        return this.systemTimeZone;
    }
    
    public int getDefaultTransactionIsolation() {
        return this.defaultTransactionIsolation;
    }
}
