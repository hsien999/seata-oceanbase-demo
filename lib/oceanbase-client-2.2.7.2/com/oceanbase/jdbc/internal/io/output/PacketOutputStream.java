// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.output;

import java.nio.charset.Charset;
import java.util.TimeZone;
import com.oceanbase.jdbc.internal.util.exceptions.MaxAllowedPacketException;
import com.oceanbase.jdbc.internal.io.LruTraceCache;
import java.io.OutputStream;
import java.io.Reader;
import java.io.InputStream;
import java.io.IOException;

public interface PacketOutputStream
{
    void startPacket(final int p0);
    
    void writeEmptyPacket(final int p0) throws IOException;
    
    void writeEmptyPacket() throws IOException;
    
    void write(final int p0) throws IOException;
    
    void write(final byte[] p0) throws IOException;
    
    void write(final byte[] p0, final int p1, final int p2) throws IOException;
    
    void write(final String p0) throws IOException;
    
    void write(final String p0, final boolean p1, final boolean p2) throws IOException;
    
    void write(final InputStream p0, final boolean p1, final boolean p2) throws IOException;
    
    void write(final InputStream p0, final long p1, final boolean p2, final boolean p3) throws IOException;
    
    void writeHex(final InputStream p0, final boolean p1, final boolean p2) throws IOException;
    
    void writeHex(final InputStream p0, final long p1, final boolean p2, final boolean p3) throws IOException;
    
    void write(final Reader p0, final boolean p1, final boolean p2) throws IOException;
    
    void write(final Reader p0, final long p1, final boolean p2, final boolean p3) throws IOException;
    
    void writeEscapeQuote(final Reader p0, final boolean p1) throws IOException;
    
    void writeEscapeQuote(final Reader p0, final long p1, final boolean p2) throws IOException;
    
    void writeBytesEscaped(final byte[] p0, final int p1, final boolean p2) throws IOException;
    
    void writeBytesEscapedQuote(final byte[] p0, final int p1, final boolean p2) throws IOException;
    
    void flush() throws IOException;
    
    void close() throws IOException;
    
    boolean checkRemainingSize(final int p0);
    
    boolean exceedMaxLength();
    
    OutputStream getOutputStream();
    
    void writeShort(final short p0) throws IOException;
    
    void writeInt(final int p0) throws IOException;
    
    void writeLong(final long p0) throws IOException;
    
    void writeBytes(final byte p0, final int p1) throws IOException;
    
    void writeFieldLength(final long p0) throws IOException;
    
    int getMaxAllowedPacket();
    
    void setMaxAllowedPacket(final int p0);
    
    void permitTrace(final boolean p0);
    
    void setServerThreadId(final long p0, final Boolean p1);
    
    void setTraceCache(final LruTraceCache p0);
    
    void mark() throws MaxAllowedPacketException;
    
    boolean isMarked();
    
    void flushBufferStopAtMark() throws IOException;
    
    boolean bufferIsDataAfterMark();
    
    byte[] resetMark();
    
    int initialPacketPos();
    
    void checkMaxAllowedLength(final int p0) throws MaxAllowedPacketException;
    
    void setPosition(final int p0);
    
    int getPosition();
    
    void setTimeZone(final TimeZone p0);
    
    TimeZone getTimeZone();
    
    void writeIntV1(final int p0) throws IOException;
    
    void writeLongV1(final long p0) throws IOException;
    
    void writeFloatV1(final float p0) throws IOException;
    
    void enableNetworkStatistics(final boolean p0);
    
    long getTimestampBeforeFlush();
    
    void clearNetworkStatistics();
    
    Charset getCharset();
}
