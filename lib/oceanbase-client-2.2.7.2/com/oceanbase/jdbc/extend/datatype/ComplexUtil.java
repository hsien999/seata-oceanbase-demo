// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.sql.Timestamp;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import java.sql.SQLException;
import com.oceanbase.jdbc.util.Options;
import java.util.Date;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class ComplexUtil
{
    public static void storeDateTime(final PacketOutputStream pos, final Date dt, final int bufferType, final Options options) throws SQLException, IOException {
        storeDateTime413AndNewer(pos, dt, bufferType, options);
    }
    
    public static void storeDateTime413AndNewer(final PacketOutputStream pos, final Date dt, final int bufferType, final Options options) throws SQLException, IOException {
        Calendar sessionCalendar = null;
        if (!options.useLegacyDatetimeCode) {
            if (bufferType == ColumnType.DATE.getType()) {
                sessionCalendar = new GregorianCalendar(TimeZone.getDefault());
            }
            else {
                sessionCalendar = new GregorianCalendar(pos.getTimeZone());
            }
        }
        else {
            sessionCalendar = new GregorianCalendar();
        }
        final Date oldTime = sessionCalendar.getTime();
        try {
            sessionCalendar.setTime(dt);
            if (dt instanceof java.sql.Date) {
                sessionCalendar.set(11, 0);
                sessionCalendar.set(12, 0);
                sessionCalendar.set(13, 0);
            }
            byte length = 7;
            if (dt instanceof Timestamp) {
                length = 11;
            }
            pos.writeBytes(length, 1);
            final int year = sessionCalendar.get(1);
            final int month = sessionCalendar.get(2) + 1;
            final int date = sessionCalendar.get(5);
            pos.writeIntV1(year);
            pos.writeBytes((byte)month, 1);
            pos.writeBytes((byte)date, 1);
            if (dt instanceof java.sql.Date) {
                pos.writeBytes((byte)0, 1);
                pos.writeBytes((byte)0, 1);
                pos.writeBytes((byte)0, 1);
            }
            else {
                pos.writeBytes((byte)sessionCalendar.get(11), 1);
                pos.writeBytes((byte)sessionCalendar.get(12), 1);
                pos.writeBytes((byte)sessionCalendar.get(13), 1);
            }
            if (length == 11) {
                pos.writeLongV1(((Timestamp)dt).getNanos() / 1000);
            }
        }
        finally {
            sessionCalendar.setTime(oldTime);
        }
    }
    
    public static ColumnType getMysqlType(final int complexType) throws SQLException {
        switch (complexType) {
            case 2: {
                return ColumnType.DATETIME;
            }
            case 4: {
                return ColumnType.COMPLEX;
            }
            case 0: {
                return ColumnType.DECIMAL;
            }
            case 1: {
                return ColumnType.VARCHAR;
            }
            case 3: {
                return ColumnType.COMPLEX;
            }
            case 6: {
                return ColumnType.RAW;
            }
            default: {
                throw new SQLException("unsupported complex type");
            }
        }
    }
    
    public static void storeComplexStruct(final PacketOutputStream pos, final ComplexData data, final Options options) throws Exception {
        final int nullCount = (data.getAttrCount() + 7) / 8;
        final int nullBitsPosition = pos.getPosition();
        for (int i = 0; i < nullCount; ++i) {
            pos.writeBytes((byte)0, 1);
        }
        final byte[] nullBitsBuffer = new byte[nullCount];
        for (int j = 0; j < data.getAttrCount(); ++j) {
            if (null != data.getAttrData(j)) {
                storeComplexAttrData(pos, data.getComplexType().getAttrType(j), data.getAttrData(j), options);
            }
            else {
                final byte[] array = nullBitsBuffer;
                final int n = j / 8;
                array[n] |= (byte)(1 << j % 8);
            }
        }
        final int endPosition = pos.getPosition();
        pos.setPosition(nullBitsPosition);
        pos.write(nullBitsBuffer);
        pos.setPosition(endPosition);
    }
    
    public static void storeComplexArray(final PacketOutputStream pos, final ComplexData data, final Options options) throws Exception {
        pos.writeFieldLength(data.getAttrCount());
        final int nullCount = (data.getAttrCount() + 7) / 8;
        final int nullBitsPosition = pos.getPosition();
        for (int i = 0; i < nullCount; ++i) {
            pos.writeBytes((byte)0, 1);
        }
        final byte[] nullBitsBuffer = new byte[nullCount];
        for (int j = 0; j < data.getAttrCount(); ++j) {
            if (null != data.getAttrData(j)) {
                storeComplexAttrData(pos, data.getComplexType().getAttrType(0), data.getAttrData(j), options);
            }
            else {
                final byte[] array = nullBitsBuffer;
                final int n = j / 8;
                array[n] |= (byte)(1 << j % 8);
            }
        }
        final int endPosition = pos.getPosition();
        pos.setPosition(nullBitsPosition);
        pos.write(nullBitsBuffer);
        pos.setPosition(endPosition);
    }
    
    public static void storeComplexAttrData(final PacketOutputStream pos, final ComplexDataType type, final Object value, final Options options) throws Exception {
        switch (type.getType()) {
            case 4: {
                storeComplexArray(pos, (ComplexData)value, options);
            }
            case 3: {
                storeComplexStruct(pos, (ComplexData)value, options);
            }
            case 0: {
                final String valueStr = String.valueOf(value);
                pos.writeFieldLength(valueStr.getBytes().length);
                pos.write(valueStr.getBytes(StandardCharsets.UTF_8));
            }
            case 2: {
                storeDateTime(pos, (Date)value, ColumnType.DATETIME.getType(), options);
            }
            case 1:
            case 6: {
                if (value instanceof byte[]) {
                    final byte[] tmp = (byte[])value;
                    pos.writeFieldLength(tmp.length);
                    pos.write(tmp);
                }
                else {
                    final byte[] tmp = ((String)value).getBytes();
                    pos.writeFieldLength(tmp.length);
                    pos.write(tmp);
                }
            }
            default: {
                throw new SQLException("unsupported complex data type");
            }
        }
    }
}
