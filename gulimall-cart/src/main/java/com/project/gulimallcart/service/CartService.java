package com.project.gulimallcart.service;

import com.project.gulimallcart.vo.CartItemVo;

public interface CartService {

    CartItemVo add(Long skuId, Integer num);

}
