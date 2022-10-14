// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.io.InputStream;
import java.sql.SQLType;
import java.io.Reader;
import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.RowId;
import java.net.URL;
import java.sql.Array;
import java.sql.Clob;
import java.sql.Blob;
import java.sql.Ref;
import java.util.Map;
import com.oceanbase.jdbc.internal.ColumnType;
import java.sql.Timestamp;
import java.sql.Time;
import java.util.Calendar;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.ParameterMetaData;
import com.oceanbase.jdbc.internal.com.read.resultset.SelectResultSet;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import java.sql.CallableStatement;

public abstract class CallableFunctionStatement extends ClientSidePreparedStatement implements CallableStatement
{
    protected CallableParameterMetaData parameterMetadata;
    private CallParameter[] params;
    protected String arguments;
    
    public CallableFunctionStatement(final OceanBaseConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency, final ExceptionFactory exceptionFactory) throws SQLException {
        super(connection, sql, resultSetType, resultSetConcurrency, 2, exceptionFactory);
        this.arguments = null;
    }
    
    @Override
    public CallableFunctionStatement clone(final OceanBaseConnection connection) throws CloneNotSupportedException {
        final CallableFunctionStatement clone = (CallableFunctionStatement)super.clone(connection);
        clone.params = this.params;
        clone.parameterMetadata = this.parameterMetadata;
        clone.connection = connection;
        return clone;
    }
    
    public void initFunctionData(final int parametersCount) {
        this.params = new CallParameter[parametersCount];
        for (int i = 0; i < parametersCount; ++i) {
            this.params[i] = new CallParameter();
            if (i > 0) {
                this.params[i].setInput(true);
            }
        }
        this.params[0].setOutput(true);
    }
    
    protected abstract SelectResultSet getResult() throws SQLException;
    
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        this.parameterMetadata.readMetadataFromDbIfRequired(this.originalSql, this.arguments, true);
        return this.parameterMetadata;
    }
    
    private int nameToIndex(final String parameterName) throws SQLException {
        this.parameterMetadata.readMetadataFromDbIfRequired(this.sqlQuery, this.arguments, true);
        for (int i = 1; i <= this.parameterMetadata.getParameterCount(); ++i) {
            String name = this.parameterMetadata.getName(i);
            if (!this.protocol.isOracleMode() && name != null) {
                name = name.replaceAll("\\`(\\w+)\\`", "$1");
            }
            if (name != null && name.equalsIgnoreCase(parameterName)) {
                return i;
            }
        }
        throw this.exceptionFactory.create("there is no parameter with the name " + parameterName);
    }
    
    private int nameToOutputIndex(final String parameterName) throws SQLException {
        for (int i = 0; i < this.parameterMetadata.getParameterCount(); ++i) {
            final String name = this.parameterMetadata.getName(i);
            if (name != null && name.equalsIgnoreCase(parameterName)) {
                return i;
            }
        }
        throw this.exceptionFactory.create("there is no parameter with the name " + parameterName);
    }
    
    private int indexToOutputIndex(final int parameterIndex) {
        return parameterIndex;
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return this.getResult().wasNull();
    }
    
    @Override
    public String getString(final int parameterIndex) throws SQLException {
        return this.getResult().getString(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public String getString(final String parameterName) throws SQLException {
        return this.getResult().getString(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public boolean getBoolean(final int parameterIndex) throws SQLException {
        return this.getResult().getBoolean(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public boolean getBoolean(final String parameterName) throws SQLException {
        return this.getResult().getBoolean(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public byte getByte(final int parameterIndex) throws SQLException {
        return this.getResult().getByte(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public byte getByte(final String parameterName) throws SQLException {
        return this.getResult().getByte(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public short getShort(final int parameterIndex) throws SQLException {
        return this.getResult().getShort(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public short getShort(final String parameterName) throws SQLException {
        return this.getResult().getShort(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public int getInt(final String parameterName) throws SQLException {
        return this.getResult().getInt(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public int getInt(final int parameterIndex) throws SQLException {
        return this.getResult().getInt(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public long getLong(final String parameterName) throws SQLException {
        return this.getResult().getLong(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public long getLong(final int parameterIndex) throws SQLException {
        return this.getResult().getLong(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public float getFloat(final String parameterName) throws SQLException {
        return this.getResult().getFloat(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public float getFloat(final int parameterIndex) throws SQLException {
        return this.getResult().getFloat(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public double getDouble(final int parameterIndex) throws SQLException {
        return this.getResult().getDouble(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public double getDouble(final String parameterName) throws SQLException {
        return this.getResult().getDouble(this.nameToOutputIndex(parameterName));
    }
    
    @Deprecated
    @Override
    public BigDecimal getBigDecimal(final int parameterIndex, final int scale) throws SQLException {
        return this.getResult().getBigDecimal(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public BigDecimal getBigDecimal(final int parameterIndex) throws SQLException {
        return this.getResult().getBigDecimal(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public BigDecimal getBigDecimal(final String parameterName) throws SQLException {
        return this.getResult().getBigDecimal(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public byte[] getBytes(final String parameterName) throws SQLException {
        return this.getResult().getBytes(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public byte[] getBytes(final int parameterIndex) throws SQLException {
        return this.getResult().getBytes(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Date getDate(final int parameterIndex) throws SQLException {
        return this.getResult().getDate(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Date getDate(final String parameterName) throws SQLException {
        return this.getResult().getDate(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Date getDate(final String parameterName, final Calendar cal) throws SQLException {
        return this.getResult().getDate(this.nameToOutputIndex(parameterName), cal);
    }
    
    @Override
    public Date getDate(final int parameterIndex, final Calendar cal) throws SQLException {
        return this.getResult().getDate(parameterIndex, cal);
    }
    
    @Override
    public Time getTime(final int parameterIndex, final Calendar cal) throws SQLException {
        return this.getResult().getTime(this.indexToOutputIndex(parameterIndex), cal);
    }
    
    @Override
    public Time getTime(final String parameterName) throws SQLException {
        return this.getResult().getTime(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Time getTime(final String parameterName, final Calendar cal) throws SQLException {
        return this.getResult().getTime(this.nameToOutputIndex(parameterName), cal);
    }
    
    @Override
    public Time getTime(final int parameterIndex) throws SQLException {
        return this.getResult().getTime(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Timestamp getTimestamp(final int parameterIndex) throws SQLException {
        return this.getResult().getTimestamp(parameterIndex);
    }
    
    @Override
    public Timestamp getTimestamp(final int parameterIndex, final Calendar cal) throws SQLException {
        return this.getResult().getTimestamp(this.indexToOutputIndex(parameterIndex), cal);
    }
    
    @Override
    public Timestamp getTimestamp(final String parameterName) throws SQLException {
        return this.getResult().getTimestamp(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Timestamp getTimestamp(final String parameterName, final Calendar cal) throws SQLException {
        return this.getResult().getTimestamp(this.nameToOutputIndex(parameterName), cal);
    }
    
    @Override
    public Object getObject(final int parameterIndex) throws SQLException {
        final Class<?> classType = (Class<?>)ColumnType.classFromJavaType(this.getParameter(parameterIndex).getOutputSqlType());
        if (classType != null) {
            return this.getResult().getObject(this.indexToOutputIndex(parameterIndex), classType);
        }
        if (this.getResult().getProtocol() == null) {
            this.getResult().setProtocol(this.protocol);
        }
        return this.getResult().getObject(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Object getObject(final String parameterName) throws SQLException {
        final int index = this.nameToIndex(parameterName);
        final Class<?> classType = (Class<?>)ColumnType.classFromJavaType(this.getParameter(index).getOutputSqlType());
        if (classType != null) {
            return this.getResult().getObject(this.indexToOutputIndex(index), classType);
        }
        return this.getResult().getObject(this.indexToOutputIndex(index));
    }
    
    @Override
    public Object getObject(final int parameterIndex, final Map<String, Class<?>> map) throws SQLException {
        return this.getResult().getObject(this.indexToOutputIndex(parameterIndex), map);
    }
    
    @Override
    public Object getObject(final String parameterName, final Map<String, Class<?>> map) throws SQLException {
        return this.getResult().getObject(this.nameToOutputIndex(parameterName), map);
    }
    
    @Override
    public <T> T getObject(final int parameterIndex, final Class<T> type) throws SQLException {
        return this.getResult().getObject(this.indexToOutputIndex(parameterIndex), type);
    }
    
    @Override
    public <T> T getObject(final String parameterName, final Class<T> type) throws SQLException {
        return this.getResult().getObject(this.nameToOutputIndex(parameterName), type);
    }
    
    @Override
    public Ref getRef(final int parameterIndex) throws SQLException {
        return this.getResult().getRef(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Ref getRef(final String parameterName) throws SQLException {
        return this.getResult().getRef(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Blob getBlob(final int parameterIndex) throws SQLException {
        return this.getResult().getBlob(parameterIndex);
    }
    
    @Override
    public Blob getBlob(final String parameterName) throws SQLException {
        return this.getResult().getBlob(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Clob getClob(final String parameterName) throws SQLException {
        return this.getResult().getClob(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Clob getClob(final int parameterIndex) throws SQLException {
        return this.getResult().getClob(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Array getArray(final String parameterName) throws SQLException {
        return this.getResult().getArray(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Array getArray(final int parameterIndex) throws SQLException {
        return this.getResult().getArray(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public URL getURL(final int parameterIndex) throws SQLException {
        return this.getResult().getURL(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public URL getURL(final String parameterName) throws SQLException {
        return this.getResult().getURL(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public RowId getRowId(final int parameterIndex) throws SQLException {
        throw this.exceptionFactory.notSupported("RowIDs not supported");
    }
    
    @Override
    public RowId getRowId(final String parameterName) throws SQLException {
        throw this.exceptionFactory.notSupported("RowIDs not supported");
    }
    
    @Override
    public NClob getNClob(final int parameterIndex) throws SQLException {
        return this.getResult().getNClob(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public NClob getNClob(final String parameterName) throws SQLException {
        return this.getResult().getNClob(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public SQLXML getSQLXML(final int parameterIndex) throws SQLException {
        throw this.exceptionFactory.notSupported("SQLXML not supported");
    }
    
    @Override
    public SQLXML getSQLXML(final String parameterName) throws SQLException {
        throw this.exceptionFactory.notSupported("SQLXML not supported");
    }
    
    @Override
    public String getNString(final int parameterIndex) throws SQLException {
        return this.getResult().getNString(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public String getNString(final String parameterName) throws SQLException {
        return this.getResult().getNString(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Reader getNCharacterStream(final int parameterIndex) throws SQLException {
        return this.getResult().getNCharacterStream(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Reader getNCharacterStream(final String parameterName) throws SQLException {
        return this.getResult().getNCharacterStream(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Reader getCharacterStream(final int parameterIndex) throws SQLException {
        return this.getResult().getCharacterStream(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Reader getCharacterStream(final String parameterName) throws SQLException {
        return this.getResult().getCharacterStream(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public void registerOutParameter(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
        final CallParameter callParameter = this.getParameter(parameterIndex);
        callParameter.setOutputSqlType(sqlType);
        callParameter.setTypeName(typeName);
        callParameter.setOutput(true);
    }
    
    @Override
    public void registerOutParameter(final int parameterIndex, final int sqlType) throws SQLException {
        this.registerOutParameter(parameterIndex, sqlType, -1);
    }
    
    @Override
    public void registerOutParameter(final int parameterIndex, final int sqlType, final int scale) throws SQLException {
        final CallParameter callParameter = this.getParameter(parameterIndex);
        callParameter.setOutput(true);
        callParameter.setOutputSqlType(sqlType);
        callParameter.setScale(scale);
    }
    
    @Override
    public void registerOutParameter(final String parameterName, final int sqlType) throws SQLException {
        this.registerOutParameter(this.nameToIndex(parameterName), sqlType);
    }
    
    @Override
    public void registerOutParameter(final String parameterName, final int sqlType, final int scale) throws SQLException {
        this.registerOutParameter(this.nameToIndex(parameterName), sqlType, scale);
    }
    
    @Override
    public void registerOutParameter(final String parameterName, final int sqlType, final String typeName) throws SQLException {
        this.registerOutParameter(this.nameToIndex(parameterName), sqlType, typeName);
    }
    
    @Override
    public void registerOutParameter(final int parameterIndex, final SQLType sqlType) throws SQLException {
        this.registerOutParameter(parameterIndex, sqlType.getVendorTypeNumber());
    }
    
    @Override
    public void registerOutParameter(final int parameterIndex, final SQLType sqlType, final int scale) throws SQLException {
        this.registerOutParameter(parameterIndex, sqlType.getVendorTypeNumber(), scale);
    }
    
    @Override
    public void registerOutParameter(final int parameterIndex, final SQLType sqlType, final String typeName) throws SQLException {
        this.registerOutParameter(parameterIndex, sqlType.getVendorTypeNumber(), typeName);
    }
    
    @Override
    public void registerOutParameter(final String parameterName, final SQLType sqlType) throws SQLException {
        this.registerOutParameter(parameterName, sqlType.getVendorTypeNumber());
    }
    
    @Override
    public void registerOutParameter(final String parameterName, final SQLType sqlType, final int scale) throws SQLException {
        this.registerOutParameter(parameterName, sqlType.getVendorTypeNumber(), scale);
    }
    
    @Override
    public void registerOutParameter(final String parameterName, final SQLType sqlType, final String typeName) throws SQLException {
        this.registerOutParameter(parameterName, sqlType.getVendorTypeNumber(), typeName);
    }
    
    private CallParameter getParameter(final int index) throws SQLException {
        if (index > this.params.length || index <= 0) {
            throw this.exceptionFactory.create("No parameter with index " + index);
        }
        return this.params[index - 1];
    }
    
    @Override
    public void setSQLXML(final String parameterName, final SQLXML xmlObject) throws SQLException {
        throw this.exceptionFactory.notSupported("SQLXML not supported");
    }
    
    @Override
    public void setRowId(final String parameterName, final RowId rowid) throws SQLException {
        throw this.exceptionFactory.notSupported("RowIDs not supported");
    }
    
    @Override
    public void setNString(final String parameterName, final String value) throws SQLException {
        this.setNString(this.nameToIndex(parameterName), value);
    }
    
    @Override
    public void setNCharacterStream(final String parameterName, final Reader reader, final long length) throws SQLException {
        this.setCharacterStream(this.nameToIndex(parameterName), reader, length);
    }
    
    @Override
    public void setNCharacterStream(final String parameterName, final Reader reader) throws SQLException {
        this.setCharacterStream(this.nameToIndex(parameterName), reader);
    }
    
    @Override
    public void setNClob(final String parameterName, final NClob value) throws SQLException {
        this.setClob(this.nameToIndex(parameterName), value);
    }
    
    @Override
    public void setNClob(final String parameterName, final Reader reader, final long length) throws SQLException {
        this.setClob(this.nameToIndex(parameterName), reader, length);
    }
    
    @Override
    public void setNClob(final String parameterName, final Reader reader) throws SQLException {
        this.setClob(this.nameToIndex(parameterName), reader);
    }
    
    @Override
    public void setClob(final String parameterName, final Reader reader, final long length) throws SQLException {
        this.setClob(this.nameToIndex(parameterName), reader, length);
    }
    
    @Override
    public void setClob(final String parameterName, final Clob clob) throws SQLException {
        this.setClob(this.nameToIndex(parameterName), clob);
    }
    
    @Override
    public void setClob(final String parameterName, final Reader reader) throws SQLException {
        this.setClob(this.nameToIndex(parameterName), reader);
    }
    
    @Override
    public void setBlob(final String parameterName, final InputStream inputStream, final long length) throws SQLException {
        this.setBlob(this.nameToIndex(parameterName), inputStream, length);
    }
    
    @Override
    public void setBlob(final String parameterName, final Blob blob) throws SQLException {
        this.setBlob(this.nameToIndex(parameterName), blob);
    }
    
    @Override
    public void setBlob(final String parameterName, final InputStream inputStream) throws SQLException {
        this.setBlob(this.nameToIndex(parameterName), inputStream);
    }
    
    @Override
    public void setAsciiStream(final String parameterName, final InputStream inputStream, final long length) throws SQLException {
        this.setAsciiStream(this.nameToIndex(parameterName), inputStream, length);
    }
    
    @Override
    public void setAsciiStream(final String parameterName, final InputStream inputStream, final int length) throws SQLException {
        this.setAsciiStream(this.nameToIndex(parameterName), inputStream, length);
    }
    
    @Override
    public void setAsciiStream(final String parameterName, final InputStream inputStream) throws SQLException {
        this.setAsciiStream(this.nameToIndex(parameterName), inputStream);
    }
    
    @Override
    public void setBinaryStream(final String parameterName, final InputStream inputStream, final long length) throws SQLException {
        this.setBinaryStream(this.nameToIndex(parameterName), inputStream, length);
    }
    
    @Override
    public void setBinaryStream(final String parameterName, final InputStream inputStream) throws SQLException {
        this.setBinaryStream(this.nameToIndex(parameterName), inputStream);
    }
    
    @Override
    public void setBinaryStream(final String parameterName, final InputStream inputStream, final int length) throws SQLException {
        this.setBinaryStream(this.nameToIndex(parameterName), inputStream, length);
    }
    
    @Override
    public void setCharacterStream(final String parameterName, final Reader reader, final long length) throws SQLException {
        this.setCharacterStream(this.nameToIndex(parameterName), reader, length);
    }
    
    @Override
    public void setCharacterStream(final String parameterName, final Reader reader) throws SQLException {
        this.setCharacterStream(this.nameToIndex(parameterName), reader);
    }
    
    @Override
    public void setCharacterStream(final String parameterName, final Reader reader, final int length) throws SQLException {
        this.setCharacterStream(this.nameToIndex(parameterName), reader, length);
    }
    
    @Override
    public void setURL(final String parameterName, final URL url) throws SQLException {
        this.setURL(this.nameToIndex(parameterName), url);
    }
    
    @Override
    public void setNull(final String parameterName, final int sqlType) throws SQLException {
        this.setNull(this.nameToIndex(parameterName), sqlType);
    }
    
    @Override
    public void setNull(final String parameterName, final int sqlType, final String typeName) throws SQLException {
        this.setNull(this.nameToIndex(parameterName), sqlType, typeName);
    }
    
    @Override
    public void setBoolean(final String parameterName, final boolean booleanValue) throws SQLException {
        this.setBoolean(this.nameToIndex(parameterName), booleanValue);
    }
    
    @Override
    public void setByte(final String parameterName, final byte byteValue) throws SQLException {
        this.setByte(this.nameToIndex(parameterName), byteValue);
    }
    
    @Override
    public void setShort(final String parameterName, final short shortValue) throws SQLException {
        this.setShort(this.nameToIndex(parameterName), shortValue);
    }
    
    @Override
    public void setInt(final String parameterName, final int intValue) throws SQLException {
        this.setInt(this.nameToIndex(parameterName), intValue);
    }
    
    @Override
    public void setLong(final String parameterName, final long longValue) throws SQLException {
        this.setLong(this.nameToIndex(parameterName), longValue);
    }
    
    @Override
    public void setFloat(final String parameterName, final float floatValue) throws SQLException {
        this.setFloat(this.nameToIndex(parameterName), floatValue);
    }
    
    @Override
    public void setDouble(final String parameterName, final double doubleValue) throws SQLException {
        this.setDouble(this.nameToIndex(parameterName), doubleValue);
    }
    
    @Override
    public void setBigDecimal(final String parameterName, final BigDecimal bigDecimal) throws SQLException {
        this.setBigDecimal(this.nameToIndex(parameterName), bigDecimal);
    }
    
    @Override
    public void setString(final String parameterName, final String stringValue) throws SQLException {
        this.setString(this.nameToIndex(parameterName), stringValue);
    }
    
    @Override
    public void setBytes(final String parameterName, final byte[] bytes) throws SQLException {
        this.setBytes(this.nameToIndex(parameterName), bytes);
    }
    
    @Override
    public void setDate(final String parameterName, final Date date) throws SQLException {
        this.setDate(this.nameToIndex(parameterName), date);
    }
    
    @Override
    public void setDate(final String parameterName, final Date date, final Calendar cal) throws SQLException {
        this.setDate(this.nameToIndex(parameterName), date, cal);
    }
    
    @Override
    public void setTime(final String parameterName, final Time time) throws SQLException {
        this.setTime(this.nameToIndex(parameterName), time);
    }
    
    @Override
    public void setTime(final String parameterName, final Time time, final Calendar cal) throws SQLException {
        this.setTime(this.nameToIndex(parameterName), time, cal);
    }
    
    @Override
    public void setTimestamp(final String parameterName, final Timestamp timestamp) throws SQLException {
        this.setTimestamp(this.nameToIndex(parameterName), timestamp);
    }
    
    @Override
    public void setTimestamp(final String parameterName, final Timestamp timestamp, final Calendar cal) throws SQLException {
        this.setTimestamp(this.nameToIndex(parameterName), timestamp, cal);
    }
    
    @Override
    public void setObject(final String parameterName, final Object obj, final int targetSqlType, final int scale) throws SQLException {
        this.setObject(this.nameToIndex(parameterName), obj, targetSqlType, scale);
    }
    
    @Override
    public void setObject(final String parameterName, final Object obj, final int targetSqlType) throws SQLException {
        this.setObject(this.nameToIndex(parameterName), obj, targetSqlType);
    }
    
    @Override
    public void setObject(final String parameterName, final Object obj) throws SQLException {
        this.setObject(this.nameToIndex(parameterName), obj);
    }
    
    @Override
    public void setObject(final String parameterName, final Object obj, final SQLType targetSqlType, final int scaleOrLength) throws SQLException {
        this.setObject(this.nameToIndex(parameterName), obj, targetSqlType.getVendorTypeNumber(), scaleOrLength);
    }
    
    @Override
    public void setObject(final String parameterName, final Object obj, final SQLType targetSqlType) throws SQLException {
        this.setObject(this.nameToIndex(parameterName), obj, targetSqlType.getVendorTypeNumber());
    }
}
