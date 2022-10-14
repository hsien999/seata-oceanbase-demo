// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.dao;

import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import java.util.HashMap;
import java.util.Locale;
import java.sql.SQLException;
import java.util.Map;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;

public class ColumnLabelIndexer
{
    private final ColumnDefinition[] columnInfo;
    private Map<String, Integer> originalMap;
    private Map<String, Integer> aliasMap;
    
    public ColumnLabelIndexer(final ColumnDefinition[] columnDefinitions) {
        this.columnInfo = columnDefinitions;
    }
    
    public int getIndex(final String name) throws SQLException {
        if (name == null) {
            throw new SQLException("Column name cannot be null");
        }
        final String lowerName = name.toLowerCase(Locale.ROOT);
        if (this.aliasMap == null) {
            final Map<String, Integer> map = new HashMap<String, Integer>();
            int counter = 0;
            for (final ColumnDefinition ci : this.columnInfo) {
                String columnAlias = ci.getName();
                if (columnAlias != null) {
                    columnAlias = columnAlias.toLowerCase(Locale.ROOT);
                    map.putIfAbsent(columnAlias, counter);
                    final String tableName = ci.getTable();
                    if (tableName != null) {
                        map.putIfAbsent(tableName.toLowerCase(Locale.ROOT) + "." + columnAlias, counter);
                    }
                }
                ++counter;
            }
            this.aliasMap = map;
        }
        Integer res = this.aliasMap.get(lowerName);
        if (res != null) {
            return res;
        }
        if (this.originalMap == null) {
            final Map<String, Integer> map2 = new HashMap<String, Integer>();
            int counter2 = 0;
            for (final ColumnDefinition ci2 : this.columnInfo) {
                String columnRealName = ci2.getOriginalName();
                if (columnRealName != null) {
                    columnRealName = columnRealName.toLowerCase(Locale.ROOT);
                    map2.putIfAbsent(columnRealName, counter2);
                    final String tableName2 = ci2.getOriginalTable();
                    if (tableName2 != null) {
                        map2.putIfAbsent(tableName2.toLowerCase(Locale.ROOT) + "." + columnRealName, counter2);
                    }
                }
                ++counter2;
            }
            this.originalMap = map2;
        }
        res = this.originalMap.get(lowerName);
        if (res == null) {
            final Map<String, Integer> possible = new HashMap<String, Integer>();
            possible.putAll(this.aliasMap);
            possible.putAll(this.originalMap);
            throw ExceptionFactory.INSTANCE.create(String.format("No such column: '%s'. '%s' must be in %s", name, lowerName, possible.keySet().toString()), "42S22", 1054);
        }
        return res;
    }
    
    public void setAliasMap(final Map<String, Integer> map) {
        this.aliasMap = map;
    }
}
