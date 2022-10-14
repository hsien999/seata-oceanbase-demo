// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.io.IOException;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import java.sql.Timestamp;
import java.util.Date;
import java.sql.Time;
import java.util.Calendar;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.TimeZone;

public class TimeUtil
{
    static final TimeZone GMT_TIMEZONE;
    private static final TimeZone DEFAULT_TIMEZONE;
    private static final String TIME_ZONE_MAPPINGS_RESOURCE = "/com/alipay/oceanbase/jdbc/TimeZoneMapping.properties";
    private static Properties timeZoneMappings;
    protected static final Method systemNanoTimeMethod;
    
    public static boolean nanoTimeAvailable() {
        return TimeUtil.systemNanoTimeMethod != null;
    }
    
    public static final TimeZone getDefaultTimeZone(final boolean useCache) {
        return (TimeZone)(useCache ? TimeUtil.DEFAULT_TIMEZONE.clone() : TimeZone.getDefault().clone());
    }
    
    public static long getCurrentTimeNanosOrMillis() {
        if (TimeUtil.systemNanoTimeMethod != null) {
            try {
                return (long)TimeUtil.systemNanoTimeMethod.invoke(null, (Object[])null);
            }
            catch (IllegalArgumentException ex) {}
            catch (IllegalAccessException ex2) {}
            catch (InvocationTargetException ex3) {}
        }
        return System.currentTimeMillis();
    }
    
    public static Time changeTimezone(final MySQLConnection conn, final Calendar sessionCalendar, final Calendar targetCalendar, final Time t, final TimeZone fromTz, final TimeZone toTz, final boolean rollForward) {
        if (conn != null) {
            if (conn.getUseTimezone() && !conn.getNoTimezoneConversionForTimeType()) {
                final Calendar fromCal = Calendar.getInstance(fromTz);
                fromCal.setTime(t);
                final int fromOffset = fromCal.get(15) + fromCal.get(16);
                final Calendar toCal = Calendar.getInstance(toTz);
                toCal.setTime(t);
                final int toOffset = toCal.get(15) + toCal.get(16);
                final int offsetDiff = fromOffset - toOffset;
                long toTime = toCal.getTime().getTime();
                if (rollForward) {
                    toTime += offsetDiff;
                }
                else {
                    toTime -= offsetDiff;
                }
                final Time changedTime = new Time(toTime);
                return changedTime;
            }
            if (conn.getUseJDBCCompliantTimezoneShift() && targetCalendar != null) {
                final Time adjustedTime = new Time(jdbcCompliantZoneShift(sessionCalendar, targetCalendar, t));
                return adjustedTime;
            }
        }
        return t;
    }
    
    public static Timestamp changeTimezone(final MySQLConnection conn, final Calendar sessionCalendar, final Calendar targetCalendar, final Timestamp tstamp, final TimeZone fromTz, final TimeZone toTz, final boolean rollForward) {
        if (conn != null) {
            if (conn.getUseTimezone()) {
                final Calendar fromCal = Calendar.getInstance(fromTz);
                fromCal.setTime(tstamp);
                final int fromOffset = fromCal.get(15) + fromCal.get(16);
                final Calendar toCal = Calendar.getInstance(toTz);
                toCal.setTime(tstamp);
                final int toOffset = toCal.get(15) + toCal.get(16);
                final int offsetDiff = fromOffset - toOffset;
                long toTime = toCal.getTime().getTime();
                if (rollForward) {
                    toTime += offsetDiff;
                }
                else {
                    toTime -= offsetDiff;
                }
                final Timestamp changedTimestamp = new Timestamp(toTime);
                return changedTimestamp;
            }
            if (conn.getUseJDBCCompliantTimezoneShift() && targetCalendar != null) {
                final Timestamp adjustedTimestamp = new Timestamp(jdbcCompliantZoneShift(sessionCalendar, targetCalendar, tstamp));
                adjustedTimestamp.setNanos(tstamp.getNanos());
                return adjustedTimestamp;
            }
        }
        return tstamp;
    }
    
    private static long jdbcCompliantZoneShift(Calendar sessionCalendar, final Calendar targetCalendar, final Date dt) {
        if (sessionCalendar == null) {
            sessionCalendar = new GregorianCalendar();
        }
        synchronized (sessionCalendar) {
            final Date origCalDate = targetCalendar.getTime();
            final Date origSessionDate = sessionCalendar.getTime();
            try {
                sessionCalendar.setTime(dt);
                targetCalendar.set(1, sessionCalendar.get(1));
                targetCalendar.set(2, sessionCalendar.get(2));
                targetCalendar.set(5, sessionCalendar.get(5));
                targetCalendar.set(11, sessionCalendar.get(11));
                targetCalendar.set(12, sessionCalendar.get(12));
                targetCalendar.set(13, sessionCalendar.get(13));
                targetCalendar.set(14, sessionCalendar.get(14));
                return targetCalendar.getTime().getTime();
            }
            finally {
                sessionCalendar.setTime(origSessionDate);
                targetCalendar.setTime(origCalDate);
            }
        }
    }
    
    static final java.sql.Date fastDateCreate(final boolean useGmtConversion, Calendar gmtCalIfNeeded, final Calendar cal, final int year, final int month, final int day) {
        Calendar dateCal = cal;
        if (useGmtConversion) {
            if (gmtCalIfNeeded == null) {
                gmtCalIfNeeded = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
            }
            dateCal = gmtCalIfNeeded;
        }
        synchronized (dateCal) {
            final Date origCalDate = dateCal.getTime();
            try {
                dateCal.clear();
                dateCal.set(14, 0);
                dateCal.set(year, month - 1, day, 0, 0, 0);
                final long dateAsMillis = dateCal.getTimeInMillis();
                return new java.sql.Date(dateAsMillis);
            }
            finally {
                dateCal.setTime(origCalDate);
            }
        }
    }
    
    static final java.sql.Date fastDateCreate(final int year, final int month, final int day, final Calendar targetCalendar) {
        final Calendar dateCal = (targetCalendar == null) ? new GregorianCalendar() : targetCalendar;
        synchronized (dateCal) {
            final Date origCalDate = dateCal.getTime();
            try {
                dateCal.clear();
                dateCal.set(year, month - 1, day, 0, 0, 0);
                dateCal.set(14, 0);
                final long dateAsMillis = dateCal.getTimeInMillis();
                return new java.sql.Date(dateAsMillis);
            }
            finally {
                dateCal.setTime(origCalDate);
            }
        }
    }
    
    static final Time fastTimeCreate(final Calendar cal, final int hour, final int minute, final int second, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        if (hour < 0 || hour > 24) {
            throw SQLError.createSQLException("Illegal hour value '" + hour + "' for java.sql.Time type in value '" + timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        }
        if (minute < 0 || minute > 59) {
            throw SQLError.createSQLException("Illegal minute value '" + minute + "' for java.sql.Time type in value '" + timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        }
        if (second < 0 || second > 59) {
            throw SQLError.createSQLException("Illegal minute value '" + second + "' for java.sql.Time type in value '" + timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        }
        synchronized (cal) {
            final Date origCalDate = cal.getTime();
            try {
                cal.clear();
                cal.set(1970, 0, 1, hour, minute, second);
                final long timeAsMillis = cal.getTimeInMillis();
                return new Time(timeAsMillis);
            }
            finally {
                cal.setTime(origCalDate);
            }
        }
    }
    
    static final Time fastTimeCreate(final int hour, final int minute, final int second, final Calendar targetCalendar, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        if (hour < 0 || hour > 23) {
            throw SQLError.createSQLException("Illegal hour value '" + hour + "' for java.sql.Time type in value '" + timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        }
        if (minute < 0 || minute > 59) {
            throw SQLError.createSQLException("Illegal minute value '" + minute + "' for java.sql.Time type in value '" + timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        }
        if (second < 0 || second > 59) {
            throw SQLError.createSQLException("Illegal minute value '" + second + "' for java.sql.Time type in value '" + timeFormattedString(hour, minute, second) + ".", "S1009", exceptionInterceptor);
        }
        final Calendar cal = (targetCalendar == null) ? new GregorianCalendar() : targetCalendar;
        synchronized (cal) {
            final Date origCalDate = cal.getTime();
            try {
                cal.clear();
                cal.set(1970, 0, 1, hour, minute, second);
                final long timeAsMillis = cal.getTimeInMillis();
                return new Time(timeAsMillis);
            }
            finally {
                cal.setTime(origCalDate);
            }
        }
    }
    
    static final Timestamp fastTimestampCreate(final boolean useGmtConversion, Calendar gmtCalIfNeeded, final Calendar cal, final int year, final int month, final int day, final int hour, final int minute, final int seconds, final int secondsPart) {
        synchronized (cal) {
            final Date origCalDate = cal.getTime();
            try {
                cal.clear();
                cal.set(year, month - 1, day, hour, minute, seconds);
                int offsetDiff = 0;
                if (useGmtConversion) {
                    final int fromOffset = cal.get(15) + cal.get(16);
                    if (gmtCalIfNeeded == null) {
                        gmtCalIfNeeded = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
                    }
                    gmtCalIfNeeded.clear();
                    gmtCalIfNeeded.setTimeInMillis(cal.getTimeInMillis());
                    final int toOffset = gmtCalIfNeeded.get(15) + gmtCalIfNeeded.get(16);
                    offsetDiff = fromOffset - toOffset;
                }
                if (secondsPart != 0) {
                    cal.set(14, secondsPart / 1000000);
                }
                final long tsAsMillis = cal.getTimeInMillis();
                final Timestamp ts = new Timestamp(tsAsMillis + offsetDiff);
                ts.setNanos(secondsPart);
                return ts;
            }
            finally {
                cal.setTime(origCalDate);
            }
        }
    }
    
    static final Timestamp fastTimestampCreate(final TimeZone tz, final int year, final int month, final int day, final int hour, final int minute, final int seconds, final int secondsPart) {
        final Calendar cal = (tz == null) ? new GregorianCalendar() : new GregorianCalendar(tz);
        cal.clear();
        cal.set(year, month - 1, day, hour, minute, seconds);
        final long tsAsMillis = cal.getTimeInMillis();
        final Timestamp ts = new Timestamp(tsAsMillis);
        ts.setNanos(secondsPart);
        return ts;
    }
    
    public static String getCanonicalTimezone(String timezoneStr, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        if (timezoneStr == null) {
            return null;
        }
        timezoneStr = timezoneStr.trim();
        if (timezoneStr.length() > 2 && (timezoneStr.charAt(0) == '+' || timezoneStr.charAt(0) == '-') && Character.isDigit(timezoneStr.charAt(1))) {
            return "GMT" + timezoneStr;
        }
        synchronized (TimeUtil.class) {
            if (TimeUtil.timeZoneMappings == null) {
                loadTimeZoneMappings(exceptionInterceptor);
            }
        }
        final String canonicalTz;
        if ((canonicalTz = TimeUtil.timeZoneMappings.getProperty(timezoneStr)) != null) {
            return canonicalTz;
        }
        throw SQLError.createSQLException(Messages.getString("TimeUtil.UnrecognizedTimezoneId", new Object[] { timezoneStr }), "01S00", exceptionInterceptor);
    }
    
    private static String timeFormattedString(final int hours, final int minutes, final int seconds) {
        final StringBuilder buf = new StringBuilder(8);
        if (hours < 10) {
            buf.append("0");
        }
        buf.append(hours);
        buf.append(":");
        if (minutes < 10) {
            buf.append("0");
        }
        buf.append(minutes);
        buf.append(":");
        if (seconds < 10) {
            buf.append("0");
        }
        buf.append(seconds);
        return buf.toString();
    }
    
    public static String formatNanos(int nanos, final boolean serverSupportsFracSecs, final boolean usingMicros) {
        if (nanos > 999999999) {
            nanos %= 100000000;
        }
        if (usingMicros) {
            nanos /= 1000;
        }
        if (!serverSupportsFracSecs || nanos == 0) {
            return "0";
        }
        final int digitCount = usingMicros ? 6 : 9;
        String nanosString = Integer.toString(nanos);
        final String zeroPadding = usingMicros ? "000000" : "000000000";
        int pos;
        for (nanosString = zeroPadding.substring(0, digitCount - nanosString.length()) + nanosString, pos = digitCount - 1; nanosString.charAt(pos) == '0'; --pos) {}
        nanosString = nanosString.substring(0, pos + 1);
        return nanosString;
    }
    
    private static void loadTimeZoneMappings(final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        TimeUtil.timeZoneMappings = new Properties();
        try {
            TimeUtil.timeZoneMappings.load(TimeUtil.class.getResourceAsStream("/com/alipay/oceanbase/jdbc/TimeZoneMapping.properties"));
        }
        catch (IOException e) {
            throw SQLError.createSQLException(Messages.getString("TimeUtil.LoadTimeZoneMappingError"), "01S00", exceptionInterceptor);
        }
        for (final String tz : TimeZone.getAvailableIDs()) {
            if (!TimeUtil.timeZoneMappings.containsKey(tz)) {
                TimeUtil.timeZoneMappings.put(tz, tz);
            }
        }
    }
    
    public static Timestamp truncateFractionalSeconds(final Timestamp timestamp) {
        final Timestamp truncatedTimestamp = new Timestamp(timestamp.getTime());
        truncatedTimestamp.setNanos(0);
        return truncatedTimestamp;
    }
    
    static {
        GMT_TIMEZONE = TimeZone.getTimeZone("GMT");
        DEFAULT_TIMEZONE = TimeZone.getDefault();
        TimeUtil.timeZoneMappings = null;
        Method aMethod;
        try {
            aMethod = System.class.getMethod("nanoTime", (Class<?>[])null);
        }
        catch (SecurityException e) {
            aMethod = null;
        }
        catch (NoSuchMethodException e2) {
            aMethod = null;
        }
        systemNanoTimeMethod = aMethod;
    }
}
