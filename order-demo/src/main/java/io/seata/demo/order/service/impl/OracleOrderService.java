package io.seata.demo.order.service.impl;

import io.seata.demo.order.dao.OrderMapper;
import io.seata.demo.order.dao.impl.OracleOrderMapper;
import io.seata.demo.order.feign.StorageFeignClient;
import io.seata.demo.order.feign.impl.OracleStorageFeignClient;
import io.seata.demo.order.service.OrderService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OracleOrderService extends OrderService {
    @Resource(type = OracleOrderMapper.class)
    private OrderMapper oracleOrderMapper;

    @Resource(type = OracleStorageFeignClient.class)
    private StorageFeignClient oracleStorageFeignClient;

    @Override
    protected OrderMapper getOrderMapper() {
        return oracleOrderMapper;
    }

    @Override
    protected StorageFeignClient getStorageFeignClient() {
        return oracleStorageFeignClient;
    }
}
