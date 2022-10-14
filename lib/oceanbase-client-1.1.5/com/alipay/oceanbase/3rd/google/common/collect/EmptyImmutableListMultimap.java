// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.3rd.google.common.collect;

import com.alipay.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible(serializable = true)
class EmptyImmutableListMultimap extends ImmutableListMultimap<Object, Object>
{
    static final EmptyImmutableListMultimap INSTANCE;
    private static final long serialVersionUID = 0L;
    
    private EmptyImmutableListMultimap() {
        super(ImmutableMap.of(), 0);
    }
    
    private Object readResolve() {
        return EmptyImmutableListMultimap.INSTANCE;
    }
    
    static {
        INSTANCE = new EmptyImmutableListMultimap();
    }
}
