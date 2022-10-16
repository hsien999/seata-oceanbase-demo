package io.seata.demo.order.dao;

import io.seata.demo.order.entity.Orders;

public interface OrderMapper {
    void insert(Orders order);

    void update(Orders order);

    void delete(Orders order);

    Orders select(Orders order);
}
