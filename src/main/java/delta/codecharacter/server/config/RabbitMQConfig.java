package delta.codecharacter.server.config;

import delta.codecharacter.server.util.QueueUtil.JobSender;
import delta.codecharacter.server.util.QueueUtil.JobResultReceiver;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.jobqueue}")
    private String jobQueue;
    @Bean
    public Queue jobQueue() {
        return new Queue(jobQueue);
    }


    @Bean
    public JobSender jobSender() {
        return new JobSender();
    }

    @Bean
    public JobResultReceiver JobResultReceiver() {
        return new JobResultReceiver();
    }

}
