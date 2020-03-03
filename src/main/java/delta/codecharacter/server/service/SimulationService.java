package delta.codecharacter.server.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import delta.codecharacter.server.controller.request.Simulation.ExecuteGameDetails;
import delta.codecharacter.server.controller.request.Simulation.ExecuteMatchRequest;
import delta.codecharacter.server.controller.request.Simulation.SimulateMatchRequest;
import delta.codecharacter.server.model.CodeStatus;
import delta.codecharacter.server.model.Game;
import delta.codecharacter.server.model.Map;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.repository.CodeStatusRepository;
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

    @Value("/socket/response/alert/")
    private String socketAlertMessageDest;

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

    @Autowired
    private CodeStatusRepository codeStatusRepository;

    /**
     * Send an execute match request to compile-box
     *
     * @param simulateMatchRequest Details of the match to be simulated
     */
    @SneakyThrows
    public void simulateMatch(SimulateMatchRequest simulateMatchRequest) {

        Integer playerId1 = Integer.valueOf(simulateMatchRequest.getPlayerId1());
        Integer playerId2 = Integer.valueOf(simulateMatchRequest.getPlayerId2());
        Integer socketListenerId = null;
        if (simulateMatchRequest.getMatchMode() != MatchMode.AUTO) {
            socketListenerId = playerId1;
        }

        if (!simulateMatchRequest.getMatchMode().equals(String.valueOf(MatchMode.AUTO))) {
            Long remTime = matchService.getWaitTime(playerId1);
            if (remTime != 0) {
                socketService.sendMessage(socketDest + socketListenerId, "Please wait for " + remTime + "seconds to initiate next match");
                return;
            }

            // Check if the User has any matches that are not yet completed
            // NOTE: IDLE, EXECUTING are the statuses indicating unfinished matches
            Boolean isIdleMatchPresent = matchRepository.findFirstByPlayerId1AndStatusAndMatchModeNot(playerId1, Status.IDLE, MatchMode.AUTO) != null;
            Boolean isExecutingMatchPresent = matchRepository.findFirstByPlayerId1AndStatusAndMatchModeNot(playerId1, Status.EXECUTING, MatchMode.AUTO) != null;
            if (isIdleMatchPresent || isExecutingMatchPresent) {
                socketService.sendMessage(socketDest + socketListenerId, "Previous match has not completed");
                return;
            }
        }
        String dll1 = null, dll2 = null;
        String player1Code = null, player2Code = null;

        if (simulateMatchRequest.getMatchMode() == MatchMode.MANUAL || simulateMatchRequest.getMatchMode() == MatchMode.AUTO) {
            dll1 = DllUtil.getDll(playerId1, DllId.DLL_1);
            dll2 = DllUtil.getDll(playerId2, DllId.DLL_2);
            if (dll1 == null) {
                CodeStatus codeStatus = codeStatusRepository.findByUserId(playerId1);
                if (!codeStatus.isLocked())
                    socketService.sendMessage(socketDest + socketListenerId, "You have not submitted any code");
                player1Code = versionControlService.getLockedCode(playerId1);
            }
            if (dll2 == null) {
                CodeStatus codeStatus = codeStatusRepository.findByUserId(playerId2);
                if (!codeStatus.isLocked())
                    socketService.sendMessage(socketDest + socketListenerId, "Player2 has not submitted any code");
                player2Code = versionControlService.getLockedCode(playerId2);
            }
        } else {
            player1Code = versionControlService.getCode(playerId1);
            player2Code = versionControlService.getCode(playerId2);
        }

        ExecuteMatchRequest executeMatchRequest = ExecuteMatchRequest.builder()
                .dll1(dll1)
                .dll2(dll2)
                .code1(player1Code)
                .code2(player2Code)
                .build();

        Match match;
        List<ExecuteGameDetails> executeGames = new ArrayList<>();

        switch (simulateMatchRequest.getMatchMode()) {
            case SELF: {
                Integer mapId = simulateMatchRequest.getMapId();
                if (mapId == null) {
                    socketService.sendMessage(socketDest + socketListenerId, "MapId cannot be null");
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
                    socketService.sendMessage(socketDest + socketListenerId, "MapId cannot be null");
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
                String commitHash = simulateMatchRequest.getCommitHash();

                if (mapId == null) {
                    socketService.sendMessage(socketDest, "MapId cannot be null");
                    return;
                }
                if (commitHash == null) {
                    socketService.sendMessage(socketAlertMessageDest + userId, "CommitHash cannot be null");
                    return;
                }


                match = matchService.createMatch(playerId1, playerId2, MatchMode.PREV_COMMIT);
                Game newGame = gameService.createGame(match.getId(), simulateMatchRequest.getMapId());

                ExecuteGameDetails executeGameDetails = ExecuteGameDetails.builder()
                        .gameId(newGame.getId())
                        .map(MapUtil.getMap(mapId))
                        .build();
                executeGames.add(executeGameDetails);

                executeMatchRequest.setCode2(versionControlService.getCodeByCommitHash(playerId2, commitHash));
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
                socketService.sendMessage(socketDest + socketListenerId, "Invalid MatchMode");
                return;
            }
        }
        executeMatchRequest.setMatchId(match.getId());
        executeMatchRequest.setGames(executeGames);
        executeMatchRequest.setSecretKey(secretKey);

        rabbitMqService.sendMessageToQueue(gson.toJson(executeMatchRequest));

        socketService.sendMessage(socketDest + socketListenerId, "Match is executing");

        // Set match status to EXECUTING
        match.setStatus(Status.EXECUTING);
        matchRepository.save(match);
    }
}