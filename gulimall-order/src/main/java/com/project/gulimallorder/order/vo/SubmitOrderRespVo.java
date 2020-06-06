package com.project.gulimallorder.order.vo;

import com.project.gulimallorder.order.entity.OrderEntity;
import lombok.Data;

/**
 * 订单提交返回vo
 */
public class SubmitOrderRespVo {


    /**
     * 0代表订单生成成功
     */
    private Integer code;

    private OrderEntity order;



    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }
}
