// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.logging;

public class Slf4JLogger implements Logger
{
    private final org.slf4j.Logger logger;
    
    public Slf4JLogger(final org.slf4j.Logger logger) {
        this.logger = logger;
    }
    
    @Override
    public boolean isTraceEnabled() {
        return this.logger.isTraceEnabled();
    }
    
    @Override
    public void trace(final String msg) {
        this.logger.trace(msg);
    }
    
    @Override
    public void trace(final String format, final Object arg) {
        this.logger.trace(format, arg);
    }
    
    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        this.logger.trace(format, arg1, arg2);
    }
    
    @Override
    public void trace(final String format, final Object... arguments) {
        this.logger.trace(format, arguments);
    }
    
    @Override
    public void trace(final String msg, final Throwable throwable) {
        this.logger.trace(msg, throwable);
    }
    
    @Override
    public boolean isDebugEnabled() {
        return this.logger.isDebugEnabled();
    }
    
    @Override
    public void debug(final String msg) {
        this.logger.debug(msg);
    }
    
    @Override
    public void debug(final String format, final Object arg) {
        this.logger.debug(format, arg);
    }
    
    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        this.logger.debug(format, arg1, arg2);
    }
    
    @Override
    public void debug(final String format, final Object... arguments) {
        this.logger.debug(format, arguments);
    }
    
    @Override
    public void debug(final String msg, final Throwable throwable) {
        this.logger.debug(msg, throwable);
    }
    
    @Override
    public boolean isInfoEnabled() {
        return this.logger.isInfoEnabled();
    }
    
    @Override
    public void info(final String msg) {
        this.logger.info(msg);
    }
    
    @Override
    public void info(final String format, final Object arg) {
        this.logger.info(format, arg);
    }
    
    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        this.logger.info(format, arg1, arg2);
    }
    
    @Override
    public void info(final String format, final Object... arguments) {
        this.logger.info(format, arguments);
    }
    
    @Override
    public void info(final String msg, final Throwable throwable) {
        this.logger.info(msg, throwable);
    }
    
    @Override
    public boolean isWarnEnabled() {
        return this.logger.isWarnEnabled();
    }
    
    @Override
    public void warn(final String msg) {
        this.logger.warn(msg);
    }
    
    @Override
    public void warn(final String format, final Object arg) {
        this.logger.warn(format, arg);
    }
    
    @Override
    public void warn(final String format, final Object... arguments) {
        this.logger.warn(format, arguments);
    }
    
    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        this.logger.warn(format, arg1, arg2);
    }
    
    @Override
    public void warn(final String msg, final Throwable throwable) {
        this.logger.warn(msg, throwable);
    }
    
    @Override
    public boolean isErrorEnabled() {
        return this.logger.isErrorEnabled();
    }
    
    @Override
    public void error(final String msg) {
        this.logger.error(msg);
    }
    
    @Override
    public void error(final String format, final Object arg) {
        this.logger.error(format, arg);
    }
    
    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        this.logger.error(format, arg1, arg2);
    }
    
    @Override
    public void error(final String format, final Object... arguments) {
        this.logger.error(format, arguments);
    }
    
    @Override
    public void error(final String msg, final Throwable throwable) {
        this.logger.error(msg, throwable);
    }
}
