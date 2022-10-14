// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.stats;

public class ConnectionStats
{
    private long zlibCompressCostNs;
    private long zlibCompressCount;
    private long zlibDecompressCostNs;
    private long zlibDecompressCount;
    private long crc32RequestCostNs;
    private long crc32RequestCount;
    private long crc32ResponseCostNs;
    private long crc32ResponseCount;
    private long requestSendBytes;
    private long responseReadBytes;
    private long socketSendCostNs;
    private long socketSendCount;
    private long socketReadCostNs;
    private long socketReadCount;
    private long sendCommandCostNs;
    private long sendCommandCount;
    private long sqlQueryDirectCostNs;
    private long sqlQueryDirectCount;
    private long getConnectionMutexCostNs;
    private long getConnectionMutexCount;
    private long buildProto20PacketCostNs;
    private long buildProto20PacketCount;
    private long buildCompressPacketConstNs;
    private long buildCompressPacketCount;
    private long buildMysqlPacketCostNs;
    private long buildMysqlPacketCount;
    private long getNextRowCostNs;
    private long getNextRowCount;
    
    public ConnectionStats() {
        this.zlibCompressCostNs = 0L;
        this.zlibCompressCount = 0L;
        this.zlibDecompressCostNs = 0L;
        this.zlibDecompressCount = 0L;
        this.crc32RequestCostNs = 0L;
        this.crc32RequestCount = 0L;
        this.crc32ResponseCostNs = 0L;
        this.crc32ResponseCount = 0L;
        this.requestSendBytes = 0L;
        this.responseReadBytes = 0L;
        this.socketSendCostNs = 0L;
        this.socketSendCount = 0L;
        this.socketReadCostNs = 0L;
        this.socketReadCount = 0L;
        this.sendCommandCostNs = 0L;
        this.sendCommandCount = 0L;
        this.sqlQueryDirectCostNs = 0L;
        this.sqlQueryDirectCount = 0L;
        this.getConnectionMutexCostNs = 0L;
        this.getConnectionMutexCount = 0L;
        this.buildProto20PacketCostNs = 0L;
        this.buildProto20PacketCount = 0L;
        this.buildCompressPacketConstNs = 0L;
        this.buildCompressPacketCount = 0L;
        this.buildMysqlPacketCostNs = 0L;
        this.buildMysqlPacketCount = 0L;
        this.getNextRowCostNs = 0L;
        this.getNextRowCount = 0L;
    }
    
    public void addZlibCompressCostNs(final long deltaNs) {
        this.zlibCompressCostNs += deltaNs;
        ++this.zlibCompressCount;
    }
    
    public void addZlibDecompressCostNs(final long deltaNs) {
        this.zlibDecompressCostNs += deltaNs;
        ++this.zlibDecompressCount;
    }
    
    public void addCrc32RequestCostNs(final long deltaNs) {
        this.crc32RequestCostNs += deltaNs;
        ++this.crc32RequestCount;
    }
    
    public void addCrc32ResponseCostNs(final long deltaNs) {
        this.crc32ResponseCostNs += deltaNs;
        ++this.crc32ResponseCount;
    }
    
    public void addRequestSendBytes(final long delta) {
        this.requestSendBytes += delta;
    }
    
    public void addResponseReadBytes(final long delta) {
        this.responseReadBytes += delta;
    }
    
    public void addSocketSendCostNs(final long deltaNs) {
        this.socketSendCostNs += deltaNs;
        ++this.socketSendCount;
    }
    
    public void addSocketReadCostNs(final long deltaNs) {
        this.socketReadCostNs += deltaNs;
        ++this.socketReadCount;
    }
    
    public void addGetConnectionMutexCostNs(final long deltaNs) {
        this.getConnectionMutexCostNs += deltaNs;
        ++this.getConnectionMutexCount;
    }
    
    public void addBuildProto20PacketCostNs(final long deltaNs) {
        this.buildProto20PacketCostNs += deltaNs;
        ++this.buildProto20PacketCount;
    }
    
    public void addBuildCompressPacketConstNs(final long deltaNs) {
        this.buildCompressPacketConstNs += deltaNs;
        ++this.buildCompressPacketCount;
    }
    
    public void addBuildMysqlPacketCostNs(final long deltaNs) {
        this.buildMysqlPacketCostNs += deltaNs;
        ++this.buildMysqlPacketCount;
    }
    
    public void addSendCommandCostNs(final long deltaNs) {
        this.sendCommandCostNs += deltaNs;
        ++this.sendCommandCount;
    }
    
    public void addSqlQueryDirectCostNs(final long deltaNs) {
        this.sqlQueryDirectCostNs += deltaNs;
        ++this.sqlQueryDirectCount;
    }
    
    public void addGetNextRowCostNs(final long deltaNs) {
        this.getNextRowCostNs += deltaNs;
        ++this.getNextRowCount;
    }
    
    private long getValue(final long value) {
        return value / 1000L;
    }
    
    public void reset() {
        this.zlibCompressCostNs = 0L;
        this.zlibCompressCount = 0L;
        this.zlibDecompressCostNs = 0L;
        this.zlibDecompressCount = 0L;
        this.crc32RequestCostNs = 0L;
        this.crc32RequestCount = 0L;
        this.crc32ResponseCostNs = 0L;
        this.crc32ResponseCount = 0L;
        this.requestSendBytes = 0L;
        this.responseReadBytes = 0L;
        this.socketSendCostNs = 0L;
        this.socketSendCount = 0L;
        this.socketReadCostNs = 0L;
        this.socketReadCount = 0L;
        this.sendCommandCostNs = 0L;
        this.sendCommandCount = 0L;
        this.sqlQueryDirectCostNs = 0L;
        this.sqlQueryDirectCount = 0L;
        this.getConnectionMutexCostNs = 0L;
        this.getConnectionMutexCount = 0L;
        this.buildProto20PacketCostNs = 0L;
        this.buildProto20PacketCount = 0L;
        this.buildCompressPacketConstNs = 0L;
        this.buildCompressPacketCount = 0L;
        this.buildMysqlPacketCostNs = 0L;
        this.buildMysqlPacketCount = 0L;
        this.getNextRowCostNs = 0L;
        this.getNextRowCount = 0L;
    }
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("ConnectionStats {").append(" zlibCompressCost(us) = ").append(this.getValue(this.zlibCompressCostNs)).append(", zlibCompressCount = ").append(this.zlibCompressCount).append(", zlibDecompressCost(us) = ").append(this.getValue(this.zlibDecompressCostNs)).append(", zlibDecompressCount = ").append(this.zlibDecompressCount).append(", crc32RequestCost(us) = ").append(this.getValue(this.crc32RequestCostNs)).append(", crc32RequestCount = ").append(this.crc32RequestCount).append(", crc32ResponseCost(us) = ").append(this.getValue(this.crc32ResponseCostNs)).append(", crc32ResponseCount = ").append(this.crc32ResponseCount).append(", requestSendBytes = ").append(this.requestSendBytes).append(", responseReadBytes = ").append(this.responseReadBytes).append(", socketSendCost(us) = ").append(this.getValue(this.socketSendCostNs)).append(", socketSendCount = ").append(this.socketSendCount).append(", socketReadCost(us) = ").append(this.getValue(this.socketReadCostNs)).append(", socketReadCount = ").append(this.socketReadCount).append(", sendCommandCost(us) = ").append(this.getValue(this.sendCommandCostNs)).append(", sendCommandCount = ").append(this.sendCommandCount).append(", sqlQueryDirectCost(us) = ").append(this.getValue(this.sqlQueryDirectCostNs)).append(", sqlQueryDirectCount = ").append(this.sqlQueryDirectCount).append(", getConnectionMutexCost(us) = ").append(this.getValue(this.getConnectionMutexCostNs)).append(", getConnectionMutexCount = ").append(this.getConnectionMutexCount).append(", buildProto20PacketCost(us) = ").append(this.getValue(this.buildProto20PacketCostNs)).append(", buildProto20PacketCount = ").append(this.buildProto20PacketCount).append(", buildCompressPacketConst(us) = ").append(this.getValue(this.buildCompressPacketConstNs)).append(", buildCompressPacketCount = ").append(this.buildCompressPacketCount).append(", buildMysqlPacketCost(us) = ").append(this.getValue(this.buildMysqlPacketCostNs)).append(", buildMysqlPacketCount = ").append(this.buildMysqlPacketCount).append(", getNextRowCost(us) = ").append(this.getValue(this.getNextRowCostNs)).append(", getNextRowCount = ").append(this.getNextRowCount).append("}");
        return builder.toString();
    }
}
