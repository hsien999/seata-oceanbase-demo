// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;

public class OceanBaseProcedureStatement extends JDBC4ServerCallableStatement
{
    public OceanBaseProcedureStatement(final boolean isObFunction, final String query, final OceanBaseConnection connection, final String procedureName, final String database, final String arguments, final int resultSetType, final int resultSetConcurrency, final ExceptionFactory exceptionFactory) throws SQLException {
        super(isObFunction, query, connection, procedureName, database, arguments, resultSetType, resultSetConcurrency, exceptionFactory);
    }
    
    public OceanBaseProcedureStatement(final boolean isObFunction, final String query, final OceanBaseConnection connection, final String procedureName, final String database, final String arguments, final int resultSetType, final int resultSetConcurrency, final ExceptionFactory exceptionFactory, final boolean isAnoymousBlock) throws SQLException {
        super(isObFunction, query, connection, procedureName, database, arguments, resultSetType, resultSetConcurrency, exceptionFactory, isAnoymousBlock);
    }
}
