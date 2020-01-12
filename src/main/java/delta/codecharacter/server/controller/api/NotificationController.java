package delta.codecharacter.server.controller.api;


import delta.codecharacter.server.controller.request.AddNotificationRequest;
import delta.codecharacter.server.controller.request.PrivateNotificationResponse;
import delta.codecharacter.server.model.Notification;
import delta.codecharacter.server.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @RequestMapping(value = "", method = RequestMethod.POST)
    public ResponseEntity<String> addNotification(@RequestBody @Valid AddNotificationRequest notification){
        notificationService.addNotification(notification);
        return new ResponseEntity<>("Notification Added Successfully!", HttpStatus.OK);
    }

    @RequestMapping(value = "/read/id/{notificationId}/userId/{userId}/", method = RequestMethod.POST)
    public ResponseEntity<String> readNotification(@PathVariable("notificationId") Integer notificationId, @PathVariable("userId") Integer userId) {
        PrivateNotificationResponse response = notificationService.readNotification(notificationId, userId);
        if (response == null) {
            return new ResponseEntity<>("Invalid details", HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>("Notification added: " + response.toString(), HttpStatus.OK);
    }

    @RequestMapping(value = "/read/userId/{userId}", method = RequestMethod.GET)
    public List<PrivateNotificationResponse> getAllReadNotificationsByUserId(@PathVariable("userId") Integer userId) {
        return notificationService.getAllNotificationsByUserId(userId);
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }
}
