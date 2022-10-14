// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.exceptions;

public class MySQLStatementCancelledException extends MySQLNonTransientException
{
    static final long serialVersionUID = -8762717748377197378L;
    
    public MySQLStatementCancelledException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLStatementCancelledException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLStatementCancelledException(final String reason) {
        super(reason);
    }
    
    public MySQLStatementCancelledException() {
        super("Statement cancelled due to client request");
    }
}
