package com.project.gulimallorder.order.web;

import com.alipay.api.AlipayApiException;
import com.project.gulimallorder.order.config.AlipayTemplate;
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
        String s = alipayTemplate.pay(payVo);

        return s;
    }

}
