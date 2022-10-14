// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.protocol;

import com.oceanbase.jdbc.internal.logging.LoggerFactory;
import java.net.SocketException;
import java.util.Collection;
import java.util.LinkedList;
import com.oceanbase.jdbc.internal.util.constant.HaMode;
import java.time.Instant;
import com.oceanbase.jdbc.OceanBaseConnection;
import java.util.Locale;
import java.util.Map;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.TreeMap;
import java.sql.ResultSet;
import com.oceanbase.jdbc.internal.io.output.StandardPacketOutputStream;
import com.oceanbase.jdbc.internal.io.input.DecompressPacketInputStream;
import com.oceanbase.jdbc.internal.io.input.StandardPacketInputStream;
import com.oceanbase.jdbc.internal.io.output.CompressPacketOutputStream;
import com.oceanbase.jdbc.authentication.AuthenticationPlugin;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import java.nio.charset.Charset;
import com.oceanbase.jdbc.internal.com.read.ErrorPacket;
import com.oceanbase.jdbc.internal.com.send.authentication.OldPasswordPlugin;
import com.oceanbase.jdbc.authentication.AuthenticationPluginLoader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import com.oceanbase.jdbc.internal.com.send.SendHandshakeResponsePacket;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import com.oceanbase.jdbc.tls.TlsSocketPlugin;
import javax.net.ssl.SSLException;
import com.oceanbase.jdbc.tls.TlsSocketPluginLoader;
import com.oceanbase.jdbc.internal.com.send.SendSslConnectionRequestPacket;
import com.oceanbase.jdbc.credential.CredentialPlugin;
import com.oceanbase.jdbc.internal.com.read.ReadInitialHandShakePacket;
import com.oceanbase.jdbc.credential.Credential;
import com.oceanbase.jdbc.internal.com.send.SendClosePacket;
import javax.net.ssl.SSLSocket;
import java.sql.SQLException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import com.oceanbase.jdbc.internal.util.Utils;
import java.io.InputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.TimeZone;
import com.oceanbase.jdbc.HostAddress;
import java.util.List;
import com.oceanbase.jdbc.internal.util.exceptions.ExceptionFactory;
import com.oceanbase.jdbc.internal.util.ServerPrepareStatementCache;
import com.oceanbase.jdbc.internal.failover.FailoverProxy;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;
import java.net.Socket;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.internal.util.pool.GlobalStateInfo;
import com.oceanbase.jdbc.internal.io.LruTraceCache;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.UrlParser;
import java.util.concurrent.locks.ReentrantLock;
import com.oceanbase.jdbc.internal.logging.Logger;

public abstract class AbstractConnectProtocol implements Protocol
{
    private static final String SESSION_QUERY = "SELECT @@max_allowed_packet,@@system_time_zone,@@time_zone,@@auto_increment_increment,@@tx_isolation AS tx_isolation,@@session.tx_read_only AS tx_read_only";
    private static final String SESSION_QUERY_ORACLE = "SELECT @@max_allowed_packet,@@system_time_zone,@@time_zone,@@auto_increment_increment,@@tx_isolation AS tx_isolation,@@session.tx_read_only AS tx_read_only from dual";
    private static final String OB_VERSION_ORACLE_MODE = "select @@version_comment, @@version from dual where rownum <= 1";
    private static final String OB_VERSION_MYSQL_MODE = "select @@version_comment, @@version limit 1";
    private static final String IS_MASTER_QUERY = "select @@innodb_read_only";
    protected static final String CHECK_GALERA_STATE_QUERY = "show status like 'wsrep_local_state'";
    private static final String ALTER_SESSION_TIMEZONE_QUERY = "ALTER SESSION SET TIME_ZONE='";
    private static final Logger logger;
    protected final ReentrantLock lock;
    protected final UrlParser urlParser;
    protected final Options options;
    protected final LruTraceCache traceCache;
    protected String username;
    private final GlobalStateInfo globalInfo;
    public boolean hasWarnings;
    public Results activeStreamingResult;
    public short serverStatus;
    protected int autoIncrementIncrement;
    protected Socket socket;
    protected PacketOutputStream writer;
    protected boolean readOnly;
    protected PacketInputStream reader;
    protected FailoverProxy proxy;
    protected volatile boolean connected;
    protected boolean explicitClosed;
    protected String database;
    protected long serverThreadId;
    protected ServerPrepareStatementCache serverPrepareStatementCache;
    protected boolean eofDeprecated;
    protected long serverCapabilities;
    protected int socketTimeout;
    protected ExceptionFactory exceptionFactory;
    protected final List<String> galeraAllowedStates;
    private HostAddress currentHost;
    private boolean hostFailed;
    private boolean serverMariaDb;
    private String serverVersion;
    private int majorVersion;
    private int minorVersion;
    private int patchVersion;
    private String obServerVersion;
    private boolean supportStmtPrepareExecute;
    private TimeZone timeZone;
    private TimeZone serverTimeZone;
    private boolean isOracleMode;
    private boolean importedTimeZoneTables;
    public boolean autoCommit;
    public int transactionIsolationLevel;
    protected byte[] seed;
    long clientCapabilities;
    String authenticationPluginType;
    byte exchangeCharset;
    String host;
    
    public AbstractConnectProtocol(final UrlParser urlParser, final GlobalStateInfo globalInfo, final ReentrantLock lock, final LruTraceCache traceCache) {
        this.hasWarnings = false;
        this.activeStreamingResult = null;
        this.readOnly = false;
        this.connected = false;
        this.explicitClosed = false;
        this.eofDeprecated = false;
        this.transactionIsolationLevel = 0;
        this.clientCapabilities = 0L;
        urlParser.auroraPipelineQuirks();
        this.lock = lock;
        this.urlParser = urlParser;
        this.options = urlParser.getOptions();
        this.database = ((urlParser.getDatabase() == null) ? "" : urlParser.getDatabase());
        this.username = ((urlParser.getUsername() == null) ? "" : urlParser.getUsername());
        this.globalInfo = globalInfo;
        if (this.options.cachePrepStmts && this.options.useServerPrepStmts) {
            this.serverPrepareStatementCache = ServerPrepareStatementCache.newInstance(this.options.prepStmtCacheSize, this);
        }
        this.galeraAllowedStates = ((urlParser.getOptions().galeraAllowedState == null) ? Collections.emptyList() : Arrays.asList(urlParser.getOptions().galeraAllowedState.split(",")));
        this.traceCache = traceCache;
    }
    
    private static void closeSocket(final PacketInputStream packetInputStream, final PacketOutputStream packetOutputStream, final Socket socket) {
        try {
            try {
                final long maxCurrentMillis = System.currentTimeMillis() + 10L;
                socket.shutdownOutput();
                socket.setSoTimeout(3);
                final InputStream is = socket.getInputStream();
                while (is.read() != -1 && System.currentTimeMillis() < maxCurrentMillis) {}
            }
            catch (Throwable t) {}
            packetOutputStream.close();
            packetInputStream.close();
        }
        catch (IOException ex) {}
        finally {
            try {
                socket.close();
            }
            catch (IOException ex2) {}
        }
    }
    
    private static Socket createSocket(final String host, final int port, final Options options) throws SQLException {
        try {
            final Socket socket = Utils.createSocket(options, host);
            socket.setTcpNoDelay(options.tcpNoDelay);
            if (options.socketTimeout != null) {
                socket.setSoTimeout(options.socketTimeout);
            }
            if (options.tcpKeepAlive) {
                socket.setKeepAlive(true);
            }
            if (options.tcpRcvBuf != null) {
                socket.setReceiveBufferSize(options.tcpRcvBuf);
            }
            if (options.tcpSndBuf != null) {
                socket.setSendBufferSize(options.tcpSndBuf);
            }
            if (options.tcpAbortiveClose) {
                socket.setSoLinger(true, 0);
            }
            if (options.localSocketAddress != null) {
                final InetSocketAddress localAddress = new InetSocketAddress(options.localSocketAddress, 0);
                socket.bind(localAddress);
            }
            if (!socket.isConnected()) {
                InetSocketAddress sockAddr;
                if (options.socksProxyHost != null && options.socksProxyPort > 0) {
                    sockAddr = InetSocketAddress.createUnresolved(host, port);
                }
                else {
                    sockAddr = ((options.pipe == null) ? new InetSocketAddress(host, port) : null);
                }
                socket.connect(sockAddr, options.connectTimeout);
            }
            return socket;
        }
        catch (IOException ioe) {
            throw ExceptionFactory.INSTANCE.create(String.format("Socket fail to connect to host:%s, port:%s. %s", host, port, ioe.getMessage()), "08000", ioe);
        }
    }
    
    private static long initializeClientCapabilities(final Options options, final long serverCapabilities, final String database) {
        long capabilities = 146711296L;
        if (options.allowLocalInfile) {
            capabilities |= 0x80L;
        }
        if (!options.useAffectedRows) {
            capabilities |= 0x2L;
        }
        if (options.allowMultiQueries || options.rewriteBatchedStatements) {
            capabilities |= 0x10000L;
        }
        if ((serverCapabilities & 0x1000000L) != 0x0L) {
            capabilities |= 0x1000000L;
        }
        if (options.useCompression) {
            if ((serverCapabilities & 0x20L) == 0x0L) {
                options.useCompression = false;
            }
            else {
                capabilities |= 0x20L;
            }
        }
        if (options.interactiveClient) {
            capabilities |= 0x400L;
        }
        if (!database.isEmpty() && !options.createDatabaseIfNotExist) {
            capabilities |= 0x8L;
        }
        if (options.supportLobLocator) {
            capabilities |= 0x20000000L;
        }
        return capabilities;
    }
    
    private static void enabledSslProtocolSuites(final SSLSocket sslSocket, final Options options) throws SQLException {
        if (options.enabledSslProtocolSuites != null) {
            final List<String> possibleProtocols = Arrays.asList(sslSocket.getSupportedProtocols());
            final String[] split;
            final String[] protocols = split = options.enabledSslProtocolSuites.split("[,;\\s]+");
            for (final String protocol : split) {
                if (!possibleProtocols.contains(protocol)) {
                    throw new SQLException("Unsupported SSL protocol '" + protocol + "'. Supported protocols : " + possibleProtocols.toString().replace("[", "").replace("]", ""));
                }
            }
            sslSocket.setEnabledProtocols(protocols);
        }
    }
    
    private static void enabledSslCipherSuites(final SSLSocket sslSocket, final Options options) throws SQLException {
        if (options.enabledSslCipherSuites != null) {
            final List<String> possibleCiphers = Arrays.asList(sslSocket.getSupportedCipherSuites());
            final String[] split;
            final String[] ciphers = split = options.enabledSslCipherSuites.split("[,;\\s]+");
            for (final String cipher : split) {
                if (!possibleCiphers.contains(cipher)) {
                    throw new SQLException("Unsupported SSL cipher '" + cipher + "'. Supported ciphers : " + possibleCiphers.toString().replace("[", "").replace("]", ""));
                }
            }
            sslSocket.setEnabledCipherSuites(ciphers);
        }
    }
    
    @Override
    public void close() {
        boolean locked = false;
        if (this.lock != null) {
            locked = this.lock.tryLock();
        }
        try {
            this.connected = false;
            try {
                this.skip();
            }
            catch (Exception ex) {}
            SendClosePacket.send(this.writer);
            closeSocket(this.reader, this.writer, this.socket);
            this.cleanMemory();
        }
        finally {
            if (locked) {
                this.lock.unlock();
            }
        }
    }
    
    @Override
    public void abort() {
        this.explicitClosed = true;
        boolean lockStatus = false;
        if (this.lock != null) {
            lockStatus = this.lock.tryLock();
        }
        try {
            this.connected = false;
            this.abortActiveStream();
            if (!lockStatus) {
                this.forceAbort();
                try {
                    this.socket.setSoTimeout(10);
                    this.socket.setSoLinger(true, 0);
                }
                catch (IOException ex) {}
            }
            else {
                SendClosePacket.send(this.writer);
            }
            closeSocket(this.reader, this.writer, this.socket);
            this.cleanMemory();
        }
        finally {
            if (lockStatus) {
                this.lock.unlock();
            }
        }
    }
    
    private void forceAbort() {
        try (final MasterProtocol copiedProtocol = new MasterProtocol(this.urlParser, new GlobalStateInfo(), new ReentrantLock(), this.traceCache)) {
            copiedProtocol.setHostAddress(this.getHostAddress());
            copiedProtocol.connect();
            copiedProtocol.executeQuery("KILL " + this.serverThreadId);
        }
        catch (SQLException ex) {}
    }
    
    private void abortActiveStream() {
        try {
            if (this.activeStreamingResult != null) {
                this.activeStreamingResult.abort();
                this.activeStreamingResult = null;
            }
        }
        catch (Exception ex) {}
    }
    
    @Override
    public void skip() throws SQLException {
        if (this.activeStreamingResult != null) {
            this.activeStreamingResult.loadFully(true, this);
            this.activeStreamingResult = null;
        }
    }
    
    protected void cleanMemory() {
        if (this.options.cachePrepStmts && this.options.useServerPrepStmts) {
            this.serverPrepareStatementCache.clear();
        }
        if (this.options.enablePacketDebug) {
            this.traceCache.clearMemory();
        }
    }
    
    @Override
    public void setServerStatus(final short serverStatus) {
        this.serverStatus = serverStatus;
    }
    
    @Override
    public void removeHasMoreResults() {
        if (this.hasMoreResults()) {
            this.serverStatus ^= 0x8;
        }
    }
    
    @Override
    public void connect() throws SQLException {
        try {
            this.createConnection(this.currentHost, this.username);
        }
        catch (SQLException exception) {
            throw ExceptionFactory.INSTANCE.create(String.format("Could not connect to %s. %s", this.currentHost, exception.getMessage() + this.getTraces()), "08000", exception);
        }
    }
    
    private void createConnection(final HostAddress hostAddress, final String username) throws SQLException {
        final String host = (hostAddress != null) ? hostAddress.host : null;
        final int port = (hostAddress != null) ? hostAddress.port : 3306;
        final CredentialPlugin credentialPlugin = this.urlParser.getCredentialPlugin();
        Credential credential;
        if (credentialPlugin != null) {
            credential = credentialPlugin.initialize(this.options, username, hostAddress).get();
        }
        else {
            credential = new Credential(username, this.urlParser.getPassword());
        }
        this.assignStream(this.socket = createSocket(host, port, this.options), this.options);
        try {
            final ReadInitialHandShakePacket greetingPacket = new ReadInitialHandShakePacket(this.reader);
            this.serverThreadId = greetingPacket.getServerThreadId();
            this.serverVersion = greetingPacket.getServerVersion();
            this.serverMariaDb = greetingPacket.isServerMariaDb();
            this.serverCapabilities = greetingPacket.getServerCapabilities();
            this.reader.setServerThreadId(this.serverThreadId, null);
            this.writer.setServerThreadId(this.serverThreadId, null);
            this.parseVersion(greetingPacket.getServerVersion());
            final byte exchangeCharset = this.decideLanguage(greetingPacket.getServerLanguage() & 0xFF);
            long clientCapabilities = initializeClientCapabilities(this.options, this.serverCapabilities, this.database);
            this.exceptionFactory = ExceptionFactory.of(this.serverThreadId, this.options);
            this.sslWrapper(host, this.socket, this.options, greetingPacket.getServerCapabilities(), clientCapabilities, exchangeCharset, this.serverThreadId);
            if (Boolean.TRUE.equals(this.options.useSsl)) {
                if ((this.serverCapabilities & 0x800L) == 0x0L) {
                    this.exceptionFactory.create("Trying to connect with ssl, but ssl not enabled in the server", "08000");
                }
                clientCapabilities |= 0x800L;
            }
            String authenticationPluginType = greetingPacket.getAuthenticationPluginType();
            if (credentialPlugin != null && credentialPlugin.defaultAuthenticationPluginType() != null) {
                authenticationPluginType = credentialPlugin.defaultAuthenticationPluginType();
            }
            this.seed = greetingPacket.getSeed();
            this.authenticationPluginType = authenticationPluginType;
            this.host = host;
            this.authenticationHandler(this.exchangeCharset = exchangeCharset, this.clientCapabilities = clientCapabilities, authenticationPluginType, greetingPacket.getSeed(), this.options, this.database, credential, host);
            this.compressionHandler(this.options);
        }
        catch (IOException ioException) {
            this.destroySocket();
            if (host == null) {
                throw ExceptionFactory.INSTANCE.create(String.format("Could not connect to socket : %s", ioException.getMessage()), "08000", ioException);
            }
            throw ExceptionFactory.INSTANCE.create(String.format("Could not connect to %s:%s : %s", host, this.socket.getPort(), ioException.getMessage()), "08000", ioException);
        }
        catch (SQLException sqlException) {
            this.destroySocket();
            throw sqlException;
        }
        this.connected = true;
        this.reader.setServerThreadId(this.serverThreadId, this.isMasterConnection());
        this.writer.setServerThreadId(this.serverThreadId, this.isMasterConnection());
        if (this.options.socketTimeout != null) {
            this.socketTimeout = this.options.socketTimeout;
        }
        if ((this.serverCapabilities & 0x1000000L) != 0x0L) {
            this.eofDeprecated = true;
        }
        this.postConnectionQueries();
        if (this.isMasterConnection() && !this.galeraAllowedStates.isEmpty()) {
            this.galeraStateValidation();
        }
        this.activeStreamingResult = null;
        this.hostFailed = false;
    }
    
    public void destroySocket() {
        if (this.reader != null) {
            try {
                this.reader.close();
            }
            catch (IOException ex) {}
        }
        if (this.writer != null) {
            try {
                this.writer.close();
            }
            catch (IOException ex2) {}
        }
        if (this.socket != null) {
            try {
                this.socket.close();
            }
            catch (IOException ex3) {}
        }
    }
    
    private void sslWrapper(final String host, final Socket socket, final Options options, final long serverCapabilities, long clientCapabilities, final byte exchangeCharset, final long serverThreadId) throws SQLException, IOException {
        if (Boolean.TRUE.equals(options.useSsl)) {
            if ((serverCapabilities & 0x800L) == 0x0L) {
                this.exceptionFactory.create("Trying to connect with ssl, but ssl not enabled in the server", "08000");
            }
            clientCapabilities |= 0x800L;
            SendSslConnectionRequestPacket.send(this.writer, clientCapabilities, exchangeCharset);
            final TlsSocketPlugin socketPlugin = TlsSocketPluginLoader.get(options.tlsSocketType);
            final SSLSocketFactory sslSocketFactory = socketPlugin.getSocketFactory(options);
            final SSLSocket sslSocket = socketPlugin.createSocket(socket, sslSocketFactory);
            enabledSslProtocolSuites(sslSocket, options);
            enabledSslCipherSuites(sslSocket, options);
            sslSocket.setUseClientMode(true);
            sslSocket.startHandshake();
            if (!options.disableSslHostnameVerification && !options.trustServerCertificate) {
                final SSLSession session = sslSocket.getSession();
                try {
                    socketPlugin.verify(host, session, options, serverThreadId);
                }
                catch (SSLException ex) {
                    throw this.exceptionFactory.create("SSL hostname verification failed : " + ex.getMessage() + "\nThis verification can be disabled using the option \"disableSslHostnameVerification\" " + "but won't prevent man-in-the-middle attacks anymore", "08006");
                }
            }
            this.assignStream(sslSocket, options);
        }
    }
    
    private void authenticationHandler(final byte exchangeCharset, final long clientCapabilities, final String authenticationPluginType, byte[] seed, final Options options, final String database, final Credential credential, final String host) throws SQLException, IOException {
        final String clientIp = this.socket.getLocalAddress().getHostAddress();
        SendHandshakeResponsePacket.send(this.writer, credential, host, database, clientCapabilities, this.serverCapabilities, exchangeCharset, (byte)(Boolean.TRUE.equals(options.useSsl) ? 2 : 1), options, authenticationPluginType, seed, clientIp);
        this.writer.permitTrace(false);
        Buffer buffer = this.reader.getPacket(false);
        final AtomicInteger sequence = new AtomicInteger(this.reader.getLastPacketSeq());
        while (true) {
            switch (buffer.getByteAt(0) & 0xFF) {
                case 254: {
                    sequence.set(this.reader.getLastPacketSeq());
                    AuthenticationPlugin authenticationPlugin;
                    if ((this.serverCapabilities & 0x80000L) != 0x0L) {
                        buffer.readByte();
                        String plugin;
                        if (buffer.remaining() > 0) {
                            plugin = buffer.readStringNullEnd(StandardCharsets.US_ASCII);
                            seed = buffer.readRawBytes(buffer.remaining());
                        }
                        else {
                            plugin = "mysql_old_password";
                            seed = Utils.copyWithLength(seed, 8);
                        }
                        authenticationPlugin = AuthenticationPluginLoader.get(plugin);
                    }
                    else {
                        authenticationPlugin = new OldPasswordPlugin();
                        seed = Utils.copyWithLength(seed, 8);
                    }
                    if (authenticationPlugin.mustUseSsl() && options.useSsl == null) {
                        throw this.exceptionFactory.create("Connector use a plugin that require SSL without enabling ssl. For compatibility, this can still be disabled explicitly forcing 'useSsl=false' in connection string.plugin is = " + authenticationPlugin.type(), "08004", 1251);
                    }
                    authenticationPlugin.initialize(credential.getPassword(), seed, options);
                    buffer = authenticationPlugin.process(this.writer, this.reader, sequence);
                    continue;
                }
                case 255: {
                    final ErrorPacket errorPacket = new ErrorPacket(buffer);
                    if (credential.getPassword() != null && !credential.getPassword().isEmpty() && options.passwordCharacterEncoding == null && errorPacket.getErrorCode() == 1045 && "28000".equals(errorPacket.getSqlState())) {
                        throw this.exceptionFactory.create(String.format("%s\nCurrent charset is %s. If password has been set using other charset, consider using option 'passwordCharacterEncoding'", errorPacket.getMessage(), Charset.defaultCharset().displayName()), errorPacket.getSqlState(), errorPacket.getErrorCode());
                    }
                    throw this.exceptionFactory.create(errorPacket.getMessage(), errorPacket.getSqlState(), errorPacket.getErrorCode());
                }
                case 0: {
                    buffer.skipByte();
                    buffer.skipLengthEncodedNumeric();
                    buffer.skipLengthEncodedNumeric();
                    this.serverStatus = buffer.readShort();
                    if ((this.serverStatus & 0x4) != 0x0) {
                        this.setOracleMode(true);
                    }
                    this.writer.permitTrace(true);
                }
                default: {
                    throw this.exceptionFactory.create("unexpected data during authentication (header=" + (buffer.getByteAt(0) & 0xFF), "08000");
                }
            }
        }
    }
    
    private void compressionHandler(final Options options) {
        if (options.useCompression) {
            this.writer = new CompressPacketOutputStream(this.writer.getOutputStream(), options.maxQuerySizeToLog, this.serverThreadId, options.characterEncoding);
            this.reader = new DecompressPacketInputStream(((StandardPacketInputStream)this.reader).getInputStream(), options.maxQuerySizeToLog, this.serverThreadId);
            if (options.enablePacketDebug) {
                this.writer.setTraceCache(this.traceCache);
                this.reader.setTraceCache(this.traceCache);
            }
        }
    }
    
    private void assignStream(final Socket socket, final Options options) throws SQLException {
        try {
            this.writer = new StandardPacketOutputStream(socket.getOutputStream(), options, this.serverThreadId);
            this.reader = new StandardPacketInputStream(socket.getInputStream(), options, this.serverThreadId);
            if (options.enablePacketDebug) {
                this.writer.setTraceCache(this.traceCache);
                this.reader.setTraceCache(this.traceCache);
            }
        }
        catch (IOException ioe) {
            this.destroySocket();
            throw ExceptionFactory.INSTANCE.create("Socket error: " + ioe.getMessage(), "08000", ioe);
        }
    }
    
    private void galeraStateValidation() throws SQLException {
        ResultSet rs;
        try {
            final Results results = new Results();
            this.executeQuery(true, results, "show status like 'wsrep_local_state'");
            results.commandEnd();
            rs = results.getResultSet();
        }
        catch (SQLException sqle) {
            throw ExceptionFactory.of((int)this.serverThreadId, this.options).create("fail to validate Galera state");
        }
        if (rs == null || !rs.next()) {
            throw ExceptionFactory.of((int)this.serverThreadId, this.options).create("fail to validate Galera state");
        }
        if (!this.galeraAllowedStates.contains(rs.getString(2))) {
            throw ExceptionFactory.of((int)this.serverThreadId, this.options).create(String.format("fail to validate Galera state (State is %s)", rs.getString(2)));
        }
    }
    
    protected void postConnectionQueries() throws SQLException {
        try {
            if (this.options.usePipelineAuth && (this.options.socketTimeout == null || this.options.socketTimeout == 0 || this.options.socketTimeout > 500)) {
                this.socket.setSoTimeout(500);
            }
            boolean mustLoadAdditionalInfo = true;
            if (this.globalInfo != null && this.globalInfo.isAutocommit() == this.options.autocommit) {
                mustLoadAdditionalInfo = false;
            }
            if (mustLoadAdditionalInfo) {
                if (!this.options.connectProxy) {
                    final Map<String, String> serverData = new TreeMap<String, String>();
                    if (this.options.usePipelineAuth && !this.options.createDatabaseIfNotExist) {
                        try {
                            this.sendPipelineAdditionalData();
                            this.readPipelineAdditionalData(serverData);
                        }
                        catch (SQLException sqle) {
                            if ("08".equals(sqle.getSQLState())) {
                                throw sqle;
                            }
                            this.additionalData(serverData);
                        }
                    }
                    else {
                        this.additionalData(serverData);
                    }
                    this.writer.setMaxAllowedPacket(Integer.parseInt(serverData.get("max_allowed_packet")));
                    this.autoIncrementIncrement = Integer.parseInt(serverData.get("auto_increment_increment"));
                    String tmp = serverData.get("tx_read_only");
                    if (tmp.equals("OFF") || tmp.equals("ON")) {
                        this.readOnly = !tmp.equals("OFF");
                    }
                    else {
                        this.readOnly = (Integer.parseInt(tmp) != 0);
                    }
                    if (!this.readOnly) {
                        tmp = "0";
                    }
                    else {
                        tmp = "1";
                    }
                    final Map<String, Integer> mapTransIsolationNameToValue = new HashMap<String, Integer>(8);
                    mapTransIsolationNameToValue.put("READ-UNCOMMITED", 1);
                    mapTransIsolationNameToValue.put("READ-UNCOMMITTED", 1);
                    mapTransIsolationNameToValue.put("READ-COMMITTED", 2);
                    mapTransIsolationNameToValue.put("REPEATABLE-READ", 4);
                    mapTransIsolationNameToValue.put("SERIALIZABLE", 8);
                    this.transactionIsolationLevel = mapTransIsolationNameToValue.get(serverData.get("tx_isolation"));
                    this.loadCalendar(serverData.get("time_zone"), serverData.get("system_time_zone"));
                }
            }
            else {
                this.writer.setMaxAllowedPacket((int)this.globalInfo.getMaxAllowedPacket());
                this.autoIncrementIncrement = this.globalInfo.getAutoIncrementIncrement();
                this.loadCalendar(this.globalInfo.getTimeZone(), this.globalInfo.getSystemTimeZone());
            }
            this.reader.setServerThreadId(this.serverThreadId, this.isMasterConnection());
            this.writer.setServerThreadId(this.serverThreadId, this.isMasterConnection());
            this.activeStreamingResult = null;
            this.hostFailed = false;
            if (this.options.usePipelineAuth) {
                if (this.options.socketTimeout != null) {
                    this.socket.setSoTimeout(this.options.socketTimeout);
                }
                else {
                    this.socket.setSoTimeout(0);
                }
            }
            if (!this.options.connectProxy) {
                this.setObServerVersion(this.getObVersion());
            }
        }
        catch (SocketTimeoutException timeoutException) {
            this.destroySocket();
            String msg = "Socket error during post connection queries: " + timeoutException.getMessage();
            if (this.options.usePipelineAuth) {
                msg += "\nServer might not support pipelining, try disabling with option `usePipelineAuth` and `useBatchMultiSend`";
            }
            throw this.exceptionFactory.create(msg, "08000", timeoutException);
        }
        catch (IOException ioException) {
            this.destroySocket();
            throw this.exceptionFactory.create("Socket error during post connection queries: " + ioException.getMessage(), "08000", ioException);
        }
        catch (SQLException sqlException) {
            this.destroySocket();
            throw sqlException;
        }
    }
    
    private void sendPipelineAdditionalData() throws IOException, SQLException {
        this.sendSessionInfos();
        this.getResult(new Results());
        this.sendCharsetVariables();
        this.getResult(new Results());
    }
    
    private void sendSessionInfos() throws IOException {
        final StringBuilder sessionOption = new StringBuilder("autocommit=").append(this.options.autocommit ? "1" : "0");
        this.autoCommit = this.options.autocommit;
        if (this.options.jdbcCompliantTruncation) {
            sessionOption.append(", sql_mode = concat(@@sql_mode,',STRICT_TRANS_TABLES')");
        }
        if (this.options.sessionVariables != null && !this.options.sessionVariables.isEmpty()) {
            sessionOption.append(",").append(Utils.parseSessionVariables(this.options.sessionVariables));
        }
        this.writer.startPacket(0);
        this.writer.write(3);
        this.writer.write("set " + sessionOption.toString());
        this.writer.flush();
    }
    
    private void sendRequestSessionVariables() throws IOException {
        this.writer.startPacket(0);
        this.writer.write(3);
        if (this.isOracleMode) {
            this.writer.write("SELECT @@max_allowed_packet,@@system_time_zone,@@time_zone,@@auto_increment_increment,@@tx_isolation AS tx_isolation,@@session.tx_read_only AS tx_read_only from dual".getBytes());
        }
        else {
            this.writer.write("SELECT @@max_allowed_packet,@@system_time_zone,@@time_zone,@@auto_increment_increment,@@tx_isolation AS tx_isolation,@@session.tx_read_only AS tx_read_only");
        }
        this.writer.flush();
    }
    
    private void sendCharsetVariables() throws IOException {
        this.writer.startPacket(0);
        this.writer.write(3);
        String str;
        if (this.getOptions().characterEncoding.toUpperCase(Locale.ROOT).equals("UTF-8")) {
            str = "utf8";
        }
        else {
            str = this.getOptions().characterEncoding;
        }
        this.writer.write("set names " + str);
        this.writer.flush();
    }
    
    private void sendAlterSessionTimezone(final String zoneId) throws IOException {
        this.writer.startPacket(0);
        this.writer.write(3);
        this.writer.write("ALTER SESSION SET TIME_ZONE='");
        if (!this.importedTimeZoneTables && zoneId.startsWith("GMT")) {
            this.writer.write(zoneId.substring("GMT".length()).getBytes());
        }
        else {
            this.writer.write(zoneId.getBytes());
        }
        this.writer.write(39);
        this.writer.flush();
    }
    
    private void readRequestSessionVariables(final Map<String, String> serverData) throws SQLException {
        final Results results = new Results();
        this.getResult(results);
        results.commandEnd();
        final ResultSet resultSet = results.getResultSet();
        if (resultSet != null) {
            resultSet.next();
            serverData.put("max_allowed_packet", resultSet.getString(1));
            serverData.put("system_time_zone", resultSet.getString(2));
            serverData.put("time_zone", resultSet.getString(3));
            serverData.put("auto_increment_increment", resultSet.getString(4));
            serverData.put("tx_isolation", resultSet.getString(5));
            serverData.put("tx_read_only", resultSet.getString(6));
            return;
        }
        throw this.exceptionFactory.create("Error reading SessionVariables results. Socket is connected ? " + this.socket.isConnected(), "08000");
    }
    
    private void sendCreateDatabaseIfNotExist(final String quotedDb) throws IOException {
        this.writer.startPacket(0);
        this.writer.write(3);
        this.writer.write("CREATE DATABASE IF NOT EXISTS " + quotedDb);
        this.writer.flush();
    }
    
    private void sendUseDatabaseIfNotExist(final String quotedDb) throws IOException {
        this.writer.startPacket(0);
        this.writer.write(3);
        this.writer.write("USE " + quotedDb);
        this.writer.flush();
    }
    
    private void readPipelineAdditionalData(final Map<String, String> serverData) throws SQLException, IOException {
        SQLException resultingException = null;
        boolean canTrySessionWithShow = false;
        if (this.isOracleMode) {
            canTrySessionWithShow = true;
        }
        else {
            try {
                this.sendRequestSessionVariables();
                this.readRequestSessionVariables(serverData);
            }
            catch (SQLException sqlException) {
                if (resultingException == null) {
                    resultingException = this.exceptionFactory.create("could not load system variables", "08000", sqlException);
                    canTrySessionWithShow = true;
                }
            }
        }
        if (canTrySessionWithShow) {
            this.requestSessionDataWithShow(serverData);
            this.connected = true;
            return;
        }
        if (resultingException != null) {
            throw resultingException;
        }
        this.connected = true;
    }
    
    private void requestSessionDataWithShow(final Map<String, String> serverData) throws SQLException {
        try {
            final Results results = new Results();
            this.executeQuery(true, results, "SHOW VARIABLES WHERE Variable_name in ('max_allowed_packet','system_time_zone','tx_read_only','tx_isolation','time_zone','auto_increment_increment')");
            results.commandEnd();
            final ResultSet resultSet = results.getResultSet();
            if (resultSet != null) {
                while (resultSet.next()) {
                    if (AbstractConnectProtocol.logger.isDebugEnabled()) {
                        AbstractConnectProtocol.logger.debug("server data {} = {}", resultSet.getString(1), resultSet.getString(2));
                    }
                    serverData.put(resultSet.getString(1), resultSet.getString(2));
                }
            }
            if (serverData.size() < 4) {
                throw this.exceptionFactory.create("could not load system variables. socket connected: " + this.socket.isConnected(), "08000");
            }
        }
        catch (SQLException sqlException) {
            throw this.exceptionFactory.create("could not load system variables", "08000", sqlException);
        }
    }
    
    private void additionalData(final Map<String, String> serverData) throws IOException, SQLException {
        this.sendSessionInfos();
        this.getResult(new Results());
        try {
            this.sendCharsetVariables();
            this.getResult(new Results());
            if (!this.options.connectProxy) {
                this.sendRequestSessionVariables();
                this.readRequestSessionVariables(serverData);
            }
        }
        catch (SQLException sqlException) {
            this.requestSessionDataWithShow(serverData);
        }
        this.sendPipelineCheckMaster();
        this.readPipelineCheckMaster();
        if (this.options.createDatabaseIfNotExist && !this.database.isEmpty()) {
            final String quotedDb = OceanBaseConnection.quoteIdentifier(this.database);
            this.sendCreateDatabaseIfNotExist(quotedDb);
            this.getResult(new Results());
            this.sendUseDatabaseIfNotExist(quotedDb);
            this.getResult(new Results());
        }
    }
    
    @Override
    public boolean isClosed() {
        return !this.connected;
    }
    
    private boolean getServerTZTablesImported() throws IOException, SQLException {
        try {
            this.writer.startPacket(0);
            this.writer.write(3);
            this.writer.write("select count(*) from V$TIMEZONE_NAMES;");
            this.writer.flush();
            final Results results = new Results();
            this.getResult(results);
            results.commandEnd();
            final ResultSet resultSet = results.getResultSet();
            resultSet.next();
            final int count = resultSet.getInt(1);
            return count != 0;
        }
        catch (SQLException e) {
            return false;
        }
    }
    
    private String getObVersion() throws IOException, SQLException {
        try {
            this.writer.startPacket(0);
            this.writer.write(3);
            if (this.isOracleMode) {
                this.writer.write("select @@version_comment, @@version from dual where rownum <= 1".getBytes());
            }
            else {
                this.writer.write("select @@version_comment, @@version limit 1".getBytes());
            }
            this.writer.flush();
            final Results results = new Results();
            this.getResult(results);
            results.commandEnd();
            final ResultSet resultSet = results.getResultSet();
            resultSet.next();
            final String ret = resultSet.getString(1);
            resultSet.close();
            return ret;
        }
        catch (SQLException e) {
            return "";
        }
    }
    
    private void loadCalendar(final String srvTimeZone, final String srvSystemTimeZone) throws SQLException {
        String tz = null;
        try {
            if (this.isOracleMode) {
                tz = srvTimeZone;
                if (tz == null) {
                    tz = srvTimeZone;
                    if ("SYSTEM".equals(tz)) {
                        tz = srvSystemTimeZone;
                    }
                }
                this.importedTimeZoneTables = this.getServerTZTablesImported();
                if (tz != null && tz.length() >= 2 && (tz.startsWith("+") || tz.startsWith("-")) && Character.isDigit(tz.charAt(1))) {
                    tz = "GMT" + tz;
                }
                final TimeZone defaultTimezone = TimeZone.getDefault();
                final String offsetId = defaultTimezone.toZoneId().getRules().getStandardOffset(Instant.now()).getId();
                if (tz != null) {
                    this.timeZone = Utils.getTimeZone(tz);
                    this.serverTimeZone = this.timeZone;
                    if (defaultTimezone.getRawOffset() != this.timeZone.getRawOffset()) {
                        if (!this.importedTimeZoneTables) {
                            this.sendAlterSessionTimezone(offsetId);
                        }
                        else {
                            this.sendAlterSessionTimezone(defaultTimezone.getID());
                        }
                        this.getResult(new Results());
                        this.timeZone = defaultTimezone;
                    }
                    else {
                        this.timeZone = defaultTimezone;
                    }
                }
                else {
                    if (!this.importedTimeZoneTables) {
                        this.sendAlterSessionTimezone(offsetId);
                    }
                    else {
                        this.sendAlterSessionTimezone(defaultTimezone.getID());
                    }
                    this.getResult(new Results());
                    this.timeZone = defaultTimezone;
                }
            }
            else {
                tz = this.options.serverTimezone;
                if (tz == null) {
                    tz = srvTimeZone;
                    if ("SYSTEM".equals(tz)) {
                        tz = srvSystemTimeZone;
                    }
                }
                if (tz != null && tz.length() >= 2 && (tz.startsWith("+") || tz.startsWith("-")) && Character.isDigit(tz.charAt(1))) {
                    tz = "GMT" + tz;
                }
                this.timeZone = Utils.getTimeZone(tz);
            }
        }
        catch (SQLException e2) {
            if (this.options.serverTimezone != null) {
                throw this.exceptionFactory.create("The server time_zone '" + tz + "' defined in the 'serverTimezone' parameter cannot be parsed " + "by java TimeZone implementation. See java.util.TimeZone#getAvailableIDs() for available TimeZone, depending on your " + "JRE implementation.", "01S00");
            }
            throw this.exceptionFactory.create("The server time_zone '" + tz + "' cannot be parsed. The server time zone must defined in the " + "jdbc url string with the 'serverTimezone' parameter (or server time zone must be defined explicitly with " + "sessionVariables=time_zone='Canada/Atlantic' for example).  See " + "java.util.TimeZone#getAvailableIDs() for available TimeZone, depending on your JRE implementation.", "01S00");
        }
        catch (IOException e) {
            throw new SQLException("Alter session set time_zone exception", e);
        }
    }
    
    @Override
    public boolean checkIfMaster() throws SQLException {
        return this.isMasterConnection();
    }
    
    private byte decideLanguage(final int serverLanguage) {
        if (serverLanguage == 45 || serverLanguage == 46 || (serverLanguage >= 224 && serverLanguage <= 247)) {
            return (byte)serverLanguage;
        }
        if (this.getMajorServerVersion() == 5 && this.getMinorServerVersion() <= 1) {
            return 33;
        }
        if (serverLanguage == 33) {
            return 45;
        }
        if (serverLanguage == 83) {
            return 46;
        }
        if (serverLanguage >= 192 && serverLanguage <= 215) {
            return (byte)(serverLanguage + 32);
        }
        return -32;
    }
    
    @Override
    public void readEofPacket() throws SQLException, IOException {
        final Buffer buffer = this.reader.getPacket(true);
        switch (buffer.getByteAt(0)) {
            case -2: {
                buffer.skipByte();
                this.hasWarnings = (buffer.readShort() > 0);
                this.serverStatus = buffer.readShort();
            }
            case -1: {
                final ErrorPacket ep = new ErrorPacket(buffer);
                throw this.exceptionFactory.create("Could not connect: " + ep.getMessage(), ep.getSqlState(), ep.getErrorCode());
            }
            default: {
                throw this.exceptionFactory.create("Unexpected packet type " + buffer.getByteAt(0) + " instead of EOF", "08000");
            }
        }
    }
    
    @Override
    public void skipEofPacket() throws SQLException, IOException {
        final Buffer buffer = this.reader.getPacket(true);
        switch (buffer.getByteAt(0)) {
            case -2: {}
            case -1: {
                final ErrorPacket ep = new ErrorPacket(buffer);
                throw this.exceptionFactory.create("Could not connect: " + ep.getMessage(), ep.getSqlState(), ep.getErrorCode());
            }
            default: {
                throw this.exceptionFactory.create("Unexpected packet type " + buffer.getByteAt(0) + " instead of EOF");
            }
        }
    }
    
    @Override
    public void setHostFailedWithoutProxy() {
        this.hostFailed = true;
        this.close();
    }
    
    @Override
    public UrlParser getUrlParser() {
        return this.urlParser;
    }
    
    @Override
    public boolean isMasterConnection() {
        return this.currentHost == null || "master".equals(this.currentHost.type);
    }
    
    @Deprecated
    private void sendPipelineCheckMaster() throws IOException {
        if (this.urlParser.getHaMode() == HaMode.AURORA) {
            this.writer.startPacket(0);
            this.writer.write(3);
            this.writer.write("select @@innodb_read_only");
            this.writer.flush();
        }
    }
    
    @Deprecated
    public void readPipelineCheckMaster() throws SQLException {
    }
    
    @Override
    public boolean mustBeMasterConnection() {
        return true;
    }
    
    @Override
    public boolean noBackslashEscapes() {
        return (this.serverStatus & 0x200) != 0x0;
    }
    
    @Override
    public void connectWithoutProxy() throws SQLException {
        if (!this.isClosed()) {
            this.close();
        }
        final List<HostAddress> hostAddresses = this.urlParser.getHostAddresses();
        final LinkedList<HostAddress> hosts = new LinkedList<HostAddress>(hostAddresses);
        if (this.urlParser.getHaMode().equals(HaMode.LOADBALANCE)) {
            Collections.shuffle(hosts);
        }
        if (hosts.isEmpty()) {
            if (this.options.pipe != null) {
                try {
                    this.createConnection(null, this.username);
                    return;
                }
                catch (SQLException exception) {
                    throw ExceptionFactory.INSTANCE.create(String.format("Could not connect to named pipe '%s' : %s%s", this.options.pipe, exception.getMessage(), this.getTraces()), "08000", exception);
                }
            }
            throw ExceptionFactory.INSTANCE.create("No host is defined and pipe option is not set. Check if connection string respect format (jdbc:(mysql|mariadb):[replication:|loadbalance:|sequential:|aurora:]//<hostDescription>[,<hostDescription>...]/[database][?<key1>=<value1>[&<key2>=<value2>]])", "08000");
        }
        while (!hosts.isEmpty()) {
            this.currentHost = hosts.poll();
            try {
                this.createConnection(this.currentHost, this.username);
                return;
            }
            catch (SQLException e) {
                if (!hosts.isEmpty()) {
                    continue;
                }
                if (e.getSQLState() != null) {
                    throw ExceptionFactory.INSTANCE.create(String.format("Could not connect to %s : %s%s", HostAddress.toString(hostAddresses), e.getMessage(), this.getTraces()), e.getSQLState(), e.getErrorCode(), e);
                }
                throw ExceptionFactory.INSTANCE.create(String.format("Could not connect to %s. %s%s", this.currentHost, e.getMessage(), this.getTraces()), "08000", e);
            }
            break;
        }
    }
    
    @Override
    public boolean shouldReconnectWithoutProxy() {
        return (this.serverStatus & 0x1) == 0x0 && this.hostFailed && this.urlParser.getOptions().autoReconnect;
    }
    
    @Override
    public String getServerVersion() {
        return this.serverVersion;
    }
    
    @Override
    public void setObServerVersion(final String version) {
        this.obServerVersion = version;
        if (this.isOracleMode && this.options.useServerPrepStmts && this.options.useOraclePrepareExecute && this.obServerVersion.compareToIgnoreCase("2.2.76") >= 0) {
            this.supportStmtPrepareExecute = true;
        }
        else {
            this.supportStmtPrepareExecute = false;
        }
    }
    
    @Override
    public String getObServerVersion() {
        return this.obServerVersion;
    }
    
    @Override
    public boolean supportStmtPrepareExecute() {
        return this.supportStmtPrepareExecute;
    }
    
    @Override
    public boolean getReadonly() {
        return this.readOnly;
    }
    
    @Override
    public HostAddress getHostAddress() {
        return this.currentHost;
    }
    
    @Override
    public void setHostAddress(final HostAddress host) {
        this.currentHost = host;
        this.readOnly = "slave".equals(this.currentHost.type);
    }
    
    @Override
    public String getHost() {
        return (this.currentHost == null) ? null : this.currentHost.host;
    }
    
    @Override
    public FailoverProxy getProxy() {
        return this.proxy;
    }
    
    @Override
    public void setProxy(final FailoverProxy proxy) {
        this.proxy = proxy;
    }
    
    @Override
    public int getPort() {
        return (this.currentHost == null) ? 3306 : this.currentHost.port;
    }
    
    @Override
    public String getDatabase() {
        return this.database;
    }
    
    @Override
    public String getUsername() {
        return this.username;
    }
    
    @Override
    public void parseVersion(final String serverVersion) {
        final int length = serverVersion.length();
        int offset = 0;
        int type = 0;
        int val = 0;
        while (offset < length) {
            final char car = serverVersion.charAt(offset);
            if (car < '0' || car > '9') {
                switch (type) {
                    case 0: {
                        this.majorVersion = val;
                        break;
                    }
                    case 1: {
                        this.minorVersion = val;
                        break;
                    }
                    case 2: {
                        this.patchVersion = val;
                        return;
                    }
                }
                ++type;
                val = 0;
            }
            else {
                val = val * 10 + car - 48;
            }
            ++offset;
        }
        if (type == 2) {
            this.patchVersion = val;
        }
    }
    
    @Override
    public int getMajorServerVersion() {
        return this.majorVersion;
    }
    
    @Override
    public int getMinorServerVersion() {
        return this.minorVersion;
    }
    
    @Override
    public boolean versionGreaterOrEqual(final int major, final int minor, final int patch) {
        return this.majorVersion > major || (this.majorVersion >= major && (this.minorVersion > minor || (this.minorVersion >= minor && this.patchVersion >= patch)));
    }
    
    @Override
    public boolean getPinGlobalTxToPhysicalConnection() {
        return this.options.pinGlobalTxToPhysicalConnection;
    }
    
    @Override
    public boolean hasWarnings() {
        this.lock.lock();
        try {
            return this.hasWarnings;
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public boolean isConnected() {
        this.lock.lock();
        try {
            return this.connected;
        }
        finally {
            this.lock.unlock();
        }
    }
    
    @Override
    public long getServerThreadId() {
        return this.serverThreadId;
    }
    
    @Override
    public Socket getSocket() {
        return this.socket;
    }
    
    @Override
    public boolean isExplicitClosed() {
        return this.explicitClosed;
    }
    
    @Override
    public TimeZone getTimeZone() {
        return this.timeZone;
    }
    
    public TimeZone getServerTimeZone() {
        return this.serverTimeZone;
    }
    
    @Override
    public Options getOptions() {
        return this.options;
    }
    
    @Override
    public void setHasWarnings(final boolean hasWarnings) {
        this.hasWarnings = hasWarnings;
    }
    
    @Override
    public Results getActiveStreamingResult() {
        return this.activeStreamingResult;
    }
    
    @Override
    public void setActiveStreamingResult(final Results activeStreamingResult) {
        this.activeStreamingResult = activeStreamingResult;
    }
    
    @Override
    public void removeActiveStreamingResult() {
        if (this.activeStreamingResult != null) {
            this.activeStreamingResult.removeFetchSize();
            this.activeStreamingResult = null;
        }
    }
    
    @Override
    public ReentrantLock getLock() {
        return this.lock;
    }
    
    @Override
    public boolean hasMoreResults() {
        return (this.serverStatus & 0x8) != 0x0;
    }
    
    @Override
    public ServerPrepareStatementCache prepareStatementCache() {
        return this.serverPrepareStatementCache;
    }
    
    @Override
    public abstract void executeQuery(final String p0) throws SQLException;
    
    @Override
    public void changeSocketTcpNoDelay(final boolean setTcpNoDelay) {
        try {
            this.socket.setTcpNoDelay(setTcpNoDelay);
        }
        catch (SocketException ex) {}
    }
    
    @Override
    public void changeSocketSoTimeout(final int setSoTimeout) throws SocketException {
        this.socketTimeout = setSoTimeout;
        this.socket.setSoTimeout(this.socketTimeout);
    }
    
    @Override
    public boolean isServerMariaDb() {
        return this.serverMariaDb;
    }
    
    @Override
    public PacketInputStream getReader() {
        return this.reader;
    }
    
    @Override
    public boolean isEofDeprecated() {
        return this.eofDeprecated;
    }
    
    @Override
    public boolean sessionStateAware() {
        return (this.serverCapabilities & 0x800000L) != 0x0L;
    }
    
    @Override
    public String getTraces() {
        if (this.options.enablePacketDebug) {
            return this.traceCache.printStack();
        }
        return "";
    }
    
    @Override
    public boolean isOracleMode() {
        return this.isOracleMode;
    }
    
    public void setOracleMode(final boolean oracleMode) {
        this.isOracleMode = oracleMode;
    }
    
    @Override
    public boolean isTZTablesImported() {
        return this.importedTimeZoneTables;
    }
    
    @Override
    public String getEncoding() {
        return this.options.characterEncoding;
    }
    
    static {
        logger = LoggerFactory.getLogger(AbstractConnectProtocol.class);
    }
}
