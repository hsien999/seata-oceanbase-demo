// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.NClob;

public class JDBC4NClob extends Clob implements NClob
{
    JDBC4NClob(final String charDataInit, final ExceptionInterceptor exceptionInterceptor) {
        super(charDataInit, exceptionInterceptor);
    }
}
