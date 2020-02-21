package delta.codecharacter.server.service;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;


@Service
public class RabbitMqService {
    Logger LOG = Logger.getLogger(RabbitMqService.class.getName());

    private Channel channel;

    @Value("spring.rabbitmq.host")
    private String host;

    @Value("spring.rabbitmq.queue")
    private String queue;

    @SneakyThrows
    public void sendMessageToQueue(String message) {
        channel.basicPublish("", queue, null, message.getBytes(StandardCharsets.UTF_8));
        LOG.info("Sent " + message);
    }

    @SneakyThrows
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        Connection connection = connectionFactory.newConnection();
        channel = connection.createChannel();

        //Number of messages that can be processed at a time
        channel.basicQos(1);
        channel.queueDeclare(queue, true, false, false, null);
    }
}
