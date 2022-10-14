// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.feedback;

import java.util.ArrayList;
import java.util.List;

public class ObFBLocation extends ObFBElement
{
    private long tableId;
    private long partitionId;
    private long schemaVersion;
    List<ObFBReplica> replicas;
    
    public ObFBLocation() {
        super(ObFeedbackType.PARTITION_LOCATION_FB_ELE);
        this.tableId = Long.MIN_VALUE;
        this.partitionId = Long.MIN_VALUE;
        this.schemaVersion = Long.MIN_VALUE;
        this.replicas = new ArrayList<ObFBReplica>();
    }
    
    @Override
    public void decode(final ObFeedBackBuffer buffer) throws ObFBOutOfBoundException {
        ObLongValue value = buffer.readVariableLong();
        if (value.isRead) {
            this.tableId = value.valueLong;
        }
        value = buffer.readVariableLong();
        if (value.isRead) {
            this.partitionId = value.valueLong;
        }
        value = buffer.readVariableLong();
        if (value.isRead) {
            this.schemaVersion = value.valueLong;
        }
        long replicaCount = 0L;
        value = buffer.readVariableLong();
        if (value.isRead) {
            replicaCount = value.valueLong;
            for (int i = 0; i < replicaCount; ++i) {
                final ObFBReplica replica = new ObFBReplica();
                replica.decodeFbElement(buffer);
                if (replica.isValid()) {
                    this.replicas.add(replica);
                }
            }
        }
    }
    
    @Override
    public boolean isValid() {
        return Long.MIN_VALUE != this.tableId && Long.MIN_VALUE != this.partitionId && Long.MIN_VALUE != this.schemaVersion && !this.replicas.isEmpty();
    }
    
    public long getTableId() {
        return this.tableId;
    }
    
    public long getPartitionId() {
        return this.partitionId;
    }
    
    public long getSchemaVersion() {
        return this.schemaVersion;
    }
    
    public List<ObFBReplica> getReplicas() {
        return this.replicas;
    }
    
    @Override
    public String toString() {
        return "ObFBLocation{tableId=" + this.tableId + ", partitionId=" + this.partitionId + ", schemaVersion=" + this.schemaVersion + ", replicas=" + this.replicas + ", type=" + this.type + '}';
    }
}
