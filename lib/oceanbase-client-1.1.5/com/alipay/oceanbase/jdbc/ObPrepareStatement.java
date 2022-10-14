// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMPTZ;
import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMPLTZ;
import java.sql.Timestamp;
import com.alipay.oceanbase.jdbc.extend.datatype.INTERVALYM;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.extend.datatype.INTERVALDS;
import java.sql.PreparedStatement;

interface ObPrepareStatement extends PreparedStatement
{
    void setINTERVALDS(final int p0, final INTERVALDS p1) throws SQLException;
    
    void setINTERVALYM(final int p0, final INTERVALYM p1) throws SQLException;
    
    void setTIMESTAMP(final int p0, final Timestamp p1) throws SQLException;
    
    void setTIMESTAMPLTZ(final int p0, final TIMESTAMPLTZ p1) throws SQLException;
    
    void setTIMESTAMPTZ(final int p0, final TIMESTAMPTZ p1) throws SQLException;
}
