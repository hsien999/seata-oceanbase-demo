// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.RowIdLifetime;

public class JDBC4DatabaseMetaData extends DatabaseMetaData
{
    public JDBC4DatabaseMetaData(final MySQLConnection connToSet, final String databaseToSet) {
        super(connToSet, databaseToSet);
    }
    
    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        try {
            return iface.cast(this);
        }
        catch (ClassCastException cce) {
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.conn.getExceptionInterceptor());
        }
    }
    
    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;
    }
    
    @Override
    public ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException {
        final Field[] fields = this.createProcedureColumnsFields();
        return this.getProcedureOrFunctionColumns(fields, catalog, schemaPattern, procedureNamePattern, columnNamePattern, true, this.conn.getGetProceduresReturnsFunctions());
    }
    
    @Override
    public ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
        final Field[] fields = this.createFieldMetadataForGetProcedures();
        return this.getProceduresAndOrFunctions(fields, catalog, schemaPattern, procedureNamePattern, true, this.conn.getGetProceduresReturnsFunctions());
    }
    
    @Override
    protected int getJDBC4FunctionNoTableConstant() {
        return 1;
    }
    
    @Override
    protected int getColumnType(final boolean isOutParam, final boolean isInParam, final boolean isReturnParam, final boolean forGetFunctionColumns) {
        return getProcedureOrFunctionColumnType(isOutParam, isInParam, isReturnParam, forGetFunctionColumns);
    }
    
    protected static int getProcedureOrFunctionColumnType(final boolean isOutParam, final boolean isInParam, final boolean isReturnParam, final boolean forGetFunctionColumns) {
        if (isInParam && isOutParam) {
            return forGetFunctionColumns ? 2 : 2;
        }
        if (isInParam) {
            return (!forGetFunctionColumns || true) ? 1 : 0;
        }
        if (isOutParam) {
            return forGetFunctionColumns ? 3 : 4;
        }
        if (isReturnParam) {
            return forGetFunctionColumns ? 4 : 5;
        }
        return (forGetFunctionColumns && false) ? 1 : 0;
    }
}
