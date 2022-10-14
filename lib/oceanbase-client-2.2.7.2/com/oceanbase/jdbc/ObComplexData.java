// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import com.oceanbase.jdbc.extend.datatype.ComplexDataType;

public interface ObComplexData
{
    ComplexDataType getComplexType();
    
    void setComplexType(final ComplexDataType p0);
    
    Object getAttrData(final int p0);
    
    Object[] getAttrData();
    
    void setAttrData(final Object[] p0);
    
    int getAttrCount();
    
    void setAttrCount(final int p0);
    
    void addAttrData(final int p0, final Object p1);
}
