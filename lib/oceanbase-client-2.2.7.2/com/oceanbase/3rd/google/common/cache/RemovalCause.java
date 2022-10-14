// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.3rd.google.common.cache;

import com.oceanbase.3rd.google.common.annotations.GwtCompatible;
import com.oceanbase.3rd.google.common.annotations.Beta;

@Beta
@GwtCompatible
public enum RemovalCause
{
    EXPLICIT {
        @Override
        boolean wasEvicted() {
            return false;
        }
    }, 
    REPLACED {
        @Override
        boolean wasEvicted() {
            return false;
        }
    }, 
    COLLECTED {
        @Override
        boolean wasEvicted() {
            return true;
        }
    }, 
    EXPIRED {
        @Override
        boolean wasEvicted() {
            return true;
        }
    }, 
    SIZE {
        @Override
        boolean wasEvicted() {
            return true;
        }
    };
    
    abstract boolean wasEvicted();
}
