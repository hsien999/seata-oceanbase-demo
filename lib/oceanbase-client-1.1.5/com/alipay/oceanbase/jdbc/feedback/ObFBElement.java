// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.feedback;

public abstract class ObFBElement
{
    public ObFeedbackType type;
    
    public ObFBElement(final ObFeedbackType type) {
        this.type = ObFeedbackType.UNKNOWN_FB_ELE;
        this.type = type;
    }
    
    public void decodeFbElement(final ObFeedBackBuffer buffer) throws ObFBOutOfBoundException {
        final long len = buffer.newReadLength();
        final int originPos = buffer.getPosition();
        buffer.setCurrentUpperPosLimit(originPos + (int)len);
        this.decode(buffer);
        buffer.setPosition(originPos + (int)len);
        buffer.setCurrentUpperPosLimit(Integer.MAX_VALUE);
    }
    
    public abstract void decode(final ObFeedBackBuffer p0) throws ObFBOutOfBoundException;
    
    public boolean isValid() {
        return true;
    }
    
    public ObFeedbackType getType() {
        return this.type;
    }
}
