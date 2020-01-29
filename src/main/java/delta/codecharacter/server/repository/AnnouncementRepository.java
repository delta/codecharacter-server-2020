package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnnouncementRepository extends MongoRepository<Announcement, Integer> {
    Announcement findFirstByOrderByIdDesc();

    Announcement findFirstById(Integer announcementId);

    Page<Announcement> findByAdminUserId(Integer userId, Pageable pageable);
}
