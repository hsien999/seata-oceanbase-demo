// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.input;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import java.io.EOFException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import com.oceanbase.jdbc.internal.util.Utils;
import com.oceanbase.jdbc.internal.io.TraceObject;
import java.util.Arrays;
import java.io.IOException;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import com.oceanbase.jdbc.internal.io.LruTraceCache;
import java.io.InputStream;
import com.oceanbase.jdbc.internal.logging.Logger;

public class DecompressPacketInputStream implements PacketInputStream
{
    private static final int REUSABLE_BUFFER_LENGTH = 1024;
    private static final int MAX_PACKET_SIZE = 16777215;
    private static final Logger logger;
    private final byte[] header;
    private final byte[] reusableArray;
    private final InputStream inputStream;
    private final int maxQuerySizeToLog;
    private byte[] cacheData;
    private int cachePos;
    private int cacheEnd;
    private int packetSeq;
    private int compressPacketSeq;
    private String serverThreadLog;
    private LruTraceCache traceCache;
    private long threadId;
    private boolean enableNetworkStatistics;
    private long timestampAfterRead;
    
    public DecompressPacketInputStream(final InputStream in, final int maxQuerySizeToLog, final long threadId) {
        this.header = new byte[7];
        this.reusableArray = new byte[1024];
        this.cacheData = new byte[0];
        this.serverThreadLog = "";
        this.traceCache = null;
        this.enableNetworkStatistics = false;
        this.timestampAfterRead = 0L;
        this.inputStream = in;
        this.maxQuerySizeToLog = maxQuerySizeToLog;
        this.threadId = threadId;
    }
    
    @Override
    public Buffer getPacket(final boolean reUsable) throws IOException {
        return new Buffer(this.getPacketArray(reUsable));
    }
    
    @Override
    public byte[] getPacketArray(final boolean reUsable) throws IOException {
        final byte[] cachePacket = this.getNextCachePacket();
        if (cachePacket != null) {
            return cachePacket;
        }
        boolean first = true;
        byte[] packet;
        do {
            this.readBlocking(this.header, 7);
            if (first) {
                first = false;
                if (this.enableNetworkStatistics) {
                    this.timestampAfterRead = System.currentTimeMillis();
                }
            }
            final int compressedLength = (this.header[0] & 0xFF) + ((this.header[1] & 0xFF) << 8) + ((this.header[2] & 0xFF) << 16);
            this.compressPacketSeq = (this.header[3] & 0xFF);
            final int decompressedLength = (this.header[4] & 0xFF) + ((this.header[5] & 0xFF) << 8) + ((this.header[6] & 0xFF) << 16);
            byte[] rawBytes;
            if (reUsable && decompressedLength == 0 && compressedLength < 1024) {
                rawBytes = this.reusableArray;
            }
            else {
                rawBytes = new byte[(decompressedLength != 0) ? decompressedLength : compressedLength];
            }
            this.readCompressBlocking(rawBytes, compressedLength, decompressedLength);
            if (this.traceCache != null) {
                final int length = (decompressedLength != 0) ? decompressedLength : compressedLength;
                this.traceCache.put(new TraceObject(false, (decompressedLength == 0) ? 2 : 1, this.threadId, new byte[][] { Arrays.copyOfRange(this.header, 0, 7), Arrays.copyOfRange(rawBytes, 0, (length > 1000) ? 1000 : length) }));
            }
            if (DecompressPacketInputStream.logger.isTraceEnabled()) {
                final int length = (decompressedLength != 0) ? decompressedLength : compressedLength;
                DecompressPacketInputStream.logger.trace("read {} {}{}", (decompressedLength == 0) ? "uncompress" : "compress", this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog - 7, 0, length, new byte[][] { this.header, rawBytes }));
            }
            this.cache(rawBytes, (decompressedLength == 0) ? compressedLength : decompressedLength);
            packet = this.getNextCachePacket();
        } while (packet == null);
        return packet;
    }
    
    private void readCompressBlocking(final byte[] arr, final int compressedLength, final int decompressedLength) throws IOException {
        if (decompressedLength != 0) {
            final byte[] compressedBuffer = new byte[compressedLength];
            this.readBlocking(compressedBuffer, compressedLength);
            final Inflater inflater = new Inflater();
            inflater.setInput(compressedBuffer);
            try {
                final int actualUncompressBytes = inflater.inflate(arr);
                if (actualUncompressBytes != decompressedLength) {
                    throw new IOException("Invalid exception length after decompression " + actualUncompressBytes + ",expected " + decompressedLength);
                }
            }
            catch (DataFormatException dfe) {
                throw new IOException(dfe);
            }
            inflater.end();
        }
        else {
            this.readBlocking(arr, compressedLength);
        }
    }
    
    private void readBlocking(final byte[] arr, final int length) throws IOException {
        int remaining = length;
        int off = 0;
        do {
            final int count = this.inputStream.read(arr, off, remaining);
            if (count < 0) {
                throw new EOFException("unexpected end of stream, read " + (length - remaining) + " bytes from " + length + " (socket was closed by server)");
            }
            remaining -= count;
            off += count;
        } while (remaining > 0);
    }
    
    private void cache(final byte[] rawBytes, final int length) {
        if (this.cachePos >= this.cacheEnd) {
            this.cacheData = rawBytes;
            this.cachePos = 0;
            this.cacheEnd = length;
        }
        else {
            final byte[] newCache = new byte[length + this.cacheEnd - this.cachePos];
            System.arraycopy(this.cacheData, this.cachePos, newCache, 0, this.cacheEnd - this.cachePos);
            System.arraycopy(rawBytes, 0, newCache, this.cacheEnd - this.cachePos, length);
            this.cacheData = newCache;
            this.cachePos = 0;
            this.cacheEnd = newCache.length;
        }
    }
    
    private byte[] getNextCachePacket() {
        int packetOffset = 0;
        while (this.cacheEnd > this.cachePos + 4 + packetOffset * 16777219) {
            int lastPacketLength = (this.cacheData[this.cachePos + packetOffset * 16777219] & 0xFF) + ((this.cacheData[this.cachePos + packetOffset * 16777219 + 1] & 0xFF) << 8) + ((this.cacheData[this.cachePos + packetOffset * 16777219 + 2] & 0xFF) << 16);
            if (lastPacketLength == 16777215) {
                ++packetOffset;
            }
            else {
                if (this.cacheEnd < this.cachePos + 4 + packetOffset * 16777219 + lastPacketLength) {
                    return null;
                }
                if (packetOffset != 0) {
                    final byte[] packet = new byte[lastPacketLength + packetOffset * 16777215];
                    int offset = 0;
                    do {
                        lastPacketLength = (this.cacheData[this.cachePos] & 0xFF) + ((this.cacheData[this.cachePos + 1] & 0xFF) << 8) + ((this.cacheData[this.cachePos + 2] & 0xFF) << 16);
                        this.packetSeq = this.cacheData[this.cachePos + 3];
                        System.arraycopy(this.cacheData, this.cachePos + 4, packet, offset, lastPacketLength);
                        offset += lastPacketLength;
                        if (DecompressPacketInputStream.logger.isTraceEnabled()) {
                            DecompressPacketInputStream.logger.trace("read packet: seq={} len={} {}{}", this.packetSeq, lastPacketLength, this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog, this.cachePos + 4, lastPacketLength, new byte[][] { this.cacheData }));
                        }
                        this.cachePos += 4 + lastPacketLength;
                    } while (lastPacketLength == 16777215);
                    return packet;
                }
                this.packetSeq = this.cacheData[this.cachePos + 3];
                if (this.cacheEnd - (this.cachePos + 4) >= lastPacketLength) {
                    final byte[] packet = new byte[lastPacketLength];
                    System.arraycopy(this.cacheData, this.cachePos + 4, packet, 0, lastPacketLength);
                    if (DecompressPacketInputStream.logger.isTraceEnabled()) {
                        DecompressPacketInputStream.logger.trace("read packet: seq={} len={} {}{}", this.packetSeq, lastPacketLength, this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog, this.cachePos + 4, lastPacketLength, new byte[][] { this.cacheData }));
                    }
                    this.cachePos += 4 + lastPacketLength;
                    return packet;
                }
                continue;
            }
        }
        return null;
    }
    
    @Override
    public int getLastPacketSeq() {
        return this.packetSeq;
    }
    
    @Override
    public int getCompressLastPacketSeq() {
        return this.compressPacketSeq;
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
        logger = LoggerFactory.getLogger(DecompressPacketInputStream.class);
    }
}
