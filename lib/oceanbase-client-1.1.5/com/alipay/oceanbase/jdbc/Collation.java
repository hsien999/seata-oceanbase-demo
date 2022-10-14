// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

class Collation
{
    public final int index;
    public final String collationName;
    public final int priority;
    public final MysqlCharset mysqlCharset;
    
    public Collation(final int index, final String collationName, final int priority, final String charsetName) {
        this.index = index;
        this.collationName = collationName;
        this.priority = priority;
        this.mysqlCharset = CharsetMapping.CHARSET_NAME_TO_CHARSET.get(charsetName);
    }
    
    @Override
    public String toString() {
        final StringBuilder asString = new StringBuilder();
        asString.append("[");
        asString.append("index=");
        asString.append(this.index);
        asString.append(",collationName=");
        asString.append(this.collationName);
        asString.append(",charsetName=");
        asString.append(this.mysqlCharset.charsetName);
        asString.append(",javaCharsetName=");
        asString.append(this.mysqlCharset.getMatchingJavaEncoding(null));
        asString.append("]");
        return asString.toString();
    }
}
