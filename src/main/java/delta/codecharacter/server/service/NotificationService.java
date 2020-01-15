package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.request.PrivateAddNotificationRequest;
import delta.codecharacter.server.controller.response.PrivateNotificationResponse;
import delta.codecharacter.server.model.Notification;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.NotificationRepository;
import delta.codecharacter.server.repository.UserRepository;
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
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class NotificationService {

    private final Logger LOG = Logger.getLogger(NotificationService.class.getName());

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @SneakyThrows
    public PrivateNotificationResponse getNotificationById(@NotNull Integer notificationId, @NotNull User user) {
        Notification notification = findNotificationById(notificationId);
        Integer userId = user.getUserId();

        if (notification == null) {
            throw new Exception("Notification not found");
        }

        if (!notification.getUserId().equals(userId)) {
            throw new Exception("Unauthorized");
        }

        return PrivateNotificationResponse.builder()
                .notificationId(notificationId)
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .build();
    }

    @SneakyThrows
    public PrivateNotificationResponse addNotification(@NotNull PrivateAddNotificationRequest addNotificationRequest) {
        Integer notificationId = getMaxNotificationId() + 1;
        Notification notification = Notification.builder()
                .id(notificationId)
                .userId(addNotificationRequest.getUserId())
                .title(addNotificationRequest.getTitle())
                .content(addNotificationRequest.getContent())
                .type(addNotificationRequest.getType())
                .build();

        notificationRepository.save(notification);

        return PrivateNotificationResponse.builder()
                .notificationId(notificationId)
                .userId(notification.getUserId())
                .title(notification.getTitle())
                .content(notification.getContent())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .build();
    }

    @SneakyThrows
    public void readNotification(@NotNull Integer notificationId) {
        Notification notification = findNotificationById(notificationId);

        if (notification == null) {
            throw new Exception("Not Found");
        }

        notification.setIsRead(true);

        notificationRepository.save(notification);
    }

    @SneakyThrows
    public void deleteNotificationById(@NotNull Integer notificationId, @NotNull User user) {
        Notification notification = findNotificationById(notificationId);

        if (notification == null) {
            throw new Exception("Not Found");
        }

        if (!notification.getUserId().equals(user.getUserId())) {
            throw new Exception("Unauthorized");
        }

        notificationRepository.delete(notification);
    }

    @SneakyThrows
    public void deleteNotificationsByType(@NotNull Type type, @NotNull User user) {
        List<Notification> notifications = notificationRepository.findAllByTypeAndUserId(type, user.getUserId());
        notificationRepository.deleteAll(notifications);
    }

    @SneakyThrows
    public List<Notification> getAllNotificationsByUserId(@NotNull User user,
                                                          @NotNull @Positive int pageNumber,
                                                          @NotNull @PositiveOrZero int size) {
        handlePaginationBadRequests(pageNumber, size);
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        Page<Notification> notificationsPage = notificationRepository.findAllByUserIdOrderByIdDesc(user.getUserId(), pageable);
        return notificationsPage.getContent();
    }

    @SneakyThrows
    public List<Notification> getAllUnreadNotificationsByUserId(@NotNull User user,
                                                                @NotNull @Positive int pageNumber,
                                                                @NotNull @PositiveOrZero int size) {
        handlePaginationBadRequests(pageNumber, size);
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        Page<Notification> notificationsPage = notificationRepository.findAllByUserIdAndIsReadTrueOrderByIdDesc(user.getUserId(), pageable);
        return notificationsPage.getContent();
    }

    private Integer getMaxNotificationId() {
        Notification notification = notificationRepository.findFirstByOrderByIdDesc();
        if (notification == null) {
            return 1;
        }
        return notification.getId();
    }

    private Notification findNotificationById(Integer notificationId) {
        Optional<Notification> optionalNotification = notificationRepository.findById(notificationId);
        if (optionalNotification.isEmpty()) {
            return null;
        }
        return optionalNotification.get();
    }

    @SneakyThrows
    private void handlePaginationBadRequests(Integer pageNumber, Integer size) {
        if (pageNumber < 1) {
            throw new Exception("Page number should not be less than 1");
        }
        if (size < 1) {
            throw new Exception("Size should not be less than 1");
        }
        if (size > 100) {
            throw new Exception("Size should not be greater than 100");
        }
    }
}
