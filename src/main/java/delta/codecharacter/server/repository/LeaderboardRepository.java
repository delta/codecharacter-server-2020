package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Leaderboard;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderboardRepository extends MongoRepository<Leaderboard, Integer> {
    Integer countByRatingGreaterThan(Double rating);

    Leaderboard findFirstByUserId(Integer userId);

    Leaderboard findFirstByOrderByIdDesc();
}
