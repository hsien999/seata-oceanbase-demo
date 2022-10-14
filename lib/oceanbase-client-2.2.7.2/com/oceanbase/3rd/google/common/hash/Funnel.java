// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.hash;

import com.oceanbase.3rd.google.common.annotations.Beta;
import java.io.Serializable;

@Beta
public interface Funnel<T> extends Serializable
{
    void funnel(final T p0, final PrimitiveSink p1);
}
