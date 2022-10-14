// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.CallableStatement;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.io.Reader;
import java.sql.SQLException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class Blob implements ObBlob, OutputStreamWatcher
{
    private byte[] binaryData;
    private ObLobLocator locator;
    private boolean isClosed;
    private String encoding;
    private ExceptionInterceptor exceptionInterceptor;
    
    Blob(final ExceptionInterceptor exceptionInterceptor) {
        this.binaryData = null;
        this.locator = null;
        this.isClosed = false;
        this.encoding = null;
        this.setBinaryData(Constants.EMPTY_BYTE_ARRAY);
        this.exceptionInterceptor = exceptionInterceptor;
    }
    
    public Blob(final byte[] data, final ExceptionInterceptor exceptionInterceptor) {
        this.binaryData = null;
        this.locator = null;
        this.isClosed = false;
        this.encoding = null;
        this.setBinaryData(data);
        this.exceptionInterceptor = exceptionInterceptor;
    }
    
    Blob(final boolean hasLocator, final byte[] data, final String encoding, final MySQLConnection conn, final ExceptionInterceptor exceptionInterceptor) {
        this.binaryData = null;
        this.locator = null;
        this.isClosed = false;
        this.encoding = null;
        if (null != conn) {
            this.encoding = conn.getEncoding();
        }
        else if (null != encoding) {
            this.encoding = encoding;
        }
        if (!hasLocator) {
            this.setBinaryData(data);
        }
        else if (null != data && null != conn) {
            final Buffer buffer = new Buffer(data);
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
                    this.setBinaryData(buffer.getBytes((int)this.locator.payloadSize));
                    this.locator.connection = conn;
                }
            }
        }
        this.exceptionInterceptor = exceptionInterceptor;
    }
    
    Blob(final byte[] data, final ResultSetInternalMethods creatorResultSetToSet, final int columnIndexToSet) {
        this.binaryData = null;
        this.locator = null;
        this.isClosed = false;
        this.encoding = null;
        this.setBinaryData(data);
    }
    
    private synchronized byte[] getBinaryData() {
        return this.binaryData;
    }
    
    @Override
    public synchronized InputStream getBinaryStream() throws SQLException {
        this.checkClosed();
        return new ByteArrayInputStream(this.getBinaryData());
    }
    
    public Reader getCharacterStream() throws SQLException {
        if (this.binaryData != null) {
            return new StringReader(new String(this.binaryData, Charset.forName(this.encoding)));
        }
        return null;
    }
    
    @Override
    public synchronized byte[] getBytes(long pos, int length) throws SQLException {
        this.checkClosed();
        if (pos < 1L) {
            throw SQLError.createSQLException(Messages.getString("Blob.2"), "S1009", this.exceptionInterceptor);
        }
        --pos;
        if (pos > this.binaryData.length) {
            throw SQLError.createSQLException("\"pos\" argument can not be larger than the BLOB's length.", "S1009", this.exceptionInterceptor);
        }
        if (pos + length > this.binaryData.length) {
            length = (int)(this.binaryData.length - pos);
        }
        final byte[] newData = new byte[length];
        System.arraycopy(this.getBinaryData(), (int)pos, newData, 0, length);
        return newData;
    }
    
    @Override
    public synchronized long length() throws SQLException {
        this.checkClosed();
        return this.getBinaryData().length;
    }
    
    @Override
    public synchronized long position(final byte[] pattern, final long start) throws SQLException {
        throw SQLError.createSQLException("Not implemented", this.exceptionInterceptor);
    }
    
    @Override
    public synchronized long position(final java.sql.Blob pattern, final long start) throws SQLException {
        this.checkClosed();
        return this.position(pattern.getBytes(0L, (int)pattern.length()), start);
    }
    
    private synchronized void setBinaryData(final byte[] newBinaryData) {
        this.binaryData = newBinaryData;
    }
    
    @Override
    public synchronized OutputStream setBinaryStream(final long indexToWriteAt) throws SQLException {
        this.checkClosed();
        if (indexToWriteAt < 1L) {
            throw SQLError.createSQLException(Messages.getString("Blob.0"), "S1009", this.exceptionInterceptor);
        }
        final WatchableOutputStream bytesOut = new WatchableOutputStream();
        bytesOut.setWatcher(this);
        if (indexToWriteAt > 0L) {
            bytesOut.write(this.binaryData, 0, (int)(indexToWriteAt - 1L));
        }
        return bytesOut;
    }
    
    @Override
    public synchronized int setBytes(final long writeAt, final byte[] bytes) throws SQLException {
        this.checkClosed();
        return this.setBytes(writeAt, bytes, 0, bytes.length);
    }
    
    @Override
    public synchronized int setBytes(final long writeAt, final byte[] bytes, final int offset, final int length) throws SQLException {
        this.checkClosed();
        OutputStream bytesOut = null;
        try {
            if (this.locator == null) {
                bytesOut = this.setBinaryStream(writeAt);
                bytesOut.write(bytes, offset, length);
            }
            else {
                this.updateBlobToServer(writeAt, bytes, offset, length);
            }
        }
        catch (IOException ioEx) {
            final SQLException sqlEx = SQLError.createSQLException(Messages.getString("Blob.1"), "S1000", this.exceptionInterceptor);
            sqlEx.initCause(ioEx);
            throw sqlEx;
        }
        finally {
            try {
                if (bytesOut != null) {
                    bytesOut.close();
                }
            }
            catch (IOException ex) {}
        }
        return length;
    }
    
    public synchronized void streamClosed(final byte[] byteData) {
        this.binaryData = byteData;
    }
    
    @Override
    public synchronized void streamClosed(final WatchableOutputStream out) {
        final int streamSize = out.size();
        if (streamSize < this.binaryData.length) {
            out.write(this.binaryData, streamSize, this.binaryData.length - streamSize);
        }
        this.binaryData = out.toByteArray();
    }
    
    @Override
    public synchronized void truncate(final long len) throws SQLException {
        this.checkClosed();
        if (len < 0L) {
            throw SQLError.createSQLException("\"len\" argument can not be < 1.", "S1009", this.exceptionInterceptor);
        }
        if (len > this.binaryData.length) {
            throw SQLError.createSQLException("\"len\" argument can not be larger than the BLOB's length.", "S1009", this.exceptionInterceptor);
        }
        if (this.locator == null) {
            final byte[] newData = new byte[(int)len];
            System.arraycopy(this.getBinaryData(), 0, newData, 0, (int)len);
            this.binaryData = newData;
        }
        else {
            this.trimBlobToServer((int)len);
        }
    }
    
    @Override
    public synchronized void free() throws SQLException {
        this.binaryData = null;
        this.isClosed = true;
        this.locator = null;
    }
    
    @Override
    public synchronized InputStream getBinaryStream(long pos, final long length) throws SQLException {
        this.checkClosed();
        if (pos < 1L) {
            throw SQLError.createSQLException("\"pos\" argument can not be < 1.", "S1009", this.exceptionInterceptor);
        }
        --pos;
        if (pos > this.binaryData.length) {
            throw SQLError.createSQLException("\"pos\" argument can not be larger than the BLOB's length.", "S1009", this.exceptionInterceptor);
        }
        if (pos + length > this.binaryData.length) {
            throw SQLError.createSQLException("\"pos\" + \"length\" arguments can not be larger than the BLOB's length.", "S1009", this.exceptionInterceptor);
        }
        return new ByteArrayInputStream(this.getBinaryData(), (int)pos, (int)length);
    }
    
    private synchronized void checkClosed() throws SQLException {
        if (this.isClosed) {
            throw SQLError.createSQLException("Invalid operation on closed BLOB", "S1009", this.exceptionInterceptor);
        }
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
    public synchronized void updateBlobToServer(final long writeAt, final byte[] bytes, final int offset, final int length) throws SQLException {
        if (this.locator == null || this.locator.connection == null) {
            throw SQLError.createSQLException("Invalid operation on closed BLOB", "S1009", this.exceptionInterceptor);
        }
        final CallableStatement cstmt = this.locator.connection.prepareCall("{call DBMS_LOB.write( ?, ?, ?, ?)}");
        cstmt.setBlob(1, this);
        cstmt.setInt(2, length);
        cstmt.setInt(3, (int)writeAt);
        cstmt.setBytes(4, bytes);
        cstmt.registerOutParameter(1, 2004);
        cstmt.execute();
        final Blob r = (Blob)cstmt.getBlob(1);
        if (r == null || r.getLocator() == null) {
            throw SQLError.createSQLException("Invalid operator on setBytes for BLOB", "02000", this.exceptionInterceptor);
        }
        this.setLocator(r.locator);
        this.binaryData = r.binaryData;
    }
    
    @Override
    public synchronized void trimBlobToServer(final int len) throws SQLException {
        if (this.locator == null || this.locator.connection == null) {
            throw SQLError.createSQLException("Invalid operation on closed BLOB", "S1009", this.exceptionInterceptor);
        }
        final CallableStatement cstmt = this.locator.connection.prepareCall("{call DBMS_LOB.trim( ?, ?)}");
        cstmt.setBlob(1, this);
        cstmt.setInt(2, len);
        cstmt.registerOutParameter(1, 2004);
        cstmt.execute();
        final Blob r = (Blob)cstmt.getBlob(1);
        if (r == null || r.getLocator() == null) {
            throw SQLError.createSQLException("Invalid operator on trim() for BLOB", "02000", this.exceptionInterceptor);
        }
        this.setLocator(r.locator);
        this.binaryData = r.binaryData;
    }
}
