// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import java.util.Arrays;
import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class ByteArrayParameter implements Cloneable, ParameterHolder
{
    private final byte[] bytes;
    private final boolean noBackslashEscapes;
    
    public ByteArrayParameter(final byte[] bytes, final boolean noBackslashEscapes) {
        this.bytes = bytes;
        this.noBackslashEscapes = noBackslashEscapes;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        pos.write(ByteArrayParameter.BINARY_INTRODUCER);
        pos.writeBytesEscaped(this.bytes, this.bytes.length, this.noBackslashEscapes);
        pos.write(39);
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return this.bytes.length * 2;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        pos.writeFieldLength(this.bytes.length);
        pos.write(this.bytes);
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.VARSTRING;
    }
    
    @Override
    public String toString() {
        if (this.bytes.length > 1024) {
            return "<bytearray:" + new String(Arrays.copyOfRange(this.bytes, 0, 1024)) + "...>";
        }
        return "<bytearray:" + new String(this.bytes) + ">";
    }
    
    @Override
    public boolean isNullData() {
        return false;
    }
    
    @Override
    public boolean isLongData() {
        return false;
    }
}
