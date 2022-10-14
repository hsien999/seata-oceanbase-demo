// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.util.Locale;
import java.util.Iterator;
import java.util.Set;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

class MysqlCharset
{
    public final String charsetName;
    public final int mblen;
    public final int priority;
    public final List<String> javaEncodingsUc;
    public int major;
    public int minor;
    public int subminor;
    
    public MysqlCharset(final String charsetName, final int mblen, final int priority, final String[] javaEncodings) {
        this.javaEncodingsUc = new ArrayList<String>();
        this.major = 4;
        this.minor = 1;
        this.subminor = 0;
        this.charsetName = charsetName;
        this.mblen = mblen;
        this.priority = priority;
        for (int i = 0; i < javaEncodings.length; ++i) {
            final String encoding = javaEncodings[i];
            try {
                final Charset cs = Charset.forName(encoding);
                this.addEncodingMapping(cs.name());
                final Set<String> als = cs.aliases();
                final Iterator<String> ali = als.iterator();
                while (ali.hasNext()) {
                    this.addEncodingMapping(ali.next());
                }
            }
            catch (Exception e) {
                if (mblen == 1) {
                    this.addEncodingMapping(encoding);
                }
            }
        }
        if (this.javaEncodingsUc.size() == 0) {
            if (mblen > 1) {
                this.addEncodingMapping("UTF-8");
            }
            else {
                this.addEncodingMapping("Cp1252");
            }
        }
    }
    
    private void addEncodingMapping(final String encoding) {
        final String encodingUc = encoding.toUpperCase(Locale.ENGLISH);
        if (!this.javaEncodingsUc.contains(encodingUc)) {
            this.javaEncodingsUc.add(encodingUc);
        }
    }
    
    public MysqlCharset(final String charsetName, final int mblen, final int priority, final String[] javaEncodings, final int major, final int minor) {
        this(charsetName, mblen, priority, javaEncodings);
        this.major = major;
        this.minor = minor;
    }
    
    public MysqlCharset(final String charsetName, final int mblen, final int priority, final String[] javaEncodings, final int major, final int minor, final int subminor) {
        this(charsetName, mblen, priority, javaEncodings);
        this.major = major;
        this.minor = minor;
        this.subminor = subminor;
    }
    
    @Override
    public String toString() {
        final StringBuilder asString = new StringBuilder();
        asString.append("[");
        asString.append("charsetName=");
        asString.append(this.charsetName);
        asString.append(",mblen=");
        asString.append(this.mblen);
        asString.append("]");
        return asString.toString();
    }
    
    boolean isOkayForVersion(final Connection conn) throws SQLException {
        return conn.versionMeetsMinimum(this.major, this.minor, this.subminor);
    }
    
    String getMatchingJavaEncoding(final String javaEncoding) {
        if (javaEncoding != null && this.javaEncodingsUc.contains(javaEncoding.toUpperCase(Locale.ENGLISH))) {
            return javaEncoding;
        }
        return this.javaEncodingsUc.get(0);
    }
}
