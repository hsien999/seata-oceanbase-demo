// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send;

import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class SendChangeDbPacket
{
    public static void send(final PacketOutputStream pos, final String database) throws IOException {
        pos.startPacket(0);
        pos.write(2);
        pos.write(database.getBytes(pos.getCharset()));
        pos.flush();
    }
}
