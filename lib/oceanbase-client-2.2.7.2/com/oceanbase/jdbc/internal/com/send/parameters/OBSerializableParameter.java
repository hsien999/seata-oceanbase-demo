// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.Utils;

public class OBSerializableParameter extends SerializableParameter
{
    public OBSerializableParameter(final Object object, final boolean noBackslashEscapes) throws SQLException {
        super(object, noBackslashEscapes);
        try {
            if (this.loadedStream == null) {
                this.writeObjectToBytes();
            }
            this.loadedStream = Utils.toHexString(this.loadedStream).getBytes();
        }
        catch (IOException e) {
            throw new SQLException("IOException occur:" + e.getMessage());
        }
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        pos.write(39);
        pos.writeBytesEscaped(this.loadedStream, this.loadedStream.length, this.noBackSlashEscapes);
        pos.write(39);
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        pos.writeFieldLength(this.loadedStream.length);
        pos.write(this.loadedStream);
    }
}
