package com.yupi.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yupi.springbootinit.constant.MqConstant;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @hundune~
 * @version1.0
 * 用于创建测试程序用到的交换机和队列（程序启动前执行一次）
 */
@Configuration
public class BiInitMain {

//    public static void main(String[] args) {
//        try{
//            ConnectionFactory factory = new ConnectionFactory();
//            factory.setHost("localhost");
//            Connection connection = factory.newConnection();
//            Channel channel = connection.createChannel();
//            Channel channel1 = connection.createChannel();
//
//            String EXCHANGE_NAME = BiMqConstant.BI_EXCHANGE_NAME;
//
//            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
//
//            String queueName1 = BiMqConstant.BI_QUEUE_NAME;
//            channel.queueDeclare(queueName1, true, false, false, null);
//
//            String EXCHANGE_DEAD_NAME = MqConstant.BI_DEAD_EXCHANGE_NAME;
//            String EXCHANGE_DEAD_QUEUE = MqConstant.BI_DEAD_QUEUE_NAME;
//
//            channel1.exchangeDeclare(EXCHANGE_DEAD_NAME, "direct");
//            Map<String,Object> arg = new HashMap<>();
//            arg.put("x-message-ttl",60000);
//            //绑定死信交换机
//            arg.put("x-dead-letter-exchange",EXCHANGE_DEAD_NAME);
//            arg.put("x-dead-letter-routing-key",EXCHANGE_DEAD_QUEUE);
//            channel1.queueDeclare(EXCHANGE_DEAD_QUEUE, false, false, false, arg);
//
//            channel.queueBind(queueName1, EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY);
//
//            channel1.queueBind(EXCHANGE_DEAD_QUEUE,EXCHANGE_DEAD_NAME,MqConstant.BI_DEAD_ROUTING_KEY);
//        }catch (Exception e){
//
//        }
//    }

    /**
     * 将死信队列和交换机声明
     */
    @Bean
    Queue BiDeadQueue(){
        return QueueBuilder.durable(MqConstant.BI_DEAD_QUEUE_NAME).build();
    }

    @Bean
    DirectExchange BiDeadExchange() {
        return new DirectExchange(MqConstant.BI_DEAD_EXCHANGE_NAME);
    }

    @Bean
    Binding BiDeadBinding(Queue BiDeadQueue, DirectExchange BiDeadExchange) {
        return BindingBuilder.bind(BiDeadQueue).to(BiDeadExchange).with(MqConstant.BI_DEAD_ROUTING_KEY);
    }

    /**
     * 将队列和交换机声明
     */
    @Bean
    Queue BiQueue(){
        //信息参数 设置TTL为1min
        Map<String,Object> arg = new HashMap<>();
        arg.put("x-message-ttl",60000);
        //绑定死信交换机
        arg.put("x-dead-letter-exchange",MqConstant.BI_DEAD_EXCHANGE_NAME);
        arg.put("x-dead-letter-routing-key",MqConstant.BI_DEAD_ROUTING_KEY);
        return QueueBuilder.durable(MqConstant.BI_QUEUE_NAME).withArguments(arg).build();
    }

    @Bean
    DirectExchange BiExchange() {
        return new DirectExchange(MqConstant.BI_EXCHANGE_NAME);
    }

    @Bean
    Binding BiBinding(Queue BiQueue, DirectExchange BiExchange) {
        return BindingBuilder.bind(BiQueue).to(BiExchange).with(MqConstant.BI_ROUTING_KEY);
    }

}
