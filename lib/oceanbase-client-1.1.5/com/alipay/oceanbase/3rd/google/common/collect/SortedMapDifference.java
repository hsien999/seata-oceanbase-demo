// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.3rd.google.common.collect;

import java.util.SortedMap;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
public interface SortedMapDifference<K, V> extends MapDifference<K, V>
{
    SortedMap<K, V> entriesOnlyOnLeft();
    
    SortedMap<K, V> entriesOnlyOnRight();
    
    SortedMap<K, V> entriesInCommon();
    
    SortedMap<K, ValueDifference<V>> entriesDiffering();
}
