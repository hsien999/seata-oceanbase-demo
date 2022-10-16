package io.seata.demo.order.controller;


import io.seata.demo.order.entity.Orders;
import io.seata.demo.order.service.OrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public abstract class BaseController {
    protected abstract OrderService getOrderService();

    @PostMapping("/insert/success")
    public String insertOrderSuccess(@RequestBody Orders orders) {
        try {
            getOrderService().insertSuccess(orders);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }

    @PostMapping("/insert/fail")
    public String insertOrderFail(@RequestBody Orders orders) {
        try {
            getOrderService().insertFail(orders);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }

    @PostMapping("/update/success")
    public String updateOrderSuccess(@RequestBody Orders orders) {
        try {
            getOrderService().updateSuccess(orders);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }

    @PostMapping("/update/fail")
    public String updateOrderFail(@RequestBody Orders orders) {
        try {
            getOrderService().updateFail(orders);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }

    @PostMapping("/delete/success")
    public String deleteOrderSuccess(@RequestBody Orders orders) {
        try {
            getOrderService().deleteSuccess(orders);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }

    @PostMapping("/delete/fail")
    public String deleteOrderFail(@RequestBody Orders orders) {
        try {
            getOrderService().deleteFail(orders);
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
        return "success";
    }
}
