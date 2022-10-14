// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.RowId;
import java.net.URL;
import java.util.Calendar;
import java.sql.Array;
import java.sql.Clob;
import java.sql.Blob;
import java.sql.Ref;
import java.sql.Statement;
import java.io.Reader;
import java.sql.ResultSetMetaData;
import java.sql.SQLWarning;
import java.sql.SQLFeatureNotSupportedException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Map;
import java.sql.SQLException;
import com.oceanbase.jdbc.ObArray;

public class ArrayImpl extends ComplexData implements ObArray
{
    private int cur_pos;
    public final int COLUMN_INDEX_INDEX = 1;
    public final int COLUMN_INDEX_VALUE = 2;
    
    public ArrayImpl(final ComplexDataType type) {
        super(type);
        this.cur_pos = -1;
    }
    
    @Override
    public String getBaseTypeName() throws SQLException {
        return this.getComplexType().getAttrType(0).getTypeName();
    }
    
    @Override
    public int getBaseType() throws SQLException {
        return this.getComplexType().getAttrType(0).getType();
    }
    
    @Override
    public Object getArray() throws SQLException {
        return this.getAttrData();
    }
    
    @Override
    public Object getArray(final Map<String, Class<?>> map) throws SQLException {
        return null;
    }
    
    @Override
    public Object getArray(final long index, final int count) throws SQLException {
        return null;
    }
    
    @Override
    public Object getArray(final long index, final int count, final Map<String, Class<?>> map) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        return this;
    }
    
    @Override
    public ResultSet getResultSet(final Map<String, Class<?>> map) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getResultSet(final long index, final int count) throws SQLException {
        return null;
    }
    
    @Override
    public ResultSet getResultSet(final long index, final int count, final Map<String, Class<?>> map) throws SQLException {
        return null;
    }
    
    @Override
    public void free() throws SQLException {
        this.setAttrData(null);
    }
    
    @Override
    public boolean next() throws SQLException {
        if (this.cur_pos + 1 < this.getAttrCount()) {
            ++this.cur_pos;
            return true;
        }
        return false;
    }
    
    @Override
    public void close() throws SQLException {
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return this.getArray() == null;
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        return (String)this.getObject(columnIndex);
    }
    
    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return (boolean)this.getObject(columnIndex);
    }
    
    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return (byte)this.getObject(columnIndex);
    }
    
    @Override
    public short getShort(final int columnIndex) throws SQLException {
        final Object object = this.getObject(columnIndex);
        if (object instanceof BigDecimal) {
            return ((BigDecimal)object).shortValue();
        }
        return (short)this.getObject(columnIndex);
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        if (columnIndex == 1) {
            return this.cur_pos + 1;
        }
        final Object object = this.getObject(columnIndex);
        if (object instanceof BigDecimal) {
            return ((BigDecimal)object).intValue();
        }
        return (int)this.getObject(columnIndex);
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        final Object object = this.getObject(columnIndex);
        if (object instanceof BigDecimal) {
            return ((BigDecimal)object).longValue();
        }
        return (long)this.getObject(columnIndex);
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        final Object object = this.getObject(columnIndex);
        if (object instanceof BigDecimal) {
            return ((BigDecimal)object).floatValue();
        }
        return (float)this.getObject(columnIndex);
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        final Object object = this.getObject(columnIndex);
        if (object instanceof BigDecimal) {
            return ((BigDecimal)object).doubleValue();
        }
        return (double)this.getObject(columnIndex);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return (BigDecimal)this.getObject(columnIndex);
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return (byte[])this.getObject(columnIndex);
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return (Date)this.getObject(columnIndex);
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return (Time)this.getObject(columnIndex);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return (Timestamp)this.getObject(columnIndex);
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return (InputStream)this.getObject(columnIndex);
    }
    
    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return (InputStream)this.getObject(columnIndex);
    }
    
    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return (InputStream)this.getObject(columnIndex);
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public InputStream getBinaryStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }
    
    @Override
    public void clearWarnings() throws SQLException {
    }
    
    @Override
    public String getCursorName() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        if (columnIndex == 1) {
            return this.cur_pos;
        }
        if (columnIndex != 2) {
            throw new SQLFeatureNotSupportedException();
        }
        if (this.attrData == null) {
            return null;
        }
        return this.attrData[this.cur_pos];
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return (Reader)this.getObject(columnIndex);
    }
    
    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return (BigDecimal)this.getObject(columnIndex);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public boolean isBeforeFirst() throws SQLException {
        return this.cur_pos == -1;
    }
    
    @Override
    public boolean isAfterLast() throws SQLException {
        return this.cur_pos >= this.getAttrCount();
    }
    
    @Override
    public boolean isFirst() throws SQLException {
        return this.cur_pos == 1;
    }
    
    @Override
    public boolean isLast() throws SQLException {
        return this.cur_pos == this.getAttrCount() - 1;
    }
    
    @Override
    public void beforeFirst() throws SQLException {
        this.cur_pos = -1;
    }
    
    @Override
    public void afterLast() throws SQLException {
        this.cur_pos = this.getAttrCount();
    }
    
    @Override
    public boolean first() throws SQLException {
        this.cur_pos = 0;
        return true;
    }
    
    @Override
    public boolean last() throws SQLException {
        this.cur_pos = this.getAttrCount() - 1;
        return true;
    }
    
    @Override
    public int getRow() throws SQLException {
        return this.cur_pos;
    }
    
    @Override
    public boolean absolute(final int row) throws SQLException {
        if (row < this.getAttrCount()) {
            this.cur_pos = row;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean relative(final int rows) throws SQLException {
        final int tmp_pos = this.cur_pos + rows;
        if (tmp_pos >= 0 && tmp_pos < this.getAttrCount()) {
            this.cur_pos = tmp_pos;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean previous() throws SQLException {
        if (this.cur_pos > 0) {
            --this.cur_pos;
            return true;
        }
        return false;
    }
    
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
    }
    
    @Override
    public int getFetchDirection() throws SQLException {
        return 0;
    }
    
    @Override
    public void setFetchSize(final int rows) throws SQLException {
    }
    
    @Override
    public int getFetchSize() throws SQLException {
        return 0;
    }
    
    @Override
    public int getType() throws SQLException {
        return 0;
    }
    
    @Override
    public int getConcurrency() throws SQLException {
        return 0;
    }
    
    @Override
    public boolean rowUpdated() throws SQLException {
        return false;
    }
    
    @Override
    public boolean rowInserted() throws SQLException {
        return false;
    }
    
    @Override
    public boolean rowDeleted() throws SQLException {
        return false;
    }
    
    @Override
    public void updateNull(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBoolean(final int columnIndex, final boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateByte(final int columnIndex, final byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateShort(final int columnIndex, final short x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateInt(final int columnIndex, final int x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateLong(final int columnIndex, final long x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateFloat(final int columnIndex, final float x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateDouble(final int columnIndex, final double x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBigDecimal(final int columnIndex, final BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateString(final int columnIndex, final String x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBytes(final int columnIndex, final byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateDate(final int columnIndex, final Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateTime(final int columnIndex, final Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateTimestamp(final int columnIndex, final Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateObject(final int columnIndex, final Object x, final int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateObject(final int columnIndex, final Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNull(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBoolean(final String columnLabel, final boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateByte(final String columnLabel, final byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateShort(final String columnLabel, final short x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateInt(final String columnLabel, final int x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateLong(final String columnLabel, final long x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateFloat(final String columnLabel, final float x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateDouble(final String columnLabel, final double x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBigDecimal(final String columnLabel, final BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateString(final String columnLabel, final String x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBytes(final String columnLabel, final byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateDate(final String columnLabel, final Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateTime(final String columnLabel, final Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateTimestamp(final String columnLabel, final Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateObject(final String columnLabel, final Object x, final int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateObject(final String columnLabel, final Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void insertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void deleteRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void refreshRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void cancelRowUpdates() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void moveToInsertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }
    
    @Override
    public Object getObject(final int columnIndex, final Map<String, Class<?>> map) throws SQLException {
        return null;
    }
    
    @Override
    public Ref getRef(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Blob getBlob(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Array getArray(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Object getObject(final String columnLabel, final Map<String, Class<?>> map) throws SQLException {
        return null;
    }
    
    @Override
    public Ref getRef(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public Blob getBlob(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public Clob getClob(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public Array getArray(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return null;
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return null;
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return null;
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public void updateRef(final int columnIndex, final Ref x) throws SQLException {
    }
    
    @Override
    public void updateRef(final String columnLabel, final Ref x) throws SQLException {
    }
    
    @Override
    public void updateBlob(final int columnIndex, final Blob x) throws SQLException {
    }
    
    @Override
    public void updateBlob(final String columnLabel, final Blob x) throws SQLException {
    }
    
    @Override
    public void updateClob(final int columnIndex, final Clob x) throws SQLException {
    }
    
    @Override
    public void updateClob(final String columnLabel, final Clob x) throws SQLException {
    }
    
    @Override
    public void updateArray(final int columnIndex, final Array x) throws SQLException {
    }
    
    @Override
    public void updateArray(final String columnLabel, final Array x) throws SQLException {
    }
    
    @Override
    public RowId getRowId(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public RowId getRowId(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public void updateRowId(final int columnIndex, final RowId x) throws SQLException {
    }
    
    @Override
    public void updateRowId(final String columnLabel, final RowId x) throws SQLException {
    }
    
    @Override
    public int getHoldability() throws SQLException {
        return 0;
    }
    
    @Override
    public boolean isClosed() throws SQLException {
        return false;
    }
    
    @Override
    public void updateNString(final int columnIndex, final String nString) throws SQLException {
    }
    
    @Override
    public void updateNString(final String columnLabel, final String nString) throws SQLException {
    }
    
    @Override
    public void updateNClob(final int columnIndex, final NClob nClob) throws SQLException {
    }
    
    @Override
    public void updateNClob(final String columnLabel, final NClob nClob) throws SQLException {
    }
    
    @Override
    public NClob getNClob(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public NClob getNClob(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) throws SQLException {
    }
    
    @Override
    public void updateSQLXML(final String columnLabel, final SQLXML xmlObject) throws SQLException {
    }
    
    @Override
    public String getNString(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public String getNString(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public Reader getNCharacterStream(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public Reader getNCharacterStream(final String columnLabel) throws SQLException {
        return null;
    }
    
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
    }
    
    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateClob(final int columnIndex, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateClob(final String columnLabel, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNClob(final int columnIndex, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public void updateNClob(final String columnLabel, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }
    
    @Override
    public <T> T getObject(final int columnIndex, final Class<T> type) throws SQLException {
        return null;
    }
    
    @Override
    public <T> T getObject(final String columnLabel, final Class<T> type) throws SQLException {
        return null;
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }
}
