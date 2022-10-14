// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdk8;

import java.sql.Timestamp;
import java.sql.Date;
import java.lang.reflect.Method;

public class LocalDateTimeReflection
{
    public static final String CLASS_NAME = "java.time.LocalDateTime";
    public static Method of_method;
    public static Method nano_of_method;
    public static Method now_method;
    public static Method getYear_method;
    public static Method getMonth_method;
    public static Method getDayOfMonth_method;
    public static Method getHour_method;
    public static Method getMinute_method;
    public static Method getSecond_method;
    public static Method getNano_method;
    public static Class localDateTime;
    
    public static Object of(final int year, final int month, final int dayOfMonth, final int hour, final int minute, final int second, final int nanoOfSecond) throws Exception {
        return LocalDateTimeReflection.nano_of_method.invoke(LocalDateTimeReflection.localDateTime, year, month, dayOfMonth, hour, minute, second, nanoOfSecond);
    }
    
    public static Object of(final int year, final int month, final int dayOfMonth, final int hour, final int minute, final int second) throws Exception {
        return LocalDateTimeReflection.of_method.invoke(LocalDateTimeReflection.localDateTime, year, month, dayOfMonth, hour, minute, second);
    }
    
    public static Object now() throws Exception {
        return LocalDateTimeReflection.now_method.invoke(LocalDateTimeReflection.localDateTime, new Object[0]);
    }
    
    public static int getYear(final Object object) throws Exception {
        return Integer.parseInt(LocalDateTimeReflection.getYear_method.invoke(object, new Object[0]).toString());
    }
    
    public static int getMonth(final Object object) throws Exception {
        return Integer.parseInt(LocalDateTimeReflection.getMonth_method.invoke(object, new Object[0]).toString());
    }
    
    public static int getDayOfMonth(final Object object) throws Exception {
        return Integer.parseInt(LocalDateTimeReflection.getDayOfMonth_method.invoke(object, new Object[0]).toString());
    }
    
    public static int getHour(final Object object) throws Exception {
        return Integer.parseInt(LocalDateTimeReflection.getHour_method.invoke(object, new Object[0]).toString());
    }
    
    public static int getMinute(final Object object) throws Exception {
        return Integer.parseInt(LocalDateTimeReflection.getMinute_method.invoke(object, new Object[0]).toString());
    }
    
    public static int getSecond(final Object object) throws Exception {
        return Integer.parseInt(LocalDateTimeReflection.getSecond_method.invoke(object, new Object[0]).toString());
    }
    
    public static int getNano(final Object object) throws Exception {
        return Integer.parseInt(LocalDateTimeReflection.getNano_method.invoke(object, new Object[0]).toString());
    }
    
    public static Date getDate(final Object object) throws Exception {
        final StringBuilder stringBuilder = new StringBuilder();
        return Date.valueOf(stringBuilder.append(getYear(object)).append("-").append(getMonth(object)).append("-").append(getDayOfMonth(object)).toString());
    }
    
    public static Timestamp getTimeStamp(final Object object) throws Exception {
        final StringBuilder stringBuilder = new StringBuilder();
        return Timestamp.valueOf(stringBuilder.append(getYear(object)).append("-").append(getMonth(object)).append("-").append(getDayOfMonth(object)).append(" ").append(getHour(object)).append(":").append(getMinute(object)).append(":").append(getSecond(object)).append(".").append(getNano(object)).toString());
    }
    
    static {
        try {
            LocalDateTimeReflection.localDateTime = Class.forName("java.time.LocalDateTime");
            LocalDateTimeReflection.of_method = LocalDateTimeReflection.localDateTime.getMethod("of", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            LocalDateTimeReflection.nano_of_method = LocalDateTimeReflection.localDateTime.getMethod("of", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            LocalDateTimeReflection.now_method = LocalDateTimeReflection.localDateTime.getMethod("now", (Class[])new Class[0]);
            LocalDateTimeReflection.getYear_method = LocalDateTimeReflection.localDateTime.getMethod("getYear", (Class[])new Class[0]);
            LocalDateTimeReflection.getMonth_method = LocalDateTimeReflection.localDateTime.getMethod("getMonthValue", (Class[])new Class[0]);
            LocalDateTimeReflection.getDayOfMonth_method = LocalDateTimeReflection.localDateTime.getMethod("getDayOfMonth", (Class[])new Class[0]);
            LocalDateTimeReflection.getHour_method = LocalDateTimeReflection.localDateTime.getMethod("getHour", (Class[])new Class[0]);
            LocalDateTimeReflection.getMinute_method = LocalDateTimeReflection.localDateTime.getMethod("getMinute", (Class[])new Class[0]);
            LocalDateTimeReflection.getSecond_method = LocalDateTimeReflection.localDateTime.getMethod("getSecond", (Class[])new Class[0]);
            LocalDateTimeReflection.getNano_method = LocalDateTimeReflection.localDateTime.getMethod("getNano", (Class[])new Class[0]);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
