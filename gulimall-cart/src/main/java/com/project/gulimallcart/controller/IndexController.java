package com.project.gulimallcart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class IndexController {

    @RequestMapping("/cart.html")
    public String cartList(){
        return "cartList";
    }

}
