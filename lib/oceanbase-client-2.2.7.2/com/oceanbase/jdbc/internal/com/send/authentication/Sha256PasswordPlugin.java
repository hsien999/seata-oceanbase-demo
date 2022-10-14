// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication;

import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import javax.crypto.Cipher;
import java.util.Arrays;
import java.security.spec.KeySpec;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import com.oceanbase.jdbc.internal.com.read.ErrorPacket;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.authentication.AuthenticationPlugin;

public class Sha256PasswordPlugin implements AuthenticationPlugin
{
    private String authenticationData;
    private Options options;
    private byte[] seed;
    
    public static PublicKey readPublicKeyFromFile(final String serverRsaPublicKeyFile) throws SQLException {
        byte[] keyBytes;
        try {
            keyBytes = Files.readAllBytes(Paths.get(serverRsaPublicKeyFile, new String[0]));
        }
        catch (IOException ex) {
            throw new SQLException("Could not read server RSA public key from file : serverRsaPublicKeyFile=" + serverRsaPublicKeyFile, "S1009", ex);
        }
        return generatePublicKey(keyBytes);
    }
    
    public static PublicKey readPublicKeyFromSocket(final PacketInputStream reader, final AtomicInteger sequence) throws SQLException, IOException {
        final Buffer buffer = reader.getPacket(true);
        sequence.set(reader.getLastPacketSeq());
        switch (buffer.getByteAt(0)) {
            case -1: {
                final ErrorPacket ep = new ErrorPacket(buffer);
                final String message = ep.getMessage();
                throw new SQLException("Could not connect: " + message, ep.getSqlState(), ep.getErrorCode());
            }
            case -2: {
                throw new SQLException("Could not connect: receive AuthSwitchRequest in place of RSA public key. Did user has the rights to connect to database ?");
            }
            default: {
                buffer.skipByte();
                return generatePublicKey(buffer.readRawBytes(buffer.remaining()));
            }
        }
    }
    
    public static PublicKey generatePublicKey(final byte[] publicKeyBytes) throws SQLException {
        try {
            final String publicKey = new String(publicKeyBytes).replaceAll("(-+BEGIN PUBLIC KEY-+\\r?\\n|\\n?-+END PUBLIC KEY-+\\r?\\n?)", "");
            final byte[] keyBytes = Base64.getMimeDecoder().decode(publicKey);
            final X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            final KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        }
        catch (Exception ex) {
            throw new SQLException("Could read server RSA public key: " + ex.getMessage(), "S1009", ex);
        }
    }
    
    public static byte[] encrypt(final PublicKey publicKey, final String password, final byte[] seed, final String passwordCharacterEncoding) throws SQLException, UnsupportedEncodingException {
        byte[] correctedSeed;
        if (seed.length > 0) {
            correctedSeed = Arrays.copyOfRange(seed, 0, seed.length - 1);
        }
        else {
            correctedSeed = new byte[0];
        }
        byte[] bytePwd;
        if (passwordCharacterEncoding != null && !passwordCharacterEncoding.isEmpty()) {
            bytePwd = password.getBytes(passwordCharacterEncoding);
        }
        else {
            bytePwd = password.getBytes();
        }
        final byte[] nullFinishedPwd = Arrays.copyOf(bytePwd, bytePwd.length + 1);
        final byte[] xorBytes = new byte[nullFinishedPwd.length];
        final int seedLength = correctedSeed.length;
        for (int i = 0; i < xorBytes.length; ++i) {
            xorBytes[i] = (byte)(nullFinishedPwd[i] ^ correctedSeed[i % seedLength]);
        }
        try {
            final Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(1, publicKey);
            return cipher.doFinal(xorBytes);
        }
        catch (Exception ex) {
            throw new SQLException("Could not connect using SHA256 plugin : " + ex.getMessage(), "S1009", ex);
        }
    }
    
    @Override
    public String name() {
        return "Sha256 authentication plugin";
    }
    
    @Override
    public String type() {
        return "sha256_password";
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
        else if (Boolean.TRUE.equals(this.options.useSsl)) {
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
                publicKey = readPublicKeyFromFile(this.options.serverRsaPublicKeyFile);
            }
            else {
                if (!this.options.allowPublicKeyRetrieval) {
                    throw new SQLException("RSA public key is not available client side (option serverRsaPublicKeyFile)", "S1009");
                }
                out.startPacket(sequence.incrementAndGet());
                out.write(1);
                out.flush();
                publicKey = readPublicKeyFromSocket(in, sequence);
            }
            try {
                final byte[] cipherBytes = encrypt(publicKey, this.authenticationData, this.seed, this.options.passwordCharacterEncoding);
                out.startPacket(sequence.incrementAndGet());
                out.write(cipherBytes);
                out.flush();
            }
            catch (Exception ex) {
                throw new SQLException("Could not connect using SHA256 plugin : " + ex.getMessage(), "S1009", ex);
            }
        }
        final Buffer buffer = in.getPacket(true);
        sequence.set(in.getLastPacketSeq());
        return buffer;
    }
}
