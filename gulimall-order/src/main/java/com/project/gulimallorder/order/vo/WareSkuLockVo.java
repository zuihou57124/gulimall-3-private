package com.project.gulimallorder.order.vo;

import lombok.Data;

import java.util.List;

@Data
public class WareSkuLockVo {

    /**
     * 需要为哪个订单锁库存
     */
    private String orderSn;

    //需要锁的sku信息
    private List<OrderItemVo> locks;

}
