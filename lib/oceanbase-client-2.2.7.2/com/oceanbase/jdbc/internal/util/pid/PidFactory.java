// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.pid;

import java.lang.reflect.Method;
import java.util.function.Supplier;

public class PidFactory
{
    private static Supplier<String> instance;
    
    public static Supplier<String> getInstance() {
        return PidFactory.instance;
    }
    
    static {
        try {
            final Class<?> processHandle = Class.forName("java.lang.ProcessHandle");
            final Class clazz;
            Method currentProcessMethod;
            Object currentProcess;
            Method pidMethod;
            PidFactory.instance = (() -> {
                try {
                    currentProcessMethod = clazz.getMethod("current", (Class[])new Class[0]);
                    currentProcess = currentProcessMethod.invoke(null, new Object[0]);
                    pidMethod = clazz.getMethod("pid", (Class[])new Class[0]);
                    return String.valueOf(pidMethod.invoke(currentProcess, new Object[0]));
                }
                catch (Throwable throwable) {
                    return null;
                }
            });
        }
        catch (Throwable cle) {
            try {
                PidFactory.instance = JnaPidFactory.getInstance();
            }
            catch (Throwable throwable2) {
                PidFactory.instance = (Supplier<String>)(() -> null);
            }
        }
    }
}
