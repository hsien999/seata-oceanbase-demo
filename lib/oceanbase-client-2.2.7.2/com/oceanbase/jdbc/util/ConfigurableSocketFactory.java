// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.util;

import javax.net.SocketFactory;

public abstract class ConfigurableSocketFactory extends SocketFactory
{
    public abstract void setConfiguration(final Options p0, final String p1);
}
