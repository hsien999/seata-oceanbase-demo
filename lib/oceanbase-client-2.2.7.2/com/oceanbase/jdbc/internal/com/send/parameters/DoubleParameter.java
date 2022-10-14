// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class DoubleParameter implements Cloneable, ParameterHolder
{
    private final double value;
    
    public DoubleParameter(final double value) {
        this.value = value;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        pos.write(String.valueOf(this.value).getBytes());
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return String.valueOf(this.value).getBytes().length;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        pos.writeLong(Double.doubleToLongBits(this.value));
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.DOUBLE;
    }
    
    @Override
    public String toString() {
        return Double.toString(this.value);
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
