// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.util.Utils;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.io.InputStream;

public class OBStreamParameter implements Cloneable, LongDataParameterHolder
{
    private final InputStream is;
    private final long length;
    private final boolean noBackslashEscapes;
    private final boolean hasLobLocator;
    private final byte[] bytes;
    
    public OBStreamParameter(final boolean hasLobLocator, final InputStream is, final long length, final boolean noBackslashEscapes) {
        this.is = is;
        this.length = length;
        this.noBackslashEscapes = noBackslashEscapes;
        this.hasLobLocator = hasLobLocator;
        this.bytes = null;
    }
    
    public OBStreamParameter(final boolean hasLobLocator, final byte[] bytes, final long length, final boolean noBackslashEscapes) {
        this.is = null;
        this.length = length;
        this.noBackslashEscapes = noBackslashEscapes;
        this.hasLobLocator = hasLobLocator;
        this.bytes = bytes;
    }
    
    public OBStreamParameter(final InputStream is, final long length, final boolean noBackslashEscapes) {
        this(false, is, length, noBackslashEscapes);
    }
    
    public OBStreamParameter(final InputStream is, final boolean noBackSlashEscapes) {
        this(false, is, Long.MAX_VALUE, noBackSlashEscapes);
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        if (this.is != null) {
            pos.write(39);
            if (this.length == Long.MAX_VALUE) {
                pos.writeHex(this.is, false, this.noBackslashEscapes);
            }
            else {
                pos.writeHex(this.is, this.length, false, this.noBackslashEscapes);
            }
            pos.write(39);
        }
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return -1;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        if (this.hasLobLocator && this.bytes != null) {
            pos.writeFieldLength(this.bytes.length);
            pos.write(this.bytes);
        }
        else if (this.is != null) {
            if (this.length == Long.MAX_VALUE) {
                pos.write(this.is, false, this.noBackslashEscapes);
            }
            else {
                pos.write(this.is, this.length, false, this.noBackslashEscapes);
            }
        }
    }
    
    @Override
    public String toString() {
        return "<Stream>";
    }
    
    @Override
    public ColumnType getColumnType() {
        return this.hasLobLocator ? ColumnType.OBBLOB : ColumnType.STRING;
    }
    
    @Override
    public boolean isNullData() {
        return false;
    }
    
    @Override
    public boolean isLongData() {
        return !this.hasLobLocator;
    }
    
    @Override
    public boolean writePieceData(final PacketOutputStream writer, final boolean first, final Options options) throws IOException {
        final int pieceLen = options.pieceLength;
        final byte[] buffer = new byte[pieceLen];
        final int len = this.is.read(buffer);
        boolean ret = false;
        if (len < 0) {
            writer.write(3);
            writer.write(0);
            writer.writeLong(0L);
            writer.flush();
            return false;
        }
        byte[] data = new byte[len];
        System.arraycopy(buffer, 0, data, 0, len);
        data = Utils.toHexString(data).getBytes();
        final int lastLen = this.is.available();
        byte piece;
        if (first && lastLen != 0) {
            piece = 1;
            ret = true;
        }
        else if (first && lastLen == 0) {
            piece = 3;
            ret = false;
        }
        else if (!first && lastLen != 0) {
            piece = 2;
            ret = true;
        }
        else {
            piece = 3;
            ret = false;
        }
        writer.write(piece);
        writer.write(0);
        writer.writeLong(data.length);
        writer.write(data);
        writer.flush();
        return ret;
    }
}
