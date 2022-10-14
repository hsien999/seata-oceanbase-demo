// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol.tls;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import javax.net.ssl.SSLException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLSocketFactory;
import com.oceanbase.jdbc.util.Options;
import java.sql.SQLException;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.security.GeneralSecurityException;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import java.security.KeyStore;
import java.io.IOException;
import java.io.FileInputStream;
import java.net.URL;
import javax.net.ssl.KeyManager;
import com.oceanbase.jdbc.internal.logging.Logger;
import com.oceanbase.jdbc.tls.TlsSocketPlugin;

public class DefaultTlsSocketPlugin implements TlsSocketPlugin
{
    private static final Logger logger;
    
    private static KeyManager loadClientCerts(final String keyStoreUrl, final String keyStorePassword, final String keyPassword, final String storeType) throws SQLException {
        InputStream inStream = null;
        try {
            final char[] keyStorePasswordChars = (char[])((keyStorePassword == null) ? null : keyStorePassword.toCharArray());
            try {
                inStream = new URL(keyStoreUrl).openStream();
            }
            catch (IOException ioexception) {
                inStream = new FileInputStream(keyStoreUrl);
            }
            final KeyStore ks = KeyStore.getInstance((storeType != null) ? storeType : KeyStore.getDefaultType());
            ks.load(inStream, keyStorePasswordChars);
            final char[] keyStoreChars = (keyPassword == null) ? keyStorePasswordChars : keyPassword.toCharArray();
            return new OceanBaseX509KeyManager(ks, keyStoreChars);
        }
        catch (GeneralSecurityException generalSecurityEx) {
            throw ExceptionFactory.INSTANCE.create("Failed to create keyStore instance", "08000", generalSecurityEx);
        }
        catch (FileNotFoundException fileNotFoundEx) {
            throw ExceptionFactory.INSTANCE.create("Failed to find keyStore file. Option keyStore=" + keyStoreUrl, "08000", fileNotFoundEx);
        }
        catch (IOException ioEx) {
            throw ExceptionFactory.INSTANCE.create("Failed to read keyStore file. Option keyStore=" + keyStoreUrl, "08000", ioEx);
        }
        finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            }
            catch (IOException ex) {}
        }
    }
    
    @Override
    public String name() {
        return "Default TLS socket factory";
    }
    
    @Override
    public String type() {
        return "DEFAULT";
    }
    
    @Override
    public SSLSocketFactory getSocketFactory(final Options options) throws SQLException {
        TrustManager[] trustManager = null;
        KeyManager[] keyManager = null;
        if (options.trustServerCertificate || options.serverSslCert != null || options.trustStore != null) {
            trustManager = new X509TrustManager[] { new OceanBaseX509TrustManager(options) };
        }
        if (options.keyStore != null) {
            keyManager = new KeyManager[] { loadClientCerts(options.keyStore, options.keyStorePassword, options.keyPassword, options.keyStoreType) };
        }
        else {
            final String keyStore = System.getProperty("javax.net.ssl.trustStore");
            final String keyStorePassword = System.getProperty("javax.net.ssl.trustStorePassword");
            if (keyStore != null) {
                try {
                    keyManager = new KeyManager[] { loadClientCerts(keyStore, keyStorePassword, keyStorePassword, options.keyStoreType) };
                }
                catch (SQLException queryException) {
                    keyManager = null;
                    DefaultTlsSocketPlugin.logger.error("Error loading keymanager from system properties", queryException);
                }
            }
        }
        try {
            final SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManager, trustManager, null);
            return sslContext.getSocketFactory();
        }
        catch (KeyManagementException keyManagementEx) {
            throw ExceptionFactory.INSTANCE.create("Could not initialize SSL context", "08000", keyManagementEx);
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmEx) {
            throw ExceptionFactory.INSTANCE.create("SSLContext TLS Algorithm not unknown", "08000", noSuchAlgorithmEx);
        }
    }
    
    @Override
    public void verify(final String host, final SSLSession session, final Options options, final long serverThreadId) throws SSLException {
        final HostnameVerifierImpl hostnameVerifier = new HostnameVerifierImpl();
        if (!hostnameVerifier.verify(host, session, serverThreadId)) {
            final Certificate[] certs = session.getPeerCertificates();
            final X509Certificate cert = (X509Certificate)certs[0];
            hostnameVerifier.verify(host, cert, serverThreadId);
        }
    }
    
    static {
        logger = LoggerFactory.getLogger(DefaultTlsSocketPlugin.class);
    }
}
