// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.ed25519.spec;

import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.ed25519.ScalarOps;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.Encoding;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.ed25519.Ed25519LittleEndianEncoding;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.Utils;
import java.util.Locale;
import java.util.Hashtable;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.Curve;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math.Field;

public class EdDSANamedCurveTable
{
    public static final String ED_25519 = "Ed25519";
    private static final Field ed25519field;
    private static final Curve ed25519curve;
    private static final EdDSANamedCurveSpec ed25519;
    private static final Hashtable<String, EdDSANamedCurveSpec> curves;
    
    public static void defineCurve(final EdDSANamedCurveSpec curve) {
        EdDSANamedCurveTable.curves.put(curve.getName().toLowerCase(Locale.ENGLISH), curve);
    }
    
    static void defineCurveAlias(final String name, final String alias) {
        final EdDSANamedCurveSpec curve = EdDSANamedCurveTable.curves.get(name.toLowerCase(Locale.ENGLISH));
        if (curve == null) {
            throw new IllegalStateException();
        }
        EdDSANamedCurveTable.curves.put(alias.toLowerCase(Locale.ENGLISH), curve);
    }
    
    public static EdDSANamedCurveSpec getByName(final String name) {
        return EdDSANamedCurveTable.curves.get(name.toLowerCase(Locale.ENGLISH));
    }
    
    static {
        ed25519field = new Field(256, Utils.hexToBytes("edffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f"), new Ed25519LittleEndianEncoding());
        ed25519curve = new Curve(EdDSANamedCurveTable.ed25519field, Utils.hexToBytes("a3785913ca4deb75abd841414d0a700098e879777940c78c73fe6f2bee6c0352"), EdDSANamedCurveTable.ed25519field.fromByteArray(Utils.hexToBytes("b0a00e4a271beec478e42fad0618432fa7d7fb3d99004d2b0bdfc14f8024832b")));
        ed25519 = new EdDSANamedCurveSpec("Ed25519", EdDSANamedCurveTable.ed25519curve, "SHA-512", new ScalarOps(), EdDSANamedCurveTable.ed25519curve.createPoint(Utils.hexToBytes("5866666666666666666666666666666666666666666666666666666666666666"), true));
        curves = new Hashtable<String, EdDSANamedCurveSpec>();
        defineCurve(EdDSANamedCurveTable.ed25519);
    }
}
