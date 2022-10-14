// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.resultset.rowprotocol;

import com.oceanbase.jdbc.extend.datatype.BINARY_FLOAT;
import com.oceanbase.jdbc.extend.datatype.BINARY_DOUBLE;
import com.oceanbase.jdbc.extend.datatype.NUMBER_FLOAT;
import com.oceanbase.jdbc.extend.datatype.NUMBER;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.OffsetTime;
import java.time.format.DateTimeParseException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.math.BigInteger;
import com.oceanbase.jdbc.extend.datatype.ComplexData;
import java.sql.Struct;
import java.sql.Array;
import com.oceanbase.jdbc.extend.datatype.ComplexDataType;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMP;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMPLTZ;
import java.sql.Time;
import java.text.DateFormat;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMPTZ;
import java.text.ParseException;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import java.text.SimpleDateFormat;
import com.oceanbase.jdbc.internal.ColumnType;
import java.sql.SQLException;
import java.math.BigDecimal;
import com.oceanbase.jdbc.extend.datatype.INTERVALYM;
import com.oceanbase.jdbc.extend.datatype.INTERVALDS;
import java.sql.Timestamp;
import java.sql.Date;
import com.oceanbase.jdbc.internal.util.Utils;
import java.util.TimeZone;
import java.util.Calendar;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import com.oceanbase.jdbc.util.Options;

public class TextRowProtocol extends RowProtocol
{
    public TextRowProtocol(final int maxFieldSize, final Options options) {
        super(maxFieldSize, options);
    }
    
    @Override
    public void setPosition(final int newIndex) {
        if (this.index != newIndex) {
            if (this.index == -1 || this.index > newIndex) {
                this.pos = 0;
                this.index = 0;
            }
            else {
                ++this.index;
                if (this.length != -1) {
                    this.pos += this.length;
                }
            }
            while (this.index <= newIndex) {
                if (this.index == newIndex) {
                    final int type = this.buf[this.pos++] & 0xFF;
                    switch (type) {
                        case 251: {
                            this.length = -1;
                            this.lastValueNull = 1;
                            return;
                        }
                        case 252: {
                            this.length = (0xFFFF & (this.buf[this.pos++] & 0xFF) + ((this.buf[this.pos++] & 0xFF) << 8));
                            break;
                        }
                        case 253: {
                            this.length = (0xFFFFFF & (this.buf[this.pos++] & 0xFF) + ((this.buf[this.pos++] & 0xFF) << 8) + ((this.buf[this.pos++] & 0xFF) << 16));
                            break;
                        }
                        case 254: {
                            this.length = (int)((this.buf[this.pos++] & 0xFF) + ((long)(this.buf[this.pos++] & 0xFF) << 8) + ((long)(this.buf[this.pos++] & 0xFF) << 16) + ((long)(this.buf[this.pos++] & 0xFF) << 24) + ((long)(this.buf[this.pos++] & 0xFF) << 32) + ((long)(this.buf[this.pos++] & 0xFF) << 40) + ((long)(this.buf[this.pos++] & 0xFF) << 48) + ((long)(this.buf[this.pos++] & 0xFF) << 56));
                            break;
                        }
                        default: {
                            this.length = type;
                            break;
                        }
                    }
                    this.lastValueNull = 0;
                    return;
                }
                final int type = this.buf[this.pos++] & 0xFF;
                switch (type) {
                    case 251: {
                        break;
                    }
                    case 252: {
                        this.pos += 2 + (0xFFFF & (this.buf[this.pos] & 0xFF) + ((this.buf[this.pos + 1] & 0xFF) << 8));
                        break;
                    }
                    case 253: {
                        this.pos += 3 + (0xFFFFFF & (this.buf[this.pos] & 0xFF) + ((this.buf[this.pos + 1] & 0xFF) << 8) + ((this.buf[this.pos + 2] & 0xFF) << 16));
                        break;
                    }
                    case 254: {
                        this.pos += (int)(8L + ((this.buf[this.pos] & 0xFF) + ((long)(this.buf[this.pos + 1] & 0xFF) << 8) + ((long)(this.buf[this.pos + 2] & 0xFF) << 16) + ((long)(this.buf[this.pos + 3] & 0xFF) << 24) + ((long)(this.buf[this.pos + 4] & 0xFF) << 32) + ((long)(this.buf[this.pos + 5] & 0xFF) << 40) + ((long)(this.buf[this.pos + 6] & 0xFF) << 48) + ((long)(this.buf[this.pos + 7] & 0xFF) << 56)));
                        break;
                    }
                    default: {
                        this.pos += type;
                        break;
                    }
                }
                ++this.index;
            }
        }
        this.lastValueNull = ((this.length == -1) ? 1 : 0);
    }
    
    @Override
    public String getInternalString(final ColumnDefinition columnInfo, final Calendar cal, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        switch (columnInfo.getColumnType()) {
            case BIT: {
                return String.valueOf(this.parseBit());
            }
            case DOUBLE:
            case FLOAT: {
                return this.zeroFillingIfNeeded(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())), columnInfo);
            }
            case TIME: {
                return this.getInternalTimeString(columnInfo);
            }
            case DATE: {
                final Date date = this.getInternalDate(columnInfo, cal, TimeZone.getDefault());
                if (date != null) {
                    return date.toString();
                }
                if ((this.lastValueNull & 0x2) != 0x0) {
                    this.lastValueNull ^= 0x2;
                    return new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                }
                return null;
            }
            case YEAR: {
                if (this.options.yearIsDateType) {
                    final Date date2 = this.getInternalDate(columnInfo, cal, TimeZone.getDefault());
                    return (date2 == null) ? null : date2.toString();
                }
                break;
            }
            case TIMESTAMP:
            case TIMESTAMP_NANO:
            case DATETIME: {
                final Timestamp timestamp = this.getInternalTimestamp(columnInfo, cal, TimeZone.getDefault());
                if (timestamp != null) {
                    return timestamp.toString();
                }
                if ((this.lastValueNull & 0x2) != 0x0) {
                    this.lastValueNull ^= 0x2;
                    return new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                }
                return null;
            }
            case INTERVALDS: {
                final INTERVALDS intervalds = this.getInternalINTERVALDS(columnInfo);
                return intervalds.toString();
            }
            case INTERVALYM: {
                final INTERVALYM intervalym = this.getInternalINTERVALYM(columnInfo);
                return intervalym.toString();
            }
            case DECIMAL:
            case OLDDECIMAL: {
                final BigDecimal bigDecimal = this.getInternalBigDecimal(columnInfo);
                return (bigDecimal == null) ? null : this.zeroFillingIfNeeded(bigDecimal.toString(), columnInfo);
            }
            case RAW: {
                final byte[] data = new byte[this.length];
                System.arraycopy(this.buf, this.pos, data, 0, this.length);
                if (data != null) {
                    final boolean wasNullFlag = false;
                    return Utils.toHexString(data);
                }
                final boolean wasNullFlag = true;
                return null;
            }
            case BINARY_FLOAT: {
                final Float f = Float.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                return Float.toString(f);
            }
            case BINARY_DOUBLE: {
                final Double d = Double.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                return Double.toString(d);
            }
            case NULL: {
                return null;
            }
        }
        if (this.maxFieldSize > 0) {
            return new String(this.buf, this.pos, Math.min(this.maxFieldSize * 3, this.length), this.getCurrentEncoding(columnInfo.getColumnType())).substring(0, Math.min(this.maxFieldSize, this.length));
        }
        return new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
    }
    
    @Override
    public int getInternalInt(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return 0;
        }
        final long value = this.getInternalLong(columnInfo);
        this.rangeCheck(Integer.class, -2147483648L, 2147483647L, value, columnInfo);
        return (int)value;
    }
    
    @Override
    public long getInternalLong(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return 0L;
        }
        try {
            switch (columnInfo.getColumnType()) {
                case FLOAT: {
                    final Float floatValue = Float.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                    if (floatValue.compareTo(9.223372E18f) >= 1) {
                        throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' : value " + new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())) + " is not in Long range", "22003", 1264);
                    }
                    return floatValue.longValue();
                }
                case DOUBLE: {
                    final Double doubleValue = Double.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                    if (doubleValue.compareTo(9.223372036854776E18) >= 1) {
                        throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' : value " + new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())) + " is not in Long range", "22003", 1264);
                    }
                    return doubleValue.longValue();
                }
                case BIT: {
                    return this.parseBit();
                }
                case YEAR:
                case TINYINT:
                case SMALLINT:
                case INTEGER:
                case MEDIUMINT:
                case BIGINT: {
                    long result = 0L;
                    boolean negate = false;
                    int begin = this.pos;
                    if (this.length > 0 && this.buf[begin] == 45) {
                        negate = true;
                        ++begin;
                    }
                    while (begin < this.pos + this.length) {
                        result = result * 10L + this.buf[begin] - 48L;
                        ++begin;
                    }
                    if (result >= 0L) {
                        return negate ? (-1L * result) : result;
                    }
                    if (result == Long.MIN_VALUE && negate) {
                        return Long.MIN_VALUE;
                    }
                    throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' for value " + new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())), "22003", 1264);
                }
                default: {
                    return Long.parseLong(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())).trim());
                }
            }
        }
        catch (NumberFormatException nfe) {
            final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
            if (TextRowProtocol.isIntegerRegex.matcher(value).find()) {
                try {
                    return Long.parseLong(value.substring(0, value.indexOf(".")));
                }
                catch (NumberFormatException ex) {}
            }
            throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' : value " + value, "22003", 1264);
        }
    }
    
    @Override
    public float getInternalFloat(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return 0.0f;
        }
        switch (columnInfo.getColumnType()) {
            case BIT: {
                return (float)this.parseBit();
            }
            case DOUBLE:
            case FLOAT:
            case YEAR:
            case DECIMAL:
            case OLDDECIMAL:
            case BINARY_FLOAT:
            case TINYINT:
            case SMALLINT:
            case INTEGER:
            case MEDIUMINT:
            case BIGINT:
            case VARSTRING:
            case VARCHAR:
            case VARCHAR2:
            case NVARCHAR2:
            case STRING:
            case NUMBER_FLOAT: {
                try {
                    return Float.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                }
                catch (NumberFormatException nfe) {
                    final SQLException sqlException = new SQLException("Incorrect format \"" + new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())) + "\" for getFloat for data field with type " + columnInfo.getColumnType().getJavaTypeName(), "22003", 1264);
                    sqlException.initCause(nfe);
                    throw sqlException;
                }
            }
            case OBDECIMAL: {
                final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())).trim();
                final BigDecimal bigDecimal = new BigDecimal(value);
                return bigDecimal.floatValue();
            }
            default: {
                throw new SQLException("getFloat not available for data field type " + columnInfo.getColumnType().getJavaTypeName());
            }
        }
    }
    
    @Override
    public double getInternalDouble(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return 0.0;
        }
        switch (columnInfo.getColumnType()) {
            case BIT: {
                return (double)this.parseBit();
            }
            case DOUBLE:
            case FLOAT:
            case YEAR:
            case DECIMAL:
            case OLDDECIMAL:
            case BINARY_DOUBLE:
            case TINYINT:
            case SMALLINT:
            case INTEGER:
            case MEDIUMINT:
            case BIGINT:
            case VARSTRING:
            case VARCHAR:
            case VARCHAR2:
            case STRING: {
                try {
                    return Double.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                }
                catch (NumberFormatException nfe) {
                    final SQLException sqlException = new SQLException("Incorrect format \"" + new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())) + "\" for getDouble for data field with type " + columnInfo.getColumnType().getJavaTypeName(), "22003", 1264);
                    sqlException.initCause(nfe);
                    throw sqlException;
                }
            }
            case BINARY_FLOAT:
            case NUMBER_FLOAT: {
                final Float f = Float.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                return f;
            }
            case OBDECIMAL: {
                final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                final BigDecimal bigDecimal = new BigDecimal(value);
                return bigDecimal.doubleValue();
            }
            default: {
                throw new SQLException("getDouble not available for data field type " + columnInfo.getColumnType().getJavaTypeName());
            }
        }
    }
    
    @Override
    public BigDecimal getInternalBigDecimal(final ColumnDefinition columnInfo) {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (columnInfo.getColumnType() == ColumnType.BIT) {
            return BigDecimal.valueOf(this.parseBit());
        }
        switch (columnInfo.getColumnType()) {
            case BINARY_FLOAT: {
                final Float f = Float.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                return BigDecimal.valueOf(f);
            }
            case BINARY_DOUBLE: {
                final Double d = Double.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                return BigDecimal.valueOf(d);
            }
            default: {
                final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())).trim();
                return new BigDecimal(value);
            }
        }
    }
    
    @Override
    public Date getInternalDate(final ColumnDefinition columnInfo, final Calendar cal, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        switch (columnInfo.getColumnType()) {
            case DATE: {
                final int[] datePart = { 0, 0, 0 };
                int partIdx = 0;
                for (int begin = this.pos; begin < this.pos + this.length; ++begin) {
                    final byte b = this.buf[begin];
                    if (b == 45) {
                        ++partIdx;
                    }
                    else {
                        if (b < 48 || b > 57) {
                            throw new SQLException("cannot parse data in date string '" + new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())) + "'");
                        }
                        datePart[partIdx] = datePart[partIdx] * 10 + b - 48;
                    }
                }
                if (datePart[0] == 0 && datePart[1] == 0 && datePart[2] == 0) {
                    this.lastValueNull |= 0x2;
                    return null;
                }
                return new Date(datePart[0] - 1900, datePart[1] - 1, datePart[2]);
            }
            case TIMESTAMP:
            case TIMESTAMP_NANO:
            case DATETIME:
            case TIMESTAMP_LTZ: {
                final Timestamp timestamp = this.getInternalTimestamp(columnInfo, cal, timeZone);
                if (timestamp == null) {
                    return null;
                }
                return new Date(timestamp.getTime());
            }
            case TIMESTAMP_TZ: {
                final TIMESTAMPTZ timestamptz = this.getInternalTIMESTAMPTZ(columnInfo, cal, timeZone);
                if (timestamptz == null) {
                    return null;
                }
                return timestamptz.dateValue();
            }
            case TIME: {
                throw new SQLException("Cannot read DATE using a Types.TIME field");
            }
            case YEAR: {
                int year = 0;
                for (int begin2 = this.pos; begin2 < this.pos + this.length; ++begin2) {
                    year = year * 10 + this.buf[begin2] - 48;
                }
                if (this.length == 2 && columnInfo.getLength() == 2L) {
                    if (year <= 69) {
                        year += 2000;
                    }
                    else {
                        year += 1900;
                    }
                }
                return new Date(year - 1900, 0, 1);
            }
            default: {
                try {
                    final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    sdf.setTimeZone(timeZone);
                    final java.util.Date utilDate = sdf.parse(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                    return new Date(utilDate.getTime());
                }
                catch (ParseException e) {
                    throw ExceptionFactory.INSTANCE.create("Could not get object as Date : " + e.getMessage(), "S1009", e);
                }
                break;
            }
        }
    }
    
    @Override
    public Time getInternalTime(final ColumnDefinition columnInfo, final Calendar cal, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (columnInfo.getColumnType() == ColumnType.TIMESTAMP || columnInfo.getColumnType() == ColumnType.TIMESTAMP_NANO || columnInfo.getColumnType() == ColumnType.DATETIME || columnInfo.getColumnType() == ColumnType.TIMESTAMP_TZ || columnInfo.getColumnType() == ColumnType.TIMESTAMP_LTZ) {
            final Timestamp timestamp = this.getInternalTimestamp(columnInfo, cal, timeZone);
            return (timestamp == null) ? null : new Time(timestamp.getTime());
        }
        if (columnInfo.getColumnType() == ColumnType.DATE) {
            throw new SQLException("Cannot read Time using a Types.DATE field");
        }
        String raw = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
        if (!this.options.useLegacyDatetimeCode && (raw.startsWith("-") || raw.split(":").length != 3 || raw.indexOf(":") > 3)) {
            throw new SQLException("Time format \"" + raw + "\" incorrect, must be HH:mm:ss");
        }
        final boolean negate = raw.startsWith("-");
        if (negate) {
            raw = raw.substring(1);
        }
        final String[] rawPart = raw.split(":");
        if (rawPart.length == 3) {
            final int hour = Integer.parseInt(rawPart[0]);
            final int minutes = Integer.parseInt(rawPart[1]);
            final int seconds = Integer.parseInt(rawPart[2].substring(0, 2));
            final Calendar calendar = (cal != null) ? cal : Calendar.getInstance();
            if (this.options.useLegacyDatetimeCode) {
                calendar.setLenient(true);
            }
            calendar.clear();
            calendar.set(1970, 0, 1, (negate ? -1 : 1) * hour, minutes, seconds);
            final int nanoseconds = this.extractNanos(raw);
            calendar.set(14, nanoseconds / 1000000);
            return new Time(calendar.getTimeInMillis());
        }
        throw new SQLException(raw + " cannot be parse as time. time must have \"99:99:99\" format");
    }
    
    @Override
    public Timestamp getInternalTimestamp(final ColumnDefinition columnInfo, final Calendar userCalendar, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (this.getProtocol().isOracleMode()) {
            Calendar cal;
            if (userCalendar != null) {
                cal = userCalendar;
            }
            else {
                cal = Calendar.getInstance(timeZone);
            }
            TIMESTAMP timestamp = null;
            switch (columnInfo.getColumnType()) {
                case TIMESTAMP_TZ: {
                    final TIMESTAMPTZ oracleTimestampZ = this.getInternalTIMESTAMPTZ(columnInfo, userCalendar, timeZone);
                    timestamp = TIMESTAMPTZ.toTIMESTAMP(this.getProtocol(), oracleTimestampZ.toBytes());
                    return timestamp.timestampValue(cal);
                }
                case TIMESTAMP_LTZ: {
                    final TIMESTAMPLTZ oracleTimestampLTZ = this.getInternalTIMESTAMPLTZ(columnInfo, userCalendar, timeZone);
                    timestamp = TIMESTAMPLTZ.toTIMESTAMP(this.getProtocol(), oracleTimestampLTZ.getBytes());
                    return timestamp.timestampValue(cal);
                }
            }
        }
        switch (columnInfo.getColumnType()) {
            case DATE:
            case TIMESTAMP:
            case DATETIME:
            case VARSTRING:
            case VARCHAR:
            case VARCHAR2:
            case STRING: {
                int nanoBegin = -1;
                final int[] timestampsPart = { 0, 0, 0, 0, 0, 0, 0 };
                int partIdx = 0;
                for (int begin = this.pos; begin < this.pos + this.length; ++begin) {
                    final byte b = this.buf[begin];
                    if (b == 45 || b == 32 || b == 58) {
                        ++partIdx;
                    }
                    else if (b == 46) {
                        ++partIdx;
                        nanoBegin = begin;
                    }
                    else {
                        if (b < 48 || b > 57) {
                            throw new SQLException("cannot parse data in timestamp string '" + new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())) + "'");
                        }
                        timestampsPart[partIdx] = timestampsPart[partIdx] * 10 + b - 48;
                    }
                }
                if (timestampsPart[0] == 0 && timestampsPart[1] == 0 && timestampsPart[2] == 0 && timestampsPart[3] == 0 && timestampsPart[4] == 0 && timestampsPart[5] == 0 && timestampsPart[6] == 0) {
                    this.lastValueNull |= 0x2;
                    return null;
                }
                if (nanoBegin > 0) {
                    for (int begin = 0; begin < 6 - (this.pos + this.length - nanoBegin - 1); ++begin) {
                        timestampsPart[6] *= 10;
                    }
                }
                Calendar calendar;
                if (userCalendar != null) {
                    calendar = userCalendar;
                }
                else if (columnInfo.getColumnType().getSqlType() == 93) {
                    calendar = Calendar.getInstance(TimeZone.getDefault());
                }
                else {
                    calendar = Calendar.getInstance();
                }
                final Timestamp timestamp2;
                synchronized (calendar) {
                    calendar.clear();
                    calendar.set(1, timestampsPart[0]);
                    calendar.set(2, timestampsPart[1] - 1);
                    calendar.set(5, timestampsPart[2]);
                    calendar.set(11, timestampsPart[3]);
                    calendar.set(12, timestampsPart[4]);
                    calendar.set(13, timestampsPart[5]);
                    calendar.set(14, timestampsPart[6] / 1000000);
                    timestamp2 = new Timestamp(calendar.getTime().getTime());
                }
                timestamp2.setNanos(timestampsPart[6] * 1000);
                return timestamp2;
            }
            case TIME: {
                final String rawValue = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                final Timestamp tt = new Timestamp(this.getInternalTime(columnInfo, userCalendar, TimeZone.getDefault()).getTime());
                tt.setNanos(this.extractNanos(rawValue));
                return tt;
            }
            case TIMESTAMP_NANO: {
                Calendar cal2;
                if (userCalendar != null) {
                    cal2 = userCalendar;
                }
                else {
                    cal2 = Calendar.getInstance(TimeZone.getDefault());
                }
                return this.getInternalTIMESTAMP(columnInfo, userCalendar, TimeZone.getDefault()).timestampValue(cal2);
            }
            default: {
                final String value = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                throw new SQLException("Value type \"" + columnInfo.getColumnType().getTypeName() + "\" with value \"" + value + "\" cannot be parse as Timestamp");
            }
        }
    }
    
    @Override
    public Array getInternalArray(final ColumnDefinition columnInfo, final ComplexDataType complexDataType) throws SQLException {
        return null;
    }
    
    @Override
    public Struct getInternalStruct(final ColumnDefinition columnInfo, final ComplexDataType complexDataType) throws SQLException {
        return null;
    }
    
    @Override
    public ComplexData getInternalComplexCursor(final ColumnDefinition columnInfo, final ComplexDataType complexDataType) throws SQLException {
        return null;
    }
    
    @Override
    public Object getInternalObject(final ColumnDefinition columnInfo, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        switch (columnInfo.getColumnType()) {
            case BIT: {
                if (columnInfo.getLength() == 1L) {
                    return this.buf[this.pos] != 0;
                }
                final byte[] dataBit = new byte[this.length];
                System.arraycopy(this.buf, this.pos, dataBit, 0, this.length);
                return dataBit;
            }
            case TINYINT: {
                if (this.options.tinyInt1isBit && columnInfo.getLength() == 1L) {
                    return this.buf[this.pos] != 48;
                }
                return this.getInternalInt(columnInfo);
            }
            case INTEGER: {
                if (!columnInfo.isSigned()) {
                    return this.getInternalLong(columnInfo);
                }
                return this.getInternalInt(columnInfo);
            }
            case BIGINT: {
                if (!columnInfo.isSigned()) {
                    return this.getInternalBigInteger(columnInfo);
                }
                return this.getInternalLong(columnInfo);
            }
            case DOUBLE: {
                return this.getInternalDouble(columnInfo);
            }
            case VARSTRING:
            case VARCHAR:
            case VARCHAR2:
            case STRING: {
                if (columnInfo.isBinary()) {
                    final byte[] data = new byte[this.getLengthMaxFieldSize()];
                    System.arraycopy(this.buf, this.pos, data, 0, this.getLengthMaxFieldSize());
                    return data;
                }
                return this.getInternalString(columnInfo, null, timeZone);
            }
            case TIMESTAMP:
            case TIMESTAMP_NANO:
            case DATETIME: {
                return this.getInternalTimestamp(columnInfo, null, timeZone);
            }
            case DATE: {
                return this.getInternalDate(columnInfo, null, timeZone);
            }
            case DECIMAL:
            case OBDECIMAL: {
                return this.getInternalBigDecimal(columnInfo);
            }
            case BLOB:
            case LONGBLOB:
            case MEDIUMBLOB:
            case TINYBLOB: {
                final byte[] dataBlob = new byte[this.getLengthMaxFieldSize()];
                System.arraycopy(this.buf, this.pos, dataBlob, 0, this.getLengthMaxFieldSize());
                return dataBlob;
            }
            case NULL: {
                return null;
            }
            case YEAR: {
                if (this.options.yearIsDateType) {
                    return this.getInternalDate(columnInfo, null, timeZone);
                }
                return this.getInternalShort(columnInfo);
            }
            case SMALLINT:
            case MEDIUMINT: {
                return this.getInternalInt(columnInfo);
            }
            case FLOAT: {
                return this.getInternalFloat(columnInfo);
            }
            case TIME: {
                return this.getInternalTime(columnInfo, null, timeZone);
            }
            case OLDDECIMAL:
            case JSON: {
                return this.getInternalString(columnInfo, null, timeZone);
            }
            case GEOMETRY: {
                final byte[] data2 = new byte[this.length];
                System.arraycopy(this.buf, this.pos, data2, 0, this.length);
                return data2;
            }
            case ENUM: {}
            case NEWDATE: {}
            case NUMBER_FLOAT:
            case NUMBER: {
                return this.getInternalNumber(columnInfo).bigDecimalValue();
            }
            case BINARY_FLOAT: {
                final Float f = Float.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                return f;
            }
            case BINARY_DOUBLE: {
                final Double d = Double.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                return d;
            }
            case NVARCHAR2:
            case NCHAR: {
                return this.getInternalString(columnInfo, null, timeZone);
            }
            case TIMESTAMP_TZ: {
                return this.getInternalTIMESTAMPTZ(columnInfo, null, timeZone);
            }
            case TIMESTAMP_LTZ: {
                return this.getInternalTIMESTAMPLTZ(columnInfo, null, timeZone);
            }
            case INTERVALYM: {
                return this.getInternalINTERVALYM(columnInfo);
            }
            case INTERVALDS: {
                return this.getInternalINTERVALDS(columnInfo);
            }
            case RAW: {
                final byte[] returnBytes = new byte[this.length];
                System.arraycopy(this.buf, this.pos, returnBytes, 0, this.length);
                return returnBytes;
            }
        }
        throw ExceptionFactory.INSTANCE.notSupported("Type '" + columnInfo.getColumnType().getTypeName() + "' is not supported");
    }
    
    @Override
    public boolean getInternalBoolean(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return false;
        }
        if (columnInfo.getColumnType() == ColumnType.BIT) {
            return this.parseBit() != 0L;
        }
        long boolVal = 0L;
        switch (columnInfo.getColumnType()) {
            case DOUBLE:
            case FLOAT:
            case YEAR:
            case DECIMAL:
            case OLDDECIMAL:
            case TINYINT:
            case SMALLINT:
            case INTEGER:
            case MEDIUMINT:
            case BIGINT: {
                boolVal = this.getInternalLong(columnInfo);
                return boolVal > 0L || boolVal == -1L;
            }
            default: {
                final String rawVal = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                return Utils.convertStringToBoolean(rawVal);
            }
        }
    }
    
    @Override
    public byte getInternalByte(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return 0;
        }
        final long value = this.getInternalLong(columnInfo);
        this.rangeCheck(Byte.class, -128L, 127L, value, columnInfo);
        return (byte)value;
    }
    
    @Override
    public short getInternalShort(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return 0;
        }
        final long value = this.getInternalLong(columnInfo);
        this.rangeCheck(Short.class, -32768L, 32767L, value, columnInfo);
        return (short)value;
    }
    
    @Override
    public String getInternalTimeString(final ColumnDefinition columnInfo) {
        if (this.lastValueWasNull()) {
            return null;
        }
        final String rawValue = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
        if ("0000-00-00".equals(rawValue)) {
            return null;
        }
        if (this.options.maximizeMysqlCompatibility && this.options.useLegacyDatetimeCode && rawValue.indexOf(".") > 0) {
            return rawValue.substring(0, rawValue.indexOf("."));
        }
        return rawValue;
    }
    
    @Override
    public BigInteger getInternalBigInteger(final ColumnDefinition columnInfo) {
        if (this.lastValueWasNull()) {
            return null;
        }
        return new BigInteger(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())).trim());
    }
    
    @Override
    public ZonedDateTime getInternalZonedDateTime(final ColumnDefinition columnInfo, final Class clazz, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (this.length == 0) {
            this.lastValueNull |= 0x1;
            return null;
        }
        if (!this.getProtocol().isOracleMode() || (columnInfo.getColumnType() != ColumnType.TIMESTAMP_NANO && columnInfo.getColumnType() != ColumnType.TIMESTAMP_TZ && columnInfo.getColumnType() != ColumnType.TIMESTAMP_LTZ)) {
            final String raw = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
            switch (columnInfo.getColumnType().getSqlType()) {
                case 91:
                case 93: {
                    final Calendar cal = Calendar.getInstance(timeZone);
                    final Timestamp timestamp = this.getInternalTimestamp(columnInfo, cal, TimeZone.getDefault());
                    if (timestamp == null) {
                        return null;
                    }
                    try {
                        final LocalDateTime localDateTime = LocalDateTime.parse(timestamp.toString(), TextRowProtocol.TEXT_LOCAL_DATE_TIME.withZone(timeZone.toZoneId()));
                        return ZonedDateTime.of(localDateTime, timeZone.toZoneId());
                    }
                    catch (DateTimeParseException dateParserEx) {
                        throw new SQLException(timestamp.toString() + " cannot be parse as LocalDateTime. time must have \"yyyy-MM-dd HH:mm:ss[.S]\" format");
                    }
                }
                case -1:
                case 1:
                case 12: {
                    if (raw.startsWith("0000-00-00 00:00:00")) {
                        return null;
                    }
                    try {
                        LocalDateTime localDateTime;
                        if (this.getProtocol().isOracleMode()) {
                            localDateTime = LocalDateTime.parse(raw, TextRowProtocol.TEXT_LOCAL_DATE_TIME.withZone(timeZone.toZoneId()));
                        }
                        else {
                            final Calendar cal = Calendar.getInstance(timeZone);
                            final Timestamp timestamp = this.getInternalTimestamp(columnInfo, cal, TimeZone.getDefault());
                            if (timestamp == null) {
                                return null;
                            }
                            try {
                                localDateTime = LocalDateTime.parse(timestamp.toString(), TextRowProtocol.TEXT_LOCAL_DATE_TIME.withZone(timeZone.toZoneId()));
                            }
                            catch (DateTimeParseException dateParserEx) {
                                throw new SQLException(timestamp.toString() + " cannot be parse as LocalDateTime. time must have \"yyyy-MM-dd HH:mm:ss[.S]\" format");
                            }
                        }
                        return ZonedDateTime.of(localDateTime, timeZone.toZoneId());
                    }
                    catch (DateTimeParseException dateParserEx) {
                        throw new SQLException(raw + " cannot be parse as ZonedDateTime. time must have \"yyyy-MM-dd[T/ ]HH:mm:ss[.S]\" " + "with offset and timezone format (example : '2011-12-03 10:15:30+01:00[Europe/Paris]')");
                    }
                    break;
                }
            }
            throw new SQLException("Cannot read " + clazz.getName() + " using a " + columnInfo.getColumnType().getJavaTypeName() + " field");
        }
        final Timestamp oracleTimestamp = this.getInternalTimestamp(columnInfo, null, timeZone);
        if (oracleTimestamp == null) {
            return null;
        }
        final LocalDateTime localDateTimeNoTimeZone = oracleTimestamp.toLocalDateTime();
        return localDateTimeNoTimeZone.atZone(timeZone.toZoneId());
    }
    
    @Override
    public OffsetTime getInternalOffsetTime(final ColumnDefinition columnInfo, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (this.length == 0) {
            this.lastValueNull |= 0x1;
            return null;
        }
        final ZoneId zoneId = timeZone.toZoneId().normalized();
        if (zoneId instanceof ZoneOffset) {
            final ZoneOffset zoneOffset = (ZoneOffset)zoneId;
            final String raw = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
            switch (columnInfo.getColumnType().getSqlType()) {
                case 93: {
                    if (raw.startsWith("0000-00-00 00:00:00")) {
                        return null;
                    }
                    try {
                        return ZonedDateTime.parse(raw, TextRowProtocol.TEXT_LOCAL_DATE_TIME.withZone(zoneOffset)).toOffsetDateTime().toOffsetTime();
                    }
                    catch (DateTimeParseException dateParserEx) {
                        throw new SQLException(raw + " cannot be parse as OffsetTime. time must have \"yyyy-MM-dd HH:mm:ss[.S]\" format");
                    }
                }
                case 92: {
                    try {
                        final LocalTime localTime = LocalTime.parse(raw, DateTimeFormatter.ISO_LOCAL_TIME.withZone(zoneOffset));
                        return OffsetTime.of(localTime, zoneOffset);
                    }
                    catch (DateTimeParseException dateParserEx) {
                        throw new SQLException(raw + " cannot be parse as OffsetTime (format is \"HH:mm:ss[.S]\" for data type \"" + columnInfo.getColumnType() + "\")");
                    }
                }
                case -1:
                case 1:
                case 12: {
                    try {
                        return OffsetTime.parse(raw, DateTimeFormatter.ISO_OFFSET_TIME);
                    }
                    catch (DateTimeParseException dateParserEx) {
                        throw new SQLException(raw + " cannot be parse as OffsetTime (format is \"HH:mm:ss[.S]\" with offset for data type \"" + columnInfo.getColumnType() + "\")");
                    }
                    break;
                }
            }
            throw new SQLException("Cannot read " + OffsetTime.class.getName() + " using a " + columnInfo.getColumnType().getJavaTypeName() + " field");
        }
        if (this.options.useLegacyDatetimeCode) {
            throw new SQLException("Cannot return an OffsetTime for a TIME field when default timezone is '" + zoneId + "' (only possible for time-zone offset from Greenwich/UTC, such as +02:00)");
        }
        throw new SQLException("Cannot return an OffsetTime for a TIME field when server timezone '" + zoneId + "' (only possible for time-zone offset from Greenwich/UTC, such as +02:00)");
    }
    
    @Override
    public LocalTime getInternalLocalTime(final ColumnDefinition columnInfo, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (this.length == 0) {
            this.lastValueNull |= 0x1;
            return null;
        }
        final String raw = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
        switch (columnInfo.getColumnType().getSqlType()) {
            case -1:
            case 1:
            case 12:
            case 92: {
                try {
                    return LocalTime.parse(raw, DateTimeFormatter.ISO_LOCAL_TIME.withZone(timeZone.toZoneId()));
                }
                catch (DateTimeParseException dateParserEx) {
                    throw new SQLException(raw + " cannot be parse as LocalTime (format is \"HH:mm:ss[.S]\" for data type \"" + columnInfo.getColumnType() + "\")");
                }
            }
            case 93: {
                final ZonedDateTime zonedDateTime = this.getInternalZonedDateTime(columnInfo, LocalTime.class, timeZone);
                return (zonedDateTime == null) ? null : zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalTime();
            }
            default: {
                throw new SQLException("Cannot read LocalTime using a " + columnInfo.getColumnType().getJavaTypeName() + " field");
            }
        }
    }
    
    @Override
    public LocalDate getInternalLocalDate(final ColumnDefinition columnInfo, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (this.length == 0) {
            this.lastValueNull |= 0x1;
            return null;
        }
        final String raw = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
        switch (columnInfo.getColumnType().getSqlType()) {
            case -1:
            case 1:
            case 12:
            case 91: {
                if (raw.startsWith("0000-00-00")) {
                    return null;
                }
                try {
                    return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE.withZone(timeZone.toZoneId()));
                }
                catch (DateTimeParseException dateParserEx) {
                    throw new SQLException(raw + " cannot be parse as LocalDate (format is \"yyyy-MM-dd\" for data type \"" + columnInfo.getColumnType() + "\")");
                }
            }
            case 93: {
                final ZonedDateTime zonedDateTime = this.getInternalZonedDateTime(columnInfo, LocalDate.class, timeZone);
                return (zonedDateTime == null) ? null : zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
            }
            default: {
                throw new SQLException("Cannot read LocalDate using a " + columnInfo.getColumnType().getJavaTypeName() + " field");
            }
        }
    }
    
    @Override
    public boolean isBinaryEncoded() {
        return false;
    }
    
    public NUMBER getInternalNumber(final ColumnDefinition columnInfo) {
        if (this.lastValueWasNull()) {
            return new NUMBER("0".getBytes());
        }
        final byte[] b = new byte[this.length];
        System.arraycopy(this.buf, this.pos, b, 0, this.length);
        return new NUMBER(b);
    }
    
    public NUMBER_FLOAT getInternalNumber_float(final ColumnDefinition columnInfo) {
        if (this.lastValueWasNull()) {
            return new NUMBER_FLOAT(0.0f);
        }
        return new NUMBER_FLOAT(new Float(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()))));
    }
    
    public BINARY_DOUBLE getInternalBINARY_DOUBLE() {
        if (this.lastValueWasNull()) {
            return new BINARY_DOUBLE(0.0);
        }
        final byte[] b = new byte[this.length];
        System.arraycopy(this.buf, this.pos, b, 0, this.length);
        return new BINARY_DOUBLE(b);
    }
    
    public BINARY_FLOAT getInternalBINARY_FLOAT() {
        if (this.lastValueWasNull()) {
            return new BINARY_FLOAT(0.0f);
        }
        final byte[] b = new byte[this.length];
        System.arraycopy(this.buf, this.pos, b, 0, this.length);
        return new BINARY_FLOAT(this.buf);
    }
    
    @Override
    public INTERVALDS getInternalINTERVALDS(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return new INTERVALDS("0 0:0:0.0");
        }
        final byte[] b = new byte[this.length];
        System.arraycopy(this.buf, this.pos, b, 0, this.length);
        return new INTERVALDS(b);
    }
    
    @Override
    public INTERVALYM getInternalINTERVALYM(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return new INTERVALYM("0-0");
        }
        final byte[] b = new byte[this.length];
        System.arraycopy(this.buf, this.pos, b, 0, this.length);
        return new INTERVALYM(b);
    }
}
