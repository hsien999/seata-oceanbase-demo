// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdbc2.optional;

import java.util.Iterator;
import javax.sql.StatementEvent;
import java.sql.SQLException;
import java.util.HashMap;
import com.alipay.oceanbase.jdbc.Connection;
import javax.sql.StatementEventListener;
import java.util.Map;

public class JDBC4SuspendableXAConnection extends SuspendableXAConnection
{
    private final Map<StatementEventListener, StatementEventListener> statementEventListeners;
    
    public JDBC4SuspendableXAConnection(final Connection connection) throws SQLException {
        super(connection);
        this.statementEventListeners = new HashMap<StatementEventListener, StatementEventListener>();
    }
    
    @Override
    public synchronized void close() throws SQLException {
        super.close();
        this.statementEventListeners.clear();
    }
    
    @Override
    public void addStatementEventListener(final StatementEventListener listener) {
        synchronized (this.statementEventListeners) {
            this.statementEventListeners.put(listener, listener);
        }
    }
    
    @Override
    public void removeStatementEventListener(final StatementEventListener listener) {
        synchronized (this.statementEventListeners) {
            this.statementEventListeners.remove(listener);
        }
    }
    
    void fireStatementEvent(final StatementEvent event) throws SQLException {
        synchronized (this.statementEventListeners) {
            for (final StatementEventListener listener : this.statementEventListeners.keySet()) {
                listener.statementClosed(event);
            }
        }
    }
}
