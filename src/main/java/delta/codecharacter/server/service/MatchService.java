package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.response.Match.DetailedMatchStatsResponse;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.model.User;
import delta.codecharacter.server.repository.ConstantRepository;
import delta.codecharacter.server.repository.MatchRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.MatchStats;
import delta.codecharacter.server.util.enums.MatchMode;
import delta.codecharacter.server.util.enums.Status;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

@Service
public class MatchService {

    private final Logger LOG = Logger.getLogger(UserController.class.getName());

    @Autowired
    private ConstantRepository constantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

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

}
