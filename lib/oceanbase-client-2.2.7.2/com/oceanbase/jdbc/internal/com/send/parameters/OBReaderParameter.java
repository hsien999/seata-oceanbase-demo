// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.io.Reader;

public class OBReaderParameter implements Cloneable, LongDataParameterHolder
{
    private final Reader reader;
    private final long length;
    private final boolean noBackslashEscapes;
    private final boolean hasLobLocator;
    private final byte[] bytes;
    
    public OBReaderParameter(final Reader reader, final long length, final boolean noBackslashEscapes) {
        this.reader = reader;
        this.length = length;
        this.noBackslashEscapes = noBackslashEscapes;
        this.bytes = null;
        this.hasLobLocator = false;
    }
    
    public OBReaderParameter(final Reader reader, final boolean noBackslashEscapes) {
        this(reader, Long.MAX_VALUE, noBackslashEscapes);
    }
    
    public OBReaderParameter(final boolean hasLobLocator, final byte[] bytes, final long length, final boolean noBackslashEscapes) {
        this.reader = null;
        this.length = length;
        this.noBackslashEscapes = noBackslashEscapes;
        this.bytes = bytes;
        this.hasLobLocator = hasLobLocator;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        if (!this.hasLobLocator && this.reader != null) {
            pos.write(39);
            if (this.length == Long.MAX_VALUE) {
                pos.writeEscapeQuote(this.reader, this.noBackslashEscapes);
            }
            else {
                pos.writeEscapeQuote(this.reader, this.length, this.noBackslashEscapes);
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
            pos.write(this.bytes, 0, this.bytes.length);
        }
        else if (this.reader != null && this.length == Long.MAX_VALUE) {
            pos.write(this.reader, false, this.noBackslashEscapes);
        }
        else {
            pos.write(this.reader, this.length, false, this.noBackslashEscapes);
        }
    }
    
    @Override
    public ColumnType getColumnType() {
        return this.hasLobLocator ? ColumnType.OBCLOB : ColumnType.BLOB;
    }
    
    @Override
    public String toString() {
        return "<Reader>";
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
        final char[] buffer = new char[pieceLen];
        final int len = this.reader.read(buffer);
        boolean ret = false;
        if (len < 0) {
            writer.write(3);
            writer.write(0);
            writer.writeLong(0L);
            writer.flush();
            return false;
        }
        final byte[] data = new String(buffer, 0, len).getBytes(options.characterEncoding);
        byte piece;
        if (first && this.reader.ready()) {
            piece = 1;
            ret = true;
        }
        else if (first && !this.reader.ready()) {
            piece = 3;
            ret = false;
        }
        else if (!first && this.reader.ready()) {
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
