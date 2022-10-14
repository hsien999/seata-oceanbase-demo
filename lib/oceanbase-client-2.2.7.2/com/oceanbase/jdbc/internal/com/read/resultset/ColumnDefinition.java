// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.resultset;

import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.nio.charset.Charset;
import com.oceanbase.jdbc.internal.ColumnType;
import com.oceanbase.jdbc.internal.com.read.Buffer;

public class ColumnDefinition
{
    private static final int[] maxCharlen;
    private final Buffer buffer;
    private final short charsetNumber;
    private final long length;
    private final ColumnType type;
    private final int decimals;
    private final int precision;
    private final int inoutType;
    private final int sqltype;
    private int catalogNameStart;
    private int catalogNameLength;
    private int databaseNameStart;
    private int databaseNameLength;
    private int tableNameStart;
    private int tableNameLength;
    private int originalTableNameStart;
    private int originalTableNameLength;
    private int nameStart;
    private int nameLength;
    private int originalColumnNameStart;
    private int originalColumnNameLength;
    private short longColFlag;
    private int complexSchemaNameStart;
    private int complexSchemaNameLength;
    private int complexTypeNameStart;
    private int complexTypeNameLength;
    private int complexVersion;
    private final boolean isOracleMode;
    private int precisionAdjustFactor;
    private String encoding;
    
    public ColumnDefinition(final ColumnDefinition other) {
        this.complexSchemaNameStart = -1;
        this.complexSchemaNameLength = -1;
        this.complexTypeNameStart = -1;
        this.complexTypeNameLength = -1;
        this.complexVersion = 0;
        this.precisionAdjustFactor = 0;
        this.encoding = "UTF-8";
        this.buffer = other.buffer;
        this.charsetNumber = other.charsetNumber;
        this.length = other.length;
        this.sqltype = other.sqltype;
        this.type = other.type;
        this.decimals = other.decimals;
        this.isOracleMode = other.isOracleMode;
        this.precision = other.precision;
        this.inoutType = other.inoutType;
        this.catalogNameLength = other.nameLength;
        this.catalogNameStart = other.catalogNameStart;
        this.databaseNameStart = other.nameStart;
        this.databaseNameLength = other.nameLength;
        this.tableNameLength = other.tableNameLength;
        this.tableNameStart = other.tableNameStart;
        this.originalTableNameStart = other.originalTableNameStart;
        this.originalTableNameLength = other.originalTableNameLength;
        this.originalColumnNameLength = other.originalColumnNameLength;
        this.originalColumnNameStart = other.originalColumnNameStart;
        this.nameStart = other.nameStart;
        this.nameLength = other.nameLength;
        this.longColFlag = other.longColFlag;
    }
    
    public ColumnDefinition(final Buffer buffer, final boolean OracleMode, final String encoding) {
        this.complexSchemaNameStart = -1;
        this.complexSchemaNameLength = -1;
        this.complexTypeNameStart = -1;
        this.complexTypeNameLength = -1;
        this.complexVersion = 0;
        this.precisionAdjustFactor = 0;
        this.encoding = "UTF-8";
        this.buffer = buffer;
        this.catalogNameStart = buffer.getPosition() + 1;
        this.catalogNameLength = buffer.fastSkipLenString();
        this.catalogNameStart = this.adjustStartForFieldLength(this.catalogNameStart, this.catalogNameLength);
        this.databaseNameStart = buffer.getPosition() + 1;
        this.databaseNameLength = buffer.fastSkipLenString();
        this.databaseNameStart = this.adjustStartForFieldLength(this.databaseNameStart, this.databaseNameLength);
        this.tableNameStart = buffer.getPosition() + 1;
        this.tableNameLength = buffer.fastSkipLenString();
        this.tableNameStart = this.adjustStartForFieldLength(this.tableNameStart, this.tableNameLength);
        this.originalTableNameStart = buffer.getPosition() + 1;
        this.originalTableNameLength = buffer.fastSkipLenString();
        this.originalTableNameStart = this.adjustStartForFieldLength(this.originalTableNameStart, this.originalTableNameLength);
        this.originalColumnNameStart = buffer.getPosition() + 1;
        this.originalColumnNameLength = buffer.fastSkipLenString();
        this.originalColumnNameStart = this.adjustStartForFieldLength(this.originalColumnNameStart, this.originalColumnNameLength);
        this.nameStart = buffer.getPosition() + 1;
        this.nameLength = buffer.fastSkipLenString();
        this.nameStart = this.adjustStartForFieldLength(this.nameStart, this.nameLength);
        buffer.readByte();
        this.charsetNumber = buffer.readShort();
        this.length = buffer.readInt();
        this.sqltype = (buffer.readByte() & 0xFF);
        this.longColFlag = buffer.readShort();
        this.type = ColumnType.fromServer(this.sqltype, this.charsetNumber, OracleMode);
        this.decimals = (buffer.readByte() & 0xFF);
        this.precision = (buffer.readByte() & 0xFF);
        this.inoutType = (buffer.readByte() & 0xFF);
        if (ColumnType.COMPLEX.getType() == this.type.getType()) {
            buffer.setPosition(buffer.getPosition());
            this.complexSchemaNameStart = buffer.getPosition() + 1;
            this.complexSchemaNameLength = buffer.fastSkipLenString();
            this.complexSchemaNameStart = this.adjustStartForFieldLength(this.complexSchemaNameStart, this.complexSchemaNameLength);
            this.complexTypeNameStart = buffer.getPosition() + 1;
            this.complexTypeNameLength = buffer.fastSkipLenString();
            this.complexTypeNameStart = this.adjustStartForFieldLength(this.complexTypeNameStart, this.complexTypeNameLength);
            this.complexVersion = (int)buffer.readFieldLength();
        }
        this.isOracleMode = OracleMode;
        if (this.isSigned()) {
            switch (this.type) {
                case DECIMAL:
                case OBDECIMAL:
                case OLDDECIMAL: {
                    this.precisionAdjustFactor = -1;
                    break;
                }
                case DOUBLE:
                case BINARY_DOUBLE:
                case FLOAT:
                case BINARY_FLOAT: {
                    this.precisionAdjustFactor = 1;
                    break;
                }
            }
        }
        else {
            switch (this.type) {
                case DOUBLE:
                case BINARY_DOUBLE:
                case FLOAT:
                case BINARY_FLOAT: {
                    this.precisionAdjustFactor = 1;
                    break;
                }
            }
        }
        this.encoding = encoding;
    }
    
    public static ColumnDefinition create(final String name, final ColumnType type, final boolean isOracleMode, final String encoding) {
        final Charset charset = Charset.forName(encoding);
        final byte[] nameBytes = name.getBytes(charset);
        final byte[] arr = new byte[19 + 2 * nameBytes.length];
        int pos = 0;
        for (int i = 0; i < 4; ++i) {
            arr[pos++] = 0;
        }
        for (int i = 0; i < 2; ++i) {
            arr[pos++] = (byte)nameBytes.length;
            System.arraycopy(nameBytes, 0, arr, pos, nameBytes.length);
            pos += nameBytes.length;
        }
        arr[pos++] = 12;
        arr[pos++] = 33;
        arr[pos++] = 0;
        int len = 0;
        switch (type.getSqlType()) {
            case 1:
            case 12: {
                len = 192;
                break;
            }
            case 5: {
                len = 5;
                break;
            }
            case 0: {
                len = 0;
                break;
            }
            default: {
                len = 1;
                break;
            }
        }
        arr[pos] = (byte)len;
        pos += 4;
        arr[pos++] = (byte)ColumnType.toServer(type.getSqlType()).getType();
        return new ColumnDefinition(new Buffer(arr), isOracleMode, encoding);
    }
    
    private int adjustStartForFieldLength(final int nameStart, final int nameLength) {
        if (nameLength < 251) {
            return nameStart;
        }
        if (nameLength >= 251 && nameLength < 65536) {
            return nameStart + 2;
        }
        if (nameLength >= 65536 && nameLength < 16777216) {
            return nameStart + 3;
        }
        return nameStart + 8;
    }
    
    public String getDatabase() {
        return this.getStringFromBytes(this.databaseNameStart, this.databaseNameLength);
    }
    
    public String getTable() {
        return this.getStringFromBytes(this.tableNameStart, this.tableNameLength);
    }
    
    public String getOriginalTable() {
        return this.getStringFromBytes(this.originalTableNameStart, this.originalTableNameLength);
    }
    
    public String getName() {
        return this.getStringFromBytes(this.nameStart, this.nameLength);
    }
    
    public String getOriginalName() {
        return this.getStringFromBytes(this.originalColumnNameStart, this.originalColumnNameLength);
    }
    
    public short getCharsetNumber() {
        return this.charsetNumber;
    }
    
    public long getLength() {
        return this.length;
    }
    
    public long getPrecision() {
        switch (this.type.getSqlType()) {
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
                if (this.decimals > 0) {
                    return this.length - 1L + this.precisionAdjustFactor;
                }
                return this.length + this.precisionAdjustFactor;
            }
            default: {
                int maxWidth = ColumnDefinition.maxCharlen[this.charsetNumber & 0xFF];
                if (maxWidth == 0) {
                    maxWidth = 1;
                }
                switch (this.type) {
                    case NVARCHAR2:
                    case NCHAR: {
                        if (this.isOracleMode) {
                            return this.length / maxWidth;
                        }
                        return this.length;
                    }
                    case VARCHAR2:
                    case STRING:
                    case VARSTRING:
                    case OBCLOB: {
                        if (this.isOracleMode) {
                            return this.length;
                        }
                        return this.length / maxWidth;
                    }
                    default: {
                        return this.length;
                    }
                }
                break;
            }
        }
    }
    
    public int getDisplaySize() {
        final int vtype = this.type.getSqlType();
        if (vtype == 12 || vtype == 1 || vtype == -9 || vtype == -15 || vtype == 2005) {
            int maxWidth = ColumnDefinition.maxCharlen[this.charsetNumber & 0xFF];
            if (maxWidth == 0) {
                maxWidth = 1;
            }
            return (int)this.length / maxWidth;
        }
        return (int)this.length;
    }
    
    public int getDecimals() {
        switch (this.type.getSqlType()) {
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
                return this.decimals;
            }
            default: {
                return 0;
            }
        }
    }
    
    public ColumnType getColumnType() {
        return this.type;
    }
    
    public short getFlags() {
        return this.longColFlag;
    }
    
    public boolean isSigned() {
        return (this.longColFlag & 0x20) == 0x0;
    }
    
    public boolean isNotNull() {
        return (this.longColFlag & 0x1) > 0;
    }
    
    public boolean isPrimaryKey() {
        return (this.longColFlag & 0x2) > 0;
    }
    
    public boolean isUniqueKey() {
        return (this.longColFlag & 0x4) > 0;
    }
    
    public boolean isMultipleKey() {
        return (this.longColFlag & 0x8) > 0;
    }
    
    public boolean isBlob() {
        return (this.longColFlag & 0x10) > 0;
    }
    
    public boolean isZeroFill() {
        return (this.longColFlag & 0x40) > 0;
    }
    
    public boolean isBinary() {
        return this.getCharsetNumber() == 63;
    }
    
    public String getComplexTypeName() throws SQLException {
        return this.getStringFromBytes(this.complexTypeNameStart, this.complexTypeNameLength);
    }
    
    private String toAsciiString(final byte[] buffer, final int startPos, final int length) {
        final Charset cs = Charset.forName(this.encoding);
        return cs.decode(ByteBuffer.wrap(buffer, startPos, length)).toString();
    }
    
    private String getStringFromBytes(final int stringStart, final int stringLength) {
        if (stringStart == -1 || stringLength == -1) {
            return null;
        }
        return this.toAsciiString(this.buffer.buf, stringStart, stringLength);
    }
    
    public int getSqltype() {
        return this.sqltype;
    }
    
    static {
        maxCharlen = new int[] { 0, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 3, 2, 1, 1, 1, 0, 1, 2, 1, 1, 1, 1, 2, 1, 1, 1, 2, 1, 1, 1, 1, 3, 1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 4, 4, 1, 1, 1, 1, 1, 1, 1, 4, 4, 0, 1, 1, 1, 4, 4, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 3, 2, 2, 2, 2, 2, 1, 2, 3, 1, 1, 1, 2, 2, 3, 3, 1, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 0, 3, 4, 4, 0, 0, 0, 0, 0, 0, 0, 3, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 0, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    }
}
