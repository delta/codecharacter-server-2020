package delta.codecharacter.server.controller.api;


import delta.codecharacter.server.model.Announcement;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.service.AnnouncementService;
import delta.codecharacter.server.service.UserService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@RequestMapping(value = "/announcements")
public class AnnouncementController {

    private final Logger LOG = Logger.getLogger(AnnouncementController.class.getName());

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserService userService;

    @PostMapping(value = "/")
    @SneakyThrows
    public ResponseEntity<String> createAnnouncement(@RequestBody String announcementMessage, Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        if (!userService.getIsAdminUserByUsername(user.getUsername())) {
            return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        }
        announcementService.createAnnouncement(announcementMessage, user.getUserId());
        return new ResponseEntity<>("Announcement created Successfully", HttpStatus.CREATED);
    }

    @GetMapping(value = "")
    public ResponseEntity<List<Announcement>> getAllAnnouncements(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                                                  @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                                                  Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        List<Announcement> announcements = announcementService.getAllAnnouncements(page, size).getContent();
        return new ResponseEntity<>(announcements, HttpStatus.OK);
    }

    @GetMapping(value = "/{announcementId}/")
    public ResponseEntity<Announcement> findAnnouncementById(@PathVariable int announcementId, Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        Announcement announcement = announcementService.findAnnouncementById(announcementId);
        if (announcement == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(announcement, HttpStatus.OK);
    }
}
