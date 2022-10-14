// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.util.List;
import com.oceanbase.jdbc.internal.ColumnType;
import java.util.ArrayList;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import com.oceanbase.jdbc.internal.com.read.resultset.SelectResultSet;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

public abstract class OceanBaseOracleDatabaseMetadata implements DatabaseMetaData
{
    private final UrlParser urlParser;
    private final OceanBaseConnection connection;
    
    public OceanBaseOracleDatabaseMetadata(final UrlParser urlParser, final Connection connection) {
        this.urlParser = urlParser;
        this.connection = (OceanBaseConnection)connection;
    }
    
    public ResultSet executeQuery(final String sql) throws SQLException {
        final Statement stmt = this.connection.createStatement();
        final SelectResultSet rs = (SelectResultSet)stmt.executeQuery(sql);
        rs.setStatement(null);
        rs.setForceTableAlias();
        return rs;
    }
    
    @Override
    public ResultSet getProcedures(final String catalog, final String schemaPattern, final String procedureNamePattern) throws SQLException {
        final String var1 = "SELECT\n  -- Standalone procedures and functions\n  NULL AS procedure_cat,\n  owner AS procedure_schem,\n  object_name AS procedure_name,\n  NULL,\n  NULL,\n  NULL,\n  'Standalone procedure or function' AS remarks,\n  DECODE(object_type, 'PROCEDURE', 1,\n                      'FUNCTION', 2,\n                      0) AS procedure_type\n,  NULL AS specific_name\nFROM all_objects\nWHERE (object_type = 'PROCEDURE' OR object_type = 'FUNCTION')\n  AND owner LIKE ? \n  AND object_name LIKE ? \n";
        final String var2 = "SELECT\n  -- Packaged procedures with no arguments\n  package_name AS procedure_cat,\n  owner AS procedure_schem,\n  object_name AS procedure_name,\n  NULL,\n  NULL,\n  NULL,\n  'Packaged procedure' AS remarks,\n  1 AS procedure_type\n,  NULL AS specific_name\nFROM all_arguments\nWHERE argument_name IS NULL\n  AND data_type IS NULL\n  AND ";
        final String var3 = "SELECT\n  -- Packaged procedures with arguments\n  package_name AS procedure_cat,\n  owner AS procedure_schem,\n  object_name AS procedure_name,\n  NULL,\n  NULL,\n  NULL,\n  'Packaged procedure' AS remarks,\n  1 AS procedure_type\n,  NULL AS specific_name\nFROM all_arguments\nWHERE argument_name IS NOT NULL\n  AND position = 1\n  AND position = sequence\n  AND ";
        final String var4 = "SELECT\n  -- Packaged functions\n  package_name AS procedure_cat,\n  owner AS procedure_schem,\n  object_name AS procedure_name,\n  NULL,\n  NULL,\n  NULL,\n  'Packaged function' AS remarks,\n  2 AS procedure_type\n,  NULL AS specific_name\nFROM all_arguments\nWHERE argument_name IS NULL\n  AND in_out = 'OUT'\n  AND   data_level = 0\n  AND ";
        final String var5 = "package_name LIKE ? \n  AND owner LIKE ? \n  AND object_name LIKE ? \n";
        final String var6 = "package_name IS NOT NULL\n  AND owner LIKE ? \n  AND object_name LIKE ? \n";
        final String var7 = "ORDER BY procedure_schem, procedure_name\n";
        PreparedStatement stmt = null;
        String var8 = null;
        String var9 = null;
        if (schemaPattern == null) {
            var9 = "%";
        }
        else if (schemaPattern.equals("")) {
            var9 = this.getUserName().toUpperCase();
        }
        else {
            var9 = schemaPattern.toUpperCase();
        }
        String var10 = null;
        if (procedureNamePattern == null) {
            var10 = "%";
        }
        else {
            if (procedureNamePattern.equals("")) {
                throw new SQLException();
            }
            var10 = procedureNamePattern.toUpperCase();
        }
        if (catalog == null) {
            var8 = var1 + "UNION ALL " + var2 + var6 + "UNION ALL " + var3 + var6 + "UNION ALL " + var4 + var6 + var7;
            stmt = this.connection.prepareStatement(var8);
            stmt.setString(1, var9);
            stmt.setString(2, var10);
            stmt.setString(3, var9);
            stmt.setString(4, var10);
            stmt.setString(5, var9);
            stmt.setString(6, var10);
            stmt.setString(7, var9);
            stmt.setString(8, var10);
        }
        else if (catalog.equals("")) {
            stmt = this.connection.prepareStatement(var1);
            stmt.setString(1, var9);
            stmt.setString(2, var10);
        }
        else {
            var8 = var2 + var5 + "UNION ALL " + var3 + var5 + "UNION ALL " + var4 + var5 + var7;
            stmt = this.connection.prepareStatement(var8);
            stmt.setString(1, catalog);
            stmt.setString(2, var9);
            stmt.setString(3, var10);
            stmt.setString(4, catalog);
            stmt.setString(5, var9);
            stmt.setString(6, var10);
            stmt.setString(7, catalog);
            stmt.setString(8, var9);
            stmt.setString(9, var10);
        }
        stmt.closeOnCompletion();
        final ResultSet rs = stmt.executeQuery();
        return rs;
    }
    
    @Override
    public ResultSet getTables(final String catalog, final String schemaPattern, final String tableNamePattern, final String[] types) throws SQLException {
        final String var1 = "SELECT NULL AS table_cat,\n       o.owner AS table_schem,\n       o.object_name AS table_name,\n       o.object_type AS table_type,\n";
        final String var2 = "  c.comments AS remarks\n";
        final String var3 = "  FROM all_objects o, all_tab_comments c\n";
        final String var4 = "  WHERE o.owner LIKE ? ESCAPE '/'\n    AND o.object_name LIKE ? ESCAPE '/'\n      AND o.owner = c.owner (+)\n    AND o.object_name = c.table_name (+)\n  AND o.owner != '__recyclebin'\n";
        String var5 = "";
        String var6 = "";
        if (types != null) {
            var5 = "    AND o.object_type IN ('xxx'";
            var6 = "    AND o.object_type IN ('xxx'";
            for (int i = 0; i < types.length; ++i) {
                if (types[i].equals("SYNONYM")) {
                    var5 = var5 + ", '" + types[i] + "'";
                }
                else {
                    var5 = var5 + ", '" + types[i] + "'";
                    var6 = var6 + ", '" + types[i] + "'";
                }
            }
            var5 += ")\n";
        }
        else {
            var5 = "    AND o.object_type IN ('TABLE', 'SYNONYM', 'VIEW')\n";
        }
        final String var7 = "  ORDER BY table_type, table_schem, table_name\n";
        String sql = "";
        sql += var1;
        sql = sql + var2 + var3;
        sql += var4;
        sql += var5;
        sql += var7;
        final PreparedStatement stmt = this.connection.prepareStatement(sql);
        stmt.setString(1, (schemaPattern == null) ? "%" : schemaPattern);
        stmt.setString(2, (tableNamePattern == null) ? "%" : tableNamePattern);
        stmt.closeOnCompletion();
        final ResultSet rs = stmt.executeQuery();
        return rs;
    }
    
    @Override
    public ResultSet getSchemas() throws SQLException {
        final Statement stmt = this.connection.createStatement();
        final String sql = "SELECT username AS table_schem,null as table_catalog  FROM all_users ORDER BY table_schem";
        stmt.closeOnCompletion();
        final ResultSet rs = stmt.executeQuery(sql);
        return rs;
    }
    
    @Override
    public ResultSet getCatalogs() throws SQLException {
        return this.executeQuery("select 'nothing' as table_cat from dual where 1 = 2");
    }
    
    @Override
    public ResultSet getColumns(final String catalog, final String schemaPattern, final String tableNamePattern, final String columnNamePattern) throws SQLException {
        final Statement stmt = this.connection.getMetadataSafeStatement();
        final String querySql = String.format("SELECT  NULL AS table_cat,\n       t.owner AS table_schem,\n       t.table_name AS table_name,\n       t.column_name AS column_name,\n       DECODE (t.data_type, 'CHAR', 1, 'VARCHAR2', 12, 'NUMBER', 3,\n               'LONG', -1, 'DATE', 93, 'RAW', -3, 'LONG RAW', -4,  \n               'BLOB', 2004, 'CLOB', 2005, 'BFILE', -13, 'FLOAT', 6, \n               'TIMESTAMP(6)', 93, 'TIMESTAMP(6) WITH TIME ZONE', -101, \n               'TIMESTAMP(6) WITH LOCAL TIME ZONE', -102, \n               'INTERVAL YEAR(2) TO MONTH', -103, \n               'INTERVAL DAY(2) TO SECOND(6)', -104, \n               'BINARY_FLOAT', 100, 'BINARY_DOUBLE', 101, \n               'XMLTYPE', 2009, \n               1111)\n              AS data_type,\n       t.data_type AS type_name,\n       DECODE (t.data_precision, null,          DECODE (t.data_type, 'CHAR', t.char_length,                   'VARCHAR', t.char_length,                   'VARCHAR2', t.char_length,                   'NVARCHAR2', t.char_length,                   'NCHAR', t.char_length,                   'NUMBER', 0,           t.data_length),         t.data_precision)\n              AS column_size,\n       0 AS buffer_length,\n       DECODE (t.data_type,                'NUMBER', DECODE (t.data_precision,                                  null, -127,                                  t.data_scale),                t.data_scale) AS decimal_digits,\n       10 AS num_prec_radix,\n       DECODE (t.nullable, 'N', 0, 1) AS nullable,\n       NULL AS remarks,\n       t.data_default AS column_def,\n       0 AS sql_data_type,\n       0 AS sql_datetime_sub,\n       t.data_length AS char_octet_length,\n       t.column_id AS ordinal_position,\n       DECODE (t.nullable, 'N', 'NO', 'YES') AS is_nullable\nFROM all_tab_columns t\nWHERE t.owner LIKE '%s' ESCAPE '/'\n  AND t.table_name LIKE '%s' ESCAPE '/'\n  AND t.column_name LIKE '%s' ESCAPE '/'\n  AND t.owner != '__recyclebin' \n\nORDER BY table_schem, table_name, ordinal_position\n", (schemaPattern == null) ? "%" : schemaPattern, (tableNamePattern == null) ? "%" : tableNamePattern, (columnNamePattern == null) ? Character.valueOf('%') : columnNamePattern);
        stmt.closeOnCompletion();
        final ResultSet results = stmt.executeQuery(querySql);
        final String[] data = new String[24];
        final List<String[]> list = new ArrayList<String[]>();
        int i = 0;
        while (results.next()) {
            data[0] = null;
            data[1] = results.getString("TABLE_SCHEM");
            data[2] = results.getString("TABLE_NAME");
            data[3] = results.getString("COLUMN_NAME");
            data[4] = results.getString("DATA_TYPE");
            data[5] = results.getString("TYPE_NAME");
            data[6] = results.getString("COLUMN_SIZE");
            data[7] = results.getString("BUFFER_LENGTH");
            data[8] = results.getString("DECIMAL_DIGITS");
            data[9] = results.getString("NUM_PREC_RADIX");
            final String nullabilityInfo = results.getString("IS_NULLABLE");
            String isNullable = null;
            int nullability = 0;
            if (nullabilityInfo != null) {
                if (nullabilityInfo.equals("YES")) {
                    nullability = 1;
                    isNullable = "YES";
                }
                else if (nullabilityInfo.equals("UNKNOWN")) {
                    nullability = 2;
                    isNullable = "";
                }
                else {
                    nullability = 0;
                    isNullable = "NO";
                }
            }
            else {
                nullability = 0;
                isNullable = "NO";
            }
            data[10] = Integer.toString(nullability);
            data[11] = results.getString("REMARKS");
            data[12] = results.getString("COLUMN_DEF");
            data[13] = results.getString("SQL_DATA_TYPE");
            data[14] = results.getString("SQL_DATETIME_SUB");
            data[15] = results.getString("CHAR_OCTET_LENGTH");
            data[16] = Integer.toString(results.getInt("ORDINAL_POSITION") - 15);
            data[17] = isNullable;
            data[19] = (data[18] = null);
            data[21] = (data[20] = null);
            data[22] = "NO";
            data[23] = null;
            ++i;
            final String[] tmp = new String[24];
            System.arraycopy(data, 0, tmp, 0, data.length);
            list.add(tmp);
        }
        final String[][] val = new String[list.size()][];
        for (int j = 0; j < list.size(); ++j) {
            val[j] = list.get(j);
        }
        final String[] columnNames = { "TABLE_CAT", "TABLE_SCHEM", "TABLE_NAME", "COLUMN_NAME", "DATA_TYPE", "TYPE_NAME", "COLUMN_SIZE", "BUFFER_LENGTH", "DECIMAL_DIGITS", "NUM_PREC_RADIX", "NULLABLE", "REMARKS", "COLUMN_DEF", "SQL_DATA_TYPE", "SQL_DATETIME_SUB", "CHAR_OCTET_LENGTH", "ORDINAL_POSITION", "IS_NULLABLE", "SCOPE_CATALOG", "SCOPE_SCHEMA", "SCOPE_TABLE", "SOURCE_DATA_TYPE", "IS_AUTOINCREMENT", "IS_GENERATEDCOLUMN" };
        final ColumnType[] columnTypes = { ColumnType.ENUM, ColumnType.ENUM, ColumnType.ENUM, ColumnType.ENUM, ColumnType.INTEGER, ColumnType.ENUM, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.ENUM, ColumnType.ENUM, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.INTEGER, ColumnType.ENUM, ColumnType.ENUM, ColumnType.ENUM, ColumnType.ENUM, ColumnType.SMALLINT, ColumnType.ENUM, ColumnType.ENUM };
        final ResultSet rs = JDBC4ResultSet.createResultSet(columnNames, columnTypes, val, this.connection.getProtocol());
        return rs;
    }
    
    @Override
    public ResultSet getSchemas(final String catalog, final String schemaPattern) throws SQLException {
        if (schemaPattern == null) {
            return this.getSchemas();
        }
        final String sql = "SELECT username AS table_schem, null as table_catalog FROM all_users WHERE username LIKE ? ORDER BY table_schem";
        final PreparedStatement stmt = this.connection.prepareStatement(sql);
        stmt.setString(1, schemaPattern);
        stmt.closeOnCompletion();
        final ResultSet rs = stmt.executeQuery();
        return rs;
    }
    
    @Override
    public ResultSet getFunctions(final String catalog, final String schemaPattern, final String functionNamePattern) throws SQLException {
        final String var1 = "SELECT\n  NULL AS function_cat,\n  owner AS function_schem,\n  object_name AS function_name,\n  'Standalone function' AS remarks,\n  0 AS function_type,\n  NULL AS specific_name\nFROM all_objects\nWHERE object_type = 'FUNCTION'\n  AND owner LIKE ? \n  AND object_name LIKE ? \n";
        final String var2 = "SELECT\n  -- Packaged functions\n  package_name AS function_cat,\n  owner AS function_schem,\n  object_name AS function_name,\n  'Packaged function' AS remarks,\n  decode (data_type, 'TABLE', 2, 'PL/SQL TABLE', 2, 1) AS function_type,\n  NULL AS specific_name\nFROM all_arguments\nWHERE argument_name IS NULL\n  AND in_out = 'OUT'\n  AND data_level = 0\n";
        final String var3 = "  AND package_name LIKE ? \n  AND owner LIKE ? \n  AND object_name LIKE ? \n";
        final String var4 = "  AND package_name IS NOT NULL\n  AND owner LIKE ? \n  AND object_name LIKE ? \n";
        final String var5 = "ORDER BY function_schem, function_name\n";
        PreparedStatement stmt = null;
        String var6 = null;
        String var7 = schemaPattern;
        if (schemaPattern == null) {
            var7 = "%";
        }
        else if (schemaPattern.equals("")) {
            var7 = this.getUserName().toUpperCase();
        }
        else {
            var7 = schemaPattern.toUpperCase();
        }
        String var8 = functionNamePattern;
        if (functionNamePattern == null) {
            var8 = "%";
        }
        else {
            if (functionNamePattern.equals("")) {
                throw new SQLException();
            }
            var8 = functionNamePattern.toUpperCase();
        }
        if (catalog == null) {
            var6 = var1 + "UNION ALL " + var2 + var4 + var5;
            stmt = this.connection.prepareStatement(var6);
            stmt.setString(1, var7);
            stmt.setString(2, var8);
            stmt.setString(3, var7);
            stmt.setString(4, var8);
        }
        else if (catalog.equals("")) {
            stmt = this.connection.prepareStatement(var1);
            stmt.setString(1, var7);
            stmt.setString(2, var8);
        }
        else {
            var6 = var2 + var3 + var5;
            stmt = this.connection.prepareStatement(var6);
            stmt.setString(1, var7);
            stmt.setString(2, var7);
            stmt.setString(3, var8);
        }
        stmt.closeOnCompletion();
        final ResultSet rs = stmt.executeQuery();
        return rs;
    }
    
    @Override
    public String getUserName() throws SQLException {
        final ResultSet rs = this.executeQuery("SELECT USER FROM DUAL");
        rs.next();
        final String userName = rs.getString(1);
        return userName;
    }
    
    @Override
    public ResultSet getTableTypes() throws SQLException {
        return this.executeQuery("select 'TABLE' as table_type from dual\nunion select 'VIEW' as table_type from dual\nunion select 'SYNONYM' as table_type from dual\n");
    }
    
    @Override
    public ResultSet getTablePrivileges(final String catalog, final String schemaPattern, final String tableNamePattern) throws SQLException {
        final PreparedStatement stmt = this.connection.prepareStatement("SELECT NULL AS table_cat,\n       table_schema AS table_schem,\n       table_name,\n       grantor,\n       grantee,\n       privilege,\n       grantable AS is_grantable\nFROM all_tab_privs\nWHERE table_schema LIKE ? ESCAPE '/'\n  AND table_name LIKE ? ESCAPE '/'\nORDER BY table_schem, table_name, privilege\n");
        stmt.setString(1, (schemaPattern == null) ? "%" : schemaPattern);
        stmt.setString(2, (tableNamePattern == null) ? "%" : tableNamePattern.toUpperCase());
        stmt.closeOnCompletion();
        final ResultSet rs = stmt.executeQuery();
        return rs;
    }
    
    @Override
    public ResultSet getIndexInfo(final String catalog, final String schema, final String table, final boolean unique, final boolean approximate) throws SQLException {
        final Statement stmt = this.connection.createStatement();
        String sql = "";
        sql = "select null as table_cat,\n       owner as table_schem,\n       table_name,\n       0 as NON_UNIQUE,\n       null as index_qualifier,\n       null as index_name, 0 as type,\n       0 as ordinal_position, null as column_name,\n       null as asc_or_desc,\n       num_rows as cardinality,\n       blocks as pages,\n       null as filter_condition\nfrom all_tables\nwhere table_name = '" + table + "'\n";
        final String sql2 = "select null as table_cat,\n       i.owner as table_schem,\n       i.table_name,\n       decode (i.uniqueness, 'UNIQUE', 0, 1),\n       null as index_qualifier,\n       i.index_name,\n       1 as type,\n       c.column_position as ordinal_position,\n       c.column_name,\n       null as asc_or_desc,\n       i.distinct_keys as cardinality,\n       i.leaf_blocks as pages,\n       null as filter_condition\nfrom (select /*+no_merge*/ * from all_indexes i where i.table_name = '" + table + "') i, all_ind_columns c\n" + "where i.table_name = '" + table + "'\n";
        String sql3 = "";
        if (unique) {
            sql3 = "  and i.uniqueness = 'UNIQUE'\n";
        }
        final String sql4 = "  and i.index_name = c.index_name\n  and i.table_owner = c.table_owner\n  and i.table_name = c.table_name\n  and i.owner = c.index_owner\n";
        final String sql5 = "order by non_unique, type, index_name, ordinal_position\n";
        final String sql6 = sql + "union\n" + sql2 + sql3 + sql4 + sql5;
        stmt.closeOnCompletion();
        final ResultSet rs = stmt.executeQuery(sql6);
        return rs;
    }
    
    @Override
    public ResultSet getPrimaryKeys(final String catalog, final String schema, final String table) throws SQLException {
        final PreparedStatement stmt = this.connection.prepareStatement("SELECT NULL AS table_cat,\n       c.owner AS table_schem,\n       c.table_name,\n       c.column_name,\n       c.position AS key_seq,\n       c.constraint_name AS pk_name\nFROM all_cons_columns c, all_constraints k\nWHERE k.constraint_type = 'P'\n  AND k.table_name = ?\n  AND k.owner like ? escape '/'\n  AND k.constraint_name = c.constraint_name \n  AND k.table_name = c.table_name \n  AND k.owner = c.owner \nORDER BY column_name\n");
        stmt.setString(1, table);
        stmt.setString(2, (schema == null) ? "%" : schema);
        stmt.closeOnCompletion();
        final ResultSet rs = stmt.executeQuery();
        return rs;
    }
    
    @Override
    public ResultSet getBestRowIdentifier(final String catalog, final String schema, final String table, final int scope, final boolean nullable) throws SQLException {
        final PreparedStatement stmt = this.connection.prepareStatement("SELECT 1 AS scope, 'ROWID' AS column_name, -8 AS data_type,\n 'ROWID' AS type_name, 0 AS column_size, 0 AS buffer_length,\n       0 AS decimal_digits, 2 AS pseudo_column\nFROM DUAL\nWHERE ? = 1\nUNION\nSELECT 2 AS scope,\n t.column_name,\n  DECODE(substr(t.data_type, 1, 9), \n    'TIMESTAMP', \n      DECODE(substr(t.data_type, 10, 1), \n        '(', \n          DECODE(substr(t.data_type, 19, 5), \n            'LOCAL', -102, 'TIME ', -101, 93), \n        DECODE(substr(t.data_type, 16, 5), \n          'LOCAL', -102, 'TIME ', -101, 93)), \n    'INTERVAL ', \n      DECODE(substr(t.data_type, 10, 3), \n       'DAY', -104, 'YEA', -103), \n    DECODE(t.data_type, \n      'BINARY_DOUBLE', 101, \n      'BINARY_FLOAT', 100, \n      'BFILE', -13, \n      'BLOB', 2004, \n      'CHAR', 1, \n      'CLOB', 2005, \n      'COLLECTION', 2003, \n      'DATE', 93, \n      'FLOAT', 6, \n      'LONG', -1, \n      'LONG RAW', -4, \n      'NCHAR', -15, \n      'NCLOB', 2011, \n      'NUMBER', 2, \n      'NVARCHAR', -9, \n      'NVARCHAR2', -9, \n      'OBJECT', 2002, \n      'OPAQUE/XMLTYPE', 2009, \n      'RAW', -3, \n      'REF', 2006, \n      'ROWID', -8, \n      'SQLXML', 2009, \n      'UROWID', -8, \n      'VARCHAR2', 12, \n      'VARRAY', 2003, \n      'XMLTYPE', 2009, \n      DECODE((SELECT a.typecode \n        FROM ALL_TYPES a \n        WHERE a.type_name = t.data_type\n       AND ((a.owner IS NULL AND \nt.data_type_owner IS NULL)\n         OR (a.owner = t.data_type_owner))\n        ), \n        'OBJECT', 2002, \n        'COLLECTION', 2003, 1111))) \n AS data_type,\n t.data_type AS type_name,\n DECODE (t.data_precision, null,  DECODE (t.data_type, 'CHAR', t.char_length, 'VARCHAR', t.char_length, 'VARCHAR2', t.char_length, 'NVARCHAR2', t.char_length, 'NCHAR', t.char_length, t.data_length), t.data_precision)\n  AS column_size,\n  0 AS buffer_length,\n  t.data_scale AS decimal_digits,\n       1 AS pseudo_column\nFROM all_tab_columns t, all_ind_columns i\nWHERE ? = 1\n  AND t.table_name = ?\n  AND t.owner like ? escape '/'\n  AND t.nullable != ?\n  AND t.owner = i.table_owner\n  AND t.table_name = i.table_name\n  AND t.column_name = i.column_name\n");
        switch (scope) {
            case 1: {
                stmt.setInt(1, 1);
                stmt.setInt(2, 1);
                break;
            }
            case 2: {
                stmt.setInt(1, 0);
                stmt.setInt(2, 1);
                break;
            }
            default: {
                stmt.setInt(1, 0);
                stmt.setInt(2, 0);
                break;
            }
        }
        stmt.setString(3, table);
        stmt.setString(4, (schema == null) ? "%" : schema);
        stmt.setString(5, nullable ? "X" : "Y");
        stmt.closeOnCompletion();
        final ResultSet rs = stmt.executeQuery();
        return rs;
    }
    
    @Override
    public ResultSet getColumnPrivileges(final String catalog, final String schema, final String table, final String columnNamePattern) throws SQLException {
        final PreparedStatement stmt = this.connection.prepareStatement("SELECT NULL AS table_cat,\n       OWNER AS table_schem,\n       table_name,\n       column_name,\n       grantor,\n       grantee,\n       privilege,\n       grantable AS is_grantable\nFROM all_col_privs\nWHERE OWNER LIKE ? ESCAPE '/'\n  AND table_name LIKE ? ESCAPE '/'\n  AND column_name LIKE ? ESCAPE '/'\nORDER BY column_name, privilege\n");
        stmt.setString(1, (schema == null) ? "%" : schema);
        stmt.setString(2, (table == null) ? "%" : table.toUpperCase());
        stmt.setString(3, (columnNamePattern == null) ? "%" : columnNamePattern);
        stmt.closeOnCompletion();
        return stmt.executeQuery();
    }
    
    @Override
    public boolean supportsMixedCaseIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean storesLowerCaseIdentifiers() throws SQLException {
        return true;
    }
    
    @Override
    public boolean storesMixedCaseIdentifiers() throws SQLException {
        return false;
    }
    
    @Override
    public boolean supportsANSI92IntermediateSQL() {
        return false;
    }
    
    @Override
    public boolean supportsOpenCursorsAcrossRollback() {
        return false;
    }
    
    @Override
    public boolean allProceduresAreCallable() {
        return false;
    }
    
    @Override
    public boolean allTablesAreSelectable() {
        return false;
    }
    
    @Override
    public boolean supportsOpenCursorsAcrossCommit() {
        return false;
    }
    
    @Override
    public boolean supportsOpenStatementsAcrossRollback() {
        return false;
    }
    
    @Override
    public boolean supportsOpenStatementsAcrossCommit() {
        return false;
    }
    
    @Override
    public boolean nullsAreSortedAtEnd() {
        return false;
    }
    
    @Override
    public boolean locatorsUpdateCopy() {
        return true;
    }
    
    @Override
    public boolean supportsANSI92FullSQL() {
        return false;
    }
    
    @Override
    public boolean storesUpperCaseIdentifiers() {
        return true;
    }
    
    @Override
    public boolean doesMaxRowSizeIncludeBlobs() {
        return true;
    }
    
    @Override
    public boolean supportsConvert() {
        return false;
    }
    
    @Override
    public boolean supportsCatalogsInTableDefinitions() {
        return false;
    }
    
    @Override
    public boolean supportsSchemasInTableDefinitions() {
        return true;
    }
    
    public ResultSet keysQuery(final String parentSchema, final String parentTable, final String foreignSchema, final String foreignTable, final String orderBy) throws SQLException {
        int index = 1;
        final int parentTableIndex = (parentTable != null) ? index++ : 0;
        final int foreignTableIndex = (foreignTable != null) ? index++ : 0;
        final int parentSchemaIndex = (parentSchema != null && parentSchema.length() > 0) ? index++ : 0;
        final int foreignSchemaIndex = (foreignSchema != null && foreignSchema.length() > 0) ? index++ : 0;
        final PreparedStatement stmt = this.connection.prepareStatement("SELECT NULL AS pktable_cat,\n       p.owner as pktable_schem,\n       p.table_name as pktable_name,\n       pc.column_name as pkcolumn_name,\n       NULL as fktable_cat,\n       f.owner as fktable_schem,\n       f.table_name as fktable_name,\n       fc.column_name as fkcolumn_name,\n       fc.position as key_seq,\n       NULL as update_rule,\n       decode (f.delete_rule, 'CASCADE', 0, 'SET NULL', 2, 1) as delete_rule,\n       f.constraint_name as fk_name,\n       p.constraint_name as pk_name,\n       decode(f.deferrable,       'DEFERRABLE',5      ,'NOT DEFERRABLE',7      , 'DEFERRED', 6      ) deferrability \n      FROM all_cons_columns pc, all_constraints p,\n      all_cons_columns fc, all_constraints f\nWHERE 1 = 1\n" + ((parentTableIndex != 0) ? "  AND p.table_name = ?\n" : "") + ((foreignTableIndex != 0) ? "  AND f.table_name = ?\n" : "") + ((parentSchemaIndex != 0) ? "  AND p.owner = ?\n" : "") + ((foreignSchemaIndex != 0) ? "  AND f.owner = ?\n" : "") + "  AND f.constraint_type = 'R'\n  AND p.owner = f.r_owner\n  AND p.constraint_name = f.r_constraint_name\n  AND p.constraint_type = 'P'\n  AND pc.owner = p.owner\n  AND pc.constraint_name = p.constraint_name\n  AND pc.table_name = p.table_name\n  AND fc.owner = f.owner\n  AND fc.constraint_name = f.constraint_name\n  AND fc.table_name = f.table_name\n  AND fc.position = pc.position\n" + orderBy);
        if (parentTableIndex != 0) {
            stmt.setString(parentTableIndex, parentTable);
        }
        if (foreignTableIndex != 0) {
            stmt.setString(foreignTableIndex, foreignTable);
        }
        if (parentSchemaIndex != 0) {
            stmt.setString(parentSchemaIndex, parentSchema);
        }
        if (foreignSchemaIndex != 0) {
            stmt.setString(foreignSchemaIndex, foreignSchema);
        }
        stmt.closeOnCompletion();
        final ResultSet rs = stmt.executeQuery();
        return rs;
    }
}
