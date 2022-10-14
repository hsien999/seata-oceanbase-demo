// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.RowId;
import java.io.Reader;
import java.io.InputStream;
import java.net.URL;
import java.util.Calendar;
import java.sql.Array;
import java.sql.Clob;
import java.sql.Blob;
import java.sql.Ref;
import java.util.Map;
import java.sql.Timestamp;
import java.sql.Time;
import java.sql.Date;
import java.math.BigDecimal;
import java.sql.Statement;
import java.sql.ParameterMetaData;
import java.util.HashMap;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.util.Iterator;
import java.sql.SQLException;
import java.util.List;
import java.lang.reflect.Constructor;
import java.sql.CallableStatement;

public class ServerCallableStatement extends ServerPreparedStatement implements CallableStatement
{
    protected static final Constructor<?> JDBC_4_SCSTMT_2_ARGS_CTOR;
    protected static final Constructor<?> JDBC_4_SCSTMT_4_ARGS_CTOR;
    private boolean callingStoredFunction;
    protected boolean outputParamWasNull;
    private int[] parameterIndexToRsIndex;
    private int[] placeholderToParameterIndexMap;
    protected CallableStatementParamInfo paramInfo;
    private com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam returnValueParam;
    private static final int NOT_OUTPUT_PARAMETER_INDICATOR = Integer.MIN_VALUE;
    
    private void generateParameterMap() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            int parameterCountFromMetaData = this.paramInfo.getParameterCount();
            if (this.callingStoredFunction) {
                --parameterCountFromMetaData;
            }
            if (this.paramInfo != null && this.parameterCount != parameterCountFromMetaData) {
                this.placeholderToParameterIndexMap = new int[this.parameterCount];
                final int startPos = this.callingStoredFunction ? StringUtils.indexOfIgnoreCase(this.originalSql, "SELECT") : StringUtils.indexOfIgnoreCase(this.originalSql, "CALL");
                if (startPos != -1) {
                    final int parenOpenPos = this.originalSql.indexOf(40, startPos + 4);
                    if (parenOpenPos != -1) {
                        final int parenClosePos = StringUtils.indexOfIgnoreCase(parenOpenPos, this.originalSql, ")", "'", "'", StringUtils.SEARCH_MODE__ALL);
                        if (parenClosePos != -1) {
                            final List<?> parsedParameters = StringUtils.split(this.originalSql.substring(parenOpenPos + 1, parenClosePos), ",", "'\"", "'\"", true);
                            final int numParsedParameters = parsedParameters.size();
                            if (numParsedParameters != this.parameterCount) {}
                            int placeholderCount = 0;
                            for (int i = 0; i < numParsedParameters; ++i) {
                                if (((String)parsedParameters.get(i)).equals("?")) {
                                    this.placeholderToParameterIndexMap[placeholderCount++] = i;
                                }
                            }
                        }
                    }
                }
            }
            this.initParamIndexToRsIndex();
        }
    }
    
    public void initParamIndexToRsIndex() {
        this.parameterIndexToRsIndex = new int[this.paramInfo.numberOfParameters()];
        for (int i = 0; i < this.paramInfo.numberOfParameters(); ++i) {
            this.parameterIndexToRsIndex[i] = Integer.MIN_VALUE;
        }
        int localParamIndex = 0;
        for (final com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam param : this.paramInfo) {
            if (param.isOut) {
                this.parameterIndexToRsIndex[param.index] = localParamIndex++;
            }
        }
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        if (this.connection.getIO().isOracleMode()) {
            return null;
        }
        return super.getResultSet();
    }
    
    @Override
    public boolean getMoreResults() throws SQLException {
        return !this.connection.getIO().isOracleMode() && super.getMoreResults();
    }
    
    @Override
    public int getUpdateCount() throws SQLException {
        if (this.connection.getIO().isOracleMode()) {
            return -1;
        }
        return super.getUpdateCount();
    }
    
    protected static ServerCallableStatement getInstance(final MySQLConnection conn, final String sql, final String catalog, final boolean isFunctionCall) throws SQLException {
        if (!Util.isJdbc4()) {
            return new ServerCallableStatement(conn, sql, catalog, isFunctionCall);
        }
        return (ServerCallableStatement)Util.handleNewInstance(ServerCallableStatement.JDBC_4_SCSTMT_4_ARGS_CTOR, new Object[] { conn, sql, catalog, isFunctionCall }, conn.getExceptionInterceptor());
    }
    
    protected static ServerCallableStatement getInstance(final MySQLConnection conn, final CallableStatementParamInfo paramInfo) throws SQLException {
        if (!Util.isJdbc4()) {
            return new ServerCallableStatement(conn, paramInfo);
        }
        return (ServerCallableStatement)Util.handleNewInstance(ServerCallableStatement.JDBC_4_SCSTMT_2_ARGS_CTOR, new Object[] { conn, paramInfo }, conn.getExceptionInterceptor());
    }
    
    public ServerCallableStatement(final MySQLConnection conn, final String sql, final String catalog, final boolean isFunctionCall) throws SQLException {
        super(conn, sql, catalog, 1003, 1007);
        this.callingStoredFunction = false;
        this.outputParamWasNull = false;
        this.psStats.setServerCallablePSType();
        this.callingStoredFunction = isFunctionCall;
        if (this.isAnonymousBlocks()) {
            this.generageParameterTypes();
        }
        else {
            this.determineParameterTypes();
        }
        this.generateParameterMap();
        this.retrieveGeneratedKeys = true;
    }
    
    public ServerCallableStatement(final MySQLConnection conn, final CallableStatementParamInfo paramInfo) throws SQLException {
        super(conn, paramInfo.nativeSql, paramInfo.catalogInUse, 1003, 1007);
        this.callingStoredFunction = false;
        this.outputParamWasNull = false;
        this.psStats.setServerCallablePSType();
        this.paramInfo = paramInfo;
        this.callingStoredFunction = this.paramInfo.isFunctionCall;
        if (this.callingStoredFunction) {
            ++this.parameterCount;
        }
        if (this.parameterIndexToRsIndex == null) {
            this.initParamIndexToRsIndex();
        }
        this.retrieveGeneratedKeys = true;
    }
    
    private boolean isAnonymousBlocks() {
        boolean ret = false;
        final String sanitizedSql = StringUtils.stripComments(this.originalSql, "`\"'", "`\"'", true, false, true, true);
        int endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "begin ");
        endCallIndex = ((endCallIndex != -1) ? endCallIndex : StringUtils.indexOfIgnoreCase(sanitizedSql, "begin\n"));
        if (endCallIndex != -1) {
            ret = true;
        }
        return ret;
    }
    
    private void generageParameterTypes() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.paramInfo = new CallableStatementParamInfo();
            this.paramInfo.numParameters = 0;
            this.paramInfo.parameterList = new ArrayList<com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam>();
            this.paramInfo.parameterMap = new HashMap<String, com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam>();
            final ParameterMetaData parameterMetaData = this.getParameterMetaData();
            if (parameterMetaData instanceof MysqlParameterMetadata) {
                final MysqlParameterMetadata mysqlParameterMetadata = (MysqlParameterMetadata)parameterMetaData;
                for (int parancount = mysqlParameterMetadata.getParameterCount(), i = 1; i <= parancount; ++i) {
                    final String paramName = mysqlParameterMetadata.getParameterClassName(i);
                    final int inoutType = mysqlParameterMetadata.getParameterInOutType(i);
                    boolean isOutParameter = false;
                    boolean isInParameter = false;
                    if (inoutType == 1) {
                        isInParameter = true;
                    }
                    else if (inoutType == 2) {
                        isOutParameter = true;
                    }
                    else if (inoutType == 3) {
                        isInParameter = true;
                        isOutParameter = true;
                    }
                    final String typeName = mysqlParameterMetadata.getParameterTypeName(i);
                    final int jdbcType = MysqlDefs.mysqlToJavaType(typeName);
                    final int precision = mysqlParameterMetadata.getPrecision(i);
                    final int scale = mysqlParameterMetadata.getScale(i);
                    final short nullability = 1;
                    final int inOutModifier = 0;
                    final com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam paramInfoToAdd = new com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam(paramName, this.paramInfo.numParameters, isInParameter, isOutParameter, jdbcType, typeName, precision, scale, nullability, inOutModifier);
                    this.paramInfo.parameterList.add(paramInfoToAdd);
                    this.paramInfo.parameterMap.put(paramName, paramInfoToAdd);
                    final CallableStatementParamInfo paramInfo = this.paramInfo;
                    ++paramInfo.numParameters;
                }
            }
        }
    }
    
    private void determineParameterTypes() throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            ResultSet paramTypesRs = null;
            Statement paramRetrievalStmt = null;
            paramRetrievalStmt = this.connection.getMetadataSafeStatement();
            final String sqlHeader = "SELECT DISTINCT(ARGUMENT_NAME), IN_OUT, DATA_TYPE, DATA_PRECISION, DATA_SCALE, POSITION FROM ALL_ARGUMENTS WHERE (OVERLOAD is NULL OR OVERLOAD = 1) and POSITION != 0 AND object_name = '";
            final StringBuilder paramMetaSql = new StringBuilder(sqlHeader);
            final String objectName = this.extractProcedureName();
            final String[] objectNameList = objectName.split("\\.");
            if (objectNameList.length == 2) {
                paramMetaSql.append(objectNameList[1].toUpperCase());
                paramMetaSql.append("' and package_name = '").append(objectNameList[0].toUpperCase());
            }
            else {
                paramMetaSql.append(objectNameList[0].toUpperCase());
            }
            paramMetaSql.append("' order by POSITION");
            paramTypesRs = paramRetrievalStmt.executeQuery(paramMetaSql.toString());
            if (paramTypesRs.next()) {
                paramTypesRs.previous();
                this.convertGetProcedureColumnsToInternalDescriptors(paramTypesRs);
            }
            if (this.callingStoredFunction) {
                if (this.paramInfo == null) {
                    this.paramInfo = new CallableStatementParamInfo();
                }
                final CallableStatementParamInfo paramInfo = this.paramInfo;
                ++paramInfo.numParameters;
            }
        }
    }
    
    @Override
    protected void updateParamInfoBaseOnResultSet(final Field[] fields) {
        if (this.isAnonymousBlocks()) {
            for (int j = 0, i = 0; i < this.paramInfo.numParameters && j < fields.length; ++i) {
                if (this.paramInfo.parameterList.get(i).isOut) {
                    this.paramInfo.parameterList.get(i).jdbcType = fields[j].getMysqlType();
                    this.paramInfo.parameterList.get(i).precision = fields[j].getPrecision();
                    ++j;
                }
            }
        }
    }
    
    private void convertGetProcedureColumnsToInternalDescriptors(final ResultSet paramTypesRs) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.paramInfo = new CallableStatementParamInfo(paramTypesRs);
        }
    }
    
    private String extractProcedureName() throws SQLException {
        final String sanitizedSql = StringUtils.stripComments(this.originalSql, "`\"'", "`\"'", true, false, true, true);
        int endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "CALL ");
        int offset = 5;
        if (endCallIndex == -1) {
            endCallIndex = StringUtils.indexOfIgnoreCase(sanitizedSql, "SELECT ");
            offset = 7;
        }
        if (endCallIndex != -1) {
            final StringBuilder nameBuf = new StringBuilder();
            final String trimmedStatement = sanitizedSql.substring(endCallIndex + offset).trim();
            for (int statementLength = trimmedStatement.length(), i = 0; i < statementLength; ++i) {
                final char c = trimmedStatement.charAt(i);
                if (Character.isWhitespace(c) || c == '(') {
                    break;
                }
                if (c == '?') {
                    break;
                }
                nameBuf.append(c);
            }
            return nameBuf.toString();
        }
        throw SQLError.createSQLException(Messages.getString("CallableStatement.1"), "S1000", this.getExceptionInterceptor());
    }
    
    protected ResultSetInternalMethods getOutputParameters(final int paramIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.outputParamWasNull = false;
            return this.results;
        }
    }
    
    protected int mapOutputParameterIndexToRsIndex(final int paramIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction && paramIndex == 1) {
                return 1;
            }
            this.checkParameterIndexBounds(paramIndex);
            int localParamIndex = paramIndex - 1;
            if (this.placeholderToParameterIndexMap != null) {
                localParamIndex = this.placeholderToParameterIndexMap[localParamIndex];
            }
            final int rsIndex = this.parameterIndexToRsIndex[localParamIndex];
            if (rsIndex == Integer.MIN_VALUE) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.21") + paramIndex + Messages.getString("CallableStatement.22"), "S1009", this.getExceptionInterceptor());
            }
            return rsIndex + 1;
        }
    }
    
    public com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam checkIsOutputParam(int paramIndex) throws SQLException {
        if (this.callingStoredFunction) {
            if (paramIndex == 1) {
                if (this.returnValueParam == null) {
                    this.returnValueParam = new com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam("", 0, false, true, 12, "VARCHAR", 0, 0, (short)2, 5);
                }
                return this.returnValueParam;
            }
            --paramIndex;
        }
        this.checkParameterIndexBounds(paramIndex);
        int localParamIndex = paramIndex - 1;
        if (this.placeholderToParameterIndexMap != null) {
            localParamIndex = this.placeholderToParameterIndexMap[localParamIndex];
        }
        final com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam param = this.paramInfo.getParameter(localParamIndex);
        if (!param.isOut) {
            throw SQLError.createSQLException(Messages.getString("CallableStatement.9") + paramIndex + Messages.getString("CallableStatement.10"), "S1009", this.getExceptionInterceptor());
        }
        return param;
    }
    
    private void checkParameterIndexBounds(final int paramIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.paramInfo.checkBounds(paramIndex);
        }
    }
    
    @Override
    public boolean wasNull() throws SQLException {
        return this.outputParamWasNull;
    }
    
    @Override
    public String getString(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final String retValue = this.results.getString(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public boolean getBoolean(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final boolean retValue = this.results.getBoolean(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public byte getByte(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final byte retValue = this.results.getByte(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public short getShort(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final short retValue = this.results.getShort(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public int getInt(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final int retValue = this.results.getInt(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public long getLong(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final long retValue = this.results.getLong(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public float getFloat(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final float retValue = this.results.getFloat(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public double getDouble(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final double retValue = this.results.getDouble(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public BigDecimal getBigDecimal(final int parameterIndex, final int scale) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final BigDecimal retValue = this.results.getBigDecimal(this.mapOutputParameterIndexToRsIndex(parameterIndex), scale);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public byte[] getBytes(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final byte[] retValue = this.results.getBytes(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Date getDate(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Date retValue = this.results.getDate(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Time getTime(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Time retValue = this.results.getTime(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Timestamp getTimestamp(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Timestamp retValue = this.results.getTimestamp(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Object getObject(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam param = this.checkIsOutputParam(parameterIndex);
            final Object retVal = this.results.getObjectStoredProc(this.mapOutputParameterIndexToRsIndex(parameterIndex), param.jdbcType);
            this.outputParamWasNull = this.results.wasNull();
            return retVal;
        }
    }
    
    @Override
    public BigDecimal getBigDecimal(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final BigDecimal retValue = this.results.getBigDecimal(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Object getObject(final int parameterIndex, final Map<String, Class<?>> map) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Object retValue = this.results.getObject(this.mapOutputParameterIndexToRsIndex(parameterIndex), map);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Ref getRef(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Ref retValue = this.results.getRef(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Blob getBlob(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Blob retValue = this.results.getBlob(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Clob getClob(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Clob retValue = this.results.getClob(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Array getArray(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Array retValue = this.results.getArray(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Date getDate(final int parameterIndex, final Calendar calendar) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Date retValue = this.results.getDate(this.mapOutputParameterIndexToRsIndex(parameterIndex), calendar);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Time getTime(final int parameterIndex, final Calendar calendar) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Time retValue = this.results.getTime(this.mapOutputParameterIndexToRsIndex(parameterIndex), calendar);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Timestamp getTimestamp(final int parameterIndex, final Calendar calendar) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Timestamp retValue = this.results.getTimestamp(this.mapOutputParameterIndexToRsIndex(parameterIndex), calendar);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public void registerOutParameter(final int parameterIndex, final int type) throws SQLException {
        if (this.callingStoredFunction && parameterIndex == 1) {
            return;
        }
        final com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam param = this.checkIsOutputParam(parameterIndex);
        if (param.isOut && !param.isIn) {
            this.setNull(parameterIndex, type);
        }
    }
    
    @Override
    public void registerOutParameter(final int parameterIndex, final int sqlType, final int scale) throws SQLException {
        this.registerOutParameter(parameterIndex, sqlType);
    }
    
    @Override
    public void registerOutParameter(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
        final com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam param = this.checkIsOutputParam(parameterIndex);
        if (param.isOut && !param.isIn) {
            this.setNull(parameterIndex, sqlType, typeName);
        }
    }
    
    @Override
    public void registerOutParameter(final String parameterName, final int sqlType) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType);
        }
    }
    
    @Override
    public void registerOutParameter(final String parameterName, final int sqlType, final int scale) throws SQLException {
        this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType);
    }
    
    @Override
    public void registerOutParameter(final String parameterName, final int sqlType, final String typeName) throws SQLException {
        this.registerOutParameter(this.getNamedParamIndex(parameterName, true), sqlType, typeName);
    }
    
    private int getNamedParamIndex(final String paramName, final boolean forOut) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.connection.getNoAccessToProcedureBodies()) {
                throw SQLError.createSQLException("No access to parameters by name when connection has been configured not to access procedure bodies", "S1009", this.getExceptionInterceptor());
            }
            if (paramName == null || paramName.length() == 0) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.2"), "S1009", this.getExceptionInterceptor());
            }
            if (this.paramInfo == null) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.3") + paramName + Messages.getString("CallableStatement.4"), "S1009", this.getExceptionInterceptor());
            }
            final com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam param = this.paramInfo.getParameter(paramName);
            if (forOut && !param.isOut) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.5") + paramName + Messages.getString("CallableStatement.6"), "S1009", this.getExceptionInterceptor());
            }
            if (this.placeholderToParameterIndexMap == null) {
                return param.index + 1;
            }
            for (int i = 0; i < this.placeholderToParameterIndexMap.length; ++i) {
                if (this.placeholderToParameterIndexMap[i] == param.index) {
                    return i + 1;
                }
            }
            throw SQLError.createSQLException("Can't find local placeholder mapping for parameter named \"" + paramName + "\".", "S1009", this.getExceptionInterceptor());
        }
    }
    
    @Override
    public URL getURL(final int parameterIndex) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final URL retValue = this.results.getURL(this.mapOutputParameterIndexToRsIndex(parameterIndex));
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public void setURL(int parameterIndex, final URL x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setURL(parameterIndex, x);
        }
    }
    
    @Override
    public void setURL(final String parameterName, final URL url) throws SQLException {
        this.setURL(this.getNamedParamIndex(parameterName, false), url);
    }
    
    @Override
    public void setNull(int parameterIndex, final int sqlType) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setNull(parameterIndex, sqlType);
        }
    }
    
    @Override
    public void setNull(final String parameterName, final int sqlType) throws SQLException {
        this.setNull(this.getNamedParamIndex(parameterName, false), sqlType);
    }
    
    @Override
    public void setNull(int parameterIndex, final int sqlType, final String typeName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setNull(parameterIndex, sqlType, typeName);
        }
    }
    
    @Override
    public void setNull(final String parameterName, final int sqlType, final String typeName) throws SQLException {
        this.setNull(this.getNamedParamIndex(parameterName, false), sqlType, typeName);
    }
    
    @Override
    public void setBoolean(int parameterIndex, final boolean x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setBoolean(parameterIndex, x);
        }
    }
    
    @Override
    public void setBoolean(final String parameterName, final boolean x) throws SQLException {
        this.setBoolean(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setByte(int parameterIndex, final byte x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setByte(parameterIndex, x);
        }
    }
    
    @Override
    public void setByte(final String parameterName, final byte x) throws SQLException {
        this.setByte(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setShort(int parameterIndex, final short x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setShort(parameterIndex, x);
        }
    }
    
    @Override
    public void setShort(final String parameterName, final short x) throws SQLException {
        this.setShort(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setInt(int parameterIndex, final int x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setInt(parameterIndex, x);
        }
    }
    
    @Override
    public void setInt(final String parameterName, final int x) throws SQLException {
        this.setInt(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setLong(int parameterIndex, final long x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setLong(parameterIndex, x);
        }
    }
    
    @Override
    public void setLong(final String parameterName, final long x) throws SQLException {
        this.setLong(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setFloat(int parameterIndex, final float x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setFloat(parameterIndex, x);
        }
    }
    
    @Override
    public void setFloat(final String parameterName, final float x) throws SQLException {
        this.setFloat(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setDouble(int parameterIndex, final double x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setDouble(parameterIndex, x);
        }
    }
    
    @Override
    public void setDouble(final String parameterName, final double x) throws SQLException {
        this.setDouble(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setBigDecimal(int parameterIndex, final BigDecimal x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setBigDecimal(parameterIndex, x);
        }
    }
    
    @Override
    public void setBigDecimal(final String parameterName, final BigDecimal x) throws SQLException {
        this.setBigDecimal(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setString(int parameterIndex, final String x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setString(parameterIndex, x);
        }
    }
    
    @Override
    public void setString(final String parameterName, final String x) throws SQLException {
        this.setString(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setBytes(int parameterIndex, final byte[] x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setBytes(parameterIndex, x);
        }
    }
    
    @Override
    public void setBytes(final String parameterName, final byte[] x) throws SQLException {
        this.setBytes(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setDate(int parameterIndex, final Date x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setDate(parameterIndex, x);
        }
    }
    
    @Override
    public void setDate(final String parameterName, final Date x) throws SQLException {
        this.setDate(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setTime(int parameterIndex, final Time x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setTime(parameterIndex, x);
        }
    }
    
    @Override
    public void setTime(final String parameterName, final Time x) throws SQLException {
        this.setTime(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setTimestamp(int parameterIndex, final Timestamp x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setTimestamp(parameterIndex, x);
        }
    }
    
    @Override
    public void setTimestamp(final String parameterName, final Timestamp x) throws SQLException {
        this.setTimestamp(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setDate(int parameterIndex, final Date x, final Calendar calendar) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setDate(parameterIndex, x, calendar);
        }
    }
    
    @Override
    public void setDate(final String parameterName, final Date x, final Calendar calendar) throws SQLException {
        this.setDate(this.getNamedParamIndex(parameterName, false), x, calendar);
    }
    
    @Override
    public void setTime(int parameterIndex, final Time x, final Calendar calendar) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setTime(parameterIndex, x, calendar);
        }
    }
    
    @Override
    public void setTime(final String parameterName, final Time x, final Calendar calendar) throws SQLException {
        this.setTime(this.getNamedParamIndex(parameterName, false), x, calendar);
    }
    
    @Override
    public void setTimestamp(int parameterIndex, final Timestamp x, final Calendar calendar) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setTimestamp(parameterIndex, x, calendar);
        }
    }
    
    @Override
    public void setTimestamp(final String parameterName, final Timestamp x, final Calendar calendar) throws SQLException {
        this.setTimestamp(this.getNamedParamIndex(parameterName, false), x, calendar);
    }
    
    @Override
    public void setAsciiStream(int parameterIndex, final InputStream x, final int length) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setAsciiStream(parameterIndex, x, length);
        }
    }
    
    @Override
    public void setAsciiStream(final String parameterName, final InputStream x, final int length) throws SQLException {
        this.setAsciiStream(this.getNamedParamIndex(parameterName, false), x, length);
    }
    
    @Override
    public void setBinaryStream(int parameterIndex, final InputStream x, final int length) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setBinaryStream(parameterIndex, x, length);
        }
    }
    
    @Override
    public void setBinaryStream(final String parameterName, final InputStream x, final int length) throws SQLException {
        this.setBinaryStream(this.getNamedParamIndex(parameterName, false), x, length);
    }
    
    @Override
    public void setObject(final String parameterName, final Object x, final int targetSqlType, final int scale) throws SQLException {
    }
    
    @Override
    public void setObject(int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setObject(parameterIndex, x, targetSqlType);
        }
    }
    
    @Override
    public void setObject(final String parameterName, final Object x, final int targetSqlType) throws SQLException {
        this.setObject(this.getNamedParamIndex(parameterName, false), x, targetSqlType);
    }
    
    @Override
    public void setObject(int parameterIndex, final Object x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setObject(parameterIndex, x);
        }
    }
    
    @Override
    public void setObject(final String parameterName, final Object x) throws SQLException {
        this.setObject(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setCharacterStream(int parameterIndex, final Reader x, final int length) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setCharacterStream(parameterIndex, x, length);
        }
    }
    
    @Override
    public void setCharacterStream(final String parameterName, final Reader reader, final int length) throws SQLException {
        this.setCharacterStream(this.getNamedParamIndex(parameterName, false), reader, length);
    }
    
    private void checkResults() throws SQLException {
        if (this.results != null && !this.results.isFirst()) {
            this.results.first();
        }
    }
    
    @Override
    public String getString(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final String retValue = this.results.getString(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public boolean getBoolean(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final boolean retValue = this.results.getBoolean(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public byte getByte(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final byte retValue = this.results.getByte(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public short getShort(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final short retValue = this.results.getShort(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public int getInt(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final int retValue = this.results.getInt(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public long getLong(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final long retValue = this.results.getLong(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public float getFloat(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final float retValue = (float)this.results.getLong(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public double getDouble(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final double retValue = (double)this.results.getLong(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public byte[] getBytes(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final byte[] retValue = this.results.getBytes(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Date getDate(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Date retValue = this.results.getDate(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Time getTime(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Time retValue = this.results.getTime(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Timestamp getTimestamp(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Timestamp retValue = this.results.getTimestamp(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Object getObject(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Object retValue = this.results.getObject(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public BigDecimal getBigDecimal(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final BigDecimal retValue = this.results.getBigDecimal(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Object getObject(final String parameterName, final Map<String, Class<?>> map) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Object retValue = this.results.getObject(parameterName, map);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Ref getRef(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Ref retValue = this.results.getRef(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Blob getBlob(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Blob retValue = this.results.getBlob(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Clob getClob(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Clob retValue = this.results.getClob(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Array getArray(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Array retValue = this.results.getArray(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Date getDate(final String parameterName, final Calendar calendar) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Date retValue = this.results.getDate(parameterName, calendar);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Time getTime(final String parameterName, final Calendar calendar) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Time retValue = this.results.getTime(parameterName, calendar);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public Timestamp getTimestamp(final String parameterName, final Calendar calendar) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final Timestamp retValue = this.results.getTimestamp(parameterName, calendar);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public URL getURL(final String parameterName) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final URL retValue = this.results.getURL(parameterName);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    @Override
    public RowId getRowId(final int i) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public RowId getRowId(final String s) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setRowId(final String s, final RowId rowId) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNString(final String s, final String s1) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setNCharacterStream(final String parameterName, final Reader value, final long length) throws SQLException {
        this.setNCharacterStream(this.getNamedParamIndex(parameterName, false), value, length);
    }
    
    @Override
    public void setNClob(final String s, final NClob nClob) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setClob(final String parameterName, final Reader reader, final long length) throws SQLException {
        this.setClob(this.getNamedParamIndex(parameterName, false), reader, length);
    }
    
    @Override
    public void setBlob(final String parameterName, final InputStream inputStream, final long length) throws SQLException {
        this.setBlob(this.getNamedParamIndex(parameterName, false), inputStream, length);
    }
    
    @Override
    public void setNClob(final String parameterName, final Reader reader, final long length) throws SQLException {
        this.setNClob(this.getNamedParamIndex(parameterName, false), reader, length);
    }
    
    @Override
    public NClob getNClob(final int parameterIndex) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public NClob getNClob(final String parameterName) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setSQLXML(final String s, final SQLXML sqlxml) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public SQLXML getSQLXML(final int i) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public SQLXML getSQLXML(final String s) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public String getNString(final int i) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public String getNString(final String s) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Reader getNCharacterStream(final int i) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Reader getNCharacterStream(final String s) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Reader getCharacterStream(final int i) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public Reader getCharacterStream(final String s) throws SQLException {
        throw SQLError.createSQLFeatureNotSupportedException();
    }
    
    @Override
    public void setBlob(int parameterIndex, final Blob x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setBlob(parameterIndex, x);
        }
    }
    
    @Override
    public void setBlob(final String parameterName, final Blob x) throws SQLException {
        this.setBlob(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setClob(int parameterIndex, final Clob x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setClob(parameterIndex, x);
        }
    }
    
    @Override
    public void setClob(final String parameterName, final Clob x) throws SQLException {
        this.setClob(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setAsciiStream(int parameterIndex, final InputStream x, final long length) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setAsciiStream(parameterIndex, x, length);
        }
    }
    
    @Override
    public void setAsciiStream(final String parameterName, final InputStream inputStream, final long length) throws SQLException {
        this.setAsciiStream(this.getNamedParamIndex(parameterName, false), inputStream, length);
    }
    
    @Override
    public void setBinaryStream(int parameterIndex, final InputStream x, final long length) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setBinaryStream(parameterIndex, x, length);
        }
    }
    
    @Override
    public void setBinaryStream(final String parameterName, final InputStream inputStream, final long length) throws SQLException {
        this.setBinaryStream(this.getNamedParamIndex(parameterName, false), inputStream, length);
    }
    
    @Override
    public void setCharacterStream(int parameterIndex, final Reader x, final long length) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setCharacterStream(parameterIndex, x, length);
        }
    }
    
    @Override
    public void setCharacterStream(final String parameterName, final Reader reader, final long length) throws SQLException {
        this.setCharacterStream(this.getNamedParamIndex(parameterName, false), reader, length);
    }
    
    @Override
    public void setAsciiStream(int parameterIndex, final InputStream x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setAsciiStream(parameterIndex, x);
        }
    }
    
    @Override
    public void setAsciiStream(final String parameterName, final InputStream x) throws SQLException {
        this.setAsciiStream(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setBinaryStream(int parameterIndex, final InputStream x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setBinaryStream(parameterIndex, x);
        }
    }
    
    @Override
    public void setBinaryStream(final String parameterName, final InputStream x) throws SQLException {
        this.setBinaryStream(this.getNamedParamIndex(parameterName, false), x);
    }
    
    @Override
    public void setCharacterStream(int parameterIndex, final Reader x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setCharacterStream(parameterIndex, x);
        }
    }
    
    @Override
    public void setCharacterStream(final String parameterName, final Reader reader) throws SQLException {
        this.setCharacterStream(this.getNamedParamIndex(parameterName, false), reader);
    }
    
    @Override
    public void setNCharacterStream(int parameterIndex, final Reader x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setNCharacterStream(parameterIndex, x);
        }
    }
    
    @Override
    public void setNCharacterStream(final String parameterName, final Reader reader) throws SQLException {
        this.setNCharacterStream(this.getNamedParamIndex(parameterName, false), reader);
    }
    
    @Override
    public void setClob(int parameterIndex, final Reader x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setClob(parameterIndex, x);
        }
    }
    
    @Override
    public void setClob(final String parameterName, final Reader reader) throws SQLException {
        this.setClob(this.getNamedParamIndex(parameterName, false), reader);
    }
    
    @Override
    public void setBlob(int parameterIndex, final InputStream x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setBlob(parameterIndex, x);
        }
    }
    
    @Override
    public void setBlob(final String parameterName, final InputStream inputStream) throws SQLException {
        this.setBlob(this.getNamedParamIndex(parameterName, false), inputStream);
    }
    
    @Override
    public void setNClob(int parameterIndex, final Reader x) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            if (this.callingStoredFunction) {
                --parameterIndex;
            }
            super.setNClob(parameterIndex, x);
        }
    }
    
    @Override
    public void setNClob(final String parameterName, final Reader reader) throws SQLException {
        this.setNClob(this.getNamedParamIndex(parameterName, false), reader);
    }
    
    @Override
    public <T> T getObject(final int parameterIndex, final Class<T> type) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final T retVal = this.results.getObject(this.mapOutputParameterIndexToRsIndex(parameterIndex), type);
            this.outputParamWasNull = this.results.wasNull();
            return retVal;
        }
    }
    
    @Override
    public <T> T getObject(final String parameterName, final Class<T> type) throws SQLException {
        synchronized (this.checkClosed().getConnectionMutex()) {
            this.checkResults();
            final T retValue = this.results.getObject(parameterName, type);
            this.outputParamWasNull = this.results.wasNull();
            return retValue;
        }
    }
    
    static {
        if (Util.isJdbc4()) {
            try {
                final String jdbc4ClassName = Util.isJdbc42() ? "com.alipay.oceanbase.jdbc.JDBC42ServerCallableStatement" : "com.alipay.oceanbase.jdbc.JDBC4ServerCallableStatement";
                JDBC_4_SCSTMT_2_ARGS_CTOR = Class.forName(jdbc4ClassName).getConstructor(MySQLConnection.class, CallableStatementParamInfo.class);
                JDBC_4_SCSTMT_4_ARGS_CTOR = Class.forName(jdbc4ClassName).getConstructor(MySQLConnection.class, String.class, String.class, Boolean.TYPE);
                return;
            }
            catch (SecurityException e) {
                throw new RuntimeException(e);
            }
            catch (NoSuchMethodException e2) {
                throw new RuntimeException(e2);
            }
            catch (ClassNotFoundException e3) {
                throw new RuntimeException(e3);
            }
        }
        JDBC_4_SCSTMT_2_ARGS_CTOR = null;
        JDBC_4_SCSTMT_4_ARGS_CTOR = null;
    }
    
    protected class CallableStatementParamInfo implements ParameterMetaData
    {
        String catalogInUse;
        boolean isFunctionCall;
        String nativeSql;
        int numParameters;
        List<com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam> parameterList;
        Map<String, com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam> parameterMap;
        boolean isReadOnlySafeProcedure;
        boolean isReadOnlySafeChecked;
        
        CallableStatementParamInfo() {
            this.isReadOnlySafeProcedure = false;
            this.isReadOnlySafeChecked = false;
            this.parameterList = new ArrayList<com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam>(0);
            this.parameterMap = new HashMap<String, com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam>(0);
        }
        
        CallableStatementParamInfo(final CallableStatementParamInfo fullParamInfo) {
            this.isReadOnlySafeProcedure = false;
            this.isReadOnlySafeChecked = false;
            this.nativeSql = ServerCallableStatement.this.originalSql;
            this.catalogInUse = ServerCallableStatement.this.currentCatalog;
            this.isFunctionCall = fullParamInfo.isFunctionCall;
            final int[] localParameterMap = ServerCallableStatement.this.placeholderToParameterIndexMap;
            final int parameterMapLength = localParameterMap.length;
            this.isReadOnlySafeProcedure = fullParamInfo.isReadOnlySafeProcedure;
            this.isReadOnlySafeChecked = fullParamInfo.isReadOnlySafeChecked;
            this.parameterList = new ArrayList<com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam>(fullParamInfo.numParameters);
            this.parameterMap = new HashMap<String, com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam>(fullParamInfo.numParameters);
            if (this.isFunctionCall) {
                this.parameterList.add(fullParamInfo.parameterList.get(0));
            }
            final int offset = this.isFunctionCall ? 1 : 0;
            for (int i = 0; i < parameterMapLength; ++i) {
                if (localParameterMap[i] != 0) {
                    final com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam param = fullParamInfo.parameterList.get(localParameterMap[i] + offset);
                    this.parameterList.add(param);
                    this.parameterMap.put(param.paramName, param);
                }
            }
            this.numParameters = this.parameterList.size();
        }
        
        CallableStatementParamInfo(final ResultSet paramTypesRs) throws SQLException {
            this.isReadOnlySafeProcedure = false;
            this.isReadOnlySafeChecked = false;
            final boolean hadRows = paramTypesRs.last();
            this.nativeSql = ServerCallableStatement.this.originalSql;
            this.catalogInUse = ServerCallableStatement.this.currentCatalog;
            this.isFunctionCall = ServerCallableStatement.this.callingStoredFunction;
            if (hadRows) {
                this.numParameters = paramTypesRs.getRow();
                this.parameterList = new ArrayList<com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam>(this.numParameters);
                this.parameterMap = new HashMap<String, com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam>(this.numParameters);
                paramTypesRs.beforeFirst();
                this.addParametersFromDBOD(paramTypesRs);
            }
            else {
                this.numParameters = 0;
            }
            if (this.isFunctionCall) {
                ++this.numParameters;
            }
        }
        
        private void addParametersFromDBOD(final ResultSet paramTypesRs) throws SQLException {
            int i = 0;
            while (paramTypesRs.next()) {
                final String paramName = paramTypesRs.getString("ARGUMENT_NAME");
                final String inOut = paramTypesRs.getString("IN_OUT");
                int inOutModifier = 0;
                boolean isOutParameter = false;
                boolean isInParameter = false;
                if (this.numberOfParameters() == 0 && this.isFunctionCall) {
                    isOutParameter = true;
                    isInParameter = false;
                }
                else if (null == inOut || inOut.equalsIgnoreCase("IN")) {
                    isOutParameter = false;
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
                    isInParameter = false;
                    inOutModifier = 4;
                }
                else {
                    isInParameter = true;
                    isOutParameter = false;
                    inOutModifier = 1;
                }
                final String typeName = paramTypesRs.getString("DATA_TYPE");
                final int precision = paramTypesRs.getInt("DATA_PRECISION");
                final int scale = paramTypesRs.getInt("DATA_SCALE");
                final int jdbcType = MysqlDefs.mysqlToJavaType(typeName);
                final short nullability = 1;
                final com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam paramInfoToAdd = new com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam(paramName, i++, isInParameter, isOutParameter, jdbcType, typeName, precision, scale, nullability, inOutModifier);
                this.parameterList.add(paramInfoToAdd);
                this.parameterMap.put(paramName, paramInfoToAdd);
            }
        }
        
        protected void checkBounds(final int paramIndex) throws SQLException {
            final int localParamIndex = paramIndex - 1;
            if (paramIndex < 0 || localParamIndex >= this.numParameters) {
                throw SQLError.createSQLException(Messages.getString("CallableStatement.11") + paramIndex + Messages.getString("CallableStatement.12") + this.numParameters + Messages.getString("CallableStatement.13"), "S1009", ServerCallableStatement.this.getExceptionInterceptor());
            }
        }
        
        @Override
        protected Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
        
        com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam getParameter(final int index) {
            return this.parameterList.get(index);
        }
        
        com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam getParameter(final String name) {
            return this.parameterMap.get(name);
        }
        
        @Override
        public String getParameterClassName(final int arg0) throws SQLException {
            final String mysqlTypeName = this.getParameterTypeName(arg0);
            final boolean isBinaryOrBlob = StringUtils.indexOfIgnoreCase(mysqlTypeName, "BLOB") != -1 || StringUtils.indexOfIgnoreCase(mysqlTypeName, "BINARY") != -1;
            final boolean isUnsigned = StringUtils.indexOfIgnoreCase(mysqlTypeName, "UNSIGNED") != -1;
            int mysqlTypeIfKnown = 0;
            if (StringUtils.startsWithIgnoreCase(mysqlTypeName, "MEDIUMINT")) {
                mysqlTypeIfKnown = 9;
            }
            return ResultSetMetaData.getClassNameForJavaType(this.getParameterType(arg0), isUnsigned, mysqlTypeIfKnown, isBinaryOrBlob, false, ServerCallableStatement.this.connection.getYearIsDateType());
        }
        
        @Override
        public int getParameterCount() throws SQLException {
            if (this.parameterList == null) {
                return 0;
            }
            return this.parameterList.size();
        }
        
        @Override
        public int getParameterMode(final int arg0) throws SQLException {
            this.checkBounds(arg0);
            return this.getParameter(arg0 - 1).inOutModifier;
        }
        
        @Override
        public int getParameterType(final int arg0) throws SQLException {
            this.checkBounds(arg0);
            return this.getParameter(arg0 - 1).jdbcType;
        }
        
        @Override
        public String getParameterTypeName(final int arg0) throws SQLException {
            this.checkBounds(arg0);
            return this.getParameter(arg0 - 1).typeName;
        }
        
        @Override
        public int getPrecision(final int arg0) throws SQLException {
            this.checkBounds(arg0);
            return this.getParameter(arg0 - 1).precision;
        }
        
        @Override
        public int getScale(final int arg0) throws SQLException {
            this.checkBounds(arg0);
            return this.getParameter(arg0 - 1).scale;
        }
        
        @Override
        public int isNullable(final int arg0) throws SQLException {
            this.checkBounds(arg0);
            return this.getParameter(arg0 - 1).nullability;
        }
        
        @Override
        public boolean isSigned(final int arg0) throws SQLException {
            this.checkBounds(arg0);
            return false;
        }
        
        Iterator<com.alipay.oceanbase.jdbc.CallableStatement.CallableStatementParam> iterator() {
            return this.parameterList.iterator();
        }
        
        int numberOfParameters() {
            return this.numParameters;
        }
        
        @Override
        public boolean isWrapperFor(final Class<?> iface) throws SQLException {
            ServerCallableStatement.this.checkClosed();
            return iface.isInstance(this);
        }
        
        @Override
        public <T> T unwrap(final Class<T> iface) throws SQLException {
            try {
                return iface.cast(this);
            }
            catch (ClassCastException cce) {
                throw SQLError.createSQLException("Unable to unwrap to " + iface.toString(), "S1009", ServerCallableStatement.this.getExceptionInterceptor());
            }
        }
    }
}
