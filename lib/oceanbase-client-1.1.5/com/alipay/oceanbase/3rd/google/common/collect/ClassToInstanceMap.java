// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.3rd.google.common.collect;

import javax.annotation.Nullable;
import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;
import java.util.Map;

@GwtCompatible
public interface ClassToInstanceMap<B> extends Map<Class<? extends B>, B>
{
     <T extends B> T getInstance(final Class<T> p0);
    
     <T extends B> T putInstance(final Class<T> p0, @Nullable final T p1);
}
