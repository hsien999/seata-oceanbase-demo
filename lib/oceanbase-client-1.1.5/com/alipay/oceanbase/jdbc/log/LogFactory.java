// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.log;

import java.sql.SQLException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import com.alipay.oceanbase.jdbc.SQLError;
import com.alipay.oceanbase.jdbc.ExceptionInterceptor;

public class LogFactory
{
    private static final String LOGGER_CLASS_NAME;
    private static final String LOGGER_INSTANCE_NAME = "OceanBase";
    private static Log JDBC_LOGGER;
    
    public static Log getLogger() {
        if (null == LogFactory.JDBC_LOGGER) {
            try {
                LogFactory.JDBC_LOGGER = getLogger(LogFactory.LOGGER_CLASS_NAME, "OceanBase", null);
            }
            catch (Throwable t) {
                throw new RuntimeException("Error creating logger for logger '" + LogFactory.LOGGER_CLASS_NAME + "'.  Cause: " + t, t);
            }
        }
        return LogFactory.JDBC_LOGGER;
    }
    
    public static Log getLogger(final String className, final String instanceName, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        if (className == null) {
            throw SQLError.createSQLException("Logger class can not be NULL", "S1009", exceptionInterceptor);
        }
        if (instanceName == null) {
            throw SQLError.createSQLException("Logger instance name can not be NULL", "S1009", exceptionInterceptor);
        }
        try {
            Class<?> loggerClass = null;
            try {
                loggerClass = Class.forName(className);
            }
            catch (ClassNotFoundException nfe) {
                loggerClass = Class.forName(Log.class.getPackage().getName() + "." + className);
            }
            final Constructor<?> constructor = loggerClass.getConstructor(String.class);
            return (Log)constructor.newInstance(instanceName);
        }
        catch (ClassNotFoundException cnfe) {
            final SQLException sqlEx = SQLError.createSQLException("Unable to load class for logger '" + className + "'", "S1009", exceptionInterceptor);
            sqlEx.initCause(cnfe);
            throw sqlEx;
        }
        catch (NoSuchMethodException nsme) {
            final SQLException sqlEx = SQLError.createSQLException("Logger class does not have a single-arg constructor that takes an instance name", "S1009", exceptionInterceptor);
            sqlEx.initCause(nsme);
            throw sqlEx;
        }
        catch (InstantiationException inse) {
            final SQLException sqlEx = SQLError.createSQLException("Unable to instantiate logger class '" + className + "', exception in constructor?", "S1009", exceptionInterceptor);
            sqlEx.initCause(inse);
            throw sqlEx;
        }
        catch (InvocationTargetException ite) {
            final SQLException sqlEx = SQLError.createSQLException("Unable to instantiate logger class '" + className + "', exception in constructor?", "S1009", exceptionInterceptor);
            sqlEx.initCause(ite);
            throw sqlEx;
        }
        catch (IllegalAccessException iae) {
            final SQLException sqlEx = SQLError.createSQLException("Unable to instantiate logger class '" + className + "', constructor not public", "S1009", exceptionInterceptor);
            sqlEx.initCause(iae);
            throw sqlEx;
        }
        catch (ClassCastException cce) {
            final SQLException sqlEx = SQLError.createSQLException("Logger class '" + className + "' does not implement the '" + Log.class.getName() + "' interface", "S1009", exceptionInterceptor);
            sqlEx.initCause(cce);
            throw sqlEx;
        }
    }
    
    static {
        LOGGER_CLASS_NAME = StandardLogger.class.getName();
        LogFactory.JDBC_LOGGER = null;
    }
}
