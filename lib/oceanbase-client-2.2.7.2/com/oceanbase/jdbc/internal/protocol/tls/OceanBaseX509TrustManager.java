// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol.tls;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.TrustManager;
import java.util.Iterator;
import java.util.Collection;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.TrustManagerFactory;
import java.util.UUID;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.SqlStates;
import java.security.KeyStore;
import com.oceanbase.jdbc.util.Options;
import javax.net.ssl.X509TrustManager;

public class OceanBaseX509TrustManager implements X509TrustManager
{
    private X509TrustManager trustManager;
    
    public OceanBaseX509TrustManager(final Options options) throws SQLException {
        if (options.trustServerCertificate) {
            return;
        }
        KeyStore ks;
        try {
            ks = KeyStore.getInstance((options.trustStoreType != null) ? options.trustStoreType : KeyStore.getDefaultType());
        }
        catch (GeneralSecurityException generalSecurityEx) {
            throw new SQLException("Failed to create keystore instance", SqlStates.CONNECTION_EXCEPTION.getSqlState(), generalSecurityEx);
        }
        InputStream inStream = null;
        try {
            Label_0458: {
                if (options.trustStore != null) {
                    try {
                        final String trustStore = options.trustStore;
                        try {
                            inStream = new URL(trustStore).openStream();
                        }
                        catch (IOException ioexception) {
                            inStream = new FileInputStream(trustStore);
                        }
                        ks.load(inStream, (char[])((options.trustStorePassword == null) ? null : options.trustStorePassword.toCharArray()));
                        break Label_0458;
                    }
                    catch (GeneralSecurityException generalSecurityEx2) {
                        throw new SQLException("Failed to create trustStore instance", SqlStates.CONNECTION_EXCEPTION.getSqlState(), generalSecurityEx2);
                    }
                    catch (FileNotFoundException fileNotFoundEx) {
                        throw new SQLException("Failed to find trustStore file. trustStore=" + options.trustStore, SqlStates.CONNECTION_EXCEPTION.getSqlState(), fileNotFoundEx);
                    }
                    catch (IOException ioEx) {
                        throw new SQLException("Failed to read trustStore file. trustStore=" + options.trustStore, SqlStates.CONNECTION_EXCEPTION.getSqlState(), ioEx);
                    }
                }
                if (options.serverSslCert.startsWith("-----BEGIN CERTIFICATE-----")) {
                    inStream = new ByteArrayInputStream(options.serverSslCert.getBytes());
                }
                else if (options.serverSslCert.startsWith("classpath:")) {
                    final String classpathFile = options.serverSslCert.substring("classpath:".length());
                    inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(classpathFile);
                }
                else {
                    try {
                        inStream = new FileInputStream(options.serverSslCert);
                    }
                    catch (FileNotFoundException fileNotFoundEx) {
                        throw new SQLException("Failed to find serverSslCert file. serverSslCert=" + options.serverSslCert, SqlStates.CONNECTION_EXCEPTION.getSqlState(), fileNotFoundEx);
                    }
                }
                try {
                    ks.load(null);
                    final CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    final Collection<? extends Certificate> caList = cf.generateCertificates(inStream);
                    for (final Certificate ca : caList) {
                        ks.setCertificateEntry(UUID.randomUUID().toString(), ca);
                    }
                }
                catch (IOException ioEx) {
                    throw new SQLException("Failed load keyStore", SqlStates.CONNECTION_EXCEPTION.getSqlState(), ioEx);
                }
                catch (GeneralSecurityException generalSecurityEx2) {
                    throw new SQLException("Failed to store certificate from serverSslCert into a keyStore", SqlStates.CONNECTION_EXCEPTION.getSqlState(), generalSecurityEx2);
                }
            }
        }
        finally {
            if (inStream != null) {
                try {
                    inStream.close();
                }
                catch (IOException ex) {}
            }
        }
        try {
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ks);
            for (final TrustManager tm : tmf.getTrustManagers()) {
                if (tm instanceof X509TrustManager) {
                    this.trustManager = (X509TrustManager)tm;
                    break;
                }
            }
        }
        catch (NoSuchAlgorithmException noSuchAlgorithmEx) {
            throw new SQLException("Failed to create TrustManagerFactory default instance", SqlStates.CONNECTION_EXCEPTION.getSqlState(), noSuchAlgorithmEx);
        }
        catch (GeneralSecurityException generalSecurityEx2) {
            throw new SQLException("Failed to initialize trust manager", SqlStates.CONNECTION_EXCEPTION.getSqlState(), generalSecurityEx2);
        }
        if (this.trustManager == null) {
            throw new SQLException("No X509TrustManager found");
        }
    }
    
    @Override
    public void checkClientTrusted(final X509Certificate[] x509Certificates, final String string) throws CertificateException {
        if (this.trustManager == null) {
            return;
        }
        this.trustManager.checkClientTrusted(x509Certificates, string);
    }
    
    @Override
    public void checkServerTrusted(final X509Certificate[] x509Certificates, final String string) throws CertificateException {
        if (this.trustManager == null) {
            return;
        }
        this.trustManager.checkServerTrusted(x509Certificates, string);
    }
    
    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return null;
    }
}
