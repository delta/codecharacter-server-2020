package delta.codecharacter.server.controller.api;


import delta.codecharacter.server.controller.request.NotificationRequest;
import delta.codecharacter.server.controller.response.PrivateNotificationResponse;
import delta.codecharacter.server.model.Notification;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.service.NotificationService;
import delta.codecharacter.server.service.UserService;
import delta.codecharacter.server.util.PageUtils;
import delta.codecharacter.server.util.Type;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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

    @Autowired
    UserService userService;

    @GetMapping(value = "/{notificationId}")
    @SneakyThrows
    public ResponseEntity<PrivateNotificationResponse> getNotificationById(@PathVariable Integer notificationId, Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Notification notification = notificationService.getNotificationById(notificationId);
        if (notification == null) {
            throw new Exception("Notification not found");
        }
        notificationService.checkNotificationAccess(user, notification);
        PrivateNotificationResponse notificationResponse = notificationService.getNotificationResponse(notification);
        return new ResponseEntity<>(notificationResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/")
    public ResponseEntity<PrivateNotificationResponse> createNotification(@RequestBody @Valid NotificationRequest notificationRequest, Authentication authentication) {
        authenticateAdmin(authentication.getName());
        Notification notification = notificationService.addNotification(notificationRequest);
        PrivateNotificationResponse notificationResponse = notificationService.getNotificationResponse(notification);
        return new ResponseEntity<>(notificationResponse, HttpStatus.OK);
    }

    @DeleteMapping(value = "/{notificationId}/")
    @SneakyThrows
    public ResponseEntity<String> deleteNotificationById(@PathVariable Integer notificationId, Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Notification notification = notificationService.getNotificationById(notificationId);
        if (notification == null) {
            throw new Exception("Notification not found");
        }
        notificationService.checkNotificationAccess(user, notification);
        notificationService.deleteNotificationById(notificationId);
        return new ResponseEntity<>("Successfully deleted", HttpStatus.OK);
    }

    @DeleteMapping(value = "/type/{type}/")
    public ResponseEntity<String> deleteNotificationsByType(@PathVariable Type type, Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        notificationService.deleteNotificationsByTypeAndUser(type, user);
        return new ResponseEntity<>("Successfully deleted", HttpStatus.OK);
    }

    @PostMapping(value = "/read/{notificationId}/")
    @SneakyThrows
    public ResponseEntity<String> readNotification(@PathVariable Integer notificationId, Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Notification notification = notificationService.getNotificationById(notificationId);
        notificationService.checkNotificationAccess(user, notification);
        if (!notificationService.setIsReadNotificationById(notificationId)) {
            throw new Exception("Notification not found");
        }
        return new ResponseEntity<>("Successfully read", HttpStatus.OK);
    }

    @GetMapping(value = "/unread/")
    public ResponseEntity<List<Notification>> getAllUnreadNotificationsByUserId(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                                                @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                                                Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        PageUtils.validatePaginationParams(page, size);
        Page<Notification> notificationPage = notificationService.getAllUnreadNotificationsByUser(user, page, size);
        return new ResponseEntity<>(notificationPage.getContent(), HttpStatus.OK);
    }

    @GetMapping(value = "")
    public ResponseEntity<List<Notification>> getAllNotificationsByUserId(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                                          @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                                          Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        PageUtils.validatePaginationParams(page, size);
        Page<Notification> notificationPage = notificationService.getAllNotificationsByUser(user, page, size);
        return new ResponseEntity<>(notificationPage.getContent(), HttpStatus.OK);
    }

    @SneakyThrows
    private void authenticateAdmin(String username) {
        if (!userService.getIsAdminUserByUsername(username)) {
            throw new Exception("Unauthorized");
        }
    }
}
