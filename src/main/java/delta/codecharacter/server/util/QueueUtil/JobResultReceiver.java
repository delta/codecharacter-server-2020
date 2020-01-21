package delta.codecharacter.server.util.QueueUtil;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;


@RabbitListener(queues = "codechar20replies")
public class JobResultReceiver {

    @RabbitHandler
    public void receive(String message){
        //TODO
        System.out.println(message.toString());
    }
}


