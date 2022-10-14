// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import com.oceanbase.jdbc.internal.util.Utils;
import java.util.Arrays;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.authentication.AuthenticationPlugin;

public class NativePasswordPlugin implements AuthenticationPlugin
{
    public static final String TYPE = "mysql_native_password";
    private String authenticationData;
    private String passwordCharacterEncoding;
    private byte[] seed;
    
    @Override
    public String name() {
        return "mysql native password";
    }
    
    @Override
    public String type() {
        return "mysql_native_password";
    }
    
    @Override
    public void initialize(final String authenticationData, final byte[] seed, final Options options) {
        this.seed = seed;
        this.authenticationData = authenticationData;
        this.passwordCharacterEncoding = options.passwordCharacterEncoding;
    }
    
    @Override
    public Buffer process(final PacketOutputStream out, final PacketInputStream in, final AtomicInteger sequence) throws IOException {
        if (this.authenticationData == null || this.authenticationData.isEmpty()) {
            out.writeEmptyPacket(sequence.incrementAndGet());
        }
        else {
            try {
                out.startPacket(sequence.incrementAndGet());
                byte[] truncatedSeed;
                if (this.seed.length > 0) {
                    truncatedSeed = Arrays.copyOfRange(this.seed, 0, this.seed.length - 1);
                }
                else {
                    truncatedSeed = new byte[0];
                }
                out.write(Utils.encryptPassword(this.authenticationData, truncatedSeed, this.passwordCharacterEncoding));
                out.flush();
            }
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Could not use SHA-1, failing", e);
            }
        }
        final Buffer buffer = in.getPacket(true);
        sequence.set(in.getLastPacketSeq());
        return buffer;
    }
}
