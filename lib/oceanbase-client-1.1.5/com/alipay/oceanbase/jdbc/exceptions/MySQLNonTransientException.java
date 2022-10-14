// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.exceptions;

import java.sql.SQLException;

public class MySQLNonTransientException extends SQLException
{
    static final long serialVersionUID = -8714521137552613517L;
    
    public MySQLNonTransientException() {
    }
    
    public MySQLNonTransientException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLNonTransientException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLNonTransientException(final String reason) {
        super(reason);
    }
}
