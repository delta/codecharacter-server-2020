package delta.codecharacter.server.controller.api;


import delta.codecharacter.server.controller.request.Notification.CreateNotificationRequest;
import delta.codecharacter.server.model.Notification;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.service.NotificationService;
import delta.codecharacter.server.service.UserService;
import delta.codecharacter.server.util.PageUtils;
import delta.codecharacter.server.util.enums.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.logging.Logger;

@RestController
@CrossOrigin(origins = "*")
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
    public ResponseEntity<Notification> findNotificationById(@PathVariable @NotEmpty Integer notificationId, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        Notification notification = notificationService.findNotificationById(notificationId);
        if (notification == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        if (!notificationService.checkNotificationAccess(user.getUserId(), notificationId)) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }

        return new ResponseEntity<>(notification, HttpStatus.OK);
    }

    @PostMapping(value = "/")
    public ResponseEntity<String> createNotification(@RequestBody @Valid CreateNotificationRequest createNotificationRequest, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        if (!userService.getIsAdminUserByEmail(email)) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        notificationService.createNotification(createNotificationRequest);
        return new ResponseEntity<>("Notification created successfully", HttpStatus.CREATED);
    }

    @DeleteMapping(value = "/{notificationId}")
    public ResponseEntity<String> deleteNotificationById(@PathVariable @NotEmpty Integer notificationId, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        Notification notification = notificationService.findNotificationById(notificationId);
        if (notification == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        if (!notificationService.checkNotificationAccess(user.getUserId(), notificationId)) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        notificationService.deleteNotificationById(notification.getId());
        return new ResponseEntity<>("Successfully deleted", HttpStatus.OK);
    }

    @DeleteMapping(value = "/type/{type}")
    public ResponseEntity<String> deleteNotificationsByType(@PathVariable @NotEmpty Type type, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        notificationService.deleteNotificationsByTypeAndUserId(type, user.getUserId());
        return new ResponseEntity<>("Successfully deleted", HttpStatus.OK);
    }

    @PatchMapping(value = "/read/{notificationId}")
    public ResponseEntity<String> setIsReadNotificationById(@PathVariable @NotEmpty Integer notificationId, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        Notification notification = notificationService.findNotificationById(notificationId);
        if (notification == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        if (!notificationService.checkNotificationAccess(user.getUserId(), notificationId)) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        notificationService.setIsReadNotificationById(notificationId);
        return new ResponseEntity<>("Successfully read", HttpStatus.OK);
    }

    @GetMapping(value = "/unread")
    public ResponseEntity<List<Notification>> getAllUnreadNotificationsByUserId(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                                                                @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                                                                Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        PageUtils.validatePaginationParams(page, size);
        Page<Notification> notificationPage = notificationService.getAllUnreadNotificationsByUserIdPaginated(user.getUserId(), page, size);
        return new ResponseEntity<>(notificationPage.getContent(), HttpStatus.OK);
    }

    @GetMapping(value = "")
    public ResponseEntity<List<Notification>> getAllNotificationsByUserId(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                                                          @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                                                          Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        PageUtils.validatePaginationParams(page, size);
        Page<Notification> notificationPage = notificationService.getAllNotificationsByUserIdPaginated(user.getUserId(), page, size);
        return new ResponseEntity<>(notificationPage.getContent(), HttpStatus.OK);
    }

    @GetMapping(value = "/type/{type}")
    public ResponseEntity<List<Notification>> getAllNotificationsByTypeAndUserId(@PathVariable @NotEmpty Type type,
                                                                                 @RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                                                                 @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                                                                 Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        PageUtils.validatePaginationParams(page, size);
        Page<Notification> notificationPage = notificationService.getAllNotificationsByTypeAndUserIdPaginated(type, user.getUserId(), page, size);
        return new ResponseEntity<>(notificationPage.getContent(), HttpStatus.OK);
    }
}
