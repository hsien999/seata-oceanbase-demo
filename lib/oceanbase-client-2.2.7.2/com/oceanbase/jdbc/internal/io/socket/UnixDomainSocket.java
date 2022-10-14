// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.socket;

import java.util.Arrays;
import java.util.List;
import com.sun.jna.Structure;
import com.sun.jna.Native;
import java.net.SocketAddress;
import com.sun.jna.LastErrorException;
import java.io.IOException;
import com.sun.jna.Platform;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.net.Socket;

public class UnixDomainSocket extends Socket
{
    private static final int AF_UNIX = 1;
    private static final int SOCK_STREAM;
    private static final int PROTOCOL = 0;
    private final AtomicBoolean closeLock;
    private final SockAddr sockaddr;
    private final int fd;
    private InputStream is;
    private OutputStream os;
    private boolean connected;
    
    public UnixDomainSocket(final String path) throws IOException {
        this.closeLock = new AtomicBoolean();
        if (Platform.isWindows() || Platform.isWindowsCE()) {
            throw new IOException("Unix domain sockets are not supported on Windows");
        }
        this.sockaddr = new SockAddr(path);
        this.closeLock.set(false);
        try {
            this.fd = socket(1, UnixDomainSocket.SOCK_STREAM, 0);
        }
        catch (LastErrorException lee) {
            throw new IOException("native socket() failed : " + formatError(lee));
        }
    }
    
    public static native int socket(final int p0, final int p1, final int p2) throws LastErrorException;
    
    public static native int connect(final int p0, final SockAddr p1, final int p2) throws LastErrorException;
    
    public static native int recv(final int p0, final byte[] p1, final int p2, final int p3) throws LastErrorException;
    
    public static native int send(final int p0, final byte[] p1, final int p2, final int p3) throws LastErrorException;
    
    public static native int close(final int p0) throws LastErrorException;
    
    public static native String strerror(final int p0);
    
    private static String formatError(final LastErrorException lee) {
        try {
            return strerror(lee.getErrorCode());
        }
        catch (Throwable t) {
            return lee.getMessage();
        }
    }
    
    @Override
    public boolean isConnected() {
        return this.connected;
    }
    
    @Override
    public void close() throws IOException {
        if (!this.closeLock.getAndSet(true)) {
            try {
                close(this.fd);
            }
            catch (LastErrorException lee) {
                throw new IOException("native close() failed : " + formatError(lee));
            }
            this.connected = false;
        }
    }
    
    @Override
    public void connect(final SocketAddress endpoint) throws IOException {
        this.connect(endpoint, 0);
    }
    
    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        try {
            final int ret = connect(this.fd, this.sockaddr, this.sockaddr.size());
            if (ret != 0) {
                throw new IOException(strerror(Native.getLastError()));
            }
            this.connected = true;
        }
        catch (LastErrorException lee) {
            throw new IOException("native connect() failed : " + formatError(lee));
        }
        this.is = new UnixSocketInputStream();
        this.os = new UnixSocketOutputStream();
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
    public void setTcpNoDelay(final boolean b) {
    }
    
    @Override
    public void setKeepAlive(final boolean b) {
    }
    
    @Override
    public void setReceiveBufferSize(final int size) {
    }
    
    @Override
    public void setSendBufferSize(final int size) {
    }
    
    @Override
    public void setSoLinger(final boolean b, final int i) {
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
    
    static {
        SOCK_STREAM = (Platform.isSolaris() ? 2 : 1);
        if (Platform.isSolaris()) {
            System.loadLibrary("nsl");
            System.loadLibrary("socket");
        }
        if (!Platform.isWindows() && !Platform.isWindowsCE()) {
            Native.register("c");
        }
    }
    
    public static class SockAddr extends Structure
    {
        public short sun_family;
        public byte[] sun_path;
        
        public SockAddr(final String sunPath) {
            this.sun_family = 1;
            final byte[] arr = sunPath.getBytes();
            System.arraycopy(arr, 0, this.sun_path = new byte[arr.length + 1], 0, Math.min(this.sun_path.length - 1, arr.length));
            this.allocateMemory();
        }
        
        protected List<String> getFieldOrder() {
            return Arrays.asList("sun_family", "sun_path");
        }
    }
    
    class UnixSocketInputStream extends InputStream
    {
        @Override
        public int read(final byte[] bytesEntry, int off, final int len) throws IOException {
            try {
                if (off > 0) {
                    int bytes = 0;
                    int remainingLength = len;
                    final byte[] data = new byte[(len < 10240) ? len : 10240];
                    int size;
                    do {
                        size = UnixDomainSocket.recv(UnixDomainSocket.this.fd, data, (remainingLength < 10240) ? remainingLength : 10240, 0);
                        if (size > 0) {
                            System.arraycopy(data, 0, bytesEntry, off, size);
                            bytes += size;
                            off += size;
                            remainingLength -= size;
                        }
                    } while (remainingLength > 0 && size > 0);
                    return bytes;
                }
                return UnixDomainSocket.recv(UnixDomainSocket.this.fd, bytesEntry, len, 0);
            }
            catch (LastErrorException lee) {
                throw new IOException("native read() failed : " + formatError(lee));
            }
        }
        
        @Override
        public int read() throws IOException {
            final byte[] bytes = { 0 };
            final int bytesRead = this.read(bytes);
            if (bytesRead == 0) {
                return -1;
            }
            return bytes[0] & 0xFF;
        }
        
        @Override
        public int read(final byte[] bytes) throws IOException {
            return this.read(bytes, 0, bytes.length);
        }
    }
    
    class UnixSocketOutputStream extends OutputStream
    {
        @Override
        public void write(final byte[] bytesEntry, int off, final int len) throws IOException {
            try {
                int bytes;
                if (off > 0) {
                    int remainingLength = len;
                    final byte[] data = new byte[(len < 10240) ? len : 10240];
                    do {
                        final int size = (remainingLength < 10240) ? remainingLength : 10240;
                        System.arraycopy(bytesEntry, off, data, 0, size);
                        bytes = UnixDomainSocket.send(UnixDomainSocket.this.fd, data, size, 0);
                        if (bytes > 0) {
                            off += bytes;
                            remainingLength -= bytes;
                        }
                    } while (remainingLength > 0 && bytes > 0);
                }
                else {
                    bytes = UnixDomainSocket.send(UnixDomainSocket.this.fd, bytesEntry, len, 0);
                }
                if (bytes != len) {
                    throw new IOException("can't write " + len + "bytes");
                }
            }
            catch (LastErrorException lee) {
                throw new IOException("native write() failed : " + formatError(lee));
            }
        }
        
        @Override
        public void write(final int value) throws IOException {
            this.write(new byte[] { (byte)value });
        }
        
        @Override
        public void write(final byte[] bytes) throws IOException {
            this.write(bytes, 0, bytes.length);
        }
    }
}
