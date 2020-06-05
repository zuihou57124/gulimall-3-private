package com.project.gulimallorder.order.service.impl;

import com.project.gulimallorder.order.interceptor.OrderInterceptor;
import com.project.gulimallorder.order.feign.CartFeignService;
import com.project.gulimallorder.order.feign.MemberFeignService;
import com.project.gulimallorder.order.vo.MemberAddressVo;
import com.project.gulimallorder.order.vo.OrderConfirmVo;
import com.project.gulimallorder.order.vo.OrderItemVo;
import io.renren.common.vo.MemberRespVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

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

        try {
            CompletableFuture.allOf(addressTask,currentUserCartItemsTask).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return orderConfirmVo;
    }

}