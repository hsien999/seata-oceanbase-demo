// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;

public class OceanBaseThreadFactory implements ThreadFactory
{
    private final ThreadFactory parentFactory;
    private final AtomicInteger threadId;
    private final String threadName;
    
    public OceanBaseThreadFactory(final String threadName) {
        this.parentFactory = Executors.defaultThreadFactory();
        this.threadId = new AtomicInteger();
        this.threadName = threadName;
    }
    
    @Override
    public Thread newThread(final Runnable runnable) {
        final Thread result = this.parentFactory.newThread(runnable);
        result.setName(this.threadName + "-" + this.threadId.incrementAndGet());
        result.setDaemon(true);
        return result;
    }
}
