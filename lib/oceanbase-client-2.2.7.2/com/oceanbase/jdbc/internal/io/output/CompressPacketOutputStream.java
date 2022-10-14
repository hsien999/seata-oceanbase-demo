// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.output;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import java.io.IOException;
import com.oceanbase.jdbc.internal.util.Utils;
import com.oceanbase.jdbc.internal.io.TraceObject;
import java.util.Arrays;
import java.util.zip.DeflaterOutputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.io.OutputStream;
import com.oceanbase.jdbc.internal.logging.Logger;

public class CompressPacketOutputStream extends AbstractPacketOutputStream
{
    private static final Logger logger;
    private static final int MAX_PACKET_LENGTH = 16777215;
    private static final byte[] EMPTY_ARRAY;
    private static final int MIN_COMPRESSION_SIZE = 100;
    private static final float MIN_COMPRESSION_RATIO = 0.9f;
    private final byte[] header;
    private final byte[] subHeader;
    private int maxPacketLength;
    private int compressSeqNo;
    private byte[] remainingData;
    private boolean lastPacketExactMaxPacketLength;
    private long timestampBeforeFlush;
    
    public CompressPacketOutputStream(final OutputStream out, final int maxQuerySizeToLog, final long threadId, final String encoding) {
        super(out, maxQuerySizeToLog, threadId, encoding);
        this.header = new byte[7];
        this.subHeader = new byte[4];
        this.maxPacketLength = 16777215;
        this.remainingData = new byte[0];
        this.lastPacketExactMaxPacketLength = false;
        this.timestampBeforeFlush = 0L;
    }
    
    @Override
    public int getMaxPacketLength() {
        return this.maxPacketLength;
    }
    
    @Override
    public void setMaxAllowedPacket(final int maxAllowedPacket) {
        this.maxAllowedPacket = maxAllowedPacket;
        this.maxPacketLength = Math.min(16777215, maxAllowedPacket + 7);
    }
    
    @Override
    public void startPacket(final int compressSeqNo) {
        this.compressSeqNo = compressSeqNo;
        this.seqNo = 0;
        this.pos = 0;
        this.cmdLength = 0L;
        this.remainingData = CompressPacketOutputStream.EMPTY_ARRAY;
        this.lastPacketExactMaxPacketLength = false;
    }
    
    @Override
    public int initialPacketPos() {
        return 0;
    }
    
    @Override
    public void enableNetworkStatistics(final boolean flag) {
        this.timestampBeforeFlush = 0L;
        this.enableNetworkStatistics = flag;
    }
    
    @Override
    public long getTimestampBeforeFlush() {
        return this.timestampBeforeFlush;
    }
    
    @Override
    public void clearNetworkStatistics() {
        this.timestampBeforeFlush = 0L;
    }
    
    @Override
    public Charset getCharset() {
        return this.charset;
    }
    
    @Override
    protected void flushBuffer(final boolean commandEnd) throws IOException {
        if (this.enableNetworkStatistics) {
            this.timestampBeforeFlush = System.currentTimeMillis();
        }
        if (this.pos > 0) {
            if (this.pos + this.remainingData.length > 100) {
                final int uncompressSize = Math.min(16777215, this.remainingData.length + 4 + this.pos);
                this.checkMaxAllowedLength(uncompressSize);
                try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    try (final DeflaterOutputStream deflater = new DeflaterOutputStream(baos)) {
                        if (this.remainingData.length != 0) {
                            deflater.write(this.remainingData);
                        }
                        this.subHeader[0] = (byte)this.pos;
                        this.subHeader[1] = (byte)(this.pos >>> 8);
                        this.subHeader[2] = (byte)(this.pos >>> 16);
                        this.subHeader[3] = (byte)(this.seqNo++);
                        deflater.write(this.subHeader, 0, 4);
                        deflater.write(this.buf, 0, uncompressSize - (this.remainingData.length + 4));
                        deflater.finish();
                    }
                    final byte[] compressedBytes = baos.toByteArray();
                    if (compressedBytes.length < (int)(0.9f * this.pos)) {
                        final int compressedLength = compressedBytes.length;
                        this.header[0] = (byte)compressedLength;
                        this.header[1] = (byte)(compressedLength >>> 8);
                        this.header[2] = (byte)(compressedLength >>> 16);
                        this.header[3] = (byte)(this.compressSeqNo++);
                        this.header[4] = (byte)uncompressSize;
                        this.header[5] = (byte)(uncompressSize >>> 8);
                        this.header[6] = (byte)(uncompressSize >>> 16);
                        this.out.write(this.header, 0, 7);
                        this.out.write(compressedBytes, 0, compressedLength);
                        this.cmdLength += uncompressSize;
                        if (this.traceCache != null) {
                            if (this.remainingData.length != 0) {
                                this.traceCache.put(new TraceObject(true, 1, this.threadId, new byte[][] { Arrays.copyOfRange(this.header, 0, 7), Arrays.copyOfRange(this.remainingData, 0, this.remainingData.length), Arrays.copyOfRange(this.subHeader, 0, 4), Arrays.copyOfRange(this.buf, 0, ((uncompressSize > 1000) ? 1000 : uncompressSize) - (this.remainingData.length + 4)) }));
                            }
                            else {
                                this.traceCache.put(new TraceObject(true, 1, this.threadId, new byte[][] { Arrays.copyOfRange(this.header, 0, 7), Arrays.copyOfRange(this.subHeader, 0, 4), Arrays.copyOfRange(this.buf, 0, ((uncompressSize > 1000) ? 1000 : uncompressSize) - (this.remainingData.length + 4)) }));
                            }
                        }
                        if (CompressPacketOutputStream.logger.isTraceEnabled()) {
                            if (this.remainingData.length != 0) {
                                CompressPacketOutputStream.logger.trace("send compress: {}{}", this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog - (this.remainingData.length + 11), 0, compressedLength, new byte[][] { this.header, this.remainingData, this.subHeader, this.buf }));
                            }
                            else {
                                CompressPacketOutputStream.logger.trace("send compress: {}{}", this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog - 11, 0, compressedLength, new byte[][] { this.header, this.subHeader, this.buf }));
                            }
                        }
                        if (this.pos + this.remainingData.length + 4 - uncompressSize > 0) {
                            this.remainingData = Arrays.copyOfRange(this.buf, uncompressSize - (this.remainingData.length + 4), this.pos);
                        }
                        else {
                            this.remainingData = CompressPacketOutputStream.EMPTY_ARRAY;
                        }
                        this.lastPacketExactMaxPacketLength = (this.pos == 16777215);
                        if (commandEnd && this.lastPacketExactMaxPacketLength) {
                            this.writeEmptyPacket();
                        }
                        this.pos = 0;
                        return;
                    }
                }
            }
            final int uncompressSize2 = Math.min(16777215, this.remainingData.length + 4 + this.pos);
            this.checkMaxAllowedLength(uncompressSize2);
            this.cmdLength += uncompressSize2;
            this.header[0] = (byte)uncompressSize2;
            this.header[1] = (byte)(uncompressSize2 >>> 8);
            this.header[2] = (byte)(uncompressSize2 >>> 16);
            this.header[3] = (byte)(this.compressSeqNo++);
            this.header[4] = 0;
            this.header[5] = 0;
            this.header[6] = 0;
            this.out.write(this.header, 0, 7);
            this.cmdLength += uncompressSize2;
            if (this.remainingData.length != 0) {
                this.out.write(this.remainingData);
            }
            this.subHeader[0] = (byte)this.pos;
            this.subHeader[1] = (byte)(this.pos >>> 8);
            this.subHeader[2] = (byte)(this.pos >>> 16);
            this.subHeader[3] = (byte)(this.seqNo++);
            this.out.write(this.subHeader, 0, 4);
            this.out.write(this.buf, 0, uncompressSize2 - (this.remainingData.length + 4));
            this.cmdLength += this.remainingData.length;
            if (this.traceCache != null) {
                if (this.remainingData.length != 0) {
                    this.traceCache.put(new TraceObject(true, 2, this.threadId, new byte[][] { Arrays.copyOfRange(this.header, 0, 7), Arrays.copyOfRange(this.remainingData, 0, this.remainingData.length), Arrays.copyOfRange(this.subHeader, 0, 4), Arrays.copyOfRange(this.buf, 0, ((uncompressSize2 > 1000) ? 1000 : uncompressSize2) - (this.remainingData.length + 4)) }));
                }
                else {
                    this.traceCache.put(new TraceObject(true, 2, this.threadId, new byte[][] { Arrays.copyOfRange(this.header, 0, 7), Arrays.copyOfRange(this.subHeader, 0, 4), Arrays.copyOfRange(this.buf, 0, ((uncompressSize2 > 1000) ? 1000 : uncompressSize2) - (this.remainingData.length + 4)) }));
                }
            }
            if (CompressPacketOutputStream.logger.isTraceEnabled()) {
                if (this.remainingData.length != 0) {
                    CompressPacketOutputStream.logger.trace("send uncompress: {}{}", this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog - (this.remainingData.length + 11), 0, this.pos, new byte[][] { this.header, this.remainingData, this.subHeader, this.buf }));
                }
                else {
                    CompressPacketOutputStream.logger.trace("send uncompress: {}{}", this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog - 11, 0, this.pos, new byte[][] { this.header, this.subHeader, this.buf }));
                }
            }
            if (this.pos + this.remainingData.length + 4 - uncompressSize2 > 0) {
                this.remainingData = Arrays.copyOfRange(this.buf, uncompressSize2 - (this.remainingData.length + 4), this.pos);
            }
            else {
                this.remainingData = CompressPacketOutputStream.EMPTY_ARRAY;
            }
            this.lastPacketExactMaxPacketLength = (this.pos == 16777215);
            this.pos = 0;
        }
        if (this.remainingData.length > 0) {
            if (this.remainingData.length > 100) {
                final int uncompressSize = Math.min(16777215, this.remainingData.length);
                this.checkMaxAllowedLength(uncompressSize);
                this.cmdLength += uncompressSize;
                byte[] compressedBytes;
                try (final ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    try (final DeflaterOutputStream deflater = new DeflaterOutputStream(baos)) {
                        deflater.write(this.remainingData);
                        deflater.finish();
                    }
                    compressedBytes = baos.toByteArray();
                    this.remainingData = CompressPacketOutputStream.EMPTY_ARRAY;
                }
                if (compressedBytes.length < (int)(0.9f * this.pos)) {
                    final int compressedLength2 = compressedBytes.length;
                    this.header[0] = (byte)compressedLength2;
                    this.header[1] = (byte)(compressedLength2 >>> 8);
                    this.header[2] = (byte)(compressedLength2 >>> 16);
                    this.header[3] = (byte)(this.compressSeqNo++);
                    this.header[4] = (byte)uncompressSize;
                    this.header[5] = (byte)(uncompressSize >>> 8);
                    this.header[6] = (byte)(uncompressSize >>> 16);
                    this.out.write(this.header, 0, 7);
                    this.out.write(compressedBytes, 0, compressedLength2);
                    if (this.traceCache != null) {
                        this.traceCache.put(new TraceObject(true, 1, this.threadId, new byte[][] { Arrays.copyOfRange(this.header, 0, 7), Arrays.copyOfRange(this.remainingData, 0, (uncompressSize > 1000) ? 1000 : uncompressSize) }));
                    }
                    if (CompressPacketOutputStream.logger.isTraceEnabled()) {
                        CompressPacketOutputStream.logger.trace("send compress: {}{}", this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog - 7, 0, uncompressSize, new byte[][] { this.header, this.remainingData }));
                    }
                    if (commandEnd && this.lastPacketExactMaxPacketLength) {
                        this.writeEmptyPacket();
                    }
                    return;
                }
            }
            final int uncompressSize2 = Math.min(16777215, this.remainingData.length);
            this.checkMaxAllowedLength(uncompressSize2);
            this.cmdLength += uncompressSize2;
            this.header[0] = (byte)uncompressSize2;
            this.header[1] = (byte)(uncompressSize2 >>> 8);
            this.header[2] = (byte)(uncompressSize2 >>> 16);
            this.header[3] = (byte)(this.compressSeqNo++);
            this.header[4] = 0;
            this.header[5] = 0;
            this.header[6] = 0;
            this.out.write(this.header, 0, 7);
            this.out.write(this.remainingData);
            this.remainingData = CompressPacketOutputStream.EMPTY_ARRAY;
            if (this.traceCache != null) {
                this.traceCache.put(new TraceObject(true, 2, this.threadId, new byte[][] { Arrays.copyOfRange(this.header, 0, 7), Arrays.copyOfRange(this.remainingData, 0, (this.remainingData.length > 1000) ? 1000 : this.remainingData.length) }));
            }
            if (CompressPacketOutputStream.logger.isTraceEnabled()) {
                CompressPacketOutputStream.logger.trace("send uncompress: {}{}", this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog - 7, 0, this.remainingData.length, new byte[][] { this.header, this.remainingData }));
            }
            if (commandEnd && this.lastPacketExactMaxPacketLength) {
                this.writeEmptyPacket();
            }
        }
    }
    
    @Override
    public void writeEmptyPacket() throws IOException {
        if (this.enableNetworkStatistics) {
            this.timestampBeforeFlush = System.currentTimeMillis();
        }
        this.buf[0] = 4;
        this.buf[1] = 0;
        this.buf[2] = 0;
        this.buf[3] = (byte)(this.compressSeqNo++);
        this.buf[4] = 0;
        this.buf[5] = 0;
        this.buf[6] = 0;
        this.buf[7] = 0;
        this.buf[8] = 0;
        this.buf[9] = 0;
        this.buf[10] = (byte)(this.seqNo++);
        this.out.write(this.buf, 0, 11);
        if (this.traceCache != null) {
            this.traceCache.put(new TraceObject(true, 2, this.threadId, new byte[][] { Arrays.copyOfRange(this.buf, 0, 11) }));
        }
        if (CompressPacketOutputStream.logger.isTraceEnabled()) {
            CompressPacketOutputStream.logger.trace("send uncompress:{}{}", this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog, 0, 11, new byte[][] { this.buf }));
        }
    }
    
    static {
        logger = LoggerFactory.getLogger(CompressPacketOutputStream.class);
        EMPTY_ARRAY = new byte[0];
    }
}
