// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.util.dao;

import java.util.ArrayList;
import java.nio.charset.Charset;
import java.util.List;

public class ClientPrepareResult implements PrepareResult
{
    private final String sql;
    private final List<byte[]> queryParts;
    private final boolean rewriteType;
    private final int paramCount;
    private boolean isQueryMultiValuesRewritable;
    private boolean isQueryMultipleRewritable;
    
    private ClientPrepareResult(final String sql, final List<byte[]> queryParts, final boolean isQueryMultiValuesRewritable, final boolean isQueryMultipleRewritable, final boolean rewriteType) {
        this.isQueryMultiValuesRewritable = true;
        this.isQueryMultipleRewritable = true;
        this.sql = sql;
        this.queryParts = queryParts;
        this.isQueryMultiValuesRewritable = isQueryMultiValuesRewritable;
        this.isQueryMultipleRewritable = isQueryMultipleRewritable;
        this.paramCount = queryParts.size() - (rewriteType ? 3 : 1);
        this.rewriteType = rewriteType;
    }
    
    public static ClientPrepareResult parameterParts(final String queryString, final boolean noBackslashEscapes, final boolean isOracleMode, final String encoding) {
        final Charset charset = Charset.forName(encoding);
        final boolean reWritablePrepare = false;
        boolean multipleQueriesPrepare = true;
        final List<byte[]> partList = new ArrayList<byte[]>();
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
        for (int i = 0; i < queryLength; ++i) {
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
                            break;
                        }
                        break;
                    }
                    case '/': {
                        if (state == LexState.SlashStarComment && lastChar == '*') {
                            state = LexState.Normal;
                            slashend = true;
                            break;
                        }
                        if (state == LexState.Normal && slashend && lastChar == '/') {
                            slashend = false;
                            break;
                        }
                        if (state == LexState.Normal && lastChar == '/') {
                            state = LexState.EOLComment;
                            break;
                        }
                        break;
                    }
                    case '#': {
                        if (!isOracleMode && state == LexState.Normal) {
                            state = LexState.EOLComment;
                            break;
                        }
                        break;
                    }
                    case '-': {
                        if (state == LexState.Normal && lastChar == '-') {
                            state = LexState.EOLComment;
                            multipleQueriesPrepare = false;
                            break;
                        }
                        break;
                    }
                    case '\n': {
                        if (state == LexState.EOLComment) {
                            multipleQueriesPrepare = true;
                            state = LexState.Normal;
                        }
                        if (isOracleMode && state == LexState.NabmeBinding) {
                            endNameBinding = true;
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
                        if (isOracleMode && state == LexState.NabmeBinding) {
                            endNameBinding = true;
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
                            partList.add(queryString.substring(lastParameterPosition, i).getBytes(charset));
                            lastParameterPosition = i + 1;
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
                            state = LexState.NabmeBinding;
                        }
                        if (state == LexState.Normal && endingSemicolon && (byte)car >= 40) {
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
                        if (state == LexState.NabmeBinding) {
                            state = LexState.Normal;
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
                        if (isOracleMode && state == LexState.NabmeBinding) {
                            endNameBinding = true;
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
                if (isOracleMode && state == LexState.NabmeBinding && car >= '(' && !endNameBinding) {
                    paramSb.append(car);
                }
                if (isOracleMode && state == LexState.NabmeBinding && (endNameBinding || i == queryLength - 1)) {
                    partList.add(queryString.substring(lastParameterPosition, i - paramSb.length()).getBytes(charset));
                    paramSb.setLength(0);
                    state = LexState.Normal;
                    if (endNameBinding) {
                        lastParameterPosition = i;
                    }
                    else {
                        lastParameterPosition = i + 1;
                    }
                    endNameBinding = false;
                }
                lastChar = car;
            }
        }
        if (lastParameterPosition == 0) {
            partList.add(queryString.getBytes(charset));
        }
        else {
            partList.add(queryString.substring(lastParameterPosition, queryLength).getBytes(charset));
        }
        return new ClientPrepareResult(queryString, partList, reWritablePrepare, multipleQueriesPrepare, false);
    }
    
    public static boolean canAggregateSemiColon(final String queryString, final boolean noBackslashEscapes, final boolean isOracleMode) {
        LexState state = LexState.Normal;
        char lastChar = '\0';
        boolean singleQuotes = false;
        boolean endingSemicolon = false;
        final char[] charArray;
        final char[] query = charArray = queryString.toCharArray();
        for (final char car : charArray) {
            if (state == LexState.Escape && (car != '\'' || !singleQuotes) && (car != '\"' || singleQuotes)) {
                state = LexState.String;
                lastChar = car;
            }
            else {
                switch (car) {
                    case '*': {
                        if (state == LexState.Normal && lastChar == '/') {
                            state = LexState.SlashStarComment;
                            break;
                        }
                        break;
                    }
                    case '/': {
                        if (state == LexState.SlashStarComment && lastChar == '*') {
                            state = LexState.Normal;
                            break;
                        }
                        break;
                    }
                    case '#': {
                        if (!isOracleMode && state == LexState.Normal) {
                            state = LexState.EOLComment;
                            break;
                        }
                        break;
                    }
                    case '-': {
                        if (state == LexState.Normal && lastChar == '-') {
                            state = LexState.EOLComment;
                            break;
                        }
                        break;
                    }
                    case ';': {
                        if (state == LexState.Normal) {
                            endingSemicolon = true;
                            break;
                        }
                        break;
                    }
                    case '\n': {
                        if (state == LexState.EOLComment) {
                            state = LexState.Normal;
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
                    default: {
                        if (state == LexState.Normal && endingSemicolon && (byte)car >= 40) {
                            endingSemicolon = false;
                            break;
                        }
                        break;
                    }
                }
                lastChar = car;
            }
        }
        return state != LexState.EOLComment && !endingSemicolon;
    }
    
    public static ClientPrepareResult rewritableParts(final String queryString, final boolean noBackslashEscapes, final boolean isOracleMode, final String encoding) {
        final Charset charset = Charset.forName(encoding);
        boolean reWritablePrepare = true;
        boolean multipleQueriesPrepare = true;
        final List<byte[]> partList = new ArrayList<byte[]>();
        LexState state = LexState.Normal;
        char lastChar = '\0';
        final StringBuilder sb = new StringBuilder();
        String preValuePart1 = null;
        String preValuePart2 = null;
        String postValuePart = null;
        boolean singleQuotes = false;
        int isInParenthesis = 0;
        boolean skipChar = false;
        boolean isFirstChar = true;
        boolean isInsert = false;
        boolean semicolon = false;
        boolean hasParam = false;
        boolean endNameBinding = false;
        final char[] query = queryString.toCharArray();
        for (int queryLength = query.length, i = 0; i < queryLength; ++i) {
            final char car = query[i];
            if (state == LexState.Escape && (car != '\'' || !singleQuotes) && (car != '\"' || singleQuotes)) {
                sb.append(car);
                lastChar = car;
                state = LexState.String;
            }
            else {
                switch (car) {
                    case '*': {
                        if (state == LexState.Normal && lastChar == '/') {
                            state = LexState.SlashStarComment;
                            break;
                        }
                        break;
                    }
                    case '/': {
                        if (state == LexState.SlashStarComment && lastChar == '*') {
                            state = LexState.Normal;
                            break;
                        }
                        break;
                    }
                    case '#': {
                        if (!isOracleMode && state == LexState.Normal) {
                            state = LexState.EOLComment;
                            break;
                        }
                        break;
                    }
                    case '-': {
                        if (state == LexState.Normal && lastChar == '-') {
                            state = LexState.EOLComment;
                            multipleQueriesPrepare = false;
                            break;
                        }
                        break;
                    }
                    case '\n': {
                        if (state == LexState.EOLComment) {
                            state = LexState.Normal;
                        }
                        if (isOracleMode && state == LexState.NabmeBinding) {
                            endNameBinding = true;
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
                    case ';': {
                        if (state == LexState.Normal) {
                            semicolon = true;
                            multipleQueriesPrepare = false;
                        }
                        if (isOracleMode && state == LexState.NabmeBinding) {
                            endNameBinding = true;
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
                    case '?': {
                        if (state == LexState.Normal) {
                            hasParam = true;
                            if (preValuePart1 == null) {
                                preValuePart1 = sb.toString();
                                sb.setLength(0);
                            }
                            if (preValuePart2 == null) {
                                preValuePart2 = sb.toString();
                                sb.setLength(0);
                            }
                            else {
                                if (postValuePart != null) {
                                    reWritablePrepare = false;
                                    sb.insert(0, postValuePart);
                                    postValuePart = null;
                                }
                                partList.add(sb.toString().getBytes(charset));
                                sb.setLength(0);
                            }
                            skipChar = true;
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
                    case 'S':
                    case 's': {
                        if (state != LexState.Normal || postValuePart != null || queryLength <= i + 7 || (query[i + 1] != 'e' && query[i + 1] != 'E') || (query[i + 2] != 'l' && query[i + 2] != 'L') || (query[i + 3] != 'e' && query[i + 3] != 'E') || (query[i + 4] != 'c' && query[i + 4] != 'C') || (query[i + 5] != 't' && query[i + 5] != 'T')) {
                            break;
                        }
                        if (i > 0 && query[i - 1] > ' ' && "();><=-+,".indexOf(query[i - 1]) == -1) {
                            break;
                        }
                        if (query[i + 6] > ' ' && "();><=-+,".indexOf(query[i + 6]) == -1) {
                            break;
                        }
                        reWritablePrepare = false;
                        break;
                    }
                    case 'V':
                    case 'v': {
                        if (state == LexState.Normal && preValuePart1 == null && (lastChar == ')' || (byte)lastChar <= 40) && queryLength > i + 7 && (query[i + 1] == 'a' || query[i + 1] == 'A') && (query[i + 2] == 'l' || query[i + 2] == 'L') && (query[i + 3] == 'u' || query[i + 3] == 'U') && (query[i + 4] == 'e' || query[i + 4] == 'E') && (query[i + 5] == 's' || query[i + 5] == 'S') && (query[i + 6] == '(' || (byte)query[i + 6] <= 40)) {
                            sb.append(car);
                            sb.append(query[i + 1]);
                            sb.append(query[i + 2]);
                            sb.append(query[i + 3]);
                            sb.append(query[i + 4]);
                            sb.append(query[i + 5]);
                            i += 5;
                            preValuePart1 = sb.toString();
                            sb.setLength(0);
                            skipChar = true;
                            break;
                        }
                        break;
                    }
                    case 'L':
                    case 'l': {
                        if (state == LexState.Normal && queryLength > i + 14 && (query[i + 1] == 'a' || query[i + 1] == 'A') && (query[i + 2] == 's' || query[i + 2] == 'S') && (query[i + 3] == 't' || query[i + 3] == 'T') && query[i + 4] == '_' && (query[i + 5] == 'i' || query[i + 5] == 'I') && (query[i + 6] == 'n' || query[i + 6] == 'N') && (query[i + 7] == 's' || query[i + 7] == 'S') && (query[i + 8] == 'e' || query[i + 8] == 'E') && (query[i + 9] == 'r' || query[i + 9] == 'R') && (query[i + 10] == 't' || query[i + 10] == 'T') && query[i + 11] == '_' && (query[i + 12] == 'i' || query[i + 12] == 'I') && (query[i + 13] == 'd' || query[i + 13] == 'D') && query[i + 14] == '(') {
                            sb.append(car);
                            reWritablePrepare = false;
                            skipChar = true;
                            break;
                        }
                        break;
                    }
                    case '(': {
                        if (state == LexState.Normal) {
                            ++isInParenthesis;
                            break;
                        }
                        break;
                    }
                    case ')': {
                        if (state == LexState.Normal && --isInParenthesis == 0 && preValuePart2 != null && postValuePart == null) {
                            sb.append(car);
                            postValuePart = sb.toString();
                            sb.setLength(0);
                            skipChar = true;
                            break;
                        }
                        break;
                    }
                    default: {
                        if (state == LexState.Normal && isFirstChar && (byte)car >= 40) {
                            if (car == 'I' || car == 'i') {
                                isInsert = true;
                            }
                            isFirstChar = false;
                        }
                        if (state == LexState.Normal && semicolon && (byte)car >= 40) {
                            reWritablePrepare = false;
                            multipleQueriesPrepare = true;
                            break;
                        }
                        break;
                    }
                }
                lastChar = car;
                if (skipChar) {
                    skipChar = false;
                }
                else {
                    sb.append(car);
                }
            }
        }
        if (!hasParam) {
            if (preValuePart1 == null) {
                partList.add(0, sb.toString().getBytes(charset));
                partList.add(1, new byte[0]);
            }
            else {
                partList.add(0, preValuePart1.getBytes(charset));
                partList.add(1, sb.toString().getBytes(charset));
            }
            sb.setLength(0);
        }
        else {
            partList.add(0, (preValuePart1 == null) ? new byte[0] : preValuePart1.getBytes(charset));
            partList.add(1, (preValuePart2 == null) ? new byte[0] : preValuePart2.getBytes(charset));
        }
        if (!isInsert) {
            reWritablePrepare = false;
        }
        if (hasParam) {
            partList.add((postValuePart == null) ? new byte[0] : postValuePart.getBytes(charset));
        }
        partList.add(sb.toString().getBytes(charset));
        return new ClientPrepareResult(queryString, partList, reWritablePrepare, multipleQueriesPrepare, true);
    }
    
    public static List<String> rewritablePartsInsertSql(final String queryString, final boolean noBackslashEscapes, final boolean isOracleMode, final String encoding) {
        final Charset charset = Charset.forName(encoding);
        boolean reWritablePrepare = true;
        boolean multipleQueriesPrepare = true;
        final List<String> stringPartList = new ArrayList<String>();
        LexState state = LexState.Normal;
        char lastChar = '\0';
        final StringBuilder sb = new StringBuilder();
        String preValuePart1 = null;
        String preValuePart2 = null;
        String postValuePart = null;
        boolean singleQuotes = false;
        int isInParenthesis = 0;
        boolean skipChar = false;
        boolean isFirstChar = true;
        boolean isInsert = false;
        boolean semicolon = false;
        boolean hasParam = false;
        boolean endNameBinding = false;
        final char[] query = queryString.toCharArray();
        for (int queryLength = query.length, i = 0; i < queryLength; ++i) {
            final char car = query[i];
            if (state == LexState.Escape && (car != '\'' || !singleQuotes) && (car != '\"' || singleQuotes)) {
                sb.append(car);
                lastChar = car;
                state = LexState.String;
            }
            else {
                switch (car) {
                    case '*': {
                        if (state == LexState.Normal && lastChar == '/') {
                            state = LexState.SlashStarComment;
                            break;
                        }
                        break;
                    }
                    case '/': {
                        if (state == LexState.SlashStarComment && lastChar == '*') {
                            state = LexState.Normal;
                            break;
                        }
                        break;
                    }
                    case '#': {
                        if (!isOracleMode && state == LexState.Normal) {
                            state = LexState.EOLComment;
                            break;
                        }
                        break;
                    }
                    case '-': {
                        if (state == LexState.Normal && lastChar == '-') {
                            state = LexState.EOLComment;
                            multipleQueriesPrepare = false;
                            break;
                        }
                        break;
                    }
                    case '\n': {
                        if (state == LexState.EOLComment) {
                            state = LexState.Normal;
                        }
                        if (isOracleMode && state == LexState.NabmeBinding) {
                            endNameBinding = true;
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
                    case ';': {
                        if (state == LexState.Normal) {
                            semicolon = true;
                            multipleQueriesPrepare = false;
                        }
                        if (isOracleMode && state == LexState.NabmeBinding) {
                            endNameBinding = true;
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
                    case '?': {
                        if (state == LexState.Normal) {
                            hasParam = true;
                            if (preValuePart1 == null) {
                                preValuePart1 = sb.toString();
                                sb.setLength(0);
                            }
                            if (preValuePart2 == null) {
                                preValuePart2 = sb.toString();
                                sb.setLength(0);
                            }
                            else {
                                if (postValuePart != null) {
                                    reWritablePrepare = false;
                                    sb.insert(0, postValuePart);
                                    postValuePart = null;
                                }
                                stringPartList.add(sb.toString());
                                sb.setLength(0);
                            }
                            skipChar = true;
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
                    case 'S':
                    case 's': {
                        if (state != LexState.Normal || postValuePart != null || queryLength <= i + 7 || (query[i + 1] != 'e' && query[i + 1] != 'E') || (query[i + 2] != 'l' && query[i + 2] != 'L') || (query[i + 3] != 'e' && query[i + 3] != 'E') || (query[i + 4] != 'c' && query[i + 4] != 'C') || (query[i + 5] != 't' && query[i + 5] != 'T')) {
                            break;
                        }
                        if (i > 0 && query[i - 1] > ' ' && "();><=-+,".indexOf(query[i - 1]) == -1) {
                            break;
                        }
                        if (query[i + 6] > ' ' && "();><=-+,".indexOf(query[i + 6]) == -1) {
                            break;
                        }
                        reWritablePrepare = false;
                        break;
                    }
                    case 'V':
                    case 'v': {
                        if (state == LexState.Normal && preValuePart1 == null && (lastChar == ')' || (byte)lastChar <= 40) && queryLength > i + 7 && (query[i + 1] == 'a' || query[i + 1] == 'A') && (query[i + 2] == 'l' || query[i + 2] == 'L') && (query[i + 3] == 'u' || query[i + 3] == 'U') && (query[i + 4] == 'e' || query[i + 4] == 'E') && (query[i + 5] == 's' || query[i + 5] == 'S') && (query[i + 6] == '(' || (byte)query[i + 6] <= 40)) {
                            sb.append(car);
                            sb.append(query[i + 1]);
                            sb.append(query[i + 2]);
                            sb.append(query[i + 3]);
                            sb.append(query[i + 4]);
                            sb.append(query[i + 5]);
                            i += 5;
                            preValuePart1 = sb.toString();
                            sb.setLength(0);
                            skipChar = true;
                            break;
                        }
                        break;
                    }
                    case 'L':
                    case 'l': {
                        if (state == LexState.Normal && queryLength > i + 14 && (query[i + 1] == 'a' || query[i + 1] == 'A') && (query[i + 2] == 's' || query[i + 2] == 'S') && (query[i + 3] == 't' || query[i + 3] == 'T') && query[i + 4] == '_' && (query[i + 5] == 'i' || query[i + 5] == 'I') && (query[i + 6] == 'n' || query[i + 6] == 'N') && (query[i + 7] == 's' || query[i + 7] == 'S') && (query[i + 8] == 'e' || query[i + 8] == 'E') && (query[i + 9] == 'r' || query[i + 9] == 'R') && (query[i + 10] == 't' || query[i + 10] == 'T') && query[i + 11] == '_' && (query[i + 12] == 'i' || query[i + 12] == 'I') && (query[i + 13] == 'd' || query[i + 13] == 'D') && query[i + 14] == '(') {
                            sb.append(car);
                            reWritablePrepare = false;
                            skipChar = true;
                            break;
                        }
                        break;
                    }
                    case '(': {
                        if (state == LexState.Normal) {
                            ++isInParenthesis;
                            break;
                        }
                        break;
                    }
                    case ')': {
                        if (state == LexState.Normal && --isInParenthesis == 0 && preValuePart2 != null && postValuePart == null) {
                            sb.append(car);
                            postValuePart = sb.toString();
                            sb.setLength(0);
                            skipChar = true;
                            break;
                        }
                        break;
                    }
                    default: {
                        if (state == LexState.Normal && isFirstChar && (byte)car >= 40) {
                            if (car == 'I' || car == 'i') {
                                isInsert = true;
                            }
                            isFirstChar = false;
                        }
                        if (state == LexState.Normal && semicolon && (byte)car >= 40) {
                            reWritablePrepare = false;
                            multipleQueriesPrepare = true;
                            break;
                        }
                        break;
                    }
                }
                lastChar = car;
                if (skipChar) {
                    skipChar = false;
                }
                else {
                    sb.append(car);
                }
            }
        }
        if (!hasParam) {
            if (preValuePart1 == null) {
                stringPartList.add(0, sb.toString());
                stringPartList.add(1, "?");
            }
            else {
                stringPartList.add(0, preValuePart1);
                stringPartList.add(1, sb.toString());
            }
            sb.setLength(0);
        }
        else {
            stringPartList.add(0, (preValuePart1 == null) ? "" : preValuePart1);
            stringPartList.add(1, (preValuePart2 == null) ? "" : preValuePart2);
        }
        if (!isInsert) {
            reWritablePrepare = false;
        }
        if (hasParam) {
            stringPartList.add((postValuePart == null) ? "" : postValuePart.toString());
        }
        stringPartList.add(sb.toString());
        if (reWritablePrepare) {
            return stringPartList;
        }
        return null;
    }
    
    @Override
    public String getSql() {
        return this.sql;
    }
    
    public List<byte[]> getQueryParts() {
        return this.queryParts;
    }
    
    public boolean isQueryMultiValuesRewritable() {
        return this.isQueryMultiValuesRewritable;
    }
    
    public boolean isQueryMultipleRewritable() {
        return this.isQueryMultipleRewritable;
    }
    
    public boolean isRewriteType() {
        return this.rewriteType;
    }
    
    @Override
    public int getParamCount() {
        return this.paramCount;
    }
    
    enum LexState
    {
        Normal, 
        String, 
        SlashStarComment, 
        Escape, 
        EOLComment, 
        Backtick, 
        NabmeBinding;
    }
}
