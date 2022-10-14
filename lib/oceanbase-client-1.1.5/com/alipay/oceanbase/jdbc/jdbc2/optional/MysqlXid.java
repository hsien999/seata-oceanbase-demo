// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdbc2.optional;

import javax.transaction.xa.Xid;

public class MysqlXid implements Xid
{
    int hash;
    byte[] myBqual;
    int myFormatId;
    byte[] myGtrid;
    
    public MysqlXid(final byte[] gtrid, final byte[] bqual, final int formatId) {
        this.hash = 0;
        this.myGtrid = gtrid;
        this.myBqual = bqual;
        this.myFormatId = formatId;
    }
    
    @Override
    public boolean equals(final Object another) {
        if (another instanceof Xid) {
            final Xid anotherAsXid = (Xid)another;
            if (this.myFormatId != anotherAsXid.getFormatId()) {
                return false;
            }
            final byte[] otherBqual = anotherAsXid.getBranchQualifier();
            final byte[] otherGtrid = anotherAsXid.getGlobalTransactionId();
            if (otherGtrid != null && otherGtrid.length == this.myGtrid.length) {
                for (int length = otherGtrid.length, i = 0; i < length; ++i) {
                    if (otherGtrid[i] != this.myGtrid[i]) {
                        return false;
                    }
                }
                if (otherBqual != null && otherBqual.length == this.myBqual.length) {
                    for (int length = otherBqual.length, i = 0; i < length; ++i) {
                        if (otherBqual[i] != this.myBqual[i]) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }
        }
        return false;
    }
    
    @Override
    public byte[] getBranchQualifier() {
        return this.myBqual;
    }
    
    @Override
    public int getFormatId() {
        return this.myFormatId;
    }
    
    @Override
    public byte[] getGlobalTransactionId() {
        return this.myGtrid;
    }
    
    @Override
    public synchronized int hashCode() {
        if (this.hash == 0) {
            for (int i = 0; i < this.myGtrid.length; ++i) {
                this.hash = 33 * this.hash + this.myGtrid[i];
            }
        }
        return this.hash;
    }
}
