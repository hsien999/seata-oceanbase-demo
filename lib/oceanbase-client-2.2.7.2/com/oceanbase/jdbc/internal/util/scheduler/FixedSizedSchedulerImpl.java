// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.scheduler;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class FixedSizedSchedulerImpl extends ScheduledThreadPoolExecutor
{
    public FixedSizedSchedulerImpl(final int corePoolSize, final String poolName) {
        super(corePoolSize, new OceanBaseThreadFactory(poolName));
    }
}
