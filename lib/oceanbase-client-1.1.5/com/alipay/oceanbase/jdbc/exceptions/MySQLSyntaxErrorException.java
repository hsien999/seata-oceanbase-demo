// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.exceptions;

public class MySQLSyntaxErrorException extends MySQLNonTransientException
{
    static final long serialVersionUID = 6919059513432113764L;
    
    public MySQLSyntaxErrorException() {
    }
    
    public MySQLSyntaxErrorException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLSyntaxErrorException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLSyntaxErrorException(final String reason) {
        super(reason);
    }
}
