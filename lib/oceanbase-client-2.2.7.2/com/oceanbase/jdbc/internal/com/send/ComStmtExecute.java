// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send;

import java.io.IOException;
import java.sql.SQLException;
import com.oceanbase.jdbc.internal.com.send.parameters.OBStructParameter;
import com.oceanbase.jdbc.internal.com.send.parameters.OBArrayParameter;
import com.oceanbase.jdbc.internal.protocol.Protocol;
import com.oceanbase.jdbc.internal.ColumnType;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class ComStmtExecute
{
    public static void send(final PacketOutputStream pos, final int statementId, final ParameterHolder[] parameters, final int parameterCount, final ColumnType[] parameterTypeHeader, final byte cursorFlag, final Protocol protocol) throws IOException, SQLException {
        pos.startPacket(0);
        pos.write(23);
        pos.writeInt(statementId);
        pos.write(cursorFlag);
        if (protocol.versionGreaterOrEqual(4, 1, 2)) {
            if (protocol.isOracleMode() && protocol.getOptions().useServerPsStmtChecksum) {
                pos.writeInt((int)protocol.getChecksum());
            }
            else {
                pos.writeInt(1);
            }
        }
        else {
            pos.writeInt(1);
        }
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
            boolean mustSendHeaderType = false;
            if (parameterTypeHeader != null && parameterTypeHeader[0] == null) {
                mustSendHeaderType = true;
            }
            else {
                for (int j = 0; j < parameterCount; ++j) {
                    if (!parameterTypeHeader[j].equals(parameters[j].getColumnType())) {
                        mustSendHeaderType = true;
                        break;
                    }
                }
            }
            if (mustSendHeaderType) {
                pos.write(1);
                for (int j = 0; j < parameterCount; ++j) {
                    parameterTypeHeader[j] = parameters[j].getColumnType();
                    pos.writeShort(parameterTypeHeader[j].getType());
                    if (parameterTypeHeader[j].getType() == ColumnType.COMPLEX.getType()) {
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
            else {
                pos.write(0);
            }
        }
        for (final ParameterHolder holder : parameters) {
            if (!holder.isNullData() && !holder.isLongData()) {
                holder.writeBinary(pos);
            }
        }
        pos.flush();
    }
}
