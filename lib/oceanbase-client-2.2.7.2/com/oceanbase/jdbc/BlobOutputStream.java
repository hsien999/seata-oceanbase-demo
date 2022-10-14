// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.io.IOException;
import java.io.OutputStream;

class BlobOutputStream extends OutputStream
{
    private final Blob blob;
    private int pos;
    
    public BlobOutputStream(final Blob blob, final int pos) {
        this.blob = blob;
        this.pos = pos;
    }
    
    @Override
    public void write(final int bit) throws IOException {
        if (this.pos >= this.blob.length) {
            final byte[] tmp = new byte[2 * this.blob.length + 1];
            System.arraycopy(this.blob.data, this.blob.offset, tmp, 0, this.blob.length);
            this.blob.data = tmp;
            this.pos -= this.blob.offset;
            this.blob.offset = 0;
            final Blob blob = this.blob;
            ++blob.length;
        }
        this.blob.data[this.pos] = (byte)bit;
        ++this.pos;
    }
    
    @Override
    public void write(final byte[] buf, final int off, final int len) throws IOException {
        if (off < 0) {
            throw new IOException("Invalid offset " + off);
        }
        final int realLen = Math.min(buf.length - off, len);
        if (this.pos + realLen >= this.blob.length) {
            final int newLen = 2 * this.blob.length + realLen;
            final byte[] tmp = new byte[newLen];
            System.arraycopy(this.blob.data, this.blob.offset, tmp, 0, this.blob.length);
            this.blob.data = tmp;
            this.pos -= this.blob.offset;
            this.blob.offset = 0;
            this.blob.length = this.pos + realLen;
        }
        System.arraycopy(buf, off, this.blob.data, this.pos, realLen);
        this.pos += realLen;
    }
    
    @Override
    public void write(final byte[] buf) throws IOException {
        this.write(buf, 0, buf.length);
    }
}
