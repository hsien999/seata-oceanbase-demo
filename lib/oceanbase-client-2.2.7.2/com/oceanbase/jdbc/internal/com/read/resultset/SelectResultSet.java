// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.resultset;

import java.util.List;
import java.sql.SQLException;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.JDBC4ResultSet;

public class SelectResultSet extends JDBC4ResultSet
{
    public SelectResultSet(final ColumnDefinition[] columnDefinition, final Results results, final Protocol protocol, final PacketInputStream reader, final boolean callableResult, final boolean eofDeprecated, final boolean isPsOutParameter) throws IOException, SQLException {
        super(columnDefinition, results, protocol, reader, callableResult, eofDeprecated, isPsOutParameter);
    }
    
    public SelectResultSet(final ColumnDefinition[] columnDefinition, final Results results, final Protocol protocol, final boolean callableResult, final boolean eofDeprecated, final boolean isPsOutParameter) throws IOException, SQLException {
        super(columnDefinition, results, protocol, callableResult, eofDeprecated, isPsOutParameter);
    }
    
    public SelectResultSet(final ColumnDefinition[] columnDefinition, final List<byte[]> resultSet, final Protocol protocol, final int resultSetScrollType) {
        super(columnDefinition, resultSet, protocol, resultSetScrollType);
    }
}
