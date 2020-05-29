package com.project.gulimallcart.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.project.gulimallcart.constant.CartConst;
import com.project.gulimallcart.feign.ProductFeignService;
import com.project.gulimallcart.interceptor.CartInterceptor;
import com.project.gulimallcart.service.CartService;
import com.project.gulimallcart.vo.CartItemVo;
import com.project.gulimallcart.vo.SkuInfoTo;
import com.project.gulimallcart.vo.UserInfoTo;
import io.renren.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Override
    public CartItemVo add(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartRedisOps = getCartRedisOps();
        R r = productFeignService.info(skuId);
        if(r.getCode()==0){
            SkuInfoTo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoTo>() {});
            cartRedisOps.put(skuInfo.getSkuId(),skuInfo);
        }else {
            System.out.println("远程调用商品服务失败");
        }

        return null;
    }

    /**
     * @return 返回购物车的redis操作
     */
    private BoundHashOperations<String,Object,Object> getCartRedisOps() {
        UserInfoTo userInfo = CartInterceptor.threadLocal.get();
        String cartKey = "";
        if(userInfo.getUserId()==null){
            cartKey = CartConst.CART_PREFIX+userInfo.getUserKey();
        }else {
            cartKey = CartConst.CART_PREFIX+userInfo.getUserId().toString();
        }
        BoundHashOperations<String,Object,Object> operations = redisTemplate.boundHashOps(cartKey);
        return operations;
    }
}
