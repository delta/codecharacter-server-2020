package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.request.Notification.CreateNotificationRequest;
import delta.codecharacter.server.model.Notification;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.NotificationRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.enums.Type;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;
import java.util.logging.Logger;

@Service
public class NotificationService {

    private final Logger LOG = Logger.getLogger(NotificationService.class.getName());

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @SneakyThrows
    public Notification createNotification(@NotNull CreateNotificationRequest createNotificationRequest) {
        Integer notificationId = getMaxNotificationId() + 1;
        Notification notification = Notification.builder()
                .id(notificationId)
                .userId(createNotificationRequest.getUserId())
                .title(createNotificationRequest.getTitle())
                .content(createNotificationRequest.getContent())
                .type(createNotificationRequest.getType())
                .build();

        notificationRepository.save(notification);

        return notification;
    }

    @SneakyThrows
    public void setIsReadNotificationById(@NotNull Integer notificationId) {
        Notification notification = notificationRepository.findFirstById(notificationId);
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @SneakyThrows
    public void deleteNotificationById(@NotNull Integer notificationId) {
        Notification notification = notificationRepository.findFirstById(notificationId);
        notificationRepository.delete(notification);
    }

    @SneakyThrows
    public Page<Notification> getAllNotificationsByTypeAndUserId(@NotNull Type type,
                                                                 @NotNull Integer userId,
                                                                 @NotNull int pageNumber,
                                                                 @NotNull int size) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        return notificationRepository.findAllByTypeAndUserId(type, userId, pageable);
    }

    @SneakyThrows
    public void deleteNotificationsByTypeAndUserId(@NotNull Type type, @NotNull Integer userId) {
        List<Notification> notifications = notificationRepository.findAllByTypeAndUserId(type, userId);
        notificationRepository.deleteAll(notifications);
    }

    @SneakyThrows
    public Page<Notification> getAllNotificationsByUserId(@NotNull Integer userId,
                                                        @NotNull @Positive int pageNumber,
                                                        @NotNull @PositiveOrZero int size) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        return notificationRepository.findAllByUserIdOrderByIdDesc(userId, pageable);
    }

    @SneakyThrows
    public Page<Notification> getAllUnreadNotificationsByUserId(@NotNull Integer userId,
                                                                @NotNull @Positive int pageNumber,
                                                                @NotNull @PositiveOrZero int size) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        return notificationRepository.findAllByUserIdAndIsReadFalseOrderByIdDesc(userId, pageable);
    }

    @SneakyThrows
    public boolean checkNotificationAccess(@NotNull Integer userId, @NotNull Integer notificationId) {
        Notification notification = notificationRepository.findFirstById(notificationId);
        User user = userRepository.findByUserId(userId);
        return notification.getUserId().equals(userId) || user.getIsAdmin();
    }

    private Integer getMaxNotificationId() {
        Notification notification = notificationRepository.findFirstByOrderByIdDesc();
        if (notification == null) {
            return 1;
        }
        return notification.getId();
    }

    @SneakyThrows
    public Notification findNotificationById(Integer notificationId) {
        return notificationRepository.findFirstById(notificationId);
    }
}
