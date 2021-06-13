package com.zzx.gulimall.ware.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zzx
 * @date 2021-06-08 21:09
 */
@Configuration
public class MyRabbitMqConfig {

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /*@RabbitListener(queues = "stock.release.stock.queue")
    public void listener(Message message) {
    }*/

    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }

    @Bean
    public Queue stockReleaseStockQueue() {
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    @Bean
    public Queue stockDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        // 指定死信交换机
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        // 指定死信路由键
        arguments.put("x-dead-letter-routing-key", "stock.release");
        // 队列的ttl
        arguments.put("x-message-ttl", 120000);
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    @Bean
    public Binding stockReleaseBinding(){
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange","stock.release.#",null);
    }

    @Bean
    public Binding stockLockedBinding(){
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange","stock.locked",null);
    }
}
