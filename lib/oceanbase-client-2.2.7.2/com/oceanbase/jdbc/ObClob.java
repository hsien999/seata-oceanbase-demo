// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.SQLException;
import java.sql.Clob;

public interface ObClob extends Clob
{
    ObLobLocator getLocator();
    
    void setLocator(final ObLobLocator p0);
    
    void updateClobToServer(final long p0, final byte[] p1, final int p2, final int p3) throws SQLException;
}
