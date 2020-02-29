package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.response.Game.GameResponse;
import delta.codecharacter.server.controller.response.Match.MatchResponse;
import delta.codecharacter.server.controller.response.UserMatchStatsResponse;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.ConstantRepository;
import delta.codecharacter.server.repository.MatchRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.UserMatchStatData;
import delta.codecharacter.server.util.enums.MatchMode;
import delta.codecharacter.server.util.enums.Status;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Service
public class MatchService {

    private final Logger LOG = Logger.getLogger(UserController.class.getName());

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ConstantRepository constantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private GameService gameService;

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

    public List<MatchResponse> getAllMatchesByUserId(Integer userId) {
        User user1 = userRepository.findByUserId(userId);
        List<Match> matches = matchRepository.findAllByPlayerId1AndMatchMode(userId, MatchMode.AUTO);
        matches.addAll(matchRepository.findAllByPlayerId1AndMatchMode(userId, MatchMode.MANUAL));
        List<MatchResponse> matchResponses = new ArrayList<>();
        for (var match : matches) {
            User user2 = userRepository.findByUserId(match.getPlayerId2());
            MatchResponse matchResponse = MatchResponse.builder()
                    .username1(user1.getUsername())
                    .username2(user2.getUsername())
                    .avatar1(user1.getAvatarId())
                    .avatar2(user1.getAvatarId())
                    .score1(match.getScore1())
                    .score2(match.getScore2())
                    .verdict(match.getVerdict())
                    .playedAt(match.getCreatedAt())
                    .build();
            List<GameResponse> gameResponses = new ArrayList<>();
            for (var game : gameService.findAllGamesByMatchId(match.getId())) {
                gameResponses.add(GameResponse.builder()
                        .id(game.getId())
                        .mapId(game.getMapId())
                        .verdict(game.getVerdict())
                        .build());
            }
            matchResponse.setGames(gameResponses);
        }
        return matchResponses;
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
    public UserMatchStatsResponse getUserMatchStats(String username) {
        User user = userRepository.findByUsername(username);
        Integer userId = user.getUserId();

        List<Match> matches = matchRepository.findAllByPlayerId1AndMatchMode(userId, MatchMode.AUTO);
        matches.addAll(matchRepository.findAllByPlayerId1AndMatchMode(userId, MatchMode.MANUAL));
        matches.addAll(matchRepository.findAllByPlayerId2AndMatchMode(userId, MatchMode.AUTO));
        matches.addAll(matchRepository.findAllByPlayerId2AndMatchMode(userId, MatchMode.MANUAL));

        UserMatchStatData initiated = new UserMatchStatData();
        UserMatchStatData faced = new UserMatchStatData();
        UserMatchStatData auto = new UserMatchStatData();
        Integer totalMatches = matches.size();

        for (var match : matches) {
            if (match.getMatchMode() == MatchMode.AUTO) {
                if (match.getPlayerId1() == userId) {
                    switch (match.getVerdict()) {
                        case PLAYER_1:
                            auto.wins++;
                            break;
                        case PLAYER_2:
                            auto.losses++;
                            break;
                        default:
                            auto.ties++;
                    }
                } else if (match.getPlayerId2() == userId) {
                    switch (match.getVerdict()) {
                        case PLAYER_1:
                            auto.losses++;
                            break;
                        case PLAYER_2:
                            auto.wins++;
                            break;
                        default:
                            auto.ties++;
                    }
                }
            } else if (match.getPlayerId1() == userId) {
                switch (match.getVerdict()) {
                    case PLAYER_1:
                        initiated.wins++;
                        break;
                    case PLAYER_2:
                        initiated.losses++;
                        break;
                    default:
                        initiated.ties++;
                }
            } else if (match.getPlayerId2() == userId) {
                switch (match.getVerdict()) {
                    case PLAYER_1:
                        faced.losses++;
                        break;
                    case PLAYER_2:
                        faced.wins++;
                        break;
                    default:
                        faced.ties++;
                }
            }
        }

        Date lastMatchAt = getLastInitiatedManualMatchTime(userId);

        return UserMatchStatsResponse.builder()
                .userId(userId)
                .numMatches(totalMatches)
                .initiated(initiated)
                .faced(faced)
                .auto(auto)
                .lastMatchAt(lastMatchAt)
                .build();
    }

    /**
     * Return the time of last match initiated by a user against another user
     *
     * @param userId - User id of the user
     * @return if user played a Manual match, return time of last manual match played by user
     * else return time of user account creation
     */
    @SneakyThrows
    public Date getLastInitiatedManualMatchTime(Integer userId) {
        Match match = matchRepository.findFirstByPlayerId1AndMatchModeOrderByCreatedAtDesc(userId, MatchMode.MANUAL);
        if (match == null) {
            return null;
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
        LOG.info("userid: " + userId + " match: " + match);
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

}
