// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.resultset.rowprotocol;

import java.time.format.DateTimeFormatterBuilder;
import com.oceanbase.jdbc.JDBC4ResultSet;
import com.oceanbase.jdbc.internal.com.read.resultset.SelectResultSet;
import com.oceanbase.jdbc.extend.datatype.BINARY_FLOAT;
import com.oceanbase.jdbc.extend.datatype.BINARY_DOUBLE;
import com.oceanbase.jdbc.extend.datatype.NUMBER_FLOAT;
import com.oceanbase.jdbc.extend.datatype.NUMBER;
import com.oceanbase.jdbc.extend.datatype.INTERVALYM;
import com.oceanbase.jdbc.extend.datatype.INTERVALDS;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetTime;
import java.time.ZonedDateTime;
import java.math.BigInteger;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMPLTZ;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMPTZ;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMP;
import com.oceanbase.jdbc.extend.datatype.ComplexData;
import java.sql.Struct;
import java.sql.Array;
import com.oceanbase.jdbc.extend.datatype.ComplexDataType;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.TimeZone;
import java.util.Calendar;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import com.oceanbase.jdbc.internal.ColumnType;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import java.util.regex.Pattern;
import java.time.format.DateTimeFormatter;

public abstract class RowProtocol
{
    public static final int BIT_LAST_FIELD_NOT_NULL = 0;
    public static final int BIT_LAST_FIELD_NULL = 1;
    public static final int BIT_LAST_ZERO_DATE = 2;
    public static final int TINYINT1_IS_BIT = 1;
    public static final int YEAR_IS_DATE_TYPE = 2;
    public static final DateTimeFormatter TEXT_LOCAL_DATE_TIME;
    public static final DateTimeFormatter TEXT_OFFSET_DATE_TIME;
    public static final DateTimeFormatter TEXT_ZONED_DATE_TIME;
    public static final Pattern isIntegerRegex;
    protected static final int NULL_LENGTH = -1;
    private Protocol protocol;
    protected final int maxFieldSize;
    protected final Options options;
    public int lastValueNull;
    public byte[] buf;
    public int pos;
    public int length;
    protected int index;
    public int[] complexEndPos;
    
    public RowProtocol(final int maxFieldSize, final Options options) {
        this.maxFieldSize = maxFieldSize;
        this.options = options;
    }
    
    public void resetRow(final byte[] buf) {
        this.buf = buf;
        this.index = -1;
    }
    
    Charset getCurrentEncoding(final ColumnType columnType) {
        switch (columnType) {
            case VARCHAR:
            case VARCHAR2:
            case VARSTRING:
            case NVARCHAR2:
            case NCHAR:
            case RAW:
            case STRING: {
                return Charset.forName(this.options.characterEncoding);
            }
            default: {
                return StandardCharsets.UTF_8;
            }
        }
    }
    
    public abstract void setPosition(final int p0);
    
    public int getLengthMaxFieldSize() {
        return (this.maxFieldSize != 0 && this.maxFieldSize < this.length) ? this.maxFieldSize : this.length;
    }
    
    public int getMaxFieldSize() {
        return this.maxFieldSize;
    }
    
    public abstract String getInternalString(final ColumnDefinition p0, final Calendar p1, final TimeZone p2) throws SQLException;
    
    public abstract int getInternalInt(final ColumnDefinition p0) throws SQLException;
    
    public abstract long getInternalLong(final ColumnDefinition p0) throws SQLException;
    
    public abstract float getInternalFloat(final ColumnDefinition p0) throws SQLException;
    
    public abstract double getInternalDouble(final ColumnDefinition p0) throws SQLException;
    
    public abstract BigDecimal getInternalBigDecimal(final ColumnDefinition p0) throws SQLException;
    
    public abstract Date getInternalDate(final ColumnDefinition p0, final Calendar p1, final TimeZone p2) throws SQLException;
    
    public abstract Time getInternalTime(final ColumnDefinition p0, final Calendar p1, final TimeZone p2) throws SQLException;
    
    public abstract Timestamp getInternalTimestamp(final ColumnDefinition p0, final Calendar p1, final TimeZone p2) throws SQLException;
    
    public abstract Array getInternalArray(final ColumnDefinition p0, final ComplexDataType p1) throws SQLException;
    
    public abstract Struct getInternalStruct(final ColumnDefinition p0, final ComplexDataType p1) throws SQLException;
    
    public abstract ComplexData getInternalComplexCursor(final ColumnDefinition p0, final ComplexDataType p1) throws SQLException;
    
    public String getEncoding() {
        return this.options.characterEncoding;
    }
    
    public TIMESTAMP getInternalTIMESTAMP(final ColumnDefinition columnInfo, final Calendar userCalendar, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (columnInfo.getColumnType() != ColumnType.TIMESTAMP_NANO) {
            final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
            throw new SQLException("Value type \"" + columnInfo.getColumnType().getTypeName() + "\" with value \"" + value + "\" cannot be parse as TIMESTAMP");
        }
        if (this.length < 12) {
            throw new SQLException("timestamp field data length is invalid, expected 12 at least, actual length is " + this.length);
        }
        return this.buildTIMETAMP(this.buf, this.pos, this.length);
    }
    
    public TIMESTAMPTZ getInternalTIMESTAMPTZ(final ColumnDefinition columnInfo, final Calendar userCalendar, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (columnInfo.getColumnType() != ColumnType.TIMESTAMP_TZ) {
            final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
            throw new SQLException("Value type \"" + columnInfo.getColumnType().getTypeName() + "\" with value \"" + value + "\" cannot be parse as TIMESTAMP");
        }
        if (this.length < 12) {
            throw new SQLException("timestamp field data length is invalid, expected 12 at least, actual length is " + this.length);
        }
        final byte[] returnBytes = new byte[this.length];
        System.arraycopy(this.buf, this.pos, returnBytes, 0, this.length);
        final TIMESTAMPTZ timestamptz = new TIMESTAMPTZ(returnBytes);
        timestamptz.setByte(11, returnBytes[11]);
        return timestamptz;
    }
    
    public TIMESTAMPLTZ getInternalTIMESTAMPLTZ(final ColumnDefinition columnInfo, final Calendar userCalendar, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (columnInfo.getColumnType() != ColumnType.TIMESTAMP_LTZ) {
            final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
            throw new SQLException("Value type \"" + columnInfo.getColumnType().getTypeName() + "\" with value \"" + value + "\" cannot be parse as TIMESTAMP");
        }
        if (this.length < 12) {
            throw new SQLException("timestamp field data length is invalid, expected 12 at least, actual length is " + this.length);
        }
        final byte[] returnBytes = new byte[this.length];
        System.arraycopy(this.buf, this.pos, returnBytes, 0, this.length);
        return new TIMESTAMPLTZ(returnBytes);
    }
    
    private TIMESTAMP buildTIMETAMP(final byte[] bytes, final int pos, final int length) throws SQLException {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.buildTimestamp(bytes[pos]));
        sb.append(this.buildTimestamp(bytes[pos + 1]));
        sb.append("-");
        sb.append(this.buildTimestamp(bytes[pos + 2]));
        sb.append("-");
        sb.append(this.buildTimestamp(bytes[pos + 3]));
        sb.append(" ");
        sb.append(this.buildTimestamp(bytes[pos + 4]));
        sb.append(":");
        sb.append(this.buildTimestamp(bytes[pos + 5]));
        sb.append(":");
        sb.append(this.buildTimestamp(bytes[pos + 6]));
        sb.append(".");
        final byte[] nanosBytes = new byte[4];
        System.arraycopy(bytes, pos + 7, nanosBytes, 0, 4);
        final int nanos = TIMESTAMP.getNanos(nanosBytes, 0);
        String temp = String.format("%09d", nanos);
        char[] chars;
        int index;
        for (chars = temp.toCharArray(), index = chars.length; index > 1 && chars[index - 1] == '0'; --index) {}
        temp = temp.substring(0, index);
        final int scale = bytes[pos + 11];
        if (scale > temp.length()) {
            final int x = scale - temp.length();
            final StringBuilder strBuf = new StringBuilder();
            for (int i = 0; i < x; ++i) {
                strBuf.append("0");
            }
            temp += strBuf.toString();
        }
        sb.append(temp);
        final TIMESTAMP timestamp = new TIMESTAMP(Timestamp.valueOf(sb.toString()));
        timestamp.setByte(11, bytes[pos + 11]);
        return timestamp;
    }
    
    private String buildTimestamp(final byte b) {
        if (b < 10) {
            return "0" + b;
        }
        return "" + b;
    }
    
    public abstract Object getInternalObject(final ColumnDefinition p0, final TimeZone p1) throws SQLException;
    
    public abstract boolean getInternalBoolean(final ColumnDefinition p0) throws SQLException;
    
    public abstract byte getInternalByte(final ColumnDefinition p0) throws SQLException;
    
    public abstract short getInternalShort(final ColumnDefinition p0) throws SQLException;
    
    public abstract String getInternalTimeString(final ColumnDefinition p0);
    
    public abstract BigInteger getInternalBigInteger(final ColumnDefinition p0) throws SQLException;
    
    public abstract ZonedDateTime getInternalZonedDateTime(final ColumnDefinition p0, final Class p1, final TimeZone p2) throws SQLException;
    
    public abstract OffsetTime getInternalOffsetTime(final ColumnDefinition p0, final TimeZone p1) throws SQLException;
    
    public abstract LocalTime getInternalLocalTime(final ColumnDefinition p0, final TimeZone p1) throws SQLException;
    
    public abstract LocalDate getInternalLocalDate(final ColumnDefinition p0, final TimeZone p1) throws SQLException;
    
    public abstract INTERVALDS getInternalINTERVALDS(final ColumnDefinition p0) throws SQLException;
    
    public abstract INTERVALYM getInternalINTERVALYM(final ColumnDefinition p0) throws SQLException;
    
    public abstract boolean isBinaryEncoded();
    
    public boolean lastValueWasNull() {
        return (this.lastValueNull & 0x1) != 0x0;
    }
    
    protected String zeroFillingIfNeeded(final String value, final ColumnDefinition columnDefinition) {
        if (columnDefinition.isZeroFill()) {
            final StringBuilder zeroAppendStr = new StringBuilder();
            long zeroToAdd = columnDefinition.getDisplaySize() - value.length();
            while (zeroToAdd-- > 0L) {
                zeroAppendStr.append("0");
            }
            return zeroAppendStr.append(value).toString();
        }
        return value;
    }
    
    protected int getInternalTinyInt(final ColumnDefinition columnInfo) {
        if (this.lastValueWasNull()) {
            return 0;
        }
        int value = this.buf[this.pos];
        if (!columnInfo.isSigned()) {
            value = (this.buf[this.pos] & 0xFF);
        }
        return value;
    }
    
    protected long parseBit() {
        if (this.length == 1) {
            return this.buf[this.pos];
        }
        long val = 0L;
        int ind = 0;
        do {
            val += (long)(this.buf[this.pos + ind] & 0xFF) << 8 * (this.length - ++ind);
        } while (ind < this.length);
        return val;
    }
    
    protected int getInternalSmallInt(final ColumnDefinition columnInfo) {
        if (this.lastValueWasNull()) {
            return 0;
        }
        final int value = (this.buf[this.pos] & 0xFF) + ((this.buf[this.pos + 1] & 0xFF) << 8);
        if (!columnInfo.isSigned()) {
            return value & 0xFFFF;
        }
        return (short)value;
    }
    
    protected long getInternalMediumInt(final ColumnDefinition columnInfo) {
        if (this.lastValueWasNull()) {
            return 0L;
        }
        long value = (this.buf[this.pos] & 0xFF) + ((this.buf[this.pos + 1] & 0xFF) << 8) + ((this.buf[this.pos + 2] & 0xFF) << 16) + ((this.buf[this.pos + 3] & 0xFF) << 24);
        if (!columnInfo.isSigned()) {
            value &= 0xFFFFFFFFL;
        }
        return value;
    }
    
    boolean isNUMBERTYPE(final ColumnType columnType) {
        return columnType == ColumnType.NUMBER || columnType == ColumnType.FLOAT || columnType == ColumnType.DECIMAL || columnType == ColumnType.BINARY_DOUBLE || columnType == ColumnType.BINARY_FLOAT;
    }
    
    protected NUMBER zgetNUMBER(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (this.isNUMBERTYPE(columnInfo.getColumnType())) {
            final byte[] b = new byte[this.length];
            System.arraycopy(this.buf, this.pos, b, 0, this.length);
            return new NUMBER(b);
        }
        final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
        throw new SQLException("Value type \"" + columnInfo.getColumnType().getTypeName() + "\" with value \"" + value + "\" cannot be parse as NUMBER");
    }
    
    protected NUMBER_FLOAT getNUMBER_FLOAT(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
        if (columnInfo.getColumnType() == ColumnType.NUMBER_FLOAT) {
            return new NUMBER_FLOAT(new Float(value), this.buf);
        }
        throw new SQLException("Value type \"" + columnInfo.getColumnType().getTypeName() + "\" with value \"" + value + "\" cannot be parse as NUMBER_FLOAT");
    }
    
    protected BINARY_DOUBLE getBINARY_DOUBLE(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (columnInfo.getColumnType() == ColumnType.BINARY_DOUBLE) {
            return new BINARY_DOUBLE(this.buf[this.pos]);
        }
        final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
        throw new SQLException("Value type \"" + columnInfo.getColumnType().getTypeName() + "\" with value \"" + value + "\" cannot be parse as BINARY_DOUBLE");
    }
    
    protected BINARY_FLOAT getBINARY_FLOAT(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (columnInfo.getColumnType() == ColumnType.BINARY_FLOAT) {
            return new BINARY_FLOAT(this.buf[this.pos]);
        }
        final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
        throw new SQLException("Value type \"" + columnInfo.getColumnType().getTypeName() + "\" with value \"" + value + "\" cannot be parse as BINARY_FLOAT");
    }
    
    protected INTERVALDS getINTERVALDS(final ColumnDefinition columnInfo) throws Exception {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (columnInfo.getColumnType() == ColumnType.INTERVALDS) {
            return new INTERVALDS(this.buf);
        }
        final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
        throw new SQLException("Value type \"" + columnInfo.getColumnType().getTypeName() + "\" with value \"" + value + "\" cannot be parse as INTERVALDS");
    }
    
    protected INTERVALYM getINTERVALYM(final ColumnDefinition columnInfo) throws Exception {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (columnInfo.getColumnType() == ColumnType.INTERVALYM) {
            return new INTERVALYM(this.buf);
        }
        final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
        throw new SQLException("Value type \"" + columnInfo.getColumnType().getTypeName() + "\" with value \"" + value + "\" cannot be parse as INTERVALYM");
    }
    
    protected void rangeCheck(final Object className, final long minValue, final long maxValue, final BigDecimal value, final ColumnDefinition columnInfo) throws SQLException {
        if (value.compareTo(BigDecimal.valueOf(minValue)) < 0 || value.compareTo(BigDecimal.valueOf(maxValue)) > 0) {
            throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' : value " + value + " is not in " + className + " range", "22003", 1264);
        }
    }
    
    protected void rangeCheck(final Object className, final long minValue, final long maxValue, final long value, final ColumnDefinition columnInfo) throws SQLException {
        if (value < minValue || value > maxValue) {
            throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' : value " + value + " is not in " + className + " range", "22003", 1264);
        }
    }
    
    protected int extractNanos(final String timestring) throws SQLException {
        final int index = timestring.indexOf(46);
        if (index == -1) {
            return 0;
        }
        int nanos = 0;
        for (int i = index + 1; i < index + 10; ++i) {
            int digit;
            if (i >= timestring.length()) {
                digit = 0;
            }
            else {
                final char value = timestring.charAt(i);
                if (value < '0' || value > '9') {
                    throw new SQLException("cannot parse sub-second part in timestamp string '" + timestring + "'");
                }
                digit = value - '0';
            }
            nanos = nanos * 10 + digit;
        }
        return nanos;
    }
    
    public boolean wasNull() {
        return (this.lastValueNull & 0x1) != 0x0 || (this.lastValueNull & 0x2) != 0x0;
    }
    
    public Protocol getProtocol() {
        return this.protocol;
    }
    
    public void setProtocol(final Protocol protocol) {
        this.protocol = protocol;
    }
    
    public SelectResultSet sendFechRowViaCursor(final long statementid, final int fetchSize) {
        return (SelectResultSet)JDBC4ResultSet.createEmptyResultSet();
    }
    
    static {
        isIntegerRegex = Pattern.compile("^-?\\d+\\.[0-9]+$");
        TEXT_LOCAL_DATE_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive().append(DateTimeFormatter.ISO_LOCAL_DATE).appendLiteral(' ').append(DateTimeFormatter.ISO_LOCAL_TIME).toFormatter();
        TEXT_OFFSET_DATE_TIME = new DateTimeFormatterBuilder().parseCaseInsensitive().append(RowProtocol.TEXT_LOCAL_DATE_TIME).appendOffsetId().toFormatter();
        TEXT_ZONED_DATE_TIME = new DateTimeFormatterBuilder().append(RowProtocol.TEXT_OFFSET_DATE_TIME).optionalStart().appendLiteral('[').parseCaseSensitive().appendZoneRegionId().appendLiteral(']').toFormatter();
    }
}
