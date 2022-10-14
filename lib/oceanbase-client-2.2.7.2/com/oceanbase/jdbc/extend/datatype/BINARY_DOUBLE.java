// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

import java.math.BigDecimal;
import java.sql.SQLException;

public class BINARY_DOUBLE extends Datum
{
    static final long serialVersionUID = 6718338151847341754L;
    public static final boolean TRACE = false;
    
    public BINARY_DOUBLE() {
    }
    
    public BINARY_DOUBLE(final byte[] var1) {
        super(var1);
    }
    
    public BINARY_DOUBLE(final double var1) {
        super(doubleToCanonicalFormatBytes(var1));
    }
    
    public BINARY_DOUBLE(final Double var1) {
        super(doubleToCanonicalFormatBytes(var1));
    }
    
    public BINARY_DOUBLE(final String var1) throws SQLException {
        this(stringToDouble(var1));
    }
    
    public BINARY_DOUBLE(final Boolean var1) {
        this(((boolean)var1) ? 1 : 0);
    }
    
    @Override
    public Object toJdbc() throws SQLException {
        return new Double(canonicalFormatBytesToDouble(this.getBytes()));
    }
    
    @Override
    public boolean isConvertibleTo(final Class var1) {
        final String var2 = var1.getName();
        return var2.compareTo("java.lang.String") == 0 || var2.compareTo("java.lang.Double") == 0;
    }
    
    @Override
    public String stringValue() {
        final String var1 = Double.toString(canonicalFormatBytesToDouble(this.getBytes()));
        return var1;
    }
    
    @Override
    public double doubleValue() throws SQLException {
        return canonicalFormatBytesToDouble(this.getBytes());
    }
    
    @Override
    public BigDecimal bigDecimalValue() throws SQLException {
        return new BigDecimal(canonicalFormatBytesToDouble(this.getBytes()));
    }
    
    @Override
    public Object makeJdbcArray(final int var1) {
        return new Double[var1];
    }
    
    private static double stringToDouble(final String var0) throws SQLException {
        try {
            return Double.valueOf(var0);
        }
        catch (NumberFormatException var) {
            throw var;
        }
    }
    
    static byte[] doubleToCanonicalFormatBytes(final double var0) {
        double var = var0;
        if (var0 == 0.0) {
            var = 0.0;
        }
        else if (var0 != var0) {
            var = Double.NaN;
        }
        final long var2 = Double.doubleToLongBits(var);
        final byte[] var3 = new byte[8];
        int var4 = (int)var2;
        int var5 = (int)(var2 >> 32);
        int var6 = var4;
        int var7;
        var4 = (var7 = var4 >> 8);
        int var8;
        var4 = (var8 = var4 >> 8);
        int var9;
        var4 = (var9 = var4 >> 8);
        int var10 = var5;
        int var11;
        var5 = (var11 = var5 >> 8);
        int var12;
        var5 = (var12 = var5 >> 8);
        var5 >>= 8;
        int var13;
        if ((var5 & 0x80) == 0x0) {
            var13 = (var5 | 0x80);
        }
        else {
            var13 = ~var5;
            var12 ^= -1;
            var11 ^= -1;
            var10 ^= -1;
            var9 = ~var4;
            var8 ^= -1;
            var7 ^= -1;
            var6 ^= -1;
        }
        var3[7] = (byte)var6;
        var3[6] = (byte)var7;
        var3[5] = (byte)var8;
        var3[4] = (byte)var9;
        var3[3] = (byte)var10;
        var3[2] = (byte)var11;
        var3[1] = (byte)var12;
        var3[0] = (byte)var13;
        return var3;
    }
    
    static double canonicalFormatBytesToDouble(final byte[] var0) {
        final byte var = var0[0];
        final byte var2 = var0[1];
        final byte var3 = var0[2];
        final byte var4 = var0[3];
        final byte var5 = var0[4];
        final byte var6 = var0[5];
        final byte var7 = var0[6];
        final byte var8 = var0[7];
        int var9;
        int var10;
        int var11;
        int var12;
        int var13;
        int var14;
        int var15;
        int var16;
        if ((var & 0x80) != 0x0) {
            var9 = (var & 0x7F);
            var10 = (var2 & 0xFF);
            var11 = (var3 & 0xFF);
            var12 = (var4 & 0xFF);
            var13 = (var5 & 0xFF);
            var14 = (var6 & 0xFF);
            var15 = (var7 & 0xFF);
            var16 = (var8 & 0xFF);
        }
        else {
            var9 = (~var & 0xFF);
            var10 = (~var2 & 0xFF);
            var11 = (~var3 & 0xFF);
            var12 = (~var4 & 0xFF);
            var13 = (~var5 & 0xFF);
            var14 = (~var6 & 0xFF);
            var15 = (~var7 & 0xFF);
            var16 = (~var8 & 0xFF);
        }
        final int var17 = var9 << 24 | var10 << 16 | var11 << 8 | var12;
        final int var18 = var13 << 24 | var14 << 16 | var15 << 8 | var16;
        final long var19 = (long)var17 << 32 | ((long)var18 & 0xFFFFFFFFL);
        return Double.longBitsToDouble(var19);
    }
}
