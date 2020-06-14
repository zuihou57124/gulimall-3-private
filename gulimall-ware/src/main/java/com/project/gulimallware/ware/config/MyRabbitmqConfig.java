package com.project.gulimallware.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class MyRabbitmqConfig {

/*    @RabbitListener(queues = "stock.release.stock.queue")
    public void orderListener(){
        System.out.println("监听中...");
    }*/

    @Bean
    public Queue stockDelayQueue(){
        HashMap<String, Object> arg = new HashMap<>();
        arg.put("x-dead-letter-exchange","stock-event-exchange");
        arg.put("x-dead-letter-routing-key","stock.release.queue");
        arg.put("x-message-ttl",60000*6);

        return new Queue("stock.delay.queue",true,false,false,arg);
    }

    @Bean
    public Queue stockReleaseStockQueue(){

        return new Queue("stock.release.stock.queue",true,false,false);
    }

    @Bean
    public Exchange stockEventExchange(){

        return new TopicExchange("stock-event-exchange",true,false);
    }

    @Bean
    public Binding stockLockBinding(){

        //关于锁定和解锁的路由键格式不同的原因：
        // 锁库存只需
        // 解锁要...

        return new Binding("stock.delay.queue",
               Binding.DestinationType.QUEUE,
               "stock-event-exchange",
               "stock.lock",null);
    }

    @Bean
    public Binding stockReleaseBinding(){
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "stock-event-exchange",
                "stock.release.#",null);
    }

}
