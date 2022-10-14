// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.jdbc2.optional;

import com.oceanbase.jdbc.OceanBaseXid;

public class MysqlXid extends OceanBaseXid
{
    public MysqlXid(final byte[] globalTransactionId, final byte[] branchQualifier, final int formatId) {
        super(globalTransactionId, branchQualifier, formatId);
    }
}
