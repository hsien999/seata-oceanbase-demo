// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send;

import com.oceanbase.jdbc.internal.com.read.ErrorPacket;
import com.oceanbase.jdbc.internal.com.read.Buffer;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import java.io.IOException;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class ComStmtPrepare
{
    public static void send(final PacketOutputStream pos, final String sql) throws IOException {
        pos.startPacket(0);
        pos.write(22);
        pos.write(sql);
        pos.flush();
    }
    
    public static ServerPrepareResult read(final PacketInputStream reader, final boolean eofDeprecated, final Protocol protocol, final String sql) throws IOException, SQLException {
        final Buffer buffer = reader.getPacket(true);
        final byte firstByte = buffer.getByteAt(buffer.position);
        if (firstByte == -1) {
            throw buildErrorException(buffer);
        }
        if (firstByte != 0) {
            throw new SQLException("Unexpected packet returned by server, first byte " + firstByte);
        }
        buffer.readByte();
        final int statementId = buffer.readInt();
        final int numColumns = buffer.readShort() & 0xFFFF;
        final int numParams = buffer.readShort() & 0xFFFF;
        final ColumnDefinition[] params = new ColumnDefinition[numParams];
        final ColumnDefinition[] columns = new ColumnDefinition[numColumns];
        if (numParams > 0) {
            for (int i = 0; i < numParams; ++i) {
                params[i] = new ColumnDefinition(reader.getPacket(false), protocol.isOracleMode(), protocol.getOptions().characterEncoding);
            }
            if (numColumns > 0) {
                if (!eofDeprecated) {
                    protocol.skipEofPacket();
                }
                for (int i = 0; i < numColumns; ++i) {
                    columns[i] = new ColumnDefinition(reader.getPacket(false), protocol.isOracleMode(), protocol.getOptions().characterEncoding);
                }
            }
            if (!eofDeprecated) {
                protocol.readEofPacket();
            }
        }
        else if (numColumns > 0) {
            for (int i = 0; i < numColumns; ++i) {
                columns[i] = new ColumnDefinition(reader.getPacket(false), protocol.isOracleMode(), protocol.getOptions().characterEncoding);
            }
            if (!eofDeprecated) {
                protocol.readEofPacket();
            }
        }
        else {
            buffer.readByte();
            protocol.setHasWarnings(buffer.readShort() > 0);
        }
        final ServerPrepareResult serverPrepareResult = new ServerPrepareResult(sql, statementId, columns, params, protocol);
        if (protocol.getOptions().cachePrepStmts && protocol.getOptions().useServerPrepStmts && sql != null && sql.length() < protocol.getOptions().prepStmtCacheSqlLimit) {
            final String key = protocol.getDatabase() + "-" + sql;
            final ServerPrepareResult cachedServerPrepareResult = protocol.addPrepareInCache(key, serverPrepareResult);
            return (cachedServerPrepareResult != null) ? cachedServerPrepareResult : serverPrepareResult;
        }
        return serverPrepareResult;
    }
    
    private static SQLException buildErrorException(final Buffer buffer) {
        final ErrorPacket ep = new ErrorPacket(buffer);
        final String message = ep.getMessage();
        if (1054 == ep.getErrorCode()) {
            return new SQLException(message + "\nIf column exists but type cannot be identified (example 'select ? `field1` from dual'). " + "Use CAST function to solve this problem (example 'select CAST(? as integer) `field1` from dual')", ep.getSqlState(), ep.getErrorCode());
        }
        return new SQLException(message, ep.getSqlState(), ep.getErrorCode());
    }
}
