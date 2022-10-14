// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.sql.Blob;

public interface ObBlob extends Blob
{
    ObLobLocator getLocator();
    
    void setLocator(final ObLobLocator p0);
    
    void updateBlobToServer(final long p0, final byte[] p1, final int p2, final int p3) throws SQLException;
    
    void trimBlobToServer(final int p0) throws SQLException;
}
