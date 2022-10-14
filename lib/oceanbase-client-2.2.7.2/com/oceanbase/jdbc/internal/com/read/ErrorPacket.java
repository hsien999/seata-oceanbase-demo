// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read;

import java.nio.charset.StandardCharsets;

public class ErrorPacket
{
    private final short errorCode;
    private final String sqlState;
    private final String message;
    
    public ErrorPacket(final Buffer buffer) {
        buffer.skipByte();
        this.errorCode = buffer.readShort();
        if (buffer.getByte() == 35) {
            buffer.skipByte();
            this.sqlState = buffer.readString(5);
            this.message = buffer.readStringNullEnd(StandardCharsets.UTF_8);
        }
        else {
            this.message = new String(buffer.buf, buffer.position, buffer.limit - buffer.position, StandardCharsets.UTF_8);
            this.sqlState = "HY000";
        }
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public short getErrorCode() {
        return this.errorCode;
    }
    
    public String getSqlState() {
        return this.sqlState;
    }
}
