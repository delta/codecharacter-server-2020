package delta.codecharacter.server.controller.api;


import delta.codecharacter.server.controller.request.PrivateAddNotificationRequest;
import delta.codecharacter.server.controller.response.PrivateNotificationResponse;
import delta.codecharacter.server.model.Notification;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.service.NotificationService;
import delta.codecharacter.server.util.Type;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final Logger LOG = Logger.getLogger(NotificationController.class.getName());

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping(value = "/{notificationId}")
    public ResponseEntity<PrivateNotificationResponse> getNotificationById(@PathVariable Integer notificationId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        Notification notification = notificationService.getNotificationById(notificationId);
        checkNotificationAccess(user, notification);
        PrivateNotificationResponse notificationResponse = getNotificationResponse(notification);
        return new ResponseEntity<>(notificationResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/")
    public ResponseEntity<PrivateNotificationResponse> addNotification(@RequestBody @Valid PrivateAddNotificationRequest privateAddNotificationRequest, Authentication authentication) {
        User user = getAuthenticatedAdminUser(authentication.getName());
        Notification notification = notificationService.addNotification(privateAddNotificationRequest);
        PrivateNotificationResponse notificationResponse = getNotificationResponse(notification);
        return new ResponseEntity<>(notificationResponse, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{notificationId}/")
    public ResponseEntity<String> deleteNotificationById(@PathVariable Integer notificationId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        Notification notification = notificationService.getNotificationById(notificationId);
        checkNotificationAccess(user, notification);
        notificationService.deleteNotificationById(notificationId);
        return new ResponseEntity<>("Successfully deleted", HttpStatus.OK);
    }

    @DeleteMapping(value = "/type/{type}/")
    public ResponseEntity<String> deleteNotificationsByType(@PathVariable Type type, Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        notificationService.deleteNotificationsByTypeAndUser(type, user);
        return new ResponseEntity<>("Successfully deleted", HttpStatus.OK);
    }

    @PostMapping(value = "/read/{notificationId}/")
    public ResponseEntity<String> readNotification(@PathVariable Integer notificationId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        Notification notification = notificationService.getNotificationById(notificationId);
        checkNotificationAccess(user, notification);
        notificationService.readNotificationById(notificationId);
        return new ResponseEntity<>("Successfully read", HttpStatus.OK);
    }

    @GetMapping(value = "/unread/")
    public List<Notification> getAllUnreadNotificationsByUserId(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                                                @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                                                Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        Page<Notification> notificationPage = notificationService.getAllUnreadNotificationsByUser(user, page, size);
        return notificationPage.getContent();
    }

    @GetMapping(value = "")
    public List<Notification> getAllNotificationsByUserId(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                                          @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                                          Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        Page<Notification> notificationPage = notificationService.getAllNotificationsByUser(user, page, size);
        return notificationPage.getContent();
    }

    private PrivateNotificationResponse getNotificationResponse(Notification notification) {
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
    public User getAuthenticatedUser(String username) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new Exception("Unauthorized");
        }
        return user;
    }

    @SneakyThrows
    public User getAuthenticatedAdminUser(String username) {
        User user = userRepository.findByEmail(username);
        if ((user == null) || (!user.getIsAdmin())) {
            throw new Exception("Unauthorized");
        }
        return user;
    }

    @SneakyThrows
    private void checkNotificationAccess(@NotNull User user, @NotNull Notification notification) {
        if (!(notification.getUserId().equals(user.getUserId())) && !(user.getIsAdmin())) {
            throw new Exception("Unauthorized");
        }
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
