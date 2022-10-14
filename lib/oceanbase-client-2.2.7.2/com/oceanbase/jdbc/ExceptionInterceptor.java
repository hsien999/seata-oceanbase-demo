// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.security.cert.Extension;

public interface ExceptionInterceptor extends Extension
{
    SQLException interceptException(final SQLException p0, final Connection p1);
}
