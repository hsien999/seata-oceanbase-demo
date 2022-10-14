package io.seata.demo.storage.dao;

import io.seata.demo.storage.entity.Orders;

public interface StorageMapper {
    void update(Orders order, int used);
}
