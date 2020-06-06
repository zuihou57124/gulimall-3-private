package com.project.gulimallorder.order.service.impl;

import com.project.gulimallorder.order.constant.OrderConst;
import com.project.gulimallorder.order.interceptor.OrderInterceptor;
import com.project.gulimallorder.order.feign.CartFeignService;
import com.project.gulimallorder.order.feign.MemberFeignService;
import com.project.gulimallorder.order.vo.*;
import io.renren.common.vo.MemberRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.renren.common.utils.PageUtils;
import io.renren.common.utils.Query;

import com.project.gulimallorder.order.dao.OrderDao;
import com.project.gulimallorder.order.entity.OrderEntity;
import com.project.gulimallorder.order.service.OrderService;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    CartFeignService cartFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 获取用户确认订单的信息
     */
    @Override
    public OrderConfirmVo confirmOrder() {

        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        MemberRespVo memberRespVo = OrderInterceptor.threadLocal.get();

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        CompletableFuture<Void> addressTask = CompletableFuture.runAsync(() -> {
            //远程查询用户地址
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> addressList = memberFeignService.getAddressList(memberRespVo.getId());
            orderConfirmVo.setMemberAddressVoList(addressList);
        },executor);

        CompletableFuture<Void> currentUserCartItemsTask = CompletableFuture.runAsync(() -> {
            //远程查询购物车被选中的选项
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> currentUserCartItems = cartFeignService.getCurrentUserCartItems();
            orderConfirmVo.setOrderItemVoList(currentUserCartItems);
        },executor);

        //查询用户积分信息
        orderConfirmVo.setIntegration(memberRespVo.getIntegration());

        //防重令牌
        String uuid = UUID.randomUUID().toString().replace("-", "");
        orderConfirmVo.setOrderToken(uuid);
        redisTemplate.opsForValue().set(OrderConst.USER_ORDER_TOKEN_PRIFIX+memberRespVo.getId(),uuid,20, TimeUnit.MINUTES);

        try {
            CompletableFuture.allOf(addressTask,currentUserCartItemsTask).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return orderConfirmVo;
    }

    @Override
    public SubmitOrderRespVo submitOrder(OrderSubmitVo orderSubmitVo) {

        SubmitOrderRespVo resp = new SubmitOrderRespVo();
        //需要实现：下单，验证令牌，验证价格，锁库存
        String script = "if redis.call('get',KEYS[1])==ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        MemberRespVo memberRespVo = OrderInterceptor.threadLocal.get();
        //redis存储的token
        String redisToken = redisTemplate.opsForValue().get(OrderConst.USER_ORDER_TOKEN_PRIFIX + memberRespVo.getId());
        //页面传过来的token
        String orderToken = orderSubmitVo.getOrderToken();
        //令牌的对比和删除必须是原子性的
        Long result = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList(OrderConst.USER_ORDER_TOKEN_PRIFIX + memberRespVo.getId()), orderToken);
        if(result==1L){
            //通过令牌验证

            return resp;
        }

        resp.setCode(500);
        /*if(orderToken!=null && orderToken.equals(redisToken)){
            //令牌通过验证，首先删除服务端的令牌
            redisTemplate.delete(OrderConst.USER_ORDER_TOKEN_PRIFIX + memberRespVo.getId());
        }*/

        return resp;
    }

}