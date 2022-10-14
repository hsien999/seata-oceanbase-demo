// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.output;

import java.io.Reader;
import com.oceanbase.jdbc.internal.util.Utils;
import java.io.InputStream;
import java.util.Arrays;
import com.oceanbase.jdbc.internal.util.exceptions.MaxAllowedPacketException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.TimeZone;
import com.oceanbase.jdbc.internal.io.LruTraceCache;
import java.io.FilterOutputStream;

public abstract class AbstractPacketOutputStream extends FilterOutputStream implements PacketOutputStream
{
    private static final byte QUOTE = 39;
    private static final byte DBL_QUOTE = 34;
    private static final byte ZERO_BYTE = 0;
    private static final byte BACKSLASH = 92;
    private static final int SMALL_BUFFER_SIZE = 8192;
    private static final int MEDIUM_BUFFER_SIZE = 131072;
    private static final int LARGE_BUFFER_SIZE = 1048576;
    protected final int maxQuerySizeToLog;
    protected byte[] buf;
    protected int pos;
    protected int maxAllowedPacket;
    protected long cmdLength;
    protected boolean permitTrace;
    protected int seqNo;
    protected String serverThreadLog;
    protected LruTraceCache traceCache;
    private int mark;
    private boolean bufferContainDataAfterMark;
    protected long threadId;
    private TimeZone tm;
    protected boolean enableNetworkStatistics;
    protected Charset charset;
    
    public AbstractPacketOutputStream(final OutputStream out, final int maxQuerySizeToLog, final long threadId, final String encoding) {
        super(out);
        this.maxAllowedPacket = Integer.MAX_VALUE;
        this.seqNo = 0;
        this.serverThreadLog = "";
        this.traceCache = null;
        this.mark = -1;
        this.bufferContainDataAfterMark = false;
        this.tm = null;
        this.enableNetworkStatistics = false;
        this.charset = null;
        this.buf = new byte[8192];
        this.maxQuerySizeToLog = maxQuerySizeToLog;
        this.cmdLength = 0L;
        this.threadId = threadId;
        this.charset = Charset.forName(encoding);
    }
    
    @Override
    public void setTimeZone(final TimeZone tm) {
        this.tm = tm;
    }
    
    @Override
    public TimeZone getTimeZone() {
        return this.tm;
    }
    
    public abstract int getMaxPacketLength();
    
    @Override
    public abstract void startPacket(final int p0);
    
    protected abstract void flushBuffer(final boolean p0) throws IOException;
    
    private void growBuffer(final int len) throws IOException {
        final int bufferLength = this.buf.length;
        int newCapacity;
        if (bufferLength == 8192) {
            if (len + this.pos < 131072) {
                newCapacity = 131072;
            }
            else if (len + this.pos < 1048576) {
                newCapacity = 1048576;
            }
            else {
                newCapacity = this.getMaxPacketLength();
            }
        }
        else if (bufferLength == 131072) {
            if (len + this.pos < 1048576) {
                newCapacity = 1048576;
            }
            else {
                newCapacity = this.getMaxPacketLength();
            }
        }
        else if (this.bufferContainDataAfterMark) {
            newCapacity = Math.max(len + this.pos, this.getMaxPacketLength());
        }
        else {
            newCapacity = this.getMaxPacketLength();
        }
        if (this.mark != -1 && len + this.pos > newCapacity) {
            this.flushBufferStopAtMark();
            if (len + this.pos <= bufferLength) {
                return;
            }
            if (len + this.pos > newCapacity) {
                newCapacity = len + this.pos;
            }
        }
        final byte[] newBuf = new byte[newCapacity];
        System.arraycopy(this.buf, 0, newBuf, 0, this.pos);
        this.buf = newBuf;
    }
    
    @Override
    public void writeEmptyPacket(final int seqNo) throws IOException {
        this.startPacket(seqNo);
        this.writeEmptyPacket();
        this.out.flush();
        this.cmdLength = 0L;
    }
    
    @Override
    public abstract void writeEmptyPacket() throws IOException;
    
    @Override
    public void flush() throws IOException {
        this.flushBuffer(true);
        this.out.flush();
        if (this.buf.length > 8192 && this.cmdLength * 2L < this.buf.length) {
            this.buf = new byte[8192];
        }
        if (this.cmdLength >= this.maxAllowedPacket) {
            throw new MaxAllowedPacketException("query size (" + this.cmdLength + ") is >= to max_allowed_packet (" + this.maxAllowedPacket + ")", true);
        }
    }
    
    @Override
    public boolean checkRemainingSize(final int len) {
        return this.getMaxPacketLength() - this.pos > len;
    }
    
    @Override
    public void checkMaxAllowedLength(final int length) throws MaxAllowedPacketException {
        if (this.cmdLength + length >= this.maxAllowedPacket && this.cmdLength == 0L) {
            throw new MaxAllowedPacketException("query size (" + (this.cmdLength + length) + ") is >= to max_allowed_packet (" + this.maxAllowedPacket + ")", false);
        }
    }
    
    @Override
    public boolean exceedMaxLength() {
        return this.cmdLength + (this.pos - this.initialPacketPos()) >= this.maxAllowedPacket;
    }
    
    @Override
    public OutputStream getOutputStream() {
        return this.out;
    }
    
    @Override
    public void writeShort(final short value) throws IOException {
        if (2 > this.buf.length - this.pos) {
            final byte[] arr = { (byte)value, (byte)(value >> 8) };
            this.write(arr, 0, 2);
            return;
        }
        this.buf[this.pos] = (byte)value;
        this.buf[this.pos + 1] = (byte)(value >> 8);
        this.pos += 2;
    }
    
    @Override
    public final void writeIntV1(final int i) throws IOException {
        final byte[] b = this.buf;
        b[this.pos++] = (byte)(i & 0xFF);
        b[this.pos++] = (byte)(i >>> 8);
    }
    
    @Override
    public void writeInt(final int value) throws IOException {
        if (4 > this.buf.length - this.pos) {
            final byte[] arr = { (byte)value, (byte)(value >> 8), (byte)(value >> 16), (byte)(value >> 24) };
            this.write(arr, 0, 4);
            return;
        }
        this.buf[this.pos] = (byte)value;
        this.buf[this.pos + 1] = (byte)(value >> 8);
        this.buf[this.pos + 2] = (byte)(value >> 16);
        this.buf[this.pos + 3] = (byte)(value >> 24);
        this.pos += 4;
    }
    
    @Override
    public void writeFloatV1(final float f) throws IOException {
        final int i = Float.floatToIntBits(f);
        final byte[] b = this.buf;
        b[this.pos++] = (byte)(i & 0xFF);
        b[this.pos++] = (byte)(i >>> 8);
        b[this.pos++] = (byte)(i >>> 16);
        b[this.pos++] = (byte)(i >>> 24);
    }
    
    @Override
    public void writeLong(final long value) throws IOException {
        if (8 > this.buf.length - this.pos) {
            final byte[] arr = { (byte)value, (byte)(value >> 8), (byte)(value >> 16), (byte)(value >> 24), (byte)(value >> 32), (byte)(value >> 40), (byte)(value >> 48), (byte)(value >> 56) };
            this.write(arr, 0, 8);
            return;
        }
        this.buf[this.pos] = (byte)value;
        this.buf[this.pos + 1] = (byte)(value >> 8);
        this.buf[this.pos + 2] = (byte)(value >> 16);
        this.buf[this.pos + 3] = (byte)(value >> 24);
        this.buf[this.pos + 4] = (byte)(value >> 32);
        this.buf[this.pos + 5] = (byte)(value >> 40);
        this.buf[this.pos + 6] = (byte)(value >> 48);
        this.buf[this.pos + 7] = (byte)(value >> 56);
        this.pos += 8;
    }
    
    @Override
    public void writeLongV1(final long i) throws IOException {
        final byte[] b = this.buf;
        b[this.pos++] = (byte)(i & 0xFFL);
        b[this.pos++] = (byte)(i >>> 8);
        b[this.pos++] = (byte)(i >>> 16);
        b[this.pos++] = (byte)(i >>> 24);
    }
    
    @Override
    public void writeBytes(final byte value, final int len) throws IOException {
        if (len > this.buf.length - this.pos) {
            final byte[] arr = new byte[len];
            Arrays.fill(arr, value);
            this.write(arr, 0, len);
            return;
        }
        for (int i = this.pos; i < this.pos + len; ++i) {
            this.buf[i] = value;
        }
        this.pos += len;
    }
    
    @Override
    public void writeFieldLength(final long length) throws IOException {
        if (length < 251L) {
            this.write((byte)length);
            return;
        }
        if (length < 65536L) {
            if (3 > this.buf.length - this.pos) {
                final byte[] arr = { -4, (byte)length, (byte)(length >>> 8) };
                this.write(arr, 0, 3);
                return;
            }
            this.buf[this.pos] = -4;
            this.buf[this.pos + 1] = (byte)length;
            this.buf[this.pos + 2] = (byte)(length >>> 8);
            this.pos += 3;
        }
        else if (length < 16777216L) {
            if (4 > this.buf.length - this.pos) {
                final byte[] arr = { -3, (byte)length, (byte)(length >>> 8), (byte)(length >>> 16) };
                this.write(arr, 0, 4);
                return;
            }
            this.buf[this.pos] = -3;
            this.buf[this.pos + 1] = (byte)length;
            this.buf[this.pos + 2] = (byte)(length >>> 8);
            this.buf[this.pos + 3] = (byte)(length >>> 16);
            this.pos += 4;
        }
        else {
            if (9 > this.buf.length - this.pos) {
                final byte[] arr = { -2, (byte)length, (byte)(length >>> 8), (byte)(length >>> 16), (byte)(length >>> 24), (byte)(length >>> 32), (byte)(length >>> 40), (byte)(length >>> 48), (byte)(length >>> 56) };
                this.write(arr, 0, 9);
                return;
            }
            this.buf[this.pos] = -2;
            this.buf[this.pos + 1] = (byte)length;
            this.buf[this.pos + 2] = (byte)(length >>> 8);
            this.buf[this.pos + 3] = (byte)(length >>> 16);
            this.buf[this.pos + 4] = (byte)(length >>> 24);
            this.buf[this.pos + 5] = (byte)(length >>> 32);
            this.buf[this.pos + 6] = (byte)(length >>> 40);
            this.buf[this.pos + 7] = (byte)(length >>> 48);
            this.buf[this.pos + 8] = (byte)(length >>> 56);
            this.pos += 9;
        }
    }
    
    @Override
    public void write(final int value) throws IOException {
        if (this.pos >= this.buf.length) {
            if (this.pos >= this.getMaxPacketLength() && !this.bufferContainDataAfterMark) {
                this.flushBuffer(false);
            }
            else {
                this.growBuffer(1);
            }
        }
        this.buf[this.pos++] = (byte)value;
    }
    
    @Override
    public void write(final byte[] arr) throws IOException {
        this.write(arr, 0, arr.length);
    }
    
    @Override
    public void write(final byte[] arr, int off, final int len) throws IOException {
        if (len > this.buf.length - this.pos) {
            if (this.buf.length != this.getMaxPacketLength()) {
                this.growBuffer(len);
            }
            if (len > this.buf.length - this.pos) {
                if (this.mark == -1) {
                    int remainingLen = len;
                    while (true) {
                        final int lenToFillBuffer = Math.min(this.getMaxPacketLength() - this.pos, remainingLen);
                        System.arraycopy(arr, off, this.buf, this.pos, lenToFillBuffer);
                        remainingLen -= lenToFillBuffer;
                        off += lenToFillBuffer;
                        this.pos += lenToFillBuffer;
                        if (remainingLen <= 0) {
                            break;
                        }
                        this.flushBuffer(false);
                    }
                    return;
                }
                this.growBuffer(len);
                if (this.mark != -1) {
                    this.flushBufferStopAtMark();
                }
            }
        }
        System.arraycopy(arr, off, this.buf, this.pos, len);
        this.pos += len;
    }
    
    @Override
    public void write(final String str) throws IOException {
        this.write(str, false, false);
    }
    
    @Override
    public void write(final String str, final boolean escape, final boolean noBackslashEscapes) throws IOException {
        final int charsLength = str.length();
        if (charsLength * 3 + 2 >= this.buf.length - this.pos) {
            final byte[] arr = str.getBytes(this.charset);
            if (escape) {
                this.write(39);
                this.writeBytesEscaped(arr, arr.length, noBackslashEscapes);
                this.write(39);
            }
            else {
                this.write(arr, 0, arr.length);
            }
            return;
        }
        int charsOffset = 0;
        if (escape) {
            this.buf[this.pos++] = 39;
            if (noBackslashEscapes) {
                char currChar;
                while (charsOffset < charsLength && (currChar = str.charAt(charsOffset)) < '\u0080') {
                    if (currChar == '\'') {
                        this.buf[this.pos++] = 39;
                    }
                    this.buf[this.pos++] = (byte)currChar;
                    ++charsOffset;
                }
            }
            else {
                char currChar;
                while (charsOffset < charsLength && (currChar = str.charAt(charsOffset)) < '\u0080') {
                    if (currChar == '\\' || currChar == '\'' || currChar == '\0' || currChar == '\"') {
                        this.buf[this.pos++] = 92;
                    }
                    this.buf[this.pos++] = (byte)currChar;
                    ++charsOffset;
                }
            }
        }
        else {
            char currChar;
            while (charsOffset < charsLength && (currChar = str.charAt(charsOffset)) < '\u0080') {
                this.buf[this.pos++] = (byte)currChar;
                ++charsOffset;
            }
        }
        while (charsOffset < charsLength) {
            final char currChar = str.charAt(charsOffset++);
            if (currChar < '\u0080') {
                if (escape) {
                    if (noBackslashEscapes) {
                        if (currChar == '\'') {
                            this.buf[this.pos++] = 39;
                        }
                    }
                    else if (currChar == '\\' || currChar == '\'' || currChar == '\0' || currChar == '\"') {
                        this.buf[this.pos++] = 92;
                    }
                }
                this.buf[this.pos++] = (byte)currChar;
            }
            else if (currChar < '\u0800') {
                this.buf[this.pos++] = (byte)(0xC0 | currChar >> 6);
                this.buf[this.pos++] = (byte)(0x80 | (currChar & '?'));
            }
            else if (currChar >= '\ud800' && currChar < '\ue000') {
                if (currChar < '\udc00') {
                    if (charsOffset + 1 > charsLength) {
                        this.buf[this.pos++] = 99;
                    }
                    else {
                        final char nextChar = str.charAt(charsOffset);
                        if (nextChar >= '\udc00' && nextChar < '\ue000') {
                            final int surrogatePairs = (currChar << 10) + nextChar - 56613888;
                            this.buf[this.pos++] = (byte)(0xF0 | surrogatePairs >> 18);
                            this.buf[this.pos++] = (byte)(0x80 | (surrogatePairs >> 12 & 0x3F));
                            this.buf[this.pos++] = (byte)(0x80 | (surrogatePairs >> 6 & 0x3F));
                            this.buf[this.pos++] = (byte)(0x80 | (surrogatePairs & 0x3F));
                            ++charsOffset;
                        }
                        else {
                            this.buf[this.pos++] = 63;
                        }
                    }
                }
                else {
                    this.buf[this.pos++] = 63;
                }
            }
            else {
                this.buf[this.pos++] = (byte)(0xE0 | currChar >> 12);
                this.buf[this.pos++] = (byte)(0x80 | (currChar >> 6 & 0x3F));
                this.buf[this.pos++] = (byte)(0x80 | (currChar & '?'));
            }
        }
        if (escape) {
            this.buf[this.pos++] = 39;
        }
    }
    
    @Override
    public void write(final InputStream is, final boolean escape, final boolean noBackslashEscapes) throws IOException {
        final byte[] array = new byte[4096];
        if (escape) {
            int len;
            while ((len = is.read(array)) > 0) {
                this.writeBytesEscaped(array, len, noBackslashEscapes);
            }
        }
        else {
            int len;
            while ((len = is.read(array)) > 0) {
                this.write(array, 0, len);
            }
        }
    }
    
    @Override
    public void writeHex(final InputStream is, final boolean escape, final boolean noBackslashEscapes) throws IOException {
        final byte[] array = new byte[2048];
        if (escape) {
            int len;
            while ((len = is.read(array)) > 0) {
                this.writeBytesEscaped(Utils.toHexString(array).getBytes(), len << 1, noBackslashEscapes);
            }
        }
        else {
            int len;
            while ((len = is.read(array)) > 0) {
                this.write(Utils.toHexString(array).getBytes(), 0, len << 1);
            }
        }
    }
    
    @Override
    public void write(final InputStream is, long length, final boolean escape, final boolean noBackslashEscapes) throws IOException {
        int len;
        for (byte[] array = new byte[4096]; length > 0L && (len = is.read(array, 0, Math.min(4096, (int)length))) > 0; length -= len) {
            if (escape) {
                this.writeBytesEscaped(array, len, noBackslashEscapes);
            }
            else {
                this.write(array, 0, len);
            }
        }
    }
    
    @Override
    public void writeHex(final InputStream is, long length, final boolean escape, final boolean noBackslashEscapes) throws IOException {
        int len;
        for (byte[] array = new byte[2048]; length > 0L && (len = is.read(array, 0, Math.min(2048, (int)length))) > 0; length -= len) {
            this.writeBytesEscaped(Utils.toHexString(array).getBytes(), len << 1, escape);
        }
    }
    
    @Override
    public void write(final Reader reader, final boolean escape, final boolean noBackslashEscapes) throws IOException {
        final char[] buffer = new char[4096];
        int len;
        while ((len = reader.read(buffer)) >= 0) {
            final byte[] data = new String(buffer, 0, len).getBytes(this.charset);
            if (escape) {
                this.writeBytesEscaped(data, data.length, noBackslashEscapes);
            }
            else {
                this.write(data);
            }
        }
    }
    
    @Override
    public void writeEscapeQuote(final Reader reader, final boolean noBackslashEscapes) throws IOException {
        final char[] buffer = new char[4096];
        int len;
        while ((len = reader.read(buffer)) >= 0) {
            final byte[] data = new String(buffer, 0, len).getBytes(this.charset);
            this.writeBytesEscapedQuote(data, data.length, noBackslashEscapes);
        }
    }
    
    @Override
    public void writeEscapeQuote(final Reader reader, long length, final boolean noBackslashEscapes) throws IOException {
        int len;
        for (char[] buffer = new char[4096]; length > 0L && (len = reader.read(buffer, 0, Math.min((int)length, 4096))) >= 0; length -= len) {
            final byte[] data = new String(buffer, 0, len).getBytes(this.charset);
            this.writeBytesEscapedQuote(data, data.length, noBackslashEscapes);
        }
    }
    
    @Override
    public void write(final Reader reader, long length, final boolean escape, final boolean noBackslashEscapes) throws IOException {
        int len;
        for (char[] buffer = new char[4096]; length > 0L && (len = reader.read(buffer, 0, Math.min((int)length, 4096))) >= 0; length -= len) {
            final byte[] data = new String(buffer, 0, len).getBytes(this.charset);
            if (escape) {
                this.writeBytesEscaped(data, data.length, noBackslashEscapes);
            }
            else {
                this.write(data);
            }
        }
    }
    
    @Override
    public void writeBytesEscaped(final byte[] bytes, final int len, final boolean noBackslashEscapes) throws IOException {
        if (len * 2 > this.buf.length - this.pos) {
            if (this.buf.length != this.getMaxPacketLength()) {
                this.growBuffer(len * 2);
            }
            if (len * 2 > this.buf.length - this.pos) {
                if (this.mark == -1) {
                    if (noBackslashEscapes) {
                        for (int i = 0; i < len; ++i) {
                            if (39 == bytes[i]) {
                                this.buf[this.pos++] = 39;
                                if (this.buf.length <= this.pos) {
                                    this.flushBuffer(false);
                                }
                            }
                            this.buf[this.pos++] = bytes[i];
                            if (this.buf.length <= this.pos) {
                                this.flushBuffer(false);
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < len; ++i) {
                            if (bytes[i] == 39 || bytes[i] == 92 || bytes[i] == 34 || bytes[i] == 0) {
                                this.buf[this.pos++] = 92;
                                if (this.buf.length <= this.pos) {
                                    this.flushBuffer(false);
                                }
                            }
                            this.buf[this.pos++] = bytes[i];
                            if (this.buf.length <= this.pos) {
                                this.flushBuffer(false);
                            }
                        }
                    }
                    return;
                }
                this.growBuffer(len * 2);
                if (this.mark != -1) {
                    this.flushBufferStopAtMark();
                }
            }
        }
        if (noBackslashEscapes) {
            for (int i = 0; i < len; ++i) {
                if (39 == bytes[i]) {
                    this.buf[this.pos++] = 39;
                }
                this.buf[this.pos++] = bytes[i];
            }
        }
        else {
            for (int i = 0; i < len; ++i) {
                if (bytes[i] == 39 || bytes[i] == 92 || bytes[i] == 34 || bytes[i] == 0) {
                    this.buf[this.pos++] = 92;
                }
                this.buf[this.pos++] = bytes[i];
            }
        }
    }
    
    @Override
    public void writeBytesEscapedQuote(final byte[] bytes, final int len, final boolean noBackslashEscapes) throws IOException {
        if (len * 2 > this.buf.length - this.pos) {
            if (this.buf.length != this.getMaxPacketLength()) {
                this.growBuffer(len * 2);
            }
            if (len * 2 > this.buf.length - this.pos) {
                if (this.mark == -1) {
                    if (noBackslashEscapes) {
                        for (int i = 0; i < len; ++i) {
                            if (39 == bytes[i]) {
                                this.buf[this.pos++] = 39;
                                if (this.buf.length <= this.pos) {
                                    this.flushBuffer(false);
                                }
                            }
                            this.buf[this.pos++] = bytes[i];
                            if (this.buf.length <= this.pos) {
                                this.flushBuffer(false);
                            }
                        }
                    }
                    else {
                        for (int i = 0; i < len; ++i) {
                            if (bytes[i] == 39) {
                                this.buf[this.pos++] = 39;
                                if (this.buf.length <= this.pos) {
                                    this.flushBuffer(false);
                                }
                            }
                            this.buf[this.pos++] = bytes[i];
                            if (this.buf.length <= this.pos) {
                                this.flushBuffer(false);
                            }
                        }
                    }
                    return;
                }
                this.growBuffer(len * 2);
                if (this.mark != -1) {
                    this.flushBufferStopAtMark();
                }
            }
        }
        if (noBackslashEscapes) {
            for (int i = 0; i < len; ++i) {
                if (39 == bytes[i]) {
                    this.buf[this.pos++] = 39;
                }
                this.buf[this.pos++] = bytes[i];
            }
        }
        else {
            for (int i = 0; i < len; ++i) {
                if (bytes[i] == 39) {
                    this.buf[this.pos++] = 39;
                }
                this.buf[this.pos++] = bytes[i];
            }
        }
    }
    
    @Override
    public int getMaxAllowedPacket() {
        return this.maxAllowedPacket;
    }
    
    @Override
    public abstract void setMaxAllowedPacket(final int p0);
    
    @Override
    public void permitTrace(final boolean permitTrace) {
        this.permitTrace = permitTrace;
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
    public void mark() {
        this.mark = this.pos;
    }
    
    @Override
    public boolean isMarked() {
        return this.mark != -1;
    }
    
    @Override
    public void flushBufferStopAtMark() throws IOException {
        final int end = this.pos;
        this.pos = this.mark;
        this.flushBuffer(true);
        this.out.flush();
        this.startPacket(0);
        System.arraycopy(this.buf, this.mark, this.buf, this.pos, end - this.mark);
        this.pos += end - this.mark;
        this.mark = -1;
        this.bufferContainDataAfterMark = true;
    }
    
    @Override
    public boolean bufferIsDataAfterMark() {
        return this.bufferContainDataAfterMark;
    }
    
    @Override
    public byte[] resetMark() {
        this.mark = -1;
        if (this.bufferContainDataAfterMark) {
            final byte[] data = Arrays.copyOfRange(this.buf, this.initialPacketPos(), this.pos);
            this.startPacket(0);
            this.bufferContainDataAfterMark = false;
            return data;
        }
        return null;
    }
    
    @Override
    public int getPosition() {
        return this.pos;
    }
    
    @Override
    public void setPosition(final int val) {
        this.pos = val;
    }
}
