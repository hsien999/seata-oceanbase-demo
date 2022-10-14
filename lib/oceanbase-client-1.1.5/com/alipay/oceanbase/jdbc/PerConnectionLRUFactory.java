// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Set;
import com.alipay.oceanbase.jdbc.util.LRUCache;
import java.sql.SQLException;
import java.util.Properties;

public class PerConnectionLRUFactory implements CacheAdapterFactory<String, PreparedStatement.ParseInfo>
{
    @Override
    public CacheAdapter<String, PreparedStatement.ParseInfo> getInstance(final Connection forConnection, final String url, final int cacheMaxSize, final int maxKeySize, final Properties connectionProperties) throws SQLException {
        return new PerConnectionLRU(forConnection, cacheMaxSize, maxKeySize);
    }
    
    class PerConnectionLRU implements CacheAdapter<String, PreparedStatement.ParseInfo>
    {
        private final int cacheSqlLimit;
        private final LRUCache cache;
        private final Connection conn;
        
        protected PerConnectionLRU(final Connection forConnection, final int cacheMaxSize, final int maxKeySize) {
            final int cacheSize = cacheMaxSize;
            this.cacheSqlLimit = maxKeySize;
            this.cache = new LRUCache(cacheSize);
            this.conn = forConnection;
        }
        
        @Override
        public PreparedStatement.ParseInfo get(final String key) {
            if (key == null || key.length() > this.cacheSqlLimit) {
                return null;
            }
            synchronized (this.conn.getConnectionMutex()) {
                return ((LinkedHashMap<K, PreparedStatement.ParseInfo>)this.cache).get(key);
            }
        }
        
        @Override
        public void put(final String key, final PreparedStatement.ParseInfo value) {
            if (key == null || key.length() > this.cacheSqlLimit) {
                return;
            }
            synchronized (this.conn.getConnectionMutex()) {
                ((HashMap<String, PreparedStatement.ParseInfo>)this.cache).put(key, value);
            }
        }
        
        @Override
        public void invalidate(final String key) {
            synchronized (this.conn.getConnectionMutex()) {
                this.cache.remove(key);
            }
        }
        
        @Override
        public void invalidateAll(final Set<String> keys) {
            synchronized (this.conn.getConnectionMutex()) {
                for (final String key : keys) {
                    this.cache.remove(key);
                }
            }
        }
        
        @Override
        public void invalidateAll() {
            synchronized (this.conn.getConnectionMutex()) {
                this.cache.clear();
            }
        }
    }
}
