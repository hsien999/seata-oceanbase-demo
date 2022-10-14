// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal;

public class OceanBaseServerCapabilities
{
    public static final int CLIENT_MYSQL = 1;
    public static final int FOUND_ROWS = 2;
    public static final int LONG_FLAG = 4;
    public static final int CONNECT_WITH_DB = 8;
    public static final int NO_SCHEMA = 16;
    public static final int COMPRESS = 32;
    public static final int ODBC = 64;
    public static final int LOCAL_FILES = 128;
    public static final int IGNORE_SPACE = 256;
    public static final int CLIENT_PROTOCOL_41 = 512;
    public static final int CLIENT_INTERACTIVE = 1024;
    public static final int SSL = 2048;
    public static final int IGNORE_SIGPIPE = 4096;
    public static final int TRANSACTIONS = 8192;
    public static final int RESERVED = 16384;
    public static final int SECURE_CONNECTION = 32768;
    public static final int MULTI_STATEMENTS = 65536;
    public static final int MULTI_RESULTS = 131072;
    public static final int PS_MULTI_RESULTS = 262144;
    public static final int PLUGIN_AUTH = 524288;
    public static final int CONNECT_ATTRS = 1048576;
    public static final int PLUGIN_AUTH_LENENC_CLIENT_DATA = 2097152;
    public static final int CLIENT_SESSION_TRACK = 8388608;
    public static final int CLIENT_DEPRECATE_EOF = 16777216;
    public static final int CLIENT_SUPPORT_ORACLE = 134217728;
    public static final int CLIENT_SUPPORT_LOB_LOCATOR = 536870912;
    public static final long MARIADB_CLIENT_PROGRESS = 4294967296L;
    public static final long MARIADB_CLIENT_COM_MULTI = 8589934592L;
}
