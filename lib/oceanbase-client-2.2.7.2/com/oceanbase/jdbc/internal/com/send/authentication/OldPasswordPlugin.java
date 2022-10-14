// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication;

import java.io.IOException;
import com.oceanbase.jdbc.internal.util.Utils;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.authentication.AuthenticationPlugin;

public class OldPasswordPlugin implements AuthenticationPlugin
{
    public static final String TYPE = "mysql_old_password";
    private String authenticationData;
    private byte[] seed;
    
    @Override
    public String name() {
        return "mysql pre 4.1 password encoding";
    }
    
    @Override
    public String type() {
        return "mysql_old_password";
    }
    
    @Override
    public void initialize(final String authenticationData, final byte[] seed, final Options options) {
        this.seed = seed;
        this.authenticationData = authenticationData;
    }
    
    @Override
    public Buffer process(final PacketOutputStream out, final PacketInputStream in, final AtomicInteger sequence) throws IOException {
        if (this.authenticationData == null || this.authenticationData.isEmpty()) {
            out.writeEmptyPacket(sequence.incrementAndGet());
        }
        else {
            out.startPacket(sequence.incrementAndGet());
            final byte[] seed = Utils.copyWithLength(this.seed, 8);
            out.write(this.cryptOldFormatPassword(this.authenticationData, new String(seed)));
            out.write(0);
            out.flush();
        }
        final Buffer buffer = in.getPacket(true);
        sequence.set(in.getLastPacketSeq());
        return buffer;
    }
    
    private byte[] cryptOldFormatPassword(final String password, final String seed) {
        final byte[] result = new byte[seed.length()];
        if (password == null || password.length() == 0) {
            return new byte[0];
        }
        final long[] seedHash = this.hashPassword(seed);
        final long[] passHash = this.hashPassword(password);
        final RandStruct randSeed = new RandStruct(seedHash[0] ^ passHash[0], seedHash[1] ^ passHash[1]);
        for (int i = 0; i < seed.length(); ++i) {
            result[i] = (byte)Math.floor(this.random(randSeed) * 31.0 + 64.0);
        }
        final byte extra = (byte)Math.floor(this.random(randSeed) * 31.0);
        for (int j = 0; j < seed.length(); ++j) {
            final byte[] array = result;
            final int n = j;
            array[n] ^= extra;
        }
        return result;
    }
    
    private double random(final RandStruct rand) {
        rand.seed1 = (rand.seed1 * 3L + rand.seed2) % 1073741823L;
        rand.seed2 = (rand.seed1 + rand.seed2 + 33L) % 1073741823L;
        return rand.seed1 / 1.073741823E9;
    }
    
    private long[] hashPassword(final String password) {
        long nr = 1345345333L;
        long nr2 = 305419889L;
        long add = 7L;
        for (int i = 0; i < password.length(); ++i) {
            final char currChar = password.charAt(i);
            if (currChar != ' ') {
                if (currChar != '\t') {
                    nr ^= ((nr & 0x3FL) + add) * currChar + (nr << 8);
                    nr2 += (nr2 << 8 ^ nr);
                    add += currChar;
                }
            }
        }
        return new long[] { nr & 0x7FFFFFFFL, nr2 & 0x7FFFFFFFL };
    }
    
    private class RandStruct
    {
        private final long maxValue = 1073741823L;
        private long seed1;
        private long seed2;
        
        public RandStruct(final long seed1, final long seed2) {
            this.seed1 = seed1 % 1073741823L;
            this.seed2 = seed2 % 1073741823L;
        }
    }
}
