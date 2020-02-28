package delta.codecharacter.server.controller.api;


import delta.codecharacter.server.model.Announcement;
import delta.codecharacter.server.model.User;
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
@CrossOrigin(origins = "*")
@RequestMapping(value = "/announcements")
public class AnnouncementController {

    private final Logger LOG = Logger.getLogger(AnnouncementController.class.getName());

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserService userService;

    @PostMapping(value = "/")
    @SneakyThrows
    public ResponseEntity<String> createAnnouncement(@RequestBody String announcementMessage, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        User user = userService.getUserByEmail(email);
        if (user == null) return new ResponseEntity<>("User not found!", HttpStatus.UNAUTHORIZED);
        if (!userService.getIsAdminUserByEmail(email)) {
            return new ResponseEntity<>("Unauthorized!", HttpStatus.UNAUTHORIZED);
        }
        announcementService.createAnnouncement(announcementMessage, user.getUserId());
        return new ResponseEntity<>("Announcement created Successfully", HttpStatus.CREATED);
    }

    @GetMapping(value = "/")
    public ResponseEntity<List<Announcement>> getAllAnnouncementsPaginated(@RequestParam(value = "page", defaultValue = "1", required = true) int page,
                                                                           @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                                                           Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        if (!userService.isEmailPresent(email)) return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        List<Announcement> announcements = announcementService.getAllAnnouncementsPaginated(page, size).getContent();
        return new ResponseEntity<>(announcements, HttpStatus.OK);
    }

    @GetMapping(value = "/{announcementId}/")
    public ResponseEntity<Announcement> findAnnouncementById(@PathVariable int announcementId, Authentication authentication) {
        String email = userService.getEmailFromAuthentication(authentication);
        if (!userService.isEmailPresent(email)) return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
        Announcement announcement = announcementService.findAnnouncementById(announcementId);
        if (announcement == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(announcement, HttpStatus.OK);
    }

}
