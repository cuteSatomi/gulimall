package com.zzx.gulimall.order;

import com.zzx.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessage() {
        // String msg = "Hello World!";
        OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
        reasonEntity.setCreateTime(new Date());
        reasonEntity.setId(1L);
        reasonEntity.setName("嘻嘻嘻");
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity);
        log.info("消息发送成功:{}", reasonEntity);
    }

    @Test
    public void createExchange() {
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("hello-java-exchange创建成功");
    }

    @Test
    public void createQueue() {
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("hello-java-queue创建成功");
    }

    @Test
    public void bindingExchange() {
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE, "hello-java-exchange", "hello.java", null);
        amqpAdmin.declareBinding(binding);
    }

}
