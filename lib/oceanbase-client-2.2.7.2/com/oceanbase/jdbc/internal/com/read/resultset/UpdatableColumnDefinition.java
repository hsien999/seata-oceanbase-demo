// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.com.read.resultset;

public class UpdatableColumnDefinition extends ColumnDefinition
{
    private final boolean canBeNull;
    private final boolean primary;
    private final boolean hasDefault;
    private final boolean generated;
    private final boolean autoIncrement;
    
    public UpdatableColumnDefinition(final ColumnDefinition columnDefinition, final boolean canBeNull, final boolean hasDefault, final boolean generated, final boolean primary, final boolean autoIncrement) {
        super(columnDefinition);
        this.canBeNull = canBeNull;
        this.hasDefault = hasDefault;
        this.generated = generated;
        this.primary = primary;
        this.autoIncrement = autoIncrement;
    }
    
    public boolean canBeNull() {
        return this.canBeNull;
    }
    
    public boolean hasDefault() {
        return this.hasDefault;
    }
    
    public boolean isGenerated() {
        return this.generated;
    }
    
    public boolean isPrimary() {
        return this.primary;
    }
    
    public boolean isAutoIncrement() {
        return this.autoIncrement;
    }
}
