// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

class WatchableOutputStream extends ByteArrayOutputStream
{
    private OutputStreamWatcher watcher;
    
    @Override
    public void close() throws IOException {
        super.close();
        if (this.watcher != null) {
            this.watcher.streamClosed(this);
        }
    }
    
    public void setWatcher(final OutputStreamWatcher watcher) {
        this.watcher = watcher;
    }
}
