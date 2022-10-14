// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class OBEmptyLobParameter implements Cloneable, ParameterHolder
{
    int lobType;
    private static final byte[] EMPTY_CLOB;
    private static final byte[] EMPTY_BLOB;
    
    public OBEmptyLobParameter(final int lobType) {
        this.lobType = 0;
        this.lobType = lobType;
    }
    
    @Override
    public void writeTo(final PacketOutputStream os) throws IOException {
        if (this.lobType == 0) {
            os.write(OBEmptyLobParameter.EMPTY_BLOB);
        }
        else {
            os.write(OBEmptyLobParameter.EMPTY_CLOB);
        }
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
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
        return ColumnType.STRING;
    }
    
    @Override
    public boolean isLongData() {
        return false;
    }
    
    static {
        EMPTY_CLOB = new byte[] { 69, 77, 80, 84, 89, 95, 67, 76, 79, 66, 40, 41 };
        EMPTY_BLOB = new byte[] { 69, 77, 80, 84, 89, 95, 66, 76, 79, 66, 40, 41 };
    }
}
