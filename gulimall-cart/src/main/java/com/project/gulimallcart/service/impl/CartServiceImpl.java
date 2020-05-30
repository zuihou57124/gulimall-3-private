package com.project.gulimallcart.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.project.gulimallcart.constant.CartConst;
import com.project.gulimallcart.feign.ProductFeignService;
import com.project.gulimallcart.interceptor.CartInterceptor;
import com.project.gulimallcart.service.CartService;
import com.project.gulimallcart.vo.Cart;
import com.project.gulimallcart.vo.CartItemVo;
import com.project.gulimallcart.vo.SkuInfoTo;
import com.project.gulimallcart.vo.UserInfoTo;
import io.renren.common.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    /**
     * 获取用户的购物车信息
     * @param userInfoTo
     * @return cartItemList
     */
    @Override
    public Cart getAllCartItem(UserInfoTo userInfoTo) {

        //如果已登录，要合并游客购物车
        BoundHashOperations<String, Object, Object> cartRedisOps = getCartRedisOps();
        Cart cart = new Cart();
        List<CartItemVo> cartItemListForUser = null;
        List<CartItemVo> cartItemListForTemp = null;
        if(userInfoTo.getUserId()==null){
            List<Object> values = cartRedisOps.values();
            cartItemListForUser = getCartItemFromRedis(cartRedisOps);
            cart.setItems(cartItemListForUser);
        }
        else {
            //已登录,合并购物车
            cartItemListForUser = getCartItemFromRedis(cartRedisOps);
            cartRedisOps = redisTemplate.boundHashOps(CartConst.CART_PREFIX+userInfoTo.getUserKey());
            cartItemListForTemp = getCartItemFromRedis(cartRedisOps);
            //商品如果存在，只增加数量
            List<Long> userCartSkuIds =  cartItemListForUser.stream().map(CartItemVo::getSkuId).collect(Collectors.toList());
            List<Long> tempCartSkuIds =  cartItemListForTemp.stream().map(CartItemVo::getSkuId).collect(Collectors.toList());

            List<CartItemVo> finalCartItemList = new ArrayList<>();
            finalCartItemList.addAll(cartItemListForUser);

            for (CartItemVo c1 : cartItemListForTemp) {

                if(userCartSkuIds.contains(c1.getSkuId())){
                    for (CartItemVo cartItemUser : cartItemListForUser) {
                        if(cartItemUser.getSkuId().equals(c1.getSkuId())){
                            c1.setCount(c1.getCount()+cartItemUser.getCount());
                            for (int i = 0; i < finalCartItemList.size(); i++) {
                                CartItemVo cartItemVo = finalCartItemList.get(i);
                                if(cartItemVo.getSkuId().equals(c1.getSkuId())){
                                    finalCartItemList.set(i,c1);
                                    break;
                                }
                            }
                        }
                    }
                }
                else {
                    finalCartItemList.add(c1);
                }


//                for (CartItemVo c2 : cartItemListForTemp) {
//                    if(c1.getSkuId().equals(c2.getSkuId())){
//                        c1.setCount(c1.getCount()+c2.getCount());
//                        finalCartItemList.add(c1);
//                    }
//                }
            }

            //cartItemListForUser.addAll(cartItemListForTemp);
            //cartItemListForUser.addAll(finalCartItemList);
            //合并后，删除临时购物车
            redisTemplate.delete(cartRedisOps.getKey());
            cart.setItems(finalCartItemList);
        }

        return cart;
    }

    private List<CartItemVo> getCartItemFromRedis(BoundHashOperations<String,Object,Object> cartRedisOps) {
        List<Object> values = cartRedisOps.values();
        List<CartItemVo> cartItemList= null;
        if(values!=null && values.size()>0){
            cartItemList = values.stream().map((item) -> {
                String json = (String) item;
                CartItemVo cartItemVo = JSONObject.parseObject(json, CartItemVo.class);
                return cartItemVo;
            }).collect(Collectors.toList());
        }
        if(cartItemList==null){
            return new ArrayList<>();
        }
        return cartItemList;
    }

    /**
     * 获取购物项信息
     * @param skuId
     * @return cartItem
     */
    @Override
    public CartItemVo getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartRedisOps = getCartRedisOps();
        String json = (String) cartRedisOps.get(skuId.toString());
        return JSONObject.parseObject(json, CartItemVo.class);
    }

    /**
     * 添加商品到购物车(基于redis)
     * @param skuId
     * @param num
     * @return 商品项
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public CartItemVo add(Long skuId, Integer num) throws ExecutionException, InterruptedException {
        BoundHashOperations<String, Object, Object> cartRedisOps = getCartRedisOps();

        //R r1 = productFeignService.info(skuId);
        //R r2 = productFeignService.list(skuId);

        //首先判断购物车是否已经包含该商品，如果有，只增加数量，没有则添加
        Object object = cartRedisOps.get(skuId.toString());
        if(object!=null){
            String jsonString = (String) object;
            CartItemVo cartItemVo = JSONObject.parseObject(jsonString,CartItemVo.class);
            cartItemVo.setCount(cartItemVo.getCount()+num);
            cartRedisOps.put(skuId.toString(),JSONObject.toJSONString(cartItemVo));
            return cartItemVo;
        }

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

            }else {
                System.out.println("远程调用商品服务获商品信息失败");
            }
        },executor);
        CompletableFuture<Void> skuSaleAttrsTask = CompletableFuture.runAsync(() -> {
            R r = productFeignService.list(skuId);
            if (r.getCode() == 0) {
                List<String> saleAttrsList = r.getData("saleAttrList", new TypeReference<List<String>>() {});
                //商品的属性信息需要远程调用商品服务
                cartItemVo.setSkuAttr(saleAttrsList);
            } else {
                System.out.println("远程调用商品服务获取sku销售信息失败");
            }
        },executor);

        CompletableFuture.allOf(skuInfoTask,skuSaleAttrsTask).get();
        //把商品信息保存在redis中
        String json = JSONObject.toJSONString(cartItemVo);
        cartRedisOps.put(skuId.toString(),json);

        //redisTemplate.opsForHash().put("cart:id-1","1",json);
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
