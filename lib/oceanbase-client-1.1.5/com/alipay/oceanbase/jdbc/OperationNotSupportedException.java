// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;

class OperationNotSupportedException extends SQLException
{
    static final long serialVersionUID = 474918612056813430L;
    
    OperationNotSupportedException() {
        super(Messages.getString("RowDataDynamic.10"), "S1009");
    }
}
