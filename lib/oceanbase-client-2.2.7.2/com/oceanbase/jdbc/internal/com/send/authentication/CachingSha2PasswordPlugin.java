// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication;

import java.io.IOException;
import java.security.PublicKey;
import java.sql.SQLException;
import java.util.Arrays;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.authentication.AuthenticationPlugin;

public class CachingSha2PasswordPlugin implements AuthenticationPlugin
{
    public static final String TYPE = "caching_sha2_password";
    private String authenticationData;
    private byte[] seed;
    private Options options;
    
    public static byte[] sha256encryptPassword(final String password, final byte[] seed, final String passwordCharacterEncoding) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (password == null || password.isEmpty()) {
            return new byte[0];
        }
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] bytePwd;
        if (passwordCharacterEncoding != null && !passwordCharacterEncoding.isEmpty()) {
            bytePwd = password.getBytes(passwordCharacterEncoding);
        }
        else {
            bytePwd = password.getBytes();
        }
        final byte[] stage1 = messageDigest.digest(bytePwd);
        messageDigest.reset();
        final byte[] stage2 = messageDigest.digest(stage1);
        messageDigest.reset();
        messageDigest.update(stage2);
        messageDigest.update(seed);
        final byte[] digest = messageDigest.digest();
        final byte[] returnBytes = new byte[digest.length];
        for (int i = 0; i < digest.length; ++i) {
            returnBytes[i] = (byte)(stage1[i] ^ digest[i]);
        }
        return returnBytes;
    }
    
    @Override
    public String name() {
        return "caching sha2 password";
    }
    
    @Override
    public String type() {
        return "caching_sha2_password";
    }
    
    @Override
    public void initialize(final String authenticationData, final byte[] seed, final Options options) {
        this.seed = seed;
        this.authenticationData = authenticationData;
        this.options = options;
    }
    
    @Override
    public Buffer process(final PacketOutputStream out, final PacketInputStream in, final AtomicInteger sequence) throws IOException, SQLException {
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
                out.write(sha256encryptPassword(this.authenticationData, truncatedSeed, this.options.passwordCharacterEncoding));
                out.flush();
            }
            catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Could not use SHA-256, failing", e);
            }
        }
        Buffer buffer = in.getPacket(true);
        sequence.set(in.getLastPacketSeq());
        switch (buffer.getByteAt(0)) {
            case -1:
            case 0: {
                return buffer;
            }
            default: {
                final byte[] authResult = buffer.getLengthEncodedBytes();
                switch (authResult[0]) {
                    case 3: {
                        buffer = in.getPacket(true);
                        sequence.set(in.getLastPacketSeq());
                        return buffer;
                    }
                    case 4: {
                        if (Boolean.TRUE.equals(this.options.useSsl)) {
                            out.startPacket(sequence.incrementAndGet());
                            byte[] bytePwd;
                            if (this.options.passwordCharacterEncoding != null && !this.options.passwordCharacterEncoding.isEmpty()) {
                                bytePwd = this.authenticationData.getBytes(this.options.passwordCharacterEncoding);
                            }
                            else {
                                bytePwd = this.authenticationData.getBytes();
                            }
                            out.write(bytePwd);
                            out.write(0);
                            out.flush();
                        }
                        else {
                            PublicKey publicKey;
                            if (this.options.serverRsaPublicKeyFile != null && !this.options.serverRsaPublicKeyFile.isEmpty()) {
                                publicKey = Sha256PasswordPlugin.readPublicKeyFromFile(this.options.serverRsaPublicKeyFile);
                            }
                            else {
                                if (!this.options.allowPublicKeyRetrieval) {
                                    throw new SQLException("RSA public key is not available client side (option serverRsaPublicKeyFile not set)", "S1009");
                                }
                                out.startPacket(sequence.incrementAndGet());
                                out.write(2);
                                out.flush();
                                publicKey = Sha256PasswordPlugin.readPublicKeyFromSocket(in, sequence);
                            }
                            try {
                                final byte[] cipherBytes = Sha256PasswordPlugin.encrypt(publicKey, this.authenticationData, this.seed, this.options.passwordCharacterEncoding);
                                out.startPacket(sequence.incrementAndGet());
                                out.write(cipherBytes);
                                out.flush();
                            }
                            catch (Exception ex) {
                                throw new SQLException("Could not connect using SHA256 plugin : " + ex.getMessage(), "S1009", ex);
                            }
                        }
                        buffer = in.getPacket(true);
                        sequence.set(in.getLastPacketSeq());
                        return buffer;
                    }
                    default: {
                        throw new SQLException("Protocol exchange error. Expect login success or RSA login request message", "S1009");
                    }
                }
                break;
            }
        }
    }
}
