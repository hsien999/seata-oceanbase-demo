// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.Connection;
import javax.sql.ConnectionEvent;
import java.util.Iterator;
import javax.sql.StatementEvent;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.concurrent.Executor;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import javax.sql.StatementEventListener;
import javax.sql.ConnectionEventListener;
import java.util.List;
import javax.sql.PooledConnection;

public class OceanBasePooledConnection implements PooledConnection
{
    private final OceanBaseConnection connection;
    private final List<ConnectionEventListener> connectionEventListeners;
    private final List<StatementEventListener> statementEventListeners;
    private final AtomicLong lastUsed;
    
    public OceanBasePooledConnection(final OceanBaseConnection connection) {
        this.connection = connection;
        connection.pooledConnection = this;
        this.statementEventListeners = new CopyOnWriteArrayList<StatementEventListener>();
        this.connectionEventListeners = new CopyOnWriteArrayList<ConnectionEventListener>();
        this.lastUsed = new AtomicLong(System.nanoTime());
    }
    
    @Override
    public OceanBaseConnection getConnection() {
        return this.connection;
    }
    
    @Override
    public void close() throws SQLException {
        this.connection.pooledConnection = null;
        this.connection.close();
    }
    
    public void abort(final Executor executor) throws SQLException {
        this.connection.pooledConnection = null;
        this.connection.abort(executor);
    }
    
    @Override
    public void addConnectionEventListener(final ConnectionEventListener listener) {
        this.connectionEventListeners.add(listener);
    }
    
    @Override
    public void removeConnectionEventListener(final ConnectionEventListener listener) {
        this.connectionEventListeners.remove(listener);
    }
    
    @Override
    public void addStatementEventListener(final StatementEventListener listener) {
        this.statementEventListeners.add(listener);
    }
    
    @Override
    public void removeStatementEventListener(final StatementEventListener listener) {
        this.statementEventListeners.remove(listener);
    }
    
    public void fireStatementClosed(final Statement st) {
        if (st instanceof PreparedStatement) {
            final StatementEvent event = new StatementEvent(this, (PreparedStatement)st);
            for (final StatementEventListener listener : this.statementEventListeners) {
                listener.statementClosed(event);
            }
        }
    }
    
    public void fireStatementErrorOccured(final Statement st, final SQLException ex) {
        if (st instanceof PreparedStatement) {
            final StatementEvent event = new StatementEvent(this, (PreparedStatement)st, ex);
            for (final StatementEventListener listener : this.statementEventListeners) {
                listener.statementErrorOccurred(event);
            }
        }
    }
    
    public void fireConnectionClosed() {
        final ConnectionEvent event = new ConnectionEvent(this);
        for (final ConnectionEventListener listener : this.connectionEventListeners) {
            listener.connectionClosed(event);
        }
    }
    
    public void fireConnectionErrorOccured(final SQLException ex) {
        final ConnectionEvent event = new ConnectionEvent(this, ex);
        for (final ConnectionEventListener listener : this.connectionEventListeners) {
            listener.connectionErrorOccurred(event);
        }
    }
    
    public boolean noStmtEventListeners() {
        return this.statementEventListeners.isEmpty();
    }
    
    public AtomicLong getLastUsed() {
        return this.lastUsed;
    }
    
    public void lastUsedToNow() {
        this.lastUsed.set(System.nanoTime());
    }
}
