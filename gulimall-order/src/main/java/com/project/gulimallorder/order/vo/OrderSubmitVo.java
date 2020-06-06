package com.project.gulimallorder.order.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户订单提交 vo
 */
@Data
public class OrderSubmitVo {

    //用户信息可以直接在里面取出
    //无需提交商品信息，可以从购物车再查询一遍

    private Long addrId;

    private Integer payType;

    private String orderToken;

    private BigDecimal payPrice;

    /**
     * 订单备注
     */
    private String note;


}
