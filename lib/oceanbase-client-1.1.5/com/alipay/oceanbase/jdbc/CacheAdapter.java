// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.Set;

public interface CacheAdapter<K, V>
{
    V get(final K p0);
    
    void put(final K p0, final V p1);
    
    void invalidate(final K p0);
    
    void invalidateAll(final Set<K> p0);
    
    void invalidateAll();
}
