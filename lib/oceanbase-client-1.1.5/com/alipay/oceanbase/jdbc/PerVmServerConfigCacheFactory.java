// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.Iterator;
import java.util.Set;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class PerVmServerConfigCacheFactory implements CacheAdapterFactory<String, Map<String, String>>
{
    static final ConcurrentHashMap<String, Map<String, String>> serverConfigByUrl;
    private static final CacheAdapter<String, Map<String, String>> serverConfigCache;
    
    @Override
    public CacheAdapter<String, Map<String, String>> getInstance(final Connection forConn, final String url, final int cacheMaxSize, final int maxKeySize, final Properties connectionProperties) throws SQLException {
        return PerVmServerConfigCacheFactory.serverConfigCache;
    }
    
    static {
        serverConfigByUrl = new ConcurrentHashMap<String, Map<String, String>>();
        serverConfigCache = new CacheAdapter<String, Map<String, String>>() {
            @Override
            public Map<String, String> get(final String key) {
                return PerVmServerConfigCacheFactory.serverConfigByUrl.get(key);
            }
            
            @Override
            public void put(final String key, final Map<String, String> value) {
                PerVmServerConfigCacheFactory.serverConfigByUrl.putIfAbsent(key, value);
            }
            
            @Override
            public void invalidate(final String key) {
                PerVmServerConfigCacheFactory.serverConfigByUrl.remove(key);
            }
            
            @Override
            public void invalidateAll(final Set<String> keys) {
                for (final String key : keys) {
                    PerVmServerConfigCacheFactory.serverConfigByUrl.remove(key);
                }
            }
            
            @Override
            public void invalidateAll() {
                PerVmServerConfigCacheFactory.serverConfigByUrl.clear();
            }
        };
    }
}
