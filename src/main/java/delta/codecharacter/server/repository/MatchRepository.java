package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.util.enums.MatchMode;
import delta.codecharacter.server.util.enums.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends MongoRepository<Match, Integer> {
    Match findFirstByPlayerId1AndMatchModeNotOrderByCreatedAtDesc(Integer userId, MatchMode matchMode);

    Match findFirstByPlayerId1AndMatchModeOrderByCreatedAtDesc(Integer userId, MatchMode matchmode);

    List<Match> findAllByPlayerId1AndMatchMode(Integer userId, MatchMode matchmode);

    List<Match> findAllByPlayerId2AndMatchMode(Integer userId, MatchMode matchMode);

    Match findFirstByPlayerId1AndStatusAndMatchModeNot(Integer userId, Status status, MatchMode matchMode);

    Match findFirstByOrderByIdDesc();

    Match findFirstById(Integer id);
}
