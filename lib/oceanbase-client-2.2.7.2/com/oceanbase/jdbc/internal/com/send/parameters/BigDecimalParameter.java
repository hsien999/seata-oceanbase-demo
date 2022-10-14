// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.math.BigDecimal;

public class BigDecimalParameter implements Cloneable, ParameterHolder
{
    private final BigDecimal bigDecimal;
    
    public BigDecimalParameter(final BigDecimal bigDecimal) {
        this.bigDecimal = bigDecimal;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        pos.write(this.bigDecimal.toPlainString().getBytes());
    }
    
    @Override
    public int getApproximateTextProtocolLength() {
        return this.bigDecimal.toPlainString().getBytes().length;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        final String value = this.bigDecimal.toPlainString();
        pos.writeFieldLength(value.length());
        pos.write(value);
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.DECIMAL;
    }
    
    @Override
    public String toString() {
        return this.bigDecimal.toString();
    }
    
    @Override
    public boolean isNullData() {
        return false;
    }
    
    @Override
    public boolean isLongData() {
        return false;
    }
}
