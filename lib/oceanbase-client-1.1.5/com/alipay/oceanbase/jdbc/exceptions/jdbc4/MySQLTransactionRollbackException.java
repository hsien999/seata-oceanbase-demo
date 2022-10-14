// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.exceptions.jdbc4;

import com.alipay.oceanbase.jdbc.exceptions.DeadlockTimeoutRollbackMarker;
import java.sql.SQLTransactionRollbackException;

public class MySQLTransactionRollbackException extends SQLTransactionRollbackException implements DeadlockTimeoutRollbackMarker
{
    static final long serialVersionUID = 6034999468737801730L;
    
    public MySQLTransactionRollbackException(final String reason, final String SQLState, final int vendorCode) {
        super(reason, SQLState, vendorCode);
    }
    
    public MySQLTransactionRollbackException(final String reason, final String SQLState) {
        super(reason, SQLState);
    }
    
    public MySQLTransactionRollbackException(final String reason) {
        super(reason);
    }
    
    public MySQLTransactionRollbackException() {
    }
}
