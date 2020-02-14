package delta.codecharacter.server.service;

import delta.codecharacter.server.model.Game;
import delta.codecharacter.server.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GameService {

    @Autowired
    private GameRepository gameRepository;

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

    public Integer getMaxGameId() {
        Game game = gameRepository.findFirstByOrderByIdDesc();
        if (game == null) return 0;
        return game.getId();
    }
}
