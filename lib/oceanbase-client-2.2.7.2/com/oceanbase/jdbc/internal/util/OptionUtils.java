// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util;

import java.util.HashMap;
import com.oceanbase.jdbc.util.DefaultOptions;
import java.util.Map;

public class OptionUtils
{
    public static final Map<String, DefaultOptions> OPTIONS_MAP;
    
    static {
        OPTIONS_MAP = new HashMap<String, DefaultOptions>();
        for (final DefaultOptions defaultOption : DefaultOptions.values()) {
            OptionUtils.OPTIONS_MAP.put(defaultOption.getOptionName(), defaultOption);
        }
        OptionUtils.OPTIONS_MAP.put("createDB", DefaultOptions.CREATE_DATABASE_IF_NOT_EXISTS);
        OptionUtils.OPTIONS_MAP.put("useSSL", DefaultOptions.USE_SSL);
        OptionUtils.OPTIONS_MAP.put("profileSQL", DefaultOptions.PROFILE_SQL);
        OptionUtils.OPTIONS_MAP.put("enabledSSLCipherSuites", DefaultOptions.ENABLED_SSL_CIPHER_SUITES);
        OptionUtils.OPTIONS_MAP.put("trustCertificateKeyStorePassword", DefaultOptions.TRUST_CERTIFICATE_KEYSTORE_PASSWORD);
        OptionUtils.OPTIONS_MAP.put("trustCertificateKeyStoreUrl", DefaultOptions.TRUSTSTORE);
        OptionUtils.OPTIONS_MAP.put("clientCertificateKeyStorePassword", DefaultOptions.KEYSTORE_PASSWORD);
        OptionUtils.OPTIONS_MAP.put("clientCertificateKeyStoreUrl", DefaultOptions.KEYSTORE);
        OptionUtils.OPTIONS_MAP.put("trustCertificateKeyStoreType", DefaultOptions.TRUST_STORE_TYPE);
        OptionUtils.OPTIONS_MAP.put("clientCertificateKeyStoreType", DefaultOptions.KEY_STORE_TYPE);
    }
}
