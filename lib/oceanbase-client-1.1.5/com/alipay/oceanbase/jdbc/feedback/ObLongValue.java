// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.feedback;

public class ObLongValue
{
    public long valueLong;
    public boolean isRead;
    
    public ObLongValue() {
        this.valueLong = 0L;
        this.isRead = false;
    }
    
    @Override
    public String toString() {
        return "ObLongValue{valueLong=" + this.valueLong + ", isRead=" + this.isRead + '}';
    }
}
