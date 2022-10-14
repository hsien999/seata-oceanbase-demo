// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.resultset;

import java.sql.SQLException;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.JDBC4UpdatableResultSet;

public class UpdatableResultSet extends JDBC4UpdatableResultSet
{
    public UpdatableResultSet(final ColumnDefinition[] columnsInformation, final Results results, final Protocol protocol, final PacketInputStream reader, final boolean callableResult, final boolean eofDeprecated, final boolean isPsOutParamter) throws IOException, SQLException {
        super(columnsInformation, results, protocol, reader, callableResult, eofDeprecated, isPsOutParamter);
    }
}
