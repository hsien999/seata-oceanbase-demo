// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math;

import java.io.Serializable;

public class Field implements Serializable
{
    private static final long serialVersionUID = 8746587465875676L;
    public final FieldElement ZERO;
    public final FieldElement ONE;
    public final FieldElement TWO;
    public final FieldElement FOUR;
    public final FieldElement FIVE;
    public final FieldElement EIGHT;
    private final int b;
    private final FieldElement q;
    private final FieldElement qm2;
    private final FieldElement qm5d8;
    private final Encoding enc;
    
    public Field(final int b, final byte[] q, final Encoding enc) {
        this.b = b;
        (this.enc = enc).setField(this);
        this.q = this.fromByteArray(q);
        this.ZERO = this.fromByteArray(Constants.ZERO);
        this.ONE = this.fromByteArray(Constants.ONE);
        this.TWO = this.fromByteArray(Constants.TWO);
        this.FOUR = this.fromByteArray(Constants.FOUR);
        this.FIVE = this.fromByteArray(Constants.FIVE);
        this.EIGHT = this.fromByteArray(Constants.EIGHT);
        this.qm2 = this.q.subtract(this.TWO);
        this.qm5d8 = this.q.subtract(this.FIVE).divide(this.EIGHT);
    }
    
    public FieldElement fromByteArray(final byte[] x) {
        return this.enc.decode(x);
    }
    
    public int getb() {
        return this.b;
    }
    
    public FieldElement getQ() {
        return this.q;
    }
    
    public FieldElement getQm2() {
        return this.qm2;
    }
    
    public FieldElement getQm5d8() {
        return this.qm5d8;
    }
    
    public Encoding getEncoding() {
        return this.enc;
    }
    
    @Override
    public int hashCode() {
        return this.q.hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Field)) {
            return false;
        }
        final Field f = (Field)obj;
        return this.b == f.b && this.q.equals(f.q);
    }
}
