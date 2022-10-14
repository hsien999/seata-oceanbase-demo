// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

public class AssertionFailedException extends RuntimeException
{
    private static final long serialVersionUID = 1L;
    
    public static void shouldNotHappen(final Exception ex) throws AssertionFailedException {
        throw new AssertionFailedException(ex);
    }
    
    public AssertionFailedException(final Exception ex) {
        super(Messages.getString("AssertionFailedException.0") + ex.toString() + Messages.getString("AssertionFailedException.1"));
    }
}
