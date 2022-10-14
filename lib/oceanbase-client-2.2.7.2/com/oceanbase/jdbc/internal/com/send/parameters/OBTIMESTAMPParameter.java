// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMP;
import java.io.IOException;
import java.util.Date;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.util.TimeZone;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class OBTIMESTAMPParameter implements Cloneable, ParameterHolder
{
    private final SimpleDateFormat sdf;
    private final Timestamp ts;
    private final TimeZone timeZone;
    private final boolean fractionalSeconds;
    private final boolean isDate;
    private static final byte[] LITERALS_TIMESTAMP;
    private static final int ORACLE_TIME_SCALE = 9;
    
    public OBTIMESTAMPParameter(final Timestamp ts, final TimeZone timeZone, final boolean fractionalSeconds) {
        this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.ts = ts;
        this.timeZone = timeZone;
        this.fractionalSeconds = fractionalSeconds;
        this.isDate = false;
    }
    
    public OBTIMESTAMPParameter(final Timestamp ts, final TimeZone timeZone, final boolean fractionalSeconds, final boolean isDate) {
        this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.ts = ts;
        this.timeZone = timeZone;
        this.fractionalSeconds = fractionalSeconds;
        this.isDate = isDate;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        this.sdf.setTimeZone(this.timeZone);
        if (this.isDate) {
            pos.write("TO_DATE(".getBytes());
            pos.write(39);
            pos.write(this.sdf.format(this.ts).getBytes());
            pos.write(39);
            pos.write(",".getBytes());
            pos.write(39);
            pos.write("YYYY-MM-DD HH24:MI:SS".getBytes());
            pos.write(39);
            pos.write(")".getBytes());
        }
        else {
            pos.write(OBTIMESTAMPParameter.LITERALS_TIMESTAMP);
            pos.write(39);
            pos.write(this.ts.toString().getBytes());
            pos.write(39);
        }
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return 27;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        final TIMESTAMP timestamp = new TIMESTAMP(this.ts);
        final byte[] data = timestamp.getBytes();
        pos.write((byte)data.length);
        pos.write(data, 0, 11);
        pos.write(9);
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.TIMESTAMP_NANO;
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
