// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.nio.charset.Charset;

public class StringParameter implements Cloneable, ParameterHolder
{
    private final String stringValue;
    private final boolean noBackslashEscapes;
    private final String characterEncoding;
    private final Charset charset;
    
    public StringParameter(final String str, final boolean noBackslashEscapes, final String characterEncoding) {
        this.stringValue = str;
        this.noBackslashEscapes = noBackslashEscapes;
        this.characterEncoding = characterEncoding;
        this.charset = Charset.forName(characterEncoding);
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        pos.write(this.stringValue, true, this.noBackslashEscapes);
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return this.stringValue.length() * 3;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        final byte[] bytes = this.stringValue.getBytes(this.charset);
        pos.writeFieldLength(bytes.length);
        pos.write(bytes);
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.VARCHAR;
    }
    
    @Override
    public String toString() {
        if (this.stringValue.length() < 1024) {
            return "'" + this.stringValue + "'";
        }
        return "'" + this.stringValue.substring(0, 1024) + "...'";
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