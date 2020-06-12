package com.project.gulimallorder.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.project.gulimallorder.order.vo.OrderConfirmVo;
import com.project.gulimallorder.order.vo.OrderSubmitVo;
import com.project.gulimallorder.order.vo.SubmitOrderRespVo;
import io.renren.common.utils.PageUtils;
import com.project.gulimallorder.order.entity.OrderEntity;

import java.util.Map;

/**
 * 订单
 *
 * @author qcw
 * @email zuihou57124@gmail.com
 * @date 2020-04-29 17:16:42
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    OrderConfirmVo confirmOrder();

    SubmitOrderRespVo submitOrder(OrderSubmitVo orderSubmitVo);

    void closeOrder(OrderEntity orderEntity);
}

