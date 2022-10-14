// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.extend.datatype.ComplexDataType;

interface ObConnection extends MySQLConnection
{
    String getSessionTimeZone();
    
    ComplexDataType getComplexDataType(final String p0) throws SQLException;
    
    ComplexDataType getComplexDataTypeFromCache(final String p0);
    
    ComplexDataType getComplexDataTypeFromRemote(final String p0) throws SQLException;
    
    void recacheComplexDataType(final ComplexDataType p0);
    
    long getObGroupDatasourceId();
    
    void setObGroupDatasourceId(final long p0);
    
    long getLocalConnectionId();
    
    boolean isOracleMode() throws SQLException;
}
