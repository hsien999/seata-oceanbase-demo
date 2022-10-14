// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.net.SocketException;
import java.net.Socket;
import java.security.Key;
import javax.crypto.Cipher;
import java.security.spec.KeySpec;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import com.alipay.oceanbase.jdbc.util.Base64Decoder;
import java.security.interfaces.RSAPublicKey;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import javax.net.ssl.KeyManager;
import java.security.cert.X509Certificate;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLContext;
import java.net.MalformedURLException;
import java.security.cert.CertificateException;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.net.URL;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLSocketFactory;
import java.sql.SQLException;
import java.util.List;
import java.io.IOException;
import java.io.BufferedOutputStream;
import java.io.BufferedInputStream;
import java.util.Arrays;
import javax.net.ssl.SSLSocket;
import java.util.ArrayList;
import java.util.Properties;

public class ExportControlled
{
    private static final String SQL_STATE_BAD_SSL_PARAMS = "08000";
    
    protected static boolean enabled() {
        return true;
    }
    
    protected static void transformSocketToSSLSocket(final MysqlIO mysqlIO) throws SQLException {
        final SocketFactory sslFact = new StandardSSLSocketFactory(getSSLSocketFactoryDefaultOrConfigured(mysqlIO), mysqlIO.socketFactory, mysqlIO.mysqlConnection);
        try {
            mysqlIO.mysqlConnection = sslFact.connect(mysqlIO.host, mysqlIO.port, null);
            final List<String> allowedProtocols = new ArrayList<String>();
            final List<String> supportedProtocols = Arrays.asList(((SSLSocket)mysqlIO.mysqlConnection).getSupportedProtocols());
            for (final String protocol : (mysqlIO.versionMeetsMinimum(5, 6, 0) && Util.isEnterpriseEdition(mysqlIO.getServerVersion())) ? new String[] { "TLSv1.2", "TLSv1.1", "TLSv1" } : new String[] { "TLSv1.1", "TLSv1" }) {
                if (supportedProtocols.contains(protocol)) {
                    allowedProtocols.add(protocol);
                }
            }
            ((SSLSocket)mysqlIO.mysqlConnection).setEnabledProtocols(allowedProtocols.toArray(new String[0]));
            final String enabledSSLCipherSuites = mysqlIO.connection.getEnabledSSLCipherSuites();
            final boolean overrideCiphers = enabledSSLCipherSuites != null && enabledSSLCipherSuites.length() > 0;
            List<String> allowedCiphers = null;
            if (overrideCiphers) {
                allowedCiphers = new ArrayList<String>();
                final List<String> availableCiphers = Arrays.asList(((SSLSocket)mysqlIO.mysqlConnection).getEnabledCipherSuites());
                for (final String cipher : enabledSSLCipherSuites.split("\\s*,\\s*")) {
                    if (availableCiphers.contains(cipher)) {
                        allowedCiphers.add(cipher);
                    }
                }
            }
            else {
                boolean disableDHAlgorithm = false;
                if ((mysqlIO.versionMeetsMinimum(5, 5, 45) && !mysqlIO.versionMeetsMinimum(5, 6, 0)) || (mysqlIO.versionMeetsMinimum(5, 6, 26) && !mysqlIO.versionMeetsMinimum(5, 7, 0)) || mysqlIO.versionMeetsMinimum(5, 7, 6)) {
                    if (Util.getJVMVersion() < 8) {
                        disableDHAlgorithm = true;
                    }
                }
                else if (Util.getJVMVersion() >= 8) {
                    disableDHAlgorithm = true;
                }
                if (disableDHAlgorithm) {
                    allowedCiphers = new ArrayList<String>();
                    for (final String cipher : ((SSLSocket)mysqlIO.mysqlConnection).getEnabledCipherSuites()) {
                        if (!disableDHAlgorithm || (cipher.indexOf("_DHE_") <= -1 && cipher.indexOf("_DH_") <= -1)) {
                            allowedCiphers.add(cipher);
                        }
                    }
                }
            }
            if (allowedCiphers != null) {
                ((SSLSocket)mysqlIO.mysqlConnection).setEnabledCipherSuites(allowedCiphers.toArray(new String[0]));
            }
            ((SSLSocket)mysqlIO.mysqlConnection).startHandshake();
            if (mysqlIO.connection.getUseUnbufferedInput()) {
                mysqlIO.mysqlInput = mysqlIO.mysqlConnection.getInputStream();
            }
            else {
                mysqlIO.mysqlInput = new BufferedInputStream(mysqlIO.mysqlConnection.getInputStream(), 16384);
            }
            (mysqlIO.mysqlOutput = new BufferedOutputStream(mysqlIO.mysqlConnection.getOutputStream(), 16384)).flush();
            mysqlIO.socketFactory = sslFact;
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(mysqlIO.connection, mysqlIO.getLastPacketSentTimeMs(), mysqlIO.getLastPacketReceivedTimeMs(), ioEx, mysqlIO.getExceptionInterceptor());
        }
    }
    
    private ExportControlled() {
    }
    
    private static SSLSocketFactory getSSLSocketFactoryDefaultOrConfigured(final MysqlIO mysqlIO) throws SQLException {
        final String clientCertificateKeyStoreUrl = mysqlIO.connection.getClientCertificateKeyStoreUrl();
        final String trustCertificateKeyStoreUrl = mysqlIO.connection.getTrustCertificateKeyStoreUrl();
        final String clientCertificateKeyStoreType = mysqlIO.connection.getClientCertificateKeyStoreType();
        final String clientCertificateKeyStorePassword = mysqlIO.connection.getClientCertificateKeyStorePassword();
        final String trustCertificateKeyStoreType = mysqlIO.connection.getTrustCertificateKeyStoreType();
        final String trustCertificateKeyStorePassword = mysqlIO.connection.getTrustCertificateKeyStorePassword();
        if (StringUtils.isNullOrEmpty(clientCertificateKeyStoreUrl) && StringUtils.isNullOrEmpty(trustCertificateKeyStoreUrl) && mysqlIO.connection.getVerifyServerCertificate()) {
            return (SSLSocketFactory)SSLSocketFactory.getDefault();
        }
        TrustManagerFactory tmf = null;
        KeyManagerFactory kmf = null;
        try {
            tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        }
        catch (NoSuchAlgorithmException nsae2) {
            throw SQLError.createSQLException("Default algorithm definitions for TrustManager and/or KeyManager are invalid.  Check java security properties file.", "08000", 0, false, mysqlIO.getExceptionInterceptor());
        }
        if (!StringUtils.isNullOrEmpty(clientCertificateKeyStoreUrl)) {
            InputStream ksIS = null;
            try {
                if (!StringUtils.isNullOrEmpty(clientCertificateKeyStoreType)) {
                    final KeyStore clientKeyStore = KeyStore.getInstance(clientCertificateKeyStoreType);
                    final URL ksURL = new URL(clientCertificateKeyStoreUrl);
                    final char[] password = (clientCertificateKeyStorePassword == null) ? new char[0] : clientCertificateKeyStorePassword.toCharArray();
                    ksIS = ksURL.openStream();
                    clientKeyStore.load(ksIS, password);
                    kmf.init(clientKeyStore, password);
                }
            }
            catch (UnrecoverableKeyException uke) {
                throw SQLError.createSQLException("Could not recover keys from client keystore.  Check password?", "08000", 0, false, mysqlIO.getExceptionInterceptor());
            }
            catch (NoSuchAlgorithmException nsae) {
                throw SQLError.createSQLException("Unsupported keystore algorithm [" + nsae.getMessage() + "]", "08000", 0, false, mysqlIO.getExceptionInterceptor());
            }
            catch (KeyStoreException kse) {
                throw SQLError.createSQLException("Could not create KeyStore instance [" + kse.getMessage() + "]", "08000", 0, false, mysqlIO.getExceptionInterceptor());
            }
            catch (CertificateException nsae3) {
                throw SQLError.createSQLException("Could not load client" + clientCertificateKeyStoreType + " keystore from " + clientCertificateKeyStoreUrl, mysqlIO.getExceptionInterceptor());
            }
            catch (MalformedURLException mue) {
                throw SQLError.createSQLException(clientCertificateKeyStoreUrl + " does not appear to be a valid URL.", "08000", 0, false, mysqlIO.getExceptionInterceptor());
            }
            catch (IOException ioe) {
                final SQLException sqlEx = SQLError.createSQLException("Cannot open " + clientCertificateKeyStoreUrl + " [" + ioe.getMessage() + "]", "08000", 0, false, mysqlIO.getExceptionInterceptor());
                sqlEx.initCause(ioe);
                throw sqlEx;
            }
            finally {
                if (ksIS != null) {
                    try {
                        ksIS.close();
                    }
                    catch (IOException ex) {}
                }
            }
        }
        if (!StringUtils.isNullOrEmpty(trustCertificateKeyStoreUrl)) {
            InputStream ksIS = null;
            try {
                if (!StringUtils.isNullOrEmpty(trustCertificateKeyStoreType)) {
                    final KeyStore trustKeyStore = KeyStore.getInstance(trustCertificateKeyStoreType);
                    final URL ksURL = new URL(trustCertificateKeyStoreUrl);
                    final char[] password = (trustCertificateKeyStorePassword == null) ? new char[0] : trustCertificateKeyStorePassword.toCharArray();
                    ksIS = ksURL.openStream();
                    trustKeyStore.load(ksIS, password);
                    tmf.init(trustKeyStore);
                }
            }
            catch (NoSuchAlgorithmException nsae) {
                throw SQLError.createSQLException("Unsupported keystore algorithm [" + nsae.getMessage() + "]", "08000", 0, false, mysqlIO.getExceptionInterceptor());
            }
            catch (KeyStoreException kse) {
                throw SQLError.createSQLException("Could not create KeyStore instance [" + kse.getMessage() + "]", "08000", 0, false, mysqlIO.getExceptionInterceptor());
            }
            catch (CertificateException nsae3) {
                throw SQLError.createSQLException("Could not load trust" + trustCertificateKeyStoreType + " keystore from " + trustCertificateKeyStoreUrl, "08000", 0, false, mysqlIO.getExceptionInterceptor());
            }
            catch (MalformedURLException mue) {
                throw SQLError.createSQLException(trustCertificateKeyStoreUrl + " does not appear to be a valid URL.", "08000", 0, false, mysqlIO.getExceptionInterceptor());
            }
            catch (IOException ioe) {
                final SQLException sqlEx = SQLError.createSQLException("Cannot open " + trustCertificateKeyStoreUrl + " [" + ioe.getMessage() + "]", "08000", 0, false, mysqlIO.getExceptionInterceptor());
                sqlEx.initCause(ioe);
                throw sqlEx;
            }
            finally {
                if (ksIS != null) {
                    try {
                        ksIS.close();
                    }
                    catch (IOException ex2) {}
                }
            }
        }
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init((KeyManager[])(StringUtils.isNullOrEmpty(clientCertificateKeyStoreUrl) ? null : kmf.getKeyManagers()), mysqlIO.connection.getVerifyServerCertificate() ? tmf.getTrustManagers() : new X509TrustManager[] { new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
                    }
                    
                    @Override
                    public void checkServerTrusted(final X509Certificate[] chain, final String authType) throws CertificateException {
                    }
                    
                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                } }, null);
            return sslContext.getSocketFactory();
        }
        catch (NoSuchAlgorithmException nsae) {
            throw SQLError.createSQLException("TLS is not a valid SSL protocol.", "08000", 0, false, mysqlIO.getExceptionInterceptor());
        }
        catch (KeyManagementException kme) {
            throw SQLError.createSQLException("KeyManagementException: " + kme.getMessage(), "08000", 0, false, mysqlIO.getExceptionInterceptor());
        }
    }
    
    public static boolean isSSLEstablished(final MysqlIO mysqlIO) {
        return SSLSocket.class.isAssignableFrom(mysqlIO.mysqlConnection.getClass());
    }
    
    public static RSAPublicKey decodeRSAPublicKey(final String key, final ExceptionInterceptor interceptor) throws SQLException {
        try {
            if (key == null) {
                throw new SQLException("key parameter is null");
            }
            final int offset = key.indexOf("\n") + 1;
            final int len = key.indexOf("-----END PUBLIC KEY-----") - offset;
            final byte[] certificateData = Base64Decoder.decode(key.getBytes(), offset, len);
            final X509EncodedKeySpec spec = new X509EncodedKeySpec(certificateData);
            final KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPublicKey)kf.generatePublic(spec);
        }
        catch (Exception ex) {
            throw SQLError.createSQLException("Unable to decode public key", "S1009", ex, interceptor);
        }
    }
    
    public static byte[] encryptWithRSAPublicKey(final byte[] source, final RSAPublicKey key, final ExceptionInterceptor interceptor) throws SQLException {
        try {
            final Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-1AndMGF1Padding");
            cipher.init(1, key);
            return cipher.doFinal(source);
        }
        catch (Exception ex) {
            throw SQLError.createSQLException(ex.getMessage(), "S1009", ex, interceptor);
        }
    }
    
    public static class StandardSSLSocketFactory implements SocketFactory, SocketMetadata
    {
        private SSLSocket rawSocket;
        private final SSLSocketFactory sslFact;
        private final SocketFactory existingSocketFactory;
        private final Socket existingSocket;
        
        public StandardSSLSocketFactory(final SSLSocketFactory sslFact, final SocketFactory existingSocketFactory, final Socket existingSocket) {
            this.rawSocket = null;
            this.sslFact = sslFact;
            this.existingSocketFactory = existingSocketFactory;
            this.existingSocket = existingSocket;
        }
        
        @Override
        public Socket afterHandshake() throws SocketException, IOException {
            this.existingSocketFactory.afterHandshake();
            return this.rawSocket;
        }
        
        @Override
        public Socket beforeHandshake() throws SocketException, IOException {
            return this.rawSocket;
        }
        
        @Override
        public Socket connect(final String host, final int portNumber, final Properties props) throws SocketException, IOException {
            return this.rawSocket = (SSLSocket)this.sslFact.createSocket(this.existingSocket, host, portNumber, true);
        }
        
        @Override
        public boolean isLocallyConnected(final ConnectionImpl conn) throws SQLException {
            return Helper.isLocallyConnected(conn);
        }
    }
}
