package com.yupi.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.Scanner;

public class TopicProducer {

    private static final String EXCHANGE_NAME = "topic-exchange";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.exchangeDeclare(EXCHANGE_NAME, "topic");


            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String mu = scanner.nextLine();
                String[] res = mu.split(" ");
                if (res.length < 1) {
                    continue;
                }
                String message = res[0];
                String route = res[1];
                channel.basicPublish(EXCHANGE_NAME, route, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "'");
            }
        }
        //..
    }
}