package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.response.UserMatchStatsResponse;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.repository.ConstantRepository;
import delta.codecharacter.server.repository.MatchRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.UserMatchStatData;
import delta.codecharacter.server.util.enums.MatchMode;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
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
                .score1(0)
                .score2(0)
                .matchMode(matchMode)
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
     * @param username - Username of the given user
     * @return match statistics of the user
     */
    @SneakyThrows
    public UserMatchStatsResponse getUserMatchStats(@NotEmpty String username) {
        if (userRepository.findByUsername(username) == null)
            throw new Exception("Invalid username");
        Integer userId = userRepository.findByUsername(username).getUserId();

        List<Match> matches = matchRepository.findAllByPlayerId1OrPlayerId2(userId, userId);

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

        Date lastMatchAt = matchRepository.findFirstByPlayerId1AndMatchModeNotOrderByCreatedAtDesc(userId, MatchMode.AUTO).getCreatedAt();

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
     * Return the time(seconds) left for user to be able to attack next
     *
     * @param username - Username of the user
     * @return remaining time in seconds
     */
    @SneakyThrows
    public Long getWaitTime(@NotEmpty String username) {
        if (userRepository.findByUsername(username) == null)
            throw new Exception("Invalid username");

        Integer userId = userRepository.findByUsername(username).getUserId();
        Float minWaitTime = Float.parseFloat(constantRepository.findByKey("MATCH_WAIT_TIME").getValue());
        Date lastMatchTime = matchRepository.findFirstByPlayerId1AndMatchModeNotOrderByCreatedAtDesc(userId, MatchMode.AUTO).getCreatedAt();
        Date currentTime = new Date();

        // Seconds passed since last initiated match
        Long timePassedSeconds = (currentTime.getTime() - lastMatchTime.getTime()) / 1000;
        if (timePassedSeconds > minWaitTime)
            return (long) 0;
        else
            return (long) (minWaitTime - timePassedSeconds);
    }

}
