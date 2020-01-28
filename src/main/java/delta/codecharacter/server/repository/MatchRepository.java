package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.model.UserStats;
import delta.codecharacter.server.util.Mode;
import delta.codecharacter.server.util.Verdict;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends MongoRepository<Match, Integer> {
    Match findFirstByPlayerId1AndMatchModeNotOrderByCreatedAtDesc(Integer userId,Mode matchmode);

    Long countByPlayerId1AndVerdictAndMatchMode(Integer userid, Verdict verdict, Mode matchmode);

    Long countByPlayerId2AndVerdictAndMatchMode(Integer userid, Verdict verdict, Mode matchmode);

    Long countByPlayerId1OrPlayerId2(Integer userid1,Integer userid2);
}
