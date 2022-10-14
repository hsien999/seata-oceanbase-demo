package io.seata.demo.order.controller;


import io.seata.demo.order.service.OrderService;
import io.seata.demo.order.service.impl.MySQLOrderService;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

@RequestMapping("/mysql")
public class MySQLController extends BaseController {

    @Resource(type = MySQLOrderService.class)
    private OrderService mySQLOrderService;

    @Override
    protected OrderService getOrderService() {
        return mySQLOrderService;
    }
}
