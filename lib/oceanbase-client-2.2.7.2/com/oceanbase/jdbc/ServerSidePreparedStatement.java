// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;

public class ServerSidePreparedStatement extends JDBC4ServerPreparedStatement
{
    public ServerSidePreparedStatement(final boolean isObFunction, final OceanBaseConnection connection, final String sql, final int resultSetScrollType, final int resultSetConcurrency, final int autoGeneratedKeys, final ExceptionFactory exceptionFactory) throws SQLException {
        super(isObFunction, connection, sql, resultSetScrollType, resultSetConcurrency, autoGeneratedKeys, exceptionFactory);
    }
}
