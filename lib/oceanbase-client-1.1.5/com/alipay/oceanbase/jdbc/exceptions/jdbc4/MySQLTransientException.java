// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.exceptions.jdbc4;

import java.sql.SQLTransientException;

public class MySQLTransientException extends SQLTransientException
{
    static final long serialVersionUID = -1885878228558607563L;
    
    public MySQLTransientException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLTransientException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLTransientException(final String reason) {
        super(reason);
    }
    
    public MySQLTransientException() {
    }
}
