package io.seata.demo.order.dao.impl;

import io.seata.demo.order.dao.OrderMapper;
import io.seata.demo.order.entity.Orders;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MySQLOrderMapper extends OrderMapper {

    @Insert("INSERT INTO orders (id, storage_id, product_id, count) VALUES" +
        "(#{id, jdbcType=INTEGER}, #{storageId, jdbcType=INTEGER}, " +
        "#{productId, jdbcType=VARCHAR}, #{count, jdbcType=INTEGER})")
    void insert(Orders order);

    @Update("UPDATE orders SET count = #{count, jdbcType=INTEGER} " +
        "WHERE id = #{id, jdbcType=INTEGER} AND auto_id = #{autoId, jdbcType=INTEGER}")
    void update(Orders order);

    @Delete("DELETE FROM orders " +
        "WHERE id = #{id, jdbcType=INTEGER} AND auto_id = #{autoId, jdbcType=INTEGER}")
    void delete(Orders order);

    @Select("SELECT * FROM orders " +
        "WHERE id = #{id, jdbcType=INTEGER} AND auto_id = #{autoId, jdbcType=INTEGER}")
    Orders select(Orders order);
}
