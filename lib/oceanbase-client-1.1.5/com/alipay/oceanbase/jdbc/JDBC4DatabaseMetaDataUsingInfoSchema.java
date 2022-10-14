// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;

public class JDBC4DatabaseMetaDataUsingInfoSchema extends DatabaseMetaDataUsingInfoSchema
{
    public JDBC4DatabaseMetaDataUsingInfoSchema(final MySQLConnection connToSet, final String databaseToSet) throws SQLException {
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
    protected ResultSet getProcedureColumnsNoISParametersView(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException {
        final Field[] fields = this.createProcedureColumnsFields();
        return this.getProcedureOrFunctionColumns(fields, catalog, schemaPattern, procedureNamePattern, columnNamePattern, true, this.conn.getGetProceduresReturnsFunctions());
    }
    
    @Override
    protected String getRoutineTypeConditionForGetProcedures() {
        return this.conn.getGetProceduresReturnsFunctions() ? "" : "ROUTINE_TYPE = 'PROCEDURE' AND ";
    }
    
    @Override
    protected String getRoutineTypeConditionForGetProcedureColumns() {
        return this.conn.getGetProceduresReturnsFunctions() ? "" : "ROUTINE_TYPE = 'PROCEDURE' AND ";
    }
    
    @Override
    protected int getJDBC4FunctionConstant(final JDBC4FunctionConstant constant) {
        switch (constant) {
            case FUNCTION_COLUMN_IN: {
                return 1;
            }
            case FUNCTION_COLUMN_INOUT: {
                return 2;
            }
            case FUNCTION_COLUMN_OUT: {
                return 3;
            }
            case FUNCTION_COLUMN_RETURN: {
                return 4;
            }
            case FUNCTION_COLUMN_RESULT: {
                return 5;
            }
            case FUNCTION_COLUMN_UNKNOWN: {
                return 0;
            }
            case FUNCTION_NO_NULLS: {
                return 0;
            }
            case FUNCTION_NULLABLE: {
                return 1;
            }
            case FUNCTION_NULLABLE_UNKNOWN: {
                return 2;
            }
            default: {
                return -1;
            }
        }
    }
    
    @Override
    protected int getJDBC4FunctionNoTableConstant() {
        return 1;
    }
    
    @Override
    protected int getColumnType(final boolean isOutParam, final boolean isInParam, final boolean isReturnParam, final boolean forGetFunctionColumns) {
        return JDBC4DatabaseMetaData.getProcedureOrFunctionColumnType(isOutParam, isInParam, isReturnParam, forGetFunctionColumns);
    }
}
