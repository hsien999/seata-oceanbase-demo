// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.ResultSet;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.internal.com.read.resultset.SelectResultSet;
import com.oceanbase.jdbc.internal.util.dao.CloneableCallableStatement;

public class JDBC4CallableStatement extends CallableFunctionStatement implements CloneableCallableStatement
{
    private SelectResultSet outputResultSet;
    
    public JDBC4CallableStatement(final OceanBaseConnection connection, final String databaseName, final String procedureName, final String arguments, final int resultSetType, final int resultSetConcurrency, final ExceptionFactory exceptionFactory) throws SQLException {
        super(connection, "SELECT " + procedureName + ((arguments == null) ? "()" : arguments), resultSetType, resultSetConcurrency, exceptionFactory);
        this.outputResultSet = null;
        this.parameterMetadata = new CallableParameterMetaData(connection, databaseName, procedureName, true);
        this.arguments = arguments;
        super.initFunctionData(this.getParameterCount() + 1);
    }
    
    @Override
    protected SelectResultSet getResult() throws SQLException {
        if (this.outputResultSet == null) {
            throw new SQLException("No output result");
        }
        return this.outputResultSet;
    }
    
    @Override
    public JDBC4CallableStatement clone(final OceanBaseConnection connection) throws CloneNotSupportedException {
        final JDBC4CallableStatement clone = (JDBC4CallableStatement)super.clone(connection);
        clone.outputResultSet = null;
        return clone;
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        this.connection.lock.lock();
        try {
            super.execute();
            this.retrieveOutputResult();
            if (this.results != null && this.results.getResultSet() == null) {
                return 0;
            }
            return this.getUpdateCount();
        }
        finally {
            this.connection.lock.unlock();
        }
    }
    
    private void retrieveOutputResult() throws SQLException {
        this.outputResultSet = this.results.getResultSet();
        if (this.outputResultSet != null) {
            this.outputResultSet.next();
        }
    }
    
    @Override
    public void setParameter(final int parameterIndex, final ParameterHolder holder) throws SQLException {
        super.setParameter(parameterIndex - 1, holder);
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        this.connection.lock.lock();
        try {
            super.execute();
            this.retrieveOutputResult();
            if (this.results != null && this.results.getResultSet() == null) {
                return this.results.getResultSet();
            }
            return JDBC4ResultSet.createEmptyResultSet();
        }
        finally {
            this.connection.lock.unlock();
        }
    }
    
    @Override
    public boolean execute() throws SQLException {
        this.connection.lock.lock();
        try {
            super.execute();
            this.retrieveOutputResult();
            return this.results != null && this.results.getResultSet() == null;
        }
        finally {
            this.connection.lock.unlock();
        }
    }
}
