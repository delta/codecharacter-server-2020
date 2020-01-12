package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends MongoRepository<Notification, Integer> {
    Notification findFirstByOrderByIdDesc();

    Optional<Notification> findById(Integer integer);

    List<Notification> findAllByUserIdEquals(Integer userId);
}
