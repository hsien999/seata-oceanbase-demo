// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.extend.datatype.INTERVALYM;

public class OBINTERVALYMParameter implements Cloneable, ParameterHolder
{
    INTERVALYM intervalymValue;
    private static final byte[] LITERALS_INTERVAL;
    private static final byte[] LITERALS_INTERVAL_END;
    
    public OBINTERVALYMParameter(final INTERVALYM intervalym) {
        this.intervalymValue = intervalym;
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
        os.write(OBINTERVALYMParameter.LITERALS_INTERVAL);
        os.write(39);
        os.write(this.intervalymValue.toString().getBytes());
        os.write(39);
        os.write(OBINTERVALYMParameter.LITERALS_INTERVAL_END);
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        final byte[] data = this.intervalymValue.getBytes();
        pos.write((byte)data.length);
        pos.write(data, 0, data.length);
    }
    
    @Override
    public int getApproximateTextProtocolLength() throws IOException {
        return this.intervalymValue.getBytes().length;
    }
    
    @Override
    public boolean isNullData() {
        return false;
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.INTERVALYM;
    }
    
    @Override
    public boolean isLongData() {
        return false;
    }
    
    static {
        LITERALS_INTERVAL = "interval ".getBytes();
        LITERALS_INTERVAL_END = " year(9) to month".getBytes();
    }
}
