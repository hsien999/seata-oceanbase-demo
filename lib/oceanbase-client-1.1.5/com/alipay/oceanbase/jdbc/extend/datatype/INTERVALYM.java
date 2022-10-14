// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.extend.datatype;

import java.sql.SQLException;
import java.security.InvalidParameterException;

public class INTERVALYM extends Datum
{
    private static int MASKVAL;
    private static int INTYMYEAROFFSET;
    private static int INTYMMONTHOFFSET;
    private static int INTERVALYMMAXLENGTH;
    private static int MAXYEARPREC;
    private static int MAXMONTH;
    private static int INTERVALYM_BYTE_NUM;
    static final long serialVersionUID = 8393284561907159296L;
    
    public INTERVALYM() {
    }
    
    public INTERVALYM(final byte[] bytes) {
        super(bytes);
    }
    
    public INTERVALYM(final String str) {
        super(toBytes(str));
    }
    
    public static byte[] toBytes(final String var) {
        if (var == null) {
            return null;
        }
        final byte[] intervalym_bytes = new byte[INTERVALYM.INTERVALYM_BYTE_NUM];
        final String trimStr = var.trim();
        final char ch = var.charAt(0);
        int is_negative = 0;
        int year = 0;
        int month = 0;
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
        final String subStr = trimStr.substring(pos);
        final int index = subStr.indexOf(45);
        final String yearStr = subStr.substring(0, index);
        if (yearStr.length() > INTERVALYM.MAXYEARPREC) {
            throw new NumberFormatException("invalid year " + yearStr + " in " + var);
        }
        final String monthStr = subStr.substring(index + 1);
        year = Integer.valueOf(yearStr);
        month = Integer.valueOf(monthStr);
        if (month > INTERVALYM.MAXMONTH) {
            throw new NumberFormatException("invalid month " + month + " in " + var);
        }
        intervalym_bytes[0] = (byte)(is_negative & 0xFF);
        final byte[] tmpBytes = Datum.intToBytes(year);
        System.arraycopy(tmpBytes, 0, intervalym_bytes, 1, 4);
        intervalym_bytes[5] = (byte)(month & 0xFF);
        intervalym_bytes[6] = (byte)yearStr.length();
        return intervalym_bytes;
    }
    
    public static String toString(final byte[] data) {
        if (data == null || data.length == 0) {
            return new String();
        }
        if (data.length != INTERVALYM.INTERVALYM_BYTE_NUM) {
            throw new InvalidParameterException("invalid len:" + data.length);
        }
        final int is_negative = data[0] & INTERVALYM.MASKVAL;
        final int year = Datum.getInt(data, 1);
        final int month = data[5] & INTERVALYM.MASKVAL;
        final int year_scale = data[6] & INTERVALYM.MASKVAL;
        if (year < 0 || year > Math.pow(10.0, INTERVALYM.MAXYEARPREC)) {
            throw new NumberFormatException("year should not exceed " + Math.pow(10.0, INTERVALYM.MAXYEARPREC) + ", now is " + year);
        }
        if (month < 0 || month > INTERVALYM.MAXMONTH) {
            throw new NumberFormatException("month should not exceed " + INTERVALYM.MAXMONTH + ", now is " + month);
        }
        final String format = String.format("%%%dd-%%2d", year_scale);
        String result = String.format(format, year, month);
        final String[] s = result.split(" ");
        if (s.length > 1) {
            result = "";
            for (int i = 0; i < s.length; ++i) {
                result += s[i];
            }
        }
        if (is_negative == 0) {
            return result;
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
        final INTERVALYM[] object = new INTERVALYM[var1];
        return object;
    }
    
    @Override
    public String toString() {
        return toString(this.getBytes());
    }
    
    static {
        INTERVALYM.MASKVAL = 255;
        INTERVALYM.INTYMYEAROFFSET = Integer.MIN_VALUE;
        INTERVALYM.INTYMMONTHOFFSET = 60;
        INTERVALYM.INTERVALYMMAXLENGTH = 5;
        INTERVALYM.MAXYEARPREC = 9;
        INTERVALYM.MAXMONTH = 12;
        INTERVALYM.INTERVALYM_BYTE_NUM = 7;
    }
}
