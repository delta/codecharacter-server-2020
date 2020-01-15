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

    @GetMapping(value = "/{notificationId}/")
    public ResponseEntity<PrivateNotificationResponse> getNotificationById(@PathVariable("notificationId") Integer notificationId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        PrivateNotificationResponse response = notificationService.getNotificationById(notificationId, user);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/")
    public PrivateNotificationResponse addNotification(@RequestBody @Valid PrivateAddNotificationRequest privateAddNotificationRequest, Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        return notificationService.addNotification(privateAddNotificationRequest);
    }

    @DeleteMapping(value = "/{notificationId}/")
    public ResponseEntity<String> deleteNotificationById(@PathVariable("notificationId") Integer notificationId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        notificationService.deleteNotificationById(notificationId, user);
        return new ResponseEntity<>("Successfully deleted", HttpStatus.OK);
    }

    @DeleteMapping(value = "/type/{type}/")
    public ResponseEntity<String> deleteNotificationsByType(@PathVariable("type") Type type, Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        notificationService.deleteNotificationsByType(type, user);
        return new ResponseEntity<>("Successfully deleted", HttpStatus.OK);
    }

    @PostMapping(value = "/read/{notificationId}/")
    public ResponseEntity<String> readNotification(@PathVariable("notificationId") Integer notificationId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        notificationService.readNotification(notificationId);
        return new ResponseEntity<>("Successfully read", HttpStatus.OK);
    }

    @GetMapping(value = "/unread/")
    public List<Notification> getAllUnreadNotificationsByUserId(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                                                @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                                                Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        return notificationService.getAllUnreadNotificationsByUserId(user, page, size);
    }

    @GetMapping(value = "")
    public List<Notification> getAllNotificationsByUserId(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                                          @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                                          Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        return notificationService.getAllNotificationsByUserId(user, page, size);
    }

    @SneakyThrows
    private User getAuthenticatedUser(String username) {
        User user = userRepository.findByEmail(username);
        if (user == null) {
            throw new Exception("Unauthorized");
        }
        return user;
    }
}
