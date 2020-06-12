package com.project.gulimallware.ware.listener;

import com.project.gulimallware.ware.service.WareSkuService;
import com.project.gulimallware.ware.to.mq.StockLockTo;
import com.project.gulimallware.ware.vo.OrderVo;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RabbitListener(queues = "stock.release.stock.queue")
public class StockReleaseListener {

    @Autowired
    WareSkuService wareSkuService;

    /**
     * @param stockLockTo
     * @param message
     * @param channel
     *
     * 监听队列，判断是否需要解锁库存
     */
    @RabbitHandler
    public void releaseStockHdanler(StockLockTo stockLockTo, Message message, Channel channel) throws IOException {
        System.out.println("订单关闭，解锁库存...");
        try {
            wareSkuService.unLock(stockLockTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            System.out.println("解锁成功");
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            System.out.println("解锁失败,重新入列");
        }

    }

    /**
     * 订单主动关闭，库存解锁
     */
    @RabbitHandler
    public void orderCloseHdanler(OrderVo orderVo, Message message, Channel channel) throws IOException {
        System.out.println("收到解锁库存的消息...");
        try {
            wareSkuService.unLock(orderVo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            System.out.println("解锁成功");
        } catch (Exception e) {
            channel.basicReject(message.getMessageProperties().getDeliveryTag(),true);
            System.out.println("解锁失败,重新入列");
        }

    }


}
