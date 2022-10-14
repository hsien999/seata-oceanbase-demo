package io.seata.demo.storage.dao.impl;

import io.seata.demo.storage.dao.StorageMapper;
import io.seata.demo.storage.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface OracleStorageMapper extends StorageMapper {

    @Update("UPDATE STORAGE SET USED = USED + #{used, jdbcType=INTEGER}"
        + " WHERE ID = #{order.storageId, jdbcType=INTEGER} AND PRODUCT_ID = #{order.productId, jdbcType=VARCHAR}")
    void update(@Param("order") Orders order, @Param("used") int used);
}
