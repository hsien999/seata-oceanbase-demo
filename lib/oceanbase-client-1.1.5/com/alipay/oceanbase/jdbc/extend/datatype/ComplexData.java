// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.extend.datatype;

import com.alipay.oceanbase.jdbc.ObComplexData;

public class ComplexData implements ObComplexData
{
    protected ComplexDataType complexType;
    protected Object[] attrData;
    protected int attrCount;
    
    public ComplexData(final ComplexDataType type) {
        this.attrData = null;
        this.attrCount = 0;
        this.complexType = type;
    }
    
    @Override
    public ComplexDataType getComplexType() {
        return this.complexType;
    }
    
    @Override
    public void setComplexType(final ComplexDataType complexType) {
        this.complexType = complexType;
    }
    
    @Override
    public Object getAttrData(final int attrIndex) {
        if (attrIndex >= this.getAttrCount()) {
            return null;
        }
        return this.attrData[attrIndex];
    }
    
    @Override
    public Object[] getAttrData() {
        return this.attrData;
    }
    
    @Override
    public void setAttrData(final Object[] attrData) {
        if (null != attrData) {
            this.attrCount = attrData.length;
            this.attrData = attrData;
        }
    }
    
    @Override
    public int getAttrCount() {
        return this.attrCount;
    }
    
    @Override
    public void setAttrCount(final int attrCount) {
        this.attrCount = attrCount;
    }
    
    @Override
    public void addAttrData(final int index, final Object value) {
        if (null == this.attrData) {
            this.attrData = new Object[this.attrCount];
        }
        if (index < this.attrCount) {
            this.attrData[index] = value;
        }
    }
}
