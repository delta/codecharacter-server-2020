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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class SimulationService {

    private final Logger LOG = Logger.getLogger(SimulationService.class.getName());

    Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    @Value("${compilebox.secret-key}")
    private String secretKey;

    @Value("user.dir")
    private String userDirectory;

    @Value("ai.dir")
    private String aiDirectory;

    @Autowired
    private VersionControlService versionControlService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private GameService gameService;

    @Autowired
    private RabbitMqService rabbitMqService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Send an execute match request to compile-box
     *
     * @param simulateMatchRequest Details of the match to be simulated
     * @param username             Username of the User initiating the match
     */
    public void simulateMatch(SimulateMatchRequest simulateMatchRequest, String username) {

        Integer playerId1 = Integer.valueOf(simulateMatchRequest.getPlayerId1());
        Integer playerId2 = Integer.valueOf(simulateMatchRequest.getPlayerId2());

        String dll1 = DllUtil.getDll(userDirectory, playerId1, DllId.DLL_1);
        String dll2 = DllUtil.getDll(userDirectory, playerId2, DllId.DLL_2);

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

                Integer mapId = simulateMatchRequest.getMapId();
                if (mapId == null) {
                    simpMessagingTemplate.convertAndSendToUser(username, "/simulation/match-response", "MapId cannot be null");
                }

                Game newGame = gameService.createGame(match.getId(), mapId);

                ExecuteGameDetails[] executeGames = new ExecuteGameDetails[1];
                executeGames[0] = ExecuteGameDetails.builder()
                        .gameId(newGame.getId())
                        .map(MapUtil.getMap(mapId))
                        .build();

                executeMatchRequest.setMatchId(match.getId());
                executeMatchRequest.setGames(executeGames);
                executeMatchRequest.setSecretKey(secretKey);

                rabbitMqService.sendMessageToQueue(gson.toJson(executeMatchRequest));
                break;
            }
            case AI: {
                Match match = matchService.createMatch(playerId1, playerId2, MatchMode.AI);
                Game newGame = gameService.createGame(match.getId(), simulateMatchRequest.getMapId());

                ExecuteGameDetails[] executeGames = new ExecuteGameDetails[1];
                executeGames[0] = ExecuteGameDetails.builder()
                        .gameId(newGame.getId())
                        .map(MapUtil.getMap(simulateMatchRequest.getMapId()))
                        .build();

                executeMatchRequest.setDll2(DllUtil.getDll(aiDirectory, simulateMatchRequest.getAiId(), DllId.DLL_2));

                executeMatchRequest.setMatchId(match.getId());
                executeMatchRequest.setGames(executeGames);
                executeMatchRequest.setSecretKey(secretKey);

                rabbitMqService.sendMessageToQueue(gson.toJson(executeMatchRequest));
                break;
            }
            case MANUAL: {
                Match match = matchService.createMatch(playerId1, playerId2, MatchMode.MANUAL);

                ExecuteGameDetails[] executeGames = new ExecuteGameDetails[5];
                for (int i = 0; i < 5; i++) {
                    Game newGame = gameService.createGame(match.getId(), i + 1);
                    executeGames[i] = ExecuteGameDetails.builder()
                            .gameId(newGame.getId())
                            .map(MapUtil.getMap(i + 1))
                            .build();
                }

                executeMatchRequest.setMatchId(match.getId());
                executeMatchRequest.setGames(executeGames);
                executeMatchRequest.setSecretKey(secretKey);

                rabbitMqService.sendMessageToQueue(gson.toJson(executeMatchRequest));
                break;
            }
            case PREV_COMMIT: {
                Match match = matchService.createMatch(playerId1, playerId2, MatchMode.PREV_COMMIT);
                Game newGame = gameService.createGame(match.getId(), simulateMatchRequest.getMapId());

                ExecuteGameDetails[] executeGames = new ExecuteGameDetails[1];
                executeGames[0] = ExecuteGameDetails.builder()
                        .gameId(newGame.getId())
                        .map(MapUtil.getMap(simulateMatchRequest.getMapId()))
                        .build();

                executeMatchRequest.setCode2(versionControlService.getCodeByCommitHash(playerId2, simulateMatchRequest.getCommitHash()));

                executeMatchRequest.setMatchId(match.getId());
                executeMatchRequest.setGames(executeGames);
                executeMatchRequest.setSecretKey(secretKey);

                rabbitMqService.sendMessageToQueue(gson.toJson(executeMatchRequest));
                break;
            }
            case AUTO: {
                Match match = matchService.createMatch(playerId1, playerId2, MatchMode.AUTO);

                ExecuteGameDetails[] executeGames = new ExecuteGameDetails[5];
                for (int i = 0; i < 5; i++) {
                    Game newGame = gameService.createGame(match.getId(), i + 1);
                    executeGames[i] = ExecuteGameDetails.builder()
                            .gameId(newGame.getId())
                            .map(MapUtil.getMap(i + 1))
                            .build();
                }

                executeMatchRequest.setMatchId(match.getId());
                executeMatchRequest.setGames(executeGames);
                executeMatchRequest.setSecretKey(secretKey);

                rabbitMqService.sendMessageToQueue(gson.toJson(executeMatchRequest));
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected MatchMode value: " + simulateMatchRequest.getMatchMode());
            }
        }
    }
}
