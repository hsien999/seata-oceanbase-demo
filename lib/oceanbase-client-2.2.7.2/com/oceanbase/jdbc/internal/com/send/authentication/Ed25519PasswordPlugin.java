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
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.GroupElement;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.spec.EdDSAParameterSpec;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.spec.EdDSANamedCurveTable;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.ed25519.ScalarOps;
import java.util.Arrays;
import java.security.MessageDigest;
import com.oceanbase.jdbc.authentication.AuthenticationPlugin;

public class Ed25519PasswordPlugin implements AuthenticationPlugin
{
    private String authenticationData;
    private String passwordCharacterEncoding;
    private byte[] seed;
    
    private static byte[] ed25519SignWithPassword(final String password, final byte[] seed, final String passwordCharacterEncoding) throws SQLException {
        try {
            byte[] bytePwd;
            if (passwordCharacterEncoding != null && !passwordCharacterEncoding.isEmpty()) {
                bytePwd = password.getBytes(passwordCharacterEncoding);
            }
            else {
                bytePwd = password.getBytes();
            }
            final MessageDigest hash = MessageDigest.getInstance("SHA-512");
            final int mlen = seed.length;
            final byte[] sm = new byte[64 + mlen];
            final byte[] digest;
            final byte[] az = digest = hash.digest(bytePwd);
            final int n = 0;
            digest[n] &= (byte)248;
            final byte[] array = az;
            final int n2 = 31;
            array[n2] &= 0x3F;
            final byte[] array2 = az;
            final int n3 = 31;
            array2[n3] |= 0x40;
            System.arraycopy(seed, 0, sm, 64, mlen);
            System.arraycopy(az, 32, sm, 32, 32);
            final byte[] buff = Arrays.copyOfRange(sm, 32, 96);
            hash.reset();
            byte[] nonce = hash.digest(buff);
            final ScalarOps scalar = new ScalarOps();
            final EdDSAParameterSpec spec = EdDSANamedCurveTable.getByName("Ed25519");
            final GroupElement elementAvalue = spec.getB().scalarMultiply(az);
            final byte[] elementAarray = elementAvalue.toByteArray();
            System.arraycopy(elementAarray, 0, sm, 32, elementAarray.length);
            nonce = scalar.reduce(nonce);
            final GroupElement elementRvalue = spec.getB().scalarMultiply(nonce);
            final byte[] elementRarray = elementRvalue.toByteArray();
            System.arraycopy(elementRarray, 0, sm, 0, elementRarray.length);
            hash.reset();
            byte[] hram = hash.digest(sm);
            hram = scalar.reduce(hram);
            final byte[] tt = scalar.multiplyAndAdd(hram, az, nonce);
            System.arraycopy(tt, 0, sm, 32, tt.length);
            return Arrays.copyOfRange(sm, 0, 64);
        }
        catch (NoSuchAlgorithmException e) {
            throw new SQLException("Could not use SHA-512, failing", e);
        }
        catch (UnsupportedEncodingException use) {
            throw new SQLException("Unsupported encoding '" + passwordCharacterEncoding + "' (option passwordCharacterEncoding)", use);
        }
    }
    
    @Override
    public String name() {
        return "Ed25519 authentication plugin";
    }
    
    @Override
    public String type() {
        return "client_ed25519";
    }
    
    @Override
    public void initialize(final String authenticationData, final byte[] seed, final Options options) {
        this.seed = seed;
        this.authenticationData = authenticationData;
        this.passwordCharacterEncoding = options.passwordCharacterEncoding;
    }
    
    @Override
    public Buffer process(final PacketOutputStream out, final PacketInputStream in, final AtomicInteger sequence) throws IOException, SQLException {
        if (this.authenticationData == null || this.authenticationData.isEmpty()) {
            out.writeEmptyPacket(sequence.incrementAndGet());
        }
        else {
            out.startPacket(sequence.incrementAndGet());
            out.write(ed25519SignWithPassword(this.authenticationData, this.seed, this.passwordCharacterEncoding));
            out.flush();
        }
        final Buffer buffer = in.getPacket(true);
        sequence.set(in.getLastPacketSeq());
        return buffer;
    }
}
