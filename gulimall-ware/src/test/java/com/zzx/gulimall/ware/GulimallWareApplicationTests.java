package com.zzx.gulimall.ware;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallWareApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Test
    public void contextLoads() {
        TopicExchange topicExchange = new TopicExchange("stock-event-exchange", true, false);
        amqpAdmin.declareExchange(topicExchange);

        Queue queue = new Queue("stock.release.stock.queue", true, false, false);
        amqpAdmin.declareQueue(queue);

        Map<String, Object> arguments = new HashMap<>();
        // 指定死信交换机
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        // 指定死信路由键
        arguments.put("x-dead-letter-routing-key", "stock.release");
        // 队列的ttl
        arguments.put("x-message-ttl", 120000);
        queue = new Queue("stock.delay.queue", true, false, false, arguments);
        amqpAdmin.declareQueue(queue);

        Binding binding = new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange", "stock.release.#", null);
        amqpAdmin.declareBinding(binding);

        binding = new Binding("stock.delay.queue", Binding.DestinationType.QUEUE,
                "stock-event-exchange", "stock.locked", null);
        amqpAdmin.declareBinding(binding);
    }

}
