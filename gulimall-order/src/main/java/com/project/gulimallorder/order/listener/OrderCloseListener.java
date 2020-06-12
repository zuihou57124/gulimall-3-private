package com.project.gulimallorder.order.listener;

import com.project.gulimallorder.order.entity.OrderEntity;
import com.project.gulimallorder.order.service.OrderService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    OrderService orderService;

    /**
     * @param orderEntity
     * @param message
     * @param channel
     *
     * 监听队列，判断是否需要解锁库存
     */
    @RabbitHandler
    public void releaseStockHdanler(OrderEntity orderEntity, Message message, Channel channel) throws IOException {
        System.out.println("收到关闭订单的消息...");
        try {
            orderService.closeOrder(orderEntity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            System.out.println("关闭成功");
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            System.out.println("关闭失败,重新入列");
        }

    }




}
