// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.ed25519;

import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.FieldElement;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.Encoding;

public class Ed25519LittleEndianEncoding extends Encoding
{
    static int load_3(final byte[] in, int offset) {
        int result = in[offset++] & 0xFF;
        result |= (in[offset++] & 0xFF) << 8;
        result |= (in[offset] & 0xFF) << 16;
        return result;
    }
    
    static long load_4(final byte[] in, int offset) {
        int result = in[offset++] & 0xFF;
        result |= (in[offset++] & 0xFF) << 8;
        result |= (in[offset++] & 0xFF) << 16;
        result |= in[offset] << 24;
        return (long)result & 0xFFFFFFFFL;
    }
    
    @Override
    public byte[] encode(final FieldElement x) {
        final int[] h = ((Ed25519FieldElement)x).t;
        int h2 = h[0];
        int h3 = h[1];
        int h4 = h[2];
        int h5 = h[3];
        int h6 = h[4];
        int h7 = h[5];
        int h8 = h[6];
        int h9 = h[7];
        int h10 = h[8];
        int h11 = h[9];
        int q = 19 * h11 + 16777216 >> 25;
        q = h2 + q >> 26;
        q = h3 + q >> 25;
        q = h4 + q >> 26;
        q = h5 + q >> 25;
        q = h6 + q >> 26;
        q = h7 + q >> 25;
        q = h8 + q >> 26;
        q = h9 + q >> 25;
        q = h10 + q >> 26;
        q = h11 + q >> 25;
        h2 += 19 * q;
        final int carry0 = h2 >> 26;
        h3 += carry0;
        h2 -= carry0 << 26;
        final int carry2 = h3 >> 25;
        h4 += carry2;
        h3 -= carry2 << 25;
        final int carry3 = h4 >> 26;
        h5 += carry3;
        h4 -= carry3 << 26;
        final int carry4 = h5 >> 25;
        h6 += carry4;
        h5 -= carry4 << 25;
        final int carry5 = h6 >> 26;
        h7 += carry5;
        h6 -= carry5 << 26;
        final int carry6 = h7 >> 25;
        h8 += carry6;
        h7 -= carry6 << 25;
        final int carry7 = h8 >> 26;
        h9 += carry7;
        h8 -= carry7 << 26;
        final int carry8 = h9 >> 25;
        h10 += carry8;
        h9 -= carry8 << 25;
        final int carry9 = h10 >> 26;
        h11 += carry9;
        h10 -= carry9 << 26;
        final int carry10 = h11 >> 25;
        h11 -= carry10 << 25;
        final byte[] s = { (byte)h2, (byte)(h2 >> 8), (byte)(h2 >> 16), (byte)(h2 >> 24 | h3 << 2), (byte)(h3 >> 6), (byte)(h3 >> 14), (byte)(h3 >> 22 | h4 << 3), (byte)(h4 >> 5), (byte)(h4 >> 13), (byte)(h4 >> 21 | h5 << 5), (byte)(h5 >> 3), (byte)(h5 >> 11), (byte)(h5 >> 19 | h6 << 6), (byte)(h6 >> 2), (byte)(h6 >> 10), (byte)(h6 >> 18), (byte)h7, (byte)(h7 >> 8), (byte)(h7 >> 16), (byte)(h7 >> 24 | h8 << 1), (byte)(h8 >> 7), (byte)(h8 >> 15), (byte)(h8 >> 23 | h9 << 3), (byte)(h9 >> 5), (byte)(h9 >> 13), (byte)(h9 >> 21 | h10 << 4), (byte)(h10 >> 4), (byte)(h10 >> 12), (byte)(h10 >> 20 | h11 << 6), (byte)(h11 >> 2), (byte)(h11 >> 10), (byte)(h11 >> 18) };
        return s;
    }
    
    @Override
    public FieldElement decode(final byte[] in) {
        long h0 = load_4(in, 0);
        long h2 = load_3(in, 4) << 6;
        long h3 = load_3(in, 7) << 5;
        long h4 = load_3(in, 10) << 3;
        long h5 = load_3(in, 13) << 2;
        long h6 = load_4(in, 16);
        long h7 = load_3(in, 20) << 7;
        long h8 = load_3(in, 23) << 5;
        long h9 = load_3(in, 26) << 4;
        long h10 = (load_3(in, 29) & 0x7FFFFF) << 2;
        final long carry9 = h10 + 16777216L >> 25;
        h0 += carry9 * 19L;
        h10 -= carry9 << 25;
        final long carry10 = h2 + 16777216L >> 25;
        h3 += carry10;
        h2 -= carry10 << 25;
        final long carry11 = h4 + 16777216L >> 25;
        h5 += carry11;
        h4 -= carry11 << 25;
        final long carry12 = h6 + 16777216L >> 25;
        h7 += carry12;
        h6 -= carry12 << 25;
        final long carry13 = h8 + 16777216L >> 25;
        h9 += carry13;
        h8 -= carry13 << 25;
        final long carry14 = h0 + 33554432L >> 26;
        h2 += carry14;
        h0 -= carry14 << 26;
        final long carry15 = h3 + 33554432L >> 26;
        h4 += carry15;
        h3 -= carry15 << 26;
        final long carry16 = h5 + 33554432L >> 26;
        h6 += carry16;
        h5 -= carry16 << 26;
        final long carry17 = h7 + 33554432L >> 26;
        h8 += carry17;
        h7 -= carry17 << 26;
        final long carry18 = h9 + 33554432L >> 26;
        h10 += carry18;
        h9 -= carry18 << 26;
        final int[] h11 = { (int)h0, (int)h2, (int)h3, (int)h4, (int)h5, (int)h6, (int)h7, (int)h8, (int)h9, (int)h10 };
        return new Ed25519FieldElement(this.f, h11);
    }
    
    @Override
    public boolean isNegative(final FieldElement x) {
        final byte[] s = this.encode(x);
        return (s[0] & 0x1) != 0x0;
    }
}
