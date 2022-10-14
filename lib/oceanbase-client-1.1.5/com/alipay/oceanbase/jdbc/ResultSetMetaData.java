// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;

public class ResultSetMetaData implements java.sql.ResultSetMetaData
{
    Field[] fields;
    boolean useOldAliasBehavior;
    boolean treatYearAsDate;
    private ExceptionInterceptor exceptionInterceptor;
    
    private static int clampedGetLength(final Field f) {
        long fieldLength = f.getLength();
        if (fieldLength > 2147483647L) {
            fieldLength = 2147483647L;
        }
        return (int)fieldLength;
    }
    
    private static final boolean isDecimalType(final int type) {
        switch (type) {
            case -7:
            case -6:
            case -5:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public ResultSetMetaData(final Field[] fields, final boolean useOldAliasBehavior, final boolean treatYearAsDate, final ExceptionInterceptor exceptionInterceptor) {
        this.useOldAliasBehavior = false;
        this.treatYearAsDate = true;
        this.fields = fields;
        this.useOldAliasBehavior = useOldAliasBehavior;
        this.treatYearAsDate = treatYearAsDate;
        this.exceptionInterceptor = exceptionInterceptor;
    }
    
    @Override
    public String getCatalogName(final int column) throws SQLException {
        final Field f = this.getField(column);
        final String database = f.getDatabaseName();
        return (database == null) ? "" : database;
    }
    
    public String getColumnCharacterEncoding(final int column) throws SQLException {
        final String mysqlName = this.getColumnCharacterSet(column);
        String javaName = null;
        if (mysqlName != null) {
            try {
                javaName = CharsetMapping.getJavaEncodingForMysqlCharset(mysqlName);
            }
            catch (RuntimeException ex) {
                final SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                sqlEx.initCause(ex);
                throw sqlEx;
            }
        }
        return javaName;
    }
    
    public String getColumnCharacterSet(final int column) throws SQLException {
        return this.getField(column).getEncoding();
    }
    
    @Override
    public String getColumnClassName(final int column) throws SQLException {
        final Field f = this.getField(column);
        return getClassNameForJavaType(f.getSQLType(), f.isUnsigned(), f.getMysqlType(), f.isBinary() || f.isBlob(), f.isOpaqueBinary(), this.treatYearAsDate);
    }
    
    @Override
    public int getColumnCount() throws SQLException {
        return this.fields.length;
    }
    
    @Override
    public int getColumnDisplaySize(final int column) throws SQLException {
        final Field f = this.getField(column);
        final int lengthInBytes = clampedGetLength(f);
        return lengthInBytes / f.getMaxBytesPerCharacter();
    }
    
    @Override
    public String getColumnLabel(final int column) throws SQLException {
        if (this.useOldAliasBehavior) {
            return this.getColumnName(column);
        }
        return this.getField(column).getColumnLabel();
    }
    
    @Override
    public String getColumnName(final int column) throws SQLException {
        if (this.useOldAliasBehavior) {
            return this.getField(column).getName();
        }
        if (this.getField(column).getConnect().getIO().isOracleMode()) {
            return this.getField(column).getName();
        }
        final String name = this.getField(column).getNameNoAliases();
        if (name != null && name.length() == 0) {
            return this.getField(column).getName();
        }
        return name;
    }
    
    @Override
    public int getColumnType(final int column) throws SQLException {
        final int sqlType = this.getField(column).getSQLType();
        if (this.getField(column).getConnect().getIO().isOracleMode()) {
            switch (this.getField(column).getMysqlType()) {
                case 5: {
                    return 101;
                }
                case 4: {
                    return 100;
                }
                case 207: {
                    return -9;
                }
                case 206:
                case 246: {
                    return 2;
                }
                case 209: {
                    return -8;
                }
                case 208: {
                    return -15;
                }
                case 211: {
                    return 2005;
                }
                case 204: {
                    return -103;
                }
            }
        }
        return sqlType;
    }
    
    public int getInOutType(final int column) throws SQLException {
        return this.getField(column).getInoutType();
    }
    
    @Override
    public String getColumnTypeName(final int column) throws SQLException {
        final Field field = this.getField(column);
        final int mysqlType = field.getMysqlType();
        final int jdbcType = field.getSQLType();
        switch (mysqlType) {
            case 16: {
                return "BIT";
            }
            case 0:
            case 246: {
                if (field.getConnect().getIO().isOracleMode()) {
                    return field.isUnsigned() ? "NUMBER UNSIGNED" : "NUMBER";
                }
                return field.isUnsigned() ? "DECIMAL UNSIGNED" : "DECIMAL";
            }
            case 1: {
                return field.isUnsigned() ? "TINYINT UNSIGNED" : "TINYINT";
            }
            case 2: {
                return field.isUnsigned() ? "SMALLINT UNSIGNED" : "SMALLINT";
            }
            case 3: {
                return field.isUnsigned() ? "INT UNSIGNED" : "INT";
            }
            case 4: {
                if (field.getConnect().getIO().isOracleMode()) {
                    return field.isUnsigned() ? "BINARY_FLOAT UNSIGNED" : "BINARY_FLOAT";
                }
                return field.isUnsigned() ? "FLOAT UNSIGNED" : "FLOAT";
            }
            case 5: {
                if (field.getConnect().getIO().isOracleMode()) {
                    return field.isUnsigned() ? "BINARY_DOUBLE UNSIGNED" : "BINARY_DOUBLE";
                }
                return field.isUnsigned() ? "DOUBLE UNSIGNED" : "DOUBLE";
            }
            case 6: {
                return "NULL";
            }
            case 7:
            case 202: {
                return "TIMESTAMP";
            }
            case 200: {
                return "TIMESTAMP WITH TIME ZONE";
            }
            case 201: {
                return "TIMESTAMP WITH LOCAL TIME ZONE";
            }
            case 8: {
                return field.isUnsigned() ? "BIGINT UNSIGNED" : "BIGINT";
            }
            case 9: {
                return field.isUnsigned() ? "MEDIUMINT UNSIGNED" : "MEDIUMINT";
            }
            case 10: {
                return "DATE";
            }
            case 11: {
                return "TIME";
            }
            case 12: {
                if (field.getConnect().getIO().isOracleMode()) {
                    return "DATE";
                }
                return "DATETIME";
            }
            case 249: {
                return "TINYBLOB";
            }
            case 250: {
                return "MEDIUMBLOB";
            }
            case 251: {
                if (jdbcType == 2005) {
                    return "CLOB";
                }
                return "LONGBLOB";
            }
            case 203: {
                return "RAW";
            }
            case 252: {
                if (this.getField(column).isBinary()) {
                    return "BLOB";
                }
                return "TEXT";
            }
            case 15: {
                return "VARCHAR";
            }
            case 209: {
                if (field.getConnect().getIO().isOracleMode()) {
                    return "ROWID";
                }
            }
            case 253: {
                if (jdbcType == -3) {
                    return "VARBINARY";
                }
                if (this.getField(column).getConnect().getIO().isOracleMode()) {
                    return "VARCHAR2";
                }
                return "VARCHAR";
            }
            case 254: {
                if (jdbcType == -2) {
                    return "BINARY";
                }
                return "CHAR";
            }
            case 247: {
                return "ENUM";
            }
            case 13: {
                return "YEAR";
            }
            case 248: {
                return "SET";
            }
            case 255: {
                return "GEOMETRY";
            }
            case 245: {
                return "JSON";
            }
            case 210: {
                return "BLOB";
            }
            case 211: {
                return "CLOB";
            }
            case 207: {
                return "NVARCHAR2";
            }
            case 206: {
                if (field.getConnect().getIO().isOracleMode()) {
                    return "NUMBER";
                }
            }
            case 208: {
                if (field.getConnect().getIO().isOracleMode()) {
                    return "NCHAR";
                }
            }
            case 204: {
                if (field.getConnect().getIO().isOracleMode()) {
                    return "INTERVALYM";
                }
                break;
            }
        }
        return "UNKNOWN";
    }
    
    protected Field getField(final int columnIndex) throws SQLException {
        if (columnIndex < 1 || columnIndex > this.fields.length) {
            throw SQLError.createSQLException(Messages.getString("ResultSetMetaData.46"), "S1002", this.exceptionInterceptor);
        }
        return this.fields[columnIndex - 1];
    }
    
    @Override
    public int getPrecision(final int column) throws SQLException {
        final Field f = this.getField(column);
        if (isDecimalType(f.getSQLType())) {
            if (f.getDecimals() > 0) {
                return clampedGetLength(f) - 1 + f.getPrecisionAdjustFactor();
            }
            return clampedGetLength(f) + f.getPrecisionAdjustFactor();
        }
        else {
            switch (f.getMysqlType()) {
                case 203:
                case 249:
                case 250:
                case 251:
                case 252: {
                    if (f.getSQLType() != 2005) {
                        return clampedGetLength(f);
                    }
                    return clampedGetLength(f) / f.getMaxBytesPerCharacter();
                }
                default: {
                    return clampedGetLength(f) / f.getMaxBytesPerCharacter();
                }
            }
        }
    }
    
    @Override
    public int getScale(final int column) throws SQLException {
        final Field f = this.getField(column);
        if (isDecimalType(f.getSQLType())) {
            return f.getDecimals();
        }
        return 0;
    }
    
    @Override
    public String getSchemaName(final int column) throws SQLException {
        return "";
    }
    
    @Override
    public String getTableName(final int column) throws SQLException {
        if (this.useOldAliasBehavior) {
            return this.getField(column).getTableName();
        }
        return this.getField(column).getTableNameNoAliases();
    }
    
    @Override
    public boolean isAutoIncrement(final int column) throws SQLException {
        final Field f = this.getField(column);
        return f.isAutoIncrement();
    }
    
    @Override
    public boolean isCaseSensitive(final int column) throws SQLException {
        final Field field = this.getField(column);
        final int sqlType = field.getSQLType();
        if (field.getConnect() != null && field.getConnect().getIO().isOracleMode()) {
            switch (sqlType) {
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
            switch (sqlType) {
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
                    if (field.isBinary()) {
                        return true;
                    }
                    final String collationName = field.getCollation();
                    return collationName != null && !collationName.endsWith("_ci");
                }
                default: {
                    return true;
                }
            }
        }
    }
    
    @Override
    public boolean isCurrency(final int column) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isDefinitelyWritable(final int column) throws SQLException {
        return this.isWritable(column);
    }
    
    @Override
    public int isNullable(final int column) throws SQLException {
        if (!this.getField(column).isNotNull()) {
            return 1;
        }
        return 0;
    }
    
    @Override
    public boolean isReadOnly(final int column) throws SQLException {
        return this.getField(column).isReadOnly();
    }
    
    @Override
    public boolean isSearchable(final int column) throws SQLException {
        return true;
    }
    
    @Override
    public boolean isSigned(final int column) throws SQLException {
        final Field f = this.getField(column);
        final int sqlType = f.getSQLType();
        switch (sqlType) {
            case -6:
            case -5:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8: {
                return !f.isUnsigned();
            }
            case 91:
            case 92:
            case 93: {
                return false;
            }
            default: {
                return false;
            }
        }
    }
    
    @Override
    public boolean isWritable(final int column) throws SQLException {
        return !this.isReadOnly(column);
    }
    
    @Override
    public String toString() {
        final StringBuilder toStringBuf = new StringBuilder();
        toStringBuf.append(super.toString());
        toStringBuf.append(" - Field level information: ");
        for (int i = 0; i < this.fields.length; ++i) {
            toStringBuf.append("\n\t");
            toStringBuf.append(this.fields[i].toString());
        }
        return toStringBuf.toString();
    }
    
    static String getClassNameForJavaType(final int javaType, final boolean isUnsigned, final int mysqlTypeIfKnown, final boolean isBinaryOrBlob, final boolean isOpaqueBinary, final boolean treatYearAsDate) {
        switch (javaType) {
            case -7:
            case 16: {
                return "java.lang.Boolean";
            }
            case -6: {
                if (isUnsigned) {
                    return "java.lang.Integer";
                }
                return "java.lang.Integer";
            }
            case 5: {
                if (isUnsigned) {
                    return "java.lang.Integer";
                }
                return "java.lang.Integer";
            }
            case 4: {
                if (!isUnsigned || mysqlTypeIfKnown == 9) {
                    return "java.lang.Integer";
                }
                return "java.lang.Long";
            }
            case -5: {
                if (!isUnsigned) {
                    return "java.lang.Long";
                }
                return "java.math.BigInteger";
            }
            case 2:
            case 3: {
                return "java.math.BigDecimal";
            }
            case 7: {
                return "java.lang.Float";
            }
            case 6:
            case 8: {
                return "java.lang.Double";
            }
            case -1:
            case 1:
            case 12: {
                if (mysqlTypeIfKnown == 210) {
                    return "com.alipay.oceanbase.jdbc.Blob";
                }
                if (mysqlTypeIfKnown == 211) {
                    return "com.alipay.oceanbase.jdbc.Clob";
                }
                if (!isOpaqueBinary) {
                    return "java.lang.String";
                }
                return "[B";
            }
            case -4:
            case -3:
            case -2: {
                if (mysqlTypeIfKnown == 210) {
                    return "com.alipay.oceanbase.jdbc.Blob";
                }
                if (mysqlTypeIfKnown == 211) {
                    return "com.alipay.oceanbase.jdbc.Clob";
                }
                if (mysqlTypeIfKnown == 255) {
                    return "[B";
                }
                if (isBinaryOrBlob) {
                    return "[B";
                }
                return "java.lang.String";
            }
            case 91: {
                return (treatYearAsDate || mysqlTypeIfKnown != 13) ? "java.sql.Date" : "java.lang.Short";
            }
            case 92: {
                return "java.sql.Time";
            }
            case 93: {
                return "java.sql.Timestamp";
            }
            case -101: {
                return "com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMPTZ";
            }
            case -102: {
                return "com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMPLTZ";
            }
            case 2005: {
                return "java.sql.Clob";
            }
            default: {
                return "java.lang.Object";
            }
        }
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
            throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", this.exceptionInterceptor);
        }
    }
}
