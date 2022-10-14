// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import com.oceanbase.jdbc.internal.ColumnType;
import java.io.OutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class SerializableParameter implements Cloneable, ParameterHolder
{
    protected final boolean noBackSlashEscapes;
    protected Object object;
    protected byte[] loadedStream;
    
    public SerializableParameter(final Object object, final boolean noBackslashEscapes) {
        this.loadedStream = null;
        this.object = object;
        this.noBackSlashEscapes = noBackslashEscapes;
    }
    
    @Override
    public void writeTo(final PacketOutputStream pos) throws IOException {
        if (this.loadedStream == null) {
            this.writeObjectToBytes();
        }
        pos.write(SerializableParameter.BINARY_INTRODUCER);
        pos.writeBytesEscaped(this.loadedStream, this.loadedStream.length, this.noBackSlashEscapes);
        pos.write(39);
    }
    
    protected void writeObjectToBytes() throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(this.object);
        this.loadedStream = baos.toByteArray();
        this.object = null;
    }
    
    @Override
    public int getApproximateTextProtocolLength() throws IOException {
        this.writeObjectToBytes();
        return this.loadedStream.length;
    }
    
    @Override
    public void writeBinary(final PacketOutputStream pos) throws IOException {
        if (this.loadedStream == null) {
            this.writeObjectToBytes();
        }
        pos.writeFieldLength(this.loadedStream.length);
        pos.write(this.loadedStream);
    }
    
    @Override
    public String toString() {
        return "<Serializable>";
    }
    
    @Override
    public ColumnType getColumnType() {
        return ColumnType.BLOB;
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
