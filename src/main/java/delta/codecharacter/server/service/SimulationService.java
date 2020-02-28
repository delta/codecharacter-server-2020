package delta.codecharacter.server.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import delta.codecharacter.server.controller.request.Simulation.ExecuteGameDetails;
import delta.codecharacter.server.controller.request.Simulation.ExecuteMatchRequest;
import delta.codecharacter.server.controller.request.Simulation.SimulateMatchRequest;
import delta.codecharacter.server.model.Game;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.repository.MatchRepository;
import delta.codecharacter.server.util.AiDllUtil;
import delta.codecharacter.server.util.DllUtil;
import delta.codecharacter.server.util.MapUtil;
import delta.codecharacter.server.util.enums.DllId;
import delta.codecharacter.server.util.enums.MatchMode;
import delta.codecharacter.server.util.enums.Status;
import lombok.SneakyThrows;
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

    @Autowired
    private VersionControlService versionControlService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private MatchRepository matchRepository;

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
     * @param userId               UserId of the User initiating the match
     */
    @SneakyThrows
    public void simulateMatch(SimulateMatchRequest simulateMatchRequest, Integer userId) {

        Integer playerId1 = Integer.valueOf(simulateMatchRequest.getPlayerId1());
        Integer playerId2 = Integer.valueOf(simulateMatchRequest.getPlayerId2());

        Long remTime = matchService.getWaitTime(playerId1);
        if (remTime != 0) {
            simpMessagingTemplate.convertAndSend("/simulation/match-response/" + userId, "PLease wait for " + remTime + " seconds to initiate your next match");
            return;
        } else {
            if (matchRepository.findByPlayerId1AndStatus(playerId1, Status.IDLE) != null
                    || matchRepository.findByPlayerId1AndStatus(playerId1, Status.EXECUTE_QUEUED) != null
                    || matchRepository.findByPlayerId1AndStatus(playerId1, Status.EXECUTING) != null) {
                simpMessagingTemplate.convertAndSend("/simulation/match-response/" + userId, "Your previous match is has not yet completed");
                return;
            }
        }

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

        Match match;
        ExecuteGameDetails[] executeGames;
        switch (MatchMode.valueOf(simulateMatchRequest.getMatchMode())) {
            case SELF: {
                match = matchService.createMatch(playerId1, playerId2, MatchMode.SELF);

                Integer mapId = simulateMatchRequest.getMapId();
                if (mapId == null) {
                    simpMessagingTemplate.convertAndSend("/simulation/match-response/" + userId, "MapId cannot be null");
                }

                Game newGame = gameService.createGame(match.getId(), mapId);

                executeGames = new ExecuteGameDetails[1];
                executeGames[0] = ExecuteGameDetails.builder()
                        .gameId(newGame.getId())
                        .map(MapUtil.getMap(mapId))
                        .build();
                break;
            }
            case AI: {
                match = matchService.createMatch(playerId1, playerId2, MatchMode.AI);
                Game newGame = gameService.createGame(match.getId(), simulateMatchRequest.getMapId());

                Integer mapId = simulateMatchRequest.getMapId();
                if (mapId == null) {
                    simpMessagingTemplate.convertAndSend("/simulation/match-response/" + userId, "MapId cannot be null");
                }

                executeGames = new ExecuteGameDetails[1];
                executeGames[0] = ExecuteGameDetails.builder()
                        .gameId(newGame.getId())
                        .map(MapUtil.getMap(mapId))
                        .build();

                executeMatchRequest.setDll2(AiDllUtil.getAiDll(playerId2));
                break;
            }
            case MANUAL: {
                match = matchService.createMatch(playerId1, playerId2, MatchMode.MANUAL);

                executeGames = new ExecuteGameDetails[5];
                for (int i = 0; i < 5; i++) {
                    Game newGame = gameService.createGame(match.getId(), i + 1);
                    executeGames[i] = ExecuteGameDetails.builder()
                            .gameId(newGame.getId())
                            .map(MapUtil.getMap(i + 1))
                            .build();
                }
                break;
            }
            case PREV_COMMIT: {
                match = matchService.createMatch(playerId1, playerId2, MatchMode.PREV_COMMIT);
                Game newGame = gameService.createGame(match.getId(), simulateMatchRequest.getMapId());

                Integer mapId = simulateMatchRequest.getMapId();
                if (mapId == null) {
                    simpMessagingTemplate.convertAndSend("/simulation/match-response/" + userId, "MapId cannot be null");
                }

                executeGames = new ExecuteGameDetails[1];
                executeGames[0] = ExecuteGameDetails.builder()
                        .gameId(newGame.getId())
                        .map(MapUtil.getMap(mapId))
                        .build();

                executeMatchRequest.setCode2(versionControlService.getCodeByCommitHash(playerId2, simulateMatchRequest.getCommitHash()));
                break;
            }
            case AUTO: {
                match = matchService.createMatch(playerId1, playerId2, MatchMode.AUTO);

                executeGames = new ExecuteGameDetails[5];
                for (int i = 0; i < 5; i++) {
                    Game newGame = gameService.createGame(match.getId(), i + 1);
                    executeGames[i] = ExecuteGameDetails.builder()
                            .gameId(newGame.getId())
                            .map(MapUtil.getMap(i + 1))
                            .build();
                }
                break;
            }
            default: {
                throw new IllegalStateException("Unexpected MatchMode value: " + simulateMatchRequest.getMatchMode());
            }
        }
        executeMatchRequest.setMatchId(match.getId());
        executeMatchRequest.setGames(executeGames);
        executeMatchRequest.setSecretKey(secretKey);

        simpMessagingTemplate.convertAndSend("/simulation/match-response/" + userId, "Match has been added to queue");

        rabbitMqService.sendMessageToQueue(gson.toJson(executeMatchRequest));

        //set match status to Execute_Queued
        match.setStatus(Status.EXECUTE_QUEUED);
        matchRepository.save(match);
    }
}
