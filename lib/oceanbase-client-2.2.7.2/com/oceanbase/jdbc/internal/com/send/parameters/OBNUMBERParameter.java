// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class OBNUMBERParameter implements Cloneable, ParameterHolder
{
    private final Integer value;
    
    public OBNUMBERParameter(final Integer value) {
        this.value = value;
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
        os.write(String.valueOf(this.value));
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        pos.writeInt(this.value);
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
        return ColumnType.NUMBER;
    }
    
    @Override
    public boolean isLongData() {
        return false;
    }
    
    public Integer getValue() {
        return this.value;
    }
}
