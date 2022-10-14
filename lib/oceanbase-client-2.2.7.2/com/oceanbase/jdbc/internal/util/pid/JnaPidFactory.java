// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.pid;

import com.sun.jna.Native;
import com.sun.jna.Library;
import com.sun.jna.Platform;
import com.sun.jna.platform.win32.Kernel32;
import java.util.function.Supplier;

public class JnaPidFactory
{
    private static Supplier<String> instance;
    
    public static Supplier<String> getInstance() {
        return JnaPidFactory.instance;
    }
    
    static {
        try {
            if (Platform.isLinux()) {
                CLibrary.INSTANCE.getpid();
                JnaPidFactory.instance = (() -> String.valueOf(CLibrary.INSTANCE.getpid()));
            }
            else if (Platform.isWindows()) {
                try {
                    Kernel32.INSTANCE.GetCurrentProcessId();
                    JnaPidFactory.instance = (() -> String.valueOf(Kernel32.INSTANCE.GetCurrentProcessId()));
                }
                catch (Throwable cle) {
                    JnaPidFactory.instance = (Supplier<String>)(() -> null);
                }
            }
            else {
                JnaPidFactory.instance = (Supplier<String>)(() -> null);
            }
        }
        catch (Throwable cle) {
            JnaPidFactory.instance = (Supplier<String>)(() -> null);
        }
    }
    
    private interface CLibrary extends Library
    {
        public static final CLibrary INSTANCE = (CLibrary)Native.load("c", (Class)CLibrary.class);
        
        int getpid();
    }
}
