// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import com.oceanbase.jdbc.internal.util.constant.HaMode;
import com.oceanbase.jdbc.internal.logging.Logger;

public class HostAddress
{
    private static final Logger logger;
    public String host;
    public int port;
    public String type;
    
    private HostAddress() {
        this.type = null;
    }
    
    public HostAddress(final String host, final int port) {
        this.type = null;
        this.host = host;
        this.port = port;
        this.type = "master";
    }
    
    public HostAddress(final String host, final int port, final String type) {
        this.type = null;
        this.host = host;
        this.port = port;
        this.type = type;
    }
    
    public static List<HostAddress> parse(final String spec, final HaMode haMode) {
        if (spec == null) {
            throw new IllegalArgumentException("Invalid connection URL, host address must not be empty ");
        }
        if ("".equals(spec)) {
            return new ArrayList<HostAddress>(0);
        }
        final String[] tokens = spec.trim().split(",");
        final int size = tokens.length;
        final List<HostAddress> arr = new ArrayList<HostAddress>(size);
        if (haMode == HaMode.AURORA) {
            final Pattern clusterPattern = Pattern.compile("(.+)\\.(?:cluster-|cluster-ro-)([a-z0-9]+\\.[a-z0-9\\-]+\\.rds\\.amazonaws\\.com)", 2);
            final Matcher matcher = clusterPattern.matcher(spec);
            if (!matcher.find()) {
                HostAddress.logger.warn("Aurora recommended connection URL must only use cluster end-point like \"jdbc:oceanbase:aurora://xx.cluster-yy.zz.rds.amazonaws.com\". Using end-point permit auto-discovery of new replicas");
            }
        }
        for (final String token : tokens) {
            if (token.startsWith("address=")) {
                arr.add(parseParameterHostAddress(token));
            }
            else {
                arr.add(parseSimpleHostAddress(token));
            }
        }
        if (haMode == HaMode.REPLICATION) {
            for (int i = 0; i < size; ++i) {
                if (i == 0 && arr.get(i).type == null) {
                    arr.get(i).type = "master";
                }
                else if (i != 0 && arr.get(i).type == null) {
                    arr.get(i).type = "slave";
                }
            }
        }
        return arr;
    }
    
    private static HostAddress parseSimpleHostAddress(final String str) {
        final HostAddress result = new HostAddress();
        if (str.charAt(0) == '[') {
            final int ind = str.indexOf(93);
            result.host = str.substring(1, ind);
            if (ind != str.length() - 1 && str.charAt(ind + 1) == ':') {
                result.port = getPort(str.substring(ind + 2));
            }
        }
        else if (str.contains(":")) {
            final String[] hostPort = str.split(":");
            result.host = hostPort[0];
            result.port = getPort(hostPort[1]);
        }
        else {
            result.host = str;
            result.port = 3306;
        }
        return result;
    }
    
    private static int getPort(final String portString) {
        try {
            return Integer.parseInt(portString);
        }
        catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Incorrect port value : " + portString);
        }
    }
    
    private static HostAddress parseParameterHostAddress(final String str) {
        final HostAddress result = new HostAddress();
        final String[] array = str.split("(?=\\()|(?<=\\))");
        for (int i = 1; i < array.length; ++i) {
            final String[] token = array[i].replace("(", "").replace(")", "").trim().split("=");
            if (token.length != 2) {
                throw new IllegalArgumentException("Invalid connection URL, expected key=value pairs, found " + array[i]);
            }
            final String key = token[0].toLowerCase();
            final String value = token[1].toLowerCase();
            if ("host".equals(key)) {
                result.host = value.replace("[", "").replace("]", "");
            }
            else if ("port".equals(key)) {
                result.port = getPort(value);
            }
            else if ("type".equals(key) && (value.equals("master") || value.equals("slave"))) {
                result.type = value;
            }
        }
        return result;
    }
    
    public static String toString(final List<HostAddress> addrs) {
        final StringBuilder str = new StringBuilder();
        for (int i = 0; i < addrs.size(); ++i) {
            if (addrs.get(i).type != null) {
                str.append("address=(host=").append(addrs.get(i).host).append(")(port=").append(addrs.get(i).port).append(")(type=").append(addrs.get(i).type).append(")");
            }
            else {
                final boolean isIPv6 = addrs.get(i).host != null && addrs.get(i).host.contains(":");
                final String host = isIPv6 ? ("[" + addrs.get(i).host + "]") : addrs.get(i).host;
                str.append(host).append(":").append(addrs.get(i).port);
            }
            if (i < addrs.size() - 1) {
                str.append(",");
            }
        }
        return str.toString();
    }
    
    public static String toString(final HostAddress[] addrs) {
        final StringBuilder str = new StringBuilder();
        for (int i = 0; i < addrs.length; ++i) {
            if (addrs[i].type != null) {
                str.append("address=(host=").append(addrs[i].host).append(")(port=").append(addrs[i].port).append(")(type=").append(addrs[i].type).append(")");
            }
            else {
                final boolean isIPv6 = addrs[i].host != null && addrs[i].host.contains(":");
                final String host = isIPv6 ? ("[" + addrs[i].host + "]") : addrs[i].host;
                str.append(host).append(":").append(addrs[i].port);
            }
            if (i < addrs.length - 1) {
                str.append(",");
            }
        }
        return str.toString();
    }
    
    @Override
    public String toString() {
        return "HostAddress{host='" + this.host + '\'' + ", port=" + this.port + ((this.type != null) ? (", type='" + this.type + "'") : "") + "}";
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || this.getClass() != obj.getClass()) {
            return false;
        }
        final HostAddress that = (HostAddress)obj;
        if (this.port != that.port || !((this.host == null) ? (that.host == null) : this.host.equals(that.host))) {
            return false;
        }
        boolean b = false;
        Label_0102: {
            b = true;
        }
        return b;
        Label_0106:
        b = false;
        return b;
        // iftrue(Label_0102:, this.type.equals((Object)that.type))
        return false;
        // iftrue(Label_0106:, that.type != null)
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = (this.host != null) ? this.host.hashCode() : 0;
        result = 31 * result + this.port;
        return result;
    }
    
    static {
        logger = LoggerFactory.getLogger(HostAddress.class);
    }
}
