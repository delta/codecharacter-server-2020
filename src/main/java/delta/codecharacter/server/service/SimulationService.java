package delta.codecharacter.server.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import delta.codecharacter.server.controller.request.Simulation.ExecuteGame;
import delta.codecharacter.server.controller.request.Simulation.ExecuteMatchRequest;
import delta.codecharacter.server.controller.request.Simulation.SimulateMatchRequest;
import delta.codecharacter.server.model.Game;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.util.DllFile;
import delta.codecharacter.server.util.MapFile;
import delta.codecharacter.server.util.MatchMode;
import delta.codecharacter.server.util.RabbitSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

@Service
public class SimulationService {

    private final Logger LOG = Logger.getLogger(SimulationService.class.getName());

    Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    @Value("${compilebox.secret-key")
    private String secretKey;

    @Autowired
    private VersionControlService versionControlService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private GameService gameService;

    @Autowired
    private ConstantService constantService;

    public void simulateMatch(SimulateMatchRequest simulateMatchRequest) throws IOException, TimeoutException {

        Integer playerId1 = Integer.valueOf(simulateMatchRequest.getPlayerId1());
        Integer playerId2 = Integer.valueOf(simulateMatchRequest.getPlayerId2());

        String dll1 = DllFile.getDll1(playerId1);
        String dll2 = DllFile.getDll2(playerId2);

        String player1Code = null;
        String player2Code = null;
        if (dll1 == null) player1Code = versionControlService.getCode(playerId1);
        if (dll2 == null) player2Code = versionControlService.getCode(playerId2);

        ExecuteMatchRequest executeMatchRequest = ExecuteMatchRequest.builder()
                .dll1(dll1)
                .dll2(dll2)
                .code1(player1Code)
                .code2(player2Code)
                .build();

        switch (MatchMode.valueOf(simulateMatchRequest.getMatchMode())) {
            case SELF: {
                Match match = matchService.createMatch(playerId1, playerId2, MatchMode.SELF);

                Integer selfMatchMapId = Integer.valueOf(constantService.getConstantValueByKey("SELF_MATCH_MAP_ID"));
                Game newGame = gameService.createGame(match.getId(), selfMatchMapId);

                ExecuteGame[] executeGames = new ExecuteGame[1];
                executeGames[0] = ExecuteGame.builder()
                        .gameId(newGame.getId())
                        .map(MapFile.getMap(selfMatchMapId))
                        .build();

                executeMatchRequest.setMatchId(match.getId());
                executeMatchRequest.setGames(executeGames);
                executeMatchRequest.setSecretKey(secretKey);

                RabbitSender.sendMessage(gson.toJson(executeMatchRequest));
                break;
            }
            case AI: {

                break;
            }
            case PREV_COMMIT: {

                break;
            }
            case MANUAL: {

                break;
            }
            case AUTO: {

                break;
            }
            default: {
                throw new IllegalStateException("Unexpected value: " + simulateMatchRequest.getMatchMode());
            }
        }
    }
}
