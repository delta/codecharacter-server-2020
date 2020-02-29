package delta.codecharacter.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SocketService {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void sendMessage(String dest, String message) {
        simpMessagingTemplate.convertAndSend(dest, message);
    }
}
