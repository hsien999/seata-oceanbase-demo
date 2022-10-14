// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.cache;

import com.oceanbase.3rd.google.common.annotations.GwtCompatible;
import com.oceanbase.3rd.google.common.annotations.Beta;

@Beta
@GwtCompatible
public interface RemovalListener<K, V>
{
    void onRemoval(final RemovalNotification<K, V> p0);
}
