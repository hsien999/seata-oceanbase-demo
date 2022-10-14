// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol.tls;

import java.util.ArrayList;
import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import javax.security.auth.x500.X500Principal;
import java.security.cert.Certificate;
import javax.net.ssl.SSLSession;
import java.security.cert.CertificateParsingException;
import java.util.Collection;
import java.util.List;
import java.security.cert.X509Certificate;
import java.net.UnknownHostException;
import java.net.InetAddress;
import java.util.Iterator;
import javax.naming.InvalidNameException;
import javax.naming.ldap.Rdn;
import javax.naming.ldap.LdapName;
import javax.net.ssl.SSLException;
import java.util.StringTokenizer;
import java.util.Locale;
import com.oceanbase.jdbc.internal.util.Utils;
import com.oceanbase.jdbc.internal.logging.Logger;
import javax.net.ssl.HostnameVerifier;

public class HostnameVerifierImpl implements HostnameVerifier
{
    private static final Logger logger;
    
    private static boolean matchDns(final String hostname, final String tlsDnsPattern) throws SSLException {
        final boolean hostIsIp = Utils.isIPv4(hostname) || Utils.isIPv6(hostname);
        final StringTokenizer hostnameSt = new StringTokenizer(hostname.toLowerCase(Locale.ROOT), ".");
        final StringTokenizer templateSt = new StringTokenizer(tlsDnsPattern.toLowerCase(Locale.ROOT), ".");
        if (hostnameSt.countTokens() != templateSt.countTokens()) {
            return false;
        }
        try {
            while (hostnameSt.hasMoreTokens()) {
                if (!matchWildCards(hostIsIp, hostnameSt.nextToken(), templateSt.nextToken())) {
                    return false;
                }
            }
        }
        catch (SSLException exception) {
            throw new SSLException(normalizedHostMsg(hostname) + " doesn't correspond to certificate CN \"" + tlsDnsPattern + "\" : wildcards not possible for IPs");
        }
        return true;
    }
    
    private static boolean matchWildCards(final boolean hostIsIp, final String hostnameToken, final String tlsDnsToken) throws SSLException {
        int wildcardIndex = tlsDnsToken.indexOf("*");
        String token = hostnameToken;
        if (wildcardIndex == -1) {
            return token.equals(tlsDnsToken);
        }
        if (hostIsIp) {
            throw new SSLException("WildCards not possible when using IP's");
        }
        boolean first = true;
        String afterWildcard;
        for (afterWildcard = tlsDnsToken; wildcardIndex != -1; wildcardIndex = afterWildcard.indexOf("*")) {
            final String beforeWildcard = afterWildcard.substring(0, wildcardIndex);
            afterWildcard = afterWildcard.substring(wildcardIndex + 1);
            final int beforeStartIdx = token.indexOf(beforeWildcard);
            if (beforeStartIdx == -1 || (first && beforeStartIdx != 0)) {
                return false;
            }
            first = false;
            token = token.substring(beforeStartIdx + beforeWildcard.length());
        }
        return token.endsWith(afterWildcard);
    }
    
    private static String extractCommonName(final String principal) throws SSLException {
        if (principal == null) {
            return null;
        }
        try {
            final LdapName ldapName = new LdapName(principal);
            for (final Rdn rdn : ldapName.getRdns()) {
                if (rdn.getType().equalsIgnoreCase("CN")) {
                    final Object obj = rdn.getValue();
                    if (obj != null) {
                        return obj.toString();
                    }
                    continue;
                }
            }
            return null;
        }
        catch (InvalidNameException e) {
            throw new SSLException("DN value \"" + principal + "\" is invalid");
        }
    }
    
    private static String normaliseAddress(final String hostname) {
        try {
            if (hostname == null) {
                return null;
            }
            final InetAddress inetAddress = InetAddress.getByName(hostname);
            return inetAddress.getHostAddress();
        }
        catch (UnknownHostException unexpected) {
            return hostname;
        }
    }
    
    private static String normalizedHostMsg(final String normalizedHost) {
        final StringBuilder msg = new StringBuilder();
        if (Utils.isIPv4(normalizedHost)) {
            msg.append("IPv4 host \"");
        }
        else if (Utils.isIPv6(normalizedHost)) {
            msg.append("IPv6 host \"");
        }
        else {
            msg.append("DNS host \"");
        }
        msg.append(normalizedHost).append("\"");
        return msg.toString();
    }
    
    private SubjectAltNames getSubjectAltNames(final X509Certificate cert) throws CertificateParsingException {
        final Collection<List<?>> entries = cert.getSubjectAlternativeNames();
        final SubjectAltNames subjectAltNames = new SubjectAltNames();
        if (entries != null) {
            for (final List<?> entry : entries) {
                if (entry.size() >= 2) {
                    final int type = (int)entry.get(0);
                    if (type == 2) {
                        final String altNameDns = (String)entry.get(1);
                        if (altNameDns != null) {
                            final String normalizedSubjectAlt = altNameDns.toLowerCase(Locale.ROOT);
                            subjectAltNames.add(new GeneralName(normalizedSubjectAlt, Extension.DNS));
                        }
                    }
                    if (type != 7) {
                        continue;
                    }
                    final String altNameIp = (String)entry.get(1);
                    if (altNameIp == null) {
                        continue;
                    }
                    subjectAltNames.add(new GeneralName(altNameIp, Extension.IP));
                }
            }
        }
        return subjectAltNames;
    }
    
    @Override
    public boolean verify(final String host, final SSLSession session) {
        return this.verify(host, session, -1L);
    }
    
    public boolean verify(final String host, final SSLSession session, final long serverThreadId) {
        try {
            final Certificate[] certs = session.getPeerCertificates();
            final X509Certificate cert = (X509Certificate)certs[0];
            this.verify(host, cert, serverThreadId);
            return true;
        }
        catch (SSLException ex) {
            if (HostnameVerifierImpl.logger.isDebugEnabled()) {
                HostnameVerifierImpl.logger.debug(ex.getMessage(), ex);
            }
            return false;
        }
    }
    
    public void verify(final String host, final X509Certificate cert, final long serverThreadId) throws SSLException {
        if (host == null) {
            return;
        }
        final String lowerCaseHost = host.toLowerCase(Locale.ROOT);
        try {
            final SubjectAltNames subjectAltNames = this.getSubjectAltNames(cert);
            if (!subjectAltNames.isEmpty()) {
                if (Utils.isIPv4(lowerCaseHost)) {
                    for (final GeneralName entry : subjectAltNames.getGeneralNames()) {
                        if (HostnameVerifierImpl.logger.isTraceEnabled()) {
                            HostnameVerifierImpl.logger.trace("Conn={}. IPv4 verification of hostname : type={} value={} to {}", serverThreadId, entry.extension, entry.value, lowerCaseHost);
                        }
                        if (entry.extension == Extension.IP && lowerCaseHost.equals(entry.value)) {
                            return;
                        }
                    }
                }
                else if (Utils.isIPv6(lowerCaseHost)) {
                    final String normalisedHost = normaliseAddress(lowerCaseHost);
                    for (final GeneralName entry2 : subjectAltNames.getGeneralNames()) {
                        if (HostnameVerifierImpl.logger.isTraceEnabled()) {
                            HostnameVerifierImpl.logger.trace("Conn={}. IPv6 verification of hostname : type={} value={} to {}", serverThreadId, entry2.extension, entry2.value, lowerCaseHost);
                        }
                        if (entry2.extension == Extension.IP && !Utils.isIPv4(entry2.value) && normalisedHost.equals(normaliseAddress(entry2.value))) {
                            return;
                        }
                    }
                }
                else {
                    for (final GeneralName entry : subjectAltNames.getGeneralNames()) {
                        if (HostnameVerifierImpl.logger.isTraceEnabled()) {
                            HostnameVerifierImpl.logger.trace("Conn={}. DNS verification of hostname : type={} value={} to {}", serverThreadId, entry.extension, entry.value, lowerCaseHost);
                        }
                        if (entry.extension == Extension.DNS && matchDns(lowerCaseHost, entry.value.toLowerCase(Locale.ROOT))) {
                            return;
                        }
                    }
                }
            }
            final X500Principal subjectPrincipal = cert.getSubjectX500Principal();
            final String cn = extractCommonName(subjectPrincipal.getName("RFC2253"));
            if (cn == null) {
                if (subjectAltNames.isEmpty()) {
                    throw new SSLException("CN not found in certificate principal \"{}" + subjectPrincipal + "\" and certificate doesn't contain SAN");
                }
                throw new SSLException("CN not found in certificate principal \"" + subjectPrincipal + "\" and " + normalizedHostMsg(lowerCaseHost) + " doesn't correspond to " + subjectAltNames.toString());
            }
            else {
                final String normalizedCn = cn.toLowerCase(Locale.ROOT);
                if (HostnameVerifierImpl.logger.isTraceEnabled()) {
                    HostnameVerifierImpl.logger.trace("Conn={}. DNS verification of hostname : CN={} to {}", serverThreadId, normalizedCn, lowerCaseHost);
                }
                if (!matchDns(lowerCaseHost, normalizedCn)) {
                    String errorMsg = normalizedHostMsg(lowerCaseHost) + " doesn't correspond to certificate CN \"" + normalizedCn + "\"";
                    if (!subjectAltNames.isEmpty()) {
                        errorMsg = errorMsg + " and " + subjectAltNames.toString();
                    }
                    throw new SSLException(errorMsg);
                }
            }
        }
        catch (CertificateParsingException cpe) {
            throw new SSLException("certificate parsing error : " + cpe.getMessage());
        }
    }
    
    static {
        logger = LoggerFactory.getLogger(HostnameVerifierImpl.class);
    }
    
    private enum Extension
    {
        DNS, 
        IP;
    }
    
    private class GeneralName
    {
        private final String value;
        private final Extension extension;
        
        public GeneralName(final String value, final Extension extension) {
            this.value = value;
            this.extension = extension;
        }
        
        @Override
        public String toString() {
            return "{" + this.extension + ":\"" + this.value + "\"}";
        }
    }
    
    private class SubjectAltNames
    {
        private final List<GeneralName> generalNames;
        
        private SubjectAltNames() {
            this.generalNames = new ArrayList<GeneralName>();
        }
        
        @Override
        public String toString() {
            if (this.isEmpty()) {
                return "SAN[-empty-]";
            }
            final StringBuilder sb = new StringBuilder("SAN[");
            boolean first = true;
            for (final GeneralName generalName : this.generalNames) {
                if (!first) {
                    sb.append(",");
                }
                first = false;
                sb.append(generalName.toString());
            }
            sb.append("]");
            return sb.toString();
        }
        
        public List<GeneralName> getGeneralNames() {
            return this.generalNames;
        }
        
        public void add(final GeneralName generalName) {
            this.generalNames.add(generalName);
        }
        
        public boolean isEmpty() {
            return this.generalNames.isEmpty();
        }
    }
}
