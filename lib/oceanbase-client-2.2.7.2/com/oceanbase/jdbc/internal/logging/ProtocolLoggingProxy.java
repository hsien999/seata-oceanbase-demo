// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.logging;

import java.util.Iterator;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import java.util.List;
import com.oceanbase.jdbc.internal.util.dao.PrepareResult;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import com.oceanbase.jdbc.internal.util.dao.ClientPrepareResult;
import java.nio.charset.Charset;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import java.text.NumberFormat;
import java.lang.reflect.InvocationHandler;

public class ProtocolLoggingProxy implements InvocationHandler
{
    private static final Logger logger;
    private final NumberFormat numberFormat;
    private final boolean profileSql;
    private final Long slowQueryThresholdNanos;
    private final int maxQuerySizeToLog;
    private final Protocol protocol;
    
    public ProtocolLoggingProxy(final Protocol protocol, final Options options) {
        this.protocol = protocol;
        this.profileSql = options.profileSql;
        this.slowQueryThresholdNanos = options.slowQueryThresholdNanos;
        this.maxQuerySizeToLog = options.maxQuerySizeToLog;
        this.numberFormat = NumberFormat.getInstance();
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        try {
            final String name = method.getName();
            switch (name) {
                case "executeQuery":
                case "executePreparedQuery":
                case "executeBatchStmt":
                case "executeBatchClient":
                case "executeBatchServer": {
                    final long startTime = System.nanoTime();
                    final Object returnObj = method.invoke(this.protocol, args);
                    if (ProtocolLoggingProxy.logger.isInfoEnabled() && (this.profileSql || (this.slowQueryThresholdNanos != null && System.nanoTime() - startTime > this.slowQueryThresholdNanos))) {
                        final String sql = this.logQuery(method.getName(), args);
                        ProtocolLoggingProxy.logger.info("conn={}({}) - {} ms - Query: {}", this.protocol.getServerThreadId(), this.protocol.isMasterConnection() ? "M" : "S", this.numberFormat.format((System.nanoTime() - (double)startTime) / 1000000.0), this.subQuery(sql));
                    }
                    return returnObj;
                }
                default: {
                    return method.invoke(this.protocol, args);
                }
            }
        }
        catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
    
    private String logQuery(final String methodName, final Object[] args) {
        int n = -1;
        switch (methodName.hashCode()) {
            case -1359179181: {
                if (methodName.equals("executeQuery")) {
                    n = 0;
                    break;
                }
                break;
            }
            case -1236809488: {
                if (methodName.equals("executeBatchClient")) {
                    n = 1;
                    break;
                }
                break;
            }
            case 276292141: {
                if (methodName.equals("executeBatchStmt")) {
                    n = 2;
                    break;
                }
                break;
            }
            case -784923544: {
                if (methodName.equals("executeBatchServer")) {
                    n = 3;
                    break;
                }
                break;
            }
            case 139555094: {
                if (methodName.equals("executePreparedQuery")) {
                    n = 4;
                    break;
                }
                break;
            }
        }
        Label_0556: {
            switch (n) {
                case 0: {
                    switch (args.length) {
                        case 1: {
                            return (String)args[0];
                        }
                        case 3: {
                            return (String)args[2];
                        }
                        case 4:
                        case 5: {
                            if (args[3] instanceof Charset) {
                                return (String)args[2];
                            }
                            final ClientPrepareResult clientPrepareResult = (ClientPrepareResult)args[2];
                            return this.getQueryFromPrepareParameters(clientPrepareResult, (ParameterHolder[])args[3], clientPrepareResult.getParamCount());
                        }
                        default: {
                            break Label_0556;
                        }
                    }
                    break;
                }
                case 1: {
                    final ClientPrepareResult clientPrepareResult = (ClientPrepareResult)args[2];
                    return this.getQueryFromPrepareParameters(clientPrepareResult.getSql(), (List<ParameterHolder[]>)args[3], clientPrepareResult.getParamCount());
                }
                case 2: {
                    final List<String> multipleQueries = (List<String>)args[2];
                    if (multipleQueries.size() == 1) {
                        return multipleQueries.get(0);
                    }
                    final StringBuilder sb = new StringBuilder();
                    for (final String multipleQuery : multipleQueries) {
                        if (this.maxQuerySizeToLog > 0 && sb.length() + multipleQuery.length() + 1 > this.maxQuerySizeToLog) {
                            sb.append(multipleQuery, 1, Math.max(1, this.maxQuerySizeToLog - sb.length()));
                            break;
                        }
                        sb.append(multipleQuery).append(";");
                        if (this.maxQuerySizeToLog > 0 && sb.length() >= this.maxQuerySizeToLog) {
                            break;
                        }
                    }
                    return sb.toString();
                }
                case 3: {
                    final List<ParameterHolder[]> parameterList = (List<ParameterHolder[]>)args[4];
                    final ServerPrepareResult serverPrepareResult = (ServerPrepareResult)args[1];
                    return this.getQueryFromPrepareParameters(serverPrepareResult.getSql(), parameterList, serverPrepareResult.getParamCount());
                }
                case 4: {
                    final ServerPrepareResult prepareResult = (ServerPrepareResult)args[1];
                    if (args[3] instanceof ParameterHolder[]) {
                        return this.getQueryFromPrepareParameters(prepareResult, (ParameterHolder[])args[3], prepareResult.getParamCount());
                    }
                    return this.getQueryFromPrepareParameters(prepareResult.getSql(), (List<ParameterHolder[]>)args[3], prepareResult.getParameters().length);
                }
            }
        }
        return "-unknown-";
    }
    
    public String subQuery(final String sql) {
        if (this.maxQuerySizeToLog > 0 && sql.length() > this.maxQuerySizeToLog - 3) {
            return sql.substring(0, this.maxQuerySizeToLog - 3) + "...";
        }
        return sql;
    }
    
    private String getQueryFromPrepareParameters(final String sql, final List<ParameterHolder[]> parameterList, final int parameterLength) {
        if (parameterLength == 0) {
            return sql;
        }
        final StringBuilder sb = new StringBuilder(sql).append(", parameters ");
        for (int paramNo = 0; paramNo < parameterList.size(); ++paramNo) {
            final ParameterHolder[] parameters = parameterList.get(paramNo);
            if (paramNo != 0) {
                sb.append(",");
            }
            sb.append("[");
            for (int i = 0; i < parameterLength; ++i) {
                if (i != 0) {
                    sb.append(",");
                }
                sb.append(parameters[i].toString());
            }
            if (this.maxQuerySizeToLog > 0 && sb.length() > this.maxQuerySizeToLog) {
                break;
            }
            sb.append("]");
        }
        return sb.toString();
    }
    
    private String getQueryFromPrepareParameters(final PrepareResult serverPrepareResult, final ParameterHolder[] paramHolders, final int parameterLength) {
        final StringBuilder sb = new StringBuilder(serverPrepareResult.getSql());
        if (paramHolders.length > 0) {
            sb.append(", parameters [");
            for (int i = 0; i < parameterLength; ++i) {
                if (i != 0) {
                    sb.append(",");
                }
                sb.append(paramHolders[i].toString());
                if (this.maxQuerySizeToLog > 0 && sb.length() > this.maxQuerySizeToLog) {
                    break;
                }
            }
            return sb.append("]").toString();
        }
        return serverPrepareResult.getSql();
    }
    
    static {
        logger = LoggerFactory.getLogger(ProtocolLoggingProxy.class);
    }
}
