// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.cache;

import java.util.concurrent.ConcurrentMap;
import com.oceanbase.3rd.google.common.collect.ImmutableMap;
import java.util.concurrent.ExecutionException;
import com.oceanbase.3rd.google.common.annotations.GwtCompatible;
import com.oceanbase.3rd.google.common.annotations.Beta;
import com.oceanbase.3rd.google.common.base.Function;

@Beta
@GwtCompatible
public interface LoadingCache<K, V> extends Cache<K, V>, Function<K, V>
{
    V get(final K p0) throws ExecutionException;
    
    V getUnchecked(final K p0);
    
    ImmutableMap<K, V> getAll(final Iterable<? extends K> p0) throws ExecutionException;
    
    @Deprecated
    V apply(final K p0);
    
    void refresh(final K p0);
    
    ConcurrentMap<K, V> asMap();
}
