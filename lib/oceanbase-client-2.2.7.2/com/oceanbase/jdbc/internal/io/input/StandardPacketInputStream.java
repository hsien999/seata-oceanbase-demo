// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.input;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import com.oceanbase.jdbc.internal.util.Utils;
import com.oceanbase.jdbc.internal.io.TraceObject;
import java.util.Arrays;
import java.io.EOFException;
import java.io.IOException;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import com.oceanbase.jdbc.internal.ColumnType;
import java.io.BufferedInputStream;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.io.LruTraceCache;
import java.io.InputStream;
import com.oceanbase.jdbc.internal.logging.Logger;

public class StandardPacketInputStream implements PacketInputStream
{
    private static final int REUSABLE_BUFFER_LENGTH = 1024;
    private static final int MAX_PACKET_SIZE = 16777215;
    private static final Logger logger;
    private final byte[] header;
    private final byte[] reusableArray;
    private final InputStream inputStream;
    private final int maxQuerySizeToLog;
    private int packetSeq;
    private int lastPacketLength;
    private String serverThreadLog;
    private long threadId;
    private LruTraceCache traceCache;
    private boolean enableNetworkStatistics;
    private boolean firstRead;
    private long timestampAfterRead;
    
    public StandardPacketInputStream(final InputStream in, final Options options, final long threadId) {
        this.header = new byte[4];
        this.reusableArray = new byte[1024];
        this.serverThreadLog = "";
        this.traceCache = null;
        this.enableNetworkStatistics = false;
        this.firstRead = false;
        this.timestampAfterRead = 0L;
        this.inputStream = (options.useReadAheadInput ? new ReadAheadBufferedStream(in) : new BufferedInputStream(in, 16384));
        this.maxQuerySizeToLog = options.maxQuerySizeToLog;
        this.threadId = threadId;
    }
    
    public static byte[] create(final byte[] value) {
        if (value == null) {
            return new byte[] { -5 };
        }
        final int length = value.length;
        if (length < 251) {
            final byte[] buf = new byte[length + 1];
            buf[0] = (byte)length;
            System.arraycopy(value, 0, buf, 1, length);
            return buf;
        }
        if (length < 65536) {
            final byte[] buf = new byte[length + 3];
            buf[0] = -4;
            buf[1] = (byte)length;
            buf[2] = (byte)(length >>> 8);
            System.arraycopy(value, 0, buf, 3, length);
            return buf;
        }
        if (length < 16777216) {
            final byte[] buf = new byte[length + 4];
            buf[0] = -3;
            buf[1] = (byte)length;
            buf[2] = (byte)(length >>> 8);
            buf[3] = (byte)(length >>> 16);
            System.arraycopy(value, 0, buf, 4, length);
            return buf;
        }
        final byte[] buf = new byte[length + 9];
        buf[0] = -2;
        buf[1] = (byte)length;
        buf[2] = (byte)(length >>> 8);
        buf[3] = (byte)(length >>> 16);
        buf[4] = (byte)(length >>> 24);
        System.arraycopy(value, 0, buf, 9, length);
        return buf;
    }
    
    public static byte[] create(final byte[][] row, final ColumnType[] columnTypes) {
        int totalLength = 0;
        for (final byte[] data : row) {
            if (data == null) {
                ++totalLength;
            }
            else {
                final int length = data.length;
                if (length < 251) {
                    totalLength += length + 1;
                }
                else if (length < 65536) {
                    totalLength += length + 3;
                }
                else if (length < 16777216) {
                    totalLength += length + 4;
                }
                else {
                    totalLength += length + 9;
                }
            }
        }
        final byte[] buf = new byte[totalLength];
        int pos = 0;
        for (final byte[] data2 : row) {
            if (data2 == null) {
                buf[pos++] = -5;
            }
            else {
                final int length2 = data2.length;
                if (length2 < 251) {
                    buf[pos++] = (byte)length2;
                }
                else if (length2 < 65536) {
                    buf[pos++] = -4;
                    buf[pos++] = (byte)length2;
                    buf[pos++] = (byte)(length2 >>> 8);
                }
                else if (length2 < 16777216) {
                    buf[pos++] = -3;
                    buf[pos++] = (byte)length2;
                    buf[pos++] = (byte)(length2 >>> 8);
                    buf[pos++] = (byte)(length2 >>> 16);
                }
                else {
                    buf[pos++] = -2;
                    buf[pos++] = (byte)length2;
                    buf[pos++] = (byte)(length2 >>> 8);
                    buf[pos++] = (byte)(length2 >>> 16);
                    buf[pos++] = (byte)(length2 >>> 24);
                    pos += 4;
                }
                System.arraycopy(data2, 0, buf, pos, length2);
                pos += length2;
            }
        }
        return buf;
    }
    
    @Override
    public Buffer getPacket(final boolean reUsable) throws IOException {
        return new Buffer(this.getPacketArray(reUsable), this.lastPacketLength);
    }
    
    public InputStream getInputStream() {
        return this.inputStream;
    }
    
    @Override
    public byte[] getPacketArray(final boolean reUsable) throws IOException {
        int remaining = 4;
        int off = 0;
        boolean first = true;
        do {
            final int count = this.inputStream.read(this.header, off, remaining);
            if (count < 0) {
                throw new EOFException("unexpected end of stream, read " + off + " bytes from 4 (socket was closed by server)");
            }
            if (first) {
                first = false;
                if (this.enableNetworkStatistics) {
                    this.timestampAfterRead = System.currentTimeMillis();
                }
            }
            remaining -= count;
            off += count;
        } while (remaining > 0);
        this.lastPacketLength = (this.header[0] & 0xFF) + ((this.header[1] & 0xFF) << 8) + ((this.header[2] & 0xFF) << 16);
        this.packetSeq = this.header[3];
        byte[] rawBytes;
        if (reUsable && this.lastPacketLength < 1024) {
            rawBytes = this.reusableArray;
        }
        else {
            rawBytes = new byte[this.lastPacketLength];
        }
        remaining = this.lastPacketLength;
        off = 0;
        do {
            final int count2 = this.inputStream.read(rawBytes, off, remaining);
            if (count2 < 0) {
                throw new EOFException("unexpected end of stream, read " + (this.lastPacketLength - remaining) + " bytes from " + this.lastPacketLength + " (socket was closed by server)");
            }
            remaining -= count2;
            off += count2;
        } while (remaining > 0);
        if (this.traceCache != null) {
            this.traceCache.put(new TraceObject(false, 0, this.threadId, new byte[][] { Arrays.copyOfRange(this.header, 0, 4), Arrays.copyOfRange(rawBytes, 0, (off > 1000) ? 1000 : off) }));
        }
        if (StandardPacketInputStream.logger.isTraceEnabled()) {
            StandardPacketInputStream.logger.trace("read: {}{}", this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog - 4, 0, this.lastPacketLength, new byte[][] { this.header, rawBytes }));
        }
        if (this.lastPacketLength == 16777215) {
            int packetLength;
            do {
                remaining = 4;
                off = 0;
                do {
                    final int count3 = this.inputStream.read(this.header, off, remaining);
                    if (count3 < 0) {
                        throw new EOFException("unexpected end of stream, read " + off + " bytes from 4");
                    }
                    remaining -= count3;
                    off += count3;
                } while (remaining > 0);
                packetLength = (this.header[0] & 0xFF) + ((this.header[1] & 0xFF) << 8) + ((this.header[2] & 0xFF) << 16);
                this.packetSeq = this.header[3];
                final int currentBufferLength = rawBytes.length;
                final byte[] newRawBytes = new byte[currentBufferLength + packetLength];
                System.arraycopy(rawBytes, 0, newRawBytes, 0, currentBufferLength);
                rawBytes = newRawBytes;
                remaining = packetLength;
                off = currentBufferLength;
                do {
                    final int count4 = this.inputStream.read(rawBytes, off, remaining);
                    if (count4 < 0) {
                        throw new EOFException("unexpected end of stream, read " + (packetLength - remaining) + " bytes from " + packetLength);
                    }
                    remaining -= count4;
                    off += count4;
                } while (remaining > 0);
                if (this.traceCache != null) {
                    this.traceCache.put(new TraceObject(false, 0, this.threadId, new byte[][] { Arrays.copyOfRange(this.header, 0, 4), Arrays.copyOfRange(rawBytes, 0, (off > 1000) ? 1000 : off) }));
                }
                if (StandardPacketInputStream.logger.isTraceEnabled()) {
                    StandardPacketInputStream.logger.trace("read: {}{}", this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog - 4, currentBufferLength, packetLength, new byte[][] { this.header, rawBytes }));
                }
                this.lastPacketLength += packetLength;
            } while (packetLength == 16777215);
        }
        return rawBytes;
    }
    
    @Override
    public int getLastPacketSeq() {
        return this.packetSeq;
    }
    
    @Override
    public int getCompressLastPacketSeq() {
        return 0;
    }
    
    @Override
    public void close() throws IOException {
        this.inputStream.close();
    }
    
    @Override
    public void setServerThreadId(final long serverThreadId, final Boolean isMaster) {
        this.serverThreadLog = "conn=" + serverThreadId + ((isMaster != null) ? ("(" + (isMaster ? "M" : "S") + ")") : "");
    }
    
    @Override
    public void setTraceCache(final LruTraceCache traceCache) {
        this.traceCache = traceCache;
    }
    
    @Override
    public void enableNetworkStatistics(final boolean flag) {
        this.timestampAfterRead = 0L;
        this.enableNetworkStatistics = flag;
    }
    
    @Override
    public long getTimestampAfterRead() {
        return this.timestampAfterRead;
    }
    
    @Override
    public void clearNetworkStatistics() {
        this.timestampAfterRead = 0L;
    }
    
    static {
        logger = LoggerFactory.getLogger(StandardPacketInputStream.class);
    }
}
