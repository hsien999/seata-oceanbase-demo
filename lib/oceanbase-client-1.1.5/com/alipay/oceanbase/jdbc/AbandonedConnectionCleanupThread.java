// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.lang.ref.Reference;

public class AbandonedConnectionCleanupThread extends Thread
{
    private static boolean running;
    private static Thread threadRef;
    
    public AbandonedConnectionCleanupThread() {
        super("Abandoned connection cleanup thread");
    }
    
    @Override
    public void run() {
        AbandonedConnectionCleanupThread.threadRef = this;
        while (AbandonedConnectionCleanupThread.running) {
            try {
                final Reference<? extends ConnectionImpl> ref = NonRegisteringDriver.refQueue.remove(100L);
                if (ref == null) {
                    continue;
                }
                try {
                    ((NonRegisteringDriver.ConnectionPhantomReference)ref).cleanup();
                }
                finally {
                    NonRegisteringDriver.connectionPhantomRefs.remove(ref);
                }
            }
            catch (Exception ex) {}
        }
    }
    
    public static void shutdown() throws InterruptedException {
        AbandonedConnectionCleanupThread.running = false;
        if (AbandonedConnectionCleanupThread.threadRef != null) {
            AbandonedConnectionCleanupThread.threadRef.interrupt();
            AbandonedConnectionCleanupThread.threadRef.join();
            AbandonedConnectionCleanupThread.threadRef = null;
        }
    }
    
    static {
        AbandonedConnectionCleanupThread.running = true;
        AbandonedConnectionCleanupThread.threadRef = null;
    }
}
