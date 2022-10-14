// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class LongParameter implements Cloneable, ParameterHolder
{
    private final long value;
    
    public LongParameter(final long value) {
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
        pos.writeLong(this.value);
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.BIGINT;
    }
    
    @Override
    public String toString() {
        return Long.toString(this.value);
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
