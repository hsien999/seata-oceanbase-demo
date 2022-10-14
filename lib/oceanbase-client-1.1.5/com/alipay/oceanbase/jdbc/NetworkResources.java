// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.Socket;

class NetworkResources
{
    private final Socket mysqlConnection;
    private final InputStream mysqlInput;
    private final OutputStream mysqlOutput;
    
    protected NetworkResources(final Socket mysqlConnection, final InputStream mysqlInput, final OutputStream mysqlOutput) {
        this.mysqlConnection = mysqlConnection;
        this.mysqlInput = mysqlInput;
        this.mysqlOutput = mysqlOutput;
    }
    
    protected final void forceClose() {
        try {
            try {
                if (this.mysqlInput != null) {
                    this.mysqlInput.close();
                }
            }
            finally {
                if (this.mysqlConnection != null && !this.mysqlConnection.isClosed() && !this.mysqlConnection.isInputShutdown()) {
                    try {
                        this.mysqlConnection.shutdownInput();
                    }
                    catch (UnsupportedOperationException ex) {}
                }
            }
        }
        catch (IOException ex2) {}
        try {
            try {
                if (this.mysqlOutput != null) {
                    this.mysqlOutput.close();
                }
            }
            finally {
                if (this.mysqlConnection != null && !this.mysqlConnection.isClosed() && !this.mysqlConnection.isOutputShutdown()) {
                    try {
                        this.mysqlConnection.shutdownOutput();
                    }
                    catch (UnsupportedOperationException ex3) {}
                }
            }
        }
        catch (IOException ex4) {}
        try {
            if (this.mysqlConnection != null) {
                this.mysqlConnection.close();
            }
        }
        catch (IOException ex5) {}
    }
}
