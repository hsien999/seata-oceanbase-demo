// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.collect;

import java.util.Collection;
import java.util.Map;
import java.util.List;
import javax.annotation.Nullable;
import com.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
public interface ListMultimap<K, V> extends Multimap<K, V>
{
    List<V> get(@Nullable final K p0);
    
    List<V> removeAll(@Nullable final Object p0);
    
    List<V> replaceValues(final K p0, final Iterable<? extends V> p1);
    
    Map<K, Collection<V>> asMap();
    
    boolean equals(@Nullable final Object p0);
}
