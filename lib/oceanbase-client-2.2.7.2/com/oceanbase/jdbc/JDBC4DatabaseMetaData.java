// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import com.oceanbase.jdbc.internal.com.read.resultset.SelectResultSet;
import com.oceanbase.jdbc.internal.io.input.StandardPacketInputStream;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import java.sql.RowIdLifetime;
import java.sql.SQLFeatureNotSupportedException;
import java.util.regex.Matcher;
import java.sql.PreparedStatement;
import com.oceanbase.jdbc.internal.util.constant.Version;
import java.util.SortedMap;
import java.util.TreeMap;
import com.oceanbase.jdbc.internal.util.Utils;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.Locale;
import java.util.Arrays;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.util.List;
import java.text.ParseException;
import com.oceanbase.jdbc.internal.util.dao.Identifier;
import com.oceanbase.jdbc.util.Options;
import java.sql.Connection;
import com.oceanbase.jdbc.internal.ColumnType;
import java.util.regex.Pattern;
import com.oceanbase.jdbc.internal.protocol.Protocol;

public class JDBC4DatabaseMetaData extends OceanBaseOracleDatabaseMetadata
{
    public static final String DRIVER_NAME = "OceanBase Connector/J";
    private final UrlParser urlParser;
    private final OceanBaseConnection connection;
    private boolean datePrecisionColumnExist;
    Protocol protocol;
    private static final Pattern RETURN_PATTERN;
    private static final Pattern PARAMETER_PATTERN;
    private static final Pattern ORALCLE_PARAMETER_PATTERN;
    String[] exportKeysColumnNames;
    ColumnType[] exportKeysColumnTypes;
    
    public JDBC4DatabaseMetaData(final Connection connection, final UrlParser urlParser) {
        super(urlParser, connection);
        this.datePrecisionColumnExist = true;
        this.protocol = null;
        this.exportKeysColumnNames = new String[] { "PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME", "FKTABLE_CAT", "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME", "KEY_SEQ", "UPDATE_RULE", "DELETE_RULE", "FK_NAME", "PK_NAME", "DEFERRABILITY" };
        this.exportKeysColumnTypes = new ColumnType[] { ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.SMALLINT, ColumnType.SMALLINT, ColumnType.SMALLINT, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.SMALLINT };
        this.connection = (OceanBaseConnection)connection;
        this.urlParser = urlParser;
        this.protocol = this.connection.getProtocol();
    }
    
    private static String columnTypeClause(final Options options) {
        String upperCaseWithoutSize = " UCASE(IF( COLUMN_TYPE LIKE '%(%)%', CONCAT(SUBSTRING( COLUMN_TYPE,1, LOCATE('(',COLUMN_TYPE) - 1 ), SUBSTRING(COLUMN_TYPE ,1+locate(')', COLUMN_TYPE))), COLUMN_TYPE))";
        if (options.tinyInt1isBit) {
            upperCaseWithoutSize = " IF(COLUMN_TYPE like 'tinyint(1)%', 'BIT', " + upperCaseWithoutSize + ")";
        }
        if (!options.yearIsDateType) {
            return " IF(COLUMN_TYPE IN ('year(2)', 'year(4)'), 'SMALLINT', " + upperCaseWithoutSize + ")";
        }
        return upperCaseWithoutSize;
    }
    
    private static int skipWhite(final char[] part, final int startPos) {
        for (int i = startPos; i < part.length; ++i) {
            if (!Character.isWhitespace(part[i])) {
                return i;
            }
        }
        return part.length;
    }
    
    private static int parseIdentifier(final char[] part, final int startPos, final Identifier identifier) throws ParseException {
        int pos = skipWhite(part, startPos);
        if (part[pos] != '`') {
            throw new ParseException(new String(part), pos);
        }
        ++pos;
        final StringBuilder sb = new StringBuilder();
        int quotes = 0;
        while (pos < part.length) {
            final char ch = part[pos];
            if (ch == '`') {
                ++quotes;
            }
            else {
                for (int j = 0; j < quotes / 2; ++j) {
                    sb.append('`');
                }
                if (quotes % 2 == 1) {
                    if (ch != '.') {
                        identifier.name = sb.toString();
                        return pos;
                    }
                    if (identifier.schema != null) {
                        throw new ParseException(new String(part), pos);
                    }
                    identifier.schema = sb.toString();
                    return parseIdentifier(part, pos + 1, identifier);
                }
                else {
                    quotes = 0;
                    sb.append(ch);
                }
            }
            ++pos;
        }
        throw new ParseException(new String(part), startPos);
    }
    
    private static int parseIdentifierList(final char[] part, final int startPos, final List<Identifier> list) throws ParseException {
        int pos = skipWhite(part, startPos);
        if (part[pos] != '(') {
            throw new ParseException(new String(part), pos);
        }
        ++pos;
        while (true) {
            pos = skipWhite(part, pos);
            final char ch = part[pos];
            switch (ch) {
                case ')': {
                    return pos + 1;
                }
                case '`': {
                    final Identifier id = new Identifier();
                    pos = parseIdentifier(part, pos, id);
                    list.add(id);
                    continue;
                }
                case ',': {
                    ++pos;
                    continue;
                }
                default: {
                    throw new ParseException(new String(part, startPos, part.length - startPos), startPos);
                }
            }
        }
    }
    
    private static int skipKeyword(final char[] part, final int startPos, final String keyword) throws ParseException {
        int pos = skipWhite(part, startPos);
        for (int i = 0; i < keyword.length(); ++i, ++pos) {
            if (part[pos] != keyword.charAt(i)) {
                throw new ParseException(new String(part), pos);
            }
        }
        return pos;
    }
    
    private static int getImportedKeyAction(final String actionKey) {
        if (actionKey == null) {
            return 1;
        }
        switch (actionKey) {
            case "NO ACTION": {
                return 3;
            }
            case "CASCADE": {
                return 0;
            }
            case "SET NULL": {
                return 2;
            }
            case "SET DEFAULT": {
                return 4;
            }
            case "RESTRICT": {
                return 1;
            }
            default: {
                throw new IllegalArgumentException("Illegal key action '" + actionKey + "' specified.");
            }
        }
    }
    
    private static ResultSet getImportedKeys(final String tableDef, final String tableName, final String catalog, final OceanBaseConnection connection) throws ParseException {
        final String[] columnNames = { "PKTABLE_CAT", "PKTABLE_SCHEM", "PKTABLE_NAME", "PKCOLUMN_NAME", "FKTABLE_CAT", "FKTABLE_SCHEM", "FKTABLE_NAME", "FKCOLUMN_NAME", "KEY_SEQ", "UPDATE_RULE", "DELETE_RULE", "FK_NAME", "PK_NAME", "DEFERRABILITY" };
        final ColumnType[] columnTypes = { ColumnType.VARCHAR, ColumnType.NULL, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.NULL, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.SMALLINT, ColumnType.SMALLINT, ColumnType.SMALLINT, ColumnType.VARCHAR, ColumnType.NULL, ColumnType.SMALLINT };
        final String[] parts = tableDef.split("\n");
        final List<String[]> data = new ArrayList<String[]>();
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("CONSTRAINT") || part.contains("FOREIGN KEY")) {
                final char[] partChar = part.toCharArray();
                final Identifier constraintName = new Identifier();
                int pos = skipKeyword(partChar, 0, "CONSTRAINT");
                pos = parseIdentifier(partChar, pos, constraintName);
                pos = skipKeyword(partChar, pos, "FOREIGN KEY");
                final List<Identifier> foreignKeyCols = new ArrayList<Identifier>();
                pos = parseIdentifierList(partChar, pos, foreignKeyCols);
                pos = skipKeyword(partChar, pos, "REFERENCES");
                final Identifier pkTable = new Identifier();
                pos = parseIdentifier(partChar, pos, pkTable);
                final List<Identifier> primaryKeyCols = new ArrayList<Identifier>();
                parseIdentifierList(partChar, pos, primaryKeyCols);
                if (primaryKeyCols.size() != foreignKeyCols.size()) {
                    throw new ParseException(tableDef, 0);
                }
                int onUpdateReferenceAction = 1;
                int onDeleteReferenceAction = 1;
                for (final String referenceAction : new String[] { "RESTRICT", "CASCADE", "SET NULL", "NO ACTION" }) {
                    if (part.contains("ON UPDATE " + referenceAction)) {
                        onUpdateReferenceAction = getImportedKeyAction(referenceAction);
                    }
                    if (part.contains("ON DELETE " + referenceAction)) {
                        onDeleteReferenceAction = getImportedKeyAction(referenceAction);
                    }
                }
                for (int i = 0; i < primaryKeyCols.size(); ++i) {
                    final String[] row3 = new String[columnNames.length];
                    row3[0] = pkTable.schema;
                    if (row3[0] == null) {
                        row3[0] = catalog;
                    }
                    row3[1] = null;
                    row3[2] = pkTable.name;
                    row3[3] = primaryKeyCols.get(i).name;
                    row3[4] = catalog;
                    row3[5] = null;
                    row3[6] = tableName;
                    row3[7] = foreignKeyCols.get(i).name;
                    row3[8] = Integer.toString(i + 1);
                    row3[9] = Integer.toString(onUpdateReferenceAction);
                    row3[10] = Integer.toString(onDeleteReferenceAction);
                    row3[11] = constraintName.name;
                    row3[12] = null;
                    row3[13] = Integer.toString(7);
                    data.add(row3);
                }
            }
        }
        final String[][] arr = data.toArray(new String[0][]);
        int result;
        Arrays.sort(arr, (row1, row2) -> {
            result = row1[0].compareTo(row2[0]);
            if (result == 0) {
                result = row1[2].compareTo(row2[2]);
                if (result == 0) {
                    result = row1[8].length() - row2[8].length();
                    if (result == 0) {
                        result = row1[8].compareTo(row2[8]);
                    }
                }
            }
            return result;
        });
        return JDBC4ResultSet.createResultSet(columnNames, columnTypes, arr, connection.getProtocol());
    }
    
    @Override
    public ResultSet getImportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        final String database = catalog;
        if (this.connection.getProtocol().isOracleMode()) {
            return this.keysQuery(null, null, schema, table, "ORDER BY pktable_schem, pktable_name, key_seq");
        }
        try {
            final ArrayList<String> tableNameList = this.getALlTableNames(catalog);
            final ResultSet allTableRS = this.getAllTablesResultSet(tableNameList, catalog);
            final ArrayList<String[]> list = new ArrayList<String[]>();
            while (allTableRS.next()) {
                final String[] data = new String[14];
                final String tableType = allTableRS.getString("Type");
                if (tableType != null && (tableType.toUpperCase(Locale.ROOT).equals("INNODB") || tableType.toUpperCase(Locale.ROOT).equals("SUPPORTS_FK"))) {
                    final String tableComment = allTableRS.getString("Comment").trim();
                    final String fkTableName = allTableRS.getString("Name");
                    if (tableComment == null) {
                        continue;
                    }
                    final StringTokenizer stringTokenizer = new StringTokenizer(tableComment, ";", false);
                    if (stringTokenizer.hasMoreTokens()) {
                        stringTokenizer.nextToken();
                    }
                    while (stringTokenizer.hasMoreTokens()) {
                        String keys = stringTokenizer.nextToken();
                        final int fkParamOpenIndex = keys.indexOf("(");
                        if (fkParamOpenIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find start of local columns list.");
                        }
                        final String constraintName = keys.substring(1, fkParamOpenIndex - 1);
                        keys = keys.substring(fkParamOpenIndex + 1);
                        final int fkParamCloseIndex = keys.indexOf(")");
                        if (fkParamCloseIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find start of local columns list.");
                        }
                        final String fkParamNames = keys.substring(1, fkParamCloseIndex - 1);
                        final int refIndex = keys.indexOf("REFER");
                        if (refIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find start of referenced tables list");
                        }
                        final int refParamOpenIndex = keys.indexOf("(", refIndex);
                        if (refParamOpenIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find start of referenced columns list");
                        }
                        final String refCatalogAndTable = keys.substring(refIndex + "REFER".length() + 1, refParamOpenIndex);
                        final int slashIndex = refCatalogAndTable.indexOf("/");
                        if (slashIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find name of referenced catalog.");
                        }
                        String refCatalog = refCatalogAndTable.substring(0, slashIndex);
                        refCatalog = this.removeQuoted(refCatalog);
                        String refTableName = refCatalogAndTable.substring(slashIndex + 1);
                        refTableName = this.removeQuoted(refTableName);
                        if (fkTableName.compareTo(table) != 0) {
                            continue;
                        }
                        final int refParamCloseIndex = keys.indexOf(")", refParamOpenIndex);
                        if (refParamCloseIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find end of referenced columns list.");
                        }
                        final String refParamNames = keys.substring(refParamOpenIndex + 1, refParamCloseIndex);
                        final String[] refParamList = refParamNames.split(",");
                        final String[] fkParamList = fkParamNames.split(",");
                        int fkCur = 0;
                        int refCur = 0;
                        int keySeq = 1;
                        while (fkCur < fkParamList.length) {
                            final String lColumnName = this.removeQuoted(fkParamList[fkCur++]);
                            String rColumnName = null;
                            if (refCur < refParamList.length) {
                                rColumnName = this.removeQuoted(refParamList[refCur++]);
                            }
                            int updateRuleAction = 3;
                            int deleteRuleAction = 3;
                            final int lastIndex = keys.lastIndexOf(")");
                            if (lastIndex != keys.length() - 1) {
                                final String options = keys.substring(lastIndex + 1);
                                final String optionsForUpdate = options.substring(options.indexOf("ON UPDATE"));
                                if (optionsForUpdate.startsWith("ON UPDATE CASCADE")) {
                                    updateRuleAction = 0;
                                }
                                else if (optionsForUpdate.startsWith("ON UPDATE SET NUL")) {
                                    updateRuleAction = 2;
                                }
                                else if (optionsForUpdate.startsWith("ON UPDATE RESTRICT")) {
                                    updateRuleAction = 1;
                                }
                                else if (optionsForUpdate.startsWith("ON UPDATE NO ACTION")) {
                                    updateRuleAction = 3;
                                }
                                final String optionsForDelete = options.substring(options.indexOf("ON DELETE"));
                                if (optionsForDelete.startsWith("ON DELETE CASCADE")) {
                                    deleteRuleAction = 0;
                                }
                                else if (optionsForDelete.startsWith("ON DELETE SET NUL")) {
                                    deleteRuleAction = 2;
                                }
                                else if (optionsForDelete.startsWith("ON DELETE RESTRICT")) {
                                    deleteRuleAction = 1;
                                }
                                else if (optionsForDelete.startsWith("ON DELETE NO ACTIO")) {
                                    deleteRuleAction = 3;
                                }
                            }
                            data[0] = refCatalog;
                            data[1] = null;
                            data[2] = refTableName;
                            data[3] = rColumnName;
                            data[4] = ((catalog == null) ? this.protocol.getCatalog() : catalog);
                            data[5] = null;
                            data[6] = table;
                            data[7] = lColumnName;
                            data[8] = Integer.toString(keySeq++);
                            data[9] = Integer.toString(updateRuleAction);
                            data[10] = Integer.toString(deleteRuleAction);
                            data[11] = constraintName;
                            data[12] = null;
                            data[13] = Integer.toString(7);
                            list.add(data);
                        }
                    }
                }
            }
            final String[][] val = new String[list.size()][];
            for (int j = 0; j < list.size(); ++j) {
                val[j] = list.get(j);
            }
            return JDBC4ResultSet.createResultSet(this.exportKeysColumnNames, this.exportKeysColumnTypes, val, this.protocol);
        }
        catch (SQLException e) {
            throw e;
        }
    }
    
    private String dataTypeClause(final String fullTypeColumnName) {
        final Options options = this.urlParser.getOptions();
        return " CASE data_type WHEN 'bit' THEN -7 WHEN 'tinyblob' THEN -3 WHEN 'mediumblob' THEN -4 WHEN 'longblob' THEN -4 WHEN 'blob' THEN -4 WHEN 'tinytext' THEN 12 WHEN 'mediumtext' THEN -1 WHEN 'longtext' THEN -1 WHEN 'text' THEN -1 WHEN 'date' THEN 91 WHEN 'datetime' THEN 93 WHEN 'decimal' THEN 3 WHEN 'double' THEN 8 WHEN 'enum' THEN 12 WHEN 'float' THEN 7 WHEN 'int' THEN IF( " + fullTypeColumnName + " like '%unsigned%', " + 4 + "," + 4 + ")" + " WHEN 'bigint' THEN " + -5 + " WHEN 'mediumint' THEN " + 4 + " WHEN 'null' THEN " + 0 + " WHEN 'set' THEN " + 12 + " WHEN 'smallint' THEN IF( " + fullTypeColumnName + " like '%unsigned%', " + 5 + "," + 5 + ")" + " WHEN 'varchar' THEN " + 12 + " WHEN 'varbinary' THEN " + -3 + " WHEN 'char' THEN " + 1 + " WHEN 'binary' THEN " + -2 + " WHEN 'time' THEN " + 92 + " WHEN 'timestamp' THEN " + 93 + " WHEN 'tinyint' THEN " + (options.tinyInt1isBit ? ("IF(" + fullTypeColumnName + " like 'tinyint(1)%'," + -7 + "," + -6 + ") ") : Integer.valueOf(-6)) + " WHEN 'year' THEN " + (options.yearIsDateType ? 91 : 5) + " ELSE " + 1111 + " END ";
    }
    
    private String escapeQuote(final String value) {
        if (value == null) {
            return "NULL";
        }
        return "'" + Utils.escapeString(value, this.connection.getProtocol().noBackslashEscapes()) + "'";
    }
    
    private String catalogCond(final String columnName, final String catalog) {
        if (catalog == null) {
            if (this.connection.nullCatalogMeansCurrent) {
                return "(ISNULL(database()) OR (" + columnName + " = database()))";
            }
            return "(1 = 1)";
        }
        else {
            if (catalog.isEmpty()) {
                return "(ISNULL(database()) OR (" + columnName + " = database()))";
            }
            return "(" + columnName + " = " + this.escapeQuote(catalog) + ")";
        }
    }
    
    private String patternCond(final String columnName, final String tableName) {
        if (tableName == null) {
            return "";
        }
        final String predicate = (tableName.indexOf(37) == -1 && tableName.indexOf(95) == -1) ? "=" : "LIKE";
        return " AND " + columnName + " " + predicate + " '" + Utils.escapeString(tableName, true) + "' ";
    }
    
    @Override
    public ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getPrimaryKeys(catalog, schema, table);
        }
        final String sql = "SELECT A.TABLE_SCHEMA TABLE_CAT, NULL TABLE_SCHEM, A.TABLE_NAME, A.COLUMN_NAME, B.SEQ_IN_INDEX KEY_SEQ, B.INDEX_NAME PK_NAME  FROM INFORMATION_SCHEMA.COLUMNS A, INFORMATION_SCHEMA.STATISTICS B WHERE A.COLUMN_KEY in ('PRI','pri') AND B.INDEX_NAME='PRIMARY'  AND " + this.catalogCond("A.TABLE_SCHEMA", catalog) + " AND " + this.catalogCond("B.TABLE_SCHEMA", catalog) + this.patternCond("A.TABLE_NAME", table) + this.patternCond("B.TABLE_NAME", table) + " AND A.TABLE_SCHEMA = B.TABLE_SCHEMA AND A.TABLE_NAME = B.TABLE_NAME AND A.COLUMN_NAME = B.COLUMN_NAME " + " ORDER BY A.COLUMN_NAME";
        return this.executeQuery(sql);
    }
    
    @Override
    public ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getTables(catalog, schemaPattern, tableNamePattern, types);
        }
        final String[] columnNames = { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "TABLE_TYPE", "REMARKS", "TYPE_CAT", "TYPE_SCHEM", "TYPE_NAME", "SELF_REFERENCING_COL_NAME", "REF_GENERATION" };
        final ColumnType[] columnTypes = { ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR };
        final ResultSet rs = this.getTablesImpl(columnTypes, columnNames, tableNamePattern, types, catalog);
        return rs;
    }
    
    ResultSet getTablesImpl(final ColumnType[] columnTypes, final String[] columnNames, final String tableNamePattern, final String[] types, String catalog) throws SQLException {
        final boolean isSystemDb = "information_schema".equalsIgnoreCase(catalog) || "mysql".equalsIgnoreCase(catalog) || "performance_schema".equalsIgnoreCase(catalog);
        if (catalog == null || catalog.length() == 0) {
            catalog = this.connection.getCatalog();
        }
        final StringBuilder getTablesSql = new StringBuilder();
        getTablesSql.append("SHOW FULL TABLES FROM ");
        getTablesSql.append("`");
        getTablesSql.append(catalog);
        getTablesSql.append("` LIKE '");
        if (tableNamePattern == null) {
            getTablesSql.append("%");
        }
        else {
            getTablesSql.append(tableNamePattern);
        }
        getTablesSql.append("'");
        final ResultSet rs = this.executeQuery(getTablesSql.toString());
        boolean isTables = false;
        boolean isViews = false;
        boolean isSystemTables = false;
        boolean isSystemViews = false;
        boolean isLocalTemporaries = false;
        if (types == null || types.length == 0) {
            isTables = true;
            isViews = true;
            isSystemTables = true;
            isSystemViews = true;
            isLocalTemporaries = true;
        }
        else {
            for (int i = 0; i < types.length; ++i) {
                if ("TABLE".equals(types[i]) || "BASE TABLE".equals(types[i])) {
                    isTables = true;
                }
                else if ("VIEW".equals(types[i])) {
                    isViews = true;
                }
                else if ("SYSTEM TABLE".equals(types[i])) {
                    isSystemTables = true;
                }
                else if ("SYSTEM VIEW".equals(types[i])) {
                    isSystemViews = true;
                }
                else if ("LOCAL TEMPORARY".equals(types[i])) {
                    isLocalTemporaries = true;
                }
            }
        }
        int columnIndex = 1;
        final boolean hasTableType = true;
        columnIndex = rs.findColumn("table_type");
        final SortedMap<TableMetaData, String[]> map = new TreeMap<TableMetaData, String[]>();
        while (rs.next()) {
            final String[] data = new String[10];
            boolean shouldPut = false;
            data[0] = catalog;
            data[1] = null;
            data[2] = rs.getString(1);
            data[4] = new byte[0].toString();
            data[5] = null;
            data[7] = (data[6] = null);
            data[9] = (data[8] = null);
            TableMetaData key = null;
            if (hasTableType) {
                final String tableType = rs.getString(columnIndex);
                final String typeName = null;
                if ("TABLE".equals(tableType) || "BASE TABLE".equals(tableType)) {
                    if (isSystemDb && isSystemTables) {
                        shouldPut = true;
                        key = new TableMetaData("SYSTEM TABLE", catalog, null, rs.getString(1));
                    }
                    else if (!isSystemDb && isTables) {
                        shouldPut = true;
                        key = new TableMetaData("TABLE", catalog, null, rs.getString(1));
                    }
                    if (!shouldPut) {
                        continue;
                    }
                    data[3] = "TABLE";
                    map.put(key, data);
                }
                else if ("VIEW".equals(tableType)) {
                    if (!isViews) {
                        continue;
                    }
                    key = new TableMetaData("VIEW", catalog, null, rs.getString(1));
                    data[3] = "VIEW";
                    map.put(key, data);
                }
                else if ("SYSTEM VIEW".equals(tableType)) {
                    if (!isSystemViews) {
                        continue;
                    }
                    key = new TableMetaData("SYSTEM VIEW", catalog, null, rs.getString(1));
                    data[3] = "SYSTEM VIEW";
                    map.put(key, data);
                }
                else if ("SYSTEM TABLE".equals(tableType)) {
                    if (!isSystemTables) {
                        continue;
                    }
                    key = new TableMetaData("SYSTEM TABLE", catalog, null, rs.getString(1));
                    data[3] = "SYSTEM TABLE";
                    map.put(key, data);
                }
                else if ("LOCAL TEMPORARY".equals(tableType)) {
                    if (!isLocalTemporaries) {
                        continue;
                    }
                    key = new TableMetaData("LOCAL TEMPORARY", catalog, null, rs.getString(1));
                    data[3] = "LOCAL TEMPORARY";
                    map.put(key, data);
                }
                else {
                    key = new TableMetaData("TABLE", catalog, null, rs.getString(1));
                    data[3] = "TABLE";
                    map.put(key, data);
                }
            }
            else {
                key = new TableMetaData("TABLE", catalog, null, rs.getString(1));
                data[3] = "TABLE";
                map.put(key, data);
            }
        }
        final Object[] a = map.values().toArray();
        final String[][] val = new String[map.size()][];
        for (int j = 0; j < a.length; ++j) {
            val[j] = (String[])a[j];
        }
        return JDBC4ResultSet.createResultSet(columnNames, columnTypes, val, this.protocol);
    }
    
    @Override
    public ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getColumns(catalog, schemaPattern, (tableNamePattern == null) ? null : tableNamePattern, (columnNamePattern == null) ? null : columnNamePattern);
        }
        final Options options = this.urlParser.getOptions();
        final String sql = "SELECT TABLE_SCHEMA TABLE_CAT, NULL TABLE_SCHEM, TABLE_NAME, COLUMN_NAME," + this.dataTypeClause("COLUMN_TYPE") + " DATA_TYPE," + columnTypeClause(options) + " TYPE_NAME, " + " CASE DATA_TYPE" + "  WHEN 'time' THEN " + (this.datePrecisionColumnExist ? "IF(DATETIME_PRECISION = 0, 10, CAST(11 + DATETIME_PRECISION as signed integer))" : "10") + "  WHEN 'date' THEN 10" + "  WHEN 'datetime' THEN " + (this.datePrecisionColumnExist ? "IF(DATETIME_PRECISION = 0, 19, CAST(20 + DATETIME_PRECISION as signed integer))" : "19") + "  WHEN 'timestamp' THEN " + (this.datePrecisionColumnExist ? "IF(DATETIME_PRECISION = 0, 19, CAST(20 + DATETIME_PRECISION as signed integer))" : "19") + (options.yearIsDateType ? "" : " WHEN 'year' THEN 5") + "  ELSE " + "  IF(NUMERIC_PRECISION IS NULL, LEAST(CHARACTER_MAXIMUM_LENGTH," + Integer.MAX_VALUE + "), NUMERIC_PRECISION) " + " END" + " COLUMN_SIZE, 65535 BUFFER_LENGTH, " + " CONVERT (CASE DATA_TYPE" + " WHEN 'year' THEN " + (options.yearIsDateType ? "NUMERIC_SCALE" : "0") + " WHEN 'tinyint' THEN " + (options.tinyInt1isBit ? "0" : "NUMERIC_SCALE") + " ELSE NUMERIC_SCALE END, UNSIGNED INTEGER) DECIMAL_DIGITS," + " 10 NUM_PREC_RADIX, IF(IS_NULLABLE = 'yes',1,0) NULLABLE,COLUMN_COMMENT REMARKS," + " COLUMN_DEFAULT COLUMN_DEF, 0 SQL_DATA_TYPE, 0 SQL_DATETIME_SUB,  " + " LEAST(CHARACTER_OCTET_LENGTH," + Integer.MAX_VALUE + ") CHAR_OCTET_LENGTH," + " ORDINAL_POSITION, IS_NULLABLE, NULL SCOPE_CATALOG, NULL SCOPE_SCHEMA, NULL SCOPE_TABLE, NULL SOURCE_DATA_TYPE," + " IF(EXTRA = 'auto_increment','YES','NO') IS_AUTOINCREMENT, " + " IF(EXTRA in ('VIRTUAL', 'PERSISTENT', 'VIRTUAL GENERATED', 'STORED GENERATED') ,'YES','NO') IS_GENERATEDCOLUMN " + " FROM INFORMATION_SCHEMA.COLUMNS  WHERE " + this.catalogCond("TABLE_SCHEMA", catalog) + this.patternCond("TABLE_NAME", tableNamePattern) + this.patternCond("COLUMN_NAME", columnNamePattern) + " ORDER BY TABLE_CAT, TABLE_SCHEM, TABLE_NAME, ORDINAL_POSITION";
        try {
            return this.executeQuery(sql);
        }
        catch (SQLException sqlException) {
            if (sqlException.getMessage().contains("Unknown column 'DATETIME_PRECISION'")) {
                this.datePrecisionColumnExist = false;
                return this.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
            }
            throw sqlException;
        }
    }
    
    @Override
    public ResultSet getExportedKeys(final String catalog, final String schema, final String table) throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return this.keysQuery(schema, table, null, null, "ORDER BY fktable_schem, fktable_name, key_seq");
        }
        if (table == null) {
            throw new SQLException("Table not specified.");
        }
        final String sql = "SELECT KCU.REFERENCED_TABLE_SCHEMA PKTABLE_CAT, NULL PKTABLE_SCHEM,  KCU.REFERENCED_TABLE_NAME PKTABLE_NAME, KCU.REFERENCED_COLUMN_NAME PKCOLUMN_NAME, KCU.TABLE_SCHEMA FKTABLE_CAT, NULL FKTABLE_SCHEM,  KCU.TABLE_NAME FKTABLE_NAME, KCU.COLUMN_NAME FKCOLUMN_NAME, KCU.POSITION_IN_UNIQUE_CONSTRAINT KEY_SEQ, CASE update_rule    WHEN 'RESTRICT' THEN 1   WHEN 'NO ACTION' THEN 3   WHEN 'CASCADE' THEN 0   WHEN 'SET NULL' THEN 2   WHEN 'SET DEFAULT' THEN 4 END UPDATE_RULE, CASE DELETE_RULE  WHEN 'RESTRICT' THEN 1  WHEN 'NO ACTION' THEN 3  WHEN 'CASCADE' THEN 0  WHEN 'SET NULL' THEN 2  WHEN 'SET DEFAULT' THEN 4 END DELETE_RULE, RC.CONSTRAINT_NAME FK_NAME, 'PRIMARY' PK_NAME,7 DEFERRABILITY FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE KCU INNER JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS RC ON KCU.CONSTRAINT_SCHEMA = RC.CONSTRAINT_SCHEMA AND KCU.CONSTRAINT_NAME = RC.CONSTRAINT_NAME WHERE " + this.catalogCond("KCU.REFERENCED_TABLE_SCHEMA", catalog) + this.patternCond("KCU.REFERENCED_TABLE_NAME", table) + " ORDER BY FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, KEY_SEQ";
        try {
            final ArrayList<String> tableNameList = this.getALlTableNames(catalog);
            final ResultSet allTableRS = this.getAllTablesResultSet(tableNameList, catalog);
            final ArrayList<String[]> list = new ArrayList<String[]>();
            while (allTableRS.next()) {
                final String[] data = new String[14];
                final String tableType = allTableRS.getString("Type");
                if (tableType != null && (tableType.toUpperCase(Locale.ROOT).equals("INNODB") || tableType.toUpperCase(Locale.ROOT).equals("SUPPORTS_FK"))) {
                    final String tableComment = allTableRS.getString("Comment").trim();
                    final String fkTableName = allTableRS.getString("Name");
                    if (tableComment == null) {
                        continue;
                    }
                    final StringTokenizer stringTokenizer = new StringTokenizer(tableComment, ";", false);
                    if (stringTokenizer.hasMoreTokens()) {
                        stringTokenizer.nextToken();
                    }
                    while (stringTokenizer.hasMoreTokens()) {
                        String keys = stringTokenizer.nextToken();
                        final int fkParamOpenIndex = keys.indexOf("(");
                        if (fkParamOpenIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find start of local columns list.");
                        }
                        final String constraintName = keys.substring(1, fkParamOpenIndex - 1);
                        keys = keys.substring(fkParamOpenIndex + 1);
                        final int fkParamCloseIndex = keys.indexOf(")");
                        if (fkParamCloseIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find start of local columns list.");
                        }
                        final String fkParamNames = keys.substring(1, fkParamCloseIndex - 1);
                        final int refIndex = keys.indexOf("REFER");
                        if (refIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find start of referenced tables list");
                        }
                        final int refParamOpenIndex = keys.indexOf("(", refIndex);
                        if (refParamOpenIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find start of referenced columns list");
                        }
                        final String refCatalogAndTable = keys.substring(refIndex + "REFER".length() + 1, refParamOpenIndex);
                        final int slashIndex = refCatalogAndTable.indexOf("/");
                        if (slashIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find name of referenced catalog.");
                        }
                        String refCatalog = refCatalogAndTable.substring(0, slashIndex);
                        refCatalog = this.removeQuoted(refCatalog);
                        String refTableName = refCatalogAndTable.substring(slashIndex + 1);
                        refTableName = this.removeQuoted(refTableName);
                        final boolean isExportKeys = true;
                        if (!refTableName.equals(table) && isExportKeys) {
                            continue;
                        }
                        final int refParamCloseIndex = keys.indexOf(")", refParamOpenIndex);
                        if (refParamCloseIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find end of referenced columns list.");
                        }
                        final String refParamNames = keys.substring(refParamOpenIndex + 1, refParamCloseIndex);
                        final String[] refParamList = refParamNames.split(",");
                        final String[] fkParamList = fkParamNames.split(",");
                        int fkCur = 0;
                        int refCur = 0;
                        int keySeq = 1;
                        while (fkCur < fkParamList.length) {
                            final String lColumnName = this.removeQuoted(fkParamList[fkCur++]);
                            String rColumnName = null;
                            if (refCur < refParamList.length) {
                                rColumnName = this.removeQuoted(refParamList[refCur++]);
                            }
                            int updateRuleAction = 3;
                            int deleteRuleAction = 3;
                            final int lastIndex = keys.lastIndexOf(")");
                            if (lastIndex != keys.length() - 1) {
                                final String options = keys.substring(lastIndex + 1);
                                final String optionsForUpdate = options.substring(options.indexOf("ON UPDATE"));
                                if (optionsForUpdate.startsWith("ON UPDATE CASCADE")) {
                                    updateRuleAction = 0;
                                }
                                else if (optionsForUpdate.startsWith("ON UPDATE SET NUL")) {
                                    updateRuleAction = 2;
                                }
                                else if (optionsForUpdate.startsWith("ON UPDATE RESTRICT")) {
                                    updateRuleAction = 1;
                                }
                                else if (optionsForUpdate.startsWith("ON UPDATE NO ACTION")) {
                                    updateRuleAction = 3;
                                }
                                final String optionsForDelete = options.substring(options.indexOf("ON DELETE"));
                                if (optionsForDelete.startsWith("ON DELETE CASCADE")) {
                                    deleteRuleAction = 0;
                                }
                                else if (optionsForDelete.startsWith("ON DELETE SET NUL")) {
                                    deleteRuleAction = 2;
                                }
                                else if (optionsForDelete.startsWith("ON DELETE RESTRICT")) {
                                    deleteRuleAction = 1;
                                }
                                else if (optionsForDelete.startsWith("ON DELETE NO ACTIO")) {
                                    deleteRuleAction = 3;
                                }
                            }
                            data[0] = refCatalog;
                            data[1] = null;
                            data[2] = (isExportKeys ? refTableName : table);
                            data[3] = rColumnName;
                            data[4] = catalog;
                            data[5] = null;
                            data[6] = (isExportKeys ? fkTableName : table);
                            data[7] = lColumnName;
                            data[8] = Integer.toString(keySeq++);
                            data[9] = Integer.toString(updateRuleAction);
                            data[10] = Integer.toString(deleteRuleAction);
                            data[11] = constraintName;
                            data[12] = null;
                            data[13] = Integer.toString(7);
                            list.add(data);
                        }
                    }
                }
            }
            final String[][] val = new String[list.size()][];
            for (int j = 0; j < list.size(); ++j) {
                val[j] = list.get(j);
            }
            return JDBC4ResultSet.createResultSet(this.exportKeysColumnNames, this.exportKeysColumnTypes, val, this.protocol);
        }
        catch (SQLException e) {
            throw e;
        }
    }
    
    String removeQuoted(final String str) {
        if (str.startsWith("`") && str.endsWith("`")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }
    
    ArrayList<String> getALlTableNames(final String catalog) throws SQLException {
        final ResultSet rs = this.getTables(catalog, "", "%", new String[] { "TABLE" });
        final ArrayList<String> tableNameList = new ArrayList<String>();
        while (rs.next()) {
            tableNameList.add(rs.getString("TABLE_NAME"));
        }
        return tableNameList;
    }
    
    ResultSet getAllTablesResultSet(final ArrayList<String> tableNameList, String catalog) throws SQLException {
        final String[] columnNames = { "Name", "Type", "Comment" };
        final ColumnType[] columnTypes = { ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR };
        final ArrayList<String[]> list = new ArrayList<String[]>();
        if (catalog == null || catalog.length() == 0) {
            catalog = this.connection.getCatalog();
        }
        for (int i = 0; i < tableNameList.size(); ++i) {
            final StringBuilder query = new StringBuilder();
            query.append("SHOW CREATE TABLE ");
            query.append("`");
            query.append(catalog);
            query.append("`.");
            final String name = OceanBaseConnection.quoteIdentifier(tableNameList.get(i));
            query.append(name);
            final ResultSet rs = this.executeQuery(query.toString());
            final String[] data = new String[3];
            final StringBuilder sb = new StringBuilder("comment; ");
            while (rs.next()) {
                String constraintName = null;
                String columnName = null;
                String refColumnName = null;
                String referencedCatalog = null;
                String referencedTable = null;
                data[0] = rs.getString(1);
                data[1] = "SUPPORTS_FK";
                final String createTableString = rs.getString(2);
                final StringTokenizer stringTokenizer = new StringTokenizer(createTableString, "\n");
                boolean firstTime = true;
                while (stringTokenizer.hasMoreTokens()) {
                    String str = stringTokenizer.nextToken().trim();
                    if (str.toUpperCase(Locale.ROOT).startsWith("CONSTRAINT")) {
                        int beginIndex = str.indexOf("`");
                        int endIndex = 0;
                        boolean useBackTicks = false;
                        if (beginIndex == -1) {
                            beginIndex = str.indexOf("\"");
                            useBackTicks = true;
                        }
                        if (beginIndex != -1) {
                            if (!useBackTicks) {
                                endIndex = str.indexOf("`", beginIndex + 1);
                            }
                            else {
                                endIndex = str.indexOf("\"", beginIndex + 1);
                            }
                            if (endIndex != -1) {
                                constraintName = str.substring(beginIndex + 1, endIndex);
                                str = str.substring(endIndex + 1, str.length()).trim();
                            }
                        }
                    }
                    if (str.toUpperCase(Locale.ROOT).startsWith("FOREIGN KEY")) {
                        if (str.endsWith(",")) {
                            str = str.substring(0, str.length() - 1);
                        }
                        final int fkIndex = str.indexOf("FOREIGN KEY");
                        if (fkIndex != -1) {
                            final int startIndex = fkIndex + "FOREIGN KEY".length();
                            final int refIndex = str.toUpperCase(Locale.ROOT).indexOf("REFERENCES", startIndex);
                            if (refIndex != -1) {
                                final int paramOpenIndex = str.indexOf("(", startIndex);
                                final int paramCloseIndex = str.indexOf(")", paramOpenIndex);
                                if (paramCloseIndex == -1 || paramOpenIndex == -1) {
                                    throw new SQLException("Parsing REFERENCES failed !");
                                }
                                columnName = str.substring(paramOpenIndex + 1, paramCloseIndex);
                                final int afterRefIndex = refIndex + "REFERENCES".length();
                                final int referenceParamOpenIndex = str.indexOf("(", afterRefIndex);
                                if (referenceParamOpenIndex != -1) {
                                    final String refTableName = str.substring(afterRefIndex, referenceParamOpenIndex);
                                    final int referenceParamCloseIndex = str.indexOf(")", referenceParamOpenIndex);
                                    refColumnName = str.substring(referenceParamOpenIndex + 1, referenceParamCloseIndex);
                                    final int catalogEndIndex = refTableName.indexOf(".");
                                    if (catalogEndIndex != -1) {
                                        referencedCatalog = refTableName.substring(0, catalogEndIndex);
                                        referencedTable = refTableName.substring(catalogEndIndex + 1);
                                    }
                                }
                            }
                        }
                        if (!firstTime) {
                            sb.append("comment; ");
                        }
                        else {
                            firstTime = false;
                        }
                        if (constraintName != null) {
                            sb.append(constraintName);
                        }
                        else {
                            sb.append("not_availabl");
                        }
                        sb.append("(");
                        sb.append(columnName);
                        sb.append(") REFER");
                        sb.append(referencedCatalog);
                        sb.append("/");
                        sb.append(referencedTable);
                        sb.append("(");
                        sb.append(refColumnName);
                        sb.append(")");
                        final int lastParenIndex = str.lastIndexOf(")");
                        if (lastParenIndex == str.length() - 1) {
                            continue;
                        }
                        final String cascadeOptions = str.substring(lastParenIndex + 1);
                        sb.append(" ");
                        sb.append(cascadeOptions);
                    }
                }
                data[2] = sb.toString();
            }
            list.add(data);
        }
        final String[][] val = new String[list.size()][];
        for (int j = 0; j < list.size(); ++j) {
            val[j] = list.get(j);
        }
        return JDBC4ResultSet.createResultSet(columnNames, columnTypes, val, this.protocol);
    }
    
    public ResultSet getImportedKeysUsingInformationSchema(final String catalog, final String table) throws SQLException {
        if (table == null) {
            throw new SQLException("'table' parameter in getImportedKeys cannot be null");
        }
        final String sql = "SELECT KCU.REFERENCED_TABLE_SCHEMA PKTABLE_CAT, NULL PKTABLE_SCHEM,  KCU.REFERENCED_TABLE_NAME PKTABLE_NAME, KCU.REFERENCED_COLUMN_NAME PKCOLUMN_NAME, KCU.TABLE_SCHEMA FKTABLE_CAT, NULL FKTABLE_SCHEM,  KCU.TABLE_NAME FKTABLE_NAME, KCU.COLUMN_NAME FKCOLUMN_NAME, KCU.POSITION_IN_UNIQUE_CONSTRAINT KEY_SEQ, CASE update_rule    WHEN 'RESTRICT' THEN 1   WHEN 'NO ACTION' THEN 3   WHEN 'CASCADE' THEN 0   WHEN 'SET NULL' THEN 2   WHEN 'SET DEFAULT' THEN 4 END UPDATE_RULE, CASE DELETE_RULE  WHEN 'RESTRICT' THEN 1  WHEN 'NO ACTION' THEN 3  WHEN 'CASCADE' THEN 0  WHEN 'SET NULL' THEN 2  WHEN 'SET DEFAULT' THEN 4 END DELETE_RULE, RC.CONSTRAINT_NAME FK_NAME, NULL PK_NAME,7 DEFERRABILITY FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE KCU INNER JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS RC ON KCU.CONSTRAINT_SCHEMA = RC.CONSTRAINT_SCHEMA AND KCU.CONSTRAINT_NAME = RC.CONSTRAINT_NAME WHERE " + this.catalogCond("KCU.TABLE_SCHEMA", catalog) + " AND " + " KCU.TABLE_NAME = " + this.escapeQuote(table) + " ORDER BY PKTABLE_CAT, PKTABLE_SCHEM, PKTABLE_NAME, KEY_SEQ";
        return this.executeQuery(sql);
    }
    
    public ResultSet getImportedKeysUsingShowCreateTable(final String catalog, final String table) throws Exception {
        if (catalog == null || catalog.isEmpty()) {
            throw new IllegalArgumentException("catalog");
        }
        if (table == null || table.isEmpty()) {
            throw new IllegalArgumentException("table");
        }
        final ResultSet rs = this.connection.createStatement().executeQuery("SHOW CREATE TABLE " + OceanBaseConnection.quoteIdentifier(catalog) + "." + OceanBaseConnection.quoteIdentifier(table));
        if (rs.next()) {
            final String tableDef = rs.getString(2);
            return getImportedKeys(tableDef, table, catalog, this.connection);
        }
        throw new SQLException("Fail to retrieve table information using SHOW CREATE TABLE");
    }
    
    @Override
    public ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
        if (table == null) {
            throw new SQLException("'table' parameter cannot be null in getBestRowIdentifier()");
        }
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getBestRowIdentifier(catalog, schema, table, scope, nullable);
        }
        final String sql = "SELECT 0 SCOPE, COLUMN_NAME," + this.dataTypeClause("COLUMN_TYPE") + " DATA_TYPE, DATA_TYPE TYPE_NAME," + " IF(NUMERIC_PRECISION IS NULL, CHARACTER_MAXIMUM_LENGTH, NUMERIC_PRECISION) COLUMN_SIZE, 0 BUFFER_LENGTH," + " NUMERIC_SCALE DECIMAL_DIGITS," + " 1 PSEUDO_COLUMN" + " FROM INFORMATION_SCHEMA.COLUMNS" + " WHERE COLUMN_KEY IN('PRI', 'MUL', 'UNI')" + " AND " + this.catalogCond("TABLE_SCHEMA", (catalog == null) ? this.connection.getProtocol().getCatalog() : catalog) + " AND TABLE_NAME = " + this.escapeQuote(table);
        return this.executeQuery(sql);
    }
    
    @Override
    public boolean generatedKeyAlwaysReturned() {
        return true;
    }
    
    @Override
    public ResultSet getPseudoColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        return this.connection.createStatement().executeQuery("SELECT ' ' TABLE_CAT, ' ' TABLE_SCHEM,' ' TABLE_NAME, ' ' COLUMN_NAME, 0 DATA_TYPE, 0 COLUMN_SIZE, 0 DECIMAL_DIGITS,10 NUM_PREC_RADIX, ' ' COLUMN_USAGE,  ' ' REMARKS, 0 CHAR_OCTET_LENGTH, 'YES' IS_NULLABLE FROM DUAL WHERE 1=0");
    }
    
    @Override
    public boolean allProceduresAreCallable() {
        return !this.connection.getProtocol().isOracleMode() || super.allProceduresAreCallable();
    }
    
    @Override
    public boolean allTablesAreSelectable() {
        return !this.connection.getProtocol().isOracleMode() || super.allTablesAreSelectable();
    }
    
    @Override
    public String getURL() {
        return this.urlParser.getInitialUrl();
    }
    
    @Override
    public String getUserName() throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getUserName();
        }
        return this.urlParser.getUsername();
    }
    
    @Override
    public boolean isReadOnly() {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedHigh() {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedLow() {
        return !this.nullsAreSortedHigh();
    }
    
    @Override
    public boolean nullsAreSortedAtStart() {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedAtEnd() {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.nullsAreSortedAtEnd();
        }
        return !this.nullsAreSortedAtStart();
    }
    
    @Override
    public String getDatabaseProductName() throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return "Oracle";
        }
        return "MySQL";
    }
    
    @Override
    public String getDatabaseProductVersion() {
        return this.connection.getProtocol().getServerVersion();
    }
    
    @Override
    public String getDriverName() {
        return "OceanBase Connector/J";
    }
    
    @Override
    public String getDriverVersion() {
        return Version.version;
    }
    
    @Override
    public int getDriverMajorVersion() {
        return Version.majorVersion;
    }
    
    @Override
    public int getDriverMinorVersion() {
        return Version.minorVersion;
    }
    
    @Override
    public boolean usesLocalFiles() {
        return false;
    }
    
    @Override
    public boolean usesLocalFilePerTable() {
        return false;
    }
    
    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.supportsMixedCaseIdentifiers();
        }
        return this.connection.getLowercaseTableNames() == 0;
    }
    
    @Override
    public boolean storesUpperCaseIdentifiers() {
        return this.connection.getProtocol().isOracleMode() && super.storesUpperCaseIdentifiers();
    }
    
    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.storesLowerCaseIdentifiers();
        }
        return this.connection.getLowercaseTableNames() == 1;
    }
    
    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.storesMixedCaseIdentifiers();
        }
        return this.connection.getLowercaseTableNames() == 2;
    }
    
    @Override
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException {
        return this.supportsMixedCaseIdentifiers();
    }
    
    @Override
    public boolean storesUpperCaseQuotedIdentifiers() {
        return this.storesUpperCaseIdentifiers();
    }
    
    @Override
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException {
        return this.storesLowerCaseIdentifiers();
    }
    
    @Override
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException {
        return this.storesMixedCaseIdentifiers();
    }
    
    @Override
    public String getIdentifierQuoteString() {
        if (this.protocol.isOracleMode()) {
            return "\"";
        }
        return "`";
    }
    
    @Override
    public String getSQLKeywords() {
        if (this.protocol.isOracleMode()) {
            return "ACCESS, ADD, ALTER, AUDIT, CLUSTER, COLUMN, COMMENT, COMPRESS, CONNECT, DATE, DROP, EXCLUSIVE, FILE, IDENTIFIED, IMMEDIATE, INCREMENT, INDEX, INITIAL, INTERSECT, LEVEL, LOCK, LONG, MAXEXTENTS, MINUS, MODE, NOAUDIT, NOCOMPRESS, NOWAIT, NUMBER, OFFLINE, ONLINE, PCTFREE, PRIOR, all_PL_SQL_reserved_ words";
        }
        return "ACCESSIBLE,ANALYZE,ASENSITIVE,BEFORE,BIGINT,BINARY,BLOB,CALL,CHANGE,CONDITION,DATABASE,DATABASES,DAY_HOUR,DAY_MICROSECOND,DAY_MINUTE,DAY_SECOND,DELAYED,DETERMINISTIC,DISTINCTROW,DIV,DUAL,EACH,ELSEIF,ENCLOSED,ESCAPED,EXIT,EXPLAIN,FLOAT4,FLOAT8,FORCE,FULLTEXT,GENERAL,HIGH_PRIORITY,HOUR_MICROSECOND,HOUR_MINUTE,HOUR_SECOND,IF,IGNORE,IGNORE_SERVER_IDS,INDEX,INFILE,INOUT,INT1,INT2,INT3,INT4,INT8,ITERATE,KEY,KEYS,KILL,LEAVE,LIMIT,LINEAR,LINES,LOAD,LOCALTIME,LOCALTIMESTAMP,LOCK,LONG,LONGBLOB,LONGTEXT,LOOP,LOW_PRIORITY,MASTER_HEARTBEAT_PERIOD,MASTER_SSL_VERIFY_SERVER_CERT,MAXVALUE,MEDIUMBLOB,MEDIUMINT,MEDIUMTEXT,MIDDLEINT,MINUTE_MICROSECOND,MINUTE_SECOND,MOD,MODIFIES,NO_WRITE_TO_BINLOG,OPTIMIZE,OPTIONALLY,OUT,OUTFILE,PURGE,RANGE,READ_WRITE,READS,REGEXP,RELEASE,RENAME,REPEAT,REPLACE,REQUIRE,RESIGNAL,RESTRICT,RETURN,RLIKE,SCHEMAS,SECOND_MICROSECOND,SENSITIVE,SEPARATOR,SHOW,SIGNAL,SLOW,SPATIAL,SPECIFIC,SQL_BIG_RESULT,SQL_CALC_FOUND_ROWS,SQL_SMALL_RESULT,SQLEXCEPTION,SSL,STARTING,STRAIGHT_JOIN,TERMINATED,TINYBLOB,TINYINT,TINYTEXT,TRIGGER,UNDO,UNLOCK,UNSIGNED,USE,UTC_DATE,UTC_TIME,UTC_TIMESTAMP,VARBINARY,VARCHARACTER,WHILE,XOR,YEAR_MONTH,ZEROFILL";
    }
    
    @Override
    public String getNumericFunctions() {
        if (this.protocol.isOracleMode()) {
            return "ABS,ACOS,ASIN,ATAN,ATAN2,CEILING,COS,EXP,FLOOR,LOG,LOG10,MOD,PI,POWER,ROUND,SIGN,SIN,SQRT,TAN,TRUNCATE";
        }
        return "DIV,ABS,ACOS,ASIN,ATAN,ATAN2,CEIL,CEILING,CONV,COS,COT,CRC32,DEGREES,EXP,FLOOR,GREATEST,LEAST,LN,LOG,LOG10,LOG2,MOD,OCT,PI,POW,POWER,RADIANS,RAND,ROUND,SIGN,SIN,SQRT,TAN,TRUNCATE";
    }
    
    @Override
    public String getStringFunctions() {
        if (this.protocol.isOracleMode()) {
            return "ASCII,CHAR,CHAR_LENGTH,CHARACTER_LENGTH,CONCAT,LCASE,LENGTH,LTRIM,OCTET_LENGTH,REPLACE,RTRIM,SOUNDEX,SUBSTRING,UCASE";
        }
        return "ASCII,BIN,BIT_LENGTH,CAST,CHARACTER_LENGTH,CHAR_LENGTH,CONCAT,CONCAT_WS,CONVERT,ELT,EXPORT_SET,EXTRACTVALUE,FIELD,FIND_IN_SET,FORMAT,FROM_BASE64,HEX,INSTR,LCASE,LEFT,LENGTH,LIKE,LOAD_FILE,LOCATE,LOWER,LPAD,LTRIM,MAKE_SET,MATCH AGAINST,MID,NOT LIKE,NOT REGEXP,OCTET_LENGTH,ORD,POSITION,QUOTE,REPEAT,REPLACE,REVERSE,RIGHT,RPAD,RTRIM,SOUNDEX,SOUNDS LIKE,SPACE,STRCMP,SUBSTR,SUBSTRING,SUBSTRING_INDEX,TO_BASE64,TRIM,UCASE,UNHEX,UPDATEXML,UPPER,WEIGHT_STRING";
    }
    
    @Override
    public String getSystemFunctions() {
        if (this.protocol.isOracleMode()) {
            return "USER";
        }
        return "DATABASE,USER,SYSTEM_USER,SESSION_USER,LAST_INSERT_ID,VERSION";
    }
    
    @Override
    public String getTimeDateFunctions() {
        if (this.protocol.isOracleMode()) {
            return "CURRENT_DATE,CURRENT_TIMESTAMP,CURDATE,EXTRACT,HOUR,MINUTE,MONTH,SECOND,YEAR";
        }
        return "ADDDATE,ADDTIME,CONVERT_TZ,CURDATE,CURRENT_DATE,CURRENT_TIME,CURRENT_TIMESTAMP,CURTIME,DATEDIFF,DATE_ADD,DATE_FORMAT,DATE_SUB,DAY,DAYNAME,DAYOFMONTH,DAYOFWEEK,DAYOFYEAR,EXTRACT,FROM_DAYS,FROM_UNIXTIME,GET_FORMAT,HOUR,LAST_DAY,LOCALTIME,LOCALTIMESTAMP,MAKEDATE,MAKETIME,MICROSECOND,MINUTE,MONTH,MONTHNAME,NOW,PERIOD_ADD,PERIOD_DIFF,QUARTER,SECOND,SEC_TO_TIME,STR_TO_DATE,SUBDATE,SUBTIME,SYSDATE,TIMEDIFF,TIMESTAMPADD,TIMESTAMPDIFF,TIME_FORMAT,TIME_TO_SEC,TO_DAYS,TO_SECONDS,UNIX_TIMESTAMP,UTC_DATE,UTC_TIME,UTC_TIMESTAMP,WEEK,WEEKDAY,WEEKOFYEAR,YEAR,YEARWEEK";
    }
    
    @Override
    public String getSearchStringEscape() {
        if (this.protocol.isOracleMode()) {
            return "/";
        }
        return "\\";
    }
    
    @Override
    public String getExtraNameCharacters() {
        if (this.protocol.isOracleMode()) {
            return "$#";
        }
        return "#@";
    }
    
    @Override
    public boolean supportsAlterTableWithAddColumn() {
        return true;
    }
    
    @Override
    public boolean supportsAlterTableWithDropColumn() {
        return true;
    }
    
    @Override
    public boolean supportsColumnAliasing() {
        return true;
    }
    
    @Override
    public boolean nullPlusNonNullIsNull() {
        return true;
    }
    
    @Override
    public boolean supportsConvert() {
        return !this.connection.getProtocol().isOracleMode() || super.supportsConvert();
    }
    
    @Override
    public boolean supportsConvert(final int fromType, final int toType) {
        switch (fromType) {
            case -7:
            case -6:
            case -5:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 16: {
                switch (toType) {
                    case -7:
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
                    case 16: {
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
                break;
            }
            case 2004: {
                switch (toType) {
                    case -7:
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
                    case 16: {
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
                break;
            }
            case -4:
            case -3:
            case -2:
            case -1:
            case 1:
            case 12:
            case 2005: {
                switch (toType) {
                    case -16:
                    case -15:
                    case -7:
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
                    case 16:
                    case 91:
                    case 92:
                    case 93:
                    case 2004:
                    case 2005:
                    case 2011: {
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
                    case 12:
                    case 91: {
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
                    case 12:
                    case 92: {
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
                    case 92:
                    case 93: {
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
    public boolean supportsTableCorrelationNames() {
        return true;
    }
    
    @Override
    public boolean supportsDifferentTableCorrelationNames() {
        return true;
    }
    
    @Override
    public boolean supportsExpressionsInOrderBy() {
        return true;
    }
    
    @Override
    public boolean supportsOrderByUnrelated() {
        return true;
    }
    
    @Override
    public boolean supportsGroupBy() {
        return true;
    }
    
    @Override
    public boolean supportsGroupByUnrelated() {
        return true;
    }
    
    @Override
    public boolean supportsGroupByBeyondSelect() {
        return true;
    }
    
    @Override
    public boolean supportsLikeEscapeClause() {
        return true;
    }
    
    @Override
    public boolean supportsMultipleResultSets() {
        return true;
    }
    
    @Override
    public boolean supportsMultipleTransactions() {
        return true;
    }
    
    @Override
    public boolean supportsNonNullableColumns() {
        return true;
    }
    
    @Override
    public boolean supportsMinimumSQLGrammar() {
        return true;
    }
    
    @Override
    public boolean supportsCoreSQLGrammar() {
        return true;
    }
    
    @Override
    public boolean supportsExtendedSQLGrammar() {
        return false;
    }
    
    @Override
    public boolean supportsANSI92EntryLevelSQL() {
        return true;
    }
    
    @Override
    public boolean supportsANSI92IntermediateSQL() {
        return !this.connection.getProtocol().isOracleMode() || super.supportsANSI92IntermediateSQL();
    }
    
    @Override
    public boolean supportsANSI92FullSQL() {
        return !this.connection.getProtocol().isOracleMode() || super.supportsANSI92FullSQL();
    }
    
    @Override
    public boolean supportsIntegrityEnhancementFacility() {
        return true;
    }
    
    @Override
    public boolean supportsOuterJoins() {
        return true;
    }
    
    @Override
    public boolean supportsFullOuterJoins() {
        return true;
    }
    
    @Override
    public boolean supportsLimitedOuterJoins() {
        return true;
    }
    
    @Override
    public String getSchemaTerm() {
        return "schema";
    }
    
    @Override
    public String getProcedureTerm() {
        return "procedure";
    }
    
    @Override
    public String getCatalogTerm() {
        return "database";
    }
    
    @Override
    public boolean isCatalogAtStart() {
        return true;
    }
    
    @Override
    public String getCatalogSeparator() {
        return ".";
    }
    
    @Override
    public boolean supportsSchemasInDataManipulation() {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInProcedureCalls() {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInTableDefinitions() {
        return this.connection.getProtocol().isOracleMode() && super.supportsSchemasInTableDefinitions();
    }
    
    @Override
    public boolean supportsSchemasInIndexDefinitions() {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInPrivilegeDefinitions() {
        return true;
    }
    
    @Override
    public boolean supportsCatalogsInDataManipulation() {
        return true;
    }
    
    @Override
    public boolean supportsCatalogsInProcedureCalls() {
        return true;
    }
    
    @Override
    public boolean supportsCatalogsInTableDefinitions() {
        return !this.connection.getProtocol().isOracleMode() || super.supportsCatalogsInTableDefinitions();
    }
    
    @Override
    public boolean supportsCatalogsInIndexDefinitions() {
        return true;
    }
    
    @Override
    public boolean supportsCatalogsInPrivilegeDefinitions() {
        return true;
    }
    
    @Override
    public boolean supportsPositionedDelete() {
        return false;
    }
    
    @Override
    public boolean supportsPositionedUpdate() {
        return false;
    }
    
    @Override
    public boolean supportsSelectForUpdate() {
        return true;
    }
    
    @Override
    public boolean supportsStoredProcedures() {
        return true;
    }
    
    @Override
    public boolean supportsSubqueriesInComparisons() {
        return true;
    }
    
    @Override
    public boolean supportsSubqueriesInExists() {
        return true;
    }
    
    @Override
    public boolean supportsSubqueriesInIns() {
        return true;
    }
    
    @Override
    public boolean supportsSubqueriesInQuantifieds() {
        return true;
    }
    
    @Override
    public boolean supportsCorrelatedSubqueries() {
        return true;
    }
    
    @Override
    public boolean supportsUnion() {
        return true;
    }
    
    @Override
    public boolean supportsUnionAll() {
        return true;
    }
    
    @Override
    public boolean supportsOpenCursorsAcrossCommit() {
        return !this.connection.getProtocol().isOracleMode() || super.supportsOpenCursorsAcrossCommit();
    }
    
    @Override
    public boolean supportsOpenCursorsAcrossRollback() {
        return !this.connection.getProtocol().isOracleMode() || super.supportsOpenCursorsAcrossRollback();
    }
    
    @Override
    public boolean supportsOpenStatementsAcrossCommit() {
        return !this.connection.getProtocol().isOracleMode() || super.supportsOpenStatementsAcrossCommit();
    }
    
    @Override
    public boolean supportsOpenStatementsAcrossRollback() {
        return !this.connection.getProtocol().isOracleMode() || super.supportsOpenStatementsAcrossRollback();
    }
    
    @Override
    public int getMaxBinaryLiteralLength() {
        return 16777208;
    }
    
    @Override
    public int getMaxCharLiteralLength() {
        return 16777208;
    }
    
    @Override
    public int getMaxColumnNameLength() {
        return 64;
    }
    
    @Override
    public int getMaxColumnsInGroupBy() {
        return 64;
    }
    
    @Override
    public int getMaxColumnsInIndex() {
        return 16;
    }
    
    @Override
    public int getMaxColumnsInOrderBy() {
        return 64;
    }
    
    @Override
    public int getMaxColumnsInSelect() {
        return 256;
    }
    
    @Override
    public int getMaxColumnsInTable() {
        return 0;
    }
    
    @Override
    public int getMaxConnections() {
        return 0;
    }
    
    @Override
    public int getMaxCursorNameLength() {
        return 0;
    }
    
    @Override
    public int getMaxIndexLength() {
        return 256;
    }
    
    @Override
    public int getMaxSchemaNameLength() {
        return 32;
    }
    
    @Override
    public int getMaxProcedureNameLength() {
        return 256;
    }
    
    @Override
    public int getMaxCatalogNameLength() {
        return 0;
    }
    
    @Override
    public int getMaxRowSize() {
        return 0;
    }
    
    @Override
    public boolean doesMaxRowSizeIncludeBlobs() {
        return this.connection.getProtocol().isOracleMode() && super.doesMaxRowSizeIncludeBlobs();
    }
    
    @Override
    public int getMaxStatementLength() {
        return 0;
    }
    
    @Override
    public int getMaxStatements() {
        return 0;
    }
    
    @Override
    public int getMaxTableNameLength() {
        return 64;
    }
    
    @Override
    public int getMaxTablesInSelect() {
        return 256;
    }
    
    @Override
    public int getMaxUserNameLength() {
        return 16;
    }
    
    @Override
    public int getDefaultTransactionIsolation() {
        return 4;
    }
    
    @Override
    public boolean supportsTransactions() {
        return true;
    }
    
    @Override
    public boolean supportsTransactionIsolationLevel(final int level) {
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
    public boolean supportsDataDefinitionAndDataManipulationTransactions() {
        return true;
    }
    
    @Override
    public boolean supportsDataManipulationTransactionsOnly() {
        return false;
    }
    
    @Override
    public boolean dataDefinitionCausesTransactionCommit() {
        return true;
    }
    
    @Override
    public boolean dataDefinitionIgnoredInTransactions() {
        return false;
    }
    
    @Override
    public ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getProcedures(catalog, schemaPattern, procedureNamePattern);
        }
        final String sql = "SELECT ROUTINE_SCHEMA PROCEDURE_CAT,NULL PROCEDURE_SCHEM, ROUTINE_NAME PROCEDURE_NAME, NULL RESERVED1, NULL RESERVED2, NULL RESERVED3, CASE ROUTINE_TYPE   WHEN 'FUNCTION' THEN 2  WHEN 'PROCEDURE' THEN 1  ELSE 0 END PROCEDURE_TYPE, ROUTINE_COMMENT REMARKS, SPECIFIC_NAME  FROM INFORMATION_SCHEMA.ROUTINES  WHERE " + this.catalogCond("ROUTINE_SCHEMA", catalog) + this.patternCond("ROUTINE_NAME", procedureNamePattern) + "/* AND ROUTINE_TYPE='PROCEDURE' */";
        return this.executeQuery(sql);
    }
    
    private boolean haveInformationSchemaParameters() {
        return false;
    }
    
    @Override
    public ResultSet getProcedureColumns(final String catalog, final String schemaPattern, final String procedureNamePattern, final String columnNamePattern) throws SQLException {
        if (this.haveInformationSchemaParameters()) {
            final String sql = "SELECT SPECIFIC_SCHEMA PROCEDURE_CAT, NULL PROCEDURE_SCHEM, SPECIFIC_NAME PROCEDURE_NAME, PARAMETER_NAME COLUMN_NAME,  CASE PARAMETER_MODE   WHEN 'IN' THEN 1  WHEN 'OUT' THEN 4  WHEN 'INOUT' THEN 2  ELSE IF(PARAMETER_MODE IS NULL,5,0) END COLUMN_TYPE," + this.dataTypeClause("DTD_IDENTIFIER") + " DATA_TYPE," + "DATA_TYPE TYPE_NAME," + " CASE DATA_TYPE" + "  WHEN 'time' THEN " + (this.datePrecisionColumnExist ? "IF(DATETIME_PRECISION = 0, 10, CAST(11 + DATETIME_PRECISION as signed integer))" : "10") + "  WHEN 'date' THEN 10" + "  WHEN 'datetime' THEN " + (this.datePrecisionColumnExist ? "IF(DATETIME_PRECISION = 0, 19, CAST(20 + DATETIME_PRECISION as signed integer))" : "19") + "  WHEN 'timestamp' THEN " + (this.datePrecisionColumnExist ? "IF(DATETIME_PRECISION = 0, 19, CAST(20 + DATETIME_PRECISION as signed integer))" : "19") + "  ELSE " + "  IF(NUMERIC_PRECISION IS NULL, LEAST(CHARACTER_MAXIMUM_LENGTH," + Integer.MAX_VALUE + "), NUMERIC_PRECISION) " + " END `PRECISION`," + " CASE DATA_TYPE" + "  WHEN 'time' THEN " + (this.datePrecisionColumnExist ? "IF(DATETIME_PRECISION = 0, 10, CAST(11 + DATETIME_PRECISION as signed integer))" : "10") + "  WHEN 'date' THEN 10" + "  WHEN 'datetime' THEN " + (this.datePrecisionColumnExist ? "IF(DATETIME_PRECISION = 0, 19, CAST(20 + DATETIME_PRECISION as signed integer))" : "19") + "  WHEN 'timestamp' THEN " + (this.datePrecisionColumnExist ? "IF(DATETIME_PRECISION = 0, 19, CAST(20 + DATETIME_PRECISION as signed integer))" : "19") + "  ELSE " + "  IF(NUMERIC_PRECISION IS NULL, LEAST(CHARACTER_MAXIMUM_LENGTH," + Integer.MAX_VALUE + "), NUMERIC_PRECISION) " + " END `LENGTH`," + (this.datePrecisionColumnExist ? " CASE DATA_TYPE  WHEN 'time' THEN CAST(DATETIME_PRECISION as signed integer)  WHEN 'datetime' THEN CAST(DATETIME_PRECISION as signed integer)  WHEN 'timestamp' THEN CAST(DATETIME_PRECISION as signed integer)  ELSE NUMERIC_SCALE  END `SCALE`," : " NUMERIC_SCALE `SCALE`,") + "10 RADIX," + 2 + " NULLABLE,NULL REMARKS,NULL COLUMN_DEF,0 SQL_DATA_TYPE,0 SQL_DATETIME_SUB," + "CHARACTER_OCTET_LENGTH CHAR_OCTET_LENGTH ,ORDINAL_POSITION, '' IS_NULLABLE, SPECIFIC_NAME " + " FROM INFORMATION_SCHEMA.PARAMETERS " + " WHERE " + this.catalogCond("SPECIFIC_SCHEMA", catalog) + this.patternCond("SPECIFIC_NAME", procedureNamePattern) + this.patternCond("PARAMETER_NAME", columnNamePattern) + " /* AND ROUTINE_TYPE='PROCEDURE' */ " + " ORDER BY SPECIFIC_SCHEMA, SPECIFIC_NAME, ORDINAL_POSITION";
        }
        else {
            try {
                final String[] columnNames = { "PROCEDURE_CAT", "PROCEDURE_SCHEM", "PROCEDURE_NAME", "COLUMN_NAME", "COLUMN_TYPE", "DATA_TYPE", "TYPE_NAME", "PRECISION", "LENGTH", "SCALE", "RADIX", "NULLABLE", "REMARKS", "COLUMN_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE", "SPECIFIC_NAME" };
                final ColumnType[] columnTypes = { ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.NUMBER, ColumnType.SMALLINT, ColumnType.VARCHAR, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.SMALLINT, ColumnType.SMALLINT, ColumnType.SMALLINT, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.VARCHAR, ColumnType.VARCHAR };
                final ResultSet procsAndOrFuncsRs = this.getProcAndFuncs(catalog, procedureNamePattern, true, false, columnNames, columnTypes, false);
                return procsAndOrFuncsRs;
            }
            catch (SQLException throwables) {
                throw throwables;
            }
        }
        return null;
    }
    
    ResultSet getProcAndFuncs(String catalog, String procedureNamePattern, final boolean isProcedure, final boolean isFunction, final String[] columnNames, final ColumnType[] columnTypes, final boolean forGetFunctionColumns) throws SQLException {
        String db = catalog;
        ResultSet results = null;
        final StringBuilder procSql = new StringBuilder();
        if (this.protocol.isOracleMode()) {
            procSql.append("select object_name as name,object_type as type  from  all_objects where ");
            if (isProcedure && !isFunction) {
                procSql.append("object_type = 'PROCEDURE' AND ");
            }
            else if (!isProcedure && isFunction) {
                procSql.append("object_type = 'FUNCTION' AND ");
            }
            procSql.append("object_name LIKE ? AND owner = ? ORDER BY object_name, object_type");
        }
        else {
            procSql.append("SELECT name, type, comment  FROM mysql.proc WHERE ");
            if (isProcedure && !isFunction) {
                procSql.append("type = 'PROCEDURE' AND ");
            }
            else if (!isProcedure && isFunction) {
                procSql.append("type = 'FUNCTION' AND ");
            }
            procSql.append("name LIKE ? AND db <=> ? ORDER BY name, type");
        }
        try {
            final PreparedStatement proceduresStmt = this.connection.clientPrepareStatement(procSql.toString());
            if (procedureNamePattern == null || procedureNamePattern.length() == 0) {
                procedureNamePattern = "%";
            }
            proceduresStmt.setString(1, procedureNamePattern);
            if (db != null || this.protocol.getDatabase() != null) {
                db = ((db == null) ? this.protocol.getDatabase() : db);
                proceduresStmt.setString(2, db);
                catalog = db;
            }
            else {
                proceduresStmt.setNull(2, 12);
            }
            results = proceduresStmt.executeQuery();
        }
        catch (SQLException e) {
            PreparedStatement proceduresStmt2 = null;
            if (isFunction) {
                proceduresStmt2 = this.connection.clientPrepareStatement("SHOW FUNCTION STATUS LIKE ?");
            }
            else if (isProcedure) {
                proceduresStmt2 = this.connection.clientPrepareStatement("SHOW PROCEDURE STATUS LIKE ?");
            }
            if (procedureNamePattern == null || procedureNamePattern.length() == 0) {
                procedureNamePattern = "%";
            }
            proceduresStmt2.setString(1, procedureNamePattern);
            results = proceduresStmt2.executeQuery();
        }
        return this.getCallStmtParameterTypes(catalog, results, columnNames, columnTypes, forGetFunctionColumns);
    }
    
    private int mapMariaDbTypeToJdbc(final String str) {
        final String upperCase = str.toUpperCase(Locale.ROOT);
        switch (upperCase) {
            case "BIT": {
                return -7;
            }
            case "TINYINT": {
                return -6;
            }
            case "SMALLINT": {
                return 5;
            }
            case "MEDIUMINT": {
                return 4;
            }
            case "INT": {
                return 4;
            }
            case "INTEGER": {
                return 4;
            }
            case "LONG": {
                return 4;
            }
            case "BIGINT": {
                return -5;
            }
            case "INT24": {
                return 4;
            }
            case "REAL": {
                return 8;
            }
            case "FLOAT": {
                return 6;
            }
            case "DECIMAL": {
                return 3;
            }
            case "NUMERIC": {
                return 2;
            }
            case "DOUBLE": {
                return 8;
            }
            case "CHAR": {
                return 1;
            }
            case "VARCHAR": {
                return 12;
            }
            case "DATE": {
                return 91;
            }
            case "TIME": {
                return 92;
            }
            case "YEAR": {
                return 5;
            }
            case "TIMESTAMP": {
                return 93;
            }
            case "DATETIME": {
                return 93;
            }
            case "TINYBLOB": {
                return -2;
            }
            case "BLOB": {
                return -4;
            }
            case "MEDIUMBLOB": {
                return -4;
            }
            case "LONGBLOB": {
                return -4;
            }
            case "TINYTEXT": {
                return 12;
            }
            case "TEXT": {
                return -1;
            }
            case "MEDIUMTEXT": {
                return -1;
            }
            case "LONGTEXT": {
                return -1;
            }
            case "ENUM": {
                return 12;
            }
            case "SET": {
                return 12;
            }
            case "GEOMETRY": {
                return -4;
            }
            case "VARBINARY": {
                return -3;
            }
            default: {
                return 1111;
            }
        }
    }
    
    protected int getColumnType(final boolean isOutParam, final boolean isInParam, final boolean isReturnParam, final boolean forGetFunctionColumns) {
        if (isInParam && isOutParam) {
            return forGetFunctionColumns ? 2 : 2;
        }
        if (isInParam) {
            return (!forGetFunctionColumns || true) ? 1 : 0;
        }
        if (isOutParam) {
            return forGetFunctionColumns ? 3 : 4;
        }
        if (isReturnParam) {
            return forGetFunctionColumns ? 4 : 5;
        }
        return (forGetFunctionColumns && false) ? 1 : 0;
    }
    
    private ResultSet parseParamListProc(final boolean isFunction, final ArrayList<String> paramListArray, final String catalog, final ArrayList<String> nameArray, final String[] columnNames, final ColumnType[] columnTypes, final boolean forGetFunctionColumns) throws SQLException {
        final List<String[]> list = new ArrayList<String[]>();
        int origin = 1;
        for (int i = 0; i < paramListArray.size(); ++i) {
            String paramList = paramListArray.get(i);
            final String procName = nameArray.get(i);
            Matcher matcher1;
            if (this.protocol.isOracleMode()) {
                matcher1 = JDBC4DatabaseMetaData.ORALCLE_PARAMETER_PATTERN.matcher(paramList);
            }
            else {
                matcher1 = JDBC4DatabaseMetaData.PARAMETER_PATTERN.matcher(paramList);
            }
            TypeInfo typeInfo = null;
            boolean isInParam = false;
            boolean isOutParam = false;
            boolean isReturnParam = false;
            final String[] data = new String[20];
            if (paramList != null && paramList.indexOf("(") != 0 && !matcher1.find()) {
                final String[] tokens = { "LANGUAGE", "NOT", "DETERMINISTIC", "CONTAINS", "NO", "READ", "MODIFIES", "SQL", "COMMENT", "BEGIN", "RETURN" };
                int startIndex = 0;
                int endIndex = -1;
                for (int j = 0; j < tokens.length; ++j) {
                    endIndex = paramList.substring(startIndex).toUpperCase(Locale.ROOT).indexOf(tokens[j]);
                    if (endIndex != -1) {
                        startIndex = endIndex + 1;
                    }
                }
                if (endIndex != -1) {
                    paramList = paramList.substring(0, endIndex);
                }
                isInParam = false;
                isOutParam = false;
                isReturnParam = true;
                typeInfo = new TypeInfo(paramList.toUpperCase(Locale.ROOT));
                final String[] tmp = this.getRowData(catalog, "", procName, forGetFunctionColumns, isInParam, isOutParam, isReturnParam, typeInfo, 0);
                list.add(tmp);
            }
            else {
                Matcher matcher2;
                if (this.protocol.isOracleMode()) {
                    matcher2 = JDBC4DatabaseMetaData.ORALCLE_PARAMETER_PATTERN.matcher(paramList);
                }
                else {
                    matcher2 = JDBC4DatabaseMetaData.PARAMETER_PATTERN.matcher(paramList);
                }
                while (matcher2.find()) {
                    String direction = matcher2.group(1);
                    if (direction != null) {
                        direction = direction.trim();
                    }
                    isInParam = false;
                    isOutParam = false;
                    isReturnParam = false;
                    if (direction == null || direction.equalsIgnoreCase("IN")) {
                        isInParam = true;
                    }
                    else if (direction.equalsIgnoreCase("OUT")) {
                        isOutParam = true;
                    }
                    else {
                        if (!direction.equalsIgnoreCase("INOUT")) {
                            throw new SQLException("unknown parameter direction " + direction + "for ");
                        }
                        isInParam = true;
                        isOutParam = true;
                    }
                    String paramName = matcher2.group(2).trim();
                    if (this.protocol.isOracleMode()) {
                        typeInfo = new TypeInfo(matcher2.group(4).trim().toUpperCase(Locale.ROOT));
                    }
                    else if (matcher2.group(5) != null) {
                        typeInfo = new TypeInfo(matcher2.group(4).trim().toUpperCase(Locale.ROOT) + matcher2.group(5).trim().toUpperCase(Locale.ROOT));
                    }
                    else {
                        typeInfo = new TypeInfo(matcher2.group(4).trim().toUpperCase(Locale.ROOT));
                    }
                    if (paramName.startsWith("`") && paramName.endsWith("`")) {
                        paramName = paramName.substring(1, paramName.length() - 1);
                    }
                    else if (paramName.startsWith("") && paramName.endsWith("")) {
                        paramName = paramName.substring(1, paramName.length() - 1);
                    }
                    final String[] tmp2 = this.getRowData(catalog, paramName, procName, forGetFunctionColumns, isInParam, isOutParam, isReturnParam, typeInfo, origin++);
                    list.add(tmp2);
                }
            }
        }
        final String[][] val = new String[list.size()][];
        for (int k = 0; k < list.size(); ++k) {
            val[k] = list.get(k);
        }
        return JDBC4ResultSet.createResultSet(columnNames, columnTypes, val, this.protocol);
    }
    
    String[] getRowData(final String catalog, String paramName, final String procName, final boolean forGetFunctionColumns, final boolean isInParam, final boolean isOutParam, final boolean isReturnParam, final TypeInfo typeInfo, final int origin) throws SQLException {
        final String[] data = new String[20];
        if (paramName.startsWith("`") && paramName.endsWith("`")) {
            paramName = paramName.substring(1, paramName.length() - 1);
        }
        else if (paramName.startsWith("'") && paramName.endsWith("'")) {
            paramName = paramName.substring(1, paramName.length() - 1);
        }
        data[0] = catalog;
        data[1] = null;
        data[2] = procName;
        data[3] = paramName;
        data[4] = String.valueOf(this.getColumnType(isOutParam, isInParam, isReturnParam, forGetFunctionColumns));
        data[5] = Integer.toString(typeInfo.sqlType);
        data[6] = typeInfo.typeName;
        data[8] = (data[7] = String.valueOf(typeInfo.columnSize));
        data[9] = typeInfo.decimalDigits;
        data[10] = String.valueOf(typeInfo.numPrecRadix);
        switch (typeInfo.nullability) {
            case 0: {
                data[11] = String.valueOf(0);
                break;
            }
            case 1: {
                data[11] = String.valueOf(1);
                break;
            }
            case 2: {
                data[11] = String.valueOf(2);
                break;
            }
            default: {
                throw new SQLException("Internal error while parsing callable statement metadata (unknown nullability value fount");
            }
        }
        data[12] = null;
        if (forGetFunctionColumns) {
            data[13] = null;
            data[14] = Integer.toString(origin);
            data[15] = typeInfo.isNullable;
            data[16] = procName;
        }
        else {
            data[14] = (data[13] = null);
            data[16] = (data[15] = null);
            data[17] = String.valueOf(0);
            data[18] = typeInfo.isNullable;
            data[19] = procName;
        }
        final String[] tmp = new String[20];
        System.arraycopy(data, 0, tmp, 0, data.length);
        return tmp;
    }
    
    ResultSet getCallStmtParameterTypes(final String catalog, final ResultSet procsAndOrFuncsRs, final String[] columnNames, final ColumnType[] columnTypes, final boolean forGetFunctionColumns) throws SQLException {
        final ArrayList<String> paramListArray = new ArrayList<String>();
        final ArrayList<String> nameArray = new ArrayList<String>();
        while (procsAndOrFuncsRs.next()) {
            final String name = procsAndOrFuncsRs.getString("name");
            final StringBuilder procNameBuf = new StringBuilder();
            final String type = procsAndOrFuncsRs.getString("type");
            if (this.protocol.isOracleMode()) {
                procNameBuf.append("\"");
                procNameBuf.append(name);
                procNameBuf.append("\"");
            }
            else {
                procNameBuf.append("`");
                procNameBuf.append(catalog);
                procNameBuf.append("`.`");
                procNameBuf.append(name);
                procNameBuf.append("`");
            }
            if ("FUNCTION".equals(type)) {
                final ResultSet paramRetrievalRs = this.connection.createStatement().executeQuery("SHOW CREATE FUNCTION " + procNameBuf.toString());
                paramRetrievalRs.next();
                final String procedureDDl = paramRetrievalRs.getString("Create Function");
                final int startIndex = procedureDDl.indexOf("(");
                final int returnIndex = procedureDDl.indexOf("RETURNS");
                if (returnIndex != -1 && returnIndex > startIndex) {
                    final String returnList = procedureDDl.substring(returnIndex + "RESTURNS".length());
                    paramListArray.add(returnList);
                    nameArray.add(name);
                    final String paramList = procedureDDl.substring(startIndex - 1, returnIndex - 1);
                    paramListArray.add(paramList);
                    nameArray.add(name);
                }
                else {
                    final String paramList2 = procedureDDl.substring(procedureDDl.indexOf("(") - 1);
                    paramListArray.add(paramList2);
                    nameArray.add(name);
                }
            }
            else {
                final ResultSet paramRetrievalRs = this.connection.createStatement().executeQuery("SHOW CREATE PROCEDURE " + procNameBuf.toString());
                paramRetrievalRs.next();
                final String procedureDDl = paramRetrievalRs.getString("Create Procedure");
                final int startIndex = procedureDDl.indexOf("(");
                final int endIdex = procedureDDl.indexOf("BEGIN");
                String paramList2 = null;
                if (endIdex != -1 && endIdex > startIndex) {
                    paramList2 = procedureDDl.substring(startIndex - 1, endIdex - 1);
                }
                else {
                    paramList2 = procedureDDl.substring(procedureDDl.indexOf("(") - 1);
                }
                paramListArray.add(paramList2);
                nameArray.add(name);
            }
        }
        return this.parseParamListProc(false, paramListArray, catalog, nameArray, columnNames, columnTypes, forGetFunctionColumns);
    }
    
    @Override
    public ResultSet getFunctionColumns(final String catalog, final String schemaPattern, final String functionNamePattern, final String columnNamePattern) throws SQLException {
        if (this.haveInformationSchemaParameters()) {
            final String sql = "SELECT SPECIFIC_SCHEMA `FUNCTION_CAT`, NULL `FUNCTION_SCHEM`, SPECIFIC_NAME FUNCTION_NAME, PARAMETER_NAME COLUMN_NAME,  CASE PARAMETER_MODE   WHEN 'IN' THEN 1  WHEN 'OUT' THEN 3  WHEN 'INOUT' THEN 2  ELSE 4 END COLUMN_TYPE," + this.dataTypeClause("DTD_IDENTIFIER") + " DATA_TYPE," + "DATA_TYPE TYPE_NAME,NUMERIC_PRECISION `PRECISION`,CHARACTER_MAXIMUM_LENGTH LENGTH,NUMERIC_SCALE SCALE,10 RADIX," + 2 + " NULLABLE,NULL REMARKS," + "CHARACTER_OCTET_LENGTH CHAR_OCTET_LENGTH ,ORDINAL_POSITION, '' IS_NULLABLE, SPECIFIC_NAME " + " FROM INFORMATION_SCHEMA.PARAMETERS " + " WHERE " + this.catalogCond("SPECIFIC_SCHEMA", catalog) + this.patternCond("SPECIFIC_NAME", functionNamePattern) + this.patternCond("PARAMETER_NAME", columnNamePattern) + " AND ROUTINE_TYPE='FUNCTION'" + " ORDER BY FUNCTION_CAT, SPECIFIC_NAME, ORDINAL_POSITION";
        }
        else {
            final String sql = "SELECT '' FUNCTION_CAT, NULL FUNCTION_SCHEM, '' FUNCTION_NAME, '' COLUMN_NAME, 0  COLUMN_TYPE, 0 DATA_TYPE, '' TYPE_NAME,0 `PRECISION`,0 LENGTH, 0 SCALE,0 RADIX, 0 NULLABLE,NULL REMARKS, 0 CHAR_OCTET_LENGTH , 0 ORDINAL_POSITION,  '' IS_NULLABLE, '' SPECIFIC_NAME  FROM DUAL WHERE 1=0 ";
        }
        final String[] columnNames = { "FUNCTION_CAT", "FUNCTION_SCHEM", "FUNCTION_NAME", "COLUMN_NAME", "COLUMN_TYPE", "DATA_TYPE", "TYPE_NAME", "PRECISION", "LENGTH", "SCALE", "RADIX", "NULLABLE", "REMARKS", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE", "SPECIFIC_NAME" };
        final ColumnType[] columnTypes = { ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.NUMBER, ColumnType.SMALLINT, ColumnType.VARCHAR, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.SMALLINT, ColumnType.SMALLINT, ColumnType.SMALLINT, ColumnType.VARCHAR, ColumnType.NUMBER, ColumnType.NUMBER, ColumnType.VARCHAR, ColumnType.VARCHAR };
        final ResultSet procsAndOrFuncsRs = this.getProcAndFuncs(catalog, functionNamePattern, false, true, columnNames, columnTypes, true);
        return procsAndOrFuncsRs;
    }
    
    @Override
    public ResultSet getSchemas() throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getSchemas();
        }
        return this.executeQuery("SELECT '' TABLE_SCHEM, '' TABLE_catalog  FROM DUAL WHERE 1=0");
    }
    
    @Override
    public ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getSchemas(catalog, schemaPattern);
        }
        return this.executeQuery("SELECT  ' ' table_schem, ' ' table_catalog FROM DUAL WHERE 1=0");
    }
    
    @Override
    public ResultSet getCatalogs() throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getCatalogs();
        }
        return this.executeQuery("SELECT SCHEMA_NAME  TABLE_CAT FROM INFORMATION_SCHEMA.SCHEMATA ORDER BY 1");
    }
    
    @Override
    public ResultSet getTableTypes() throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getTableTypes();
        }
        return this.executeQuery("SELECT 'TABLE' TABLE_TYPE UNION SELECT 'SYSTEM VIEW' TABLE_TYPE UNION SELECT 'VIEW' TABLE_TYPE");
    }
    
    @Override
    public ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
        if (table == null) {
            throw new SQLException("'table' parameter must not be null");
        }
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getColumnPrivileges(catalog, schema, table, columnNamePattern);
        }
        final String sql = "SELECT TABLE_SCHEMA TABLE_CAT, NULL TABLE_SCHEM, TABLE_NAME, COLUMN_NAME, NULL AS GRANTOR, GRANTEE, PRIVILEGE_TYPE AS PRIVILEGE, IS_GRANTABLE FROM  INFORMATION_SCHEMA.COLUMN_PRIVILEGES WHERE " + this.catalogCond("TABLE_SCHEMA", catalog) + " AND " + " TABLE_NAME = " + this.escapeQuote(table) + this.patternCond("COLUMN_NAME", columnNamePattern) + " ORDER BY COLUMN_NAME, PRIVILEGE_TYPE";
        return this.executeQuery(sql);
    }
    
    @Override
    public ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getTablePrivileges(catalog, schemaPattern, tableNamePattern);
        }
        final String sql = "SELECT TABLE_SCHEMA TABLE_CAT,NULL  TABLE_SCHEM, TABLE_NAME, NULL GRANTOR,GRANTEE, PRIVILEGE_TYPE  PRIVILEGE, IS_GRANTABLE  FROM INFORMATION_SCHEMA.TABLE_PRIVILEGES  WHERE " + this.catalogCond("TABLE_SCHEMA", catalog) + this.patternCond("TABLE_NAME", tableNamePattern) + "ORDER BY TABLE_SCHEMA, TABLE_NAME,  PRIVILEGE_TYPE ";
        return this.executeQuery(sql);
    }
    
    @Override
    public ResultSet getVersionColumns(final String catalog, final String schema, final String table) throws SQLException {
        final String sql = "SELECT 0 SCOPE, ' ' COLUMN_NAME, 0 DATA_TYPE, ' ' TYPE_NAME, 0 COLUMN_SIZE, 0 BUFFER_LENGTH, 0 DECIMAL_DIGITS, 0 PSEUDO_COLUMN  FROM DUAL WHERE 1 = 0";
        return this.executeQuery(sql);
    }
    
    @Override
    public ResultSet getCrossReference(final String parentCatalog, final String parentSchema, final String parentTable, final String foreignCatalog, final String foreignSchema, final String foreignTable) throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return this.keysQuery(parentSchema, parentTable, foreignSchema, foreignTable, "ORDER BY fktable_schem, fktable_name, key_seq");
        }
        final String sql = "SELECT KCU.REFERENCED_TABLE_SCHEMA PKTABLE_CAT, NULL PKTABLE_SCHEM,  KCU.REFERENCED_TABLE_NAME PKTABLE_NAME, KCU.REFERENCED_COLUMN_NAME PKCOLUMN_NAME, KCU.TABLE_SCHEMA FKTABLE_CAT, NULL FKTABLE_SCHEM,  KCU.TABLE_NAME FKTABLE_NAME, KCU.COLUMN_NAME FKCOLUMN_NAME, KCU.POSITION_IN_UNIQUE_CONSTRAINT KEY_SEQ, CASE update_rule    WHEN 'RESTRICT' THEN 1   WHEN 'NO ACTION' THEN 3   WHEN 'CASCADE' THEN 0   WHEN 'SET NULL' THEN 2   WHEN 'SET DEFAULT' THEN 4 END UPDATE_RULE, CASE DELETE_RULE  WHEN 'RESTRICT' THEN 1  WHEN 'NO ACTION' THEN 3  WHEN 'CASCADE' THEN 0  WHEN 'SET NULL' THEN 2  WHEN 'SET DEFAULT' THEN 4 END DELETE_RULE, RC.CONSTRAINT_NAME FK_NAME, NULL PK_NAME,7 DEFERRABILITY FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE KCU INNER JOIN INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS RC ON KCU.CONSTRAINT_SCHEMA = RC.CONSTRAINT_SCHEMA AND KCU.CONSTRAINT_NAME = RC.CONSTRAINT_NAME WHERE " + this.catalogCond("KCU.REFERENCED_TABLE_SCHEMA", parentCatalog) + " AND " + this.catalogCond("KCU.TABLE_SCHEMA", foreignCatalog) + " AND " + " KCU.REFERENCED_TABLE_NAME = " + this.escapeQuote(parentTable) + " AND " + " KCU.TABLE_NAME = " + this.escapeQuote(foreignTable) + " ORDER BY FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, KEY_SEQ";
        try {
            final ArrayList<String> tableNameList = this.getALlTableNames(parentCatalog);
            final ResultSet allTableRS = this.getAllTablesResultSet(tableNameList, parentCatalog);
            final ArrayList<String[]> list = new ArrayList<String[]>();
            while (allTableRS.next()) {
                final String[] data = new String[14];
                final String tableType = allTableRS.getString("Type");
                if (tableType != null && (tableType.toUpperCase(Locale.ROOT).equals("INNODB") || tableType.toUpperCase(Locale.ROOT).equals("SUPPORTS_FK"))) {
                    final String tableComment = allTableRS.getString("Comment").trim();
                    final String fkTableName = allTableRS.getString("Name");
                    if (tableComment == null) {
                        continue;
                    }
                    final StringTokenizer stringTokenizer = new StringTokenizer(tableComment, ";", false);
                    if (stringTokenizer.hasMoreTokens()) {
                        stringTokenizer.nextToken();
                    }
                    while (stringTokenizer.hasMoreTokens()) {
                        String keys = stringTokenizer.nextToken();
                        final int fkParamOpenIndex = keys.indexOf("(");
                        if (fkParamOpenIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find start of local columns list.");
                        }
                        final String constraintName = keys.substring(1, fkParamOpenIndex - 1);
                        keys = keys.substring(fkParamOpenIndex + 1);
                        final int fkParamCloseIndex = keys.indexOf(")");
                        if (fkParamCloseIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find start of local columns list.");
                        }
                        final String fkParamNames = keys.substring(1, fkParamCloseIndex - 1);
                        final int refIndex = keys.indexOf("REFER");
                        if (refIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find start of referenced tables list");
                        }
                        final int refParamOpenIndex = keys.indexOf("(", refIndex);
                        if (refParamOpenIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find start of referenced columns list");
                        }
                        final String refCatalogAndTable = keys.substring(refIndex + "REFER".length() + 1, refParamOpenIndex);
                        final int slashIndex = refCatalogAndTable.indexOf("/");
                        if (slashIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find name of referenced catalog.");
                        }
                        String refCatalog = refCatalogAndTable.substring(0, slashIndex);
                        refCatalog = this.removeQuoted(refCatalog);
                        String refTableName = refCatalogAndTable.substring(slashIndex + 1);
                        refTableName = this.removeQuoted(refTableName);
                        if (fkTableName.compareTo(foreignTable) != 0) {
                            continue;
                        }
                        final int refParamCloseIndex = keys.indexOf(")", refParamOpenIndex);
                        if (refParamCloseIndex == -1) {
                            throw new SQLException("Error parsing foreign keys definition, couldn't find end of referenced columns list.");
                        }
                        final String refParamNames = keys.substring(refParamOpenIndex + 1, refParamCloseIndex);
                        final String[] refParamList = refParamNames.split(",");
                        final String[] fkParamList = fkParamNames.split(",");
                        int fkCur = 0;
                        int refCur = 0;
                        int keySeq = 0;
                        while (fkCur < fkParamList.length) {
                            final String lColumnName = this.removeQuoted(fkParamList[fkCur++]);
                            String rColumnName = null;
                            if (refCur < refParamList.length) {
                                rColumnName = this.removeQuoted(refParamList[refCur++]);
                            }
                            if (refTableName.compareTo(parentTable) != 0) {
                                continue;
                            }
                            int updateRuleAction = 3;
                            int deleteRuleAction = 3;
                            final int lastIndex = keys.lastIndexOf(")");
                            if (lastIndex != keys.length() - 1) {
                                final String options = keys.substring(lastIndex + 1);
                                final String optionsForUpdate = options.substring(options.indexOf("ON UPDATE"));
                                if (optionsForUpdate.startsWith("ON UPDATE CASCADE")) {
                                    updateRuleAction = 0;
                                }
                                else if (optionsForUpdate.startsWith("ON UPDATE SET NUL")) {
                                    updateRuleAction = 2;
                                }
                                else if (optionsForUpdate.startsWith("ON UPDATE RESTRICT")) {
                                    updateRuleAction = 1;
                                }
                                else if (optionsForUpdate.startsWith("ON UPDATE NO ACTION")) {
                                    updateRuleAction = 3;
                                }
                                final String optionsForDelete = options.substring(options.indexOf("ON DELETE"));
                                if (optionsForDelete.startsWith("ON DELETE CASCADE")) {
                                    deleteRuleAction = 0;
                                }
                                else if (optionsForDelete.startsWith("ON DELETE SET NUL")) {
                                    deleteRuleAction = 2;
                                }
                                else if (optionsForDelete.startsWith("ON DELETE RESTRICT")) {
                                    deleteRuleAction = 1;
                                }
                                else if (optionsForDelete.startsWith("ON DELETE NO ACTIO")) {
                                    deleteRuleAction = 3;
                                }
                            }
                            data[0] = parentCatalog;
                            data[1] = parentSchema;
                            data[2] = refTableName;
                            data[3] = rColumnName;
                            data[4] = foreignCatalog;
                            data[5] = foreignSchema;
                            data[6] = fkTableName;
                            data[7] = lColumnName;
                            data[8] = Integer.toString(keySeq++);
                            data[9] = Integer.toString(updateRuleAction);
                            data[10] = Integer.toString(deleteRuleAction);
                            data[12] = (data[11] = null);
                            data[13] = Integer.toString(7);
                            list.add(data);
                        }
                    }
                }
            }
            final String[][] val = new String[list.size()][];
            for (int j = 0; j < list.size(); ++j) {
                val[j] = list.get(j);
            }
            return JDBC4ResultSet.createResultSet(this.exportKeysColumnNames, this.exportKeysColumnTypes, val, this.protocol);
        }
        catch (SQLException e) {
            throw e;
        }
    }
    
    @Override
    public ResultSet getTypeInfo() {
        final String[] columnNames = { "TYPE_NAME", "DATA_TYPE", "PRECISION", "LITERAL_PREFIX", "LITERAL_SUFFIX", "CREATE_PARAMS", "NULLABLE", "CASE_SENSITIVE", "SEARCHABLE", "UNSIGNED_ATTRIBUTE", "FIXED_PREC_SCALE", "AUTO_INCREMENT", "LOCAL_TYPE_NAME", "MINIMUM_SCALE", "MAXIMUM_SCALE", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "NUM_PREC_RADIX" };
        final ColumnType[] columnTypes = { ColumnType.VARCHAR, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.VARCHAR, ColumnType.INTEGER, ColumnType.BIT, ColumnType.SMALLINT, ColumnType.BIT, ColumnType.BIT, ColumnType.BIT, ColumnType.VARCHAR, ColumnType.SMALLINT, ColumnType.SMALLINT, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER };
        final String[][] data = { { "BIT", "-7", "1", "", "", "", "1", "1", "3", "0", "0", "0", "BIT", "0", "0", "0", "0", "10" }, { "BOOL", "-7", "1", "", "", "", "1", "1", "3", "0", "0", "0", "BOOL", "0", "0", "0", "0", "10" }, { "TINYINT", "-6", "3", "", "", "[(M)] [UNSIGNED] [ZEROFILL]", "1", "0", "3", "1", "0", "1", "TINYINT", "0", "0", "0", "0", "10" }, { "TINYINT UNSIGNED", "-6", "3", "", "", "[(M)] [UNSIGNED] [ZEROFILL]", "1", "0", "3", "1", "0", "1", "TINYINT UNSIGNED", "0", "0", "0", "0", "10" }, { "BIGINT", "-5", "19", "", "", "[(M)] [UNSIGNED] [ZEROFILL]", "1", "0", "3", "1", "0", "1", "BIGINT", "0", "0", "0", "0", "10" }, { "BIGINT UNSIGNED", "-5", "20", "", "", "[(M)] [ZEROFILL]", "1", "0", "3", "1", "0", "1", "BIGINT UNSIGNED", "0", "0", "0", "0", "10" }, { "LONG VARBINARY", "-4", "16777215", "'", "'", "", "1", "1", "3", "0", "0", "0", "LONG VARBINARY", "0", "0", "0", "0", "10" }, { "MEDIUMBLOB", "-4", "16777215", "'", "'", "", "1", "1", "3", "0", "0", "0", "MEDIUMBLOB", "0", "0", "0", "0", "10" }, { "LONGBLOB", "-4", "2147483647", "'", "'", "", "1", "1", "3", "0", "0", "0", "LONGBLOB", "0", "0", "0", "0", "10" }, { "BLOB", "-4", "65535", "'", "'", "", "1", "1", "3", "0", "0", "0", "BLOB", "0", "0", "0", "0", "10" }, { "TINYBLOB", "-4", "255", "'", "'", "", "1", "1", "3", "0", "0", "0", "TINYBLOB", "0", "0", "0", "0", "10" }, { "VARBINARY", "-3", "255", "'", "'", "(M)", "1", "1", "3", "0", "0", "0", "VARBINARY", "0", "0", "0", "0", "10" }, { "BINARY", "-2", "255", "'", "'", "(M)", "1", "1", "3", "0", "0", "0", "BINARY", "0", "0", "0", "0", "10" }, { "LONG VARCHAR", "-1", "16777215", "'", "'", "", "1", "0", "3", "0", "0", "0", "LONG VARCHAR", "0", "0", "0", "0", "10" }, { "MEDIUMTEXT", "-1", "16777215", "'", "'", "", "1", "0", "3", "0", "0", "0", "MEDIUMTEXT", "0", "0", "0", "0", "10" }, { "LONGTEXT", "-1", "2147483647", "'", "'", "", "1", "0", "3", "0", "0", "0", "LONGTEXT", "0", "0", "0", "0", "10" }, { "TEXT", "-1", "65535", "'", "'", "", "1", "0", "3", "0", "0", "0", "TEXT", "0", "0", "0", "0", "10" }, { "TINYTEXT", "-1", "255", "'", "'", "", "1", "0", "3", "0", "0", "0", "TINYTEXT", "0", "0", "0", "0", "10" }, { "CHAR", "1", "255", "'", "'", "(M)", "1", "0", "3", "0", "0", "0", "CHAR", "0", "0", "0", "0", "10" }, { "NUMERIC", "2", "65", "", "", "[(M,D])] [ZEROFILL]", "1", "0", "3", "0", "0", "1", "NUMERIC", "-308", "308", "0", "0", "10" }, { "DECIMAL", "3", "65", "", "", "[(M,D])] [ZEROFILL]", "1", "0", "3", "0", "0", "1", "DECIMAL", "-308", "308", "0", "0", "10" }, { "INTEGER", "4", "10", "", "", "[(M)] [UNSIGNED] [ZEROFILL]", "1", "0", "3", "1", "0", "1", "INTEGER", "0", "0", "0", "0", "10" }, { "INTEGER UNSIGNED", "4", "10", "", "", "[(M)] [ZEROFILL]", "1", "0", "3", "1", "0", "1", "INTEGER UNSIGNED", "0", "0", "0", "0", "10" }, { "INT", "4", "10", "", "", "[(M)] [UNSIGNED] [ZEROFILL]", "1", "0", "3", "1", "0", "1", "INT", "0", "0", "0", "0", "10" }, { "INT UNSIGNED", "4", "10", "", "", "[(M)] [ZEROFILL]", "1", "0", "3", "1", "0", "1", "INT UNSIGNED", "0", "0", "0", "0", "10" }, { "MEDIUMINT", "4", "7", "", "", "[(M)] [UNSIGNED] [ZEROFILL]", "1", "0", "3", "1", "0", "1", "MEDIUMINT", "0", "0", "0", "0", "10" }, { "MEDIUMINT UNSIGNED", "4", "8", "", "", "[(M)] [ZEROFILL]", "1", "0", "3", "1", "0", "1", "MEDIUMINT UNSIGNED", "0", "0", "0", "0", "10" }, { "SMALLINT", "5", "5", "", "", "[(M)] [UNSIGNED] [ZEROFILL]", "1", "0", "3", "1", "0", "1", "SMALLINT", "0", "0", "0", "0", "10" }, { "SMALLINT UNSIGNED", "5", "5", "", "", "[(M)] [ZEROFILL]", "1", "0", "3", "1", "0", "1", "SMALLINT UNSIGNED", "0", "0", "0", "0", "10" }, { "FLOAT", "7", "10", "", "", "[(M|D)] [ZEROFILL]", "1", "0", "3", "0", "0", "1", "FLOAT", "-38", "38", "0", "0", "10" }, { "DOUBLE", "8", "17", "", "", "[(M|D)] [ZEROFILL]", "1", "0", "3", "0", "0", "1", "DOUBLE", "-308", "308", "0", "0", "10" }, { "DOUBLE PRECISION", "8", "17", "", "", "[(M,D)] [ZEROFILL]", "1", "0", "3", "0", "0", "1", "DOUBLE PRECISION", "-308", "308", "0", "0", "10" }, { "REAL", "8", "17", "", "", "[(M,D)] [ZEROFILL]", "1", "0", "3", "0", "0", "1", "REAL", "-308", "308", "0", "0", "10" }, { "VARCHAR", "12", "255", "'", "'", "(M)", "1", "0", "3", "0", "0", "0", "VARCHAR", "0", "0", "0", "0", "10" }, { "ENUM", "12", "65535", "'", "'", "", "1", "0", "3", "0", "0", "0", "ENUM", "0", "0", "0", "0", "10" }, { "SET", "12", "64", "'", "'", "", "1", "0", "3", "0", "0", "0", "SET", "0", "0", "0", "0", "10" }, { "DATE", "91", "10", "'", "'", "", "1", "0", "3", "0", "0", "0", "DATE", "0", "0", "0", "0", "10" }, { "TIME", "92", "18", "'", "'", "[(M)]", "1", "0", "3", "0", "0", "0", "TIME", "0", "0", "0", "0", "10" }, { "DATETIME", "93", "27", "'", "'", "[(M)]", "1", "0", "3", "0", "0", "0", "DATETIME", "0", "0", "0", "0", "10" }, { "TIMESTAMP", "93", "27", "'", "'", "[(M)]", "1", "0", "3", "0", "0", "0", "TIMESTAMP", "0", "0", "0", "0", "10" } };
        return JDBC4ResultSet.createResultSet(columnNames, columnTypes, data, this.connection.getProtocol());
    }
    
    @Override
    public ResultSet getIndexInfo(final String catalog, final String schema, final String table, final boolean unique, final boolean approximate) throws SQLException {
        if (table == null || table.length() == 0) {
            throw new SQLException();
        }
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getIndexInfo(catalog, schema, table, unique, approximate);
        }
        final String sql = "SELECT TABLE_SCHEMA TABLE_CAT, NULL TABLE_SCHEM, TABLE_NAME, NON_UNIQUE,  TABLE_SCHEMA INDEX_QUALIFIER, INDEX_NAME, 3 TYPE, SEQ_IN_INDEX ORDINAL_POSITION, COLUMN_NAME, COLLATION ASC_OR_DESC, CARDINALITY, NULL PAGES, NULL FILTER_CONDITION FROM INFORMATION_SCHEMA.STATISTICS WHERE TABLE_NAME = " + this.escapeQuote(table) + " AND " + this.catalogCond("TABLE_SCHEMA", catalog) + (unique ? " AND NON_UNIQUE = 0" : "") + " ORDER BY NON_UNIQUE, TYPE, INDEX_NAME, ORDINAL_POSITION";
        return this.executeQuery(sql);
    }
    
    @Override
    public boolean supportsResultSetType(final int type) {
        return type == 1004 || type == 1003;
    }
    
    @Override
    public boolean supportsResultSetConcurrency(final int type, final int concurrency) {
        return type == 1004 || type == 1003;
    }
    
    @Override
    public boolean ownUpdatesAreVisible(final int type) {
        return this.supportsResultSetType(type);
    }
    
    @Override
    public boolean ownDeletesAreVisible(final int type) {
        return this.supportsResultSetType(type);
    }
    
    @Override
    public boolean ownInsertsAreVisible(final int type) {
        return this.supportsResultSetType(type);
    }
    
    @Override
    public boolean othersUpdatesAreVisible(final int type) {
        return false;
    }
    
    @Override
    public boolean othersDeletesAreVisible(final int type) {
        return false;
    }
    
    @Override
    public boolean othersInsertsAreVisible(final int type) {
        return false;
    }
    
    @Override
    public boolean updatesAreDetected(final int type) {
        return false;
    }
    
    @Override
    public boolean deletesAreDetected(final int type) {
        return false;
    }
    
    @Override
    public boolean insertsAreDetected(final int type) {
        return false;
    }
    
    @Override
    public boolean supportsBatchUpdates() {
        return true;
    }
    
    @Override
    public ResultSet getUDTs(final String catalog, final String schemaPattern, final String typeNamePattern, final int[] types) throws SQLException {
        final String sql = "SELECT ' ' TYPE_CAT, NULL TYPE_SCHEM, ' ' TYPE_NAME, ' ' CLASS_NAME, 0 DATA_TYPE, ' ' REMARKS, 0 BASE_TYPE FROM DUAL WHERE 1=0";
        return this.executeQuery(sql);
    }
    
    @Override
    public Connection getConnection() {
        return this.connection;
    }
    
    @Override
    public boolean supportsSavepoints() {
        return true;
    }
    
    @Override
    public boolean supportsNamedParameters() {
        return false;
    }
    
    @Override
    public boolean supportsMultipleOpenResults() {
        return false;
    }
    
    @Override
    public boolean supportsGetGeneratedKeys() {
        return true;
    }
    
    @Override
    public ResultSet getSuperTypes(final String catalog, final String schemaPattern, final String typeNamePattern) throws SQLException {
        final String sql = "SELECT  ' ' TYPE_CAT, NULL TYPE_SCHEM, ' ' TYPE_NAME, ' ' SUPERTYPE_CAT, ' ' SUPERTYPE_SCHEM, ' '  SUPERTYPE_NAME FROM DUAL WHERE 1=0";
        return this.executeQuery(sql);
    }
    
    @Override
    public ResultSet getSuperTables(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        final String sql = "SELECT  ' ' TABLE_CAT, ' ' TABLE_SCHEM, ' ' TABLE_NAME, ' ' SUPERTABLE_NAME FROM DUAL WHERE 1=0";
        return this.executeQuery(sql);
    }
    
    @Override
    public ResultSet getAttributes(final String catalog, final String schemaPattern, final String typeNamePattern, final String attributeNamePattern) throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            throw new SQLFeatureNotSupportedException("Oracle model Unsupported features");
        }
        final String sql = "SELECT ' ' TYPE_CAT, ' ' TYPE_SCHEM, ' ' TYPE_NAME, ' ' ATTR_NAME, 0 DATA_TYPE, ' ' ATTR_TYPE_NAME, 0 ATTR_SIZE, 0 DECIMAL_DIGITS, 0 NUM_PREC_RADIX, 0 NULLABLE, ' ' REMARKS, ' ' ATTR_DEF,  0 SQL_DATA_TYPE, 0 SQL_DATETIME_SUB, 0 CHAR_OCTET_LENGTH, 0 ORDINAL_POSITION, ' ' IS_NULLABLE, ' ' SCOPE_CATALOG, ' ' SCOPE_SCHEMA, ' ' SCOPE_TABLE, 0 SOURCE_DATA_TYPE FROM DUAL  WHERE 1=0";
        return this.executeQuery(sql);
    }
    
    @Override
    public boolean supportsResultSetHoldability(final int holdability) {
        return holdability == 1;
    }
    
    @Override
    public int getResultSetHoldability() {
        return 1;
    }
    
    @Override
    public int getDatabaseMajorVersion() {
        return this.connection.getProtocol().getMajorServerVersion();
    }
    
    @Override
    public int getDatabaseMinorVersion() {
        return this.connection.getProtocol().getMinorServerVersion();
    }
    
    @Override
    public int getJDBCMajorVersion() {
        return 4;
    }
    
    @Override
    public int getJDBCMinorVersion() {
        return 2;
    }
    
    @Override
    public int getSQLStateType() {
        return 2;
    }
    
    @Override
    public boolean locatorsUpdateCopy() {
        return this.connection.getProtocol().isOracleMode() && super.locatorsUpdateCopy();
    }
    
    @Override
    public boolean supportsStatementPooling() {
        return false;
    }
    
    @Override
    public RowIdLifetime getRowIdLifetime() {
        return RowIdLifetime.ROWID_UNSUPPORTED;
    }
    
    @Override
    public boolean supportsStoredFunctionsUsingCallSyntax() {
        return true;
    }
    
    @Override
    public boolean autoCommitFailureClosesAllResultSets() {
        return false;
    }
    
    @Override
    public ResultSet getClientInfoProperties() {
        final ColumnDefinition[] columns = new ColumnDefinition[4];
        final boolean isOracleMode = this.protocol.isOracleMode();
        columns[0] = ColumnDefinition.create("NAME", ColumnType.STRING, isOracleMode, this.protocol.getOptions().characterEncoding);
        columns[1] = ColumnDefinition.create("MAX_LEN", ColumnType.INTEGER, isOracleMode, this.protocol.getOptions().characterEncoding);
        columns[2] = ColumnDefinition.create("DEFAULT_VALUE", ColumnType.STRING, isOracleMode, this.protocol.getOptions().characterEncoding);
        columns[3] = ColumnDefinition.create("DESCRIPTION", ColumnType.STRING, isOracleMode, this.protocol.getOptions().characterEncoding);
        final byte[] sixteenMb = { 49, 54, 55, 55, 55, 50, 49, 53 };
        final byte[] empty = new byte[0];
        final ColumnType[] types = { ColumnType.STRING, ColumnType.INTEGER, ColumnType.STRING, ColumnType.STRING };
        final List<byte[]> rows = new ArrayList<byte[]>(3);
        rows.add(StandardPacketInputStream.create(new byte[][] { "ApplicationName".getBytes(), sixteenMb, empty, "The name of the application currently utilizing the connection".getBytes() }, types));
        rows.add(StandardPacketInputStream.create(new byte[][] { "ClientUser".getBytes(), sixteenMb, empty, "The name of the user that the application using the connection is performing work for. This may not be the same as the user name that was used in establishing the connection.".getBytes() }, types));
        rows.add(StandardPacketInputStream.create(new byte[][] { "ClientHostname".getBytes(), sixteenMb, empty, "The hostname of the computer the application using the connection is running on".getBytes() }, types));
        return new SelectResultSet(columns, rows, this.connection.getProtocol(), 1004);
    }
    
    @Override
    public ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException {
        if (this.connection.getProtocol().isOracleMode()) {
            return super.getFunctions(catalog, schemaPattern, functionNamePattern);
        }
        final String sql = "SELECT ROUTINE_SCHEMA FUNCTION_CAT,NULL FUNCTION_SCHEM, ROUTINE_NAME FUNCTION_NAME, ROUTINE_COMMENT REMARKS,1 FUNCTION_TYPE, SPECIFIC_NAME  FROM INFORMATION_SCHEMA.ROUTINES  WHERE " + this.catalogCond("ROUTINE_SCHEMA", catalog) + this.patternCond("ROUTINE_NAME", functionNamePattern) + " AND ROUTINE_TYPE='FUNCTION'";
        return this.executeQuery(sql);
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return false;
    }
    
    @Override
    public long getMaxLogicalLobSize() {
        return 4294967295L;
    }
    
    @Override
    public boolean supportsRefCursors() {
        return false;
    }
    
    static {
        RETURN_PATTERN = Pattern.compile("\\s*(UNSIGNED\\s+)?(\\w+)\\s*(\\([\\d,]+\\))?\\s*(CHARSET\\s+)?(\\w+)?\\s*", 2);
        PARAMETER_PATTERN = Pattern.compile("\\s*(IN\\s+|OUT\\s+|INOUT\\s+)?(\\`[\\w\\d]+\\`)\\s+(UNSIGNED\\s+)?(\\w+)\\s*(\\([\\d,]+\\))?\\s*", 2);
        ORALCLE_PARAMETER_PATTERN = Pattern.compile("\\s*(IN\\s+|OUT\\s+|INOUT\\s+)?(\"[\\w\\d]+\")\\s+(UNSIGNED\\s+)?(\\w+)\\s*(\\([\\d,]+\\))?\\s*", 2);
    }
    
    protected class TableMetaData implements Comparable<TableMetaData>
    {
        String type;
        String catalog;
        String schema;
        String name;
        
        TableMetaData(final String type, final String catalog, final String schema, final String name) {
            this.type = ((type == null) ? "" : type);
            this.catalog = ((catalog == null) ? "" : catalog);
            this.schema = ((schema == null) ? "" : schema);
            this.name = ((name == null) ? "" : name);
        }
        
        @Override
        public int compareTo(final TableMetaData tablesKey) {
            int ret;
            if ((ret = this.type.compareTo(tablesKey.type)) != 0) {
                return ret;
            }
            if ((ret = this.catalog.compareTo(tablesKey.catalog)) != 0) {
                return ret;
            }
            if ((ret = this.schema.compareTo(tablesKey.schema)) != 0) {
                return ret;
            }
            return this.name.compareTo(tablesKey.name);
        }
        
        @Override
        public boolean equals(final Object obj) {
            return obj instanceof TableMetaData && obj != null && (obj == this || this.compareTo((TableMetaData)obj) == 0);
        }
    }
    
    class TypeInfo
    {
        int bufferLength;
        int columnSize;
        String decimalDigits;
        String isNullable;
        int nullability;
        int numPrecRadix;
        String typeName;
        int sqlType;
        
        TypeInfo(String fullTypeName) {
            this.decimalDigits = null;
            this.numPrecRadix = 10;
            this.bufferLength = 65535;
            boolean isUnsigned = false;
            this.nullability = 1;
            this.isNullable = "YES";
            String mysqlType;
            if (fullTypeName.indexOf("(") != -1) {
                mysqlType = fullTypeName.substring(0, fullTypeName.indexOf("(")).trim();
            }
            else {
                mysqlType = fullTypeName;
            }
            final int indexOfUnsignedInMysqlType = mysqlType.toLowerCase(Locale.ROOT).indexOf("unsigned");
            if (indexOfUnsignedInMysqlType != -1) {
                mysqlType = mysqlType.substring(0, indexOfUnsignedInMysqlType - 1);
            }
            this.typeName = mysqlType;
            this.sqlType = JDBC4DatabaseMetaData.this.mapMariaDbTypeToJdbc(mysqlType);
            final int indexUnsigned = fullTypeName.toLowerCase(Locale.ROOT).indexOf("unsigned");
            if (indexUnsigned != -1) {
                fullTypeName = fullTypeName.substring(0, indexUnsigned - 1);
                isUnsigned = true;
            }
            if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("enum")) {
                final String sub = fullTypeName.substring(fullTypeName.indexOf("("), fullTypeName.indexOf(")"));
                final StringTokenizer tokenizer = new StringTokenizer(sub, ",");
                int length = 0;
                while (tokenizer.hasMoreTokens()) {
                    length = Math.max(length, tokenizer.nextToken().length() - 2);
                }
                this.columnSize = length;
            }
            if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("set")) {
                final String sub = fullTypeName.substring(fullTypeName.indexOf("("), fullTypeName.indexOf(")"));
                final StringTokenizer tokenizer = new StringTokenizer(sub, ",");
                int length = 0;
                this.columnSize = 0;
                final int num = tokenizer.countTokens();
                if (num > 0) {
                    length += num - 1;
                }
                while (tokenizer.hasMoreTokens()) {
                    final String setMember = tokenizer.nextToken().trim();
                    if (setMember.startsWith("'") && setMember.endsWith("'")) {
                        length += setMember.length() - 2;
                    }
                    else {
                        length += setMember.length();
                    }
                }
                this.columnSize = length;
            }
            else if (fullTypeName.indexOf(",") != -1) {
                this.columnSize = Integer.valueOf(fullTypeName.substring(fullTypeName.indexOf("(") + 1, fullTypeName.indexOf(",")));
                this.decimalDigits = fullTypeName.substring(fullTypeName.indexOf(",") + 1, fullTypeName.indexOf(")"));
            }
            else if ((fullTypeName.toLowerCase(Locale.ROOT).indexOf("char") != -1 || fullTypeName.toLowerCase(Locale.ROOT).indexOf("text") != -1 || fullTypeName.toLowerCase(Locale.ROOT).indexOf("binary") != -1 || fullTypeName.toLowerCase(Locale.ROOT).indexOf("blob") != -1 || fullTypeName.toLowerCase(Locale.ROOT).indexOf("bit") != -1) && fullTypeName.indexOf("(") != -1) {
                int endIndex = fullTypeName.indexOf(")");
                if (endIndex == -1) {
                    endIndex = fullTypeName.length();
                }
                this.columnSize = Integer.valueOf(fullTypeName.substring(fullTypeName.indexOf("(") + 1, endIndex));
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("tinyint")) {
                this.columnSize = 5;
                this.decimalDigits = "0";
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("smallint")) {
                this.columnSize = 5;
                this.decimalDigits = "0";
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("mediumint")) {
                this.columnSize = (isUnsigned ? 8 : 7);
                this.decimalDigits = "0";
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("int")) {
                this.columnSize = 10;
                this.decimalDigits = "0";
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("integer")) {
                this.columnSize = 10;
                this.decimalDigits = "0";
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("bigint")) {
                this.columnSize = (isUnsigned ? 20 : 19);
                this.decimalDigits = "0";
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("int24")) {
                this.columnSize = 19;
                this.decimalDigits = "0";
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("real")) {
                this.columnSize = 12;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("float")) {
                this.columnSize = 12;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("decimal")) {
                this.columnSize = 12;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("numeric")) {
                this.columnSize = 12;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("double")) {
                this.columnSize = 12;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("char")) {
                this.columnSize = 1;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("varchar")) {
                this.columnSize = 255;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("timestamp")) {
                this.columnSize = 19;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("datetime")) {
                this.columnSize = 19;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("date")) {
                this.columnSize = 10;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("time")) {
                this.columnSize = 8;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("tinyblob")) {
                this.columnSize = 255;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("blob")) {
                this.columnSize = 65535;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("mediumblob")) {
                this.columnSize = 16777215;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("longblob")) {
                this.columnSize = Integer.MAX_VALUE;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("tinytext")) {
                this.columnSize = 255;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("text")) {
                this.columnSize = 65535;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("mediumtext")) {
                this.columnSize = 16777215;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("longtext")) {
                this.columnSize = Integer.MAX_VALUE;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("enum")) {
                this.columnSize = 255;
            }
            else if (fullTypeName.toLowerCase(Locale.ROOT).startsWith("set")) {
                this.columnSize = 255;
            }
        }
    }
}
