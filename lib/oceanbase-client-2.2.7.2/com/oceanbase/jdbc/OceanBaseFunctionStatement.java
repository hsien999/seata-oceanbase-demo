// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;

public class OceanBaseFunctionStatement extends JDBC4CallableStatement
{
    public OceanBaseFunctionStatement(final OceanBaseConnection connection, final String databaseName, final String procedureName, final String arguments, final int resultSetType, final int resultSetConcurrency, final ExceptionFactory exceptionFactory) throws SQLException {
        super(connection, databaseName, procedureName, arguments, resultSetType, resultSetConcurrency, exceptionFactory);
    }
}
