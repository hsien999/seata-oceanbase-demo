package io.seata.demo.storage.service.impl;

import io.seata.demo.storage.dao.StorageMapper;
import io.seata.demo.storage.dao.impl.MySQLStorageMapper;
import io.seata.demo.storage.service.StorageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class MySQLStorageService extends StorageService {
    @Resource(type = MySQLStorageMapper.class)
    public StorageMapper mySQLStorageMapper;

    @Override
    public StorageMapper getStorageMapper() {
        return mySQLStorageMapper;
    }
}

