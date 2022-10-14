// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.resultset.rowprotocol;

import com.oceanbase.jdbc.extend.datatype.RowObCursorData;
import com.oceanbase.jdbc.ObStruct;
import java.sql.Struct;
import com.oceanbase.jdbc.ObArray;
import java.sql.Array;
import java.nio.charset.StandardCharsets;
import com.oceanbase.jdbc.extend.datatype.StructImpl;
import com.oceanbase.jdbc.extend.datatype.ArrayImpl;
import com.oceanbase.jdbc.extend.datatype.ComplexData;
import com.oceanbase.jdbc.extend.datatype.ComplexDataType;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.OffsetTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.ZonedDateTime;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.extend.datatype.NUMBER;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMP;
import com.oceanbase.jdbc.internal.ColumnType;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMPLTZ;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMPTZ;
import java.sql.Time;
import java.math.BigInteger;
import java.sql.SQLException;
import java.math.BigDecimal;
import com.oceanbase.jdbc.extend.datatype.INTERVALYM;
import com.oceanbase.jdbc.extend.datatype.INTERVALDS;
import java.sql.Timestamp;
import java.sql.Date;
import com.oceanbase.jdbc.OceanBaseConnection;
import com.oceanbase.jdbc.Clob;
import com.oceanbase.jdbc.internal.util.Utils;
import java.nio.charset.Charset;
import java.util.TimeZone;
import java.util.Calendar;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;

public class BinaryRowProtocol extends RowProtocol
{
    private final ColumnDefinition[] columnDefinition;
    private final int columnInformationLength;
    
    public BinaryRowProtocol(final ColumnDefinition[] columnDefinition, final int columnInformationLength, final int maxFieldSize, final Options options) {
        super(maxFieldSize, options);
        this.columnDefinition = columnDefinition;
        this.columnInformationLength = columnInformationLength;
    }
    
    @Override
    public void setPosition(final int newIndex) {
        if ((this.buf[1 + (newIndex + 2) / 8] & 1 << (newIndex + 2) % 8) != 0x0) {
            this.lastValueNull = 1;
            return;
        }
        boolean readFromHead = false;
        for (int i = 0; i < this.columnDefinition.length; ++i) {
            switch (this.columnDefinition[i].getColumnType()) {
                case COMPLEX:
                case CURSOR: {
                    readFromHead = true;
                    break;
                }
            }
        }
        int internalPos = 0;
        boolean doRead = true;
        if (readFromHead) {
            this.index = 0;
            internalPos = 1 + (this.columnInformationLength + 9) / 8;
        }
        else if (this.index != newIndex) {
            internalPos = this.pos;
            if (this.index == -1 || this.index > newIndex) {
                this.index = 0;
                internalPos = 1 + (this.columnInformationLength + 9) / 8;
            }
            else {
                ++this.index;
                internalPos += this.length;
            }
            doRead = true;
        }
        else {
            doRead = false;
        }
        if (doRead) {
            while (this.index <= newIndex) {
                if ((this.buf[1 + (this.index + 2) / 8] & 1 << (this.index + 2) % 8) == 0x0) {
                    if (this.index != newIndex) {
                        Label_0674: {
                            switch (this.columnDefinition[this.index].getColumnType()) {
                                case BIGINT:
                                case DOUBLE: {
                                    internalPos += 8;
                                    break;
                                }
                                case INTEGER:
                                case MEDIUMINT:
                                case FLOAT:
                                case NUMBER: {
                                    internalPos += 4;
                                    break;
                                }
                                case SMALLINT:
                                case YEAR: {
                                    internalPos += 2;
                                    break;
                                }
                                case TINYINT: {
                                    ++internalPos;
                                    break;
                                }
                                case COMPLEX: {
                                    internalPos = this.complexEndPos[this.index];
                                    break;
                                }
                                case CURSOR: {
                                    internalPos = this.complexEndPos[this.index];
                                    break;
                                }
                                default: {
                                    final int type = this.buf[internalPos++] & 0xFF;
                                    switch (type) {
                                        case 251: {
                                            break Label_0674;
                                        }
                                        case 252: {
                                            internalPos += 2 + (0xFFFF & (this.buf[internalPos] & 0xFF) + ((this.buf[internalPos + 1] & 0xFF) << 8));
                                            break Label_0674;
                                        }
                                        case 253: {
                                            internalPos += 3 + (0xFFFFFF & (this.buf[internalPos] & 0xFF) + ((this.buf[internalPos + 1] & 0xFF) << 8) + ((this.buf[internalPos + 2] & 0xFF) << 16));
                                            break Label_0674;
                                        }
                                        case 254: {
                                            internalPos += (int)(8L + ((this.buf[internalPos] & 0xFF) + ((long)(this.buf[internalPos + 1] & 0xFF) << 8) + ((long)(this.buf[internalPos + 2] & 0xFF) << 16) + ((long)(this.buf[internalPos + 3] & 0xFF) << 24) + ((long)(this.buf[internalPos + 4] & 0xFF) << 32) + ((long)(this.buf[internalPos + 5] & 0xFF) << 40) + ((long)(this.buf[internalPos + 6] & 0xFF) << 48) + ((long)(this.buf[internalPos + 7] & 0xFF) << 56)));
                                            break Label_0674;
                                        }
                                        default: {
                                            internalPos += type;
                                            break Label_0674;
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    else {
                        switch (this.columnDefinition[this.index].getColumnType()) {
                            case BIGINT:
                            case DOUBLE: {
                                this.pos = internalPos;
                                this.length = 8;
                                this.lastValueNull = 0;
                                return;
                            }
                            case INTEGER:
                            case MEDIUMINT:
                            case FLOAT:
                            case NUMBER: {
                                this.pos = internalPos;
                                this.length = 4;
                                this.lastValueNull = 0;
                                return;
                            }
                            case SMALLINT:
                            case YEAR: {
                                this.pos = internalPos;
                                this.length = 2;
                                this.lastValueNull = 0;
                                return;
                            }
                            case TINYINT: {
                                this.pos = internalPos;
                                this.length = 1;
                                this.lastValueNull = 0;
                                return;
                            }
                            case COMPLEX: {
                                this.pos = internalPos;
                                this.lastValueNull = 0;
                                return;
                            }
                            case CURSOR: {
                                this.pos = internalPos;
                                this.lastValueNull = 0;
                                return;
                            }
                            case BINARY_FLOAT: {
                                this.pos = internalPos;
                                this.length = 4;
                                this.lastValueNull = 0;
                                return;
                            }
                            case BINARY_DOUBLE: {
                                this.pos = internalPos;
                                this.length = 8;
                                this.lastValueNull = 0;
                                return;
                            }
                            default: {
                                final int typeOrLength = this.buf[internalPos++] & 0xFF;
                                switch (typeOrLength) {
                                    case 251: {
                                        throw new IllegalStateException("null data is encoded in binary protocol but NULL-Bitmap is not set");
                                    }
                                    case 252: {
                                        this.length = (0xFFFF & (this.buf[internalPos++] & 0xFF) + ((this.buf[internalPos++] & 0xFF) << 8));
                                        this.pos = internalPos;
                                        this.lastValueNull = 0;
                                        return;
                                    }
                                    case 253: {
                                        this.length = (0xFFFFFF & (this.buf[internalPos++] & 0xFF) + ((this.buf[internalPos++] & 0xFF) << 8) + ((this.buf[internalPos++] & 0xFF) << 16));
                                        this.pos = internalPos;
                                        this.lastValueNull = 0;
                                        return;
                                    }
                                    case 254: {
                                        this.length = (int)((this.buf[internalPos++] & 0xFF) + ((long)(this.buf[internalPos++] & 0xFF) << 8) + ((long)(this.buf[internalPos++] & 0xFF) << 16) + ((long)(this.buf[internalPos++] & 0xFF) << 24) + ((long)(this.buf[internalPos++] & 0xFF) << 32) + ((long)(this.buf[internalPos++] & 0xFF) << 40) + ((long)(this.buf[internalPos++] & 0xFF) << 48) + ((long)(this.buf[internalPos++] & 0xFF) << 56));
                                        this.pos = internalPos;
                                        this.lastValueNull = 0;
                                        return;
                                    }
                                    default: {
                                        this.length = typeOrLength;
                                        this.pos = internalPos;
                                        this.lastValueNull = 0;
                                        return;
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                ++this.index;
            }
        }
        this.lastValueNull = ((this.length == -1) ? 1 : 0);
    }
    
    @Override
    public String getInternalString(final ColumnDefinition columnInfo, final Calendar cal, final TimeZone timeZone) throws SQLException {
        final Charset charset = Charset.forName(this.options.characterEncoding);
        if ((this.lastValueNull & 0x1) != 0x0) {
            switch (columnInfo.getColumnType()) {
                case BINARY_DOUBLE: {
                    return Double.toString(0.0);
                }
                case BINARY_FLOAT: {
                    return Float.toString(0.0f);
                }
                default: {
                    return null;
                }
            }
        }
        else {
            switch (columnInfo.getColumnType()) {
                case STRING: {
                    if (this.getMaxFieldSize() > 0) {
                        return new String(this.buf, this.pos, Math.min(this.getMaxFieldSize() * 3, this.length), this.getCurrentEncoding(columnInfo.getColumnType())).substring(0, Math.min(this.getMaxFieldSize(), this.length));
                    }
                    return new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                }
                case BIT: {
                    return String.valueOf(this.parseBit());
                }
                case NUMBER: {
                    return this.zeroFillingIfNeeded(String.valueOf(this.getInternalInt(columnInfo)), columnInfo);
                }
                case TINYINT: {
                    return this.zeroFillingIfNeeded(String.valueOf(this.getInternalTinyInt(columnInfo)), columnInfo);
                }
                case SMALLINT: {
                    return this.zeroFillingIfNeeded(String.valueOf(this.getInternalSmallInt(columnInfo)), columnInfo);
                }
                case INTEGER:
                case MEDIUMINT: {
                    return this.zeroFillingIfNeeded(String.valueOf(this.getInternalMediumInt(columnInfo)), columnInfo);
                }
                case BIGINT: {
                    if (!columnInfo.isSigned()) {
                        return this.zeroFillingIfNeeded(String.valueOf(this.getInternalBigInteger(columnInfo)), columnInfo);
                    }
                    return this.zeroFillingIfNeeded(String.valueOf(this.getInternalLong(columnInfo)), columnInfo);
                }
                case DOUBLE: {
                    return this.zeroFillingIfNeeded(String.valueOf(this.getInternalDouble(columnInfo)), columnInfo);
                }
                case FLOAT: {
                    return this.zeroFillingIfNeeded(String.valueOf(this.getInternalFloat(columnInfo)), columnInfo);
                }
                case TIME: {
                    return this.getInternalTimeString(columnInfo);
                }
                case DATE: {
                    final Date date = this.getInternalDate(columnInfo, cal, TimeZone.getDefault());
                    if (date == null) {
                        return null;
                    }
                    return date.toString();
                }
                case YEAR: {
                    if (this.options.yearIsDateType) {
                        final Date dateInter = this.getInternalDate(columnInfo, cal, TimeZone.getDefault());
                        return (dateInter == null) ? null : dateInter.toString();
                    }
                    return String.valueOf(this.getInternalSmallInt(columnInfo));
                }
                case TIMESTAMP:
                case TIMESTAMP_NANO:
                case DATETIME: {
                    final Timestamp timestamp = this.getInternalTimestamp(columnInfo, cal, TimeZone.getDefault());
                    if (timestamp == null) {
                        return null;
                    }
                    return timestamp.toString();
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
                case GEOMETRY: {
                    return new String(this.buf, this.pos, this.length);
                }
                case NULL: {
                    return null;
                }
                case RAW: {
                    final byte[] returnBytes = new byte[this.length];
                    System.arraycopy(this.buf, this.pos, returnBytes, 0, this.length);
                    return Utils.toHexString(returnBytes);
                }
                case VARSTRING: {
                    return new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                }
                case BINARY_DOUBLE: {
                    final Double d = this.getInternalDouble(columnInfo);
                    return Double.toString(d);
                }
                case BINARY_FLOAT: {
                    final Float f = this.getInternalFloat(columnInfo);
                    return Float.toString(f);
                }
                case OBCLOB: {
                    if (this.options.supportLobLocator) {
                        final String encoding = this.options.characterEncoding;
                        final byte[] data = new byte[this.buf.length];
                        System.arraycopy(this.buf, this.pos, data, 0, this.length);
                        final Clob c = new Clob(true, data, encoding, null);
                        return c.toString();
                    }
                    return new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                }
                default: {
                    if (this.getMaxFieldSize() > 0) {
                        return new String(this.buf, this.pos, Math.min(this.getMaxFieldSize() * 3, this.length), charset).substring(0, Math.min(this.getMaxFieldSize(), this.length));
                    }
                    return new String(this.buf, this.pos, this.length, charset);
                }
            }
        }
    }
    
    @Override
    public int getInternalInt(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return 0;
        }
        long value = 0L;
        switch (columnInfo.getColumnType()) {
            case BIT: {
                value = this.parseBit();
                break;
            }
            case TINYINT: {
                value = this.getInternalTinyInt(columnInfo);
                break;
            }
            case SMALLINT:
            case YEAR: {
                value = this.getInternalSmallInt(columnInfo);
                break;
            }
            case INTEGER:
            case MEDIUMINT:
            case NUMBER: {
                value = (this.buf[this.pos] & 0xFF) + ((this.buf[this.pos + 1] & 0xFF) << 8) + ((this.buf[this.pos + 2] & 0xFF) << 16) + ((this.buf[this.pos + 3] & 0xFF) << 24);
                if (columnInfo.isSigned()) {
                    return (int)value;
                }
                if (value < 0L) {
                    value &= 0xFFFFFFFFL;
                    break;
                }
                break;
            }
            case BIGINT: {
                value = this.getInternalLong(columnInfo);
                break;
            }
            case FLOAT: {
                value = (long)this.getInternalFloat(columnInfo);
                break;
            }
            case DOUBLE: {
                value = (long)this.getInternalDouble(columnInfo);
                break;
            }
            case DECIMAL:
            case OLDDECIMAL:
            case OBDECIMAL: {
                final BigDecimal bigDecimal = this.getInternalBigDecimal(columnInfo);
                this.rangeCheck(Integer.class, -2147483648L, 2147483647L, bigDecimal, columnInfo);
                return bigDecimal.intValue();
            }
            case STRING:
            case VARSTRING:
            case VARCHAR:
            case VARCHAR2:
            case NVARCHAR2: {
                value = Long.parseLong(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                break;
            }
            default: {
                throw new SQLException("getInt not available for data field type " + columnInfo.getColumnType().getJavaTypeName());
            }
        }
        this.rangeCheck(Integer.class, -2147483648L, 2147483647L, value, columnInfo);
        return (int)value;
    }
    
    @Override
    public long getInternalLong(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return 0L;
        }
        try {
            long value = 0L;
            switch (columnInfo.getColumnType()) {
                case BIT: {
                    return this.parseBit();
                }
                case TINYINT: {
                    value = this.getInternalTinyInt(columnInfo);
                    break;
                }
                case SMALLINT:
                case YEAR: {
                    value = this.getInternalSmallInt(columnInfo);
                    break;
                }
                case INTEGER:
                case MEDIUMINT: {
                    value = this.getInternalMediumInt(columnInfo);
                    break;
                }
                case BIGINT: {
                    value = (this.buf[this.pos] & 0xFF) + ((long)(this.buf[this.pos + 1] & 0xFF) << 8) + ((long)(this.buf[this.pos + 2] & 0xFF) << 16) + ((long)(this.buf[this.pos + 3] & 0xFF) << 24) + ((long)(this.buf[this.pos + 4] & 0xFF) << 32) + ((long)(this.buf[this.pos + 5] & 0xFF) << 40) + ((long)(this.buf[this.pos + 6] & 0xFF) << 48) + ((long)(this.buf[this.pos + 7] & 0xFF) << 56);
                    if (columnInfo.isSigned()) {
                        return value;
                    }
                    final BigInteger unsignedValue = new BigInteger(1, new byte[] { (byte)(value >> 56), (byte)(value >> 48), (byte)(value >> 40), (byte)(value >> 32), (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value });
                    if (unsignedValue.compareTo(new BigInteger(String.valueOf(Long.MAX_VALUE))) > 0) {
                        throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' : value " + unsignedValue + " is not in Long range", "22003", 1264);
                    }
                    return unsignedValue.longValue();
                }
                case FLOAT: {
                    final Float floatValue = this.getInternalFloat(columnInfo);
                    if (floatValue.compareTo(9.223372E18f) >= 1) {
                        throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' : value " + floatValue + " is not in Long range", "22003", 1264);
                    }
                    return floatValue.longValue();
                }
                case DOUBLE: {
                    final Double doubleValue = this.getInternalDouble(columnInfo);
                    if (doubleValue.compareTo(9.223372036854776E18) >= 1) {
                        throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' : value " + doubleValue + " is not in Long range", "22003", 1264);
                    }
                    return doubleValue.longValue();
                }
                case DECIMAL:
                case OLDDECIMAL: {
                    final BigDecimal bigDecimal = this.getInternalBigDecimal(columnInfo);
                    this.rangeCheck(Long.class, Long.MIN_VALUE, Long.MAX_VALUE, bigDecimal, columnInfo);
                    return bigDecimal.longValue();
                }
                case STRING:
                case VARSTRING:
                case OBDECIMAL:
                case VARCHAR:
                case VARCHAR2:
                case NVARCHAR2: {
                    return Long.parseLong(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                }
                default: {
                    throw new SQLException("getLong not available for data field type " + columnInfo.getColumnType().getJavaTypeName());
                }
            }
            this.rangeCheck(Long.class, Long.MIN_VALUE, Long.MAX_VALUE, value, columnInfo);
            return value;
        }
        catch (NumberFormatException nfe) {
            final String valueParams = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
            if (BinaryRowProtocol.isIntegerRegex.matcher(valueParams).find()) {
                try {
                    return Long.parseLong(valueParams.substring(0, valueParams.indexOf(".")));
                }
                catch (NumberFormatException ex) {}
            }
            throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' : value " + valueParams, "22003", 1264);
        }
    }
    
    @Override
    public float getInternalFloat(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return 0.0f;
        }
        long value = 0L;
        switch (columnInfo.getColumnType()) {
            case BIT: {
                return (float)this.parseBit();
            }
            case TINYINT: {
                value = this.getInternalTinyInt(columnInfo);
                break;
            }
            case SMALLINT:
            case YEAR: {
                value = this.getInternalSmallInt(columnInfo);
                break;
            }
            case INTEGER:
            case MEDIUMINT: {
                value = this.getInternalMediumInt(columnInfo);
                break;
            }
            case BIGINT: {
                value = (this.buf[this.pos] & 0xFF) + ((long)(this.buf[this.pos + 1] & 0xFF) << 8) + ((long)(this.buf[this.pos + 2] & 0xFF) << 16) + ((long)(this.buf[this.pos + 3] & 0xFF) << 24) + ((long)(this.buf[this.pos + 4] & 0xFF) << 32) + ((long)(this.buf[this.pos + 5] & 0xFF) << 40) + ((long)(this.buf[this.pos + 6] & 0xFF) << 48) + ((long)(this.buf[this.pos + 7] & 0xFF) << 56);
                if (columnInfo.isSigned()) {
                    return (float)value;
                }
                final BigInteger unsignedValue = new BigInteger(1, new byte[] { (byte)(value >> 56), (byte)(value >> 48), (byte)(value >> 40), (byte)(value >> 32), (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value });
                return unsignedValue.floatValue();
            }
            case FLOAT: {
                final int valueFloat = (this.buf[this.pos] & 0xFF) + ((this.buf[this.pos + 1] & 0xFF) << 8) + ((this.buf[this.pos + 2] & 0xFF) << 16) + ((this.buf[this.pos + 3] & 0xFF) << 24);
                return Float.intBitsToFloat(valueFloat);
            }
            case BINARY_FLOAT: {
                final int asInt = (this.buf[this.pos + 0] & 0xFF) | (this.buf[this.pos + 1] & 0xFF) << 8 | (this.buf[this.pos + 2] & 0xFF) << 16 | (this.buf[this.pos + 3] & 0xFF) << 24;
                return Float.intBitsToFloat(asInt);
            }
            case NUMBER_FLOAT: {
                final String str = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                final Float f = Float.valueOf(str);
                return f;
            }
            case DOUBLE: {
                return (float)this.getInternalDouble(columnInfo);
            }
            case STRING:
            case DECIMAL:
            case VARSTRING:
            case VARCHAR:
            case VARCHAR2:
            case NVARCHAR2: {
                try {
                    return Float.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                }
                catch (NumberFormatException nfe) {
                    final SQLException sqlException = new SQLException("Incorrect format for getFloat for data field with type " + columnInfo.getColumnType().getJavaTypeName(), "22003", 1264, nfe);
                    throw sqlException;
                }
            }
            case OBDECIMAL: {
                final String val = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                final BigDecimal bigDecimal = new BigDecimal(val);
                return bigDecimal.floatValue();
            }
            default: {
                throw new SQLException("getFloat not available for data field type " + columnInfo.getColumnType().getJavaTypeName());
            }
        }
        try {
            return Float.valueOf(String.valueOf(value));
        }
        catch (NumberFormatException nfe2) {
            final SQLException sqlException2 = new SQLException("Incorrect format for getFloat for data field with type " + columnInfo.getColumnType().getJavaTypeName(), "22003", 1264, nfe2);
            throw sqlException2;
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
            case TINYINT: {
                return this.getInternalTinyInt(columnInfo);
            }
            case SMALLINT:
            case YEAR: {
                return this.getInternalSmallInt(columnInfo);
            }
            case INTEGER:
            case MEDIUMINT: {
                return (double)this.getInternalMediumInt(columnInfo);
            }
            case BIGINT: {
                final long valueLong = (this.buf[this.pos] & 0xFF) + ((long)(this.buf[this.pos + 1] & 0xFF) << 8) + ((long)(this.buf[this.pos + 2] & 0xFF) << 16) + ((long)(this.buf[this.pos + 3] & 0xFF) << 24) + ((long)(this.buf[this.pos + 4] & 0xFF) << 32) + ((long)(this.buf[this.pos + 5] & 0xFF) << 40) + ((long)(this.buf[this.pos + 6] & 0xFF) << 48) + ((long)(this.buf[this.pos + 7] & 0xFF) << 56);
                if (columnInfo.isSigned()) {
                    return (double)valueLong;
                }
                return new BigInteger(1, new byte[] { (byte)(valueLong >> 56), (byte)(valueLong >> 48), (byte)(valueLong >> 40), (byte)(valueLong >> 32), (byte)(valueLong >> 24), (byte)(valueLong >> 16), (byte)(valueLong >> 8), (byte)valueLong }).doubleValue();
            }
            case FLOAT: {
                return this.getInternalFloat(columnInfo);
            }
            case DOUBLE: {
                final long valueDouble = (this.buf[this.pos] & 0xFF) + ((long)(this.buf[this.pos + 1] & 0xFF) << 8) + ((long)(this.buf[this.pos + 2] & 0xFF) << 16) + ((long)(this.buf[this.pos + 3] & 0xFF) << 24) + ((long)(this.buf[this.pos + 4] & 0xFF) << 32) + ((long)(this.buf[this.pos + 5] & 0xFF) << 40) + ((long)(this.buf[this.pos + 6] & 0xFF) << 48) + ((long)(this.buf[this.pos + 7] & 0xFF) << 56);
                return Double.longBitsToDouble(valueDouble);
            }
            case STRING:
            case DECIMAL:
            case VARSTRING:
            case VARCHAR:
            case VARCHAR2:
            case NVARCHAR2: {
                try {
                    return Double.valueOf(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                }
                catch (NumberFormatException nfe) {
                    final SQLException sqlException = new SQLException("Incorrect format for getDouble for data field with type " + columnInfo.getColumnType().getJavaTypeName(), "22003", 1264);
                    sqlException.initCause(nfe);
                    throw sqlException;
                }
            }
            case BINARY_DOUBLE: {
                final long valueAsLong = (long)(this.buf[this.pos + 0] & 0xFF) | (long)(this.buf[this.pos + 1] & 0xFF) << 8 | (long)(this.buf[this.pos + 2] & 0xFF) << 16 | (long)(this.buf[this.pos + 3] & 0xFF) << 24 | (long)(this.buf[this.pos + 4] & 0xFF) << 32 | (long)(this.buf[this.pos + 5] & 0xFF) << 40 | (long)(this.buf[this.pos + 6] & 0xFF) << 48 | (long)(this.buf[this.pos + 7] & 0xFF) << 56;
                return Double.longBitsToDouble(valueAsLong);
            }
            case OBDECIMAL: {
                final String val = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                final BigDecimal bigDecimal = new BigDecimal(val);
                return bigDecimal.doubleValue();
            }
            default: {
                throw new SQLException("getDouble not available for data field type " + columnInfo.getColumnType().getJavaTypeName());
            }
        }
    }
    
    @Override
    public BigDecimal getInternalBigDecimal(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        switch (columnInfo.getColumnType()) {
            case BIT: {
                return BigDecimal.valueOf(this.parseBit());
            }
            case TINYINT: {
                return BigDecimal.valueOf(this.getInternalTinyInt(columnInfo));
            }
            case SMALLINT:
            case YEAR: {
                return BigDecimal.valueOf(this.getInternalSmallInt(columnInfo));
            }
            case INTEGER:
            case MEDIUMINT: {
                return BigDecimal.valueOf(this.getInternalMediumInt(columnInfo));
            }
            case BIGINT: {
                final long value = (this.buf[this.pos] & 0xFF) + ((long)(this.buf[this.pos + 1] & 0xFF) << 8) + ((long)(this.buf[this.pos + 2] & 0xFF) << 16) + ((long)(this.buf[this.pos + 3] & 0xFF) << 24) + ((long)(this.buf[this.pos + 4] & 0xFF) << 32) + ((long)(this.buf[this.pos + 5] & 0xFF) << 40) + ((long)(this.buf[this.pos + 6] & 0xFF) << 48) + ((long)(this.buf[this.pos + 7] & 0xFF) << 56);
                if (columnInfo.isSigned()) {
                    return new BigDecimal(String.valueOf(BigInteger.valueOf(value))).setScale(columnInfo.getDecimals());
                }
                return new BigDecimal(String.valueOf(new BigInteger(1, new byte[] { (byte)(value >> 56), (byte)(value >> 48), (byte)(value >> 40), (byte)(value >> 32), (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value }))).setScale(columnInfo.getDecimals());
            }
            case FLOAT:
            case BINARY_FLOAT: {
                return BigDecimal.valueOf(this.getInternalFloat(columnInfo));
            }
            case DOUBLE:
            case BINARY_DOUBLE: {
                return BigDecimal.valueOf(this.getInternalDouble(columnInfo));
            }
            case STRING:
            case DECIMAL:
            case OLDDECIMAL:
            case VARSTRING:
            case OBDECIMAL:
            case VARCHAR:
            case VARCHAR2:
            case NVARCHAR2: {
                return new BigDecimal(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())).trim());
            }
            default: {
                throw new SQLException("getBigDecimal not available for data field type " + columnInfo.getColumnType().getJavaTypeName());
            }
        }
    }
    
    @Override
    public Date getInternalDate(final ColumnDefinition columnInfo, final Calendar cal, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        switch (columnInfo.getColumnType()) {
            case TIMESTAMP:
            case TIMESTAMP_NANO:
            case DATETIME:
            case TIMESTAMP_TZ:
            case TIMESTAMP_LTZ: {
                final Timestamp timestamp = this.getInternalTimestamp(columnInfo, cal, timeZone);
                return (timestamp == null) ? null : new Date(timestamp.getTime());
            }
            case TIME: {
                throw new SQLException("Cannot read Date using a Types.TIME field");
            }
            case STRING: {
                final String rawValue = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                if ("0000-00-00".equals(rawValue)) {
                    this.lastValueNull |= 0x2;
                    return null;
                }
                return new Date(Integer.parseInt(rawValue.substring(0, 4)) - 1900, Integer.parseInt(rawValue.substring(5, 7)) - 1, Integer.parseInt(rawValue.substring(8, 10)));
            }
            default: {
                if (this.length == 0) {
                    this.lastValueNull |= 0x1;
                    return null;
                }
                int year = (this.buf[this.pos] & 0xFF) | (this.buf[this.pos + 1] & 0xFF) << 8;
                if (this.length == 2 && columnInfo.getLength() == 2L) {
                    if (year <= 69) {
                        year += 2000;
                    }
                    else {
                        year += 1900;
                    }
                }
                int month = 1;
                int day = 1;
                if (this.length >= 4) {
                    month = this.buf[this.pos + 2];
                    day = this.buf[this.pos + 3];
                }
                final Calendar calendar = Calendar.getInstance();
                calendar.clear();
                calendar.set(1, year);
                calendar.set(2, month - 1);
                calendar.set(5, day);
                calendar.set(11, 0);
                calendar.set(12, 0);
                calendar.set(13, 0);
                calendar.set(14, 0);
                final Date dt = new Date(calendar.getTimeInMillis());
                return dt;
            }
        }
    }
    
    @Override
    public Time getInternalTime(final ColumnDefinition columnInfo, final Calendar cal, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        switch (columnInfo.getColumnType()) {
            case TIMESTAMP:
            case TIMESTAMP_NANO:
            case DATETIME:
            case TIMESTAMP_TZ:
            case TIMESTAMP_LTZ: {
                final Timestamp ts = this.getInternalTimestamp(columnInfo, cal, timeZone);
                return (ts == null) ? null : new Time(ts.getTime());
            }
            case DATE: {
                throw new SQLException("Cannot read Time using a Types.DATE field");
            }
            default: {
                final Calendar calendar = (cal != null) ? cal : Calendar.getInstance();
                calendar.clear();
                int day = 0;
                int hour = 0;
                int minutes = 0;
                int seconds = 0;
                boolean negate = false;
                if (this.length > 0) {
                    negate = ((this.buf[this.pos] & 0xFF) == 0x1);
                }
                if (this.length > 4) {
                    day = (this.buf[this.pos + 1] & 0xFF) + ((this.buf[this.pos + 2] & 0xFF) << 8) + ((this.buf[this.pos + 3] & 0xFF) << 16) + ((this.buf[this.pos + 4] & 0xFF) << 24);
                }
                if (this.length > 7) {
                    hour = this.buf[this.pos + 5];
                    minutes = this.buf[this.pos + 6];
                    seconds = this.buf[this.pos + 7];
                }
                calendar.set(1970, 0, (negate ? -1 : 1) * day + 1, (negate ? -1 : 1) * hour, minutes, seconds);
                int nanoseconds = 0;
                if (this.length > 8) {
                    nanoseconds = (this.buf[this.pos + 8] & 0xFF) + ((this.buf[this.pos + 9] & 0xFF) << 8) + ((this.buf[this.pos + 10] & 0xFF) << 16) + ((this.buf[this.pos + 11] & 0xFF) << 24);
                }
                calendar.set(14, nanoseconds / 1000);
                return new Time(calendar.getTimeInMillis());
            }
        }
    }
    
    @Override
    public Timestamp getInternalTimestamp(final ColumnDefinition columnInfo, final Calendar userCalendar, final TimeZone timeZone) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (this.length == 0) {
            this.lastValueNull |= 0x1;
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
                    timestamp = TIMESTAMPTZ.resultTIMESTAMP(this.getProtocol(), oracleTimestampZ.toBytes());
                    return timestamp.timestampValue(cal);
                }
                case TIMESTAMP_LTZ: {
                    final TIMESTAMPLTZ oracleTimestampLTZ = this.getInternalTIMESTAMPLTZ(columnInfo, userCalendar, timeZone);
                    timestamp = TIMESTAMPLTZ.resultTIMESTAMP(this.getProtocol(), oracleTimestampLTZ.getBytes());
                    return timestamp.timestampValue(cal);
                }
            }
        }
        if (columnInfo.getColumnType() == ColumnType.TIMESTAMP_NANO) {
            Calendar cal;
            if (userCalendar != null) {
                cal = userCalendar;
            }
            else {
                cal = Calendar.getInstance(TimeZone.getDefault());
            }
            return this.getInternalTIMESTAMP(columnInfo, userCalendar, TimeZone.getDefault()).timestampValue(cal);
        }
        int year = 1970;
        int month = 0;
        int day = 0;
        int hour = 0;
        int minutes = 0;
        int seconds = 0;
        int microseconds = 0;
        switch (columnInfo.getColumnType()) {
            case TIME: {
                final Calendar calendar = (userCalendar != null) ? userCalendar : Calendar.getInstance();
                boolean negate = false;
                if (this.length > 0) {
                    negate = ((this.buf[this.pos] & 0xFF) == 0x1);
                }
                if (this.length > 4) {
                    day = (this.buf[this.pos + 1] & 0xFF) + ((this.buf[this.pos + 2] & 0xFF) << 8) + ((this.buf[this.pos + 3] & 0xFF) << 16) + ((this.buf[this.pos + 4] & 0xFF) << 24);
                }
                if (this.length > 7) {
                    hour = this.buf[this.pos + 5];
                    minutes = this.buf[this.pos + 6];
                    seconds = this.buf[this.pos + 7];
                }
                if (this.length > 8) {
                    microseconds = (this.buf[this.pos + 8] & 0xFF) + ((this.buf[this.pos + 9] & 0xFF) << 8) + ((this.buf[this.pos + 10] & 0xFF) << 16) + ((this.buf[this.pos + 11] & 0xFF) << 24);
                }
                year = 1970;
                month = 1;
                day = (negate ? -1 : 1) * day + 1;
                hour *= (negate ? -1 : 1);
                break;
            }
            case STRING:
            case VARSTRING: {
                final String rawValue = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                if (rawValue.startsWith("0000-00-00 00:00:00")) {
                    this.lastValueNull |= 0x2;
                    return null;
                }
                if (rawValue.length() < 4) {
                    break;
                }
                year = Integer.parseInt(rawValue.substring(0, 4));
                if (rawValue.length() < 7) {
                    break;
                }
                month = Integer.parseInt(rawValue.substring(5, 7));
                if (rawValue.length() >= 10) {
                    day = Integer.parseInt(rawValue.substring(8, 10));
                    if (rawValue.length() >= 19) {
                        hour = Integer.parseInt(rawValue.substring(11, 13));
                        minutes = Integer.parseInt(rawValue.substring(14, 16));
                        seconds = Integer.parseInt(rawValue.substring(17, 19));
                    }
                    microseconds = this.extractNanos(rawValue) / 1000000;
                    break;
                }
                break;
            }
            default: {
                year = ((this.buf[this.pos] & 0xFF) | (this.buf[this.pos + 1] & 0xFF) << 8);
                month = this.buf[this.pos + 2];
                day = this.buf[this.pos + 3];
                if (this.length <= 4) {
                    break;
                }
                hour = this.buf[this.pos + 4];
                minutes = this.buf[this.pos + 5];
                seconds = this.buf[this.pos + 6];
                if (this.length > 7) {
                    microseconds = (this.buf[this.pos + 7] & 0xFF) + ((this.buf[this.pos + 8] & 0xFF) << 8) + ((this.buf[this.pos + 9] & 0xFF) << 16) + ((this.buf[this.pos + 10] & 0xFF) << 24);
                    break;
                }
                break;
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
        final Timestamp tt;
        synchronized (calendar) {
            calendar.clear();
            calendar.set(year, month - 1, day, hour, minutes, seconds);
            tt = new Timestamp(calendar.getTimeInMillis());
        }
        tt.setNanos(microseconds * 1000);
        return tt;
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
                    return this.buf[this.pos] != 0;
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
            case DOUBLE:
            case BINARY_DOUBLE: {
                return this.getInternalDouble(columnInfo);
            }
            case STRING:
            case VARSTRING:
            case VARCHAR:
            case VARCHAR2: {
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
            case MEDIUMINT:
            case NUMBER:
            case SMALLINT: {
                return this.getInternalInt(columnInfo);
            }
            case FLOAT: {
                return this.getInternalFloat(columnInfo);
            }
            case BINARY_FLOAT:
            case NUMBER_FLOAT: {
                final byte[] b = new byte[this.length];
                System.arraycopy(this.buf, this.pos, b, 0, this.length);
                final NUMBER number = new NUMBER(b);
                return number.bigDecimalValue();
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
            case TIMESTAMP_TZ: {
                return this.getInternalTIMESTAMPTZ(columnInfo, null, timeZone);
            }
            case TIMESTAMP_LTZ: {
                return this.getInternalTIMESTAMPLTZ(columnInfo, null, timeZone);
            }
            case STRUCT: {
                final byte[] structData = new byte[this.length];
                System.arraycopy(this.buf, this.pos, structData, 0, this.length);
                return structData;
            }
            case ARRAY: {
                final byte[] arrayData = new byte[this.length];
                System.arraycopy(this.buf, this.pos, arrayData, 0, this.length);
                return arrayData;
            }
            case CURSOR: {
                return null;
            }
            case RAW: {
                final byte[] returnBytes = new byte[this.length];
                System.arraycopy(this.buf, this.pos, returnBytes, 0, this.length);
                return returnBytes;
            }
        }
        throw ExceptionFactory.INSTANCE.notSupported(String.format("Type '%s' is not supported", columnInfo.getColumnType().getTypeName()));
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
            case BIGINT:
            case DOUBLE:
            case INTEGER:
            case MEDIUMINT:
            case FLOAT:
            case SMALLINT:
            case YEAR:
            case TINYINT:
            case DECIMAL:
            case OLDDECIMAL: {
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
        try {
            long value = 0L;
            switch (columnInfo.getColumnType()) {
                case BIT: {
                    value = this.parseBit();
                    break;
                }
                case TINYINT: {
                    value = this.getInternalTinyInt(columnInfo);
                    break;
                }
                case SMALLINT:
                case YEAR: {
                    value = this.getInternalSmallInt(columnInfo);
                    break;
                }
                case INTEGER:
                case MEDIUMINT: {
                    value = this.getInternalMediumInt(columnInfo);
                    break;
                }
                case BIGINT: {
                    value = this.getInternalLong(columnInfo);
                    break;
                }
                case FLOAT: {
                    value = (long)this.getInternalFloat(columnInfo);
                    break;
                }
                case DOUBLE: {
                    value = (long)this.getInternalDouble(columnInfo);
                    break;
                }
                case DECIMAL:
                case OLDDECIMAL: {
                    final BigDecimal bigDecimal = this.getInternalBigDecimal(columnInfo);
                    this.rangeCheck(Byte.class, -128L, 127L, bigDecimal, columnInfo);
                    return bigDecimal.byteValue();
                }
                case STRING:
                case VARSTRING:
                case OBDECIMAL:
                case VARCHAR:
                case VARCHAR2:
                case NVARCHAR2: {
                    value = Long.parseLong(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                    break;
                }
                default: {
                    throw new SQLException("getByte not available for data field type " + columnInfo.getColumnType().getJavaTypeName());
                }
            }
            this.rangeCheck(Byte.class, -128L, 127L, value, columnInfo);
            return (byte)value;
        }
        catch (NumberFormatException nfe) {
            final String valueParams = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
            if (BinaryRowProtocol.isIntegerRegex.matcher(valueParams).find()) {
                try {
                    final long value = Long.parseLong(valueParams.substring(0, valueParams.indexOf(".")));
                    this.rangeCheck(Byte.class, -128L, 127L, value, columnInfo);
                    return (byte)value;
                }
                catch (NumberFormatException ex) {}
            }
            throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' : value " + valueParams, "22003", 1264);
        }
    }
    
    @Override
    public short getInternalShort(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return 0;
        }
        try {
            long value = 0L;
            switch (columnInfo.getColumnType()) {
                case BIT: {
                    value = this.parseBit();
                    break;
                }
                case TINYINT: {
                    value = this.getInternalTinyInt(columnInfo);
                    break;
                }
                case SMALLINT:
                case YEAR: {
                    value = (this.buf[this.pos] & 0xFF) + ((this.buf[this.pos + 1] & 0xFF) << 8);
                    if (columnInfo.isSigned()) {
                        return (short)value;
                    }
                    value &= 0xFFFFL;
                    break;
                }
                case INTEGER:
                case MEDIUMINT: {
                    value = this.getInternalMediumInt(columnInfo);
                    break;
                }
                case BIGINT: {
                    value = this.getInternalLong(columnInfo);
                    break;
                }
                case FLOAT: {
                    value = (long)this.getInternalFloat(columnInfo);
                    break;
                }
                case DOUBLE: {
                    value = (long)this.getInternalDouble(columnInfo);
                    break;
                }
                case DECIMAL:
                case OLDDECIMAL: {
                    final BigDecimal bigDecimal = this.getInternalBigDecimal(columnInfo);
                    this.rangeCheck(Short.class, -32768L, 32767L, bigDecimal, columnInfo);
                    return bigDecimal.shortValue();
                }
                case STRING:
                case VARSTRING:
                case OBDECIMAL:
                case VARCHAR:
                case VARCHAR2:
                case NVARCHAR2: {
                    value = Long.parseLong(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())));
                    break;
                }
                default: {
                    throw new SQLException("getShort not available for data field type " + columnInfo.getColumnType().getJavaTypeName());
                }
            }
            this.rangeCheck(Short.class, -32768L, 32767L, value, columnInfo);
            return (short)value;
        }
        catch (NumberFormatException nfe) {
            final String valueParams = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
            if (BinaryRowProtocol.isIntegerRegex.matcher(valueParams).find()) {
                try {
                    final long value = Long.parseLong(valueParams.substring(0, valueParams.indexOf(".")));
                    this.rangeCheck(Short.class, -32768L, 32767L, value, columnInfo);
                    return (short)value;
                }
                catch (NumberFormatException ex) {}
            }
            throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' : value " + valueParams, "22003", 1264);
        }
    }
    
    @Override
    public String getInternalTimeString(final ColumnDefinition columnInfo) {
        if (this.lastValueWasNull()) {
            return null;
        }
        if (this.length == 0) {
            if (columnInfo.getDecimals() == 0) {
                return "00:00:00";
            }
            final StringBuilder value = new StringBuilder("00:00:00.");
            int decimal = columnInfo.getDecimals();
            while (decimal-- > 0) {
                value.append("0");
            }
            return value.toString();
        }
        else {
            final String rawValue = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
            if ("0000-00-00".equals(rawValue)) {
                return null;
            }
            final int day = (this.buf[this.pos + 1] & 0xFF) | (this.buf[this.pos + 2] & 0xFF) << 8 | (this.buf[this.pos + 3] & 0xFF) << 16 | (this.buf[this.pos + 4] & 0xFF) << 24;
            final int hour = this.buf[this.pos + 5];
            final int timeHour = hour + day * 24;
            String hourString;
            if (timeHour < 10) {
                hourString = "0" + timeHour;
            }
            else {
                hourString = Integer.toString(timeHour);
            }
            final int minutes = this.buf[this.pos + 6];
            String minuteString;
            if (minutes < 10) {
                minuteString = "0" + minutes;
            }
            else {
                minuteString = Integer.toString(minutes);
            }
            final int seconds = this.buf[this.pos + 7];
            String secondString;
            if (seconds < 10) {
                secondString = "0" + seconds;
            }
            else {
                secondString = Integer.toString(seconds);
            }
            int microseconds = 0;
            if (this.length > 8) {
                microseconds = ((this.buf[this.pos + 8] & 0xFF) | (this.buf[this.pos + 9] & 0xFF) << 8 | (this.buf[this.pos + 10] & 0xFF) << 16 | (this.buf[this.pos + 11] & 0xFF) << 24);
            }
            final StringBuilder microsecondString = new StringBuilder(Integer.toString(microseconds));
            while (microsecondString.length() < 6) {
                microsecondString.insert(0, "0");
            }
            final boolean negative = this.buf[this.pos] == 1;
            return (negative ? "-" : "") + hourString + ":" + minuteString + ":" + secondString + "." + (Object)microsecondString;
        }
    }
    
    @Override
    public BigInteger getInternalBigInteger(final ColumnDefinition columnInfo) throws SQLException {
        if (this.lastValueWasNull()) {
            return null;
        }
        switch (columnInfo.getColumnType()) {
            case BIT: {
                return BigInteger.valueOf(this.buf[this.pos]);
            }
            case TINYINT: {
                return BigInteger.valueOf(columnInfo.isSigned() ? this.buf[this.pos] : ((long)(this.buf[this.pos] & 0xFF)));
            }
            case SMALLINT:
            case YEAR: {
                final short valueShort = (short)((this.buf[this.pos] & 0xFF) | (this.buf[this.pos + 1] & 0xFF) << 8);
                return BigInteger.valueOf(columnInfo.isSigned() ? valueShort : ((long)(valueShort & 0xFFFF)));
            }
            case INTEGER:
            case MEDIUMINT: {
                final int valueInt = (this.buf[this.pos] & 0xFF) + ((this.buf[this.pos + 1] & 0xFF) << 8) + ((this.buf[this.pos + 2] & 0xFF) << 16) + ((this.buf[this.pos + 3] & 0xFF) << 24);
                return BigInteger.valueOf(columnInfo.isSigned() ? valueInt : ((valueInt >= 0) ? valueInt : ((long)valueInt & 0xFFFFFFFFL)));
            }
            case BIGINT: {
                final long value = (this.buf[this.pos] & 0xFF) + ((long)(this.buf[this.pos + 1] & 0xFF) << 8) + ((long)(this.buf[this.pos + 2] & 0xFF) << 16) + ((long)(this.buf[this.pos + 3] & 0xFF) << 24) + ((long)(this.buf[this.pos + 4] & 0xFF) << 32) + ((long)(this.buf[this.pos + 5] & 0xFF) << 40) + ((long)(this.buf[this.pos + 6] & 0xFF) << 48) + ((long)(this.buf[this.pos + 7] & 0xFF) << 56);
                if (columnInfo.isSigned()) {
                    return BigInteger.valueOf(value);
                }
                return new BigInteger(1, new byte[] { (byte)(value >> 56), (byte)(value >> 48), (byte)(value >> 40), (byte)(value >> 32), (byte)(value >> 24), (byte)(value >> 16), (byte)(value >> 8), (byte)value });
            }
            case FLOAT: {
                return BigInteger.valueOf((long)this.getInternalFloat(columnInfo));
            }
            case DOUBLE: {
                return BigInteger.valueOf((long)this.getInternalDouble(columnInfo));
            }
            case DECIMAL:
            case OLDDECIMAL:
            case OBDECIMAL: {
                return BigInteger.valueOf(this.getInternalBigDecimal(columnInfo).longValue());
            }
            default: {
                return new BigInteger(new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType())).trim());
            }
        }
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
            switch (columnInfo.getColumnType().getSqlType()) {
                case 91:
                case 93: {
                    final int year = (this.buf[this.pos] & 0xFF) | (this.buf[this.pos + 1] & 0xFF) << 8;
                    final int month = this.buf[this.pos + 2];
                    final int day = this.buf[this.pos + 3];
                    int hour = 0;
                    int minutes = 0;
                    int seconds = 0;
                    int microseconds = 0;
                    if (this.length > 4) {
                        hour = this.buf[this.pos + 4];
                        minutes = this.buf[this.pos + 5];
                        seconds = this.buf[this.pos + 6];
                        if (this.length > 7) {
                            microseconds = (this.buf[this.pos + 7] & 0xFF) + ((this.buf[this.pos + 8] & 0xFF) << 8) + ((this.buf[this.pos + 9] & 0xFF) << 16) + ((this.buf[this.pos + 10] & 0xFF) << 24);
                        }
                    }
                    return ZonedDateTime.of(year, month, day, hour, minutes, seconds, microseconds * 1000, timeZone.toZoneId());
                }
                case -1:
                case 1:
                case 12: {
                    final String raw = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                    if (raw.startsWith("0000-00-00 00:00:00")) {
                        return null;
                    }
                    try {
                        return ZonedDateTime.parse(raw, BinaryRowProtocol.TEXT_ZONED_DATE_TIME);
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
            int day = 0;
            int hour = 0;
            int minutes = 0;
            int seconds = 0;
            int microseconds = 0;
            switch (columnInfo.getColumnType().getSqlType()) {
                case 93: {
                    final int year = (this.buf[this.pos] & 0xFF) | (this.buf[this.pos + 1] & 0xFF) << 8;
                    final int month = this.buf[this.pos + 2];
                    day = this.buf[this.pos + 3];
                    if (this.length > 4) {
                        hour = this.buf[this.pos + 4];
                        minutes = this.buf[this.pos + 5];
                        seconds = this.buf[this.pos + 6];
                        if (this.length > 7) {
                            microseconds = (this.buf[this.pos + 7] & 0xFF) + ((this.buf[this.pos + 8] & 0xFF) << 8) + ((this.buf[this.pos + 9] & 0xFF) << 16) + ((this.buf[this.pos + 10] & 0xFF) << 24);
                        }
                    }
                    return ZonedDateTime.of(year, month, day, hour, minutes, seconds, microseconds * 1000, zoneOffset).toOffsetDateTime().toOffsetTime();
                }
                case 92: {
                    final boolean negate = (this.buf[this.pos] & 0xFF) == 0x1;
                    if (this.length > 4) {
                        day = (this.buf[this.pos + 1] & 0xFF) + ((this.buf[this.pos + 2] & 0xFF) << 8) + ((this.buf[this.pos + 3] & 0xFF) << 16) + ((this.buf[this.pos + 4] & 0xFF) << 24);
                    }
                    if (this.length > 7) {
                        hour = this.buf[this.pos + 5];
                        minutes = this.buf[this.pos + 6];
                        seconds = this.buf[this.pos + 7];
                    }
                    if (this.length > 8) {
                        microseconds = (this.buf[this.pos + 8] & 0xFF) + ((this.buf[this.pos + 9] & 0xFF) << 8) + ((this.buf[this.pos + 10] & 0xFF) << 16) + ((this.buf[this.pos + 11] & 0xFF) << 24);
                    }
                    return OffsetTime.of((negate ? -1 : 1) * (day * 24 + hour), minutes, seconds, microseconds * 1000, zoneOffset);
                }
                case -1:
                case 1:
                case 12: {
                    final String raw = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
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
        switch (columnInfo.getColumnType().getSqlType()) {
            case 92: {
                int day = 0;
                int hour = 0;
                int minutes = 0;
                int seconds = 0;
                int microseconds = 0;
                final boolean negate = (this.buf[this.pos] & 0xFF) == 0x1;
                if (this.length > 4) {
                    day = (this.buf[this.pos + 1] & 0xFF) + ((this.buf[this.pos + 2] & 0xFF) << 8) + ((this.buf[this.pos + 3] & 0xFF) << 16) + ((this.buf[this.pos + 4] & 0xFF) << 24);
                }
                if (this.length > 7) {
                    hour = this.buf[this.pos + 5];
                    minutes = this.buf[this.pos + 6];
                    seconds = this.buf[this.pos + 7];
                }
                if (this.length > 8) {
                    microseconds = (this.buf[this.pos + 8] & 0xFF) + ((this.buf[this.pos + 9] & 0xFF) << 8) + ((this.buf[this.pos + 10] & 0xFF) << 16) + ((this.buf[this.pos + 11] & 0xFF) << 24);
                }
                return LocalTime.of((negate ? -1 : 1) * (day * 24 + hour), minutes, seconds, microseconds * 1000);
            }
            case -1:
            case 1:
            case 12: {
                final String raw = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
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
        switch (columnInfo.getColumnType().getSqlType()) {
            case 91: {
                final int year = (this.buf[this.pos] & 0xFF) | (this.buf[this.pos + 1] & 0xFF) << 8;
                final int month = this.buf[this.pos + 2];
                final int day = this.buf[this.pos + 3];
                return LocalDate.of(year, month, day);
            }
            case 93: {
                final ZonedDateTime zonedDateTime = this.getInternalZonedDateTime(columnInfo, LocalDate.class, timeZone);
                return (zonedDateTime == null) ? null : zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate();
            }
            case -1:
            case 1:
            case 12: {
                final String raw = new String(this.buf, this.pos, this.length, this.getCurrentEncoding(columnInfo.getColumnType()));
                if (raw.startsWith("0000-00-00")) {
                    return null;
                }
                try {
                    return LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE.withZone(timeZone.toZoneId()));
                }
                catch (DateTimeParseException dateParserEx) {
                    throw new SQLException(raw + " cannot be parse as LocalDate. time must have \"yyyy-MM-dd\" format");
                }
                break;
            }
        }
        throw new SQLException("Cannot read LocalDate using a " + columnInfo.getColumnType().getJavaTypeName() + " field");
    }
    
    private ComplexData getComplexField(final Buffer packet, final ComplexDataType type) throws SQLException {
        ComplexData value = null;
        if (null == type || !type.isValid()) {
            throw new SQLException(String.format("invalid complex type, check if exists, typeName=%s", type.getTypeName()));
        }
        switch (type.getType()) {
            case 4: {
                value = this.getComplexArray(packet, type);
                break;
            }
            case 3: {
                value = this.getComplexStruct(packet, type);
                break;
            }
            default: {
                throw new SQLException(String.format("invalid complex type, check if exists, typeName=%s", type.getTypeName()));
            }
        }
        return value;
    }
    
    private ComplexData getComplexArray(final Buffer packet, final ComplexDataType type) throws SQLException {
        final ComplexData array = new ArrayImpl(type);
        final int attrCount = (int)packet.readFieldLength();
        array.setAttrCount(attrCount);
        final int curPos = packet.getPosition();
        final byte[] nullBitsBuffer = packet.getBytes(curPos, (attrCount + 7 + 2) / 8);
        packet.setPosition(curPos + (attrCount + 7 + 2) / 8);
        for (int i = 0; i < attrCount; ++i) {
            if ((nullBitsBuffer[(i + 2) / 8] & 1 << (i + 2) % 8) == 0x0) {
                final Object value = this.getComplexAttrData(packet, type.getAttrType(0));
                array.addAttrData(i, value);
            }
            else {
                array.addAttrData(i, null);
            }
        }
        return array;
    }
    
    public ComplexData getComplexStruct(final Buffer packet, final ComplexDataType type) throws SQLException {
        final ComplexData struct = new StructImpl(type);
        final int attrCount = type.getAttrCount();
        struct.setAttrCount(attrCount);
        final int curPos = packet.getPosition();
        final byte[] nullBitsBuffer = packet.getBytes(curPos, (attrCount + 7 + 2) / 8);
        packet.setPosition(curPos + (attrCount + 7 + 2) / 8);
        for (int i = 0; i < attrCount; ++i) {
            if ((nullBitsBuffer[(i + 2) / 8] & 1 << (i + 2) % 8) == 0x0) {
                final Object value = this.getComplexAttrData(packet, type.getAttrType(i));
                struct.addAttrData(i, value);
            }
            else {
                struct.addAttrData(i, null);
            }
        }
        return struct;
    }
    
    private Timestamp getComplexDate(final byte[] bits) {
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int minute = 0;
        int seconds = 0;
        int nanos = 0;
        if (null == bits) {
            return null;
        }
        final int length = bits.length;
        if (length != 0) {
            year = ((bits[0] & 0xFF) | (bits[1] & 0xFF) << 8);
            month = bits[2];
            day = bits[3];
            if (length > 4) {
                hour = bits[4];
                minute = bits[5];
                seconds = bits[6];
            }
            if (length > 7) {
                nanos = ((bits[7] & 0xFF) | (bits[8] & 0xFF) << 8 | (bits[9] & 0xFF) << 16 | (bits[10] & 0xFF) << 24) * 1000;
            }
        }
        final Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, minute, seconds);
        final long tsAsMillis = cal.getTimeInMillis();
        final Timestamp ts = new Timestamp(tsAsMillis);
        ts.setNanos(nanos);
        return ts;
    }
    
    private Object getComplexAttrData(final Buffer packet, final ComplexDataType type) throws SQLException {
        Object value = null;
        byte[] b = null;
        final Charset charset = Charset.forName(this.getProtocol().getEncoding());
        switch (type.getType()) {
            case 0: {
                b = packet.readLenByteArray(0);
                value = new BigDecimal(new String(b, 0, b.length, StandardCharsets.UTF_8).trim());
                break;
            }
            case 1: {
                b = packet.readLenByteArray(0);
                value = new String(b, 0, b.length, charset);
                break;
            }
            case 6: {
                b = (byte[])(value = packet.readLenByteArray(0));
                break;
            }
            case 2: {
                value = this.getComplexDate(packet.readLenByteArray(0));
                break;
            }
            case 4: {
                value = this.getComplexArray(packet, type);
                break;
            }
            case 3: {
                value = this.getComplexStruct(packet, type);
                break;
            }
            case 7: {
                b = packet.readLenByteArray(0);
                value = (char)b[0];
                break;
            }
            default: {
                throw new SQLException("unsupported complex data type");
            }
        }
        return value;
    }
    
    @Override
    public Array getInternalArray(final ColumnDefinition columnInf, final ComplexDataType complexDataType) throws SQLException {
        Array ret = null;
        final Buffer buffer = new Buffer(this.buf);
        buffer.setPosition(this.pos);
        ret = (ObArray)this.getComplexField(buffer, complexDataType);
        this.pos = buffer.getPosition();
        return ret;
    }
    
    @Override
    public Struct getInternalStruct(final ColumnDefinition columnInfo, final ComplexDataType complexDataType) throws SQLException {
        ObStruct struct = null;
        final Buffer buffer = new Buffer(this.buf);
        buffer.setPosition(this.pos);
        struct = (ObStruct)this.getComplexField(buffer, complexDataType);
        this.pos = buffer.getPosition();
        return struct;
    }
    
    @Override
    public ComplexData getInternalComplexCursor(final ColumnDefinition columnInfo, final ComplexDataType complexDataType) throws SQLException {
        final ComplexData value = new ComplexData(complexDataType);
        if (this.buf.length <= this.pos) {
            throw new SQLException("cursor is not open");
        }
        final Buffer buffer = new Buffer(this.buf);
        buffer.setPosition(this.pos);
        final int id = (int)buffer.readLongV1();
        value.setAttrCount(1);
        final RowObCursorData rowObCursorData = new RowObCursorData(id, true);
        value.addAttrData(0, rowObCursorData);
        this.pos = buffer.getPosition();
        return value;
    }
    
    @Override
    public INTERVALDS getInternalINTERVALDS(final ColumnDefinition columnInfo) throws SQLException {
        if (columnInfo.getColumnType() != ColumnType.INTERVALDS) {
            throw new SQLException("the field type is not FIELD_TYPE_INTERVALDS");
        }
        final byte[] target = new byte[this.length];
        System.arraycopy(this.buf, this.pos, target, 0, this.length);
        return new INTERVALDS(target);
    }
    
    @Override
    public INTERVALYM getInternalINTERVALYM(final ColumnDefinition columnInfo) throws SQLException {
        if (columnInfo.getColumnType() != ColumnType.INTERVALYM) {
            throw new SQLException("the field type is not FIELD_TYPE_INTERVALYM");
        }
        final byte[] target = new byte[this.length];
        System.arraycopy(this.buf, this.pos, target, 0, this.length);
        return new INTERVALYM(target);
    }
    
    @Override
    public boolean isBinaryEncoded() {
        return true;
    }
}
