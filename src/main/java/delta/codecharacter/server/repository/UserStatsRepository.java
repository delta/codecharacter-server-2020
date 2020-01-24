package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.User;
import delta.codecharacter.server.model.UserStats;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStatsRepository extends MongoRepository<UserStats, Integer> {
    UserStats findByUserId(Integer userId);
}
