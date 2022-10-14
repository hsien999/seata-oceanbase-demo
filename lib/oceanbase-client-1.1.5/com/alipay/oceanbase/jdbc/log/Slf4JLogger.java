// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.log;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Slf4JLogger implements Log
{
    private Logger log;
    
    public Slf4JLogger(final String name) {
        this.log = LoggerFactory.getLogger(name);
    }
    
    @Override
    public boolean isDebugEnabled() {
        return this.log.isDebugEnabled();
    }
    
    @Override
    public boolean isErrorEnabled() {
        return this.log.isErrorEnabled();
    }
    
    @Override
    public boolean isFatalEnabled() {
        return this.log.isErrorEnabled();
    }
    
    @Override
    public boolean isInfoEnabled() {
        return this.log.isInfoEnabled();
    }
    
    @Override
    public boolean isTraceEnabled() {
        return this.log.isTraceEnabled();
    }
    
    @Override
    public boolean isWarnEnabled() {
        return this.log.isWarnEnabled();
    }
    
    @Override
    public void logDebug(final Object msg) {
        this.log.debug(msg.toString());
    }
    
    @Override
    public void logDebug(final Object msg, final Throwable thrown) {
        this.log.debug(msg.toString(), thrown);
    }
    
    @Override
    public void logError(final Object msg) {
        this.log.error(msg.toString());
    }
    
    @Override
    public void logError(final Object msg, final Throwable thrown) {
        this.log.error(msg.toString(), thrown);
    }
    
    @Override
    public void logFatal(final Object msg) {
        this.log.error(msg.toString());
    }
    
    @Override
    public void logFatal(final Object msg, final Throwable thrown) {
        this.log.error(msg.toString(), thrown);
    }
    
    @Override
    public void logInfo(final Object msg) {
        this.log.info(msg.toString());
    }
    
    @Override
    public void logInfo(final Object msg, final Throwable thrown) {
        this.log.info(msg.toString(), thrown);
    }
    
    @Override
    public void logTrace(final Object msg) {
        this.log.trace(msg.toString());
    }
    
    @Override
    public void logTrace(final Object msg, final Throwable thrown) {
        this.log.trace(msg.toString(), thrown);
    }
    
    @Override
    public void logWarn(final Object msg) {
        this.log.warn(msg.toString());
    }
    
    @Override
    public void logWarn(final Object msg, final Throwable thrown) {
        this.log.warn(msg.toString(), thrown);
    }
}
