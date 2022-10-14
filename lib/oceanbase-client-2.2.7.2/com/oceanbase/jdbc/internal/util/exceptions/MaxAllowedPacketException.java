// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.exceptions;

import java.io.IOException;

public class MaxAllowedPacketException extends IOException
{
    private final boolean mustReconnect;
    
    public MaxAllowedPacketException(final String message, final boolean mustReconnect) {
        super(message);
        this.mustReconnect = mustReconnect;
    }
    
    public boolean isMustReconnect() {
        return this.mustReconnect;
    }
}
