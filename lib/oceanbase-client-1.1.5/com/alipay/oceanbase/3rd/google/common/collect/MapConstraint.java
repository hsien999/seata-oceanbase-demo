// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.3rd.google.common.collect;

import javax.annotation.Nullable;
import com.alipay.oceanbase.3rd.google.common.annotations.Beta;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
@Beta
public interface MapConstraint<K, V>
{
    void checkKeyValue(@Nullable final K p0, @Nullable final V p1);
    
    String toString();
}
