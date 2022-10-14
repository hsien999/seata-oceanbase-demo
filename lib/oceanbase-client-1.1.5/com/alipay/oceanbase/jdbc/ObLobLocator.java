// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

class ObLobLocator
{
    protected long magicCode;
    protected long version;
    protected long snapshotVersion;
    protected byte[] tableId;
    protected long columnId;
    protected int flags;
    protected int option;
    protected long payloadOffset;
    protected long payloadSize;
    protected byte[] rowId;
    protected byte[] binaryData;
    public static int OB_LOG_LOCATOR_HEADER;
    protected volatile MySQLConnection connection;
    
    static {
        ObLobLocator.OB_LOG_LOCATOR_HEADER = 40;
    }
}
