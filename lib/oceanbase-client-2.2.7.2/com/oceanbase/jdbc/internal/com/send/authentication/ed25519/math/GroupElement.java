// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.ed25519.math;

import java.util.Arrays;
import com.oceanbase.jdbc.internal.com.send.authentication.ed25519.Utils;
import java.io.Serializable;

public class GroupElement implements Serializable
{
    private static final long serialVersionUID = 2395879087349587L;
    final Curve curve;
    final Representation repr;
    final FieldElement X;
    final FieldElement Y;
    final FieldElement Z;
    final FieldElement T;
    GroupElement[][] precmp;
    GroupElement[] dblPrecmp;
    
    public GroupElement(final Curve curve, final Representation repr, final FieldElement X, final FieldElement Y, final FieldElement Z, final FieldElement T) {
        this.curve = curve;
        this.repr = repr;
        this.X = X;
        this.Y = Y;
        this.Z = Z;
        this.T = T;
    }
    
    public GroupElement(final Curve curve, final byte[] s) {
        final FieldElement y = curve.getField().fromByteArray(s);
        final FieldElement yy = y.square();
        final FieldElement u = yy.subtractOne();
        final FieldElement v = yy.multiply(curve.getD()).addOne();
        final FieldElement v2 = v.square().multiply(v);
        FieldElement x = v2.square().multiply(v).multiply(u);
        x = x.pow22523();
        x = v2.multiply(u).multiply(x);
        final FieldElement vxx = x.square().multiply(v);
        FieldElement check = vxx.subtract(u);
        if (check.isNonZero()) {
            check = vxx.add(u);
            if (check.isNonZero()) {
                throw new IllegalArgumentException("not a valid GroupElement");
            }
            x = x.multiply(curve.getI());
        }
        if ((x.isNegative() ? 1 : 0) != Utils.bit(s, curve.getField().getb() - 1)) {
            x = x.negate();
        }
        this.curve = curve;
        this.repr = Representation.P3;
        this.X = x;
        this.Y = y;
        this.Z = curve.getField().ONE;
        this.T = this.X.multiply(this.Y);
    }
    
    public static GroupElement p2(final Curve curve, final FieldElement X, final FieldElement Y, final FieldElement Z) {
        return new GroupElement(curve, Representation.P2, X, Y, Z, null);
    }
    
    public static GroupElement p3(final Curve curve, final FieldElement X, final FieldElement Y, final FieldElement Z, final FieldElement T) {
        return new GroupElement(curve, Representation.P3, X, Y, Z, T);
    }
    
    public static GroupElement p1p1(final Curve curve, final FieldElement X, final FieldElement Y, final FieldElement Z, final FieldElement T) {
        return new GroupElement(curve, Representation.P1P1, X, Y, Z, T);
    }
    
    public static GroupElement precomp(final Curve curve, final FieldElement ypx, final FieldElement ymx, final FieldElement xy2d) {
        return new GroupElement(curve, Representation.PRECOMP, ypx, ymx, xy2d, null);
    }
    
    public static GroupElement cached(final Curve curve, final FieldElement YpX, final FieldElement YmX, final FieldElement Z, final FieldElement T2d) {
        return new GroupElement(curve, Representation.CACHED, YpX, YmX, Z, T2d);
    }
    
    static byte[] toRadix16(final byte[] a) {
        final byte[] e = new byte[64];
        for (int i = 0; i < 32; ++i) {
            e[2 * i + 0] = (byte)(a[i] & 0xF);
            e[2 * i + 1] = (byte)(a[i] >> 4 & 0xF);
        }
        int carry = 0;
        for (int i = 0; i < 63; ++i) {
            final byte[] array = e;
            final int n = i;
            array[n] += (byte)carry;
            carry = e[i] + 8;
            carry >>= 4;
            final byte[] array2 = e;
            final int n2 = i;
            array2[n2] -= (byte)(carry << 4);
        }
        final byte[] array3 = e;
        final int n3 = 63;
        array3[n3] += (byte)carry;
        return e;
    }
    
    static byte[] slide(final byte[] a) {
        final byte[] r = new byte[256];
        for (int i = 0; i < 256; ++i) {
            r[i] = (byte)(0x1 & a[i >> 3] >> (i & 0x7));
        }
        for (int i = 0; i < 256; ++i) {
            if (r[i] != 0) {
                for (int b = 1; b <= 6 && i + b < 256; ++b) {
                    if (r[i + b] != 0) {
                        if (r[i] + (r[i + b] << b) <= 15) {
                            final byte[] array = r;
                            final int n = i;
                            array[n] += (byte)(r[i + b] << b);
                            r[i + b] = 0;
                        }
                        else {
                            if (r[i] - (r[i + b] << b) < -15) {
                                break;
                            }
                            final byte[] array2 = r;
                            final int n2 = i;
                            array2[n2] -= (byte)(r[i + b] << b);
                            for (int k = i + b; k < 256; ++k) {
                                if (r[k] == 0) {
                                    r[k] = 1;
                                    break;
                                }
                                r[k] = 0;
                            }
                        }
                    }
                }
            }
        }
        return r;
    }
    
    public Curve getCurve() {
        return this.curve;
    }
    
    public Representation getRepresentation() {
        return this.repr;
    }
    
    public FieldElement getX() {
        return this.X;
    }
    
    public FieldElement getY() {
        return this.Y;
    }
    
    public FieldElement getZ() {
        return this.Z;
    }
    
    public FieldElement getT() {
        return this.T;
    }
    
    public byte[] toByteArray() {
        switch (this.repr) {
            case P2:
            case P3: {
                final FieldElement recip = this.Z.invert();
                final FieldElement x = this.X.multiply(recip);
                final FieldElement y = this.Y.multiply(recip);
                final byte[] byteArray;
                final byte[] s = byteArray = y.toByteArray();
                final int n = s.length - 1;
                byteArray[n] |= (byte)(x.isNegative() ? -128 : 0);
                return s;
            }
            default: {
                return this.toP2().toByteArray();
            }
        }
    }
    
    public GroupElement toP2() {
        return this.toRep(Representation.P2);
    }
    
    public GroupElement toP3() {
        return this.toRep(Representation.P3);
    }
    
    public GroupElement toCached() {
        return this.toRep(Representation.CACHED);
    }
    
    private GroupElement toRep(final Representation repr) {
        switch (this.repr) {
            case P2: {
                switch (repr) {
                    case P2: {
                        return p2(this.curve, this.X, this.Y, this.Z);
                    }
                    default: {
                        throw new IllegalArgumentException();
                    }
                }
                break;
            }
            case P3: {
                switch (repr) {
                    case P2: {
                        return p2(this.curve, this.X, this.Y, this.Z);
                    }
                    case P3: {
                        return p3(this.curve, this.X, this.Y, this.Z, this.T);
                    }
                    case CACHED: {
                        return cached(this.curve, this.Y.add(this.X), this.Y.subtract(this.X), this.Z, this.T.multiply(this.curve.get2D()));
                    }
                    default: {
                        throw new IllegalArgumentException();
                    }
                }
                break;
            }
            case P1P1: {
                switch (repr) {
                    case P2: {
                        return p2(this.curve, this.X.multiply(this.T), this.Y.multiply(this.Z), this.Z.multiply(this.T));
                    }
                    case P3: {
                        return p3(this.curve, this.X.multiply(this.T), this.Y.multiply(this.Z), this.Z.multiply(this.T), this.X.multiply(this.Y));
                    }
                    case P1P1: {
                        return p1p1(this.curve, this.X, this.Y, this.Z, this.T);
                    }
                    default: {
                        throw new IllegalArgumentException();
                    }
                }
                break;
            }
            case PRECOMP: {
                switch (repr) {
                    case PRECOMP: {
                        return precomp(this.curve, this.X, this.Y, this.Z);
                    }
                    default: {
                        throw new IllegalArgumentException();
                    }
                }
                break;
            }
            case CACHED: {
                switch (repr) {
                    case CACHED: {
                        return cached(this.curve, this.X, this.Y, this.Z, this.T);
                    }
                    default: {
                        throw new IllegalArgumentException();
                    }
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException();
            }
        }
    }
    
    public synchronized void precompute(final boolean precomputeSingle) {
        if (precomputeSingle && this.precmp == null) {
            this.precmp = new GroupElement[32][8];
            GroupElement Bi = this;
            for (int i = 0; i < 32; ++i) {
                GroupElement Bij = Bi;
                for (int j = 0; j < 8; ++j) {
                    final FieldElement recip = Bij.Z.invert();
                    final FieldElement x = Bij.X.multiply(recip);
                    final FieldElement y = Bij.Y.multiply(recip);
                    this.precmp[i][j] = precomp(this.curve, y.add(x), y.subtract(x), x.multiply(y).multiply(this.curve.get2D()));
                    Bij = Bij.add(Bi.toCached()).toP3();
                }
                for (int k = 0; k < 8; ++k) {
                    Bi = Bi.add(Bi.toCached()).toP3();
                }
            }
        }
        if (this.dblPrecmp != null) {
            return;
        }
        this.dblPrecmp = new GroupElement[8];
        GroupElement Bi = this;
        for (int i = 0; i < 8; ++i) {
            final FieldElement recip2 = Bi.Z.invert();
            final FieldElement x2 = Bi.X.multiply(recip2);
            final FieldElement y2 = Bi.Y.multiply(recip2);
            this.dblPrecmp[i] = precomp(this.curve, y2.add(x2), y2.subtract(x2), x2.multiply(y2).multiply(this.curve.get2D()));
            Bi = this.add(this.add(Bi.toCached()).toP3().toCached()).toP3();
        }
    }
    
    public GroupElement dbl() {
        switch (this.repr) {
            case P2:
            case P3: {
                final FieldElement XX = this.X.square();
                final FieldElement YY = this.Y.square();
                final FieldElement B = this.Z.squareAndDouble();
                final FieldElement A = this.X.add(this.Y);
                final FieldElement AA = A.square();
                final FieldElement Yn = YY.add(XX);
                final FieldElement Zn = YY.subtract(XX);
                return p1p1(this.curve, AA.subtract(Yn), Yn, Zn, B.subtract(Zn));
            }
            default: {
                throw new UnsupportedOperationException();
            }
        }
    }
    
    private GroupElement madd(final GroupElement q) {
        if (this.repr != Representation.P3) {
            throw new UnsupportedOperationException();
        }
        if (q.repr != Representation.PRECOMP) {
            throw new IllegalArgumentException();
        }
        final FieldElement YpX = this.Y.add(this.X);
        final FieldElement YmX = this.Y.subtract(this.X);
        final FieldElement A = YpX.multiply(q.X);
        final FieldElement B = YmX.multiply(q.Y);
        final FieldElement C = q.Z.multiply(this.T);
        final FieldElement D = this.Z.add(this.Z);
        return p1p1(this.curve, A.subtract(B), A.add(B), D.add(C), D.subtract(C));
    }
    
    private GroupElement msub(final GroupElement q) {
        if (this.repr != Representation.P3) {
            throw new UnsupportedOperationException();
        }
        if (q.repr != Representation.PRECOMP) {
            throw new IllegalArgumentException();
        }
        final FieldElement YpX = this.Y.add(this.X);
        final FieldElement YmX = this.Y.subtract(this.X);
        final FieldElement A = YpX.multiply(q.Y);
        final FieldElement B = YmX.multiply(q.X);
        final FieldElement C = q.Z.multiply(this.T);
        final FieldElement D = this.Z.add(this.Z);
        return p1p1(this.curve, A.subtract(B), A.add(B), D.subtract(C), D.add(C));
    }
    
    public GroupElement add(final GroupElement q) {
        if (this.repr != Representation.P3) {
            throw new UnsupportedOperationException();
        }
        if (q.repr != Representation.CACHED) {
            throw new IllegalArgumentException();
        }
        final FieldElement YpX = this.Y.add(this.X);
        final FieldElement YmX = this.Y.subtract(this.X);
        final FieldElement A = YpX.multiply(q.X);
        final FieldElement B = YmX.multiply(q.Y);
        final FieldElement C = q.T.multiply(this.T);
        final FieldElement ZZ = this.Z.multiply(q.Z);
        final FieldElement D = ZZ.add(ZZ);
        return p1p1(this.curve, A.subtract(B), A.add(B), D.add(C), D.subtract(C));
    }
    
    public GroupElement sub(final GroupElement q) {
        if (this.repr != Representation.P3) {
            throw new UnsupportedOperationException();
        }
        if (q.repr != Representation.CACHED) {
            throw new IllegalArgumentException();
        }
        final FieldElement YpX = this.Y.add(this.X);
        final FieldElement YmX = this.Y.subtract(this.X);
        final FieldElement A = YpX.multiply(q.Y);
        final FieldElement B = YmX.multiply(q.X);
        final FieldElement C = q.T.multiply(this.T);
        final FieldElement ZZ = this.Z.multiply(q.Z);
        final FieldElement D = ZZ.add(ZZ);
        return p1p1(this.curve, A.subtract(B), A.add(B), D.subtract(C), D.add(C));
    }
    
    public GroupElement negate() {
        if (this.repr != Representation.P3) {
            throw new UnsupportedOperationException();
        }
        return this.curve.getZero(Representation.P3).sub(this.toCached()).toP3();
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.toByteArray());
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof GroupElement)) {
            return false;
        }
        GroupElement ge = (GroupElement)obj;
        if (!this.repr.equals(ge.repr)) {
            try {
                ge = ge.toRep(this.repr);
            }
            catch (RuntimeException e) {
                return false;
            }
        }
        switch (this.repr) {
            case P2:
            case P3: {
                if (this.Z.equals(ge.Z)) {
                    return this.X.equals(ge.X) && this.Y.equals(ge.Y);
                }
                final FieldElement x1 = this.X.multiply(ge.Z);
                final FieldElement y1 = this.Y.multiply(ge.Z);
                final FieldElement x2 = ge.X.multiply(this.Z);
                final FieldElement y2 = ge.Y.multiply(this.Z);
                return x1.equals(x2) && y1.equals(y2);
            }
            case P1P1: {
                return this.toP2().equals(ge);
            }
            case PRECOMP: {
                return this.X.equals(ge.X) && this.Y.equals(ge.Y) && this.Z.equals(ge.Z);
            }
            case CACHED: {
                if (this.Z.equals(ge.Z)) {
                    return this.X.equals(ge.X) && this.Y.equals(ge.Y) && this.T.equals(ge.T);
                }
                final FieldElement x3 = this.X.multiply(ge.Z);
                final FieldElement y3 = this.Y.multiply(ge.Z);
                final FieldElement t3 = this.T.multiply(ge.Z);
                final FieldElement x4 = ge.X.multiply(this.Z);
                final FieldElement y4 = ge.Y.multiply(this.Z);
                final FieldElement t4 = ge.T.multiply(this.Z);
                return x3.equals(x4) && y3.equals(y4) && t3.equals(t4);
            }
            default: {
                return false;
            }
        }
    }
    
    GroupElement cmov(final GroupElement u, final int b) {
        return precomp(this.curve, this.X.cmov(u.X, b), this.Y.cmov(u.Y, b), this.Z.cmov(u.Z, b));
    }
    
    GroupElement select(final int pos, final int b) {
        final int bnegative = Utils.negative(b);
        final int babs = b - ((-bnegative & b) << 1);
        final GroupElement t = this.curve.getZero(Representation.PRECOMP).cmov(this.precmp[pos][0], Utils.equal(babs, 1)).cmov(this.precmp[pos][1], Utils.equal(babs, 2)).cmov(this.precmp[pos][2], Utils.equal(babs, 3)).cmov(this.precmp[pos][3], Utils.equal(babs, 4)).cmov(this.precmp[pos][4], Utils.equal(babs, 5)).cmov(this.precmp[pos][5], Utils.equal(babs, 6)).cmov(this.precmp[pos][6], Utils.equal(babs, 7)).cmov(this.precmp[pos][7], Utils.equal(babs, 8));
        final GroupElement tminus = precomp(this.curve, t.Y, t.X, t.Z.negate());
        return t.cmov(tminus, bnegative);
    }
    
    public GroupElement scalarMultiply(final byte[] a) {
        final byte[] e = toRadix16(a);
        GroupElement h = this.curve.getZero(Representation.P3);
        synchronized (this) {
            for (int i = 1; i < 64; i += 2) {
                final GroupElement t = this.select(i / 2, e[i]);
                h = h.madd(t).toP3();
            }
            h = h.dbl().toP2().dbl().toP2().dbl().toP2().dbl().toP3();
            for (int i = 0; i < 64; i += 2) {
                final GroupElement t = this.select(i / 2, e[i]);
                h = h.madd(t).toP3();
            }
        }
        return h;
    }
    
    public GroupElement doubleScalarMultiplyVariableTime(final GroupElement A, final byte[] a, final byte[] b) {
        final byte[] aslide = slide(a);
        final byte[] bslide = slide(b);
        GroupElement r = this.curve.getZero(Representation.P2);
        int i;
        for (i = 255; i >= 0 && aslide[i] == 0 && bslide[i] == 0; --i) {}
        synchronized (this) {
            while (i >= 0) {
                GroupElement t = r.dbl();
                if (aslide[i] > 0) {
                    t = t.toP3().madd(A.dblPrecmp[aslide[i] / 2]);
                }
                else if (aslide[i] < 0) {
                    t = t.toP3().msub(A.dblPrecmp[-aslide[i] / 2]);
                }
                if (bslide[i] > 0) {
                    t = t.toP3().madd(this.dblPrecmp[bslide[i] / 2]);
                }
                else if (bslide[i] < 0) {
                    t = t.toP3().msub(this.dblPrecmp[-bslide[i] / 2]);
                }
                r = t.toP2();
                --i;
            }
        }
        return r;
    }
    
    public boolean isOnCurve() {
        return this.isOnCurve(this.curve);
    }
    
    public boolean isOnCurve(final Curve curve) {
        switch (this.repr) {
            case P2:
            case P3: {
                final FieldElement recip = this.Z.invert();
                final FieldElement x = this.X.multiply(recip);
                final FieldElement y = this.Y.multiply(recip);
                final FieldElement xx = x.square();
                final FieldElement yy = y.square();
                final FieldElement dxxyy = curve.getD().multiply(xx).multiply(yy);
                return curve.getField().ONE.add(dxxyy).add(xx).equals(yy);
            }
            default: {
                return this.toP2().isOnCurve(curve);
            }
        }
    }
    
    @Override
    public String toString() {
        return "[GroupElement\nX=" + this.X + "\nY=" + this.Y + "\nZ=" + this.Z + "\nT=" + this.T + "\n]";
    }
    
    public enum Representation
    {
        P2, 
        P3, 
        P1P1, 
        PRECOMP, 
        CACHED;
    }
}
