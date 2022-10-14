// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.io.Reader;

public class ReaderParameter implements Cloneable, ParameterHolder
{
    private final Reader reader;
    private final long length;
    private final boolean noBackslashEscapes;
    
    public ReaderParameter(final Reader reader, final long length, final boolean noBackslashEscapes) {
        this.reader = reader;
        this.length = length;
        this.noBackslashEscapes = noBackslashEscapes;
    }
    
    public ReaderParameter(final Reader reader, final boolean noBackslashEscapes) {
        this(reader, Long.MAX_VALUE, noBackslashEscapes);
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        pos.write(39);
        if (this.length == Long.MAX_VALUE) {
            pos.write(this.reader, true, this.noBackslashEscapes);
        }
        else {
            pos.write(this.reader, this.length, true, this.noBackslashEscapes);
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
            pos.write(this.reader, false, this.noBackslashEscapes);
        }
        else {
            pos.write(this.reader, this.length, false, this.noBackslashEscapes);
        }
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.STRING;
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
        return true;
    }
}
