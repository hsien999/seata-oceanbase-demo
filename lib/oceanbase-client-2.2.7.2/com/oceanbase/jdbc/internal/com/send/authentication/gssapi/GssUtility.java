// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.gssapi;

import com.sun.jna.Platform;

public class GssUtility
{
    public static GssapiAuth getAuthenticationMethod() {
        try {
            if (Platform.isWindows()) {
                try {
                    Class.forName("waffle.windows.auth.impl.WindowsAuthProviderImpl");
                    return new WindowsNativeSspiAuthentication();
                }
                catch (ClassNotFoundException ex) {}
            }
        }
        catch (Throwable t) {}
        return new StandardGssapiAuthentication();
    }
}
