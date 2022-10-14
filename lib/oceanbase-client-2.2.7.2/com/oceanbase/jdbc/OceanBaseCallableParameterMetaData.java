// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLSyntaxErrorException;
import java.util.HashMap;
import java.util.Locale;
import java.sql.SQLException;
import java.util.regex.Pattern;

public class OceanBaseCallableParameterMetaData extends CallableParameterMetaData
{
    private static final Pattern PARAMETER_PATTERN;
    private static final Pattern RETURN_PATTERN;
    
    @Override
    public String getProName() {
        return this.name;
    }
    
    @Override
    public String getDatabase() {
        return this.database;
    }
    
    public OceanBaseCallableParameterMetaData(final OceanBaseConnection con, final String database, final String name, final boolean isFunction) {
        super(con, database, name, isFunction);
    }
    
    @Override
    public void readMetadataFromDbIfRequired(final String query, final String arguments, final Boolean isObFunction) throws SQLException {
        if (this.valid) {
            return;
        }
        this.query = query;
        this.queryMetaInfos(false);
        this.resetParams(arguments, isObFunction);
    }
    
    private int mapMariaDbTypeToJdbc(final String str) {
        final String upperCase = str.toUpperCase(Locale.ROOT);
        switch (upperCase) {
            case "BIT": {
                return -7;
            }
            case "TINYINT": {
                return -6;
            }
            case "SMALLINT": {
                return 5;
            }
            case "MEDIUMINT": {
                return 4;
            }
            case "INT": {
                return 4;
            }
            case "INTEGER": {
                return 4;
            }
            case "LONG": {
                return 4;
            }
            case "BIGINT": {
                return -5;
            }
            case "INT24": {
                return 4;
            }
            case "REAL": {
                return 8;
            }
            case "FLOAT": {
                return 6;
            }
            case "DECIMAL": {
                return 3;
            }
            case "NUMERIC": {
                return 2;
            }
            case "DOUBLE": {
                return 8;
            }
            case "CHAR": {
                return 1;
            }
            case "VARCHAR": {
                return 12;
            }
            case "DATE": {
                return 91;
            }
            case "TIME": {
                return 92;
            }
            case "YEAR": {
                return 5;
            }
            case "TIMESTAMP": {
                return 93;
            }
            case "DATETIME": {
                return 93;
            }
            case "TINYBLOB": {
                return -2;
            }
            case "BLOB": {
                return -4;
            }
            case "MEDIUMBLOB": {
                return -4;
            }
            case "LONGBLOB": {
                return -4;
            }
            case "TINYTEXT": {
                return 12;
            }
            case "TEXT": {
                return -1;
            }
            case "MEDIUMTEXT": {
                return -1;
            }
            case "LONGTEXT": {
                return -1;
            }
            case "ENUM": {
                return 12;
            }
            case "SET": {
                return 12;
            }
            case "GEOMETRY": {
                return -4;
            }
            case "VARBINARY": {
                return -3;
            }
            default: {
                return 1111;
            }
        }
    }
    
    private void queryMetaInfos(final boolean isFunction) throws SQLException {
        this.mapNameToParamter = new HashMap<String, CallParameter>();
        try (final Statement stmt = this.con.createStatement()) {
            final String query_sql = "SELECT DISTINCT(ARGUMENT_NAME), IN_OUT, DATA_TYPE, DATA_PRECISION, DATA_SCALE, POSITION FROM ALL_ARGUMENTS WHERE (OVERLOAD is NULL OR OVERLOAD = 1) and POSITION != 0 AND object_name = '";
            final StringBuilder paramMetaSql = new StringBuilder(query_sql);
            paramMetaSql.append(this.name);
            if (this.obOraclePackageName != null && this.obOraclePackageName.trim().length() > 0) {
                paramMetaSql.append("' and package_name = '").append(this.obOraclePackageName);
            }
            if (this.obOracleSchema != null && this.obOracleSchema.trim().length() > 0) {
                String tmp;
                if (this.obOracleSchema.startsWith("\"") && this.obOracleSchema.endsWith("\"")) {
                    tmp = this.obOracleSchema.replace("\"", "");
                }
                else {
                    tmp = this.obOracleSchema.toUpperCase();
                }
                paramMetaSql.append("' and owner =  '").append(tmp);
                paramMetaSql.append("' order by POSITION");
            }
            else {
                if (this.obOraclePackageName != null) {
                    if (!this.obOraclePackageName.equals("DBMS_LOB")) {
                        paramMetaSql.append("' and owner = SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA')");
                    }
                    else {
                        paramMetaSql.append("' and owner = 'SYS'");
                    }
                }
                else {
                    paramMetaSql.append("' and owner = SYS_CONTEXT('USERENV', 'CURRENT_SCHEMA')");
                }
                paramMetaSql.append(" order by POSITION");
            }
            final ResultSet rs = stmt.executeQuery(paramMetaSql.toString());
            this.addParametersFromDBOD(rs);
        }
        catch (SQLSyntaxErrorException sqlSyntaxErrorException) {
            throw new SQLException("Access to metaData informations not granted for current user. Consider grant select access to mysql.proc  or avoid using parameter by name", sqlSyntaxErrorException);
        }
    }
    
    private void parseFunctionReturnParam(final String functionReturn) throws SQLException {
        if (functionReturn == null || functionReturn.length() == 0) {
            throw new SQLException(this.name + "is not a function returning value");
        }
        final Matcher matcher = OceanBaseCallableParameterMetaData.RETURN_PATTERN.matcher(functionReturn);
        if (!matcher.matches()) {
            throw new SQLException("can not parse return value definition :" + functionReturn);
        }
        final CallParameter callParameter = this.params.get(0);
        callParameter.setOutput(true);
        callParameter.setSigned(matcher.group(1) == null);
        callParameter.setTypeName(matcher.group(2).trim());
        callParameter.setSqlType(this.mapMariaDbTypeToJdbc(callParameter.getTypeName()));
        String scale = matcher.group(3);
        if (scale != null) {
            scale = scale.replace("(", "").replace(")", "").replace(" ", "");
            callParameter.setScale(Integer.valueOf(scale));
        }
    }
    
    private void parseParamList(final boolean isFunction, final String paramList) throws SQLException {
        this.params = new ArrayList<CallParameter>();
        if (isFunction) {
            this.params.add(new CallParameter());
        }
        final Matcher matcher2 = OceanBaseCallableParameterMetaData.PARAMETER_PATTERN.matcher(paramList);
        while (matcher2.find()) {
            final CallParameter callParameter = new CallParameter();
            String direction = matcher2.group(1);
            if (direction != null) {
                direction = direction.trim();
            }
            callParameter.setName(matcher2.group(2).trim());
            callParameter.setSigned(matcher2.group(3) == null);
            callParameter.setTypeName(matcher2.group(4).trim().toUpperCase(Locale.ROOT));
            if (direction == null || direction.equalsIgnoreCase("IN")) {
                callParameter.setInput(true);
            }
            else if (direction.equalsIgnoreCase("OUT")) {
                callParameter.setOutput(true);
            }
            else {
                if (!direction.equalsIgnoreCase("INOUT")) {
                    throw new SQLException("unknown parameter direction " + direction + "for " + callParameter.getName());
                }
                callParameter.setInput(true);
                callParameter.setOutput(true);
            }
            callParameter.setSqlType(this.mapMariaDbTypeToJdbc(callParameter.getTypeName()));
            String scale = matcher2.group(5);
            if (scale != null) {
                scale = scale.trim().replace("(", "").replace(")", "").replace(" ", "");
                if (scale.contains(",")) {
                    scale = scale.substring(0, scale.indexOf(","));
                }
                callParameter.setScale(Integer.valueOf(scale));
            }
            this.params.add(callParameter);
        }
    }
    
    private void readMetadata() throws SQLException {
        if (this.valid) {
            return;
        }
        final String[] metaInfos = null;
        final String paramList = metaInfos[0];
        final String functionReturn = metaInfos[1];
        this.parseParamList(this.isFunction, paramList);
        if (this.isFunction) {
            this.parseFunctionReturnParam(functionReturn);
        }
    }
    
    @Override
    public int getParameterCount() {
        return this.params.size();
    }
    
    @Override
    public CallParameter getParamByName(final String name) {
        return this.mapNameToParamter.containsKey(name) ? this.mapNameToParamter.get(name) : null;
    }
    
    @Override
    public CallParameter getParam(final int index) throws SQLException {
        if (index < 1 || index > this.params.size()) {
            throw new SQLException("invalid parameter index " + index);
        }
        this.readMetadataFromDbIfRequired();
        return this.params.get(index - 1);
    }
    
    @Override
    public int isNullable(final int param) throws SQLException {
        return this.getParam(param).getCanBeNull();
    }
    
    @Override
    public boolean isSigned(final int param) throws SQLException {
        return this.getParam(param).isSigned();
    }
    
    @Override
    public int getPrecision(final int param) throws SQLException {
        return this.getParam(param).getPrecision();
    }
    
    @Override
    public int getScale(final int param) throws SQLException {
        return this.getParam(param).getScale();
    }
    
    @Override
    public int getParameterType(final int param) throws SQLException {
        return this.getParam(param).getSqlType();
    }
    
    @Override
    public String getParameterTypeName(final int param) throws SQLException {
        return this.getParam(param).getTypeName();
    }
    
    @Override
    public String getParameterClassName(final int param) throws SQLException {
        return this.getParam(param).getClassName();
    }
    
    @Override
    public int getParameterMode(final int param) throws SQLException {
        final CallParameter callParameter = this.getParam(param);
        if (callParameter.isInput() && callParameter.isOutput()) {
            return 2;
        }
        if (callParameter.isInput()) {
            return 1;
        }
        if (callParameter.isOutput()) {
            return 4;
        }
        return 0;
    }
    
    @Override
    public String getName(final int param) throws SQLException {
        return this.getParam(param).getName();
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) {
        return null;
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return false;
    }
    
    private void addParametersFromDBOD(final ResultSet paramTypesRs) throws SQLException {
        this.params = new ArrayList<CallParameter>();
        if (this.isFunction) {
            final CallParameter callParameter = new CallParameter();
            callParameter.setOutput(true);
            callParameter.setInput(false);
            callParameter.setName("functionreturn");
            this.params.add(callParameter);
        }
        int i = 0;
        while (paramTypesRs.next()) {
            final String inOut = paramTypesRs.getString("IN_OUT");
            int inOutModifier = 0;
            boolean isOutParameter = false;
            boolean isInParameter = false;
            if (this.getParameterCount() == 0 && this.isFunction) {
                isOutParameter = true;
            }
            else if (null == inOut || inOut.equalsIgnoreCase("IN")) {
                isInParameter = true;
                inOutModifier = 1;
            }
            else if (inOut.equalsIgnoreCase("INOUT")) {
                isOutParameter = true;
                isInParameter = true;
                inOutModifier = 2;
            }
            else if (inOut.equalsIgnoreCase("OUT")) {
                isOutParameter = true;
                inOutModifier = 4;
            }
            else {
                isInParameter = true;
                inOutModifier = 1;
            }
            final String paramName = paramTypesRs.getString("ARGUMENT_NAME");
            final String typeName = paramTypesRs.getString("DATA_TYPE");
            final int jdbcType = this.mapMariaDbTypeToJdbc(typeName);
            final int precision = paramTypesRs.getInt("DATA_PRECISION");
            final int scale = paramTypesRs.getInt("DATA_SCALE");
            final short nullability = 1;
            final CallParameter paramInfoToAdd = new CallParameter(paramName, i++, isInParameter, isOutParameter, jdbcType, typeName, precision, scale, nullability, inOutModifier);
            this.params.add(paramInfoToAdd);
            this.mapNameToParamter.put(paramName, paramInfoToAdd);
        }
    }
    
    static {
        PARAMETER_PATTERN = Pattern.compile("\\s*(IN\\s+|OUT\\s+|INOUT\\s+)?([\\w\\d]+)\\s+(UNSIGNED\\s+)?(\\w+)\\s*(\\([\\d,]+\\))?\\s*", 2);
        RETURN_PATTERN = Pattern.compile("\\s*(UNSIGNED\\s+)?(\\w+)\\s*(\\([\\d,]+\\))?\\s*(CHARSET\\s+)?(\\w+)?\\s*", 2);
    }
}
