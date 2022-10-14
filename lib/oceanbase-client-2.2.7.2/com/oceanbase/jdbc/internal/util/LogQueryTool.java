// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util;

import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import com.oceanbase.jdbc.internal.util.dao.PrepareResult;

public class LogQueryTool
{
    public static String queryWithParams(final PrepareResult serverPrepareResult, final ParameterHolder[] parameters, final Options options) {
        if (!options.dumpQueriesOnException || serverPrepareResult == null) {
            return null;
        }
        StringBuilder sql = new StringBuilder(serverPrepareResult.getSql());
        if (serverPrepareResult.getParamCount() > 0 && parameters != null) {
            sql.append(", parameters [");
            if (parameters.length > 0) {
                for (int i = 0; i < Math.min(parameters.length, serverPrepareResult.getParamCount()); ++i) {
                    sql.append(parameters[i].toString()).append(",");
                }
                sql = new StringBuilder(sql.substring(0, sql.length() - 1));
            }
            sql.append("]");
        }
        if (options.maxQuerySizeToLog != 0 && sql.length() > options.maxQuerySizeToLog - 3) {
            return sql.substring(0, options.maxQuerySizeToLog - 3) + "...";
        }
        return sql.toString();
    }
}
