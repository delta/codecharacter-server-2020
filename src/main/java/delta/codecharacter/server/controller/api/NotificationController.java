package delta.codecharacter.server.controller.api;


import delta.codecharacter.server.controller.request.AddNotificationRequest;
import delta.codecharacter.server.controller.request.PublicUserRequest;
import delta.codecharacter.server.model.Notification;
import delta.codecharacter.server.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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


    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<Notification> getAllNotifications() {
        return notificationService.getAllNotifications();
    }
}
