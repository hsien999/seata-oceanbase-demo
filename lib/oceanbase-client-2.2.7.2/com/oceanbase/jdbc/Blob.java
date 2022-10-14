// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.CallableStatement;
import java.io.OutputStream;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.io.Reader;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.sql.SQLException;

public class Blob extends Lob implements ObBlob
{
    private static final long serialVersionUID = -4736603161284649490L;
    
    public Blob() {
        this.data = new byte[0];
        this.offset = 0;
        this.length = 0;
    }
    
    public static Blob getEmptyBLOB() throws SQLException {
        final byte[] emptyData = new byte[40];
        emptyData[0] = 33;
        emptyData[1] = 66;
        emptyData[2] = 79;
        emptyData[3] = 76;
        emptyData[4] = 1;
        return new Blob(true, emptyData, null, null);
    }
    
    public Blob(final Blob other) {
        this.data = other.data;
        this.offset = other.offset;
        this.length = other.length;
    }
    
    public Blob(final byte[] bytes) {
        this.data = bytes;
        this.offset = 0;
        this.length = bytes.length;
    }
    
    public Blob(final byte[] bytes, final ExceptionInterceptor exceptionInterceptor) {
        this.data = bytes;
        this.offset = 0;
        this.length = bytes.length;
        this.exceptionInterceptor = exceptionInterceptor;
    }
    
    public Blob(final byte[] bytes, final int offset, final int length) {
        this.data = bytes;
        this.offset = offset;
        this.length = Math.min(bytes.length - offset, length);
    }
    
    public Blob(final boolean hasLocator, final byte[] data, final String encoding, final OceanBaseConnection conn) {
        if (null != conn) {
            this.encoding = "UTF-8";
        }
        else if (null != encoding) {
            this.encoding = encoding;
        }
        if (null != data) {
            final Buffer buffer = new Buffer(data);
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
                    this.data = buffer.getBytes((int)this.locator.payloadSize);
                    this.length = (int)this.locator.payloadSize;
                    this.locator.connection = conn;
                }
            }
        }
    }
    
    private void writeObject(final ObjectOutputStream out) throws IOException {
        out.writeInt(this.offset);
        out.writeInt(this.length);
        out.defaultWriteObject();
    }
    
    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        this.offset = in.readInt();
        this.length = in.readInt();
        in.defaultReadObject();
    }
    
    @Override
    public long length() {
        return this.length;
    }
    
    public Reader getCharacterStream() throws SQLException {
        if (this.data != null) {
            return new StringReader(new String(this.data, Charset.forName(this.encoding)));
        }
        return null;
    }
    
    @Override
    public long position(final byte[] pattern, final long start) throws SQLException {
        if (pattern.length == 0) {
            return 0L;
        }
        if (start < 1L) {
            throw ExceptionFactory.INSTANCE.create(String.format("Out of range (position should be > 0, but is %s)", start));
        }
        if (start > this.length) {
            throw ExceptionFactory.INSTANCE.create("Out of range (start > stream size)");
        }
        int i = (int)(this.offset + start - 1L);
    Label_0067:
        while (i <= this.offset + this.length - pattern.length) {
            for (int j = 0; j < pattern.length; ++j) {
                if (this.data[i + j] != pattern[j]) {
                    ++i;
                    continue Label_0067;
                }
            }
            return i + 1 - this.offset;
        }
        return -1L;
    }
    
    @Override
    public long position(final java.sql.Blob pattern, final long start) throws SQLException {
        final byte[] blobBytes = pattern.getBytes(1L, (int)pattern.length());
        return this.position(blobBytes, start);
    }
    
    @Override
    public int setBytes(final long pos, final byte[] bytes) throws SQLException {
        if (pos < 1L) {
            throw ExceptionFactory.INSTANCE.create("pos should be > 0, first position is 1.");
        }
        if (this.locator != null) {
            this.updateBlobToServer(pos, bytes, this.offset, bytes.length);
            return bytes.length;
        }
        final int arrayPos = (int)pos - 1;
        if (this.length > arrayPos + bytes.length) {
            System.arraycopy(bytes, 0, this.data, this.offset + arrayPos, bytes.length);
        }
        else {
            final byte[] newContent = new byte[arrayPos + bytes.length];
            if (Math.min(arrayPos, this.length) > 0) {
                System.arraycopy(this.data, this.offset, newContent, 0, Math.min(arrayPos, this.length));
            }
            System.arraycopy(bytes, 0, newContent, arrayPos, bytes.length);
            this.data = newContent;
            this.length = arrayPos + bytes.length;
            this.offset = 0;
            this.charData = new String(this.data, Charset.forName((this.encoding == null) ? "UTF-8" : this.encoding));
        }
        return bytes.length;
    }
    
    @Override
    public int setBytes(final long pos, final byte[] bytes, final int offset, final int len) throws SQLException {
        if (pos < 1L) {
            throw ExceptionFactory.INSTANCE.create("pos should be > 0, first position is 1.");
        }
        if (this.locator != null) {
            this.updateBlobToServer(pos, bytes, offset, len);
            return len;
        }
        final int arrayPos = (int)pos - 1;
        final int byteToWrite = Math.min(bytes.length - offset, len);
        if (this.length > arrayPos + byteToWrite) {
            System.arraycopy(bytes, offset, this.data, this.offset + arrayPos, byteToWrite);
        }
        else {
            final byte[] newContent = new byte[arrayPos + byteToWrite];
            if (Math.min(arrayPos, this.length) > 0) {
                System.arraycopy(this.data, this.offset, newContent, 0, Math.min(arrayPos, this.length));
            }
            System.arraycopy(bytes, offset, newContent, arrayPos, byteToWrite);
            this.data = newContent;
            this.length = arrayPos + byteToWrite;
            this.offset = 0;
            this.charData = new String(this.data, Charset.forName((this.encoding == null) ? "UTF-8" : this.encoding));
        }
        return byteToWrite;
    }
    
    @Override
    public OutputStream setBinaryStream(final long pos) throws SQLException {
        if (pos < 1L) {
            throw ExceptionFactory.INSTANCE.create("Invalid position in blob");
        }
        if (this.offset > 0) {
            final byte[] tmp = new byte[this.length];
            System.arraycopy(this.data, this.offset, tmp, 0, this.length);
            this.data = tmp;
            this.offset = 0;
        }
        return new BlobOutputStream(this, (int)(pos - 1L) + this.offset);
    }
    
    @Override
    public void truncate(final long len) throws SQLException {
        if (this.locator != null) {
            this.trimBlobToServer((int)len);
        }
        else if (len >= 0L && len < this.length) {
            this.length = (int)len;
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
            throw new SQLException("Invalid operation on closed BLOB");
        }
        final CallableStatement cstmt = this.locator.connection.prepareCall("{call DBMS_LOB.write( ?, ?, ?, ?)}");
        ((BasePrepareStatement)cstmt).setLobLocator(1, this);
        cstmt.setInt(2, length);
        cstmt.setInt(3, (int)writeAt);
        cstmt.setBytes(4, bytes);
        cstmt.registerOutParameter(1, 2004);
        cstmt.execute();
        final Blob r = (Blob)cstmt.getBlob(1);
        if (r == null || r.getLocator() == null) {
            throw new SQLException("Invalid operator on setBytes for BLOB");
        }
        this.setLocator(r.locator);
        this.data = r.data;
    }
    
    @Override
    public synchronized void trimBlobToServer(final int len) throws SQLException {
        if (this.locator == null || this.locator.connection == null) {
            throw new SQLException("Invalid operation on closed BLOB");
        }
        final CallableStatement cstmt = this.locator.connection.prepareCall("{call DBMS_LOB.trim( ?, ?)}");
        ((BasePrepareStatement)cstmt).setLobLocator(1, this);
        cstmt.setInt(2, len);
        cstmt.registerOutParameter(1, 2004);
        cstmt.execute();
        final Blob r = (Blob)cstmt.getBlob(1);
        if (r == null || r.getLocator() == null) {
            throw new SQLException("Invalid operator on trim() for BLOB");
        }
        this.setLocator(r.locator);
        this.data = r.data;
    }
}
