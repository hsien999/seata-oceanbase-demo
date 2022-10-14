// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.constant;

public class ColumnFlags
{
    public static final short NOT_NULL = 1;
    public static final short PRIMARY_KEY = 2;
    public static final short UNIQUE_KEY = 4;
    public static final short MULTIPLE_KEY = 8;
    public static final short BLOB = 16;
    public static final short UNSIGNED = 32;
    public static final short DECIMAL = 64;
    public static final short BINARY_COLLATION = 128;
    public static final short ENUM = 256;
    public static final short AUTO_INCREMENT = 512;
    public static final short TIMESTAMP = 1024;
    public static final short SET = 2048;
}
