// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.socket;

import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;
import com.sun.jna.platform.win32.Kernel32;
import java.net.SocketAddress;
import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

public class NamedPipeSocket extends Socket
{
    private final String host;
    private final String name;
    private RandomAccessFile file;
    private InputStream is;
    private OutputStream os;
    
    public NamedPipeSocket(final String host, final String name) {
        this.host = host;
        this.name = name;
    }
    
    @Override
    public void close() throws IOException {
        if (this.file != null) {
            this.file.close();
            this.file = null;
        }
    }
    
    @Override
    public void connect(final SocketAddress endpoint) throws IOException {
        this.connect(endpoint, 0);
    }
    
    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        String filename;
        if (this.host == null || this.host.equals("localhost")) {
            filename = "\\\\.\\pipe\\" + this.name;
        }
        else {
            filename = "\\\\" + this.host + "\\pipe\\" + this.name;
        }
        final int usedTimeout = (timeout == 0) ? 100 : timeout;
        final long initialNano = System.nanoTime();
        while (true) {
            try {
                this.file = new RandomAccessFile(filename, "rw");
            }
            catch (FileNotFoundException fileNotFoundException) {
                try {
                    Kernel32.INSTANCE.WaitNamedPipe(filename, timeout);
                    this.file = new RandomAccessFile(filename, "rw");
                }
                catch (Throwable cle) {
                    if (System.nanoTime() - initialNano > TimeUnit.MILLISECONDS.toNanos(usedTimeout)) {
                        if (timeout == 0) {
                            throw new FileNotFoundException(fileNotFoundException.getMessage() + "\nplease consider set connectTimeout option, so connection can retry having access to named pipe. " + "\n(Named pipe can throw ERROR_PIPE_BUSY error)");
                        }
                        throw fileNotFoundException;
                    }
                    else {
                        try {
                            TimeUnit.MILLISECONDS.sleep(5L);
                        }
                        catch (InterruptedException interrupted) {
                            final IOException ioException = new IOException("Interruption during connection to named pipe");
                            ioException.initCause(interrupted);
                            throw ioException;
                        }
                    }
                }
                continue;
            }
            break;
        }
        this.is = new InputStream() {
            @Override
            public int read(final byte[] bytes, final int off, final int len) throws IOException {
                return NamedPipeSocket.this.file.read(bytes, off, len);
            }
            
            @Override
            public int read() throws IOException {
                return NamedPipeSocket.this.file.read();
            }
            
            @Override
            public int read(final byte[] bytes) throws IOException {
                return NamedPipeSocket.this.file.read(bytes);
            }
        };
        this.os = new OutputStream() {
            @Override
            public void write(final byte[] bytes, final int off, final int len) throws IOException {
                NamedPipeSocket.this.file.write(bytes, off, len);
            }
            
            @Override
            public void write(final int value) throws IOException {
                NamedPipeSocket.this.file.write(value);
            }
            
            @Override
            public void write(final byte[] bytes) throws IOException {
                NamedPipeSocket.this.file.write(bytes);
            }
        };
    }
    
    @Override
    public InputStream getInputStream() {
        return this.is;
    }
    
    @Override
    public OutputStream getOutputStream() {
        return this.os;
    }
    
    @Override
    public void setTcpNoDelay(final boolean bool) {
    }
    
    @Override
    public void setKeepAlive(final boolean bool) {
    }
    
    @Override
    public void setReceiveBufferSize(final int size) {
    }
    
    @Override
    public void setSendBufferSize(final int size) {
    }
    
    @Override
    public void setSoLinger(final boolean bool, final int value) {
    }
    
    @Override
    public void setSoTimeout(final int timeout) {
    }
    
    @Override
    public void shutdownInput() {
    }
    
    @Override
    public void shutdownOutput() {
    }
}
