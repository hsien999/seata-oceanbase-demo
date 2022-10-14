// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.util;

import java.util.Random;

public class MysqlCommonUtils
{
    static final Random rand;
    
    public static final String long2Ip(final long ip) {
        final long[] mask = { 255L, 65280L, 16711680L, -16777216L };
        final StringBuilder ipAddress = new StringBuilder();
        for (int i = 0; i < mask.length; ++i) {
            ipAddress.insert(0, (ip & mask[i]) >> i * 8);
            if (i < mask.length - 1) {
                ipAddress.insert(0, ".");
            }
        }
        return ipAddress.toString();
    }
    
    public static long bytesToLong(final byte[] bytes) {
        long result = 0L;
        if (null != bytes) {
            for (final byte b : bytes) {
                result = (result << 8 | (long)(b & 0xFF));
            }
        }
        return result;
    }
    
    public static long ip2Long(final String ipAddress) {
        long result = 0L;
        final String[] ipAddressInArray = ipAddress.split("\\.");
        for (int i = 3; i >= 0; --i) {
            final long ip = Long.parseLong(ipAddressInArray[3 - i]);
            result |= ip << i * 8;
        }
        return result;
    }
    
    public static int bytes2Int(final byte[] bytes) {
        int value = 0;
        value = ((bytes[3] & 0xFF) << 24 | (bytes[2] & 0xFF) << 16 | (bytes[1] & 0xFF) << 8 | (bytes[0] & 0xFF));
        return value;
    }
    
    public static int getRandomNum(final int min, final int max) {
        if (min > max) {
            throw new IllegalArgumentException(String.format("min is larger than max, min:%d, max:%d", min, max));
        }
        if (min == max && 0 == max) {
            return 0;
        }
        return MysqlCommonUtils.rand.nextInt(max) % (max - min + 1) + min;
    }
    
    public static int getRandomHalfToFull(final int value) {
        return getRandomNum(value / 2, value);
    }
    
    static {
        rand = new Random(System.currentTimeMillis());
    }
}
