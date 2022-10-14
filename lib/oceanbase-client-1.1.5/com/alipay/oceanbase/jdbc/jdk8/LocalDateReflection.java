// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdk8;

import java.sql.Date;
import java.lang.reflect.Method;

public class LocalDateReflection
{
    public static final String LocalDate_CLASS_NAME = "java.time.LocalDate";
    public static Method of_method;
    public static Method now_method;
    public static Method getYear_method;
    public static Method getMonth_method;
    public static Method getDayOfMonth_method;
    public static Class localDate;
    
    public static Object of(final int year, final int month, final int dayOfMonth) throws Exception {
        return LocalDateReflection.of_method.invoke(LocalDateReflection.localDate, year, month, dayOfMonth);
    }
    
    public static Object now() throws Exception {
        return LocalDateReflection.now_method.invoke(LocalDateReflection.localDate, new Object[0]);
    }
    
    public static int getYear(final Object object) throws Exception {
        return Integer.parseInt(LocalDateReflection.getYear_method.invoke(object, new Object[0]).toString());
    }
    
    public static int getMonth(final Object object) throws Exception {
        return Integer.parseInt(LocalDateReflection.getMonth_method.invoke(object, new Object[0]).toString());
    }
    
    public static int getDayOfMonth(final Object object) throws Exception {
        return Integer.parseInt(LocalDateReflection.getDayOfMonth_method.invoke(object, new Object[0]).toString());
    }
    
    public static Date getDate(final Object object) throws Exception {
        final StringBuilder stringBuilder = new StringBuilder();
        return Date.valueOf(stringBuilder.append(getYear(object)).append("-").append(getMonth(object)).append("-").append(getDayOfMonth(object)).toString());
    }
    
    static {
        try {
            LocalDateReflection.localDate = Class.forName("java.time.LocalDate");
            LocalDateReflection.of_method = LocalDateReflection.localDate.getMethod("of", Integer.TYPE, Integer.TYPE, Integer.TYPE);
            LocalDateReflection.now_method = LocalDateReflection.localDate.getMethod("now", (Class[])new Class[0]);
            LocalDateReflection.getYear_method = LocalDateReflection.localDate.getMethod("getYear", (Class[])new Class[0]);
            LocalDateReflection.getMonth_method = LocalDateReflection.localDate.getMethod("getMonthValue", (Class[])new Class[0]);
            LocalDateReflection.getDayOfMonth_method = LocalDateReflection.localDate.getMethod("getDayOfMonth", (Class[])new Class[0]);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
