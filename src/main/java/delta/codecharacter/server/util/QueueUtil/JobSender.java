package delta.codecharacter.server.util.QueueUtil;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;

public class JobSender {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Queue jobQueue;

    public void send(String message) {
        this.rabbitTemplate.convertAndSend(jobQueue.getName(), message);
    }

}
