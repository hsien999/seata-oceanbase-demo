// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.jdbc2.optional;

import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import com.oceanbase.jdbc.OceanBaseConnection;
import com.oceanbase.jdbc.OceanBaseXaResource;
import javax.transaction.xa.XAResource;

public class JDBC4MysqlXAConnection extends MysqlXAConnection implements XAResource
{
    OceanBaseXaResource oceanBaseXaResource;
    
    public JDBC4MysqlXAConnection(final OceanBaseConnection connection) {
        super(connection);
        this.oceanBaseXaResource = new MysqlXAResource(connection);
    }
    
    @Override
    public XAResource getXAResource() {
        return this.oceanBaseXaResource;
    }
    
    @Override
    public void commit(final Xid xid, final boolean b) throws XAException {
        this.oceanBaseXaResource.commit(xid, b);
    }
    
    @Override
    public void end(final Xid xid, final int i) throws XAException {
        this.oceanBaseXaResource.end(xid, i);
    }
    
    @Override
    public void forget(final Xid xid) throws XAException {
        this.oceanBaseXaResource.forget(xid);
    }
    
    @Override
    public int getTransactionTimeout() throws XAException {
        return this.oceanBaseXaResource.getTransactionTimeout();
    }
    
    @Override
    public boolean isSameRM(final XAResource xaResource) throws XAException {
        return this.oceanBaseXaResource.isSameRM(xaResource);
    }
    
    @Override
    public int prepare(final Xid xid) throws XAException {
        return this.oceanBaseXaResource.prepare(xid);
    }
    
    @Override
    public Xid[] recover(final int i) throws XAException {
        return this.oceanBaseXaResource.recover(i);
    }
    
    @Override
    public void rollback(final Xid xid) throws XAException {
        this.oceanBaseXaResource.rollback(xid);
    }
    
    @Override
    public boolean setTransactionTimeout(final int i) throws XAException {
        return this.oceanBaseXaResource.setTransactionTimeout(i);
    }
    
    @Override
    public void start(final Xid xid, final int i) throws XAException {
        this.oceanBaseXaResource.start(xid, i);
    }
}
