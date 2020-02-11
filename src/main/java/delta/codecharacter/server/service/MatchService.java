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

        Integer totalMatches = 0;
        Integer initiatedWins = 0, initiatedLosses = 0, initiatedTies = 0;
        Integer facedWins = 0, facedLosses = 0, facedTies = 0;
        Integer autoWins = 0, autoLosses = 0, autoTies = 0;
        Date lastMatchAt = matchRepository.findFirstByPlayerId1AndMatchModeNotOrderByCreatedAtDesc(userId, MatchMode.AUTO).getCreatedAt();

        for (var match : matches) {
            if (match.getMatchMode() == MatchMode.AUTO) {
                if ((match.getPlayerId1() == userId && match.getVerdict() == Verdict.PLAYER_1) || (match.getPlayerId2() == userId && match.getVerdict() == Verdict.PLAYER_2))
                    autoWins++;
                else if ((match.getPlayerId1() == userId && match.getVerdict() == Verdict.PLAYER_2) || (match.getPlayerId2() == userId && match.getVerdict() == Verdict.PLAYER_1))
                    autoLosses++;
                else if (match.getVerdict() == Verdict.TIE)
                    autoTies++;
            } else if (match.getPlayerId1() == userId) {
                if (match.getVerdict() == Verdict.PLAYER_1)
                    initiatedWins++;
                else if (match.getVerdict() == Verdict.PLAYER_2)
                    initiatedLosses++;
                else if (match.getVerdict() == Verdict.TIE)
                    initiatedTies++;
            } else if (match.getPlayerId2() == userId) {
                if (match.getVerdict() == Verdict.PLAYER_2)
                    facedWins++;
                else if (match.getVerdict() == Verdict.PLAYER_1)
                    facedLosses++;
                else if (match.getVerdict() == Verdict.TIE)
                    facedTies++;
            }
        }

        return UserMatchStatsResponse.builder()
                .userId(userId)
                .numMatches(totalMatches)
                .initiatedWins(initiatedWins)
                .initiatedLosses(initiatedLosses)
                .initiatedTies(initiatedTies)
                .facedWins(facedWins)
                .facedLosses(facedLosses)
                .facedTies(facedTies)
                .autoWins(autoWins)
                .autoLosses(autoLosses)
                .autoTies(autoTies)
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
