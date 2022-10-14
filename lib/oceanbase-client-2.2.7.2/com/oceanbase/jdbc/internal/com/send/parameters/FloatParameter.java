// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class FloatParameter implements Cloneable, ParameterHolder
{
    private final float value;
    
    public FloatParameter(final float value) {
        this.value = value;
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
        os.write(String.valueOf(this.value).getBytes());
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return String.valueOf(this.value).getBytes().length;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        pos.writeInt(Float.floatToIntBits(this.value));
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.FLOAT;
    }
    
    @Override
    public String toString() {
        return Float.toString(this.value);
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
