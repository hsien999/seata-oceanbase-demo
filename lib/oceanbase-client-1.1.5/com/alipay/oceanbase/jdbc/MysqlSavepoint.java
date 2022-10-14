// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.rmi.server.UID;
import java.sql.Savepoint;

public class MysqlSavepoint implements Savepoint
{
    private String savepointName;
    private ExceptionInterceptor exceptionInterceptor;
    
    private static String getUniqueId() {
        final String uidStr = new UID().toString();
        final int uidLength = uidStr.length();
        final StringBuilder safeString = new StringBuilder(uidLength + 1);
        safeString.append('_');
        for (int i = 0; i < uidLength; ++i) {
            final char c = uidStr.charAt(i);
            if (Character.isLetter(c) || Character.isDigit(c)) {
                safeString.append(c);
            }
            else {
                safeString.append('_');
            }
        }
        return safeString.toString();
    }
    
    MysqlSavepoint(final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        this(getUniqueId(), exceptionInterceptor);
    }
    
    MysqlSavepoint(final String name, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        if (name == null || name.length() == 0) {
            throw SQLError.createSQLException("Savepoint name can not be NULL or empty", "S1009", exceptionInterceptor);
        }
        this.savepointName = name;
        this.exceptionInterceptor = exceptionInterceptor;
    }
    
    @Override
    public int getSavepointId() throws SQLException {
        throw SQLError.createSQLException("Only named savepoints are supported.", "S1C00", this.exceptionInterceptor);
    }
    
    @Override
    public String getSavepointName() throws SQLException {
        return this.savepointName;
    }
}
