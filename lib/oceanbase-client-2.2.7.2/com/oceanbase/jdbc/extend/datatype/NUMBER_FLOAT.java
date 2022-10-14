// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

import java.sql.SQLException;
import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.math.BigDecimal;

public class NUMBER_FLOAT extends Datum
{
    private final float value;
    private BigDecimal bigDecimal;
    
    public NUMBER_FLOAT(final float value) {
        this.bigDecimal = null;
        this.value = value;
    }
    
    public NUMBER_FLOAT(final float value, final byte[] bytes) {
        super(bytes);
        this.bigDecimal = null;
        this.value = value;
        if (bytes != null) {
            this.bigDecimal = new BigDecimal(new String(bytes));
        }
    }
    
    public void writeTo(final PacketOutputStream pos) throws IOException {
        pos.write(String.valueOf(this.value).getBytes(StandardCharsets.UTF_8));
    }
    
    public int getApproximateTextProtocolLength() {
        return String.valueOf(this.value).getBytes().length;
    }
    
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        pos.writeLong(Double.doubleToLongBits(this.value));
    }
    
    public ColumnType getColumnType() {
        return ColumnType.DOUBLE;
    }
    
    @Override
    public String toString() {
        return Double.toString(this.value);
    }
    
    public boolean isNullData() {
        return false;
    }
    
    public boolean isLongData() {
        return false;
    }
    
    @Override
    public boolean isConvertibleTo(final Class var1) {
        final String var2 = var1.getName();
        return var2.compareTo("java.lang.Float") == 0 || var2.compareTo("java.lang.Double") == 0;
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
}
