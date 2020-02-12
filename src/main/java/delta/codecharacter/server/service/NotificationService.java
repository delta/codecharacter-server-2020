package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.request.CreateNotificationRequest;
import delta.codecharacter.server.controller.response.PrivateNotificationResponse;
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
        return findNotificationById(notificationId);
    }

    @SneakyThrows
    public Notification addNotification(@NotNull CreateNotificationRequest addCreateNotificationRequest) {
        Integer notificationId = getMaxNotificationId() + 1;
        Notification notification = Notification.builder()
                .id(notificationId)
                .userId(addCreateNotificationRequest.getUserId())
                .title(addCreateNotificationRequest.getTitle())
                .content(addCreateNotificationRequest.getContent())
                .type(addCreateNotificationRequest.getType())
                .build();

        notificationRepository.save(notification);

        return notification;
    }

    @SneakyThrows
    public boolean setIsReadNotificationById(@NotNull Integer notificationId) {
        Notification notification = findNotificationById(notificationId);

        if (notification == null) {
            return false;
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        return true;
    }

    @SneakyThrows
    public boolean deleteNotificationById(@NotNull Integer notificationId) {
        Notification notification = findNotificationById(notificationId);

        if (notification == null) {
            return false;
        }

        notificationRepository.delete(notification);
        return true;
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

    public PrivateNotificationResponse getNotificationResponse(Notification notification) {
        return PrivateNotificationResponse.builder()
                .notificationId(notification.getId())
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .build();
    }

    @SneakyThrows
    public boolean checkNotificationAccess(@NotNull User user, @NotNull Notification notification) {
        if (!(notification.getUserId().equals(user.getUserId())) && !(user.getIsAdmin())) {
            return false;
        }
        return true;
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
