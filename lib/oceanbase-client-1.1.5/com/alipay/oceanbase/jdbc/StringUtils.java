// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.util.Collections;
import java.nio.CharBuffer;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.EnumSet;
import java.sql.SQLException;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.reflect.Method;
import java.util.Set;

public class StringUtils
{
    public static final Set<SearchMode> SEARCH_MODE__ALL;
    public static final Set<SearchMode> SEARCH_MODE__MRK_COM_WS;
    public static final Set<SearchMode> SEARCH_MODE__BSESC_COM_WS;
    public static final Set<SearchMode> SEARCH_MODE__BSESC_MRK_WS;
    public static final Set<SearchMode> SEARCH_MODE__COM_WS;
    public static final Set<SearchMode> SEARCH_MODE__MRK_WS;
    public static final Set<SearchMode> SEARCH_MODE__NONE;
    private static final int NON_COMMENTS_MYSQL_VERSION_REF_LENGTH = 5;
    private static final int BYTE_RANGE = 256;
    private static byte[] allBytes;
    private static char[] byteToChars;
    private static Method toPlainStringMethod;
    static final int WILD_COMPARE_MATCH_NO_WILD = 0;
    static final int WILD_COMPARE_MATCH_WITH_WILD = 1;
    static final int WILD_COMPARE_NO_MATCH = -1;
    private static final ConcurrentHashMap<String, Charset> charsetsByAlias;
    private static final String platformEncoding;
    private static final String VALID_ID_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789$_#@";
    private static final char[] HEX_DIGITS;
    
    static Charset findCharset(final String alias) throws UnsupportedEncodingException {
        try {
            Charset cs = StringUtils.charsetsByAlias.get(alias);
            if (cs == null) {
                cs = Charset.forName(alias);
                final Charset oldCs = StringUtils.charsetsByAlias.putIfAbsent(alias, cs);
                if (oldCs != null) {
                    cs = oldCs;
                }
            }
            return cs;
        }
        catch (UnsupportedCharsetException uce) {
            throw new UnsupportedEncodingException(alias);
        }
        catch (IllegalCharsetNameException icne) {
            throw new UnsupportedEncodingException(alias);
        }
        catch (IllegalArgumentException iae) {
            throw new UnsupportedEncodingException(alias);
        }
    }
    
    public static String consistentToString(final BigDecimal decimal) {
        if (decimal == null) {
            return null;
        }
        if (StringUtils.toPlainStringMethod != null) {
            try {
                return (String)StringUtils.toPlainStringMethod.invoke(decimal, (Object[])null);
            }
            catch (InvocationTargetException ex) {}
            catch (IllegalAccessException ex2) {}
        }
        return decimal.toString();
    }
    
    public static String dumpAsHex(final byte[] byteBuffer, final int length) {
        final StringBuilder outputBuilder = new StringBuilder(length * 4);
        int p = 0;
        for (int rows = length / 8, i = 0; i < rows && p < length; ++i) {
            int ptemp = p;
            for (int j = 0; j < 8; ++j) {
                String hexVal = Integer.toHexString(byteBuffer[ptemp] & 0xFF);
                if (hexVal.length() == 1) {
                    hexVal = "0" + hexVal;
                }
                outputBuilder.append(hexVal + " ");
                ++ptemp;
            }
            outputBuilder.append("    ");
            for (int j = 0; j < 8; ++j) {
                final int b = 0xFF & byteBuffer[p];
                if (b > 32 && b < 127) {
                    outputBuilder.append((char)b + " ");
                }
                else {
                    outputBuilder.append(". ");
                }
                ++p;
            }
            outputBuilder.append("\n");
        }
        int n = 0;
        for (int k = p; k < length; ++k) {
            String hexVal2 = Integer.toHexString(byteBuffer[k] & 0xFF);
            if (hexVal2.length() == 1) {
                hexVal2 = "0" + hexVal2;
            }
            outputBuilder.append(hexVal2 + " ");
            ++n;
        }
        for (int k = n; k < 8; ++k) {
            outputBuilder.append("   ");
        }
        outputBuilder.append("    ");
        for (int k = p; k < length; ++k) {
            final int b2 = 0xFF & byteBuffer[k];
            if (b2 > 32 && b2 < 127) {
                outputBuilder.append((char)b2 + " ");
            }
            else {
                outputBuilder.append(". ");
            }
        }
        outputBuilder.append("\n");
        return outputBuilder.toString();
    }
    
    private static boolean endsWith(final byte[] dataFrom, final String suffix) {
        for (int i = 1; i <= suffix.length(); ++i) {
            final int dfOffset = dataFrom.length - i;
            final int suffixOffset = suffix.length() - i;
            if (dataFrom[dfOffset] != suffix.charAt(suffixOffset)) {
                return false;
            }
        }
        return true;
    }
    
    public static byte[] escapeEasternUnicodeByteStream(final byte[] origBytes, final String origString) {
        if (origBytes == null) {
            return null;
        }
        if (origBytes.length == 0) {
            return new byte[0];
        }
        final int bytesLen = origBytes.length;
        int bufIndex = 0;
        int strIndex = 0;
        final ByteArrayOutputStream bytesOut = new ByteArrayOutputStream(bytesLen);
        while (true) {
            if (origString.charAt(strIndex) == '\\') {
                bytesOut.write(origBytes[bufIndex++]);
            }
            else {
                int loByte = origBytes[bufIndex];
                if (loByte < 0) {
                    loByte += 256;
                }
                bytesOut.write(loByte);
                if (loByte >= 128) {
                    if (bufIndex < bytesLen - 1) {
                        int hiByte = origBytes[bufIndex + 1];
                        if (hiByte < 0) {
                            hiByte += 256;
                        }
                        bytesOut.write(hiByte);
                        ++bufIndex;
                        if (hiByte == 92) {
                            bytesOut.write(hiByte);
                        }
                    }
                }
                else if (loByte == 92 && bufIndex < bytesLen - 1) {
                    int hiByte = origBytes[bufIndex + 1];
                    if (hiByte < 0) {
                        hiByte += 256;
                    }
                    if (hiByte == 98) {
                        bytesOut.write(92);
                        bytesOut.write(98);
                        ++bufIndex;
                    }
                }
                ++bufIndex;
            }
            if (bufIndex >= bytesLen) {
                break;
            }
            ++strIndex;
        }
        return bytesOut.toByteArray();
    }
    
    public static char firstNonWsCharUc(final String searchIn) {
        return firstNonWsCharUc(searchIn, 0);
    }
    
    public static char firstNonWsCharUc(final String searchIn, final int startAt) {
        if (searchIn == null) {
            return '\0';
        }
        for (int length = searchIn.length(), i = startAt; i < length; ++i) {
            final char c = searchIn.charAt(i);
            if (!Character.isWhitespace(c)) {
                return Character.toUpperCase(c);
            }
        }
        return '\0';
    }
    
    public static char firstAlphaCharUc(final String searchIn, final int startAt) {
        if (searchIn == null) {
            return '\0';
        }
        for (int length = searchIn.length(), i = startAt; i < length; ++i) {
            final char c = searchIn.charAt(i);
            if (Character.isLetter(c)) {
                return Character.toUpperCase(c);
            }
        }
        return '\0';
    }
    
    public static String fixDecimalExponent(String dString) {
        int ePos = dString.indexOf(69);
        if (ePos == -1) {
            ePos = dString.indexOf(101);
        }
        if (ePos != -1 && dString.length() > ePos + 1) {
            final char maybeMinusChar = dString.charAt(ePos + 1);
            if (maybeMinusChar != '-' && maybeMinusChar != '+') {
                final StringBuilder strBuilder = new StringBuilder(dString.length() + 1);
                strBuilder.append(dString.substring(0, ePos + 1));
                strBuilder.append('+');
                strBuilder.append(dString.substring(ePos + 1, dString.length()));
                dString = strBuilder.toString();
            }
        }
        return dString;
    }
    
    public static byte[] getBytes(final char[] c, final SingleByteCharsetConverter converter, final String encoding, final String serverEncoding, final boolean parserKnowsUnicode, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            byte[] b;
            if (converter != null) {
                b = converter.toBytes(c);
            }
            else if (encoding == null) {
                b = getBytes(c);
            }
            else {
                b = getBytes(c, encoding);
                if (!parserKnowsUnicode && CharsetMapping.requiresEscapeEasternUnicode(encoding) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = escapeEasternUnicodeByteStream(b, new String(c));
                }
            }
            return b;
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.0") + encoding + Messages.getString("StringUtils.1"), "S1009", exceptionInterceptor);
        }
    }
    
    public static byte[] getBytes(final char[] c, final SingleByteCharsetConverter converter, final String encoding, final String serverEncoding, final int offset, final int length, final boolean parserKnowsUnicode, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            byte[] b;
            if (converter != null) {
                b = converter.toBytes(c, offset, length);
            }
            else if (encoding == null) {
                b = getBytes(c, offset, length);
            }
            else {
                b = getBytes(c, offset, length, encoding);
                if (!parserKnowsUnicode && CharsetMapping.requiresEscapeEasternUnicode(encoding) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = escapeEasternUnicodeByteStream(b, new String(c, offset, length));
                }
            }
            return b;
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.0") + encoding + Messages.getString("StringUtils.1"), "S1009", exceptionInterceptor);
        }
    }
    
    public static byte[] getBytes(final char[] c, final String encoding, final String serverEncoding, final boolean parserKnowsUnicode, final MySQLConnection conn, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            final SingleByteCharsetConverter converter = (conn != null) ? conn.getCharsetConverter(encoding) : SingleByteCharsetConverter.getInstance(encoding, null);
            return getBytes(c, converter, encoding, serverEncoding, parserKnowsUnicode, exceptionInterceptor);
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.0") + encoding + Messages.getString("StringUtils.1"), "S1009", exceptionInterceptor);
        }
    }
    
    public static byte[] getBytes(final String s, final SingleByteCharsetConverter converter, final String encoding, final String serverEncoding, final boolean parserKnowsUnicode, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            byte[] b;
            if (converter != null) {
                b = converter.toBytes(s);
            }
            else if (encoding == null) {
                b = getBytes(s);
            }
            else {
                b = getBytes(s, encoding);
                if (!parserKnowsUnicode && CharsetMapping.requiresEscapeEasternUnicode(encoding) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = escapeEasternUnicodeByteStream(b, s);
                }
            }
            return b;
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.5") + encoding + Messages.getString("StringUtils.6"), "S1009", exceptionInterceptor);
        }
    }
    
    public static byte[] getBytes(String s, final SingleByteCharsetConverter converter, final String encoding, final String serverEncoding, final int offset, final int length, final boolean parserKnowsUnicode, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            byte[] b;
            if (converter != null) {
                b = converter.toBytes(s, offset, length);
            }
            else if (encoding == null) {
                b = getBytes(s, offset, length);
            }
            else {
                s = s.substring(offset, offset + length);
                b = getBytes(s, encoding);
                if (!parserKnowsUnicode && CharsetMapping.requiresEscapeEasternUnicode(encoding) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = escapeEasternUnicodeByteStream(b, s);
                }
            }
            return b;
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.5") + encoding + Messages.getString("StringUtils.6"), "S1009", exceptionInterceptor);
        }
    }
    
    public static byte[] getBytes(final String s, final String encoding, final String serverEncoding, final boolean parserKnowsUnicode, final MySQLConnection conn, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            final SingleByteCharsetConverter converter = (conn != null) ? conn.getCharsetConverter(encoding) : SingleByteCharsetConverter.getInstance(encoding, null);
            return getBytes(s, converter, encoding, serverEncoding, parserKnowsUnicode, exceptionInterceptor);
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.5") + encoding + Messages.getString("StringUtils.6"), "S1009", exceptionInterceptor);
        }
    }
    
    public static final byte[] getBytes(final String s, final String encoding, final String serverEncoding, final int offset, final int length, final boolean parserKnowsUnicode, final MySQLConnection conn, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            final SingleByteCharsetConverter converter = (conn != null) ? conn.getCharsetConverter(encoding) : SingleByteCharsetConverter.getInstance(encoding, null);
            return getBytes(s, converter, encoding, serverEncoding, offset, length, parserKnowsUnicode, exceptionInterceptor);
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.5") + encoding + Messages.getString("StringUtils.6"), "S1009", exceptionInterceptor);
        }
    }
    
    public static byte[] getBytesWrapped(String s, final char beginWrap, final char endWrap, final SingleByteCharsetConverter converter, final String encoding, final String serverEncoding, final boolean parserKnowsUnicode, final ExceptionInterceptor exceptionInterceptor) throws SQLException {
        try {
            byte[] b;
            if (converter != null) {
                b = converter.toBytesWrapped(s, beginWrap, endWrap);
            }
            else if (encoding == null) {
                final StringBuilder strBuilder = new StringBuilder(s.length() + 2);
                strBuilder.append(beginWrap);
                strBuilder.append(s);
                strBuilder.append(endWrap);
                b = getBytes(strBuilder.toString());
            }
            else {
                final StringBuilder strBuilder = new StringBuilder(s.length() + 2);
                strBuilder.append(beginWrap);
                strBuilder.append(s);
                strBuilder.append(endWrap);
                s = strBuilder.toString();
                b = getBytes(s, encoding);
                if (!parserKnowsUnicode && CharsetMapping.requiresEscapeEasternUnicode(encoding) && !encoding.equalsIgnoreCase(serverEncoding)) {
                    b = escapeEasternUnicodeByteStream(b, s);
                }
            }
            return b;
        }
        catch (UnsupportedEncodingException uee) {
            throw SQLError.createSQLException(Messages.getString("StringUtils.10") + encoding + Messages.getString("StringUtils.11"), "S1009", exceptionInterceptor);
        }
    }
    
    public static int getInt(final byte[] buf) throws NumberFormatException {
        return getInt(buf, 0, buf.length);
    }
    
    public static int getInt(final byte[] buf, final int offset, final int endPos) throws NumberFormatException {
        final int base = 10;
        int s;
        for (s = offset; s < endPos && Character.isWhitespace((char)buf[s]); ++s) {}
        if (s == endPos) {
            throw new NumberFormatException(toString(buf));
        }
        boolean negative = false;
        if ((char)buf[s] == '-') {
            negative = true;
            ++s;
        }
        else if ((char)buf[s] == '+') {
            ++s;
        }
        final int save = s;
        final int cutoff = Integer.MAX_VALUE / base;
        int cutlim = Integer.MAX_VALUE % base;
        if (negative) {
            ++cutlim;
        }
        boolean overflow = false;
        int i = 0;
        while (s < endPos) {
            char c = (char)buf[s];
            if (Character.isDigit(c)) {
                c -= '0';
            }
            else {
                if (!Character.isLetter(c)) {
                    break;
                }
                c = (char)(Character.toUpperCase(c) - 'A' + 10);
            }
            if (c >= base) {
                break;
            }
            if (i > cutoff || (i == cutoff && c > cutlim)) {
                overflow = true;
            }
            else {
                i *= base;
                i += c;
            }
            ++s;
        }
        if (s == save) {
            throw new NumberFormatException(toString(buf));
        }
        if (overflow) {
            throw new NumberFormatException(toString(buf));
        }
        return negative ? (-i) : i;
    }
    
    public static long getLong(final byte[] buf) throws NumberFormatException {
        return getLong(buf, 0, buf.length);
    }
    
    public static long getLong(final byte[] buf, final int offset, final int endpos) throws NumberFormatException {
        final int base = 10;
        int s;
        for (s = offset; s < endpos && Character.isWhitespace((char)buf[s]); ++s) {}
        if (s == endpos) {
            throw new NumberFormatException(toString(buf));
        }
        boolean negative = false;
        if ((char)buf[s] == '-') {
            negative = true;
            ++s;
        }
        else if ((char)buf[s] == '+') {
            ++s;
        }
        final int save = s;
        final long cutoff = Long.MAX_VALUE / base;
        long cutlim = (int)(Long.MAX_VALUE % base);
        if (negative) {
            ++cutlim;
        }
        boolean overflow = false;
        long i = 0L;
        while (s < endpos) {
            char c = (char)buf[s];
            if (Character.isDigit(c)) {
                c -= '0';
            }
            else {
                if (!Character.isLetter(c)) {
                    break;
                }
                c = (char)(Character.toUpperCase(c) - 'A' + 10);
            }
            if (c >= base) {
                break;
            }
            if (i > cutoff || (i == cutoff && c > cutlim)) {
                overflow = true;
            }
            else {
                i *= base;
                i += c;
            }
            ++s;
        }
        if (s == save) {
            throw new NumberFormatException(toString(buf));
        }
        if (overflow) {
            throw new NumberFormatException(toString(buf));
        }
        return negative ? (-i) : i;
    }
    
    public static short getShort(final byte[] buf) throws NumberFormatException {
        return getShort(buf, 0, buf.length);
    }
    
    public static short getShort(final byte[] buf, final int offset, final int endpos) throws NumberFormatException {
        final short base = 10;
        int s;
        for (s = offset; s < endpos && Character.isWhitespace((char)buf[s]); ++s) {}
        if (s == endpos) {
            throw new NumberFormatException(toString(buf));
        }
        boolean negative = false;
        if ((char)buf[s] == '-') {
            negative = true;
            ++s;
        }
        else if ((char)buf[s] == '+') {
            ++s;
        }
        final int save = s;
        final short cutoff = (short)(32767 / base);
        short cutlim = (short)(32767 % base);
        if (negative) {
            ++cutlim;
        }
        boolean overflow = false;
        short i = 0;
        while (s < endpos) {
            char c = (char)buf[s];
            if (Character.isDigit(c)) {
                c -= '0';
            }
            else {
                if (!Character.isLetter(c)) {
                    break;
                }
                c = (char)(Character.toUpperCase(c) - 'A' + 10);
            }
            if (c >= base) {
                break;
            }
            if (i > cutoff || (i == cutoff && c > cutlim)) {
                overflow = true;
            }
            else {
                i *= base;
                i += (short)c;
            }
            ++s;
        }
        if (s == save) {
            throw new NumberFormatException(toString(buf));
        }
        if (overflow) {
            throw new NumberFormatException(toString(buf));
        }
        return negative ? ((short)(-i)) : i;
    }
    
    public static int indexOfIgnoreCase(final String searchIn, final String searchFor) {
        return indexOfIgnoreCase(0, searchIn, searchFor);
    }
    
    public static int indexOfIgnoreCase(final int startingPosition, final String searchIn, final String searchFor) {
        if (searchIn == null || searchFor == null) {
            return -1;
        }
        final int searchInLength = searchIn.length();
        final int searchForLength = searchFor.length();
        final int stopSearchingAt = searchInLength - searchForLength;
        if (startingPosition > stopSearchingAt || searchForLength == 0) {
            return -1;
        }
        final char firstCharOfSearchForUc = Character.toUpperCase(searchFor.charAt(0));
        final char firstCharOfSearchForLc = Character.toLowerCase(searchFor.charAt(0));
        for (int i = startingPosition; i <= stopSearchingAt; ++i) {
            if (isCharAtPosNotEqualIgnoreCase(searchIn, i, firstCharOfSearchForUc, firstCharOfSearchForLc)) {
                while (++i <= stopSearchingAt && isCharAtPosNotEqualIgnoreCase(searchIn, i, firstCharOfSearchForUc, firstCharOfSearchForLc)) {}
            }
            if (i <= stopSearchingAt && startsWithIgnoreCase(searchIn, i, searchFor)) {
                return i;
            }
        }
        return -1;
    }
    
    public static int indexOfIgnoreCase(final int startingPosition, final String searchIn, final String[] searchForSequence, final String openingMarkers, final String closingMarkers, Set<SearchMode> searchMode) {
        if (searchIn == null || searchForSequence == null) {
            return -1;
        }
        final int searchInLength = searchIn.length();
        int searchForLength = 0;
        for (final String searchForPart : searchForSequence) {
            searchForLength += searchForPart.length();
        }
        if (searchForLength == 0) {
            return -1;
        }
        final int searchForWordsCount = searchForSequence.length;
        searchForLength += ((searchForWordsCount > 0) ? (searchForWordsCount - 1) : 0);
        final int stopSearchingAt = searchInLength - searchForLength;
        if (startingPosition > stopSearchingAt) {
            return -1;
        }
        if (searchMode.contains(SearchMode.SKIP_BETWEEN_MARKERS) && (openingMarkers == null || closingMarkers == null || openingMarkers.length() != closingMarkers.length())) {
            throw new IllegalArgumentException(Messages.getString("StringUtils.15", new String[] { openingMarkers, closingMarkers }));
        }
        if (Character.isWhitespace(searchForSequence[0].charAt(0)) && searchMode.contains(SearchMode.SKIP_WHITE_SPACE)) {
            searchMode = EnumSet.copyOf(searchMode);
            searchMode.remove(SearchMode.SKIP_WHITE_SPACE);
        }
        final Set<SearchMode> searchMode2 = EnumSet.of(SearchMode.SKIP_WHITE_SPACE);
        searchMode2.addAll(searchMode);
        searchMode2.remove(SearchMode.SKIP_BETWEEN_MARKERS);
        for (int positionOfFirstWord = startingPosition; positionOfFirstWord <= stopSearchingAt; ++positionOfFirstWord) {
            positionOfFirstWord = indexOfIgnoreCase(positionOfFirstWord, searchIn, searchForSequence[0], openingMarkers, closingMarkers, searchMode);
            if (positionOfFirstWord == -1 || positionOfFirstWord > stopSearchingAt) {
                return -1;
            }
            int startingPositionForNextWord = positionOfFirstWord + searchForSequence[0].length();
            int wc = 0;
            boolean match = true;
            while (++wc < searchForWordsCount && match) {
                final int positionOfNextWord = indexOfNextChar(startingPositionForNextWord, searchInLength - 1, searchIn, null, null, searchMode2);
                if (startingPositionForNextWord == positionOfNextWord || !startsWithIgnoreCase(searchIn, positionOfNextWord, searchForSequence[wc])) {
                    match = false;
                }
                else {
                    startingPositionForNextWord = positionOfNextWord + searchForSequence[wc].length();
                }
            }
            if (match) {
                return positionOfFirstWord;
            }
        }
        return -1;
    }
    
    public static int indexOfIgnoreCase(final int startingPosition, final String searchIn, final String searchFor, final String openingMarkers, final String closingMarkers, Set<SearchMode> searchMode) {
        if (searchIn == null || searchFor == null) {
            return -1;
        }
        final int searchInLength = searchIn.length();
        final int searchForLength = searchFor.length();
        final int stopSearchingAt = searchInLength - searchForLength;
        if (startingPosition > stopSearchingAt || searchForLength == 0) {
            return -1;
        }
        if (searchMode.contains(SearchMode.SKIP_BETWEEN_MARKERS) && (openingMarkers == null || closingMarkers == null || openingMarkers.length() != closingMarkers.length())) {
            throw new IllegalArgumentException(Messages.getString("StringUtils.15", new String[] { openingMarkers, closingMarkers }));
        }
        final char firstCharOfSearchForUc = Character.toUpperCase(searchFor.charAt(0));
        final char firstCharOfSearchForLc = Character.toLowerCase(searchFor.charAt(0));
        if (Character.isWhitespace(firstCharOfSearchForLc) && searchMode.contains(SearchMode.SKIP_WHITE_SPACE)) {
            searchMode = EnumSet.copyOf(searchMode);
            searchMode.remove(SearchMode.SKIP_WHITE_SPACE);
        }
        for (int i = startingPosition; i <= stopSearchingAt; ++i) {
            i = indexOfNextChar(i, stopSearchingAt, searchIn, openingMarkers, closingMarkers, searchMode);
            if (i == -1) {
                return -1;
            }
            final char c = searchIn.charAt(i);
            if (isCharEqualIgnoreCase(c, firstCharOfSearchForUc, firstCharOfSearchForLc) && startsWithIgnoreCase(searchIn, i, searchFor)) {
                return i;
            }
        }
        return -1;
    }
    
    private static int indexOfNextChar(final int startingPosition, final int stopPosition, final String searchIn, final String openingMarkers, final String closingMarkers, final Set<SearchMode> searchMode) {
        if (searchIn == null) {
            return -1;
        }
        final int searchInLength = searchIn.length();
        if (startingPosition >= searchInLength) {
            return -1;
        }
        char c0 = '\0';
        char c2 = searchIn.charAt(startingPosition);
        char c3 = (startingPosition + 1 < searchInLength) ? searchIn.charAt(startingPosition + 1) : '\0';
        for (int i = startingPosition; i <= stopPosition; ++i) {
            c0 = c2;
            c2 = c3;
            c3 = ((i + 2 < searchInLength) ? searchIn.charAt(i + 2) : '\0');
            boolean dashDashCommentImmediateEnd = false;
            int markerIndex = -1;
            if (searchMode.contains(SearchMode.ALLOW_BACKSLASH_ESCAPE) && c0 == '\\') {
                ++i;
                c2 = c3;
                c3 = ((i + 2 < searchInLength) ? searchIn.charAt(i + 2) : '\0');
            }
            else if (searchMode.contains(SearchMode.SKIP_BETWEEN_MARKERS) && (markerIndex = openingMarkers.indexOf(c0)) != -1) {
                int nestedMarkersCount = 0;
                final char openingMarker = c0;
                final char closingMarker = closingMarkers.charAt(markerIndex);
                while (++i <= stopPosition && ((c0 = searchIn.charAt(i)) != closingMarker || nestedMarkersCount != 0)) {
                    if (c0 == openingMarker) {
                        ++nestedMarkersCount;
                    }
                    else if (c0 == closingMarker) {
                        --nestedMarkersCount;
                    }
                    else {
                        if (!searchMode.contains(SearchMode.ALLOW_BACKSLASH_ESCAPE) || c0 != '\\') {
                            continue;
                        }
                        ++i;
                    }
                }
                c2 = ((i + 1 < searchInLength) ? searchIn.charAt(i + 1) : '\0');
                c3 = ((i + 2 < searchInLength) ? searchIn.charAt(i + 2) : '\0');
            }
            else if (searchMode.contains(SearchMode.SKIP_BLOCK_COMMENTS) && c0 == '/' && c2 == '*') {
                if (c3 != '!') {
                    ++i;
                    while (++i <= stopPosition) {
                        if (searchIn.charAt(i) == '*') {
                            if (((i + 1 < searchInLength) ? searchIn.charAt(i + 1) : '\0') != '/') {
                                continue;
                            }
                            break;
                        }
                    }
                    ++i;
                }
                else {
                    ++i;
                    ++i;
                    int j;
                    for (j = 1; j <= 5 && i + j < searchInLength && Character.isDigit(searchIn.charAt(i + j)); ++j) {}
                    if (j == 5) {
                        i += 5;
                    }
                }
                c2 = ((i + 1 < searchInLength) ? searchIn.charAt(i + 1) : '\0');
                c3 = ((i + 2 < searchInLength) ? searchIn.charAt(i + 2) : '\0');
            }
            else if (searchMode.contains(SearchMode.SKIP_BLOCK_COMMENTS) && c0 == '*' && c2 == '/') {
                ++i;
                c2 = c3;
                c3 = ((i + 2 < searchInLength) ? searchIn.charAt(i + 2) : '\0');
            }
            else if (searchMode.contains(SearchMode.SKIP_LINE_COMMENTS) && ((c0 == '-' && c2 == '-' && (Character.isWhitespace(c3) || (dashDashCommentImmediateEnd = (c3 == ';')) || c3 == '\0')) || c0 == '#')) {
                if (dashDashCommentImmediateEnd) {
                    ++i;
                    c2 = ((++i + 1 < searchInLength) ? searchIn.charAt(i + 1) : '\0');
                    c3 = ((i + 2 < searchInLength) ? searchIn.charAt(i + 2) : '\0');
                }
                else {
                    while (++i <= stopPosition && (c0 = searchIn.charAt(i)) != '\n' && c0 != '\r') {}
                    c2 = ((i + 1 < searchInLength) ? searchIn.charAt(i + 1) : '\0');
                    if (c0 == '\r' && c2 == '\n') {
                        c2 = ((++i + 1 < searchInLength) ? searchIn.charAt(i + 1) : '\0');
                    }
                    c3 = ((i + 2 < searchInLength) ? searchIn.charAt(i + 2) : '\0');
                }
            }
            else if (!searchMode.contains(SearchMode.SKIP_WHITE_SPACE) || !Character.isWhitespace(c0)) {
                return i;
            }
        }
        return -1;
    }
    
    private static boolean isCharAtPosNotEqualIgnoreCase(final String searchIn, final int pos, final char firstCharOfSearchForUc, final char firstCharOfSearchForLc) {
        return Character.toLowerCase(searchIn.charAt(pos)) != firstCharOfSearchForLc && Character.toUpperCase(searchIn.charAt(pos)) != firstCharOfSearchForUc;
    }
    
    private static boolean isCharEqualIgnoreCase(final char charToCompare, final char compareToCharUC, final char compareToCharLC) {
        return Character.toLowerCase(charToCompare) == compareToCharLC || Character.toUpperCase(charToCompare) == compareToCharUC;
    }
    
    public static List<String> split(final String stringToSplit, final String delimiter, final boolean trim) {
        if (stringToSplit == null) {
            return new ArrayList<String>();
        }
        if (delimiter == null) {
            throw new IllegalArgumentException();
        }
        final StringTokenizer tokenizer = new StringTokenizer(stringToSplit, delimiter, false);
        final List<String> splitTokens = new ArrayList<String>(tokenizer.countTokens());
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            if (trim) {
                token = token.trim();
            }
            splitTokens.add(token);
        }
        return splitTokens;
    }
    
    public static List<String> split(final String stringToSplit, final String delimiter, final String markers, final String markerCloses, final boolean trim) {
        if (stringToSplit == null) {
            return new ArrayList<String>();
        }
        if (delimiter == null) {
            throw new IllegalArgumentException();
        }
        int delimPos = 0;
        int currentPos = 0;
        final List<String> splitTokens = new ArrayList<String>();
        while ((delimPos = indexOfIgnoreCase(currentPos, stringToSplit, delimiter, markers, markerCloses, StringUtils.SEARCH_MODE__MRK_COM_WS)) != -1) {
            String token = stringToSplit.substring(currentPos, delimPos);
            if (trim) {
                token = token.trim();
            }
            splitTokens.add(token);
            currentPos = delimPos + 1;
        }
        if (currentPos < stringToSplit.length()) {
            String token = stringToSplit.substring(currentPos);
            if (trim) {
                token = token.trim();
            }
            splitTokens.add(token);
        }
        return splitTokens;
    }
    
    private static boolean startsWith(final byte[] dataFrom, final String chars) {
        final int charsLength = chars.length();
        if (dataFrom.length < charsLength) {
            return false;
        }
        for (int i = 0; i < charsLength; ++i) {
            if (dataFrom[i] != chars.charAt(i)) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean startsWithIgnoreCase(final String searchIn, final int startAt, final String searchFor) {
        return searchIn.regionMatches(true, startAt, searchFor, 0, searchFor.length());
    }
    
    public static boolean startsWithIgnoreCase(final String searchIn, final String searchFor) {
        return startsWithIgnoreCase(searchIn, 0, searchFor);
    }
    
    public static boolean startsWithIgnoreCaseAndNonAlphaNumeric(final String searchIn, final String searchFor) {
        if (searchIn == null) {
            return searchFor == null;
        }
        int beginPos = 0;
        for (int inLength = searchIn.length(); beginPos < inLength; ++beginPos) {
            final char c = searchIn.charAt(beginPos);
            if (Character.isLetterOrDigit(c)) {
                break;
            }
        }
        return startsWithIgnoreCase(searchIn, beginPos, searchFor);
    }
    
    public static boolean startsWithIgnoreCaseAndWs(final String searchIn, final String searchFor) {
        return startsWithIgnoreCaseAndWs(searchIn, searchFor, 0);
    }
    
    public static boolean startsWithIgnoreCaseAndWs(final String searchIn, final String searchFor, int beginPos) {
        if (searchIn == null) {
            return searchFor == null;
        }
        for (int inLength = searchIn.length(); beginPos < inLength && Character.isWhitespace(searchIn.charAt(beginPos)); ++beginPos) {}
        return startsWithIgnoreCase(searchIn, beginPos, searchFor);
    }
    
    public static int startsWithIgnoreCaseAndWs(final String searchIn, final String[] searchFor) {
        for (int i = 0; i < searchFor.length; ++i) {
            if (startsWithIgnoreCaseAndWs(searchIn, searchFor[i], 0)) {
                return i;
            }
        }
        return -1;
    }
    
    public static byte[] stripEnclosure(final byte[] source, final String prefix, final String suffix) {
        if (source.length >= prefix.length() + suffix.length() && startsWith(source, prefix) && endsWith(source, suffix)) {
            final int totalToStrip = prefix.length() + suffix.length();
            final int enclosedLength = source.length - totalToStrip;
            final byte[] enclosed = new byte[enclosedLength];
            final int startPos = prefix.length();
            final int numToCopy = enclosed.length;
            System.arraycopy(source, startPos, enclosed, 0, numToCopy);
            return enclosed;
        }
        return source;
    }
    
    public static String toAsciiString(final byte[] buffer) {
        return toAsciiString(buffer, 0, buffer.length);
    }
    
    public static String toAsciiString(final byte[] buffer, final int startPos, final int length) {
        final char[] charArray = new char[length];
        int readpoint = startPos;
        for (int i = 0; i < length; ++i) {
            charArray[i] = (char)buffer[readpoint];
            ++readpoint;
        }
        return new String(charArray);
    }
    
    public static int wildCompare(final String searchIn, final String searchForWildcard) {
        if (searchIn == null || searchForWildcard == null) {
            return -1;
        }
        if (searchForWildcard.equals("%")) {
            return 1;
        }
        int result = -1;
        final char wildcardMany = '%';
        final char wildcardOne = '_';
        final char wildcardEscape = '\\';
        int searchForPos = 0;
        final int searchForEnd = searchForWildcard.length();
        int searchInPos = 0;
        final int searchInEnd = searchIn.length();
    Label_0418:
        while (searchForPos != searchForEnd) {
            final char wildstrChar = searchForWildcard.charAt(searchForPos);
            while (searchForWildcard.charAt(searchForPos) != wildcardMany && wildstrChar != wildcardOne) {
                if (searchForWildcard.charAt(searchForPos) == wildcardEscape && searchForPos + 1 != searchForEnd) {
                    ++searchForPos;
                }
                if (searchInPos == searchInEnd || Character.toUpperCase(searchForWildcard.charAt(searchForPos++)) != Character.toUpperCase(searchIn.charAt(searchInPos++))) {
                    return 1;
                }
                if (searchForPos == searchForEnd) {
                    return (searchInPos != searchInEnd) ? 1 : 0;
                }
                result = 1;
            }
            Label_0223: {
                if (searchForWildcard.charAt(searchForPos) == wildcardOne) {
                    while (searchInPos != searchInEnd) {
                        ++searchInPos;
                        if (++searchForPos >= searchForEnd || searchForWildcard.charAt(searchForPos) != wildcardOne) {
                            if (searchForPos == searchForEnd) {
                                break Label_0418;
                            }
                            break Label_0223;
                        }
                    }
                    return result;
                }
            }
            if (searchForWildcard.charAt(searchForPos) == wildcardMany) {
                ++searchForPos;
                while (searchForPos != searchForEnd) {
                    if (searchForWildcard.charAt(searchForPos) != wildcardMany) {
                        if (searchForWildcard.charAt(searchForPos) != wildcardOne) {
                            break;
                        }
                        if (searchInPos == searchInEnd) {
                            return -1;
                        }
                        ++searchInPos;
                    }
                    ++searchForPos;
                }
                if (searchForPos == searchForEnd) {
                    return 0;
                }
                if (searchInPos == searchInEnd) {
                    return -1;
                }
                char cmp;
                if ((cmp = searchForWildcard.charAt(searchForPos)) == wildcardEscape && searchForPos + 1 != searchForEnd) {
                    cmp = searchForWildcard.charAt(++searchForPos);
                }
                ++searchForPos;
                while (true) {
                    if (searchInPos != searchInEnd && Character.toUpperCase(searchIn.charAt(searchInPos)) != Character.toUpperCase(cmp)) {
                        ++searchInPos;
                    }
                    else {
                        if (searchInPos++ == searchInEnd) {
                            return -1;
                        }
                        final int tmp = wildCompare(searchIn, searchForWildcard);
                        if (tmp <= 0) {
                            return tmp;
                        }
                        if (searchInPos == searchInEnd || searchForWildcard.charAt(0) == wildcardMany) {
                            return -1;
                        }
                        continue;
                    }
                }
            }
        }
        return (searchInPos != searchInEnd) ? 1 : 0;
    }
    
    static byte[] s2b(final String s, final MySQLConnection conn) throws SQLException {
        if (s == null) {
            return null;
        }
        if (conn != null && conn.getUseUnicode()) {
            try {
                final String encoding = conn.getEncoding();
                if (encoding == null) {
                    return s.getBytes();
                }
                final SingleByteCharsetConverter converter = conn.getCharsetConverter(encoding);
                if (converter != null) {
                    return converter.toBytes(s);
                }
                return s.getBytes(encoding);
            }
            catch (UnsupportedEncodingException E) {
                return s.getBytes();
            }
        }
        return s.getBytes();
    }
    
    public static int lastIndexOf(final byte[] s, final char c) {
        if (s == null) {
            return -1;
        }
        for (int i = s.length - 1; i >= 0; --i) {
            if (s[i] == c) {
                return i;
            }
        }
        return -1;
    }
    
    public static int indexOf(final byte[] s, final char c) {
        if (s == null) {
            return -1;
        }
        for (int length = s.length, i = 0; i < length; ++i) {
            if (s[i] == c) {
                return i;
            }
        }
        return -1;
    }
    
    public static boolean isNullOrEmpty(final String toTest) {
        return toTest == null || toTest.length() == 0;
    }
    
    public static String stripComments(final String src, final String stringOpens, final String stringCloses, final boolean slashStarComments, final boolean slashSlashComments, final boolean hashComments, final boolean dashDashComments) {
        if (src == null) {
            return null;
        }
        final StringBuilder strBuilder = new StringBuilder(src.length());
        final StringReader sourceReader = new StringReader(src);
        int contextMarker = 0;
        final boolean escaped = false;
        int markerTypeFound = -1;
        int ind = 0;
        int currentChar = 0;
        try {
            while ((currentChar = sourceReader.read()) != -1) {
                if (markerTypeFound != -1 && currentChar == stringCloses.charAt(markerTypeFound) && !escaped) {
                    contextMarker = 0;
                    markerTypeFound = -1;
                }
                else if ((ind = stringOpens.indexOf(currentChar)) != -1 && !escaped && contextMarker == 0) {
                    markerTypeFound = ind;
                    contextMarker = currentChar;
                }
                if (contextMarker == 0 && currentChar == 47 && (slashSlashComments || slashStarComments)) {
                    currentChar = sourceReader.read();
                    if (currentChar == 42 && slashStarComments) {
                        for (int prevChar = 0; (currentChar = sourceReader.read()) != 47 || prevChar != 42; prevChar = currentChar) {
                            if (currentChar == 13) {
                                currentChar = sourceReader.read();
                                if (currentChar == 10) {
                                    currentChar = sourceReader.read();
                                }
                            }
                            else if (currentChar == 10) {
                                currentChar = sourceReader.read();
                            }
                            if (currentChar < 0) {
                                break;
                            }
                        }
                        continue;
                    }
                    if (currentChar == 47 && slashSlashComments) {
                        while ((currentChar = sourceReader.read()) != 10 && currentChar != 13 && currentChar >= 0) {}
                    }
                }
                else if (contextMarker == 0 && currentChar == 35 && hashComments) {
                    while ((currentChar = sourceReader.read()) != 10 && currentChar != 13 && currentChar >= 0) {}
                }
                else if (contextMarker == 0 && currentChar == 45 && dashDashComments) {
                    currentChar = sourceReader.read();
                    if (currentChar == -1 || currentChar != 45) {
                        strBuilder.append('-');
                        if (currentChar != -1) {
                            strBuilder.append(currentChar);
                            continue;
                        }
                        continue;
                    }
                    else {
                        while ((currentChar = sourceReader.read()) != 10 && currentChar != 13 && currentChar >= 0) {}
                    }
                }
                if (currentChar != -1) {
                    strBuilder.append((char)currentChar);
                }
            }
        }
        catch (IOException ex) {}
        return strBuilder.toString();
    }
    
    public static String sanitizeProcOrFuncName(final String src) {
        if (src == null || src.equals("%")) {
            return null;
        }
        return src;
    }
    
    public static List<String> splitDBdotName(final String src, final String cat, final String quotId, final boolean isNoBslashEscSet) {
        if (src == null || src.equals("%")) {
            return new ArrayList<String>();
        }
        final boolean isQuoted = indexOfIgnoreCase(0, src, quotId) > -1;
        String retval = src;
        String tmpCat = cat;
        int trueDotIndex = -1;
        if (!" ".equals(quotId)) {
            if (isQuoted) {
                trueDotIndex = indexOfIgnoreCase(0, retval, quotId + "." + quotId);
            }
            else {
                trueDotIndex = indexOfIgnoreCase(0, retval, ".");
            }
        }
        else {
            trueDotIndex = retval.indexOf(".");
        }
        final List<String> retTokens = new ArrayList<String>(2);
        if (trueDotIndex != -1) {
            if (isQuoted) {
                tmpCat = toString(stripEnclosure(retval.substring(0, trueDotIndex + 1).getBytes(), quotId, quotId));
                if (startsWithIgnoreCaseAndWs(tmpCat, quotId)) {
                    tmpCat = tmpCat.substring(1, tmpCat.length() - 1);
                }
                retval = retval.substring(trueDotIndex + 2);
                retval = toString(stripEnclosure(retval.getBytes(), quotId, quotId));
            }
            else {
                tmpCat = retval.substring(0, trueDotIndex);
                retval = retval.substring(trueDotIndex + 1);
            }
        }
        else {
            retval = toString(stripEnclosure(retval.getBytes(), quotId, quotId));
        }
        retTokens.add(tmpCat);
        retTokens.add(retval);
        return retTokens;
    }
    
    public static boolean isEmptyOrWhitespaceOnly(final String str) {
        if (str == null || str.length() == 0) {
            return true;
        }
        for (int length = str.length(), i = 0; i < length; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    public static String escapeQuote(String src, final String quotChar) {
        if (src == null) {
            return null;
        }
        src = toString(stripEnclosure(src.getBytes(), quotChar, quotChar));
        int lastNdx = src.indexOf(quotChar);
        String tmpSrc = src.substring(0, lastNdx);
        tmpSrc = tmpSrc + quotChar + quotChar;
        String tmpRest;
        for (tmpRest = src.substring(lastNdx + 1, src.length()), lastNdx = tmpRest.indexOf(quotChar); lastNdx > -1; lastNdx = tmpRest.indexOf(quotChar)) {
            tmpSrc += tmpRest.substring(0, lastNdx);
            tmpSrc = tmpSrc + quotChar + quotChar;
            tmpRest = tmpRest.substring(lastNdx + 1, tmpRest.length());
        }
        tmpSrc = (src = tmpSrc + tmpRest);
        return src;
    }
    
    public static String quoteIdentifier(String identifier, final String quoteChar, final boolean isPedantic) {
        if (identifier == null) {
            return null;
        }
        identifier = identifier.trim();
        final int quoteCharLength = quoteChar.length();
        if (quoteCharLength == 0 || " ".equals(quoteChar)) {
            return identifier;
        }
        if (!isPedantic && identifier.startsWith(quoteChar) && identifier.endsWith(quoteChar)) {
            String identifierQuoteTrimmed;
            int quoteCharPos;
            int quoteCharNextExpectedPos;
            int quoteCharNextPosition;
            for (identifierQuoteTrimmed = identifier.substring(quoteCharLength, identifier.length() - quoteCharLength), quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar); quoteCharPos >= 0; quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextPosition + quoteCharLength)) {
                quoteCharNextExpectedPos = quoteCharPos + quoteCharLength;
                quoteCharNextPosition = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextExpectedPos);
                if (quoteCharNextPosition != quoteCharNextExpectedPos) {
                    break;
                }
            }
            if (quoteCharPos < 0) {
                return identifier;
            }
        }
        return quoteChar + identifier.replaceAll(quoteChar, quoteChar + quoteChar) + quoteChar;
    }
    
    public static String quoteIdentifier(final String identifier, final boolean isPedantic) {
        return quoteIdentifier(identifier, "`", isPedantic);
    }
    
    public static String unQuoteIdentifier(String identifier, final String quoteChar) {
        if (identifier == null) {
            return null;
        }
        identifier = identifier.trim();
        final int quoteCharLength = quoteChar.length();
        if (quoteCharLength == 0 || " ".equals(quoteChar)) {
            return identifier;
        }
        if (identifier.startsWith(quoteChar) && identifier.endsWith(quoteChar)) {
            final String identifierQuoteTrimmed = identifier.substring(quoteCharLength, identifier.length() - quoteCharLength);
            int quoteCharNextPosition;
            for (int quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar); quoteCharPos >= 0; quoteCharPos = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextPosition + quoteCharLength)) {
                final int quoteCharNextExpectedPos = quoteCharPos + quoteCharLength;
                quoteCharNextPosition = identifierQuoteTrimmed.indexOf(quoteChar, quoteCharNextExpectedPos);
                if (quoteCharNextPosition != quoteCharNextExpectedPos) {
                    return identifier;
                }
            }
            return identifier.substring(quoteCharLength, identifier.length() - quoteCharLength).replaceAll(quoteChar + quoteChar, quoteChar);
        }
        return identifier;
    }
    
    public static int indexOfQuoteDoubleAware(final String searchIn, final String quoteChar, final int startFrom) {
        if (searchIn == null || quoteChar == null || quoteChar.length() == 0 || startFrom > searchIn.length()) {
            return -1;
        }
        final int lastIndex = searchIn.length() - 1;
        int beginPos = startFrom;
        int pos = -1;
        boolean next = true;
        while (next) {
            pos = searchIn.indexOf(quoteChar, beginPos);
            if (pos == -1 || pos == lastIndex || !searchIn.startsWith(quoteChar, pos + 1)) {
                next = false;
            }
            else {
                beginPos = pos + 2;
            }
        }
        return pos;
    }
    
    public static String toString(final byte[] value, final int offset, final int length, final String encoding) throws UnsupportedEncodingException {
        final Charset cs = findCharset(encoding);
        return cs.decode(ByteBuffer.wrap(value, offset, length)).toString();
    }
    
    public static String toString(final byte[] value, final String encoding) throws UnsupportedEncodingException {
        final Charset cs = findCharset(encoding);
        return cs.decode(ByteBuffer.wrap(value)).toString();
    }
    
    public static String toString(final byte[] value, final int offset, final int length) {
        try {
            final Charset cs = findCharset(StringUtils.platformEncoding);
            return cs.decode(ByteBuffer.wrap(value, offset, length)).toString();
        }
        catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
    
    public static String toString(final byte[] value) {
        try {
            final Charset cs = findCharset(StringUtils.platformEncoding);
            return cs.decode(ByteBuffer.wrap(value)).toString();
        }
        catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
    
    public static byte[] getBytes(final char[] value) {
        try {
            return getBytes(value, 0, value.length, StringUtils.platformEncoding);
        }
        catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
    
    public static byte[] getBytes(final char[] value, final int offset, final int length) {
        try {
            return getBytes(value, offset, length, StringUtils.platformEncoding);
        }
        catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
    
    public static byte[] getBytes(final char[] value, final String encoding) throws UnsupportedEncodingException {
        return getBytes(value, 0, value.length, encoding);
    }
    
    public static byte[] getBytes(final char[] value, final int offset, final int length, final String encoding) throws UnsupportedEncodingException {
        final Charset cs = findCharset(encoding);
        final ByteBuffer buf = cs.encode(CharBuffer.wrap(value, offset, length));
        final int encodedLen = buf.limit();
        final byte[] asBytes = new byte[encodedLen];
        buf.get(asBytes, 0, encodedLen);
        return asBytes;
    }
    
    public static byte[] getBytes(final String value) {
        try {
            return getBytes(value, 0, value.length(), StringUtils.platformEncoding);
        }
        catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
    
    public static byte[] getBytes(final String value, final int offset, final int length) {
        try {
            return getBytes(value, offset, length, StringUtils.platformEncoding);
        }
        catch (UnsupportedEncodingException ex) {
            return null;
        }
    }
    
    public static byte[] getBytes(final String value, final String encoding) throws UnsupportedEncodingException {
        return getBytes(value, 0, value.length(), encoding);
    }
    
    public static byte[] getBytes(final String value, final int offset, final int length, final String encoding) throws UnsupportedEncodingException {
        if (Util.isJdbc4()) {
            final Charset cs = findCharset(encoding);
            final ByteBuffer buf = cs.encode(CharBuffer.wrap(value.toCharArray(), offset, length));
            final int encodedLen = buf.limit();
            final byte[] asBytes = new byte[encodedLen];
            buf.get(asBytes, 0, encodedLen);
            return asBytes;
        }
        if (offset != 0 || length != value.length()) {
            return value.substring(offset, offset + length).getBytes(encoding);
        }
        return value.getBytes(encoding);
    }
    
    public static final boolean isValidIdChar(final char c) {
        return "abcdefghijklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789$_#@".indexOf(c) != -1;
    }
    
    public static void appendAsHex(final StringBuilder builder, final byte[] bytes) {
        builder.append("0x");
        for (final byte b : bytes) {
            builder.append(StringUtils.HEX_DIGITS[b >>> 4 & 0xF]).append(StringUtils.HEX_DIGITS[b & 0xF]);
        }
    }
    
    public static void appendAsHex(final StringBuilder builder, final int value) {
        if (value == 0) {
            builder.append("0x0");
            return;
        }
        int shift = 32;
        boolean nonZeroFound = false;
        builder.append("0x");
        do {
            shift -= 4;
            final byte nibble = (byte)(value >>> shift & 0xF);
            if (nonZeroFound) {
                builder.append(StringUtils.HEX_DIGITS[nibble]);
            }
            else {
                if (nibble == 0) {
                    continue;
                }
                builder.append(StringUtils.HEX_DIGITS[nibble]);
                nonZeroFound = true;
            }
        } while (shift != 0);
    }
    
    public static byte[] getBytesNullTerminated(final String value, final String encoding) throws UnsupportedEncodingException {
        final Charset cs = findCharset(encoding);
        final ByteBuffer buf = cs.encode(value);
        final int encodedLen = buf.limit();
        final byte[] asBytes = new byte[encodedLen + 1];
        buf.get(asBytes, 0, encodedLen);
        asBytes[encodedLen] = 0;
        return asBytes;
    }
    
    public static boolean isStrictlyNumeric(final CharSequence cs) {
        if (cs == null || cs.length() == 0) {
            return false;
        }
        for (int i = 0; i < cs.length(); ++i) {
            if (!Character.isDigit(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isBlank(final String str) {
        final int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; ++i) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    public static boolean isNotBlank(final String str) {
        return !isBlank(str);
    }
    
    public static boolean isEmpty(final String str) {
        return str == null || str.length() == 0;
    }
    
    public static boolean isNotEmpty(final String str) {
        return isEmpty(str);
    }
    
    static {
        SEARCH_MODE__ALL = Collections.unmodifiableSet((Set<? extends SearchMode>)EnumSet.allOf(SearchMode.class));
        SEARCH_MODE__MRK_COM_WS = Collections.unmodifiableSet((Set<? extends SearchMode>)EnumSet.of(SearchMode.SKIP_BETWEEN_MARKERS, SearchMode.SKIP_BLOCK_COMMENTS, SearchMode.SKIP_LINE_COMMENTS, SearchMode.SKIP_WHITE_SPACE));
        SEARCH_MODE__BSESC_COM_WS = Collections.unmodifiableSet((Set<? extends SearchMode>)EnumSet.of(SearchMode.ALLOW_BACKSLASH_ESCAPE, SearchMode.SKIP_BLOCK_COMMENTS, SearchMode.SKIP_LINE_COMMENTS, SearchMode.SKIP_WHITE_SPACE));
        SEARCH_MODE__BSESC_MRK_WS = Collections.unmodifiableSet((Set<? extends SearchMode>)EnumSet.of(SearchMode.ALLOW_BACKSLASH_ESCAPE, SearchMode.SKIP_BETWEEN_MARKERS, SearchMode.SKIP_WHITE_SPACE));
        SEARCH_MODE__COM_WS = Collections.unmodifiableSet((Set<? extends SearchMode>)EnumSet.of(SearchMode.SKIP_BLOCK_COMMENTS, SearchMode.SKIP_LINE_COMMENTS, SearchMode.SKIP_WHITE_SPACE));
        SEARCH_MODE__MRK_WS = Collections.unmodifiableSet((Set<? extends SearchMode>)EnumSet.of(SearchMode.SKIP_BETWEEN_MARKERS, SearchMode.SKIP_WHITE_SPACE));
        SEARCH_MODE__NONE = Collections.unmodifiableSet((Set<? extends SearchMode>)EnumSet.noneOf(SearchMode.class));
        StringUtils.allBytes = new byte[256];
        StringUtils.byteToChars = new char[256];
        charsetsByAlias = new ConcurrentHashMap<String, Charset>();
        platformEncoding = System.getProperty("file.encoding");
        for (int i = -128; i <= 127; ++i) {
            StringUtils.allBytes[i + 128] = (byte)i;
        }
        final String allBytesString = new String(StringUtils.allBytes, 0, 255);
        for (int allBytesStringLen = allBytesString.length(), j = 0; j < 255 && j < allBytesStringLen; ++j) {
            StringUtils.byteToChars[j] = allBytesString.charAt(j);
        }
        try {
            StringUtils.toPlainStringMethod = BigDecimal.class.getMethod("toPlainString", (Class<?>[])new Class[0]);
        }
        catch (NoSuchMethodException ex) {}
        HEX_DIGITS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    }
    
    public enum SearchMode
    {
        ALLOW_BACKSLASH_ESCAPE, 
        SKIP_BETWEEN_MARKERS, 
        SKIP_BLOCK_COMMENTS, 
        SKIP_LINE_COMMENTS, 
        SKIP_WHITE_SPACE;
    }
}
