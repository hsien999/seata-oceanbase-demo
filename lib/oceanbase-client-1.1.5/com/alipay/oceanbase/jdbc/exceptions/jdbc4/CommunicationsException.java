// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.exceptions.jdbc4;

import com.alipay.oceanbase.jdbc.Messages;
import com.alipay.oceanbase.jdbc.SQLError;
import com.alipay.oceanbase.jdbc.MySQLConnection;
import com.alipay.oceanbase.jdbc.StreamingNotifiable;
import java.sql.SQLRecoverableException;

public class CommunicationsException extends SQLRecoverableException implements StreamingNotifiable
{
    static final long serialVersionUID = 4317904269797988677L;
    private String exceptionMessage;
    
    public CommunicationsException(final MySQLConnection conn, final long lastPacketSentTimeMs, final long lastPacketReceivedTimeMs, final Exception underlyingException) {
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
