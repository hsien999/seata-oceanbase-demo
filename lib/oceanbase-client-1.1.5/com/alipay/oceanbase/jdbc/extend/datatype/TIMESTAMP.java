// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.extend.datatype;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.SQLException;
import java.util.Calendar;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.Time;
import java.io.Serializable;

public class TIMESTAMP extends Datum implements Serializable
{
    static final int[] daysInMonth;
    static final long serialVersionUID = -7964732752952728545L;
    public static int SIZE_TIMESTAMP;
    
    public TIMESTAMP() {
        super(initTimestamp());
    }
    
    public TIMESTAMP(final byte[] bytes) {
        super(bytes);
    }
    
    public TIMESTAMP(final Time time) {
        super(toBytes(time));
    }
    
    public TIMESTAMP(final Date date) {
        super(toBytes(date));
    }
    
    public TIMESTAMP(final Timestamp timestamp) {
        super(toBytes(timestamp));
    }
    
    public TIMESTAMP(final Timestamp timestamp, final Calendar calendar) {
        super(toBytes(timestamp, calendar));
    }
    
    public static final int getNanos(final byte[] bytes, final int idx) {
        int nanos = (bytes[idx + 3] & 0xFF) << 24;
        nanos |= (bytes[idx + 2] & 0xFF) << 16;
        nanos |= (bytes[idx + 1] & 0xFF) << 8;
        nanos |= (bytes[idx] & 0xFF);
        return nanos;
    }
    
    public static final void setNanos(final byte[] bytes, final int idx, final int nanos) {
        bytes[idx + 3] = (byte)(nanos >> 24 & 0xFF);
        bytes[idx + 2] = (byte)(nanos >> 16 & 0xFF);
        bytes[idx + 1] = (byte)(nanos >> 8 & 0xFF);
        bytes[idx] = (byte)(nanos & 0xFF);
    }
    
    public TIMESTAMP(final String str) {
        super(toBytes(str));
    }
    
    public static Date toDate(final byte[] bytes) throws SQLException {
        final int[] result = new int[TIMESTAMP.SIZE_TIMESTAMP];
        for (int i = 0; i < bytes.length; ++i) {
            result[i] = (bytes[i] & 0xFF);
        }
        int i = result[0] * 100 + result[1];
        final Calendar calendar = Calendar.getInstance();
        calendar.set(1, i);
        calendar.set(2, result[2] - 1);
        calendar.set(5, result[3]);
        calendar.set(11, result[4] - 1);
        calendar.set(12, result[5] - 1);
        calendar.set(13, result[6] - 1);
        calendar.set(14, 0);
        final long time = calendar.getTime().getTime();
        return new Date(time);
    }
    
    public static Time toTime(final byte[] bytes) throws SQLException {
        final int hour = bytes[4] & 0xFF;
        final int minute = bytes[5] & 0xFF;
        final int second = bytes[6] & 0xFF;
        return new Time(hour, minute, second);
    }
    
    public static Timestamp toTimestamp(final byte[] bytes) throws SQLException {
        return innerToTimestamp(bytes, null);
    }
    
    private static Timestamp innerToTimestamp(final byte[] bytes, final Calendar cal) throws SQLException {
        final int[] result = new int[TIMESTAMP.SIZE_TIMESTAMP];
        for (int i = 0; i < bytes.length; ++i) {
            result[i] = (bytes[i] & 0xFF);
        }
        int i = result[0] * 100 + result[1];
        Calendar calendar = cal;
        if (null == calendar) {
            calendar = Calendar.getInstance();
        }
        calendar.set(1, i);
        calendar.set(2, result[2] - 1);
        calendar.set(5, result[3]);
        calendar.set(11, result[4]);
        calendar.set(12, result[5]);
        calendar.set(13, result[6]);
        calendar.set(14, 0);
        final long time = calendar.getTime().getTime();
        final Timestamp timestamp = new Timestamp(time);
        timestamp.setNanos(getNanos(bytes, 7));
        return timestamp;
    }
    
    public static Timestamp toTimestamp(final byte[] bytes, final Calendar calendar) throws SQLException {
        return innerToTimestamp(bytes, calendar);
    }
    
    @Override
    public Timestamp timestampValue() throws SQLException {
        return toTimestamp(this.getBytes());
    }
    
    @Override
    public Timestamp timestampValue(final Calendar var1) throws SQLException {
        return toTimestamp(this.getBytes(), var1);
    }
    
    public static String toString(final byte[] bytes) {
        final int[] var1 = new int[bytes.length];
        for (int i = 0; i < bytes.length; ++i) {
            var1[i] = (bytes[i] & 0xFF);
        }
        int i = var1[0] * 100 + var1[1];
        final int month = var1[2];
        final int day = var1[3];
        final int hour = var1[4];
        final int minute = var1[5];
        final int second = var1[6];
        final int nanos = getNanos(bytes, 7);
        return TIMESTAMPTZ.toString(i, month, day, hour, minute, second, nanos, bytes[11], null);
    }
    
    public byte[] toBytes() {
        return this.getBytes();
    }
    
    public static byte[] toBytes(final Time time) {
        if (time == null) {
            return null;
        }
        final byte[] result = new byte[TIMESTAMP.SIZE_TIMESTAMP];
        final Calendar var2 = Calendar.getInstance();
        var2.setTime(time);
        result[0] = 19;
        result[1] = 70;
        result[3] = (result[2] = 1);
        result[4] = (byte)var2.get(11);
        result[5] = (byte)var2.get(12);
        result[6] = (byte)var2.get(13);
        setNanos(result, 7, 0);
        result[11] = 0;
        return result;
    }
    
    public static byte[] toBytes(final Date date) {
        if (date == null) {
            return null;
        }
        final byte[] result = new byte[TIMESTAMP.SIZE_TIMESTAMP];
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        result[0] = (byte)(calendar.get(1) / 100);
        result[1] = (byte)(calendar.get(1) % 100);
        result[2] = (byte)(calendar.get(2) + 1);
        result[3] = (byte)calendar.get(5);
        result[4] = 0;
        result[6] = (result[5] = 0);
        setNanos(result, 7, 0);
        result[11] = 0;
        return result;
    }
    
    public static byte[] toBytes(final Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        final int nanos = timestamp.getNanos();
        final byte[] result = new byte[TIMESTAMP.SIZE_TIMESTAMP];
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        result[0] = (byte)(calendar.get(1) / 100);
        result[1] = (byte)(calendar.get(1) % 100);
        result[2] = (byte)(calendar.get(2) + 1);
        result[3] = (byte)calendar.get(5);
        result[4] = (byte)calendar.get(11);
        result[5] = (byte)calendar.get(12);
        result[6] = (byte)calendar.get(13);
        setNanos(result, 7, nanos);
        return result;
    }
    
    public static byte[] toBytes(final Timestamp timestamp, Calendar calendar) {
        if (timestamp == null) {
            return null;
        }
        final int nanos = timestamp.getNanos();
        final byte[] result = new byte[TIMESTAMP.SIZE_TIMESTAMP];
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.clear();
        calendar.setTime(timestamp);
        int var4 = calendar.get(1);
        if (calendar.get(0) == 0) {
            var4 = -(var4 - 1);
        }
        if (var4 >= -4712 && var4 <= 9999) {
            result[0] = (byte)(var4 / 100);
            result[1] = (byte)(var4 % 100);
            result[2] = (byte)(calendar.get(2) + 1);
            result[3] = (byte)calendar.get(5);
            result[4] = (byte)calendar.get(11);
            result[5] = (byte)calendar.get(12);
            result[6] = (byte)calendar.get(13);
            setNanos(result, 7, nanos);
            return result;
        }
        throw new IllegalArgumentException("Invalid year value");
    }
    
    public static byte[] toBytes(final String str) {
        return toBytes(Timestamp.valueOf(str));
    }
    
    @Override
    public Object toJdbc() throws SQLException {
        return this.timestampValue();
    }
    
    @Override
    public Object makeJdbcArray(final int len) {
        final Timestamp[] timestamps = new Timestamp[len];
        return timestamps;
    }
    
    @Override
    public boolean isConvertibleTo(final Class clazz) {
        return clazz.getName().compareTo("java.sql.Date") == 0 || clazz.getName().compareTo("java.sql.Time") == 0 || clazz.getName().compareTo("java.sql.Timestamp") == 0 || clazz.getName().compareTo("java.lang.String") == 0;
    }
    
    @Override
    public String stringValue() {
        return toString(this.getBytes());
    }
    
    @Override
    public String toString() {
        return this.stringValue();
    }
    
    @Override
    public Date dateValue() throws SQLException {
        return toDate(this.getBytes());
    }
    
    @Override
    public Time timeValue() throws SQLException {
        return toTime(this.getBytes());
    }
    
    private static byte[] initTimestamp() {
        final byte[] bytes = new byte[TIMESTAMP.SIZE_TIMESTAMP];
        bytes[0] = 19;
        bytes[1] = 70;
        bytes[3] = (bytes[2] = 1);
        bytes[4] = 0;
        bytes[6] = (bytes[5] = 0);
        setNanos(bytes, 7, 0);
        bytes[11] = 0;
        return bytes;
    }
    
    private boolean isLeapYear(final int time) {
        if (time % 4 == 0) {
            if (time <= 1582) {
                if (time == -4712) {
                    return false;
                }
            }
            else if (time % 100 == 0) {
                if (time % 400 != 0) {
                    return false;
                }
            }
            final boolean isLeapYear = true;
            return isLeapYear;
        }
        final boolean isLeapYear = false;
        return isLeapYear;
    }
    
    private boolean isValid() {
        final byte[] bytes = this.getBytes();
        if (bytes.length < TIMESTAMP.SIZE_TIMESTAMP) {
            return false;
        }
        final int year = (bytes[0] & 0xFF) * 100 + (bytes[1] & 0xFF);
        if (year < -4712 || year > 9999) {
            return false;
        }
        if (year == 0) {
            return false;
        }
        final int month = bytes[2] & 0xFF;
        if (month < 1 || month > 12) {
            return false;
        }
        final int day = bytes[3] & 0xFF;
        if (day < 1 || day > 31) {
            return false;
        }
        if (day > TIMESTAMP.daysInMonth[month - 1] && (!this.isLeapYear(year) || month != 2 || day != 29)) {
            return false;
        }
        if (year == 1582 && month == 10 && day >= 5 && day < 15) {
            return false;
        }
        final int hour = bytes[4] & 0xFF;
        if (hour < 1 || hour > 24) {
            return false;
        }
        final int second = bytes[5] & 0xFF;
        if (second >= 1 && second <= 60) {
            final int temp = bytes[6] & 0xFF;
            return temp >= 1 && temp <= 60;
        }
        return false;
    }
    
    private void readObject(final ObjectInputStream stream) throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        if (!this.isValid()) {
            throw new IOException("Invalid TIMESTAMP");
        }
    }
    
    static {
        daysInMonth = new int[] { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
        TIMESTAMP.SIZE_TIMESTAMP = 12;
    }
}
