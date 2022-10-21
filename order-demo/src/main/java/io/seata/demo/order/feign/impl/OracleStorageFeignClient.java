package io.seata.demo.order.feign.impl;

import io.seata.demo.order.config.FeignSupportConfig;
import io.seata.demo.order.entity.Orders;
import io.seata.demo.order.feign.StorageFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "storage", contextId = "storage-oracle", configuration = FeignSupportConfig.class)
public interface OracleStorageFeignClient extends StorageFeignClient {

    @PostMapping("/storage/oracle/update/success")
    void updateStorageSuccess(@RequestBody Orders orders, @RequestParam int used);

    @PostMapping("/storage/oracle/update/fail")
    void updateStorageFail(@RequestBody Orders orders, @RequestParam int used);
}
