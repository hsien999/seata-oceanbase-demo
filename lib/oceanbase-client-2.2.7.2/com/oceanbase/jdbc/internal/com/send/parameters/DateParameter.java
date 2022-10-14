// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.util.Calendar;
import java.text.SimpleDateFormat;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.util.Options;
import java.util.TimeZone;
import java.sql.Date;

public class DateParameter implements Cloneable, ParameterHolder
{
    private final Date date;
    private final TimeZone timeZone;
    private final Options options;
    
    public DateParameter(final Date date, final TimeZone timeZone, final Options options) {
        this.date = date;
        this.timeZone = timeZone;
        this.options = options;
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
        os.write(39);
        os.write(this.dateByteFormat());
        os.write(39);
    }
    
    private byte[] dateByteFormat() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (this.options.useLegacyDatetimeCode || this.options.maximizeMysqlCompatibility) {
            sdf.setTimeZone(Calendar.getInstance().getTimeZone());
        }
        else {
            sdf.setTimeZone(this.timeZone);
        }
        return sdf.format(this.date).getBytes();
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return 16;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        final Calendar calendar = Calendar.getInstance(this.timeZone);
        calendar.setTimeInMillis(this.date.getTime());
        pos.write(7);
        pos.writeShort((short)calendar.get(1));
        pos.write((byte)(calendar.get(2) + 1 & 0xFF));
        pos.write((byte)(calendar.get(5) & 0xFF));
        pos.write(0);
        pos.write(0);
        pos.write(0);
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.DATE;
    }
    
    @Override
    public String toString() {
        return "'" + this.date.toString() + "'";
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
