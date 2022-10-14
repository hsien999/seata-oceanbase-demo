// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.internal.ColumnType;

public class NullParameter implements ParameterHolder, Cloneable
{
    private static final byte[] NULL;
    private final ColumnType type;
    
    public NullParameter() {
        this.type = ColumnType.NULL;
    }
    
    public NullParameter(final ColumnType type) {
        this.type = type;
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
        os.write(NullParameter.NULL);
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return 4;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) {
    }
    
    @Override
    public ColumnType getColumnType() {
        return this.type;
    }
    
    @Override
    public String toString() {
        return "<null>";
    }
    
    @Override
    public boolean isNullData() {
        return true;
    }
    
    @Override
    public boolean isLongData() {
        return false;
    }
    
    static {
        NULL = new byte[] { 78, 85, 76, 76 };
    }
}
