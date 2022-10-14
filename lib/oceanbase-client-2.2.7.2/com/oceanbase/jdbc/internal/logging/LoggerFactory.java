// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.logging;

public class LoggerFactory
{
    private static final Logger NO_LOGGER;
    private static Boolean hasToLog;
    
    public static void init(final boolean mustLog) {
        if ((LoggerFactory.hasToLog == null || LoggerFactory.hasToLog != mustLog) && mustLog) {
            synchronized (LoggerFactory.class) {
                if (LoggerFactory.hasToLog != null) {
                    if (LoggerFactory.hasToLog == mustLog) {
                        return;
                    }
                }
                try {
                    Class.forName("org.slf4j.LoggerFactory");
                    LoggerFactory.hasToLog = Boolean.TRUE;
                }
                catch (ClassNotFoundException classNotFound) {
                    System.out.println("Logging cannot be activated, missing slf4j dependency");
                    LoggerFactory.hasToLog = Boolean.FALSE;
                }
            }
        }
    }
    
    public static Logger getLogger(final Class<?> clazz) {
        if (LoggerFactory.hasToLog != null && LoggerFactory.hasToLog) {
            return new Slf4JLogger(org.slf4j.LoggerFactory.getLogger((Class)clazz));
        }
        return LoggerFactory.NO_LOGGER;
    }
    
    static {
        NO_LOGGER = new NoLogger();
        LoggerFactory.hasToLog = null;
    }
}
