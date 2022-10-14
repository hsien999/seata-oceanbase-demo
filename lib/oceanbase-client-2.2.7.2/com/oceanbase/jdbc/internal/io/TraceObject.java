// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io;

public class TraceObject
{
    public static final int NOT_COMPRESSED = 0;
    public static final int COMPRESSED_PROTOCOL_COMPRESSED_PACKET = 1;
    public static final int COMPRESSED_PROTOCOL_NOT_COMPRESSED_PACKET = 2;
    private final boolean send;
    private final int indicatorFlag;
    private final long threadId;
    private byte[][] buf;
    
    public TraceObject(final boolean send, final int indicatorFlag, final long threadId, final byte[]... buf) {
        this.send = send;
        this.indicatorFlag = indicatorFlag;
        this.buf = buf;
        this.threadId = threadId;
    }
    
    public void remove() {
        for (int i = 0; i < this.buf.length; ++i) {
            this.buf[i] = null;
        }
        this.buf = null;
    }
    
    public boolean isSend() {
        return this.send;
    }
    
    public int getIndicatorFlag() {
        return this.indicatorFlag;
    }
    
    public byte[][] getBuf() {
        return this.buf;
    }
    
    public long getThreadId() {
        return this.threadId;
    }
}
