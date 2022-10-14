// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.feedback;

public class ObFBUnknown extends ObFBElement
{
    public ObFBUnknown() {
        super(ObFeedbackType.UNKNOWN_FB_ELE);
    }
    
    @Override
    public void decode(final ObFeedBackBuffer buffer) {
    }
    
    @Override
    public boolean isValid() {
        return false;
    }
    
    @Override
    public String toString() {
        return "ObFBUnknown{type=" + this.type + '}';
    }
}
