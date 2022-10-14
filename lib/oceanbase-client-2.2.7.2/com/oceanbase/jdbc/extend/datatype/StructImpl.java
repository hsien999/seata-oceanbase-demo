// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

import java.util.Map;
import java.sql.SQLException;
import com.oceanbase.jdbc.ObStruct;

public class StructImpl extends ComplexData implements ObStruct
{
    public StructImpl(final ComplexDataType type) {
        super(type);
    }
    
    @Override
    public String getSQLTypeName() throws SQLException {
        return this.getComplexType().getTypeName();
    }
    
    @Override
    public Object[] getAttributes() throws SQLException {
        return this.getAttrData();
    }
    
    @Override
    public Object[] getAttributes(final Map<String, Class<?>> map) throws SQLException {
        return new Object[0];
    }
}
