// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import java.sql.SQLException;
import java.sql.ParameterMetaData;

public class SimpleParameterMetaData implements ParameterMetaData
{
    private final int parameterCount;
    
    public SimpleParameterMetaData(final int parameterCount) {
        this.parameterCount = parameterCount;
    }
    
    @Override
    public int getParameterCount() throws SQLException {
        return this.parameterCount;
    }
    
    @Override
    public int isNullable(final int param) throws SQLException {
        if (param < 1 || param > this.parameterCount) {
            throw ExceptionFactory.INSTANCE.create(String.format("Parameter metadata out of range : param was %s and must be in range 1 - %s", param, this.parameterCount), "07009");
        }
        return 2;
    }
    
    @Override
    public boolean isSigned(final int param) throws SQLException {
        if (param < 1 || param > this.parameterCount) {
            throw ExceptionFactory.INSTANCE.create(String.format("Parameter metadata out of range : param was %s and must be in range 1 - %s", param, this.parameterCount), "07009");
        }
        return true;
    }
    
    @Override
    public int getPrecision(final int param) throws SQLException {
        if (param < 1 || param > this.parameterCount) {
            throw ExceptionFactory.INSTANCE.create(String.format("Parameter metadata out of range : param was %s and must be in range 1 - %s", param, this.parameterCount), "07009");
        }
        throw ExceptionFactory.INSTANCE.create("Unknown parameter metadata precision");
    }
    
    @Override
    public int getScale(final int param) throws SQLException {
        if (param < 1 || param > this.parameterCount) {
            throw ExceptionFactory.INSTANCE.create(String.format("Parameter metadata out of range : param was %s and must be in range 1 - %s", param, this.parameterCount), "07009");
        }
        throw ExceptionFactory.INSTANCE.create("Unknown parameter metadata scale");
    }
    
    @Override
    public int getParameterType(final int param) throws SQLException {
        if (param < 1 || param > this.parameterCount) {
            throw ExceptionFactory.INSTANCE.create(String.format("Parameter metadata out of range : param was %s and must be in range 1 - %s", param, this.parameterCount), "07009");
        }
        throw ExceptionFactory.INSTANCE.notSupported("Getting parameter type metadata are not supported");
    }
    
    @Override
    public String getParameterTypeName(final int param) throws SQLException {
        if (param < 1 || param > this.parameterCount) {
            throw ExceptionFactory.INSTANCE.create(String.format("Parameter metadata out of range : param was %s and must be in range 1 - %s", param, this.parameterCount), "07009");
        }
        throw ExceptionFactory.INSTANCE.create("Unknown parameter metadata type name");
    }
    
    @Override
    public String getParameterClassName(final int param) throws SQLException {
        if (param < 1 || param > this.parameterCount) {
            throw ExceptionFactory.INSTANCE.create(String.format("Parameter metadata out of range : param was %s and must be in range 1 - %s", param, this.parameterCount), "07009");
        }
        throw ExceptionFactory.INSTANCE.create("Unknown parameter metadata class name");
    }
    
    @Override
    public int getParameterMode(final int param) {
        return 1;
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        try {
            if (this.isWrapperFor(iface)) {
                return iface.cast(this);
            }
            throw new SQLException("The receiver is not a wrapper for " + iface.getName());
        }
        catch (Exception e) {
            throw new SQLException("The receiver is not a wrapper and does not implement the interface");
        }
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }
}
