// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math;

import java.io.Serializable;

public abstract class FieldElement implements Serializable
{
    private static final long serialVersionUID = 1239527465875676L;
    protected final Field f;
    
    public FieldElement(final Field f) {
        if (null == f) {
            throw new IllegalArgumentException("field cannot be null");
        }
        this.f = f;
    }
    
    public byte[] toByteArray() {
        return this.f.getEncoding().encode(this);
    }
    
    public abstract boolean isNonZero();
    
    public boolean isNegative() {
        return this.f.getEncoding().isNegative(this);
    }
    
    public abstract FieldElement add(final FieldElement p0);
    
    public FieldElement addOne() {
        return this.add(this.f.ONE);
    }
    
    public abstract FieldElement subtract(final FieldElement p0);
    
    public FieldElement subtractOne() {
        return this.subtract(this.f.ONE);
    }
    
    public abstract FieldElement negate();
    
    public FieldElement divide(final FieldElement val) {
        return this.multiply(val.invert());
    }
    
    public abstract FieldElement multiply(final FieldElement p0);
    
    public abstract FieldElement square();
    
    public abstract FieldElement squareAndDouble();
    
    public abstract FieldElement invert();
    
    public abstract FieldElement pow22523();
    
    public abstract FieldElement cmov(final FieldElement p0, final int p1);
}
