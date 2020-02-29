package delta.codecharacter.server.service;

import delta.codecharacter.server.model.Game;
import delta.codecharacter.server.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    /**
     * Create a new game for the given matchId
     *
     * @param matchId MatchId to which the game belongs to
     * @param mapId   MapId of the map for the game
     * @return Details of the Game created
     */
    public Game createGame(Integer matchId, Integer mapId) {
        Integer gameId = getMaxGameId() + 1;
        Game game = Game.builder()
                .id(gameId)
                .matchId(matchId)
                .mapId(mapId)
                .build();

        gameRepository.save(game);

        return game;
    }

    public List<Game> findAllGamesByMatchId(Integer matchId) {
        return gameRepository.findAllByMatchId(matchId);
    }

    /**
     * Get all the games associated with a match
     *
     * @param matchId MatchId for which the games are to be fetched
     * @return List of all the games for the given matchId
     */
    public List<Game> getAllGamesByMatchId(Integer matchId) {
        return gameRepository.findAllByMatchId(matchId);
    }

    /**
     * Get the current maximum gameId
     *
     * @return Maximum gameId
     */
    public Integer getMaxGameId() {
        Game game = gameRepository.findFirstByOrderByIdDesc();
        if (game == null) return 0;
        return game.getId();
    }
}
