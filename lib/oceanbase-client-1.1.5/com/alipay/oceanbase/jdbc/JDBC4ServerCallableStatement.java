// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.io.Reader;
import java.sql.NClob;
import java.sql.SQLXML;
import java.sql.RowId;
import java.sql.SQLException;

public class JDBC4ServerCallableStatement extends ServerCallableStatement
{
    public JDBC4ServerCallableStatement(final MySQLConnection conn, final CallableStatementParamInfo paramInfo) throws SQLException {
        super(conn, paramInfo);
    }
    
    public JDBC4ServerCallableStatement(final MySQLConnection conn, final String sql, final String catalog, final boolean isFunctionCall) throws SQLException {
        super(conn, sql, catalog, isFunctionCall);
    }
    
    @Override
    public void setRowId(final int parameterIndex, final RowId x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setRowId(final String parameterName, final RowId x) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) throws SQLException {
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
    public RowId getRowId(final int parameterIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public RowId getRowId(final String parameterName) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNClob(final int parameterIndex, final NClob value) throws SQLException {
        JDBC4PreparedStatementHelper.setNClob(this, parameterIndex, value);
    }
    
    @Override
    public void setNClob(final String parameterName, final NClob value) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNClob(final String parameterName, final Reader reader) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNClob(final String parameterName, final Reader reader, final long length) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNString(final String parameterName, final String value) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Reader getCharacterStream(final int parameterIndex) throws SQLException {
        final ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
        final Reader retValue = rs.getCharacterStream(this.mapOutputParameterIndexToRsIndex(parameterIndex));
        this.outputParamWasNull = rs.wasNull();
        return retValue;
    }
    
    @Override
    public Reader getCharacterStream(final String parameterName) throws SQLException {
        final ResultSetInternalMethods rs = this.getOutputParameters(0);
        final Reader retValue = rs.getCharacterStream(parameterName);
        this.outputParamWasNull = rs.wasNull();
        return retValue;
    }
    
    @Override
    public Reader getNCharacterStream(final int parameterIndex) throws SQLException {
        final ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
        final Reader retValue = ((JDBC4ResultSet)rs).getNCharacterStream(this.mapOutputParameterIndexToRsIndex(parameterIndex));
        this.outputParamWasNull = rs.wasNull();
        return retValue;
    }
    
    @Override
    public Reader getNCharacterStream(final String parameterName) throws SQLException {
        final ResultSetInternalMethods rs = this.getOutputParameters(0);
        final Reader retValue = ((JDBC4ResultSet)rs).getNCharacterStream(parameterName);
        this.outputParamWasNull = rs.wasNull();
        return retValue;
    }
    
    @Override
    public NClob getNClob(final int parameterIndex) throws SQLException {
        final ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
        final NClob retValue = ((JDBC4ResultSet)rs).getNClob(this.mapOutputParameterIndexToRsIndex(parameterIndex));
        this.outputParamWasNull = rs.wasNull();
        return retValue;
    }
    
    @Override
    public NClob getNClob(final String parameterName) throws SQLException {
        final ResultSetInternalMethods rs = this.getOutputParameters(0);
        final NClob retValue = ((JDBC4ResultSet)rs).getNClob(parameterName);
        this.outputParamWasNull = rs.wasNull();
        return retValue;
    }
    
    @Override
    public String getNString(final int parameterIndex) throws SQLException {
        final ResultSetInternalMethods rs = this.getOutputParameters(parameterIndex);
        final String retValue = ((JDBC4ResultSet)rs).getNString(this.mapOutputParameterIndexToRsIndex(parameterIndex));
        this.outputParamWasNull = rs.wasNull();
        return retValue;
    }
    
    @Override
    public String getNString(final String parameterName) throws SQLException {
        final ResultSetInternalMethods rs = this.getOutputParameters(0);
        final String retValue = ((JDBC4ResultSet)rs).getNString(parameterName);
        this.outputParamWasNull = rs.wasNull();
        return retValue;
    }
}
