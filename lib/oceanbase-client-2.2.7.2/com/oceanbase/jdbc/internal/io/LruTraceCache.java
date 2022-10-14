// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io;

import java.util.Iterator;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import com.oceanbase.jdbc.internal.util.Utils;
import java.util.Map;
import java.time.temporal.TemporalAccessor;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.LinkedHashMap;

public class LruTraceCache extends LinkedHashMap<String, TraceObject>
{
    private AtomicLong increment;
    
    public LruTraceCache() {
        super(16, 1.0f, false);
        this.increment = new AtomicLong();
    }
    
    public TraceObject put(final TraceObject value) {
        final String key = this.increment.incrementAndGet() + "- " + DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        return this.put(key, value);
    }
    
    @Override
    protected boolean removeEldestEntry(final Map.Entry<String, TraceObject> eldest) {
        return this.size() > 10;
    }
    
    public synchronized String printStack() {
        final StringBuilder sb = new StringBuilder();
        boolean finished = false;
        while (!finished) {
            try {
                final Map.Entry<String, TraceObject>[] array;
                final Map.Entry<String, TraceObject>[] arr = array = this.entrySet().toArray(new Map.Entry[0]);
                for (final Map.Entry<String, TraceObject> entry : array) {
                    final TraceObject traceObj = entry.getValue();
                    if (traceObj.getBuf() != null) {
                        final String key = entry.getKey();
                        String indicator = "";
                        switch (traceObj.getIndicatorFlag()) {
                            case 2: {
                                indicator = " (compressed protocol - packet not compressed)";
                                break;
                            }
                            case 1: {
                                indicator = " (compressed protocol - packet compressed)";
                                break;
                            }
                        }
                        sb.append("\nthread:").append(traceObj.getThreadId());
                        if (traceObj.isSend()) {
                            sb.append(" send at -exchange:");
                        }
                        else {
                            sb.append(" read at -exchange:");
                        }
                        sb.append(key).append(indicator).append(Utils.hexdump(traceObj.getBuf()));
                    }
                }
                finished = true;
            }
            catch (ConcurrentModificationException ex) {}
        }
        this.clear();
        return sb.toString();
    }
    
    public synchronized void clearMemory() {
        try {
            final Collection<TraceObject> traceObjects = ((LinkedHashMap<K, TraceObject>)this).values();
            for (final TraceObject traceObject : traceObjects) {
                traceObject.remove();
            }
        }
        catch (ConcurrentModificationException ex) {}
        this.clear();
    }
}
