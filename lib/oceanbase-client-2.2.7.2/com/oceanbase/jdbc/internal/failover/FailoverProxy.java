// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.failover;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.HostAddress;
import java.lang.reflect.InvocationTargetException;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import com.oceanbase.jdbc.OceanBaseStatement;
import com.oceanbase.jdbc.OceanBaseConnection;
import java.lang.reflect.Method;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.io.LruTraceCache;
import java.util.concurrent.locks.ReentrantLock;
import com.oceanbase.jdbc.internal.logging.Logger;
import java.lang.reflect.InvocationHandler;

public class FailoverProxy implements InvocationHandler
{
    private static final String METHOD_IS_EXPLICIT_CLOSED = "isExplicitClosed";
    private static final String METHOD_GET_OPTIONS = "getOptions";
    private static final String METHOD_GET_URLPARSER = "getUrlParser";
    private static final String METHOD_GET_PROXY = "getProxy";
    private static final String METHOD_EXECUTE_QUERY = "executeQuery";
    private static final String METHOD_SET_READ_ONLY = "setReadonly";
    private static final String METHOD_GET_READ_ONLY = "getReadonly";
    private static final String METHOD_IS_MASTER_CONNECTION = "isMasterConnection";
    private static final String METHOD_VERSION_GREATER_OR_EQUAL = "versionGreaterOrEqual";
    private static final String METHOD_SESSION_STATE_AWARE = "sessionStateAware";
    private static final String METHOD_CLOSED_EXPLICIT = "closeExplicit";
    private static final String METHOD_ABORT = "abort";
    private static final String METHOD_IS_CLOSED = "isClosed";
    private static final String METHOD_EXECUTE_PREPARED_QUERY = "executePreparedQuery";
    private static final String METHOD_COM_MULTI_PREPARE_EXECUTES = "prepareAndExecutesComMulti";
    private static final String METHOD_PROLOG_PROXY = "prologProxy";
    private static final String METHOD_RESET = "reset";
    private static final String METHOD_IS_VALID = "isValid";
    private static final String METHOD_GET_LOCK = "getLock";
    private static final String METHOD_GET_NO_BACKSLASH = "noBackslashEscapes";
    private static final String METHOD_GET_SERVER_THREAD_ID = "getServerThreadId";
    private static final String METHOD_PROLOG = "prolog";
    private static final String METHOD_GET_CATALOG = "getCatalog";
    private static final String METHOD_GET_TIMEOUT = "getTimeout";
    private static final String METHOD_GET_MAJOR_VERSION = "getMajorServerVersion";
    private static final String METHOD_IN_TRANSACTION = "inTransaction";
    private static final String METHOD_IS_MARIADB = "isServerMariaDb";
    private static final Logger logger;
    public final ReentrantLock lock;
    public final LruTraceCache traceCache;
    private final Listener listener;
    
    public FailoverProxy(final Listener listener, final ReentrantLock lock, final LruTraceCache traceCache) throws SQLException {
        this.lock = lock;
        (this.listener = listener).setProxy(this);
        this.traceCache = traceCache;
        this.listener.initializeConnection();
    }
    
    private static SQLException addHostInformationToException(final SQLException exception, final Protocol protocol) {
        if (protocol != null) {
            return new SQLException(exception.getMessage() + "\non " + protocol.getHostAddress().toString() + ",master=" + protocol.isMasterConnection(), exception.getSQLState(), exception.getErrorCode(), exception.getCause());
        }
        return exception;
    }
    
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String name;
        final String methodName = name = method.getName();
        int n = -1;
        switch (name.hashCode()) {
            case -75354719: {
                if (name.equals("getLock")) {
                    n = 0;
                    break;
                }
                break;
            }
            case 1558077981: {
                if (name.equals("noBackslashEscapes")) {
                    n = 1;
                    break;
                }
                break;
            }
            case -312044473: {
                if (name.equals("isServerMariaDb")) {
                    n = 2;
                    break;
                }
                break;
            }
            case 1592564707: {
                if (name.equals("getCatalog")) {
                    n = 3;
                    break;
                }
                break;
            }
            case -277051093: {
                if (name.equals("getTimeout")) {
                    n = 4;
                    break;
                }
                break;
            }
            case 1873988783: {
                if (name.equals("versionGreaterOrEqual")) {
                    n = 5;
                    break;
                }
                break;
            }
            case -1699572861: {
                if (name.equals("sessionStateAware")) {
                    n = 6;
                    break;
                }
                break;
            }
            case 1401570442: {
                if (name.equals("isExplicitClosed")) {
                    n = 7;
                    break;
                }
                break;
            }
            case -212614552: {
                if (name.equals("getOptions")) {
                    n = 8;
                    break;
                }
                break;
            }
            case -975563566: {
                if (name.equals("getMajorServerVersion")) {
                    n = 9;
                    break;
                }
                break;
            }
            case 1351887390: {
                if (name.equals("getServerThreadId")) {
                    n = 10;
                    break;
                }
                break;
            }
            case -466923592: {
                if (name.equals("getUrlParser")) {
                    n = 11;
                    break;
                }
                break;
            }
            case 1962766520: {
                if (name.equals("getProxy")) {
                    n = 12;
                    break;
                }
                break;
            }
            case -683486410: {
                if (name.equals("isClosed")) {
                    n = 13;
                    break;
                }
                break;
            }
            case 2073378034: {
                if (name.equals("isValid")) {
                    n = 14;
                    break;
                }
                break;
            }
            case -979806857: {
                if (name.equals("prolog")) {
                    n = 15;
                    break;
                }
                break;
            }
            case -1359179181: {
                if (name.equals("executeQuery")) {
                    n = 16;
                    break;
                }
                break;
            }
            case 1122589892: {
                if (name.equals("setReadonly")) {
                    n = 17;
                    break;
                }
                break;
            }
            case 1211169720: {
                if (name.equals("getReadonly")) {
                    n = 18;
                    break;
                }
                break;
            }
            case 85800057: {
                if (name.equals("inTransaction")) {
                    n = 19;
                    break;
                }
                break;
            }
            case -357814230: {
                if (name.equals("isMasterConnection")) {
                    n = 20;
                    break;
                }
                break;
            }
            case 92611376: {
                if (name.equals("abort")) {
                    n = 21;
                    break;
                }
                break;
            }
            case -677171124: {
                if (name.equals("closeExplicit")) {
                    n = 22;
                    break;
                }
                break;
            }
            case -773017242: {
                if (name.equals("prepareAndExecutesComMulti")) {
                    n = 23;
                    break;
                }
                break;
            }
            case 139555094: {
                if (name.equals("executePreparedQuery")) {
                    n = 24;
                    break;
                }
                break;
            }
            case -1381935305: {
                if (name.equals("prologProxy")) {
                    n = 25;
                    break;
                }
                break;
            }
            case 108404047: {
                if (name.equals("reset")) {
                    n = 26;
                    break;
                }
                break;
            }
        }
        Label_1375: {
            switch (n) {
                case 0: {
                    return this.lock;
                }
                case 1: {
                    return this.listener.noBackslashEscapes();
                }
                case 2: {
                    return this.listener.isServerMariaDb();
                }
                case 3: {
                    return this.listener.getCatalog();
                }
                case 4: {
                    return this.listener.getTimeout();
                }
                case 5: {
                    return this.listener.versionGreaterOrEqual((int)args[0], (int)args[1], (int)args[2]);
                }
                case 6: {
                    return this.listener.sessionStateAware();
                }
                case 7: {
                    return this.listener.isExplicitClosed();
                }
                case 8: {
                    return this.listener.getUrlParser().getOptions();
                }
                case 9: {
                    return this.listener.getMajorServerVersion();
                }
                case 10: {
                    return this.listener.getServerThreadId();
                }
                case 11: {
                    return this.listener.getUrlParser();
                }
                case 12: {
                    return this;
                }
                case 13: {
                    return this.listener.isClosed();
                }
                case 14: {
                    return this.listener.isValid((int)args[0]);
                }
                case 15: {
                    this.listener.prolog((long)args[0], (OceanBaseConnection)args[2], (OceanBaseStatement)args[3]);
                    return null;
                }
                case 16: {
                    final boolean isClosed = this.listener.isClosed();
                    try {
                        this.listener.preExecute();
                    }
                    catch (SQLException e) {
                        if (this.hasToHandleFailover(e)) {
                            return this.handleFailOver(e, method, args, this.listener.getCurrentProtocol(), isClosed);
                        }
                    }
                    break;
                }
                case 17: {
                    this.listener.switchReadOnlyConnection((Boolean)args[0]);
                    return null;
                }
                case 18: {
                    return this.listener.isReadOnly();
                }
                case 19: {
                    return this.listener.inTransaction();
                }
                case 20: {
                    return this.listener.isMasterConnection();
                }
                case 21: {
                    this.listener.preAbort();
                    return null;
                }
                case 22: {
                    this.listener.preClose();
                    return null;
                }
                case 23:
                case 24: {
                    final boolean mustBeOnMaster = (boolean)args[0];
                    final ServerPrepareResult serverPrepareResult = (ServerPrepareResult)args[1];
                    if (serverPrepareResult != null) {
                        if (!mustBeOnMaster && serverPrepareResult.getUnProxiedProtocol().isMasterConnection() && !this.listener.hasHostFail()) {
                            try {
                                FailoverProxy.logger.trace("re-prepare query \"{}\" on slave (was temporary on master since failover)", serverPrepareResult.getSql());
                                this.listener.rePrepareOnSlave(serverPrepareResult, false);
                            }
                            catch (SQLException ex) {}
                        }
                        final boolean wasClosed = this.listener.isClosed();
                        try {
                            return this.listener.invoke(method, args, serverPrepareResult.getUnProxiedProtocol());
                        }
                        catch (InvocationTargetException e2) {
                            if (e2.getTargetException() == null) {
                                throw e2;
                            }
                            if (e2.getTargetException() instanceof SQLException && this.hasToHandleFailover((SQLException)e2.getTargetException())) {
                                return this.handleFailOver((SQLException)e2.getTargetException(), method, args, serverPrepareResult.getUnProxiedProtocol(), wasClosed);
                            }
                            throw e2.getTargetException();
                        }
                        break Label_1375;
                    }
                    break;
                }
                case 25: {
                    final boolean wasClosed = this.listener.isClosed();
                    try {
                        if (args[0] != null) {
                            return this.listener.invoke(method, args, ((ServerPrepareResult)args[0]).getUnProxiedProtocol());
                        }
                        return null;
                    }
                    catch (InvocationTargetException e2) {
                        if (e2.getTargetException() == null) {
                            throw e2;
                        }
                        if (e2.getTargetException() instanceof SQLException && this.hasToHandleFailover((SQLException)e2.getTargetException())) {
                            return this.handleFailOver((SQLException)e2.getTargetException(), method, args, ((ServerPrepareResult)args[0]).getUnProxiedProtocol(), wasClosed);
                        }
                        throw e2.getTargetException();
                    }
                }
                case 26: {
                    this.listener.reset();
                    return null;
                }
            }
        }
        return this.executeInvocation(method, args, false);
    }
    
    private Object executeInvocation(final Method method, final Object[] args, final boolean isSecondExecution) throws Throwable {
        final boolean isClosed = this.listener.isClosed();
        try {
            return this.listener.invoke(method, args);
        }
        catch (InvocationTargetException e) {
            if (e.getTargetException() != null) {
                if (e.getTargetException() instanceof SQLException) {
                    SQLException queryException = (SQLException)e.getTargetException();
                    final Protocol protocol = this.listener.getCurrentProtocol();
                    queryException = addHostInformationToException(queryException, protocol);
                    final boolean killCmd = queryException != null && queryException.getSQLState() != null && queryException.getSQLState().equals("70100") && 1927 == queryException.getErrorCode();
                    if (killCmd) {
                        this.handleFailOver(queryException, method, args, protocol, isClosed);
                        return null;
                    }
                    if (this.hasToHandleFailover(queryException)) {
                        return this.handleFailOver(queryException, method, args, protocol, isClosed);
                    }
                    if (queryException.getErrorCode() == 1290 && !isSecondExecution && protocol != null && protocol.isMasterConnection() && !protocol.checkIfMaster()) {
                        final boolean inTransaction = protocol.inTransaction();
                        this.lock.lock();
                        boolean isReconnected;
                        try {
                            protocol.close();
                            isReconnected = this.listener.primaryFail(null, null, false, isClosed).isReconnected;
                        }
                        finally {
                            this.lock.unlock();
                        }
                        if (isReconnected && !inTransaction) {
                            return this.executeInvocation(method, args, true);
                        }
                        return this.handleFailOver(queryException, method, args, this.listener.getCurrentProtocol(), isClosed);
                    }
                }
                throw e.getTargetException();
            }
            throw e;
        }
    }
    
    private Object handleFailOver(final SQLException qe, final Method method, final Object[] args, final Protocol protocol, final boolean isClosed) throws Throwable {
        HostAddress failHostAddress = null;
        boolean failIsMaster = true;
        if (protocol != null) {
            failHostAddress = protocol.getHostAddress();
            failIsMaster = protocol.isMasterConnection();
        }
        final HandleErrorResult handleErrorResult = this.listener.handleFailover(qe, method, args, protocol, isClosed);
        if (handleErrorResult.mustThrowError) {
            this.listener.throwFailoverMessage(failHostAddress, failIsMaster, qe, handleErrorResult.isReconnected);
        }
        return handleErrorResult.resultObject;
    }
    
    public boolean hasToHandleFailover(final SQLException exception) {
        return exception.getSQLState() != null && (exception.getSQLState().startsWith("08") || (exception.getSQLState().equals("70100") && 1927 == exception.getErrorCode()));
    }
    
    public void reconnect() throws SQLException {
        try {
            this.listener.reconnect();
        }
        catch (SQLException e) {
            throw ExceptionFactory.INSTANCE.create(e);
        }
    }
    
    public Listener getListener() {
        return this.listener;
    }
    
    static {
        logger = LoggerFactory.getLogger(FailoverProxy.class);
    }
}
