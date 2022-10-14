// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util;

import com.oceanbase.jdbc.internal.io.socket.SocketUtility;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import java.io.ByteArrayOutputStream;
import java.util.TimeZone;
import com.oceanbase.jdbc.internal.logging.ProtocolLoggingProxy;
import com.oceanbase.jdbc.internal.failover.impl.MastersFailoverListener;
import com.oceanbase.jdbc.internal.protocol.MasterProtocol;
import com.oceanbase.jdbc.internal.failover.impl.MastersSlavesListener;
import com.oceanbase.jdbc.internal.protocol.MastersSlavesProtocol;
import java.lang.reflect.InvocationHandler;
import com.oceanbase.jdbc.internal.failover.Listener;
import com.oceanbase.jdbc.internal.failover.FailoverProxy;
import com.oceanbase.jdbc.internal.failover.impl.AuroraListener;
import com.oceanbase.jdbc.internal.protocol.AuroraProtocol;
import com.oceanbase.jdbc.internal.io.LruTraceCache;
import java.util.concurrent.locks.ReentrantLock;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import com.oceanbase.jdbc.UrlParser;
import java.util.ArrayList;
import java.util.List;
import java.sql.SQLException;
import java.util.Locale;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.lang.reflect.Constructor;
import java.io.IOException;
import com.oceanbase.jdbc.util.ConfigurableSocketFactory;
import javax.net.SocketFactory;
import java.net.Socket;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.io.socket.SocketHandlerFunction;
import java.util.regex.Pattern;

public class Utils
{
    private static final char[] hexArray;
    private static final Pattern IP_V4;
    private static final Pattern IP_V6;
    private static final Pattern IP_V6_COMPRESSED;
    private static final SocketHandlerFunction socketHandler;
    private static final char[] DIGITS_LOWER;
    private static final char[] DIGITS_UPPER;
    
    public static Socket standardSocket(final Options options, final String host) throws IOException {
        final String socketFactoryName = options.socketFactory;
        if (socketFactoryName != null) {
            try {
                final Class<? extends SocketFactory> socketFactoryClass = (Class<? extends SocketFactory>)Class.forName(socketFactoryName);
                if (socketFactoryClass != null) {
                    final Constructor<? extends SocketFactory> constructor = socketFactoryClass.getConstructor((Class<?>[])new Class[0]);
                    final SocketFactory socketFactory = (SocketFactory)constructor.newInstance(new Object[0]);
                    if (socketFactoryClass.isInstance(ConfigurableSocketFactory.class)) {
                        ((ConfigurableSocketFactory)socketFactory).setConfiguration(options, host);
                    }
                    return socketFactory.createSocket();
                }
            }
            catch (Exception exp) {
                throw new IOException("Socket factory failed to initialized with option \"socketFactory\" set to \"" + options.socketFactory + "\"", exp);
            }
        }
        final SocketFactory socketFactory = SocketFactory.getDefault();
        return socketFactory.createSocket();
    }
    
    public static Socket socksSocket(final Options options) {
        final String socksProxyHost = options.socksProxyHost;
        final int socksProxyPort = options.socksProxyPort;
        return new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksProxyHost, socksProxyPort)));
    }
    
    public static String escapeString(final String value, final boolean noBackslashEscapes) {
        if (!value.contains("'")) {
            if (noBackslashEscapes) {
                return value;
            }
            if (!value.contains("\\")) {
                return value;
            }
        }
        final String escaped = value.replace("'", "''");
        if (noBackslashEscapes) {
            return escaped;
        }
        return escaped.replace("\\", "\\\\");
    }
    
    public static byte[] encryptPassword(final String password, final byte[] seed, final String passwordCharacterEncoding) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        if (password == null || password.isEmpty()) {
            return new byte[0];
        }
        final MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
        byte[] bytePwd;
        if (passwordCharacterEncoding != null && !passwordCharacterEncoding.isEmpty()) {
            bytePwd = password.getBytes(passwordCharacterEncoding);
        }
        else {
            bytePwd = password.getBytes();
        }
        final byte[] stage1 = messageDigest.digest(bytePwd);
        messageDigest.reset();
        final byte[] stage2 = messageDigest.digest(stage1);
        messageDigest.reset();
        messageDigest.update(seed);
        messageDigest.update(stage2);
        final byte[] digest = messageDigest.digest();
        final byte[] returnBytes = new byte[digest.length];
        for (int i = 0; i < digest.length; ++i) {
            returnBytes[i] = (byte)(stage1[i] ^ digest[i]);
        }
        return returnBytes;
    }
    
    public static byte[] copyWithLength(final byte[] orig, final int length) {
        final byte[] result = new byte[length];
        final int howMuchToCopy = (length < orig.length) ? length : orig.length;
        System.arraycopy(orig, 0, result, 0, howMuchToCopy);
        return result;
    }
    
    public static byte[] copyRange(final byte[] orig, final int from, final int to) {
        final int length = to - from;
        final byte[] result = new byte[length];
        final int howMuchToCopy = (orig.length - from < length) ? (orig.length - from) : length;
        System.arraycopy(orig, from, result, 0, howMuchToCopy);
        return result;
    }
    
    private static String replaceFunctionParameter(final String functionString, final Protocol protocol) {
        final char[] input = functionString.toCharArray();
        final StringBuilder sb = new StringBuilder();
        int index;
        for (index = 0; index < input.length; ++index) {
            if (input[index] != ' ') {
                break;
            }
        }
        while (((input[index] >= 'a' && input[index] <= 'z') || (input[index] >= 'A' && input[index] <= 'Z')) && index < input.length) {
            sb.append(input[index]);
            ++index;
        }
        final String lowerCase;
        final String func = lowerCase = sb.toString().toLowerCase(Locale.ROOT);
        switch (lowerCase) {
            case "convert": {
                final int lastCommaIndex = functionString.lastIndexOf(44);
                final int firstParentheses = functionString.indexOf(40);
                final String value = functionString.substring(firstParentheses + 1, lastCommaIndex);
                for (index = lastCommaIndex + 1; index < input.length && Character.isWhitespace(input[index]); ++index) {}
                int endParam;
                for (endParam = index + 1; endParam < input.length && ((input[endParam] >= 'a' && input[endParam] <= 'z') || (input[endParam] >= 'A' && input[endParam] <= 'Z') || input[endParam] == '_'); ++endParam) {}
                String typeParam = new String(input, index, endParam - index).toUpperCase(Locale.ROOT);
                if (typeParam.startsWith("SQL_")) {
                    typeParam = typeParam.substring(4);
                }
                final String s = typeParam;
                switch (s) {
                    case "BOOLEAN": {
                        return "1=" + value;
                    }
                    case "BIGINT":
                    case "SMALLINT":
                    case "TINYINT": {
                        typeParam = "SIGNED INTEGER";
                        break;
                    }
                    case "BIT": {
                        typeParam = "UNSIGNED INTEGER";
                        break;
                    }
                    case "BLOB":
                    case "VARBINARY":
                    case "LONGVARBINARY":
                    case "ROWID": {
                        typeParam = "BINARY";
                        break;
                    }
                    case "NCHAR":
                    case "CLOB":
                    case "NCLOB":
                    case "DATALINK":
                    case "VARCHAR":
                    case "NVARCHAR":
                    case "LONGVARCHAR":
                    case "LONGNVARCHAR":
                    case "SQLXML":
                    case "LONGNCHAR": {
                        typeParam = "CHAR";
                        break;
                    }
                    case "DOUBLE":
                    case "FLOAT": {
                        if (protocol.isServerMariaDb() || protocol.versionGreaterOrEqual(8, 0, 17)) {
                            typeParam = "DOUBLE";
                            break;
                        }
                        return "0.0+" + value;
                    }
                    case "REAL":
                    case "NUMERIC": {
                        typeParam = "DECIMAL";
                        break;
                    }
                    case "TIMESTAMP": {
                        typeParam = "DATETIME";
                        break;
                    }
                }
                return new String(input, 0, index) + typeParam + new String(input, endParam, input.length - endParam);
            }
            case "timestampdiff":
            case "timestampadd": {
                while (index < input.length && (Character.isWhitespace(input[index]) || input[index] == '(')) {
                    ++index;
                }
                if (index < input.length - 8) {
                    final String paramPrefix = new String(input, index, 8);
                    if ("SQL_TSI_".equals(paramPrefix)) {
                        return new String(input, 0, index) + new String(input, index + 8, input.length - (index + 8));
                    }
                }
                return functionString;
            }
            default: {
                return functionString;
            }
        }
    }
    
    private static String resolveEscapes(final String escaped, final Protocol protocol) throws SQLException {
        if (escaped.charAt(0) != '{' || escaped.charAt(escaped.length() - 1) != '}') {
            throw new SQLException("unexpected escaped string");
        }
        final int endIndex = escaped.length() - 1;
        final String escapedLower = escaped.toLowerCase(Locale.ROOT);
        if (escaped.startsWith("{fn ")) {
            final String resolvedParams = replaceFunctionParameter(escaped.substring(4, endIndex), protocol);
            return nativeSql(resolvedParams, protocol);
        }
        if (escapedLower.startsWith("{oj ")) {
            return nativeSql(escaped.substring(4, endIndex), protocol);
        }
        if (escaped.startsWith("{d ")) {
            return escaped.substring(3, endIndex);
        }
        if (escaped.startsWith("{t ")) {
            return escaped.substring(3, endIndex);
        }
        if (escaped.startsWith("{ts ")) {
            return escaped.substring(4, endIndex);
        }
        if (escaped.startsWith("{d'")) {
            return escaped.substring(2, endIndex);
        }
        if (escaped.startsWith("{t'")) {
            return escaped.substring(2, endIndex);
        }
        if (escaped.startsWith("{ts'")) {
            return escaped.substring(3, endIndex);
        }
        if (escaped.startsWith("{call ") || escaped.startsWith("{CALL ")) {
            return nativeSql(escaped.substring(1, endIndex), protocol);
        }
        if (escaped.startsWith("{escape ")) {
            return escaped.substring(1, endIndex);
        }
        if (escaped.startsWith("{?")) {
            return nativeSql(escaped.substring(1, endIndex), protocol);
        }
        if (escaped.startsWith("{ ") || escaped.startsWith("{\n")) {
            for (int i = 2; i < escaped.length(); ++i) {
                if (!Character.isWhitespace(escaped.charAt(i))) {
                    return resolveEscapes("{" + escaped.substring(i), protocol);
                }
            }
        }
        else if (escaped.startsWith("{\r\n")) {
            for (int i = 3; i < escaped.length(); ++i) {
                if (!Character.isWhitespace(escaped.charAt(i))) {
                    return resolveEscapes("{" + escaped.substring(i), protocol);
                }
            }
        }
        throw new SQLException("unknown escape sequence " + escaped);
    }
    
    public static String nativeSql(final String sql, final Protocol protocol) throws SQLException {
        if (!sql.contains("{")) {
            return sql;
        }
        final StringBuilder escapeSequenceBuf = new StringBuilder();
        final StringBuilder sqlBuffer = new StringBuilder();
        final char[] charArray = sql.toCharArray();
        char lastChar = '\0';
        boolean inQuote = false;
        char quoteChar = '\0';
        boolean inComment = false;
        boolean isSlashSlashComment = false;
        int inEscapeSeq = 0;
        final boolean isOracleMode = protocol.isOracleMode();
        for (int i = 0; i < charArray.length; ++i) {
            final char car = charArray[i];
            if (!isOracleMode && lastChar == '\\' && !protocol.noBackslashEscapes()) {
                sqlBuffer.append(car);
                lastChar = ' ';
            }
            else {
                switch (car) {
                    case '\"':
                    case '\'':
                    case '`': {
                        if (inComment) {
                            break;
                        }
                        if (!inQuote) {
                            inQuote = true;
                            quoteChar = car;
                            break;
                        }
                        if (quoteChar == car) {
                            inQuote = false;
                            break;
                        }
                        break;
                    }
                    case '*': {
                        if (!inQuote && !inComment && lastChar == '/') {
                            inComment = true;
                            isSlashSlashComment = false;
                            break;
                        }
                        break;
                    }
                    case '-':
                    case '/': {
                        if (inQuote) {
                            break;
                        }
                        if (inComment) {
                            if (lastChar == '*' && !isSlashSlashComment) {
                                inComment = false;
                                break;
                            }
                            if (lastChar == car && isSlashSlashComment) {
                                inComment = false;
                                break;
                            }
                            break;
                        }
                        else {
                            if (lastChar == car) {
                                inComment = true;
                                isSlashSlashComment = true;
                                break;
                            }
                            if (lastChar == '*') {
                                inComment = true;
                                isSlashSlashComment = false;
                                break;
                            }
                            break;
                        }
                        break;
                    }
                    case '\n': {
                        if (inComment && isSlashSlashComment) {
                            inComment = false;
                            break;
                        }
                        break;
                    }
                    case '{': {
                        if (!inQuote && !inComment) {
                            ++inEscapeSeq;
                            break;
                        }
                        break;
                    }
                    case '}': {
                        if (!inQuote && !inComment && --inEscapeSeq == 0) {
                            escapeSequenceBuf.append(car);
                            sqlBuffer.append(resolveEscapes(escapeSequenceBuf.toString(), protocol));
                            escapeSequenceBuf.setLength(0);
                            lastChar = car;
                            continue;
                        }
                        break;
                    }
                }
                lastChar = car;
                if (inEscapeSeq > 0) {
                    escapeSequenceBuf.append(car);
                }
                else {
                    sqlBuffer.append(car);
                }
            }
        }
        if (inEscapeSeq > 0) {
            throw new SQLException("Invalid escape sequence , missing closing '}' character in '" + (Object)sqlBuffer);
        }
        return sqlBuffer.toString();
    }
    
    public static String getSQLWithoutCommentMysql(final String sql) throws SQLException {
        final StringBuilder escapeSequenceBuf = new StringBuilder();
        final StringBuilder sqlBuffer = new StringBuilder();
        final char[] charArray = sql.toCharArray();
        char lastChar = '\0';
        boolean inQuote = false;
        char quoteChar = '\0';
        boolean inComment = false;
        boolean isSlashSlashComment = false;
        boolean isDoubleDashComment = false;
        boolean inDoubleDashComment = false;
        boolean add = false;
        boolean skip = false;
        boolean afterDoubleDash = false;
        for (int i = 0; i < charArray.length; ++i) {
            char car = charArray[i];
            if (afterDoubleDash && isDoubleDashComment && inComment) {
                if (car == '\u007f' || car < '!') {
                    inDoubleDashComment = true;
                    afterDoubleDash = false;
                }
                else {
                    isDoubleDashComment = false;
                    afterDoubleDash = false;
                    inComment = false;
                    inDoubleDashComment = false;
                }
            }
            else {
                switch (car) {
                    case '\"':
                    case '\'':
                    case '`': {
                        if (inComment) {
                            break;
                        }
                        if (!inQuote) {
                            inQuote = true;
                            quoteChar = car;
                            break;
                        }
                        if (quoteChar == car) {
                            inQuote = false;
                            add = true;
                            break;
                        }
                        break;
                    }
                    case '*': {
                        if (!inQuote && !inComment && lastChar == '/') {
                            inComment = true;
                            isSlashSlashComment = false;
                            isDoubleDashComment = false;
                            break;
                        }
                        break;
                    }
                    case '/': {
                        if (inQuote) {
                            break;
                        }
                        if (!inComment) {
                            inComment = true;
                            break;
                        }
                        if (lastChar == '*' && !isSlashSlashComment && !isDoubleDashComment) {
                            inComment = false;
                            skip = true;
                            break;
                        }
                        break;
                    }
                    case '-': {
                        if (inQuote) {
                            break;
                        }
                        if (inComment && !isDoubleDashComment) {
                            isDoubleDashComment = false;
                            break;
                        }
                        if (inComment && isDoubleDashComment && lastChar == '-') {
                            afterDoubleDash = true;
                            inComment = true;
                            isDoubleDashComment = true;
                            break;
                        }
                        if (!inComment) {
                            isDoubleDashComment = true;
                            inComment = true;
                            break;
                        }
                        break;
                    }
                    case '#': {
                        if (inQuote) {
                            break;
                        }
                        if (inComment) {
                            isSlashSlashComment = false;
                            break;
                        }
                        inComment = true;
                        isSlashSlashComment = true;
                        break;
                    }
                    case '\n': {
                        if ((inComment && isSlashSlashComment) || (inComment && isDoubleDashComment && inDoubleDashComment)) {
                            inComment = false;
                            skip = true;
                            isDoubleDashComment = false;
                            isSlashSlashComment = false;
                            inDoubleDashComment = false;
                            afterDoubleDash = false;
                            break;
                        }
                        car = ' ';
                        break;
                    }
                    default: {
                        if (!inQuote && !inComment && !isSlashSlashComment && !isDoubleDashComment) {
                            add = true;
                            break;
                        }
                        break;
                    }
                }
            }
            lastChar = car;
            if (i == charArray.length - 1) {
                add = true;
            }
            escapeSequenceBuf.append(car);
            if (skip) {
                escapeSequenceBuf.setLength(0);
                skip = false;
            }
            if (add) {
                sqlBuffer.append((CharSequence)escapeSequenceBuf);
                add = false;
                escapeSequenceBuf.setLength(0);
            }
        }
        return sqlBuffer.toString().trim();
    }
    
    public static String trimSQLString(final String queryString, final boolean noBackslashEscapes, final boolean isOracleMode) {
        return trimSQLString(queryString, noBackslashEscapes, isOracleMode, false);
    }
    
    public static String trimSQLString(final String queryString, final boolean noBackslashEscapes, final boolean isOracleMode, final boolean skipComment) {
        return trimSQLStringInternal(queryString, noBackslashEscapes, isOracleMode, skipComment)[0];
    }
    
    public static String[] trimSQLStringInternal(final String queryString, final boolean noBackslashEscapes, final boolean isOracleMode, final boolean skipComment) {
        int parameterCount = 0;
        final StringBuilder trimedSqlString = new StringBuilder();
        boolean multipleQueriesPrepare = true;
        LexState state = LexState.Normal;
        char lastChar = '\0';
        boolean endingSemicolon = false;
        boolean singleQuotes = false;
        int lastParameterPosition = 0;
        final char[] query = queryString.toCharArray();
        final int queryLength = query.length;
        boolean slashend = false;
        boolean endNameBinding = false;
        final StringBuilder paramSb = new StringBuilder();
        boolean includeCurChar = false;
        int commentStart = 0;
        for (int i = 0; i < queryLength; ++i) {
            if (i == queryLength - 1 && state == LexState.NameBinding && isOracleMode) {
                endNameBinding = true;
                includeCurChar = true;
            }
            final char car = query[i];
            if (state == LexState.Escape && (car != '\'' || !singleQuotes) && (car != '\"' || singleQuotes)) {
                state = LexState.String;
                lastChar = car;
            }
            else {
                switch (car) {
                    case '*': {
                        if (state == LexState.Normal && lastChar == '/') {
                            state = LexState.SlashStarComment;
                            commentStart = i - 1;
                            break;
                        }
                        break;
                    }
                    case '/': {
                        if (state == LexState.SlashStarComment && lastChar == '*') {
                            state = LexState.Normal;
                            slashend = true;
                            if (skipComment) {
                                trimedSqlString.append(queryString.substring(lastParameterPosition, i + 1));
                            }
                            else if (commentStart != 0) {
                                trimedSqlString.append(queryString.substring(lastParameterPosition, commentStart));
                            }
                            lastParameterPosition = i + 1;
                            break;
                        }
                        if (state == LexState.Normal && slashend && lastChar == '/') {
                            slashend = false;
                            break;
                        }
                        if (state == LexState.Normal && lastChar == '/') {
                            state = LexState.EOLComment;
                            lastParameterPosition = i + 1;
                            commentStart = i - 1;
                            break;
                        }
                        break;
                    }
                    case '#': {
                        if (!isOracleMode && state == LexState.Normal) {
                            state = LexState.EOLComment;
                            commentStart = i;
                            break;
                        }
                        break;
                    }
                    case '-': {
                        if (state == LexState.Normal && lastChar == '-') {
                            state = LexState.EOLComment;
                            multipleQueriesPrepare = false;
                            commentStart = i - 1;
                            break;
                        }
                        break;
                    }
                    case '\n': {
                        if (state == LexState.EOLComment) {
                            multipleQueriesPrepare = true;
                            state = LexState.Normal;
                            if (skipComment) {
                                trimedSqlString.append(queryString.substring(lastParameterPosition, i + 1));
                            }
                            else if (commentStart != 0) {
                                trimedSqlString.append(queryString.substring(lastParameterPosition, commentStart));
                            }
                            lastParameterPosition = i + 1;
                        }
                        if (isOracleMode && state == LexState.NameBinding) {
                            endNameBinding = true;
                            includeCurChar = false;
                            break;
                        }
                        break;
                    }
                    case '\"': {
                        if (state == LexState.Normal) {
                            state = LexState.String;
                            singleQuotes = false;
                            break;
                        }
                        if (state == LexState.String && !singleQuotes) {
                            state = LexState.Normal;
                            break;
                        }
                        if (state == LexState.Escape && !singleQuotes) {
                            state = LexState.String;
                            break;
                        }
                        break;
                    }
                    case '\'': {
                        if (state == LexState.Normal) {
                            state = LexState.String;
                            singleQuotes = true;
                            break;
                        }
                        if (state == LexState.String && singleQuotes) {
                            state = LexState.Normal;
                            break;
                        }
                        if (state == LexState.Escape && singleQuotes) {
                            state = LexState.String;
                            break;
                        }
                        break;
                    }
                    case '\\': {
                        if (noBackslashEscapes) {
                            break;
                        }
                        if (!isOracleMode && state == LexState.String) {
                            state = LexState.Escape;
                            break;
                        }
                        break;
                    }
                    case ';': {
                        if (isOracleMode && state == LexState.NameBinding) {
                            endNameBinding = true;
                            includeCurChar = false;
                        }
                        if (state == LexState.Normal) {
                            endingSemicolon = true;
                            multipleQueriesPrepare = false;
                            break;
                        }
                        break;
                    }
                    case '?': {
                        if (state == LexState.Normal) {
                            trimedSqlString.append(queryString.substring(lastParameterPosition, i));
                            trimedSqlString.append("?");
                            lastParameterPosition = i + 1;
                            ++parameterCount;
                            break;
                        }
                        break;
                    }
                    case '`': {
                        if (state == LexState.Backtick) {
                            state = LexState.Normal;
                            break;
                        }
                        if (state == LexState.Normal) {
                            state = LexState.Backtick;
                            break;
                        }
                        break;
                    }
                    case ':': {
                        if (!isOracleMode) {
                            break;
                        }
                        if (state == LexState.Normal) {
                            state = LexState.NameBinding;
                        }
                        if (state == LexState.Normal && endingSemicolon && car >= '(') {
                            endingSemicolon = false;
                            multipleQueriesPrepare = true;
                            break;
                        }
                        break;
                    }
                    case '=': {
                        if (!isOracleMode) {
                            break;
                        }
                        if (state == LexState.NameBinding) {
                            state = LexState.Normal;
                            paramSb.setLength(0);
                        }
                        if (state == LexState.Normal && endingSemicolon && car >= '(') {
                            endingSemicolon = false;
                            multipleQueriesPrepare = true;
                            break;
                        }
                        break;
                    }
                    case ' ':
                    case ')':
                    case ',':
                    case '}': {
                        if (isOracleMode && state == LexState.NameBinding) {
                            endNameBinding = true;
                            includeCurChar = false;
                            break;
                        }
                        break;
                    }
                    default: {
                        if (state == LexState.Normal && endingSemicolon && car >= '(') {
                            endingSemicolon = false;
                            multipleQueriesPrepare = true;
                            break;
                        }
                        break;
                    }
                }
                if (isOracleMode && state == LexState.NameBinding && car >= '(' && !endNameBinding) {
                    paramSb.append(car);
                }
                if (isOracleMode && state == LexState.NameBinding && endNameBinding) {
                    if (!includeCurChar) {
                        trimedSqlString.append(queryString.substring(lastParameterPosition, i - paramSb.length()));
                        trimedSqlString.append("?");
                        lastParameterPosition = i;
                    }
                    else {
                        paramSb.append(car);
                        trimedSqlString.append(queryString.substring(lastParameterPosition, i - paramSb.length() + 1));
                        trimedSqlString.append("?");
                        lastParameterPosition = i + 1;
                        includeCurChar = false;
                    }
                    paramSb.setLength(0);
                    endNameBinding = false;
                    state = LexState.Normal;
                    ++parameterCount;
                }
                lastChar = car;
            }
        }
        if (lastParameterPosition == 0) {
            trimedSqlString.append(queryString);
        }
        else {
            trimedSqlString.append(queryString.substring(lastParameterPosition, queryLength));
        }
        return new String[] { trimedSqlString.toString(), String.valueOf(parameterCount) };
    }
    
    private static int nextCharIndex(final int startPos, final int stopPos, final String searchedString, final String leftMarks, final String rightMarks) {
        if (searchedString == null) {
            return -1;
        }
        final int searchStringLength = searchedString.length();
        if (startPos >= searchStringLength) {
            return -1;
        }
        char charVal0 = '\0';
        char charVal2 = searchedString.charAt(startPos);
        char charVal3 = (startPos + 1 < searchStringLength) ? searchedString.charAt(startPos + 1) : '\0';
        for (int i = startPos; i <= stopPos; ++i) {
            charVal0 = charVal2;
            charVal2 = charVal3;
            charVal3 = ((i + 2 < searchStringLength) ? searchedString.charAt(i + 2) : '\0');
            int markerIndex = -1;
            if ((markerIndex = leftMarks.indexOf(charVal0)) == -1) {
                return i;
            }
            int nestedMarkersCount = 0;
            final char openingMarker = charVal0;
            final char closingMarker = rightMarks.charAt(markerIndex);
            while (++i <= stopPos && ((charVal0 = searchedString.charAt(i)) != closingMarker || nestedMarkersCount != 0)) {
                if (charVal0 == openingMarker) {
                    ++nestedMarkersCount;
                }
                else if (charVal0 == closingMarker) {
                    --nestedMarkersCount;
                }
                else {
                    if (charVal0 != '\\') {
                        continue;
                    }
                    ++i;
                }
            }
            charVal2 = ((i + 1 < searchStringLength) ? searchedString.charAt(i + 1) : '\0');
            charVal3 = ((i + 2 < searchStringLength) ? searchedString.charAt(i + 2) : '\0');
        }
        return -1;
    }
    
    public static int nextDelimiterPos(final String stringToSearched, final int startingPosition, final String stringToSearch, final String leftMarks, final String rightMarks) {
        if (stringToSearched == null || stringToSearch == null) {
            return -1;
        }
        final int stringToSearchedLen = stringToSearched.length();
        final int stringToSearchLen = stringToSearch.length();
        final int stopSearchingAt = stringToSearchedLen - stringToSearchLen;
        if (startingPosition > stopSearchingAt || stringToSearchLen == 0) {
            return -1;
        }
        final char firstUc = Character.toUpperCase(stringToSearch.charAt(0));
        final char firstLc = Character.toLowerCase(stringToSearch.charAt(0));
        for (int i = startingPosition; i <= stopSearchingAt; ++i) {
            i = nextCharIndex(i, stopSearchingAt, stringToSearched, leftMarks, rightMarks);
            if (i == -1) {
                return -1;
            }
            final char c = stringToSearched.charAt(i);
            if ((c == firstLc || c == firstUc) && stringToSearched.toUpperCase(Locale.ROOT).substring(i).startsWith(stringToSearch)) {
                return i;
            }
        }
        return -1;
    }
    
    public static List<ParsedCallParameters> argumentsSplit(String arguments, final String delimiter, final String markers, final String markerCloses) {
        if (arguments == null) {
            return null;
        }
        arguments = arguments.substring(arguments.indexOf("(") + 1, arguments.lastIndexOf(")"));
        final List<ParsedCallParameters> retList = new ArrayList<ParsedCallParameters>();
        final boolean trim = true;
        if (delimiter == null) {
            throw new IllegalArgumentException();
        }
        int delimPos;
        int currentPos;
        for (delimPos = 0, currentPos = 0; (delimPos = nextDelimiterPos(arguments, currentPos, delimiter, markers, markerCloses)) != -1; currentPos = delimPos + 1) {
            String token = arguments.substring(currentPos, delimPos);
            if (trim) {
                token = token.trim();
            }
            if (token.startsWith(":") || token.startsWith("?")) {
                retList.add(new ParsedCallParameters(true, token));
            }
            else {
                retList.add(new ParsedCallParameters(false, token));
            }
        }
        if (currentPos < arguments.length()) {
            String token = arguments.substring(currentPos);
            if (trim) {
                token = token.trim();
            }
            if (token.startsWith(":") || token.startsWith("?")) {
                retList.add(new ParsedCallParameters(true, token));
            }
            else {
                retList.add(new ParsedCallParameters(false, token));
            }
        }
        return retList;
    }
    
    public static int getStatementType(final String queryString) {
        if (queryString.substring(0, 4).equalsIgnoreCase("with")) {
            return 1;
        }
        if (queryString.substring(0, 6).equalsIgnoreCase("select")) {
            return 1;
        }
        if (queryString.substring(0, 6).equalsIgnoreCase("update")) {
            return 2;
        }
        if (queryString.substring(0, 6).equalsIgnoreCase("delete")) {
            return 3;
        }
        if (queryString.substring(0, 6).equalsIgnoreCase("insert")) {
            return 4;
        }
        if (queryString.substring(0, 6).equalsIgnoreCase("create")) {
            return 5;
        }
        if (queryString.substring(0, 4).equalsIgnoreCase("drop")) {
            return 6;
        }
        if (queryString.substring(0, 5).equalsIgnoreCase("alter")) {
            return 7;
        }
        if (queryString.substring(0, 5).equalsIgnoreCase("begin")) {
            return 8;
        }
        if (queryString.substring(0, 7).equalsIgnoreCase("declare")) {
            return 9;
        }
        if (queryString.substring(0, 4).equalsIgnoreCase("call")) {
            return 10;
        }
        return 0;
    }
    
    public static Protocol retrieveProxy(final UrlParser urlParser, final GlobalStateInfo globalInfo) throws SQLException {
        final ReentrantLock lock = new ReentrantLock();
        final LruTraceCache traceCache = urlParser.getOptions().enablePacketDebug ? new LruTraceCache() : null;
        switch (urlParser.getHaMode()) {
            case AURORA: {
                return getProxyLoggingIfNeeded(urlParser, (Protocol)java.lang.reflect.Proxy.newProxyInstance(AuroraProtocol.class.getClassLoader(), new Class[] { Protocol.class }, new FailoverProxy(new AuroraListener(urlParser, globalInfo), lock, traceCache)));
            }
            case REPLICATION: {
                return getProxyLoggingIfNeeded(urlParser, (Protocol)java.lang.reflect.Proxy.newProxyInstance(MastersSlavesProtocol.class.getClassLoader(), new Class[] { Protocol.class }, new FailoverProxy(new MastersSlavesListener(urlParser, globalInfo), lock, traceCache)));
            }
            case LOADBALANCE:
            case SEQUENTIAL: {
                return getProxyLoggingIfNeeded(urlParser, (Protocol)java.lang.reflect.Proxy.newProxyInstance(MasterProtocol.class.getClassLoader(), new Class[] { Protocol.class }, new FailoverProxy(new MastersFailoverListener(urlParser, globalInfo), lock, traceCache)));
            }
            default: {
                final Protocol protocol = getProxyLoggingIfNeeded(urlParser, new MasterProtocol(urlParser, globalInfo, lock, traceCache));
                protocol.connectWithoutProxy();
                return protocol;
            }
        }
    }
    
    private static Protocol getProxyLoggingIfNeeded(final UrlParser urlParser, final Protocol protocol) {
        if (urlParser.getOptions().profileSql || urlParser.getOptions().slowQueryThresholdNanos != null) {
            return (Protocol)java.lang.reflect.Proxy.newProxyInstance(MasterProtocol.class.getClassLoader(), new Class[] { Protocol.class }, new ProtocolLoggingProxy(protocol, urlParser.getOptions()));
        }
        return protocol;
    }
    
    public static TimeZone getTimeZone(final String id) throws SQLException {
        final TimeZone tz = TimeZone.getTimeZone(id);
        if ("GMT".equals(tz.getID()) && !"GMT".equals(id)) {
            throw new SQLException("invalid timezone id '" + id + "'");
        }
        return tz;
    }
    
    public static Socket createSocket(final Options options, final String host) throws IOException {
        return Utils.socketHandler.apply(options, host);
    }
    
    public static String hexdump(final byte[]... bytes) {
        return hexdump(Integer.MAX_VALUE, 0, Integer.MAX_VALUE, bytes);
    }
    
    public static String hexdump(final int maxQuerySizeToLog, final int offset, final int length, final byte[]... byteArr) {
        switch (byteArr.length) {
            case 0: {
                return "";
            }
            case 1: {
                final byte[] bytes = byteArr[0];
                if (bytes.length <= offset) {
                    return "";
                }
                final int dataLength = Math.min(maxQuerySizeToLog, Math.min(bytes.length - offset, length));
                final StringBuilder outputBuilder = new StringBuilder(dataLength * 5);
                outputBuilder.append("\n");
                writeHex(bytes, offset, dataLength, outputBuilder);
                return outputBuilder.toString();
            }
            default: {
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                try {
                    outputStream.write(byteArr[0]);
                    outputStream.write(byteArr[1], offset, Math.min(length, byteArr[1].length));
                    for (int i = 2; i < byteArr.length; ++i) {
                        outputStream.write(byteArr[i]);
                    }
                }
                catch (IOException ex) {}
                final byte[] concat = outputStream.toByteArray();
                if (concat.length <= offset) {
                    return "";
                }
                final int stlength = Math.min(maxQuerySizeToLog, outputStream.size());
                final StringBuilder out = new StringBuilder(stlength * 3 + 80);
                out.append("\n");
                writeHex(outputStream.toByteArray(), 0, outputStream.size(), out);
                return out.toString();
            }
        }
    }
    
    private static void writeHex(final byte[] bytes, final int offset, final int dataLength, final StringBuilder outputBuilder) {
        if (bytes == null || bytes.length == 0) {
            return;
        }
        final char[] hexaValue = { '\0', '\0', '\0', '\0', '\0', '\0', '\0', '\0', ' ', '\0', '\0', '\0', '\0', '\0', '\0', '\0' };
        int pos = offset;
        int posHexa = 0;
        outputBuilder.append("+--------------------------------------------------+\n|  0  1  2  3  4  5  6  7   8  9  a  b  c  d  e  f |\n+--------------------------------------------------+------------------+\n| ");
        while (pos < dataLength + offset) {
            final int byteValue = bytes[pos] & 0xFF;
            outputBuilder.append(Utils.hexArray[byteValue >>> 4]).append(Utils.hexArray[byteValue & 0xF]).append(" ");
            hexaValue[posHexa++] = ((byteValue > 31 && byteValue < 127) ? ((char)byteValue) : '.');
            if (posHexa == 8) {
                outputBuilder.append(" ");
            }
            if (posHexa == 16) {
                outputBuilder.append("| ").append(hexaValue).append(" |\n");
                if (pos + 1 != dataLength + offset) {
                    outputBuilder.append("| ");
                }
                posHexa = 0;
            }
            ++pos;
        }
        int remaining = posHexa;
        if (remaining > 0) {
            if (remaining < 8) {
                while (remaining < 8) {
                    outputBuilder.append("   ");
                    ++remaining;
                }
                outputBuilder.append(" ");
            }
            while (remaining < 16) {
                outputBuilder.append("   ");
                ++remaining;
            }
            while (posHexa < 16) {
                hexaValue[posHexa] = ' ';
                ++posHexa;
            }
            outputBuilder.append("| ").append(hexaValue).append(" |\n");
        }
        outputBuilder.append("+--------------------------------------------------+------------------+\n");
    }
    
    private static String getHex(final byte[] raw) {
        final StringBuilder hex = new StringBuilder(2 * raw.length);
        for (final byte b : raw) {
            hex.append(Utils.hexArray[(b & 0xF0) >> 4]).append(Utils.hexArray[b & 0xF]);
        }
        return hex.toString();
    }
    
    public static String byteArrayToHexString(final byte[] bytes) {
        return (bytes != null) ? getHex(bytes) : "";
    }
    
    public static String intToHexString(final int value) {
        final StringBuilder hex = new StringBuilder(8);
        int offset = 24;
        boolean nullEnd = false;
        while (offset >= 0) {
            final byte b = (byte)(value >> offset);
            offset -= 8;
            if (b != 0 || nullEnd) {
                nullEnd = true;
                hex.append(Utils.hexArray[(b & 0xF0) >> 4]).append(Utils.hexArray[b & 0xF]);
            }
        }
        return hex.toString();
    }
    
    public static String parseSessionVariables(final String sessionVariable) {
        final StringBuilder out = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        Parse state = Parse.Normal;
        boolean iskey = true;
        boolean singleQuotes = true;
        boolean first = true;
        String key = null;
        final char[] charArray;
        final char[] chars = charArray = sessionVariable.toCharArray();
        for (final char car : charArray) {
            Label_0388: {
                if (state == Parse.Escape) {
                    sb.append(car);
                    state = (singleQuotes ? Parse.Quote : Parse.String);
                }
                else {
                    switch (car) {
                        case '\"': {
                            if (state == Parse.Normal) {
                                state = Parse.String;
                                singleQuotes = false;
                                break;
                            }
                            if (state == Parse.String && !singleQuotes) {
                                state = Parse.Normal;
                                break;
                            }
                            break;
                        }
                        case '\'': {
                            if (state == Parse.Normal) {
                                state = Parse.String;
                                singleQuotes = true;
                                break;
                            }
                            if (state == Parse.String && singleQuotes) {
                                state = Parse.Normal;
                                break;
                            }
                            break;
                        }
                        case '\\': {
                            if (state == Parse.String) {
                                state = Parse.Escape;
                                break;
                            }
                            break;
                        }
                        case ',':
                        case ';': {
                            if (state == Parse.Normal) {
                                if (!iskey) {
                                    if (!first) {
                                        out.append(",");
                                    }
                                    out.append(key);
                                    out.append(sb.toString());
                                    first = false;
                                }
                                else {
                                    key = sb.toString().trim();
                                    if (!key.isEmpty()) {
                                        if (!first) {
                                            out.append(",");
                                        }
                                        out.append(key);
                                        first = false;
                                    }
                                }
                                iskey = true;
                                key = null;
                                sb = new StringBuilder();
                                break Label_0388;
                            }
                            break;
                        }
                        case '=': {
                            if (state == Parse.Normal && iskey) {
                                key = sb.toString().trim();
                                iskey = false;
                                sb = new StringBuilder();
                                break;
                            }
                            break;
                        }
                    }
                    sb.append(car);
                }
            }
        }
        if (!iskey) {
            if (!first) {
                out.append(",");
            }
            out.append(key);
            out.append(sb.toString());
        }
        else {
            final String tmpkey = sb.toString().trim();
            if (!tmpkey.isEmpty() && !first) {
                out.append(",");
            }
            out.append(tmpkey);
        }
        return out.toString();
    }
    
    public static boolean isIPv4(final String ip) {
        return Utils.IP_V4.matcher(ip).matches();
    }
    
    public static boolean isIPv6(final String ip) {
        return Utils.IP_V6.matcher(ip).matches() || Utils.IP_V6_COMPRESSED.matcher(ip).matches();
    }
    
    public static int transactionFromString(final String txIsolation) throws SQLException {
        switch (txIsolation) {
            case "READ-UNCOMMITTED": {
                return 1;
            }
            case "READ-COMMITTED": {
                return 2;
            }
            case "REPEATABLE-READ": {
                return 4;
            }
            case "SERIALIZABLE": {
                return 8;
            }
            default: {
                throw new SQLException("unknown transaction isolation level");
            }
        }
    }
    
    public static boolean validateFileName(final String sql, final ParameterHolder[] parameters, final String fileName) {
        Pattern pattern = Pattern.compile("^(\\s*\\/\\*([^\\*]|\\*[^\\/])*\\*\\/)*\\s*LOAD\\s+DATA\\s+((LOW_PRIORITY|CONCURRENT)\\s+)?LOCAL\\s+INFILE\\s+'" + fileName + "'", 2);
        if (pattern.matcher(sql).find()) {
            return true;
        }
        if (parameters != null) {
            pattern = Pattern.compile("^(\\s*\\/\\*([^\\*]|\\*[^\\/])*\\*\\/)*\\s*LOAD\\s+DATA\\s+((LOW_PRIORITY|CONCURRENT)\\s+)?LOCAL\\s+INFILE\\s+\\?", 2);
            if (pattern.matcher(sql).find() && parameters.length > 0) {
                return parameters[0].toString().toLowerCase().equals("'" + fileName.toLowerCase() + "'");
            }
        }
        return false;
    }
    
    protected static char[] encodeHex(final byte[] data, final char[] toDigits) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        int i = 0;
        int j = 0;
        while (i < l) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0xF & data[i]];
            ++i;
        }
        return out;
    }
    
    public static String encodeHexStr(final byte[] data) {
        return encodeHexStr(data, false);
    }
    
    public static String encodeHexStr(final byte[] data, final boolean toLowerCase) {
        return encodeHexStr(data, toLowerCase ? Utils.DIGITS_LOWER : Utils.DIGITS_UPPER);
    }
    
    protected static String encodeHexStr(final byte[] data, final char[] toDigits) {
        return new String(encodeHex(data, toDigits));
    }
    
    public static String toHexString(final byte[] data) {
        return encodeHexStr(data);
    }
    
    public static boolean convertStringToBoolean(final String str) {
        if (str != null && str.length() > 0) {
            final int c = Character.toLowerCase(str.charAt(0));
            return c == 116 || c == 121 || c == 49 || str.equals("-1");
        }
        return false;
    }
    
    static {
        hexArray = "0123456789ABCDEF".toCharArray();
        IP_V4 = Pattern.compile("^(([1-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){1}(([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){2}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");
        IP_V6 = Pattern.compile("^[0-9a-fA-F]{1,4}(:[0-9a-fA-F]{1,4}){7}$");
        IP_V6_COMPRESSED = Pattern.compile("^(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)::(([0-9A-Fa-f]{1,4}(:[0-9A-Fa-f]{1,4}){0,5})?)$");
        SocketHandlerFunction init;
        try {
            init = SocketUtility.getSocketHandler();
        }
        catch (Throwable t) {
            final SocketHandlerFunction defaultSocketHandler = init = ((options, host) -> standardSocket(options, host));
        }
        socketHandler = init;
        DIGITS_LOWER = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
        DIGITS_UPPER = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    }
    
    enum LexState
    {
        Normal, 
        String, 
        SlashStarComment, 
        Escape, 
        EOLComment, 
        Backtick, 
        NameBinding;
    }
    
    private enum Parse
    {
        Normal, 
        String, 
        Quote, 
        Escape;
    }
}
