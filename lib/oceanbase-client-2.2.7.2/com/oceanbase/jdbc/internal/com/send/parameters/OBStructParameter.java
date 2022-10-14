// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import com.oceanbase.jdbc.extend.datatype.ComplexUtil;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.ObStruct;

public class OBStructParameter implements Cloneable, ParameterHolder
{
    ObStruct structValue;
    Options options;
    
    public void storeStructTypeInfo(final PacketOutputStream packet) throws IOException {
        final ObStruct struct = this.structValue;
        byte[] tmp = struct.getComplexType().getSchemaName().getBytes();
        packet.writeFieldLength(tmp.length);
        packet.write(tmp);
        tmp = struct.getComplexType().getTypeName().getBytes();
        packet.writeFieldLength(tmp.length);
        packet.write(tmp);
        packet.writeFieldLength(struct.getComplexType().getVersion());
    }
    
    public OBStructParameter(final ObStruct structImpl, final Options options) {
        this.structValue = structImpl;
        this.options = options;
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        try {
            final int nullCount = (this.structValue.getAttrCount() + 7) / 8;
            final int nullBitsPosition = pos.getPosition();
            for (int i = 0; i < nullCount; ++i) {
                pos.writeBytes((byte)0, 1);
            }
            final byte[] nullBitsBuffer = new byte[nullCount];
            for (int j = 0; j < this.structValue.getAttrCount(); ++j) {
                if (null != this.structValue.getAttrData(j)) {
                    ComplexUtil.storeComplexAttrData(pos, this.structValue.getComplexType().getAttrType(j), this.structValue.getAttrData(j), this.options);
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
