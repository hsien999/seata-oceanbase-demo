// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util;

public class ParsedCallParameters
{
    private boolean isParam;
    private String name;
    
    public ParsedCallParameters(final boolean isParam, final String name) {
        this.isParam = isParam;
        this.name = name;
    }
    
    public boolean isParam() {
        return this.isParam;
    }
    
    public String getName() {
        return this.name;
    }
}
