package delta.codecharacter.server.repository;

import delta.codecharacter.server.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends MongoRepository<Game, Integer> {
    Game findFirstByOrderByIdDesc();

    List<Game> findAllByMatchId(Integer matchId);

    Game findFirstById(Integer gameId);
}
