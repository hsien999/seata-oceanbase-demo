// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.CallableStatement;
import java.util.Arrays;
import java.io.Writer;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.Reader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class Clob implements ObClob, OutputStreamWatcher, WriterWatcher
{
    private String charData;
    private ObLobLocator locator;
    private ExceptionInterceptor exceptionInterceptor;
    private String encoding;
    private static int maxLength;
    
    Clob(final ExceptionInterceptor exceptionInterceptor) {
        this.locator = null;
        this.encoding = null;
        this.charData = "";
        this.exceptionInterceptor = exceptionInterceptor;
        this.encoding = Charset.defaultCharset().name();
    }
    
    public Clob(final String charDataInit, final ExceptionInterceptor exceptionInterceptor) {
        this.locator = null;
        this.encoding = null;
        this.charData = charDataInit;
        this.exceptionInterceptor = exceptionInterceptor;
        this.encoding = Charset.defaultCharset().name();
    }
    
    Clob(final boolean hasLocator, final byte[] data, final String encoding, final MySQLConnection conn, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        this.locator = null;
        this.encoding = null;
        Label_0441: {
            if (!hasLocator) {
                this.encoding = encoding;
                try {
                    this.charData = new String(data, this.encoding);
                    break Label_0441;
                }
                catch (UnsupportedEncodingException e) {
                    throw SQLError.createSQLException("Unsupported character encoding " + this.encoding, "S1009", this.exceptionInterceptor);
                }
            }
            if (null != data && null != conn) {
                final Buffer buffer = new Buffer(data);
                this.encoding = encoding;
                if (buffer.getBufLength() >= ObLobLocator.OB_LOG_LOCATOR_HEADER) {
                    this.locator = new ObLobLocator();
                    this.locator.magicCode = buffer.readLong();
                    this.locator.version = buffer.readLong();
                    this.locator.snapshotVersion = buffer.readLongLong();
                    this.locator.tableId = buffer.getBytes(8);
                    this.locator.columnId = buffer.readLong();
                    this.locator.flags = buffer.readInt();
                    this.locator.option = buffer.readInt();
                    this.locator.payloadOffset = buffer.readLong();
                    this.locator.payloadSize = buffer.readLong();
                    this.locator.binaryData = buffer.getByteBuffer();
                    if (this.locator.payloadSize + this.locator.payloadOffset <= buffer.getBufLength() - ObLobLocator.OB_LOG_LOCATOR_HEADER) {
                        this.locator.rowId = buffer.getBytes((int)this.locator.payloadOffset);
                        this.locator.connection = conn;
                        try {
                            if ((int)this.locator.payloadSize > Clob.maxLength) {
                                throw SQLError.createSQLException("Exceed max length of Clob for support " + Clob.maxLength + " current " + this.locator.payloadSize, "S1009", this.exceptionInterceptor);
                            }
                            this.charData = StringUtils.toString(buffer.getBytes((int)(ObLobLocator.OB_LOG_LOCATOR_HEADER + this.locator.payloadOffset), (int)this.locator.payloadSize), this.encoding);
                        }
                        catch (UnsupportedEncodingException e2) {
                            throw SQLError.createSQLException("Unsupported character encoding " + this.encoding, "S1009", this.exceptionInterceptor);
                        }
                    }
                }
            }
        }
        this.exceptionInterceptor = exceptionInterceptor;
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
    public InputStream getAsciiStream() throws SQLException {
        if (this.charData != null) {
            try {
                if (this.encoding != null) {
                    return new ByteArrayInputStream(StringUtils.getBytes(this.charData, this.encoding));
                }
                return new ByteArrayInputStream(StringUtils.getBytes(this.charData));
            }
            catch (UnsupportedEncodingException e) {
                throw SQLError.createSQLException("Unsupported character encoding " + this.encoding, "S1009", this.exceptionInterceptor);
            }
        }
        return null;
    }
    
    @Override
    public Reader getCharacterStream() throws SQLException {
        if (this.charData != null) {
            return new StringReader(this.charData);
        }
        return null;
    }
    
    @Override
    public String getSubString(final long startPos, final int length) throws SQLException {
        if (startPos < 1L) {
            throw SQLError.createSQLException(Messages.getString("Clob.6"), "S1009", this.exceptionInterceptor);
        }
        final int adjustedStartPos = (int)startPos - 1;
        int adjustedEndIndex = adjustedStartPos + length;
        if (this.charData != null) {
            if (adjustedEndIndex > this.charData.length()) {
                adjustedEndIndex = this.charData.length();
            }
            return this.charData.substring(adjustedStartPos, adjustedEndIndex);
        }
        return null;
    }
    
    @Override
    public long length() throws SQLException {
        if (this.charData != null) {
            return this.charData.length();
        }
        return 0L;
    }
    
    @Override
    public long position(final java.sql.Clob arg0, final long arg1) throws SQLException {
        return this.position(arg0.getSubString(1L, (int)arg0.length()), arg1);
    }
    
    @Override
    public long position(final String stringToFind, final long startPos) throws SQLException {
        if (startPos < 1L) {
            throw SQLError.createSQLException(Messages.getString("Clob.8") + startPos + Messages.getString("Clob.9"), "S1009", this.exceptionInterceptor);
        }
        if (this.charData == null) {
            return -1L;
        }
        if (startPos - 1L > this.charData.length()) {
            throw SQLError.createSQLException(Messages.getString("Clob.10"), "S1009", this.exceptionInterceptor);
        }
        final int pos = this.charData.indexOf(stringToFind, (int)(startPos - 1L));
        return (pos == -1) ? -1L : (pos + 1);
    }
    
    @Override
    public OutputStream setAsciiStream(final long indexToWriteAt) throws SQLException {
        if (indexToWriteAt < 1L) {
            throw SQLError.createSQLException(Messages.getString("Clob.0"), "S1009", this.exceptionInterceptor);
        }
        final WatchableOutputStream bytesOut = new WatchableOutputStream();
        bytesOut.setWatcher(this);
        if (indexToWriteAt > 0L) {
            try {
                bytesOut.write(StringUtils.getBytes(this.charData, this.encoding), 0, (int)(indexToWriteAt - 1L));
            }
            catch (UnsupportedEncodingException e) {
                throw SQLError.createSQLException("Unsupported character encoding " + this.encoding, "S1009", this.exceptionInterceptor);
            }
        }
        return bytesOut;
    }
    
    @Override
    public Writer setCharacterStream(final long indexToWriteAt) throws SQLException {
        if (indexToWriteAt < 1L) {
            throw SQLError.createSQLException(Messages.getString("Clob.1"), "S1009", this.exceptionInterceptor);
        }
        final WatchableWriter writer = new WatchableWriter();
        writer.setWatcher(this);
        if (indexToWriteAt > 1L) {
            writer.write(this.charData, 0, (int)(indexToWriteAt - 1L));
        }
        return writer;
    }
    
    @Override
    public int setString(long pos, final String str) throws SQLException {
        if (pos < 1L) {
            throw SQLError.createSQLException(Messages.getString("Clob.2"), "S1009", this.exceptionInterceptor);
        }
        if (str == null) {
            throw SQLError.createSQLException(Messages.getString("Clob.3"), "S1009", this.exceptionInterceptor);
        }
        int strLength = 0;
        if (this.locator == null) {
            final StringBuilder charBuf = new StringBuilder(this.charData);
            --pos;
            strLength = str.length();
            charBuf.replace((int)pos, (int)(pos + strLength), str);
            this.charData = charBuf.toString();
        }
        else {
            try {
                this.updateClobToServer(pos, str.getBytes(this.encoding), 0, str.length());
            }
            catch (UnsupportedEncodingException e) {
                throw SQLError.createSQLException("Unsupported character encoding " + this.encoding, "S1009", this.exceptionInterceptor);
            }
        }
        return strLength;
    }
    
    @Override
    public int setString(long pos, final String str, final int offset, final int len) throws SQLException {
        if (pos < 1L) {
            throw SQLError.createSQLException(Messages.getString("Clob.4"), "S1009", this.exceptionInterceptor);
        }
        if (str == null) {
            throw SQLError.createSQLException(Messages.getString("Clob.5"), "S1009", this.exceptionInterceptor);
        }
        final StringBuilder charBuf = new StringBuilder(this.charData);
        if (this.locator == null) {
            --pos;
            try {
                final String replaceString = str.substring(offset, offset + len);
                charBuf.replace((int)pos, (int)(pos + replaceString.length()), replaceString);
            }
            catch (StringIndexOutOfBoundsException e) {
                throw SQLError.createSQLException(e.getMessage(), "S1009", e, this.exceptionInterceptor);
            }
            this.charData = charBuf.toString();
        }
        else {
            try {
                this.updateClobToServer(pos, str.getBytes(this.encoding), offset, len);
            }
            catch (UnsupportedEncodingException e2) {
                throw SQLError.createSQLException("Unsupported character encoding " + this.encoding, "S1009", this.exceptionInterceptor);
            }
        }
        return len;
    }
    
    @Override
    public void streamClosed(final WatchableOutputStream out) {
        final int streamSize = out.size();
        if (streamSize < this.charData.length()) {
            try {
                out.write(StringUtils.getBytes(this.charData, null, null, false, null, this.exceptionInterceptor), streamSize, this.charData.length() - streamSize);
            }
            catch (SQLException ex) {}
        }
        this.charData = StringUtils.toAsciiString(out.toByteArray());
    }
    
    @Override
    public void truncate(final long length) throws SQLException {
        if (length > this.charData.length()) {
            throw SQLError.createSQLException(Messages.getString("Clob.11") + this.charData.length() + Messages.getString("Clob.12") + length + Messages.getString("Clob.13"), this.exceptionInterceptor);
        }
        if (this.locator == null) {
            this.charData = this.charData.substring(0, (int)length);
        }
        else {
            this.trimBlobToServer((int)length);
        }
    }
    
    public void writerClosed(final char[] charDataBeingWritten) {
        this.charData = new String(charDataBeingWritten);
    }
    
    @Override
    public void writerClosed(final WatchableWriter out) {
        final int dataLength = out.size();
        if (dataLength < this.charData.length()) {
            out.write(this.charData, dataLength, this.charData.length() - dataLength);
        }
        this.charData = out.toString();
    }
    
    @Override
    public void free() throws SQLException {
        this.charData = null;
    }
    
    @Override
    public Reader getCharacterStream(final long pos, final long length) throws SQLException {
        return new StringReader(this.getSubString(pos, (int)length));
    }
    
    @Override
    public synchronized void updateClobToServer(final long writeAt, final byte[] bytes, final int offset, final int length) throws SQLException {
        if (this.locator == null || this.locator.connection == null) {
            throw SQLError.createSQLException("Invalid operation on closed CLOB", "S1009", this.exceptionInterceptor);
        }
        final CallableStatement cstmt = this.locator.connection.prepareCall("{call DBMS_LOB.write( ?, ?, ?, ?)}");
        cstmt.setClob(1, this);
        cstmt.setInt(2, length);
        cstmt.setInt(3, (int)writeAt);
        try {
            cstmt.setString(4, StringUtils.toString(bytes, this.encoding));
        }
        catch (UnsupportedEncodingException e) {
            throw SQLError.createSQLException(e.getMessage(), "42S22", e, this.exceptionInterceptor);
        }
        cstmt.registerOutParameter(1, 2005);
        cstmt.execute();
        final Clob r = (Clob)cstmt.getClob(1);
        if (r == null || r.getLocator() == null) {
            throw SQLError.createSQLException("Invalid operation on closed CLOB", "S1009", this.exceptionInterceptor);
        }
        this.setLocator(r.locator);
        final int from = (int)(ObLobLocator.OB_LOG_LOCATOR_HEADER + this.locator.payloadOffset);
        final int to = (int)(from + this.locator.payloadSize);
        try {
            this.charData = new String(Arrays.copyOfRange(r.locator.binaryData, from, to), this.encoding);
        }
        catch (UnsupportedEncodingException e2) {
            throw SQLError.createSQLException("Unsupported character encoding " + this.encoding, "S1009", this.exceptionInterceptor);
        }
    }
    
    @Override
    public synchronized void trimBlobToServer(final int len) throws SQLException {
        if (this.locator == null || this.locator.connection == null) {
            throw SQLError.createSQLException("Invalid operation on closed CLOB", "S1009", this.exceptionInterceptor);
        }
        final CallableStatement cstmt = this.locator.connection.prepareCall("{call DBMS_LOB.trim( ?, ?)}");
        cstmt.setClob(1, this);
        cstmt.setInt(2, len);
        cstmt.registerOutParameter(1, 2005);
        cstmt.execute();
        final Clob r = (Clob)cstmt.getClob(1);
        if (r == null || r.getLocator() == null) {
            throw SQLError.createSQLException("Invalid operation on closed CLOB", "S1009", this.exceptionInterceptor);
        }
        this.setLocator(r.locator);
        final int from = (int)(ObLobLocator.OB_LOG_LOCATOR_HEADER + this.locator.payloadOffset);
        final int to = (int)(from + this.locator.payloadSize);
        try {
            this.charData = new String(Arrays.copyOfRange(r.locator.binaryData, from, to), this.encoding);
        }
        catch (UnsupportedEncodingException e) {
            throw SQLError.createSQLException("Unsupported character encoding " + this.encoding, "S1009", this.exceptionInterceptor);
        }
    }
    
    static {
        Clob.maxLength = 1073741824;
    }
}
