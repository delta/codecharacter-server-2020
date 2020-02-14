package delta.codecharacter.server.util;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

public class RabbitSender {

    @Autowired
    private static Environment environment;

    private static String jobQueue = "codechar";

    private static String host = "localhost";

    private static Logger LOG = Logger.getLogger(RabbitSender.class.getName());

    public static void sendMessage(String message) throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        boolean durable = true;
        int prefetchCount = 1;
        channel.basicQos(prefetchCount);
        channel.queueDeclare(jobQueue, durable, false, false, null);
        channel.basicPublish("", jobQueue, null, message.getBytes(StandardCharsets.UTF_8));
        LOG.info("Sent " + message);
    }
}
