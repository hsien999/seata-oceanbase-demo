// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util;

import java.util.Map;
import java.util.LinkedHashMap;

public class LRUCache extends LinkedHashMap<Object, Object>
{
    private static final long serialVersionUID = 1L;
    protected int maxElements;
    
    public LRUCache(final int maxSize) {
        super(maxSize, 0.75f, true);
        this.maxElements = maxSize;
    }
    
    @Override
    protected boolean removeEldestEntry(final Map.Entry<Object, Object> eldest) {
        return this.size() > this.maxElements;
    }
}
