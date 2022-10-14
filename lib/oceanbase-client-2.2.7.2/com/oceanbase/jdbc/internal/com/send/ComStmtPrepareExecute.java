// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send;

import com.oceanbase.jdbc.internal.com.read.Buffer;
import com.oceanbase.jdbc.internal.com.read.resultset.ColumnDefinition;
import com.oceanbase.jdbc.internal.io.input.PacketInputStream;
import java.io.IOException;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.com.send.parameters.OBStructParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.OBArrayParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.OBStringParameter;
import com.oceanbase.jdbc.internal.util.dao.ServerPrepareResult;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.ColumnType;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import com.oceanbase.jdbc.internal.com.read.dao.Results;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class ComStmtPrepareExecute
{
    public static void send(final PacketOutputStream pos, final Results results, final int parameterCount, final ParameterHolder[] parameters, final ColumnType[] parameterTypeHeader, final byte cursorFlag, final Protocol protocol, final ServerPrepareResult serverPrepareResult) throws IOException, SQLException {
        pos.startPacket(0);
        pos.write(-95);
        if (serverPrepareResult == null) {
            pos.writeInt(results.getStatementId());
        }
        else {
            pos.writeInt(serverPrepareResult.getStatementId());
        }
        pos.write(cursorFlag);
        pos.writeInt(protocol.getIterationCount());
        final ParameterHolder lengthEncodedString = new OBStringParameter(results.getStatement().getOriginalSql(), protocol.noBackslashEscapes(), protocol.getOptions().characterEncoding);
        lengthEncodedString.writeBinary(pos);
        pos.writeInt(parameterCount);
        if (parameterCount > 0) {
            final int nullCount = (parameterCount + 7) / 8;
            final byte[] nullBitsBuffer = new byte[nullCount];
            for (int i = 0; i < parameterCount; ++i) {
                if (parameters[i].isNullData()) {
                    final byte[] array = nullBitsBuffer;
                    final int n = i / 8;
                    array[n] |= (byte)(1 << i % 8);
                }
            }
            pos.write(nullBitsBuffer, 0, nullCount);
            int mustSendHeaderType = 0;
            if (parameterTypeHeader == null || parameterTypeHeader[0] == null) {
                mustSendHeaderType = 1;
            }
            else {
                for (int j = 0; j < parameterCount; ++j) {
                    if (!parameterTypeHeader[j].equals(parameters[j].getColumnType())) {
                        mustSendHeaderType = 1;
                        break;
                    }
                }
            }
            pos.write((byte)mustSendHeaderType);
            if (mustSendHeaderType == 1) {
                for (int j = 0; j < parameterCount; ++j) {
                    if (parameterTypeHeader == null) {
                        pos.writeShort(parameters[j].getColumnType().getType());
                    }
                    else {
                        parameterTypeHeader[j] = parameters[j].getColumnType();
                        pos.writeShort(parameterTypeHeader[j].getType());
                    }
                    if (parameters[j].getColumnType().getType() == ColumnType.COMPLEX.getType()) {
                        final Object obj = parameters[j];
                        if (obj instanceof OBArrayParameter) {
                            ((OBArrayParameter)obj).storeArrayTypeInfo(pos);
                        }
                        else {
                            if (!(obj instanceof OBStructParameter)) {
                                throw new SQLException("complex param type is not supported\uff0c only array is supported");
                            }
                            ((OBStructParameter)obj).storeStructTypeInfo(pos);
                        }
                    }
                }
            }
            for (final ParameterHolder holder : parameters) {
                if (!holder.isNullData() && !holder.isLongData()) {
                    holder.writeBinary(pos);
                }
            }
        }
        pos.writeInt(protocol.getExecuteMode());
        pos.writeInt(0);
        pos.writeInt((int)protocol.getChecksum());
        pos.writeInt(0);
        pos.flush();
    }
    
    public static ServerPrepareResult read(final Protocol protocol, final PacketInputStream reader, final ServerPrepareResult serverPrepareResult, final Results results) throws IOException, SQLException {
        final boolean eofDeprecated = protocol.isEofDeprecated();
        Buffer buffer = reader.getPacket(true);
        final byte firstByte = buffer.getByteAt(0);
        switch (firstByte) {
            case 0: {
                buffer.skipByte();
                final int statementId = buffer.readInt();
                results.setStatementId(statementId);
                final int numColumns = buffer.readShort() & 0xFFFF;
                final int numParams = buffer.readIntV1();
                final byte reserved1 = buffer.readByte();
                final short warningCount = buffer.readShort();
                final int extendFlag = buffer.readInt();
                final byte hasResultSet = buffer.readByte();
                final ColumnDefinition[] params = new ColumnDefinition[numParams];
                if (numParams > 0) {
                    for (int i = 0; i < numParams; ++i) {
                        params[i] = new ColumnDefinition(reader.getPacket(false), true, protocol.getOptions().characterEncoding);
                    }
                    if (!eofDeprecated) {
                        protocol.skipEofPacket();
                    }
                }
                final ColumnDefinition[] columns = new ColumnDefinition[numColumns];
                if (hasResultSet == 1) {
                    protocol.readResultSet(columns, results);
                }
                buffer = reader.getPacket(true);
                switch (buffer.getByteAt(0)) {
                    case 0: {
                        protocol.readOkPacket(buffer, results);
                        break;
                    }
                    case -1: {
                        throw protocol.readErrorPacket(buffer, results);
                    }
                }
                return (serverPrepareResult != null) ? serverPrepareResult : new ServerPrepareResult(results.getSql(), statementId, columns, params, protocol);
            }
            case -1: {
                throw protocol.readErrorPacket(buffer, results);
            }
            default: {
                throw new SQLException("Unexpected packet returned by server, first byte " + firstByte);
            }
        }
    }
}
