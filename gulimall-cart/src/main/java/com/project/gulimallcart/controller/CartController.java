package com.project.gulimallcart.controller;

import com.project.gulimallcart.constant.CartConst;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

@Controller
public class CartController {

    @RequestMapping("/cart.html")
    public String cartList(HttpSession session){
        Object user = session.getAttribute(CartConst.LOGIN_USER);
        if (user != null) {
            return "";
        }
        return "cartList";
    }

}
