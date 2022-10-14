// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util;

public class BulkStatus
{
    public volatile int sendSubCmdCounter;
    public volatile boolean sendEnded;
    public int sendCmdCounter;
    
    public BulkStatus() {
        this.sendCmdCounter = 0;
    }
}
