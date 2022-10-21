package io.seata.demo.order.feign.impl;

import io.seata.demo.order.config.FeignSupportConfig;
import io.seata.demo.order.entity.Orders;
import io.seata.demo.order.feign.StorageFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "storage", contextId = "storage-mysql", configuration = FeignSupportConfig.class)
public interface MySQLStorageFeignClient extends StorageFeignClient {

    @PostMapping("/storage/mysql/update/success")
    void updateStorageSuccess(@RequestBody Orders orders, @RequestParam int used);

    @PostMapping("/storage/mysql/update/fail")
    void updateStorageFail(@RequestBody Orders orders, @RequestParam int used);
}
