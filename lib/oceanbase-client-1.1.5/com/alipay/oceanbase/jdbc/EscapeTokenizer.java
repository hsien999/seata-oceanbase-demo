// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

public class EscapeTokenizer
{
    private static final char CHR_ESCAPE = '\\';
    private static final char CHR_SGL_QUOTE = '\'';
    private static final char CHR_DBL_QUOTE = '\"';
    private static final char CHR_LF = '\n';
    private static final char CHR_CR = '\r';
    private static final char CHR_COMMENT = '-';
    private static final char CHR_BEGIN_TOKEN = '{';
    private static final char CHR_END_TOKEN = '}';
    private static final char CHR_VARIABLE = '@';
    private String source;
    private int sourceLength;
    private int pos;
    private boolean emittingEscapeCode;
    private boolean sawVariableUse;
    private int bracesLevel;
    private boolean inQuotes;
    private char quoteChar;
    
    public EscapeTokenizer(final String source) {
        this.source = null;
        this.sourceLength = 0;
        this.pos = 0;
        this.emittingEscapeCode = false;
        this.sawVariableUse = false;
        this.bracesLevel = 0;
        this.inQuotes = false;
        this.quoteChar = '\0';
        this.source = source;
        this.sourceLength = source.length();
        this.pos = 0;
    }
    
    public synchronized boolean hasMoreTokens() {
        return this.pos < this.sourceLength;
    }
    
    public synchronized String nextToken() {
        final StringBuilder tokenBuf = new StringBuilder();
        boolean backslashEscape = false;
        if (this.emittingEscapeCode) {
            tokenBuf.append("{");
            this.emittingEscapeCode = false;
        }
        while (this.pos < this.sourceLength) {
            char c = this.source.charAt(this.pos);
            Label_0444: {
                if (c == '\\') {
                    tokenBuf.append(c);
                    backslashEscape = !backslashEscape;
                }
                else if ((c == '\'' || c == '\"') && !backslashEscape) {
                    tokenBuf.append(c);
                    if (this.inQuotes) {
                        if (c == this.quoteChar) {
                            if (this.pos + 1 < this.sourceLength && this.source.charAt(this.pos + 1) == this.quoteChar) {
                                tokenBuf.append(c);
                                ++this.pos;
                            }
                            else {
                                this.inQuotes = false;
                            }
                        }
                    }
                    else {
                        this.inQuotes = true;
                        this.quoteChar = c;
                    }
                }
                else if (c == '\n' || c == '\r') {
                    tokenBuf.append(c);
                    backslashEscape = false;
                }
                else {
                    if (!this.inQuotes && !backslashEscape) {
                        if (c == '-') {
                            tokenBuf.append(c);
                            if (this.pos + 1 < this.sourceLength && this.source.charAt(this.pos + 1) == '-') {
                                while (++this.pos < this.sourceLength && c != '\n' && c != '\r') {
                                    c = this.source.charAt(this.pos);
                                    tokenBuf.append(c);
                                }
                                --this.pos;
                            }
                            break Label_0444;
                        }
                        else if (c == '{') {
                            ++this.bracesLevel;
                            if (this.bracesLevel == 1) {
                                this.emittingEscapeCode = true;
                                ++this.pos;
                                return tokenBuf.toString();
                            }
                            tokenBuf.append(c);
                            break Label_0444;
                        }
                        else if (c == '}') {
                            tokenBuf.append(c);
                            --this.bracesLevel;
                            if (this.bracesLevel == 0) {
                                ++this.pos;
                                return tokenBuf.toString();
                            }
                            break Label_0444;
                        }
                        else if (c == '@') {
                            this.sawVariableUse = true;
                        }
                    }
                    tokenBuf.append(c);
                    backslashEscape = false;
                }
            }
            ++this.pos;
        }
        return tokenBuf.toString();
    }
    
    boolean sawVariableUse() {
        return this.sawVariableUse;
    }
}
