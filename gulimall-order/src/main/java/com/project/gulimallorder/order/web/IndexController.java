package com.project.gulimallorder.order.web;

import com.project.gulimallorder.order.feign.MemberFeignService;
import com.project.gulimallorder.order.service.OrderService;
import com.project.gulimallorder.order.vo.MemberAddressVo;
import com.project.gulimallorder.order.vo.OrderConfirmVo;
import com.project.gulimallorder.order.vo.OrderSubmitVo;
import com.project.gulimallorder.order.vo.SubmitOrderRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.PostConstruct;
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
    public String toTrade(Model model){
        OrderConfirmVo orderConfirmVo =  orderService.confirmOrder();
        model.addAttribute("orderConfirm",orderConfirmVo);
        return "confirm";
    }

    @PostMapping("/submitOrder")
    public String submitOrder(OrderSubmitVo orderSubmitVo){

        SubmitOrderRespVo submitOrderRespVo = orderService.submitOrder(orderSubmitVo);

        if (submitOrderRespVo.getCode()==0){
            //成功后跳到支付页

            return null;
        }
        //失败后跳到确认页面，重新确认订单信息
        System.out.println(orderSubmitVo.toString());

        return "redirect:http://order.gulimall.com/toTrade";
    }


}
