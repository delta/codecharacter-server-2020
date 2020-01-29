package delta.codecharacter.server.controller.api;

import delta.codecharacter.server.controller.response.PublicAnnouncementResponse;
import delta.codecharacter.server.model.Announcement;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.service.AnnouncementService;
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

    @PostMapping(value = "/")
    @SneakyThrows
    public ResponseEntity<PublicAnnouncementResponse> addAnnouncement(@RequestBody String announcementMessage, Authentication authentication) {

        User user = getAuthenticatedAdminUser(authentication.getName());
        if (!user.getIsAdmin()) {
            throw new Exception("Unauthorized");
        }
        PublicAnnouncementResponse announcementResponse = announcementService.addAnnouncement(announcementMessage, user);
        return new ResponseEntity<>(announcementResponse, HttpStatus.OK);
    }

    @GetMapping(value = "")
    public List<Announcement> getAllAnnouncements(@RequestParam(value = "page", defaultValue = "1", required = false) int page,
                                                  @RequestParam(value = "size", defaultValue = "10", required = false) int size,
                                                  Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        return announcementService.getAllAnnouncements(page, size);
    }

    @GetMapping(value = "/{announcementId}/")
    public ResponseEntity<PublicAnnouncementResponse> getAnnouncementById(@PathVariable("announcementId") int announcementId, Authentication authentication) {
        User user = getAuthenticatedUser(authentication.getName());
        return new ResponseEntity<>(announcementService.getAnnouncementById(announcementId), HttpStatus.OK);
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
}
