// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.gssapi;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public interface GssapiAuth
{
    void authenticate(final PacketOutputStream p0, final PacketInputStream p1, final AtomicInteger p2, final String p3, final String p4) throws SQLException, IOException;
}
