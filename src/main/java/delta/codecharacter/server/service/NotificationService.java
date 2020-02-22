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

    /**
     * @param createNotificationRequest Notification request with the required details
     * @return Created notification
     */
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

    /**
     * Set isRead of notification to true
     * @param notificationId ID of notification
     */
    @SneakyThrows
    public void setIsReadNotificationById(@NotNull Integer notificationId) {
        Notification notification = notificationRepository.findFirstById(notificationId);
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Deletes notification by ID
     * @param notificationId ID of notification
     */
    @SneakyThrows
    public void deleteNotificationById(@NotNull Integer notificationId) {
        Notification notification = notificationRepository.findFirstById(notificationId);
        notificationRepository.delete(notification);
    }

    /**
     * 
     * @param type Type of notification
     * @param userId ID of the current user
     * @param pageNumber Starting page number in the paginated response
     * @param size Size of the response list Size of notifications list
     * @return Page of notifications
     */
    @SneakyThrows
    public Page<Notification> getAllNotificationsByTypeAndUserIdPaginated(@NotNull Type type,
                                                                 @NotNull Integer userId,
                                                                 @NotNull int pageNumber,
                                                                 @NotNull int size) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        return notificationRepository.findAllByTypeAndUserId(type, userId, pageable);
    }

    /**
     * 
     * @param type Type of notification
     * @param userId ID of the current user
     */
    @SneakyThrows
    public void deleteNotificationsByTypeAndUserId(@NotNull Type type, @NotNull Integer userId) {
        List<Notification> notifications = notificationRepository.findAllByTypeAndUserId(type, userId);
        notificationRepository.deleteAll(notifications);
    }

    /**
     * 
     * @param userId ID of the current user
     * @param pageNumber Starting page number in the paginated response
     * @param size Size of the response list
     * @return Paginated response of all notifications by user ID
     */
    @SneakyThrows
    public Page<Notification> getAllNotificationsByUserId(@NotNull Integer userId,
                                                        @NotNull @Positive int pageNumber,
                                                        @NotNull @PositiveOrZero int size) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        return notificationRepository.findAllByUserIdOrderByIdDesc(userId, pageable);
    }

    /**
     * 
     * @param userId ID of the current user
     * @param pageNumber Starting page number in the paginated response
     * @param size Size of the response list
     * @return Paginated response of all unread notifications by user ID
     */
    @SneakyThrows
    public Page<Notification> getAllUnreadNotificationsByUserId(@NotNull Integer userId,
                                                                @NotNull @Positive int pageNumber,
                                                                @NotNull @PositiveOrZero int size) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        return notificationRepository.findAllByUserIdAndIsReadFalseOrderByIdDesc(userId, pageable);
    }

    /**
     * 
     * @param userId ID of the current user
     * @param notificationId ID of notification
     * @return Check if a user has access to a particular notification
     */
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

    /**
     * 
     * @param notificationId ID of notification
     * @return Notification of the passed ID, null if it doesn't exist
     */
    @SneakyThrows
    public Notification findNotificationById(Integer notificationId) {
        return notificationRepository.findFirstById(notificationId);
    }
}
