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
import java.sql.SQLException;
import java.time.ZoneOffset;
import com.oceanbase.jdbc.util.Options;
import java.time.ZoneId;
import java.time.OffsetTime;

public class OffsetTimeParameter implements Cloneable, ParameterHolder
{
    private OffsetTime time;
    private boolean fractionalSeconds;
    
    public OffsetTimeParameter(final OffsetTime offsetTime, final ZoneId serverZoneId, final boolean fractionalSeconds, final Options options) throws SQLException {
        final ZoneId zoneId = options.useLegacyDatetimeCode ? ZoneId.systemDefault() : serverZoneId;
        if (zoneId instanceof ZoneOffset) {
            throw new SQLException("cannot set OffsetTime, since server time zone is set to '" + serverZoneId.toString() + "' (check server variables time_zone and system_time_zone)");
        }
        this.time = offsetTime.withOffsetSameInstant((ZoneOffset)zoneId);
        this.fractionalSeconds = fractionalSeconds;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(this.fractionalSeconds ? "HH:mm:ss.SSSSSS" : "HH:mm:ss", Locale.ENGLISH);
        pos.write(39);
        pos.write(formatter.format(this.time).getBytes());
        pos.write(39);
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return 15;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        if (this.fractionalSeconds) {
            pos.write(12);
            pos.write(0);
            pos.writeInt(0);
            pos.write((byte)this.time.getHour());
            pos.write((byte)this.time.getMinute());
            pos.write((byte)this.time.getSecond());
            pos.writeInt(this.time.getNano() / 1000);
        }
        else {
            pos.write(8);
            pos.write(0);
            pos.writeInt(0);
            pos.write((byte)this.time.getHour());
            pos.write((byte)this.time.getMinute());
            pos.write((byte)this.time.getSecond());
        }
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.TIME;
    }
    
    @Override
    public String toString() {
        return "'" + this.time.toString() + "'";
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
