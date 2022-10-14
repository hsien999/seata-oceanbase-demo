// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.logging;

public class NoLogger implements Logger
{
    @Override
    public boolean isTraceEnabled() {
        return false;
    }
    
    @Override
    public void trace(final String msg) {
    }
    
    @Override
    public void trace(final String format, final Object arg) {
    }
    
    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
    }
    
    @Override
    public void trace(final String format, final Object... arguments) {
    }
    
    @Override
    public void trace(final String msg, final Throwable throwable) {
    }
    
    @Override
    public boolean isDebugEnabled() {
        return false;
    }
    
    @Override
    public void debug(final String msg) {
    }
    
    @Override
    public void debug(final String format, final Object arg) {
    }
    
    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
    }
    
    @Override
    public void debug(final String format, final Object... arguments) {
    }
    
    @Override
    public void debug(final String msg, final Throwable throwable) {
    }
    
    @Override
    public boolean isInfoEnabled() {
        return false;
    }
    
    @Override
    public void info(final String msg) {
    }
    
    @Override
    public void info(final String format, final Object arg) {
    }
    
    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
    }
    
    @Override
    public void info(final String format, final Object... arguments) {
    }
    
    @Override
    public void info(final String msg, final Throwable throwable) {
    }
    
    @Override
    public boolean isWarnEnabled() {
        return false;
    }
    
    @Override
    public void warn(final String msg) {
    }
    
    @Override
    public void warn(final String format, final Object arg) {
    }
    
    @Override
    public void warn(final String format, final Object... arguments) {
    }
    
    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
    }
    
    @Override
    public void warn(final String msg, final Throwable throwable) {
    }
    
    @Override
    public boolean isErrorEnabled() {
        return false;
    }
    
    @Override
    public void error(final String msg) {
    }
    
    @Override
    public void error(final String format, final Object arg) {
    }
    
    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
    }
    
    @Override
    public void error(final String format, final Object... arguments) {
    }
    
    @Override
    public void error(final String msg, final Throwable throwable) {
    }
}
