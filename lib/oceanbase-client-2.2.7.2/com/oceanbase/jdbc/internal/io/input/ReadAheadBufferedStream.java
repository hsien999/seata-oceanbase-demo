// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.input;

import java.io.IOException;
import java.io.InputStream;
import java.io.FilterInputStream;

public class ReadAheadBufferedStream extends FilterInputStream
{
    private static final int BUF_SIZE = 16384;
    private volatile byte[] buf;
    private int end;
    private int pos;
    
    public ReadAheadBufferedStream(final InputStream in) {
        super(in);
        this.buf = new byte[16384];
    }
    
    @Override
    public synchronized int read() throws IOException {
        if (this.pos >= this.end) {
            this.fillBuffer(1);
            if (this.pos >= this.end) {
                return -1;
            }
        }
        return this.buf[this.pos++] & 0xFF;
    }
    
    @Override
    public synchronized int read(final byte[] externalBuf, final int off, final int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        int totalReads = 0;
        while (true) {
            if (this.end - this.pos <= 0) {
                if (len - totalReads >= this.buf.length) {
                    final int reads = super.read(externalBuf, off + totalReads, len - totalReads);
                    if (reads <= 0) {
                        return (totalReads == 0) ? -1 : totalReads;
                    }
                    return totalReads + reads;
                }
                else {
                    this.fillBuffer(len - totalReads);
                    if (this.end <= 0) {
                        return (totalReads == 0) ? -1 : totalReads;
                    }
                }
            }
            final int copyLength = Math.min(len - totalReads, this.end - this.pos);
            System.arraycopy(this.buf, this.pos, externalBuf, off + totalReads, copyLength);
            this.pos += copyLength;
            totalReads += copyLength;
            if (totalReads >= len || super.available() <= 0) {
                return totalReads;
            }
        }
    }
    
    private void fillBuffer(final int minNeededBytes) throws IOException {
        final int lengthToReallyRead = Math.min(16384, Math.max(super.available(), minNeededBytes));
        this.end = super.read(this.buf, 0, lengthToReallyRead);
        this.pos = 0;
    }
    
    @Override
    public synchronized long skip(final long n) throws IOException {
        throw new IOException("Skip from socket not implemented");
    }
    
    @Override
    public synchronized int available() throws IOException {
        throw new IOException("available from socket not implemented");
    }
    
    @Override
    public synchronized void reset() throws IOException {
        throw new IOException("reset from socket not implemented");
    }
    
    @Override
    public boolean markSupported() {
        return false;
    }
    
    @Override
    public void close() throws IOException {
        super.close();
    }
}
