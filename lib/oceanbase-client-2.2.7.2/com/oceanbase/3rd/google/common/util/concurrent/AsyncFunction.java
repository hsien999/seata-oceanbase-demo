// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.util.concurrent;

public interface AsyncFunction<I, O>
{
    ListenableFuture<O> apply(final I p0) throws Exception;
}
