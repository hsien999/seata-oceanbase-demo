// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.failover.thread;

import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class TerminableRunnable implements Runnable
{
    private final AtomicReference<State> runState;
    private final AtomicBoolean unschedule;
    private volatile ScheduledFuture<?> scheduledFuture;
    
    public TerminableRunnable(final ScheduledExecutorService scheduler, final long initialDelay, final long delay, final TimeUnit unit) {
        this.runState = new AtomicReference<State>(State.IDLE);
        this.unschedule = new AtomicBoolean();
        this.scheduledFuture = null;
        this.scheduledFuture = scheduler.scheduleWithFixedDelay(this, initialDelay, delay, unit);
    }
    
    protected abstract void doRun();
    
    @Override
    public final void run() {
        if (!this.runState.compareAndSet(State.IDLE, State.ACTIVE)) {
            return;
        }
        try {
            this.doRun();
        }
        finally {
            this.runState.compareAndSet(State.ACTIVE, State.IDLE);
        }
    }
    
    public void blockTillTerminated() {
        while (!this.runState.compareAndSet(State.IDLE, State.REMOVED)) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10L));
            if (Thread.currentThread().isInterrupted()) {
                this.runState.set(State.REMOVED);
            }
        }
    }
    
    public boolean isUnschedule() {
        return this.unschedule.get();
    }
    
    public void unscheduleTask() {
        if (this.unschedule.compareAndSet(false, true)) {
            this.scheduledFuture.cancel(false);
            this.scheduledFuture = null;
        }
    }
    
    private enum State
    {
        REMOVED, 
        IDLE, 
        ACTIVE;
    }
}
