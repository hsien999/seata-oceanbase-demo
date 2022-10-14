// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.sql.Connection;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMPLTZ;

public class OBTIMESTAMPLTZParameter implements Cloneable, ParameterHolder
{
    private final TIMESTAMPLTZ ts;
    private final Connection connection;
    private static final byte[] LITERALS_TIMESTAMP;
    private static final int ORACLE_TIME_SCALE = 9;
    
    public OBTIMESTAMPLTZParameter(final TIMESTAMPLTZ ts, final Connection connection) {
        this.ts = ts;
        this.connection = connection;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        Timestamp timestamp;
        try {
            timestamp = TIMESTAMPLTZ.toTimestamp(this.connection, this.ts.getBytes());
        }
        catch (SQLException ex) {
            throw new IOException(ex);
        }
        pos.write(OBTIMESTAMPLTZParameter.LITERALS_TIMESTAMP);
        pos.write(39);
        pos.write(timestamp.toString().getBytes());
        pos.write(39);
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return 27;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        final byte[] data = this.ts.getBytes();
        pos.write((byte)data.length);
        pos.write(data, 0, 11);
        pos.write(9);
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.TIMESTAMP_LTZ;
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
