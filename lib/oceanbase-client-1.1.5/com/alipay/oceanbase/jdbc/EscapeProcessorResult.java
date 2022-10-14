// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

class EscapeProcessorResult
{
    boolean callingStoredFunction;
    String escapedSql;
    byte usesVariables;
    
    EscapeProcessorResult() {
        this.callingStoredFunction = false;
        this.usesVariables = 0;
    }
}
