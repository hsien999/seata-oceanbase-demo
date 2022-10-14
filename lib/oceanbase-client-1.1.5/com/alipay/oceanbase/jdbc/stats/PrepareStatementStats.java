// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.stats;

public class PrepareStatementStats
{
    PSType type;
    private long checksServerPSMetaCachedCostNs;
    private long checksServerPSMetaCachedCount;
    private long cacheServerPSMetaCostNs;
    private long cacheServerPSMetaCount;
    private long serverPSDoPrepareCostNs;
    private long serverPSDoPrepareCount;
    private long resetPSCostNs;
    private long resetPSCount;
    private long serverPSPreExecuteCostNs;
    private long serverPSPreExecuteCount;
    private long serverPSExecuteCostNs;
    private long serverPSExecuteCount;
    private long serverPSExecuteTotalCostNs;
    private long serverPSExecuteTotalCount;
    private long serverPsRealCloseCostNs;
    private long serverPsRealCloseCount;
    private long serverPsSendLongDataCostNs;
    private long serverPsSendLongDataCount;
    private long fillSendPacketCostNs;
    private long fillSendPacketCount;
    private long clientPSExecuteTotalCostNs;
    private long clientPSExecuteTotalCount;
    private long getMetaDataCostNs;
    private long getMetaDataCount;
    private long parseInfoCostNs;
    private long parseInfoCount;
    
    public PrepareStatementStats() {
        this.type = PSType.MinPStype;
        this.checksServerPSMetaCachedCostNs = 0L;
        this.checksServerPSMetaCachedCount = 0L;
        this.cacheServerPSMetaCostNs = 0L;
        this.cacheServerPSMetaCount = 0L;
        this.serverPSDoPrepareCostNs = 0L;
        this.serverPSDoPrepareCount = 0L;
        this.resetPSCostNs = 0L;
        this.resetPSCount = 0L;
        this.serverPSPreExecuteCostNs = 0L;
        this.serverPSPreExecuteCount = 0L;
        this.serverPSExecuteCostNs = 0L;
        this.serverPSExecuteCount = 0L;
        this.serverPSExecuteTotalCostNs = 0L;
        this.serverPSExecuteTotalCount = 0L;
        this.serverPsRealCloseCostNs = 0L;
        this.serverPsRealCloseCount = 0L;
        this.serverPsSendLongDataCostNs = 0L;
        this.serverPsSendLongDataCount = 0L;
        this.fillSendPacketCostNs = 0L;
        this.fillSendPacketCount = 0L;
        this.clientPSExecuteTotalCostNs = 0L;
        this.clientPSExecuteTotalCount = 0L;
        this.getMetaDataCostNs = 0L;
        this.getMetaDataCount = 0L;
        this.parseInfoCostNs = 0L;
        this.parseInfoCount = 0L;
    }
    
    public void addFillSendPacketCostNs(final long deltaNs) {
        this.fillSendPacketCostNs += deltaNs;
        ++this.fillSendPacketCount;
    }
    
    public void addServerPSPreExecuteCostNs(final long deltaNs) {
        this.serverPSPreExecuteCostNs += deltaNs;
        ++this.serverPSPreExecuteCount;
    }
    
    public void addServerPSExecuteCostNs(final long deltaNs) {
        this.serverPSExecuteCostNs += deltaNs;
        ++this.serverPSExecuteCount;
    }
    
    public void addServerPSExecuteTotalCostNs(final long deltaNs) {
        this.serverPSExecuteTotalCostNs += deltaNs;
        ++this.serverPSExecuteTotalCount;
    }
    
    public void addServerPsRealCloseCostNs(final long deltaNs) {
        this.serverPsRealCloseCostNs += deltaNs;
        ++this.serverPsRealCloseCount;
    }
    
    public void addServerPsSendLongDataCostNs(final long deltaNs) {
        this.serverPsSendLongDataCostNs += deltaNs;
        ++this.serverPsSendLongDataCount;
    }
    
    public void addChecksServerPSMetaCachedCostNs(final long deltaNs) {
        this.checksServerPSMetaCachedCostNs += deltaNs;
        ++this.checksServerPSMetaCachedCount;
    }
    
    public void addCacheServerPSMetaCostNs(final long deltaNs) {
        this.cacheServerPSMetaCostNs += deltaNs;
        ++this.cacheServerPSMetaCount;
    }
    
    public void addServerPSDoPrepareCostNs(final long deltaNs) {
        this.serverPSDoPrepareCostNs += deltaNs;
        ++this.serverPSDoPrepareCount;
    }
    
    public void addResetPSCostNs(final long deltaNs) {
        this.resetPSCostNs += deltaNs;
        ++this.resetPSCount;
    }
    
    public void addClientPSExecuteTotalCostNs(final long deltaNs) {
        this.clientPSExecuteTotalCostNs += deltaNs;
        ++this.clientPSExecuteTotalCount;
    }
    
    public void addGetMetaDataCostNs(final long deltaNs) {
        this.getMetaDataCostNs += deltaNs;
        ++this.getMetaDataCount;
    }
    
    public void addParseInfoCostNs(final long deltaNs) {
        this.parseInfoCostNs += deltaNs;
        ++this.parseInfoCount;
    }
    
    public void setServerPsType() {
        this.type = PSType.ServerPSType;
    }
    
    public void setClientPsType() {
        this.type = PSType.ClientPSType;
    }
    
    public void setCallablePsType() {
        this.type = PSType.CallablePSType;
    }
    
    public void setServerCallablePSType() {
        this.type = PSType.ServerCallablePSType;
    }
    
    void reset() {
        this.checksServerPSMetaCachedCostNs = 0L;
        this.checksServerPSMetaCachedCount = 0L;
        this.cacheServerPSMetaCostNs = 0L;
        this.cacheServerPSMetaCount = 0L;
        this.serverPSDoPrepareCostNs = 0L;
        this.serverPSDoPrepareCount = 0L;
        this.resetPSCostNs = 0L;
        this.resetPSCount = 0L;
        this.serverPSPreExecuteCostNs = 0L;
        this.serverPSPreExecuteCount = 0L;
        this.serverPSExecuteCostNs = 0L;
        this.serverPSExecuteCount = 0L;
        this.serverPSExecuteTotalCostNs = 0L;
        this.serverPSExecuteTotalCount = 0L;
        this.serverPsRealCloseCostNs = 0L;
        this.serverPsRealCloseCount = 0L;
        this.serverPsSendLongDataCostNs = 0L;
        this.serverPsSendLongDataCount = 0L;
        this.fillSendPacketCostNs = 0L;
        this.fillSendPacketCount = 0L;
        this.clientPSExecuteTotalCostNs = 0L;
        this.clientPSExecuteTotalCount = 0L;
        this.getMetaDataCostNs = 0L;
        this.getMetaDataCount = 0L;
        this.parseInfoCostNs = 0L;
        this.parseInfoCount = 0L;
    }
    
    private long getValue(final long value) {
        return value / 1000L;
    }
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("PrepareStatementStats {").append("type = ").append(this.type.name()).append(", serverPSDoPrepareCost(us) = ").append(this.getValue(this.serverPSDoPrepareCostNs)).append(", serverPSDoPrepareCount = ").append(this.serverPSDoPrepareCount).append(", checksServerPSMetaCachedCost(us) = ").append(this.getValue(this.checksServerPSMetaCachedCostNs)).append(", checksServerPSMetaCachedCount = ").append(this.checksServerPSMetaCachedCount).append(", cacheServerPSMetaCost(us) = ").append(this.getValue(this.cacheServerPSMetaCostNs)).append(", cacheServerPSMetaCount = ").append(this.cacheServerPSMetaCount).append(", resetPSCost(us) = ").append(this.getValue(this.resetPSCostNs)).append(", resetPSCount = ").append(this.resetPSCount).append(", serverPSPreExecuteCost(us) = ").append(this.getValue(this.serverPSPreExecuteCostNs)).append(", serverPSPreExecuteCount = ").append(this.serverPSPreExecuteCount).append(", serverPSExecuteCost(us) = ").append(this.getValue(this.serverPSExecuteCostNs)).append(", serverPSExecuteCount = ").append(this.serverPSExecuteCount).append(", serverPSExecuteTotalCost(us) = ").append(this.getValue(this.serverPSExecuteTotalCostNs)).append(", serverPSExecuteTotalCount = ").append(this.serverPSExecuteTotalCount).append(", serverPsRealCloseCost(us) = ").append(this.getValue(this.serverPsRealCloseCostNs)).append(", serverPsRealCloseCount = ").append(this.serverPsRealCloseCount).append(", serverPsSendLongDataCost(us) = ").append(this.getValue(this.serverPsSendLongDataCostNs)).append(", serverPsSendLongDataCount = ").append(this.serverPsSendLongDataCount).append(", fillSendPacketCost(us) = ").append(this.getValue(this.fillSendPacketCostNs)).append(", fillSendPacketCount = ").append(this.fillSendPacketCount).append(", clientPSExecuteTotalCost(us) = ").append(this.getValue(this.clientPSExecuteTotalCostNs)).append(", clientPSExecuteTotalCount = ").append(this.clientPSExecuteTotalCount).append(", getMetaDataCost(us) = ").append(this.getValue(this.getMetaDataCostNs)).append(", getMetaDataCount = ").append(this.getMetaDataCount).append(", parseInfoCost(us) = ").append(this.getValue(this.parseInfoCostNs)).append(", parseInfoCount = ").append(this.parseInfoCount).append("}");
        return builder.toString();
    }
    
    enum PSType
    {
        MinPStype, 
        ClientPSType, 
        ServerPSType, 
        CallablePSType, 
        ServerCallablePSType;
    }
}
