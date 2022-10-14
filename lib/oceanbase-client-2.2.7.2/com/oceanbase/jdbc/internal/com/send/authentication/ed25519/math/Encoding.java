// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math;

public abstract class Encoding
{
    protected Field f;
    
    public synchronized void setField(final Field f) {
        if (this.f != null) {
            throw new IllegalStateException("already set");
        }
        this.f = f;
    }
    
    public abstract byte[] encode(final FieldElement p0);
    
    public abstract FieldElement decode(final byte[] p0);
    
    public abstract boolean isNegative(final FieldElement p0);
}
