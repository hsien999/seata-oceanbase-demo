// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.scheduler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public class SchedulerServiceProviderHolder
{
    public static final SchedulerProvider DEFAULT_PROVIDER;
    private static AtomicReference<SchedulerProvider> currentProvider;
    
    public static SchedulerProvider getSchedulerProvider() {
        return SchedulerServiceProviderHolder.currentProvider.get();
    }
    
    public static void setSchedulerProvider(SchedulerProvider newProvider) {
        if (newProvider == null) {
            newProvider = SchedulerServiceProviderHolder.DEFAULT_PROVIDER;
        }
        SchedulerServiceProviderHolder.currentProvider.getAndSet(newProvider).close();
    }
    
    public static void close() {
        SchedulerServiceProviderHolder.currentProvider.get().close();
    }
    
    public static DynamicSizedSchedulerInterface getScheduler(final int initialThreadCount, final String poolName, final int maximumPoolSize) {
        return getSchedulerProvider().getScheduler(initialThreadCount, poolName, maximumPoolSize);
    }
    
    public static ScheduledExecutorService getFixedSizeScheduler(final int initialThreadCount, final String poolName) {
        return getSchedulerProvider().getFixedSizeScheduler(initialThreadCount, poolName);
    }
    
    public static ScheduledExecutorService getTimeoutScheduler() {
        return getSchedulerProvider().getTimeoutScheduler();
    }
    
    public static ThreadPoolExecutor getBulkScheduler() {
        return getSchedulerProvider().getBulkScheduler();
    }
    
    static {
        DEFAULT_PROVIDER = new SchedulerProvider() {
            private DynamicSizedSchedulerInterface dynamicSizedScheduler;
            private FixedSizedSchedulerImpl fixedSizedScheduler;
            private ScheduledThreadPoolExecutor timeoutScheduler;
            private ThreadPoolExecutor threadPoolExecutor;
            
            @Override
            public DynamicSizedSchedulerInterface getScheduler(final int minimumThreads, final String poolName, final int maximumPoolSize) {
                if (this.dynamicSizedScheduler == null) {
                    synchronized (this) {
                        if (this.dynamicSizedScheduler == null) {
                            this.dynamicSizedScheduler = new DynamicSizedSchedulerImpl(minimumThreads, poolName, maximumPoolSize);
                        }
                    }
                }
                return this.dynamicSizedScheduler;
            }
            
            @Override
            public ScheduledThreadPoolExecutor getFixedSizeScheduler(final int minimumThreads, final String poolName) {
                if (this.fixedSizedScheduler == null) {
                    synchronized (this) {
                        if (this.fixedSizedScheduler == null) {
                            this.fixedSizedScheduler = new FixedSizedSchedulerImpl(minimumThreads, poolName);
                        }
                    }
                }
                return this.fixedSizedScheduler;
            }
            
            @Override
            public ScheduledThreadPoolExecutor getTimeoutScheduler() {
                if (this.timeoutScheduler == null) {
                    synchronized (this) {
                        if (this.timeoutScheduler == null) {
                            (this.timeoutScheduler = new ScheduledThreadPoolExecutor(1, new OceanBaseThreadFactory("MariaDb-timeout"))).setRemoveOnCancelPolicy(true);
                        }
                    }
                }
                return this.timeoutScheduler;
            }
            
            @Override
            public ThreadPoolExecutor getBulkScheduler() {
                if (this.threadPoolExecutor == null) {
                    synchronized (this) {
                        if (this.threadPoolExecutor == null) {
                            this.threadPoolExecutor = new ThreadPoolExecutor(5, 100, 1L, TimeUnit.MINUTES, new SynchronousQueue<Runnable>(), new OceanBaseThreadFactory("MariaDb-bulk"));
                        }
                    }
                }
                return this.threadPoolExecutor;
            }
            
            @Override
            public void close() {
                synchronized (this) {
                    if (this.dynamicSizedScheduler != null) {
                        this.dynamicSizedScheduler.shutdownNow();
                    }
                    if (this.fixedSizedScheduler != null) {
                        this.fixedSizedScheduler.shutdownNow();
                    }
                    if (this.timeoutScheduler != null) {
                        this.timeoutScheduler.shutdownNow();
                    }
                    if (this.threadPoolExecutor != null) {
                        this.threadPoolExecutor.shutdownNow();
                    }
                    this.dynamicSizedScheduler = null;
                    this.fixedSizedScheduler = null;
                    this.timeoutScheduler = null;
                    this.threadPoolExecutor = null;
                }
            }
        };
        SchedulerServiceProviderHolder.currentProvider = new AtomicReference<SchedulerProvider>(SchedulerServiceProviderHolder.DEFAULT_PROVIDER);
    }
    
    public interface SchedulerProvider
    {
        DynamicSizedSchedulerInterface getScheduler(final int p0, final String p1, final int p2);
        
        ScheduledExecutorService getFixedSizeScheduler(final int p0, final String p1);
        
        ScheduledThreadPoolExecutor getTimeoutScheduler();
        
        ThreadPoolExecutor getBulkScheduler();
        
        void close();
    }
}
