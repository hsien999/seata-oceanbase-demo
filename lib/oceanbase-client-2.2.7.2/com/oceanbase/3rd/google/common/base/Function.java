// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.base;

import javax.annotation.Nullable;
import com.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
public interface Function<F, T>
{
    @Nullable
    T apply(@Nullable final F p0);
    
    boolean equals(@Nullable final Object p0);
}
