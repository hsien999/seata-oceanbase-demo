// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import java.sql.Savepoint;

public class OceanBaseSavepoint implements Savepoint
{
    private final String name;
    
    public OceanBaseSavepoint(final String name) {
        this.name = name;
    }
    
    @Override
    public int getSavepointId() throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Doesn't support savepoint identifier");
    }
    
    @Override
    public String getSavepointName() {
        return this.name;
    }
    
    @Override
    public String toString() {
        return "MariaDbSavepoint{name='" + this.name + '\'' + '}';
    }
}
