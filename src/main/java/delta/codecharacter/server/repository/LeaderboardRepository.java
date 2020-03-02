package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Leaderboard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderboardRepository extends MongoRepository<Leaderboard, Integer> {
    Integer countByRatingGreaterThan(Integer rating);

    Leaderboard findFirstByUserId(Integer userId);
}
