// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.time.LocalTime;

public class LocalTimeParameter implements Cloneable, ParameterHolder
{
    private final LocalTime time;
    private final boolean fractionalSeconds;
    
    public LocalTimeParameter(final LocalTime time, final boolean fractionalSeconds) {
        this.time = time;
        this.fractionalSeconds = fractionalSeconds;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        final StringBuilder dateString = new StringBuilder(15);
        dateString.append((this.time.getHour() < 10) ? "0" : "").append(this.time.getHour()).append((this.time.getMinute() < 10) ? ":0" : ":").append(this.time.getMinute()).append((this.time.getSecond() < 10) ? ":0" : ":").append(this.time.getSecond());
        final int microseconds = this.time.getNano() / 1000;
        if (microseconds > 0 && this.fractionalSeconds) {
            dateString.append(".");
            if (microseconds % 1000 == 0) {
                dateString.append(Integer.toString(microseconds / 1000 + 1000).substring(1));
            }
            else {
                dateString.append(Integer.toString(microseconds + 1000000).substring(1));
            }
        }
        pos.write(39);
        pos.write(dateString.toString().getBytes());
        pos.write(39);
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return 15;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        final int nano = this.time.getNano();
        if (this.fractionalSeconds && nano > 0) {
            pos.write(12);
            pos.write(0);
            pos.writeInt(0);
            pos.write((byte)this.time.getHour());
            pos.write((byte)this.time.getMinute());
            pos.write((byte)this.time.getSecond());
            pos.writeInt(nano / 1000);
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
