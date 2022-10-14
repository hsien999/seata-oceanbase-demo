// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.jdbc2.optional;

import java.util.Collections;
import java.util.HashMap;
import com.alipay.oceanbase.jdbc.StringUtils;
import com.alipay.oceanbase.jdbc.StructImpl;
import java.sql.Statement;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Struct;
import java.util.ArrayList;
import com.alipay.oceanbase.jdbc.MySQLConnection;
import com.alipay.oceanbase.jdbc.Messages;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import java.sql.SQLException;
import com.alipay.oceanbase.jdbc.Util;
import java.lang.reflect.Constructor;
import com.alipay.oceanbase.jdbc.log.Log;
import java.util.Map;
import com.alipay.oceanbase.jdbc.Connection;
import javax.transaction.xa.XAResource;
import javax.sql.XAConnection;

public class MysqlXAConnection extends MysqlPooledConnection implements XAConnection, XAResource
{
    private static final int MAX_COMMAND_LENGTH = 300;
    private Connection underlyingConnection;
    private static final Map<Integer, Integer> MYSQL_ERROR_CODES_TO_XA_ERROR_CODES;
    private Log log;
    protected boolean logXaCommands;
    private boolean isChangedCommit;
    private static final Constructor<?> JDBC_4_XA_CONNECTION_WRAPPER_CTOR;
    
    protected static MysqlXAConnection getInstance(final Connection mysqlConnection, final boolean logXaCommands) throws SQLException {
        if (!Util.isJdbc4()) {
            return new MysqlXAConnection(mysqlConnection, logXaCommands);
        }
        return (MysqlXAConnection)Util.handleNewInstance(MysqlXAConnection.JDBC_4_XA_CONNECTION_WRAPPER_CTOR, new Object[] { mysqlConnection, logXaCommands }, mysqlConnection.getExceptionInterceptor());
    }
    
    public MysqlXAConnection(final Connection connection, final boolean logXaCommands) throws SQLException {
        super(connection);
        this.underlyingConnection = connection;
        this.log = connection.getLog();
        this.logXaCommands = logXaCommands;
        this.isChangedCommit = false;
    }
    
    @Override
    public XAResource getXAResource() throws SQLException {
        return this;
    }
    
    @Override
    public int getTransactionTimeout() throws XAException {
        return 0;
    }
    
    @Override
    public boolean setTransactionTimeout(final int arg0) throws XAException {
        return false;
    }
    
    @Override
    public boolean isSameRM(final XAResource xares) throws XAException {
        return xares instanceof MysqlXAConnection && this.underlyingConnection.isSameResource(((MysqlXAConnection)xares).underlyingConnection);
    }
    
    @Override
    public Xid[] recover(final int flag) throws XAException {
        return recover(this.underlyingConnection, flag);
    }
    
    protected static Xid[] recover(final java.sql.Connection c, final int flag) throws XAException {
        final boolean startRscan = (flag & 0x1000000) > 0;
        final boolean endRscan = (flag & 0x800000) > 0;
        if (!startRscan && !endRscan && flag != 0) {
            throw new MysqlXAException(-5, Messages.getString("MysqlXAConnection.001"), null);
        }
        if (!startRscan) {
            return new Xid[0];
        }
        boolean isOracleMode = false;
        if (c instanceof MySQLConnection) {
            try {
                if (((MySQLConnection)c).getIO().isOracleMode()) {
                    isOracleMode = true;
                }
            }
            catch (SQLException sqlEx) {
                throw mapXAExceptionFromSQLException(sqlEx);
            }
        }
        final List<MysqlXid> recoveredXidList = new ArrayList<MysqlXid>();
        if (isOracleMode) {
            PreparedStatement psStmt = null;
            ResultSet rs = null;
            ResultSet arrayRes = null;
            final String command = "declare   x DBMS_XA_XID_ARRAY := DBMS_XA_XID_ARRAY(); BEGIN   x := DBMS_XA.XA_RECOVER();   ? := x;END;";
            try {
                final MySQLConnection mySQLConnection = (MySQLConnection)c;
                psStmt = mySQLConnection.prepareStatement(command);
                psStmt.setNull(1, 0);
                rs = psStmt.executeQuery();
                while (rs.next()) {
                    final Array array = rs.getArray(1);
                    arrayRes = array.getResultSet();
                    while (arrayRes.next()) {
                        final Struct struct = (Struct)arrayRes.getObject(2);
                        final Object[] objArr = struct.getAttributes();
                        recoveredXidList.add(new MysqlXid(((String)objArr[1]).getBytes(), ((String)objArr[2]).getBytes(), ((BigDecimal)objArr[0]).intValue()));
                    }
                }
            }
            catch (SQLException sqlEx2) {
                throw mapXAExceptionFromSQLException(sqlEx2);
            }
            finally {
                try {
                    if (arrayRes != null) {
                        arrayRes.close();
                    }
                    if (rs != null) {
                        rs.close();
                    }
                    if (psStmt != null) {
                        psStmt.close();
                    }
                }
                catch (SQLException sqlEx3) {
                    throw mapXAExceptionFromSQLException(sqlEx3);
                }
            }
        }
        else {
            Statement stmt = null;
            ResultSet rs = null;
            try {
                stmt = c.createStatement();
                rs = stmt.executeQuery("XA RECOVER");
                while (rs.next()) {
                    final int formatId = rs.getInt(1);
                    final int gtridLength = rs.getInt(2);
                    final int bqualLength = rs.getInt(3);
                    final byte[] gtridAndBqual = rs.getBytes(4);
                    final byte[] gtrid = new byte[gtridLength];
                    final byte[] bqual = new byte[bqualLength];
                    if (gtridAndBqual.length != gtridLength + bqualLength) {
                        throw new MysqlXAException(105, Messages.getString("MysqlXAConnection.002"), null);
                    }
                    System.arraycopy(gtridAndBqual, 0, gtrid, 0, gtridLength);
                    System.arraycopy(gtridAndBqual, gtridLength, bqual, 0, bqualLength);
                    recoveredXidList.add(new MysqlXid(gtrid, bqual, formatId));
                }
            }
            catch (SQLException sqlEx4) {
                throw mapXAExceptionFromSQLException(sqlEx4);
            }
            finally {
                if (rs != null) {
                    try {
                        rs.close();
                    }
                    catch (SQLException sqlEx5) {
                        throw mapXAExceptionFromSQLException(sqlEx5);
                    }
                }
                if (stmt != null) {
                    try {
                        stmt.close();
                    }
                    catch (SQLException sqlEx5) {
                        throw mapXAExceptionFromSQLException(sqlEx5);
                    }
                }
            }
        }
        final int numXids = recoveredXidList.size();
        final Xid[] asXids = new Xid[numXids];
        final Object[] asObjects = recoveredXidList.toArray();
        for (int i = 0; i < numXids; ++i) {
            asXids[i] = (Xid)asObjects[i];
        }
        return asXids;
    }
    
    @Override
    public int prepare(final Xid xid) throws XAException {
        final StringBuilder commandBuf = new StringBuilder(300);
        if (this.isOracleMode()) {
            commandBuf.append("select DBMS_XA.XA_PREPARE(?) from dual");
            final StructImpl xidObj = this.genOracleXid(xid);
            this.dispatchOracleCommand(commandBuf.toString(), xidObj);
        }
        else {
            commandBuf.append("XA PREPARE ");
            appendXid(commandBuf, xid);
            this.dispatchCommand(commandBuf.toString());
        }
        return 0;
    }
    
    @Override
    public void forget(final Xid xid) throws XAException {
    }
    
    @Override
    public void rollback(final Xid xid) throws XAException {
        final StringBuilder commandBuf = new StringBuilder(300);
        if (this.isOracleMode()) {
            commandBuf.append("select DBMS_XA.XA_ROLLBACK(?) from dual");
            final StructImpl xidObj = this.genOracleXid(xid);
            try {
                this.dispatchOracleCommand(commandBuf.toString(), xidObj);
            }
            finally {
                this.underlyingConnection.setInGlobalTx(false);
            }
        }
        else {
            commandBuf.append("XA ROLLBACK ");
            appendXid(commandBuf, xid);
            try {
                this.dispatchCommand(commandBuf.toString());
            }
            finally {
                this.underlyingConnection.setInGlobalTx(false);
            }
        }
    }
    
    @Override
    public void end(final Xid xid, final int flags) throws XAException {
        final StringBuilder commandBuf = new StringBuilder(300);
        if (this.isOracleMode()) {
            commandBuf.append("select DBMS_XA.XA_END(?, ?) from dual");
            final StructImpl xidObj = this.genOracleXid(xid);
            this.dispatchOracleCommand(commandBuf.toString(), xidObj, flags);
            try {
                final MySQLConnection mySQLConnection = (MySQLConnection)this.underlyingConnection;
                if (this.isChangedCommit) {
                    mySQLConnection.setAutoCommit(true);
                    this.isChangedCommit = false;
                }
            }
            catch (SQLException e) {
                throw new MysqlXAException(-7, Messages.getString("MysqlXAConnection.003"), null);
            }
        }
        else {
            commandBuf.append("XA END ");
            appendXid(commandBuf, xid);
            switch (flags) {
                case 67108864: {
                    break;
                }
                case 33554432: {
                    commandBuf.append(" SUSPEND");
                    break;
                }
                case 536870912: {
                    break;
                }
                default: {
                    throw new XAException(-5);
                }
            }
            this.dispatchCommand(commandBuf.toString());
        }
    }
    
    @Override
    public void start(final Xid xid, final int flags) throws XAException {
        final StringBuilder commandBuf = new StringBuilder(300);
        if (this.isOracleMode()) {
            commandBuf.append("select DBMS_XA.XA_START(?, ?) from dual");
            final StructImpl xidObj = this.genOracleXid(xid);
            try {
                final MySQLConnection mySQLConnection = (MySQLConnection)this.underlyingConnection;
                final boolean isAutoCommit = mySQLConnection.getAutoCommit();
                if (isAutoCommit) {
                    mySQLConnection.setAutoCommit(false);
                    this.isChangedCommit = true;
                }
                else {
                    this.isChangedCommit = false;
                }
            }
            catch (SQLException e2) {
                throw new MysqlXAException(-7, Messages.getString("MysqlXAConnection.003"), null);
            }
            try {
                this.dispatchOracleCommand(commandBuf.toString(), xidObj, flags);
            }
            catch (XAException e) {
                try {
                    final MySQLConnection mySQLConnection2 = (MySQLConnection)this.underlyingConnection;
                    if (this.isChangedCommit) {
                        mySQLConnection2.setAutoCommit(true);
                        this.isChangedCommit = false;
                    }
                }
                catch (SQLException ex) {}
                throw e;
            }
        }
        else {
            commandBuf.append("XA START ");
            appendXid(commandBuf, xid);
            switch (flags) {
                case 2097152: {
                    commandBuf.append(" JOIN");
                    break;
                }
                case 134217728: {
                    commandBuf.append(" RESUME");
                    break;
                }
                case 0: {
                    break;
                }
                default: {
                    throw new XAException(-5);
                }
            }
            this.dispatchCommand(commandBuf.toString());
        }
        this.underlyingConnection.setInGlobalTx(true);
    }
    
    @Override
    public void commit(final Xid xid, final boolean onePhase) throws XAException {
        final StringBuilder commandBuf = new StringBuilder(300);
        if (this.isOracleMode()) {
            commandBuf.append("select DBMS_XA.XA_COMMIT(?, ?) from dual");
            final StructImpl xidObj = this.genOracleXid(xid);
            try {
                this.dispatchOracleCommand(commandBuf.toString(), xidObj, onePhase);
            }
            finally {
                this.underlyingConnection.setInGlobalTx(false);
            }
        }
        else {
            commandBuf.append("XA COMMIT ");
            appendXid(commandBuf, xid);
            if (onePhase) {
                commandBuf.append(" ONE PHASE");
            }
            try {
                this.dispatchCommand(commandBuf.toString());
            }
            finally {
                this.underlyingConnection.setInGlobalTx(false);
            }
        }
    }
    
    private ResultSet dispatchCommand(final String command) throws XAException {
        Statement stmt = null;
        try {
            if (this.logXaCommands) {
                this.log.logDebug("Executing XA statement: " + command);
            }
            stmt = this.underlyingConnection.createStatement();
            stmt.execute(command);
            final ResultSet rs = stmt.getResultSet();
            return rs;
        }
        catch (SQLException sqlEx) {
            throw mapXAExceptionFromSQLException(sqlEx);
        }
        finally {
            if (stmt != null) {
                try {
                    stmt.close();
                }
                catch (SQLException ex) {}
            }
        }
    }
    
    private ResultSet dispatchOracleCommand(final String command, final StructImpl xid) throws XAException {
        return this.dispatchOracleCommand(command, xid, null);
    }
    
    private ResultSet dispatchOracleCommand(final String command, final StructImpl xid, final Object param) throws XAException {
        PreparedStatement psStmt = null;
        try {
            if (this.logXaCommands) {
                this.log.logDebug("Executing XA statement: " + command);
            }
            final MySQLConnection mySQLConnection = (MySQLConnection)this.underlyingConnection;
            psStmt = mySQLConnection.prepareStatement(command);
            psStmt.setObject(1, xid);
            if (param != null) {
                psStmt.setObject(2, param);
            }
            final ResultSet rs = psStmt.executeQuery();
            if (!rs.next()) {
                throw new MysqlXAException(-7, Messages.getString("MysqlXAConnection.003"), null);
            }
            final int xaRet = rs.getInt(1);
            if (0 != xaRet) {
                throw new MysqlXAException(xaRet, Messages.getString("MysqlXAConnection.003"), null);
            }
            return rs;
        }
        catch (SQLException sqlEx) {
            throw mapXAExceptionFromSQLException(sqlEx);
        }
        finally {
            if (psStmt != null) {
                try {
                    psStmt.close();
                }
                catch (SQLException ex) {}
            }
        }
    }
    
    private boolean isOracleMode() throws XAException {
        boolean isOracleMode = false;
        if (this.underlyingConnection instanceof MySQLConnection) {
            try {
                if (((MySQLConnection)this.underlyingConnection).getIO().isOracleMode()) {
                    isOracleMode = true;
                }
            }
            catch (SQLException sqlEx) {
                throw mapXAExceptionFromSQLException(sqlEx);
            }
        }
        return isOracleMode;
    }
    
    private StructImpl genOracleXid(final Xid xid) throws XAException {
        try {
            final MySQLConnection mySQLConnection = (MySQLConnection)this.underlyingConnection;
            final Object[] xidObj = new Object[3];
            int i = 0;
            xidObj[i++] = xid.getFormatId();
            xidObj[i++] = xid.getGlobalTransactionId();
            xidObj[i++] = xid.getBranchQualifier();
            return (StructImpl)mySQLConnection.createStruct("DBMS_XA_XID", xidObj);
        }
        catch (SQLException sqlEx) {
            throw mapXAExceptionFromSQLException(sqlEx);
        }
    }
    
    protected static XAException mapXAExceptionFromSQLException(final SQLException sqlEx) {
        final Integer xaCode = MysqlXAConnection.MYSQL_ERROR_CODES_TO_XA_ERROR_CODES.get(sqlEx.getErrorCode());
        if (xaCode != null) {
            return (XAException)new MysqlXAException(xaCode, sqlEx.getMessage(), null).initCause(sqlEx);
        }
        return (XAException)new MysqlXAException(-7, Messages.getString("MysqlXAConnection.003"), null).initCause(sqlEx);
    }
    
    private static void appendXid(final StringBuilder builder, final Xid xid) {
        final byte[] gtrid = xid.getGlobalTransactionId();
        final byte[] btrid = xid.getBranchQualifier();
        if (gtrid != null) {
            StringUtils.appendAsHex(builder, gtrid);
        }
        builder.append(',');
        if (btrid != null) {
            StringUtils.appendAsHex(builder, btrid);
        }
        builder.append(',');
        StringUtils.appendAsHex(builder, xid.getFormatId());
    }
    
    @Override
    public synchronized java.sql.Connection getConnection() throws SQLException {
        final java.sql.Connection connToWrap = this.getConnection(false, true);
        return connToWrap;
    }
    
    static {
        final HashMap<Integer, Integer> temp = new HashMap<Integer, Integer>();
        temp.put(1397, -4);
        temp.put(1398, -5);
        temp.put(1399, -7);
        temp.put(1400, -9);
        temp.put(1401, -3);
        temp.put(1402, 100);
        temp.put(1440, -8);
        temp.put(1613, 106);
        temp.put(1614, 102);
        MYSQL_ERROR_CODES_TO_XA_ERROR_CODES = Collections.unmodifiableMap((Map<? extends Integer, ? extends Integer>)temp);
        if (Util.isJdbc4()) {
            try {
                JDBC_4_XA_CONNECTION_WRAPPER_CTOR = Class.forName("com.alipay.oceanbase.jdbc.jdbc2.optional.JDBC4MysqlXAConnection").getConstructor(Connection.class, Boolean.TYPE);
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
        JDBC_4_XA_CONNECTION_WRAPPER_CTOR = null;
    }
}
