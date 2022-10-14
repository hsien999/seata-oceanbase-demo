// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdk8;

import java.sql.Time;
import java.lang.reflect.Method;

public class SqlTimeReflection
{
    private static final String CLASS_NAME = "java.sql.Time";
    private static Method toLocalTime;
    private static Class sqlDate;
    
    public static Object toLocalTime(final Time time) {
        if (time == null) {
            return null;
        }
        try {
            return SqlTimeReflection.toLocalTime.invoke(time, new Object[0]);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    static {
        SqlTimeReflection.toLocalTime = null;
        SqlTimeReflection.sqlDate = null;
        try {
            SqlTimeReflection.sqlDate = Class.forName("java.sql.Time");
            SqlTimeReflection.toLocalTime = SqlTimeReflection.sqlDate.getMethod("toLocalTime", (Class[])new Class[0]);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
