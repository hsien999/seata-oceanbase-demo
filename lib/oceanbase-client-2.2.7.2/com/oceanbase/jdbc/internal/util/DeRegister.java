// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util;

import com.oceanbase.jdbc.internal.util.scheduler.SchedulerServiceProviderHolder;
import java.sql.DriverAction;

public class DeRegister implements DriverAction
{
    @Override
    public void deregister() {
        SchedulerServiceProviderHolder.close();
    }
}
