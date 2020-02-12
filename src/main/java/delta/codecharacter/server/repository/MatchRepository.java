package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.util.Enums.MatchMode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends MongoRepository<Match, Integer> {
    List<Match> findAllByPlayerId1OrPlayerId2(Integer userId1, Integer userId2);

    Match findFirstByPlayerId1AndMatchModeNotOrderByCreatedAtDesc(Integer userId, MatchMode matchmode);
}
