// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.credential;

public class Credential
{
    private String user;
    private String password;
    
    public Credential(final String user, final String password) {
        this.user = user;
        this.password = password;
    }
    
    public String getUser() {
        return this.user;
    }
    
    public void setUser(final String user) {
        this.user = user;
    }
    
    public String getPassword() {
        return this.password;
    }
}
