package com.project.gulimallcart.service;

import com.project.gulimallcart.vo.Cart;
import com.project.gulimallcart.vo.CartItemVo;
import com.project.gulimallcart.vo.UserInfoTo;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartService {

    CartItemVo add(Long skuId, Integer num) throws ExecutionException, InterruptedException;

    CartItemVo getCartItem(Long skuId);

    Cart getAllCartItem(UserInfoTo userInfoTo) throws ExecutionException, InterruptedException;

    void clearCart(String cartKey);

    void checkCartItem(Long skuId, Boolean checked);
}
