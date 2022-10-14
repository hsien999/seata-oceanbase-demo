// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.scheduler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class DynamicSizedSchedulerImpl extends ScheduledThreadPoolExecutor implements DynamicSizedSchedulerInterface
{
    public DynamicSizedSchedulerImpl(final int corePoolSize, final String poolName, final int maximumPoolSize) {
        super(corePoolSize, new OceanBaseThreadFactory(poolName));
        this.setMaximumPoolSize(maximumPoolSize);
    }
    
    @Override
    public void setPoolSize(final int newSize) {
        synchronized (this) {
            super.setCorePoolSize(newSize);
        }
    }
}
