// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication;

import com.oceanbase.jdbc.internal.com.send.authentication.gssapi.StandardGssapiAuthentication;
import com.oceanbase.jdbc.internal.com.send.authentication.gssapi.GssUtility;
import java.sql.SQLException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.com.send.authentication.gssapi.GssapiAuth;
import com.oceanbase.jdbc.authentication.AuthenticationPlugin;

public class SendGssApiAuthPacket implements AuthenticationPlugin
{
    private static final GssapiAuth gssapiAuth;
    private byte[] seed;
    private String optionServicePrincipalName;
    
    @Override
    public String name() {
        return "GSSAPI client authentication";
    }
    
    @Override
    public String type() {
        return "auth_gssapi_client";
    }
    
    @Override
    public void initialize(final String authenticationData, final byte[] seed, final Options options) {
        this.seed = seed;
        this.optionServicePrincipalName = options.servicePrincipalName;
    }
    
    @Override
    public Buffer process(final PacketOutputStream out, final PacketInputStream in, final AtomicInteger sequence) throws IOException, SQLException {
        Buffer buffer = new Buffer(this.seed);
        final String serverSpn = buffer.readStringNullEnd(StandardCharsets.UTF_8);
        final String servicePrincipalName = (this.optionServicePrincipalName != null && !this.optionServicePrincipalName.isEmpty()) ? this.optionServicePrincipalName : serverSpn;
        String mechanisms = buffer.readStringNullEnd(StandardCharsets.UTF_8);
        if (mechanisms.isEmpty()) {
            mechanisms = "Kerberos";
        }
        SendGssApiAuthPacket.gssapiAuth.authenticate(out, in, sequence, servicePrincipalName, mechanisms);
        buffer = in.getPacket(true);
        sequence.set(in.getLastPacketSeq());
        return buffer;
    }
    
    static {
        GssapiAuth init;
        try {
            init = GssUtility.getAuthenticationMethod();
        }
        catch (Throwable t) {
            init = new StandardGssapiAuthentication();
        }
        gssapiAuth = init;
    }
}
