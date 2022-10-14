// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public interface ParameterHolder
{
    public static final byte[] BINARY_INTRODUCER = { 95, 98, 105, 110, 97, 114, 121, 32, 39 };
    public static final byte QUOTE = 39;
    
    void writeTo(final PacketOutputStream p0) throws IOException;
    
    void writeBinary(final PacketOutputStream p0) throws IOException;
    
    int getApproximateTextProtocolLength() throws IOException;
    
    String toString();
    
    boolean isNullData();
    
    ColumnType getColumnType();
    
    boolean isLongData();
}
