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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public CartItemVo add(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartRedisOps = getCartRedisOps();

        CartItemVo cartItemVo = new CartItemVo();
        CompletableFuture<Void> skuInfoTask = CompletableFuture.runAsync(() -> {
            R r = productFeignService.info(skuId);
            if(r.getCode()==0){
                SkuInfoTo skuInfo = r.getData("skuInfo", new TypeReference<SkuInfoTo>(){});
                //封装商品项信息
                cartItemVo.setChecked(false);
                cartItemVo.setCount(num);
                cartItemVo.setSkuId(skuId);
                cartItemVo.setImg(skuInfo.getSkuDefaultImg());
                cartItemVo.setTitle(skuInfo.getSkuTitle());
                cartItemVo.setPrice(skuInfo.getPrice());
                //把商品信息保存在redis中
                //cartRedisOps.put(skuInfo.getSkuId(),skuInfo);
            }else {
                System.out.println("远程调用商品服务获商品信息失败");
            }
        });
        CompletableFuture.runAsync(()->{
            R r = productFeignService.list(skuId);
            if(r.getCode()==0){
                List<String> saleAttrsList = r.getData("saleAttrsList", new TypeReference<List<String>>() {});
                //商品的属性信息需要远程调用商品服务
                cartItemVo.setSkuAttr(saleAttrsList);
            }
            else {
                System.out.println("远程调用商品服务获取sku销售信息失败");
            }
        });

        return cartItemVo;
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
