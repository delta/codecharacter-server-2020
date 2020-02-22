package delta.codecharacter.server.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import delta.codecharacter.server.controller.request.Simulation.ExecuteGameDetails;
import delta.codecharacter.server.controller.request.Simulation.ExecuteMatchRequest;
import delta.codecharacter.server.controller.request.Simulation.SimulateMatchRequest;
import delta.codecharacter.server.model.Game;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.util.DllUtil;
import delta.codecharacter.server.util.MapUtil;
import delta.codecharacter.server.util.enums.DllId;
import delta.codecharacter.server.util.enums.MatchMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class SimulationService {

    private final Logger LOG = Logger.getLogger(SimulationService.class.getName());

    Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    @Value("${compilebox.secret-key}")
    private String secretKey;

    @Autowired
    private VersionControlService versionControlService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private GameService gameService;

    @Autowired
    private ConstantService constantService;

    @Autowired
    private RabbitMqService rabbitMqService;

    /**
     * Send an execute match request to compile-box
     *
     * @param simulateMatchRequest Details of the match to be simulated
     */
    public void simulateMatch(SimulateMatchRequest simulateMatchRequest) {

        Integer playerId1 = Integer.valueOf(simulateMatchRequest.getPlayerId1());
        Integer playerId2 = Integer.valueOf(simulateMatchRequest.getPlayerId2());

        String dll1 = DllUtil.getDll(playerId1, DllId.DLL_1);
        String dll2 = DllUtil.getDll(playerId2, DllId.DLL_2);

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

                ExecuteGameDetails[] executeGames = new ExecuteGameDetails[1];
                executeGames[0] = ExecuteGameDetails.builder()
                        .gameId(newGame.getId())
                        .map(MapUtil.getMap(selfMatchMapId))
                        .build();

                executeMatchRequest.setMatchId(match.getId());
                executeMatchRequest.setGames(executeGames);
                executeMatchRequest.setSecretKey(secretKey);

                rabbitMqService.sendMessageToQueue(gson.toJson(executeMatchRequest));
                break;
            }
            case AI: {
                //TODO: Handle Simulate AI match
                break;
            }
            case PREV_COMMIT: {
                //TODO: Handle Simulate PREV_COMMIT match
                break;
            }
            case MANUAL: {
                //TODO: Handle Simulate MANUAL match
                break;
            }
            case AUTO: {
                //TODO: Handle Simulate AUTO match
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected MatchMode value: " + simulateMatchRequest.getMatchMode());
            }
        }
    }
}