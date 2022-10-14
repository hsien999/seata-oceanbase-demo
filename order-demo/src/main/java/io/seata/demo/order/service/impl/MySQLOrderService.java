package io.seata.demo.order.service.impl;

import io.seata.demo.order.dao.OrderMapper;
import io.seata.demo.order.dao.impl.MySQLOrderMapper;
import io.seata.demo.order.feign.StorageFeignClient;
import io.seata.demo.order.feign.impl.MySQLStorageFeignClient;
import io.seata.demo.order.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MySQLOrderService extends OrderService {
    @Resource(type = MySQLOrderMapper.class)
    private OrderMapper mySQLOrderMapper;

    @Resource(type = MySQLStorageFeignClient.class)
    private StorageFeignClient mySQLStorageFeignClient;


    @Override
    protected OrderMapper getOrderMapper() {
        return mySQLOrderMapper;
    }

    @Override
    protected StorageFeignClient getStorageFeignClient() {
        return mySQLStorageFeignClient;
    }
}
