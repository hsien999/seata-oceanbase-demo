// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.socket;

import java.io.IOException;
import java.net.Socket;
import com.oceanbase.jdbc.util.Options;

@FunctionalInterface
public interface SocketHandlerFunction
{
    Socket apply(final Options p0, final String p1) throws IOException;
}
