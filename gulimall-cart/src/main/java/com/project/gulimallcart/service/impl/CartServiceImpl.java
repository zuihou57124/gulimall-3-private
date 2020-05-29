package com.project.gulimallcart.service.impl;

import com.project.gulimallcart.interceptor.CartInterceptor;
import com.project.gulimallcart.service.CartService;
import com.project.gulimallcart.vo.CartItemVo;
import com.project.gulimallcart.vo.UserInfoTo;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {
    @Override
    public CartItemVo add(Long skuId, Integer num) {
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        String cartKey = "";

        if(userInfo.getUserId()==null){
            cartKey = userInfo.getUserKey();
        }

        cartKey = userInfo.getUserId().toString();
        return null;
    }
}
