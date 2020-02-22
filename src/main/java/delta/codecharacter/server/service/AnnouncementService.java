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

    @SneakyThrows
    public void createAnnouncement(String announcementMessage, Integer userId) {
        Integer announcementId = getMaxAnnouncementId() + 1;
        Announcement announcement = Announcement.builder()
                .id(announcementId)
                .adminUserId(userId)
                .message(announcementMessage)
                .build();
        announcementRepository.save(announcement);
    }

    @SneakyThrows
    public Announcement findAnnouncementById(Integer announcementId) {
        return announcementRepository.findFirstById(announcementId);
    }

    @SneakyThrows
    public Page<Announcement> getAllAnnouncementsPaginated(@NotNull @Positive int pageNumber,
                                                  @NotNull @Positive int size) {
        Pageable pageable = PageRequest.of(pageNumber - 1, size);
        return announcementRepository.findAll(pageable);
    }

    private Integer getMaxAnnouncementId() {
        Announcement announcement = announcementRepository.findFirstByOrderByIdDesc();
        if (announcement == null) {
            return 0;
        }
        return announcement.getId();
    }
}
