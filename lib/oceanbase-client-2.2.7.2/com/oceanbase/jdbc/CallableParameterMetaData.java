// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.util.regex.Matcher;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLSyntaxErrorException;
import com.oceanbase.jdbc.internal.util.Utils;
import com.oceanbase.jdbc.internal.util.ParsedCallParameters;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import java.util.ArrayList;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Map;
import java.util.List;
import java.util.regex.Pattern;
import java.sql.ParameterMetaData;

public class CallableParameterMetaData implements ParameterMetaData
{
    private static final Pattern PARAMETER_PATTERN;
    private static final Pattern RETURN_PATTERN;
    protected final OceanBaseConnection con;
    protected final String name;
    protected List<CallParameter> params;
    protected Map<String, CallParameter> mapNameToParamter;
    protected String obOraclePackageName;
    protected String obOracleSchema;
    protected String database;
    protected boolean valid;
    protected boolean isFunction;
    protected String query;
    
    public String getProName() {
        return this.name;
    }
    
    public String getDatabase() {
        return this.database;
    }
    
    public CallableParameterMetaData(final OceanBaseConnection con, final String database, final String name, final boolean isFunction) {
        this.params = null;
        this.con = con;
        if (database != null) {
            final String tmp = database.replace("`", "");
            if (this.con.getProtocol().isOracleMode()) {
                if (tmp.equals(this.con.getProtocol().getDatabase())) {
                    this.database = tmp;
                    this.obOracleSchema = tmp;
                }
                else if (tmp.contains(".")) {
                    final String[] databaseAndPackage = tmp.split("\\.");
                    if (databaseAndPackage.length == 2) {
                        if (databaseAndPackage[1].startsWith("\"") && databaseAndPackage[1].endsWith("\"")) {
                            this.obOraclePackageName = databaseAndPackage[1].replace("\"", "");
                        }
                        else {
                            this.obOraclePackageName = databaseAndPackage[1].toUpperCase(Locale.ROOT);
                        }
                        if (databaseAndPackage[0].startsWith("\"") && databaseAndPackage[0].endsWith("\"")) {
                            this.obOracleSchema = databaseAndPackage[0].replace("\"", "");
                        }
                        else {
                            this.obOracleSchema = databaseAndPackage[0].toUpperCase(Locale.ROOT);
                        }
                        this.database = databaseAndPackage[0];
                    }
                }
                else {
                    if (tmp.startsWith("\"") && tmp.startsWith("\"")) {
                        if (tmp.replace("\"", "").equals(this.con.getProtocol().getDatabase())) {
                            this.obOracleSchema = tmp.replace("\"", "");
                            this.obOraclePackageName = null;
                        }
                        else {
                            this.obOracleSchema = null;
                            this.obOraclePackageName = tmp.replace("\"", "");
                        }
                    }
                    else if (tmp.equals(this.con.getProtocol().getDatabase()) || tmp.toUpperCase(Locale.ROOT).equals(this.con.getProtocol().getDatabase())) {
                        this.obOraclePackageName = null;
                        this.obOracleSchema = tmp.toUpperCase(Locale.ROOT);
                    }
                    else {
                        this.obOraclePackageName = tmp.toUpperCase(Locale.ROOT);
                        this.obOracleSchema = null;
                    }
                    this.database = this.con.getProtocol().getDatabase();
                }
            }
            else {
                this.database = database;
            }
        }
        else {
            this.database = null;
        }
        if (name.startsWith("\"") && name.endsWith("\"")) {
            this.name = name.replace("`", "").replace("\"", "");
        }
        else {
            this.name = name.replace("`", "").toUpperCase(Locale.ROOT);
        }
        this.isFunction = isFunction;
    }
    
    public void readMetadataFromDbIfRequired() throws SQLException {
        if (this.valid) {
            return;
        }
        this.readMetadata();
        this.valid = true;
    }
    
    public void generateMetadataFromPrepareResultSet(final ServerPrepareResult serverPrepareResult) throws SQLException {
        if (this.valid) {
            return;
        }
        this.params = new ArrayList<CallParameter>();
        final ColumnDefinition[] parameters = serverPrepareResult.getParameters();
        for (int i = 0; i < parameters.length; ++i) {
            final CallParameter callParameter = new CallParameter();
            callParameter.setName("");
            callParameter.setOutput(false);
            callParameter.setInput(false);
            this.params.add(callParameter);
        }
        this.valid = false;
    }
    
    public void readMetadataFromDbIfRequired(final String query, final String arguments, final Boolean isObFunction) throws SQLException {
        if (this.valid) {
            return;
        }
        this.query = query;
        this.readMetadata();
        this.resetParams(arguments, isObFunction);
    }
    
    void resetParams(String arguments, final boolean isObFunction) {
        int parameterCount = this.params.size();
        final List<CallParameter> currentParams = new ArrayList<CallParameter>(parameterCount);
        final int[] placeholderToParameterIndexMap = new int[parameterCount];
        for (int i = 0; i < parameterCount; ++i) {
            placeholderToParameterIndexMap[i] = -1;
        }
        List<ParsedCallParameters> paramList = new ArrayList<ParsedCallParameters>();
        if (arguments != null) {
            arguments = Utils.trimSQLString(arguments, false, true, false);
            paramList = Utils.argumentsSplit(arguments, ",", "'\"", "'\"");
        }
        int placeholderCount = 0;
        if (isObFunction) {
            paramList.add(0, new ParsedCallParameters(true, "?"));
        }
        for (int j = 0; j < paramList.size(); ++j) {
            if (paramList.get(j).isParam()) {
                placeholderToParameterIndexMap[placeholderCount++] = j;
            }
        }
        parameterCount = placeholderCount;
        for (int index = 0; index < this.params.size(); ++index) {
            if (placeholderToParameterIndexMap != null) {
                final int localIndex = placeholderToParameterIndexMap[index];
                if (localIndex != -1) {
                    final CallParameter parameter = this.params.get(localIndex);
                    currentParams.add(parameter);
                }
            }
            else {
                final CallParameter parameter2 = this.params.get(index + 1);
                if (parameter2.getIndex() != -1) {
                    currentParams.add(parameter2);
                }
            }
        }
        this.params = currentParams;
        this.valid = true;
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
    
    private String[] queryMetaInfos(final boolean isFunction) throws SQLException {
        try (final PreparedStatement preparedStatement = this.con.prepareStatement("select param_list, returns, db, type from mysql.proc where name=? and db=" + ((this.database != null) ? "?" : "DATABASE()"))) {
            preparedStatement.setString(1, this.name);
            if (this.database != null) {
                preparedStatement.setString(2, this.database);
            }
            try (final ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException((isFunction ? "function" : "procedure") + " `" + this.name + "` does not exist");
                }
                final String paramList = rs.getString(1);
                final String functionReturn = rs.getString(2);
                this.database = rs.getString(3);
                this.isFunction = "FUNCTION".equals(rs.getString(4));
                return new String[] { paramList, functionReturn };
            }
        }
        catch (SQLSyntaxErrorException sqlSyntaxErrorException) {
            throw new SQLException("Access to metaData informations not granted for current user. Consider grant select access to mysql.proc  or avoid using parameter by name", sqlSyntaxErrorException);
        }
    }
    
    private CallParameter parseFunctionReturnParam(final String functionReturn) throws SQLException {
        if (functionReturn == null || functionReturn.length() == 0) {
            throw new SQLException(this.name + "is not a function returning value");
        }
        final Matcher matcher = CallableParameterMetaData.RETURN_PATTERN.matcher(functionReturn);
        if (!matcher.matches()) {
            throw new SQLException("can not parse return value definition :" + functionReturn);
        }
        final CallParameter callParameter = new CallParameter();
        callParameter.setOutput(true);
        callParameter.setSigned(matcher.group(1) == null);
        callParameter.setTypeName(matcher.group(2).trim());
        callParameter.setSqlType(this.mapMariaDbTypeToJdbc(callParameter.getTypeName()));
        String scale = matcher.group(3);
        if (scale != null) {
            scale = scale.replace("(", "").replace(")", "").replace(" ", "");
            callParameter.setScale(Integer.valueOf(scale));
        }
        return callParameter;
    }
    
    private void parseParamList(final boolean isFunction, String paramList) throws SQLException {
        this.params = new ArrayList<CallParameter>();
        int index = 1;
        final int i = 0;
        if (isFunction) {
            final int returnIndex = paramList.indexOf("RETURNS");
            if (returnIndex != -1) {
                final int bodyStartInedx = paramList.toUpperCase(Locale.ROOT).indexOf("BEGIN");
                final String returnString = paramList.substring(returnIndex + "RETURNS".length(), bodyStartInedx);
                paramList = paramList.substring(0, returnIndex - 1);
                final CallParameter parameterRetrurn = this.parseFunctionReturnParam(returnString);
                parameterRetrurn.setIndex(index++);
                this.params.add(parameterRetrurn);
            }
        }
        final Matcher matcher2 = CallableParameterMetaData.PARAMETER_PATTERN.matcher(paramList);
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
            callParameter.setIndex(index++);
        }
    }
    
    private void readMetadata() throws SQLException {
        if (this.valid) {
            return;
        }
        String paramList = null;
        final String functionReturn = null;
        final PreparedStatement pstmt = null;
        ResultSet resultSet = null;
        if (this.isFunction) {
            if (this.database != null) {
                resultSet = this.con.createStatement().executeQuery("SHOW CREATE FUNCTION " + this.database + "." + this.name);
            }
            else {
                resultSet = this.con.createStatement().executeQuery("SHOW CREATE FUNCTION " + this.name);
            }
            resultSet.next();
            final String procedureDDl = resultSet.getString("Create Function");
            paramList = procedureDDl.substring(procedureDDl.indexOf("(") - 1);
        }
        else {
            if (this.database != null) {
                resultSet = this.con.createStatement().executeQuery("SHOW CREATE PROCEDURE " + this.database + "." + this.name);
            }
            else {
                resultSet = this.con.createStatement().executeQuery("SHOW CREATE PROCEDURE " + this.name);
            }
            resultSet.next();
            final String procedureDDl = resultSet.getString("Create Procedure");
            paramList = procedureDDl.substring(procedureDDl.indexOf("(") - 1);
        }
        this.parseParamList(this.isFunction, paramList);
    }
    
    @Override
    public int getParameterCount() {
        return this.params.size();
    }
    
    public CallParameter getParamByName(final String name) {
        return null;
    }
    
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
    
    static {
        PARAMETER_PATTERN = Pattern.compile("\\s*(IN\\s+|OUT\\s+|INOUT\\s+)?(\\`[\\w\\d]+\\`)\\s+(UNSIGNED\\s+)?(\\w+)\\s*(\\([\\d,]+\\))?\\s*", 2);
        RETURN_PATTERN = Pattern.compile("\\s*(UNSIGNED\\s+)?(\\w+)\\s*(\\([\\d,]+\\))?\\s*(CHARSET\\s+)?(\\w+)?\\s*", 2);
    }
}
