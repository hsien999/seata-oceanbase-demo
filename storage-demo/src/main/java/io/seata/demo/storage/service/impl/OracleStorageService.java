package io.seata.demo.storage.service.impl;


import io.seata.demo.storage.dao.StorageMapper;
import io.seata.demo.storage.dao.impl.OracleStorageMapper;
import io.seata.demo.storage.service.StorageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class OracleStorageService extends StorageService {
    @Resource(type = OracleStorageMapper.class)
    public StorageMapper oracleStorageMapper;

    @Override
    public StorageMapper getStorageMapper() {
        return oracleStorageMapper;
    }
}
