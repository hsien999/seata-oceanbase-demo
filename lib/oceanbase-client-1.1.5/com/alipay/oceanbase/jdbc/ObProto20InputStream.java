// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.io.EOFException;
import com.alipay.oceanbase.jdbc.util.ObCrc16;
import java.io.IOException;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.log.NullLogger;
import com.alipay.oceanbase.jdbc.util.ObCrc32C;
import com.alipay.oceanbase.jdbc.log.Log;
import com.alipay.oceanbase.jdbc.stats.ConnectionStats;
import java.io.InputStream;

public class ObProto20InputStream extends InputStream
{
    private byte[] buffer;
    private InputStream in;
    private ConnectionPropertiesImpl.BooleanConnectionProperty traceProtocol;
    private ConnectionStats connStats;
    private Log log;
    private long currConnectionId;
    private long requestId;
    private byte prePacketSeq;
    private byte[] packetHeaderBuffer;
    private byte[] payloadChecksum;
    private ObProto20PacketHeader header;
    private ObProto20ExtraInfo extraInfo;
    private ObCrc32C crc32C;
    private int pos;
    private boolean receivedDataWithoutChecksum;
    
    public ObProto20InputStream(final Connection conn, final InputStream streamFromServer, final long connectionId) {
        this.connStats = null;
        this.currConnectionId = 0L;
        this.requestId = 0L;
        this.prePacketSeq = 0;
        this.packetHeaderBuffer = new byte[31];
        this.payloadChecksum = new byte[4];
        this.header = new ObProto20PacketHeader();
        this.extraInfo = new ObProto20ExtraInfo();
        this.crc32C = new ObCrc32C();
        this.pos = 0;
        this.receivedDataWithoutChecksum = false;
        this.traceProtocol = ((ConnectionPropertiesImpl)conn).traceProtocol;
        this.connStats = ((MySQLConnection)conn).getConnectionStats();
        this.currConnectionId = connectionId;
        try {
            this.log = conn.getLog();
        }
        catch (SQLException e) {
            this.log = new NullLogger(null);
        }
        this.in = streamFromServer;
    }
    
    public ObProto20InputStream(final InputStream streamFromServer, final long connectionId) {
        this.connStats = null;
        this.currConnectionId = 0L;
        this.requestId = 0L;
        this.prePacketSeq = 0;
        this.packetHeaderBuffer = new byte[31];
        this.payloadChecksum = new byte[4];
        this.header = new ObProto20PacketHeader();
        this.extraInfo = new ObProto20ExtraInfo();
        this.crc32C = new ObCrc32C();
        this.pos = 0;
        this.receivedDataWithoutChecksum = false;
        this.traceProtocol = null;
        this.log = new NullLogger(null);
        this.currConnectionId = connectionId;
        this.in = streamFromServer;
        this.connStats = new ConnectionStats();
    }
    
    public void setPrePacketSeq(final byte prePacketSeq) {
        this.prePacketSeq = prePacketSeq;
    }
    
    public void setRequestId(final long requestId) {
        this.requestId = requestId;
    }
    
    @Override
    public int available() throws IOException {
        if (this.buffer == null) {
            return this.in.available();
        }
        return this.buffer.length - this.pos + this.in.available();
    }
    
    @Override
    public void close() throws IOException {
        this.in.close();
        this.buffer = null;
        this.traceProtocol = null;
        this.log = null;
    }
    
    private void getNextPacketFromServer() throws IOException, SQLException {
        byte[] rawMysqlData = null;
        final int lengthRead = this.readFully(this.packetHeaderBuffer, 0, 31);
        if (lengthRead < 31) {
            throw new IOException("Unexpected end of input stream");
        }
        this.checkHeader();
        final boolean doTrace = null != this.traceProtocol && this.traceProtocol.getValueAsBoolean();
        if (doTrace) {
            this.log.logTrace("Reading proto20 packet of length " + this.header.payloadLen + 31 + 4);
        }
        rawMysqlData = new byte[(int)this.header.payloadLen];
        this.readFully(rawMysqlData, 0, (int)this.header.payloadLen);
        this.readFully(this.payloadChecksum, 0, 4);
        final Buffer payloadChecksumBuffer = new Buffer(this.payloadChecksum);
        final long receivedPayloadChecksum = payloadChecksumBuffer.readLong();
        if (0L != receivedPayloadChecksum) {
            this.receivedDataWithoutChecksum = false;
            final long crc32StartNs = System.nanoTime();
            this.crc32C.reset();
            this.crc32C.update(rawMysqlData, 0, rawMysqlData.length);
            final long localPayloadChecksum = this.crc32C.getValue();
            this.connStats.addCrc32ResponseCostNs(System.nanoTime() - crc32StartNs);
            if (localPayloadChecksum != receivedPayloadChecksum) {
                throw SQLError.createSQLException(String.format("payload checksum mismatch, expectedPayloadChecksum=%d, received PayloadChecksum=%d", localPayloadChecksum, receivedPayloadChecksum), null);
            }
        }
        else {
            this.receivedDataWithoutChecksum = true;
            if (doTrace) {
                this.log.logTrace("proto20 packet not used checksum...");
            }
        }
        if (doTrace) {
            this.log.logTrace("mysql packet: \n" + StringUtils.dumpAsHex(rawMysqlData, rawMysqlData.length));
        }
        this.checkExtraInfo(rawMysqlData);
        int mysqlPayloadStartPos = 0;
        if (this.header.isExtraInfoExist()) {
            if (doTrace) {
                this.log.logTrace("this proto20 packet has extra info, length=" + this.extraInfo.extraLen);
            }
            mysqlPayloadStartPos = (int)(this.extraInfo.extraLen + 4L);
        }
        if (this.buffer != null && this.pos < this.buffer.length) {
            if (doTrace) {
                this.log.logTrace("Combining remaining packet with new: ");
            }
            final int remaining = this.buffer.length - this.pos;
            final byte[] newBuffer = new byte[remaining + (rawMysqlData.length - mysqlPayloadStartPos)];
            System.arraycopy(this.buffer, this.pos, newBuffer, 0, remaining);
            System.arraycopy(rawMysqlData, mysqlPayloadStartPos, newBuffer, remaining, rawMysqlData.length - mysqlPayloadStartPos);
            rawMysqlData = newBuffer;
            this.pos = 0;
        }
        else {
            this.pos = mysqlPayloadStartPos;
        }
        this.buffer = rawMysqlData;
    }
    
    private void checkHeader() throws SQLException {
        final Buffer headerBuffer = new Buffer(this.packetHeaderBuffer);
        this.header.reset();
        this.header.compressedLen = headerBuffer.readLongInt();
        this.header.compressedSeqId = headerBuffer.readByte();
        this.header.uncompressedLen = headerBuffer.readLongInt();
        this.header.magicNum = headerBuffer.readInt();
        this.header.version = headerBuffer.readInt();
        this.header.connId = headerBuffer.readLong();
        this.header.requestId = headerBuffer.readLongInt();
        this.header.packetSeq = headerBuffer.readByte();
        this.header.payloadLen = headerBuffer.readLong();
        this.header.flag = headerBuffer.readLong();
        this.header.reserved = headerBuffer.readInt();
        this.header.headerChecksum = headerBuffer.readInt();
        if (0 != this.header.headerChecksum) {
            final int localHeaderChecksum = ObCrc16.calculate(this.packetHeaderBuffer, 29);
            if (localHeaderChecksum != this.header.headerChecksum) {
                throw SQLError.createSQLException(String.format("header checksum mismatch, expectedHeaderChecksum=%d, received headerChecksum=%d", localHeaderChecksum, this.header.headerChecksum), null);
            }
        }
        if (this.header.compressedLen != this.header.payloadLen + 24L + 4L) {
            throw SQLError.createSQLException(String.format("packet len mismatch, totolLen=%d, payloadLen=%d, headerLen=%d, tailerLen=%d", this.header.compressedLen, this.header.payloadLen, 24, 4), null);
        }
        if (0 != this.header.uncompressedLen) {
            throw SQLError.createSQLException(String.format("uncompressedLen must be 0, uncompressedLen=%d", this.header.uncompressedLen), null);
        }
        if (8363 != this.header.magicNum) {
            throw SQLError.createSQLException(String.format("invalid proto20 magic num, magicNum=%d, expectedMagicNum=%d", this.header.uncompressedLen, this.header.magicNum, 8363), null);
        }
        if (this.currConnectionId != this.header.connId) {
            throw SQLError.createSQLException(String.format("connectionId mismatch, currConnectionId=%d, connId=%d", this.currConnectionId, this.header.connId), null);
        }
        if (this.requestId != this.header.requestId) {
            throw SQLError.createSQLException(String.format("requestId mismatch, currRequestId=%d, requestId=%d", this.requestId, this.header.requestId), null);
        }
        ++this.prePacketSeq;
        if (this.prePacketSeq != this.header.packetSeq) {
            throw SQLError.createSQLException(String.format("Packets out of order, expected packet seq=%d, but received packet seq=%d", this.prePacketSeq, this.header.packetSeq), null);
        }
        if (20 != this.header.version) {
            throw SQLError.createSQLException(String.format("invalid packet version, expected version=%d, but received version=%d", 20, this.header.version), null);
        }
    }
    
    private void checkExtraInfo(final byte[] payloadBuffer) throws IOException {
        this.extraInfo.reset();
        if (this.header.isExtraInfoExist()) {
            final Buffer extraBuffer = new Buffer(payloadBuffer);
            this.extraInfo.extraLen = extraBuffer.readLong();
            if (this.extraInfo.extraLen + 4L > this.header.payloadLen) {
                throw new IOException(String.format("Invalid extra len, extraLen=%d, payloadLen=%d", this.extraInfo.extraLen + 4L, this.header.payloadLen));
            }
        }
    }
    
    private void getNextPacketIfRequired(final int numBytes) throws IOException, SQLException {
        while (this.buffer == null || this.pos + numBytes > this.buffer.length) {
            this.getNextPacketFromServer();
        }
    }
    
    @Override
    public int read() throws IOException {
        try {
            this.getNextPacketIfRequired(1);
        }
        catch (IOException ioEx) {
            if (null != this.log) {
                this.log.logWarn("fail to get next packet", ioEx);
            }
            return -1;
        }
        catch (SQLException e) {
            if (null != this.log) {
                this.log.logWarn("fail to get next packet", e);
            }
            return -1;
        }
        return this.buffer[this.pos++] & 0xFF;
    }
    
    @Override
    public int read(final byte[] b) throws IOException {
        return this.read(b, 0, b.length);
    }
    
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        }
        if (off < 0 || off > b.length || len < 0 || off + len > b.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        }
        if (len <= 0) {
            return 0;
        }
        try {
            this.getNextPacketIfRequired(len);
        }
        catch (IOException ioEx) {
            if (null != this.log) {
                this.log.logWarn("fail to get next packet", ioEx);
            }
            return -1;
        }
        catch (SQLException e) {
            if (null != this.log) {
                this.log.logWarn("fail to get next packet", e);
            }
            return -1;
        }
        System.arraycopy(this.buffer, this.pos, b, off, len);
        this.pos += len;
        return len;
    }
    
    private final int readFully(final byte[] b, final int off, final int len) throws IOException {
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n;
        int count;
        for (n = 0; n < len; n += count) {
            count = this.in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException();
            }
        }
        return n;
    }
    
    @Override
    public long skip(final long n) throws IOException {
        long count = 0L;
        for (long i = 0L; i < n; ++i) {
            final int bytesRead = this.read();
            if (bytesRead == -1) {
                break;
            }
            ++count;
        }
        return count;
    }
    
    public boolean isReceivedDataWithoutChecksum() {
        return this.receivedDataWithoutChecksum;
    }
    
    public ObProto20PacketHeader getHeader() {
        return this.header;
    }
    
    public ObProto20ExtraInfo getExtraInfo() {
        return this.extraInfo;
    }
    
    public class ObProto20PacketHeader
    {
        public int compressedLen;
        public byte compressedSeqId;
        public int uncompressedLen;
        public int magicNum;
        public int version;
        public long connId;
        public int requestId;
        public byte packetSeq;
        public long payloadLen;
        public long flag;
        public int reserved;
        public int headerChecksum;
        
        public ObProto20PacketHeader() {
            this.compressedLen = 0;
            this.compressedSeqId = 0;
            this.uncompressedLen = 0;
            this.magicNum = 0;
            this.version = 0;
            this.connId = 0L;
            this.requestId = 0;
            this.packetSeq = 0;
            this.payloadLen = 0L;
            this.flag = 0L;
            this.reserved = 0;
            this.headerChecksum = 0;
        }
        
        boolean isExtraInfoExist() {
            return 0x1L == (ObProto20InputStream.this.header.flag & 0x1L);
        }
        
        void reset() {
            this.compressedLen = 0;
            this.compressedSeqId = 0;
            this.uncompressedLen = 0;
            this.magicNum = 0;
            this.version = 0;
            this.connId = 0L;
            this.requestId = 0;
            this.packetSeq = 0;
            this.payloadLen = 0L;
            this.flag = 0L;
            this.reserved = 0;
            this.headerChecksum = 0;
        }
    }
    
    public class ObProto20ExtraInfo
    {
        public long extraLen;
        
        public ObProto20ExtraInfo() {
            this.extraLen = 0L;
        }
        
        void reset() {
            this.extraLen = 0L;
        }
    }
}
