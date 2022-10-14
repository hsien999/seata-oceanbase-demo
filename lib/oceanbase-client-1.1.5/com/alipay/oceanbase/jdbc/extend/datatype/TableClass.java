// 
// Decompiled by Procyon v0.5.36
// 

package com.alipay.oceanbase.jdbc.extend.datatype;

import java.util.HashMap;

class TableClass
{
    private HashMap<String, Integer> zoneToIdMap;
    private HashMap<Integer, String> idToZoneMap;
    private HashMap<Integer, String> oldIdToZoneMap;
    
    TableClass(final int id1, final float id) {
        this.zoneToIdMap = new HashMap<String, Integer>(id1, id);
        this.idToZoneMap = new HashMap<Integer, String>(id1, id);
        this.oldIdToZoneMap = new HashMap<Integer, String>(10, 0.99f);
    }
    
    void put(final String name, final Integer id) {
        this.zoneToIdMap.put(name, id);
        this.idToZoneMap.put(id, name);
    }
    
    void putOld(final String name, final Integer id) {
        this.oldIdToZoneMap.put(id, name);
    }
    
    Integer getID(final String name) {
        return this.zoneToIdMap.get(name);
    }
    
    String getZone(final Integer id) {
        return this.idToZoneMap.get(id);
    }
    
    String getOldZone(final Integer id) {
        return this.oldIdToZoneMap.get(id);
    }
}
