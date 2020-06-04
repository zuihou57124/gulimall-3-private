package com.project.gulimallcart.controller;

import com.project.gulimallcart.constant.CartConst;
import com.project.gulimallcart.feign.ProductFeignService;
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
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpSession;
import javax.xml.ws.Action;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @Autowired
    ProductFeignService productFeignService;

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
     * @param skuId
     * @param action
     * @return
     * 改变商品数量
     */
    @RequestMapping("/changeItemCount")
    public String changeItemCount(@RequestParam("skuId") Long skuId,
                                  @RequestParam("action") String action){

        cartService.changeItemCount(skuId,action);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    /**
     * @param skuId
     * @return
     * 删除购物车选项
     */
    @RequestMapping("/delItem")
    public String delItem(@RequestParam("skuId") Long skuId){

        cartService.delItem(skuId);

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




    /**
     * @return 返回当前用户选中的购物车选项(feign interface)
     */
    @ResponseBody
    @RequestMapping("/currentUserCartItems")
    public List<CartItemVo> getCurrentUserCartItems(){
        UserInfoTo userInfoTo = CartInterceptor.threadLocal.get();
        Cart allCartItem = null;
        //如果未登录,直接返回null
        if(userInfoTo.getUserId()==null){
            return null;
        }

        try {
            allCartItem = cartService.getAllCartItem(userInfoTo);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        if(allCartItem!=null){
            return allCartItem.getItems().stream()
                    .filter(CartItemVo::getChecked)
                    .map((item)->{
                        //更新最新价格
                        BigDecimal price = productFeignService.getPrice(item.getSkuId());
                        item.setPrice(price);
                        return item;
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }

}
