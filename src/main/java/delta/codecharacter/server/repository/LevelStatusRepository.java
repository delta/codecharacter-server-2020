package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.LevelStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LevelStatusRepository extends MongoRepository<LevelStatus,Integer> {
    LevelStatus findFirstByUserId (Integer userId);
}
