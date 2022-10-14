// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class OBBINARY_FLOATParameter implements Cloneable, ParameterHolder
{
    private final float value;
    
    public OBBINARY_FLOATParameter(final float value) {
        this.value = value;
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
        os.write(String.valueOf(this.value));
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        pos.writeInt(Float.floatToIntBits(this.value));
    }
    
    @Override
    public int getApproximateTextProtocolLength() throws IOException {
        return String.valueOf(this.value).getBytes().length;
    }
    
    @Override
    public boolean isNullData() {
        return false;
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.BINARY_FLOAT;
    }
    
    @Override
    public boolean isLongData() {
        return false;
    }
    
    public float getValue() {
        return this.value;
    }
}
