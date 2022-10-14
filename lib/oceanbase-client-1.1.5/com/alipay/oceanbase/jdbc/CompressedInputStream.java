// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.io.EOFException;
import java.util.zip.DataFormatException;
import java.io.IOException;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.log.NullLogger;
import com.alipay.oceanbase.jdbc.log.Log;
import com.alipay.oceanbase.jdbc.stats.ConnectionStats;
import java.util.zip.Inflater;
import java.io.InputStream;

class CompressedInputStream extends InputStream
{
    private byte[] buffer;
    private InputStream in;
    private Inflater inflater;
    private ConnectionPropertiesImpl.BooleanConnectionProperty traceProtocol;
    private ConnectionStats connStats;
    private Log log;
    private byte[] packetHeaderBuffer;
    private int pos;
    private boolean receivedUcompressedData;
    
    public CompressedInputStream(final Connection conn, final InputStream streamFromServer) {
        this.connStats = null;
        this.packetHeaderBuffer = new byte[7];
        this.pos = 0;
        this.receivedUcompressedData = false;
        this.traceProtocol = ((ConnectionPropertiesImpl)conn).traceProtocol;
        this.connStats = ((MySQLConnection)conn).getConnectionStats();
        try {
            this.log = conn.getLog();
        }
        catch (SQLException e) {
            this.log = new NullLogger(null);
        }
        this.in = streamFromServer;
        this.inflater = new Inflater();
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
        this.inflater.end();
        this.inflater = null;
        this.traceProtocol = null;
        this.log = null;
    }
    
    private void getNextPacketFromServer() throws IOException {
        byte[] uncompressedData = null;
        final int lengthRead = this.readFully(this.packetHeaderBuffer, 0, 7);
        if (lengthRead < 7) {
            throw new IOException("Unexpected end of input stream");
        }
        final int compressedPacketLength = (this.packetHeaderBuffer[0] & 0xFF) + ((this.packetHeaderBuffer[1] & 0xFF) << 8) + ((this.packetHeaderBuffer[2] & 0xFF) << 16);
        int uncompressedLength = (this.packetHeaderBuffer[4] & 0xFF) + ((this.packetHeaderBuffer[5] & 0xFF) << 8) + ((this.packetHeaderBuffer[6] & 0xFF) << 16);
        final boolean doTrace = this.traceProtocol.getValueAsBoolean();
        if (doTrace) {
            this.log.logTrace("Reading compressed packet of length " + compressedPacketLength + " uncompressed to " + uncompressedLength);
        }
        if (uncompressedLength > 0) {
            uncompressedData = new byte[uncompressedLength];
            final byte[] compressedBuffer = new byte[compressedPacketLength];
            this.readFully(compressedBuffer, 0, compressedPacketLength);
            this.inflater.reset();
            this.inflater.setInput(compressedBuffer);
            final long inflateStartNs = System.nanoTime();
            try {
                this.inflater.inflate(uncompressedData);
            }
            catch (DataFormatException dfe) {
                throw new IOException("Error while uncompressing packet from server.");
            }
            this.connStats.addZlibDecompressCostNs(System.nanoTime() - inflateStartNs);
            this.receivedUcompressedData = false;
        }
        else {
            this.receivedUcompressedData = true;
            if (doTrace) {
                this.log.logTrace("Packet didn't meet compression threshold, not uncompressing...");
            }
            uncompressedLength = compressedPacketLength;
            uncompressedData = new byte[uncompressedLength];
            this.readFully(uncompressedData, 0, uncompressedLength);
        }
        if (doTrace) {
            if (uncompressedLength > 1024) {
                this.log.logTrace("Uncompressed packet: \n" + StringUtils.dumpAsHex(uncompressedData, 256));
                final byte[] tempData = new byte[256];
                System.arraycopy(uncompressedData, uncompressedLength - 256, tempData, 0, 256);
                this.log.logTrace("Uncompressed packet: \n" + StringUtils.dumpAsHex(tempData, 256));
                this.log.logTrace("Large packet dump truncated. Showing first and last 256 bytes.");
            }
            else {
                this.log.logTrace("Uncompressed packet: \n" + StringUtils.dumpAsHex(uncompressedData, uncompressedLength));
            }
        }
        if (this.buffer != null && this.pos < this.buffer.length) {
            if (doTrace) {
                this.log.logTrace("Combining remaining packet with new: ");
            }
            final int remaining = this.buffer.length - this.pos;
            final byte[] newBuffer = new byte[remaining + uncompressedData.length];
            System.arraycopy(this.buffer, this.pos, newBuffer, 0, remaining);
            System.arraycopy(uncompressedData, 0, newBuffer, remaining, uncompressedData.length);
            uncompressedData = newBuffer;
        }
        this.pos = 0;
        this.buffer = uncompressedData;
    }
    
    private void getNextPacketIfRequired(final int numBytes) throws IOException {
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
        final int remainingBufferLength = this.buffer.length - this.pos;
        final int consummedBytesLength = Math.min(remainingBufferLength, len);
        System.arraycopy(this.buffer, this.pos, b, off, consummedBytesLength);
        this.pos += consummedBytesLength;
        return consummedBytesLength;
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
    
    public boolean isReceivedUcompressedData() {
        return this.receivedUcompressedData;
    }
}
