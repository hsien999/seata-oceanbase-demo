// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication;

import java.io.IOException;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.authentication.AuthenticationPlugin;

public class ClearPasswordPlugin implements AuthenticationPlugin
{
    public static final String TYPE = "mysql_clear_password";
    private String authenticationData;
    private String passwordCharacterEncoding;
    
    @Override
    public String name() {
        return "mysql clear password";
    }
    
    @Override
    public String type() {
        return "mysql_clear_password";
    }
    
    @Override
    public boolean mustUseSsl() {
        return true;
    }
    
    @Override
    public void initialize(final String authenticationData, final byte[] authData, final Options options) {
        this.authenticationData = authenticationData;
        this.passwordCharacterEncoding = options.passwordCharacterEncoding;
    }
    
    @Override
    public Buffer process(final PacketOutputStream out, final PacketInputStream in, final AtomicInteger sequence) throws IOException {
        if (this.authenticationData == null || this.authenticationData.isEmpty()) {
            out.writeEmptyPacket(sequence.incrementAndGet());
        }
        else {
            out.startPacket(sequence.incrementAndGet());
            byte[] bytePwd;
            if (this.passwordCharacterEncoding != null && !this.passwordCharacterEncoding.isEmpty()) {
                bytePwd = this.authenticationData.getBytes(this.passwordCharacterEncoding);
            }
            else {
                bytePwd = this.authenticationData.getBytes();
            }
            out.write(bytePwd);
            out.write(0);
            out.flush();
        }
        final Buffer buffer = in.getPacket(true);
        sequence.set(in.getLastPacketSeq());
        return buffer;
    }
}
