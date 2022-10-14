// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdbc2.optional;

import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.RowId;
import java.io.Reader;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.sql.Array;
import java.sql.Clob;
import java.sql.Blob;
import java.sql.Ref;
import java.util.Map;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;
import java.math.BigDecimal;
import com.alipay.oceanbase.jdbc.SQLError;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.Util;
import java.lang.reflect.Constructor;
import java.sql.CallableStatement;

public class CallableStatementWrapper extends PreparedStatementWrapper implements CallableStatement
{
    private static final Constructor<?> JDBC_4_CALLABLE_STATEMENT_WRAPPER_CTOR;
    
    protected static CallableStatementWrapper getInstance(final ConnectionWrapper c, final MysqlPooledConnection conn, final CallableStatement toWrap) throws SQLException {
        if (!Util.isJdbc4()) {
            return new CallableStatementWrapper(c, conn, toWrap);
        }
        return (CallableStatementWrapper)Util.handleNewInstance(CallableStatementWrapper.JDBC_4_CALLABLE_STATEMENT_WRAPPER_CTOR, new Object[] { c, conn, toWrap }, conn.getExceptionInterceptor());
    }
    
    public CallableStatementWrapper(final ConnectionWrapper c, final MysqlPooledConnection conn, final CallableStatement toWrap) {
        super(c, conn, toWrap);
    }
    
    @Override
    public void registerOutParameter(final int parameterIndex, final int sqlType) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterIndex, sqlType);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void registerOutParameter(final int parameterIndex, final int sqlType, final int scale) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterIndex, sqlType, scale);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).wasNull();
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }
    
    @Override
    public String getString(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getString(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public boolean getBoolean(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBoolean(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }
    
    @Override
    public byte getByte(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getByte(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }
    
    @Override
    public short getShort(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getShort(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }
    
    @Override
    public int getInt(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getInt(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }
    
    @Override
    public long getLong(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getLong(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0L;
        }
    }
    
    @Override
    public float getFloat(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getFloat(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0.0f;
        }
    }
    
    @Override
    public double getDouble(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getDouble(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0.0;
        }
    }
    
    @Deprecated
    @Override
    public BigDecimal getBigDecimal(final int parameterIndex, final int scale) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBigDecimal(parameterIndex, scale);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public byte[] getBytes(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBytes(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Date getDate(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getDate(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Time getTime(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTime(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Timestamp getTimestamp(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTimestamp(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Object getObject(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getObject(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public BigDecimal getBigDecimal(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBigDecimal(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Object getObject(final int parameterIndex, final Map<String, Class<?>> typeMap) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getObject(parameterIndex, typeMap);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Ref getRef(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getRef(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Blob getBlob(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBlob(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Clob getClob(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getClob(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Array getArray(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getArray(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Date getDate(final int parameterIndex, final Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getDate(parameterIndex, cal);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Time getTime(final int parameterIndex, final Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTime(parameterIndex, cal);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Timestamp getTimestamp(final int parameterIndex, final Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTimestamp(parameterIndex, cal);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public void registerOutParameter(final int paramIndex, final int sqlType, final String typeName) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).registerOutParameter(paramIndex, sqlType, typeName);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void registerOutParameter(final String parameterName, final int sqlType) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterName, sqlType);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void registerOutParameter(final String parameterName, final int sqlType, final int scale) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterName, sqlType, scale);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void registerOutParameter(final String parameterName, final int sqlType, final String typeName) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).registerOutParameter(parameterName, sqlType, typeName);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public URL getURL(final int parameterIndex) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getURL(parameterIndex);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public void setURL(final String parameterName, final URL val) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setURL(parameterName, val);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setNull(final String parameterName, final int sqlType) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setNull(parameterName, sqlType);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setBoolean(final String parameterName, final boolean x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setBoolean(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setByte(final String parameterName, final byte x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setByte(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setShort(final String parameterName, final short x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setShort(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setInt(final String parameterName, final int x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setInt(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setLong(final String parameterName, final long x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setLong(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setFloat(final String parameterName, final float x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setFloat(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setDouble(final String parameterName, final double x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setDouble(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setBigDecimal(final String parameterName, final BigDecimal x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setBigDecimal(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setString(final String parameterName, final String x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setString(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setBytes(final String parameterName, final byte[] x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setBytes(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setDate(final String parameterName, final Date x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setDate(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setTime(final String parameterName, final Time x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setTime(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setTimestamp(final String parameterName, final Timestamp x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setTimestamp(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setAsciiStream(final String parameterName, final InputStream x, final int length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setAsciiStream(parameterName, x, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setBinaryStream(final String parameterName, final InputStream x, final int length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setBinaryStream(parameterName, x, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setObject(final String parameterName, final Object x, final int targetSqlType, final int scale) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setObject(parameterName, x, targetSqlType, scale);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setObject(final String parameterName, final Object x, final int targetSqlType) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setObject(parameterName, x, targetSqlType);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setObject(final String parameterName, final Object x) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setObject(parameterName, x);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setCharacterStream(final String parameterName, final Reader reader, final int length) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setCharacterStream(parameterName, reader, length);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setDate(final String parameterName, final Date x, final Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setDate(parameterName, x, cal);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setTime(final String parameterName, final Time x, final Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setTime(parameterName, x, cal);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setTimestamp(final String parameterName, final Timestamp x, final Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setTimestamp(parameterName, x, cal);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public void setNull(final String parameterName, final int sqlType, final String typeName) throws SQLException {
        try {
            if (this.wrappedStmt == null) {
                throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
            }
            ((CallableStatement)this.wrappedStmt).setNull(parameterName, sqlType, typeName);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
        }
    }
    
    @Override
    public String getString(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getString(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public boolean getBoolean(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBoolean(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return false;
        }
    }
    
    @Override
    public byte getByte(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getByte(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }
    
    @Override
    public short getShort(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getShort(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }
    
    @Override
    public int getInt(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getInt(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0;
        }
    }
    
    @Override
    public long getLong(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getLong(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0L;
        }
    }
    
    @Override
    public float getFloat(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getFloat(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0.0f;
        }
    }
    
    @Override
    public double getDouble(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getDouble(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return 0.0;
        }
    }
    
    @Override
    public byte[] getBytes(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBytes(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Date getDate(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getDate(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Time getTime(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTime(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Timestamp getTimestamp(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTimestamp(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Object getObject(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getObject(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public BigDecimal getBigDecimal(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBigDecimal(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Object getObject(final String parameterName, final Map<String, Class<?>> typeMap) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getObject(parameterName, typeMap);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Ref getRef(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getRef(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Blob getBlob(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getBlob(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Clob getClob(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getClob(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Array getArray(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getArray(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Date getDate(final String parameterName, final Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getDate(parameterName, cal);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Time getTime(final String parameterName, final Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTime(parameterName, cal);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public Timestamp getTimestamp(final String parameterName, final Calendar cal) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getTimestamp(parameterName, cal);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public URL getURL(final String parameterName) throws SQLException {
        try {
            if (this.wrappedStmt != null) {
                return ((CallableStatement)this.wrappedStmt).getURL(parameterName);
            }
            throw SQLError.createSQLException("No operations allowed after statement closed", "S1000", this.exceptionInterceptor);
        }
        catch (SQLException sqlEx) {
            this.checkAndFireConnectionError(sqlEx);
            return null;
        }
    }
    
    @Override
    public RowId getRowId(final int parameterIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public RowId getRowId(final String parameterName) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setRowId(final String parameterName, final RowId x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNString(final String parameterName, final String value) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNCharacterStream(final String parameterName, final Reader value, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNClob(final String parameterName, final NClob value) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setClob(final String parameterName, final Reader reader, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setBlob(final String parameterName, final InputStream inputStream, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNClob(final String parameterName, final Reader reader, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public NClob getNClob(final int parameterIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public NClob getNClob(final String parameterName) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setSQLXML(final String parameterName, final SQLXML xmlObject) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public SQLXML getSQLXML(final int parameterIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public SQLXML getSQLXML(final String parameterName) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public String getNString(final int parameterIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public String getNString(final String parameterName) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Reader getNCharacterStream(final int parameterIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Reader getNCharacterStream(final String parameterName) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Reader getCharacterStream(final int parameterIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Reader getCharacterStream(final String parameterName) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setBlob(final String parameterName, final Blob x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setClob(final String parameterName, final Clob x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setAsciiStream(final String parameterName, final InputStream x, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setBinaryStream(final String parameterName, final InputStream x, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setCharacterStream(final String parameterName, final Reader reader, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setAsciiStream(final String parameterName, final InputStream x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setBinaryStream(final String parameterName, final InputStream x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setCharacterStream(final String parameterName, final Reader reader) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNCharacterStream(final String parameterName, final Reader value) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setClob(final String parameterName, final Reader reader) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setBlob(final String parameterName, final InputStream inputStream) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNClob(final String parameterName, final Reader reader) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public <T> T getObject(final int parameterIndex, final Class<T> type) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public <T> T getObject(final String parameterName, final Class<T> type) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    static {
        if (Util.isJdbc4()) {
            try {
                final String jdbc4ClassName = Util.isJdbc42() ? "com.alipay.oceanbase.jdbc.jdbc2.optional.JDBC42CallableStatementWrapper" : "com.alipay.oceanbase.jdbc.jdbc2.optional.JDBC4CallableStatementWrapper";
                JDBC_4_CALLABLE_STATEMENT_WRAPPER_CTOR = Class.forName(jdbc4ClassName).getConstructor(ConnectionWrapper.class, MysqlPooledConnection.class, CallableStatement.class);
                return;
            }
            catch (SecurityException e) {
                throw new RuntimeException(e);
            }
            catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            }
            catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_CALLABLE_STATEMENT_WRAPPER_CTOR = null;
    }
}
