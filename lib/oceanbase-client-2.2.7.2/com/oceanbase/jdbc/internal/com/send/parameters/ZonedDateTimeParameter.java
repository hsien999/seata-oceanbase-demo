// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import java.time.temporal.TemporalAccessor;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.util.Options;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimeParameter implements Cloneable, ParameterHolder
{
    private final ZonedDateTime tz;
    private final boolean fractionalSeconds;
    
    public ZonedDateTimeParameter(final ZonedDateTime tz, final ZoneId serverZoneId, final boolean fractionalSeconds, final Options options) {
        final ZoneId zoneId = options.useLegacyDatetimeCode ? ZoneId.systemDefault() : serverZoneId;
        this.tz = tz.withZoneSameInstant(zoneId);
        this.fractionalSeconds = fractionalSeconds;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.fractionalSeconds ? "yyyy-MM-dd HH:mm:ss.SSSSSS" : "yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        pos.write(39);
        pos.write(formatter.format(this.tz).getBytes());
        pos.write(39);
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return 27;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        pos.write((byte)(this.fractionalSeconds ? 11 : 7));
        pos.writeShort((short)this.tz.getYear());
        pos.write((byte)(this.tz.getMonth().getValue() & 0xFF));
        pos.write((byte)(this.tz.getDayOfMonth() & 0xFF));
        pos.write((byte)this.tz.getHour());
        pos.write((byte)this.tz.getMinute());
        pos.write((byte)this.tz.getSecond());
        if (this.fractionalSeconds) {
            pos.writeInt(this.tz.getNano() / 1000);
        }
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.DATETIME;
    }
    
    @Override
    public String toString() {
        return "'" + this.tz.toString() + "'";
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
