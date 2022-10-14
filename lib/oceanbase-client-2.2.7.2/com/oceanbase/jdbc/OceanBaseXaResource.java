// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.util.List;
import java.math.BigDecimal;
import java.sql.Struct;
import java.util.ArrayList;
import javax.transaction.xa.XAException;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.Utils;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAResource;

public class OceanBaseXaResource implements XAResource
{
    private static final int MAX_COMMAND_LENGTH = 300;
    private final OceanBaseConnection connection;
    private boolean isChangedCommit;
    
    public OceanBaseXaResource(final OceanBaseConnection connection) {
        this.connection = connection;
    }
    
    protected static String xidToString(final Xid xid) {
        return "0x" + Utils.byteArrayToHexString(xid.getGlobalTransactionId()) + ",0x" + Utils.byteArrayToHexString(xid.getBranchQualifier()) + ",0x" + Utils.intToHexString(xid.getFormatId());
    }
    
    private static String flagsToString(final int flags) {
        switch (flags) {
            case 2097152: {
                return "JOIN";
            }
            case 1073741824: {
                return "ONE PHASE";
            }
            case 134217728: {
                return "RESUME";
            }
            case 33554432: {
                return "SUSPEND";
            }
            default: {
                return "";
            }
        }
    }
    
    private XAException mapXaException(final SQLException sqle) {
        int xaErrorCode = 0;
        switch (sqle.getErrorCode()) {
            case 1397: {
                xaErrorCode = -4;
                break;
            }
            case 1398: {
                xaErrorCode = -5;
                break;
            }
            case 1399: {
                xaErrorCode = -7;
                break;
            }
            case 1400: {
                xaErrorCode = -9;
                break;
            }
            case 1401: {
                xaErrorCode = -3;
                break;
            }
            case 1402: {
                xaErrorCode = 100;
                break;
            }
            default: {
                xaErrorCode = 0;
                break;
            }
        }
        XAException xaException;
        if (xaErrorCode != 0) {
            xaException = new XAException(xaErrorCode);
        }
        else {
            xaException = new XAException(sqle.getMessage());
        }
        xaException.initCause(sqle);
        return xaException;
    }
    
    private XAException mapXaException2(final SQLException sqle) {
        final XAException xaException = new XAException(-7);
        xaException.initCause(sqle);
        return xaException;
    }
    
    private void execute(final String command) throws XAException {
        try {
            this.connection.createStatement().execute(command);
        }
        catch (SQLException sqle) {
            throw this.mapXaException(sqle);
        }
    }
    
    @Override
    public void commit(final Xid xid, final boolean onePhase) throws XAException {
        if (this.connection.getProtocol().isOracleMode()) {
            final StringBuilder commandBuf = new StringBuilder(300);
            commandBuf.append("select DBMS_XA.XA_COMMIT(?, ?) from dual");
            final ObStruct xidObj = this.genOracleXid(xid);
            try {
                this.dispatchOracleCommand(commandBuf.toString(), xidObj, onePhase);
            }
            finally {
                this.connection.setInGlobalTx(false);
            }
        }
        else {
            String command = "XA COMMIT " + xidToString(xid);
            if (onePhase) {
                command += " ONE PHASE";
            }
            this.execute(command);
        }
    }
    
    @Override
    public void end(final Xid xid, final int flags) throws XAException {
        if (this.connection.getProtocol().isOracleMode()) {
            final StringBuilder commandBuf = new StringBuilder(300);
            commandBuf.append("select DBMS_XA.XA_END(?, ?) from dual");
            final ObStruct xidObj = this.genOracleXid(xid);
            this.dispatchOracleCommand(commandBuf.toString(), xidObj, flags);
            try {
                final OceanBaseConnection mySQLConnection = this.connection;
                if (this.isChangedCommit) {
                    mySQLConnection.setAutoCommit(true);
                    this.isChangedCommit = false;
                }
            }
            catch (SQLException e) {
                throw this.mapXaException2(e);
            }
        }
        else {
            if (flags != 67108864 && flags != 33554432 && flags != 536870912) {
                throw new XAException(-5);
            }
            this.execute("XA END " + xidToString(xid) + " " + flagsToString(flags));
        }
    }
    
    @Override
    public void forget(final Xid xid) {
    }
    
    @Override
    public int getTransactionTimeout() {
        return 0;
    }
    
    @Override
    public boolean isSameRM(final XAResource xaResource) {
        return false;
    }
    
    @Override
    public int prepare(final Xid xid) throws XAException {
        if (this.connection.getProtocol().isOracleMode()) {
            final StringBuilder commandBuf = new StringBuilder(300);
            commandBuf.append("select DBMS_XA.XA_PREPARE(?) from dual");
            final ObStruct xidObj = this.genOracleXid(xid);
            final int xaRet = this.dispatchOracleCommand(commandBuf.toString(), xidObj);
            return xaRet;
        }
        this.execute("XA PREPARE " + xidToString(xid));
        return 0;
    }
    
    @Override
    public Xid[] recover(final int flags) throws XAException {
        if ((flags & 0x1000000) == 0x0 && (flags & 0x800000) == 0x0 && flags != 0) {
            throw new XAException(-5);
        }
        if ((flags & 0x1000000) == 0x0) {
            return new OceanBaseXid[0];
        }
        if (this.connection.getProtocol().isOracleMode()) {
            final List<OceanBaseXid> recoveredXidList = new ArrayList<OceanBaseXid>();
            PreparedStatement psStmt = null;
            ResultSet rs = null;
            ResultSet arrayRes = null;
            final String command = "declare   x DBMS_XA_XID_ARRAY := DBMS_XA_XID_ARRAY(); BEGIN   x := DBMS_XA.XA_RECOVER();   ? := x;END;";
            try {
                final OceanBaseConnection mySQLConnection = this.connection;
                psStmt = mySQLConnection.prepareStatement(command);
                psStmt.setNull(1, 0);
                rs = psStmt.executeQuery();
                while (rs.next()) {
                    final Array array = rs.getArray(1);
                    arrayRes = array.getResultSet();
                    while (arrayRes.next()) {
                        final Struct struct = (Struct)arrayRes.getObject(2);
                        final Object[] objArr = struct.getAttributes();
                        recoveredXidList.add(new OceanBaseXid(((String)objArr[1]).getBytes(), ((String)objArr[2]).getBytes(), ((BigDecimal)objArr[0]).intValue()));
                    }
                }
            }
            catch (SQLException sqlEx2) {
                throw new XAException(-7);
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
                catch (SQLException sqlEx) {
                    throw this.mapXaException2(sqlEx);
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
        try {
            final ResultSet rs2 = this.connection.createStatement().executeQuery("XA RECOVER");
            final ArrayList<OceanBaseXid> xidList = new ArrayList<OceanBaseXid>();
            while (rs2.next()) {
                final int formatId = rs2.getInt(1);
                final int len1 = rs2.getInt(2);
                final int len2 = rs2.getInt(3);
                final byte[] arr = rs2.getBytes(4);
                final byte[] globalTransactionId = new byte[len1];
                final byte[] branchQualifier = new byte[len2];
                System.arraycopy(arr, 0, globalTransactionId, 0, len1);
                System.arraycopy(arr, len1, branchQualifier, 0, len2);
                xidList.add(new OceanBaseXid(formatId, globalTransactionId, branchQualifier));
            }
            final Xid[] xids = new Xid[xidList.size()];
            xidList.toArray(xids);
            return xids;
        }
        catch (SQLException sqle) {
            throw this.mapXaException(sqle);
        }
    }
    
    @Override
    public void rollback(final Xid xid) throws XAException {
        if (this.connection.getProtocol().isOracleMode()) {
            final StringBuilder commandBuf = new StringBuilder(300);
            commandBuf.append("select DBMS_XA.XA_ROLLBACK(?) from dual");
            final ObStruct xidObj = this.genOracleXid(xid);
            try {
                this.dispatchOracleCommand(commandBuf.toString(), xidObj);
            }
            finally {
                this.connection.setInGlobalTx(false);
            }
        }
        else {
            this.execute("XA ROLLBACK " + xidToString(xid));
        }
    }
    
    @Override
    public boolean setTransactionTimeout(final int timeout) {
        return false;
    }
    
    @Override
    public void start(final Xid xid, final int flags) throws XAException {
        if (flags != 2097152 && flags != 134217728 && flags != 0) {
            throw new XAException(-5);
        }
        if (this.connection.getProtocol().isOracleMode()) {
            final StringBuilder commandBuf = new StringBuilder(300);
            commandBuf.append("select DBMS_XA.XA_START(?, ?) from dual");
            final ObStruct xidObj = this.genOracleXid(xid);
            try {
                final OceanBaseConnection mySQLConnection = this.connection;
                final boolean isAutoCommit = mySQLConnection.getAutoCommit();
                if (isAutoCommit) {
                    mySQLConnection.setAutoCommit(false);
                    this.isChangedCommit = true;
                }
                else {
                    this.isChangedCommit = false;
                }
            }
            catch (SQLException e) {
                throw this.mapXaException2(e);
            }
            try {
                this.dispatchOracleCommand(commandBuf.toString(), xidObj, flags);
            }
            catch (XAException e2) {
                try {
                    final OceanBaseConnection mySQLConnection2 = this.connection;
                    if (this.isChangedCommit) {
                        mySQLConnection2.setAutoCommit(true);
                        this.isChangedCommit = false;
                    }
                }
                catch (SQLException se) {
                    throw this.mapXaException2(se);
                }
                throw e2;
            }
        }
        else {
            this.execute("XA START " + xidToString(xid) + " " + flagsToString((flags == 2097152 && this.connection.getPinGlobalTxToPhysicalConnection()) ? 134217728 : flags));
        }
    }
    
    private ObStruct genOracleXid(final Xid xid) throws XAException {
        try {
            final OceanBaseConnection mySQLConnection = this.connection;
            final Object[] xidObj = new Object[3];
            int i = 0;
            xidObj[i++] = xid.getFormatId();
            xidObj[i++] = xid.getGlobalTransactionId();
            xidObj[i++] = xid.getBranchQualifier();
            return (ObStruct)mySQLConnection.createStruct("DBMS_XA_XID", xidObj);
        }
        catch (SQLException sqlEx) {
            throw this.mapXaException2(sqlEx);
        }
    }
    
    private int dispatchOracleCommand(final String command, final ObStruct xid) throws XAException {
        return this.dispatchOracleCommand(command, xid, null);
    }
    
    private int dispatchOracleCommand(final String command, final ObStruct xid, final Object param) throws XAException {
        PreparedStatement psStmt = null;
        try {
            final OceanBaseConnection mySQLConnection = this.connection;
            psStmt = mySQLConnection.prepareStatement(command);
            psStmt.setObject(1, xid);
            if (param != null) {
                psStmt.setObject(2, param);
            }
            ResultSet rs = null;
            try {
                rs = psStmt.executeQuery();
                if (!rs.next()) {
                    throw new XAException(-7);
                }
                final int xaRet = rs.getInt(1);
                if (xaRet < 0) {
                    final XAException xaException = new XAException(xaRet);
                    throw xaException;
                }
                return xaRet;
            }
            finally {
                if (rs != null) {
                    rs.close();
                }
            }
        }
        catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
            throw this.mapXaException2(sqlEx);
        }
        finally {
            if (psStmt != null) {
                try {
                    psStmt.close();
                }
                catch (SQLException sqlEx2) {
                    throw this.mapXaException2(sqlEx2);
                }
            }
        }
    }
}
