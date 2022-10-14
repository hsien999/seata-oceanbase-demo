// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.collect;

import java.util.Map;
import com.oceanbase.3rd.google.common.base.Predicate;
import com.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
interface FilteredMultimap<K, V> extends Multimap<K, V>
{
    Multimap<K, V> unfiltered();
    
    Predicate<? super Map.Entry<K, V>> entryPredicate();
}
