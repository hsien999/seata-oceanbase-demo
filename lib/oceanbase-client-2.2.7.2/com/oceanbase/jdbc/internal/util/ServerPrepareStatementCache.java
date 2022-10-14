// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util;

import java.util.Iterator;
import java.sql.SQLException;
import java.util.Map;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import java.util.LinkedHashMap;

public final class ServerPrepareStatementCache extends LinkedHashMap<String, ServerPrepareResult>
{
    private final int maxSize;
    private final Protocol protocol;
    
    private ServerPrepareStatementCache(final int size, final Protocol protocol) {
        super(size, 0.75f, true);
        this.maxSize = size;
        this.protocol = protocol;
    }
    
    public static ServerPrepareStatementCache newInstance(final int size, final Protocol protocol) {
        return new ServerPrepareStatementCache(size, protocol);
    }
    
    public boolean removeEldestEntry(final Map.Entry eldest) {
        final boolean mustBeRemoved = this.size() > this.maxSize;
        if (mustBeRemoved) {
            final ServerPrepareResult serverPrepareResult = eldest.getValue();
            serverPrepareResult.setRemoveFromCache();
            if (serverPrepareResult.canBeDeallocate()) {
                try {
                    this.protocol.forceReleasePrepareStatement(serverPrepareResult.getStatementId());
                }
                catch (SQLException ex) {}
            }
        }
        return mustBeRemoved;
    }
    
    @Override
    public synchronized ServerPrepareResult put(final String key, final ServerPrepareResult result) {
        final ServerPrepareResult cachedServerPrepareResult = super.get(key);
        if (cachedServerPrepareResult != null && cachedServerPrepareResult.incrementShareCounter()) {
            return cachedServerPrepareResult;
        }
        result.setAddToCache();
        super.put(key, result);
        return null;
    }
    
    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder("ServerPrepareStatementCache.map[");
        for (final Map.Entry<String, ServerPrepareResult> entry : this.entrySet()) {
            stringBuilder.append("\n").append(entry.getKey()).append("-").append(entry.getValue().getShareCounter());
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }
}
