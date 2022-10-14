// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.feedback;

public enum ObFeedbackType
{
    UNKNOWN_FB_ELE(0), 
    PARTITION_LOCATION_FB_ELE(1), 
    FOLLOWER_FIRST_FB_ELE(2);
    
    private int index;
    
    private ObFeedbackType(final int idx) {
        this.index = -1;
        this.index = idx;
    }
    
    public int getIndex() {
        return this.index;
    }
}
