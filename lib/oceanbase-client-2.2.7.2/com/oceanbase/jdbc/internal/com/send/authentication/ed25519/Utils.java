// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.ed25519;

public class Utils
{
    public static int equal(final int b, final int c) {
        int result = 0;
        final int xor = b ^ c;
        for (int i = 0; i < 8; ++i) {
            result |= xor >> i;
        }
        return (result ^ 0x1) & 0x1;
    }
    
    public static int equal(final byte[] b, final byte[] c) {
        int result = 0;
        for (int i = 0; i < 32; ++i) {
            result |= (b[i] ^ c[i]);
        }
        return equal(result, 0);
    }
    
    public static int negative(final int b) {
        return b >> 8 & 0x1;
    }
    
    public static int bit(final byte[] h, final int i) {
        return h[i >> 3] >> (i & 0x7) & 0x1;
    }
    
    public static byte[] hexToBytes(final String s) {
        final int len = s.length();
        final byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
    
    public static String bytesToHex(final byte[] raw) {
        if (raw == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(Character.forDigit((b & 0xF0) >> 4, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return hex.toString();
    }
}
