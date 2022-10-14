// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.feedback;

import com.alipay.oceanbase.jdbc.log.LogFactory;
import java.util.concurrent.ConcurrentHashMap;
import com.alipay.oceanbase.jdbc.log.Log;

public class ObFeedbackManager
{
    private transient Log log;
    private ConcurrentHashMap<ObFeedbackType, ObFBElement> fbMap;
    
    public ObFeedbackManager() {
        this.log = LogFactory.getLogger();
        this.fbMap = new ConcurrentHashMap<ObFeedbackType, ObFBElement>();
    }
    
    public void decode(final ObFeedBackBuffer buffer) {
        try {
            if (buffer.isValid()) {
                while (buffer.hasRemainData()) {
                    final long type = buffer.newReadLength();
                    final ObFBElement ele = this.getFBElement(type);
                    ele.decodeFbElement(buffer);
                    if (ele.isValid()) {
                        this.fbMap.put(ele.getType(), ele);
                    }
                }
            }
            this.log.logInfo(String.format("FeedbackManager decode succ, %s", this.fbMap));
        }
        catch (Exception e) {
            this.log.logWarn("fail to decode", e);
            this.reset();
        }
    }
    
    public void reset() {
        this.fbMap.clear();
    }
    
    private ObFBElement getFBElement(final long type) {
        if (type == ObFeedbackType.PARTITION_LOCATION_FB_ELE.getIndex()) {
            return new ObFBLocation();
        }
        if (type == ObFeedbackType.FOLLOWER_FIRST_FB_ELE.getIndex()) {
            return new ObFBFollowerFirst();
        }
        return new ObFBUnknown();
    }
    
    public ObFBElement getFBElement(final ObFeedbackType type) {
        ObFBElement ele = null;
        if (null != type) {
            ele = this.fbMap.get(type);
        }
        return ele;
    }
    
    public int getFBElementCount() {
        return this.fbMap.size();
    }
    
    @Override
    public String toString() {
        return "ObFeedbackManager{log=" + this.log + ", fbMap=" + this.fbMap + '}';
    }
}
