package io.seata.demo.storage.controller;

import io.seata.demo.storage.entity.Orders;
import io.seata.demo.storage.service.StorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public abstract class BaseController {
    protected abstract StorageService getStorageService();

    @PostMapping("/update/success")
    public String updateOracleStorageSuccess(@RequestBody Orders orders, @RequestParam int used) {
        getStorageService().updateSuccess(orders, used);
        return "success";
    }

    @PostMapping("/update/fail")
    public String updateOracleStorageFail(@RequestBody Orders orders, @RequestParam int used) {
        getStorageService().updateFail(orders, used);
        return "success";
    }
}
