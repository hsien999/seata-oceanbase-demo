// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send;

import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import com.oceanbase.jdbc.internal.com.send.parameters.ParameterHolder;
import com.oceanbase.jdbc.internal.util.dao.ClientPrepareResult;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public class ComQuery
{
    public static void sendSubCmd(final PacketOutputStream out, final ClientPrepareResult clientPrepareResult, final ParameterHolder[] parameters, final int queryTimeout) throws IOException {
        out.startPacket(0);
        out.write(3);
        if (queryTimeout > 0) {
            out.write(("SET STATEMENT max_statement_time=" + queryTimeout + " FOR ").getBytes(out.getCharset()));
        }
        if (clientPrepareResult.isRewriteType()) {
            out.write(clientPrepareResult.getQueryParts().get(0));
            out.write(clientPrepareResult.getQueryParts().get(1));
            for (int i = 0; i < clientPrepareResult.getParamCount(); ++i) {
                parameters[i].writeTo(out);
                out.write(clientPrepareResult.getQueryParts().get(i + 2));
            }
            out.write(clientPrepareResult.getQueryParts().get(clientPrepareResult.getParamCount() + 2));
        }
        else {
            out.write(clientPrepareResult.getQueryParts().get(0));
            for (int i = 0; i < clientPrepareResult.getParamCount(); ++i) {
                parameters[i].writeTo(out);
                out.write(clientPrepareResult.getQueryParts().get(i + 1));
            }
        }
        out.flush();
    }
    
    public static int sendRewriteCmd(final PacketOutputStream pos, final List<byte[]> queryParts, final int currentIndex, final int paramCount, final List<ParameterHolder[]> parameterList, final boolean rewriteValues) throws IOException {
        pos.startPacket(0);
        pos.write(3);
        int index = currentIndex;
        ParameterHolder[] parameters = parameterList.get(index++);
        final byte[] firstPart = queryParts.get(0);
        final byte[] secondPart = queryParts.get(1);
        if (!rewriteValues) {
            pos.write(firstPart, 0, firstPart.length);
            pos.write(secondPart, 0, secondPart.length);
            int staticLength = 1;
            for (final byte[] queryPart : queryParts) {
                staticLength += queryPart.length;
            }
            for (int i = 0; i < paramCount; ++i) {
                parameters[i].writeTo(pos);
                pos.write(queryParts.get(i + 2));
            }
            pos.write(queryParts.get(paramCount + 2));
            while (index < parameterList.size()) {
                parameters = parameterList.get(index);
                int parameterLength = 0;
                boolean knownParameterSize = true;
                for (final ParameterHolder parameter : parameters) {
                    final int paramSize = parameter.getApproximateTextProtocolLength();
                    if (paramSize == -1) {
                        knownParameterSize = false;
                        break;
                    }
                    parameterLength += paramSize;
                }
                if (!knownParameterSize) {
                    pos.write(59);
                    pos.write(firstPart, 0, firstPart.length);
                    pos.write(secondPart, 0, secondPart.length);
                    for (int j = 0; j < paramCount; ++j) {
                        parameters[j].writeTo(pos);
                        pos.write(queryParts.get(j + 2));
                    }
                    pos.write(queryParts.get(paramCount + 2));
                    ++index;
                    break;
                }
                if (!pos.checkRemainingSize(staticLength + parameterLength)) {
                    break;
                }
                pos.write(59);
                pos.write(firstPart, 0, firstPart.length);
                pos.write(secondPart, 0, secondPart.length);
                for (int j = 0; j < paramCount; ++j) {
                    parameters[j].writeTo(pos);
                    pos.write(queryParts.get(j + 2));
                }
                pos.write(queryParts.get(paramCount + 2));
                ++index;
            }
        }
        else {
            pos.write(firstPart, 0, firstPart.length);
            pos.write(secondPart, 0, secondPart.length);
            final int lastPartLength = queryParts.get(paramCount + 2).length;
            int intermediatePartLength = queryParts.get(1).length;
            for (int k = 0; k < paramCount; ++k) {
                parameters[k].writeTo(pos);
                pos.write(queryParts.get(k + 2));
                intermediatePartLength += queryParts.get(k + 2).length;
            }
            while (index < parameterList.size()) {
                parameters = parameterList.get(index);
                int parameterLength2 = 0;
                boolean knownParameterSize2 = true;
                for (final ParameterHolder parameter2 : parameters) {
                    final int paramSize2 = parameter2.getApproximateTextProtocolLength();
                    if (paramSize2 == -1) {
                        knownParameterSize2 = false;
                        break;
                    }
                    parameterLength2 += paramSize2;
                }
                if (!knownParameterSize2) {
                    pos.write(44);
                    pos.write(secondPart, 0, secondPart.length);
                    for (int l = 0; l < paramCount; ++l) {
                        parameters[l].writeTo(pos);
                        pos.write(queryParts.get(l + 2));
                    }
                    ++index;
                    break;
                }
                if (!pos.checkRemainingSize(1 + parameterLength2 + intermediatePartLength + lastPartLength)) {
                    break;
                }
                pos.write(44);
                pos.write(secondPart, 0, secondPart.length);
                for (int l = 0; l < paramCount; ++l) {
                    parameters[l].writeTo(pos);
                    final byte[] addPart = queryParts.get(l + 2);
                    pos.write(addPart, 0, addPart.length);
                }
                ++index;
            }
            pos.write(queryParts.get(paramCount + 2));
        }
        pos.flush();
        return index;
    }
    
    public static int sendBatchAggregateSemiColon(final PacketOutputStream writer, final String firstQuery, final List<String> queries, final int currentIndex) throws IOException {
        writer.startPacket(0);
        writer.write(3);
        writer.write(firstQuery.getBytes(writer.getCharset()));
        int index;
        for (index = currentIndex; index < queries.size(); ++index) {
            final byte[] sqlByte = queries.get(index).getBytes(writer.getCharset());
            if (!writer.checkRemainingSize(sqlByte.length + 1)) {
                break;
            }
            writer.write(59);
            writer.write(sqlByte);
        }
        writer.flush();
        return index;
    }
    
    public static void sendDirect(final PacketOutputStream pos, final byte[] sqlBytes) throws IOException {
        pos.startPacket(0);
        pos.write(3);
        pos.write(sqlBytes);
        pos.flush();
    }
    
    public static void sendDirect(final PacketOutputStream pos, final byte[] sqlBytes, final int queryTimeout) throws IOException {
        pos.startPacket(0);
        pos.write(3);
        if (queryTimeout > 0) {
            pos.write(("SET STATEMENT max_statement_time=" + queryTimeout + " FOR ").getBytes(pos.getCharset()));
        }
        pos.write(sqlBytes);
        pos.flush();
    }
    
    public static void sendMultiDirect(final PacketOutputStream pos, final List<byte[]> sqlBytes) throws IOException {
        pos.startPacket(0);
        pos.write(3);
        for (final byte[] bytes : sqlBytes) {
            pos.write(bytes);
        }
        pos.flush();
    }
    
    public static void sendMultiDirect(final PacketOutputStream pos, final List<byte[]> sqlBytes, final int queryTimeout) throws IOException {
        pos.startPacket(0);
        pos.write(3);
        pos.write(("SET STATEMENT max_statement_time=" + queryTimeout + " FOR ").getBytes(pos.getCharset()));
        for (final byte[] bytes : sqlBytes) {
            pos.write(bytes);
        }
        pos.flush();
    }
}
