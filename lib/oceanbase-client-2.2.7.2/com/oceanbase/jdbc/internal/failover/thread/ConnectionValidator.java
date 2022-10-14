// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.failover.thread;

import java.util.Iterator;
import java.lang.reflect.Method;
import com.oceanbase.jdbc.internal.failover.tools.SearchFilter;
import java.util.concurrent.TimeUnit;
import com.oceanbase.jdbc.internal.util.scheduler.SchedulerServiceProviderHolder;
import java.util.concurrent.atomic.AtomicLong;
import com.oceanbase.jdbc.internal.failover.Listener;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;

public class ConnectionValidator
{
    private static final int MINIMUM_CHECK_DELAY_MILLIS = 100;
    private final ScheduledExecutorService fixedSizedScheduler;
    private final ConcurrentLinkedQueue<Listener> queue;
    private final AtomicLong currentScheduledFrequency;
    private final ListenerChecker checker;
    
    public ConnectionValidator() {
        this.fixedSizedScheduler = SchedulerServiceProviderHolder.getFixedSizeScheduler(1, "validator");
        this.queue = new ConcurrentLinkedQueue<Listener>();
        this.currentScheduledFrequency = new AtomicLong(-1L);
        this.checker = new ListenerChecker();
    }
    
    public void addListener(final Listener listener, final long listenerCheckMillis) {
        this.queue.add(listener);
        final long newFrequency = Math.min(100L, listenerCheckMillis);
        if (this.currentScheduledFrequency.get() == -1L) {
            if (this.currentScheduledFrequency.compareAndSet(-1L, newFrequency)) {
                this.fixedSizedScheduler.schedule(this.checker, listenerCheckMillis, TimeUnit.MILLISECONDS);
            }
        }
        else {
            final long frequency = this.currentScheduledFrequency.get();
            if (frequency > newFrequency) {
                this.currentScheduledFrequency.compareAndSet(frequency, newFrequency);
            }
        }
    }
    
    public void removeListener(final Listener listener) {
        this.queue.remove(listener);
        if (this.queue.isEmpty()) {
            synchronized (this.queue) {
                if (this.currentScheduledFrequency.get() > 0L && this.queue.isEmpty()) {
                    this.currentScheduledFrequency.set(-1L);
                }
            }
        }
    }
    
    private class ListenerChecker implements Runnable
    {
        @Override
        public void run() {
            try {
                this.doRun();
            }
            finally {
                final long delay = ConnectionValidator.this.currentScheduledFrequency.get();
                if (delay > 0L) {
                    ConnectionValidator.this.fixedSizedScheduler.schedule(this, delay, TimeUnit.MILLISECONDS);
                }
            }
        }
        
        private void doRun() {
            final Iterator<Listener> tmpQueue = ConnectionValidator.this.queue.iterator();
            long now = -1L;
            while (tmpQueue.hasNext()) {
                final Listener listener = tmpQueue.next();
                if (!listener.isExplicitClosed()) {
                    final long durationNanos = ((now == -1L) ? (now = System.nanoTime()) : now) - listener.getLastQueryNanos();
                    final long durationSeconds = TimeUnit.NANOSECONDS.toSeconds(durationNanos);
                    if (durationSeconds < listener.getUrlParser().getOptions().validConnectionTimeout || listener.isMasterHostFail()) {
                        continue;
                    }
                    boolean masterFail = false;
                    if (listener.isMasterConnected()) {
                        listener.checkMasterStatus(null);
                    }
                    else {
                        masterFail = true;
                    }
                    if (!masterFail || !listener.setMasterHostFail()) {
                        continue;
                    }
                    try {
                        listener.primaryFail(null, null, false, false);
                    }
                    catch (Throwable t) {}
                }
            }
        }
    }
}
