// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.util;

import com.alipay.oceanbase.3rd.google.common.cache.CacheBuilder;
import com.alipay.oceanbase.3rd.google.common.cache.Cache;

public class StringCacheUtil
{
    private static int DefaultSQLStringCacheSize;
    public static Cache<String, String> sqlStringCache;
    
    static {
        StringCacheUtil.DefaultSQLStringCacheSize = 51200;
        StringCacheUtil.sqlStringCache = CacheBuilder.newBuilder().maximumSize(StringCacheUtil.DefaultSQLStringCacheSize).build();
    }
}
