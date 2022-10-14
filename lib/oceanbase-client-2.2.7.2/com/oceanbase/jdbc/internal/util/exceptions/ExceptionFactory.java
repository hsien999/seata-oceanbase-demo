// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.exceptions;

import java.sql.SQLTransientConnectionException;
import java.sql.SQLTransactionRollbackException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.SQLInvalidAuthorizationSpecException;
import java.sql.SQLSyntaxErrorException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLTimeoutException;
import java.sql.SQLException;
import java.sql.Statement;
import com.oceanbase.jdbc.OceanBaseConnection;
import com.oceanbase.jdbc.util.Options;

public final class ExceptionFactory
{
    public static final ExceptionFactory INSTANCE;
    private final long threadId;
    private final Options options;
    private OceanBaseConnection connection;
    private Statement statement;
    
    public ExceptionFactory(final long threadId, final Options options, final OceanBaseConnection connection, final Statement statement) {
        this.threadId = threadId;
        this.options = options;
        this.connection = connection;
        this.statement = statement;
    }
    
    private ExceptionFactory(final long threadId, final Options options) {
        this.threadId = threadId;
        this.options = options;
    }
    
    public static ExceptionFactory of(final long threadId, final Options options) {
        return new ExceptionFactory(threadId, options);
    }
    
    private static SQLException createException(final String initialMessage, final String sqlState, final int errorCode, final long threadId, final Options options, final OceanBaseConnection connection, final Statement statement, final Exception cause) {
        final String msg = buildMsgText(initialMessage, threadId, options, cause);
        if ("70100".equals(sqlState)) {
            return new SQLTimeoutException(msg, sqlState, errorCode);
        }
        final String s;
        final String sqlClass = s = ((sqlState == null) ? "42" : sqlState.substring(0, 2));
        SQLException returnEx = null;
        switch (s) {
            case "0A": {
                returnEx = new SQLFeatureNotSupportedException(msg, sqlState, errorCode, cause);
                break;
            }
            case "22":
            case "26":
            case "2F":
            case "20":
            case "42":
            case "XA": {
                returnEx = new SQLSyntaxErrorException(msg, sqlState, errorCode, cause);
                break;
            }
            case "25":
            case "28": {
                returnEx = new SQLInvalidAuthorizationSpecException(msg, sqlState, errorCode, cause);
                break;
            }
            case "21":
            case "23": {
                returnEx = new SQLIntegrityConstraintViolationException(msg, sqlState, errorCode, cause);
                break;
            }
            case "08": {
                returnEx = new SQLNonTransientConnectionException(msg, sqlState, errorCode, cause);
                break;
            }
            case "40": {
                returnEx = new SQLTransactionRollbackException(msg, sqlState, errorCode, cause);
                break;
            }
            default: {
                returnEx = new SQLTransientConnectionException(msg, sqlState, errorCode, cause);
                break;
            }
        }
        if (connection != null && connection.pooledConnection != null) {
            connection.pooledConnection.fireStatementErrorOccured(statement, returnEx);
        }
        return returnEx;
    }
    
    private static String buildMsgText(final String initialMessage, final long threadId, final Options options, final Exception cause) {
        final StringBuilder msg = new StringBuilder();
        String deadLockException = null;
        String threadName = null;
        if (threadId != -1L) {
            msg.append("(conn=").append(threadId).append(") ").append(initialMessage);
        }
        else {
            msg.append(initialMessage);
        }
        if (cause instanceof OceanBaseSqlException) {
            final OceanBaseSqlException exception = (OceanBaseSqlException)cause;
            final String sql = exception.getSql();
            if (options.dumpQueriesOnException && sql != null) {
                if (options != null && options.maxQuerySizeToLog != 0 && sql.length() > options.maxQuerySizeToLog - 3) {
                    msg.append("\nQuery is: ").append(sql, 0, options.maxQuerySizeToLog - 3).append("...");
                }
                else {
                    msg.append("\nQuery is: ").append(sql);
                }
            }
            deadLockException = exception.getDeadLockInfo();
            threadName = exception.getThreadName();
        }
        if (options != null && options.includeInnodbStatusInDeadlockExceptions && deadLockException != null) {
            msg.append("\ndeadlock information: ").append(deadLockException);
        }
        if (options != null && options.includeThreadDumpInDeadlockExceptions) {
            if (threadName != null) {
                msg.append("\nthread name: ").append(threadName);
            }
            msg.append("\ncurrent threads: ");
            final StringBuilder sb;
            int i;
            Thread.getAllStackTraces().forEach((thread, traces) -> {
                sb.append("\n  name:\"").append(thread.getName()).append("\" pid:").append(thread.getId()).append(" status:").append(thread.getState());
                for (i = 0; i < traces.length; ++i) {
                    sb.append("\n    ").append(traces[i]);
                }
                return;
            });
        }
        return msg.toString();
    }
    
    public ExceptionFactory raiseStatementError(final OceanBaseConnection connection, final Statement stmt) {
        return new ExceptionFactory(this.threadId, this.options, connection, stmt);
    }
    
    public SQLException create(final SQLException cause) {
        return createException(cause.getMessage().contains("\n") ? cause.getMessage().substring(0, cause.getMessage().indexOf("\n")) : cause.getMessage(), cause.getSQLState(), cause.getErrorCode(), this.threadId, this.options, this.connection, this.statement, cause);
    }
    
    public SQLException notSupported(final String message) {
        return createException(message, "0A000", -1, this.threadId, this.options, this.connection, this.statement, null);
    }
    
    public SQLException create(final String message) {
        return createException(message, "42000", -1, this.threadId, this.options, this.connection, this.statement, null);
    }
    
    public SQLException create(final String message, final Exception cause) {
        return createException(message, "42000", -1, this.threadId, this.options, this.connection, this.statement, cause);
    }
    
    public SQLException create(final String message, final String sqlState) {
        return createException(message, sqlState, -1, this.threadId, this.options, this.connection, this.statement, null);
    }
    
    public SQLException create(final String message, final String sqlState, final Exception cause) {
        return createException(message, sqlState, -1, this.threadId, this.options, this.connection, this.statement, cause);
    }
    
    public SQLException create(final String message, final String sqlState, final int errorCode) {
        return createException(message, sqlState, errorCode, this.threadId, this.options, this.connection, this.statement, null);
    }
    
    public SQLException create(final String message, final String sqlState, final int errorCode, final Exception cause) {
        return createException(message, sqlState, errorCode, this.threadId, this.options, this.connection, this.statement, cause);
    }
    
    public long getThreadId() {
        return this.threadId;
    }
    
    public Options getOptions() {
        return this.options;
    }
    
    @Override
    public String toString() {
        return "ExceptionFactory{threadId=" + this.threadId + '}';
    }
    
    static {
        INSTANCE = new ExceptionFactory(-1L, null);
    }
}
