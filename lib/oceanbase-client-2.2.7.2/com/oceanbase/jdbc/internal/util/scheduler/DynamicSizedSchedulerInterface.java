// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.scheduler;

import java.util.concurrent.ScheduledExecutorService;

public interface DynamicSizedSchedulerInterface extends ScheduledExecutorService
{
    void setPoolSize(final int p0);
}
