// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.dao;

import com.oceanbase.jdbc.OceanBaseConnection;
import java.sql.CallableStatement;

public interface CloneableCallableStatement extends CallableStatement
{
    CloneableCallableStatement clone(final OceanBaseConnection p0) throws CloneNotSupportedException;
}
