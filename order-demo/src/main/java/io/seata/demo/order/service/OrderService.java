package io.seata.demo.order.service;

import io.seata.demo.order.dao.OrderMapper;
import io.seata.demo.order.entity.Orders;
import io.seata.demo.order.feign.StorageFeignClient;
import io.seata.spring.annotation.GlobalTransactional;

import java.util.Objects;

public abstract class OrderService {

    protected abstract OrderMapper getOrderMapper();

    protected abstract StorageFeignClient getStorageFeignClient();

    @GlobalTransactional(name = "insertSuccess", rollbackFor = Exception.class)
    public void insertSuccess(Orders order) {
        getOrderMapper().insert(order);
        getStorageFeignClient().updateStorageSuccess(order, order.getCount());
    }

    @GlobalTransactional(name = "insertFail", rollbackFor = Exception.class)
    public void insertFail(Orders order) {
        getOrderMapper().insert(order);
        getStorageFeignClient().updateStorageFail(order, order.getCount());
    }

    @GlobalTransactional(name = "updateSuccess", rollbackFor = Exception.class)
    public void updateSuccess(Orders order) {
        int count = getOrderCount(order);
        getOrderMapper().update(order);
        System.out.println("count === " + count);
        getStorageFeignClient().updateStorageSuccess(order, order.getCount() - count);
    }

    @GlobalTransactional(name = "updateFail", rollbackFor = Exception.class)
    public void updateFail(Orders order) {
        updateSuccess(order);
        int i = 1 / 0;
    }

    @GlobalTransactional(name = "deleteSuccess", rollbackFor = Exception.class)
    public void deleteSuccess(Orders order) {
        int count = getOrderCount(order);
        getOrderMapper().delete(order);
        getStorageFeignClient().updateStorageSuccess(order, -count);
    }

    @GlobalTransactional(name = "deleteFail", rollbackFor = Exception.class)
    public void deleteFail(Orders order) {
        deleteSuccess(order);
        int i = 1 / 0;
    }

    private int getOrderCount(Orders order) {
        Orders dbOrders = getOrderMapper().select(order);
        if (Objects.isNull(dbOrders)) {
            throw new RuntimeException("Empty result for order: " + order);
        }
        return dbOrders.getCount();
    }
}
