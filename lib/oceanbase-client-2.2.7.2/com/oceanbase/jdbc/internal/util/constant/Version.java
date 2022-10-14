// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.constant;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;

public final class Version
{
    public static final String version;
    public static final int majorVersion;
    public static final int minorVersion;
    public static final int patchVersion;
    public static final String qualifier;
    
    static {
        InputStream inputStream = null;
        String tmpVersion = "2.2.7";
        try {
            final Properties prop = new Properties();
            inputStream = Version.class.getResourceAsStream("/oceanbase.properties");
            if (inputStream != null) {
                prop.load(inputStream);
                tmpVersion = prop.getProperty("version");
            }
            else {
                System.out.println("property file 'oceanbase.properties' not found in the classpath");
            }
        }
        catch (Exception ex) {}
        finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            }
            catch (IOException ex2) {}
        }
        version = tmpVersion;
        int major = 0;
        int minor = 0;
        int patch = 0;
        String qualif = "";
        final int length = Version.version.length();
        int offset = 0;
        int type = 0;
        int val = 0;
        while (offset < length) {
            final char car = Version.version.charAt(offset);
            if (car < '0' || car > '9') {
                switch (type) {
                    case 0: {
                        major = val;
                        break;
                    }
                    case 1: {
                        minor = val;
                        break;
                    }
                    case 2: {
                        patch = val;
                        qualif = Version.version.substring(offset);
                        offset = length;
                        break;
                    }
                }
                ++type;
                val = 0;
            }
            else {
                val = val * 10 + car - 48;
            }
            ++offset;
        }
        if (type == 2) {
            patch = val;
        }
        majorVersion = major;
        minorVersion = minor;
        patchVersion = patch;
        qualifier = qualif;
    }
}
