package com.project.gulimallorder.order.config;

import com.project.gulimallorder.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;
import io.swagger.annotations.Authorization;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;

@Configuration
public class MyRabbitmqConfig {

    //首次创建队列时，要监听任意一个队列，spring才会自动创建


    /*@RabbitListener(queues = "order.release.order.queue")
    public void orderListener(OrderEntity orderEntity, Channel channel, Message message) throws IOException {
        System.out.println("订单即将过期："+orderEntity.getOrderSn());
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
    }*/

    @Bean
    public Queue orderDelayQueue(){
        HashMap<String, Object> arg = new HashMap<>();
        arg.put("x-dead-letter-exchange","order-event-exchange");
        arg.put("x-dead-letter-routing-key","order.release.queue");
        arg.put("x-message-ttl",60000*5);

        return new Queue("order.delay.queue",true,false,false,arg);
    }

    @Bean
    public Queue orderReleaseOderQueue(){

        return new Queue("order.release.order.queue",true,false,false);
    }

    @Bean
    public Exchange orderEventExchange(){

        return new TopicExchange("order-event-exchange",true,false);
    }

    @Bean
    public Binding orderCreateOrderBinding(){
       return new Binding("order.delay.queue",
               Binding.DestinationType.QUEUE,
               "order-event-exchange",
               "order.create.queue",null);
    }

    @Bean
    public Binding orderReleaseOrderBinding(){
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.queue",null);
    }

    /**
     * @return
     * 订单直接和库存队列绑定，订单主动释放，库存直接解锁
     */
    @Bean
    public Binding orderReleaseOtherBinding(){
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",null);
    }


}
