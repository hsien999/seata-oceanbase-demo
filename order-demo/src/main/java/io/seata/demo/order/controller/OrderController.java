package io.seata.demo.order.controller;

import io.seata.demo.order.service.OrderService;
import io.seata.demo.order.service.impl.OracleOrderService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/oracle")
public class OrderController extends BaseController {

    @Resource(type = OracleOrderService.class)
    private OrderService oracleOrderService;

    @Override
    protected OrderService getOrderService() {
        return oracleOrderService;
    }
}
