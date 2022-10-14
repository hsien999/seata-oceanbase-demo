// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.3rd.google.common.io;

import java.io.IOException;

@Deprecated
public interface InputSupplier<T>
{
    T getInput() throws IOException;
}
