// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

import java.io.Reader;
import java.sql.Timestamp;
import java.util.Calendar;
import java.sql.Time;
import java.sql.Date;
import java.math.BigDecimal;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;
import com.oceanbase.jdbc.OceanBaseConnection;
import java.sql.Connection;
import java.io.Serializable;

public abstract class Datum implements Serializable
{
    protected byte[] data;
    static final long serialVersionUID = 4645732484621936751L;
    
    public Datum() {
    }
    
    public static String getSessionTimeZone(final Connection connection) throws SQLException {
        if (connection instanceof OceanBaseConnection) {
            return ((OceanBaseConnection)connection).getSessionTimeZone();
        }
        throw new SQLException("unexpected connection type");
    }
    
    protected static final int getInt(final byte[] bytes, final int idx) {
        int value = (bytes[idx + 3] & 0xFF) << 24;
        value |= (bytes[idx + 2] & 0xFF) << 16;
        value |= (bytes[idx + 1] & 0xFF) << 8;
        value |= (bytes[idx] & 0xFF);
        return value;
    }
    
    protected static byte[] intToBytes(final int val) {
        final byte[] b = { (byte)(val & 0xFF), (byte)(val >> 8 & 0xFF), (byte)(val >> 16 & 0xFF), (byte)(val >> 24 & 0xFF) };
        return b;
    }
    
    public Datum(final byte[] bytes) {
        this.data = bytes;
    }
    
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || !(object instanceof Datum)) {
            return true;
        }
        if (this.getClass() != object.getClass()) {
            return false;
        }
        final Datum datum = (Datum)object;
        if (this.data == null && datum.data == null) {
            return true;
        }
        if ((this.data == null && datum.data != null) || (this.data != null && datum.data == null)) {
            return false;
        }
        if (this.data.length != datum.data.length) {
            return false;
        }
        for (int i = 0; i < this.data.length; ++i) {
            if (this.data[i] != datum.data[i]) {
                return false;
            }
        }
        return true;
    }
    
    public byte[] shareBytes() {
        return this.data;
    }
    
    public long getLength() {
        return (null == this.data) ? 0L : this.data.length;
    }
    
    public void setBytes(final byte[] bytes) {
        final int len = bytes.length;
        System.arraycopy(bytes, 0, this.data = new byte[len], 0, len);
    }
    
    public void setShareBytes(final byte[] bytes) {
        this.data = bytes;
    }
    
    public byte[] getBytes() {
        if (this.data == null) {
            return new byte[0];
        }
        final byte[] bytes = new byte[this.data.length];
        System.arraycopy(this.data, 0, bytes, 0, this.data.length);
        return bytes;
    }
    
    public void setByte(final int index, final byte b) {
        if (index < this.data.length) {
            this.data[index] = b;
        }
    }
    
    public InputStream getStream() {
        return new ByteArrayInputStream(this.data);
    }
    
    public String stringValue() throws SQLException {
        throw new SQLException("Conversion to String failed");
    }
    
    public String stringValue(final Connection var1) throws SQLException {
        return this.stringValue();
    }
    
    public boolean booleanValue() throws SQLException {
        throw new SQLException("Conversion to boolean failed");
    }
    
    public int intValue() throws SQLException {
        throw new SQLException("Conversion to integer failed");
    }
    
    public long longValue() throws SQLException {
        throw new SQLException("Conversion to long failed");
    }
    
    public float floatValue() throws SQLException {
        throw new SQLException("Conversion to float failed");
    }
    
    public double doubleValue() throws SQLException {
        throw new SQLException("Conversion to double failed");
    }
    
    public byte byteValue() throws SQLException {
        throw new SQLException("Conversion to byte failed");
    }
    
    public BigDecimal bigDecimalValue() throws SQLException {
        throw new SQLException("Conversion to BigDecimal failed");
    }
    
    public Date dateValue() throws SQLException {
        throw new SQLException("Conversion to Date failed");
    }
    
    public Time timeValue() throws SQLException {
        throw new SQLException("Conversion to Time failed");
    }
    
    public Time timeValue(final Calendar var1) throws SQLException {
        throw new SQLException("Conversion to Time failed");
    }
    
    public Timestamp timestampValue() throws SQLException {
        throw new SQLException("Conversion to Timestamp failed");
    }
    
    public Timestamp timestampValue(final Calendar var1) throws SQLException {
        throw new SQLException("Conversion to Timestamp failed");
    }
    
    public Reader characterStreamValue() throws SQLException {
        throw new SQLException("Conversion to character stream failed");
    }
    
    public InputStream asciiStreamValue() throws SQLException {
        throw new SQLException("Conversion to ascii stream failed");
    }
    
    public InputStream binaryStreamValue() throws SQLException {
        throw new SQLException("Conversion to binary stream failed");
    }
    
    public abstract boolean isConvertibleTo(final Class p0);
    
    public abstract Object toJdbc() throws SQLException;
    
    public abstract Object makeJdbcArray(final int p0);
}
