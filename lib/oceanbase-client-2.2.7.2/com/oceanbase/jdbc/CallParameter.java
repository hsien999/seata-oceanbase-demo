// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

class CallParameter
{
    private boolean isInput;
    private boolean isOutput;
    private int sqlType;
    private int outputSqlType;
    private int scale;
    private String typeName;
    private boolean isSigned;
    private int canBeNull;
    private int precision;
    private String className;
    private String name;
    private int index;
    
    public CallParameter() {
        this.sqlType = 1111;
        this.outputSqlType = 1111;
    }
    
    public CallParameter(final String name, final int index, final boolean isInput, final boolean isOutput, final int sqlType, final String typeName, final int precision, final int scale, final int canBeNull, final int outputSqlType) {
        this.name = name;
        this.index = index;
        this.isInput = isInput;
        this.isOutput = isOutput;
        this.sqlType = sqlType;
        this.typeName = typeName;
        this.precision = precision;
        this.scale = scale;
        this.canBeNull = canBeNull;
        this.outputSqlType = outputSqlType;
    }
    
    public boolean isInput() {
        return this.isInput;
    }
    
    public void setInput(final boolean input) {
        this.isInput = input;
    }
    
    public boolean isOutput() {
        return this.isOutput;
    }
    
    public void setOutput(final boolean output) {
        this.isOutput = output;
    }
    
    public int getSqlType() {
        return this.sqlType;
    }
    
    public void setSqlType(final int sqlType) {
        this.sqlType = sqlType;
    }
    
    public int getOutputSqlType() {
        return this.outputSqlType;
    }
    
    public void setOutputSqlType(final int outputSqlType) {
        this.outputSqlType = outputSqlType;
    }
    
    public int getScale() {
        return this.scale;
    }
    
    public void setScale(final int scale) {
        this.scale = scale;
    }
    
    public String getTypeName() {
        return this.typeName;
    }
    
    public void setTypeName(final String typeName) {
        this.typeName = typeName;
    }
    
    public boolean isSigned() {
        return this.isSigned;
    }
    
    public void setSigned(final boolean signed) {
        this.isSigned = signed;
    }
    
    public int getCanBeNull() {
        return this.canBeNull;
    }
    
    public void setCanBeNull(final int canBeNull) {
        this.canBeNull = canBeNull;
    }
    
    public int getPrecision() {
        return this.precision;
    }
    
    public void setPrecision(final int precision) {
        this.precision = precision;
    }
    
    public String getClassName() {
        return this.className;
    }
    
    public void setClassName(final String className) {
        this.className = className;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public void setIndex(final int index) {
        this.index = index;
    }
    
    public int getIndex() {
        return this.index;
    }
}
