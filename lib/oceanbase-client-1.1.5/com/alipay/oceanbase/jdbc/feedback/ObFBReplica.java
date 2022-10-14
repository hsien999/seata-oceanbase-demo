// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.feedback;

import com.alipay.oceanbase.jdbc.StringUtils;
import com.alipay.oceanbase.jdbc.util.MysqlCommonUtils;

public class ObFBReplica extends ObFBElement
{
    private String ip;
    private long port;
    private int role;
    private int type;
    
    public ObFBReplica() {
        super(ObFeedbackType.UNKNOWN_FB_ELE);
        this.role = 0;
        this.type = -1;
    }
    
    @Override
    public void decode(final ObFeedBackBuffer buffer) throws ObFBOutOfBoundException {
        boolean useIPV4 = true;
        ObLongValue value = buffer.readVariableLong();
        if (value.isRead) {
            useIPV4 = (4L == value.valueLong);
        }
        if (useIPV4) {
            value = buffer.readVariableLong();
            if (value.isRead) {
                this.ip = MysqlCommonUtils.long2Ip(value.valueLong);
            }
            value = buffer.readVariableLong();
            if (value.isRead) {
                this.port = value.valueLong;
            }
            value = buffer.readVariableLong();
            if (value.isRead) {
                this.role = (int)value.valueLong;
            }
            value = buffer.readVariableLong();
            if (value.isRead) {
                this.type = (int)value.valueLong;
            }
        }
    }
    
    @Override
    public boolean isValid() {
        return this.port > 0L && !StringUtils.isEmpty(this.ip) && 0 != this.role && -1 != this.type;
    }
    
    public String getIp() {
        return this.ip;
    }
    
    public long getPort() {
        return this.port;
    }
    
    public int getRole() {
        return this.role;
    }
    
    public int getReplicaType() {
        return this.type;
    }
    
    @Override
    public String toString() {
        return "ObFBReplica{ip='" + this.ip + '\'' + ", port=" + this.port + ", role=" + this.role + ", type=" + this.type + ", type=" + this.type + '}';
    }
}
