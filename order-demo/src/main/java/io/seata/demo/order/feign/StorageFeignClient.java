package io.seata.demo.order.feign;

import io.seata.demo.order.entity.Orders;

public interface StorageFeignClient {
    void updateStorageSuccess(Orders orders, int used);

    void updateStorageFail(Orders orders, int used);
}
