// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.gssapi;

import java.io.IOException;
import com.sun.jna.platform.win32.Sspi;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import waffle.windows.auth.IWindowsSecurityContext;
import com.sun.jna.platform.win32.SspiUtil;
import waffle.windows.auth.impl.WindowsSecurityContextImpl;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class WindowsNativeSspiAuthentication implements GssapiAuth
{
    @Override
    public void authenticate(final PacketOutputStream out, final PacketInputStream in, final AtomicInteger sequence, final String servicePrincipalName, final String mechanisms) throws IOException {
        final IWindowsSecurityContext clientContext = WindowsSecurityContextImpl.getCurrent(mechanisms, servicePrincipalName);
        do {
            final byte[] tokenForTheServerOnTheClient = clientContext.getToken();
            out.startPacket(sequence.incrementAndGet());
            out.write(tokenForTheServerOnTheClient);
            out.flush();
            if (clientContext.isContinue()) {
                final Buffer buffer = in.getPacket(true);
                sequence.set(in.getLastPacketSeq());
                final byte[] tokenForTheClientOnTheServer = buffer.readRawBytes(buffer.remaining());
                final Sspi.SecBufferDesc continueToken = (Sspi.SecBufferDesc)new SspiUtil.ManagedSecBufferDesc(2, tokenForTheClientOnTheServer);
                clientContext.initialize(clientContext.getHandle(), continueToken, servicePrincipalName);
            }
        } while (clientContext.isContinue());
        clientContext.dispose();
    }
}
