package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.response.AggregateResponse;
import delta.codecharacter.server.controller.response.UserStatsResponse;
import delta.codecharacter.server.model.Match;
import delta.codecharacter.server.repository.ConstantRepository;
import delta.codecharacter.server.repository.MatchRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.Mode;
import delta.codecharacter.server.util.Verdict;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotEmpty;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class MatchService {

    private final Logger LOG = Logger.getLogger(UserController.class.getName());

    @Autowired
    private ConstantRepository constantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    MongoTemplate mongoTemplate;

    /**
     * Return the user-stats of a user
     *
     * @param username - Username of the given user
     * @return user-stats of the user
     */
    @SneakyThrows
    public UserStatsResponse getUserStats(@NotEmpty String username) {
        if (userRepository.findByUsername(username) == null)
            throw new Exception("Invalid username");
        Integer userId = userRepository.findByUsername(username).getUserId();

        //agregation-------------
        Aggregation aggregation = newAggregation(
                match(Criteria.where("playerId1").is(userId)),
                group("verdict", "matchMode").count().as("total")
        );

        AggregationResults<AggregateResponse> groupResults = mongoTemplate.aggregate(
                aggregation, Match.class, AggregateResponse.class);

        List<AggregateResponse> matches = groupResults.getMappedResults();
        for (var match : matches) {
            LOG.info("checkGrouping:" + match.getVerdict() + " " + match.getMatchMode() + " " + match.getTotal());
        }
        //-------------------------

        Long initiatedWins = matchRepository.countByPlayerId1AndVerdictAndMatchMode(userId, Verdict.PLAYER_1, Mode.MANUAL_PLAYER);
        Long facedWins = matchRepository.countByPlayerId2AndVerdictAndMatchMode(userId, Verdict.PLAYER_2, Mode.MANUAL_PLAYER);
        Long autoWins = matchRepository.countByPlayerId1AndVerdictAndMatchMode(userId, Verdict.PLAYER_1, Mode.AUTO_PLAYER)
                + matchRepository.countByPlayerId2AndVerdictAndMatchMode(userId, Verdict.PLAYER_2, Mode.AUTO_PLAYER);

        Long initiatedLosses = matchRepository.countByPlayerId1AndVerdictAndMatchMode(userId, Verdict.PLAYER_2, Mode.MANUAL_PLAYER);
        Long facedLosses = matchRepository.countByPlayerId2AndVerdictAndMatchMode(userId, Verdict.PLAYER_1, Mode.MANUAL_PLAYER);
        Long autoLosses = matchRepository.countByPlayerId1AndVerdictAndMatchMode(userId, Verdict.PLAYER_2, Mode.AUTO_PLAYER)
                + matchRepository.countByPlayerId2AndVerdictAndMatchMode(userId, Verdict.PLAYER_1, Mode.AUTO_PLAYER);

        Long initiatedTies = matchRepository.countByPlayerId1AndVerdictAndMatchMode(userId, Verdict.TIE, Mode.MANUAL_PLAYER);
        Long facedTies = matchRepository.countByPlayerId2AndVerdictAndMatchMode(userId, Verdict.TIE, Mode.MANUAL_PLAYER);
        Long autoTies = matchRepository.countByPlayerId1AndVerdictAndMatchMode(userId, Verdict.TIE, Mode.AUTO_PLAYER)
                + matchRepository.countByPlayerId2AndVerdictAndMatchMode(userId, Verdict.TIE, Mode.AUTO_PLAYER);

        Long totalMatches = matchRepository.countByPlayerId1OrPlayerId2(userId, userId);
        Date lastMatchAt = matchRepository.findFirstByPlayerId1AndMatchModeNotOrderByCreatedAtDesc(userId, Mode.AUTO_PLAYER).getCreatedAt();

        return UserStatsResponse.builder()
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
        Date lastMatchTime = matchRepository.findFirstByPlayerId1AndMatchModeNotOrderByCreatedAtDesc(userId, Mode.AUTO_PLAYER).getCreatedAt();
        Date currentTime = new Date();

        Long timepassed = (currentTime.getTime() - lastMatchTime.getTime()) / 1000;
        if (timepassed > minWaitTime)
            return Float.valueOf(0);
        else
            return (minWaitTime - timepassed);
    }

}
