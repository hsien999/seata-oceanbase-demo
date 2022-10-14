// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.extend.datatype;

import java.sql.SQLException;
import java.security.InvalidParameterException;
import java.util.StringTokenizer;

public class INTERVALDS extends Datum
{
    private static int MAXLEADPREC;
    private static int MAXHOUR;
    private static int MAXMINUTE;
    private static int MAXSECOND;
    private static final int INTERVALDS_BYTE_NUM = 14;
    private static int INTERVALDSMAXLENGTH;
    private static int INTERVALDSOFFSET;
    private static int INTERVALDAYOFFSET;
    static final long serialVersionUID = 7164731704878764759L;
    
    public INTERVALDS() {
    }
    
    public INTERVALDS(final byte[] bytes) {
        super(bytes);
    }
    
    public INTERVALDS(final String var) {
        super(toBytes(var));
    }
    
    public static byte[] toBytes(final String var) {
        if (var == null) {
            return null;
        }
        final byte[] intervalds_bytes = new byte[14];
        final String trimStr = var.trim();
        final char ch = var.charAt(0);
        int is_negative = 0;
        int day = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;
        int fractional_second = 0;
        int pos = 0;
        if (ch != '-' && ch != '+') {
            pos = 0;
        }
        else {
            pos = 1;
            if (ch == '-') {
                is_negative = 1;
            }
        }
        String subStr = trimStr.substring(pos);
        final int index = subStr.indexOf(32);
        final String dayStr = subStr.substring(0, index);
        if (dayStr.length() > INTERVALDS.MAXLEADPREC) {
            throw new NumberFormatException("invalid daylen " + dayStr);
        }
        day = Integer.valueOf(dayStr);
        subStr = subStr.substring(index + 1);
        final StringTokenizer tokenizer = new StringTokenizer(subStr, ":.");
        if (!tokenizer.hasMoreTokens()) {
            throw new NumberFormatException("invalid format " + var);
        }
        String hourStr;
        String minuteStr;
        String secondStr;
        String fracSecondStr;
        try {
            hourStr = tokenizer.nextToken();
            minuteStr = tokenizer.nextToken();
            secondStr = tokenizer.nextToken();
            try {
                fracSecondStr = tokenizer.nextToken();
            }
            catch (Exception ex) {
                fracSecondStr = null;
            }
        }
        catch (Exception ex) {
            throw new NumberFormatException("invalid format " + var);
        }
        hour = Integer.valueOf(hourStr);
        minute = Integer.valueOf(minuteStr);
        second = Integer.valueOf(secondStr);
        if (hour > INTERVALDS.MAXHOUR) {
            throw new NumberFormatException("invalid hour " + hour + " in " + var);
        }
        if (minute > INTERVALDS.MAXMINUTE) {
            throw new NumberFormatException("invalid minute  " + minute + " in " + var);
        }
        if (second > INTERVALDS.MAXSECOND) {
            throw new NumberFormatException("invalid second  " + second + " in " + var);
        }
        if (fracSecondStr != null) {
            if (fracSecondStr.length() > INTERVALDS.MAXLEADPREC) {
                throw new NumberFormatException("invalid fracsecond length " + fracSecondStr + " in " + var);
            }
            fractional_second = Integer.valueOf(fracSecondStr);
            for (int lenDiff = INTERVALDS.MAXLEADPREC - fracSecondStr.length(), i = 0; i < lenDiff; ++i) {
                fractional_second *= 10;
            }
        }
        intervalds_bytes[0] = (byte)(is_negative & 0xFF);
        byte[] tmpBytes = Datum.intToBytes(day);
        System.arraycopy(tmpBytes, 0, intervalds_bytes, 1, 4);
        intervalds_bytes[5] = (byte)(hour & 0xFF);
        intervalds_bytes[6] = (byte)(minute & 0xFF);
        intervalds_bytes[7] = (byte)(second & 0xFF);
        tmpBytes = Datum.intToBytes(fractional_second);
        System.arraycopy(tmpBytes, 0, intervalds_bytes, 8, 4);
        intervalds_bytes[12] = (byte)(dayStr.length() & 0xFF);
        intervalds_bytes[13] = (byte)INTERVALDS.MAXLEADPREC;
        return intervalds_bytes;
    }
    
    public static String toString(final byte[] data) {
        if (data == null || data.length == 0) {
            return new String();
        }
        if (data.length != 14) {
            throw new InvalidParameterException("invalid len:" + data.length);
        }
        final int is_negative = data[0] & 0xFF;
        final int day = Datum.getInt(data, 1);
        final int hour = data[5] & 0xFF;
        final int minute = data[6] & 0xFF;
        final int second = data[7] & 0xFF;
        int fractional_second = Datum.getInt(data, 8);
        final int day_scale = data[12] & 0xFF;
        final int fractional_second_scale = data[13] & 0xFF;
        if (day < 0 || day > Math.pow(10.0, INTERVALDS.MAXLEADPREC)) {
            throw new NumberFormatException("day should not exceed " + Math.pow(10.0, INTERVALDS.MAXLEADPREC) + ", now is " + day);
        }
        if (hour < 0 || hour > INTERVALDS.MAXHOUR) {
            throw new NumberFormatException("hour should not exceed " + INTERVALDS.MAXHOUR + ", now is " + hour);
        }
        if (minute < 0 || minute > INTERVALDS.MAXMINUTE) {
            throw new NumberFormatException("minute should not exceed " + INTERVALDS.MAXMINUTE + ", now is " + minute);
        }
        if (second < 0 || second > INTERVALDS.MAXSECOND) {
            throw new NumberFormatException("second should not exceed " + INTERVALDS.MAXSECOND + ", now is " + second);
        }
        if (fractional_second < 0 || fractional_second > Math.pow(10.0, INTERVALDS.MAXLEADPREC)) {
            throw new NumberFormatException("fractional_second should not exceed " + Math.pow(10.0, INTERVALDS.MAXLEADPREC) + ", now is " + fractional_second);
        }
        if (fractional_second_scale < 0 || fractional_second_scale > INTERVALDS.MAXLEADPREC) {
            throw new NumberFormatException("fractional_second_scale should not exceed " + INTERVALDS.MAXLEADPREC + ", now is " + fractional_second_scale);
        }
        if (fractional_second_scale < INTERVALDS.MAXLEADPREC) {
            fractional_second /= (int)Math.pow(10.0, INTERVALDS.MAXLEADPREC - fractional_second_scale);
        }
        final String format = String.format("%%0%dd %%02d:%%02d:%%02d.%%0%dd", day_scale, fractional_second_scale);
        final String result = String.format(format, day, hour, minute, second, fractional_second);
        if (is_negative == 0) {
            return "+" + result;
        }
        return "-" + result;
    }
    
    @Override
    public String stringValue() {
        return toString(this.getBytes());
    }
    
    @Override
    public boolean isConvertibleTo(final Class var1) {
        return var1.getName().compareTo("java.lang.String") == 0;
    }
    
    @Override
    public Object toJdbc() throws SQLException {
        return this;
    }
    
    @Override
    public Object makeJdbcArray(final int var1) {
        final INTERVALDS[] object = new INTERVALDS[var1];
        return object;
    }
    
    @Override
    public String toString() {
        return toString(this.getBytes());
    }
    
    static {
        INTERVALDS.MAXLEADPREC = 9;
        INTERVALDS.MAXHOUR = 23;
        INTERVALDS.MAXMINUTE = 59;
        INTERVALDS.MAXSECOND = 59;
        INTERVALDS.INTERVALDSMAXLENGTH = 11;
        INTERVALDS.INTERVALDSOFFSET = 60;
        INTERVALDS.INTERVALDAYOFFSET = Integer.MIN_VALUE;
    }
}
