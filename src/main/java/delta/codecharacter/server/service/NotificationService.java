package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.NotificationController;
import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.request.AddNotificationRequest;
import delta.codecharacter.server.model.Notification;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.NotificationRepository;
import delta.codecharacter.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class NotificationService {

    private final Logger LOG = Logger.getLogger(NotificationController.class.getName());

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void addNotification(@NotNull AddNotificationRequest notification) {
        Integer notificationId = getMaxNotificationId() + 1;
        User user = userRepository.findFirstById(notification.getUserId());

        if(user != null) {
            Notification newNotification = Notification.builder()
                    .id(notificationId)
                    .userId(notification.getUserId())
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .type(notification.getType())
                    .build();

            notificationRepository.save(newNotification);
        }
    }

    private Integer getMaxNotificationId() {
        Notification notification = notificationRepository.findFirstByOrderByIdDesc();
        System.out.println(notification);
        if (notification == null) {
            return 1;
        }
        return notification.getId();
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
