// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.ed25519.spec;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.GroupElement;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.ed25519.ScalarOps;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.Curve;
import java.io.Serializable;
import java.security.spec.AlgorithmParameterSpec;

public class EdDSAParameterSpec implements AlgorithmParameterSpec, Serializable
{
    private static final long serialVersionUID = 8274987108472012L;
    private final Curve curve;
    private final String hashAlgo;
    private final ScalarOps sc;
    private final GroupElement B;
    
    public EdDSAParameterSpec(final Curve curve, final String hashAlgo, final ScalarOps sc, final GroupElement B) {
        try {
            final MessageDigest hash = MessageDigest.getInstance(hashAlgo);
            if (curve.getField().getb() / 4 != hash.getDigestLength()) {
                throw new IllegalArgumentException("Hash output is not 2b-bit");
            }
        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unsupported hash algorithm");
        }
        this.curve = curve;
        this.hashAlgo = hashAlgo;
        this.sc = sc;
        this.B = B;
    }
    
    public Curve getCurve() {
        return this.curve;
    }
    
    public String getHashAlgorithm() {
        return this.hashAlgo;
    }
    
    public ScalarOps getScalarOps() {
        return this.sc;
    }
    
    public GroupElement getB() {
        return this.B;
    }
    
    @Override
    public int hashCode() {
        return this.hashAlgo.hashCode() ^ this.curve.hashCode() ^ this.B.hashCode();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof EdDSAParameterSpec)) {
            return false;
        }
        final EdDSAParameterSpec s = (EdDSAParameterSpec)o;
        return this.hashAlgo.equals(s.getHashAlgorithm()) && this.curve.equals(s.getCurve()) && this.B.equals(s.getB());
    }
}
