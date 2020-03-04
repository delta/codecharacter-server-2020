package delta.codecharacter.server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Destination prefix to carry the messages back to the client.
        config.enableSimpleBroker("/socket/response");

        // Prefix for messages from client bound to methods annotated with @MessageMapping
        config.setApplicationDestinationPrefixes("/socket/request");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // End-Point for the socket to connect.
        registry.addEndpoint("/socket/connect").setAllowedOrigins("*").withSockJS();
    }

}
