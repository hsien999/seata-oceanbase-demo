// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdk8;

import java.sql.Date;
import java.lang.reflect.Method;

public class SqlDateReflection
{
    private static final String CLASS_NAME = "java.sql.Date";
    private static Method toLocalDate;
    private static Class sqlDate;
    
    public static Object toLocalDate(final Date date) {
        if (date == null) {
            return null;
        }
        try {
            return SqlDateReflection.toLocalDate.invoke(date, new Object[0]);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    static {
        SqlDateReflection.toLocalDate = null;
        SqlDateReflection.sqlDate = null;
        try {
            SqlDateReflection.sqlDate = Class.forName("java.sql.Date");
            SqlDateReflection.toLocalDate = SqlDateReflection.sqlDate.getMethod("toLocalDate", (Class[])new Class[0]);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
