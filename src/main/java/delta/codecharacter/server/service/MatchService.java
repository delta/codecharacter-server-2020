package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.response.UserMatchStatsResponse;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.repository.ConstantRepository;
import delta.codecharacter.server.repository.MatchRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.MatchMode;
import delta.codecharacter.server.util.Verdict;
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
    MongoTemplate mongoTemplate;

    @Autowired
    private ConstantRepository constantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

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
        class LeaderboardData {
            Integer wins = 0;
            Integer losses = 0;
            Integer ties = 0;
        }
        LeaderboardData initiated = new LeaderboardData();
        LeaderboardData faced = new LeaderboardData();
        LeaderboardData auto = new LeaderboardData();
        Integer totalMatches = 0;
        Date lastMatchAt = matchRepository.findFirstByPlayerId1AndMatchModeNotOrderByCreatedAtDesc(userId, MatchMode.AUTO).getCreatedAt();

        for (var match : matches) {
            if (match.getMatchMode() == MatchMode.AUTO) {
                if ((match.getPlayerId1() == userId && match.getVerdict() == Verdict.PLAYER_1) || (match.getPlayerId2() == userId && match.getVerdict() == Verdict.PLAYER_2))
                    auto.wins++;
                else if ((match.getPlayerId1() == userId && match.getVerdict() == Verdict.PLAYER_2) || (match.getPlayerId2() == userId && match.getVerdict() == Verdict.PLAYER_1))
                    auto.losses++;
                else if (match.getVerdict() == Verdict.TIE)
                    auto.ties++;
            } else if (match.getPlayerId1() == userId) {
                if (match.getVerdict() == Verdict.PLAYER_1)
                    initiated.wins++;
                else if (match.getVerdict() == Verdict.PLAYER_2)
                    initiated.losses++;
                else if (match.getVerdict() == Verdict.TIE)
                    initiated.ties++;
            } else if (match.getPlayerId2() == userId) {
                if (match.getVerdict() == Verdict.PLAYER_2)
                    faced.wins++;
                else if (match.getVerdict() == Verdict.PLAYER_1)
                    faced.losses++;
                else if (match.getVerdict() == Verdict.TIE)
                    faced.ties++;
            }
            totalMatches++;
        }

        return UserMatchStatsResponse.builder()
                .userId(userId)
                .numMatches(totalMatches)
                .initiatedWins(initiated.wins)
                .initiatedLosses(initiated.losses)
                .initiatedTies(initiated.ties)
                .facedWins(faced.wins)
                .facedLosses(faced.losses)
                .facedTies(faced.ties)
                .autoWins(auto.wins)
                .autoLosses(auto.losses)
                .autoTies(auto.ties)
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
    public Float getWaitTime(@NotEmpty String username) {
        if (userRepository.findByUsername(username) == null)
            throw new Exception("Invalid username");

        Integer userId = userRepository.findByUsername(username).getUserId();
        Float minWaitTime = Float.parseFloat(constantRepository.findByKey("MATCH_WAIT_TIME").getValue());
        Date lastMatchTime = matchRepository.findFirstByPlayerId1AndMatchModeNotOrderByCreatedAtDesc(userId, MatchMode.AUTO).getCreatedAt();
        Date currentTime = new Date();

        Long timepassed = (currentTime.getTime() - lastMatchTime.getTime()) / 1000;
        if (timepassed > minWaitTime)
            return Float.valueOf(0);
        else
            return (minWaitTime - timepassed);
    }

}
