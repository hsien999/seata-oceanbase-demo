// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.util.Arrays;
import com.oceanbase.jdbc.internal.com.send.parameters.DefaultParameter;
import java.sql.SQLXML;
import java.sql.NClob;
import java.sql.RowId;
import java.sql.Array;
import java.sql.Ref;
import com.oceanbase.jdbc.internal.com.send.parameters.LongParameter;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.Instant;
import java.time.LocalDateTime;
import java.sql.Clob;
import java.sql.Blob;
import com.oceanbase.jdbc.internal.com.send.parameters.ZonedDateTimeParameter;
import java.time.ZonedDateTime;
import com.oceanbase.jdbc.internal.com.send.parameters.OffsetTimeParameter;
import java.time.OffsetTime;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.internal.com.send.parameters.ReaderParameter;
import java.io.Reader;
import com.oceanbase.jdbc.internal.com.send.parameters.StreamParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.OBStreamParameter;
import java.io.InputStream;
import com.oceanbase.jdbc.internal.com.send.parameters.TimeParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.TimestampParameter;
import java.sql.Timestamp;
import java.sql.Time;
import com.oceanbase.jdbc.internal.com.send.parameters.DateParameter;
import java.util.TimeZone;
import java.sql.Date;
import com.oceanbase.jdbc.internal.com.send.parameters.ByteArrayParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.OBByteArrayParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.StringParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.BigDecimalParameter;
import com.oceanbase.jdbc.internal.ColumnType;
import java.math.BigDecimal;
import com.oceanbase.jdbc.internal.com.send.parameters.DoubleParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.FloatParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.IntParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.ShortParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.ByteParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.NullParameter;
import java.sql.SQLDataException;
import java.sql.ResultSet;
import java.sql.Statement;
import com.oceanbase.jdbc.internal.com.read.resultset.UpdatableColumnDefinition;
import java.sql.SQLException;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import java.sql.PreparedStatement;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import com.oceanbase.jdbc.internal.com.read.resultset.SelectResultSet;

public class JDBC4UpdatableResultSet extends SelectResultSet
{
    private static final int STATE_STANDARD = 0;
    private static final int STATE_UPDATE = 1;
    private static final int STATE_UPDATED = 2;
    private static final int STATE_INSERT = 3;
    private String database;
    private String table;
    private boolean canBeUpdate;
    private boolean canBeInserted;
    private boolean canBeRefresh;
    private int notInsertRowPointer;
    private String exceptionUpdateMsg;
    private String exceptionInsertMsg;
    private int state;
    private ParameterHolder[] parameterHolders;
    protected OceanBaseConnection connection;
    private PreparedStatement refreshPreparedStatement;
    private ClientSidePreparedStatement insertPreparedStatement;
    private ClientSidePreparedStatement deletePreparedStatement;
    
    public JDBC4UpdatableResultSet(final ColumnDefinition[] columnsInformation, final Results results, final Protocol protocol, final PacketInputStream reader, final boolean callableResult, final boolean eofDeprecated, final boolean isPsOutParamter) throws IOException, SQLException {
        super(columnsInformation, results, protocol, reader, callableResult, eofDeprecated, isPsOutParamter);
        this.state = 0;
        this.refreshPreparedStatement = null;
        this.insertPreparedStatement = null;
        this.deletePreparedStatement = null;
        this.checkIfUpdatable(results);
        this.parameterHolders = new ParameterHolder[this.columnInformationLength];
    }
    
    @Override
    public int getConcurrency() {
        return 1008;
    }
    
    private void checkIfUpdatable(final Results results) throws SQLException {
        this.database = null;
        this.table = null;
        this.canBeUpdate = true;
        this.canBeInserted = true;
        this.canBeRefresh = false;
        for (final ColumnDefinition columnDefinition : this.columnsInformation) {
            if (columnDefinition.getDatabase() == null || columnDefinition.getDatabase().isEmpty()) {
                this.cannotUpdateInsertRow("The result-set contains fields without without any database information");
                return;
            }
            if (this.database != null && !this.database.equals(columnDefinition.getDatabase())) {
                this.cannotUpdateInsertRow("The result-set contains more than one database");
                return;
            }
            this.database = columnDefinition.getDatabase();
            if (columnDefinition.getOriginalTable() == null || columnDefinition.getOriginalTable().isEmpty()) {
                this.cannotUpdateInsertRow("The result-set contains fields without without any table information");
                return;
            }
            if (this.table != null && !this.table.equals(columnDefinition.getOriginalTable())) {
                this.cannotUpdateInsertRow("The result-set contains fields on different tables");
                return;
            }
            this.table = columnDefinition.getOriginalTable();
        }
        if (this.database == null) {
            this.cannotUpdateInsertRow("The result-set does not contain any table information");
            return;
        }
        if (this.table == null) {
            this.cannotUpdateInsertRow("The result-set does not contain any table information");
            return;
        }
        if (this.canBeUpdate) {
            if (results.getStatement() == null || results.getStatement().getConnection() == null) {
                throw new SQLException("abnormal error : connection is null");
            }
            this.connection = results.getStatement().getConnection();
            final Statement stmt = this.connection.createStatement(1004, 1007);
            final ResultSet rs = stmt.executeQuery("SHOW COLUMNS FROM `" + this.database + "`.`" + this.table + "`");
            final UpdatableColumnDefinition[] updatableColumns = new UpdatableColumnDefinition[this.columnInformationLength];
            boolean primaryFound = false;
            while (rs.next()) {
                final String fieldName = rs.getString("Field");
                final boolean canBeNull = "YES".equals(rs.getString("Null"));
                final boolean hasDefault = rs.getString("Default") == null;
                final String extra = rs.getString("Extra");
                final boolean generated = extra != null && !extra.isEmpty();
                final boolean autoIncrement = extra != null && "auto_increment".equals(extra);
                final boolean primary = "PRI".equals(rs.getString("Key"));
                boolean found = false;
                for (int index = 0; index < this.columnInformationLength; ++index) {
                    final ColumnDefinition columnDefinition2 = this.columnsInformation[index];
                    if (fieldName.equals(columnDefinition2.getOriginalName())) {
                        updatableColumns[index] = new UpdatableColumnDefinition(columnDefinition2, canBeNull, hasDefault, generated, primary, autoIncrement);
                        found = true;
                    }
                }
                if (primary) {
                    primaryFound = true;
                }
                if (!found) {
                    if (primary) {
                        this.cannotUpdateInsertRow("Primary key field `" + fieldName + "` is not in result-set");
                        return;
                    }
                    if (canBeNull || hasDefault || generated) {
                        continue;
                    }
                    this.cannotInsertRow("Field `" + fieldName + "` is not present in query returning " + "fields and cannot be null");
                }
            }
            if (!primaryFound) {
                this.cannotUpdateInsertRow("Table `" + this.database + "`.`" + this.table + "` has no primary key");
                return;
            }
            this.canBeRefresh = true;
            boolean ensureAllColumnHaveMeta = true;
            for (int index2 = 0; index2 < this.columnInformationLength; ++index2) {
                if (updatableColumns[index2] == null) {
                    this.cannotUpdateInsertRow("Metadata information not available for table `" + this.database + "`.`" + this.table + "`, field `" + this.columnsInformation[index2].getOriginalName() + "`");
                    ensureAllColumnHaveMeta = false;
                }
            }
            if (ensureAllColumnHaveMeta) {
                this.columnsInformation = updatableColumns;
            }
        }
    }
    
    private UpdatableColumnDefinition[] getUpdatableColumns() {
        return (UpdatableColumnDefinition[])this.columnsInformation;
    }
    
    private void cannotUpdateInsertRow(final String reason) {
        if (this.exceptionUpdateMsg == null) {
            this.exceptionUpdateMsg = "ResultSet cannot be updated. " + reason;
        }
        if (this.exceptionInsertMsg == null) {
            this.exceptionInsertMsg = "No row can be inserted. " + reason;
        }
        this.canBeUpdate = false;
        this.canBeInserted = false;
    }
    
    private void cannotInsertRow(final String reason) {
        if (this.exceptionInsertMsg == null) {
            this.exceptionInsertMsg = "No row can be inserted. " + reason;
        }
        this.canBeInserted = false;
    }
    
    private void checkUpdatable(final int position) throws SQLException {
        if (position <= 0 || position > this.columnInformationLength) {
            throw new SQLDataException("No such column: " + position, "22023");
        }
        if (this.state == 0) {
            this.state = 1;
        }
        if (this.state == 1) {
            if (this.getRowPointer() < 0) {
                throw new SQLDataException("Current position is before the first row", "22023");
            }
            if (this.getRowPointer() >= this.getDataSize()) {
                throw new SQLDataException("Current position is after the last row", "22023");
            }
            if (!this.canBeUpdate) {
                throw new SQLException(this.exceptionUpdateMsg);
            }
        }
        if (this.state == 3 && !this.canBeInserted) {
            throw new SQLException(this.exceptionInsertMsg);
        }
    }
    
    @Override
    public void updateNull(final int columnIndex) throws SQLException {
        this.checkUpdatable(columnIndex);
        this.parameterHolders[columnIndex - 1] = new NullParameter();
    }
    
    @Override
    public void updateNull(final String columnLabel) throws SQLException {
        this.updateNull(this.findColumn(columnLabel));
    }
    
    @Override
    public void updateBoolean(final int columnIndex, final boolean bool) throws SQLException {
        this.checkUpdatable(columnIndex);
        this.parameterHolders[columnIndex - 1] = new ByteParameter((byte)(bool ? 1 : 0));
    }
    
    @Override
    public void updateBoolean(final String columnLabel, final boolean value) throws SQLException {
        this.updateBoolean(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateByte(final int columnIndex, final byte value) throws SQLException {
        this.checkUpdatable(columnIndex);
        this.parameterHolders[columnIndex - 1] = new ByteParameter(value);
    }
    
    @Override
    public void updateByte(final String columnLabel, final byte value) throws SQLException {
        this.updateByte(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateShort(final int columnIndex, final short value) throws SQLException {
        this.checkUpdatable(columnIndex);
        this.parameterHolders[columnIndex - 1] = new ShortParameter(value);
    }
    
    @Override
    public void updateShort(final String columnLabel, final short value) throws SQLException {
        this.updateShort(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateInt(final int columnIndex, final int value) throws SQLException {
        this.checkUpdatable(columnIndex);
        this.parameterHolders[columnIndex - 1] = new IntParameter(value);
    }
    
    @Override
    public void updateInt(final String columnLabel, final int value) throws SQLException {
        this.updateInt(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateFloat(final int columnIndex, final float value) throws SQLException {
        this.checkUpdatable(columnIndex);
        this.parameterHolders[columnIndex - 1] = new FloatParameter(value);
    }
    
    @Override
    public void updateFloat(final String columnLabel, final float value) throws SQLException {
        this.updateFloat(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateDouble(final int columnIndex, final double value) throws SQLException {
        this.checkUpdatable(columnIndex);
        this.parameterHolders[columnIndex - 1] = new DoubleParameter(value);
    }
    
    @Override
    public void updateDouble(final String columnLabel, final double value) throws SQLException {
        this.updateDouble(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateBigDecimal(final int columnIndex, final BigDecimal value) throws SQLException {
        this.checkUpdatable(columnIndex);
        if (value == null) {
            this.parameterHolders[columnIndex - 1] = new NullParameter(ColumnType.DECIMAL);
            return;
        }
        this.parameterHolders[columnIndex - 1] = new BigDecimalParameter(value);
    }
    
    @Override
    public void updateBigDecimal(final String columnLabel, final BigDecimal value) throws SQLException {
        this.updateBigDecimal(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateString(final int columnIndex, final String value) throws SQLException {
        this.checkUpdatable(columnIndex);
        if (value == null) {
            this.parameterHolders[columnIndex - 1] = new NullParameter(ColumnType.STRING);
            return;
        }
        this.parameterHolders[columnIndex - 1] = new StringParameter(value, this.noBackslashEscapes, this.getProtocol().getOptions().characterEncoding);
    }
    
    @Override
    public void updateString(final String columnLabel, final String value) throws SQLException {
        this.updateString(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateBytes(final int columnIndex, final byte[] value) throws SQLException {
        this.checkUpdatable(columnIndex);
        if (value == null) {
            this.parameterHolders[columnIndex - 1] = new NullParameter(ColumnType.BLOB);
            return;
        }
        if (this.getProtocol().isOracleMode()) {
            this.parameterHolders[columnIndex - 1] = new OBByteArrayParameter(value, this.noBackslashEscapes);
        }
        else {
            this.parameterHolders[columnIndex - 1] = new ByteArrayParameter(value, this.noBackslashEscapes);
        }
    }
    
    @Override
    public void updateBytes(final String columnLabel, final byte[] value) throws SQLException {
        this.updateBytes(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateDate(final int columnIndex, final Date date) throws SQLException {
        this.checkUpdatable(columnIndex);
        if (date == null) {
            this.parameterHolders[columnIndex - 1] = new NullParameter(ColumnType.DATE);
            return;
        }
        this.parameterHolders[columnIndex - 1] = new DateParameter(date, TimeZone.getDefault(), this.options);
    }
    
    @Override
    public void updateDate(final String columnLabel, final Date value) throws SQLException {
        this.updateDate(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateTime(final int columnIndex, final Time time) throws SQLException {
        this.checkUpdatable(columnIndex);
        if (time == null) {
            this.parameterHolders[columnIndex - 1] = new NullParameter(ColumnType.TIME);
            return;
        }
        if (this.connection.getProtocol().isOracleMode()) {
            final Timestamp ts = new Timestamp(time.getTime());
            final TimeZone tz = this.connection.getProtocol().getTimeZone();
            this.parameterHolders[columnIndex - 1] = new TimestampParameter(ts, tz, this.options.useFractionalSeconds);
        }
        else {
            this.parameterHolders[columnIndex - 1] = new TimeParameter(time, TimeZone.getDefault(), this.options.useFractionalSeconds);
        }
    }
    
    @Override
    public void updateTime(final String columnLabel, final Time value) throws SQLException {
        this.updateTime(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateTimestamp(final int columnIndex, final Timestamp timeStamp) throws SQLException {
        this.checkUpdatable(columnIndex);
        if (timeStamp == null) {
            this.parameterHolders[columnIndex - 1] = new NullParameter(ColumnType.DATETIME);
            return;
        }
        this.parameterHolders[columnIndex - 1] = new TimestampParameter(timeStamp, this.timeZone, this.options.useFractionalSeconds);
    }
    
    @Override
    public void updateTimestamp(final String columnLabel, final Timestamp value) throws SQLException {
        this.updateTimestamp(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream inputStream) throws SQLException {
        this.updateAsciiStream(columnIndex, inputStream, Long.MAX_VALUE);
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream inputStream) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnLabel), inputStream);
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream inputStream, final int length) throws SQLException {
        this.updateAsciiStream(columnIndex, inputStream, (long)length);
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream inputStream, final int length) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnLabel), inputStream, length);
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        this.checkUpdatable(columnIndex);
        if (inputStream == null) {
            this.parameterHolders[columnIndex - 1] = new NullParameter(ColumnType.BLOB);
            return;
        }
        if (this.getProtocol().isOracleMode()) {
            this.parameterHolders[columnIndex - 1] = new OBStreamParameter(inputStream, length, this.noBackslashEscapes);
        }
        else {
            this.parameterHolders[columnIndex - 1] = new StreamParameter(inputStream, length, this.noBackslashEscapes);
        }
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
        this.updateAsciiStream(this.findColumn(columnLabel), inputStream, length);
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream inputStream, final int length) throws SQLException {
        this.updateBinaryStream(columnIndex, inputStream, (long)length);
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        this.checkUpdatable(columnIndex);
        if (inputStream == null) {
            this.parameterHolders[columnIndex - 1] = new NullParameter(ColumnType.BLOB);
            return;
        }
        if (this.getProtocol().isOracleMode()) {
            this.parameterHolders[columnIndex - 1] = new OBStreamParameter(inputStream, length, this.noBackslashEscapes);
        }
        else {
            this.parameterHolders[columnIndex - 1] = new StreamParameter(inputStream, length, this.noBackslashEscapes);
        }
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream inputStream, final int length) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnLabel), inputStream, (long)length);
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnLabel), inputStream, length);
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream inputStream) throws SQLException {
        this.updateBinaryStream(columnIndex, inputStream, Long.MAX_VALUE);
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream inputStream) throws SQLException {
        this.updateBinaryStream(this.findColumn(columnLabel), inputStream);
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader reader, final int length) throws SQLException {
        this.updateCharacterStream(columnIndex, reader, (long)length);
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader value) throws SQLException {
        this.updateCharacterStream(columnIndex, value, Long.MAX_VALUE);
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader, final int length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnLabel), reader, (long)length);
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader value, final long length) throws SQLException {
        this.checkUpdatable(columnIndex);
        if (value == null) {
            this.parameterHolders[columnIndex - 1] = new NullParameter(ColumnType.BLOB);
            return;
        }
        this.parameterHolders[columnIndex - 1] = new ReaderParameter(value, length, this.noBackslashEscapes);
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnLabel), reader, length);
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnLabel), reader, Long.MAX_VALUE);
    }
    
    private void updateInternalObject(final int parameterIndex, final Object obj, final int targetSqlType, final long scaleOrLength) throws SQLException {
        switch (targetSqlType) {
            case -8:
            case 70:
            case 2000:
            case 2002:
            case 2003:
            case 2006:
            case 2009: {
                throw ExceptionFactory.INSTANCE.notSupported("Type not supported");
            }
            default: {
                if (obj == null) {
                    this.updateNull(parameterIndex);
                }
                else if (obj instanceof String) {
                    if (targetSqlType == 2004) {
                        throw ExceptionFactory.INSTANCE.create("Cannot convert a String to a Blob");
                    }
                    final String str = (String)obj;
                    try {
                        switch (targetSqlType) {
                            case -7:
                            case 16: {
                                this.updateBoolean(parameterIndex, !"false".equalsIgnoreCase(str) && !"0".equals(str));
                                break;
                            }
                            case -6: {
                                this.updateByte(parameterIndex, Byte.parseByte(str));
                                break;
                            }
                            case 5: {
                                this.updateShort(parameterIndex, Short.parseShort(str));
                                break;
                            }
                            case 4: {
                                this.updateInt(parameterIndex, Integer.parseInt(str));
                                break;
                            }
                            case 6:
                            case 8: {
                                this.updateDouble(parameterIndex, Double.valueOf(str));
                                break;
                            }
                            case 7: {
                                this.updateFloat(parameterIndex, Float.valueOf(str));
                                break;
                            }
                            case -5: {
                                this.updateLong(parameterIndex, Long.valueOf(str));
                                break;
                            }
                            case 2:
                            case 3: {
                                this.updateBigDecimal(parameterIndex, new BigDecimal(str));
                                break;
                            }
                            case -16:
                            case -15:
                            case -9:
                            case -1:
                            case 1:
                            case 12:
                            case 2005:
                            case 2011: {
                                this.updateString(parameterIndex, str);
                                break;
                            }
                            case 93: {
                                if (str.startsWith("0000-00-00")) {
                                    this.updateTimestamp(parameterIndex, null);
                                    break;
                                }
                                this.updateTimestamp(parameterIndex, Timestamp.valueOf(str));
                                break;
                            }
                            case 92: {
                                this.updateTime(parameterIndex, Time.valueOf((String)obj));
                                break;
                            }
                            case 2013: {
                                this.parameterHolders[parameterIndex - 1] = new OffsetTimeParameter(OffsetTime.parse(str), this.timeZone.toZoneId(), this.options.useFractionalSeconds, this.options);
                                break;
                            }
                            case 2014: {
                                this.parameterHolders[parameterIndex - 1] = new ZonedDateTimeParameter(ZonedDateTime.parse(str, BasePrepareStatement.SPEC_ISO_ZONED_DATE_TIME), this.timeZone.toZoneId(), this.options.useFractionalSeconds, this.options);
                                break;
                            }
                            default: {
                                throw ExceptionFactory.INSTANCE.create("Could not convert [" + str + "] to " + targetSqlType);
                            }
                        }
                    }
                    catch (IllegalArgumentException e) {
                        throw ExceptionFactory.INSTANCE.create("Could not convert [" + str + "] to " + targetSqlType, e);
                    }
                }
                else if (obj instanceof Number) {
                    final Number bd = (Number)obj;
                    switch (targetSqlType) {
                        case -6: {
                            this.updateByte(parameterIndex, bd.byteValue());
                            break;
                        }
                        case 5: {
                            this.updateShort(parameterIndex, bd.shortValue());
                            break;
                        }
                        case 4: {
                            this.updateInt(parameterIndex, bd.intValue());
                            break;
                        }
                        case -5: {
                            this.updateLong(parameterIndex, bd.longValue());
                            break;
                        }
                        case 6:
                        case 8: {
                            this.updateDouble(parameterIndex, bd.doubleValue());
                            break;
                        }
                        case 7: {
                            this.updateFloat(parameterIndex, bd.floatValue());
                            break;
                        }
                        case 2:
                        case 3: {
                            if (obj instanceof BigDecimal) {
                                this.updateBigDecimal(parameterIndex, (BigDecimal)obj);
                                break;
                            }
                            if (obj instanceof Double || obj instanceof Float) {
                                this.updateDouble(parameterIndex, bd.doubleValue());
                                break;
                            }
                            this.updateLong(parameterIndex, bd.longValue());
                            break;
                        }
                        case -7: {
                            this.updateBoolean(parameterIndex, bd.shortValue() != 0);
                            break;
                        }
                        case 1:
                        case 12: {
                            this.updateString(parameterIndex, bd.toString());
                            break;
                        }
                        default: {
                            throw ExceptionFactory.INSTANCE.create("Could not convert [" + bd + "] to " + targetSqlType);
                        }
                    }
                }
                else if (obj instanceof byte[]) {
                    if (targetSqlType != -2 && targetSqlType != -3 && targetSqlType != -4) {
                        throw ExceptionFactory.INSTANCE.create("Can only convert a byte[] to BINARY, VARBINARY or LONGVARBINARY");
                    }
                    this.updateBytes(parameterIndex, (byte[])obj);
                }
                else if (obj instanceof Time) {
                    this.updateTime(parameterIndex, (Time)obj);
                }
                else if (obj instanceof Timestamp) {
                    this.updateTimestamp(parameterIndex, (Timestamp)obj);
                }
                else if (obj instanceof Date) {
                    this.updateDate(parameterIndex, (Date)obj);
                }
                else if (obj instanceof java.util.Date) {
                    final long timemillis = ((java.util.Date)obj).getTime();
                    if (targetSqlType == 91) {
                        this.updateDate(parameterIndex, new Date(timemillis));
                    }
                    else if (targetSqlType == 92) {
                        this.updateTime(parameterIndex, new Time(timemillis));
                    }
                    else if (targetSqlType == 93) {
                        this.updateTimestamp(parameterIndex, new Timestamp(timemillis));
                    }
                }
                else if (obj instanceof Boolean) {
                    this.updateBoolean(parameterIndex, (boolean)obj);
                }
                else if (obj instanceof Blob) {
                    this.updateBlob(parameterIndex, (Blob)obj);
                }
                else if (obj instanceof Clob) {
                    this.updateClob(parameterIndex, (Clob)obj);
                }
                else if (obj instanceof InputStream) {
                    this.updateBinaryStream(parameterIndex, (InputStream)obj, scaleOrLength);
                }
                else if (obj instanceof Reader) {
                    this.updateCharacterStream(parameterIndex, (Reader)obj, scaleOrLength);
                }
                else if (obj instanceof LocalDateTime) {
                    this.updateTimestamp(parameterIndex, Timestamp.valueOf((LocalDateTime)obj));
                }
                else if (obj instanceof Instant) {
                    this.updateTimestamp(parameterIndex, Timestamp.from((Instant)obj));
                }
                else if (obj instanceof LocalDate) {
                    this.updateDate(parameterIndex, Date.valueOf((LocalDate)obj));
                }
                else if (obj instanceof OffsetDateTime) {
                    this.parameterHolders[parameterIndex - 1] = new ZonedDateTimeParameter(((OffsetDateTime)obj).toZonedDateTime(), this.timeZone.toZoneId(), this.options.useFractionalSeconds, this.options);
                }
                else if (obj instanceof OffsetTime) {
                    this.parameterHolders[parameterIndex - 1] = new OffsetTimeParameter((OffsetTime)obj, this.timeZone.toZoneId(), this.options.useFractionalSeconds, this.options);
                }
                else if (obj instanceof ZonedDateTime) {
                    this.parameterHolders[parameterIndex - 1] = new ZonedDateTimeParameter((ZonedDateTime)obj, this.timeZone.toZoneId(), this.options.useFractionalSeconds, this.options);
                }
                else {
                    if (!(obj instanceof LocalTime)) {
                        throw ExceptionFactory.INSTANCE.create("Could not set parameter in setObject, could not convert: " + obj.getClass() + " to " + targetSqlType);
                    }
                    this.updateTime(parameterIndex, Time.valueOf((LocalTime)obj));
                }
            }
        }
    }
    
    @Override
    public void updateObject(final int columnIndex, final Object value, final int scaleOrLength) throws SQLException {
        this.checkUpdatable(columnIndex);
        this.updateInternalObject(columnIndex, value, this.columnsInformation[columnIndex - 1].getColumnType().getSqlType(), scaleOrLength);
    }
    
    @Override
    public void updateObject(final int columnIndex, final Object value) throws SQLException {
        this.checkUpdatable(columnIndex);
        this.updateInternalObject(columnIndex, value, this.columnsInformation[columnIndex - 1].getColumnType().getSqlType(), Long.MAX_VALUE);
    }
    
    @Override
    public void updateObject(final String columnLabel, final Object value, final int scaleOrLength) throws SQLException {
        this.updateObject(this.findColumn(columnLabel), value, scaleOrLength);
    }
    
    @Override
    public void updateObject(final String columnLabel, final Object value) throws SQLException {
        this.updateObject(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateLong(final int columnIndex, final long value) throws SQLException {
        this.checkUpdatable(columnIndex);
        this.parameterHolders[columnIndex - 1] = new LongParameter(value);
    }
    
    @Override
    public void updateLong(final String columnLabel, final long value) throws SQLException {
        this.updateLong(this.findColumn(columnLabel), value);
    }
    
    @Override
    public void updateRef(final int columnIndex, final Ref ref) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("REF not supported");
    }
    
    @Override
    public void updateRef(final String columnLabel, final Ref ref) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("REF not supported");
    }
    
    @Override
    public void updateBlob(final int columnIndex, final Blob blob) throws SQLException {
        this.checkUpdatable(columnIndex);
        if (blob == null) {
            this.parameterHolders[columnIndex - 1] = new NullParameter(ColumnType.BLOB);
            return;
        }
        if (this.getProtocol().isOracleMode()) {
            this.parameterHolders[columnIndex - 1] = new OBStreamParameter(blob.getBinaryStream(), blob.length(), this.noBackslashEscapes);
        }
        else {
            this.parameterHolders[columnIndex - 1] = new StreamParameter(blob.getBinaryStream(), blob.length(), this.noBackslashEscapes);
        }
    }
    
    @Override
    public void updateBlob(final String columnLabel, final Blob blob) throws SQLException {
        this.updateBlob(this.findColumn(columnLabel), blob);
    }
    
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream) throws SQLException {
        this.updateBlob(columnIndex, inputStream, Long.MAX_VALUE);
    }
    
    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream) throws SQLException {
        this.updateBlob(this.findColumn(columnLabel), inputStream, Long.MAX_VALUE);
    }
    
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        this.checkUpdatable(columnIndex);
        if (inputStream == null) {
            this.parameterHolders[columnIndex - 1] = new NullParameter(ColumnType.BLOB);
            return;
        }
        if (this.getProtocol().isOracleMode()) {
            this.parameterHolders[columnIndex - 1] = new OBStreamParameter(inputStream, length, this.noBackslashEscapes);
        }
        else {
            this.parameterHolders[columnIndex - 1] = new StreamParameter(inputStream, length, this.noBackslashEscapes);
        }
    }
    
    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
        this.updateBlob(this.findColumn(columnLabel), inputStream, length);
    }
    
    @Override
    public void updateClob(final int columnIndex, final Clob clob) throws SQLException {
        this.checkUpdatable(columnIndex);
        if (clob == null) {
            this.parameterHolders[columnIndex - 1] = new NullParameter(ColumnType.BLOB);
            return;
        }
        this.parameterHolders[columnIndex - 1] = new ReaderParameter(clob.getCharacterStream(), clob.length(), this.noBackslashEscapes);
    }
    
    @Override
    public void updateClob(final String columnLabel, final Clob clob) throws SQLException {
        this.updateClob(this.findColumn(columnLabel), clob);
    }
    
    @Override
    public void updateClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        this.updateCharacterStream(columnIndex, reader, length);
    }
    
    @Override
    public void updateClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnLabel), reader, length);
    }
    
    @Override
    public void updateClob(final int columnIndex, final Reader reader) throws SQLException {
        this.updateCharacterStream(columnIndex, reader);
    }
    
    @Override
    public void updateClob(final String columnLabel, final Reader reader) throws SQLException {
        this.updateCharacterStream(this.findColumn(columnLabel), reader);
    }
    
    @Override
    public void updateArray(final int columnIndex, final Array array) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Arrays not supported");
    }
    
    @Override
    public void updateArray(final String columnLabel, final Array array) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Arrays not supported");
    }
    
    @Override
    public void updateRowId(final int columnIndex, final RowId rowId) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("RowIDs not supported");
    }
    
    @Override
    public void updateRowId(final String columnLabel, final RowId rowId) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("RowIDs not supported");
    }
    
    @Override
    public void updateNString(final int columnIndex, final String nstring) throws SQLException {
        this.updateString(columnIndex, nstring);
    }
    
    @Override
    public void updateNString(final String columnLabel, final String nstring) throws SQLException {
        this.updateString(columnLabel, nstring);
    }
    
    @Override
    public void updateNClob(final int columnIndex, final NClob nclob) throws SQLException {
        this.updateClob(columnIndex, nclob);
    }
    
    @Override
    public void updateNClob(final String columnLabel, final NClob nclob) throws SQLException {
        this.updateClob(columnLabel, nclob);
    }
    
    @Override
    public void updateNClob(final int columnIndex, final Reader reader) throws SQLException {
        this.updateClob(columnIndex, reader);
    }
    
    @Override
    public void updateNClob(final String columnLabel, final Reader reader) throws SQLException {
        this.updateClob(columnLabel, reader);
    }
    
    @Override
    public void updateNClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        this.updateClob(columnIndex, reader, length);
    }
    
    @Override
    public void updateNClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        this.updateClob(columnLabel, reader, length);
    }
    
    @Override
    public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("SQlXML not supported");
    }
    
    @Override
    public void updateSQLXML(final String columnLabel, final SQLXML xmlObject) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("SQLXML not supported");
    }
    
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader value, final long length) throws SQLException {
        this.updateCharacterStream(columnIndex, value, length);
    }
    
    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
        this.updateCharacterStream(columnLabel, reader, length);
    }
    
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader reader) throws SQLException {
        this.updateCharacterStream(columnIndex, reader);
    }
    
    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
        this.updateCharacterStream(columnLabel, reader);
    }
    
    @Override
    public void insertRow() throws SQLException {
        if (this.state == 3) {
            if (this.insertPreparedStatement == null) {
                final StringBuilder insertSql = new StringBuilder("INSERT `" + this.database + "`.`" + this.table + "` ( ");
                final StringBuilder valueClause = new StringBuilder();
                for (int pos = 0; pos < this.columnInformationLength; ++pos) {
                    final UpdatableColumnDefinition colInfo = this.getUpdatableColumns()[pos];
                    if (pos != 0) {
                        insertSql.append(",");
                        valueClause.append(", ");
                    }
                    insertSql.append("`").append(colInfo.getOriginalName()).append("`");
                    valueClause.append("?");
                }
                insertSql.append(") VALUES (").append((CharSequence)valueClause).append(")");
                this.insertPreparedStatement = this.connection.clientPrepareStatement(insertSql.toString());
            }
            int fieldsIndex = 0;
            boolean hasGeneratedPrimaryFields = false;
            int generatedSqlType = 0;
            for (int pos2 = 0; pos2 < this.columnInformationLength; ++pos2) {
                final ParameterHolder value = this.parameterHolders[pos2];
                if (value != null) {
                    this.insertPreparedStatement.setParameter(fieldsIndex++ + 1, value);
                }
                else {
                    final UpdatableColumnDefinition colInfo2 = this.getUpdatableColumns()[pos2];
                    if (colInfo2.isPrimary() && colInfo2.isAutoIncrement()) {
                        hasGeneratedPrimaryFields = true;
                        generatedSqlType = colInfo2.getColumnType().getSqlType();
                    }
                    this.insertPreparedStatement.setParameter(fieldsIndex++ + 1, new DefaultParameter());
                }
            }
            this.insertPreparedStatement.execute();
            if (hasGeneratedPrimaryFields) {
                final ResultSet rsKey = this.insertPreparedStatement.getGeneratedKeys();
                if (rsKey.next()) {
                    this.prepareRefreshStmt();
                    this.refreshPreparedStatement.setObject(1, rsKey.getObject(1), generatedSqlType);
                    final SelectResultSet rs = (SelectResultSet)this.refreshPreparedStatement.executeQuery();
                    if (rs.next()) {
                        this.addRowData(rs.getCurrentRowData());
                    }
                }
            }
            else {
                this.addRowData(this.refreshRawData());
            }
            Arrays.fill(this.parameterHolders, null);
        }
    }
    
    @Override
    public void updateRow() throws SQLException {
        if (this.state == 3) {
            throw new SQLException("Cannot call updateRow() when inserting a new row");
        }
        if (this.state == 1) {
            final StringBuilder updateSql = new StringBuilder("UPDATE `" + this.database + "`.`" + this.table + "` SET ");
            final StringBuilder whereClause = new StringBuilder(" WHERE ");
            boolean firstUpdate = true;
            boolean firstPrimary = true;
            int fieldsToUpdate = 0;
            for (int pos = 0; pos < this.columnInformationLength; ++pos) {
                final UpdatableColumnDefinition colInfo = this.getUpdatableColumns()[pos];
                final ParameterHolder value = this.parameterHolders[pos];
                if (colInfo.isPrimary()) {
                    if (!firstPrimary) {
                        whereClause.append("AND ");
                    }
                    firstPrimary = false;
                    whereClause.append("`").append(colInfo.getOriginalName()).append("` = ? ");
                }
                if (value != null) {
                    if (!firstUpdate) {
                        updateSql.append(",");
                    }
                    firstUpdate = false;
                    ++fieldsToUpdate;
                    updateSql.append("`").append(colInfo.getOriginalName()).append("` = ? ");
                }
            }
            updateSql.append(whereClause.toString());
            final ClientSidePreparedStatement preparedStatement = this.connection.clientPrepareStatement(updateSql.toString());
            int fieldsIndex = 0;
            int fieldsPrimaryIndex = 0;
            for (int pos2 = 0; pos2 < this.columnInformationLength; ++pos2) {
                final UpdatableColumnDefinition colInfo2 = this.getUpdatableColumns()[pos2];
                final ParameterHolder value2 = this.parameterHolders[pos2];
                if (value2 != null) {
                    preparedStatement.setParameter(fieldsIndex++ + 1, value2);
                }
                if (colInfo2.isPrimary()) {
                    preparedStatement.setObject(fieldsToUpdate + fieldsPrimaryIndex++ + 1, this.getObject(pos2 + 1), colInfo2.getColumnType().getSqlType());
                }
            }
            preparedStatement.execute();
            this.state = 2;
            this.refreshRow();
            Arrays.fill(this.parameterHolders, null);
            this.state = 0;
        }
    }
    
    @Override
    public void deleteRow() throws SQLException {
        if (this.state == 3) {
            throw new SQLException("Cannot call deleteRow() when inserting a new row");
        }
        if (!this.canBeUpdate) {
            throw new SQLDataException(this.exceptionUpdateMsg);
        }
        if (this.getRowPointer() < 0) {
            throw new SQLDataException("Current position is before the first row", "22023");
        }
        if (this.getRowPointer() >= this.getDataSize()) {
            throw new SQLDataException("Current position is after the last row", "22023");
        }
        if (this.deletePreparedStatement == null) {
            final StringBuilder deleteSql = new StringBuilder("DELETE FROM `" + this.database + "`.`" + this.table + "` WHERE ");
            boolean firstPrimary = true;
            for (int pos = 0; pos < this.columnInformationLength; ++pos) {
                final UpdatableColumnDefinition colInfo = this.getUpdatableColumns()[pos];
                if (colInfo.isPrimary()) {
                    if (!firstPrimary) {
                        deleteSql.append("AND ");
                    }
                    firstPrimary = false;
                    deleteSql.append("`").append(colInfo.getOriginalName()).append("` = ? ");
                }
            }
            this.deletePreparedStatement = this.connection.clientPrepareStatement(deleteSql.toString());
        }
        int fieldsPrimaryIndex = 1;
        for (int pos2 = 0; pos2 < this.columnInformationLength; ++pos2) {
            final UpdatableColumnDefinition colInfo2 = this.getUpdatableColumns()[pos2];
            if (colInfo2.isPrimary()) {
                this.deletePreparedStatement.setObject(fieldsPrimaryIndex++, this.getObject(pos2 + 1), colInfo2.getColumnType().getSqlType());
            }
        }
        this.deletePreparedStatement.executeUpdate();
        this.deleteCurrentRowData();
    }
    
    private void prepareRefreshStmt() throws SQLException {
        if (this.refreshPreparedStatement == null) {
            final StringBuilder selectSql = new StringBuilder("SELECT ");
            final StringBuilder whereClause = new StringBuilder(" WHERE ");
            boolean firstPrimary = true;
            for (int pos = 0; pos < this.columnInformationLength; ++pos) {
                final UpdatableColumnDefinition colInfo = this.getUpdatableColumns()[pos];
                if (pos != 0) {
                    selectSql.append(",");
                }
                selectSql.append("`").append(colInfo.getOriginalName()).append("`");
                if (colInfo.isPrimary()) {
                    if (!firstPrimary) {
                        whereClause.append("AND ");
                    }
                    firstPrimary = false;
                    whereClause.append("`").append(colInfo.getOriginalName()).append("` = ? ");
                }
            }
            selectSql.append(" FROM `").append(this.database).append("`.`").append(this.table).append("`").append((CharSequence)whereClause);
            if (this.isBinaryEncoded()) {
                this.refreshPreparedStatement = this.connection.serverPrepareStatement(selectSql.toString());
            }
            else {
                this.refreshPreparedStatement = this.connection.clientPrepareStatement(selectSql.toString());
            }
        }
    }
    
    private byte[] refreshRawData() throws SQLException {
        this.prepareRefreshStmt();
        int fieldsPrimaryIndex = 1;
        for (int pos = 0; pos < this.columnInformationLength; ++pos) {
            final UpdatableColumnDefinition colInfo = this.getUpdatableColumns()[pos];
            if (colInfo.isPrimary()) {
                final ParameterHolder value = this.parameterHolders[pos];
                if (this.state != 0 && value != null) {
                    if (this.isBinaryEncoded()) {
                        ((ServerSidePreparedStatement)this.refreshPreparedStatement).setParameter(fieldsPrimaryIndex++, value);
                    }
                    else {
                        ((ClientSidePreparedStatement)this.refreshPreparedStatement).setParameter(fieldsPrimaryIndex++, value);
                    }
                }
                else {
                    this.refreshPreparedStatement.setObject(fieldsPrimaryIndex++, this.getObject(pos + 1), colInfo.getColumnType().getSqlType());
                }
            }
        }
        final SelectResultSet rs = (SelectResultSet)this.refreshPreparedStatement.executeQuery();
        if (rs.next()) {
            return rs.getCurrentRowData();
        }
        return new byte[0];
    }
    
    @Override
    public void refreshRow() throws SQLException {
        if (this.state == 3) {
            throw new SQLException("Cannot call deleteRow() when inserting a new row");
        }
        if (this.getRowPointer() < 0) {
            throw new SQLDataException("Current position is before the first row", "22023");
        }
        if (this.getRowPointer() >= this.getDataSize()) {
            throw new SQLDataException("Current position is after the last row", "22023");
        }
        if (this.canBeRefresh) {
            this.updateRowData(this.refreshRawData());
        }
    }
    
    @Override
    public void cancelRowUpdates() {
        Arrays.fill(this.parameterHolders, null);
        this.state = 0;
    }
    
    @Override
    public void moveToInsertRow() throws SQLException {
        if (!this.canBeInserted) {
            throw new SQLException(this.exceptionInsertMsg);
        }
        Arrays.fill(this.parameterHolders, null);
        this.state = 3;
        this.notInsertRowPointer = this.getRowPointer();
    }
    
    @Override
    public void moveToCurrentRow() {
        Arrays.fill(this.parameterHolders, null);
        this.state = 0;
        this.setRowPointer(this.notInsertRowPointer);
    }
    
    @Override
    public void beforeFirst() throws SQLException {
        if (this.state == 3) {
            this.state = 1;
            this.setRowPointer(this.notInsertRowPointer);
        }
        super.beforeFirst();
    }
    
    @Override
    public boolean first() throws SQLException {
        if (this.state == 3) {
            this.state = 1;
            this.setRowPointer(this.notInsertRowPointer);
        }
        return super.first();
    }
    
    @Override
    public boolean last() throws SQLException {
        if (this.state == 3) {
            this.state = 1;
            this.setRowPointer(this.notInsertRowPointer);
        }
        return super.last();
    }
    
    @Override
    public void afterLast() throws SQLException {
        if (this.state == 3) {
            this.state = 1;
            this.setRowPointer(this.notInsertRowPointer);
        }
        super.afterLast();
    }
    
    @Override
    public boolean absolute(final int row) throws SQLException {
        if (this.state == 3) {
            this.state = 1;
            this.setRowPointer(this.notInsertRowPointer);
        }
        return super.absolute(row);
    }
    
    @Override
    public boolean relative(final int rows) throws SQLException {
        if (this.state == 3) {
            this.state = 1;
            this.setRowPointer(this.notInsertRowPointer);
        }
        return super.relative(rows);
    }
    
    @Override
    public boolean next() throws SQLException {
        if (this.state == 3) {
            this.state = 1;
            this.setRowPointer(this.notInsertRowPointer);
        }
        return super.next();
    }
    
    @Override
    public boolean previous() throws SQLException {
        if (this.state == 3) {
            this.state = 1;
            this.setRowPointer(this.notInsertRowPointer);
        }
        return super.previous();
    }
}
