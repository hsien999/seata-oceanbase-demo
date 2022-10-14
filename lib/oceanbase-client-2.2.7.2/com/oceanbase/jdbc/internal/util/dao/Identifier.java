// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.dao;

public class Identifier
{
    public String schema;
    public String name;
    
    @Override
    public String toString() {
        if (this.schema != null) {
            return this.schema + "." + this.name;
        }
        return this.name;
    }
}
