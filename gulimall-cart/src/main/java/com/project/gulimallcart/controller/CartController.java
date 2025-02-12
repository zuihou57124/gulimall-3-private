package com.project.gulimallcart.controller;

import com.alibaba.fastjson.TypeReference;
import com.project.gulimallcart.constant.CartConst;
import com.project.gulimallcart.feign.ProductFeignService;
import com.project.gulimallcart.feign.WareFeignService;
import com.project.gulimallcart.interceptor.CartInterceptor;
import com.project.gulimallcart.service.CartService;
import com.project.gulimallcart.vo.Cart;
import com.project.gulimallcart.vo.CartItemVo;
import com.project.gulimallcart.vo.UserInfoTo;
import io.renren.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Controller
public class CartController {

    @Autowired
    CartService cartService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    ThreadPoolExecutor executor;

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
            List<CartItemVo> collect = allCartItem.getItems().stream()
                    .filter(CartItemVo::getChecked)
                    .map((item) -> {
                        //远程更新最新价格

                        BigDecimal price = productFeignService.getPrice(item.getSkuId());
                        item.setPrice(price);

                        //远程查询商品是否有货
                        R r = wareFeignService.hasStock(item.getSkuId());
                        Boolean stock = r.getData("data",new TypeReference<Boolean>(){});
                        item.setHasStock(stock);

                        //远程查询商品重量
                        R r2 = productFeignService.spuWeight(item.getSpuId());
                        BigDecimal weight = r2.getData("data", new TypeReference<BigDecimal>() {});
                        item.setWeight(weight);

                        return item;
                    })
                    .collect(Collectors.toList());

            return collect;

        }
        return null;
    }

}
