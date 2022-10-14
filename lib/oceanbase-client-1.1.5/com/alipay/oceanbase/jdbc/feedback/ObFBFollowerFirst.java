// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.feedback;

public class ObFBFollowerFirst extends ObFBElement
{
    ObFollowerFirstFeedbackType fffType;
    
    public ObFBFollowerFirst() {
        super(ObFeedbackType.FOLLOWER_FIRST_FB_ELE);
        this.fffType = ObFollowerFirstFeedbackType.FFF_HIT_MIN;
    }
    
    @Override
    public void decode(final ObFeedBackBuffer buffer) throws ObFBOutOfBoundException {
        final ObLongValue value = buffer.readVariableLong();
        if (value.isRead) {
            this.fffType = ObFollowerFirstFeedbackType.getType(value.valueLong);
        }
    }
    
    @Override
    public boolean isValid() {
        return ObFollowerFirstFeedbackType.FFF_HIT_LEADER == this.fffType;
    }
    
    public ObFollowerFirstFeedbackType getFffType() {
        return this.fffType;
    }
    
    public enum ObFollowerFirstFeedbackType
    {
        FFF_HIT_MIN, 
        FFF_HIT_LEADER;
        
        public static ObFollowerFirstFeedbackType getType(final long idx) {
            if (1L == idx) {
                return ObFollowerFirstFeedbackType.FFF_HIT_LEADER;
            }
            return ObFollowerFirstFeedbackType.FFF_HIT_MIN;
        }
    }
}
