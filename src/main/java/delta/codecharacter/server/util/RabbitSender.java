package delta.codecharacter.server.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class RabbitSender {

    private static Logger LOG = Logger.getLogger(RabbitSender.class.getName());

    /**
     * Add a message to the message queue
     *
     * @param message Message to added to the queue
     */
    @SneakyThrows
    public static void sendMessage(String message, String host, String queue) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();

        //Number of messages that can be processed at a time
        channel.basicQos(1);
        channel.queueDeclare(queue, true, false, false, null);
        channel.basicPublish("", queue, null, message.getBytes(StandardCharsets.UTF_8));
        LOG.info("Sent " + message);
    }
}
