// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util;

import java.util.Map;
import java.sql.CallableStatement;
import com.oceanbase.jdbc.internal.util.dao.CallableStatementCacheKey;
import java.util.LinkedHashMap;

public class CallableStatementCache extends LinkedHashMap<CallableStatementCacheKey, CallableStatement>
{
    private final int maxSize;
    
    private CallableStatementCache(final int size) {
        super(size, 0.75f, true);
        this.maxSize = size;
    }
    
    public static CallableStatementCache newInstance(final int size) {
        return new CallableStatementCache(size);
    }
    
    @Override
    protected boolean removeEldestEntry(final Map.Entry<CallableStatementCacheKey, CallableStatement> eldest) {
        return this.size() > this.maxSize;
    }
}
