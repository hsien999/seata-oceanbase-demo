// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.ResultSetMetaData;
import java.util.Map;

public class CachedResultSetMetaData
{
    Map<String, Integer> columnNameToIndex;
    Field[] fields;
    Map<String, Integer> fullColumnNameToIndex;
    ResultSetMetaData metadata;
    
    public CachedResultSetMetaData() {
        this.columnNameToIndex = null;
        this.fullColumnNameToIndex = null;
    }
    
    public Map<String, Integer> getColumnNameToIndex() {
        return this.columnNameToIndex;
    }
    
    public Field[] getFields() {
        return this.fields;
    }
    
    public Map<String, Integer> getFullColumnNameToIndex() {
        return this.fullColumnNameToIndex;
    }
    
    public ResultSetMetaData getMetadata() {
        return this.metadata;
    }
}
