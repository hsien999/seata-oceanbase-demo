// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.SQLXML;
import java.sql.RowId;
import java.net.MalformedURLException;
import java.net.URL;
import com.oceanbase.jdbc.extend.datatype.RowObCursorData;
import com.oceanbase.jdbc.internal.com.read.resultset.RefCursor;
import java.sql.Struct;
import com.oceanbase.jdbc.extend.datatype.ComplexDataType;
import java.sql.Array;
import java.sql.Ref;
import java.util.Locale;
import java.util.HashMap;
import java.time.OffsetTime;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.math.BigInteger;
import java.io.StringReader;
import java.io.Reader;
import java.sql.NClob;
import java.util.Map;
import com.oceanbase.jdbc.extend.datatype.RowIdImpl;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import com.oceanbase.jdbc.extend.datatype.NUMBER;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMP;
import java.sql.Time;
import java.sql.Date;
import com.oceanbase.jdbc.extend.datatype.INTERVALDS;
import com.oceanbase.jdbc.extend.datatype.INTERVALYM;
import java.math.BigDecimal;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMPLTZ;
import com.oceanbase.jdbc.extend.datatype.TIMESTAMPTZ;
import java.sql.Connection;
import java.util.Calendar;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Blob;
import java.sql.Clob;
import java.io.UnsupportedEncodingException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLWarning;
import java.sql.SQLDataException;
import java.util.Arrays;
import com.oceanbase.jdbc.internal.com.read.ErrorPacket;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import java.sql.Statement;
import com.oceanbase.jdbc.internal.io.input.StandardPacketInputStream;
import java.util.ArrayList;
import com.oceanbase.jdbc.internal.ColumnType;
import java.sql.ResultSet;
import java.util.List;
import java.sql.SQLException;
import java.io.IOException;
import com.oceanbase.jdbc.internal.com.read.resultset.rowprotocol.TextRowProtocol;
import com.oceanbase.jdbc.internal.com.read.resultset.rowprotocol.BinaryRowProtocol;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import java.sql.ResultSetMetaData;
import com.oceanbase.jdbc.extend.datatype.ComplexData;
import java.util.concurrent.locks.ReentrantLock;
import com.oceanbase.jdbc.internal.com.read.dao.ColumnLabelIndexer;
import com.oceanbase.jdbc.internal.com.read.resultset.rowprotocol.RowProtocol;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.util.Options;
import java.util.TimeZone;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;

public class JDBC4ResultSet implements ResultSetImpl
{
    public static final int TINYINT1_IS_BIT = 1;
    public static final int YEAR_IS_DATE_TYPE = 2;
    private static final String NOT_UPDATABLE_ERROR = "Updates are not supported when using ResultSet.CONCUR_READ_ONLY";
    private static final ColumnDefinition[] INSERT_ID_COLUMNS;
    private static final int MAX_ARRAY_SIZE = 2147483639;
    protected TimeZone timeZone;
    protected Options options;
    protected ColumnDefinition[] columnsInformation;
    protected int columnInformationLength;
    protected boolean noBackslashEscapes;
    private Protocol protocol;
    private PacketInputStream reader;
    protected boolean isEof;
    protected boolean isLastRow;
    private boolean callableResult;
    private OceanBaseStatement statement;
    public RowProtocol row;
    private int dataFetchTime;
    protected byte[][] data;
    protected int dataSize;
    private int fetchSize;
    protected int resultSetScrollType;
    protected int rowPointer;
    private int discardedRows;
    protected ColumnLabelIndexer columnLabelIndexer;
    private int lastRowPointer;
    private boolean isClosed;
    private boolean eofDeprecated;
    private ReentrantLock lock;
    protected boolean forceAlias;
    public ComplexData[] complexData;
    public int[] complexEndPos;
    private boolean isPsOutParameter;
    private ResultSetMetaData metaData;
    private ResultSetClass rsClass;
    
    public JDBC4ResultSet(final ColumnDefinition[] columnDefinition, final Results results, final Protocol protocol, final PacketInputStream reader, final boolean callableResult, final boolean eofDeprecated, final boolean isPsOutParameter) throws IOException, SQLException {
        this.isLastRow = false;
        this.discardedRows = 0;
        this.lastRowPointer = -1;
        this.statement = results.getStatement();
        this.isClosed = false;
        this.protocol = protocol;
        this.options = protocol.getOptions();
        this.noBackslashEscapes = protocol.noBackslashEscapes();
        this.columnsInformation = columnDefinition;
        this.columnLabelIndexer = new ColumnLabelIndexer(this.columnsInformation);
        this.columnInformationLength = columnDefinition.length;
        this.complexData = new ComplexData[this.columnInformationLength];
        this.complexEndPos = new int[this.columnInformationLength];
        this.isEof = false;
        this.timeZone = protocol.getTimeZone();
        if (results.isBinaryFormat()) {
            this.row = new BinaryRowProtocol(this.columnsInformation, this.columnInformationLength, results.getMaxFieldSize(), this.options);
        }
        else {
            this.row = new TextRowProtocol(results.getMaxFieldSize(), this.options);
        }
        this.row.setProtocol(protocol);
        this.fetchSize = results.getFetchSize();
        this.resultSetScrollType = results.getResultSetScrollType();
        this.dataSize = 0;
        this.dataFetchTime = 0;
        this.rowPointer = -1;
        this.callableResult = callableResult;
        this.eofDeprecated = eofDeprecated;
        this.isPsOutParameter = isPsOutParameter;
        this.reader = reader;
        if (results.getResultSetScrollType() == 1003 && results.getResultSetConcurrency() == 1007 && this.fetchSize == Integer.MIN_VALUE) {
            this.rsClass = ResultSetClass.STREAMING;
            this.fetchSize = 1;
            this.lock = protocol.getLock();
            protocol.setActiveStreamingResult(results);
            protocol.removeHasMoreResults();
            this.data = new byte[Math.max(10, this.fetchSize)][];
            this.nextStreamingValue();
        }
        else {
            this.rsClass = ResultSetClass.COMPLETE;
            this.data = new byte[10][];
            this.fetchAllResults();
        }
    }
    
    public JDBC4ResultSet(final ColumnDefinition[] columnDefinition, final Results results, final Protocol protocol, final boolean callableResult, final boolean eofDeprecated, final boolean isPsOutParameter) throws IOException, SQLException {
        this.isLastRow = false;
        this.discardedRows = 0;
        this.lastRowPointer = -1;
        this.statement = results.getStatement();
        this.isClosed = false;
        this.protocol = protocol;
        this.options = protocol.getOptions();
        this.noBackslashEscapes = protocol.noBackslashEscapes();
        this.columnsInformation = columnDefinition;
        this.columnLabelIndexer = new ColumnLabelIndexer(this.columnsInformation);
        this.columnInformationLength = columnDefinition.length;
        this.complexData = new ComplexData[this.columnInformationLength];
        this.complexEndPos = new int[this.columnInformationLength];
        this.isEof = false;
        this.timeZone = protocol.getTimeZone();
        if (results.isBinaryFormat()) {
            this.row = new BinaryRowProtocol(this.columnsInformation, this.columnInformationLength, results.getMaxFieldSize(), this.options);
        }
        else {
            this.row = new TextRowProtocol(results.getMaxFieldSize(), this.options);
        }
        this.row.setProtocol(protocol);
        this.fetchSize = results.getFetchSize();
        this.resultSetScrollType = results.getResultSetScrollType();
        this.dataSize = 0;
        this.dataFetchTime = 0;
        this.rowPointer = -1;
        this.callableResult = callableResult;
        this.eofDeprecated = eofDeprecated;
        this.isPsOutParameter = isPsOutParameter;
        this.lock = protocol.getLock();
        this.reader = protocol.getReader();
        this.rsClass = ResultSetClass.CURSOR;
        this.data = new byte[Math.max(10, this.fetchSize)][];
        if (protocol.supportStmtPrepareExecute()) {
            this.lock = protocol.getLock();
            this.nextStreamingValue();
        }
    }
    
    public boolean assign(final JDBC4ResultSet opt) {
        if (opt.data == null || opt.dataSize == 0) {
            return false;
        }
        this.statement = opt.statement;
        this.isClosed = opt.isClosed;
        this.protocol = opt.protocol;
        this.options = opt.options;
        this.noBackslashEscapes = opt.noBackslashEscapes;
        this.columnsInformation = opt.columnsInformation;
        this.columnLabelIndexer = opt.columnLabelIndexer;
        this.columnInformationLength = opt.columnInformationLength;
        this.complexData = opt.complexData;
        this.complexEndPos = opt.complexEndPos;
        this.reader = opt.reader;
        this.isEof = opt.isEof;
        this.timeZone = this.protocol.getTimeZone();
        this.row = opt.row;
        this.fetchSize = opt.fetchSize;
        this.resultSetScrollType = opt.resultSetScrollType;
        this.dataSize = opt.dataSize;
        this.dataFetchTime = opt.dataFetchTime;
        this.rowPointer = opt.rowPointer;
        this.callableResult = opt.callableResult;
        this.eofDeprecated = opt.eofDeprecated;
        this.isPsOutParameter = opt.isPsOutParameter;
        this.data = opt.data;
        this.rsClass = opt.rsClass;
        return true;
    }
    
    public void changeRowProtocol(final RowProtocol row) {
        this.row = row;
    }
    
    public void setProtocol(final Protocol protocol) {
        this.protocol = protocol;
    }
    
    public JDBC4ResultSet(final ColumnDefinition[] columnDefinition, final List<byte[]> resultSet, final Protocol protocol, final int resultSetScrollType) {
        this.isLastRow = false;
        this.discardedRows = 0;
        this.lastRowPointer = -1;
        this.statement = null;
        this.isClosed = false;
        if (protocol != null) {
            this.options = protocol.getOptions();
            this.timeZone = protocol.getTimeZone();
        }
        else {
            this.options = new Options();
            this.timeZone = TimeZone.getDefault();
        }
        this.row = new TextRowProtocol(0, this.options);
        this.protocol = protocol;
        this.columnsInformation = columnDefinition;
        this.columnLabelIndexer = new ColumnLabelIndexer(this.columnsInformation);
        this.columnInformationLength = columnDefinition.length;
        this.isEof = true;
        this.fetchSize = 0;
        this.resultSetScrollType = resultSetScrollType;
        this.data = resultSet.toArray(new byte[10][]);
        this.dataSize = resultSet.size();
        this.dataFetchTime = 0;
        this.rowPointer = -1;
        this.callableResult = false;
        this.rsClass = ResultSetClass.COMPLETE;
    }
    
    public Protocol getProtocol() {
        return this.protocol;
    }
    
    public static ResultSet createGeneratedData(final long[] data, final Protocol protocol, final boolean findColumnReturnsOne) {
        final ColumnDefinition[] columns = { ColumnDefinition.create("GENERATED_KEY", ColumnType.BIGINT, protocol.isOracleMode(), protocol.getOptions().characterEncoding) };
        final List<byte[]> rows = new ArrayList<byte[]>();
        for (final long rowData : data) {
            if (rowData != 0L) {
                rows.add(StandardPacketInputStream.create(String.valueOf(rowData).getBytes()));
            }
        }
        if (findColumnReturnsOne) {
            return new JDBC4ResultSet(columns, rows, protocol, 1005) {
                @Override
                public int findColumn(final String name) {
                    return 1;
                }
            };
        }
        return new JDBC4ResultSet(columns, rows, protocol, 1005);
    }
    
    public static JDBC4ResultSet createResultSet(final String[] columnNames, final ColumnType[] columnTypes, final String[][] data, final Protocol protocol) {
        final int columnNameLength = columnNames.length;
        final ColumnDefinition[] columns = new ColumnDefinition[columnNameLength];
        for (int i = 0; i < columnNameLength; ++i) {
            columns[i] = ColumnDefinition.create(columnNames[i], columnTypes[i], protocol.isOracleMode(), protocol.getOptions().characterEncoding);
        }
        final List<byte[]> rows = new ArrayList<byte[]>();
        for (final String[] rowData : data) {
            final byte[][] rowBytes = new byte[rowData.length][];
            for (int j = 0; j < rowData.length; ++j) {
                if (rowData[j] != null) {
                    rowBytes[j] = rowData[j].getBytes();
                }
            }
            rows.add(StandardPacketInputStream.create(rowBytes, columnTypes));
        }
        return new JDBC4ResultSet(columns, rows, protocol, 1005);
    }
    
    public static JDBC4ResultSet createEmptyResultSet() {
        return new JDBC4ResultSet(JDBC4ResultSet.INSERT_ID_COLUMNS, new ArrayList<byte[]>(), null, 1005);
    }
    
    public boolean isFullyLoaded() {
        return this.isEof;
    }
    
    private void fetchAllResults() throws IOException, SQLException {
        this.dataSize = 0;
        while (this.readNextValue()) {}
        ++this.dataFetchTime;
    }
    
    public void fetchRemaining() throws SQLException {
        if (!this.isEof) {
            this.lock.lock();
            try {
                this.lastRowPointer = -1;
                while (!this.isEof) {
                    this.addStreamingValue();
                }
                ++this.dataFetchTime;
            }
            catch (SQLException queryException) {
                throw ExceptionFactory.INSTANCE.create(queryException);
            }
            catch (IOException ioe) {
                throw this.handleIoException(ioe);
            }
            finally {
                this.lock.unlock();
            }
        }
    }
    
    protected SQLException handleIoException(final IOException ioe) {
        return ExceptionFactory.INSTANCE.create("Server has closed the connection. \nPlease check net_read_timeout/net_write_timeout/wait_timeout server variables. If result set contain huge amount of data, Server expects client to read off the result set relatively fast. In this case, please consider increasing net_read_timeout session variable / processing your result set faster (check Streaming result sets documentation for more information)", "08000", ioe);
    }
    
    public ColumnDefinition[] getColumnsInformation() {
        return this.columnsInformation;
    }
    
    private void nextStreamingValue() throws IOException, SQLException {
        this.lastRowPointer = -1;
        if (this.resultSetScrollType == 1003) {
            this.discardedRows += this.dataSize;
            this.dataSize = 0;
        }
        this.addStreamingValue();
    }
    
    private void addStreamingValue() throws IOException, SQLException {
        for (int fetchSizeTmp = this.fetchSize; fetchSizeTmp > 0 && this.readNextValue(); --fetchSizeTmp) {}
        if (!this.isEof && this.rsClass == ResultSetClass.CURSOR) {
            this.readNextValue();
            if (!this.isEof) {
                throw new SQLException("a EOF packet is supposed to be read");
            }
        }
        ++this.dataFetchTime;
    }
    
    protected boolean readNextValue() throws IOException, SQLException {
        final byte[] buf = this.reader.getPacketArray(false);
        if (buf[0] == -1) {
            this.protocol.removeActiveStreamingResult();
            this.protocol.removeHasMoreResults();
            this.protocol.setHasWarnings(false);
            final ErrorPacket errorPacket = new ErrorPacket(new Buffer(buf));
            this.isEof = true;
            throw ExceptionFactory.INSTANCE.create(errorPacket.getMessage(), errorPacket.getSqlState(), errorPacket.getErrorCode());
        }
        if (buf[0] == -2 && ((this.eofDeprecated && buf.length < 16777215) || (!this.eofDeprecated && buf.length < 8))) {
            int warnings;
            int serverStatus;
            if (!this.eofDeprecated) {
                warnings = (buf[1] & 0xFF) + ((buf[2] & 0xFF) << 8);
                serverStatus = (buf[3] & 0xFF) + ((buf[4] & 0xFF) << 8);
                if (this.callableResult) {}
            }
            else {
                int pos = this.skipLengthEncodedValue(buf, 1);
                pos = this.skipLengthEncodedValue(buf, pos);
                serverStatus = (buf[pos++] & 0xFF) + ((buf[pos++] & 0xFF) << 8);
                warnings = (buf[pos++] & 0xFF) + ((buf[pos] & 0xFF) << 8);
                this.callableResult = ((serverStatus & 0x1000) != 0x0);
            }
            this.protocol.setServerStatus((short)serverStatus);
            this.protocol.setHasWarnings(warnings > 0);
            if ((serverStatus & 0x8) == 0x0) {
                this.protocol.removeActiveStreamingResult();
            }
            if ((serverStatus & 0x80) != 0x0) {
                this.isLastRow = true;
            }
            this.isEof = true;
            return false;
        }
        if (this.dataSize + 1 >= this.data.length) {
            this.growDataArray();
        }
        this.data[this.dataSize++] = buf;
        return true;
    }
    
    public byte[] getCurrentRowData() {
        return this.data[this.rowPointer];
    }
    
    protected void updateRowData(final byte[] rawData) {
        this.data[this.rowPointer] = rawData;
        this.row.resetRow(this.data[this.rowPointer]);
    }
    
    protected void deleteCurrentRowData() throws SQLException {
        System.arraycopy(this.data, this.rowPointer + 1, this.data, this.rowPointer, this.dataSize - 1 - this.rowPointer);
        this.data[this.dataSize - 1] = null;
        --this.dataSize;
        this.lastRowPointer = -1;
        this.previous();
    }
    
    public void addRowData(final byte[] rawData) {
        if (this.dataSize + 1 >= this.data.length) {
            this.growDataArray();
        }
        this.data[this.dataSize] = rawData;
        this.rowPointer = this.dataSize;
        ++this.dataSize;
    }
    
    private int skipLengthEncodedValue(final byte[] buf, int pos) {
        final int type = buf[pos++] & 0xFF;
        switch (type) {
            case 251: {
                return pos;
            }
            case 252: {
                return pos + 2 + (0xFFFF & (buf[pos] & 0xFF) + ((buf[pos + 1] & 0xFF) << 8));
            }
            case 253: {
                return pos + 3 + (0xFFFFFF & (buf[pos] & 0xFF) + ((buf[pos + 1] & 0xFF) << 8) + ((buf[pos + 2] & 0xFF) << 16));
            }
            case 254: {
                return (int)(pos + 8 + ((buf[pos] & 0xFF) + ((long)(buf[pos + 1] & 0xFF) << 8) + ((long)(buf[pos + 2] & 0xFF) << 16) + ((long)(buf[pos + 3] & 0xFF) << 24) + ((long)(buf[pos + 4] & 0xFF) << 32) + ((long)(buf[pos + 5] & 0xFF) << 40) + ((long)(buf[pos + 6] & 0xFF) << 48) + ((long)(buf[pos + 7] & 0xFF) << 56)));
            }
            default: {
                return pos + type;
            }
        }
    }
    
    private void growDataArray() {
        int newCapacity = this.data.length + (this.data.length >> 1);
        if (newCapacity - 2147483639 > 0) {
            newCapacity = 2147483639;
        }
        this.data = Arrays.copyOf(this.data, newCapacity);
    }
    
    public void abort() throws SQLException {
        this.isClosed = true;
        this.isEof = true;
        for (int i = 0; i < this.data.length; ++i) {
            this.data[i] = null;
        }
        if (this.statement != null) {
            this.statement.checkCloseOnCompletion(this);
            this.statement = null;
        }
    }
    
    @Override
    public void close() throws SQLException {
        if (this.rsClass == ResultSetClass.STREAMING && !this.isEof) {
            this.lock.lock();
            try {
                while (!this.isEof) {
                    this.dataSize = 0;
                    this.readNextValue();
                }
            }
            catch (SQLException queryException) {
                throw ExceptionFactory.INSTANCE.create(queryException);
            }
            catch (IOException ioe) {
                throw this.handleIoException(ioe);
            }
            finally {
                this.isEof = true;
                this.lock.unlock();
            }
        }
        for (int i = 0; i < this.data.length; ++i) {
            this.data[i] = null;
        }
        if (this.statement != null) {
            this.statement.checkCloseOnCompletion(this);
            this.statement = null;
        }
        this.isClosed = true;
    }
    
    private void resetVariables() {
        this.isEof = true;
    }
    
    private void checkObjectRange(final int position) throws SQLException {
        if (this.rowPointer < 0) {
            throw new SQLDataException("Current position is before the first row", "22023");
        }
        if (this.rowPointer >= this.dataSize) {
            throw new SQLDataException("Current position is after the last row", "22023");
        }
        if (position <= 0 || position > this.columnInformationLength) {
            throw new SQLDataException("No such column: " + position, "22023");
        }
        if (this.lastRowPointer != this.rowPointer) {
            this.row.resetRow(this.data[this.rowPointer]);
            this.lastRowPointer = this.rowPointer;
        }
        this.row.setPosition(position - 1);
    }
    
    @Override
    public SQLWarning getWarnings() throws SQLException {
        if (this.statement == null) {
            return null;
        }
        return this.statement.getWarnings();
    }
    
    @Override
    public void clearWarnings() {
        if (this.statement != null) {
            this.statement.clearWarnings();
        }
    }
    
    @Override
    public boolean isBeforeFirst() throws SQLException {
        this.checkClose();
        return (this.dataFetchTime > 0) ? (this.rowPointer == -1 && this.dataSize > 0) : (this.rowPointer == -1);
    }
    
    @Override
    public boolean isAfterLast() throws SQLException {
        this.checkClose();
        if (this.rowPointer < this.dataSize) {
            return false;
        }
        if (this.rsClass == ResultSetClass.STREAMING && !this.isEof) {
            this.lock.lock();
            try {
                this.addStreamingValue();
            }
            catch (IOException ioe) {
                throw this.handleIoException(ioe);
            }
            finally {
                this.lock.unlock();
            }
            return this.dataSize == this.rowPointer && this.isLastRow;
        }
        return this.dataSize > 0 || this.dataFetchTime > 1;
    }
    
    @Override
    public boolean isFirst() throws SQLException {
        this.checkClose();
        if (this.rsClass == ResultSetClass.STREAMING) {
            throw new SQLException("Invalid operation on STREAMING ResultSet: isFirst");
        }
        if (this.rsClass == ResultSetClass.CURSOR) {
            return this.rowPointer == 0 && this.dataSize > 0;
        }
        return this.dataFetchTime == 1 && this.rowPointer == 0 && this.dataSize > 0;
    }
    
    @Override
    public boolean isLast() throws SQLException {
        this.checkClose();
        if (this.rsClass == ResultSetClass.STREAMING) {
            throw new SQLException("Invalid operation on STREAMING ResultSet: isLast");
        }
        if (this.rowPointer < this.dataSize - 1) {
            return false;
        }
        if (this.isEof) {
            return this.rowPointer == this.dataSize - 1 && this.dataSize > 0;
        }
        this.lock.lock();
        try {
            if (!this.isEof && this.rsClass == ResultSetClass.STREAMING) {
                this.addStreamingValue();
            }
        }
        catch (IOException ioe) {
            throw this.handleIoException(ioe);
        }
        finally {
            this.lock.unlock();
        }
        return this.isEof && this.rowPointer == this.dataSize - 1 && this.dataSize > 0;
    }
    
    @Override
    public void beforeFirst() throws SQLException {
        this.checkClose();
        if (this.rsClass != ResultSetClass.COMPLETE && this.resultSetScrollType == 1003) {
            throw new SQLException("Invalid operation on TYPE_FORWARD_ONLY CURSOR or STREAMING ResultSet: beforeFirst");
        }
        this.rowPointer = -1;
    }
    
    @Override
    public void afterLast() throws SQLException {
        this.checkClose();
        if (this.rsClass == ResultSetClass.STREAMING) {
            throw new SQLException("Invalid operation on STREAMING ResultSet: afterLast");
        }
        this.fetchRemaining();
        this.rowPointer = this.dataSize;
    }
    
    @Override
    public boolean first() throws SQLException {
        this.checkClose();
        if (this.rsClass != ResultSetClass.COMPLETE && this.resultSetScrollType == 1003) {
            throw new SQLException("Invalid operation on TYPE_FORWARD_ONLY CURSOR or STREAMING ResultSet: first");
        }
        this.rowPointer = 0;
        return this.dataSize > 0;
    }
    
    @Override
    public boolean last() throws SQLException {
        this.checkClose();
        if (this.rsClass == ResultSetClass.STREAMING) {
            throw new SQLException("Invalid operation on STREAMING ResultSet: last");
        }
        this.fetchRemaining();
        this.rowPointer = this.dataSize - 1;
        return this.dataSize > 0;
    }
    
    @Override
    public boolean absolute(final int row) throws SQLException {
        this.checkClose();
        if (this.rsClass == ResultSetClass.STREAMING) {
            throw new SQLException("Invalid operation on STREAMING ResultSet: absolute");
        }
        if (row >= 0 && row <= this.dataSize) {
            this.rowPointer = row - 1;
            return true;
        }
        this.fetchRemaining();
        if (row >= 0) {
            if (row <= this.dataSize) {
                this.rowPointer = row - 1;
                return true;
            }
            this.rowPointer = this.dataSize;
            return false;
        }
        else {
            if (this.dataSize + row >= 0) {
                this.rowPointer = this.dataSize + row;
                return true;
            }
            this.rowPointer = -1;
            return false;
        }
    }
    
    @Override
    public boolean relative(final int rows) throws SQLException {
        this.checkClose();
        if (this.rsClass == ResultSetClass.STREAMING) {
            throw new SQLException("Invalid operation on STREAMING ResultSet: relative");
        }
        final int newPos = this.rowPointer + rows;
        if (newPos <= -1) {
            this.rowPointer = -1;
            return false;
        }
        if (newPos >= this.dataSize) {
            this.rowPointer = this.dataSize;
            return false;
        }
        this.rowPointer = newPos;
        return true;
    }
    
    @Override
    public boolean previous() throws SQLException {
        this.checkClose();
        if (this.rsClass != ResultSetClass.COMPLETE && this.resultSetScrollType == 1003) {
            throw new SQLException("Invalid operation on TYPE_FORWARD_ONLY CURSOR or STREAMING ResultSet: previous");
        }
        if (this.rowPointer > -1) {
            --this.rowPointer;
            return this.rowPointer != -1;
        }
        return false;
    }
    
    @Override
    public boolean next() throws SQLException {
        if (this.isClosed) {
            throw new SQLException("Operation not permit on a closed resultSet", "HY000");
        }
        if (this.rowPointer < this.dataSize - 1) {
            ++this.rowPointer;
            return true;
        }
        if (this.rsClass != ResultSetClass.STREAMING || this.isEof) {
            this.rowPointer = this.dataSize;
            return false;
        }
        this.lock.lock();
        try {
            if (!this.isEof) {
                this.nextStreamingValue();
            }
        }
        catch (IOException ioe) {
            throw this.handleIoException(ioe);
        }
        finally {
            this.lock.unlock();
        }
        if (this.rsClass != ResultSetClass.COMPLETE) {
            this.rowPointer = 0;
            return this.dataSize > 0;
        }
        ++this.rowPointer;
        return this.dataSize > this.rowPointer;
    }
    
    @Override
    public int getRow() throws SQLException {
        this.checkClose();
        if (this.resultSetScrollType == 1003) {
            return this.discardedRows + this.rowPointer + 1;
        }
        return this.rowPointer + 1;
    }
    
    @Override
    public int getFetchDirection() {
        return 1002;
    }
    
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        if (direction == 1001) {
            throw new SQLException("Invalid operation. Allowed direction are ResultSet.FETCH_FORWARD and ResultSet.FETCH_UNKNOWN");
        }
    }
    
    @Override
    public int getFetchSize() {
        return this.fetchSize;
    }
    
    @Override
    public void setFetchSize(final int rows) throws SQLException {
        if (rows < 0) {
            throw new SQLException("invalid fetch size ");
        }
        if (this.protocol.isOracleMode() && rows == 0) {
            this.fetchSize = this.statement.fetchSize;
        }
        else {
            this.fetchSize = rows;
        }
    }
    
    @Override
    public int getType() {
        return this.resultSetScrollType;
    }
    
    public ResultSetClass getRsClass() {
        return this.rsClass;
    }
    
    @Override
    public int getConcurrency() {
        return 1007;
    }
    
    private void checkClose() throws SQLException {
        if (this.isClosed) {
            throw new SQLException("Operation not permit on a closed resultSet", "HY000");
        }
    }
    
    public boolean isCallableResult() {
        return this.callableResult;
    }
    
    public boolean isPsOutParameter() {
        return this.isPsOutParameter;
    }
    
    @Override
    public boolean isClosed() {
        return this.isClosed;
    }
    
    @Override
    public OceanBaseStatement getStatement() {
        return this.statement;
    }
    
    public void setStatement(final OceanBaseStatement statement) {
        this.statement = statement;
    }
    
    @Override
    public boolean wasNull() {
        return this.row.wasNull();
    }
    
    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        return this.getAsciiStream(this.findColumn(columnLabel));
    }
    
    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        if (this.row.lastValueWasNull()) {
            return null;
        }
        if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.OBCLOB.getType()) {
            final Clob clob = this.getClob(columnIndex);
            if (clob == null) {
                return null;
            }
            return clob.getAsciiStream();
        }
        else if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.OBBLOB.getType()) {
            final Blob blob = this.getBlob(columnIndex);
            if (blob == null) {
                return null;
            }
            return blob.getBinaryStream();
        }
        else {
            try {
                return new ByteArrayInputStream(new String(this.row.buf, this.row.pos, this.row.getLengthMaxFieldSize(), this.protocol.getEncoding()).getBytes());
            }
            catch (UnsupportedEncodingException e) {
                throw new SQLException("Unsupported character encoding " + this.protocol.getEncoding());
            }
        }
    }
    
    @Override
    public String getString(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        switch (this.columnsInformation[columnIndex - 1].getColumnType()) {
            case OBCLOB: {
                final Clob clob = this.getClob(columnIndex);
                if (clob == null) {
                    return null;
                }
                final long len = clob.length();
                return clob.getSubString(1L, (int)len);
            }
            case OBBLOB: {
                throw new SQLFeatureNotSupportedException();
            }
            case TIMESTAMP_TZ: {
                final TIMESTAMPTZ timestamptz = this.row.getInternalTIMESTAMPTZ(this.columnsInformation[columnIndex - 1], null, this.timeZone);
                if (timestamptz == null) {
                    return null;
                }
                return timestamptz.toResultSetString(this.statement.getConnection());
            }
            case TIMESTAMP_LTZ: {
                final TIMESTAMPLTZ timestampltz = this.row.getInternalTIMESTAMPLTZ(this.columnsInformation[columnIndex - 1], null, this.timeZone);
                if (timestampltz == null) {
                    return null;
                }
                return timestampltz.toResultSetString(this.statement.getConnection());
            }
            default: {
                return this.row.getInternalString(this.columnsInformation[columnIndex - 1], null, this.timeZone);
            }
        }
    }
    
    @Override
    public String getString(final String columnLabel) throws SQLException {
        return this.getString(this.findColumn(columnLabel));
    }
    
    private String zeroFillingIfNeeded(final String value, final ColumnDefinition columnDefinition) {
        if (columnDefinition.isZeroFill()) {
            final StringBuilder zeroAppendStr = new StringBuilder();
            long zeroToAdd = columnDefinition.getDisplaySize() - value.length();
            while (zeroToAdd-- > 0L) {
                zeroAppendStr.append("0");
            }
            return zeroAppendStr.append(value).toString();
        }
        return value;
    }
    
    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        if (this.row.lastValueWasNull()) {
            return null;
        }
        if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.OBCLOB.getType()) {
            return this.getClob(columnIndex).getAsciiStream();
        }
        if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.OBBLOB.getType()) {
            return this.getBlob(columnIndex).getBinaryStream();
        }
        return new ByteArrayInputStream(this.row.buf, this.row.pos, this.row.getLengthMaxFieldSize());
    }
    
    @Override
    public InputStream getBinaryStream(final String columnLabel) throws SQLException {
        return this.getBinaryStream(this.findColumn(columnLabel));
    }
    
    @Override
    public int getInt(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalInt(this.columnsInformation[columnIndex - 1]);
    }
    
    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return this.getInt(this.findColumn(columnLabel));
    }
    
    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return this.getLong(this.findColumn(columnLabel));
    }
    
    @Override
    public long getLong(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalLong(this.columnsInformation[columnIndex - 1]);
    }
    
    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        return this.getFloat(this.findColumn(columnLabel));
    }
    
    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        final ColumnType type = this.columnsInformation[columnIndex - 1].getColumnType();
        switch (type) {
            case BINARY_DOUBLE: {
                return (float)this.getDouble(columnIndex);
            }
            default: {
                this.checkObjectRange(columnIndex);
                return this.row.getInternalFloat(this.columnsInformation[columnIndex - 1]);
            }
        }
    }
    
    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        return this.getDouble(this.findColumn(columnLabel));
    }
    
    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        final ColumnType type = this.columnsInformation[columnIndex - 1].getColumnType();
        this.checkObjectRange(columnIndex);
        return this.row.getInternalDouble(this.columnsInformation[columnIndex - 1]);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return this.getBigDecimal(this.findColumn(columnLabel), scale);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalBigDecimal(this.columnsInformation[columnIndex - 1]);
    }
    
    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        final ColumnType type = this.columnsInformation[columnIndex - 1].getColumnType();
        this.checkObjectRange(columnIndex);
        return this.row.getInternalBigDecimal(this.columnsInformation[columnIndex - 1]);
    }
    
    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return this.getBigDecimal(this.findColumn(columnLabel));
    }
    
    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return this.getBytes(this.findColumn(columnLabel));
    }
    
    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        if (this.row.lastValueWasNull()) {
            return null;
        }
        if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.OBCLOB.getType()) {
            throw new SQLFeatureNotSupportedException();
        }
        if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.OBBLOB.getType()) {
            final Blob blob = this.getBlob(columnIndex);
            if (blob == null) {
                return null;
            }
            final long len = ((com.oceanbase.jdbc.Blob)blob).length();
            return blob.getBytes(1L, (int)len);
        }
        else if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.TIMESTAMP_LTZ.getType()) {
            final TIMESTAMPLTZ timestampltz = this.getTIMESTAMPLTZ(columnIndex);
            if (timestampltz == null) {
                return null;
            }
            return timestampltz.getBytes();
        }
        else if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.TIMESTAMP_TZ.getType()) {
            final TIMESTAMPTZ timestamptz = this.getTIMESTAMPTZ(columnIndex);
            if (timestamptz == null) {
                return null;
            }
            return timestamptz.getBytes();
        }
        else if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.INTERVALYM.getType()) {
            final INTERVALYM intervalym = this.getINTERVALYM(columnIndex);
            if (intervalym == null) {
                return null;
            }
            return intervalym.getBytes();
        }
        else {
            if (this.columnsInformation[columnIndex - 1].getColumnType().getType() != ColumnType.INTERVALDS.getType()) {
                final byte[] data = new byte[this.row.getLengthMaxFieldSize()];
                System.arraycopy(this.row.buf, this.row.pos, data, 0, this.row.getLengthMaxFieldSize());
                return data;
            }
            final INTERVALDS intervalds = this.getINTERVALDS(columnIndex);
            if (intervalds == null) {
                return null;
            }
            return intervalds.getBytes();
        }
    }
    
    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalDate(this.columnsInformation[columnIndex - 1], null, this.timeZone);
    }
    
    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        return this.getDate(this.findColumn(columnLabel));
    }
    
    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalDate(this.columnsInformation[columnIndex - 1], cal, this.timeZone);
    }
    
    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return this.getDate(this.findColumn(columnLabel), cal);
    }
    
    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalTime(this.columnsInformation[columnIndex - 1], null, this.timeZone);
    }
    
    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        return this.getTime(this.findColumn(columnLabel));
    }
    
    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalTime(this.columnsInformation[columnIndex - 1], cal, this.timeZone);
    }
    
    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return this.getTime(this.findColumn(columnLabel), cal);
    }
    
    public TIMESTAMP getTIMESTAMP(final String columnLabel) throws SQLException {
        return this.getTIMESTAMP(this.findColumn(columnLabel));
    }
    
    public TIMESTAMP getTIMESTAMP(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalTIMESTAMP(this.columnsInformation[columnIndex - 1], null, this.timeZone);
    }
    
    @Override
    public INTERVALDS getINTERVALDS(final String columnLabel) throws SQLException {
        return this.getINTERVALDS(this.findColumn(columnLabel));
    }
    
    @Override
    public NUMBER getNUMBER(final int columnIndex) throws SQLException {
        return null;
    }
    
    @Override
    public NUMBER getNUMBER(final String columnName) throws SQLException {
        return null;
    }
    
    @Override
    public INTERVALDS getINTERVALDS(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalINTERVALDS(this.columnsInformation[columnIndex - 1]);
    }
    
    @Override
    public INTERVALYM getINTERVALYM(final String columnLabel) throws SQLException {
        return this.getINTERVALYM(this.findColumn(columnLabel));
    }
    
    @Override
    public INTERVALYM getINTERVALYM(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalINTERVALYM(this.columnsInformation[columnIndex - 1]);
    }
    
    @Override
    public TIMESTAMPTZ getTIMESTAMPTZ(final String columnLabel) throws SQLException {
        return this.getTIMESTAMPTZ(this.findColumn(columnLabel));
    }
    
    @Override
    public TIMESTAMPTZ getTIMESTAMPTZ(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalTIMESTAMPTZ(this.columnsInformation[columnIndex - 1], null, this.timeZone);
    }
    
    @Override
    public TIMESTAMPLTZ getTIMESTAMPLTZ(final String columnLabel) throws SQLException {
        return this.getTIMESTAMPLTZ(this.findColumn(columnLabel));
    }
    
    @Override
    public TIMESTAMPLTZ getTIMESTAMPLTZ(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalTIMESTAMPLTZ(this.columnsInformation[columnIndex - 1], null, this.timeZone);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return this.getTimestamp(this.findColumn(columnLabel));
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalTimestamp(this.columnsInformation[columnIndex - 1], cal, this.timeZone);
    }
    
    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return this.getTimestamp(this.findColumn(columnLabel), cal);
    }
    
    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        return this.row.getInternalTimestamp(this.columnsInformation[columnIndex - 1], null, this.timeZone);
    }
    
    @Override
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        return this.getUnicodeStream(this.findColumn(columnLabel));
    }
    
    @Override
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        if (this.row.lastValueWasNull()) {
            return null;
        }
        return new ByteArrayInputStream(new String(this.row.buf, this.row.pos, this.row.getLengthMaxFieldSize(), StandardCharsets.UTF_8).getBytes());
    }
    
    @Override
    public String getCursorName() throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Cursors not supported");
    }
    
    @Override
    public ResultSetMetaData getMetaData() {
        return new OceanBaseResultSetMetaData(this.columnsInformation, this.options, this.forceAlias, this.getProtocol().isOracleMode());
    }
    
    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        final ColumnType type = this.columnsInformation[columnIndex - 1].getColumnType();
        switch (type) {
            case COMPLEX: {
                return this.getComplex(columnIndex);
            }
            case STRUCT: {
                return this.getStruct(columnIndex);
            }
            case ARRAY: {
                return this.getArray(columnIndex);
            }
            case CURSOR: {
                return this.getComplexCursor(columnIndex);
            }
            case OBBLOB: {
                return this.getBlob(columnIndex);
            }
            case OBCLOB: {
                return this.getClob(columnIndex);
            }
            case ROWID: {
                return new RowIdImpl(this.getString(columnIndex));
            }
            default: {
                this.checkObjectRange(columnIndex);
                return this.row.getInternalObject(this.columnsInformation[columnIndex - 1], this.timeZone);
            }
        }
    }
    
    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        return this.getObject(this.findColumn(columnLabel));
    }
    
    @Override
    public Object getObject(final int columnIndex, final Map<String, Class<?>> map) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Method ResultSet.getObject(int columnIndex, Map<String, Class<?>> map) not supported");
    }
    
    @Override
    public Object getObject(final String columnLabel, final Map<String, Class<?>> map) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Method ResultSet.getObject(String columnLabel, Map<String, Class<?>> map) not supported");
    }
    
    @Override
    public <T> T getObject(final int columnIndex, final Class<T> type) throws SQLException {
        if (type == null) {
            throw new SQLException("Class type cannot be null");
        }
        this.checkObjectRange(columnIndex);
        if (this.row.lastValueWasNull()) {
            return null;
        }
        final ColumnDefinition col = this.columnsInformation[columnIndex - 1];
        if (type.equals(String.class)) {
            if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.OBCLOB.getType()) {
                final String encoding = this.options.characterEncoding;
                final byte[] data = new byte[this.row.length];
                System.arraycopy(this.row.buf, this.row.pos, data, 0, this.row.length);
                final com.oceanbase.jdbc.Clob clob = new com.oceanbase.jdbc.Clob(true, data, encoding, this.statement.getConnection());
                return (T)clob.toString();
            }
            return (T)this.row.getInternalString(col, null, this.timeZone);
        }
        else {
            if (type.equals(Integer.class)) {
                return (T)Integer.valueOf(this.row.getInternalInt(col));
            }
            if (type.equals(Long.class)) {
                return (T)Long.valueOf(this.row.getInternalLong(col));
            }
            if (type.equals(Short.class)) {
                return (T)Short.valueOf(this.row.getInternalShort(col));
            }
            if (type.equals(Double.class)) {
                return (T)Double.valueOf(this.row.getInternalDouble(col));
            }
            if (type.equals(Float.class)) {
                return (T)Float.valueOf(this.row.getInternalFloat(col));
            }
            if (type.equals(Byte.class)) {
                return (T)Byte.valueOf(this.row.getInternalByte(col));
            }
            if (type.equals(byte[].class)) {
                if (col.getColumnType() == ColumnType.OBBLOB) {
                    String encoding = this.options.characterEncoding;
                    if (encoding == null) {
                        encoding = "UTF8";
                    }
                    final byte[] data = new byte[this.row.length];
                    System.arraycopy(this.row.buf, this.row.pos, data, 0, this.row.length);
                    final com.oceanbase.jdbc.Blob blob = new com.oceanbase.jdbc.Blob(true, data, encoding, this.statement.getConnection());
                    return (T)(Object)blob.getBytes(1L, (int)blob.length());
                }
                final byte[] data2 = new byte[this.row.getLengthMaxFieldSize()];
                System.arraycopy(this.row.buf, this.row.pos, data2, 0, this.row.getLengthMaxFieldSize());
                return (T)(Object)data2;
            }
            else {
                if (type.equals(Date.class)) {
                    return (T)this.row.getInternalDate(col, null, this.timeZone);
                }
                if (type.equals(Time.class)) {
                    return (T)this.row.getInternalTime(col, null, this.timeZone);
                }
                if (type.equals(Timestamp.class) || type.equals(java.util.Date.class)) {
                    return (T)this.row.getInternalTimestamp(col, null, this.timeZone);
                }
                if (type.equals(Boolean.class)) {
                    return (T)Boolean.valueOf(this.row.getInternalBoolean(col));
                }
                if (type.equals(Calendar.class)) {
                    final Calendar calendar = Calendar.getInstance(this.timeZone);
                    final Timestamp timestamp = this.row.getInternalTimestamp(col, null, this.timeZone);
                    if (timestamp == null) {
                        return null;
                    }
                    calendar.setTimeInMillis(timestamp.getTime());
                    return type.cast(calendar);
                }
                else {
                    if (type.equals(Clob.class) || type.equals(NClob.class)) {
                        return (T)new com.oceanbase.jdbc.Clob(this.row.buf, this.row.pos, this.row.getLengthMaxFieldSize());
                    }
                    if (type.equals(InputStream.class)) {
                        return (T)new ByteArrayInputStream(this.row.buf, this.row.pos, this.row.getLengthMaxFieldSize());
                    }
                    if (type.equals(Reader.class)) {
                        final String value = this.row.getInternalString(col, null, this.timeZone);
                        if (value == null) {
                            return null;
                        }
                        return (T)new StringReader(value);
                    }
                    else {
                        if (type.equals(BigDecimal.class)) {
                            return (T)this.row.getInternalBigDecimal(col);
                        }
                        if (type.equals(BigInteger.class)) {
                            return (T)this.row.getInternalBigInteger(col);
                        }
                        if (type.equals(BigDecimal.class)) {
                            return (T)this.row.getInternalBigDecimal(col);
                        }
                        if (type.equals(LocalDateTime.class)) {
                            final ZonedDateTime zonedDateTime = this.row.getInternalZonedDateTime(col, LocalDateTime.class, this.timeZone);
                            return (zonedDateTime == null) ? null : type.cast(zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime());
                        }
                        if (type.equals(ZonedDateTime.class)) {
                            final ZonedDateTime zonedDateTime = this.row.getInternalZonedDateTime(col, ZonedDateTime.class, this.timeZone);
                            if (zonedDateTime == null) {
                                return null;
                            }
                            return type.cast(this.row.getInternalZonedDateTime(col, ZonedDateTime.class, this.timeZone));
                        }
                        else {
                            if (type.equals(OffsetDateTime.class)) {
                                final ZonedDateTime tmpZonedDateTime = this.row.getInternalZonedDateTime(col, OffsetDateTime.class, this.timeZone);
                                return (tmpZonedDateTime == null) ? null : type.cast(tmpZonedDateTime.toOffsetDateTime());
                            }
                            if (type.equals(OffsetDateTime.class)) {
                                final LocalDate localDate = this.row.getInternalLocalDate(col, this.timeZone);
                                if (localDate == null) {
                                    return null;
                                }
                                return type.cast(localDate);
                            }
                            else if (type.equals(LocalDate.class)) {
                                final LocalDate localDate = this.row.getInternalLocalDate(col, this.timeZone);
                                if (localDate == null) {
                                    return null;
                                }
                                return type.cast(localDate);
                            }
                            else if (type.equals(LocalTime.class)) {
                                final LocalTime localTime = this.row.getInternalLocalTime(col, this.timeZone);
                                if (localTime == null) {
                                    return null;
                                }
                                return type.cast(localTime);
                            }
                            else if (type.equals(OffsetTime.class)) {
                                final OffsetTime offsetTime = this.row.getInternalOffsetTime(col, this.timeZone);
                                if (offsetTime == null) {
                                    return null;
                                }
                                return type.cast(offsetTime);
                            }
                            else if (type.equals(com.oceanbase.jdbc.Blob.class)) {
                                if (col.getColumnType() == ColumnType.OBBLOB) {
                                    return (T)this.getBlob(columnIndex);
                                }
                                return (T)new com.oceanbase.jdbc.Blob(this.row.buf, this.row.pos, this.row.getLengthMaxFieldSize());
                            }
                            else {
                                if (!type.equals(com.oceanbase.jdbc.Clob.class)) {
                                    if (type.equals(TIMESTAMPLTZ.class)) {
                                        if (col.getColumnType() == ColumnType.TIMESTAMP_LTZ) {
                                            return (T)this.getTIMESTAMPLTZ(columnIndex);
                                        }
                                    }
                                    else if (type.equals(TIMESTAMPTZ.class)) {
                                        if (col.getColumnType() == ColumnType.TIMESTAMP_TZ) {
                                            return (T)this.getTIMESTAMPTZ(columnIndex);
                                        }
                                    }
                                    else if (type.equals(INTERVALYM.class)) {
                                        if (col.getColumnType() == ColumnType.INTERVALYM) {
                                            return (T)this.getINTERVALYM(columnIndex);
                                        }
                                    }
                                    else if (type.equals(INTERVALDS.class)) {
                                        if (col.getColumnType() == ColumnType.INTERVALDS) {
                                            return (T)this.getINTERVALDS(columnIndex);
                                        }
                                    }
                                    else if (this.options.autoDeserialize) {
                                        try {
                                            return type.cast(this.getObject(columnIndex));
                                        }
                                        catch (ClassCastException classCastException) {
                                            final SQLException exception = new SQLException("Type class '" + type.getName() + "' is not supported");
                                            exception.initCause(classCastException);
                                            throw exception;
                                        }
                                    }
                                    final SQLException exception2 = new SQLException("Type class '" + type.getName() + "' is not supported");
                                    throw exception2;
                                }
                                if (col.getColumnType() == ColumnType.OBCLOB) {
                                    return (T)this.getClob(columnIndex);
                                }
                                return (T)new com.oceanbase.jdbc.Clob(this.row.buf, this.row.pos, this.row.getLengthMaxFieldSize());
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public <T> T getObject(final String columnLabel, final Class<T> type) throws SQLException {
        return type.cast(this.getObject(this.findColumn(columnLabel), type));
    }
    
    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        final int index = this.columnLabelIndexer.getIndex(columnLabel);
        if (this.metaData != null && columnLabel.equalsIgnoreCase(this.metaData.getColumnName(index))) {
            return index;
        }
        this.forceAlias = true;
        this.metaData = this.getMetaData();
        if (index == 0 || !columnLabel.equalsIgnoreCase(this.metaData.getColumnName(index))) {
            final HashMap<String, Integer> map = new HashMap<String, Integer>();
            for (int i = 1; i <= this.metaData.getColumnCount(); ++i) {
                final String columnName = this.metaData.getColumnName(i);
                map.put(columnName.toLowerCase(Locale.ROOT), i);
            }
            this.columnLabelIndexer.setAliasMap(map);
            return this.columnLabelIndexer.getIndex(columnLabel);
        }
        return index;
    }
    
    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException {
        return this.getCharacterStream(this.findColumn(columnLabel));
    }
    
    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.OBCLOB.getType()) {
            final Clob clob = this.getClob(columnIndex);
            if (clob == null) {
                return null;
            }
            return clob.getCharacterStream();
        }
        else if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.OBBLOB.getType()) {
            final Blob blob = this.getBlob(columnIndex);
            if (blob == null) {
                return null;
            }
            return ((com.oceanbase.jdbc.Blob)blob).getCharacterStream();
        }
        else {
            final String value = this.row.getInternalString(this.columnsInformation[columnIndex - 1], null, this.timeZone);
            if (value == null) {
                return null;
            }
            return new StringReader(value);
        }
    }
    
    @Override
    public Reader getNCharacterStream(final int columnIndex) throws SQLException {
        return this.getCharacterStream(columnIndex);
    }
    
    @Override
    public Reader getNCharacterStream(final String columnLabel) throws SQLException {
        return this.getCharacterStream(this.findColumn(columnLabel));
    }
    
    @Override
    public Ref getRef(final int columnIndex) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public Ref getRef(final String columnLabel) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Getting REFs not supported");
    }
    
    @Override
    public Blob getBlob(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        if (this.row.lastValueWasNull()) {
            return null;
        }
        if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.OBBLOB.getType()) {
            String encoding = this.options.characterEncoding;
            if (encoding == null) {
                encoding = "UTF8";
            }
            final byte[] data = new byte[this.row.length];
            System.arraycopy(this.row.buf, this.row.pos, data, 0, this.row.length);
            return new com.oceanbase.jdbc.Blob(true, data, encoding, this.statement.getConnection());
        }
        return new com.oceanbase.jdbc.Blob(this.row.buf, this.row.pos, this.row.length);
    }
    
    @Override
    public Blob getBlob(final String columnLabel) throws SQLException {
        return this.getBlob(this.findColumn(columnLabel));
    }
    
    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        if (this.row.lastValueWasNull()) {
            return null;
        }
        if (this.columnsInformation[columnIndex - 1].getColumnType().getType() == ColumnType.OBCLOB.getType()) {
            final String encoding = this.options.characterEncoding;
            final byte[] data = new byte[this.row.length];
            System.arraycopy(this.row.buf, this.row.pos, data, 0, this.row.length);
            return new com.oceanbase.jdbc.Clob(true, data, encoding, this.statement.getConnection());
        }
        return new com.oceanbase.jdbc.Clob(this.row.buf, this.row.pos, this.row.length);
    }
    
    @Override
    public Clob getClob(final String columnLabel) throws SQLException {
        return this.getClob(this.findColumn(columnLabel));
    }
    
    public void setComplexData(final ComplexData[] complexData) {
        this.complexData = complexData;
    }
    
    @Override
    public Array getArray(final int columnIndex) throws SQLException {
        if (!this.getProtocol().isOracleMode()) {
            throw ExceptionFactory.INSTANCE.notSupported("Arrays not supported");
        }
        this.checkObjectRange(columnIndex);
        final int internalColumnIndex = columnIndex - 1;
        final Connection conn = this.statement.getConnection();
        if (this.columnsInformation[internalColumnIndex].getColumnType() != ColumnType.COMPLEX) {
            throw new SQLException("the field type is not FIELD_TYPE_COMPLEX");
        }
        final String typeName = this.columnsInformation[internalColumnIndex].getComplexTypeName();
        final ComplexDataType type = ((OceanBaseConnection)conn).getComplexDataType(typeName);
        if (type.getType() != 4) {
            throw new SQLException("the field complex type is not TYPE_COLLECTION");
        }
        final Array ret = this.row.getInternalArray(this.columnsInformation[columnIndex - 1], type);
        this.complexEndPos[columnIndex - 1] = this.row.pos;
        return ret;
    }
    
    public Object getComplex(final int columnIndex) throws SQLException {
        if (!this.getProtocol().isOracleMode()) {
            throw ExceptionFactory.INSTANCE.notSupported("Arrays not supported");
        }
        this.checkObjectRange(columnIndex);
        final int internalColumnIndex = columnIndex - 1;
        final Connection conn = this.statement.getConnection();
        if (this.columnsInformation[internalColumnIndex].getColumnType() == ColumnType.COMPLEX) {
            final String typeName = this.columnsInformation[internalColumnIndex].getComplexTypeName();
            final ComplexDataType type = ((OceanBaseConnection)conn).getComplexDataType(typeName);
            Object ret = null;
            if (type.getType() == 4) {
                ret = this.row.getInternalArray(this.columnsInformation[columnIndex - 1], type);
                this.complexEndPos[columnIndex - 1] = this.row.pos;
            }
            else if (type.getType() == 3) {
                ret = this.row.getInternalStruct(this.columnsInformation[columnIndex - 1], type);
                this.complexEndPos[columnIndex - 1] = this.row.pos;
            }
            return ret;
        }
        throw new SQLException("the field type is not FIELD_TYPE_COMPLEX");
    }
    
    @Override
    public Array getArray(final String columnLabel) throws SQLException {
        return this.getArray(this.findColumn(columnLabel));
    }
    
    public Struct getStruct(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        final int internalColumnIndex = columnIndex - 1;
        final Connection conn = this.statement.getConnection();
        if (this.columnsInformation[internalColumnIndex].getColumnType() != ColumnType.COMPLEX) {
            throw new SQLException("the field type is not FIELD_TYPE_COMPLEX");
        }
        final String typeName = this.columnsInformation[internalColumnIndex].getComplexTypeName();
        final ComplexDataType type = ((OceanBaseConnection)conn).getComplexDataType(typeName);
        if (type.getType() != 3) {
            throw new SQLException("the field complex type is not TYPE_COLLECTION");
        }
        final Struct ret = this.row.getInternalStruct(this.columnsInformation[columnIndex - 1], type);
        this.complexEndPos[columnIndex - 1] = this.row.pos;
        return ret;
    }
    
    public Struct getStruct(final String columnLabel) throws SQLException {
        return this.getStruct(this.findColumn(columnLabel));
    }
    
    public RefCursor getComplexCursor(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        final int internalColumnIndex = columnIndex - 1;
        final String typeName = this.columnsInformation[internalColumnIndex].getComplexTypeName();
        final ComplexDataType type = new ComplexDataType(typeName, typeName, ColumnType.CURSOR.getType());
        if (this.complexData[internalColumnIndex] == null) {
            final ComplexData data = this.row.getInternalComplexCursor(this.columnsInformation[internalColumnIndex], type);
            this.complexData[internalColumnIndex] = data;
        }
        final Object[] objects = this.complexData[internalColumnIndex].getAttrData();
        final RowObCursorData obCursorData = (RowObCursorData)objects[0];
        if (!obCursorData.isOpen()) {
            throw new SQLException("cursor is not open");
        }
        final Results results = this.statement.getResults();
        RefCursor obCursor = null;
        try {
            obCursor = new RefCursor(this.columnsInformation, results, this.protocol, false, false, false, obCursorData);
        }
        catch (IOException e) {
            throw new SQLException("io exception:" + e.getMessage());
        }
        this.complexEndPos[columnIndex - 1] = this.row.pos;
        return obCursor;
    }
    
    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        if (this.row.lastValueWasNull()) {
            return null;
        }
        try {
            return new URL(this.row.getInternalString(this.columnsInformation[columnIndex - 1], null, this.timeZone));
        }
        catch (MalformedURLException e) {
            throw ExceptionFactory.INSTANCE.create("Could not parse as URL");
        }
    }
    
    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        return this.getURL(this.findColumn(columnLabel));
    }
    
    @Override
    public RowId getRowId(final int columnIndex) throws SQLException {
        return new RowIdImpl(this.getString(columnIndex));
    }
    
    @Override
    public RowId getRowId(final String columnLabel) throws SQLException {
        return this.getRowId(this.findColumn(columnLabel));
    }
    
    @Override
    public NClob getNClob(final int columnIndex) throws SQLException {
        this.checkObjectRange(columnIndex);
        if (this.row.lastValueWasNull()) {
            return null;
        }
        return new JDBC4NClob(this.getString(columnIndex), null);
    }
    
    @Override
    public NClob getNClob(final String columnLabel) throws SQLException {
        return this.getNClob(this.findColumn(columnLabel));
    }
    
    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("SQLXML not supported");
    }
    
    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("SQLXML not supported");
    }
    
    @Override
    public String getNString(final int columnIndex) throws SQLException {
        return this.getString(columnIndex);
    }
    
    @Override
    public String getNString(final String columnLabel) throws SQLException {
        return this.getString(this.findColumn(columnLabel));
    }
    
    @Override
    public boolean getBoolean(final int index) throws SQLException {
        this.checkObjectRange(index);
        return this.row.getInternalBoolean(this.columnsInformation[index - 1]);
    }
    
    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return this.getBoolean(this.findColumn(columnLabel));
    }
    
    @Override
    public byte getByte(final int index) throws SQLException {
        this.checkObjectRange(index);
        return this.row.getInternalByte(this.columnsInformation[index - 1]);
    }
    
    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return this.getByte(this.findColumn(columnLabel));
    }
    
    @Override
    public short getShort(final int index) throws SQLException {
        this.checkObjectRange(index);
        return this.row.getInternalShort(this.columnsInformation[index - 1]);
    }
    
    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return this.getShort(this.findColumn(columnLabel));
    }
    
    @Override
    public boolean rowUpdated() throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Detecting row updates are not supported");
    }
    
    @Override
    public boolean rowInserted() throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Detecting inserts are not supported");
    }
    
    @Override
    public boolean rowDeleted() throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Row deletes are not supported");
    }
    
    @Override
    public void insertRow() throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("insertRow are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void deleteRow() throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("deleteRow are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void refreshRow() throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("refreshRow are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void cancelRowUpdates() throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void moveToInsertRow() throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void moveToCurrentRow() throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNull(final int columnIndex) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNull(final String columnLabel) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBoolean(final int columnIndex, final boolean bool) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBoolean(final String columnLabel, final boolean value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateByte(final int columnIndex, final byte value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateByte(final String columnLabel, final byte value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateShort(final int columnIndex, final short value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateShort(final String columnLabel, final short value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateInt(final int columnIndex, final int value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateInt(final String columnLabel, final int value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateFloat(final int columnIndex, final float value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateFloat(final String columnLabel, final float value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateDouble(final int columnIndex, final double value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateDouble(final String columnLabel, final double value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBigDecimal(final int columnIndex, final BigDecimal value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBigDecimal(final String columnLabel, final BigDecimal value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateString(final int columnIndex, final String value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateString(final String columnLabel, final String value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBytes(final int columnIndex, final byte[] value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBytes(final String columnLabel, final byte[] value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateDate(final int columnIndex, final Date date) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateDate(final String columnLabel, final Date value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateTime(final int columnIndex, final Time time) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateTime(final String columnLabel, final Time value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateTimestamp(final int columnIndex, final Timestamp timeStamp) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateTimestamp(final String columnLabel, final Timestamp value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream inputStream, final int length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream inputStream) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream value, final int length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream inputStream) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream inputStream, final int length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream value, final int length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream inputStream) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream inputStream) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader value, final int length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader, final int length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader value, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateObject(final int columnIndex, final Object value, final int scaleOrLength) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateObject(final int columnIndex, final Object value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateObject(final String columnLabel, final Object value, final int scaleOrLength) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateObject(final String columnLabel, final Object value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateLong(final String columnLabel, final long value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateLong(final int columnIndex, final long value) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateRow() throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("updateRow are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateRef(final int columnIndex, final Ref ref) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateRef(final String columnLabel, final Ref ref) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBlob(final int columnIndex, final Blob blob) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBlob(final String columnLabel, final Blob blob) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateClob(final int columnIndex, final Clob clob) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateClob(final String columnLabel, final Clob clob) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateClob(final int columnIndex, final Reader reader) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateClob(final String columnLabel, final Reader reader) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateArray(final int columnIndex, final Array array) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateArray(final String columnLabel, final Array array) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateRowId(final int columnIndex, final RowId rowId) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateRowId(final String columnLabel, final RowId rowId) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNString(final int columnIndex, final String nstring) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNString(final String columnLabel, final String nstring) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNClob(final int columnIndex, final NClob nclob) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNClob(final String columnLabel, final NClob nclob) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNClob(final int columnIndex, final Reader reader) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNClob(final String columnLabel, final Reader reader) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("SQLXML not supported");
    }
    
    @Override
    public void updateSQLXML(final String columnLabel, final SQLXML xmlObject) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("SQLXML not supported");
    }
    
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader value, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader reader) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
        throw ExceptionFactory.INSTANCE.notSupported("Updates are not supported when using ResultSet.CONCUR_READ_ONLY");
    }
    
    @Override
    public int getHoldability() {
        return 1;
    }
    
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        try {
            if (this.isWrapperFor(iface)) {
                return iface.cast(this);
            }
            throw new SQLException("The receiver is not a wrapper for " + iface.getName());
        }
        catch (Exception e) {
            throw new SQLException("The receiver is not a wrapper and does not implement the interface");
        }
    }
    
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }
    
    public void setForceTableAlias() {
        this.forceAlias = true;
    }
    
    private void rangeCheck(final Object className, final long minValue, final long maxValue, final long value, final ColumnDefinition columnInfo) throws SQLException {
        if (value < minValue || value > maxValue) {
            throw new SQLException("Out of range value for column '" + columnInfo.getName() + "' : value " + value + " is not in " + className + " range", "22003", 1264);
        }
    }
    
    public int getRowPointer() {
        return this.rowPointer;
    }
    
    protected void setRowPointer(final int pointer) {
        this.rowPointer = pointer;
    }
    
    public int getDataSize() {
        return this.dataSize;
    }
    
    public boolean isBinaryEncoded() {
        return this.row.isBinaryEncoded();
    }
    
    protected void resetState() {
        this.dataSize = 0;
        this.rowPointer = -1;
        this.lastRowPointer = -1;
    }
    
    public boolean isEof() {
        return this.isEof;
    }
    
    static {
        (INSERT_ID_COLUMNS = new ColumnDefinition[1])[0] = ColumnDefinition.create("insert_id", ColumnType.BIGINT, false, "UTF-8");
    }
    
    public enum ResultSetClass
    {
        COMPLETE, 
        STREAMING, 
        CURSOR;
    }
}
