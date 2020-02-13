package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Notification;
import delta.codecharacter.server.util.enums.Type;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, Integer> {
    Notification findFirstById(Integer notificationId);

    Notification findFirstByOrderByIdDesc();

    Page<Notification> findAllByUserIdOrderByIdDesc(Integer userId, Pageable pageable);

    List<Notification> findAllByTypeAndUserId(Type type, Integer userId);

    Page<Notification> findAllByTypeAndUserId(Type type, Integer userId, Pageable pageable);

    Page<Notification> findAllByUserIdAndIsReadFalseOrderByIdDesc(Integer userId, Pageable pageable);
}
