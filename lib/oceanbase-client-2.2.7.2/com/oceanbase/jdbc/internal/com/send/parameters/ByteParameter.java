// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class ByteParameter implements Cloneable, ParameterHolder
{
    private static final char[] hexArray;
    private final int value;
    
    public ByteParameter(final byte value) {
        this.value = value;
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
        os.write(String.valueOf(this.value).getBytes());
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return String.valueOf(this.value).length();
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        pos.write(this.value);
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.TINYINT;
    }
    
    @Override
    public String toString() {
        return String.valueOf(this.value);
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
        hexArray = "0123456789ABCDEF".toCharArray();
    }
}
