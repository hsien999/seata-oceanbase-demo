// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.collect;

import com.oceanbase.3rd.google.common.annotations.GwtCompatible;

@GwtCompatible
interface Constraint<E>
{
    E checkElement(final E p0);
    
    String toString();
}
