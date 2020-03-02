package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.request.Notification.CreateNotificationRequest;
import delta.codecharacter.server.controller.request.UpdateGameDetails;
import delta.codecharacter.server.controller.request.UpdateMatchRequest;
import delta.codecharacter.server.controller.response.Match.DetailedMatchStatsResponse;
import delta.codecharacter.server.controller.response.Match.MatchResponse;
import delta.codecharacter.server.controller.response.Match.PrivateMatchResponse;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.ConstantRepository;
import delta.codecharacter.server.repository.MatchRepository;
import delta.codecharacter.server.repository.TopMatchRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.DllUtil;
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

    @Value("${compilebox.secret-key}")
    private String compileboxSecretKey;

    @Value("/response/alert/")
    private String socketAlertMessageDest;

    @Value("/response/match/")
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
                    .avatarId1(user1.getAvatarId())
                    .avatarId2(user2.getAvatarId())
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
    public List<PrivateMatchResponse> getManualAndAutoMatchesPaginated(Integer userId, Pageable pageable) {
        Aggregation aggregation = newAggregation(
                match(
                        new Criteria().andOperator(
                                new Criteria().orOperator(Criteria.where("player_id_1").is(userId), Criteria.where("player_id_2").is(userId)),
                                new Criteria().orOperator(Criteria.where("match_mode").is(MatchMode.MANUAL), Criteria.where("match_mode").is(MatchMode.AUTO))
                        )
                ),
                sort(Sort.by("createdAt").descending()),
                skip((long) pageable.getPageNumber() * pageable.getPageSize()),
                limit(pageable.getPageSize())
        );

        var groupResults = mongoTemplate.aggregate(aggregation, Match.class, Match.class);
        List<Match> matches = groupResults.getMappedResults();

        List<PrivateMatchResponse> privateMatchResponse = new ArrayList<>();
        for (var match : matches) {

            User user1 = userRepository.findByUserId(match.getPlayerId1());
            User user2 = userRepository.findByUserId(match.getPlayerId2());

            var matchResponse = PrivateMatchResponse.builder()
                    .username1(user1.getUsername())
                    .username2(user2.getUsername())
                    .avatar1(user1.getAvatarId())
                    .avatar2(user1.getAvatarId())
                    .score1(match.getScore1())
                    .score2(match.getScore2())
                    .verdict(match.getVerdict())
                    .playedAt(match.getCreatedAt())
                    .matchMode(match.getMatchMode())
                    .games(gameService.getAllGamesByMatchId(match.getId()))
                    .build();

            privateMatchResponse.add(matchResponse);
        }
        return privateMatchResponse;
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
                        case PLAYER_1:
                            auto.setWins(auto.getWins() + 1);
                            break;
                        case PLAYER_2:
                            auto.setLosses(auto.getLosses() + 1);
                            break;
                        default:
                            auto.setTies(auto.getTies() + 1);
                    }
                } else if (match.getPlayerId2() == userId) {
                    switch (match.getVerdict()) {
                        case PLAYER_1:
                            auto.setLosses(auto.getLosses() + 1);
                            break;
                        case PLAYER_2:
                            auto.setWins(auto.getWins() + 1);
                            break;
                        default:
                            auto.setTies(auto.getTies() + 1);
                    }
                }
            } else if (match.getPlayerId1() == userId) {
                switch (match.getVerdict()) {
                    case PLAYER_1:
                        initiated.setWins(initiated.getWins() + 1);
                        break;
                    case PLAYER_2:
                        initiated.setLosses(initiated.getLosses() + 1);
                        break;
                    default:
                        initiated.setTies(initiated.getTies() + 1);
                }
            } else if (match.getPlayerId2() == userId) {
                switch (match.getVerdict()) {
                    case PLAYER_1:
                        faced.setLosses(faced.getLosses() + 1);
                        break;
                    case PLAYER_2:
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
                case PLAYER_1:
                    if (match.getPlayerId1().equals(userId))
                        matchStats.setWins(matchStats.getWins() + 1);
                    else
                        matchStats.setLosses(matchStats.getLosses() + 1);
                    break;
                case PLAYER_2:
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

    public void updateMatch(UpdateMatchRequest updateMatchRequest) {
        LOG.info(updateMatchRequest.toString());
        if (updateMatchRequest.getSecretKey().equals(compileboxSecretKey))
            return;
        Boolean success = updateMatchRequest.getSuccess();

        Integer matchId = updateMatchRequest.getMatchId();
        Match match = matchRepository.findFirstById(matchId);

        Verdict matchVerdict = deduceMatchVerdict(updateMatchRequest.getGameResults());
        String matchResult;

        if (match.getMatchMode() != MatchMode.AUTO) {
            //TODO: Send Socket Message to both User
            //TODO: Create Notification for both users
            //TODO: Save Logs
        }
        else if (match.getMatchMode() == MatchMode.MANUAL) {
            List<String> player1Dlls = updateMatchRequest.getPlayer1DLLs();
            if (success && player1Dlls != null) {
                DllUtil.setDll(match.getPlayerId1(), DllId.DLL_1, player1Dlls.get(0));
                DllUtil.setDll(match.getPlayerId1(), DllId.DLL_2, player1Dlls.get(1));
            }

            matchResult = getVerdictResult(matchVerdict);
            socketService.sendMessage(socketMatchResultDest, matchResult);

            CreateNotificationRequest notificationRequestPlayer1;
            CreateNotificationRequest notificationRequestPlayer2;

            notificationService.createNotification(createMatchResultNotificationRequest(matchVerdict, match));
            notificationService.createNotification(createMatchResultNotificationRequest(matchVerdict, match));
            // Add an entry to User rating table
            // NOTE: CalculateMatchRatings will add an entry in User Rating and update Leaderboard
            userRatingService.calculateMatchRatings(match.getPlayerId1(), match.getPlayerId2(), matchVerdict);
        }
        else if (match.getMatchMode() == MatchMode.SELF) {
            matchResult = getVerdictResult(matchVerdict);
            socketService.sendMessage(socketMatchResultDest, matchResult);

            CreateNotificationRequest notificationRequestPlayer1;

            notificationService.createNotification(createMatchResultNotificationRequest(matchVerdict, match));
            // Add an entry to User rating table
            // NOTE: CalculateMatchRatings will add an entry in User Rating and update Leaderboard
            userRatingService.calculateMatchRatings(match.getPlayerId1(), match.getPlayerId2(), matchVerdict);
        }
        else if (match.getMatchMode() == MatchMode.PREV_COMMIT) {
            matchResult = getVerdictResult(matchVerdict);
            socketService.sendMessage(socketMatchResultDest, matchResult);

            CreateNotificationRequest notificationRequestPlayer1;

            notificationService.createNotification(createMatchResultNotificationRequest(matchVerdict, match));
            // Add an entry to User rating table
            // NOTE: CalculateMatchRatings will add an entry in User Rating and update Leaderboard
            userRatingService.calculateMatchRatings(match.getPlayerId1(), match.getPlayerId2(), matchVerdict);
        }
    }

    private String getVerdictResult(Verdict verdict) {
        switch (verdict) {
            case TIE:
                return "Match tied";
            case PLAYER_1:
                return "Player 1 won";
            case PLAYER_2:
                return "Player 2 won";
            default:
                return "No Result";
        }
    }

    private CreateNotificationRequest createMatchResultNotificationRequest(Verdict matchVerdict, Match match) {
        CreateNotificationRequest createNotificationRequest;

        String playerName1 = userRepository.findByUserId(match.getPlayerId1()).getUsername();
        String playerName2 = userRepository.findByUserId(match.getPlayerId2()).getUsername();

        switch (matchVerdict) {
            case TIE:
                createNotificationRequest = CreateNotificationRequest.builder()
                        .userId(match.getPlayerId1())
                        .title("Match Result")
                        .content("Match tied against " + playerName2)
                        .type(Type.INFO)
                        .build();
                break;
            case PLAYER_1:
                createNotificationRequest = CreateNotificationRequest.builder()
                        .userId(match.getPlayerId1())
                        .title("Match Result")
                        .content("You won the match against " + playerName2)
                        .type(Type.INFO)
                        .build();
                break;
            case PLAYER_2:
                createNotificationRequest = CreateNotificationRequest.builder()
                        .userId(match.getPlayerId1())
                        .title("Match Result")
                        .content("You lost the match against " + playerName2)
                        .type(Type.INFO)
                        .build();
                break;
            default:
                createNotificationRequest = CreateNotificationRequest.builder()
                        .userId(match.getPlayerId1())
                        .title("Match Result")
                        .content("No result for match against " + playerName2)
                        .type(Type.INFO)
                        .build();
                break;
        }
        return createNotificationRequest;
    }

    public Verdict deduceMatchVerdict(List<UpdateGameDetails> gameDetails) {
        Integer player1Wins = 0, player2Wins = 0;
        for (var game : gameDetails) {
            if (game.getVerdict().equals(PLAYER_1))
                player1Wins++;
            if (game.getVerdict().equals(PLAYER_2))
                player2Wins++;
        }
        if (player1Wins > player2Wins) return PLAYER_1;
        if (player2Wins > player1Wins) return PLAYER_2;
        return TIE;
    }
}
