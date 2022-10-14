// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication;

import java.io.IOException;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.authentication.AuthenticationPlugin;

public class SendPamAuthPacket implements AuthenticationPlugin
{
    private String authenticationData;
    private String passwordCharacterEncoding;
    private Options options;
    private int counter;
    
    public SendPamAuthPacket() {
        this.counter = 0;
    }
    
    @Override
    public String name() {
        return "PAM client authentication";
    }
    
    @Override
    public String type() {
        return "dialog";
    }
    
    @Override
    public void initialize(final String authenticationData, final byte[] seed, final Options options) {
        this.authenticationData = authenticationData;
        this.passwordCharacterEncoding = options.passwordCharacterEncoding;
        this.options = options;
    }
    
    @Override
    public Buffer process(final PacketOutputStream out, final PacketInputStream in, final AtomicInteger sequence) throws IOException, SQLException {
        while (true) {
            ++this.counter;
            String password;
            if (this.counter == 1) {
                password = this.authenticationData;
            }
            else {
                if (!this.options.nonMappedOptions.containsKey("password" + this.counter)) {
                    throw new SQLException("PAM authentication request multiple passwords, but 'password" + this.counter + "' is not set");
                }
                password = (String)this.options.nonMappedOptions.get("password" + this.counter);
            }
            out.startPacket(sequence.incrementAndGet());
            byte[] bytePwd;
            if (this.passwordCharacterEncoding != null && !this.passwordCharacterEncoding.isEmpty()) {
                bytePwd = password.getBytes(this.passwordCharacterEncoding);
            }
            else {
                bytePwd = password.getBytes();
            }
            out.write(bytePwd, 0, bytePwd.length);
            out.write(0);
            out.flush();
            final Buffer buffer = in.getPacket(true);
            sequence.set(in.getLastPacketSeq());
            final int type = buffer.getByteAt(0) & 0xFF;
            if (type == 254 || type == 0 || type == 255) {
                return buffer;
            }
        }
    }
}
