// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.exceptions.jdbc4;

import java.sql.SQLTransientConnectionException;

public class MySQLTransientConnectionException extends SQLTransientConnectionException
{
    static final long serialVersionUID = 8699144578759941201L;
    
    public MySQLTransientConnectionException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLTransientConnectionException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLTransientConnectionException(final String reason) {
        super(reason);
    }
    
    public MySQLTransientConnectionException() {
    }
}
