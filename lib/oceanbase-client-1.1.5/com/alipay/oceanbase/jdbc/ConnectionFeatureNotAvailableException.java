// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

public class ConnectionFeatureNotAvailableException extends CommunicationsException
{
    static final long serialVersionUID = -5065030488729238287L;
    
    public ConnectionFeatureNotAvailableException(final MySQLConnection conn, final long lastPacketSentTimeMs, final Exception underlyingException) {
        super(conn, lastPacketSentTimeMs, 0L, underlyingException);
    }
    
    @Override
    public String getMessage() {
        return "Feature not available in this distribution of Connector/J";
    }
    
    @Override
    public String getSQLState() {
        return "01S00";
    }
}
