// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import com.alipay.oceanbase.jdbc.extend.datatype.NUMBER;
import com.alipay.oceanbase.jdbc.extend.datatype.INTERVALDS;
import com.alipay.oceanbase.jdbc.extend.datatype.INTERVALYM;
import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMPLTZ;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.extend.datatype.TIMESTAMPTZ;
import java.sql.ResultSet;

public interface ObResultSet extends ResultSet
{
    TIMESTAMPTZ getTIMESTAMPTZ(final int p0) throws SQLException;
    
    TIMESTAMPTZ getTIMESTAMPTZ(final String p0) throws SQLException;
    
    TIMESTAMPLTZ getTIMESTAMPLTZ(final int p0) throws SQLException;
    
    TIMESTAMPLTZ getTIMESTAMPLTZ(final String p0) throws SQLException;
    
    INTERVALYM getINTERVALYM(final int p0) throws SQLException;
    
    INTERVALYM getINTERVALYM(final String p0) throws SQLException;
    
    INTERVALDS getINTERVALDS(final int p0) throws SQLException;
    
    INTERVALDS getINTERVALDS(final String p0) throws SQLException;
    
    NUMBER getNUMBER(final int p0) throws SQLException;
    
    NUMBER getNUMBER(final String p0) throws SQLException;
}
