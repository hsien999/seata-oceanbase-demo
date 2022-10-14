// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdk8;

import java.sql.Timestamp;
import java.lang.reflect.Method;

public class SqlTimeStampReflection
{
    private static final String CLASS_NAME = "java.sql.Timestamp";
    private static Method toLocalDateTime;
    private static Class sqlTimeStamp;
    
    public static Object toLocalTime(final Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        try {
            return SqlTimeStampReflection.toLocalDateTime.invoke(timestamp, new Object[0]);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    static {
        SqlTimeStampReflection.toLocalDateTime = null;
        SqlTimeStampReflection.sqlTimeStamp = null;
        try {
            SqlTimeStampReflection.sqlTimeStamp = Class.forName("java.sql.Timestamp");
            SqlTimeStampReflection.toLocalDateTime = SqlTimeStampReflection.sqlTimeStamp.getMethod("toLocalDateTime", (Class[])new Class[0]);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
