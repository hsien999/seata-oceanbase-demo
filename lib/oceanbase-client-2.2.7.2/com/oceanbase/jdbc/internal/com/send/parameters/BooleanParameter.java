// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class BooleanParameter implements Cloneable, ParameterHolder
{
    private final boolean value;
    
    public BooleanParameter(final boolean value) {
        this.value = value;
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
        os.write(this.value ? 49 : 48);
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return 1;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        pos.write(this.value ? 1 : 0);
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.TINYINT;
    }
    
    @Override
    public String toString() {
        return Boolean.toString(this.value);
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
