// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import java.io.OutputStream;
import java.io.Serializable;

public class Lob implements Serializable
{
    protected byte[] data;
    protected transient int offset;
    protected transient int length;
    protected ObLobLocator locator;
    protected String encoding;
    protected String charData;
    protected ExceptionInterceptor exceptionInterceptor;
    
    public Lob() {
        this.data = null;
        this.locator = null;
        this.encoding = null;
        this.charData = null;
        this.exceptionInterceptor = null;
        this.data = new byte[0];
        this.offset = 0;
        this.length = 0;
    }
    
    public Lob(final byte[] bytes) {
        this.data = null;
        this.locator = null;
        this.encoding = null;
        this.charData = null;
        this.exceptionInterceptor = null;
        this.data = bytes;
        this.offset = 0;
        this.length = bytes.length;
    }
    
    public Lob(final byte[] bytes, final ExceptionInterceptor exceptionInterceptor) {
        this.data = null;
        this.locator = null;
        this.encoding = null;
        this.charData = null;
        this.exceptionInterceptor = null;
        this.data = bytes;
        this.offset = 0;
        this.length = bytes.length;
        this.exceptionInterceptor = exceptionInterceptor;
    }
    
    public Lob(final byte[] bytes, final int offset, final int length) {
        this.data = null;
        this.locator = null;
        this.encoding = null;
        this.charData = null;
        this.exceptionInterceptor = null;
        this.data = bytes;
        this.offset = offset;
        this.length = Math.min(bytes.length - offset, length);
    }
    
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
        return new LobOutputStream(this, (int)(pos - 1L) + this.offset);
    }
    
    public InputStream getBinaryStream() throws SQLException {
        return this.getBinaryStream(1L, this.length);
    }
    
    public InputStream getBinaryStream(final long pos, final long length) throws SQLException {
        if (pos < 1L) {
            throw ExceptionFactory.INSTANCE.create("Out of range (position should be > 0)");
        }
        if (pos - 1L > this.length) {
            throw ExceptionFactory.INSTANCE.create("Out of range (position > stream size)");
        }
        if (pos + length - 1L > this.length) {
            throw ExceptionFactory.INSTANCE.create("Out of range (position + length - 1 > streamSize)");
        }
        return new ByteArrayInputStream(this.data, this.offset + (int)pos - 1, (int)length);
    }
    
    public int setBytes(final long pos, final byte[] bytes) throws SQLException {
        if (pos < 1L) {
            throw ExceptionFactory.INSTANCE.create("pos should be > 0, first position is 1.");
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
    
    public void free() {
        this.data = new byte[0];
        this.offset = 0;
        this.length = 0;
    }
    
    public boolean isEmptyLob() {
        if (this.locator != null) {
            return this.locator.payloadSize == 0L;
        }
        return this.data == null || this.data.length == 0;
    }
    
    public byte[] getBytes(final long pos, final int length) throws SQLException {
        if (pos < 1L) {
            throw ExceptionFactory.INSTANCE.create(String.format("Out of range (position should be > 0, but is %s)", pos));
        }
        final int offset = this.offset + (int)(pos - 1L);
        final int len = (length > this.length) ? this.length : length;
        final byte[] result = new byte[len];
        System.arraycopy(this.data, offset, result, 0, Math.min(this.length - (int)(pos - 1L), length));
        return result;
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
}
