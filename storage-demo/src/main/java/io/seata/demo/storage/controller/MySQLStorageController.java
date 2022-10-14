package io.seata.demo.storage.controller;

import io.seata.demo.storage.service.StorageService;
import io.seata.demo.storage.service.impl.MySQLStorageService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/storage/mysql")
public class MySQLStorageController extends BaseController {

    @Resource(type = MySQLStorageService.class)
    private StorageService mySQLStorageService;

    @Override
    protected StorageService getStorageService() {
        return mySQLStorageService;
    }
}
