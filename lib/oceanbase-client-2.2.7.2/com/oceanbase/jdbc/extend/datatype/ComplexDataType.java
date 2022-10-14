// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

import java.sql.SQLException;

public class ComplexDataType
{
    private String typeName;
    private String schemaName;
    private long version;
    boolean isValid;
    private int type;
    private int attrCount;
    private int initAttrCount;
    private ComplexDataType[] attrTypes;
    public static final int TYPE_NUMBER = 0;
    public static final int TYPE_VARCHAR2 = 1;
    public static final int TYPE_DATE = 2;
    public static final int TYPE_OBJECT = 3;
    public static final int TYPE_COLLECTION = 4;
    public static final int TYPE_CURSOR = 5;
    public static final int TYPE_RAW = 6;
    public static final int TYPE_CHAR = 7;
    public static final int TYPE_MAX = 8;
    
    public ComplexDataType(final String typeName, final String schemaName, final int type) {
        this.version = 0L;
        this.isValid = false;
        this.initAttrCount = 0;
        this.attrTypes = null;
        this.schemaName = schemaName;
        this.type = type;
        this.typeName = typeName;
    }
    
    public String getTypeName() {
        return this.typeName;
    }
    
    public void setTypeName(final String typeName) {
        this.typeName = typeName;
    }
    
    public String getSchemaName() {
        return this.schemaName;
    }
    
    public void setSchemaName(final String schemaName) {
        this.schemaName = schemaName;
    }
    
    public long getVersion() {
        return this.version;
    }
    
    public void setVersion(final long version) {
        this.version = version;
    }
    
    public int getType() {
        return this.type;
    }
    
    public void setType(final int type) {
        this.type = type;
    }
    
    public int getAttrCount() {
        return this.attrCount;
    }
    
    public void setAttrCount(final int attrCount) {
        this.attrCount = attrCount;
    }
    
    public ComplexDataType[] getAttrTypes() {
        return this.attrTypes;
    }
    
    public ComplexDataType getAttrType(final int attrIndex) {
        if (attrIndex >= this.attrCount) {
            return null;
        }
        return this.attrTypes[attrIndex];
    }
    
    public void setAttrType(final int attrIndex, final ComplexDataType attrType) {
        if (null == this.attrTypes) {
            this.attrTypes = new ComplexDataType[this.attrCount];
        }
        this.attrTypes[attrIndex] = attrType;
    }
    
    public boolean isValid() {
        return this.isValid;
    }
    
    public void setValid(final boolean valid) {
        this.isValid = valid;
    }
    
    public int getInitAttrCount() {
        return this.initAttrCount;
    }
    
    public void incInitAttrCount() {
        ++this.initAttrCount;
    }
    
    public static int getObComplexType(final String attrType) throws SQLException {
        if (attrType.equalsIgnoreCase("COLLECTION")) {
            return 4;
        }
        if (attrType.equalsIgnoreCase("OBJECT")) {
            return 3;
        }
        if (attrType.equalsIgnoreCase("NUMBER") || attrType.equalsIgnoreCase("INTEGER") || attrType.equalsIgnoreCase("DECIMAL")) {
            return 0;
        }
        if (attrType.equalsIgnoreCase("VARCHAR2") || attrType.equalsIgnoreCase("VARCHAR")) {
            return 1;
        }
        if (attrType.equalsIgnoreCase("DATE")) {
            return 2;
        }
        if (attrType.equalsIgnoreCase("CURSOR")) {
            return 5;
        }
        if (attrType.equalsIgnoreCase("RAW")) {
            return 6;
        }
        if (attrType.equalsIgnoreCase("CHAR")) {
            return 7;
        }
        return 8;
    }
    
    public static boolean isBaseDataType(final int type) {
        return 0 == type || 1 == type || 2 == type;
    }
    
    public static byte[] getBytes() {
        return null;
    }
}
