package delta.codecharacter.server.service;

import delta.codecharacter.server.model.Announcement;
import delta.codecharacter.server.repository.AnnouncementRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.logging.Logger;

@Service
public class AnnouncementService {
    private final Logger LOG = Logger.getLogger(AnnouncementService.class.getName());

    @Autowired
    AnnouncementRepository announcementRepository;

    /**
     * Create an announcement with the given message and return it.
     * @param announcementMessage Message of announcement
     * @param adminUserId User ID of the admin user
     * @return Returns created announcement
     */
    @SneakyThrows
    public Announcement createAnnouncement(String announcementMessage, Integer adminUserId) {
        Integer announcementId = getMaxAnnouncementId() + 1;
        Announcement announcement = Announcement.builder()
                .id(announcementId)
                .adminUserId(adminUserId)
                .message(announcementMessage)
                .build();
        announcementRepository.save(announcement);
        return announcement;
    }

    /**
     * @param announcementId ID of Announcement
     * @return Announcement object
     */
    @SneakyThrows
    public Announcement findAnnouncementById(Integer announcementId) {
        return announcementRepository.findFirstById(announcementId);
    }

    /**
     *
     * @param pageNumber Starting page number in pagination
     * @param size Size of results
     * @return Paginated announcements
     */
    @SneakyThrows
    public Page<Announcement> getAllAnnouncementsPaginated(@NotNull @Positive int pageNumber,
                                                  @NotNull @Positive int size) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        return announcementRepository.findAll(pageable);
    }

    /**
     * @return Maximum announcement ID
     */
    private Integer getMaxAnnouncementId() {
        Announcement announcement = announcementRepository.findFirstByOrderByIdDesc();
        if (announcement == null) {
            return 0;
        }
        return announcement.getId();
    }
}
