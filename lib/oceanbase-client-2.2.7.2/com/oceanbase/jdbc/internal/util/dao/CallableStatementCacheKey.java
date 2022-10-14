// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.dao;

public class CallableStatementCacheKey
{
    private final String database;
    private final String query;
    
    public CallableStatementCacheKey(final String database, final String query) {
        this.database = database;
        this.query = query;
    }
    
    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        final CallableStatementCacheKey that = (CallableStatementCacheKey)object;
        return this.database.equals(that.database) && this.query.equals(that.query);
    }
    
    @Override
    public int hashCode() {
        int result = this.database.hashCode();
        result = 31 * result + this.query.hashCode();
        return result;
    }
}
