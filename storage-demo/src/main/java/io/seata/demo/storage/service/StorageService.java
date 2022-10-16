package io.seata.demo.storage.service;


import io.seata.demo.storage.dao.StorageMapper;
import io.seata.demo.storage.entity.Orders;

public abstract class StorageService {
    protected abstract StorageMapper getStorageMapper();

    public void updateSuccess(Orders order, int used) {
        getStorageMapper().update(order, used);
    }

    public void updateFail(Orders order, int used) {
        updateSuccess(order, used);
        int i = 1 / 0;
    }

}
