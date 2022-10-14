// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.ed25519.spec;

import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.GroupElement;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.ed25519.ScalarOps;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.Curve;

public class EdDSANamedCurveSpec extends EdDSAParameterSpec
{
    private final String name;
    
    public EdDSANamedCurveSpec(final String name, final Curve curve, final String hashAlgo, final ScalarOps sc, final GroupElement B) {
        super(curve, hashAlgo, sc, B);
        this.name = name;
    }
    
    public String getName() {
        return this.name;
    }
}
