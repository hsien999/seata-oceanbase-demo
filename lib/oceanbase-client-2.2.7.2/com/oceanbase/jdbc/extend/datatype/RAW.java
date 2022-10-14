// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

import java.sql.SQLException;

public class RAW extends Datum
{
    public RAW(final byte[] bytes) {
        super(bytes);
    }
    
    public RAW(final Object val) {
    }
    
    @Override
    public boolean isConvertibleTo(final Class var1) {
        return false;
    }
    
    @Override
    public Object toJdbc() throws SQLException {
        return null;
    }
    
    @Override
    public Object makeJdbcArray(final int var1) {
        return null;
    }
}
