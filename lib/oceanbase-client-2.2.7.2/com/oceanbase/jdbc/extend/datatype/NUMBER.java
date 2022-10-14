// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

import java.math.BigInteger;
import java.sql.SQLException;
import java.math.BigDecimal;

public class NUMBER extends Datum
{
    private BigDecimal bigDecimal;
    static final long serialVersionUID = -1656085588913430059L;
    
    public NUMBER() {
    }
    
    public NUMBER(final byte[] bytes) {
        super(bytes);
        if (bytes != null) {
            this.bigDecimal = new BigDecimal(new String(bytes));
        }
    }
    
    @Override
    public boolean isConvertibleTo(final Class var1) {
        final String var2 = var1.getName();
        return var2.compareTo("java.lang.Integer") == 0 || var2.compareTo("java.lang.Long") == 0 || var2.compareTo("java.lang.Float") == 0 || var2.compareTo("java.lang.Double") == 0 || var2.compareTo("java.math.BigInteger") == 0 || var2.compareTo("java.math.BigDecimal") == 0 || var2.compareTo("java.lang.String") == 0 || var2.compareTo("java.lang.Boolean") == 0 || var2.compareTo("java.lang.Byte") == 0 || var2.compareTo("java.lang.Short") == 0;
    }
    
    @Override
    public Object toJdbc() throws SQLException {
        return this.bigDecimal;
    }
    
    @Override
    public Object makeJdbcArray(final int var1) {
        final BigDecimal[] var2 = new BigDecimal[var1];
        return var2;
    }
    
    @Override
    public double doubleValue() {
        if (this.bigDecimal != null) {
            return this.bigDecimal.doubleValue();
        }
        return 0.0;
    }
    
    @Override
    public float floatValue() {
        if (this.bigDecimal != null) {
            return this.bigDecimal.floatValue();
        }
        return 0.0f;
    }
    
    @Override
    public long longValue() {
        if (this.bigDecimal != null) {
            return this.bigDecimal.longValue();
        }
        return 0L;
    }
    
    @Override
    public int intValue() {
        if (this.bigDecimal != null) {
            return this.bigDecimal.intValue();
        }
        return 0;
    }
    
    public short shortValue() {
        if (this.bigDecimal != null) {
            return this.bigDecimal.shortValue();
        }
        return 0;
    }
    
    @Override
    public byte byteValue() {
        if (this.bigDecimal != null) {
            return this.bigDecimal.byteValue();
        }
        return 0;
    }
    
    public BigInteger bigIntegerValue() {
        if (this.bigDecimal != null) {
            return this.bigDecimal.toBigInteger();
        }
        return null;
    }
    
    @Override
    public BigDecimal bigDecimalValue() throws SQLException {
        return this.bigDecimal;
    }
    
    @Override
    public String stringValue() {
        if (this.bigDecimal != null) {
            return this.bigDecimal.toString();
        }
        return null;
    }
    
    @Override
    public boolean booleanValue() {
        return this.bigDecimal != null && this.bigDecimal.intValue() == 0;
    }
    
    @Override
    public String toString() {
        if (this.bigDecimal != null) {
            return this.bigDecimal.toString();
        }
        return null;
    }
}
