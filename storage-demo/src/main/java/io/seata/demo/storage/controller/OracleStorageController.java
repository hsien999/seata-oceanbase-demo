package io.seata.demo.storage.controller;

import io.seata.demo.storage.service.StorageService;
import io.seata.demo.storage.service.impl.OracleStorageService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/storage/oracle")
public class OracleStorageController extends BaseController {

    @Resource(type = OracleStorageService.class)
    private StorageService oracleStorageService;

    @Override
    public StorageService getStorageService() {
        return oracleStorageService;
    }
}
