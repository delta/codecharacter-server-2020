package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.request.Notification.CreateNotificationRequest;
import delta.codecharacter.server.controller.request.UpdateGameDetails;
import delta.codecharacter.server.controller.request.UpdateMatchRequest;
import delta.codecharacter.server.controller.response.GameLogs;
import delta.codecharacter.server.controller.response.Match.DetailedMatchStatsResponse;
import delta.codecharacter.server.controller.response.Match.MatchResponse;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.*;
import delta.codecharacter.server.util.DllUtil;
import delta.codecharacter.server.util.LogUtil;
import delta.codecharacter.server.util.MatchStats;
import delta.codecharacter.server.util.enums.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static delta.codecharacter.server.util.enums.Verdict.*;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class MatchService {

    private final Logger LOG = Logger.getLogger(UserController.class.getName());

    @Value("/socket/response/alert/")
    private String socketAlertMessageDest;

    @Value("/socket/response/match/")
    private String socketMatchResultDest;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ConstantRepository constantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TopMatchRepository topMatchRepository;

    @Autowired
    private GameRepository gameRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GameService gameService;

    @Autowired
    private SocketService socketService;

    @Autowired
    private UserRatingService userRatingService;

    @Autowired
    private NotificationService notificationService;

    /**
     * Create a new match for the given players and matchMode
     *
     * @param playerId1 UserId of the player initiating the match
     * @param playerId2 UserId of the player against whom match was initiated
     * @param matchMode Mode of the match
     * @return Details of the Match created
     */
    public Match createMatch(Integer playerId1, Integer playerId2, MatchMode matchMode) {
        Integer matchId = getMaxMatchId() + 1;

        Match match = Match.builder()
                .id(matchId)
                .playerId1(playerId1)
                .playerId2(playerId2)
                .matchMode(matchMode)
                .status(Status.IDLE)
                .build();

        matchRepository.save(match);
        return match;
    }

    /**
     * Get the details of top matches from topMatch collection
     *
     * @return list of top matches
     */
    public List<MatchResponse> getTopMatches(Integer PageNumber, Integer PageSize) {
        Pageable pageable = PageRequest.of(PageNumber - 1, PageSize);
        var topMatches = topMatchRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<MatchResponse> matchResponses = new ArrayList<>();
        for (var topMatch : topMatches) {
            Match match = matchRepository.findFirstById(topMatch.getMatchId());
            User user1 = userRepository.findByUserId(match.getPlayerId1());
            User user2 = userRepository.findByUserId(match.getPlayerId2());
            MatchResponse matchResponse = MatchResponse.builder()
                    .username1(user1.getUsername())
                    .username2(user2.getUsername())
                    .avatar1(user1.getAvatarId())
                    .avatar2(user2.getAvatarId())
                    .score1(match.getScore1())
                    .score2(match.getScore2())
                    .verdict(match.getVerdict())
                    .matchMode(match.getMatchMode())
                    .games(gameService.getAllGamesByMatchId(match.getId()))
                    .playedAt(match.getCreatedAt())
                    .build();

            matchResponses.add(matchResponse);
        }
        return matchResponses;
    }

    /**
     * Get a paginated list of Manual and Auto matches played by the user
     *
     * @param userId UserId of the player
     * @return List of paginated manual and auto matches
     */
    public List<MatchResponse> getManualAndAutoExecutedMatchesPaginated(Integer userId, Pageable pageable) {
        Aggregation aggregation = newAggregation(
                match(
                        new Criteria().andOperator(
                                new Criteria().andOperator(
                                        new Criteria().orOperator(Criteria.where("player_id_1").is(userId), Criteria.where("player_id_2").is(userId)),
                                        new Criteria().orOperator(Criteria.where("match_mode").is(MatchMode.MANUAL), Criteria.where("match_mode").is(MatchMode.AUTO))
                                ), Criteria.where("status").is("EXECUTED")
                        )
                ),
                sort(Sort.by("createdAt").descending()),
                skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                limit(pageable.getPageSize())
        );

        var groupResults = mongoTemplate.aggregate(aggregation, Match.class, Match.class);
        List<Match> matches = groupResults.getMappedResults();

        List<MatchResponse> matchResponseList = new ArrayList<>();
        for (var match : matches) {

            User user1 = userRepository.findByUserId(match.getPlayerId1());
            User user2 = userRepository.findByUserId(match.getPlayerId2());

            var matchResponse = MatchResponse.builder()
                    .username1(user1.getUsername())
                    .username2(user2.getUsername())
                    .avatar1(user1.getAvatarId())
                    .avatar2(user2.getAvatarId())
                    .score1(match.getScore1())
                    .score2(match.getScore2())
                    .verdict(match.getVerdict())
                    .playedAt(match.getCreatedAt())
                    .matchMode(match.getMatchMode())
                    .games(gameService.getAllGamesByMatchId(match.getId()))
                    .build();

            matchResponseList.add(matchResponse);
        }
        return matchResponseList;
    }

    /**
     * Get the current maximum matchId
     *
     * @return Maximum matchId
     */
    private Integer getMaxMatchId() {
        Match match = matchRepository.findFirstByOrderByIdDesc();
        if (match == null) return 0;
        return match.getId();
    }

    /**
     * Return the match statistics of a user
     *
     * @param username Username of the given user
     * @return match statistics of the user
     */
    @SneakyThrows
    public DetailedMatchStatsResponse getDetailedMatchStatsByUsername(String username) {
        User user = userRepository.findByUsername(username);
        Integer userId = user.getUserId();

        List<Match> matches = matchRepository.findAllByPlayerId1AndMatchMode(userId, MatchMode.AUTO);
        matches.addAll(matchRepository.findAllByPlayerId1AndMatchMode(userId, MatchMode.MANUAL));
        matches.addAll(matchRepository.findAllByPlayerId2AndMatchMode(userId, MatchMode.AUTO));
        matches.addAll(matchRepository.findAllByPlayerId2AndMatchMode(userId, MatchMode.MANUAL));

        MatchStats initiated = new MatchStats();
        MatchStats faced = new MatchStats();
        MatchStats auto = new MatchStats();
        Integer totalMatches = matches.size();

        for (var match : matches) {
            if (match.getMatchMode() == MatchMode.AUTO) {
                if (match.getPlayerId1() == userId) {
                    switch (match.getVerdict()) {
                        case PLAYER1:
                            auto.setWins(auto.getWins() + 1);
                            break;
                        case PLAYER2:
                            auto.setLosses(auto.getLosses() + 1);
                            break;
                        default:
                            auto.setTies(auto.getTies() + 1);
                    }
                } else if (match.getPlayerId2() == userId) {
                    switch (match.getVerdict()) {
                        case PLAYER1:
                            auto.setLosses(auto.getLosses() + 1);
                            break;
                        case PLAYER2:
                            auto.setWins(auto.getWins() + 1);
                            break;
                        default:
                            auto.setTies(auto.getTies() + 1);
                    }
                }
            } else if (match.getPlayerId1() == userId) {
                switch (match.getVerdict()) {
                    case PLAYER1:
                        initiated.setWins(initiated.getWins() + 1);
                        break;
                    case PLAYER2:
                        initiated.setLosses(initiated.getLosses() + 1);
                        break;
                    default:
                        initiated.setTies(initiated.getTies() + 1);
                }
            } else if (match.getPlayerId2() == userId) {
                switch (match.getVerdict()) {
                    case PLAYER1:
                        faced.setLosses(faced.getLosses() + 1);
                        break;
                    case PLAYER2:
                        faced.setWins(faced.getWins() + 1);
                        break;
                    default:
                        faced.setTies(faced.getTies() + 1);
                }
            }
        }

        Date lastMatchAt = getLastInitiatedManualMatchTime(userId);

        return DetailedMatchStatsResponse.builder()
                .userId(userId)
                .numMatches(totalMatches)
                .initiated(initiated)
                .faced(faced)
                .auto(auto)
                .lastMatchAt(lastMatchAt)
                .build();
    }

    /**
     * Return the match statistics of a user
     *
     * @param username Username of the given user
     * @return match statistics of the user
     */
    @SneakyThrows
    public MatchStats getMatchStatsByUsername(String username) {
        User user = userRepository.findByUsername(username);
        Integer userId = user.getUserId();

        List<Match> matches = matchRepository.findAllByPlayerId1AndMatchMode(userId, MatchMode.AUTO);
        matches.addAll(matchRepository.findAllByPlayerId1AndMatchMode(userId, MatchMode.MANUAL));
        matches.addAll(matchRepository.findAllByPlayerId2AndMatchMode(userId, MatchMode.AUTO));
        matches.addAll(matchRepository.findAllByPlayerId2AndMatchMode(userId, MatchMode.MANUAL));

        var matchStats = new MatchStats();

        for (var match : matches) {
            switch (match.getVerdict()) {
                case PLAYER1:
                    if (match.getPlayerId1().equals(userId))
                        matchStats.setWins(matchStats.getWins() + 1);
                    else
                        matchStats.setLosses(matchStats.getLosses() + 1);
                    break;
                case PLAYER2:
                    if (match.getPlayerId2().equals(userId))
                        matchStats.setWins(matchStats.getWins() + 1);
                    else
                        matchStats.setLosses(matchStats.getLosses() + 1);
                    break;
                case TIE:
                    matchStats.setTies(matchStats.getTies() + 1);
                    break;
            }
        }

        return matchStats;
    }

    /**
     * Return the time of the recent match initiated by the user against a user
     * NOTE: If the user has not played any matches return time of user creation
     *
     * @param userId - User id of the user
     * @return if user played a Manual match, return time of last manual match played by user
     * else return time of user account creation
     */
    public Date getRecentMatchTime(Integer userId) {
        Match match = matchRepository.findFirstByPlayerId1AndMatchModeOrderByCreatedAtDesc(userId, MatchMode.MANUAL);
        if (match == null)
            return userRepository.findByUserId(userId).getCreatedAt();
        return match.getCreatedAt();
    }

    /**
     * Return the time of last match initiated by a user against another user
     *
     * @param userId - User id of the user
     * @return time of last manual match played by user
     * NOTE: return time of user account creation if user has not played any manual match
     */
    public Date getLastInitiatedManualMatchTime(Integer userId) {
        Match match = matchRepository.findFirstByPlayerId1AndMatchModeOrderByCreatedAtDesc(userId, MatchMode.MANUAL);
        if (match == null) {
            return userRepository.findByUserId(userId).getCreatedAt();
        }
        return match.getCreatedAt();
    }

    /**
     * Return the time of last Initiated match of a user
     * NOTE: Initiated match includes AI, SELF, PREV_COMMIT, MANUAL
     *
     * @param userId - User id of the user
     * @return if user initiated a match, return time of last match initiated by user
     * else return time of user account creation
     */
    public Long getLastInitiatedMatchTime(Integer userId) {
        Match match = matchRepository.findFirstByPlayerId1AndMatchModeNotOrderByCreatedAtDesc(userId, MatchMode.AUTO);
        if (match == null) {
            return (long) 0;
        }
        return match.getCreatedAt().getTime();
    }

    /**
     * Return the time(seconds) left for user to be able to initiate a match
     *
     * @param userId - UserID of the user
     * @return remaining time in seconds
     */
    @SneakyThrows
    public Long getWaitTime(@NotEmpty Integer userId) {
        Float minWaitTime = Float.parseFloat(constantRepository.findByKey("MATCH_WAIT_TIME").getValue());
        Long lastInitiatedMatchTime = getLastInitiatedMatchTime(userId);
        Date currentTime = new Date();

        // Seconds passed since last initiated match
        Long timePassedSeconds = (currentTime.getTime() - lastInitiatedMatchTime) / 1000;
        if (timePassedSeconds > minWaitTime)
            return (long) 0;
        return (long) (minWaitTime - timePassedSeconds);
    }

    /**
     * Updates the match in DB
     *
     * @param updateMatchRequest updateMatchRequest of the match
     */
    public void updateMatch(UpdateMatchRequest updateMatchRequest) {
        Boolean success = updateMatchRequest.getSuccess();
        Integer matchId = updateMatchRequest.getMatchId();
        Match match = matchRepository.findFirstById(matchId);

        if (!success) {
            match.setStatus(Status.EXECUTE_ERROR);
            matchRepository.save(match);

            socketService.sendMessage(socketMatchResultDest + match.getPlayerId1(), "Error: " + updateMatchRequest.getError());
            socketService.sendMessage(socketAlertMessageDest + match.getPlayerId1(), "Execute Error");
            return;
        }
        Verdict matchVerdict = deduceMatchVerdict(updateMatchRequest.getGameResults());

        List<GameLogs> gameLogsList = new ArrayList<>();
        var gameResults = updateMatchRequest.getGameResults();
        if (match.getMatchMode() != MatchMode.AUTO && match.getMatchMode() != MatchMode.MANUAL) {
            for (var game : gameResults) {
                var gameLogs = GameLogs.builder()
                        .playerId1(match.getPlayerId1())
                        .gameLog(game.getLog())
                        .player1Log(game.getPlayer1LogCompressed())
                        .player2Log(game.getPlayer2LogCompressed())
                        .build();
                gameLogsList.add(gameLogs);
            }

            Integer playerId = match.getPlayerId1();
            socketService.sendMessage(socketMatchResultDest + playerId, gameLogsList.toString());
            String matchMessage = getMatchResultByVerdict(matchId, matchVerdict, playerId);
            socketService.sendMessage(socketAlertMessageDest + playerId, matchMessage);
            createMatchNotification(playerId, matchMessage);
        }

        if (match.getMatchMode() == MatchMode.MANUAL) {
            List<String> player1Dlls = updateMatchRequest.getPlayer1DLLs();
            if (player1Dlls.size() != 0) {
                DllUtil.setDll(match.getPlayerId1(), DllId.DLL_1, player1Dlls.get(0));
                DllUtil.setDll(match.getPlayerId1(), DllId.DLL_2, player1Dlls.get(1));
            }

            // If match mode is manual, create a notification for player 2 also.
            Integer playerId = match.getPlayerId2();
            String matchMessage = getMatchResultByVerdict(matchId, matchVerdict, playerId);
            socketService.sendMessage(socketAlertMessageDest + playerId, matchMessage);
            createMatchNotification(playerId, matchMessage);

            // Add an entry to User rating table
            // NOTE: CalculateMatchRatings will add an entry in User Rating and update Leaderboard
            userRatingService.calculateMatchRatings(match.getPlayerId1(), match.getPlayerId2(), matchVerdict);
        }

        if (match.getMatchMode() == MatchMode.AUTO) {
            // TODO: Find whether an auto match is complete and send socket message
        }

        for (var gameResult : gameResults) {
            var game = gameRepository.findFirstById(gameResult.getId());
            game.setPoints1(gameResult.getPoints1());
            game.setPoints2(gameResult.getPoints2());
            game.setVerdict(gameResult.getVerdict());
            gameRepository.save(game);

            Integer gameId = gameResult.getId();
            LogUtil.createLogRepository(gameId);
            var gameLogs = GameLogs.builder()
                    .gameLog(gameResult.getLog())
                    .player1Log(gameResult.getPlayer1LogCompressed())
                    .player2Log(gameResult.getPlayer2LogCompressed())
                    .build();
            LogUtil.setLogs(gameId, gameLogs);
        }

        match.setStatus(Status.EXECUTED);
        match.setVerdict(matchVerdict);
        updateMatchScore(match, updateMatchRequest.getGameResults());
        matchRepository.save(match);
    }

    private void updateMatchScore(Match match, List<UpdateGameDetails> gameDetails) {
        Integer player1Wins = 0, player2Wins = 0;
        for (var game : gameDetails) {
            if (game.getVerdict().equals(PLAYER1))
                player1Wins++;
            if (game.getVerdict().equals(PLAYER2))
                player2Wins++;
        }

        match.setScore1(player1Wins);
        match.setScore2(player2Wins);
    }

    /**
     * Get result of a match by its verdict with respect to the player
     *
     * @param matchId  matchId of the match
     * @param verdict  verdict of the match
     * @param playerId userId of the player
     * @return The result of match with respect to the player
     */
    private String getMatchResultByVerdict(Integer matchId, Verdict verdict, Integer playerId) {
        Match match = matchRepository.findFirstById(matchId);
        boolean isPlayer1 = playerId.equals(match.getPlayerId1());

        Integer opponentId = match.getPlayerId1();
        if (isPlayer1)
            opponentId = match.getPlayerId2();

        String opponentUsername = userRepository.findByUserId(opponentId).getUsername();

        switch (verdict) {
            case TIE:
                return "Match tied against " + opponentUsername;
            case PLAYER1:
                if (isPlayer1)
                    return "Won match against " + opponentUsername;
                return "Lost match against " + opponentUsername;
            case PLAYER2:
                if (isPlayer1)
                    return "Lost match against " + opponentUsername;
                return "Won match against " + opponentUsername;
        }
        return "";
    }

    /**
     * Create a notification request for the player regarding the match
     *
     * @param playerId            userId of the player
     * @param notificationContent Content of the notification
     */
    private void createMatchNotification(Integer playerId, String notificationContent) {
        CreateNotificationRequest createNotificationRequest = CreateNotificationRequest.builder()
                .userId(playerId)
                .title("Match Result")
                .content(notificationContent)
                .type(Type.INFO)
                .build();
        notificationService.createNotification(createNotificationRequest);
    }

    /**
     * Get the verdict of a match
     *
     * @param gameDetails List of gameDetails
     * @return Verdict of the match
     */
    public Verdict deduceMatchVerdict(List<UpdateGameDetails> gameDetails) {
        Integer player1Wins = 0, player2Wins = 0;
        for (var game : gameDetails) {
            if (game.getVerdict().equals(PLAYER1))
                player1Wins++;
            if (game.getVerdict().equals(PLAYER2))
                player2Wins++;
        }

        if (player1Wins > player2Wins) return PLAYER1;
        if (player2Wins > player1Wins) return PLAYER2;
        return TIE;
    }
}
