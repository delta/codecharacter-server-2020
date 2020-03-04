package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.response.GameLogs;
import delta.codecharacter.server.model.Game;
import delta.codecharacter.server.repository.GameRepository;
import delta.codecharacter.server.repository.MatchRepository;
import delta.codecharacter.server.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private MatchRepository matchRepository;

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

    /**
     * Return the game logs and player1Id for the given game id
     *
     * @param gameId Game Id of the game
     * @return game log, player1 log and player2 log of the game
     */
    public GameLogs getGameLog(Integer gameId) {
        var gameLogs = LogUtil.getLogs(gameId);
        if (gameLogs == null)
            return null;

        var game = gameRepository.findFirstById(gameId);
        var match = matchRepository.findFirstById(game.getMatchId());
        gameLogs.setPlayerId1(match.getPlayerId1());

        return gameLogs;
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
     * Find game by ID
     *
     * @param gameId gameId of the game
     * @return Game details with the given gameId
     */
    public Game findGameById(Integer gameId) {
        return gameRepository.findFirstById(gameId);
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
