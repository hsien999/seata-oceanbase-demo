// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.3rd.google.common.cache;

import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;

@Beta
@GwtCompatible
public interface RemovalListener<K, V>
{
    void onRemoval(final RemovalNotification<K, V> p0);
}
