package io.seata.demo.storage.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Orders {
    private Integer id;

    private Integer autoId;

    private Integer storageId;

    private String productId;

    private Integer count;

    private Date addTime;

    private Date lastUpdateTime;
}

