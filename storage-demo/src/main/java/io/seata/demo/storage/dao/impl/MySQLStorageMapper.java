package io.seata.demo.storage.dao.impl;

import io.seata.demo.storage.dao.StorageMapper;
import io.seata.demo.storage.entity.Orders;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface MySQLStorageMapper extends StorageMapper {
    @Update("UPDATE storage SET used = used + #{used, jdbcType=INTEGER}"
        + " WHERE id = #{order.storageId, jdbcType=INTEGER} AND product_id = #{order.productId, jdbcType=VARCHAR}")
    void update(@Param("order") Orders order, @Param("used") int used);
}
