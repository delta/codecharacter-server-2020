package delta.codecharacter.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class SocketService {

    @Value("/response/")
    private String socketDest;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public void sendMessage(Integer userId, String message) {
        simpMessagingTemplate.convertAndSend(socketDest + userId, message);
    }
}
