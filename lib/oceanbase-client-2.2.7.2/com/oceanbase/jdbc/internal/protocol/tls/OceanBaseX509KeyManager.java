// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol.tls;

import javax.security.auth.x500.X500Principal;
import java.util.Iterator;
import java.util.Map;
import java.util.ArrayList;
import java.security.PrivateKey;
import javax.net.ssl.SSLEngine;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.security.cert.X509Certificate;
import java.net.Socket;
import java.util.List;
import java.security.Principal;
import java.security.KeyStoreException;
import java.util.Enumeration;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.KeyStore;
import java.util.Hashtable;
import javax.net.ssl.X509ExtendedKeyManager;

public class OceanBaseX509KeyManager extends X509ExtendedKeyManager
{
    private final Hashtable<String, KeyStore.PrivateKeyEntry> privateKeyHash;
    
    public OceanBaseX509KeyManager(final KeyStore keyStore, final char[] pwd) throws KeyStoreException {
        this.privateKeyHash = new Hashtable<String, KeyStore.PrivateKeyEntry>();
        final Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            final String alias = aliases.nextElement();
            if (keyStore.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
                try {
                    this.privateKeyHash.put(alias, (KeyStore.PrivateKeyEntry)keyStore.getEntry(alias, new KeyStore.PasswordProtection(pwd)));
                }
                catch (UnrecoverableEntryException ex) {}
                catch (NoSuchAlgorithmException ex2) {}
            }
        }
    }
    
    @Override
    public String[] getClientAliases(final String keyType, final Principal[] issuers) {
        final List<String> accurateAlias = this.searchAccurateAliases(new String[] { keyType }, issuers);
        if (accurateAlias.size() == 0) {
            return null;
        }
        return accurateAlias.toArray(new String[accurateAlias.size()]);
    }
    
    @Override
    public String chooseClientAlias(final String[] keyType, final Principal[] issuers, final Socket socket) {
        final List<String> accurateAlias = this.searchAccurateAliases(keyType, issuers);
        return (accurateAlias.size() > 0) ? accurateAlias.get(0) : null;
    }
    
    @Override
    public X509Certificate[] getCertificateChain(final String alias) {
        final KeyStore.PrivateKeyEntry keyEntry = this.privateKeyHash.get(alias);
        if (keyEntry == null) {
            return null;
        }
        final Certificate[] certs = keyEntry.getCertificateChain();
        if (certs.length > 0 && certs[0] instanceof X509Certificate) {
            return Arrays.copyOf(certs, certs.length, (Class<? extends X509Certificate[]>)X509Certificate[].class);
        }
        return null;
    }
    
    @Override
    public String chooseEngineClientAlias(final String[] keyType, final Principal[] issuers, final SSLEngine engine) {
        return this.chooseClientAlias(keyType, issuers, null);
    }
    
    @Override
    public PrivateKey getPrivateKey(final String alias) {
        final KeyStore.PrivateKeyEntry keyEntry = this.privateKeyHash.get(alias);
        if (keyEntry == null) {
            return null;
        }
        return keyEntry.getPrivateKey();
    }
    
    private ArrayList<String> searchAccurateAliases(final String[] keyTypes, final Principal[] issuers) {
        if (keyTypes == null || keyTypes.length == 0) {
            return null;
        }
        final ArrayList<String> accurateAliases = new ArrayList<String>();
        for (final Map.Entry<String, KeyStore.PrivateKeyEntry> mapEntry : this.privateKeyHash.entrySet()) {
            final Certificate[] certs = mapEntry.getValue().getCertificateChain();
            final String alg = certs[0].getPublicKey().getAlgorithm();
            for (final String keyType : keyTypes) {
                Label_0249: {
                    if (alg.equals(keyType)) {
                        if (issuers != null && issuers.length != 0) {
                            for (final Certificate cert : certs) {
                                if (cert instanceof X509Certificate) {
                                    final X500Principal certificateIssuer = ((X509Certificate)cert).getIssuerX500Principal();
                                    for (final Principal issuer : issuers) {
                                        if (certificateIssuer.equals(issuer)) {
                                            accurateAliases.add(mapEntry.getKey());
                                            break Label_0249;
                                        }
                                    }
                                }
                            }
                        }
                        else {
                            accurateAliases.add(mapEntry.getKey());
                        }
                    }
                }
            }
        }
        return accurateAliases;
    }
    
    @Override
    public String[] getServerAliases(final String keyType, final Principal[] issuers) {
        return null;
    }
    
    @Override
    public String chooseServerAlias(final String keyType, final Principal[] issuers, final Socket socket) {
        return null;
    }
    
    @Override
    public String chooseEngineServerAlias(final String keyType, final Principal[] issuers, final SSLEngine engine) {
        return null;
    }
}
