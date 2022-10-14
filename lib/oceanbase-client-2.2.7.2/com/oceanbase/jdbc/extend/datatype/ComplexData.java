// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

public class ComplexData
{
    protected ComplexDataType complexType;
    protected Object[] attrData;
    protected int attrCount;
    
    public ComplexData(final ComplexDataType type) {
        this.attrData = null;
        this.attrCount = 0;
        this.complexType = type;
    }
    
    public ComplexDataType getComplexType() {
        return this.complexType;
    }
    
    public void setComplexType(final ComplexDataType complexType) {
        this.complexType = complexType;
    }
    
    public Object getAttrData(final int attrIndex) {
        if (attrIndex >= this.getAttrCount()) {
            return null;
        }
        return this.attrData[attrIndex];
    }
    
    public Object[] getAttrData() {
        if (this.attrCount == 0) {
            this.attrData = new Object[0];
        }
        return this.attrData;
    }
    
    public void setAttrData(final Object[] attrData) {
        if (null != attrData) {
            this.attrCount = attrData.length;
            this.attrData = attrData;
        }
    }
    
    public int getAttrCount() {
        return this.attrCount;
    }
    
    public void setAttrCount(final int attrCount) {
        this.attrCount = attrCount;
    }
    
    public void addAttrData(final int index, final Object value) {
        if (null == this.attrData) {
            this.attrData = new Object[this.attrCount];
        }
        if (index < this.attrCount) {
            this.attrData[index] = value;
        }
    }
}
