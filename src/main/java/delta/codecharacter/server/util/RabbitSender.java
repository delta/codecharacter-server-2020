package delta.codecharacter.server.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class RabbitSender {

    private static Logger LOG = Logger.getLogger(RabbitSender.class.getName());

    @Autowired
    private static Environment environment;

    private static String jobQueue = environment.getProperty("spring.rabbitmq.queue");

    private static String host = environment.getProperty("spring.rabbitmq.host");

    /**
     * Add a message to the message queue
     *
     * @param message Message to added to the queue
     */
    @SneakyThrows
    public static void sendMessage(String message) {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        boolean durable = true;
        int prefetchCount = 1;

        //Number of messages that can be processed at a time
        channel.basicQos(prefetchCount);
        channel.queueDeclare(jobQueue, durable, false, false, null);
        channel.basicPublish("", jobQueue, null, message.getBytes(StandardCharsets.UTF_8));
        LOG.info("Sent " + message);
    }
}
