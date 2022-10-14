// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.credential.system;

import com.oceanbase.jdbc.credential.Credential;
import com.oceanbase.jdbc.HostAddress;
import com.oceanbase.jdbc.util.Options;
import com.oceanbase.jdbc.credential.CredentialPlugin;

public class PropertiesCredentialPlugin implements CredentialPlugin
{
    private Options options;
    private String userName;
    
    @Override
    public String type() {
        return "PROPERTY";
    }
    
    @Override
    public String name() {
        return "Environment password";
    }
    
    @Override
    public CredentialPlugin initialize(final Options options, final String userName, final HostAddress hostAddress) {
        this.options = options;
        this.userName = userName;
        return this;
    }
    
    @Override
    public Credential get() {
        final String userKey = this.options.nonMappedOptions.getProperty("userKey");
        final String pwdKey = this.options.nonMappedOptions.getProperty("pwdKey");
        final String propUser = System.getProperty((userKey != null) ? userKey : "mariadb.user");
        return new Credential((propUser == null) ? this.userName : propUser, System.getProperty((pwdKey != null) ? pwdKey : "mariadb.pwd"));
    }
}
