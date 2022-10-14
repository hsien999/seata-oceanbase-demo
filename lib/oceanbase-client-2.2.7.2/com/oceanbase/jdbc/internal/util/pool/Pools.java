// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.pool;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.Iterator;
import java.util.concurrent.ThreadFactory;
import com.oceanbase.jdbc.internal.util.scheduler.OceanBaseThreadFactory;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import com.oceanbase.jdbc.UrlParser;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Pools
{
    private static final AtomicInteger poolIndex;
    private static final Map<UrlParser, Pool> poolMap;
    private static ScheduledThreadPoolExecutor poolExecutor;
    
    public static Pool retrievePool(final UrlParser urlParser) {
        if (!Pools.poolMap.containsKey(urlParser)) {
            synchronized (Pools.poolMap) {
                if (!Pools.poolMap.containsKey(urlParser)) {
                    if (Pools.poolExecutor == null) {
                        Pools.poolExecutor = new ScheduledThreadPoolExecutor(1, new OceanBaseThreadFactory("MariaDbPool-maxTimeoutIdle-checker"));
                    }
                    final Pool pool = new Pool(urlParser, Pools.poolIndex.incrementAndGet(), Pools.poolExecutor);
                    Pools.poolMap.put(urlParser, pool);
                    return pool;
                }
            }
        }
        return Pools.poolMap.get(urlParser);
    }
    
    public static void remove(final Pool pool) {
        if (Pools.poolMap.containsKey(pool.getUrlParser())) {
            synchronized (Pools.poolMap) {
                if (Pools.poolMap.containsKey(pool.getUrlParser())) {
                    Pools.poolMap.remove(pool.getUrlParser());
                    if (Pools.poolMap.isEmpty()) {
                        shutdownExecutor();
                    }
                }
            }
        }
    }
    
    public static void close() {
        synchronized (Pools.poolMap) {
            for (final Pool pool : Pools.poolMap.values()) {
                try {
                    pool.close();
                }
                catch (InterruptedException ex) {}
            }
            shutdownExecutor();
            Pools.poolMap.clear();
        }
    }
    
    public static void close(final String poolName) {
        if (poolName == null) {
            return;
        }
        synchronized (Pools.poolMap) {
            for (final Pool pool : Pools.poolMap.values()) {
                if (poolName.equals(pool.getUrlParser().getOptions().poolName)) {
                    try {
                        pool.close();
                    }
                    catch (InterruptedException ex) {}
                }
            }
        }
    }
    
    private static void shutdownExecutor() {
        Pools.poolExecutor.shutdown();
        try {
            Pools.poolExecutor.awaitTermination(10L, TimeUnit.SECONDS);
        }
        catch (InterruptedException ex) {}
        Pools.poolExecutor = null;
    }
    
    static {
        poolIndex = new AtomicInteger();
        poolMap = new ConcurrentHashMap<UrlParser, Pool>();
        Pools.poolExecutor = null;
    }
}
