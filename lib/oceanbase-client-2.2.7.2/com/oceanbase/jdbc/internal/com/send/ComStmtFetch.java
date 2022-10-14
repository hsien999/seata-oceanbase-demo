// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send;

import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class ComStmtFetch
{
    public static void send(final PacketOutputStream pos, final int fetchSize, final int statementId) throws IOException {
        pos.startPacket(0);
        pos.write(28);
        pos.writeInt(statementId);
        pos.writeInt(fetchSize);
        pos.flush();
    }
}
