// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.feedback;

import com.alipay.oceanbase.jdbc.Buffer;

public class ObFeedBackBuffer extends Buffer
{
    private int currentUpperPosLimit;
    
    public ObFeedBackBuffer(final byte[] buf) {
        super(buf);
        this.currentUpperPosLimit = Integer.MAX_VALUE;
    }
    
    public boolean isValid() {
        return null != this.getBufferSource() && this.getPosition() >= 0 && this.getPosition() < this.getBufLength();
    }
    
    public boolean hasRemainData() {
        return this.getBufLength() > this.getPosition();
    }
    
    public void setCurrentUpperPosLimit(final int currentUpperPosLimit) {
        this.currentUpperPosLimit = currentUpperPosLimit;
    }
    
    public ObLongValue readVariableLong() throws ObFBOutOfBoundException {
        final ObLongValue tmpLong = new ObLongValue();
        if (this.getPosition() >= this.currentUpperPosLimit) {
            tmpLong.isRead = false;
        }
        else {
            tmpLong.valueLong = this.newReadLength();
            tmpLong.isRead = true;
            if (this.getPosition() > this.currentUpperPosLimit) {
                throw new ObFBOutOfBoundException(String.format("pos = %s, currentUpperPosLimit=%s", this.getPosition(), this.currentUpperPosLimit));
            }
        }
        return tmpLong;
    }
}
