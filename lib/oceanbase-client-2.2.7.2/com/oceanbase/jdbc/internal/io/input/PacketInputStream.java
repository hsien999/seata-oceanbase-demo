// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.input;

import com.oceanbase.jdbc.internal.io.LruTraceCache;
import java.io.IOException;
import com.oceanbase.jdbc.internal.com.read.Buffer;

public interface PacketInputStream
{
    Buffer getPacket(final boolean p0) throws IOException;
    
    byte[] getPacketArray(final boolean p0) throws IOException;
    
    int getLastPacketSeq();
    
    int getCompressLastPacketSeq();
    
    void close() throws IOException;
    
    void setServerThreadId(final long p0, final Boolean p1);
    
    void setTraceCache(final LruTraceCache p0);
    
    void enableNetworkStatistics(final boolean p0);
    
    long getTimestampAfterRead();
    
    void clearNetworkStatistics();
}
