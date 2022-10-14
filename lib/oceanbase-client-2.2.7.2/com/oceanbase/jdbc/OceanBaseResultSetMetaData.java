// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.internal.ColumnType;
import java.sql.SQLException;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import java.sql.ResultSetMetaData;

public class OceanBaseResultSetMetaData implements ResultSetMetaData
{
    private final ColumnDefinition[] fieldPackets;
    private final Options options;
    private final boolean forceAlias;
    private final boolean isOracleMode;
    
    public OceanBaseResultSetMetaData(final ColumnDefinition[] fieldPackets, final Options options, final boolean forceAlias) {
        this.fieldPackets = fieldPackets;
        this.options = options;
        this.forceAlias = forceAlias;
        this.isOracleMode = false;
    }
    
    public OceanBaseResultSetMetaData(final ColumnDefinition[] fieldPackets, final Options options, final boolean forceAlias, final boolean isOracleMode) {
        this.fieldPackets = fieldPackets;
        this.options = options;
        this.forceAlias = forceAlias;
        this.isOracleMode = isOracleMode;
    }
    
    @Override
    public int getColumnCount() {
        return this.fieldPackets.length;
    }
    
    @Override
    public boolean isAutoIncrement(final int column) throws SQLException {
        return (this.getColumnInformation(column).getFlags() & 0x200) != 0x0;
    }
    
    @Override
    public boolean isCaseSensitive(final int column) throws SQLException {
        final ColumnDefinition columnInformation = this.getColumnInformation(column);
        if (this.isOracleMode) {
            switch (columnInformation.getColumnType().getSqlType()) {
                case -15:
                case -9:
                case -1:
                case 1:
                case 12: {
                    return true;
                }
                default: {
                    return false;
                }
            }
        }
        else {
            switch (columnInformation.getColumnType().getSqlType()) {
                case -7:
                case -6:
                case -5:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 91:
                case 92:
                case 93: {
                    return false;
                }
                case -1:
                case 1:
                case 12:
                case 2005: {
                    return !columnInformation.isBinary() || true;
                }
                default: {
                    return true;
                }
            }
        }
    }
    
    @Override
    public boolean isSearchable(final int column) {
        return true;
    }
    
    @Override
    public boolean isCurrency(final int column) {
        return false;
    }
    
    @Override
    public int isNullable(final int column) throws SQLException {
        if ((this.getColumnInformation(column).getFlags() & 0x1) == 0x0) {
            return 1;
        }
        return 0;
    }
    
    @Override
    public boolean isSigned(final int column) throws SQLException {
        return this.getColumnInformation(column).isSigned();
    }
    
    @Override
    public int getColumnDisplaySize(final int column) throws SQLException {
        return this.getColumnInformation(column).getDisplaySize();
    }
    
    @Override
    public String getColumnLabel(final int column) throws SQLException {
        return this.getColumnInformation(column).getOriginalName();
    }
    
    @Override
    public String getColumnName(final int column) throws SQLException {
        if (this.isOracleMode) {
            return this.getColumnInformation(column).getOriginalName();
        }
        final String columnName = this.getColumnInformation(column).getName();
        if ("".equals(columnName) || this.options.useOldAliasMetadataBehavior || this.forceAlias) {
            return this.getColumnLabel(column);
        }
        return columnName;
    }
    
    @Override
    public String getCatalogName(final int column) throws SQLException {
        return this.getColumnInformation(column).getDatabase();
    }
    
    @Override
    public int getPrecision(final int column) throws SQLException {
        return (int)this.getColumnInformation(column).getPrecision();
    }
    
    @Override
    public int getScale(final int column) throws SQLException {
        return this.getColumnInformation(column).getDecimals();
    }
    
    @Override
    public String getTableName(final int column) throws SQLException {
        if (this.forceAlias) {
            return this.getColumnInformation(column).getTable();
        }
        if (this.options.blankTableNameMeta) {
            return "";
        }
        if (this.options.useOldAliasMetadataBehavior) {
            return this.getColumnInformation(column).getTable();
        }
        return this.getColumnInformation(column).getOriginalTable();
    }
    
    @Override
    public String getSchemaName(final int column) {
        return "";
    }
    
    @Override
    public int getColumnType(final int column) throws SQLException {
        final ColumnDefinition ci = this.getColumnInformation(column);
        if (this.isOracleMode) {
            switch (ci.getColumnType()) {
                case NVARCHAR2: {
                    return -9;
                }
                case BINARY_FLOAT: {
                    return 100;
                }
                case BINARY_DOUBLE: {
                    return 101;
                }
                case OBDECIMAL:
                case NUMBER_FLOAT: {
                    return 2;
                }
                case ROWID: {
                    return -8;
                }
                case NCHAR: {
                    return -15;
                }
                case INTERVALYM: {
                    return -103;
                }
                case INTERVALDS: {
                    return -104;
                }
            }
        }
        switch (ci.getColumnType()) {
            case BIT: {
                if (ci.getLength() == 1L) {
                    return -7;
                }
                return -3;
            }
            case TINYINT: {
                if (ci.getLength() == 1L && this.options.tinyInt1isBit) {
                    return -7;
                }
                return -6;
            }
            case YEAR: {
                if (this.options.yearIsDateType) {
                    return 91;
                }
                return 5;
            }
            case BLOB: {
                if (ci.getLength() < 0L || ci.getLength() > 16777215L) {
                    return -4;
                }
                return -3;
            }
            case VARCHAR: {
                if (ci.getSqltype() == 251) {
                    return -1;
                }
                if (ci.isBinary()) {
                    return -3;
                }
                return 12;
            }
            case VARSTRING: {
                if (ci.isBinary()) {
                    return -3;
                }
                return 12;
            }
            case STRING: {
                if (ci.isBinary()) {
                    return -2;
                }
                return 1;
            }
            default: {
                return ci.getColumnType().getSqlType();
            }
        }
    }
    
    @Override
    public String getColumnTypeName(final int column) throws SQLException {
        final ColumnDefinition ci = this.getColumnInformation(column);
        return ColumnType.getColumnTypeName(ci.getColumnType(), ci.getLength(), ci.isSigned(), ci.isBinary(), this.isOracleMode);
    }
    
    @Override
    public boolean isReadOnly(final int column) throws SQLException {
        final ColumnDefinition ci = this.getColumnInformation(column);
        return (ci.getOriginalTable() == null || ci.getOriginalTable().isEmpty()) && (ci.getOriginalName() == null || ci.getOriginalName().isEmpty());
    }
    
    @Override
    public boolean isWritable(final int column) throws SQLException {
        return !this.isReadOnly(column);
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) throws SQLException {
        return !this.isReadOnly(column);
    }
    
    @Override
    public String getColumnClassName(final int column) throws SQLException {
        final ColumnDefinition ci = this.getColumnInformation(column);
        final ColumnType type = ci.getColumnType();
        return ColumnType.getClassName(type, (int)ci.getLength(), ci.isSigned(), ci.isBinary(), this.options);
    }
    
    private ColumnDefinition getColumnInformation(final int column) throws SQLException {
        if (column >= 1 && column <= this.fieldPackets.length) {
            return this.fieldPackets[column - 1];
        }
        throw ExceptionFactory.INSTANCE.create(String.format("wrong column index %s. must be in [1, %s] range", column, this.fieldPackets.length));
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
