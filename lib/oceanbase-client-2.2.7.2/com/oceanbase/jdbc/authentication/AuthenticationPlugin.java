// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.authentication;

import java.sql.SQLException;
import java.io.IOException;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.util.Options;

public interface AuthenticationPlugin
{
    String name();
    
    String type();
    
    default boolean mustUseSsl() {
        return false;
    }
    
    void initialize(final String p0, final byte[] p1, final Options p2);
    
    Buffer process(final PacketOutputStream p0, final PacketInputStream p1, final AtomicInteger p2) throws IOException, SQLException;
}
