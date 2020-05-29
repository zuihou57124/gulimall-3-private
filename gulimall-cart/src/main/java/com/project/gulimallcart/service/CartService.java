package com.project.gulimallcart.service;

import com.project.gulimallcart.vo.CartItemVo;

import java.util.concurrent.ExecutionException;

public interface CartService {

    CartItemVo add(Long skuId, Integer num) throws ExecutionException, InterruptedException;

}
