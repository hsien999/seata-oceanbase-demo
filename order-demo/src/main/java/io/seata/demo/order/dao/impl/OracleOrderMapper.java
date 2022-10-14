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
public interface OracleOrderMapper extends OrderMapper {

    @Insert("INSERT INTO ORDERS (ID, AUTO_ID, STORAGE_ID, PRODUCT_ID, COUNT) VALUES" +
        "(#{id, jdbcType=INTEGER}, ORDER_SEQ.NEXTVAL, #{storageId, jdbcType=INTEGER}" +
        ", #{productId, jdbcType=VARCHAR}, #{count, jdbcType=INTEGER})")
    void insert(Orders order);

    @Update("UPDATE ORDERS SET COUNT = #{count, jdbcType=INTEGER} " +
        "WHERE ID = #{id, jdbcType=INTEGER} AND AUTO_ID = #{autoId, jdbcType=INTEGER}")
    void update(Orders order);

    @Delete("DELETE FROM ORDERS " +
        "WHERE ID = #{id, jdbcType=INTEGER} AND AUTO_ID = #{autoId, jdbcType=INTEGER}")
    void delete(Orders order);

    @Select("SELECT * FROM ORDERS " +
        "WHERE ID = #{id, jdbcType=INTEGER} AND AUTO_ID = #{autoId, jdbcType=INTEGER}")
    Orders select(Orders order);
}
