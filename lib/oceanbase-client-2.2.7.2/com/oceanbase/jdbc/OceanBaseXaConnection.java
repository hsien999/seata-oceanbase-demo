// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import javax.transaction.xa.XAResource;
import javax.sql.XAConnection;

public class OceanBaseXaConnection extends OceanBasePooledConnection implements XAConnection
{
    public OceanBaseXaConnection(final OceanBaseConnection connection) {
        super(connection);
    }
    
    @Override
    public XAResource getXAResource() {
        return new OceanBaseXaResource(this.getConnection());
    }
}
