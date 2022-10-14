// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.util.Arrays;
import javax.transaction.xa.Xid;

public class OceanBaseXid implements Xid
{
    private final int formatId;
    private final byte[] globalTransactionId;
    private final byte[] branchQualifier;
    
    public OceanBaseXid(final int formatId, final byte[] globalTransactionId, final byte[] branchQualifier) {
        this.formatId = formatId;
        this.globalTransactionId = globalTransactionId;
        this.branchQualifier = branchQualifier;
    }
    
    public OceanBaseXid(final byte[] globalTransactionId, final byte[] branchQualifier, final int formatId) {
        this.formatId = formatId;
        this.globalTransactionId = globalTransactionId;
        this.branchQualifier = branchQualifier;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Xid) {
            final Xid other = (Xid)obj;
            return this.formatId == other.getFormatId() && Arrays.equals(this.globalTransactionId, other.getGlobalTransactionId()) && Arrays.equals(this.branchQualifier, other.getBranchQualifier());
        }
        return false;
    }
    
    @Override
    public int getFormatId() {
        return this.formatId;
    }
    
    @Override
    public byte[] getGlobalTransactionId() {
        return this.globalTransactionId;
    }
    
    @Override
    public byte[] getBranchQualifier() {
        return this.branchQualifier;
    }
}
