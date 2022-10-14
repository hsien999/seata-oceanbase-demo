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
import com.oceanbase.jdbc.internal.ColumnType;
import java.util.Map;
import java.sql.Timestamp;
import java.sql.Time;
import java.util.Calendar;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.ParameterMetaData;
import com.oceanbase.jdbc.internal.com.read.resultset.SelectResultSet;
import java.util.Iterator;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import java.util.List;
import java.sql.CallableStatement;

public abstract class CallableProcedureStatement extends ServerSidePreparedStatement implements CallableStatement, Cloneable
{
    protected List<CallParameter> params;
    protected boolean isObFunction;
    protected int[] outputParameterMapper;
    protected CallableParameterMetaData parameterMetadata;
    protected boolean hasInOutParameters;
    protected String arguments;
    
    public CallableProcedureStatement(final boolean isObFuction, final OceanBaseConnection connection, final String sql, final int resultSetScrollType, final int resultSetConcurrency, final ExceptionFactory exceptionFactory) throws SQLException {
        super(isObFuction, connection, sql, resultSetScrollType, resultSetConcurrency, 2, exceptionFactory);
        this.isObFunction = false;
        this.outputParameterMapper = null;
        this.arguments = null;
        this.isObFunction = isObFuction;
    }
    
    @Override
    public CallableProcedureStatement clone(final OceanBaseConnection connection) throws CloneNotSupportedException {
        final CallableProcedureStatement clone = (CallableProcedureStatement)super.clone(connection);
        clone.params = this.params;
        clone.parameterMetadata = this.parameterMetadata;
        clone.hasInOutParameters = this.hasInOutParameters;
        clone.outputParameterMapper = this.outputParameterMapper;
        return clone;
    }
    
    public void setParametersVariables() {
        this.hasInOutParameters = false;
        for (final CallParameter param : this.params) {
            if (param != null && param.isOutput() && param.isInput()) {
                this.hasInOutParameters = true;
                break;
            }
        }
    }
    
    protected abstract SelectResultSet getOutputResult() throws SQLException;
    
    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        this.parameterMetadata.readMetadataFromDbIfRequired();
        return this.parameterMetadata;
    }
    
    private int nameToIndex(final String parameterName) throws SQLException {
        this.parameterMetadata.readMetadataFromDbIfRequired(this.originalSql, this.arguments, this.isObFunction);
        for (int i = 1; i <= this.parameterMetadata.getParameterCount(); ++i) {
            String name = this.parameterMetadata.getName(i);
            if (!this.protocol.isOracleMode()) {
                name = name.replaceAll("\\`(\\w+)\\`", "$1");
            }
            if (name != null && name.equalsIgnoreCase(parameterName)) {
                return i;
            }
        }
        throw new SQLException("there is no parameter with the name " + parameterName);
    }
    
    private int nameToOutputIndex(final String parameterName) throws SQLException {
        this.parameterMetadata.readMetadataFromDbIfRequired();
        int i = 0;
        while (i < this.parameterMetadata.getParameterCount()) {
            String name = this.parameterMetadata.getName(i + 1);
            if (!this.protocol.isOracleMode()) {
                name = name.replaceAll("\\`(\\w+)\\`", "$1");
            }
            if (name != null && name.equalsIgnoreCase(parameterName)) {
                if (this.outputParameterMapper[i] == -1) {
                    throw new SQLException("Parameter '" + parameterName + "' is not declared as output parameter with method registerOutParameter");
                }
                return this.outputParameterMapper[i];
            }
            else {
                ++i;
            }
        }
        throw new SQLException("there is no parameter with the name " + parameterName);
    }
    
    private int indexToOutputIndex(final int parameterIndex) throws SQLException {
        try {
            if (this.isObFunction && !this.protocol.isOracleMode()) {
                return 1;
            }
            if (this.outputParameterMapper[parameterIndex - 1] == -1) {
                throw new SQLException("Parameter in index '" + parameterIndex + "' is not declared as output parameter with method registerOutParameter");
            }
            return this.outputParameterMapper[parameterIndex - 1];
        }
        catch (ArrayIndexOutOfBoundsException arrayIndexOutOfBoundsException) {
            if (parameterIndex < 1) {
                throw new SQLException("Index " + parameterIndex + " must at minimum be 1");
            }
            throw new SQLException("Index value '" + parameterIndex + "' is incorrect. Maximum value is " + this.params.size());
        }
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return this.getOutputResult().wasNull();
    }
    
    @Override
    public String getString(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getString(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public String getString(final String parameterName) throws SQLException {
        return this.getOutputResult().getString(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public boolean getBoolean(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getBoolean(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public boolean getBoolean(final String parameterName) throws SQLException {
        return this.getOutputResult().getBoolean(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public byte getByte(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getByte(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public byte getByte(final String parameterName) throws SQLException {
        return this.getOutputResult().getByte(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public short getShort(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getShort(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public short getShort(final String parameterName) throws SQLException {
        return this.getOutputResult().getShort(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public int getInt(final String parameterName) throws SQLException {
        return this.getOutputResult().getInt(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public int getInt(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getInt(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public long getLong(final String parameterName) throws SQLException {
        return this.getOutputResult().getLong(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public long getLong(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getLong(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public float getFloat(final String parameterName) throws SQLException {
        return this.getOutputResult().getFloat(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public float getFloat(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getFloat(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public double getDouble(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getDouble(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public double getDouble(final String parameterName) throws SQLException {
        return this.getOutputResult().getDouble(this.nameToOutputIndex(parameterName));
    }
    
    @Deprecated
    @Override
    public BigDecimal getBigDecimal(final int parameterIndex, final int scale) throws SQLException {
        return this.getOutputResult().getBigDecimal(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public BigDecimal getBigDecimal(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getBigDecimal(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public BigDecimal getBigDecimal(final String parameterName) throws SQLException {
        return this.getOutputResult().getBigDecimal(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public byte[] getBytes(final String parameterName) throws SQLException {
        return this.getOutputResult().getBytes(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public byte[] getBytes(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getBytes(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Date getDate(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getDate(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Date getDate(final String parameterName) throws SQLException {
        return this.getOutputResult().getDate(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Date getDate(final String parameterName, final Calendar cal) throws SQLException {
        return this.getOutputResult().getDate(this.nameToOutputIndex(parameterName), cal);
    }
    
    @Override
    public Date getDate(final int parameterIndex, final Calendar cal) throws SQLException {
        return this.getOutputResult().getDate(this.indexToOutputIndex(parameterIndex), cal);
    }
    
    @Override
    public Time getTime(final int parameterIndex, final Calendar cal) throws SQLException {
        return this.getOutputResult().getTime(this.indexToOutputIndex(parameterIndex), cal);
    }
    
    @Override
    public Time getTime(final String parameterName) throws SQLException {
        return this.getOutputResult().getTime(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Time getTime(final String parameterName, final Calendar cal) throws SQLException {
        return this.getOutputResult().getTime(this.nameToOutputIndex(parameterName), cal);
    }
    
    @Override
    public Time getTime(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getTime(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Timestamp getTimestamp(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getTimestamp(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Timestamp getTimestamp(final int parameterIndex, final Calendar cal) throws SQLException {
        return this.getOutputResult().getTimestamp(this.indexToOutputIndex(parameterIndex), cal);
    }
    
    @Override
    public Timestamp getTimestamp(final String parameterName) throws SQLException {
        return this.getOutputResult().getTimestamp(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Timestamp getTimestamp(final String parameterName, final Calendar cal) throws SQLException {
        return this.getOutputResult().getTimestamp(this.nameToOutputIndex(parameterName), cal);
    }
    
    @Override
    public Object getObject(final int parameterIndex, final Map<String, Class<?>> map) throws SQLException {
        return this.getOutputResult().getObject(this.indexToOutputIndex(parameterIndex), map);
    }
    
    @Override
    public Object getObject(final int parameterIndex) throws SQLException {
        final Class<?> classType = (Class<?>)ColumnType.classFromJavaType(this.getParameter(parameterIndex).getOutputSqlType());
        if (classType != null) {
            return this.getOutputResult().getObject(this.indexToOutputIndex(parameterIndex), classType);
        }
        return this.getOutputResult().getObject(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Object getObject(final String parameterName) throws SQLException {
        final int index = this.nameToIndex(parameterName);
        final Class<?> classType = (Class<?>)ColumnType.classFromJavaType(this.getParameter(index).getOutputSqlType());
        if (classType != null) {
            return this.getOutputResult().getObject(this.indexToOutputIndex(index), classType);
        }
        return this.getOutputResult().getObject(this.indexToOutputIndex(index));
    }
    
    @Override
    public Object getObject(final String parameterName, final Map<String, Class<?>> map) throws SQLException {
        return this.getOutputResult().getObject(this.nameToOutputIndex(parameterName), map);
    }
    
    @Override
    public <T> T getObject(final int parameterIndex, final Class<T> type) throws SQLException {
        return this.getOutputResult().getObject(this.indexToOutputIndex(parameterIndex), type);
    }
    
    @Override
    public <T> T getObject(final String parameterName, final Class<T> type) throws SQLException {
        return this.getOutputResult().getObject(this.nameToOutputIndex(parameterName), type);
    }
    
    @Override
    public Ref getRef(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getRef(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Ref getRef(final String parameterName) throws SQLException {
        return this.getOutputResult().getRef(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Blob getBlob(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getBlob(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Blob getBlob(final String parameterName) throws SQLException {
        return this.getOutputResult().getBlob(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Clob getClob(final String parameterName) throws SQLException {
        return this.getOutputResult().getClob(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Clob getClob(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getClob(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Array getArray(final String parameterName) throws SQLException {
        return this.getOutputResult().getArray(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Array getArray(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getArray(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public URL getURL(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getURL(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public URL getURL(final String parameterName) throws SQLException {
        return this.getOutputResult().getURL(this.nameToOutputIndex(parameterName));
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
        return this.getOutputResult().getNClob(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public NClob getNClob(final String parameterName) throws SQLException {
        return this.getOutputResult().getNClob(this.nameToOutputIndex(parameterName));
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
        return this.getOutputResult().getString(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public String getNString(final String parameterName) throws SQLException {
        return this.getOutputResult().getString(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Reader getNCharacterStream(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getCharacterStream(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Reader getNCharacterStream(final String parameterName) throws SQLException {
        return this.getOutputResult().getCharacterStream(this.nameToOutputIndex(parameterName));
    }
    
    @Override
    public Reader getCharacterStream(final int parameterIndex) throws SQLException {
        return this.getOutputResult().getCharacterStream(this.indexToOutputIndex(parameterIndex));
    }
    
    @Override
    public Reader getCharacterStream(final String parameterName) throws SQLException {
        return this.getOutputResult().getCharacterStream(this.nameToOutputIndex(parameterName));
    }
    
    private void checkIsOutputParam(final int paramIndex) throws SQLException {
        if (this.isObFunction && paramIndex == 1) {
            return;
        }
        final CallParameter param = this.params.get(paramIndex - 1);
        if (!param.isOutput()) {
            throw new SQLException("Parameter number " + paramIndex + "is not an OUT parameter");
        }
    }
    
    @Override
    public void registerOutParameter(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
        if (this.isObFunction && parameterIndex == 1) {
            return;
        }
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
        try {
            final CallParameter callParameter = this.getParameter(parameterIndex);
            callParameter.setOutput(true);
            callParameter.setOutputSqlType(sqlType);
            callParameter.setScale(scale);
        }
        catch (SQLException e) {
            if (!this.protocol.supportStmtPrepareExecute() || 1 > parameterIndex || parameterIndex > this.parameterCount) {
                throw e;
            }
            final CallParameter paramInfoToAdd = new CallParameter("functionreturn", parameterIndex, false, true, sqlType, null, 0, scale, 0, 5);
            this.params.add(paramInfoToAdd);
        }
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
        if (index > this.params.size() || index <= 0) {
            throw new SQLException("No parameter with index " + index);
        }
        return this.params.get(index - 1);
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
        this.setString(this.nameToIndex(parameterName), value);
    }
    
    @Override
    public void setNCharacterStream(final String parameterName, final Reader value, final long length) throws SQLException {
        this.setCharacterStream(this.nameToIndex(parameterName), value, length);
    }
    
    @Override
    public void setNCharacterStream(final String parameterName, final Reader value) throws SQLException {
        this.setCharacterStream(this.nameToIndex(parameterName), value);
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
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        super.setNull(this.checkAndMinusForObFunction(parameterIndex), sqlType);
    }
    
    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        super.setBoolean(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        super.setByte(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        super.setShort(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        super.setInt(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        super.setLong(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        super.setFloat(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        super.setDouble(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        super.setBigDecimal(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setString(final int parameterIndex, final String x) throws SQLException {
        super.setString(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        super.setBytes(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setDate(final int parameterIndex, final Date x) throws SQLException {
        super.setDate(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x) throws SQLException {
        super.setTime(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        super.setTimestamp(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        super.setAsciiStream(this.checkAndMinusForObFunction(parameterIndex), x, length);
    }
    
    @Override
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        super.setUnicodeStream(this.checkAndMinusForObFunction(parameterIndex), x, length);
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        super.setBinaryStream(this.checkAndMinusForObFunction(parameterIndex), x, length);
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        super.setObject(this.checkAndMinusForObFunction(parameterIndex), x, targetSqlType);
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x) throws SQLException {
        super.setObject(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
        super.setCharacterStream(this.checkAndMinusForObFunction(parameterIndex), reader, length);
    }
    
    @Override
    public void setRef(final int parameterIndex, final Ref x) throws SQLException {
        super.setRef(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setBlob(final int parameterIndex, final Blob x) throws SQLException {
        super.setBlob(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setClob(final int parameterIndex, final Clob x) throws SQLException {
        super.setClob(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setArray(final int parameterIndex, final Array x) throws SQLException {
        super.setArray(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
        super.setDate(this.checkAndMinusForObFunction(parameterIndex), x, cal);
    }
    
    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        super.setTime(this.checkAndMinusForObFunction(parameterIndex), x, cal);
    }
    
    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        super.setTimestamp(this.checkAndMinusForObFunction(parameterIndex), x, cal);
    }
    
    @Override
    public void setNull(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
        super.setNull(this.checkAndMinusForObFunction(parameterIndex), sqlType, typeName);
    }
    
    @Override
    public void setURL(final int parameterIndex, final URL x) throws SQLException {
        super.setURL(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setRowId(final int parameterIndex, final RowId x) throws SQLException {
        super.setRowId(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setNString(final int parameterIndex, final String value) throws SQLException {
        super.setNString(this.checkAndMinusForObFunction(parameterIndex), value);
    }
    
    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value, final long length) throws SQLException {
        super.setNCharacterStream(this.checkAndMinusForObFunction(parameterIndex), value, length);
    }
    
    @Override
    public void setNClob(final int parameterIndex, final NClob value) throws SQLException {
        super.setNClob(this.checkAndMinusForObFunction(parameterIndex), value);
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        super.setClob(this.checkAndMinusForObFunction(parameterIndex), reader, length);
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream, final long length) throws SQLException {
        super.setBlob(this.checkAndMinusForObFunction(parameterIndex), inputStream, length);
    }
    
    @Override
    public void setNClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        super.setNClob(this.checkAndMinusForObFunction(parameterIndex), reader, length);
    }
    
    @Override
    public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) throws SQLException {
        super.setSQLXML(this.checkAndMinusForObFunction(parameterIndex), xmlObject);
    }
    
    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength) throws SQLException {
        super.setObject(this.checkAndMinusForObFunction(parameterIndex), x, targetSqlType, scaleOrLength);
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        super.setAsciiStream(this.checkAndMinusForObFunction(parameterIndex), x, length);
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
        super.setBinaryStream(this.checkAndMinusForObFunction(parameterIndex), x, length);
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final long length) throws SQLException {
        super.setCharacterStream(this.checkAndMinusForObFunction(parameterIndex), reader, length);
    }
    
    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x) throws SQLException {
        super.setAsciiStream(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x) throws SQLException {
        super.setBinaryStream(this.checkAndMinusForObFunction(parameterIndex), x);
    }
    
    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader) throws SQLException {
        super.setCharacterStream(this.checkAndMinusForObFunction(parameterIndex), reader);
    }
    
    @Override
    public void setNCharacterStream(final int parameterIndex, final Reader value) throws SQLException {
        super.setNCharacterStream(this.checkAndMinusForObFunction(parameterIndex), value);
    }
    
    @Override
    public void setClob(final int parameterIndex, final Reader reader) throws SQLException {
        super.setClob(this.checkAndMinusForObFunction(parameterIndex), reader);
    }
    
    @Override
    public void setBlob(final int parameterIndex, final InputStream inputStream) throws SQLException {
        super.setBlob(this.checkAndMinusForObFunction(parameterIndex), inputStream);
    }
    
    @Override
    public void setNClob(final int parameterIndex, final Reader reader) throws SQLException {
        super.setNClob(this.checkAndMinusForObFunction(parameterIndex), reader);
    }
    
    private int checkAndMinusForObFunction(final int parameterIndex) {
        return parameterIndex;
    }
}
