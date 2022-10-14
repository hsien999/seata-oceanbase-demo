// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.util;

public class Base64Decoder
{
    private static byte[] decoderMap;
    
    private static byte getNextValidByte(final byte[] in, final IntWrapper pos, final int maxPos) {
        while (pos.value <= maxPos) {
            if (in[pos.value] >= 0 && Base64Decoder.decoderMap[in[pos.value]] >= 0) {
                return in[pos.value++];
            }
            ++pos.value;
        }
        return 61;
    }
    
    public static byte[] decode(final byte[] in, final int pos, final int length) {
        final IntWrapper offset = new IntWrapper(pos);
        final byte[] sestet = new byte[4];
        final int outLen = length * 3 / 4;
        final byte[] octet = new byte[outLen];
        int octetId = 0;
        final int maxPos = offset.value + length - 1;
        while (offset.value <= maxPos) {
            sestet[0] = Base64Decoder.decoderMap[getNextValidByte(in, offset, maxPos)];
            sestet[1] = Base64Decoder.decoderMap[getNextValidByte(in, offset, maxPos)];
            sestet[2] = Base64Decoder.decoderMap[getNextValidByte(in, offset, maxPos)];
            sestet[3] = Base64Decoder.decoderMap[getNextValidByte(in, offset, maxPos)];
            if (sestet[1] != -2) {
                octet[octetId++] = (byte)(sestet[0] << 2 | sestet[1] >>> 4);
            }
            if (sestet[2] != -2) {
                octet[octetId++] = (byte)((sestet[1] & 0xF) << 4 | sestet[2] >>> 2);
            }
            if (sestet[3] != -2) {
                octet[octetId++] = (byte)((sestet[2] & 0x3) << 6 | sestet[3]);
            }
        }
        final byte[] out = new byte[octetId];
        System.arraycopy(octet, 0, out, 0, octetId);
        return out;
    }
    
    static {
        Base64Decoder.decoderMap = new byte[] { -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1 };
    }
    
    public static class IntWrapper
    {
        public int value;
        
        public IntWrapper(final int value) {
            this.value = value;
        }
    }
}
