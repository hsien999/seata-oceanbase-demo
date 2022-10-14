// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.extend.datatype;

public class RowObCursorData
{
    private int cursorId;
    private boolean isOpen;
    
    public RowObCursorData(final int cursorId, final boolean isOpen) {
        this.cursorId = cursorId;
        this.isOpen = isOpen;
    }
    
    public int getCursorId() {
        return this.cursorId;
    }
    
    public void setCursorId(final int cursorId) {
        this.cursorId = cursorId;
    }
    
    public boolean isOpen() {
        return this.isOpen;
    }
    
    public void setOpen(final boolean open) {
        this.isOpen = open;
    }
}
