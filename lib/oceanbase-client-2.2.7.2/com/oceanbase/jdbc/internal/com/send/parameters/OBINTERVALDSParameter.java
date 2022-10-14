// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.extend.datatype.INTERVALDS;

public class OBINTERVALDSParameter implements Cloneable, ParameterHolder
{
    INTERVALDS intervaldsValue;
    private static final byte[] LITERALS_INTERVALDS;
    private static final byte[] LITERALS_INTERVALDS_END;
    
    public OBINTERVALDSParameter(final INTERVALDS intervalds) {
        this.intervaldsValue = intervalds;
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
        os.write(OBINTERVALDSParameter.LITERALS_INTERVALDS);
        os.write(39);
        os.write(this.intervaldsValue.toString().getBytes());
        os.write(39);
        os.write(OBINTERVALDSParameter.LITERALS_INTERVALDS_END);
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        final byte[] data = this.intervaldsValue.getBytes();
        pos.write((byte)data.length);
        pos.write(data, 0, data.length);
    }
    
    @Override
    public int getApproximateTextProtocolLength() throws IOException {
        return this.intervaldsValue.getBytes().length;
    }
    
    @Override
    public boolean isNullData() {
        return false;
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.INTERVALDS;
    }
    
    @Override
    public boolean isLongData() {
        return false;
    }
    
    static {
        LITERALS_INTERVALDS = "interval ".getBytes();
        LITERALS_INTERVALDS_END = " day(9) to second(9)".getBytes();
    }
}
