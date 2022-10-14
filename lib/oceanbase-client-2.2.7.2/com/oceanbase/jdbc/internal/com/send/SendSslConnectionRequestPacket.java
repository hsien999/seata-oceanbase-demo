// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send;

import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class SendSslConnectionRequestPacket
{
    public static void send(final PacketOutputStream pos, final long clientCapabilities, final byte serverLanguage) throws IOException {
        pos.startPacket(1);
        pos.writeInt((int)clientCapabilities);
        pos.writeInt(1073741824);
        pos.write(serverLanguage);
        pos.writeBytes((byte)0, 19);
        pos.writeInt((int)(clientCapabilities >> 32));
        pos.flush();
    }
}
