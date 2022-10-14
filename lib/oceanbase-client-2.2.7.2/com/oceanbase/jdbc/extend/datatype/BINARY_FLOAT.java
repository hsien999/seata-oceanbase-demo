// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

import java.math.BigDecimal;
import java.sql.SQLException;

public class BINARY_FLOAT extends Datum
{
    static final long serialVersionUID = -4231112037190700631L;
    public static final boolean TRACE = false;
    
    public BINARY_FLOAT() {
    }
    
    public BINARY_FLOAT(final byte[] var1) {
        super(var1);
    }
    
    public BINARY_FLOAT(final float var1) {
        super(floatToCanonicalFormatBytes(var1));
    }
    
    public BINARY_FLOAT(final Float var1) {
        super(floatToCanonicalFormatBytes(var1));
    }
    
    public BINARY_FLOAT(final String var1) throws SQLException {
        this(stringToFloat(var1));
    }
    
    public BINARY_FLOAT(final Boolean var1) {
        this((float)(((boolean)var1) ? 1 : 0));
    }
    
    @Override
    public Object toJdbc() throws SQLException {
        return new Float(canonicalFormatBytesToFloat(this.getBytes()));
    }
    
    @Override
    public boolean isConvertibleTo(final Class var1) {
        final String var2 = var1.getName();
        return var2.compareTo("java.lang.String") == 0 || var2.compareTo("java.lang.Float") == 0;
    }
    
    @Override
    public String stringValue() {
        final String var1 = Float.toString(canonicalFormatBytesToFloat(this.getBytes()));
        return var1;
    }
    
    @Override
    public float floatValue() throws SQLException {
        return canonicalFormatBytesToFloat(this.getBytes());
    }
    
    @Override
    public double doubleValue() throws SQLException {
        return this.floatValue();
    }
    
    @Override
    public BigDecimal bigDecimalValue() throws SQLException {
        return new BigDecimal(this.floatValue());
    }
    
    private static float stringToFloat(final String var0) throws SQLException {
        try {
            return Float.valueOf(var0);
        }
        catch (NumberFormatException e) {
            throw e;
        }
    }
    
    @Override
    public Object makeJdbcArray(final int var1) {
        return new Float[var1];
    }
    
    static byte[] floatToCanonicalFormatBytes(final float var0) {
        float var = var0;
        if (var0 == 0.0f) {
            var = 0.0f;
        }
        else if (var0 != var0) {
            var = Float.NaN;
        }
        int var2 = Float.floatToIntBits(var);
        final byte[] var3 = new byte[4];
        int var4 = var2;
        int var5;
        var2 = (var5 = var2 >> 8);
        int var6;
        var2 = (var6 = var2 >> 8);
        var2 >>= 8;
        int var7;
        if ((var2 & 0x80) == 0x0) {
            var7 = (var2 | 0x80);
        }
        else {
            var7 = ~var2;
            var6 ^= -1;
            var5 ^= -1;
            var4 ^= -1;
        }
        var3[3] = (byte)var4;
        var3[2] = (byte)var5;
        var3[1] = (byte)var6;
        var3[0] = (byte)var7;
        return var3;
    }
    
    static float canonicalFormatBytesToFloat(final byte[] var0) {
        final byte var = var0[0];
        final byte var2 = var0[1];
        final byte var3 = var0[2];
        final byte var4 = var0[3];
        int var5;
        int var6;
        int var7;
        int var8;
        if ((var & 0x80) != 0x0) {
            var5 = (var & 0x7F);
            var6 = (var2 & 0xFF);
            var7 = (var3 & 0xFF);
            var8 = (var4 & 0xFF);
        }
        else {
            var5 = (~var & 0xFF);
            var6 = (~var2 & 0xFF);
            var7 = (~var3 & 0xFF);
            var8 = (~var4 & 0xFF);
        }
        final int var9 = var5 << 24 | var6 << 16 | var7 << 8 | var8;
        return Float.intBitsToFloat(var9);
    }
}
