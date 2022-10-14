// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol;

import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.dao.PrepareResult;

public class AsyncMultiReadResult
{
    private PrepareResult prepareResult;
    private SQLException exception;
    
    public AsyncMultiReadResult(final PrepareResult prepareResult) {
        this.prepareResult = prepareResult;
        this.exception = null;
    }
    
    public PrepareResult getPrepareResult() {
        return this.prepareResult;
    }
    
    public void setPrepareResult(final PrepareResult prepareResult) {
        this.prepareResult = prepareResult;
    }
    
    public SQLException getException() {
        return this.exception;
    }
    
    public void setException(final SQLException exception) {
        this.exception = exception;
    }
}
