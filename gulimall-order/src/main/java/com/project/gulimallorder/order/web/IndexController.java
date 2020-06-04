package com.project.gulimallorder.order.web;

import com.project.gulimallorder.order.feign.MemberFeignService;
import com.project.gulimallorder.order.service.OrderService;
import com.project.gulimallorder.order.vo.MemberAddressVo;
import com.project.gulimallorder.order.vo.OrderConfirmVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    OrderService orderService;

    @RequestMapping("/{page}.html")
    public String getPage(@PathVariable("page") String page){
        return page;
    }

    @RequestMapping("/toTrade")
    public String toTrade(){
        OrderConfirmVo orderConfirmVo =  orderService.confirmOrder();
        return "confirm";
    }


}
