// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal;

import com.oceanbase.jdbc.extend.datatype.NUMBER;
import com.oceanbase.jdbc.extend.datatype.StructImpl;
import com.oceanbase.jdbc.extend.datatype.ArrayImpl;
import com.oceanbase.jdbc.extend.datatype.ComplexData;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMPLTZ;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMPTZ;
import java.math.BigInteger;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.Clob;
import com.oceanbase.jdbc.Blob;
import java.sql.Time;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public enum ColumnType
{
    OLDDECIMAL(0, 3, "Types.DECIMAL", BigDecimal.class.getName(), 3), 
    TINYINT(1, -6, "Types.TINYINT", Integer.class.getName(), 3), 
    SMALLINT(2, 5, "Types.SMALLINT", Integer.class.getName(), 3), 
    INTEGER(3, 4, "Types.INTEGER", Integer.class.getName(), 1), 
    FLOAT(4, 7, "Types.REAL", Float.class.getName(), 1), 
    DOUBLE(5, 8, "Types.DOUBLE", Double.class.getName(), 1), 
    NULL(6, 0, "Types.NULL", String.class.getName(), 3), 
    TIMESTAMP(7, 93, "Types.TIMESTAMP", Timestamp.class.getName(), 3), 
    BIGINT(8, -5, "Types.BIGINT", Long.class.getName(), 3), 
    MEDIUMINT(9, 4, "Types.INTEGER", Integer.class.getName(), 3), 
    DATE(10, 91, "Types.DATE", Date.class.getName(), 3), 
    TIME(11, 92, "Types.TIME", Time.class.getName(), 3), 
    DATETIME(12, 93, "Types.TIMESTAMP", Timestamp.class.getName(), 3), 
    YEAR(13, 91, "Types.SMALLINT", Short.class.getName(), 3), 
    NEWDATE(14, 91, "Types.DATE", Date.class.getName(), 3), 
    VARCHAR(15, 12, "Types.VARCHAR", String.class.getName(), 3), 
    BIT(16, -7, "Types.BIT", "[B", 3), 
    JSON(245, 12, "Types.VARCHAR", String.class.getName(), 3), 
    DECIMAL(246, 3, "Types.DECIMAL", BigDecimal.class.getName(), 3), 
    OBDECIMAL(246, 3, "Types.DECIMAL", BigDecimal.class.getName(), 2), 
    ENUM(247, 1, "Types.VARCHAR", String.class.getName(), 3), 
    SET(248, 1, "Types.VARCHAR", String.class.getName(), 3), 
    TINYBLOB(249, -3, "Types.VARBINARY", "[B", 3), 
    MEDIUMBLOB(250, -3, "Types.VARBINARY", "[B", 3), 
    LONGBLOB(251, -4, "Types.LONGVARBINARY", "[B", 3), 
    BLOB(252, -4, "Types.LONGVARBINARY", "[B", 3), 
    VARSTRING(253, 12, "Types.VARCHAR", String.class.getName(), 1), 
    VARCHAR2(253, 12, "Types.VARCHAR", String.class.getName(), 2), 
    STRING(254, 12, "Types.VARCHAR", String.class.getName(), 3), 
    GEOMETRY(255, 1, "Types.VARBINARY", "[B", 3), 
    TIMESTAMP_TZ(200, -101, "Types.TIMESTAMP", TIMESTAMPTZ.class.getName(), 2), 
    TIMESTAMP_LTZ(201, -102, "Types.TIMESTAMP", TIMESTAMPLTZ.class.getName(), 2), 
    TIMESTAMP_NANO(202, 93, "Types.TIMESTAMP", Timestamp.class.getName(), 2), 
    OBBLOB(210, 2004, "Types.LONGVARBINARY", "java.sql.Clob", 2), 
    OBCLOB(211, 2005, "Types.LONGVARBINARY", "com.oceanbase.jdbc.Clob", 2), 
    INTERVALYM(204, 1111, "Types.OTHER", String.class.getName(), 2), 
    INTERVALDS(205, 1111, "Types.OTHER", String.class.getName(), 2), 
    COMPLEX(160, 1111, "Types.OTHER", ComplexData.class.getName(), 2), 
    ARRAY(161, 2003, "Types.Array", ArrayImpl.class.getName(), 2), 
    STRUCT(162, 2002, "Types.STRUCT", StructImpl.class.getName(), 2), 
    RAW(203, -3, "Types.VARBINARY", "[B", 2), 
    CURSOR(163, 1111, "Types.OTHER", "CURSORTEMP", 2), 
    NUMBER(3, 4, "Types.INTEGER", NUMBER.class.getName(), 2), 
    NUMBER_FLOAT(206, 12, "Types.FLOAT", String.class.getName(), 2), 
    BINARY_DOUBLE(5, 8, "Types.DOUBLE", Double.class.getName(), 2), 
    NCHAR(208, -15, "Types.NCHAR", String.class.getName(), 2), 
    NVARCHAR2(207, 12, "Types.NVARCHAR2", String.class.getName(), 2), 
    ROWID(209, -8, "Types.ROWID", String.class.getName(), 2), 
    BINARY_FLOAT(4, 6, "Types.FLOAT", Float.class.getName(), 2);
    
    static final ColumnType[] typeMysqlMap;
    static final ColumnType[] typeOracleMap;
    static final int MYSQL_TYPE = 1;
    static final int ORACLE_TYPE = 2;
    static final int BOTH_TYPE = 3;
    private final short protocolType;
    private final int javaType;
    private final String javaTypeName;
    private final String className;
    private final int serverType;
    
    private ColumnType(final int protocolType, final int javaType, final String javaTypeName, final String className, final int serverType) {
        this.protocolType = (short)protocolType;
        this.javaType = javaType;
        this.javaTypeName = javaTypeName;
        this.className = className;
        this.serverType = serverType;
    }
    
    public static Class classFromJavaType(final int type) {
        switch (type) {
            case -7:
            case 16: {
                return Boolean.class;
            }
            case -6: {
                return Integer.class;
            }
            case 5: {
                return Integer.class;
            }
            case 4: {
                return Integer.class;
            }
            case -5: {
                return Long.class;
            }
            case 6:
            case 8: {
                return Double.class;
            }
            case 7: {
                return Float.class;
            }
            case 93: {
                return Timestamp.class;
            }
            case 91: {
                return Date.class;
            }
            case -16:
            case -15:
            case -9:
            case -1:
            case 1:
            case 12:
            case 2011: {
                return String.class;
            }
            case 2:
            case 3: {
                return BigDecimal.class;
            }
            case -4:
            case -3:
            case -2:
            case 2000: {
                return byte[].class;
            }
            case 0: {
                return null;
            }
            case 92: {
                return Time.class;
            }
            case 2004: {
                return Blob.class;
            }
            case 2005: {
                return Clob.class;
            }
            default: {
                return null;
            }
        }
    }
    
    public static boolean isNumeric(final ColumnType type) {
        switch (type) {
            case OLDDECIMAL:
            case TINYINT:
            case SMALLINT:
            case INTEGER:
            case FLOAT:
            case DOUBLE:
            case BIGINT:
            case MEDIUMINT:
            case BIT:
            case DECIMAL: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    public static String getColumnTypeName(final ColumnType type, final long len, final boolean signed, final boolean binary, final boolean isOracelMode) {
        long l = len;
        switch (type) {
            case SMALLINT:
            case INTEGER:
            case BIGINT:
            case MEDIUMINT: {
                if (!signed) {
                    return type.getTypeName() + " UNSIGNED";
                }
                return type.getTypeName();
            }
            case BLOB: {
                if (binary) {
                    l -= 2L;
                }
                if (len < 0L) {
                    return "LONGBLOB";
                }
                if (l <= 255L) {
                    return "TINYBLOB";
                }
                if (l <= 65535L) {
                    return "BLOB";
                }
                if (l <= 16777215L) {
                    return "MEDIUMBLOB";
                }
                return "LONGBLOB";
            }
            case VARSTRING:
            case VARCHAR: {
                if (binary) {
                    return "VARBINARY";
                }
                return "VARCHAR";
            }
            case STRING: {
                if (binary) {
                    return "BINARY";
                }
                return "CHAR";
            }
            case OBCLOB: {
                return "CLOB";
            }
            case OBBLOB: {
                return "BLOB";
            }
            case TIMESTAMP_NANO: {
                return "TIMESTAMP";
            }
            case TIMESTAMP_TZ: {
                return "TIMESTAMP WITH TIME ZONE";
            }
            case TIMESTAMP_LTZ: {
                return "TIMESTAMP WITH LOCAL TIME ZONE";
            }
            case OLDDECIMAL:
            case DECIMAL: {
                return signed ? "DECIMAL" : "DECIMAL UNSIGNED";
            }
            case OBDECIMAL: {
                return signed ? "NUMBER" : "NUMBER UNSIGNED";
            }
            case NUMBER_FLOAT: {
                return "NUMBER";
            }
            case DATETIME: {
                if (isOracelMode) {
                    return "DATE";
                }
                break;
            }
        }
        return type.getTypeName();
    }
    
    public static ColumnType fromServer(final int typeValue, final int charsetNumber, final boolean isOracleMode) {
        ColumnType columnType;
        if (isOracleMode) {
            columnType = ColumnType.typeOracleMap[typeValue];
        }
        else {
            columnType = ColumnType.typeMysqlMap[typeValue];
        }
        if (columnType == null) {
            columnType = ColumnType.BLOB;
        }
        if (charsetNumber != 63 && typeValue >= 249 && typeValue <= 252) {
            return ColumnType.VARCHAR;
        }
        return columnType;
    }
    
    public static ColumnType toServer(final int javaType) {
        for (final ColumnType v : values()) {
            if (v.javaType == javaType) {
                return v;
            }
        }
        return ColumnType.BLOB;
    }
    
    public static String getClassName(final ColumnType type, final int len, final boolean signed, final boolean binary, final Options options) {
        switch (type) {
            case TINYINT: {
                if (len == 1 && options.tinyInt1isBit) {
                    return Boolean.class.getName();
                }
                return Integer.class.getName();
            }
            case INTEGER: {
                return signed ? Integer.class.getName() : Long.class.getName();
            }
            case BIGINT: {
                return signed ? Long.class.getName() : BigInteger.class.getName();
            }
            case YEAR: {
                if (options.yearIsDateType) {
                    return Date.class.getName();
                }
                return Short.class.getName();
            }
            case BIT: {
                return (len == 1) ? Boolean.class.getName() : "[B";
            }
            case VARSTRING:
            case VARCHAR:
            case STRING: {
                return binary ? "[B" : String.class.getName();
            }
            default: {
                return type.getClassName();
            }
        }
    }
    
    public String getClassName() {
        return this.className;
    }
    
    public int getSqlType() {
        return this.javaType;
    }
    
    public String getTypeName() {
        return this.name();
    }
    
    public short getType() {
        return this.protocolType;
    }
    
    public String getJavaTypeName() {
        return this.javaTypeName;
    }
    
    static {
        typeMysqlMap = new ColumnType[256];
        typeOracleMap = new ColumnType[256];
        for (final ColumnType v : values()) {
            if (v.serverType == 1) {
                ColumnType.typeMysqlMap[v.protocolType] = v;
            }
            else if (v.serverType == 2) {
                ColumnType.typeOracleMap[v.protocolType] = v;
            }
            else if (v.serverType == 3) {
                ColumnType.typeMysqlMap[v.protocolType] = v;
                ColumnType.typeOracleMap[v.protocolType] = v;
            }
        }
    }
}
