// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.output;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import java.io.IOException;
import com.oceanbase.jdbc.internal.util.Utils;
import com.oceanbase.jdbc.internal.io.TraceObject;
import java.util.Arrays;
import java.nio.charset.Charset;
import com.oceanbase.jdbc.util.Options;
import java.io.OutputStream;
import com.oceanbase.jdbc.internal.logging.Logger;

public class StandardPacketOutputStream extends AbstractPacketOutputStream
{
    private static final Logger logger;
    private static final int MAX_PACKET_LENGTH = 16777219;
    private int maxPacketLength;
    private long timestampBeforeFlush;
    
    public StandardPacketOutputStream(final OutputStream out, final Options options, final long threadId) {
        super(out, options.maxQuerySizeToLog, threadId, options.characterEncoding);
        this.maxPacketLength = 16777219;
        this.timestampBeforeFlush = 0L;
    }
    
    @Override
    public int getMaxPacketLength() {
        return this.maxPacketLength;
    }
    
    @Override
    public void startPacket(final int seqNo) {
        this.seqNo = seqNo;
        this.pos = 4;
        this.cmdLength = 0L;
    }
    
    @Override
    public void setMaxAllowedPacket(final int maxAllowedPacket) {
        this.maxAllowedPacket = maxAllowedPacket;
        this.maxPacketLength = Math.min(16777219, maxAllowedPacket + 4);
    }
    
    @Override
    public int initialPacketPos() {
        return 4;
    }
    
    @Override
    public void enableNetworkStatistics(final boolean flag) {
        this.enableNetworkStatistics = flag;
        this.timestampBeforeFlush = 0L;
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
        if (this.pos > 4) {
            this.buf[0] = (byte)(this.pos - 4);
            this.buf[1] = (byte)(this.pos - 4 >>> 8);
            this.buf[2] = (byte)(this.pos - 4 >>> 16);
            this.buf[3] = (byte)(this.seqNo++);
            this.checkMaxAllowedLength(this.pos - 4);
            this.out.write(this.buf, 0, this.pos);
            this.cmdLength += this.pos - 4;
            if (this.traceCache != null && this.permitTrace) {
                this.traceCache.put(new TraceObject(true, 0, this.threadId, new byte[][] { Arrays.copyOfRange(this.buf, 0, (this.pos > 1000) ? 1000 : this.pos) }));
            }
            if (StandardPacketOutputStream.logger.isTraceEnabled()) {
                if (this.permitTrace) {
                    StandardPacketOutputStream.logger.trace("send: {}{}", this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog, 0, this.pos, new byte[][] { this.buf }));
                }
                else {
                    StandardPacketOutputStream.logger.trace("send: content length={} {} com=<hidden>", (Object)(this.pos - 4), this.serverThreadLog);
                }
            }
            if (commandEnd && this.pos == 16777219) {
                this.writeEmptyPacket();
            }
            this.pos = 4;
        }
    }
    
    @Override
    public void writeEmptyPacket() throws IOException {
        if (this.enableNetworkStatistics) {
            this.timestampBeforeFlush = System.currentTimeMillis();
        }
        this.buf[0] = 0;
        this.buf[1] = 0;
        this.buf[2] = 0;
        this.buf[3] = (byte)(this.seqNo++);
        this.out.write(this.buf, 0, 4);
        if (this.traceCache != null) {
            this.traceCache.put(new TraceObject(true, 0, this.threadId, new byte[][] { Arrays.copyOfRange(this.buf, 0, 4) }));
        }
        if (StandardPacketOutputStream.logger.isTraceEnabled()) {
            StandardPacketOutputStream.logger.trace("send com : content length=0 {}{}", this.serverThreadLog, Utils.hexdump(this.maxQuerySizeToLog, 0, 4, new byte[][] { this.buf }));
        }
    }
    
    static {
        logger = LoggerFactory.getLogger(StandardPacketOutputStream.class);
    }
}
