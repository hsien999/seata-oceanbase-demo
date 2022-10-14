// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;

public class CommunicationsException extends SQLException implements StreamingNotifiable
{
    static final long serialVersionUID = 3193864990663398317L;
    private String exceptionMessage;
    
    public CommunicationsException(final MySQLConnection conn, final long lastPacketSentTimeMs, final long lastPacketReceivedTimeMs, final Exception underlyingException) {
        this.exceptionMessage = null;
        this.exceptionMessage = SQLError.createLinkFailureMessageBasedOnHeuristics(conn, lastPacketSentTimeMs, lastPacketReceivedTimeMs, underlyingException);
        if (underlyingException != null) {
            this.initCause(underlyingException);
        }
    }
    
    @Override
    public String getMessage() {
        return this.exceptionMessage;
    }
    
    @Override
    public String getSQLState() {
        return "08S01";
    }
    
    @Override
    public void setWasStreamingResults() {
        this.exceptionMessage = Messages.getString("CommunicationsException.ClientWasStreaming");
    }
}
