// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.exceptions.jdbc4;

import java.sql.SQLInvalidAuthorizationSpecException;

public class MySQLInvalidAuthorizationSpecException extends SQLInvalidAuthorizationSpecException
{
    static final long serialVersionUID = 6878889837492500030L;
    
    public MySQLInvalidAuthorizationSpecException() {
    }
    
    public MySQLInvalidAuthorizationSpecException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLInvalidAuthorizationSpecException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLInvalidAuthorizationSpecException(final String reason) {
        super(reason);
    }
}
