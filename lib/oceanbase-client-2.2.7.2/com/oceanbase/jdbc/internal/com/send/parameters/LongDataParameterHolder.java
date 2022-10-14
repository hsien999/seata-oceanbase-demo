// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.send.parameters;

import java.io.IOException;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.internal.io.output.PacketOutputStream;

public interface LongDataParameterHolder extends ParameterHolder
{
    boolean writePieceData(final PacketOutputStream p0, final boolean p1, final Options p2) throws IOException;
}
