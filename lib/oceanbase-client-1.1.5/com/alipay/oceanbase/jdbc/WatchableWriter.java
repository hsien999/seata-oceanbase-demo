// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.io.CharArrayWriter;

class WatchableWriter extends CharArrayWriter
{
    private WriterWatcher watcher;
    
    @Override
    public void close() {
        super.close();
        if (this.watcher != null) {
            this.watcher.writerClosed(this);
        }
    }
    
    public void setWatcher(final WriterWatcher watcher) {
        this.watcher = watcher;
    }
}
