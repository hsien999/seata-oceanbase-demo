// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math;

import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.Utils;

final class Constants
{
    public static final byte[] ZERO;
    public static final byte[] ONE;
    public static final byte[] TWO;
    public static final byte[] FOUR;
    public static final byte[] FIVE;
    public static final byte[] EIGHT;
    
    static {
        ZERO = Utils.hexToBytes("0000000000000000000000000000000000000000000000000000000000000000");
        ONE = Utils.hexToBytes("0100000000000000000000000000000000000000000000000000000000000000");
        TWO = Utils.hexToBytes("0200000000000000000000000000000000000000000000000000000000000000");
        FOUR = Utils.hexToBytes("0400000000000000000000000000000000000000000000000000000000000000");
        FIVE = Utils.hexToBytes("0500000000000000000000000000000000000000000000000000000000000000");
        EIGHT = Utils.hexToBytes("0800000000000000000000000000000000000000000000000000000000000000");
    }
}
