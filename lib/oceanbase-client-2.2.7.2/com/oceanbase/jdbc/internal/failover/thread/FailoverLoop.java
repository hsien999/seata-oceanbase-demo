// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.failover.thread;

import com.oceanbase.jdbc.internal.failover.tools.SearchFilter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import com.oceanbase.jdbc.internal.failover.Listener;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FailoverLoop extends TerminableRunnable
{
    private static final ConcurrentLinkedQueue<Listener> queue;
    
    public FailoverLoop(final ScheduledExecutorService scheduler) {
        super(scheduler, 1L, 1L, TimeUnit.SECONDS);
    }
    
    public static void addListener(final Listener listener) {
        FailoverLoop.queue.add(listener);
    }
    
    public static void removeListener(final Listener listener) {
        FailoverLoop.queue.remove(listener);
    }
    
    @Override
    protected void doRun() {
        Listener listener;
        while (!this.isUnschedule() && (listener = FailoverLoop.queue.poll()) != null) {
            if (!listener.isExplicitClosed() && listener.hasHostFail() && listener.canRetryFailLoop()) {
                try {
                    final SearchFilter filter = listener.getFilterForFailedHost();
                    filter.setFailoverLoop(true);
                    listener.reconnectFailedConnection(filter);
                    if (!listener.hasHostFail() || listener.isExplicitClosed()) {
                        continue;
                    }
                    FailoverLoop.queue.add(listener);
                }
                catch (Exception e) {
                    FailoverLoop.queue.add(listener);
                }
            }
        }
    }
    
    static {
        queue = new ConcurrentLinkedQueue<Listener>();
    }
}
