// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.log;

import com.alipay.oceanbase.jdbc.profiler.ProfilerEvent;
import java.util.logging.Logger;
import java.util.logging.Level;

public class Jdk14Logger implements Log
{
    private static final Level DEBUG;
    private static final Level ERROR;
    private static final Level FATAL;
    private static final Level INFO;
    private static final Level TRACE;
    private static final Level WARN;
    protected Logger jdkLogger;
    
    public Jdk14Logger(final String name) {
        this.jdkLogger = null;
        this.jdkLogger = Logger.getLogger(name);
    }
    
    @Override
    public boolean isDebugEnabled() {
        return this.jdkLogger.isLoggable(Level.FINE);
    }
    
    @Override
    public boolean isErrorEnabled() {
        return this.jdkLogger.isLoggable(Level.SEVERE);
    }
    
    @Override
    public boolean isFatalEnabled() {
        return this.jdkLogger.isLoggable(Level.SEVERE);
    }
    
    @Override
    public boolean isInfoEnabled() {
        return this.jdkLogger.isLoggable(Level.INFO);
    }
    
    @Override
    public boolean isTraceEnabled() {
        return this.jdkLogger.isLoggable(Level.FINEST);
    }
    
    @Override
    public boolean isWarnEnabled() {
        return this.jdkLogger.isLoggable(Level.WARNING);
    }
    
    @Override
    public void logDebug(final Object message) {
        this.logInternal(Jdk14Logger.DEBUG, message, null);
    }
    
    @Override
    public void logDebug(final Object message, final Throwable exception) {
        this.logInternal(Jdk14Logger.DEBUG, message, exception);
    }
    
    @Override
    public void logError(final Object message) {
        this.logInternal(Jdk14Logger.ERROR, message, null);
    }
    
    @Override
    public void logError(final Object message, final Throwable exception) {
        this.logInternal(Jdk14Logger.ERROR, message, exception);
    }
    
    @Override
    public void logFatal(final Object message) {
        this.logInternal(Jdk14Logger.FATAL, message, null);
    }
    
    @Override
    public void logFatal(final Object message, final Throwable exception) {
        this.logInternal(Jdk14Logger.FATAL, message, exception);
    }
    
    @Override
    public void logInfo(final Object message) {
        this.logInternal(Jdk14Logger.INFO, message, null);
    }
    
    @Override
    public void logInfo(final Object message, final Throwable exception) {
        this.logInternal(Jdk14Logger.INFO, message, exception);
    }
    
    @Override
    public void logTrace(final Object message) {
        this.logInternal(Jdk14Logger.TRACE, message, null);
    }
    
    @Override
    public void logTrace(final Object message, final Throwable exception) {
        this.logInternal(Jdk14Logger.TRACE, message, exception);
    }
    
    @Override
    public void logWarn(final Object message) {
        this.logInternal(Jdk14Logger.WARN, message, null);
    }
    
    @Override
    public void logWarn(final Object message, final Throwable exception) {
        this.logInternal(Jdk14Logger.WARN, message, exception);
    }
    
    private static final int findCallerStackDepth(final StackTraceElement[] stackTrace) {
        for (int numFrames = stackTrace.length, i = 0; i < numFrames; ++i) {
            final String callerClassName = stackTrace[i].getClassName();
            if (!callerClassName.startsWith("com.alipay.oceanbase.jdbc") || callerClassName.startsWith("com.alipay.oceanbase.jdbc.compliance")) {
                return i;
            }
        }
        return 0;
    }
    
    private void logInternal(final Level level, final Object msg, final Throwable exception) {
        if (this.jdkLogger.isLoggable(level)) {
            String messageAsString = null;
            String callerMethodName = "N/A";
            String callerClassName = "N/A";
            if (msg instanceof ProfilerEvent) {
                messageAsString = LogUtils.expandProfilerEventIfNecessary(msg).toString();
            }
            else {
                final Throwable locationException = new Throwable();
                final StackTraceElement[] locations = locationException.getStackTrace();
                final int frameIdx = findCallerStackDepth(locations);
                if (frameIdx != 0) {
                    callerClassName = locations[frameIdx].getClassName();
                    callerMethodName = locations[frameIdx].getMethodName();
                }
                messageAsString = String.valueOf(msg);
            }
            if (exception == null) {
                this.jdkLogger.logp(level, callerClassName, callerMethodName, messageAsString);
            }
            else {
                this.jdkLogger.logp(level, callerClassName, callerMethodName, messageAsString, exception);
            }
        }
    }
    
    static {
        DEBUG = Level.FINE;
        ERROR = Level.SEVERE;
        FATAL = Level.SEVERE;
        INFO = Level.INFO;
        TRACE = Level.FINEST;
        WARN = Level.WARNING;
    }
}
