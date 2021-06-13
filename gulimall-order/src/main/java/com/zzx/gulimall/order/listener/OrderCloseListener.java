package com.zzx.gulimall.order.listener;

import com.rabbitmq.client.Channel;
import com.zzx.gulimall.order.entity.OrderEntity;
import com.zzx.gulimall.order.service.OrderService;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author zzx
 * @date 2021-06-13 13:08
 */

@Component
@RabbitListener(queues = "order.release.order.queue")
public class OrderCloseListener {

    @Autowired
    private OrderService orderService;

    @RabbitHandler
    public void listener(OrderEntity entity, Channel channel, Message message) throws IOException {
        System.out.println("收到过期的订单消息，准备关闭订单：" + entity.getOrderSn());

        try {
            // 关闭订单
            orderService.closeOrder(entity);
            // 成功手动ack
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            e.printStackTrace();
            // 失败回到队列
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }
}
