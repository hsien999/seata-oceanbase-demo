// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import java.sql.SQLException;
import java.io.IOException;
import com.oceanbase.jdbc.internal.ColumnType;
import com.oceanbase.jdbc.extend.datatype.ComplexUtil;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.ObArray;

public class OBArrayParameter implements Cloneable, ParameterHolder
{
    ObArray arrayValue;
    Options options;
    
    public OBArrayParameter(final ObArray arrayImpl, final Options options) {
        this.arrayValue = arrayImpl;
        this.options = options;
    }
    
    public void storeArrayTypeInfo(final PacketOutputStream packet) throws IOException, SQLException {
        final ObArray array = this.arrayValue;
        packet.writeFieldLength(0L);
        packet.writeFieldLength(0L);
        packet.writeFieldLength(array.getComplexType().getVersion());
        final int elementType = ComplexUtil.getMysqlType(array.getBaseType()).getType();
        packet.writeBytes((byte)elementType, 1);
        if (elementType >= ColumnType.COMPLEX.getType() && elementType <= ColumnType.STRUCT.getType()) {
            byte[] tmp = array.getComplexType().getAttrType(0).getSchemaName().getBytes();
            packet.writeFieldLength(tmp.length);
            packet.write(tmp);
            tmp = array.getComplexType().getAttrType(0).getTypeName().getBytes();
            packet.writeFieldLength(tmp.length);
            packet.write(tmp);
            packet.writeFieldLength(tmp.length);
        }
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        try {
            pos.writeFieldLength(this.arrayValue.getAttrCount());
            final int nullCount = (this.arrayValue.getAttrCount() + 7) / 8;
            final int nullBitsPosition = pos.getPosition();
            for (int i = 0; i < nullCount; ++i) {
                pos.writeBytes((byte)0, 1);
            }
            final byte[] nullBitsBuffer = new byte[nullCount];
            for (int j = 0; j < this.arrayValue.getAttrCount(); ++j) {
                if (null != this.arrayValue.getAttrData(j)) {
                    ComplexUtil.storeComplexAttrData(pos, this.arrayValue.getComplexType().getAttrType(0), this.arrayValue.getAttrData(j), this.options);
                }
                else {
                    final byte[] array = nullBitsBuffer;
                    final int n = j / 8;
                    array[n] |= (byte)(1 << j % 8);
                }
            }
            final int endPosition = pos.getPosition();
            pos.setPosition(nullBitsPosition);
            pos.write(nullBitsBuffer);
            pos.setPosition(endPosition);
        }
        catch (Exception e) {
            throw new IOException("storeComplexAttrData exception");
        }
    }
    
    @Override
    public int getApproximateTextProtocolLength() throws IOException {
        return 0;
    }
    
    @Override
    public boolean isNullData() {
        return false;
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.COMPLEX;
    }
    
    @Override
    public boolean isLongData() {
        return false;
    }
}
