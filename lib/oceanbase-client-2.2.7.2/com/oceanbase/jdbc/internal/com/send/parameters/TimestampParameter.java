// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.util.Calendar;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.util.TimeZone;
import java.sql.Timestamp;

public class TimestampParameter implements Cloneable, ParameterHolder
{
    private final Timestamp ts;
    private final TimeZone timeZone;
    private final boolean fractionalSeconds;
    
    public TimestampParameter(final Timestamp ts, final TimeZone timeZone, final boolean fractionalSeconds) {
        this.ts = ts;
        this.timeZone = timeZone;
        this.fractionalSeconds = fractionalSeconds;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(this.timeZone);
        pos.write(39);
        pos.write(sdf.format(this.ts).getBytes());
        int microseconds = this.ts.getNanos() / 1000;
        if (microseconds > 0 && this.fractionalSeconds) {
            pos.write(46);
            int dig;
            for (int factor = 100000; microseconds > 0; microseconds -= dig * factor, factor /= 10) {
                dig = microseconds / factor;
                pos.write(48 + dig);
            }
        }
        pos.write(39);
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return 27;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        final Calendar calendar = Calendar.getInstance(this.timeZone);
        calendar.setTimeInMillis(this.ts.getTime());
        pos.write((byte)(this.fractionalSeconds ? 11 : 7));
        pos.writeShort((short)calendar.get(1));
        pos.write((byte)(calendar.get(2) + 1 & 0xFF));
        pos.write((byte)(calendar.get(5) & 0xFF));
        pos.write((byte)calendar.get(11));
        pos.write((byte)calendar.get(12));
        pos.write((byte)calendar.get(13));
        if (this.fractionalSeconds) {
            pos.writeInt(this.ts.getNanos() / 1000);
        }
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.DATETIME;
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
}
