// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read;

import java.util.Arrays;
import java.nio.charset.Charset;

public class Buffer
{
    public byte[] buf;
    public int position;
    public int limit;
    static final long NULL_LENGTH = -1L;
    public static final byte[] EMPTY_BYTE_ARRAY;
    
    public Buffer(final byte[] buf, final int limit) {
        this.buf = buf;
        this.limit = limit;
    }
    
    public Buffer(final byte[] buf) {
        this.buf = buf;
        this.limit = this.buf.length;
    }
    
    public Buffer() {
    }
    
    public int remaining() {
        return this.limit - this.position;
    }
    
    public String readStringNullEnd(final Charset charset) {
        final int initialPosition = this.position;
        int cnt = 0;
        while (this.remaining() > 0 && this.buf[this.position++] != 0) {
            ++cnt;
        }
        return new String(this.buf, initialPosition, cnt, charset);
    }
    
    public byte[] readBytesNullEnd() {
        final int initialPosition = this.position;
        int cnt = 0;
        while (this.remaining() > 0 && this.buf[this.position++] != 0) {
            ++cnt;
        }
        final byte[] tmpArr = new byte[cnt];
        System.arraycopy(this.buf, initialPosition, tmpArr, 0, cnt);
        return tmpArr;
    }
    
    public String readStringLengthEncoded(final Charset charset) {
        final int length = (int)this.getLengthEncodedNumeric();
        final String string = new String(this.buf, this.position, length, charset);
        this.position += length;
        return string;
    }
    
    public String readString(final int numberOfBytes) {
        this.position += numberOfBytes;
        return new String(this.buf, this.position - numberOfBytes, numberOfBytes);
    }
    
    public short readShort() {
        return (short)((this.buf[this.position++] & 0xFF) + ((this.buf[this.position++] & 0xFF) << 8));
    }
    
    public int read24bitword() {
        return (this.buf[this.position++] & 0xFF) + ((this.buf[this.position++] & 0xFF) << 8) + ((this.buf[this.position++] & 0xFF) << 16);
    }
    
    public int readInt() {
        return (this.buf[this.position++] & 0xFF) + ((this.buf[this.position++] & 0xFF) << 8) + ((this.buf[this.position++] & 0xFF) << 16) + ((this.buf[this.position++] & 0xFF) << 24);
    }
    
    public int readIntV1() {
        final byte[] b = this.buf;
        return (b[this.position++] & 0xFF) | (b[this.position++] & 0xFF) << 8;
    }
    
    public long readLongV1() {
        final byte[] b = this.buf;
        return ((long)b[this.position++] & 0xFFL) | ((long)b[this.position++] & 0xFFL) << 8 | (long)(b[this.position++] & 0xFF) << 16 | (long)(b[this.position++] & 0xFF) << 24;
    }
    
    public long readLongLongV1() {
        final byte[] b = this.buf;
        return (long)(b[this.position++] & 0xFF) | (long)(b[this.position++] & 0xFF) << 8 | (long)(b[this.position++] & 0xFF) << 16 | (long)(b[this.position++] & 0xFF) << 24 | (long)(b[this.position++] & 0xFF) << 32 | (long)(b[this.position++] & 0xFF) << 40 | (long)(b[this.position++] & 0xFF) << 48 | (long)(b[this.position++] & 0xFF) << 56;
    }
    
    public long readLong() {
        return (this.buf[this.position++] & 0xFF) + ((long)(this.buf[this.position++] & 0xFF) << 8) + ((long)(this.buf[this.position++] & 0xFF) << 16) + ((long)(this.buf[this.position++] & 0xFF) << 24) + ((long)(this.buf[this.position++] & 0xFF) << 32) + ((long)(this.buf[this.position++] & 0xFF) << 40) + ((long)(this.buf[this.position++] & 0xFF) << 48) + ((long)(this.buf[this.position++] & 0xFF) << 56);
    }
    
    public byte readByte() {
        return this.buf[this.position++];
    }
    
    public byte getByte() {
        return this.buf[this.position];
    }
    
    public byte[] getBytes(final int len) {
        final byte[] b = new byte[len];
        System.arraycopy(this.buf, this.position, b, 0, len);
        this.position += len;
        return b;
    }
    
    public byte[] getBytes(final int offset, final int len) {
        final byte[] dest = new byte[len];
        System.arraycopy(this.buf, offset, dest, 0, len);
        return dest;
    }
    
    public byte[] readLenByteArray(final int offset) {
        final long len = this.readFieldLength();
        if (len == -1L) {
            return null;
        }
        if (len == 0L) {
            return Buffer.EMPTY_BYTE_ARRAY;
        }
        this.position += offset;
        return this.getBytes((int)len);
    }
    
    public byte[] readRawBytes(final int numberOfBytes) {
        final byte[] tmpArr = new byte[numberOfBytes];
        System.arraycopy(this.buf, this.position, tmpArr, 0, numberOfBytes);
        this.position += numberOfBytes;
        return tmpArr;
    }
    
    public void skipByte() {
        ++this.position;
    }
    
    public void skipBytes(final int bytesToSkip) {
        this.position += bytesToSkip;
    }
    
    public void skipLengthEncodedBytes() {
        final int type = this.buf[this.position++] & 0xFF;
        switch (type) {
            case 251: {
                break;
            }
            case 252: {
                this.position += 2 + (0xFFFF & (this.buf[this.position] & 0xFF) + ((this.buf[this.position + 1] & 0xFF) << 8));
                break;
            }
            case 253: {
                this.position += 3 + (0xFFFFFF & (this.buf[this.position] & 0xFF) + ((this.buf[this.position + 1] & 0xFF) << 8) + ((this.buf[this.position + 2] & 0xFF) << 16));
                break;
            }
            case 254: {
                this.position += (int)(8L + ((this.buf[this.position] & 0xFF) + ((long)(this.buf[this.position + 1] & 0xFF) << 8) + ((long)(this.buf[this.position + 2] & 0xFF) << 16) + ((long)(this.buf[this.position + 3] & 0xFF) << 24) + ((long)(this.buf[this.position + 4] & 0xFF) << 32) + ((long)(this.buf[this.position + 5] & 0xFF) << 40) + ((long)(this.buf[this.position + 6] & 0xFF) << 48) + ((long)(this.buf[this.position + 7] & 0xFF) << 56)));
                break;
            }
            default: {
                this.position += type;
                break;
            }
        }
    }
    
    public long getLengthEncodedNumeric() {
        final int type = this.buf[this.position++] & 0xFF;
        switch (type) {
            case 251: {
                return -1L;
            }
            case 252: {
                return 0xFFFF & this.readShort();
            }
            case 253: {
                return 0xFFFFFF & this.read24bitword();
            }
            case 254: {
                return this.readLong();
            }
            default: {
                return type;
            }
        }
    }
    
    public void skipLengthEncodedNumeric() {
        final int type = this.buf[this.position++] & 0xFF;
        switch (type) {
            case 252: {
                this.position += 2;
            }
            case 253: {
                this.position += 3;
            }
            case 254: {
                this.position += 8;
            }
            default: {}
        }
    }
    
    public Buffer getLengthEncodedBuffer() {
        return new Buffer(this.getLengthEncodedBytes());
    }
    
    public byte[] getLengthEncodedBytes() {
        final int type = this.buf[this.position++] & 0xFF;
        int length = 0;
        switch (type) {
            case 251: {
                return null;
            }
            case 252: {
                length = (0xFFFF & this.readShort());
                break;
            }
            case 253: {
                length = (0xFFFFFF & this.read24bitword());
                break;
            }
            case 254: {
                length = (int)((this.buf[this.position++] & 0xFF) + ((long)(this.buf[this.position++] & 0xFF) << 8) + ((long)(this.buf[this.position++] & 0xFF) << 16) + ((long)(this.buf[this.position++] & 0xFF) << 24) + ((long)(this.buf[this.position++] & 0xFF) << 32) + ((long)(this.buf[this.position++] & 0xFF) << 40) + ((long)(this.buf[this.position++] & 0xFF) << 48) + ((long)(this.buf[this.position++] & 0xFF) << 56));
                break;
            }
            default: {
                length = type;
                break;
            }
        }
        final byte[] tmpBuf = new byte[length];
        System.arraycopy(this.buf, this.position, tmpBuf, 0, length);
        this.position += length;
        return tmpBuf;
    }
    
    public byte getByteAt(final int position) {
        return this.buf[position];
    }
    
    public void writeStringLength(final String value, final Charset charset) {
        final byte[] bytes = value.getBytes(charset);
        final int length = bytes.length;
        while (this.remaining() < length + 9) {
            this.grow();
        }
        this.writeLength(length);
        System.arraycopy(bytes, 0, this.buf, this.position, length);
        this.position += length;
    }
    
    public void writeStringLength(final byte[] bytes) {
        final int length = bytes.length;
        while (this.remaining() < length + 9) {
            this.grow();
        }
        this.writeLength(length);
        System.arraycopy(bytes, 0, this.buf, this.position, length);
        this.position += length;
    }
    
    public void writeStringSmallLength(final byte[] value) {
        final int length = value.length;
        while (this.remaining() < length + 1) {
            this.grow();
        }
        this.buf[this.position++] = (byte)length;
        System.arraycopy(value, 0, this.buf, this.position, length);
        this.position += length;
    }
    
    public void writeBytes(final byte header, final byte[] bytes) {
        final int length = bytes.length;
        while (this.remaining() < length + 10) {
            this.grow();
        }
        this.writeLength(length + 1);
        this.buf[this.position++] = header;
        System.arraycopy(bytes, 0, this.buf, this.position, length);
        this.position += length;
    }
    
    public void writeLength(final long length) {
        if (length < 251L) {
            this.buf[this.position++] = (byte)length;
        }
        else if (length < 65536L) {
            this.buf[this.position++] = -4;
            this.buf[this.position++] = (byte)length;
            this.buf[this.position++] = (byte)(length >>> 8);
        }
        else if (length < 16777216L) {
            this.buf[this.position++] = -3;
            this.buf[this.position++] = (byte)length;
            this.buf[this.position++] = (byte)(length >>> 8);
            this.buf[this.position++] = (byte)(length >>> 16);
        }
        else {
            this.buf[this.position++] = -2;
            this.buf[this.position++] = (byte)length;
            this.buf[this.position++] = (byte)(length >>> 8);
            this.buf[this.position++] = (byte)(length >>> 16);
            this.buf[this.position++] = (byte)(length >>> 24);
            this.buf[this.position++] = (byte)(length >>> 32);
            this.buf[this.position++] = (byte)(length >>> 40);
            this.buf[this.position++] = (byte)(length >>> 48);
            this.buf[this.position++] = (byte)(length >>> 54);
        }
    }
    
    private void grow() {
        int newCapacity = this.buf.length + (this.buf.length >> 1);
        if (newCapacity - 2147483639 > 0) {
            newCapacity = 2147483639;
        }
        this.buf = Arrays.copyOf(this.buf, newCapacity);
        this.limit = newCapacity;
    }
    
    public int readLongIntV1() {
        final byte[] b = this.buf;
        return (b[this.position++] & 0xFF) | (b[this.position++] & 0xFF) << 8 | (b[this.position++] & 0xFF) << 16;
    }
    
    public int readnBytes() {
        final int sw = this.buf[this.position++] & 0xFF;
        switch (sw) {
            case 1: {
                return this.buf[this.position++] & 0xFF;
            }
            case 2: {
                return this.readInt();
            }
            case 3: {
                return this.readLongIntV1();
            }
            case 4: {
                return (int)this.readLongV1();
            }
            default: {
                return 255;
            }
        }
    }
    
    public int getPosition() {
        return this.position;
    }
    
    public void setPosition(final int position) {
        this.position = position;
    }
    
    public int fastSkipLenString() {
        final long len = this.readFieldLength();
        this.position += (int)len;
        return (int)len;
    }
    
    public final long readFieldLength() {
        final int sw = this.buf[this.position++] & 0xFF;
        switch (sw) {
            case 251: {
                return -1L;
            }
            case 252: {
                return this.readIntV1();
            }
            case 253: {
                return this.readLongIntV1();
            }
            case 254: {
                return this.readLongLongV1();
            }
            default: {
                return sw;
            }
        }
    }
    
    public int getLimit() {
        return this.limit;
    }
    
    public byte[] getByteBuffer() {
        return this.buf;
    }
    
    static {
        EMPTY_BYTE_ARRAY = new byte[0];
    }
}
