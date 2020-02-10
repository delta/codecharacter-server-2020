package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.request.PrivateAddNotificationRequest;
import delta.codecharacter.server.model.Notification;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.NotificationRepository;
import delta.codecharacter.server.util.Type;
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

    @SneakyThrows
    public Notification getNotificationById(@NotNull Integer notificationId) {
        Notification notification = findNotificationById(notificationId);

        if (notification == null) {
            throw new Exception("Notification not found");
        }

        return notification;
    }

    @SneakyThrows
    public Notification addNotification(@NotNull PrivateAddNotificationRequest addNotificationRequest) {
        Integer notificationId = getMaxNotificationId() + 1;
        Notification notification = Notification.builder()
                .id(notificationId)
                .userId(addNotificationRequest.getUserId())
                .title(addNotificationRequest.getTitle())
                .content(addNotificationRequest.getContent())
                .type(addNotificationRequest.getType())
                .build();

        notificationRepository.save(notification);

        return notification;
    }

    @SneakyThrows
    public void readNotificationById(@NotNull Integer notificationId) {
        Notification notification = findNotificationById(notificationId);

        if (notification == null) {
            throw new Exception("Not Found");
        }

        notification.setIsRead(true);

        notificationRepository.save(notification);
    }

    @SneakyThrows
    public void deleteNotificationById(@NotNull Integer notificationId) {
        Notification notification = findNotificationById(notificationId);

        if (notification == null) {
            throw new Exception("Not Found");
        }

        notificationRepository.delete(notification);
    }

    @SneakyThrows
    public List<Notification> getAllNotificationsByTypeAndUser(@NotNull Type type, @NotNull User user) {
        return notificationRepository.findAllByTypeAndUserId(type, user.getUserId());
    }

    @SneakyThrows
    public void deleteNotificationsByTypeAndUser(@NotNull Type type, @NotNull User user) {
        List<Notification> notifications = notificationRepository.findAllByTypeAndUserId(type, user.getUserId());
        notificationRepository.deleteAll(notifications);
    }

    @SneakyThrows
    public Page<Notification> getAllNotificationsByUser(@NotNull User user,
                                                        @NotNull @Positive int pageNumber,
                                                        @NotNull @PositiveOrZero int size) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        return notificationRepository.findAllByUserIdOrderByIdDesc(user.getUserId(), pageable);
    }

    @SneakyThrows
    public Page<Notification> getAllUnreadNotificationsByUser(@NotNull User user,
                                                              @NotNull @Positive int pageNumber,
                                                              @NotNull @PositiveOrZero int size) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        return notificationRepository.findAllByUserIdAndIsReadFalseOrderByIdDesc(user.getUserId(), pageable);
    }

    private Integer getMaxNotificationId() {
        Notification notification = notificationRepository.findFirstByOrderByIdDesc();
        if (notification == null) {
            return 1;
        }
        return notification.getId();
    }

    private Notification findNotificationById(Integer notificationId) {
        return notificationRepository.findFirstById(notificationId);
    }
}
