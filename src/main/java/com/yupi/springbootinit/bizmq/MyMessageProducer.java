package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import io.swagger.v3.oas.annotations.headers.Header;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @hundune~
 * @version1.0
 */
@Component
public class MyMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String exchange,String routingKey,String message){
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }
}
