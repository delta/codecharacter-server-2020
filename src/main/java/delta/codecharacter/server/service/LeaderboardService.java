package delta.codecharacter.server.service;

import delta.codecharacter.server.controller.api.UserController;
import delta.codecharacter.server.controller.response.LeaderboardResponse;
import delta.codecharacter.server.model.Leaderboard;
import delta.codecharacter.server.repository.LeaderboardRepository;
import delta.codecharacter.server.repository.UserRepository;
import delta.codecharacter.server.util.Division;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.logging.Logger;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Service
public class LeaderboardService {

    private final Logger LOG = Logger.getLogger(UserController.class.getName());
    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaderboardRepository leaderboardRepository;

    /**
     * Initialize leaderboard data for new user
     *
     * @param userId - User id of the new user
     */
    @Transactional
    public void initializeLeaderboardData(@NotNull Integer userId) {
        Leaderboard leaderboard = Leaderboard.builder()
                .userId(userId)
                .division(Division.DIV_2)
                .rating(1200)
                .build();

        leaderboardRepository.save(leaderboard);
    }

    /**
     * Get all users with rank of given pageSize
     *
     * @param pageNumber page number
     * @param pageSize   page size
     * @return list of users of given pageSize
     */
    public List<LeaderboardResponse> getLeaderboardData(Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Aggregation aggregation = newAggregation(
                lookup("user", "user_id", "_id", "join"),
                sort(Sort.by("rating").descending().and(Sort.by("join.username").ascending())),
                skip((long)pageable.getPageNumber() * pageable.getPageSize()),
                limit(pageable.getPageSize())
        );

        AggregationResults<LeaderboardResponse> groupResults = mongoTemplate.aggregate(
                aggregation, Leaderboard.class, LeaderboardResponse.class);
        List<LeaderboardResponse> leaderboard = groupResults.getMappedResults();
        for (var leaderboardData : leaderboard) {
            leaderboardData.setUsername(userRepository.findByUserId(leaderboardData.getUserId()).getUsername());
        }
        return leaderboard;
    }

    /**
     * Search for users with regex-matching username of given pageSize
     *
     * @param username   username of user
     * @param pageNumber page number
     * @param pageSize   page size
     * @return return users with regex-matching username of given pageSize
     */
    public List<LeaderboardResponse> searchLeaderboardByUsername(String username, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Aggregation aggregation = newAggregation(
                lookup("user", "user_id", "_id", "join"),
                match(Criteria.where("join.username").regex(username)),
                sort(Sort.by("rating").descending().and(Sort.by("join.username").ascending())),
                skip((long)pageable.getPageNumber() * pageable.getPageSize()),
                limit(pageable.getPageSize())
        );

        AggregationResults<LeaderboardResponse> groupResults = mongoTemplate.aggregate(
                aggregation, Leaderboard.class, LeaderboardResponse.class);
        List<LeaderboardResponse> leaderboard = groupResults.getMappedResults();

        for (var leaderboardData : leaderboard) {
            leaderboardData.setUsername(userRepository.findByUserId(leaderboardData.getUserId()).getUsername());
        }
        return leaderboard;
    }

    /**
     * Get details of users playing in given division of given pageSize
     *
     * @param division   desired division
     * @param pageNumber page number
     * @param pageSize   page size
     * @return list of users playing in the given division of given pageSize
     */
    public List<LeaderboardResponse> getLeaderboardDataByDivision(Division division, Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        Aggregation aggregation = newAggregation(
                match(Criteria.where("division").is(division)),
                lookup("user", "user_id", "_id", "join"),
                sort(Sort.by("rating").descending().and(Sort.by("join.username").ascending())),
                skip((long)pageable.getPageNumber() * pageable.getPageSize()),
                limit(pageable.getPageSize())
        );

        AggregationResults<LeaderboardResponse> groupResults = mongoTemplate.aggregate(
                aggregation, Leaderboard.class, LeaderboardResponse.class);
        List<LeaderboardResponse> leaderboard = groupResults.getMappedResults();
        for (var leaderboardData : leaderboard) {
            leaderboardData.setUsername(userRepository.findByUserId(leaderboardData.getUserId()).getUsername());
        }
        return leaderboard;
    }

}
