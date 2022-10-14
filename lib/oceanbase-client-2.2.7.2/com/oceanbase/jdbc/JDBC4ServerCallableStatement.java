// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import com.oceanbase.jdbc.internal.com.send.parameters.NullParameter;
import java.sql.ResultSet;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import com.oceanbase.jdbc.internal.ColumnType;
import com.oceanbase.jdbc.internal.util.ParsedCallParameters;
import java.util.List;
import com.oceanbase.jdbc.internal.util.Utils;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ArrayList;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.internal.com.read.resultset.SelectResultSet;
import com.oceanbase.jdbc.internal.util.dao.CloneableCallableStatement;

public class JDBC4ServerCallableStatement extends CallableProcedureStatement implements CloneableCallableStatement
{
    private SelectResultSet outputResultSet;
    private boolean isObFunction;
    private final String PARAMETER_NAMESPACE_PREFIX = "@com_mysql_jdbc_outparam_";
    
    public JDBC4ServerCallableStatement(final boolean isObFunction, final String query, final OceanBaseConnection connection, final String procedureName, final String database, final String arguments, final int resultSetType, final int resultSetConcurrency, final ExceptionFactory exceptionFactory) throws SQLException {
        super(isObFunction, connection, query, resultSetType, resultSetConcurrency, exceptionFactory);
        this.outputResultSet = null;
        this.isObFunction = false;
        this.isObFunction = isObFunction;
        this.arguments = arguments;
        if (!this.connection.getProtocol().isOracleMode()) {
            this.parameterMetadata = new CallableParameterMetaData(connection, database, procedureName, false);
        }
        else {
            this.parameterMetadata = new OceanBaseCallableParameterMetaData(connection, database, procedureName, isObFunction);
            this.parameterMetadata = new OceanBaseCallableParameterMetaData(connection, database, procedureName, isObFunction);
            if (this.protocol.supportStmtPrepareExecute()) {
                this.parameterMetadata.params = new ArrayList<CallParameter>();
                this.parameterMetadata.mapNameToParamter = new HashMap<String, CallParameter>();
                this.params = new ArrayList<CallParameter>(this.parameterCount);
                return;
            }
        }
        this.setParamsAccordingToSetArguments();
        this.setParametersVariables();
    }
    
    public JDBC4ServerCallableStatement(final boolean isObFunction, final String query, final OceanBaseConnection connection, final String procedureName, final String database, final String arguments, final int resultSetType, final int resultSetConcurrency, final ExceptionFactory exceptionFactory, final boolean isAnonymousBlock) throws SQLException {
        super(isObFunction, connection, query, resultSetType, resultSetConcurrency, exceptionFactory);
        this.outputResultSet = null;
        this.isObFunction = false;
        this.arguments = arguments;
        this.isObFunction = isObFunction;
        (this.parameterMetadata = new OceanBaseCallableParameterMetaData(connection, database, procedureName, isObFunction)).generateMetadataFromPrepareResultSet(this.serverPrepareResult);
        this.setParamsAccordingToSetArguments();
        this.setParametersVariables();
    }
    
    private void setParamsAccordingToSetArguments() throws SQLException {
        if (this.parameterCount == -1) {
            if (this.arguments == null || this.arguments.equals("")) {
                this.parameterCount = 0;
            }
            else {
                final List<ParsedCallParameters> paramList = Utils.argumentsSplit(this.arguments, ",", "'\"", "'\"");
                this.parameterCount = paramList.size();
            }
            if (this.isObFunction) {
                ++this.parameterCount;
            }
        }
        this.params = new ArrayList<CallParameter>(this.parameterCount);
        for (int index = 0; index < this.parameterCount; ++index) {
            this.params.add(new CallParameter());
        }
    }
    
    private void setInputOutputParameterMap() {
        if (this.outputParameterMapper == null) {
            this.outputParameterMapper = new int[this.params.size()];
            int currentOutputMapper = 1;
            for (int index = 0; index < this.params.size(); ++index) {
                this.outputParameterMapper[index] = (this.params.get(index).isOutput() ? currentOutputMapper++ : -1);
            }
        }
    }
    
    @Override
    protected SelectResultSet getOutputResult() throws SQLException {
        if (this.outputResultSet == null) {
            if (this.isObFunction) {
                this.outputResultSet = this.results.getResultSet();
                if (this.outputResultSet != null) {
                    this.outputResultSet.next();
                    return this.outputResultSet;
                }
            }
            if (this.fetchSize != 0) {
                this.results.loadFully(false, this.protocol);
                this.outputResultSet = this.results.getCallableResultSet();
                if (this.outputResultSet != null) {
                    this.outputResultSet.next();
                    return this.outputResultSet;
                }
            }
            throw new SQLException("No output result.");
        }
        return this.outputResultSet;
    }
    
    @Override
    public JDBC4ServerCallableStatement clone(final OceanBaseConnection connection) throws CloneNotSupportedException {
        final JDBC4ServerCallableStatement clone = (JDBC4ServerCallableStatement)super.clone(connection);
        clone.outputResultSet = null;
        return clone;
    }
    
    private void retrieveOutputResult() throws SQLException {
        this.outputResultSet = this.results.getCallableResultSet();
        if (this.outputResultSet != null) {
            this.outputResultSet.next();
            final SelectResultSet selectResultSet = this.outputResultSet;
            selectResultSet.row.complexEndPos = selectResultSet.complexEndPos;
            final ColumnDefinition[] ci = selectResultSet.getColumnsInformation();
            for (int i = 1; i <= ci.length; ++i) {
                final ColumnType columnTypes = ci[i - 1].getColumnType();
                if (columnTypes == ColumnType.COMPLEX || columnTypes == ColumnType.ARRAY || columnTypes == ColumnType.STRUCT) {
                    selectResultSet.getComplex(i);
                }
                else if (columnTypes == ColumnType.CURSOR) {
                    selectResultSet.getComplexCursor(i);
                }
            }
        }
    }
    
    @Override
    public void setParameter(final int parameterIndex, final ParameterHolder holder) throws SQLException {
        try {
            this.params.get(parameterIndex - 1).setInput(true);
        }
        catch (IndexOutOfBoundsException e) {
            if (!this.protocol.supportStmtPrepareExecute() || 1 > parameterIndex || parameterIndex > this.parameterCount) {
                throw e;
            }
            final CallParameter paramInfoToAdd = new CallParameter(null, parameterIndex - 1, true, false, holder.getColumnType().getSqlType(), holder.getColumnType().getTypeName(), 0, 0, 1, 1);
            this.params.add(paramInfoToAdd);
        }
        super.setParameter(parameterIndex, holder);
    }
    
    private String mangleParameterName(final String origParameterName) {
        if (origParameterName == null) {
            return null;
        }
        int offset = 0;
        if (origParameterName.length() > 0 && origParameterName.charAt(0) == '@') {
            offset = 1;
        }
        final StringBuilder paramNameBuf = new StringBuilder("@com_mysql_jdbc_outparam_".length() + origParameterName.length());
        paramNameBuf.append("@com_mysql_jdbc_outparam_");
        paramNameBuf.append(origParameterName.substring(offset));
        return paramNameBuf.toString();
    }
    
    private int setInOutParamsOnServer() throws SQLException {
        this.parameterMetadata.readMetadataFromDbIfRequired(this.originalSql, this.arguments, this.isObFunction);
        this.params = this.parameterMetadata.params;
        final int size = this.params.size();
        final List<String> intoutParams = new ArrayList<String>(this.params.size());
        this.setInputOutputParameterMap();
        this.validAllParameters();
        for (int i = 0; i < size; ++i) {
            final String paramName = this.parameterMetadata.params.get(i).getName();
            if (paramName == null) {
                throw new SQLException("param[" + i + "] name is null.");
            }
            final String inOutParameterName = this.mangleParameterName(paramName);
            intoutParams.add(inOutParameterName);
        }
        for (int i = 0; i < size; ++i) {
            final CallParameter inParamInfo = this.params.get(i);
            if (inParamInfo.isInput() && inParamInfo.isOutput()) {
                final String inOutParameterName = intoutParams.get(i);
                final StringBuilder queryBuf = new StringBuilder(4 + inOutParameterName.length() + 1 + 1);
                final ParameterHolder holder = this.currentParameterHolder.get(i);
                queryBuf.append("SET ");
                queryBuf.append(inOutParameterName);
                queryBuf.append("=");
                if (holder.toString() == "<null>") {
                    queryBuf.append("null");
                }
                else {
                    queryBuf.append(holder.toString());
                }
                final String query = queryBuf.toString().replaceAll("\\`(\\w+)\\`", "$1");
                this.connection.createStatement().execute(query);
            }
        }
        boolean afterFirst = false;
        final int index = this.originalSql.indexOf(63);
        final StringBuilder querySB = new StringBuilder();
        String sqlString = this.originalSql;
        if (index == -1) {
            querySB.append(this.originalSql);
        }
        else {
            for (int j = 0; j < size; ++j) {
                final CallParameter inParamInfo2 = this.params.get(j);
                if (inParamInfo2.isOutput()) {
                    String inOutParameterName2 = intoutParams.get(j);
                    inOutParameterName2 = inOutParameterName2.replaceAll("\\`(\\w+)\\`", "$1");
                    sqlString = sqlString.replaceFirst("\\?", inOutParameterName2);
                }
                else {
                    final ParameterHolder holder2 = this.currentParameterHolder.get(j);
                    if (holder2.toString() == "<null>") {
                        sqlString = sqlString.replaceFirst("\\?", "null");
                    }
                    else {
                        sqlString = sqlString.replaceFirst("\\?", holder2.toString());
                    }
                }
            }
        }
        final int r = this.connection.createStatement().executeUpdate(sqlString);
        final StringBuilder selectSB = new StringBuilder();
        selectSB.append("SELECT ");
        afterFirst = false;
        boolean getResult = false;
        for (int k = 0; k < size; ++k) {
            final CallParameter inParamInfo3 = this.params.get(k);
            if (inParamInfo3.isOutput()) {
                if (afterFirst) {
                    selectSB.append(",");
                }
                String inOutParameterName3 = intoutParams.get(k);
                inOutParameterName3 = inOutParameterName3.replaceAll("\\`(\\w+)\\`", "$1");
                selectSB.append(inOutParameterName3);
                afterFirst = true;
                getResult = true;
            }
        }
        if (getResult) {
            final ResultSet resultSet = this.connection.createStatement().executeQuery(selectSB.toString());
            resultSet.next();
            this.outputResultSet = (SelectResultSet)resultSet;
        }
        return r;
    }
    
    @Override
    public boolean execute() throws SQLException {
        this.connection.lock.lock();
        try {
            if (!this.protocol.isOracleMode()) {
                this.setInOutParamsOnServer();
                return true;
            }
            this.validAllParameters();
            super.execute();
            this.retrieveOutputResult();
            return this.results != null && this.results.getResultSet() != null;
        }
        finally {
            this.connection.lock.unlock();
        }
    }
    
    private void validAllParameters() throws SQLException {
        this.setInputOutputParameterMap();
        for (int index = 0; index < this.params.size(); ++index) {
            if (!this.params.get(index).isInput()) {
                super.setParameter(index + 1, new NullParameter());
            }
        }
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        if (this.hasInOutParameters) {
            throw new SQLException("executeBatch not permit for procedure with output parameter");
        }
        if (this.protocol.isOracleMode() || this.isObFunction) {
            return super.executeBatch();
        }
        final int querySize = this.queryParameters.size();
        if (querySize == 0) {
            return new int[0];
        }
        return this.executeBatchInternal(querySize);
    }
    
    private int[] executeBatchInternal(final int querySize) throws SQLException {
        final int[] rows = new int[querySize];
        for (int i = 0; i < querySize; ++i) {
            final ParameterHolder[] parameterHolder = this.queryParameters.get(i);
            this.currentParameterHolder.clear();
            for (int j = 0; j < parameterHolder.length; ++j) {
                this.currentParameterHolder.put(j, parameterHolder[j]);
            }
            rows[i] = this.setInOutParamsOnServer();
        }
        return rows;
    }
}
