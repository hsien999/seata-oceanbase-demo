// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.cache;

import com.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
interface LongAddable
{
    void increment();
    
    void add(final long p0);
    
    long sum();
}
