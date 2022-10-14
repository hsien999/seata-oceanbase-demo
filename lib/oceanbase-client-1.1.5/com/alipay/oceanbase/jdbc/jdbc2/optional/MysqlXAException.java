// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdbc2.optional;

import javax.transaction.xa.XAException;

class MysqlXAException extends XAException
{
    private static final long serialVersionUID = -9075817535836563004L;
    private String message;
    protected String xidAsString;
    
    public MysqlXAException(final int errorCode, final String message, final String xidAsString) {
        super(errorCode);
        this.message = message;
        this.xidAsString = xidAsString;
    }
    
    public MysqlXAException(final String message, final String xidAsString) {
        this.message = message;
        this.xidAsString = xidAsString;
    }
    
    @Override
    public String getMessage() {
        final String superMessage = super.getMessage();
        final StringBuilder returnedMessage = new StringBuilder();
        if (superMessage != null) {
            returnedMessage.append(superMessage);
            returnedMessage.append(":");
        }
        if (this.message != null) {
            returnedMessage.append(this.message);
        }
        return returnedMessage.toString();
    }
}
