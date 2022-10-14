// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com;

public class Packet
{
    public static final byte ERROR = -1;
    public static final byte OK = 0;
    public static final byte EOF = -2;
    public static final byte LOCAL_INFILE = -5;
    public static final byte COM_QUIT = 1;
    public static final byte COM_INIT_DB = 2;
    public static final byte COM_QUERY = 3;
    public static final byte COM_PING = 14;
    public static final byte COM_STMT_PREPARE = 22;
    public static final byte COM_STMT_EXECUTE = 23;
    public static final byte COM_STMT_PREPARE_EXECUTE = -95;
    public static final byte COM_STMT_FETCH = 28;
    public static final byte COM_STMT_SEND_LONG_DATA = 24;
    public static final byte COM_STMT_CLOSE = 25;
    public static final byte COM_RESET_CONNECTION = 31;
    public static final byte COM_STMT_BULK_EXECUTE = -6;
    public static final byte COM_MULTI = -2;
    public static final byte COM_CHANGE_USER = 17;
    public static final byte COM_STMT_SEND_PIECE_DATA = -94;
    public static final byte COM_STMT_GET_PIECE_DATA = -93;
    public static final byte CURSOR_TYPE_NO_CURSOR = 0;
    public static final byte CURSOR_TYPE_READ_ONLY = 1;
    public static final byte CURSOR_TYPE_FOR_UPDATE = 2;
    public static final byte CURSOR_TYPE_SCROLLABLE = 4;
    public static final int OCI_BATCH_MODE = 1;
    public static final int OCI_EXACT_FETCH = 2;
    public static final int OCI_STMT_SCROLLABLE_READONLY = 8;
    public static final int OCI_DESCRIBE_ONLY = 16;
    public static final int OCI_COMMIT_ON_SUCCESS = 32;
    public static final int OCI_NON_BLOCKING = 64;
    public static final int OCI_BATCH_ERRORS = 128;
    public static final int OCI_PARSE_ONLY = 256;
    public static final int OCI_EXACT_FETCH_RESERVED_1 = 512;
    public static final int OCI_SHOW_DML_WARNINGS = 1024;
    public static final int OCI_EXEC_RESERVED_2 = 2048;
    public static final int OCI_DESC_RESERVED_1 = 4096;
    public static final int OCI_EXEC_RESERVED_3 = 8192;
    public static final int OCI_EXEC_RESERVED_4 = 16384;
    public static final int OCI_EXEC_RESERVED_5 = 32768;
    public static final int OCI_EXEC_RESERVED_6 = 65536;
    public static final int OCI_RESULT_CACHE = 131072;
    public static final int OCI_NO_RESULT_CACHE = 262144;
    public static final int OCI_EXEC_RESERVED_7 = 524288;
    public static final int OCI_RETURN_ROW_COUNT_ARRAY = 1048576;
    public static final byte OCI_ONE_PIECE = 0;
    public static final byte OCI_FIRST_PIECE = 1;
    public static final byte OCI_NEXT_PIECE = 2;
    public static final byte OCI_LAST_PIECE = 3;
}
