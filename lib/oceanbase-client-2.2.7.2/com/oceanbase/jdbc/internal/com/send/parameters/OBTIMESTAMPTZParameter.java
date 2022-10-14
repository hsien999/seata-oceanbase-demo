// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.sql.SQLException;
import java.io.IOException;
import java.sql.Connection;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMPTZ;

public class OBTIMESTAMPTZParameter implements Cloneable, ParameterHolder
{
    private final TIMESTAMPTZ ts;
    private static final byte[] LITERALS_TIMESTAMP;
    private static final int ORACLE_TIME_SCALE = 9;
    private boolean isTZTablesImported;
    
    public OBTIMESTAMPTZParameter(final TIMESTAMPTZ ts) {
        this.isTZTablesImported = false;
        this.ts = ts;
    }
    
    public OBTIMESTAMPTZParameter(final TIMESTAMPTZ ts, final boolean isTZTablesImported) {
        this.isTZTablesImported = false;
        this.ts = ts;
        this.isTZTablesImported = isTZTablesImported;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        try {
            pos.write(OBTIMESTAMPTZParameter.LITERALS_TIMESTAMP);
            pos.write(39);
            final String tmString = this.ts.toResultSetString(null);
            pos.write(tmString.getBytes());
            pos.write(39);
        }
        catch (SQLException e) {
            throw new IOException("get String value error");
        }
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return 27;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        final byte[] data = this.ts.getBytes();
        final int length = data.length;
        data[11] = 9;
        pos.write((byte)length);
        pos.write(data, 0, data.length);
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.TIMESTAMP_TZ;
    }
    
    @Override
    public String toString() {
        return "'" + this.ts.toString() + "'";
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
        LITERALS_TIMESTAMP = "timestamp ".getBytes();
    }
}
