// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.DataTruncation;

public class MysqlDataTruncation extends DataTruncation
{
    static final long serialVersionUID = 3263928195256986226L;
    private String message;
    private int vendorErrorCode;
    
    public MysqlDataTruncation(final String message, final int index, final boolean parameter, final boolean read, final int dataSize, final int transferSize, final int vendorErrorCode) {
        super(index, parameter, read, dataSize, transferSize);
        this.message = message;
        this.vendorErrorCode = vendorErrorCode;
    }
    
    @Override
    public int getErrorCode() {
        return this.vendorErrorCode;
    }
    
    @Override
    public String getMessage() {
        return super.getMessage() + ": " + this.message;
    }
}
