// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.exceptions;

public class MySQLIntegrityConstraintViolationException extends MySQLNonTransientException
{
    static final long serialVersionUID = -5528363270635808904L;
    
    public MySQLIntegrityConstraintViolationException() {
    }
    
    public MySQLIntegrityConstraintViolationException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLIntegrityConstraintViolationException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLIntegrityConstraintViolationException(final String reason) {
        super(reason);
    }
}
