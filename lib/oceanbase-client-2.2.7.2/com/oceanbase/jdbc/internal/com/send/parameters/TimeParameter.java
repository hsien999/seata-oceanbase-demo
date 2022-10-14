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
import java.sql.Time;

public class TimeParameter implements Cloneable, ParameterHolder
{
    private final Time time;
    private final TimeZone timeZone;
    private final boolean fractionalSeconds;
    
    public TimeParameter(final Time time, final TimeZone timeZone, final boolean fractionalSeconds) {
        this.time = time;
        this.timeZone = timeZone;
        this.fractionalSeconds = fractionalSeconds;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(this.timeZone);
        final String dateString = sdf.format(this.time);
        pos.write(39);
        pos.write(dateString.getBytes());
        int microseconds = (int)(this.time.getTime() % 1000L) * 1000;
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
        return 15;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        final Calendar calendar = Calendar.getInstance(this.timeZone);
        calendar.setTime(this.time);
        calendar.set(5, 1);
        if (this.fractionalSeconds) {
            pos.write(12);
            pos.write(0);
            pos.writeInt(0);
            pos.write((byte)calendar.get(11));
            pos.write((byte)calendar.get(12));
            pos.write((byte)calendar.get(13));
            pos.writeInt(calendar.get(14) * 1000);
        }
        else {
            pos.write(8);
            pos.write(0);
            pos.writeInt(0);
            pos.write((byte)calendar.get(11));
            pos.write((byte)calendar.get(12));
            pos.write((byte)calendar.get(13));
        }
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.TIME;
    }
    
    @Override
    public String toString() {
        return this.time.toString();
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
