package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.util.enums.MatchMode;
import delta.codecharacter.server.util.enums.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends MongoRepository<Match, Integer> {
    List<Match> findAllByPlayerId1OrPlayerId2(Integer userId1, Integer userId2);

    Match findFirstByPlayerId1AndMatchModeOrderByCreatedAtDesc(Integer userId, MatchMode matchmode);

    Match findFirstByPlayerId1AndMatchModeNotOrderByCreatedAtDesc(Integer userId, MatchMode matchmode);
    List<Match> findAllByPlayerId1OrPlayerId2AndMatchModeOrMatchMode(Integer userId1, Integer userId2, MatchMode matchmode1, MatchMode matchmode2);

    Match findFirstByPlayerId1AndStatusAndMatchModeNot(Integer userId, Status status, MatchMode matchMode);

    Match findFirstByOrderByIdDesc();

    Match findFirstByPlayerId1AndMatchModeOrderByCreatedAtDesc(Integer userId, MatchMode matchmode);
}
