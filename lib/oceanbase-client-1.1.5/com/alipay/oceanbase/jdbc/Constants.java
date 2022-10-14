// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

public class Constants
{
    public static final byte[] EMPTY_BYTE_ARRAY;
    public static final String MILLIS_I18N;
    public static final byte[] SLASH_STAR_SPACE_AS_BYTES;
    public static final byte[] SPACE_STAR_SLASH_SPACE_AS_BYTES;
    
    private Constants() {
    }
    
    static {
        EMPTY_BYTE_ARRAY = new byte[0];
        MILLIS_I18N = Messages.getString("Milliseconds");
        SLASH_STAR_SPACE_AS_BYTES = new byte[] { 47, 42, 32 };
        SPACE_STAR_SLASH_SPACE_AS_BYTES = new byte[] { 32, 42, 47, 32 };
    }
}
