// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.io.InputStream;

public class StreamParameter implements Cloneable, ParameterHolder
{
    private final InputStream is;
    private final long length;
    private final boolean noBackslashEscapes;
    
    public StreamParameter(final InputStream is, final long length, final boolean noBackslashEscapes) {
        this.is = is;
        this.length = length;
        this.noBackslashEscapes = noBackslashEscapes;
    }
    
    public StreamParameter(final InputStream is, final boolean noBackSlashEscapes) {
        this(is, Long.MAX_VALUE, noBackSlashEscapes);
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        pos.write(StreamParameter.BINARY_INTRODUCER);
        if (this.length == Long.MAX_VALUE) {
            pos.write(this.is, true, this.noBackslashEscapes);
        }
        else {
            pos.write(this.is, this.length, true, this.noBackslashEscapes);
        }
        pos.write(39);
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return -1;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        if (this.length == Long.MAX_VALUE) {
            pos.write(this.is, false, this.noBackslashEscapes);
        }
        else {
            pos.write(this.is, this.length, false, this.noBackslashEscapes);
        }
    }
    
    @Override
    public String toString() {
        return "<Stream>";
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.BLOB;
    }
    
    @Override
    public boolean isNullData() {
        return false;
    }
    
    @Override
    public boolean isLongData() {
        return true;
    }
}
