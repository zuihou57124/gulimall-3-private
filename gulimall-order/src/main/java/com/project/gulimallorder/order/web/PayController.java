package com.project.gulimallorder.order.web;

import com.alipay.api.AlipayApiException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.project.gulimallorder.order.config.AlipayTemplate;
import com.project.gulimallorder.order.constant.OrderEnum;
import com.project.gulimallorder.order.entity.OrderEntity;
import com.project.gulimallorder.order.service.OrderService;
import com.project.gulimallorder.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PayController {

    @Autowired
    AlipayTemplate alipayTemplate;

    @Autowired
    OrderService orderService;

    @RequestMapping(value = "/payOrder",produces = "text/html")
    @ResponseBody
    public String payOrder(String orderSn) throws AlipayApiException {

        PayVo payVo = orderService.getOrderPayInfo(orderSn);
        //跳转到支付页面前先检查订单状态，待付款状态才能支付
        OrderEntity orderEntity = orderService.getOne(new QueryWrapper<OrderEntity>()
                .eq("order_sn",orderSn));
        if(!orderEntity.getStatus().equals(OrderEnum.CREATE_NEW.getCode())){
            return "订单已过期，请重新操作";
        }
        String s = alipayTemplate.pay(payVo);

        return s;
    }

}
