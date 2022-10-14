// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.DatabaseMetaData;
import java.util.regex.PatternSyntaxException;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.extend.datatype.ComplexDataType;

public class Field implements Cloneable
{
    private static final int AUTO_INCREMENT_FLAG = 512;
    private static final int NO_CHARSET_INFO = -1;
    private byte[] buffer;
    private int collationIndex;
    private String encoding;
    private int colDecimals;
    private int precision;
    private int inoutType;
    private short colFlag;
    private String collationName;
    private MySQLConnection connection;
    private String databaseName;
    private int databaseNameLength;
    private int databaseNameStart;
    protected int defaultValueLength;
    protected int defaultValueStart;
    private String fullName;
    private String fullOriginalName;
    private boolean isImplicitTempTable;
    private long length;
    private int mysqlType;
    private String name;
    private int nameLength;
    private int nameStart;
    private String originalColumnName;
    private int originalColumnNameLength;
    private int originalColumnNameStart;
    private String originalTableName;
    private int originalTableNameLength;
    private int originalTableNameStart;
    private int precisionAdjustFactor;
    private int sqlType;
    private String tableName;
    private int tableNameLength;
    private int tableNameStart;
    private boolean useOldNameMetadata;
    private boolean isSingleBit;
    private int maxBytesPerChar;
    private final boolean valueNeedsQuoting;
    private int complexSchemaNameStart;
    private int complexSchemaNameLength;
    private int complexTypeNameStart;
    private int complexTypeNameLength;
    private int complexVersion;
    private ComplexDataType complexType;
    
    public int getPrecision() {
        return this.precision;
    }
    
    public int getInoutType() {
        return this.inoutType;
    }
    
    Field(final MySQLConnection conn, final byte[] buffer, final int databaseNameStart, final int databaseNameLength, final int tableNameStart, final int tableNameLength, final int originalTableNameStart, final int originalTableNameLength, final int nameStart, final int nameLength, final int originalColumnNameStart, final int originalColumnNameLength, final long length, final int mysqlType, final short colFlag, final int colDecimals, final int defaultValueStart, final int defaultValueLength, final int charsetIndex, final int complexSchemaNameStart, final int complexSchemaNameLength, final int complexTypeNameStart, final int complexTypeNameLength, final int complexVersion, final int precision, final int inoutType) throws SQLException {
        this(conn, buffer, databaseNameStart, databaseNameLength, tableNameStart, tableNameLength, originalTableNameStart, originalTableNameLength, nameStart, nameLength, originalColumnNameStart, originalColumnNameLength, length, mysqlType, colFlag, colDecimals, defaultValueStart, defaultValueLength, charsetIndex);
        this.complexSchemaNameStart = complexSchemaNameStart;
        this.complexSchemaNameLength = complexSchemaNameLength;
        this.complexTypeNameStart = complexTypeNameStart;
        this.complexTypeNameLength = complexTypeNameLength;
        this.complexVersion = complexVersion;
        this.precision = precision;
        this.inoutType = inoutType;
    }
    
    Field(final MySQLConnection conn, final byte[] buffer, final int databaseNameStart, final int databaseNameLength, final int tableNameStart, final int tableNameLength, final int originalTableNameStart, final int originalTableNameLength, final int nameStart, final int nameLength, final int originalColumnNameStart, final int originalColumnNameLength, final long length, final int mysqlType, final short colFlag, final int colDecimals, final int defaultValueStart, final int defaultValueLength, final int charsetIndex) throws SQLException {
        this.collationIndex = 0;
        this.encoding = null;
        this.collationName = null;
        this.connection = null;
        this.databaseName = null;
        this.databaseNameLength = -1;
        this.databaseNameStart = -1;
        this.defaultValueLength = -1;
        this.defaultValueStart = -1;
        this.fullName = null;
        this.fullOriginalName = null;
        this.isImplicitTempTable = false;
        this.mysqlType = -1;
        this.originalColumnName = null;
        this.originalColumnNameLength = -1;
        this.originalColumnNameStart = -1;
        this.originalTableName = null;
        this.originalTableNameLength = -1;
        this.originalTableNameStart = -1;
        this.precisionAdjustFactor = 0;
        this.sqlType = -1;
        this.useOldNameMetadata = false;
        this.complexSchemaNameStart = -1;
        this.complexSchemaNameLength = -1;
        this.complexTypeNameStart = -1;
        this.complexTypeNameLength = -1;
        this.complexVersion = 0;
        this.complexType = null;
        this.connection = conn;
        this.buffer = buffer;
        this.nameStart = nameStart;
        this.nameLength = nameLength;
        this.tableNameStart = tableNameStart;
        this.tableNameLength = tableNameLength;
        this.length = length;
        this.colFlag = colFlag;
        this.colDecimals = colDecimals;
        this.mysqlType = mysqlType;
        this.databaseNameStart = databaseNameStart;
        this.databaseNameLength = databaseNameLength;
        this.originalTableNameStart = originalTableNameStart;
        this.originalTableNameLength = originalTableNameLength;
        this.originalColumnNameStart = originalColumnNameStart;
        this.originalColumnNameLength = originalColumnNameLength;
        this.defaultValueStart = defaultValueStart;
        this.defaultValueLength = defaultValueLength;
        this.collationIndex = charsetIndex;
        this.sqlType = MysqlDefs.mysqlToJavaType(this.mysqlType);
        this.checkForImplicitTemporaryTable();
        final boolean isFromFunction = this.originalTableNameLength == 0;
        if (this.mysqlType == 252) {
            if (this.connection.getBlobsAreStrings() || (this.connection.getFunctionsNeverReturnBlobs() && isFromFunction)) {
                this.sqlType = 12;
                this.mysqlType = 15;
            }
            else if (this.collationIndex == 63 || !this.connection.versionMeetsMinimum(4, 1, 0)) {
                if (this.connection.getUseBlobToStoreUTF8OutsideBMP() && this.shouldSetupForUtf8StringInBlob()) {
                    this.setupForUtf8StringInBlob();
                }
                else {
                    this.setBlobTypeBasedOnLength();
                    this.sqlType = MysqlDefs.mysqlToJavaType(this.mysqlType);
                }
            }
            else {
                this.mysqlType = 253;
                this.sqlType = -1;
            }
        }
        if (this.sqlType == -6 && this.length == 1L && this.connection.getTinyInt1isBit() && conn.getTinyInt1isBit()) {
            if (conn.getTransformedBitIsBoolean()) {
                this.sqlType = 16;
            }
            else {
                this.sqlType = -7;
            }
        }
        if (!this.isNativeNumericType() && !this.isNativeDateTimeType()) {
            this.encoding = this.connection.getEncodingForIndex(this.collationIndex);
            if ("UnicodeBig".equals(this.encoding)) {
                this.encoding = "UTF-16";
            }
            if (this.mysqlType == 245) {
                this.encoding = "UTF-8";
            }
            boolean isBinary = this.isBinary();
            if (this.connection.versionMeetsMinimum(4, 1, 0) && this.mysqlType == 253 && isBinary && this.collationIndex == 63) {
                if (this.connection.getFunctionsNeverReturnBlobs() && isFromFunction) {
                    this.sqlType = 12;
                    this.mysqlType = 15;
                }
                else if (this.isOpaqueBinary()) {
                    this.sqlType = -3;
                }
            }
            if (this.connection.versionMeetsMinimum(4, 1, 0) && this.mysqlType == 254 && isBinary && this.collationIndex == 63 && this.isOpaqueBinary() && !this.connection.getBlobsAreStrings()) {
                this.sqlType = -2;
            }
            if (this.mysqlType == 16 && !(this.isSingleBit = (this.length == 0L || (this.length == 1L && (this.connection.versionMeetsMinimum(5, 0, 21) || this.connection.versionMeetsMinimum(5, 1, 10)))))) {
                this.colFlag |= 0x80;
                this.colFlag |= 0x10;
                isBinary = true;
            }
            if (this.sqlType == -4 && !isBinary) {
                this.sqlType = -1;
            }
            else if (this.sqlType == -3 && !isBinary) {
                this.sqlType = 12;
            }
        }
        else {
            this.encoding = "US-ASCII";
        }
        if (conn.getIO().isOracleMode() && this.mysqlType == 251 && charsetIndex != 63) {
            this.sqlType = 2005;
        }
        if (!this.isUnsigned()) {
            switch (this.mysqlType) {
                case 0:
                case 246: {
                    this.precisionAdjustFactor = -1;
                    break;
                }
                case 4:
                case 5: {
                    this.precisionAdjustFactor = 1;
                    break;
                }
            }
        }
        else {
            switch (this.mysqlType) {
                case 4:
                case 5: {
                    this.precisionAdjustFactor = 1;
                    break;
                }
            }
        }
        this.valueNeedsQuoting = this.determineNeedsQuoting();
    }
    
    private boolean shouldSetupForUtf8StringInBlob() throws SQLException {
        final String includePattern = this.connection.getUtf8OutsideBmpIncludedColumnNamePattern();
        final String excludePattern = this.connection.getUtf8OutsideBmpExcludedColumnNamePattern();
        if (excludePattern != null && !StringUtils.isEmptyOrWhitespaceOnly(excludePattern)) {
            try {
                if (this.getOriginalName().matches(excludePattern)) {
                    if (includePattern != null && !StringUtils.isEmptyOrWhitespaceOnly(includePattern)) {
                        try {
                            if (this.getOriginalName().matches(includePattern)) {
                                return true;
                            }
                        }
                        catch (PatternSyntaxException pse) {
                            final SQLException sqlEx = SQLError.createSQLException("Illegal regex specified for \"utf8OutsideBmpIncludedColumnNamePattern\"", "S1009", this.connection.getExceptionInterceptor());
                            if (!this.connection.getParanoid()) {
                                sqlEx.initCause(pse);
                            }
                            throw sqlEx;
                        }
                    }
                    return false;
                }
            }
            catch (PatternSyntaxException pse) {
                final SQLException sqlEx = SQLError.createSQLException("Illegal regex specified for \"utf8OutsideBmpExcludedColumnNamePattern\"", "S1009", this.connection.getExceptionInterceptor());
                if (!this.connection.getParanoid()) {
                    sqlEx.initCause(pse);
                }
                throw sqlEx;
            }
        }
        return true;
    }
    
    private void setupForUtf8StringInBlob() {
        if (this.length == 255L || this.length == 65535L) {
            this.mysqlType = 15;
            this.sqlType = 12;
        }
        else {
            this.mysqlType = 253;
            this.sqlType = -1;
        }
        this.collationIndex = 33;
    }
    
    Field(final MySQLConnection conn, final byte[] buffer, final int nameStart, final int nameLength, final int tableNameStart, final int tableNameLength, final int length, final int mysqlType, final short colFlag, final int colDecimals) throws SQLException {
        this(conn, buffer, -1, -1, tableNameStart, tableNameLength, -1, -1, nameStart, nameLength, -1, -1, length, mysqlType, colFlag, colDecimals, -1, -1, -1);
    }
    
    Field(final String tableName, final String columnName, final int jdbcType, final int length) {
        this.collationIndex = 0;
        this.encoding = null;
        this.collationName = null;
        this.connection = null;
        this.databaseName = null;
        this.databaseNameLength = -1;
        this.databaseNameStart = -1;
        this.defaultValueLength = -1;
        this.defaultValueStart = -1;
        this.fullName = null;
        this.fullOriginalName = null;
        this.isImplicitTempTable = false;
        this.mysqlType = -1;
        this.originalColumnName = null;
        this.originalColumnNameLength = -1;
        this.originalColumnNameStart = -1;
        this.originalTableName = null;
        this.originalTableNameLength = -1;
        this.originalTableNameStart = -1;
        this.precisionAdjustFactor = 0;
        this.sqlType = -1;
        this.useOldNameMetadata = false;
        this.complexSchemaNameStart = -1;
        this.complexSchemaNameLength = -1;
        this.complexTypeNameStart = -1;
        this.complexTypeNameLength = -1;
        this.complexVersion = 0;
        this.complexType = null;
        this.tableName = tableName;
        this.name = columnName;
        this.length = length;
        this.sqlType = jdbcType;
        this.colFlag = 0;
        this.colDecimals = 0;
        this.valueNeedsQuoting = this.determineNeedsQuoting();
    }
    
    Field(final String tableName, final String columnName, final int charsetIndex, final int jdbcType, final int length) {
        this.collationIndex = 0;
        this.encoding = null;
        this.collationName = null;
        this.connection = null;
        this.databaseName = null;
        this.databaseNameLength = -1;
        this.databaseNameStart = -1;
        this.defaultValueLength = -1;
        this.defaultValueStart = -1;
        this.fullName = null;
        this.fullOriginalName = null;
        this.isImplicitTempTable = false;
        this.mysqlType = -1;
        this.originalColumnName = null;
        this.originalColumnNameLength = -1;
        this.originalColumnNameStart = -1;
        this.originalTableName = null;
        this.originalTableNameLength = -1;
        this.originalTableNameStart = -1;
        this.precisionAdjustFactor = 0;
        this.sqlType = -1;
        this.useOldNameMetadata = false;
        this.complexSchemaNameStart = -1;
        this.complexSchemaNameLength = -1;
        this.complexTypeNameStart = -1;
        this.complexTypeNameLength = -1;
        this.complexVersion = 0;
        this.complexType = null;
        this.tableName = tableName;
        this.name = columnName;
        this.length = length;
        this.sqlType = jdbcType;
        this.colFlag = 0;
        this.colDecimals = 0;
        this.collationIndex = charsetIndex;
        this.valueNeedsQuoting = this.determineNeedsQuoting();
        switch (this.sqlType) {
            case -3:
            case -2: {
                this.colFlag |= 0x80;
                this.colFlag |= 0x10;
                break;
            }
        }
    }
    
    private void checkForImplicitTemporaryTable() {
        this.isImplicitTempTable = (this.tableNameLength > 5 && this.buffer[this.tableNameStart] == 35 && this.buffer[this.tableNameStart + 1] == 115 && this.buffer[this.tableNameStart + 2] == 113 && this.buffer[this.tableNameStart + 3] == 108 && this.buffer[this.tableNameStart + 4] == 95);
    }
    
    public String getEncoding() throws SQLException {
        return this.encoding;
    }
    
    public void setEncoding(final String javaEncodingName, final Connection conn) throws SQLException {
        this.encoding = javaEncodingName;
        try {
            this.collationIndex = CharsetMapping.getCollationIndexForJavaEncoding(javaEncodingName, conn);
        }
        catch (RuntimeException ex) {
            final SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }
    
    public synchronized String getCollation() throws SQLException {
        if (this.collationName == null && this.connection != null && this.connection.versionMeetsMinimum(4, 1, 0)) {
            if (this.connection.getUseDynamicCharsetInfo()) {
                final DatabaseMetaData dbmd = this.connection.getMetaData();
                String quotedIdStr = dbmd.getIdentifierQuoteString();
                if (" ".equals(quotedIdStr)) {
                    quotedIdStr = "";
                }
                final String csCatalogName = this.getDatabaseName();
                final String csTableName = this.getOriginalTableName();
                final String csColumnName = this.getOriginalName();
                if (csCatalogName != null && csCatalogName.length() != 0 && csTableName != null && csTableName.length() != 0 && csColumnName != null && csColumnName.length() != 0) {
                    final StringBuilder queryBuf = new StringBuilder(csCatalogName.length() + csTableName.length() + 28);
                    queryBuf.append("SHOW FULL COLUMNS FROM ");
                    queryBuf.append(quotedIdStr);
                    queryBuf.append(csCatalogName);
                    queryBuf.append(quotedIdStr);
                    queryBuf.append(".");
                    queryBuf.append(quotedIdStr);
                    queryBuf.append(csTableName);
                    queryBuf.append(quotedIdStr);
                    Statement collationStmt = null;
                    ResultSet collationRs = null;
                    try {
                        collationStmt = this.connection.createStatement();
                        collationRs = collationStmt.executeQuery(queryBuf.toString());
                        while (collationRs.next()) {
                            if (csColumnName.equals(collationRs.getString("Field"))) {
                                this.collationName = collationRs.getString("Collation");
                                break;
                            }
                        }
                    }
                    finally {
                        if (collationRs != null) {
                            collationRs.close();
                            collationRs = null;
                        }
                        if (collationStmt != null) {
                            collationStmt.close();
                            collationStmt = null;
                        }
                    }
                }
            }
            else {
                try {
                    this.collationName = CharsetMapping.COLLATION_INDEX_TO_COLLATION_NAME[this.collationIndex];
                }
                catch (RuntimeException ex) {
                    final SQLException sqlEx = SQLError.createSQLException(ex.toString(), "S1009", null);
                    sqlEx.initCause(ex);
                    throw sqlEx;
                }
            }
        }
        return this.collationName;
    }
    
    public String getColumnLabel() throws SQLException {
        return this.getName();
    }
    
    public String getDatabaseName() throws SQLException {
        if (this.databaseName == null && this.databaseNameStart != -1 && this.databaseNameLength != -1) {
            this.databaseName = this.getStringFromBytes(this.databaseNameStart, this.databaseNameLength);
        }
        return this.databaseName;
    }
    
    int getDecimals() {
        return this.colDecimals;
    }
    
    public String getFullName() throws SQLException {
        if (this.fullName == null) {
            StringBuilder fullNameBuf = new StringBuilder(this.getTableName().length() + 1 + this.getName().length());
            fullNameBuf.append(this.tableName);
            fullNameBuf.append('.');
            fullNameBuf.append(this.name);
            this.fullName = fullNameBuf.toString();
            fullNameBuf = null;
        }
        return this.fullName;
    }
    
    public String getFullOriginalName() throws SQLException {
        this.getOriginalName();
        if (this.originalColumnName == null) {
            return null;
        }
        if (this.fullName == null) {
            StringBuilder fullOriginalNameBuf = new StringBuilder(this.getOriginalTableName().length() + 1 + this.getOriginalName().length());
            fullOriginalNameBuf.append(this.originalTableName);
            fullOriginalNameBuf.append('.');
            fullOriginalNameBuf.append(this.originalColumnName);
            this.fullOriginalName = fullOriginalNameBuf.toString();
            fullOriginalNameBuf = null;
        }
        return this.fullOriginalName;
    }
    
    public long getLength() {
        return this.length;
    }
    
    public synchronized int getMaxBytesPerCharacter() throws SQLException {
        if (this.maxBytesPerChar == 0) {
            this.maxBytesPerChar = this.connection.getMaxBytesPerChar(this.collationIndex, this.getEncoding());
        }
        return this.maxBytesPerChar;
    }
    
    public int getMysqlType() {
        return this.mysqlType;
    }
    
    public String getName() throws SQLException {
        if (this.name == null) {
            this.name = this.getStringFromBytes(this.nameStart, this.nameLength);
        }
        return this.name;
    }
    
    public String getNameNoAliases() throws SQLException {
        if (this.useOldNameMetadata) {
            return this.getName();
        }
        if (this.connection != null && this.connection.versionMeetsMinimum(4, 1, 0)) {
            return this.getOriginalName();
        }
        return this.getName();
    }
    
    public String getOriginalName() throws SQLException {
        if (this.originalColumnName == null && this.originalColumnNameStart != -1 && this.originalColumnNameLength != -1) {
            this.originalColumnName = this.getStringFromBytes(this.originalColumnNameStart, this.originalColumnNameLength);
        }
        return this.originalColumnName;
    }
    
    public String getOriginalTableName() throws SQLException {
        if (this.originalTableName == null && this.originalTableNameStart != -1 && this.originalTableNameLength != -1) {
            this.originalTableName = this.getStringFromBytes(this.originalTableNameStart, this.originalTableNameLength);
        }
        return this.originalTableName;
    }
    
    public int getPrecisionAdjustFactor() {
        return this.precisionAdjustFactor;
    }
    
    public int getSQLType() {
        return this.sqlType;
    }
    
    private String getStringFromBytes(final int stringStart, final int stringLength) throws SQLException {
        if (stringStart == -1 || stringLength == -1) {
            return null;
        }
        String stringVal = null;
        if (this.connection != null) {
            if (this.connection.getUseUnicode()) {
                String javaEncoding = this.connection.getCharacterSetMetadata();
                if (javaEncoding == null) {
                    javaEncoding = this.connection.getEncoding();
                }
                if (javaEncoding != null) {
                    SingleByteCharsetConverter converter = null;
                    if (this.connection != null) {
                        converter = this.connection.getCharsetConverter(javaEncoding);
                    }
                    if (converter != null) {
                        stringVal = converter.toString(this.buffer, stringStart, stringLength);
                    }
                    else {
                        try {
                            stringVal = StringUtils.toString(this.buffer, stringStart, stringLength, javaEncoding);
                        }
                        catch (UnsupportedEncodingException ue) {
                            throw new RuntimeException(Messages.getString("Field.12") + javaEncoding + Messages.getString("Field.13"));
                        }
                    }
                }
                else {
                    stringVal = StringUtils.toAsciiString(this.buffer, stringStart, stringLength);
                }
            }
            else {
                stringVal = StringUtils.toAsciiString(this.buffer, stringStart, stringLength);
            }
        }
        else {
            stringVal = StringUtils.toAsciiString(this.buffer, stringStart, stringLength);
        }
        return stringVal;
    }
    
    public String getTable() throws SQLException {
        return this.getTableName();
    }
    
    public String getTableName() throws SQLException {
        if (this.tableName == null) {
            this.tableName = this.getStringFromBytes(this.tableNameStart, this.tableNameLength);
        }
        return this.tableName;
    }
    
    public String getTableNameNoAliases() throws SQLException {
        if (this.connection.versionMeetsMinimum(4, 1, 0)) {
            return this.getOriginalTableName();
        }
        return this.getTableName();
    }
    
    public String getComplexSchemaName() throws SQLException {
        return this.getStringFromBytes(this.complexSchemaNameStart, this.complexSchemaNameLength);
    }
    
    public String getComplexTypeName() throws SQLException {
        return this.getStringFromBytes(this.complexTypeNameStart, this.complexTypeNameLength);
    }
    
    public int getComplexVersion() throws SQLException {
        return this.complexVersion;
    }
    
    public boolean isAutoIncrement() {
        return (this.colFlag & 0x200) > 0;
    }
    
    public boolean isBinary() {
        if (this.getMysqlType() == 251) {
            return this.collationIndex == 63;
        }
        return (this.colFlag & 0x80) > 0;
    }
    
    public boolean isBlob() {
        if (this.getMysqlType() == 251) {
            return this.collationIndex == 63;
        }
        return (this.colFlag & 0x10) > 0;
    }
    
    private boolean isImplicitTemporaryTable() {
        return this.isImplicitTempTable;
    }
    
    public boolean isMultipleKey() {
        return (this.colFlag & 0x8) > 0;
    }
    
    boolean isNotNull() {
        return (this.colFlag & 0x1) > 0;
    }
    
    boolean isOpaqueBinary() throws SQLException {
        if (this.collationIndex == 63 && this.isBinary() && (this.getMysqlType() == 254 || this.getMysqlType() == 253)) {
            return (this.originalTableNameLength != 0 || this.connection == null || this.connection.versionMeetsMinimum(5, 0, 25)) && !this.isImplicitTemporaryTable();
        }
        return this.connection.versionMeetsMinimum(4, 1, 0) && "binary".equalsIgnoreCase(this.getEncoding());
    }
    
    public boolean isPrimaryKey() {
        return (this.colFlag & 0x2) > 0;
    }
    
    boolean isReadOnly() throws SQLException {
        if (this.connection.versionMeetsMinimum(4, 1, 0)) {
            final String orgColumnName = this.getOriginalName();
            final String orgTableName = this.getOriginalTableName();
            return orgColumnName == null || orgColumnName.length() <= 0 || orgTableName == null || orgTableName.length() <= 0;
        }
        return false;
    }
    
    public boolean isUniqueKey() {
        return (this.colFlag & 0x4) > 0;
    }
    
    public boolean isUnsigned() {
        return (this.colFlag & 0x20) > 0;
    }
    
    public void setUnsigned() {
        this.colFlag |= 0x20;
    }
    
    public boolean isZeroFill() {
        return (this.colFlag & 0x40) > 0;
    }
    
    private void setBlobTypeBasedOnLength() {
        if (this.length == 255L) {
            this.mysqlType = 249;
        }
        else if (this.length == 65535L) {
            this.mysqlType = 252;
        }
        else if (this.length == 16777215L) {
            this.mysqlType = 250;
        }
        else if (this.length == 4294967295L) {
            this.mysqlType = 251;
        }
    }
    
    private boolean isNativeNumericType() {
        return (this.mysqlType >= 1 && this.mysqlType <= 5) || this.mysqlType == 8 || this.mysqlType == 13;
    }
    
    private boolean isNativeDateTimeType() {
        return this.mysqlType == 10 || this.mysqlType == 14 || this.mysqlType == 12 || this.mysqlType == 11 || this.mysqlType == 7;
    }
    
    public void setConnection(final MySQLConnection conn) {
        this.connection = conn;
        if (this.encoding == null || this.collationIndex == 0) {
            this.encoding = this.connection.getEncoding();
        }
    }
    
    public MySQLConnection getConnect() {
        return this.connection;
    }
    
    void setMysqlType(final int type) {
        this.mysqlType = type;
        this.sqlType = MysqlDefs.mysqlToJavaType(this.mysqlType);
    }
    
    protected void setUseOldNameMetadata(final boolean useOldNameMetadata) {
        this.useOldNameMetadata = useOldNameMetadata;
    }
    
    @Override
    public String toString() {
        try {
            final StringBuilder asString = new StringBuilder();
            asString.append(super.toString());
            asString.append("[");
            asString.append("catalog=");
            asString.append(this.getDatabaseName());
            asString.append(",tableName=");
            asString.append(this.getTableName());
            asString.append(",originalTableName=");
            asString.append(this.getOriginalTableName());
            asString.append(",columnName=");
            asString.append(this.getName());
            asString.append(",originalColumnName=");
            asString.append(this.getOriginalName());
            asString.append(",mysqlType=");
            asString.append(this.getMysqlType());
            asString.append("(");
            asString.append(MysqlDefs.typeToName(this.getMysqlType()));
            asString.append(")");
            asString.append(",flags=");
            if (this.isAutoIncrement()) {
                asString.append(" AUTO_INCREMENT");
            }
            if (this.isPrimaryKey()) {
                asString.append(" PRIMARY_KEY");
            }
            if (this.isUniqueKey()) {
                asString.append(" UNIQUE_KEY");
            }
            if (this.isBinary()) {
                asString.append(" BINARY");
            }
            if (this.isBlob()) {
                asString.append(" BLOB");
            }
            if (this.isMultipleKey()) {
                asString.append(" MULTI_KEY");
            }
            if (this.isUnsigned()) {
                asString.append(" UNSIGNED");
            }
            if (this.isZeroFill()) {
                asString.append(" ZEROFILL");
            }
            asString.append(", charsetIndex=");
            asString.append(this.collationIndex);
            asString.append(", charsetName=");
            asString.append(this.encoding);
            asString.append("]");
            return asString.toString();
        }
        catch (Throwable t) {
            return super.toString();
        }
    }
    
    protected boolean isSingleBit() {
        return this.isSingleBit;
    }
    
    protected boolean getvalueNeedsQuoting() {
        return this.valueNeedsQuoting;
    }
    
    private boolean determineNeedsQuoting() {
        boolean retVal = false;
        switch (this.sqlType) {
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
                retVal = false;
                break;
            }
            default: {
                retVal = true;
                break;
            }
        }
        return retVal;
    }
    
    public Object clone() throws CloneNotSupportedException {
        final Field field = (Field)super.clone();
        field.encoding = ((this.encoding == null) ? null : new String(this.encoding));
        field.collationName = ((this.collationName == null) ? null : new String(this.collationName));
        field.databaseName = ((this.databaseName == null) ? null : new String(this.databaseName));
        field.fullName = ((this.fullName == null) ? null : new String(this.fullName));
        field.fullOriginalName = ((this.fullOriginalName == null) ? null : new String(this.fullOriginalName));
        field.name = ((this.name == null) ? null : new String(this.name));
        field.originalColumnName = ((this.originalColumnName == null) ? null : new String(this.originalColumnName));
        field.originalTableName = ((this.originalTableName == null) ? null : new String(this.originalTableName));
        field.tableName = ((this.tableName == null) ? null : new String(this.tableName));
        field.connection = null;
        return field;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        final Field other = (Field)obj;
        if (this.collationIndex != other.collationIndex) {
            return false;
        }
        if (this.encoding == null) {
            if (other.encoding != null) {
                return false;
            }
        }
        else if (!this.encoding.equals(other.encoding)) {
            return false;
        }
        if (this.colDecimals != other.colDecimals) {
            return false;
        }
        if (this.colFlag != other.colFlag) {
            return false;
        }
        if (this.collationName == null) {
            if (other.collationName != null) {
                return false;
            }
        }
        else if (!this.collationName.equals(other.collationName)) {
            return false;
        }
        if (this.databaseName == null) {
            if (other.databaseName != null) {
                return false;
            }
        }
        else if (!this.databaseName.equals(other.databaseName)) {
            return false;
        }
        if (this.databaseNameLength != other.databaseNameLength) {
            return false;
        }
        if (this.databaseNameStart != other.databaseNameStart) {
            return false;
        }
        if (this.defaultValueLength != other.defaultValueLength) {
            return false;
        }
        if (this.defaultValueStart != other.defaultValueStart) {
            return false;
        }
        if (this.fullName == null) {
            if (other.fullName != null) {
                return false;
            }
        }
        else if (!this.fullName.equals(other.fullName)) {
            return false;
        }
        if (this.fullOriginalName == null) {
            if (other.fullOriginalName != null) {
                return false;
            }
        }
        else if (!this.fullOriginalName.equals(other.fullOriginalName)) {
            return false;
        }
        if (this.isImplicitTempTable != other.isImplicitTempTable) {
            return false;
        }
        if (this.length != other.length) {
            return false;
        }
        if (this.mysqlType != other.mysqlType) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!this.name.equals(other.name)) {
            return false;
        }
        if (this.nameLength != other.nameLength) {
            return false;
        }
        if (this.nameStart != other.nameStart) {
            return false;
        }
        if (this.originalColumnName == null) {
            if (other.originalColumnName != null) {
                return false;
            }
        }
        else if (!this.originalColumnName.equals(other.originalColumnName)) {
            return false;
        }
        if (this.originalColumnNameLength != other.originalColumnNameLength) {
            return false;
        }
        if (this.originalColumnNameStart != other.originalColumnNameStart) {
            return false;
        }
        if (this.originalTableName == null) {
            if (other.originalTableName != null) {
                return false;
            }
        }
        else if (!this.originalTableName.equals(other.originalTableName)) {
            return false;
        }
        if (this.originalTableNameLength != other.originalTableNameLength) {
            return false;
        }
        if (this.originalTableNameStart != other.originalTableNameStart) {
            return false;
        }
        if (this.precisionAdjustFactor != other.precisionAdjustFactor) {
            return false;
        }
        if (this.sqlType != other.sqlType) {
            return false;
        }
        if (this.tableName == null) {
            if (other.tableName != null) {
                return false;
            }
        }
        else if (!this.tableName.equals(other.tableName)) {
            return false;
        }
        return this.tableNameLength == other.tableNameLength && this.tableNameStart == other.tableNameStart && this.useOldNameMetadata == other.useOldNameMetadata && this.isSingleBit == other.isSingleBit && this.maxBytesPerChar == other.maxBytesPerChar;
    }
}
