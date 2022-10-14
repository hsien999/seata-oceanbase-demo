// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.internal.ColumnType;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import java.sql.ParameterMetaData;

public class OceanBaseParameterMetaData implements ParameterMetaData
{
    private final ColumnDefinition[] parametersInformation;
    
    public OceanBaseParameterMetaData(final ColumnDefinition[] parametersInformation) {
        this.parametersInformation = parametersInformation;
    }
    
    private void checkAvailable() throws SQLException {
        if (this.parametersInformation == null) {
            throw new SQLException("Parameter metadata not available for these statement", "S1C00");
        }
    }
    
    @Override
    public int getParameterCount() throws SQLException {
        this.checkAvailable();
        return this.parametersInformation.length;
    }
    
    private ColumnDefinition getParameterInformation(final int param) throws SQLException {
        this.checkAvailable();
        if (param >= 1 && param <= this.parametersInformation.length) {
            return this.parametersInformation[param - 1];
        }
        throw new SQLException("Parameter metadata out of range : param was " + param + " and must be 1 <= param <=" + this.parametersInformation.length, "07009");
    }
    
    @Override
    public int isNullable(final int param) throws SQLException {
        if (this.getParameterInformation(param).isNotNull()) {
            return 0;
        }
        return 1;
    }
    
    @Override
    public boolean isSigned(final int param) throws SQLException {
        return this.getParameterInformation(param).isSigned();
    }
    
    @Override
    public int getPrecision(final int param) throws SQLException {
        final long length = this.getParameterInformation(param).getLength();
        return (length > 2147483647L) ? Integer.MAX_VALUE : ((int)length);
    }
    
    @Override
    public int getScale(final int param) throws SQLException {
        if (ColumnType.isNumeric(this.getParameterInformation(param).getColumnType())) {
            return this.getParameterInformation(param).getDecimals();
        }
        return 0;
    }
    
    @Override
    public int getParameterType(final int param) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Getting parameter type metadata are not supported");
    }
    
    @Override
    public String getParameterTypeName(final int param) throws SQLException {
        return this.getParameterInformation(param).getColumnType().getTypeName();
    }
    
    @Override
    public String getParameterClassName(final int param) throws SQLException {
        return this.getParameterInformation(param).getColumnType().getClassName();
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
