// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import com.alipay.oceanbase.jdbc.feedback.ObFBElement;
import com.alipay.oceanbase.jdbc.feedback.ObFeedbackType;
import com.alipay.oceanbase.jdbc.feedback.ObFeedBackBuffer;
import java.util.Calendar;
import java.sql.Timestamp;
import java.math.BigDecimal;
import com.alipay.oceanbase.jdbc.extend.datatype.ComplexDataType;
import java.security.NoSuchAlgorithmException;
import java.lang.management.ThreadMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.FileInputStream;
import com.alipay.oceanbase.jdbc.util.ObCrc32C;
import com.alipay.oceanbase.jdbc.util.ObCrc16;
import java.io.EOFException;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEventHandler;
import com.alipay.oceanbase.jdbc.exceptions.MySQLStatementCancelledException;
import com.alipay.oceanbase.jdbc.exceptions.MySQLTimeoutException;
import com.alipay.oceanbase.jdbc.profiler.ProfilerEvent;
import com.alipay.oceanbase.jdbc.log.LogUtils;
import java.net.SocketException;
import java.sql.Statement;
import com.alipay.oceanbase.jdbc.extend.datatype.ComplexData;
import java.io.UnsupportedEncodingException;
import com.alipay.oceanbase.jdbc.authentication.Sha256PasswordPlugin;
import com.alipay.oceanbase.jdbc.authentication.MysqlClearPasswordPlugin;
import com.alipay.oceanbase.jdbc.authentication.MysqlNativePasswordPlugin;
import com.alipay.oceanbase.jdbc.authentication.MysqlOldPasswordPlugin;
import java.util.HashMap;
import java.util.ArrayList;
import java.sql.ResultSet;
import com.alipay.oceanbase.jdbc.util.ResultSetUtil;
import java.util.Iterator;
import java.io.OutputStream;
import java.sql.SQLException;
import java.io.IOException;
import java.io.BufferedInputStream;
import com.alipay.oceanbase.jdbc.util.ReadAheadInputStream;
import com.alipay.oceanbase.jdbc.util.MysqlCommonUtils;
import java.util.Properties;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.List;
import java.lang.ref.SoftReference;
import java.net.Socket;
import java.util.LinkedList;
import java.io.InputStream;
import java.util.zip.Deflater;
import java.io.BufferedOutputStream;
import com.alipay.oceanbase.jdbc.feedback.ObFeedbackManager;

public class MysqlIO
{
    private static final String CODE_PAGE_1252 = "Cp1252";
    protected static final int NULL_LENGTH = -1;
    protected static final int COMP_HEADER_LENGTH = 3;
    protected static final int MIN_COMPRESS_LEN = 50;
    protected static final int HEADER_LENGTH = 4;
    protected static final int AUTH_411_OVERHEAD = 33;
    public static final int SEED_LENGTH = 20;
    private static int maxBufferSize;
    private static final String NONE = "none";
    private static final int CLIENT_LONG_PASSWORD = 1;
    private static final int CLIENT_FOUND_ROWS = 2;
    private static final int CLIENT_LONG_FLAG = 4;
    protected static final int CLIENT_CONNECT_WITH_DB = 8;
    private static final int CLIENT_COMPRESS = 32;
    private static final int CLIENT_LOCAL_FILES = 128;
    private static final int CLIENT_PROTOCOL_41 = 512;
    private static final int CLIENT_INTERACTIVE = 1024;
    protected static final int CLIENT_SSL = 2048;
    private static final int CLIENT_TRANSACTIONS = 8192;
    protected static final int CLIENT_RESERVED = 16384;
    protected static final int CLIENT_SECURE_CONNECTION = 32768;
    private static final int CLIENT_MULTI_STATEMENTS = 65536;
    private static final int CLIENT_MULTI_RESULTS = 131072;
    private static final int CLIENT_PLUGIN_AUTH = 524288;
    private static final int CLIENT_CONNECT_ATTRS = 1048576;
    private static final int CLIENT_PLUGIN_AUTH_LENENC_CLIENT_DATA = 2097152;
    private static final int CLIENT_CAN_HANDLE_EXPIRED_PASSWORD = 4194304;
    private static final int CLIENT_SESSION_TRACK = 8388608;
    private static final int CLIENT_DEPRECATE_EOF = 16777216;
    private static final int OB_CLIENT_SUPPORT_ORACLE_MODE = 134217728;
    private static final int OB_SERVER_USE_ORACLE_MODE = 4;
    private static final int OB_CLIENT_SUPPORT_LOB_LOCATOR = 536870912;
    private static final int SERVER_SESSION_STATE_CHANGED = 16384;
    private static final int SESSION_TRACK_SYSTEM_VARIABLES = 0;
    private static final int SESSION_TRACK_SCHEMA = 1;
    private static final int SESSION_TRACK_STATE_CHANGE = 2;
    private static final String OB_PROXY_PARTITION_HIT = "ob_proxy_partition_hit";
    private static final String OB_STATEMENT_TRACE_ID = "ob_statement_trace_id";
    private static final String OB_CAPABILITY_FLAG = "ob_capability_flag";
    private static final String OB_CLIENT_FEEDBACK = "ob_client_feedback";
    private static final long OB_CAP_VIRTUAL_COMPRESS = 8L;
    private static final long OB_CAP_VIRTUAL_COMPRESS_SWITCH = 64L;
    private static final long OB_CAP_OCJ_ENABLE_EXTRA_OK_PACKET = 128L;
    private long obCapabilityFlag;
    private boolean useVirtualCompress;
    private boolean isVirtualCompressSwitchOn;
    private boolean comrepssWithRawData;
    private boolean enableExtraOkPacket;
    private boolean isPartitionHit;
    private boolean enableAbundantFeedback;
    private String serverTraceId;
    private boolean inAuthProcess;
    private ObFeedbackManager fbMgr;
    private static final long OB_CAP_USE_OCEANBASE_2_0_PROTOCOL = 256L;
    private static final long OB_CAP_ABUNDANT_FEEDBACK = 1024L;
    private int maxAllowedPacket;
    protected int maxThreeBytes;
    private int maxThreeBytesAfterV408;
    private int halfMaxThreeBytes;
    private boolean useObProto20;
    private byte proto20PacketSeq;
    private boolean useOracleMode;
    private boolean useOracleLocator;
    private int requestId;
    private boolean proto20PacketWithoutChecksum;
    private static final long OB_CAPABILITY_FLAG_VALUE = 1480L;
    private static final int SERVER_STATUS_IN_TRANS = 1;
    private static final int SERVER_STATUS_AUTOCOMMIT = 2;
    static final int SERVER_MORE_RESULTS_EXISTS = 8;
    private static final int SERVER_QUERY_NO_GOOD_INDEX_USED = 16;
    private static final int SERVER_QUERY_NO_INDEX_USED = 32;
    private static final int SERVER_QUERY_WAS_SLOW = 2048;
    private static final int SERVER_STATUS_CURSOR_EXISTS = 64;
    private static final String FALSE_SCRAMBLE = "xxxxxxxx";
    protected static final int MAX_QUERY_SIZE_TO_LOG = 1024;
    protected static final int MAX_QUERY_SIZE_TO_EXPLAIN = 1048576;
    protected static final int INITIAL_PACKET_SIZE = 1024;
    private static String jvmPlatformCharset;
    protected static final String ZERO_DATE_VALUE_MARKER = "0000-00-00";
    protected static final String ZERO_DATETIME_VALUE_MARKER = "0000-00-00 00:00:00";
    private static final String EXPLAINABLE_STATEMENT = "SELECT";
    private static final String[] EXPLAINABLE_STATEMENT_EXTENSION;
    private static final int MAX_PACKET_DUMP_LENGTH = 1024;
    private boolean packetSequenceReset;
    protected int serverCharsetIndex;
    private Buffer reusablePacket;
    private Buffer extraOkPacaket;
    private Buffer sendPacket;
    private Buffer sharedSendPacket;
    protected BufferedOutputStream mysqlOutput;
    protected MySQLConnection connection;
    private Deflater deflater;
    protected InputStream mysqlInput;
    private LinkedList<StringBuilder> packetDebugRingBuffer;
    private RowData streamingData;
    public Socket mysqlConnection;
    protected SocketFactory socketFactory;
    private SoftReference<Buffer> loadFileBufRef;
    private SoftReference<Buffer> splitBufRef;
    private SoftReference<Buffer> protoBufRef;
    protected String host;
    protected String seed;
    private String serverVersion;
    private String socketFactoryClassName;
    private byte[] packetHeaderBuf;
    private boolean colDecimalNeedsBump;
    private boolean hadWarnings;
    private boolean has41NewNewProt;
    private boolean hasLongColumnInfo;
    private boolean isInteractiveClient;
    private boolean logSlowQueries;
    private boolean platformDbCharsetMatches;
    private boolean profileSql;
    private boolean queryBadIndexUsed;
    private boolean queryNoIndexUsed;
    private boolean serverQueryWasSlow;
    private boolean use41Extensions;
    private boolean useCompression;
    private boolean useNewLargePackets;
    private boolean useNewUpdateCounts;
    private byte packetSequence;
    private byte compressedPacketSequence;
    private byte readPacketSequence;
    private boolean checkPacketSequence;
    private byte protocolVersion;
    protected int port;
    protected int serverCapabilities;
    private int serverMajorVersion;
    private int serverMinorVersion;
    private int oldServerStatus;
    private int serverStatus;
    private int serverSubMinorVersion;
    private int warningCount;
    protected long clientParam;
    protected long lastPacketSentTimeMs;
    protected long lastPacketReceivedTimeMs;
    private boolean traceProtocol;
    private boolean enablePacketDebug;
    private boolean useConnectWithDb;
    private boolean needToGrabQueryFromPacket;
    private boolean autoGenerateTestcaseScript;
    private long threadId;
    private boolean useNanosForElapsedTime;
    private long slowQueryThreshold;
    private String queryTimingUnits;
    private boolean useDirectRowUnpack;
    private int useBufferRowSizeThreshold;
    private int commandCount;
    private List<StatementInterceptorV2> statementInterceptors;
    private ExceptionInterceptor exceptionInterceptor;
    private int authPluginDataLength;
    private int maxPacketLenForProto20;
    private String localConnectionIdString;
    static AtomicInteger connectionSeq;
    private Map<String, AuthenticationPlugin> authenticationPlugins;
    private List<String> disabledAuthenticationPlugins;
    private String clientDefaultAuthenticationPlugin;
    private String clientDefaultAuthenticationPluginName;
    private String serverDefaultAuthenticationPluginName;
    private int statementExecutionDepth;
    private boolean useAutoSlowLog;
    
    static String getLocalConnectionId(final long ip, final int port) {
        BigInteger id = new BigInteger("0");
        final BigInteger bigIp = new BigInteger(String.valueOf(ip));
        final BigInteger bigPort = new BigInteger(String.valueOf(port & 0xFFFF));
        final BigInteger bigSeq = new BigInteger(String.valueOf(MysqlIO.connectionSeq.getAndIncrement() & 0xFFFF));
        id = id.add(bigIp.shiftLeft(32)).add(bigPort.shiftLeft(16)).add(bigSeq);
        return id.toString();
    }
    
    private void incRequestId() {
        ++this.requestId;
        if (this.requestId > this.maxThreeBytesAfterV408) {
            this.requestId = 0;
        }
    }
    
    public MysqlIO(final String host, final int port, final Properties props, final String socketFactoryClassName, final MySQLConnection conn, final int socketTimeout, final int useBufferRowSizeThreshold) throws IOException, SQLException {
        this.obCapabilityFlag = 0L;
        this.useVirtualCompress = false;
        this.isVirtualCompressSwitchOn = false;
        this.comrepssWithRawData = false;
        this.enableExtraOkPacket = false;
        this.isPartitionHit = true;
        this.enableAbundantFeedback = false;
        this.serverTraceId = "";
        this.inAuthProcess = true;
        this.fbMgr = new ObFeedbackManager();
        this.maxAllowedPacket = 1048576;
        this.maxThreeBytes = 16581375;
        this.maxThreeBytesAfterV408 = 16777215;
        this.halfMaxThreeBytes = this.maxThreeBytesAfterV408 / 2;
        this.useObProto20 = false;
        this.proto20PacketSeq = 0;
        this.useOracleMode = false;
        this.useOracleLocator = false;
        this.requestId = MysqlCommonUtils.getRandomNum(0, this.maxThreeBytesAfterV408);
        this.proto20PacketWithoutChecksum = false;
        this.packetSequenceReset = false;
        this.reusablePacket = null;
        this.extraOkPacaket = null;
        this.sendPacket = null;
        this.sharedSendPacket = null;
        this.mysqlOutput = null;
        this.deflater = null;
        this.mysqlInput = null;
        this.packetDebugRingBuffer = null;
        this.streamingData = null;
        this.mysqlConnection = null;
        this.socketFactory = null;
        this.host = null;
        this.serverVersion = null;
        this.socketFactoryClassName = null;
        this.packetHeaderBuf = new byte[4];
        this.colDecimalNeedsBump = false;
        this.hadWarnings = false;
        this.has41NewNewProt = false;
        this.hasLongColumnInfo = false;
        this.isInteractiveClient = false;
        this.logSlowQueries = false;
        this.platformDbCharsetMatches = true;
        this.profileSql = false;
        this.queryBadIndexUsed = false;
        this.queryNoIndexUsed = false;
        this.serverQueryWasSlow = false;
        this.use41Extensions = false;
        this.useCompression = false;
        this.useNewLargePackets = false;
        this.useNewUpdateCounts = false;
        this.packetSequence = 0;
        this.compressedPacketSequence = 0;
        this.readPacketSequence = -1;
        this.checkPacketSequence = false;
        this.protocolVersion = 0;
        this.port = 3306;
        this.serverMajorVersion = 0;
        this.serverMinorVersion = 0;
        this.oldServerStatus = 0;
        this.serverStatus = 0;
        this.serverSubMinorVersion = 0;
        this.warningCount = 0;
        this.clientParam = 0L;
        this.lastPacketSentTimeMs = 0L;
        this.lastPacketReceivedTimeMs = 0L;
        this.traceProtocol = false;
        this.enablePacketDebug = false;
        this.useDirectRowUnpack = true;
        this.commandCount = 0;
        this.authPluginDataLength = 0;
        this.maxPacketLenForProto20 = this.maxThreeBytesAfterV408 - 28;
        this.localConnectionIdString = "";
        this.authenticationPlugins = null;
        this.disabledAuthenticationPlugins = null;
        this.clientDefaultAuthenticationPlugin = null;
        this.clientDefaultAuthenticationPluginName = null;
        this.serverDefaultAuthenticationPluginName = null;
        this.statementExecutionDepth = 0;
        this.connection = conn;
        if (this.connection.getEnablePacketDebug()) {
            this.packetDebugRingBuffer = new LinkedList<StringBuilder>();
        }
        this.traceProtocol = this.connection.getTraceProtocol();
        this.useAutoSlowLog = this.connection.getAutoSlowLog();
        this.useBufferRowSizeThreshold = useBufferRowSizeThreshold;
        this.useDirectRowUnpack = this.connection.getUseDirectRowUnpack();
        this.logSlowQueries = this.connection.getLogSlowQueries();
        this.reusablePacket = new Buffer(1024);
        this.extraOkPacaket = new Buffer(1024);
        this.sendPacket = new Buffer(1024);
        this.port = port;
        this.host = host;
        this.socketFactoryClassName = socketFactoryClassName;
        this.socketFactory = this.createSocketFactory();
        this.exceptionInterceptor = this.connection.getExceptionInterceptor();
        try {
            this.mysqlConnection = this.socketFactory.connect(this.host, this.port, props);
            if (socketTimeout != 0) {
                try {
                    this.mysqlConnection.setSoTimeout(socketTimeout);
                }
                catch (Exception ex) {}
            }
            this.mysqlConnection = this.socketFactory.beforeHandshake();
            if (this.connection.getUseReadAheadInput()) {
                this.mysqlInput = new ReadAheadInputStream(this.mysqlConnection.getInputStream(), 16384, this.connection.getTraceProtocol(), this.connection.getLog());
            }
            else if (this.connection.useUnbufferedInput()) {
                this.mysqlInput = this.mysqlConnection.getInputStream();
            }
            else {
                this.mysqlInput = new BufferedInputStream(this.mysqlConnection.getInputStream(), 16384);
            }
            this.mysqlOutput = new BufferedOutputStream(this.mysqlConnection.getOutputStream(), 16384);
            this.isInteractiveClient = this.connection.getInteractiveClient();
            this.profileSql = this.connection.getProfileSql();
            this.autoGenerateTestcaseScript = this.connection.getAutoGenerateTestcaseScript();
            this.needToGrabQueryFromPacket = (this.profileSql || this.logSlowQueries || this.autoGenerateTestcaseScript);
            if (this.connection.getUseNanosForElapsedTime() && TimeUtil.nanoTimeAvailable()) {
                this.useNanosForElapsedTime = true;
                this.queryTimingUnits = Messages.getString("Nanoseconds");
            }
            else {
                this.queryTimingUnits = Messages.getString("Milliseconds");
            }
            if (this.connection.getLogSlowQueries()) {
                this.calculateSlowQueryThreshold();
            }
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, 0L, 0L, ioEx, this.getExceptionInterceptor());
        }
    }
    
    public boolean hasLongColumnInfo() {
        return this.hasLongColumnInfo;
    }
    
    protected boolean isDataAvailable() throws SQLException {
        try {
            return this.mysqlInput.available() > 0;
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }
    
    protected long getLastPacketSentTimeMs() {
        return this.lastPacketSentTimeMs;
    }
    
    protected long getLastPacketReceivedTimeMs() {
        return this.lastPacketReceivedTimeMs;
    }
    
    protected ResultSetImpl getResultSet(final StatementImpl callingStatement, final long columnCount, final int maxRows, final int resultSetType, final int resultSetConcurrency, final boolean streamResults, final String catalog, final boolean isBinaryEncoded, final Field[] metadataFromCache) throws SQLException {
        Field[] fields = null;
        if (metadataFromCache == null) {
            fields = new Field[(int)columnCount];
            for (int i = 0; i < columnCount; ++i) {
                Buffer fieldPacket = null;
                fieldPacket = this.readPacket();
                fields[i] = this.unpackField(fieldPacket, false);
            }
        }
        else {
            for (int i = 0; i < columnCount; ++i) {
                this.skipPacket();
            }
        }
        if (!this.isEOFDeprecated() || (this.connection.versionMeetsMinimum(5, 0, 2) && callingStatement != null && isBinaryEncoded && callingStatement.isCursorRequired())) {
            final Buffer packet = this.reuseAndReadPacket(this.reusablePacket);
            this.readServerStatusForResultSets(packet);
        }
        if (this.connection.versionMeetsMinimum(5, 0, 2) && this.connection.getUseCursorFetch() && isBinaryEncoded && callingStatement != null && callingStatement.getFetchSize() != 0 && callingStatement.getResultSetType() == 1003) {
            final ServerPreparedStatement prepStmt = (ServerPreparedStatement)callingStatement;
            boolean usingCursor = true;
            if (this.connection.versionMeetsMinimum(5, 0, 5)) {
                usingCursor = ((this.serverStatus & 0x40) != 0x0);
            }
            if (usingCursor) {
                final RowData rows = new RowDataCursor(this, prepStmt, fields);
                final ResultSetImpl rs = this.buildResultSetWithRows(callingStatement, catalog, fields, rows, resultSetType, resultSetConcurrency, isBinaryEncoded);
                if (usingCursor) {
                    rs.setFetchSize(callingStatement.getFetchSize());
                }
                return rs;
            }
        }
        RowData rowData = null;
        if (!streamResults) {
            rowData = this.readSingleRowSet(columnCount, maxRows, resultSetConcurrency, isBinaryEncoded, (metadataFromCache == null) ? fields : metadataFromCache);
        }
        else {
            rowData = new RowDataDynamic(this, (int)columnCount, (metadataFromCache == null) ? fields : metadataFromCache, isBinaryEncoded);
            this.streamingData = rowData;
        }
        final ResultSetImpl rs2 = this.buildResultSetWithRows(callingStatement, catalog, (metadataFromCache == null) ? fields : metadataFromCache, rowData, resultSetType, resultSetConcurrency, isBinaryEncoded);
        return rs2;
    }
    
    protected NetworkResources getNetworkResources() {
        return new NetworkResources(this.mysqlConnection, this.mysqlInput, this.mysqlOutput);
    }
    
    protected final void forceClose() {
        try {
            this.getNetworkResources().forceClose();
        }
        finally {
            this.mysqlConnection = null;
            this.mysqlInput = null;
            this.mysqlOutput = null;
        }
    }
    
    protected final void skipPacket() throws SQLException {
        try {
            final int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
            if (lengthRead < 4) {
                this.forceClose();
                throw new IOException(Messages.getString("MysqlIO.1"));
            }
            final int packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
            if (this.traceProtocol) {
                final StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.2"));
                traceMessageBuf.append(packetLength);
                traceMessageBuf.append(Messages.getString("MysqlIO.3"));
                traceMessageBuf.append(StringUtils.dumpAsHex(this.packetHeaderBuf, 4));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            final byte multiPacketSeq = this.packetHeaderBuf[3];
            if (!this.packetSequenceReset) {
                if (this.enablePacketDebug && this.checkPacketSequence) {
                    this.checkPacketSequencing(multiPacketSeq);
                }
            }
            else {
                this.packetSequenceReset = false;
            }
            this.readPacketSequence = multiPacketSeq;
            this.skipFully(this.mysqlInput, packetLength);
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
        catch (OutOfMemoryError oom) {
            try {
                this.connection.realClose(false, false, true, oom);
            }
            catch (Exception ex) {}
            throw oom;
        }
    }
    
    protected final Buffer readPacket() throws SQLException {
        try {
            final int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
            if (lengthRead < 4) {
                this.forceClose();
                throw new IOException(Messages.getString("MysqlIO.1"));
            }
            final int packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
            if (packetLength > this.maxAllowedPacket) {
                throw new PacketTooBigException(packetLength, this.maxAllowedPacket);
            }
            if (this.traceProtocol) {
                final StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.2"));
                traceMessageBuf.append(packetLength);
                traceMessageBuf.append(Messages.getString("MysqlIO.3"));
                traceMessageBuf.append(StringUtils.dumpAsHex(this.packetHeaderBuf, 4));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            final byte multiPacketSeq = this.packetHeaderBuf[3];
            if (!this.packetSequenceReset) {
                if (this.enablePacketDebug && this.checkPacketSequence) {
                    this.checkPacketSequencing(multiPacketSeq);
                }
            }
            else {
                this.packetSequenceReset = false;
            }
            this.readPacketSequence = multiPacketSeq;
            final byte[] buffer = new byte[packetLength];
            final int numBytesRead = this.readFully(this.mysqlInput, buffer, 0, packetLength);
            if (numBytesRead != packetLength) {
                throw new IOException("Short read, expected " + packetLength + " bytes, only read " + numBytesRead);
            }
            final Buffer packet = new Buffer(buffer);
            if (this.traceProtocol) {
                final StringBuilder traceMessageBuf2 = new StringBuilder();
                traceMessageBuf2.append(Messages.getString("MysqlIO.4"));
                traceMessageBuf2.append(getPacketDumpToLog(packet, packetLength));
                this.connection.getLog().logTrace(traceMessageBuf2.toString());
            }
            if (this.enablePacketDebug) {
                this.enqueuePacketForDebugging(false, false, 0, this.packetHeaderBuf, packet);
            }
            if (this.connection.getMaintainTimeStats()) {
                this.lastPacketReceivedTimeMs = System.currentTimeMillis();
            }
            return packet;
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
        catch (OutOfMemoryError oom) {
            try {
                this.connection.realClose(false, false, true, oom);
            }
            catch (Exception ex) {}
            throw oom;
        }
    }
    
    protected final Field unpackField(final Buffer packet, final boolean extractDefaultValues) throws SQLException {
        if (this.use41Extensions) {
            if (this.has41NewNewProt) {
                int catalogNameStart = packet.getPosition() + 1;
                final int catalogNameLength = packet.fastSkipLenString();
                catalogNameStart = this.adjustStartForFieldLength(catalogNameStart, catalogNameLength);
            }
            int databaseNameStart = packet.getPosition() + 1;
            final int databaseNameLength = packet.fastSkipLenString();
            databaseNameStart = this.adjustStartForFieldLength(databaseNameStart, databaseNameLength);
            int tableNameStart = packet.getPosition() + 1;
            final int tableNameLength = packet.fastSkipLenString();
            tableNameStart = this.adjustStartForFieldLength(tableNameStart, tableNameLength);
            int originalTableNameStart = packet.getPosition() + 1;
            final int originalTableNameLength = packet.fastSkipLenString();
            originalTableNameStart = this.adjustStartForFieldLength(originalTableNameStart, originalTableNameLength);
            int nameStart = packet.getPosition() + 1;
            final int nameLength = packet.fastSkipLenString();
            nameStart = this.adjustStartForFieldLength(nameStart, nameLength);
            int originalColumnNameStart = packet.getPosition() + 1;
            final int originalColumnNameLength = packet.fastSkipLenString();
            originalColumnNameStart = this.adjustStartForFieldLength(originalColumnNameStart, originalColumnNameLength);
            packet.readByte();
            final short charSetNumber = (short)packet.readInt();
            long colLength = 0L;
            if (this.has41NewNewProt) {
                colLength = packet.readLong();
            }
            else {
                colLength = packet.readLongInt();
            }
            final int colType = packet.readByte() & 0xFF;
            short colFlag = 0;
            if (this.hasLongColumnInfo) {
                colFlag = (short)packet.readInt();
            }
            else {
                colFlag = (short)(packet.readByte() & 0xFF);
            }
            final int colDecimals = packet.readByte() & 0xFF;
            final int precision = packet.readByte() & 0xFF;
            final int inoutType = packet.readByte() & 0xFF;
            int complexSchemaNameStart = -1;
            int complexSchemaNameLength = -1;
            int complexTypeNameStart = -1;
            int complexTypeNameLength = -1;
            int complexVersion = 0;
            if (160 == colType) {
                packet.setPosition(packet.getPosition());
                complexSchemaNameStart = packet.getPosition() + 1;
                complexSchemaNameLength = packet.fastSkipLenString();
                complexSchemaNameStart = this.adjustStartForFieldLength(complexSchemaNameStart, complexSchemaNameLength);
                complexTypeNameStart = packet.getPosition() + 1;
                complexTypeNameLength = packet.fastSkipLenString();
                complexTypeNameStart = this.adjustStartForFieldLength(complexTypeNameStart, complexTypeNameLength);
                complexVersion = (int)packet.readFieldLength();
            }
            int defaultValueStart = -1;
            int defaultValueLength = -1;
            if (extractDefaultValues) {
                defaultValueStart = packet.getPosition() + 1;
                defaultValueLength = packet.fastSkipLenString();
            }
            final Field field = new Field(this.connection, packet.getByteBuffer(), databaseNameStart, databaseNameLength, tableNameStart, tableNameLength, originalTableNameStart, originalTableNameLength, nameStart, nameLength, originalColumnNameStart, originalColumnNameLength, colLength, colType, colFlag, colDecimals, defaultValueStart, defaultValueLength, charSetNumber, complexSchemaNameStart, complexSchemaNameLength, complexTypeNameStart, complexTypeNameLength, complexVersion, precision, inoutType);
            return field;
        }
        int tableNameStart2 = packet.getPosition() + 1;
        final int tableNameLength2 = packet.fastSkipLenString();
        tableNameStart2 = this.adjustStartForFieldLength(tableNameStart2, tableNameLength2);
        int nameStart2 = packet.getPosition() + 1;
        final int nameLength2 = packet.fastSkipLenString();
        nameStart2 = this.adjustStartForFieldLength(nameStart2, nameLength2);
        final int colLength2 = packet.readnBytes();
        final int colType2 = packet.readnBytes();
        packet.readByte();
        short colFlag2 = 0;
        if (this.hasLongColumnInfo) {
            colFlag2 = (short)packet.readInt();
        }
        else {
            colFlag2 = (short)(packet.readByte() & 0xFF);
        }
        int colDecimals2 = packet.readByte() & 0xFF;
        if (this.colDecimalNeedsBump) {
            ++colDecimals2;
        }
        final Field field2 = new Field(this.connection, packet.getByteBuffer(), nameStart2, nameLength2, tableNameStart2, tableNameLength2, colLength2, colType2, colFlag2, colDecimals2);
        return field2;
    }
    
    private int adjustStartForFieldLength(final int nameStart, final int nameLength) {
        if (nameLength < 251) {
            return nameStart;
        }
        if (nameLength >= 251 && nameLength < 65536) {
            return nameStart + 2;
        }
        if (nameLength >= 65536 && nameLength < 16777216) {
            return nameStart + 3;
        }
        return nameStart + 8;
    }
    
    protected boolean isSetNeededForAutoCommitMode(final boolean autoCommitFlag) {
        if (!this.use41Extensions || !this.connection.getElideSetAutoCommits()) {
            return true;
        }
        final boolean autoCommitModeOnServer = (this.serverStatus & 0x2) != 0x0;
        if (!autoCommitFlag && this.versionMeetsMinimum(5, 0, 0)) {
            return !this.inTransactionOnServer();
        }
        return autoCommitModeOnServer != autoCommitFlag;
    }
    
    protected boolean inTransactionOnServer() {
        return (this.serverStatus & 0x1) != 0x0;
    }
    
    protected void changeUser(final String userName, final String password, final String database) throws SQLException {
        this.packetSequence = -1;
        this.compressedPacketSequence = -1;
        final int passwordLength = 16;
        final int userLength = (userName != null) ? userName.length() : 0;
        final int databaseLength = (database != null) ? database.length() : 0;
        final int packLength = (userLength + passwordLength + databaseLength) * 3 + 7 + 4 + 33;
        if ((this.serverCapabilities & 0x80000) != 0x0) {
            this.proceedHandshakeWithPluggableAuthentication(userName, password, database, null);
        }
        else if ((this.serverCapabilities & 0x8000) != 0x0) {
            final Buffer changeUserPacket = new Buffer(packLength + 1);
            changeUserPacket.writeByte((byte)17);
            if (this.versionMeetsMinimum(4, 1, 1)) {
                this.secureAuth411(changeUserPacket, packLength, userName, password, database, false);
            }
            else {
                this.secureAuth(changeUserPacket, packLength, userName, password, database, false);
            }
        }
        else {
            final Buffer packet = new Buffer(packLength);
            packet.writeByte((byte)17);
            packet.writeString(userName);
            if (this.protocolVersion > 9) {
                packet.writeString(Util.newCrypt(password, this.seed, this.connection.getPasswordCharacterEncoding()));
            }
            else {
                packet.writeString(Util.oldCrypt(password, this.seed));
            }
            final boolean localUseConnectWithDb = this.useConnectWithDb && database != null && database.length() > 0;
            if (localUseConnectWithDb) {
                packet.writeString(database);
            }
            this.send(packet, packet.getPosition());
            this.checkErrorPacket();
            if (!localUseConnectWithDb) {
                this.changeDatabaseTo(database);
            }
        }
    }
    
    protected Buffer checkErrorPacket() throws SQLException {
        return this.checkErrorPacket(-1);
    }
    
    protected void checkForCharsetMismatch() {
        if (this.connection.getUseUnicode() && this.connection.getEncoding() != null) {
            String encodingToCheck = MysqlIO.jvmPlatformCharset;
            if (encodingToCheck == null) {
                encodingToCheck = System.getProperty("file.encoding");
            }
            if (encodingToCheck == null) {
                this.platformDbCharsetMatches = false;
            }
            else {
                this.platformDbCharsetMatches = encodingToCheck.equals(this.connection.getEncoding());
            }
        }
    }
    
    protected void clearInputStream() throws SQLException {
        try {
            int len;
            while ((len = this.mysqlInput.available()) > 0 && this.mysqlInput.skip(len) > 0L) {}
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }
    
    protected void resetReadPacketSequence() {
        this.readPacketSequence = 0;
    }
    
    protected void dumpPacketRingBuffer() throws SQLException {
        if (this.packetDebugRingBuffer != null && this.connection.getEnablePacketDebug()) {
            final StringBuilder dumpBuffer = new StringBuilder();
            dumpBuffer.append("Last " + this.packetDebugRingBuffer.size() + " packets received from server, from oldest->newest:\n");
            dumpBuffer.append("\n");
            final Iterator<StringBuilder> ringBufIter = this.packetDebugRingBuffer.iterator();
            while (ringBufIter.hasNext()) {
                dumpBuffer.append((CharSequence)ringBufIter.next());
                dumpBuffer.append("\n");
            }
            this.connection.getLog().logTrace(dumpBuffer.toString());
        }
    }
    
    protected void explainSlowQuery(final byte[] querySQL, final String truncatedQuery) throws SQLException {
        if (StringUtils.startsWithIgnoreCaseAndWs(truncatedQuery, "SELECT") || (this.versionMeetsMinimum(5, 6, 3) && StringUtils.startsWithIgnoreCaseAndWs(truncatedQuery, MysqlIO.EXPLAINABLE_STATEMENT_EXTENSION) != -1)) {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = (PreparedStatement)this.connection.clientPrepareStatement("EXPLAIN ?");
                stmt.setBytesNoEscapeNoQuotes(1, querySQL);
                rs = stmt.executeQuery();
                final StringBuilder explainResults = new StringBuilder(Messages.getString("MysqlIO.8") + truncatedQuery + Messages.getString("MysqlIO.9"));
                ResultSetUtil.appendResultSetSlashGStyle(explainResults, rs);
                this.connection.getLog().logWarn(explainResults.toString());
            }
            catch (SQLException ex) {}
            finally {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
            }
        }
    }
    
    static int getMaxBuf() {
        return MysqlIO.maxBufferSize;
    }
    
    final int getServerMajorVersion() {
        return this.serverMajorVersion;
    }
    
    final int getServerMinorVersion() {
        return this.serverMinorVersion;
    }
    
    final int getServerSubMinorVersion() {
        return this.serverSubMinorVersion;
    }
    
    String getServerVersion() {
        return this.serverVersion;
    }
    
    void doHandshake(final String user, final String password, final String database) throws SQLException {
        this.checkPacketSequence = false;
        this.readPacketSequence = 0;
        final Buffer buf = this.readPacket();
        this.protocolVersion = buf.readByte();
        if (this.protocolVersion == -1) {
            try {
                this.mysqlConnection.close();
            }
            catch (Exception ex) {}
            int errno = 2000;
            errno = buf.readInt();
            final String serverErrorMessage = buf.readString("ASCII", this.getExceptionInterceptor());
            final StringBuilder errorBuf = new StringBuilder(Messages.getString("MysqlIO.10"));
            errorBuf.append(serverErrorMessage);
            errorBuf.append("\"");
            final String xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
            throw SQLError.createSQLException(SQLError.get(xOpen) + ", " + errorBuf.toString(), xOpen, errno, this.getExceptionInterceptor());
        }
        this.serverVersion = buf.readString("ASCII", this.getExceptionInterceptor());
        int point = this.serverVersion.indexOf(46);
        if (point != -1) {
            try {
                final int n = Integer.parseInt(this.serverVersion.substring(0, point));
                this.serverMajorVersion = n;
            }
            catch (NumberFormatException ex2) {}
            String remaining = this.serverVersion.substring(point + 1, this.serverVersion.length());
            point = remaining.indexOf(46);
            if (point != -1) {
                try {
                    final int n2 = Integer.parseInt(remaining.substring(0, point));
                    this.serverMinorVersion = n2;
                }
                catch (NumberFormatException ex3) {}
                int pos;
                for (remaining = remaining.substring(point + 1, remaining.length()), pos = 0; pos < remaining.length() && remaining.charAt(pos) >= '0'; ++pos) {
                    if (remaining.charAt(pos) > '9') {
                        break;
                    }
                }
                try {
                    final int n3 = Integer.parseInt(remaining.substring(0, pos));
                    this.serverSubMinorVersion = n3;
                }
                catch (NumberFormatException ex4) {}
            }
        }
        if (this.versionMeetsMinimum(4, 0, 8)) {
            this.maxThreeBytes = 16777215;
            this.useNewLargePackets = true;
        }
        else {
            this.maxThreeBytes = 16581375;
            this.useNewLargePackets = false;
        }
        this.colDecimalNeedsBump = this.versionMeetsMinimum(3, 23, 0);
        this.colDecimalNeedsBump = !this.versionMeetsMinimum(3, 23, 15);
        this.useNewUpdateCounts = this.versionMeetsMinimum(3, 22, 5);
        this.threadId = buf.readLong();
        if (this.protocolVersion > 9) {
            this.seed = buf.readString("ASCII", this.getExceptionInterceptor(), 8);
            buf.readByte();
        }
        else {
            this.seed = buf.readString("ASCII", this.getExceptionInterceptor());
        }
        this.serverCapabilities = 0;
        if (buf.getPosition() < buf.getBufLength()) {
            this.serverCapabilities = buf.readInt();
        }
        if (this.versionMeetsMinimum(4, 1, 1) || (this.protocolVersion > 9 && (this.serverCapabilities & 0x200) != 0x0)) {
            this.serverCharsetIndex = (buf.readByte() & 0xFF);
            this.serverStatus = buf.readInt();
            this.checkTransactionState(0);
            this.serverCapabilities |= buf.readInt() << 16;
            if ((this.serverCapabilities & 0x80000) != 0x0) {
                this.authPluginDataLength = (buf.readByte() & 0xFF);
            }
            else {
                buf.readByte();
            }
            buf.setPosition(buf.getPosition() + 10);
            if ((this.serverCapabilities & 0x8000) != 0x0) {
                String seedPart2;
                StringBuilder newSeed;
                if (this.authPluginDataLength > 0) {
                    seedPart2 = buf.readString("ASCII", this.getExceptionInterceptor(), this.authPluginDataLength - 8);
                    newSeed = new StringBuilder(this.authPluginDataLength);
                }
                else {
                    seedPart2 = buf.readString("ASCII", this.getExceptionInterceptor());
                    newSeed = new StringBuilder(20);
                }
                newSeed.append(this.seed);
                newSeed.append(seedPart2);
                this.seed = newSeed.toString();
            }
        }
        if ((this.serverCapabilities & 0x20) != 0x0 && this.connection.getUseCompression()) {
            this.clientParam |= 0x20L;
        }
        this.useConnectWithDb = (database != null && database.length() > 0 && !this.connection.getCreateDatabaseIfNotExist());
        if (this.useConnectWithDb) {
            this.clientParam |= 0x8L;
        }
        if (this.versionMeetsMinimum(5, 7, 0) && !this.connection.getUseSSL() && !this.connection.isUseSSLExplicit()) {
            this.connection.setUseSSL(true);
            this.connection.setVerifyServerCertificate(false);
            this.connection.getLog().logWarn(Messages.getString("MysqlIO.SSLWarning"));
        }
        if ((this.serverCapabilities & 0x800) == 0x0 && this.connection.getUseSSL()) {
            if (this.connection.getRequireSSL()) {
                this.connection.close();
                this.forceClose();
                throw SQLError.createSQLException(Messages.getString("MysqlIO.15"), "08001", this.getExceptionInterceptor());
            }
            this.connection.setUseSSL(false);
        }
        if ((this.serverCapabilities & 0x4) != 0x0) {
            this.clientParam |= 0x4L;
            this.hasLongColumnInfo = true;
        }
        if (!this.connection.getUseAffectedRows()) {
            this.clientParam |= 0x2L;
        }
        if (this.connection.getAllowLoadLocalInfile()) {
            this.clientParam |= 0x80L;
        }
        if (this.isInteractiveClient) {
            this.clientParam |= 0x400L;
        }
        if ((this.serverCapabilities & 0x800000) != 0x0) {}
        if ((this.serverCapabilities & 0x1000000) != 0x0) {
            this.clientParam |= 0x1000000L;
        }
        this.clientParam |= 0x8000000L;
        if (this.connection.getSupportLobLocator()) {
            this.clientParam |= 0x20000000L;
            this.useOracleLocator = true;
        }
        if ((this.serverCapabilities & 0x80000) != 0x0) {
            this.proceedHandshakeWithPluggableAuthentication(user, password, database, buf);
            return;
        }
        if (this.protocolVersion > 9) {
            this.clientParam |= 0x1L;
        }
        else {
            this.clientParam &= 0xFFFFFFFFFFFFFFFEL;
        }
        if (this.versionMeetsMinimum(4, 1, 0) || (this.protocolVersion > 9 && (this.serverCapabilities & 0x4000) != 0x0)) {
            if (this.versionMeetsMinimum(4, 1, 1) || (this.protocolVersion > 9 && (this.serverCapabilities & 0x200) != 0x0)) {
                this.clientParam |= 0x200L;
                this.has41NewNewProt = true;
                this.clientParam |= 0x2000L;
                this.clientParam |= 0x20000L;
                if (this.connection.getAllowMultiQueries()) {
                    this.clientParam |= 0x10000L;
                }
            }
            else {
                this.clientParam |= 0x4000L;
                this.has41NewNewProt = false;
            }
            this.use41Extensions = true;
        }
        final int passwordLength = 16;
        final int userLength = (user != null) ? user.length() : 0;
        final int databaseLength = (database != null) ? database.length() : 0;
        final int packLength = (userLength + passwordLength + databaseLength) * 3 + 7 + 4 + 33;
        Buffer packet = null;
        if (!this.connection.getUseSSL()) {
            if ((this.serverCapabilities & 0x8000) != 0x0) {
                this.clientParam |= 0x8000L;
                if (this.versionMeetsMinimum(4, 1, 1) || (this.protocolVersion > 9 && (this.serverCapabilities & 0x200) != 0x0)) {
                    this.secureAuth411(null, packLength, user, password, database, true);
                }
                else {
                    this.secureAuth(null, packLength, user, password, database, true);
                }
            }
            else {
                packet = new Buffer(packLength);
                if ((this.clientParam & 0x4000L) != 0x0L) {
                    if (this.versionMeetsMinimum(4, 1, 1) || (this.protocolVersion > 9 && (this.serverCapabilities & 0x200) != 0x0)) {
                        packet.writeLong(this.clientParam);
                        packet.writeLong(this.maxThreeBytes);
                        packet.writeByte((byte)8);
                        packet.writeBytesNoNull(new byte[23]);
                    }
                    else {
                        packet.writeLong(this.clientParam);
                        packet.writeLong(this.maxThreeBytes);
                    }
                }
                else {
                    packet.writeInt((int)this.clientParam);
                    packet.writeLongInt(this.maxThreeBytes);
                }
                packet.writeString(user, "Cp1252", this.connection);
                if (this.protocolVersion > 9) {
                    packet.writeString(Util.newCrypt(password, this.seed, this.connection.getPasswordCharacterEncoding()), "Cp1252", this.connection);
                }
                else {
                    packet.writeString(Util.oldCrypt(password, this.seed), "Cp1252", this.connection);
                }
                if (this.useConnectWithDb) {
                    packet.writeString(database, "Cp1252", this.connection);
                }
                this.send(packet, packet.getPosition());
            }
        }
        else {
            this.negotiateSSLConnection(user, password, database, packLength);
            if ((this.serverCapabilities & 0x8000) != 0x0) {
                if (this.versionMeetsMinimum(4, 1, 1)) {
                    this.secureAuth411(null, packLength, user, password, database, true);
                }
                else {
                    this.secureAuth411(null, packLength, user, password, database, true);
                }
            }
            else {
                packet = new Buffer(packLength);
                if (this.use41Extensions) {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);
                }
                else {
                    packet.writeInt((int)this.clientParam);
                    packet.writeLongInt(this.maxThreeBytes);
                }
                packet.writeString(user);
                if (this.protocolVersion > 9) {
                    packet.writeString(Util.newCrypt(password, this.seed, this.connection.getPasswordCharacterEncoding()));
                }
                else {
                    packet.writeString(Util.oldCrypt(password, this.seed));
                }
                if ((this.serverCapabilities & 0x8) != 0x0 && database != null && database.length() > 0) {
                    packet.writeString(database);
                }
                this.send(packet, packet.getPosition());
            }
        }
        if (!this.versionMeetsMinimum(4, 1, 1) || this.protocolVersion <= 9 || (this.serverCapabilities & 0x200) == 0x0) {
            this.checkErrorPacket();
        }
        this.inAuthProcess = false;
        if (0x0L != (this.obCapabilityFlag & 0x80L)) {
            this.enableExtraOkPacket = true;
        }
        if (0x0L != (this.obCapabilityFlag & 0x400L)) {
            this.enableAbundantFeedback = true;
        }
        if ((this.obCapabilityFlag & 0x100L) != 0x0L) {
            this.useObProto20 = true;
            this.mysqlInput = new ObProto20InputStream(this.connection, this.mysqlInput, this.connection.getId());
        }
        else if ((this.serverCapabilities & 0x20) != 0x0 && this.connection.getUseCompression() && !(this.mysqlInput instanceof CompressedInputStream)) {
            this.deflater = new Deflater();
            this.useCompression = true;
            this.mysqlInput = new CompressedInputStream(this.connection, this.mysqlInput);
        }
        else {
            if ((this.obCapabilityFlag & 0x8L) != 0x0L) {
                this.useVirtualCompress = true;
            }
            if (this.useVirtualCompress) {
                this.deflater = new Deflater(0);
                this.useCompression = true;
                this.mysqlInput = new CompressedInputStream(this.connection, this.mysqlInput);
            }
        }
        if ((this.obCapabilityFlag & 0x40L) != 0x0L) {
            this.isVirtualCompressSwitchOn = true;
        }
        if (!this.useConnectWithDb) {
            this.changeDatabaseTo(database);
        }
        try {
            this.mysqlConnection = this.socketFactory.afterHandshake();
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }
    
    private void loadAuthenticationPlugins() throws SQLException {
        this.clientDefaultAuthenticationPlugin = this.connection.getDefaultAuthenticationPlugin();
        if (this.clientDefaultAuthenticationPlugin == null || "".equals(this.clientDefaultAuthenticationPlugin.trim())) {
            throw SQLError.createSQLException(Messages.getString("Connection.BadDefaultAuthenticationPlugin", new Object[] { this.clientDefaultAuthenticationPlugin }), this.getExceptionInterceptor());
        }
        final String disabledPlugins = this.connection.getDisabledAuthenticationPlugins();
        if (disabledPlugins != null && !"".equals(disabledPlugins)) {
            this.disabledAuthenticationPlugins = new ArrayList<String>();
            final List<String> pluginsToDisable = StringUtils.split(disabledPlugins, ",", true);
            final Iterator<String> iter = pluginsToDisable.iterator();
            while (iter.hasNext()) {
                this.disabledAuthenticationPlugins.add(iter.next());
            }
        }
        this.authenticationPlugins = new HashMap<String, AuthenticationPlugin>();
        AuthenticationPlugin plugin = new MysqlOldPasswordPlugin();
        plugin.init(this.connection, this.connection.getProperties());
        boolean defaultIsFound = this.addAuthenticationPlugin(plugin);
        plugin = new MysqlNativePasswordPlugin();
        plugin.init(this.connection, this.connection.getProperties());
        if (this.addAuthenticationPlugin(plugin)) {
            defaultIsFound = true;
        }
        plugin = new MysqlClearPasswordPlugin();
        plugin.init(this.connection, this.connection.getProperties());
        if (this.addAuthenticationPlugin(plugin)) {
            defaultIsFound = true;
        }
        plugin = new Sha256PasswordPlugin();
        plugin.init(this.connection, this.connection.getProperties());
        if (this.addAuthenticationPlugin(plugin)) {
            defaultIsFound = true;
        }
        final String authenticationPluginClasses = this.connection.getAuthenticationPlugins();
        if (authenticationPluginClasses != null && !"".equals(authenticationPluginClasses)) {
            final List<Extension> plugins = Util.loadExtensions(this.connection, this.connection.getProperties(), authenticationPluginClasses, "Connection.BadAuthenticationPlugin", this.getExceptionInterceptor());
            for (final Extension object : plugins) {
                plugin = (AuthenticationPlugin)object;
                if (this.addAuthenticationPlugin(plugin)) {
                    defaultIsFound = true;
                }
            }
        }
        if (!defaultIsFound) {
            throw SQLError.createSQLException(Messages.getString("Connection.DefaultAuthenticationPluginIsNotListed", new Object[] { this.clientDefaultAuthenticationPlugin }), this.getExceptionInterceptor());
        }
    }
    
    private boolean addAuthenticationPlugin(final AuthenticationPlugin plugin) throws SQLException {
        boolean isDefault = false;
        final String pluginClassName = plugin.getClass().getName();
        final String pluginProtocolName = plugin.getProtocolPluginName();
        final boolean disabledByClassName = this.disabledAuthenticationPlugins != null && this.disabledAuthenticationPlugins.contains(pluginClassName);
        final boolean disabledByMechanism = this.disabledAuthenticationPlugins != null && this.disabledAuthenticationPlugins.contains(pluginProtocolName);
        if (disabledByClassName || disabledByMechanism) {
            if (this.clientDefaultAuthenticationPlugin.equals(pluginClassName)) {
                throw SQLError.createSQLException(Messages.getString("Connection.BadDisabledAuthenticationPlugin", new Object[] { disabledByClassName ? pluginClassName : pluginProtocolName }), this.getExceptionInterceptor());
            }
        }
        else {
            this.authenticationPlugins.put(pluginProtocolName, plugin);
            if (this.clientDefaultAuthenticationPlugin.equals(pluginClassName)) {
                this.clientDefaultAuthenticationPluginName = pluginProtocolName;
                isDefault = true;
            }
        }
        return isDefault;
    }
    
    private AuthenticationPlugin getAuthenticationPlugin(final String pluginName) throws SQLException {
        AuthenticationPlugin plugin = this.authenticationPlugins.get(pluginName);
        if (plugin != null && !plugin.isReusable()) {
            try {
                plugin = (AuthenticationPlugin)plugin.getClass().newInstance();
                plugin.init(this.connection, this.connection.getProperties());
            }
            catch (Throwable t) {
                final SQLException sqlEx = SQLError.createSQLException(Messages.getString("Connection.BadAuthenticationPlugin", new Object[] { plugin.getClass().getName() }), this.getExceptionInterceptor());
                sqlEx.initCause(t);
                throw sqlEx;
            }
        }
        return plugin;
    }
    
    private void checkConfidentiality(final AuthenticationPlugin plugin) throws SQLException {
        if (plugin.requiresConfidentiality() && !this.isSSLEstablished()) {
            throw SQLError.createSQLException(Messages.getString("Connection.AuthenticationPluginRequiresSSL", new Object[] { plugin.getProtocolPluginName() }), this.getExceptionInterceptor());
        }
    }
    
    private void proceedHandshakeWithPluggableAuthentication(final String user, final String password, final String database, Buffer challenge) throws SQLException {
        if (this.authenticationPlugins == null) {
            this.loadAuthenticationPlugins();
        }
        boolean skipPassword = false;
        final int passwordLength = 16;
        final int userLength = (user != null) ? user.length() : 0;
        final int databaseLength = (database != null) ? database.length() : 0;
        final int packLength = (userLength + passwordLength + databaseLength) * 3 + 7 + 4 + 33;
        AuthenticationPlugin plugin = null;
        Buffer fromServer = null;
        final ArrayList<Buffer> toServer = new ArrayList<Buffer>();
        boolean done = false;
        Buffer last_sent = null;
        boolean old_raw_challenge = false;
        int counter = 100;
        while (0 < counter--) {
            if (!done) {
                if (challenge != null) {
                    if (challenge.isOKPacket()) {
                        throw SQLError.createSQLException(Messages.getString("Connection.UnexpectedAuthenticationApproval", new Object[] { plugin.getProtocolPluginName() }), this.getExceptionInterceptor());
                    }
                    this.clientParam |= 0xAA201L;
                    if (this.connection.getAllowMultiQueries()) {
                        this.clientParam |= 0x10000L;
                    }
                    if ((this.serverCapabilities & 0x400000) != 0x0 && !this.connection.getDisconnectOnExpiredPasswords()) {
                        this.clientParam |= 0x400000L;
                    }
                    if ((this.serverCapabilities & 0x100000) != 0x0 && !"none".equals(this.connection.getConnectionAttributes())) {
                        this.clientParam |= 0x100000L;
                    }
                    if ((this.serverCapabilities & 0x200000) != 0x0) {
                        this.clientParam |= 0x200000L;
                    }
                    this.has41NewNewProt = true;
                    this.use41Extensions = true;
                    if (this.connection.getUseSSL()) {
                        this.negotiateSSLConnection(user, password, database, packLength);
                    }
                    String pluginName = null;
                    if ((this.serverCapabilities & 0x80000) != 0x0) {
                        if (!this.versionMeetsMinimum(5, 5, 10) || (this.versionMeetsMinimum(5, 6, 0) && !this.versionMeetsMinimum(5, 6, 2))) {
                            pluginName = challenge.readString("ASCII", this.getExceptionInterceptor(), this.authPluginDataLength);
                        }
                        else {
                            pluginName = challenge.readString("ASCII", this.getExceptionInterceptor());
                        }
                    }
                    plugin = this.getAuthenticationPlugin(pluginName);
                    if (plugin == null) {
                        plugin = this.getAuthenticationPlugin(this.clientDefaultAuthenticationPluginName);
                    }
                    else if (pluginName.equals(Sha256PasswordPlugin.PLUGIN_NAME) && !this.isSSLEstablished() && this.connection.getServerRSAPublicKeyFile() == null && !this.connection.getAllowPublicKeyRetrieval()) {
                        plugin = this.getAuthenticationPlugin(this.clientDefaultAuthenticationPluginName);
                        skipPassword = !this.clientDefaultAuthenticationPluginName.equals(pluginName);
                    }
                    this.serverDefaultAuthenticationPluginName = plugin.getProtocolPluginName();
                    this.checkConfidentiality(plugin);
                    fromServer = new Buffer(StringUtils.getBytes(this.seed));
                }
                else {
                    plugin = this.getAuthenticationPlugin((this.serverDefaultAuthenticationPluginName == null) ? this.clientDefaultAuthenticationPluginName : this.serverDefaultAuthenticationPluginName);
                    this.checkConfidentiality(plugin);
                    fromServer = new Buffer(StringUtils.getBytes(this.seed));
                }
            }
            else {
                challenge = this.checkErrorPacket();
                old_raw_challenge = false;
                ++this.packetSequence;
                ++this.compressedPacketSequence;
                if (plugin == null) {
                    plugin = this.getAuthenticationPlugin((this.serverDefaultAuthenticationPluginName != null) ? this.serverDefaultAuthenticationPluginName : this.clientDefaultAuthenticationPluginName);
                }
                if (challenge.isOKPacket()) {
                    plugin.destroy();
                    break;
                }
                if (challenge.isAuthMethodSwitchRequestPacket()) {
                    skipPassword = false;
                    final String pluginName = challenge.readString("ASCII", this.getExceptionInterceptor());
                    if (!plugin.getProtocolPluginName().equals(pluginName)) {
                        plugin.destroy();
                        plugin = this.getAuthenticationPlugin(pluginName);
                        if (plugin == null) {
                            throw SQLError.createSQLException(Messages.getString("Connection.BadAuthenticationPlugin", new Object[] { pluginName }), this.getExceptionInterceptor());
                        }
                    }
                    this.checkConfidentiality(plugin);
                    fromServer = new Buffer(StringUtils.getBytes(challenge.readString("ASCII", this.getExceptionInterceptor())));
                }
                else if (this.versionMeetsMinimum(5, 5, 16)) {
                    fromServer = new Buffer(challenge.getBytes(challenge.getPosition(), challenge.getBufLength() - challenge.getPosition()));
                }
                else {
                    old_raw_challenge = true;
                    fromServer = new Buffer(challenge.getBytes(challenge.getPosition() - 1, challenge.getBufLength() - challenge.getPosition() + 1));
                }
            }
            try {
                plugin.setAuthenticationParameters(user, skipPassword ? null : password);
                done = plugin.nextAuthenticationStep(fromServer, toServer);
            }
            catch (SQLException e) {
                throw SQLError.createSQLException(e.getMessage(), e.getSQLState(), e, this.getExceptionInterceptor());
            }
            if (toServer.size() > 0) {
                if (challenge == null) {
                    final String enc = this.getEncodingForHandshake();
                    last_sent = new Buffer(packLength + 1);
                    last_sent.writeByte((byte)17);
                    last_sent.writeString(user, enc, this.connection);
                    if (toServer.get(0).getBufLength() < 256) {
                        last_sent.writeByte((byte)toServer.get(0).getBufLength());
                        last_sent.writeBytesNoNull(toServer.get(0).getByteBuffer(), 0, toServer.get(0).getBufLength());
                    }
                    else {
                        last_sent.writeByte((byte)0);
                    }
                    if (this.useConnectWithDb) {
                        last_sent.writeString(database, enc, this.connection);
                    }
                    else {
                        last_sent.writeByte((byte)0);
                    }
                    this.appendCharsetByteForHandshake(last_sent, enc);
                    last_sent.writeByte((byte)0);
                    if ((this.serverCapabilities & 0x80000) != 0x0) {
                        last_sent.writeString(plugin.getProtocolPluginName(), enc, this.connection);
                    }
                    if ((this.clientParam & 0x100000L) != 0x0L) {
                        this.sendConnectionAttributes(last_sent, enc, this.connection);
                        last_sent.writeByte((byte)0);
                    }
                    this.send(last_sent, last_sent.getPosition());
                }
                else if (challenge.isAuthMethodSwitchRequestPacket()) {
                    last_sent = new Buffer(toServer.get(0).getBufLength() + 4);
                    last_sent.writeBytesNoNull(toServer.get(0).getByteBuffer(), 0, toServer.get(0).getBufLength());
                    this.send(last_sent, last_sent.getPosition());
                }
                else if (challenge.isRawPacket() || old_raw_challenge) {
                    for (final Buffer buffer : toServer) {
                        last_sent = new Buffer(buffer.getBufLength() + 4);
                        last_sent.writeBytesNoNull(buffer.getByteBuffer(), 0, toServer.get(0).getBufLength());
                        this.send(last_sent, last_sent.getPosition());
                    }
                }
                else {
                    final String enc = this.getEncodingForHandshake();
                    last_sent = new Buffer(packLength);
                    last_sent.writeLong(this.clientParam);
                    last_sent.writeLong(this.maxThreeBytes);
                    this.appendCharsetByteForHandshake(last_sent, enc);
                    last_sent.writeBytesNoNull(new byte[23]);
                    last_sent.writeString(user, enc, this.connection);
                    if ((this.serverCapabilities & 0x200000) != 0x0) {
                        last_sent.writeLenBytes(toServer.get(0).getBytes(toServer.get(0).getBufLength()));
                    }
                    else {
                        last_sent.writeByte((byte)toServer.get(0).getBufLength());
                        last_sent.writeBytesNoNull(toServer.get(0).getByteBuffer(), 0, toServer.get(0).getBufLength());
                    }
                    if (this.useConnectWithDb) {
                        last_sent.writeString(database, enc, this.connection);
                    }
                    else {
                        last_sent.writeByte((byte)0);
                    }
                    if ((this.serverCapabilities & 0x80000) != 0x0) {
                        last_sent.writeString(plugin.getProtocolPluginName(), enc, this.connection);
                    }
                    if ((this.clientParam & 0x100000L) != 0x0L) {
                        this.sendConnectionAttributes(last_sent, enc, this.connection);
                    }
                    this.send(last_sent, last_sent.getPosition());
                }
            }
        }
        if (counter == 0) {
            throw SQLError.createSQLException(Messages.getString("CommunicationsException.TooManyAuthenticationPluginNegotiations"), this.getExceptionInterceptor());
        }
        if (0x0L != (this.obCapabilityFlag & 0x80L)) {
            this.enableExtraOkPacket = true;
        }
        if (0x0L != (this.obCapabilityFlag & 0x400L)) {
            this.enableAbundantFeedback = true;
        }
        this.inAuthProcess = false;
        if ((this.obCapabilityFlag & 0x100L) != 0x0L) {
            this.useObProto20 = true;
            this.mysqlInput = new ObProto20InputStream(this.connection, this.mysqlInput, this.getThreadId());
        }
        else if ((this.serverCapabilities & 0x20) != 0x0 && this.connection.getUseCompression() && !(this.mysqlInput instanceof CompressedInputStream)) {
            this.deflater = new Deflater();
            this.useCompression = true;
            this.mysqlInput = new CompressedInputStream(this.connection, this.mysqlInput);
        }
        else {
            if ((this.obCapabilityFlag & 0x8L) != 0x0L) {
                this.useVirtualCompress = true;
            }
            if (this.useVirtualCompress) {
                this.deflater = new Deflater(0);
                this.useCompression = true;
                this.mysqlInput = new CompressedInputStream(this.connection, this.mysqlInput);
            }
        }
        if ((this.obCapabilityFlag & 0x40L) != 0x0L) {
            this.isVirtualCompressSwitchOn = true;
        }
        if (!this.useConnectWithDb) {
            this.changeDatabaseTo(database);
        }
        try {
            this.mysqlConnection = this.socketFactory.afterHandshake();
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }
    
    private String makeUpLocalConnectionIdString() {
        String idString = "";
        try {
            if (null != this.mysqlConnection && null != this.mysqlConnection.getLocalAddress()) {
                final int port = this.mysqlConnection.getLocalPort();
                final long ip = MysqlCommonUtils.bytesToLong(this.mysqlConnection.getLocalAddress().getAddress());
                idString = getLocalConnectionId(ip, port);
            }
        }
        catch (Exception e) {
            idString = "";
        }
        return idString;
    }
    
    private Properties getConnectionAttributesAsProperties(final String atts) throws SQLException {
        final Properties props = new Properties();
        if (atts != null) {
            final String[] split;
            final String[] pairs = split = atts.split(",");
            for (final String pair : split) {
                final int keyEnd = pair.indexOf(":");
                if (keyEnd > 0 && keyEnd + 1 < pair.length()) {
                    props.setProperty(pair.substring(0, keyEnd), pair.substring(keyEnd + 1));
                }
            }
        }
        long capFlag = 1480L;
        if (!this.connection.getUseObChecksum()) {
            capFlag &= 0xFFFFFFFFFFFFFFF7L;
            capFlag &= 0xFFFFFFFFFFFFFFBFL;
        }
        if (!this.connection.getUseOceanBaseProtocolV20()) {
            capFlag &= 0xFFFFFFFFFFFFFEFFL;
        }
        props.setProperty("__proxy_capability_flag", String.valueOf(capFlag));
        this.localConnectionIdString = this.makeUpLocalConnectionIdString();
        if (StringUtils.isNotBlank(this.localConnectionIdString)) {
            props.setProperty("__proxy_connection_id", this.localConnectionIdString);
        }
        return props;
    }
    
    private void sendConnectionAttributes(final Buffer buf, final String enc, final MySQLConnection conn) throws SQLException {
        final String atts = conn.getConnectionAttributes();
        final Buffer lb = new Buffer(100);
        try {
            final Properties props = this.getConnectionAttributesAsProperties(atts);
            for (final Object key : props.keySet()) {
                lb.writeLenString((String)key, enc, conn.getServerCharset(), null, conn.parserKnowsUnicode(), conn);
                lb.writeLenString(props.getProperty((String)key), enc, conn.getServerCharset(), null, conn.parserKnowsUnicode(), conn);
            }
        }
        catch (UnsupportedEncodingException ex) {}
        buf.writeFieldLength(lb.getPosition() - 4);
        buf.writeBytesNoNull(lb.getByteBuffer(), 4, lb.getBufLength() - 4);
    }
    
    private void changeDatabaseTo(final String database) throws SQLException {
        if (database == null || database.length() == 0) {
            return;
        }
        try {
            this.sendCommand(2, database, null, false, null, 0);
        }
        catch (Exception ex) {
            if (!this.connection.getCreateDatabaseIfNotExist()) {
                throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ex, this.getExceptionInterceptor());
            }
            this.sendCommand(3, "CREATE DATABASE IF NOT EXISTS " + database, null, false, null, 0);
            this.sendCommand(2, database, null, false, null, 0);
        }
    }
    
    final ResultSetRow nextRow(final Field[] fields, final int columnCount, final boolean isBinaryEncoded, final int resultSetConcurrency, final boolean useBufferRowIfPossible, boolean useBufferRowExplicit, final boolean canReuseRowPacketForBufferRow, final Buffer existingRowPacket) throws SQLException {
        final long nextRowStartNs = System.nanoTime();
        if (this.useDirectRowUnpack && existingRowPacket == null && !isBinaryEncoded && !useBufferRowIfPossible && !useBufferRowExplicit) {
            final ResultSetRow tmpRs = this.nextRowFast(fields, columnCount, isBinaryEncoded, resultSetConcurrency, useBufferRowIfPossible, useBufferRowExplicit, canReuseRowPacketForBufferRow);
            this.connection.getConnectionStats().addGetNextRowCostNs(System.nanoTime() - nextRowStartNs);
            return tmpRs;
        }
        Buffer rowPacket = null;
        if (existingRowPacket == null) {
            rowPacket = this.checkErrorPacket();
            if (!useBufferRowExplicit && useBufferRowIfPossible && rowPacket.getBufLength() > this.useBufferRowSizeThreshold) {
                useBufferRowExplicit = true;
            }
        }
        else {
            rowPacket = existingRowPacket;
            this.checkErrorPacket(existingRowPacket);
        }
        if (!isBinaryEncoded) {
            rowPacket.setPosition(rowPacket.getPosition() - 1);
            if ((!this.isEOFDeprecated() && rowPacket.isEOFPacket()) || (this.isEOFDeprecated() && rowPacket.isResultSetOKPacket())) {
                this.readServerStatusForResultSets(rowPacket);
                this.handleExtraOkPacket(rowPacket);
                return null;
            }
            if (resultSetConcurrency == 1008 || (!useBufferRowIfPossible && !useBufferRowExplicit)) {
                final byte[][] rowData = new byte[columnCount][];
                for (int i = 0; i < columnCount; ++i) {
                    rowData[i] = rowPacket.readLenByteArray(0);
                }
                return new ByteArrayRow(rowData, this.getExceptionInterceptor());
            }
            if (!canReuseRowPacketForBufferRow) {
                this.reusablePacket = new Buffer(rowPacket.getBufLength());
            }
            this.connection.getConnectionStats().addGetNextRowCostNs(System.nanoTime() - nextRowStartNs);
            final BufferRow bufferRow = new BufferRow(rowPacket, fields, false, this.getExceptionInterceptor());
            return bufferRow;
        }
        else {
            if ((!this.isEOFDeprecated() && rowPacket.isEOFPacket()) || (this.isEOFDeprecated() && rowPacket.isResultSetOKPacket())) {
                rowPacket.setPosition(rowPacket.getPosition() - 1);
                this.readServerStatusForResultSets(rowPacket);
                this.handleExtraOkPacket(rowPacket);
                this.connection.getConnectionStats().addGetNextRowCostNs(System.nanoTime() - nextRowStartNs);
                return null;
            }
            if (resultSetConcurrency == 1008 || (!useBufferRowIfPossible && !useBufferRowExplicit)) {
                return this.unpackBinaryResultSetRow(fields, rowPacket, resultSetConcurrency);
            }
            if (!canReuseRowPacketForBufferRow) {
                this.reusablePacket = new Buffer(rowPacket.getBufLength());
            }
            this.connection.getConnectionStats().addGetNextRowCostNs(System.nanoTime() - nextRowStartNs);
            final BufferRow bufferRow = new BufferRow(rowPacket, fields, true, this.getExceptionInterceptor());
            this.handleCompleDataForBufferRow(bufferRow, rowPacket, fields, resultSetConcurrency);
            return bufferRow;
        }
    }
    
    private void handleCompleDataForBufferRow(final BufferRow bufferRow, final Buffer binaryData, final Field[] fields, final int resultSetConcurrency) {
        final int oldPosition = binaryData.getPosition();
        final int numFields = fields.length;
        final byte[][] unpackedRowData = new byte[numFields][];
        final ComplexData[] complexRowData = new ComplexData[numFields];
        final int nullCount = (numFields + 9) / 8;
        int nullMaskPos = binaryData.getPosition();
        binaryData.setPosition(nullMaskPos + nullCount);
        int bit = 4;
        try {
            for (int i = 0; i < numFields; ++i) {
                if ((binaryData.readByte(nullMaskPos) & bit) != 0x0) {
                    unpackedRowData[i] = null;
                }
                else {
                    complexRowData[i] = null;
                    if (resultSetConcurrency != 1008) {
                        this.extractNativeEncodedColumn(binaryData, fields, i, unpackedRowData, complexRowData);
                    }
                    else {
                        this.unpackNativeEncodedColumn(binaryData, fields, i, unpackedRowData, complexRowData);
                    }
                }
                if (((bit <<= 1) & 0xFF) == 0x0) {
                    bit = 1;
                    ++nullMaskPos;
                }
            }
        }
        catch (Exception ex) {}
        finally {
            binaryData.setPosition(oldPosition);
        }
        bufferRow.setComplexRowData(complexRowData);
    }
    
    private void handleExtraOkPacket(final Buffer rowPacket) throws SQLException {
        final boolean moreRowSetsExist = (this.serverStatus & 0x8) != 0x0;
        if (!moreRowSetsExist) {
            if (!this.isEOFDeprecated() && rowPacket.isEOFPacket()) {
                this.readAndCheckExtraOKPacket();
            }
            else if (!this.isEOFDeprecated() || rowPacket.isResultSetOKPacket()) {}
        }
    }
    
    final ResultSetRow nextRowFast(final Field[] fields, final int columnCount, final boolean isBinaryEncoded, final int resultSetConcurrency, final boolean useBufferRowIfPossible, final boolean useBufferRowExplicit, final boolean canReuseRowPacket) throws SQLException {
        try {
            final int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
            if (lengthRead < 4) {
                this.forceClose();
                throw new RuntimeException(Messages.getString("MysqlIO.43"));
            }
            final int packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
            if (packetLength == this.maxThreeBytes) {
                this.reuseAndReadPacket(this.reusablePacket, packetLength);
                return this.nextRow(fields, columnCount, isBinaryEncoded, resultSetConcurrency, useBufferRowIfPossible, useBufferRowExplicit, canReuseRowPacket, this.reusablePacket);
            }
            if (packetLength > this.useBufferRowSizeThreshold) {
                this.reuseAndReadPacket(this.reusablePacket, packetLength);
                return this.nextRow(fields, columnCount, isBinaryEncoded, resultSetConcurrency, true, true, false, this.reusablePacket);
            }
            int remaining = packetLength;
            boolean firstTime = true;
            byte[][] rowData = null;
            for (int i = 0; i < columnCount; ++i) {
                final int sw = this.mysqlInput.read() & 0xFF;
                --remaining;
                if (firstTime) {
                    if (sw == 255) {
                        final Buffer errorPacket = new Buffer(packetLength + 4);
                        errorPacket.setPosition(0);
                        errorPacket.writeByte(this.packetHeaderBuf[0]);
                        errorPacket.writeByte(this.packetHeaderBuf[1]);
                        errorPacket.writeByte(this.packetHeaderBuf[2]);
                        errorPacket.writeByte((byte)1);
                        errorPacket.writeByte((byte)sw);
                        this.readFully(this.mysqlInput, errorPacket.getByteBuffer(), 5, packetLength - 1);
                        errorPacket.setPosition(4);
                        this.checkErrorPacket(errorPacket);
                    }
                    if (sw == 254 && packetLength < 16777215) {
                        if (this.use41Extensions) {
                            if (this.isEOFDeprecated()) {
                                remaining -= this.skipLengthEncodedInteger(this.mysqlInput);
                                remaining -= this.skipLengthEncodedInteger(this.mysqlInput);
                                this.oldServerStatus = this.serverStatus;
                                this.serverStatus = ((this.mysqlInput.read() & 0xFF) | (this.mysqlInput.read() & 0xFF) << 8);
                                this.checkTransactionState(this.oldServerStatus);
                                remaining -= 2;
                                this.warningCount = ((this.mysqlInput.read() & 0xFF) | (this.mysqlInput.read() & 0xFF) << 8);
                                remaining -= 2;
                                if (this.warningCount > 0) {
                                    this.hadWarnings = true;
                                }
                            }
                            else {
                                this.warningCount = ((this.mysqlInput.read() & 0xFF) | (this.mysqlInput.read() & 0xFF) << 8);
                                remaining -= 2;
                                if (this.warningCount > 0) {
                                    this.hadWarnings = true;
                                }
                                this.oldServerStatus = this.serverStatus;
                                this.serverStatus = ((this.mysqlInput.read() & 0xFF) | (this.mysqlInput.read() & 0xFF) << 8);
                                this.checkTransactionState(this.oldServerStatus);
                                remaining -= 2;
                                if (remaining > 0) {
                                    this.skipFully(this.mysqlInput, remaining);
                                    remaining = 0;
                                }
                                final boolean moreRowSetsExist = (this.serverStatus & 0x8) != 0x0;
                                if (!moreRowSetsExist) {
                                    this.readAndCheckExtraOKPacket();
                                }
                            }
                            this.setServerSlowQueryFlags();
                            if (remaining > 0) {
                                this.skipFully(this.mysqlInput, remaining);
                            }
                        }
                        return null;
                    }
                    rowData = new byte[columnCount][];
                    firstTime = false;
                }
                int len = 0;
                switch (sw) {
                    case 251: {
                        len = -1;
                        break;
                    }
                    case 252: {
                        len = ((this.mysqlInput.read() & 0xFF) | (this.mysqlInput.read() & 0xFF) << 8);
                        remaining -= 2;
                        break;
                    }
                    case 253: {
                        len = ((this.mysqlInput.read() & 0xFF) | (this.mysqlInput.read() & 0xFF) << 8 | (this.mysqlInput.read() & 0xFF) << 16);
                        remaining -= 3;
                        break;
                    }
                    case 254: {
                        len = (int)((long)(this.mysqlInput.read() & 0xFF) | (long)(this.mysqlInput.read() & 0xFF) << 8 | (long)(this.mysqlInput.read() & 0xFF) << 16 | (long)(this.mysqlInput.read() & 0xFF) << 24 | (long)(this.mysqlInput.read() & 0xFF) << 32 | (long)(this.mysqlInput.read() & 0xFF) << 40 | (long)(this.mysqlInput.read() & 0xFF) << 48 | (long)(this.mysqlInput.read() & 0xFF) << 56);
                        remaining -= 8;
                        break;
                    }
                    default: {
                        len = sw;
                        break;
                    }
                }
                if (len == -1) {
                    rowData[i] = null;
                }
                else if (len == 0) {
                    rowData[i] = Constants.EMPTY_BYTE_ARRAY;
                }
                else {
                    rowData[i] = new byte[len];
                    final int bytesRead = this.readFully(this.mysqlInput, rowData[i], 0, len);
                    if (bytesRead != len) {
                        throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException(Messages.getString("MysqlIO.43")), this.getExceptionInterceptor());
                    }
                    remaining -= bytesRead;
                }
            }
            if (remaining > 0) {
                this.skipFully(this.mysqlInput, remaining);
            }
            return new ByteArrayRow(rowData, this.getExceptionInterceptor());
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }
    
    final void quit() throws SQLException {
        try {
            try {
                if (!this.mysqlConnection.isClosed()) {
                    try {
                        this.mysqlConnection.shutdownInput();
                    }
                    catch (UnsupportedOperationException ex) {}
                }
            }
            catch (IOException ioEx) {
                this.connection.getLog().logWarn("Caught while disconnecting...", ioEx);
            }
            final Buffer packet = new Buffer(6);
            this.packetSequence = -1;
            this.compressedPacketSequence = -1;
            packet.writeByte((byte)1);
            this.send(packet, packet.getPosition());
        }
        finally {
            this.forceClose();
        }
    }
    
    Buffer getSharedSendPacket() {
        if (this.sharedSendPacket == null) {
            this.sharedSendPacket = new Buffer(1024);
        }
        return this.sharedSendPacket;
    }
    
    void closeStreamer(final RowData streamer) throws SQLException {
        if (this.streamingData == null) {
            throw SQLError.createSQLException(Messages.getString("MysqlIO.17") + streamer + Messages.getString("MysqlIO.18"), this.getExceptionInterceptor());
        }
        if (streamer != this.streamingData) {
            throw SQLError.createSQLException(Messages.getString("MysqlIO.19") + streamer + Messages.getString("MysqlIO.20") + Messages.getString("MysqlIO.21") + Messages.getString("MysqlIO.22"), this.getExceptionInterceptor());
        }
        this.streamingData = null;
    }
    
    boolean tackOnMoreStreamingResults(final ResultSetImpl addingTo) throws SQLException {
        if ((this.serverStatus & 0x8) != 0x0) {
            boolean moreRowSetsExist = true;
            ResultSetImpl currentResultSet = addingTo;
            boolean firstTime = true;
            while (moreRowSetsExist && (firstTime || !currentResultSet.reallyResult())) {
                firstTime = false;
                final Buffer fieldPacket = this.checkErrorPacket();
                fieldPacket.setPosition(0);
                final Statement owningStatement = addingTo.getStatement();
                final int maxRows = owningStatement.getMaxRows();
                final ResultSetImpl newResultSet = this.readResultsForQueryOrUpdate((StatementImpl)owningStatement, maxRows, owningStatement.getResultSetType(), owningStatement.getResultSetConcurrency(), true, owningStatement.getConnection().getCatalog(), fieldPacket, addingTo.isBinaryEncoded, -1L, null);
                currentResultSet.setNextResultSet(newResultSet);
                currentResultSet = newResultSet;
                moreRowSetsExist = ((this.serverStatus & 0x8) != 0x0);
                if (!currentResultSet.reallyResult() && !moreRowSetsExist) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    ResultSetImpl readAllResults(final StatementImpl callingStatement, final int maxRows, final int resultSetType, final int resultSetConcurrency, final boolean streamResults, final String catalog, final Buffer resultPacket, final boolean isBinaryEncoded, final long preSentColumnCount, final Field[] metadataFromCache) throws SQLException {
        resultPacket.setPosition(resultPacket.getPosition() - 1);
        ResultSetImpl currentResultSet;
        final ResultSetImpl topLevelResultSet = currentResultSet = this.readResultsForQueryOrUpdate(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, resultPacket, isBinaryEncoded, preSentColumnCount, metadataFromCache);
        final boolean checkForMoreResults = (this.clientParam & 0x20000L) != 0x0L;
        final boolean serverHasMoreResults = (this.serverStatus & 0x8) != 0x0;
        if (serverHasMoreResults && streamResults) {
            if (topLevelResultSet.getUpdateCount() != -1L) {
                this.tackOnMoreStreamingResults(topLevelResultSet);
            }
            this.reclaimLargeReusablePacket();
            return topLevelResultSet;
        }
        for (boolean moreRowSetsExist = checkForMoreResults & serverHasMoreResults; moreRowSetsExist; moreRowSetsExist = ((this.serverStatus & 0x8) != 0x0)) {
            final Buffer fieldPacket = this.checkErrorPacket();
            fieldPacket.setPosition(0);
            final ResultSetImpl newResultSet = this.readResultsForQueryOrUpdate(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, fieldPacket, isBinaryEncoded, preSentColumnCount, metadataFromCache);
            currentResultSet.setNextResultSet(newResultSet);
            currentResultSet = newResultSet;
        }
        if (!streamResults) {
            this.clearInputStream();
        }
        this.reclaimLargeReusablePacket();
        return topLevelResultSet;
    }
    
    void resetMaxBuf() {
        this.maxAllowedPacket = this.connection.getMaxAllowedPacket();
    }
    
    final Buffer sendCommand(final int command, final String extraData, final Buffer queryPacket, final boolean skipCheck, final String extraDataCharEncoding, final int timeoutMillis) throws SQLException {
        final long sendCommandStartNs = System.nanoTime();
        ++this.commandCount;
        this.enablePacketDebug = this.connection.getEnablePacketDebug();
        this.readPacketSequence = 0;
        int oldTimeout = 0;
        Label_0086: {
            if (timeoutMillis == 0) {
                break Label_0086;
            }
            try {
                oldTimeout = this.mysqlConnection.getSoTimeout();
                this.mysqlConnection.setSoTimeout(timeoutMillis);
            }
            catch (SocketException e) {
                throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e, this.getExceptionInterceptor());
            }
            try {
                this.checkForOutstandingStreamingData();
                this.oldServerStatus = this.serverStatus;
                this.serverStatus = 0;
                this.hadWarnings = false;
                this.warningCount = 0;
                this.queryNoIndexUsed = false;
                this.queryBadIndexUsed = false;
                this.serverQueryWasSlow = false;
                if (this.useCompression || this.useObProto20) {
                    final int bytesLeft = this.mysqlInput.available();
                    if (bytesLeft > 0) {
                        this.mysqlInput.skip(bytesLeft);
                    }
                }
                try {
                    this.clearInputStream();
                    if (queryPacket == null) {
                        final int packLength = 8 + ((extraData != null) ? extraData.length() : 0) + 2;
                        if (this.sendPacket == null) {
                            this.sendPacket = new Buffer(packLength);
                        }
                        this.packetSequence = -1;
                        this.compressedPacketSequence = -1;
                        this.readPacketSequence = 0;
                        this.checkPacketSequence = true;
                        this.sendPacket.clear();
                        this.sendPacket.writeByte((byte)command);
                        if (command == 2 || command == 5 || command == 6 || command == 3 || command == 22) {
                            if (extraDataCharEncoding == null) {
                                this.sendPacket.writeStringNoNull(extraData);
                            }
                            else {
                                this.sendPacket.writeStringNoNull(extraData, extraDataCharEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.connection);
                            }
                        }
                        else if (command == 12) {
                            final long id = Long.parseLong(extraData);
                            this.sendPacket.writeLong(id);
                        }
                        this.send(this.sendPacket, this.sendPacket.getPosition());
                    }
                    else {
                        this.packetSequence = -1;
                        this.compressedPacketSequence = -1;
                        this.send(queryPacket, queryPacket.getPosition());
                    }
                }
                catch (SQLException sqlEx) {
                    throw sqlEx;
                }
                catch (Exception ex) {
                    throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ex, this.getExceptionInterceptor());
                }
                Buffer returnPacket = null;
                if (!skipCheck) {
                    if (command == 23 || command == 26) {
                        this.readPacketSequence = 0;
                        this.packetSequenceReset = true;
                    }
                    returnPacket = this.checkErrorPacket(command);
                }
                return returnPacket;
            }
            catch (IOException ioEx) {
                this.preserveOldTransactionState();
                throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
            }
            catch (SQLException e2) {
                this.preserveOldTransactionState();
                throw e2;
            }
            finally {
                if (timeoutMillis != 0) {
                    try {
                        this.mysqlConnection.setSoTimeout(oldTimeout);
                    }
                    catch (SocketException e3) {
                        throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, e3, this.getExceptionInterceptor());
                    }
                }
                this.connection.getConnectionStats().addSendCommandCostNs(System.nanoTime() - sendCommandStartNs);
            }
        }
    }
    
    protected boolean shouldIntercept() {
        return this.statementInterceptors != null;
    }
    
    final ResultSetInternalMethods sqlQueryDirect(final StatementImpl callingStatement, final String query, final String characterEncoding, Buffer queryPacket, final int maxRows, final int resultSetType, final int resultSetConcurrency, final boolean streamResults, final String catalog, final Field[] cachedMetadata) throws Exception {
        final long sqlQueryDirectStartNs = System.nanoTime();
        ++this.statementExecutionDepth;
        try {
            if (this.statementInterceptors != null) {
                final ResultSetInternalMethods interceptedResults = this.invokeStatementInterceptorsPre(query, callingStatement, false);
                if (interceptedResults != null) {
                    return interceptedResults;
                }
            }
            long queryStartTime = 0L;
            long queryEndTime = 0L;
            String statementComment = this.connection.getStatementComment();
            if (this.connection.getIncludeThreadNamesAsStatementComment()) {
                statementComment = ((statementComment != null) ? (statementComment + ", ") : "") + "java thread: " + Thread.currentThread().getName();
            }
            if (query != null) {
                int packLength = 5 + query.length() * 3 + 2;
                byte[] commentAsBytes = null;
                if (statementComment != null) {
                    commentAsBytes = StringUtils.getBytes(statementComment, null, characterEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.getExceptionInterceptor());
                    packLength += commentAsBytes.length;
                    packLength += 6;
                }
                if (this.sendPacket == null) {
                    this.sendPacket = new Buffer(packLength);
                }
                else {
                    this.sendPacket.clear();
                }
                this.sendPacket.writeByte((byte)3);
                if (commentAsBytes != null) {
                    this.sendPacket.writeBytesNoNull(Constants.SLASH_STAR_SPACE_AS_BYTES);
                    this.sendPacket.writeBytesNoNull(commentAsBytes);
                    this.sendPacket.writeBytesNoNull(Constants.SPACE_STAR_SLASH_SPACE_AS_BYTES);
                }
                if (characterEncoding != null) {
                    if (this.platformDbCharsetMatches) {
                        this.sendPacket.writeStringNoNull(query, characterEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.connection);
                    }
                    else if (StringUtils.startsWithIgnoreCaseAndWs(query, "LOAD DATA")) {
                        this.sendPacket.writeBytesNoNull(StringUtils.getBytes(query));
                    }
                    else {
                        this.sendPacket.writeStringNoNull(query, characterEncoding, this.connection.getServerCharset(), this.connection.parserKnowsUnicode(), this.connection);
                    }
                }
                else {
                    this.sendPacket.writeStringNoNull(query);
                }
                queryPacket = this.sendPacket;
            }
            byte[] queryBuf = null;
            int oldPacketPosition = 0;
            if (this.needToGrabQueryFromPacket) {
                queryBuf = queryPacket.getByteBuffer();
                oldPacketPosition = queryPacket.getPosition();
                queryStartTime = this.getCurrentTimeNanosOrMillis();
            }
            if (this.autoGenerateTestcaseScript) {
                String testcaseQuery = null;
                if (query != null) {
                    if (statementComment != null) {
                        testcaseQuery = "/* " + statementComment + " */ " + query;
                    }
                    else {
                        testcaseQuery = query;
                    }
                }
                else {
                    testcaseQuery = StringUtils.toString(queryBuf, 5, oldPacketPosition - 5);
                }
                final StringBuilder debugBuf = new StringBuilder(testcaseQuery.length() + 32);
                this.connection.generateConnectionCommentBlock(debugBuf);
                debugBuf.append(testcaseQuery);
                debugBuf.append(';');
                this.connection.dumpTestcaseQuery(debugBuf.toString());
            }
            final Buffer resultPacket = this.sendCommand(3, null, queryPacket, false, null, 0);
            long fetchBeginTime = 0L;
            long fetchEndTime = 0L;
            String profileQueryToLog = null;
            boolean queryWasSlow = false;
            if (this.profileSql || this.logSlowQueries) {
                queryEndTime = this.getCurrentTimeNanosOrMillis();
                boolean shouldExtractQuery = false;
                if (this.profileSql) {
                    shouldExtractQuery = true;
                }
                else if (this.logSlowQueries) {
                    final long queryTime = queryEndTime - queryStartTime;
                    boolean logSlow = false;
                    if (!this.useAutoSlowLog) {
                        logSlow = (queryTime > this.connection.getSlowQueryThresholdMillis());
                    }
                    else {
                        logSlow = this.connection.isAbonormallyLongQuery(queryTime);
                        this.connection.reportQueryTime(queryTime);
                    }
                    if (logSlow) {
                        shouldExtractQuery = true;
                        queryWasSlow = true;
                    }
                }
                if (shouldExtractQuery) {
                    boolean truncated = false;
                    int extractPosition;
                    if ((extractPosition = oldPacketPosition) > this.connection.getMaxQuerySizeToLog()) {
                        extractPosition = this.connection.getMaxQuerySizeToLog() + 5;
                        truncated = true;
                    }
                    profileQueryToLog = StringUtils.toString(queryBuf, 5, extractPosition - 5);
                    if (truncated) {
                        profileQueryToLog += Messages.getString("MysqlIO.25");
                    }
                }
                fetchBeginTime = queryEndTime;
            }
            ResultSetInternalMethods rs = this.readAllResults(callingStatement, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, resultPacket, false, -1L, cachedMetadata);
            if (queryWasSlow && !this.serverQueryWasSlow) {
                final StringBuilder mesgBuf = new StringBuilder(48 + profileQueryToLog.length());
                mesgBuf.append(Messages.getString("MysqlIO.SlowQuery", new Object[] { String.valueOf(this.useAutoSlowLog ? " 95% of all queries " : Long.valueOf(this.slowQueryThreshold)), this.queryTimingUnits, queryEndTime - queryStartTime }));
                mesgBuf.append(profileQueryToLog);
                final ProfilerEventHandler eventSink = ProfilerEventHandlerFactory.getInstance(this.connection);
                eventSink.consumeEvent(new ProfilerEvent((byte)6, "", catalog, this.connection.getId(), (callingStatement != null) ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), (int)(queryEndTime - queryStartTime), this.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), mesgBuf.toString()));
                if (this.connection.getExplainSlowQueries()) {
                    if (oldPacketPosition < 1048576) {
                        this.explainSlowQuery(queryPacket.getBytes(5, oldPacketPosition - 5), profileQueryToLog);
                    }
                    else {
                        this.connection.getLog().logWarn(Messages.getString("MysqlIO.28") + 1048576 + Messages.getString("MysqlIO.29"));
                    }
                }
            }
            if (this.logSlowQueries) {
                final ProfilerEventHandler eventSink2 = ProfilerEventHandlerFactory.getInstance(this.connection);
                if (this.queryBadIndexUsed && this.profileSql) {
                    eventSink2.consumeEvent(new ProfilerEvent((byte)6, "", catalog, this.connection.getId(), (callingStatement != null) ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), Messages.getString("MysqlIO.33") + profileQueryToLog));
                }
                if (this.queryNoIndexUsed && this.profileSql) {
                    eventSink2.consumeEvent(new ProfilerEvent((byte)6, "", catalog, this.connection.getId(), (callingStatement != null) ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), Messages.getString("MysqlIO.35") + profileQueryToLog));
                }
                if (this.serverQueryWasSlow && this.profileSql) {
                    eventSink2.consumeEvent(new ProfilerEvent((byte)6, "", catalog, this.connection.getId(), (callingStatement != null) ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), Messages.getString("MysqlIO.ServerSlowQuery") + profileQueryToLog));
                }
            }
            if (this.profileSql) {
                fetchEndTime = this.getCurrentTimeNanosOrMillis();
                final ProfilerEventHandler eventSink2 = ProfilerEventHandlerFactory.getInstance(this.connection);
                eventSink2.consumeEvent(new ProfilerEvent((byte)3, "", catalog, this.connection.getId(), (callingStatement != null) ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), queryEndTime - queryStartTime, this.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), profileQueryToLog));
                eventSink2.consumeEvent(new ProfilerEvent((byte)5, "", catalog, this.connection.getId(), (callingStatement != null) ? callingStatement.getId() : 999, ((ResultSetImpl)rs).resultId, System.currentTimeMillis(), fetchEndTime - fetchBeginTime, this.queryTimingUnits, null, LogUtils.findCallingClassAndMethod(new Throwable()), null));
            }
            if (this.hadWarnings) {
                this.scanForAndThrowDataTruncation();
            }
            if (this.statementInterceptors != null) {
                final ResultSetInternalMethods interceptedResults2 = this.invokeStatementInterceptorsPost(query, callingStatement, rs, false, null);
                if (interceptedResults2 != null) {
                    rs = interceptedResults2;
                }
            }
            return rs;
        }
        catch (SQLException sqlEx) {
            if (this.statementInterceptors != null) {
                this.invokeStatementInterceptorsPost(query, callingStatement, null, false, sqlEx);
            }
            if (callingStatement != null) {
                synchronized (callingStatement.cancelTimeoutMutex) {
                    if (callingStatement.wasCancelled) {
                        SQLException cause = null;
                        if (callingStatement.wasCancelledByTimeout) {
                            cause = new MySQLTimeoutException();
                        }
                        else {
                            cause = new MySQLStatementCancelledException();
                        }
                        callingStatement.resetCancelledState();
                        throw cause;
                    }
                }
            }
            throw sqlEx;
        }
        finally {
            --this.statementExecutionDepth;
            this.connection.getConnectionStats().addSqlQueryDirectCostNs(System.nanoTime() - sqlQueryDirectStartNs);
        }
    }
    
    ResultSetInternalMethods invokeStatementInterceptorsPre(final String sql, final com.alipay.oceanbase.jdbc.Statement interceptedStatement, final boolean forceExecute) throws SQLException {
        ResultSetInternalMethods previousResultSet = null;
        for (int i = 0, s = this.statementInterceptors.size(); i < s; ++i) {
            final StatementInterceptorV2 interceptor = this.statementInterceptors.get(i);
            final boolean executeTopLevelOnly = interceptor.executeTopLevelOnly();
            final boolean shouldExecute = (executeTopLevelOnly && (this.statementExecutionDepth == 1 || forceExecute)) || !executeTopLevelOnly;
            if (shouldExecute) {
                final String sqlToInterceptor = sql;
                final ResultSetInternalMethods interceptedResultSet = interceptor.preProcess(sqlToInterceptor, interceptedStatement, this.connection);
                if (interceptedResultSet != null) {
                    previousResultSet = interceptedResultSet;
                }
            }
        }
        return previousResultSet;
    }
    
    ResultSetInternalMethods invokeStatementInterceptorsPost(final String sql, final com.alipay.oceanbase.jdbc.Statement interceptedStatement, ResultSetInternalMethods originalResultSet, final boolean forceExecute, final SQLException statementException) throws SQLException {
        for (int i = 0, s = this.statementInterceptors.size(); i < s; ++i) {
            final StatementInterceptorV2 interceptor = this.statementInterceptors.get(i);
            final boolean executeTopLevelOnly = interceptor.executeTopLevelOnly();
            final boolean shouldExecute = (executeTopLevelOnly && (this.statementExecutionDepth == 1 || forceExecute)) || !executeTopLevelOnly;
            if (shouldExecute) {
                final String sqlToInterceptor = sql;
                final ResultSetInternalMethods interceptedResultSet = interceptor.postProcess(sqlToInterceptor, interceptedStatement, originalResultSet, this.connection, this.warningCount, this.queryNoIndexUsed, this.queryBadIndexUsed, statementException);
                if (interceptedResultSet != null) {
                    originalResultSet = interceptedResultSet;
                }
            }
        }
        return originalResultSet;
    }
    
    private void calculateSlowQueryThreshold() {
        this.slowQueryThreshold = this.connection.getSlowQueryThresholdMillis();
        if (this.connection.getUseNanosForElapsedTime()) {
            final long nanosThreshold = this.connection.getSlowQueryThresholdNanos();
            if (nanosThreshold != 0L) {
                this.slowQueryThreshold = nanosThreshold;
            }
            else {
                this.slowQueryThreshold *= 1000000L;
            }
        }
    }
    
    protected long getCurrentTimeNanosOrMillis() {
        if (this.useNanosForElapsedTime) {
            return TimeUtil.getCurrentTimeNanosOrMillis();
        }
        return System.currentTimeMillis();
    }
    
    String getHost() {
        return this.host;
    }
    
    boolean isVersion(final int major, final int minor, final int subminor) {
        return major == this.getServerMajorVersion() && minor == this.getServerMinorVersion() && subminor == this.getServerSubMinorVersion();
    }
    
    boolean versionMeetsMinimum(final int major, final int minor, final int subminor) {
        return this.getServerMajorVersion() >= major && (this.getServerMajorVersion() != major || (this.getServerMinorVersion() >= minor && (this.getServerMinorVersion() != minor || this.getServerSubMinorVersion() >= subminor)));
    }
    
    private static final String getPacketDumpToLog(final Buffer packetToDump, final int packetLength) {
        if (packetLength < 1024) {
            return packetToDump.dump(packetLength);
        }
        final StringBuilder packetDumpBuf = new StringBuilder(4096);
        packetDumpBuf.append(packetToDump.dump(1024));
        packetDumpBuf.append(Messages.getString("MysqlIO.36"));
        packetDumpBuf.append(1024);
        packetDumpBuf.append(Messages.getString("MysqlIO.37"));
        return packetDumpBuf.toString();
    }
    
    private final int readFully(final InputStream in, final byte[] b, final int off, final int len) throws IOException {
        final long readStartNs = System.nanoTime();
        if (len < 0) {
            throw new IndexOutOfBoundsException();
        }
        int n;
        int count;
        for (n = 0; n < len; n += count) {
            count = in.read(b, off + n, len - n);
            if (count < 0) {
                throw new EOFException(Messages.getString("MysqlIO.EOF", new Object[] { len, n }));
            }
        }
        this.connection.getConnectionStats().addSocketReadCostNs(System.nanoTime() - readStartNs);
        this.connection.getConnectionStats().addResponseReadBytes(len);
        return n;
    }
    
    private final long skipFully(final InputStream in, final long len) throws IOException {
        final long readStartNs = System.nanoTime();
        if (len < 0L) {
            throw new IOException("Negative skip length not allowed");
        }
        long n;
        long count;
        for (n = 0L; n < len; n += count) {
            count = in.skip(len - n);
            if (count < 0L) {
                throw new EOFException(Messages.getString("MysqlIO.EOF", new Object[] { len, n }));
            }
        }
        this.connection.getConnectionStats().addSocketReadCostNs(System.nanoTime() - readStartNs);
        this.connection.getConnectionStats().addResponseReadBytes(len);
        return n;
    }
    
    private final int skipLengthEncodedInteger(final InputStream in) throws IOException {
        final int sw = in.read() & 0xFF;
        switch (sw) {
            case 252: {
                return (int)this.skipFully(in, 2L) + 1;
            }
            case 253: {
                return (int)this.skipFully(in, 3L) + 1;
            }
            case 254: {
                return (int)this.skipFully(in, 8L) + 1;
            }
            default: {
                return 1;
            }
        }
    }
    
    protected final ResultSetImpl readResultsForQueryOrUpdate(final StatementImpl callingStatement, final int maxRows, final int resultSetType, final int resultSetConcurrency, final boolean streamResults, final String catalog, final Buffer resultPacket, final boolean isBinaryEncoded, final long preSentColumnCount, final Field[] metadataFromCache) throws SQLException {
        final long columnCount = resultPacket.readFieldLength();
        if (columnCount == 0L) {
            return this.buildResultSetWithUpdates(callingStatement, resultPacket);
        }
        if (columnCount == -1L) {
            String charEncoding = null;
            if (this.connection.getUseUnicode()) {
                charEncoding = this.connection.getEncoding();
            }
            String fileName = null;
            if (this.platformDbCharsetMatches) {
                fileName = ((charEncoding != null) ? resultPacket.readString(charEncoding, this.getExceptionInterceptor()) : resultPacket.readString());
            }
            else {
                fileName = resultPacket.readString();
            }
            return this.sendFileToServer(callingStatement, fileName);
        }
        final ResultSetImpl results = this.getResultSet(callingStatement, columnCount, maxRows, resultSetType, resultSetConcurrency, streamResults, catalog, isBinaryEncoded, metadataFromCache);
        return results;
    }
    
    private int alignPacketSize(final int a, final int l) {
        return a + l - 1 & ~(l - 1);
    }
    
    private ResultSetImpl buildResultSetWithRows(final StatementImpl callingStatement, final String catalog, final Field[] fields, final RowData rows, final int resultSetType, final int resultSetConcurrency, final boolean isBinaryEncoded) throws SQLException {
        ResultSetImpl rs = null;
        switch (resultSetConcurrency) {
            case 1007: {
                rs = ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, false);
                if (isBinaryEncoded) {
                    rs.setBinaryEncoded();
                    break;
                }
                break;
            }
            case 1008: {
                rs = ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, true);
                break;
            }
            default: {
                return ResultSetImpl.getInstance(catalog, fields, rows, this.connection, callingStatement, false);
            }
        }
        rs.setResultSetType(resultSetType);
        rs.setResultSetConcurrency(resultSetConcurrency);
        return rs;
    }
    
    private ResultSetImpl buildResultSetWithUpdates(final StatementImpl callingStatement, final Buffer resultPacket) throws SQLException {
        long updateCount = -1L;
        long updateID = -1L;
        String info = null;
        try {
            if (this.useNewUpdateCounts) {
                updateCount = resultPacket.newReadLength();
                updateID = resultPacket.newReadLength();
            }
            else {
                updateCount = resultPacket.readLength();
                updateID = resultPacket.readLength();
            }
            if (this.use41Extensions) {
                this.serverStatus = resultPacket.readInt();
                this.checkTransactionState(this.oldServerStatus);
                this.warningCount = resultPacket.readInt();
                if (this.warningCount > 0) {
                    this.hadWarnings = true;
                }
                resultPacket.readByte();
                this.setServerSlowQueryFlags();
            }
            if (this.connection.isReadInfoMsgEnabled()) {
                info = resultPacket.readString(this.connection.getErrorMessageEncoding(), this.getExceptionInterceptor());
            }
        }
        catch (Exception ex) {
            final SQLException sqlEx = SQLError.createSQLException(SQLError.get("S1000"), "S1000", -1, this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
        final ResultSetInternalMethods updateRs = ResultSetImpl.getInstance(updateCount, updateID, this.connection, callingStatement);
        if (info != null) {
            ((ResultSetImpl)updateRs).setServerInfo(info);
        }
        return (ResultSetImpl)updateRs;
    }
    
    private void setServerSlowQueryFlags() {
        this.queryBadIndexUsed = ((this.serverStatus & 0x10) != 0x0);
        this.queryNoIndexUsed = ((this.serverStatus & 0x20) != 0x0);
        this.serverQueryWasSlow = ((this.serverStatus & 0x800) != 0x0);
    }
    
    public boolean isOracleMode() {
        return this.useOracleMode;
    }
    
    public boolean isUseOracleLocator() {
        return this.useOracleLocator;
    }
    
    private void checkForOutstandingStreamingData() throws SQLException {
        if (this.streamingData != null) {
            final boolean shouldClobber = this.connection.getClobberStreamingResults();
            if (!shouldClobber) {
                throw SQLError.createSQLException(Messages.getString("MysqlIO.39") + this.streamingData + Messages.getString("MysqlIO.40") + Messages.getString("MysqlIO.41") + Messages.getString("MysqlIO.42"), this.getExceptionInterceptor());
            }
            this.streamingData.getOwner().realClose(false);
            this.clearInputStream();
        }
    }
    
    private Buffer buildProto20Packet(final Buffer packet, final int offset, final int packetLen, final boolean isLast) throws SQLException {
        final int totalLen = packetLen + 35;
        final byte[] targetBytes = new byte[totalLen];
        System.arraycopy(packet.getByteBuffer(), offset, targetBytes, 31, packetLen);
        final Buffer targetBuffer = new Buffer(targetBytes);
        targetBuffer.setPosition(0);
        targetBuffer.writeLongInt(packetLen + 28);
        targetBuffer.writeByte(this.compressedPacketSequence);
        targetBuffer.writeLongInt(0);
        targetBuffer.writeInt(8363);
        targetBuffer.writeInt(20);
        targetBuffer.writeLong(this.connection.getId());
        targetBuffer.writeLongInt(this.requestId);
        targetBuffer.writeByte(this.proto20PacketSeq);
        targetBuffer.writeLong(packetLen);
        long flag = 0L;
        if (isLast) {
            flag |= 0x2L;
        }
        targetBuffer.writeLong(flag);
        final int reserved = 0;
        targetBuffer.writeInt(reserved);
        int crc16 = 0;
        if (!this.isVirtualCompressSwitchOn || !this.proto20PacketWithoutChecksum) {
            if (targetBuffer.getPosition() != 29) {
                throw SQLError.createSQLException("seri error", this.getExceptionInterceptor());
            }
            crc16 = ObCrc16.calculate(targetBytes, 29);
        }
        targetBuffer.writeInt(crc16);
        long crc32Value = 0L;
        if (!this.isVirtualCompressSwitchOn || !this.proto20PacketWithoutChecksum) {
            final long crc32BeginNs = System.nanoTime();
            final ObCrc32C crc32C = new ObCrc32C();
            crc32C.reset();
            crc32C.update(targetBytes, 31, packetLen);
            crc32Value = crc32C.getValue();
            this.connection.getConnectionStats().addCrc32RequestCostNs(System.nanoTime() - crc32BeginNs);
        }
        targetBuffer.setPosition(31 + packetLen);
        targetBuffer.writeLong(crc32Value);
        if (targetBuffer.getPosition() != totalLen) {
            throw SQLError.createSQLException(String.format("encode proto20 error, pos=%d, totalLen=%d", targetBuffer.getPosition(), totalLen), this.getExceptionInterceptor());
        }
        return targetBuffer;
    }
    
    private Buffer buildSplitProto20Packet(final Buffer packet, int offset, final int packetLen, final boolean isLast) throws SQLException {
        if (packetLen > 50) {
            offset = MysqlCommonUtils.getRandomNum(1, 49);
            final Buffer buf1 = this.buildProto20Packet(packet, 0, offset, false);
            ++this.proto20PacketSeq;
            ++this.compressedPacketSequence;
            final Buffer buf2 = this.buildProto20Packet(packet, offset, packetLen - offset, true);
            final byte[] data = new byte[buf1.getPosition() + buf2.getPosition()];
            System.arraycopy(buf1.getByteBuffer(), 0, data, 0, buf1.getPosition());
            System.arraycopy(buf2.getByteBuffer(), 0, data, buf1.getPosition(), buf2.getPosition());
            final Buffer tmpBuffer = new Buffer(data);
            tmpBuffer.setPosition(data.length);
            ((ObProto20InputStream)this.mysqlInput).setPrePacketSeq(this.proto20PacketSeq);
            ((ObProto20InputStream)this.mysqlInput).setRequestId(this.requestId);
            return tmpBuffer;
        }
        return this.buildProto20Packet(packet, offset, packetLen, isLast);
    }
    
    private Buffer compressPacket(final Buffer packet, final int offset, final int packetLen) throws SQLException {
        int compressedLength = packetLen;
        int uncompressedLength = 0;
        byte[] compressedBytes = null;
        int offsetWrite = offset;
        if (packetLen < 50 && !this.useVirtualCompress) {
            compressedBytes = packet.getByteBuffer();
        }
        else if (this.useVirtualCompress && this.isVirtualCompressSwitchOn && this.comrepssWithRawData) {
            compressedBytes = packet.getByteBuffer();
        }
        else {
            final byte[] bytesToCompress = packet.getByteBuffer();
            compressedBytes = new byte[bytesToCompress.length * 2 + 13];
            if (this.deflater == null) {
                if (this.useVirtualCompress) {
                    this.deflater = new Deflater(0);
                }
                else {
                    this.deflater = new Deflater();
                }
            }
            final long compressStartNs = System.nanoTime();
            this.deflater.reset();
            this.deflater.setInput(bytesToCompress, offset, packetLen);
            this.deflater.finish();
            compressedLength = this.deflater.deflate(compressedBytes);
            this.connection.getConnectionStats().addZlibCompressCostNs(System.nanoTime() - compressStartNs);
            if (compressedLength >= this.maxThreeBytes) {
                throw new SQLException(String.format("packet length is larger than 16MB after compressed,compressedLength=%d, packetLength=%d", compressedLength, packetLen));
            }
            uncompressedLength = packetLen;
            offsetWrite = 0;
        }
        final Buffer compressedPacket = new Buffer(7 + compressedLength);
        compressedPacket.setPosition(0);
        compressedPacket.writeLongInt(compressedLength);
        compressedPacket.writeByte(this.compressedPacketSequence);
        compressedPacket.writeLongInt(uncompressedLength);
        compressedPacket.writeBytesNoNull(compressedBytes, offsetWrite, compressedLength);
        return compressedPacket;
    }
    
    private BufferResult buildBuffers(final int packetLength) {
        Buffer[] buffers = null;
        int splitPacketLen = 0;
        if (packetLength > this.halfMaxThreeBytes) {
            buffers = new Buffer[2];
            splitPacketLen = packetLength / 2;
        }
        else {
            buffers = new Buffer[] { null };
            splitPacketLen = packetLength;
        }
        return new BufferResult(buffers, splitPacketLen);
    }
    
    private Buffer[] compressPackets(final Buffer packet, final int offset, final int packetLength) throws SQLException {
        final BufferResult bufferResult = this.buildBuffers(packetLength);
        int splitOffset = offset;
        int splitPacketLen = bufferResult.getSplitPacketLen();
        final Buffer[] buffers = bufferResult.getBuffers();
        for (int i = 0; i < buffers.length; ++i) {
            ++this.compressedPacketSequence;
            buffers[i] = this.compressPacket(packet, splitOffset, splitPacketLen);
            if (this.traceProtocol) {
                final StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.57"));
                traceMessageBuf.append(getPacketDumpToLog(buffers[i], buffers[i].getPosition()));
                traceMessageBuf.append(Messages.getString("MysqlIO.58"));
                traceMessageBuf.append(getPacketDumpToLog(packet, packetLength));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            splitOffset += splitPacketLen;
            splitPacketLen = packetLength - splitPacketLen;
        }
        return buffers;
    }
    
    private Buffer[] splitProto20Packets(final Buffer packet, final int offset, final int packetLength, final boolean isLast) throws SQLException {
        final BufferResult bufferResult = this.buildBuffers(packetLength);
        int splitOffset = offset;
        int splitPacketLen = bufferResult.getSplitPacketLen();
        final Buffer[] buffers = bufferResult.getBuffers();
        for (int i = 0; i < buffers.length; ++i) {
            boolean isSplitLast = false;
            isSplitLast = (buffers.length == i + 1 && isLast);
            ++this.proto20PacketSeq;
            ++this.compressedPacketSequence;
            buffers[i] = this.buildProto20Packet(packet, splitOffset, splitPacketLen, isSplitLast);
            ((ObProto20InputStream)this.mysqlInput).setPrePacketSeq(this.proto20PacketSeq);
            ((ObProto20InputStream)this.mysqlInput).setRequestId(this.requestId);
            if (this.traceProtocol) {
                final StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.57 proto20"));
                traceMessageBuf.append(getPacketDumpToLog(buffers[i], buffers[i].getPosition()));
                traceMessageBuf.append(Messages.getString("MysqlIO.58"));
                traceMessageBuf.append(getPacketDumpToLog(packet, packetLength));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            splitOffset += splitPacketLen;
            splitPacketLen = packetLength - splitPacketLen;
        }
        return buffers;
    }
    
    private final void readServerStatusForResultSets(final Buffer rowPacket) throws SQLException {
        if (this.use41Extensions) {
            rowPacket.readByte();
            if (this.isEOFDeprecated()) {
                rowPacket.newReadLength();
                rowPacket.newReadLength();
                this.oldServerStatus = this.serverStatus;
                this.serverStatus = rowPacket.readInt();
                this.checkTransactionState(this.oldServerStatus);
                this.warningCount = rowPacket.readInt();
                if (this.warningCount > 0) {
                    this.hadWarnings = true;
                }
                rowPacket.readByte();
                if (this.connection.isReadInfoMsgEnabled()) {
                    rowPacket.readString(this.connection.getErrorMessageEncoding(), this.getExceptionInterceptor());
                }
            }
            else {
                this.warningCount = rowPacket.readInt();
                if (this.warningCount > 0) {
                    this.hadWarnings = true;
                }
                this.oldServerStatus = this.serverStatus;
                this.serverStatus = rowPacket.readInt();
                this.checkTransactionState(this.oldServerStatus);
            }
            this.setServerSlowQueryFlags();
        }
    }
    
    private SocketFactory createSocketFactory() throws SQLException {
        try {
            if (this.socketFactoryClassName == null) {
                throw SQLError.createSQLException(Messages.getString("MysqlIO.75"), "08001", this.getExceptionInterceptor());
            }
            return (SocketFactory)Class.forName(this.socketFactoryClassName).newInstance();
        }
        catch (Exception ex) {
            final SQLException sqlEx = SQLError.createSQLException(Messages.getString("MysqlIO.76") + this.socketFactoryClassName + Messages.getString("MysqlIO.77"), "08001", this.getExceptionInterceptor());
            sqlEx.initCause(ex);
            throw sqlEx;
        }
    }
    
    private void enqueuePacketForDebugging(final boolean isPacketBeingSent, final boolean isPacketReused, final int sendLength, final byte[] header, final Buffer packet) throws SQLException {
        if (this.packetDebugRingBuffer.size() + 1 > this.connection.getPacketDebugBufferSize()) {
            this.packetDebugRingBuffer.removeFirst();
        }
        StringBuilder packetDump = null;
        if (!isPacketBeingSent) {
            final int bytesToDump = Math.min(1024, packet.getBufLength());
            final Buffer packetToDump = new Buffer(4 + bytesToDump);
            packetToDump.setPosition(0);
            packetToDump.writeBytesNoNull(header);
            packetToDump.writeBytesNoNull(packet.getBytes(0, bytesToDump));
            final String packetPayload = packetToDump.dump(bytesToDump);
            packetDump = new StringBuilder(96 + packetPayload.length());
            packetDump.append("Server ");
            packetDump.append(isPacketReused ? "(re-used) " : "(new) ");
            packetDump.append(packet.toSuperString());
            packetDump.append(" --------------------> Client\n");
            packetDump.append("\nPacket payload:\n\n");
            packetDump.append(packetPayload);
            if (bytesToDump == 1024) {
                packetDump.append("\nNote: Packet of " + packet.getBufLength() + " bytes truncated to " + 1024 + " bytes.\n");
            }
        }
        else {
            final int bytesToDump = Math.min(1024, sendLength);
            final String packetPayload2 = packet.dump(bytesToDump);
            packetDump = new StringBuilder(68 + packetPayload2.length());
            packetDump.append("Client ");
            packetDump.append(packet.toSuperString());
            packetDump.append("--------------------> Server\n");
            packetDump.append("\nPacket payload:\n\n");
            packetDump.append(packetPayload2);
            if (bytesToDump == 1024) {
                packetDump.append("\nNote: Packet of " + sendLength + " bytes truncated to " + 1024 + " bytes.\n");
            }
        }
        this.packetDebugRingBuffer.addLast(packetDump);
    }
    
    private RowData readSingleRowSet(final long columnCount, final int maxRows, final int resultSetConcurrency, final boolean isBinaryEncoded, final Field[] fields) throws SQLException {
        final ArrayList<ResultSetRow> rows = new ArrayList<ResultSetRow>();
        final boolean useBufferRowExplicit = useBufferRowExplicit(fields);
        ResultSetRow row = this.nextRow(fields, (int)columnCount, isBinaryEncoded, resultSetConcurrency, false, useBufferRowExplicit, false, null);
        int rowCount = 0;
        if (row != null) {
            rows.add(row);
            rowCount = 1;
        }
        while (row != null) {
            row = this.nextRow(fields, (int)columnCount, isBinaryEncoded, resultSetConcurrency, false, useBufferRowExplicit, false, null);
            if (row != null && (maxRows == -1 || rowCount < maxRows)) {
                rows.add(row);
                ++rowCount;
            }
        }
        final RowData rowData = new RowDataStatic(rows);
        return rowData;
    }
    
    public static boolean useBufferRowExplicit(final Field[] fields) {
        if (fields == null) {
            return false;
        }
        int i = 0;
        while (i < fields.length) {
            switch (fields[i].getSQLType()) {
                case -4:
                case -1:
                case 2004:
                case 2005: {
                    return true;
                }
                default: {
                    ++i;
                    continue;
                }
            }
        }
        return false;
    }
    
    private void reclaimLargeReusablePacket() {
        if (this.reusablePacket != null && this.reusablePacket.getCapacity() > 1048576) {
            this.reusablePacket = new Buffer(1024);
        }
    }
    
    private final Buffer reuseAndReadPacket(final Buffer reuse) throws SQLException {
        return this.reuseAndReadPacket(reuse, -1);
    }
    
    private final Buffer reuseAndReadPacket(final Buffer reuse, final int existingPacketLength) throws SQLException {
        try {
            reuse.setWasMultiPacket(false);
            int packetLength = 0;
            if (existingPacketLength == -1) {
                final int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
                if (lengthRead < 4) {
                    this.forceClose();
                    throw new IOException(Messages.getString("MysqlIO.43"));
                }
                packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
            }
            else {
                packetLength = existingPacketLength;
            }
            if (this.traceProtocol) {
                final StringBuilder traceMessageBuf = new StringBuilder();
                traceMessageBuf.append(Messages.getString("MysqlIO.44"));
                traceMessageBuf.append(packetLength);
                traceMessageBuf.append(Messages.getString("MysqlIO.45"));
                traceMessageBuf.append(StringUtils.dumpAsHex(this.packetHeaderBuf, 4));
                this.connection.getLog().logTrace(traceMessageBuf.toString());
            }
            final byte multiPacketSeq = this.packetHeaderBuf[3];
            if (!this.packetSequenceReset) {
                if (this.enablePacketDebug && this.checkPacketSequence) {
                    this.checkPacketSequencing(multiPacketSeq);
                }
            }
            else {
                this.packetSequenceReset = false;
            }
            this.readPacketSequence = multiPacketSeq;
            reuse.setPosition(0);
            if (reuse.getByteBuffer().length <= packetLength) {
                reuse.setByteBuffer(new byte[packetLength + 1]);
            }
            reuse.setBufLength(packetLength);
            final int numBytesRead = this.readFully(this.mysqlInput, reuse.getByteBuffer(), 0, packetLength);
            if (numBytesRead != packetLength) {
                throw new IOException("Short read, expected " + packetLength + " bytes, only read " + numBytesRead);
            }
            if (this.useVirtualCompress && this.isVirtualCompressSwitchOn && this.mysqlInput instanceof CompressedInputStream) {
                final boolean isTempReceivedUnCompressedData = ((CompressedInputStream)this.mysqlInput).isReceivedUcompressedData();
                if (this.comrepssWithRawData != isTempReceivedUnCompressedData) {
                    this.connection.getLog().logInfo(String.format("compress config changed, this.comrepssWithRawData=%s, isTempReceivedUnCompressedData=%s", this.comrepssWithRawData, isTempReceivedUnCompressedData));
                    this.comrepssWithRawData = isTempReceivedUnCompressedData;
                }
            }
            if (this.useObProto20 && this.isVirtualCompressSwitchOn && this.mysqlInput instanceof ObProto20InputStream) {
                final boolean isTempReceivedDataWithoutChecksum = ((ObProto20InputStream)this.mysqlInput).isReceivedDataWithoutChecksum();
                if (this.proto20PacketWithoutChecksum != isTempReceivedDataWithoutChecksum) {
                    this.connection.getLog().logInfo(String.format("proto20 checksum config changed, this.proto20PacketWithoutChecksum=%s, isTempReceivedDataWithoutChecksum=%s", this.proto20PacketWithoutChecksum, isTempReceivedDataWithoutChecksum));
                    this.proto20PacketWithoutChecksum = isTempReceivedDataWithoutChecksum;
                }
            }
            if (this.traceProtocol) {
                final StringBuilder traceMessageBuf2 = new StringBuilder();
                traceMessageBuf2.append(Messages.getString("MysqlIO.46"));
                traceMessageBuf2.append(getPacketDumpToLog(reuse, packetLength));
                this.connection.getLog().logTrace(traceMessageBuf2.toString());
            }
            if (this.enablePacketDebug) {
                this.enqueuePacketForDebugging(false, true, 0, this.packetHeaderBuf, reuse);
            }
            boolean isMultiPacket = false;
            if (packetLength == this.maxThreeBytes) {
                reuse.setPosition(this.maxThreeBytes);
                isMultiPacket = true;
                packetLength = this.readRemainingMultiPackets(reuse, multiPacketSeq);
            }
            if (!isMultiPacket) {
                reuse.getByteBuffer()[packetLength] = 0;
            }
            if (this.connection.getMaintainTimeStats()) {
                this.lastPacketReceivedTimeMs = System.currentTimeMillis();
            }
            return reuse;
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
        catch (OutOfMemoryError oom) {
            try {
                this.clearInputStream();
            }
            catch (Exception ex) {}
            try {
                this.connection.realClose(false, false, true, oom);
            }
            catch (Exception ex2) {}
            throw oom;
        }
    }
    
    private int readRemainingMultiPackets(final Buffer reuse, byte multiPacketSeq) throws IOException, SQLException {
        int packetLength = -1;
        Buffer multiPacket = null;
        do {
            final int lengthRead = this.readFully(this.mysqlInput, this.packetHeaderBuf, 0, 4);
            if (lengthRead < 4) {
                this.forceClose();
                throw new IOException(Messages.getString("MysqlIO.47"));
            }
            packetLength = (this.packetHeaderBuf[0] & 0xFF) + ((this.packetHeaderBuf[1] & 0xFF) << 8) + ((this.packetHeaderBuf[2] & 0xFF) << 16);
            if (multiPacket == null) {
                multiPacket = new Buffer(packetLength);
            }
            if (!this.useNewLargePackets && packetLength == 1) {
                this.clearInputStream();
                break;
            }
            ++multiPacketSeq;
            if (multiPacketSeq != this.packetHeaderBuf[3]) {
                throw new IOException(Messages.getString("MysqlIO.49"));
            }
            multiPacket.setPosition(0);
            multiPacket.setBufLength(packetLength);
            final byte[] byteBuf = multiPacket.getByteBuffer();
            final int lengthToWrite = packetLength;
            final int bytesRead = this.readFully(this.mysqlInput, byteBuf, 0, packetLength);
            if (bytesRead != lengthToWrite) {
                throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, SQLError.createSQLException(Messages.getString("MysqlIO.50") + lengthToWrite + Messages.getString("MysqlIO.51") + bytesRead + ".", this.getExceptionInterceptor()), this.getExceptionInterceptor());
            }
            reuse.writeBytesNoNull(byteBuf, 0, lengthToWrite);
        } while (packetLength == this.maxThreeBytes);
        reuse.setPosition(0);
        reuse.setWasMultiPacket(true);
        return packetLength;
    }
    
    private void checkPacketSequencing(final byte multiPacketSeq) throws SQLException {
        if (multiPacketSeq == -128 && this.readPacketSequence != 127) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException("Packets out of order, expected packet # -128, but received packet # " + multiPacketSeq), this.getExceptionInterceptor());
        }
        if (this.readPacketSequence == -1 && multiPacketSeq != 0) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException("Packets out of order, expected packet # -1, but received packet # " + multiPacketSeq), this.getExceptionInterceptor());
        }
        if (multiPacketSeq != -128 && this.readPacketSequence != -1 && multiPacketSeq != this.readPacketSequence + 1) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, new IOException("Packets out of order, expected packet # " + (this.readPacketSequence + 1) + ", but received packet # " + multiPacketSeq), this.getExceptionInterceptor());
        }
    }
    
    void enableMultiQueries() throws SQLException {
        final Buffer buf = this.getSharedSendPacket();
        buf.clear();
        buf.writeByte((byte)27);
        buf.writeInt(0);
        this.sendCommand(27, null, buf, false, null, 0);
    }
    
    void disableMultiQueries() throws SQLException {
        final Buffer buf = this.getSharedSendPacket();
        buf.clear();
        buf.writeByte((byte)27);
        buf.writeInt(1);
        this.sendCommand(27, null, buf, false, null, 0);
    }
    
    private final void send(final Buffer packet, final int packetLen) throws SQLException {
        try {
            final long buildPacketStartNs = System.nanoTime();
            if (this.maxAllowedPacket > 0 && packetLen > this.maxAllowedPacket) {
                throw new PacketTooBigException(packetLen, this.maxAllowedPacket);
            }
            this.incRequestId();
            this.proto20PacketSeq = -1;
            Buffer[] splitPackets = null;
            if ((this.serverMajorVersion >= 4 && (packetLen - 4 >= this.maxThreeBytes || (this.useCompression && packetLen - 4 >= this.maxThreeBytes - 3))) || (this.useObProto20 && packetLen - 4 >= this.maxPacketLenForProto20)) {
                this.sendSplitPackets(packet, packetLen);
            }
            else {
                ++this.packetSequence;
                final Buffer packetToSend = packet;
                packetToSend.setPosition(0);
                packetToSend.writeLongInt(packetLen - 4);
                packetToSend.writeByte(this.packetSequence);
                if (this.useObProto20) {
                    splitPackets = this.splitProto20Packets(packetToSend, 0, packetLen, true);
                    this.connection.getConnectionStats().addBuildProto20PacketCostNs(System.nanoTime() - buildPacketStartNs);
                }
                else if (this.useCompression) {
                    splitPackets = this.compressPackets(packetToSend, 0, packetLen);
                    this.connection.getConnectionStats().addBuildCompressPacketConstNs(System.nanoTime() - buildPacketStartNs);
                }
                else {
                    if (this.traceProtocol) {
                        final StringBuilder traceMessageBuf = new StringBuilder();
                        traceMessageBuf.append(Messages.getString("MysqlIO.59"));
                        traceMessageBuf.append("host: '");
                        traceMessageBuf.append(this.host);
                        traceMessageBuf.append("' threadId: '");
                        traceMessageBuf.append(this.threadId);
                        traceMessageBuf.append("'\n");
                        traceMessageBuf.append(packetToSend.dump(packetLen));
                        this.connection.getLog().logTrace(traceMessageBuf.toString());
                    }
                    this.connection.getConnectionStats().addBuildMysqlPacketCostNs(System.nanoTime() - buildPacketStartNs);
                }
                if (null != splitPackets) {
                    this.writeBuffers(splitPackets);
                }
                else {
                    final long sendStartNs = System.nanoTime();
                    this.mysqlOutput.write(packetToSend.getByteBuffer(), 0, packetLen);
                    this.mysqlOutput.flush();
                    this.connection.getConnectionStats().addSocketSendCostNs(System.nanoTime() - sendStartNs);
                    this.connection.getConnectionStats().addRequestSendBytes(packetLen);
                }
            }
            if (this.enablePacketDebug) {
                this.enqueuePacketForDebugging(true, false, packetLen + 5, this.packetHeaderBuf, packet);
            }
            if (packet == this.sharedSendPacket) {
                this.reclaimLargeSharedSendPacket();
            }
            if (this.connection.getMaintainTimeStats()) {
                this.lastPacketSentTimeMs = System.currentTimeMillis();
            }
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }
    
    private void writeBuffers(final Buffer[] splitPackets) throws IOException {
        if (null != splitPackets) {
            for (final Buffer splitPacket : splitPackets) {
                final long sendStartNs = System.nanoTime();
                final int packetLen = splitPacket.getPosition();
                this.mysqlOutput.write(splitPacket.getByteBuffer(), 0, packetLen);
                this.mysqlOutput.flush();
                this.connection.getConnectionStats().addSocketSendCostNs(System.nanoTime() - sendStartNs);
                this.connection.getConnectionStats().addRequestSendBytes(packetLen);
            }
        }
    }
    
    private final ResultSetImpl sendFileToServer(final StatementImpl callingStatement, final String fileName) throws SQLException {
        if (this.useObProto20) {
            throw SQLError.createSQLException("proto20 not supported now", "0A000", this.getExceptionInterceptor());
        }
        if (this.useCompression) {
            ++this.compressedPacketSequence;
        }
        Buffer filePacket = (this.loadFileBufRef == null) ? null : this.loadFileBufRef.get();
        final int bigPacketLength = Math.min(this.connection.getMaxAllowedPacket() - 12, this.alignPacketSize(this.connection.getMaxAllowedPacket() - 16, 4096) - 12);
        final int oneMeg = 1048576;
        final int smallerPacketSizeAligned = Math.min(oneMeg - 12, this.alignPacketSize(oneMeg - 16, 4096) - 12);
        final int packetLength = Math.min(smallerPacketSizeAligned, bigPacketLength);
        if (filePacket == null) {
            try {
                filePacket = new Buffer(packetLength + 4);
                this.loadFileBufRef = new SoftReference<Buffer>(filePacket);
            }
            catch (OutOfMemoryError oom) {
                throw SQLError.createSQLException("Could not allocate packet of " + packetLength + " bytes required for LOAD DATA LOCAL INFILE operation. Try increasing max heap allocation for JVM or decreasing server variable 'max_allowed_packet'", "S1001", this.getExceptionInterceptor());
            }
        }
        filePacket.clear();
        this.send(filePacket, 0);
        final byte[] fileBuf = new byte[packetLength];
        BufferedInputStream fileIn = null;
        try {
            if (!this.connection.getAllowLoadLocalInfile()) {
                throw SQLError.createSQLException(Messages.getString("MysqlIO.LoadDataLocalNotAllowed"), "S1000", this.getExceptionInterceptor());
            }
            InputStream hookedStream = null;
            if (callingStatement != null) {
                hookedStream = callingStatement.getLocalInfileInputStream();
            }
            if (hookedStream != null) {
                fileIn = new BufferedInputStream(hookedStream);
            }
            else if (!this.connection.getAllowUrlInLocalInfile()) {
                fileIn = new BufferedInputStream(new FileInputStream(fileName));
            }
            else if (fileName.indexOf(58) != -1) {
                try {
                    final URL urlFromFileName = new URL(fileName);
                    fileIn = new BufferedInputStream(urlFromFileName.openStream());
                }
                catch (MalformedURLException badUrlEx) {
                    fileIn = new BufferedInputStream(new FileInputStream(fileName));
                }
            }
            else {
                fileIn = new BufferedInputStream(new FileInputStream(fileName));
            }
            int bytesRead = 0;
            while ((bytesRead = fileIn.read(fileBuf)) != -1) {
                filePacket.clear();
                filePacket.writeBytesNoNull(fileBuf, 0, bytesRead);
                this.send(filePacket, filePacket.getPosition());
            }
        }
        catch (IOException ioEx) {
            final StringBuilder messageBuf = new StringBuilder(Messages.getString("MysqlIO.60"));
            if (fileName != null && !this.connection.getParanoid()) {
                messageBuf.append("'");
                messageBuf.append(fileName);
                messageBuf.append("'");
            }
            messageBuf.append(Messages.getString("MysqlIO.63"));
            if (!this.connection.getParanoid()) {
                messageBuf.append(Messages.getString("MysqlIO.64"));
                messageBuf.append(Util.stackTraceToString(ioEx));
            }
            throw SQLError.createSQLException(messageBuf.toString(), "S1009", this.getExceptionInterceptor());
        }
        finally {
            if (fileIn != null) {
                try {
                    fileIn.close();
                }
                catch (Exception ex) {
                    final SQLException sqlEx = SQLError.createSQLException(Messages.getString("MysqlIO.65"), "S1000", ex, this.getExceptionInterceptor());
                    throw sqlEx;
                }
                fileIn = null;
            }
            else {
                filePacket.clear();
                this.send(filePacket, filePacket.getPosition());
                this.checkErrorPacket();
            }
        }
        filePacket.clear();
        this.send(filePacket, filePacket.getPosition());
        final Buffer resultPacket = this.checkErrorPacket();
        return this.buildResultSetWithUpdates(callingStatement, resultPacket);
    }
    
    private Buffer checkErrorPacket(final int command) throws SQLException {
        Buffer resultPacket = null;
        this.serverStatus = 0;
        try {
            resultPacket = this.reuseAndReadPacket(this.reusablePacket);
        }
        catch (SQLException sqlEx) {
            throw sqlEx;
        }
        catch (Exception fallThru) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, fallThru, this.getExceptionInterceptor());
        }
        this.checkErrorPacket(resultPacket, command);
        return resultPacket;
    }
    
    private void checkErrorPacket(final Buffer resultPacket) throws SQLException {
        this.checkErrorPacket(resultPacket, -1);
    }
    
    private void checkErrorPacket(final Buffer resultPacket, final int command) throws SQLException {
        final int statusCode = resultPacket.readByte();
        if (statusCode != -1) {
            if (0 == statusCode) {
                if (this.inAuthProcess) {
                    this.checkOKPacket(resultPacket, 1);
                }
                else if (this.isEnableExtraOKPacket() && -1 != command && 22 != command) {
                    this.checkOKPacket(resultPacket, 1);
                }
            }
            return;
        }
        this.readAndCheckExtraOKPacket();
        int errno = 2000;
        if (this.protocolVersion > 9) {
            errno = resultPacket.readInt();
            String xOpen = null;
            String serverErrorMessage = resultPacket.readString(this.connection.getErrorMessageEncoding(), this.getExceptionInterceptor());
            if (serverErrorMessage.charAt(0) == '#') {
                if (serverErrorMessage.length() > 6) {
                    xOpen = serverErrorMessage.substring(1, 6);
                    serverErrorMessage = serverErrorMessage.substring(6);
                    if (xOpen.equals("HY000")) {
                        xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
                    }
                }
                else {
                    xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
                }
            }
            else {
                xOpen = SQLError.mysqlToSqlState(errno, this.connection.getUseSqlStateCodes());
            }
            this.clearInputStream();
            final StringBuilder errorBuf = new StringBuilder();
            final String xOpenErrorMessage = SQLError.get(xOpen);
            if (!this.connection.getUseOnlyServerErrorMessages() && xOpenErrorMessage != null) {
                errorBuf.append(xOpenErrorMessage);
                errorBuf.append(Messages.getString("MysqlIO.68"));
            }
            errorBuf.append(serverErrorMessage);
            if (!this.connection.getUseOnlyServerErrorMessages() && xOpenErrorMessage != null) {
                errorBuf.append("\"");
            }
            this.appendDeadlockStatusInformation(xOpen, errorBuf);
            if (xOpen != null && xOpen.startsWith("22")) {
                throw new MysqlDataTruncation(errorBuf.toString(), 0, true, false, 0, 0, errno);
            }
            throw SQLError.createSQLException(errorBuf.toString(), xOpen, errno, false, this.getExceptionInterceptor(), this.connection);
        }
        else {
            final String serverErrorMessage = resultPacket.readString(this.connection.getErrorMessageEncoding(), this.getExceptionInterceptor());
            this.clearInputStream();
            if (serverErrorMessage.indexOf(Messages.getString("MysqlIO.70")) != -1) {
                throw SQLError.createSQLException(SQLError.get("S0022") + ", " + serverErrorMessage, "S0022", -1, false, this.getExceptionInterceptor(), this.connection);
            }
            final StringBuilder errorBuf2 = new StringBuilder(Messages.getString("MysqlIO.72"));
            errorBuf2.append(serverErrorMessage);
            errorBuf2.append("\"");
            throw SQLError.createSQLException(SQLError.get("S1000") + ", " + errorBuf2.toString(), "S1000", -1, false, this.getExceptionInterceptor(), this.connection);
        }
    }
    
    private void appendDeadlockStatusInformation(final String xOpen, final StringBuilder errorBuf) throws SQLException {
        if (this.connection.getIncludeInnodbStatusInDeadlockExceptions() && xOpen != null && (xOpen.startsWith("40") || xOpen.startsWith("41")) && this.streamingData == null) {
            ResultSet rs = null;
            try {
                rs = this.sqlQueryDirect(null, "SHOW ENGINE INNODB STATUS", this.connection.getEncoding(), null, -1, 1003, 1007, false, this.connection.getCatalog(), null);
                if (rs.next()) {
                    errorBuf.append("\n\n");
                    errorBuf.append(rs.getString("Status"));
                }
                else {
                    errorBuf.append("\n\n");
                    errorBuf.append(Messages.getString("MysqlIO.NoInnoDBStatusFound"));
                }
            }
            catch (Exception ex) {
                errorBuf.append("\n\n");
                errorBuf.append(Messages.getString("MysqlIO.InnoDBStatusFailed"));
                errorBuf.append("\n\n");
                errorBuf.append(Util.stackTraceToString(ex));
            }
            finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
        if (this.connection.getIncludeThreadDumpInDeadlockExceptions()) {
            errorBuf.append("\n\n*** Java threads running at time of deadlock ***\n\n");
            final ThreadMXBean threadMBean = ManagementFactory.getThreadMXBean();
            final long[] threadIds = threadMBean.getAllThreadIds();
            final ThreadInfo[] threads = threadMBean.getThreadInfo(threadIds, Integer.MAX_VALUE);
            final List<ThreadInfo> activeThreads = new ArrayList<ThreadInfo>();
            for (final ThreadInfo info : threads) {
                if (info != null) {
                    activeThreads.add(info);
                }
            }
            for (final ThreadInfo threadInfo : activeThreads) {
                errorBuf.append('\"');
                errorBuf.append(threadInfo.getThreadName());
                errorBuf.append("\" tid=");
                errorBuf.append(threadInfo.getThreadId());
                errorBuf.append(" ");
                errorBuf.append(threadInfo.getThreadState());
                if (threadInfo.getLockName() != null) {
                    errorBuf.append(" on lock=" + threadInfo.getLockName());
                }
                if (threadInfo.isSuspended()) {
                    errorBuf.append(" (suspended)");
                }
                if (threadInfo.isInNative()) {
                    errorBuf.append(" (running in native)");
                }
                final StackTraceElement[] stackTrace = threadInfo.getStackTrace();
                if (stackTrace.length > 0) {
                    errorBuf.append(" in ");
                    errorBuf.append(stackTrace[0].getClassName());
                    errorBuf.append(".");
                    errorBuf.append(stackTrace[0].getMethodName());
                    errorBuf.append("()");
                }
                errorBuf.append("\n");
                if (threadInfo.getLockOwnerName() != null) {
                    errorBuf.append("\t owned by " + threadInfo.getLockOwnerName() + " Id=" + threadInfo.getLockOwnerId());
                    errorBuf.append("\n");
                }
                for (int j = 0; j < stackTrace.length; ++j) {
                    final StackTraceElement ste = stackTrace[j];
                    errorBuf.append("\tat " + ste.toString());
                    errorBuf.append("\n");
                }
            }
        }
    }
    
    private final void sendSplitPackets(final Buffer packet, final int packetLen) throws SQLException {
        try {
            Buffer packetToSend = (null == this.splitBufRef) ? null : this.splitBufRef.get();
            Buffer protoBuffer = ((!this.useCompression && !this.useObProto20) || null == this.protoBufRef) ? null : this.protoBufRef.get();
            if (null == packetToSend) {
                packetToSend = new Buffer(this.maxThreeBytes + 4);
                this.splitBufRef = new SoftReference<Buffer>(packetToSend);
            }
            if (this.useCompression || this.useObProto20) {
                final int cbuflen = packetLen + (packetLen / this.maxThreeBytes + 1) * 4;
                if (null == protoBuffer) {
                    protoBuffer = new Buffer(cbuflen);
                    this.protoBufRef = new SoftReference<Buffer>(protoBuffer);
                }
                else if (protoBuffer.getBufLength() < cbuflen) {
                    protoBuffer.setPosition(protoBuffer.getBufLength());
                    protoBuffer.ensureCapacity(cbuflen - protoBuffer.getBufLength());
                }
            }
            int len = packetLen - 4;
            int splitSize = this.maxThreeBytes;
            int originalPacketPos = 4;
            final byte[] origPacketBytes = packet.getByteBuffer();
            int toSplitPosition = 0;
            while (len >= 0) {
                ++this.packetSequence;
                if (len < splitSize) {
                    splitSize = len;
                }
                packetToSend.setPosition(0);
                packetToSend.writeLongInt(splitSize);
                packetToSend.writeByte(this.packetSequence);
                if (len > 0) {
                    System.arraycopy(origPacketBytes, originalPacketPos, packetToSend.getByteBuffer(), 4, splitSize);
                }
                if (this.useCompression || this.useObProto20) {
                    System.arraycopy(packetToSend.getByteBuffer(), 0, protoBuffer.getByteBuffer(), toSplitPosition, 4 + splitSize);
                    toSplitPosition += 4 + splitSize;
                }
                else {
                    final long sendStartNs = System.nanoTime();
                    this.mysqlOutput.write(packetToSend.getByteBuffer(), 0, 4 + splitSize);
                    this.mysqlOutput.flush();
                    this.connection.getConnectionStats().addSocketSendCostNs(System.nanoTime() - sendStartNs);
                    this.connection.getConnectionStats().addRequestSendBytes(4 + splitSize);
                }
                originalPacketPos += splitSize;
                len -= this.maxThreeBytes;
            }
            boolean isLast = false;
            final int compressSplitSize = this.maxThreeBytes - 3;
            if (this.useCompression || this.useObProto20) {
                len = toSplitPosition;
                toSplitPosition = 0;
                if (this.useCompression) {
                    splitSize = compressSplitSize;
                }
                else if (this.useObProto20) {
                    splitSize = this.maxPacketLenForProto20;
                }
                while (len >= 0) {
                    if (len < splitSize) {
                        splitSize = len;
                        isLast = true;
                    }
                    Buffer[] splitPackets = null;
                    if (this.useObProto20) {
                        splitPackets = this.splitProto20Packets(protoBuffer, toSplitPosition, splitSize, isLast);
                    }
                    else if (this.useCompression) {
                        splitPackets = this.compressPackets(protoBuffer, toSplitPosition, splitSize);
                    }
                    this.writeBuffers(splitPackets);
                    toSplitPosition += splitSize;
                    if (this.useCompression) {
                        len -= compressSplitSize;
                    }
                    else {
                        if (!this.useObProto20) {
                            continue;
                        }
                        len -= this.maxPacketLenForProto20;
                    }
                }
            }
        }
        catch (IOException ioEx) {
            throw SQLError.createCommunicationsException(this.connection, this.lastPacketSentTimeMs, this.lastPacketReceivedTimeMs, ioEx, this.getExceptionInterceptor());
        }
    }
    
    private void reclaimLargeSharedSendPacket() {
        if (this.sharedSendPacket != null && this.sharedSendPacket.getCapacity() > 1048576) {
            this.sharedSendPacket = new Buffer(1024);
        }
    }
    
    boolean hadWarnings() {
        return this.hadWarnings;
    }
    
    void scanForAndThrowDataTruncation() throws SQLException {
        if (this.streamingData == null && this.versionMeetsMinimum(4, 1, 0) && this.connection.getJdbcCompliantTruncation() && this.warningCount > 0) {
            SQLError.convertShowWarningsToSQLWarnings(this.connection, this.warningCount, true);
        }
    }
    
    private void secureAuth(Buffer packet, final int packLength, final String user, final String password, final String database, final boolean writeClientParams) throws SQLException {
        if (packet == null) {
            packet = new Buffer(packLength);
        }
        if (writeClientParams) {
            if (this.use41Extensions) {
                if (this.versionMeetsMinimum(4, 1, 1)) {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);
                    packet.writeByte((byte)8);
                    packet.writeBytesNoNull(new byte[23]);
                }
                else {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);
                }
            }
            else {
                packet.writeInt((int)this.clientParam);
                packet.writeLongInt(this.maxThreeBytes);
            }
        }
        packet.writeString(user, "Cp1252", this.connection);
        if (password.length() != 0) {
            packet.writeString("xxxxxxxx", "Cp1252", this.connection);
        }
        else {
            packet.writeString("", "Cp1252", this.connection);
        }
        if (this.useConnectWithDb) {
            packet.writeString(database, "Cp1252", this.connection);
        }
        this.send(packet, packet.getPosition());
        if (password.length() > 0) {
            final Buffer b = this.readPacket();
            b.setPosition(0);
            final byte[] replyAsBytes = b.getByteBuffer();
            if (replyAsBytes.length == 24 && replyAsBytes[0] != 0) {
                if (replyAsBytes[0] != 42) {
                    try {
                        final byte[] buff = Security.passwordHashStage1(password);
                        byte[] passwordHash = new byte[buff.length];
                        System.arraycopy(buff, 0, passwordHash, 0, buff.length);
                        passwordHash = Security.passwordHashStage2(passwordHash, replyAsBytes);
                        final byte[] packetDataAfterSalt = new byte[replyAsBytes.length - 4];
                        System.arraycopy(replyAsBytes, 4, packetDataAfterSalt, 0, replyAsBytes.length - 4);
                        final byte[] mysqlScrambleBuff = new byte[20];
                        Security.xorString(packetDataAfterSalt, mysqlScrambleBuff, passwordHash, 20);
                        Security.xorString(mysqlScrambleBuff, buff, buff, 20);
                        final Buffer packet2 = new Buffer(25);
                        packet2.writeBytesNoNull(buff);
                        ++this.packetSequence;
                        this.send(packet2, 24);
                        return;
                    }
                    catch (NoSuchAlgorithmException nse) {
                        throw SQLError.createSQLException(Messages.getString("MysqlIO.91") + Messages.getString("MysqlIO.92"), "S1000", this.getExceptionInterceptor());
                    }
                }
                try {
                    final byte[] passwordHash2 = Security.createKeyFromOldPassword(password);
                    final byte[] netReadPos4 = new byte[replyAsBytes.length - 4];
                    System.arraycopy(replyAsBytes, 4, netReadPos4, 0, replyAsBytes.length - 4);
                    final byte[] mysqlScrambleBuff2 = new byte[20];
                    Security.xorString(netReadPos4, mysqlScrambleBuff2, passwordHash2, 20);
                    final String scrambledPassword = Util.scramble(StringUtils.toString(mysqlScrambleBuff2), password);
                    final Buffer packet2 = new Buffer(packLength);
                    packet2.writeString(scrambledPassword, "Cp1252", this.connection);
                    ++this.packetSequence;
                    this.send(packet2, 24);
                }
                catch (NoSuchAlgorithmException nse) {
                    throw SQLError.createSQLException(Messages.getString("MysqlIO.91") + Messages.getString("MysqlIO.92"), "S1000", this.getExceptionInterceptor());
                }
            }
        }
    }
    
    void secureAuth411(Buffer packet, final int packLength, final String user, final String password, final String database, final boolean writeClientParams) throws SQLException {
        final String enc = this.getEncodingForHandshake();
        if (packet == null) {
            packet = new Buffer(packLength);
        }
        if (writeClientParams) {
            if (this.use41Extensions) {
                if (this.versionMeetsMinimum(4, 1, 1)) {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);
                    this.appendCharsetByteForHandshake(packet, enc);
                    packet.writeBytesNoNull(new byte[23]);
                }
                else {
                    packet.writeLong(this.clientParam);
                    packet.writeLong(this.maxThreeBytes);
                }
            }
            else {
                packet.writeInt((int)this.clientParam);
                packet.writeLongInt(this.maxThreeBytes);
            }
        }
        if (user != null) {
            packet.writeString(user, enc, this.connection);
        }
        Label_0254: {
            if (password.length() != 0) {
                packet.writeByte((byte)20);
                try {
                    packet.writeBytesNoNull(Security.scramble411(password, this.seed, this.connection.getPasswordCharacterEncoding()));
                    break Label_0254;
                }
                catch (NoSuchAlgorithmException nse) {
                    throw SQLError.createSQLException(Messages.getString("MysqlIO.91") + Messages.getString("MysqlIO.92"), "S1000", this.getExceptionInterceptor());
                }
                catch (UnsupportedEncodingException e) {
                    throw SQLError.createSQLException(Messages.getString("MysqlIO.91") + Messages.getString("MysqlIO.92"), "S1000", this.getExceptionInterceptor());
                }
            }
            packet.writeByte((byte)0);
        }
        if (this.useConnectWithDb) {
            packet.writeString(database, enc, this.connection);
        }
        else {
            packet.writeByte((byte)0);
        }
        if ((this.serverCapabilities & 0x100000) != 0x0) {
            this.sendConnectionAttributes(packet, enc, this.connection);
        }
        this.send(packet, packet.getPosition());
        final byte packetSequence = this.packetSequence;
        this.packetSequence = (byte)(packetSequence + 1);
        byte savePacketSequence = packetSequence;
        final Buffer reply = this.checkErrorPacket();
        if (reply.isAuthMethodSwitchRequestPacket()) {
            ++savePacketSequence;
            this.packetSequence = savePacketSequence;
            packet.clear();
            final String seed323 = this.seed.substring(0, 8);
            packet.writeString(Util.newCrypt(password, seed323, this.connection.getPasswordCharacterEncoding()));
            this.send(packet, packet.getPosition());
            this.checkErrorPacket();
        }
    }
    
    private final ResultSetRow unpackBinaryResultSetRow(final Field[] fields, final Buffer binaryData, final int resultSetConcurrency) throws SQLException {
        final int numFields = fields.length;
        final byte[][] unpackedRowData = new byte[numFields][];
        final ComplexData[] complexRowData = new ComplexData[numFields];
        final int nullCount = (numFields + 9) / 8;
        int nullMaskPos = binaryData.getPosition();
        binaryData.setPosition(nullMaskPos + nullCount);
        int bit = 4;
        for (int i = 0; i < numFields; ++i) {
            if ((binaryData.readByte(nullMaskPos) & bit) != 0x0) {
                unpackedRowData[i] = null;
            }
            else {
                complexRowData[i] = null;
                if (resultSetConcurrency != 1008) {
                    this.extractNativeEncodedColumn(binaryData, fields, i, unpackedRowData, complexRowData);
                }
                else {
                    this.unpackNativeEncodedColumn(binaryData, fields, i, unpackedRowData, complexRowData);
                }
            }
            if (((bit <<= 1) & 0xFF) == 0x0) {
                bit = 1;
                ++nullMaskPos;
            }
        }
        return new ByteArrayRow(unpackedRowData, complexRowData, this.getExceptionInterceptor());
    }
    
    private final void extractNativeEncodedColumn(final Buffer binaryData, final Field[] fields, final int columnIndex, final byte[][] unpackedRowData, final ComplexData[] complexData) throws SQLException {
        final Field curField = fields[columnIndex];
        switch (curField.getMysqlType()) {
            case 6: {
                break;
            }
            case 1: {
                unpackedRowData[columnIndex] = new byte[] { binaryData.readByte() };
                break;
            }
            case 2:
            case 13: {
                unpackedRowData[columnIndex] = binaryData.getBytes(2);
                break;
            }
            case 163: {
                complexData[columnIndex] = this.getComplexCursor(binaryData, curField.getName());
                break;
            }
            case 3:
            case 9: {
                unpackedRowData[columnIndex] = binaryData.getBytes(4);
                break;
            }
            case 8: {
                unpackedRowData[columnIndex] = binaryData.getBytes(8);
                break;
            }
            case 4: {
                unpackedRowData[columnIndex] = binaryData.getBytes(4);
                break;
            }
            case 5: {
                unpackedRowData[columnIndex] = binaryData.getBytes(8);
                break;
            }
            case 11: {
                final int length = (int)binaryData.readFieldLength();
                unpackedRowData[columnIndex] = binaryData.getBytes(length);
                break;
            }
            case 10: {
                final int length = (int)binaryData.readFieldLength();
                unpackedRowData[columnIndex] = binaryData.getBytes(length);
                break;
            }
            case 7:
            case 12: {
                final int length = (int)binaryData.readFieldLength();
                unpackedRowData[columnIndex] = binaryData.getBytes(length);
                break;
            }
            case 0:
            case 15:
            case 16:
            case 200:
            case 201:
            case 202:
            case 203:
            case 206:
            case 207:
            case 208:
            case 209:
            case 245:
            case 246:
            case 249:
            case 250:
            case 251:
            case 252:
            case 253:
            case 254:
            case 255: {
                unpackedRowData[columnIndex] = binaryData.readLenByteArray(0);
                break;
            }
            case 160: {
                complexData[columnIndex] = this.getComplexField(binaryData, curField.getComplexTypeName());
                break;
            }
            case 204:
            case 205: {
                final int length = (int)binaryData.readFieldLength();
                unpackedRowData[columnIndex] = binaryData.getBytes(length);
                break;
            }
            case 210:
            case 211: {
                final int length = (int)binaryData.readFieldLength();
                unpackedRowData[columnIndex] = binaryData.getBytes(length);
                break;
            }
            default: {
                throw SQLError.createSQLException(Messages.getString("MysqlIO.97") + curField.getMysqlType() + Messages.getString("MysqlIO.98") + columnIndex + Messages.getString("MysqlIO.99") + fields.length + Messages.getString("MysqlIO.100"), "S1000", this.getExceptionInterceptor());
            }
        }
    }
    
    private final void unpackNativeEncodedColumn(final Buffer binaryData, final Field[] fields, final int columnIndex, final byte[][] unpackedRowData, final ComplexData[] complexData) throws SQLException {
        final Field curField = fields[columnIndex];
        switch (curField.getMysqlType()) {
            case 6: {
                break;
            }
            case 1: {
                final byte tinyVal = binaryData.readByte();
                if (!curField.isUnsigned()) {
                    unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(tinyVal));
                    break;
                }
                final short unsignedTinyVal = (short)(tinyVal & 0xFF);
                unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(unsignedTinyVal));
                break;
            }
            case 2:
            case 13: {
                final short shortVal = (short)binaryData.readInt();
                if (!curField.isUnsigned()) {
                    unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(shortVal));
                    break;
                }
                final int unsignedShortVal = shortVal & 0xFFFF;
                unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(unsignedShortVal));
                break;
            }
            case 3:
            case 9: {
                final int intVal = (int)binaryData.readLong();
                if (!curField.isUnsigned()) {
                    unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(intVal));
                    break;
                }
                final long longVal = (long)intVal & 0xFFFFFFFFL;
                unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(longVal));
                break;
            }
            case 8: {
                final long longVal = binaryData.readLongLong();
                if (!curField.isUnsigned()) {
                    unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(longVal));
                    break;
                }
                final BigInteger asBigInteger = ResultSetImpl.convertLongToUlong(longVal);
                unpackedRowData[columnIndex] = StringUtils.getBytes(asBigInteger.toString());
                break;
            }
            case 4: {
                final float floatVal = Float.intBitsToFloat(binaryData.readIntAsLong());
                unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(floatVal));
                break;
            }
            case 5: {
                final double doubleVal = Double.longBitsToDouble(binaryData.readLongLong());
                unpackedRowData[columnIndex] = StringUtils.getBytes(String.valueOf(doubleVal));
                break;
            }
            case 11: {
                final int length = (int)binaryData.readFieldLength();
                int hour = 0;
                int minute = 0;
                int seconds = 0;
                if (length != 0) {
                    binaryData.readByte();
                    binaryData.readLong();
                    hour = binaryData.readByte();
                    minute = binaryData.readByte();
                    seconds = binaryData.readByte();
                    if (length > 8) {
                        binaryData.readLong();
                    }
                }
                final byte[] timeAsBytes = { (byte)Character.forDigit(hour / 10, 10), (byte)Character.forDigit(hour % 10, 10), 58, (byte)Character.forDigit(minute / 10, 10), (byte)Character.forDigit(minute % 10, 10), 58, (byte)Character.forDigit(seconds / 10, 10), (byte)Character.forDigit(seconds % 10, 10) };
                unpackedRowData[columnIndex] = timeAsBytes;
                break;
            }
            case 10: {
                final int length = (int)binaryData.readFieldLength();
                int year = 0;
                int month = 0;
                int day = 0;
                final int hour = 0;
                final int minute = 0;
                final int seconds = 0;
                if (length != 0) {
                    year = binaryData.readInt();
                    month = binaryData.readByte();
                    day = binaryData.readByte();
                }
                if (year == 0 && month == 0 && day == 0) {
                    if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                        unpackedRowData[columnIndex] = null;
                        break;
                    }
                    if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                        throw SQLError.createSQLException("Value '0000-00-00' can not be represented as java.sql.Date", "S1009", this.getExceptionInterceptor());
                    }
                    year = 1;
                    month = 1;
                    day = 1;
                }
                final byte[] dateAsBytes = new byte[10];
                dateAsBytes[0] = (byte)Character.forDigit(year / 1000, 10);
                final int after1000 = year % 1000;
                dateAsBytes[1] = (byte)Character.forDigit(after1000 / 100, 10);
                final int after1001 = after1000 % 100;
                dateAsBytes[2] = (byte)Character.forDigit(after1001 / 10, 10);
                dateAsBytes[3] = (byte)Character.forDigit(after1001 % 10, 10);
                dateAsBytes[4] = 45;
                dateAsBytes[5] = (byte)Character.forDigit(month / 10, 10);
                dateAsBytes[6] = (byte)Character.forDigit(month % 10, 10);
                dateAsBytes[7] = 45;
                dateAsBytes[8] = (byte)Character.forDigit(day / 10, 10);
                dateAsBytes[9] = (byte)Character.forDigit(day % 10, 10);
                unpackedRowData[columnIndex] = dateAsBytes;
                break;
            }
            case 7:
            case 12: {
                final int length = (int)binaryData.readFieldLength();
                int year = 0;
                int month = 0;
                int day = 0;
                int hour = 0;
                int minute = 0;
                int seconds = 0;
                final int nanos = 0;
                if (length != 0) {
                    year = binaryData.readInt();
                    month = binaryData.readByte();
                    day = binaryData.readByte();
                    if (length > 4) {
                        hour = binaryData.readByte();
                        minute = binaryData.readByte();
                        seconds = binaryData.readByte();
                    }
                }
                if (year == 0 && month == 0 && day == 0) {
                    if ("convertToNull".equals(this.connection.getZeroDateTimeBehavior())) {
                        unpackedRowData[columnIndex] = null;
                        break;
                    }
                    if ("exception".equals(this.connection.getZeroDateTimeBehavior())) {
                        throw SQLError.createSQLException("Value '0000-00-00' can not be represented as java.sql.Timestamp", "S1009", this.getExceptionInterceptor());
                    }
                    year = 1;
                    month = 1;
                    day = 1;
                }
                int stringLength = 19;
                final byte[] nanosAsBytes = StringUtils.getBytes(Integer.toString(nanos));
                stringLength += 1 + nanosAsBytes.length;
                final byte[] datetimeAsBytes = new byte[stringLength];
                datetimeAsBytes[0] = (byte)Character.forDigit(year / 1000, 10);
                final int after1000 = year % 1000;
                datetimeAsBytes[1] = (byte)Character.forDigit(after1000 / 100, 10);
                final int after1001 = after1000 % 100;
                datetimeAsBytes[2] = (byte)Character.forDigit(after1001 / 10, 10);
                datetimeAsBytes[3] = (byte)Character.forDigit(after1001 % 10, 10);
                datetimeAsBytes[4] = 45;
                datetimeAsBytes[5] = (byte)Character.forDigit(month / 10, 10);
                datetimeAsBytes[6] = (byte)Character.forDigit(month % 10, 10);
                datetimeAsBytes[7] = 45;
                datetimeAsBytes[8] = (byte)Character.forDigit(day / 10, 10);
                datetimeAsBytes[9] = (byte)Character.forDigit(day % 10, 10);
                datetimeAsBytes[10] = 32;
                datetimeAsBytes[11] = (byte)Character.forDigit(hour / 10, 10);
                datetimeAsBytes[12] = (byte)Character.forDigit(hour % 10, 10);
                datetimeAsBytes[13] = 58;
                datetimeAsBytes[14] = (byte)Character.forDigit(minute / 10, 10);
                datetimeAsBytes[15] = (byte)Character.forDigit(minute % 10, 10);
                datetimeAsBytes[16] = 58;
                datetimeAsBytes[17] = (byte)Character.forDigit(seconds / 10, 10);
                datetimeAsBytes[18] = (byte)Character.forDigit(seconds % 10, 10);
                datetimeAsBytes[19] = 46;
                final int nanosOffset = 20;
                System.arraycopy(nanosAsBytes, 0, datetimeAsBytes, 20, nanosAsBytes.length);
                unpackedRowData[columnIndex] = datetimeAsBytes;
                break;
            }
            case 0:
            case 15:
            case 16:
            case 200:
            case 201:
            case 202:
            case 203:
            case 206:
            case 207:
            case 208:
            case 209:
            case 245:
            case 246:
            case 249:
            case 250:
            case 251:
            case 252:
            case 253:
            case 254: {
                unpackedRowData[columnIndex] = binaryData.readLenByteArray(0);
                break;
            }
            case 160: {
                complexData[columnIndex] = this.getComplexField(binaryData, curField.getComplexTypeName());
                break;
            }
            case 163: {
                complexData[columnIndex] = this.getComplexCursor(binaryData, curField.getName());
                break;
            }
            case 204:
            case 205: {
                final int length = (int)binaryData.readFieldLength();
                unpackedRowData[columnIndex] = binaryData.getBytes(length);
                break;
            }
            default: {
                throw SQLError.createSQLException(Messages.getString("MysqlIO.97") + curField.getMysqlType() + Messages.getString("MysqlIO.98") + columnIndex + Messages.getString("MysqlIO.99") + fields.length + Messages.getString("MysqlIO.100"), "S1000", this.getExceptionInterceptor());
            }
        }
    }
    
    private ComplexData getComplexCursor(final Buffer packet, final String typeName) throws SQLException {
        final ComplexDataType type = new ComplexDataType(typeName, typeName, 163);
        final ComplexData value = new ComplexData(type);
        final int id = (int)packet.readLong();
        value.setAttrCount(1);
        final RowObCursorData rowObCursorData = new RowObCursorData(id, true);
        value.addAttrData(0, rowObCursorData);
        return value;
    }
    
    private ComplexData getComplexField(final Buffer packet, final String typeName) throws SQLException {
        ComplexData value = null;
        final ComplexDataType type = ((ConnectionImpl)this.connection).getComplexDataType(typeName);
        if (null == type || !type.isValid()) {
            throw new SQLException(String.format("invalid complex type, check if exists, typeName=%s", typeName));
        }
        switch (type.getType()) {
            case 4: {
                value = this.getComplexArray(packet, type);
                break;
            }
            case 3: {
                value = this.getComplexStruct(packet, type);
                break;
            }
            default: {
                throw new SQLException(String.format("invalid complex type, check if exists, typeName=%s", typeName));
            }
        }
        return value;
    }
    
    private ComplexData getComplexArray(final Buffer packet, final ComplexDataType type) throws SQLException {
        final ComplexData array = new ArrayImpl(type);
        final int attrCount = (int)packet.readFieldLength();
        array.setAttrCount(attrCount);
        final int curPos = packet.getPosition();
        final byte[] nullBitsBuffer = packet.getBytes(curPos, (attrCount + 7 + 2) / 8);
        packet.setPosition(curPos + (attrCount + 7 + 2) / 8);
        for (int i = 0; i < attrCount; ++i) {
            if ((nullBitsBuffer[(i + 2) / 8] & 1 << (i + 2) % 8) == 0x0) {
                final Object value = this.getComplexAttrData(packet, type.getAttrType(0));
                array.addAttrData(i, value);
            }
            else {
                array.addAttrData(i, null);
            }
        }
        return array;
    }
    
    public ComplexData getComplexStruct(final Buffer packet, final ComplexDataType type) throws SQLException {
        final ComplexData struct = new StructImpl(type);
        final int attrCount = type.getAttrCount();
        struct.setAttrCount(attrCount);
        final int curPos = packet.getPosition();
        final byte[] nullBitsBuffer = packet.getBytes(curPos, (attrCount + 7 + 2) / 8);
        packet.setPosition(curPos + (attrCount + 7 + 2) / 8);
        for (int i = 0; i < attrCount; ++i) {
            if ((nullBitsBuffer[(i + 2) / 8] & 1 << (i + 2) % 8) == 0x0) {
                final Object value = this.getComplexAttrData(packet, type.getAttrType(i));
                struct.addAttrData(i, value);
            }
            else {
                struct.addAttrData(i, null);
            }
        }
        return struct;
    }
    
    private Object getComplexAttrData(final Buffer packet, final ComplexDataType type) throws SQLException {
        Object value = null;
        switch (type.getType()) {
            case 0: {
                value = new BigDecimal(StringUtils.toString(packet.readLenByteArray(0)));
                break;
            }
            case 1:
            case 6: {
                value = new String(StringUtils.toString(packet.readLenByteArray(0)));
                break;
            }
            case 2: {
                value = this.getComplexDate(packet.readLenByteArray(0));
                break;
            }
            case 4: {
                value = this.getComplexArray(packet, type);
                break;
            }
            case 3: {
                value = this.getComplexStruct(packet, type);
                break;
            }
            default: {
                throw new SQLException("unsupported complex data type");
            }
        }
        return value;
    }
    
    private Timestamp getComplexDate(final byte[] bits) {
        int year = 0;
        int month = 0;
        int day = 0;
        int hour = 0;
        int minute = 0;
        int seconds = 0;
        int nanos = 0;
        if (null == bits) {
            return null;
        }
        final int length = bits.length;
        if (length != 0) {
            year = ((bits[0] & 0xFF) | (bits[1] & 0xFF) << 8);
            month = bits[2];
            day = bits[3];
            if (length > 4) {
                hour = bits[4];
                minute = bits[5];
                seconds = bits[6];
            }
            if (length > 7) {
                nanos = ((bits[7] & 0xFF) | (bits[8] & 0xFF) << 8 | (bits[9] & 0xFF) << 16 | (bits[10] & 0xFF) << 24) * 1000;
            }
        }
        final Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, day, hour, minute, seconds);
        final long tsAsMillis = cal.getTimeInMillis();
        final Timestamp ts = new Timestamp(tsAsMillis);
        ts.setNanos(nanos);
        return ts;
    }
    
    private void negotiateSSLConnection(final String user, final String password, final String database, final int packLength) throws SQLException {
        if (!ExportControlled.enabled()) {
            throw new ConnectionFeatureNotAvailableException(this.connection, this.lastPacketSentTimeMs, null);
        }
        if ((this.serverCapabilities & 0x8000) != 0x0) {
            this.clientParam |= 0x8000L;
        }
        this.clientParam |= 0x800L;
        final Buffer packet = new Buffer(packLength);
        if (this.use41Extensions) {
            packet.writeLong(this.clientParam);
            packet.writeLong(this.maxThreeBytes);
            this.appendCharsetByteForHandshake(packet, this.getEncodingForHandshake());
            packet.writeBytesNoNull(new byte[23]);
        }
        else {
            packet.writeInt((int)this.clientParam);
        }
        this.send(packet, packet.getPosition());
        ExportControlled.transformSocketToSSLSocket(this);
    }
    
    public boolean isSSLEstablished() {
        return ExportControlled.enabled() && ExportControlled.isSSLEstablished(this);
    }
    
    public int getServerStatus() {
        return this.serverStatus;
    }
    
    public Buffer sendFechRowViaCursor(final long statementId, final int fetchSize) throws SQLException {
        this.sharedSendPacket.clear();
        this.sharedSendPacket.writeByte((byte)28);
        this.sharedSendPacket.writeLong(statementId);
        this.sharedSendPacket.writeLong(fetchSize);
        return this.sendCommand(28, null, this.sharedSendPacket, true, null, 0);
    }
    
    protected List<ResultSetRow> fetchRowsViaCursor(List<ResultSetRow> fetchedRows, final long statementId, final Field[] columnTypes, final int fetchSize, final boolean useBufferRowExplicit) throws SQLException {
        if (fetchedRows == null) {
            fetchedRows = new ArrayList<ResultSetRow>(fetchSize);
        }
        else {
            fetchedRows.clear();
        }
        this.sharedSendPacket.clear();
        this.sharedSendPacket.writeByte((byte)28);
        this.sharedSendPacket.writeLong(statementId);
        this.sharedSendPacket.writeLong(fetchSize);
        this.sendCommand(28, null, this.sharedSendPacket, true, null, 0);
        ResultSetRow row = null;
        while ((row = this.nextRow(columnTypes, columnTypes.length, true, 1007, false, useBufferRowExplicit, false, null)) != null) {
            fetchedRows.add(row);
        }
        return fetchedRows;
    }
    
    protected long getThreadId() {
        return this.threadId;
    }
    
    protected boolean useNanosForElapsedTime() {
        return this.useNanosForElapsedTime;
    }
    
    protected long getSlowQueryThreshold() {
        return this.slowQueryThreshold;
    }
    
    protected String getQueryTimingUnits() {
        return this.queryTimingUnits;
    }
    
    public int getCommandCount() {
        return this.commandCount;
    }
    
    private void checkTransactionState(final int oldStatus) throws SQLException {
        final boolean previouslyInTrans = (oldStatus & 0x1) != 0x0;
        final boolean currentlyInTrans = this.inTransactionOnServer();
        if (previouslyInTrans && !currentlyInTrans) {
            this.connection.transactionCompleted();
        }
        else if (!previouslyInTrans && currentlyInTrans) {
            this.connection.transactionBegun();
        }
    }
    
    private void preserveOldTransactionState() {
        this.serverStatus |= (this.oldServerStatus & 0x1);
    }
    
    protected void setStatementInterceptors(final List<StatementInterceptorV2> statementInterceptors) {
        this.statementInterceptors = (statementInterceptors.isEmpty() ? null : statementInterceptors);
    }
    
    protected ExceptionInterceptor getExceptionInterceptor() {
        return this.exceptionInterceptor;
    }
    
    protected void setSocketTimeout(final int milliseconds) throws SQLException {
        try {
            this.mysqlConnection.setSoTimeout(milliseconds);
        }
        catch (SocketException e) {
            final SQLException sqlEx = SQLError.createSQLException("Invalid socket timeout value or state", "S1009", this.getExceptionInterceptor());
            sqlEx.initCause(e);
            throw sqlEx;
        }
    }
    
    protected void releaseResources() {
        if (this.deflater != null) {
            this.deflater.end();
            this.deflater = null;
        }
    }
    
    String getEncodingForHandshake() {
        String enc = this.connection.getEncoding();
        if (enc == null) {
            enc = "UTF-8";
        }
        return enc;
    }
    
    private void appendCharsetByteForHandshake(final Buffer packet, final String enc) throws SQLException {
        int charsetIndex = 0;
        if (enc != null) {
            charsetIndex = CharsetMapping.getCollationIndexForJavaEncoding(enc, this.connection);
        }
        if (charsetIndex == 0) {
            charsetIndex = 33;
        }
        if (charsetIndex > 255) {
            throw SQLError.createSQLException("Invalid character set index for encoding: " + enc, "S1009", this.getExceptionInterceptor());
        }
        packet.writeByte((byte)charsetIndex);
    }
    
    public boolean isEOFDeprecated() {
        return (this.clientParam & 0x1000000L) != 0x0L;
    }
    
    public boolean isUseVirtualCompress() {
        return this.useVirtualCompress;
    }
    
    public boolean isVirtualCompressSwitchOn() {
        return this.isVirtualCompressSwitchOn;
    }
    
    public boolean isComrepssWithRawData() {
        return this.comrepssWithRawData;
    }
    
    public boolean isUseCompression() {
        return this.useCompression;
    }
    
    public String getLocalConnectionIdString() {
        return this.localConnectionIdString;
    }
    
    public boolean isEnableExtraOKPacket() {
        return this.enableExtraOkPacket;
    }
    
    public boolean isEnableAbundantFeedback() {
        return this.isEnableExtraOKPacket() && this.enableAbundantFeedback;
    }
    
    public void readAndCheckExtraOKPacket() throws SQLException {
        if (this.isEnableExtraOKPacket()) {
            this.extraOkPacaket.setPosition(0);
            final Buffer extraOKBuffer = this.reuseAndReadPacket(this.extraOkPacaket);
            this.checkOKPacket(extraOKBuffer, 0);
        }
    }
    
    private void checkOKPacket(final Buffer resultPacket, final int offsetFromPktStart) throws SQLException {
        this.isPartitionHit = true;
        this.serverTraceId = "";
        final int oldPos = resultPacket.getPosition();
        final int packetStart = oldPos - offsetFromPktStart;
        final int packetLen = resultPacket.getBufLength() - packetStart;
        resultPacket.setPosition(packetStart);
        final byte packetType = resultPacket.readByte();
        if ((!this.isEOFDeprecated() && 0 != packetType) || (this.isEOFDeprecated() && 254 != packetType)) {
            throw new SQLException("invalid ok packet");
        }
        final long affectedRows = resultPacket.newReadLength();
        final long lastInsertId = resultPacket.newReadLength();
        final int serverStatus = resultPacket.readInt();
        long warns = -1L;
        if (0x0 != (this.serverCapabilities & 0x200)) {
            warns = resultPacket.readInt();
        }
        if (this.inAuthProcess && 0x0 != (serverStatus & 0x4)) {
            this.useOracleMode = true;
        }
        this.fbMgr.reset();
        if (resultPacket.getPosition() - packetStart < packetLen) {
            if (this.enableExtraOkPacket || this.inAuthProcess) {
                resultPacket.fastSkipLenString();
                if ((serverStatus & 0x4000) != 0x0) {
                    final long sessionStateInfoLen = resultPacket.newReadLength();
                    final int endPos = resultPacket.getPosition() + (int)sessionStateInfoLen;
                    while (resultPacket.getPosition() < endPos) {
                        String name = null;
                        byte[] value = null;
                        final Byte type = resultPacket.readByte();
                        if (0 == type) {
                            final long nameValueLen = resultPacket.newReadLength();
                            final int varEndPos = resultPacket.getPosition() + (int)nameValueLen;
                            while (resultPacket.getPosition() < varEndPos) {
                                final long nameLen = resultPacket.newReadLength();
                                name = resultPacket.readString("ASCII", this.getExceptionInterceptor(), (int)nameLen);
                                final long valueLen = resultPacket.newReadLength();
                                value = resultPacket.getBytes((int)valueLen);
                                this.handleTrackedSystemVariables(name, value);
                            }
                        }
                        else if (1 == type) {
                            resultPacket.fastSkipLenString();
                        }
                        else {
                            if (2 != type) {
                                throw new SQLException("invalid track type in ok packet");
                            }
                            resultPacket.fastSkipLenString();
                        }
                    }
                }
            }
            else {
                resultPacket.setPosition(packetLen + packetStart);
            }
        }
        if (resultPacket.getPosition() - packetStart != packetLen) {
            final StringBuilder traceMessageBuf = new StringBuilder();
            traceMessageBuf.append(Messages.getString("invalid ok packet:"));
            traceMessageBuf.append("\n");
            traceMessageBuf.append(getPacketDumpToLog(resultPacket, resultPacket.getBufLength()));
            this.connection.getLog().logWarn(traceMessageBuf.toString());
            final SQLException e = new SQLException(String.format("invalid ok packet, pos=%s, packetStart=%s, packetLen=%s", resultPacket.getPosition(), packetStart, packetLen));
            this.connection.getLog().logWarn(String.format("affectedRows=%s, lastInsertId=%s, serverStatus=%s, tmpWarns=%s", affectedRows, lastInsertId, serverStatus, warns), e);
            throw e;
        }
        resultPacket.setPosition(oldPos);
    }
    
    final String getString(final String encoding, final byte[] data, final int pos, final int expectedLength) throws SQLException {
        if (null == data || pos + expectedLength > data.length) {
            throw SQLError.createSQLException(Messages.getString("ByteArrayBuffer.2"), "S1009", this.exceptionInterceptor);
        }
        try {
            return StringUtils.toString(data, pos, expectedLength, encoding);
        }
        catch (UnsupportedEncodingException uEE) {
            throw SQLError.createSQLException(Messages.getString("ByteArrayBuffer.1") + encoding + "'", "S1009", this.exceptionInterceptor);
        }
    }
    
    void handleTrackedSystemVariables(final String name, final byte[] value) throws SQLException {
        if (StringUtils.isNotBlank(name) && null != value && value.length > 0) {
            if ("ob_capability_flag".equalsIgnoreCase(name)) {
                final String flagStr = this.getString("ASCII", value, 0, value.length);
                this.obCapabilityFlag = Long.valueOf(flagStr);
            }
            else if ("ob_statement_trace_id".equalsIgnoreCase(name)) {
                final String traceIdStr = this.getString("ASCII", value, 0, value.length);
                this.serverTraceId = String.format("%s", traceIdStr);
            }
            else if ("ob_proxy_partition_hit".equalsIgnoreCase(name)) {
                final String partitionHitStr = this.getString("ASCII", value, 0, value.length);
                this.isPartitionHit = Boolean.valueOf(partitionHitStr);
            }
            else if ("ob_client_feedback".equalsIgnoreCase(name)) {
                final ObFeedBackBuffer fbBuffer = new ObFeedBackBuffer(value);
                this.fbMgr.decode(fbBuffer);
            }
        }
    }
    
    public boolean isPartitionHit() {
        return this.isPartitionHit;
    }
    
    public String getServerTraceId() {
        return this.serverTraceId;
    }
    
    public boolean isUseObProto20() {
        return this.useObProto20;
    }
    
    public boolean isProto20PacketWithoutChecksum() {
        return this.proto20PacketWithoutChecksum;
    }
    
    public ObFBElement getFBElement(final ObFeedbackType type) {
        return this.fbMgr.getFBElement(type);
    }
    
    static {
        MysqlIO.maxBufferSize = 65535;
        MysqlIO.jvmPlatformCharset = null;
        EXPLAINABLE_STATEMENT_EXTENSION = new String[] { "INSERT", "UPDATE", "REPLACE", "DELETE" };
        OutputStreamWriter outWriter = null;
        try {
            outWriter = new OutputStreamWriter(new ByteArrayOutputStream());
            MysqlIO.jvmPlatformCharset = outWriter.getEncoding();
        }
        finally {
            try {
                if (outWriter != null) {
                    outWriter.close();
                }
            }
            catch (IOException ex) {}
        }
        MysqlIO.connectionSeq = new AtomicInteger(0);
    }
    
    class BufferResult
    {
        private Buffer[] buffers;
        private int splitPacketLen;
        
        public BufferResult(final Buffer[] buffers, final int splitPacketLen) {
            this.buffers = null;
            this.splitPacketLen = 0;
            this.buffers = buffers;
            this.splitPacketLen = splitPacketLen;
        }
        
        public Buffer[] getBuffers() {
            return this.buffers;
        }
        
        public int getSplitPacketLen() {
            return this.splitPacketLen;
        }
    }
}
