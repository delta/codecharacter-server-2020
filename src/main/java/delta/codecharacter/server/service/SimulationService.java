package delta.codecharacter.server.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import delta.codecharacter.server.controller.request.Simulation.ExecuteGameDetails;
import delta.codecharacter.server.controller.request.Simulation.ExecuteMatchRequest;
import delta.codecharacter.server.controller.request.Simulation.SimulateMatchRequest;
import delta.codecharacter.server.model.Game;
import delta.codecharacter.server.model.Map;
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
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Service
public class SimulationService {

    private final Logger LOG = Logger.getLogger(SimulationService.class.getName());

    Gson gson = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

    @Value("${compilebox.secret-key}")
    private String secretKey;

    @Value("/response/")
    private String socketDest;

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
    private SocketService socketService;

    @Autowired
    private MapService mapService;

    /**
     * Send an execute match request to compile-box
     *
     * @param simulateMatchRequest Details of the match to be simulated
     * @param userId               UserId of the User to whom socket messages are to be sent
     *                             NOTE: userId is null if matchMode is AUTO
     */
    @SneakyThrows
    public void simulateMatch(SimulateMatchRequest simulateMatchRequest, @Nullable Integer userId) {

        Integer playerId1 = Integer.valueOf(simulateMatchRequest.getPlayerId1());
        Integer playerId2 = Integer.valueOf(simulateMatchRequest.getPlayerId2());

        if (!simulateMatchRequest.getMatchMode().equals(String.valueOf(MatchMode.AUTO))) {
            Long remTime = matchService.getWaitTime(playerId1);
            if (remTime != 0) {
                socketService.sendMessage(socketDest + userId, "Please wait for " + remTime + "seconds to initiate next match");
                return;
            }

            // Check if the User has any matches that are not yet completed
            // NOTE: IDLE, EXECUTING are the statuses indicating unfinished matches
            Boolean isIdleMatchPresent = matchRepository.findFirstByPlayerId1AndStatusAndMatchModeNot(userId, Status.IDLE, MatchMode.AUTO) != null;
            Boolean isExecutingMatchPresent = matchRepository.findFirstByPlayerId1AndStatusAndMatchModeNot(userId, Status.EXECUTING, MatchMode.AUTO) != null;
            if (isIdleMatchPresent || isExecutingMatchPresent) {
                socketService.sendMessage(socketDest + userId, "Previous match has not completed");
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
        List<ExecuteGameDetails> executeGames = new ArrayList<>();

        switch (MatchMode.valueOf(simulateMatchRequest.getMatchMode())) {
            case SELF: {
                Integer mapId = simulateMatchRequest.getMapId();
                if (mapId == null) {
                    socketService.sendMessage(socketDest + userId, "MapId cannot be null");
                    return;
                }

                match = matchService.createMatch(playerId1, playerId2, MatchMode.SELF);
                Game newGame = gameService.createGame(match.getId(), mapId);

                ExecuteGameDetails executeGameDetails = ExecuteGameDetails.builder()
                        .gameId(newGame.getId())
                        .map(MapUtil.getMap(mapId))
                        .build();
                executeGames.add(executeGameDetails);
                break;
            }

            case AI: {
                Integer mapId = simulateMatchRequest.getMapId();
                if (mapId == null) {
                    socketService.sendMessage(socketDest + userId, "MapId cannot be null");
                    return;
                }

                match = matchService.createMatch(playerId1, playerId2, MatchMode.AI);
                Game newGame = gameService.createGame(match.getId(), simulateMatchRequest.getMapId());

                ExecuteGameDetails executeGameDetails = ExecuteGameDetails.builder()
                        .gameId(newGame.getId())
                        .map(MapUtil.getMap(mapId))
                        .build();
                executeGames.add(executeGameDetails);

                executeMatchRequest.setDll2(AiDllUtil.getAiDll(playerId2));
                break;
            }

            case MANUAL: {
                match = matchService.createMatch(playerId1, playerId2, MatchMode.MANUAL);

                List<Map> maps = mapService.getAllMaps();
                for (var map : maps) {
                    Game newGame = gameService.createGame(match.getId(), map.getId());
                    var executeGameDetails = ExecuteGameDetails.builder()
                            .gameId(newGame.getId())
                            .map(MapUtil.getMap(map.getId()))
                            .build();
                    executeGames.add(executeGameDetails);
                }
                break;
            }

            case PREV_COMMIT: {
                Integer mapId = simulateMatchRequest.getMapId();
                if (mapId == null) {
                    socketService.sendMessage(socketDest, "MapId cannot be null");
                    return;
                }

                match = matchService.createMatch(playerId1, playerId2, MatchMode.PREV_COMMIT);
                Game newGame = gameService.createGame(match.getId(), simulateMatchRequest.getMapId());

                ExecuteGameDetails executeGameDetails = ExecuteGameDetails.builder()
                        .gameId(newGame.getId())
                        .map(MapUtil.getMap(mapId))
                        .build();
                executeGames.add(executeGameDetails);

                executeMatchRequest.setCode2(versionControlService.getCodeByCommitHash(playerId2, simulateMatchRequest.getCommitHash()));
                break;
            }
            case AUTO: {
                match = matchService.createMatch(playerId1, playerId2, MatchMode.AUTO);

                List<Map> maps = mapService.getAllMaps();
                for (var map : maps) {
                    Game newGame = gameService.createGame(match.getId(), map.getId());
                    var executeGameDetails = ExecuteGameDetails.builder()
                            .gameId(newGame.getId())
                            .map(MapUtil.getMap(map.getId()))
                            .build();
                    executeGames.add(executeGameDetails);
                }
                break;
            }
            default: {
                socketService.sendMessage(socketDest + userId, "Invalid MatchMode");
                return;
            }
        }
        executeMatchRequest.setMatchId(match.getId());
        executeMatchRequest.setGames(executeGames);
        executeMatchRequest.setSecretKey(secretKey);

        rabbitMqService.sendMessageToQueue(gson.toJson(executeMatchRequest));

        socketService.sendMessage(socketDest + userId, "Match is executing");

        // Set match status to EXECUTING
        match.setStatus(Status.EXECUTING);
        matchRepository.save(match);
    }
}
