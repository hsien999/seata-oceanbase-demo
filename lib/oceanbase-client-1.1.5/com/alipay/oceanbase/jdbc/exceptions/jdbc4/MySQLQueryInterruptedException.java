// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.exceptions.jdbc4;

public class MySQLQueryInterruptedException extends MySQLNonTransientException
{
    private static final long serialVersionUID = -8714521137662613517L;
    
    public MySQLQueryInterruptedException() {
    }
    
    public MySQLQueryInterruptedException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLQueryInterruptedException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLQueryInterruptedException(final String reason) {
        super(reason);
    }
}
