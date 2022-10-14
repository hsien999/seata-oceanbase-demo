// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read;

import java.io.IOException;
import com.oceanbase.jdbc.internal.util.Utils;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;

public class ReadInitialHandShakePacket
{
    private static final String MARIADB_RPL_HACK_PREFIX = "5.5.5-";
    private final byte protocolVersion;
    private final long serverThreadId;
    private final long serverCapabilities;
    private final byte serverLanguage;
    private final short serverStatus;
    private final byte[] seed;
    private String serverVersion;
    private String authenticationPluginType;
    private boolean serverMariaDb;
    
    public ReadInitialHandShakePacket(final PacketInputStream reader) throws IOException, SQLException {
        this.authenticationPluginType = "";
        final Buffer buffer = reader.getPacket(true);
        if (buffer.getByteAt(0) == -1) {
            final ErrorPacket errorPacket = new ErrorPacket(buffer);
            throw new SQLException(errorPacket.getMessage());
        }
        this.protocolVersion = buffer.readByte();
        this.serverVersion = buffer.readStringNullEnd(StandardCharsets.US_ASCII);
        this.serverThreadId = buffer.readLongV1();
        final byte[] seed1 = buffer.readRawBytes(8);
        buffer.skipByte();
        final int serverCapabilities2FirstBytes = buffer.readShort() & 0xFFFF;
        this.serverLanguage = buffer.readByte();
        this.serverStatus = buffer.readShort();
        final int serverCapabilities4FirstBytes = serverCapabilities2FirstBytes + (buffer.readShort() << 16);
        int saltLength = 0;
        if ((serverCapabilities4FirstBytes & 0x80000) != 0x0) {
            saltLength = Math.max(12, buffer.readByte() - 9);
        }
        else {
            buffer.skipByte();
        }
        buffer.skipBytes(6);
        final long mariaDbAdditionalCapacities = buffer.readInt();
        if ((serverCapabilities4FirstBytes & 0x8000) != 0x0) {
            byte[] seed2;
            if (saltLength > 0) {
                seed2 = buffer.readRawBytes(saltLength);
            }
            else {
                seed2 = buffer.readBytesNullEnd();
            }
            System.arraycopy(seed2, 0, this.seed = Utils.copyWithLength(seed1, seed1.length + seed2.length), seed1.length, seed2.length);
        }
        else {
            this.seed = Utils.copyWithLength(seed1, seed1.length);
        }
        buffer.skipByte();
        if (this.serverVersion.startsWith("5.5.5-")) {
            this.serverMariaDb = true;
            this.serverVersion = this.serverVersion.substring("5.5.5-".length());
        }
        else {
            this.serverMariaDb = this.serverVersion.contains("MariaDB");
        }
        if ((serverCapabilities4FirstBytes & 0x1) == 0x0) {
            this.serverCapabilities = ((long)serverCapabilities4FirstBytes & 0xFFFFFFFFL) + (mariaDbAdditionalCapacities << 32);
            this.serverMariaDb = true;
        }
        else {
            this.serverCapabilities = ((long)serverCapabilities4FirstBytes & 0xFFFFFFFFL);
        }
        if ((serverCapabilities4FirstBytes & 0x80000) != 0x0) {
            this.authenticationPluginType = buffer.readStringNullEnd(StandardCharsets.US_ASCII);
        }
    }
    
    @Override
    public String toString() {
        return this.protocolVersion + ":" + this.serverVersion + ":" + this.serverThreadId + ":" + new String(this.seed) + ":" + this.serverCapabilities + ":" + this.serverLanguage + ":" + this.serverStatus;
    }
    
    public String getServerVersion() {
        return this.serverVersion;
    }
    
    public byte getProtocolVersion() {
        return this.protocolVersion;
    }
    
    public long getServerThreadId() {
        return this.serverThreadId;
    }
    
    public byte[] getSeed() {
        return this.seed;
    }
    
    public long getServerCapabilities() {
        return this.serverCapabilities;
    }
    
    public byte getServerLanguage() {
        return this.serverLanguage;
    }
    
    public short getServerStatus() {
        return this.serverStatus;
    }
    
    public String getAuthenticationPluginType() {
        return this.authenticationPluginType;
    }
    
    public boolean isServerMariaDb() {
        return this.serverMariaDb;
    }
}
