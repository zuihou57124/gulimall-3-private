package com.project.gulimallcart.controller;

import com.project.gulimallcart.constant.CartConst;
import com.project.gulimallcart.interceptor.CartInterceptor;
import com.project.gulimallcart.service.CartService;
import com.project.gulimallcart.vo.Cart;
import com.project.gulimallcart.vo.CartItemVo;
import com.project.gulimallcart.vo.UserInfoTo;
import io.swagger.models.auth.In;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.xml.ws.Action;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @RequestMapping("/cart.html")
    public String cartList(HttpSession session,Model model) throws ExecutionException, InterruptedException {

        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();

        Object user = session.getAttribute(CartConst.LOGIN_USER);
        if (user != null) {
            //return "";
        }

        Cart cart = cartService.getAllCartItem(userInfoTo);
        model.addAttribute("cart",cart);

        return "cartList";
    }

    @RequestMapping("/add")
    public String add(@RequestParam("skuId") Long skuId,@RequestParam("num") Integer num,
                    Model model) throws ExecutionException, InterruptedException {
        CartItemVo cartItemVo = cartService.add(skuId,num);
        //model.addAttribute("skuId",cartItemVo.getSkuId());
        return "redirect:http://cart.gulimall.com/success.html"+"?skuId="+skuId.toString();
    }

    /**
     * @param checked
     * @param skuId
     * @return
     * 改变商品选项状态（是否选中）
     */
    @RequestMapping("/checkCartItem")
    public String checkCartItem(@RequestParam("checked") Boolean checked,
                                @RequestParam("skuId") Long skuId){
        cartService.checkCartItem(skuId,checked);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * 全选购物项
     */
    @RequestMapping("/checkAllCartItem")
    public String checkAllCartItem(@RequestParam("checked") Boolean checked){
        cartService.checkCartItem(null,checked);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @RequestMapping("/success.html")
    public String successPage(@RequestParam("skuId") Long skuId,Model model){
        CartItemVo cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item",cartItem);
        return "success";
    }



}
