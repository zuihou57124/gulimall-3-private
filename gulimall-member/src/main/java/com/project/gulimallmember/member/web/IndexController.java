package com.project.gulimallmember.member.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/memberOrder.html")
    public String orderListPage(){
        return "orderList";
    }

}
