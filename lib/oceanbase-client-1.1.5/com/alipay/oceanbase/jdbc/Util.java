// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Iterator;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.ObjectInputStream;
import java.sql.ResultSet;
import java.nio.charset.Charset;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ConcurrentMap;

public class Util
{
    private static Util enclosingInstance;
    private static boolean isJdbc4;
    private static boolean isJdbc42;
    private static int jvmVersion;
    private static int jvmUpdateNumber;
    private static boolean isColdFusion;
    private static final ConcurrentMap<Class<?>, Boolean> isJdbcInterfaceCache;
    private static final String MYSQL_JDBC_PACKAGE_ROOT;
    private static final ConcurrentMap<Class<?>, Class<?>[]> implementedInterfacesCache;
    
    public static boolean isJdbc4() {
        return Util.isJdbc4;
    }
    
    public static boolean isJdbc42() {
        return Util.isJdbc42;
    }
    
    public static int getJVMVersion() {
        return Util.jvmVersion;
    }
    
    public static boolean jvmMeetsMinimum(final int version, final int updateNumber) {
        return getJVMVersion() > version || (getJVMVersion() == version && getJVMUpdateNumber() >= updateNumber);
    }
    
    public static int getJVMUpdateNumber() {
        return Util.jvmUpdateNumber;
    }
    
    public static boolean isColdFusion() {
        return Util.isColdFusion;
    }
    
    public static boolean isCommunityEdition(final String serverVersion) {
        return !isEnterpriseEdition(serverVersion);
    }
    
    public static boolean isEnterpriseEdition(final String serverVersion) {
        return serverVersion.contains("enterprise") || serverVersion.contains("commercial") || serverVersion.contains("advanced");
    }
    
    public static String newCrypt(final String password, final String seed, final String encoding) {
        if (password == null || password.length() == 0) {
            return password;
        }
        final long[] pw = newHash(seed.getBytes());
        final long[] msg = hashPre41Password(password, encoding);
        final long max = 1073741823L;
        long seed2 = (pw[0] ^ msg[0]) % max;
        long seed3 = (pw[1] ^ msg[1]) % max;
        final char[] chars = new char[seed.length()];
        for (int i = 0; i < seed.length(); ++i) {
            seed2 = (seed2 * 3L + seed3) % max;
            seed3 = (seed2 + seed3 + 33L) % max;
            final double d = seed2 / (double)max;
            final byte b = (byte)Math.floor(d * 31.0 + 64.0);
            chars[i] = (char)b;
        }
        seed2 = (seed2 * 3L + seed3) % max;
        seed3 = (seed2 + seed3 + 33L) % max;
        final double d = seed2 / (double)max;
        final byte b = (byte)Math.floor(d * 31.0);
        for (int i = 0; i < seed.length(); ++i) {
            final char[] array = chars;
            final int n = i;
            array[n] ^= (char)b;
        }
        return new String(chars);
    }
    
    public static long[] hashPre41Password(final String password, final String encoding) {
        try {
            return newHash(password.replaceAll("\\s", "").getBytes(encoding));
        }
        catch (UnsupportedEncodingException e) {
            return new long[0];
        }
    }
    
    public static long[] hashPre41Password(final String password) {
        return hashPre41Password(password, Charset.defaultCharset().name());
    }
    
    static long[] newHash(final byte[] password) {
        long nr = 1345345333L;
        long add = 7L;
        long nr2 = 305419889L;
        for (final byte b : password) {
            final long tmp = 0xFF & b;
            nr ^= ((nr & 0x3FL) + add) * tmp + (nr << 8);
            nr2 += (nr2 << 8 ^ nr);
            add += tmp;
        }
        final long[] result = { nr & 0x7FFFFFFFL, nr2 & 0x7FFFFFFFL };
        return result;
    }
    
    public static String oldCrypt(final String password, final String seed) {
        final long max = 33554431L;
        if (password == null || password.length() == 0) {
            return password;
        }
        final long hp = oldHash(seed);
        final long hm = oldHash(password);
        long nr = hp ^ hm;
        long s1;
        nr = (s1 = nr % max);
        long s2 = nr / 2L;
        final char[] chars = new char[seed.length()];
        for (int i = 0; i < seed.length(); ++i) {
            s1 = (s1 * 3L + s2) % max;
            s2 = (s1 + s2 + 33L) % max;
            final double d = s1 / (double)max;
            final byte b = (byte)Math.floor(d * 31.0 + 64.0);
            chars[i] = (char)b;
        }
        return new String(chars);
    }
    
    static long oldHash(final String password) {
        long nr = 1345345333L;
        long nr2 = 7L;
        for (int i = 0; i < password.length(); ++i) {
            if (password.charAt(i) != ' ') {
                if (password.charAt(i) != '\t') {
                    final long tmp = password.charAt(i);
                    nr ^= ((nr & 0x3FL) + nr2) * tmp + (nr << 8);
                    nr2 += tmp;
                }
            }
        }
        return nr & 0x7FFFFFFFL;
    }
    
    private static RandStructcture randomInit(final long seed1, final long seed2) {
        final RandStructcture randStruct = Util.enclosingInstance.new RandStructcture();
        randStruct.maxValue = 1073741823L;
        randStruct.maxValueDbl = (double)randStruct.maxValue;
        randStruct.seed1 = seed1 % randStruct.maxValue;
        randStruct.seed2 = seed2 % randStruct.maxValue;
        return randStruct;
    }
    
    public static Object readObject(final ResultSet resultSet, final int index) throws Exception {
        final ObjectInputStream objIn = new ObjectInputStream(resultSet.getBinaryStream(index));
        final Object obj = objIn.readObject();
        objIn.close();
        return obj;
    }
    
    private static double rnd(final RandStructcture randStruct) {
        randStruct.seed1 = (randStruct.seed1 * 3L + randStruct.seed2) % randStruct.maxValue;
        randStruct.seed2 = (randStruct.seed1 + randStruct.seed2 + 33L) % randStruct.maxValue;
        return randStruct.seed1 / randStruct.maxValueDbl;
    }
    
    public static String scramble(String message, final String password) {
        final byte[] to = new byte[8];
        String val = "";
        message = message.substring(0, 8);
        if (password != null && password.length() > 0) {
            final long[] hashPass = hashPre41Password(password);
            final long[] hashMessage = newHash(message.getBytes());
            final RandStructcture randStruct = randomInit(hashPass[0] ^ hashMessage[0], hashPass[1] ^ hashMessage[1]);
            int msgPos = 0;
            final int msgLength = message.length();
            int toPos = 0;
            while (msgPos++ < msgLength) {
                to[toPos++] = (byte)(Math.floor(rnd(randStruct) * 31.0) + 64.0);
            }
            final byte extra = (byte)Math.floor(rnd(randStruct) * 31.0);
            for (int i = 0; i < to.length; ++i) {
                final byte[] array = to;
                final int n = i;
                array[n] ^= extra;
            }
            val = StringUtils.toString(to);
        }
        return val;
    }
    
    public static String stackTraceToString(final Throwable ex) {
        final StringBuilder traceBuf = new StringBuilder();
        traceBuf.append(Messages.getString("Util.1"));
        if (ex != null) {
            traceBuf.append(ex.getClass().getName());
            final String message = ex.getMessage();
            if (message != null) {
                traceBuf.append(Messages.getString("Util.2"));
                traceBuf.append(message);
            }
            final StringWriter out = new StringWriter();
            final PrintWriter printOut = new PrintWriter(out);
            ex.printStackTrace(printOut);
            traceBuf.append(Messages.getString("Util.3"));
            traceBuf.append(out.toString());
        }
        traceBuf.append(Messages.getString("Util.4"));
        return traceBuf.toString();
    }
    
    public static Object getInstance(final String className, final Class<?>[] argTypes, final Object[] args, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            return handleNewInstance(Class.forName(className).getConstructor(argTypes), args, exceptionInterceptor);
        }
        catch (SecurityException e) {
            throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
        }
        catch (NoSuchMethodException e2) {
            throw SQLError.createSQLException("Can't instantiate required class", "S1000", e2, exceptionInterceptor);
        }
        catch (ClassNotFoundException e3) {
            throw SQLError.createSQLException("Can't instantiate required class", "S1000", e3, exceptionInterceptor);
        }
    }
    
    public static final Object handleNewInstance(final Constructor<?> ctor, final Object[] args, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            return ctor.newInstance(args);
        }
        catch (IllegalArgumentException e) {
            throw SQLError.createSQLException("Can't instantiate required class", "S1000", e, exceptionInterceptor);
        }
        catch (InstantiationException e2) {
            throw SQLError.createSQLException("Can't instantiate required class", "S1000", e2, exceptionInterceptor);
        }
        catch (IllegalAccessException e3) {
            throw SQLError.createSQLException("Can't instantiate required class", "S1000", e3, exceptionInterceptor);
        }
        catch (InvocationTargetException e4) {
            Throwable target = e4.getTargetException();
            if (target instanceof SQLException) {
                throw (SQLException)target;
            }
            if (target instanceof ExceptionInInitializerError) {
                target = ((ExceptionInInitializerError)target).getException();
            }
            throw SQLError.createSQLException(target.toString(), "S1000", target, exceptionInterceptor);
        }
    }
    
    public static boolean interfaceExists(final String hostname) {
        try {
            final Class<?> networkInterfaceClass = Class.forName("java.net.NetworkInterface");
            return networkInterfaceClass.getMethod("getByName", (Class<?>[])null).invoke(networkInterfaceClass, hostname) != null;
        }
        catch (Throwable t) {
            return false;
        }
    }
    
    public static void resultSetToMap(final Map mappedValues, final ResultSet rs) throws SQLException {
        while (rs.next()) {
            mappedValues.put(rs.getObject(1), rs.getObject(2));
        }
    }
    
    public static void resultSetToMap(final Map mappedValues, final ResultSet rs, final int key, final int value) throws SQLException {
        while (rs.next()) {
            mappedValues.put(rs.getObject(key), rs.getObject(value));
        }
    }
    
    public static void resultSetToMap(final Map mappedValues, final ResultSet rs, final String key, final String value) throws SQLException {
        while (rs.next()) {
            mappedValues.put(rs.getObject(key), rs.getObject(value));
        }
    }
    
    public static Map<Object, Object> calculateDifferences(final Map<?, ?> map1, final Map<?, ?> map2) {
        final Map<Object, Object> diffMap = new HashMap<Object, Object>();
        for (final Map.Entry<?, ?> entry : map1.entrySet()) {
            final Object key = entry.getKey();
            Number value1 = null;
            Number value2 = null;
            if (entry.getValue() instanceof Number) {
                value1 = (Number)entry.getValue();
                value2 = (Number)map2.get(key);
            }
            else {
                try {
                    value1 = new Double(entry.getValue().toString());
                    value2 = new Double(map2.get(key).toString());
                }
                catch (NumberFormatException nfe) {
                    continue;
                }
            }
            if (value1.equals(value2)) {
                continue;
            }
            if (value1 instanceof Byte) {
                diffMap.put(key, (byte)((byte)value2 - (byte)value1));
            }
            else if (value1 instanceof Short) {
                diffMap.put(key, (short)((short)value2 - (short)value1));
            }
            else if (value1 instanceof Integer) {
                diffMap.put(key, (int)value2 - (int)value1);
            }
            else if (value1 instanceof Long) {
                diffMap.put(key, (long)value2 - (long)value1);
            }
            else if (value1 instanceof Float) {
                diffMap.put(key, (float)value2 - (float)value1);
            }
            else if (value1 instanceof Double) {
                diffMap.put(key, (double)(((Double)value2).shortValue() - ((Double)value1).shortValue()));
            }
            else if (value1 instanceof BigDecimal) {
                diffMap.put(key, ((BigDecimal)value2).subtract((BigDecimal)value1));
            }
            else {
                if (!(value1 instanceof BigInteger)) {
                    continue;
                }
                diffMap.put(key, ((BigInteger)value2).subtract((BigInteger)value1));
            }
        }
        return diffMap;
    }
    
    public static List<Extension> loadExtensions(final Connection conn, final Properties props, final String extensionClassNames, final String errorMessageKey, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        final List<Extension> extensionList = new LinkedList<Extension>();
        final List<String> interceptorsToCreate = StringUtils.split(extensionClassNames, ",", true);
        String className = null;
        try {
            for (int i = 0, s = interceptorsToCreate.size(); i < s; ++i) {
                className = interceptorsToCreate.get(i);
                final Extension extensionInstance = (Extension)Class.forName(className).newInstance();
                extensionInstance.init(conn, props);
                extensionList.add(extensionInstance);
            }
        }
        catch (Throwable t) {
            final SQLException sqlEx = SQLError.createSQLException(Messages.getString(errorMessageKey, new Object[] { className }), exceptionInterceptor);
            sqlEx.initCause(t);
            throw sqlEx;
        }
        return extensionList;
    }
    
    public static boolean isJdbcInterface(final Class<?> clazz) {
        if (Util.isJdbcInterfaceCache.containsKey(clazz)) {
            return Util.isJdbcInterfaceCache.get(clazz);
        }
        if (clazz.isInterface()) {
            try {
                if (isJdbcPackage(clazz.getPackage().getName())) {
                    Util.isJdbcInterfaceCache.putIfAbsent(clazz, true);
                    return true;
                }
            }
            catch (Exception ex) {}
        }
        for (final Class<?> iface : clazz.getInterfaces()) {
            if (isJdbcInterface(iface)) {
                Util.isJdbcInterfaceCache.putIfAbsent(clazz, true);
                return true;
            }
        }
        if (clazz.getSuperclass() != null && isJdbcInterface(clazz.getSuperclass())) {
            Util.isJdbcInterfaceCache.putIfAbsent(clazz, true);
            return true;
        }
        Util.isJdbcInterfaceCache.putIfAbsent(clazz, false);
        return false;
    }
    
    public static boolean isJdbcPackage(final String packageName) {
        return packageName != null && (packageName.startsWith("java.sql") || packageName.startsWith("javax.sql") || packageName.startsWith(Util.MYSQL_JDBC_PACKAGE_ROOT));
    }
    
    public static Class<?>[] getImplementedInterfaces(final Class<?> clazz) {
        Class<?>[] implementedInterfaces = (Class<?>[])Util.implementedInterfacesCache.get(clazz);
        if (implementedInterfaces != null) {
            return implementedInterfaces;
        }
        final Set<Class<?>> interfaces = new LinkedHashSet<Class<?>>();
        Class<?> superClass = clazz;
        do {
            Collections.addAll(interfaces, (Class<?>[])superClass.getInterfaces());
        } while ((superClass = superClass.getSuperclass()) != null);
        implementedInterfaces = interfaces.toArray(new Class[interfaces.size()]);
        final Class<?>[] oldValue = Util.implementedInterfacesCache.putIfAbsent(clazz, implementedInterfaces);
        if (oldValue != null) {
            implementedInterfaces = oldValue;
        }
        return implementedInterfaces;
    }
    
    public static long secondsSinceMillis(final long timeInMillis) {
        return (System.currentTimeMillis() - timeInMillis) / 1000L;
    }
    
    public static int truncateAndConvertToInt(final long longValue) {
        return (longValue > 2147483647L) ? Integer.MAX_VALUE : ((longValue < -2147483648L) ? Integer.MIN_VALUE : ((int)longValue));
    }
    
    public static int[] truncateAndConvertToInt(final long[] longArray) {
        final int[] intArray = new int[longArray.length];
        for (int i = 0; i < longArray.length; ++i) {
            intArray[i] = ((longArray[i] > 2147483647L) ? Integer.MAX_VALUE : ((longArray[i] < -2147483648L) ? Integer.MIN_VALUE : ((int)longArray[i])));
        }
        return intArray;
    }
    
    public static boolean checkFieldsEqual(final Field[] srcFields, final Field[] destFields) {
        boolean isEquals = true;
        if (srcFields != destFields) {
            if (null == srcFields || null == destFields) {
                isEquals = false;
            }
            else if (srcFields.length != destFields.length) {
                isEquals = false;
            }
            else {
                for (int i = 0; i < srcFields.length; ++i) {
                    final Field srcField = srcFields[i];
                    final Field destField = destFields[i];
                    if (srcField != destField) {
                        isEquals = (null != srcField && null != destField && srcField.equals(destField));
                        if (!isEquals) {
                            break;
                        }
                    }
                }
            }
        }
        return isEquals;
    }
    
    public static Field[] deepCopyFields(final Field[] fields, final MySQLConnection connection) throws SQLException {
        Field[] fieldsCopy = null;
        if (null != fields) {
            fieldsCopy = new Field[fields.length];
            try {
                for (int i = 0; i < fields.length; ++i) {
                    (fieldsCopy[i] = (Field)fields[i].clone()).setConnection(connection);
                }
            }
            catch (CloneNotSupportedException e) {
                throw new SQLException("fail to clone fields", e);
            }
        }
        return fieldsCopy;
    }
    
    public static ServerPreparedStatement.BindValue[] constructParameterBindings(final int parameterCount) {
        if (parameterCount < 0) {
            throw new IllegalArgumentException(String.format("parameterCount[%s] should >= 0", parameterCount));
        }
        final ServerPreparedStatement.BindValue[] parameterBindings = new ServerPreparedStatement.BindValue[parameterCount];
        for (int i = 0; i < parameterCount; ++i) {
            parameterBindings[i] = new ServerPreparedStatement.BindValue();
        }
        return parameterBindings;
    }
    
    static {
        Util.enclosingInstance = new Util();
        Util.jvmVersion = -1;
        Util.jvmUpdateNumber = -1;
        Util.isColdFusion = false;
        try {
            Class.forName("java.sql.NClob");
            Util.isJdbc4 = true;
        }
        catch (ClassNotFoundException e) {
            Util.isJdbc4 = false;
        }
        Util.isJdbc42 = false;
        final String jvmVersionString = System.getProperty("java.version");
        int startPos = jvmVersionString.indexOf(46);
        int endPos = startPos + 1;
        if (startPos != -1) {
            while (Character.isDigit(jvmVersionString.charAt(endPos)) && ++endPos < jvmVersionString.length()) {}
        }
        ++startPos;
        if (endPos > startPos) {
            Util.jvmVersion = Integer.parseInt(jvmVersionString.substring(startPos, endPos));
        }
        else {
            Util.jvmVersion = (Util.isJdbc42 ? 8 : (Util.isJdbc4 ? 6 : 5));
        }
        startPos = jvmVersionString.indexOf("_");
        endPos = startPos + 1;
        if (startPos != -1) {
            while (Character.isDigit(jvmVersionString.charAt(endPos)) && ++endPos < jvmVersionString.length()) {}
        }
        ++startPos;
        if (endPos > startPos) {
            Util.jvmUpdateNumber = Integer.parseInt(jvmVersionString.substring(startPos, endPos));
        }
        final String loadedFrom = stackTraceToString(new Throwable());
        if (loadedFrom != null) {
            Util.isColdFusion = (loadedFrom.indexOf("coldfusion") != -1);
        }
        else {
            Util.isColdFusion = false;
        }
        isJdbcInterfaceCache = new ConcurrentHashMap<Class<?>, Boolean>();
        final String packageName = MultiHostConnectionProxy.class.getPackage().getName();
        MYSQL_JDBC_PACKAGE_ROOT = packageName.substring(0, packageName.indexOf("jdbc") + 4);
        implementedInterfacesCache = new ConcurrentHashMap<Class<?>, Class<?>[]>();
    }
    
    class RandStructcture
    {
        long maxValue;
        double maxValueDbl;
        long seed1;
        long seed2;
    }
}
