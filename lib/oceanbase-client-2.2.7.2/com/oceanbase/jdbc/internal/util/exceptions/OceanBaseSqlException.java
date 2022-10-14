// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.exceptions;

import java.sql.SQLException;

public class OceanBaseSqlException extends SQLException
{
    private String sql;
    private String threadName;
    private String deadLockInfo;
    
    public OceanBaseSqlException(final String reason, final String sql, final Throwable cause) {
        super(reason, cause);
        this.threadName = null;
        this.deadLockInfo = null;
        this.sql = sql;
    }
    
    public OceanBaseSqlException(final String reason, final String sql, final String sqlState, final Throwable cause) {
        super(reason, sqlState, cause);
        this.threadName = null;
        this.deadLockInfo = null;
        this.sql = sql;
    }
    
    public OceanBaseSqlException(final String reason, final String sql, final String sqlState, final int vendorCode, final Throwable cause) {
        super(reason, sqlState, vendorCode, cause);
        this.threadName = null;
        this.deadLockInfo = null;
        this.sql = sql;
    }
    
    public static OceanBaseSqlException of(final SQLException cause, final String sql) {
        return new OceanBaseSqlException(cause.getMessage().contains("\n") ? cause.getMessage().substring(0, cause.getMessage().indexOf("\n")) : cause.getMessage(), sql, cause.getSQLState(), cause.getErrorCode(), cause);
    }
    
    public OceanBaseSqlException withThreadName(final String threadName) {
        this.threadName = threadName;
        return this;
    }
    
    public OceanBaseSqlException withDeadLockInfo(final String deadLockInfo) {
        this.deadLockInfo = deadLockInfo;
        return this;
    }
    
    public String getSql() {
        return this.sql;
    }
    
    public String getThreadName() {
        return this.threadName;
    }
    
    public String getDeadLockInfo() {
        return this.deadLockInfo;
    }
}
