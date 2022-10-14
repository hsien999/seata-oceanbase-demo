// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.protocol;

public class ObProtocolV20
{
    public static final int OB20_PROTOCOL_MAGIC_NUM = 8363;
    public static final int COMPRESS_HEADER_LENGTH = 7;
    public static final int OB20_PROTOCOL_HEADER_LENGTH = 24;
    public static final int OB20_PROTOCOL_TAILER_LENGTH = 4;
    public static final int OB20_PROTOCOL_EXTRA_INFO_LENGTH = 4;
    public static final int OB20_TOTAOL_HEADER_LENGTH = 31;
    public static final int OB20_PROTOCOL_HEADER_TALER_LENGTH = 28;
    public static final int OB20_PROTOCOL_META_LENGTH = 35;
    public static final int OB20_PROTOCOL_VERSION_VALUE = 20;
    public static final long OB20_EXTRA_INFO_EXIST = 1L;
    public static final long OB20_LAST_PACKET_FLAG = 2L;
}
