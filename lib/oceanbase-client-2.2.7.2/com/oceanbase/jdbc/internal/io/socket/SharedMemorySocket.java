// 
// Decompiled by Procyon v0.5.36
// 

package com.oceanbase.jdbc.internal.io.socket;

import java.util.Arrays;
import java.util.List;
import com.sun.jna.Structure;
import com.sun.jna.Native;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APITypeMapper;
import com.sun.jna.win32.W32APIFunctionMapper;
import java.util.HashMap;
import com.sun.jna.LastErrorException;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import java.net.SocketAddress;
import com.sun.jna.platform.win32.BaseTSD;
import java.io.IOException;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.WinNT;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Map;
import java.net.Socket;

public class SharedMemorySocket extends Socket
{
    private static final String EVERYONE_SYNCHRONIZE_SDDL = "D:(A;;0x100000;;;WD)";
    private static final Map<String, Object> WIN32API_OPTIONS;
    private static final int BUFFERLEN = 16004;
    private InputStream is;
    private OutputStream os;
    private String memoryName;
    private WinNT.HANDLE serverRead;
    private WinNT.HANDLE serverWrote;
    private WinNT.HANDLE clientRead;
    private WinNT.HANDLE clientWrote;
    private WinNT.HANDLE connectionClosed;
    private Pointer view;
    private int bytesLeft;
    private int position;
    private int timeout;
    
    public SharedMemorySocket(final String name) throws IOException {
        this.timeout = -1;
        if (!Platform.isWindows()) {
            throw new IOException("shared memory connections are only supported on Windows");
        }
        this.memoryName = name;
    }
    
    private static WinNT.HANDLE openEvent(final String name) {
        return Kernel32.INSTANCE.OpenEvent(1048578, false, name);
    }
    
    private static Pointer mapMemory(final String mapName, final int mode, final int size) {
        final WinNT.HANDLE mapping = Kernel32.INSTANCE.OpenFileMapping(mode, false, mapName);
        final Pointer v = Kernel32.INSTANCE.MapViewOfFile(mapping, mode, 0, 0, new BaseTSD.SIZE_T((long)size));
        Kernel32.INSTANCE.CloseHandle(mapping);
        return v;
    }
    
    @Override
    public void connect(final SocketAddress endpoint) throws IOException {
        this.connect(endpoint, 0);
    }
    
    private WinNT.HANDLE lockMutex() throws IOException {
        final PointerByReference securityDescriptor = new PointerByReference();
        Advapi32.INSTANCE.ConvertStringSecurityDescriptorToSecurityDescriptor("D:(A;;0x100000;;;WD)", 1, securityDescriptor, null);
        final Advapi32.SECURITY_ATTRIBUTES sa = new Advapi32.SECURITY_ATTRIBUTES();
        sa.nLength = sa.size();
        sa.lpSecurityDescriptor = securityDescriptor.getValue();
        sa.bInheritHandle = false;
        final WinNT.HANDLE mutex = Kernel32.INSTANCE.CreateMutex(sa, false, this.memoryName + "_CONNECT_MUTEX");
        Kernel32.INSTANCE.LocalFree(securityDescriptor.getValue());
        if (Kernel32.INSTANCE.WaitForSingleObject(mutex, this.timeout) == -1) {
            Kernel32.INSTANCE.CloseHandle(mutex);
            throw new IOException("wait failed (timeout, last error =  " + Kernel32.INSTANCE.GetLastError());
        }
        return mutex;
    }
    
    private int getConnectNumber() throws IOException {
        WinNT.HANDLE connectRequest;
        try {
            connectRequest = openEvent(this.memoryName + "_CONNECT_REQUEST");
        }
        catch (LastErrorException lee3) {
            try {
                connectRequest = openEvent("Global\\" + this.memoryName + "_CONNECT_REQUEST");
                this.memoryName = "Global\\" + this.memoryName;
            }
            catch (LastErrorException lee2) {
                throw new IOException("getConnectNumber() fails : " + lee2.getMessage() + " " + this.memoryName);
            }
        }
        final WinNT.HANDLE connectAnswer = openEvent(this.memoryName + "_CONNECT_ANSWER");
        final WinNT.HANDLE mutex = this.lockMutex();
        Pointer connectData = null;
        try {
            Kernel32.INSTANCE.SetEvent(connectRequest);
            connectData = mapMemory(this.memoryName + "_CONNECT_DATA", 4, 4);
            final int ret = Kernel32.INSTANCE.WaitForSingleObject(connectAnswer, this.timeout);
            if (ret != 0) {
                throw new IOException("WaitForSingleObject returned " + ret + ", last error " + Kernel32.INSTANCE.GetLastError());
            }
            return connectData.getInt(0L);
        }
        finally {
            Kernel32.INSTANCE.ReleaseMutex(mutex);
            Kernel32.INSTANCE.CloseHandle(mutex);
            if (connectData != null) {
                Kernel32.INSTANCE.UnmapViewOfFile(connectData);
            }
            Kernel32.INSTANCE.CloseHandle(connectRequest);
            Kernel32.INSTANCE.CloseHandle(connectAnswer);
        }
    }
    
    @Override
    public void connect(final SocketAddress endpoint, final int timeout) throws IOException {
        try {
            this.is = new SharedMemoryInputStream();
            this.os = new SharedMemoryOutputStream();
            final String prefix = this.memoryName + "_" + this.getConnectNumber();
            this.clientRead = openEvent(prefix + "_CLIENT_READ");
            this.serverRead = openEvent(prefix + "_SERVER_READ");
            this.serverWrote = openEvent(prefix + "_SERVER_WROTE");
            this.clientWrote = openEvent(prefix + "_CLIENT_WROTE");
            this.connectionClosed = openEvent(prefix + "_CONNECTION_CLOSED");
            this.view = mapMemory(prefix + "_DATA", 2, 16004);
            Kernel32.INSTANCE.SetEvent(this.serverRead);
        }
        catch (LastErrorException lee) {
            throw new IOException(lee.getMessage(), lee.getCause());
        }
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
    public void setSoTimeout(final int t) {
        if (t == 0) {
            this.timeout = -1;
        }
        else {
            this.timeout = t;
        }
    }
    
    @Override
    public void shutdownInput() {
    }
    
    @Override
    public void shutdownOutput() {
    }
    
    @Override
    public void close() {
        if (this.connectionClosed != null && Kernel32.INSTANCE.WaitForSingleObject(this.connectionClosed, 0) != 0) {
            Kernel32.INSTANCE.SetEvent(this.connectionClosed);
        }
        final WinNT.HANDLE[] array;
        final WinNT.HANDLE[] handles = array = new WinNT.HANDLE[] { this.serverRead, this.serverWrote, this.clientRead, this.clientWrote, this.connectionClosed };
        for (final WinNT.HANDLE h : array) {
            if (h != null) {
                Kernel32.INSTANCE.CloseHandle(h);
            }
        }
        if (this.view != null) {
            Kernel32.INSTANCE.UnmapViewOfFile(this.view);
        }
        this.serverRead = null;
        this.serverWrote = null;
        this.clientRead = null;
        this.clientWrote = null;
        this.connectionClosed = null;
        this.view = null;
    }
    
    static {
        (WIN32API_OPTIONS = new HashMap<String, Object>()).put("function-mapper", W32APIFunctionMapper.UNICODE);
        SharedMemorySocket.WIN32API_OPTIONS.put("type-mapper", W32APITypeMapper.UNICODE);
    }
    
    public interface Kernel32 extends StdCallLibrary
    {
        public static final Kernel32 INSTANCE = (Kernel32)Native.load("Kernel32", (Class)Kernel32.class, SharedMemorySocket.WIN32API_OPTIONS);
        public static final int FILE_MAP_WRITE = 2;
        public static final int FILE_MAP_READ = 4;
        public static final int EVENT_MODIFY_STATE = 2;
        public static final int SYNCHRONIZE = 1048576;
        public static final int INFINITE = -1;
        
        WinNT.HANDLE OpenEvent(final int p0, final boolean p1, final String p2) throws LastErrorException;
        
        WinNT.HANDLE OpenFileMapping(final int p0, final boolean p1, final String p2) throws LastErrorException;
        
        Pointer MapViewOfFile(final WinNT.HANDLE p0, final int p1, final int p2, final int p3, final BaseTSD.SIZE_T p4) throws LastErrorException;
        
        boolean UnmapViewOfFile(final Pointer p0) throws LastErrorException;
        
        boolean SetEvent(final WinNT.HANDLE p0) throws LastErrorException;
        
        boolean CloseHandle(final WinNT.HANDLE p0) throws LastErrorException;
        
        int WaitForSingleObject(final WinNT.HANDLE p0, final int p1) throws LastErrorException;
        
        int WaitForMultipleObjects(final int p0, final WinNT.HANDLE[] p1, final boolean p2, final int p3) throws LastErrorException;
        
        int GetLastError() throws LastErrorException;
        
        WinNT.HANDLE CreateMutex(final Advapi32.SECURITY_ATTRIBUTES p0, final boolean p1, final String p2);
        
        boolean ReleaseMutex(final WinNT.HANDLE p0);
        
        Pointer LocalFree(final Pointer p0);
    }
    
    public interface Advapi32 extends StdCallLibrary
    {
        public static final Advapi32 INSTANCE = (Advapi32)Native.load("advapi32", (Class)Advapi32.class, SharedMemorySocket.WIN32API_OPTIONS);
        
        boolean ConvertStringSecurityDescriptorToSecurityDescriptor(final String p0, final int p1, final PointerByReference p2, final IntByReference p3);
        
        public static class SECURITY_ATTRIBUTES extends Structure
        {
            public int nLength;
            public Pointer lpSecurityDescriptor;
            public boolean bInheritHandle;
            
            protected List<String> getFieldOrder() {
                return Arrays.asList("nLength", "lpSecurityDescriptor", "bInheritHandle");
            }
        }
    }
    
    class SharedMemoryInputStream extends InputStream
    {
        @Override
        public int read(final byte[] bytes, final int off, final int count) throws IOException {
            final WinNT.HANDLE[] handles = { SharedMemorySocket.this.serverWrote, SharedMemorySocket.this.connectionClosed };
            if (SharedMemorySocket.this.bytesLeft == 0) {
                final int index = Kernel32.INSTANCE.WaitForMultipleObjects(2, handles, false, SharedMemorySocket.this.timeout);
                if (index == -1) {
                    throw new IOException("wait failed, timeout");
                }
                if (index == 1) {
                    throw new IOException("Server closed connection");
                }
                if (index != 0) {
                    throw new IOException("Unexpected return result from WaitForMultipleObjects : " + index);
                }
                SharedMemorySocket.this.bytesLeft = SharedMemorySocket.this.view.getInt(0L);
                SharedMemorySocket.this.position = 4;
            }
            final int len = Math.min(count, SharedMemorySocket.this.bytesLeft);
            SharedMemorySocket.this.view.read((long)SharedMemorySocket.this.position, bytes, off, len);
            SharedMemorySocket.this.position += len;
            SharedMemorySocket.this.bytesLeft -= len;
            if (SharedMemorySocket.this.bytesLeft == 0) {
                Kernel32.INSTANCE.SetEvent(SharedMemorySocket.this.clientRead);
            }
            return len;
        }
        
        @Override
        public int read() throws IOException {
            final byte[] bit = { 0 };
            final int bytesRead = this.read(bit);
            if (bytesRead == 0) {
                return -1;
            }
            return bit[0] & 0xFF;
        }
        
        @Override
        public int read(final byte[] bytes) throws IOException {
            return this.read(bytes, 0, bytes.length);
        }
    }
    
    class SharedMemoryOutputStream extends OutputStream
    {
        @Override
        public void write(final byte[] bytes, final int off, final int count) throws IOException {
            int bytesToWrite = count;
            int buffPos = off;
            final WinNT.HANDLE[] handles = { SharedMemorySocket.this.serverRead, SharedMemorySocket.this.connectionClosed };
            while (bytesToWrite > 0) {
                final int index = Kernel32.INSTANCE.WaitForMultipleObjects(2, handles, false, SharedMemorySocket.this.timeout);
                if (index == -1) {
                    throw new IOException("WaitForMultipleObjects() failed, timeout");
                }
                if (index == 1) {
                    throw new IOException("Server closed connection");
                }
                if (index != 0) {
                    throw new IOException("Unexpected return result from WaitForMultipleObjects : " + index);
                }
                final int chunk = Math.min(bytesToWrite, 16004);
                SharedMemorySocket.this.view.setInt(0L, chunk);
                SharedMemorySocket.this.view.write(4L, bytes, buffPos, chunk);
                buffPos += chunk;
                bytesToWrite -= chunk;
                if (!Kernel32.INSTANCE.SetEvent(SharedMemorySocket.this.clientWrote)) {
                    throw new IOException("SetEvent failed");
                }
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
