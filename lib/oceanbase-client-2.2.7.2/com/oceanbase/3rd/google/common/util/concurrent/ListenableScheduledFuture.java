// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.util.concurrent;

import com.oceanbase.3rd.google.common.annotations.Beta;
import java.util.concurrent.ScheduledFuture;

@Beta
public interface ListenableScheduledFuture<V> extends ScheduledFuture<V>, ListenableFuture<V>
{
}
