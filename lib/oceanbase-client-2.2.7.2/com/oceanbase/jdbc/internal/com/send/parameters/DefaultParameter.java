// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class DefaultParameter implements Cloneable, ParameterHolder
{
    private static final byte[] defaultBytes;
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        pos.write(DefaultParameter.defaultBytes);
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return 7;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        pos.writeFieldLength(DefaultParameter.defaultBytes.length);
        pos.write(DefaultParameter.defaultBytes);
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.VARCHAR;
    }
    
    @Override
    public String toString() {
        return "DEFAULT";
    }
    
    @Override
    public boolean isNullData() {
        return false;
    }
    
    @Override
    public boolean isLongData() {
        return false;
    }
    
    static {
        defaultBytes = "DEFAULT".getBytes();
    }
}
