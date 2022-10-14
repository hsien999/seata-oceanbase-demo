// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.dao;

import java.sql.ResultSet;
import com.oceanbase.jdbc.internal.protocol.Protocol;

public interface CmdInformation
{
    public static final int RESULT_SET_VALUE = -1;
    
    int[] getUpdateCounts();
    
    int[] getServerUpdateCounts();
    
    long[] getLargeUpdateCounts();
    
    int getUpdateCount();
    
    long getLargeUpdateCount();
    
    void addSuccessStat(final long p0, final long p1);
    
    void addErrorStat();
    
    void reset();
    
    void addResultSetStat();
    
    ResultSet getGeneratedKeys(final Protocol p0, final String p1);
    
    ResultSet getBatchGeneratedKeys(final Protocol p0);
    
    int getCurrentStatNumber();
    
    boolean moreResults();
    
    boolean isCurrentUpdateCount();
    
    void setRewrite(final boolean p0);
}
