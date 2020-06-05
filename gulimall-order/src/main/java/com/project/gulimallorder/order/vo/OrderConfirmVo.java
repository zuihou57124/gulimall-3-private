package com.project.gulimallorder.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author root
 */
@Data
public class OrderConfirmVo {

    /**
     * 订单令牌(防止重复下单)
     */
    private String orderToken;

    /**
     * 收货地址
     */
    List<MemberAddressVo> memberAddressVoList;

    /**
     * 订单项
     */
    List<OrderItemVo> orderItemVoList;

    /**
     * 商品总数
     */
    Integer num;

    public Integer getNum(){
        int num = 0;
        for (OrderItemVo orderItemVo : orderItemVoList) {
            num = num + orderItemVo.getCount();
        }
        return num;
    }


    /*发票记录*/

    /**
     * 会员积分
     */
    private Integer integration;

    /**
     * 订单总金额
     */
    private BigDecimal total;

    /**
     * 应付金额
     */
    private BigDecimal payPrice;

    public BigDecimal getTotal(){
        BigDecimal total = new BigDecimal(0);
        if(orderItemVoList!=null){
            for (OrderItemVo orderItemVo : orderItemVoList) {
                BigDecimal itemPrice = orderItemVo.getPrice().multiply(new BigDecimal(orderItemVo.getCount()));
                total = total.add(itemPrice);
            }
        }

        return total;
    }

    public BigDecimal getPayPrice(){
        return payPrice;
    }

}
