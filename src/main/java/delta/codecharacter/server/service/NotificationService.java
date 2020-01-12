package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.request.AddNotificationRequest;
import delta.codecharacter.server.controller.request.PrivateNotificationResponse;
import delta.codecharacter.server.model.Notification;
import delta.codecharacter.server.repository.NotificationRepository;
import delta.codecharacter.server.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class NotificationService {

    private final Logger LOG = Logger.getLogger(NotificationService.class.getName());

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void addNotification(@NotNull AddNotificationRequest notification) {
        Integer notificationId = getMaxNotificationId() + 1;
        Notification newNotification = Notification.builder()
                .id(notificationId)
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .build();

        System.out.println("New notification: " + newNotification);
        notificationRepository.save(newNotification);
    }

    public PrivateNotificationResponse readNotification(Integer notificationId, Integer userId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isEmpty()) {
            return null;
        }
        Notification notification = optionalNotification.get();
        notification.setIsRead(true);
        PrivateNotificationResponse response = PrivateNotificationResponse.builder()
                .title(notification.getTitle())
                .content(notification.getContent())
                .isRead(true)
                .build();
        return response;
    }

    public List<PrivateNotificationResponse> getAllNotificationsByUserId(Integer userId) {
        List<Notification> notifications = notificationRepository.findAllByUserIdEquals(userId);
        List<PrivateNotificationResponse> response = new ArrayList<>();

        for (var notification : notifications) {
            response.add(PrivateNotificationResponse.builder()
                    .title(notification.getTitle())
                    .content(notification.getContent())
                    .type(notification.getType())
                    .isRead(notification.getIsRead())
                    .build());
        }
        return response;
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
