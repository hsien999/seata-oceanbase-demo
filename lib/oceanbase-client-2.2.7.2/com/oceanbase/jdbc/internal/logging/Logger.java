// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.logging;

public interface Logger
{
    boolean isTraceEnabled();
    
    void trace(final String p0);
    
    void trace(final String p0, final Object p1);
    
    void trace(final String p0, final Object p1, final Object p2);
    
    void trace(final String p0, final Object... p1);
    
    void trace(final String p0, final Throwable p1);
    
    boolean isDebugEnabled();
    
    void debug(final String p0);
    
    void debug(final String p0, final Object p1);
    
    void debug(final String p0, final Object p1, final Object p2);
    
    void debug(final String p0, final Object... p1);
    
    void debug(final String p0, final Throwable p1);
    
    boolean isInfoEnabled();
    
    void info(final String p0);
    
    void info(final String p0, final Object p1);
    
    void info(final String p0, final Object p1, final Object p2);
    
    void info(final String p0, final Object... p1);
    
    void info(final String p0, final Throwable p1);
    
    boolean isWarnEnabled();
    
    void warn(final String p0);
    
    void warn(final String p0, final Object p1);
    
    void warn(final String p0, final Object... p1);
    
    void warn(final String p0, final Object p1, final Object p2);
    
    void warn(final String p0, final Throwable p1);
    
    boolean isErrorEnabled();
    
    void error(final String p0);
    
    void error(final String p0, final Object p1);
    
    void error(final String p0, final Object p1, final Object p2);
    
    void error(final String p0, final Object... p1);
    
    void error(final String p0, final Throwable p1);
}
