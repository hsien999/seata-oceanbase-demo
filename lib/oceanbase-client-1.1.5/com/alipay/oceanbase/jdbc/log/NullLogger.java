// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.log;

public class NullLogger implements Log
{
    public NullLogger(final String instanceName) {
    }
    
    @Override
    public boolean isDebugEnabled() {
        return false;
    }
    
    @Override
    public boolean isErrorEnabled() {
        return false;
    }
    
    @Override
    public boolean isFatalEnabled() {
        return false;
    }
    
    @Override
    public boolean isInfoEnabled() {
        return false;
    }
    
    @Override
    public boolean isTraceEnabled() {
        return false;
    }
    
    @Override
    public boolean isWarnEnabled() {
        return false;
    }
    
    @Override
    public void logDebug(final Object msg) {
    }
    
    @Override
    public void logDebug(final Object msg, final Throwable thrown) {
    }
    
    @Override
    public void logError(final Object msg) {
    }
    
    @Override
    public void logError(final Object msg, final Throwable thrown) {
    }
    
    @Override
    public void logFatal(final Object msg) {
    }
    
    @Override
    public void logFatal(final Object msg, final Throwable thrown) {
    }
    
    @Override
    public void logInfo(final Object msg) {
    }
    
    @Override
    public void logInfo(final Object msg, final Throwable thrown) {
    }
    
    @Override
    public void logTrace(final Object msg) {
    }
    
    @Override
    public void logTrace(final Object msg, final Throwable thrown) {
    }
    
    @Override
    public void logWarn(final Object msg) {
    }
    
    @Override
    public void logWarn(final Object msg, final Throwable thrown) {
    }
}
