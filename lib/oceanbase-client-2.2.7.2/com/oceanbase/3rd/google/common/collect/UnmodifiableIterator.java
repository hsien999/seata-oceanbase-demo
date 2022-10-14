// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.collect;

import com.oceanbase.3rd.google.common.annotations.GwtCompatible;
import java.util.Iterator;

@GwtCompatible
public abstract class UnmodifiableIterator<E> implements Iterator<E>
{
    protected UnmodifiableIterator() {
    }
    
    @Deprecated
    @Override
    public final void remove() {
        throw new UnsupportedOperationException();
    }
}
