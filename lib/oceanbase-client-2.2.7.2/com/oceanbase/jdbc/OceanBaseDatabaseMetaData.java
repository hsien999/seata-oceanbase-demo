// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.Connection;

public class OceanBaseDatabaseMetaData extends JDBC4DatabaseMetaData
{
    public OceanBaseDatabaseMetaData(final Connection connection, final UrlParser urlParser) {
        super(connection, urlParser);
    }
}
