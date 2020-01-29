package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.response.PublicAnnouncementResponse;
import delta.codecharacter.server.model.Announcement;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.AnnouncementRepository;
import delta.codecharacter.server.repository.UserRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;
import java.util.logging.Logger;

@Service
public class AnnouncementService {
    private final Logger LOG = Logger.getLogger(AnnouncementService.class.getName());

    @Autowired
    AnnouncementRepository announcementRepository;

    @Autowired
    UserRepository userRepository;

    @SneakyThrows
    public PublicAnnouncementResponse addAnnouncement(String announcementMessage, User user) {
        Integer announcementId = getMaxAnnouncementId() + 1;
        Announcement announcement = Announcement.builder()
                .id(announcementId)
                .adminUserId(user.getUserId())
                .message(announcementMessage)
                .build();

        PublicAnnouncementResponse announcementResponse = PublicAnnouncementResponse.builder()
                .announcementId(announcementId)
                .adminUserId(announcement.getAdminUserId())
                .message(announcement.getMessage())
                .build();

        announcementRepository.save(announcement);
        return announcementResponse;
    }

    @SneakyThrows
    public PublicAnnouncementResponse getAnnouncementById(Integer announcementId) {
        Announcement announcement = announcementRepository.findFirstById(announcementId);
        if (announcement == null) {
            throw new Exception("Not Found");
        }
        return PublicAnnouncementResponse.builder()
                .announcementId(announcement.getId())
                .adminUserId(announcement.getAdminUserId())
                .message(announcement.getMessage())
                .build();
    }

    @SneakyThrows
    public List<Announcement> getAllAnnouncements(@NotNull @Positive int pageNumber,
                                                  @NotNull @Positive int size) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        Page<Announcement> announcementPage = announcementRepository.findAll(pageable);
        return announcementPage.getContent();
    }

    private Integer getMaxAnnouncementId() {
        Announcement announcement = announcementRepository.findFirstByOrderByIdDesc();
        if (announcement == null) {
            return 0;
        }
        return announcement.getId();
    }
}
