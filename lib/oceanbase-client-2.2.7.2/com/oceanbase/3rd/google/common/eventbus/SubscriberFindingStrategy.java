// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.eventbus;

import com.oceanbase.3rd.google.common.collect.Multimap;

interface SubscriberFindingStrategy
{
    Multimap<Class<?>, EventSubscriber> findAllSubscribers(final Object p0);
}
