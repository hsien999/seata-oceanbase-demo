package io.seata.demo.order.service;

import com.sun.istack.internal.NotNull;
import io.seata.demo.order.dao.OrderMapper;
import io.seata.demo.order.entity.Orders;
import io.seata.demo.order.feign.StorageFeignClient;
import io.seata.spring.annotation.GlobalTransactional;

public abstract class OrderService {

    protected abstract @NotNull OrderMapper getOrderMapper();

    protected abstract @NotNull StorageFeignClient getStorageFeignClient();

    @GlobalTransactional
    public void insertSuccess(Orders order) {
        getOrderMapper().insert(order);
        getStorageFeignClient().updateStorageSuccess(order, order.getCount());
    }

    @GlobalTransactional
    public void insertFail(Orders order) {
        getOrderMapper().insert(order);
        getStorageFeignClient().updateStorageFail(order, order.getCount());
    }

    @GlobalTransactional
    public void updateSuccess(Orders order) {
        int count = getOrderCount(order);
        getOrderMapper().update(order);
        getStorageFeignClient().updateStorageSuccess(order, count - order.getCount());
    }

    @GlobalTransactional
    public void updateFail(Orders order) {
        updateSuccess(order);
        int i = 1 / 0;
    }

    @GlobalTransactional
    public void deleteSuccess(Orders order) {
        int count = getOrderCount(order);
        getOrderMapper().delete(order);
        getStorageFeignClient().updateStorageSuccess(order, -count);
    }

    @GlobalTransactional
    public void deleteFail(Orders order) {
        deleteSuccess(order);
        int i = 1 / 0;
    }

    private int getOrderCount(Orders order) {
        return getOrderMapper().select(order).getCount();
    }
}
