// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

import com.oceanbase.jdbc.internal.protocol.Protocol;
import java.util.TimeZone;
import com.oceanbase.jdbc.OceanBaseConnection;
import java.sql.Timestamp;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Connection;
import java.util.Calendar;

public class TIMESTAMPLTZ extends Datum
{
    private static int SIZE_TIMESTAMPLTZ;
    private static int SIZE_TIMESTAMPLTZ_NOFRAC;
    private static int SIZE_DATE;
    private static int HOUR_MILLISECOND;
    private static int MINUTE_MILLISECOND;
    private static int JAVA_YEAR;
    private static int JAVA_MONTH;
    private static int JAVA_DATE;
    private static int MINYEAR;
    private static int MAXYEAR;
    private static boolean cached;
    private static Calendar dbtz;
    static final long serialVersionUID = 2045880772054757133L;
    
    public TIMESTAMPLTZ() {
        super(initTimestampltz());
    }
    
    public TIMESTAMPLTZ(final byte[] var1) {
        super(var1);
    }
    
    public TIMESTAMPLTZ(final Connection connection, final Time time) throws SQLException {
        super(toBytes(connection, time));
    }
    
    public TIMESTAMPLTZ(final Connection connection, final Date date) throws SQLException {
        super(toBytes(connection, date));
    }
    
    public TIMESTAMPLTZ(final Connection connection, final Timestamp timestamp) throws SQLException {
        super(toBytes(connection, timestamp));
    }
    
    public static String toString(final Connection connection, final byte[] bytes, final boolean isResult) throws SQLException {
        if (bytes.length < 12) {
            throw new SQLException("invalid bytes length");
        }
        final Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(((OceanBaseConnection)connection).getSessionTimeZone()));
        calendar.setTimeInMillis(TIMESTAMPTZ.getOriginTime(bytes, TimeZone.getTimeZone(((OceanBaseConnection)connection).getSessionTimeZone())));
        final int year = calendar.get(1);
        final int month = calendar.get(2) + 1;
        final int day = calendar.get(5);
        final int hour = calendar.get(11);
        final int minute = calendar.get(12);
        final int second = calendar.get(13);
        final int nanos = TIMESTAMP.getNanos(bytes, 7);
        return toString(year, month, day, hour, minute, second, nanos, bytes[11], Datum.getSessionTimeZone(connection), isResult);
    }
    
    private static final String toStr(final int temp) {
        return (temp < 10) ? ("0" + temp) : Integer.toString(temp);
    }
    
    public static final String toString(final int year, final int month, final int day, final int hour, final int minute, final int second, final int nanos, final int scale, final String timezone, final boolean isResult) {
        String time = "" + year + "-" + toStr(month) + "-" + toStr(day) + " " + toStr(hour) + ":" + toStr(minute) + ":" + toStr(second);
        int target = 0;
        if (isResult) {
            target = 0;
        }
        else {
            target = 1;
        }
        if (nanos >= target) {
            String temp = String.format("%09d", nanos);
            char[] chars;
            int index;
            for (chars = temp.toCharArray(), index = chars.length; index > 1 && chars[index - 1] == '0'; --index) {}
            temp = temp.substring(0, index);
            time = time + "." + temp;
        }
        if (timezone != null) {
            time = time + " " + timezone;
        }
        return time;
    }
    
    public String toResultSetString(final Connection connection) throws SQLException {
        return toString(connection, this.getBytes(), true);
    }
    
    public byte[] toBytes() {
        return this.getBytes();
    }
    
    public static byte[] toBytes(final Connection connection, final Time time) throws SQLException {
        if (time == null) {
            return null;
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(time);
        final short base = 1970;
        calendar.set(1, base);
        calendar.set(2, 0);
        calendar.set(5, 1);
        final byte[] bytes = toBytes(connection, calendar, 0);
        return bytes;
    }
    
    public static byte[] toBytes(final Connection connection, final Date date) throws SQLException {
        if (date == null) {
            return null;
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(11, 0);
        calendar.set(12, 0);
        calendar.set(13, 0);
        final byte[] result = toBytes(connection, calendar, 0);
        return result;
    }
    
    public static byte[] toBytes(final Connection connection, final Timestamp timestamp) throws SQLException {
        if (timestamp == null) {
            return null;
        }
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(timestamp);
        final int nanos = timestamp.getNanos();
        final byte[] bytes = toBytes(connection, calendar, nanos);
        return bytes;
    }
    
    public static byte[] toBytes(final Connection connection, final String time) throws SQLException {
        return toBytes(connection, Timestamp.valueOf(time));
    }
    
    @Override
    public Date dateValue() throws SQLException {
        throw new SQLException("Conversion to Date failed");
    }
    
    public static Date toDate(final Connection connection, final byte[] bytes) throws SQLException {
        return new Date(TIMESTAMPTZ.getOriginTime(bytes, TimeZone.getTimeZone(((OceanBaseConnection)connection).getSessionTimeZone()), false));
    }
    
    public static Time toTime(final Connection connection, final byte[] bytes) throws SQLException {
        return new Time(TIMESTAMPTZ.getOriginTime(bytes, TimeZone.getTimeZone(((OceanBaseConnection)connection).getSessionTimeZone())));
    }
    
    public static Timestamp toTimestamp(final Connection connection, final byte[] bytes) throws SQLException {
        if (bytes.length < 12) {
            throw new SQLException("invalid bytes length");
        }
        final Timestamp timestamp = new Timestamp(TIMESTAMPTZ.getOriginTime(bytes, TimeZone.getTimeZone(((OceanBaseConnection)connection).getSessionTimeZone())));
        timestamp.setNanos(TIMESTAMP.getNanos(bytes, 7));
        return timestamp;
    }
    
    public static Timestamp toTimestamp(final Protocol protocol, final byte[] bytes) throws SQLException {
        if (bytes.length < 12) {
            throw new SQLException("invalid bytes length");
        }
        final Timestamp timestamp = new Timestamp(TIMESTAMPTZ.getOriginTime(bytes, TimeZone.getTimeZone(protocol.getTimeZone().getID()), false));
        timestamp.setNanos(TIMESTAMP.getNanos(bytes, 7));
        return timestamp;
    }
    
    public static Timestamp toTimestamp(final Protocol protocol, final byte[] bytes, final boolean isResult) throws SQLException {
        if (bytes.length < 12) {
            throw new SQLException("invalid bytes length");
        }
        final Timestamp timestamp = new Timestamp(TIMESTAMPTZ.getOriginTime(bytes, TimeZone.getTimeZone(protocol.getTimeZone().getID()), !isResult));
        timestamp.setNanos(TIMESTAMP.getNanos(bytes, 7));
        return timestamp;
    }
    
    public static TIMESTAMP toTIMESTAMP(final Connection connection, final byte[] bytes) throws SQLException {
        return new TIMESTAMP(toTimestamp(connection, bytes));
    }
    
    public static TIMESTAMP toTIMESTAMP(final Protocol protocol, final byte[] bytes) throws SQLException {
        return new TIMESTAMP(toTimestamp(protocol, bytes));
    }
    
    public static TIMESTAMP resultTIMESTAMP(final Protocol protocol, final byte[] bytes) throws SQLException {
        return new TIMESTAMP(toTimestamp(protocol, bytes, true));
    }
    
    public static TIMESTAMPTZ toTIMESTAMPTZ(final Connection connection, final byte[] bytes) throws SQLException {
        return new TIMESTAMPTZ(connection, toTimestamp(connection, bytes), Calendar.getInstance());
    }
    
    public Timestamp timestampValue(final Connection connection) throws SQLException {
        return toTimestamp(connection, this.getBytes());
    }
    
    @Override
    public String stringValue(final Connection connection) throws SQLException {
        return toString(connection, this.getBytes(), false);
    }
    
    public Date dateValue(final Connection connection) throws SQLException {
        return toDate(connection, this.getBytes());
    }
    
    public Time timeValue(final Connection connection) throws SQLException {
        return toTime(connection, this.getBytes());
    }
    
    @Override
    public Object toJdbc() throws SQLException {
        return null;
    }
    
    @Override
    public Object makeJdbcArray(final int time) {
        final Timestamp[] timestamps = new Timestamp[time];
        return timestamps;
    }
    
    @Override
    public boolean isConvertibleTo(final Class clazz) {
        return clazz.getName().compareTo("java.sql.Date") == 0 || clazz.getName().compareTo("java.sql.Time") == 0 || clazz.getName().compareTo("java.sql.Timestamp") == 0 || clazz.getName().compareTo("java.lang.String") == 0;
    }
    
    private static byte[] initTimestampltz() {
        final byte[] result = new byte[TIMESTAMPLTZ.SIZE_TIMESTAMPLTZ];
        result[0] = 19;
        result[1] = 70;
        result[2] = 1;
        result[4] = (result[3] = 1);
        result[6] = (result[5] = 1);
        TIMESTAMP.setNanos(result, 7, 0);
        result[11] = 0;
        return result;
    }
    
    private static byte[] toBytes(final Connection connection, final Calendar calendar, final int nanos) throws SQLException {
        final byte[] result = new byte[TIMESTAMPLTZ.SIZE_TIMESTAMPLTZ];
        final int year = calendar.get(1);
        if (year >= TIMESTAMPLTZ.MINYEAR && year <= TIMESTAMPLTZ.MAXYEAR) {
            result[0] = (byte)(calendar.get(1) / 100);
            result[1] = (byte)(calendar.get(1) % 100);
            result[2] = (byte)(calendar.get(2) + 1);
            result[3] = (byte)calendar.get(5);
            result[4] = (byte)calendar.get(11);
            result[5] = (byte)calendar.get(12);
            result[6] = (byte)calendar.get(13);
            TIMESTAMP.setNanos(result, 7, nanos);
            String temp = String.format("%09d", nanos);
            char[] chars;
            int index;
            for (chars = temp.toCharArray(), index = chars.length; index > 1 && chars[index - 1] == '0'; --index) {}
            temp = temp.substring(0, index);
            final String nanosStr = String.valueOf(temp);
            result[11] = (byte)nanosStr.length();
            return result;
        }
        throw new SQLException("error format", "268");
    }
    
    static {
        TIMESTAMPLTZ.SIZE_TIMESTAMPLTZ = 12;
        TIMESTAMPLTZ.SIZE_TIMESTAMPLTZ_NOFRAC = 7;
        TIMESTAMPLTZ.SIZE_DATE = 7;
        TIMESTAMPLTZ.HOUR_MILLISECOND = 3600000;
        TIMESTAMPLTZ.MINUTE_MILLISECOND = 60000;
        TIMESTAMPLTZ.JAVA_YEAR = 1970;
        TIMESTAMPLTZ.JAVA_MONTH = 0;
        TIMESTAMPLTZ.JAVA_DATE = 1;
        TIMESTAMPLTZ.MINYEAR = -4712;
        TIMESTAMPLTZ.MAXYEAR = 9999;
        TIMESTAMPLTZ.cached = false;
    }
}
