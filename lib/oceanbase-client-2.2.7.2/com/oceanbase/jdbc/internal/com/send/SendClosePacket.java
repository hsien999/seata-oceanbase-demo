// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send;

import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class SendClosePacket
{
    public static void send(final PacketOutputStream pos) {
        try {
            pos.startPacket(0);
            pos.write(1);
            pos.flush();
        }
        catch (IOException ex) {}
    }
}
