// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.exceptions;

public class MySQLNonTransientConnectionException extends MySQLNonTransientException
{
    static final long serialVersionUID = -3050543822763367670L;
    
    public MySQLNonTransientConnectionException() {
    }
    
    public MySQLNonTransientConnectionException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLNonTransientConnectionException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLNonTransientConnectionException(final String reason) {
        super(reason);
    }
}