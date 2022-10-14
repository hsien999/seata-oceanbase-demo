// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.CallableStatement;
import java.util.Arrays;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.CharacterCodingException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.io.StringReader;
import java.io.Reader;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.nio.charset.Charset;

public class Clob extends Lob implements ObClob
{
    private static final long serialVersionUID = -3066501059817815286L;
    private static int maxLength;
    
    public Clob(final byte[] bytes) {
        super(bytes);
        this.encoding = Charset.defaultCharset().name();
    }
    
    public static Clob getEmptyCLOB() throws SQLException {
        final byte[] emptyData = new byte[40];
        emptyData[0] = 33;
        emptyData[1] = 66;
        emptyData[2] = 79;
        emptyData[3] = 76;
        emptyData[4] = 1;
        return new Clob(true, emptyData, Charset.defaultCharset().name(), null);
    }
    
    public Clob(final byte[] bytes, final ExceptionInterceptor exceptionInterceptor) {
        super(bytes, exceptionInterceptor);
        this.encoding = Charset.defaultCharset().name();
    }
    
    public Clob(final byte[] bytes, final int offset, final int length) {
        super(bytes, offset, length);
        this.encoding = Charset.defaultCharset().name();
    }
    
    public Clob(final String charDataInit, final ExceptionInterceptor exceptionInterceptor) {
        this.charData = charDataInit;
        this.exceptionInterceptor = exceptionInterceptor;
        this.encoding = Charset.defaultCharset().name();
    }
    
    public Clob(final boolean hasLocator, final byte[] data, final String encoding, final OceanBaseConnection conn) throws SQLException {
        if (!hasLocator) {
            this.encoding = encoding;
            try {
                this.charData = new String(data, this.encoding);
                return;
            }
            catch (UnsupportedEncodingException e) {
                throw new SQLException("Unsupported character encoding");
            }
        }
        if (null != data) {
            final Buffer buffer = new Buffer(data);
            this.encoding = encoding;
            if (buffer.getLimit() >= ObLobLocator.OB_LOG_LOCATOR_HEADER) {
                this.locator = new ObLobLocator();
                this.locator.magicCode = buffer.readLongV1();
                this.locator.version = buffer.readLongV1();
                this.locator.snapshotVersion = buffer.readLongLongV1();
                this.locator.tableId = buffer.getBytes(8);
                this.locator.columnId = buffer.readLongV1();
                this.locator.flags = buffer.readIntV1();
                this.locator.option = buffer.readIntV1();
                this.locator.payloadOffset = buffer.readLongV1();
                this.locator.payloadSize = buffer.readLongV1();
                this.locator.binaryData = buffer.getByteBuffer();
                if (this.locator.payloadSize + this.locator.payloadOffset <= buffer.getLimit() - ObLobLocator.OB_LOG_LOCATOR_HEADER && conn != null) {
                    this.locator.rowId = buffer.getBytes((int)this.locator.payloadOffset);
                    this.locator.connection = conn;
                    try {
                        if ((int)this.locator.payloadSize > Clob.maxLength) {
                            throw new SQLException("Exceed max length of Clob for support " + Clob.maxLength + " current " + this.locator.payloadSize);
                        }
                        this.charData = new String(buffer.getBytes((int)(ObLobLocator.OB_LOG_LOCATOR_HEADER + this.locator.payloadOffset), (int)this.locator.payloadSize), this.encoding);
                    }
                    catch (UnsupportedEncodingException e2) {
                        throw new SQLException("Unsupported character encoding " + this.encoding);
                    }
                }
            }
        }
    }
    
    public Clob() {
        this.encoding = Charset.defaultCharset().name();
    }
    
    @Override
    public synchronized ObLobLocator getLocator() {
        return this.locator;
    }
    
    @Override
    public synchronized void setLocator(final ObLobLocator locator) {
        if (this.locator == null) {
            this.locator = new ObLobLocator();
        }
        this.locator.columnId = locator.columnId;
        this.locator.flags = locator.flags;
        this.locator.magicCode = locator.magicCode;
        this.locator.option = locator.option;
        this.locator.snapshotVersion = locator.snapshotVersion;
        this.locator.tableId = locator.tableId;
        this.locator.rowId = locator.rowId;
        this.locator.version = locator.version;
        this.locator.payloadOffset = locator.payloadOffset;
        this.locator.payloadSize = locator.payloadSize;
        this.locator.binaryData = locator.binaryData;
    }
    
    @Override
    public String toString() {
        if (this.charData != null) {
            return this.charData;
        }
        final Charset charset = Charset.forName(this.encoding);
        return new String(this.data, this.offset, this.length, charset);
    }
    
    @Override
    public String getSubString(final long pos, final int length) throws SQLException {
        if (pos < 1L) {
            throw ExceptionFactory.INSTANCE.create("position must be >= 1");
        }
        if (length < 0) {
            throw ExceptionFactory.INSTANCE.create("length must be > 0");
        }
        final int adjustedStartPos = (int)pos - 1;
        int adjustedEndIndex = adjustedStartPos + length;
        if (this.charData != null) {
            if (adjustedEndIndex > this.charData.length()) {
                adjustedEndIndex = this.charData.length();
            }
            return this.charData.substring(adjustedStartPos, adjustedEndIndex);
        }
        try {
            final String val = this.toString();
            return val.substring((int)pos - 1, Math.min((int)pos - 1 + length, val.length()));
        }
        catch (Exception e) {
            throw new SQLException(e);
        }
    }
    
    @Override
    public Reader getCharacterStream() {
        if (this.charData != null) {
            return new StringReader(this.charData);
        }
        return new StringReader(this.toString());
    }
    
    @Override
    public Reader getCharacterStream(final long pos, final long length) throws SQLException {
        final String val = this.toString();
        if (val.length() < (int)pos - 1 + length) {
            throw ExceptionFactory.INSTANCE.create("pos + length is greater than the number of characters in the Clob");
        }
        final String sub = val.substring((int)pos - 1, (int)pos - 1 + (int)length);
        return new StringReader(sub);
    }
    
    @Override
    public Writer setCharacterStream(final long pos) throws SQLException {
        final int bytePosition = this.utf8Position((int)pos - 1);
        final OutputStream stream = this.setBinaryStream(bytePosition + 1);
        try {
            return new OutputStreamWriter(stream, this.encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new SQLException("Unsupported character encoding " + this.encoding);
        }
    }
    
    @Override
    public InputStream getAsciiStream() throws SQLException {
        if (this.charData != null) {
            try {
                if (this.encoding != null) {
                    return new ByteArrayInputStream(this.charData.getBytes(this.encoding));
                }
                return new ByteArrayInputStream(this.charData.getBytes());
            }
            catch (UnsupportedEncodingException e) {
                throw new SQLException("Unsupported character encoding " + this.encoding);
            }
        }
        return this.getBinaryStream();
    }
    
    @Override
    public long position(final String searchStr, final long start) {
        return this.toString().indexOf(searchStr, (int)start - 1) + 1;
    }
    
    @Override
    public long position(final java.sql.Clob searchStr, final long start) {
        return this.position(searchStr.toString(), start);
    }
    
    private int utf8Position(final int charPosition) {
        int pos = this.offset;
        for (int i = 0; i < charPosition; ++i) {
            final int byteValue = this.data[pos] & 0xFF;
            if (byteValue < 128) {
                ++pos;
            }
            else {
                if (byteValue < 194) {
                    throw new UncheckedIOException("invalid UTF8", new CharacterCodingException());
                }
                if (byteValue < 224) {
                    pos += 2;
                }
                else if (byteValue < 240) {
                    pos += 3;
                }
                else {
                    if (byteValue >= 248) {
                        throw new UncheckedIOException("invalid UTF8", new CharacterCodingException());
                    }
                    pos += 4;
                }
            }
        }
        return pos;
    }
    
    @Override
    public int setString(final long pos, final String str) throws SQLException {
        if (this.locator != null) {
            try {
                this.updateClobToServer(pos, str.getBytes(this.encoding), 0, str.length());
                return 0;
            }
            catch (UnsupportedEncodingException e) {
                throw new SQLException("Unsupported character encoding " + this.encoding);
            }
        }
        final int bytePosition = this.utf8Position((int)pos - 1);
        try {
            super.setBytes(bytePosition + 1 - this.offset, str.getBytes(this.encoding));
        }
        catch (UnsupportedEncodingException e2) {
            throw new SQLException("Unsupported character encoding " + this.encoding);
        }
        return str.length();
    }
    
    @Override
    public int setString(final long pos, final String str, final int offset, final int len) throws SQLException {
        return this.setString(pos, str.substring(offset, offset + len));
    }
    
    @Override
    public OutputStream setAsciiStream(final long pos) throws SQLException {
        return this.setBinaryStream(this.utf8Position((int)pos - 1) + 1);
    }
    
    @Override
    public long length() {
        if (this.charData != null) {
            return this.charData.length();
        }
        long len;
        int pos;
        for (len = 0L, pos = this.offset; len < this.length && this.data[pos] >= 0; ++len, ++pos) {}
        while (pos < this.offset + this.length) {
            final byte firstByte = this.data[pos++];
            if (firstByte < 0) {
                if (firstByte >> 5 != -2 || (firstByte & 0x1E) == 0x0) {
                    if (firstByte >> 4 == -2) {
                        if (pos + 1 >= this.offset + this.length) {
                            throw new UncheckedIOException("invalid UTF8", new CharacterCodingException());
                        }
                        pos += 2;
                        ++len;
                    }
                    else {
                        if (firstByte >> 3 != -2) {
                            throw new UncheckedIOException("invalid UTF8", new CharacterCodingException());
                        }
                        if (pos + 2 < this.offset + this.length) {
                            pos += 3;
                            len += 2L;
                        }
                        else {
                            pos += this.offset + this.length;
                            ++len;
                        }
                    }
                }
                else {
                    ++pos;
                    ++len;
                }
            }
            else {
                ++len;
            }
        }
        return len;
    }
    
    @Override
    public void truncate(final long truncateLen) throws SQLException {
        if (this.locator == null) {
            long len;
            int pos;
            for (len = 0L, pos = this.offset; len < this.length && len < truncateLen && this.data[pos] >= 0; ++len, ++pos) {}
            while (pos < this.offset + this.length && len < truncateLen) {
                final byte firstByte = this.data[pos++];
                if (firstByte < 0) {
                    if (firstByte >> 5 != -2 || (firstByte & 0x1E) == 0x0) {
                        if (firstByte >> 4 == -2) {
                            if (pos + 1 >= this.offset + this.length) {
                                throw new UncheckedIOException("invalid UTF8", new CharacterCodingException());
                            }
                            pos += 2;
                            ++len;
                        }
                        else {
                            if (firstByte >> 3 != -2) {
                                throw new UncheckedIOException("invalid UTF8", new CharacterCodingException());
                            }
                            if (pos + 2 >= this.offset + this.length) {
                                throw new UncheckedIOException("invalid UTF8", new CharacterCodingException());
                            }
                            if (len + 2L <= truncateLen) {
                                pos += 3;
                                len += 2L;
                            }
                            else {
                                ++pos;
                                len = truncateLen;
                            }
                        }
                    }
                    else {
                        ++pos;
                        ++len;
                    }
                }
                else {
                    ++len;
                }
            }
            this.length = pos - this.offset;
            return;
        }
        if (this.length > this.charData.length()) {
            throw new SQLException("Clob length is more than charData length");
        }
        if (this.locator == null) {
            this.charData = this.charData.substring(0, (int)truncateLen);
        }
        else {
            this.trimClobToServer((int)truncateLen);
        }
    }
    
    @Override
    public synchronized void updateClobToServer(final long writeAt, final byte[] bytes, final int offset, final int length) throws SQLException {
        if (this.locator == null || this.locator.connection == null) {
            throw new SQLException("Invalid operation on closed CLOB");
        }
        final CallableStatement cstmt = this.locator.connection.prepareCall("{call DBMS_LOB.write( ?, ?, ?, ?)}");
        cstmt.setClob(1, this);
        cstmt.setInt(2, length);
        cstmt.setInt(3, (int)writeAt);
        try {
            cstmt.setString(4, new String(bytes, this.encoding));
        }
        catch (UnsupportedEncodingException e) {
            throw new SQLException(e.getMessage());
        }
        cstmt.registerOutParameter(1, 2005);
        cstmt.execute();
        final Clob r = (Clob)cstmt.getClob(1);
        if (r == null || r.getLocator() == null) {
            throw new SQLException("Invalid operation on closed CLOB");
        }
        this.setLocator(r.locator);
        this.data = r.data;
        final int from = (int)(ObLobLocator.OB_LOG_LOCATOR_HEADER + this.locator.payloadOffset);
        final int to = (int)(from + this.locator.payloadSize);
        this.encoding = r.encoding;
        try {
            this.charData = new String(Arrays.copyOfRange(r.locator.binaryData, from, to), this.encoding);
        }
        catch (UnsupportedEncodingException e2) {
            throw new SQLException("Unsupported character encoding " + this.encoding);
        }
    }
    
    public synchronized void trimClobToServer(final int len) throws SQLException {
        if (this.locator == null || this.locator.connection == null) {
            throw new SQLException("Invalid operation on closed CLOB");
        }
        final CallableStatement cstmt = this.locator.connection.prepareCall("{call DBMS_LOB.trim( ?, ?)}");
        cstmt.setClob(1, this);
        cstmt.setInt(2, len);
        cstmt.registerOutParameter(1, 2005);
        cstmt.execute();
        final Clob r = (Clob)cstmt.getClob(1);
        if (r == null || r.getLocator() == null) {
            throw new SQLException("Invalid operation on closed CLOB");
        }
        this.setLocator(r.locator);
        this.data = r.data;
        final int from = (int)(ObLobLocator.OB_LOG_LOCATOR_HEADER + this.locator.payloadOffset);
        final int to = (int)(from + this.locator.payloadSize);
        this.encoding = r.encoding;
        try {
            this.charData = new String(Arrays.copyOfRange(r.locator.binaryData, from, to), this.encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw new SQLException("Unsupported character encoding " + this.encoding);
        }
    }
    
    static {
        Clob.maxLength = 1073741824;
    }
}
