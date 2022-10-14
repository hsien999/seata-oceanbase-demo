// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send;

import com.oceanbase.jdbc.internal.util.pid.PidFactory;
import java.util.StringTokenizer;
import com.oceanbase.jdbc.internal.util.constant.Version;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.util.Locale;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import com.oceanbase.jdbc.internal.util.Utils;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.credential.Credential;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.util.function.Supplier;

public class SendHandshakeResponsePacket
{
    private static final Supplier<String> pidRequest;
    private static final String _CLIENT_NAME = "__client_name";
    private static final String _CLIENT_VERSION = "__client_version";
    private static final String _SERVER_HOST = "__server_host";
    private static final String _CLIENT_IP = "__client_ip";
    private static final String _OS = "__os";
    private static final String _PID = "__pid";
    private static final String _THREAD = "__thread";
    private static final String _JAVA_VENDOR = "__java_vendor";
    private static final String _JAVA_VERSION = "__java_version";
    private static final String OB_PROXY_PARTITION_HIT = "ob_proxy_partition_hit";
    private static final String OB_STATEMENT_TRACE_ID = "ob_statement_trace_id";
    private static final String OB_CAPABILITY_FLAG = "ob_capability_flag";
    private static final String OB_CLIENT_FEEDBACK = "ob_client_feedback";
    private static final long OB_CAP_VIRTUAL_COMPRESS = 8L;
    private static final long OB_CAP_USE_OCEANBASE_2_0_PROTOCOL = 256L;
    private static final long OB_CAP_VIRTUAL_COMPRESS_SWITCH = 64L;
    private static final long OB_CAP_OCJ_ENABLE_EXTRA_OK_PACKET = 128L;
    private static final long OB_CAP_ABUNDANT_FEEDBACK = 1024L;
    private static final long OB_CAPABILITY_FLAG_VALUE = 1480L;
    
    public static void send(final PacketOutputStream pos, final Credential credential, final String host, final String database, final long clientCapabilities, final long serverCapabilities, final byte serverLanguage, final byte packetSeq, final Options options, String authenticationPluginType, final byte[] seed, final String clientIp) throws IOException {
        pos.startPacket(packetSeq);
        final String s = authenticationPluginType;
        byte[] authData = null;
        switch (s) {
            case "mysql_clear_password": {
                pos.permitTrace(false);
                if (credential.getPassword() == null) {
                    authData = new byte[0];
                    break;
                }
                if (options.passwordCharacterEncoding != null && !options.passwordCharacterEncoding.isEmpty()) {
                    authData = credential.getPassword().getBytes(options.passwordCharacterEncoding);
                    break;
                }
                authData = credential.getPassword().getBytes();
                break;
            }
            default: {
                authenticationPluginType = "mysql_native_password";
                pos.permitTrace(false);
                try {
                    authData = Utils.encryptPassword(credential.getPassword(), seed, options.passwordCharacterEncoding);
                }
                catch (NoSuchAlgorithmException e) {
                    throw new IOException("Unknown algorithm SHA-1. Cannot encrypt password", e);
                }
                break;
            }
        }
        pos.writeInt((int)clientCapabilities);
        pos.writeInt(1073741824);
        pos.write(serverLanguage);
        pos.writeBytes((byte)0, 19);
        pos.writeInt((int)(clientCapabilities >> 32));
        if (credential.getUser() == null || credential.getUser().isEmpty()) {
            pos.write(System.getProperty("user.name").getBytes());
        }
        else {
            pos.write(credential.getUser().getBytes());
        }
        pos.write(0);
        if ((serverCapabilities & 0x200000L) != 0x0L) {
            pos.writeFieldLength(authData.length);
            pos.write(authData);
        }
        else if ((serverCapabilities & 0x8000L) != 0x0L) {
            pos.write((byte)authData.length);
            pos.write(authData);
        }
        else {
            pos.write(authData);
            pos.write(0);
        }
        if ((clientCapabilities & 0x8L) != 0x0L) {
            pos.write(database);
            pos.write(0);
        }
        if ((serverCapabilities & 0x80000L) != 0x0L) {
            pos.write(authenticationPluginType);
            pos.write(0);
        }
        if ((serverCapabilities & 0x100000L) != 0x0L) {
            writeConnectAttributes(pos, options.connectionAttributes, host, options, clientIp);
        }
        pos.flush();
        pos.permitTrace(true);
    }
    
    public static void sendChangeUser(final PacketOutputStream pos, final Credential credential, final String host, final String database, final long clientCapabilities, final long serverCapabilities, final byte serverLanguage, final byte packetSeq, final Options options, String authenticationPluginType, final byte[] seed, final String clientIp, final boolean isOracleMode) throws IOException {
        pos.startPacket(packetSeq);
        final String s = authenticationPluginType;
        byte[] authData = null;
        switch (s) {
            case "mysql_clear_password": {
                pos.permitTrace(false);
                if (credential.getPassword() == null) {
                    authData = new byte[0];
                    break;
                }
                if (options.passwordCharacterEncoding != null && !options.passwordCharacterEncoding.isEmpty()) {
                    authData = credential.getPassword().getBytes(options.passwordCharacterEncoding);
                    break;
                }
                authData = credential.getPassword().getBytes();
                break;
            }
            default: {
                authenticationPluginType = "mysql_native_password";
                pos.permitTrace(false);
                try {
                    authData = Utils.encryptPassword(credential.getPassword(), seed, options.passwordCharacterEncoding);
                }
                catch (NoSuchAlgorithmException e) {
                    throw new IOException("Unknown algorithm SHA-1. Cannot encrypt password", e);
                }
                break;
            }
        }
        pos.startPacket(0);
        pos.write(17);
        String user;
        if (credential.getUser() == null || credential.getUser().isEmpty()) {
            user = System.getProperty("user.name");
            pos.write(System.getProperty("user.name").getBytes());
        }
        else {
            user = credential.getUser();
            pos.write(credential.getUser().getBytes());
        }
        pos.write(0);
        if ((serverCapabilities & 0x200000L) != 0x0L) {
            pos.writeFieldLength(authData.length);
            pos.write(authData);
        }
        else if ((serverCapabilities & 0x8000L) != 0x0L) {
            pos.write((byte)authData.length);
            pos.write(authData);
        }
        else {
            pos.write(authData);
            pos.write(0);
        }
        if (isOracleMode) {
            if ((clientCapabilities & 0x8L) != 0x0L) {
                pos.write(database.toUpperCase(Locale.ROOT));
            }
            else {
                final int index = user.indexOf(64);
                final String databaseTmp = user.substring(0, index);
                pos.write(databaseTmp);
            }
        }
        else if ((clientCapabilities & 0x8L) != 0x0L) {
            pos.write(database);
        }
        pos.write(0);
        if ((serverCapabilities & 0x80000L) != 0x0L) {
            pos.write(authenticationPluginType);
            pos.write(0);
        }
        if ((serverCapabilities & 0x100000L) != 0x0L) {
            writeConnectAttributes(pos, options.connectionAttributes, host, options, clientIp);
        }
        pos.flush();
        pos.permitTrace(true);
    }
    
    private static void writeConnectAttributes(final PacketOutputStream pos, final String connectionAttributes, final String host, final Options options, final String clientIp) throws IOException {
        final Buffer buffer = new Buffer(new byte[200]);
        buffer.writeStringSmallLength("__client_name".getBytes(pos.getCharset()));
        buffer.writeStringLength("OceanBase Connector/J", pos.getCharset());
        buffer.writeStringSmallLength("__client_version".getBytes(pos.getCharset()));
        buffer.writeStringLength(Version.version, pos.getCharset());
        buffer.writeStringSmallLength("__server_host".getBytes(pos.getCharset()));
        buffer.writeStringLength((host != null) ? host : "", pos.getCharset());
        buffer.writeStringSmallLength("__client_ip".getBytes(pos.getCharset()));
        buffer.writeStringLength((clientIp != null) ? clientIp : "", pos.getCharset());
        buffer.writeStringSmallLength("__os".getBytes(pos.getCharset()));
        buffer.writeStringLength(System.getProperty("os.name"), pos.getCharset());
        final String pid = SendHandshakeResponsePacket.pidRequest.get();
        if (pid != null) {
            buffer.writeStringSmallLength("__pid".getBytes(pos.getCharset()));
            buffer.writeStringLength(pid, pos.getCharset());
        }
        buffer.writeStringSmallLength("__thread".getBytes(pos.getCharset()));
        buffer.writeStringLength(Long.toString(Thread.currentThread().getId()), pos.getCharset());
        buffer.writeStringLength("__java_vendor".getBytes(pos.getCharset()));
        buffer.writeStringLength(System.getProperty("java.vendor"), pos.getCharset());
        buffer.writeStringSmallLength("__java_version".getBytes(pos.getCharset()));
        buffer.writeStringLength(System.getProperty("java.version"), pos.getCharset());
        if (connectionAttributes != null) {
            final StringTokenizer tokenizer = new StringTokenizer(connectionAttributes, ",");
            while (tokenizer.hasMoreTokens()) {
                final String token = tokenizer.nextToken();
                final int separator = token.indexOf(":");
                if (separator != -1) {
                    buffer.writeStringLength(token.substring(0, separator), pos.getCharset());
                    buffer.writeStringLength(token.substring(separator + 1), pos.getCharset());
                }
                else {
                    buffer.writeStringLength(token, pos.getCharset());
                    buffer.writeStringLength("", pos.getCharset());
                }
            }
        }
        long capFlag = 1480L;
        if (!options.useObChecksum) {
            capFlag &= 0xFFFFFFFFFFFFFFF7L;
            capFlag &= 0xFFFFFFFFFFFFFFBFL;
        }
        if (!options.useOceanBaseProtocolV20) {
            capFlag &= 0xFFFFFFFFFFFFFEFFL;
        }
        buffer.writeStringSmallLength("__proxy_capability_flag".getBytes(pos.getCharset()));
        buffer.writeStringLength(String.valueOf(capFlag), pos.getCharset());
        pos.writeFieldLength(buffer.position);
        pos.write(buffer.buf, 0, buffer.position);
    }
    
    static {
        pidRequest = PidFactory.getInstance();
    }
}
