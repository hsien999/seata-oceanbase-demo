// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

import java.sql.RowId;

public class RowIdImpl implements RowId
{
    private String val;
    
    public RowIdImpl(final String value) {
        this.val = value;
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof RowIdImpl && obj.hashCode() == this.hashCode();
    }
    
    @Override
    public byte[] getBytes() {
        if (this.val != null) {
            return this.val.getBytes();
        }
        return new byte[0];
    }
    
    @Override
    public String toString() {
        return this.val;
    }
    
    @Override
    public int hashCode() {
        return (this.val != null) ? this.val.hashCode() : 0;
    }
}
