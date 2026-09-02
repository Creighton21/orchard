package com.creightoncornelison.orchard.adapter.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.nio.charset.StandardCharsets;

public class RabbitMqConnection {
    /*
    * This class is responsible for establishing and managing a connection to a RabbitMQ server.
    * It provides methods for connecting, disconnecting, and publishing messages to a RabbitMQ queue.
    * */

    /*
    connect
    declare test queue
    publish "hello"
    consume "hello"
     */

    private final static String QUEUE_NAME = "hello";

    public static void publish() throws Exception {
        // Setup the connection factory
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost"); // Change if your broker is remote

        // Automatically close connection and channel using try-with-resources
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // queueDeclare(queueName, durable, exclusive, autoDelete, arguments)
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            String message = "Hello from Native Java Client!";
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + message + "'");
        }
    }


    public static void consume() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        // Connection and Channel remain open for continuous listening
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        // Callback mechanism to handle incoming messages
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };

        // basicConsume(queueName, autoAck, deliverCallback, cancelCallback)
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {
        });
    }
}
