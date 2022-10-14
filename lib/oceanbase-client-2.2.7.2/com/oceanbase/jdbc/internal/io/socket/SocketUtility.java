// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.socket;

import java.net.Socket;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.util.Utils;
import java.io.IOException;
import com.sun.jna.Platform;

public class SocketUtility
{
    public static SocketHandlerFunction getSocketHandler() {
        try {
            Platform.getOSType();
            return (options, host) -> {
                if (options.pipe != null) {
                    return new NamedPipeSocket(host, options.pipe);
                }
                else {
                    if (options.localSocket != null) {
                        try {
                            return new UnixDomainSocket(options.localSocket);
                        }
                        catch (RuntimeException re) {
                            throw new IOException(re.getMessage(), re.getCause());
                        }
                    }
                    if (options.sharedMemory != null) {
                        try {
                            return new SharedMemorySocket(options.sharedMemory);
                        }
                        catch (RuntimeException re2) {
                            throw new IOException(re2.getMessage(), re2.getCause());
                        }
                    }
                    if (options.socksProxyHost != null) {
                        return Utils.socksSocket(options);
                    }
                    else {
                        return Utils.standardSocket(options, host);
                    }
                }
            };
        }
        catch (Throwable t) {
            return (options, host) -> Utils.standardSocket(options, host);
        }
    }
}
