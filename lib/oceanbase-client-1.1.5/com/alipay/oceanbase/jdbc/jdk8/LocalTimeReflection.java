// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdk8;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.Timestamp;
import java.lang.reflect.Method;

public class LocalTimeReflection
{
    public static final String LocalTime_CLASS_NAME = "java.time.LocalTime";
    public static Method of_method;
    public static Method of_method2;
    public static Method now_method;
    public static Method getHour_method;
    public static Method getMinute_method;
    public static Method getSecond_method;
    public static Method getNano_method;
    public static Class localTime;
    
    public static Object of(final int hour, final int minute, final int second, final int nanoOfSecond) throws Exception {
        return LocalTimeReflection.of_method.invoke(LocalTimeReflection.localTime, hour, minute, second, nanoOfSecond);
    }
    
    public static Object of(final int hour, final int minute, final int second) throws Exception {
        return LocalTimeReflection.of_method.invoke(LocalTimeReflection.localTime, hour, minute, second);
    }
    
    public static Object now() throws Exception {
        return LocalTimeReflection.now_method.invoke(LocalTimeReflection.localTime, new Object[0]);
    }
    
    public static int getHour(final Object object) throws Exception {
        return Integer.parseInt(LocalTimeReflection.getHour_method.invoke(object, new Object[0]).toString());
    }
    
    public static int getMinute(final Object object) throws Exception {
        return Integer.parseInt(LocalTimeReflection.getMinute_method.invoke(object, new Object[0]).toString());
    }
    
    public static int getSecond(final Object object) throws Exception {
        return Integer.parseInt(LocalTimeReflection.getSecond_method.invoke(object, new Object[0]).toString());
    }
    
    public static int getNano(final Object object) throws Exception {
        return Integer.parseInt(LocalTimeReflection.getNano_method.invoke(object, new Object[0]).toString());
    }
    
    public static Timestamp getTime(final Object object) throws Exception {
        final StringBuilder stringBuilder = new StringBuilder();
        final Date date = new Date();
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return Timestamp.valueOf(stringBuilder.append(format.format(date)).append(" ").append(getHour(object)).append(":").append(getMinute(object)).append(":").append(getSecond(object)).append(".").append(getNano(object)).toString());
    }
    
    static {
        try {
            LocalTimeReflection.localTime = Class.forName("java.time.LocalTime");
            LocalTimeReflection.of_method = LocalTimeReflection.localTime.getMethod("of", Integer.TYPE, Integer.TYPE, Integer.TYPE);
            LocalTimeReflection.of_method2 = LocalTimeReflection.localTime.getMethod("of", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE);
            LocalTimeReflection.now_method = LocalTimeReflection.localTime.getMethod("now", (Class[])new Class[0]);
            LocalTimeReflection.getHour_method = LocalTimeReflection.localTime.getMethod("getHour", (Class[])new Class[0]);
            LocalTimeReflection.getMinute_method = LocalTimeReflection.localTime.getMethod("getMinute", (Class[])new Class[0]);
            LocalTimeReflection.getSecond_method = LocalTimeReflection.localTime.getMethod("getSecond", (Class[])new Class[0]);
            LocalTimeReflection.getNano_method = LocalTimeReflection.localTime.getMethod("getNano", (Class[])new Class[0]);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
