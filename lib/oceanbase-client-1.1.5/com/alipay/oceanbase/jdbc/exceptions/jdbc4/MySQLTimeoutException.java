// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.exceptions.jdbc4;

import java.sql.SQLTimeoutException;

public class MySQLTimeoutException extends SQLTimeoutException
{
    static final long serialVersionUID = -789621240523230339L;
    
    public MySQLTimeoutException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLTimeoutException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLTimeoutException(final String reason) {
        super(reason);
    }
    
    public MySQLTimeoutException() {
        super("Statement cancelled due to timeout or client request");
    }
    
    @Override
    public int getErrorCode() {
        return super.getErrorCode();
    }
}
