package com.project.gulimallware.ware.vo;

import lombok.Data;

@Data
public class LockStockResult {

    /**
     * 需要锁库存的skuId
     */
    private Long skuId;

    /**
     * 数量
     */
    private Integer num;

    /**
     * 是否成功
     */
    private Boolean status;

}
