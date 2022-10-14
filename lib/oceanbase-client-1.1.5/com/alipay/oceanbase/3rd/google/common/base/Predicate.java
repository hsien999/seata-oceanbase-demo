// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.3rd.google.common.base;

import javax.annotation.Nullable;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
public interface Predicate<T>
{
    boolean apply(@Nullable final T p0);
    
    boolean equals(@Nullable final Object p0);
}
