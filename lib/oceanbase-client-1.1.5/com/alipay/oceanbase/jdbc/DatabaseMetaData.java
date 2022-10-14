// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.RowIdLifetime;
import java.util.Set;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map;
import java.util.HashMap;
import java.sql.PreparedStatement;
import java.util.Locale;
import java.util.Iterator;
import java.util.Collections;
import java.io.UnsupportedEncodingException;
import java.sql.Statement;
import java.util.StringTokenizer;
import java.util.List;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.sql.SQLException;
import java.lang.reflect.Constructor;

public class DatabaseMetaData implements java.sql.DatabaseMetaData
{
    protected static final int MAX_IDENTIFIER_LENGTH = 64;
    private static final int DEFERRABILITY = 13;
    private static final int DELETE_RULE = 10;
    private static final int FK_NAME = 11;
    private static final int FKCOLUMN_NAME = 7;
    private static final int FKTABLE_CAT = 4;
    private static final int FKTABLE_NAME = 6;
    private static final int FKTABLE_SCHEM = 5;
    private static final int KEY_SEQ = 8;
    private static final int PK_NAME = 12;
    private static final int PKCOLUMN_NAME = 3;
    private static final int PKTABLE_CAT = 0;
    private static final int PKTABLE_NAME = 2;
    private static final int PKTABLE_SCHEM = 1;
    private static final String SUPPORTS_FK = "SUPPORTS_FK";
    protected static final byte[] TABLE_AS_BYTES;
    protected static final byte[] SYSTEM_TABLE_AS_BYTES;
    private static final int UPDATE_RULE = 9;
    protected static final byte[] VIEW_AS_BYTES;
    private static final Constructor<?> JDBC_4_DBMD_SHOW_CTOR;
    private static final Constructor<?> JDBC_4_DBMD_IS_CTOR;
    private static final String[] MYSQL_KEYWORDS;
    private static final String[] SQL92_KEYWORDS;
    private static final String[] SQL2003_KEYWORDS;
    private static volatile String mysqlKeywords;
    protected MySQLConnection conn;
    protected String database;
    protected final String quotedId;
    private ExceptionInterceptor exceptionInterceptor;
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }
    
    protected static DatabaseMetaData getInstance(final MySQLConnection connToSet, final String databaseToSet, final boolean checkForInfoSchema) throws SQLException {
        if (!Util.isJdbc4()) {
            if (checkForInfoSchema && connToSet.getUseInformationSchema() && connToSet.versionMeetsMinimum(5, 0, 7)) {
                return new DatabaseMetaDataUsingInfoSchema(connToSet, databaseToSet);
            }
            return new DatabaseMetaData(connToSet, databaseToSet);
        }
        else {
            if (checkForInfoSchema && connToSet.getUseInformationSchema() && connToSet.versionMeetsMinimum(5, 0, 7)) {
                return (DatabaseMetaData)Util.handleNewInstance(DatabaseMetaData.JDBC_4_DBMD_IS_CTOR, new Object[] { connToSet, databaseToSet }, connToSet.getExceptionInterceptor());
            }
            return (DatabaseMetaData)Util.handleNewInstance(DatabaseMetaData.JDBC_4_DBMD_SHOW_CTOR, new Object[] { connToSet, databaseToSet }, connToSet.getExceptionInterceptor());
        }
    }
    
    protected DatabaseMetaData(final MySQLConnection connToSet, final String databaseToSet) {
        this.database = null;
        this.conn = connToSet;
        this.database = databaseToSet;
        this.exceptionInterceptor = this.conn.getExceptionInterceptor();
        String identifierQuote = null;
        try {
            identifierQuote = this.getIdentifierQuoteString();
        }
        catch (SQLException sqlEx) {
            AssertionFailedException.shouldNotHappen(sqlEx);
        }
        finally {
            this.quotedId = identifierQuote;
        }
    }
    
    @Override
    public boolean allProceduresAreCallable() throws SQLException {
        return false;
    }
    
    @Override
    public boolean allTablesAreSelectable() throws SQLException {
        return false;
    }
    
    private ResultSet buildResultSet(final Field[] fields, final ArrayList<ResultSetRow> rows) throws SQLException {
        return buildResultSet(fields, rows, this.conn);
    }
    
    static ResultSet buildResultSet(final Field[] fields, final ArrayList<ResultSetRow> rows, final MySQLConnection c) throws SQLException {
        for (int fieldsLength = fields.length, i = 0; i < fieldsLength; ++i) {
            final int jdbcType = fields[i].getSQLType();
            switch (jdbcType) {
                case -1:
                case 1:
                case 12:
                case 2005: {
                    fields[i].setEncoding(c.getCharacterSetMetadata(), c);
                    break;
                }
            }
            fields[i].setConnection(c);
            fields[i].setUseOldNameMetadata(true);
        }
        return ResultSetImpl.getInstance(c.getCatalog(), fields, new RowDataStatic(rows), c, null, false);
    }
    
    protected void convertToJdbcFunctionList(final String catalog, final ResultSet proceduresRs, final boolean needsClientFiltering, final String db, final List<ComparableWrapper<String, ResultSetRow>> procedureRows, final int nameIndex, final Field[] fields) throws SQLException {
        while (proceduresRs.next()) {
            boolean shouldAdd = true;
            if (needsClientFiltering) {
                shouldAdd = false;
                final String procDb = proceduresRs.getString(1);
                if (db == null && procDb == null) {
                    shouldAdd = true;
                }
                else if (db != null && db.equals(procDb)) {
                    shouldAdd = true;
                }
            }
            if (shouldAdd) {
                final String functionName = proceduresRs.getString(nameIndex);
                byte[][] rowData = null;
                if (fields != null && fields.length == 9) {
                    rowData = new byte[][] { (catalog == null) ? null : this.s2b(catalog), null, this.s2b(functionName), null, null, null, this.s2b(proceduresRs.getString("comment")), this.s2b(Integer.toString(2)), this.s2b(functionName) };
                }
                else {
                    rowData = new byte[][] { (catalog == null) ? null : this.s2b(catalog), null, this.s2b(functionName), this.s2b(proceduresRs.getString("comment")), this.s2b(Integer.toString(this.getJDBC4FunctionNoTableConstant())), this.s2b(functionName) };
                }
                procedureRows.add(new ComparableWrapper<String, ResultSetRow>(this.getFullyQualifiedName(catalog, functionName), new ByteArrayRow(rowData, this.getExceptionInterceptor())));
            }
        }
    }
    
    protected String getFullyQualifiedName(final String catalog, final String entity) {
        final StringBuilder fullyQualifiedName = new StringBuilder(StringUtils.quoteIdentifier((catalog == null) ? "" : catalog, this.quotedId, this.conn.getPedantic()));
        fullyQualifiedName.append('.');
        fullyQualifiedName.append(StringUtils.quoteIdentifier(entity, this.quotedId, this.conn.getPedantic()));
        return fullyQualifiedName.toString();
    }
    
    protected int getJDBC4FunctionNoTableConstant() {
        return 0;
    }
    
    protected void convertToJdbcProcedureList(final boolean fromSelect, final String catalog, final ResultSet proceduresRs, final boolean needsClientFiltering, final String db, final List<ComparableWrapper<String, ResultSetRow>> procedureRows, final int nameIndex) throws SQLException {
        while (proceduresRs.next()) {
            boolean shouldAdd = true;
            if (needsClientFiltering) {
                shouldAdd = false;
                final String procDb = proceduresRs.getString(1);
                if (db == null && procDb == null) {
                    shouldAdd = true;
                }
                else if (db != null && db.equals(procDb)) {
                    shouldAdd = true;
                }
            }
            if (shouldAdd) {
                final String procedureName = proceduresRs.getString(nameIndex);
                final byte[][] rowData = { (catalog == null) ? null : this.s2b(catalog), null, this.s2b(procedureName), null, null, null, this.s2b(proceduresRs.getString("comment")), null, null };
                final boolean isFunction = fromSelect && "FUNCTION".equalsIgnoreCase(proceduresRs.getString("type"));
                rowData[7] = this.s2b(isFunction ? Integer.toString(2) : Integer.toString(1));
                rowData[8] = this.s2b(procedureName);
                procedureRows.add(new ComparableWrapper<String, ResultSetRow>(this.getFullyQualifiedName(catalog, procedureName), new ByteArrayRow(rowData, this.getExceptionInterceptor())));
            }
        }
    }
    
    private ResultSetRow convertTypeDescriptorToProcedureRow(final byte[] procNameAsBytes, final byte[] procCatAsBytes, final String paramName, final boolean isOutParam, final boolean isInParam, final boolean isReturnParam, final TypeDescriptor typeDesc, final boolean forGetFunctionColumns, final int ordinal) throws SQLException {
        final byte[][] row = forGetFunctionColumns ? new byte[17][] : new byte[20][];
        row[0] = procCatAsBytes;
        row[1] = null;
        row[2] = procNameAsBytes;
        row[3] = this.s2b(paramName);
        row[4] = this.s2b(String.valueOf(this.getColumnType(isOutParam, isInParam, isReturnParam, forGetFunctionColumns)));
        row[5] = this.s2b(Short.toString(typeDesc.dataType));
        row[6] = this.s2b(typeDesc.typeName);
        row[8] = (row[7] = (byte[])((typeDesc.columnSize == null) ? null : this.s2b(typeDesc.columnSize.toString())));
        row[9] = (byte[])((typeDesc.decimalDigits == null) ? null : this.s2b(typeDesc.decimalDigits.toString()));
        row[10] = this.s2b(Integer.toString(typeDesc.numPrecRadix));
        switch (typeDesc.nullability) {
            case 0: {
                row[11] = this.s2b(String.valueOf(0));
                break;
            }
            case 1: {
                row[11] = this.s2b(String.valueOf(1));
                break;
            }
            case 2: {
                row[11] = this.s2b(String.valueOf(2));
                break;
            }
            default: {
                throw SQLError.createSQLException("Internal error while parsing callable statement metadata (unknown nullability value fount)", "S1000", this.getExceptionInterceptor());
            }
        }
        row[12] = null;
        if (forGetFunctionColumns) {
            row[13] = null;
            row[14] = this.s2b(String.valueOf(ordinal));
            row[15] = this.s2b(typeDesc.isNullable);
            row[16] = procNameAsBytes;
        }
        else {
            row[14] = (row[13] = null);
            row[16] = (row[15] = null);
            row[17] = this.s2b(String.valueOf(ordinal));
            row[18] = this.s2b(typeDesc.isNullable);
            row[19] = procNameAsBytes;
        }
        return new ByteArrayRow(row, this.getExceptionInterceptor());
    }
    
    protected int getColumnType(final boolean isOutParam, final boolean isInParam, final boolean isReturnParam, final boolean forGetFunctionColumns) {
        if (isInParam && isOutParam) {
            return 2;
        }
        if (isInParam) {
            return 1;
        }
        if (isOutParam) {
            return 4;
        }
        if (isReturnParam) {
            return 5;
        }
        return 0;
    }
    
    protected ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }
    
    @Override
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException {
        return true;
    }
    
    @Override
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean deletesAreDetected(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException {
        return true;
    }
    
    public List<ResultSetRow> extractForeignKeyForTable(final ArrayList<ResultSetRow> rows, final ResultSet rs, final String catalog) throws SQLException {
        final byte[][] row = { rs.getBytes(1), this.s2b("SUPPORTS_FK"), null };
        final String createTableString = rs.getString(2);
        final StringTokenizer lineTokenizer = new StringTokenizer(createTableString, "\n");
        final StringBuilder commentBuf = new StringBuilder("comment; ");
        boolean firstTime = true;
        while (lineTokenizer.hasMoreTokens()) {
            String line = lineTokenizer.nextToken().trim();
            String constraintName = null;
            if (StringUtils.startsWithIgnoreCase(line, "CONSTRAINT")) {
                boolean usingBackTicks = true;
                int beginPos = StringUtils.indexOfQuoteDoubleAware(line, this.quotedId, 0);
                if (beginPos == -1) {
                    beginPos = line.indexOf("\"");
                    usingBackTicks = false;
                }
                if (beginPos != -1) {
                    int endPos = -1;
                    if (usingBackTicks) {
                        endPos = StringUtils.indexOfQuoteDoubleAware(line, this.quotedId, beginPos + 1);
                    }
                    else {
                        endPos = StringUtils.indexOfQuoteDoubleAware(line, "\"", beginPos + 1);
                    }
                    if (endPos != -1) {
                        constraintName = line.substring(beginPos + 1, endPos);
                        line = line.substring(endPos + 1, line.length()).trim();
                    }
                }
            }
            if (line.startsWith("FOREIGN KEY")) {
                if (line.endsWith(",")) {
                    line = line.substring(0, line.length() - 1);
                }
                final int indexOfFK = line.indexOf("FOREIGN KEY");
                String localColumnName = null;
                String referencedCatalogName = StringUtils.quoteIdentifier(catalog, this.quotedId, this.conn.getPedantic());
                String referencedTableName = null;
                String referencedColumnName = null;
                if (indexOfFK != -1) {
                    final int afterFk = indexOfFK + "FOREIGN KEY".length();
                    final int indexOfRef = StringUtils.indexOfIgnoreCase(afterFk, line, "REFERENCES", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
                    if (indexOfRef != -1) {
                        final int indexOfParenOpen = line.indexOf(40, afterFk);
                        final int indexOfParenClose = StringUtils.indexOfIgnoreCase(indexOfParenOpen, line, ")", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
                        if (indexOfParenOpen == -1 || indexOfParenClose == -1) {}
                        localColumnName = line.substring(indexOfParenOpen + 1, indexOfParenClose);
                        final int afterRef = indexOfRef + "REFERENCES".length();
                        final int referencedColumnBegin = StringUtils.indexOfIgnoreCase(afterRef, line, "(", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
                        if (referencedColumnBegin != -1) {
                            referencedTableName = line.substring(afterRef, referencedColumnBegin);
                            final int referencedColumnEnd = StringUtils.indexOfIgnoreCase(referencedColumnBegin + 1, line, ")", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
                            if (referencedColumnEnd != -1) {
                                referencedColumnName = line.substring(referencedColumnBegin + 1, referencedColumnEnd);
                            }
                            final int indexOfCatalogSep = StringUtils.indexOfIgnoreCase(0, referencedTableName, ".", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
                            if (indexOfCatalogSep != -1) {
                                referencedCatalogName = referencedTableName.substring(0, indexOfCatalogSep);
                                referencedTableName = referencedTableName.substring(indexOfCatalogSep + 1);
                            }
                        }
                    }
                }
                if (!firstTime) {
                    commentBuf.append("; ");
                }
                else {
                    firstTime = false;
                }
                if (constraintName != null) {
                    commentBuf.append(constraintName);
                }
                else {
                    commentBuf.append("not_available");
                }
                commentBuf.append("(");
                commentBuf.append(localColumnName);
                commentBuf.append(") REFER ");
                commentBuf.append(referencedCatalogName);
                commentBuf.append("/");
                commentBuf.append(referencedTableName);
                commentBuf.append("(");
                commentBuf.append(referencedColumnName);
                commentBuf.append(")");
                final int lastParenIndex = line.lastIndexOf(")");
                if (lastParenIndex == line.length() - 1) {
                    continue;
                }
                final String cascadeOptions = line.substring(lastParenIndex + 1);
                commentBuf.append(" ");
                commentBuf.append(cascadeOptions);
            }
        }
        row[2] = this.s2b(commentBuf.toString());
        rows.add(new ByteArrayRow(row, this.getExceptionInterceptor()));
        return rows;
    }
    
    public ResultSet extractForeignKeyFromCreateTable(final String catalog, final String tableName) throws SQLException {
        final ArrayList<String> tableList = new ArrayList<String>();
        ResultSet rs = null;
        Statement stmt = null;
        if (tableName != null) {
            tableList.add(tableName);
        }
        else {
            try {
                rs = this.getTables(catalog, "", "%", new String[] { "TABLE" });
                while (rs.next()) {
                    tableList.add(rs.getString("TABLE_NAME"));
                }
            }
            finally {
                if (rs != null) {
                    rs.close();
                }
                rs = null;
            }
        }
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        final Field[] fields = { new Field("", "Name", 1, Integer.MAX_VALUE), new Field("", "Type", 1, 255), new Field("", "Comment", 1, Integer.MAX_VALUE) };
        final int numTables = tableList.size();
        stmt = this.conn.getMetadataSafeStatement();
        try {
            for (int i = 0; i < numTables; ++i) {
                final String tableToExtract = tableList.get(i);
                final String query = "SHOW CREATE TABLE " + this.getFullyQualifiedName(catalog, tableToExtract);
                try {
                    rs = stmt.executeQuery(query);
                }
                catch (SQLException sqlEx) {
                    final String sqlState = sqlEx.getSQLState();
                    if (!"42S02".equals(sqlState) && sqlEx.getErrorCode() != 1146) {
                        throw sqlEx;
                    }
                    continue;
                }
                while (rs.next()) {
                    this.extractForeignKeyForTable(rows, rs, catalog);
                }
            }
        }
        finally {
            if (rs != null) {
                rs.close();
            }
            rs = null;
            if (stmt != null) {
                stmt.close();
            }
            stmt = null;
        }
        return this.buildResultSet(fields, rows);
    }
    
    @Override
    public ResultSet getAttributes(final String arg0, final String arg1, final String arg2, final String arg3) throws SQLException {
        final Field[] fields = { new Field("", "TYPE_CAT", 1, 32), new Field("", "TYPE_SCHEM", 1, 32), new Field("", "TYPE_NAME", 1, 32), new Field("", "ATTR_NAME", 1, 32), new Field("", "DATA_TYPE", 5, 32), new Field("", "ATTR_TYPE_NAME", 1, 32), new Field("", "ATTR_SIZE", 4, 32), new Field("", "DECIMAL_DIGITS", 4, 32), new Field("", "NUM_PREC_RADIX", 4, 32), new Field("", "NULLABLE ", 4, 32), new Field("", "REMARKS", 1, 32), new Field("", "ATTR_DEF", 1, 32), new Field("", "SQL_DATA_TYPE", 4, 32), new Field("", "SQL_DATETIME_SUB", 4, 32), new Field("", "CHAR_OCTET_LENGTH", 4, 32), new Field("", "ORDINAL_POSITION", 4, 32), new Field("", "IS_NULLABLE", 1, 32), new Field("", "SCOPE_CATALOG", 1, 32), new Field("", "SCOPE_SCHEMA", 1, 32), new Field("", "SCOPE_TABLE", 1, 32), new Field("", "SOURCE_DATA_TYPE", 5, 32) };
        return this.buildResultSet(fields, new ArrayList<ResultSetRow>());
    }
    
    @Override
    public ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
        }
        final Field[] fields = { new Field("", "SCOPE", 5, 5), new Field("", "COLUMN_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 32), new Field("", "TYPE_NAME", 1, 32), new Field("", "COLUMN_SIZE", 4, 10), new Field("", "BUFFER_LENGTH", 4, 10), new Field("", "DECIMAL_DIGITS", 5, 10), new Field("", "PSEUDO_COLUMN", 5, 5) };
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        final Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)) {
                @Override
                void forEach(final String catalogStr) throws SQLException {
                    ResultSet results = null;
                    try {
                        final StringBuilder queryBuf = new StringBuilder("SHOW COLUMNS FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(table, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        queryBuf.append(" FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        results = stmt.executeQuery(queryBuf.toString());
                        while (results.next()) {
                            final String keyType = results.getString("Key");
                            if (keyType != null && StringUtils.startsWithIgnoreCase(keyType, "PRI")) {
                                final byte[][] rowVal = new byte[8][];
                                rowVal[0] = Integer.toString(2).getBytes();
                                rowVal[1] = results.getBytes("Field");
                                String type = results.getString("Type");
                                int size = MysqlIO.getMaxBuf();
                                int decimals = 0;
                                if (type.indexOf("enum") != -1) {
                                    final String temp = type.substring(type.indexOf("("), type.indexOf(")"));
                                    final StringTokenizer tokenizer = new StringTokenizer(temp, ",");
                                    int maxLength = 0;
                                    while (tokenizer.hasMoreTokens()) {
                                        maxLength = Math.max(maxLength, tokenizer.nextToken().length() - 2);
                                    }
                                    size = maxLength;
                                    decimals = 0;
                                    type = "enum";
                                }
                                else if (type.indexOf("(") != -1) {
                                    if (type.indexOf(",") != -1) {
                                        size = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(",")));
                                        decimals = Integer.parseInt(type.substring(type.indexOf(",") + 1, type.indexOf(")")));
                                    }
                                    else {
                                        size = Integer.parseInt(type.substring(type.indexOf("(") + 1, type.indexOf(")")));
                                    }
                                    type = type.substring(0, type.indexOf("("));
                                }
                                rowVal[2] = DatabaseMetaData.this.s2b(String.valueOf(MysqlDefs.mysqlToJavaType(type)));
                                rowVal[3] = DatabaseMetaData.this.s2b(type);
                                rowVal[4] = Integer.toString(size + decimals).getBytes();
                                rowVal[5] = Integer.toString(size + decimals).getBytes();
                                rowVal[6] = Integer.toString(decimals).getBytes();
                                rowVal[7] = Integer.toString(1).getBytes();
                                rows.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                            }
                        }
                    }
                    catch (SQLException sqlEx) {
                        if (!"42S02".equals(sqlEx.getSQLState())) {
                            throw sqlEx;
                        }
                    }
                    finally {
                        if (results != null) {
                            try {
                                results.close();
                            }
                            catch (Exception ex) {}
                            results = null;
                        }
                    }
                }
            }.doForAll();
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        final ResultSet results = this.buildResultSet(fields, rows);
        return results;
    }
    
    private void getCallStmtParameterTypes(String catalog, String quotedProcName, final ProcedureType procType, String parameterNamePattern, final List<ResultSetRow> resultRows, final boolean forGetFunctionColumns) throws SQLException {
        Statement paramRetrievalStmt = null;
        ResultSet paramRetrievalRs = null;
        if (parameterNamePattern == null) {
            if (!this.conn.getNullNamePatternMatchesAll()) {
                throw SQLError.createSQLException("Parameter/Column name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
            }
            parameterNamePattern = "%";
        }
        String parameterDef = null;
        byte[] procNameAsBytes = null;
        byte[] procCatAsBytes = null;
        boolean isProcedureInAnsiMode = false;
        String storageDefnDelims = null;
        String storageDefnClosures = null;
        try {
            paramRetrievalStmt = this.conn.getMetadataSafeStatement();
            final String oldCatalog = this.conn.getCatalog();
            if (this.conn.lowerCaseTableNames() && catalog != null && catalog.length() != 0 && oldCatalog != null && oldCatalog.length() != 0) {
                ResultSet rs = null;
                try {
                    this.conn.setCatalog(StringUtils.unQuoteIdentifier(catalog, this.quotedId));
                    rs = paramRetrievalStmt.executeQuery("SELECT DATABASE()");
                    rs.next();
                    catalog = rs.getString(1);
                }
                finally {
                    this.conn.setCatalog(oldCatalog);
                    if (rs != null) {
                        rs.close();
                    }
                }
            }
            if (paramRetrievalStmt.getMaxRows() != 0) {
                paramRetrievalStmt.setMaxRows(0);
            }
            int dotIndex = -1;
            if (!" ".equals(this.quotedId)) {
                dotIndex = StringUtils.indexOfIgnoreCase(0, quotedProcName, ".", this.quotedId, this.quotedId, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
            }
            else {
                dotIndex = quotedProcName.indexOf(".");
            }
            String dbName = null;
            if (dotIndex != -1 && dotIndex + 1 < quotedProcName.length()) {
                dbName = quotedProcName.substring(0, dotIndex);
                quotedProcName = quotedProcName.substring(dotIndex + 1);
            }
            else {
                dbName = StringUtils.quoteIdentifier(catalog, this.quotedId, this.conn.getPedantic());
            }
            String tmpProcName = StringUtils.unQuoteIdentifier(quotedProcName, this.quotedId);
            try {
                procNameAsBytes = StringUtils.getBytes(tmpProcName, "UTF-8");
            }
            catch (UnsupportedEncodingException ueEx) {
                procNameAsBytes = this.s2b(tmpProcName);
            }
            tmpProcName = StringUtils.unQuoteIdentifier(dbName, this.quotedId);
            try {
                procCatAsBytes = StringUtils.getBytes(tmpProcName, "UTF-8");
            }
            catch (UnsupportedEncodingException ueEx) {
                procCatAsBytes = this.s2b(tmpProcName);
            }
            final StringBuilder procNameBuf = new StringBuilder();
            procNameBuf.append(dbName);
            procNameBuf.append('.');
            procNameBuf.append(quotedProcName);
            String fieldName = null;
            if (procType == ProcedureType.PROCEDURE) {
                paramRetrievalRs = paramRetrievalStmt.executeQuery("SHOW CREATE PROCEDURE " + procNameBuf.toString());
                fieldName = "Create Procedure";
            }
            else {
                paramRetrievalRs = paramRetrievalStmt.executeQuery("SHOW CREATE FUNCTION " + procNameBuf.toString());
                fieldName = "Create Function";
            }
            if (paramRetrievalRs.next()) {
                String procedureDef = paramRetrievalRs.getString(fieldName);
                if (!this.conn.getNoAccessToProcedureBodies() && (procedureDef == null || procedureDef.length() == 0)) {
                    throw SQLError.createSQLException("User does not have access to metadata required to determine stored procedure parameter types. If rights can not be granted, configure connection with \"noAccessToProcedureBodies=true\" to have driver generate parameters that represent INOUT strings irregardless of actual parameter types.", "S1000", this.getExceptionInterceptor());
                }
                try {
                    final String sqlMode = paramRetrievalRs.getString("sql_mode");
                    if (StringUtils.indexOfIgnoreCase(sqlMode, "ANSI") != -1) {
                        isProcedureInAnsiMode = true;
                    }
                }
                catch (SQLException ex) {}
                final String identifierMarkers = isProcedureInAnsiMode ? "`\"" : "`";
                final String identifierAndStringMarkers = "'" + identifierMarkers;
                storageDefnDelims = "(" + identifierMarkers;
                storageDefnClosures = ")" + identifierMarkers;
                if (procedureDef != null && procedureDef.length() != 0) {
                    procedureDef = StringUtils.stripComments(procedureDef, identifierAndStringMarkers, identifierAndStringMarkers, true, false, true, true);
                    final int openParenIndex = StringUtils.indexOfIgnoreCase(0, procedureDef, "(", this.quotedId, this.quotedId, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
                    int endOfParamDeclarationIndex = 0;
                    endOfParamDeclarationIndex = this.endPositionOfParameterDeclaration(openParenIndex, procedureDef, this.quotedId);
                    if (procType == ProcedureType.FUNCTION) {
                        final int returnsIndex = StringUtils.indexOfIgnoreCase(0, procedureDef, " RETURNS ", this.quotedId, this.quotedId, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
                        final int endReturnsDef = this.findEndOfReturnsClause(procedureDef, returnsIndex);
                        int declarationStart;
                        for (declarationStart = returnsIndex + "RETURNS ".length(); declarationStart < procedureDef.length() && Character.isWhitespace(procedureDef.charAt(declarationStart)); ++declarationStart) {}
                        final String returnsDefn = procedureDef.substring(declarationStart, endReturnsDef).trim();
                        final TypeDescriptor returnDescriptor = new TypeDescriptor(returnsDefn, "YES");
                        resultRows.add(this.convertTypeDescriptorToProcedureRow(procNameAsBytes, procCatAsBytes, "", false, false, true, returnDescriptor, forGetFunctionColumns, 0));
                    }
                    if (openParenIndex == -1 || endOfParamDeclarationIndex == -1) {
                        throw SQLError.createSQLException("Internal error when parsing callable statement metadata", "S1000", this.getExceptionInterceptor());
                    }
                    parameterDef = procedureDef.substring(openParenIndex + 1, endOfParamDeclarationIndex);
                }
            }
        }
        finally {
            SQLException sqlExRethrow = null;
            if (paramRetrievalRs != null) {
                try {
                    paramRetrievalRs.close();
                }
                catch (SQLException sqlEx) {
                    sqlExRethrow = sqlEx;
                }
                paramRetrievalRs = null;
            }
            if (paramRetrievalStmt != null) {
                try {
                    paramRetrievalStmt.close();
                }
                catch (SQLException sqlEx) {
                    sqlExRethrow = sqlEx;
                }
                paramRetrievalStmt = null;
            }
            if (sqlExRethrow != null) {
                throw sqlExRethrow;
            }
        }
        if (parameterDef != null) {
            int ordinal = 1;
            final List<String> parseList = StringUtils.split(parameterDef, ",", storageDefnDelims, storageDefnClosures, true);
            for (int parseListLen = parseList.size(), i = 0; i < parseListLen; ++i) {
                String declaration = parseList.get(i);
                if (declaration.trim().length() == 0) {
                    break;
                }
                declaration = declaration.replaceAll("[\\t\\n\\x0B\\f\\r]", " ");
                final StringTokenizer declarationTok = new StringTokenizer(declaration, " \t");
                String paramName = null;
                boolean isOutParam = false;
                boolean isInParam = false;
                if (!declarationTok.hasMoreTokens()) {
                    throw SQLError.createSQLException("Internal error when parsing callable statement metadata (unknown output from 'SHOW CREATE PROCEDURE')", "S1000", this.getExceptionInterceptor());
                }
                final String possibleParamName = declarationTok.nextToken();
                if (possibleParamName.equalsIgnoreCase("OUT")) {
                    isOutParam = true;
                    if (!declarationTok.hasMoreTokens()) {
                        throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", "S1000", this.getExceptionInterceptor());
                    }
                    paramName = declarationTok.nextToken();
                }
                else if (possibleParamName.equalsIgnoreCase("INOUT")) {
                    isOutParam = true;
                    isInParam = true;
                    if (!declarationTok.hasMoreTokens()) {
                        throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", "S1000", this.getExceptionInterceptor());
                    }
                    paramName = declarationTok.nextToken();
                }
                else if (possibleParamName.equalsIgnoreCase("IN")) {
                    isOutParam = false;
                    isInParam = true;
                    if (!declarationTok.hasMoreTokens()) {
                        throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter name)", "S1000", this.getExceptionInterceptor());
                    }
                    paramName = declarationTok.nextToken();
                }
                else {
                    isOutParam = false;
                    isInParam = true;
                    paramName = possibleParamName;
                }
                TypeDescriptor typeDesc = null;
                if (!declarationTok.hasMoreTokens()) {
                    throw SQLError.createSQLException("Internal error when parsing callable statement metadata (missing parameter type)", "S1000", this.getExceptionInterceptor());
                }
                final StringBuilder typeInfoBuf = new StringBuilder(declarationTok.nextToken());
                while (declarationTok.hasMoreTokens()) {
                    typeInfoBuf.append(" ");
                    typeInfoBuf.append(declarationTok.nextToken());
                }
                final String typeInfo = typeInfoBuf.toString();
                typeDesc = new TypeDescriptor(typeInfo, "YES");
                if ((paramName.startsWith("`") && paramName.endsWith("`")) || (isProcedureInAnsiMode && paramName.startsWith("\"") && paramName.endsWith("\""))) {
                    paramName = paramName.substring(1, paramName.length() - 1);
                }
                final int wildCompareRes = StringUtils.wildCompare(paramName, parameterNamePattern);
                if (wildCompareRes != -1) {
                    final ResultSetRow row = this.convertTypeDescriptorToProcedureRow(procNameAsBytes, procCatAsBytes, paramName, isOutParam, isInParam, false, typeDesc, forGetFunctionColumns, ordinal++);
                    resultRows.add(row);
                }
            }
        }
    }
    
    private int endPositionOfParameterDeclaration(final int beginIndex, final String procedureDef, final String quoteChar) throws SQLException {
        int currentPos = beginIndex + 1;
        int parenDepth = 1;
        while (parenDepth > 0 && currentPos < procedureDef.length()) {
            final int closedParenIndex = StringUtils.indexOfIgnoreCase(currentPos, procedureDef, ")", quoteChar, quoteChar, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
            if (closedParenIndex == -1) {
                throw SQLError.createSQLException("Internal error when parsing callable statement metadata", "S1000", this.getExceptionInterceptor());
            }
            final int nextOpenParenIndex = StringUtils.indexOfIgnoreCase(currentPos, procedureDef, "(", quoteChar, quoteChar, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
            if (nextOpenParenIndex != -1 && nextOpenParenIndex < closedParenIndex) {
                ++parenDepth;
                currentPos = closedParenIndex + 1;
            }
            else {
                --parenDepth;
                currentPos = closedParenIndex;
            }
        }
        return currentPos;
    }
    
    private int findEndOfReturnsClause(final String procedureDefn, final int positionOfReturnKeyword) throws SQLException {
        final String openingMarkers = this.quotedId + "(";
        final String closingMarkers = this.quotedId + ")";
        final String[] tokens = { "LANGUAGE", "NOT", "DETERMINISTIC", "CONTAINS", "NO", "READ", "MODIFIES", "SQL", "COMMENT", "BEGIN", "RETURN" };
        final int startLookingAt = positionOfReturnKeyword + "RETURNS".length() + 1;
        int endOfReturn = -1;
        for (int i = 0; i < tokens.length; ++i) {
            final int nextEndOfReturn = StringUtils.indexOfIgnoreCase(startLookingAt, procedureDefn, tokens[i], openingMarkers, closingMarkers, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
            if (nextEndOfReturn != -1 && (endOfReturn == -1 || nextEndOfReturn < endOfReturn)) {
                endOfReturn = nextEndOfReturn;
            }
        }
        if (endOfReturn != -1) {
            return endOfReturn;
        }
        endOfReturn = StringUtils.indexOfIgnoreCase(startLookingAt, procedureDefn, ":", openingMarkers, closingMarkers, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
        if (endOfReturn != -1) {
            for (int i = endOfReturn; i > 0; --i) {
                if (Character.isWhitespace(procedureDefn.charAt(i))) {
                    return i;
                }
            }
        }
        throw SQLError.createSQLException("Internal error when parsing callable statement metadata", "S1000", this.getExceptionInterceptor());
    }
    
    private int getCascadeDeleteOption(final String cascadeOptions) {
        final int onDeletePos = cascadeOptions.indexOf("ON DELETE");
        if (onDeletePos != -1) {
            final String deleteOptions = cascadeOptions.substring(onDeletePos, cascadeOptions.length());
            if (deleteOptions.startsWith("ON DELETE CASCADE")) {
                return 0;
            }
            if (deleteOptions.startsWith("ON DELETE SET NULL")) {
                return 2;
            }
            if (deleteOptions.startsWith("ON DELETE RESTRICT")) {
                return 1;
            }
            if (deleteOptions.startsWith("ON DELETE NO ACTION")) {
                return 3;
            }
        }
        return 3;
    }
    
    private int getCascadeUpdateOption(final String cascadeOptions) {
        final int onUpdatePos = cascadeOptions.indexOf("ON UPDATE");
        if (onUpdatePos != -1) {
            final String updateOptions = cascadeOptions.substring(onUpdatePos, cascadeOptions.length());
            if (updateOptions.startsWith("ON UPDATE CASCADE")) {
                return 0;
            }
            if (updateOptions.startsWith("ON UPDATE SET NULL")) {
                return 2;
            }
            if (updateOptions.startsWith("ON UPDATE RESTRICT")) {
                return 1;
            }
            if (updateOptions.startsWith("ON UPDATE NO ACTION")) {
                return 3;
            }
        }
        return 3;
    }
    
    protected IteratorWithCleanup<String> getCatalogIterator(final String catalogSpec) throws SQLException {
        IteratorWithCleanup<String> allCatalogsIter;
        if (catalogSpec != null) {
            if (!catalogSpec.equals("")) {
                if (this.conn.getPedantic()) {
                    allCatalogsIter = new SingleStringIterator(catalogSpec);
                }
                else {
                    allCatalogsIter = new SingleStringIterator(StringUtils.unQuoteIdentifier(catalogSpec, this.quotedId));
                }
            }
            else {
                allCatalogsIter = new SingleStringIterator(this.database);
            }
        }
        else if (this.conn.getNullCatalogMeansCurrent()) {
            allCatalogsIter = new SingleStringIterator(this.database);
        }
        else {
            allCatalogsIter = new ResultSetIterator(this.getCatalogs(), 1);
        }
        return allCatalogsIter;
    }
    
    @Override
    public ResultSet getCatalogs() throws SQLException {
        ResultSet results = null;
        Statement stmt = null;
        try {
            stmt = this.conn.getMetadataSafeStatement();
            if (this.conn.getIO().isOracleMode()) {
                results = stmt.executeQuery("SELECT  USERNAME FROM ALL_USERS; ");
            }
            else {
                results = stmt.executeQuery("SHOW DATABASES");
            }
            int catalogsCount = 0;
            if (results.last()) {
                catalogsCount = results.getRow();
                results.beforeFirst();
            }
            final List<String> resultsAsList = new ArrayList<String>(catalogsCount);
            while (results.next()) {
                resultsAsList.add(results.getString(1));
            }
            Collections.sort(resultsAsList);
            final Field[] fields = { new Field("", "TABLE_CAT", 12, results.getMetaData().getColumnDisplaySize(1)) };
            final ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>(catalogsCount);
            for (final String cat : resultsAsList) {
                final byte[][] rowVal = { this.s2b(cat) };
                tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
            }
            return this.buildResultSet(fields, tuples);
        }
        finally {
            if (results != null) {
                try {
                    results.close();
                }
                catch (SQLException sqlEx) {
                    AssertionFailedException.shouldNotHappen(sqlEx);
                }
                results = null;
            }
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException sqlEx) {
                    AssertionFailedException.shouldNotHappen(sqlEx);
                }
                stmt = null;
            }
        }
    }
    
    @Override
    public String getCatalogSeparator() throws SQLException {
        return ".";
    }
    
    @Override
    public String getCatalogTerm() throws SQLException {
        return "database";
    }
    
    @Override
    public ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
        final Field[] fields = { new Field("", "TABLE_CAT", 1, 64), new Field("", "TABLE_SCHEM", 1, 1), new Field("", "TABLE_NAME", 1, 64), new Field("", "COLUMN_NAME", 1, 64), new Field("", "GRANTOR", 1, 77), new Field("", "GRANTEE", 1, 77), new Field("", "PRIVILEGE", 1, 64), new Field("", "IS_GRANTABLE", 1, 3) };
        final String grantQuery = "SELECT c.host, c.db, t.grantor, c.user, c.table_name, c.column_name, c.column_priv FROM mysql.columns_priv c, mysql.tables_priv t WHERE c.host = t.host AND c.db = t.db AND c.table_name = t.table_name AND c.db LIKE ? AND c.table_name = ? AND c.column_name LIKE ?";
        PreparedStatement pStmt = null;
        ResultSet results = null;
        final ArrayList<ResultSetRow> grantRows = new ArrayList<ResultSetRow>();
        try {
            pStmt = this.prepareMetaDataSafeStatement(grantQuery);
            pStmt.setString(1, (catalog != null && catalog.length() != 0) ? catalog : "%");
            pStmt.setString(2, table);
            pStmt.setString(3, columnNamePattern);
            results = pStmt.executeQuery();
            while (results.next()) {
                final String host = results.getString(1);
                final String db = results.getString(2);
                final String grantor = results.getString(3);
                String user = results.getString(4);
                if (user == null || user.length() == 0) {
                    user = "%";
                }
                final StringBuilder fullUser = new StringBuilder(user);
                if (host != null && this.conn.getUseHostsInPrivileges()) {
                    fullUser.append("@");
                    fullUser.append(host);
                }
                final String columnName = results.getString(6);
                String allPrivileges = results.getString(7);
                if (allPrivileges != null) {
                    allPrivileges = allPrivileges.toUpperCase(Locale.ENGLISH);
                    final StringTokenizer st = new StringTokenizer(allPrivileges, ",");
                    while (st.hasMoreTokens()) {
                        final String privilege = st.nextToken().trim();
                        final byte[][] tuple = { this.s2b(db), null, this.s2b(table), this.s2b(columnName), null, null, null, null };
                        if (grantor != null) {
                            tuple[4] = this.s2b(grantor);
                        }
                        else {
                            tuple[4] = null;
                        }
                        tuple[5] = this.s2b(fullUser.toString());
                        tuple[6] = this.s2b(privilege);
                        tuple[7] = null;
                        grantRows.add(new ByteArrayRow(tuple, this.getExceptionInterceptor()));
                    }
                }
            }
        }
        finally {
            if (results != null) {
                try {
                    results.close();
                }
                catch (Exception ex) {}
                results = null;
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                }
                catch (Exception ex2) {}
                pStmt = null;
            }
        }
        return this.buildResultSet(fields, grantRows);
    }
    
    @Override
    public ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        if (this.conn instanceof JDBC4Connection && ((JDBC4Connection)this.conn).isOracleMode()) {
            return this.getColumnsOracle(catalog, schemaPattern, tableNamePattern, columnNamePattern);
        }
        return this.getColumnsMysql(catalog, schemaPattern, tableNamePattern, columnNamePattern);
    }
    
    public ResultSet getColumnsMysql(final String catalog, final String schemaPattern, final String tableNamePattern, String columnNamePattern) throws SQLException {
        if (columnNamePattern == null) {
            if (!this.conn.getNullNamePatternMatchesAll()) {
                throw SQLError.createSQLException("Column name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
            }
            columnNamePattern = "%";
        }
        final String colPattern = columnNamePattern;
        final Field[] fields = this.createColumnsFields();
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        final Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)) {
                @Override
                void forEach(final String catalogStr) throws SQLException {
                    final ArrayList<String> tableNameList = new ArrayList<String>();
                    if (tableNamePattern == null) {
                        ResultSet tables = null;
                        try {
                            tables = DatabaseMetaData.this.getTables(catalogStr, schemaPattern, "%", new String[0]);
                            while (tables.next()) {
                                final String tableNameFromList = tables.getString("TABLE_NAME");
                                tableNameList.add(tableNameFromList);
                            }
                        }
                        finally {
                            if (tables != null) {
                                try {
                                    tables.close();
                                }
                                catch (Exception sqlEx) {
                                    AssertionFailedException.shouldNotHappen(sqlEx);
                                }
                                tables = null;
                            }
                        }
                    }
                    else {
                        ResultSet tables = null;
                        try {
                            tables = DatabaseMetaData.this.getTables(catalogStr, schemaPattern, tableNamePattern, new String[0]);
                            while (tables.next()) {
                                final String tableNameFromList = tables.getString("TABLE_NAME");
                                tableNameList.add(tableNameFromList);
                            }
                        }
                        finally {
                            if (tables != null) {
                                try {
                                    tables.close();
                                }
                                catch (SQLException sqlEx2) {
                                    AssertionFailedException.shouldNotHappen(sqlEx2);
                                }
                                tables = null;
                            }
                        }
                    }
                    for (final String tableName : tableNameList) {
                        ResultSet results = null;
                        try {
                            final StringBuilder queryBuf = new StringBuilder("SHOW ");
                            if (DatabaseMetaData.this.conn.versionMeetsMinimum(4, 1, 0)) {
                                queryBuf.append("FULL ");
                            }
                            queryBuf.append("COLUMNS FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(tableName, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            queryBuf.append(" FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            queryBuf.append(" LIKE ");
                            queryBuf.append(StringUtils.quoteIdentifier(colPattern, "'", true));
                            boolean fixUpOrdinalsRequired = false;
                            Map<String, Integer> ordinalFixUpMap = null;
                            if (!colPattern.equals("%")) {
                                fixUpOrdinalsRequired = true;
                                final StringBuilder fullColumnQueryBuf = new StringBuilder("SHOW ");
                                if (DatabaseMetaData.this.conn.versionMeetsMinimum(4, 1, 0)) {
                                    fullColumnQueryBuf.append("FULL ");
                                }
                                fullColumnQueryBuf.append("COLUMNS FROM ");
                                fullColumnQueryBuf.append(StringUtils.quoteIdentifier(tableName, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                fullColumnQueryBuf.append(" FROM ");
                                fullColumnQueryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                results = stmt.executeQuery(fullColumnQueryBuf.toString());
                                ordinalFixUpMap = new HashMap<String, Integer>();
                                int fullOrdinalPos = 1;
                                while (results.next()) {
                                    final String fullOrdColName = results.getString("Field");
                                    ordinalFixUpMap.put(fullOrdColName, fullOrdinalPos++);
                                }
                            }
                            results = stmt.executeQuery(queryBuf.toString());
                            int ordPos = 1;
                            while (results.next()) {
                                final byte[][] rowVal = new byte[24][];
                                rowVal[0] = DatabaseMetaData.this.s2b(catalogStr);
                                rowVal[1] = null;
                                rowVal[2] = DatabaseMetaData.this.s2b(tableName);
                                rowVal[3] = results.getBytes("Field");
                                final TypeDescriptor typeDesc = new TypeDescriptor(results.getString("Type"), results.getString("Null"));
                                rowVal[4] = Short.toString(typeDesc.dataType).getBytes();
                                rowVal[5] = DatabaseMetaData.this.s2b(typeDesc.typeName);
                                if (typeDesc.columnSize == null) {
                                    rowVal[6] = null;
                                }
                                else {
                                    final String collation = results.getString("Collation");
                                    int mbminlen = 1;
                                    if (collation != null && ("TEXT".equals(typeDesc.typeName) || "TINYTEXT".equals(typeDesc.typeName) || "MEDIUMTEXT".equals(typeDesc.typeName))) {
                                        if (collation.indexOf("ucs2") > -1 || collation.indexOf("utf16") > -1) {
                                            mbminlen = 2;
                                        }
                                        else if (collation.indexOf("utf32") > -1) {
                                            mbminlen = 4;
                                        }
                                    }
                                    rowVal[6] = ((mbminlen == 1) ? DatabaseMetaData.this.s2b(typeDesc.columnSize.toString()) : DatabaseMetaData.this.s2b(Integer.valueOf(typeDesc.columnSize / mbminlen).toString()));
                                }
                                rowVal[7] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.bufferLength));
                                rowVal[8] = (byte[])((typeDesc.decimalDigits == null) ? null : DatabaseMetaData.this.s2b(typeDesc.decimalDigits.toString()));
                                rowVal[9] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.numPrecRadix));
                                rowVal[10] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.nullability));
                                try {
                                    if (DatabaseMetaData.this.conn.versionMeetsMinimum(4, 1, 0)) {
                                        rowVal[11] = results.getBytes("Comment");
                                    }
                                    else {
                                        rowVal[11] = results.getBytes("Extra");
                                    }
                                }
                                catch (Exception E) {
                                    rowVal[11] = new byte[0];
                                }
                                rowVal[12] = results.getBytes("Default");
                                rowVal[13] = new byte[] { 48 };
                                rowVal[14] = new byte[] { 48 };
                                if (StringUtils.indexOfIgnoreCase(typeDesc.typeName, "CHAR") != -1 || StringUtils.indexOfIgnoreCase(typeDesc.typeName, "BLOB") != -1 || StringUtils.indexOfIgnoreCase(typeDesc.typeName, "TEXT") != -1 || StringUtils.indexOfIgnoreCase(typeDesc.typeName, "BINARY") != -1) {
                                    rowVal[15] = rowVal[6];
                                }
                                else {
                                    rowVal[15] = null;
                                }
                                if (!fixUpOrdinalsRequired) {
                                    rowVal[16] = Integer.toString(ordPos++).getBytes();
                                }
                                else {
                                    final String origColName = results.getString("Field");
                                    final Integer realOrdinal = ordinalFixUpMap.get(origColName);
                                    if (realOrdinal == null) {
                                        throw SQLError.createSQLException("Can not find column in full column list to determine true ordinal position.", "S1000", DatabaseMetaData.this.getExceptionInterceptor());
                                    }
                                    rowVal[16] = realOrdinal.toString().getBytes();
                                }
                                rowVal[17] = DatabaseMetaData.this.s2b(typeDesc.isNullable);
                                rowVal[19] = (rowVal[18] = null);
                                rowVal[21] = (rowVal[20] = null);
                                rowVal[22] = DatabaseMetaData.this.s2b("");
                                final String extra = results.getString("Extra");
                                if (extra != null) {
                                    rowVal[22] = DatabaseMetaData.this.s2b((StringUtils.indexOfIgnoreCase(extra, "auto_increment") != -1) ? "YES" : "NO");
                                    rowVal[23] = DatabaseMetaData.this.s2b((StringUtils.indexOfIgnoreCase(extra, "generated") != -1) ? "YES" : "NO");
                                }
                                rows.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                            }
                        }
                        finally {
                            if (results != null) {
                                try {
                                    results.close();
                                }
                                catch (Exception ex) {}
                                results = null;
                            }
                        }
                    }
                }
            }.doForAll();
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        final ResultSet results = this.buildResultSet(fields, rows);
        return results;
    }
    
    public ResultSet getColumnsOracle(final String catalog, final String schemaPattern, final String tableNamePattern, String columnNamePattern) throws SQLException {
        if (columnNamePattern == null) {
            if (!this.conn.getNullNamePatternMatchesAll()) {
                throw SQLError.createSQLException("Column name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
            }
            columnNamePattern = "%";
        }
        final String colPattern = columnNamePattern;
        final Field[] fields = this.createColumnsFields();
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        final Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)) {
                @Override
                void forEach(final String catalogStr) throws SQLException {
                    final ArrayList<String> tableNameList = new ArrayList<String>();
                    if (tableNamePattern == null) {
                        ResultSet tables = null;
                        try {
                            tables = DatabaseMetaData.this.getTables(catalogStr, schemaPattern, "%", null);
                            while (tables.next()) {
                                final String tableNameFromList = tables.getString("TABLE_NAME");
                                tableNameList.add(tableNameFromList);
                            }
                        }
                        finally {
                            if (tables != null) {
                                try {
                                    tables.close();
                                }
                                catch (Exception sqlEx) {
                                    AssertionFailedException.shouldNotHappen(sqlEx);
                                }
                                tables = null;
                            }
                        }
                    }
                    else {
                        ResultSet tables = null;
                        try {
                            tables = DatabaseMetaData.this.getTables(catalogStr, schemaPattern, tableNamePattern, null);
                            while (tables.next()) {
                                final String tableNameFromList = tables.getString("TABLE_NAME");
                                tableNameList.add(tableNameFromList);
                            }
                        }
                        finally {
                            if (tables != null) {
                                try {
                                    tables.close();
                                }
                                catch (SQLException sqlEx2) {
                                    AssertionFailedException.shouldNotHappen(sqlEx2);
                                }
                                tables = null;
                            }
                        }
                    }
                    for (final String tableName : tableNameList) {
                        ResultSet results = null;
                        try {
                            final String querySql = String.format("SELECT  NULL AS table_cat,\n       t.owner AS table_schem,\n       t.table_name AS table_name,\n       t.column_name AS column_name,\n       DECODE (t.data_type, 'CHAR', 1, 'VARCHAR2', 12, 'NUMBER', 3,\n               'LONG', -1, 'DATE', 93, 'RAW', -3, 'LONG RAW', -4,  \n               'BLOB', 2004, 'CLOB', 2005, 'BFILE', -13, 'FLOAT', 6, \n               'TIMESTAMP(6)', 93, 'TIMESTAMP(6) WITH TIME ZONE', -101, \n               'TIMESTAMP(6) WITH LOCAL TIME ZONE', -102, \n               'INTERVAL YEAR(2) TO MONTH', -103, \n               'INTERVAL DAY(2) TO SECOND(6)', -104, \n               'BINARY_FLOAT', 100, 'BINARY_DOUBLE', 101, \n               'XMLTYPE', 2009, \n               1111)\n              AS data_type,\n       t.data_type AS type_name,\n       DECODE (t.data_precision, null,          DECODE (t.data_type, 'CHAR', t.char_length,                   'VARCHAR', t.char_length,                   'VARCHAR2', t.char_length,                   'NVARCHAR2', t.char_length,                   'NCHAR', t.char_length,                   'NUMBER', 0,           t.data_length),         t.data_precision)\n              AS column_size,\n       0 AS buffer_length,\n       DECODE (t.data_type,                'NUMBER', DECODE (t.data_precision,                                  null, -127,                                  t.data_scale),                t.data_scale) AS decimal_digits,\n       10 AS num_prec_radix,\n       DECODE (t.nullable, 'N', 0, 1) AS nullable,\n       NULL AS remarks,\n       t.data_default AS column_def,\n       0 AS sql_data_type,\n       0 AS sql_datetime_sub,\n       t.data_length AS char_octet_length,\n       t.column_id AS ordinal_position,\n       DECODE (t.nullable, 'N', 'NO', 'YES') AS is_nullable\nFROM all_tab_columns t\nWHERE t.owner LIKE '%s' ESCAPE '/'\n  AND t.table_name LIKE '%s' ESCAPE '/'\n  AND t.column_name LIKE '%s' ESCAPE '/'\n\nORDER BY table_schem, table_name, ordinal_position\n", (schemaPattern == null) ? "%" : schemaPattern, (tableNamePattern == null) ? "%" : tableNamePattern, (colPattern == null) ? Character.valueOf('%') : colPattern);
                            boolean fixUpOrdinalsRequired = false;
                            Map<String, Integer> ordinalFixUpMap = null;
                            if (!colPattern.equals("%")) {
                                fixUpOrdinalsRequired = true;
                                final StringBuilder fullColumnQueryBuf = new StringBuilder("DESC ");
                                fullColumnQueryBuf.append(StringUtils.quoteIdentifier(tableName, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                results = stmt.executeQuery(fullColumnQueryBuf.toString());
                                ordinalFixUpMap = new HashMap<String, Integer>();
                                int fullOrdinalPos = 1;
                                while (results.next()) {
                                    final String fullOrdColName = results.getString("FIELD");
                                    ordinalFixUpMap.put(fullOrdColName, fullOrdinalPos++);
                                }
                            }
                            results = stmt.executeQuery(querySql);
                            int ordPos = 1;
                            while (results.next()) {
                                final byte[][] rowVal = new byte[24][];
                                rowVal[0] = DatabaseMetaData.this.s2b(catalogStr);
                                rowVal[1] = null;
                                rowVal[2] = DatabaseMetaData.this.s2b(tableName);
                                rowVal[3] = results.getBytes("COLUMN_NAME");
                                final TypeDescriptor typeDesc = new TypeDescriptor(results.getString("TYPE_NAME"), results.getString("NULLABLE"));
                                rowVal[4] = results.getString("DATA_TYPE").getBytes();
                                rowVal[5] = DatabaseMetaData.this.s2b(typeDesc.typeName);
                                if (typeDesc.columnSize == null) {
                                    rowVal[6] = null;
                                }
                                else {
                                    final String collation = null;
                                    int mbminlen = 1;
                                    if (collation != null && ("TEXT".equals(typeDesc.typeName) || "TINYTEXT".equals(typeDesc.typeName) || "MEDIUMTEXT".equals(typeDesc.typeName))) {
                                        if (collation.indexOf("ucs2") > -1 || collation.indexOf("utf16") > -1) {
                                            mbminlen = 2;
                                        }
                                        else if (collation.indexOf("utf32") > -1) {
                                            mbminlen = 4;
                                        }
                                    }
                                    rowVal[6] = ((mbminlen == 1) ? DatabaseMetaData.this.s2b(typeDesc.columnSize.toString()) : DatabaseMetaData.this.s2b(Integer.valueOf(typeDesc.columnSize / mbminlen).toString()));
                                }
                                rowVal[7] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.bufferLength));
                                rowVal[8] = (byte[])((typeDesc.decimalDigits == null) ? null : DatabaseMetaData.this.s2b(typeDesc.decimalDigits.toString()));
                                rowVal[9] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.numPrecRadix));
                                rowVal[10] = DatabaseMetaData.this.s2b(Integer.toString(typeDesc.nullability));
                                try {
                                    if (DatabaseMetaData.this.conn.versionMeetsMinimum(4, 1, 0)) {
                                        rowVal[11] = results.getBytes("REMARKS");
                                    }
                                    else {
                                        rowVal[11] = results.getBytes("REMARKS");
                                    }
                                }
                                catch (Exception E) {
                                    rowVal[11] = new byte[0];
                                }
                                rowVal[12] = results.getBytes("COLUMN_DEF");
                                rowVal[13] = new byte[] { 48 };
                                rowVal[14] = new byte[] { 48 };
                                if (StringUtils.indexOfIgnoreCase(typeDesc.typeName, "CHAR") != -1 || StringUtils.indexOfIgnoreCase(typeDesc.typeName, "BLOB") != -1 || StringUtils.indexOfIgnoreCase(typeDesc.typeName, "TEXT") != -1 || StringUtils.indexOfIgnoreCase(typeDesc.typeName, "BINARY") != -1) {
                                    rowVal[15] = rowVal[6];
                                }
                                else {
                                    rowVal[15] = null;
                                }
                                if (!fixUpOrdinalsRequired) {
                                    rowVal[16] = Integer.toString(ordPos++).getBytes();
                                }
                                else {
                                    final String origColName = results.getString("COLUMN_NAME");
                                    final Integer realOrdinal = ordinalFixUpMap.get(origColName);
                                    if (realOrdinal == null) {
                                        throw SQLError.createSQLException("Can not find column in full column list to determine true ordinal position.", "S1000", DatabaseMetaData.this.getExceptionInterceptor());
                                    }
                                    rowVal[16] = realOrdinal.toString().getBytes();
                                }
                                rowVal[17] = DatabaseMetaData.this.s2b(typeDesc.isNullable);
                                rowVal[19] = (rowVal[18] = null);
                                rowVal[21] = (rowVal[20] = null);
                                rowVal[22] = DatabaseMetaData.this.s2b("");
                                rows.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                            }
                        }
                        finally {
                            if (results != null) {
                                try {
                                    results.close();
                                }
                                catch (Exception ex) {}
                                results = null;
                            }
                        }
                    }
                }
            }.doForAll();
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        final ResultSet results = this.buildResultSet(fields, rows);
        return results;
    }
    
    protected Field[] createColumnsFields() {
        final Field[] fields = { new Field("", "TABLE_CAT", 1, 255), new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_NAME", 1, 255), new Field("", "COLUMN_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 5), new Field("", "TYPE_NAME", 1, 16), new Field("", "COLUMN_SIZE", 4, Integer.toString(Integer.MAX_VALUE).length()), new Field("", "BUFFER_LENGTH", 4, 10), new Field("", "DECIMAL_DIGITS", 4, 10), new Field("", "NUM_PREC_RADIX", 4, 10), new Field("", "NULLABLE", 4, 10), new Field("", "REMARKS", 1, 0), new Field("", "COLUMN_DEF", 1, 0), new Field("", "SQL_DATA_TYPE", 4, 10), new Field("", "SQL_DATETIME_SUB", 4, 10), new Field("", "CHAR_OCTET_LENGTH", 4, Integer.toString(Integer.MAX_VALUE).length()), new Field("", "ORDINAL_POSITION", 4, 10), new Field("", "IS_NULLABLE", 1, 3), new Field("", "SCOPE_CATALOG", 1, 255), new Field("", "SCOPE_SCHEMA", 1, 255), new Field("", "SCOPE_TABLE", 1, 255), new Field("", "SOURCE_DATA_TYPE", 5, 10), new Field("", "IS_AUTOINCREMENT", 1, 3), new Field("", "IS_GENERATEDCOLUMN", 1, 3) };
        return fields;
    }
    
    @Override
    public java.sql.Connection getConnection() throws SQLException {
        return this.conn;
    }
    
    @Override
    public ResultSet getCrossReference(final String primaryCatalog, final String primarySchema, final String primaryTable, final String foreignCatalog, final String foreignSchema, final String foreignTable) throws SQLException {
        if (primaryTable == null) {
            throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
        }
        final Field[] fields = this.createFkMetadataFields();
        final ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>();
        if (this.conn.versionMeetsMinimum(3, 23, 0)) {
            final Statement stmt = this.conn.getMetadataSafeStatement();
            try {
                new IterateBlock<String>(this.getCatalogIterator(foreignCatalog)) {
                    @Override
                    void forEach(final String catalogStr) throws SQLException {
                        ResultSet fkresults = null;
                        try {
                            if (DatabaseMetaData.this.conn.versionMeetsMinimum(3, 23, 50)) {
                                fkresults = DatabaseMetaData.this.extractForeignKeyFromCreateTable(catalogStr, null);
                            }
                            else {
                                final StringBuilder queryBuf = new StringBuilder("SHOW TABLE STATUS FROM ");
                                queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                fkresults = stmt.executeQuery(queryBuf.toString());
                            }
                            final String foreignTableWithCase = DatabaseMetaData.this.getTableNameWithCase(foreignTable);
                            final String primaryTableWithCase = DatabaseMetaData.this.getTableNameWithCase(primaryTable);
                            while (fkresults.next()) {
                                final String tableType = fkresults.getString("Type");
                                if (tableType != null && (tableType.equalsIgnoreCase("innodb") || tableType.equalsIgnoreCase("SUPPORTS_FK"))) {
                                    final String comment = fkresults.getString("Comment").trim();
                                    if (comment == null) {
                                        continue;
                                    }
                                    final StringTokenizer commentTokens = new StringTokenizer(comment, ";", false);
                                    if (commentTokens.hasMoreTokens()) {
                                        commentTokens.nextToken();
                                    }
                                    while (commentTokens.hasMoreTokens()) {
                                        final String keys = commentTokens.nextToken();
                                        final LocalAndReferencedColumns parsedInfo = DatabaseMetaData.this.parseTableStatusIntoLocalAndReferencedColumns(keys);
                                        int keySeq = 0;
                                        final Iterator<String> referencingColumns = parsedInfo.localColumnsList.iterator();
                                        final Iterator<String> referencedColumns = parsedInfo.referencedColumnsList.iterator();
                                        while (referencingColumns.hasNext()) {
                                            final String referencingColumn = StringUtils.unQuoteIdentifier(referencingColumns.next(), DatabaseMetaData.this.quotedId);
                                            final byte[][] tuple = new byte[14][];
                                            tuple[4] = (byte[])((foreignCatalog == null) ? null : DatabaseMetaData.this.s2b(foreignCatalog));
                                            tuple[5] = (byte[])((foreignSchema == null) ? null : DatabaseMetaData.this.s2b(foreignSchema));
                                            final String dummy = fkresults.getString("Name");
                                            if (dummy.compareTo(foreignTableWithCase) != 0) {
                                                continue;
                                            }
                                            tuple[6] = DatabaseMetaData.this.s2b(dummy);
                                            tuple[7] = DatabaseMetaData.this.s2b(referencingColumn);
                                            tuple[0] = (byte[])((primaryCatalog == null) ? null : DatabaseMetaData.this.s2b(primaryCatalog));
                                            tuple[1] = (byte[])((primarySchema == null) ? null : DatabaseMetaData.this.s2b(primarySchema));
                                            if (parsedInfo.referencedTable.compareTo(primaryTableWithCase) != 0) {
                                                continue;
                                            }
                                            tuple[2] = DatabaseMetaData.this.s2b(parsedInfo.referencedTable);
                                            tuple[3] = DatabaseMetaData.this.s2b(StringUtils.unQuoteIdentifier(referencedColumns.next(), DatabaseMetaData.this.quotedId));
                                            tuple[8] = Integer.toString(keySeq).getBytes();
                                            final int[] actions = DatabaseMetaData.this.getForeignKeyActions(keys);
                                            tuple[9] = Integer.toString(actions[1]).getBytes();
                                            tuple[10] = Integer.toString(actions[0]).getBytes();
                                            tuple[12] = (tuple[11] = null);
                                            tuple[13] = Integer.toString(7).getBytes();
                                            tuples.add(new ByteArrayRow(tuple, DatabaseMetaData.this.getExceptionInterceptor()));
                                            ++keySeq;
                                        }
                                    }
                                }
                            }
                        }
                        finally {
                            if (fkresults != null) {
                                try {
                                    fkresults.close();
                                }
                                catch (Exception sqlEx) {
                                    AssertionFailedException.shouldNotHappen(sqlEx);
                                }
                                fkresults = null;
                            }
                        }
                    }
                }.doForAll();
            }
            finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        }
        final ResultSet results = this.buildResultSet(fields, tuples);
        return results;
    }
    
    protected Field[] createFkMetadataFields() {
        final Field[] fields = { new Field("", "PKTABLE_CAT", 1, 255), new Field("", "PKTABLE_SCHEM", 1, 0), new Field("", "PKTABLE_NAME", 1, 255), new Field("", "PKCOLUMN_NAME", 1, 32), new Field("", "FKTABLE_CAT", 1, 255), new Field("", "FKTABLE_SCHEM", 1, 0), new Field("", "FKTABLE_NAME", 1, 255), new Field("", "FKCOLUMN_NAME", 1, 32), new Field("", "KEY_SEQ", 5, 2), new Field("", "UPDATE_RULE", 5, 2), new Field("", "DELETE_RULE", 5, 2), new Field("", "FK_NAME", 1, 0), new Field("", "PK_NAME", 1, 0), new Field("", "DEFERRABILITY", 5, 2) };
        return fields;
    }
    
    @Override
    public int getDatabaseMajorVersion() throws SQLException {
        return this.conn.getServerMajorVersion();
    }
    
    @Override
    public int getDatabaseMinorVersion() throws SQLException {
        return this.conn.getServerMinorVersion();
    }
    
    @Override
    public String getDatabaseProductName() throws SQLException {
        if (this.conn != null && this.conn.isAlive() && this.conn.getIO().isOracleMode()) {
            return "Oracle";
        }
        return "MySQL";
    }
    
    @Override
    public String getDatabaseProductVersion() throws SQLException {
        return this.conn.getServerVersion();
    }
    
    @Override
    public int getDefaultTransactionIsolation() throws SQLException {
        if (this.conn.supportsIsolationLevel()) {
            return 2;
        }
        return 0;
    }
    
    @Override
    public int getDriverMajorVersion() {
        return NonRegisteringDriver.getMajorVersionInternal();
    }
    
    @Override
    public int getDriverMinorVersion() {
        return NonRegisteringDriver.getMinorVersionInternal();
    }
    
    @Override
    public String getDriverName() throws SQLException {
        return "@MYSQL_CJ_DISPLAY_PROD_NAME@";
    }
    
    @Override
    public String getDriverVersion() throws SQLException {
        return "@MYSQL_CJ_FULL_PROD_NAME@ ( Revision: @MYSQL_CJ_REVISION@ )";
    }
    
    @Override
    public ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
        }
        final Field[] fields = this.createFkMetadataFields();
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        if (this.conn.versionMeetsMinimum(3, 23, 0)) {
            final Statement stmt = this.conn.getMetadataSafeStatement();
            try {
                new IterateBlock<String>(this.getCatalogIterator(catalog)) {
                    @Override
                    void forEach(final String catalogStr) throws SQLException {
                        ResultSet fkresults = null;
                        try {
                            if (DatabaseMetaData.this.conn.versionMeetsMinimum(3, 23, 50)) {
                                fkresults = DatabaseMetaData.this.extractForeignKeyFromCreateTable(catalogStr, null);
                            }
                            else {
                                final StringBuilder queryBuf = new StringBuilder("SHOW TABLE STATUS FROM ");
                                queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                fkresults = stmt.executeQuery(queryBuf.toString());
                            }
                            final String tableNameWithCase = DatabaseMetaData.this.getTableNameWithCase(table);
                            while (fkresults.next()) {
                                final String tableType = fkresults.getString("Type");
                                if (tableType != null && (tableType.equalsIgnoreCase("innodb") || tableType.equalsIgnoreCase("SUPPORTS_FK"))) {
                                    final String comment = fkresults.getString("Comment").trim();
                                    if (comment == null) {
                                        continue;
                                    }
                                    final StringTokenizer commentTokens = new StringTokenizer(comment, ";", false);
                                    if (!commentTokens.hasMoreTokens()) {
                                        continue;
                                    }
                                    commentTokens.nextToken();
                                    while (commentTokens.hasMoreTokens()) {
                                        final String keys = commentTokens.nextToken();
                                        DatabaseMetaData.this.getExportKeyResults(catalogStr, tableNameWithCase, keys, rows, fkresults.getString("Name"));
                                    }
                                }
                            }
                        }
                        finally {
                            if (fkresults != null) {
                                try {
                                    fkresults.close();
                                }
                                catch (SQLException sqlEx) {
                                    AssertionFailedException.shouldNotHappen(sqlEx);
                                }
                                fkresults = null;
                            }
                        }
                    }
                }.doForAll();
            }
            finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        }
        final ResultSet results = this.buildResultSet(fields, rows);
        return results;
    }
    
    protected void getExportKeyResults(final String catalog, final String exportingTable, final String keysComment, final List<ResultSetRow> tuples, final String fkTableName) throws SQLException {
        this.getResultsImpl(catalog, exportingTable, keysComment, tuples, fkTableName, true);
    }
    
    @Override
    public String getExtraNameCharacters() throws SQLException {
        return "#@";
    }
    
    protected int[] getForeignKeyActions(final String commentString) {
        final int[] actions = { 3, 3 };
        final int lastParenIndex = commentString.lastIndexOf(")");
        if (lastParenIndex != commentString.length() - 1) {
            final String cascadeOptions = commentString.substring(lastParenIndex + 1).trim().toUpperCase(Locale.ENGLISH);
            actions[0] = this.getCascadeDeleteOption(cascadeOptions);
            actions[1] = this.getCascadeUpdateOption(cascadeOptions);
        }
        return actions;
    }
    
    @Override
    public String getIdentifierQuoteString() throws SQLException {
        if (!this.conn.isAlive()) {
            return " ";
        }
        if (this.conn.getIO().isOracleMode()) {
            return "\"";
        }
        if (this.conn.supportsQuotedIdentifiers()) {
            return this.conn.useAnsiQuotedIdentifiers() ? "\"" : "`";
        }
        return " ";
    }
    
    @Override
    public ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
        }
        final Field[] fields = this.createFkMetadataFields();
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        if (this.conn.versionMeetsMinimum(3, 23, 0)) {
            final Statement stmt = this.conn.getMetadataSafeStatement();
            try {
                new IterateBlock<String>(this.getCatalogIterator(catalog)) {
                    @Override
                    void forEach(final String catalogStr) throws SQLException {
                        ResultSet fkresults = null;
                        try {
                            if (DatabaseMetaData.this.conn.versionMeetsMinimum(3, 23, 50)) {
                                fkresults = DatabaseMetaData.this.extractForeignKeyFromCreateTable(catalogStr, table);
                            }
                            else {
                                final StringBuilder queryBuf = new StringBuilder("SHOW TABLE STATUS ");
                                queryBuf.append(" FROM ");
                                queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                                queryBuf.append(" LIKE ");
                                queryBuf.append(StringUtils.quoteIdentifier(table, "'", true));
                                fkresults = stmt.executeQuery(queryBuf.toString());
                            }
                            while (fkresults.next()) {
                                final String tableType = fkresults.getString("Type");
                                if (tableType != null && (tableType.equalsIgnoreCase("innodb") || tableType.equalsIgnoreCase("SUPPORTS_FK"))) {
                                    final String comment = fkresults.getString("Comment").trim();
                                    if (comment == null) {
                                        continue;
                                    }
                                    final StringTokenizer commentTokens = new StringTokenizer(comment, ";", false);
                                    if (!commentTokens.hasMoreTokens()) {
                                        continue;
                                    }
                                    commentTokens.nextToken();
                                    while (commentTokens.hasMoreTokens()) {
                                        final String keys = commentTokens.nextToken();
                                        DatabaseMetaData.this.getImportKeyResults(catalogStr, table, keys, rows);
                                    }
                                }
                            }
                        }
                        finally {
                            if (fkresults != null) {
                                try {
                                    fkresults.close();
                                }
                                catch (SQLException sqlEx) {
                                    AssertionFailedException.shouldNotHappen(sqlEx);
                                }
                                fkresults = null;
                            }
                        }
                    }
                }.doForAll();
            }
            finally {
                if (stmt != null) {
                    stmt.close();
                }
            }
        }
        final ResultSet results = this.buildResultSet(fields, rows);
        return results;
    }
    
    protected void getImportKeyResults(final String catalog, final String importingTable, final String keysComment, final List<ResultSetRow> tuples) throws SQLException {
        this.getResultsImpl(catalog, importingTable, keysComment, tuples, null, false);
    }
    
    @Override
    public ResultSet getIndexInfo(final String catalog, final String schema, final String table, final boolean unique, final boolean approximate) throws SQLException {
        final Field[] fields = this.createIndexInfoFields();
        final SortedMap<IndexMetaDataKey, ResultSetRow> sortedRows = new TreeMap<IndexMetaDataKey, ResultSetRow>();
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        final Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)) {
                @Override
                void forEach(final String catalogStr) throws SQLException {
                    ResultSet results = null;
                    try {
                        final StringBuilder queryBuf = new StringBuilder("SHOW INDEX FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(table, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        queryBuf.append(" FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        try {
                            results = stmt.executeQuery(queryBuf.toString());
                        }
                        catch (SQLException sqlEx) {
                            final int errorCode = sqlEx.getErrorCode();
                            if (!"42S02".equals(sqlEx.getSQLState()) && errorCode != 1146) {
                                throw sqlEx;
                            }
                        }
                        while (results != null && results.next()) {
                            final byte[][] row = new byte[14][];
                            row[0] = ((catalogStr == null) ? new byte[0] : DatabaseMetaData.this.s2b(catalogStr));
                            row[1] = null;
                            row[2] = results.getBytes("Table");
                            final boolean indexIsUnique = results.getInt("Non_unique") == 0;
                            row[3] = (indexIsUnique ? DatabaseMetaData.this.s2b("false") : DatabaseMetaData.this.s2b("true"));
                            row[4] = new byte[0];
                            row[5] = results.getBytes("Key_name");
                            final short indexType = 3;
                            row[6] = Integer.toString(indexType).getBytes();
                            row[7] = results.getBytes("Seq_in_index");
                            row[8] = results.getBytes("Column_name");
                            row[9] = results.getBytes("Collation");
                            long cardinality = results.getLong("Cardinality");
                            if (!Util.isJdbc42() && cardinality > 2147483647L) {
                                cardinality = 2147483647L;
                            }
                            row[10] = DatabaseMetaData.this.s2b(String.valueOf(cardinality));
                            row[11] = DatabaseMetaData.this.s2b("0");
                            row[12] = null;
                            final IndexMetaDataKey indexInfoKey = new IndexMetaDataKey(!indexIsUnique, indexType, results.getString("Key_name").toLowerCase(), results.getShort("Seq_in_index"));
                            if (unique) {
                                if (!indexIsUnique) {
                                    continue;
                                }
                                sortedRows.put(indexInfoKey, new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                            }
                            else {
                                sortedRows.put(indexInfoKey, new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                            }
                        }
                    }
                    finally {
                        if (results != null) {
                            try {
                                results.close();
                            }
                            catch (Exception ex) {}
                            results = null;
                        }
                    }
                }
            }.doForAll();
            final Iterator<ResultSetRow> sortedRowsIterator = sortedRows.values().iterator();
            while (sortedRowsIterator.hasNext()) {
                rows.add(sortedRowsIterator.next());
            }
            final ResultSet indexInfo = this.buildResultSet(fields, rows);
            return indexInfo;
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }
    
    protected Field[] createIndexInfoFields() {
        final Field[] fields = { new Field("", "TABLE_CAT", 1, 255), new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_NAME", 1, 255), new Field("", "NON_UNIQUE", 16, 4), new Field("", "INDEX_QUALIFIER", 1, 1), new Field("", "INDEX_NAME", 1, 32), new Field("", "TYPE", 5, 32), new Field("", "ORDINAL_POSITION", 5, 5), new Field("", "COLUMN_NAME", 1, 32), new Field("", "ASC_OR_DESC", 1, 1), null, null, null };
        if (Util.isJdbc42()) {
            fields[10] = new Field("", "CARDINALITY", -5, 20);
            fields[11] = new Field("", "PAGES", -5, 20);
        }
        else {
            fields[10] = new Field("", "CARDINALITY", 4, 20);
            fields[11] = new Field("", "PAGES", 4, 10);
        }
        fields[12] = new Field("", "FILTER_CONDITION", 1, 32);
        return fields;
    }
    
    @Override
    public int getJDBCMajorVersion() throws SQLException {
        return 4;
    }
    
    @Override
    public int getJDBCMinorVersion() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxBinaryLiteralLength() throws SQLException {
        return 16777208;
    }
    
    @Override
    public int getMaxCatalogNameLength() throws SQLException {
        return 32;
    }
    
    @Override
    public int getMaxCharLiteralLength() throws SQLException {
        return 16777208;
    }
    
    @Override
    public int getMaxColumnNameLength() throws SQLException {
        return 64;
    }
    
    @Override
    public int getMaxColumnsInGroupBy() throws SQLException {
        return 64;
    }
    
    @Override
    public int getMaxColumnsInIndex() throws SQLException {
        return 16;
    }
    
    @Override
    public int getMaxColumnsInOrderBy() throws SQLException {
        return 64;
    }
    
    @Override
    public int getMaxColumnsInSelect() throws SQLException {
        return 256;
    }
    
    @Override
    public int getMaxColumnsInTable() throws SQLException {
        return 512;
    }
    
    @Override
    public int getMaxConnections() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxCursorNameLength() throws SQLException {
        return 64;
    }
    
    @Override
    public int getMaxIndexLength() throws SQLException {
        return 256;
    }
    
    @Override
    public int getMaxProcedureNameLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxRowSize() throws SQLException {
        return 2147483639;
    }
    
    @Override
    public int getMaxSchemaNameLength() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxStatementLength() throws SQLException {
        return MysqlIO.getMaxBuf() - 4;
    }
    
    @Override
    public int getMaxStatements() throws SQLException {
        return 0;
    }
    
    @Override
    public int getMaxTableNameLength() throws SQLException {
        return 64;
    }
    
    @Override
    public int getMaxTablesInSelect() throws SQLException {
        return 256;
    }
    
    @Override
    public int getMaxUserNameLength() throws SQLException {
        return 16;
    }
    
    @Override
    public String getNumericFunctions() throws SQLException {
        return "ABS,ACOS,ASIN,ATAN,ATAN2,BIT_COUNT,CEILING,COS,COT,DEGREES,EXP,FLOOR,LOG,LOG10,MAX,MIN,MOD,PI,POW,POWER,RADIANS,RAND,ROUND,SIN,SQRT,TAN,TRUNCATE";
    }
    
    @Override
    public ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
        final Field[] fields = { new Field("", "TABLE_CAT", 1, 255), new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_NAME", 1, 255), new Field("", "COLUMN_NAME", 1, 32), new Field("", "KEY_SEQ", 5, 5), new Field("", "PK_NAME", 1, 32) };
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
        }
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        final Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)) {
                @Override
                void forEach(final String catalogStr) throws SQLException {
                    ResultSet rs = null;
                    try {
                        final StringBuilder queryBuf = new StringBuilder("SHOW KEYS FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(table, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        queryBuf.append(" FROM ");
                        queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                        rs = stmt.executeQuery(queryBuf.toString());
                        final TreeMap<String, byte[][]> sortMap = new TreeMap<String, byte[][]>();
                        while (rs.next()) {
                            final String keyType = rs.getString("Key_name");
                            if (keyType != null && (keyType.equalsIgnoreCase("PRIMARY") || keyType.equalsIgnoreCase("PRI"))) {
                                final byte[][] tuple = { (catalogStr == null) ? new byte[0] : DatabaseMetaData.this.s2b(catalogStr), null, DatabaseMetaData.this.s2b(table), null, null, null };
                                final String columnName = rs.getString("Column_name");
                                tuple[3] = DatabaseMetaData.this.s2b(columnName);
                                tuple[4] = DatabaseMetaData.this.s2b(rs.getString("Seq_in_index"));
                                tuple[5] = DatabaseMetaData.this.s2b(keyType);
                                sortMap.put(columnName, tuple);
                            }
                        }
                        final Iterator<byte[][]> sortedIterator = sortMap.values().iterator();
                        while (sortedIterator.hasNext()) {
                            rows.add(new ByteArrayRow(sortedIterator.next(), DatabaseMetaData.this.getExceptionInterceptor()));
                        }
                    }
                    finally {
                        if (rs != null) {
                            try {
                                rs.close();
                            }
                            catch (Exception ex) {}
                            rs = null;
                        }
                    }
                }
            }.doForAll();
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        final ResultSet results = this.buildResultSet(fields, rows);
        return results;
    }
    
    @Override
    public ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException {
        final Field[] fields = this.createProcedureColumnsFields();
        return this.getProcedureOrFunctionColumns(fields, catalog, schemaPattern, procedureNamePattern, columnNamePattern, true, true);
    }
    
    protected Field[] createProcedureColumnsFields() {
        final Field[] fields = { new Field("", "PROCEDURE_CAT", 1, 512), new Field("", "PROCEDURE_SCHEM", 1, 512), new Field("", "PROCEDURE_NAME", 1, 512), new Field("", "COLUMN_NAME", 1, 512), new Field("", "COLUMN_TYPE", 1, 64), new Field("", "DATA_TYPE", 5, 6), new Field("", "TYPE_NAME", 1, 64), new Field("", "PRECISION", 4, 12), new Field("", "LENGTH", 4, 12), new Field("", "SCALE", 5, 12), new Field("", "RADIX", 5, 6), new Field("", "NULLABLE", 5, 6), new Field("", "REMARKS", 1, 512), new Field("", "COLUMN_DEF", 1, 512), new Field("", "SQL_DATA_TYPE", 4, 12), new Field("", "SQL_DATETIME_SUB", 4, 12), new Field("", "CHAR_OCTET_LENGTH", 4, 12), new Field("", "ORDINAL_POSITION", 4, 12), new Field("", "IS_NULLABLE", 1, 512), new Field("", "SPECIFIC_NAME", 1, 512) };
        return fields;
    }
    
    protected ResultSet getProcedureOrFunctionColumns(final Field[] fields, String catalog, final String schemaPattern, final String procedureOrFunctionNamePattern, final String columnNamePattern, final boolean returnProcedures, final boolean returnFunctions) throws SQLException {
        final List<ComparableWrapper<String, ProcedureType>> procsOrFuncsToExtractList = new ArrayList<ComparableWrapper<String, ProcedureType>>();
        ResultSet procsAndOrFuncsRs = null;
        if (this.supportsStoredProcedures()) {
            try {
                String tmpProcedureOrFunctionNamePattern = null;
                if (procedureOrFunctionNamePattern != null && !procedureOrFunctionNamePattern.equals("%")) {
                    tmpProcedureOrFunctionNamePattern = StringUtils.sanitizeProcOrFuncName(procedureOrFunctionNamePattern);
                }
                if (tmpProcedureOrFunctionNamePattern == null) {
                    tmpProcedureOrFunctionNamePattern = procedureOrFunctionNamePattern;
                }
                else {
                    String tmpCatalog = catalog;
                    final List<String> parseList = StringUtils.splitDBdotName(tmpProcedureOrFunctionNamePattern, tmpCatalog, this.quotedId, this.conn.isNoBackslashEscapesSet());
                    if (parseList.size() == 2) {
                        tmpCatalog = parseList.get(0);
                        tmpProcedureOrFunctionNamePattern = parseList.get(1);
                    }
                }
                procsAndOrFuncsRs = this.getProceduresAndOrFunctions(this.createFieldMetadataForGetProcedures(), catalog, schemaPattern, tmpProcedureOrFunctionNamePattern, returnProcedures, returnFunctions);
                boolean hasResults = false;
                while (procsAndOrFuncsRs.next()) {
                    procsOrFuncsToExtractList.add(new ComparableWrapper<String, ProcedureType>(this.getFullyQualifiedName(procsAndOrFuncsRs.getString(1), procsAndOrFuncsRs.getString(3)), (procsAndOrFuncsRs.getShort(8) == 1) ? ProcedureType.PROCEDURE : ProcedureType.FUNCTION));
                    hasResults = true;
                }
                if (hasResults) {
                    Collections.sort(procsOrFuncsToExtractList);
                }
            }
            finally {
                SQLException rethrowSqlEx = null;
                if (procsAndOrFuncsRs != null) {
                    try {
                        procsAndOrFuncsRs.close();
                    }
                    catch (SQLException sqlEx) {
                        rethrowSqlEx = sqlEx;
                    }
                }
                if (rethrowSqlEx != null) {
                    throw rethrowSqlEx;
                }
            }
        }
        final ArrayList<ResultSetRow> resultRows = new ArrayList<ResultSetRow>();
        int idx = 0;
        String procNameToCall = "";
        for (final ComparableWrapper<String, ProcedureType> procOrFunc : procsOrFuncsToExtractList) {
            final String procName = procOrFunc.getKey();
            final ProcedureType procType = procOrFunc.getValue();
            if (!" ".equals(this.quotedId)) {
                idx = StringUtils.indexOfIgnoreCase(0, procName, ".", this.quotedId, this.quotedId, this.conn.isNoBackslashEscapesSet() ? StringUtils.SEARCH_MODE__MRK_COM_WS : StringUtils.SEARCH_MODE__ALL);
            }
            else {
                idx = procName.indexOf(".");
            }
            if (idx > 0) {
                catalog = StringUtils.unQuoteIdentifier(procName.substring(0, idx), this.quotedId);
                procNameToCall = procName;
            }
            else {
                procNameToCall = procName;
            }
            this.getCallStmtParameterTypes(catalog, procNameToCall, procType, columnNamePattern, resultRows, fields.length == 17);
        }
        return this.buildResultSet(fields, resultRows);
    }
    
    @Override
    public ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
        final Field[] fields = this.createFieldMetadataForGetProcedures();
        return this.getProceduresAndOrFunctions(fields, catalog, schemaPattern, procedureNamePattern, true, true);
    }
    
    protected Field[] createFieldMetadataForGetProcedures() {
        final Field[] fields = { new Field("", "PROCEDURE_CAT", 1, 255), new Field("", "PROCEDURE_SCHEM", 1, 255), new Field("", "PROCEDURE_NAME", 1, 255), new Field("", "reserved1", 1, 0), new Field("", "reserved2", 1, 0), new Field("", "reserved3", 1, 0), new Field("", "REMARKS", 1, 255), new Field("", "PROCEDURE_TYPE", 5, 6), new Field("", "SPECIFIC_NAME", 1, 255) };
        return fields;
    }
    
    protected ResultSet getProceduresAndOrFunctions(final Field[] fields, final String catalog, final String schemaPattern, String procedureNamePattern, final boolean returnProcedures, final boolean returnFunctions) throws SQLException {
        if (procedureNamePattern == null || procedureNamePattern.length() == 0) {
            if (!this.conn.getNullNamePatternMatchesAll()) {
                throw SQLError.createSQLException("Procedure name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
            }
            procedureNamePattern = "%";
        }
        final ArrayList<ResultSetRow> procedureRows = new ArrayList<ResultSetRow>();
        if (this.supportsStoredProcedures()) {
            final String procNamePattern = procedureNamePattern;
            final List<ComparableWrapper<String, ResultSetRow>> procedureRowsToSort = new ArrayList<ComparableWrapper<String, ResultSetRow>>();
            new IterateBlock<String>(this.getCatalogIterator(catalog)) {
                @Override
                void forEach(final String catalogStr) throws SQLException {
                    String db = catalogStr;
                    ResultSet proceduresRs = null;
                    boolean needsClientFiltering = true;
                    final StringBuilder selectFromMySQLProcSQL = new StringBuilder();
                    selectFromMySQLProcSQL.append("SELECT name, type, comment FROM mysql.proc WHERE ");
                    if (returnProcedures && !returnFunctions) {
                        selectFromMySQLProcSQL.append("type = 'PROCEDURE' AND ");
                    }
                    else if (!returnProcedures && returnFunctions) {
                        selectFromMySQLProcSQL.append("type = 'FUNCTION' AND ");
                    }
                    selectFromMySQLProcSQL.append("name LIKE ? AND db <=> ? ORDER BY name, type");
                    PreparedStatement proceduresStmt = DatabaseMetaData.this.prepareMetaDataSafeStatement(selectFromMySQLProcSQL.toString());
                    try {
                        if (db != null) {
                            if (DatabaseMetaData.this.conn.lowerCaseTableNames()) {
                                db = db.toLowerCase();
                            }
                            proceduresStmt.setString(2, db);
                        }
                        else {
                            proceduresStmt.setNull(2, 12);
                        }
                        int nameIndex = 1;
                        proceduresStmt.setString(1, procNamePattern);
                        try {
                            proceduresRs = proceduresStmt.executeQuery();
                            needsClientFiltering = false;
                            if (returnProcedures) {
                                DatabaseMetaData.this.convertToJdbcProcedureList(true, db, proceduresRs, needsClientFiltering, db, procedureRowsToSort, nameIndex);
                            }
                            if (returnFunctions) {
                                DatabaseMetaData.this.convertToJdbcFunctionList(db, proceduresRs, needsClientFiltering, db, procedureRowsToSort, nameIndex, fields);
                            }
                        }
                        catch (SQLException sqlEx2) {
                            nameIndex = (DatabaseMetaData.this.conn.versionMeetsMinimum(5, 0, 1) ? 2 : 1);
                            if (returnFunctions) {
                                proceduresStmt.close();
                                proceduresStmt = DatabaseMetaData.this.prepareMetaDataSafeStatement("SHOW FUNCTION STATUS LIKE ?");
                                proceduresStmt.setString(1, procNamePattern);
                                proceduresRs = proceduresStmt.executeQuery();
                                DatabaseMetaData.this.convertToJdbcFunctionList(db, proceduresRs, needsClientFiltering, db, procedureRowsToSort, nameIndex, fields);
                            }
                            if (returnProcedures) {
                                proceduresStmt.close();
                                proceduresStmt = DatabaseMetaData.this.prepareMetaDataSafeStatement("SHOW PROCEDURE STATUS LIKE ?");
                                proceduresStmt.setString(1, procNamePattern);
                                proceduresRs = proceduresStmt.executeQuery();
                                DatabaseMetaData.this.convertToJdbcProcedureList(false, db, proceduresRs, needsClientFiltering, db, procedureRowsToSort, nameIndex);
                            }
                        }
                    }
                    finally {
                        SQLException rethrowSqlEx = null;
                        if (proceduresRs != null) {
                            try {
                                proceduresRs.close();
                            }
                            catch (SQLException sqlEx) {
                                rethrowSqlEx = sqlEx;
                            }
                        }
                        if (proceduresStmt != null) {
                            try {
                                proceduresStmt.close();
                            }
                            catch (SQLException sqlEx) {
                                rethrowSqlEx = sqlEx;
                            }
                        }
                        if (rethrowSqlEx != null) {
                            throw rethrowSqlEx;
                        }
                    }
                }
            }.doForAll();
            Collections.sort(procedureRowsToSort);
            for (final ComparableWrapper<String, ResultSetRow> procRow : procedureRowsToSort) {
                procedureRows.add(procRow.getValue());
            }
        }
        return this.buildResultSet(fields, procedureRows);
    }
    
    @Override
    public String getProcedureTerm() throws SQLException {
        return "PROCEDURE";
    }
    
    @Override
    public int getResultSetHoldability() throws SQLException {
        return 1;
    }
    
    private void getResultsImpl(final String catalog, final String table, final String keysComment, final List<ResultSetRow> tuples, final String fkTableName, final boolean isExport) throws SQLException {
        final LocalAndReferencedColumns parsedInfo = this.parseTableStatusIntoLocalAndReferencedColumns(keysComment);
        if (isExport && !parsedInfo.referencedTable.equals(table)) {
            return;
        }
        if (parsedInfo.localColumnsList.size() != parsedInfo.referencedColumnsList.size()) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, number of local and referenced columns is not the same.", "S1000", this.getExceptionInterceptor());
        }
        final Iterator<String> localColumnNames = parsedInfo.localColumnsList.iterator();
        final Iterator<String> referColumnNames = parsedInfo.referencedColumnsList.iterator();
        int keySeqIndex = 1;
        while (localColumnNames.hasNext()) {
            final byte[][] tuple = new byte[14][];
            final String lColumnName = StringUtils.unQuoteIdentifier(localColumnNames.next(), this.quotedId);
            final String rColumnName = StringUtils.unQuoteIdentifier(referColumnNames.next(), this.quotedId);
            tuple[4] = ((catalog == null) ? new byte[0] : this.s2b(catalog));
            tuple[5] = null;
            tuple[6] = this.s2b(isExport ? fkTableName : table);
            tuple[7] = this.s2b(lColumnName);
            tuple[0] = this.s2b(parsedInfo.referencedCatalog);
            tuple[1] = null;
            tuple[2] = this.s2b(isExport ? table : parsedInfo.referencedTable);
            tuple[3] = this.s2b(rColumnName);
            tuple[8] = this.s2b(Integer.toString(keySeqIndex++));
            final int[] actions = this.getForeignKeyActions(keysComment);
            tuple[9] = this.s2b(Integer.toString(actions[1]));
            tuple[10] = this.s2b(Integer.toString(actions[0]));
            tuple[11] = this.s2b(parsedInfo.constraintName);
            tuple[12] = null;
            tuple[13] = this.s2b(Integer.toString(7));
            tuples.add(new ByteArrayRow(tuple, this.getExceptionInterceptor()));
        }
    }
    
    @Override
    public ResultSet getSchemas() throws SQLException {
        final Field[] fields = { new Field("", "TABLE_SCHEM", 1, 0), new Field("", "TABLE_CATALOG", 1, 0) };
        final ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>();
        final ResultSet results = this.buildResultSet(fields, tuples);
        return results;
    }
    
    @Override
    public String getSchemaTerm() throws SQLException {
        return "";
    }
    
    @Override
    public String getSearchStringEscape() throws SQLException {
        return "\\";
    }
    
    @Override
    public String getSQLKeywords() throws SQLException {
        if (DatabaseMetaData.mysqlKeywords != null) {
            return DatabaseMetaData.mysqlKeywords;
        }
        synchronized (DatabaseMetaData.class) {
            if (DatabaseMetaData.mysqlKeywords != null) {
                return DatabaseMetaData.mysqlKeywords;
            }
            final Set<String> mysqlKeywordSet = new TreeSet<String>();
            final StringBuilder mysqlKeywordsBuffer = new StringBuilder();
            Collections.addAll(mysqlKeywordSet, DatabaseMetaData.MYSQL_KEYWORDS);
            mysqlKeywordSet.removeAll(Arrays.asList(Util.isJdbc4() ? DatabaseMetaData.SQL2003_KEYWORDS : DatabaseMetaData.SQL92_KEYWORDS));
            for (final String keyword : mysqlKeywordSet) {
                mysqlKeywordsBuffer.append(",").append(keyword);
            }
            return DatabaseMetaData.mysqlKeywords = mysqlKeywordsBuffer.substring(1);
        }
    }
    
    @Override
    public int getSQLStateType() throws SQLException {
        if (this.conn.versionMeetsMinimum(4, 1, 0)) {
            return 2;
        }
        if (this.conn.getUseSqlStateCodes()) {
            return 2;
        }
        return 1;
    }
    
    @Override
    public String getStringFunctions() throws SQLException {
        return "ASCII,BIN,BIT_LENGTH,CHAR,CHARACTER_LENGTH,CHAR_LENGTH,CONCAT,CONCAT_WS,CONV,ELT,EXPORT_SET,FIELD,FIND_IN_SET,HEX,INSERT,INSTR,LCASE,LEFT,LENGTH,LOAD_FILE,LOCATE,LOCATE,LOWER,LPAD,LTRIM,MAKE_SET,MATCH,MID,OCT,OCTET_LENGTH,ORD,POSITION,QUOTE,REPEAT,REPLACE,REVERSE,RIGHT,RPAD,RTRIM,SOUNDEX,SPACE,STRCMP,SUBSTRING,SUBSTRING,SUBSTRING,SUBSTRING,SUBSTRING_INDEX,TRIM,UCASE,UPPER";
    }
    
    @Override
    public ResultSet getSuperTables(final String arg0, final String arg1, final String arg2) throws SQLException {
        final Field[] fields = { new Field("", "TABLE_CAT", 1, 32), new Field("", "TABLE_SCHEM", 1, 32), new Field("", "TABLE_NAME", 1, 32), new Field("", "SUPERTABLE_NAME", 1, 32) };
        return this.buildResultSet(fields, new ArrayList<ResultSetRow>());
    }
    
    @Override
    public ResultSet getSuperTypes(final String arg0, final String arg1, final String arg2) throws SQLException {
        final Field[] fields = { new Field("", "TYPE_CAT", 1, 32), new Field("", "TYPE_SCHEM", 1, 32), new Field("", "TYPE_NAME", 1, 32), new Field("", "SUPERTYPE_CAT", 1, 32), new Field("", "SUPERTYPE_SCHEM", 1, 32), new Field("", "SUPERTYPE_NAME", 1, 32) };
        return this.buildResultSet(fields, new ArrayList<ResultSetRow>());
    }
    
    @Override
    public String getSystemFunctions() throws SQLException {
        return "DATABASE,USER,SYSTEM_USER,SESSION_USER,PASSWORD,ENCRYPT,LAST_INSERT_ID,VERSION";
    }
    
    protected String getTableNameWithCase(final String table) {
        final String tableNameWithCase = this.conn.lowerCaseTableNames() ? table.toLowerCase() : table;
        return tableNameWithCase;
    }
    
    @Override
    public ResultSet getTablePrivileges(final String catalog, final String schemaPattern, String tableNamePattern) throws SQLException {
        if (tableNamePattern == null) {
            if (!this.conn.getNullNamePatternMatchesAll()) {
                throw SQLError.createSQLException("Table name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
            }
            tableNamePattern = "%";
        }
        final Field[] fields = { new Field("", "TABLE_CAT", 1, 64), new Field("", "TABLE_SCHEM", 1, 1), new Field("", "TABLE_NAME", 1, 64), new Field("", "GRANTOR", 1, 77), new Field("", "GRANTEE", 1, 77), new Field("", "PRIVILEGE", 1, 64), new Field("", "IS_GRANTABLE", 1, 3) };
        final String grantQuery = "SELECT host,db,table_name,grantor,user,table_priv FROM mysql.tables_priv WHERE db LIKE ? AND table_name LIKE ?";
        ResultSet results = null;
        final ArrayList<ResultSetRow> grantRows = new ArrayList<ResultSetRow>();
        PreparedStatement pStmt = null;
        try {
            pStmt = this.prepareMetaDataSafeStatement(grantQuery);
            pStmt.setString(1, (catalog != null && catalog.length() != 0) ? catalog : "%");
            pStmt.setString(2, tableNamePattern);
            results = pStmt.executeQuery();
            while (results.next()) {
                final String host = results.getString(1);
                final String db = results.getString(2);
                final String table = results.getString(3);
                final String grantor = results.getString(4);
                String user = results.getString(5);
                if (user == null || user.length() == 0) {
                    user = "%";
                }
                final StringBuilder fullUser = new StringBuilder(user);
                if (host != null && this.conn.getUseHostsInPrivileges()) {
                    fullUser.append("@");
                    fullUser.append(host);
                }
                String allPrivileges = results.getString(6);
                if (allPrivileges != null) {
                    allPrivileges = allPrivileges.toUpperCase(Locale.ENGLISH);
                    final StringTokenizer st = new StringTokenizer(allPrivileges, ",");
                    while (st.hasMoreTokens()) {
                        final String privilege = st.nextToken().trim();
                        ResultSet columnResults = null;
                        try {
                            columnResults = this.getColumns(catalog, schemaPattern, table, "%");
                            while (columnResults.next()) {
                                final byte[][] tuple = new byte[8][];
                                tuple[0] = this.s2b(db);
                                tuple[1] = null;
                                tuple[2] = this.s2b(table);
                                if (grantor != null) {
                                    tuple[3] = this.s2b(grantor);
                                }
                                else {
                                    tuple[3] = null;
                                }
                                tuple[4] = this.s2b(fullUser.toString());
                                tuple[5] = this.s2b(privilege);
                                tuple[6] = null;
                                grantRows.add(new ByteArrayRow(tuple, this.getExceptionInterceptor()));
                            }
                        }
                        finally {
                            if (columnResults != null) {
                                try {
                                    columnResults.close();
                                }
                                catch (Exception ex) {}
                            }
                        }
                    }
                }
            }
        }
        finally {
            if (results != null) {
                try {
                    results.close();
                }
                catch (Exception ex2) {}
                results = null;
            }
            if (pStmt != null) {
                try {
                    pStmt.close();
                }
                catch (Exception ex3) {}
                pStmt = null;
            }
        }
        return this.buildResultSet(fields, grantRows);
    }
    
    @Override
    public ResultSet getTables(final String catalog, final String schemaPattern, String tableNamePattern, final String[] types) throws SQLException {
        if (tableNamePattern == null) {
            if (!this.conn.getNullNamePatternMatchesAll()) {
                throw SQLError.createSQLException("Table name pattern can not be NULL or empty.", "S1009", this.getExceptionInterceptor());
            }
            tableNamePattern = "%";
        }
        if (this.conn instanceof JDBC4Connection && ((JDBC4Connection)this.conn).isOracleMode()) {
            return this.getTablesOracleMode(schemaPattern, tableNamePattern, types);
        }
        final SortedMap<TableMetaDataKey, ResultSetRow> sortedRows = new TreeMap<TableMetaDataKey, ResultSetRow>();
        final ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>();
        final Statement stmt = this.conn.getMetadataSafeStatement();
        String tmpCat = "";
        if (catalog == null || catalog.length() == 0) {
            if (this.conn.getNullCatalogMeansCurrent()) {
                tmpCat = this.database;
            }
        }
        else {
            tmpCat = catalog;
        }
        final List<String> parseList = StringUtils.splitDBdotName(tableNamePattern, tmpCat, this.quotedId, this.conn.isNoBackslashEscapesSet());
        String tableNamePat;
        if (parseList.size() == 2) {
            tableNamePat = parseList.get(1);
        }
        else {
            tableNamePat = tableNamePattern;
        }
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)) {
                @Override
                void forEach(final String catalogStr) throws SQLException {
                    final boolean operatingOnSystemDB = "information_schema".equalsIgnoreCase(catalogStr) || "mysql".equalsIgnoreCase(catalogStr) || "performance_schema".equalsIgnoreCase(catalogStr);
                    ResultSet results = null;
                    try {
                        try {
                            results = stmt.executeQuery((DatabaseMetaData.this.conn.versionMeetsMinimum(5, 0, 2) ? "SHOW FULL TABLES FROM " : "SHOW TABLES FROM ") + StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()) + " LIKE " + StringUtils.quoteIdentifier(tableNamePat, "'", true));
                        }
                        catch (SQLException sqlEx) {
                            if ("08S01".equals(sqlEx.getSQLState())) {
                                throw sqlEx;
                            }
                            return;
                        }
                        boolean shouldReportTables = false;
                        boolean shouldReportViews = false;
                        boolean shouldReportSystemTables = false;
                        boolean shouldReportSystemViews = false;
                        boolean shouldReportLocalTemporaries = false;
                        if (types == null || types.length == 0) {
                            shouldReportTables = true;
                            shouldReportViews = true;
                            shouldReportSystemTables = true;
                            shouldReportSystemViews = true;
                            shouldReportLocalTemporaries = true;
                        }
                        else {
                            for (int i = 0; i < types.length; ++i) {
                                if (TableType.TABLE.equalsTo(types[i])) {
                                    shouldReportTables = true;
                                }
                                else if (TableType.VIEW.equalsTo(types[i])) {
                                    shouldReportViews = true;
                                }
                                else if (TableType.SYSTEM_TABLE.equalsTo(types[i])) {
                                    shouldReportSystemTables = true;
                                }
                                else if (TableType.SYSTEM_VIEW.equalsTo(types[i])) {
                                    shouldReportSystemViews = true;
                                }
                                else if (TableType.LOCAL_TEMPORARY.equalsTo(types[i])) {
                                    shouldReportLocalTemporaries = true;
                                }
                            }
                        }
                        int typeColumnIndex = 1;
                        boolean hasTableTypes = false;
                        if (DatabaseMetaData.this.conn.versionMeetsMinimum(5, 0, 2)) {
                            try {
                                typeColumnIndex = results.findColumn("table_type");
                                hasTableTypes = true;
                            }
                            catch (SQLException sqlEx2) {
                                try {
                                    typeColumnIndex = results.findColumn("Type");
                                    hasTableTypes = true;
                                }
                                catch (SQLException sqlEx3) {
                                    hasTableTypes = false;
                                }
                            }
                        }
                        while (results.next()) {
                            final byte[][] row = { (catalogStr == null) ? null : DatabaseMetaData.this.s2b(catalogStr), null, results.getBytes(1), null, new byte[0], null, null, null, null, null };
                            if (hasTableTypes) {
                                final String tableType = results.getString(typeColumnIndex);
                                switch (TableType.getTableTypeCompliantWith(tableType)) {
                                    case TABLE: {
                                        boolean reportTable = false;
                                        TableMetaDataKey tablesKey = null;
                                        if (operatingOnSystemDB && shouldReportSystemTables) {
                                            row[3] = TableType.SYSTEM_TABLE.asBytes();
                                            tablesKey = new TableMetaDataKey(TableType.SYSTEM_TABLE.getName(), catalogStr, null, results.getString(1));
                                            reportTable = true;
                                        }
                                        else if (!operatingOnSystemDB && shouldReportTables) {
                                            row[3] = TableType.TABLE.asBytes();
                                            tablesKey = new TableMetaDataKey(TableType.TABLE.getName(), catalogStr, null, results.getString(1));
                                            reportTable = true;
                                        }
                                        if (reportTable) {
                                            sortedRows.put(tablesKey, new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                            continue;
                                        }
                                        continue;
                                    }
                                    case VIEW: {
                                        if (shouldReportViews) {
                                            row[3] = TableType.VIEW.asBytes();
                                            sortedRows.put(new TableMetaDataKey(TableType.VIEW.getName(), catalogStr, null, results.getString(1)), new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                            continue;
                                        }
                                        continue;
                                    }
                                    case SYSTEM_TABLE: {
                                        if (shouldReportSystemTables) {
                                            row[3] = TableType.SYSTEM_TABLE.asBytes();
                                            sortedRows.put(new TableMetaDataKey(TableType.SYSTEM_TABLE.getName(), catalogStr, null, results.getString(1)), new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                            continue;
                                        }
                                        continue;
                                    }
                                    case SYSTEM_VIEW: {
                                        if (shouldReportSystemViews) {
                                            row[3] = TableType.SYSTEM_VIEW.asBytes();
                                            sortedRows.put(new TableMetaDataKey(TableType.SYSTEM_VIEW.getName(), catalogStr, null, results.getString(1)), new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                            continue;
                                        }
                                        continue;
                                    }
                                    case LOCAL_TEMPORARY: {
                                        if (shouldReportLocalTemporaries) {
                                            row[3] = TableType.LOCAL_TEMPORARY.asBytes();
                                            sortedRows.put(new TableMetaDataKey(TableType.LOCAL_TEMPORARY.getName(), catalogStr, null, results.getString(1)), new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                            continue;
                                        }
                                        continue;
                                    }
                                    default: {
                                        row[3] = TableType.TABLE.asBytes();
                                        sortedRows.put(new TableMetaDataKey(TableType.TABLE.getName(), catalogStr, null, results.getString(1)), new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                                        continue;
                                    }
                                }
                            }
                            else {
                                if (!shouldReportTables) {
                                    continue;
                                }
                                row[3] = TableType.TABLE.asBytes();
                                sortedRows.put(new TableMetaDataKey(TableType.TABLE.getName(), catalogStr, null, results.getString(1)), new ByteArrayRow(row, DatabaseMetaData.this.getExceptionInterceptor()));
                            }
                        }
                    }
                    finally {
                        if (results != null) {
                            try {
                                results.close();
                            }
                            catch (Exception ex) {}
                            results = null;
                        }
                    }
                }
            }.doForAll();
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        tuples.addAll(sortedRows.values());
        final ResultSet tables = this.buildResultSet(this.createTablesFields(), tuples);
        return tables;
    }
    
    private ResultSet getTablesOracleMode(final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
        final String str1 = "SELECT NULL AS table_cat,\n       o.owner AS table_schem,\n       o.object_name AS table_name,\n       o.object_type AS table_type,\n";
        final String str2 = "       c.comments AS remarks\n";
        final String str3 = "  FROM all_objects o, all_tab_comments c\n";
        final String str4 = "  WHERE o.owner LIKE ? ESCAPE '/'\n    AND o.object_name LIKE ? ESCAPE '/'\n";
        final String str5 = "    AND o.owner = c.owner (+)\n    AND o.object_name = c.table_name (+)\n";
        int i = 0;
        String str6 = "";
        if (types != null) {
            str6 = "    AND o.object_type IN ('xxx'";
            for (int j = 0; j < types.length; ++j) {
                if (types[j].equals("SYNONYM")) {
                    i = 1;
                }
                else {
                    str6 = str6 + ", '" + types[j] + "'";
                }
            }
            str6 += ")\n";
        }
        else {
            i = 1;
            str6 = "    AND o.object_type IN ('TABLE', 'VIEW')\n";
        }
        final String str7 = "  ORDER BY table_type, table_schem, table_name\n";
        final String str8 = "SELECT NULL AS table_cat,\n       s.owner AS table_schem,\n       s.synonym_name AS table_name,\n       'SYNONYM' AS table_table_type,\n";
        final String str9 = "       c.comments AS remarks\n";
        final String str10 = "  FROM all_synonyms s, all_objects o, all_tab_comments c\n";
        final String str11 = "  WHERE s.owner LIKE ? ESCAPE '/'\n    AND s.synonym_name LIKE ? ESCAPE '/'\n    AND s.table_owner = o.owner\n    AND s.table_name = o.object_name\n    AND o.object_type IN ('TABLE', 'VIEW')\n";
        String str12 = "";
        str12 += str1;
        str12 = str12 + str2 + str3;
        str12 += str4;
        str12 += str6;
        str12 += str5;
        if (i != 0) {
            str12 = str12 + "UNION\n" + str8;
            str12 = str12 + str9 + str10;
            str12 += str11;
            str12 += str5;
        }
        str12 += str7;
        final PreparedStatement localPreparedStatement = this.conn.prepareStatement(str12);
        localPreparedStatement.setString(1, (schemaPattern == null) ? "%" : schemaPattern);
        localPreparedStatement.setString(2, (tableNamePattern == null) ? "%" : tableNamePattern);
        if (i != 0) {
            localPreparedStatement.setString(3, (schemaPattern == null) ? "%" : schemaPattern);
            localPreparedStatement.setString(4, (tableNamePattern == null) ? "%" : tableNamePattern);
        }
        return localPreparedStatement.executeQuery();
    }
    
    protected Field[] createTablesFields() {
        final Field[] fields = { new Field("", "TABLE_CAT", 12, 255), new Field("", "TABLE_SCHEM", 12, 0), new Field("", "TABLE_NAME", 12, 255), new Field("", "TABLE_TYPE", 12, 5), new Field("", "REMARKS", 12, 0), new Field("", "TYPE_CAT", 12, 0), new Field("", "TYPE_SCHEM", 12, 0), new Field("", "TYPE_NAME", 12, 0), new Field("", "SELF_REFERENCING_COL_NAME", 12, 0), new Field("", "REF_GENERATION", 12, 0) };
        return fields;
    }
    
    @Override
    public ResultSet getTableTypes() throws SQLException {
        final ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>();
        final Field[] fields = { new Field("", "TABLE_TYPE", 12, 256) };
        final boolean minVersion5_0_1 = this.conn.versionMeetsMinimum(5, 0, 1);
        tuples.add(new ByteArrayRow(new byte[][] { TableType.LOCAL_TEMPORARY.asBytes() }, this.getExceptionInterceptor()));
        tuples.add(new ByteArrayRow(new byte[][] { TableType.SYSTEM_TABLE.asBytes() }, this.getExceptionInterceptor()));
        if (minVersion5_0_1) {
            tuples.add(new ByteArrayRow(new byte[][] { TableType.SYSTEM_VIEW.asBytes() }, this.getExceptionInterceptor()));
        }
        tuples.add(new ByteArrayRow(new byte[][] { TableType.TABLE.asBytes() }, this.getExceptionInterceptor()));
        if (minVersion5_0_1) {
            tuples.add(new ByteArrayRow(new byte[][] { TableType.VIEW.asBytes() }, this.getExceptionInterceptor()));
        }
        return this.buildResultSet(fields, tuples);
    }
    
    @Override
    public String getTimeDateFunctions() throws SQLException {
        return "DAYOFWEEK,WEEKDAY,DAYOFMONTH,DAYOFYEAR,MONTH,DAYNAME,MONTHNAME,QUARTER,WEEK,YEAR,HOUR,MINUTE,SECOND,PERIOD_ADD,PERIOD_DIFF,TO_DAYS,FROM_DAYS,DATE_FORMAT,TIME_FORMAT,CURDATE,CURRENT_DATE,CURTIME,CURRENT_TIME,NOW,SYSDATE,CURRENT_TIMESTAMP,UNIX_TIMESTAMP,FROM_UNIXTIME,SEC_TO_TIME,TIME_TO_SEC";
    }
    
    @Override
    public ResultSet getTypeInfo() throws SQLException {
        final Field[] fields = { new Field("", "TYPE_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 5), new Field("", "PRECISION", 4, 10), new Field("", "LITERAL_PREFIX", 1, 4), new Field("", "LITERAL_SUFFIX", 1, 4), new Field("", "CREATE_PARAMS", 1, 32), new Field("", "NULLABLE", 5, 5), new Field("", "CASE_SENSITIVE", 16, 3), new Field("", "SEARCHABLE", 5, 3), new Field("", "UNSIGNED_ATTRIBUTE", 16, 3), new Field("", "FIXED_PREC_SCALE", 16, 3), new Field("", "AUTO_INCREMENT", 16, 3), new Field("", "LOCAL_TYPE_NAME", 1, 32), new Field("", "MINIMUM_SCALE", 5, 5), new Field("", "MAXIMUM_SCALE", 5, 5), new Field("", "SQL_DATA_TYPE", 4, 10), new Field("", "SQL_DATETIME_SUB", 4, 10), new Field("", "NUM_PREC_RADIX", 4, 10) };
        byte[][] rowVal = null;
        final ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>();
        rowVal = new byte[][] { this.s2b("BIT"), Integer.toString(-7).getBytes(), this.s2b("1"), this.s2b(""), this.s2b(""), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("true"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("BIT"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("BOOL"), Integer.toString(-7).getBytes(), this.s2b("1"), this.s2b(""), this.s2b(""), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("true"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("BOOL"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("TINYINT"), Integer.toString(-6).getBytes(), this.s2b("3"), this.s2b(""), this.s2b(""), this.s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("true"), this.s2b("false"), this.s2b("true"), this.s2b("TINYINT"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("TINYINT UNSIGNED"), Integer.toString(-6).getBytes(), this.s2b("3"), this.s2b(""), this.s2b(""), this.s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("true"), this.s2b("false"), this.s2b("true"), this.s2b("TINYINT UNSIGNED"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("BIGINT"), Integer.toString(-5).getBytes(), this.s2b("19"), this.s2b(""), this.s2b(""), this.s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("true"), this.s2b("false"), this.s2b("true"), this.s2b("BIGINT"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("BIGINT UNSIGNED"), Integer.toString(-5).getBytes(), this.s2b("20"), this.s2b(""), this.s2b(""), this.s2b("[(M)] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("true"), this.s2b("false"), this.s2b("true"), this.s2b("BIGINT UNSIGNED"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("LONG VARBINARY"), Integer.toString(-4).getBytes(), this.s2b("16777215"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("true"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("LONG VARBINARY"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("MEDIUMBLOB"), Integer.toString(-4).getBytes(), this.s2b("16777215"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("true"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("MEDIUMBLOB"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("LONGBLOB"), Integer.toString(-4).getBytes(), Integer.toString(Integer.MAX_VALUE).getBytes(), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("true"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("LONGBLOB"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("BLOB"), Integer.toString(-4).getBytes(), this.s2b("65535"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("true"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("BLOB"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("TINYBLOB"), Integer.toString(-4).getBytes(), this.s2b("255"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("true"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("TINYBLOB"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("VARBINARY"), Integer.toString(-3).getBytes(), this.s2b(this.conn.versionMeetsMinimum(5, 0, 3) ? "65535" : "255"), this.s2b("'"), this.s2b("'"), this.s2b("(M)"), Integer.toString(1).getBytes(), this.s2b("true"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("VARBINARY"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("BINARY"), Integer.toString(-2).getBytes(), this.s2b("255"), this.s2b("'"), this.s2b("'"), this.s2b("(M)"), Integer.toString(1).getBytes(), this.s2b("true"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("BINARY"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("LONG VARCHAR"), Integer.toString(-1).getBytes(), this.s2b("16777215"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("LONG VARCHAR"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("MEDIUMTEXT"), Integer.toString(-1).getBytes(), this.s2b("16777215"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("MEDIUMTEXT"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("LONGTEXT"), Integer.toString(-1).getBytes(), Integer.toString(Integer.MAX_VALUE).getBytes(), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("LONGTEXT"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("TEXT"), Integer.toString(-1).getBytes(), this.s2b("65535"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("TEXT"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("TINYTEXT"), Integer.toString(-1).getBytes(), this.s2b("255"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("TINYTEXT"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("CHAR"), Integer.toString(1).getBytes(), this.s2b("255"), this.s2b("'"), this.s2b("'"), this.s2b("(M)"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("CHAR"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        int decimalPrecision = 254;
        if (this.conn.versionMeetsMinimum(5, 0, 3)) {
            if (this.conn.versionMeetsMinimum(5, 0, 6)) {
                decimalPrecision = 65;
            }
            else {
                decimalPrecision = 64;
            }
        }
        rowVal = new byte[][] { this.s2b("NUMERIC"), Integer.toString(2).getBytes(), this.s2b(String.valueOf(decimalPrecision)), this.s2b(""), this.s2b(""), this.s2b("[(M[,D])] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("true"), this.s2b("NUMERIC"), this.s2b("-308"), this.s2b("308"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("DECIMAL"), Integer.toString(3).getBytes(), this.s2b(String.valueOf(decimalPrecision)), this.s2b(""), this.s2b(""), this.s2b("[(M[,D])] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("true"), this.s2b("DECIMAL"), this.s2b("-308"), this.s2b("308"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("INTEGER"), Integer.toString(4).getBytes(), this.s2b("10"), this.s2b(""), this.s2b(""), this.s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("true"), this.s2b("false"), this.s2b("true"), this.s2b("INTEGER"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("INTEGER UNSIGNED"), Integer.toString(4).getBytes(), this.s2b("10"), this.s2b(""), this.s2b(""), this.s2b("[(M)] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("true"), this.s2b("false"), this.s2b("true"), this.s2b("INTEGER UNSIGNED"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("INT"), Integer.toString(4).getBytes(), this.s2b("10"), this.s2b(""), this.s2b(""), this.s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("true"), this.s2b("false"), this.s2b("true"), this.s2b("INT"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("INT UNSIGNED"), Integer.toString(4).getBytes(), this.s2b("10"), this.s2b(""), this.s2b(""), this.s2b("[(M)] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("true"), this.s2b("false"), this.s2b("true"), this.s2b("INT UNSIGNED"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("MEDIUMINT"), Integer.toString(4).getBytes(), this.s2b("7"), this.s2b(""), this.s2b(""), this.s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("true"), this.s2b("false"), this.s2b("true"), this.s2b("MEDIUMINT"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("MEDIUMINT UNSIGNED"), Integer.toString(4).getBytes(), this.s2b("8"), this.s2b(""), this.s2b(""), this.s2b("[(M)] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("true"), this.s2b("false"), this.s2b("true"), this.s2b("MEDIUMINT UNSIGNED"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("SMALLINT"), Integer.toString(5).getBytes(), this.s2b("5"), this.s2b(""), this.s2b(""), this.s2b("[(M)] [UNSIGNED] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("true"), this.s2b("false"), this.s2b("true"), this.s2b("SMALLINT"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("SMALLINT UNSIGNED"), Integer.toString(5).getBytes(), this.s2b("5"), this.s2b(""), this.s2b(""), this.s2b("[(M)] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("true"), this.s2b("false"), this.s2b("true"), this.s2b("SMALLINT UNSIGNED"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("FLOAT"), Integer.toString(7).getBytes(), this.s2b("10"), this.s2b(""), this.s2b(""), this.s2b("[(M,D)] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("true"), this.s2b("FLOAT"), this.s2b("-38"), this.s2b("38"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("DOUBLE"), Integer.toString(8).getBytes(), this.s2b("17"), this.s2b(""), this.s2b(""), this.s2b("[(M,D)] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("true"), this.s2b("DOUBLE"), this.s2b("-308"), this.s2b("308"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("DOUBLE PRECISION"), Integer.toString(8).getBytes(), this.s2b("17"), this.s2b(""), this.s2b(""), this.s2b("[(M,D)] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("true"), this.s2b("DOUBLE PRECISION"), this.s2b("-308"), this.s2b("308"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("REAL"), Integer.toString(8).getBytes(), this.s2b("17"), this.s2b(""), this.s2b(""), this.s2b("[(M,D)] [ZEROFILL]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("true"), this.s2b("REAL"), this.s2b("-308"), this.s2b("308"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("VARCHAR"), Integer.toString(12).getBytes(), this.s2b(this.conn.versionMeetsMinimum(5, 0, 3) ? "65535" : "255"), this.s2b("'"), this.s2b("'"), this.s2b("(M)"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("VARCHAR"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("ENUM"), Integer.toString(12).getBytes(), this.s2b("65535"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("ENUM"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("SET"), Integer.toString(12).getBytes(), this.s2b("64"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("SET"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("DATE"), Integer.toString(91).getBytes(), this.s2b("0"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("DATE"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("TIME"), Integer.toString(92).getBytes(), this.s2b("0"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("TIME"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("DATETIME"), Integer.toString(93).getBytes(), this.s2b("0"), this.s2b("'"), this.s2b("'"), this.s2b(""), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("DATETIME"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        rowVal = new byte[][] { this.s2b("TIMESTAMP"), Integer.toString(93).getBytes(), this.s2b("0"), this.s2b("'"), this.s2b("'"), this.s2b("[(M)]"), Integer.toString(1).getBytes(), this.s2b("false"), Integer.toString(3).getBytes(), this.s2b("false"), this.s2b("false"), this.s2b("false"), this.s2b("TIMESTAMP"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("0"), this.s2b("10") };
        tuples.add(new ByteArrayRow(rowVal, this.getExceptionInterceptor()));
        return this.buildResultSet(fields, tuples);
    }
    
    @Override
    public ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException {
        final Field[] fields = { new Field("", "TYPE_CAT", 12, 32), new Field("", "TYPE_SCHEM", 12, 32), new Field("", "TYPE_NAME", 12, 32), new Field("", "CLASS_NAME", 12, 32), new Field("", "DATA_TYPE", 4, 10), new Field("", "REMARKS", 12, 32), new Field("", "BASE_TYPE", 5, 10) };
        final ArrayList<ResultSetRow> tuples = new ArrayList<ResultSetRow>();
        return this.buildResultSet(fields, tuples);
    }
    
    @Override
    public String getURL() throws SQLException {
        return this.conn.getURL();
    }
    
    @Override
    public String getUserName() throws SQLException {
        if (this.conn.getUseHostsInPrivileges()) {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = this.conn.getMetadataSafeStatement();
                if (this.conn.getIO().isOracleMode()) {
                    rs = stmt.executeQuery("SELECT USER FROM DUAL");
                }
                else {
                    rs = stmt.executeQuery("SELECT USER()");
                }
                rs.next();
                return rs.getString(1);
            }
            finally {
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (Exception ex) {
                        AssertionFailedException.shouldNotHappen(ex);
                    }
                    rs = null;
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    }
                    catch (Exception ex) {
                        AssertionFailedException.shouldNotHappen(ex);
                    }
                    stmt = null;
                }
            }
        }
        return this.conn.getUser();
    }
    
    @Override
    public ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException {
        if (table == null) {
            throw SQLError.createSQLException("Table not specified.", "S1009", this.getExceptionInterceptor());
        }
        final Field[] fields = { new Field("", "SCOPE", 5, 5), new Field("", "COLUMN_NAME", 1, 32), new Field("", "DATA_TYPE", 4, 5), new Field("", "TYPE_NAME", 1, 16), new Field("", "COLUMN_SIZE", 4, 16), new Field("", "BUFFER_LENGTH", 4, 16), new Field("", "DECIMAL_DIGITS", 5, 16), new Field("", "PSEUDO_COLUMN", 5, 5) };
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        final Statement stmt = this.conn.getMetadataSafeStatement();
        try {
            new IterateBlock<String>(this.getCatalogIterator(catalog)) {
                @Override
                void forEach(final String catalogStr) throws SQLException {
                    ResultSet results = null;
                    final boolean with_where = DatabaseMetaData.this.conn.versionMeetsMinimum(5, 0, 0);
                    try {
                        StringBuilder whereBuf = new StringBuilder(" Extra LIKE '%on update CURRENT_TIMESTAMP%'");
                        final List<String> rsFields = new ArrayList<String>();
                        if (!DatabaseMetaData.this.conn.versionMeetsMinimum(5, 1, 23)) {
                            whereBuf = new StringBuilder();
                            boolean firstTime = true;
                            final String query = "SHOW CREATE TABLE " + DatabaseMetaData.this.getFullyQualifiedName(catalogStr, table);
                            results = stmt.executeQuery(query);
                            while (results.next()) {
                                final String createTableString = results.getString(2);
                                final StringTokenizer lineTokenizer = new StringTokenizer(createTableString, "\n");
                                while (lineTokenizer.hasMoreTokens()) {
                                    final String line = lineTokenizer.nextToken().trim();
                                    if (StringUtils.indexOfIgnoreCase(line, "on update CURRENT_TIMESTAMP") > -1) {
                                        boolean usingBackTicks = true;
                                        int beginPos = line.indexOf(DatabaseMetaData.this.quotedId);
                                        if (beginPos == -1) {
                                            beginPos = line.indexOf("\"");
                                            usingBackTicks = false;
                                        }
                                        if (beginPos == -1) {
                                            continue;
                                        }
                                        int endPos = -1;
                                        if (usingBackTicks) {
                                            endPos = line.indexOf(DatabaseMetaData.this.quotedId, beginPos + 1);
                                        }
                                        else {
                                            endPos = line.indexOf("\"", beginPos + 1);
                                        }
                                        if (endPos == -1) {
                                            continue;
                                        }
                                        if (with_where) {
                                            if (!firstTime) {
                                                whereBuf.append(" or");
                                            }
                                            else {
                                                firstTime = false;
                                            }
                                            whereBuf.append(" Field='");
                                            whereBuf.append(line.substring(beginPos + 1, endPos));
                                            whereBuf.append("'");
                                        }
                                        else {
                                            rsFields.add(line.substring(beginPos + 1, endPos));
                                        }
                                    }
                                }
                            }
                        }
                        if (whereBuf.length() > 0 || rsFields.size() > 0) {
                            final StringBuilder queryBuf = new StringBuilder("SHOW COLUMNS FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(table, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            queryBuf.append(" FROM ");
                            queryBuf.append(StringUtils.quoteIdentifier(catalogStr, DatabaseMetaData.this.quotedId, DatabaseMetaData.this.conn.getPedantic()));
                            if (with_where) {
                                queryBuf.append(" WHERE");
                                queryBuf.append(whereBuf.toString());
                            }
                            results = stmt.executeQuery(queryBuf.toString());
                            while (results.next()) {
                                if (with_where || rsFields.contains(results.getString("Field"))) {
                                    final TypeDescriptor typeDesc = new TypeDescriptor(results.getString("Type"), results.getString("Null"));
                                    final byte[][] rowVal = { null, results.getBytes("Field"), Short.toString(typeDesc.dataType).getBytes(), DatabaseMetaData.this.s2b(typeDesc.typeName), (typeDesc.columnSize == null) ? null : DatabaseMetaData.this.s2b(typeDesc.columnSize.toString()), DatabaseMetaData.this.s2b(Integer.toString(typeDesc.bufferLength)), (typeDesc.decimalDigits == null) ? null : DatabaseMetaData.this.s2b(typeDesc.decimalDigits.toString()), Integer.toString(1).getBytes() };
                                    rows.add(new ByteArrayRow(rowVal, DatabaseMetaData.this.getExceptionInterceptor()));
                                }
                            }
                        }
                    }
                    catch (SQLException sqlEx) {
                        if (!"42S02".equals(sqlEx.getSQLState())) {
                            throw sqlEx;
                        }
                    }
                    finally {
                        if (results != null) {
                            try {
                                results.close();
                            }
                            catch (Exception ex) {}
                            results = null;
                        }
                    }
                }
            }.doForAll();
        }
        finally {
            if (stmt != null) {
                stmt.close();
            }
        }
        return this.buildResultSet(fields, rows);
    }
    
    @Override
    public boolean insertsAreDetected(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean isCatalogAtStart() throws SQLException {
        return true;
    }
    
    @Override
    public boolean isReadOnly() throws SQLException {
        return false;
    }
    
    @Override
    public boolean locatorsUpdateCopy() throws SQLException {
        return !this.conn.getEmulateLocators();
    }
    
    @Override
    public boolean nullPlusNonNullIsNull() throws SQLException {
        return true;
    }
    
    @Override
    public boolean nullsAreSortedAtEnd() throws SQLException {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedAtStart() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 2) && !this.conn.versionMeetsMinimum(4, 0, 11);
    }
    
    @Override
    public boolean nullsAreSortedHigh() throws SQLException {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedLow() throws SQLException {
        return !this.nullsAreSortedHigh();
    }
    
    @Override
    public boolean othersDeletesAreVisible(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean othersInsertsAreVisible(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean othersUpdatesAreVisible(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean ownDeletesAreVisible(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean ownInsertsAreVisible(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean ownUpdatesAreVisible(final int type) throws SQLException {
        return false;
    }
    
    protected LocalAndReferencedColumns parseTableStatusIntoLocalAndReferencedColumns(String keysComment) throws SQLException {
        final String columnsDelimitter = ",";
        final int indexOfOpenParenLocalColumns = StringUtils.indexOfIgnoreCase(0, keysComment, "(", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
        if (indexOfOpenParenLocalColumns == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find start of local columns list.", "S1000", this.getExceptionInterceptor());
        }
        final String constraintName = StringUtils.unQuoteIdentifier(keysComment.substring(0, indexOfOpenParenLocalColumns).trim(), this.quotedId);
        keysComment = keysComment.substring(indexOfOpenParenLocalColumns, keysComment.length());
        final String keysCommentTrimmed = keysComment.trim();
        final int indexOfCloseParenLocalColumns = StringUtils.indexOfIgnoreCase(0, keysCommentTrimmed, ")", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
        if (indexOfCloseParenLocalColumns == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find end of local columns list.", "S1000", this.getExceptionInterceptor());
        }
        final String localColumnNamesString = keysCommentTrimmed.substring(1, indexOfCloseParenLocalColumns);
        final int indexOfRefer = StringUtils.indexOfIgnoreCase(0, keysCommentTrimmed, "REFER ", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
        if (indexOfRefer == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find start of referenced tables list.", "S1000", this.getExceptionInterceptor());
        }
        final int indexOfOpenParenReferCol = StringUtils.indexOfIgnoreCase(indexOfRefer, keysCommentTrimmed, "(", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__MRK_COM_WS);
        if (indexOfOpenParenReferCol == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find start of referenced columns list.", "S1000", this.getExceptionInterceptor());
        }
        final String referCatalogTableString = keysCommentTrimmed.substring(indexOfRefer + "REFER ".length(), indexOfOpenParenReferCol);
        final int indexOfSlash = StringUtils.indexOfIgnoreCase(0, referCatalogTableString, "/", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__MRK_COM_WS);
        if (indexOfSlash == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find name of referenced catalog.", "S1000", this.getExceptionInterceptor());
        }
        final String referCatalog = StringUtils.unQuoteIdentifier(referCatalogTableString.substring(0, indexOfSlash), this.quotedId);
        final String referTable = StringUtils.unQuoteIdentifier(referCatalogTableString.substring(indexOfSlash + 1).trim(), this.quotedId);
        final int indexOfCloseParenRefer = StringUtils.indexOfIgnoreCase(indexOfOpenParenReferCol, keysCommentTrimmed, ")", this.quotedId, this.quotedId, StringUtils.SEARCH_MODE__ALL);
        if (indexOfCloseParenRefer == -1) {
            throw SQLError.createSQLException("Error parsing foreign keys definition, couldn't find end of referenced columns list.", "S1000", this.getExceptionInterceptor());
        }
        final String referColumnNamesString = keysCommentTrimmed.substring(indexOfOpenParenReferCol + 1, indexOfCloseParenRefer);
        final List<String> referColumnsList = StringUtils.split(referColumnNamesString, columnsDelimitter, this.quotedId, this.quotedId, false);
        final List<String> localColumnsList = StringUtils.split(localColumnNamesString, columnsDelimitter, this.quotedId, this.quotedId, false);
        return new LocalAndReferencedColumns(localColumnsList, referColumnsList, constraintName, referCatalog, referTable);
    }
    
    protected byte[] s2b(final String s) throws SQLException {
        if (s == null) {
            return null;
        }
        return StringUtils.getBytes(s, this.conn.getCharacterSetMetadata(), this.conn.getServerCharset(), this.conn.parserKnowsUnicode(), this.conn, this.getExceptionInterceptor());
    }
    
    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return this.conn.storesLowerCaseTableName();
    }
    
    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return this.conn.storesLowerCaseTableName();
    }
    
    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return !this.conn.storesLowerCaseTableName();
    }
    
    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return !this.conn.storesLowerCaseTableName();
    }
    
    @Override
    public boolean storesUpperCaseIdentifiers() throws SQLException {
        return this.conn instanceof JDBC4Connection && ((JDBC4Connection)this.conn).isOracleMode();
    }
    
    @Override
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsAlterTableWithAddColumn() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsAlterTableWithDropColumn() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsANSI92EntryLevelSQL() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsANSI92FullSQL() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsANSI92IntermediateSQL() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsBatchUpdates() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsCatalogsInDataManipulation() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }
    
    @Override
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }
    
    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }
    
    @Override
    public boolean supportsCatalogsInProcedureCalls() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }
    
    @Override
    public boolean supportsCatalogsInTableDefinitions() throws SQLException {
        return this.conn.versionMeetsMinimum(3, 22, 0);
    }
    
    @Override
    public boolean supportsColumnAliasing() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsConvert() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsConvert(final int fromType, final int toType) throws SQLException {
        switch (fromType) {
            case -4:
            case -3:
            case -2:
            case -1:
            case 1:
            case 12: {
                switch (toType) {
                    case -6:
                    case -5:
                    case -4:
                    case -3:
                    case -2:
                    case -1:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 12:
                    case 91:
                    case 92:
                    case 93:
                    case 1111: {
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
                break;
            }
            case -7: {
                return false;
            }
            case -6:
            case -5:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8: {
                switch (toType) {
                    case -6:
                    case -5:
                    case -4:
                    case -3:
                    case -2:
                    case -1:
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 12: {
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
                break;
            }
            case 0: {
                return false;
            }
            case 1111: {
                switch (toType) {
                    case -4:
                    case -3:
                    case -2:
                    case -1:
                    case 1:
                    case 12: {
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
                break;
            }
            case 91: {
                switch (toType) {
                    case -4:
                    case -3:
                    case -2:
                    case -1:
                    case 1:
                    case 12: {
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
                break;
            }
            case 92: {
                switch (toType) {
                    case -4:
                    case -3:
                    case -2:
                    case -1:
                    case 1:
                    case 12: {
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
                break;
            }
            case 93: {
                switch (toType) {
                    case -4:
                    case -3:
                    case -2:
                    case -1:
                    case 1:
                    case 12:
                    case 91:
                    case 92: {
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
                break;
            }
            default: {
                return false;
            }
        }
    }
    
    @Override
    public boolean supportsCoreSQLGrammar() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsCorrelatedSubqueries() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }
    
    @Override
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsDifferentTableCorrelationNames() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsExpressionsInOrderBy() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsExtendedSQLGrammar() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsFullOuterJoins() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsGetGeneratedKeys() {
        return true;
    }
    
    @Override
    public boolean supportsGroupBy() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsGroupByBeyondSelect() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsGroupByUnrelated() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsIntegrityEnhancementFacility() throws SQLException {
        return this.conn.getOverrideSupportsIntegrityEnhancementFacility();
    }
    
    @Override
    public boolean supportsLikeEscapeClause() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsLimitedOuterJoins() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsMinimumSQLGrammar() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return !this.conn.lowerCaseTableNames();
    }
    
    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return !this.conn.lowerCaseTableNames();
    }
    
    @Override
    public boolean supportsMultipleOpenResults() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsMultipleResultSets() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }
    
    @Override
    public boolean supportsMultipleTransactions() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsNonNullableColumns() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOrderByUnrelated() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsOuterJoins() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsPositionedDelete() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsPositionedUpdate() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsResultSetConcurrency(final int type, final int concurrency) throws SQLException {
        switch (type) {
            case 1004: {
                if (concurrency == 1007 || concurrency == 1008) {
                    return true;
                }
                throw SQLError.createSQLException("Illegal arguments to supportsResultSetConcurrency()", "S1009", this.getExceptionInterceptor());
            }
            case 1003: {
                if (concurrency == 1007 || concurrency == 1008) {
                    return true;
                }
                throw SQLError.createSQLException("Illegal arguments to supportsResultSetConcurrency()", "S1009", this.getExceptionInterceptor());
            }
            case 1005: {
                return false;
            }
            default: {
                throw SQLError.createSQLException("Illegal arguments to supportsResultSetConcurrency()", "S1009", this.getExceptionInterceptor());
            }
        }
    }
    
    @Override
    public boolean supportsResultSetHoldability(final int holdability) throws SQLException {
        return holdability == 1;
    }
    
    @Override
    public boolean supportsResultSetType(final int type) throws SQLException {
        return type == 1004;
    }
    
    @Override
    public boolean supportsSavepoints() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 14) || this.conn.versionMeetsMinimum(4, 1, 1);
    }
    
    @Override
    public boolean supportsSchemasInDataManipulation() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInIndexDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInProcedureCalls() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInTableDefinitions() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsSelectForUpdate() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 0);
    }
    
    @Override
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }
    
    @Override
    public RowIdLifetime getRowIdLifetime() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public boolean supportsStoredProcedures() throws SQLException {
        return this.conn.versionMeetsMinimum(5, 0, 0);
    }
    
    @Override
    public boolean supportsSubqueriesInComparisons() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }
    
    @Override
    public boolean supportsSubqueriesInExists() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }
    
    @Override
    public boolean supportsSubqueriesInIns() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }
    
    @Override
    public boolean supportsSubqueriesInQuantifieds() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 1, 0);
    }
    
    @Override
    public boolean supportsTableCorrelationNames() throws SQLException {
        return true;
    }
    
    @Override
    public boolean supportsTransactionIsolationLevel(final int level) throws SQLException {
        if (!this.conn.supportsIsolationLevel()) {
            return false;
        }
        switch (level) {
            case 1:
            case 2:
            case 4:
            case 8: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    @Override
    public boolean supportsTransactions() throws SQLException {
        return this.conn.supportsTransactions();
    }
    
    @Override
    public boolean supportsUnion() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 0);
    }
    
    @Override
    public boolean supportsUnionAll() throws SQLException {
        return this.conn.versionMeetsMinimum(4, 0, 0);
    }
    
    @Override
    public boolean updatesAreDetected(final int type) throws SQLException {
        return false;
    }
    
    @Override
    public boolean usesLocalFilePerTable() throws SQLException {
        return false;
    }
    
    @Override
    public boolean usesLocalFiles() throws SQLException {
        return false;
    }
    
    @Override
    public ResultSet getClientInfoProperties() throws SQLException {
        final Field[] fields = { new Field("", "NAME", 12, 255), new Field("", "MAX_LEN", 4, 10), new Field("", "DEFAULT_VALUE", 12, 255), new Field("", "DESCRIPTION", 12, 255) };
        return buildResultSet(fields, new ArrayList<ResultSetRow>(), this.conn);
    }
    
    @Override
    public ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) throws SQLException {
        final Field[] fields = this.createFunctionColumnsFields();
        return this.getProcedureOrFunctionColumns(fields, catalog, schemaPattern, functionNamePattern, columnNamePattern, false, true);
    }
    
    protected Field[] createFunctionColumnsFields() {
        final Field[] fields = { new Field("", "FUNCTION_CAT", 12, 512), new Field("", "FUNCTION_SCHEM", 12, 512), new Field("", "FUNCTION_NAME", 12, 512), new Field("", "COLUMN_NAME", 12, 512), new Field("", "COLUMN_TYPE", 12, 64), new Field("", "DATA_TYPE", 5, 6), new Field("", "TYPE_NAME", 12, 64), new Field("", "PRECISION", 4, 12), new Field("", "LENGTH", 4, 12), new Field("", "SCALE", 5, 12), new Field("", "RADIX", 5, 6), new Field("", "NULLABLE", 5, 6), new Field("", "REMARKS", 12, 512), new Field("", "CHAR_OCTET_LENGTH", 4, 32), new Field("", "ORDINAL_POSITION", 4, 32), new Field("", "IS_NULLABLE", 12, 12), new Field("", "SPECIFIC_NAME", 12, 64) };
        return fields;
    }
    
    @Override
    public ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException {
        final Field[] fields = { new Field("", "FUNCTION_CAT", 1, 255), new Field("", "FUNCTION_SCHEM", 1, 255), new Field("", "FUNCTION_NAME", 1, 255), new Field("", "REMARKS", 1, 255), new Field("", "FUNCTION_TYPE", 5, 6), new Field("", "SPECIFIC_NAME", 1, 255) };
        return this.getProceduresAndOrFunctions(fields, catalog, schemaPattern, functionNamePattern, false, true);
    }
    
    public boolean providesQueryObjectGenerator() throws SQLException {
        return false;
    }
    
    @Override
    public ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException {
        final Field[] fields = { new Field("", "TABLE_SCHEM", 12, 255), new Field("", "TABLE_CATALOG", 12, 255) };
        return this.buildResultSet(fields, new ArrayList<ResultSetRow>());
    }
    
    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return true;
    }
    
    @Override
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    protected PreparedStatement prepareMetaDataSafeStatement(final String sql) throws SQLException {
        final PreparedStatement pStmt = this.conn.clientPrepareStatement(sql);
        if (pStmt.getMaxRows() != 0) {
            pStmt.setMaxRows(0);
        }
        ((com.alipay.oceanbase.jdbc.Statement)pStmt).setHoldResultsOpenOverClose(true);
        return pStmt;
    }
    
    @Override
    public ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        final Field[] fields = { new Field("", "TABLE_CAT", 12, 512), new Field("", "TABLE_SCHEM", 12, 512), new Field("", "TABLE_NAME", 12, 512), new Field("", "COLUMN_NAME", 12, 512), new Field("", "DATA_TYPE", 4, 12), new Field("", "COLUMN_SIZE", 4, 12), new Field("", "DECIMAL_DIGITS", 4, 12), new Field("", "NUM_PREC_RADIX", 4, 12), new Field("", "COLUMN_USAGE", 12, 512), new Field("", "REMARKS", 12, 512), new Field("", "CHAR_OCTET_LENGTH", 4, 12), new Field("", "IS_NULLABLE", 12, 512) };
        return this.buildResultSet(fields, new ArrayList<ResultSetRow>());
    }
    
    @Override
    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return true;
    }
    
    static {
        TABLE_AS_BYTES = "TABLE".getBytes();
        SYSTEM_TABLE_AS_BYTES = "SYSTEM TABLE".getBytes();
        VIEW_AS_BYTES = "VIEW".getBytes();
        Label_0126: {
            if (Util.isJdbc4()) {
                try {
                    JDBC_4_DBMD_SHOW_CTOR = Class.forName("com.alipay.oceanbase.jdbc.JDBC4DatabaseMetaData").getConstructor(MySQLConnection.class, String.class);
                    JDBC_4_DBMD_IS_CTOR = Class.forName("com.alipay.oceanbase.jdbc.JDBC4DatabaseMetaDataUsingInfoSchema").getConstructor(MySQLConnection.class, String.class);
                    break Label_0126;
                }
                catch (SecurityException e) {
                    throw new RuntimeException(e);
                }
                catch (NoSuchMethodException e2) {
                    throw new RuntimeException(e2);
                }
                catch (ClassNotFoundException e3) {
                    throw new RuntimeException(e3);
                }
            }
            JDBC_4_DBMD_IS_CTOR = null;
            JDBC_4_DBMD_SHOW_CTOR = null;
        }
        MYSQL_KEYWORDS = new String[] { "ACCESSIBLE", "ADD", "ALL", "ALTER", "ANALYZE", "AND", "AS", "ASC", "ASENSITIVE", "BEFORE", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOTH", "BY", "CALL", "CASCADE", "CASE", "CHANGE", "CHAR", "CHARACTER", "CHECK", "COLLATE", "COLUMN", "CONDITION", "CONSTRAINT", "CONTINUE", "CONVERT", "CREATE", "CROSS", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATABASE", "DATABASES", "DAY_HOUR", "DAY_MICROSECOND", "DAY_MINUTE", "DAY_SECOND", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELAYED", "DELETE", "DESC", "DESCRIBE", "DETERMINISTIC", "DISTINCT", "DISTINCTROW", "DIV", "DOUBLE", "DROP", "DUAL", "EACH", "ELSE", "ELSEIF", "ENCLOSED", "ESCAPED", "EXISTS", "EXIT", "EXPLAIN", "FALSE", "FETCH", "FLOAT", "FLOAT4", "FLOAT8", "FOR", "FORCE", "FOREIGN", "FROM", "FULLTEXT", "GENERATED", "GET", "GRANT", "GROUP", "HAVING", "HIGH_PRIORITY", "HOUR_MICROSECOND", "HOUR_MINUTE", "HOUR_SECOND", "IF", "IGNORE", "IN", "INDEX", "INFILE", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INT1", "INT2", "INT3", "INT4", "INT8", "INTEGER", "INTERVAL", "INTO", "IO_AFTER_GTIDS", "IO_BEFORE_GTIDS", "IS", "ITERATE", "JOIN", "KEY", "KEYS", "KILL", "LEADING", "LEAVE", "LEFT", "LIKE", "LIMIT", "LINEAR", "LINES", "LOAD", "LOCALTIME", "LOCALTIMESTAMP", "LOCK", "LONG", "LONGBLOB", "LONGTEXT", "LOOP", "LOW_PRIORITY", "MASTER_BIND", "MASTER_SSL_VERIFY_SERVER_CERT", "MATCH", "MAXVALUE", "MEDIUMBLOB", "MEDIUMINT", "MEDIUMTEXT", "MIDDLEINT", "MINUTE_MICROSECOND", "MINUTE_SECOND", "MOD", "MODIFIES", "NATURAL", "NOT", "NO_WRITE_TO_BINLOG", "NULL", "NUMERIC", "ON", "OPTIMIZE", "OPTIMIZER_COSTS", "OPTION", "OPTIONALLY", "OR", "ORDER", "OUT", "OUTER", "OUTFILE", "PARTITION", "PRECISION", "PRIMARY", "PROCEDURE", "PURGE", "RANGE", "READ", "READS", "READ_WRITE", "REAL", "REFERENCES", "REGEXP", "RELEASE", "RENAME", "REPEAT", "REPLACE", "REQUIRE", "RESIGNAL", "RESTRICT", "RETURN", "REVOKE", "RIGHT", "RLIKE", "SCHEMA", "SCHEMAS", "SECOND_MICROSECOND", "SELECT", "SENSITIVE", "SEPARATOR", "SET", "SHOW", "SIGNAL", "SMALLINT", "SPATIAL", "SPECIFIC", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQL_BIG_RESULT", "SQL_CALC_FOUND_ROWS", "SQL_SMALL_RESULT", "SSL", "STARTING", "STORED", "STRAIGHT_JOIN", "TABLE", "TERMINATED", "THEN", "TINYBLOB", "TINYINT", "TINYTEXT", "TO", "TRAILING", "TRIGGER", "TRUE", "UNDO", "UNION", "UNIQUE", "UNLOCK", "UNSIGNED", "UPDATE", "USAGE", "USE", "USING", "UTC_DATE", "UTC_TIME", "UTC_TIMESTAMP", "VALUES", "VARBINARY", "VARCHAR", "VARCHARACTER", "VARYING", "VIRTUAL", "WHEN", "WHERE", "WHILE", "WITH", "WRITE", "XOR", "YEAR_MONTH", "ZEROFILL" };
        SQL92_KEYWORDS = new String[] { "ABSOLUTE", "ACTION", "ADD", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "AS", "ASC", "ASSERTION", "AT", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIT", "BIT_LENGTH", "BOTH", "BY", "CASCADE", "CASCADED", "CASE", "CAST", "CATALOG", "CHAR", "CHARACTER", "CHARACTER_LENGTH", "CHAR_LENGTH", "CHECK", "CLOSE", "COALESCE", "COLLATE", "COLLATION", "COLUMN", "COMMIT", "CONNECT", "CONNECTION", "CONSTRAINT", "CONSTRAINTS", "CONTINUE", "CONVERT", "CORRESPONDING", "COUNT", "CREATE", "CROSS", "CURRENT", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_USER", "CURSOR", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DEFERRABLE", "DEFERRED", "DELETE", "DESC", "DESCRIBE", "DESCRIPTOR", "DIAGNOSTICS", "DISCONNECT", "DISTINCT", "DOMAIN", "DOUBLE", "DROP", "ELSE", "END", "END-EXEC", "ESCAPE", "EXCEPT", "EXCEPTION", "EXEC", "EXECUTE", "EXISTS", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FIRST", "FLOAT", "FOR", "FOREIGN", "FOUND", "FROM", "FULL", "GET", "GLOBAL", "GO", "GOTO", "GRANT", "GROUP", "HAVING", "HOUR", "IDENTITY", "IMMEDIATE", "IN", "INDICATOR", "INITIALLY", "INNER", "INPUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERVAL", "INTO", "IS", "ISOLATION", "JOIN", "KEY", "LANGUAGE", "LAST", "LEADING", "LEFT", "LEVEL", "LIKE", "LOCAL", "LOWER", "MATCH", "MAX", "MIN", "MINUTE", "MODULE", "MONTH", "NAMES", "NATIONAL", "NATURAL", "NCHAR", "NEXT", "NO", "NOT", "NULL", "NULLIF", "NUMERIC", "OCTET_LENGTH", "OF", "ON", "ONLY", "OPEN", "OPTION", "OR", "ORDER", "OUTER", "OUTPUT", "OVERLAPS", "PAD", "PARTIAL", "POSITION", "PRECISION", "PREPARE", "PRESERVE", "PRIMARY", "PRIOR", "PRIVILEGES", "PROCEDURE", "PUBLIC", "READ", "REAL", "REFERENCES", "RELATIVE", "RESTRICT", "REVOKE", "RIGHT", "ROLLBACK", "ROWS", "SCHEMA", "SCROLL", "SECOND", "SECTION", "SELECT", "SESSION", "SESSION_USER", "SET", "SIZE", "SMALLINT", "SOME", "SPACE", "SQL", "SQLCODE", "SQLERROR", "SQLSTATE", "SUBSTRING", "SUM", "SYSTEM_USER", "TABLE", "TEMPORARY", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSACTION", "TRANSLATE", "TRANSLATION", "TRIM", "TRUE", "UNION", "UNIQUE", "UNKNOWN", "UPDATE", "UPPER", "USAGE", "USER", "USING", "VALUE", "VALUES", "VARCHAR", "VARYING", "VIEW", "WHEN", "WHENEVER", "WHERE", "WITH", "WORK", "WRITE", "YEAR", "ZONE" };
        SQL2003_KEYWORDS = new String[] { "ABS", "ALL", "ALLOCATE", "ALTER", "AND", "ANY", "ARE", "ARRAY", "AS", "ASENSITIVE", "ASYMMETRIC", "AT", "ATOMIC", "AUTHORIZATION", "AVG", "BEGIN", "BETWEEN", "BIGINT", "BINARY", "BLOB", "BOOLEAN", "BOTH", "BY", "CALL", "CALLED", "CARDINALITY", "CASCADED", "CASE", "CAST", "CEIL", "CEILING", "CHAR", "CHARACTER", "CHARACTER_LENGTH", "CHAR_LENGTH", "CHECK", "CLOB", "CLOSE", "COALESCE", "COLLATE", "COLLECT", "COLUMN", "COMMIT", "CONDITION", "CONNECT", "CONSTRAINT", "CONVERT", "CORR", "CORRESPONDING", "COUNT", "COVAR_POP", "COVAR_SAMP", "CREATE", "CROSS", "CUBE", "CUME_DIST", "CURRENT", "CURRENT_DATE", "CURRENT_DEFAULT_TRANSFORM_GROUP", "CURRENT_PATH", "CURRENT_ROLE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "CURRENT_TRANSFORM_GROUP_FOR_TYPE", "CURRENT_USER", "CURSOR", "CYCLE", "DATE", "DAY", "DEALLOCATE", "DEC", "DECIMAL", "DECLARE", "DEFAULT", "DELETE", "DENSE_RANK", "DEREF", "DESCRIBE", "DETERMINISTIC", "DISCONNECT", "DISTINCT", "DOUBLE", "DROP", "DYNAMIC", "EACH", "ELEMENT", "ELSE", "END", "END-EXEC", "ESCAPE", "EVERY", "EXCEPT", "EXEC", "EXECUTE", "EXISTS", "EXP", "EXTERNAL", "EXTRACT", "FALSE", "FETCH", "FILTER", "FLOAT", "FLOOR", "FOR", "FOREIGN", "FREE", "FROM", "FULL", "FUNCTION", "FUSION", "GET", "GLOBAL", "GRANT", "GROUP", "GROUPING", "HAVING", "HOLD", "HOUR", "IDENTITY", "IN", "INDICATOR", "INNER", "INOUT", "INSENSITIVE", "INSERT", "INT", "INTEGER", "INTERSECT", "INTERSECTION", "INTERVAL", "INTO", "IS", "JOIN", "LANGUAGE", "LARGE", "LATERAL", "LEADING", "LEFT", "LIKE", "LN", "LOCAL", "LOCALTIME", "LOCALTIMESTAMP", "LOWER", "MATCH", "MAX", "MEMBER", "MERGE", "METHOD", "MIN", "MINUTE", "MOD", "MODIFIES", "MODULE", "MONTH", "MULTISET", "NATIONAL", "NATURAL", "NCHAR", "NCLOB", "NEW", "NO", "NONE", "NORMALIZE", "NOT", "NULL", "NULLIF", "NUMERIC", "OCTET_LENGTH", "OF", "OLD", "ON", "ONLY", "OPEN", "OR", "ORDER", "OUT", "OUTER", "OVER", "OVERLAPS", "OVERLAY", "PARAMETER", "PARTITION", "PERCENTILE_CONT", "PERCENTILE_DISC", "PERCENT_RANK", "POSITION", "POWER", "PRECISION", "PREPARE", "PRIMARY", "PROCEDURE", "RANGE", "RANK", "READS", "REAL", "RECURSIVE", "REF", "REFERENCES", "REFERENCING", "REGR_AVGX", "REGR_AVGY", "REGR_COUNT", "REGR_INTERCEPT", "REGR_R2", "REGR_SLOPE", "REGR_SXX", "REGR_SXY", "REGR_SYY", "RELEASE", "RESULT", "RETURN", "RETURNS", "REVOKE", "RIGHT", "ROLLBACK", "ROLLUP", "ROW", "ROWS", "ROW_NUMBER", "SAVEPOINT", "SCOPE", "SCROLL", "SEARCH", "SECOND", "SELECT", "SENSITIVE", "SESSION_USER", "SET", "SIMILAR", "SMALLINT", "SOME", "SPECIFIC", "SPECIFICTYPE", "SQL", "SQLEXCEPTION", "SQLSTATE", "SQLWARNING", "SQRT", "START", "STATIC", "STDDEV_POP", "STDDEV_SAMP", "SUBMULTISET", "SUBSTRING", "SUM", "SYMMETRIC", "SYSTEM", "SYSTEM_USER", "TABLE", "TABLESAMPLE", "THEN", "TIME", "TIMESTAMP", "TIMEZONE_HOUR", "TIMEZONE_MINUTE", "TO", "TRAILING", "TRANSLATE", "TRANSLATION", "TREAT", "TRIGGER", "TRIM", "TRUE", "UESCAPE", "UNION", "UNIQUE", "UNKNOWN", "UNNEST", "UPDATE", "UPPER", "USER", "USING", "VALUE", "VALUES", "VARCHAR", "VARYING", "VAR_POP", "VAR_SAMP", "WHEN", "WHENEVER", "WHERE", "WIDTH_BUCKET", "WINDOW", "WITH", "WITHIN", "WITHOUT", "YEAR" };
        DatabaseMetaData.mysqlKeywords = null;
    }
    
    protected abstract class IteratorWithCleanup<T>
    {
        abstract void close() throws SQLException;
        
        abstract boolean hasNext() throws SQLException;
        
        abstract T next() throws SQLException;
    }
    
    class LocalAndReferencedColumns
    {
        String constraintName;
        List<String> localColumnsList;
        String referencedCatalog;
        List<String> referencedColumnsList;
        String referencedTable;
        
        LocalAndReferencedColumns(final List<String> localColumns, final List<String> refColumns, final String constName, final String refCatalog, final String refTable) {
            this.localColumnsList = localColumns;
            this.referencedColumnsList = refColumns;
            this.constraintName = constName;
            this.referencedTable = refTable;
            this.referencedCatalog = refCatalog;
        }
    }
    
    protected class ResultSetIterator extends IteratorWithCleanup<String>
    {
        int colIndex;
        ResultSet resultSet;
        
        ResultSetIterator(final ResultSet rs, final int index) {
            this.resultSet = rs;
            this.colIndex = index;
        }
        
        @Override
        void close() throws SQLException {
            this.resultSet.close();
        }
        
        @Override
        boolean hasNext() throws SQLException {
            return this.resultSet.next();
        }
        
        @Override
        String next() throws SQLException {
            return this.resultSet.getObject(this.colIndex).toString();
        }
    }
    
    protected class SingleStringIterator extends IteratorWithCleanup<String>
    {
        boolean onFirst;
        String value;
        
        SingleStringIterator(final String s) {
            this.onFirst = true;
            this.value = s;
        }
        
        @Override
        void close() throws SQLException {
        }
        
        @Override
        boolean hasNext() throws SQLException {
            return this.onFirst;
        }
        
        @Override
        String next() throws SQLException {
            this.onFirst = false;
            return this.value;
        }
    }
    
    class TypeDescriptor
    {
        int bufferLength;
        int charOctetLength;
        Integer columnSize;
        short dataType;
        Integer decimalDigits;
        String isNullable;
        int nullability;
        int numPrecRadix;
        String typeName;
        
        TypeDescriptor(final String typeInfo, final String nullabilityInfo) throws SQLException {
            this.numPrecRadix = 10;
            if (typeInfo == null) {
                throw SQLError.createSQLException("NULL typeinfo not supported.", "S1009", DatabaseMetaData.this.getExceptionInterceptor());
            }
            String mysqlType = "";
            String fullMysqlType = null;
            if (typeInfo.indexOf("(") != -1) {
                mysqlType = typeInfo.substring(0, typeInfo.indexOf("(")).trim();
            }
            else {
                mysqlType = typeInfo;
            }
            final int indexOfUnsignedInMysqlType = StringUtils.indexOfIgnoreCase(mysqlType, "unsigned");
            if (indexOfUnsignedInMysqlType != -1) {
                mysqlType = mysqlType.substring(0, indexOfUnsignedInMysqlType - 1);
            }
            boolean isUnsigned = false;
            if (StringUtils.indexOfIgnoreCase(typeInfo, "unsigned") != -1 && StringUtils.indexOfIgnoreCase(typeInfo, "set") != 0 && StringUtils.indexOfIgnoreCase(typeInfo, "enum") != 0) {
                fullMysqlType = mysqlType + " unsigned";
                isUnsigned = true;
            }
            else {
                fullMysqlType = mysqlType;
            }
            if (DatabaseMetaData.this.conn.getCapitalizeTypeNames()) {
                fullMysqlType = fullMysqlType.toUpperCase(Locale.ENGLISH);
            }
            this.dataType = (short)MysqlDefs.mysqlToJavaType(mysqlType);
            this.typeName = fullMysqlType;
            if (StringUtils.startsWithIgnoreCase(typeInfo, "enum")) {
                final String temp = typeInfo.substring(typeInfo.indexOf("("), typeInfo.lastIndexOf(")"));
                final StringTokenizer tokenizer = new StringTokenizer(temp, ",");
                int maxLength = 0;
                while (tokenizer.hasMoreTokens()) {
                    maxLength = Math.max(maxLength, tokenizer.nextToken().length() - 2);
                }
                this.columnSize = maxLength;
                this.decimalDigits = null;
            }
            else if (StringUtils.startsWithIgnoreCase(typeInfo, "set")) {
                final String temp = typeInfo.substring(typeInfo.indexOf("(") + 1, typeInfo.lastIndexOf(")"));
                final StringTokenizer tokenizer = new StringTokenizer(temp, ",");
                int maxLength = 0;
                final int numElements = tokenizer.countTokens();
                if (numElements > 0) {
                    maxLength += numElements - 1;
                }
                while (tokenizer.hasMoreTokens()) {
                    final String setMember = tokenizer.nextToken().trim();
                    if (setMember.startsWith("'") && setMember.endsWith("'")) {
                        maxLength += setMember.length() - 2;
                    }
                    else {
                        maxLength += setMember.length();
                    }
                }
                this.columnSize = maxLength;
                this.decimalDigits = null;
            }
            else if (typeInfo.indexOf(",") != -1) {
                this.columnSize = Integer.valueOf(typeInfo.substring(typeInfo.indexOf("(") + 1, typeInfo.indexOf(",")).trim());
                this.decimalDigits = Integer.valueOf(typeInfo.substring(typeInfo.indexOf(",") + 1, typeInfo.indexOf(")")).trim());
            }
            else {
                this.columnSize = null;
                this.decimalDigits = null;
                if ((StringUtils.indexOfIgnoreCase(typeInfo, "char") != -1 || StringUtils.indexOfIgnoreCase(typeInfo, "text") != -1 || StringUtils.indexOfIgnoreCase(typeInfo, "blob") != -1 || StringUtils.indexOfIgnoreCase(typeInfo, "binary") != -1 || StringUtils.indexOfIgnoreCase(typeInfo, "bit") != -1) && typeInfo.indexOf("(") != -1) {
                    int endParenIndex = typeInfo.indexOf(")");
                    if (endParenIndex == -1) {
                        endParenIndex = typeInfo.length();
                    }
                    this.columnSize = Integer.valueOf(typeInfo.substring(typeInfo.indexOf("(") + 1, endParenIndex).trim());
                    if (DatabaseMetaData.this.conn.getTinyInt1isBit() && this.columnSize == 1 && StringUtils.startsWithIgnoreCase(typeInfo, 0, "tinyint")) {
                        if (DatabaseMetaData.this.conn.getTransformedBitIsBoolean()) {
                            this.dataType = 16;
                            this.typeName = "BOOLEAN";
                        }
                        else {
                            this.dataType = -7;
                            this.typeName = "BIT";
                        }
                    }
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "tinyint")) {
                    if (DatabaseMetaData.this.conn.getTinyInt1isBit() && typeInfo.indexOf("(1)") != -1) {
                        if (DatabaseMetaData.this.conn.getTransformedBitIsBoolean()) {
                            this.dataType = 16;
                            this.typeName = "BOOLEAN";
                        }
                        else {
                            this.dataType = -7;
                            this.typeName = "BIT";
                        }
                    }
                    else {
                        this.columnSize = 3;
                        this.decimalDigits = 0;
                    }
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "smallint")) {
                    this.columnSize = 5;
                    this.decimalDigits = 0;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "mediumint")) {
                    this.columnSize = (isUnsigned ? 8 : 7);
                    this.decimalDigits = 0;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "int")) {
                    this.columnSize = 10;
                    this.decimalDigits = 0;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "integer")) {
                    this.columnSize = 10;
                    this.decimalDigits = 0;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "bigint")) {
                    this.columnSize = (isUnsigned ? 20 : 19);
                    this.decimalDigits = 0;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "int24")) {
                    this.columnSize = 19;
                    this.decimalDigits = 0;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "real")) {
                    this.columnSize = 12;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "float")) {
                    this.columnSize = 12;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "decimal")) {
                    this.columnSize = 12;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "numeric")) {
                    this.columnSize = 12;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "double")) {
                    this.columnSize = 22;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "char")) {
                    this.columnSize = 1;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "varchar")) {
                    this.columnSize = 255;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "timestamp")) {
                    this.columnSize = 19;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "datetime")) {
                    this.columnSize = 19;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "date")) {
                    this.columnSize = 10;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "time")) {
                    this.columnSize = 8;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "tinyblob")) {
                    this.columnSize = 255;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "blob")) {
                    this.columnSize = 65535;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "mediumblob")) {
                    this.columnSize = 16777215;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "longblob")) {
                    this.columnSize = Integer.MAX_VALUE;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "tinytext")) {
                    this.columnSize = 255;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "text")) {
                    this.columnSize = 65535;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "mediumtext")) {
                    this.columnSize = 16777215;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "longtext")) {
                    this.columnSize = Integer.MAX_VALUE;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "enum")) {
                    this.columnSize = 255;
                }
                else if (StringUtils.startsWithIgnoreCaseAndWs(typeInfo, "set")) {
                    this.columnSize = 255;
                }
            }
            this.bufferLength = MysqlIO.getMaxBuf();
            this.numPrecRadix = 10;
            if (nullabilityInfo != null) {
                if (nullabilityInfo.equals("YES")) {
                    this.nullability = 1;
                    this.isNullable = "YES";
                }
                else if (nullabilityInfo.equals("UNKNOWN")) {
                    this.nullability = 2;
                    this.isNullable = "";
                }
                else {
                    this.nullability = 0;
                    this.isNullable = "NO";
                }
            }
            else {
                this.nullability = 0;
                this.isNullable = "NO";
            }
        }
    }
    
    protected class IndexMetaDataKey implements Comparable<IndexMetaDataKey>
    {
        Boolean columnNonUnique;
        Short columnType;
        String columnIndexName;
        Short columnOrdinalPosition;
        
        IndexMetaDataKey(final boolean columnNonUnique, final short columnType, final String columnIndexName, final short columnOrdinalPosition) {
            this.columnNonUnique = columnNonUnique;
            this.columnType = columnType;
            this.columnIndexName = columnIndexName;
            this.columnOrdinalPosition = columnOrdinalPosition;
        }
        
        @Override
        public int compareTo(final IndexMetaDataKey indexInfoKey) {
            int compareResult;
            if ((compareResult = this.columnNonUnique.compareTo(indexInfoKey.columnNonUnique)) != 0) {
                return compareResult;
            }
            if ((compareResult = this.columnType.compareTo(indexInfoKey.columnType)) != 0) {
                return compareResult;
            }
            if ((compareResult = this.columnIndexName.compareTo(indexInfoKey.columnIndexName)) != 0) {
                return compareResult;
            }
            return this.columnOrdinalPosition.compareTo(indexInfoKey.columnOrdinalPosition);
        }
        
        @Override
        public boolean equals(final Object obj) {
            return obj != null && (obj == this || (obj instanceof IndexMetaDataKey && this.compareTo((IndexMetaDataKey)obj) == 0));
        }
        
        @Override
        public int hashCode() {
            assert false : "hashCode not designed";
            return 0;
        }
    }
    
    protected class TableMetaDataKey implements Comparable<TableMetaDataKey>
    {
        String tableType;
        String tableCat;
        String tableSchem;
        String tableName;
        
        TableMetaDataKey(final String tableType, final String tableCat, final String tableSchem, final String tableName) {
            this.tableType = ((tableType == null) ? "" : tableType);
            this.tableCat = ((tableCat == null) ? "" : tableCat);
            this.tableSchem = ((tableSchem == null) ? "" : tableSchem);
            this.tableName = ((tableName == null) ? "" : tableName);
        }
        
        @Override
        public int compareTo(final TableMetaDataKey tablesKey) {
            int compareResult;
            if ((compareResult = this.tableType.compareTo(tablesKey.tableType)) != 0) {
                return compareResult;
            }
            if ((compareResult = this.tableCat.compareTo(tablesKey.tableCat)) != 0) {
                return compareResult;
            }
            if ((compareResult = this.tableSchem.compareTo(tablesKey.tableSchem)) != 0) {
                return compareResult;
            }
            return this.tableName.compareTo(tablesKey.tableName);
        }
        
        @Override
        public boolean equals(final Object obj) {
            return obj != null && (obj == this || (obj instanceof TableMetaDataKey && this.compareTo((TableMetaDataKey)obj) == 0));
        }
        
        @Override
        public int hashCode() {
            assert false : "hashCode not designed";
            return 0;
        }
    }
    
    protected class ComparableWrapper<K extends java.lang.Object, V> implements Comparable<ComparableWrapper<K, V>>
    {
        K key;
        V value;
        
        public ComparableWrapper(final K key, final V value) {
            this.key = key;
            this.value = value;
        }
        
        public K getKey() {
            return this.key;
        }
        
        public V getValue() {
            return this.value;
        }
        
        @Override
        public int compareTo(final ComparableWrapper<K, V> other) {
            return this.getKey().compareTo(other.getKey());
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof ComparableWrapper)) {
                return false;
            }
            final Object otherKey = ((ComparableWrapper)obj).getKey();
            return this.key.equals(otherKey);
        }
        
        @Override
        public int hashCode() {
            assert false : "hashCode not designed";
            return 0;
        }
        
        @Override
        public String toString() {
            return "{KEY:" + this.key + "; VALUE:" + this.value + "}";
        }
    }
    
    protected enum TableType
    {
        LOCAL_TEMPORARY("LOCAL TEMPORARY"), 
        SYSTEM_TABLE("SYSTEM TABLE"), 
        SYSTEM_VIEW("SYSTEM VIEW"), 
        TABLE("TABLE", new String[] { "BASE TABLE" }), 
        VIEW("VIEW"), 
        UNKNOWN("UNKNOWN");
        
        private String name;
        private byte[] nameAsBytes;
        private String[] synonyms;
        
        private TableType(final String tableTypeName) {
            this(tableTypeName, null);
        }
        
        private TableType(final String tableTypeName, final String[] tableTypeSynonyms) {
            this.name = tableTypeName;
            this.nameAsBytes = tableTypeName.getBytes();
            this.synonyms = tableTypeSynonyms;
        }
        
        String getName() {
            return this.name;
        }
        
        byte[] asBytes() {
            return this.nameAsBytes;
        }
        
        boolean equalsTo(final String tableTypeName) {
            return this.name.equalsIgnoreCase(tableTypeName);
        }
        
        static TableType getTableTypeEqualTo(final String tableTypeName) {
            for (final TableType tableType : values()) {
                if (tableType.equalsTo(tableTypeName)) {
                    return tableType;
                }
            }
            return TableType.UNKNOWN;
        }
        
        boolean compliesWith(final String tableTypeName) {
            if (this.equalsTo(tableTypeName)) {
                return true;
            }
            if (this.synonyms != null) {
                for (final String synonym : this.synonyms) {
                    if (synonym.equalsIgnoreCase(tableTypeName)) {
                        return true;
                    }
                }
            }
            return false;
        }
        
        static TableType getTableTypeCompliantWith(final String tableTypeName) {
            for (final TableType tableType : values()) {
                if (tableType.compliesWith(tableTypeName)) {
                    return tableType;
                }
            }
            return TableType.UNKNOWN;
        }
    }
    
    protected enum ProcedureType
    {
        PROCEDURE, 
        FUNCTION;
    }
}
