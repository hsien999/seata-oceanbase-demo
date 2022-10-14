// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.sql.SQLException;

public class NotImplemented extends SQLException
{
    static final long serialVersionUID = 7768433826547599990L;
    
    public NotImplemented() {
        super(Messages.getString("NotImplemented.0"), "S1C00");
    }
}
