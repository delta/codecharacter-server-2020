package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.model.UserStats;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends MongoRepository<Match, Integer> {
    List<Match> findByPlayerId1OrderByIdDesc(Integer userId);
    List<Match> findByPlayerId2OrderByIdDesc(Integer userId);

}