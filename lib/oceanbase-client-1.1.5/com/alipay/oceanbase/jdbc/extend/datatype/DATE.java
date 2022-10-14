// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.extend.datatype;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;

public class DATE extends Datum
{
    static final long serialVersionUID = 5229717576495161269L;
    
    public DATE() {
        super(_initDate());
    }
    
    public DATE(final byte[] bytes) {
        super(bytes);
    }
    
    public DATE(final Date date) {
        super(toBytes(date));
    }
    
    public DATE(final Time time) {
        super(toBytes(time));
    }
    
    public DATE(final Timestamp timestamp) {
        super(toBytes(timestamp));
    }
    
    public DATE(final Date date, final Calendar calendar) {
        super(toBytes(date, calendar));
    }
    
    public DATE(final Time time, final Calendar calendar) {
        super(toBytes(time, calendar));
    }
    
    public DATE(final Timestamp timestamp, final Calendar calendar) {
        super(toBytes(timestamp, calendar));
    }
    
    public DATE(final String date) {
        super(toBytes(date));
    }
    
    public DATE(final String date, final boolean b) throws ParseException {
        super(toBytes(date));
        if (!b) {
            final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
            simpleDateFormat.setLenient(false);
            simpleDateFormat.parse(date);
        }
    }
    
    public DATE(final String date, final Calendar calendar) {
        super(toBytes(date, calendar));
    }
    
    public DATE(final Object object) throws SQLException {
        if (object instanceof Date) {
            this.setShareBytes(toBytes((Date)object));
        }
        else if (object instanceof Time) {
            this.setShareBytes(toBytes((Time)object));
        }
        else if (object instanceof Timestamp) {
            this.setShareBytes(toBytes((Timestamp)object));
        }
        else {
            if (!(object instanceof String)) {
                throw new SQLException("Initialization failed");
            }
            this.setShareBytes(toBytes((String)object));
        }
    }
    
    public DATE(final Object object, final Calendar calendar) throws SQLException {
        if (object instanceof Date) {
            this.setShareBytes(toBytes((Date)object, calendar));
        }
        else if (object instanceof Time) {
            this.setShareBytes(toBytes((Time)object, calendar));
        }
        else if (object instanceof Timestamp) {
            this.setShareBytes(toBytes((Timestamp)object, calendar));
        }
        else {
            if (!(object instanceof String)) {
                throw new SQLException("Initialization failed");
            }
            this.setShareBytes(toBytes((String)object, calendar));
        }
    }
    
    public static Date toDate(final byte[] bytes) {
        final int[] result = new int[7];
        for (int i = 0; i < 7; ++i) {
            result[i] = (bytes[i] & 0xFF);
        }
        int i = (result[0] - 100) * 100 + (result[1] - 100);
        int year = i - 1900;
        if (i <= 0) {
            ++year;
        }
        return new Date(year, result[2] - 1, result[3]);
    }
    
    public static Time toTime(final byte[] bytes) {
        final int[] result = new int[7];
        for (int i = 0; i < 7; ++i) {
            result[i] = (bytes[i] & 0xFF);
        }
        return new Time(result[4] - 1, result[5] - 1, result[6] - 1);
    }
    
    public static Timestamp toTimestamp(final byte[] bytes) {
        final int[] result = new int[7];
        for (int i = 0; i < 7; ++i) {
            result[i] = (bytes[i] & 0xFF);
        }
        int i = (result[0] - 100) * 100 + (result[1] - 100);
        int year = i - 1900;
        if (i <= 0) {
            ++year;
        }
        return new Timestamp(year, result[2] - 1, result[3], result[4] - 1, result[5] - 1, result[6] - 1, 0);
    }
    
    public static Date toDate(final byte[] bytes, Calendar calendar) {
        final int[] result = new int[7];
        for (int i = 0; i < 7; ++i) {
            result[i] = (bytes[i] & 0xFF);
        }
        int i = (result[0] - 100) * 100 + (result[1] - 100);
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.clear();
        calendar.set(1, i);
        calendar.set(2, result[2] - 1);
        calendar.set(5, result[3]);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        final Date date = new Date(calendar.getTime().getTime());
        return date;
    }
    
    public static Time toTime(final byte[] bytes, Calendar calendar) {
        final int[] result = new int[7];
        for (int i = 0; i < 7; ++i) {
            result[i] = (bytes[i] & 0xFF);
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.clear();
        calendar.set(1, 1970);
        calendar.set(2, 0);
        calendar.set(5, 1);
        calendar.set(11, result[4] - 1);
        calendar.set(12, result[5] - 1);
        calendar.set(13, result[6] - 1);
        calendar.set(14, 0);
        final Time time = new Time(calendar.getTime().getTime());
        return time;
    }
    
    public static Timestamp toTimestamp(final byte[] bytes, Calendar calendar) {
        final int[] result = new int[7];
        for (int i = 0; i < 7; ++i) {
            result[i] = (bytes[i] & 0xFF);
        }
        int i = (result[0] - 100) * 100 + (result[1] - 100);
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.clear();
        calendar.set(1, i);
        calendar.set(2, result[2] - 1);
        calendar.set(5, result[3]);
        calendar.set(11, result[4] - 1);
        calendar.set(12, result[5] - 1);
        calendar.set(13, result[6] - 1);
        calendar.set(14, 0);
        final Timestamp timestamp = new Timestamp(calendar.getTime().getTime());
        return timestamp;
    }
    
    public static String toString(final byte[] bytes) {
        final int[] result = new int[7];
        for (int i = 0; i < 7; ++i) {
            if (bytes[i] < 0) {
                result[i] = bytes[i] + 256;
            }
            else {
                result[i] = bytes[i];
            }
        }
        int i = (result[0] - 100) * 100 + (result[1] - 100);
        final int month = result[2];
        final int day = result[3];
        final int hour = result[4] - 1;
        final int minute = result[5] - 1;
        final int second = result[6] - 1;
        return TIMESTAMPTZ.toString(i, month, day, hour, minute, second, -1, 0, null);
    }
    
    public byte[] toBytes() {
        return this.getBytes();
    }
    
    public static byte[] toBytes(final Date date) {
        if (date == null) {
            return null;
        }
        final byte[] result = new byte[7];
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(1);
        if (calendar.get(0) == 0) {
            year = -year;
        }
        if (year >= -4712 && year <= 9999) {
            result[0] = (byte)(year / 100 + 100);
            result[1] = (byte)(year % 100 + 100);
            result[2] = (byte)(calendar.get(2) + 1);
            result[3] = (byte)calendar.get(5);
            result[4] = 1;
            result[6] = (result[5] = 1);
            return result;
        }
        throw new IllegalArgumentException("Invalid year value");
    }
    
    public static byte[] toBytes(final Time time) {
        if (time == null) {
            return null;
        }
        final byte[] result = new byte[7];
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        result[0] = 119;
        result[1] = -86;
        result[3] = (result[2] = 1);
        result[4] = (byte)(calendar.get(11) + 1);
        result[5] = (byte)(calendar.get(12) + 1);
        result[6] = (byte)(calendar.get(13) + 1);
        return result;
    }
    
    public static byte[] toBytes(final Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        final byte[] result = new byte[7];
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        int year = calendar.get(1);
        if (calendar.get(0) == 0) {
            year = -year;
        }
        if (year >= -4712 && year <= 9999) {
            result[0] = (byte)(year / 100 + 100);
            result[1] = (byte)(year % 100 + 100);
            result[2] = (byte)(calendar.get(2) + 1);
            result[3] = (byte)calendar.get(5);
            result[4] = (byte)(calendar.get(11) + 1);
            result[5] = (byte)(calendar.get(12) + 1);
            result[6] = (byte)(calendar.get(13) + 1);
            return result;
        }
        throw new IllegalArgumentException("Invalid year value");
    }
    
    public static byte[] toBytes(final Date date, Calendar calendar) {
        if (date == null) {
            return null;
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.clear();
        calendar.setTime(date);
        final byte[] result = new byte[7];
        int year = calendar.get(1);
        if (calendar.get(0) == 0) {
            year = -year;
        }
        if (year >= -4712 && year <= 9999) {
            result[0] = (byte)(year / 100 + 100);
            result[1] = (byte)(year % 100 + 100);
            result[2] = (byte)(calendar.get(2) + 1);
            result[3] = (byte)calendar.get(5);
            result[4] = 1;
            result[6] = (result[5] = 1);
            return result;
        }
        throw new IllegalArgumentException("Invalid year value");
    }
    
    public static byte[] toBytes(final Time time, Calendar calendar) {
        if (time == null) {
            return null;
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.clear();
        calendar.setTime(time);
        final byte[] result = { 119, -86, 1, 1, (byte)(calendar.get(11) + 1), (byte)(calendar.get(12) + 1), (byte)(calendar.get(13) + 1) };
        return result;
    }
    
    public static byte[] toBytes(final Timestamp timestamp, Calendar calendar) {
        if (timestamp == null) {
            return null;
        }
        if (calendar == null) {
            calendar = Calendar.getInstance();
        }
        calendar.clear();
        calendar.setTime(timestamp);
        final byte[] result = new byte[7];
        int year = calendar.get(1);
        if (calendar.get(0) == 0) {
            year = -year;
        }
        if (year >= -4712 && year <= 9999) {
            result[0] = (byte)(year / 100 + 100);
            result[1] = (byte)(year % 100 + 100);
            result[2] = (byte)(calendar.get(2) + 1);
            result[3] = (byte)calendar.get(5);
            result[4] = (byte)(calendar.get(11) + 1);
            result[5] = (byte)(calendar.get(12) + 1);
            result[6] = (byte)(calendar.get(13) + 1);
            return result;
        }
        throw new IllegalArgumentException("Invalid year value");
    }
    
    public static byte[] toBytes(final String time) {
        return toBytes(Timestamp.valueOf(time));
    }
    
    public static byte[] toBytes(final String time, final Calendar calendar) {
        return toBytes(Timestamp.valueOf(time), calendar);
    }
    
    @Override
    public Date dateValue() {
        return toDate(this.getBytes());
    }
    
    @Override
    public Time timeValue() {
        return toTime(this.getBytes());
    }
    
    @Override
    public Timestamp timestampValue() {
        return toTimestamp(this.getBytes());
    }
    
    public Date dateValue(final Calendar calendar) {
        return toDate(this.getBytes(), calendar);
    }
    
    @Override
    public Timestamp timestampValue(final Calendar calendar) {
        return toTimestamp(this.getBytes(), calendar);
    }
    
    @Override
    public Time timeValue(final Calendar calendar) {
        return toTime(this.getBytes(), calendar);
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
    public Object toJdbc() {
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
    
    private static byte[] _initDate() {
        final byte[] result = { 119, -86, 1, 1, 1, 1, 1 };
        return result;
    }
}
