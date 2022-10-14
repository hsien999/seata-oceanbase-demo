// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.io.IOException;
import java.io.OutputStream;

public class LobOutputStream extends OutputStream
{
    private final Lob lob;
    private int pos;
    
    public LobOutputStream(final Lob lob, final int pos) {
        this.lob = lob;
        this.pos = pos;
    }
    
    @Override
    public void write(final int bit) throws IOException {
        if (this.pos >= this.lob.length) {
            final byte[] tmp = new byte[2 * this.lob.length + 1];
            System.arraycopy(this.lob.data, this.lob.offset, tmp, 0, this.lob.length);
            this.lob.data = tmp;
            this.pos -= this.lob.offset;
            this.lob.offset = 0;
            final Lob lob = this.lob;
            ++lob.length;
        }
        this.lob.data[this.pos] = (byte)bit;
        ++this.pos;
    }
    
    @Override
    public void write(final byte[] buf, final int off, final int len) throws IOException {
        if (off < 0) {
            throw new IOException("Invalid offset " + off);
        }
        final int realLen = Math.min(buf.length - off, len);
        if (this.pos + realLen >= this.lob.length) {
            final int newLen = 2 * this.lob.length + realLen;
            final byte[] tmp = new byte[newLen];
            System.arraycopy(this.lob.data, this.lob.offset, tmp, 0, this.lob.length);
            this.lob.data = tmp;
            this.pos -= this.lob.offset;
            this.lob.offset = 0;
            this.lob.length = this.pos + realLen;
        }
        System.arraycopy(buf, off, this.lob.data, this.pos, realLen);
        this.pos += realLen;
    }
    
    @Override
    public void write(final byte[] buf) throws IOException {
        this.write(buf, 0, buf.length);
    }
}
