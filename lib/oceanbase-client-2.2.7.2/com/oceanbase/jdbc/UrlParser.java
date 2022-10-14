// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import java.util.Properties;
import java.sql.SQLException;
import java.util.Iterator;
import com.oceanbase.jdbc.util.DefaultOptions;
import com.oceanbase.jdbc.credential.CredentialPluginLoader;
import com.oceanbase.jdbc.credential.CredentialPlugin;
import com.oceanbase.jdbc.internal.util.constant.HaMode;
import java.util.List;
import com.oceanbase.jdbc.util.Options;
import java.util.regex.Pattern;

public class UrlParser implements Cloneable
{
    private static final String DISABLE_MYSQL_URL = "disableMariaDbDriver";
    private static final Pattern URL_PARAMETER;
    private static final Pattern AWS_PATTERN;
    private String database;
    private Options options;
    private List<HostAddress> addresses;
    private HaMode haMode;
    private String initialUrl;
    private boolean multiMaster;
    private CredentialPlugin credentialPlugin;
    
    private UrlParser() {
        this.options = null;
    }
    
    public UrlParser(final String database, final List<HostAddress> addresses, final Options options, final HaMode haMode) throws SQLException {
        this.options = null;
        this.options = options;
        this.database = database;
        this.addresses = addresses;
        this.haMode = haMode;
        if (haMode == HaMode.AURORA) {
            for (final HostAddress hostAddress : addresses) {
                hostAddress.type = null;
            }
        }
        else {
            for (final HostAddress hostAddress : addresses) {
                if (hostAddress.type == null) {
                    hostAddress.type = "master";
                }
            }
        }
        DefaultOptions.postOptionProcess(options, this.credentialPlugin = CredentialPluginLoader.get(options.credentialType));
        this.setInitialUrl();
        this.loadMultiMasterValue();
    }
    
    public static boolean acceptsUrl(final String url) {
        return url != null && url.startsWith("jdbc:oceanbase:") && !url.contains("disableMariaDbDriver");
    }
    
    public static UrlParser parse(final String url) throws SQLException {
        return parse(url, new Properties());
    }
    
    public static UrlParser parse(final String url, final Properties prop) throws SQLException {
        if (url != null && (url.startsWith("jdbc:oceanbase:") || url.startsWith("jdbc:mysql:")) && !url.contains("disableMariaDbDriver")) {
            final UrlParser urlParser = new UrlParser();
            parseInternal(urlParser, url, (prop == null) ? new Properties() : prop);
            return urlParser;
        }
        return null;
    }
    
    private static void parseInternal(final UrlParser urlParser, final String url, final Properties properties) throws SQLException {
        try {
            urlParser.initialUrl = url;
            final int separator = url.indexOf("//");
            if (separator == -1) {
                throw new IllegalArgumentException("url parsing error : '//' is not present in the url " + url);
            }
            urlParser.haMode = parseHaMode(url, separator);
            final String urlSecondPart = url.substring(separator + 2);
            final int dbIndex = urlSecondPart.indexOf("/");
            final int paramIndex = urlSecondPart.indexOf("?");
            String hostAddressesString;
            String additionalParameters;
            if ((dbIndex < paramIndex && dbIndex < 0) || (dbIndex > paramIndex && paramIndex > -1)) {
                hostAddressesString = urlSecondPart.substring(0, paramIndex);
                additionalParameters = urlSecondPart.substring(paramIndex);
            }
            else if ((dbIndex < paramIndex && dbIndex > -1) || (dbIndex > paramIndex && paramIndex < 0)) {
                hostAddressesString = urlSecondPart.substring(0, dbIndex);
                additionalParameters = urlSecondPart.substring(dbIndex);
            }
            else {
                hostAddressesString = urlSecondPart;
                additionalParameters = null;
            }
            defineUrlParserParameters(urlParser, properties, hostAddressesString, additionalParameters);
            setDefaultHostAddressType(urlParser);
            urlParser.loadMultiMasterValue();
        }
        catch (IllegalArgumentException i) {
            throw new SQLException("error parsing url : " + i.getMessage(), i);
        }
    }
    
    private static void defineUrlParserParameters(final UrlParser urlParser, final Properties properties, final String hostAddressesString, final String additionalParameters) throws SQLException {
        if (additionalParameters != null) {
            final Matcher matcher = UrlParser.URL_PARAMETER.matcher(additionalParameters);
            matcher.find();
            urlParser.database = matcher.group(2);
            urlParser.options = DefaultOptions.parse(urlParser.haMode, matcher.group(4), properties, urlParser.options);
            if (urlParser.database != null && urlParser.database.isEmpty()) {
                urlParser.database = null;
            }
        }
        else {
            urlParser.database = null;
            urlParser.options = DefaultOptions.parse(urlParser.haMode, "", properties, urlParser.options);
        }
        urlParser.credentialPlugin = CredentialPluginLoader.get(urlParser.options.credentialType);
        DefaultOptions.postOptionProcess(urlParser.options, urlParser.credentialPlugin);
        LoggerFactory.init(urlParser.options.log || urlParser.options.profileSql || urlParser.options.slowQueryThresholdNanos != null);
        urlParser.addresses = HostAddress.parse(hostAddressesString, urlParser.haMode);
        if (properties.get("port") != null) {
            urlParser.addresses.forEach(address -> address.port = Integer.parseInt((String)properties.get("port")));
        }
    }
    
    private static HaMode parseHaMode(final String url, final int separator) {
        final int firstColonPos = url.indexOf(58);
        final int secondColonPos = url.indexOf(58, firstColonPos + 1);
        int thirdColonPos = url.indexOf(58, secondColonPos + 1);
        int forthColonPos = url.indexOf(58, thirdColonPos + 1);
        if (thirdColonPos > separator || thirdColonPos == -1) {
            if (secondColonPos == separator - 1) {
                return HaMode.NONE;
            }
            thirdColonPos = separator;
        }
        if (forthColonPos > separator || forthColonPos == -1) {
            forthColonPos = separator;
        }
        try {
            String haModeString = url.substring(secondColonPos + 1, thirdColonPos).toUpperCase(Locale.ROOT);
            if ("ORACLE".equals(haModeString)) {
                haModeString = url.substring(thirdColonPos + 1, forthColonPos).toUpperCase(Locale.ROOT);
            }
            if ("".equals(haModeString)) {
                return HaMode.NONE;
            }
            if ("FAILOVER".equals(haModeString)) {
                haModeString = "LOADBALANCE";
            }
            return HaMode.valueOf(haModeString);
        }
        catch (IllegalArgumentException i) {
            throw new IllegalArgumentException("wrong failover parameter format in connection String " + url);
        }
    }
    
    private static void setDefaultHostAddressType(final UrlParser urlParser) {
        if (urlParser.haMode == HaMode.AURORA) {
            for (final HostAddress hostAddress : urlParser.addresses) {
                hostAddress.type = null;
            }
        }
        else {
            for (final HostAddress hostAddress : urlParser.addresses) {
                if (hostAddress.type == null) {
                    hostAddress.type = "master";
                }
            }
        }
    }
    
    private void setInitialUrl() {
        final StringBuilder sb = new StringBuilder();
        sb.append("jdbc:oceanbase:");
        if (this.haMode != HaMode.NONE) {
            sb.append(this.haMode.toString().toLowerCase(Locale.ROOT)).append(":");
        }
        sb.append("//");
        for (int i = 0; i < this.addresses.size(); ++i) {
            final HostAddress hostAddress = this.addresses.get(i);
            if (i > 0) {
                sb.append(",");
            }
            sb.append("address=(host=").append(hostAddress.host).append(")").append("(port=").append(hostAddress.port).append(")");
            if (hostAddress.type != null) {
                sb.append("(type=").append(hostAddress.type).append(")");
            }
        }
        sb.append("/");
        if (this.database != null) {
            sb.append(this.database);
        }
        DefaultOptions.propertyString(this.options, this.haMode, sb);
        this.initialUrl = sb.toString();
    }
    
    public UrlParser auroraPipelineQuirks() {
        final boolean disablePipeline = this.isAurora();
        if (this.options.useBatchMultiSend == null) {
            this.options.useBatchMultiSend = false;
        }
        if (this.options.usePipelineAuth == null) {
            this.options.usePipelineAuth = false;
        }
        return this;
    }
    
    public boolean isAurora() {
        if (this.haMode == HaMode.AURORA) {
            return true;
        }
        if (this.addresses != null) {
            for (final HostAddress hostAddress : this.addresses) {
                final Matcher matcher = UrlParser.AWS_PATTERN.matcher(hostAddress.host);
                if (matcher.find()) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public void parseUrl(final String url) throws SQLException {
        if (acceptsUrl(url)) {
            parseInternal(this, url, new Properties());
        }
    }
    
    public String getUsername() {
        return this.options.user;
    }
    
    public void setUsername(final String username) {
        this.options.user = username;
    }
    
    public String getPassword() {
        return this.options.password;
    }
    
    public void setPassword(final String password) {
        this.options.password = password;
    }
    
    public String getDatabase() {
        return this.database;
    }
    
    public void setDatabase(final String database) {
        this.database = database;
    }
    
    public List<HostAddress> getHostAddresses() {
        return this.addresses;
    }
    
    public Options getOptions() {
        return this.options;
    }
    
    protected void setProperties(final String urlParameters) {
        DefaultOptions.parse(this.haMode, urlParameters, this.options);
        this.setInitialUrl();
    }
    
    public CredentialPlugin getCredentialPlugin() {
        return this.credentialPlugin;
    }
    
    @Override
    public String toString() {
        return this.initialUrl;
    }
    
    public String getInitialUrl() {
        return this.initialUrl;
    }
    
    public HaMode getHaMode() {
        return this.haMode;
    }
    
    @Override
    public boolean equals(final Object parser) {
        if (this == parser) {
            return true;
        }
        if (!(parser instanceof UrlParser)) {
            return false;
        }
        final UrlParser urlParser = (UrlParser)parser;
        if (this.initialUrl != null) {
            if (!this.initialUrl.equals(urlParser.getInitialUrl())) {
                return false;
            }
        }
        else if (urlParser.getInitialUrl() != null) {
            return false;
        }
        if (this.getUsername() != null) {
            if (!this.getUsername().equals(urlParser.getUsername())) {
                return false;
            }
        }
        else if (urlParser.getUsername() != null) {
            return false;
        }
        if ((this.getPassword() == null) ? (urlParser.getPassword() == null) : this.getPassword().equals(urlParser.getPassword())) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.options.password != null) ? this.options.password.hashCode() : 0;
        result = 31 * result + ((this.options.user != null) ? this.options.user.hashCode() : 0);
        result = 31 * result + this.initialUrl.hashCode();
        return result;
    }
    
    private void loadMultiMasterValue() {
        if (this.haMode == HaMode.SEQUENTIAL || this.haMode == HaMode.REPLICATION || this.haMode == HaMode.LOADBALANCE) {
            boolean firstMaster = false;
            for (final HostAddress host : this.addresses) {
                if (host.type.equals("master")) {
                    if (firstMaster) {
                        this.multiMaster = true;
                        return;
                    }
                    firstMaster = true;
                }
            }
        }
        this.multiMaster = false;
    }
    
    public boolean isMultiMaster() {
        return this.multiMaster;
    }
    
    public Object clone() throws CloneNotSupportedException {
        final UrlParser tmpUrlParser = (UrlParser)super.clone();
        tmpUrlParser.options = (Options)this.options.clone();
        (tmpUrlParser.addresses = new ArrayList<HostAddress>()).addAll(this.addresses);
        return tmpUrlParser;
    }
    
    static {
        URL_PARAMETER = Pattern.compile("(\\/([^\\?]*))?(\\?(.+))*", 32);
        AWS_PATTERN = Pattern.compile("(.+)\\.([a-z0-9\\-]+\\.rds\\.amazonaws\\.com)", 2);
    }
}
