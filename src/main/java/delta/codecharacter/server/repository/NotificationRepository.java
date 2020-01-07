package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification, Integer> {
    Notification findFirstByOrderByIdDesc();
}
