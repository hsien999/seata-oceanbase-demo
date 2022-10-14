// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.authentication.gssapi;

import java.security.PrivilegedExceptionAction;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import org.ietf.jgss.GSSContext;
import javax.security.auth.login.LoginException;
import java.security.PrivilegedActionException;
import javax.security.auth.Subject;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.Oid;
import javax.security.auth.login.LoginContext;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class StandardGssapiAuthentication implements GssapiAuth
{
    @Override
    public void authenticate(final PacketOutputStream out, final PacketInputStream in, final AtomicInteger sequence, final String servicePrincipalName, final String mechanisms) throws SQLException, IOException {
        if ("".equals(servicePrincipalName)) {
            throw new SQLException("No principal name defined on server. Please set server variable \"gssapi-principal-name\" or set option \"servicePrincipalName\"", "28000");
        }
        if (System.getProperty("java.security.auth.login.config") == null) {
            File jaasConfFile;
            try {
                jaasConfFile = File.createTempFile("jaas.conf", null);
                try (final PrintStream bos = new PrintStream(new FileOutputStream(jaasConfFile))) {
                    bos.print("Krb5ConnectorContext {\ncom.sun.security.auth.module.Krb5LoginModule required useTicketCache=true debug=true renewTGT=true doNotPrompt=true; };");
                }
                jaasConfFile.deleteOnExit();
            }
            catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            System.setProperty("java.security.auth.login.config", jaasConfFile.getCanonicalPath());
        }
        try {
            final LoginContext loginContext = new LoginContext("Krb5ConnectorContext");
            loginContext.login();
            final Subject mySubject = loginContext.getSubject();
            if (!mySubject.getPrincipals().isEmpty()) {
                try {
                    Oid krb5Mechanism;
                    GSSManager manager;
                    GSSName peerName;
                    GSSContext context;
                    byte[] inToken;
                    byte[] outToken;
                    Buffer buffer;
                    final PrivilegedExceptionAction<Void> action = (PrivilegedExceptionAction<Void>)(() -> {
                        try {
                            krb5Mechanism = new Oid("1.2.840.113554.1.2.2");
                            manager = GSSManager.getInstance();
                            peerName = manager.createName(servicePrincipalName, GSSName.NT_USER_NAME);
                            context = manager.createContext(peerName, krb5Mechanism, null, 0);
                            context.requestMutualAuth(true);
                            inToken = new byte[0];
                            while (!context.isEstablished()) {
                                outToken = context.initSecContext(inToken, 0, inToken.length);
                                if (outToken != null) {
                                    out.startPacket(sequence.incrementAndGet());
                                    out.write(outToken);
                                    out.flush();
                                }
                                if (!context.isEstablished()) {
                                    buffer = in.getPacket(true);
                                    sequence.set(in.getLastPacketSeq());
                                    inToken = buffer.readRawBytes(buffer.remaining());
                                }
                            }
                        }
                        catch (GSSException le) {
                            throw new SQLException("GSS-API authentication exception", "28000", 1045, le);
                        }
                        return null;
                    });
                    Subject.doAs(mySubject, action);
                    return;
                }
                catch (PrivilegedActionException exception) {
                    throw new SQLException("GSS-API authentication exception", "28000", 1045, exception);
                }
                throw new SQLException("GSS-API authentication exception : no credential cache not found.", "28000", 1045);
            }
            throw new SQLException("GSS-API authentication exception : no credential cache not found.", "28000", 1045);
        }
        catch (LoginException le2) {
            throw new SQLException("GSS-API authentication exception", "28000", 1045, le2);
        }
    }
}
