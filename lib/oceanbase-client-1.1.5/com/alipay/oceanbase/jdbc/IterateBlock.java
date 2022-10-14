// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;
import java.util.Iterator;

public abstract class IterateBlock<T>
{
    DatabaseMetaData.IteratorWithCleanup<T> iteratorWithCleanup;
    Iterator<T> javaIterator;
    boolean stopIterating;
    
    IterateBlock(final DatabaseMetaData.IteratorWithCleanup<T> i) {
        this.stopIterating = false;
        this.iteratorWithCleanup = i;
        this.javaIterator = null;
    }
    
    IterateBlock(final Iterator<T> i) {
        this.stopIterating = false;
        this.javaIterator = i;
        this.iteratorWithCleanup = null;
    }
    
    public void doForAll() throws SQLException {
        if (this.iteratorWithCleanup != null) {
            try {
                while (this.iteratorWithCleanup.hasNext()) {
                    this.forEach(this.iteratorWithCleanup.next());
                    if (this.stopIterating) {
                        break;
                    }
                }
            }
            finally {
                this.iteratorWithCleanup.close();
            }
        }
        else {
            while (this.javaIterator.hasNext()) {
                this.forEach(this.javaIterator.next());
                if (this.stopIterating) {
                    break;
                }
            }
        }
    }
    
    abstract void forEach(final T p0) throws SQLException;
    
    public final boolean fullIteration() {
        return !this.stopIterating;
    }
}
