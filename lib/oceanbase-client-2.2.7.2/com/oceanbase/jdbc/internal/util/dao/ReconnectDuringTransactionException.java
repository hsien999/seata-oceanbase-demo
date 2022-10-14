// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.dao;

import java.sql.SQLException;

public class ReconnectDuringTransactionException extends SQLException
{
    public ReconnectDuringTransactionException(final String message, final int errorCode, final String sqlState) {
        super(message, sqlState, errorCode);
    }
}
