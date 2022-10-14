// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class OBBINARY_DOUBLEParameter implements Cloneable, ParameterHolder
{
    private final double value;
    
    public OBBINARY_DOUBLEParameter(final double value) {
        this.value = value;
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
        os.write(String.valueOf(this.value));
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        pos.writeLong(Double.doubleToLongBits(this.value));
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
        return ColumnType.BINARY_DOUBLE;
    }
    
    @Override
    public boolean isLongData() {
        return false;
    }
    
    public Double getValue() {
        return this.value;
    }
}
